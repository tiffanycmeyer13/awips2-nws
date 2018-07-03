/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.configparams.support;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.localization.climate.climodates.ClimoDates;
import gov.noaa.nws.ocp.common.localization.climate.climodates.ClimoDatesManager;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;

/**
 * 
 * Dialog for "View/Edit Annual Periods"
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/16/2016   20639    wkwock      Initial creation
 * 10/14/2016   20639    wkwock      use SnowPrecipServiceRequest
 * 10/18/2016   20639    wkwock      Remove back ground color
 * 20 DEC 2016  26404    amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 12 MAY 2017  33104    amoore      Address FindBugs.
 * 27 JUL 2017  33104    amoore      Do not use effectively final functionality, for 1.7 build.
 * 01 MAR 2018  44624    amoore      Clean up logic for determining new seasons.
 * 05 APR 2019  DR17116  wpaintsil   Add the ability to specify up to 4 alternate 
 *                                   annual periods for snow and precip.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class AnnualDialog extends ClimateCaveChangeTrackDialog {
    /**
     * snow composite
     */
    private Composite[] snowTimeComp = new Composite[4];

    /**
     * current 3 month season radio button in snow composite
     */
    private Button snowCurrentRdo;

    /**
     * alternate 3 month season radio button in snow composite
     */
    private Button snowAlternateRdo;

    /**
     * month combo in the snow composite
     */
    private Combo[] snowMonthCbo = new Combo[4];

    /**
     * day spinner in snow composite
     */
    private Spinner[] snowDaySpn = new Spinner[4];

    /**
     * checkboxes for alternate snow periods
     */
    private Button[] snowAlternateChk = new Button[4];

    /**
     * precipitation composite
     */
    private Composite precipTimeComp[] = new Composite[4];

    /**
     * current 3 month season in precipitation composite
     */
    private Button precipCurrentRdo;

    /**
     * alternate 3 month season in precipitation composite
     */
    private Button precipAlternateRdo;

    /**
     * month combo in precipitation composite
     */
    private Combo[] precipMonthCbo = new Combo[4];

    /**
     * day spinner for precipitation
     */
    private Spinner[] precipDaySpn = new Spinner[4];

    /**
     * checkboxes for alternate precip periods
     */
    private Button[] precipAlternateChk = new Button[4];

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AnnualDialog.class);

    /**
     * bold font
     */
    private Font boldFont;

    public AnnualDialog(Shell parent) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        setText("View/Edit Annual Periods");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridLayout shellGL = new GridLayout(1, false);
        shellGL.marginWidth = 10;
        shellGL.marginHeight = 10;
        shell.setLayout(shellGL);

        setupFont();

        createPeriodGrp(shell);

        createBottomButtons(shell);

        displayAnnualPeriods();
    }

    /**
     * setup font
     */
    private void setupFont() {
        FontData fontData = shell.getFont().getFontData()[0];

        if (boldFont == null) {
            boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                    fontData.getHeight(), SWT.BOLD));
        }

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (boldFont != null) {
                    boldFont.dispose();
                }
            }
        });
    }

    /**
     * create the 'Alternate Annual Period' group
     * 
     * @param shell
     */
    private void createPeriodGrp(Shell shell) {
        Group bigPeriodGrp = new Group(shell, SWT.BORDER);
        GridLayout grpGl = new GridLayout(2, false);
        grpGl.verticalSpacing = 20;
        grpGl.marginWidth = 10;
        grpGl.marginHeight = 10;
        bigPeriodGrp.setLayout(grpGl);
        bigPeriodGrp.setText("Alternate Annual Period");

        createSnowGrp(bigPeriodGrp);
        createPrecipGrp(bigPeriodGrp);
    }

    /**
     * create the snow group
     * 
     * @param periodComp
     */
    private void createSnowGrp(Composite periodComp) {
        Group periodGrp = new Group(periodComp, SWT.SHADOW_IN);
        periodGrp.setLayout(new GridLayout(1, false));
        periodGrp.setText("Snow");
        periodGrp.setFont(boldFont);

        snowCurrentRdo = new Button(periodGrp, SWT.RADIO);
        snowCurrentRdo.setText("Current 3 Month Season");
        snowCurrentRdo.addListener(SWT.Selection, changeListener);

        snowAlternateRdo = new Button(periodGrp, SWT.RADIO);
        snowAlternateRdo.setText("Alternate 3 Month Season(s):");

        for (int ii = 0; ii < 4; ii++) {
            Composite timeSelectComp = new Composite(periodGrp, SWT.NONE);
            timeSelectComp.setLayout(new GridLayout(2, false));

            snowTimeComp[ii] = timeComposite(ii, timeSelectComp, snowMonthCbo,
                    snowDaySpn, ClimoDates.SNOW_DEFAULT_MON, snowAlternateChk);

        }

        timeCascadeToggle(snowAlternateChk);

        snowAlternateRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                enableTimes(snowAlternateRdo.getSelection(), snowTimeComp,
                        snowAlternateChk);
            }
        });

        snowCurrentRdo.setSelection(true);
        enableTimes(snowAlternateRdo.getSelection(), snowTimeComp,
                snowAlternateChk);

    }

    /**
     * create the precipitation group
     *
     * @param periodComp
     */
    private void createPrecipGrp(Composite periodComp) {
        Group periodGrp = new Group(periodComp, SWT.SHADOW_IN);
        periodGrp.setLayout(new GridLayout(1, false));
        periodGrp.setText("Precip");
        periodGrp.setFont(boldFont);

        precipCurrentRdo = new Button(periodGrp, SWT.RADIO);
        precipCurrentRdo.setText("Current 3 Month Season");
        precipCurrentRdo.addListener(SWT.Selection, changeListener);

        precipAlternateRdo = new Button(periodGrp, SWT.RADIO);
        precipAlternateRdo.setText("Alternate 3 Month Season(s):");

        for (int ii = 0; ii < 4; ii++) {
            Composite timeSelectComp = new Composite(periodGrp, SWT.NONE);
            timeSelectComp.setLayout(new GridLayout(2, false));

            precipTimeComp[ii] = timeComposite(ii, timeSelectComp,
                    precipMonthCbo, precipDaySpn, ClimoDates.PRECIP_DEFAULT_MON,
                    precipAlternateChk);

        }

        timeCascadeToggle(precipAlternateChk);

        precipAlternateRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                enableTimes(precipAlternateRdo.getSelection(), precipTimeComp,
                        precipAlternateChk);
            }
        });

        precipCurrentRdo.setSelection(true);
        enableTimes(precipAlternateRdo.getSelection(), precipTimeComp,
                precipAlternateChk);
    }

    /**
     * Create a composite that can be used in the Snow or Precip group.
     *
     * @param parent
     * @param monthCombo
     * @param daySpinner
     * @param defaultMon
     * @return Composite
     */
    private Composite timeComposite(int index, Composite parent,
            Combo[] monthCombo, Spinner[] daySpinner, int defaultMon,
            Button[] alternateChk) {

        alternateChk[index] = new Button(parent, SWT.CHECK);

        Composite timeComp = new Composite(parent, SWT.BORDER);
        timeComp.setLayout(new GridLayout(3, false));

        alternateChk[index].addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                enableTimes(alternateChk[index].getSelection(), timeComp);
            }
        });

        Label fake1Lbl = new Label(timeComp, SWT.NONE);
        fake1Lbl.setText("");

        Label monthLbl = new Label(timeComp, SWT.NONE);
        monthLbl.setText("Month");

        Label dayLbl = new Label(timeComp, SWT.NONE);
        dayLbl.setText("Day");

        Label startLbl = new Label(timeComp, SWT.NONE);
        startLbl.setText("Start:");

        monthCombo[index] = new Combo(timeComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        for (int i = 0; i < 12; i++) { // avoid months[12], which is empty
            monthCombo[index].add(months[i]);
        }
        monthCombo[index].select(defaultMon - 1);
        monthCombo[index].addListener(SWT.Modify, changeListener);

        daySpinner[index] = new Spinner(timeComp, SWT.BORDER);
        daySpinner[index].setMinimum(1);
        daySpinner[index].setMaximum(31);
        daySpinner[index].addListener(SWT.Modify, changeListener);

        return timeComp;
    }

    /**
     * Create save and cancel buttons
     * 
     * @param shell
     */
    private void createBottomButtons(final Shell shell) {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 20;
        buttonComp.setLayout(gl);

        Button saveBtn = new Button(buttonComp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setFont(boldFont);

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveAnnualPreferences();
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setFont(boldFont);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * Set up the checkboxes such that checking the first enables the second and
     * so on. Unchecking the first unchecks and disables the ones after it and
     * so on.
     * 
     * @param alternateChk
     *            snow or precip alternate period checkboxes
     */
    private void timeCascadeToggle(Button[] alternateChk) {
        alternateChk[0].setVisible(false);

        for (int ii = 1; ii < 3; ii++) {
            final int index = ii;

            alternateChk[ii].addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (!alternateChk[index].getSelection()
                            && alternateChk[index + 1].getSelection()) {
                        alternateChk[index + 1].setSelection(false);
                        alternateChk[index + 1].notifyListeners(SWT.Selection,
                                new Event());
                    }
                    alternateChk[index + 1]
                            .setEnabled(alternateChk[index].getSelection());
                }
            });
        }
    }

    /**
     * enable controls in a composite
     * 
     * @param currentBtn
     * @param composite
     */
    private void enableTimes(boolean enable, Composite composite) {

        for (Control control : composite.getChildren()) {
            control.setEnabled(enable);
        }
    }

    /**
     * Enable controls in the snow/precip composites.
     * 
     * @param currentBtn
     * @param composite
     */
    private void enableTimes(boolean enable, Composite[] composite,
            Button[] alternateChk) {
        if (!enable) {
            for (int ii = 0; ii < 4; ii++) {
                alternateChk[ii].setEnabled(false);
                enableTimes(false, composite[ii]);
            }
        } else {
            enableTimes(true, composite[0]);

            // Enable each checkbox and its time selection if the previous
            // checkbox/time selection checkbox is checked.
            for (int ii = 1; ii < 4; ii++) {
                alternateChk[ii].setEnabled(
                        ii == 1 ? true : alternateChk[ii - 1].getSelection());
                enableTimes(alternateChk[ii].getSelection(), composite[ii]);
            }
        }
    }

    /**
     * Save annual period preferences
     */
    protected void saveAnnualPreferences() {
        boolean save = MessageDialog.openQuestion(shell, "Continue to save?",
                "If you save these changes, you should also retrieve and save \n"
                        + "all existing products related to the changes. \nDo you wish to continue?");
        if (!save) {
            return;
        }

        boolean snowContinue = true;
        boolean precipContinue = true;

        if (snowAlternateRdo.getSelection() && checkContinue(snowAlternateChk,
                snowDaySpn, snowMonthCbo, ClimoDates.SNOW_DEFAULT_DAY,
                ClimoDates.SNOW_DEFAULT_MON)) {
            snowContinue = MessageDialog.openQuestion(shell, "Warning",
                    "The snow start date(s) indicated will result in seasonal sums"
                            + " for\nthe current 3 month season. \nContinue?");
        }

        if (precipAlternateRdo.getSelection() && checkContinue(
                precipAlternateChk, precipDaySpn, precipMonthCbo,
                ClimoDates.PRECIP_DEFAULT_DAY, ClimoDates.PRECIP_DEFAULT_MON)) {
            precipContinue = MessageDialog.openQuestion(shell, "Warning",
                    "The precip start date(s) indicated will result in seasonal sums "
                            + "for\nthe current 3 month season. \nContinue?");
        }

        if (snowContinue && precipContinue) {
            boolean saved = changeSeason(buildClimoDates());

            if (saved) {
                changeListener.setChangesUnsaved(false);
                close();
            }
        }
    }

    /**
     * Build a ClimoDates object from user input.
     * 
     * @return ClimoDates
     */
    private ClimoDates buildClimoDates() {
        ClimoDates climoDates = ClimoDates.getDefaultClimoDates();

        // Alternate snow seasons
        if (snowAlternateRdo.getSelection()) {
            // Create a new list reflecting alternate seasons selected by the
            // user
            List<ClimateDate> snowSeason = new ArrayList<>();

            for (int ii = 0; ii < 4; ii++) {
                // The first alternate doesn't need a checkbox. For the rest,
                // break if it's unchecked since any after it should also be
                // unchecked.
                if (ii == 0 || snowAlternateChk[ii].getSelection()) {
                    int day = Integer.parseInt(snowDaySpn[ii].getText());
                    int month = snowMonthCbo[ii].getSelectionIndex() + 1;

                    ClimateDate date = ClimateDate.getMissingClimateDate();
                    date.setMon(month);
                    date.setDay(day);

                    snowSeason.add(date);
                } else {
                    break;
                }
            }

            climoDates.setSnowSeasons(snowSeason);
        }

        // Alternate precip seasons
        if (precipAlternateRdo.getSelection()) {
            List<ClimateDate> precipSeason = new ArrayList<>();

            for (int ii = 0; ii < 4; ii++) {
                if (ii == 0 || precipAlternateChk[ii].getSelection()) {
                    int day = Integer.parseInt(precipDaySpn[ii].getText());
                    int month = precipMonthCbo[ii].getSelectionIndex() + 1;

                    ClimateDate date = ClimateDate.getMissingClimateDate();
                    date.setMon(month);
                    date.setDay(day);

                    precipSeason.add(date);
                } else {
                    break;
                }
            }
            climoDates.setPrecipSeasons(precipSeason);
        }
        return climoDates;
    }

    /**
     * 
     * @param alternateChk
     * @param daySpn
     * @param monthCbo
     * @param defaultDay
     * @param defaultMon
     * @return true if one or more alternate 3 month seasons matches the current
     *         3 month season; false otherwise.
     */
    private boolean checkContinue(Button[] alternateChk, Spinner[] daySpn,
            Combo[] monthCbo, int defaultDay, int defaultMon) {
        for (int ii = 0; ii < alternateChk.length; ii++) {
            if (ii == 0 || alternateChk[ii].getSelection()) {
                int month = monthCbo[ii].getSelectionIndex() + 1;
                int day = daySpn[ii].getSelection();
                if (month == defaultMon && day == defaultDay) {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Change the seasons in localization
     *
     * @param climoDates
     */
    private boolean changeSeason(ClimoDates climoDates) {

        String message = "Preferences have been saved to Localization.";

        boolean isSaved = ClimoDatesManager.getInstance()
                .saveClimoDates(climoDates);
        if (!isSaved) {
            logger.error("Failed to save preference to localization.");
            // TODO: see if the error message from the manager can be displayed
            // here
            message = "Error: Failed to save preferences to localization.";
        }

        MessageDialog.openInformation(shell, "Season Preferences", message);

        return isSaved;
    }

    /**
     * Get annual preferences from localization and display them.
     */
    private void displayAnnualPeriods() {
        ClimoDates climoDates = ClimoDatesManager.getInstance().getClimoDates();

        changeListener.setIgnoreChanges(true);
        List<ClimateDate> snowSeason = climoDates.getSnowSeasons();
        List<ClimateDate> precipSeason = climoDates.getPrecipSeasons();

        // Show the alternates if theres is more than one OR there is one
        // with alternate dates.
        if ((snowSeason.get(0).getMon() != ClimoDates.SNOW_DEFAULT_MON
                || snowSeason.get(0).getDay() != ClimoDates.SNOW_DEFAULT_DAY)
                || snowSeason.size() > 1) {
            snowAlternateRdo.setSelection(true);
            snowCurrentRdo.setSelection(false);

            for (int ii = 0; ii < 4 && ii < snowSeason.size(); ii++) {
                ClimateDate snowSeasonBeg = snowSeason.get(ii);

                snowMonthCbo[ii].select(snowSeasonBeg.getMon() - 1);
                snowDaySpn[ii].setSelection(snowSeasonBeg.getDay());

                snowAlternateChk[ii].setSelection(true);
                snowAlternateChk[ii].setEnabled(true);
                enableTimes(true, snowTimeComp[ii]);
            }
        }
        enableTimes(snowAlternateRdo.getSelection(), snowTimeComp,
                snowAlternateChk);

        if ((precipSeason.get(0).getMon() != ClimoDates.PRECIP_DEFAULT_MON
                || precipSeason.get(0)
                        .getDay() != ClimoDates.PRECIP_DEFAULT_DAY)
                || precipSeason.size() > 1) {
            precipAlternateRdo.setSelection(true);
            precipCurrentRdo.setSelection(false);

            for (int ii = 0; ii < 4 && ii < precipSeason.size(); ii++) {
                ClimateDate precipSeasonBeg = precipSeason.get(ii);

                precipMonthCbo[ii].select(precipSeasonBeg.getMon() - 1);
                precipDaySpn[ii].setSelection(precipSeasonBeg.getDay());

                precipAlternateChk[ii].setSelection(true);
                precipAlternateChk[ii].setEnabled(true);
                enableTimes(true, precipTimeComp[ii]);
            }
        }
        enableTimes(precipAlternateRdo.getSelection(), precipTimeComp,
                precipAlternateChk);

        changeListener.setIgnoreChanges(false);
        changeListener.setChangesUnsaved(false);

    }
}

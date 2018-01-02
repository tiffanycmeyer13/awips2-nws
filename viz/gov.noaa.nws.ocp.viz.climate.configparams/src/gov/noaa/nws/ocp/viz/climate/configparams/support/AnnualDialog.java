/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.configparams.support;

import java.text.DateFormatSymbols;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimoDatesRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.configparams.SnowPrecipServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
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
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class AnnualDialog extends ClimateCaveChangeTrackDialog {
    /**
     * default month for snow
     */
    private final static int SNOW_DEFAULT_MON = 7;

    /**
     * default day for snow
     */
    private final static int SNOW_DEFAULT_DAY = 1;

    /**
     * default month for precipitation
     */
    private final static int PRECIP_DEFAULT_MON = 1;

    /**
     * default day for precipitation
     */
    private final static int PRECIP_DEFAULT_DAY = 1;

    /**
     * snow composite
     */
    protected Composite snowTimeComp;

    /**
     * Current 3 month season check button
     */
    protected Button snowCurrentChk;

    /**
     * Month combo in the snow composite
     */
    private Combo snowMonthCbo;

    /**
     * day spinner in snow composite
     */
    private Spinner snowDaySpn;

    /**
     * precipitation composite
     */
    protected Composite precipTimeComp;

    /**
     * current 3 month season in precipitation composite
     */
    protected Button precipCurrentChk;

    /**
     * Month combo in precipitation composite
     */
    private Combo precipMonthCbo;

    /**
     * day spinner for precipitation
     */
    private Spinner precipDaySpn;

    /**
     * bold font
     */
    protected Font boldFont;

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
        GridLayout grpGl = new GridLayout(1, false);
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

        snowCurrentChk = new Button(periodGrp, SWT.CHECK);
        snowCurrentChk.setText("Current 3 Month Season");
        snowCurrentChk.addListener(SWT.Selection, changeListener);

        snowTimeComp = new Composite(periodGrp, SWT.NONE);
        snowTimeComp.setLayout(new GridLayout(3, false));

        Label fake1Lbl = new Label(snowTimeComp, SWT.NONE);
        fake1Lbl.setText("");

        Label monthLbl = new Label(snowTimeComp, SWT.NONE);
        monthLbl.setText("Month");

        Label dayLbl = new Label(snowTimeComp, SWT.NONE);
        dayLbl.setText("Day");

        Label startLbl = new Label(snowTimeComp, SWT.NONE);
        startLbl.setText("Start:");

        snowMonthCbo = new Combo(snowTimeComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        // getShortMonths() return 13 items and the last one is empty
        // Use a for loop to avoid the last one
        for (int i = 0; i < 12; i++) {
            snowMonthCbo.add(months[i]);
        }
        snowMonthCbo.select(SNOW_DEFAULT_MON - 1);
        snowMonthCbo.addListener(SWT.Modify, changeListener);

        snowDaySpn = new Spinner(snowTimeComp, SWT.BORDER);
        snowDaySpn.setMinimum(1);
        snowDaySpn.setMaximum(31);
        snowDaySpn.addListener(SWT.Modify, changeListener);

        snowCurrentChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                enableTimes(snowCurrentChk, snowTimeComp);
            }
        });

        snowCurrentChk.setSelection(true);
        enableTimes(snowCurrentChk, snowTimeComp);
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

        precipCurrentChk = new Button(periodGrp, SWT.CHECK);
        precipCurrentChk.setText("Current 3 Month Season");
        precipCurrentChk.addListener(SWT.Selection, changeListener);

        precipTimeComp = new Composite(periodGrp, SWT.NONE);
        precipTimeComp.setLayout(new GridLayout(3, false));

        Label fake1Lbl = new Label(precipTimeComp, SWT.NONE);
        fake1Lbl.setText("");

        Label monthLbl = new Label(precipTimeComp, SWT.NONE);
        monthLbl.setText("Month");

        Label dayLbl = new Label(precipTimeComp, SWT.NONE);
        dayLbl.setText("Day");

        Label startLbl = new Label(precipTimeComp, SWT.NONE);
        startLbl.setText("Start:");

        precipMonthCbo = new Combo(precipTimeComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        for (int i = 0; i < 12; i++) { // avoid months[12], which is empty
            precipMonthCbo.add(months[i]);
        }
        precipMonthCbo.select(PRECIP_DEFAULT_MON - 1);
        precipMonthCbo.addListener(SWT.Modify, changeListener);

        precipDaySpn = new Spinner(precipTimeComp, SWT.BORDER);
        precipDaySpn.setMinimum(1);
        precipDaySpn.setMaximum(31);
        precipDaySpn.addListener(SWT.Modify, changeListener);

        precipCurrentChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                enableTimes(precipCurrentChk, precipTimeComp);
            }
        });

        precipCurrentChk.setSelection(true);
        enableTimes(precipCurrentChk, precipTimeComp);
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
     * enable controls in a composite
     * 
     * @param currentBtn
     * @param composite
     */
    private void enableTimes(Button currentBtn, Composite composite) {
        boolean enable = !(currentBtn.getSelection());
        for (Control control : composite.getChildren()) {
            control.setEnabled(enable);
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

        ClimateDates snowSeason = ClimateDates.getMissingClimateDates();
        ClimateDates snowYear = ClimateDates.getMissingClimateDates();
        ClimateDates precipSeason = ClimateDates.getMissingClimateDates();
        ClimateDates precipYear = ClimateDates.getMissingClimateDates();

        boolean snowContinue = true;
        boolean precipContinue = true;

        if (snowCurrentChk.getSelection()) {
            snowSeason.getStart().setMon(SNOW_DEFAULT_MON);
            snowSeason.getStart().setDay(SNOW_DEFAULT_DAY);
            snowSeason.getEnd().setDay(SNOW_DEFAULT_DAY - 1);
            if (snowSeason.getEnd().getDay() == 0) {
                snowSeason.getEnd().setMon(SNOW_DEFAULT_MON - 1);
                if (snowSeason.getEnd().getMon() == 0) {
                    snowSeason.getEnd().setMon(12);
                }
                snowSeason.getEnd().setDay(ClimateUtilities.daysInMonth(2015,
                        snowSeason.getEnd().getMon()));
            } else {
                snowSeason.getEnd().setMon(SNOW_DEFAULT_MON);
            }
        } else {
            int snowMonth = snowMonthCbo.getSelectionIndex() + 1;
            int snowDay = snowDaySpn.getSelection();

            if (snowMonth == SNOW_DEFAULT_MON && snowDay == SNOW_DEFAULT_DAY) {
                snowContinue = MessageDialog.openQuestion(shell, "Warning",
                        "The snow start date indicated will result in seasonal sums"
                                + " for\nthe current 3 month season. \nContinue?");
            }

            if (snowContinue) {
                setSeasonFromStart(snowSeason, snowMonth, snowDay);
            }
        }

        if (snowContinue) {
            if (precipCurrentChk.getSelection()) {
                precipSeason.getStart().setMon(PRECIP_DEFAULT_MON);
                precipSeason.getStart().setDay(PRECIP_DEFAULT_DAY);
                precipSeason.getEnd().setDay(PRECIP_DEFAULT_DAY - 1);
                precipSeason.getEnd().setMon(12);
            } else {
                int precipMonth = precipMonthCbo.getSelectionIndex() + 1;
                int precipDay = this.precipDaySpn.getSelection();

                if (precipMonth == PRECIP_DEFAULT_MON
                        && precipDay == PRECIP_DEFAULT_DAY) {
                    precipContinue = MessageDialog.openQuestion(shell,
                            "Warning",
                            "The precip start date indicated will result in seasonal sums "
                                    + "for\nthe current 3 month season. \nContinue?");
                }

                if (precipContinue) {
                    setSeasonFromStart(precipSeason, precipMonth, precipDay);
                }
            }
        }

        /* Annual Values Currently ALWAYS stay the same */
        snowYear.getStart().setMon(7);
        snowYear.getStart().setDay(1);
        snowYear.getEnd().setMon(6);
        snowYear.getEnd().setDay(30);
        precipYear.getStart().setMon(1);
        precipYear.getStart().setDay(1);
        precipYear.getEnd().setMon(12);
        precipYear.getEnd().setDay(31);

        if (snowContinue && precipContinue) {
            boolean saved = changeSeason(precipSeason, precipYear, snowSeason,
                    snowYear);

            if (saved) {
                changeListener.setChangesUnsaved(false);
                close();
            }
        }
    }

    /**
     * Set season start and end based on given start month and day. End will be
     * the day prior to the start (in the next year).
     * 
     * @param season
     * @param startMonth
     * @param startDay
     */
    private static void setSeasonFromStart(ClimateDates season, int startMonth,
            int startDay) {
        season.getStart().setMon(startMonth);
        season.getStart().setDay(startDay);
        season.getEnd().setDay(startDay - 1);
        if (season.getEnd().getDay() == 0) {
            season.getEnd().setMon(startMonth - 1);
            if (season.getEnd().getMon() == 0) {
                season.getEnd().setMon(12);
            }
            season.getEnd().setDay(ClimateUtilities.daysInMonth(2015,
                    season.getEnd().getMon()));
        } else {
            season.getEnd().setMon(startMonth);
        }
    }

    /**
     * Change the seasons in DB
     * 
     * @param precipSeason
     * @param precipYear
     * @param snowSeason
     * @param snowYear
     */
    private boolean changeSeason(ClimateDates precipSeason,
            ClimateDates precipYear, ClimateDates snowSeason,
            ClimateDates snowYear) {

        String message = "Preferences have saved to table climo_dates.";

        ClimoDatesRequest cdr = new ClimoDatesRequest(precipSeason, precipYear,
                snowSeason, snowYear);

        boolean isSaved = false;
        try {
            ThriftClient.sendRequest(cdr);
            isSaved = true;
        } catch (VizException e) {
            logger.error("Failed to save preference to table climo_dates.", e);
            message = "Error: Failed to save preference to table climo_dates.";
        }

        MessageDialog.openInformation(shell, "Season Preferences", message);

        return isSaved;
    }

    /**
     * Get annual preferences from DB and display them
     */
    private void displayAnnualPeriods() {
        SnowPrecipServiceRequest spsr = new SnowPrecipServiceRequest();

        try {
            @SuppressWarnings("unchecked")
            List<ClimateDate> dates = (List<ClimateDate>) ThriftClient
                    .sendRequest(spsr);
            if (dates == null || dates.size() != 2) {
                return;
            } else {
                changeListener.setIgnoreChanges(true);
                ClimateDate snowSeasonBeg = dates.get(0);
                ClimateDate precipSeasonBeg = dates.get(1);

                if (snowSeasonBeg.getMon() != SNOW_DEFAULT_MON
                        || snowSeasonBeg.getDay() != SNOW_DEFAULT_DAY) {
                    snowCurrentChk.setSelection(false);
                    snowMonthCbo.select(snowSeasonBeg.getMon() - 1);
                    snowDaySpn.setSelection(snowSeasonBeg.getDay());
                    enableTimes(snowCurrentChk, snowTimeComp);
                }

                if (precipSeasonBeg.getMon() != PRECIP_DEFAULT_MON
                        || precipSeasonBeg.getDay() != PRECIP_DEFAULT_DAY) {
                    precipCurrentChk.setSelection(false);
                    precipMonthCbo.select(precipSeasonBeg.getMon() - 1);
                    precipDaySpn.setSelection(precipSeasonBeg.getDay());
                    enableTimes(precipCurrentChk, precipTimeComp);
                }
                changeListener.setIgnoreChanges(false);
                changeListener.setChangesUnsaved(false);
            }
        } catch (VizException e) {
            logger.warn(
                    "Unable to get preferences from DB. Use default preferences.");
        }
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate.dialog;

import java.text.DateFormatSymbols;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;

/**
 * 
 * Dialog to View/Edit climate reporting period.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/21/2016   20640    jwu        Modified from AnnualDialog.java
 * 02/07/2017   20640    jwu        Make it singleton & linked with setup dialog.
 * 02/21/2017   20640    jwu        Set instance to null when shell is disposed.
 * 17 MAY 2017  33104    amoore     FindBugs. Package reorg.
 * 06 OCT 2017  38974    amoore     Reporting Periods dialog should not be singleton.
 * 12 OCT 2017  39354    amoore     Decoupling from Setup a bit.
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class ClimateReportPeriodsDialog extends ClimateCaveChangeTrackDialog {

    /**
     * Default start/end month/day for snow, HDD (Heating Degree Days), CDD
     * (Cooling Degree Days)
     */
    protected final static int SNOW_DEFAULT_START_MON = 7;

    protected final static int SNOW_DEFAULT_START_DAY = 1;

    protected final static int SNOW_DEFAULT_END_MON = 6;

    protected final static int SNOW_DEFAULT_END_DAY = 30;

    protected final static int HDD_DEFAULT_START_MON = 7;

    protected final static int HDD_DEFAULT_START_DAY = 1;

    protected final static int HDD_DEFAULT_END_MON = 6;

    protected final static int HDD_DEFAULT_END_DAY = 30;

    protected final static int CDD_DEFAULT_START_MON = 1;

    protected final static int CDD_DEFAULT_START_DAY = 1;

    protected final static int CDD_DEFAULT_END_MON = 12;

    protected final static int CDD_DEFAULT_END_DAY = 31;

    /**
     * ClimateDates to hold snow, heat cool start/end dates
     */
    private ClimateDates snowDates;

    private ClimateDates heatDates;

    private ClimateDates coolDates;

    /**
     * Widgets for snow start/end time
     */
    protected Combo snowStartMonthCombo;

    protected Spinner snowStartDaySpinner;

    protected Combo snowEndMonthCombo;

    protected Spinner snowEndDaySpinner;

    /**
     * Widgets for HDD start/end time
     */
    protected Combo hddStartMonthCombo;

    protected Spinner hddStartDaySpinner;

    protected Combo hddEndMonthCombo;

    protected Spinner hddEndDaySpinner;

    /**
     * Widgets for CDD start/end time
     */
    protected Combo cddStartMonthCombo;

    protected Spinner cddStartDaySpinner;

    protected Combo cddEndMonthCombo;

    protected Spinner cddEndDaySpinner;

    /**
     * Bold font
     */
    protected Font boldFont;

    /**
     * A GridData for spinners to use.
     */
    private GridData spinnerGD = new GridData(SWT.FILL, SWT.CENTER, false,
            false, 1, 1);

    /**
     * Constructor
     * 
     * @param parent
     * @param snowDates
     * @param heatDates
     * @param coolDates
     */
    protected ClimateReportPeriodsDialog(Shell parent, ClimateDates snowDates,
            ClimateDates heatDates, ClimateDates coolDates) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);
        setText("View/Edit Reporting Periods");

        // no changes yet
        setReturnValue(false);

        if (snowDates.isMissing()) {
            this.snowDates = new ClimateDates(
                    new ClimateDate(SNOW_DEFAULT_START_DAY,
                            SNOW_DEFAULT_START_MON),
                    new ClimateDate(SNOW_DEFAULT_END_DAY,
                            SNOW_DEFAULT_END_MON));

            // force change of missing value to something valid
            setReturnValue(true);
        } else {
            this.snowDates = new ClimateDates(snowDates);
        }

        if (heatDates.isMissing()) {
            this.heatDates = new ClimateDates(
                    new ClimateDate(HDD_DEFAULT_START_DAY,
                            HDD_DEFAULT_START_MON),
                    new ClimateDate(HDD_DEFAULT_END_DAY, HDD_DEFAULT_END_MON));

            // force change of missing value to something valid
            setReturnValue(true);
        } else {
            this.heatDates = new ClimateDates(heatDates);
        }

        if (coolDates.isMissing()) {
            this.coolDates = new ClimateDates(
                    new ClimateDate(CDD_DEFAULT_START_DAY,
                            CDD_DEFAULT_START_MON),
                    new ClimateDate(CDD_DEFAULT_END_DAY, CDD_DEFAULT_END_MON));

            // force change of missing value to something valid
            setReturnValue(true);
        } else {
            this.coolDates = new ClimateDates(coolDates);
        }
    }

    /**
     * Create dialog
     * 
     * @param parent
     */
    @Override
    protected void initializeComponents(Shell shell) {

        GridLayout shellGL = new GridLayout(1, false);
        shellGL.marginWidth = 10;
        shellGL.marginHeight = 10;
        shell.setLayout(shellGL);

        setupFonts();

        createPeriodGroup(shell);

        createActionButtons(shell);

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                closed();
            }
        });

    }

    /**
     * Closed dialog. Clean up.
     */
    protected void closed() {
        if (boldFont != null) {
            boldFont.dispose();
        }
    }

    /**
     * Method called after open() is called on shell - used here to update GUI
     * selections from snow/heat/cool dates.
     */
    @Override
    protected void opened() {
        // snow
        snowStartMonthCombo.select(snowDates.getStart().getMon() - 1);
        snowStartMonthCombo.addListener(SWT.Modify, changeListener);

        snowStartDaySpinner.setSelection(snowDates.getStart().getDay());
        snowStartDaySpinner.addListener(SWT.Modify, changeListener);

        snowEndMonthCombo.select(snowDates.getEnd().getMon() - 1);
        snowEndMonthCombo.addListener(SWT.Modify, changeListener);

        snowEndDaySpinner.setSelection(snowDates.getEnd().getDay());
        snowEndDaySpinner.addListener(SWT.Modify, changeListener);

        // heating degree days
        hddStartMonthCombo.select(heatDates.getStart().getMon() - 1);
        hddStartMonthCombo.addListener(SWT.Modify, changeListener);

        hddStartDaySpinner.setSelection(heatDates.getStart().getDay());
        hddStartDaySpinner.addListener(SWT.Modify, changeListener);

        hddEndMonthCombo.select(heatDates.getEnd().getMon() - 1);
        hddEndMonthCombo.addListener(SWT.Modify, changeListener);

        hddEndDaySpinner.setSelection(heatDates.getEnd().getDay());
        hddEndDaySpinner.addListener(SWT.Modify, changeListener);

        // cooling degree days
        cddStartMonthCombo.select(coolDates.getStart().getMon() - 1);
        cddStartMonthCombo.addListener(SWT.Modify, changeListener);

        cddStartDaySpinner.setSelection(coolDates.getStart().getDay());
        cddStartDaySpinner.addListener(SWT.Modify, changeListener);

        cddEndMonthCombo.select(coolDates.getEnd().getMon() - 1);
        cddEndMonthCombo.addListener(SWT.Modify, changeListener);

        cddEndDaySpinner.setSelection(coolDates.getEnd().getDay());
        cddEndDaySpinner.addListener(SWT.Modify, changeListener);
    }

    /**
     * Create fonts
     */
    private void setupFonts() {
        FontData fontData = shell.getFont().getFontData()[0];

        boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.BOLD));
    }

    /**
     * Create reporting period group
     * 
     * @param shell
     *            main Shell
     */
    private void createPeriodGroup(Shell shell) {
        Group reportPeriodGrp = new Group(shell, SWT.BORDER);
        GridLayout grpGl = new GridLayout(1, false);
        grpGl.verticalSpacing = 15;
        grpGl.marginWidth = 10;
        grpGl.marginHeight = 10;
        reportPeriodGrp.setLayout(grpGl);
        reportPeriodGrp.setText("Reporting Periods");

        createSnowGrp(reportPeriodGrp);
        createHDDGrp(reportPeriodGrp);
        createCDDGrp(reportPeriodGrp);
    }

    /**
     * Create snow period group
     * 
     * @param periodComp
     *            main composite
     */
    private void createSnowGrp(Composite periodComp) {
        Group periodGrp = new Group(periodComp, SWT.SHADOW_IN);
        periodGrp.setLayout(new GridLayout(1, false));
        periodGrp.setText("Snow");
        periodGrp.setFont(boldFont);

        Composite timeComp = new Composite(periodGrp, SWT.NONE);
        timeComp.setLayout(new GridLayout(3, false));

        // Create snow start month/day
        Label fake1Lbl = new Label(timeComp, SWT.NONE);
        fake1Lbl.setText("");

        Label monthLbl = new Label(timeComp, SWT.NONE);
        monthLbl.setText("Month");

        Label dayLbl = new Label(timeComp, SWT.NONE);
        dayLbl.setText("Day");

        Label startLbl = new Label(timeComp, SWT.NONE);
        startLbl.setText("Start:");

        snowStartMonthCombo = new Combo(timeComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        // getShortMonths() return 13 items and the last one is empty
        for (String month : months) {
            snowStartMonthCombo.add(month);
        }
        snowStartMonthCombo.remove(snowStartMonthCombo.getItemCount() - 1);

        snowStartMonthCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ee) {
                setDaysLimit(snowStartMonthCombo, snowStartDaySpinner);
            }
        });

        snowStartMonthCombo.select(snowDates.getStart().getMon());

        snowStartDaySpinner = new Spinner(timeComp, SWT.BORDER);
        snowStartDaySpinner.setMinimum(1);
        snowStartDaySpinner.setMaximum(31);
        snowStartDaySpinner.setLayoutData(spinnerGD);

        snowStartDaySpinner.setSelection(snowDates.getStart().getDay());

        // Create snow end month/day
        Label endLbl = new Label(timeComp, SWT.NONE);
        endLbl.setText("End:");

        snowEndMonthCombo = new Combo(timeComp, SWT.POP_UP);
        for (String month : months) {
            snowEndMonthCombo.add(month);
        }
        snowEndMonthCombo.remove(snowEndMonthCombo.getItemCount() - 1);

        snowEndMonthCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ee) {
                setDaysLimit(snowEndMonthCombo, snowEndDaySpinner);
            }
        });

        snowEndMonthCombo.select(snowDates.getEnd().getMon());

        snowEndDaySpinner = new Spinner(timeComp, SWT.BORDER);
        snowEndDaySpinner.setMinimum(1);
        snowEndDaySpinner.setMaximum(31);

        snowEndDaySpinner.setSelection(snowDates.getEnd().getDay());
    }

    /**
     * Create HDD (Heating Degree Days) month/day group
     * 
     * @param periodComp
     */
    private void createHDDGrp(Composite periodComp) {
        Group periodGrp = new Group(periodComp, SWT.SHADOW_IN);
        periodGrp.setLayout(new GridLayout(1, false));
        periodGrp.setText("HDD");
        periodGrp.setFont(boldFont);

        Composite timeComp = new Composite(periodGrp, SWT.NONE);
        timeComp.setLayout(new GridLayout(3, false));

        // Create HDD start month/day
        Label fake1Lbl = new Label(timeComp, SWT.NONE);
        fake1Lbl.setText("");

        Label monthLbl = new Label(timeComp, SWT.NONE);
        monthLbl.setText("Month");

        Label dayLbl = new Label(timeComp, SWT.NONE);
        dayLbl.setText("Day");

        Label startLbl = new Label(timeComp, SWT.NONE);
        startLbl.setText("Start:");

        hddStartMonthCombo = new Combo(timeComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        for (String month : months) {
            hddStartMonthCombo.add(month);
        }
        hddStartMonthCombo.remove(hddStartMonthCombo.getItemCount() - 1);

        hddStartMonthCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ee) {
                setDaysLimit(hddStartMonthCombo, hddStartDaySpinner);
            }
        });

        hddStartMonthCombo.select(heatDates.getStart().getMon());

        hddStartDaySpinner = new Spinner(timeComp, SWT.BORDER);
        hddStartDaySpinner.setMinimum(1);
        hddStartDaySpinner.setMaximum(31);
        hddStartDaySpinner.setLayoutData(spinnerGD);

        hddStartDaySpinner.setSelection(heatDates.getStart().getDay());

        // Create HDD end month/day
        Label endLbl = new Label(timeComp, SWT.NONE);
        endLbl.setText("End:");

        hddEndMonthCombo = new Combo(timeComp, SWT.POP_UP);
        for (String month : months) {
            hddEndMonthCombo.add(month);
        }
        hddEndMonthCombo.remove(hddEndMonthCombo.getItemCount() - 1);

        hddEndMonthCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ee) {
                setDaysLimit(hddEndMonthCombo, hddEndDaySpinner);
            }
        });

        hddEndMonthCombo.select(heatDates.getEnd().getMon());

        hddEndDaySpinner = new Spinner(timeComp, SWT.BORDER);
        hddEndDaySpinner.setMinimum(1);
        hddEndDaySpinner.setMaximum(31);

        hddEndDaySpinner.setSelection(heatDates.getEnd().getDay());
    }

    /**
     * Create CDD (Cooling Degree Days) month/day group
     * 
     * @param periodComp
     */
    private void createCDDGrp(Composite periodComp) {
        Group periodGrp = new Group(periodComp, SWT.SHADOW_IN);
        periodGrp.setLayout(new GridLayout(1, false));
        periodGrp.setText("CDD");
        periodGrp.setFont(boldFont);

        Composite timeComp = new Composite(periodGrp, SWT.NONE);
        timeComp.setLayout(new GridLayout(3, false));

        // Create CDD start month/day
        Label fake1Lbl = new Label(timeComp, SWT.NONE);
        fake1Lbl.setText("");

        Label monthLbl = new Label(timeComp, SWT.NONE);
        monthLbl.setText("Month");

        Label dayLbl = new Label(timeComp, SWT.NONE);
        dayLbl.setText("Day");

        Label startLbl = new Label(timeComp, SWT.NONE);
        startLbl.setText("Start:");

        cddStartMonthCombo = new Combo(timeComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        // getShortMonths() return 13 items and the last one is empty
        for (String month : months) {
            cddStartMonthCombo.add(month);
        }
        cddStartMonthCombo.remove(cddStartMonthCombo.getItemCount() - 1);

        cddStartMonthCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ee) {
                setDaysLimit(cddStartMonthCombo, cddStartDaySpinner);
            }
        });

        cddStartMonthCombo.select(coolDates.getStart().getMon());

        cddStartDaySpinner = new Spinner(timeComp, SWT.BORDER);
        cddStartDaySpinner.setMinimum(1);
        cddStartDaySpinner.setMaximum(31);
        cddStartDaySpinner.setLayoutData(spinnerGD);

        cddStartDaySpinner.setSelection(coolDates.getStart().getDay());

        // Create CDD end month/day
        Label endLbl = new Label(timeComp, SWT.NONE);
        endLbl.setText("End:");

        cddEndMonthCombo = new Combo(timeComp, SWT.POP_UP);
        for (String month : months) {
            cddEndMonthCombo.add(month);
        }
        cddEndMonthCombo.remove(cddEndMonthCombo.getItemCount() - 1);

        cddStartMonthCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent ee) {
                setDaysLimit(cddEndMonthCombo, cddEndDaySpinner);
            }
        });

        cddEndMonthCombo.select(coolDates.getEnd().getMon());

        cddEndDaySpinner = new Spinner(timeComp, SWT.BORDER);
        cddEndDaySpinner.setMinimum(1);
        cddEndDaySpinner.setMaximum(31);

        cddEndDaySpinner.setSelection(coolDates.getEnd().getDay());
    }

    /**
     * Create "Ok/Cancel" button at bottom
     * 
     * @param shell
     */
    private void createActionButtons(final Shell shell) {
        Composite buttonComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.horizontalSpacing = 10;
        gl.marginWidth = 20;
        buttonComp.setLayout(gl);

        Button okBtn = new Button(buttonComp, SWT.NONE);
        okBtn.setText("Ok");
        okBtn.setFont(boldFont);
        GridData okBtnGD = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,
                1);
        okBtn.setLayoutData(okBtnGD);

        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                buildDates();
                changeListener.setChangesUnsaved(false);
                // assume changes to pick up
                setReturnValue(true);
                close();
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.NONE);
        cancelBtn.setText("Cancel");
        cancelBtn.setFont(boldFont);
        cancelBtn.setLayoutData(okBtnGD);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * Build new snow/heat/cool dates from user selections on dialog.
     */
    protected void buildDates() {
        snowDates = new ClimateDates(
                new ClimateDate(snowStartDaySpinner.getSelection(),
                        snowStartMonthCombo.getSelectionIndex() + 1, 0),
                new ClimateDate(snowEndDaySpinner.getSelection(),
                        snowEndMonthCombo.getSelectionIndex() + 1, 0));
        heatDates = new ClimateDates(
                new ClimateDate(hddStartDaySpinner.getSelection(),
                        hddStartMonthCombo.getSelectionIndex() + 1, 0),
                new ClimateDate(hddEndDaySpinner.getSelection(),
                        hddEndMonthCombo.getSelectionIndex() + 1, 0));
        coolDates = new ClimateDates(
                new ClimateDate(cddStartDaySpinner.getSelection(),
                        cddStartMonthCombo.getSelectionIndex() + 1, 0),
                new ClimateDate(cddEndDaySpinner.getSelection(),
                        cddEndMonthCombo.getSelectionIndex() + 1, 0));
    }

    /**
     * Set the maximum number of days on a spinner based on the selection of
     * month.
     * 
     * @param monthCombo
     *            combo to list month choice
     * @param day
     *            spinner to select a day in a month
     * 
     */
    protected void setDaysLimit(Combo monthCmb, Spinner daySpinner) {
        int maxDays = ClimateUtilities.daysInMonth(
                ClimateDate.getLocalDate().getYear(),
                monthCmb.getSelectionIndex() + 1);
        daySpinner.setMaximum(maxDays);
    }

    /**
     * @return copy of snow dates
     */
    protected ClimateDates getSnowDates() {
        return new ClimateDates(snowDates);
    }

    /**
     * @return copy of heat dates
     */
    protected ClimateDates getHeatDates() {
        return new ClimateDates(heatDates);
    }

    /**
     * @return copy of cool dates
     */
    protected ClimateDates getCoolDates() {
        return new ClimateDates(coolDates);
    }
}
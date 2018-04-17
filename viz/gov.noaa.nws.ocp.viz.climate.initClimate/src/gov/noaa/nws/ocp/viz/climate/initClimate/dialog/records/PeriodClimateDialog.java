/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records;

import java.text.DateFormatSymbols;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.SeasonType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchFreezeDatesRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.DeleteClimateMonthRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.FetchClimateMonthRecordRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.GetAvailableMonthOfYearRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.SaveClimateMonthRecordRequest;
import gov.noaa.nws.ocp.viz.climate.initClimate.ClimatologyInputType;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;

/**
 * This class display the "MONTHLY NORMALS AND EXTREMES" dialog for init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#   Engineer   Description
 * ----------- --------- ---------- --------------------------
 * 03/25/2016  18469     wkwock     Initial creation
 * 10/27/2016  20635     wkwock     Clean up
 * 20 DEC 2016 26404     amoore     Correcting yes-no, ok-cancel ordering in message boxes.
 * 21 DEC 2016 20955     amoore     Fix logger class input.
 * 12 JAN 2017 26411     wkwock     Merge Modify and Add button to Save button
 * 27 FEB 2017 29617     wkwock     Remove first and last buttons from annual dialog.
 * 27 FEB 2017 27420     amoore     Fix titles.
 * 03 MAY 2017 33104     amoore     Address FindBugs.
 * 15 MAY 2017 33104     amoore     FindBugs and logic issues. Class rename.
 * 19 SEP 2017 38124     amoore     Use GC for text control sizes.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public class PeriodClimateDialog extends ClimateCaveChangeTrackDialog {
    /** climatology input type */
    private ClimatologyInputType climatologyInputType;

    /** period label */
    private Label periodLbl;

    /** station location */
    protected Text stationValueLbl;

    /** the period(month,season,annual) drop down menu */
    protected Combo periodCbo;

    /** up button */
    private Button upBtn;

    /** down button */
    private Button downBtn;

    /** temperature composite */
    private TemperatureComp tmpComp;

    /** precipitation composite */
    private PrecipitationComp precipComp;

    /** snow fall composite */
    private SnowfallComp snowComp;

    /** degree days composite */
    private DegreeDaysComp degreeComp;

    /** first button */
    private Button firstBtn;

    /** save record button */
    private Button saveRecordBtn;

    /** delete button */
    private Button deleteBtn;

    /** last button */
    private Button lastBtn;

    /** last period selection */
    protected int lastPeriodSelection;

    /**
     * Constructor.
     * 
     * @param display
     */
    public PeriodClimateDialog(Display display) {
        super(display);
        String hostname = System.getenv("HOSTNAME");
        setText("Monthly Normals and Extremes (on " + hostname + ")");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        mainLayout.verticalSpacing = 5;
        shell.setLayout(mainLayout);

        createStationControls();
        createDateControls();

        // separate line
        Label separateLbl = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        TabFolder folder = new TabFolder(shell, SWT.NONE);

        TabItem tempTab = new TabItem(folder, SWT.NONE);
        tempTab.setText("Temperature");
        tmpComp = new TemperatureComp(folder, changeListener);
        tempTab.setControl(tmpComp);

        TabItem precipTab = new TabItem(folder, SWT.NONE);
        precipTab.setText("Precipitation");
        precipComp = new PrecipitationComp(folder, changeListener);
        precipTab.setControl(precipComp);

        TabItem snowTab = new TabItem(folder, SWT.NONE);
        snowTab.setText("Snow");
        snowComp = new SnowfallComp(folder, changeListener);
        snowTab.setControl(snowComp);

        TabItem degreeTab = new TabItem(folder, SWT.NONE);
        degreeTab.setText("Degree Days");
        degreeComp = new DegreeDaysComp(folder, changeListener);
        degreeTab.setControl(degreeComp);
    }

    /**
     * Always return true, as this dialog is managed by another.
     */
    public boolean shouldClose() {
        return true;
    }

    /**
     * create station controls
     */
    private void createStationControls() {
        Composite stationComp = new Composite(shell, SWT.NONE);
        stationComp.setLayout(new RowLayout());

        // Station label
        Label stationLbl = new Label(stationComp, SWT.CENTER);
        stationLbl.setText("Station");

        // Station label
        stationValueLbl = new Text(stationComp, SWT.BORDER);

        GC gc = new GC(stationValueLbl);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        stationValueLbl.setEditable(false);
        stationValueLbl.setLayoutData(new RowData(43 * fontWidth, SWT.DEFAULT));
    }

    /**
     * create date controls
     */
    private void createDateControls() {
        Composite buttonsComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(11, false);
        buttonsComp.setLayout(gl);

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        buttonsComp.setLayoutData(gd);

        // Month label
        periodLbl = new Label(buttonsComp, SWT.CENTER);
        periodLbl.setText("Season");

        // Period drop menu
        periodCbo = new Combo(buttonsComp, SWT.POP_UP);
        String[] months = DateFormatSymbols.getInstance().getShortMonths();
        for (int i = 0; i < 12; i++) { // avoid months[12], which is empty
            periodCbo.add(months[i]);
        }
        periodCbo.select(0);
        lastPeriodSelection = 0;
        periodCbo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (periodCbo.getSelectionIndex() != lastPeriodSelection) {
                    boolean proceed = true;
                    if (changeListener.isChangesUnsaved()) {
                        boolean ok = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to different record?"
                                        + " Unsaved changes will be lost.");
                        if (!ok) {
                            periodCbo.select(lastPeriodSelection);
                            proceed = false;
                        }
                    }
                    if (proceed) {
                        displayPeriodClimatologyInfo();
                    }
                }
            }
        });

        // up button
        Composite spinnerComp = new Composite(buttonsComp, SWT.NONE);
        spinnerComp.setLayout(new GridLayout(1, false));
        upBtn = new Button(spinnerComp, SWT.ARROW | SWT.UP);
        upBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (periodCbo.getSelectionIndex() > 0) {
                    boolean proceed = true;
                    if (changeListener.isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to record " + periodCbo.getItem(
                                        periodCbo.getSelectionIndex() - 1) + "?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        periodCbo.select(periodCbo.getSelectionIndex() - 1);
                        displayPeriodClimatologyInfo();
                    }
                }
            }
        });

        // down button
        downBtn = new Button(spinnerComp, SWT.ARROW | SWT.DOWN);
        downBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (periodCbo
                        .getSelectionIndex() < (periodCbo.getItemCount() - 1)) {
                    boolean proceed = true;
                    if (changeListener.isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Different Record",
                                "Change to record " + periodCbo.getItem(
                                        periodCbo.getSelectionIndex() + 1) + "?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        periodCbo.select(periodCbo.getSelectionIndex() + 1);
                        displayPeriodClimatologyInfo();
                    }
                }
            }
        });

        // First button
        firstBtn = new Button(buttonsComp, SWT.PUSH);
        firstBtn.setText("First");
        gd = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
        gd.horizontalIndent = 100;
        firstBtn.setLayoutData(gd);
        firstBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int stationId = getStationId(stationValueLbl.getText());

                int newMonthOfYear = 1;
                GetAvailableMonthOfYearRequest request = new GetAvailableMonthOfYearRequest(
                        true, stationId, getPeriodType());
                try {
                    newMonthOfYear = (short) ThriftClient.sendRequest(request);
                } catch (VizException e) {
                    logger.error("Failed to get month of yer from DB.", e);
                }

                if (getMonthOfyear() != newMonthOfYear) {
                    boolean proceed = true;
                    if (changeListener.isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to First Record",
                                "Change to first record?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        setAvailableClimatologyInfo(true);
                    }
                }
            }
        });

        // Modify Record button
        saveRecordBtn = new Button(buttonsComp, SWT.PUSH);
        saveRecordBtn.setText("Save Record");
        saveRecordBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveRecord();
            }
        });

        // Delete Record button
        deleteBtn = new Button(buttonsComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                deleteRecord();
            }
        });

        // Last button
        lastBtn = new Button(buttonsComp, SWT.PUSH);
        lastBtn.setText("Last");
        lastBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int stationId = getStationId(stationValueLbl.getText());

                int newMonthOfYear = 12;
                GetAvailableMonthOfYearRequest request = new GetAvailableMonthOfYearRequest(
                        false, stationId, getPeriodType());
                try {
                    newMonthOfYear = (short) ThriftClient.sendRequest(request);
                } catch (VizException e) {
                    logger.error("Failed to get month of year from DB.", e);
                }

                if (getMonthOfyear() != newMonthOfYear) {
                    boolean proceed = true;
                    if (changeListener.isChangesUnsaved()) {
                        proceed = MessageDialog.openConfirm(shell,
                                "Change to Last Record",
                                "Change to last record?"
                                        + " Unsaved changes will be lost.");
                    }
                    if (proceed) {
                        setAvailableClimatologyInfo(false);
                    }
                }
            }
        });

    }

    /**
     * set the station location
     * 
     * @param stationLocation
     */
    public void setStationLoc(String stationLocation) {
        stationValueLbl.setText(stationLocation);
    }

    /**
     * set climatology input type
     * 
     * @param inputType
     */
    public void setClimatologyInputType(ClimatologyInputType inputType) {
        if (inputType != this.climatologyInputType) {
            this.climatologyInputType = inputType;
            String hostname = System.getenv("HOSTNAME");
            setText(climatologyInputType.toString()
                    + " Normals and Extremes (on " + hostname + ")");
            updatePeriodComb();
            setAvailableClimatologyInfo(true);
            if (climatologyInputType.equals(ClimatologyInputType.ANNUAL)) {
                firstBtn.setVisible(false);
                lastBtn.setVisible(false);
                periodLbl.setVisible(false);
                periodCbo.setVisible(false);
                upBtn.setVisible(false);
                downBtn.setVisible(false);
            } else {
                firstBtn.setVisible(true);
                lastBtn.setVisible(true);
                periodLbl.setVisible(true);
                periodCbo.setVisible(true);
                if (climatologyInputType.equals(ClimatologyInputType.MONTHLY)) {
                    periodLbl.setText("Month");
                } else {
                    periodLbl.setText("Season");
                }
                upBtn.setVisible(true);
                downBtn.setVisible(true);
            }
        }
        bringToTop();
    }

    /**
     * update period combo
     */
    private void updatePeriodComb() {
        periodCbo.removeAll();
        if (climatologyInputType.equals(ClimatologyInputType.MONTHLY)) {
            String[] months = DateFormatSymbols.getInstance().getShortMonths();
            for (int i = 0; i < 12; i++) { // avoid months[12], which is empty
                periodCbo.add(months[i]);
            }
        } else if (climatologyInputType.equals(ClimatologyInputType.SEASONAL)) {
            for (SeasonType st : SeasonType.values()) {
                periodCbo.add(st.toString());
            }
        }
        periodCbo.select(0);
    }

    /**
     * get period type
     * 
     * @return
     */
    protected PeriodType getPeriodType() {
        PeriodType periodType;
        switch (climatologyInputType) {
        case MONTHLY:
            periodType = PeriodType.MONTHLY_RAD;
            break;
        case SEASONAL:
            periodType = PeriodType.SEASONAL_RAD;
            break;
        case ANNUAL:
            periodType = PeriodType.ANNUAL_RAD;
            break;
        default:
            periodType = PeriodType.OTHER;
            break;
        }
        return periodType;
    }

    /**
     * get month of year
     * 
     * @return
     */
    protected int getMonthOfyear() {
        int monthOfYear = 12;
        switch (climatologyInputType) {
        case MONTHLY:
            monthOfYear = periodCbo.getSelectionIndex() + 1;
            break;
        case SEASONAL: // possible month of year for seasonal: 2,5,8,11
            monthOfYear = periodCbo.getSelectionIndex() * 3 + 2;
            break;
        case ANNUAL:
            monthOfYear = 12;
            break;
        default:
            break;
        }

        return monthOfYear;
    }

    /**
     * display period climatology informations
     */
    public void displayPeriodClimatologyInfo() {
        int stationId = getStationId(stationValueLbl.getText());
        PeriodClimo monthlyRcd = null;
        FetchClimateMonthRecordRequest fetchRequest = new FetchClimateMonthRecordRequest(
                stationId, getMonthOfyear(), getPeriodType());
        try {
            monthlyRcd = (PeriodClimo) ThriftClient.sendRequest(fetchRequest);
        } catch (VizException e) {
            logger.error("Failed to get record for station=" + stationId
                    + " month=" + getMonthOfyear() + " period type="
                    + getPeriodType(), e);
        }

        if (monthlyRcd == null) {
            monthlyRcd = PeriodClimo.getMissingPeriodClimo();
        }

        FetchFreezeDatesRequest request = new FetchFreezeDatesRequest(2,
                stationId);
        try {
            PeriodClimo freezeDatesRcd = (PeriodClimo) ThriftClient
                    .sendRequest(request);
            monthlyRcd.setEarlyFreezeNorm(freezeDatesRcd.getEarlyFreezeNorm());
            monthlyRcd.setLateFreezeNorm(freezeDatesRcd.getLateFreezeNorm());
            monthlyRcd.setEarlyFreezeRec(freezeDatesRcd.getEarlyFreezeRec());
            monthlyRcd.setLateFreezeRec(freezeDatesRcd.getLateFreezeRec());
        } catch (VizException e) {
            logger.error("Failed to get freeze dates with station ID="
                    + stationId + " module=2", e);
        }

        tmpComp.displayTemperatureInfo(monthlyRcd);
        precipComp.displayPrecipitationInfo(monthlyRcd);
        snowComp.displayMonthlySnowFallInfo(monthlyRcd);
        degreeComp.displayDegreeInfo(monthlyRcd);

        deleteBtn.setEnabled(isRecordAvailable(stationId, getMonthOfyear(),
                getPeriodType()));

        lastPeriodSelection = periodCbo.getSelectionIndex();

        degreeComp.setDateSelectionValidMonths();
        snowComp.setDateSelectionValidMonths();
        tmpComp.setDateSelectionValidMonths();
        precipComp.setDateSelectionValidMonths();
    }

    /**
     * Display the next available (by time) of selected-station info from table
     * period_climate_norm
     */
    private void setAvailableClimatologyInfo(boolean firstOne) {
        int stationId = getStationId(stationValueLbl.getText());
        int monthOfYear = 1;
        GetAvailableMonthOfYearRequest request = new GetAvailableMonthOfYearRequest(
                firstOne, stationId, getPeriodType());
        try {
            monthOfYear = (short) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to get month of year from DB.", e);
        }

        int index = 0;
        if (getPeriodType() == PeriodType.MONTHLY_RAD) {
            index = monthOfYear - 1;

        } else if (getPeriodType() == PeriodType.SEASONAL_RAD) {
            index = (monthOfYear + 1) / 3 - 1;
        } else { // type 9 annual
            index = 0;
        }
        periodCbo.select(index);

        displayPeriodClimatologyInfo();

        deleteBtn.setEnabled(
                isRecordAvailable(stationId, monthOfYear, getPeriodType()));
    }

    /**
     * is a row with statioId and month available in DB?
     * 
     * @param stationId
     * @param month
     * @param periodType
     * @return
     */
    private boolean isRecordAvailable(int stationId, int month,
            PeriodType periodType) {
        PeriodClimo climateRcd = null;

        FetchClimateMonthRecordRequest request = new FetchClimateMonthRecordRequest(
                stationId, month, periodType);
        try {
            climateRcd = (PeriodClimo) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to get monthly record.", e);
        }

        return (climateRcd != null);
    }

    /**
     * Save the record from GUI to DB table mon_climate_norm
     */
    protected void saveRecord() {
        tmpComp.getTemperatureInfo();
        precipComp.getPrecipitationInfo();
        snowComp.getSnowFallInfo();
        PeriodClimo record = degreeComp.getDegreeInfo();

        record.setMonthOfYear(getMonthOfyear());
        record.setPeriodType(getPeriodType());
        record.setInformId(getStationId(stationValueLbl.getText()));
        record.setMonthOfYear(getMonthOfyear());
        record.setPeriodType(getPeriodType());

        boolean recordSaved = false;
        SaveClimateMonthRecordRequest request = new SaveClimateMonthRecordRequest(
                record);
        try {
            recordSaved = (boolean) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to save record with station ID="
                    + record.getInformId() + " and mon_of_year="
                    + record.getMonthOfYear(), e);
        }

        if (!recordSaved) {
            MessageDialog.openError(shell, "Save unsuccessful",
                    "Failed to save to database");
        } else {
            MessageDialog.openInformation(shell, "Save successful",
                    "Data has successfully saved to database.");
            changeListener.setChangesUnsaved(false);
            deleteBtn.setEnabled(true);
        }
    }

    /**
     * Delete the record from GUI to DB table mon_climate_norm
     */
    protected void deleteRecord() {
        boolean delete = MessageDialog.openQuestion(shell,
                "Delete this record?",
                "Warning! You are about to delete this record! Proceed?");
        if (!delete) {
            return;
        }

        tmpComp.getTemperatureInfo();
        precipComp.getPrecipitationInfo();
        snowComp.getSnowFallInfo();
        PeriodClimo record = degreeComp.getDegreeInfo();

        boolean isDeleted = false;
        DeleteClimateMonthRecordRequest request = new DeleteClimateMonthRecordRequest(
                record.getInformId(), getMonthOfyear(), getPeriodType());
        try {
            isDeleted = (boolean) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to delete record with station ID="
                    + record.getInformId(), e);
        }

        if (!isDeleted) {
            MessageDialog.openError(shell, "Deletion unsuccessful",
                    "Failed to delete record with station ID="
                            + record.getInformId());
        } else {
            MessageDialog.openInformation(shell, "Deletion successful",
                    "Record with station ID=" + record.getInformId()
                            + " deleted.");
        }

        displayPeriodClimatologyInfo();
    }

    /**
     * get station ID base in station name
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected int getStationId(String stationName) {
        int stationId = ParameterFormatClimate.MISSING * -1;
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        try {
            List<Station> stations = (List<Station>) ThriftClient
                    .sendRequest(request);
            for (Station station : stations) {
                if (stationName.equals(station.getStationName())) {
                    stationId = station.getInformId();
                    break;
                }
            }
        } catch (VizException e) {
            logger.error(
                    "Failed to retrieve stations ID for station " + stationName,
                    e);
        }

        if (stationId == ParameterFormatClimate.MISSING * -1) {
            logger.warn(
                    "Failed to find stations ID for station " + stationName);
        }

        return stationId;
    }

    /**
     * For use by controlling dialog.
     * 
     * @return true if this dialog has some unsaved changes
     */
    public boolean isChangesUnsaved() {
        return changeListener.isChangesUnsaved();
    }
}

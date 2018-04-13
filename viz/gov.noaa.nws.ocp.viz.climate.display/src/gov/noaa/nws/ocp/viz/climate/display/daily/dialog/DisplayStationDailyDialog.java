/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.daily.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdGenerateSessionDataForView;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CancelClimateProdGenerateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CompleteDisplayClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.DisplayClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.GetClimateProdGenerateSessionRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.DisplayClimateResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues.QCValueType;
import gov.noaa.nws.ocp.viz.climate.display.common.DisplayValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.comp.QCTextComp;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.QCToolTip;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.TimeSelectorFocusListener;

/**
 * This class displays the Daily Station Climate values.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 17 MAY 2016  18384      amoore      Add data connection
 * 18 MAY 2016  18384      amoore      Relative humidity is hour-only for time.
 * 07 JUL 2016  16962      amoore      Added data display.
 * 13 JUL 2016  20414      amoore      Completed, needs integration with other Climate modules.
 * 28 JUL 2016  20652      amoore      Common SWT style for Climate dialogs.
 * 04 AUG 2016  20414      amoore      Address CP1.01-beta GUI comments.
 * 07 SEP 2016  20414      amoore      Warning on clearing values.
 * 08 SEP 2016  20414      amoore      Smarter warning on potential loss of changes. Add reference
 *                                     to help Handbook.
 * 01 DEC 2016  20414      amoore      Cleanup + comments.
 * 06 DEC 2016  20414      amoore      Import data from Climate Creator.
 * 09 DEC 2016  27015      amoore      Basic database output flow.
 * 12 DEC 2016  27015      amoore      Display module final steps in workflow.
 * 27 DEC 2016  22450      amoore      Wind direction precision.
 * 28 DEC 2016  22784      amoore      Sky cover precision. Legacy labeling was misleading.
 * 03 JAN 2017  22134      amoore      QC Text Field hovering.
 * 04 JAN 2017  22134      amoore      QC wind speed tooltips.
 * 05 JAN 2017  22134      amoore      Missed Snow on Ground QC Text Field.
 * 21 MAR 2017  30166      amoore      Integration with CPG workflow.
 * 04 APR 2017  30166      amoore      Implement abort. Adjust data types used. Integration with workflow.
 * 07 APR 2017  30166      amoore      Add auto-CPG constructor.
 * 07 APR 2017  30166      amoore      Update CPG status on display.
 * 10 MAY 2017  33104      amoore      Use new response data.
 * 12 MAY 2017  33104      amoore      Address minor FindBugs.
 * 02 JUN 2017  34777      amoore      Read-only mode if session is past DISPLAY state.
 * 15 JUN 2017  35187      amoore      Handle trace symbol in text box.
 * 01 AUG 2017  36639      amoore      Average Wind Speed QC value was not showing. Most robust logic for
 *                                     wind QC tooltips.
 * 04 AUG 2017  36707      amoore      Add warning on closing with X.
 * 19 SEP 2017  38124      amoore      Use GC for text control sizes.
 * </pre>
 * 
 * @author amoore
 */
public class DisplayStationDailyDialog extends ClimateCaveChangeTrackDialog {
    /**
     * Prefix for average wind speed label.
     */
    private static final String AVERAGE_WIND_SPEED = "Average Wind Speed";

    /**
     * Prefix for max wind gust speed label.
     */
    private static final String MAXIMUM_WIND_GUST_SPEED = "Maximum Wind Gust Speed";

    /**
     * Prefix for max wind speed label.
     */
    private static final String MAXIMUM_WIND_SPEED_STRING = "Maximum Wind Speed";

    /**
     * Summary type field.
     */
    private Text mySummaryTypeTF;

    /**
     * Date field.
     */
    private Text myDateTF;

    /**
     * Station list.
     */
    protected List myStationList;

    /**
     * Max temperature text field.
     */
    private QCTextComp myMaxTempTF;

    /**
     * Max temperature hour text field.
     */
    private Text myMaxTempTimeTF;

    /**
     * Min temperature text field.
     */
    private QCTextComp myMinTempTF;

    /**
     * Min temperature time text field.
     */
    private Text myMinTempTimeTF;

    /**
     * Max humidity text field.
     */
    private Text myMaxRelHumTF;

    /**
     * Max humidity time text field.
     */
    private Text myMaxRelHumHourTF;

    /**
     * Min humidity text field.
     */
    private Text myMinRelHumTF;

    /**
     * Min humidity time text field.
     */
    private Text myMinRelHumHourTF;

    /**
     * Max wind direction text field.
     */
    private QCTextComp myMaxWindDirTF;

    /**
     * Max wind time text field.
     */
    private Text myMaxWindTimeTF;

    /**
     * Max wind speed text field.
     */
    protected QCTextComp myMaxWindSpeedTF;

    /**
     * Max wind gust direction text field.
     */
    private QCTextComp myMaxGustDirTF;

    /**
     * Max wind gust time text field.
     */
    private Text myMaxGustTimeTF;

    /**
     * Max wind gust text field.
     */
    protected QCTextComp myMaxGustSpeedTF;

    /**
     * Average wind speed text field.
     */
    protected QCTextComp myAvgWindSpeedTF;

    /**
     * Precipitation text field.
     */
    private QCTextComp myPrecipTF;

    /**
     * Snowfall text field.
     */
    private QCTextComp mySnowDayFallTF;

    /**
     * Minutes of sun text field.
     */
    private Text myMinutesSunTF;

    /**
     * Percent of sun text field.
     */
    private QCTextComp myPercentPossSunTF;

    /**
     * Average sky cover text field.
     */
    private QCTextComp mySkyCoverTF;

    /**
     * Snow on ground text field.
     */
    private QCTextComp mySnowOnGroundTF;

    /**
     * Max SLP text field.
     */
    private Text myMaxSLPTF;

    /**
     * Min SLP text field.
     */
    private Text myMinSLPTF;

    /**
     * QC tool tip for weather.
     */
    private QCToolTip myWeatherQCTip;

    /**
     * TS checkbox.
     */
    private Button myTSCheckbox;

    /**
     * -FZRA checkbox.
     */
    private Button myNegFZRACheckbox;

    /**
     * FG checkbox.
     */
    private Button myFGCheckbox;

    /**
     * Mixed checkbox.
     */
    private Button myMixedCheckbox;

    /**
     * GR checkbox.
     */
    private Button myGRCheckbox;

    /**
     * FG <= 1/4SM checkbox.
     */
    private Button myFGFourthSMCheckbox;

    /**
     * +RA checkbox.
     */
    private Button myPosRACheckbox;

    /**
     * +SN checkbox.
     */
    private Button myPosSNCheckbox;

    /**
     * HZ checkbox.
     */
    private Button myHZCheckbox;

    /**
     * RA checkbox.
     */
    private Button myRACheckbox;

    /**
     * SN checkbox.
     */
    private Button mySNCheckbox;

    /**
     * BLSN checkbox.
     */
    private Button myBLSNCheckbox;

    /**
     * -RA checkbox.
     */
    private Button myNegRACheckbox;

    /**
     * -SN checkbox.
     */
    private Button myNegSNCheckbox;

    /**
     * SS checkbox.
     */
    private Button mySSCheckbox;

    /**
     * FZRA checkbox.
     */
    private Button myFZRACheckbox;

    /**
     * PL checkbox.
     */
    private Button myPLCheckbox;

    /**
     * FC checkbox.
     */
    private Button myFCCheckbox;

    /**
     * Data map retrieved from backend.
     */
    protected HashMap<Integer, ClimateDailyReportData> myDataMap = new HashMap<>();

    /**
     * Stations retrieved from backend.
     */
    private java.util.List<Station> myStations = new ArrayList<>();

    /**
     * Daily display date.
     */
    protected ClimateDate myDate;

    /**
     * Daily display type.
     */
    private DisplayDailySummaryType myDailyDisplaySummaryType;

    /**
     * Period type.
     */
    protected PeriodType myPeriodType;

    /**
     * Boolean to show wind in knots or mph.
     */
    protected boolean myIsWindSpeedsKnots;

    /**
     * Currently selected Daily Climate Data.
     */
    protected DailyClimateData myData;

    /**
     * Save menu item.
     */
    private MenuItem myAcceptValuesMenuItem;

    /**
     * Save button.
     */
    private Button myAcceptValuesButton;

    /**
     * Clear values button.
     */
    private Button myClearValuesButton;

    /**
     * Max wind speed label.
     */
    protected Label myMaxWindSpeedLbl;

    /**
     * Max wind gust label.
     */
    protected Label myMaxWindGustLbl;

    /**
     * Average wind speed label.
     */
    protected Label myAvgWindSpeedLbl;

    /**
     * The previously-selected station index.
     */
    private int myPrevStationSelectionIndex = -1;

    /**
     * Collection of listeners.
     */
    protected final ClimateTextListeners myDisplayListeners = new ClimateTextListeners();

    /**
     * Session ID.
     */
    protected final String mySessionID;

    /**
     * Writeable flag.
     */
    private final boolean myWriteable;

    /**
     * Completed flag. True if user chose confirmed Abort or "Accept and
     * Continue"
     */
    private boolean completed = false;

    /**
     * Constructor.
     * 
     * @param parentShell
     *            parent shell for this dialog.
     * @param sessionID
     *            session ID.
     * @throws ClimateInvalidParameterException
     * @throws SerializationException
     * @throws VizException
     */
    public DisplayStationDailyDialog(Shell parentShell, String sessionID)
            throws ClimateInvalidParameterException, SerializationException,
            VizException {
        super(parentShell);

        DisplayClimateResponse dailyData = (DisplayClimateResponse) ThriftClient
                .sendRequest(new DisplayClimateRequest(sessionID,
                        System.getProperty("user.name")));

        setText("Display Station Daily Climate Values");

        myIsWindSpeedsKnots = false;

        mySessionID = sessionID;

        ClimateRunDailyData dailyCreatorData = (ClimateRunDailyData) dailyData
                .getReportData();

        myDate = dailyCreatorData.getBeginDate();

        /*
         * Get summary type based on period type.
         */
        myPeriodType = dailyCreatorData.getPeriodType();
        switch (myPeriodType) {
        case MORN_NWWS:
            myDailyDisplaySummaryType = DisplayDailySummaryType.MORNING;
            break;
        case MORN_RAD:
            myDailyDisplaySummaryType = DisplayDailySummaryType.MORNING;
            break;
        case INTER_NWWS:
            myDailyDisplaySummaryType = DisplayDailySummaryType.INTERMEDIATE;
            break;
        case INTER_RAD:
            myDailyDisplaySummaryType = DisplayDailySummaryType.INTERMEDIATE;
            break;
        case EVEN_NWWS:
            myDailyDisplaySummaryType = DisplayDailySummaryType.EVENING;
            break;
        case EVEN_RAD:
            myDailyDisplaySummaryType = DisplayDailySummaryType.EVENING;
            break;
        default:
            throw new ClimateInvalidParameterException(
                    "Unhandled period type: [" + myPeriodType + "]");
        }

        myDataMap = dailyCreatorData.getReportMap();

        /*
         * Get the all the climate stations from the master station table (via
         * the previous data request). The master station table is configured by
         * the set-up program.
         */
        for (ClimateDailyReportData dailyReportData : myDataMap.values()) {
            myStations.add(dailyReportData.getStation());
        }

        // get if Display should be read-only
        GetClimateProdGenerateSessionRequest sessionRequest = new GetClimateProdGenerateSessionRequest(
                mySessionID);

        try {
            ClimateProdGenerateSessionDataForView session = (ClimateProdGenerateSessionDataForView) ThriftClient
                    .sendRequest(sessionRequest);

            if (session.getState().equals(SessionState.UNKNOWN)) {
                throw new ClimateInvalidParameterException(
                        "Invalid state for a session");
            }
            // read-only only if session state is past DISPLAY
            myWriteable = session.getState().getValue() <= SessionState.DISPLAY
                    .getValue();
        } catch (VizException e) {
            throw new VizException(
                    "Could not retrieve session information for session ID: ["
                            + mySessionID + "]",
                    e);
        } catch (IndexOutOfBoundsException e) {
            throw new VizException("No sessions returned for session ID: ["
                    + mySessionID + "]", e);
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        // Initialize all of the controls
        initializeComponents();
        loadData();
    }

    /**
     * If exiting not due to user-confirmed abort/"accept and continue", and
     * this is a writeable set of data, confirm with user.
     */
    public boolean shouldClose() {
        boolean close = !completed && myWriteable ? MessageDialog.openConfirm(
                getShell(), "Close?",
                "Are you sure you wish to close? " + " Work will be lost.")
                : true;

        return close;
    }

    /**
     * Initialize the dialog components.
     */
    private void initializeComponents() {
        Composite dialogComp = new Composite(shell, SWT.NONE);
        GridLayout dialogLayout = new GridLayout(1, false);
        dialogComp.setLayout(dialogLayout);

        createMenu();
        createStationControls(dialogComp);
        createTempAndWindDisplay(dialogComp);
        createOtherDataAndObservedDisplay(dialogComp);
    }

    /**
     * Create menu bar.
     */
    private void createMenu() {
        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuHeader.setText("File");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileMenuHeader.setMenu(fileMenu);

        myAcceptValuesMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        myAcceptValuesMenuItem.setText("Accept Values");
        myAcceptValuesMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                acceptValues();
            }
        });
        myAcceptValuesMenuItem.setEnabled(myWriteable && false);

        MenuItem abortClimateRunMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        abortClimateRunMenuItem.setText("Abort Climate Run");
        abortClimateRunMenuItem
                .setToolTipText("Abort this execution in its entirety");
        abortClimateRunMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                completed = MessageDialog.openConfirm(getShell(), "Abort?",
                        "Are you sure you wish to abort? "
                                + "Unsaved changes will be lost.");

                if (completed) {
                    /*
                     * Climate workflow should also be halted
                     */
                    CancelClimateProdGenerateRequest request = new CancelClimateProdGenerateRequest(
                            mySessionID, "User aborted climate run.",
                            System.getProperty("user.name"));
                    try {
                        ThriftClient.sendRequest(request);
                    } catch (VizException e) {
                        logger.error(
                                "Failed to abort session per user request.", e);
                    }
                    // close dialog
                    close();
                }
            }
        });
        abortClimateRunMenuItem.setEnabled(myWriteable);

        MenuItem acceptValuesAndContinueMenuItem = new MenuItem(fileMenu,
                SWT.PUSH);
        acceptValuesAndContinueMenuItem.setText("Accept Values and Continue");
        acceptValuesAndContinueMenuItem.setToolTipText(
                "Accept values for all stations and continue to the next step of execution.");
        acceptValuesAndContinueMenuItem
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        completed = MessageDialog.openConfirm(getShell(),
                                "Continue?",
                                "Are you sure you wish to accept all values for the stations?"
                                        + " Unsaved changes will be lost, and"
                                        + " climate execution will proceed to the next step.");

                        if (completed) {
                            /*
                             * send EDEX request for the Display headless
                             * functionality and continue CPG workflow.
                             */
                            CompleteDisplayClimateRequest request = new CompleteDisplayClimateRequest(
                                    mySessionID, new ClimateRunDailyData(
                                            myPeriodType, myDate, myDataMap));
                            try {
                                ThriftClient.sendRequest(request);
                            } catch (VizException e) {
                                logger.error(
                                        "Error finalizing data after completion of Display module.",
                                        e);
                            }
                            // close the GUI
                            close();
                        }
                    }
                });
        acceptValuesAndContinueMenuItem.setEnabled(myWriteable);

        MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuItem.setText("Help");

        Menu helpMenu = new Menu(menuBar);
        helpMenuItem.setMenu(helpMenu);

        // Handbook menu item
        MenuItem aboutMI = new MenuItem(helpMenu, SWT.NONE);
        aboutMI.setText("Handbook");
        aboutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("execute_AM_PM_climate.html");
            }
        });

        shell.setMenuBar(menuBar);
    }

    /**
     * Set data for dialog.
     */
    private void loadData() {
        changeListener.setIgnoreChanges(true);
        // clear data first
        clearValues();

        // disable save/clear controls unless data is loaded
        myClearValuesButton.setEnabled(false);
        myAcceptValuesButton.setEnabled(false);
        myAcceptValuesMenuItem.setEnabled(false);

        // see what the selected station is
        int selectedStationIndex = myStationList.getSelectionIndex();

        if (selectedStationIndex >= 0
                && selectedStationIndex < myStations.size()) {

            Station newStation = myStations.get(selectedStationIndex);

            try {
                myData = myDataMap.get(newStation.getInformId()).getData();

                myMaxTempTF.setTextAndTip(String.valueOf(myData.getMaxTemp()),
                        myData.getDataMethods().getMaxTempQc());
                myMaxTempTimeTF
                        .setText(myData.getMaxTempTime().toHourMinString());
                myMinTempTF.setTextAndTip(String.valueOf(myData.getMinTemp()),
                        myData.getDataMethods().getMinTempQc());
                myMinTempTimeTF
                        .setText(myData.getMinTempTime().toHourMinString());

                myMaxRelHumTF.setText(String.valueOf(myData.getMaxRelHumid()));
                myMaxRelHumHourTF
                        .setText(String.valueOf(myData.getMaxRelHumidHour()));
                myMinRelHumTF.setText(String.valueOf(myData.getMinRelHumid()));
                myMinRelHumHourTF
                        .setText(String.valueOf(myData.getMinRelHumidHour()));

                ClimateWind maxWind = myData.getMaxWind();
                myMaxWindDirTF.setTextAndTip(String.valueOf(maxWind.getDir()),
                        myData.getDataMethods().getMaxWindQc());
                // switch to knots if knots selected by user and speed is not
                // missing
                float maxWindSpeedMPH = maxWind.getSpeed();
                if (!myIsWindSpeedsKnots
                        || (int) maxWindSpeedMPH == ParameterFormatClimate.MISSING_SPEED) {
                    myMaxWindSpeedTF.setTextAndTip(
                            String.valueOf(maxWindSpeedMPH),
                            myData.getDataMethods().getMaxWindQc());
                } else {
                    myMaxWindSpeedTF.setTextAndTip(
                            String.valueOf(maxWindSpeedMPH
                                    / ClimateUtilities.KNOTS_TO_MPH),
                            myData.getDataMethods().getMaxWindQc());
                }
                myMaxWindTimeTF
                        .setText(myData.getMaxWindTime().toHourMinString());

                ClimateWind maxGust = myData.getMaxGust();
                myMaxGustDirTF.setTextAndTip(String.valueOf(maxGust.getDir()),
                        myData.getDataMethods().getMaxGustQc());
                // switch to knots if knots selected by user and speed is
                // not missing
                float maxGustSpeedMPH = maxGust.getSpeed();
                if (!myIsWindSpeedsKnots
                        || (int) maxGustSpeedMPH == ParameterFormatClimate.MISSING_SPEED) {
                    myMaxGustSpeedTF.setTextAndTip(
                            String.valueOf(maxGustSpeedMPH),
                            myData.getDataMethods().getMaxGustQc());
                } else {
                    myMaxGustSpeedTF.setTextAndTip(
                            String.valueOf(maxGustSpeedMPH
                                    / ClimateUtilities.KNOTS_TO_MPH),
                            myData.getDataMethods().getMaxGustQc());
                }
                myMaxGustTimeTF
                        .setText(myData.getMaxGustTime().toHourMinString());

                // switch to knots if knots selected by user and speed is
                // not missing
                float avgWindSpeedMPH = myData.getAvgWindSpeed();
                if (!myIsWindSpeedsKnots
                        || (int) avgWindSpeedMPH == ParameterFormatClimate.MISSING_SPEED) {
                    myAvgWindSpeedTF.setTextAndTip(
                            String.valueOf(avgWindSpeedMPH),
                            myData.getDataMethods().getAvgWindQc());
                } else {
                    myAvgWindSpeedTF.setTextAndTip(
                            String.valueOf(avgWindSpeedMPH
                                    / ClimateUtilities.KNOTS_TO_MPH),
                            myData.getDataMethods().getAvgWindQc());
                }

                myPrecipTF.setTextAndTip(String.valueOf(myData.getPrecip()),
                        myData.getDataMethods().getPrecipQc());

                mySnowDayFallTF.setTextAndTip(
                        String.valueOf(myData.getSnowDay()),
                        myData.getDataMethods().getSnowQc());

                myMinutesSunTF.setText(String.valueOf(myData.getMinutesSun()));

                myPercentPossSunTF.setTextAndTip(
                        String.valueOf(myData.getPercentPossSun()),
                        myData.getDataMethods().getPossSunQc());

                mySkyCoverTF.setTextAndTip(String.valueOf(myData.getSkyCover()),
                        myData.getDataMethods().getSkyCoverQc());

                mySnowOnGroundTF.setTextAndTip(
                        String.valueOf(myData.getSnowGround()),
                        myData.getDataMethods().getDepthQc());

                myMaxSLPTF.setText(String.valueOf(myData.getMaxSlp()));
                myMinSLPTF.setText(String.valueOf(myData.getMinSlp()));

                myWeatherQCTip
                        .setQCValue(myData.getDataMethods().getWeatherQc());

                int[] wxTypes = myData.getWxType();
                try {
                    myTSCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_THUNDER_STORM_INDEX] == 1);
                    myNegFZRACheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_LIGHT_FREEZING_RAIN_INDEX] == 1);
                    myFGCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_FOG_INDEX] == 1);
                    myMixedCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_MIXED_PRECIP_INDEX] == 1);
                    myGRCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_HAIL_INDEX] == 1);
                    myFGFourthSMCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_FOG_QUARTER_SM_INDEX] == 1);
                    myPosRACheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_HEAVY_RAIN_INDEX] == 1);
                    myPosSNCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_HEAVY_SNOW_INDEX] == 1);
                    myHZCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_HAZE_INDEX] == 1);
                    myRACheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_RAIN_INDEX] == 1);
                    mySNCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_SNOW_INDEX] == 1);
                    myBLSNCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_BLOWING_SNOW_INDEX] == 1);
                    myNegRACheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_LIGHT_RAIN_INDEX] == 1);
                    myNegSNCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_LIGHT_SNOW_INDEX] == 1);
                    mySSCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_SAND_STORM_INDEX] == 1);
                    myFZRACheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_FREEZING_RAIN_INDEX] == 1);
                    myPLCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_ICE_PELLETS_INDEX] == 1);
                    myFCCheckbox.setSelection(
                            wxTypes[DailyClimateData.WX_FUNNEL_CLOUD_INDEX] == 1);

                    // re-enable save/clear controls, data is loaded
                    myClearValuesButton.setEnabled(myWriteable && true);
                    myAcceptValuesButton.setEnabled(myWriteable && true);
                    myAcceptValuesMenuItem.setEnabled(myWriteable && true);

                } catch (ArrayIndexOutOfBoundsException e) {
                    String message = "Weather Types array for Daily Climate Data is not of expected length."
                            + "\nWeather Types checkboxes may not reflect accurate status."
                            + "\nSelect a different station.";
                    logger.error(message, e);
                    MessageDialog.openError(getShell(),
                            "Unexpected Weather Types Length", message);
                    throw new VizException(e);
                }
            } catch (VizException e) {
                String message = "Could not retrieve data for Climate Daily Display dialog, with station ID ["
                        + newStation.getInformId() + "] and date ["
                        + myDate.toFullDateString()
                        + "]. Select a different station.";
                logger.error(message, e);
                MessageDialog.openError(getShell(), "Data Retrieval Error",
                        message);
                myData = null;
            }

            changeListener.setIgnoreChanges(false);
            changeListener.setChangesUnsaved(false);
        } else {
            logger.debug("Station Index [" + selectedStationIndex
                    + "] is not valid. No station data loaded.");
            myData = null;
        }
    }

    /**
     * Create other measures and observed weather display.
     * 
     * @param parent
     *            parent component.
     */
    private void createOtherDataAndObservedDisplay(Composite parent) {
        Composite otherDataAndObservedDisplayComp = new Composite(parent,
                SWT.NONE);
        GridLayout otherDataAndObservedDisplayLayout = new GridLayout(2, false);
        otherDataAndObservedDisplayLayout.marginLeft = 3;
        otherDataAndObservedDisplayComp
                .setLayout(otherDataAndObservedDisplayLayout);

        /*
         * Other data box.
         */
        createOtherDataSection(otherDataAndObservedDisplayComp);

        /*
         * Observed weather box.
         */
        createObservedWeatherSection(otherDataAndObservedDisplayComp);
    }

    /**
     * Create other data section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createOtherDataSection(Composite parent) {
        Group otherDataDisplayComp = new Group(parent, SWT.BORDER);
        GridLayout otherDataDisplayLayout = new GridLayout(2, false);
        otherDataDisplayLayout.verticalSpacing = 5;
        otherDataDisplayComp.setLayout(otherDataDisplayLayout);

        // precip label
        Label precipLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        precipLbl.setText("Precipitation (INCHES)");

        myPrecipTF = new QCTextComp(otherDataDisplayComp, SWT.NONE,
                "Precipitation (INCHES)", QCValueType.PRECIP);
        ((RowLayout) myPrecipTF.getLayout()).marginLeft = 0;
        myPrecipTF.addListener(SWT.Verify,
                myDisplayListeners.getPrecipListener());
        myPrecipTF.addListener(SWT.FocusOut,
                myDisplayListeners.getPrecipListener());
        myPrecipTF.addListener(SWT.Modify, changeListener);

        // snowfall label
        Label snowFallLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        snowFallLbl.setText("Snowfall (INCHES)");

        mySnowDayFallTF = new QCTextComp(otherDataDisplayComp, SWT.NONE,
                "Snowfall (INCHES)", QCValueType.SNOW);
        ((RowLayout) mySnowDayFallTF.getLayout()).marginLeft = 0;
        mySnowDayFallTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        mySnowDayFallTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        mySnowDayFallTF.addListener(SWT.Modify, changeListener);

        // minutes sun label
        Label minutesSunLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        minutesSunLbl.setText("Minutes of Sunshine");

        myMinutesSunTF = new Text(otherDataDisplayComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMinutesSunTF);
        myMinutesSunTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myMinutesSunTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myMinutesSunTF.addListener(SWT.Modify, changeListener);

        // percent sun label
        Label percentSunLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        percentSunLbl.setText("Percent of Sunshine");

        myPercentPossSunTF = new QCTextComp(otherDataDisplayComp, SWT.NONE,
                "Percent of Sunshine", QCValueType.SUNSHINE);
        ((RowLayout) myPercentPossSunTF.getLayout()).marginLeft = 0;
        myPercentPossSunTF.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        myPercentPossSunTF.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        myPercentPossSunTF.addListener(SWT.Modify, changeListener);

        // avg sky cover label
        Label avgSkyCoverLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        avgSkyCoverLbl.setText("Average Sky Cover");

        mySkyCoverTF = new QCTextComp(otherDataDisplayComp, SWT.NONE,
                "Average Sky Cover", QCValueType.SKYCOVER);
        ((RowLayout) mySkyCoverTF.getLayout()).marginLeft = 0;
        mySkyCoverTF.addListener(SWT.Verify,
                myDisplayListeners.getSkyCoverListener());
        mySkyCoverTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSkyCoverListener());
        mySkyCoverTF.addListener(SWT.Modify, changeListener);

        // snow on ground label
        Label snowGroundLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        snowGroundLbl.setText("Snow on Ground (INCHES)");

        mySnowOnGroundTF = new QCTextComp(otherDataDisplayComp, SWT.NONE,
                "Snow on Ground (INCHES)", QCValueType.SNOWDEPTH);
        ((RowLayout) mySnowOnGroundTF.getLayout()).marginLeft = 0;
        mySnowOnGroundTF.addListener(SWT.Verify,
                myDisplayListeners.getSnowFallListener());
        mySnowOnGroundTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSnowFallListener());
        mySnowOnGroundTF.addListener(SWT.Modify, changeListener);

        // max SLP label
        Label maxSLPLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        maxSLPLbl.setText("Maximum SLP (INCHES)");

        myMaxSLPTF = new Text(otherDataDisplayComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMaxSLPTF);
        myMaxSLPTF.addListener(SWT.Verify, myDisplayListeners.getSlpListener());
        myMaxSLPTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSlpListener());
        myMaxSLPTF.addListener(SWT.Modify, changeListener);

        // min SLP label
        Label minSLPLbl = new Label(otherDataDisplayComp, SWT.NORMAL);
        minSLPLbl.setText("Minimum SLP (INCHES)");

        myMinSLPTF = new Text(otherDataDisplayComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsGD(myMinSLPTF);
        myMinSLPTF.addListener(SWT.Verify, myDisplayListeners.getSlpListener());
        myMinSLPTF.addListener(SWT.FocusOut,
                myDisplayListeners.getSlpListener());
        myMinSLPTF.addListener(SWT.Modify, changeListener);
    }

    /**
     * Create observed weather section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createObservedWeatherSection(Composite parent) {
        Composite observedWeatherDisplayComp = new Composite(parent, SWT.NONE);
        RowLayout observedWeatherDisplayLayout = new RowLayout(SWT.VERTICAL);
        observedWeatherDisplayLayout.spacing = 5;
        observedWeatherDisplayLayout.center = true;
        observedWeatherDisplayComp.setLayout(observedWeatherDisplayLayout);

        // checkboxes
        Group observedWeatherCheckBoxesDisplayComp = new Group(
                observedWeatherDisplayComp, SWT.BORDER | SWT.CENTER);
        observedWeatherCheckBoxesDisplayComp.setText("Observed Weather");
        GridLayout observedWeatherDisplayCheckBoxesLayout = new GridLayout(3,
                true);
        observedWeatherDisplayCheckBoxesLayout.horizontalSpacing = 10;
        observedWeatherDisplayCheckBoxesLayout.verticalSpacing = 10;
        observedWeatherDisplayCheckBoxesLayout.marginBottom = 10;
        observedWeatherDisplayCheckBoxesLayout.marginTop = 10;
        observedWeatherDisplayCheckBoxesLayout.marginLeft = 15;
        observedWeatherDisplayCheckBoxesLayout.marginRight = 15;
        observedWeatherCheckBoxesDisplayComp
                .setLayout(observedWeatherDisplayCheckBoxesLayout);

        myWeatherQCTip = new QCToolTip(observedWeatherCheckBoxesDisplayComp,
                "Observed Weather", QCValueType.WEATHER);
        myWeatherQCTip.setDataValueVisible(false);

        myTSCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myTSCheckbox.setText("TS");
        myTSCheckbox.addListener(SWT.Selection, changeListener);

        myNegFZRACheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myNegFZRACheckbox.setText("-FZRA");
        myNegFZRACheckbox.addListener(SWT.Selection, changeListener);

        myFGCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myFGCheckbox.setText("FG");
        myFGCheckbox.addListener(SWT.Selection, changeListener);

        myMixedCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myMixedCheckbox.setText("MIXED");
        myMixedCheckbox.addListener(SWT.Selection, changeListener);

        myGRCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myGRCheckbox.setText("GR");
        myGRCheckbox.addListener(SWT.Selection, changeListener);

        myFGFourthSMCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myFGFourthSMCheckbox.setText("FG <=1/4SM");
        myFGFourthSMCheckbox.addListener(SWT.Selection, changeListener);

        myPosRACheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myPosRACheckbox.setText("+RA");
        myPosRACheckbox.addListener(SWT.Selection, changeListener);

        myPosSNCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myPosSNCheckbox.setText("+SN");
        myPosSNCheckbox.addListener(SWT.Selection, changeListener);

        myHZCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myHZCheckbox.setText("HZ");
        myHZCheckbox.addListener(SWT.Selection, changeListener);

        myRACheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myRACheckbox.setText("RA");
        myRACheckbox.addListener(SWT.Selection, changeListener);

        mySNCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        mySNCheckbox.setText("SN");
        mySNCheckbox.addListener(SWT.Selection, changeListener);

        myBLSNCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myBLSNCheckbox.setText("BLSN");
        myBLSNCheckbox.addListener(SWT.Selection, changeListener);

        myNegRACheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myNegRACheckbox.setText("-RA");
        myNegRACheckbox.addListener(SWT.Selection, changeListener);

        myNegSNCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myNegSNCheckbox.setText("-SN");
        myNegSNCheckbox.addListener(SWT.Selection, changeListener);

        mySSCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        mySSCheckbox.setText("SS");
        mySSCheckbox.addListener(SWT.Selection, changeListener);

        myFZRACheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myFZRACheckbox.setText("FZRA");
        myFZRACheckbox.addListener(SWT.Selection, changeListener);

        myPLCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myPLCheckbox.setText("PL");
        myPLCheckbox.addListener(SWT.Selection, changeListener);

        myFCCheckbox = new Button(observedWeatherCheckBoxesDisplayComp,
                SWT.CHECK);
        myFCCheckbox.setText("FC");
        myFCCheckbox.addListener(SWT.Selection, changeListener);
    }

    /**
     * Create temp and wind display.
     * 
     * @param parent
     *            parent component.
     */
    private void createTempAndWindDisplay(Composite parent) {
        Composite tempAndWindDisplayComp = new Composite(parent, SWT.NONE);
        RowLayout tempAndWindDisplayLayout = new RowLayout(SWT.VERTICAL);
        tempAndWindDisplayLayout.marginLeft = 18;
        tempAndWindDisplayComp.setLayout(tempAndWindDisplayLayout);

        /*
         * Wind Speeds in knots checkbox.
         */
        createWindSpeedKnotsSection(tempAndWindDisplayComp);

        /*
         * Data labels/fields.
         */
        createMainDataLabelsAndFieldsSection(tempAndWindDisplayComp);
    }

    /**
     * Create wind speed knots section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createWindSpeedKnotsSection(Composite parent) {
        Composite windSpeedsKnotsComp = new Composite(parent, SWT.NONE);
        RowLayout windSpeedsKnotsLayout = new RowLayout(SWT.HORIZONTAL);
        windSpeedsKnotsComp.setLayout(windSpeedsKnotsLayout);

        RowData windSpeedsKnotsRD = new RowData(250, SWT.DEFAULT);
        final Button myWindSpeedsKnotsCheckbox = new Button(windSpeedsKnotsComp,
                SWT.CHECK);
        myWindSpeedsKnotsCheckbox.setText("Select for Wind Speeds in knots");
        myWindSpeedsKnotsCheckbox.setLayoutData(windSpeedsKnotsRD);
        myWindSpeedsKnotsCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (myIsWindSpeedsKnots != myWindSpeedsKnotsCheckbox
                        .getSelection()) {
                    changeListener.setIgnoreChanges(true);

                    // selection value changed
                    myIsWindSpeedsKnots = myWindSpeedsKnotsCheckbox
                            .getSelection();
                    // figure out conversion
                    // also convert labels here
                    double multiplier;
                    String units;
                    if (myIsWindSpeedsKnots) {
                        // convert to knots
                        multiplier = 1 / ClimateUtilities.KNOTS_TO_MPH;
                        units = DisplayValues.KNOTS_ABB;
                    } else {
                        // convert to mph
                        multiplier = ClimateUtilities.KNOTS_TO_MPH;
                        units = DisplayValues.MPH_ABB;
                    }

                    myMaxWindSpeedLbl
                            .setText(MAXIMUM_WIND_SPEED_STRING + " " + units);
                    myMaxWindGustLbl
                            .setText(MAXIMUM_WIND_GUST_SPEED + " " + units);
                    myAvgWindSpeedLbl.setText(AVERAGE_WIND_SPEED + " " + units);

                    /*
                     * convert all wind fields, if they are not set to missing
                     * value
                     */
                    try {
                        float maxWindSpeedToConvert = Float
                                .parseFloat(myMaxWindSpeedTF.getText());

                        if ((int) maxWindSpeedToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            double convertedValue = maxWindSpeedToConvert
                                    * multiplier;
                            myMaxWindSpeedTF
                                    .setText(String.valueOf(convertedValue));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse max wind speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myMaxWindSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }

                    try {
                        float maxWindGustToConvert = Float
                                .parseFloat(myMaxGustSpeedTF.getText());

                        if ((int) maxWindGustToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            double convertedValue = maxWindGustToConvert
                                    * multiplier;
                            myMaxGustSpeedTF
                                    .setText(String.valueOf(convertedValue));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse max gust speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myMaxGustSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }

                    try {
                        float avgWindSpeedToConvert = Float
                                .parseFloat(myAvgWindSpeedTF.getText());

                        if ((int) avgWindSpeedToConvert != ParameterFormatClimate.MISSING_SPEED) {
                            double convertedValue = avgWindSpeedToConvert
                                    * multiplier;
                            myAvgWindSpeedTF
                                    .setText(String.valueOf(convertedValue));
                        }
                    } catch (NumberFormatException e) {
                        logger.error(
                                "Could not parse average wind speed for knot-mph conversion."
                                        + "\nMissing value will be used.",
                                e);
                        myAvgWindSpeedTF.setText(String
                                .valueOf(ParameterFormatClimate.MISSING_SPEED));
                    }
                    /*
                     * Convert QC Tooltip texts
                     * 
                     * Show saved values if displaying in MPH (default)
                     * 
                     * If displaying in knots and saved value is not missing,
                     * convert to knots
                     */
                    if (!myIsWindSpeedsKnots) {
                        // MPH
                        myMaxWindSpeedTF.getToolTip().setDataValue(
                                String.valueOf(myData.getMaxWind().getSpeed()));
                        myMaxGustSpeedTF.getToolTip().setDataValue(
                                String.valueOf(myData.getMaxGust().getSpeed()));
                        myAvgWindSpeedTF.getToolTip().setDataValue(
                                String.valueOf(myData.getAvgWindSpeed()));
                    } else {
                        // knots
                        int precision = myDisplayListeners.getWindSpdListener()
                                .getNumDecimal();

                        double newMaxWind;
                        if ((int) myData.getMaxWind()
                                .getSpeed() == ParameterFormatClimate.MISSING_SPEED) {
                            newMaxWind = ParameterFormatClimate.MISSING_SPEED;
                        } else {
                            newMaxWind = ((int) ((myData.getMaxWind().getSpeed()
                                    / ClimateUtilities.KNOTS_TO_MPH)
                                    * Math.pow(10, precision)))
                                    / Math.pow(10, precision);
                        }
                        myMaxWindSpeedTF.getToolTip()
                                .setDataValue(String.valueOf(newMaxWind));

                        double newMaxGust;
                        if ((int) myData.getMaxGust()
                                .getSpeed() == ParameterFormatClimate.MISSING_SPEED) {
                            newMaxGust = ParameterFormatClimate.MISSING_SPEED;
                        } else {
                            newMaxGust = ((int) ((myData.getMaxGust().getSpeed()
                                    / ClimateUtilities.KNOTS_TO_MPH)
                                    * Math.pow(10, precision)))
                                    / Math.pow(10, precision);
                        }
                        myMaxGustSpeedTF.getToolTip()
                                .setDataValue(String.valueOf(newMaxGust));

                        double newAvgWindSpd;
                        if ((int) myData
                                .getAvgWindSpeed() == ParameterFormatClimate.MISSING_SPEED) {
                            newAvgWindSpd = ParameterFormatClimate.MISSING_SPEED;
                        } else {
                            newAvgWindSpd = ((int) ((myData.getAvgWindSpeed()
                                    / ClimateUtilities.KNOTS_TO_MPH)
                                    * Math.pow(10, precision)))
                                    / Math.pow(10, precision);
                        }
                        myAvgWindSpeedTF.getToolTip()
                                .setDataValue(String.valueOf(newAvgWindSpd));
                    }

                    changeListener.setIgnoreChanges(false);
                }
            }
        });
    }

    /**
     * Create main data labels and fields section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createMainDataLabelsAndFieldsSection(Composite parent) {
        Group labelsAndFieldsComp = new Group(parent, SWT.BORDER);
        GridLayout labelsAndFieldsLayout = new GridLayout(3, false);
        labelsAndFieldsLayout.horizontalSpacing = 5;
        labelsAndFieldsComp.setLayout(labelsAndFieldsLayout);

        // max temp label
        Label maxTempLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxTempLbl.setText("Maximum Temperature (F)");

        // max temp fields
        myMaxTempTF = new QCTextComp(labelsAndFieldsComp, SWT.NONE,
                "Maximum Temperature (F)", QCValueType.TEMPERATURE);
        myMaxTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        myMaxTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        myMaxTempTF.addListener(SWT.Modify, changeListener);

        myMaxTempTimeTF = new Text(myMaxTempTF,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myMaxTempTimeTF.addFocusListener(
                new TimeSelectorFocusListener(myMaxTempTimeTF));
        ClimateLayoutValues.assignFieldsRD(myMaxTempTimeTF);
        myMaxTempTimeTF.addListener(SWT.Modify, changeListener);

        // max temp time label
        Label maxTempTimeLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxTempTimeLbl.setText("Time of Maximum Temperature");

        // min temp label
        Label minTempLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        minTempLbl.setText("Minimum Temperature (F)");

        // min temp fields
        myMinTempTF = new QCTextComp(labelsAndFieldsComp, SWT.NONE,
                "Minimum Temperature (F)", QCValueType.TEMPERATURE);
        myMinTempTF.addListener(SWT.Verify,
                myDisplayListeners.getTempIntListener());
        myMinTempTF.addListener(SWT.FocusOut,
                myDisplayListeners.getTempIntListener());
        myMinTempTF.addListener(SWT.Modify, changeListener);

        myMinTempTimeTF = new Text(myMinTempTF,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myMinTempTimeTF.addFocusListener(
                new TimeSelectorFocusListener(myMinTempTimeTF));
        ClimateLayoutValues.assignFieldsRD(myMinTempTimeTF);
        myMinTempTimeTF.addListener(SWT.Modify, changeListener);

        // min temp time label
        Label minTempTimeLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        minTempTimeLbl.setText("Time of Minimum Temperature");

        // max hum label
        Label maxHumLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxHumLbl.setText("Maximum Relative Humidity (%)");

        // max hum fields
        Composite maxHumFieldsComp = new Composite(labelsAndFieldsComp,
                SWT.NONE);
        RowLayout maxHumFieldsLayout = new RowLayout(SWT.HORIZONTAL);
        maxHumFieldsComp.setLayout(maxHumFieldsLayout);
        myMaxRelHumTF = new Text(maxHumFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMaxRelHumTF);
        myMaxRelHumTF.addListener(SWT.Verify,
                myDisplayListeners.getRelHumListener());
        myMaxRelHumTF.addListener(SWT.FocusOut,
                myDisplayListeners.getRelHumListener());
        myMaxRelHumTF.addListener(SWT.Modify, changeListener);

        myMaxRelHumHourTF = new Text(maxHumFieldsComp,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myMaxRelHumHourTF.addFocusListener(
                new TimeSelectorFocusListener(myMaxRelHumHourTF, true));
        ClimateLayoutValues.assignShortFieldsRD(myMaxRelHumHourTF);
        myMaxRelHumHourTF.addListener(SWT.Modify, changeListener);

        // max hum time label
        Label maxHumTimeLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxHumTimeLbl.setText("Hour of Maximum Relative Humidity");

        // min hum label
        Label minHumLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        minHumLbl.setText("Minimum Relative Humidity (%)");

        // min hum fields
        Composite minHumFieldsComp = new Composite(labelsAndFieldsComp,
                SWT.NONE);
        RowLayout minHumFieldsLayout = new RowLayout(SWT.HORIZONTAL);
        minHumFieldsComp.setLayout(minHumFieldsLayout);
        myMinRelHumTF = new Text(minHumFieldsComp,
                ClimateLayoutValues.TEXT_FIELD_STYLE);
        ClimateLayoutValues.assignFieldsRD(myMinRelHumTF);
        myMinRelHumTF.addListener(SWT.Verify,
                myDisplayListeners.getRelHumListener());
        myMinRelHumTF.addListener(SWT.FocusOut,
                myDisplayListeners.getRelHumListener());
        myMinRelHumTF.addListener(SWT.Modify, changeListener);

        myMinRelHumHourTF = new Text(minHumFieldsComp,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myMinRelHumHourTF.addFocusListener(
                new TimeSelectorFocusListener(myMinRelHumHourTF, true));
        ClimateLayoutValues.assignShortFieldsRD(myMinRelHumHourTF);
        myMinRelHumHourTF.addListener(SWT.Modify, changeListener);

        // min hum time label
        Label minHumTimeLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        minHumTimeLbl.setText("Hour of Minimum Relative Humidity");

        // max wind direction label
        Label maxWindDirLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxWindDirLbl.setText("Maximum Wind Direction (DEG)");

        // max wind direction fields
        myMaxWindDirTF = new QCTextComp(labelsAndFieldsComp, SWT.NONE,
                "Maximum Wind Direction (DEG)", QCValueType.WIND);
        myMaxWindDirTF.addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxWindDirTF.addListener(SWT.Modify, changeListener);

        myMaxWindTimeTF = new Text(myMaxWindDirTF,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myMaxWindTimeTF.addFocusListener(
                new TimeSelectorFocusListener(myMaxWindTimeTF));
        ClimateLayoutValues.assignFieldsRD(myMaxWindTimeTF);
        myMaxWindTimeTF.addListener(SWT.Modify, changeListener);

        // max wind direction time label
        Label maxWindDirTimeLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxWindDirTimeLbl.setText("Time of Maximum Wind");

        myMaxWindSpeedLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        String windSpeedString;
        if (!myIsWindSpeedsKnots) {
            windSpeedString = MAXIMUM_WIND_SPEED_STRING + " "
                    + DisplayValues.MPH_ABB;
            myMaxWindSpeedLbl.setText(windSpeedString);
        } else {
            windSpeedString = MAXIMUM_WIND_SPEED_STRING + " "
                    + DisplayValues.KNOTS_ABB;
            myMaxWindSpeedLbl.setText(windSpeedString);
        }

        // max wind speed fields
        Composite maxWindSpeedFieldsComp = new Composite(labelsAndFieldsComp,
                SWT.NONE);
        RowLayout maxWindSpeedFieldsLayout = new RowLayout(SWT.HORIZONTAL);
        maxWindSpeedFieldsComp.setLayout(maxWindSpeedFieldsLayout);
        myMaxWindSpeedTF = new QCTextComp(maxWindSpeedFieldsComp, SWT.NONE,
                windSpeedString, QCValueType.WIND);
        ((RowLayout) myMaxWindSpeedTF.getLayout()).marginLeft = 0;
        myMaxWindSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myMaxWindSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myMaxWindSpeedTF.addListener(SWT.Modify, changeListener);

        // max wind speed spacer label
        Label maxWindSpeedSpacerLbl = new Label(labelsAndFieldsComp,
                SWT.NORMAL);
        maxWindSpeedSpacerLbl.setText("");

        // max wind gust direction label
        Label maxWindGustDirLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxWindGustDirLbl.setText("Maximum Wind Gust Direction (DEG)");

        // max wind gust direction fields
        myMaxGustDirTF = new QCTextComp(labelsAndFieldsComp, SWT.NONE,
                "Maximum Wind Gust Direction (DEG)", QCValueType.GUST);
        myMaxGustDirTF.addListener(SWT.Verify,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindDirListener());
        myMaxGustDirTF.addListener(SWT.Modify, changeListener);

        myMaxGustTimeTF = new Text(myMaxGustDirTF,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myMaxGustTimeTF.addFocusListener(
                new TimeSelectorFocusListener(myMaxGustTimeTF));
        ClimateLayoutValues.assignFieldsRD(myMaxGustTimeTF);
        myMaxGustTimeTF.addListener(SWT.Modify, changeListener);

        // max wind direction time label
        Label maxWindGustDirTimeLbl = new Label(labelsAndFieldsComp,
                SWT.NORMAL);
        maxWindGustDirTimeLbl.setText("Time of Maximum Wind Gust");

        myMaxWindGustLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        String gustSpeedString;
        if (!myIsWindSpeedsKnots) {
            gustSpeedString = MAXIMUM_WIND_GUST_SPEED + " "
                    + DisplayValues.MPH_ABB;
            myMaxWindGustLbl.setText(gustSpeedString);
        } else {
            gustSpeedString = MAXIMUM_WIND_GUST_SPEED + " "
                    + DisplayValues.KNOTS_ABB;
            myMaxWindGustLbl.setText(gustSpeedString);
        }

        // max wind gust fields
        Composite maxWindGustFieldsComp = new Composite(labelsAndFieldsComp,
                SWT.NONE);
        RowLayout maxWindGustFieldsLayout = new RowLayout(SWT.HORIZONTAL);
        maxWindGustFieldsComp.setLayout(maxWindGustFieldsLayout);
        myMaxGustSpeedTF = new QCTextComp(maxWindGustFieldsComp, SWT.NONE,
                gustSpeedString, QCValueType.GUST);
        ((RowLayout) myMaxGustSpeedTF.getLayout()).marginLeft = 0;
        myMaxGustSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myMaxGustSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myMaxGustSpeedTF.addListener(SWT.Modify, changeListener);

        // max wind gust spacer label
        Label maxWindGustSpacerLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        maxWindGustSpacerLbl.setText("");

        // avg wind speed label
        myAvgWindSpeedLbl = new Label(labelsAndFieldsComp, SWT.NORMAL);
        String avgWindSpeedString;
        if (!myIsWindSpeedsKnots) {
            avgWindSpeedString = AVERAGE_WIND_SPEED + " "
                    + DisplayValues.MPH_ABB;
            myAvgWindSpeedLbl.setText(avgWindSpeedString);
        } else {
            avgWindSpeedString = AVERAGE_WIND_SPEED + " "
                    + DisplayValues.KNOTS_ABB;
            myAvgWindSpeedLbl.setText(avgWindSpeedString);
        }

        // avg wind speed fields
        Composite avgWindSpeedFieldsComp = new Composite(labelsAndFieldsComp,
                SWT.NONE);
        RowLayout avgWindSpeedFieldsLayout = new RowLayout(SWT.HORIZONTAL);
        avgWindSpeedFieldsComp.setLayout(avgWindSpeedFieldsLayout);
        myAvgWindSpeedTF = new QCTextComp(avgWindSpeedFieldsComp, SWT.NONE,
                avgWindSpeedString, QCValueType.AVGWIND);
        ((RowLayout) myAvgWindSpeedTF.getLayout()).marginLeft = 0;
        myAvgWindSpeedTF.addListener(SWT.Verify,
                myDisplayListeners.getWindSpdListener());
        myAvgWindSpeedTF.addListener(SWT.FocusOut,
                myDisplayListeners.getWindSpdListener());
        myAvgWindSpeedTF.addListener(SWT.Modify, changeListener);

        // avg wind speed spacer label
        Label avgWindSpeedSpacerLbl = new Label(labelsAndFieldsComp,
                SWT.NORMAL);
        avgWindSpeedSpacerLbl.setText("");
    }

    /**
     * Create station controls.
     * 
     * @param parent
     *            parent component.
     */
    private void createStationControls(Composite parent) {
        Composite stationControlsComp = new Composite(parent, SWT.NONE);
        RowLayout stationControlsLayout = new RowLayout(SWT.HORIZONTAL);
        stationControlsLayout.spacing = 10;
        stationControlsLayout.center = true;
        stationControlsComp.setLayout(stationControlsLayout);

        /*
         * Value buttons.
         */
        createValueControlsSection(stationControlsComp);

        /*
         * Station list.
         */
        createStationListSection(stationControlsComp);

        /*
         * Date section.
         */
        createDateSection(stationControlsComp);
    }

    /**
     * Create value controls section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createValueControlsSection(Composite parent) {
        Composite valueControlsComp = new Composite(parent, SWT.NONE);
        RowLayout valueControlsLayout = new RowLayout(SWT.VERTICAL);
        valueControlsLayout.spacing = 25;
        valueControlsLayout.center = true;
        valueControlsLayout.marginHeight = 10;
        valueControlsComp.setLayout(valueControlsLayout);

        // accept values
        RowData acceptValuesRD = new RowData(120, 40);
        myAcceptValuesButton = new Button(valueControlsComp, SWT.PUSH);
        myAcceptValuesButton.setText("Accept Values");
        myAcceptValuesButton
                .setToolTipText("Accept the values for the current station");
        myAcceptValuesButton.setLayoutData(acceptValuesRD);
        myAcceptValuesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                acceptValues();
            }

        });
        myAcceptValuesButton.setEnabled(myWriteable && false);

        // clear values
        RowData clearValuesRD = new RowData(120, 40);
        myClearValuesButton = new Button(valueControlsComp, SWT.PUSH);
        myClearValuesButton.setText("Clear Values");
        myClearValuesButton
                .setToolTipText("Clear all displayed values for the station");
        myClearValuesButton.setLayoutData(clearValuesRD);
        myClearValuesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                boolean clear = MessageDialog.openConfirm(getShell(),
                        "Clear Values?",
                        "Are you sure you wish to clear the displayed values for station "
                                + myStationList.getItem(
                                        myStationList.getSelectionIndex())
                                + "?");
                if (clear) {
                    clearValues();
                }
            }

        });
        myClearValuesButton.setEnabled(myWriteable && false);
    }

    /**
     * Accept input values for the currently selected station. Data is saved to
     * the data class, which is pointing to the object in the data map.
     */
    protected void acceptValues() {
        // knots/mph multiple
        float knotsmphMultiplier;
        if (myIsWindSpeedsKnots) {
            knotsmphMultiplier = (float) ClimateUtilities.KNOTS_TO_MPH;
        } else {
            knotsmphMultiplier = 1;
        }

        int newMaxTemp = Integer.parseInt(myMaxTempTF.getText());
        if (newMaxTemp != myData.getMaxTemp()) {
            myData.setMaxTemp(newMaxTemp);
            myData.getDataMethods().setMaxTempQc(QCValues.MANUAL_ENTRY);
        }
        myData.setMaxTempTime(new ClimateTime(myMaxTempTimeTF.getText()));

        int newMinTemp = Integer.parseInt(myMinTempTF.getText());
        if (newMinTemp != myData.getMinTemp()) {
            myData.setMinTemp(newMinTemp);
            myData.getDataMethods().setMinTempQc(QCValues.MANUAL_ENTRY);
        }
        myData.setMinTempTime(new ClimateTime(myMinTempTimeTF.getText()));

        myData.setMaxRelHumid(Integer.parseInt(myMaxRelHumTF.getText()));
        myData.setMaxRelHumidHour(
                Integer.parseInt(myMaxRelHumHourTF.getText()));
        myData.setMinRelHumid(Integer.parseInt(myMinRelHumTF.getText()));
        myData.setMinRelHumidHour(
                Integer.parseInt(myMinRelHumHourTF.getText()));

        // calculate max wind speed
        float newMaxWindSpeed = Float.parseFloat(myMaxWindSpeedTF.getText());
        if ((int) newMaxWindSpeed != ParameterFormatClimate.MISSING_SPEED) {
            newMaxWindSpeed *= knotsmphMultiplier;
        }
        ClimateWind newMaxWind = new ClimateWind(
                Integer.parseInt(myMaxWindDirTF.getText()), newMaxWindSpeed);
        if (!newMaxWind.equals(myData.getMaxWind())) {
            myData.setMaxWind(newMaxWind);
            myData.getDataMethods().setMaxWindQc(QCValues.MANUAL_ENTRY);
        }
        myData.setMaxWindTime(new ClimateTime(myMaxWindTimeTF.getText()));

        // calculate max gust speed
        float maxGustSpeed = Float.parseFloat(myMaxGustSpeedTF.getText());
        if ((int) maxGustSpeed != ParameterFormatClimate.MISSING_SPEED) {
            maxGustSpeed *= knotsmphMultiplier;
        }
        ClimateWind newMaxGust = new ClimateWind(
                Integer.parseInt(myMaxGustDirTF.getText()), maxGustSpeed);
        if (!newMaxGust.equals(myData.getMaxGust())) {
            myData.setMaxGust(newMaxGust);
            myData.getDataMethods().setMaxGustQc(QCValues.MANUAL_ENTRY);
        }
        myData.setMaxGustTime(new ClimateTime(myMaxGustTimeTF.getText()));

        // calculate avg wind speed
        float avgWindSpeed = Float.parseFloat(myAvgWindSpeedTF.getText());
        if ((int) avgWindSpeed != ParameterFormatClimate.MISSING_SPEED) {
            avgWindSpeed *= knotsmphMultiplier;
        }
        if (!ClimateUtilities.floatingEquals(avgWindSpeed,
                myData.getAvgWindSpeed())) {
            myData.setAvgWindSpeed(avgWindSpeed);
            myData.getDataMethods().setAvgWindQc(QCValues.MANUAL_ENTRY);
        }

        float newPrecip = myPrecipTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(myPrecipTF.getText());
        if (!ClimateUtilities.floatingEquals(newPrecip, myData.getPrecip())) {
            myData.setPrecip(newPrecip);
            myData.getDataMethods().setPrecipQc(QCValues.MANUAL_ENTRY);
        }

        float newSnow = mySnowDayFallTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(mySnowDayFallTF.getText());
        if (!ClimateUtilities.floatingEquals(newSnow, myData.getSnowDay())) {
            myData.setSnowDay(newSnow);
            myData.getDataMethods().setSnowQc(QCValues.MANUAL_ENTRY);
        }

        myData.setMinutesSun(Integer.parseInt(myMinutesSunTF.getText()));

        int newPossSun = Integer.parseInt(myPercentPossSunTF.getText());
        if (newPossSun != myData.getPercentPossSun()) {
            myData.setPercentPossSun(newPossSun);
            myData.getDataMethods().setPossSunQc(QCValues.MANUAL_ENTRY);
        }

        float newSkyCover = Float.parseFloat(mySkyCoverTF.getText());
        if (!ClimateUtilities.floatingEquals(newSkyCover,
                myData.getSkyCover())) {
            myData.setSkyCover(newSkyCover);
            myData.getDataMethods().setSkyCoverQc(QCValues.MANUAL_ENTRY);
        }

        float newSnowGround = mySnowOnGroundTF.getText()
                .equalsIgnoreCase(ParameterFormatClimate.TRACE_SYMBOL)
                        ? ParameterFormatClimate.TRACE
                        : Float.parseFloat(mySnowOnGroundTF.getText());
        if (!ClimateUtilities.floatingEquals(newSnowGround,
                myData.getSnowGround())) {
            myData.setSnowGround(newSnowGround);
            myData.getDataMethods().setDepthQc(QCValues.MANUAL_ENTRY);
        }

        myData.setMaxSlp(Double.parseDouble(myMaxSLPTF.getText()));
        myData.setMinSlp(Double.parseDouble(myMinSLPTF.getText()));

        // copy of wxtype array before its altered
        int[] oldWxType = Arrays.copyOf(myData.getWxType(),
                myData.getWxType().length);

        myData.getWxType()[DailyClimateData.WX_THUNDER_STORM_INDEX] = myTSCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_LIGHT_FREEZING_RAIN_INDEX] = myNegFZRACheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_FOG_INDEX] = myFGCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_MIXED_PRECIP_INDEX] = myMixedCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_HAIL_INDEX] = myGRCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_FOG_QUARTER_SM_INDEX] = myFGFourthSMCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_HEAVY_RAIN_INDEX] = myPosRACheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_HEAVY_SNOW_INDEX] = myPosSNCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_HAZE_INDEX] = myHZCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_RAIN_INDEX] = myRACheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_SNOW_INDEX] = mySNCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_BLOWING_SNOW_INDEX] = myBLSNCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_LIGHT_RAIN_INDEX] = myNegRACheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_LIGHT_SNOW_INDEX] = myNegSNCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_SAND_STORM_INDEX] = mySSCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_FREEZING_RAIN_INDEX] = myFZRACheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_ICE_PELLETS_INDEX] = myPLCheckbox
                .getSelection() ? 1 : 0;
        myData.getWxType()[DailyClimateData.WX_FUNNEL_CLOUD_INDEX] = myFCCheckbox
                .getSelection() ? 1 : 0;

        if (!Arrays.equals(myData.getWxType(), oldWxType)) {
            myData.getDataMethods().setWeatherQc(QCValues.MANUAL_ENTRY);
        }

        // go to next station in list
        myStationList.setSelection(myStationList.getSelectionIndex()
                + 1 < myStationList.getItemCount()
                        ? myStationList.getSelectionIndex() + 1 : 0);
        stationSelectionChanged(false);

        changeListener.setChangesUnsaved(false);
    }

    /**
     * Clear all text fields and checkboxes to missing values.
     */
    protected void clearValues() {
        myMaxTempTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        myMaxTempTimeTF.setText(ClimateTime.MISSING_TIME_STRING);
        myMinTempTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        myMinTempTimeTF.setText(ClimateTime.MISSING_TIME_STRING);
        myMaxRelHumTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myMaxRelHumHourTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        myMinRelHumTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myMinRelHumHourTF
                .setText(String.valueOf(ParameterFormatClimate.MISSING_HOUR));
        myMaxWindDirTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        myMaxWindTimeTF.setText(ClimateTime.MISSING_TIME_STRING);
        myMaxWindSpeedTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SPEED),
                ParameterFormatClimate.MISSING);
        myMaxGustDirTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        myMaxGustTimeTF.setText(ClimateTime.MISSING_TIME_STRING);
        myMaxGustSpeedTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SPEED),
                ParameterFormatClimate.MISSING);
        myAvgWindSpeedTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SPEED),
                ParameterFormatClimate.MISSING);
        myPrecipTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_PRECIP),
                ParameterFormatClimate.MISSING);
        mySnowDayFallTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SNOW_VALUE),
                ParameterFormatClimate.MISSING);
        myMinutesSunTF.setText(String.valueOf(ParameterFormatClimate.MISSING));
        myPercentPossSunTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        mySkyCoverTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING),
                ParameterFormatClimate.MISSING);
        mySnowOnGroundTF.setTextAndTip(
                String.valueOf(ParameterFormatClimate.MISSING_SNOW_VALUE),
                ParameterFormatClimate.MISSING);
        myMaxSLPTF.setText(String.valueOf(ParameterFormatClimate.MISSING_SLP));
        myMinSLPTF.setText(String.valueOf(ParameterFormatClimate.MISSING_SLP));
        myWeatherQCTip.setQCValue(ParameterFormatClimate.MISSING);
        myTSCheckbox.setSelection(false);
        myNegFZRACheckbox.setSelection(false);
        myFGCheckbox.setSelection(false);
        myMixedCheckbox.setSelection(false);
        myGRCheckbox.setSelection(false);
        myFGFourthSMCheckbox.setSelection(false);
        myPosRACheckbox.setSelection(false);
        myPosSNCheckbox.setSelection(false);
        myHZCheckbox.setSelection(false);
        myRACheckbox.setSelection(false);
        mySNCheckbox.setSelection(false);
        myBLSNCheckbox.setSelection(false);
        myNegRACheckbox.setSelection(false);
        myNegSNCheckbox.setSelection(false);
        mySSCheckbox.setSelection(false);
        myFZRACheckbox.setSelection(false);
        myPLCheckbox.setSelection(false);
        myFCCheckbox.setSelection(false);
    }

    /**
     * Create station list section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createStationListSection(Composite parent) {
        Composite stationListComp = new Composite(parent, SWT.NONE);
        RowLayout stationListLayout = new RowLayout(SWT.VERTICAL);
        stationListLayout.spacing = 5;
        stationListLayout.center = true;
        stationListComp.setLayout(stationListLayout);

        // Station label
        Label stationLbl = new Label(stationListComp, SWT.NORMAL);
        stationLbl.setText("Station");

        // Station list
        RowData stationListRD = new RowData(250, 150);
        myStationList = new List(stationListComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        myStationList.setLayoutData(stationListRD);

        myStationList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                stationSelectionChanged(true);
            }
        });
        for (Station station : myStations) {
            myStationList.add(station.getStationName());
        }
    }

    /**
     * Create date section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createDateSection(Composite parent) {
        Composite dateComp = new Composite(parent, SWT.NONE);
        RowLayout dateLayout = new RowLayout(SWT.VERTICAL);
        dateLayout.spacing = 5;
        dateLayout.center = true;
        dateComp.setLayout(dateLayout);

        // Date label
        Label dateLbl = new Label(dateComp, SWT.NORMAL);
        dateLbl.setText("Date");

        GC gc = new GC(dateLbl);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        // Date field
        RowData dateRD = new RowData(20 * fontWidth, (3 * fontHeight) / 2);
        myDateTF = new Text(dateComp, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myDateTF.setEditable(false);
        myDateTF.setLayoutData(dateRD);
        myDateTF.setText(myDate.toFullDateString());

        // Summary type label
        Label summaryTypeLbl = new Label(dateComp, SWT.NORMAL);
        summaryTypeLbl.setText("Type of Summary");

        // Summary type field
        RowData summaryTypeRD = new RowData(38 * fontWidth,
                (3 * fontHeight) / 2);
        mySummaryTypeTF = new Text(dateComp,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        mySummaryTypeTF.setEditable(false);
        mySummaryTypeTF.setLayoutData(summaryTypeRD);
        mySummaryTypeTF.setText(myDailyDisplaySummaryType.toString());
    }

    /**
     * Selection for station list changed.
     * 
     * @param warn
     *            true to warn user.
     */
    private void stationSelectionChanged(boolean warn) {
        int currSelection = myStationList.getSelectionIndex();
        if ((myPrevStationSelectionIndex != currSelection)
                && (currSelection != -1)) {
            // warn that current values will be lost if unsaved
            boolean load = true;

            if (myWriteable && warn && myPrevStationSelectionIndex != -1
                    && changeListener.isChangesUnsaved()) {
                // confirm that work is done/willing to be lost for
                // previous selection
                load = MessageDialog.openConfirm(getShell(), "Load new data?",
                        "Are you sure you wish to load "
                                + myStationList.getItem(currSelection)
                                + "? Unsaved changes to "
                                + myStationList
                                        .getItem(myPrevStationSelectionIndex)
                                + " will be lost.");
            }
            if (load) {
                myPrevStationSelectionIndex = currSelection;
                loadData();
            } else {
                myStationList.setSelection(myPrevStationSelectionIndex);
            }
        }
    }

    /**
     * This enum has choices for morning, intermediate, or evening daily
     * reports.
     * 
     * <pre>
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * 17 MAY 2016  18384      amoore      Initial creation
     * 16 AUG 2016  20414      amoore      "Climate Summary" string is redundant.
     * 21 MAR 2017  30166      amoore      Move to Dialog class; only used there.
     * </pre>
     * 
     * @author amoore
     */
    public enum DisplayDailySummaryType {

        MORNING("Morning Daily"),

        INTERMEDIATE("Intermediate Daily"),

        EVENING("Evening Daily");

        private final String value;

        /**
         * @param iValue
         */
        private DisplayDailySummaryType(final String iValue) {
            this.value = iValue;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period.dialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdGenerateSessionDataForView;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodDesc;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.display.DisplayMonthlyASOSClimateServiceRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CancelClimateProdGenerateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CompleteDisplayClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.DisplayClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.GetClimateProdGenerateSessionRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.DisplayClimateResponse;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.support.DataValueOrigin;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * This class displays the Period Station Climate values.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 18 JUL 2016  20414      amoore      Start making more generic for periods,
 *                                     not just monthly.
 * 28 JUL 2016  20652      amoore      Common SWT style for Climate dialogs.
 * 04 AUG 2016  20414      amoore      Address CP1.01-beta GUI comments. Beginnings of query logic.
 * 08 AUG 2016  20414      amoore      Making monthly logic.
 * 16 AUG 2016  20414      amoore      Monthly-specific logic completed.
 * 09 SEP 2016  20414      amoore      Smarter warning on potential loss of changes. Add reference to 
 *                                     help Handbook. Disable saving if using an incomplete period.
 * 12 SEP 2016  20414      amoore      Loading of saved data.
 * 01 DEC 2016  20414      amoore      Cleanup + comments.
 * 08 DEC 2016  20414      amoore      Take Climate Creator data as input. Add comments for saving/output
 *                                     given deeper understanding of Legacy operations. Add bounds to the
 *                                     date selection components. Fixed tooltip for disabled save button.
 * 09 DEC 2016  27015      amoore      Basic database output flow.
 * 14 DEC 2016  27015      amoore      Period data output completion/shifting to EDEX-side.
 * 19 DEC 2016  20955      amoore      Use global properties for the user-defined thresholds.
 * 21 DEC 2016  20955      amoore      Fixes for user-defined thresholds labels and fields.
 * 27 DEC 2016  22450      amoore      Wind direction precision.
 * 28 DEC 2016  22784      amoore      Sky cover precision.
 * 05 JAN 2017  22134      amoore      QC Tooltips for Monthly period.
 * 23 JAN 2017  22134      amoore      Save QC values.
 * 17 FEB 2017  28609      amoore      Fix array-list type casting.
 * 24 FEB 2017  27420      amoore      Address warnings in code.
 * 27 FEB 2017  29577      amoore      Cleaner mismatch tooltip for dates.
 * 15 MAR 2017  30162      amoore      Fix logging and exception throwing.
 * 21 MAR 2017  30166      amoore      Integration with CPG workflow.
 * 04 APR 2017  30166      amoore      Implement abort. Adjust data types used. Integration with workflow.
 * 07 APR 2017  30166      amoore      Add auto-CPG constructor.
 * 07 APR 2017  30166      amoore      Update CPG status on display.
 * 10 MAY 2017  33104      amoore      Use new response data.
 * 12 MAY 2017  33104      amoore      Address minor FindBugs.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 22 MAY 2017  33104      amoore      Add loading of wind data.
 * 02 JUN 2017  34777      amoore      Read-only mode if session is past DISPLAY state.
 * 15 JUN 2017  35187      amoore      Handle trace symbol in text box.
 * 16 JUN 2017  35185      amoore      Fix issue where dates may keep appending over and over. Fix issue where
 *                                     date lists were not considered equivalent when they should be.
 * 16 JUN 2017  35185      amoore      Add matched icon (blank). Set matched by default. These changes together
 *                                     ensure consistent element formatting in the dialog.
 * 16 JUN 2017  35182      amoore      Fix more accidental continuous appending of dates.
 * 20 JUN 2017  35324      amoore      Month-day only date component was not saving or providing access to
 *                                     years. New functionality for component exposing a new date field
 *                                     with all date info.
 * 03 JUL 2017  35694      amoore      Alter to take into account new {@link DateSelectionComp} API.
 * 07 JUL 2017  33104      amoore      Extract out MismatchLabel as its own class.
 * 03 AUG 2017  36717      amoore      Minor header text field adjustments.
 * 04 AUG 2017  36707      amoore      Add warning on closing with X.
 * 19 SEP 2017  38124      amoore      Use GC for text control sizes.
 * 20 NOV 2017  41128      amoore      Split into multiple classes.
 * 21 NOV 2017  41180      amoore      CLS and CLA should not deal with MSM values.
 * </pre>
 * 
 * @author amoore
 */
public class DisplayStationPeriodDialog extends ClimateCaveChangeTrackDialog {
    /**
     * Summary section fields.
     */
    /**
     * Station list.
     */
    private List myStationList;

    /**
     * Overall dates of the report.
     */
    private Text myReportDatesTF;

    /**
     * Type of summary report this is.
     */
    private Text myReportSummaryTypeTF;
    /**
     * End summary section fields.
     */

    /**
     * Original period data gathered by month by station.
     */
    protected HashMap<Integer, PeriodData> myMSMPeriodDataByStation = new HashMap<>();

    /**
     * "Other" period data by station. Not necessarily saved.
     */
    protected HashMap<Integer, PeriodData> myOtherPeriodDataByStation = new HashMap<>();

    /**
     * User-saved period data by station. Not necessarily the "Other" values.
     */
    protected HashMap<Integer, PeriodData> mySavedPeriodDataByStation = new HashMap<>();

    /**
     * Current station.
     */
    protected Station myCurrStation = null;

    /**
     * Period desc for this display.
     */
    protected final PeriodDesc myPeriodDesc;

    /**
     * Report data map. Daily DB data.
     */
    protected final HashMap<Integer, ClimatePeriodReportData> myOriginalDataMap;

    /**
     * Stations retrieved from backend.
     */
    private java.util.List<Station> myStations = new ArrayList<>();

    /**
     * Save button.
     */
    private Button mySaveValuesButton;

    /**
     * Save button composite that will hold tooltip, since tooltips do not
     * appear on disabled controls and the save button could get disabled.
     */
    private Composite mySaveValuesButtonComp;

    /**
     * Temperature tab.
     */
    private TemperatureTab myTempTab;

    /**
     * Precipitation tab.
     */
    private PrecipTab myPrecipTab;

    /**
     * Snow tab.
     */
    private SnowTab mySnowTab;

    /**
     * Wind tab.
     */
    private WindTab myWindTab;

    /**
     * Sky and weather tab.
     */
    private SkyAndWeatherTab mySkyAndWeatherTab;

    /**
     * Boolean for whether or not some values are mismatched between daily and
     * monthly.
     */
    private boolean myMSMandDailyMismatched = false;

    /**
     * The previously-selected station index.
     */
    private int myPrevStationSelectionIndex = -1;

    /**
     * Flag for if data is currently being loaded.
     */
    protected boolean myLoadingData = false;

    /**
     * Accept values menu item.
     */
    private MenuItem myAcceptValuesMenuItem;

    /**
     * Abort run menu item.
     */
    private MenuItem myAbortClimateRunMenuItem;

    /**
     * Lower bound for date selections.
     */
    protected final ClimateDate myLowerBoundDate;

    /**
     * Upper bound for date selections.
     */
    protected final ClimateDate myUpperBoundDate;

    /**
     * Set of station IDs for which mismatched Daily DB values have already been
     * approved by the user for overwrite by MSM values, so that users are not
     * repeatedly asked for the same station if they would like the overwrite.
     */
    protected final Set<Integer> myStationIdsMismatchOverwriteApproved = new HashSet<>();

    /**
     * Global configuration, for custom user-defined threshold fields.
     */
    protected ClimateGlobal myGlobals;

    /**
     * True if calculating a monthly period.
     */
    protected final boolean myIsMonthly;

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
     *            CPG session ID.
     * @throws ClimateInvalidParameterException
     * @throws SerializationException
     * @throws VizException
     */
    public DisplayStationPeriodDialog(Shell parentShell, String sessionID)
            throws ClimateInvalidParameterException, SerializationException,
            VizException {
        super(parentShell);

        DisplayClimateResponse periodData = (DisplayClimateResponse) ThriftClient
                .sendRequest(new DisplayClimateRequest(sessionID,
                        System.getProperty("user.name")));

        mySessionID = sessionID;

        ClimateRunPeriodData periodCreatorData = (ClimateRunPeriodData) periodData
                .getReportData();

        // period
        myPeriodDesc = new PeriodDesc(periodCreatorData.getPeriodType(),
                new ClimateDates(periodCreatorData.getBeginDate(),
                        periodCreatorData.getEndDate()));

        // is monthly?
        if (myPeriodDesc.getPeriodType().equals(PeriodType.MONTHLY_NWWS)
                || myPeriodDesc.getPeriodType()
                        .equals(PeriodType.MONTHLY_RAD)) {
            myIsMonthly = true;
        } else {
            myIsMonthly = false;
        }

        // title
        setText("Display Station Climate Values");

        myOriginalDataMap = periodCreatorData.getReportMap();

        for (ClimatePeriodReportData report : myOriginalDataMap.values()) {
            /*
             * Get the all the climate stations from report data.
             */
            myStations.add(report.getStation());
        }

        /*
         * Set upper and lower bound dates for date selection components. Lower
         * bound is the day before the first date of the report to account for
         * 24-hour record guidelines.
         */
        Calendar lowerBoundCal = myPeriodDesc.getDates().getStart()
                .getCalendarFromClimateDate();
        lowerBoundCal.add(Calendar.DAY_OF_MONTH, -1);
        myLowerBoundDate = new ClimateDate(lowerBoundCal);

        myUpperBoundDate = new ClimateDate(myPeriodDesc.getDates().getEnd());

        myGlobals = periodData.getGlobalConfig();

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
        try {
            initializeComponents();
        } catch (ClimateInvalidParameterException e) {
            logger.error("Error constructing dialog due to invalid period type",
                    e);
        }
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
     * 
     * @throws ClimateInvalidParameterException
     */
    private void initializeComponents()
            throws ClimateInvalidParameterException {
        Composite dialogComp = new Composite(shell, SWT.NONE);
        RowLayout dialogLayout = new RowLayout(SWT.VERTICAL);
        dialogLayout.center = true;
        dialogComp.setLayout(dialogLayout);

        createMenu();
        createStationControls(dialogComp);

        // disable saving if incomplete period
        /**
         * Monthly, seasonal, and annual records are only saved to the database
         * when a complete period is run. A monthly report for Jan.1-31 will be
         * saved while a month to date run for Jan. 1-15 will not.<br>
         * <br>
         * 
         * The logic of period-completeness is handled by the PeriodDesc class.
         */
        if (myPeriodDesc.isUseCustom()) {
            StringBuilder saveDisabledTooltip = new StringBuilder(
                    "Cannot save for an incomplete period. Selected period type ");
            switch (myPeriodDesc.getPeriodType()) {
            case MONTHLY_NWWS:
            case MONTHLY_RAD:
                saveDisabledTooltip.append("monthly");
                break;
            case SEASONAL_NWWS:
            case SEASONAL_RAD:
                saveDisabledTooltip.append("seasonal");
                break;
            case ANNUAL_NWWS:
            case ANNUAL_RAD:
                saveDisabledTooltip.append("annual");
                break;
            default:
                logger.error("Unhandled period type ["
                        + myPeriodDesc.getPeriodType() + "].");
                saveDisabledTooltip.append("UNKNOWN");
            }
            saveDisabledTooltip.append(" which is invalid for the dates [");
            saveDisabledTooltip.append(
                    myPeriodDesc.getDates().getStart().toFullDateString());
            saveDisabledTooltip.append("] to [");
            saveDisabledTooltip.append(
                    myPeriodDesc.getDates().getEnd().toFullDateString());
            saveDisabledTooltip.append("].");

            /*
             * Disable save button.
             */
            mySaveValuesButton.setEnabled(false);
            /*
             * Set tooltip on composite holding button rather than button itself
             * so tooltip still appears even if button is disabled.
             */
            mySaveValuesButtonComp
                    .setToolTipText(saveDisabledTooltip.toString());

            /*
             * Disable save and abort menu items.
             */
            myAcceptValuesMenuItem.setEnabled(false);
            myAcceptValuesMenuItem
                    .setToolTipText(saveDisabledTooltip.toString());
            myAbortClimateRunMenuItem.setEnabled(false);
            myAbortClimateRunMenuItem.setToolTipText(
                    "No Climate report is executing for this incomplete period.");
        }
        createTabbedSections(dialogComp);
    }

    /**
     * Create the tabbed section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createTabbedSections(Composite parent) {
        TabFolder tabFolder = new TabFolder(parent, SWT.TOP | SWT.BORDER);

        myTempTab = new TemperatureTab(tabFolder, SWT.NONE, this);

        myPrecipTab = new PrecipTab(tabFolder, SWT.NONE, this);

        mySnowTab = new SnowTab(tabFolder, SWT.NONE, this);

        myWindTab = new WindTab(tabFolder, SWT.NONE, this);

        mySkyAndWeatherTab = new SkyAndWeatherTab(tabFolder, SWT.NONE, this);
    }

    /**
     * Create a combo viewer that displays {@link DataValueOrigin} options.
     * 
     * @param parent
     *            parent composite.
     * @return a new combo viewer.
     */
    protected ComboViewer createDataValueOriginComboViewer(Composite parent) {
        ComboViewer viewer = new ComboViewer(parent, SWT.READ_ONLY);

        viewer.setContentProvider(ArrayContentProvider.getInstance());

        viewer.setLabelProvider(new LabelProvider());

        if (!myPeriodDesc.isUseCustom()
                && (PeriodType.MONTHLY_NWWS.equals(myPeriodDesc.getPeriodType())
                        || PeriodType.MONTHLY_RAD
                                .equals(myPeriodDesc.getPeriodType()))) {
            /*
             * non-custom period, allow all origins (Daily, Monthly, Other)
             */
            viewer.setInput(DataValueOrigin.values());
        } else {
            /*
             * custom period, CLS, or CLA; allow only Daily and Other origins
             */
            viewer.setInput(new DataValueOrigin[] {
                    DataValueOrigin.DAILY_DATABASE, DataValueOrigin.OTHER });
        }

        viewer.setSelection(new StructuredSelection(viewer.getElementAt(0)),
                true);

        return viewer;
    }

    /**
     * Create station controls.
     * 
     * @param parent
     *            parent component.
     * @throws ClimateInvalidParameterException
     */
    private void createStationControls(Composite parent)
            throws ClimateInvalidParameterException {
        Composite stationControlsComp = new Composite(parent, SWT.NONE);
        RowLayout stationControlsLayout = new RowLayout(SWT.HORIZONTAL);
        stationControlsLayout.spacing = 10;
        stationControlsLayout.center = true;
        stationControlsComp.setLayout(stationControlsLayout);

        /*
         * Save button.
         */
        createSaveSection(stationControlsComp);

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
     * Create save controls section.
     * 
     * @param parent
     *            parent composite.
     */
    private void createSaveSection(Composite parent) {
        Composite valueControlsComp = new Composite(parent, SWT.NONE);
        RowLayout valueControlsLayout = new RowLayout(SWT.VERTICAL);
        valueControlsLayout.center = true;
        valueControlsLayout.marginHeight = 10;
        valueControlsComp.setLayout(valueControlsLayout);

        // save values
        mySaveValuesButtonComp = new Composite(valueControlsComp, SWT.NONE);

        GC gc = new GC(mySaveValuesButtonComp);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        RowData saveValuesRD = new RowData(25 * fontWidth, SWT.DEFAULT);
        mySaveValuesButtonComp.setLayoutData(saveValuesRD);
        RowLayout saveValuesButtonCompLayout = new RowLayout(SWT.VERTICAL);
        saveValuesButtonCompLayout.center = true;
        saveValuesButtonCompLayout.marginHeight = 0;
        saveValuesButtonCompLayout.marginWidth = 0;
        mySaveValuesButtonComp.setLayout(saveValuesButtonCompLayout);

        RowData saveValuesButtonRD = new RowData(23 * fontWidth, SWT.DEFAULT);
        mySaveValuesButton = new Button(mySaveValuesButtonComp, SWT.PUSH);
        mySaveValuesButton.setLayoutData(saveValuesButtonRD);
        mySaveValuesButton.setText("Save Station Values");
        mySaveValuesButton
                .setToolTipText("Accept the values for the current station");
        mySaveValuesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    saveValues(false);
                } catch (Exception e) {
                    logger.error("Error saving period data", e);
                }
            }

        });
        mySaveValuesButton.setEnabled(myWriteable);
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
        myStationList = new List(stationListComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        GC gc = new GC(myStationList);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        RowData stationListRD = new RowData(36 * fontWidth, SWT.DEFAULT);

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
     * Save values.
     * 
     * @param programmatic
     *            true if called programmatically (no prompts, do not move on to
     *            the next station), false if called by user.
     * 
     * @throws VizException
     * @throws NumberFormatException
     * @throws ParseException
     */
    private void saveValues(boolean programmatic)
            throws VizException, NumberFormatException, ParseException {
        /*
         * parse data from dialog values, saving to the saved data map. Data is
         * started as the copy from the Climate Creator data map and then has
         * values overwritten with what is in the GUI.
         */
        PeriodData dataToSave = new PeriodData(
                myOriginalDataMap.get(myCurrStation.getInformId()).getData());

        myTempTab.saveValues(dataToSave);

        myPrecipTab.saveValues(dataToSave);

        mySnowTab.saveValues(dataToSave);

        myWindTab.saveValues(dataToSave);

        mySkyAndWeatherTab.saveValues(dataToSave);

        // save data copy to Saved map
        mySavedPeriodDataByStation.put(myCurrStation.getInformId(),
                new PeriodData(dataToSave));

        if (!programmatic) {
            /*
             * Only save to Other if saving due to user call, as Other is
             * reserved for user values.
             */
            /*
             * save data copy to Other map, to ensure at least one Combo Box
             * selection for Origin will have the saved value
             */
            myOtherPeriodDataByStation.put(myCurrStation.getInformId(),
                    new PeriodData(dataToSave));
        }

        changeListener.setChangesUnsaved(false);

        if (!programmatic) {
            /*
             * Only bring up mismatched values and move on to next station due
             * to user call.
             */
            /**
             * Legacy comments:
             * 
             * Hopefully, the MSM and daily database values are equal. In the
             * case that they are not, the offending elements will be displayed
             * in reverse color. AWIPS II migration: flag such elements with an
             * icon instead.
             * 
             * "Do you want to update the daily database with the MSM values?"
             * This warning appears when mismatched values are detected (as
             * described above). Choosing Yes results in Climate replacing the
             * maximum temperature, minimum temperature, and snow depth values
             * in the daily database with the MSM values for the given dates of
             * occurrence (where they do not equal the daily database values).
             * Choosing No does not update the daily database.
             */
            /*
             * For mismatched values only, and do not re-ask on going back to a
             * station for which mismatched values have already been persisted
             */
            if (myMSMandDailyMismatched
                    && !myStationIdsMismatchOverwriteApproved
                            .contains(myCurrStation.getInformId())) {

                int overwriteChoice = messageDialogMSMOverwrite(getShell(),
                        myCurrStation.getInformId());

                if (overwriteChoice == IDialogConstants.YES_ID) {
                    /*
                     * Add the station ID to the tracking list so that this
                     * overwrite question is never asked again for this station.
                     */
                    myStationIdsMismatchOverwriteApproved
                            .add(myCurrStation.getInformId());
                } else if (overwriteChoice == IDialogConstants.YES_TO_ALL_ID) {
                    /*
                     * Add all station IDs to the tracking list for overwrite.
                     */
                    for (Station station : myStations) {
                        int stationID = station.getInformId();
                        /*
                         * Collection is a set, so no duplicates will exist.
                         */
                        myStationIdsMismatchOverwriteApproved.add(stationID);
                    }
                }
            }

            // go to next station in list
            myStationList.setSelection(myStationList.getSelectionIndex()
                    + 1 < myStationList.getItemCount()
                            ? myStationList.getSelectionIndex() + 1 : 0);
            stationSelectionChanged(false);
        }
    }

    /**
     * Prompt user if they would like to overwrite daily data with MSM for some
     * station, or all stations, or do nothing for now.
     * 
     * @param shell
     * @param stationID
     * @return {@link IDialogConstants#YES_ID} for Yes,
     *         {@link IDialogConstants#YES_TO_ALL_ID} for Yes to All stations,
     *         or {@link IDialogConstants#NO_ID} for No.
     */
    private static int messageDialogMSMOverwrite(Shell shell, int stationID) {
        MessageDialog dialog = new MessageDialog(shell,
                "Overwrite Daily with MSM?", null,
                "Do you want to update the daily database with the MSM values?\n"
                        + "This will overwrite values for:\n\nMaximum temperature"
                        + "\nMinimum temperature" + "\nSnow depth"
                        + "\n\nwhere they are not missing from MSM for station ID ["
                        + stationID + "]."
                        + "\nThis overwrite can also be done for all stations.",
                MessageDialog.QUESTION,
                new String[] { IDialogConstants.YES_LABEL,
                        IDialogConstants.YES_TO_ALL_LABEL,
                        IDialogConstants.NO_LABEL },
                0);

        int overwriteChoice = dialog.open();

        return overwriteChoice;
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

        myAbortClimateRunMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        myAbortClimateRunMenuItem.setText("Abort Climate Run");
        myAbortClimateRunMenuItem
                .setToolTipText("Abort this execution in its entirety");
        myAbortClimateRunMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                completed = MessageDialog.openConfirm(getShell(), "Abort?",
                        "Are you sure you wish to abort? "
                                + "Unsaved changes will be lost.");

                if (completed) {
                    /*
                     * abort this climate session in the workflow
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
        myAbortClimateRunMenuItem.setEnabled(myWriteable);

        myAcceptValuesMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        myAcceptValuesMenuItem.setText("Accept Values and Continue");
        myAcceptValuesMenuItem.setToolTipText(
                "Accept values for all stations and continue to the next step of execution.");
        myAcceptValuesMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // confirmation
                completed = MessageDialog.openConfirm(getShell(), "Continue?",
                        "Are you sure you wish to accept all values for the stations?"
                                + " Unsaved changes will be lost, and"
                                + " climate execution will proceed to the next step.");

                if (completed) {
                    HashMap<Integer, ClimatePeriodReportData> savedPeriodReportData = new HashMap<>();
                    for (Entry<Integer, ClimatePeriodReportData> entry : myOriginalDataMap
                            .entrySet()) {
                        ClimatePeriodReportData data = new ClimatePeriodReportData(
                                entry.getValue());
                        if (mySavedPeriodDataByStation
                                .get(entry.getKey()) != null) {
                            // overwrite data with saved equivalent for the
                            // request
                            data.setData(mySavedPeriodDataByStation
                                    .get(entry.getKey()));
                        }
                        savedPeriodReportData.put(entry.getKey(), data);
                    }

                    CompleteDisplayClimateRequest request = new CompleteDisplayClimateRequest(
                            mySessionID,
                            new ClimateRunPeriodData(
                                    myPeriodDesc.getPeriodType(),
                                    myPeriodDesc.getDates().getStart(),
                                    myPeriodDesc.getDates().getEnd(),
                                    savedPeriodReportData),
                            myOriginalDataMap,
                            myStationIdsMismatchOverwriteApproved);
                    try {
                        ThriftClient.sendRequest(request);
                    } catch (VizException e) {
                        logger.error(
                                "Error finalizing data after completion of Display module.",
                                e);
                    }
                    /*
                     * accept entire report and move on to next step in
                     * workflow.
                     */
                    close();
                }
            }
        });
        myAcceptValuesMenuItem.setEnabled(myWriteable);

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
     * Create date section.
     * 
     * @param parent
     *            parent composite.
     * @throws ClimateInvalidParameterException
     */
    private void createDateSection(Composite parent)
            throws ClimateInvalidParameterException {
        /**
         * This GUI is used for monthly, seasonal, and annual climate runs. The
         * type of summary and the dates of inclusion for the run are displayed
         * in the upper right portion of the interface.
         */
        Composite dateComp = new Composite(parent, SWT.NONE);
        RowLayout dateLayout = new RowLayout(SWT.VERTICAL);
        dateLayout.spacing = 5;
        dateLayout.center = true;
        dateComp.setLayout(dateLayout);

        // Date label
        Label dateLbl = new Label(dateComp, SWT.NORMAL);
        dateLbl.setText("Report Period");

        // Date field
        myReportDatesTF = new Text(dateComp,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myReportDatesTF.setEditable(false);

        GC gc = new GC(myReportDatesTF);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        RowData dateAndSummaryRD = new RowData(30 * fontWidth, SWT.DEFAULT);

        myReportDatesTF.setLayoutData(dateAndSummaryRD);

        // set dates
        ClimateDates dates = ClimateDates
                .getClimateDatesFromPeriodDesc(myPeriodDesc);
        myReportDatesTF.setText(dates.getStart().toFullDateString() + " to "
                + dates.getEnd().toFullDateString());

        // Summary type label
        Label summaryTypeLbl = new Label(dateComp, SWT.NORMAL);
        summaryTypeLbl.setText("Type of Summary");

        // Summary type field
        myReportSummaryTypeTF = new Text(dateComp,
                SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        myReportSummaryTypeTF.setEditable(false);
        myReportSummaryTypeTF.setLayoutData(dateAndSummaryRD);

        // set type of summary
        PeriodType periodType = myPeriodDesc.getPeriodType();
        switch (periodType) {
        case ANNUAL_RAD:
        case ANNUAL_NWWS:
            myReportSummaryTypeTF.setText("Annual");
            break;
        case MONTHLY_RAD:
        case MONTHLY_NWWS:
            myReportSummaryTypeTF.setText("Monthly");
            break;
        case SEASONAL_RAD:
        case SEASONAL_NWWS:
            myReportSummaryTypeTF.setText("Seasonal");
            break;
        default:
            throw new ClimateInvalidParameterException(
                    "Unhandled period type: [" + periodType + "]");
        }
    }

    /**
     * Set data for dialog.
     * 
     * Legacy comments:
     * 
     * For monthly runs, data are retrieved/calculated from both the ASOS
     * Monthly Summary Message (MSM) and the existing daily database values. If
     * an element does not display an option menu, it means that the value has
     * been retrieved from the daily database. If an MSM exists, it will be the
     * default value of choice. The daily database value can be chosen, but the
     * "Save Station Values" button has to be selected to override the default.
     * If both are missing, a value can be entered and the option menu displays
     * "Other". Again, the values then have to be saved.
     * 
     * Seasonal and annual values are compiled from the monthly records.
     * 
     * Migration comments:
     * 
     * Seasonal and annual will still have the Daily Database (Climate Creator)
     * values available; the Legacy documentation is misleading as even in
     * Legacy Climate this seems to be the case.
     */
    private void loadData() {
        // set loading flag
        myLoadingData = true;
        changeListener.setIgnoreChanges(true);

        // clear data first
        clearValues();

        // disable save controls unless data is loaded
        mySaveValuesButton.setEnabled(false);

        // see what the selected station is
        int selectedStationIndex = myStationList.getSelectionIndex();

        if (selectedStationIndex >= 0
                && selectedStationIndex < myStations.size()) {
            myCurrStation = myStations.get(selectedStationIndex);

            // if station data already exists in a save map, display saved data
            // and no need to re-query
            if (mySavedPeriodDataByStation
                    .containsKey(myCurrStation.getInformId())) {
                loadSavedData(mySavedPeriodDataByStation
                        .get(myCurrStation.getInformId()));
            } else {
                loadUnsavedData(myCurrStation);
            }
            // re-enable save button
            if (!myPeriodDesc.isUseCustom()) {
                mySaveValuesButton.setEnabled(myWriteable && true);
            }
            changeListener.setChangesUnsaved(false);
            changeListener.setIgnoreChanges(false);
        } else {
            logger.debug("Station Index [" + selectedStationIndex
                    + "] is not valid. No station data loaded.");
            myCurrStation = null;
        }

        // set loading flag
        myLoadingData = false;
    }

    /**
     * Load data for the given station.
     * 
     * @param station
     */
    private void loadUnsavedData(Station station) {
        /*
         * station data has not yet been saved; need to query for it
         */
        /*
         * also save a missing values object in Other data map
         */
        PeriodData otherData = PeriodData.getMissingPeriodData();
        otherData.setInformId(station.getInformId());
        myOtherPeriodDataByStation.put(station.getInformId(), otherData);
        try {
            switch (myPeriodDesc.getPeriodType()) {
            case MONTHLY_RAD:
            case MONTHLY_NWWS:
                // monthly request
                loadMonthlyASOSData();

                break;
            case SEASONAL_RAD:
            case SEASONAL_NWWS:
            case ANNUAL_RAD:
            case ANNUAL_NWWS:
                // no need to do anything; MSM used only in Monthly
                break;
            default:
                throw new VizException("Unhandled period type ["
                        + myPeriodDesc.getPeriodType() + "]");
            }

            /*
             * also need daily data, from Climate Creator report, to be compared
             * with MSM data (MSM preferred).
             */
            displayDailyBuildData(
                    myMSMPeriodDataByStation.get(station.getInformId()),
                    myOriginalDataMap.get(station.getInformId()).getData());

            try {
                /*
                 * save display after all values are loaded as these are the
                 * preferred values. This allows users to move on to the next
                 * station without needing to review and save each one if they
                 * don't want to.
                 */
                saveValues(true);
            } catch (NumberFormatException e) {
                logger.error(
                        "Could not auto-save data for Climate Period Display dialog, with station ID ["
                                + station.getInformId() + "] and date ["
                                + myPeriodDesc.toString() + "].",
                        e);
            } catch (ParseException e) {
                logger.error(
                        "Could not auto-save data for Climate Period Display dialog, with station ID ["
                                + station.getInformId() + "] and date ["
                                + myPeriodDesc.toString() + "].",
                        e);
            } catch (VizException e) {
                logger.error(
                        "Could not auto-save data for Climate Period Display dialog, with station ID ["
                                + station.getInformId() + "] and date ["
                                + myPeriodDesc.toString() + "].",
                        e);
            }
        } catch (VizException e) {
            String message = "Could not retrieve data for Climate Period Display dialog, with station ID ["
                    + station.getInformId() + "] and date ["
                    + myPeriodDesc.toString() + "].";
            logger.error(message, e);
            MessageDialog.openError(getShell(), "Data Retrieval Error",
                    message);
        }
    }

    /**
     * Load the given saved data into the view.
     * 
     * @param iSavedPeriodData
     *            the saved data.
     */
    private void loadSavedData(PeriodData iSavedPeriodData) {
        // display the saved values, comparing against data saved in
        // DailyDB, MSM, and Other maps to determine which combo boxes to toggle
        // to (if applicable)
        PeriodData msmPeriodData = myMSMPeriodDataByStation
                .get(myCurrStation.getInformId());
        PeriodData dailyPeriodData = myOriginalDataMap
                .get(myCurrStation.getInformId()).getData();

        // set mismatch icon for tab items
        boolean tempTabMismatch = myTempTab.loadSavedData(iSavedPeriodData,
                msmPeriodData, dailyPeriodData);

        boolean precipTabMismatch = myPrecipTab.loadSavedData(iSavedPeriodData,
                msmPeriodData, dailyPeriodData);

        boolean snowTabMismatch = mySnowTab.loadSavedData(iSavedPeriodData,
                msmPeriodData, dailyPeriodData);

        boolean windTabMismatch = myWindTab.loadSavedData(iSavedPeriodData,
                msmPeriodData, dailyPeriodData);

        boolean skyAndWeatherMismatch = mySkyAndWeatherTab.loadSavedData(
                iSavedPeriodData, msmPeriodData, dailyPeriodData);

        // any mismatching at all?
        myMSMandDailyMismatched = tempTabMismatch || precipTabMismatch
                || snowTabMismatch || windTabMismatch || skyAndWeatherMismatch;
    }

    /**
     * Display daily build data (Daily DB) from the Climate Creator data map.
     * Compare with given monthly ASOS data.
     * 
     * @param iMonthlyAsosData
     * @param iDailyBuildData
     */
    private void displayDailyBuildData(PeriodData iMonthlyAsosData,
            PeriodData iDailyBuildData) {
        // fill out daily data, some handled by combo box listeners
        // some data may already be filled out from monthly ASOS, which is given
        // preference
        // also compare dates/times of values when available, not just values

        // set mismatch icon for tab items
        boolean tempTabMismatch = myTempTab
                .displayDailyBuildData(iMonthlyAsosData, iDailyBuildData);

        boolean precipTabMismatch = myPrecipTab
                .displayDailyBuildData(iMonthlyAsosData, iDailyBuildData);

        boolean snowTabMismatch = mySnowTab
                .displayDailyBuildData(iMonthlyAsosData, iDailyBuildData);

        boolean windTabMismatch = myWindTab
                .displayDailyBuildData(iMonthlyAsosData, iDailyBuildData);

        boolean skyAndWeatherMismatch = mySkyAndWeatherTab
                .displayDailyBuildData(iMonthlyAsosData, iDailyBuildData);

        // any mismatching at all?
        myMSMandDailyMismatched = tempTabMismatch || precipTabMismatch
                || snowTabMismatch || windTabMismatch || skyAndWeatherMismatch;
    }

    /**
     * Retrieve and save monthly ASOS data for a single month. Also display the
     * data.
     * 
     * @throws VizException
     */
    private void loadMonthlyASOSData() throws VizException {
        requestMonthlyASOSData();

        displayMonthlyASOSData();
    }

    /**
     * Request and save monthly ASOS data for a single month.
     * 
     * @throws VizException
     */
    private void requestMonthlyASOSData() throws VizException {
        logger.debug("Sending monthly ASOS request for ["
                + myPeriodDesc.getMonth() + "-" + myPeriodDesc.getYear()
                + "] and Station [" + myCurrStation.getInformId() + "]");

        DisplayMonthlyASOSClimateServiceRequest monthlyASOSRequest = new DisplayMonthlyASOSClimateServiceRequest(
                myCurrStation.getIcaoId(), myPeriodDesc.getMonth(),
                myPeriodDesc.getYear());

        PeriodData monthlyASOSData = (PeriodData) ThriftClient
                .sendRequest(monthlyASOSRequest);

        // save values to map
        myMSMPeriodDataByStation.put(myCurrStation.getInformId(),
                monthlyASOSData);
    }

    /**
     * Display monthly ASOS data.
     */
    private void displayMonthlyASOSData() {
        // set data value origins to MSM; listeners will automatically load data
        myTempTab.displayMonthlyASOSData();

        myPrecipTab.displayMonthlyASOSData();

        mySnowTab.displayMonthlyASOSData();

        myWindTab.displayMonthlyASOSData();

        mySkyAndWeatherTab.displayMonthlyASOSData();
    }

    /**
     * Clear all text fields to missing values.
     */
    private void clearValues() {
        myTempTab.clearValues();
        myPrecipTab.clearValues();
        mySnowTab.clearValues();
        myWindTab.clearValues();
        mySkyAndWeatherTab.clearValues();
    }

    /**
     * Station list selection changed.
     * 
     * @param warn
     *            true to provide warning to user.
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
     * Get the change listener for the dialog; for use by individual tab
     * classes.
     * 
     * @return changeListener
     */
    protected UnsavedChangesListener getChangeListener() {
        return changeListener;
    }
}

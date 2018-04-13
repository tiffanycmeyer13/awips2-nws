/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.qualitycontrol.dialog;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodDesc;
import gov.noaa.nws.ocp.common.dataplugin.climate.RecentDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.SeasonType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.FindDateRequest;
import gov.noaa.nws.ocp.viz.climate.qualitycontrol.QCDataComposite;
import gov.noaa.nws.ocp.viz.common.climate.comp.DateSelectionComp;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * Dialog for Quality Control Climate Database
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2016  20636      wpaintsil   Initial creation
 * Aug 31, 2016  20636      wpaintsil   Use DateSelectionComp. Add station List.
 * Sep 02, 2016  20636      wpaintsil   Daily back end implementation
 * Sep 08, 2016  20636      wpaintsil   Press enter in the date field to load data.
 * Sep 15, 2016  20636      wpaintsil   Use UnsavedChangesListener. Period front end implementation.
 * Oct 04, 2016  20636      wpaintsil   Period back end: load data.
 * Oct 11, 2016  20636      wpaintsil   Period back end: save data.
 * Oct 27, 2016  22135      wpaintsil   Extract QDataComposite to separate file
 * Nov 17, 2016  20636      wpaintsil   Change year Spinner to a Combo.
 * Dec 02, 2016  20636      wpaintsil   Add confirm dialog to period date combo selection
 * 27 DEC 2016   22450      amoore      Wind direction precision.
 * 28 DEC 2016   22777      amoore      Cancel changes button width fix.
 * 28 DEC 2016   22784      amoore      Sky cover precision. Legacy labeling was misleading.
 * Jan 03, 2017  27680      wpaintsil   Move to next station after saving.
 * 03 JAN 2017   22134      amoore      Unnecessary parameter passing.
 * 15 MAR 2017   30162      amoore      Fix exception throwing.
 * 13 APR 2017   33104      amoore      Address comments from review.
 * 15 MAY 2017   33104      amoore      Move SEASON_END_MONTH constant here.
 * 16 MAY 2017   33104      amoore      FindBugs issues.
 * 14 JUN 2017   35181      wpaintsil   Format missing slp to 2 decimal places.
 * 16 JUN 2017   35181      amoore      Undo previous change; fix Double validation listener instead.
 * 20 JUN 2017   35324      amoore      Month-day only date component was not saving or providing access to
 *                                      years. New functionality for component exposing a new date field
 *                                      with all date info.
 * 03 JUL 2017   35694      amoore      Alter to take into account new {@link DateSelectionComp} API.
 * 27 JUL 2017   33104      amoore      Do not use effectively final functionality, for 1.7 build.
 * 03 AUG 2017   36644      amoore      Disable "Set Missing" in date selection.
 * 04 AUG 2017   36647      amoore      For Daily date selection, add arrow buttons/hot keys for
 *                                      adjusting month and day.
 * 04 AUG 2017   33104      amoore      Remove daily arrow key listener on disposal, otherwise it still
 *                                      exists and is listening.
 * 04 AUG 2017   33104      amoore      Only process daily arrow key adjustments if QC is the active shell.
 * 04 AUG 2017   33104      amoore      Make header cleaner.
 * 19 SEP 2017   38124      amoore      Use GC for text control sizes.
 * 12 OCT 2017   39149      wpaintsil   Allow for the editing/saving of dates/periods with empty data.
 * </pre>
 * 
 * @author wpaintsil
 */
public class QCDialog extends ClimateCaveChangeTrackDialog {

    /**
     * Map an index (0-3 : winter to fall) representing a season, to an int
     * representing the end month(1-12) of that season. E.g. the end month for
     * Winter(index 0) is Feb(2), so SEASON_END_MONTH[0] = 2;
     */
    private static final int[] SEASON_END_MONTH = new int[] {
            Calendar.FEBRUARY + 1, Calendar.MAY + 1, Calendar.AUGUST + 1,
            Calendar.NOVEMBER + 1 };// Calendar type months start at 0

    protected Font boldFont;

    protected Combo monthCombo;

    protected Combo yearCombo;

    protected QCDataComposite currentMainDataSection;

    protected Composite dateSection;

    protected DateSelectionComp dailyDateSelector;

    protected ClimateDate dailyDate;

    protected ClimateDate monthlyDate;

    protected ClimateDate seasonalDate;

    protected int annualYear;

    protected DailyClimateData dailyData;

    protected ClimateDayNorm dailyRecords;

    protected PeriodData periodData;

    protected PeriodDesc periodDesc;

    protected String dataType;

    protected java.util.List<Station> stations;

    protected List stationNames;

    protected Button saveValuesButton;

    protected Button cancelChangesButton;

    private int prevStationSelectionIndex = 0;

    protected int currentPeriodTabIndex = 0;

    private static final String DAILY_SELECTION = "Daily";

    protected static final String MONTHLY_SELECTION = "Monthly";

    protected static final String SEASONAL_SELECTION = "Seasonal";

    protected static final String ANNUAL_SELECTION = "Annual";

    private static final int YEAR_START = 1800;

    /**
     * Parameter for String.format to specify a float with 2 decimal places.
     */
    protected static final String FLOAT_TWO_DECIMALS = "%.2f";

    protected ClimateGlobal climateGlobals;

    /**
     * Daily date adjustment buttons.
     */
    private Button myUpDailyDateButton;

    private Button myLeftDailyDateButton;

    private Button myRightDailyDateButton;

    private Button myDownDailyDateButton;

    /**
     * Collection of listeners.
     */
    /**
     * Validation listeners..
     */
    protected final ClimateTextListeners displayListeners = new ClimateTextListeners();

    /**
     * Daily date selection key listener.
     */
    protected final Listener myDailyDateKeyListener = new Listener() {

        @Override
        public void handleEvent(Event event) {
            /*
             * ensure this is the active shell (listener is on the display)
             */
            if (getDisplay().getActiveShell() == getShell()) {
                // ensure on Daily display
                if (DAILY_SELECTION.equals(dataType)) {
                    // check that alt is pressed
                    if ((event.stateMask & SWT.ALT) != 0) {
                        switch (event.keyCode) {
                        case SWT.ARROW_UP:
                            myUpDailyDateButton.notifyListeners(SWT.Selection,
                                    new Event());
                            break;
                        case SWT.ARROW_DOWN:
                            myDownDailyDateButton.notifyListeners(SWT.Selection,
                                    new Event());
                            break;
                        case SWT.ARROW_LEFT:
                            myLeftDailyDateButton.notifyListeners(SWT.Selection,
                                    new Event());
                            break;
                        case SWT.ARROW_RIGHT:
                            myRightDailyDateButton.notifyListeners(
                                    SWT.Selection, new Event());
                            break;
                        }
                    }
                }
            }
        }
    };

    @SuppressWarnings("unchecked")
    public QCDialog(Shell parentShell) {
        super(parentShell);

        setText("Edit Climatological Data");

        // get stations
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        try {
            stations = (java.util.List<Station>) ThriftClient
                    .sendRequest(request);
        } catch (VizException e) {
            logger.error(
                    "Could not retrieve stations for Climate Daily Display dialog",
                    e);
        }

        // get global preferences
        ClimateRequest globalsRequest = new ClimateRequest();
        globalsRequest.setRequestType(RequestType.GET_GLOBAL);
        try {
            climateGlobals = (ClimateGlobal) ThriftClient
                    .sendRequest(globalsRequest);
            // the service can return nulls
            if (climateGlobals == null) {
                climateGlobals = ClimateGlobal.getMissingClimateGlobal();
            }

        } catch (VizException e) {
            climateGlobals = ClimateGlobal.getMissingClimateGlobal();
            logger.error("Failed to read preferences.", e);
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        FontData fontData = getShell().getFont().getFontData()[0];
        boldFont = new Font(getDisplay(), new FontData(fontData.getName(),
                fontData.getHeight(), SWT.BOLD));

        dataType = DAILY_SELECTION;

        createMenus(shell);
        createDataTypeSection(shell);

        Label separator1 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        try {
            initDates();
        } catch (ClimateInvalidParameterException e1) {
            logger.error("Error constructing dates section of dialog.", e1);
        }

        createControls(shell);

        Label separator2 = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        currentMainDataSection = new DailySection(this, shell, SWT.NONE);

        try {
            currentMainDataSection.loadData();
        } catch (ClimateException e1) {
            logger.error("Error loading main data section for dialog.", e1);
        }

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (boldFont != null) {
                    boldFont.dispose();
                }
                getDisplay().removeFilter(SWT.KeyDown, myDailyDateKeyListener);
            }
        });

        // add daily date key listener
        getDisplay().addFilter(SWT.KeyDown, myDailyDateKeyListener);
    }

    private void createMenus(final Shell shell) {
        Menu menuBar = new Menu(shell, SWT.BAR);

        MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuHeader.setText("File");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileMenuHeader.setMenu(fileMenu);

        MenuItem closeMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        closeMenuItem.setText("Close");
        closeMenuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        MenuItem helpMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuItem.setText("&Help");

        Menu helpMenu = new Menu(menuBar);
        helpMenuItem.setMenu(helpMenu);

        // Handbook menu item
        MenuItem aboutMI = new MenuItem(helpMenu, SWT.NONE);
        aboutMI.setText("Handbook");
        aboutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("edit_data.html");
            }
        });

        shell.setMenuBar(menuBar);
    }

    private void createDataTypeSection(Composite parent) {
        Composite dataTypeComp = new Composite(parent, SWT.NONE);
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.spacing = 10;
        layout.center = true;
        dataTypeComp.setLayout(layout);

        Label dataTypeLabel = new Label(dataTypeComp, SWT.NORMAL);
        dataTypeLabel.setText("Data Type");

        final Combo dataTypeCombo = new Combo(dataTypeComp,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        dataTypeCombo.setItems(new String[] { DAILY_SELECTION,
                MONTHLY_SELECTION, SEASONAL_SELECTION, ANNUAL_SELECTION });
        dataTypeCombo.select(0);

        // Change dataSection Composite depending on dataType Selection
        dataTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!dataType.equals(dataTypeCombo.getText())) {
                    dataType = dataTypeCombo.getText();

                    Composite dateParent = dateSection.getParent();
                    dateSection.dispose();
                    dateSection = createDateSection(dateParent);

                    try {
                        refreshMainDataSection();
                    } catch (ClimateException e1) {
                        logger.error("Error refreshing main section of dialog.",
                                e1);
                    }

                }
                /*
                 * Take focus away from the data type combo box so users can use
                 * arrow hot keys without opening the combo box
                 */
                stationNames.setFocus();
            }
        });
    }

    /**
     * Dispose and reload currentMainDataSection.
     * 
     * @throws ClimateException
     */
    protected void refreshMainDataSection() throws ClimateException {
        Composite dataParent = currentMainDataSection.getParent();
        currentMainDataSection.dispose();

        if (dataType.equals(DAILY_SELECTION)) {
            currentMainDataSection = new DailySection(this, dataParent,
                    SWT.NONE);
        } else {
            currentMainDataSection = new PeriodSection(this, dataParent,
                    SWT.NONE);
        }

        dataParent.layout(true, true);
        dataParent.pack(true);

        currentMainDataSection.loadData();
    }

    /**
     * Create station, button, and date controls.
     * 
     * @param parent
     */
    private void createControls(Composite parent) {
        Composite stationComp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        stationComp.setLayout(layout);

        createStationListSection(stationComp);
        createButtonSection(stationComp);

        /*
         * Take focus away from the data type combo box so users can use arrow
         * hot keys without opening the combo box
         */
        stationNames.setFocus();
    }

    private void createButtonSection(Composite parent) {
        Composite buttonControlsComp = new Composite(parent, SWT.NONE);
        buttonControlsComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));
        RowLayout buttonControlsLayout = new RowLayout(SWT.VERTICAL);
        buttonControlsLayout.spacing = 40;
        buttonControlsLayout.center = false;
        buttonControlsComp.setLayout(buttonControlsLayout);

        Composite topComp = new Composite(buttonControlsComp, SWT.NONE);
        RowLayout topLayout = new RowLayout(SWT.HORIZONTAL);
        topLayout.spacing = 15;
        topLayout.center = true;
        topComp.setLayout(topLayout);

        saveValuesButton = new Button(topComp, SWT.PUSH);

        saveValuesButton.setText("Save Station Values");
        saveValuesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                confirmSave();
            }

        });

        cancelChangesButton = new Button(topComp, SWT.PUSH);
        cancelChangesButton.setText("Cancel Station Changes");
        cancelChangesButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    currentMainDataSection.loadData();
                } catch (ClimateException e) {
                    logger.error("Error loading main data section for dialog.",
                            e);
                }
            }
        });

        dateSection = createDateSection(parent);

    }

    protected void confirmSave() {
        boolean load = true;

        String datesConfirmString = currentMainDataSection instanceof DailySection
                ? "date " + dailyDate.toFullDateString()
                : "dates ["
                        + periodDesc.getDates().getStart().toFullDateString()
                        + " - "
                        + periodDesc.getDates().getEnd().toFullDateString()
                        + "]?";

        load = MessageDialog.openConfirm(getShell(), "Save this data?",
                "Are you sure you wish to save data for "
                        + stationNames.getItem(stationNames.getSelectionIndex())
                        + " and " + datesConfirmString + "?");

        if (load) {
            currentMainDataSection.saveData();
            // go to next station in list
            stationNames.setSelection(stationNames.getSelectionIndex()
                    + 1 < stationNames.getItemCount()
                            ? stationNames.getSelectionIndex() + 1 : 0);
            try {
                stationSelectionChanged(false);
            } catch (ClimateException e) {
                logger.error("Error handling station change.", e);
            }
        }

    }

    /**
     * Get the most recent dates for stored data to show for the first station
     * by default.
     * 
     * @throws ClimateInvalidParameterException
     */
    private void initDates() throws ClimateInvalidParameterException {
        RecentDates recentDates = RecentDates.getMissingRecentDates();
        if (!stations.isEmpty()) {
            FindDateRequest request = new FindDateRequest(
                    stations.get(0).getInformId());
            try {
                recentDates = (RecentDates) ThriftClient.sendRequest(request);
            } catch (VizException e) {

                logger.error("Could not get recent dates.", e);
            }

        }
        dailyDate = recentDates.getDailyDate();
        monthlyDate = recentDates.getMonthDate();
        seasonalDate = recentDates.getSeasonDate();
        annualYear = recentDates.getAnnualDate().getYear();

        Calendar yesterday = TimeUtil.newCalendar();
        yesterday.add(Calendar.DATE, -1);

        // If the dates are somehow missing, set them to yesterday, last month,
        // etc.
        if (dailyDate.isMissing()) {
            dailyDate = new ClimateDate(yesterday.getTime());
        }

        // Shift to today, in case it's the 1st day of the month/year, to get
        // last year/month.
        yesterday.add(Calendar.DATE, 1);

        // get last month
        if (monthlyDate.isMissing()) {
            monthlyDate = new ClimateDate(1, yesterday.get(Calendar.MONTH),
                    yesterday.get(Calendar.YEAR));
        }

        // get previous season
        if (seasonalDate.isMissing()) {
            seasonalDate = ClimateDates.getPreviousSeasonDates().getEnd();
        }

        // get last year
        if (annualYear == ClimateDate.getMissingClimateDate().getYear()) {
            annualYear = yesterday.get(Calendar.YEAR) - 1;// previous year
        }
    }

    protected Composite createDateSection(Composite parent) {
        Composite dateComposite = new Composite(parent, SWT.NONE);
        dateComposite.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        RowLayout dateLayout = new RowLayout(SWT.HORIZONTAL);
        dateLayout.spacing = 5;
        dateLayout.center = true;
        dateComposite.setLayout(dateLayout);

        Label dateLabel = new Label(dateComposite, SWT.NORMAL);

        switch (dataType) {
        case DAILY_SELECTION:
            dateLabel.setText("Date");
            createDailyDateComp(dateComposite);
            break;
        case MONTHLY_SELECTION:
            dateLabel.setText("Month");
            createMonthlyDateComp(dateComposite);
            break;
        case SEASONAL_SELECTION:
            dateLabel.setText("End Month\nof Season");
            createSeasonalDateComp(dateComposite);
            break;
        case ANNUAL_SELECTION:
            dateLabel.setText("Year");
            createAnnualDateComp(dateComposite);
            break;
        }

        return dateComposite;
    }

    private void createDailyDateComp(Composite parent) {
        Composite dailyDateComp = new Composite(parent, SWT.NONE);
        GridLayout dailyDateCompGL = new GridLayout(3, false);
        dailyDateComp.setLayout(dailyDateCompGL);

        // up/increase month button, a row to itself
        GridData upGD = new GridData(SWT.CENTER, SWT.CENTER, false, true, 3, 1);
        myUpDailyDateButton = new Button(dailyDateComp, SWT.ARROW | SWT.UP);
        myUpDailyDateButton.setToolTipText("Next month (ALT + UP)");
        myUpDailyDateButton.setLayoutData(upGD);
        myUpDailyDateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Calendar currDate = dailyDateSelector.getDate()
                        .getCalendarFromClimateDate();
                currDate.add(Calendar.MONTH, 1);
                dailyDateSelector.setDate(new ClimateDate(currDate));
            }

        });

        // left/decrease day button
        GridData leftGD = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        myLeftDailyDateButton = new Button(dailyDateComp, SWT.ARROW | SWT.LEFT);
        myLeftDailyDateButton.setToolTipText("Previous day (ALT + LEFT)");
        myLeftDailyDateButton.setLayoutData(leftGD);
        myLeftDailyDateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Calendar currDate = dailyDateSelector.getDate()
                        .getCalendarFromClimateDate();
                currDate.add(Calendar.DATE, -1);
                dailyDateSelector.setDate(new ClimateDate(currDate));
            }

        });

        // date selector
        Calendar cal = TimeUtil.newCalendar();
        cal.add(Calendar.YEAR, 1);
        // set the date selector upperbound to a year after the current date.
        dailyDateSelector = new DateSelectionComp(dailyDateComp, true, SWT.NONE,
                dailyDate, null, new ClimateDate(cal), true);
        dailyDateSelector.setEditable(false);

        // Load data when selecting from the calendar widget.
        dailyDateSelector.addListener(SWT.Modify, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    dateChanged(true);
                } catch (ClimateException e1) {
                    logger.error("Error handling date change.", e1);
                }
            }

        });

        // right/increase day button
        GridData rightGD = new GridData(SWT.CENTER, SWT.CENTER, false, true);
        myRightDailyDateButton = new Button(dailyDateComp,
                SWT.ARROW | SWT.RIGHT);
        myRightDailyDateButton.setToolTipText("Next day (ALT + RIGHT)");
        myRightDailyDateButton.setLayoutData(rightGD);
        myRightDailyDateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Calendar currDate = dailyDateSelector.getDate()
                        .getCalendarFromClimateDate();
                currDate.add(Calendar.DATE, 1);
                dailyDateSelector.setDate(new ClimateDate(currDate));
            }

        });

        // down/decrease month button, a row to itself
        GridData downGD = new GridData(SWT.CENTER, SWT.CENTER, false, true, 3,
                1);
        myDownDailyDateButton = new Button(dailyDateComp, SWT.ARROW | SWT.DOWN);
        myDownDailyDateButton.setToolTipText("Previous month (ALT + DOWN)");
        myDownDailyDateButton.setLayoutData(downGD);
        myDownDailyDateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Calendar currDate = dailyDateSelector.getDate()
                        .getCalendarFromClimateDate();
                currDate.add(Calendar.MONTH, -1);
                dailyDateSelector.setDate(new ClimateDate(currDate));
            }

        });
    }

    private void createMonthlyDateComp(Composite parent) {
        Calendar today = TimeUtil.newCalendar();

        monthCombo = new Combo(parent,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        yearCombo = new Combo(parent,
                SWT.VERTICAL | SWT.BORDER | SWT.READ_ONLY);

        final int maxYear = today.get(Calendar.YEAR) + 1;
        // Maximum is the current year + 1 to account for timezones
        // potentially shifting current data to the next year
        // earliest records are around 1850
        for (int i = maxYear; i >= YEAR_START; i--) {
            yearCombo.add(String.valueOf(i));
        }

        String[] months = DateFormatSymbols.getInstance().getShortMonths();

        for (int i = 0; i < 12; i++) {
            monthCombo.add(months[i]);
        }

        // selection index starts at 0
        monthCombo.select(monthlyDate.getMon() - 1);
        monthCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String currentStation = stationNames
                        .getItem(stationNames.getSelectionIndex());
                int selectedMonth = monthCombo.getSelectionIndex() + 1;
                if (dateChanged(true, selectedMonth != monthlyDate.getMon(),
                        "Are you sure you wish to load " + currentStation
                                + " for the month of "
                                + String.format("%02d", selectedMonth) + "-"
                                + monthlyDate.getYear()
                                + "? Unsaved changes to " + currentStation
                                + " for the month of "
                                + String.format("%02d", monthlyDate.getMon())
                                + "-" + monthlyDate.getYear()
                                + " will be lost.")) {

                    monthlyDate.setMon(selectedMonth);
                    // refresh composite to reset the day-only date
                    // pickers for the appropriate month
                    try {
                        refreshMainDataSection();
                    } catch (ClimateException e1) {
                        logger.error(
                                "Error freshing main data section of dialog.",
                                e1);
                    }
                } else {
                    monthCombo.select(monthlyDate.getMon() - 1);
                }

            }

        });

        yearCombo.select(maxYear - monthlyDate.getYear());
        yearCombo.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {

                String currentStation = stationNames
                        .getItem(stationNames.getSelectionIndex());
                int selectedYear = Integer.parseInt(yearCombo.getText());
                if (dateChanged(true, selectedYear != annualYear,
                        "Are you sure you wish to load " + currentStation
                                + " for the month of "
                                + String.format("%02d", monthlyDate.getMon())
                                + "-" + selectedYear + "? Unsaved changes to "
                                + currentStation + " for the month of "
                                + String.format("%02d", monthlyDate.getMon())
                                + "-" + monthlyDate.getYear()
                                + " will be lost.")) {
                    monthlyDate.setYear(selectedYear);
                    // refresh composite to reset the day-only date
                    // pickers for the appropriate year
                    try {
                        refreshMainDataSection();
                    } catch (ClimateException e1) {
                        logger.error(
                                "Error freshing main data section of dialog.",
                                e1);
                    }
                } else {
                    yearCombo.select(maxYear - monthlyDate.getYear());
                }

            }

        });
    }

    private void createSeasonalDateComp(Composite parent) {
        Calendar today = TimeUtil.newCalendar();

        monthCombo = new Combo(parent,
                SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
        yearCombo = new Combo(parent,
                SWT.VERTICAL | SWT.BORDER | SWT.READ_ONLY);

        final int maxYear = today.get(Calendar.YEAR) + 1;
        // Maximum is the current year + 1 to account for timezones
        // potentially shifting current data to the next year
        // earliest records are around 1850
        for (int i = maxYear; i >= YEAR_START; i--) {
            yearCombo.add(String.valueOf(i));
        }

        String[] months = DateFormatSymbols.getInstance().getShortMonths();

        for (int i = 1; i < 12; i += 3) {
            monthCombo.add(months[i]);
        }

        int monthSelection = 0;
        try {
            monthSelection = SeasonType
                    .getSeasonTypeFromMonth(seasonalDate.getMon()).getIndex();
        } catch (ClimateInvalidParameterException e) {
            logger.error("Error: " + e.getMessage(), e);
        }
        monthCombo.select(monthSelection);

        final int lastMonthSelection = monthSelection;

        monthCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                String currentStation = stationNames
                        .getItem(stationNames.getSelectionIndex());
                int selectedMonth = SEASON_END_MONTH[monthCombo
                        .getSelectionIndex()];
                if (dateChanged(true, selectedMonth != seasonalDate.getMon(),
                        "Are you sure you wish to load " + currentStation
                                + " for the season ending in the month of "
                                + String.format("%02d", selectedMonth) + "-"
                                + seasonalDate.getYear()
                                + "? Unsaved changes to " + currentStation
                                + " for the season ending in the month of "
                                + String.format("%02d", seasonalDate.getMon())
                                + "-" + seasonalDate.getYear()
                                + " will be lost.")) {
                    seasonalDate.setMon(selectedMonth);
                    try {
                        currentMainDataSection.loadData();
                    } catch (ClimateException e1) {
                        logger.error(
                                "Error loading main data section for dialog.",
                                e1);
                    }
                } else {
                    monthCombo.select(lastMonthSelection);
                }

            }

        });

        yearCombo.select(maxYear - seasonalDate.getYear());

        yearCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                String currentStation = stationNames
                        .getItem(stationNames.getSelectionIndex());
                int selectedYear = Integer.parseInt(yearCombo.getText());
                if (dateChanged(true, selectedYear != annualYear,
                        "Are you sure you wish to load " + currentStation
                                + " for the season ending in the month of "
                                + String.format("%02d", seasonalDate.getMon())
                                + "-" + selectedYear + "? Unsaved changes to "
                                + currentStation
                                + " for the season ending in the month of "
                                + String.format("%02d", seasonalDate.getMon())
                                + "-" + seasonalDate.getYear()
                                + " will be lost.")) {
                    seasonalDate.setYear(selectedYear);
                    try {
                        currentMainDataSection.loadData();
                    } catch (ClimateException e1) {
                        logger.error(
                                "Error loading main data section for dialog.",
                                e1);
                    }
                } else {
                    yearCombo.select(maxYear - seasonalDate.getYear());
                }
            }

        });
    }

    private void createAnnualDateComp(Composite parent) {

        Calendar today = TimeUtil.newCalendar();
        yearCombo = new Combo(parent,
                SWT.VERTICAL | SWT.BORDER | SWT.READ_ONLY);

        final int maxYear = today.get(Calendar.YEAR) + 1;
        // Maximum is the current year + 1 to account for timezones
        // potentially shifting current data to the next year
        // earliest records are around 1850
        for (int i = maxYear; i >= YEAR_START; i--) {
            yearCombo.add(String.valueOf(i));
        }
        yearCombo.select(maxYear - annualYear);
        yearCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String currentStation = stationNames
                        .getItem(stationNames.getSelectionIndex());
                int selectedYear = Integer.parseInt(yearCombo.getText());
                if (dateChanged(true, selectedYear != annualYear,
                        "Are you sure you wish to load " + currentStation
                                + " for the year " + selectedYear
                                + "? Unsaved changes to " + currentStation
                                + " for the year " + annualYear
                                + " will be lost.")) {
                    annualYear = maxYear - yearCombo.getSelectionIndex();
                    try {
                        currentMainDataSection.loadData();
                    } catch (ClimateException e1) {
                        logger.error(
                                "Error loading main data section for dialog.",
                                e1);
                    }
                } else {
                    yearCombo.select(maxYear - annualYear);
                }

            }

        });
    }

    /**
     * Use a confirmation pop-up for daily date changing
     * 
     * @param warn
     *            true if there should be a confirmation, false otherwise.
     *            Confirmation only appears if both this flag is true and there
     *            are unsaved changes detected.
     * @throws ClimateException
     */
    private void dateChanged(boolean warn) throws ClimateException {
        // warn that current values will be lost if unsaved

        ClimateDate nextDate = dailyDate;
        nextDate = dailyDateSelector.getDate();

        if (nextDate.toFullDateString()
                .compareTo(dailyDate.toFullDateString()) != 0) {
            boolean load = true;

            if (warn && changeListener.isChangesUnsaved()) {
                String currentStation = stationNames
                        .getItem(stationNames.getSelectionIndex());
                // confirm that work is done/willing to be lost for
                // previous selection
                load = MessageDialog.openConfirm(getShell(), "Load new data?",
                        "Are you sure you wish to load " + currentStation
                                + " for the date " + nextDate.toFullDateString()
                                + "? Unsaved changes to " + currentStation
                                + " for the date "
                                + dailyDate.toFullDateString()
                                + " will be lost.");

            }
            if (load) {
                dailyDate = nextDate;
                currentMainDataSection.loadData();
            } else {
                dailyDateSelector.setDate(dailyDate);
            }

        }
    }

    /**
     * Use a confirmation pop-up for period date changing
     * 
     * @param warn
     *            true if there should be a confirmation, false otherwise
     * @param dateChanged
     *            true if the period date(s) was changed, false otherwise
     * @param message
     *            the confirm message
     * @return
     */
    private boolean dateChanged(boolean warn, boolean dateChanged,
            String message) {
        boolean load = true;
        if (dateChanged) {

            if (warn && changeListener.isChangesUnsaved()) {

                // confirm that work is done/willing to be lost for
                // previous selection
                load = MessageDialog.openConfirm(getShell(), "Load new data?",
                        message);

            }

        }
        return load;
    }

    private void createStationListSection(Composite parent) {
        Composite stationListComp = new Composite(parent, SWT.NONE);
        stationListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2));
        RowLayout stationListLayout = new RowLayout(SWT.HORIZONTAL);
        stationListLayout.spacing = 5;
        stationListLayout.center = true;
        stationListComp.setLayout(stationListLayout);

        Label stationLabel = new Label(stationListComp, SWT.NONE);
        stationLabel.setText("Station");

        stationNames = new List(stationListComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        GC gc = new GC(stationNames);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        /*
         * Height to limit space list will occupy
         */
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        stationNames.setLayoutData(new RowData(36 * fontWidth, 9 * fontHeight));

        for (Station station : stations) {
            stationNames.add(station.getStationName());
        }

        stationNames.setSelection(0);

        stationNames.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    stationSelectionChanged(true);
                } catch (ClimateException e) {
                    logger.error("Error handling station change.", e);
                }
            }
        });
    }

    /**
     * Selection for station list changed.
     * 
     * @param warn
     *            true to warn user.
     * @throws ClimateException
     */
    private void stationSelectionChanged(boolean warn) throws ClimateException {
        int currSelection = stationNames.getSelectionIndex();
        if (prevStationSelectionIndex != currSelection) {
            // warn that current values will be lost if unsaved
            boolean load = true;

            if (warn && changeListener.isChangesUnsaved()) {
                // confirm that work is done/willing to be lost for
                // previous selection
                load = MessageDialog.openConfirm(getShell(), "Load new data?",
                        "Are you sure you wish to load "
                                + stationNames.getItem(currSelection)
                                + "? Unsaved changes to "
                                + stationNames
                                        .getItem(prevStationSelectionIndex)
                                + " will be lost.");
            }
            if (load) {
                prevStationSelectionIndex = currSelection;
                currentMainDataSection.loadData();
            } else {
                stationNames.setSelection(prevStationSelectionIndex);
            }
        }

    }

    /**
     * For use by sub-section classes.
     * 
     * @return dialog's change listener
     */
    protected UnsavedChangesListener getChangeListener() {
        return changeListener;
    }
}

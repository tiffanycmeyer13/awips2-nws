/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchClimatePeriodRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.UpdateClimatePeriodRequest;
import gov.noaa.nws.ocp.viz.climate.initClimate.ClimatologyInputType;
import gov.noaa.nws.ocp.viz.climate.initClimate.dialog.importdata.ImportClimateDialog;
import gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records.DailyClimateDialog;
import gov.noaa.nws.ocp.viz.climate.initClimate.dialog.records.PeriodClimateDialog;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;
import gov.noaa.nws.ocp.viz.common.climate.listener.impl.ClimateTextListeners;

/**
 * This class display the "CLIMATOLOGY NORMALS, MEANS, EXTREMES" dialog for
 * init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 03/24/2016  18469    wkwock      Initial creation
 * 10/27/2016  20635    wkwock      Clean up
 * 11/30/2016  26405    astrakovsky Add auto refresh after data import.
 * 20 DEC 2016 26404    amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 27 DEC 2016 22450    amoore      Init should use common listeners where possible.
 * 27 FEB 2017 27420    amoore      Fix titles.
 * 01 MAY 2017 33546    astrakovsky Added check to prevent error when referencing disposed shell.
 * 15 MAY 2017 33104    amoore      Address FindBugs and logic issues.
 * 19 SEP 2017 38124    amoore      Use GC for text control sizes.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public class ClimateInitDialog extends ClimateCaveChangeTrackDialog {
    /**
     * import climate data dialog
     */
    protected ImportClimateDialog importClimateDlg = null;

    /**
     * daily climate dialog
     */
    private DailyClimateDialog dailyClimateDlg = null;

    /**
     * period climate dialog
     */
    private PeriodClimateDialog periodClimateDlg = null;

    /**
     * normal start year
     */
    private Text normStartYearTxt;

    /**
     * record start year
     */
    private Text recordStartYearTxt;

    /**
     * normal end year
     */
    private Text normEndYearTxt;

    /**
     * record end year
     */
    private Text recordEndYearTxt;

    /**
     * last station index
     */
    private int lastStationIndex = 0;

    /**
     * last selected button
     */
    private Button lastSelectedBtn = null;

    /**
     * stations list on GUI
     */
    private List stationsList = null;

    /**
     * stations list in memory
     */
    private java.util.List<Station> stations = null;

    /**
     * Collection of listeners.
     */
    private final ClimateTextListeners myDisplayListeners = new ClimateTextListeners();

    private Font inputTypeFont;

    /**
     * Constructor.
     * 
     * @param display
     */
    public ClimateInitDialog(Display display) {
        super(display);
        setText("Climatology Normals, Means, Extremes");
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event) {
                closeAllOtherDlgs();
            }
        });

        shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (inputTypeFont != null) {
                    inputTypeFont.dispose();
                }
            }
        });

        // Create the main layout for the shell.
        createMenus();
        createMainControls();
    }

    /**
     * Check for unsaved changes both in this dialog and in daily/period
     * dialogs.
     */
    public boolean shouldClose() {
        boolean close = true;
        if (changeListener.isChangesUnsaved()
                || (dailyClimateDlg != null && !dailyClimateDlg.isDisposed()
                        && dailyClimateDlg.isChangesUnsaved())
                || (periodClimateDlg != null && !periodClimateDlg.isDisposed()
                        && periodClimateDlg.isChangesUnsaved())) {
            close = MessageDialog.openConfirm(shell, "Unsaved Changes",
                    "Close Init Climate? Unsaved changes will be lost.");
        }

        return close;
    }

    @Override
    protected void opened() {
        updateStationLst();
        updateYears();
        openDailyClimateDlg();
    }

    /**
     * Create the menus at the top of the display.
     */
    private void createMenus() {
        Menu menuBar = new Menu(shell, SWT.BAR);

        createFileMenu(menuBar);
        createHelpMenu(menuBar);

        shell.setMenuBar(menuBar);
    }

    /**
     * Create the File menu.
     * 
     * @param menuBar
     *            Menu bar.
     */
    private void createFileMenu(Menu menuBar) {
        MenuItem fileMenuItem = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuItem.setText("&File");

        Menu fileMenu = new Menu(menuBar);
        fileMenuItem.setMenu(fileMenu);

        MenuItem closeMI = new MenuItem(fileMenu, SWT.NONE);
        closeMI.setText("&Close");
        closeMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });

        MenuItem importMI = new MenuItem(fileMenu, SWT.NONE);
        importMI.setText("Import Data Window");
        importMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if ((importClimateDlg == null)
                        || importClimateDlg.getShell().isDisposed()) {
                    importClimateDlg = new ImportClimateDialog(
                            Display.getCurrent(), ClimateInitDialog.this);
                    importClimateDlg.addCloseCallback(new ICloseCallback() {

                        @Override
                        public void dialogClosed(Object returnValue) {
                            importClimateDlg = null;
                        }
                    });
                    importClimateDlg.open();
                } else {
                    importClimateDlg.bringToTop();
                }
            }
        });

    }

    /**
     * Create the Help menu.
     * 
     * @param menuBar
     *            Menu bar.
     */
    private void createHelpMenu(Menu menuBar) {
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
                Handbook.displayHandbook("initialize_climate.html");
            }
        });
    }

    /**
     * Create the main controls.
     */
    private void createMainControls() {
        Composite controlComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        controlComp.setLayout(gl);

        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        controlComp.setLayoutData(gd);

        // Station label
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label stationLbl = new Label(controlComp, SWT.CENTER);
        stationLbl.setText("Station");
        stationLbl.setLayoutData(gd);

        // Station (single selection) composite
        stationsList = new List(controlComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        GC gc = new GC(stationsList);
        /*
         * Height to limit space the scroll will take up
         */
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        gd.heightHint = 7 * fontHeight;
        stationsList.setLayoutData(gd);
        stationsList.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeStation();
            }
        });

        // Start year combo
        createYearPeriodControls(controlComp);

        // Save Years button
        Button saveYearBtn = new Button(controlComp, SWT.PUSH);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        saveYearBtn.setLayoutData(gd);
        saveYearBtn.setText("Save Years");
        saveYearBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                saveYears();
            }
        });

        // Climatology Input Type radio group
        SelectionListener selectionListener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                Button button = ((Button) event.widget);
                openSelectedDlg(button);
            };
        };
        Group typeGrp = new Group(controlComp, SWT.SHADOW_IN);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.verticalIndent = 20;
        typeGrp.setLayoutData(gd);

        FontData fontData = typeGrp.getFont().getFontData()[0];
        if (inputTypeFont == null) {
            inputTypeFont = new Font(getDisplay(), new FontData(
                    fontData.getName(), fontData.getHeight(), SWT.BOLD));
        }

        typeGrp.setFont(inputTypeFont);
        typeGrp.setText("Climatology Input Type");
        typeGrp.setLayout(new RowLayout(SWT.VERTICAL));

        Button dailyBtn = new Button(typeGrp, SWT.RADIO);
        dailyBtn.setText(ClimatologyInputType.DAILY.toString());
        dailyBtn.addSelectionListener(selectionListener);
        dailyBtn.setSelection(true);

        lastSelectedBtn = dailyBtn;

        Button monthlyRdo = new Button(typeGrp, SWT.RADIO);
        monthlyRdo.setText(ClimatologyInputType.MONTHLY.toString());
        monthlyRdo.addSelectionListener(selectionListener);

        Button seasonalRdo = new Button(typeGrp, SWT.RADIO);
        seasonalRdo.setText(ClimatologyInputType.SEASONAL.toString());
        seasonalRdo.addSelectionListener(selectionListener);

        Button annualRdo = new Button(typeGrp, SWT.RADIO);
        annualRdo.setText(ClimatologyInputType.ANNUAL.toString());
        annualRdo.addSelectionListener(selectionListener);

        // Close button
        Button closeBtn = new Button(controlComp, SWT.PUSH);
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.verticalIndent = 20;
        closeBtn.setLayoutData(gd);
        closeBtn.setText("Close");
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * Change to a different station
     */
    protected void changeStation() {
        int selectedIndex = stationsList.getSelectionIndex();
        if (selectedIndex == lastStationIndex) {
            return;
        }

        String selectedStation = stationsList.getItem(selectedIndex);

        boolean proceed = true;
        if (changeListener.isChangesUnsaved()
                || (dailyClimateDlg != null && !dailyClimateDlg.isDisposed()
                        && dailyClimateDlg.isChangesUnsaved())
                || (periodClimateDlg != null && !periodClimateDlg.isDisposed()
                        && periodClimateDlg.isChangesUnsaved())) {
            proceed = MessageDialog.openConfirm(shell,
                    "Change to Different Record", "Change to " + selectedStation
                            + "? Unsaved changes will be lost.");
        }
        if (!proceed) {
            stationsList.setSelection(lastStationIndex);
            return;
        }

        lastStationIndex = selectedIndex;

        refreshSelection();
    }

    /**
     * Refresh current selection (26405 - Add auto refresh after data import)
     */
    public void refreshSelection() {
        int selectedIndex = stationsList.getSelectionIndex();
        String selectedStation = stationsList.getItem(selectedIndex);

        if (dailyClimateDlg != null && !dailyClimateDlg.isDisposed()) {
            dailyClimateDlg.setStationLoc(selectedStation);
        }
        if (periodClimateDlg != null && !periodClimateDlg.isDisposed()) {
            periodClimateDlg.setStationLoc(selectedStation);
            periodClimateDlg.displayPeriodClimatologyInfo();
        }
        updateYears();
    }

    /**
     * create year period controls
     * 
     * @param controlComp
     */
    private void createYearPeriodControls(Composite controlComp) {
        Composite yearComp = new Composite(controlComp, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        yearComp.setLayout(gl);

        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.verticalIndent = 20;
        yearComp.setLayoutData(gd);

        // period labels
        Label fakeLbl = new Label(yearComp, SWT.CENTER);
        fakeLbl.setText(""); // just a place holder
        Label NormPeriod = new Label(yearComp, SWT.LEFT);
        NormPeriod.setText("Normals\nPeriod");
        Label recordPeriod = new Label(yearComp, SWT.LEFT);
        recordPeriod.setText("Records\nPeriod");
        // year label
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label yearLbl = new Label(yearComp, SWT.LEFT);
        yearLbl.setText("Start Year");
        yearLbl.setLayoutData(gd);

        normStartYearTxt = new Text(yearComp, SWT.WRAP | SWT.BORDER);
        normStartYearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        normStartYearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        normStartYearTxt.addListener(SWT.Modify, changeListener);

        recordStartYearTxt = new Text(yearComp, SWT.WRAP | SWT.BORDER);
        recordStartYearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        recordStartYearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        recordStartYearTxt.addListener(SWT.Modify, changeListener);

        // year label
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Label endYearLbl = new Label(yearComp, SWT.LEFT);
        endYearLbl.setText("End Year");
        endYearLbl.setLayoutData(gd);

        normEndYearTxt = new Text(yearComp, SWT.WRAP | SWT.BORDER);
        normEndYearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        normEndYearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        normEndYearTxt.addListener(SWT.Modify, changeListener);

        recordEndYearTxt = new Text(yearComp, SWT.WRAP | SWT.BORDER);
        recordEndYearTxt.addListener(SWT.Verify,
                myDisplayListeners.getDefaultIntListener());
        recordEndYearTxt.addListener(SWT.FocusOut,
                myDisplayListeners.getDefaultIntListener());
        recordEndYearTxt.addListener(SWT.Modify, changeListener);

    }

    /**
     * open daily climate dialog
     */
    public void openDailyClimateDlg() {

        if ((dailyClimateDlg == null)
                || dailyClimateDlg.getShell().isDisposed()) {
            dailyClimateDlg = new DailyClimateDialog(Display.getCurrent());
            dailyClimateDlg.addCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    dailyClimateDlg = null;
                }
            });
            dailyClimateDlg.open();

            dailyClimateDlg.setStationLoc(getStation());
            dailyClimateDlg.getShell().setLocation(
                    getShell().getLocation().x + getShell().getSize().x, 200);
        } else {
            dailyClimateDlg.bringToTop();
        }
    };

    /**
     * open selected climatology dialog
     * 
     * @param button
     */
    private void openSelectedDlg(Button button) {
        if (!button.getSelection()) {
            return;
        }

        if (button != lastSelectedBtn) {
            boolean proceed = true;
            if ((dailyClimateDlg != null && !dailyClimateDlg.isDisposed()
                    && dailyClimateDlg.isChangesUnsaved())
                    || (periodClimateDlg != null
                            && !periodClimateDlg.isDisposed()
                            && periodClimateDlg.isChangesUnsaved())) {
                proceed = MessageDialog.openConfirm(shell,
                        "Change to Different Climatology",
                        "Change to " + button.getText()
                                + "? Unsaved changes will be lost.");
            }
            if (!proceed) {
                lastSelectedBtn.setSelection(true);
                button.setSelection(false);
                return;
            }

            lastSelectedBtn = button;
        }

        Point location = getShell().getLocation();
        location.x = location.x + getShell().getSize().x;
        location.y = 200;
        if (button.getText()
                .equalsIgnoreCase(ClimatologyInputType.DAILY.name())) {
            if (periodClimateDlg != null && !periodClimateDlg.isDisposed()) {
                location = periodClimateDlg.getShell().getLocation();
                periodClimateDlg.close();
                periodClimateDlg = null;
            }

            if (dailyClimateDlg == null || dailyClimateDlg.isDisposed()) {
                dailyClimateDlg = new DailyClimateDialog(getDisplay());
                dailyClimateDlg.open();
                dailyClimateDlg.getShell().setLocation(location);
                dailyClimateDlg.setStationLoc(getStation());
            }
            dailyClimateDlg.bringToTop();
        } else {
            if (dailyClimateDlg != null && !dailyClimateDlg.isDisposed()) {
                location = dailyClimateDlg.getShell().getLocation();
                dailyClimateDlg.close();
                dailyClimateDlg = null;
            }

            if (periodClimateDlg == null || periodClimateDlg.isDisposed()) {
                periodClimateDlg = new PeriodClimateDialog(getDisplay());
                periodClimateDlg.open();
                periodClimateDlg.getShell().setLocation(location);
                periodClimateDlg.setStationLoc(getStation());
            }
            periodClimateDlg.setClimatologyInputType(ClimatologyInputType
                    .valueOf(button.getText().toUpperCase()));
        }
    };

    /**
     * get selected station
     * 
     * @return
     */
    private String getStation() {
        return stationsList.getItem(stationsList.getSelectionIndex());
    }

    @SuppressWarnings("unchecked")
    public void updateStationLst() {
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        try {
            stations = (java.util.List<Station>) ThriftClient
                    .sendRequest(request);
        } catch (VizException e) {
            logger.error("Could not retrieve stations from DB", e);
        }

        if (stations != null) {
            for (Station station : stations) {
                stationsList.add(station.getStationName());
            }
            stationsList.setSelection(lastStationIndex);
        }
        shell.pack();
    }

    /**
     * update years
     */
    public void updateYears() {
        int stationId = -1;
        String stationName = getStation();
        for (Station station : stations) {
            if (stationName.equals(station.getStationName())) {
                stationId = station.getInformId();
                break;
            }
        }

        // get years from climate_period tables
        int years[] = null;
        FetchClimatePeriodRequest request = new FetchClimatePeriodRequest(
                stationId);
        try {
            years = (int[]) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            logger.error("Failed to fetch record for station =" + stationId, e);
        }

        if (years == null) {
            years = new int[] { ParameterFormatClimate.MISSING,
                    ParameterFormatClimate.MISSING,
                    ParameterFormatClimate.MISSING,
                    ParameterFormatClimate.MISSING };
        }

        changeListener.setIgnoreChanges(true);
        normStartYearTxt.setText(Integer.toString(years[0]));
        normEndYearTxt.setText(Integer.toString(years[1]));
        recordStartYearTxt.setText(Integer.toString(years[2]));
        recordEndYearTxt.setText(Integer.toString(years[3]));

        changeListener.setChangesUnsaved(false);
        changeListener.setIgnoreChanges(false);
    }

    /**
     * save period years
     */
    protected void saveYears() {
        int stationId = -1;
        String stationName = getStation();
        for (Station station : stations) {
            if (stationName.equals(station.getStationName())) {
                stationId = station.getInformId();
                break;
            }
        }

        int normStartYear = Integer.parseInt(normStartYearTxt.getText());
        int recordStartYear = Integer.parseInt(recordStartYearTxt.getText());
        int normEndYear = Integer.parseInt(normEndYearTxt.getText());
        int recordEndYear = Integer.parseInt(recordEndYearTxt.getText());

        boolean recordSaved = false;
        UpdateClimatePeriodRequest updateRrequest = new UpdateClimatePeriodRequest(
                stationId, normStartYear, normEndYear, recordStartYear,
                recordEndYear);
        try {
            recordSaved = (boolean) ThriftClient.sendRequest(updateRrequest);
        } catch (VizException e) {
            logger.error("Failed to save record with station ID=" + stationId,
                    e);
        }

        if (recordSaved) {
            MessageDialog.openInformation(shell, "Save successful",
                    "Climate periods successfully saved.");
            this.changeListener.setChangesUnsaved(false);
        } else {
            MessageDialog.openError(shell, "Save unsuccessful",
                    "Failed to save climate periods.");
        }
    }

    /**
     * close all dialogs
     */
    protected void closeAllOtherDlgs() {
        if (importClimateDlg != null && !importClimateDlg.isDisposed()) {
            importClimateDlg.close();
        }

        if (dailyClimateDlg != null && !dailyClimateDlg.isDisposed()) {
            dailyClimateDlg.close();
        }

        if (periodClimateDlg != null && !periodClimateDlg.isDisposed()) {
            periodClimateDlg.close();
        }
    }
}

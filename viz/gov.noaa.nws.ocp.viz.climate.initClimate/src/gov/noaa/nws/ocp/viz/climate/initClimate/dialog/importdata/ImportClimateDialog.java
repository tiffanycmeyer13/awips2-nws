/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.importdata;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.UpdateClimateDayNormNoMissingRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.UpdateClimateMonthNormNoMissingRequest;
import gov.noaa.nws.ocp.viz.climate.initClimate.dialog.ClimateInitDialog;
import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;

/**
 * This class display the "Import Climate" dialog for init_climate
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 05/06/2016  18469    wkwock      Initial creation
 * 10/20/2016  20635    wkwock      javadoc
 * 11/30/2016  26405    astrakovsky Add auto refresh after data import.
 * 20 DEC 2016 26404    amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 21 DEC 2016 20955    amoore      Clean up class name.
 * 22 DEC 2016 22395    amoore      Correct multi-selection of stations/months; java standards.
 *                                  Handle null pointer exceptions.
 * 03 MAY 2017 33104    amoore      Address FindBugs. Better variable naming.
 * 15 MAY 2017 33104    amoore      FindBugs and logic issues.
 * 19 SEP 2017 38124    amoore      Use GC for text control sizes.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public class ImportClimateDialog extends ClimateCaveDialog {
    /**
     * date formats
     */
    private String[] dateFormats = { FileData.MONTH_FIRST_STRING,
            FileData.DAY_FIRST_STRING, FileData.JULIAN_DAYS_STRING,
            FileData.JULIAN_DAYS_FEB29_60_STRING };

    /**
     * Delimiter names
     */
    private String[] delimiterNames = { "Space", "Tab", "Pipe(|)", "Colon(:)",
            "Comma(,)", "Semi-colon(;)", "Caret(^)" };

    /**
     * Delimiter shortened names.
     */
    private String[] delimiters = { "\\s+", "\t", "\\|", ":", ",", ";", "^" };

    /** climate init dialog */
    private ClimateInitDialog climateInitDialog = null;

    /**
     * month list
     */
    private List monthList = null;

    /**
     * station name list
     */
    protected List stationList = null;

    /**
     * file name text field
     */
    private Text fileNameValueTxt;

    /**
     * order button
     */
    protected Button orderBtn;

    /**
     * add File to List button
     */
    private Button addBtn;

    /**
     * Save new Values to list button
     */
    private Button saveBtn;

    /**
     * Delete From List button
     */
    private Button deleteBtn;

    /**
     * Import Data from File(s) button
     */
    private Button importBtn;

    /**
     * delimiter combo
     */
    private Combo delimiterCbo;

    /**
     * Table with data file information
     */
    private Table infoTable;

    /**
     * Daily type radio button
     */
    private Button dailyTypeRdo;

    /**
     * Monthly type radio button
     */
    private Button monthlyTypeRdo;

    /**
     * station header radio button
     */
    private Button stationHeaderRdo;

    /**
     * Station data radio button
     */
    private Button stationDataRdo;

    /**
     * Month header radio button
     */
    private Button monthHeaderRdo;

    /**
     * Month date radio button
     */
    private Button monthDateRdo;

    /**
     * Date format combo
     */
    private Combo dateFormatCbo;

    /**
     * Data arrange dialog
     */
    protected DataArrangeDialog dataArrageDlg = null;

    /**
     * File Data list to keep track items on table
     */
    private Map<TableItem, FileData> fileDataMap = new HashMap<>();

    /**
     * current file data that has not yet been completed.
     */
    private FileData workingFileData = null;

    /**
     * Constructor.
     * 
     * @param display
     */
    public ImportClimateDialog(Display display, ClimateInitDialog climateInit) {
        super(display, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
                CAVE.INDEPENDENT_SHELL);
        setText("Import Climate");
        this.climateInitDialog = climateInit;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        mainLayout.verticalSpacing = 5;
        shell.setLayout(mainLayout);

        createMenus();

        createMainControls();
    }

    @Override
    protected void opened() {
        loadData();
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

        MenuItem quitMI = new MenuItem(fileMenu, SWT.NONE);
        quitMI.setText("&Close");
        quitMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
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
        MenuItem handbookMI = new MenuItem(helpMenu, SWT.NONE);
        handbookMI.setText("Handbook");
        handbookMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("import_climate.html");
            }
        });
    }

    /**
     * Create the main controls.
     */
    private void createMainControls() {
        createTopControls();

        // separate line
        Label separateLbl = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createBottomControls();
    }

    /**
     * create top controls: station name, date format
     */
    private void createTopControls() {
        Composite topComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(5, false);
        gl.marginWidth = 20;
        topComp.setLayout(gl);

        createStationNameControls(topComp);

        // separate line
        Label separate1Lbl = new Label(topComp, SWT.SEPARATOR | SWT.VERTICAL);
        separate1Lbl.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        createFileNameControls(topComp);

        // separate line
        Label separate2Lbl = new Label(topComp, SWT.SEPARATOR | SWT.VERTICAL);
        separate2Lbl.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        createDateFormatControls(topComp);
    }

    /**
     * create station name controls
     * 
     * @param mainComp
     */
    private void createStationNameControls(Composite mainComp) {
        Composite stationNameComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 20;
        stationNameComp.setLayout(gl);

        // Station label
        Label stationLbl = new Label(stationNameComp, SWT.CENTER);
        stationLbl.setText("Station Name");

        Label chooseLbl = new Label(stationNameComp, SWT.CENTER);
        chooseLbl.setText("Choose Station in File:");
        // Station (single selection) composite
        // Task 22395, disable multi-selection of stations
        stationList = new List(stationNameComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        GC gc = new GC(stationList);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        /*
         * Height to cleanly fit multiple stations on screen
         */
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        GridData stationListGd = new GridData();
        stationListGd.widthHint = 29 * fontWidth;
        stationListGd.heightHint = 5 * fontHeight;
        stationList.setLayoutData(stationListGd);
        stationList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // enable data ordering
                if (stationList.getSelectionIndex() > -1) {
                    orderBtn.setEnabled(true);
                } else {
                    orderBtn.setEnabled(false);
                }
            }
        });

        // Station Name in Header button
        stationHeaderRdo = new Button(stationNameComp, SWT.RADIO);
        stationHeaderRdo.setText("Station Name in Header");
        stationHeaderRdo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeStationType();
            };
        });
        stationHeaderRdo.setSelection(true);

        // Station Name(s) in Data radio button
        stationDataRdo = new Button(stationNameComp, SWT.RADIO);
        stationDataRdo.setText("Station Name(s) in Data");
        stationDataRdo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeStationType();
            };
        });
        stationDataRdo.setSelection(false);

        // separate line
        Label separateLbl = new Label(stationNameComp,
                SWT.SEPARATOR | SWT.HORIZONTAL);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite delimitersComp = new Composite(stationNameComp, SWT.NONE);
        GridLayout delimiterGl = new GridLayout(2, false);
        delimitersComp.setLayout(delimiterGl);

        // Station label
        Label delimiterLbl = new Label(delimitersComp, SWT.LEFT);
        delimiterLbl.setText("Select Data Delimiter:");

        delimiterCbo = new Combo(delimitersComp, SWT.POP_UP);
        delimiterCbo.setItems(delimiterNames);
        delimiterCbo.select(0);

        // default station name section to disabled
        stationList.setEnabled(false);
        stationHeaderRdo.setEnabled(false);
        stationDataRdo.setEnabled(false);
    }

    /**
     * create file name controls
     * 
     * @param mainComp
     */
    private void createFileNameControls(Composite mainComp) {
        Composite fileNameComp = new Composite(mainComp, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 20;
        fileNameComp.setLayout(gl);

        // file name label
        Label fileNameLbl = new Label(fileNameComp, SWT.CENTER);
        fileNameLbl.setText("File Name");

        fileNameValueTxt = new Text(fileNameComp, SWT.WRAP | SWT.BORDER);

        GC gc = new GC(fileNameValueTxt);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        fileNameValueTxt.setEditable(false);
        GridData fileNameValueGd = new GridData();
        fileNameValueGd.widthHint = 23 * fontWidth;
        fileNameValueTxt.setLayoutData(fileNameValueGd);

        // Get New Data File Name button
        Button fileBtn = new Button(fileNameComp, SWT.PUSH);
        fileBtn.setText("Get New Data File Name");
        fileBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                getFileName();
            };
        });

        // separate line
        Label separateLbl = new Label(fileNameComp,
                SWT.SEPARATOR | SWT.HORIZONTAL);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label dataTypeLbl = new Label(fileNameComp, SWT.CENTER);
        dataTypeLbl.setText("Data Type");

        Composite dataTypeComp = new Composite(fileNameComp, SWT.NONE);
        GridLayout dataTypeGl = new GridLayout(2, false);
        dataTypeGl.marginWidth = 20;
        dataTypeComp.setLayout(dataTypeGl);

        // Daily radio button
        dailyTypeRdo = new Button(dataTypeComp, SWT.RADIO);
        dailyTypeRdo.setText("Daily");
        dailyTypeRdo.setSelection(true);
        dailyTypeRdo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeType(true);
            };
        });

        // Monthly radio button
        monthlyTypeRdo = new Button(dataTypeComp, SWT.RADIO);
        monthlyTypeRdo.setText("Monthly");
        monthlyTypeRdo.setSelection(false);
        dailyTypeRdo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeType(false);
            };
        });

    }

    /**
     * change station type
     */
    protected void changeStationType() {
        stationList.setEnabled(stationHeaderRdo.getSelection());

        /*
         * An SWT graphical flaw makes selected items in a list seem to
         * disappear when the list is disabled, so de-select the items to
         * prevent confusion.
         */
        if (!stationList.isEnabled()) {
            stationList.deselectAll();
        }

        if (stationDataRdo.getSelection()
                || (stationList.getSelectionIndex() > -1)) {
            /*
             * (Station type is in data, or in header and a station name is
             * selected), and a file has been selected, so enable data ordering
             * button.
             */
            orderBtn.setEnabled(true);
        } else {
            /*
             * A file is being worked, and station type is in header but no
             * station name selected, so disable data ordering button.
             */
            orderBtn.setEnabled(false);
        }

        FileData fileData = workingFileData;
        if (fileData == null) {
            fileData = fileDataMap
                    .get(infoTable.getItem(infoTable.getSelectionIndex()));
        }

        if (fileData.getChoices() == null
                || fileData.getChoices().length == 0) {
            return;
        }

        boolean change = MessageDialog.openQuestion(shell, "Change Station",
                "Change station will reset the order of data in file to none. \nContinue with new selection?");
        if (change) {
            fileData.setChoices(null);
            if (stationHeaderRdo.getSelection()) {
                fileData.setStationName(null);
            } else {
                fileData.setStationName(FileData.LOCATED_IN_DATA);
            }
        } else {
            // undo the selection
            stationDataRdo.setSelection(!stationDataRdo.getSelection());
            stationHeaderRdo.setSelection(!stationHeaderRdo.getSelection());
            stationList.setEnabled(stationHeaderRdo.getSelection());
        }
    }

    /**
     * Change to daily or monthly type
     * 
     * @param isDaily
     */
    private void changeType(boolean isDaily) {
        FileData fileData = workingFileData;
        if ((fileData == null) && (infoTable.getSelectionIndex() > -1)
                && (infoTable.getItemCount() > 0)) {
            fileData = fileDataMap
                    .get(infoTable.getItem(infoTable.getSelectionIndex()));
        }

        if (fileData == null) {
            return;
        }

        if (fileData.getChoices() == null
                || fileData.getChoices().length == 0) {
            return;
        }

        boolean change = MessageDialog.openQuestion(shell, "Change Type",
                "Change type will reset the order of data in file to none. \nContinue with new type?");
        if (change) {
            fileData.setChoices(null);
            fileData.setDaily(dailyTypeRdo.getSelection());
        } else {
            // undo the selection
            dailyTypeRdo.setSelection(!dailyTypeRdo.getSelection());
            monthlyTypeRdo.setSelection(!monthlyTypeRdo.getSelection());
        }
    }

    /**
     * get file name
     */
    protected void getFileName() {
        // file could be in any place.
        FileDialog fd = new FileDialog(shell);
        fd.open();
        String selectedFile = fd.getFileName();
        if (selectedFile.isEmpty()) {
            return;
        }

        String filePath = fd.getFilterPath();
        fileNameValueTxt.setText(selectedFile);

        // Check if selected file already in the list
        boolean found = false;
        for (Entry<TableItem, FileData> entry : fileDataMap.entrySet()) {
            FileData fileData = entry.getValue();
            if (fileData.getPathName().equals(filePath)
                    && fileData.getFileName().equals(selectedFile)) {
                infoTable.setSelection(entry.getKey());
                found = true;
                break;
            }
        }

        if (found) {
            this.updateDlgDisplay();
        } else {
            workingFileData = new FileData();
            workingFileData.setFileName(selectedFile);
            workingFileData.setPathName(filePath);

            stationList.deselectAll();
            stationHeaderRdo.setEnabled(true);
            stationList.setEnabled(true);
            stationDataRdo.setEnabled(true);
            stationHeaderRdo.setSelection(true);
            stationDataRdo.setSelection(false);

            delimiterCbo.select(0);

            dailyTypeRdo.setSelection(true);
            monthlyTypeRdo.setSelection(false);

            monthList.deselectAll();
            monthList.setEnabled(true);
            monthHeaderRdo.setSelection(true);
            monthDateRdo.setSelection(false);

            addBtn.setEnabled(true);
            deleteBtn.setEnabled(false);
            saveBtn.setEnabled(false);

            infoTable.deselectAll();
        }
    }

    /**
     * create Date Format controls
     * 
     * @param mainComp
     */
    private void createDateFormatControls(Composite mainComp) {
        Composite dateFormatComp = new Composite(mainComp, SWT.NONE);
        dateFormatComp
                .setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, true));
        RowLayout rl = new RowLayout(SWT.VERTICAL);
        rl.marginWidth = 20;
        dateFormatComp.setLayout(rl);

        // date format label
        Label dateFormatLbl = new Label(dateFormatComp, SWT.CENTER);
        dateFormatLbl.setText("Date Format");

        Label chooseLbl = new Label(dateFormatComp, SWT.CENTER);
        chooseLbl.setText("Choose Month(s) in File:");

        // Month (single selection) composite
        monthList = new List(dateFormatComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        GC gc = new GC(monthList);
        int fontHeight = gc.getFontMetrics().getHeight();
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();
        monthList.setLayoutData(new RowData(fontWidth * 14, fontHeight * 5));

        // header radio button
        monthHeaderRdo = new Button(dateFormatComp, SWT.RADIO);
        monthHeaderRdo.setText("Month Name(s) in Header(s)");
        monthHeaderRdo.setEnabled(true);
        monthHeaderRdo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeMonthSelectionType();
            };
        });
        monthHeaderRdo.setSelection(true);

        // Date radio button
        monthDateRdo = new Button(dateFormatComp, SWT.RADIO);
        monthDateRdo.setText("Month Located in Date Field");
        monthDateRdo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                changeMonthSelectionType();
            };
        });
        monthDateRdo.setSelection(false);

        // first drop menu
        dateFormatCbo = new Combo(dateFormatComp, SWT.POP_UP);
        dateFormatCbo.setItems(dateFormats);
        dateFormatCbo.select(0);
        dateFormatCbo.setEnabled(false);
    }

    /**
     * create bottom controls
     */
    private void createBottomControls() {
        Composite bottomComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(2, false);
        gl.marginWidth = 10;
        bottomComp.setLayout(gl);

        Composite controlsComp = new Composite(bottomComp, SWT.NONE);
        GridLayout controlGl = new GridLayout(1, false);
        controlGl.marginWidth = 0;
        controlsComp.setLayout(controlGl);

        // Arrange Order of Data button
        orderBtn = new Button(controlsComp, SWT.PUSH);
        orderBtn.setText("Arrange Order\nof Data");
        orderBtn.setEnabled(false);
        orderBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                openDataArrangeDlg();
            };
        });

        // add File to List button
        addBtn = new Button(controlsComp, SWT.PUSH);
        addBtn.setText("Add File\nto List");
        addBtn.setEnabled(false);
        addBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                addFileToList();
            };
        });

        // Save new Values for File button
        saveBtn = new Button(controlsComp, SWT.PUSH);
        saveBtn.setText("Save New Values\nfor File");
        saveBtn.setEnabled(false);
        saveBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                saveForFile();
            };
        });

        // Delete From List button
        deleteBtn = new Button(controlsComp, SWT.PUSH);
        deleteBtn.setText("Delete\nFrom List");
        deleteBtn.setEnabled(false);
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                deleteFromList();
            };
        });

        // Import Data from File(s) button
        importBtn = new Button(controlsComp, SWT.PUSH);
        importBtn.setText("Import Data\nfrom File(s)");
        importBtn.setEnabled(false);
        importBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                importDataFromFile();
            };
        });

        // Cancel button
        Button cancelBtn = new Button(controlsComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                closeWindow();
            };
        });

        // information table
        infoTable = new Table(bottomComp,
                SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);

        GC gc = new GC(infoTable);
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        infoTable.setLinesVisible(true);
        infoTable.setHeaderVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = 107 * fontWidth;
        infoTable.setLayoutData(data);
        String[] titles = { "File", "Station(s)", "Data Type", "Date Format",
                "Delimiter" };
        int[] sizes = { 24 * fontWidth, 34 * fontWidth, 20 * fontWidth,
                20 * fontWidth, 8 * fontWidth };
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(infoTable,
                    SWT.FILL | SWT.BORDER);

            column.setText(titles[i]);
            column.setWidth(sizes[i]);
        }

        infoTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                updateDlgDisplay();
            };
        });
    }

    /**
     * open the the "Import Daily or Monthly Climatology Data" window
     */
    protected void openDataArrangeDlg() {
        final FileData fileData;
        if (infoTable.getSelectionIndex() == -1) {
            /*
             * The info table does not have a selected element. Assume user
             * wants to handle the work-in-progress file data being filled out.
             */
            if (workingFileData != null) {
                fileData = workingFileData;
            } else {
                MessageDialog.openError(shell, "Select a File",
                        "No file is currently selected.");
                return;
            }
        } else {
            /*
             * The info table has a selected element. Assume user wants to
             * arrange data order for the selected file.
             */
            fileData = fileDataMap
                    .get(infoTable.getItem(infoTable.getSelectionIndex()));
        }

        FileData tmpFileData = new FileData();
        getFileData(tmpFileData);
        if (dataArrageDlg == null || dataArrageDlg.isDisposed()) {
            dataArrageDlg = new DataArrangeDialog(shell,
                    dailyTypeRdo.getSelection(), tmpFileData);
            dataArrageDlg.open();
            dataArrageDlg.getShell().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    int[] choices = (int[]) dataArrageDlg.getReturnValue();
                    fileData.setChoices(choices);
                }
            });
        } else {
            dataArrageDlg.bringToTop();
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        // Use loop to avoid months[12], the 13th lunar month
        for (int i = 0; i < 12; i++) {
            monthList.add(months[i]);
        }

        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);

        java.util.List<Station> stations = null;
        try {
            stations = (java.util.List<Station>) ThriftClient
                    .sendRequest(request);
        } catch (VizException e) {
            logger.error("Could not retrieve stations from DB", e);
        }

        if (stations != null) {
            for (Station station : stations) {
                stationList.add(station.getStationName());
            }
        }
    }

    /**
     * add file to list. Original: appWindImportClimate.C:activateCB_pBListAdd()
     * 
     * @return true for successfully add file to list
     */
    protected boolean addFileToList() {
        // get selected station and month
        if (!this.stationDataRdo.getSelection()
                && stationList.getSelectionCount() <= 0) {
            MessageDialog.openError(shell, "Choose Station",
                    "You must choose a station name from the station list first.");
            return false;
        }
        if (!monthDateRdo.getSelection()
                && this.monthList.getSelectionCount() <= 0) {
            MessageDialog.openError(shell, "Choose Month",
                    "You must choose a month from the month list first.");
            return false;
        }

        getFileData(workingFileData);

        if (workingFileData.getChoices() == null
                || workingFileData.getChoices().length == 0) {
            MessageDialog.openError(shell, "Arrange Data Order",
                    "You must arrange the order of data.");
            return false;
        }

        String type = workingFileData.isDaily() ? "Daily" : "Monthly";

        TableItem item = new TableItem(infoTable, SWT.NONE);
        item.setText(new String[] { workingFileData.getFileName(),
                workingFileData.getStationName(), type,
                workingFileData.getDateFormat(),
                workingFileData.getDelimiter() });
        infoTable.setSelection(item);

        fileDataMap.put(item, workingFileData);

        addBtn.setEnabled(false);
        deleteBtn.setEnabled(true);

        enableSaveBtn();
        enableImportBtn();

        return true;
    }

    /**
     * get station, date, data type, and delimiter info from GUI
     * 
     * @return FileData
     */
    private void getFileData(FileData fileData) {
        // station(s)
        if (stationDataRdo.getSelection()) {
            fileData.setStationName(FileData.LOCATED_IN_DATA);
        } else {
            int index = stationList.getSelectionIndex();
            if (index < 0) {
                fileData.setStationName(null);
            } else {
                fileData.setStationName(
                        stationList.getItem(stationList.getSelectionIndex()));
            }
        }

        // Data Type
        fileData.setDaily(dailyTypeRdo.getSelection());

        // date format
        if (this.monthDateRdo.getSelection()) {
            fileData.setDateFormat(dateFormatCbo.getText());
        } else {
            int index = monthList.getSelectionIndex();
            if (index < 0) {
                fileData.setDateFormat(null);
            } else {
                fileData.setDateFormat(monthList.getItem(index));
            }
        }

        /* Add to delimiter list */
        String delimiter = delimiters[delimiterCbo.getSelectionIndex()];
        fileData.setDelimiter(delimiter);
    }

    /**
     * Save new values for selected file
     */
    protected void saveForFile() {
        if (infoTable.getSelectionIndex() < 0) {
            return;
        }

        FileData fileData = fileDataMap
                .get(infoTable.getItem(infoTable.getSelectionIndex()));

        getFileData(fileData);

        // check station
        if (fileData.getStationName() == null) {
            MessageDialog.openWarning(shell, "Choose Station",
                    "Please choose a station.");
            return;
        }

        // Check month
        if (fileData.getDateFormat() == null) {
            MessageDialog.openWarning(shell, "Choose Date Format",
                    "Please choose date format.");
            return;
        }

        // check data order
        if (fileData.getChoices() == null
                || fileData.getChoices().length == 0) {
            MessageDialog.openWarning(shell, "Data Order",
                    "Please arrange data order.");
            return;
        }

        String type = fileData.isDaily() ? "Daily" : "Monthly";

        TableItem item = infoTable.getItem(infoTable.getSelectionIndex());
        item.setText(new String[] { fileData.getFileName(),
                fileData.getStationName(), type, fileData.getDateFormat(),
                fileData.getDelimiter() });
        infoTable.setSelection(item);
        updateDlgDisplay();
    }

    /**
     * Delete selected item from list
     */
    protected void deleteFromList() {
        if (infoTable.getSelectionCount() <= 0) {
            return;
        }

        int index = infoTable.getSelectionIndex();
        fileDataMap.remove(infoTable.getItem(index));
        infoTable.remove(index);

        if (infoTable.getItemCount() > index) {
            infoTable.setSelection(index);
        } else if (infoTable.getItemCount() > 0) {
            infoTable.setSelection(infoTable.getItemCount() - 1);
        }

        if (infoTable.getItemCount() == 0) {
            stationList.setEnabled(false);
            stationHeaderRdo.setEnabled(false);
            stationDataRdo.setEnabled(false);
            stationList.deselectAll();
            delimiterCbo.select(0);
            fileNameValueTxt.setText("");
            dailyTypeRdo.setSelection(true);
            monthlyTypeRdo.setSelection(false);
            monthList.setEnabled(true);
            monthList.deselectAll();
            monthHeaderRdo.setEnabled(true);
            monthHeaderRdo.setSelection(true);
            monthDateRdo.setEnabled(true);
            monthDateRdo.setSelection(false);
            dateFormatCbo.setEnabled(false);
            orderBtn.setEnabled(false);
            addBtn.setEnabled(false);
            saveBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        } else {
            updateDlgDisplay();
        }

        enableImportBtn();
    }

    /**
     * update the 'Import Climate' dialog base on the selected table item in
     * infoTable
     */
    protected void updateDlgDisplay() {
        if (infoTable.getSelectionIndex() < 0) {
            return;
        }
        FileData fileData = fileDataMap
                .get(infoTable.getItem(infoTable.getSelectionIndex()));
        fileNameValueTxt.setText(fileData.getFileName());

        stationHeaderRdo.setEnabled(true);
        stationList.setEnabled(true);
        stationDataRdo.setEnabled(true);

        // station name
        if (fileData.getStationName().equals(FileData.LOCATED_IN_DATA)) {
            stationList.deselectAll();
            stationList.setEnabled(false);
            stationDataRdo.setSelection(true);
            stationHeaderRdo.setSelection(false);
        } else {
            stationList.deselectAll();
            stationList.setEnabled(true);
            stationHeaderRdo.setSelection(true);
            stationDataRdo.setSelection(false);

            for (int i = 0; i < stationList.getItemCount(); i++) {
                if (fileData.getStationName().equals(stationList.getItem(i))) {
                    stationList.select(i);
                    break;
                }
            }
        }

        // Data Type
        dailyTypeRdo.setSelection(fileData.isDaily());
        monthlyTypeRdo.setSelection(!fileData.isDaily());

        // Date Format
        boolean found = false;
        for (int i = 0; i < dateFormatCbo.getItemCount(); i++) {
            if (fileData.getDateFormat().equals(dateFormatCbo.getItem(i))) {
                monthList.deselectAll();
                monthList.setEnabled(false);
                monthHeaderRdo.setSelection(false);
                monthDateRdo.setSelection(true);
                dateFormatCbo.setEnabled(true);
                dateFormatCbo.select(i);
                found = true;
                break;
            }
        }

        if (!found) {
            // It must be in the month list
            monthList.deselectAll();
            monthList.setEnabled(true);
            monthHeaderRdo.setSelection(true);
            monthDateRdo.setSelection(false);
            dateFormatCbo.setEnabled(false);

            for (int i = 0; i < monthList.getItemCount(); i++) {
                if (fileData.getDateFormat().equals(monthList.getItem(i))) {
                    monthList.select(i);
                    break;
                }
            }
        }

        // set delimiter selection
        for (int i = 0; i < delimiters.length; i++) {
            if (delimiters[i].equals(fileData.getDelimiter())) {
                delimiterCbo.select(i);
                break;
            }
        }

        // the buttons
        orderBtn.setEnabled(true);
        addBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        deleteBtn.setEnabled(true);
    }

    /**
     * enable the Save New Values for File button if an item has selected in
     * infoTable
     */
    private void enableSaveBtn() {
        if (infoTable.getSelectionIndex() != -1) {
            saveBtn.setEnabled(true);
            workingFileData = fileDataMap
                    .get(infoTable.getItem(infoTable.getSelectionIndex()));
        } else {
            workingFileData = null;
            saveBtn.setEnabled(false);
        }
    }

    /**
     * enable the Import Data from File(s) button if the fileDataLst had choices
     */
    private void enableImportBtn() {
        boolean enabled = false;
        if (fileDataMap != null) {
            for (FileData fd : fileDataMap.values()) {
                if (fd.getChoices() != null) {
                    enabled = true;
                    break;
                }
            }
        }
        importBtn.setEnabled(enabled);
    }

    /**
     * import data from file(s). Original:
     * appWindImportClimate.C:activateCB_pBImport()
     */
    protected void importDataFromFile() {
        ClimateDataReader cdr = new ClimateDataReader(shell);

        for (FileData fileData : fileDataMap.values()) {
            int savedCount = 0;
            if (fileData.isDaily()) {
                // read file for climate_day
                java.util.List<ClimateDayNorm> dayRecords = cdr
                        .readClimateDay(fileData);
                if (dayRecords != null) {
                    for (ClimateDayNorm dayRecord : dayRecords) {
                        UpdateClimateDayNormNoMissingRequest request = new UpdateClimateDayNormNoMissingRequest(
                                dayRecord);
                        try {
                            ThriftClient.sendRequest(request);
                            savedCount++;
                        } catch (VizException e) {
                            logger.error(
                                    "Failed to save record with station ID="
                                            + dayRecord.getStationId()
                                            + " and day_of_year="
                                            + dayRecord.getDayOfYear(),
                                    e);
                        }
                    }
                }
            } else {// Monthly
                java.util.List<PeriodClimo> monthRecords = cdr
                        .readClimateMonth(fileData);
                if (monthRecords != null) {
                    for (PeriodClimo monthRecord : monthRecords) {
                        UpdateClimateMonthNormNoMissingRequest request = new UpdateClimateMonthNormNoMissingRequest(
                                monthRecord);
                        try {
                            ThriftClient.sendRequest(request);
                            savedCount++;
                        } catch (VizException e) {
                            logger.error("Failed save record with station ID="
                                    + monthRecord.getInformId()
                                    + " and mon_of_year="
                                    + monthRecord.getMonthOfYear(), e);
                        }
                    }
                }
            }
            MessageDialog.openInformation(shell, "Save results",
                    savedCount + " record(s) saved to DB for file "
                            + fileData.getFileName());
        }

        infoTable.removeAll();
        fileDataMap.clear();
        stationList.setEnabled(false);
        stationHeaderRdo.setEnabled(false);
        stationDataRdo.setEnabled(false);
        fileNameValueTxt.setText("");
        // 26405 - Add auto refresh after data import
        climateInitDialog.refreshSelection();
    }

    /**
     * close the import data window
     */
    protected void closeWindow() {
        boolean yesToClose = true;

        if (!fileDataMap.isEmpty()) {
            yesToClose = MessageDialog.openQuestion(shell, "Close Window",
                    "Closing the window will abandon the table list.\nContinue to close?");
        }
        if (yesToClose) {
            close();
        }
    }

    /**
     * Either "Months in Date Field" or "Months in Header" selected.
     */
    protected void changeMonthSelectionType() {
        dateFormatCbo.setEnabled(monthDateRdo.getSelection());
        monthList.setEnabled(monthHeaderRdo.getSelection());

        /*
         * An SWT graphical flaw makes selected items in a list seem to
         * disappear when the list is disabled, so de-select the items to
         * prevent confusion.
         */
        if (!monthList.isEnabled()) {
            monthList.deselectAll();
        }
    }
}

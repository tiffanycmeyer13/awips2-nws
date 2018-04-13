/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.importdata;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;
import gov.noaa.nws.ocp.viz.common.climate.dialog.ClimateCaveDialog;
import gov.noaa.nws.ocp.viz.common.climate.handbook.Handbook;

/**
 * Display the "Import Daily or Monthly Climatology Data" dialog
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ---------- --------------------------
 * 04/29/2016   18469       wkwock     Initial creation.
 * 10/27/2016   20635       wkwock     Clean up
 * 22 DEC 2016  22395       amoore     Correct multi-selection of stations/months; java standards.
 * 25 JAN 2017  26511       wkwock     Implements multiple selection of 'Other Data Not Listed'.
 * 27 FEB 2017  27420       amoore     Use constants. Safe return of constant arrays.
 * 15 MAY 2017  33104       amoore     FindBugs and logic issues. Class rename.
 * 19 SEP 2017  38124       amoore     Use GC for text control sizes.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class DataArrangeDialog extends ClimateCaveDialog {
    /**
     * Choice number for station name.
     */
    public static final int STATION_NAME_CHOICE_NUM = 88;

    /**
     * Choice number for other.
     */
    public static final int OTHER_CHOICE_NUM = 0;

    /**
     * Choice number for date.
     */
    public static final int DATE_CHOICE_NUM = 99;

    /**
     * Date option
     */
    private final static String DATE = "Date";

    /**
     * station name
     */
    private final static String STATION_NAME = "Station Name";

    /**
     * Daily options. Ordering of items matters to retrievers.
     */
    private final static String[] DAILY = { "Mean Maximum Temperature",
            "Record Maximum Temperature",
            "Year(s) Record Maximum Temperature Observed",
            "Mean Minimum Temperature", "Record Minimum Temperature",
            "Year(s) Record Minimum Temperature Observed",
            "Heating Degree Days", "Cooling Degree Days",
            "Average Precipitation", "Record Maximum Precipitation",
            "Year(s) Record Maximum Precip Observed", "Average Daily Snowfall",
            "Record Maximum Snowfall",
            "Year(s) Record Maximum Snowfall Observed", "Daily Snow on Ground",
            "Normal Mean Temperature" };

    /**
     * Monthly options. Ordering of items matters to retrievers.
     */
    private final static String MONTHLY[] = { "Normal Maximum Temperature",
            "Norm No. Days with Max Temp GE 90",
            "Norm No. Days with Max Temp LE 32", "Normal Minimum Temperature",
            "Norm No. Days with Min Temp LE 32",
            "Norm No. Days with Min Temp LE 0",
            "Record Maximum Precipitation Total",
            "Year(s) Record Maximum Precip Observed",
            "Record Minimum Precipitation Total",
            "Year(s) Record Minimum Precip Observed",
            "Norm No. Days with Precip GE 0.01 in",
            "Norm No. Days with Precip GE 0.10 in",
            "Norm No. Days with Precip GE 0.50 in",
            "Norm No. Days with Precip GE 1.00 in",
            "Record Maximum Snowfall Total",
            "Year(s) Record Maximum Snowfall Observed",
            "Record Maximum 24Hr Snowfall Total",
            "Start Date(s) Record Maximum 24Hr Snowfall",
            "End Date(s) Record Maximum 24Hr Snowfall",
            "Normal Water Equivalent of Snow", "Record Snow Depth",
            "Date(s) of Record Snow Depth", "Norm No. Days with Any Snowfall",
            "Norm No. Days with Snowfall GE 1.0 in", "Normal First Freeze Date",
            "Normal Last Freeze Date", "Record First Freeze Date",
            "Record Last Freeze Date" };

    /**
     * the other option
     */
    private final static String OTHERS = "Other Data Not Listed";

    /**
     * option table
     */
    private Table optionTable;

    /**
     * order table
     */
    private Table orderTable;

    /**
     * placement dialog
     */
    private PlacementDialog placementDlg;

    /**
     * choices
     */
    private int choices[] = null;

    /**
     * is daily type
     */
    private boolean isDailyType;

    /**
     * File data to get choices for.
     */
    private final FileData fileData;

    protected DataArrangeDialog(Shell shell, boolean isDailyType,
            FileData fileData) {
        super(shell, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
                CAVE.DO_NOT_BLOCK);
        String hostname = System.getenv("HOSTNAME");
        setText("Import Daily or Monthly Climatology Data (on " + hostname
                + ")");
        this.isDailyType = isDailyType;
        this.fileData = fileData;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        createMenus();
        createMainControls();
        fillInOptions();
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
        MenuItem aboutMI = new MenuItem(helpMenu, SWT.NONE);
        aboutMI.setText("Handbook");
        aboutMI.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                Handbook.displayHandbook("import_climate.html");
            }
        });
    }

    /** create main controls */
    private void createMainControls() {
        GridLayout mainLayout = new GridLayout(3, false);
        shell.setLayout(mainLayout);

        createLeftControls();
        Label separateLbl = new Label(shell, SWT.VERTICAL | SWT.SEPARATOR);
        separateLbl.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        createRightControls();
    }

    /** create controls on left side */
    private void createLeftControls() {
        Composite leftComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        leftComp.setLayout(gl);

        Label dataOptionLbl = new Label(leftComp, SWT.CENTER);
        dataOptionLbl.setText("Climatology Data Options");

        Label separateLbl = new Label(leftComp, SWT.HORIZONTAL | SWT.SEPARATOR);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        optionTable = new Table(leftComp,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        optionTable.setLinesVisible(true);
        optionTable.setHeaderVisible(false);

        GC gc = new GC(optionTable);
        /*
         * Height to fit all data options without scrolling
         */
        int fontHeight = gc.getFontMetrics().getHeight();
        gc.dispose();

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 23 * fontHeight;
        optionTable.setLayoutData(data);

        // Button controls
        Composite buttonComp = new Composite(leftComp, SWT.NONE);
        gl = new GridLayout(3, false);
        gl.marginWidth = 20;
        buttonComp.setLayout(gl);

        Button appendBtn = new Button(buttonComp, SWT.PUSH);
        appendBtn.setText("Append");
        appendBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                appendOption();
            }
        });

        Button placeBtn = new Button(buttonComp, SWT.PUSH);
        placeBtn.setText("Place");
        placeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                placeOption();
            }
        });

        Button appendAllBtn = new Button(buttonComp, SWT.PUSH);
        appendAllBtn.setText("Append All");
        appendAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                appendAllOptions();
            }
        });
    }

    /** create controls on right side */
    private void createRightControls() {
        Composite rightComp = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 20;
        rightComp.setLayout(gl);

        Label dataOptionLbl = new Label(rightComp, SWT.CENTER);
        dataOptionLbl.setText("Order of Data in File");

        Label separateLbl = new Label(rightComp,
                SWT.HORIZONTAL | SWT.SEPARATOR);
        separateLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        orderTable = new Table(rightComp,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

        GC gc = new GC(orderTable);
        /*
         * Height to fit all data options without scrolling
         */
        int fontHeight = gc.getFontMetrics().getHeight();
        int fontWidth = gc.getFontMetrics().getAverageCharWidth();
        gc.dispose();

        orderTable.setLinesVisible(true);
        orderTable.setHeaderVisible(false);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 23 * fontHeight;
        data.widthHint = 62 * fontWidth;
        orderTable.setLayoutData(data);
        TableColumn tc1 = new TableColumn(orderTable, SWT.LEFT);
        TableColumn tc2 = new TableColumn(orderTable, SWT.LEFT);
        tc1.setWidth(14 * fontWidth);
        tc2.setWidth(14 * fontWidth);

        // Button controls
        Composite buttonComp = new Composite(rightComp, SWT.NONE);
        gl = new GridLayout(4, false);
        gl.marginWidth = 20;
        buttonComp.setLayout(gl);

        Button removeBtn = new Button(buttonComp, SWT.PUSH);
        removeBtn.setText("Remove");
        removeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                removeOption();
            }
        });

        Button removeAllBtn = new Button(buttonComp, SWT.PUSH);
        removeAllBtn.setText("Remove ALL");
        removeAllBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                removeAllOptions();
            }
        });

        Button acceptBtn = new Button(buttonComp, SWT.PUSH);
        acceptBtn.setText("Accept");
        acceptBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                acceptOptions();
            }
        });

        Button cancelBtn = new Button(buttonComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /**
     * fill in options to option table and order tables
     */
    private void fillInOptions() {
        String[] options;
        if (isDailyType) {
            options = DAILY;
        } else {
            options = MONTHLY;
        }

        // fill in the order table
        if (fileData.getChoices() == null) {
            // no order has been made for this file yet
            TableItem item = new TableItem(optionTable, SWT.NONE);
            item.setText(DATE);
            if (fileData.getStationName().equals(FileData.LOCATED_IN_DATA)) {
                /*
                 * station is located in data rows, so add station name item to
                 * selectable list
                 */
                item = new TableItem(optionTable, SWT.NONE);
                item.setText(STATION_NAME);
            }
            for (String option : options) {
                item = new TableItem(optionTable, SWT.NONE);
                item.setText(option);
            }
        } else {
            // use existing order for file
            Map<Integer, String> optionsMap = new HashMap<>();
            optionsMap.put(DATE_CHOICE_NUM, DATE);
            if (fileData.getStationName().equals(FileData.LOCATED_IN_DATA)) {
                optionsMap.put(STATION_NAME_CHOICE_NUM, STATION_NAME);
            }
            for (int i = 0; i < options.length; i++) {
                optionsMap.put(i + 1, options[i]);
            }

            int[] choices = fileData.getChoices();
            int orderIndex = 0;
            for (int choice : choices) {
                String choiceName = optionsMap.get(choice);
                optionsMap.remove(choice);
                TableItem item = new TableItem(orderTable, SWT.NONE);
                orderIndex++;
                item.setText(
                        new String[] { "Column " + orderIndex, choiceName });
            }

            // fill in the option table
            for (String choiceName : optionsMap.values()) {
                if (!choiceName.equals(OTHERS)) {
                    // there could be more than one 'OTHERS' in optionsMap
                    TableItem item = new TableItem(optionTable, SWT.NONE);
                    item.setText(choiceName);
                }
            }
        }

        // There should be one and only one 'OTHERS' in optionTable
        TableItem item = new TableItem(optionTable, SWT.NONE);
        item.setText(OTHERS);
    }

    /**
     * Move selected item(s) to 'Order of Data in File'
     */
    protected void appendOption() {
        int orderIndex = orderTable.getItemCount();
        for (int index : optionTable.getSelectionIndices()) {
            String option = optionTable.getItem(index).getText();

            TableItem item = new TableItem(orderTable, SWT.LEFT);
            orderIndex++;
            item.setText(new String[] { "Column " + orderIndex, option });
        }

        int[] indexes = optionTable.getSelectionIndices();
        for (int i = indexes.length - 1; i >= 0; i--) {
            if (!optionTable.getItem(indexes[i]).getText().equals(OTHERS)) {
                optionTable.remove(indexes[i]);
            }
        }
    };

    /**
     * Place selected item to selected column in 'Order of Data in File'
     */
    protected void placeOption() {
        if (placementDlg == null || placementDlg.isDisposed()) {
            placementDlg = new PlacementDialog(shell);
            placementDlg.open();
            placementDlg.getShell().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    insertOptionToOrdertable();
                }
            });
        } else {
            placementDlg.bringToTop();
        }
    }

    /**
     * Insert selected option into order table
     */
    protected void insertOptionToOrdertable() {
        int dstIndex = (int) placementDlg.getReturnValue() - 1;
        if (dstIndex < 0) {
            return;
        } else if (dstIndex > orderTable.getItemCount()) {
            dstIndex = orderTable.getItemCount();
        }

        int srcIndex = optionTable.getSelectionIndex();
        if (srcIndex < 0) {
            return;
        }

        String option = optionTable.getItem(srcIndex).getText();
        if (!option.equals(OTHERS)) {
            optionTable.remove(srcIndex);
        }

        TableItem item = new TableItem(orderTable, SWT.LEFT, dstIndex);
        item.setText(new String[] { "Column", option });

        srcIndex = 1;
        for (TableItem tableItem : orderTable.getItems()) {
            tableItem.setText(0, "Column " + srcIndex);
            srcIndex++;
        }
    }

    /**
     * Move all items to 'Order of Data in File'
     */
    protected void appendAllOptions() {
        TableItem[] items = optionTable.getItems();
        int index = orderTable.getItemCount() + 1;

        for (TableItem item : items) {
            String option = item.getText();

            TableItem newItem = new TableItem(orderTable, SWT.LEFT);
            newItem.setText(new String[] { "Column " + index, option });
            index++;
        }

        for (int i = optionTable.getItemCount() - 1; i >= 0; i--) {
            if (!optionTable.getItem(i).getText().equals(OTHERS)) {
                optionTable.remove(i);
            }
        }
    };

    /**
     * Move selected item(s) back to 'Climatology Data Options' table
     */
    protected void removeOption() {
        for (int index : orderTable.getSelectionIndices()) {
            String option = orderTable.getItem(index).getText(1);

            if (!option.equals(OTHERS)) {
                TableItem item = new TableItem(optionTable, SWT.NONE);
                item.setText(option);
            }
        }

        orderTable.remove(orderTable.getSelectionIndices());

        int index = 1;
        for (TableItem tableItem : orderTable.getItems()) {
            tableItem.setText(0, "Column " + index);
            index++;
        }
    };

    /**
     * Move all items back to 'Climatology Data Options' table
     */
    protected void removeAllOptions() {
        for (TableItem item : orderTable.getItems()) {
            String option = item.getText(1);
            if (!option.equals(OTHERS)) {
                TableItem newItem = new TableItem(optionTable, SWT.NONE);
                newItem.setText(option);
            }
        }
        orderTable.removeAll();
    };

    /**
     * Accept options
     */
    protected void acceptOptions() {
        choices = new int[orderTable.getItemCount()];

        for (int i = 0; i < orderTable.getItemCount(); i++) {
            TableItem item = orderTable.getItem(i);
            if (DATE.equals(item.getText(1))) {
                choices[i] = DATE_CHOICE_NUM;
            } else if (STATION_NAME.equals(item.getText(1))) {
                choices[i] = STATION_NAME_CHOICE_NUM;
            } else if (OTHERS.equals(item.getText(1))) {
                choices[i] = OTHER_CHOICE_NUM;
            }

            if (isDailyType) {
                for (int j = 0; j < DAILY.length; j++) {
                    if (DAILY[j].equals(item.getText(1))) {
                        choices[i] = j + 1;
                        break;
                    }
                }
            } else {
                for (int j = 0; j < MONTHLY.length; j++) {
                    if (MONTHLY[j].equals(item.getText(1))) {
                        choices[i] = j + 1;
                        break;
                    }
                }
            }
        }

        this.setReturnValue(choices);
        close();
    }

    /**
     * get choices
     * 
     * @return
     */
    public int[] getChoices() {
        return choices;
    }

    /**
     * get daily options clone.
     * 
     * @return
     */
    public static String[] getDaily() {
        return DAILY.clone();
    }

    /**
     * get monthly options clone.
     * 
     * @return
     */
    public static String[] getMonthly() {
        return MONTHLY.clone();
    }
}

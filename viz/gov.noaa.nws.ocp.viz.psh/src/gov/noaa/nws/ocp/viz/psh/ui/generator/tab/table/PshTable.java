/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator.tab.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.dataplugin.psh.EffectDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.FloodingDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.MarineDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.MetarDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.NonMetarDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;
import gov.noaa.nws.ocp.common.dataplugin.psh.RainfallDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.TornadoDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.WaterLevelDataEntry;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.MetarStormDataRetrieveRequest;
import gov.noaa.nws.ocp.common.localization.psh.PshCities;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshStation;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.nws.ocp.viz.psh.ui.generator.tab.PshTabComp;
import gov.noaa.nws.ocp.viz.psh.ui.validation.PshAbstractControl;
import gov.noaa.nws.ocp.viz.psh.ui.validation.PshCombo;
import gov.noaa.nws.ocp.viz.psh.ui.validation.PshDateTimeText;
import gov.noaa.nws.ocp.viz.psh.ui.validation.PshNumberText;
import gov.noaa.nws.ocp.viz.psh.ui.validation.PshText;
import gov.noaa.nws.ocp.viz.psh.ui.validation.PshWindText;
import gov.noaa.ocp.viz.psh.data.PshCounty;

/**
 * Composite containing an entry table for several PSH Generator tabs.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 23, 2017 #35463      wpaintsil   Initial creation.
 * Jul 26, 2017 #35463      wpaintsil   Use a ScrolledComposite field 
 *                                      instead of extending ScrolledComposite.
 *                                      Rename to from PshTableComp to PshTable.
 * Aug 22, 2017 #36922      astrakovsky Added autocomplete fields for rainfall and tornadoes.
 * Sep 01, 2017 #36921      wpaintsil   Implementation of add/edit/delete entries in a table;
 *                                      County and Station site selection added to some cells.
 * Sep 12, 2017 #37605      wpaintsil   Implement Retrieve Data button for Metar tab.
 * Sep 12, 2017 #36923      astrakovsky Adjusted autocomplete for rainfall and tornadoes.
 * Sep 19, 2017 #37917      wpaintsil   Refactor algorithm for dropdown lists in the table. 
 *                                      Add Save/Revert functionality.
 * Oct 16, 2017 #39235      astrakovsky Fixed null pointer exception and some formatting issues.
 * Nov 03, 2017 #40067      wpaintsil   Add validation to appropriate text/combo fields.
 * Nov 08, 2017 #40423      jwu         Replace tide/surge with water level.
 * Nov 14, 2017 #40426      jwu         Update GUI with water level.
 * Nov 17, 2017 #39868      wpaintsil   Add up/down arrow buttons to move a selected entry.
 * Nov 27, 2017 #40299      wpaintsil   Add row-sorting functionality.
 * Nov 28, 2017 #41389      astrakovsky Fixed rainfall and tornado autocomplete error.
 * Dec 08, 2017 #41955      astrakovsky Improved editing performance for rainfall/tornado tabs.
 * Dec 10, 2018 DR20982     jwu         Allow user type in for storm "Effects".
 * Dec 18, 2018 DR20978     jwu         Add match cities as auto assist while user is typing in.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public abstract class PshTable {

    /**
     * Enum type indicating which of the tabs this table is in.
     */
    protected PshTabComp tab;

    /**
     * The index of the current selected row. "-1" indicates nothing is selected
     * yet.
     */
    protected int currentSelection = -1;

    /**
     * A Map containing data for each entry in the table. LinkedHashMap
     * maintains order of insertion.
     */
    protected Map<TableItem, StormDataEntry> tableData = new LinkedHashMap<>();

    /**
     * The list of editors in the currently selected row.
     */
    protected List<TableEditor> currentEditorRow = new ArrayList<>();

    protected Button editButton;

    protected Button deleteButton;

    protected Button cancelButton;

    protected Button upButton;

    protected Button downButton;

    protected static final String EDIT_STRING = "Edit";

    protected static final String OK_STRING = "OK";

    protected static final String CLEAR_STRING = "Clear";

    protected static final String DELETE_STRING = "Delete";

    /**
     * Flag for whether the table is being edited (Edit button has been
     * selected).
     */
    protected boolean editing = false;

    /**
     * Flag for whether a new entry is being edited.
     */
    protected boolean newEntry = false;

    protected Composite editComp;

    protected Composite addComp;

    /**
     * Composite holding metar range controls.
     */
    protected Group metarRangeComp;

    /**
     * The table object.
     */
    protected Table table;

    /**
     * Custom columns defined for the table.
     */
    protected PshTableColumn[] columns;

    /**
     * Scrolled composite holding the table.
     */
    protected ScrolledComposite scrollComp;

    private Button addButton;

    private Button radioButton24;

    private Button radioButton48;

    private Button radioButton72;

    private Button saveButton;

    private Button revertButton;

    // Get PSH cities from localization
    private static PshCities pshCities = PshConfigurationManager.getInstance()
            .getCities();

    /**
     * The logger
     */
    protected static final IUFStatusHandler logger = UFStatus
            .getHandler(PshTable.class);

    /**
     * Constructor
     * 
     * @param parent
     * @param columns
     */
    public PshTable(PshTabComp tab, PshTableColumn[] columns) {
        this.columns = columns;
        this.tab = tab;
    }

    /**
     * Create control above the table to an an entry.
     * 
     * @param parent
     */
    public void createTopControls(Composite parent) {
        createTopControls(parent, false);
    }

    /**
     * Create controls above the table. A control to add an entry is present for
     * all tabs.
     * 
     * @param parent
     * @param metarRange
     *            true if the metar range control is present (Metar
     *            Observations)
     */
    public void createTopControls(Composite parent, boolean metarRange) {
        addComp = new Composite(parent, SWT.NONE);
        GridLayout addLayout = new GridLayout(3, false);
        addLayout.marginHeight = 0;
        addComp.setLayout(addLayout);
        GridData addCompData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        addComp.setLayoutData(addCompData);

        addButton = new Button(addComp, SWT.PUSH);
        addButton.setText("Add Entry");

        GridData buttonData = new GridData();
        buttonData.verticalAlignment = SWT.CENTER;
        buttonData.grabExcessVerticalSpace = true;
        addButton.setLayoutData(buttonData);

        if (metarRange) {
            metarRangeComp = new Group(addComp, SWT.SHADOW_IN);
            metarRangeComp.setText("Metar Time Range");
            GridLayout metarRangeLayout = new GridLayout(4, false);
            metarRangeLayout.marginBottom = 5;
            metarRangeLayout.marginTop = 0;
            GridData metarRangeData = new GridData(SWT.RIGHT, SWT.CENTER, false,
                    false);
            metarRangeComp.setLayout(metarRangeLayout);
            metarRangeComp.setLayoutData(metarRangeData);

            radioButton24 = new Button(metarRangeComp, SWT.RADIO);
            radioButton24.setText("24 Hours");
            radioButton24.setSelection(true);
            radioButton48 = new Button(metarRangeComp, SWT.RADIO);
            radioButton48.setText("48 Hours");
            radioButton72 = new Button(metarRangeComp, SWT.RADIO);
            radioButton72.setText("72 Hours");

            radioButton72.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (radioButton72.getSelection()) {
                        new MessageDialog(tab.getShell(), "Database Warning",
                                null,
                                "The METAR database in AWIPS is highly unreliable beyond 48 hours. "
                                        + "It is strongly recommended to double check the database "
                                        + "for the requested station before proceeding.",
                                MessageDialog.WARNING, new String[] { "OK" }, 0)
                                        .open();
                    }
                }
            });

            Button retrieveButton = new Button(metarRangeComp, SWT.PUSH);
            retrieveButton.setText("Retrieve Data");
            retrieveButton.addSelectionListener(retrieveListener());

            metarRangeComp.setEnabled(false);
            for (Control metarControl : metarRangeComp.getChildren()) {
                metarControl.setEnabled(false);
            }
        }

    }

    /**
     * @return a SelectionListener attached to the Retrieve button.
     */
    private SelectionListener retrieveListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (currentEditorRow != null && !currentEditorRow.isEmpty()) {
                    PshCombo stationCombo = (PshCombo) currentEditorRow.get(0)
                            .getEditor();
                    if (stationCombo.getSelectionIndex() >= 0) {

                        List<PshStation> metarStations = PshConfigurationManager
                                .getInstance().getMetarStations().getStations();

                        PshStation selectedStation = metarStations
                                .get(stationCombo.getSelectionIndex());

                        String node = selectedStation.getNode();
                        String station = selectedStation.getCode().substring(1);

                        int period;
                        if (radioButton48 != null
                                && radioButton48.getSelection()) {
                            period = 48;
                        } else if (radioButton72 != null
                                && radioButton72.getSelection()) {
                            period = 72;
                        } else {
                            period = 24;
                        }
                        try {
                            // retrieve metar data from the textdb
                            MetarDataEntry metarData = (MetarDataEntry) ThriftClient
                                    .sendRequest(
                                            new MetarStormDataRetrieveRequest(
                                                    node, period, station));

                            Text minSLPText = (Text) currentEditorRow.get(3)
                                    .getEditor();
                            PshDateTimeText minSLPTimeText = (PshDateTimeText) currentEditorRow
                                    .get(4).getEditor();

                            PshWindText sustWindText = (PshWindText) currentEditorRow
                                    .get(6).getEditor();
                            PshDateTimeText sustWindTimeText = (PshDateTimeText) currentEditorRow
                                    .get(7).getEditor();

                            PshWindText peakWindText = (PshWindText) currentEditorRow
                                    .get(9).getEditor();
                            PshDateTimeText peakWindTimeText = (PshDateTimeText) currentEditorRow
                                    .get(10).getEditor();

                            if (metarData.getMinSeaLevelPres() != null) {
                                minSLPText.setText(
                                        metarData.getMinSeaLevelPres());
                                minSLPTimeText.setText(
                                        metarData.getMinSeaLevelPresTime());
                            }
                            if (metarData.getSustWind() != null) {
                                sustWindText.setText(metarData.getSustWind());
                                sustWindTimeText
                                        .setText(metarData.getSustWindTime());
                            }

                            if (metarData.getPeakWind() != null) {
                                peakWindText.setText(metarData.getPeakWind());
                                peakWindTimeText
                                        .setText(metarData.getPeakWindTime());
                            }

                        } catch (VizException e1) {
                            logger.error(e1.getMessage(), e1);
                        }
                    }
                }
            }
        };
    }

    /**
     * Create the table widget.
     * 
     * @param parent
     */
    public void createTable(Composite parent) {
        createTable(parent, new GridData(SWT.CENTER, SWT.FILL, true, true));
    }

    /**
     * Create the table widget with specified GridData for the ScrolledComposite
     * containing the table.
     * 
     * @param parent
     */
    public void createTable(Composite parent, GridData scrolledData) {
        Composite tableComp = new Composite(parent, SWT.NONE);

        GridLayout tableLayout = new GridLayout(2, false);
        tableLayout.horizontalSpacing = 0;
        tableLayout.marginWidth = 0;
        tableLayout.marginHeight = 0;
        tableComp.setLayout(tableLayout);

        tableComp.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));

        Composite arrowComp = new Composite(tableComp, SWT.NONE);
        GridLayout arrowLayout = new GridLayout(1, false);
        arrowLayout.marginWidth = 0;
        arrowLayout.horizontalSpacing = 0;

        arrowComp.setLayout(arrowLayout);

        upButton = new Button(arrowComp, SWT.ARROW | SWT.UP);
        upButton.setToolTipText("Move Up");
        downButton = new Button(arrowComp, SWT.ARROW | SWT.DOWN);
        downButton.setToolTipText("Move Down");
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        upButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveUp();

            }
        });

        downButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveDown();

            }
        });

        scrollComp = new ScrolledComposite(tableComp, SWT.NONE);
        scrollComp.setLayout(new GridLayout());
        scrollComp.setLayoutData(scrolledData);

        table = new Table(scrollComp, SWT.BORDER | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        scrollComp.setContent(table);
        scrollComp.setExpandHorizontal(true);
        scrollComp.setExpandVertical(true);
        scrollComp.setAlwaysShowScrollBars(false);

        for (int ii = 0; ii < columns.length; ii++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setWidth(columns[ii].getWidth());
            column.setText(columns[ii].getName());
        }

        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                if (editButton != null && !editing) {
                    // Create a new entry in the table and edit it.
                    TableItem newItem = new TableItem(table, SWT.NONE);
                    table.select(table.getItemCount() - 1);
                    newEntry = true;
                    clearRemarks();
                    editButton.notifyListeners(SWT.Selection, new Event());
                    addButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    revertButton.setEnabled(false);
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);

                    table.showItem(newItem);
                }
            }
        });

    }

    /**
     * Clear remarks when a new entry is added/deleted for certain tab types
     * (Flooding, Tornadoes, Effects).
     */
    private void clearRemarks() {
        if (tab != null && !tab.getRemarksText().isEmpty()
                && (tab.getTabType() == PshDataCategory.FLOODING
                        || tab.getTabType() == PshDataCategory.EFFECT
                        || tab.getTabType() == PshDataCategory.TORNADO)) {
            tab.setRemarksText("");
        }
    }

    /**
     * Cancel the editing action
     */
    public void cancelEditing() {
        if (editing) {
            cancelButton.notifyListeners(SWT.Selection, new Event());
        }

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        table.deselectAll();
    }

    /**
     * Add a SelectionListener to the table.
     * 
     * @param listener
     */
    public void addSelectionListener(SelectionListener listener) {
        if (table != null) {
            table.addSelectionListener(listener);
        }
    }

    /**
     * 
     * @return the index of the currently selected row in the table; return -1
     *         if the are none.
     */
    public int getSelectionIndex() {
        return table != null ? table.getSelectionIndex() : -1;
    }

    /**
     * Add a new entry to the end of the table.
     * 
     * @param data
     *            StormData to be added to the table
     * 
     */
    public void addItem(StormDataEntry data) {
        addItem(data, -1);
    }

    /**
     * Add a new entry to the table at a specific index.
     * 
     * @param data
     *            StormData to be added to the table
     * @param tableIndex
     *            index at which to add the item
     * 
     */
    public abstract void addItem(StormDataEntry data, int tableIndex);

    /**
     * Create Edit and Delete button that toggle to OK, Clear, and Cancel
     * buttons.
     * 
     * @param parent
     */
    public void createBottomControls(Composite parent) {
        editComp = new Composite(parent, SWT.NONE);
        GridLayout editLayout = new GridLayout(4, false);
        editComp.setLayout(editLayout);
        GridData editCompData = new GridData(SWT.LEFT, SWT.DEFAULT, false,
                false);
        editComp.setLayoutData(editCompData);

        editButton = new Button(editComp, SWT.PUSH);
        editButton.setText(EDIT_STRING);
        editButton.setEnabled(false);

        table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editButton.setEnabled(true);
                deleteButton.setEnabled(true);
                upButton.setEnabled(true);
                downButton.setEnabled(true);

                if (!editing) {
                    updateRemarks();
                }

                if (editing) {
                    table.deselectAll();
                }
            }
        });

        // Show controls only when a row is selected.
        editButton.addSelectionListener(editListener());

        deleteButton = new Button(editComp, SWT.PUSH);
        deleteButton.setText(DELETE_STRING);
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(deleteListener());

        cancelButton = new Button(editComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setVisible(false);
        cancelButton.addSelectionListener(cancelListener());

        // Create save and revert buttons
        createSaveArea(editComp);
    }

    /**
     * @return a SelectionListener attached to the Edit button.
     */
    private SelectionListener editListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // toggle between an edit and ok button
                if (editButton.getText().equals(EDIT_STRING)) {
                    editing = true;
                    editButton.setText(OK_STRING);
                    editButton.setEnabled(true);
                    addButton.setEnabled(false);
                    saveButton.setEnabled(false);
                    revertButton.setEnabled(false);
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);

                    if (tab.getTabType() == PshDataCategory.FLOODING
                            || tab.getTabType() == PshDataCategory.TORNADO
                            || tab.getTabType() == PshDataCategory.EFFECT) {
                        tab.setRemarksTextEditable(true);
                    }

                    if (deleteButton != null) {
                        deleteButton.setText(CLEAR_STRING);
                        deleteButton.setEnabled(true);
                    }
                    if (cancelButton != null) {
                        cancelButton.setVisible(true);
                    }

                    if (metarRangeComp != null) {
                        metarRangeComp.setEnabled(true);
                        for (Control metarControl : metarRangeComp
                                .getChildren()) {
                            metarControl.setEnabled(true);
                        }
                    }

                    int index = table.getSelectionIndex();
                    currentSelection = index;
                    table.deselectAll();

                    for (int jj = 0; jj < columns.length; jj++) {
                        TableEditor editor = new TableEditor(table);
                        editor.grabHorizontal = true;
                        editor.grabVertical = true;
                        editor.horizontalAlignment = SWT.CENTER;
                        Control control;

                        TableItem[] items = table.getItems();

                        switch (columns[jj].getControlType()) {

                        case COMBO:

                            control = new CCombo(table,
                                    SWT.BORDER | SWT.READ_ONLY);

                            // DR 20982: allow user type-in for
                            // Injuries/Deaths/Evacuation numbers
                            if (tab.getTabType() == PshDataCategory.EFFECT) {
                                ((CCombo) control).setEditable(true);
                            }

                            selectComboItem((CCombo) control,
                                    columns[jj].getDropdownList(),
                                    items[index].getText(jj));

                            break;

                        case NONEMPTY_COMBO:

                            control = new PshCombo(table);

                            selectComboItem(((PshCombo) control).getTextCombo(),
                                    columns[jj].getDropdownList(),
                                    items[index].getText(jj));
                            break;

                        case CHECKBOX:
                            control = new Button(table, SWT.CHECK);

                            // translate contents of cell to checkbox state
                            ((Button) control).setSelection(items[index]
                                    .getText(jj).equals(columns[jj].getName()));
                            break;

                        case WIND_TEXT:
                            if (columns[jj].getName()
                                    .equals(PshTabComp.SUST_WIND_LBL_STRING)) {
                                control = new PshWindText(table, false);
                            } else {
                                control = new PshWindText(table, true);
                            }
                            ((PshWindText) control)
                                    .setText(items[index].getText(jj));
                            break;

                        case DATETIME_TEXT:
                            control = new PshDateTimeText(table);
                            ((PshDateTimeText) control)
                                    .setText(items[index].getText(jj));
                            break;

                        case NUMBER_TEXT:
                            control = new PshNumberText(table);
                            ((PshNumberText) control)
                                    .setText(items[index].getText(jj));
                            break;

                        case NONEMPTY_TEXT:
                            control = new PshText(table);
                            ((PshText) control)
                                    .setText(items[index].getText(jj));
                            break;

                        default:// Just use a text field by default
                            control = new Text(table, SWT.BORDER);
                            ((Text) control).setText(items[index].getText(jj));

                            // Tooltip for ANEMHGT field
                            if (columns[jj].getName()
                                    .equals(PshTabComp.ANEMHGT_LBL_STRING)) {
                                ((Text) control).setToolTipText(
                                        "XX/YY Anemometer Height in Meters/Time in Minutes\nExample:10/02");
                            }

                            break;
                        }

                        // Match city in entry to city in localization
                        boolean foundCity = false;
                        List<PshCity> cityList = pshCities.getCities();
                        if (jj == 0 && (tab.getTabType()
                                .equals(PshDataCategory.RAINFALL)
                                || tab.getTabType()
                                        .equals(PshDataCategory.TORNADO))) {
                            for (PshCity city : cityList) {
                                if (city.getName().equalsIgnoreCase(
                                        ((PshText) control).getText())) {
                                    foundCity = true;
                                    control.setData(city);
                                    break;
                                }
                            }

                            /*
                             * City in entry from LSR report may be slightly
                             * different / more specific than the city in
                             * localization. This will check again for a partial
                             * match if an exact one was not found.
                             */
                            if (!foundCity) {
                                for (PshCity city : cityList) {
                                    if (((PshText) control).getText()
                                            .toUpperCase().contains(city
                                                    .getName().toUpperCase())) {
                                        foundCity = true;
                                        control.setData(city);
                                        break;
                                    }
                                }
                            }
                        }

                        editor.setEditor(control, items[index], jj);
                        currentEditorRow.add(editor);
                    }

                    autoPopulateLatLon();
                    autoPopulateGaugeStation();
                    autocompleteFields();

                } else if (validInput()) {
                    editing = false;
                    editButton.setText(EDIT_STRING);
                    table.select(currentSelection);

                    if (deleteButton != null) {
                        deleteButton.setText(DELETE_STRING);
                    }
                    if (cancelButton != null) {
                        cancelButton.setVisible(false);
                    }

                    if (metarRangeComp != null) {
                        metarRangeComp.setEnabled(false);
                        for (Control metarControl : metarRangeComp
                                .getChildren()) {
                            metarControl.setEnabled(false);
                        }
                    }

                    // save row that was being edited and dispose controls.
                    setRow();

                    newEntry = false;
                    addButton.setEnabled(true);
                    saveButton.setEnabled(true);
                    revertButton.setEnabled(true);
                    upButton.setEnabled(table.getSelectionCount() > 0);
                    downButton.setEnabled(table.getSelectionCount() > 0);
                    editButton.setEnabled(table.getSelectionCount() > 0);
                    deleteButton.setEnabled(table.getSelectionCount() > 0);

                    if (tab.getTabType() == PshDataCategory.FLOODING
                            || tab.getTabType() == PshDataCategory.TORNADO
                            || tab.getTabType() == PshDataCategory.EFFECT) {
                        tab.setRemarksTextEditable(false);
                    }
                }
            }

        };
    }

    protected boolean validInput() {
        boolean validInput = true;
        if (!currentEditorRow.isEmpty()) {
            // copy editor contents to table, then dispose editors
            int ii = 0;
            for (TableEditor editor : currentEditorRow) {
                // handle different editor types
                switch (columns[ii].getControlType()) {
                case NONEMPTY_COMBO:
                case WIND_TEXT:
                case DATETIME_TEXT:
                case NUMBER_TEXT:
                case NONEMPTY_TEXT:
                    // true only if all input fields are valid
                    validInput = validInput
                            && ((PshAbstractControl) editor.getEditor())
                                    .highlightInput();
                    break;

                default:
                    // do nothing
                    break;
                }
                ii++;
            }

        }

        if (!validInput) {
            new MessageDialog(tab.getShell(), "Invalid Input", null,
                    "There is missing or invalid data in the highlighted fields.",
                    MessageDialog.ERROR, new String[] { "OK" }, 0).open();
        }

        return validInput;
    }

    /**
     * @return a SelectionListener attached to the Delete button.
     */
    private SelectionListener deleteListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (deleteButton.getText().equals(DELETE_STRING)
                        && table.getSelectionCount() > 0) {
                    boolean confirm = new MessageDialog(tab.getShell(),
                            "Confirm Delete", null,
                            "Are you sure you want to delete this entry?",
                            MessageDialog.QUESTION,
                            new String[] { IDialogConstants.YES_LABEL,
                                    IDialogConstants.NO_LABEL },
                            1).open() == MessageDialog.OK;

                    if (confirm) {
                        deleteRow(table.getSelectionIndex());

                        if (table.getSelectionCount() < 1) {
                            editButton.setEnabled(false);
                            deleteButton.setEnabled(false);
                            upButton.setEnabled(false);
                            downButton.setEnabled(false);
                        }
                    }

                } else {
                    // Clear Button (if getText() != DELETE_STRING)
                    for (int ii = 0; ii < currentEditorRow.size(); ii++) {
                        Control editorControl = currentEditorRow.get(ii)
                                .getEditor();
                        if (editorControl instanceof Text) {
                            ((Text) editorControl).setText("");
                        } else if (editorControl instanceof PshAbstractControl) {
                            ((PshAbstractControl) editorControl).setText("");
                        } else if (editorControl instanceof CCombo) {
                            ((CCombo) editorControl).setText("");
                        } else if (editorControl instanceof Button) {
                            ((Button) editorControl).setSelection(false);
                        }
                    }
                }

            }
        };
    }

    /**
     * @return a SelectionListener attached to the Cancel button.
     */
    private SelectionListener cancelListener() {
        return new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                editing = false;
                editButton.setText(EDIT_STRING);
                table.select(currentSelection);

                addButton.setEnabled(true);
                saveButton.setEnabled(true);
                revertButton.setEnabled(true);
                upButton.setEnabled(true);
                downButton.setEnabled(true);

                if (deleteButton != null) {
                    deleteButton.setText(DELETE_STRING);
                }
                if (cancelButton != null) {
                    cancelButton.setVisible(false);
                }
                if (metarRangeComp != null) {
                    metarRangeComp.setEnabled(false);
                    for (Control metarControl : metarRangeComp.getChildren()) {
                        metarControl.setEnabled(false);
                    }
                }

                for (int ii = 0; ii < currentEditorRow.size(); ii++) {
                    currentEditorRow.get(ii).getEditor().dispose();
                    currentEditorRow.get(ii).dispose();
                }
                currentEditorRow.clear();

                // If this is editing a new entry triggered by the "Add Entry"
                // button, remove the new entry if editing is cancelled.
                if (newEntry) {
                    if (currentSelection >= 0) {
                        table.remove(currentSelection);
                    }
                    if (table.getSelectionCount() < 1) {
                        editButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                        upButton.setEnabled(false);
                        downButton.setEnabled(false);
                    }
                    newEntry = false;
                    clearRemarks();
                }

                if (tab.getTabType() == PshDataCategory.FLOODING
                        || tab.getTabType() == PshDataCategory.TORNADO
                        || tab.getTabType() == PshDataCategory.EFFECT) {
                    tab.setRemarksTextEditable(false);
                }
            }
        };
    }

    /**
     * Create a portion with save and cancel buttons.
     * 
     * @return
     */
    private void createSaveArea(Composite parent) {
        Composite saveComp = new Composite(parent, SWT.NONE);
        GridLayout saveLayout = new GridLayout(2, true);
        saveComp.setLayout(saveLayout);
        GridData saveData = new GridData(SWT.RIGHT, SWT.CENTER, true, false);

        saveComp.setLayoutData(saveData);

        saveButton = new Button(saveComp, SWT.PUSH);
        saveButton.setText("Save Changes");
        saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        saveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                tab.savePshData(
                        new ArrayList<StormDataEntry>(tableData.values()));

            }
        });

        revertButton = new Button(saveComp, SWT.PUSH);
        revertButton.setText("Revert Changes");
        revertButton
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        revertButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                cancelEditing();
                tab.clearTable();
                table.setSortDirection(SWT.NONE);
                tab.setDataList();

            }
        });

    }

    /**
     * Delete a row in the table.
     * 
     * @param rowIndex
     */
    public void deleteRow(int rowIndex) {
        if (table != null && tableData != null) {
            clearRemarks();
            tableData.remove(table.getItem(rowIndex));
            table.remove(rowIndex);
            tab.updatePreviewArea();
        }
    }

    /**
     * @return the number of items in the table.
     */
    public int size() {
        return table.getItemCount();
    }

    /**
     * When editing a row in the Metar, Non-metar, or Marine tabs, populate the
     * lat/lon fields when a station is selected.
     */
    private void autoPopulateLatLon() {
        if ((tab.getTabType() == PshDataCategory.METAR
                || tab.getTabType() == PshDataCategory.NON_METAR
                || tab.getTabType() == PshDataCategory.MARINE) && editing) {
            PshCombo stationCombo = (PshCombo) currentEditorRow.get(0)
                    .getEditor();
            PshNumberText latText = (PshNumberText) currentEditorRow.get(1)
                    .getEditor();
            PshNumberText lonText = (PshNumberText) currentEditorRow.get(2)
                    .getEditor();

            int curSelection = stationCombo.getSelectionIndex();
            stationCombo.addSelectionListener(new SelectionAdapter() {

                @SuppressWarnings("incomplete-switch")
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // clear the fields other than lat/lon when switching
                    // stations
                    if (curSelection != stationCombo.getSelectionIndex()) {
                        for (int ii = 3; ii < currentEditorRow.size(); ii++) {
                            Control editorField = currentEditorRow.get(ii)
                                    .getEditor();
                            if (editorField instanceof Button) {
                                ((Button) editorField).setSelection(false);
                            } else if (editorField instanceof PshAbstractControl) {
                                ((PshAbstractControl) editorField).setText("");
                            } else if (editorField instanceof Text) {
                                ((Text) editorField).setText("");
                            }
                        }
                    }

                    switch (tab.getTabType()) {

                    case METAR:
                        List<PshStation> metarStations = PshConfigurationManager
                                .getInstance().getMetarStations().getStations();

                        PshStation selectedStation = metarStations
                                .get(stationCombo.getSelectionIndex());
                        latText.setText(
                                String.valueOf(selectedStation.getLat()));
                        lonText.setText(
                                String.valueOf(selectedStation.getLon()));

                        break;

                    case NON_METAR:
                        List<PshStation> nonMetarStations = PshConfigurationManager
                                .getInstance().getNonMetarStations()
                                .getStations();
                        PshStation nonMetarStation = nonMetarStations
                                .get(stationCombo.getSelectionIndex());
                        latText.setText(
                                String.valueOf(nonMetarStation.getLat()));
                        lonText.setText(
                                String.valueOf(nonMetarStation.getLon()));

                        break;

                    case MARINE:
                        List<PshStation> marineStations = PshConfigurationManager
                                .getInstance().getMarineStations()
                                .getStations();
                        PshStation marineStation = marineStations
                                .get(stationCombo.getSelectionIndex());
                        latText.setText(String.valueOf(marineStation.getLat()));
                        lonText.setText(String.valueOf(marineStation.getLon()));

                        break;

                    }
                }
            });
        }
    }

    /**
     * Set the remarks text for the tabs that have a remark for each entry, each
     * time a row is selected.
     */
    public void updateRemarks() {

        if (tab.getTabType() == PshDataCategory.FLOODING
                || tab.getTabType() == PshDataCategory.EFFECT
                || tab.getTabType() == PshDataCategory.TORNADO) {

            TableItem selectedItem = table.getItem(table.getSelectionIndex());
            String remarks = "";
            if (tableData.containsKey(selectedItem)) {
                StormDataEntry dataItem = tableData.get(selectedItem);
                switch (tab.getTabType()) {
                case FLOODING:
                    remarks = ((FloodingDataEntry) dataItem).getRemarks();
                    break;

                case TORNADO:
                    remarks = ((TornadoDataEntry) dataItem).getRemarks();
                    break;

                case EFFECT:
                    remarks = ((EffectDataEntry) dataItem).getRemarks();
                    break;

                default:
                    // do nothing
                    break;
                }
            }

            tab.setRemarksText(remarks);
        }
    }

    /**
     * Finish editing the current row and dispose editors.
     */
    private void setRow() {

        if (!currentEditorRow.isEmpty()) {

            // get table item being edited
            TableItem item = currentEditorRow.get(0).getItem();

            // copy editor contents to table, then dispose editors
            int ii = 0;
            for (TableEditor editor : currentEditorRow) {

                // handle different editor types
                switch (columns[ii].getControlType()) {
                case COMBO:
                    item.setText(ii, ((CCombo) editor.getEditor()).getText());
                    break;
                case NONEMPTY_COMBO:
                    item.setText(ii, ((PshCombo) editor.getEditor()).getText());
                    break;
                case TEXT:
                    item.setText(ii, ((Text) editor.getEditor()).getText());
                    break;
                case WIND_TEXT:
                    item.setText(ii,
                            ((PshWindText) editor.getEditor()).getText());
                    break;
                case DATETIME_TEXT:
                    item.setText(ii,
                            ((PshDateTimeText) editor.getEditor()).getText());
                    break;
                case NUMBER_TEXT:
                    item.setText(ii,
                            ((PshNumberText) editor.getEditor()).getText());
                    break;
                case NONEMPTY_TEXT:
                    item.setText(ii, ((PshText) editor.getEditor()).getText());
                    break;
                case CHECKBOX:
                    item.setText(ii,
                            ((Button) editor.getEditor()).getSelection()
                                    ? columns[ii].getName() : "");
                    break;

                default:
                    // do nothing
                    break;
                }

                editor.getEditor().dispose();
                editor.dispose();
                ii++;
            }

            setData(item);
            currentEditorRow.clear();

        }

    }

    /**
     * Map a StormData object to a newly added TableItem.
     * 
     * @param item
     */
    protected void setData(TableItem item) {

        switch (tab.getTabType()) {

        case METAR:
            MetarDataEntry stormData = new MetarDataEntry();

            stormData.setSite(item.getText(0));
            stormData.setLat(PshUtil.parseFloat(item.getText(1)));
            stormData.setLon(PshUtil.parseFloat(item.getText(2)));
            stormData.setMinSeaLevelPres(item.getText(3));
            stormData.setMinSeaLevelPresTime(item.getText(4));
            stormData.setMinSeaLevelComplete(item.getText(5));
            stormData.setSustWind(item.getText(6));
            stormData.setSustWindTime(item.getText(7));
            stormData.setSustWindComplete(item.getText(8));
            stormData.setPeakWind(item.getText(9));
            stormData.setPeakWindTime(item.getText(10));
            stormData.setPeakWindComplete(item.getText(11));

            tableData.put(item, stormData);
            tab.updatePreviewArea();
            break;

        case NON_METAR:
            NonMetarDataEntry nonMetarData = new NonMetarDataEntry();

            nonMetarData.setSite(item.getText(0));
            nonMetarData.setLat(PshUtil.parseFloat(item.getText(1)));
            nonMetarData.setLon(PshUtil.parseFloat(item.getText(2)));
            nonMetarData.setMinSeaLevelPres(item.getText(3));
            nonMetarData.setMinSeaLevelPresTime(item.getText(4));
            nonMetarData.setMinSeaLevelComplete(item.getText(5));
            nonMetarData.setSustWind(item.getText(6));
            nonMetarData.setSustWindTime(item.getText(7));
            nonMetarData.setSustWindComplete(item.getText(8));
            nonMetarData.setPeakWind(item.getText(9));
            nonMetarData.setPeakWindTime(item.getText(10));
            nonMetarData.setPeakWindComplete(item.getText(11));
            nonMetarData.setEstWind(item.getText(12));
            nonMetarData.setAnemHgmt(item.getText(13));

            tableData.put(item, nonMetarData);
            tab.updatePreviewArea();
            break;

        case MARINE:
            MarineDataEntry marineData = new MarineDataEntry();

            marineData.setSite(item.getText(0));
            marineData.setLat(PshUtil.parseFloat(item.getText(1)));
            marineData.setLon(PshUtil.parseFloat(item.getText(2)));
            marineData.setMinSeaLevelPres(item.getText(3));
            marineData.setMinSeaLevelPresTime(item.getText(4));
            marineData.setMinSeaLevelComplete(item.getText(5));
            marineData.setSustWind(item.getText(6));
            marineData.setSustWindTime(item.getText(7));
            marineData.setSustWindComplete(item.getText(8));
            marineData.setPeakWind(item.getText(9));
            marineData.setPeakWindTime(item.getText(10));
            marineData.setPeakWindComplete(item.getText(11));
            marineData.setAnemHgmt(item.getText(12));

            tableData.put(item, marineData);
            tab.updatePreviewArea();
            break;

        case RAINFALL:
            RainfallDataEntry rainfallData = new RainfallDataEntry();

            PshCity city = new PshCity(item.getText(0), item.getText(4), "",
                    PshUtil.parseFloat(item.getText(2)),
                    PshUtil.parseFloat(item.getText(3)), item.getText(1), "",
                    "");
            rainfallData.setCity(city);

            rainfallData.setRainfall(PshUtil.parseFloat(item.getText(5)));
            rainfallData.setDirection(item.getText(6));
            rainfallData.setDistance(PshUtil.parseFloat(item.getText(7)));

            // convert empty field to dash in report
            if (item.getText(8).isEmpty()) {
                rainfallData.setIncomplete("");
            } else {
                rainfallData.setIncomplete(item.getText(8));
            }

            tableData.put(item, rainfallData);
            tab.updatePreviewArea();
            break;

        case FLOODING:
            FloodingDataEntry floodData = new FloodingDataEntry();

            floodData.setCounty(item.getText(0));
            floodData.setRemarks(tab.getRemarksText());

            tableData.put(item, floodData);
            tab.updatePreviewArea();
            break;

        case WATER_LEVEL:
            WaterLevelDataEntry waterLevelData = new WaterLevelDataEntry();

            PshCity city0 = new PshCity(item.getText(0), item.getText(2),
                    item.getText(3), PshUtil.parseFloat(item.getText(4)),
                    PshUtil.parseFloat(item.getText(5)), item.getText(1), "",
                    "G");

            waterLevelData.setLocation(city0);

            waterLevelData.setWaterLevel(PshUtil.parseFloat(item.getText(6)));
            waterLevelData.setDatum(item.getText(7));
            waterLevelData.setDatetime(item.getText(8));
            waterLevelData.setSource(item.getText(9));

            // convert empty field to dash in report
            if (item.getText(10).isEmpty()) {
                waterLevelData.setIncomplete("");
            } else {
                waterLevelData.setIncomplete(item.getText(10));
            }

            tableData.put(item, waterLevelData);
            tab.updatePreviewArea();
            break;

        case TORNADO:

            TornadoDataEntry tornadoData = new TornadoDataEntry();
            PshCity city1 = new PshCity(item.getText(0), item.getText(1), "",
                    PshUtil.parseFloat(item.getText(4)),
                    PshUtil.parseFloat(item.getText(5)), "", "", "");
            tornadoData.setLocation(city1);

            tornadoData.setMagnitude(item.getText(2));
            tornadoData.setDatetime(item.getText(3));
            tornadoData.setDirection(item.getText(6));
            tornadoData.setDistance(PshUtil.parseFloat(item.getText(7)));

            // convert empty field to dash in report
            if (item.getText(8).isEmpty()) {
                tornadoData.setIncomplete("-");
            } else {
                tornadoData.setIncomplete(item.getText(8));
            }
            tornadoData.setRemarks(tab.getRemarksText());

            tableData.put(item, tornadoData);
            tab.updatePreviewArea();
            break;

        case EFFECT:
            EffectDataEntry effectData = new EffectDataEntry();

            effectData.setCounty(item.getText(0));
            effectData.setDeaths(PshUtil.parseInt(item.getText(1)));
            effectData.setInjuries(PshUtil.parseInt(item.getText(2)));
            effectData.setEvacuations(PshUtil.parseInt(item.getText(3)));
            effectData.setRemarks(tab.getRemarksText());

            tableData.put(item, effectData);
            tab.updatePreviewArea();
            break;

        default:
            // Do nothing
            break;
        }
    }

    /**
     * Return a list of storm data currently displayed in the table.
     * 
     * @return
     */
    public <T> List<T> getTableData(Class<T> c) {
        List<T> stormData = new ArrayList<>();
        for (StormDataEntry data : tableData.values()) {
            stormData.add(c.cast(data));
        }

        return stormData;
    }

    /**
     * Autocomplete data from other fields if applicable
     */
    private void autocompleteFields() {

        // rainfall tab
        if (tab.getTabType().equals(PshDataCategory.RAINFALL)) {

            // pass rainfall fields
            autocompleteRainfallTornadoes(
                    (PshNumberText) currentEditorRow.get(2).getEditor(),
                    (PshNumberText) currentEditorRow.get(3).getEditor(),
                    (PshText) currentEditorRow.get(0).getEditor(),
                    (Text) currentEditorRow.get(4).getEditor(),
                    (CCombo) currentEditorRow.get(6).getEditor(),
                    (PshNumberText) currentEditorRow.get(7).getEditor(),
                    (Text) currentEditorRow.get(1).getEditor());

        }

        // tornadoes tab
        else if (tab.getTabType().equals(PshDataCategory.TORNADO)) {

            // pass tornadoes fields
            autocompleteRainfallTornadoes(
                    (PshNumberText) currentEditorRow.get(4).getEditor(),
                    (PshNumberText) currentEditorRow.get(5).getEditor(),
                    (PshText) currentEditorRow.get(0).getEditor(),
                    (Text) currentEditorRow.get(1).getEditor(),
                    (CCombo) currentEditorRow.get(6).getEditor(),
                    (PshNumberText) currentEditorRow.get(7).getEditor()
                    , null);

        }

    }

    /**
     * Autocomplete fields for Storm Rainfall or Tornadoes tabs.
     * 
     * @param latField
     * @param lonField
     * @param cityField
     * @param countyField
     * @param dirField
     * @param distField
     */
    private void autocompleteRainfallTornadoes(PshNumberText latField,
            PshNumberText lonField, PshText cityField, Text countyField,
            CCombo dirField, PshNumberText distField, Text idField) {

        /*
         * DR 20978 - Add match cities as auto assist while user is typing in to
         * avoid errors.
         */
        AutoCompleteField cityLookup = new AutoCompleteField(
                cityField.getTextField(), new TextContentAdapter(),
                new String[] {});


        // get PSH counties with geometry
        List<PshCounty> counties = PshUtil.getCountyGeodata();

        latField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                // only look for city if focused and lat/lon is not empty
                if (latField.isFocusControl()
                        && NumberUtils.isNumber(latField.getText())
                        && NumberUtils.isNumber(lonField.getText())) {
                    autocompleteLatLon(pshCities, counties,
                            Double.parseDouble(latField.getText()),
                            Double.parseDouble(lonField.getText()), cityField,
                            countyField, dirField, distField);
                }

            }

        });

        lonField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                // only look for city if focused and lat/lon is not empty
                if (lonField.isFocusControl()
                        && NumberUtils.isNumber(latField.getText())
                        && NumberUtils.isNumber(lonField.getText())) {
                    autocompleteLatLon(pshCities, counties,
                            Double.parseDouble(latField.getText()),
                            Double.parseDouble(lonField.getText()), cityField,
                            countyField, dirField, distField);
                }

            }

        });

        cityField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {

                // only look for point if focused
                if (cityField.isFocusControl()) {

                    // Update the content of the auto-complete field.
                    String[] cs = findCities(cityField.getText());
                    cityLookup.setProposals(cs);

                    autocompleteCity(pshCities, counties, latField, lonField,
                            cityField, countyField, dirField, distField,
                            idField);
                }

            }

        });

        dirField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {

                // only look for point if focused and dist/dir is not empty
                if (dirField.isFocusControl()
                        && PshUtil.stringToAngle(dirField.getText()) != 9999
                        && NumberUtils.isNumber(distField.getText())) {

                    autocompleteDistDir(counties, latField, lonField, cityField,
                            countyField,
                            PshUtil.stringToAngle(dirField.getText()),
                            Double.parseDouble(distField.getText()));
                }

            }

        });

        distField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {

                // only look for point if focused and dist/dir is not empty
                if (distField.isFocusControl()
                        && PshUtil.stringToAngle(dirField.getText()) != 9999.0
                        && NumberUtils.isNumber(distField.getText())) {

                    autocompleteDistDir(counties, latField, lonField, cityField,
                            countyField,
                            PshUtil.stringToAngle(dirField.getText()),
                            Double.parseDouble(distField.getText()));
                }

            }

        });

    }

    /**
     * Autocomplete from lat/lon
     * 
     * @param cities
     * @param counties
     * @param lat
     * @param lon
     * @param cityField
     * @param countyField
     * @param dirField
     * @param distField
     */
    private void autocompleteLatLon(PshCities cities, List<PshCounty> counties,
            double lat, double lon, PshText cityField, Text countyField,
            CCombo dirField, PshNumberText distField) {

        // find closest city to location and store
        PshCity closest = PshUtil.closestCity(lat, lon, cities);
        cityField.setText(closest.getName());
        cityField.setData(closest);

        // get distance between location and closest city
        double distance = PshUtil.latLonDistanceMiles(lat, lon,
                closest.getLat(), closest.getLon());
        distField.setText(Math.round(distance) + "");

        // get direction from closest city to location
        double direction = PshUtil.latLonDirection(closest.getLat(),
                closest.getLon(), lat, lon);
        dirField.setText(PshUtil.angleToString(direction));

        // find and set county
        boolean countyFound = false;
        for (PshCounty county : counties) {
            if (county.contains(lat, lon)) {
                countyField.setText(county.getName().toUpperCase());
                countyFound = true;
                break;
            }
        }
        if (!countyFound) {
            countyField.setText("------");
        }

    }

    /**
     * Autocomplete from dist/dir
     * 
     * @param counties
     * @param latField
     * @param lonField
     * @param cityField
     * @param countyField
     * @param direction
     * @param distance
     */
    private void autocompleteDistDir(List<PshCounty> counties,
            PshNumberText latField, PshNumberText lonField, PshText cityField,
            Text countyField, double direction, double distance) {

        // make sure city is already set
        if (cityField.getData() != null
                && cityField.getData().getClass().equals(PshCity.class)) {

            // get closest city from field
            PshCity closestCity = ((PshCity) cityField.getData());

            // compute coordinate from distance and direction
            Coordinate location = PshUtil.computePointMiles(
                    closestCity.getLat(), closestCity.getLon(), distance,
                    direction);

            // set lat and lon fields
            latField.setText(location.y + "");
            lonField.setText(location.x + "");

            // find and set county
            boolean countyFound = false;
            for (PshCounty county : counties) {
                if (county.contains(location)) {
                    countyField.setText(county.getName().toUpperCase());
                    countyFound = true;
                    break;
                }
            }
            if (!countyFound) {
                countyField.setText("------");
            }

        }

    }

    /**
     * Autocomplete from city
     * 
     * @param cities
     * @param counties
     * @param latField
     * @param lonField
     * @param cityField
     * @param countyField
     * @param dirField
     * @param distField
     */
    private void autocompleteCity(PshCities cities, List<PshCounty> counties,
            PshNumberText latField, PshNumberText lonField, PshText cityField,
            Text countyField, CCombo dirField, PshNumberText distField,
            Text idField) {

        // check if valid city entered
        boolean cityFound = false;
        for (PshCity city : cities.getCities()) {
            if (city.getName().equalsIgnoreCase(cityField.getText())) {
                cityField.setData(city);
                cityFound = true;

                // Auto-complete lat/lon/county/id.
                latField.setText(String.valueOf(city.getLat()));
                lonField.setText(String.valueOf(city.getLon()));
                countyField.setText(city.getCounty());
                if (idField != null) {
                    idField.setText(city.getStationID());
                }

                break;
            }
        }

        // unset if not valid
        if (!cityFound) {
            cityField.setData(null);
        }
        // clear all fields but dist and dir if empty
        if (cityField.getText().isEmpty()) {
            latField.setText("");
            lonField.setText("");
            countyField.setText("");
        }

        // find lat/lon and county from dist/dir if filled in
        if (PshUtil.stringToAngle(dirField.getText()) != 9999
                && NumberUtils.isNumber(distField.getText())) {

            autocompleteDistDir(counties, latField, lonField, cityField,
                    countyField, PshUtil.stringToAngle(dirField.getText()),
                    Double.parseDouble(distField.getText()));
        }

    }

    /**
     * Insert data in the tableData LinkedHashMap
     * 
     * @param item
     * @param data
     * @param index
     *            index at which to insert
     */
    protected void insertTableData(TableItem item, StormDataEntry data,
            int index) {
        // Just use Map.put() if inserting at the end
        if (index > -1 && index != tableData.size()) {
            Map<TableItem, StormDataEntry> tableCopy = new LinkedHashMap<>(
                    tableData);
            tableData.clear();

            ArrayList<TableItem> keys = new ArrayList<>(tableCopy.keySet());
            boolean inserted = false;

            for (int ii = 0; ii < tableCopy.size(); ii++) {
                if (ii == index && !inserted) {
                    tableData.put(item, data);
                    inserted = true;
                    ii--;
                } else {
                    tableData.put(keys.get(ii), tableCopy.get(keys.get(ii)));
                }
            }
        } else {
            tableData.put(item, data);
        }
    }

    /**
     * Move a selected item in the table up one row.
     */
    private void moveUp() {

        if (table.getSelectionCount() > 0 && table.getSelectionCount() < 2
                && table.getSelectionIndex() > 0 && !editing) {
            int selectionIndex = table.getSelectionIndex();

            StormDataEntry shiftedData = tableData
                    .get(table.getItem(selectionIndex));

            tableData.remove(table.getItem(selectionIndex));
            table.remove(selectionIndex);

            addItem(shiftedData, selectionIndex - 1);
            table.setSelection(selectionIndex - 1);

            tab.updatePreviewArea();
        }
    }

    /**
     * Move a selected item in the table down one row.
     */
    private void moveDown() {
        if (table.getSelectionCount() > 0 && table.getSelectionCount() < 2
                && table.getSelectionIndex() < table.getItemCount() - 1
                && !editing) {
            int selectionIndex = table.getSelectionIndex();

            StormDataEntry shiftedData = tableData
                    .get(table.getItem(selectionIndex));

            tableData.remove(table.getItem(selectionIndex));
            table.remove(selectionIndex);

            addItem(shiftedData, selectionIndex + 1);
            table.setSelection(selectionIndex + 1);

            tab.updatePreviewArea();
        }
    }

    /**
     * When editing a row in the Storm Surge (Water Level) tab, populate the ID,
     * county, state, lat/lon fields when a gauge station is selected.
     */
    private void autoPopulateGaugeStation() {
        if (tab.getTabType() == PshDataCategory.WATER_LEVEL && editing) {
            PshCombo stationCombo = (PshCombo) currentEditorRow.get(0)
                    .getEditor();
            Text idText = (Text) currentEditorRow.get(1).getEditor();
            Text countyText = (Text) currentEditorRow.get(2).getEditor();
            Text stateText = (Text) currentEditorRow.get(3).getEditor();
            PshNumberText latText = (PshNumberText) currentEditorRow.get(4)
                    .getEditor();
            PshNumberText lonText = (PshNumberText) currentEditorRow.get(5)
                    .getEditor();

            int curSelection = stationCombo.getSelectionIndex();
            stationCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // clear the fields if station changes.
                    if (curSelection != stationCombo.getSelectionIndex()) {
                        for (int ii = 1; ii < currentEditorRow.size(); ii++) {
                            Control editorField = currentEditorRow.get(ii)
                                    .getEditor();
                            if (editorField instanceof Button) {
                                ((Button) editorField).setSelection(false);
                            } else if (editorField instanceof PshAbstractControl) {
                                ((PshAbstractControl) editorField).setText("");
                            } else if (editorField instanceof Text) {
                                ((Text) editorField).setText("");
                            }
                        }
                    }

                    List<PshCity> gaugeStations = PshConfigurationManager
                            .getInstance().getCities().getTideGaugeStations();

                    PshCity selectedStation = gaugeStations
                            .get(stationCombo.getSelectionIndex());
                    idText.setText(selectedStation.getStationID());
                    countyText.setText(selectedStation.getCounty());
                    stateText.setText(selectedStation.getState());
                    latText.setText(String.valueOf(selectedStation.getLat()));
                    lonText.setText(String.valueOf(selectedStation.getLon()));
                }
            });
        }
    }

    /**
     * Select a given item in a combo.
     * 
     * @param combo
     *            Combo control.
     * @param comboItems
     *            Items in combo
     * @param selected
     *            Item to be selected.
     */
    private void selectComboItem(CCombo combo, List<String> items,
            String selected) {

        // Add all items to combo.
        for (String listItem : items) {
            combo.add(listItem);
        }

        // Find which one to be selected.
        int nitem = 0;
        int sel = -1;
        for (String listItem : items) {
            if (selected.equals(listItem)) {
                sel = nitem;
                break;
            }
            nitem++;
        }

        // Select.
        if (sel >= 0) {
            combo.select(sel);
        } else {
            combo.setText(selected);
        }
    }

    /**
     * Find an array of city names starting with the given string (case
     * insensitive).
     * 
     * @param name
     *            name to start with.
     * 
     * @return String[] Array of city names starting with the given string.
     */
    private String[] findCities(String name) {

        List<String> cities = new ArrayList<>();
        if (name != null && !name.isEmpty()) {
            for (PshCity city : pshCities.getCities()) {
                String cname = city.getName().toUpperCase();
                if (cname.startsWith(name.toUpperCase())) {
                    cities.add(city.getName());
                }
            }
        }

        return cities.toArray(new String[] {});
    }

}

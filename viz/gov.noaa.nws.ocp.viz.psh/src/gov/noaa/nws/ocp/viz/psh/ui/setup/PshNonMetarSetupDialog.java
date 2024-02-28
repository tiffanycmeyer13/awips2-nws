/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.ArrayList;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshStation;
import gov.noaa.nws.ocp.common.localization.psh.PshStations;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.ocp.viz.psh.data.PshStationsProvider;

/**
 * Implementation of the PSH Non-Metar stations setup dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 19, 2017 #34881     astrakovsky  Initial creation.
 * Jul 12, 2017 #35465     astrakovsky  Changed field list to table.
 * Jul 28, 2017 #36367     astrakovsky  Added autocomplete suggestions from database.
 * Aug 17, 2017 #36981     astrakovsky  Made changes to optimize performance.
 * Aug 30, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Sep 27, 2017 #38429     astrakovsky  Fixed error when no entries loaded.
 * Oct 31, 2017 #39988     astrakovsky  Adjusted autocomplete and improved some button functions.
 * Nov 14, 2017 #40296     astrakovsky  Removed save message following delete.
 * Dec 11, 2017 #41998     jwu          Use localization access control file in base/roles.
 * Feb 15, 2018 #46354     wpaintsil    Various refactorings.
 * May 27, 2021 DCS22095   mporricelli  Add some qc checks
 * Jul 19, 2021 DCS22178   mporricelli  Verify PSH Lock owner before saving
 *
 * </pre>
 *
 * @author astrakovsky
 * @version 1.0
 *
 */

public class PshNonMetarSetupDialog extends PshMultiFieldSetupDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int COMMON_BUTTON_WIDTH = 120;

    private static final int COMMON_BUTTON_HEIGHT = 32;

    private static final int CODE_FIELD_WIDTH = 105;

    private static final int STATION_FIELD_WIDTH = 350;

    private static final int STATE_FIELD_WIDTH = 55;

    private static final int COORDINATE_FIELD_WIDTH = 85;

    /*
     * Table column positions
     */
    private static final int CODE_COLUMN = 1;

    private static final int STATION_COLUMN = 2;

    private static final int STATE_COLUMN = 3;

    private static final int LATITUDE_COLUMN = 4;

    private static final int LONGITUDE_COLUMN = 5;

    /**
     * Example form dialog.
     */
    PshSetupExampleFormDialog nonMetarExampleForm = null;

    /**
     * Tooltips for empty fields.
     */
    private static final String CODE_FIELD_TOOLTIP = "Please type a station code";

    private static final String STATION_FIELD_TOOLTIP = "Please type a station name";

    private static final String STATE_FIELD_TOOLTIP = "Please type a state abbreviation";

    private static final String LATITUDE_FIELD_TOOLTIP = "Please type a latitude value";

    private static final String LONGITUDE_FIELD_TOOLTIP = "Please type a longitude value";

    /**
     * Message dialog title
     */
    private static final String MESSAGE_TITLE = "Save Non-Metar Stations";

    /**
     * arrays created from sets for passing autocomplete suggestions
     */
    private String[] codeAutocompleteArray;

    private String[] stationAutocompleteArray;

    private String[] stateAutocompleteArray;

    /**
     * PSH stations (local and DB).
     */
    private PshStations stations;

    private PshStations dbStations;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshNonMetarSetupDialog(Shell parentShell) {
        super(parentShell);
        setDialogTitle("PSH Non-Metar Station setup");
        setFieldContainerHeight(280);
        setVerticalButtons(false);

        // get local and DB stations
        stations = PshConfigurationManager.getInstance().getNonMetarStations();
        dbStations = PshStationsProvider.getNonMetarStations();
    }

    /**
     * Creates the labeled fields.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected final void createLabeledFields(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 5;
        parent.setLayout(gl);

        // create fields table
        createFieldsTable(parent,
                SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL, true, true,
                true, "Click on an entry to edit");

        // create columns
        Table fTable = getFieldsTable();

        TableColumn checkColumn = new TableColumn(fTable, SWT.FILL);
        checkColumn.setWidth(fTable.getItemHeight());
        TableColumn codeColumn = new TableColumn(fTable, SWT.FILL);
        codeColumn.setWidth(CODE_FIELD_WIDTH);
        codeColumn.setText("Station Code");
        TableColumn stationColumn = new TableColumn(fTable, SWT.FILL);
        stationColumn.setWidth(STATION_FIELD_WIDTH);
        stationColumn.setText("                                  Station Name");
        TableColumn stateColumn = new TableColumn(fTable, SWT.FILL);
        stateColumn.setWidth(STATE_FIELD_WIDTH);
        stateColumn.setText("State");
        TableColumn latitudeColumn = new TableColumn(fTable, SWT.FILL);
        latitudeColumn.setWidth(COORDINATE_FIELD_WIDTH);
        latitudeColumn.setText("Latitude");
        TableColumn longitudeColumn = new TableColumn(fTable, SWT.FILL);
        longitudeColumn.setWidth(COORDINATE_FIELD_WIDTH);
        longitudeColumn.setText("Longitude");

        // load autocomplete suggestions
        fillAutocomplete();

        // Fill in the stations
        fillStations(stations);
    }

    /**
     * Creates the buttons.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected final void createButtons(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(7, false);
        gl.marginWidth = 0;
        parent.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalAlignment = SWT.CENTER;
        parent.setLayoutData(gd);

        // create the buttons

        // "Add" button
        createButton(parent, "Add", "Add a new station", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // add a new item at the bottom of the table
                        addItem();
                    }
                });

        // "Delete" button
        setDeleteButton(createButton(parent, "Delete",
                "Delete all checked stations", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // warn user that this is permanent
                        MessageDialog dialog = new MessageDialog(getShell(),
                                "Delete?", null,
                                "Are you sure you want to delete the selected stations?",
                                MessageDialog.WARNING,
                                new String[] { "Yes", "No" }, 1);
                        if (dialog.open() == MessageDialog.OK) {

                            // delete all checked items
                            deleteItems();
                        }
                    }
                }));

        // "Save Changes" button
        setSaveButton(createButton(parent, "Save Changes",
                "Save current stations to localization", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // save all entries
                        saveItems(true);
                    }
                }));

        Label spacerLabel = new Label(parent, SWT.NONE);
        GridData sgd1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        sgd1.widthHint = 40;
        spacerLabel.setLayoutData(sgd1);

        // "Reset" button
        setResetButton(createButton(parent, "Reset",
                "Reset from last-saved stations", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // load stations
                        stations = PshConfigurationManager.getInstance()
                                .getNonMetarStations();

                        // refresh table
                        fillStations(stations);
                    }
                }));

        spacerLabel = new Label(parent, SWT.NONE);
        GridData sgd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        sgd2.widthHint = 40;
        spacerLabel.setLayoutData(sgd2);

        // "Exit" button
        createButton(parent, "Exit", "Close the non-metar station setup dialog",
                COMMON_BUTTON_WIDTH, COMMON_BUTTON_HEIGHT,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        close();
                    }
                });

    }

    /**
     * Load autocomplete suggestions
     */
    private void fillAutocomplete() {

        // initialize autocomplete sets
        SortedSet<String> codeAutocompleteSet = new TreeSet<>();
        SortedSet<String> stationAutocompleteSet = new TreeSet<>();
        SortedSet<String> stateAutocompleteSet = new TreeSet<>();

        // Load DB results into autocomplete sets
        for (PshStation station : dbStations.getStations()) {
            codeAutocompleteSet.add(station.getCode());
            stationAutocompleteSet.add(station.getName());
            stateAutocompleteSet.add(station.getState());
        }

        // convert sets to arrays for passing autocomplete suggestions
        codeAutocompleteArray = codeAutocompleteSet.toArray(new String[] {});
        stationAutocompleteArray = stationAutocompleteSet
                .toArray(new String[] {});
        stateAutocompleteArray = stateAutocompleteSet.toArray(new String[] {});

    }

    /**
     * Clear all stations from GUI and fill from localization.
     * 
     * @param stations
     *            PshStations
     */
    private void fillStations(PshStations stations) {

        // save current row before filling
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        // only continue if not empty
        if (stations.getStations().isEmpty()) {
            return;
        }

        // Clear
        fTable.clearAll();

        // remove extra fields
        if (fTable.getItemCount() > stations.getStations().size()) {
            fTable.remove(stations.getStations().size(),
                    fTable.getItemCount() - 1);
        }

        // reset checkbox and change tracking
        setCheckedBoxes(0);
        setUnsavedChanges(false);
        setNewRows(false);
        updateButtonStates();

        // Fill in the station names
        int ii = 0;
        TableItem item;
        for (PshStation station : stations.getStations()) {
            if (ii < fTable.getItemCount()) {
                item = fTable.getItem(ii);
            } else {
                item = new TableItem(fTable, SWT.NONE);
            }
            item.setText(new String[] { "", station.getCode(),
                    station.getName(), station.getState(),
                    station.getLat() + "", station.getLon() + "" });

            // record original state of item for change checking
            for (int jj = 1; jj < fTable.getColumnCount(); jj++) {
                item.setData(fTable.getColumn(jj).getText(), item.getText(jj));
            }
            item.setData("itemChanged", false);
            ii++;
        }
    }

    /**
     * Get stations from GUI and save into localization.
     */
    @Override
    protected final void saveItems(boolean displayMessage) {

        if (!PshUtil.checkLockStatusOk(getShell())) {
            return;
        }
        // save current row before saving all
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        double latitude = 0;
        double longitude = 0;

        StringBuilder errMsg = new StringBuilder();

        // Retrieve from GUI
        PshStations stations = new PshStations();

        for (int ii = 0; ii < fTable.getItemCount(); ii++) {

            // read station info
            TableItem currItem = fTable.getItem(ii);

            int lineNum = ii + 1;

            String code = currItem.getText(CODE_COLUMN);
            String station = currItem.getText(STATION_COLUMN);
            String state = currItem.getText(STATE_COLUMN);
            String latText = currItem.getText(LATITUDE_COLUMN);
            String lonText = currItem.getText(LONGITUDE_COLUMN);

            // Verify lat/lon fields
            if (PshUtil.verifyLatField(latText, currItem, LATITUDE_COLUMN)) {
                latitude = Double.parseDouble(latText);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": Invalid latitude entered: ").append(latText);
            }
            if (PshUtil.verifyLonField(lonText, currItem, LONGITUDE_COLUMN)) {
                longitude = Double.parseDouble(lonText);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": Invalid longitude entered: ")
                        .append(lonText);
            }
            // check that at least code or name is filled in
            if (!PshUtil.verifyFieldFilled(code, currItem, CODE_COLUMN) && !PshUtil
                    .verifyFieldFilled(station, currItem, STATION_COLUMN)) {
                errMsg.append("\nLine ").append(lineNum).append(
                        ": Entry must have at least a station code or a station name.");

            } else {
                PshStation pshStation = new PshStation("", latitude, longitude,
                        code, station, state);
                stations.getStations().add(pshStation);
                PshUtil.setBackground(currItem, CODE_COLUMN, true);
                PshUtil.setBackground(currItem, STATION_COLUMN, true);
            }

        }
        // Inform user of any problems found
        if (errMsg.length() > 0) {
            MessageDialog.openWarning(getShell(), MESSAGE_TITLE,
                    "Changes have not been saved.\nPlease correct the following issue(s) before saving:\n"
                            + errMsg);
            return;
        }
        // Save to non_metar_stationinfo.xml in localization SITE level
        if (PshConfigurationManager.getInstance()
                .saveNonMetarStations(stations)) {

            // Refill stations after successful save
            setSaveSuccess(true);
            fillStations(stations);

            // Inform the user
            String message = "Non-Metar stations have been saved into Localization store.";
            MessageDialog infoDlg = new MessageDialog(getShell(),
                    MESSAGE_TITLE, null, message,
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);

            infoDlg.open();
        } else {
            new MessageDialog(getShell(), MESSAGE_TITLE, null,
                    "Couldn't save configuration file.", MessageDialog.ERROR,
                    new String[] { "Ok" }, 0).open();
        }

    }

    /**
     * Create editors for the indicated table item.
     * 
     * @param item
     *            - the item to edit.
     */
    protected final void createEditors(TableItem item) {

        // initialize editor list
        setRowEditorList(new ArrayList<>());

        // create editors
        Table fTable = getFieldsTable();

        getRowEditorList().add(PshUtil.createAutoCompleteTableField(fTable,
                item, 1, item.getText(1), CODE_FIELD_TOOLTIP,
                codeAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createAutoCompleteTableField(fTable,
                item, 2, item.getText(2), STATION_FIELD_TOOLTIP,
                stationAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createAutoCompleteTableField(fTable,
                item, 3, item.getText(3), STATE_FIELD_TOOLTIP,
                stateAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createTableField(fTable, item, 4,
                item.getText(4), LATITUDE_FIELD_TOOLTIP, this));
        getRowEditorList().add(PshUtil.createTableField(fTable, item, 5,
                item.getText(5), LONGITUDE_FIELD_TOOLTIP, this));

        // add modify listener for multi-field autocomplete
        Text textField = (Text) getRowEditorList().get(0).getEditor();
        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setToolTipText(CODE_FIELD_TOOLTIP);
                } else {
                    textField.setToolTipText("");

                    // check for match in DB
                    PshStation station = findStation(dbStations,
                            textField.getText());

                    // if match found, autocomplete
                    if (station != null) {
                        ((Text) getRowEditorList().get(1).getEditor())
                                .setText(station.getName());
                        ((Text) getRowEditorList().get(2).getEditor())
                                .setText(station.getState());

                        if (station.getLat() != PshStation.getDefaultStation()
                                .getLat()) {
                            ((Text) getRowEditorList().get(3).getEditor())
                                    .setText(station.getLat() + "");
                        } else {
                            ((Text) getRowEditorList().get(3).getEditor())
                                    .setText("");
                        }

                        if (station.getLon() != PshStation.getDefaultStation()
                                .getLon()) {
                            ((Text) getRowEditorList().get(4).getEditor())
                                    .setText(station.getLon() + "");
                        } else {
                            ((Text) getRowEditorList().get(4).getEditor())
                                    .setText("");
                        }
                    }

                    // set boolean based on current field value matching
                    // starting field value
                    String prevData = Objects.toString(
                            item.getData(fTable.getColumn(1).getText()), "");
                    String curData = textField.getText();
                    textField.setData("fieldChanged",
                            !curData.equals(prevData));

                    // set unsaved changes boolean based on text field change
                    // booleans in row
                    setUnsavedChanges(checkChangedItems());

                    // update buttons
                    updateButtonStates();
                }
            }
        });

    }

    /**
     * Find a station by station code in the current list of stations.
     * 
     * @param stations
     *            - the station list
     * @param code
     *            - the station code
     * @return The matching station, null if no match
     */
    private PshStation findStation(PshStations stations, String code) {

        // look for matching station
        for (PshStation station : stations.getStations()) {
            if (station.getCode().equals(code)) {
                return station;
            }
        }

        // return null if no match found
        return null;
    }

}

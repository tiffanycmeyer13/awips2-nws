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
 * Implementation of the PSH Marine stations setup dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2017 #34882     astrakovsky  Initial creation.
 * Jul 14, 2017 #35465     astrakovsky  Changed field list to table.
 * Aug 01, 2017 #36367     astrakovsky  Added autocomplete suggestions from database.
 * Aug 18, 2017 #36981     astrakovsky  Made changes to optimize performance.
 * Aug 30, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Sep 27, 2017 #38429     astrakovsky  Fixed error when no entries loaded.
 * Oct 31, 2017 #39988     astrakovsky  Adjusted autocomplete and improved some button functions.
 * Nov 14, 2017 #40296     astrakovsky  Removed save message following delete.
 * Dec 11, 2017 #41998     jwu          Use localization access control file in base/roles.
 * Feb 15, 2018 #46354     wpaintsil   Various refactorings.
 * May 27, 2021 DCS22095   mporricelli  Add some qc checks
 *
 * </pre>
 *
 * @author astrakovsky
 * @version 1.0
 *
 */

public class PshMarineSetupDialog extends PshMultiFieldSetupDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int COMMON_BUTTON_WIDTH = 120;

    private static final int COMMON_BUTTON_HEIGHT = 32;

    private static final int STATION_FIELD_WIDTH = 500;

    private static final int COORDINATE_FIELD_WIDTH = 85;

    /*
     * Column positions
     */
    private static final int STATION_COLUMN = 1;

    private static final int LATITUDE_COLUMN = 2;

    private static final int LONGITUDE_COLUMN = 3;

    /**
     * Tooltips for empty fields.
     */
    private static final String STATION_FIELD_TOOLTIP = "Please type a station name";

    private static final String LATITUDE_FIELD_TOOLTIP = "Please type a latitude value";

    private static final String LONGITUDE_FIELD_TOOLTIP = "Please type a longitude value";

    /**
     * Message dialog title
     */
    private static final String MESSAGE_TITLE = "Save Marine Stations";

    /**
     * array created from sets for passing autocomplete suggestions
     */
    private String[] stationAutocompleteArray;

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
    public PshMarineSetupDialog(Shell parentShell) {
        super(parentShell);
        setDialogTitle("PSH Marine Station Setup");
        setFieldContainerHeight(280);
        setVerticalButtons(false);

        // get local and DB stations.
        stations = PshConfigurationManager.getInstance().getMarineStations();
        dbStations = PshStationsProvider.getMarineStations();
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
        TableColumn stationColumn = new TableColumn(fTable, SWT.FILL);
        stationColumn.setWidth(STATION_FIELD_WIDTH);
        stationColumn.setText(
                "                                                    Station Name");
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
        GridData sgd = new GridData(SWT.FILL, SWT.FILL, true, true);
        sgd.widthHint = 35;
        spacerLabel.setLayoutData(sgd);

        // "Reset" button
        setResetButton(createButton(parent, "Reset",
                "Reset from last-saved stations", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // load stations
                        stations = PshConfigurationManager.getInstance()
                                .getMarineStations();

                        // refresh table
                        fillStations(stations);
                    }
                }));

        spacerLabel = new Label(parent, SWT.NONE);
        spacerLabel.setLayoutData(sgd);

        // "Exit" button
        createButton(parent, "Exit", "Close the marine station setup dialog",
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

        // initialize autocomplete set
        SortedSet<String> stationAutocompleteSet = new TreeSet<>();

        // Load DB results into autocomplete set
        for (PshStation station : dbStations.getStations()) {
            stationAutocompleteSet.add(station.getFullName());
        }

        // convert set to array for passing autocomplete suggestions
        stationAutocompleteArray = stationAutocompleteSet
                .toArray(new String[] {});

    }

    /**
     * Clear all stations from GUI and fill with new county names.
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
            item.setText(new String[] { "", station.getFullName(),
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

        // save current row before saving all
        saveRow();

        Table fTable = getFieldsTable();

        fTable.deselectAll();

        // Retrieve from GUI
        PshStations stations = new PshStations();

        StringBuilder errMsg = new StringBuilder();

        double latitude = 0;
        double longitude = 0;

        for (int ii = 0; ii < fTable.getItemCount(); ii++) {

            // read station info
            TableItem currItem = fTable.getItem(ii);

            int lineNum = ii + 1;

            String station = currItem.getText(STATION_COLUMN);
            String latText = currItem.getText(LATITUDE_COLUMN);
            String lonText = currItem.getText(LONGITUDE_COLUMN);

            // Verify lat/lon fields
            if (PshUtil.verifyLatField(latText, currItem, LATITUDE_COLUMN)) {
                latitude = Double.parseDouble(latText);
            } else {
                errMsg.append("\nLine ");
                errMsg.append(lineNum).append(": Invalid latitude entered: ")
                        .append(latText);
            }
            if (PshUtil.verifyLonField(lonText, currItem, LONGITUDE_COLUMN)) {
                longitude = Double.parseDouble(lonText);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": Invalid longitude entered: ")
                        .append(lonText);
            }

            // check that everything is filled in
            if (PshUtil.verifyFieldFilled(station, currItem, STATION_COLUMN)) {
                PshStation pshStation = new PshStation("", latitude, longitude,
                        "", station, "");
                stations.getStations().add(pshStation);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": Station name is blank.");
            }

        }
        // Inform user of any problems found
        if (errMsg.length() > 0) {
            MessageDialog.openWarning(getShell(), MESSAGE_TITLE,
                    "Changes have not been saved.\nPlease correct the following issue(s) before saving:\n"
                            + errMsg);
            return;
        }

        // Save to marine_stationinfo.xml in localization SITE level
        if (PshConfigurationManager.getInstance()
                .saveMarineStations(stations)) {

            // Refill stations after successful save
            setSaveSuccess(true);
            fillStations(stations);

            String message = "Marine Stations have been saved into Localization store.";
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
                item, 1, item.getText(1), STATION_FIELD_TOOLTIP,
                stationAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createTableField(fTable, item, 2,
                item.getText(2), LATITUDE_FIELD_TOOLTIP, this));
        getRowEditorList().add(PshUtil.createTableField(fTable, item, 3,
                item.getText(3), LONGITUDE_FIELD_TOOLTIP, this));

        // add modify listener for multi-field autocomplete
        Text textField = (Text) getRowEditorList().get(0).getEditor();
        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setToolTipText(STATION_FIELD_TOOLTIP);
                } else {
                    textField.setToolTipText("");

                    // check for match in DB
                    PshStation station = findStation(dbStations,
                            textField.getText());
                    // if match found, autocomplete
                    if (station != null) {
                        if (station.getLat() != PshStation.getDefaultStation()
                                .getLat()) {
                            ((Text) getRowEditorList().get(1).getEditor())
                                    .setText(station.getLat() + "");
                        } else {
                            ((Text) getRowEditorList().get(1).getEditor())
                                    .setText("");
                        }

                        if (station.getLon() != PshStation.getDefaultStation()
                                .getLon()) {
                            ((Text) getRowEditorList().get(2).getEditor())
                                    .setText(station.getLon() + "");
                        } else {
                            ((Text) getRowEditorList().get(2).getEditor())
                                    .setText("");
                        }
                    }

                    // set boolean based on current field value matching
                    // starting field value
                    textField
                            .setData("fieldChanged",
                                    !textField.getText()
                                            .equals(Objects
                                                    .toString(
                                                            item.getData(fTable
                                                                    .getColumn(
                                                                            1)
                                                                    .getText()),
                                                            "")));

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
     * Find a station by full station name in the current list of stations.
     * 
     * @param stations
     *            - the station list
     * @param name
     *            - the full station name
     * @return The matching station, null if no match
     */
    private PshStation findStation(PshStations stations, String name) {

        // look for matching station
        for (PshStation station : stations.getStations()) {
            if (station.getFullName().equals(name)) {
                return station;
            }
        }

        // return null if no match found
        return null;
    }

}

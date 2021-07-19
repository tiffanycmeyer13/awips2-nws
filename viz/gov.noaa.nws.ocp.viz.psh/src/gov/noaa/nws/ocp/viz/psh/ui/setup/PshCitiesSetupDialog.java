/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.localization.psh.PshCities;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshCounties;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.ocp.viz.psh.data.PshCitiesProvider;
import gov.noaa.ocp.viz.psh.data.PshCountiesProvider;
import gov.noaa.ocp.viz.psh.data.PshStationsProvider;

/**
 * Implementation of the PSH cities setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 12, 2017 #34873     astrakovsky  Initial creation.
 * Jul 14, 2017 #35465     astrakovsky  Changed field list to table.
 * Aug 08, 2017 #35737     astrakovsky  Added autocomplete suggestions from database.
 * Aug 15, 2017 #36981     astrakovsky  Made changes to optimize performance.
 * Aug 28, 2017 #37366     astrakovsky  Added columns, fixed autocomplete errors, 
 *                                      and moved some code to base class.
 * Sep 27, 2017 #38429     astrakovsky  Fixed error when no entries loaded.
 * Oct 30, 2017 #39988     astrakovsky  Adjusted autocomplete and improved some button functions.
 * Nov 14, 2017 #40296     astrakovsky  Removed save message following delete.
 * Dec 08, 2017 #41955     astrakovsky  Added call to clear county geometry data when saving cities.
 * Dec 11, 2017 #41998     jwu          Use localization access control file in base/roles.
 * Feb 15, 2018 #46354     wpaintsil   Various refactorings.
 * May 27, 2021 DCS22095   mporricelli  Add some qc checks
 * Jul 19, 2021 DCS22178   mporricelli  Verify PSH Lock owner before saving
 *
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCitiesSetupDialog extends PshMultiFieldSetupDialog {

    private static final int CITY_FIELD_WIDTH = 300;

    private static final int COUNTY_FIELD_WIDTH = 185;

    private static final int STATE_FIELD_WIDTH = 55;

    private static final int COORDINATE_FIELD_WIDTH = 85;

    private static final int CODE_FIELD_WIDTH = 105;

    private static final int GAUGE_FIELD_WIDTH = 60;

    /**
     * Column positions
     */
    private static final int CITY_COLUMN = 1;

    private static final int COUNTY_COLUMN = 2;

    private static final int STATE_COLUMN = 3;

    private static final int LATITUDE_COLUMN = 4;

    private static final int LONGITUDE_COLUMN = 5;

    private static final int CODE_COLUMN = 6;

    private static final int GAUGE_COLUMN = 7;

    /**
     * Strings for missing data.
     */

    private static final String MISSING_COUNTY_STRING = ", ";

    private static final String MISSING_STATE_STRING = ", ";

    /**
     * Tooltips for empty fields.
     */
    private static final String CITY_FIELD_TOOLTIP = "Please type a city name";

    private static final String COUNTY_FIELD_TOOLTIP = "Please type a county name";

    private static final String STATE_FIELD_TOOLTIP = "Please type a state abbreviation";

    private static final String LATITUDE_FIELD_TOOLTIP = "Please type a latitude value";

    private static final String LONGITUDE_FIELD_TOOLTIP = "Please type a longitude value";

    private static final String CODE_FIELD_TOOLTIP = "Please type a station code if applicable";

    private static final String GAUGE_FIELD_TOOLTIP = "Please check if an official tide gauge is present, otherwise leave unchecked.";

    /**
     * Message dialog title
     */
    private static final String MESSAGE_TITLE = "Save Cities";

    /**
     * Arrays created from sets for passing autocomplete suggestions.
     */
    private String[] cityAutocompleteArray;

    private String[] countyAutocompleteArray;

    private String[] stateAutocompleteArray;

    private String[] codeAutocompleteArray;

    /**
     * Map linking autocomplete suggestions to city data.
     */
    private Map<String, PshCity> cityAutocompleteMap;

    /**
     * PSH cities and counties (local and DB).
     */
    private PshCities cities;

    private PshCities dbCities;

    private PshCounties dbCounties;

    /**
     * List of all station codes from DB.
     */
    private List<String> dbStationCodes;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshCitiesSetupDialog(Shell parentShell) {
        super(parentShell);
        setDialogTitle("PSH Cities Setup");
        setFieldContainerHeight(500);
        setVerticalButtons(false);

        // get local and DB cities
        cities = PshConfigurationManager.getInstance().getCities();
        dbCities = PshCitiesProvider.getCities();
        dbCounties = PshCountiesProvider.getCountyNames();
        dbStationCodes = PshStationsProvider.getAllStationCodes();
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
        TableColumn cityColumn = new TableColumn(fTable, SWT.FILL);
        cityColumn.setWidth(CITY_FIELD_WIDTH);
        cityColumn.setText("                          City");
        TableColumn countyColumn = new TableColumn(fTable, SWT.FILL);
        countyColumn.setWidth(COUNTY_FIELD_WIDTH);
        countyColumn.setText("                  County");
        TableColumn stateColumn = new TableColumn(fTable, SWT.FILL);
        stateColumn.setWidth(STATE_FIELD_WIDTH);
        stateColumn.setText("State");
        TableColumn latitudeColumn = new TableColumn(fTable, SWT.FILL);
        latitudeColumn.setWidth(COORDINATE_FIELD_WIDTH);
        latitudeColumn.setText("Latitude");
        TableColumn longitudeColumn = new TableColumn(fTable, SWT.FILL);
        longitudeColumn.setWidth(COORDINATE_FIELD_WIDTH);
        longitudeColumn.setText("Longitude");
        TableColumn stationColumn = new TableColumn(fTable, SWT.FILL);
        stationColumn.setWidth(CODE_FIELD_WIDTH);
        stationColumn.setText("Station Code");
        TableColumn gaugeColumn = new TableColumn(fTable, SWT.FILL);
        gaugeColumn.setWidth(GAUGE_FIELD_WIDTH);
        gaugeColumn.setText(" Tide\nGauge");

        // load autocomplete suggestions
        fillAutocomplete(cities);

        // Fill in the cities
        fillCities(cities);
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
        createButton(parent, StringUtils.center("Add", 20), "Add a new city",
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // add a new item at the bottom of the table
                        addItem();
                    }
                });

        // "Delete" button
        setDeleteButton(createButton(parent, StringUtils.center("Delete", 20),
                "Delete all checked cities", new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // warn user that this is permanent
                        MessageDialog dialog = new MessageDialog(getShell(),
                                "Delete?", null,
                                "Are you sure you want to delete the selected cities?",
                                MessageDialog.WARNING,
                                new String[] { "Yes", "No" }, 1);
                        if (dialog.open() == MessageDialog.OK) {

                            // delete all checked items
                            deleteItems();
                        }
                    }
                }));

        // "Save Changes" button
        setSaveButton(createButton(parent,
                StringUtils.center("Save Changes", 20),
                "Save current cities to localization", new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // save all entries
                        saveItems(true);
                    }
                }));

        Label spacerLabel = new Label(parent, SWT.NONE);
        GridData sgd1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        sgd1.widthHint = 140;
        spacerLabel.setLayoutData(sgd1);

        // "Reset" button
        setResetButton(createButton(parent, StringUtils.center("Reset", 20),
                "Reset from last-saved cities", new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // load cities
                        cities = PshConfigurationManager.getInstance()
                                .getCities();

                        // refresh table
                        fillCities(cities);
                    }
                }));

        spacerLabel = new Label(parent, SWT.NONE);
        GridData sgd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        sgd2.widthHint = 140;
        spacerLabel.setLayoutData(sgd2);

        // "Exit" button
        createButton(parent, StringUtils.center("Exit", 20),
                "Close the cities setup dialog", new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        close();
                    }
                });

    }

    /**
     * Load autocomplete suggestions
     * 
     * @param stations
     *            PshStations
     */
    private void fillAutocomplete(PshCities cities) {

        // initialize autocomplete sets
        SortedSet<String> cityAutocompleteSet = new TreeSet<>();
        SortedSet<String> countyAutocompleteSet = new TreeSet<>(
                dbCounties.getCounties());
        SortedSet<String> stateAutocompleteSet = new TreeSet<>();
        SortedSet<String> codeAutocompleteSet = new TreeSet<>(dbStationCodes);

        // initialize city data map
        cityAutocompleteMap = new HashMap<>();

        // Load DB results into autocomplete sets and link to map
        String cityKey;
        for (PshCity city : dbCities.getCities()) {
            cityKey = city.getName();
            if (city.getCounty() != null && !city.getCounty().isEmpty()) {
                cityKey += ", " + city.getCounty();
            } else {
                cityKey += MISSING_COUNTY_STRING;
            }
            if (city.getState() != null && !city.getState().isEmpty()) {
                cityKey += ", " + city.getState();
            } else {
                cityKey += MISSING_STATE_STRING;
            }
            cityAutocompleteMap.put(cityKey, city);
            cityAutocompleteSet.add(cityKey);
            stateAutocompleteSet.add(city.getState());
        }

        // convert sets to arrays for passing autocomplete suggestions
        cityAutocompleteArray = cityAutocompleteSet.toArray(new String[] {});
        countyAutocompleteArray = countyAutocompleteSet
                .toArray(new String[] {});
        stateAutocompleteArray = stateAutocompleteSet.toArray(new String[] {});
        codeAutocompleteArray = codeAutocompleteSet.toArray(new String[] {});

    }

    /**
     * Clear all cities from GUI and fill from localization.
     * 
     * @param cities
     *            PshCities
     */
    private void fillCities(PshCities cities) {

        // save current row before filling
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        // only continue if not empty
        if (cities.getCities().isEmpty()) {
            return;
        }

        // Clear
        fTable.clearAll();

        // remove extra fields
        if (fTable.getItemCount() > cities.getCities().size()) {
            fTable.remove(cities.getCities().size(), fTable.getItemCount() - 1);
        }

        // reset checkbox and change tracking
        setCheckedBoxes(0);
        setUnsavedChanges(false);
        setNewRows(false);
        updateButtonStates();

        // Fill in the station names
        int ii = 0;
        TableItem item;
        for (PshCity city : cities.getCities()) {
            if (ii < fTable.getItemCount()) {
                item = fTable.getItem(ii);
                item.setText(new String[] { "", city.getName(),
                        city.getCounty(), city.getState(), city.getLat() + "",
                        city.getLon() + "", city.getStationID(),
                        city.getGauge() });
            } else {
                item = new TableItem(fTable, SWT.NONE);
                item.setText(new String[] { "", city.getName(),
                        city.getCounty(), city.getState(), city.getLat() + "",
                        city.getLon() + "", city.getStationID(),
                        city.getGauge() });
            }
            // record original state of item for change checking
            for (int jj = 1; jj < fTable.getColumnCount(); jj++) {
                item.setData(fTable.getColumn(jj).getText(), item.getText(jj));
            }
            item.setData("itemChanged", false);
            ii++;
        }
    }

    /**
     * Get cities from GUI and save into localization.
     * 
     * @param displayMessage
     *            - boolean indicating whether a confirmation message is shown.
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

        // Retrieve from GUI
        cities = new PshCities(new ArrayList<>());

        StringBuilder errMsg = new StringBuilder();

        float latitude = 0;
        float longitude = 0;

        String cityKey;
        for (int ii = 0; ii < fTable.getItemCount(); ii++) {

            // read station info
            TableItem currItem = fTable.getItem(ii);

            int lineNum = ii + 1;

            String city = currItem.getText(CITY_COLUMN);
            String county = currItem.getText(COUNTY_COLUMN);
            String state = currItem.getText(STATE_COLUMN);
            String code = currItem.getText(CODE_COLUMN);
            String gauge = currItem.getText(GAUGE_COLUMN);
            String latText = currItem.getText(LATITUDE_COLUMN);
            String lonText = currItem.getText(LONGITUDE_COLUMN);

            // Verify lat/lon fields; notify user to make change if not valid
            if (PshUtil.verifyLatField(latText, currItem, LATITUDE_COLUMN)) {
                latitude = Float.parseFloat(latText);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": Invalid latitude entered: ").append(latText);
            }
            if (PshUtil.verifyLonField(lonText, currItem, LONGITUDE_COLUMN)) {
                longitude = Float.parseFloat(lonText);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": Invalid longitude entered: ")
                        .append(lonText);
            }

            // check that city name is filled in
            if (PshUtil.verifyFieldFilled(city, currItem, CITY_COLUMN)) {

                // build city key to get CWA
                cityKey = city;
                if (!county.isEmpty()) {
                    cityKey += ", " + county;
                } else {
                    cityKey += MISSING_COUNTY_STRING;
                }
                if (!state.isEmpty()) {
                    cityKey += ", " + state;
                } else {
                    cityKey += MISSING_STATE_STRING;
                }

                // prepare city to save
                PshCity pshCity;
                if (cityAutocompleteMap.get(cityKey) != null) {
                    String cwa = Objects.toString(
                            cityAutocompleteMap.get(cityKey).getCWA(), "");
                    pshCity = new PshCity(city, county, state, latitude,
                            longitude, code, cwa, gauge);
                } else {
                    pshCity = new PshCity(city, county, state, latitude,
                            longitude, code, "", gauge);
                }
                cities.getCities().add(pshCity);
            } else {
                errMsg.append("\nLine ").append(lineNum)
                        .append(": City is blank");
            }

        }

        // Inform user of any problems found
        if (errMsg.length() > 0) {
            MessageDialog.openWarning(getShell(), MESSAGE_TITLE,
                    "Changes have not been saved.\nPlease correct the following issue(s) before saving:\n"
                            + errMsg);
            return;
        }

        // Save to cities.xml in localization SITE level
        if (PshConfigurationManager.getInstance().saveCities(cities)) {

            // Refill after successful save
            setSaveSuccess(true);
            fillCities(cities);

            // city data has changed, so county geodata will need to be
            // re-loaded
            PshUtil.clearCountyGeodata();

            String message = "Cities/Water Level Stations have been saved into Localization store.";
            MessageDialog infoDlg = new MessageDialog(getShell(), MESSAGE_TITLE,
                    null, message, MessageDialog.INFORMATION,
                    new String[] { "Ok" }, 0);

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
                item, 1, item.getText(1), CITY_FIELD_TOOLTIP,
                cityAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createAutoCompleteTableField(fTable,
                item, 2, item.getText(2), COUNTY_FIELD_TOOLTIP,
                countyAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createAutoCompleteTableField(fTable,
                item, 3, item.getText(3), STATE_FIELD_TOOLTIP,
                stateAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createTableField(fTable, item, 4,
                item.getText(4), LATITUDE_FIELD_TOOLTIP, this));
        getRowEditorList().add(PshUtil.createTableField(fTable, item, 5,
                item.getText(5), LONGITUDE_FIELD_TOOLTIP, this));
        getRowEditorList().add(PshUtil.createAutoCompleteTableField(fTable,
                item, 6, item.getText(6), CODE_FIELD_TOOLTIP,
                codeAutocompleteArray, this));
        getRowEditorList().add(PshUtil.createTableCheckbox(fTable, item, 7, "G",
                GAUGE_FIELD_TOOLTIP, this));

        // add modify listener for multi-field autocomplete
        Text textField = (Text) getRowEditorList().get(0).getEditor();

        textField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setToolTipText(CITY_FIELD_TOOLTIP);
                } else {
                    textField.setToolTipText("");
                }

                // check for matching city and autocomplete
                PshCity city = cityAutocompleteMap.get(textField.getText());
                if (city != null) {
                    autocompleteCity(city);
                }

                // set boolean based on current field value matching starting
                // field value
                String prevData = Objects.toString(
                        item.getData(fTable.getColumn(1).getText()), "");
                textField.setData("fieldChanged",
                        !textField.getText().equals(prevData));

                // set unsaved changes boolean based on text field change
                // booleans in row
                setUnsavedChanges(checkChangedItems());

                // update buttons
                updateButtonStates();
            }
        });

    }

    /**
     * Autocomplete fields from city data.
     * 
     * @param city
     */
    private void autocompleteCity(PshCity city) {

        // if match found, autocomplete
        if (getRowEditorList() != null && city != null) {
            ((Text) getRowEditorList().get(0).getEditor())
                    .setText(city.getName());
            ((Text) getRowEditorList().get(1).getEditor())
                    .setText(city.getCounty());
            ((Text) getRowEditorList().get(2).getEditor())
                    .setText(city.getState());

            if (city.getLat() != PshCity.getDefaultCity().getLat()) {
                ((Text) getRowEditorList().get(3).getEditor())
                        .setText(city.getLat() + "");
            } else {
                ((Text) getRowEditorList().get(3).getEditor()).setText("");
            }

            if (city.getLon() != PshCity.getDefaultCity().getLon()) {
                ((Text) getRowEditorList().get(4).getEditor())
                        .setText(city.getLon() + "");
            } else {
                ((Text) getRowEditorList().get(4).getEditor()).setText("");
            }

            ((Text) getRowEditorList().get(5).getEditor())
                    .setText(city.getStationID());
            ((Button) getRowEditorList().get(6).getEditor())
                    .setSelection(city.getGauge().trim().equals("G"));
        }

    }

    /**
     * Save row currently being edited and dispose editors.
     * 
     * Override to account for last field having a checkbox editor.
     */
    @Override
    protected final void saveRow() {

        if (getRowEditorList() != null) {

            // get table item being edited
            TableItem item = getRowEditorList().get(0).getItem();

            // copy editor contents to table, then dispose editors
            TableEditor editor;
            // text fields
            for (int ii = 0; ii < getRowEditorList().size() - 1; ii++) {
                editor = getRowEditorList().get(ii);
                item.setText(ii + 1, ((Text) editor.getEditor()).getText());
                editor.getEditor().dispose();
                editor.dispose();
            }
            // checkbox
            editor = getRowEditorList().get(getRowEditorList().size() - 1);
            if (((Button) editor.getEditor()).getSelection()) {
                item.setText(getRowEditorList().size(), "G");
            } else {
                item.setText(getRowEditorList().size(), "");
            }
            editor.getEditor().dispose();
            editor.dispose();
            setRowEditorList(null);
        }

    }

    /**
     * Set focus to the text field where the mouse is if applicable, or the
     * default one if not.
     * 
     * Override to account for last field having a checkbox editor.
     * 
     * @param column
     *            - the default column to focus on
     */
    @Override
    protected final void focusMouseField(int column) {

        if (getRowEditorList() != null) {

            // get default field
            Text textField = (Text) getRowEditorList().get(column).getEditor();

            // only check mouse position if multiple fields
            if (getRowEditorList().size() > 1) {
                boolean fieldSelected = false;
                TableEditor editor;
                for (int ii = 0; ii < getRowEditorList().size() - 1; ii++) {
                    editor = getRowEditorList().get(ii);
                    if (editor.getEditor().getBounds()
                            .contains(getLastMouseClick())) {
                        Text text = ((Text) editor.getEditor());
                        text.setFocus();
                        text.setSelection(text.getText().length());
                        fieldSelected = true;
                        break;
                    }
                }
                // if mouse not over any field, select default
                if (!fieldSelected) {
                    textField.setFocus();
                    textField.setSelection(textField.getText().length());
                }
            }
            // only one field, select it
            else {
                textField.setFocus();
                textField.setSelection(textField.getText().length());
            }
        }
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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

import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.common.localization.psh.PshCounties;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.ocp.viz.psh.data.PshCountiesProvider;

/**
 * Implementation of the PSH county setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 27, 2017 #34294     astrakovsky  Initial creation.
 * Jun 13, 2017 #34873     astrakovsky  Refactored to use a new superclass.
 * Jun 20, 2017 #35269     jwu          Implemented Get/Save with localization.
 * Jun 28, 2017 #35465     astrakovsky  Changed field list to table.
 * Aug 09, 2017 #35737     astrakovsky  Added autocomplete suggestions from database.
 * Aug 18, 2017 #36981     astrakovsky  Made changes to optimize performance.
 * Aug 31, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Sep 27, 2017 #38429     astrakovsky  Fixed error when no entries loaded.
 * Oct 31, 2017 #39988     astrakovsky  Adjusted autocomplete and improved some button functions.
 * Nov 14, 2017 #40296     astrakovsky  Removed save message following delete.
 * Dec 11, 2017 #41998     jwu          Use localization access control file in base/roles.
 * Jul 19, 2021 DCS22178   mporricelli  Verify PSH Lock owner before saving
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCountySetupDialog extends PshMultiFieldSetupDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int COMMON_BUTTON_WIDTH = 120;

    private static final int COMMON_BUTTON_HEIGHT = 32;

    private static final int FIELD_WIDTH = 350;

    /**
     * Tooltip for empty fields.
     */
    private String countyFieldTooltip = "Please type a county/parish name";

    /**
     * array created from set for passing autocomplete suggestions
     */
    private String[] countyAutocompleteArray;

    /**
     * PSH counties.
     */
    private PshCounties counties;

    private PshCounties dbCounties;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshCountySetupDialog(Shell parentShell) {
        super(parentShell);

        setDialogTitle("PSH County/Parish Setup");
        setFieldContainerHeight(240);
        setVerticalButtons(true);

        // get local and DB counties
        counties = PshConfigurationManager.getInstance().getCounties();
        dbCounties = PshCountiesProvider.getCountyNames();
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
                SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL, false, true,
                true, "Click on an entry to edit");

        // create columns
        Table fTable = getFieldsTable();
        TableColumn checkColumn = new TableColumn(fTable, SWT.FILL);
        checkColumn.setWidth(fTable.getItemHeight());

        TableColumn countyColumn = new TableColumn(fTable, SWT.FILL);
        countyColumn.setWidth(FIELD_WIDTH);
        countyColumn.setText("County/Parish Name");

        // add DB counties to set, then convert to array for passing
        // autocomplete suggestions
        countyAutocompleteArray = new TreeSet<>(dbCounties.getCounties())
                .toArray(new String[] {});

        // Fill in the county names
        fillCounties(counties);
    }

    /**
     * Creates the buttons.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected final void createButtons(Composite parent) {

        // set up layout
        GridLayout gl = new GridLayout(1, false);
        gl.marginWidth = 0;
        parent.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalAlignment = SWT.CENTER;
        parent.setLayoutData(gd);

        // create the buttons

        // "Add" button
        createButton(parent, "Add", "Add a new county", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // add a new item at the bottom of the table
                        addItem();
                    }
                });

        // "Delete" button
        setDeleteButton(createButton(parent, "Delete",
                "Delete all checked counties", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // warn user that this is permanent
                        MessageDialog dialog = new MessageDialog(getShell(),
                                "Delete?", null,
                                "Are you sure you want to delete the selected counties?",
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
                "Save current counties to localization", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // save all entries
                        saveItems(true);
                    }
                }));

        Label spacerLabel = new Label(parent, SWT.NONE);
        spacerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // "Reset" button
        setResetButton(createButton(parent, "Reset",
                "Reset from last-saved counties", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // load counties
                        counties = PshConfigurationManager.getInstance()
                                .getCounties();

                        // refresh table
                        fillCounties(counties);
                    }
                }));

        spacerLabel = new Label(parent, SWT.NONE);
        spacerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // "Exit" button
        createButton(parent, "Exit", "Close the county setup dialog",
                COMMON_BUTTON_WIDTH, COMMON_BUTTON_HEIGHT,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        close();
                    }
                });
    }

    /**
     * Clear all counties from GUI and fill with new county names.
     * 
     * @param counties
     *            PshCounties
     */
    private void fillCounties(PshCounties counties) {

        // save current row before filling
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        // only continue if not empty
        if (counties.getCounties().isEmpty()) {
            return;
        }

        // Clear
        fTable.clearAll();

        // remove extra fields
        if (fTable.getItemCount() > counties.getCounties().size()) {
            fTable.remove(counties.getCounties().size(),
                    fTable.getItemCount() - 1);
        }

        // reset checkbox and change tracking
        setCheckedBoxes(0);
        setUnsavedChanges(false);
        setNewRows(false);
        updateButtonStates();

        // Fill in the county names
        int ii = 0;
        TableItem item;
        for (String county : counties.getCounties()) {
            if (ii < getFieldsTable().getItemCount()) {
                item = getFieldsTable().getItem(ii);
                item.setText(1, county);
            } else {
                item = new TableItem(getFieldsTable(), SWT.NONE);
                item.setText(1, county);
            }
            // record original state of item for change checking
            item.setData(getFieldsTable().getColumn(1).getText(), county);
            item.setData("itemChanged", false);
            ii++;
        }
    }

    /**
     * Get county names from GUI and save into localization.
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
        PshCounties counties = new PshCounties(new ArrayList<>());

        for (TableItem item : fTable.getItems()) {
            String name = item.getText(1);
            if (name != null && name.trim().length() > 0) {
                counties.getCounties().add(name);
            }
        }

        // Save to counties.xml in localization SITE level
        if (PshConfigurationManager.getInstance().saveCounties(counties)) {

            // Refill counties after successful save
            setSaveSuccess(true);
            fillCounties(counties);

            // Inform the user
            String message = "Counties have been saved into Localization store.";
            MessageDialog infoDlg = new MessageDialog(getShell(),
                    "Save Counties", null, message, MessageDialog.INFORMATION,
                    new String[] { "Ok" }, 0);

            infoDlg.open();
        } else {
            new MessageDialog(getShell(), "Save Counties", null,
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

        // create editor
        getRowEditorList().add(PshUtil.createAutoCompleteTableField(
                getFieldsTable(), item, 1, item.getText(1), countyFieldTooltip,
                countyAutocompleteArray, this));
    }

}
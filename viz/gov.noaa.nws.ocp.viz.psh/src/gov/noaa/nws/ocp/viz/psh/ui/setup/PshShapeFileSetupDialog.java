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
import gov.noaa.nws.ocp.common.localization.psh.PshCwas;
import gov.noaa.nws.ocp.viz.psh.PshUtil;
import gov.noaa.ocp.viz.psh.data.PshCwaProvider;

/**
 * Implementation of the PSH shape file configuration setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 30, 2017 #34294     astrakovsky  Initial creation.
 * Jun 13, 2017 #34873     astrakovsky  Refactored to use a new superclass.
 * Jul 18, 2017 #35465     astrakovsky  Changed field list to table.
 * Aug 11, 2017 #35268     astrakovsky  Added CWA loading from database.
 * Aug 18, 2017 #36981     astrakovsky  Made changes to optimize performance.
 * Aug 31, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Sep 27, 2017 #38429     astrakovsky  Fixed error when no entries loaded.
 * Oct 31, 2017 #39988     astrakovsky  Adjusted autocomplete and improved some button functions.
 * Nov 14, 2017 #40296     astrakovsky  Removed save message following delete.
 * Dec 11, 2017 #41998     jwu          Use localization access control file in base/roles.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshShapeFileSetupDialog extends PshMultiFieldSetupDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int COMMON_BUTTON_WIDTH = 100;

    private static final int COMMON_BUTTON_HEIGHT = 32;

    private static final int FIELD_WIDTH = 180;

    /**
     * Tooltip for empty fields.
     */
    private String cwaFieldTooltip = "Please type a CWA";

    /**
     * array created from set for passing autocomplete suggestions
     */
    private String[] cwaAutocompleteArray;

    /**
     * Shapefile CWAs (local and DB).
     */
    private PshCwas cwas;

    private PshCwas dbCwas;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshShapeFileSetupDialog(Shell parentShell) {
        super(parentShell);
        setDialogTitle("Shape File Configuration Setup Menu");
        setFieldContainerHeight(180);
        setVerticalButtons(true);

        // get local and DB CWA names
        cwas = PshConfigurationManager.getInstance().getCwas();
        dbCwas = PshCwaProvider.getCwaStrings();
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

        TableColumn cwaColumn = new TableColumn(fTable, SWT.FILL);
        cwaColumn.setWidth(FIELD_WIDTH);
        cwaColumn.setText("                  CWA");

        // add DB CWAs to set, then convert to array for passing autocomplete
        // suggestions
        cwaAutocompleteArray = new TreeSet<>(dbCwas.getCwas())
                .toArray(new String[] {});

        // Fill in the CWAs
        fillCwas(cwas);
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
        createButton(parent, "Add", "Add a new CWA", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // add a new item at the bottom of the table
                        addItem();
                    }
                });

        // "Delete" button
        setDeleteButton(createButton(parent, "Delete",
                "Delete all checked CWAs", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // warn user that this is permanent
                        MessageDialog dialog = new MessageDialog(getShell(),
                                "Delete?", null,
                                "Are you sure you want to delete the selected CWAs?",
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
                "Save current CWAs to localization", COMMON_BUTTON_WIDTH,
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
                "Reset from last-saved CWAs", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // load CWAs
                        cwas = PshConfigurationManager.getInstance().getCwas();

                        // refresh table
                        fillCwas(cwas);
                    }
                }));

        spacerLabel = new Label(parent, SWT.NONE);
        spacerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // "Exit" button
        createButton(parent, "Exit", "Close the CWA setup dialog",
                COMMON_BUTTON_WIDTH, COMMON_BUTTON_HEIGHT,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        close();
                    }
                });
    }

    /**
     * Clear all CWAs from GUI and fill with new CWAs.
     * 
     * @param cwas
     *            PshCwas
     */
    private void fillCwas(PshCwas cwas) {

        // save current row before filling
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        // only continue if not empty
        if (cwas.getCwas().isEmpty()) {
            return;
        }

        // Clear
        fTable.clearAll();

        // remove extra fields
        if (fTable.getItemCount() > cwas.getCwas().size()) {
            fTable.remove(cwas.getCwas().size(), fTable.getItemCount() - 1);
        }

        // reset checkbox and change tracking
        setCheckedBoxes(0);
        setUnsavedChanges(false);
        setNewRows(false);
        updateButtonStates();

        // Fill in the CWAs
        int ii = 0;
        TableItem item;
        for (String cwa : cwas.getCwas()) {
            if (ii < fTable.getItemCount()) {
                item = fTable.getItem(ii);
                item.setText(1, cwa);
            } else {
                item = new TableItem(fTable, SWT.NONE);
                item.setText(1, cwa);
            }
            // record original state of item for change checking
            item.setData(fTable.getColumn(1).getText(), cwa);
            item.setData("itemChanged", false);
            ii++;
        }
    }

    /**
     * Get CWAs from GUI and save into localization.
     */
    @Override
    protected final void saveItems(boolean displayMessage) {

        // save current row before saving all
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        // Retrieve from GUI
        PshCwas cwas = new PshCwas(new ArrayList<>());

        for (TableItem item : fTable.getItems()) {
            String name = item.getText(1);
            if (name != null && name.trim().length() > 0) {
                cwas.getCwas().add(name);
            }
        }

        // Save to cwas.xml in localization SITE level
        if (PshConfigurationManager.getInstance().saveCwas(cwas)) {

            // Refill CWAs after successful save
            setSaveSuccess(true);
            fillCwas(cwas);

            // Inform the user
            String message = "CWAs have been saved into Localization store.";
            MessageDialog infoDlg = new MessageDialog(getShell(), "Save CWAs",
                    null, message, MessageDialog.INFORMATION,
                    new String[] { "Ok" }, 0);

            infoDlg.open();
        } else {
            new MessageDialog(getShell(), "Save CWAs", null,
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
                getFieldsTable(), item, 1, item.getText(1), cwaFieldTooltip,
                cwaAutocompleteArray, this));
    }

}

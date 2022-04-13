/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.ArrayList;

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
import gov.noaa.nws.ocp.common.localization.psh.PshForecasters;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Implementation of the PSH forecaster setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 07, 2017 #34871     astrakovsky  Initial creation.
 * Jun 13, 2017 #34873     astrakovsky  Refactored to use a new superclass.
 * Jun 19, 2017 #35269     jwu          Implemented Get/Save with localization.
 * Jul 11, 2017 #35465     astrakovsky  Changed field list to table.
 * Aug 18, 2017 #36981     astrakovsky  Made changes to optimize performance.
 * Aug 31, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Sep 27, 2017 #38429     astrakovsky  Fixed error when no entries loaded.
 * Oct 31, 2017 #39988     astrakovsky  Removed autocomplete and improved some button functions.
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

public class PshForecasterSetupDialog extends PshMultiFieldSetupDialog {

    /**
     * Size constants for Widgets.
     */
    private static final int COMMON_BUTTON_WIDTH = 105;

    private static final int COMMON_BUTTON_HEIGHT = 32;

    private static final int FIELD_WIDTH = 300;

    /**
     * Tooltip for empty fields.
     */
    private String fcstrFieldTooltip = "Please type a forecaster's last name";

    /**
     * PSH forecasters.
     */
    private PshForecasters fcstrs;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshForecasterSetupDialog(Shell parentShell) {
        super(parentShell);
        setDialogTitle("PSH Forecaster Setup");
        setFieldContainerHeight(240);
        setVerticalButtons(true);

        // get forecaster list
        fcstrs = PshConfigurationManager.getInstance().getForecasters();
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

        TableColumn fcstrColumn = new TableColumn(fTable, SWT.FILL);
        fcstrColumn.setWidth(FIELD_WIDTH);
        fcstrColumn.setText("Forecaster's Last Name");

        // Fill in the forecaster names
        fillForecasters(fcstrs);
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

        // "Add" button
        createButton(parent, "Add", "Add a new forecaster", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // add a new item at the bottom of the table
                        addItem();
                    }
                });

        // "Delete" button
        setDeleteButton(createButton(parent, "Delete",
                "Delete all checked forecasters", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // warn user that this is permanent
                        MessageDialog dialog = new MessageDialog(getShell(),
                                "Delete?", null,
                                "Are you sure you want to delete the selected forecasters?",
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
                "Save current forecasters to localization", COMMON_BUTTON_WIDTH,
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
                "Reset from last-saved forecasters", COMMON_BUTTON_WIDTH,
                COMMON_BUTTON_HEIGHT, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        // load forecasters
                        fcstrs = PshConfigurationManager.getInstance()
                                .getForecasters();

                        // refresh table
                        fillForecasters(fcstrs);
                    }
                }));

        spacerLabel = new Label(parent, SWT.NONE);
        spacerLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // "Exit" button
        createButton(parent, "Exit", "Close the forecaster setup dialog",
                COMMON_BUTTON_WIDTH, COMMON_BUTTON_HEIGHT,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        close();
                    }
                });
    }

    /**
     * Clear all forecasters from GUI and fill with new forecaster names.
     * 
     * @param fcstrs
     *            PshForecasters
     */
    private void fillForecasters(PshForecasters fcstrs) {

        // save current row before filling
        saveRow();

        Table fTable = getFieldsTable();
        fTable.deselectAll();

        // only continue if not empty
        if (fcstrs.getForecasters().isEmpty()) {
            return;
        }

        // Clear
        fTable.clearAll();

        // remove extra fields
        if (fTable.getItemCount() > fcstrs.getForecasters().size()) {
            fTable.remove(fcstrs.getForecasters().size(),
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
        for (String fcstr : fcstrs.getForecasters()) {
            if (ii < fTable.getItemCount()) {
                item = fTable.getItem(ii);
                item.setText(1, fcstr);
            } else {
                item = new TableItem(fTable, SWT.NONE);
                item.setText(1, fcstr);
            }
            // record original state of item for change checking
            item.setData(fTable.getColumn(1).getText(), fcstr);
            item.setData("itemChanged", false);
            ii++;
        }

    }

    /**
     * Get forecaster names from GUI and save into localization.
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
        PshForecasters fcstrs = new PshForecasters(new ArrayList<>());

        for (TableItem item : fTable.getItems()) {
            String name = item.getText(1);
            if (name != null && name.trim().length() > 0) {
                fcstrs.getForecasters().add(name);
            }
        }

        // Save to fcstr.xml in localization SITE level
        if (PshConfigurationManager.getInstance().saveForecasters(fcstrs)) {

            // Refill forecasters after successful save
            setSaveSuccess(true);
            fillForecasters(fcstrs);

            String message = "Forecasters have been saved into Localization store.";
            MessageDialog infoDlg = new MessageDialog(getShell(),
                    "Save Forecasters", null, message,
                    MessageDialog.INFORMATION, new String[] { "Ok" }, 0);

            infoDlg.open();
        } else {
            new MessageDialog(getShell(), "Save Forecasters", null,
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
        getRowEditorList().add(PshUtil.createTableField(getFieldsTable(), item,
                1, item.getText(1), fcstrFieldTooltip, this));
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.setup;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.common.localization.psh.PshConfigurationManager;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Abstract class for implementation of PSH setup dialogs.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 09, 2017 #34873     astrakovsky  Initial creation.
 * Jul 05, 2017 #35465     astrakovsky  Changed field list to table.
 * Aug 30, 2017 #37366     astrakovsky  Moved some code from subclasses to base class.
 * Oct 31, 2017 #39988     astrakovsky  Improved some button functions.
 * Nov 14, 2017 #40296     astrakovsky  Removed save message following delete.
 * Dec 11, 2017 #41955     astrakovsky  Added button update after dialog creation.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public abstract class PshMultiFieldSetupDialog extends CaveJFACEDialog {

    /**
     * Height of the scrollable container holding the fields.
     */
    private int fieldContainerHeight = 300;

    /**
     * The dialog text.
     */
    private String dialogTitle = "";

    /**
     * The dialog label.
     */
    private String dialogLabel = "";

    /**
     * List containing text editor fields when they are needed.
     */
    private List<TableEditor> rowEditorList;

    /**
     * Boolean indicating when the table is editable.
     */
    private boolean editMode = true;

    /**
     * Counter for checked boxes.
     */
    private int checkedBoxes = 0;

    /**
     * Boolean indicating unsaved changes.
     */
    private boolean unsavedChanges = false;

    /**
     * Boolean indicating new rows have been added but may not have been edited
     * yet. This means that there may not be changes to save, but a reset is
     * possible.
     */
    private boolean newRows = false;

    /**
     * Boolean indicating that at least one save has been successful.
     */
    private boolean saveSuccess = false;

    /**
     * Buttons which can be updated based on GUI state.
     */
    private Button deleteButton;

    private Button saveButton;

    private Button resetButton;

    /**
     * Point of last mouse click.
     */
    private Point lastMouseClick = new Point(0, 0);

    /**
     * Table containing the fields
     */
    private Table fieldsTable;

    /**
     * List containing table items for checking unsaved changes
     */
    private List<TableItem> itemList;

    /**
     * Boolean determining orientation of button panel.
     */
    private boolean verticalButtons = false;

    /**
     * Constructor
     * 
     * @param parentShell
     */
    public PshMultiFieldSetupDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);
    }

    @Override
    public Control createDialogArea(Composite parent) {

        Composite top = (Composite) super.createDialogArea(parent);

        // Create the main layout for the shell
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 5;
        mainLayout.marginWidth = 10;
        top.setLayout(mainLayout);
        top.setLayoutData(
                new GridData(SWT.CENTER, SWT.BEGINNING, false, false));

        initializeComponents(top);

        return top;
    }

    /**
     * Creates buttons, and other controls in the dialog area
     * 
     * @param top
     */
    protected void initializeComponents(Composite top) {

        // Set dialog title
        getShell().setText(dialogTitle);

        // create label with title if not empty
        if (!getDialogLabel().isEmpty()) {
            Label setupLabel = new Label(top, SWT.CENTER);
            setupLabel.setText(getDialogLabel());
            GridData gd = new GridData();
            gd.horizontalAlignment = SWT.CENTER;
            gd.grabExcessHorizontalSpace = true;
            setupLabel.setLayoutData(gd);

            // increase label font size
            FontData[] fontData = setupLabel.getFont().getFontData();
            for (int i = 0; i < fontData.length; ++i) {
                fontData[i].setHeight(18);
            }

            final Font newFont = new Font(getShell().getDisplay(), fontData);
            setupLabel.setFont(newFont);

            // dispose new font when dialog closes
            setupLabel.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    newFont.dispose();
                }
            });
        }

        // Create composite to hold everything else and determine button
        // orientation.
        Composite mainGroup = new Composite(top, SWT.NONE);
        GridLayout gl;
        if (verticalButtons) {
            gl = new GridLayout(2, false);
            gl.horizontalSpacing = 10;
        } else {
            gl = new GridLayout(1, false);
            gl.verticalSpacing = 5;
        }
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        mainGroup.setLayout(gl);

        // create the fields table
        Composite fieldsGroup = new Composite(mainGroup, SWT.NONE);
        createLabeledFields(fieldsGroup);

        // Create the buttons
        Composite buttonsGroup = new Composite(mainGroup, SWT.NONE);
        createButtons(buttonsGroup);
        buttonsGroup.pack();
        updateButtonStates();

    }

    /**
     * Creates the labeled fields.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected abstract void createLabeledFields(Composite parent);

    /**
     * Creates the buttons.
     * 
     * @param parent
     *            - Composite, parent composite.
     */
    protected abstract void createButtons(Composite parent);

    /**
     * Create a button with a custom action
     * 
     * @param parent
     *            - Composite, parent composite.
     * @param btnText
     *            - String, the button label.
     * @param width
     *            - int, the button width.
     * @param height
     *            - int, the button height.
     * @param action
     *            - SelectionAdapter, the button action.
     * @return button
     */
    protected Button createButton(Composite parent, String btnText, int width,
            int height, SelectionAdapter action) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(btnText);
        button.setLayoutData(new GridData(width, height));

        if (action != null) {
            button.addSelectionListener(action);
        }

        return button;
    }

    /**
     * Create a button with a custom action
     * 
     * @param parent
     *            - Composite, parent composite.
     * @param btnText
     *            - String, the button label.
     * @param toolTipText
     *            - String, the tooltip text.
     * @param width
     *            - int, the button width.
     * @param height
     *            - int, the button height.
     * @param action
     *            - SelectionAdapter, the button action.
     * @return the button
     */
    protected Button createButton(Composite parent, String btnText,
            String toolTipText, int width, int height,
            SelectionAdapter action) {
        Button button = createButton(parent, btnText, toolTipText, action);
        button.setLayoutData(new GridData(width, height));
        return button;
    }

    /**
     * Create a button with a custom action
     * 
     * @param parent
     *            - Composite, parent composite.
     * @param btnText
     *            - String, the button label.
     * @param toolTipText
     *            - String, the tooltip text.
     * @param action
     *            - SelectionAdapter, the button action.
     * @return the button
     */
    protected Button createButton(Composite parent, String btnText,
            String toolTipText, SelectionAdapter action) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(btnText);
        button.setToolTipText(toolTipText);

        if (action != null) {
            button.addSelectionListener(action);
        }

        return button;
    }

    /**
     * Create the fields table
     * 
     * @param parent
     *            - Composite, parent composite.
     * @param style
     *            - int, SWT table style.
     * @param headerVisible
     *            - boolean, header visibility.
     * @param linesVisible
     *            - boolean, lines visibility.
     * @param verticalBarVisible
     *            - boolean, vertical scrollbar visibility.
     * @param tipText
     *            - String, table tooltip text.
     */
    protected void createFieldsTable(Composite parent, int style,
            boolean headerVisible, boolean linesVisible,
            boolean verticalBarVisible, String tipText) {

        // create the table
        fieldsTable = new Table(parent, style);
        fieldsTable.setHeaderVisible(headerVisible);
        fieldsTable.setLinesVisible(linesVisible);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd.heightHint = getFieldContainerHeight();

        fieldsTable.setLayoutData(gd);
        fieldsTable.getVerticalBar().setVisible(verticalBarVisible);
        fieldsTable.setToolTipText(tipText);

        // edit row when selected
        fieldsTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editRow(e);
            }
        });

        // get mouse position when clicked
        fieldsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setLastMouseClick(e.x, e.y);
            }
        });

    }

    /**
     * Add an item to the fields table
     */
    protected void addItem() {

        if (fieldsTable != null) {

            // save current row before adding a new one
            saveRow();
            fieldsTable.deselectAll();

            // add a new row
            TableItem item = new TableItem(fieldsTable, SWT.NONE);
            fieldsTable.setSelection(item);
            editRow(null);

            // update buttons
            newRows = true;
            updateButtonStates();
        }

    }

    /**
     * Delete checked items from the fields table
     */
    protected void deleteItems() {

        if (fieldsTable != null) {

            // save current row and disable editing while deleting
            saveRow();
            fieldsTable.deselectAll();
            editMode = false;

            // delete checked items and dispose editors
            for (TableItem item : fieldsTable.getItems()) {
                if (item.getChecked()) {
                    fieldsTable.setSelection(item);
                    fieldsTable.remove(fieldsTable.getSelectionIndex());
                    item.dispose();
                }
            }

            // re-enable editing when done
            editMode = true;

            // save all items after deleting
            saveItems(false);
        }

    }

    /**
     * Save all current items.
     */
    protected abstract void saveItems(boolean displayMessage);

    /**
     * Save row currently being edited and dispose editors.
     */
    protected void saveRow() {

        if (rowEditorList != null) {

            // get table item being edited
            TableItem item = rowEditorList.get(0).getItem();

            // copy editor contents to table, then dispose editors
            int ii = 1;
            for (TableEditor editor : rowEditorList) {
                item.setText(ii, ((Text) editor.getEditor()).getText());
                editor.getEditor().dispose();
                editor.dispose();
                ii++;
            }
            rowEditorList = null;
        }

    }

    /**
     * Edit currently selected row.
     */
    protected void editRow(SelectionEvent e) {

        if (fieldsTable != null) {

            // save row last edited
            saveRow();

            // get selection from table
            TableItem[] selection = fieldsTable.getSelection();

            // make sure exactly one item is selected
            if (editMode && selection.length == 1) {

                // get currently selected item
                TableItem item = selection[0];

                // don't start editing row if checkbox column clicked
                if (getCellBounds(item, 0).contains(lastMouseClick)) {
                    // track checkbox clicks
                    if (e != null && e.detail == SWT.CHECK) {
                        if (item.getChecked()) {
                            checkedBoxes++;
                        } else {
                            checkedBoxes--;
                        }
                    }
                    updateButtonStates();
                    return;
                }

                // create editors and set up autocomplete
                createEditors(item);

                // set focus to text field under mouse, or first field by
                // default
                focusMouseField(0);
            }
        }

    }

    /**
     * Check if any fields in the editor list have been changed.
     * 
     * @return - boolean, indicator for changed fields.
     */
    private boolean checkChangedFields() {
        if (rowEditorList != null) {
            for (TableEditor editor : rowEditorList) {
                if (editor.getEditor().getData("fieldChanged") != null
                        && (boolean) editor.getEditor()
                                .getData("fieldChanged")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if any items have been changed.
     * 
     * @return - boolean, indicator for changed items.
     */
    public boolean checkChangedItems() {
        // check if currently edited fields have any changes and update item.
        if (rowEditorList != null) {
            rowEditorList.get(0).getItem().setData("itemChanged",
                    checkChangedFields());
        }

        // check if any items have changes.
        for (TableItem item : fieldsTable.getItems()) {
            if (item.getData("itemChanged") != null
                    && (boolean) item.getData("itemChanged")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Update delete, save, and reset button activation states.
     */
    public void updateButtonStates() {
        if (deleteButton != null) {
            deleteButton.setEnabled(checkedBoxes > 0);
        }

        if (saveButton != null) {
            saveButton.setEnabled(
                    unsavedChanges || !(saveSuccess || PshConfigurationManager
                            .getInstance().isLoadedFromXml()));
        }

        if (resetButton != null) {
            resetButton
                    .setEnabled(checkedBoxes > 0 || unsavedChanges || newRows);
        }
    }

    /**
     * Get the bounds of the cell indicated by the table item and column.
     * 
     * @param item
     *            - the item (row) to get bounds for.
     * @param column
     *            - the column to get bounds for.
     * @return Rectangle - the cell bounds.
     */
    protected Rectangle getCellBounds(TableItem item, int column) {
        Rectangle bounds = item.getBounds(column);
        if (column == 0) {
            /*
             * Getting bounds of checkbox column - the checkbox is not counted
             * as part of the item so it needs adjustment.
             */
            bounds.width = bounds.width + bounds.x;
            bounds.x = 0;
        }
        return bounds;
    }

    /**
     * Create editors for the indicated table item.
     * 
     * @param item
     *            - the item to edit.
     */
    protected abstract void createEditors(TableItem item);

    /**
     * Set focus to the text field where the mouse is if applicable, or the
     * default one if not.
     * 
     * @param column
     *            - the default column to focus on
     */
    protected void focusMouseField(int column) {

        if (rowEditorList != null) {

            // get default field
            Text textField = (Text) rowEditorList.get(column).getEditor();

            // only check mouse position if multiple fields
            if (getRowEditorList().size() > 1) {
                boolean fieldSelected = false;
                for (TableEditor editor : rowEditorList) {
                    if (editor.getEditor().getBounds()
                            .contains(lastMouseClick)) {
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
            } else { // only one field, select it
                textField.setFocus();
                textField.setSelection(textField.getText().length());
            }
        }
    }

    /**
     * Override button bar creation to do nothing.
     * 
     * @param parent
     */
    @Override
    protected Control createButtonBar(Composite parent) {
        // do nothing, no button bar needed
        return null;
    }

    @Override
    public boolean close() {
        if (PshUtil.exitConfirmed(getShell())) {
            return super.close();
        }
        return false;
    }

    /**
     * @return the fieldContainerHeight
     */
    public int getFieldContainerHeight() {
        return fieldContainerHeight;
    }

    /**
     * @param fieldContainerHeight
     *            the fieldContainerHeight to set
     */
    public void setFieldContainerHeight(int fieldContainerHeight) {
        this.fieldContainerHeight = fieldContainerHeight;
    }

    /**
     * @return the dialogText
     */
    public String getDialogTitle() {
        return dialogTitle;
    }

    /**
     * @param dialogTitle
     *            the dialogTitle to set
     */
    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    /**
     * @return the dialogLabel
     */
    public String getDialogLabel() {
        return dialogLabel;
    }

    /**
     * @param dialogLabel
     *            the dialogLabel to set
     */
    public void setDialogLabel(String dialogLabel) {
        this.dialogLabel = dialogLabel;
    }

    /**
     * @return the rowEditorList
     */
    public List<TableEditor> getRowEditorList() {
        return rowEditorList;
    }

    /**
     * @param rowEditorList
     *            the rowEditorList to set
     */
    public void setRowEditorList(List<TableEditor> rowEditorList) {
        this.rowEditorList = rowEditorList;
    }

    /**
     * @return the editMode
     */
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * @param editMode
     *            the editMode to set
     */
    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    /**
     * @return the checkedBoxes
     */
    public int getCheckedBoxes() {
        return checkedBoxes;
    }

    /**
     * @param checkedBoxes
     *            the checkedBoxes to set
     */
    public void setCheckedBoxes(int checkedBoxes) {
        this.checkedBoxes = checkedBoxes;
    }

    /**
     * @return the unsavedChanges
     */
    public boolean isUnsavedChanges() {
        return unsavedChanges;
    }

    /**
     * @param unsavedChanges
     *            the unsavedChanges to set
     */
    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
    }

    /**
     * @return the newRows
     */
    public boolean isNewRows() {
        return newRows;
    }

    /**
     * @param newRows
     *            the newRows to set
     */
    public void setNewRows(boolean newRows) {
        this.newRows = newRows;
    }

    /**
     * @return the saveSuccess
     */
    public boolean isSaveSuccess() {
        return saveSuccess;
    }

    /**
     * @param saveSuccess
     *            the saveSuccess to set
     */
    public void setSaveSuccess(boolean saveSuccess) {
        this.saveSuccess = saveSuccess;
    }

    /**
     * @return the deleteButton
     */
    public Button getDeleteButton() {
        return deleteButton;
    }

    /**
     * @param deleteButton
     *            the deleteButton to set
     */
    public void setDeleteButton(Button deleteButton) {
        this.deleteButton = deleteButton;
    }

    /**
     * @return the saveButton
     */
    public Button getSaveButton() {
        return saveButton;
    }

    /**
     * @param saveButton
     *            the saveButton to set
     */
    public void setSaveButton(Button saveButton) {
        this.saveButton = saveButton;
    }

    /**
     * @return the resetButton
     */
    public Button getResetButton() {
        return resetButton;
    }

    /**
     * @param resetButton
     *            the resetButton to set
     */
    public void setResetButton(Button resetButton) {
        this.resetButton = resetButton;
    }

    /**
     * @return the lastMouseClick
     */
    public Point getLastMouseClick() {
        return lastMouseClick;
    }

    /**
     * @param lastMouseClick
     *            the lastMouseClick to set
     */
    public void setLastMouseClick(Point lastMouseClick) {
        this.lastMouseClick = lastMouseClick;
    }

    /**
     * Set lastMouseClick from x and y
     * 
     * @param x
     * @param y
     */
    public void setLastMouseClick(int x, int y) {
        this.lastMouseClick.x = x;
        this.lastMouseClick.y = y;
    }

    /**
     * @return the fieldsTable
     */
    public Table getFieldsTable() {
        return fieldsTable;
    }

    /**
     * @param fieldsTable
     *            the fieldsTable to set
     */
    public void setFieldsTable(Table fieldsTable) {
        this.fieldsTable = fieldsTable;
    }

    /**
     * @return the itemsSet
     */
    public List<TableItem> getItemList() {
        return itemList;
    }

    /**
     * @param itemsSet
     *            the itemsSet to set
     */
    public void setItemList(List<TableItem> itemList) {
        this.itemList = itemList;
    }

    /**
     * @return the verticalButtons
     */
    public boolean getVerticalButtons() {
        return verticalButtons;
    }

    /**
     * @param verticalButtons
     *            the verticalButtons to set
     */
    public void setVerticalButtons(boolean verticalButtons) {
        this.verticalButtons = verticalButtons;
    }

}
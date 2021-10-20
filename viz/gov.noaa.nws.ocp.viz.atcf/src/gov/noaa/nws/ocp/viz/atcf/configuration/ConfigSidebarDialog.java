/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
**/
package gov.noaa.nws.ocp.viz.atcf.configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.SidebarMenuEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.SidebarMenuSelection;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 *
 * Dialog for "Configure" => "Change Sidebar Menu Selections".
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ --------  ----------- --------------------------
 * Sep 14, 2018 54781     jwu         Initial creation
 * Oct 03, 2018 55873     jwu         Move retrieval of sidebar commands to
 *                                    AtcfVizUtil & remove actual command from GUI.
 * Nov 10, 2020 84442     wpaintsil   Add Scrollbars.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ConfigSidebarDialog extends OcpCaveSWTDialog {

    // A string indicate a separator entry in sidebar menu
    private static final String MENU_SEPARATER = "---";

    // Map of all registered ATCF sidebar commands
    private static Map<String, String> atcfCommandMap = AtcfVizUtil
            .getSidebarCommands();

    // Menu entry table
    private Table entryTable;

    /**
     * A Map containing editors for each TableItem in the table.
     */
    private Map<TableItem, TableEditor> editors = new LinkedHashMap<>();

    /**
     * Constructor.
     *
     * @param parent
     */
    public ConfigSidebarDialog(Shell parent) {
        super(parent, SWT.MIN | SWT.CLOSE | SWT.BORDER | SWT.TITLE
                | SWT.PRIMARY_MODAL, CAVE.DO_NOT_BLOCK);

        setText("Change Sidebar Menu Selections");

        // Initialize "returnValue" to "false" to indicate no changes so far.
        this.setReturnValue(false);
    }

    /**
     * Create dialog.
     *
     * @param shell
     *            Parent shell
     */
    @Override
    protected void initializeComponents(final Shell shell) {
        shell.setLayout(new GridLayout(1, false));

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite mainComp = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, true);
        mainComp.setLayout(mainLayout);

        Composite entryComp = new Composite(mainComp, SWT.NONE);
        entryComp.setLayout(new GridLayout(2, false));
        GridData entryCompGd = new GridData(SWT.FILL, SWT.TOP, true, true, 2,
                1);
        entryComp.setLayoutData(entryCompGd);

        buildEntryTable(entryComp);

        createControlButtons(mainComp);

        loadEntries();

        /*
         * It is necessary to add at least one item to the table in order for
         * getItemHeight to be somewhat accurate.
         */
        GridData entryTableGd = new GridData(SWT.FILL, SWT.TOP, true, true, 1,
                1);
        Rectangle entryTableTrim = entryTable.computeTrim(0, 0, 0,
                20 * entryTable.getItemHeight());
        Point sz = entryTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        entryTableGd.widthHint = sz.x + entryTableTrim.width - entryTableTrim.x;
        entryTableGd.heightHint = entryTableTrim.height - entryTableTrim.y;
        entryTable.setLayoutData(entryTableGd);

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Build the menu selection table
     *
     * @param parent
     */
    private void buildEntryTable(Composite parent) {

        // Add up/down arrows at left to move selected row up/down.
        Composite arrowComp = new Composite(parent, SWT.NONE);
        GridLayout arrowLayout = new GridLayout(1, false);
        arrowLayout.marginLeft = 15;
        arrowLayout.marginRight = 3;
        arrowLayout.horizontalSpacing = 0;

        arrowComp.setLayout(arrowLayout);

        Button upButton = new Button(arrowComp, SWT.ARROW | SWT.UP);
        upButton.setToolTipText("Move Up");
        Button downButton = new Button(arrowComp, SWT.ARROW | SWT.DOWN);
        downButton.setToolTipText("Move Down");
        upButton.setEnabled(true);
        downButton.setEnabled(true);

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

        // Add a table at right to display all entries.
        Composite cmdComp = new Composite(parent, SWT.NONE);
        GridLayout cmdLayout = new GridLayout(1, false);
        cmdLayout.marginLeft = 3;
        cmdLayout.marginRight = 15;

        cmdComp.setLayout(cmdLayout);

        entryTable = new Table(cmdComp,
                SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        entryTable.setHeaderVisible(true);
        entryTable.setLinesVisible(true);

        int fontWidth = AtcfVizUtil.getCharWidth(entryTable);

        TableColumn checkColumn = new TableColumn(entryTable, SWT.NONE);
        checkColumn.setWidth(8 * fontWidth);
        checkColumn.setText("Show");

        TableColumn codeColumn = new TableColumn(entryTable, SWT.NONE);
        codeColumn.setWidth(25 * fontWidth);
        codeColumn.setText("Command Name");

        TableColumn nameColumn = new TableColumn(entryTable, SWT.NONE);
        nameColumn.setWidth(30 * fontWidth);
        nameColumn.setText("Command Alias");

    }

    /**
     * Build save and cancel buttons
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite btnComp = new Composite(parent, SWT.NONE);
        GridLayout btnCompLayout = new GridLayout(2, true);
        btnCompLayout.horizontalSpacing = 50;
        btnCompLayout.marginLeft = 75;
        btnComp.setLayout(btnCompLayout);

        final Button saveBtn = new Button(btnComp, SWT.PUSH);
        GridData saveGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1,
                1);
        saveGd.widthHint = 200;
        saveBtn.setLayoutData(saveGd);
        saveBtn.setText("Save");

        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                save();
            }
        });

        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        GridData cancelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1,
                1);
        cancelGd.widthHint = 200;
        cancelBtn.setLayoutData(cancelGd);
        cancelBtn.setText("Cancel");

        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                close();
            }
        });
    }

    /*
     * Get menu selections from localization and display them.
     */
    private void loadEntries() {

        // Retrieve what stored in localization.
        SidebarMenuSelection menuSelections = AtcfConfigurationManager
                .getInstance().getSidebarMenuSelection();

        // If no selections stored in localization, build from default.
        if (menuSelections == null
                || menuSelections.getSidebarMenuSelection().isEmpty()) {
            menuSelections = buildDfltMenuSelections();
        }

        /*
         * Match against the defined ATCF commands.
         */
        for (SidebarMenuEntry entry : menuSelections
                .getSidebarMenuSelection()) {
            for (Map.Entry<String, String> cmdEntry : atcfCommandMap
                    .entrySet()) {
                if (cmdEntry.getKey().equalsIgnoreCase(entry.getName())) {
                    entry.setCommand(cmdEntry.getValue());
                    break;
                }
            }

            addEntry(entry, -1);
        }
    }

    /*
     * Build default menu selections.
     *
     * @return SidebarMenuSelection
     */
    private SidebarMenuSelection buildDfltMenuSelections() {

        SidebarMenuSelection menuSelections = new SidebarMenuSelection();

        for (String entryName : SidebarMenuSelection.getDefaultEntries()) {

            String cmd = SidebarMenuSelection.SIDEBAR_DEFAULT_COMMAND;

            for (Map.Entry<String, String> cmdEntry : atcfCommandMap
                    .entrySet()) {
                if (entryName.equalsIgnoreCase(cmdEntry.getKey())) {
                    cmd = cmdEntry.getValue();
                    break;
                }
            }

            SidebarMenuEntry entry = new SidebarMenuEntry(true, entryName,
                    entryName, cmd);
            menuSelections.getSidebarMenuSelection().add(entry);
        }

        return menuSelections;
    }

    /**
     * add an entry to the table
     *
     * @param entry
     *            Entry to be added
     * @param index
     *            index for adding an entry at.
     */
    private void addEntry(SidebarMenuEntry entry, int index) {

        if (editors == null) {
            editors = new HashMap<>();
        }

        // Create a table item.
        TableItem item;
        if (index >= 0) {
            item = new TableItem(entryTable, SWT.BORDER, index);
        } else {
            item = new TableItem(entryTable, SWT.BORDER);
        }

        item.setData(entry);

        // If entry be shown in sidebar.
        item.setChecked(entry.isShow());

        // Entry name.
        item.setText(1, entry.getName());

        // Entry alias - user can edit this one.
        String alias = entry.getAlias();
        if (alias == null || alias.trim().isEmpty()) {
            alias = entry.getName();
        }

        if (alias.startsWith(MENU_SEPARATER)) {
            item.setText(2, alias);
        } else {
            Text aliasTxt = new Text(entryTable, SWT.BORDER);
            aliasTxt.setText(alias);

            TableEditor aliasEditor = new TableEditor(entryTable);
            aliasEditor.grabHorizontal = true;
            aliasEditor.setEditor(aliasTxt, item, 2);

            editors.put(item, aliasEditor);
        }

    }

    /**
     * Move a selected item in the table up one row.
     */
    private void moveUp() {

        if (entryTable.getSelectionCount() > 0
                && entryTable.getSelectionCount() < 2
                && entryTable.getSelectionIndex() > 0) {

            int selectionIndex = entryTable.getSelectionIndex();

            SidebarMenuEntry shiftedData = (SidebarMenuEntry) entryTable
                    .getItem(selectionIndex).getData();

            removeTableItem(selectionIndex);

            addEntry(shiftedData, selectionIndex - 1);

            entryTable.pack(true);
            entryTable.getParent().layout(true);

            entryTable.setSelection(selectionIndex - 1);

        }
    }

    /**
     * Move a selected item in the table down one row.
     */
    private void moveDown() {

        if (entryTable.getSelectionCount() > 0
                && entryTable.getSelectionCount() < 2
                && entryTable.getSelectionIndex() < (entryTable.getItemCount()
                        - 1)) {
            int selectionIndex = entryTable.getSelectionIndex();

            SidebarMenuEntry selectedEntry = (SidebarMenuEntry) entryTable
                    .getItem(selectionIndex).getData();

            addEntry(selectedEntry, selectionIndex + 2);

            removeTableItem(selectionIndex);

            entryTable.pack(true);
            entryTable.getParent().layout(true);

            entryTable.setSelection(selectionIndex + 1);
        }
    }

    /**
     * Remove an item from the table
     *
     * @param index
     *            index of the item to be removed.
     */
    private void removeTableItem(int index) {

        TableItem item = entryTable.getItem(index);
        entryTable.remove(index);
        TableEditor editor = editors.get(item);
        if (editor != null) {
            Text txt = (Text) editor.getEditor();
            txt.dispose();
            editors.remove(item);
            editor.dispose();
        }

        item.dispose();
    }

    /**
     * Save the selections.
     */
    private void save() {

        TableItem[] entries = entryTable.getItems();
        SidebarMenuSelection menuSelections = new SidebarMenuSelection();

        for (TableItem item : entries) {
            SidebarMenuEntry menuEntry = (SidebarMenuEntry) item.getData();
            boolean show = item.getChecked();
            menuEntry.setShow(show);

            TableEditor editor = editors.get(item);
            if (editor != null) {
                Text txt = (Text) editor.getEditor();
                String alias = txt.getText();
                if (!alias.isEmpty()) {
                    menuEntry.setAlias(alias);
                }
            }

            menuSelections.getSidebarMenuSelection().add(menuEntry);
        }

        AtcfConfigurationManager.getInstance()
                .saveSidebarMenuSelection(menuSelections);

        // Update the sidebar content.
        AtcfSession.getInstance().getSideBar().refresh();

    }

}
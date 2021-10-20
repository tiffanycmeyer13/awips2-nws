/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.raytheon.uf.common.util.Pair;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictRecordPair;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.notification.AtcfNotificationListeners;
import gov.noaa.nws.ocp.viz.atcf.notification.IAtcfNotificationListener;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Create a dialog with a toolbar to receive notifications.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2019 68118      wpaintsil   Initial creation.
 * Mar 18, 2021 89201      mporricelli Create submitAction()
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public abstract class NotificationToolbarDialog extends OcpCaveChangeTrackDialog
        implements IAtcfNotificationListener {

    /**
     * Conflict icon. Currently a yellow triangle with exclamation.
     */
    public static final Image ALERT_ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin("gov.noaa.nws.ocp.viz.atcf",
                    "icons/alert.png")
            .createImage();

    /**
     * Ready icon. Currently a yellow triangle with exclamation.
     */
    public static final Image READY_ICON = AbstractUIPlugin
            .imageDescriptorFromPlugin("gov.noaa.nws.ocp.viz.atcf",
                    "icons/ready.ico")
            .createImage();

    /**
     * A list of toolbar states.
     */
    protected enum ToolbarType {
        CONFLICT_DETECTED, READY_SUBMIT, NO_CHANGES
    }

    /**
     * Button width constant
     */
    private static final int BUTTON_WIDTH = 100;

    /**
     * Current toolbar state.
     */
    protected ToolbarType toolbarStatus = ToolbarType.NO_CHANGES;

    protected StackLayout stackLayout;

    // Current storm and sandbox id for Adeck.
    protected Storm storm;

    /**
     * Current sandbox ID
     */
    protected int sandboxID;

    /**
     * Current AtcfProduct
     */
    protected AtcfProduct atcfProduct;

    private Button submitBtn;

    /**
     * Stack Composite used to switch toolbar states
     */
    private Composite stackComp;

    /**
     * Composite containing conflict information
     */
    private Composite conflictComp;

    /**
     * Composite showing no changes
     */
    private Composite noChangeComp;

    /**
     * Composite showing the data is ready to submit
     */
    private Composite readyComp;

    /**
     * Dialog for merge functionality
     */
    private MergeDialog mergeDialog;

    /**
     * Flag for Ok button in merge dialog
     */
    private boolean shouldMerge = false;

    /**
     * Constructor
     *
     * @param parent
     * @param storm
     */
    protected NotificationToolbarDialog(Shell parent, Storm storm) {
        super(parent);
        this.storm = storm;

        /*
         * Register this to AtcfNotificationJobListeners to receive
         * notification.
         */
        AtcfNotificationListeners.getInstance().addListener(this);

        parent.addDisposeListener(e -> AtcfNotificationListeners.getInstance()
                .removeListener(NotificationToolbarDialog.this));

        atcfProduct = AtcfSession.getInstance().getAtcfResource()
                .getResourceData().getAtcfProduct(storm);

        mergeDialog = new MergeDialog(parent);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        final Composite mainComp = new Composite(scrollComposite, SWT.NONE);

        createNotificationToolbar(mainComp);

        createMainContent(mainComp);

        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 0;

        mainComp.setLayout(mainLayout);
        mainComp.setData(new GridData(SWT.FILL, SWT.FILL, true, false));

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Create the toolbar composites
     *
     * @param parent
     */
    private void createNotificationToolbar(Composite parent) {

        Composite topComp = new Composite(parent, SWT.NONE);

        GridLayout topLayout = new GridLayout(3, false);
        topLayout.marginHeight = 0;
        topLayout.verticalSpacing = 0;

        topComp.setLayout(topLayout);
        topComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createLabelComp(topComp);

        stackComp = new Composite(topComp, SWT.NONE);
        stackLayout = new StackLayout();
        stackComp.setLayout(stackLayout);
        stackComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        readyComp = showReady(stackComp);
        conflictComp = showConflict(stackComp);
        noChangeComp = showNoChanges(stackComp);

        if (atcfProduct.getCurrentNotification() != null) {
            stackLayout.topControl = conflictComp;
        } else {
            stackLayout.topControl = noChangeComp;
        }

        Composite submitComp = new Composite(topComp, SWT.NONE);

        GridLayout submitLayout = new GridLayout(1, false);
        submitLayout.marginHeight = 0;
        submitLayout.verticalSpacing = 0;

        submitComp.setLayout(submitLayout);
        submitComp.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, true, false));

        submitBtn = new Button(submitComp, SWT.PUSH);
        submitBtn.setText("Submit");
        submitBtn.setEnabled(false);
        GridData buttonData = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        buttonData.widthHint = BUTTON_WIDTH;
        submitBtn.setLayoutData(buttonData);

        submitBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                submitAction();
            }
        });

        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
    }

    /**
     * Submit changes to baseline and clean up
     */
    protected void submitAction() {
        AtcfDataUtil.checkinADeckRecords(sandboxID);

        AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                .getResourceData().getAtcfProduct(storm);

        prd.setAdeckSandboxID(-1);

        close();
    }

    /**
     * An "Editing Status" label
     *
     * @param parent
     * @return
     */
    private Composite createLabelComp(Composite parent) {

        Composite labelComp = new Composite(parent, SWT.NONE);

        GridLayout labelLayout = new GridLayout(1, true);
        labelLayout.marginHeight = 0;
        labelLayout.verticalSpacing = 0;

        labelComp.setLayout(labelLayout);
        labelComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

        Label editingLbl = new Label(labelComp, SWT.NONE);
        editingLbl.setText("Editing Status:");

        return labelComp;
    }

    /**
     * Composite showing "Ready to Submit"
     *
     * @param parent
     * @return
     */
    private Composite showReady(Composite parent) {

        Composite showReadyComp = new Composite(parent, SWT.NONE);

        GridLayout readyLblLayout = new GridLayout(2, false);
        readyLblLayout.marginHeight = 0;
        readyLblLayout.verticalSpacing = 0;

        showReadyComp.setLayout(readyLblLayout);
        showReadyComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        Label iconLbl = new Label(showReadyComp, SWT.NONE);
        iconLbl.setImage(READY_ICON);

        Label editingLbl = new Label(showReadyComp, SWT.NONE);
        editingLbl.setText("Ready to Submit");
        editingLbl
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        return showReadyComp;
    }

    /**
     * Composite showing potential conflict.
     *
     * @param parent
     * @return
     */
    private Composite showConflict(Composite parent) {
        Color redColor = parent.getDisplay().getSystemColor(SWT.COLOR_RED);

        Composite showConflictComp = new Composite(parent, SWT.BORDER);

        GridLayout conflictLayout = new GridLayout(2, true);
        conflictLayout.marginHeight = 0;
        conflictLayout.verticalSpacing = 0;

        showConflictComp.setLayout(conflictLayout);
        showConflictComp
                .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true));
        showConflictComp.setBackground(redColor);

        Composite conflictLblComp = new Composite(showConflictComp, SWT.NONE);

        GridLayout conflictLblLayout = new GridLayout(2, false);
        conflictLblLayout.marginHeight = 0;
        conflictLblLayout.verticalSpacing = 0;

        conflictLblComp.setLayout(conflictLblLayout);
        conflictLblComp
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
        conflictLblComp.setBackground(redColor);

        Label iconLbl = new Label(conflictLblComp, SWT.NONE);
        iconLbl.setImage(ALERT_ICON);
        iconLbl.setBackground(redColor);

        Label conflictLbl = new Label(conflictLblComp, SWT.NONE);
        conflictLbl.setText("Possible Conflict Detected");
        conflictLbl.setBackground(redColor);

        Composite conflictBtnComp = new Composite(showConflictComp, SWT.NONE);

        GridLayout conflictBtnLayout = new GridLayout(2, true);
        conflictBtnLayout.marginHeight = 0;
        conflictBtnLayout.verticalSpacing = 0;

        conflictBtnComp.setLayout(conflictBtnLayout);
        conflictBtnComp
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
        conflictBtnComp.setBackground(redColor);

        GridData buttonData = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        buttonData.widthHint = BUTTON_WIDTH;

        Button mergeBtn = new Button(conflictBtnComp, SWT.PUSH);
        mergeBtn.setText("Merge");
        mergeBtn.setToolTipText(
                "Update the currently saved data with the latest baseline data.");
        mergeBtn.setLayoutData(buttonData);
        mergeBtn.setBackground(redColor);
        mergeBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                mergeData();
            }
        });

        MessageBox restartDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        restartDialog.setText("Restart?");
        restartDialog.setMessage(
                "Currently edited data will be discarded and baseline data will be loaded.\nAre you sure you want to Restart?");

        Button restartBtn = new Button(conflictBtnComp, SWT.PUSH);
        restartBtn.setText("Start Over");
        restartBtn.setToolTipText("Discard all saved changes.");
        restartBtn.setLayoutData(buttonData);
        restartBtn.setBackground(redColor);
        restartBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int restart = restartDialog.open();

                if (restart == SWT.OK) {
                    restartData();
                    setToolbarStatus(ToolbarType.NO_CHANGES);
                }
            }
        });

        return showConflictComp;
    }

    /**
     * Composite showing no changes.
     *
     * @param parent
     * @return
     */
    private Composite showNoChanges(Composite parent) {

        Composite showNoChangeComp = new Composite(parent, SWT.NONE);

        GridLayout noChangeLayout = new GridLayout(1, true);
        noChangeLayout.marginHeight = 0;
        noChangeLayout.verticalSpacing = 0;

        showNoChangeComp.setLayout(noChangeLayout);
        showNoChangeComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        Label editingLbl = new Label(showNoChangeComp, SWT.NONE);
        editingLbl.setText("No Changes");
        editingLbl.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.CENTER, true, true));

        return showNoChangeComp;
    }

    /**
     * @return the toolbarStatus
     */
    public ToolbarType getToolbarStatus() {
        return toolbarStatus;
    }

    /**
     * @param toolbarStatus
     *            the toolbarStatus to set
     */
    public void setToolbarStatus(ToolbarType toolbarStatus) {
        this.toolbarStatus = toolbarStatus;

        switch (toolbarStatus) {
        case CONFLICT_DETECTED:
            stackLayout.topControl = conflictComp;
            submitBtn.setEnabled(false);
            break;
        case READY_SUBMIT:
            stackLayout.topControl = readyComp;
            submitBtn.setEnabled(true);
            break;
        default:
            stackLayout.topControl = noChangeComp;
            submitBtn.setToolTipText("No Changes to Submit");
            submitBtn.setEnabled(false);
            break;

        }
        stackComp.layout();
    }

    /**
     * Show the main content under the toolbar.
     *
     * @param mainComp
     */
    protected abstract void createMainContent(Composite mainComp);

    /**
     * Implement the "start over" functionality. Saved sandbox deck data should
     * be discarded and refreshed with baseline data.
     */
    protected abstract void restartData();

    /**
     * Merge functionality. Current sandbox edits should be combined with the
     * baseline edits.
     */
    protected void mergeData() {

        mergeDialog.open();

        if (shouldMerge) {
            // TODO: refresh data in fields with baseline data?
            shouldMerge = false;
        }

    }

    /**
     * Show the conflict message when the notification arrives.
     */
    @Override
    public void notificationArrived(AtcfDataChangeNotification notification) {
        Display.getDefault().asyncExec(() -> {
            String uid = AtcfSession.getInstance().getUid();
            if (notification != null
                    && notification.getAffectedSandboxes().length > 0
                    && !notification.getSourceUserId().equals(uid)
                    && !getShell().isDisposed()) {
                setToolbarStatus(ToolbarType.CONFLICT_DETECTED);
            }
        });
    }

    /**
     * <pre>
     * SOFTWARE HISTORY
     * Date         Ticket#     Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * Jun 11, 2019 68118      wpaintsil   Initial creation.
     *
     * </pre>
     *
     * @author wpaintsil
     *
     */
    private class MergeDialog extends OcpCaveSWTDialog {
        /**
         * List of conflicts
         */
        private List<ConflictRecordPair> conflicts;

        /**
         * List of changes to keep
         */
        private List<Map<String, String>> changesKept;

        public MergeDialog(Shell shell) {
            super(shell);
        }

        public MergeDialog(Shell shell, List<ConflictRecordPair> conflicts) {
            super(shell);
            this.conflicts = conflicts;
        }

        /**
         * @return the conflicts
         */
        public List<ConflictRecordPair> getConflicts() {
            return conflicts;
        }

        /**
         * @param conflicts
         *            the conflicts to set
         */
        public void setConflicts(List<ConflictRecordPair> conflicts) {
            this.conflicts = conflicts;
        }

        @Override
        protected void initializeComponents(Shell shell) {
            shell.setLayout(new GridLayout(2, true));
            buildTable();
            buildBottomButtons();

        }

        /**
         * Add list of conflict pairs
         *
         * @param table
         */
        private void addItems(Table table) {

            for (ConflictRecordPair pair : conflicts) {
                Map<String, Pair<String, String>> fields = pair
                        .getConflictedFields();

                for (Map.Entry<String, Pair<String, String>> conflictEntry : fields
                        .entrySet()) {
                    String conflictString = conflictEntry.getKey();
                    Pair<String, String> fieldPair = conflictEntry.getValue();

                    final TableItem item = new TableItem(table, SWT.BORDER);

                    item.setText(0, conflictString);
                    item.setText(1, fieldPair.getFirst());
                    item.setText(2, fieldPair.getSecond());

                    final Button checkBtn = new Button(table, SWT.CHECK);
                    checkBtn.setSelection(true);

                    final TableEditor checkEditor = new TableEditor(table);
                    checkEditor.grabHorizontal = true;

                    checkEditor.setEditor(checkBtn, item, 3);
                }
            }

        }

        /**
         * Build the table viewer.
         */
        private void buildTable() {
            Table conflictTable = new Table(shell,
                    SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            conflictTable.setHeaderVisible(true);
            conflictTable.setLinesVisible(true);

            int fontWidth = AtcfVizUtil.getCharWidth(conflictTable);

            TableColumn fieldColumn = new TableColumn(conflictTable, SWT.NONE);
            fieldColumn.setWidth(10 * fontWidth);
            fieldColumn.setText("Field");
            fieldColumn.setMoveable(false);
            fieldColumn.setResizable(false);

            TableColumn baselineColumn = new TableColumn(conflictTable,
                    SWT.NONE);
            baselineColumn.setWidth(12 * fontWidth);
            baselineColumn.setText("Baseline\nValue");
            baselineColumn.setMoveable(false);
            baselineColumn.setResizable(false);

            TableColumn personalColumn = new TableColumn(conflictTable,
                    SWT.NONE);
            personalColumn.setWidth(15 * fontWidth);
            personalColumn.setText("Personal\nEditing\nCopy");
            personalColumn.setMoveable(false);
            personalColumn.setResizable(false);

            TableColumn overwriteColumn = new TableColumn(conflictTable,
                    SWT.NONE);
            overwriteColumn.setWidth(12 * fontWidth);
            overwriteColumn.setText("Overwrite\nBaseline");
            overwriteColumn.setMoveable(false);
            overwriteColumn.setResizable(false);

            // addItems(conflictTable);

            GridData conflictTableGd = new GridData(SWT.FILL, SWT.TOP, true,
                    true, 2, 1);
            conflictTableGd.heightHint = 17 * conflictTable.getItemHeight();
            conflictTable.setLayoutData(conflictTableGd);
        }

        /**
         * build save and cancel buttons
         */
        private void buildBottomButtons() {

            Label separator = new Label(shell, SWT.HORIZONTAL | SWT.SEPARATOR);
            separator.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

            final Button okBtn = new Button(shell, SWT.PUSH);
            GridData okGd = new GridData(SWT.CENTER, SWT.CENTER, false, false,
                    1, 1);
            int fontWidth = AtcfVizUtil.getCharWidth(okBtn);

            okGd.widthHint = fontWidth * 22;
            okBtn.setLayoutData(okGd);
            okBtn.setText("OK");

            okBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    shouldMerge = true;
                    setToolbarStatus(ToolbarType.READY_SUBMIT);
                    close();
                }

            });

            Button cancelBtn = new Button(shell, SWT.PUSH);
            GridData cancelGd = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false, 1, 1);
            cancelGd.widthHint = okGd.widthHint;
            cancelBtn.setLayoutData(cancelGd);
            cancelBtn.setText("Cancel");

            cancelBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    close();
                }
            });
        }

        /**
         * @return the changesKept
         */
        public List<Map<String, String>> getChangesKept() {
            return changesKept;
        }

        /**
         * @param changesKept
         *            the changesKept to set
         */
        public void setChangesKept(List<Map<String, String>> changesKept) {
            this.changesKept = changesKept;
        }

    }

}

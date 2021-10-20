/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.main;

import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.SidebarMenuEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.SidebarMenuSelection;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.TrackColorUtil;
import gov.noaa.nws.ocp.viz.atcf.forecast.ForecastTrackDialog;
import gov.noaa.nws.ocp.viz.atcf.notification.AtcfNotificationListeners;
import gov.noaa.nws.ocp.viz.atcf.notification.AtcfNotificationObserver;
import gov.noaa.nws.ocp.viz.atcf.notification.IAtcfNotificationListener;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * Dialog for ATCF sidebar.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 08, 2018 34955      jwu         Initial creation.
 * May 11, 2018 50329      jwu         Update with all operational entries.
 * Jun 05, 2018 48178      jwu         Remove map-control buttons
 * Jul 05, 2018 52119      jwu         Link with command handler.
 * Sep 14, 2018 54781      jwu         Make sidebar entries configurable.
 * Oct 03, 2018 55873      jwu         Match commands to those defined in plugin.xml.
 * Feb 05, 2019 58990      jwu         Add storm switcher.
 * Feb 19, 2019 60097      jwu         Implemente "Remove" storm.
 * Mar 18, 2019 61605      jwu         Add track color option.
 * Mar 28, 2019 61882      jwu         Activate action with Use command name.
 * May 07, 2019 63005      jwu         Set name button color same as storm track color.
 * Jul 30, 2019 66618      dfriedman   Remove embedded menu bar.
 * Aug 23, 2019 65564      jwu         Start ATCF notification.
 * Aug 28, 2019 67881      jwu         Use AtcfDataChangeNotification
 * Apr 20, 2020 77478      jwu         Change sidebar command name to case-sensitive
 * Jun 11, 2020 68118      wpaintsil   Notification support for user concurrency support.
 * Aug 13, 2020 81881      dfriedman   Fix potential NPE.
 * Mar 13, 2021 90825      jnengel     Fix storm list disappearing on small screens & align display checkboxes.
 * May 03, 2021 91536      jwu         Reset size.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AtcfSideBar extends CaveJFACEDialog
        implements IPartListener2, IAtcfNotificationListener {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ForecastTrackDialog.class);

    /* Main widgets */
    private Composite top;

    private Group stormGrp;

    private List actionList;

    // User name combos
    private CCombo userCombo;

    /*
     * Map of all registered ATCF commands with their id as the key
     */
    private SidebarMenuSelection sidebarMenuSelections = null;

    /**
     * Preferred width and height of action list.
     */
    private static final int ACTION_LIST_CHARACTER_WIDTH = 26;

    private static final int ACTION_LIST_DISPLAYED_ITEMS = 28;

    /**
     * List of storms
     */
    private java.util.List<Storm> stormList = null;

    /**
     * Selection Counter
     */
    private int stormSelectionCounter = 0;

    /**
     * Constant indicating a nonexistent sandbox upon submission.
     */
    private static final int NONEXISTENT = -1;

    /**
     * An invalid sandbox
     */
    private static final int INVALID = -2;

    /**
     * An unchanged sandbox
     */
    private static final int UNCHANGED = -3;

    /**
     * Constructor
     */
    public AtcfSideBar(Shell parent) {
        super(parent);

        setShellStyle(SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER
                | SWT.TITLE | SWT.RESIZE);

        // Retrieve menu entries stored in localization set to be shown.
        sidebarMenuSelections = AtcfConfigurationManager.getInstance()
                .getSidebarMenuSelection().getAvailableSidebarMenuSelection();

        /*
         * Match against the defined ATCF sidebar commands.
         */
        Map<String, String> sidebarCommands = AtcfVizUtil.getSidebarCommands();
        for (SidebarMenuEntry entry : sidebarMenuSelections
                .getSidebarMenuSelection()) {
            for (Map.Entry<String, String> cmdEntry : sidebarCommands
                    .entrySet()) {
                if (cmdEntry.getKey().equals(entry.getName())) {
                    entry.setCommand(cmdEntry.getValue());
                    break;
                }
            }
        }

        // Set the default location relative to the main GUI.
        setDefaultLocation(parent);

        // Register to get notification if editor changes.
        IEditorPart editor = EditorUtil.getActiveEditor();
        editor.getSite().getPage().addPartListener(this);

        // List of available storms and geneses
        stormList = AtcfDataUtil.getFullStormList();

        /*
         * Register this to AtcfNotificationJobListeners to receive
         * notification.
         */
        AtcfNotificationListeners.getInstance().addListener(this);
    }

    /**
     * Create contents.
     *
     * @param parent
     */
    @Override
    protected Control createContents(Composite parent) {

        Composite comp = (Composite) super.createContents(parent);

        // Set the location of the dialog.
        getShell().setLocation(lastLocation);

        return comp;
    }

    /**
     * Create dialog area.
     *
     * @param parent
     */
    @Override
    public Control createDialogArea(Composite parent) {
        /*
         * Sets dialog title
         */
        getShell().setText("ATCF SideBar");

        top = (Composite) super.createDialogArea(parent);

        /*
         * Create the main layout for the shell.
         */
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginWidth = 15;
        mainLayout.marginHeight = 0;
        mainLayout.verticalSpacing = 15;
        top.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        top.setLayoutData(mainLayoutData);

        // Create section to switch between storms.
        createStormSwitcher();

        // Create action list
        createListArea(top);

        // TODO This will be removed or hidden later.

        // Create a list to change user name for testing....
        userCombo = new CCombo(top, SWT.BORDER);
        userCombo.add(System.getProperty("user.name"));
        userCombo.add("testUser1");
        userCombo.add("testUser2");
        userCombo.select(0);
        userCombo.setEditable(false);
        userCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfSession.getInstance().setUid(userCombo.getText());
            }
        });

        // Set this sidebar with the ATCF Session
        AtcfSession.getInstance().setSideBar(this);
        this.setBlockOnOpen(false);

        return top;
    }

    /**
     * Set up the list structure for action selection.
     *
     * @param top
     */
    private void createListArea(Composite top) {

        Group actionGrp = new Group(top, SWT.NONE);
        GridLayout actionGrpLayout = new GridLayout(1, true);
        actionGrp.setLayout(actionGrpLayout);
        actionGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        actionGrp.setText("Select an Action");

        actionList = new org.eclipse.swt.widgets.List(actionGrp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        Rectangle r = actionList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(actionList)
                        * ACTION_LIST_CHARACTER_WIDTH,
                actionList.getItemHeight() * ACTION_LIST_DISPLAYED_ITEMS);
        GridData listData = new GridData(SWT.FILL, SWT.FILL, true, true);
        listData.widthHint = r.width - r.x;
        listData.heightHint = r.height - r.y;
        actionList.setLayoutData(listData);

        for (SidebarMenuEntry entry : sidebarMenuSelections
                .getSidebarMenuSelection()) {
            actionList.add(entry.getAlias());
        }

        actionList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                stormSelectionCounter++;
                /*
                 * TODO SWT.List always select the first item. We want the list
                 * open with no item selected so a counter is used here to get
                 * around that. Maybe there is a better way to do it?
                 */
                if (stormSelectionCounter > 1) {
                    int index = ((org.eclipse.swt.widgets.List) e.widget)
                            .getSelectionIndex();
                    SidebarMenuEntry cmdEntry = sidebarMenuSelections
                            .getSidebarMenuSelection().get(index);

                    AtcfVizUtil.executeCommand(cmdEntry.getCommand(),
                            AtcfVizUtil.SIDEBAR_COMMAND_NAME,
                            cmdEntry.getName());
                } else {
                    actionList.deselectAll();
                }
            }
        });

        stormGrp.layout(true);
        stormGrp.pack();
    }

    /**
     * Set up the list structure for action selection.
     *
     * @param parent
     */
    public void createStormSwitcher() {

        // Find AtcfResource for current editor.
        AtcfResource drawingLayer = AtcfSession.getInstance().getAtcfResource();
        int numPrd = drawingLayer.getProducts() != null
                ? drawingLayer.getProducts().size() : 0;

        // Create or hide storm switcher.
        if (stormGrp != null && !stormGrp.isDisposed()) {
            for (Control ctrl : stormGrp.getChildren()) {
                ctrl.dispose();
            }
        } else {
            stormGrp = new Group(top, SWT.SHADOW_ETCHED_IN);
            stormGrp.setText("Storms");
            GridLayout stormGrpLayout = new GridLayout(3, false);

            stormGrpLayout.marginHeight = 0;
            stormGrpLayout.marginWidth = 1;
            stormGrpLayout.verticalSpacing = 1;
            stormGrpLayout.horizontalSpacing = 0;

            stormGrp.setLayout(stormGrpLayout);
            GridData stormCompGD = new GridData(SWT.FILL, SWT.FILL, true,
                    false);
            stormGrp.setLayoutData(stormCompGD);
        }

        stormGrp.setVisible(false);

        // Add storm(s) in current editor.
        if (numPrd > 0) {

            stormGrp.setVisible(true);

            Composite nameComp = new Composite(stormGrp, SWT.NONE);
            GridLayout nameCompLayout = new GridLayout(1, true);
            nameComp.setLayout(nameCompLayout);
            nameComp.setLayoutData(
                    new GridData(SWT.CENTER, SWT.FILL, false, false));

            Composite displayComp = new Composite(stormGrp, SWT.NONE);
            GridLayout displayCompLayout = new GridLayout(1, true);
            displayCompLayout.marginTop = 8;
            displayCompLayout.marginLeft = 6;
            displayCompLayout.verticalSpacing = 23;
            displayComp.setLayout(displayCompLayout);
            displayComp.setLayoutData(
                    new GridData(SWT.CENTER, SWT.FILL, false, false));

            Composite deleteComp = new Composite(stormGrp, SWT.NONE);
            GridLayout deleteCompLayout = new GridLayout(1, true);
            deleteComp.setLayout(deleteCompLayout);
            deleteComp.setLayoutData(
                    new GridData(SWT.CENTER, SWT.FILL, false, false));

            for (Product prd : drawingLayer.getProducts()) {

                Storm storm = findStorm(prd.getName());
                if (storm == null) {
                    continue;
                }

                String nameStr = storm.getStormName() + ", "
                        + storm.getStormId();
                Button nameBtn = new Button(nameComp, SWT.PUSH | SWT.LEFT);
                nameBtn.setText(nameStr);
                nameBtn.setData(storm);

                if (storm == drawingLayer.getResourceData().getActiveStorm()) {
                    Color clr = getStormTrackColor(storm, drawingLayer);
                    nameBtn.setBackground(clr);
                } else {
                    AtcfVizUtil.setDefaultBackground(nameBtn);
                }

                GridData nameBtnGD = new GridData(SWT.FILL, SWT.FILL, false,
                        false);
                nameBtn.setLayoutData(nameBtnGD);
                nameBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        Storm storm = (Storm) event.widget.getData();
                        Storm activeStorm = drawingLayer.getResourceData()
                                .getActiveStorm();
                        if (storm != activeStorm) {
                            Button btn = (Button) event.widget;
                            for (Control ctrl : nameComp.getChildren()) {
                                if (ctrl instanceof Button) {
                                    AtcfVizUtil.setDefaultBackground(ctrl);
                                }
                            }

                            // Set this one as active storm.
                            drawingLayer.getResourceData()
                                    .setActiveStorm(storm);
                            switchStorm(storm, activeStorm, drawingLayer);

                            Color clr = getStormTrackColor(storm, drawingLayer);
                            btn.setBackground(clr);
                        }
                    }
                });

                Button displayBtn = new Button(displayComp, SWT.CHECK);
                displayBtn.setSelection(prd.isOnOff());
                displayBtn.setToolTipText(
                        "Show/Hide display of storm " + nameStr);
                displayBtn.setData(storm);
                displayBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        Storm storm = (Storm) event.widget.getData();
                        boolean onOff = displayBtn.getSelection();
                        for (Product prd : drawingLayer.getProducts()) {
                            if (storm.getStormId().equals(prd.getName())) {
                                prd.setOnOff(onOff);

                                AbstractEditor editor = AtcfVizUtil
                                        .getActiveEditor();
                                if (editor != null) {
                                    editor.refresh();
                                }

                                break;
                            }
                        }
                    }
                });

                Button deleteBtn = new Button(deleteComp, SWT.PUSH);
                deleteBtn.setText("X");
                deleteBtn.setToolTipText(
                        "Remove " + nameStr + " from the list.");
                deleteBtn.setData(storm);
                deleteBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        Storm storm = (Storm) event.widget.getData();
                        removeStorm(storm, drawingLayer);
                    }
                });
            }

            stormGrp.layout(true);

        }

        getShell().pack();

    }

    /**
     * Remove buttons;
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // Not used.
    }

    /**
     * Close & remember the last location.
     */
    @Override
    public boolean close() {
        Shell shell = getShell();
        if (shell != null && !shell.isDisposed()) {
            Rectangle bounds = shell.getBounds();
            lastLocation = new Point(bounds.x, bounds.y);
        }

        for (AbstractEditor edt : AtcfSession.getInstance().getEditors()) {
            AtcfResource rsc = AtcfVizUtil.findAtcfResource(edt);
            if (rsc != null) {
                IDescriptor idesc = edt.getActiveDisplayPane().getDescriptor();
                if (!idesc.getResourceList().isEmpty()) {
                    idesc.getResourceList().removeRsc(rsc);
                }
            }
        }

        // Deactivate ATCF context.
        AtcfVizUtil.deactivateAtcfContext();

        // Reset storm counter.
        stormSelectionCounter = 0;

        // Remove this from receiving notification.
        AtcfNotificationListeners.getInstance().removeListener(this);

        // Unregister ATCF notification observer.
        AtcfNotificationObserver.unregister();

        return super.close();
    }

    /**
     * Set the default location.
     *
     * @param parent
     */
    private void setDefaultLocation(Shell parent) {
        if (lastLocation == null) {
            lastLocation = parent.getLocation();
            lastLocation.y += 160;
        }
    }

    /**
     * De-select the selected action.
     *
     * @param parent
     */
    public void deselect() {
        actionList.deselectAll();
    }

    /**
     * Refresh the action list.
     */
    public void refresh() {
        // Retrieve menu entries stored in localization set to be shown.
        sidebarMenuSelections = AtcfConfigurationManager.getInstance()
                .getSidebarMenuSelection().getAvailableSidebarMenuSelection();

        actionList.removeAll();

        for (SidebarMenuEntry entry : sidebarMenuSelections
                .getSidebarMenuSelection()) {
            actionList.add(entry.getAlias());
        }
    }

    @Override
    public void partActivated(IWorkbenchPartReference partRef) {

        String id = "";
        AbstractVizPerspectiveManager pMngr = VizPerspectiveListener
                .getCurrentPerspectiveManager();
        if (pMngr != null) {
            id = pMngr.getPerspectiveId();
        }

        // Only activate ATCD sidebar for D2D and NCP perspectives.
        if ((id.contains("D2D") || id.contains("NCP")) && top != null
                && !top.isDisposed()) {
            if (!top.isVisible()) {
                this.restore();
            }

            createStormSwitcher();
        }
    }

    @Override
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // Not implemented
    }

    @Override
    public void partClosed(IWorkbenchPartReference partRef) {
        // Not implemented
    }

    @Override
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // Hide the ATCF sidebar.
        this.hide();
    }

    @Override
    public void partOpened(IWorkbenchPartReference partRef) {
        // Not implemented
    }

    @Override
    public void partHidden(IWorkbenchPartReference partRef) {
        // Not implemented
    }

    @Override
    public void partVisible(IWorkbenchPartReference partRef) {
        // Not implemented
    }

    @Override
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // Not implemented
    }

    /**
     * Find a storm with id
     *
     * @param stormID
     *
     * @return Storm
     */
    private Storm findStorm(String stormID) {
        Storm storm = null;
        for (Storm stm : stormList) {
            if (stormID.equals(stm.getStormId())) {
                storm = stm;
                break;
            }
        }

        return storm;
    }

    /**
     * Remove a storm from a AtcfResource
     *
     * @param storm
     *            Storm
     * @param rsc
     *            AtcfResource
     */
    private void removeStorm(Storm storm, AtcfResource rsc) {
        // Ask user for confirmation
        MessageDialog dialog = new MessageDialog(getShell(),
                "Remove storm " + storm.getStormName(), null,
                "Are you sure you want to remove storm " + storm.getStormName()
                        + " " + storm.getStormId() + "?",
                MessageDialog.WARNING, new String[] { "Yes", "No" }, 1);
        if (dialog.open() == Window.OK) {

            AtcfProduct prd = rsc.getResourceData().getAtcfProduct(storm);

            // Check if this is the active storm
            int index = -1;
            if (prd == rsc.getActiveProduct()) {
                index = rsc.getProducts().indexOf(prd);
                if ((index + 1) == rsc.getProducts().size()) {
                    index--;
                }
            }

            // Remove the product.
            rsc.removeProduct(prd);

            // Reset the active storm to the next one
            if (index >= 0) {
                Storm activeStorm = ((AtcfProduct) rsc.getProducts().get(index))
                        .getStorm();
                rsc.getResourceData().setActiveStorm(activeStorm);

                switchStorm(activeStorm, null, rsc);
            }

            // Refresh display.
            AbstractEditor editor = AtcfVizUtil.getActiveEditor();
            if (editor != null) {
                editor.refresh();
            }

            // Update sidebar.
            createStormSwitcher();
        }
    }

    /**
     * Switch active storm to another storm in an AtcfResource
     *
     * @param storm
     *            Storm to be switched to as active storm
     * @param storm
     *            Storm to be switched from being active storm
     * @param rsc
     *            AtcfResource
     */
    private void switchStorm(Storm storm1, Storm storm2, AtcfResource rsc) {

        // Draw storm one with active storm color
        if (storm1 != null) {
            BestTrackProperties prop1 = rsc.getResourceData()
                    .getAtcfProduct(storm1).getBestTrackProperties();
            if (prop1 != null) {
                BestTrackGenerator btg1 = new BestTrackGenerator(rsc, prop1,
                        storm1);
                btg1.create();
            }
        }

        // Draw storm one with its original color.
        if (storm2 != null) {
            BestTrackProperties prop2 = rsc.getResourceData()
                    .getAtcfProduct(storm2).getBestTrackProperties();

            if (prop2 != null) {
                BestTrackGenerator btg2 = new BestTrackGenerator(rsc, prop2,
                        storm2);
                btg2.create();
            }
        }

    }

    /**
     * Switch active storm to another storm in an AtcfResource
     *
     * @param storm
     *            Storm to be switched to as active storm
     * @param storm
     *            Storm to be switched from being active storm
     * @param rsc
     *            AtcfResource
     */
    private Color getStormTrackColor(Storm storm, AtcfResource rsc) {
        TrackColorUtil tcu = new TrackColorUtil();
        BestTrackProperties attr = rsc.getResourceData().getAtcfProduct(storm)
                .getBestTrackProperties();

        int clrInd = 0;
        if (attr != null) {
            clrInd = attr.getTrackColor();
        }

        java.awt.Color awtColor = tcu.getAtcfCustomColor(clrInd);
        return new Color(awtColor.getRed(), awtColor.getGreen(),
                awtColor.getBlue());
    }

    /**
     * Accepts the notification from EDEX, and take further actions.
     *
     */
    @Override
    public void notificationArrived(AtcfDataChangeNotification notification) {

        int submitId = notification.getSourceSandboxId();

        switch (submitId) {
        case NONEXISTENT:
            logger.warn("The sandbox does not exist.");
            break;
        case INVALID:
            logger.warn("The sandbox is invalid.");
            break;
        case UNCHANGED:
            logger.warn("The sandbox is unchanged.");
            break;
        default:
            break;
        }

        AtcfProduct prd = (AtcfProduct) AtcfSession.getInstance()
                .getAtcfResource().getActiveProduct();

        if (!AtcfSession.getInstance().getSideBar().isDisposed()) {
            String uid = AtcfSession.getInstance().getUid();
            if (!notification.getSourceUserId().equals(uid)
                    && notification.getAffectedSandboxes().length > 0) {

                prd.setCurrentNotification(notification);
            }
        }

    }

}
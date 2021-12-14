/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.SortedSet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesDialog;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesProperties;
import gov.noaa.nws.ocp.viz.atcf.fixes.DrawFixesElements;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;

/**
 * Handler for "ATCF Display Fixes" request, as well as all fixes displaying
 * actions from sidebar.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jun 06, 2017 ?           bhebbard    Initial creation
 * Jun 05, 2018 48178       jwu         Updated.
 * Jul 18, 2018 52658       jwu         Retrieve data from DB & localization.
 * Oct 26, 2018 54780       jwu         Move most code to dialog.
 * Dec 05, 2018 57484       jwu         Handle sidebar actions with this same handler.
 * Apr 10, 2018 62427       jwu         Fix exception when command is issued from menu.
 * Jul 26, 2019 66465       jwu         Open dialog only with data found
 *
 * </pre>
 * 
 * @author b. hebbard
 * @version 0.0.1
 */
public class DisplayFixesHandler extends AbstractAtcfTool {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayFixesHandler.class);

    // Commands issued from Sidebar
    private static final String TOGGLE_FIXES = "fixes";

    private static final String FIX_AUTOLABEL = "fix autolabel";

    private static final String FIX_WINDRADII = "fix wind radii";

    private static final String FIX_CONFIDENCES = "fix confidences";

    // Map editor
    protected AbstractEditor mapEditor = null;

    // Current ATCF resource
    protected AtcfResource drawingLayer = null;

    // Instance of DisplayFixesDialog
    protected DisplayFixesDialog displayFixesDialog = null;

    /**
     * Display F-Deck data with/without dialogs.
     * 
     * @param event
     *            ExecutionEvent
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        // Sanity check - the storm should have been selected at this point.
        Storm curStorm = AtcfSession.getInstance().getActiveStorm();
        if (curStorm == null) {
            logger.error("DisplayFixesHandler: No active storm is selected.");
            return;
        }

        // Get the current AtcfResource
        drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Handle sidebar commands to erase or redraw without dialog.
        boolean proceedWithDialog = handleSidebarCommands();

        // Only proceed when dialog need to be opened for drawing.
        if (proceedWithDialog) {
            drawWithDialog(event, curStorm);
        }
    }

    /*
     * Open dialog and draw F-Deck data
     *
     * @param event ExecutionEvent
     *
     * @param curStorm current storm
     */
    private void drawWithDialog(ExecutionEvent event, Storm curStorm) {

        // Get the instance of DisplayFixesDialog.
        Shell shell = HandlerUtil.getActiveShell(event);

        if (displayFixesDialog == null) {
            try {
                displayFixesDialog = DisplayFixesDialog.getInstance(shell);
            } catch (Exception e) {
                logger.error("Cannot create DisplayObjectiveAidsDialog.", e);
                return;
            }
        }

        // Use the last-used display attributes; if not, use the default one.
        DisplayFixesProperties lastAttr = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getDisplayFixesProperties();

        if (lastAttr != null) {
            displayFixesDialog.setDisplayProperties(lastAttr);
        }

        DisplayFixesProperties dispAttr = displayFixesDialog
                .getDisplayProperties();

        // Retrieve all the F-deck records for the current storm
        List<FDeckRecord> pdos = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getFDeckData();

        if (pdos.isEmpty()) {
            pdos = AtcfDataUtil.getFDeckRecords(curStorm);

            if (pdos == null || pdos.isEmpty()) {
                logger.warn("No fixes data found for storm "
                        + curStorm.getStormName() + ". Quit...");
                return;
            }

            // Store data to ATCF Resource
            drawingLayer.getResourceData().getActiveAtcfProduct()
                    .setFDeckData(pdos);

            // Determine all date/time groups (DTGs) appearing in retrieved
            // records
            SortedSet<String> timeSet = AtcfDataUtil
                    .getFDeckDateTimeGroups(pdos);

            dispAttr.setAvailableDateTimeGroups(timeSet.toArray(new String[0]));

            if (!timeSet.isEmpty()) {
                dispAttr.setDateTimeGroup(timeSet.last());
            }
        }

        // Open dialog
        displayFixesDialog.setBlockOnOpen(false);
        displayFixesDialog.open();
    }

    /**
     * Handle commands issued from sidebar to erase or redraw fixes with
     * options.
     *
     * <pre>
     *
     *     "fixes" - if there are fixes elements in the F-Deck layer, erase them. 
     *               Otherwise, open dialog to draw fixes. 
     *     "fix autolabel" - if there are fixes elements in the F-Deck layer, flip 
     *               "autolabel" flag and redraw. Otherwise, just exit the command. 
     *     "fix wind radii" - similar as "fix autolabel", but apply to wind radii. 
     *     "fix confidences" - similar as "fix autolabel", but apply to confidences;
     * </pre>
     * 
     * @return continueWithDialog boolean
     */
    private boolean handleSidebarCommands() {

        boolean continueWithDialog = true;

        if (sidebarCmdName != null) {
            AtcfProduct prd = drawingLayer.getResourceData()
                    .getActiveAtcfProduct();
            List<AbstractDrawableComponent> elems = prd.getFDeckLayer()
                    .getDrawables();

            // Check if need to continue normal drawing with dialog from here.
            if (!sidebarCmdName.equals(TOGGLE_FIXES) || !elems.isEmpty()) {
                continueWithDialog = false;
            }

            // Proceed if there are elements being drawn before.
            if (!elems.isEmpty()) {
                DrawFixesElements drawFixes = new DrawFixesElements();

                if (sidebarCmdName.equals(TOGGLE_FIXES)) {
                    drawFixes.remove();
                } else {
                    // Flip flags.
                    DisplayFixesProperties attr = prd
                            .getDisplayFixesProperties();
                    boolean toggleAttrs = flipDisplayFlags(sidebarCmdName,
                            attr);

                    // Redraw
                    if (toggleAttrs) {
                        drawFixes.setDisplayProperties(attr);
                        drawFixes.draw();
                    }
                }
            }
        }

        return continueWithDialog;
    }

    /*
     * Flips the flags in a DisplayFixesProperties based on command name.
     *
     * @param cmdName
     *
     * @param attr DisplayFixesProperties
     * 
     * @return boolean Flag to recreate elements.
     */
    private boolean flipDisplayFlags(String cmdName,
            DisplayFixesProperties attr) {
        boolean toggleAttrs = true;
        if (cmdName.equals(FIX_AUTOLABEL)) {
            attr.setAutoLabel(!attr.getAutoLabel());
        } else if (cmdName.equals(FIX_WINDRADII)) {
            attr.setWindRadii(!attr.getWindRadii());
        } else if (cmdName.equals(FIX_CONFIDENCES)) {
            attr.setConfidence(!attr.getConfidence());
        } else {
            toggleAttrs = false;
        }

        return toggleAttrs;
    }

}

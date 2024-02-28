/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.aids.DisplayObjectiveAidsDialog;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsGenerator;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsProperties;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;

/**
 * Handler for "ATCF DisplayObjective Aids" request.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jun 20, 2017 ?           bhebbard    Initial creation
 * Jun 05, 2018 48178       jwu         Updated.
 * Jun 21, 2018 51863       jwu         Test data retrieval and drawing for aal052017.
 * Jul 05, 2018 52119       jwu         Linked with storm chooser.
 * Aug 21, 2018 53949       jwu         Move drawing to DisplayObjectiveAidsDialog.
 * Apr 11, 2019 62487       jwu         Decouple GUI with draw options.
 * May 07, 2019 63005       jwu         Revised logic to handle menu commands.
 * Juy 26, 2019 66502       jwu         Enable "GPCE climatology".
 *
 * </pre>
 * 
 * @author b. hebbard
 * @version 0.0.1
 */
public class DisplayObjectiveAidsHandler extends AbstractAtcfTool {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayObjectiveAidsHandler.class);

    // Commands issued from Aids menu
    private static final String DISPLAY_OBJ_AIDS = "Display Objective Aids";

    private static final String DISPLAY_AID_INTENSITIES = "Display Aid Intensities";

    // Commands issued from Sidebar
    private static final String TOGGLE_OBJAIDS = "obj aids";

    private static final String AIDS_INTENSITIES = "aid intensities";

    private static final String AIDS_34KT_RADII = "aid 34-kt radii";

    private static final String AIDS_50KT_RADII = "aid 50-kt radii";

    private static final String AIDS_64KT_RADII = "aid 64-kt radii";

    private static final String AIDS_GPCE = "GPCE";

    private static final String AIDS_GPCE_CLIMATOLOGY = "GPCE climatology";

    private static final String AIDS_GPCE_AX = "GPCE-AX";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.
     * commands.ExecutionEvent)
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        // Sanity check - the storm should have been selected at this point.
        Storm curStorm = AtcfSession.getInstance().getActiveStorm();
        if (curStorm == null) {
            logger.error("No storm has been selected yet.");
            return;
        }

        // Handle sidebar commands to erase or redraw without dialog.
        boolean proceedWithDialog = handleSidebarCommands();

        // Only proceed when dialog need to be opened for drawing.
        if (proceedWithDialog) {
            drawWithDialog(event, curStorm);
        }

    }

    /*
     * Open dialog and draw A-Deck data
     *
     * @param event ExecutionEvent
     *
     * @param curStorm current storm
     */
    private void drawWithDialog(ExecutionEvent event, Storm curStorm) {

        // Get the instance of DisplayObjectiveAidsDialog.
        Shell shell = HandlerUtil.getActiveShell(event);

        DisplayObjectiveAidsDialog displayObjectiveAidsDialog = null;

        try {
            displayObjectiveAidsDialog = DisplayObjectiveAidsDialog
                    .getInstance(shell);
        } catch (Exception e) {
            logger.error("Cannot create DisplayObjectiveAidsDialog.", e);
            return;
        }

        // Use the last-used display attributes; if not, use the default one.
        // Get the current AtcfResource
        AtcfResource drawingLayer = AtcfSession.getInstance().getAtcfResource();
        ObjAidsProperties lastAttr = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getObjAidsProperties();

        if (lastAttr != null) {
            displayObjectiveAidsDialog.setObjAidsProperties(lastAttr);
        }

        /*
         * Populate the dialog with the date/time groups (DTGs) and objective
         * aid techniques appearing in retrieved records
         */
        List<String> allDTG = AtcfDataUtil
                .getDateTimeGroups(curStorm);

        if (allDTG.isEmpty()) {
            logger.warn("No Obj Aids data found for storm "
                    + curStorm.getStormName() + ". Quit...");
            return;
        }

        Set<String> aidsFound = AtcfDataUtil.getObjAidTechniques(curStorm);

        if (aidsFound.isEmpty()) {
            logger.warn("No Obj Aids techniques found for storm "
                    + curStorm.getStormName() + ". Quit...");
            return;
        }

        ObjAidsProperties objAidsProperties = displayObjectiveAidsDialog
                .getObjAidsProperties();

        objAidsProperties.setAvailableDateTimeGroups(
                allDTG.toArray(new String[allDTG.size()]));
        objAidsProperties.setSelectedDateTimeGroups(
                new String[] { allDTG.get(allDTG.size() - 1) });

        objAidsProperties.setActiveObjectiveAids(aidsFound);

        /*
         * Open dialog and draw the initial drawing with the latest DTG and
         * default techniques.
         */
        displayObjectiveAidsDialog.setBlockOnOpen(false);

        ObjAidsGenerator obj = displayObjectiveAidsDialog.getObjAidsGenerator();
        obj.setObjAidsProperties(objAidsProperties);

        displayObjectiveAidsDialog.open();
        displayObjectiveAidsDialog.retrieveData();

        displayObjectiveAidsDialog.getObjAidsGenerator().draw(true, false);
    }

    /**
     * Handle commands issued from sidebar or menu to erase or redraw obj aids
     * with options.
     *
     * <pre>
     *
     *     Sidebar commands:
     *
     *    "obj aids"        - if there are obj aids elements in the A-Deck layer, erase them;
     *                        otherwise, open dialog to draw obj aids.
     *    "aid intensities" - draw obj aid intensities.
     *    "aid 34-kt radii" - draw 34-kt wind radii for selected DTGs.
     *    "aid 50-kt radii" - similar to "aid 34-kt radii".
     *    "aid 64-kt radii" - similar to "aid 34-kt radii".
     *    "GPCE"            - draw obj aid GPCE
     *    "GPCE Climatology"- draw obj aid GPCE Climatology.
     *    "GPCE-AX"         - draw obj aid GPCE-AX.
     *
     *    Aids menu commands:
     *
     *    "Display Aid Intensities" - same as "aid intensities".
     *
     * </pre>
     * 
     * @return continueWithDialog boolean
     */
    private boolean handleSidebarCommands() {

        AtcfResource drawingLayer = AtcfSession.getInstance().getAtcfResource();

        boolean continueWithDialog = false;

        String cmdName = commandName;
        if (sidebarCmdName != null) {
            cmdName = sidebarCmdName;
        }

        if (cmdName != null) {
            AtcfProduct prd = drawingLayer.getResourceData()
                    .getActiveAtcfProduct();
            List<AbstractDrawableComponent> elems = prd.getADeckLayer()
                    .getDrawables();

            // Check if need to continue normal drawing with dialog from here.
            continueWithDialog = (cmdName.equals(DISPLAY_OBJ_AIDS)
                    || (cmdName.equals(TOGGLE_OBJAIDS) && elems.isEmpty()));

            // Proceed if there are elements being drawn before.
            if (!elems.isEmpty()) {
                updateDisplay(cmdName, prd.getObjAidsProperties());
            }
        }

        return continueWithDialog;
    }

    /*
     * Update the display.
     *
     * @param cmdName
     *
     * @param attr ObjAidsProperties
     */
    private void updateDisplay(String cmdName, ObjAidsProperties attr) {
        ObjAidsGenerator objAidsGenenator = new ObjAidsGenerator();

        if (cmdName.equals(TOGGLE_OBJAIDS)) {
            objAidsGenenator.remove();
        } else {
            // Flip flags.
            boolean toggleAttrs = flipDisplayFlags(cmdName, attr);

            // Redraw
            if (toggleAttrs) {
                objAidsGenenator.setObjAidsProperties(attr);
                objAidsGenenator.draw();
            }
        }
    }

    /*
     * Flips the flags in an ObjAidsProperties based on command name.
     *
     * @param cmdName
     *
     * @param attr ObjAidsProperties
     * 
     * @return boolean Flag to recreate elements.
     */
    private boolean flipDisplayFlags(String cmdName, ObjAidsProperties attr) {

        boolean toggleAttrs = true;
        if (cmdName.equals(AIDS_INTENSITIES)
                || cmdName.equals(DISPLAY_AID_INTENSITIES)) {
            attr.setDisplayAidIntensities(!attr.isDisplayAidIntensities());
        } else if (cmdName.equals(AIDS_34KT_RADII)) {
            attr.setShow34ktWindRadii(!attr.isShow34ktWindRadii());
        } else if (cmdName.equals(AIDS_50KT_RADII)) {
            attr.setShow50ktWindRadii(!attr.isShow50ktWindRadii());
        } else if (cmdName.equals(AIDS_64KT_RADII)) {
            attr.setShow64ktWindRadii(!attr.isShow64ktWindRadii());
        } else if (cmdName.equals(AIDS_GPCE)) {
            attr.setGpce(!attr.isGpce());
        } else if (cmdName.equals(AIDS_GPCE_CLIMATOLOGY)) {
            attr.setGpceClimatology(!attr.isGpceClimatology());
        } else if (cmdName.equals(AIDS_GPCE_AX)) {
            attr.setGpceAX(!attr.isGpceAX());
        } else {
            toggleAttrs = false;
        }

        return toggleAttrs;
    }

}
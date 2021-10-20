/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;

/**
 * Handler for ATCF best track displaying commands from sidebar or Track menu.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Mar 26, 2019 61613       jwu         Initial creation
 * Mar 28, 2019 61882       jwu         Add all sidebar actions.
 * Apr 10, 2018 62427       jwu         Handle commands from track menu.
 *
 * </pre>
 * 
 * @author jwu
 * @version 0.0.1
 */
public class DisplayBestTrackHandler extends AbstractAtcfTool {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayBestTrackHandler.class);

    // Commands issued from Track menu
    private static final String DISPLAY_BEST_TRACK_INTENSITIES = "Display Best Track Intensities";

    private static final String DISPLAY_BEST_TRACK_LABELS = "Display Best Track Labels";

    // Commands issued from Sidebar
    private static final String DISPLAY_BEST_TRACK = "best tracks";

    private static final String BEST_TRACK_34KT_RADII = "b-track 34-kt radii";

    private static final String BEST_TRACK_50KT_RADII = "b-track 50-kt radii";

    private static final String BEST_TRACK_64KT_RADII = "b-track 64-kt radii";

    private static final String BEST_TRACK_RMW = "b-track RMW";

    private static final String BEST_TRACK_ROCI = "b-track ROCI";

    private static final String BEST_TRACK_INTENSITIES = "b-track intensities";

    private static final String BEST_TRACK_LABELS = "b-track labels";

    // Current storm
    protected Storm curStorm = null;

    /**
     * Handle best track displaying commands from sidebar or Track menu.
     * 
     * @param event
     *            ExecutionEvent
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        // Sanity check - the storm should have been selected at this point.
        curStorm = AtcfSession.getInstance().getActiveStorm();
        if (curStorm == null) {
            logger.error(
                    "DisplayBestTrackHandler: No active storm is selected.");
            return;
        }

        handleCommands(curStorm);

    }

    /**
     * Handle commands issued from sidebar or menu to erase or redraw best track
     * with options.
     *
     * <pre>
     *
     *     Sidebar commands:
     *
     *    "best tracks"         - If best track is in partial display, create/display
     *                            whole track. Otherwise, just toggle on/off the
     *                            B-Deck layer.
     *
     *    "b-track 34-kt radii" - draw 34-kt wind radii for DTGs selected via "Best Track
     *                            Options" dialog.
     *    "b-track 50-kt radii" - simlmilar to "b-track 34-kt radii".
     *    "b-track 64-kt radii" - simlmilar to "b-track 34-kt radii".
     *
     *    "b-track RMW"         - draw radius of max wind
     *    "b-track ROCI"        - draw radius of outmost closed isobar.
     *    "b-track intensities" - draw best track intensities.
     *    "b-track labels"      - draw best track labels in format of "DD/HHZ".
     *
     *    Track menu commands:
     *
     *    "Display Best Track Intensities" - same as "b-track intensities".
     *    "Display Best Track Labels"      - same as "b-track labels".
     *
     * </pre>
     *
     * @param storm
     *            storm to work on
     * 
     */
    private void handleCommands(Storm storm) {

        AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
        AtcfProduct prd = rsc.getResourceData().getAtcfProduct(storm);

        BestTrackProperties prop = prd.getBestTrackProperties();
        boolean displayTrack = prd.getBDeckLayer().isOnOff();

        String cmdName = commandName;
        if (sidebarCmdName != null) {
            cmdName = sidebarCmdName;
        }

        // Toggle the options and redraw.
        if (prop != null && cmdName != null) {

            boolean recreate = true;

            if (cmdName.equals(DISPLAY_BEST_TRACK)) {
                // Toggle display flag.
                if (!stormSelected) {
                    displayTrack = !displayTrack;
                }

                // Reset these four properties.
                prop.setSelectedDTGs(null);
                prop.setSelectedRadiiDTGs(null);
                prop.setTrackIntensities(false);
                prop.setTrackLabels(false);
            } else {
                // Toggle other properties.
                recreate = flipDisplayFlags(cmdName, prop);
            }

            // Redraw.
            if (recreate) {
                BestTrackGenerator btkGen = new BestTrackGenerator(rsc, prop,
                        storm);
                btkGen.create(displayTrack);
            }
        }
    }

    /*
     * Flips the flags in a BestTrackProperties based on command name.
     *
     * @param cmdName
     *
     * @param attr BestTrackProperties
     * 
     * @return boolean Flag to recreate elements.
     */
    private boolean flipDisplayFlags(String cmdName, BestTrackProperties prop) {
        boolean recreate = true;

        // Toggle other properties.
        if (cmdName.equals(BEST_TRACK_34KT_RADII)) {
            prop.setRadiiFor34Knot(!prop.isRadiiFor34Knot());
        } else if (cmdName.equals(BEST_TRACK_50KT_RADII)) {
            prop.setRadiiFor50Knot(!prop.isRadiiFor50Knot());
        } else if (cmdName.equals(BEST_TRACK_64KT_RADII)) {
            prop.setRadiiFor64Knot(!prop.isRadiiFor64Knot());
        } else if (cmdName.equals(BEST_TRACK_RMW)) {
            prop.setDrawRMW(!prop.isDrawRMW());
        } else if (cmdName.equals(BEST_TRACK_ROCI)) {
            prop.setDrawROCI(!prop.isDrawROCI());
        } else if (cmdName.equals(BEST_TRACK_INTENSITIES)
                || cmdName.equals(DISPLAY_BEST_TRACK_INTENSITIES)) {
            prop.setTrackIntensities(!prop.isTrackIntensities());
        } else if (cmdName.equals(BEST_TRACK_LABELS)
                || cmdName.equals(DISPLAY_BEST_TRACK_LABELS)) {
            prop.setTrackLabels(!prop.isTrackLabels());
        } else {
            recreate = false;
        }

        return recreate;
    }

}
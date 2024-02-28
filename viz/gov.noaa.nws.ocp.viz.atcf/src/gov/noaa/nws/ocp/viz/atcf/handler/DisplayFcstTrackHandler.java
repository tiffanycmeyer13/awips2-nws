/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackProperties;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;

/**
 * Handler for ATCF forecast track displaying commands from sidebar or Graphic
 * menu.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Oct 29, 2019 69592       jwu         Initial creation
 * </pre>
 *
 * @author jwu
 * @version 0.0.1
 */
public class DisplayFcstTrackHandler extends AbstractAtcfTool {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayFcstTrackHandler.class);

    // Commands issued from Graphic menu
    private static final String DISPLAY_FCST_TRACK = "Display Forecast Track";

    private static final String DISPLAY_WIND_RADII = "Display Forecast Track and Wind Radii";

    private static final String DISPLAY_12_FT_SEAS_RADII = "Display 12 ft Seas Radii";

    private static final String DISPLAY_ERROR_SWATH = "Display Error Swath";

    private static final String DISPLAY_CUMUL_WIND_PROBS = "Display Cumulative Wind Probs";

    // Commands issued from Sidebar
    private static final String FORECAST_TRACK = "forecast track";

    private static final String FCST_TRACK_WIND_RADII = "forecast wind radii";

    private static final String FCST_TRACK_SEAS_RADII = "forecast seas radii";

    private static final String FCST_TRACK_CUMUL_WIND_PROBS = "cumul wind probs";

    private static final String FCST_TRACK_LABELS = "forecast labels";

    // Current storm
    protected Storm curStorm = null;

    /**
     * Handle forecast track displaying commands from sidebar or Graphic menu.
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
                    "DisplayFcstTrackHandler: No active storm is selected.");
            return;
        }

        handleCommands(curStorm);

    }

    /**
     * Handle commands issued from sidebar or menu to erase or redraw forecast
     * track with options.
     *
     * <pre>
     *
     *     Sidebar commands:
     *
     *    "forecast track"                             - Toggle on/off the forecast track layer.
     *    "forecast wind radii"                        - Toggle on/off 34/50/64-kt wind radii.
     *    "foreast seas radii"                         - Toggle on/off 12 ft seas radii
     *    "cumul wind probs"                           - Toggle on/off cumul wind probabilities
     *    "forecast labels"                            - Toggle on/off forecast track labels in format of "DD/HHZ".
     *
     *    Graphic menu commands:
     *
     *    "Display Forecast Track"                     - Draw forecast track if not drawn yet.
     *    "Display Forecast Track and Wind Radii"      - Draw forecast track and wind radii.
     *    "Display 12 ft Seas Radii"                   - Draw 12 ft seas radii
     *    "Display Error Swath"                        - Draw error swath
     *    "Display Cumulative Wind Probs"              - Draw cumul wind probabilities
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

        // Retrieve all the F-deck records for the current storm
        Map<String, List<ForecastTrackRecord>> fcstTrackData = AtcfDataUtil
                .getFcstTrackRecords(curStorm, false);
        prd.setFcstTrackDataMap(fcstTrackData);

        FcstTrackProperties prop = prd.getFcstTrackProperties();
        if (prop == null) {
            prop = new FcstTrackProperties();
            prd.getForecastTrackLayer().setOnOff(false);
        }

        boolean onOff = prd.getForecastTrackLayer().isOnOff();

        String cmdName = commandName;
        if (sidebarCmdName != null) {
            cmdName = sidebarCmdName;
        }

        // Toggle the options and redraw.
        if (cmdName != null) {
            prop.setSpecialTypePosition(true);
            boolean recreate = true;

            if (cmdName.equals(DISPLAY_FCST_TRACK)) {
                onOff = true;
                prop.setSelectedRadiiDTGs(null);
            } else if (cmdName.equals(FORECAST_TRACK)) {
                onOff = !onOff;
                prop.setSelectedRadiiDTGs(null);
            } else {
                recreate = flipDisplayFlags(cmdName, prop);
            }

            // Redraw.
            if (recreate) {
                FcstTrackGenerator fcstTrackGen = new FcstTrackGenerator(rsc,
                        prop, storm);
                fcstTrackGen.create(onOff);
            }
        }
    }

    /*
     * Flips the flags in a FcstTrackProperties based on command name.
     *
     * @param cmdName
     *
     * @param attr FcstTrackProperties
     *
     * @return boolean Flag to recreate elements.
     */
    private boolean flipDisplayFlags(String cmdName, FcstTrackProperties prop) {
        boolean recreate = true;

        if (cmdName.equals(DISPLAY_WIND_RADII)) {
            prop.setRadiiFor34Knot(true);
            prop.setRadiiFor50Knot(true);
            prop.setRadiiFor64Knot(true);
        } else if (cmdName.equals(DISPLAY_12_FT_SEAS_RADII)) {
            prop.setRadiiFor12FtSea(true);
        } else if (cmdName.equals(FCST_TRACK_WIND_RADII)) {
            prop.setRadiiFor34Knot(!prop.isRadiiFor34Knot());
            prop.setRadiiFor50Knot(!prop.isRadiiFor50Knot());
            prop.setRadiiFor64Knot(!prop.isRadiiFor64Knot());
        } else if (cmdName.equals(FCST_TRACK_LABELS)) {
            prop.setTrackLabels(!prop.isTrackLabels());
        } else if (cmdName.equals(FCST_TRACK_SEAS_RADII)) {
            prop.setRadiiFor12FtSea(!prop.isRadiiFor12FtSea());
        } else if (cmdName.equals(DISPLAY_CUMUL_WIND_PROBS)
                || cmdName.equals(FCST_TRACK_CUMUL_WIND_PROBS)) {
            // TODO: To be implemented in a future release.
        } else if (cmdName.equals(DISPLAY_ERROR_SWATH)) {
            // TODO: To be implemented in a future release.
        } else {
            recreate = false;
        }

        return recreate;
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.localization.climate.stationorder.ClimateStationOrderManager;

/**
 * 
 * GUI Utility class for Climate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 06 DEC 2016  26345      astrakovsky Initial creation
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 14 NOV 2018  DR20977    wpaintsil   Add NumberFormatException handling.
 * 21 OCT 2019  DR21671    wpaintsil   Add helper to fetch the station list.
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 */
public final class ClimateGUIUtils {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateGUIUtils.class);

    /**
     * Set the cursor to a work in progress icon.
     * 
     * @param shell
     */
    public static void setCursorWait(Shell shell) {
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
    }

    /**
     * Set the cursor to the default icon.
     * 
     * @param shell
     */
    public static void resetCursor(Shell shell) {
        shell.setCursor(null);
    }

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateGUIUtils() {
    }

    /**
     * Get the SITE id for the current site.
     * 
     * @return ID of current site.
     */
    public static String getCurrentSite() {
        String wfo = LocalizationManager.getInstance().getCurrentSite();
        if (wfo == null || wfo.equalsIgnoreCase("none") || wfo.isEmpty()) {
            logger.warn("Localization site is either 'none', empty, or null.");
            wfo = "ALY";
        }
        return wfo;
    }

    /**
     * Retrieve the ordered list of stations.
     * 
     * @return station list
     */
    @SuppressWarnings("unchecked")
    public static List<Station> getOrderedStationList() {
        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);
        List<Station> orderedStations = new ArrayList<>();
        try {
            orderedStations = (List<Station>) ThriftClient.sendRequest(request);

            List<String> orderedStationStrings = ClimateStationOrderManager
                    .getInstance().getStationOrder();
            Map<String, Integer> orderedStationMap = new HashMap<>();

            int size = orderedStationStrings.size();
            for (int i = 0; i < size; i++) {
                orderedStationMap.put(orderedStationStrings.get(i), i);
            }

            Collections.sort(orderedStations,
                    Comparator.comparing(station -> orderedStationMap
                            .containsKey(station.getIcaoId())
                                    ? orderedStationMap.get(station.getIcaoId())
                                    : size));

        } catch (VizException e) {
            logger.error("Could not retrieve stations for this dialog.", e);
        }

        return orderedStations;
    }

    /**
     * Parse Integer from String with exception handling.
     * 
     * @param text
     * @return the value of the parsed integer
     */
    public static int parseInt(String text) {
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            value = ParameterFormatClimate.MISSING;
        }
        return value;
    }

    /**
     * Parse Float from String with exception handling.
     * 
     * @param text
     * @return the value of the parsed float
     */
    public static float parseFloat(String text) {
        float value;
        try {
            value = Float.parseFloat(text);
        } catch (NumberFormatException e) {
            value = ParameterFormatClimate.MISSING;
        }
        return value;
    }

    /**
     * Parse Double from String with exception handling.
     * 
     * @param text
     * @return the value of the parsed double
     */
    public static double parseDouble(String text) {
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            value = ParameterFormatClimate.MISSING;
        }
        return value;
    }

}

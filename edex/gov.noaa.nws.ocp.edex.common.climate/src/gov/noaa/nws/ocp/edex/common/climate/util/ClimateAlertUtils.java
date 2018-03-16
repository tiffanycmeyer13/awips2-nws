/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.util;

/**
 * Utility class for Climate Alerts.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 06 NOV 2017  36706      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 *
 */
public class ClimateAlertUtils {

    /**
     * EDEX source key.
     */
    public final static String SOURCE_EDEX = "EDEX";

    /**
     * Climate category.
     */
    public final static String CATEGORY_CLIMATE = "CLIMATE";

    /**
     * Climate notify endpoint, for passing information to Climate perspective.
     */
    public static final String CPG_ENDPOINT = "climateNotify";

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateAlertUtils() {
    }
}

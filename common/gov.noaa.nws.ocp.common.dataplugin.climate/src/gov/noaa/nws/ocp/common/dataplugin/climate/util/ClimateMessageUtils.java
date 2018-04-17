/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.util;

/**
 * Utility class for Climate Message-related constants used by both EDEX and
 * CAVE.
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
public class ClimateMessageUtils {

    /**
     * F6 Plugin ID for alerts.
     */
    public static final String F6_PLUGIN_ID = "F6Builder";
    /**
     * RER Plugin ID for alerts.
     */
    public static final String RER_PLUGIN_ID = "RecordClimate";
    /**
     * CPG Plugin ID for alerts.
     */
    public final static String CPG_PLUGIN_ID = "ClimateProdGenerateSession";

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateMessageUtils() {
    }
}

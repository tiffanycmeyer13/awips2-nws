/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.common;

/**
 * This class holds common Climate Display module values.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 04 AUG 2016  20414      amoore      Initial creation.
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * </pre>
 * 
 * @author amoore
 */
public final class DisplayValues {

    /**
     * Plug-in ID.
     */
    public static final String PLUGIN_ID = "gov.noaa.nws.ocp.viz.climate.display";

    /**
     * Knots abbreviation string.
     */
    public static final String KNOTS_ABB = "(KT)";
    /**
     * Miles per hour abbreviation string.
     */
    public static final String MPH_ABB = "(MPH)";

    /**
     * Private constructor. This is a utility class.
     */
    private DisplayValues() {
    }
}

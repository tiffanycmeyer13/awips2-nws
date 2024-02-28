/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim;

import gov.noaa.nws.ocp.common.dataplugin.odim.internal.ODIMProductProperties;

/**
 * Maps ODIM data types to existing description of NEXRAD radar products. Also
 * maps to NEXRAD product codes to allow the ODIM plugin to use the radar
 * plugin's capabilities.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMProductUtil {

    private ODIMProductUtil() {
        // static methods only
    }

    /**
     * Get the NEXRAD mnemonic equivalent to an ODIM dataset type.
     *
     * @param quantity
     *            the ODIM dataset type
     * @return
     */
    public static String getQuantityMnemonic(String quantity) {
        ODIMProductProperties properties = getProperties(quantity);
        return properties != null ? properties.getMnemonic() : null;
    }

    /**
     * Get the NEXRAD product description equivalent to an ODIM dataset type.
     *
     * @param quantity
     *            the ODIM dataset type
     * @return
     */
    public static String getQuantityDescription(String quantity) {
        ODIMProductProperties properties = getProperties(quantity);
        return properties != null ? properties.getDescription() : null;
    }

    /**
     * Get the NEXRAD product code equivalent to an ODIM dataset type.
     *
     * @param quantity
     *            the ODIM dataset type
     * @return
     */
    public static Integer getNexradProductCode(String quantity) {
        ODIMProductProperties properties = getProperties(quantity);
        return properties != null ? properties.getNexradProductCode() : null;
    }

    private static ODIMProductProperties getProperties(String quantity) {
        ODIMProductProperties properties;
        try {
            properties = ODIMProductProperties.valueOf(quantity);
        } catch (IllegalArgumentException e) {
            properties = null;
        }
        return properties;
    }

}

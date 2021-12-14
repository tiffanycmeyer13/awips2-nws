/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

/**
 * enum representing what configuration info the Change Satellite Types dialog
 * is displaying
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * May 31, 2019 63379       dmanzella   Created
 *
 * </pre>
 * 
 * @author dmanzella
 * @version 1
 */
public enum SatelliteTypes {
    MICROWAVE("Microwave"), SCAT("Scatterometer");

    public final String description;

    SatelliteTypes(final String text) {
        this.description = text;
    }
}

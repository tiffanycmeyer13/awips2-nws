/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 * Some common properties of A-deck and B-deck records
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 17, 2021 93824      dfriedman   Initial version
 *
 * </pre>
 *
 * @version 1.0
 */
public interface ITrackDeckRecord {
    String getStormName();

    String getSubRegion();
}

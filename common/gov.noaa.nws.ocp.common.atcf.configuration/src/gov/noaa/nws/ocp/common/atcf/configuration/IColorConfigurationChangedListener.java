/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.atcf.configuration;

/**
 * Listener that is notified when the ATCF color configuration is changed.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 22, 2021 87890       dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 */
public interface IColorConfigurationChangedListener {
    void colorsChanged();
}

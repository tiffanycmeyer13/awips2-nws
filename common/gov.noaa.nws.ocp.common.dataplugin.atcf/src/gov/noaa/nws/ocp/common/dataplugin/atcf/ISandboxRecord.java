/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

/**
 * Common interface for sandbox entity records.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 29, 2019 #61590     dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
public interface ISandboxRecord {
    public Sandbox getSandbox();

    public void setSandbox(Sandbox sandbox);

    public int getChangeCD();

    public void setChangeCD(int changeCD);
}

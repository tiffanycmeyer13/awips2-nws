/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;

/**
 * Interface to manipulate PSH data between PSH UI components
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2018 #46636      wpaintsil   Initial creation.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 * 
 */
public interface IPshData {

    /**
     * 
     * @return pshData
     */
    public PshData getPshData();

    /**
     * Set pshData
     * 
     * @param pshData
     */
    public void setPshData(PshData data);
}

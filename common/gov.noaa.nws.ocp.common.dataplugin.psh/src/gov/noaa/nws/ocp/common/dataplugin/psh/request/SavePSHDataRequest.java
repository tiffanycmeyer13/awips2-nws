/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;

/**
 * SavePSHDataRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 04, 2017            pwang       Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class SavePSHDataRequest implements IServerRequest {

    @DynamicSerializeElement
    private PshData pshData;

    /**
     * Empty constructor
     */
    public SavePSHDataRequest() {

    }

    /**
     * Constructor
     * 
     * @param basin
     * @param year
     * @param stormName
     * @param data
     * @param forecaster
     */
    public SavePSHDataRequest(PshData data) {
        this.pshData = data;
    }

    /**
     * @return the pshData
     */
    public PshData getPshData() {
        return this.pshData;
    }

    /**
     * @param pshData
     *            the pshData to set
     */
    public void setPshData(PshData pshData) {
        this.pshData = pshData;
    }

}

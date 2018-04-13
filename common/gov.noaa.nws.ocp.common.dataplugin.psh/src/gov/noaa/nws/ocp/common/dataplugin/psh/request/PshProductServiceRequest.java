/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;

/**
 * Service request for PSH product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 07, 2017 #35738     jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class PshProductServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private PshData pshData;

    /**
     * Constructor
     */
    public PshProductServiceRequest() {
    }

    /**
     * Constructor
     * 
     * @param pshData
     */
    public PshProductServiceRequest(PshData pshData) {
        this.pshData = pshData;
    }

    /**
     * @return the pshData
     */
    public PshData getPshData() {
        return pshData;
    }

    /**
     * @param pshData
     *            the pshData to set
     */
    public void setPshData(PshData pshData) {
        this.pshData = pshData;
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.psh.PshData;
import gov.noaa.nws.ocp.common.dataplugin.psh.PshDataCategory;

/**
 * Service request for PSH product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 26, 2017 #38085     wpaintsil   Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class PshPreviewServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private PshData pshData;

    @DynamicSerializeElement
    private PshDataCategory type;

    /**
     * Constructor
     */
    public PshPreviewServiceRequest() {
    }

    /**
     * Constructor
     * 
     * @param pshData
     * @param type
     */
    public PshPreviewServiceRequest(PshData pshData, PshDataCategory type) {
        this.pshData = pshData;
        this.type = type;
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

    /**
     * @return the type
     */
    public PshDataCategory getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(PshDataCategory type) {
        this.type = type;
    }

}
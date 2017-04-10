/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;

/**
 * SendNWRClimateProdustsRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2, 2017  20637      pwang       Initial creation
 * Apr 10, 2017 20637      pwang       Added operational flag
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class SendNWRClimateProductsRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID;

    @DynamicSerializeElement
    private boolean operational = false;

    @DynamicSerializeElement
    private Map<String, ClimateProduct> sendingProduct;
    
    @DynamicSerializeElement
    private String userId;

    /**
     * Constructor
     */
    public SendNWRClimateProductsRequest() {

    }

    /**
     * Constructor
     * 
     * @param cpgSessionId
     */
    public SendNWRClimateProductsRequest(String cpgSessionId,
            Map<String, ClimateProduct> products) {
        this.cpgSessionID = cpgSessionId;
        this.sendingProduct = products;
    }

    /**
     * @return the cpgSessionID
     */
    public String getCpgSessionID() {
        return cpgSessionID;
    }

    /**
     * @param cpgSessionID
     *            the cpgSessionID to set
     */
    public void setCpgSessionID(String cpgSessionID) {
        this.cpgSessionID = cpgSessionID;
    }

    /**
     * @return the sendingProduct
     */
    public Map<String, ClimateProduct> getSendingProduct() {
        return sendingProduct;
    }

    /**
     * @param sendingProduct
     *            the sendingProduct to set
     */
    public void setSendingProduct(Map<String, ClimateProduct> sendingProduct) {
        this.sendingProduct = sendingProduct;
    }

    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * SendNWWSClimateProdustsRequest
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
public class SendNWWSClimateProductsRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID;

    @DynamicSerializeElement
    private boolean operational = false;
    
    @DynamicSerializeElement
    private String userId;

    /**
     * Constructor
     */
    public SendNWWSClimateProductsRequest() {

    }

    /**
     * Constructor
     * 
     * @param cpgSessionId
     */
    public SendNWWSClimateProductsRequest(String cpgSessionId, String userId) {
        this.cpgSessionID = cpgSessionId;
        this.userId = userId;
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
     * @return the operational
     */
    public boolean isOperational() {
        return operational;
    }

    /**
     * @param operational
     *            the operational to set
     */
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

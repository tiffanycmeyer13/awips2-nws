/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * ForwardProdToNWRForReviewRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2017 33532      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ForwardProdToNWRForReviewRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID;

    @DynamicSerializeElement
    private boolean operational = false;

    @DynamicSerializeElement
    private String userId;

    /**
     * Empty constructor.
     */
    public ForwardProdToNWRForReviewRequest() {
    }

    /**
     * Constructor.
     * 
     * @param cpgSessionId
     */
    public ForwardProdToNWRForReviewRequest(String cpgSessionId,
            String userId) {
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
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
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

}

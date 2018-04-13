/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * This request should be issued when the Display is activated for the session.
 * 
 * No difference between auto and manual sessions
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016            pwang       Initial creation
 * APR 07, 2017 30166      amoore      Correct javadoc.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DisplayClimateRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID;

    @DynamicSerializeElement
    private String userId;

    /**
     * Empty constructor.
     */
    public DisplayClimateRequest() {
    }

    /**
     * Constructor.
     * 
     * @param cpgSessionId
     */
    public DisplayClimateRequest(String cpgSessionId, String userId) {
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

}

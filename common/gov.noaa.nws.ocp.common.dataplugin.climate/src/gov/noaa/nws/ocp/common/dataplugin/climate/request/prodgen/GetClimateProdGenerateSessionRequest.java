/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * GetClimateProdGenerateSessionRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 3, 2017  20637      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class GetClimateProdGenerateSessionRequest implements IServerRequest {

    @DynamicSerializeElement
    private String cpgSessionID = null;

    /**
     * Empty constructor.
     */
    public GetClimateProdGenerateSessionRequest() {
        // Null session ID will retrieve all sessions
        this.cpgSessionID = null;
    }

    public GetClimateProdGenerateSessionRequest(String sessionId) {
        // Retrieve particular one
        this.cpgSessionID = sessionId;
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

}

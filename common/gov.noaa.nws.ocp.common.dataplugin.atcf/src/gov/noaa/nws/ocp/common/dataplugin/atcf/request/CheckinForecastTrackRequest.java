/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;


/**
 * CheckinForecastTrackRequest
 *
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 16, 2019 #69593     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CheckinForecastTrackRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxid;

    @DynamicSerializeElement
    private String userId;


    /**
     * @return the sandboxid
     */
    public int getSandboxid() {
        return sandboxid;
    }

    /**
     * @param sandboxid
     *            the sandboxid to set
     */
    public void setSandboxid(int sandboxid) {
        this.sandboxid = sandboxid;
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


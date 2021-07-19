package gov.noaa.nws.ocp.common.dataplugin.psh.request;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request for managing ClusterTask lock for PSH application
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 19, 2021  DCS22178  mporricelli Initial creation
 * 
 * </pre>
 * 
 * @author porricel
 * @version 1.0
 */
@DynamicSerialize
public class PshLockRequest implements IServerRequest {
    /**
     * The types of requests that can be made for handling the PSH Lock
     *
     */
    @DynamicSerialize
    public enum ReqType {

        GETLOCK,

        RENEW_TIMEOUT,

        BREAKLOCK,

        FORCE_BREAKLOCK,

        CHECK_LOCKOWNER
    }

    /**
     * The type of PSH Lock request
     */
    @DynamicSerializeElement
    private ReqType reqType;

    /**
     * The current PSH application user
     */
    @DynamicSerializeElement
    private WsId currentUser;

    /**
     * Get the type of request for managing the PSH Lock
     *
     * @return the request type
     */
    public ReqType getReqType() {
        return reqType;
    }

    /**
     * Set the type of request for managing the PSH Lock
     *
     * @param reqType
     *            set the request type
     */
    public void setReqType(ReqType reqType) {
        this.reqType = reqType;
    }

    /**
     * @return current user
     */
    public WsId getCurrentUser() {
        return currentUser;
    }

    /**
     * @param currentUser
     *            set the current user
     */
    public void setCurrentUser(WsId currentUser) {
        this.currentUser = currentUser;
    }
}

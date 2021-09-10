/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.response;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Response from ThriftClient request for PSH ClusterTask locking.
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
public class PshLockServiceResponse {

    @DynamicSerializeElement
    private boolean pshLockObtained = false;

    @DynamicSerializeElement
    private WsId lockOwner;

    @DynamicSerializeElement
    private boolean pshLockStale = false;

    @DynamicSerializeElement
    private String message = "";

    /**
     * @return true if PSH lock creation successful, false otherwise
     */
    public boolean isPshLockObtained() {
        return pshLockObtained;
    }

    /**
     * Set status of attempt to create PSH Lock
     *
     * @param pshLockObtained
     */
    public void setPshLockObtained(boolean pshLockObtained) {
        this.pshLockObtained = pshLockObtained;
    }

    /**
     * @return lock owner information for existing PSH Lock
     */
    public WsId getLockOwner() {
        return lockOwner;
    }

    /**
     * Set lock owner information
     *
     * @param lockOwner
     */
    public void setLockOwner(WsId lockOwner) {
        this.lockOwner = lockOwner;
    }

    /**
     * Retrieve active/stale status of PSH Lock
     *
     * @return true if PSH Lock older than expected timeout, false otherwise
     */
    public boolean isPshLockStale() {
        return pshLockStale;
    }

    /**
     * Set active/stale status of PSH Lock
     *
     * @param pshLockStale
     */
    public void setPshLockStale(boolean pshLockStale) {
        this.pshLockStale = pshLockStale;
    }

    /**
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

}

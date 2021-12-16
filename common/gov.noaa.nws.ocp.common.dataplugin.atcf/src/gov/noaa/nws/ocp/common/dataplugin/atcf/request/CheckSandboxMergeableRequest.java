/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;


/**
 * CheckSandboxMergeableRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 28, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CheckSandboxMergeableRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxid;

    public CheckSandboxMergeableRequest() {

    }

    public CheckSandboxMergeableRequest(int sandboxId) {
        this.sandboxid = sandboxId;
    }

    /**
     * @return the sandboxid
     */
    public int getSandboxid() {
        return sandboxid;
    }

    /**
     * @param sandboxid the sandboxid to set
     */
    public void setSandboxid(int sandboxid) {
        this.sandboxid = sandboxid;
    }

}


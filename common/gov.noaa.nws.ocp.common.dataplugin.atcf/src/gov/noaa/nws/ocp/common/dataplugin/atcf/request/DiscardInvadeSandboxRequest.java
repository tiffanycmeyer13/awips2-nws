/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;


@DynamicSerialize
public class DiscardInvadeSandboxRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxid;

    public DiscardInvadeSandboxRequest() {

    }

    public DiscardInvadeSandboxRequest(int sandboxId) {
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


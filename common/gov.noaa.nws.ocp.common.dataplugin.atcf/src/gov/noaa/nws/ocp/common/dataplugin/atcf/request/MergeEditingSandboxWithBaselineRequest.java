/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictMergingRecordSet;


/**
 * MergeEditingSandboxWithBaselineRequest
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
public class MergeEditingSandboxWithBaselineRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxid;
    
    @DynamicSerializeElement
    private ConflictMergingRecordSet mergingRecordSet;


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

    /**
     * @return the mergingRecordSet
     */
    public ConflictMergingRecordSet getMergingRecordSet() {
        return mergingRecordSet;
    }

    /**
     * @param mergingRecordSet the mergingRecordSet to set
     */
    public void setMergingRecordSet(ConflictMergingRecordSet mergingRecordSet) {
        this.mergingRecordSet = mergingRecordSet;
    }
    
}



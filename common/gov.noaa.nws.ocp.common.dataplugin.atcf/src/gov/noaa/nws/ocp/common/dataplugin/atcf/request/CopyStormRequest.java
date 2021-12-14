/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * CopyStormRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2020 82622      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class CopyStormRequest implements IServerRequest {

    @DynamicSerializeElement
    private Storm oldStorm;

    @DynamicSerializeElement
    private Storm newStorm;

    public CopyStormRequest() {

    }

    public CopyStormRequest(Storm oldStorm, Storm newStorm) {
        this.oldStorm = oldStorm;
        this.newStorm = newStorm;
    }

    public Storm getOldStorm() {
        return oldStorm;
    }

    public void setOldStorm(Storm oldStorm) {
        this.oldStorm = oldStorm;
    }

    public Storm getNewStorm() {
        return newStorm;
    }

    public void setNewStorm(Storm newStorm) {
        this.newStorm = newStorm;
    }

}

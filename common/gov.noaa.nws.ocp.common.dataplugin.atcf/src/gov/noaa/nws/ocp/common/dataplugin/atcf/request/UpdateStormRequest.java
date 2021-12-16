/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * UpdateStormRequest
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
public class UpdateStormRequest implements IServerRequest {

    @DynamicSerializeElement
    private Storm oldStorm;

    @DynamicSerializeElement
    private Storm changedStorm;

    public UpdateStormRequest() {

    }

    public UpdateStormRequest(Storm oldStorm, Storm changedStorm) {
        this.oldStorm = oldStorm;
        this.changedStorm = changedStorm;
    }

    public Storm getOldStorm() {
        return oldStorm;
    }

    public void setOldStorm(Storm oldStorm) {
        this.oldStorm = oldStorm;
    }

    public Storm getChangedStorm() {
        return changedStorm;
    }

    public void setChangedStorm(Storm changedStorm) {
        this.changedStorm = changedStorm;
    }

}

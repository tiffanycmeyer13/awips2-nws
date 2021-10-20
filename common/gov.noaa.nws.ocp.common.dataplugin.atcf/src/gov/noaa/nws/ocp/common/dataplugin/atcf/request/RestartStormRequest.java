/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * RestartStormRequest
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
public class RestartStormRequest implements IServerRequest {

    @DynamicSerializeElement
    private Storm storm;

    public RestartStormRequest() {

    }

    public RestartStormRequest(Storm storm) {
        this.storm = storm;
    }

    public Storm getStorm() {
        return storm;
    }

    public void setStorm(Storm storm) {
        this.storm = storm;
    }

}

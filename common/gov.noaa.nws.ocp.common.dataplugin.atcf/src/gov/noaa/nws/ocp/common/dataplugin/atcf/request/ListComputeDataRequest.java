/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * ListComputeDataRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Jun 15, 2020 79543      mporricelli  Initial creation
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
@DynamicSerialize
public class ListComputeDataRequest implements IServerRequest {

    @DynamicSerializeElement
    private Storm storm;

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    public ListComputeDataRequest() {

    }

    /**
     * construct request
     *
     * @param storm
     */
    public ListComputeDataRequest(Storm storm) {
        this.storm = storm;
    }

}

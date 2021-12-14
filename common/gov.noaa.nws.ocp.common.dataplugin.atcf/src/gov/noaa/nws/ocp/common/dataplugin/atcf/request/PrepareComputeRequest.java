/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * PrepareComputeRequest for writing out .com file
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Nov 19, 2019 70253      mporricelli  Initial creation
 *
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
@DynamicSerialize
public class PrepareComputeRequest implements IServerRequest {
    @DynamicSerializeElement
    private Storm storm;

    @DynamicSerializeElement
    private List<ADeckRecord> currentADeckRecords;

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

    /**
     * @return the currentADeckRecords
     */
    public List<ADeckRecord> getCurrentADeckRecords() {
        return currentADeckRecords;
    }

    /**
     * @param currentADeckRecords
     *            the currentADeckRecords to set
     */
    public void setCurrentADeckRecords(List<ADeckRecord> currentADeckRecords) {
        this.currentADeckRecords = currentADeckRecords;
    }

    public PrepareComputeRequest() {

    }

    /**
     * construct request
     *
     * @param storm
     * @param currentADeckRecords
     */
    public PrepareComputeRequest(Storm storm,
            List<ADeckRecord> currentADeckRecords) {
        this.storm = storm;
        this.currentADeckRecords = currentADeckRecords;
    }

}

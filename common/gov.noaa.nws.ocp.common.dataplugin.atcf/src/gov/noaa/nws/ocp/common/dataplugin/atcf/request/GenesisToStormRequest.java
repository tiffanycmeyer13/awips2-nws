/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;

/**
 * GenesisToStormRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 8, 2020 # 77134     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class GenesisToStormRequest implements IServerRequest {

    @DynamicSerializeElement
    private Genesis candidate;

    @DynamicSerializeElement
    private int cycloneNum;

    @DynamicSerializeElement
    private String stormName;


    public Genesis getCandidate() {
        return candidate;
    }

    public void setCandidate(Genesis candidate) {
        this.candidate = candidate;
    }

    public int getCycloneNum() {
        return cycloneNum;
    }

    public void setCycloneNum(int cycloneNum) {
        this.cycloneNum = cycloneNum;
    }

    /**
     * @return the stormName
     */
    public String getStormName() {
        return stormName;
    }

    /**
     * @param stormName
     *            the stormName to set
     */
    public void setStormName(String stormName) {
        this.stormName = stormName;
    }

}

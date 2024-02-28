/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ModeContext;



/**
 * GetGenesisDeckRecordsRequest
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
public class GetGenesisDeckRecordsRequest implements IServerRequest {

    // deckType "B" / "E"
    @DynamicSerializeElement
    private String deckType;

    @DynamicSerializeElement
    private ModeContext mode;

    @DynamicSerializeElement
    private Map<String, Object> queryConditions = new HashMap<>();

    /**
     * Empty constructor
     */
    public GetGenesisDeckRecordsRequest() {

    }

    public GetGenesisDeckRecordsRequest(String deckType) {
        this.deckType = deckType;
    }

    public String getDeckType() {
        return deckType;
    }

    public void setDeckType(String deckType) {
        this.deckType = deckType;
    }

    /**
     * @return the mode
     */
    public ModeContext getMode() {
        return mode;
    }


    /**
     * @param mode the mode to set
     */
    public void setMode(ModeContext mode) {
        this.mode = mode;
    }

    /**
     * @return the queryConditions
     */
    public Map<String, Object> getQueryConditions() {
        return queryConditions;
    }

    /**
     * @param queryConditions
     *            the queryConditions to set
     */
    public void setQueryConditions(Map<String, Object> queryConditions) {
        this.queryConditions = queryConditions;
    }

    /**
     * @param column
     * @param value
     */
    public void addOneQueryCondition(String column, Object value) {
        this.queryConditions.put(column, value);
    }

}


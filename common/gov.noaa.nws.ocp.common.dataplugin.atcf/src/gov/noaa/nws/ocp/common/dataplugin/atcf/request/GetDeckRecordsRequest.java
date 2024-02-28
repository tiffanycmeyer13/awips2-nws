/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModeContext;

/**
 * GetDeckRecordsRequest
 * Usage:
 * When query ADeckRecord set from a Sandbox:
 * 1) setSandboxId with a real sandboxId;
 * 2) addOneQueryCondition to narrow down the query.
 *    Example: addOneQueryCondition("technique", "AC00")
 *
 * When query ADeckRecord set from atcf.a_deck table:
 * 1) Keep the sandboxId <= 0;
 * 2) To keep the A Deck data in one Storm, following three conditions are required:
 *    addOneQueryCondition("basin", <region value>);
 *    addOneQueryCondition("year", <the year>);
 *    addOneQueryCondition("cyclonenum", <cyclone number>);
 * 3) Other conditions can be added to narrow down
 *
 * Commonly used conditions for the request:
 *   . basin        String       2 upper case chars, such as "AL"
 *   . year         Integer      4 digits year, like 2017
 *   . cyclonenum   Integer      1-2 digits
 *   . technique    String       3-4 upper case chars, such as "CARQ" for a model
 *
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 14, 2018            pwang       Initial creation
 * Mar 29, 2019 #61590     dfriedman   Merge type-specific requests into one class.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class GetDeckRecordsRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private ModeContext mode;

    @DynamicSerializeElement
    private int sandboxId = 0;

    @DynamicSerializeElement
    private Map<String, Object> queryConditions = new HashMap<>();

    /**
     * Empty constructor
     */
    public GetDeckRecordsRequest() {

    }

    public GetDeckRecordsRequest(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    public AtcfDeckType getDeckType() {
        return deckType;
    }


    public void setDeckType(AtcfDeckType deckType) {
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
     * @return the sandboxId
     */
    public int getSandboxId() {
        return sandboxId;
    }

    /**
     * @param sandboxId
     *            the sandboxId to set
     */
    public void setSandboxId(int sandboxId) {
        this.sandboxId = sandboxId;
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

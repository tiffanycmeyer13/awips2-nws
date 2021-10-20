/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * GetGenesisRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 6, 2020 # 77134     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class GetGenesisRequest implements IServerRequest {

    @DynamicSerializeElement
    private Map<String, Object> queryConditions = new HashMap<>();

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


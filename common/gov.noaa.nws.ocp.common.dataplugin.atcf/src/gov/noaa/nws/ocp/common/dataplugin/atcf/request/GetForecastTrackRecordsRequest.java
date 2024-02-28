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
 * GetForecastTrackRecordsRequest
 * sandboxId < 0: retrieve ForecastTrackRecords from baseline fst
 * sandboxId > 0: retrieve ForecastTrackRecords from valid sandbox_fst
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 11, 2019 #69593     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class GetForecastTrackRecordsRequest implements IServerRequest {


    @DynamicSerializeElement
    private int sandboxId = -1;

    @DynamicSerializeElement
    private Map<String, Object> queryConditions = new HashMap<>();


    /**
     * @return the sandboxId
     */
    public int getSandboxId() {
        return sandboxId;
    }

    /**
     * @param sandboxId the sandboxId to set
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
     * @param queryConditions the queryConditions to set
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

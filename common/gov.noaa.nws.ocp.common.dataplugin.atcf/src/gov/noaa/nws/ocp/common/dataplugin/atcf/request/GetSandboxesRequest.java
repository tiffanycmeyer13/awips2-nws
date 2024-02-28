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
 * GetSandboxesRequest
 * 
 * Usage:
 * To get all my sandbox, add userId to the queryConditions:
 * . userid        String        AWIPS userId
 * 
 * Get a special sandox by adding the sandbox id as:
 * addOneQueryCondition("id", <sandbox ID value> 
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 14, 2018            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class GetSandboxesRequest implements IServerRequest {

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

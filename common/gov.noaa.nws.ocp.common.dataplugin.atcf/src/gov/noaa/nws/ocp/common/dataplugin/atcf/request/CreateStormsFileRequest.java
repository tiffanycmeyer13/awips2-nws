/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * Request for creating CSV-formatted storms.txt file
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2020 79366      mporricelli Initial creation
 *
 * </pre>
 * @author porricel
 *
 */
@DynamicSerialize
public class CreateStormsFileRequest implements IServerRequest {

    @DynamicSerializeElement
    private Map<String, Object> queryConditions;

    @DynamicSerializeElement
    private String stormsFileFullPath;

    @DynamicSerializeElement
    private List<Storm> stormsRecs;


    public Map<String, Object> getQueryConditions() {
        return queryConditions;
    }

    public void setQueryConditions(Map<String, Object> queryConditions) {
        this.queryConditions = queryConditions;
    }

    public String getStormsFileFullPath() {
        return stormsFileFullPath;
    }

    public void setStormsFileFullPath(String stormsFileFullPath) {
        this.stormsFileFullPath = stormsFileFullPath;
    }

    public List<Storm> getStormsRecs() {
        return stormsRecs;
    }
    public void setStormsRecs(List<Storm> stormsRecs) {
        this.stormsRecs = stormsRecs;
    }
}

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

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;

/**
 * Request file for creating CSV-formatted deck files
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
public class CreateDeckFileRequest implements IServerRequest {

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private String stormid;

    @DynamicSerializeElement
    private Map<String, Object> queryConditions;

    @DynamicSerializeElement
    private String deckFileFullPath;

    @DynamicSerializeElement
    private List<? extends AbstractDeckRecord> deckRecs;


    public AtcfDeckType getDeckType() {
        return deckType;
    }

    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    public String getStormid() {
        return stormid;
    }

    public void setStormid(String stormid) {
        this.stormid = stormid;
    }

    public Map<String, Object> getQueryConditions() {
        return queryConditions;
    }

    public void setQueryConditions(Map<String, Object> queryConditions) {
        this.queryConditions = queryConditions;
    }

    public String getDeckFileFullPath() {
        return deckFileFullPath;
    }

    public void setDeckFileFullPath(String deckFileFullPath) {
        this.deckFileFullPath = deckFileFullPath;
    }

    public List<? extends AbstractDeckRecord> getDeckRecs() {
        return deckRecs;
    }
    public void setDeckRecs(List<? extends AbstractDeckRecord> deckRecs) {
        this.deckRecs = deckRecs;
    }
}

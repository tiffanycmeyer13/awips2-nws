/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;


/**
 * GetDeckRecordChangeCDRequest
 *
 * This request will retrieve a Map<recordId, changeCd> based on
 * given conditions.
 *
 * 1) to retrieve all records' changeCd of given sandbox, just don't
 *    set recIdList and modifiedOnly;
 * 2) to retrieve a subset of records in a given sandbox, need to add
 *    recId for each record;
 * 3) to retrieve only modified records' changeCd, set modifiedOnly to true.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 21, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class GetDeckRecordChangeCDRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxid;

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private List<Integer> recIdList;

    @DynamicSerializeElement
    private boolean modifiedOnly;

    /**
     * Empty constructor
     */
    public GetDeckRecordChangeCDRequest() {
        this.recIdList = new ArrayList<>();
        this.modifiedOnly = false;
    }

    public int getSandboxid() {
        return sandboxid;
    }

    public void setSandboxid(int sandboxid) {
        this.sandboxid = sandboxid;
    }

    public AtcfDeckType getDeckType() {
        return deckType;
    }

    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
    }

    public List<Integer> getRecIdList() {
        return recIdList;
    }

    public void setRecIdList(List<Integer> recIdList) {
        this.recIdList = recIdList;
    }

    public void addRecordId(int recId) {
        this.recIdList.add(recId);
    }

    public boolean isModifiedOnly() {
        return modifiedOnly;
    }

    public void setModifiedOnly(boolean modifiedOnly) {
        this.modifiedOnly = modifiedOnly;
    }

}


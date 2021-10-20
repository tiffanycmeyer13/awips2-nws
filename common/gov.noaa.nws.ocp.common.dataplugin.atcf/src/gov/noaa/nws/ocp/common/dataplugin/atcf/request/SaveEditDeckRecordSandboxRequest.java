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
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;


/**
 * SaveEditDeckRecordSandboxRequest
 *
 * The service is designed for saving a batch edited records, including:
 * 1) Add one or more new records
 * 2) Modify any existing record(s), including newly added
 * 3) Mark one or more existing records as deleted
 * 4) Undo modification for new, modified or deleted records
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 16, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class SaveEditDeckRecordSandboxRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxId;

    @DynamicSerializeElement
    private AtcfDeckType deckType;

    @DynamicSerializeElement
    private List<ModifiedDeckRecord> modifiedRecords;


    public SaveEditDeckRecordSandboxRequest() {
        this.modifiedRecords = new ArrayList<>();
    }


    public int getSandboxId() {
        return sandboxId;
    }


    public AtcfDeckType getDeckType() {
        return deckType;
    }


    public void setDeckType(AtcfDeckType deckType) {
        this.deckType = deckType;
    }


    public void setSandboxId(int sandboxId) {
        this.sandboxId = sandboxId;
    }


    public List<ModifiedDeckRecord> getModifiedRecords() {
        return modifiedRecords;
    }


    public void setModifiedRecords(List<ModifiedDeckRecord> modifiedRecords) {
        this.modifiedRecords = modifiedRecords;
    }

    public void addModifiedRecord(ModifiedDeckRecord mrecord) {
        this.modifiedRecords.add(mrecord);
    }

    public int size() {
        return this.modifiedRecords.size();
    }


}

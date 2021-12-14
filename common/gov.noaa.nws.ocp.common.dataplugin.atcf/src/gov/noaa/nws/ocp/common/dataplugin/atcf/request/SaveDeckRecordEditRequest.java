/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;

/**
 * SaveDeckRecordEditRequest
 *
 * The service is designed for add a new deck record, save modified record in
 * sandbox. Delete an existing record will just mark the record as deleted. The
 * record will not be physically removed.
 *
 * <pre>
 *
 * The caller must specify a Edit Type:
 * 1) "NEW": add a new deck record.
 * 2) "MODIFY": update a record in the sandbox.
 *     A record in sandbox could be a new record, or a record exists in baseline.
 * 3) "DELETE": to mark an existing record in sandbox as deleted.
 * 4) "UNDO": remove new record, reset modified record back to baseline, remove the deletion mark
 * </pre>
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 25, 2018            pwang       Initial creation
 * Mar 29, 2019 #61590     dfriedman   Merge type-specific requests into one class.
 * Aug 05, 2019 66888      jwu         Add recordType.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class SaveDeckRecordEditRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxId;

    @DynamicSerializeElement
    private AbstractDeckRecord record;

    @DynamicSerializeElement
    private RecordEditType editType;

    @DynamicSerializeElement
    private RecordEditType recordType;


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

    public AbstractDeckRecord getRecord() {
        return record;
    }

    public void setRecord(AbstractDeckRecord record) {
        this.record = record;
    }

    /**
     * @return the editType
     */
    public RecordEditType getEditType() {
        return editType;
    }

    /**
     * @param editType
     *            the editType to set
     */
    public void setEditType(RecordEditType editType) {
        this.editType = editType;
    }

    public RecordEditType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordEditType recordType) {
        this.recordType = recordType;
    }

}
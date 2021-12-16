package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ModifiedDeckRecord
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class ModifiedDeckRecord {

    @DynamicSerializeElement
    protected RecordEditType editType;

    @DynamicSerializeElement
    protected AbstractAtcfRecord record;

    public ModifiedDeckRecord() {

    }

    public ModifiedDeckRecord(RecordEditType editType,
            AbstractAtcfRecord record) {
        this.editType = editType;
        this.setRecord(record);
    }

    public RecordEditType getEditType() {
        return editType;
    }

    public void setEditType(RecordEditType editType) {
        this.editType = editType;
    }

    public AbstractAtcfRecord getRecord() {
        return record;
    }

    public void setRecord(AbstractAtcfRecord record) {
        this.record = record;
    }

}

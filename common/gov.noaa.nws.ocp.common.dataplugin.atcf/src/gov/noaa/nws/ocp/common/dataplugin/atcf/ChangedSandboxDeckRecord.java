package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ChangedSandboxDeckRecord
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2019 #64739      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ChangedSandboxDeckRecord {

    @DynamicSerializeElement
    private Integer recordId;

    @DynamicSerializeElement
    private Integer changeCd;

    @DynamicSerializeElement
    private AbstractDeckRecord modifiedRecord;

    public ChangedSandboxDeckRecord() {

    }
    public ChangedSandboxDeckRecord(Integer recordId, Integer changeCd, AbstractDeckRecord modifiedRecord) {
        this.recordId = recordId;
        this.changeCd = changeCd;
        this.modifiedRecord = modifiedRecord;
    }
    /**
     * @return the recordId
     */
    public Integer getRecordId() {
        return recordId;
    }
    /**
     * @param recordId the recordId to set
     */
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
    /**
     * @return the changeCd
     */
    public Integer getChangeCd() {
        return changeCd;
    }
    /**
     * @param changeCd the changeCd to set
     */
    public void setChangeCd(Integer changeCd) {
        this.changeCd = changeCd;
    }
    /**
     * @return the modifiedRecord
     */
    public AbstractDeckRecord getModifiedRecord() {
        return modifiedRecord;
    }
    /**
     * @param modifiedRecord the modifiedRecord to set
     */
    public void setModifiedRecord(AbstractDeckRecord modifiedRecord) {
        this.modifiedRecord = modifiedRecord;
    }



}

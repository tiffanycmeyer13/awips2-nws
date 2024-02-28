package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.util.Pair;

import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;

/**
 * ConflictRecordPair A container class to hold a pair of DeckRecord BASELINE
 * record is L (left) Merging target record is R (Right) ChangeCode for each
 * record, see DataChangeCode If any field has different values, called
 * conflicted field Can be find in the conflictedFields map.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2019 #64739      pwang     Initial creation
 * Jun 11, 2019 #68118      wpaintsil Specify String types in conflict pair map.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class ConflictRecordPair {

    @DynamicSerializeElement
    private Integer conflictRecordId;

    @DynamicSerializeElement
    private DataChangeCode baselineRecordChangeCode;

    @DynamicSerializeElement
    private DataChangeCode mergingRecordChangeCode;

    @DynamicSerializeElement
    private AbstractDeckRecord baselineRecord;

    @DynamicSerializeElement
    private AbstractDeckRecord mergingRecord;

    /* identified fields which have different values */
    @DynamicSerializeElement
    private Map<String, Pair<String, String>> conflictedFields = new HashMap<>();

    public ConflictRecordPair() {

    }

    public ConflictRecordPair(Integer recordId,
            AbstractDeckRecord baselineRecord, AbstractDeckRecord mergingRecord,
            DataChangeCode baselineRecordChangeCode,
            DataChangeCode mergingRecordChangeCode) throws AtcfException {
        this.conflictRecordId = recordId;
        this.baselineRecord = baselineRecord;
        this.mergingRecord = mergingRecord;
        this.baselineRecordChangeCode = baselineRecordChangeCode;
        this.mergingRecordChangeCode = mergingRecordChangeCode;

        this.findConflictedFields();
    }

    /**
     * populate conflictedFields
     */
    public void findConflictedFields() throws AtcfException {
        if (null == this.baselineRecord || null == this.mergingRecord) {
            return;
        }
        try {
            conflictedFields = baselineRecord
                    .findFieldDifference(this.mergingRecord);
        } catch (IllegalAccessException e) {
            throw new AtcfException("Unable to determine conflicts", e);
        }
    }

    /**
     * @return the conflictRecordId
     */
    public Integer getConflictRecordId() {
        return conflictRecordId;
    }

    /**
     * @param conflictRecordId
     *            the conflictRecordId to set
     */
    public void setConflictRecordId(Integer conflictRecordId) {
        this.conflictRecordId = conflictRecordId;
    }

    /**
     * @return the baselineRecordChangeCode
     */
    public DataChangeCode getBaselineRecordChangeCode() {
        return baselineRecordChangeCode;
    }

    /**
     * @param baselineRecordChangeCode
     *            the baselineRecordChangeCode to set
     */
    public void setBaselineRecordChangeCode(
            DataChangeCode baselineRecordChangeCode) {
        this.baselineRecordChangeCode = baselineRecordChangeCode;
    }

    /**
     * @return the mergingRecordChangeCode
     */
    public DataChangeCode getMergingRecordChangeCode() {
        return mergingRecordChangeCode;
    }

    /**
     * @param mergingRecordChangeCode
     *            the mergingRecordChangeCode to set
     */
    public void setMergingRecordChangeCode(
            DataChangeCode mergingRecordChangeCode) {
        this.mergingRecordChangeCode = mergingRecordChangeCode;
    }

    /**
     * @return the baselineRecord
     */
    public AbstractDeckRecord getBaselineRecord() {
        return baselineRecord;
    }

    /**
     * @param baselineRecord
     *            the baselineRecord to set
     */
    public void setBaselineRecord(AbstractDeckRecord baselineRecord) {
        this.baselineRecord = baselineRecord;
    }

    /**
     * @return the mergingRecord
     */
    public AbstractDeckRecord getMergingRecord() {
        return mergingRecord;
    }

    /**
     * @param mergingRecord
     *            the mergingRecord to set
     */
    public void setMergingRecord(AbstractDeckRecord mergingRecord) {
        this.mergingRecord = mergingRecord;
    }

    /**
     * @return the conflictedFields
     */
    public Map<String, Pair<String, String>> getConflictedFields() {
        return conflictedFields;
    }

    /**
     * @param conflictedFields
     *            the conflictedFields to set
     */
    public void setConflictedFields(
            Map<String, Pair<String, String>> conflictedFields) {
        this.conflictedFields = conflictedFields;
    }

    public boolean hasConflictedFields() {
        return !conflictedFields.isEmpty();
    }

}

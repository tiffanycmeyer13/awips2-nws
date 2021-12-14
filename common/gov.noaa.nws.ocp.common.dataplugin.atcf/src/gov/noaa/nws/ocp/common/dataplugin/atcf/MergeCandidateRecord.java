package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * MergeCandidateRecord
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
public class MergeCandidateRecord {

    @DynamicSerializeElement
    private Integer conflictRecordId;

    @DynamicSerializeElement
    private MergeActionType actionTaken = MergeActionType.USEBASELINE;

    @DynamicSerializeElement
    private AbstractDeckRecord candidateRecord;


    /**
     * @return the conflictRecordId
     */
    public Integer getConflictRecordId() {
        return conflictRecordId;
    }

    /**
     * @param conflictRecordId the conflictRecordId to set
     */
    public void setConflictRecordId(Integer conflictRecordId) {
        this.conflictRecordId = conflictRecordId;
    }

    /**
     * @return the actionTaken
     */
    public MergeActionType getActionTaken() {
        return actionTaken;
    }

    /**
     * @param actionTaken the actionTaken to set
     */
    public void setActionTaken(MergeActionType actionTaken) {
        this.actionTaken = actionTaken;
    }

    /**
     * @return the candidateRecord
     */
    public AbstractDeckRecord getCandidateRecord() {
        return candidateRecord;
    }

    /**
     * @param candidateRecord the candidateRecord to set
     */
    public void setCandidateRecord(AbstractDeckRecord candidateRecord) {
        this.candidateRecord = candidateRecord;
    }

}

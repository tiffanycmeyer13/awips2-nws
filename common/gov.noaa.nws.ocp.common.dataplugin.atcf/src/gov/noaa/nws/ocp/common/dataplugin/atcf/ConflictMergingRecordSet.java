package gov.noaa.nws.ocp.common.dataplugin.atcf;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * ConflictMergingRecordSet
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
public class ConflictMergingRecordSet {

    @DynamicSerializeElement
    private Integer baselineId;

    @DynamicSerializeElement
    private String deck;

    @DynamicSerializeElement
    private Integer totalAdded;

    @DynamicSerializeElement
    private Integer totalUpdated;

    @DynamicSerializeElement
    private Integer totalDeleted;

    @DynamicSerializeElement
    private List<ConflictRecordPair> conflictedRecords = new ArrayList<>();

    @DynamicSerializeElement
    private List<MergeCandidateRecord> mergeCandidateRecords = new ArrayList<>();


    /**
     * @return the totalAdded
     */
    public Integer getTotalAdded() {
        return totalAdded;
    }

    /**
     * @param totalAdded
     *            the totalAdded to set
     */
    public void setTotalAdded(Integer totalAdded) {
        this.totalAdded = totalAdded;
    }

    /**
     * @return the totalUpdated
     */
    public Integer getTotalUpdated() {
        return totalUpdated;
    }

    /**
     * @param totalUpdated
     *            the totalUpdated to set
     */
    public void setTotalUpdated(Integer totalUpdated) {
        this.totalUpdated = totalUpdated;
    }

    /**
     * @return the totalDeleted
     */
    public Integer getTotalDeleted() {
        return totalDeleted;
    }

    /**
     * @param totalDeleted
     *            the totalDeleted to set
     */
    public void setTotalDeleted(Integer totalDeleted) {
        this.totalDeleted = totalDeleted;
    }

    /**
     * @return the conflictedRecords
     */
    public List<ConflictRecordPair> getConflictedRecords() {
        return conflictedRecords;
    }

    /**
     * @param conflictedRecords
     *            the conflictedRecords to set
     */
    public void setConflictedRecords(
            List<ConflictRecordPair> conflictedRecords) {
        this.conflictedRecords = conflictedRecords;
    }

    public void addConflictedRecord(ConflictRecordPair conflictRecord) {
        this.conflictedRecords.add(conflictRecord);
    }

    /**
     * @return the mergeCandidateRecords
     */
    public List<MergeCandidateRecord> getMergeCandidateRecords() {
        return mergeCandidateRecords;
    }

    /**
     * @param mergeCandidateRecords
     *            the mergeCandidateRecords to set
     */
    public void setMergeCandidateRecords(
            List<MergeCandidateRecord> mergeCandidateRecords) {
        this.mergeCandidateRecords = mergeCandidateRecords;
    }

    public void addMergeCandidateRecord(
            MergeCandidateRecord mergeCandidateRecord) {
        this.mergeCandidateRecords.add(mergeCandidateRecord);
    }

    public Integer totalConflict() {
        return this.conflictedRecords.size();
    }

    public Integer totalMergingRecords() {
        return totalAdded + totalUpdated + totalDeleted;
    }

    /**
     * @return the baselineId
     */
    public Integer getBaselineId() {
        return baselineId;
    }

    /**
     * @param baselineId the baselineId to set
     */
    public void setBaselineId(Integer baselineId) {
        this.baselineId = baselineId;
    }

    /**
     * @return the deck
     */
    public String getDeck() {
        return deck;
    }

    /**
     * @param deck the deck to set
     */
    public void setDeck(String deck) {
        this.deck = deck;
    }

    public void setTotal(DataChangeCode code, int count) {
        if(code == DataChangeCode.NEW) {
            this.totalAdded = count;
        }
        else if(code == DataChangeCode.UPDATED) {
            this.totalUpdated = count;
        }
        else if(code == DataChangeCode.DELETED) {
            this.totalDeleted = count;
        }
    }

}

/**
9 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ChangedSandboxDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictMergingRecordSet;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictRecordPair;
import gov.noaa.nws.ocp.common.dataplugin.atcf.DataChangeCode;
import gov.noaa.nws.ocp.common.dataplugin.atcf.MergeActionType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.MergeCandidateRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.MergeEditingSandboxWithBaselineRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * CheckSandboxMergeableHandler Return a container object of
 * ConflictMergingRecordSet to include 1) totalAdded, totalUpdated, and
 * totalDeleted 2) A list of ConflictRecordPair, if any
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 28, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class MergeEditingSandboxWithBaselineHandler
        implements IRequestHandler<MergeEditingSandboxWithBaselineRequest> {

    @Override
    public Integer handleRequest(MergeEditingSandboxWithBaselineRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            ConflictMergingRecordSet cmrs = request.getMergingRecordSet();

            Map<Integer, ConflictRecordPair> conflictedRecords = new HashMap<>();
            for (ConflictRecordPair cr : cmrs.getConflictedRecords()) {
                conflictedRecords.put(cr.getConflictRecordId(), cr);
            }
            Map<Integer, MergeCandidateRecord> mergeCandidateRecords = new HashMap<>();
            for (MergeCandidateRecord mr : cmrs.getMergeCandidateRecords()) {
                mergeCandidateRecords.put(mr.getConflictRecordId(), mr);
            }

            /* user need to determine how to merge conflict records */
            if (conflictedRecords.size() > 0 && conflictedRecords
                    .size() != mergeCandidateRecords.size()) {
                throw new Exception(
                        "A MergeCandidateRecord must be selected for each ConflictRecordPair. ");
            }

            String deckType = cmrs.getDeck().substring(0, 1).toUpperCase();

            // retrieve all merging records
            Map<Integer, ChangedSandboxDeckRecord> baseRecords = dao
                    .getModifiedDeckRecords(AtcfDeckType.valueOf(deckType),
                            cmrs.getBaselineId());

            // merging
            for (Map.Entry<Integer, ChangedSandboxDeckRecord> e : baseRecords
                    .entrySet()) {
                Integer recordId = e.getKey();
                AbstractDeckRecord baseRcord = e.getValue().getModifiedRecord();
                int changeCd = e.getValue().getChangeCd();
                if (changeCd == AbstractAtcfRecord.CHANG_CD_NEW) {
                    mergeNewRecord(dao, request.getSandboxid(), recordId,
                            baseRcord, conflictedRecords,
                            mergeCandidateRecords);
                } else if (changeCd == AbstractAtcfRecord.CHANG_CD_MODIFY) {
                    mergeUpdatedRecord(dao, request.getSandboxid(), recordId,
                            baseRcord, conflictedRecords,
                            mergeCandidateRecords);
                }
                else if (changeCd == AbstractAtcfRecord.CHANG_CD_DELETE) {
                    mergeDeletedRecord(dao, request.getSandboxid(), recordId,
                            baseRcord, conflictedRecords,
                            mergeCandidateRecords);
                }
            }

            // rest the validFlag and lastUpdated of the sandbox
            dao.validateSandbox(request.getSandboxid());
            return request.getSandboxid();

        } catch (Exception de) {
            throw new Exception(
                    "Failed to merge baseline to target sandbox ",
                    de);
        }

    }

    /**
     *
     * @param dao
     * @param sandboxId
     * @param recordId
     * @param baseRecord
     * @param conflictedRecords
     * @param mergeCandidateRecords
     * @throws Exception
     */
    private void mergeNewRecord(AtcfProcessDao dao, int sandboxId, int recordId,
            AbstractDeckRecord baseRecord,
            final Map<Integer, ConflictRecordPair> conflictedRecords,
            final Map<Integer, MergeCandidateRecord> mergeCandidateRecords)
            throws Exception {

        if (conflictedRecords.containsKey(recordId)) {
            ConflictRecordPair p = conflictedRecords.get(recordId);
            MergeCandidateRecord m = mergeCandidateRecords.get(recordId);
            if (p.getMergingRecordChangeCode() == DataChangeCode.NEW) {
                // [1, 1] merging
                if (m.getActionTaken() == MergeActionType.USESANDBOX) {
                    // select to retain the sandbox record
                    dao.updateSandboxDeckRecord(sandboxId,
                            m.getCandidateRecord(),
                            AbstractAtcfRecord.CHANG_CD_MODIFY);
                } else if (m.getActionTaken() == MergeActionType.USEBASELINE) {
                    // select to merge baseline record
                    dao.updateSandboxDeckRecord(sandboxId,
                            m.getCandidateRecord(),
                            AbstractAtcfRecord.CHANG_CD_UNCHANGE);
                }

            }
        } else {
            // [1,0]
            dao.mergeNewDeckRecordInSandbox(sandboxId, baseRecord);
        }

    }

    /**
     *
     * @param dao
     * @param sandboxId
     * @param recordId
     * @param baseRecord
     * @param conflictedRecords
     * @param mergeCandidateRecords
     * @throws Exception
     */
    private void mergeUpdatedRecord(AtcfProcessDao dao, int sandboxId,
            int recordId, AbstractDeckRecord baseRecord,
            final Map<Integer, ConflictRecordPair> conflictedRecords,
            final Map<Integer, MergeCandidateRecord> mergeCandidateRecords)
            throws Exception {

        if (conflictedRecords.containsKey(recordId)) {
            ConflictRecordPair p = conflictedRecords.get(recordId);
            MergeCandidateRecord m = mergeCandidateRecords.get(recordId);
            if (p.getMergingRecordChangeCode() == DataChangeCode.UPDATED) {
                // [2, 2] merging
                if (m.getActionTaken() == MergeActionType.USEBASELINE) {
                    // select to merge the baseline
                    dao.mergeUpdatedSandboxDeckRecord(sandboxId, baseRecord,
                            AbstractAtcfRecord.CHANG_CD_UNCHANGE);
                } else if (m.getActionTaken() == MergeActionType.MERGED) {
                    // User has manually mixed updated record with the baseline
                    dao.mergeUpdatedSandboxDeckRecord(sandboxId, baseRecord,
                            AbstractAtcfRecord.CHANG_CD_MODIFY);
                }

            } else if (p
                    .getMergingRecordChangeCode() == DataChangeCode.DELETED) {
                // [2, 3] merging
                if (m.getActionTaken() == MergeActionType.USEBASELINE) {
                    // select to merge the baseline
                    dao.mergeUpdatedSandboxDeckRecord(sandboxId, baseRecord,
                            AbstractAtcfRecord.CHANG_CD_UNCHANGE);
                }

            }
        } else {
            // [2, 0]
            dao.mergeUpdatedSandboxDeckRecord(sandboxId, baseRecord,
                    AbstractAtcfRecord.CHANG_CD_UNCHANGE);
        }

    }

    /**
     *
     * @param dao
     * @param sandboxId
     * @param recordId
     * @param baseRecord
     * @param conflictedRecords
     * @param mergeCandidateRecords
     * @throws Exception
     */
    private void mergeDeletedRecord(AtcfProcessDao dao, int sandboxId, int recordId,
            AbstractDeckRecord baseRecord,
            final Map<Integer, ConflictRecordPair> conflictedRecords,
            final Map<Integer, MergeCandidateRecord> mergeCandidateRecords)
            throws Exception {

        if (conflictedRecords.containsKey(recordId)) {
            ConflictRecordPair p = conflictedRecords.get(recordId);
            MergeCandidateRecord m = mergeCandidateRecords.get(recordId);
            if (p.getMergingRecordChangeCode() == DataChangeCode.UPDATED) {
                // [3, 2] merging
                if (m.getActionTaken() == MergeActionType.USEBASELINE) {
                    // select to use baseline , i.e., delete the record
                    dao.mergeDeletedDeckRecordInSandbox(sandboxId, baseRecord);
                } else if (m.getActionTaken() == MergeActionType.USESANDBOX) {
                    // select to retain sandbox record, it need to treat as a new record
                    dao.updateSandboxDeckRecord(sandboxId,
                            m.getCandidateRecord(),
                            AbstractAtcfRecord.CHANG_CD_NEW);
                }
            }
            else if (p.getMergingRecordChangeCode() == DataChangeCode.DELETED) {
                // [3, 3], delete the record
                dao.mergeDeletedDeckRecordInSandbox(sandboxId, baseRecord);
            }
        } else {
            // [3, 0]
            dao.mergeDeletedDeckRecordInSandbox(sandboxId, baseRecord);
        }

    }

}

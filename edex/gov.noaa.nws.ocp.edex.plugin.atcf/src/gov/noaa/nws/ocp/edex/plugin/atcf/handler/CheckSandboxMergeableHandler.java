/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.Map;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ChangedSandboxDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictMergingRecordSet;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckSandboxMergeableRequest;
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
public class CheckSandboxMergeableHandler
        implements IRequestHandler<CheckSandboxMergeableRequest> {

    @Override
    public ConflictMergingRecordSet handleRequest(
            CheckSandboxMergeableRequest request) throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            ConflictMergingRecordSet cmrs = dao
                    .checkSandboxMergeable(request.getSandboxid());
            String deckType = cmrs.getDeck().substring(0, 1).toUpperCase();

            Map<Integer, ChangedSandboxDeckRecord> baseRecords = dao
                    .getModifiedDeckRecords(AtcfDeckType.valueOf(deckType),
                            cmrs.getBaselineId());
            Map<Integer, ChangedSandboxDeckRecord> sboxRecords = dao
                    .getModifiedDeckRecords(AtcfDeckType.valueOf(deckType),
                            request.getSandboxid());
            dao.checkConflictRecords(request.getSandboxid(), cmrs, baseRecords,
                    sboxRecords);

            return cmrs;

        } catch (Exception de) {
            throw new Exception("Failed to run checkSandboxMergeable or other dao calls. ", de);
        }

    }

}

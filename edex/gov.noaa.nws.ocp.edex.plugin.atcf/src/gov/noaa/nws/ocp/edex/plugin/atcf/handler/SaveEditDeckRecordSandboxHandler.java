/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.SaveEditDeckRecordSandboxRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * SaveEditDeckRecordSandboxHandler
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
public class SaveEditDeckRecordSandboxHandler
        implements IRequestHandler<SaveEditDeckRecordSandboxRequest> {

    @Override
    public Void handleRequest(SaveEditDeckRecordSandboxRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {

            if (request.getModifiedRecords().isEmpty()) {
                throw new Exception("No modified record in the request!");
            }

            List<Integer> recordidList = new ArrayList<>();
            for (ModifiedDeckRecord r : request.getModifiedRecords()) {
                recordidList.add(r.getRecord().getId());
            }

            Map<Integer, Integer> currentChangeCds = dao.getCurrentChangeCD(
                    request.getDeckType(), request.getSandboxId(), recordidList,
                    false);

            // Process each record
            for (ModifiedDeckRecord mdr : request.getModifiedRecords()) {
                switch (mdr.getEditType()) {

                case NEW:
                    if (mdr.getRecord().getId() > 0) {
                        throw new Exception("The record: " + mdr.getRecord().getId()
                                + " may already exist!");
                    }

                    dao.addNewDeckRecordInSandbox(request.getSandboxId(),
                            mdr.getRecord());
                    break;

                case MODIFY:
                    dao.modifyDeckRecord(request.getSandboxId(),
                            currentChangeCds.get(mdr.getRecord().getId()), mdr);
                    break;

                case DELETE:
                    dao.deleteRecordInSandbox(request.getSandboxId(),
                            currentChangeCds.get(mdr.getRecord().getId()), mdr);
                    break;

                case UNDO:
                    dao.undoModifiedRecord(request.getSandboxId(),
                            mdr.getRecord().getId());
                }

            }

            // Update sandbox "lastUpdated" time.
            if (!request.getModifiedRecords().isEmpty()) {
                dao.updateSandboxLastupdated(request.getSandboxId());
            }

        } catch (Exception de) {
            throw new Exception("AtcfProcessDao failed to save deck records ",
                    de);
        }
        return null;
    }

}

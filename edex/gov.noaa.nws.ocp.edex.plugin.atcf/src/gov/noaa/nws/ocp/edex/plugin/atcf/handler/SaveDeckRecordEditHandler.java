/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.SaveDeckRecordEditRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * SaveBDeckRecordEditHandler
 *
 * The request should specify the edit type, i.e. "NEW" / "MODIFY" / "DELETE"
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 26, 2018            pwang       Initial creation
 * Aug 23, 2018 53502      dfriedman   Modify for Hibernate implementation.
 * Mar 29, 2019 61590      dfriedman   Merge type-specific handlers into one class.
 * Aug 05, 2019 66888      pwang       Use modifySandboxDeckRecord() .
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class SaveDeckRecordEditHandler
        implements IRequestHandler<SaveDeckRecordEditRequest> {

    @Override
    public Void handleRequest(SaveDeckRecordEditRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {

            RecordEditType recType = request.getRecordType();

            switch (request.getEditType()) {
            case NEW:
                dao.addNewDeckRecordInSandbox(request.getSandboxId(),
                        request.getRecord());
                break;
            case MODIFY:
                dao.modifySandboxDeckRecord(request.getSandboxId(),
                        request.getRecord(), recType);

                break;
            case DELETE:
                dao.markDeckInSandboxAsDeleted(request.getSandboxId(),
                        request.getRecord());
                break;
            case UNDO:
                dao.undoModifiedRecord(request.getSandboxId(),
                        request.getRecord().getId());
            }

            dao.updateSandboxLastupdated(request.getSandboxId());
        } catch (Exception de) {
            throw new Exception("AtcfProcessDao failed to save deck record ",
                    de);
        }
        return null;
    }

}
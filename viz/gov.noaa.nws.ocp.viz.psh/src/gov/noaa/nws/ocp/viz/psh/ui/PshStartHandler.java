/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshLockRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshLockRequest.ReqType;
import gov.noaa.nws.ocp.common.dataplugin.psh.response.PshLockServiceResponse;
import gov.noaa.nws.ocp.viz.psh.ui.generator.PshGeneratorDialog;

/**
 * A handler class to pop up the PSH setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 15, 2017 #34238      B. Yin      Initial creation.
 * Jul 05, 2017 #35463      wpaintsil   Open PSH Generator dialog 
 *                                      instead of Config dialog.
 * Jul 19, 2021 DCS22178    mporricelli Set up/check for PSH Lock
 *                                      to prevent multiple PSH open
 * 
 * </pre>
 * 
 * @author B. Yin
 * @version 1.0
 * 
 */
public class PshStartHandler extends AbstractHandler {

    protected static final IUFStatusHandler logger = UFStatus
            .getHandler(PshStartHandler.class);

    private PshGeneratorDialog pshDlg = null;

    /**
     * Pops up the PSH Generator dialog.
     */
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        WsId curUser = VizApp.getWsId();

        /* Attempt to establish clustertask lock in order to open PSH */
        PshLockRequest request = new PshLockRequest();
        request.setCurrentUser(curUser);
        request.setReqType(ReqType.GETLOCK);

        PshLockServiceResponse response = new PshLockServiceResponse();

        try {
            response = (PshLockServiceResponse) ThriftClient
                    .sendRequest(request);
        } catch (VizException e) {
            logger.error("PshLockRequest failed for " + curUser.toPrettyString(), e);
            return null;
        }

        if (response.isPshLockObtained()) {
            // Lock has been obtained; open PSH dialog
            if (pshDlg == null || !pshDlg.isOpen()) {
                pshDlg = new PshGeneratorDialog(shell);
            }
            pshDlg.open();
        } else {
            // Lock not obtained; give user option to break existing lock
            String lockStatus = response.isPshLockStale() ? "STALE" : "ACTIVE";
            int userResp = new MessageDialog(shell, "PSH In Use", null,
                    "PSH Application currently locked by "
                            + response.getLockOwner().toPrettyString()
                            + "\n\nThe lock appears to be " + lockStatus
                            + "\n\nWARNING: BEFORE BREAKING THIS LOCK, PLEASE COORDINATE WITH LOCK OWNER",
                    MessageDialog.QUESTION,
                    new String[] { "Break Lock", "Cancel" }, 1).open();

            if (userResp == 0) {
                request.setReqType(ReqType.FORCE_BREAKLOCK);
                try {
                    ThriftClient.sendRequest(request);
                } catch (VizException e) {
                    logger.error("PshLockRequest to break PSH Lock failed.", e);
                    return null;
                }
                // Have broken the PSH lock; try to get lock and open dialog
                request.setReqType(ReqType.GETLOCK);
                try {
                    response = (PshLockServiceResponse) ThriftClient
                            .sendRequest(request);
                } catch (VizException e) {
                    logger.error("PshLockRequest for " + curUser.toPrettyString() + " failed", e);
                }

                if (response.isPshLockObtained()) {
                    // Lock has been obtained; open PSH dialog
                    if (pshDlg == null || !pshDlg.isOpen()) {
                        pshDlg = new PshGeneratorDialog(shell);
                    }
                    pshDlg.open();
                }
            }
        }

        return null;
    }
}

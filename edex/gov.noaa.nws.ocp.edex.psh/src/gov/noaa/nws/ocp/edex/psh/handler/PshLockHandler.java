/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.handler;

import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshLockRequest;
import gov.noaa.nws.ocp.common.dataplugin.psh.request.PshLockRequest.ReqType;
import gov.noaa.nws.ocp.common.dataplugin.psh.response.PshLockServiceResponse;

/**
 * Handle request to manage PSH ClusterTask lock
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Ju1 19, 2021 DCS 22178  mporricelli  Initial creation
 *
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */

public class PshLockHandler implements IRequestHandler<PshLockRequest> {

    private static final long TIMEOUT_OVERRIDE = 5 * TimeUtil.MILLIS_PER_MINUTE;

    private static final String LOCKNAME = "PSHLock";

    private static final String LOCKDETAILS = "PSH";

    @Override
    public PshLockServiceResponse handleRequest(PshLockRequest request)
            throws Exception {
        ReqType reqType = request.getReqType();
        WsId curUser = request.getCurrentUser();

        PshLockServiceResponse plsResponse = new PshLockServiceResponse();

        ClusterTask ct = ClusterLockUtils.lookupLock(LOCKNAME, LOCKDETAILS);

        String extraInfo = "";

        if (ct != null && ct.getExtraInfo() != null) {
            extraInfo = ct.getExtraInfo();
        }

        switch (reqType) {

        case RENEW_TIMEOUT:
            renewTimeout(curUser, extraInfo);
            break;
        case CHECK_LOCKOWNER:
            plsResponse = checkLockOwner(extraInfo);
            break;
        case BREAKLOCK:
            breakLock(curUser, extraInfo);
            break;
        case FORCE_BREAKLOCK:
            forceBreakLock();
            break;
        case GETLOCK:
            plsResponse = getLock(curUser, extraInfo);
            break;
        default:
            throw new UnsupportedOperationException(
                    "Unrecognized PSH Lock request type: " + reqType);
        }
        return plsResponse;
    }

    /**
     * Create PSH Lock
     * 
     * @param curUser
     *            the current PSH application user
     * @param extraInfo
     *            the lock owner info from the extrainfo field of the PSHLock
     *            clustertask DB entry
     * @return PshLockServiceResponse containing lock info
     */
    private PshLockServiceResponse getLock(WsId curUser, String extraInfo) {
        // Attempt to obtain new lock for current user
        PshLockServiceResponse response = new PshLockServiceResponse();

        ClusterTask ct = ClusterLockUtils.lock(LOCKNAME, LOCKDETAILS,
                curUser.toString(), TIMEOUT_OVERRIDE, false);

        /*
         * If attempt to get new lock fails, collect information about existing
         * lock to display to user
         */
        if (!ct.getLockState().equals(LockState.SUCCESSFUL)) {
            if (!extraInfo.equals("")) {
                WsId pshLockWsId = new WsId(extraInfo);
                response.setLockOwner(pshLockWsId);
            }

            response.setPshLockObtained(false);

            long checkTime = System.currentTimeMillis();
            /*
             * Should not be stale here as would have succeeded above in taking
             * over the lock if beyond timeout
             */

            response.setPshLockStale(
                    checkTime > (ct.getLastExecution() + TIMEOUT_OVERRIDE));

        } else {
            // Lock has been obtained for current user

            response.setPshLockObtained(true);
        }

        return response;
    }

    /**
     * Renew the PSH Lock's lastexecution time
     * 
     * @param curUser
     *            the current PSH application user
     * @param extraInfo
     *            the lock owner info from the extrainfo field of the PSHLock
     *            clustertask DB entry
     */
    private void renewTimeout(WsId curUser, String extraInfo) {
        if (extraInfo.contains(curUser.toString())) {
            ClusterLockUtils.updateLockTime(LOCKNAME, LOCKDETAILS,
                    System.currentTimeMillis() + TIMEOUT_OVERRIDE);
        }
    }

    /**
     * Retrieve wsId for existing PSH Lock
     * 
     * @param extraInfo
     *            the lock owner info from the extrainfo field of the PSHLock
     *            clustertask DB entry
     * @return PshLockServiceResponse containing lock info
     */
    private PshLockServiceResponse checkLockOwner(String extraInfo) {
        PshLockServiceResponse response = new PshLockServiceResponse();
        if (!extraInfo.equals("")) {
            WsId pshLockWsId = new WsId(extraInfo);
            response.setLockOwner(pshLockWsId);
        }
        return response;
    }

    /**
     * Break the PSH Lock if current user is lock owner
     *
     * @param curUser
     *            the current PSH app user
     * @param extraInfo
     *            the lock owner info from the extrainfo field of the PSHLock
     *            clustertask DB entry
     */
    private void breakLock(WsId curUser, String extraInfo) {
        if (extraInfo.contains(curUser.toString())) {
            ClusterLockUtils.deleteLock(LOCKNAME, LOCKDETAILS);
        }
    }

    /**
     * Break the PSH Lock regardless of whether user is lock owner
     *
     */
    private void forceBreakLock() {
        ClusterLockUtils.deleteLock(LOCKNAME, LOCKDETAILS);
    }

}

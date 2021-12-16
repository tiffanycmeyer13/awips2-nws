/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.io.File;
import java.io.IOException;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.RunProcess;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ModelPriorityRequest;

/**
 * Request handler for ModelPriorityRequests.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * May 30, 2020 78922      mporricelli  Initial creation
 * Aug 11, 2020 76541      mporricelli  Get directory defs from
 *                                      properties file
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
@DynamicSerialize
public class ModelPriorityRequestHandler
        implements IRequestHandler<ModelPriorityRequest> {

    private static final int CLUSTER_LOCK_TIMEOUT = 300_000;

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ModelPriorityRequestHandler.class);

    @Override
    public String handleRequest(ModelPriorityRequest request) throws Exception {

        AtcfEnvironmentConfig envConfig = AtcfConfigurationManager
                .getEnvConfig();

        String atcfStrmsDir = envConfig.getAtcfstrms();

        statusHandler.info(
                "ModelPriorityRequestHandler: atcfStrmsDir=" + atcfStrmsDir);
        String pushScript = AtcfConfigurationManager.getPushScript().toString();

        Storm storm = request.getStorm();
        String dtg = request.getDtg();
        String priority = request.getPriority();

        String stormid = storm.getStormId().toLowerCase();

        File comFile = new File(
                atcfStrmsDir + File.separator + stormid + ".com");

        String output = null;

        String retval = "Success";

        if (!comFile.exists()) {
            retval = "Compute file " + comFile
                    + " does not exist. Not sending to WCOSS.";
            statusHandler.handle(Priority.SIGNIFICANT, retval);
            sendFailedMsg(stormid, dtg, retval);
            return retval;
        }
        /*
         * Construct and execute command to run pushToLdm.sh. pushToLdm.sh sends
         * the storm files to the LDAD Server and then to the NCEP LDM server
         * for transfer to WCOSS
         */
        String[] runCmd = new String[] { pushScript, stormid, priority };

        ClusterTask ct = null;
        try {
            do {
                ct = ClusterLockUtils.lock("atcfFileCreateLock",
                        "atcfCreateFiles", CLUSTER_LOCK_TIMEOUT, true);
            } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));

            RunProcess runPushScriptCmd = null;
            try {
                runPushScriptCmd = RunProcess.getRunProcess().exec(runCmd);
            } catch (IOException e) {
                statusHandler.handle(Priority.CRITICAL,
                        "ATCF: Problem executing " + pushScript + " - "
                                + e.getMessage(), e);
                retval = "Problem executing " + pushScript + " - "
                        + e.getMessage();
                sendFailedMsg(stormid, dtg, retval);
                return retval;
            }
            output = runPushScriptCmd.getStderr().trim();
        } finally {
            ClusterLockUtils.unlock(ct, false);
        }
        /*
         * Check output from script. If not successful return error, else start
         * monitoring retrieval directory for return from wcoss
         */
        if (!"Finished".equals(output)) {
            statusHandler.handle(Priority.CRITICAL,
                    "ATCF: Error running " + pushScript + ": " + output);
            retval = "Error running " + pushScript + ": " + output;
            sendFailedMsg(stormid, dtg, retval);

        } else {
            statusHandler.handle(Priority.SIGNIFICANT, pushScript
                    + "has finished. Model priority was sent to LDM");
        }
        return retval;
    }

    /**
     * Alert user that sending of data did not happen
     *
     * @param stormid
     * @param dtg
     * @param msg
     */
    private void sendFailedMsg(String stormid, String dtg, String msg) {
        EDEXUtil.sendMessageAlertViz(Priority.SIGNIFICANT,
                "ModelPriorityRequestHandler", "ANNOUNCER", "WORKSTATION",
                "ATCF Storm " + stormid
                        + "  --> MODEL PRIORITY *NOT* SUBMITTED for Date-Time: "
                        + dtg,
                msg, null);

    }

}

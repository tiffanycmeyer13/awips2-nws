/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.handler;

import java.util.Date;
import java.util.List;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ConflictSandbox;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.CheckinDeckRequest;
import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;
import gov.noaa.nws.ocp.edex.plugin.atcf.util.NotifyUtil;

/**
 * Handler to submit a sandbox to baseline, send out notification to conflicted
 * sandboxes, and return a list of possible conflicted sandboxes. Affected
 * sandboxes will be marked as invalid and the users need to update with the
 * changes before they can check in those sandboxes.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2019 60291      pwang     Initial creation
 * May 13, 2019 63859      pwang     return a list of ConflictSandbox
 *                                   send a notify message to the caller
 * Aug 28, 2019 67881      jwu       use AtcfDataChangeNotification
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class CheckinDeckHandler implements IRequestHandler<CheckinDeckRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CheckinDeckHandler.class);

    @Override
    public List<ConflictSandbox> handleRequest(CheckinDeckRequest request)
            throws Exception {

        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new Exception("AtcfProcessDao object creation failed", e);
        }

        try {
            List<ConflictSandbox> results = dao.submitModifiedDeckSandbox(
                    request.getDeckType(), request.getSandboxid());

            // Build a ATCF notification object.
            AtcfDataChangeNotification notification = new AtcfDataChangeNotification(
                    new Date(), request.getDeckType(), request.getSandboxid(),
                    request.getUserId(),
                    results.toArray(new ConflictSandbox[results.size()]));

            // Send out the notification.
            NotifyUtil.sendNotifyMessage(notification);

            return results;

        } catch (Exception de) {
            String message = "ATCF-CheckinDeckHandler: Failed to submit modified"
                    + request.getDeckType().getValue()
                    + "deck records into the baseline ";
            logger.error(message);
            throw new Exception(message, de);
        }

    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.DeleteClimateProdAfterReviewRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;

/**
 * Handler for user choosing to delete a product after it has been reviewed.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2, 2017  20637      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class DeleteClimateProdAfterReviewHandler
        implements IRequestHandler<DeleteClimateProdAfterReviewRequest> {

    @Override
    public Object handleRequest(DeleteClimateProdAfterReviewRequest request)
            throws Exception {

        String cpgSessionId = request.getCpgSessionID();

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(cpgSessionId);

        if (session.getState() == SessionState.SENT) {
            String msg = "Tried to delete sent products in " + cpgSessionId;
            ClimateProdGenerateSession.sendAlertVizMessage(Priority.WARN, msg,
                    null);
        }

        // Save and update database
        try {
            session.deleteClimateProd(request.getProdType(),
                    request.getDeleteProdKey());
        } catch (Exception e) {
            throw new Exception("Delete climate product failed", e);
        }

        return null;
    }

}

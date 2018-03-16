/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ForwardProdToNWRForReviewRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;

/**
 * Handler for when user is ready to review NWR products (forward to NWR Waves).
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
public class ForwardProdToNWRForReviewHandler
        implements IRequestHandler<ForwardProdToNWRForReviewRequest> {

    @Override
    public Object handleRequest(ForwardProdToNWRForReviewRequest request)
            throws Exception {

        String cpgSessionId = request.getCpgSessionID();

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(cpgSessionId);

        if (session.getState() == SessionState.SENT) {
            ClimateProdGenerateSession.sendAlertVizMessage(Priority.WARN,
                    "The same NWR products had been sent before!", null);
        }

        return session.sendAllNWRProducts(request.isOperational(),
                request.getUserId());
    }

}

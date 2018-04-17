/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CancelClimateProdGenerateRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;

/**
 * Handler for when user cancels a CPG session.
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
public class CancelClimateProdGenerateHandler
        implements IRequestHandler<CancelClimateProdGenerateRequest> {

    @Override
    public Object handleRequest(CancelClimateProdGenerateRequest request)
            throws Exception {

        String cpgSessionId = request.getCpgSessionID();

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(cpgSessionId);

        if (session.getClimateProdGenerateSessionData() == null) {
            throw new Exception(
                    "Missing CPG Session for the ID: " + cpgSessionId);
        }

        return session.cancelCPGSession(request.getUserId(),
                request.getReason());

    }

}

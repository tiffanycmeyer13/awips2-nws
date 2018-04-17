/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.GetClimateProdGenerateSessionRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;

/**
 * Handler to retrieve CPG session data by session ID.
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
public class GetClimateProdGenerateSessionHandler
        implements IRequestHandler<GetClimateProdGenerateSessionRequest> {

    @Override
    public Object handleRequest(GetClimateProdGenerateSessionRequest request)
            throws Exception {

        ClimateProdGenerateSessionDAO dao = null;
        try {
            dao = new ClimateProdGenerateSessionDAO();
        } catch (Exception e) {
            throw new Exception(
                    "ClimateProdGenerateSessionDAO object creation failed", e);
        }

        try {
            if (request.getCpgSessionID() == null) {
                // Get all CPG session data
                return dao.retrieveAllCPGSessionsForView();
            } else {
                // Get one CPG session by given session ID
                return dao.getCPGSessionForView(request.getCpgSessionID());
            }
        } catch (Exception de) {
            throw new Exception("Retrieve CPG session(s) failed", de);
        }

    }

}

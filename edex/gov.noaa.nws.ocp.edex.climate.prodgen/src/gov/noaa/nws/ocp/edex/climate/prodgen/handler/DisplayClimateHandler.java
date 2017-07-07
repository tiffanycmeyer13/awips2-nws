/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.DisplayClimateRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;

/**
 * DisplayClimateHandler
 * 
 * DisplayClimateRequest will only be issued when the user respond to auto
 * createClimate is done
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016 20637      pwang     Initial creation
 * Apr 07, 2017 20637      pwang     Support both auto and manual sessions
 * May 10, 2017 33104      amoore    Unneeded import.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class DisplayClimateHandler
        implements IRequestHandler<DisplayClimateRequest> {

    @Override
    public Object handleRequest(DisplayClimateRequest request)
            throws Exception {

        String cpgSessionId = request.getCpgSessionID();

        ClimateProdGenerateSessionDAO dao = null;
        try {
            dao = new ClimateProdGenerateSessionDAO();
        } catch (Exception e) {
            throw new Exception(
                    "ClimateProdGenerateSessionDAO object creation failed", e);
        }

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(dao, cpgSessionId);

        if (session.getState() != SessionState.CREATED
                && session.getState() != SessionState.DISPLAY) {
            String msg = "Display only, no editing and saving the climate report data at the state: "
                    + session.getState();
            session.sendAlertVizMessage(Priority.WARN, msg, null);
        }

        return session.startDisplayReportData(request.getUserId());

    }

}

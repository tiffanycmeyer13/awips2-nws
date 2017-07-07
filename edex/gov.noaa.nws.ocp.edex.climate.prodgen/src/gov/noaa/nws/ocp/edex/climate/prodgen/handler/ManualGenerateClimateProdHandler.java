/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ManualGenerateClimateProdRequest;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;

/**
 * ManualGenerateClimateProdHandler
 * 
 * ManualGenerateClimateProdHandler will create a CPG session, then run the
 * method to CreateClimate Then, return the session ID to the client
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016 20637      pwang       Initial creation
 * APR 04  2017 30166      amoore      Adjust return data.
 * 10 MAY 2017  33104      amoore      Can return just session ID.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ManualGenerateClimateProdHandler
        implements IRequestHandler<ManualGenerateClimateProdRequest> {

    @Override
    public Object handleRequest(ManualGenerateClimateProdRequest request)
            throws Exception {

        // Create a CPG session
        int runType = 2; // 2 : manual

        ClimateProdGenerateSessionDAO dao = null;
        try {
            dao = new ClimateProdGenerateSessionDAO();
        } catch (Exception e) {
            throw new Exception(
                    "ClimateProdGenerateSessionDAO object creation failed", e);
        }

        // Create a new CPG Session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(dao, runType, request.getProdType().getValue());

        if (session.getState() != SessionState.STARTED) {
            throw new Exception(
                    "Wrong session state, valid STATE should be STARTED");
        }

        // Call to create Daily or Period Climate
        // TODO change manual methods to void return
        if (request.getProdType().isDaily()) {
            session.manualCreateDailyClimate(request.getBeginDate(),
                    request.isNonRecentRun());
        } else {
            session.manualCreatePeriodClimate(request.getBeginDate(),
                    request.getEndDate(), request.isNonRecentRun());
        }

        return session.getCPGSessionId();
    }

}

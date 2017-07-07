/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.handler;

import java.util.HashMap;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;

import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.CompleteDisplayClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorDailyResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;

/**
 * CompleteDisplayClimateHandler
 * 
 * CompleteDisplayClimateHandler will only be issued when the user take
 * following actions: 1) On the DisplayClimate GUI, the user click "OK" to
 * approve the report data 2) User may make some modifications and click "OK" to
 * update the report data
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2016 20637      pwang       Initial creation
 * APR 04, 2017 30166      amoore      Modify for Display integration with workflow.
 * APR 06, 2017 30166      pwang       Separate Daily and Period calls
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class CompleteDisplayClimateHandler
        implements IRequestHandler<CompleteDisplayClimateRequest> {

    @Override
    public Object handleRequest(CompleteDisplayClimateRequest request)
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

        if (session.getState() != SessionState.DISPLAY) {
            String msg = "Display report data only, no action on the report data allowed when the state becomes " + session.getState();
            session.sendAlertVizMessage(Priority.WARN, msg, null);
            return null;
        }

        // Finalize the displayClimate
        if (session.getProdType().isDaily()) {
            ClimateCreatorDailyResponse modifiedDailyData = (ClimateCreatorDailyResponse) request
                    .getUserData();
            session.finalizeDisplayDailyCimate(modifiedDailyData);
            session.setReportDataAndUpdateDatabase(modifiedDailyData);
        } else {
            // Period cases
            ClimateCreatorPeriodResponse modifiedPeriodData = (ClimateCreatorPeriodResponse) request
                    .getUserData();
            // orgPeriodDataMap will be updated in the method
            // finalizeDisplayPeriodCimate
            HashMap<Integer, ClimatePeriodReportData> orgPeriodDataMap = request
                    .getOriginalDataMap();
            session.finalizeDisplayPeriodCimate(orgPeriodDataMap,
                    modifiedPeriodData, request.getMsmOverwriteApproved());
            modifiedPeriodData.setReportMap(orgPeriodDataMap);
            session.setReportDataAndUpdateDatabase(modifiedPeriodData);
        }

        // Update session state
        session.setState(SessionState.DISPLAYED);

        // Manual formatting will return climate products to the caller
        return session.manualFormatClimate(session.getProdType());

    }

}

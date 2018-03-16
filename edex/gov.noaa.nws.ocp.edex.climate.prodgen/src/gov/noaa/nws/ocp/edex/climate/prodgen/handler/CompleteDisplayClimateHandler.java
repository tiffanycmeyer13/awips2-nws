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
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSession;
import gov.noaa.nws.ocp.edex.climate.prodgen.ClimateProdGenerateSessionFactory;

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

        // Get existing session
        ClimateProdGenerateSession session = ClimateProdGenerateSessionFactory
                .getCPGSession(cpgSessionId);

        if (session.getState() != SessionState.DISPLAY) {
            String msg = "Display report data only, no action on the report data allowed when the state becomes "
                    + session.getState();
            ClimateProdGenerateSession.sendAlertVizMessage(Priority.WARN, msg,
                    null);
            return null;
        }

        // Finalize the displayClimate
        if (session.getProdType().isDaily()) {
            ClimateRunDailyData modifiedDailyData = (ClimateRunDailyData) request
                    .getUserData();
            session.finalizeDisplayDailyCimate(modifiedDailyData);
            session.setReportDataAndUpdateDatabase(modifiedDailyData);
        } else {
            // Period cases
            ClimateRunPeriodData modifiedPeriodData = (ClimateRunPeriodData) request
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

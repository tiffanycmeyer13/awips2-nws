/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.display;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.display.DisplayMonthlyBuildClimateServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;

/**
 * Request service handler for Period Display module, monthly build.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 27 JUL 2016  20414      amoore     Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class MonthlyBuildDisplayServiceHandler
        implements IRequestHandler<DisplayMonthlyBuildClimateServiceRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MonthlyBuildDisplayServiceHandler.class);

    @Override
    public Object handleRequest(
            DisplayMonthlyBuildClimateServiceRequest request) throws Exception {
        try {
            return new DailyClimateDAO().buildMonthObsClimo(
                    request.getDates().getStart(), request.getDates().getEnd(),
                    request.getStationID());
        } catch (Exception e) {
            logger.error("Error retrieving display monthly data for station ["
                    + request.getStationID() + "] and dates ["
                    + request.getDates().getStart().toFullDateString() + " to "
                    + request.getDates().getEnd().toFullDateString() + "]", e);
            throw e;
        }
    }
}

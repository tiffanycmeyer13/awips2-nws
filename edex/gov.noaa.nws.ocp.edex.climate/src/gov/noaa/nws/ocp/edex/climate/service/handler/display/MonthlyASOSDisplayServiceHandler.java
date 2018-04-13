/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.display;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.display.DisplayMonthlyASOSClimateServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Request service handler for Period Display module, monthly ASOS.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 17 MAY 2016  18384      amoore     Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class MonthlyASOSDisplayServiceHandler
        implements IRequestHandler<DisplayMonthlyASOSClimateServiceRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MonthlyASOSDisplayServiceHandler.class);

    @Override
    public Object handleRequest(DisplayMonthlyASOSClimateServiceRequest request)
            throws Exception {
        try {
            return new ClimatePeriodDAO().getMonthlyASOS(request.getStationCode(),
                    request.getMonth(), request.getYear());
        } catch (Exception e) {
            logger.error("Error retrieving display monthly data for station ["
                    + request.getStationCode() + "] and date ["
                    + request.getMonth() + "/" + request.getYear() + "]", e);
            throw e;
        }
    }
}

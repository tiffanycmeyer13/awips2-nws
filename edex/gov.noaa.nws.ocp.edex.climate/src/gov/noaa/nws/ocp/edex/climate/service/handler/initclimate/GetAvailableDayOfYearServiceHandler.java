/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.GetAvailableDayOfYearRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDailyNormDAO;

/**
 * Service handler to get first/last available DayOfYear from day_climate_norm
 * table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/12/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class GetAvailableDayOfYearServiceHandler
        implements IRequestHandler<GetAvailableDayOfYearRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(GetAvailableDayOfYearServiceHandler.class);

    @Override
    public Object handleRequest(GetAvailableDayOfYearRequest request)
            throws Exception {
        try {
            return new ClimateDailyNormDAO().getAvailableDayOfYear(
                    request.isFirstOne(), request.getStationId());
        } catch (Exception e) {
            logger.error("Failed to get day_of_year for station "
                    + request.getStationId(), e);
            throw e;
        }
    }
}

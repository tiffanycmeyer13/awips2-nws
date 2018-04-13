/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.monclimatenorm;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.GetAvailableMonthOfYearRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.MonthClimateNormDAO;

/**
 * Service handler to get first/last available monOfYear from mon_climate_norm
 * table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/04/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class GetAvailableMonthOfYearServiceHandler
        implements IRequestHandler<GetAvailableMonthOfYearRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(GetAvailableMonthOfYearServiceHandler.class);

    @Override
    public Object handleRequest(GetAvailableMonthOfYearRequest request)
            throws Exception {
        try {
            return new MonthClimateNormDAO().getAvailableMonthOfYear(
                    request.isFirstOne(), request.getStationId(),
                    request.getPeriodType());
        } catch (Exception e) {
            logger.error("Failed to get mon_of_year for station "
                    + request.getStationId(), e);
            throw e;
        }
    }
}

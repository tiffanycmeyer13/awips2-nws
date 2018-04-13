/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.monclimatenorm;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.FetchClimateMonthRecordRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.MonthClimateNormDAO;

/**
 * Service handler to fetch a row from mon_climate_norm table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/05/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class FetchClimateMonthRecordServiceHandler
        implements IRequestHandler<FetchClimateMonthRecordRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(FetchClimateMonthRecordServiceHandler.class);

    @Override
    public Object handleRequest(FetchClimateMonthRecordRequest request)
            throws Exception {
        try {
            return new MonthClimateNormDAO().fetchClimateMonthRecord(
                    request.getStationId(), request.getMonthOfYear(),
                    request.getPeriodType());
        } catch (Exception e) {
            logger.error("Failed to fetch row with mon_of_year="
                    + request.getMonthOfYear() + " for station "
                    + request.getStationId(), e);
            throw e;
        }
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.monclimatenorm;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.DeleteClimateMonthRecordRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.MonthClimateNormDAO;

/**
 * Service handler to delete a row from mon_climate_norm table
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

public class DeleteClimateMonthRecordServiceHandler
        implements IRequestHandler<DeleteClimateMonthRecordRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DeleteClimateMonthRecordServiceHandler.class);

    @Override
    public Object handleRequest(DeleteClimateMonthRecordRequest request)
            throws Exception {
        try {
            return new MonthClimateNormDAO().deleteClimateMonthRecord(
                    request.getStationId(), request.getMonthOfYear(),
                    request.getPeriodType());
        } catch (Exception e) {
            logger.error(
                    "Failed to delete row for station " + request.getStationId()
                            + " with mon_of_year=" + request.getMonthOfYear(),
                    e);
            throw e;
        }
    }
}

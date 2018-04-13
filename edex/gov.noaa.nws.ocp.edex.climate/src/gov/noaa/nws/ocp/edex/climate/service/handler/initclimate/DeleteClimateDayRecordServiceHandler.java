/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.DeleteClimateDayRecordRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDailyNormDAO;

/**
 * Service handler to delete a row from day_climate_norm table
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

public class DeleteClimateDayRecordServiceHandler
        implements IRequestHandler<DeleteClimateDayRecordRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DeleteClimateDayRecordServiceHandler.class);

    @Override
    public Object handleRequest(DeleteClimateDayRecordRequest request)
            throws Exception {
        try {
            return new ClimateDailyNormDAO().deleteClimateDayRecord(
                    request.getStationId(), request.getDayOfYear());
        } catch (Exception e) {
            logger.error(
                    "Failed to delete row for station " + request.getStationId()
                            + " with day_of_year=" + request.getDayOfYear(),
                    e);
            throw e;
        }
    }
}

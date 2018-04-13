/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.UpdateClimateDayNormNoMissingRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDailyNormDAO;

/**
 * Service handler to update a row in day_climate_norm table with non-missing values
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/21/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class UpdateClimateDayNormNoMissingServiceHandler
        implements IRequestHandler<UpdateClimateDayNormNoMissingRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(UpdateClimateDayNormNoMissingServiceHandler.class);

    @Override
    public Object handleRequest(UpdateClimateDayNormNoMissingRequest request)
            throws Exception {
        ClimateDayNorm dayRecord = request.getDayRecord();
        try {
            return new ClimateDailyNormDAO().updateClimateDayNormNoMissing(dayRecord);
        } catch (Exception e) {
            logger.error("Failed to Update row for station "
                    + dayRecord.getStationId() + " with day_of_year="
                    + dayRecord.getDayOfYear(), e);
            throw e;
        }
    }
}

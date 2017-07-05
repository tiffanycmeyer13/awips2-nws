/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.SaveClimateDayRecordRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDailyNormDAO;

/**
 * Service handler to save record in day_climate_norm table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/05/2016  20635    wkwock      Initial creation
 * 01/12/2017  26411    wkwock      Rename class name
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class SaveClimateDayRecordServiceHandler
        implements IRequestHandler<SaveClimateDayRecordRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SaveClimateDayRecordServiceHandler.class);

    @Override
    public Object handleRequest(SaveClimateDayRecordRequest request)
            throws Exception {
        ClimateDayNorm dayRecord = request.getDayRecord();
        try {
            return new ClimateDailyNormDAO().saveClimateDayRecord(dayRecord);
        } catch (Exception e) {
            logger.error("Failed to save row for station "
                    + dayRecord.getStationId() + " with day_of_year="
                    + dayRecord.getDayOfYear(), e);
            throw e;
        }
    }
}

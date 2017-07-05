/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.InsertClimatePeriodRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Service handler to insert a row into climate_period table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/13/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class InsertClimatePeriodServiceHandler
        implements IRequestHandler<InsertClimatePeriodRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(InsertClimatePeriodServiceHandler.class);

    @Override
    public Object handleRequest(InsertClimatePeriodRequest request)
            throws Exception {
        try {
            return new ClimatePeriodDAO().insertClimatePeriod(
                    request.getStationId(), request.getNormStartYear(),
                    request.getNormEndYear(), request.getRecordStartYear(),
                    request.getRecordEndYear());
        } catch (Exception e) {
            logger.error("Failed to insert a row for station="
                    + request.getStationId(), e);
            throw e;
        }
    }
}

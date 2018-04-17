/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.UpdateClimatePeriodRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Service handler to update a row in climate_period table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/14/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class UpdateClimatePeriodServiceHandler
        implements IRequestHandler<UpdateClimatePeriodRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(UpdateClimatePeriodServiceHandler.class);

    @Override
    public Object handleRequest(UpdateClimatePeriodRequest request)
            throws Exception {
        try {
            return new ClimatePeriodDAO().updateClimatePeriod(
                    request.getStationId(), request.getNormStartYear(),
                    request.getNormEndYear(), request.getRecordStartYear(),
                    request.getRecordEndYear());
        } catch (Exception e) {
            logger.error("Failed to update row with station="
                    + request.getStationId(), e);
            throw e;
        }
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchClimatePeriodRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Service handler to fetch a row from climate_period table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/21/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class FetchClimatePeriodServiceHandler
        implements IRequestHandler<FetchClimatePeriodRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(FetchClimatePeriodServiceHandler.class);

    @Override
    public Object handleRequest(FetchClimatePeriodRequest request)
            throws Exception {
        try {
            return new ClimatePeriodDAO()
                    .fetchClimatePeriod(request.getStationId());
        } catch (Exception e) {
            logger.error(
                    "Failed to fetch row for station=" + request.getStationId(),
                    e);
            throw e;
        }
    }
}

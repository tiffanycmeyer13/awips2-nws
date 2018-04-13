package gov.noaa.nws.ocp.edex.climate.service.handler.configparams;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.configparams.SnowPrecipServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimoDatesDAO;

/**
 * Service handler for getting snow and precipitation from climo_dates table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/14/2016  20639     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class SnowPrecipServiceHandler
        implements IRequestHandler<SnowPrecipServiceRequest> {
    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SnowPrecipServiceHandler.class);

    @Override
    public Object handleRequest(SnowPrecipServiceRequest request)
            throws Exception {
        try {
            return new ClimoDatesDAO().getSnowPrecip();
        } catch (Exception e) {
            logger.error(
                    "Failed to get snow and precipitation from climo_dates table",
                    e);
            throw e;
        }
    }

}

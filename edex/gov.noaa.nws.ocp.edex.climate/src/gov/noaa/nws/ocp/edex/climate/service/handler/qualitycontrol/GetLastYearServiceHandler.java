/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.qualitycontrol;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.GetLastYearRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;

/**
 * Request service handler for Daily data requests. Used with getLastYear(date,
 * stationId).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 27 OCT 2016  22135      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

public class GetLastYearServiceHandler
        implements IRequestHandler<GetLastYearRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(GetLastYearServiceHandler.class);

    @Override
    public Object handleRequest(GetLastYearRequest request) throws Exception {
        try {
            return new DailyClimateDAO().getLastYear(request.getDate(),
                    request.getStationID());
        } catch (Exception e) {
            logger.error("Error retrieving daily data for station ["
                    + request.getStationID() + "] and date ["
                    + request.getDate().toFullDateString() + "]", e);
            throw e;
        }
    }
}

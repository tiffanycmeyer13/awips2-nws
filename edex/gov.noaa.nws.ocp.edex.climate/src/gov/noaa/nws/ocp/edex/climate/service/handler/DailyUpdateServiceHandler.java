/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.DailyClimateServiceUpdateRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;

/**
 * Request service update handler for Daily data requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 11 JUL 2016  20414      amoore      Initial creation
 * 15 SEP 2016  20414      amoore      Move to generic location since this is now
 *                                     used by more than just Display.
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class DailyUpdateServiceHandler
        implements IRequestHandler<DailyClimateServiceUpdateRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DailyUpdateServiceHandler.class);

    @Override
    public Object handleRequest(DailyClimateServiceUpdateRequest request)
            throws Exception {
        try {
            return new DailyClimateDAO()
                    .updateDailyDataForStationAndDate(request.getDate(),
                            request.getStationID(), request.getData());
        } catch (Exception e) {
            logger.error("Error updating display daily data for station ["
                    + request.getStationID() + "] and date ["
                    + request.getDate().toFullDateString() + "]", e);
            throw e;
        }
    }
}

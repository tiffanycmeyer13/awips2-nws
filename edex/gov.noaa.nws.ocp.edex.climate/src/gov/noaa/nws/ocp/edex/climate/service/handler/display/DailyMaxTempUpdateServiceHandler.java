/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.display;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.display.DailyClimateMaxTempUpdateServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;

/**
 * Request service update handler for Daily max temp values.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 AUG 2016  20414      amoore     Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class DailyMaxTempUpdateServiceHandler
        implements IRequestHandler<DailyClimateMaxTempUpdateServiceRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DailyMaxTempUpdateServiceHandler.class);

    @Override
    public Object handleRequest(DailyClimateMaxTempUpdateServiceRequest request)
            throws Exception {
        try {
            return new DailyClimateDAO().updateDailyDataForMaxTemp(
                    request.getStationID(), request.getMaxTemp(),
                    request.getDates());
        } catch (Exception e) {
            logger.error("Error updating display daily data for station ["
                    + request.getStationID() + "] and max temp ["
                    + request.getMaxTemp() + "] and date count ["
                    + request.getDates().size() + "]", e);
            throw e;
        }
    }
}

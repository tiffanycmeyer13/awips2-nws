/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.qualitycontrol;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.qualitycontrol.DetFreezeDatesRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateFreezeDatesDAO;

/**
 * Request service handler for first and last freeze dates for a stationID in a
 * specified date range. Used with detFreezeDates(stationId, dates).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 23 NOV 2016  20636      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

public class DetFreezeDatesServiceHandler
        implements IRequestHandler<DetFreezeDatesRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DetFreezeDatesServiceHandler.class);

    @Override
    public Object handleRequest(DetFreezeDatesRequest request)
            throws Exception {
        try {
            return new ClimateFreezeDatesDAO()
                    .detFreezeDates(request.getStationID(), request.getDates());
        } catch (Exception e) {
            logger.error("Error retrieving daily data for station ["
                    + request.getStationID() + "] and dates ["
                    + request.getDates().getStart().toFullDateString() + " - "
                    + request.getDates().getEnd().toFullDateString() + "]", e);
            throw e;
        }
    }
}

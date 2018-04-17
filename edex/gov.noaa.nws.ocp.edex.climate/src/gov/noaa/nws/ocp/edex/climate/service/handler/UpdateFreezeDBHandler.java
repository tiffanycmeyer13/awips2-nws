/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.UpdateFreezeDBRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateFreezeDatesDAO;

/**
 * Request service handler for freeze date update requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 NOV 2016  20636      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

public class UpdateFreezeDBHandler
        implements IRequestHandler<UpdateFreezeDBRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(UpdateFreezeDBHandler.class);

    @Override
    public Object handleRequest(UpdateFreezeDBRequest request)
            throws Exception {
        try {
            return new ClimateFreezeDatesDAO().updateFreezeDB(request.getType(),
                    request.getStationID(), request.getDates());

        } catch (Exception e) {
            logger.error("Error checking/updating daily records for station ["
                    + request.getStationID() + "] and dates ["
                    + request.getDates().getStart().toFullDateString() + " - "
                    + request.getDates().getEnd().toFullDateString() + "]", e);
            throw e;
        }
    }
}

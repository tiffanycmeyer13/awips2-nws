/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.PeriodClimateServiceUpdateRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Request service update handler for Period Display module.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08 AUG 2016  20414      amoore     Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class PeriodUpdateServiceHandler
        implements IRequestHandler<PeriodClimateServiceUpdateRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PeriodUpdateServiceHandler.class);

    @Override
    public Object handleRequest(
            PeriodClimateServiceUpdateRequest request) throws Exception {
        try {
            return new ClimatePeriodDAO()
                    .updatePeriodData(
                            request.getStationID(), request.getDates(),
                            request.getPeriodType(), request.getData());
        } catch (Exception e) {
            logger.error("Error updating display period data for station ["
                    + request.getStationID() + "], dates ["
                    + request.getDates().getStart().toFullDateString() + "]["
                    + request.getDates().getEnd().toFullDateString()
                    + "], and period type [" + request.getPeriodType() + "]",
                    e);
            throw e;
        }
    }
}

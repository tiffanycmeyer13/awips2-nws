/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.display;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.display.DisplaySeasonalAnnualClimateServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Request service handler for Period Display module, seasonal/annual built from
 * monthly ASOS.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 17 AUG 2016  20414      amoore     Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class SeasonalAnnualDisplayServiceHandler
        implements IRequestHandler<DisplaySeasonalAnnualClimateServiceRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SeasonalAnnualDisplayServiceHandler.class);

    @Override
    public Object handleRequest(
            DisplaySeasonalAnnualClimateServiceRequest request)
                    throws Exception {
        try {
            return new ClimatePeriodDAO().getPeriodFromMonthlyASOS(
                    request.getStationCode(), request.getDates());
        } catch (Exception e) {
            logger.error("Error retrieving display period data for station ["
                    + request.getStationCode() + "] and dates ["
                    + request.getDates().getStart().toFullDateString()
                    + "] to [" + request.getDates().getEnd().toFullDateString()
                    + "]", e);
            throw e;
        }
    }
}

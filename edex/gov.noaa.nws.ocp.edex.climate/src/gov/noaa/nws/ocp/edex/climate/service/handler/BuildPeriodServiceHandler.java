/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.BuildPeriodServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Request service handler for calling buildPeriodObsClimo().
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 03 NOV 2016  20636      wpaintsil     Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

public class BuildPeriodServiceHandler
        implements IRequestHandler<BuildPeriodServiceRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(BuildPeriodServiceHandler.class);

    @Override
    public Object handleRequest(BuildPeriodServiceRequest request)
            throws Exception {
        try {
            return new ClimatePeriodDAO().buildPeriodObsClimo(
                    request.getStationId(), request.getDates(),
                    request.getGlobalValues(), request.getPeriodType());
        } catch (Exception e) {
            logger.error("Error retrieving display monthly data for station ["
                    + request.getStationId() + "] and dates ["
                    + request.getDates().getStart().toFullDateString() + " to "
                    + request.getDates().getEnd().toFullDateString() + "]", e);
            throw e;
        }
    }
}

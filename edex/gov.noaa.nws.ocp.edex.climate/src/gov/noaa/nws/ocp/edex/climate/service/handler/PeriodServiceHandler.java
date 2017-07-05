/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.PeriodServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Request service handler for Period data requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 SEP 2016  20636      wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

public class PeriodServiceHandler
        implements IRequestHandler<PeriodServiceRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PeriodServiceHandler.class);

    @Override
    public Object handleRequest(PeriodServiceRequest request) throws Exception {
        try {
            return new ClimatePeriodDAO().getPeriodData(request.getStationID(),
                    request.getPeriodType(), request.getDates());
        } catch (Exception e) {
            logger.error("Error retrieving period data for station ["
                    + request.getStationID() + "] and dates ["
                    + request.getDates().getStart().toFullDateString() + " - "
                    + request.getDates().getEnd().toFullDateString() + "]", e);
            throw e;
        }
    }
}

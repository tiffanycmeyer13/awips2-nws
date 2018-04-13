/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.qualitycontrol;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.FindDateRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateFindDateDAO;

/**
 * Request service handler for recent dates request.
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

public class FindDateServiceHandler
        implements IRequestHandler<FindDateRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(FindDateServiceHandler.class);

    @Override
    public Object handleRequest(FindDateRequest request) throws Exception {
        try {
            return new ClimateFindDateDAO().findDate(request.getStationID());
        } catch (Exception e) {
            logger.error("Error retrieving most recent dates for station ["
                    + request.getStationID() + "]");
            throw e;
        }
    }
}

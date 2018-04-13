/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.CompareUpdateDailyRecordsRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDailyNormDAO;

/**
 * Request service handler for Daily record check/update requests.
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

public class CompareUpdateDailyRecordsHandler
        implements IRequestHandler<CompareUpdateDailyRecordsRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CompareUpdateDailyRecordsHandler.class);

    @Override
    public Object handleRequest(CompareUpdateDailyRecordsRequest request)
            throws Exception {
        try {
            return new ClimateDailyNormDAO().compareUpdateDailyRecords(request.getDate(),
                    request.getStationID(), (short) request.getMaxTemp(),
                    (short) request.getMinTemp(), request.getPrecip(),
                    request.getSnow());

        } catch (Exception e) {
            logger.error("Error checking/updating daily records for station ["
                    + request.getStationID() + "] and date ["
                    + request.getDate().toFullDateString() + "]", e);
            throw e;
        }
    }
}

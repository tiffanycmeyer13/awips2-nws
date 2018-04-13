/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.CompareUpdatePeriodRecordsRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodNormDAO;

/**
 * Request service handler for Period record check/update requests.
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

public class CompareUpdatePeriodRecordsHandler
        implements IRequestHandler<CompareUpdatePeriodRecordsRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CompareUpdatePeriodRecordsHandler.class);

    @Override
    public Object handleRequest(CompareUpdatePeriodRecordsRequest request)
            throws Exception {
        try {
            return new ClimatePeriodNormDAO().compareUpdatePeriodRecords(
                    request.getType(), request.getEndDate(), request.getData());

        } catch (Exception e) {
            logger.error("Error checking/updating period records for station ["
                    + request.getData().getInformId() + "] and end date ["
                    + request.getEndDate().toFullDateString() + "]", e);
            throw e;
        }
    }
}

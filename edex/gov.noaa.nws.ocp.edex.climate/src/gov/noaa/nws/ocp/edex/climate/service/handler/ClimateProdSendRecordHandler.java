/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateProdSendRecordRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateProdSendRecordDAO;

/**
 * ClimateProdSendRecordHandler
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 2, 2017  20642      pwang       Initial creation
 * 20 JUN 2017  33104      amoore      Correct logging class.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateProdSendRecordHandler
        implements IRequestHandler<ClimateProdSendRecordRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdSendRecordHandler.class);

    @Override
    public Object handleRequest(ClimateProdSendRecordRequest request)
            throws Exception {
        try {
            return new ClimateProdSendRecordDAO().getSentClimateProductRecords(
                    request.getStartDateTime(), request.getEndDateTime());

        } catch (Exception e) {
            String msg = "Error retrieving Send Product Record between ["
                    + request.getStartDateTime() + "] and ["
                    + request.getEndDateTime() + "]";
            logger.error(msg, e);
            throw new Exception(msg, e);
        }
    }
}

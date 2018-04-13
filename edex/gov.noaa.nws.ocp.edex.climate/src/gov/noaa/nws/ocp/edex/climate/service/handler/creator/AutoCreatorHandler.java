/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.creator;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.creator.CreatorAutoRequest;
import gov.noaa.nws.ocp.edex.climate.creator.ClimateCreator;

/**
 * Request service handler for Auto (cron/scheduled) Climate Creator requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 21 NOV 2016  21378      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

public class AutoCreatorHandler implements IRequestHandler<CreatorAutoRequest> {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AutoCreatorHandler.class);

    @Override
    public Object handleRequest(CreatorAutoRequest request) throws Exception {
        try {
            return new ClimateCreator().createClimate(request.getPeriodType());
        } catch (Exception e) {
            logger.error(
                    "Error retrieving Climate Creator scheduled data for station",
                    e);
            throw e;
        }
    }
}

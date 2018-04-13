/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.initclimate;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate.FetchFreezeDatesRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateFreezeDatesDAO;

/**
 * Service handler to fetch a row from cli_freezedates table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 09/21/2016  20635    wkwock      Initial creation
 * 16 MAR 2017 30162    amoore      Merge two logically identical methods.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class FetchFreezeDatesServiceHandler
        implements IRequestHandler<FetchFreezeDatesRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(FetchFreezeDatesServiceHandler.class);

    @Override
    public Object handleRequest(FetchFreezeDatesRequest request)
            throws Exception {
        try {
            PeriodClimo periodClimo = PeriodClimo.getMissingPeriodClimo();
            new ClimateFreezeDatesDAO().getFreezeDates(request.getModule(),
                    request.getInformId(), periodClimo.getEarlyFreezeNorm(),
                    periodClimo.getLateFreezeNorm(),
                    periodClimo.getEarlyFreezeRec(),
                    periodClimo.getLateFreezeRec());
            return periodClimo;
        } catch (Exception e) {
            logger.error(
                    "Failed to fetch row with module=" + request.getModule()
                            + " and informId= " + request.getInformId(),
                    e);
            throw e;
        }
    }
}

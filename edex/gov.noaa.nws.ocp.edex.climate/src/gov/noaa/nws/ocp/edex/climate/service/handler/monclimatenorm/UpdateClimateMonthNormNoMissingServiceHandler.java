/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.monclimatenorm;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm.UpdateClimateMonthNormNoMissingRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.MonthClimateNormDAO;

/**
 * Service handler to update a row in mon_climate_norm table with
 * non-missing values
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#  Engineer    Description
 * ----------- -------- ----------- --------------------------
 * 10/05/2016  20635    wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class UpdateClimateMonthNormNoMissingServiceHandler
        implements IRequestHandler<UpdateClimateMonthNormNoMissingRequest> {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(UpdateClimateMonthNormNoMissingServiceHandler.class);

    @Override
    public Object handleRequest(UpdateClimateMonthNormNoMissingRequest request)
            throws Exception {
        PeriodClimo record = request.getRecord();
        try {
            return new MonthClimateNormDAO()
                    .updateClimateMonthNormNoMissing(record);
        } catch (Exception e) {
            logger.error(
                    "Failed to update station " + record.getInformId()
                            + " with mon_of_year=" + record.getMonthOfYear(),
                    e);
            throw e;
        }
    }
}

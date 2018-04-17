/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;

/**
 * 
 * Request to update a record in mon_climate_norm table with non-missing values
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/21/2016  20635     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class UpdateClimateMonthNormNoMissingRequest implements IServerRequest {

    @DynamicSerializeElement
    private PeriodClimo record;

    public UpdateClimateMonthNormNoMissingRequest() {
    }

    public UpdateClimateMonthNormNoMissingRequest(PeriodClimo record) {
        this.record = record;
    }

    public PeriodClimo getRecord() {
        return record;
    }

    public void setRecord(PeriodClimo record) {
        this.record = record;
    }
}

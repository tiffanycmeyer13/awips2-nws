/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;

/**
 * 
 * Request to update a record in day_climate_norm table for columns with
 * non-missing values.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/15/2016  20635     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class UpdateClimateDayNormNoMissingRequest implements IServerRequest {

    @DynamicSerializeElement
    private ClimateDayNorm dayRecord;

    public UpdateClimateDayNormNoMissingRequest() {
    }

    public UpdateClimateDayNormNoMissingRequest(ClimateDayNorm dayRecord) {
        this.dayRecord = dayRecord;
    }

    public ClimateDayNorm getDayRecord() {
        return dayRecord;
    }

    public void setDayRecord(ClimateDayNorm dayRecord) {
        this.dayRecord = dayRecord;
    }

}

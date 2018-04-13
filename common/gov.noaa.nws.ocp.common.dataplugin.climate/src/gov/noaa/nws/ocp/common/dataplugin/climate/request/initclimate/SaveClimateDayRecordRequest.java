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
 * Request to save a record in day_climate_norm table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/01/2016  20635     wkwock      Initial creation
 * 01/12/2017  26411     wkwock      rename class name
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class SaveClimateDayRecordRequest implements IServerRequest {

    @DynamicSerializeElement
    private ClimateDayNorm dayRecord;

    public SaveClimateDayRecordRequest() {
    }

    public SaveClimateDayRecordRequest(ClimateDayNorm dayRecord) {
        this.dayRecord = dayRecord;
    }

    public ClimateDayNorm getDayRecord() {
        return dayRecord;
    }

    public void setDayRecord(ClimateDayNorm dayRecord) {
        this.dayRecord = dayRecord;
    }

}

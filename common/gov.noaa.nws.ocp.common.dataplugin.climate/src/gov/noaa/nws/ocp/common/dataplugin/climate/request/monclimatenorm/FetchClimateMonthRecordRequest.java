/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.monclimatenorm;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * 
 * Request to fetch a record from mon_climate_norm table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/01/2016  20635     wkwock      Initial creation
 * 10/31/2016  20536     wkwock      Change type of periodType to PeriodType
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class FetchClimateMonthRecordRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationId;

    @DynamicSerializeElement
    private int monthOfYear;

    @DynamicSerializeElement
    private PeriodType periodType;

    public FetchClimateMonthRecordRequest() {
    }

    public FetchClimateMonthRecordRequest(int stationId, int monthOfYear,
            PeriodType periodtype) {
        this.stationId = stationId;
        this.monthOfYear = monthOfYear;
        this.periodType = periodtype;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    public void setMonthOfYear(int monthOfYear) {
        this.monthOfYear = monthOfYear;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
}

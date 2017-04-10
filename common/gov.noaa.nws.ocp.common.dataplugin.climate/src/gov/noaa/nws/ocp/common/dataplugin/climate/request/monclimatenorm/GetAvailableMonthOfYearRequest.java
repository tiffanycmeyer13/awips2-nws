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
 * Request to get first/last available month_of_year from mon_climate_norm table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/01/2016  20635     wkwock      Initial creation
 * 10/31/2016  20635     wkwock      Change int to PeriodType for periodType
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class GetAvailableMonthOfYearRequest implements IServerRequest {

    @DynamicSerializeElement
    private boolean firstOne;

    @DynamicSerializeElement
    private int stationId;

    @DynamicSerializeElement
    private PeriodType periodType;

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public GetAvailableMonthOfYearRequest() {
    }

    public GetAvailableMonthOfYearRequest(boolean firstOne, int stationId,
            PeriodType periodType) {
        this.firstOne = firstOne;
        this.stationId = stationId;
        this.periodType = periodType;
    }

    public boolean isFirstOne() {
        return firstOne;
    }

    public void setFirstOne(boolean firstOne) {
        this.firstOne = firstOne;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
}

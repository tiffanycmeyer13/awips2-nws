/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * 
 * Request to insert a record into climate_period table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/14/2016  20635     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class InsertClimatePeriodRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationId;

    @DynamicSerializeElement
    private int normStartYear;

    @DynamicSerializeElement
    private int normEndYear;

    @DynamicSerializeElement
    private int recordStartYear;

    @DynamicSerializeElement
    private int recordEndYear;

    public InsertClimatePeriodRequest() {
    }

    public InsertClimatePeriodRequest(int stationId, int normStartYear,
            int normEndYear, int recordStartYear, int recordEndYear) {
        this.stationId = stationId;
        this.normStartYear = normStartYear;
        this.normEndYear = normEndYear;
        this.recordStartYear = recordStartYear;
        this.recordEndYear = recordEndYear;
    }

    public int getNormStartYear() {
        return normStartYear;
    }

    public void setNormStartYear(int normStartYear) {
        this.normStartYear = normStartYear;
    }

    public int getNormEndYear() {
        return normEndYear;
    }

    public void setNormEndYear(int normEndYear) {
        this.normEndYear = normEndYear;
    }

    public int getRecordStartYear() {
        return recordStartYear;
    }

    public void setRecordStartYear(int recordStartYear) {
        this.recordStartYear = recordStartYear;
    }

    public int getRecordEndYear() {
        return recordEndYear;
    }

    public void setRecordEndYear(int recordEndYear) {
        this.recordEndYear = recordEndYear;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Request to check/update period record for a given type, end date, and period
 * data object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 JUL 2016  20636      wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class CompareUpdatePeriodRecordsRequest implements IServerRequest {

    @DynamicSerializeElement
    private PeriodType type;

    @DynamicSerializeElement
    private ClimateDate endDate;

    @DynamicSerializeElement
    private PeriodData data;

    /**
     * Empty constructor.
     */
    public CompareUpdatePeriodRecordsRequest() {
    }

    /**
     * Constructor
     * 
     * @param type
     * @param endDate
     * @param data
     */
    public CompareUpdatePeriodRecordsRequest(PeriodType type,

            ClimateDate endDate, PeriodData data) {
        this.type = type;
        this.endDate = endDate;
        this.data = data;
    }

    public PeriodType getType() {
        return type;
    }

    public void setType(PeriodType type) {
        this.type = type;
    }

    public ClimateDate getEndDate() {
        return endDate;
    }

    public void setEndDate(ClimateDate end) {
        this.endDate = end;
    }

    public PeriodData getData() {
        return data;
    }

    public void setData(PeriodData data) {
        this.data = data;
    }

}

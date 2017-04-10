/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Request for buildPeriodObsClimo.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 03 NOV 2016  20636      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class BuildPeriodServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationId;

    @DynamicSerializeElement
    private ClimateDates dates;

    @DynamicSerializeElement
    private ClimateGlobal globalValues;

    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * Empty constructor.
     */
    public BuildPeriodServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param iStationID
     *            station ID to search for.
     * @param iDates
     *            start/end dates (inclusive).
     */
    public BuildPeriodServiceRequest(int stationId, ClimateDates dates,
            ClimateGlobal globalValues, PeriodType periodType) {
        this.dates = dates;
        this.stationId = stationId;
        this.globalValues = globalValues;
        this.periodType = periodType;
    }

    public ClimateDates getDates() {
        return dates;
    }

    public void setDates(ClimateDates iDates) {
        this.dates = iDates;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public ClimateGlobal getGlobalValues() {
        return globalValues;
    }

    public void setGlobalValues(ClimateGlobal globalValues) {
        this.globalValues = globalValues;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }
}

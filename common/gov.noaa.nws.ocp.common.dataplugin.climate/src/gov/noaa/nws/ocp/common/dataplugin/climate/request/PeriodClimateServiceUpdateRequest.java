/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * Request to update period climate data for a stationID on dates with period
 * type.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08 AUG 2016  20414      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class PeriodClimateServiceUpdateRequest
        implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDates dates;

    @DynamicSerializeElement
    private PeriodData data;

    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * Empty constructor.
     */
    public PeriodClimateServiceUpdateRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     *            station ID.
     * @param dates
     *            dates.
     * @param periodType
     *            period type.
     * @param data
     *            data.
     */
    public PeriodClimateServiceUpdateRequest(int stationID,
            ClimateDates dates, PeriodType periodType, PeriodData data) {
        this.stationID = stationID;
        this.dates = dates;
        this.periodType = periodType;
        this.data = data;
    }

    /**
     * @return the station
     */
    public int getStationID() {
        return stationID;
    }

    /**
     * @return the dates
     */
    public ClimateDates getDates() {
        return dates;
    }

    /**
     * @param stationID
     */
    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    /**
     * @param dates
     */
    public void setDates(ClimateDates dates) {
        this.dates = dates;
    }

    /**
     * @return data
     */
    public PeriodData getData() {
        return data;
    }

    /**
     * @param data
     */
    public void setData(PeriodData data) {
        this.data = data;
    }

    /**
     * @return the periodType
     */
    public PeriodType getPeriodType() {
        return periodType;
    }

    /**
     * @param periodType
     *            the periodType to set
     */
    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }
}

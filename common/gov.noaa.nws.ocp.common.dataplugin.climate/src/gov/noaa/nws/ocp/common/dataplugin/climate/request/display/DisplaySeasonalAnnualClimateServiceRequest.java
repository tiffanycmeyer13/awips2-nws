/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.display;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;

/**
 * Request for seasonal/annual climate data compiled from monthly ASOS for a
 * stationID on dates.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 17 AUG 2016  20414      amoore      Initial creation
 * 04 OCT 2016  20414      amoore      Use Station Code (ICAO ID), not Station ID (inform ID).
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class DisplaySeasonalAnnualClimateServiceRequest
        implements IServerRequest {

    @DynamicSerializeElement
    private String stationCode;

    @DynamicSerializeElement
    private ClimateDates dates;

    /**
     * Empty constructor.
     */
    public DisplaySeasonalAnnualClimateServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationCode
     *            station ID.
     * @param dates
     *            dates.
     */
    public DisplaySeasonalAnnualClimateServiceRequest(String stationCode,
            ClimateDates dates) {
        this.stationCode = stationCode;
        this.dates = dates;
    }

    /**
     * @return the station
     */
    public String getStationCode() {
        return stationCode;
    }

    /**
     * @param stationCode
     */
    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    /**
     * @return the dates
     */
    public ClimateDates getDates() {
        return dates;
    }

    /**
     * @param dates
     *            the dates to set
     */
    public void setDates(ClimateDates dates) {
        this.dates = dates;
    }
}

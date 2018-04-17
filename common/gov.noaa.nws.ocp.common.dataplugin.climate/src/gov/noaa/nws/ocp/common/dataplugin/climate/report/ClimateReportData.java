/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.report;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Data for Climate Reports (daily or period).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05 DEC 2016  20414      amoore      Initial creation.
 * 04 APR 2017  30166      amoore      Add copy constructor.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
@DynamicSerialize
public abstract class ClimateReportData {

    /**
     * Station for this data. In Legacy was used by write_daily_info.f to make
     * the file of format "info_*".
     */
    @DynamicSerializeElement
    private Station station;

    /**
     * Empty constructor.
     */
    public ClimateReportData() {
    }

    /**
     * Copy constructor.
     * 
     * @param other
     */
    public ClimateReportData(ClimateReportData other) {
        station = new Station(other.getStation());
    }

    /**
     * Constructor.
     * 
     * @param iStation
     *            station for this data.
     */
    public ClimateReportData(Station iStation) {
        station = iStation;
    }

    /**
     * @return the station
     */
    public Station getStation() {
        return station;
    }

    /**
     * @param station
     *            the station to set
     */
    public void setStation(Station station) {
        this.station = station;
    }
}
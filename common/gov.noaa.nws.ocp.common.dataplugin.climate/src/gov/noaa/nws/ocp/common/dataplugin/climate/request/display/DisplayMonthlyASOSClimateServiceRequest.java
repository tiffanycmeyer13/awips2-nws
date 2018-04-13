/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.display;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request for monthly ASOS climate data for a stationID on dates.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 17 MAY 2016  18384      amoore      Initial creation
 * 07 JUL 2016  16962      amoore      Fix serialization
 * 04 OCT 2016  20414      amoore      Use Station Code (ICAO ID), not Station ID (inform ID).
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class DisplayMonthlyASOSClimateServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private String stationCode;

    @DynamicSerializeElement
    private int month;

    @DynamicSerializeElement
    private int year;

    /**
     * Empty constructor.
     */
    public DisplayMonthlyASOSClimateServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationCode
     *            station code (ICAO ID).
     * @param month
     * @param year
     */
    public DisplayMonthlyASOSClimateServiceRequest(String stationCode,
            int month, int year) {
        this.stationCode = stationCode;
        this.month = month;
        this.year = year;
    }

    /**
     * @return the station
     */
    public String getStationCode() {
        return stationCode;
    }

    /**
     * @param stationID
     */
    public void setStationCode(String stationID) {
        this.stationCode = stationID;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * @param month
     *            the month to set
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.qualitycontrol;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;

/**
 * Request for first and last freeze dates for a stationID in a specified date
 * range. Used with detFreezeDates(stationId, dates).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 23 NOV 2016  20636      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class DetFreezeDatesRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDates dates;

    /**
     * Empty constructor.
     */
    public DetFreezeDatesRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     *            station ID.
     * @param date
     *            dates.
     */
    public DetFreezeDatesRequest(int stationID, ClimateDates dates) {
        this.stationID = stationID;
        this.dates = dates;
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
}

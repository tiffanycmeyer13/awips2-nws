/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * Request for daily climate data for a stationID on a date.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 17 MAY 2016  18384      amoore      Initial creation
 * 07 JUL 2016  16962      amoore      Fix serialization
 * 15 SEP 2016  20414      amoore      Move to generic location since this is now
 *                                     used by more than just Display.
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class DailyClimateServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDate date;

    /**
     * Empty constructor.
     */
    public DailyClimateServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     *            station ID.
     * @param date
     *            date.
     */
    public DailyClimateServiceRequest(int stationID, ClimateDate date) {
        this.stationID = stationID;
        this.date = date;
    }

    /**
     * @return the station
     */
    public int getStationID() {
        return stationID;
    }

    /**
     * @return the date
     */
    public ClimateDate getDate() {
        return date;
    }

    /**
     * @param stationID
     */
    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    /**
     * @param myDate
     */
    public void setDate(ClimateDate date) {
        this.date = date;
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;

/**
 * Request to update daily climate data for a stationID on a date.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 11 JUL 2016  20414      amoore      Initial creation
 * 15 SEP 2016  20414      amoore      Move to generic location since this is now
 *                                     used by more than just Display.
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class DailyClimateServiceUpdateRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDate date;

    @DynamicSerializeElement
    private DailyClimateData data;

    /**
     * Empty constructor.
     */
    public DailyClimateServiceUpdateRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     *            station ID.
     * @param date
     *            date.
     * @param data
     *            data.
     */
    public DailyClimateServiceUpdateRequest(int stationID, ClimateDate date,
            DailyClimateData data) {
        this.stationID = stationID;
        this.date = date;
        this.data = data;
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
     * @param date
     */
    public void setDate(ClimateDate date) {
        this.date = date;
    }

    /**
     * @return data
     */
    public DailyClimateData getData() {
        return data;
    }

    /**
     * @param data
     */
    public void setData(DailyClimateData data) {
        this.data = data;
    }
}

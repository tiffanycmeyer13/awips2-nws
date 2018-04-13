/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.display;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * Request for to update min temperature daily climate data for a stationID.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 AUG 2016  20414      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */

@DynamicSerialize
public class DailyClimateMinTempUpdateServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private int minTemp;

    @DynamicSerializeElement
    private List<ClimateDate> dates;

    /**
     * Empty constructor.
     */
    public DailyClimateMinTempUpdateServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     *            station ID.
     * @param minTemp
     *            min temp.
     * @param dates
     *            dates of min temp.
     */
    public DailyClimateMinTempUpdateServiceRequest(int stationID, int minTemp,
            List<ClimateDate> dates) {
        this.stationID = stationID;
        this.minTemp = minTemp;
        this.dates = dates;
    }

    /**
     * @return the station
     */
    public int getStationID() {
        return stationID;
    }

    /**
     * @param stationID
     */
    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    /**
     * @return the minTemp
     */
    public int getMinTemp() {
        return minTemp;
    }

    /**
     * @param minTemp
     *            the minTemp to set
     */
    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    /**
     * @return the dates
     */
    public List<ClimateDate> getDates() {
        return dates;
    }

    /**
     * @param dates
     *            the dates to set
     */
    public void setDates(List<ClimateDate> dates) {
        this.dates = dates;
    }
}

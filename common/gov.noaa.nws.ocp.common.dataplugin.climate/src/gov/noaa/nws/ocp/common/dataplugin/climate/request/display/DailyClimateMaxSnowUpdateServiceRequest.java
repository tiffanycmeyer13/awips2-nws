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
 * Request for to update max snow daily climate data for a stationID.
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
public class DailyClimateMaxSnowUpdateServiceRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private int maxSnow;

    @DynamicSerializeElement
    private List<ClimateDate> dates;

    /**
     * Empty constructor.
     */
    public DailyClimateMaxSnowUpdateServiceRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     *            station ID.
     * @param maxSnow
     *            max snow.
     * @param dates
     *            dates of max snow.
     */
    public DailyClimateMaxSnowUpdateServiceRequest(int stationID, int maxSnow,
            List<ClimateDate> dates) {
        this.stationID = stationID;
        this.maxSnow = maxSnow;
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
     * @return the maxSnow
     */
    public int getMaxSnow() {
        return maxSnow;
    }

    /**
     * @param maxSnow
     *            the maxSnow to set
     */
    public void setMaxSnow(int maxSnow) {
        this.maxSnow = maxSnow;
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

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * Request to check daily record for a station and date.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 JUL 2016  20636      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class CompareUpdateDailyRecordsRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDate date;

    @DynamicSerializeElement
    private int maxTemp;

    @DynamicSerializeElement
    private int minTemp;

    @DynamicSerializeElement
    private float precip;

    @DynamicSerializeElement
    private float snow;

    /**
     * Empty constructor.
     */
    public CompareUpdateDailyRecordsRequest() {
    }

    /**
     * Constructor.
     * 
     * @param stationID
     * @param date
     * @param maxTemp
     * @param minTemp
     * @param precip
     * @param snow
     */
    public CompareUpdateDailyRecordsRequest(int stationID, ClimateDate date,
            int maxTemp, int minTemp, float precip, float snow) {
        this.stationID = stationID;
        this.date = date;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.precip = precip;
        this.snow = snow;
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

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public float getPrecip() {
        return precip;
    }

    public void setPrecip(float precip) {
        this.precip = precip;
    }

    public float getSnow() {
        return snow;
    }

    public void setSnow(float snow) {
        this.snow = snow;
    }

}

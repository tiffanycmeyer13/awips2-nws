/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class to represent the formatted forecast data at a given forecast hour (TAU).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 12, 2021 87783      jwu         Initial coding.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class ForecastData {

    /**
     * Forecast times in UTC
     */
    @DynamicSerializeElement
    private ForecastTime times;

    /**
     * Forecast times in local advisory time zone.
     */
    @DynamicSerializeElement
    private ForecastTime localTimes;
    
   /**
     * Forecast position - lat, i.e, "29.2N".
     */
    @DynamicSerializeElement
    private String lat;

    /**
     * Forecast position - lon, i.e, "78.3W".
     */
    @DynamicSerializeElement
    private String lon;

    /**
     * Forecast position (full description) lat, i.e, "29.2 North".
     */
    @DynamicSerializeElement
    private String fullLat;

    /**
     * Forecast position -(full description) lon, i.e, "78.3 West".
     */
    @DynamicSerializeElement
    private String fullLon;

    /**
     * Forecast storm state, i.e, "INLAND".
     */
    @DynamicSerializeElement
    private String state;

    /**
     * Dissipated or not.
     */
    @DynamicSerializeElement
    private boolean dissipated;

    /**
     * Wind data - maximum wind, gust, wind radii/wave radii.
     */
    @DynamicSerializeElement
    private WindData windData;

    /**
     * Constructor
     */
    public ForecastData() {
        this.times = new ForecastTime();
        this.localTimes = new ForecastTime();
        this.lat = "";
        this.lon = "";
        this.fullLat = "";
        this.fullLon = "";
        this.state = "";
        this.dissipated = false;
        this.windData = null;
    }

    /**
     * @return the times
     */
    public ForecastTime getTimes() {
        return times;
    }

    /**
     * @param times
     *            the times to set
     */
    public void setTimes(ForecastTime times) {
        this.times = times;
    }

    /**
     * @return the localTimes
     */
    public ForecastTime getLocalTimes() {
        return localTimes;
    }

    /**
     * @param localTimes
     *            the localTimes to set
     */
    public void setLocalTimes(ForecastTime localTimes) {
        this.localTimes = localTimes;
    }

    /**
     * @return the lat
     */
    public String getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(String lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public String getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(String lon) {
        this.lon = lon;
    }

    /**
     * @return the fullLat
     */
    public String getFullLat() {
        return fullLat;
    }

    /**
     * @param fullLat the fullLat to set
     */
    public void setFullLat(String fullLat) {
        this.fullLat = fullLat;
    }

    /**
     * @return the fullLon
     */
    public String getFullLon() {
        return fullLon;
    }

    /**
     * @param fullLon
     *            the fullLon to set
     */
    public void setFullLon(String fullLon) {
        this.fullLon = fullLon;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the dissipated
     */
    public boolean isDissipated() {
        return dissipated;
    }

    /**
     * @param dissipated
     *            the dissipated to set
     */
    public void setDissipated(boolean dissipated) {
        this.dissipated = dissipated;
    }

    /**
     * @return the windData
     */
    public WindData getWindData() {
        return windData;
    }

    /**
     * @param windData
     *            the windData to set
     */
    public void setWindData(WindData windData) {
        this.windData = windData;
    }

    /**
     * Write as an information string.
     * 
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(times.toString());
        sb.append(String
                .format("Forecast Valid: %s%sZ %s %s", times.getDdHH(),
                        times.getMinute(), lat, lon)
                .toUpperCase());
        if (!state.isEmpty()) {
            sb.append(String.format("...%s", state));
        }
        sb.append("\n");

        sb.append(windData.toString());

        return sb.toString();
    }

}

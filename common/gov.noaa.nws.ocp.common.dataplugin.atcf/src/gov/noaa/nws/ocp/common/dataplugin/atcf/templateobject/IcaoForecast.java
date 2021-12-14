/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class to represent the formatted ICAO data information.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 24, 2021 88638      jwu         Initial coding.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class IcaoForecast {

    /**
     * Forecast time
     */
    @DynamicSerializeElement
    private ForecastTime time;

    /**
     * Storm Direction and Speed
     */
    @DynamicSerializeElement
    private String movement;

    /**
     * Intensity change (2021):
     *
     * Values: INTSF --> Intensifying; WKN --> Weakening NC --> No change
     */
    @DynamicSerializeElement
    private String intensityChange;

    /**
     * Maximum Sea Level Pressure, padded as a 4 character string, i.e., 0965
     */
    @DynamicSerializeElement
    private String mslp;

    /**
     * Forecast latitude position in minutes, padded as a 5-character string
     * (i.e.,N3654)
     */
    @DynamicSerializeElement
    private String lat;

    /**
     * Forecast longitude position in minutes, padded as a 6-character string
     * (i.e.,N07445)
     */
    @DynamicSerializeElement
    private String lon;

    /**
     * Forecast maximum wind padded as a 3-character string (i.e.,045)
     */
    @DynamicSerializeElement
    private String maxWind;

    /**
     * Constructor
     */
    public IcaoForecast() {
        this.time = new ForecastTime();
        this.movement = "";
        this.intensityChange = "NC";
        this.mslp = "";
        this.lat = "N////";
        this.lon = "W/////";
        this.maxWind = "///";
    }

    /**
     * @return the time
     */
    public ForecastTime getTime() {
        return time;
    }

    /**
     * @param time
     *            the time to set
     */
    public void setTime(ForecastTime time) {
        this.time = time;
    }

    /**
     * @return the movement
     */
    public String getMovement() {
        return movement;
    }

    /**
     * @param movement
     *            the movement to set
     */
    public void setMovement(String movement) {
        this.movement = movement;
    }

    /**
     * @return the intensityChange
     */
    public String getIntensityChange() {
        return intensityChange;
    }

    /**
     * @param intensityChange
     *            the intensityChange to set
     */
    public void setIntensityChange(String intensityChange) {
        this.intensityChange = intensityChange;
    }

    /**
     * @return the mslp
     */
    public String getMslp() {
        return mslp;
    }

    /**
     * @param mslp
     *            the mslp to set
     */
    public void setMslp(String mslp) {
        this.mslp = mslp;
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
     * @return the maxWind
     */
    public String getMaxWind() {
        return maxWind;
    }

    /**
     * @param maxWind
     *            the maxWind to set
     */
    public void setMaxWind(String maxWind) {
        this.maxWind = maxWind;
    }

}

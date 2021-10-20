/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * An Object to all pre-formatted data for the discussion section in Public
 * Advisory.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 22, 2021 87781      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class AdvisoryDiscussion {

    /**
     * Lead wording for the expecting moving.
     */
    private static final String MOTION_EXPECTED = " and this motion is expected to ";

    /**
     * A placeholder to insert expecting moving.
     */
    private static final String MOTION_INSERT = "!** [PLACE EXPECTED MOTION INFO HERE] **!";

    /**
     * Advisory issuance time in local time zone., i.e., "500 AM EDT". Same as
     * in ForecastTime's localTime.hhmmaz.
     */
    @DynamicSerializeElement
    private String localTime;

    /**
     * Advisory issuance time in local time zone., i.e., "0900". Same as in
     * ForecastTime's time.hhmm.
     */
    @DynamicSerializeElement
    private String utcTime;

    /**
     * Forecast position (full description) lat, i.e, "29.2 North". Same as
     * ForecastData's fullLat.
     */
    @DynamicSerializeElement
    private String fullLat;

    /**
     * Forecast position -(full description) lon, i.e, "78.3 West". Same as
     * ForecastData's fullLat.
     */
    @DynamicSerializeElement
    private String fullLon;

    /**
     * Advisory class. Same as in AdvisoryHeader.
     */
    @DynamicSerializeElement
    private String advClass;

    /**
     * Advisory name. Same as in AdvisoryHeader.
     */
    @DynamicSerializeElement
    private String advName;

    /**
     * Maximum wind speed in mph & km/h. Same as in AdvisorySummary.
     */
    @DynamicSerializeElement
    private String windMph;

    @DynamicSerializeElement
    private String windKmh;

    /**
     * Storm minimum sea level pressure in mb & inches. Same as in
     * AdvisorySummary.
     */
    @DynamicSerializeElement
    private String pressureMb;

    @DynamicSerializeElement
    private String pressureIn;

    /**
     * Intensity descriptor. "disturbance", "remnants of", or "center of"
     */
    private String intensityStatus;

    /**
     * Position centered or located descriptor
     */
    @DynamicSerializeElement
    private String centerOrLocated;

    /**
     * Intensity descriptor. "depression", "post-tropical cyclone", "remnants",
     * "storm", "system", or the storm name.
     */
    @DynamicSerializeElement
    private String longIntensity;

    /**
     * Movement descriptor. "stationary", or "moving towards the".
     */
    @DynamicSerializeElement
    private String movingOrStay;

    /**
     * Movement direction and speed.
     */
    @DynamicSerializeElement
    private String movingDirSpd;

    /**
     * Motion expectation - "and this motion is expected to ".
     */
    @DynamicSerializeElement
    private String expectedMotion;

    /**
     * Reminder to insert moving - "!** [PLACE EXPECTED MOTION INFO HERE] **!".
     */
    @DynamicSerializeElement
    private String insertMotion;

    /**
     * Cyclone category number (1 to 5) -- only 3 to 5 will be used..
     */
    @DynamicSerializeElement
    private String stormCategory;

    /**
     * Flag to add genesis info.
     */
    @DynamicSerializeElement
    private boolean addGenesisInfo;

    /**
     * Intensity tendency in last 48 hours.
     */
    @DynamicSerializeElement
    private String intensityTendency;

    /**
     * Flag for hurricane-force winds >64 kt extent
     */
    @DynamicSerializeElement
    private boolean windGr64;

    /**
     * Maximum wind radii for winds >64 kt
     */
    @DynamicSerializeElement
    private String windRad64Mph;

    @DynamicSerializeElement
    private String windRad64Kmh;

    /**
     * Flag for hurricane-force winds >34 kt extent
     */
    @DynamicSerializeElement
    private boolean windGr34;

    /**
     * Maximum wind radii for winds >34 kt
     */
    @DynamicSerializeElement
    private String windRad34Mph;

    @DynamicSerializeElement
    private String windRad34Kmh;

    /**
     * Flag if this is a sub-tropical storm (intensity="SS")
     */
    @DynamicSerializeElement
    private boolean subtropicalStorm;

    /**
     * Default constructor
     */
    public AdvisoryDiscussion() {
        this.localTime = "";
        this.utcTime = "";
        this.fullLat = "";
        this.fullLon = "";
        this.advClass = "";
        this.advName = "";
        this.windMph = "";
        this.windKmh = "";
        this.pressureMb = "";
        this.pressureIn = "";
        this.intensityStatus = "";
        this.centerOrLocated = "";
        this.longIntensity = "";
        this.movingOrStay = "";
        this.movingDirSpd = "";
        this.expectedMotion = MOTION_EXPECTED;
        this.insertMotion = MOTION_INSERT;
        this.stormCategory = "";
        this.addGenesisInfo = false;
        this.intensityTendency = "";
        this.windGr64 = false;
        this.windRad64Mph = "";
        this.windRad64Kmh = "";
        this.windGr34 = false;
        this.windRad34Mph = "";
        this.windRad34Kmh = "";
        this.subtropicalStorm = false;
    }

    /**
     * @return the localTime
     */
    public String getLocalTime() {
        return localTime;
    }

    /**
     * @param localTime
     *            the localTime to set
     */
    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }

    /**
     * @return the utcTime
     */
    public String getUtcTime() {
        return utcTime;
    }

    /**
     * @param utcTime
     *            the utcTime to set
     */
    public void setUtcTime(String utcTime) {
        this.utcTime = utcTime;
    }

    /**
     * @return the fullLat
     */
    public String getFullLat() {
        return fullLat;
    }

    /**
     * @param fullLat
     *            the fullLat to set
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
     * @return the advClass
     */
    public String getAdvClass() {
        return advClass;
    }

    /**
     * @param advClass
     *            the advClass to set
     */
    public void setAdvClass(String advClass) {
        this.advClass = advClass;
    }

    /**
     * @return the advName
     */
    public String getAdvName() {
        return advName;
    }

    /**
     * @param advName
     *            the advName to set
     */
    public void setAdvName(String advName) {
        this.advName = advName;
    }

    /**
     * @return the windMph
     */
    public String getWindMph() {
        return windMph;
    }

    /**
     * @param windMph
     *            the windMph to set
     */
    public void setWindMph(String windMph) {
        this.windMph = windMph;
    }

    /**
     * @return the windKmh
     */
    public String getWindKmh() {
        return windKmh;
    }

    /**
     * @param windKmh
     *            the windKmh to set
     */
    public void setWindKmh(String windKmh) {
        this.windKmh = windKmh;
    }

    /**
     * @return the pressureMb
     */
    public String getPressureMb() {
        return pressureMb;
    }

    /**
     * @param pressureMb
     *            the pressureMb to set
     */
    public void setPressureMb(String pressureMb) {
        this.pressureMb = pressureMb;
    }

    /**
     * @return the pressureIn
     */
    public String getPressureIn() {
        return pressureIn;
    }

    /**
     * @param pressureIn
     *            the pressureIn to set
     */
    public void setPressureIn(String pressureIn) {
        this.pressureIn = pressureIn;
    }

    /**
     * @return the intensityStatus
     */
    public String getIntensityStatus() {
        return intensityStatus;
    }

    /**
     * @param intensityStatus
     *            the intensityStatus to set
     */
    public void setIntensityStatus(String intensityStatus) {
        this.intensityStatus = intensityStatus;
    }

    /**
     * @return the centerOrLocated
     */
    public String getCenterOrLocated() {
        return centerOrLocated;
    }

    /**
     * @param centerOrLocated
     *            the centerOrLocated to set
     */
    public void setCenterOrLocated(String centerOrLocated) {
        this.centerOrLocated = centerOrLocated;
    }

    /**
     * @return the longIntensity
     */
    public String getLongIntensity() {
        return longIntensity;
    }

    /**
     * @param longIntensity
     *            the longIntensity to set
     */
    public void setLongIntensity(String longIntensity) {
        this.longIntensity = longIntensity;
    }

    /**
     * @return the movingOrStay
     */
    public String getMovingOrStay() {
        return movingOrStay;
    }

    /**
     * @param movingOrStay
     *            the movingOrStay to set
     */
    public void setMovingOrStay(String movingOrStay) {
        this.movingOrStay = movingOrStay;
    }

    /**
     * @return the movingDirSpd
     */
    public String getMovingDirSpd() {
        return movingDirSpd;
    }

    /**
     * @param movingDirSpd
     *            the movingDirSpd to set
     */
    public void setMovingDirSpd(String movingDirSpd) {
        this.movingDirSpd = movingDirSpd;
    }

    /**
     * @return the expectedMotion
     */
    public String getExpectedMotion() {
        return expectedMotion;
    }

    /**
     * @param expectedMotion
     *            the expectedMotion to set
     */
    public void setExpectedMotion(String expectedMotion) {
        this.expectedMotion = expectedMotion;
    }

    /**
     * @return the insertMotion
     */
    public String getInsertMotion() {
        return insertMotion;
    }

    /**
     * @param insertMotion
     *            the insertMotion to set
     */
    public void setInsertMotion(String insertMotion) {
        this.insertMotion = insertMotion;
    }

    /**
     * @return the stormCategory
     */
    public String getStormCategory() {
        return stormCategory;
    }

    /**
     * @param stormCategory
     *            the stormCategory to set
     */
    public void setStormCategory(String stormCategory) {
        this.stormCategory = stormCategory;
    }

    /**
     * @return the addGenesisInfo
     */
    public boolean isAddGenesisInfo() {
        return addGenesisInfo;
    }

    /**
     * @param addGenesisInfo
     *            the addGenesisInfo to set
     */
    public void setAddGenesisInfo(boolean addGenesisInfo) {
        this.addGenesisInfo = addGenesisInfo;
    }

    /**
     * @return the intensityTendency
     */
    public String getIntensityTendency() {
        return intensityTendency;
    }

    /**
     * @param intensityTendency
     *            the intensityTendency to set
     */
    public void setIntensityTendency(String intensityTendency) {
        this.intensityTendency = intensityTendency;
    }

    /**
     * @return the windGr64
     */
    public boolean isWindGr64() {
        return windGr64;
    }

    /**
     * @param windGr64
     *            the windGr64 to set
     */
    public void setWindGr64(boolean windGr64) {
        this.windGr64 = windGr64;
    }

    /**
     * @return the windRad64Mph
     */
    public String getWindRad64Mph() {
        return windRad64Mph;
    }

    /**
     * @param windRad64Mph
     *            the windRad64Mph to set
     */
    public void setWindRad64Mph(String windRad64Mph) {
        this.windRad64Mph = windRad64Mph;
    }

    /**
     * @return the windRad64Kmh
     */
    public String getWindRad64Kmh() {
        return windRad64Kmh;
    }

    /**
     * @param windRad64Kmh
     *            the windRad64Kmh to set
     */
    public void setWindRad64Kmh(String windRad64Kmh) {
        this.windRad64Kmh = windRad64Kmh;
    }

    /**
     * @return the windGr34
     */
    public boolean isWindGr34() {
        return windGr34;
    }

    /**
     * @param windGr34
     *            the windGr34 to set
     */
    public void setWindGr34(boolean windGr34) {
        this.windGr34 = windGr34;
    }

    /**
     * @return the windRad34Mph
     */
    public String getWindRad34Mph() {
        return windRad34Mph;
    }

    /**
     * @param windRad34Mph
     *            the windRad34Mph to set
     */
    public void setWindRad34Mph(String windRad34Mph) {
        this.windRad34Mph = windRad34Mph;
    }

    /**
     * @return the windRad34Kmh
     */
    public String getWindRad34Kmh() {
        return windRad34Kmh;
    }

    /**
     * @param windRad34Kmh
     *            the windRad34Kmh to set
     */
    public void setWindRad34Kmh(String windRad34Kmh) {
        this.windRad34Kmh = windRad34Kmh;
    }

    /**
     * @return the subtropicalStorm
     */
    public boolean isSubtropicalStorm() {
        return subtropicalStorm;
    }

    /**
     * @param subtropicalStorm
     *            the subtropicalStorm to set
     */
    public void setSubtropicalStorm(boolean subtropicalStorm) {
        this.subtropicalStorm = subtropicalStorm;
    }
}

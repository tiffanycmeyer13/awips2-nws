/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Converted from rehost-adapt/adapt/climate/include/TYPE_daily_climate_data.h
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2015            xzhang      Initial creation
 * 18 MAY 2016  18384      amoore      Clarifications, relative humidity times are just hours.
 * 18 MAY 2016  18384      amoore      Serialization
 * 08 JUL 2016  16962      amoore      Add constants for WX Type array index references.
 * 09 AUG 2016  20414      amoore      Cleanup naming and comments.
 * 26 SEP 2016  21378      amoore      WX indices confirmed from retrieve_daily_sum.ec
 *                                     and build_daily_obs_weather.ec.
 * 12 OCT 2016  21378      amoore      Appropriate missing values (all are some variant of 9's).
 * 27 OCT 2016  22135      wpaintsil   Add DailyDataMethod field.
 * 25 JAN 2017  22786      amoore      Remove action to split WX array into separate fields.
 *                                     Rename constant to more clear meaning.
 * 13 APR 2018  DR17116    wpaintsil   Change precipSeason and snowSeason to account 
 *                                     for multiple alternate seasons.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class DailyClimateData {
    /**
     * Total weather event types. Size of wx array.
     */
    public static final int TOTAL_WX_TYPES = 18;

    /**
     * WX Type index for TS (thunder storms). Index validated from Legacy
     * retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_THUNDER_STORM_INDEX = 0;

    /**
     * WX Type index for -FZRA (light freezing rain). Index validated from
     * Legacy build_daily_obs_weather.ec.
     */
    public static final int WX_LIGHT_FREEZING_RAIN_INDEX = 6;

    /**
     * WX Type index for FG (fog). Index validated from Legacy
     * retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_FOG_INDEX = 12;

    /**
     * WX Type index for Mixed Precip or RASN (rain and snow). Index validated
     * from Legacy build_daily_obs_weather.ec.
     */
    public static final int WX_MIXED_PRECIP_INDEX = 1;

    /**
     * WX Type index for GR (hail). Index validated from Legacy
     * retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_HAIL_INDEX = 7;

    /**
     * WX Type index for FG <= 1/4SM (dense/heavy fog, visibility less than 0.25
     * statute miles). Index validated from Legacy retrieve_daily_sum.ec and
     * build_daily_obs_weather.ec.
     */
    public static final int WX_FOG_QUARTER_SM_INDEX = 13;

    /**
     * WX Type index for +RA (heavy rain). Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_HEAVY_RAIN_INDEX = 2;

    /**
     * WX Type index for +SN (heavy snow). Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_HEAVY_SNOW_INDEX = 8;

    /**
     * WX Type index for HZ (haze). Index validated from Legacy
     * retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_HAZE_INDEX = 14;

    /**
     * WX Type index for rain. Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_RAIN_INDEX = 3;

    /**
     * WX Type index for SN (Snow). Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_SNOW_INDEX = 9;

    /**
     * WX Type index for BLSN (blowing snow). Index validated from Legacy
     * retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_BLOWING_SNOW_INDEX = 15;

    /**
     * WX Type index for -RA (light rain). Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_LIGHT_RAIN_INDEX = 4;

    /**
     * WX Type index for -SN (light snow). Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_LIGHT_SNOW_INDEX = 10;

    /**
     * WX Type index for SS (sandstorm or dust storm). Index validated from
     * Legacy retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_SAND_STORM_INDEX = 16;

    /**
     * WX Type index for FZRA (freezing rain). Index validated from Legacy
     * build_daily_obs_weather.ec.
     */
    public static final int WX_FREEZING_RAIN_INDEX = 5;

    /**
     * WX Type index for PL (ice pellets) or sleet. Index validated from Legacy
     * retrieve_daily_sum.ec and build_daily_obs_weather.ec.
     */
    public static final int WX_ICE_PELLETS_INDEX = 11;

    /**
     * WX Type index for FC (funnel cloud) or +FC (tornado/waterspout). Index
     * validated from Legacy retrieve_daily_sum.ec and
     * build_daily_obs_weather.ec.
     */
    public static final int WX_FUNNEL_CLOUD_INDEX = 17;

    /**
     * unique Informix station id
     */
    @DynamicSerializeElement
    private int informId;

    /**
     * mean max temperature, degrees F
     */
    @DynamicSerializeElement
    private int maxTemp;

    /**
     * time of maxTemp
     */
    @DynamicSerializeElement
    private ClimateTime maxTempTime;

    /**
     * mean min temperature, degrees F
     */
    @DynamicSerializeElement
    private int minTemp;

    /**
     * time of minTemp
     */
    @DynamicSerializeElement
    private ClimateTime minTempTime;

    /**
     * precipitation (inches)
     */
    @DynamicSerializeElement
    private float precip;

    /**
     * cumulative precip this month (in.)
     */
    @DynamicSerializeElement
    private float precipMonth;

    /**
     * cumulative precip this season or for each user-defined alternate season
     * (in.)
     */
    @DynamicSerializeElement
    private List<Float> precipSeasons;

    /**
     * total precip for year to date (in.)
     */
    @DynamicSerializeElement
    private float precipYear;

    /**
     * snowfall (inches)
     */
    @DynamicSerializeElement
    private float snowDay;

    /**
     * total snow for month to date (in.)
     */
    @DynamicSerializeElement
    private float snowMonth;

    /**
     * cumulative snow this season (in.)
     */
    @DynamicSerializeElement
    private List<Float> snowSeasons;

    /**
     * total snow for year to date (in.)
     */
    @DynamicSerializeElement
    private float snowYear;

    /**
     * snow cover on ground (in.)
     */
    @DynamicSerializeElement
    private float snowGround;

    /**
     * heating degree days
     */
    @DynamicSerializeElement
    private int numHeat;

    /**
     * cumulative # of heat days this month
     */
    @DynamicSerializeElement
    private int numHeatMonth;

    /**
     * cumulative # of heat days this season
     */
    @DynamicSerializeElement
    private int numHeatSeason;

    /**
     * cumulative heating days this year
     */
    @DynamicSerializeElement
    private int numHeatYear;

    /**
     * cooling degree days
     */
    @DynamicSerializeElement
    private int numCool;

    /**
     * cumulative # of cool days this month
     */
    @DynamicSerializeElement
    private int numCoolMonth;

    /**
     * cumulative # of cool days for season
     */
    @DynamicSerializeElement
    private int numCoolSeason;

    /**
     * cumulative cooling days this year
     */
    @DynamicSerializeElement
    private int numCoolYear;

    /**
     * average scalar wind speed
     */
    @DynamicSerializeElement
    private float avgWindSpeed;

    /**
     * resultant wind direction and speed
     */
    @DynamicSerializeElement
    private ClimateWind resultWind;

    @DynamicSerializeElement
    private double resultX;

    @DynamicSerializeElement
    private double resultY;

    @DynamicSerializeElement
    private int numWndObs;

    /**
     * speed and direction of the max wind
     */
    @DynamicSerializeElement
    private ClimateWind maxWind;

    /**
     * time of max wind
     */
    @DynamicSerializeElement
    private ClimateTime maxWindTime;

    /**
     * speed and direction of the max gust
     */
    @DynamicSerializeElement
    private ClimateWind maxGust;

    /**
     * time of max gust
     */
    @DynamicSerializeElement
    private ClimateTime maxGustTime;

    /**
     * minutes of sunshine
     */
    @DynamicSerializeElement
    private int minutesSun;

    /**
     * possible sunshine, %
     */
    @DynamicSerializeElement
    private int percentPossSun;

    /**
     * average sky cover
     */
    @DynamicSerializeElement
    private float skyCover;

    /**
     * hour of minimum relative humidity
     */
    @DynamicSerializeElement
    private int minRelHumidHour;

    /**
     * maximum relative humiditiy, units=%
     */
    @DynamicSerializeElement
    private int maxRelHumid;

    /**
     * minimum relative humidity, units=%
     */
    @DynamicSerializeElement
    private int minRelHumid;

    /**
     * hour of minimum relative humidity
     */
    @DynamicSerializeElement
    private int maxRelHumidHour;

    /**
     * average relative humidity, units=%
     */
    @DynamicSerializeElement
    private int meanRelHumid;

    /**
     * maximum sea level pressure
     */
    @DynamicSerializeElement
    private double maxSlp;

    /**
     * minimum sea level pressure
     */
    @DynamicSerializeElement
    private double minSlp;

    /**
     * number of weather types reported
     */
    @DynamicSerializeElement
    private int numWx;

    /**
     * observed daily weather
     */
    @DynamicSerializeElement
    private int[] wxType = new int[TOTAL_WX_TYPES];;

    /**
     * the data methods for each field
     */
    @DynamicSerializeElement
    private DailyDataMethod dataMethods;

    /**
     * Empty constructor.
     */
    public DailyClimateData() {
    }

    /**
     * @return the avgWindSpeed
     */
    public float getAvgWindSpeed() {
        return avgWindSpeed;
    }

    /**
     * @return the informId
     */
    public int getInformId() {
        return informId;
    }

    /**
     * @return the maxGust
     */
    public ClimateWind getMaxGust() {
        return maxGust;
    }

    /**
     * @return the maxGustTime
     */
    public ClimateTime getMaxGustTime() {
        return maxGustTime;
    }

    /**
     * @return the maxRelHumid
     */
    public int getMaxRelHumid() {
        return maxRelHumid;
    }

    /**
     * @return the maxSlp
     */
    public double getMaxSlp() {
        return maxSlp;
    }

    /**
     * @return the maxTemp
     */
    public int getMaxTemp() {
        return maxTemp;
    }

    /**
     * @return the maxWind
     */
    public ClimateWind getMaxWind() {
        return maxWind;
    }

    /**
     * @return the maxWindTime
     */
    public ClimateTime getMaxWindTime() {
        return maxWindTime;
    }

    /**
     * @return the meanRelHumid
     */
    public int getMeanRelHumid() {
        return meanRelHumid;
    }

    /**
     * @return the minRelHumid
     */
    public int getMinRelHumid() {
        return minRelHumid;
    }

    /**
     * @return the minSlp
     */
    public double getMinSlp() {
        return minSlp;
    }

    /**
     * @return the minutesSun
     */
    public int getMinutesSun() {
        return minutesSun;
    }

    /**
     * @return the minTemp
     */
    public int getMinTemp() {
        return minTemp;
    }

    /**
     * @return the numCool
     */
    public int getNumCool() {
        return numCool;
    }

    /**
     * @return the numCoolMonth
     */
    public int getNumCoolMonth() {
        return numCoolMonth;
    }

    /**
     * @return the numCoolSeason
     */
    public int getNumCoolSeason() {
        return numCoolSeason;
    }

    /**
     * @return the numCoolYear
     */
    public int getNumCoolYear() {
        return numCoolYear;
    }

    /**
     * @return the numHeat
     */
    public int getNumHeat() {
        return numHeat;
    }

    /**
     * @return the numHeatMonth
     */
    public int getNumHeatMonth() {
        return numHeatMonth;
    }

    /**
     * @return the numHeatSeason
     */
    public int getNumHeatSeason() {
        return numHeatSeason;
    }

    /**
     * @return the numHeatYear
     */
    public int getNumHeatYear() {
        return numHeatYear;
    }

    /**
     * @return the numWndObs
     */
    public int getNumWndObs() {
        return numWndObs;
    }

    /**
     * @return the numWx
     */
    public int getNumWx() {
        return numWx;
    }

    /**
     * @return the percentPossSun
     */
    public int getPercentPossSun() {
        return percentPossSun;
    }

    /**
     * @return the precip
     */
    public float getPrecip() {
        return precip;
    }

    /**
     * @return the precipMonth
     */
    public float getPrecipMonth() {
        return precipMonth;
    }

    /**
     * @return the precipSeason
     */
    public List<Float> getPrecipSeasons() {
        return precipSeasons;
    }

    /**
     * @return the precipYear
     */
    public float getPrecipYear() {
        return precipYear;
    }

    /**
     * @return the resultWind
     */
    public ClimateWind getResultWind() {
        return resultWind;
    }

    /**
     * @return the resultX
     */
    public double getResultX() {
        return resultX;
    }

    /**
     * @return the resultY
     */
    public double getResultY() {
        return resultY;
    }

    /**
     * @return the skyCover
     */
    public float getSkyCover() {
        return skyCover;
    }

    /**
     * @return the snowDay
     */
    public float getSnowDay() {
        return snowDay;
    }

    /**
     * @return the snowGround
     */
    public float getSnowGround() {
        return snowGround;
    }

    /**
     * @return the snowMonth
     */
    public float getSnowMonth() {
        return snowMonth;
    }

    /**
     * @return the snowSeason
     */
    public List<Float> getSnowSeasons() {
        return snowSeasons;
    }

    /**
     * @return the snowYear
     */
    public float getSnowYear() {
        return snowYear;
    }

    /**
     * @return the maxRelHumidHour
     */
    public int getMaxRelHumidHour() {
        return maxRelHumidHour;
    }

    /**
     * @return the maxTempTime
     */
    public ClimateTime getMaxTempTime() {
        return maxTempTime;
    }

    /**
     * @return the minRelHumidHour
     */
    public int getMinRelHumidHour() {
        return minRelHumidHour;
    }

    /**
     * @return the minTempTime
     */
    public ClimateTime getMinTempTime() {
        return minTempTime;
    }

    /**
     * @return the wxType. Applicable values are 0 (no event), 1 (event), or
     *         9999 (missing data, effectively same as no event).
     */
    public int[] getWxType() {
        return wxType;
    }

    /**
     * @return the dataMethods.
     */
    public DailyDataMethod getDataMethods() {
        return dataMethods;
    }

    /**
     * @param avgWindSpeed
     *            the avgWindSpeed to set
     */
    public void setAvgWindSpeed(float avgWindSpeed) {
        this.avgWindSpeed = avgWindSpeed;
    }

    public void setDataToMissing() {
        this.informId = -1 * ParameterFormatClimate.MISSING;
        this.maxTemp = ParameterFormatClimate.MISSING;
        this.minTemp = ParameterFormatClimate.MISSING;
        this.maxTempTime = ClimateTime.getMissingClimateTime();
        this.minTempTime = ClimateTime.getMissingClimateTime();
        this.precip = ParameterFormatClimate.MISSING_PRECIP;
        this.precipMonth = ParameterFormatClimate.MISSING_PRECIP;
        this.precipSeasons = new ArrayList<>();
        this.precipYear = ParameterFormatClimate.MISSING_PRECIP;
        this.snowDay = ParameterFormatClimate.MISSING_SNOW_VALUE;
        this.snowMonth = ParameterFormatClimate.MISSING_SNOW_VALUE;
        this.snowSeasons = new ArrayList<>();
        this.snowYear = ParameterFormatClimate.MISSING_SNOW_VALUE;
        this.snowGround = ParameterFormatClimate.MISSING_SNOW_VALUE;
        this.numHeat = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numHeatMonth = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numHeatSeason = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numHeatYear = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCool = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolMonth = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolSeason = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolYear = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.avgWindSpeed = ParameterFormatClimate.MISSING;
        this.resultWind = ClimateWind.getMissingClimateWind();
        resultX = ParameterFormatClimate.MISSING;
        resultY = ParameterFormatClimate.MISSING;

        this.maxWind = ClimateWind.getMissingClimateWind();
        this.maxGust = ClimateWind.getMissingClimateWind();
        this.maxWindTime = ClimateTime.getMissingClimateTime();
        this.maxGustTime = ClimateTime.getMissingClimateTime();

        numWndObs = ParameterFormatClimate.MISSING;

        this.minutesSun = ParameterFormatClimate.MISSING;
        this.percentPossSun = ParameterFormatClimate.MISSING;
        this.skyCover = ParameterFormatClimate.MISSING;
        this.maxRelHumid = ParameterFormatClimate.MISSING;
        this.minRelHumid = ParameterFormatClimate.MISSING;
        this.meanRelHumid = ParameterFormatClimate.MISSING;
        maxRelHumidHour = ParameterFormatClimate.MISSING_HOUR;
        minRelHumidHour = ParameterFormatClimate.MISSING_HOUR;
        this.maxSlp = ParameterFormatClimate.MISSING_SLP;
        this.minSlp = ParameterFormatClimate.MISSING_SLP;
        this.numWx = ParameterFormatClimate.MISSING;

        wxType = new int[TOTAL_WX_TYPES];
        for (int i = 0; i < wxType.length; i++) {
            wxType[i] = ParameterFormatClimate.MISSING;
        }

        dataMethods = DailyDataMethod.getMissingDailyDataMethod();
    }

    /**
     * @param informId
     *            the informId to set
     */
    public void setInformId(int informId) {
        this.informId = informId;
    }

    /**
     * @param maxGust
     *            the maxGust to set
     */
    public void setMaxGust(ClimateWind maxGust) {
        this.maxGust = maxGust;
    }

    /**
     * @param maxGustTime
     *            the maxGustTime to set
     */
    public void setMaxGustTime(ClimateTime maxGustTime) {
        this.maxGustTime = maxGustTime;
    }

    /**
     * @param maxRelHumid
     *            the maxRelHumid to set
     */
    public void setMaxRelHumid(int maxRelHumid) {
        this.maxRelHumid = maxRelHumid;
    }

    /**
     * @param maxSlp
     *            the maxSlp to set
     */
    public void setMaxSlp(double maxSlp) {
        this.maxSlp = maxSlp;
    }

    /**
     * @param maxTemp
     *            the maxTemp to set
     */
    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    /**
     * @param maxWind
     *            the maxWind to set
     */
    public void setMaxWind(ClimateWind maxWind) {
        this.maxWind = maxWind;
    }

    /**
     * @param maxWindTime
     *            the maxWindTime to set
     */
    public void setMaxWindTime(ClimateTime maxWindTime) {
        this.maxWindTime = maxWindTime;
    }

    /**
     * @param meanRelHumid
     *            the meanRelHumid to set
     */
    public void setMeanRelHumid(int meanRelHumid) {
        this.meanRelHumid = meanRelHumid;
    }

    /**
     * @param minRelHumid
     *            the minRelHumid to set
     */
    public void setMinRelHumid(int minRelHumid) {
        this.minRelHumid = minRelHumid;
    }

    /**
     * @param minSlp
     *            the minSlp to set
     */
    public void setMinSlp(double minSlp) {
        this.minSlp = minSlp;
    }

    /**
     * @param iMinutesSun
     *            the minutesSun to set
     */
    public void setMinutesSun(int iMinutesSun) {
        this.minutesSun = iMinutesSun;
    }

    /**
     * @param minTemp
     *            the minTemp to set
     */
    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    /**
     * @param numCool
     *            the numCool to set
     */
    public void setNumCool(int numCool) {
        this.numCool = numCool;
    }

    /**
     * @param numCoolMonth
     *            the numCoolMonth to set
     */
    public void setNumCoolMonth(int numCoolMonth) {
        this.numCoolMonth = numCoolMonth;
    }

    /**
     * @param numCoolSeason
     *            the numCoolSeason to set
     */
    public void setNumCoolSeason(int numCoolSeason) {
        this.numCoolSeason = numCoolSeason;
    }

    /**
     * @param numCoolYear
     *            the numCoolYear to set
     */
    public void setNumCoolYear(int numCoolYear) {
        this.numCoolYear = numCoolYear;
    }

    /**
     * @param numHeat
     *            the numHeat to set
     */
    public void setNumHeat(int numHeat) {
        this.numHeat = numHeat;
    }

    /**
     * @param numHeatMonth
     *            the numHeatMonth to set
     */
    public void setNumHeatMonth(int numHeatMonth) {
        this.numHeatMonth = numHeatMonth;
    }

    /**
     * @param numHeatSeason
     *            the numHeatSeason to set
     */
    public void setNumHeatSeason(int numHeatSeason) {
        this.numHeatSeason = numHeatSeason;
    }

    /**
     * @param numHeatYear
     *            the numHeatYear to set
     */
    public void setNumHeatYear(int numHeatYear) {
        this.numHeatYear = numHeatYear;
    }

    /**
     * @param numWndObs
     *            the numWndObs to set
     */
    public void setNumWndObs(int numWndObs) {
        this.numWndObs = numWndObs;
    }

    /**
     * @param numWx
     *            the numWx to set
     */
    public void setNumWx(int numWx) {
        this.numWx = numWx;
    }

    /**
     * @param iPercentPossSun
     *            the percentPossSun to set
     */
    public void setPercentPossSun(int iPercentPossSun) {
        this.percentPossSun = iPercentPossSun;
    }

    /**
     * @param precip
     *            the precip to set
     */
    public void setPrecip(float precip) {
        this.precip = precip;
    }

    /**
     * @param precipMonth
     *            the precipMonth to set
     */
    public void setPrecipMonth(float precipMonth) {
        this.precipMonth = precipMonth;
    }

    /**
     * @param precipSeason
     *            the precipSeason to set
     */
    public void setPrecipSeasons(List<Float> precipSeasons) {
        this.precipSeasons = precipSeasons;
    }

    /**
     * Set an element by index in the precipSeasons list.
     * 
     * @param index
     * @param value
     */
    public void setPrecipSeasons(int index, float value) {
        precipSeasons.set(index, value);
    }

    /**
     * @param precipYear
     *            the precipYear to set
     */
    public void setPrecipYear(float precipYear) {
        this.precipYear = precipYear;
    }

    /**
     * @param resultWind
     *            the resultWind to set
     */
    public void setResultWind(ClimateWind resultWind) {
        this.resultWind = resultWind;
    }

    /**
     * @param resultX
     *            the resultX to set
     */
    public void setResultX(double resultX) {
        this.resultX = resultX;
    }

    /**
     * @param resultY
     *            the resultY to set
     */
    public void setResultY(double resultY) {
        this.resultY = resultY;
    }

    /**
     * @param skyCover
     *            the skyCover to set
     */
    public void setSkyCover(float skyCover) {
        this.skyCover = skyCover;
    }

    /**
     * @param snowDay
     *            the snowDay to set
     */
    public void setSnowDay(float snowDay) {
        this.snowDay = snowDay;
    }

    /**
     * @param snowGround
     *            the snowGround to set
     */
    public void setSnowGround(float snowGround) {
        this.snowGround = snowGround;
    }

    /**
     * @param snowMonth
     *            the snowMonth to set
     */
    public void setSnowMonth(float snowMonth) {
        this.snowMonth = snowMonth;
    }

    /**
     * @param snowSeason
     *            the snowSeason to set
     */
    public void setSnowSeasons(List<Float> snowSeasons) {
        this.snowSeasons = snowSeasons;
    }

    /**
     * Set an element by index in the snowSeasons list.
     * 
     * @param index
     * @param value
     */
    public void setSnowSeasons(int index, float value) {
        snowSeasons.set(index, value);
    }

    /**
     * @param snowYear
     *            the snowYear to set
     */
    public void setSnowYear(float snowYear) {
        this.snowYear = snowYear;
    }

    /**
     * @param maxRelHumidHour
     *            the maxRelHumidHour to set
     */
    public void setMaxRelHumidHour(int maxRelHumidHour) {
        this.maxRelHumidHour = maxRelHumidHour;
    }

    /**
     * @param iMaxTempTime
     *            the maxTempTime to set
     */
    public void setMaxTempTime(ClimateTime iMaxTempTime) {
        this.maxTempTime = iMaxTempTime;
    }

    /**
     * @param minRelHumidHour
     *            the minRelHumidHour to set
     */
    public void setMinRelHumidHour(int minRelHumidHour) {
        this.minRelHumidHour = minRelHumidHour;
    }

    /**
     * @param iMinTempTime
     *            the minTempTime to set
     */
    public void setMinTempTime(ClimateTime iMinTempTime) {
        this.minTempTime = iMinTempTime;
    }

    /**
     * @param wxType
     *            the wxType to set. Applicable values are 0 and 1.
     */
    public void setWxType(int[] wxType) {
        this.wxType = wxType;
    }

    /**
     * Set a value in wxType array.
     * 
     * @param index
     *            the index to set
     * @param value
     *            the value set it to
     */
    public void setWxType(int index, int value) {
        wxType[index] = value;
    }

    /**
     * @param dataMethods
     *            the dataMethods to set
     */
    public void setDataMethods(DailyDataMethod dataMethods) {
        this.dataMethods = dataMethods;
    }

    public static DailyClimateData getMissingDailyClimateData() {
        DailyClimateData dailyClimateData = new DailyClimateData();
        dailyClimateData.setDataToMissing();
        return dailyClimateData;
    }
}

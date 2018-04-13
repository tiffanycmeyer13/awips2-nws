/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.parameter;

/**
 * Strings for each weather type (wx) description. Used in Climate Formatter.
 * Migrated from PARAMETER_format_climate.h.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * FEB 06 2017  21099      wpaintsil     Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */
public enum WeatherStrings {
    /**
     * Thunderstorm string. WX index 0
     * (DailyClimateData.WX_THUNDER_STORM_INDEX).
     */
    THUNDERSTORM("thunderstorm"),
    /**
     * Mixed precip string. WX index 1
     * (DailyClimateData.WX_THUNDER_STORM_INDEX).
     */
    MIXED_PRECIP("mixed precip"),
    /**
     * Heavy rain string. WX index 2 (DailyClimateData.WX_HEAVY_RAIN_INDEX).
     */
    HEAVY_RAIN("heavy rain"),
    /**
     * Rain string. WX index 3 (DailyClimateData.WX_RAIN_INDEX).
     */
    RAIN("rain"),
    /**
     * Light rain string. WX index 4 (DailyClimateData.WX_LIGHT_RAIN_INDEX).
     */
    LIGHT_RAIN("light rain"),
    /**
     * Freezing rain string. WX index 5
     * (DailyClimateData.WX_FREEZING_RAIN_INDEX).
     */
    FREEZING_RAIN("freezing rain"),
    /**
     * Light freezing rain string. WX index 6
     * (DailyClimateData.WX_LIGHT_FREEZING_RAIN_INDEX).
     */
    LT_FREEZING_RAIN("lt freezing rain"),
    /**
     * Hail string. WX index 7 (DailyClimateData.WX_HAIL_INDEX).
     */
    HAIL("hail"),
    /**
     * Heavy snow string. WX index 8 (DailyClimateData.WX_HEAVY_SNOW_INDEX).
     */
    HEAVY_SNOW("heavy snow"),
    /**
     * Snow string. WX index 9 (DailyClimateData.WX_SNOW_INDEX).
     */
    SNOW("snow"),
    /**
     * Light snow string. WX index 10 (DailyClimateData.WX_LIGHT_SNOW_INDEX).
     */
    LIGHT_SNOW("light snow"),
    /**
     * Sleet string. WX index 11 (DailyClimateData.WX_ICE_PELLETS_INDEX).
     */
    SLEET("sleet"),
    /**
     * Fog string. WX index 12 (DailyClimateData.WX_FOG_INDEX).
     */
    FOG("fog"),
    /**
     * Fog w/visibility <= 1/4 string. WX index 13
     * (DailyClimateData.WX_FOG_QUARTER_SM_INDEX).
     */
    FOG_14("fog w/visibility <= 1/4 mile"),
    /**
     * Haze string. WX index 14 (DailyClimateData.WX_HAZE_INDEX).
     */
    HAZE("haze"),
    /**
     * Blowing snow string. WX index 15 (DailyClimateData.WX_BLOWING_SNOW_INDEX)
     */
    BLOWING_SNOW("blowing snow"),
    /**
     * Sandstorm string. WX index 16 (DailyClimateData.WX_SAND_STORM_INDEX).
     */
    SANDSTORM("sandstorm"),
    /**
     * Tornado string. WX index 17 (DailyClimateData.WX_FUNNEL_CLOUD_INDEX).
     */
    TORNADO("tornado");

    /**
     * The string for the weather type.
     */
    private String weatherString;

    /**
     * Private constructor.
     * 
     * @param weatherString
     */
    private WeatherStrings(String weatherString) {
        this.weatherString = weatherString;
    }

    /**
     * @return the string for the weather type
     */
    public String getString() {
        return weatherString;
    }

    /**
     * The enum constants are arranged in the order of their corresponding wx
     * index so that this method will return the appropriate string for the
     * appropriate index.
     * 
     * @param index
     * @return
     */
    public static String getString(int index) {
        return WeatherStrings.values()[index].getString();

    }

}
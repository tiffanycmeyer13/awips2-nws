/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.listener.impl;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterBounds;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * This class holds common Climate Display module listeners.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 19 AUG 2016  20414      amoore      Initial creation.
 * 10 NOV 2016  20636      wpaintsil   Use TextDoubleWithTListener for appropriate fields.
 * 27 DEC 2016  22450      amoore      Updating listeners.
 * 28 DEC 2016  22780      amoore      Better naming and documentation of listeners. Snow
 *                                     precision correction.
 * 28 DEC 2016  22781      amoore      Wind speed precision correction.
 * 28 DEC 2016  22784      amoore      Sky cover listener.
 * 22 MAY 2017  33104      amoore      Fix wind direction precision listener.
 * </pre>
 * 
 * @author amoore
 */
public class ClimateTextListeners {

    /**
     * Precip text listener. For any precip.
     */
    private final TextDoubleWithTListener precipListener = new TextDoubleWithTListener(
            (double) ParameterBounds.PRECIP_LOWER_BOUND,
            (double) ParameterBounds.PRECIP_UPPER_BOUND,
            (double) ParameterFormatClimate.MISSING_PRECIP, 2);

    /**
     * Snow double text listener. For snow fall, or any average snow.
     */
    private final TextDoubleWithTListener snowFallListener = new TextDoubleWithTListener(
            (double) ParameterBounds.SNOW_LOWER_BOUND,
            (double) ParameterBounds.SNOW_UPPER_BOUND,
            (double) ParameterFormatClimate.MISSING_SNOW, 1);

    /**
     * Snow int text listener. For snow depth (but not a snow depth average).
     */
    private final TextIntListener snowDepthListener = new TextIntListener(
            (int) ParameterBounds.SNOW_LOWER_BOUND,
            (int) ParameterBounds.SNOW_UPPER_BOUND,
            (int) ParameterFormatClimate.MISSING_SNOW);

    /**
     * SLP text listener.
     */
    private final TextDoubleListener slpListener = new TextDoubleListener(
            (double) ParameterBounds.PRESS_LOWER_BOUND,
            (double) ParameterBounds.PRESS_UPPER_BOUND,
            ParameterFormatClimate.MISSING_SLP);

    /**
     * Temperature int text listener. For any temperature not related to period
     * averages.
     */
    private final TextIntListener tempIntListener = new TextIntListener(
            ParameterBounds.TEMP_LOWER_BOUND, ParameterBounds.TEMP_UPPER_BOUND,
            (int) ParameterFormatClimate.MISSING);

    /**
     * Temperature double text listener. For any temperature related to period
     * averages.
     */
    private final TextDoubleListener tempPeriodAverageListener = new TextDoubleListener(
            (double) ParameterBounds.TEMP_LOWER_BOUND,
            (double) ParameterBounds.TEMP_UPPER_BOUND,
            (double) ParameterFormatClimate.MISSING, 1);

    /**
     * Relative humidity text listener.
     */
    private final TextIntListener relHumListener = new TextIntListener(
            ParameterBounds.RH_LOWER_BOUND, ParameterBounds.RH_UPPER_BOUND,
            (int) ParameterFormatClimate.MISSING);

    /**
     * Wind direction text listener. Precision is 10s of degrees, but should
     * allow singles.
     */
    private final TextIntListener windDirListener = new TextIntListener(
            ParameterBounds.WIND_DIR_LOWER_BOUND,
            ParameterBounds.WIND_DIR_UPPER_BOUND,
            (int) ParameterFormatClimate.MISSING);

    /**
     * Wind speed text listener.
     */
    private final TextDoubleListener windSpdListener = new TextDoubleListener(
            (double) ParameterBounds.WIND_SPD_LOWER_BOUND,
            (double) ParameterBounds.WIND_SPD_UPPER_BOUND,
            (double) ParameterFormatClimate.MISSING_SPEED, 1);

    /**
     * Degree days text listener.
     */
    private final TextIntListener degreeDaysListener = new TextIntListener(
            ParameterBounds.DEGREE_DAY_LOWER, ParameterBounds.DEGREE_DAY_UPPER,
            ParameterFormatClimate.MISSING_DEGREE_DAY);

    /**
     * Percent text listener.
     */
    private final TextIntListener percentListener = new TextIntListener(
            ParameterBounds.PERCENT_LOWER_BOUND,
            ParameterBounds.PERCENT_UPPER_BOUND,
            (int) ParameterFormatClimate.MISSING);

    /**
     * Sky cover down to hundredths text listener. For any sky cover.
     */
    private final TextDoubleListener skyCoverListener = new TextDoubleListener(
            0d, 1d, (double) ParameterFormatClimate.MISSING, 2);

    /**
     * Default integer listener. Uses 0 as min and 9999 as max/default.
     */
    private final TextIntListener defaultIntListener = new TextIntListener(
            (int) ParameterFormatClimate.MISSING);

    /**
     * Default double listener. Uses 0 as min and 9999 as max/default.
     */
    private final TextDoubleListener defaultDoubleListener = new TextDoubleListener(
            (double) ParameterFormatClimate.MISSING);

    /**
     * @return the precipListener. For any precip.
     */
    public TextDoubleWithTListener getPrecipListener() {
        return precipListener;
    }

    /**
     * @return the snowfall listener. For snow fall, or any average snow.
     */
    public TextDoubleWithTListener getSnowFallListener() {
        return snowFallListener;
    }

    /**
     * @return the snow depth listener. For snow depth (but not a snow depth
     *         average).
     */
    public TextIntListener getSnowDepthListener() {
        return snowDepthListener;
    }

    /**
     * @return the slpListener
     */
    public TextDoubleListener getSlpListener() {
        return slpListener;
    }

    /**
     * @return the tempIntListener. For any temperature not related to period
     *         averages.
     */
    public TextIntListener getTempIntListener() {
        return tempIntListener;
    }

    /**
     * @return the temperature period average listener. For any temperature
     *         related to period averages.
     */
    public TextDoubleListener getTempPeriodAverageListener() {
        return tempPeriodAverageListener;
    }

    /**
     * @return the relHumListener
     */
    public TextIntListener getRelHumListener() {
        return relHumListener;
    }

    /**
     * @return the windDirListener. Precision is 10s of degrees, but should
     *         allow singles.
     */
    public TextIntListener getWindDirListener() {
        return windDirListener;
    }

    /**
     * @return the windSpdListener
     */
    public TextDoubleListener getWindSpdListener() {
        return windSpdListener;
    }

    /**
     * @return the degreeDaysListener
     */
    public TextIntListener getDegreeDaysListener() {
        return degreeDaysListener;
    }

    /**
     * @return the percentListener
     */
    public TextIntListener getPercentListener() {
        return percentListener;
    }

    /**
     * @return the defaultIntListener. Uses 0 as min and 9999 as max/default.
     */
    public TextIntListener getDefaultIntListener() {
        return defaultIntListener;
    }

    /**
     * @return the defaultDoubleListener. Uses 0 as min and 9999 as max/default.
     */
    public TextDoubleListener getDefaultDoubleListener() {
        return defaultDoubleListener;
    }

    /**
     * @return the skyCoverListener. For any sky cover.
     */
    public TextDoubleListener getSkyCoverListener() {
        return skyCoverListener;
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Converted from the daily_data_method structure
 * (rehost-adapt/adapt/climate/include/TYPE_daily_data_method.h). Values should
 * reference appropriate constants from QCValues.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2015            xzhang     Initial creation
 * Jul 22, 2016 20712	   wpaintsil  Serialization
 * 23 SEP 2016  21378      amoore     Added missing instance methods.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class DailyDataMethod {
    @DynamicSerializeElement
    private int maxTempQc;

    @DynamicSerializeElement
    private int minTempQc;

    @DynamicSerializeElement
    private int precipQc;

    @DynamicSerializeElement
    private int snowQc;

    @DynamicSerializeElement
    private int depthQc;

    @DynamicSerializeElement
    private int maxWindQc;

    @DynamicSerializeElement
    private int maxGustQc;

    @DynamicSerializeElement
    private int avgWindQc;

    @DynamicSerializeElement
    private int skyCoverQc;

    @DynamicSerializeElement
    private int minSunQc;

    @DynamicSerializeElement
    private int possSunQc;

    @DynamicSerializeElement
    private int weatherQc;

    /**
     * @return the avgWindQc
     */
    public int getAvgWindQc() {
        return avgWindQc;
    }

    /**
     * @return the depthQc
     */
    public int getDepthQc() {
        return depthQc;
    }

    /**
     * @return the maxGustQc
     */
    public int getMaxGustQc() {
        return maxGustQc;
    }

    /**
     * @return the maxTempQc
     */
    public int getMaxTempQc() {
        return maxTempQc;
    }

    /**
     * @return the maxWindQc
     */
    public int getMaxWindQc() {
        return maxWindQc;
    }

    /**
     * @return the minSunQc
     */
    public int getMinSunQc() {
        return minSunQc;
    }

    /**
     * @return the minTempQc
     */
    public int getMinTempQc() {
        return minTempQc;
    }

    /**
     * @return the possSunQc
     */
    public int getPossSunQc() {
        return possSunQc;
    }

    /**
     * @return the precipQc
     */
    public int getPrecipQc() {
        return precipQc;
    }

    /**
     * @return the skyCoverQc
     */
    public int getSkyCoverQc() {
        return skyCoverQc;
    }

    /**
     * @return the snowQc
     */
    public int getSnowQc() {
        return snowQc;
    }

    /**
     * @return the weatherQc
     */
    public int getWeatherQc() {
        return weatherQc;
    }

    /**
     * @param avgWindQc
     *            the avgWindQc to set
     */
    public void setAvgWindQc(int avgWindQc) {
        this.avgWindQc = avgWindQc;
    }

    /**
     * @param depthQc
     *            the depthQc to set
     */
    public void setDepthQc(int depthQc) {
        this.depthQc = depthQc;
    }

    /**
     * @param maxGustQc
     *            the maxGustQc to set
     */
    public void setMaxGustQc(int maxGustQc) {
        this.maxGustQc = maxGustQc;
    }

    /**
     * @param maxTempQc
     *            the maxTempQc to set
     */
    public void setMaxTempQc(int maxTempQc) {
        this.maxTempQc = maxTempQc;
    }

    /**
     * @param maxWindQc
     *            the maxWindQc to set
     */
    public void setMaxWindQc(int maxWindQc) {
        this.maxWindQc = maxWindQc;
    }

    /**
     * @param minSunQc
     *            the minSunQc to set
     */
    public void setMinSunQc(int minSunQc) {
        this.minSunQc = minSunQc;
    }

    /**
     * @param minTempQc
     *            the minTempQc to set
     */
    public void setMinTempQc(int minTempQc) {
        this.minTempQc = minTempQc;
    }

    /**
     * @param possSunQc
     *            the possSunQc to set
     */
    public void setPossSunQc(int possSunQc) {
        this.possSunQc = possSunQc;
    }

    /**
     * @param precipQc
     *            the precipQc to set
     */
    public void setPrecipQc(int precipQc) {
        this.precipQc = precipQc;
    }

    /**
     * @param skyCoverQc
     *            the skyCoverQc to set
     */
    public void setSkyCoverQc(int skyCoverQc) {
        this.skyCoverQc = skyCoverQc;
    }

    /**
     * @param snowQc
     *            the snowQc to set
     */
    public void setSnowQc(int snowQc) {
        this.snowQc = snowQc;
    }

    /**
     * @param weatherQc
     *            the weatherQc to set
     */
    public void setWeatherQc(int weatherQc) {
        this.weatherQc = weatherQc;
    }

    /**
     * Set data to missing values.
     */
    public void setDataToMissing() {
        maxTempQc = ParameterFormatClimate.MISSING;
        minTempQc = ParameterFormatClimate.MISSING;
        precipQc = ParameterFormatClimate.MISSING;
        snowQc = ParameterFormatClimate.MISSING;
        depthQc = ParameterFormatClimate.MISSING;
        maxWindQc = ParameterFormatClimate.MISSING;
        maxGustQc = ParameterFormatClimate.MISSING;
        avgWindQc = ParameterFormatClimate.MISSING;
        skyCoverQc = ParameterFormatClimate.MISSING;
        minSunQc = ParameterFormatClimate.MISSING;
        possSunQc = ParameterFormatClimate.MISSING;
        weatherQc = ParameterFormatClimate.MISSING;
    }

    /**
     * @return an instance with all missing data.
     */
    public static DailyDataMethod getMissingDailyDataMethod() {
        DailyDataMethod dailyDataMethod = new DailyDataMethod();
        dailyDataMethod.setDataToMissing();
        return dailyDataMethod;
    }

}

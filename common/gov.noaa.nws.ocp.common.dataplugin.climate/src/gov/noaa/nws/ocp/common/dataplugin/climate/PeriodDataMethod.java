/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Converted from: /include/TYPE_period_data_method.h. Values should reference
 * appropriate constants from QCValues.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 22, 2016            xzhang      Initial creation
 * Jul 22, 2016 20712	   wpaintsil   Serialization
 * 21 SEP 2016  22395      amoore      Added missing data methods.
 * 14 DEC 2016  27015      amoore      Added copy constructor.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class PeriodDataMethod {
    /**
     * 
     * max temp qc flag
     */
    @DynamicSerializeElement
    private int maxTempQc;

    /**
     * 
     * average max temp qc flag
     */
    @DynamicSerializeElement
    private int avgMaxTempQc;

    /**
     * number of days with max temp >90 qc
     */
    @DynamicSerializeElement
    private int maxTempGE90Qc;

    /**
     * number of days with max temp <32 qc
     */
    @DynamicSerializeElement
    private int maxTempLE32Qc;

    /**
     * min temp qc flag
     */
    @DynamicSerializeElement
    private int minTempQc;

    /**
     * average min temp qc flag
     */
    @DynamicSerializeElement
    private int avgMinTempQc;

    /**
     * number of days with min temp <32 qc
     */
    @DynamicSerializeElement
    private int minLE32Qc;

    /**
     * number of days with min temp <0 qc
     */
    @DynamicSerializeElement
    private int minLE0Qc;

    /**
     * mean temp qc flag
     */
    @DynamicSerializeElement
    private int meanTempQc;

    /**
     * precip total qc flag
     */
    @DynamicSerializeElement
    private int precipQc;

    /**
     * 24-hour max precip qc flag
     */
    @DynamicSerializeElement
    private int precip24hrMaxQc;

    /**
     * number of days with precip >0.01 qc
     */
    @DynamicSerializeElement
    private int precipGE01Qc;

    /**
     * number of days with precip >0.10 qc
     */
    @DynamicSerializeElement
    private int precipGE10Qc;

    /**
     * number of days with precip >0.50 qc
     */
    @DynamicSerializeElement
    private int precipGE50Qc;

    /**
     * number of days with precip >1.00 qc
     */
    @DynamicSerializeElement
    private int precipGE100Qc;

    /**
     * 24-hour max snow qc flag
     */
    @DynamicSerializeElement
    private int snow24hrMaxQc;

    /**
     * max snow depth qc flag
     */
    @DynamicSerializeElement
    private int maxDepthQc;

    /**
     * HDD qc flag
     */
    @DynamicSerializeElement
    private int heatQc;

    /**
     * CDD qc flag
     */
    @DynamicSerializeElement
    private int coolQc;

    /**
     * percent possible sunshine qc flag
     */
    @DynamicSerializeElement
    private int possSunQc;

    /**
     * number of fair days qc flag
     */
    @DynamicSerializeElement
    private int fairDaysQc;

    /**
     * number of partly cloudy days qc flag
     */
    @DynamicSerializeElement
    private int pcDaysQc;

    /**
     * number of cloudy days qc flag
     */
    @DynamicSerializeElement
    private int cloudyDaysQc;

    /**
     * Empty constructor.
     */
    public PeriodDataMethod() {
    }

    /**
     * Copy constructor.
     * 
     * @param other
     */
    public PeriodDataMethod(PeriodDataMethod other) {
        maxTempQc = other.getMaxTempQc();
        avgMaxTempQc = other.getAvgMaxTempQc();
        maxTempGE90Qc = other.getMaxTempGE90Qc();
        maxTempLE32Qc = other.getMaxTempLE32Qc();
        minTempQc = other.getMinTempQc();
        avgMinTempQc = other.getAvgMinTempQc();
        minLE32Qc = other.getMinLE32Qc();
        minLE0Qc = other.getMinLE0Qc();
        meanTempQc = other.getMeanTempQc();
        precipQc = other.getPrecipQc();
        precip24hrMaxQc = other.getPrecip24hrMaxQc();
        precipGE01Qc = other.getPrecipGE01Qc();
        precipGE10Qc = other.getPrecipGE10Qc();
        precipGE50Qc = other.getPrecipGE50Qc();
        precipGE100Qc = other.getPrecipGE100Qc();
        snow24hrMaxQc = other.getSnow24hrMaxQc();
        maxDepthQc = other.getMaxDepthQc();
        heatQc = other.getHeatQc();
        coolQc = other.getCoolQc();
        possSunQc = other.getPossSunQc();
        fairDaysQc = other.getFairDaysQc();
        pcDaysQc = other.getPcDaysQc();
        cloudyDaysQc = other.getCloudyDaysQc();
    }

    public int getMaxTempQc() {
        return maxTempQc;
    }

    public void setMaxTempQc(int maxTempQc) {
        this.maxTempQc = maxTempQc;
    }

    public int getAvgMaxTempQc() {
        return avgMaxTempQc;
    }

    public void setAvgMaxTempQc(int avgMaxTempQc) {
        this.avgMaxTempQc = avgMaxTempQc;
    }

    public int getMaxTempGE90Qc() {
        return maxTempGE90Qc;
    }

    public void setMaxTempGE90Qc(int maxTempGE90Qc) {
        this.maxTempGE90Qc = maxTempGE90Qc;
    }

    public int getMaxTempLE32Qc() {
        return maxTempLE32Qc;
    }

    public void setMaxTempLE32Qc(int maxTempLE32Qc) {
        this.maxTempLE32Qc = maxTempLE32Qc;
    }

    public int getMinTempQc() {
        return minTempQc;
    }

    public void setMinTempQc(int minTempQc) {
        this.minTempQc = minTempQc;
    }

    public int getAvgMinTempQc() {
        return avgMinTempQc;
    }

    public void setAvgMinTempQc(int avgMinTempQc) {
        this.avgMinTempQc = avgMinTempQc;
    }

    public int getMinLE32Qc() {
        return minLE32Qc;
    }

    public void setMinLE32Qc(int minLE32Qc) {
        this.minLE32Qc = minLE32Qc;
    }

    public int getMinLE0Qc() {
        return minLE0Qc;
    }

    public void setMinLE0Qc(int minLE0Qc) {
        this.minLE0Qc = minLE0Qc;
    }

    public int getMeanTempQc() {
        return meanTempQc;
    }

    public void setMeanTempQc(int meanTempQc) {
        this.meanTempQc = meanTempQc;
    }

    public int getPrecipQc() {
        return precipQc;
    }

    public void setPrecipQc(int precipQc) {
        this.precipQc = precipQc;
    }

    public int getPrecip24hrMaxQc() {
        return precip24hrMaxQc;
    }

    public void setPrecip24hrMaxQc(int precip24hrMaxQc) {
        this.precip24hrMaxQc = precip24hrMaxQc;
    }

    public int getPrecipGE01Qc() {
        return precipGE01Qc;
    }

    public void setPrecipGE01Qc(int precipGE01Qc) {
        this.precipGE01Qc = precipGE01Qc;
    }

    public int getPrecipGE10Qc() {
        return precipGE10Qc;
    }

    public void setPrecipGE10Qc(int precipGE10Qc) {
        this.precipGE10Qc = precipGE10Qc;
    }

    public int getPrecipGE50Qc() {
        return precipGE50Qc;
    }

    public void setPrecipGE50Qc(int precipGE50Qc) {
        this.precipGE50Qc = precipGE50Qc;
    }

    public int getPrecipGE100Qc() {
        return precipGE100Qc;
    }

    public void setPrecipGE100Qc(int precipGE100Qc) {
        this.precipGE100Qc = precipGE100Qc;
    }

    public int getSnow24hrMaxQc() {
        return snow24hrMaxQc;
    }

    public void setSnow24hrMaxQc(int snow24hrMaxQc) {
        this.snow24hrMaxQc = snow24hrMaxQc;
    }

    public int getMaxDepthQc() {
        return maxDepthQc;
    }

    public void setMaxDepthQc(int maxDepthQc) {
        this.maxDepthQc = maxDepthQc;
    }

    public int getHeatQc() {
        return heatQc;
    }

    public void setHeatQc(int heatQc) {
        this.heatQc = heatQc;
    }

    public int getCoolQc() {
        return coolQc;
    }

    public void setCoolQc(int coolQc) {
        this.coolQc = coolQc;
    }

    public int getPossSunQc() {
        return possSunQc;
    }

    public void setPossSunQc(int possSunQc) {
        this.possSunQc = possSunQc;
    }

    public int getFairDaysQc() {
        return fairDaysQc;
    }

    public void setFairDaysQc(int fairDaysQc) {
        this.fairDaysQc = fairDaysQc;
    }

    public int getPcDaysQc() {
        return pcDaysQc;
    }

    public void setPcDaysQc(int pcDaysQc) {
        this.pcDaysQc = pcDaysQc;
    }

    public int getCloudyDaysQc() {
        return cloudyDaysQc;
    }

    public void setCloudyDaysQc(int cloudyDaysQc) {
        this.cloudyDaysQc = cloudyDaysQc;
    }

    /**
     * Set data to missing values.
     */
    public void setDataToMissing() {
        maxTempQc = ParameterFormatClimate.MISSING;
        avgMaxTempQc = ParameterFormatClimate.MISSING;
        maxTempGE90Qc = ParameterFormatClimate.MISSING;
        maxTempLE32Qc = ParameterFormatClimate.MISSING;
        minTempQc = ParameterFormatClimate.MISSING;
        avgMinTempQc = ParameterFormatClimate.MISSING;
        minLE32Qc = ParameterFormatClimate.MISSING;
        minLE0Qc = ParameterFormatClimate.MISSING;
        meanTempQc = ParameterFormatClimate.MISSING;
        precipQc = ParameterFormatClimate.MISSING;
        precip24hrMaxQc = ParameterFormatClimate.MISSING;
        precipGE01Qc = ParameterFormatClimate.MISSING;
        precipGE10Qc = ParameterFormatClimate.MISSING;
        precipGE50Qc = ParameterFormatClimate.MISSING;
        precipGE100Qc = ParameterFormatClimate.MISSING;
        snow24hrMaxQc = ParameterFormatClimate.MISSING;
        maxDepthQc = ParameterFormatClimate.MISSING;
        heatQc = ParameterFormatClimate.MISSING;
        coolQc = ParameterFormatClimate.MISSING;
        possSunQc = ParameterFormatClimate.MISSING;
        fairDaysQc = ParameterFormatClimate.MISSING;
        pcDaysQc = ParameterFormatClimate.MISSING;
        cloudyDaysQc = ParameterFormatClimate.MISSING;
    }

    /**
     * @return instance full of missing data values.
     */
    public static PeriodDataMethod getMissingPeriodDataMethod() {
        PeriodDataMethod periodDataMethod = new PeriodDataMethod();
        periodDataMethod.setDataToMissing();
        return periodDataMethod;
    }
}

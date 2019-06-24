/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Converted from the climate_record_day
 * structure(rehost-adapt/adapt/climate/include/TYPE_climate_record_day.h).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2015            xzhang     Initial creation
 * Jul 22, 2016 20712	   wpaintsil  Serialization
 * 20 SEP 2016  21378      amoore     Added method to get missing value instance.
 * 01 MAY 2018  DR17116    wpaintsil  Accommodate multiple alternate snow/precip seasons.
 * 09 APR 2019  DR21228    wpaintsil  Unsorted record years results in 'MM' value 
 *                                    appearing under year column.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateRecordDay {
    /**
     * unique Informix id for this station
     */
    @DynamicSerializeElement
    private long informId;

    /**
     * max temperature for a given date (F)
     */
    @DynamicSerializeElement
    private short maxTempMean;

    /**
     * min temperature for a given date (F)
     */
    @DynamicSerializeElement
    private short minTempMean;

    /**
     * record maxTemp (F)
     */
    @DynamicSerializeElement
    private short maxTempRecord;

    /**
     * record minTemp (F)
     */
    @DynamicSerializeElement
    private short minTempRecord;

    /**
     * year(s) record maxTemp was observed
     */
    @DynamicSerializeElement
    private int[] maxTempYear = new int[3];

    /**
     * year(s) record minTemp was observed
     */
    @DynamicSerializeElement
    private int[] minTempYear = new int[3];

    /**
     * normal mean temperature (F)
     */
    @DynamicSerializeElement
    private float meanTemp;

    /**
     * average precip (inches)
     */
    @DynamicSerializeElement
    private float precipMean;

    /**
     * mean accumulated monthly precip
     */
    @DynamicSerializeElement
    private float precipMonthMean;

    /**
     * mean accumulated seasonal precip
     */
    @DynamicSerializeElement
    private List<Float> precipSeasonMean;

    /**
     * mean accumulated yearly precip
     */
    @DynamicSerializeElement
    private float precipYearMean;

    /**
     * max. precip for given date (inches)
     */
    @DynamicSerializeElement
    private float precipDayRecord;

    /**
     * precip. record for month
     */
    @DynamicSerializeElement
    private float precipMonthRecord;

    /**
     * precip. record for season
     */
    @DynamicSerializeElement
    private float precipSeasonRecord;

    /**
     * precip. record for year
     */
    @DynamicSerializeElement
    private float precipYearRecord;

    /**
     * year(s) max precip was observed
     */
    @DynamicSerializeElement
    private int[] precipDayRecordYear = new int[3];

    /**
     * years monthly record observed
     */
    @DynamicSerializeElement
    private int[] precipMonthRecordYear = new int[3];

    /**
     * years seasonal record observed
     */
    @DynamicSerializeElement
    private int[] precipSeasonRecordYear = new int[3];

    /**
     * years yearly record observed
     */
    @DynamicSerializeElement
    private int[] precipYearRecordYear = new int[3];

    /**
     * average daily snowfall
     */
    @DynamicSerializeElement
    private float snowDayMean;

    /**
     * mean accumulated monthly snowfall
     */
    @DynamicSerializeElement
    private float snowMonthMean;

    /**
     * mean accumulated seasonal snowfall
     */
    @DynamicSerializeElement
    private List<Float> snowSeasonMean;

    /**
     * mean accumulated yearly snowfall
     */
    @DynamicSerializeElement
    private float snowYearMean;

    /**
     * maximum snow in inches
     */
    @DynamicSerializeElement
    private float snowDayRecord;

    /**
     * snow record for month
     */
    @DynamicSerializeElement
    private float snowMonthRecord;

    /**
     * snow record for season
     */
    @DynamicSerializeElement
    private float snowSeasonRecord;

    /**
     * snow record for year
     */
    @DynamicSerializeElement
    private float snowYearRecord;

    /**
     * year(s) the maximum snow was observed
     */
    @DynamicSerializeElement
    private int[] snowDayRecordYear = new int[3];

    /**
     * years monthly record observed
     */
    @DynamicSerializeElement
    private int[] snowMonthRecordYear = new int[3];

    /**
     * years season record observed
     */
    @DynamicSerializeElement
    private int[] snowSeasonRecordYear = new int[3];

    /**
     * years yearly record observed
     */
    @DynamicSerializeElement
    private int[] snowYearRecordYear = new int[3];

    /**
     * mean heating degree days this date
     */
    @DynamicSerializeElement
    private int numHeatMean;

    /**
     * mean monthly accumulated heating days
     */
    @DynamicSerializeElement
    private int numHeatMonth;

    /**
     * mean seasonal accumulated heating days
     */
    @DynamicSerializeElement
    private int numHeatSeason;

    /**
     * mean yearly accumulated heating days
     */
    @DynamicSerializeElement
    private int numHeatYear;

    /**
     * mean cooling degree days this date
     */
    @DynamicSerializeElement
    private int numCoolMean;

    /**
     * mean monthly accumulated cooling days
     */
    @DynamicSerializeElement
    private int numCoolMonth;

    /**
     * mean seasonal accumulated cooling days
     */
    @DynamicSerializeElement
    private int numCoolSeason;

    /**
     * mean yearly accumulated cooling days
     */
    @DynamicSerializeElement
    private int numCoolYear;

    /**
     * @return the informId
     */
    public long getInformId() {
        return informId;
    }

    /**
     * @param informId
     *            the informId to set
     */
    public void setInformId(long informId) {
        this.informId = informId;
    }

    /**
     * @return the maxTempMean
     */
    public short getMaxTempMean() {
        return maxTempMean;
    }

    /**
     * @param maxTempMean
     *            the maxTempMean to set
     */
    public void setMaxTempMean(short maxTempMean) {
        this.maxTempMean = maxTempMean;
    }

    /**
     * @return the minTempMean
     */
    public short getMinTempMean() {
        return minTempMean;
    }

    /**
     * @param minTempMean
     *            the minTempMean to set
     */
    public void setMinTempMean(short minTempMean) {
        this.minTempMean = minTempMean;
    }

    /**
     * @return the maxTempRecord
     */
    public short getMaxTempRecord() {
        return maxTempRecord;
    }

    /**
     * @param maxTempRecord
     *            the maxTempRecord to set
     */
    public void setMaxTempRecord(short maxTempRecord) {
        this.maxTempRecord = maxTempRecord;
    }

    /**
     * @return the minTempRecord
     */
    public short getMinTempRecord() {
        return minTempRecord;
    }

    /**
     * @param minTempRecord
     *            the minTempRecord to set
     */
    public void setMinTempRecord(short minTempRecord) {
        this.minTempRecord = minTempRecord;
    }

    /**
     * @return the maxTempYear
     */
    public int[] getMaxTempYear() {
        return maxTempYear;
    }

    /**
     * @param maxTempYear
     *            the maxTempYear to set
     */
    public void setMaxTempYear(int[] maxTempYear) {
        Arrays.sort(maxTempYear);
        this.maxTempYear = maxTempYear;
    }

    /**
     * @return the minTempYear
     */
    public int[] getMinTempYear() {
        return minTempYear;
    }

    /**
     * @param minTempYear
     *            the minTempYear to set
     */
    public void setMinTempYear(int[] minTempYear) {
        Arrays.sort(minTempYear);
        this.minTempYear = minTempYear;
    }

    /**
     * @return the meanTemp
     */
    public float getMeanTemp() {
        return meanTemp;
    }

    /**
     * @param meanTemp
     *            the meanTemp to set
     */
    public void setMeanTemp(float meanTemp) {
        this.meanTemp = meanTemp;
    }

    /**
     * @return the precipMean
     */
    public float getPrecipMean() {
        return precipMean;
    }

    /**
     * @param precipMean
     *            the precipMean to set
     */
    public void setPrecipMean(float precipMean) {
        this.precipMean = precipMean;
    }

    /**
     * @return the precipMonthMean
     */
    public float getPrecipMonthMean() {
        return precipMonthMean;
    }

    /**
     * @param precipMonthMean
     *            the precipMonthMean to set
     */
    public void setPrecipMonthMean(float precipMonthMean) {
        this.precipMonthMean = precipMonthMean;
    }

    /**
     * @return the precipSeasonMean
     */
    public List<Float> getPrecipSeasonMean() {
        return precipSeasonMean;
    }

    /**
     * @param precipSeasonMean
     *            the precipSeasonMean to set
     */
    public void setPrecipSeasonMean(List<Float> precipSeasonMean) {
        this.precipSeasonMean = precipSeasonMean;
    }

    /**
     * @return the precipYearMean
     */
    public float getPrecipYearMean() {
        return precipYearMean;
    }

    /**
     * @param precipYearMean
     *            the precipYearMean to set
     */
    public void setPrecipYearMean(float precipYearMean) {
        this.precipYearMean = precipYearMean;
    }

    /**
     * @return the precipDayRecord
     */
    public float getPrecipDayRecord() {
        return precipDayRecord;
    }

    /**
     * @param precipDayRecord
     *            the precipDayRecord to set
     */
    public void setPrecipDayRecord(float precipDayRecord) {
        this.precipDayRecord = precipDayRecord;
    }

    /**
     * @return the precipMonthRecord
     */
    public float getPrecipMonthRecord() {
        return precipMonthRecord;
    }

    /**
     * @param precipMonthRecord
     *            the precipMonthRecord to set
     */
    public void setPrecipMonthRecord(float precipMonthRecord) {
        this.precipMonthRecord = precipMonthRecord;
    }

    /**
     * @return the precipSeasonRecord
     */
    public float getPrecipSeasonRecord() {
        return precipSeasonRecord;
    }

    /**
     * @param precipSeasonRecord
     *            the precipSeasonRecord to set
     */
    public void setPrecipSeasonRecord(float precipSeasonRecord) {
        this.precipSeasonRecord = precipSeasonRecord;
    }

    /**
     * @return the precipYearRecord
     */
    public float getPrecipYearRecord() {
        return precipYearRecord;
    }

    /**
     * @param precipYearRecord
     *            the precipYearRecord to set
     */
    public void setPrecipYearRecord(float precipYearRecord) {
        this.precipYearRecord = precipYearRecord;
    }

    /**
     * @return the precipDayRecordYear
     */
    public int[] getPrecipDayRecordYear() {
        return precipDayRecordYear;
    }

    /**
     * @param precipDayRecordYear
     *            the precipDayRecordYear to set
     */
    public void setPrecipDayRecordYear(int[] precipDayRecordYear) {
        Arrays.sort(precipDayRecordYear);
        this.precipDayRecordYear = precipDayRecordYear;
    }

    /**
     * @return the precipMonthRecordYear
     */
    public int[] getPrecipMonthRecordYear() {
        return precipMonthRecordYear;
    }

    /**
     * @param precipMonthRecordYear
     *            the precipMonthRecordYear to set
     */
    public void setPrecipMonthRecordYear(int[] precipMonthRecordYear) {
        Arrays.sort(precipMonthRecordYear);
        this.precipMonthRecordYear = precipMonthRecordYear;
    }

    /**
     * @return the precipSeasonRecordYear
     */
    public int[] getPrecipSeasonRecordYear() {
        return precipSeasonRecordYear;
    }

    /**
     * @param precipSeasonRecordYear
     *            the precipSeasonRecordYear to set
     */
    public void setPrecipSeasonRecordYear(int[] precipSeasonRecordYear) {
        Arrays.sort(precipSeasonRecordYear);
        this.precipSeasonRecordYear = precipSeasonRecordYear;
    }

    /**
     * @return the precipYearRecordYear
     */
    public int[] getPrecipYearRecordYear() {
        return precipYearRecordYear;
    }

    /**
     * @param precipYearRecordYear
     *            the precipYearRecordYear to set
     */
    public void setPrecipYearRecordYear(int[] precipYearRecordYear) {
        Arrays.sort(precipYearRecordYear);
        this.precipYearRecordYear = precipYearRecordYear;
    }

    /**
     * @return the snowDayMean
     */
    public float getSnowDayMean() {
        return snowDayMean;
    }

    /**
     * @param snowDayMean
     *            the snowDayMean to set
     */
    public void setSnowDayMean(float snowDayMean) {
        this.snowDayMean = snowDayMean;
    }

    /**
     * @return the snowMonthMean
     */
    public float getSnowMonthMean() {
        return snowMonthMean;
    }

    /**
     * @param snowMonthMean
     *            the snowMonthMean to set
     */
    public void setSnowMonthMean(float snowMonthMean) {
        this.snowMonthMean = snowMonthMean;
    }

    /**
     * @return the snowSeasonMean
     */
    public List<Float> getSnowSeasonMean() {
        return snowSeasonMean;
    }

    /**
     * @param snowSeasonMean
     *            the snowSeasonMean to set
     */
    public void setSnowSeasonMean(List<Float> snowSeasonMean) {
        this.snowSeasonMean = snowSeasonMean;
    }

    /**
     * @return the snowYearMean
     */
    public float getSnowYearMean() {
        return snowYearMean;
    }

    /**
     * @param snowYearMean
     *            the snowYearMean to set
     */
    public void setSnowYearMean(float snowYearMean) {
        this.snowYearMean = snowYearMean;
    }

    /**
     * @return the snowDayRecord
     */
    public float getSnowDayRecord() {
        return snowDayRecord;
    }

    /**
     * @param snowDayRecord
     *            the snowDayRecord to set
     */
    public void setSnowDayRecord(float snowDayRecord) {
        this.snowDayRecord = snowDayRecord;
    }

    /**
     * @return the snowMonthRecord
     */
    public float getSnowMonthRecord() {
        return snowMonthRecord;
    }

    /**
     * @param snowMonthRecord
     *            the snowMonthRecord to set
     */
    public void setSnowMonthRecord(float snowMonthRecord) {
        this.snowMonthRecord = snowMonthRecord;
    }

    /**
     * @return the snowSeasonRecord
     */
    public float getSnowSeasonRecord() {
        return snowSeasonRecord;
    }

    /**
     * @param snowSeasonRecord
     *            the snowSeasonRecord to set
     */
    public void setSnowSeasonRecord(float snowSeasonRecord) {
        this.snowSeasonRecord = snowSeasonRecord;
    }

    /**
     * @return the snowYearRecord
     */
    public float getSnowYearRecord() {
        return snowYearRecord;
    }

    /**
     * @param snowYearRecord
     *            the snowYearRecord to set
     */
    public void setSnowYearRecord(float snowYearRecord) {
        this.snowYearRecord = snowYearRecord;
    }

    /**
     * @return the snowDayRecordYear
     */
    public int[] getSnowDayRecordYear() {
        return snowDayRecordYear;
    }

    /**
     * @param snowDayRecordYear
     *            the snowDayRecordYear to set
     */
    public void setSnowDayRecordYear(int[] snowDayRecordYear) {
        Arrays.sort(snowDayRecordYear);
        this.snowDayRecordYear = snowDayRecordYear;
    }

    /**
     * @return the snowMonthRecordYear
     */
    public int[] getSnowMonthRecordYear() {
        return snowMonthRecordYear;
    }

    /**
     * @param snowMonthRecordYear
     *            the snowMonthRecordYear to set
     */
    public void setSnowMonthRecordYear(int[] snowMonthRecordYear) {
        Arrays.sort(snowMonthRecordYear);
        this.snowMonthRecordYear = snowMonthRecordYear;
    }

    /**
     * @return the snowSeasonRecordYear
     */
    public int[] getSnowSeasonRecordYear() {
        return snowSeasonRecordYear;
    }

    /**
     * @param snowSeasonRecordYear
     *            the snowSeasonRecordYear to set
     */
    public void setSnowSeasonRecordYear(int[] snowSeasonRecordYear) {
        Arrays.sort(snowSeasonRecordYear);
        this.snowSeasonRecordYear = snowSeasonRecordYear;
    }

    /**
     * @return the snowYearRecordYear
     */
    public int[] getSnowYearRecordYear() {
        return snowYearRecordYear;
    }

    /**
     * @param snowYearRecordYear
     *            the snowYearRecordYear to set
     */
    public void setSnowYearRecordYear(int[] snowYearRecordYear) {
        Arrays.sort(snowYearRecordYear);
        this.snowYearRecordYear = snowYearRecordYear;
    }

    /**
     * @return the numHeatMean
     */
    public int getNumHeatMean() {
        return numHeatMean;
    }

    /**
     * @param numHeatMean
     *            the numHeatMean to set
     */
    public void setNumHeatMean(int numHeatMean) {
        this.numHeatMean = numHeatMean;
    }

    /**
     * @return the numHeatMonth
     */
    public int getNumHeatMonth() {
        return numHeatMonth;
    }

    /**
     * @param numHeatMonth
     *            the numHeatMonth to set
     */
    public void setNumHeatMonth(int numHeatMonth) {
        this.numHeatMonth = numHeatMonth;
    }

    /**
     * @return the numHeatSeason
     */
    public int getNumHeatSeason() {
        return numHeatSeason;
    }

    /**
     * @param numHeatSeason
     *            the numHeatSeason to set
     */
    public void setNumHeatSeason(int numHeatSeason) {
        this.numHeatSeason = numHeatSeason;
    }

    /**
     * @return the numHeatYear
     */
    public int getNumHeatYear() {
        return numHeatYear;
    }

    /**
     * @param numHeatYear
     *            the numHeatYear to set
     */
    public void setNumHeatYear(int numHeatYear) {
        this.numHeatYear = numHeatYear;
    }

    /**
     * @return the numCoolMean
     */
    public int getNumCoolMean() {
        return numCoolMean;
    }

    /**
     * @param numCoolMean
     *            the numCoolMean to set
     */
    public void setNumCoolMean(int numCoolMean) {
        this.numCoolMean = numCoolMean;
    }

    /**
     * @return the numCoolMonth
     */
    public int getNumCoolMonth() {
        return numCoolMonth;
    }

    /**
     * @param numCoolMonth
     *            the numCoolMonth to set
     */
    public void setNumCoolMonth(int numCoolMonth) {
        this.numCoolMonth = numCoolMonth;
    }

    /**
     * @return the numCoolSeason
     */
    public int getNumCoolSeason() {
        return numCoolSeason;
    }

    /**
     * @param numCoolSeason
     *            the numCoolSeason to set
     */
    public void setNumCoolSeason(int numCoolSeason) {
        this.numCoolSeason = numCoolSeason;
    }

    /**
     * @return the numCoolYear
     */
    public int getNumCoolYear() {
        return numCoolYear;
    }

    /**
     * @param numCoolYear
     *            the numCoolYear to set
     */
    public void setNumCoolYear(int numCoolYear) {
        this.numCoolYear = numCoolYear;
    }

    public void setDataToMissing() {
        this.informId = -1 * ParameterFormatClimate.MISSING;
        this.meanTemp = ParameterFormatClimate.MISSING;
        this.maxTempMean = ParameterFormatClimate.MISSING;
        this.minTempMean = ParameterFormatClimate.MISSING;
        this.maxTempRecord = ParameterFormatClimate.MISSING;
        this.minTempRecord = ParameterFormatClimate.MISSING;
        this.precipMean = ParameterFormatClimate.MISSING;
        this.precipMonthMean = ParameterFormatClimate.MISSING;
        this.precipSeasonMean = new ArrayList<>();
        this.precipYearMean = ParameterFormatClimate.MISSING;
        this.precipDayRecord = ParameterFormatClimate.MISSING;
        this.precipMonthRecord = ParameterFormatClimate.MISSING;
        this.precipSeasonRecord = ParameterFormatClimate.MISSING;
        this.precipYearRecord = ParameterFormatClimate.MISSING;
        this.snowDayMean = ParameterFormatClimate.MISSING;
        this.snowMonthMean = ParameterFormatClimate.MISSING;
        this.snowSeasonMean = new ArrayList<>();
        this.snowYearMean = ParameterFormatClimate.MISSING;
        this.snowDayRecord = ParameterFormatClimate.MISSING;
        this.snowMonthRecord = ParameterFormatClimate.MISSING;
        this.snowSeasonRecord = ParameterFormatClimate.MISSING;
        this.snowYearRecord = ParameterFormatClimate.MISSING;
        this.numHeatMean = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numHeatMonth = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numHeatSeason = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numHeatYear = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolMean = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolMonth = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolSeason = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolYear = ParameterFormatClimate.MISSING_DEGREE_DAY;

        for (int j = 0; j < 3; j++) {

            this.maxTempYear[j] = ParameterFormatClimate.MISSING;
            this.minTempYear[j] = ParameterFormatClimate.MISSING;
            this.precipDayRecordYear[j] = ParameterFormatClimate.MISSING;
            this.precipMonthRecordYear[j] = ParameterFormatClimate.MISSING;
            this.precipSeasonRecordYear[j] = ParameterFormatClimate.MISSING;
            this.precipYearRecordYear[j] = ParameterFormatClimate.MISSING;
            this.snowDayRecordYear[j] = ParameterFormatClimate.MISSING;
            this.snowMonthRecordYear[j] = ParameterFormatClimate.MISSING;
            this.snowSeasonRecordYear[j] = ParameterFormatClimate.MISSING;
            this.snowYearRecordYear[j] = ParameterFormatClimate.MISSING;
        }
    }

    public static ClimateRecordDay getMissingClimateRecordDay() {
        ClimateRecordDay recordDay = new ClimateRecordDay();
        recordDay.setDataToMissing();
        return recordDay;
    }

}

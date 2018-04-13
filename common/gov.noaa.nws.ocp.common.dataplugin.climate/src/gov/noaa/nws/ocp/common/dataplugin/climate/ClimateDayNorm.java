/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Originate climate_record_c2.h:struct Climate_Day_Record.
 * 
 * table: day_climate_norm
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 20, 2016 18469      wkwock      Initial creation
 * Jul 22, 2016 20712	   wpaintsil   Serialization
 * 13 MAR 2017  27420      amoore      Rename due to similarly named class in package.
 * 16 MAR 2017  30162      amoore      Add static missing data instantiation.
 * 24 APR 2017  33104      amoore      Capitalization correction.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class ClimateDayNorm {

    /**
     * station identifier
     */
    @DynamicSerializeElement
    private long stationId;

    /**
     * month and day mm-dd format
     */
    @DynamicSerializeElement
    private String dayOfYear;

    /**
     * record maxTemp (F)
     */
    @DynamicSerializeElement
    private short maxTempRecord;

    /**
     * max temperature for a given date (F)
     */
    @DynamicSerializeElement
    private short maxTempMean;

    /**
     * record minTemp (F)
     */
    @DynamicSerializeElement
    private short minTempRecord;

    /**
     * min temperature for a given date (F)
     */
    @DynamicSerializeElement
    private short minTempMean;

    /**
     * year(s) record maxTemp was observed
     */
    @DynamicSerializeElement
    private short[] maxTempYear = new short[3];

    /**
     * year(s) record minTemp was observed
     */
    @DynamicSerializeElement
    private short[] minTempYear = new short[3];

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
     * max. precip for given date (inches)
     */
    @DynamicSerializeElement
    private float precipDayRecord;

    /**
     * year(s) max precip was observed
     */
    @DynamicSerializeElement
    private short[] precipDayRecordYear = new short[3];

    /**
     * average daily snowfall
     */
    @DynamicSerializeElement
    private float snowDayMean;

    /**
     * maximum snow in inches
     */
    @DynamicSerializeElement
    private float snowDayRecord;

    /**
     * year(s) the maximum snow was observed)
     */
    @DynamicSerializeElement
    private short[] snowDayRecordYear = new short[3];

    /**
     * snow depth
     */
    @DynamicSerializeElement
    private float snowGround;

    /**
     * mean heating degree days this date
     */
    @DynamicSerializeElement
    private int numHeatMean;

    /**
     * mean cooling degree days this date
     */
    @DynamicSerializeElement
    private int numCoolMean;

    /**
     * @return the station ID
     */
    public long getStationId() {
        return stationId;
    }

    /**
     * @param stationId
     *            the stationId to set
     */
    public void setStationId(long stationId) {
        this.stationId = stationId;
    }

    /**
     * @return the day of year
     */
    public String getDayOfYear() {
        return dayOfYear;
    }

    /**
     * @param dayOfYear
     *            the dayOfYear to set
     */
    public void setDayOfYear(String dayOfYear) {
        this.dayOfYear = dayOfYear;
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
    public short[] getMaxTempYear() {
        return maxTempYear;
    }

    /**
     * @param maxTempYear
     *            the maxTempYear to set
     */
    public void setMaxTempYear(short[] maxTempYear) {
        this.maxTempYear = maxTempYear;
    }

    /**
     * @return the minTempYear
     */
    public short[] getMinTempYear() {
        return minTempYear;
    }

    /**
     * @param minTempYear
     *            the minTempYear to set
     */
    public void setMinTempYear(short[] minTempYear) {
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
     * @return the precipDayRecordYear
     */
    public short[] getPrecipDayRecordYear() {
        return precipDayRecordYear;
    }

    /**
     * @param precipDayRecordYear
     *            the precipDayRecordYear to set
     */
    public void setPrecipDayRecordYear(short[] precipDayRecordYear) {
        this.precipDayRecordYear = precipDayRecordYear;
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
     * @return the snowDayRecordYear
     */
    public short[] getSnowDayRecordYear() {
        return snowDayRecordYear;
    }

    /**
     * @param snowDayRecordYear
     *            the snowDayRecordYear to set
     */
    public void setSnowDayRecordYear(short[] snowDayRecordYear) {
        this.snowDayRecordYear = snowDayRecordYear;
    }

    /**
     * @return the snowGround
     */
    public float getSnowGround() {
        return snowGround;
    }

    /**
     * @param snowGround
     *            the snowGround to set
     */
    public void setSnowGround(float snowGround) {
        this.snowGround = snowGround;
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
     * Set all data to missing.
     */
    public void setDataToMissing() {
        this.stationId = -1 * ParameterFormatClimate.MISSING;
        this.dayOfYear = "";
        this.meanTemp = ParameterFormatClimate.MISSING;
        this.maxTempMean = ParameterFormatClimate.MISSING;
        this.minTempMean = ParameterFormatClimate.MISSING;
        this.maxTempRecord = ParameterFormatClimate.MISSING;
        this.minTempRecord = ParameterFormatClimate.MISSING;
        this.precipMean = ParameterFormatClimate.MISSING;
        this.precipDayRecord = ParameterFormatClimate.MISSING;
        this.snowDayMean = ParameterFormatClimate.MISSING;
        this.snowDayRecord = ParameterFormatClimate.MISSING;
        this.numHeatMean = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.numCoolMean = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.snowGround = ParameterFormatClimate.MISSING;

        for (int j = 0; j < 3; j++) {
            this.maxTempYear[j] = ParameterFormatClimate.MISSING;
            this.minTempYear[j] = ParameterFormatClimate.MISSING;
            this.precipDayRecordYear[j] = ParameterFormatClimate.MISSING;
            this.snowDayRecordYear[j] = ParameterFormatClimate.MISSING;
        }
    }

    /**
     * @return a new object filled with missing values.
     */
    public static ClimateDayNorm getMissingClimateDayNorm() {
        ClimateDayNorm dayNorm = new ClimateDayNorm();
        dayNorm.setDataToMissing();
        return dayNorm;
    }
}

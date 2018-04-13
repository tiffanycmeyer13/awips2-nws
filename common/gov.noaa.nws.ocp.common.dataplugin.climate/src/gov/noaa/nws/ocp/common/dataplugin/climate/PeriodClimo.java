/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Class for store data from table mon_climate_norm. Missing data is represented
 * by 9999.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 7, 2016             xzhang      Initial creation
 * May 18, 2016 18469      wkwock      Make object clonable
 * Jul 22, 2016 20635      wkwock      remove clonable. add precipPeriodMinYearList to setMissing
 * Jul 22, 2016 20712      wpaintsil   Serialization
 * SEP 21 2016  22395      amoore      Add missing data methods.
 * OCT 14 2016  20635      wkwock      Add monthOfYear and periodType.
 * OCT 31 2016  20635      wkwock      Change int to PeriodType for periodType.
 * 04 APR 2017  30166      amoore      Add copy constructor.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class PeriodClimo {
    /**
     * stationId
     */
    @DynamicSerializeElement
    private int informId;

    /**
     * month_of_year
     */
    @DynamicSerializeElement
    private int monthOfYear;

    /**
     * period_type
     */
    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * record maxTemp
     */
    @DynamicSerializeElement
    private int maxTempRecord;

    /**
     * date(s) of record max temp
     */
    @DynamicSerializeElement
    private List<ClimateDate> dayMaxTempRecordList;

    /**
     * mean climo max temperature
     */
    @DynamicSerializeElement
    private float maxTempNorm;

    /**
     * record min temp, degrees F
     */
    @DynamicSerializeElement
    private int minTempRecord;

    /**
     * date(s) of observed minTemp
     */
    @DynamicSerializeElement
    private List<ClimateDate> dayMinTempRecordList;

    /**
     * mean climo min temp
     */
    @DynamicSerializeElement
    private float minTempNorm;

    /**
     * norm climo average temp
     */
    @DynamicSerializeElement
    private float normMeanTemp;

    /**
     * normal mean max temperature
     */
    @DynamicSerializeElement
    private float normMeanMaxTemp;

    /**
     * normal mean min temperature
     */
    @DynamicSerializeElement
    private float normMeanMinTemp;

    /**
     * mean # of days max temp GE 90F
     */
    @DynamicSerializeElement
    private float normNumMaxGE90F;

    /**
     * mean # of days max temp LE 32F
     */
    @DynamicSerializeElement
    private float normNumMaxLE32F;

    /**
     * mean # of days min temp LE 32F
     */
    @DynamicSerializeElement
    private float normNumMinLE32F;

    /**
     * mean # of days min temp LE 0F
     */
    @DynamicSerializeElement
    private float normNumMinLE0F;

    /**
     * mean cumulative precip this month (in.)
     */
    @DynamicSerializeElement
    private float precipPeriodNorm;

    /**
     * record precip for this month
     */
    @DynamicSerializeElement
    private float precipPeriodMax;

    /**
     * year of record monthly precip
     */
    @DynamicSerializeElement
    private List<ClimateDate> precipPeriodMaxYearList;

    /**
     * record min precip for this month
     */
    @DynamicSerializeElement
    private float precipPeriodMin;

    /**
     * year of record monthly precip
     */
    @DynamicSerializeElement
    private List<ClimateDate> precipPeriodMinYearList;

    /**
     * daily average precip
     */
    @DynamicSerializeElement
    private float precipDayNorm;

    /**
     * mean # of days precip GE .01 inches
     */
    @DynamicSerializeElement
    private float numPrcpGE01Norm;

    /**
     * mean # of days precip GE .10 inches
     */
    @DynamicSerializeElement
    private float numPrcpGE10Norm;

    /**
     * mean # of days precip GE .50 inches
     */
    @DynamicSerializeElement
    private float numPrcpGE50Norm;

    /**
     * mean # of days precip GE 1.00 inches
     */
    @DynamicSerializeElement
    private float numPrcpGE100Norm;

    /**
     * total snow for month (in.)
     */
    @DynamicSerializeElement
    private float snowPeriodNorm;

    /**
     * record snowfall for the month
     */
    @DynamicSerializeElement
    private float snowPeriodRecord;

    /**
     * year of record monthly snow
     */
    @DynamicSerializeElement
    private List<ClimateDate> snowPeriodMaxYearList;

    /**
     * record max 24 hour snowfall
     */
    @DynamicSerializeElement
    private float snowMax24HRecord;

    /**
     * start, end dates of max 24H snow
     */
    @DynamicSerializeElement
    private List<ClimateDates> snow24HList;

    /**
     * normal water equivalent of snow (in.)
     */
    @DynamicSerializeElement
    private float snowWaterPeriodNorm;

    /**
     * normal water equivalent of snow (in.)
     */
    @DynamicSerializeElement
    private float snowWaterJuly1Norm;

    /**
     * mean total snowfall sinceJuly
     */
    @DynamicSerializeElement
    private float snowJuly1Norm;

    /**
     * average climo snow depth
     */
    @DynamicSerializeElement
    private float snowGroundNorm;

    /**
     * record snow depth for the month
     */
    @DynamicSerializeElement
    private int snowGroundMax;

    /**
     * date(s) of observed snow depth
     */
    @DynamicSerializeElement
    private List<ClimateDate> daySnowGroundMaxList;

    /**
     * mean # of days with any snowfall
     */
    @DynamicSerializeElement
    private float numSnowGETRNorm;

    /**
     * mean # of days with snow GEinch
     */
    @DynamicSerializeElement
    private float numSnowGE1Norm;

    /**
     * mean cumulative # of heat days
     */
    @DynamicSerializeElement
    private int numHeatPeriodNorm;

    /**
     * mean cumlative # of heat days sinceJuly
     */
    @DynamicSerializeElement
    private int numHeat1JulyNorm;

    /**
     * mean cumulative # of cool days
     */
    @DynamicSerializeElement
    private int numCoolPeriodNorm;

    /**
     * mean cumulative # of cool days sinceJan
     */
    @DynamicSerializeElement
    private int numCool1JanNorm;

    /**
     * normal earliest freeze date
     */
    @DynamicSerializeElement
    private ClimateDate earlyFreezeNorm;

    /**
     * record early freeze date
     */
    @DynamicSerializeElement
    private ClimateDate earlyFreezeRec;

    /**
     * normal latest freeze date
     */
    @DynamicSerializeElement
    private ClimateDate lateFreezeNorm;

    /**
     * record late freeze date
     */
    @DynamicSerializeElement
    private ClimateDate lateFreezeRec;

    /**
     * Empty constructor.
     */
    public PeriodClimo() {
    }

    /**
     * Copy constructor.
     *
     * @param other
     */
    public PeriodClimo(PeriodClimo other) {
        informId = other.getInformId();
        monthOfYear = other.getMonthOfYear();
        periodType = other.getPeriodType();
        maxTempRecord = other.getMaxTempRecord();
        dayMaxTempRecordList = new ArrayList<>();
        for (ClimateDate date : other.getDayMaxTempRecordList()) {
            dayMaxTempRecordList.add(new ClimateDate(date));
        }
        maxTempNorm = other.getMaxTempNorm();
        minTempRecord = other.getMinTempRecord();
        dayMinTempRecordList = new ArrayList<>();
        for (ClimateDate date : other.getDayMinTempRecordList()) {
            dayMinTempRecordList.add(new ClimateDate(date));
        }
        minTempNorm = other.getMinTempNorm();
        normMeanTemp = other.getNormMeanTemp();
        normMeanMaxTemp = other.getNormMeanMaxTemp();
        normMeanMinTemp = other.getNormMeanMinTemp();
        normNumMaxGE90F = other.getNormNumMaxGE90F();
        normNumMaxLE32F = other.getNormNumMaxLE32F();
        normNumMinLE32F = other.getNormNumMinLE32F();
        normNumMinLE0F = other.getNormNumMinLE0F();
        precipPeriodNorm = other.getPrecipPeriodNorm();
        precipPeriodMax = other.getPrecipPeriodMax();
        precipPeriodMaxYearList = new ArrayList<>();
        for (ClimateDate date : other.getPrecipPeriodMaxYearList()) {
            precipPeriodMaxYearList.add(new ClimateDate(date));
        }
        precipPeriodMin = other.getPrecipPeriodMin();
        precipPeriodMinYearList = new ArrayList<>();
        for (ClimateDate date : other.getPrecipPeriodMinYearList()) {
            precipPeriodMinYearList.add(new ClimateDate(date));
        }
        precipDayNorm = other.getPrecipDayNorm();
        numPrcpGE01Norm = other.getNumPrcpGE01Norm();
        numPrcpGE10Norm = other.getNumPrcpGE10Norm();
        numPrcpGE50Norm = other.getNumPrcpGE50Norm();
        numPrcpGE100Norm = other.getNumPrcpGE100Norm();
        snowPeriodNorm = other.getSnowPeriodNorm();
        snowPeriodRecord = other.getSnowPeriodRecord();
        snowPeriodMaxYearList = new ArrayList<>();
        for (ClimateDate date : other.getSnowPeriodMaxYearList()) {
            snowPeriodMaxYearList.add(new ClimateDate(date));
        }
        snowMax24HRecord = other.getSnowMax24HRecord();
        snow24HList = new ArrayList<>();
        for (ClimateDates dates : other.getSnow24HList()) {
            snow24HList.add(new ClimateDates(dates));
        }
        snowWaterPeriodNorm = other.getSnowWaterPeriodNorm();
        snowWaterJuly1Norm = other.getSnowWaterJuly1Norm();
        snowJuly1Norm = other.getSnowJuly1Norm();
        snowGroundNorm = other.getSnowGroundNorm();
        snowGroundMax = other.getSnowGroundMax();
        daySnowGroundMaxList = new ArrayList<>();
        for (ClimateDate date : other.getDaySnowGroundMaxList()) {
            daySnowGroundMaxList.add(new ClimateDate(date));
        }
        numSnowGETRNorm = other.getNumSnowGETRNorm();
        numSnowGE1Norm = other.getNumSnowGE1Norm();
        numHeatPeriodNorm = other.getNumHeatPeriodNorm();
        numHeat1JulyNorm = other.getNumHeat1JulyNorm();
        numCoolPeriodNorm = other.getNumCoolPeriodNorm();
        numCool1JanNorm = other.getNumCool1JanNorm();
        earlyFreezeNorm = new ClimateDate(other.getEarlyFreezeNorm());
        earlyFreezeRec = new ClimateDate(other.getEarlyFreezeRec());
        lateFreezeNorm = new ClimateDate(other.getLateFreezeNorm());
        lateFreezeRec = new ClimateDate(other.getLateFreezeRec());
    }

    public int getInformId() {
        return informId;
    }

    public void setInformId(int informId) {
        this.informId = informId;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    public void setMonthOfYear(int monthOfYear) {
        this.monthOfYear = monthOfYear;
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public int getMaxTempRecord() {
        return maxTempRecord;
    }

    public void setMaxTempRecord(int maxTempRecord) {
        this.maxTempRecord = maxTempRecord;
    }

    public List<ClimateDate> getDayMaxTempRecordList() {
        return dayMaxTempRecordList;
    }

    public void setDayMaxTempRecordList(
            List<ClimateDate> dayMaxTempRecordList) {
        this.dayMaxTempRecordList = dayMaxTempRecordList;
    }

    public float getMaxTempNorm() {
        return maxTempNorm;
    }

    public void setMaxTempNorm(float maxTempNorm) {
        this.maxTempNorm = maxTempNorm;
    }

    public int getMinTempRecord() {
        return minTempRecord;
    }

    public void setMinTempRecord(int minTempRecord) {
        this.minTempRecord = minTempRecord;
    }

    public List<ClimateDate> getDayMinTempRecordList() {
        return dayMinTempRecordList;
    }

    public void setDayMinTempRecordList(
            List<ClimateDate> dayMinTempRecordList) {
        this.dayMinTempRecordList = dayMinTempRecordList;
    }

    public float getMinTempNorm() {
        return minTempNorm;
    }

    public void setMinTempNorm(float minTempNorm) {
        this.minTempNorm = minTempNorm;
    }

    public float getNormMeanTemp() {
        return normMeanTemp;
    }

    public void setNormMeanTemp(float normMeanTemp) {
        this.normMeanTemp = normMeanTemp;
    }

    public float getNormMeanMaxTemp() {
        return normMeanMaxTemp;
    }

    public void setNormMeanMaxTemp(float normMeanMaxTemp) {
        this.normMeanMaxTemp = normMeanMaxTemp;
    }

    public float getNormMeanMinTemp() {
        return normMeanMinTemp;
    }

    public void setNormMeanMinTemp(float normMeanMinTemp) {
        this.normMeanMinTemp = normMeanMinTemp;
    }

    public float getNormNumMaxGE90F() {
        return normNumMaxGE90F;
    }

    public void setNormNumMaxGE90F(float normNumMaxGE90F) {
        this.normNumMaxGE90F = normNumMaxGE90F;
    }

    public float getNormNumMaxLE32F() {
        return normNumMaxLE32F;
    }

    public void setNormNumMaxLE32F(float normNumMaxLE32F) {
        this.normNumMaxLE32F = normNumMaxLE32F;
    }

    public float getNormNumMinLE32F() {
        return normNumMinLE32F;
    }

    public void setNormNumMinLE32F(float normNumMinLE32F) {
        this.normNumMinLE32F = normNumMinLE32F;
    }

    public float getNormNumMinLE0F() {
        return normNumMinLE0F;
    }

    public void setNormNumMinLE0F(float normNumMinLE0F) {
        this.normNumMinLE0F = normNumMinLE0F;
    }

    public float getPrecipPeriodNorm() {
        return precipPeriodNorm;
    }

    public void setPrecipPeriodNorm(float precipPeriodNorm) {
        this.precipPeriodNorm = precipPeriodNorm;
    }

    public float getPrecipPeriodMax() {
        return precipPeriodMax;
    }

    public void setPrecipPeriodMax(float precipPeriodMax) {
        this.precipPeriodMax = precipPeriodMax;
    }

    public List<ClimateDate> getPrecipPeriodMaxYearList() {
        return precipPeriodMaxYearList;
    }

    public void setPrecipPeriodMaxYearList(
            List<ClimateDate> precipPeriodMaxYearList) {
        this.precipPeriodMaxYearList = precipPeriodMaxYearList;
    }

    public float getPrecipPeriodMin() {
        return precipPeriodMin;
    }

    public void setPrecipPeriodMin(float precipPeriodMin) {
        this.precipPeriodMin = precipPeriodMin;
    }

    public List<ClimateDate> getPrecipPeriodMinYearList() {
        return precipPeriodMinYearList;
    }

    public void setPrecipPeriodMinYearList(
            List<ClimateDate> precipPeriodMinYearList) {
        this.precipPeriodMinYearList = precipPeriodMinYearList;
    }

    public float getPrecipDayNorm() {
        return precipDayNorm;
    }

    public void setPrecipDayNorm(float precipDayNorm) {
        this.precipDayNorm = precipDayNorm;
    }

    public float getNumPrcpGE01Norm() {
        return numPrcpGE01Norm;
    }

    public void setNumPrcpGE01Norm(float numPrcpGE01Norm) {
        this.numPrcpGE01Norm = numPrcpGE01Norm;
    }

    public float getNumPrcpGE10Norm() {
        return numPrcpGE10Norm;
    }

    public void setNumPrcpGE10Norm(float numPrcpGE10Norm) {
        this.numPrcpGE10Norm = numPrcpGE10Norm;
    }

    public float getNumPrcpGE50Norm() {
        return numPrcpGE50Norm;
    }

    public void setNumPrcpGE50Norm(float numPrcpGE50Norm) {
        this.numPrcpGE50Norm = numPrcpGE50Norm;
    }

    public float getNumPrcpGE100Norm() {
        return numPrcpGE100Norm;
    }

    public void setNumPrcpGE100Norm(float numPrcpGE100Norm) {
        this.numPrcpGE100Norm = numPrcpGE100Norm;
    }

    public float getSnowPeriodNorm() {
        return snowPeriodNorm;
    }

    public void setSnowPeriodNorm(float snowPeriodNorm) {
        this.snowPeriodNorm = snowPeriodNorm;
    }

    public float getSnowPeriodRecord() {
        return snowPeriodRecord;
    }

    public void setSnowPeriodRecord(float snowPeriodRecord) {
        this.snowPeriodRecord = snowPeriodRecord;
    }

    public List<ClimateDate> getSnowPeriodMaxYearList() {
        return snowPeriodMaxYearList;
    }

    public void setSnowPeriodMaxYearList(
            List<ClimateDate> snowPeriodMaxYearList) {
        this.snowPeriodMaxYearList = snowPeriodMaxYearList;
    }

    public float getSnowMax24HRecord() {
        return snowMax24HRecord;
    }

    public void setSnowMax24HRecord(float snowMax24HRecord) {
        this.snowMax24HRecord = snowMax24HRecord;
    }

    public List<ClimateDates> getSnow24HList() {
        return snow24HList;
    }

    public void setSnow24HList(List<ClimateDates> snow24hList) {
        snow24HList = snow24hList;
    }

    public float getSnowWaterPeriodNorm() {
        return snowWaterPeriodNorm;
    }

    public void setSnowWaterPeriodNorm(float snowWaterPeriodNorm) {
        this.snowWaterPeriodNorm = snowWaterPeriodNorm;
    }

    public float getSnowWaterJuly1Norm() {
        return snowWaterJuly1Norm;
    }

    public void setSnowWaterJuly1Norm(float snowWaterJuly1Norm) {
        this.snowWaterJuly1Norm = snowWaterJuly1Norm;
    }

    public float getSnowJuly1Norm() {
        return snowJuly1Norm;
    }

    public void setSnowJuly1Norm(float snowJuly1Norm) {
        this.snowJuly1Norm = snowJuly1Norm;
    }

    public float getSnowGroundNorm() {
        return snowGroundNorm;
    }

    public void setSnowGroundNorm(float snowGroundNorm) {
        this.snowGroundNorm = snowGroundNorm;
    }

    public int getSnowGroundMax() {
        return snowGroundMax;
    }

    public void setSnowGroundMax(int snowGroundMax) {
        this.snowGroundMax = snowGroundMax;
    }

    public List<ClimateDate> getDaySnowGroundMaxList() {
        return daySnowGroundMaxList;
    }

    public void setDaySnowGroundMaxList(
            List<ClimateDate> daySnowGroundMaxList) {
        this.daySnowGroundMaxList = daySnowGroundMaxList;
    }

    public float getNumSnowGETRNorm() {
        return numSnowGETRNorm;
    }

    public void setNumSnowGETRNorm(float numSnowGETRNorm) {
        this.numSnowGETRNorm = numSnowGETRNorm;
    }

    public float getNumSnowGE1Norm() {
        return numSnowGE1Norm;
    }

    public void setNumSnowGE1Norm(float numSnowGE1Norm) {
        this.numSnowGE1Norm = numSnowGE1Norm;
    }

    public int getNumHeatPeriodNorm() {
        return numHeatPeriodNorm;
    }

    public void setNumHeatPeriodNorm(int numHeatPeriodNorm) {
        this.numHeatPeriodNorm = numHeatPeriodNorm;
    }

    public int getNumHeat1JulyNorm() {
        return numHeat1JulyNorm;
    }

    public void setNumHeat1JulyNorm(int numHeat1JulyNorm) {
        this.numHeat1JulyNorm = numHeat1JulyNorm;
    }

    public int getNumCoolPeriodNorm() {
        return numCoolPeriodNorm;
    }

    public void setNumCoolPeriodNorm(int numCoolPeriodNorm) {
        this.numCoolPeriodNorm = numCoolPeriodNorm;
    }

    public int getNumCool1JanNorm() {
        return numCool1JanNorm;
    }

    public void setNumCool1JanNorm(int numCool1JanNorm) {
        this.numCool1JanNorm = numCool1JanNorm;
    }

    public ClimateDate getEarlyFreezeNorm() {
        return earlyFreezeNorm;
    }

    public void setEarlyFreezeNorm(ClimateDate earlyFreezeNorm) {
        this.earlyFreezeNorm = earlyFreezeNorm;
    }

    public ClimateDate getEarlyFreezeRec() {
        return earlyFreezeRec;
    }

    public void setEarlyFreezeRec(ClimateDate earlyFreezeRec) {
        this.earlyFreezeRec = earlyFreezeRec;
    }

    public ClimateDate getLateFreezeNorm() {
        return lateFreezeNorm;
    }

    public void setLateFreezeNorm(ClimateDate lateFreezeNorm) {
        this.lateFreezeNorm = lateFreezeNorm;
    }

    public ClimateDate getLateFreezeRec() {
        return lateFreezeRec;
    }

    public void setLateFreezeRec(ClimateDate lateFreezeRec) {
        this.lateFreezeRec = lateFreezeRec;
    }

    public void setDataToMissing() {
        /* stationId */
        this.informId = -1 * ParameterFormatClimate.MISSING;
        this.monthOfYear = ParameterFormatClimate.MISSING;
        this.periodType = PeriodType.OTHER;
        /*
         * record maxTemp
         */
        this.maxTempRecord = ParameterFormatClimate.MISSING;

        this.dayMaxTempRecordList = new ArrayList<>();
        this.dayMinTempRecordList = new ArrayList<>();
        this.precipPeriodMaxYearList = new ArrayList<>();
        this.precipPeriodMinYearList = new ArrayList<>();
        this.snowPeriodMaxYearList = new ArrayList<>();
        this.dayMaxTempRecordList = new ArrayList<>();
        this.snow24HList = new ArrayList<>();
        this.daySnowGroundMaxList = new ArrayList<>();
        this.dayMaxTempRecordList = new ArrayList<>();

        for (int j = 0; j < 3; j++) {
            /*
             * date ( s ) of record max temp
             */
            this.dayMaxTempRecordList.add(ClimateDate.getMissingClimateDate());
            /*
             * date ( s ) of observed minTemp
             */
            this.dayMinTempRecordList.add(ClimateDate.getMissingClimateDate());
            this.precipPeriodMaxYearList
                    .add(ClimateDate.getMissingClimateDate());
            this.precipPeriodMinYearList
                    .add(ClimateDate.getMissingClimateDate());
            this.snowPeriodMaxYearList.add(ClimateDate.getMissingClimateDate());
            this.dayMaxTempRecordList.add(ClimateDate.getMissingClimateDate());
            this.snow24HList.add(ClimateDates.getMissingClimateDates());
            this.daySnowGroundMaxList.add(ClimateDate.getMissingClimateDate());
            /*
             * date ( s ) of observed snow depth
             */
            this.dayMaxTempRecordList.add(ClimateDate.getMissingClimateDate());
        }
        /*
         * mean climo max temperature
         */
        this.maxTempNorm = ParameterFormatClimate.MISSING;
        /*
         * record min temp, degrees F
         */
        this.minTempRecord = ParameterFormatClimate.MISSING;
        /*
         * mean climo min temp
         */
        this.minTempNorm = ParameterFormatClimate.MISSING;
        /*
         * norm climo average temp
         */
        this.normMeanTemp = ParameterFormatClimate.MISSING;
        /*
         * normal mean max temperature
         */
        this.normMeanMaxTemp = ParameterFormatClimate.MISSING;
        /*
         * normal mean min temperature
         */
        this.normMeanMinTemp = ParameterFormatClimate.MISSING;
        /*
         * mean # of days max temp GE 90F
         */
        this.normNumMaxGE90F = ParameterFormatClimate.MISSING;
        /*
         * mean # of days max temp LE 32F
         */
        this.normNumMaxLE32F = ParameterFormatClimate.MISSING;
        /*
         * mean # of days min temp LE 32F
         */
        this.normNumMinLE32F = ParameterFormatClimate.MISSING;
        /*
         * mean # of days min temp LE 0F
         */
        this.normNumMinLE0F = ParameterFormatClimate.MISSING;
        /*
         * mean cumulative precip this month (in.)
         */
        this.precipPeriodNorm = ParameterFormatClimate.MISSING;
        /*
         * record precip for this month
         */
        this.precipPeriodMax = ParameterFormatClimate.MISSING;
        /*
         * record min precip for this month
         */
        this.precipPeriodMin = ParameterFormatClimate.MISSING;
        /*
         * daily average precip
         */
        this.precipDayNorm = ParameterFormatClimate.MISSING;
        /*
         * mean # of days precip GE .01 inches
         */
        this.numPrcpGE01Norm = ParameterFormatClimate.MISSING;
        /*
         * mean # of days precip GE .10 inches
         */
        this.numPrcpGE10Norm = ParameterFormatClimate.MISSING;
        /*
         * mean # of days precip GE .50 inches
         */
        this.numPrcpGE50Norm = ParameterFormatClimate.MISSING;
        /*
         * mean # of days precip GE 1.00 inches
         */
        this.numPrcpGE100Norm = ParameterFormatClimate.MISSING;
        /*
         * total snow for month (in.)
         */
        this.snowPeriodNorm = ParameterFormatClimate.MISSING;
        /*
         * record snowfall for the month
         */
        this.snowPeriodRecord = ParameterFormatClimate.MISSING;
        /*
         * record max 24 hour snowfall
         */
        this.snowMax24HRecord = ParameterFormatClimate.MISSING;
        /*
         * normal water equivalent of snow (in.)
         */
        this.snowWaterPeriodNorm = ParameterFormatClimate.MISSING;
        /*
         * normal water equivalent of snow (in.)
         */
        this.snowWaterJuly1Norm = ParameterFormatClimate.MISSING;
        /*
         * mean total snowfall since 1 July
         */
        this.snowJuly1Norm = ParameterFormatClimate.MISSING;
        /*
         * average climo snow depth
         */
        this.snowGroundNorm = ParameterFormatClimate.MISSING;
        /*
         * record snow depth for the month
         */
        this.snowGroundMax = ParameterFormatClimate.MISSING;
        /*
         * mean # of days with any snowfall
         */
        this.numSnowGETRNorm = ParameterFormatClimate.MISSING;
        /*
         * mean # of days with snow GE 1 inch
         */
        this.numSnowGE1Norm = ParameterFormatClimate.MISSING;
        /*
         * mean cumulative # of heat days
         */
        this.numHeatPeriodNorm = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * mean cumlative # of heat days since 1 July
         */
        this.numHeat1JulyNorm = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * mean cumulative # of cool days
         */
        this.numCoolPeriodNorm = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * mean cumulative # of cool days since 1 Jan
         */
        this.numCool1JanNorm = ParameterFormatClimate.MISSING_DEGREE_DAY;
        this.earlyFreezeNorm = ClimateDate.getMissingClimateDate();
        /*
         * normal latest freeze date
         */
        this.lateFreezeNorm = ClimateDate.getMissingClimateDate();
        this.earlyFreezeRec = ClimateDate.getMissingClimateDate();

        this.lateFreezeRec = ClimateDate.getMissingClimateDate();
    }

    /**
     * @return instance with all missing values.
     */
    public static PeriodClimo getMissingPeriodClimo() {
        PeriodClimo periodClimo = new PeriodClimo();
        periodClimo.setDataToMissing();
        return periodClimo;
    }
}

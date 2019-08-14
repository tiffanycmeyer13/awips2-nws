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
 * Data over a specific period (usually month, season, year).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 7, 2016             xzhang      Initial creation
 * 14 JUL 2016  20414      amoore      Added serialization and missing data method.
 * 28 JUL 2016  20414      amoore      Precip/Snow 24-hour can have multiple start-end pairs.
 * 09 AUG 2016  20414      amoore      Cleanup naming.
 * 21 OCT 2016  22135      wpaintsil   Add PeriodDataMethod field.
 * 14 DEC 2016  27015      amoore      Added copy constructor.
 * 17 APR 2017  33104      amoore      Address comments from review.
 * 18 JUL 2019  DR21454    wpaintsil   More than 4 minimum temp dates results in first 3 
 *                                     being displayed when the last 3 are expected. 
 *                                     Limit the list size to 3.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class PeriodData {
    /**
     * stationId number
     */
    @DynamicSerializeElement
    private int informId;

    /**
     * max observed temp, degrees F
     */
    @DynamicSerializeElement
    private int maxTemp;

    /**
     * date(s) of max observed temp
     */
    @DynamicSerializeElement
    private List<ClimateDate> dayMaxTempList;

    /**
     * average observed max temperature
     */
    @DynamicSerializeElement
    private float maxTempMean;

    /**
     * mean observed temp for month
     */
    @DynamicSerializeElement
    private float meanTemp;

    /**
     * min observed temp, degrees F
     */
    @DynamicSerializeElement
    private int minTemp;

    /**
     * date(s) of observed minTemp
     */
    @DynamicSerializeElement
    private List<ClimateDate> dayMinTempList;

    /**
     * average observed min temp
     */
    @DynamicSerializeElement
    private float minTempMean;

    /**
     * Days of "hot" temperature, meaning 90F/higher.
     */
    @DynamicSerializeElement
    private int numMaxGreaterThan90F;

    /**
     * # of days max temp less than 32F (below freezing).
     */
    @DynamicSerializeElement
    private int numMaxLessThan32F;

    /**
     * # of days max temp greater than T1
     */
    @DynamicSerializeElement
    private int numMaxGreaterThanT1F;

    /**
     * # of days max temp greater than T2
     */
    @DynamicSerializeElement
    private int numMaxGreaterThanT2F;

    /**
     * # of days max temp less than T3
     */
    @DynamicSerializeElement
    private int numMaxLessThanT3F;

    /**
     * # of days min temp less than 32F (below freezing).
     */
    @DynamicSerializeElement
    private int numMinLessThan32F;

    /**
     * # of days min temp less than 0F
     */
    @DynamicSerializeElement
    private int numMinLessThan0F;

    /**
     * # of days min temp greater than T4
     */
    @DynamicSerializeElement
    private int numMinGreaterThanT4F;

    /**
     * # of days min temp less than T5
     */
    @DynamicSerializeElement
    private int numMinLessThanT5F;

    /**
     * # of days min temp less than T6
     */
    @DynamicSerializeElement
    private int numMinLessThanT6F;

    /**
     * cumulative precip this month (in.)
     */
    @DynamicSerializeElement
    private float precipTotal;

    /**
     * max 24 hour precip
     */
    @DynamicSerializeElement
    private float precipMax24H;

    /**
     * 24 hour period of max precip.
     */
    @DynamicSerializeElement
    private List<ClimateDates> precip24HDates;

    /**
     * max storm total precip
     */
    @DynamicSerializeElement
    private float precipStormMax;

    /**
     * start and ending periods of storm
     */
    @DynamicSerializeElement
    private List<ClimateDates> precipStormList;

    /**
     * daily average precip
     */
    @DynamicSerializeElement
    private float precipMeanDay;

    /**
     * # of days precip greater than .01 inches
     */
    @DynamicSerializeElement
    private int numPrcpGreaterThan01;

    /**
     * # of days precip greater than .10 inches
     */
    @DynamicSerializeElement
    private int numPrcpGreaterThan10;

    /**
     * # of days precip greater than .50 inches
     */
    @DynamicSerializeElement
    private int numPrcpGreaterThan50;

    /**
     * # of days precip greater than 1.00 inches
     */
    @DynamicSerializeElement
    private int numPrcpGreaterThan100;

    /**
     * # of days precip greater than P1 inches
     */
    @DynamicSerializeElement
    private int numPrcpGreaterThanP1;

    /**
     * # of days precip greater than P2 inches
     */
    @DynamicSerializeElement
    private int numPrcpGreaterThanP2;

    /**
     * total snow for month (in.)
     */
    @DynamicSerializeElement
    private float snowTotal;

    /**
     * max 24 hour snowfall
     */
    @DynamicSerializeElement
    private float snowMax24H;

    /**
     * start, end dates of max 24H snow
     */
    @DynamicSerializeElement
    private List<ClimateDates> snow24HDates;

    /**
     * max storm total snowfall
     */
    @DynamicSerializeElement
    private float snowMaxStorm;

    /**
     * start, end of max storm snow
     */
    @DynamicSerializeElement
    private List<ClimateDates> snowStormList;

    /**
     * water equivalent of snow (in.)
     */
    @DynamicSerializeElement
    private float snowWater;

    /**
     * total snowfall sinceJuly
     */
    @DynamicSerializeElement
    private float snowJuly1;

    /**
     * water equivalent of snow since July
     */
    @DynamicSerializeElement
    private float snowWaterJuly1;

    /**
     * average observed snow depth
     */
    @DynamicSerializeElement
    private float snowGroundMean;

    /**
     * maximum snow depth
     */
    @DynamicSerializeElement
    private int snowGroundMax;

    /**
     * date of maximum snow depth
     */
    @DynamicSerializeElement
    private List<ClimateDate> snowGroundMaxDateList;

    /**
     * # of days with any snowfall
     */
    @DynamicSerializeElement
    private int numSnowGreaterThanTR;

    /**
     * # of days with snow GreaterThaninch
     */
    @DynamicSerializeElement
    private int numSnowGreaterThan1;

    /**
     * # of days with snow greater than S1
     */
    @DynamicSerializeElement
    private int numSnowGreaterThanS1;

    /**
     * cumulative # of heat days this month
     */
    @DynamicSerializeElement
    private int numHeatTotal;

    /**
     * cumulative # of heat days since July
     */
    @DynamicSerializeElement
    private int numHeat1July;

    /** cumulative # of cool days this month */
    @DynamicSerializeElement
    private int numCoolTotal;

    /**
     * cumulative # of cool days since Jan
     */
    @DynamicSerializeElement
    private int numCool1Jan;

    /**
     * average wind speed
     */
    @DynamicSerializeElement
    private float avgWindSpd;

    /**
     * resultant wind direction and speed
     */
    @DynamicSerializeElement
    private ClimateWind resultWind;

    /**
     * speed and direction of the max wind
     */
    @DynamicSerializeElement
    private List<ClimateWind> maxWindList;

    /**
     * date(s) of max wind
     */
    @DynamicSerializeElement
    private List<ClimateDate> maxWindDayList;

    /**
     * speed and direction of the max gust
     */
    @DynamicSerializeElement
    private List<ClimateWind> maxGustList;

    /**
     * date(s) of max wind gust
     */
    @DynamicSerializeElement
    private List<ClimateDate> maxGustDayList;

    /** possible sunshine, % */
    @DynamicSerializeElement
    private int possSun;

    /**
     * average sky cover
     */
    @DynamicSerializeElement
    private float meanSkyCover;

    /**
     * # of days fair (0-3 tenths)
     */
    @DynamicSerializeElement
    private int numFairDays;

    /**
     * # of days partly cloudy (4-7 tenths)
     */
    @DynamicSerializeElement
    private int numPartlyCloudyDays;

    /**
     * # of days cloudy (8-10 tenths)
     */
    @DynamicSerializeElement
    private int numMostlyCloudyDays;

    /**
     * # of days with thunderstorms (T)
     */
    @DynamicSerializeElement
    private int numThunderStorms;

    /**
     * # of days with mixed precipitation (P)
     */
    @DynamicSerializeElement
    private int numMixedPrecip;

    /**
     * # of days with heavy rain (RRR)
     */
    @DynamicSerializeElement
    private int numHeavyRain;

    /**
     * # of days with rain (RR)
     */
    @DynamicSerializeElement
    private int numRain;

    /**
     * # of days with light rain (R)
     */
    @DynamicSerializeElement
    private int numLightRain;

    /**
     * # of days with freezing rain (ZRR)
     */
    @DynamicSerializeElement
    private int numFreezingRain;

    /**
     * # of days with light freezing rain (ZR)
     */
    @DynamicSerializeElement
    private int numLightFreezingRain;

    /**
     * # of days with hail (A)
     */
    @DynamicSerializeElement
    private int numHail;

    /**
     * # of days with heavy snow (SSS)
     */
    @DynamicSerializeElement
    private int numHeavySnow;

    /**
     * # of days with snow (SS)
     */
    @DynamicSerializeElement
    private int numSnow;

    /**
     * # of days with light snow (S)
     */
    @DynamicSerializeElement
    private int numLightSnow;

    /**
     * # of days with ice pellets (IP)
     */
    @DynamicSerializeElement
    private int numIcePellets;

    /**
     * # of days with fog (F)
     */
    @DynamicSerializeElement
    private int numFog;

    /**
     * # of days with fog & visibility less than .25 statute miles (FQUARTER)
     */
    @DynamicSerializeElement
    private int numFogQuarterSM;

    /**
     * # of days with haze (H)
     */
    @DynamicSerializeElement
    private int numHaze;

    /**
     * average RH, units=%
     */
    @DynamicSerializeElement
    private int meanRh;

    /**
     * early freezing date
     */
    @DynamicSerializeElement
    private ClimateDate earlyFreeze;

    /**
     * late freezing date
     */
    @DynamicSerializeElement
    private ClimateDate lateFreeze;

    /**
     * Data method fields
     */
    @DynamicSerializeElement
    private PeriodDataMethod dataMethods;

    /**
     * Empty constructor.
     */
    public PeriodData() {
    }

    /**
     * Copy constructor.
     * 
     * @param other
     */
    public PeriodData(PeriodData other) {
        informId = other.getInformId();
        maxTemp = other.getMaxTemp();
        dayMaxTempList = new ArrayList<>();
        for (ClimateDate date : other.getDayMaxTempList()) {
            dayMaxTempList.add(new ClimateDate(date));
        }
        maxTempMean = other.getMaxTempMean();
        meanTemp = other.getMeanTemp();
        minTemp = other.getMinTemp();
        dayMinTempList = new ArrayList<>();
        for (ClimateDate date : other.getDayMinTempList()) {
            dayMinTempList.add(new ClimateDate(date));
        }
        minTempMean = other.getMinTempMean();
        numMaxGreaterThan90F = other.getNumMaxGreaterThan90F();
        numMaxLessThan32F = other.getNumMaxLessThan32F();
        numMaxGreaterThanT1F = other.getNumMaxGreaterThanT1F();
        numMaxGreaterThanT2F = other.getNumMaxGreaterThanT2F();
        numMaxLessThanT3F = other.getNumMaxLessThanT3F();
        numMinLessThan32F = other.getNumMinLessThan32F();
        numMinLessThan0F = other.getNumMinLessThan0F();
        numMinGreaterThanT4F = other.getNumMinGreaterThanT4F();
        numMinLessThanT5F = other.getNumMinLessThanT5F();
        numMinLessThanT6F = other.getNumMinLessThanT6F();
        precipTotal = other.getPrecipTotal();
        precipMax24H = other.getPrecipMax24H();
        precip24HDates = new ArrayList<>();
        for (ClimateDates dates : other.getPrecip24HDates()) {
            precip24HDates.add(new ClimateDates(dates));
        }
        precipStormMax = other.getPrecipStormMax();
        precipStormList = new ArrayList<>();
        for (ClimateDates dates : other.getPrecipStormList()) {
            precipStormList.add(new ClimateDates(dates));
        }
        precipMeanDay = other.getPrecipMeanDay();
        numPrcpGreaterThan01 = other.getNumPrcpGreaterThan01();
        numPrcpGreaterThan10 = other.getNumPrcpGreaterThan10();
        numPrcpGreaterThan50 = other.getNumPrcpGreaterThan50();
        numPrcpGreaterThan100 = other.getNumPrcpGreaterThan100();
        numPrcpGreaterThanP1 = other.getNumPrcpGreaterThanP1();
        numPrcpGreaterThanP2 = other.getNumPrcpGreaterThanP2();
        snowTotal = other.getSnowTotal();
        snowMax24H = other.getSnowMax24H();
        snow24HDates = new ArrayList<>();
        for (ClimateDates dates : other.getSnow24HDates()) {
            snow24HDates.add(new ClimateDates(dates));
        }
        snowMaxStorm = other.getSnowMaxStorm();
        snowStormList = new ArrayList<>();
        for (ClimateDates dates : other.getSnowStormList()) {
            snowStormList.add(new ClimateDates(dates));
        }
        snowWater = other.getSnowWater();
        snowJuly1 = other.getSnowJuly1();
        snowWaterJuly1 = other.getSnowWaterJuly1();
        snowGroundMean = other.getSnowGroundMean();
        snowGroundMax = other.getSnowGroundMax();
        snowGroundMaxDateList = new ArrayList<>();
        for (ClimateDate date : other.getSnowGroundMaxDateList()) {
            snowGroundMaxDateList.add(new ClimateDate(date));
        }
        numSnowGreaterThanTR = other.getNumSnowGreaterThanTR();
        numSnowGreaterThan1 = other.getNumSnowGreaterThan1();
        numSnowGreaterThanS1 = other.getNumSnowGreaterThanS1();
        numHeatTotal = other.getNumHeatTotal();
        numHeat1July = other.getNumHeat1July();
        numCoolTotal = other.getNumCoolTotal();
        numCool1Jan = other.getNumCool1Jan();
        avgWindSpd = other.getAvgWindSpd();
        resultWind = new ClimateWind(other.getResultWind());
        maxWindList = new ArrayList<>();
        for (ClimateWind wind : other.getMaxWindList()) {
            maxWindList.add(new ClimateWind(wind));
        }
        maxWindDayList = new ArrayList<>();
        for (ClimateDate date : other.getMaxWindDayList()) {
            maxWindDayList.add(new ClimateDate(date));
        }
        maxGustList = new ArrayList<>();
        for (ClimateWind wind : other.getMaxGustList()) {
            maxGustList.add(new ClimateWind(wind));
        }
        maxGustDayList = new ArrayList<>();
        for (ClimateDate date : other.getMaxGustDayList()) {
            maxGustDayList.add(new ClimateDate(date));
        }
        possSun = other.getPossSun();
        meanSkyCover = other.getMeanSkyCover();
        numFairDays = other.getNumFairDays();
        numPartlyCloudyDays = other.getNumPartlyCloudyDays();
        numMostlyCloudyDays = other.getNumMostlyCloudyDays();
        numThunderStorms = other.getNumThunderStorms();
        numMixedPrecip = other.getNumMixedPrecip();
        numHeavyRain = other.getNumHeavyRain();
        numRain = other.getNumRain();
        numLightRain = other.getNumLightRain();
        numFreezingRain = other.getNumFreezingRain();
        numLightFreezingRain = other.getNumLightFreezingRain();
        numHail = other.getNumHail();
        numHeavySnow = other.getNumHeavySnow();
        numSnow = other.getNumSnow();
        numLightSnow = other.getNumLightSnow();
        numIcePellets = other.getNumIcePellets();
        numFog = other.getNumFog();
        numFogQuarterSM = other.getNumFogQuarterSM();
        numHaze = other.getNumHaze();
        meanRh = other.getMeanRh();
        earlyFreeze = new ClimateDate(other.getEarlyFreeze());
        lateFreeze = new ClimateDate(other.getLateFreeze());
        dataMethods = new PeriodDataMethod(other.getDataMethods());
    }

    /**
     * @return the informId
     */
    public int getInformId() {
        return informId;
    }

    /**
     * @param informId
     *            the informId to set
     */
    public void setInformId(int informId) {
        this.informId = informId;
    }

    /**
     * @return the maxTemp
     */
    public int getMaxTemp() {
        return maxTemp;
    }

    /**
     * @param maxTemp
     *            the maxTemp to set
     */
    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    /**
     * @return the dayMaxTempList
     */
    public List<ClimateDate> getDayMaxTempList() {
        return dayMaxTempList;
    }

    /**
     * @param dayMaxTempList
     *            the dayMaxTempList to set
     */
    public void setDayMaxTempList(List<ClimateDate> dayMaxTempList) {
        int size = dayMaxTempList.size();
        this.dayMaxTempList = size > 3 ? dayMaxTempList.subList(size - 3, size)
                : dayMaxTempList;
    }

    /**
     * @return the maxTempMean
     */
    public float getMaxTempMean() {
        return maxTempMean;
    }

    /**
     * @param maxTempMean
     *            the maxTempMean to set
     */
    public void setMaxTempMean(float maxTempMean) {
        this.maxTempMean = maxTempMean;
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
     * @return the minTemp
     */
    public int getMinTemp() {
        return minTemp;
    }

    /**
     * @param minTemp
     *            the minTemp to set
     */
    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    /**
     * @return the dayMinTempList
     */
    public List<ClimateDate> getDayMinTempList() {
        return dayMinTempList;
    }

    /**
     * @param dayMinTempList
     *            the dayMinTempList to set
     */
    public void setDayMinTempList(List<ClimateDate> dayMinTempList) {
        int size = dayMinTempList.size();
        this.dayMinTempList = size > 3 ? dayMinTempList.subList(size - 3, size)
                : dayMinTempList;
    }

    /**
     * @return the minTempMean
     */
    public float getMinTempMean() {
        return minTempMean;
    }

    /**
     * @param minTempMean
     *            the minTempMean to set
     */
    public void setMinTempMean(float minTempMean) {
        this.minTempMean = minTempMean;
    }

    /**
     * Days of "hot" temperature, meaning 90F/higher.
     * 
     * @return the numMaxGreaterThan90F
     */
    public int getNumMaxGreaterThan90F() {
        return numMaxGreaterThan90F;
    }

    /**
     * Days of "hot" temperature, meaning 90F/higher.
     * 
     * @param numMaxGreaterThan90F
     *            the numMaxGreaterThan90F to set
     */
    public void setNumMaxGreaterThan90F(int numMaxGreaterThan90F) {
        this.numMaxGreaterThan90F = numMaxGreaterThan90F;
    }

    /**
     * # of days max temp less than 32F (below freezing).
     * 
     * @return the numMaxLessThan32F
     */
    public int getNumMaxLessThan32F() {
        return numMaxLessThan32F;
    }

    /**
     * # of days max temp less than 32F (below freezing).
     * 
     * @param numMaxLessThan32F
     *            the numMaxLessThan32F to set
     */
    public void setNumMaxLessThan32F(int numMaxLessThan32F) {
        this.numMaxLessThan32F = numMaxLessThan32F;
    }

    /**
     * @return the numMaxGreaterThanT1F
     */
    public int getNumMaxGreaterThanT1F() {
        return numMaxGreaterThanT1F;
    }

    /**
     * @param numMaxGreaterThanT1F
     *            the numMaxGreaterThanT1F to set
     */
    public void setNumMaxGreaterThanT1F(int numMaxGreaterThanT1F) {
        this.numMaxGreaterThanT1F = numMaxGreaterThanT1F;
    }

    /**
     * @return the numMaxGreaterThanT2F
     */
    public int getNumMaxGreaterThanT2F() {
        return numMaxGreaterThanT2F;
    }

    /**
     * @param numMaxGreaterThanT2F
     *            the numMaxGreaterThanT2F to set
     */
    public void setNumMaxGreaterThanT2F(int numMaxGreaterThanT2F) {
        this.numMaxGreaterThanT2F = numMaxGreaterThanT2F;
    }

    /**
     * @return the numMaxLessThanT3F
     */
    public int getNumMaxLessThanT3F() {
        return numMaxLessThanT3F;
    }

    /**
     * @param numMaxLessThanT3F
     *            the numMaxLessThanT3F to set
     */
    public void setNumMaxLessThanT3F(int numMaxLessThanT3F) {
        this.numMaxLessThanT3F = numMaxLessThanT3F;
    }

    /**
     * # of days min temp less than 32F (below freezing).
     * 
     * @return the numMinLessThan32F
     */
    public int getNumMinLessThan32F() {
        return numMinLessThan32F;
    }

    /**
     * # of days min temp less than 32F (below freezing).
     * 
     * @param numMinLessThan32F
     *            the numMinLessThan32F to set
     */
    public void setNumMinLessThan32F(int numMinLessThan32F) {
        this.numMinLessThan32F = numMinLessThan32F;
    }

    /**
     * # of days min temp less than 0F
     * 
     * @return the numMinLessThan0F
     */
    public int getNumMinLessThan0F() {
        return numMinLessThan0F;
    }

    /**
     * # of days min temp less than 0F
     * 
     * @param numMinLessThan0F
     *            the numMinLessThan0F to set
     */
    public void setNumMinLessThan0F(int numMinLessThan0F) {
        this.numMinLessThan0F = numMinLessThan0F;
    }

    /**
     * @return the numMinGreaterThanT4F
     */
    public int getNumMinGreaterThanT4F() {
        return numMinGreaterThanT4F;
    }

    /**
     * @param numMinGreaterThanT4F
     *            the numMinGreaterThanT4F to set
     */
    public void setNumMinGreaterThanT4F(int numMinGreaterThanT4F) {
        this.numMinGreaterThanT4F = numMinGreaterThanT4F;
    }

    /**
     * @return the numMinLessThanT5F
     */
    public int getNumMinLessThanT5F() {
        return numMinLessThanT5F;
    }

    /**
     * @param numMinLessThanT5F
     *            the numMinLessThanT5F to set
     */
    public void setNumMinLessThanT5F(int numMinLessThanT5F) {
        this.numMinLessThanT5F = numMinLessThanT5F;
    }

    /**
     * @return the numMinLessThanT6F
     */
    public int getNumMinLessThanT6F() {
        return numMinLessThanT6F;
    }

    /**
     * @param numMinLessThanT6F
     *            the numMinLessThanT6F to set
     */
    public void setNumMinLessThanT6F(int numMinLessThanT6F) {
        this.numMinLessThanT6F = numMinLessThanT6F;
    }

    /**
     * Total precipitation.
     * 
     * @return the precipTotal
     */
    public float getPrecipTotal() {
        return precipTotal;
    }

    /**
     * Total precipitation.
     * 
     * @param precipTotal
     *            the precipTotal to set
     */
    public void setPrecipTotal(float precipTotal) {
        this.precipTotal = precipTotal;
    }

    /**
     * @return the precipMax24H
     */
    public float getPrecipMax24H() {
        return precipMax24H;
    }

    /**
     * @param precipMax24H
     *            the precipMax24H to set
     */
    public void setPrecipMax24H(float precipMax24H) {
        this.precipMax24H = precipMax24H;
    }

    /**
     * 24 hour periods of max precip.
     * 
     * @return the precip24HList
     */
    public List<ClimateDates> getPrecip24HDates() {
        return precip24HDates;
    }

    /**
     * 24 hour period of max precip.
     * 
     * @param precip24HDates
     *            the precip24HDates to set
     */
    public void setPrecip24HDates(List<ClimateDates> precip24HDates) {
        int size = precip24HDates.size();
        this.precip24HDates = size > 3 ? precip24HDates.subList(size - 3, size)
                : precip24HDates;
    }

    /**
     * @return the precipStormMax
     */
    public float getPrecipStormMax() {
        return precipStormMax;
    }

    /**
     * @param precipStormMax
     *            the precipStormMax to set
     */
    public void setPrecipStormMax(float precipStormMax) {
        this.precipStormMax = precipStormMax;
    }

    /**
     * @return the precipStormList
     */
    public List<ClimateDates> getPrecipStormList() {
        return precipStormList;
    }

    /**
     * @param precipStormList
     *            the precipStormList to set
     */
    public void setPrecipStormList(List<ClimateDates> precipStormList) {
        int size = precipStormList.size();
        this.precipStormList = size > 3
                ? precipStormList.subList(size - 3, size) : precipStormList;
    }

    /**
     * @return the precipMeanDay
     */
    public float getPrecipMeanDay() {
        return precipMeanDay;
    }

    /**
     * @param precipMeanDay
     *            the precipMeanDay to set
     */
    public void setPrecipMeanDay(float precipMeanDay) {
        this.precipMeanDay = precipMeanDay;
    }

    /**
     * # of days precip greater than .01 inches
     * 
     * @return the numPrcpGreaterThan01
     */
    public int getNumPrcpGreaterThan01() {
        return numPrcpGreaterThan01;
    }

    /**
     * # of days precip greater than .01 inches
     * 
     * @param numPrcpGreaterThan01
     *            the numPrcpGreaterThan01 to set
     */
    public void setNumPrcpGreaterThan01(int numPrcpGreaterThan01) {
        this.numPrcpGreaterThan01 = numPrcpGreaterThan01;
    }

    /**
     * # of days precip greater than .10 inches
     * 
     * @return the numPrcpGreaterThan10
     */
    public int getNumPrcpGreaterThan10() {
        return numPrcpGreaterThan10;
    }

    /**
     * # of days precip greater than .10 inches
     * 
     * @param numPrcpGreaterThan10
     *            the numPrcpGreaterThan10 to set
     */
    public void setNumPrcpGreaterThan10(int numPrcpGreaterThan10) {
        this.numPrcpGreaterThan10 = numPrcpGreaterThan10;
    }

    /**
     * # of days precip greater than .50 inches
     * 
     * @return the numPrcpGreaterThan50
     */
    public int getNumPrcpGreaterThan50() {
        return numPrcpGreaterThan50;
    }

    /**
     * # of days precip greater than .50 inches
     * 
     * @param numPrcpGreaterThan50
     *            the numPrcpGreaterThan50 to set
     */
    public void setNumPrcpGreaterThan50(int numPrcpGreaterThan50) {
        this.numPrcpGreaterThan50 = numPrcpGreaterThan50;
    }

    /**
     * # of days precip greater than 1.00 inches
     * 
     * @return the numPrcpGreaterThan100
     */
    public int getNumPrcpGreaterThan100() {
        return numPrcpGreaterThan100;
    }

    /**
     * # of days precip greater than 1.00 inches
     * 
     * @param numPrcpGreaterThan100
     *            the numPrcpGreaterThan100 to set
     */
    public void setNumPrcpGreaterThan100(int numPrcpGreaterThan100) {
        this.numPrcpGreaterThan100 = numPrcpGreaterThan100;
    }

    /**
     * @return the numPrcpGreaterThanP1
     */
    public int getNumPrcpGreaterThanP1() {
        return numPrcpGreaterThanP1;
    }

    /**
     * @param numPrcpGreaterThanP1
     *            the numPrcpGreaterThanP1 to set
     */
    public void setNumPrcpGreaterThanP1(int numPrcpGreaterThanP1) {
        this.numPrcpGreaterThanP1 = numPrcpGreaterThanP1;
    }

    /**
     * @return the numPrcpGreaterThanP2
     */
    public int getNumPrcpGreaterThanP2() {
        return numPrcpGreaterThanP2;
    }

    /**
     * @param numPrcpGreaterThanP2
     *            the numPrcpGreaterThanP2 to set
     */
    public void setNumPrcpGreaterThanP2(int numPrcpGreaterThanP2) {
        this.numPrcpGreaterThanP2 = numPrcpGreaterThanP2;
    }

    /**
     * @return the snowTotal
     */
    public float getSnowTotal() {
        return snowTotal;
    }

    /**
     * @param snowTotal
     *            the snowTotal to set
     */
    public void setSnowTotal(float snowTotal) {
        this.snowTotal = snowTotal;
    }

    /**
     * @return the snowMax24H
     */
    public float getSnowMax24H() {
        return snowMax24H;
    }

    /**
     * @param snowMax24H
     *            the snowMax24H to set
     */
    public void setSnowMax24H(float snowMax24H) {
        this.snowMax24H = snowMax24H;
    }

    /**
     * @return the snow24HDates
     */
    public List<ClimateDates> getSnow24HDates() {
        return snow24HDates;
    }

    /**
     * @param snow24hDates
     *            the snow24HDates to set
     */
    public void setSnow24HDates(List<ClimateDates> snow24hDates) {
        int size = snow24hDates.size();
        snow24HDates = size > 3 ? snow24hDates.subList(size - 3, size)
                : snow24hDates;
    }

    /**
     * @return the snowMaxStorm
     */
    public float getSnowMaxStorm() {
        return snowMaxStorm;
    }

    /**
     * @param snowMaxStorm
     *            the snowMaxStorm to set
     */
    public void setSnowMaxStorm(float snowMaxStorm) {
        this.snowMaxStorm = snowMaxStorm;
    }

    /**
     * @return the snowStormList
     */
    public List<ClimateDates> getSnowStormList() {
        return snowStormList;
    }

    /**
     * @param snowStormList
     *            the snowStormList to set
     */
    public void setSnowStormList(List<ClimateDates> snowStormList) {
        int size = snowStormList.size();
        this.snowStormList = size > 3 ? snowStormList.subList(size - 3, size)
                : snowStormList;
    }

    /**
     * @return the snowWater
     */
    public float getSnowWater() {
        return snowWater;
    }

    /**
     * @param snowWater
     *            the snowWater to set
     */
    public void setSnowWater(float snowWater) {
        this.snowWater = snowWater;
    }

    /**
     * @return the snowJuly1
     */
    public float getSnowJuly1() {
        return snowJuly1;
    }

    /**
     * @param snowJuly1
     *            the snowJuly1 to set
     */
    public void setSnowJuly1(float snowJuly1) {
        this.snowJuly1 = snowJuly1;
    }

    /**
     * @return the snowWaterJuly1
     */
    public float getSnowWaterJuly1() {
        return snowWaterJuly1;
    }

    /**
     * @param snowWaterJuly1
     *            the snowWaterJuly1 to set
     */
    public void setSnowWaterJuly1(float snowWaterJuly1) {
        this.snowWaterJuly1 = snowWaterJuly1;
    }

    /**
     * @return the snowGroundMean
     */
    public float getSnowGroundMean() {
        return snowGroundMean;
    }

    /**
     * @param snowGroundMean
     *            the snowGroundMean to set
     */
    public void setSnowGroundMean(float snowGroundMean) {
        this.snowGroundMean = snowGroundMean;
    }

    /**
     * @return the snowGroundMax
     */
    public int getSnowGroundMax() {
        return snowGroundMax;
    }

    /**
     * @param snowGroundMax
     *            the snowGroundMax to set
     */
    public void setSnowGroundMax(int snowGroundMax) {
        this.snowGroundMax = snowGroundMax;
    }

    /**
     * @return the dates of maximum snow on ground
     */
    public List<ClimateDate> getSnowGroundMaxDateList() {
        return snowGroundMaxDateList;
    }

    /**
     * @param iSnowGroundMaxDateList
     *            the dates of maximum snow on ground to set
     */
    public void setSnowGroundMaxDateList(
            List<ClimateDate> iSnowGroundMaxDateList) {
        int size = iSnowGroundMaxDateList.size();
        this.snowGroundMaxDateList = size > 3
                ? iSnowGroundMaxDateList.subList(size - 3, size)
                : iSnowGroundMaxDateList;
    }

    /**
     * @return the numSnowGreaterThanTR
     */
    public int getNumSnowGreaterThanTR() {
        return numSnowGreaterThanTR;
    }

    /**
     * @param numSnowGreaterThanTR
     *            the numSnowGreaterThanTR to set
     */
    public void setNumSnowGreaterThanTR(int numSnowGreaterThanTR) {
        this.numSnowGreaterThanTR = numSnowGreaterThanTR;
    }

    /**
     * @return the numSnowGreaterThan1
     */
    public int getNumSnowGreaterThan1() {
        return numSnowGreaterThan1;
    }

    /**
     * @param numSnowGreaterThan1
     *            the numSnowGreaterThan1 to set
     */
    public void setNumSnowGreaterThan1(int numSnowGreaterThan1) {
        this.numSnowGreaterThan1 = numSnowGreaterThan1;
    }

    /**
     * @return the numSnowGreaterThanS1
     */
    public int getNumSnowGreaterThanS1() {
        return numSnowGreaterThanS1;
    }

    /**
     * @param numSnowGreaterThanS1
     *            the numSnowGreaterThanS1 to set
     */
    public void setNumSnowGreaterThanS1(int numSnowGreaterThanS1) {
        this.numSnowGreaterThanS1 = numSnowGreaterThanS1;
    }

    /**
     * Heating days.
     * 
     * @return the numHeatTotal
     */
    public int getNumHeatTotal() {
        return numHeatTotal;
    }

    /**
     * Heating days.
     * 
     * @param numHeatTotal
     *            the numHeatTotal to set
     */
    public void setNumHeatTotal(int numHeatTotal) {
        this.numHeatTotal = numHeatTotal;
    }

    /**
     * @return the numHeat1July
     */
    public int getNumHeat1July() {
        return numHeat1July;
    }

    /**
     * @param numHeat1July
     *            the numHeat1July to set
     */
    public void setNumHeat1July(int numHeat1July) {
        this.numHeat1July = numHeat1July;
    }

    /**
     * Cooling days.
     * 
     * @return the numCoolTotal
     */
    public int getNumCoolTotal() {
        return numCoolTotal;
    }

    /**
     * Cooling days.
     * 
     * @param numCoolTotal
     *            the numCoolTotal to set
     */
    public void setNumCoolTotal(int numCoolTotal) {
        this.numCoolTotal = numCoolTotal;
    }

    /**
     * @return the numCool1Jan
     */
    public int getNumCool1Jan() {
        return numCool1Jan;
    }

    /**
     * @param numCool1Jan
     *            the numCool1Jan to set
     */
    public void setNumCool1Jan(int numCool1Jan) {
        this.numCool1Jan = numCool1Jan;
    }

    /**
     * @return the avgWindSpd
     */
    public float getAvgWindSpd() {
        return avgWindSpd;
    }

    /**
     * @param avgWindSpd
     *            the avgWindSpd to set
     */
    public void setAvgWindSpd(float avgWindSpd) {
        this.avgWindSpd = avgWindSpd;
    }

    /**
     * @return the resultWind
     */
    public ClimateWind getResultWind() {
        return resultWind;
    }

    /**
     * @param resultWind
     *            the resultWind to set
     */
    public void setResultWind(ClimateWind resultWind) {
        this.resultWind = resultWind;
    }

    /**
     * @return the maxWindList
     */
    public List<ClimateWind> getMaxWindList() {
        return maxWindList;
    }

    /**
     * @param maxWindList
     *            the maxWindList to set
     */
    public void setMaxWindList(List<ClimateWind> maxWindList) {
        int size = maxWindList.size();
        this.maxWindList = size > 3 ? maxWindList.subList(size - 3, size)
                : maxWindList;
    }

    /**
     * @return the maxWindDayList
     */
    public List<ClimateDate> getMaxWindDayList() {
        return maxWindDayList;
    }

    /**
     * @param maxWindDayList
     *            the maxWindDayList to set
     */
    public void setMaxWindDayList(List<ClimateDate> maxWindDayList) {
        int size = maxWindDayList.size();
        this.maxWindDayList = size > 3 ? maxWindDayList.subList(size - 3, size)
                : maxWindDayList;
    }

    /**
     * @return the maxGustList
     */
    public List<ClimateWind> getMaxGustList() {
        return maxGustList;
    }

    /**
     * @param maxGustList
     *            the maxGustList to set
     */
    public void setMaxGustList(List<ClimateWind> maxGustList) {
        int size = maxGustList.size();
        this.maxGustList = size > 3 ? maxGustList.subList(size - 3, size)
                : maxGustList;
    }

    /**
     * @return the maxGustDayList
     */
    public List<ClimateDate> getMaxGustDayList() {
        return maxGustDayList;
    }

    /**
     * @param maxGustDayList
     *            the maxGustDayList to set
     */
    public void setMaxGustDayList(List<ClimateDate> maxGustDayList) {
        int size = maxGustDayList.size();
        this.maxGustDayList = size > 3 ? maxGustDayList.subList(size - 3, size)
                : maxGustDayList;
    }

    /**
     * @return the possSun
     */
    public int getPossSun() {
        return possSun;
    }

    /**
     * @param possSun
     *            the possSun to set
     */
    public void setPossSun(int possSun) {
        this.possSun = possSun;
    }

    /**
     * @return the meanSkyCover
     */
    public float getMeanSkyCover() {
        return meanSkyCover;
    }

    /**
     * @param meanSkyCover
     *            the meanSkyCover to set
     */
    public void setMeanSkyCover(float meanSkyCover) {
        this.meanSkyCover = meanSkyCover;
    }

    /**
     * @return the number of fair/clear days.
     */
    public int getNumFairDays() {
        return numFairDays;
    }

    /**
     * @param iNumFairDays
     *            the number of fair/clear days to set.
     */
    public void setNumFairDays(int iNumFairDays) {
        this.numFairDays = iNumFairDays;
    }

    /**
     * @return the number of partly cloudy days.
     */
    public int getNumPartlyCloudyDays() {
        return numPartlyCloudyDays;
    }

    /**
     * @param iNumPartlyCloudyDays
     *            the number of partly cloudy days to set.
     */
    public void setNumPartlyCloudyDays(int iNumPartlyCloudyDays) {
        this.numPartlyCloudyDays = iNumPartlyCloudyDays;
    }

    /**
     * @return the number of mostly cloudy days.
     */
    public int getNumMostlyCloudyDays() {
        return numMostlyCloudyDays;
    }

    /**
     * @param iNumMostlyCloudyDays
     *            the number of mostly cloudy days to set
     */
    public void setNumMostlyCloudyDays(int iNumMostlyCloudyDays) {
        this.numMostlyCloudyDays = iNumMostlyCloudyDays;
    }

    /**
     * @return the numThunderStorms
     */
    public int getNumThunderStorms() {
        return numThunderStorms;
    }

    /**
     * @param numThunderStorms
     *            the numT to set
     */
    public void setNumThunderStorms(int numThunderStorms) {
        this.numThunderStorms = numThunderStorms;
    }

    /**
     * @return the numMixedPrecip
     */
    public int getNumMixedPrecip() {
        return numMixedPrecip;
    }

    /**
     * @param numMixedPrecip
     *            the numMixedPrecip to set
     */
    public void setNumMixedPrecip(int numMixedPrecip) {
        this.numMixedPrecip = numMixedPrecip;
    }

    /**
     * @return the numHeavyRain
     */
    public int getNumHeavyRain() {
        return numHeavyRain;
    }

    /**
     * @param numHeavyRain
     *            the numHeavyRain to set
     */
    public void setNumHeavyRain(int numHeavyRain) {
        this.numHeavyRain = numHeavyRain;
    }

    /**
     * @return the numRain
     */
    public int getNumRain() {
        return numRain;
    }

    /**
     * @param numRain
     *            the numRain to set
     */
    public void setNumRain(int numRain) {
        this.numRain = numRain;
    }

    /**
     * @return the numLightRain
     */
    public int getNumLightRain() {
        return numLightRain;
    }

    /**
     * @param numLightRain
     *            the numLightRain to set
     */
    public void setNumLightRain(int numLightRain) {
        this.numLightRain = numLightRain;
    }

    /**
     * @return the numFreezingRain
     */
    public int getNumFreezingRain() {
        return numFreezingRain;
    }

    /**
     * @param numFreezingRain
     *            the numFreezingRain to set
     */
    public void setNumFreezingRain(int numFreezingRain) {
        this.numFreezingRain = numFreezingRain;
    }

    /**
     * @return the numLightFreezingRain
     */
    public int getNumLightFreezingRain() {
        return numLightFreezingRain;
    }

    /**
     * @param numLightFreezingRain
     *            the numLightFreezingRain to set
     */
    public void setNumLightFreezingRain(int numLightFreezingRain) {
        this.numLightFreezingRain = numLightFreezingRain;
    }

    /**
     * @return the numHail
     */
    public int getNumHail() {
        return numHail;
    }

    /**
     * @param numHail
     *            the numHail to set
     */
    public void setNumHail(int numHail) {
        this.numHail = numHail;
    }

    /**
     * @return the numHeavySnow
     */
    public int getNumHeavySnow() {
        return numHeavySnow;
    }

    /**
     * @param numHeavySnow
     *            the numHeavySnow to set
     */
    public void setNumHeavySnow(int numHeavySnow) {
        this.numHeavySnow = numHeavySnow;
    }

    /**
     * @return the numSnow
     */
    public int getNumSnow() {
        return numSnow;
    }

    /**
     * @param numSnow
     *            the numSnow to set
     */
    public void setNumSnow(int numSnow) {
        this.numSnow = numSnow;
    }

    /**
     * @return the numLightSnow
     */
    public int getNumLightSnow() {
        return numLightSnow;
    }

    /**
     * @param numLightSnow
     *            the numLightSnow to set
     */
    public void setNumLightSnow(int numLightSnow) {
        this.numLightSnow = numLightSnow;
    }

    /**
     * @return the numIcePellets
     */
    public int getNumIcePellets() {
        return numIcePellets;
    }

    /**
     * @param numIcePellets
     *            the numIcePellets to set
     */
    public void setNumIcePellets(int numIcePellets) {
        this.numIcePellets = numIcePellets;
    }

    /**
     * @return the numFog
     */
    public int getNumFog() {
        return numFog;
    }

    /**
     * @param numFog
     *            the numFog to set
     */
    public void setNumFog(int numFog) {
        this.numFog = numFog;
    }

    /**
     * @return the numFogQuarterSM
     */
    public int getNumFogQuarterSM() {
        return numFogQuarterSM;
    }

    /**
     * @param numFogQuarterSM
     *            the numFogQuarterSM to set
     */
    public void setNumFogQuarterSM(int numFogQuarterSM) {
        this.numFogQuarterSM = numFogQuarterSM;
    }

    /**
     * @return the numHaze
     */
    public int getNumHaze() {
        return numHaze;
    }

    /**
     * @param numHaze
     *            the numHaze to set
     */
    public void setNumHaze(int numHaze) {
        this.numHaze = numHaze;
    }

    /**
     * @return the meanRh
     */
    public int getMeanRh() {
        return meanRh;
    }

    /**
     * @param meanRh
     *            the meanRh to set
     */
    public void setMeanRh(int meanRh) {
        this.meanRh = meanRh;
    }

    /**
     * @return the earlyFreeze
     */
    public ClimateDate getEarlyFreeze() {
        return earlyFreeze;
    }

    /**
     * @param earlyFreeze
     *            the earlyFreeze to set
     */
    public void setEarlyFreeze(ClimateDate earlyFreeze) {
        this.earlyFreeze = earlyFreeze;
    }

    /**
     * @return the lateFreeze
     */
    public ClimateDate getLateFreeze() {
        return lateFreeze;
    }

    /**
     * @param lateFreeze
     *            the lateFreeze to set
     */
    public void setLateFreeze(ClimateDate lateFreeze) {
        this.lateFreeze = lateFreeze;
    }

    /**
     * @return the dataMethods
     */
    public PeriodDataMethod getDataMethods() {
        return dataMethods;
    }

    /**
     * @param dataMethods
     *            the dataMethods to set
     */
    public void setDataMethods(PeriodDataMethod dataMethods) {
        this.dataMethods = dataMethods;
    }

    public void setDataToMissing() {
        /* stationId number */
        this.informId = -1 * ParameterFormatClimate.MISSING;
        /*
         * max observed temp, degrees F
         */
        this.maxTemp = ParameterFormatClimate.MISSING;
        /*
         * average observed max temperature
         */
        this.maxTempMean = ParameterFormatClimate.MISSING;
        /*
         * mean observed temp for month
         */
        this.meanTemp = ParameterFormatClimate.MISSING;
        /*
         * min observed temp, degrees F
         */
        this.minTemp = ParameterFormatClimate.MISSING;
        /*
         * average observed min temp
         */
        this.minTempMean = ParameterFormatClimate.MISSING;
        /*
         * # of days max temp GreaterThan 90F
         */
        this.numMaxGreaterThan90F = ParameterFormatClimate.MISSING;
        /*
         * # of days max temp less than 32F
         */
        this.numMaxLessThan32F = ParameterFormatClimate.MISSING;
        /*
         * # of days max temp GreaterThan T1
         */
        this.numMaxGreaterThanT1F = ParameterFormatClimate.MISSING;
        /*
         * # of days max temp GreaterThan T2
         */
        this.numMaxGreaterThanT2F = ParameterFormatClimate.MISSING;
        /*
         * # of days max temp less than T3
         */
        this.numMaxLessThanT3F = ParameterFormatClimate.MISSING;
        /*
         * # of days min temp less than 32F
         */
        this.numMinLessThan32F = ParameterFormatClimate.MISSING;
        /*
         * # of days min temp LessThan 0F
         */
        this.numMinLessThan0F = ParameterFormatClimate.MISSING;
        /*
         * # of days min temp GreaterThan T4
         */
        this.numMinGreaterThanT4F = ParameterFormatClimate.MISSING;
        /*
         * # of days min temp less than T5
         */
        this.numMinLessThanT5F = ParameterFormatClimate.MISSING;
        /*
         * # of days min temp less than T6
         */
        this.numMinLessThanT6F = ParameterFormatClimate.MISSING;
        /*
         * cumulative precip this month (in.)
         */
        this.precipTotal = ParameterFormatClimate.MISSING_PRECIP;
        /*
         * max 24 hour precip
         */
        this.precipMax24H = ParameterFormatClimate.MISSING_PRECIP;
        /*
         * max storm total precip
         */
        this.precipStormMax = ParameterFormatClimate.MISSING_PRECIP;
        /*
         * daily average precip
         */
        this.precipMeanDay = ParameterFormatClimate.MISSING_PRECIP;
        /*
         * # of days precip GreaterThan .01 inches
         */
        this.numPrcpGreaterThan01 = ParameterFormatClimate.MISSING;
        /*
         * # of days precip GreaterThan .10 inches
         */
        this.numPrcpGreaterThan10 = ParameterFormatClimate.MISSING;
        /*
         * # of days precip GreaterThan .50 inches
         */
        this.numPrcpGreaterThan50 = ParameterFormatClimate.MISSING;
        /*
         * # of days precip GreaterThan 1.00 inches
         */
        this.numPrcpGreaterThan100 = ParameterFormatClimate.MISSING;
        /*
         * # of days precip GreaterThan P1 inches
         */
        this.numPrcpGreaterThanP1 = ParameterFormatClimate.MISSING;
        /*
         * # of days precip GreaterThan P2 inches
         */
        this.numPrcpGreaterThanP2 = ParameterFormatClimate.MISSING;
        /*
         * total snow for month (in.)
         */
        this.snowTotal = ParameterFormatClimate.MISSING_SNOW;
        /*
         * max 24 hour snowfall
         */
        this.snowMax24H = ParameterFormatClimate.MISSING_SNOW;
        /*
         * max storm total snowfall
         */
        this.snowMaxStorm = ParameterFormatClimate.MISSING_SNOW;
        /*
         * water equivalent of snow (in.)
         */
        this.snowWater = ParameterFormatClimate.MISSING_SNOW;
        /*
         * total snowfall since 1 July
         */
        this.snowJuly1 = ParameterFormatClimate.MISSING_SNOW;
        /*
         * water equivalent of snow since July 1
         */
        this.snowWaterJuly1 = ParameterFormatClimate.MISSING_SNOW;
        /*
         * average observed snow depth
         */
        this.snowGroundMean = ParameterFormatClimate.MISSING_SNOW;
        /*
         * maximum snow depth
         */
        this.snowGroundMax = ParameterFormatClimate.MISSING;
        /*
         * # of days with any snowfall
         */
        this.numSnowGreaterThanTR = ParameterFormatClimate.MISSING;
        /*
         * # of days with snow GreaterThan 1 inch
         */
        this.numSnowGreaterThan1 = ParameterFormatClimate.MISSING;
        /*
         * # of days with snow GreaterThan S1
         */
        this.numSnowGreaterThanS1 = ParameterFormatClimate.MISSING;
        /*
         * cumulative # of heat days this month
         */
        this.numHeatTotal = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * cumlative # of heat days since 1 July
         */
        this.numHeat1July = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * cumulative # of cool days this month
         */
        this.numCoolTotal = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * cumulative # of cool days since 1 Jan
         */
        this.numCool1Jan = ParameterFormatClimate.MISSING_DEGREE_DAY;
        /*
         * average wind speed
         */
        this.avgWindSpd = ParameterFormatClimate.MISSING;
        /*
         * resultant wind direction and speed
         */
        this.resultWind = ClimateWind.getMissingClimateWind();
        /*
         * possible sunshine, %
         */
        this.possSun = ParameterFormatClimate.MISSING;
        /*
         * average sky cover
         */
        this.meanSkyCover = ParameterFormatClimate.MISSING;
        /*
         * # of days fair (0-3 tenths)
         */
        this.numFairDays = ParameterFormatClimate.MISSING;
        /*
         * # of days partly cloudy (4-7 tenths)
         */
        this.numPartlyCloudyDays = ParameterFormatClimate.MISSING;
        /*
         * # of days cloudy (8-10 tenths)
         */
        this.numMostlyCloudyDays = ParameterFormatClimate.MISSING;
        /*
         * # of days with thunderstorms
         */
        this.numThunderStorms = ParameterFormatClimate.MISSING;
        /*
         * # of days with mixed precipitation
         */
        this.numMixedPrecip = ParameterFormatClimate.MISSING;
        /*
         * # of days with heavy rain
         */
        this.numHeavyRain = ParameterFormatClimate.MISSING;
        /* # of days with rain */
        this.numRain = ParameterFormatClimate.MISSING;
        /*
         * # of days with light rain
         */
        this.numLightRain = ParameterFormatClimate.MISSING;
        /*
         * # of days with freezing rain
         */
        this.numFreezingRain = ParameterFormatClimate.MISSING;
        /*
         * # of days with light freezing rain
         */
        this.numLightFreezingRain = ParameterFormatClimate.MISSING;
        /* # of days with hail */
        this.numHail = ParameterFormatClimate.MISSING;
        /*
         * # of days with heavy snow
         */
        this.numHeavySnow = ParameterFormatClimate.MISSING;
        /* # of days with snow */
        this.numSnow = ParameterFormatClimate.MISSING;
        /*
         * # of days with light snow
         */
        this.numLightSnow = ParameterFormatClimate.MISSING;
        /*
         * # of days with ice pellets
         */
        this.numIcePellets = ParameterFormatClimate.MISSING;
        /* # of days with fog */
        this.numFog = ParameterFormatClimate.MISSING;
        /*
         * # of days with fog & visibility less than .25
         */
        this.numFogQuarterSM = ParameterFormatClimate.MISSING;
        /* # of days with haze */
        this.numHaze = ParameterFormatClimate.MISSING;
        /* average RH, units=% */
        this.meanRh = ParameterFormatClimate.MISSING;
        this.earlyFreeze = ClimateDate.getMissingClimateDate();
        this.lateFreeze = ClimateDate.getMissingClimateDate();
        this.dataMethods = PeriodDataMethod.getMissingPeriodDataMethod();

        this.dayMaxTempList = new ArrayList<>();
        this.precip24HDates = new ArrayList<>();
        this.snowGroundMaxDateList = new ArrayList<>();
        this.dayMinTempList = new ArrayList<>();
        this.maxWindDayList = new ArrayList<>();
        this.maxGustDayList = new ArrayList<>();
        this.maxWindList = new ArrayList<>();
        this.maxGustList = new ArrayList<>();
        this.precipStormList = new ArrayList<>();
        this.snow24HDates = new ArrayList<>();
        this.snowStormList = new ArrayList<>();
    }

    /**
     * @return new {@link PeriodData} instance with all data set to missing.
     */
    public static PeriodData getMissingPeriodData() {
        PeriodData periodData = new PeriodData();
        periodData.setDataToMissing();
        return periodData;
    }

}

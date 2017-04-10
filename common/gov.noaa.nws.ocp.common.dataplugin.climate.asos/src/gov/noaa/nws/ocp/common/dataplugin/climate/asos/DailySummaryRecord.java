/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.asos;

import java.util.Calendar;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * DailySummaryRecord is the Data Access component for Climate ASOS DailySummary
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date           Ticket#    Engineer    Description
 * -------------- ---------- ----------- --------------------------
 * March 22, 2016 16962      pwang       Initial creation for Climate
 *                                       Data plugin
 * 25 JAN 2017    23221      amoore      Default times to the String "null", or 
 *                                       insert/update will fail.
 * 13 APR 2017    33104      amoore      Address comments from review.
 * 05 MAY 2017    33104      amoore      Use common functionality.
 * </pre>
 * 
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class DailySummaryRecord extends ClimateASOSMessageRecord {

    /**
     * Table to use.
     */
    private static final String TABLE_NAME = "cli_asos_daily";

    /**
     * maps to (COR) field in DSM.pdf documentation
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean correction = false;

    /**
     * maps to ZZZZ for intermediate daily summary message, MISSING_VAL_4_DIGITS
     * when not intermediate report
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short messageValidTime;

    /**
     * have to derive from DSM creation time in the text database (Note Local
     * Standard Time time zone)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short year;

    /**
     * maps to DaDa field (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short day;

    /**
     * maps to MoMo field (01-12)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short month;

    /**
     * maps to TxTxTx field, max T in calendar day, reported in whole degrees
     * Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short maxT;

    /**
     * reported in hours and minutes, Local Standard Time, using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String maxTTime;

    /**
     * maps to TnTnTn field, min T in calendar day, reported in whole degrees
     * Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short minT;

    /**
     * reported in hours and minutes, Local Standard Time, using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String minTTime;

    /**
     * maps to MMM, max T from 7 a.m. to 7 p.m. Local Standard Time, in whole
     * degrees Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short maxTDaytime;

    /**
     * maps to NNN, max T from 19:00 previous calendar day to 08:00 Local
     * Standard Time, in whole degrees Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short minTNight;

    /**
     * maps to SLPmm, reported to the nearest 0.01 inches of Hg
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float minSeaLevelPressure;

    /**
     * maps to time after SLPmm, Local Standard Time, using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String minSeaLevelPressureTime;

    /**
     * maps to PPPP, reported in hundredths of an inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float totalPrecip;

    /**
     * maps to PnPPP, reported in hundredths of a inch from 0x:00 through 0x:59
     * Local Standard Time. 24 hours of the day.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float hourlyPrecip[] = new float[24];

    /**
     * maps to FaFaFa, reported in tenths of miles per hour (mph)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float windSpeed2MinAvg;

    /**
     * maps to dd, Direction of the 2-minute fastest wind speed, reported in
     * tens of degrees
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short windDirection2MinFastest;

    /**
     * maps to fff, Speed of the 2-minute fastest wind speed, reported in mph
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short windSpeed2MinFastest;

    /**
     * maps to tttt, Time of the 2-minute fastest wind speed, in hours and
     * minutes (Local Standard Time) using 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String windSpeed2MinFastestTime;

    /**
     * maps to DD, Direction of the day's peak wind, reported in tens of degrees
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short peakWindDirection;

    /**
     * maps to FFF, Speed of the day's peak wind, reported in mph
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short peakWindSpeed;

    /**
     * maps to TTTT, peak wind time, reported in hours and minutes (Local
     * Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String peakWindTime;

    /**
     * maps to WWWWW, Weather occurrence symbols. (DSM.pdf say 1, 2, 3, 4, 5, 7,
     * 8, 9, X (as 10) are currently available, though 6 was seen in some test
     * msg possible?). Up to 5 symbols.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short[] wxSymbol = new short[5];

    /**
     * maps to SSS, Minutes of sunshine, in whole minutes (when available or if
     * augmentation is available)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short sunshineMinutes;

    /**
     * maps to SpSpSp, Percentage of sunshine observed, to the nearest whole
     * percent (when available or augmented)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short sunshinePercent;

    /**
     * maps to SwSwSw, reported in tenths of an inch (when available or
     * augmented)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float snowAmount;

    /**
     * maps to DDD, reported in whole inches (when available or augmented)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short depthOfSnow;

    /**
     * maps to CsCs, Average daily sky cover from sunrise to sunset, in tenths
     * of sky cover (when available or augmented)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float skyCoverDaytime;

    /**
     * maps to CmCm, Average daily sky cover, midnight to midnight Local
     * Standard Time, in tenths of sky cover (when available or augmented)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float skyCoverWholeDay;

    /**
     * maps to (Remarks)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String remarks;

    public DailySummaryRecord() {
        /*
         * set all the default/missing values, based on DSM documentation,
         * legacy C++ code, and possibly expected missing values in DB
         */
        messageValidTime = MISSING_VAL_4_DIGITS;
        Calendar now = TimeUtil.newCalendar();
        year = (short) now.get(Calendar.YEAR);
        day = 0;
        month = 0;
        maxT = MISSING_VAL_4_DIGITS;
        maxTTime = MISSING_VAL_TIME;
        minT = MISSING_VAL_4_DIGITS;
        minTTime = MISSING_VAL_TIME;
        maxTDaytime = MISSING_VAL_4_DIGITS;
        minTNight = MISSING_VAL_4_DIGITS;
        minSeaLevelPressure = MISSING_VAL_4_DIGITS;
        minSeaLevelPressureTime = MISSING_VAL_TIME;
        totalPrecip = MISSING_VAL_4_DIGITS;

        for (int i = 0; i < hourlyPrecip.length; i++) {
            hourlyPrecip[i] = MISSING_VAL_4_DIGITS;
        }

        windSpeed2MinAvg = MISSING_VAL_4_DIGITS;
        windDirection2MinFastest = MISSING_VAL_4_DIGITS;
        windSpeed2MinFastest = MISSING_VAL_4_DIGITS;
        windSpeed2MinFastestTime = MISSING_VAL_TIME;
        peakWindDirection = MISSING_VAL_4_DIGITS;
        peakWindSpeed = MISSING_VAL_4_DIGITS;
        peakWindTime = MISSING_VAL_TIME;

        for (int i = 0; i < wxSymbol.length; i++) {
            wxSymbol[i] = MISSING_VAL_4_DIGITS;
        }

        sunshineMinutes = MISSING_VAL_4_DIGITS;
        sunshinePercent = MISSING_VAL_4_DIGITS;
        snowAmount = MISSING_VAL_4_DIGITS;
        depthOfSnow = MISSING_VAL_4_DIGITS;
        skyCoverDaytime = MISSING_VAL_4_DIGITS;
        skyCoverWholeDay = MISSING_VAL_4_DIGITS;

        // from legacy C++ code, "MM" because DB table expects that
        remarks = "MM";
    }

    /**
     * @return the correction
     */
    public boolean isCorrection() {
        return correction;
    }

    /**
     * @param correction
     *            the correction to set
     */
    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    /**
     * @return the messageValidTime
     */
    public short getMessageValidTime() {
        return messageValidTime;
    }

    /**
     * @param messageValidTime
     *            the messageValidTime to set
     */
    public void setMessageValidTime(short messageValidTime) {
        this.messageValidTime = messageValidTime;
    }

    /**
     * @return the year
     */
    public short getYear() {
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(short year) {
        this.year = year;
    }

    /**
     * @return the day
     */
    public short getDay() {
        return day;
    }

    /**
     * @param day
     *            the day to set
     */
    public void setDay(short day) {
        this.day = day;
    }

    /**
     * @return the month
     */
    public short getMonth() {
        return month;
    }

    /**
     * @param month
     *            the month to set
     */
    public void setMonth(short month) {
        this.month = month;
        this.year = this.getAsosSummaryMessageGenerationYear(month);
    }

    /**
     * @return the maxT
     */
    public short getMaxT() {
        return maxT;
    }

    /**
     * @param maxT
     *            the maxT to set
     */
    public void setMaxT(short maxT) {
        this.maxT = maxT;
    }

    /**
     * @return the maxTTime
     */
    public String getMaxTTime() {
        return maxTTime;
    }

    /**
     * @param maxTTime
     *            the maxTTime to set
     */
    public void setMaxTTime(String maxTTime) {

        this.maxTTime = maxTTime;
    }

    /**
     * @return the minT
     */
    public short getMinT() {
        return minT;
    }

    /**
     * @param minT
     *            the minT to set
     */
    public void setMinT(short minT) {
        this.minT = minT;
    }

    /**
     * @return the minTTime
     */
    public String getMinTTime() {
        return minTTime;
    }

    /**
     * @param minTTime
     *            the minTTime to set
     */
    public void setMinTTime(String minTTime) {
        this.minTTime = minTTime;
    }

    /**
     * @return the maxTDaytime
     */
    public short getMaxTDaytime() {
        return maxTDaytime;
    }

    /**
     * @param maxTDaytime
     *            the maxTDaytime to set
     */
    public void setMaxTDaytime(short maxTDaytime) {
        this.maxTDaytime = maxTDaytime;
    }

    /**
     * @return the minTNight
     */
    public short getMinTNight() {
        return minTNight;
    }

    /**
     * @param minTNight
     *            the minTNight to set
     */
    public void setMinTNight(short minTNight) {
        this.minTNight = minTNight;
    }

    /**
     * @return the minSeaLevelPressure
     */
    public float getMinSeaLevelPressure() {
        return minSeaLevelPressure;
    }

    /**
     * @param minSeaLevelPressure
     *            the minSeaLevelPressure to set
     */
    public void setMinSeaLevelPressure(float minSeaLevelPressure) {
        this.minSeaLevelPressure = minSeaLevelPressure;
    }

    /**
     * @return the minSeaLevelPressureTime
     */
    public String getMinSeaLevelPressureTime() {
        return minSeaLevelPressureTime;
    }

    /**
     * @param minSeaLevelPressureTime
     *            the minSeaLevelPressureTime to set
     */
    public void setMinSeaLevelPressureTime(String minSeaLevelPressureTime) {
        this.minSeaLevelPressureTime = minSeaLevelPressureTime;
    }

    /**
     * @return the totalPrecip
     */
    public float getTotalPrecip() {
        return totalPrecip;
    }

    /**
     * @param totalPrecip
     *            the totalPrecip to set
     */
    public void setTotalPrecip(float totalPrecip) {
        this.totalPrecip = totalPrecip;
    }

    /**
     * @return the hourlyPrecip 24-hour array
     */
    public float[] getHourlyPrecip() {
        return hourlyPrecip;
    }

    /**
     * @param hourlyPrecip
     *            the hourlyPrecip 24-hour array to set
     */
    public void setHourlyPrecip(float[] hourlyPrecip) {
        this.hourlyPrecip = hourlyPrecip;
    }

    /**
     * @return the windSpeed2MinAvg
     */
    public float getWindSpeed2MinAvg() {
        return windSpeed2MinAvg;
    }

    /**
     * @param windSpeed2MinAvg
     *            the windSpeed2MinAvg to set
     */
    public void setWindSpeed2MinAvg(float windSpeed2MinAvg) {
        this.windSpeed2MinAvg = windSpeed2MinAvg;
    }

    /**
     * @return the windDirection2MinFastest
     */
    public short getWindDirection2MinFastest() {
        return windDirection2MinFastest;
    }

    /**
     * @param windDirection2MinFastest
     *            the windDirection2MinFastest to set
     */
    public void setWindDirection2MinFastest(short windDirection2MinFastest) {
        this.windDirection2MinFastest = windDirection2MinFastest;
    }

    /**
     * @return the windSpeed2MinFastest
     */
    public short getWindSpeed2MinFastest() {
        return windSpeed2MinFastest;
    }

    /**
     * @param windSpeed2MinFastest
     *            the windSpeed2MinFastest to set
     */
    public void setWindSpeed2MinFastest(short windSpeed2MinFastest) {
        this.windSpeed2MinFastest = windSpeed2MinFastest;
    }

    /**
     * @return the windSpeed2MinFastestTime
     */
    public String getWindSpeed2MinFastestTime() {
        return windSpeed2MinFastestTime;
    }

    /**
     * @param windSpeed2MinFastestTime
     *            the windSpeed2MinFastestTime to set
     */
    public void setWindSpeed2MinFastestTime(String windSpeed2MinFastestTime) {
        this.windSpeed2MinFastestTime = windSpeed2MinFastestTime;
    }

    /**
     * @return the peakWindDirection
     */
    public short getPeakWindDirection() {
        return peakWindDirection;
    }

    /**
     * @param peakWindDirection
     *            the peakWindDirection to set
     */
    public void setPeakWindDirection(short peakWindDirection) {
        this.peakWindDirection = peakWindDirection;
    }

    /**
     * @return the peakWindSpeed
     */
    public short getPeakWindSpeed() {
        return peakWindSpeed;
    }

    /**
     * @param peakWindSpeed
     *            the peakWindSpeed to set
     */
    public void setPeakWindSpeed(short peakWindSpeed) {
        this.peakWindSpeed = peakWindSpeed;
    }

    /**
     * @return the peakWindTime
     */
    public String getPeakWindTime() {
        return peakWindTime;
    }

    /**
     * @param peakWindTime
     *            the peakWindTime to set
     */
    public void setPeakWindTime(String peakWindTime) {
        this.peakWindTime = peakWindTime;
    }

    /**
     * @return the wxSymbols
     */
    public short[] getWxSymbol() {
        return wxSymbol;
    }

    /**
     * @param wxSymbols
     *            the wxSymbols to set
     */
    public void setWxSymbol(short[] wxSymbols) {
        this.wxSymbol = wxSymbols;
    }

    /**
     * @return the sunshineMinutes
     */
    public short getSunshineMinutes() {
        return sunshineMinutes;
    }

    /**
     * @param sunshineMinutes
     *            the sunshineMinutes to set
     */
    public void setSunshineMinutes(short sunshineMinutes) {
        this.sunshineMinutes = sunshineMinutes;
    }

    /**
     * @return the sunshinePercent
     */
    public short getSunshinePercent() {
        return sunshinePercent;
    }

    /**
     * @param sunshinePercent
     *            the sunshinePercent to set
     */
    public void setSunshinePercent(short sunshinePercent) {
        this.sunshinePercent = sunshinePercent;
    }

    /**
     * @return the snowAmount
     */
    public float getSnowAmount() {
        return snowAmount;
    }

    /**
     * @param snowAmount
     *            the snowAmount to set
     */
    public void setSnowAmount(float snowAmount) {
        this.snowAmount = snowAmount;
    }

    /**
     * @return the depthOfSnow
     */
    public short getDepthOfSnow() {
        return depthOfSnow;
    }

    /**
     * @param depthOfSnow
     *            the depthOfSnow to set
     */
    public void setDepthOfSnow(short depthOfSnow) {
        this.depthOfSnow = depthOfSnow;
    }

    /**
     * @return the skyCoverDaytime
     */
    public float getSkyCoverDaytime() {
        return skyCoverDaytime;
    }

    /**
     * @param skyCoverDaytime
     *            the skyCoverDaytime to set
     */
    public void setSkyCoverDaytime(float skyCoverDaytime) {
        this.skyCoverDaytime = skyCoverDaytime;
    }

    /**
     * @return the skyCoverWholeDay
     */
    public float getSkyCoverWholeDay() {
        return skyCoverWholeDay;
    }

    /**
     * @param skyCoverWholeDay
     *            the skyCoverWholeDay to set
     */
    public void setSkyCoverWholeDay(float skyCoverWholeDay) {
        this.skyCoverWholeDay = skyCoverWholeDay;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks
     *            the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Get MM-DD format of day_of_year
     * 
     * @return String
     */
    public String getDayOfYear() {
        return new ClimateDate(day, month).toMonthDayDateString();
    }

    @Override
    public String queryExistingRecordSQL(Map<String, Object> queryParams) {
        StringBuilder sb = new StringBuilder("SELECT count(*) FROM ");
        sb.append(TABLE_NAME);
        sb.append(" WHERE day_of_year=:day_of_year");
        queryParams.put("day_of_year", getDayOfYear());
        sb.append(" AND station_code=:station_code");
        queryParams.put("station_code", getStationCode());
        return sb.toString();

    }

    @Override
    public String toInsertSQL(Map<String, Object> queryParams) {
        // TODO need to handle Time without time zone for parameterization
        StringBuilder sb = new StringBuilder("INSERT INTO ");

        sb.append(TABLE_NAME);
        sb.append(" VALUES(");
        if (getStationCode() != null && !getStationCode().isEmpty()) {
            queryParams.put("stationId", getStationCode());
            sb.append(":stationId");
        } else {
            return null;
        }
        // valid_time is not used in this case
        sb.append(",NULL");
        sb.append(",");
        sb.append(year);
        if (getDayOfYear().length() == 5) {
            queryParams.put("dayOfYear", getDayOfYear());
            sb.append(",:dayOfYear");
        } else {
            return null;
        }

        sb.append(",:maxT");
        queryParams.put("maxT", maxT);

        if (maxTTime != null) {
            sb.append(",'");
            sb.append(maxTTime);
            sb.append("'");
        } else {
            sb.append(",NULL");
        }

        sb.append(",:minT");
        queryParams.put("minT", minT);

        if (minTTime != null) {
            sb.append(",'");
            sb.append(minTTime);
            sb.append("'");
        } else {
            sb.append(",NULL");
        }

        sb.append(",:maxTDaytime");
        queryParams.put("maxTDaytime", maxTDaytime);

        sb.append(",:minTNight");
        queryParams.put("minTNight", minTNight);

        sb.append(",:minSeaLevelPressure");
        queryParams.put("minSeaLevelPressure", minSeaLevelPressure);

        if (minSeaLevelPressureTime != null) {
            sb.append(",'");
            sb.append(minSeaLevelPressureTime);
            sb.append("'");
        } else {
            sb.append(",NULL");
        }

        sb.append(",:totalPrecip");
        queryParams.put("totalPrecip", totalPrecip);

        // hourly precip
        for (int i = 0; i < hourlyPrecip.length; i++) {
            String hourlyPrecipParamName = "hourlyPrecip" + (i + 1);

            sb.append(",:");
            sb.append(hourlyPrecipParamName);
            queryParams.put(hourlyPrecipParamName, hourlyPrecip[i]);
        }

        sb.append(",:windSpeed2MinAvg");
        queryParams.put("windSpeed2MinAvg", windSpeed2MinAvg);

        sb.append(",:windDirection2MinFastest");
        queryParams.put("windDirection2MinFastest", windDirection2MinFastest);

        sb.append(",:windSpeed2MinFastest");
        queryParams.put("windSpeed2MinFastest", windSpeed2MinFastest);

        if (windSpeed2MinFastestTime != null) {
            sb.append(",'");
            sb.append(windSpeed2MinFastestTime);
            sb.append("'");
        } else {
            sb.append(",NULL");
        }

        sb.append(",:peakWindDirection");
        queryParams.put("peakWindDirection", peakWindDirection);

        sb.append(",:peakWindSpeed");
        queryParams.put("peakWindSpeed", peakWindSpeed);

        if (peakWindTime != null) {
            sb.append(",'");
            sb.append(peakWindTime);
            sb.append("'");
        } else {
            sb.append(",NULL");
        }

        sb.append(",:wxSymbol1");
        queryParams.put("wxSymbol1", wxSymbol[0]);

        sb.append(",:wxSymbol2");
        queryParams.put("wxSymbol2", wxSymbol[1]);

        sb.append(",:wxSymbol3");
        queryParams.put("wxSymbol3", wxSymbol[2]);

        sb.append(",:wxSymbol4");
        queryParams.put("wxSymbol4", wxSymbol[3]);

        sb.append(",:wxSymbol5");
        queryParams.put("wxSymbol5", wxSymbol[4]);

        sb.append(",:sunshineMinutes");
        queryParams.put("sunshineMinutes", sunshineMinutes);

        sb.append(",:sunshinePercent");
        queryParams.put("sunshinePercent", sunshinePercent);

        sb.append(",:snowAmount");
        queryParams.put("snowAmount", snowAmount);

        sb.append(",:depthOfSnow");
        queryParams.put("depthOfSnow", depthOfSnow);

        sb.append(",:skyCoverDaytime");
        queryParams.put("skyCoverDaytime", skyCoverDaytime);

        sb.append(",:skyCoverWholeDay");
        queryParams.put("skyCoverWholeDay", skyCoverWholeDay);

        sb.append(",:remarks");
        queryParams.put("remarks", remarks);

        sb.append(")");
        return sb.toString();

    }

    @Override
    public String toUpdateSQL(Map<String, Object> queryParams) {
        // TODO need to handle Time without time zone for parameterization
        StringBuilder sb = new StringBuilder("UPDATE ");

        sb.append(TABLE_NAME);
        sb.append(" SET ");

        sb.append("year=:year");
        queryParams.put("year", year);

        sb.append(", maxtemp_cal=:maxT");
        queryParams.put("maxT", maxT);

        if (maxTTime != null) {
            sb.append(", maxtemp_cal_time='");
            sb.append(maxTTime);
            sb.append("'");
        } else {
            sb.append(", maxtemp_cal_time=NULL");
        }

        sb.append(", mintemp_cal=:minT");
        queryParams.put("minT", minT);

        if (minTTime != null) {
            sb.append(", mintemp_cal_time='");
            sb.append(minTTime);
            sb.append("'");
        } else {
            sb.append(", mintemp_cal_time=NULL");
        }

        sb.append(", maxtemp_day=:maxTDaytime");
        queryParams.put("maxTDaytime", maxTDaytime);

        sb.append(", mintemp_day=:minTNight");
        queryParams.put("minTNight", minTNight);

        sb.append(", min_press=:minSeaLevelPressure");
        queryParams.put("minSeaLevelPressure", minSeaLevelPressure);

        if (minSeaLevelPressureTime != null) {
            sb.append(", min_press_time='");
            sb.append(minSeaLevelPressureTime);
            sb.append("'");
        } else {
            sb.append(", min_press_time=NULL");
        }

        sb.append(", equiv_water=:totalPrecip");
        queryParams.put("totalPrecip", totalPrecip);

        // hourly precip
        for (int i = 0; i < hourlyPrecip.length; i++) {
            String colParamName = "pcp_hr_amt_" + String.format("%02d", i + 1);

            sb.append(", ");
            sb.append(colParamName);
            sb.append("=:");
            sb.append(colParamName);

            queryParams.put(colParamName, hourlyPrecip[i]);
        }

        sb.append(", twomin_wspd=:windSpeed2MinAvg");
        queryParams.put("windSpeed2MinAvg", windSpeed2MinAvg);

        sb.append(", max2min_wdir=:windDirection2MinFastest");
        queryParams.put("windDirection2MinFastest", windDirection2MinFastest);

        sb.append(", max2min_wspd=:windSpeed2MinFastest");
        queryParams.put("windSpeed2MinFastest", windSpeed2MinFastest);

        if (windSpeed2MinFastestTime != null) {
            sb.append(", max2min_wnd_time='");
            sb.append(windSpeed2MinFastestTime);
            sb.append("'");
        } else {
            sb.append(", max2min_wnd_time=NULL");
        }

        sb.append(", pkwnd_dir=:peakWindDirection");
        queryParams.put("peakWindDirection", peakWindDirection);

        sb.append(", pkwnd_spd=:peakWindSpeed");
        queryParams.put("peakWindSpeed", peakWindSpeed);

        if (peakWindTime != null) {
            sb.append(", pkwnd_time='");
            sb.append(peakWindTime);
            sb.append("'");
        } else {
            sb.append(", pkwnd_time=NULL");
        }

        sb.append(", wx1=:wxSymbol1");
        queryParams.put("wxSymbol1", wxSymbol[0]);

        sb.append(", wx2=:wxSymbol2");
        queryParams.put("wxSymbol2", wxSymbol[1]);

        sb.append(", wx3=:wxSymbol3");
        queryParams.put("wxSymbol3", wxSymbol[2]);

        sb.append(", wx4=:wxSymbol4");
        queryParams.put("wxSymbol4", wxSymbol[3]);

        sb.append(", wx5=:wxSymbol5");
        queryParams.put("wxSymbol5", wxSymbol[4]);

        sb.append(", min_sun=:sunshineMinutes");
        queryParams.put("sunshineMinutes", sunshineMinutes);

        sb.append(", percent_sun=:sunshinePercent");
        queryParams.put("sunshinePercent", sunshinePercent);

        sb.append(", solid_precip=:snowAmount");
        queryParams.put("snowAmount", snowAmount);

        sb.append(", snowdepth=:depthOfSnow");
        queryParams.put("depthOfSnow", depthOfSnow);

        sb.append(", sky_cover=:skyCoverDaytime");
        queryParams.put("skyCoverDaytime", skyCoverDaytime);

        sb.append(", avg_sky_cover=:skyCoverWholeDay");
        queryParams.put("skyCoverWholeDay", skyCoverWholeDay);

        sb.append(", remarks=:remarks");
        queryParams.put("remarks", remarks);

        sb.append(" WHERE day_of_year=:day_of_year");
        queryParams.put("day_of_year", getDayOfYear());
        sb.append(" AND station_code=:station_code");
        queryParams.put("station_code", getStationCode());

        return sb.toString();

    }

}

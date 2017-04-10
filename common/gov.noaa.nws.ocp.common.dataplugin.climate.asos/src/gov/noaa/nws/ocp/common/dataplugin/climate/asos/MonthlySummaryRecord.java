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

/**
 * MonthlySummaryRecord is the Data Access component for Climate ASOS
 * MonthlySummary
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date           Ticket#    Engineer    Description
 * -------------- ---------- ----------- --------------------------
 * March 22, 2016 16962      pwang       Initial creation for Climate
 *                                       Data plugin
 * 13 APR 2017    33104      amoore      Address comments from review.
 * 05 MAY 2017    33104      amoore      Use common functionality.
 * </pre>
 * 
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class MonthlySummaryRecord extends ClimateASOSMessageRecord {
    /**
     * Table to use.
     */
    private static final String TABLE_NAME = "cli_asos_monthly";

    /**
     * maps to (COR) field in DSM.pdf documentation
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean correction = false;

    /**
     * have to derive from DSM creation time in the text database (Note Local
     * Standard Time time zone)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short year;

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
     * maps to DxDx's, up to 3 dates
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short[] maxTDates = new short[3];

    /**
     * maps to TnTnTn field, min T in calendar day, reported in whole degrees
     * Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short minT;

    /**
     * maps to DnDn's, up to 3 dates
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short[] minTDates = new short[3];

    /**
     * Average daily maximum temperature, reported to the nearest 0.1 degree
     * Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float avgDailyMaxT;

    /**
     * Average daily minimum temperature, reported to the nearest 0.1 degree
     * Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float avgDailyMinT;

    /**
     * Average monthly temperature, reported to the nearest 0.1 degree
     * Fahrenheit
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float avgMonthlyT;

    /**
     * Number of days with a maximum temperature of less than or equal to 32°F
     * (encoded as two digits)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysMaxTBelow32;

    /**
     * Number of days with a maximum temperature of greater than or equal to
     * 90°F (or 70°F in Alaska) (encoded as two digits)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysMaxTAbove90;

    /**
     * Number of days with a minimum temperature of less than or equal to 32°F
     * (encoded as two digits)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysMinTBelow32;

    /**
     * Number of days with a minimum temperature of less than or equal to 0°F
     * (encoded as two digits)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysMinTBelow0;

    /**
     * Monthly total of heating degree days
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short totalHeatingDegreeDays;

    /**
     * Monthly total of cooling degree days
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short totalCoolingDegreeDays;

    /**
     * Monthly mean station pressure, reported to the nearest 0.005 inch of Hg
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float meanStationPressure;

    /**
     * Monthly mean sea-level pressure, reported to the nearest 0.01 inch of Hg
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float meanSeaLevelPressure;

    /**
     * Monthly max sea-level pressure, reported to monththe nearest 0.01 inch of
     * Hg
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float maxSeaLevelPressure;

    /**
     * Date of occurrence of SLPmm (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String maxSeaLevelPressureDate;

    /**
     * Time of occurrence of SLPmm, reported in hours and minutes (Local
     * Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String maxSeaLevelPressureTime;

    /**
     * (+) indicates last of several occurrences
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String multiMaxSLP;

    /**
     * Monthly minimum sea-level pressure, reported to the nearest 0.01 inch of
     * Hg
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float minSeaLevelPressure;

    /**
     * Date of occurrence of SLPnn (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String minSeaLevelPressureDate;

    /**
     * Time of occurrence of SLPnn, reported in hours and minutes (Local
     * Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String minSeaLevelPressureTime;

    /**
     * (+) indicates last of several occurrences
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String multiMinSLP;

    /**
     * Monthly total precipitation (water equivalent), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float totalPrecip;

    /**
     * Number of days with precipitation greater than or equal to 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysPrecipAbove01;

    /**
     * Number of days with precipitation greater than or equal to 0.10 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysPrecipAbove10;

    /**
     * Number of days with precipitation greater than or equal to 0.50 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysPrecipAbove50;

    /**
     * Number of days with precipitation greater than or equal to 1 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short daysPrecipAbove100;

    /**
     * Greatest precipitation in 24 hours (water equivalent) reported to the
     * nearest 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float max24HourPrecip;

    /**
     * start date of occurrence of PmPmPmPm (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short max24HourPrecipStartDate;

    /**
     * end date of occurrence of PmPmPmPm (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short max24HourPrecipEndDate;

    /**
     * (+) indicates last of several occurrences
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean multiMax24HourPrecip = false;

    /**
     * Short-duration precipitation (5-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip5min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip5minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip5minTime;

    /**
     * Short-duration precipitation (10-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip10min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip10minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip10minTime;

    /**
     * Short-duration precipitation (15-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip15min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip15minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip15minTime;

    /**
     * Short-duration precipitation (20-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip20min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip20minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip20minTime;

    /**
     * Short-duration precipitation (30-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip30min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip30minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip30minTime;

    /**
     * Short-duration precipitation (45-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip45min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip45minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip45minTime;

    /**
     * Short-duration precipitation (60-minute maximum), reported to the nearest
     * 0.01 inchmonth
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip60min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip60minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip60minTime;

    /**
     * Short-duration precipitation (80-minute maximum), reported to the nearest
     * 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip80min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip80minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip80minTime;

    /**
     * Short-duration precipitation (100-minute maximum), reported to the
     * nearest 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip100min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip100minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip100minTime;

    /**
     * Short-duration precipitation (120-minute maximum), reported to the
     * nearest 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip120min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip120minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip120minTime;

    /**
     * Short-duration precipitation (150-minute maximum), reported to the
     * nearest 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip150min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip150minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip150minTime;

    /**
     * Short-duration precipitation (180-minute maximum), reported to the
     * nearest 0.01 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float precip180min;

    /**
     * Date on which the short-duration precipitation ended (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip180minDate;

    /**
     * Time of the ending of the specified short-duration precipitation,
     * reported in hours and minutes (Local Standard Time) using a 24-hour clock
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String precip180minTime;

    /**
     * Hours of sunshine, reported to the nearest 0.1 hour
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float sunshineHours;

    /**
     * Percentage of sunshine observed, to the nearest whole percent
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short sunshinePercent;

    /**
     * Greatest snowfall in 24 hours, to the nearest 0.1 inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float max24HourSnow;

    /**
     * start date of occurrence of the greatest snowfall SmSmSm
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short max24HourSnowStartDate;

    /**
     * end date of occurrence of the greatest snowfall SmSmSm
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short max24HourSnowEndDate;

    /**
     * (+) indicates last of several occurrencess
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean multiMax24HourSnow = false;

    /**
     * Greatest snow depth during the month, reported to the nearest whole inch
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short maxSnowDepth;

    /**
     * Date of occurrence of the greatest snow depth SgSgSg (01-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short maxSnowDepthDate;

    /**
     * (+) indicates last of several occurrences
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean multiMaxSnowDepth = false;

    /**
     * Number of clear days (00-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short clearDays;

    /**
     * Number of partly cloudy days (00-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short partlyCloudyDays;

    /**
     * Number of cloudy days (00-31)
     */
    @DynamicSerializeElement
    @XmlAttribute
    private short cloudyDays;

    /**
     * if available, one or more from ET|Epr|EP|ES|ESw|ESd|EC
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String remarks;

    public MonthlySummaryRecord() {
        /*
         * set all the default/missing values, based on DSM documentation,
         * legacy C++ code, and possibly expected missing values in DB
         */
        Calendar now = TimeUtil.newCalendar();
        year = (short) now.get(Calendar.YEAR);
        month = MISSING_VAL_4_DIGITS;
        maxT = MISSING_VAL_4_DIGITS;

        for (int i = 0; i < maxTDates.length; i++) {
            maxTDates[i] = MISSING_VAL_4_DIGITS;
        }

        minT = MISSING_VAL_4_DIGITS;

        for (int i = 0; i < minTDates.length; i++) {
            minTDates[i] = MISSING_VAL_4_DIGITS;
        }

        avgDailyMaxT = MISSING_VAL_4_DIGITS;
        avgDailyMinT = MISSING_VAL_4_DIGITS;
        avgMonthlyT = MISSING_VAL_4_DIGITS;
        daysMaxTBelow32 = MISSING_VAL_4_DIGITS;
        daysMaxTAbove90 = MISSING_VAL_4_DIGITS;
        daysMinTBelow32 = MISSING_VAL_4_DIGITS;
        daysMinTBelow0 = MISSING_VAL_4_DIGITS;
        totalHeatingDegreeDays = MISSING_DEGREE_DAYS;
        totalCoolingDegreeDays = MISSING_DEGREE_DAYS;
        meanStationPressure = MISSING_VAL_4_DIGITS;
        meanSeaLevelPressure = MISSING_VAL_4_DIGITS;
        maxSeaLevelPressure = MISSING_VAL_4_DIGITS;
        maxSeaLevelPressureDate = MISSING_VAL_STRING;
        maxSeaLevelPressureTime = MISSING_VAL_STRING;
        multiMaxSLP = MISSING_VAL_STRING;
        minSeaLevelPressure = MISSING_VAL_4_DIGITS;
        minSeaLevelPressureDate = MISSING_VAL_STRING;
        minSeaLevelPressureTime = MISSING_VAL_STRING;
        multiMinSLP = MISSING_VAL_STRING;
        totalPrecip = MISSING_VAL_4_DIGITS;
        daysPrecipAbove01 = MISSING_VAL_4_DIGITS;
        daysPrecipAbove10 = MISSING_VAL_4_DIGITS;
        daysPrecipAbove50 = MISSING_VAL_4_DIGITS;
        daysPrecipAbove100 = MISSING_VAL_4_DIGITS;
        max24HourPrecip = MISSING_VAL_4_DIGITS;
        max24HourPrecipStartDate = MISSING_VAL_4_DIGITS;
        max24HourPrecipEndDate = MISSING_VAL_4_DIGITS;
        precip5min = MISSING_VAL_4_DIGITS;
        precip5minDate = MISSING_VAL_STRING;
        precip5minTime = MISSING_VAL_STRING;
        precip10min = MISSING_VAL_4_DIGITS;
        precip10minDate = MISSING_VAL_STRING;
        precip10minTime = MISSING_VAL_STRING;
        precip15min = MISSING_VAL_4_DIGITS;
        precip15minDate = MISSING_VAL_STRING;
        precip15minTime = MISSING_VAL_STRING;
        precip20min = MISSING_VAL_4_DIGITS;
        precip20minDate = MISSING_VAL_STRING;
        precip20minTime = MISSING_VAL_STRING;
        precip30min = MISSING_VAL_4_DIGITS;
        precip30minDate = MISSING_VAL_STRING;
        precip30minTime = MISSING_VAL_STRING;
        precip45min = MISSING_VAL_4_DIGITS;
        precip45minDate = MISSING_VAL_STRING;
        precip45minTime = MISSING_VAL_STRING;
        precip60min = MISSING_VAL_4_DIGITS;
        precip60minDate = MISSING_VAL_STRING;
        precip60minTime = MISSING_VAL_STRING;
        precip80min = MISSING_VAL_4_DIGITS;
        precip80minDate = MISSING_VAL_STRING;
        precip80minTime = MISSING_VAL_STRING;
        precip100min = MISSING_VAL_4_DIGITS;
        precip100minDate = MISSING_VAL_STRING;
        precip100minTime = MISSING_VAL_STRING;
        precip120min = MISSING_VAL_4_DIGITS;
        precip120minDate = MISSING_VAL_STRING;
        precip120minTime = MISSING_VAL_STRING;
        precip150min = MISSING_VAL_4_DIGITS;
        precip150minDate = MISSING_VAL_STRING;
        precip150minTime = MISSING_VAL_STRING;
        precip180min = MISSING_VAL_4_DIGITS;
        precip180minDate = MISSING_VAL_STRING;
        precip180minTime = MISSING_VAL_STRING;
        sunshineHours = MISSING_VAL_4_DIGITS;
        sunshinePercent = MISSING_VAL_4_DIGITS;
        max24HourSnow = MISSING_VAL_4_DIGITS;
        max24HourSnowStartDate = MISSING_VAL_4_DIGITS;
        max24HourSnowEndDate = MISSING_VAL_4_DIGITS;
        maxSnowDepth = MISSING_VAL_4_DIGITS;
        maxSnowDepthDate = MISSING_VAL_4_DIGITS;
        clearDays = MISSING_VAL_4_DIGITS;
        partlyCloudyDays = MISSING_VAL_4_DIGITS;
        cloudyDays = MISSING_VAL_4_DIGITS;

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
     * @return the maxTDates
     */
    public short[] getMaxTDates() {
        return maxTDates;
    }

    /**
     * @param maxTDates
     *            the maxTDates to set
     */
    public void setMaxTDates(short[] maxTDates) {
        this.maxTDates = maxTDates;
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
     * @return the minTDates
     */
    public short[] getMinTDates() {
        return minTDates;
    }

    /**
     * @param minTDates
     *            the minTDates to set
     */
    public void setMinTDate1(short[] minTDates) {
        this.minTDates = minTDates;
    }

    /**
     * @return the avgDailyMaxT
     */
    public float getAvgDailyMaxT() {
        return avgDailyMaxT;
    }

    /**
     * @param avgDailyMaxT
     *            the avgDailyMaxT to set
     */
    public void setAvgDailyMaxT(float avgDailyMaxT) {
        this.avgDailyMaxT = avgDailyMaxT;
    }

    /**
     * @return the avgDailyMinT
     */
    public float getAvgDailyMinT() {
        return avgDailyMinT;
    }

    /**
     * @param avgDailyMinT
     *            the avgDailyMinT to set
     */
    public void setAvgDailyMinT(float avgDailyMinT) {
        this.avgDailyMinT = avgDailyMinT;
    }

    /**
     * @return the avgMonthlyT
     */
    public float getAvgMonthlyT() {
        return avgMonthlyT;
    }

    /**
     * @param avgMonthlyT
     *            the avgMonthlyT to set
     */
    public void setAvgMonthlyT(float avgMonthlyT) {
        this.avgMonthlyT = avgMonthlyT;
    }

    /**
     * @return the daysMaxTBelow32
     */
    public short getDaysMaxTBelow32() {
        return daysMaxTBelow32;
    }

    /**
     * @param daysMaxTBelow32
     *            the daysMaxTBelow32 to set
     */
    public void setDaysMaxTBelow32(short daysMaxTBelow32) {
        this.daysMaxTBelow32 = daysMaxTBelow32;
    }

    /**
     * @return the daysMaxTAbove90
     */
    public short getDaysMaxTAbove90() {
        return daysMaxTAbove90;
    }

    /**
     * @param daysMaxTAbove90
     *            the daysMaxTAbove90 to set
     */
    public void setDaysMaxTAbove90(short daysMaxTAbove90) {
        this.daysMaxTAbove90 = daysMaxTAbove90;
    }

    /**
     * @return the daysMinTBelow32
     */
    public short getDaysMinTBelow32() {
        return daysMinTBelow32;
    }

    /**
     * @param daysMinTBelow32
     *            the daysMinTBelow32 to set
     */
    public void setDaysMinTBelow32(short daysMinTBelow32) {
        this.daysMinTBelow32 = daysMinTBelow32;
    }

    /**
     * @return the daysMinTBelow0
     */
    public short getDaysMinTBelow0() {
        return daysMinTBelow0;
    }

    /**
     * @param daysMinTBelow0
     *            the daysMinTBelow0 to set
     */
    public void setDaysMinTBelow0(short daysMinTBelow0) {
        this.daysMinTBelow0 = daysMinTBelow0;
    }

    /**
     * @return the totalHeatingDegreeDays
     */
    public short getTotalHeatingDegreeDays() {
        return totalHeatingDegreeDays;
    }

    /**
     * @param totalHeatingDegreeDays
     *            the totalHeatingDegreeDays to set
     */
    public void setTotalHeatingDegreeDays(short totalHeatingDegreeDays) {
        this.totalHeatingDegreeDays = totalHeatingDegreeDays;
    }

    /**
     * @return the totalCoolingDegreeDays
     */
    public short getTotalCoolingDegreeDays() {
        return totalCoolingDegreeDays;
    }

    /**
     * @param totalCoolingDegreeDays
     *            the totalCoolingDegreeDays to set
     */
    public void setTotalCoolingDegreeDays(short totalCoolingDegreeDays) {
        this.totalCoolingDegreeDays = totalCoolingDegreeDays;
    }

    /**
     * @return the meanStationPressure
     */
    public float getMeanStationPressure() {
        return meanStationPressure;
    }

    /**
     * @param meanStationPressure
     *            the meanStationPressure to set
     */
    public void setMeanStationPressure(float meanStationPressure) {
        this.meanStationPressure = meanStationPressure;
    }

    /**
     * @return the meanSeaLevelPressure
     */
    public float getMeanSeaLevelPressure() {
        return meanSeaLevelPressure;
    }

    /**
     * @param meanSeaLevelPressure
     *            the meanSeaLevelPressure to set
     */
    public void setMeanSeaLevelPressure(float meanSeaLevelPressure) {
        this.meanSeaLevelPressure = meanSeaLevelPressure;
    }

    /**
     * @return the maxSeaLevelPressure
     */
    public float getMaxSeaLevelPressure() {
        return maxSeaLevelPressure;
    }

    /**
     * @param maxSeaLevelPressure
     *            the maxSeaLevelPressure to set
     */
    public void setMaxSeaLevelPressure(float maxSeaLevelPressure) {
        this.maxSeaLevelPressure = maxSeaLevelPressure;
    }

    /**
     * @return the maxSeaLevelPressureDate
     */
    public String getMaxSeaLevelPressureDate() {
        return maxSeaLevelPressureDate;
    }

    /**
     * @param maxSeaLevelPressureDate
     *            the maxSeaLevelPressureDate to set
     */
    public void setMaxSeaLevelPressureDate(String maxSeaLevelPressureDate) {
        this.maxSeaLevelPressureDate = maxSeaLevelPressureDate;
    }

    /**
     * @return the maxSeaLevelPressureTime
     */
    public String getMaxSeaLevelPressureTime() {
        return maxSeaLevelPressureTime;
    }

    /**
     * @param maxSeaLevelPressureTime
     *            the maxSeaLevelPressureTime to set
     */
    public void setMaxSeaLevelPressureTime(String maxSeaLevelPressureTime) {
        this.maxSeaLevelPressureTime = maxSeaLevelPressureTime;
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
     * @return the minSeaLevelPressureDate
     */
    public String getMinSeaLevelPressureDate() {
        return minSeaLevelPressureDate;
    }

    /**
     * @param minSeaLevelPressureDate
     *            the minSeaLevelPressureDate to set
     */
    public void setMinSeaLevelPressureDate(String minSeaLevelPressureDate) {
        this.minSeaLevelPressureDate = minSeaLevelPressureDate;
    }

    /**
     * @return the minSeaLevelPressureTime
     */
    public String getMinSeaLevelPressureTime() {
        return minSeaLevelPressureTime;
    }

    /**
     * @return the multiMaxSLP
     */
    public String getMultiMaxSLP() {
        return multiMaxSLP;
    }

    /**
     * @param multiMaxSLP
     *            the multiMaxSLP to set
     */
    public void setMultiMaxSLP(String multiMaxSLP) {
        this.multiMaxSLP = multiMaxSLP;
    }

    /**
     * @return the multiMinSLP
     */
    public String getMultiMinSLP() {
        return multiMinSLP;
    }

    /**
     * @param multiMinSLP
     *            the multiMinSLP to set
     */
    public void setMultiMinSLP(String multiMinSLP) {
        this.multiMinSLP = multiMinSLP;
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
     * @return the daysPrecipAbove01
     */
    public short getDaysPrecipAbove01() {
        return daysPrecipAbove01;
    }

    /**
     * @param daysPrecipAbove01
     *            the daysPrecipAbove01 to set
     */
    public void setDaysPrecipAbove01(short daysPrecipAbove01) {
        this.daysPrecipAbove01 = daysPrecipAbove01;
    }

    /**
     * @return the daysPrecipAbove10
     */
    public short getDaysPrecipAbove10() {
        return daysPrecipAbove10;
    }

    /**
     * @param daysPrecipAbove10
     *            the daysPrecipAbove10 to set
     */
    public void setDaysPrecipAbove10(short daysPrecipAbove10) {
        this.daysPrecipAbove10 = daysPrecipAbove10;
    }

    /**
     * @return the daysPrecipAbove50
     */
    public short getDaysPrecipAbove50() {
        return daysPrecipAbove50;
    }

    /**
     * @param daysPrecipAbove50
     *            the daysPrecipAbove50 to set
     */
    public void setDaysPrecipAbove50(short daysPrecipAbove50) {
        this.daysPrecipAbove50 = daysPrecipAbove50;
    }

    /**
     * @return the daysPrecipAbove100
     */
    public short getDaysPrecipAbove100() {
        return daysPrecipAbove100;
    }

    /**
     * @param daysPrecipAbove100
     *            the daysPrecipAbove100 to set
     */
    public void setDaysPrecipAbove100(short daysPrecipAbove100) {
        this.daysPrecipAbove100 = daysPrecipAbove100;
    }

    /**
     * @return the max24HourPrecip
     */
    public float getMax24HourPrecip() {
        return max24HourPrecip;
    }

    /**
     * @param max24HourPrecip
     *            the max24HourPrecip to set
     */
    public void setMax24HourPrecip(float max24HourPrecip) {
        this.max24HourPrecip = max24HourPrecip;
    }

    /**
     * @return the max24HourPrecipStartDate
     */
    public short getMax24HourPrecipStartDate() {
        return max24HourPrecipStartDate;
    }

    /**
     * @param max24HourPrecipStartDate
     *            the max24HourPrecipStartDate to set
     */
    public void setMax24HourPrecipStartDate(short max24HourPrecipStartDate) {
        this.max24HourPrecipStartDate = max24HourPrecipStartDate;
    }

    /**
     * @return the max24HourPrecipEndDate
     */
    public short getMax24HourPrecipEndDate() {
        return max24HourPrecipEndDate;
    }

    /**
     * @param max24HourPrecipEndDate
     *            the max24HourPrecipEndDate to set
     */
    public void setMax24HourPrecipEndDate(short max24HourPrecipEndDate) {
        this.max24HourPrecipEndDate = max24HourPrecipEndDate;
    }

    /**
     * @return the multiMax24HourPrecip
     */
    public boolean isMultiMax24HourPrecip() {
        return multiMax24HourPrecip;
    }

    /**
     * @param multiMax24HourPrecip
     *            the multiMax24HourPrecip to set
     */
    public void setMultiMax24HourPrecip(boolean multiMax24HourPrecip) {
        this.multiMax24HourPrecip = multiMax24HourPrecip;
    }

    /**
     * @return the precip5min
     */
    public float getPrecip5min() {
        return precip5min;
    }

    /**
     * @param precip5min
     *            the precip5min to set
     */
    public void setPrecip5min(float precip5min) {
        this.precip5min = precip5min;
    }

    /**
     * @return the precip5minDate
     */
    public String getPrecip5minDate() {
        return precip5minDate;
    }

    /**
     * @param precip5minDate
     *            the precip5minDate to set
     */
    public void setPrecip5minDate(String precip5minDate) {
        this.precip5minDate = precip5minDate;
    }

    /**
     * @return the precip5minTime
     */
    public String getPrecip5minTime() {
        return precip5minTime;
    }

    /**
     * @param precip5minTime
     *            the precip5minTime to set
     */
    public void setPrecip5minTime(String precip5minTime) {
        this.precip5minTime = precip5minTime;
    }

    /**
     * @return the precip10min
     */
    public float getPrecip10min() {
        return precip10min;
    }

    /**
     * @param precip10min
     *            the precip10min to set
     */
    public void setPrecip10min(float precip10min) {
        this.precip10min = precip10min;
    }

    /**
     * @return the precip10minDate
     */
    public String getPrecip10minDate() {
        return precip10minDate;
    }

    /**
     * @param precip10minDate
     *            the precip10minDate to set
     */
    public void setPrecip10minDate(String precip10minDate) {
        this.precip10minDate = precip10minDate;
    }

    /**
     * @return the precip10minTime
     */
    public String getPrecip10minTime() {
        return precip10minTime;
    }

    /**
     * @param precip10minTime
     *            the precip10minTime to set
     */
    public void setPrecip10minTime(String precip10minTime) {
        this.precip10minTime = precip10minTime;
    }

    /**
     * @return the precip15min
     */
    public float getPrecip15min() {
        return precip15min;
    }

    /**
     * @param precip15min
     *            the precip15min to set
     */
    public void setPrecip15min(float precip15min) {
        this.precip15min = precip15min;
    }

    /**
     * @return the precip15minDate
     */
    public String getPrecip15minDate() {
        return precip15minDate;
    }

    /**
     * @param precip15minDate
     *            the precip15minDate to set
     */
    public void setPrecip15minDate(String precip15minDate) {
        this.precip15minDate = precip15minDate;
    }

    /**
     * @return the precip15minTime
     */
    public String getPrecip15minTime() {
        return precip15minTime;
    }

    /**
     * @param precip15minTime
     *            the precip15minTime to set
     */
    public void setPrecip15minTime(String precip15minTime) {
        this.precip15minTime = precip15minTime;
    }

    /**
     * @return the precip20min
     */
    public float getPrecip20min() {
        return precip20min;
    }

    /**
     * @param precip20min
     *            the precip20min to set
     */
    public void setPrecip20min(float precip20min) {
        this.precip20min = precip20min;
    }

    /**
     * @return the precip20minDate
     */
    public String getPrecip20minDate() {
        return precip20minDate;
    }

    /**
     * @param precip20minDate
     *            the precip20minDate to set
     */
    public void setPrecip20minDate(String precip20minDate) {
        this.precip20minDate = precip20minDate;
    }

    /**
     * @return the precip20minTime
     */
    public String getPrecip20minTime() {
        return precip20minTime;
    }

    /**
     * @param precip20minTime
     *            the precip20minTime to set
     */
    public void setPrecip20minTime(String precip20minTime) {
        this.precip20minTime = precip20minTime;
    }

    /**
     * @return the precip30min
     */
    public float getPrecip30min() {
        return precip30min;
    }

    /**
     * @param precip30min
     *            the precip30min to set
     */
    public void setPrecip30min(float precip30min) {
        this.precip30min = precip30min;
    }

    /**
     * @return the precip30minDate
     */
    public String getPrecip30minDate() {
        return precip30minDate;
    }

    /**
     * @param precip30minDate
     *            the precip30minDate to set
     */
    public void setPrecip30minDate(String precip30minDate) {
        this.precip30minDate = precip30minDate;
    }

    /**
     * @return the precip30minTime
     */
    public String getPrecip30minTime() {
        return precip30minTime;
    }

    /**
     * @param precip30minTime
     *            the precip30minTime to set
     */
    public void setPrecip30minTime(String precip30minTime) {
        this.precip30minTime = precip30minTime;
    }

    /**
     * @return the precip45min
     */
    public float getPrecip45min() {
        return precip45min;
    }

    /**
     * @param precip45min
     *            the precip45min to set
     */
    public void setPrecip45min(float precip45min) {
        this.precip45min = precip45min;
    }

    /**
     * @return the precip45minDate
     */
    public String getPrecip45minDate() {
        return precip45minDate;
    }

    /**
     * @param precip45minDate
     *            the precip45minDate to set
     */
    public void setPrecip45minDate(String precip45minDate) {
        this.precip45minDate = precip45minDate;
    }

    /**
     * @return the precip45minTime
     */
    public String getPrecip45minTime() {
        return precip45minTime;
    }

    /**
     * @param precip45minTime
     *            the precip45minTime to set
     */
    public void setPrecip45minTime(String precip45minTime) {
        this.precip45minTime = precip45minTime;
    }

    /**
     * @return the precip60min
     */
    public float getPrecip60min() {
        return precip60min;
    }

    /**
     * @param precip60min
     *            the precip60min to set
     */
    public void setPrecip60min(float precip60min) {
        this.precip60min = precip60min;
    }

    /**
     * @return the precip60minDate
     */
    public String getPrecip60minDate() {
        return precip60minDate;
    }

    /**
     * @param precip60minDate
     *            the precip60minDate to set
     */
    public void setPrecip60minDate(String precip60minDate) {
        this.precip60minDate = precip60minDate;
    }

    /**
     * @return the precip60minTime
     */
    public String getPrecip60minTime() {
        return precip60minTime;
    }

    /**
     * @param precip60minTime
     *            the precip60minTime to set
     */
    public void setPrecip60minTime(String precip60minTime) {
        this.precip60minTime = precip60minTime;
    }

    /**
     * @return the precip80min
     */
    public float getPrecip80min() {
        return precip80min;
    }

    /**
     * @param precip80min
     *            the precip80min to set
     */
    public void setPrecip80min(float precip80min) {
        this.precip80min = precip80min;
    }

    /**
     * @return the precip80minDate
     */
    public String getPrecip80minDate() {
        return precip80minDate;
    }

    /**
     * @param precip80minDate
     *            the precip80minDate to set
     */
    public void setPrecip80minDate(String precip80minDate) {
        this.precip80minDate = precip80minDate;
    }

    /**
     * @return the precip80minTime
     */
    public String getPrecip80minTime() {
        return precip80minTime;
    }

    /**
     * @param precip80minTime
     *            the precip80minTime to set
     */
    public void setPrecip80minTime(String precip80minTime) {
        this.precip80minTime = precip80minTime;
    }

    /**
     * @return the precip100min
     */
    public float getPrecip100min() {
        return precip100min;
    }

    /**
     * @param precip100min
     *            the precip100min to set
     */
    public void setPrecip100min(float precip100min) {
        this.precip100min = precip100min;
    }

    /**
     * @return the precip100minDate
     */
    public String getPrecip100minDate() {
        return precip100minDate;
    }

    /**
     * @param precip100minDate
     *            the precip100minDate to set
     */
    public void setPrecip100minDate(String precip100minDate) {
        this.precip100minDate = precip100minDate;
    }

    /**
     * @return the precip100minTime
     */
    public String getPrecip100minTime() {
        return precip100minTime;
    }

    /**
     * @param precip100minTime
     *            the precip100minTime to set
     */
    public void setPrecip100minTime(String precip100minTime) {
        this.precip100minTime = precip100minTime;
    }

    /**
     * @return the precip120min
     */
    public float getPrecip120min() {
        return precip120min;
    }

    /**
     * @param precip120min
     *            the precip120min to set
     */
    public void setPrecip120min(float precip120min) {
        this.precip120min = precip120min;
    }

    /**
     * @return the precip120minDate
     */
    public String getPrecip120minDate() {
        return precip120minDate;
    }

    /**
     * @param precip120minDate
     *            the precip120minDate to set
     */
    public void setPrecip120minDate(String precip120minDate) {
        this.precip120minDate = precip120minDate;
    }

    /**
     * @return the precip120minTime
     */
    public String getPrecip120minTime() {
        return precip120minTime;
    }

    /**
     * @param precip120minTime
     *            the precip120minTime to set
     */
    public void setPrecip120minTime(String precip120minTime) {
        this.precip120minTime = precip120minTime;
    }

    /**
     * @return the precip150min
     */
    public float getPrecip150min() {
        return precip150min;
    }

    /**
     * @param precip150min
     *            the precip150min to set
     */
    public void setPrecip150min(float precip150min) {
        this.precip150min = precip150min;
    }

    /**
     * @return the precip150minDate
     */
    public String getPrecip150minDate() {
        return precip150minDate;
    }

    /**
     * @param precip150minDate
     *            the precip150minDate to set
     */
    public void setPrecip150minDate(String precip150minDate) {
        this.precip150minDate = precip150minDate;
    }

    /**
     * @return the precip150minTime
     */
    public String getPrecip150minTime() {
        return precip150minTime;
    }

    /**
     * @param precip150minTime
     *            the precip150minTime to set
     */
    public void setPrecip150minTime(String precip150minTime) {
        this.precip150minTime = precip150minTime;
    }

    /**
     * @return the precip180min
     */
    public float getPrecip180min() {
        return precip180min;
    }

    /**
     * @param precip180min
     *            the precip180min to set
     */
    public void setPrecip180min(float precip180min) {
        this.precip180min = precip180min;
    }

    /**
     * @return the precip180minDate
     */
    public String getPrecip180minDate() {
        return precip180minDate;
    }

    /**
     * @param precip180minDate
     *            the precip180minDate to set
     */
    public void setPrecip180minDate(String precip180minDate) {
        this.precip180minDate = precip180minDate;
    }

    /**
     * @return the precip180minTime
     */
    public String getPrecip180minTime() {
        return precip180minTime;
    }

    /**
     * @param precip180minTime
     *            the precip180minTime to set
     */
    public void setPrecip180minTime(String precip180minTime) {
        this.precip180minTime = precip180minTime;
    }

    /**
     * @return the sunshineHours
     */
    public float getSunshineHours() {
        return sunshineHours;
    }

    /**
     * @param sunshineHours
     *            the sunshineHours to set
     */
    public void setSunshineHours(float sunshineHours) {
        this.sunshineHours = sunshineHours;
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
     * @return the max24HourSnow
     */
    public float getMax24HourSnow() {
        return max24HourSnow;
    }

    /**
     * @param max24HourSnow
     *            the max24HourSnow to set
     */
    public void setMax24HourSnow(float max24HourSnow) {
        this.max24HourSnow = max24HourSnow;
    }

    /**
     * @return the max24HourSnowStartDate
     */
    public short getMax24HourSnowStartDate() {
        return max24HourSnowStartDate;
    }

    /**
     * @param max24HourSnowStartDate
     *            the max24HourSnowStartDate to set
     */
    public void setMax24HourSnowStartDate(short max24HourSnowStartDate) {
        this.max24HourSnowStartDate = max24HourSnowStartDate;
    }

    /**
     * @return the max24HourSnowEndDate
     */
    public short getMax24HourSnowEndDate() {
        return max24HourSnowEndDate;
    }

    /**
     * @param max24HourSnowEndDate
     *            the max24HourSnowEndDate to set
     */
    public void setMax24HourSnowEndDate(short max24HourSnowEndDate) {
        this.max24HourSnowEndDate = max24HourSnowEndDate;
    }

    /**
     * @return the multiMax24HourSnow
     */
    public boolean isMultiMax24HourSnow() {
        return multiMax24HourSnow;
    }

    /**
     * @param multiMax24HourSnow
     *            the multiMax24HourSnow to set
     */
    public void setMultiMax24HourSnow(boolean multiMax24HourSnow) {
        this.multiMax24HourSnow = multiMax24HourSnow;
    }

    /**
     * @return the maxSnowDepth
     */
    public short getMaxSnowDepth() {
        return maxSnowDepth;
    }

    /**
     * @param maxSnowDepth
     *            the maxSnowDepth to set
     */
    public void setMaxSnowDepth(short maxSnowDepth) {
        this.maxSnowDepth = maxSnowDepth;
    }

    /**
     * @return the maxSnowDepthDate
     */
    public short getMaxSnowDepthDate() {
        return maxSnowDepthDate;
    }

    /**
     * @param maxSnowDepthDate
     *            the maxSnowDepthDate to set
     */
    public void setMaxSnowDepthDate(short maxSnowDepthDate) {
        this.maxSnowDepthDate = maxSnowDepthDate;
    }

    /**
     * @return the multiMaxSnowDepth
     */
    public boolean isMultiMaxSnowDepth() {
        return multiMaxSnowDepth;
    }

    /**
     * @param multiMaxSnowDepth
     *            the multiMaxSnowDepth to set
     */
    public void setMultiMaxSnowDepth(boolean multiMaxSnowDepth) {
        this.multiMaxSnowDepth = multiMaxSnowDepth;
    }

    /**
     * @return the clearDays
     */
    public short getClearDays() {
        return clearDays;
    }

    /**
     * @param clearDays
     *            the clearDays to set
     */
    public void setClearDays(short clearDays) {
        this.clearDays = clearDays;
    }

    /**
     * @return the partlyCloudyDays
     */
    public short getPartlyCloudyDays() {
        return partlyCloudyDays;
    }

    /**
     * @param partlyCloudyDays
     *            the partlyCloudyDays to set
     */
    public void setPartlyCloudyDays(short partlyCloudyDays) {
        this.partlyCloudyDays = partlyCloudyDays;
    }

    /**
     * @return the cloudyDays
     */
    public short getCloudyDays() {
        return cloudyDays;
    }

    /**
     * @param cloudyDays
     *            the cloudyDays to set
     */
    public void setCloudyDays(short cloudyDays) {
        this.cloudyDays = cloudyDays;
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

    private String getMSMFormatDateString(String date2, String time4) {
        if (date2 == null || time4 == null || date2.length() < 2
                || time4.length() < 4) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(date2);
        sb.append(" ");
        sb.append(time4.substring(0, 2));
        sb.append(":");
        sb.append(time4.substring(2));
        return sb.toString();
    }

    @Override
    public String queryExistingRecordSQL(Map<String, Object> queryParams) {
        StringBuilder sb = new StringBuilder("SELECT count(*) FROM ");
        sb.append(TABLE_NAME);
        sb.append(" WHERE month=:month");
        queryParams.put("month", month);
        sb.append(" AND year=:year");
        queryParams.put("year", year);
        sb.append(" AND station_code=:station_code");
        queryParams.put("station_code", getStationCode());
        return sb.toString();

    }

    @Override
    public String toInsertSQL(Map<String, Object> queryParams) {
        StringBuilder sb = new StringBuilder("INSERT INTO ");

        sb.append(TABLE_NAME);
        sb.append(" VALUES(");
        if (getStationCode() != null && !getStationCode().isEmpty()) {
            sb.append(":stationId");
            queryParams.put("stationId", getStationCode());
        } else {
            return null;
        }
        sb.append(",:year");
        queryParams.put("year", year);
        sb.append(",:month");
        queryParams.put("month", month);
        sb.append(",:maxT");
        queryParams.put("maxT", maxT);
        sb.append(",:maxTDate1");
        queryParams.put("maxTDate1", maxTDates[0]);
        sb.append(",:maxTDate2");
        queryParams.put("maxTDate2", maxTDates[1]);
        sb.append(",:maxTDate3");
        queryParams.put("maxTDate3", maxTDates[2]);
        sb.append(",:minT");
        queryParams.put("minT", minT);
        sb.append(",:minTDate1");
        queryParams.put("minTDate1", minTDates[0]);
        sb.append(",:minTDate2");
        queryParams.put("minTDate2", minTDates[1]);
        sb.append(",:minTDate3");
        queryParams.put("minTDate3", minTDates[2]);
        sb.append(",:avgDailyMaxT");
        queryParams.put("avgDailyMaxT", avgDailyMaxT);
        sb.append(",:avgDailyMinT");
        queryParams.put("avgDailyMinT", avgDailyMinT);
        sb.append(",:avgMonthlyT");
        queryParams.put("avgMonthlyT", avgMonthlyT);
        sb.append(",:daysMaxTBelow32");
        queryParams.put("daysMaxTBelow32", daysMaxTBelow32);
        sb.append(",:daysMaxTAbove90");
        queryParams.put("daysMaxTAbove90", daysMaxTAbove90);
        sb.append(",:daysMinTBelow32");
        queryParams.put("daysMinTBelow32", daysMinTBelow32);
        sb.append(",:daysMinTBelow0");
        queryParams.put("daysMinTBelow0", daysMinTBelow0);
        sb.append(",:totalHeatingDegreeDays");
        queryParams.put("totalHeatingDegreeDays", totalHeatingDegreeDays);
        sb.append(",:totalCoolingDegreeDays");
        queryParams.put("totalCoolingDegreeDays", totalCoolingDegreeDays);
        sb.append(",:meanStationPressure");
        queryParams.put("meanStationPressure", meanStationPressure);
        sb.append(",:meanSeaLevelPressure");
        queryParams.put("meanSeaLevelPressure", meanSeaLevelPressure);
        sb.append(",:maxSeaLevelPressure");
        queryParams.put("maxSeaLevelPressure", maxSeaLevelPressure);
        sb.append(",:maxSeaLevelPressureDate");
        queryParams.put("maxSeaLevelPressureDate", getMSMFormatDateString(
                maxSeaLevelPressureDate, maxSeaLevelPressureTime));
        sb.append(",:multiMaxSLP");
        queryParams.put("multiMaxSLP", multiMaxSLP);
        sb.append(",:minSeaLevelPressure");
        queryParams.put("minSeaLevelPressure", minSeaLevelPressure);
        sb.append(",:minSeaLevelPressureDate");
        queryParams.put("minSeaLevelPressureDate", getMSMFormatDateString(
                minSeaLevelPressureDate, minSeaLevelPressureTime));
        sb.append(",:multiMinSLP");
        queryParams.put("multiMinSLP", multiMinSLP);
        sb.append(",:totalPrecip");
        queryParams.put("totalPrecip", totalPrecip);
        sb.append(",:daysPrecipAbove01");
        queryParams.put("daysPrecipAbove01", daysPrecipAbove01);
        sb.append(",:daysPrecipAbove10");
        queryParams.put("daysPrecipAbove10", daysPrecipAbove10);
        sb.append(",:daysPrecipAbove50");
        queryParams.put("daysPrecipAbove50", daysPrecipAbove50);
        sb.append(",:daysPrecipAbove100");
        queryParams.put("daysPrecipAbove100", daysPrecipAbove100);
        sb.append(",:max24HourPrecip");
        queryParams.put("max24HourPrecip", max24HourPrecip);
        sb.append(",:max24HourPrecipStartDate");
        queryParams.put("max24HourPrecipStartDate", max24HourPrecipStartDate);
        sb.append(",:max24HourPrecipEndDate");
        queryParams.put("max24HourPrecipEndDate", max24HourPrecipEndDate);
        sb.append(",:precip5min");
        queryParams.put("precip5min", precip5min);
        sb.append(",:precip10min");
        queryParams.put("precip10min", precip10min);
        sb.append(",:precip15min");
        queryParams.put("precip15min", precip15min);
        sb.append(",:precip20min");
        queryParams.put("precip20min", precip20min);
        sb.append(",:precip30min");
        queryParams.put("precip30min", precip30min);
        sb.append(",:precip45min");
        queryParams.put("precip45min", precip45min);
        sb.append(",:precip60min");
        queryParams.put("precip60min", precip60min);
        sb.append(",:precip80min");
        queryParams.put("precip80min", precip80min);
        sb.append(",:precip100min");
        queryParams.put("precip100min", precip100min);
        sb.append(",:precip120min");
        queryParams.put("precip120min", precip120min);
        sb.append(",:precip150min");
        queryParams.put("precip150min", precip150min);
        sb.append(",:precip180min");
        queryParams.put("precip180min", precip180min);
        sb.append(",:precip5minDate");
        queryParams.put("precip5minDate",
                getMSMFormatDateString(precip5minDate, this.precip5minTime));
        sb.append(",:precip10minDate");
        queryParams.put("precip10minDate",
                getMSMFormatDateString(precip10minDate, this.precip10minTime));
        sb.append(",:precip15minDate");
        queryParams.put("precip15minDate",
                getMSMFormatDateString(precip15minDate, this.precip15minTime));
        sb.append(",:precip20minDate");
        queryParams.put("precip20minDate",
                getMSMFormatDateString(precip20minDate, this.precip20minTime));
        sb.append(",:precip30minDate");
        queryParams.put("precip30minDate",
                getMSMFormatDateString(precip30minDate, this.precip30minTime));
        sb.append(",:precip45minDate");
        queryParams.put("precip45minDate",
                getMSMFormatDateString(precip45minDate, this.precip45minTime));
        sb.append(",:precip60minDate");
        queryParams.put("precip60minDate",
                getMSMFormatDateString(precip60minDate, this.precip60minTime));
        sb.append(",:precip80minDate");
        queryParams.put("precip80minDate",
                getMSMFormatDateString(precip80minDate, this.precip80minTime));
        sb.append(",:precip100minDate");
        queryParams.put("precip100minDate", getMSMFormatDateString(
                precip100minDate, this.precip100minTime));
        sb.append(",:precip120minDate");
        queryParams.put("precip120minDate", getMSMFormatDateString(
                precip120minDate, this.precip120minTime));
        sb.append(",:precip150minDate");
        queryParams.put("precip150minDate", getMSMFormatDateString(
                precip150minDate, this.precip150minTime));
        sb.append(",:precip180minDate");
        queryParams.put("precip180minDate", getMSMFormatDateString(
                precip180minDate, this.precip180minTime));
        sb.append(",:sunshineHours");
        queryParams.put("sunshineHours", sunshineHours);
        sb.append(",:sunshinePercent");
        queryParams.put("sunshinePercent", sunshinePercent);
        sb.append(",:max24HourSnow");
        queryParams.put("max24HourSnow", max24HourSnow);
        sb.append(",:max24HourSnowStartDate");
        queryParams.put("max24HourSnowStartDate", max24HourSnowStartDate);
        sb.append(",:max24HourSnowEndDate");
        queryParams.put("max24HourSnowEndDate", max24HourSnowEndDate);
        sb.append(",:maxSnowDepth");
        queryParams.put("maxSnowDepth", maxSnowDepth);
        sb.append(",:maxSnowDepthDate");
        queryParams.put("maxSnowDepthDate", maxSnowDepthDate);
        sb.append(",:clearDays");
        queryParams.put("clearDays", clearDays);
        sb.append(",:partlyCloudyDays");
        queryParams.put("partlyCloudyDays", partlyCloudyDays);
        sb.append(",:cloudyDays");
        queryParams.put("cloudyDays", cloudyDays);
        sb.append(",:remarks");
        queryParams.put("remarks", remarks);

        sb.append(")");
        return sb.toString();

    }

    @Override
    public String toUpdateSQL(Map<String, Object> queryParams) {

        StringBuilder sb = new StringBuilder("UPDATE ");

        sb.append(TABLE_NAME);
        sb.append(" SET ");
        sb.append("maxtemp_mon=:maxT");
        queryParams.put("maxT", maxT);
        sb.append(", maxtemp_day1=:maxTDate1");
        queryParams.put("maxTDate1", maxTDates[0]);
        sb.append(", maxtemp_day2=:maxTDate2");
        queryParams.put("maxTDate2", maxTDates[1]);
        sb.append(", maxtemp_day3=:maxTDate3");
        queryParams.put("maxTDate3", maxTDates[2]);
        sb.append(", mintemp_mon=:minT");
        queryParams.put("minT", minT);
        sb.append(", mintemp_day1=:minTDate1");
        queryParams.put("minTDate1", minTDates[0]);
        sb.append(", mintemp_day2=:minTDate2");
        queryParams.put("minTDate2", minTDates[1]);
        sb.append(", mintemp_day3=:minTDate3");
        queryParams.put("minTDate3", minTDates[2]);
        sb.append(", avg_daily_max=:avgDailyMaxT");
        queryParams.put("avgDailyMaxT", avgDailyMaxT);
        sb.append(", avg_daily_min=:avgDailyMinT");
        queryParams.put("avgDailyMinT", avgDailyMinT);
        sb.append(", avg_mon_temp=:avgMonthlyT");
        queryParams.put("avgMonthlyT", avgMonthlyT);
        sb.append(", days_maxt_blo_fzg=:daysMaxTBelow32");
        queryParams.put("daysMaxTBelow32", daysMaxTBelow32);
        sb.append(", days_maxt_hot=:daysMaxTAbove90");
        queryParams.put("daysMaxTAbove90", daysMaxTAbove90);
        sb.append(", days_mint_blo_fzg=:daysMinTBelow32");
        queryParams.put("daysMinTBelow32", daysMinTBelow32);
        sb.append(", days_mint_blo_0f=:daysMinTBelow0");
        queryParams.put("daysMinTBelow0", daysMinTBelow0);
        sb.append(", heating=:totalHeatingDegreeDays");
        queryParams.put("totalHeatingDegreeDays", totalHeatingDegreeDays);
        sb.append(", cooling=:totalCoolingDegreeDays");
        queryParams.put("totalCoolingDegreeDays", totalCoolingDegreeDays);
        sb.append(", mean_stn_press=:meanStationPressure");
        queryParams.put("meanStationPressure", meanStationPressure);
        sb.append(", mean_sea_press=:meanSeaLevelPressure");
        queryParams.put("meanSeaLevelPressure", meanSeaLevelPressure);
        sb.append(", max_sea_press=:maxSeaLevelPressure");
        queryParams.put("maxSeaLevelPressure", maxSeaLevelPressure);
        sb.append(", max_slp_date=:maxSeaLevelPressureDate");
        queryParams.put("maxSeaLevelPressureDate", getMSMFormatDateString(
                maxSeaLevelPressureDate, maxSeaLevelPressureTime));
        sb.append(", max_slp_occur=:multiMaxSLP");
        queryParams.put("multiMaxSLP", multiMaxSLP);
        sb.append(", min_sea_press=:minSeaLevelPressure");
        queryParams.put("minSeaLevelPressure", minSeaLevelPressure);
        sb.append(", min_slp_date=:minSeaLevelPressureDate");
        queryParams.put("minSeaLevelPressureDate", getMSMFormatDateString(
                minSeaLevelPressureDate, minSeaLevelPressureTime));
        sb.append(", min_slp_occur=:multiMinSLP");
        queryParams.put("multiMinSLP", multiMinSLP);
        sb.append(", month_precip=:totalPrecip");
        queryParams.put("totalPrecip", totalPrecip);
        sb.append(", days_hundreth=:daysPrecipAbove01");
        queryParams.put("daysPrecipAbove01", daysPrecipAbove01);
        sb.append(", days_tenth=:daysPrecipAbove10");
        queryParams.put("daysPrecipAbove10", daysPrecipAbove10);
        sb.append(", days_half=:daysPrecipAbove50");
        queryParams.put("daysPrecipAbove50", daysPrecipAbove50);
        sb.append(", days_inch=:daysPrecipAbove100");
        queryParams.put("daysPrecipAbove100", daysPrecipAbove100);
        sb.append(", max_24h_pcp=:max24HourPrecip");
        queryParams.put("max24HourPrecip", max24HourPrecip);
        sb.append(", max_24h_pcp_day1=:max24HourPrecipStartDate");
        queryParams.put("max24HourPrecipStartDate", max24HourPrecipStartDate);
        sb.append(", max_24h_pcp_day2=:max24HourPrecipEndDate");
        queryParams.put("max24HourPrecipEndDate", max24HourPrecipEndDate);
        sb.append(", shrtdurpcp5=:precip5min");
        queryParams.put("precip5min", precip5min);
        sb.append(", shrtdurpcp10=:precip10min");
        queryParams.put("precip10min", precip10min);
        sb.append(", shrtdurpcp15=:precip15min");
        queryParams.put("precip15min", precip15min);
        sb.append(", shrtdurpcp20=:precip20min");
        queryParams.put("precip20min", precip20min);
        sb.append(", shrtdurpcp30=:precip30min");
        queryParams.put("precip30min", precip30min);
        sb.append(", shrtdurpcp45=:precip45min");
        queryParams.put("precip45min", precip45min);
        sb.append(", shrtdurpcp60=:precip60min");
        queryParams.put("precip60min", precip60min);
        sb.append(", shrtdurpcp80=:precip80min");
        queryParams.put("precip80min", precip80min);
        sb.append(", shrtdurpcp100=:precip100min");
        queryParams.put("precip100min", precip100min);
        sb.append(", shrtdurpcp120=:precip120min");
        queryParams.put("precip120min", precip120min);
        sb.append(", shrtdurpcp150=:precip150min");
        queryParams.put("precip150min", precip150min);
        sb.append(", shrtdurpcp180=:precip180min");
        queryParams.put("precip180min", precip180min);
        sb.append(", shrtdurpcp_date5=:precip5minDate");
        queryParams.put("precip5minDate",
                getMSMFormatDateString(precip5minDate, this.precip5minTime));
        sb.append(", shrtdurpcp_date10=:precip10minDate");
        queryParams.put("precip10minDate",
                getMSMFormatDateString(precip10minDate, this.precip10minTime));
        sb.append(", shrtdurpcp_date15=:precip15minDate");
        queryParams.put("precip15minDate",
                getMSMFormatDateString(precip15minDate, this.precip15minTime));
        sb.append(", shrtdurpcp_date20=:precip20minDate");
        queryParams.put("precip20minDate",
                getMSMFormatDateString(precip20minDate, this.precip20minTime));
        sb.append(", shrtdurpcp_date30=:precip30minDate");
        queryParams.put("precip30minDate",
                getMSMFormatDateString(precip30minDate, this.precip30minTime));
        sb.append(", shrtdurpcp_date45=:precip45minDate");
        queryParams.put("precip45minDate",
                getMSMFormatDateString(precip45minDate, this.precip45minTime));
        sb.append(", shrtdurpcp_date60=:precip60minDate");
        queryParams.put("precip60minDate",
                getMSMFormatDateString(precip60minDate, this.precip60minTime));
        sb.append(", shrtdurpcp_date80=:precip80minDate");
        queryParams.put("precip80minDate",
                getMSMFormatDateString(precip80minDate, this.precip80minTime));
        sb.append(", shrtdurpcp_date100=:precip100minDate");
        queryParams.put("precip100minDate", getMSMFormatDateString(
                precip100minDate, this.precip100minTime));
        sb.append(", shrtdurpcp_date120=:precip120minDate");
        queryParams.put("precip120minDate", getMSMFormatDateString(
                precip120minDate, this.precip120minTime));
        sb.append(", shrtdurpcp_date150=:precip150minDate");
        queryParams.put("precip150minDate", getMSMFormatDateString(
                precip150minDate, this.precip150minTime));
        sb.append(", shrtdurpcp_date180=:precip180minDate");
        queryParams.put("precip180minDate", getMSMFormatDateString(
                precip180minDate, this.precip180minTime));
        sb.append(", hrs_sun=:sunshineHours");
        queryParams.put("sunshineHours", sunshineHours);
        sb.append(", percent_sun=:sunshinePercent");
        queryParams.put("sunshinePercent", sunshinePercent);
        sb.append(", max_24h_snow=:max24HourSnow");
        queryParams.put("max24HourSnow", max24HourSnow);
        sb.append(", max_24h_snow_date1=:max24HourSnowStartDate");
        queryParams.put("max24HourSnowStartDate", max24HourSnowStartDate);
        sb.append(", max_24h_snow_date2=:max24HourSnowEndDate");
        queryParams.put("max24HourSnowEndDate", max24HourSnowEndDate);
        sb.append(", snowdepth=:maxSnowDepth");
        queryParams.put("maxSnowDepth", maxSnowDepth);
        sb.append(", snowdepth_date=:maxSnowDepthDate");
        queryParams.put("maxSnowDepthDate", maxSnowDepthDate);
        sb.append(", clear_days=:clearDays");
        queryParams.put("clearDays", clearDays);
        sb.append(", pcloud_days=:partlyCloudyDays");
        queryParams.put("partlyCloudyDays", partlyCloudyDays);
        sb.append(", cloud_days=:cloudyDays");
        queryParams.put("cloudyDays", cloudyDays);
        sb.append(", remarks=:remarks");
        queryParams.put("remarks", remarks);

        sb.append(" WHERE month=:month");
        queryParams.put("month", month);
        sb.append(" AND year=:year");
        queryParams.put("year", year);
        sb.append(" AND station_code=:stationCode");
        queryParams.put("stationCode", getStationCode());

        return sb.toString();

    }
}

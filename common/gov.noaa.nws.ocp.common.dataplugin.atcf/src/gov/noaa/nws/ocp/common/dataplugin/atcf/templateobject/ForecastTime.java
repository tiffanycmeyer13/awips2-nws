/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Class to represent the formatted forecast times at a given forecast hour
 * (TAU).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 15, 2021 87783      jwu         Initial coding.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class ForecastTime {

    /**
     * Forecast TAU
     */
    @DynamicSerializeElement
    private int tau;

    /**
     * Forecast TAU in format "%3d" like "120"
     */
    @DynamicSerializeElement
    private String fcstHour;

    /**
     * Hours ahead of TAU 3 (mainly used for TCA, +06, +12, +18, +24), in format
     * "%2d" like "06"
     */
    @DynamicSerializeElement
    private String plusHour;

    /**
     * TimeZone, mostly UTC, local time zone for TCP/TCD.
     */
    @DynamicSerializeElement
    private String timeZone;

    /**
     * Year, i.e., "2021"
     */
    @DynamicSerializeElement
    private String year;

    /**
     * Month i.e, "04"
     */
    @DynamicSerializeElement
    private String mon;

    /**
     * Short name of month, i.e, "JAN"
     */
    @DynamicSerializeElement
    private String month;

    /**
     * Short name of week day, i.e, "MON"
     */
    @DynamicSerializeElement
    private String weekDay;

    /**
     * Day, i.e, "21"
     */
    @DynamicSerializeElement
    private String day;

    /**
     * Hour, like "6" or "12"
     */
    @DynamicSerializeElement
    private String hour;

    /**
     * 2-Digit Hour, like "06" or "12"
     */
    @DynamicSerializeElement
    private String hour2;

    /**
     * Minutes. Mostly it is "00", but could be "30" if for a special advisory.
     */
    @DynamicSerializeElement
    private String minute;

    /**
     * Am/Pm, like "AM"
     */
    @DynamicSerializeElement
    private String amPm;

    /**
     * "day/hour2" for convenience, like "27/06"
     */
    @DynamicSerializeElement
    private String ddHH;

    /**
     * "{hour}{minute} {amPM} {timeZone}" for convenience, like "200 AM EDT"
     */
    @DynamicSerializeElement
    private String hhmmaz;

    /**
     * "{hour2}{minute}" for UTC; "{hour}{minute}" for local time.
     */
    @DynamicSerializeElement
    private String hhmm;

    /**
     * "{year}{mon}{day}/{hour2}" - like "20210228/09".
     */
    @DynamicSerializeElement
    private String ymdh;

    /**
     * Advisory issue time
     * 
     * For UTC time (TCM/PWS/TCA), i.e., "2100 UTC SAT MAY 16 2020"
     *
     * For local time (TCP/TCD), i.e., "500 PM EDT Sat May 16 2020"
     */
    @DynamicSerializeElement
    private String advTime;

    /**
     * Constructor
     */
    public ForecastTime() {
        this.tau = 0;
        this.fcstHour = "0";
        this.plusHour = "00";
        this.timeZone = "UTC";
        this.year = "";
        this.mon = "";
        this.month = "";
        this.weekDay = "";
        this.day = "";
        this.hour = "";
        this.hour2 = "";
        this.minute = "00";
        this.amPm = "";
        this.ddHH = "";
        this.hhmmaz = "";
        this.hhmm = "";
        this.ymdh = "";
        this.advTime = "";
    }

    /**
     * Constructor
     * 
     * @param tm
     *            ForecastTime
     */
    public ForecastTime(ForecastTime tm) {
        this.tau = tm.tau;
        this.timeZone = tm.timeZone;
        this.year = tm.year;
        this.mon = tm.mon;
        this.month = tm.month;
        this.weekDay = tm.weekDay;
        this.day = tm.day;
        this.hour = tm.day;
        this.hour2 = tm.hour2;
        this.minute = tm.minute;
        this.amPm = tm.amPm;
        this.ddHH = tm.ddHH;
        this.hhmmaz = tm.hhmmaz;
        this.hhmm = tm.hhmm;
        this.ymdh = tm.ymdh;
        this.advTime = tm.advTime;
    }

    /**
     * @return the tau
     */
    public int getTau() {
        return tau;
    }

    /**
     * @param tau
     *            the tau to set
     */
    public void setTau(int tau) {
        this.tau = tau;
    }

    /**
     * @return the fcstHour
     */
    public String getFcstHour() {
        return fcstHour;
    }

    /**
     * @param fcstHour
     *            the fcstHour to set
     */
    public void setFcstHour(String fcstHour) {
        this.fcstHour = fcstHour;
    }

    /**
     * @return the plusHour
     */
    public String getPlusHour() {
        return plusHour;
    }

    /**
     * @param plusHour
     *            the plusHour to set
     */
    public void setPlusHour(String plusHour) {
        this.plusHour = plusHour;
    }

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone
     *            the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param year
     *            the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * @return the mon
     */
    public String getMon() {
        return mon;
    }

    /**
     * @param mon
     *            the mon to set
     */
    public void setMon(String mon) {
        this.mon = mon;
    }

    /**
     * @return the month
     */
    public String getMonth() {
        return month;
    }

    /**
     * @param month
     *            the month to set
     */
    public void setMonth(String month) {
        this.month = month;
    }

    /**
     * @return the weekDay
     */
    public String getWeekDay() {
        return weekDay;
    }

    /**
     * @param weekDay
     *            the weekDay to set
     */
    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    /**
     * @return the day
     */
    public String getDay() {
        return day;
    }

    /**
     * @param day
     *            the day to set
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * @return the hour
     */
    public String getHour() {
        return hour;
    }

    /**
     * @param hour
     *            the hour to set
     */
    public void setHour(String hour) {
        this.hour = hour;
    }

    /**
     * @return the hour2
     */
    public String getHour2() {
        return hour2;
    }

    /**
     * @param hour2
     *            the hour2 to set
     */
    public void setHour2(String hour2) {
        this.hour2 = hour2;
    }

    /**
     * @return the minute
     */
    public String getMinute() {
        return minute;
    }

    /**
     * @param minute
     *            the minute to set
     */
    public void setMinute(String minute) {
        this.minute = minute;
    }

    /**
     * @return the amPm
     */
    public String getAmPm() {
        return amPm;
    }

    /**
     * @param amPm
     *            the amPm to set
     */
    public void setAmPm(String amPm) {
        this.amPm = amPm;
    }

    /**
     * @return the ddHH
     */
    public String getDdHH() {
        return ddHH;
    }

    /**
     * @param ddHH
     *            the ddHH to set
     */
    public void setDdHH(String ddHH) {
        this.ddHH = ddHH;
    }

    /**
     * @return the hhmmaz
     */
    public String getHhmmaz() {
        return hhmmaz;
    }

    /**
     * @param hhmmaz
     *            the hhmmaz to set
     */
    public void setHhmmaz(String hhmmaz) {
        this.hhmmaz = hhmmaz;
    }

    /**
     * @return the hhmm
     */
    public String getHhmm() {
        return hhmm;
    }

    /**
     * @param hhmm
     *            the hhmm to set
     */
    public void setHhmm(String hhmm) {
        this.hhmm = hhmm;
    }

    /**
     * @return the ymdh
     */
    public String getYmdh() {
        return ymdh;
    }

    /**
     * @param ymdh
     *            the ymdh to set
     */
    public void setYmdh(String ymdh) {
        this.ymdh = ymdh;
    }

    /**
     * @return the advTime
     */
    public String getAdvTime() {
        return advTime;
    }

    /**
     * @param advTime
     *            the advTime to set
     */
    public void setAdvTime(String advTime) {
        this.advTime = advTime;
    }

    /**
     * Write as an information string.
     * 
     * @return String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Forecast Hour: %d\n", tau));
        sb.append(String.format("DTG: %s%s %s %s %s %s %s\n", hour2, minute,
                timeZone, weekDay, month, day, year));
        return sb.toString();
    }

}

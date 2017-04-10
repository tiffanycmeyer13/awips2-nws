/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.sql.Time;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Converted from rehost-adapt/adapt/climate/include/TYPE_climate_time.h
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2015            xzhang      Initial creation
 * 17 MAY 2016  18384      amoore      Logic cleanup.
 * 07 JUL 2016  16962      amoore      Fix serialization
 * 08 AUG 2016  20414      amoore      Hour-only constructor.
 * 26 OCT 2016  21378      amoore      Copy data from a calendar object.
 * 19 DEC 2016  20955      amoore      Publicize am-pm constants.
 * 12 JAN 2017  20640      jwu         Add xml annotations
 * 26 JAN 2017  27017      amoore      Construct from Calendar or Time object.
 * 09 FEB 2017  28609      amoore      Hour-minute only constructor.
 * 22 FEB 2017  28609      amoore      Address TODOs. Fix comments.
 * 15 MAR 2017  30162      amoore      Fix logging.
 * 17 MAR 2016  21099      wpaintsil   Add getDailyValidTime from ClimateCreator.
 * 21 MAR 2017  20632      amoore      Add warnings on bad constructor input.
 * 12 APR 2017  28536      wpaintsil   Add conversion to 24 hour time.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 02 MAY 2017  33104      amoore      Remove SQL string method. Fill out constructors.
 * 11 MAY 2017  33104      amoore      More Find Bugs minor issues.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimateTime")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateTime {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateTime.class);

    /**
     * PM string.
     */
    public static final String PM_STRING = "PM";

    /**
     * AM string.
     */
    public static final String AM_STRING = "AM";

    /**
     * String to return for SQL statements if hour and minute indicate missing
     * time.
     */
    public static final String NULL_TIME_STRING = "null";

    /**
     * Separator between hour and minute.
     */
    public static final String TIME_SEPARATOR = ":";

    /**
     * String for missing/unknown hour and minute.
     */
    public static final String MISSING_TIME_STRING = ParameterFormatClimate.MISSING_HOUR
            + TIME_SEPARATOR + ParameterFormatClimate.MISSING_MINUTE;

    /**
     * Hour of the day (0-23)
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Hour")
    private int hour;

    /**
     * Minutes of the hour (0-59)
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Minute")
    private int min;

    /**
     * am or pm
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "AmPm")
    private String ampm;

    /**
     * time zone.
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "TimeZone")
    private String zone;

    /**
     * If global configuration is missing, use this default valid time (5pm).
     */
    public static final int DEFAULT_VALID_TIME_HOUR = 17;

    /**
     * Empty contructor.
     */
    public ClimateTime() {
    }

    /**
     * Copy constructor.
     * 
     * @param time
     */
    public ClimateTime(ClimateTime time) {
        hour = time.getHour();
        min = time.getMin();
        ampm = time.getAmpm();
        zone = time.getZone();
    }

    /**
     * Constructor. Assume minutes 0.
     * 
     * @param iHour
     *            hour to set.
     */
    public ClimateTime(int iHour) {
        hour = iHour;
        min = 0;
        ampm = "";
        zone = "";
    }

    /**
     * Constructor.
     * 
     * @param iHour
     *            hour to set.
     * @param iMin
     *            minute to set.
     */
    public ClimateTime(int iHour, int iMin) {
        hour = iHour;
        min = iMin;
        ampm = "";
        zone = "";
    }

    /**
     * Constructor.
     * 
     * @param iHour
     *            hour to set.
     * @param iMin
     *            minute to set.
     * @param iAmpm
     *            am-pm to set.
     */
    public ClimateTime(int iHour, int iMin, String iAmpm) {
        hour = iHour;
        min = iMin;
        ampm = iAmpm;
        zone = "";
    }

    /**
     * Construct from an object. If the object is null or not an appropriate
     * object ({@link String}, {@link Calendar}, or {@link Time}), then set data
     * to missing.
     * 
     * @param iTimeObject
     */
    public ClimateTime(Object iTimeObject) {
        if (iTimeObject == null) {
            setDataToMissing();
            logger.warn(
                    "Null object cannot be used to construct ClimateTime. Data will be set to missing.");
        } else if (iTimeObject instanceof String) {
            initFromSingleString((String) iTimeObject);
        } else if (iTimeObject instanceof Calendar) {
            setTimeFromCalendar((Calendar) iTimeObject);
        } else if (iTimeObject instanceof Time) {
            initFromSingleString(((Time) iTimeObject).toString());
        } else {
            setDataToMissing();
            logger.warn("Object type: [" + iTimeObject.getClass()
                    + "] cannot be used to construct ClimateTime. Data will be set to missing.");
        }
    }

    /**
     * Construct from "hh:mm". If not formatted correctly, set data to missing.
     * 
     * @param iTimeString
     */
    public ClimateTime(String iTimeString) {
        initFromSingleString(iTimeString);
    }

    /**
     * Construct from calendar object.
     * 
     * @param iCalendar
     */
    public ClimateTime(Calendar iCalendar) {
        setTimeFromCalendar(iCalendar);
    }

    /**
     * Construct from time object.
     * 
     * @param iTime
     */
    public ClimateTime(Time iTime) {
        initFromSingleString(iTime.toString());
    }

    /**
     * Init from "hh:mm". If not formatted correctly, set data to missing.
     * 
     * @param iTimeString
     */
    private void initFromSingleString(String iTimeString) {
        try {
            String[] ss = iTimeString.split(TIME_SEPARATOR);
            this.hour = Integer.parseInt(ss[0]);
            this.min = Integer.parseInt(ss[1]);
        } catch (Exception e) {
            logger.warn("The string: [" + iTimeString
                    + "] is not of expected hh:mm time format. Time data will be set to missing.");
            setDataToMissing();
        }

        ampm = "";
        zone = "";
    }

    /**
     * Construct from {"hh", "mm", "ampm", "zone"}. If not formatted correctly,
     * set data to missing.
     * 
     * @param ss
     */
    public ClimateTime(String[] ss) {
        try {
            if (ss.length >= 4) {
                this.hour = Integer.parseInt(ss[0]);
                this.min = Integer.parseInt(ss[1]);
                this.ampm = ss[2];
                this.zone = ss[3];
            } else {
                logger.warn("The string array: [" + Arrays.toString(ss)
                        + "] is not of the expected length or greater. Time data will be set to missing.");
                setDataToMissing();
            }
        } catch (Exception e) {
            logger.warn("The string array: [" + Arrays.toString(ss)
                    + "] is not of expected format {'hh', 'mm', 'ampm', 'zone'}. Time data will be set to missing.");
            setDataToMissing();
        }
    }

    /**
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * @return the min
     */
    public int getMin() {
        return min;
    }

    /**
     * @param ihour
     *            the hour to set
     */
    public void setHour(int ihour) {
        this.hour = ihour;
    }

    /**
     * @param imin
     *            the min to set
     */
    public void setMin(int imin) {
        this.min = imin;
    }

    /**
     * Set data fields to missing values.
     */
    public void setDataToMissing() {
        this.hour = ParameterFormatClimate.MISSING_HOUR;
        this.min = ParameterFormatClimate.MISSING_MINUTE;
        this.ampm = "";
        this.zone = "";
    }

    /**
     * @return true if either the hour or minute value is missing. False
     *         otherwise.
     */
    public boolean isMissingHourMin() {
        return (hour == ParameterFormatClimate.MISSING_HOUR)
                || (min == ParameterFormatClimate.MISSING_MINUTE);
    }

    /**
     * @return true if both hour and min are missing, false otherwise.
     */
    public boolean isMissing() {
        return (hour == ParameterFormatClimate.MISSING_HOUR)
                && (min == ParameterFormatClimate.MISSING_MINUTE);
    }

    /**
     * @return true if the time is realistic (24 hr time), false otherwise.
     */
    public boolean isValid() {
        return hour >= 0 && hour < 24 && min >= 0 && min < 60;
    }

    /**
     * @return am or pm string.
     */
    public String getAmpm() {
        return ampm;
    }

    /**
     * @param iampm
     *            am or pm string.
     */
    public void setAmpm(String iampm) {
        this.ampm = iampm;
    }

    /**
     * @return timezone string.
     */
    public String getZone() {
        return zone;
    }

    /**
     * @param izone
     *            timezone string.
     */
    public void setZone(String izone) {
        this.zone = izone;
    }

    /**
     * Set hour, minute, ampm based on given calendar.
     * 
     * @param cal
     */
    public void setTimeFromCalendar(Calendar cal) {
        hour = cal.get(Calendar.HOUR);
        min = cal.get(Calendar.MINUTE);
        ampm = cal.get(Calendar.AM_PM) == Calendar.AM ? AM_STRING : PM_STRING;
    }

    /**
     * @return the hour and minute for this time, separated by
     *         {@link ClimateTime#TIME_SEPARATOR}.
     */
    public String toHourMinString() {
        if (min >= 10) {
            return hour + TIME_SEPARATOR + min;
        } else {
            return hour + TIME_SEPARATOR + "0" + min;
        }
    }

    /**
     * @return the full string for this time, including hour, minute, am or pm,
     *         and time zone, separated by spaces as expected by Formatter.
     */
    public String toFullString() {
        return hour + " " + min + " " + ampm + " " + zone;
    }

    /**
     * Migrated from conv_time.f
     * 
     * <pre>
     *   Sept. 1998     David O. Miller      PRC/TDL
    *   
    *   Purpose:  
    *          This subroutine will convert 24-hour time (military time)
    *          to standard 12-hour time and will add AM/PM.
    *
    *   Variables
    *
    *      INPUT
    *        t_time - 24-hour time
    *
    *      OUTPUT
    *        t_time - 12-hour time
    *
    *      LOCAL
    *
    *         NONE
    *
    *      INCLUDE files
    *
    *  TYPE_time.I                 - Defines the derived TYPE used to specify the
    *                                 time.
    *
    *      Non-system routines used
    *
    *  NONE
    *
    *      Non-system functions used
    *
    *  NONE
     * </pre>
     * 
     * @return {@link ClimateTime} instance with 12 hour format.
     */
    public ClimateTime to12HourTime() {
        ClimateTime time12Hour = new ClimateTime(this);

        if (hour == 0) {
            time12Hour.setHour(12);
            time12Hour.setAmpm(AM_STRING);
        } else if (hour == 12) {
            time12Hour.setHour(12);
            time12Hour.setAmpm(PM_STRING);
        } else if (hour == ParameterFormatClimate.MISSING_HOUR) {
            time12Hour.setHour(ParameterFormatClimate.MISSING_HOUR);
            time12Hour.setMin(ParameterFormatClimate.MISSING_MINUTE);
            time12Hour.setAmpm(ParameterFormatClimate.MM);
        } else if (min == ParameterFormatClimate.MISSING_MINUTE) {
            time12Hour.setHour(ParameterFormatClimate.MISSING_HOUR);
            time12Hour.setMin(ParameterFormatClimate.MISSING_MINUTE);
            time12Hour.setAmpm(ParameterFormatClimate.MM);
        } else if (hour > 23) {
            time12Hour.setHour(ParameterFormatClimate.MISSING_HOUR);
            time12Hour.setMin(ParameterFormatClimate.MISSING_MINUTE);
            time12Hour.setAmpm(ParameterFormatClimate.MM);
        } else if (hour > 12) {
            time12Hour.setHour(hour - 12);
            time12Hour.setAmpm(PM_STRING);
        } else {
            time12Hour.setAmpm(AM_STRING);
        }

        return time12Hour;
    }

    /**
     * Convert 12 hour time to 24 hour time.
     * 
     * @return new {@link ClimateTime} instance in 24 hour format.
     */
    public ClimateTime to24HourTime() {
        ClimateTime time24Hour = new ClimateTime(this);

        if (ampm != null) {
            if (ampm.equals(AM_STRING)) {
                if (hour == TimeUtil.HOURS_PER_HALF_DAY) {
                    time24Hour.setHour(hour - TimeUtil.HOURS_PER_HALF_DAY);
                }
            } else {
                if (hour < TimeUtil.HOURS_PER_HALF_DAY) {
                    time24Hour.setHour(hour + TimeUtil.HOURS_PER_HALF_DAY);
                }
            }
        }

        return time24Hour;
    }

    /**
     * @return {@link ClimateTime} instance with all missing values.
     */
    public static ClimateTime getMissingClimateTime() {
        ClimateTime time = new ClimateTime();
        time.setDataToMissing();
        return time;
    }

    /**
     * @return object based on local time.
     */
    public static ClimateTime getLocalTime() {
        Calendar cal = TimeUtil.newCalendar();

        return new ClimateTime(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE),
                cal.get(Calendar.AM_PM) == Calendar.AM ? AM_STRING : PM_STRING);
    }

    @Override
    public boolean equals(Object iOther) {
        if (iOther != null && iOther instanceof ClimateTime) {
            ClimateTime otherTime = (ClimateTime) iOther;
            if (hour == otherTime.getHour() && min == otherTime.getMin()
                    && Objects.equals(ampm, otherTime.getAmpm())
                    && Objects.equals(zone, otherTime.getZone())) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param other
     * @return true if this time is after the given one. False otherwise.
     *         Assumes same equivalent time zones. At least the hours must be
     *         set.
     */
    public boolean after(ClimateTime other) {
        if (getHour() == ParameterFormatClimate.MISSING_HOUR
                || other.getHour() == ParameterFormatClimate.MISSING_HOUR) {
            // hours are not set
            return false;
        } else if (PM_STRING.equals(getAmpm())
                && AM_STRING.equals(other.getAmpm())) {
            // this is in PM, other is AM
            return true;
        } else if (PM_STRING.equals(other.getAmpm())
                && AM_STRING.equals(getAmpm())) {
            // this is AM, other is PM
            return false;
        } else if (getHour() > other.getHour()) {
            // this has later hour
            return true;
        } else if (other.getHour() > getHour()) {
            // other has later hour
            return false;
        } else if (getMin() > other.getMin()) {
            // this has later minute
            return true;
        } else if (other.getMin() > getMin()) {
            // other has later minute
            return false;
        } else {
            // equal
            return false;
        }
    }

    /**
     * @param other
     * @return true if this time is before the given one. False otherwise.
     *         Assumes same equivalent time zones. At least the hours must be
     *         set.
     */
    public boolean before(ClimateTime other) {
        if (getHour() == ParameterFormatClimate.MISSING_HOUR
                || other.getHour() == ParameterFormatClimate.MISSING_HOUR) {
            // hours are not set
            return false;
        } else if (PM_STRING.equals(getAmpm())
                && AM_STRING.equals(other.getAmpm())) {
            // this is in PM, other is AM
            return false;
        } else if (PM_STRING.equals(other.getAmpm())
                && AM_STRING.equals(getAmpm())) {
            // this is AM, other is PM
            return true;
        } else if (getHour() > other.getHour()) {
            // this has later hour
            return false;
        } else if (other.getHour() > getHour()) {
            // other has later hour
            return true;
        } else if (getMin() > other.getMin()) {
            // this has later minute
            return false;
        } else if (other.getMin() > getMin()) {
            // other has later minute
            return true;
        } else {
            // equal
            return false;
        }
    }

    /**
     * @param periodType
     * @return the appropriate valid time for the given daily period type, or
     *         missing if the global configuration is missing.
     */
    public static ClimateTime getDailyValidTime(PeriodType periodType,
            ClimateGlobal globals) {
        ClimateTime validTime = getMissingClimateTime();

        if (globals == null) {
            logger.warn(
                    "Global configuration is null. Valid time will be set to 5pm.");
            validTime = new ClimateTime(DEFAULT_VALID_TIME_HOUR);
        } else if (periodType.isDaily()) {
            ClimateTime localTime = getLocalTime();

            switch (periodType) {
            case EVEN_NWWS:
            case EVEN_RAD:
                // evening case
                if (globals.isUseValidPm()) {
                    validTime = new ClimateTime(globals.getValidPm());

                    // legacy set valid hour to local hour no matter what
                    validTime.setHour(localTime.getHour());
                }
                break;
            case INTER_NWWS:
            case INTER_RAD:
                // intermediate case
                if (globals.isUseValidIm()) {
                    validTime = new ClimateTime(globals.getValidIm());

                    // legacy set valid hour to local hour no matter what
                    validTime.setHour(localTime.getHour());
                }
                break;
            default:
                // do nothing
                logger.debug("No daily valid time possible for period type: ["
                        + periodType + "]");
            }
        }
        return validTime;
    }
}

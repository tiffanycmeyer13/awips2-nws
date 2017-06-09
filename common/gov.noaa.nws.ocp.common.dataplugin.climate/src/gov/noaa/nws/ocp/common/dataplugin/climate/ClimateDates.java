/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

/**
 * 
 * Converted from TYPE_climate_dates.h
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 01, 2015            xzhang      Initial creation
 * 17 MAY 2016  18384      amoore      Logic cleanup.
 * 18 MAY 2016  18384      amoore      Serialization.
 * 07 JUL 2016  16962      amoore      Fix serialization.
 * 08 AUG 2016  20414      amoore      Added constructors by period type and with time.
 * 04 OCT 2016  20636      wpaintsil   Add yyyy-MM-dd HH format logic.
 * 07 DEC 2016  20414      amoore      Add toString.
 * 14 DEC 2016  27015      amoore      Added copy constructor.
 * 29 DEC 2016  27681      wpaintsil   Tweak parseClimateDatesFromStrings to ensure missing values by default.
 * 12 JAN 2017  20640      jwu         Add xml annotations
 * 24 JAN 2017  27022      amoore      Add missing data on partial constructors.
 * 07 FEB 2017  20640      jwu         Remove xml annotations for startTime/EndTime
 * 15 MAR 2017  30162      amoore      Fix logging and exceptions.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 24 APR 2017  33104      amoore      Add logging.
 * 10 MAY 2017  33104      amoore      Address Find Bugs issue about static date formats.
 * 11 MAY 2017  33104      amoore      More Find Bugs minor issues.
 * 23 MAY 2017  33104      amoore      Fix duplicate logging.
 * 15 JUN 2017  35185      amoore      Add {@link #isMissing()}.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ClimateDates")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateDates {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateDates.class);

    @DynamicSerializeElement
    @XmlElement(name = "StartDate")
    private ClimateDate start;

    @DynamicSerializeElement
    @XmlElement(name = "EndDate")
    private ClimateDate end;

    @DynamicSerializeElement
    private ClimateTime startTime;

    @DynamicSerializeElement
    private ClimateTime endTime;

    /**
     * Empty constructor.
     */
    public ClimateDates() {
    }

    /**
     * Copy constructor.
     * 
     * @param other
     */
    public ClimateDates(ClimateDates other) {
        start = new ClimateDate(other.getStart());
        end = new ClimateDate(other.getEnd());
        startTime = new ClimateTime(other.getStartTime());
        endTime = new ClimateTime(other.getEndTime());
    }

    /**
     * Constructor for seasonal period.
     * 
     * @param iSeason
     *            season
     * @param iYear
     *            year
     * @throws NullPointerException
     *             on null season type.
     */
    public ClimateDates(SeasonType iSeason, int iYear)
            throws ClimateInvalidParameterException {
        switch (iSeason) {
        case DJF_WINTER:
            start = new ClimateDate(1, 12, iYear - 1);
            end = new ClimateDate(ClimateUtilities.daysInMonth(iYear, 2), 2,
                    iYear);
            break;
        case JJA_SUMMER:
            start = new ClimateDate(1, 6, iYear);
            end = new ClimateDate(ClimateUtilities.daysInMonth(iYear, 8), 8,
                    iYear);
            break;
        case MAM_SPRING:
            start = new ClimateDate(1, 3, iYear);
            end = new ClimateDate(ClimateUtilities.daysInMonth(iYear, 5), 5,
                    iYear);
            break;
        case SON_FALL:
            start = new ClimateDate(1, 9, iYear);
            end = new ClimateDate(ClimateUtilities.daysInMonth(iYear, 11), 11,
                    iYear);
            break;
        default:
            throw new ClimateInvalidParameterException(
                    "Invalid season type: [" + iSeason + "]");
        }
        startTime = ClimateTime.getMissingClimateTime();
        endTime = ClimateTime.getMissingClimateTime();
    }

    /**
     * Constructor for an annual period.
     * 
     * @param iYear
     *            the year.
     */
    public ClimateDates(int iYear) {
        start = new ClimateDate(1, 1, iYear);
        end = new ClimateDate(ClimateUtilities.daysInMonth(iYear, 12), 12,
                iYear);
        startTime = ClimateTime.getMissingClimateTime();
        endTime = ClimateTime.getMissingClimateTime();
    }

    /**
     * Constructor for a monthly period.
     * 
     * @param iMonth
     *            the month.
     * @param iYear
     *            the year.
     */
    public ClimateDates(int iMonth, int iYear) {
        start = new ClimateDate(1, iMonth, iYear);
        end = new ClimateDate(ClimateUtilities.daysInMonth(iYear, iMonth),
                iMonth, iYear);
        startTime = ClimateTime.getMissingClimateTime();
        endTime = ClimateTime.getMissingClimateTime();
    }

    /**
     * Constructor.
     * 
     * @param iStartDate
     * @param iEndDate
     */
    public ClimateDates(ClimateDate iStartDate, ClimateDate iEndDate) {
        start = iStartDate;
        end = iEndDate;
        startTime = ClimateTime.getMissingClimateTime();
        endTime = ClimateTime.getMissingClimateTime();
    }

    /**
     * Constructor.
     * 
     * @param iStartDate
     * @param iEndDate
     * @param iStartTime
     * @param iEndTime
     */
    public ClimateDates(ClimateDate iStartDate, ClimateDate iEndDate,
            ClimateTime iStartTime, ClimateTime iEndTime) {
        start = iStartDate;
        end = iEndDate;
        startTime = iStartTime;
        endTime = iEndTime;
    }

    /**
     * Constructor.
     * 
     * @param iStartDate
     * @param iEndDate
     * @param iStartHour
     * @param iEndHour
     */
    public ClimateDates(ClimateDate iStartDate, ClimateDate iEndDate,
            int iStartHour, int iEndHour) {
        start = iStartDate;
        end = iEndDate;
        startTime = new ClimateTime(iStartHour);
        endTime = new ClimateTime(iEndHour);
    }

    /**
     * @return the start
     */
    public ClimateDate getStart() {
        return start;
    }

    /**
     * @param istart
     *            the start to set
     */
    public void setStart(ClimateDate istart) {
        this.start = istart;
    }

    /**
     * @return the end
     */
    public ClimateDate getEnd() {
        return end;
    }

    /**
     * @param iend
     *            the end to set
     */
    public void setEnd(ClimateDate iend) {
        this.end = iend;
    }

    /**
     * @return the start_time
     */
    public ClimateTime getStartTime() {
        return startTime;
    }

    /**
     * @param istart_time
     *            the start_time to set
     */
    public void setStartTime(ClimateTime istart_time) {
        this.startTime = istart_time;
    }

    /**
     * @return the end_time
     */
    public ClimateTime getEndTime() {
        return endTime;
    }

    /**
     * @param iend_time
     *            the end_time to set
     */
    public void setEndTime(ClimateTime iend_time) {
        this.endTime = iend_time;
    }

    /**
     * Set all data to missing.
     */
    public void setDataToMissing() {
        start = ClimateDate.getMissingClimateDate();
        end = ClimateDate.getMissingClimateDate();
        startTime = ClimateTime.getMissingClimateTime();
        endTime = ClimateTime.getMissingClimateTime();
    }

    /**
     * @param iPeriodDesc
     *            {@link PeriodDesc} to use to form an new instance.
     * @return an instance based on the given period.
     * @throws ClimateInvalidParameterException
     */
    public static ClimateDates getClimateDatesFromPeriodDesc(
            PeriodDesc iPeriodDesc) throws ClimateInvalidParameterException {
        if (iPeriodDesc.isUseCustom()) {
            return iPeriodDesc.getDates();
        }
        PeriodType periodType = iPeriodDesc.getPeriodType();
        switch (periodType) {
        case MONTHLY_RAD:
        case MONTHLY_NWWS:
            return new ClimateDates(iPeriodDesc.getMonth(),
                    iPeriodDesc.getYear());
        case ANNUAL_RAD:
        case ANNUAL_NWWS:
            return new ClimateDates(iPeriodDesc.getYear());
        case SEASONAL_RAD:
        case SEASONAL_NWWS:
            return new ClimateDates(iPeriodDesc.getSeasonType(),
                    iPeriodDesc.getYear());
        default:
            throw new ClimateInvalidParameterException(
                    "Invalid period type: [" + periodType + "]");
        }
    }

    /**
     * @return an instance with all missing data.
     */
    public static ClimateDates getMissingClimateDates() {
        ClimateDates dates = new ClimateDates();
        dates.setDataToMissing();
        return dates;
    }

    @Override
    public boolean equals(Object iOther) {
        if (iOther != null && iOther instanceof ClimateDates) {
            ClimateDates otherDates = (ClimateDates) iOther;
            if (Objects.equals(start, otherDates.getStart())
                    && Objects.equals(end, otherDates.getEnd())
                    && Objects.equals(startTime, otherDates.getStartTime())
                    && Objects.equals(endTime, otherDates.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return start and end dates for full previous season
     * @throws ClimateInvalidParameterException
     */
    public static ClimateDates getPreviousSeasonDates()
            throws ClimateInvalidParameterException {
        PeriodDesc seasonPeriod = PeriodDesc.getPreviousSeasonPeriodDesc();
        return new ClimateDates(seasonPeriod.getSeasonType(),
                seasonPeriod.getYear());
    }

    /**
     * @return start and end dates for full previous month
     */
    public static ClimateDates getPreviousMonthDates() {
        Calendar cal = TimeUtil.newCalendar();
        cal.add(Calendar.MONTH, -1);
        ClimateDate date = new ClimateDate(cal.getTime());
        return new ClimateDates(date.getMon(), date.getYear());
    }

    /**
     * @return start and end dates for previous full year
     */
    public static ClimateDates getPreviousYearDates() {
        Calendar cal = TimeUtil.newCalendar();
        cal.add(Calendar.YEAR, -1);
        ClimateDate date = new ClimateDate(cal.getTime());
        return new ClimateDates(date.getYear());
    }

    /**
     * Parse ClimateDates (start date and hour, end date and hour) from
     * monthly/season/annual table values with yyyy-MM-dd HH format.
     * 
     * @param iStartDateString
     * @param iEndDateString
     * @return ClimateDate
     */
    public static ClimateDates parseClimateDatesFromStrings(
            Object iStartDateString, Object iEndDateString) {

        Calendar cal = TimeUtil.newCalendar();

        ClimateDate startDate = ClimateDate.getMissingClimateDate();
        int startHour = ParameterFormatClimate.MISSING_HOUR;

        try {
            if (iStartDateString != null
                    && (iStartDateString instanceof String)) {
                cal.setTime(getFullDateHourFormat()
                        .parse((String) iStartDateString));

                startDate = new ClimateDate(cal);

                startHour = cal.get(Calendar.HOUR_OF_DAY);

            }
        } catch (ParseException e) {
            /*
             * Date strings without hours are also passed to this method which
             * would cause a parse exception to be thrown above.
             */
            try {
                cal.setTime(ClimateDate.getFullDateFormat()
                        .parse((String) iStartDateString));
                startDate = new ClimateDate(cal);

            } catch (ParseException e1) {
                logger.warn("Could not parse date string: [" + iStartDateString
                        + "]. Missing values will be used. " + e.getMessage());

                startDate = ClimateDate.getMissingClimateDate();
                startHour = ParameterFormatClimate.MISSING_HOUR;
            }

        }

        ClimateDate endDate = ClimateDate.getMissingClimateDate();
        int endHour = ParameterFormatClimate.MISSING_HOUR;

        try {
            if (iEndDateString != null && (iEndDateString instanceof String)) {
                cal.setTime(
                        getFullDateHourFormat().parse((String) iEndDateString));

                endDate = new ClimateDate(cal);

                endHour = cal.get(Calendar.HOUR_OF_DAY);

            }
        } catch (ParseException e) {
            /*
             * Date strings without hours are also passed to this method which
             * would cause a parse exception to be thrown above.
             */
            try {
                cal.setTime(ClimateDate.getFullDateFormat()
                        .parse((String) iEndDateString));
                endDate = new ClimateDate(cal);

            } catch (ParseException e1) {
                logger.warn("Could not parse date string: [" + iEndDateString
                        + "]. Missing values will be used. " + e.getMessage());

                endDate = ClimateDate.getMissingClimateDate();
                endHour = ParameterFormatClimate.MISSING_HOUR;
            }

        }

        return new ClimateDates(startDate, endDate, startHour, endHour);
    }

    /**
     * Based off need to provide similar functionality as is in
     * build_daily_obs_weather.ec.
     * 
     * @return the interval of these dates in whole hour resolution (minutes and
     *         seconds will be ignored).
     * @throws ClimateInvalidParameterException
     *             if one of the dates are missing year, month, or day, or if
     *             one of the times is missing hour.
     */
    public int getIntervalInHours() throws ClimateInvalidParameterException {
        if (start.isPartialMissing()) {
            throw new ClimateInvalidParameterException(
                    "Start Date is missing data: [" + start.toFullDateString()
                            + "]");
        } else if (end.isPartialMissing()) {
            throw new ClimateInvalidParameterException(
                    "End Date is missing data: [" + end.toFullDateString()
                            + "]");
        } else if (startTime.getHour() == ParameterFormatClimate.MISSING_HOUR) {
            throw new ClimateInvalidParameterException(
                    "Start Time is missing hour");
        } else if (endTime.getHour() == ParameterFormatClimate.MISSING_HOUR) {
            throw new ClimateInvalidParameterException(
                    "End Time is missing hour");
        } else {
            Calendar startCal = TimeUtil.newCalendar();
            startCal.clear();
            // Java calendar starts months at 0
            startCal.set(start.getYear(), start.getMon() - 1, start.getDay(),
                    startTime.getHour(), 0);
            Calendar endCal = TimeUtil.newCalendar();
            endCal.clear();
            // Java calendar starts months at 0
            endCal.set(end.getYear(), end.getMon() - 1, end.getDay(),
                    endTime.getHour(), 0);

            long duration = endCal.getTimeInMillis()
                    - startCal.getTimeInMillis();
            return (int) TimeUnit.MILLISECONDS.toHours(duration);
        }
    }

    @Override
    public String toString() {
        return start.toFullDateString() + " " + startTime.toFullString()
                + " to " + end.toFullDateString() + " "
                + endTime.toFullString();
    }

    /**
     * @return Format for full date and hour (yyyy-MM-dd HH).
     */
    public static SimpleDateFormat getFullDateHourFormat() {
        return new SimpleDateFormat("yyyy" + ClimateDate.DATE_SEPARATOR + "MM"
                + ClimateDate.DATE_SEPARATOR + "dd HH");
    }

    /**
     * @return true if all fields are either null or missing all data.
     */
    public boolean isMissing() {
        return ((start == null || start.isMissing())
                && (end == null || end.isMissing())
                && (startTime == null || startTime.isMissing())
                && (endTime == null || endTime.isMissing()));
    }
}

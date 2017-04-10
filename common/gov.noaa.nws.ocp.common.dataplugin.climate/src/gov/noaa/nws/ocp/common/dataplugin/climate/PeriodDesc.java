/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate;

import java.util.Calendar;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

/**
 * Class for describing a period (month, seasonal, annual).
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 22 JUL 2016  20414      amoore      Initial creation
 * O7 OCT 2016  20636      wpaintsil   Init dates in the constructors.
 * 15 MAR 2017  30162      amoore      Fix exception throwing.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * </pre>
 * 
 * @author amoore
 */
@DynamicSerialize
public class PeriodDesc {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PeriodDesc.class);

    /**
     * Period type.
     */
    @DynamicSerializeElement
    private PeriodType periodType;

    /**
     * Season type.
     */
    @DynamicSerializeElement
    private SeasonType seasonType;

    /**
     * Year.
     */
    @DynamicSerializeElement
    private int year;

    /**
     * Month.
     */
    @DynamicSerializeElement
    private int month;

    /**
     * Custom dates.
     */
    @DynamicSerializeElement
    private ClimateDates dates;

    /**
     * Flag for if custom dates are to be used.
     */
    @DynamicSerializeElement
    private boolean useCustom;

    /**
     * Empty constructor.
     */
    public PeriodDesc() {
    }

    /**
     * Season constructor. Also sets period type to
     * {@link PeriodType#SEASONAL_RAD}.
     * 
     * @param iSeason
     *            season to set.
     * @param iYear
     *            year to set.
     * @throws ClimateInvalidParameterException
     */
    public PeriodDesc(SeasonType iSeason, int iYear)
            throws ClimateInvalidParameterException {
        periodType = PeriodType.SEASONAL_RAD;
        seasonType = iSeason;
        year = iYear;
        useCustom = false;
        dates = ClimateDates.getClimateDatesFromPeriodDesc(this);
    }

    /**
     * Annual constructor. Also sets period type to
     * {@link PeriodType#ANNUAL_RAD}.
     * 
     * @param iYear
     *            year to set.
     * @throws ClimateInvalidParameterException
     */
    public PeriodDesc(int iYear) throws ClimateInvalidParameterException {
        periodType = PeriodType.ANNUAL_RAD;
        year = iYear;
        useCustom = false;
        dates = ClimateDates.getClimateDatesFromPeriodDesc(this);
    }

    /**
     * Month constructor. Also sets period type to
     * {@link PeriodType#MONTHLY_RAD}.
     * 
     * @param iYear
     *            year to set.
     * @param iMonth
     *            month to set.
     * @throws ClimateInvalidParameterException
     */
    public PeriodDesc(int iYear, int iMonth)
            throws ClimateInvalidParameterException {
        periodType = PeriodType.MONTHLY_RAD;
        year = iYear;
        month = iMonth;
        useCustom = false;
        dates = ClimateDates.getClimateDatesFromPeriodDesc(this);
    }

    /**
     * Custom period constructor. Will set "useCustom" flag to false if given
     * dates match the given period type, and true otherwise.
     * 
     * @param iPeriodType
     *            the period type for which custom dates are selected.
     * @param iDates
     *            start and end dates.
     * @throws ClimateInvalidParameterException
     *             on invalid period type.
     */
    public PeriodDesc(PeriodType iPeriodType, ClimateDates iDates)
            throws ClimateInvalidParameterException {
        periodType = iPeriodType;
        dates = iDates;
        ClimateDate startDate = dates.getStart();
        ClimateDate endDate = dates.getEnd();
        switch (iPeriodType) {
        case MONTHLY_NWWS:
        case MONTHLY_RAD:
            if ((startDate.getDay() != 1)
                    || (startDate.getMon() != endDate.getMon())
                    || (startDate.getYear() != endDate.getYear())
                    || (endDate.getDay() != ClimateUtilities
                            .daysInMonth(endDate))) {
                // start day is not the first of the month, or the months are
                // not equal, or the years are not equal, or the end day is not
                // the last day of the month; custom dates
                useCustom = true;
            } else {
                useCustom = false;
                year = startDate.getYear();
                month = startDate.getMon();
            }
            break;
        case ANNUAL_NWWS:
        case ANNUAL_RAD:
            if ((startDate.getDay() != 1) || (startDate.getMon() != 1)
                    || (startDate.getYear() != endDate.getYear())
                    || (endDate.getMon() != 12) || (endDate.getDay() != 31)) {
                // start date is not January 1st, or start and end year are
                // different, or end date is not December 31st; custom dates
                useCustom = true;
            } else {
                useCustom = false;
                year = startDate.getYear();
            }
            break;
        case SEASONAL_NWWS:
        case SEASONAL_RAD:
            if ((startDate.getDay() != 1) || (endDate
                    .getDay() != ClimateUtilities.daysInMonth(endDate))) {
                // start date is not the first of its month, or end date is not
                // the end of its month; custom dates
                useCustom = true;
            } else {
                // check possible seasons
                switch (startDate.getMon()) {
                case 12:
                    // winter
                    if ((startDate.getYear() == endDate.getYear() - 1)
                            && (endDate.getMon() == 2)) {
                        // start date's year is 1 prior to end date, and end
                        // date month is Feb.; valid winter
                        useCustom = false;
                        seasonType = SeasonType.DJF_WINTER;
                        year = endDate.getYear();
                    } else {
                        useCustom = true;
                    }
                    break;
                case 3:
                    // spring
                    if ((startDate.getYear() == endDate.getYear())
                            && (endDate.getMon() == 5)) {
                        // start date's year is same as end date, and end month
                        // is May; valid spring
                        useCustom = false;
                        seasonType = SeasonType.MAM_SPRING;
                        year = endDate.getYear();
                    } else {
                        useCustom = true;
                    }
                    break;
                case 6:
                    // summer
                    if ((startDate.getYear() == endDate.getYear())
                            && (endDate.getMon() == 8)) {
                        // start date's year is same as end date, and end month
                        // is Aug.; valid spring
                        useCustom = false;
                        seasonType = SeasonType.JJA_SUMMER;
                        year = endDate.getYear();
                    } else {
                        useCustom = true;
                    }
                    break;
                case 9:
                    // fall
                    if ((startDate.getYear() == endDate.getYear())
                            && (endDate.getMon() == 11)) {
                        // start date's year is same as end date, and end month
                        // is Nov.; valid spring
                        useCustom = false;
                        seasonType = SeasonType.SON_FALL;
                        year = endDate.getYear();
                    } else {
                        useCustom = true;
                    }
                    break;
                default:
                    // not a valid start month for a seasonal; custom dates
                    useCustom = true;
                }
            }
            break;
        default:
            logger.warn("Unhandled period type [" + iPeriodType + "]");
            throw new ClimateInvalidParameterException(
                    "Invalid value for Period Type [" + iPeriodType + "]");
        }
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public SeasonType getSeasonType() {
        return seasonType;
    }

    public void setSeasonType(SeasonType seasonType) {
        this.seasonType = seasonType;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public ClimateDates getDates() {
        return dates;
    }

    /**
     * Sets the custom dates to use instead of dates based on {@link PeriodType}
     * . Does not set the flag for using custom dates.
     * 
     * @param dates
     */
    public void setDates(ClimateDates dates) {
        this.dates = dates;
    }

    /**
     * @return true if custom dates are to be used, instead of dates based on
     *         {@link PeriodType}.
     */
    public boolean isUseCustom() {
        return useCustom;
    }

    /**
     * @param useCustom
     *            the useCustom to set
     */
    public void setUseCustom(boolean useCustom) {
        this.useCustom = useCustom;
    }

    @Override
    public String toString() {
        if (useCustom) {
            // custom
            return "Custom Period Type " + periodType + ", "
                    + dates.getStart().toFullDateString() + " to "
                    + dates.getEnd().toFullDateString();
        }
        switch (periodType) {
        case ANNUAL_RAD:
        case ANNUAL_NWWS:
            // annual
            return "Annual, " + year;
        case SEASONAL_RAD:
        case SEASONAL_NWWS:
            // seasonal
            return "Seasonal, " + seasonType + " " + year;
        case MONTHLY_RAD:
        case MONTHLY_NWWS:
            // monthly
            return "Monthly, " + month + "/" + year;
        default:
            // unhandled period type
            return "Unhandled period type [" + periodType + "]";
        }
    }

    /**
     * @return period desc for previous season
     * @throws ClimateInvalidParameterException
     */
    public static PeriodDesc getPreviousSeasonPeriodDesc()
            throws ClimateInvalidParameterException {
        Calendar cal = TimeUtil.newCalendar();
        SeasonType season = null;
        int year = cal.get(Calendar.YEAR);
        // figure out PREVIOUS season
        switch (cal.get(Calendar.MONTH)) {
        case Calendar.JANUARY:
        case Calendar.FEBRUARY:
            // previous season is in the previous year
            year = cal.get(Calendar.YEAR) - 1;
        case Calendar.DECEMBER:
            // previous season is fall
            season = SeasonType.SON_FALL;
            break;
        case Calendar.MARCH:
        case Calendar.MAY:
        case Calendar.APRIL:
            // previous season is winter
            season = SeasonType.DJF_WINTER;
            break;
        case Calendar.JUNE:
        case Calendar.JULY:
        case Calendar.AUGUST:
            // previous season is this year's spring
            season = SeasonType.MAM_SPRING;
            break;
        case Calendar.SEPTEMBER:
        case Calendar.OCTOBER:
        case Calendar.NOVEMBER:
            // previous season is this year's summer
            season = SeasonType.JJA_SUMMER;
            break;
        default:
            logger.error("Could not parse previous season for date: ["
                    + cal.getTime().toString() + "]");
        }
        return new PeriodDesc(season, year);
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * This class contains common Climate data values and utilities.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08 JUL 2016  20414      amoore      Initial creation.
 * 06 OCT 2016  21378      amoore      Added daylight savings check.
 * 17 OCT 2016  21378      amoore      Added rounding by number of decimal places.
 * 02 NOV 2016  21378      amoore      Correct KNOTS_TO_MPH constant to unrounded number.
 * 07 NOV 2016  21378      amoore      Added more common calculations.
 * 14 NOV 2016  21378      amoore      Add time-related constants.
 * 22 NOV 2016  20636      wpaintsil   Took calcCoolDays, calcHeatDays, and AVG_TEMP 
 *                                     constants from ClimateDAOUtil for use in QCDialog.
 * 15 DEC 2016  27015      amoore      Test equality of lists up to a size.
 * 22 DEC 2016  20772      amoore      Move max days per month array to Climate Norm DAO, since
 *                                     that is the only applicable place for it.
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 26 JAN 2017  27017      amoore      Millisecond constants.
 * 22 FEB 2017  28609      amoore      Add build resulstant wind to common utilities.
 * 17 MAR 2017  21099      wpaintsil   Add fahrenheit to celsius conversion.
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 19 APR 2017  33104      amoore      Create {@link Timestamp} instance from Climate objects.
 * 03 MAY 2017  33104      amoore      Address FindBugs.
 * 16 MAY 2017  33104      amoore      Add float comparison method.
 * 15 JUN 2017  35184      amoore      Fix issue with resultant wind calculations where wind sums
 *                                     were not scaled down by number of observed hours.
 * 16 JUN 2017  35185      amoore      Move List comparison to Period Dialog, as that is the only user.
 * </pre>
 * 
 * @author amoore
 */
public final class ClimateUtilities {

    /**
     * Seconds in six hours.
     */
    public static final int SECONDS_IN_SIX_HOURS = TimeUtil.HOURS_PER_QUARTER_DAY
            * TimeUtil.SECONDS_PER_HOUR;

    /**
     * Seconds in three hours.
     */
    public static final int SECONDS_IN_THREE_HOURS = SECONDS_IN_SIX_HOURS / 2;

    /**
     * Milliseconds in six hours.
     */
    public static final long MILLISECONDS_IN_SIX_HOURS = TimeUtil.MILLIS_PER_HOUR
            * 6;

    /**
     * Milliseconds in three hours.
     */
    public static final long MILLISECONDS_IN_THREE_HOURS = MILLISECONDS_IN_SIX_HOURS
            / 2;

    /**
     * Multiplier for knots to mph.
     */
    public static final double KNOTS_TO_MPH = 1.150776775;

    /**
     * Zero degrees Celsius in Kelvin.
     */
    public static final double ZERO_C_IN_K = 273.16;

    /**
     * Latent heat of vaporization in Joules per Kilogram, at 0 degrees Celsius.
     */
    public static final double LATENT_HEAT_AT_0C = 2.5E+6;

    /**
     * Zero degrees Celsius in Fahrenheit.
     */
    public static final double ZERO_C_IN_F = 32;

    /**
     * Celsius degrees per Fahrenheit degrees.
     */
    public static final double C_PER_F = 5.0 / 9.0;

    /**
     * Vapor pressure at 0 Celsius of water.
     */
    public static final double WATER_VAPOR_PRESSURE_AT_0C = 6.11;

    /**
     * Specific heat of water vapor.
     */
    public static final double SPECIFIC_HEAT_WATER_VAPOR = 461.5;

    /**
     * Constant A1 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A1 = 6984.505294;

    /**
     * Constant A2 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A2 = -188.9039310;

    /**
     * Constant A3 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A3 = 2.133357675;

    /**
     * Constant A4 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A4 = -1.288580973E-2;

    /**
     * Constant A5 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A5 = 4.393587233E-5;

    /**
     * Constant A6 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A6 = -8.023923082E-8;

    /**
     * Constant A7 for approximating polynomial for the computation of
     * saturation vapor pressure in millibars using Kelvin.
     */
    private static final double VAPOR_PRESSURE_POLYNOMIAL_K_A7 = 6.136820929E-11;

    /**
     * Average cooling temp. Above this we have cooling degree days.
     */
    public static final int AVG_TEMP_COOL = 65;

    /**
     * Average heating temp. Below this we have heating degree days.
     */
    public static final int AVG_TEMP_HEAT = 65;

    /**
     * Epsilon for floating point comparison.
     * 
     * 1. Climate typically does not deal with user/report values with more than
     * 2 decimal points of precision, and values that have decimal precision are
     * less than 250 (max speed value) (so 0.001 would work for this).
     * 
     * 2. For some fields, such as precip, users may enter more precision, but
     * are very unlikely to, and it would not be more than one or two decimal
     * places (so combining with #1, 0.00001 would work for this).
     * 
     * 3. Conversion between knots and mph uses 3 decimal places with no change
     * in magnitude, and highest precision for a speed is a single decimal place
     * (so 0.00001 would work for this).
     * 
     * Thus, 0.00001 is acceptable.
     */
    private static final double EPSILON = 0.00001;

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateUtilities() {
    }

    /**
     * @param iDate
     *            date containing at least year and month to check.
     * @return days in a month for the given month/year in the given date.
     */
    public static int daysInMonth(ClimateDate iDate) {
        return daysInMonth(iDate.getYear(), iDate.getMon());
    }

    /**
     * @param iYear
     *            year to check.
     * @param iMonth
     *            month to check.
     * @return days in a month for the given month/year.
     */
    public static int daysInMonth(int iYear, int iMonth) {
        // subtract 1 from month per Java month indexing
        GregorianCalendar calendar = new GregorianCalendar(iYear, iMonth - 1,
                1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * written from ninttemp.c
     * 
     * Original comments:
     * 
     * <pre>
     * ninttemp.c   David T. Miller   PRC/TDL   March 2000
     * Purpose - In accordance with World Meteorological Organization (WMO)
     *           standards, negative temperatures are rounded to 
     *           nearest integers slightly different than what one
     *           would expect.  Instead of rounding a number such as 
     *           -4.5 to -5, the WMO standard rounds to -4.  While this
     *           may seem to introduce an artificial warmth, it makes
     *           sense when one thinks in absolute temperature instead
     *           of arbitrary temperature scales such as Celsius.
     *        
     *           The subroutine will work much like the FORTRAN NINT except
     *           when the fractional part is exactly .5 for negative numbers.
     * </pre>
     * 
     * @param d
     * @return
     */
    public static int nint(double d) {
        return (int) (d > 0 ? d + 0.51 : d - 0.51);
    }

    /**
     * Follow logic of {@link #nint(double)}, allowing for the given number of
     * decimal places.
     * 
     * @param d
     * @param decimals
     * @return
     */
    public static double nint(double d, int decimals) {
        double multiplier = Math.pow(10, decimals);
        return nint(d * multiplier) / multiplier;
    }

    /**
     * written from ninttemp.c
     * 
     * Original comments:
     * 
     * <pre>
     * ninttemp.c   David T. Miller   PRC/TDL   March 2000
     * Purpose - In accordance with World Meteorological Organization (WMO)
     *           standards, negative temperatures are rounded to 
     *           nearest integers slightly different than what one
     *           would expect.  Instead of rounding a number such as 
     *           -4.5 to -5, the WMO standard rounds to -4.  While this
     *           may seem to introduce an artificial warmth, it makes
     *           sense when one thinks in absolute temperature instead
     *           of arbitrary temperature scales such as Celsius.
     *        
     *           The subroutine will work much like the FORTRAN NINT except
     *           when the fractional part is exactly .5 for negative numbers.
     * </pre>
     */
    public static int nint(float d) {
        return (int) (d > 0 ? d + 0.51 : d - 0.51);
    }

    /**
     * Follow logic of {@link #nint(float)}, allowing for the given number of
     * decimal places.
     * 
     * @param d
     * @param decimals
     * @return
     */
    public static float nint(float d, int decimals) {
        float multiplier = (float) Math.pow(10, decimals);
        return nint(d * multiplier) / multiplier;
    }

    /**
     * Migrated from determine_daylight_savings.f
     * 
     * <pre>
     * Oct. 1998     David O. Miller        PRC/TDL
     *
     *
     *   Purpose:  This routine will set up the arguments needed by
     *             and will call det_dst.c.
     * </pre>
     * 
     * Migrated from det_dst.c
     * 
     * <pre>
    *
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    * 
    *   This function acts as a wrapper around CLIMATEUtils::stdTime to prevent
    *   the arguments from being mangled when passed between fortran and
    *   C++.
     *
     * </pre>
     * 
     * Migrated from ClimateUtils.C::stdTime
     * 
     * <pre>
     * Name: tzStdTime()
    * Purpose: to calculate difference between UTC and time zone specified by
    *          the argument tz
     *
     * </pre>
     * 
     * @param aDate
     * @param numOffUTC
     * @return
     * @throws ClimateInvalidParameterException
     */
    public static boolean determineDaylightSavings(ClimateDate aDate,
            short numOffUTC) throws ClimateInvalidParameterException {
        // Method was almost completely re-written from Legacy due to much more
        // modern capabilities present in Java
        // time zones use full names for clarity when able
        String timeZone;
        // Pick the timezone based on the UTC offset
        switch (numOffUTC) {
        case 1:
            // Middle European Time
            // legacy:
            // timeZone = "MET-1METDST";
            // migrated:
            timeZone = "MET";
            break;
        case 0:
            // Greenwich Mean Time
            // legacy:
            // timeZone = "GMT0BST";
            // migrated, check against BST only:
            timeZone = "BST";
            break;
        case -4:
            // Atlantic time
            // legacy:
            // timeZone = "AST4ADT";
            // migrated:
            timeZone = "Canada/Atlantic";
            break;
        case -5:
            // Eastern time
            // legacy:
            // timeZone = "EST5EDT";
            // migrated:
            timeZone = "US/Eastern";
            break;
        case -6:
            // Central time
            // legacy:
            // timeZone = "CST6CDT";
            // migrated:
            timeZone = "US/Central";
            break;
        case -7:
            // Mountain time
            // legacy:
            // timeZone = "MST7MDT";
            // migrated:
            timeZone = "US/Mountain";
            break;
        case -8:
            // Pacific time
            // legacy:
            // timeZone = "PST8PDT";
            // migrated:
            timeZone = "US/Pacific";
            break;
        case -9:
            // Yukon/Alaska time
            // The following line changed for new Alaska Time Zone Change
            // 09/18/2007 MSikder
            // legacy: this timezone does not exist as written
            // timeZone = "AKST9AKDT";
            // migrated:
            timeZone = "US/Alaska";
            break;
        case -10:
            // Aleutian time
            // legacy: this timezone does not exist as written
            // timeZone = "AST10ADT";
            // migrated:
            timeZone = "US/Aleutian";
            break;
        case -11:
            // (not present in legacy) Samoan time
            timeZone = "Pacific/Samoa";
            break;
        case 10:
            // (not present in legacy) Guam time
            timeZone = "Pacific/Guam";
            break;
        default:
            throw new ClimateInvalidParameterException(
                    "Unknown UTC offset: [" + numOffUTC + "]");
        }

        return TimeZone.getTimeZone(timeZone)
                .inDaylightTime(aDate.getCalendarFromClimateDate().getTime());
    }

    /**
     * Migrated from latent_heat.f.
     * 
     * <pre>
     * August 1998 Jason P. Tuell PRC/TDL
     *
     *
     * Purpose: This function calculates the latent heat of evaporation as a
     * function of the input temperature.
     *
     * Reference: An Introduction to Atmospheric Physics Fleagle and Businger,
     * Academic Press, 1963
     * 
     * @param tempK
     *            temperature in Kelvin.
     * @return
     */
    public static double latentHeat(double tempK) {
        /*
         * Function that adjusts the latent heat of evaporation for temperature
         */
        return LATENT_HEAT_AT_0C - (22.74 * (tempK - ZERO_C_IN_K));
    }

    /**
     * Migrated from f_to_K.f. Convert fahrenheit to kelvin.
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This function converts degrees Fahrenheit to degrees 
    *             Kelvin.
     *
     * </pre>
     * 
     * @param tempF
     * @return
     */
    public static double fahrenheitToKelvin(double tempF) {
        double zeroc_f = 32;
        double slope = 5.0 / 9.0;

        return ZERO_C_IN_K + ((tempF - zeroc_f) * slope);
    }

    /**
     * Convert celsius to fahrenheit.
     * 
     * @param tempC
     * @return
     */
    public static double celsiusToFahrenheit(double tempC) {
        return (tempC / C_PER_F) + ZERO_C_IN_F;
    }

    /**
     * Migrated from f_to_c.f. Convert fahrenheit to celsius.
     * 
     * <pre>
    *  Jan 1998       Jason Tuell        PRC
    *
    *  Purpose:  Function converts input Farenheit temperature to temperature
    *            Celsius.
     *
     *
     * </pre>
     */
    public static double fahrenheitToCelsius(double tempF) {
        return ((tempF - ZERO_C_IN_F)) / 1.8;
    }

    /**
     * Migrated from calculate_delta_hours.f.
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine calculates the number of hours difference in the
    *             starting and ending periods of a daily climate summary.  This 
    *             is done for the evening climate summaries whose period of data
    *             retrieval isn't a whole day.
     * 
     * </pre>
     * 
     * @param dateTimes
     *            datetime range to process. Assumed to be start and end
     *            datetimes within 24 hours of each other.
     * @return hours between the given start and end datetimes.
     */
    public static int calculateDeltaHours(ClimateDates dateTimes) {
        if (dateTimes.getStart().getDay() == dateTimes.getEnd().getDay()) {
            return dateTimes.getEndTime().getHour()
                    - dateTimes.getStartTime().getHour() + 1;
        } else {
            return dateTimes.getEndTime().getHour() + TimeUtil.HOURS_PER_DAY
                    - dateTimes.getStartTime().getHour();
        }
    }

    /**
     * Migrated from e_from_t.f.
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This function calculates the vapor pressure for a given 
    *             input temperature.  It primarily uses a 6th order 
    *             polynomial which is accurate to within 1% over the range
    *             +50 C to -50 C.  We use an alternative expression if
    *             the input temperature is outside that range.
     *
     * </pre>
     * 
     * @param tempF
     *            in Fahrenheit
     * @return pressure in millibars.
     */
    public static double vaporPressureFromTemperature(double tempF) {
        double tempK = fahrenheitToKelvin(tempF);

        // Calculate the vapor pressure

        /*
         * Use the polynomial if the temperature is between -50 C and +50 C;
         * otherwise, use a somewhat less accurate expression. The polynomial is
         * preferred because it is very fast.
         */
        double tempC = tempK - ZERO_C_IN_K;

        if ((-50 < tempC) && (tempC < 50)) {
            return VAPOR_PRESSURE_POLYNOMIAL_K_A1
                    + tempK * (VAPOR_PRESSURE_POLYNOMIAL_K_A2
                            + tempK * (VAPOR_PRESSURE_POLYNOMIAL_K_A3
                                    + tempK * (VAPOR_PRESSURE_POLYNOMIAL_K_A4
                                            + tempK * (VAPOR_PRESSURE_POLYNOMIAL_K_A5
                                                    + tempK * (VAPOR_PRESSURE_POLYNOMIAL_K_A6
                                                            + tempK * VAPOR_PRESSURE_POLYNOMIAL_K_A7)))));
        } else {
            return WATER_VAPOR_PRESSURE_AT_0C
                    * Math.exp((latentHeat(tempK) / SPECIFIC_HEAT_WATER_VAPOR)
                            - (1.0 / tempK));
        }
    }

    /**
     * Converted from cool_days.f
     * 
     * Original comments:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
     *         Purpose:  This function calculates the number of cooling degree days
     *           for an input average temperature.
     *               Cooling degree days are defined as the difference between
     *           the daily average temperature and 65 Farenheit.  They are
     *           only calculated if the average temperature is greater than
     *           65 F.  If the average temperature is less than 65, the 
     *           number of cooling degree days is set to zero.
     * 
     * </pre>
     */
    public static int calcCoolDays(float avgTemp) {
        int iavg = nint(avgTemp);

        if (iavg > AVG_TEMP_COOL) {
            return iavg - AVG_TEMP_COOL;
        } else {
            return 0;
        }
    }

    /**
     * Converted from heat_days.f
     * 
     * Original comments:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
     *         Purpose:  This function calculates the number of heating degree days
     *           for an input average temperature.
     *               Heating degree days are defined as the difference between
     *           65 Farenheit and the daily average temperature.  They are
     *           only calculated if the average temperature is less than
     *           65 F.
     * </pre>
     */
    public static int calcHeatDays(float avgTemp) {
        int iavg = nint(avgTemp);

        if (iavg <= AVG_TEMP_HEAT) {
            return AVG_TEMP_HEAT - iavg;
        } else {
            return 0;
        }
    }

    /**
     * Rewritten as helper method from build_p_resultant_wind.f and
     * build_resultant_wind.f.
     * 
     * Build hourly resultant wind from X and Y sums, and given hours.
     * 
     * @param sumX
     * @param sumY
     * @param numObsHours
     * @return resultant wind, or missing.
     */
    public static ClimateWind buildResultantWind(double sumX, double sumY,
            int numObsHours) {
        if (numObsHours <= 0 || numObsHours == ParameterFormatClimate.MISSING
                || sumX == ParameterFormatClimate.MISSING_SPEED
                || sumY == ParameterFormatClimate.MISSING_SPEED) {
            return ClimateWind.getMissingClimateWind();
        } else {
            ClimateWind wind = ClimateWind.getMissingClimateWind();

            // valid values
            // resultant wind uses simple pythagorean theorem calculation
            // scale by hours
            sumX /= numObsHours;
            sumY /= numObsHours;

            double precision = 0.0000001;
            // only add the precision if a value = 0
            if (sumX == 0) {
                sumX += precision;
            }
            if (sumY == 0) {
                sumY += precision;
            }

            wind.setSpeed((float) Math.sqrt((sumX * sumX) + (sumY * sumY)));

            /*********************************************************************
             * Legacy documentation:
             * 
             * We need to adjust for negative directions. While negative
             * directions are perfectly acceptable mathematically, they don't
             * make sense meteorologically. We do this by first determining what
             * quadrant we are in and then adjusting accordingly.
             **********************************************************************/

            int idir = nint(Math.toDegrees(Math.atan(sumX / sumY)));

            /*
             * We will expect a direction from the above calculations to be
             * between -90 and 90 degrees.
             * 
             * Compass starts at north with 0 degrees, east 90 degrees, etc, so
             * expected direction from above calculations are west to north to
             * east (no south direction).
             */
            if (sumX < 0) {
                // left (west) half of compass
                if (sumY >= 0) {
                    // top-left (northwest) half of compass, reported as
                    // negative degrees. Add 360.
                    idir += 360;
                } else {
                    // bottom-left (southwest) half of compass, reported as
                    // positive degrees (northeast). Add 180.
                    idir += 180;
                }
            } else {
                // right (east) half of compass
                // no work needed if in top-right (northeast)
                if (sumY < 0) {
                    // bottom-right (southeast) half of compass, reported as
                    // negative degrees (northwest). Add 180.
                    idir += 180;
                }
            }

            // wind direction is in 10s of degrees
            wind.setDir(nint(((double) idir) / 10) * 10);

            return wind;
        }
    }

    /**
     * @param date
     * @return {@link Timestamp} instance.
     */
    public static Timestamp getSQLTimestamp(ClimateDate date) {
        return getSQLTimestamp(date, null);
    }

    /**
     * 
     * @param date
     * @param time
     * @return {@link Timestamp} instance.
     */
    public static Timestamp getSQLTimestamp(ClimateDate date,
            ClimateTime time) {
        Calendar cal = date.getCalendarFromClimateDate();
        if (time != null) {
            cal.set(Calendar.HOUR, time.getHour());
            if (time.getMin() != ParameterFormatClimate.MISSING_MINUTE) {
                cal.set(Calendar.MINUTE, time.getMin());
            }
        }
        return new Timestamp(cal.getTimeInMillis());
    }

    /**
     * 
     * @param f1
     * @param f2
     * @return true if the difference between the two given numbers is less than
     *         a constant epsilon value.
     */
    public static boolean floatingEquals(float f1, float f2) {
        return (Math.abs(f1 - f2) < EPSILON);
    }

    /**
     * 
     * @param d1
     * @param d2
     * @return true if the difference between the two given numbers is less than
     *         a constant epsilon value.
     */
    public static boolean floatingEquals(double d1, double d2) {
        return (Math.abs(d1 - d2) < EPSILON);
    }

    /**
     * Check if the two given lists are equal up to the given capacity (not
     * necessarily equal for all elements). A missing value for
     * {@link ClimateDate} or {@link ClimateDates} is considered equivalent to
     * no/null data. For {@link ClimateDate}, only check month and day for
     * equality. For {@link ClimateDates}, only check month and day of the
     * individual dates for equality.
     * 
     * @param iCapacity
     * @param iFirstList
     * @param iSecondList
     * @return true if equal, false otherwise.
     */
    public static boolean isListsEqualUpToCapacity(int iCapacity,
            java.util.List<?> iFirstList, java.util.List<?> iSecondList) {
        /*
         * check if the lists match up to the smaller of the given capacity and
         * (greater of the lists' capacities)
         */
        for (int i = 0; i < iCapacity
                && (i < iFirstList.size() || i < iSecondList.size()); i++) {
            if (i < iFirstList.size() && i < iSecondList.size()) {
                // both lists have an element
                if (iFirstList.get(i) instanceof ClimateDate
                        && iSecondList.get(i) instanceof ClimateDate) {
                    ClimateDate firstDate = (ClimateDate) iFirstList.get(i);
                    ClimateDate secondDate = (ClimateDate) iSecondList.get(i);

                    if (firstDate.getDay() != secondDate.getDay()
                            || firstDate.getMon() != secondDate.getMon()) {
                        return false;
                    }
                } else if (iFirstList.get(i) instanceof ClimateDates
                        && iSecondList.get(i) instanceof ClimateDates) {
                    ClimateDates firstDates = (ClimateDates) iFirstList.get(i);
                    ClimateDates secondDates = (ClimateDates) iSecondList
                            .get(i);

                    if (firstDates.getStart().getDay() != secondDates.getStart()
                            .getDay()
                            || firstDates.getStart().getMon() != secondDates
                                    .getStart().getMon()
                            || firstDates.getEnd().getDay() != secondDates
                                    .getEnd().getDay()
                            || firstDates.getEnd().getMon() != secondDates
                                    .getEnd().getMon()) {
                        return false;
                    }
                } else if (!iFirstList.get(i).equals(iSecondList.get(i))) {
                    return false;
                }
            } else {
                Object maybeMissingObject;

                if (i < iFirstList.size()) {
                    // first list has an element, second list does not
                    maybeMissingObject = iFirstList.get(i);
                } else {
                    // second list has an element, first list does not
                    maybeMissingObject = iSecondList.get(i);
                }

                if (maybeMissingObject instanceof ClimateDate) {

                    if (!((ClimateDate) maybeMissingObject).isMissing()) {
                        // not missing data
                        return false;
                    }
                } else if (maybeMissingObject instanceof ClimateDates) {

                    if (!((ClimateDates) maybeMissingObject).isMissing()) {
                        // not missing data
                        return false;
                    }
                } else {
                    // not an object that has an equivalent missing data
                    return false;
                }
            }
        }
        return true;
    }
}

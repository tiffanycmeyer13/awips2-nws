/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.util;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimoDatesDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.DailyClimateDAO;

/**
 * 
 * Implementations converted from SUBROUTINES under
 * adapt/climate/lib/src/climate_utils
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 2, 2016             xzhang      Initial creation
 * 13 JUL 2016  20414      amoore      Cleaning up DAO's C-structure implementation.
 * 19 AUG 2016  20753      amoore      EDEX common utils consolidation and cleanup. DAO thread safety.
 * 22 NOV 2016  23222      amoore      Legacy DR 15685, verifying rounding. Some clean up.
 * 22 NOV 2016  20636      wpaintsil   Moved calcCoolDays, calcHeatDays, and AVG_TEMP 
 *                                     constants to ClimateUtilities for use in QCDialog. 
 * 28 NOV 2016  22930      astrakovsky Added comments explaining Julian day conversions.
 * 12 DEC 2016  27015      amoore      Documented migration of build_derived_fields.f.
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 20 JUN 2017  35179      amoore      Fix issue with improper summing dates for season/year data.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public final class ClimateDAOUtils {

    /** The logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateDAOUtils.class);

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateDAOUtils() {
    }

    /**
     * Converted from update_cool.f
     * 
     * Original comments:
     * 
     * <pre>
     *     August 1998     Jason P. Tuell        PRC/TDL
     *        
     * Purpose:  This routine controls the determination of accumulated
     *           cooling degree days.  It determines the monthly 
     *           accumulated cooling degree days first, followed by the
     *           seasonal and lastly the annual cooling degree days.
     * 
     * 
     * Variables
     * 
     *    Input
     *      a_date         - derived TYPE that contains the date for this
     *                       climate summary
     *      current        - derived TYPE that holds the daily climate
     *                       data for this station
     *      cool_season    - derived TYPE that contains the begin and end
     *                       dates for the cooling degree day season
     *      cool_year      - derived TYPE that contains the begin and end
     *                       dates for the cooling degree day year
     *      inform_id      - INFORMIX station id
     * 
     *    Output
     *      current        - derived TYPE that holds the daily climate
     *                       data for this station
     * 
     *    Local
     *      avg_temp       - average temperature (F)
     *      iday           - Julian day
     *      missing        - flag for missing data
     *      sum_cool       - sum of the daily cooling degree days for the
     *                       period between start and end date
     * 
     *    Non-system routines used
     *      convert_julday        - converts an input Julian day into a date
     *      sum_cool_degree_days  - C routine that sums the daily cooling
     *                              degree days for this station for the
     *                              period between a start and end date
     * 
     * 
     *    Non-system functions used
     *      cool_days             - calculates the number of cooling degree
     *                              days given the average temperature
     *      jul_day               - calculates the Julian day for an input
     *                              date
     * 
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param coolSeason
     * @param coolYear
     * @param current
     */
    private static void updateCool(ClimateDate aDate, int stationId,
            ClimateDates coolSeason, ClimateDates coolYear,
            DailyClimateData current) {
        DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

        ClimateDate beginDate = ClimateDate.getMissingClimateDate();
        ClimateDate endDate = ClimateDate.getMissingClimateDate();

        if (current.getMaxTemp() != ParameterFormatClimate.MISSING
                && current.getMinTemp() != ParameterFormatClimate.MISSING) {
            float avgTemp = (current.getMaxTemp() + current.getMinTemp())
                    / 2.0f;
            current.setNumCool(ClimateUtilities.calcCoolDays(avgTemp));
        } else {
            current.setNumCool(ParameterFormatClimate.MISSING_DEGREE_DAY);
        }
        /*
         * Some rules for updating the cooling degree days.
         * 
         * 1. The monthly total is reset at the first of each month.
         * 
         * 2. The seasonal total is at the start of each season.
         * 
         * 3. The yearly total is reset at the start of each cooling season (1
         * July).
         * 
         * Calculate the accumulated monthly cooling degree days. Treat the
         * first of the month as a special case that requires INFORMIX calls.
         */
        int sumCool = 0;

        if (aDate.getDay() == 1) {
            current.setNumCoolMonth(current.getNumCool());
        } else {

            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the month.
             */

            beginDate.setDay(1);
            beginDate.setMon(aDate.getMon());
            beginDate.setYear(aDate.getYear());

            endDate.setDay(aDate.getDay() - 1);
            endDate.setMon(aDate.getMon());
            endDate.setYear(aDate.getYear());

            sumCool = dailyClimateDao.sumCoolDegreeDays(beginDate, endDate,
                    stationId);

            /*
             * Add today's cooling degree days to the total if it isn't
             * missing,; otherwise set the cooling degree days to missing
             */

            if (current
                    .getNumCool() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                if (sumCool != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    current.setNumCoolMonth(sumCool + current.getNumCool());
                } else {
                    current.setNumCoolMonth(current.getNumCool());
                }
            } else {
                current.setNumCoolMonth(sumCool);
            }

        }

        /*
         * Calculate the seasonal accumulated cooling degree days. Treat the
         * first day of the season as a special cast that requires no INFORMIX
         * calls.
         */

        sumCool = 0;

        if ((aDate.getDay() == coolSeason.getStart().getDay())
                && (aDate.getMon() == coolSeason.getStart().getMon())) {

            current.setNumCoolSeason(current.getNumCool());

        } else {

            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the month.
             */

            beginDate.setDay(coolSeason.getStart().getDay());
            beginDate.setMon(coolSeason.getStart().getMon());
            beginDate.setYear(coolSeason.getStart().getYear());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            int iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            sumCool = dailyClimateDao.sumCoolDegreeDays(beginDate, endDate,
                    stationId);

            // Add today's cooling degree days to the total if it isn't
            // missing,; otherwise set the cooling degree days to missing

            if (current
                    .getNumCool() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                if (sumCool != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    current.setNumCoolSeason(sumCool + current.getNumCool());
                } else {
                    current.setNumCoolSeason(current.getNumCool());
                }

            } else {
                current.setNumCoolSeason(sumCool);
            }

        }

        // Calculate the annual accumulated cooling degree days.
        // Treat the first day of the season as a special cast that
        // == ires no INFORMIX calls.

        sumCool = 0;

        if ((aDate.getDay() == coolYear.getStart().getDay())
                && (aDate.getMon() == coolYear.getStart().getMon())) {

            current.setNumCoolYear(current.getNumCool());

        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated cooling degree days
            // for days other than the first of the month.

            beginDate.setDay(coolYear.getStart().getDay());
            beginDate.setMon(coolYear.getStart().getMon());
            beginDate.setYear(coolYear.getStart().getYear());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            int iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            sumCool = dailyClimateDao.sumCoolDegreeDays(beginDate, endDate,
                    stationId);

            // Add today's cooling degree days to the total if it isn't
            // missing,; otherwise set the cooling degree days to missing

            if (current
                    .getNumCool() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                if (sumCool != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    current.setNumCoolYear(sumCool + current.getNumCool());
                } else {
                    current.setNumCoolYear(current.getNumCool());
                }

            } else {
                current.setNumCoolYear(sumCool);

            }

        }
    }

    /**
     * Converted from update_heat.f
     * 
     * <pre>
     * original comments: August 1998 Jason P. Tuell PRC/TDL
     * 
     * Purpose: This routine controls the determination of accumulated heating
     * degree days. It determines the monthly accumulated heating degree days
     * first, followed by the seasonal and lastly the annual heating degree
     * days.
     * 
     * <pre>
     * 
     * @param aDate
     * @param stationId
     * @param heatSeason
     * @param heatYear
     * @param current
     */
    private static void updateHeat(ClimateDate aDate, int stationId,
            ClimateDates heatSeason, ClimateDates heatYear,
            DailyClimateData current) {

        DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

        ClimateDate beginDate = ClimateDate.getMissingClimateDate();
        ClimateDate endDate = ClimateDate.getMissingClimateDate();

        if (current.getMaxTemp() != ParameterFormatClimate.MISSING
                && current.getMinTemp() != ParameterFormatClimate.MISSING) {
            float avgTemp = (current.getMaxTemp() + current.getMinTemp())
                    / 2.0f;
            current.setNumHeat(ClimateUtilities.calcHeatDays(avgTemp));
        } else {
            current.setNumHeat(ParameterFormatClimate.MISSING_DEGREE_DAY);
        }
        /*
         * Some rules for updating the heating degree days. 1. The monthly total
         * is reset at the first of each month. 2. The seasonal total is at the
         * start of each season. 3. The yearly total is reset at the start of
         * each heating season (1 July).
         * 
         * Calculate the accumulated monthly heating degree days. Treat the
         * first of the month as a special case that requires INFORMIX calls.
         */
        int sumHeat = 0;

        if (aDate.getDay() == 1) {
            current.setNumHeatMonth(current.getNumHeat());
        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated heating degree days
            // for days other than the first of the month.

            beginDate.setDay(1);
            beginDate.setMon(aDate.getMon());
            beginDate.setYear(aDate.getYear());

            endDate.setDay(aDate.getDay() - 1);
            endDate.setMon(aDate.getMon());
            endDate.setYear(aDate.getYear());

            sumHeat = dailyClimateDao.sumHeatDegreeDays(beginDate, endDate,
                    stationId);

            // Add today's heating degree days to the total if it isn't
            // missing,; otherwise set the heating degree days to missing

            if (current
                    .getNumHeat() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                if (sumHeat != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    current.setNumHeatMonth(sumHeat + current.getNumHeat());
                } else {
                    current.setNumHeatMonth(current.getNumHeat());
                }

            } else {
                current.setNumHeatMonth(sumHeat);
            }

        }

        // Calculate the seasonal accumulated heating degree days.
        // Treat the first day of the season as a special cast that
        // == ires no INFORMIX calls.

        sumHeat = 0;

        if ((aDate.getDay() == heatSeason.getStart().getDay())
                && (aDate.getMon() == heatSeason.getStart().getMon())) {

            current.setNumHeatSeason(current.getNumHeat());

        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated heating degree days
            // for days other than the first of the month.

            beginDate.setDay(heatSeason.getStart().getDay());
            beginDate.setMon(heatSeason.getStart().getMon());
            beginDate.setYear(heatSeason.getStart().getYear());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            int iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            sumHeat = dailyClimateDao.sumHeatDegreeDays(beginDate, endDate,
                    stationId);

            // Add today's heating degree days to the total if it isn't
            // missing,; otherwise set the heating degree days to missing

            if (current
                    .getNumHeat() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                if (sumHeat != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    current.setNumHeatSeason(sumHeat + current.getNumHeat());
                } else {
                    current.setNumHeatSeason(current.getNumHeat());
                }

            } else {
                current.setNumHeatSeason(sumHeat);
            }

        }

        // Calculate the annual accumulated heating degree days.
        // Treat the first day of the season as a special cast that
        // == ires no INFORMIX calls.

        sumHeat = 0;

        if ((aDate.getDay() == heatYear.getStart().getDay())
                && (aDate.getMon() == heatYear.getStart().getMon())) {

            current.setNumHeatYear(current.getNumHeat());

        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated heating degree days
            // for days other than the first of the month.

            beginDate.setDay(heatYear.getStart().getDay());
            beginDate.setMon(heatYear.getStart().getMon());
            beginDate.setYear(heatYear.getStart().getYear());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            int iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            sumHeat = dailyClimateDao.sumHeatDegreeDays(beginDate, endDate,
                    stationId);

            // Add today's heating degree days to the total if it isn't
            // missing,; otherwise set the heating degree days to missing

            if (current
                    .getNumHeat() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                if (sumHeat != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    current.setNumHeatYear(sumHeat + current.getNumHeat());
                } else {
                    current.setNumHeatYear(current.getNumHeat());
                }
            } else {
                current.setNumHeatYear(sumHeat);
            }
        }

    }

    /**
     * Converted from update_precip.f
     * 
     * Original comments:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
     *  
     * Purpose:  This routine controls the determination of accumulated
     *           precipitation.  It determines the monthly 
     *           accumulated precipitation first, followed by the
     *           seasonal and lastly the annual precipitation.
     * 
     * 
     * Variables
     * 
     *    Input
     *      a_date         - derived TYPE that contains the date for this
     *                       climate summary
     *      current        - derived TYPE that holds the daily climate
     *                       data for this station
     *      precip_season  - derived TYPE that contains the begin and end
     *                       dates for the precipitation season
     *      precip_year    - derived TYPE that contains the begin and end
     *                       dates for the precipitation year
     *      inform_id      - INFORMIX station id
     * 
     *    Output
     *      current        - derived TYPE that holds the daily climate
     *                       data for this station
     * 
     *    Local
     *      avg_temp       - average temperature (F)
     *      iday           - Julian day
     *      missing        - flag for missing data
     *      precip_sum     - sum of the daily precipitation for the
     *                       period between start and end date
     * 
     *    Non-system routines used
     *      convert_julday        - converts an input Julian day into a date
     *      sum_precip            - C routine that sums the daily 
     *                              precipitation for this station for the
     *                              period between a start and end date
     * 
     * 
     *    Non-system functions used
     *      jul_day               - calculates the Julian day for an input
     *                              date
     * 
     * 
     * 
     * MODIFICATION LOG
     * NAME         DATE        CHANGE
     * David T. Miller      April 2000      Updated if tests to ensure 
     *                             trace totals are correctly
     *                 taken into account
     * David T. Miller      Aug 2000        Missed one small test in the
     *                                     ifs.  If the day's precip
     *                                     was 0, it wouldn't take
     *                                     trace into account.
     * 
     * </pre>
     * 
     * @param aDate
     * @param informId
     * @param precipSeason
     * @param precipYear
     * @param current
     */
    private static void updatePrecip(ClimateDate aDate, int informId,
            ClimateDates precipSeason, ClimateDates precipYear,
            DailyClimateData current) {

        DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

        ClimateDate beginDate = ClimateDate.getMissingClimateDate();
        ClimateDate endDate = ClimateDate.getMissingClimateDate();
        int iday;

        float precipSum = 0;
        // Some rules for updating the precipitation.
        // 1. The monthly total is reset at the first of each month.
        // 2. The seasonal total is at the start of each season.
        // 3. The yearly total is reset at the start of each
        // precipitation season.
        // Calculate the accumulated monthly precipitation.
        // Treat the first of the month as a special case that
        // requires INFORMIX calls.
        if (aDate.getDay() == 1) {
            current.setPrecipMonth(current.getPrecip());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated precipitation
            // for days other than the first of the month.
            beginDate = new ClimateDate(1, aDate.getMon(), aDate.getYear());
            endDate = new ClimateDate(aDate.getDay() - 1, aDate.getMon(),
                    aDate.getYear());

            precipSum = dailyClimateDao.sumPrecip(beginDate, endDate, informId);
            // Add today's precipitation to the total if it isn't
            // missing,; otherwise set the precipitation to missing
            // except where trace amounts are involved

            if ((current.getPrecip() != ParameterFormatClimate.MISSING_PRECIP)
                    && (current.getPrecip() != ParameterFormatClimate.TRACE)
                    && (current.getPrecip() != 0)) {

                if ((precipSum != ParameterFormatClimate.MISSING_PRECIP)
                        && (precipSum != ParameterFormatClimate.TRACE)) {
                    current.setPrecipMonth(precipSum + current.getPrecip());
                } else {
                    current.setPrecipMonth(current.getPrecip());
                }

            } else if (((current.getPrecip() == ParameterFormatClimate.TRACE)
                    || (current.getPrecip() == 0))
                    && (precipSum == ParameterFormatClimate.MISSING_PRECIP)) {
                current.setPrecipMonth(current.getPrecip());
            } else {
                current.setPrecipMonth(precipSum);
            }

        }

        // Calculate the seasonal accumulated precipitation.
        // Treat the first day of the season as a special cast that
        // requires no INFORMIX calls.
        precipSum = 0;
        if ((aDate.getDay() == precipSeason.getStart().getDay())
                && (aDate.getMon() == precipSeason.getStart().getMon())) {
            current.setPrecipSeason(current.getPrecip());

        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated precipitation
            // for days other than the first of the month.
            beginDate = new ClimateDate(precipSeason.getStart());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            precipSum = dailyClimateDao.sumPrecip(beginDate, endDate, informId);

            // Add today's precipitation to the total if it isn't
            // missing,; otherwise set the precipitation to missing
            // except where trace amounts are involved

            if ((current.getPrecip() != ParameterFormatClimate.MISSING_PRECIP)
                    && (current.getPrecip() != ParameterFormatClimate.TRACE)
                    && (current.getPrecip() != 0)) {

                if ((precipSum != ParameterFormatClimate.MISSING_PRECIP)
                        && (precipSum != ParameterFormatClimate.TRACE)) {
                    current.setPrecipSeason(precipSum + current.getPrecip());
                } else {
                    current.setPrecipSeason(current.getPrecip());
                }
            } else if (((current.getPrecip() == ParameterFormatClimate.TRACE)
                    || (current.getPrecip() == 0))
                    && (precipSum == ParameterFormatClimate.MISSING_PRECIP)) {
                current.setPrecipSeason(current.getPrecip());
            } else {
                current.setPrecipSeason(precipSum);
            }
        }

        // Calculate the annual accumulated precipitation.
        // Treat the first day of the season as a special cast that
        // requires no INFORMIX calls.
        precipSum = 0;
        if ((aDate.getDay() == precipYear.getStart().getDay())
                && (aDate.getMon() == precipYear.getStart().getMon())) {
            current.setPrecipYear(current.getPrecip());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated precipitation
            // for days other than the first of the month.
            beginDate = new ClimateDate(precipYear.getStart());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            precipSum = dailyClimateDao.sumPrecip(beginDate, endDate, informId);

            // Add today's precipitation to the total if it isn't
            // missing,; otherwise set the precipitation to missing
            // except where trace amounts are involved

            if ((current.getPrecip() != ParameterFormatClimate.MISSING_PRECIP)
                    && (current.getPrecip() != ParameterFormatClimate.TRACE)
                    && (current.getPrecip() != 0)) {

                if ((precipSum != ParameterFormatClimate.MISSING_PRECIP)
                        && (precipSum != ParameterFormatClimate.TRACE)) {
                    current.setPrecipYear(precipSum + current.getPrecip());
                } else {
                    current.setPrecipYear(current.getPrecip());
                }
            } else if (((current.getPrecip() == ParameterFormatClimate.TRACE)
                    || (current.getPrecip() == 0))
                    && (precipSum == ParameterFormatClimate.MISSING_PRECIP)) {
                current.setPrecipYear(current.getPrecip());
            } else {
                current.setPrecipYear(precipSum);
            }

        }
    }

    /**
     * Converted from update_snow.f
     * 
     * Original comments:
     * 
     * <pre>
     * August 1998     Jason P. Tuell        PRC/TDL
     * 
     * Purpose:  This routine controls the determination of accumulated
     *           snowfall.  It determines the monthly 
     *           accumulated snowfall first, followed by the
     *           seasonal and lastly, the annual snowfall.
     * 
     * 
     * Variables
     * 
     *    Input
     *      a_date         - derived TYPE that contains the date for this
     *                       climate summary
     *      current        - derived TYPE that holds the daily climate
     *                       data for this station
     *      snow_season    - derived TYPE that contains the begin and end
     *                       dates for the snow season
     *      snow_year      - derived TYPE that contains the begin and end
     *                       dates for the snow year
     *      inform_id      - INFORMIX station id
     * 
     *    Output
     *      current        - derived TYPE that holds the daily climate
     *                       data for this station
     * 
     *    Local
     *      avg_temp       - average temperature (F)
     *      iday           - Julian day
     *      missing        - flag for missing data
     *      snow_sum       - sum of the daily snowfall for the
     *                       period between start and end date
     * 
     *    Non-system routines used
     *      convert_julday        - converts an input Julian day into a date
     *      sum_snow              - C routine that sums the daily 
     *                              snowfall for this station for the
     *                              period between a start and end date
     * 
     * 
     *    Non-system functions used
     *      jul_day               - calculates the Julian day for an input
     *                              date
     * 
     * 
     * 
     * MODIFICATION LOG
     * NAME         DATE        CHANGE
     * David T. Miller      April 2000      Updated if tests to ensure 
     *                             trace totals are correctly
     *                 taken into account
     * David T. Miller      Aug 2000        Missed one part of if test
     *                                     when current day's precip
     *                                     is zero and trace was recorded
     *                                     previously.
     * 
     * </pre>
     * 
     * @param aDate
     * @param informId
     * @param snowSeason
     * @param snowYear
     * @param current
     */
    private static void updateSnow(ClimateDate aDate, int informId,
            ClimateDates snowSeason, ClimateDates snowYear,
            DailyClimateData current) {

        DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

        ClimateDate beginDate = ClimateDate.getMissingClimateDate();
        ClimateDate endDate = ClimateDate.getMissingClimateDate();
        int iday;

        float snowSum;
        // Some rules for updating the snowfall.
        // 1. The monthly total is reset at the first of each month.
        // 2. The seasonal total is at the start of each season.
        // 3. The yearly total is reset at the start of each
        // snowfall season.
        // Calculate the accumulated monthly snowfall.
        // Treat the first of the month as a special case that
        // requires INFORMIX calls.
        snowSum = 0;
        if (aDate.getDay() == 1) {
            current.setSnowMonth(current.getSnowDay());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated snowfall
            // for days other than the first of the month.
            beginDate = new ClimateDate(1, aDate.getMon(), aDate.getYear());
            endDate = new ClimateDate(aDate.getDay() - 1, aDate.getMon(),
                    aDate.getYear());

            snowSum = dailyClimateDao.sumSnow(beginDate, endDate, informId);
            // Add today's snowfall to the total if it isn't
            // missing,; otherwize set the snowfall to missing
            // except where trace amounts are involved

            if ((current.getSnowDay() != ParameterFormatClimate.MISSING_SNOW)
                    && (current.getSnowDay() != ParameterFormatClimate.TRACE)
                    && (current.getSnowDay() != 0)) {

                if ((snowSum != ParameterFormatClimate.MISSING_SNOW)
                        && (snowSum != ParameterFormatClimate.TRACE)) {
                    current.setSnowMonth(snowSum + current.getSnowDay());
                } else {
                    current.setSnowMonth(current.getSnowDay());
                }
            } else if (((current.getSnowDay() == ParameterFormatClimate.TRACE)
                    || (current.getSnowDay() == 0))
                    && (snowSum == ParameterFormatClimate.MISSING_SNOW)) {
                current.setSnowMonth(current.getSnowDay());
            } else {
                current.setSnowMonth(snowSum);
            }

        }

        // Calculate the seasonal accumulated snowfall.
        // Treat the first day of the season as a special cast that
        // requires no INFORMIX calls.
        snowSum = 0;
        if ((aDate.getDay() == snowSeason.getStart().getDay())
                && (aDate.getMon() == snowSeason.getStart().getMon())) {
            current.setSnowSeason(current.getSnowDay());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated snowfall
            // for days other than the first of the month.
            beginDate = new ClimateDate(snowSeason.getStart());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            iday = aDate.julday() - 1;

            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            snowSum = dailyClimateDao.sumSnow(beginDate, endDate, informId);

            // Add today's snowfall to the total if it isn't
            // missing,; otherwise set the snowfall to missing
            // except where trace amounts are involved
            if ((current.getSnowDay() != ParameterFormatClimate.MISSING_SNOW)
                    && (current.getSnowDay() != ParameterFormatClimate.TRACE)
                    && (current.getSnowDay() != 0)) {

                if ((snowSum != ParameterFormatClimate.MISSING_SNOW)
                        && (snowSum != ParameterFormatClimate.TRACE)) {
                    current.setSnowSeason(snowSum + current.getSnowDay());
                } else {
                    current.setSnowSeason(current.getSnowDay());
                }
            } else if (((current.getSnowDay() == ParameterFormatClimate.TRACE)
                    || (current.getSnowDay() == 0))
                    && (snowSum == ParameterFormatClimate.MISSING_SNOW)) {
                current.setSnowSeason(current.getSnowDay());
            } else {
                current.setSnowSeason(snowSum);
            }

        }
        // Calculate the annual accumulated snowfall.
        // Treat the first day of the season as a special cast that
        // requires no INFORMIX calls.

        snowSum = 0;
        if ((aDate.getDay() == snowYear.getStart().getDay())
                && (aDate.getMon() == snowYear.getStart().getMon())) {
            current.setSnowYear(current.getSnowDay());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated snowfall
            // for days other than the first of the month.
            beginDate = new ClimateDate(snowYear.getStart());
            endDate.setYear(aDate.getYear());

            // Get previous day of year
            iday = aDate.julday() - 1;
            // Set endDate to previous day of year
            endDate.convertJulday(iday);

            snowSum = dailyClimateDao.sumSnow(beginDate, endDate, informId);
            // Add today's snowfall to the total if it isn't
            // missing,; otherwise set the snowfall to missing
            // except where trace amounts are involved

            if ((current.getSnowDay() != ParameterFormatClimate.MISSING_SNOW)
                    && (current.getSnowDay() != ParameterFormatClimate.TRACE)
                    && (current.getSnowDay() != 0)) {

                if ((snowSum != ParameterFormatClimate.MISSING_SNOW)
                        && (snowSum != ParameterFormatClimate.TRACE)) {
                    current.setSnowYear(snowSum + current.getSnowDay());
                } else {
                    current.setSnowYear(current.getSnowDay());
                }
            } else if (((current.getSnowDay() == ParameterFormatClimate.TRACE)
                    || (current.getSnowDay() == 0))
                    && (snowSum == ParameterFormatClimate.MISSING_SNOW)) {
                current.setSnowYear(current.getSnowDay());
            } else {
                current.setSnowYear(snowSum);
            }

        }
    }

    /**
     * Converted from build_derived_data.f. Also a duplicate of
     * build_derived_fields.f, which only handled Daily period types but used
     * exactly the same functionality.
     * 
     * Original comments:
     * 
     * <pre>
     * June 1999     Jason P. Tuell        PRC/TDL
     * 
     * 
     * Purpose:  This subroutine controls the calculation of the derived
     *        fields.  These fields include heating and cooling degree
     *        days, cumulative totals of snow, precipitation, heating 
     *        cooling degree days, etc.  Derived fields are listed 
     *        below:
     * 
     *        precip
     *          precip_month      monthly accumulated precip
     *          precip_season     seasonal accumulated precip
     *          precip_year       yearly accumulated precip
     * 
     *        snow
     *          snow_month        monthly accumulated snow
     *          snow_season       seassonal accumulated snow
     *          snow_year         yearly accumulated snow
     * 
     *        num_heat            heating degree days yesterday
     *        num_heat_month      monthly accumulated heating degree days
     *        num_heat_season     seasonal accumulated heating degree days
     *        num_heat_year       yearly accumulated heating degree days
     * 
     *        num_cool            cooling degree days yesterday
     *        num_cool_month      monthly accumulated cooling degree days
     *        num_cool_season     seasonal accumulated cooling degree days
     *        num_cool_year       yearly accumulated cooling degree days
     * 
     * 
     * Variables
     * 
     * Input
     *   a_date          - derived TYPE that contains the date for this
     *                     climate summary
     *   inform_id       - Informix ID of the station
     *   cdata           - derived TYPE that contains the observed climate
     *                     data
     * 
     * Output
     * 
     * 
     * Local
     *   cool_season      - derived TYPE that defines the date for the
     *                      start of the cooling season
     *   cool_year        - derived TYPE that defines the date for the
     *                      start of the cooling year
     *   heat_season      - derived TYPE that defines the date for the
     *                      start of the heating season
     *   heat_year        - derived TYPE that defines the date for the
     *                      start of the heating year
     *   precip_season    - derived TYPE that defines the date for the
     *                      start of the precipitation season
     *   precip_year      - derived TYPE that defines the date for the
     *                      start of the precipitation year
     *   snow_season      - derived TYPE that defines the date for the
     *                      start of the snow season
     *   snow_season      - derived TYPE that defines the date for the
     *                      start of the snow year
     *   day_before_date - derived TYPE that contains the date for the day
     *                     before a_date
     *   iday            - Julian day for a_date
     * 
     * Non-system routines used
     *   get_day_before  - get the observed climate data for the day before
     *   set_season      - set the dates that define the start of the season and 
     *                     year for various parameters
     *   update_cool     - update the cooling degree day climate data
     *   update_heat     - update the heating degree day climate data
     *   update_precip   - update the precipitation climate data
     *   update_snow     - update the snowfall climate data
     * 
     * Non-system functions used
     *   julday          - returns the Julian date for an input date
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param cData
     * @throws ClimateQueryException
     */
    public static void buildDerivedData(ClimateDate aDate, int stationId,
            DailyClimateData cData) throws ClimateQueryException {

        ClimateDates coolSeason = ClimateDates.getMissingClimateDates();
        ClimateDates coolYear = ClimateDates.getMissingClimateDates();
        ClimateDates heatSeason = ClimateDates.getMissingClimateDates();
        ClimateDates heatYear = ClimateDates.getMissingClimateDates();
        ClimateDates precipSeason = ClimateDates.getMissingClimateDates();
        ClimateDates precipYear = ClimateDates.getMissingClimateDates();
        ClimateDates snowSeason = ClimateDates.getMissingClimateDates();
        ClimateDates snowYear = ClimateDates.getMissingClimateDates();

        setSeason(aDate, coolSeason, coolYear, heatSeason, heatYear,
                precipSeason, precipYear, snowSeason, snowYear);

        int maxRh = cData.getMaxRelHumid();
        int minRh = cData.getMinRelHumid();

        if (maxRh != ParameterFormatClimate.MISSING
                && minRh != ParameterFormatClimate.MISSING) {
            cData.setMeanRelHumid(ClimateUtilities.nint((maxRh + minRh) / 2.0));
        } else {
            cData.setMeanRelHumid(ParameterFormatClimate.MISSING);
        }

        updateHeat(aDate, stationId, heatSeason, heatYear, cData);

        updateCool(aDate, stationId, coolSeason, coolYear, cData);

        updatePrecip(aDate, stationId, precipSeason, precipYear, cData);

        updateSnow(aDate, stationId, snowSeason, snowYear, cData);

    }

    /**
     * Converted from set_season.f
     * 
     * <pre>
     * Original comments:
     * 
     * June 1998 Jason P. Tuell PRC/TDL February 1999 Jason P. Tuell PRC/TDL
     * 
     * 
     * Purpose: This routine defines the dates for the start period for seasonal
     * and annual accumulation periods. It is used for heating degree days,
     * cooling degree days, precipitation and snowfall. It allows for the
     * possibility of non-standard seasons and annual periods.
     * 
     * 
     * Variables
     * 
     * Input a_date - derived TYPE that contains the date for this climate
     * summary
     * 
     * Output cool_season - derived TYPE that defines the date for the start of
     * the cooling season cool_year - derived TYPE that defines the date for the
     * start of the cooling year heat_season - derived TYPE that defines the
     * date for the start of the heating season heat_year - derived TYPE that
     * defines the date for the start of the heating year precip_season -
     * derived TYPE that defines the date for the start of the precipitation
     * season precip_year - derived TYPE that defines the date for the start of
     * the precipitation year snow_season - derived TYPE that defines the date
     * for the start of the snow season snow_season - derived TYPE that defines
     * the date for the start of the snow year
     * 
     * Local mon - month of the start of a season no_dates - flag that indicates
     * season and year dates weren't available from the data base = 0 dates were
     * available = 1 dates were not available
     * 
     * Non-system routines used get_season - retrieves seasonal and annual dates
     * for snow and precipitation from the data base (i.e., table climo_dates)
     * set_month - sets the starting month of the current season set_year - sets
     * the year for the starting date
     * 
     * Non-system functions used
     * 
     * <pre>
     * 
     * @param aDate
     * @param coolSeason
     * @param coolYear
     * @param heatSeason
     * @param heatYear
     * @param precipSeason
     * @param precipYear
     * @param snowSeason
     * @param snowYear
     * @throws ClimateQueryException
     */
    public static void setSeason(ClimateDate aDate, ClimateDates coolSeason,
            ClimateDates coolYear, ClimateDates heatSeason,
            ClimateDates heatYear, ClimateDates precipSeason,
            ClimateDates precipYear, ClimateDates snowSeason,
            ClimateDates snowYear) throws ClimateQueryException {
        ClimoDatesDAO climoDatesDAO = new ClimoDatesDAO();

        ClimateDate firstDay = new ClimateDate(1, 1, aDate.getYear());
        ClimateDate lastDay = new ClimateDate(31, 12, aDate.getYear());

        int iday;
        int monStart, noDates;

        /*
         * Retrieve the seasonal and yearly dates for snow and precipitation
         * from the data base. If they aren't in the data base, then set them
         * here.
         */
        noDates = climoDatesDAO.getSeason(precipSeason, precipYear, snowSeason,
                snowYear);

        /*
         * Define the start of the heating year. According to the TSPs, the heat
         * year is defined to start at 1 July.
         */
        heatYear.setStart(new ClimateDate(1, 7, aDate.getYear()));
        heatYear.setEnd(new ClimateDate(30, 6, aDate.getYear()));
        setYear(aDate, heatYear.getStart());

        /*
         * Define the start of the cooling year. According to the TSPs, the heat
         * year is defined to start at 1 January.
         */
        coolYear.setStart(firstDay);
        coolYear.setEnd(lastDay);
        setYear(aDate, coolYear.getStart());

        /*
         * Only set the annual dates for snow and precipitation if they aren't
         * available in the data base.
         * 
         * Define the start of the snow year. According to the TSPs, the snow
         * year is defined to start at 1 July.
         */
        snowYear.setStart(new ClimateDate(1, 7, aDate.getYear()));
        snowYear.setEnd(new ClimateDate(30, 6, aDate.getYear()));
        setYear(aDate, snowYear.getStart());

        /*
         * Define the start of the precipitation year. According to the TSPs,
         * the precipitation year is defined to start at 1 January.
         */
        precipYear.setStart(firstDay);
        precipYear.setEnd(lastDay);
        setYear(aDate, precipYear.getStart());

        /*
         * Determine the month of the starting season for the current date
         */
        monStart = getStartMonthOfSeasonFromMonth(aDate.getMon());

        /*
         * the heating and cooling degree dates first since they dates fixed in
         * the TSPs
         */
        coolSeason.setStart(new ClimateDate(1, monStart, aDate.getYear()));
        heatSeason.setStart(new ClimateDate(1, monStart, aDate.getYear()));

        coolSeason.setEnd(new ClimateDate(aDate));
        heatSeason.setEnd(new ClimateDate(aDate));

        setYear(aDate, coolSeason.getStart());
        setYear(aDate, heatSeason.getStart());

        // Now do the seasonal dates for snow and precip

        /*
         * Don't forget the case where the alternate year is the the same as the
         * default year. In this case, we revert to the standard definition of
         * the season.
         */

        // Precip first
        if ((noDates == 0) && (precipSeason.getStart().getDay() > 0)
                && (precipSeason.getStart().getDay() < 32)
                && (precipSeason.getStart().getMon() > 0)
                && (precipSeason.getStart().getMon() < 13)) {
            iday = precipSeason.getStart().julday();

            precipSeason.setEnd(new ClimateDate(aDate));

            if (iday == 1) {
                precipSeason.getStart().setDay(1);
                precipSeason.getStart().setMon(monStart);

                setYear(aDate, precipSeason.getStart());
            } else {
                setYear(aDate, precipSeason.getStart());
            }

        } else {

            precipSeason.getStart().setDay(1);
            precipSeason.getStart().setMon(monStart);

            precipSeason.setEnd(new ClimateDate(aDate));

            setYear(aDate, precipSeason.getStart());
        }

        // Snow
        if ((noDates == 0) && (snowSeason.getStart().getDay() > 0)
                && (snowSeason.getStart().getDay() < 32)
                && (snowSeason.getStart().getMon() > 0)
                && (snowSeason.getStart().getMon() < 13)) {
            snowSeason.setEnd(new ClimateDate(aDate));

            if ((snowSeason.getStart().getDay() == 1)
                    && (snowSeason.getStart().getMon() == 7)) {

                snowSeason.getStart().setDay(1);
                snowSeason.getStart().setMon(monStart);

                setYear(aDate, snowSeason.getStart());
            } else {
                setYear(aDate, snowSeason.getStart());
            }

        } else {
            snowSeason.getStart().setDay(1);
            snowSeason.getStart().setMon(monStart);

            snowSeason.setEnd(new ClimateDate(aDate));

            setYear(aDate, snowSeason.getStart());
        }

    }

    /**
     * Converted from set_month.f
     * 
     * <pre>
     * Original comments:
     * 
     * February 1999 Jason P. Tuell PRC/TDL Purpose: This routine defines the
     * starting month of a season given the current month.
     * 
     * 
     * Variables
     * 
     * Input imon - current month
     * 
     * Output set_month - starting month of the season for imon
     * 
     * Local mon_start -starting month of the season for imon
     * 
     * Non-system routines used
     * 
     * Non-system functions used
     * </pre>
     * 
     * @param iMonth
     * @return
     */
    private static int getStartMonthOfSeasonFromMonth(int iMonth) {
        int monStart = 0;
        switch (iMonth) {
        // WINTER
        case 12:
        case 1:
        case 2:
            monStart = 12;
            break;
        // SPRING
        case 3:
        case 4:
        case 5:
            monStart = 3;
            break;
        // SUMMER
        case 6:
        case 7:
        case 8:
            monStart = 6;
            break;
        // FALL
        case 9:
        case 10:
        case 11:
            monStart = 9;
            break;
        default:
            logger.error("Bad Climate month: [" + iMonth + "].");
            break;
        }
        return monStart;
    }

    /**
     * <pre>
     * Converted from set_year.f
     * 
     * Original comments:
     * 
     * February 1999 Jason P. Tuell PRC/TDL Purpose: This routine defines the
     * year for the starting period.
     * 
     * 
     * Variables
     * 
     * Input a_date - derived TYPE that contains the date for this climate
     * summary
     * 
     * Output
     * 
     * Local
     * 
     * Non-system routines used
     * 
     * Non-system functions used
     * </pre>
     * 
     * Comment from amoore: Sets the year of start to the year of aDate if
     * aDate's month is equal to or after the month of start. Otherwise, set the
     * year of start to the year prior to that of aDate.
     * 
     * @param aDate
     * @param start
     */
    private static void setYear(ClimateDate aDate, ClimateDate start) {
        int monDelta = aDate.getMon() - start.getMon();

        if (monDelta >= 0) {
            start.setYear(aDate.getYear());
        } else {
            start.setYear(aDate.getYear() - 1);
        }
    }

}
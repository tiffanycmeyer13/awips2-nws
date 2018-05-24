/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.util;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateSeason;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.localization.climate.climodates.ClimoDates;
import gov.noaa.nws.ocp.common.localization.climate.climodates.ClimoDatesManager;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodNormDAO;
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
 * 01 MAR 2018  44624      amoore      Clean up #setSeason with modern logic.
 * 02 MAY 2018  DR17116    wpaintsil   Accommodate multiple alternate precip/snow seasons.
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
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param coolSeason
     * @param coolYear
     * @param current
     */
    private static void updateCool(ClimateDate aDate, int stationId,
            ClimateDate coolSeason, ClimateDate coolYear,
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

        if ((aDate.getDay() == coolSeason.getDay())
                && (aDate.getMon() == coolSeason.getMon())) {

            current.setNumCoolSeason(current.getNumCool());

        } else {

            /*
             * Set up the beginning and ending dates for the retrieval of the
             * monthly accumulated cooling degree days for days other than the
             * first of the month.
             */

            beginDate.setDay(coolSeason.getDay());
            beginDate.setMon(coolSeason.getMon());
            beginDate.setYear(coolSeason.getYear());
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

        if ((aDate.getDay() == coolYear.getDay())
                && (aDate.getMon() == coolYear.getMon())) {

            current.setNumCoolYear(current.getNumCool());

        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated cooling degree days
            // for days other than the first of the month.

            beginDate.setDay(coolYear.getDay());
            beginDate.setMon(coolYear.getMon());
            beginDate.setYear(coolYear.getYear());
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
            ClimateDate heatSeason, ClimateDate heatYear,
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

        if ((aDate.getDay() == heatSeason.getDay())
                && (aDate.getMon() == heatSeason.getMon())) {

            current.setNumHeatSeason(current.getNumHeat());

        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated heating degree days
            // for days other than the first of the month.

            beginDate.setDay(heatSeason.getDay());
            beginDate.setMon(heatSeason.getMon());
            beginDate.setYear(heatSeason.getYear());
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

        if ((aDate.getDay() == heatYear.getDay())
                && (aDate.getMon() == heatYear.getMon())) {

            current.setNumHeatYear(current.getNumHeat());

        } else {

            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated heating degree days
            // for days other than the first of the month.

            beginDate.setDay(heatYear.getDay());
            beginDate.setMon(heatYear.getMon());
            beginDate.setYear(heatYear.getYear());
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
     * </pre>
     * 
     * @param aDate
     * @param informId
     * @param precipSeason
     * @param precipYear
     * @param current
     */
    private static void updatePrecip(ClimateDate aDate, int informId,
            List<ClimateDate> precipSeasons, ClimateDate precipYear,
            DailyClimateData current) {

        DailyClimateDAO dailyClimateDao = new DailyClimateDAO();

        ClimateDate beginDate = ClimateDate.getMissingClimateDate();
        ClimateDate endDate = ClimateDate.getMissingClimateDate();
        int iday;

        float precipSum = 0;
        /*
         * These calculations are already done in Creator, however the user may
         * have updated the daily precip in the Display phase which would adjust
         * all other values.
         * 
         * Some rules for updating the precipitation.
         * 
         * 1. The monthly total is reset at the first of each month.
         * 
         * 2. The seasonal total is at the start of each season.
         * 
         * 3. The yearly total is reset at the start of each precipitation
         * season.
         * 
         * Calculate the accumulated monthly precipitation.
         */
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
        List<Float> precipValues = new ArrayList<>();
        for (ClimateDate precipSeason : precipSeasons) {
            precipSum = 0;
            if ((aDate.getDay() == precipSeason.getDay())
                    && (aDate.getMon() == precipSeason.getMon())) {
                precipValues.add(current.getPrecip());

            } else {
                // Set up the beginning and ending dates for the
                // retrieval of the monthly accumulated precipitation
                // for days other than the first of the month.
                beginDate = new ClimateDate(precipSeason);
                endDate.setYear(aDate.getYear());

                // Get previous day of year
                iday = aDate.julday() - 1;

                // Set endDate to previous day of year
                endDate.convertJulday(iday);

                precipSum = dailyClimateDao.sumPrecip(beginDate, endDate,
                        informId);

                // Add today's precipitation to the total if it isn't
                // missing,; otherwise set the precipitation to missing
                // except where trace amounts are involved

                if ((current
                        .getPrecip() != ParameterFormatClimate.MISSING_PRECIP)
                        && (current.getPrecip() != ParameterFormatClimate.TRACE)
                        && (current.getPrecip() != 0)) {

                    if ((precipSum != ParameterFormatClimate.MISSING_PRECIP)
                            && (precipSum != ParameterFormatClimate.TRACE)) {
                        precipValues.add(precipSum + current.getPrecip());
                    } else {
                        precipValues.add(current.getPrecip());
                    }
                } else if (((current
                        .getPrecip() == ParameterFormatClimate.TRACE)
                        || (current.getPrecip() == 0))
                        && (precipSum == ParameterFormatClimate.MISSING_PRECIP)) {
                    precipValues.add(current.getPrecip());
                } else {
                    precipValues.add(precipSum);
                }
            }
        }
        current.setPrecipSeasons(precipValues);

        // Calculate the annual accumulated precipitation.
        // Treat the first day of the season as a special cast that
        // requires no INFORMIX calls.
        precipSum = 0;
        if ((aDate.getDay() == precipYear.getDay())
                && (aDate.getMon() == precipYear.getMon())) {
            current.setPrecipYear(current.getPrecip());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated precipitation
            // for days other than the first of the month.
            beginDate = new ClimateDate(precipYear);
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
            List<ClimateDate> snowSeasons, ClimateDate snowYear,
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
        List<Float> snowValues = new ArrayList<>();
        for (ClimateDate snowSeason : snowSeasons) {
            snowSum = 0;
            if ((aDate.getDay() == snowSeason.getDay())
                    && (aDate.getMon() == snowSeason.getMon())) {
                snowValues.add(current.getSnowDay());
            } else {
                // Set up the beginning and ending dates for the
                // retrieval of the monthly accumulated snowfall
                // for days other than the first of the month.
                beginDate = new ClimateDate(snowSeason);
                endDate.setYear(aDate.getYear());

                // Get previous day of year
                iday = aDate.julday() - 1;

                // Set endDate to previous day of year
                endDate.convertJulday(iday);

                snowSum = dailyClimateDao.sumSnow(beginDate, endDate, informId);

                // Add today's snowfall to the total if it isn't
                // missing,; otherwise set the snowfall to missing
                // except where trace amounts are involved
                if ((current
                        .getSnowDay() != ParameterFormatClimate.MISSING_SNOW)
                        && (current
                                .getSnowDay() != ParameterFormatClimate.TRACE)
                        && (current.getSnowDay() != 0)) {

                    if ((snowSum != ParameterFormatClimate.MISSING_SNOW)
                            && (snowSum != ParameterFormatClimate.TRACE)) {
                        snowValues.add(snowSum + current.getSnowDay());
                    } else {
                        snowValues.add(current.getSnowDay());
                    }
                } else if (((current
                        .getSnowDay() == ParameterFormatClimate.TRACE)
                        || (current.getSnowDay() == 0))
                        && (snowSum == ParameterFormatClimate.MISSING_SNOW)) {
                    snowValues.add(current.getSnowDay());
                } else {
                    snowValues.add(snowSum);
                }

            }

        }
        current.setSnowSeasons(snowValues);

        // Calculate the annual accumulated snowfall.
        // Treat the first day of the season as a special cast that
        // requires no INFORMIX calls.

        snowSum = 0;
        if ((aDate.getDay() == snowYear.getDay())
                && (aDate.getMon() == snowYear.getMon())) {
            current.setSnowYear(current.getSnowDay());
        } else {
            // Set up the beginning and ending dates for the
            // retrieval of the monthly accumulated snowfall
            // for days other than the first of the month.
            beginDate = new ClimateDate(snowYear);
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
     * Migrated from update_his_precip.f
     * 
     * <pre>
     *   January 1999     Jason P. Tuell        PRC/TDL
     *
     *
     *   Purpose:  This routine controls the determination of accumulated
     *             historical precipitation.  It determines the monthly 
     *             accumulated historical precipitation first, followed by the
     *             seasonal and lastly the annual historical precipitation.
     * 
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param precipSeason
     * @param precipYear
     * @param yClimate
     */
    public static void updateHisPrecip(ClimateDate aDate, int stationId,
            List<ClimateDate> precipSeasons, ClimateDate precipYear,
            ClimateRecordDay yClimate) {
        ClimatePeriodNormDAO climatePeriodNormDAO = new ClimatePeriodNormDAO();

        // First do the monthly total. We need to consider leap years
        // for summing in the month of February
        ClimateDate first = new ClimateDate(aDate);
        ClimateDate last = new ClimateDate(aDate);

        first.setDay(1);

        yClimate.setPrecipMonthMean(
                climatePeriodNormDAO.sumHisPrecip(first, last, stationId));

        // Now handle the seasonal summations. This is a little
        // more involved since we need to take into account seasons
        // that cross yearly boundaries.
        List<Float> precipSeasonValues = new ArrayList<>();
        for (ClimateDate precipSeason : precipSeasons) {
            first = precipSeason;
            precipSeasonValues.add(
                    climatePeriodNormDAO.sumHisPrecip(first, last, stationId));
        }
        yClimate.setPrecipSeasonMean(precipSeasonValues);

        // Now do the annual precipitation
        // We have the same potential problems as with the seasonal
        // precip

        first = precipYear;
        yClimate.setPrecipYearMean(
                climatePeriodNormDAO.sumHisPrecip(first, last, stationId));
    }

    /**
     * Migrated from update_his_snow.f
     * 
     * <pre>
     *   January 1999     Jason P. Tuell        PRC/TDL
     *
     *
     *   Purpose:  This routine controls the determination of accumulated
     *             historical snowfall.  It determines the monthly 
     *             accumulated historical snowfall first, followed by the
     *             seasonal and lastly the annual historical snowfall.
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param snowSeason
     * @param snowYear
     * @param yClimate
     */
    public static void updateHisSnow(ClimateDate aDate, int stationId,
            List<ClimateDate> snowSeasons, ClimateDate snowYear,
            ClimateRecordDay yClimate) {
        ClimatePeriodNormDAO climatePeriodNormDAO = new ClimatePeriodNormDAO();

        ClimateDate first = new ClimateDate(aDate);
        ClimateDate last = new ClimateDate(aDate);

        // First do the monthly total. We need to consider leap years
        // for summing in the month of February

        first.setDay(1);

        yClimate.setSnowMonthMean(
                climatePeriodNormDAO.sumHisSnow(first, last, stationId));

        // Now handle the seasonal summations. This is a little
        // more involved since we need to take into account seasons
        // that cross yearly boundaries.
        List<Float> snowSeasonValues = new ArrayList<>();
        for (ClimateDate snowSeason : snowSeasons) {
            first = snowSeason;
            snowSeasonValues.add(
                    climatePeriodNormDAO.sumHisSnow(first, last, stationId));
        }
        yClimate.setSnowSeasonMean(snowSeasonValues);

        // Now do the annual snow
        // We have the same potential problems as with the seasonal
        // snow

        first = snowYear;
        yClimate.setSnowYearMean(
                climatePeriodNormDAO.sumHisSnow(first, last, stationId));
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
     * </pre>
     * 
     * @param aDate
     * @param stationId
     * @param cData
     * @param yClimate
     * @throws ClimateQueryException
     */
    public static void buildDerivedData(ClimateDate aDate, int stationId,
            DailyClimateData cData, ClimateRecordDay yClimate)
            throws ClimateQueryException {

        ClimateSeason season = getSeason(aDate);

        int maxRh = cData.getMaxRelHumid();
        int minRh = cData.getMinRelHumid();

        if (maxRh != ParameterFormatClimate.MISSING
                && minRh != ParameterFormatClimate.MISSING) {
            cData.setMeanRelHumid(ClimateUtilities.nint((maxRh + minRh) / 2.0));
        } else {
            cData.setMeanRelHumid(ParameterFormatClimate.MISSING);
        }

        updateHeat(aDate, stationId, season.getHeatSeason(),
                season.getHeatYear(), cData);

        updateCool(aDate, stationId, season.getCoolSeason(),
                season.getCoolYear(), cData);

        updatePrecip(aDate, stationId, season.getPrecipSeasons(),
                season.getPrecipYear(), cData);

        updateSnow(aDate, stationId, season.getSnowSeasons(),
                season.getSnowYear(), cData);

        // Update snow/precip norms
        if (yClimate != null) {
            updateHisPrecip(aDate, stationId, season.getPrecipSeasons(),
                    season.getPrecipYear(), yClimate);
            updateHisSnow(aDate, stationId, season.getSnowSeasons(),
                    season.getSnowYear(), yClimate);
        }

    }

    /**
     * Overload method for when yClimate is not used.
     * 
     * @param aDate
     * @param stationId
     * @param cData
     * @throws ClimateQueryException
     */
    public static void buildDerivedData(ClimateDate aDate, int stationId,
            DailyClimateData cData) throws ClimateQueryException {
        buildDerivedData(aDate, stationId, cData, null);
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
     * <pre>
     * 
     * @param aDate
     * @throws ClimateQueryException
     */
    public static ClimateSeason getSeason(ClimateDate aDate)
            throws ClimateQueryException {
        ClimateSeason season = new ClimateSeason();

        ClimateDate firstDay = new ClimateDate(1, 1, aDate.getYear());

        int iday;
        int monStart;

        /*
         * Legacy comment: Retrieve the seasonal and yearly dates for snow and
         * precipitation from the data base. If they aren't in the data base,
         * then set them here.
         */
        // Retrieve the climo_dates information from localization instead of the
        // database table.
        ClimoDates localClimoDates = ClimoDatesManager.getInstance()
                .getClimoDates();

        List<ClimateDate> precipSeasons = localClimoDates.getPrecipSeasons();
        ClimateDate precipYear = localClimoDates.getPrecipYear();
        List<ClimateDate> snowSeasons = localClimoDates.getSnowSeasons();
        ClimateDate snowYear = localClimoDates.getSnowYear();

        ClimateDate heatYear = ClimateDate.getMissingClimateDate();
        ClimateDate coolYear = ClimateDate.getMissingClimateDate();
        ClimateDate heatSeason = ClimateDate.getMissingClimateDate();
        ClimateDate coolSeason = ClimateDate.getMissingClimateDate();

        /*
         * Legacy comment: Define the start of the heating year. According to
         * the TSPs, the heat year is defined to start at 1 July.
         */
        heatYear.setDateFromDate(new ClimateDate(1, 7, aDate.getYear()));
        setYear(aDate, heatYear);

        /*
         * Legacy comment: Define the start of the cooling year. According to
         * the TSPs, the heat year is defined to start at 1 January.
         */
        coolYear.setDateFromDate(firstDay);
        setYear(aDate, coolYear);

        /*
         * Legacy comment: Only set the annual dates for snow and precipitation
         * if they aren't available in the data base.
         * 
         * Define the start of the snow year. According to the TSPs, the snow
         * year is defined to start at 1 July.
         */
        snowYear.setDateFromDate(new ClimateDate(1, 7, aDate.getYear()));
        setYear(aDate, snowYear);

        /*
         * Legacy comment: Define the start of the precipitation year. According
         * to the TSPs, the precipitation year is defined to start at 1 January.
         */
        precipYear.setDateFromDate(firstDay);
        setYear(aDate, precipYear);

        /*
         * Determine the month of the starting season for the current date
         */
        monStart = getStartMonthOfSeasonFromMonth(aDate.getMon());

        /*
         * the heating and cooling degree dates first since they dates fixed in
         * the TSPs
         */
        coolSeason
                .setDateFromDate(new ClimateDate(1, monStart, aDate.getYear()));
        heatSeason
                .setDateFromDate(new ClimateDate(1, monStart, aDate.getYear()));

        setYear(aDate, coolSeason);
        setYear(aDate, heatSeason);

        // Now do the seasonal dates for snow and precip

        /*
         * Legacy comment: Don't forget the case where the alternate year is the
         * the same as the default year. In this case, we revert to the standard
         * definition of the season.
         */

        // Precip first
        for (ClimateDate precipSeason : precipSeasons) {
            if ((precipSeason.getDay() > 0) && (precipSeason.getDay() < 32)
                    && (precipSeason.getMon() > 0)
                    && (precipSeason.getMon() < 13)) {
                iday = precipSeason.julday();

                if (iday == 1) {
                    precipSeason.setDay(1);
                    precipSeason.setMon(monStart);

                    setYear(aDate, precipSeason);
                } else {
                    setYear(aDate, precipSeason);
                }

            } else {

                precipSeason.setDay(1);
                precipSeason.setMon(monStart);

                setYear(aDate, precipSeason);
            }
        }
        // Snow

        for (ClimateDate snowSeason : snowSeasons) {
            if ((snowSeason.getDay() > 0) && (snowSeason.getDay() < 32)
                    && (snowSeason.getMon() > 0)
                    && (snowSeason.getMon() < 13)) {

                if ((snowSeason.getDay() == 1) && (snowSeason.getMon() == 7)) {

                    snowSeason.setDay(1);
                    snowSeason.setMon(monStart);

                    setYear(aDate, snowSeason);
                } else {
                    setYear(aDate, snowSeason);
                }

            } else {
                snowSeason.setDay(1);
                snowSeason.setMon(monStart);

                setYear(aDate, snowSeason);
            }
        }

        season.setCoolSeason(coolSeason);
        season.setCoolYear(coolYear);
        season.setHeatSeason(heatSeason);
        season.setHeatYear(heatYear);
        season.setPrecipSeasons(precipSeasons);
        season.setPrecipYear(precipYear);
        season.setSnowSeasons(snowSeasons);
        season.setSnowYear(snowYear);

        return season;
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
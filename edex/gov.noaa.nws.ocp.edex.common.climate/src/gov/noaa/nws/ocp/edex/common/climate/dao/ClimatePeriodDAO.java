/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodDataMethod;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.QueryData;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateSessionException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.QCValues;

/**
 * Implementations converted from SUBROUTINES under
 * adapt/climate/lib/src/climate_db_utils
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 18 JUL 2016  20414      amoore      Initial creation
 * 04 AUG 2016  20414      amoore      Added period update/insert method.
 * 22 SEP 2016  21378      amoore      Migrated Period Sum method.
 * 04 OCT 2016  20636      wpaintsil   Implement getPeriodData.
 * 14 OCT 2016  21378      amoore      Implementing more Climate Creator functions.
 * 21 OCT 2016  22135      wpaintsil   Modify getPeriodData for data methods
 * 25 OCT 2016  21378      amoore      Cleaning up Climate Creator functions.
 * 14 OCT 2016  20635      wkwock      Add updateClimatePeriod
 * 17 NOV 2016  21378      amoore      Query bug fixes. Don't use query maps.
 * 21 NOV 2016  21378      amoore      Fixing queries.
 * 08 DEC 2016  20414      amoore      Some handling if Globals are null.
 * 09 DEC 2016  27015      amoore      Query method cleanup and doc.
 * 15 DEC 2016  27015      amoore      Display module non-GUI processing. Query clean up.
 * 19 DEC 2016  27015      amoore      Checking against missing date information should
 *                                     require all date data present if logic would need it.
 * 05 JAN 2017  22134      amoore      Combine monthly ASOS queries. Monthly ASOS returns should
 *                                     specify QC value.
 * 16 MAR 2017  30162      amoore      Fix logging and comments.
 * 21 MAR 2017  20632      amoore      Handle null DB values.
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 05 APR 2017  30166      amoore      Change data type input for headless Display execution.
 * 18 APR 2017  33104      amoore      Adjust record/normal year fetch to default to returning
 *                                     missing values. Use query maps now that DB issue is fixed.
 * 24 APR 2017  33104      amoore      Use query maps for update/insert/delete.
 * 03 MAY 2017  33104      amoore      Use abstract map. More query map replacements.
 * 04 MAY 2017  33104      amoore      Query bug fixes.
 * 11 MAY 2017  33104      amoore      Fix query parameters.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 15 JUN 2017  35186      amoore      Fix accidental mishandling of missing values for temp
 *                                     extreme dates.
 * 16 JUN 2017  35182      amoore      Fix wind/gust logic.
 * 20 JUN 2017  33104      amoore      Address review comments.
 * 02 AUG 2017  33641      amoore      If snow total is trace, snow water is also trace.
 * 31 AUG 2017  37561      amoore      Use calendar/date parameters where possible.
 * 06 SEP 2017  37721      amoore      Failure on Display finalization should fail CPG session
 * 07 SEP 2017  37754      amoore      Additional parameterization.
 * 08 SEP 2017  37809      amoore      For queries, cast to Number rather than specific number type.
 * 23 OCT 2017  39816      amoore      Fix bad, unnecessary, and missing checking of period types for
 *                                     #sumNum aggregate functionality.
 * 26 OCT 2016  39809      wpaintsil   Add num_cool_1jan and num_heat_1july to period data query.
 * 15 NOV 2017  40988      amoore      Fix error introduced by 40624 changes. Explicitly order max/min
 *                                     queries.
 * 21 NOV 2017  41180      amoore      CLS and CLA should not deal with MSM values.
 * 07 NOV 2018  DR20923    wpaintsil   Queries for dates may return a single date object 
 *                                     rather than an array.
 *                                     Revise monthly period logic in buildPeriodObsClimo.
 * 30 APR 2019  DR21261    wpaintsil   Several fields missing due to incorrect queries.
 * 13 JUN 2019  DR21099    wpaintsil   Snow values should default to missing value if the station
 *                                     does not report snow.
 * 15 JUL 2019  DR21432    wpaintsil   Psql round function returns no results.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimatePeriodDAO extends ClimateDAO {
    /**
     * Constructor.
     */
    public ClimatePeriodDAO() {
        super();
    }

    /**
     * Migrated from build_period_obs_climo.ecpp.
     * 
     * <pre>
     * build_period_obs_climo( )
     * 
     * December 1999     David T. Miller        PRC/TDL
     *
     *
     * Purpose:  Retrieves stored climatology values from either the 
     *        daily or monthly tables and builds climatology for the 
     *    next time period level.  The monthly climatology is built
     *    using the daily_climate table. The season and annual values
     *    are built using the monthly values.  Could also build 
     *    the season and annual from the daily, but the ASOS Monthly
     *    Summary Message climatology values wouldn't be used.
     * 
     * </pre>
     * 
     * Does not have early/late freeze dates logic, since this functionality is
     * present in another DAO. See
     * {@link DailyClimateDAO#getEarlyFreezeDate(ClimateDate, ClimateDate, int)}
     * and
     * {@link DailyClimateDAO#getLateFreezeDate(ClimateDate, ClimateDate, int)}.
     * 
     * @param beginDate
     * @param endDate
     * @param periodData
     *            data to fill out, which has at least inform ID (station ID)
     *            filled in.
     * @param globalValues
     * @param itype
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    public PeriodData buildPeriodObsClimo(ClimateDate beginDate,
            ClimateDate endDate, PeriodData periodData,
            ClimateGlobal globalValues, PeriodType itype, boolean monthly)
            throws ClimateQueryException, ClimateInvalidParameterException {
        // interval of days (add 1 to account for first day)
        int numDays = (int) TimeUnit.DAYS
                .convert(
                        endDate.getCalendarFromClimateDate().getTimeInMillis()
                                - beginDate.getCalendarFromClimateDate()
                                        .getTimeInMillis(),
                        TimeUnit.MILLISECONDS)
                + 1;

        int stationID = periodData.getInformId();

        // PeriodType.OTHER is passed to the helper methods to indicate that the
        // daily_climate table should be queried for CLM.
        PeriodType currentType = monthly ? PeriodType.OTHER : itype;

        String icaoId = new String();
        for (Station station : new ClimateStationsSetupDAO()
                .getMasterStations()) {
            if (station.getInformId() == stationID) {
                icaoId = station.getIcaoId();
            }
        }

        // temperature section

        // get max temp
        periodData.setMaxTemp(
                getMaxMaxTemp(beginDate, endDate, stationID, currentType));

        /* dates with max temp */
        if (periodData.getMaxTemp() != ParameterFormatClimate.MISSING) {
            periodData.setDayMaxTempList(getMaxTempOccurrences(beginDate,
                    endDate, stationID, periodData.getMaxTemp(), currentType));
        }

        /* average max temp */
        periodData.setMaxTempMean(
                avgMaxTempMean(beginDate, endDate, currentType, stationID));

        /* number of days for max temp thresholds */
        if (monthly) {
            periodData.setNumMaxGreaterThan90F(sumReportMaxTempGreater90F(
                    beginDate, endDate, PeriodType.OTHER, stationID));

            periodData.setNumMaxLessThan32F(sumReportMaxTempLess32F(beginDate,
                    endDate, PeriodType.OTHER, stationID));

            periodData.setNumMaxGreaterThanT1F(
                    sumReportMaxTempGreaterT1F(beginDate, endDate,
                            PeriodType.OTHER, stationID, globalValues));

            periodData.setNumMaxGreaterThanT2F(
                    sumReportMaxTempGreaterT2F(beginDate, endDate,
                            PeriodType.OTHER, stationID, globalValues));

            periodData.setNumMaxLessThanT3F(sumReportMaxTempLessT3F(beginDate,
                    endDate, PeriodType.OTHER, stationID, globalValues));
        }
        // get min temp
        periodData.setMinTemp(
                getMinMinTemp(beginDate, endDate, stationID, currentType));

        /* dates with min temp */
        if (periodData.getMinTemp() != ParameterFormatClimate.MISSING) {
            periodData.setDayMinTempList(getMinTempOccurrences(beginDate,
                    endDate, stationID, periodData.getMinTemp(), currentType));
        }

        /* average min temp */
        periodData.setMinTempMean(
                avgMinTempMean(beginDate, endDate, currentType, stationID));

        /* number of days for min temp thresholds */
        if (monthly) {
            periodData.setNumMinLessThan32F(sumReportMinTempLess32F(beginDate,
                    endDate, PeriodType.OTHER, stationID));

            periodData.setNumMinLessThan0F(sumReportMinTempLess0F(beginDate,
                    endDate, PeriodType.OTHER, stationID));

            periodData.setNumMinGreaterThanT4F(
                    sumReportMinTempGreaterT4F(beginDate, endDate,
                            PeriodType.OTHER, stationID, globalValues));

            periodData.setNumMinLessThanT5F(sumReportMinTempLessT5F(beginDate,
                    beginDate, PeriodType.OTHER, stationID, globalValues));

            periodData.setNumMinLessThanT6F(sumReportMinTempLessT6F(beginDate,
                    endDate, PeriodType.OTHER, stationID, globalValues));
        }

        /*
         * DAI 29, Task 23172- We would prefer to do calculation not from
         * rounded numbers, which is what could potentially happen with this
         * code. Needs resolution
         */
        if ((periodData
                .getMaxTempMean() != (float) ParameterFormatClimate.MISSING)
                && (periodData
                        .getMinTempMean() != (float) ParameterFormatClimate.MISSING)) {
            periodData.setMeanTemp(
                    (periodData.getMaxTempMean() + periodData.getMinTempMean())
                            / 2);
        }

        // cumulative precipitation section
        periodData.setPrecipTotal(
                getSumTotalPrecip(beginDate, endDate, stationID, currentType));

        // average precipitation for month (only for period type 0 (monthly))
        if (monthly) {
            float averagePrecip = getAvgTotalPrecip(beginDate, endDate,
                    stationID, PeriodType.OTHER);

            if (averagePrecip != ParameterFormatClimate.MISSING_PRECIP) {
                periodData.setPrecipMeanDay(averagePrecip);
            } else {
                periodData.setPrecipMeanDay(ParameterFormatClimate.TRACE);
            }
        }

        // threshold precipitation
        if (monthly) {
            periodData.setNumPrcpGreaterThan01(sumReportPrecipGreater01(
                    beginDate, endDate, PeriodType.OTHER, stationID));

            periodData.setNumPrcpGreaterThan10(sumReportPrecipGreater10(
                    beginDate, endDate, PeriodType.OTHER, stationID));

            periodData.setNumPrcpGreaterThan50(sumReportPrecipGreater50(
                    beginDate, endDate, PeriodType.OTHER, stationID));

            periodData.setNumPrcpGreaterThan100(sumReportPrecipGreater100(
                    beginDate, endDate, PeriodType.OTHER, stationID));

            periodData.setNumPrcpGreaterThanP1(
                    sumReportPrecipGreaterP1(beginDate, endDate,
                            PeriodType.OTHER, stationID, globalValues));

            periodData.setNumPrcpGreaterThanP2(
                    sumReportPrecipGreaterP2(beginDate, endDate,
                            PeriodType.OTHER, stationID, globalValues));
        }

        /*
         * Legacy documentation:
         * 
         * 12/18/00 - Doug Murphy: Find max 24-hr precip from stored daily data
         * first.
         * 
         * In cases where DSM might be missing, this will end up being the max
         * 24-hr value for the F-6 report
         */
        if (monthly) {
            periodData.setPrecipMax24H(getMaxTotalPrecip(beginDate, endDate,
                    stationID, PeriodType.OTHER));

            if (periodData.getPrecipMax24H() == 0) {
                /* check for trace amounts */
                int traceReports = getNumTotalPrecipTrace(beginDate, endDate,
                        stationID, PeriodType.OTHER);
                if ((traceReports == 0)
                        || (traceReports == ParameterFormatClimate.MISSING)) {
                    // no reports with trace precipitation
                    periodData.setPrecipMax24H(
                            ParameterFormatClimate.MISSING_PRECIP);
                } else {
                    // at least one report with trace precipitation
                    periodData.setPrecipMax24H(ParameterFormatClimate.TRACE);
                }
            }

            /* dates with max 24H precip */
            if ((periodData
                    .getPrecipMax24H() != ParameterFormatClimate.MISSING_PRECIP)) {
                periodData.setPrecip24HDates(
                        getMaxTotalPrecipOccurrences(beginDate, endDate,
                                stationID, periodData.getPrecipMax24H()));
            }
        }

        /*
         * Legacy documentation:
         * 
         * max 24 hour precip using DSM from cli_asos_daily table *
         * 
         * must add a day to the interval since 24 hr can span
         *
         * since DSM is only good for a year, have a problem with the year value
         * of the 24h max precip. Therefore, split this up into month vs season
         * or year. Do a separate SQL on the month 24 hr max to get the season
         * and year values
         */
        if (monthly) {
            Calendar searchRange24HoursBeginCal = beginDate
                    .getCalendarFromClimateDate();
            searchRange24HoursBeginCal.add(Calendar.DATE, -1);
            ClimateDate searchRange24HoursBegin = new ClimateDate(
                    searchRange24HoursBeginCal);

            // search range duration is simply 1 more day than the initial
            // interval
            int searchRange24HoursNumDays = numDays + 1;

            String stationCode = getStationCodeByID(stationID);
            // precip search range begin, which will advance as we sift through
            // data
            ClimateDate precipSearchRange24HoursBegin = new ClimateDate(
                    searchRange24HoursBegin);
            /*
             * Logic of the next section:
             * 
             * The maximum 24 hour precipitation can be ANY 24 hour
             * precipitation total during the period. And it could be continued
             * from the previous period because it's reported in the current
             * period. For example, if we found the maximum 24 hour
             * precipitation ran from noon Oct 31 to noon Nov 1, and none of the
             * other Novemenber 24 hour precipitation amounts were greater, then
             * the maximum 24 hour precipitation would be reported as Oct 31 to
             * Nov 1 for the month of November.
             * 
             * Therefore, so date spanning around 24 hour time periods can
             * occur, the routine must grab 48 hours of precipitation data and
             * loop through the information 24 hours at a time.
             * 
             * A few notes though:
             * 
             * 1. If the max 24 hour precip occurs during the first 24 hour
             * period, it is outside the period and must be ignored.
             * 
             * 2. Must account for at least 3 days where the maximum was the
             * same
             * 
             * 3. Must account when the second 24 hour period read in contains
             * the max 24 hr precip. This could cause a duplicate date of
             * occurrence situation
             * 
             * 4. Have to find the hours of occurrence as well
             * 
             */
            /*
             * Task 25624 code does nothing if the number of days is too large
             */
            /* do it over the month first */
            if (searchRange24HoursNumDays <= 32) {
                calculate24HMaxPrecip(periodData, searchRange24HoursNumDays,
                        stationCode, precipSearchRange24HoursBegin);
            } // end of days check
        } else {
            /*
             * For the season and the year, we don't need to call the
             * cli_asos_daily table, but just go through the dates of 24-hr
             * precipitation for several months
             */
            // get maximum 24-hour precip by column
            periodData.setPrecipMax24H(
                    getMax24HPrecip(beginDate, endDate, stationID, itype));

            // get dates for 24-hour precip by the precip date columns
            if (periodData
                    .getPrecipMax24H() != ParameterFormatClimate.MISSING_PRECIP) {
                periodData.setPrecip24HDates(getMaxPrecip24HOccurrences(
                        beginDate, endDate, stationID,
                        periodData.getPrecipMax24H(), itype));
            }
        }

        // storm total precipitation
        // get maximum storm precip
        periodData.setPrecipStormMax(
                getMaxStormPrecip(beginDate, endDate, stationID, currentType));

        // get dates for max storm precip
        if (periodData
                .getPrecipStormMax() != ParameterFormatClimate.MISSING_PRECIP) {
            periodData.setPrecipStormList(
                    getMaxStormPrecipOccurrences(beginDate, endDate, stationID,
                            periodData.getPrecipStormMax(), currentType));
        }

        // avg seasonal/yearly precip
        if (!monthly) {
            // get mean precip
            periodData.setPrecipMeanDay(
                    getAvgMeanPrecip(beginDate, endDate, stationID, itype));
        }

        /*
         * snow since July 1 - need to find out which month it is and then
         * decrease the year by 1 if necessary
         */
        ClimateDate july1Date = new ClimateDate(1, 7, beginDate.getYear());
        switch (itype) {
        case OTHER:
        case MONTHLY_NWWS:
        case MONTHLY_RAD:
            // monthly, or other, and begin month is before July, decrease
            // year
            // by 1
            if (beginDate.getMon() < 7) {
                july1Date.setYear(july1Date.getYear() - 1);
            }
            break;
        case SEASONAL_NWWS:
        case SEASONAL_RAD:
            // seasonal and begin month is before June and end month is
            // before July
            if ((beginDate.getMon() < 6) && (endDate.getMon() < 7)) {
                july1Date.setYear(july1Date.getYear() - 1);
            }
            break;
        case ANNUAL_NWWS:
        case ANNUAL_RAD:
            // annual, and end month is before July
            if (endDate.getMon() < 7) {
                july1Date.setYear(july1Date.getYear() - 1);
            }
            break;
        default:
            throw new ClimateInvalidParameterException(
                    "Unhandled period type [" + itype + "]");
        }

        // Snow values should remain missing if the station doesn't report snow.
        if (globalValues.getSnowReportingStations().contains(icaoId)) {

            // cumulative snowfall
            periodData.setSnowTotal(getSumTotalSnow(beginDate, endDate,
                    stationID, currentType));

            /*
             * snow - water equivalent
             * 
             * Legacy description:
             * 
             * DAI 30, Task 23211: okay, use 1:10 ratio here? First we'll do
             * that but should look into using the DSM as well
             * 
             * Discrepancy #64: In Legacy Climate, it was possible for reports
             * to have -0.1 total water equivalent values, since it is
             * calculated from dividing the snow total by 10, and snow total
             * could be trace (-1.0). In Migrated Climate, if snow total is
             * trace, then total water equivalent will also be trace.
             */
            if (ClimateUtilities.floatingEquals(periodData.getSnowTotal(),
                    ParameterFormatClimate.TRACE)) {
                periodData.setSnowWater(ParameterFormatClimate.TRACE);
            } else if (periodData
                    .getSnowTotal() != ParameterFormatClimate.MISSING_SNOW) {
                periodData.setSnowWater(periodData.getSnowTotal() / 10);
            } else {
                periodData.setSnowWater(ParameterFormatClimate.MISSING_SNOW);
            }

            periodData.setSnowJuly1(getSumTotalSnow(july1Date, endDate,
                    stationID, currentType));

            /*
             * Legacy documentation:
             * 
             * DAI 30, Task 23211: again, 1:10 ratio for water equivalent
             */
            periodData.setSnowMax24H(getMax24HSnow(beginDate, endDate,
                    stationID, PeriodType.OTHER));

            if (periodData
                    .getSnowJuly1() != ParameterFormatClimate.MISSING_SNOW) {
                periodData.setSnowWaterJuly1(periodData.getSnowJuly1() / 10);
            } else {
                periodData
                        .setSnowWaterJuly1(ParameterFormatClimate.MISSING_SNOW);
            }

            // get days of different snow amounts
            if (monthly) {
                periodData.setNumSnowGreaterThanTR(sumReportSnowGreaterTR(
                        beginDate, endDate, PeriodType.OTHER, stationID));

                periodData.setNumSnowGreaterThan1(sumReportSnowGreater1(
                        beginDate, endDate, PeriodType.OTHER, stationID));

                periodData.setNumSnowGreaterThanS1(
                        sumReportSnowGreaterS1(beginDate, endDate,
                                PeriodType.OTHER, stationID, globalValues));
            }

            // 24 hour snow
            if (monthly) {
                /*
                 * Legacy documentation:
                 * 
                 * Find max 24-hr snowfall from stored daily data using total
                 * snow column.
                 */
                periodData.setSnowMax24H(
                        getMaxTotalSnow(beginDate, endDate, stationID));

                if (periodData.getSnowMax24H() == 0) {
                    /* check for trace amounts */
                    int traceReports = getNumTotalSnowTrace(beginDate, endDate,
                            stationID, PeriodType.OTHER);

                    if ((traceReports == 0)
                            || (traceReports == ParameterFormatClimate.MISSING)) {
                        // no trace snow reports
                        periodData.setSnowMax24H(
                                ParameterFormatClimate.MISSING_SNOW);
                    } else {
                        periodData.setSnowMax24H(ParameterFormatClimate.TRACE);
                    }
                }
                // dates with max 24H snow
                if ((periodData
                        .getSnowMax24H() != ParameterFormatClimate.MISSING_SNOW)
                        && (periodData
                                .getSnowMax24H() != ParameterFormatClimate.TRACE)) {
                    periodData.setSnow24HDates(getMaxTotalSnowOccurrences(
                            beginDate, endDate, stationID,
                            periodData.getSnowMax24H(), PeriodType.OTHER));
                }

            } else {
                // Find max 24-hour snowfall using the 24H snow column
                periodData.setSnowMax24H(
                        getMax24HSnow(beginDate, endDate, stationID, itype));

                // dates with max 24H snow
                if (periodData
                        .getSnowMax24H() != ParameterFormatClimate.MISSING_SNOW) {
                    periodData.setSnow24HDates(getMax24HSnowOccurrences(
                            beginDate, endDate, stationID,
                            periodData.getSnowMax24H(), itype));
                }
            }

            // snow storm totals
            // find max snow storm using the column
            periodData.setSnowMaxStorm(getMaxSnowStorm(beginDate, endDate,
                    stationID, currentType));

            if (periodData
                    .getSnowMaxStorm() != ParameterFormatClimate.MISSING_SNOW) {
                // get start and end dates of max snow storm
                periodData.setSnowStormList(getMaxSnowStormOccurrences(
                        beginDate, endDate, stationID,
                        periodData.getSnowMaxStorm(), currentType));
            }

            // snow depth information
            periodData.setSnowGroundMean(getAvgMeanSnowOnGround(beginDate,
                    endDate, stationID, currentType));

            // max snow on ground
            periodData.setSnowGroundMax(getMaxSnowGround(beginDate, endDate,
                    stationID, currentType));

            if ((periodData
                    .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW_VALUE)
                    && ((periodData.getSnowGroundMax() > 0) || (periodData
                            .getSnowGroundMax() == ParameterFormatClimate.TRACE))) {
                // get dates of max snow on ground, if the value is not missing
                // and
                // is either greater than 0 or the trace amount
                periodData.setSnowGroundMaxDateList(getMaxSnowGroundOccurrences(
                        beginDate, endDate, stationID,
                        periodData.getSnowGroundMax(), currentType));
            }
        }

        // heating degree days
        // sum regular heating degrees
        periodData.setNumHeatTotal(getSumHeatDegreeDays(beginDate, endDate,
                stationID, currentType));

        // sum July 1st heating degrees
        periodData.setNumHeat1July(getSumHeatDegreeDays(july1Date, endDate,
                stationID, currentType));

        // cooling degree days
        // sum regular cooling degrees
        periodData.setNumCoolTotal(getSumCoolDegreeDays(beginDate, endDate,
                stationID, currentType));

        // sum January 1st cooling degrees
        ClimateDate jan1Date = ClimateDate.getMissingClimateDate();
        switch (itype) {
        case OTHER:
        case MONTHLY_NWWS:
        case MONTHLY_RAD:
            // monthly, or other; use the begin date year
            jan1Date = new ClimateDate(1, 1, beginDate.getYear());
            break;
        case SEASONAL_NWWS:
        case SEASONAL_RAD:
            // seasonal, use end date year
            jan1Date = new ClimateDate(1, 1, endDate.getYear());
            break;
        case ANNUAL_NWWS:
        case ANNUAL_RAD:
            // annual
            if (beginDate.getMon() > endDate.getMon()) {
                // begin date is later month than end date, use end date year
                jan1Date = new ClimateDate(1, 1, endDate.getYear());
            } else {
                // begin date is same or earlier month than end date, use begin
                // date year
                jan1Date = new ClimateDate(1, 1, beginDate.getYear());
            }
            break;
        default:
            throw new ClimateInvalidParameterException(
                    "Unhandled period type [" + itype + "]");
        }

        periodData.setNumCool1Jan(getSumCoolDegreeDays(jan1Date, endDate,
                stationID, currentType));

        // maximum wind
        float maxWindSpeed = getMaxWindSpeed(beginDate, endDate, stationID,
                currentType);

        if (maxWindSpeed != ParameterFormatClimate.MISSING_SPEED) {
            // dates and directions of max winds, combining with given speed to
            // get full data
            getMaxWindSpeedOccurrencesAndDir(beginDate, endDate, stationID,
                    maxWindSpeed, currentType, periodData.getMaxWindDayList(),
                    periodData.getMaxWindList());
        }

        // maximum gust
        float maxGustSpeed = getMaxGustSpeed(beginDate, endDate, stationID,
                currentType);

        if (maxGustSpeed != ParameterFormatClimate.MISSING_SPEED) {
            // dates and directions of max gusts, combining with given speed to
            // get full data
            getMaxGustSpeedOccurrencesAndDir(beginDate, endDate, stationID,
                    maxGustSpeed, currentType, periodData.getMaxGustDayList(),
                    periodData.getMaxGustList());
        }

        // percent sun
        periodData.setPossSun(
                getAvgPossSun(beginDate, endDate, stationID, currentType));

        // mean sky cover
        periodData.setMeanSkyCover(
                getAvgMeanSkyCover(beginDate, endDate, stationID, currentType));

        // number cloudy days
        if (monthly) {
            /*
             * Legacy documentation:
             * 
             * Task 23218
             * 
             * Note: the display will show values of
             * 
             * 0-.3 for fair
             * 
             * .4-.7 for partly cloudy
             * 
             * .8-1.0 for cloudy
             * 
             * We're assuming that rounding up will be okay here as the average
             * sky cover for the month can be .313 or .35 when Informix does the
             * average. Thresholds below reflect our assumptions.
             */
            periodData.setNumMostlyCloudyDays(
                    getNumMostlyCloudy(beginDate, endDate, stationID));

            periodData.setNumPartlyCloudyDays(
                    getNumPartlyCloudy(beginDate, endDate, stationID));

            periodData
                    .setNumFairDays(getNumFair(beginDate, endDate, stationID));
        }

        // summing weather elements
        periodData.setNumThunderStorms(getSumNumThunderStorms(beginDate,
                endDate, stationID, currentType));

        periodData.setNumMixedPrecip(getSumNumMixedPrecip(beginDate, endDate,
                stationID, currentType));

        periodData.setNumHeavyRain(
                getSumNumHeavyRain(beginDate, endDate, stationID, currentType));

        periodData.setNumRain(
                getSumNumRain(beginDate, endDate, stationID, currentType));

        periodData.setNumLightRain(
                getSumNumLightRain(beginDate, endDate, stationID, currentType));

        periodData.setNumFreezingRain(getSumNumFreezingRain(beginDate, endDate,
                stationID, currentType));

        periodData.setNumLightFreezingRain(getSumNumLightFreezingRain(beginDate,
                endDate, stationID, currentType));

        periodData.setNumHail(
                getSumNumHail(beginDate, endDate, stationID, currentType));

        periodData.setNumHeavySnow(
                getSumNumHeavySnow(beginDate, endDate, stationID, currentType));

        periodData.setNumSnow(
                getSumNumSnow(beginDate, endDate, stationID, currentType));

        periodData.setNumLightSnow(
                getSumNumLightSnow(beginDate, endDate, stationID, currentType));

        periodData.setNumIcePellets(getSumNumIcePellets(beginDate, endDate,
                stationID, currentType));

        periodData.setNumFog(
                getSumNumFog(beginDate, endDate, stationID, currentType));

        periodData.setNumFogQuarterSM(
                getSumNumHeavyFog(beginDate, endDate, stationID, currentType));

        periodData.setNumHaze(
                getSumNumHaze(beginDate, endDate, stationID, currentType));

        // average humidity
        periodData.setMeanRh(
                getAvgMeanRh(beginDate, endDate, stationID, currentType));

        // average wind speed
        periodData.setAvgWindSpd(
                getAvgWindSpeed(beginDate, endDate, stationID, currentType));

        /*
         * early/late freeze dates logic moved to caller due to functionality
         * being in another DAO.
         */

        return periodData;
    }

    /**
     * Overload - Station id and ClimateDates parameters
     * 
     * @param stationId
     * @param dates
     * @param globalValues
     * @param itype
     * @return PeriodData;
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    public PeriodData buildPeriodObsClimo(int stationId, ClimateDates dates,
            ClimateGlobal globalValues, PeriodType itype)
            throws ClimateQueryException, ClimateInvalidParameterException {

        PeriodData data = PeriodData.getMissingPeriodData();
        data.setInformId(stationId);

        return buildPeriodObsClimo(dates.getStart(), dates.getEnd(), data,
                globalValues, itype, false);
    }

    /**
     * Calculate 24-hour max precip, looking at hourly amounts in Daily ASOS.
     * 
     * @param periodData
     *            period data to set 24-hour precip for.
     * @param searchRangeDays
     *            number of days in search range.
     * @param stationCode
     *            station code to search for.
     * @param beginDate
     * @throws ClimateQueryException
     */
    private void calculate24HMaxPrecip(PeriodData periodData,
            int searchRangeDays, String stationCode, ClimateDate beginDate)
            throws ClimateQueryException {
        // flag to see if have precipitation data
        boolean maxPrecipFound = false;

        /* loop through number of days */
        for (int k = 0; k < searchRangeDays - 1; k++) {
            float[] precip48HourData = new float[48];

            /* loop through two 24 hour periods */
            for (int j = 0; j < 48; j += TimeUtil.HOURS_PER_DAY) {
                StringBuilder query = new StringBuilder(
                        "SELECT pcp_hr_amt_01, pcp_hr_amt_02, ");
                query.append(" pcp_hr_amt_03, pcp_hr_amt_04, pcp_hr_amt_05, ");
                query.append(" pcp_hr_amt_06, pcp_hr_amt_07, pcp_hr_amt_08, ");
                query.append(" pcp_hr_amt_09, pcp_hr_amt_10, pcp_hr_amt_11, ");
                query.append(" pcp_hr_amt_12, pcp_hr_amt_13, pcp_hr_amt_14, ");
                query.append(" pcp_hr_amt_15, pcp_hr_amt_16, pcp_hr_amt_17, ");
                query.append(" pcp_hr_amt_18, pcp_hr_amt_19, pcp_hr_amt_20, ");
                query.append(" pcp_hr_amt_21, pcp_hr_amt_22, pcp_hr_amt_23, ");
                query.append(" pcp_hr_amt_24 FROM ");
                query.append(ClimateDAOValues.CLI_ASOS_DAILY_TABLE_NAME);
                query.append(" WHERE station_code = :stationCode");
                query.append(" AND day_of_year = :date AND year = :year");

                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("stationCode", stationCode);
                queryParams.put("date", beginDate.toMonthDayDateString());
                queryParams.put("year", beginDate.getYear());

                try {
                    Object[] results = getDao()
                            .executeSQLQuery(query.toString(), queryParams);
                    if ((results != null) && (results.length >= 1)) {
                        for (Object result : results) {
                            if (result instanceof Object[]) {
                                Object[] oa = (Object[]) result;
                                // nulls are ok
                                int index = 0;
                                for (int i = j; i < j
                                        + TimeUtil.HOURS_PER_DAY; i++) {
                                    Object precipData = oa[index++];
                                    // any of the values could be null
                                    if (precipData == null) {
                                        precip48HourData[i] = ParameterFormatClimate.MISSING_PRECIP;
                                    } else {
                                        precip48HourData[i] = ((Number) precipData)
                                                .floatValue();
                                        /*
                                         * Legacy did not validate that data is
                                         * actually present and not the missing
                                         * value before flipping this flag, only
                                         * that a row was returned. Moved to
                                         * when non-missing data is found.
                                         * 
                                         * Found 1 day's worth of precip
                                         */
                                        maxPrecipFound = true;
                                    }
                                }
                            } else {
                                throw new ClimateQueryException(
                                        "Unexpected return type from query, expected Object[], got "
                                                + result.getClass().getName());
                            }
                        }
                    } else {
                        logger.warn(
                                "Couldn't find a precip measures for the query: ["
                                        + query + "] and map ["
                                        + queryParams.toString() + "]");
                        /* if no rows found, set array to missing */
                        for (int i = j; i < j + TimeUtil.HOURS_PER_DAY; i++) {
                            precip48HourData[i] = ParameterFormatClimate.MISSING_PRECIP;
                        }
                    }
                } catch (Exception e) {
                    throw new ClimateQueryException(
                            "An error was encountered retrieving with query: ["
                                    + query + "] and map ["
                                    + queryParams.toString() + "]",
                            e);
                }
                /* advance 1 day every other time */
                if (j < TimeUtil.HOURS_PER_DAY) {
                    Calendar tempCal = beginDate.getCalendarFromClimateDate();
                    tempCal.add(Calendar.DATE, 1);
                    beginDate = new ClimateDate(tempCal);
                }
            } // end of inner loop getting 48 hours of data

            /*
             * 11/22/99 Here's the idea for at least the month: Since the 24
             * hour precip can span dates, need to check two days' worth of
             * precip information in 24 hour blocks. There are 25 blocks over
             * two days. Sum the 24 hours and compare to the existing max. We
             * need to keep track of the dates as well. So if we find a max 24h
             * precip, make that the one and save the day. If it's the first
             * block of 24 hours for the day we've grabbed, then no date span
             * (dates are equal). If it's not the last block, dates span. Last
             * block, second date only and no date span again.
             * 
             * If the precip for the block is equal we need to save the date.
             */
            /* continue if have precipitation data */
            if (maxPrecipFound) {
                /* loop through the number of 24 hour periods */
                /*
                 * Legacy skipped results when both k and i were 0, so start i
                 * from 1 instead and get rid of the check.
                 */
                for (int i = 1; i < TimeUtil.HOURS_PER_DAY + 1; i++) {
                    /* initialize the precipitation total */
                    float currSum = 0;

                    /* loop through 24 hours */
                    for (int j = 0; j < TimeUtil.HOURS_PER_DAY; j++) {
                        /*
                         * throw out those 24 hour groups with missing data
                         */
                        if (precip48HourData[j
                                + i] == ParameterFormatClimate.MISSING_PRECIP) {
                            currSum = ParameterFormatClimate.MISSING_PRECIP;
                            /*
                             * Legacy would keep summing at this point, and
                             * continue through the rest of the data doing
                             * nothing or re-assigning the same value. Faster to
                             * just break here.
                             */
                            break;
                        } else if ((precip48HourData[j
                                + i] < ParameterFormatClimate.MISSING_PRECIP)
                                && (precip48HourData[j + i] >= 0)) {
                            /* sum the precip for the 24 hour block */
                            currSum += precip48HourData[j + i];
                        }
                    }

                    /* now must check for trace */
                    if (currSum == 0) {
                        /* loop through 24 hours */
                        for (int j = 0; j < TimeUtil.HOURS_PER_DAY; j++) {
                            if (precip48HourData[j + i] <= 0) {
                                /* set to trace for 24 hours */
                                currSum = ParameterFormatClimate.TRACE;
                                break;
                            }
                        }
                    }
                    /*
                     * Legacy documentation:
                     * 
                     * if this is the very first 24 hour period or if there's
                     * missing data, ignore
                     * 
                     * Migration modification:
                     * 
                     * Legacy skipped results when both k and i were 0, so
                     * starting i from 1 instead and got rid of the check.
                     */
                    if (currSum != ParameterFormatClimate.MISSING_PRECIP) {

                        if ((periodData
                                .getPrecipMax24H() == ParameterFormatClimate.MISSING_PRECIP)
                                || (currSum > periodData.getPrecipMax24H()
                                        && (currSum != 0 || periodData
                                                .getPrecipMax24H() != ParameterFormatClimate.TRACE))
                                || (currSum == ParameterFormatClimate.TRACE
                                        && periodData.getPrecipMax24H() == 0)) {
                            // a new maximum was found
                            /*
                             * reset the period data's max 24 hour dates list
                             */
                            ClimateDates newPrecipDates = getPrecipDatesFromDateAndHour(
                                    beginDate, i);

                            // assign start of new dates collection
                            List<ClimateDates> precipDates = new ArrayList<>();
                            precipDates.add(newPrecipDates);
                            periodData.setPrecip24HDates(precipDates);
                            // assign new max value
                            periodData.setPrecipMax24H(currSum);

                        } else if (ClimateUtilities.floatingEquals(currSum,
                                periodData.getPrecipMax24H())
                                && (currSum != 0)) {
                            /* a second and equal maximum was found */

                            // Legacy would limit dates to 3 sets; this
                            // is unnecessary here and potentially
                            // limiting to future expansion

                            ClimateDates newPrecipDates = getPrecipDatesFromDateAndHour(
                                    beginDate, i);

                            /*
                             * check if this 24 hour period is a
                             * repeat/overlapping of the last
                             * 
                             * Legacy only checked days; now will month, day,
                             * and year for completeness.
                             */
                            ClimateDates existingDates;
                            if (periodData.getPrecip24HDates().size() > 0) {
                                existingDates = periodData.getPrecip24HDates()
                                        .get(periodData.getPrecip24HDates()
                                                .size() - 1);
                            } else {
                                existingDates = ClimateDates
                                        .getMissingClimateDates();
                            }

                            if (!((newPrecipDates.getStart()
                                    .equals(existingDates.getStart()))
                                    && (newPrecipDates.getEnd()
                                            .equals(existingDates.getEnd())))) {
                                periodData.getPrecip24HDates()
                                        .add(newPrecipDates);
                            }
                        }
                    }
                }
            }
        } // end of outer loop
    }

    /**
     * Get the average of average wind speeds for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return average of average wind speeds, or the missing value.
     */
    private float getAvgWindSpeed(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType,
                "avg_wind_speed", "avg_wind_spd",
                ClimateDAO.BuildElementType.AVG,
                ParameterFormatClimate.MISSING_SPEED).floatValue();
    }

    /**
     * Get the average of mean humidity for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return average of mean humidity, rounded, or the missing value.
     */
    private int getAvgMeanRh(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        /*
         * Legacy used to take the average of the max RH from daily_climate, the
         * average of the min RH from daily_climate, and then average those.
         * However for period it was then switched over to averaging the mean RH
         * from cli_mon_season_yr and calling that the maximum, averaging the
         * same thing again and calling that the minimum, and then averaging
         * those values.
         */
        float maxRhMean = buildElement(beginDate, endDate, stationID, iType,
                "max_rh", "mean_rh", ClimateDAO.BuildElementType.AVG,
                (float) ParameterFormatClimate.MISSING).floatValue();

        if (maxRhMean != (float) ParameterFormatClimate.MISSING) {
            float minRhMean = buildElement(beginDate, endDate, stationID, iType,
                    "min_rh", "mean_rh", ClimateDAO.BuildElementType.AVG,
                    (float) ParameterFormatClimate.MISSING).floatValue();

            if (minRhMean != (float) ParameterFormatClimate.MISSING) {
                return ClimateUtilities.nint((minRhMean + maxRhMean) / 2);
            }
        }

        return ParameterFormatClimate.MISSING;
    }

    /**
     * Get the sum of haze for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of haze, or the missing value.
     */
    private int getSumNumHaze(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_15",
                "num_h", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of heavy fog for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of heavy fog, or the missing value.
     */
    private int getSumNumHeavyFog(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_14",
                "num_fquarter", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of fog for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of fog, or the missing value.
     */
    private int getSumNumFog(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_13",
                "num_f", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of ice pellets for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of ice pellets, or the missing value.
     */
    private int getSumNumIcePellets(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_12",
                "num_ip", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of light snow for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of light snow, or the missing value.
     */
    private int getSumNumLightSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_11",
                "num_s", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of snow for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of snow, or the missing value.
     */
    private int getSumNumSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_10",
                "num_ss", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of heavy snow for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of heavy snow, or the missing value.
     */
    private int getSumNumHeavySnow(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_9",
                "num_sss", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of hail for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of hail, or the missing value.
     */
    private int getSumNumHail(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_8",
                "num_a", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of light freezing rain for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of light freezing rain, or the missing value.
     */
    private int getSumNumLightFreezingRain(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_7",
                "num_zr", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of freezing rain for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of freezing rain, or the missing value.
     */
    private int getSumNumFreezingRain(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_6",
                "num_zrr", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of light rain for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of light rain, or the missing value.
     */
    private int getSumNumLightRain(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_5",
                "num_r", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of rain for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of rain, or the missing value.
     */
    private int getSumNumRain(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_4",
                "num_rr", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of heavy rain for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of heavy rain, or the missing value.
     */
    private int getSumNumHeavyRain(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_3",
                "num_rrr", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of mixed precip for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of mixed precip, or the missing value.
     */
    private int getSumNumMixedPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_2",
                "num_p", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the sum of thunderstorms for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of thunderstorms, or the missing value.
     */
    private int getSumNumThunderStorms(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "wx_1",
                "num_t", ClimateDAO.BuildElementType.SUM).intValue();
    }

    /**
     * Get the count of reports that are fair, examining the avg sky cover
     * column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * Task 25627 Legacy has no cloudy calculations for non-zero period types
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return number of reports with fair sky, or the missing value.
     */
    private int getNumFair(ClimateDate beginDate, ClimateDate endDate,
            int stationID) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :beginDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id = :stationID");
        query.append(" AND avg_sky_cover >= 0 AND avg_sky_cover < 0.35");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the count of reports that are partly cloudy, examining the avg sky
     * cover column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * Task 25627 Legacy has no cloudy calculations for non-zero period types
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return number of reports with partly cloudy sky, or the missing value.
     */
    private int getNumPartlyCloudy(ClimateDate beginDate, ClimateDate endDate,
            int stationID) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :beginDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id = :stationID");
        query.append(" AND avg_sky_cover >= 0.35 AND avg_sky_cover < 0.75");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the count of reports that are mostly cloudy, examining the avg sky
     * cover column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * Task 25627 Legacy has no cloudy calculations for non-zero period types
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return number of reports with mostly cloudy sky, or the missing value.
     */
    private int getNumMostlyCloudy(ClimateDate beginDate, ClimateDate endDate,
            int stationID) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :beginDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id = :stationID");
        query.append(" AND avg_sky_cover >= 0.75 AND avg_sky_cover != ");
        query.append(":missing");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        queryParams.put("missing", ParameterFormatClimate.MISSING);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the average of mean sky cover for the given dates, station, and
     * period type by averaging over the mean/avg sky cover column for
     * applicable reports, where the value is not missing.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return Missing value if nothing could be found or the average of mean
     *         sky cover).
     */
    private float getAvgMeanSkyCover(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType,
                "avg_sky_cover", "mean_sky_cover",
                ClimateDAO.BuildElementType.AVG,
                (float) ParameterFormatClimate.MISSING).floatValue();
    }

    /**
     * Get the average of possible sun for the given dates, station, and period
     * type by averaging over the possible sun column for applicable reports,
     * where the value is not missing.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return Missing value if nothing could be found or the average of
     *         possible sun (rounded).
     */
    private int getAvgPossSun(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        float possSun = buildElement(beginDate, endDate, stationID, iType,
                "percent_pos_sun", "poss_sun", ClimateDAO.BuildElementType.AVG,
                (float) ParameterFormatClimate.MISSING).floatValue();

        if (possSun == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        } else {
            return ClimateUtilities.nint(possSun);
        }
    }

    /**
     * Get the occurrences of the given max gust speed, rounded to 2 decimals.
     * Rewritten from build_period_obs_climo.ecpp,
     * calc_period_obs.ecpp#return_element_dates,
     * calc_period_obs.ecpp#fill_period_date, and
     * calc_period_obs.ecpp#fill_period_dir.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxGustSpeed
     * @param iType
     *            if type other than 0, filter by type 5.
     * @param dates
     *            dates list to add to
     * @param gusts
     *            list to add to
     * @return
     * @throws ClimateQueryException
     */
    private void getMaxGustSpeedOccurrencesAndDir(ClimateDate beginDate,
            ClimateDate endDate, int stationID, float maxGustSpeed,
            PeriodType iType, List<ClimateDate> dates, List<ClimateWind> gusts)
            throws ClimateQueryException {
        int expectedDateResultSize;

        StringBuilder dateQuery;
        Map<String, Object> dateQueryParams = new HashMap<>();

        int dirSearchLimit;

        StringBuilder dirQuery = new StringBuilder("SELECT max_gust_dir");
        if (PeriodType.OTHER.equals(iType)) {
            dirQuery.append(" FROM ");
            dirQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            dirQuery.append(" WHERE date = :date AND station_id = ");
        } else {
            dirQuery.append(":dirIndex FROM ");
            dirQuery.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            dirQuery.append(
                    " WHERE max_gust_date:dirIndex = :date AND inform_id = ");

        }
        dirQuery.append(":stationID");

        Map<String, Object> dirQueryParams = new HashMap<>();

        if (PeriodType.OTHER.equals(iType)) {
            // go by period end date
            expectedDateResultSize = 1;
            dateQuery = new StringBuilder("SELECT date FROM ");
            dateQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            dateQuery.append(" WHERE date >= ");
            dateQuery.append(" :beginDate");
            dateQuery.append(" AND date <= :endDate");
            dateQuery.append(" AND station_id = :stationID");
            dateQuery.append(" AND ROUND(max_gust_spd::numeric, 2) >= :speed");
            dateQuery.append(" AND max_gust_spd != :missing");
            dateQuery.append(" ORDER BY max_gust_spd DESC");

            // only look at max_gust_dir1 column
            dirSearchLimit = 1;

            // legacy searched by nonexistent column "date"; assumed max gust
            // date
        } else {
            // go by actual date DB columns
            expectedDateResultSize = 3;
            dateQuery = new StringBuilder(
                    "SELECT max_gust_date1, max_gust_date2, max_gust_date3 FROM ");
            dateQuery.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            dateQuery.append(" WHERE period_start >= ");
            dateQuery.append(" :beginDate");
            dateQuery.append(" AND period_end <= :endDate");
            dateQuery.append(" AND inform_id = :stationID");
            dateQuery.append(" AND ROUND(max_gust_spd::numeric, 2) >= :speed");
            dateQuery.append(" AND max_gust_spd != :missing");
            dateQuery.append(" AND period_type = :periodType");
            dateQuery.append(" ORDER BY max_gust_spd DESC");
            dateQueryParams.put("periodType", 5);

            // look at max_gust_dir1, max_gust_dir2, and max_gust_dir3 columns
            dirSearchLimit = 3;

            dirQuery.append(" AND period_type = :periodType");

            dirQueryParams.put("periodType", 5);
        }
        dateQueryParams.put("beginDate",
                beginDate.getCalendarFromClimateDate());
        dateQueryParams.put("endDate", endDate.getCalendarFromClimateDate());
        dateQueryParams.put("stationID", stationID);
        dateQueryParams.put("speed", ClimateUtilities.nint(maxGustSpeed, 2));
        dateQueryParams.put("missing", ParameterFormatClimate.MISSING_SPEED);

        dirQueryParams.put("stationID", stationID);

        try {
            Object results = getDao().executeSQLQuery(dateQuery.toString(),
                    dateQueryParams);
            if (results != null && results instanceof java.sql.Date) {

                ClimateDate searchDate = new ClimateDate(results);
                if (!searchDate.isMissing()) {
                    // add dates to the list
                    dates.add(searchDate);

                    gusts = getWindDirList(stationID, gusts, dirQueryParams,
                            dirQuery, dateQueryParams, dateQuery, searchDate,
                            dirSearchLimit, maxGustSpeed);
                }

            } else if ((results != null)
                    && (((Object[]) results).length >= 1)) {
                for (Object result : (Object[]) results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int searchDateIndex = 0; searchDateIndex < expectedDateResultSize; searchDateIndex++) {
                            Object dateObj = oa[searchDateIndex];
                            // dates can be null
                            if (dateObj != null) {
                                ClimateDate searchDate = new ClimateDate(
                                        dateObj);
                                if (!searchDate.isMissing()) {
                                    // add dates to the list
                                    dates.add(searchDate);

                                    gusts = getWindDirList(stationID, gusts,
                                            dirQueryParams, dirQuery,
                                            dateQueryParams, dateQuery,
                                            searchDate, dirSearchLimit,
                                            maxGustSpeed);
                                }
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        ClimateDate searchDate = new ClimateDate(result);
                        if (!searchDate.isMissing()) {
                            // add dates to the list
                            dates.add(searchDate);

                            gusts = getWindDirList(stationID, gusts,
                                    dirQueryParams, dirQuery, dateQueryParams,
                                    dateQuery, searchDate, dirSearchLimit,
                                    maxGustSpeed);
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find max gust speed dates for the query: ["
                                + dateQuery + "] and map ["
                                + dateQueryParams.toString() + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: ["
                            + dateQuery + "] and map ["
                            + dateQueryParams.toString() + "]",
                    e);
        }
    }

    /**
     * Get the max gust speed for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max gust speed, or the missing value.
     */
    private float getMaxGustSpeed(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return ((Number) buildElement(beginDate, endDate, stationID, iType,
                "max_gust_spd", "max_gust_spd", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_SPEED)).floatValue();
    }

    /**
     * Get the occurrences of the given max wind speed, rounded to 2 decimals.
     * Rewritten from build_period_obs_climo.ecpp,
     * calc_period_obs.ecpp#return_element_dates,
     * calc_period_obs.ecpp#fill_period_date, and
     * calc_period_obs.ecpp#fill_period_dir.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxWindSpeed
     * @param iType
     *            if type other than 0, filter by type 5.
     * @param dates
     *            dates list to add to
     * @param winds
     *            list to add to
     * @return
     * @throws ClimateQueryException
     */
    private void getMaxWindSpeedOccurrencesAndDir(ClimateDate beginDate,
            ClimateDate endDate, int stationID, float maxWindSpeed,
            PeriodType iType, List<ClimateDate> dates, List<ClimateWind> winds)
            throws ClimateQueryException {
        int expectedDateResultSize;

        StringBuilder dateQuery;
        Map<String, Object> dateQueryParams = new HashMap<>();

        int dirSearchLimit;
        StringBuilder dirQuery = new StringBuilder("SELECT max_wind_dir");
        if (PeriodType.OTHER.equals(iType)) {
            dirQuery.append(" FROM ");
            dirQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            dirQuery.append(" WHERE date = :date AND station_id = ");
        } else {
            dirQuery.append(":dirIndex FROM ");
            dirQuery.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            dirQuery.append(
                    " WHERE max_wind_date:dirIndex = :date AND inform_id = ");

        }
        dirQuery.append(":stationID");
        Map<String, Object> dirQueryParams = new HashMap<>();

        if (PeriodType.OTHER.equals(iType)) {
            // go by period end date
            expectedDateResultSize = 1;
            dateQuery = new StringBuilder("SELECT date FROM ");
            dateQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            dateQuery.append(" WHERE date >= ");
            dateQuery.append(" :beginDate");
            dateQuery.append(" AND date <= :endDate");
            dateQuery.append(" AND station_id = :stationID");
            dateQuery.append(" AND ROUND(max_wind_spd::numeric, 2) >= :speed");
            dateQuery.append(" AND max_wind_spd != :missing");
            dateQuery.append(" ORDER BY max_wind_spd DESC");

            // only look at max_wind_dir1 column
            dirSearchLimit = 1;

        } else {
            // go by actual date DB columns
            expectedDateResultSize = 3;
            dateQuery = new StringBuilder(
                    "SELECT max_wind_date1, max_wind_date2, max_wind_date3 FROM ");
            dateQuery.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            dateQuery.append(" WHERE period_start >= ");
            dateQuery.append(" :beginDate");
            dateQuery.append(" AND period_end <= :endDate");
            dateQuery.append(" AND inform_id = :stationID");
            dateQuery.append(" AND ROUND(max_wind_spd::numeric, 2) >= :speed");
            dateQuery.append(" AND max_wind_spd != :missing");
            dateQuery.append(" AND period_type = :periodType");
            dateQuery.append(" ORDER BY max_wind_spd DESC");
            dateQueryParams.put("periodType", 5);

            // look at max_wind_dir1, max_wind_dir2, and max_wind_dir3 columns
            dirSearchLimit = 3;

            dirQuery.append(" AND period_type = :periodType");

            dirQueryParams.put("periodType", 5);
        }
        dateQueryParams.put("beginDate",
                beginDate.getCalendarFromClimateDate());
        dateQueryParams.put("endDate", endDate.getCalendarFromClimateDate());
        dateQueryParams.put("stationID", stationID);
        dateQueryParams.put("speed", ClimateUtilities.nint(maxWindSpeed, 2));
        dateQueryParams.put("missing", ParameterFormatClimate.MISSING_SPEED);

        dirQueryParams.put("stationID", stationID);

        try {
            Object results = getDao().executeSQLQuery(dateQuery.toString(),
                    dateQueryParams);

            if (results != null && results instanceof java.sql.Date) {

                ClimateDate searchDate = new ClimateDate(results);
                if (!searchDate.isMissing()) {
                    // add dates to the list
                    dates.add(searchDate);

                    winds = getWindDirList(stationID, winds, dirQueryParams,
                            dirQuery, dateQueryParams, dateQuery, searchDate,
                            dirSearchLimit, maxWindSpeed);
                }

            } else if ((results != null)
                    && (((Object[]) results).length >= 1)) {
                for (Object result : (Object[]) results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int searchDateIndex = 0; searchDateIndex < expectedDateResultSize; searchDateIndex++) {
                            Object dateObj = oa[searchDateIndex];
                            // date could be null
                            if (dateObj != null) {
                                ClimateDate searchDate = new ClimateDate(
                                        dateObj);
                                if (!searchDate.isMissing()) {
                                    // add dates to the list
                                    dates.add(searchDate);

                                    winds = getWindDirList(stationID, winds,
                                            dirQueryParams, dirQuery,
                                            dateQueryParams, dateQuery,
                                            searchDate, dirSearchLimit,
                                            maxWindSpeed);
                                }
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        ClimateDate searchDate = new ClimateDate(result);

                        if (!searchDate.isMissing()) {
                            // add dates to the list
                            dates.add(searchDate);

                            winds = getWindDirList(stationID, winds,
                                    dirQueryParams, dirQuery, dateQueryParams,
                                    dateQuery, searchDate, dirSearchLimit,
                                    maxWindSpeed);
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find max wind speed dates for the query: ["
                                + dateQuery + "] and map ["
                                + dateQueryParams.toString() + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: ["
                            + dateQuery + "] and map ["
                            + dateQueryParams.toString() + "]",
                    e);
        }
    }

    /**
     * Helper method for querying wind directions by date.
     * 
     * @param stationID
     * @param winds
     * @param dirQueryParams
     * @param dirQuery
     * @param dateQueryParams
     * @param dateQuery
     * @param searchDate
     * @param dirSearchLimit
     * @param maxWindSpeed
     * @return
     */
    private List<ClimateWind> getWindDirList(int stationID,
            List<ClimateWind> winds, Map<String, Object> dirQueryParams,
            StringBuilder dirQuery, Map<String, Object> dateQueryParams,
            StringBuilder dateQuery, ClimateDate searchDate, int dirSearchLimit,
            float maxWindSpeed) {

        /*
         * look for directions involving this date
         * 
         * legacy repeated the same search 3 times for Period Type 0, whereas
         * other period types did 3 distinct searches. Introduced limiter so
         * type 0 only does 1 search and the other types still do their multiple
         * distinct searches.
         */
        for (int searchDirIndex = 1; searchDirIndex <= dirSearchLimit; searchDirIndex++) {
            dirQueryParams.put("date", searchDate.getCalendarFromClimateDate());

            int direction = ((Number) queryForOneValue(
                    dirQuery.toString().replaceAll(":dirIndex",
                            String.valueOf(searchDirIndex)),
                    dirQueryParams, ParameterFormatClimate.MISSING)).intValue();

            if (direction != ParameterFormatClimate.MISSING) {
                winds.add(new ClimateWind(direction, maxWindSpeed));
            } else {
                logger.warn("Could not find expected non-missing"
                        + " direction for date ["
                        + searchDate.toFullDateString() + "], station ID ["
                        + stationID + "], using query [" + dirQuery
                        + "] and map [" + dirQueryParams.toString()
                        + "]. Date retrieved from query: [" + dateQuery
                        + "] and map: [" + dateQueryParams.toString() + "]."
                        + "\nUsing missing value and"
                        + " skipping to next date.");
                winds.add(new ClimateWind(ParameterFormatClimate.MISSING,
                        maxWindSpeed));
                break;
            }
        }

        return winds;
    }

    /**
     * Get the max wind speed for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max wind speed, or the missing value.
     */
    private float getMaxWindSpeed(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        float value = ((Number) buildElement(beginDate, endDate, stationID,
                iType, "max_wind_spd", "max_wind_spd",
                ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_SPEED)).floatValue();
        return value;
    }

    /**
     * Get the sum of cooling degree days for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of cooling degree days, or the missing value.
     */
    private int getSumCoolDegreeDays(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "cool",
                "num_cool_total", ClimateDAO.BuildElementType.SUM,
                ParameterFormatClimate.MISSING_DEGREE_DAY).intValue();
    }

    /**
     * Get the sum of heating degree days for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of heating degree days, or the missing value.
     */
    private int getSumHeatDegreeDays(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "heat",
                "num_heat_total", ClimateDAO.BuildElementType.SUM,
                ParameterFormatClimate.MISSING_DEGREE_DAY).intValue();
    }

    /**
     * Get the occurrences of the given max snow on ground, rounded to 2
     * decimals. Rewritten from calc_period_obs.ecpp#return_element_dates and
     * #fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param snowGround
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDate> getMaxSnowGroundOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, int snowGround,
            PeriodType iType) throws ClimateQueryException {
        List<ClimateDate> dates = new ArrayList<>();

        int expectedResultSize;

        StringBuilder query;
        Map<String, Object> queryParams = new HashMap<>();

        if (PeriodType.OTHER.equals(iType)) {
            // go by daily table dates
            expectedResultSize = 1;
            query = new StringBuilder("SELECT date FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE date >= ");
            query.append(" :beginDate");
            query.append(" AND date <= :endDate");
            query.append(" AND station_id = :stationID");
            query.append(" AND ROUND(snow_ground::numeric, 2) >= ");
            query.append(":snowGround AND snow_ground != :missing");
            query.append(" ORDER BY snow_ground DESC");
        } else {
            // go by actual date DB columns in period table
            expectedResultSize = 3;
            query = new StringBuilder(
                    "SELECT snow_ground_date1, snow_ground_date2, snow_ground_date3 FROM ");
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE period_start >= ");
            query.append(" :beginDate");
            query.append(" AND period_end <= :endDate");
            query.append(" AND inform_id = :stationID");
            query.append(" AND ROUND(snow_ground_max::numeric, 2) >= ");
            query.append(":snowGround AND snow_ground_max != :missing");
            query.append(" AND period_type = :periodType");
            query.append(" ORDER BY snow_ground_max DESC");
            queryParams.put("periodType", 5);
        }
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        queryParams.put("snowGround", ClimateUtilities.nint(snowGround, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_SNOW);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int index = 0; index < expectedResultSize; index++) {
                            Object dateObj = oa[index];
                            // date could be null
                            if (dateObj != null) {
                                ClimateDate date = new ClimateDate(dateObj);
                                if (!date.isMissing()) {
                                    dates.add(date);
                                }
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        // date could be null
                        ClimateDate date = new ClimateDate(result);
                        if (!date.isMissing()) {
                            dates.add(new ClimateDate(date));
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find max snow ground dates for the query: ["
                                + query + "] and map [" + queryParams.toString()
                                + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the max snow on ground for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max snow on ground, or the missing value.
     */
    private int getMaxSnowGround(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "snow_ground",
                "snow_ground_max", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_SNOW_VALUE, true).intValue();
    }

    /**
     * Get the average of snow ground mean for the given dates, station, and
     * period type by averaging over the snow ground mean column for applicable
     * reports, where snow is not missing and is greater than or equal to trace.
     * 
     * Rewritten from build_period_obs_climo.ecpp,
     * calc_period_obs.ecpp#build_element, and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return Missing value if nothing could be found, the average of mean snow
     *         on ground, or trace if the average is less than 0.
     */
    private float getAvgMeanSnowOnGround(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType) {
        float meanSnowOnGround;

        if (PeriodType.OTHER.equals(iType)) {
            /*
             * period 0- sum snow ground, divide by days past trace. Return
             * rounded number, or trace if rounded number < 0.
             */
            float snowGroundSum = buildElement(beginDate, endDate, stationID,
                    iType, "snow_ground", "snow_ground_mean",
                    ClimateDAO.BuildElementType.SUM,
                    ParameterFormatClimate.MISSING_SNOW, true).floatValue();

            if (snowGroundSum < 0) {
                meanSnowOnGround = ParameterFormatClimate.TRACE;
            } else if (snowGroundSum == ParameterFormatClimate.MISSING_SNOW) {
                meanSnowOnGround = ParameterFormatClimate.MISSING_SNOW;
            } else {
                meanSnowOnGround = snowGroundSum / daysPastThresh(beginDate,
                        endDate, stationID, iType, "snow_ground",
                        "snow_ground_mean", ParameterFormatClimate.MISSING_SNOW,
                        ParameterFormatClimate.TRACE, true);
            }
        } else {
            /*
             * period non-0- avg snow ground mean. Return rounded number, or
             * trace if rounded number < 0.
             */
            meanSnowOnGround = buildElement(beginDate, endDate, stationID,
                    iType, "snow_ground", "snow_ground_mean",
                    ClimateDAO.BuildElementType.AVG,
                    ParameterFormatClimate.MISSING_SNOW, true).floatValue();
        }
        if (meanSnowOnGround == ParameterFormatClimate.MISSING_SNOW) {
            return ParameterFormatClimate.MISSING_SNOW;
        } else if (meanSnowOnGround >= 0) {
            return ClimateUtilities.nint(meanSnowOnGround);
        } else {
            return ParameterFormatClimate.TRACE;
        }
    }

    /**
     * Get the occurrences of the given max 24 hour snow, rounded to 2 decimal
     * places. Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxSnowStorm
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDates> getMaxSnowStormOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, float maxSnowStorm,
            PeriodType iType) throws ClimateQueryException {
        List<ClimateDates> dates = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT sno_stm_start_day1, sno_stm_end_day1, sno_stm_start_day2, ");
        query.append(
                " sno_stm_end_day2, sno_stm_start_day3, sno_stm_end_day3 FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(" :beginDate AND period_end <= ");
        query.append(" :endDate AND inform_id = :stationID");
        query.append(" AND snow_max_storm >= :maxSnowStorm");
        query.append(" AND snow_max_storm != :missing");
        Map<String, Object> queryParams = new HashMap<>();

        if (!PeriodType.OTHER.equals(iType)) {
            query.append(" AND period_type = :periodType");
            queryParams.put("periodType", 5);
        }
        query.append(" ORDER BY snow_max_storm DESC");

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        // round the value to 2 decimal places
        queryParams.put("maxSnowStorm", ClimateUtilities.nint(maxSnowStorm, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_SNOW);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int baseIndex = 0; baseIndex < 6; baseIndex += 2) {
                            Object dateObj1 = oa[baseIndex];
                            Object dateObj2 = oa[baseIndex + 1];
                            // dates can be null
                            if ((dateObj1 != null) && (dateObj2 != null)) {
                                ClimateDate date1 = new ClimateDate(dateObj1);
                                ClimateDate date2 = new ClimateDate(dateObj2);
                                if (!date1.isMissing() && !date2.isMissing()) {
                                    dates.add(new ClimateDates(date1, date2));
                                }
                            }
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find max snow storm dates for the query: ["
                                + query + "] and map [" + queryParams.toString()
                                + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the max snow storm for the given dates and station.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * Task 23241, Legacy does not have period type 0 implementation
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return max snow storm, or the missing value.
     */
    private float getMaxSnowStorm(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType type) {
        return buildElement(beginDate, endDate, stationID, type, "snow",
                "snow_max_storm", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_SNOW, true).floatValue();
    }

    /**
     * Get the occurrences of the given max 24 hour snow, rounded to 2 decimal
     * places. Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param max24HSnow
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDates> getMax24HSnowOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, float max24HSnow,
            PeriodType iType) throws ClimateQueryException {
        List<ClimateDates> dates = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT sno_24h_start_day1, sno_24h_end_day1, sno_24h_start_day2, ");
        query.append(
                " sno_24h_end_day2, sno_24h_start_day3, sno_24h_end_day3 FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(" :beginDate AND period_end <= ");
        query.append(" :endDate AND inform_id = :stationID");
        query.append(" AND ROUND(snow_max_24h::numeric, 2) >= :max24hSnow");
        query.append(" AND snow_max_24h != :missing");
        Map<String, Object> queryParams = new HashMap<>();

        if (!PeriodType.OTHER.equals(iType)) {
            query.append(" AND period_type = :periodType");
            queryParams.put("periodType", 5);
        }
        query.append(" ORDER BY snow_max_24h DESC");

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        // round the value to 2 decimal places
        queryParams.put("max24hSnow", ClimateUtilities.nint(max24HSnow, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_SNOW);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int baseIndex = 0; baseIndex < 6; baseIndex += 2) {
                            Object dateObj1 = oa[baseIndex];
                            Object dateObj2 = oa[baseIndex + 1];
                            // dates can be null
                            if ((dateObj1 != null) && (dateObj2 != null)) {
                                ClimateDate date1 = new ClimateDate(dateObj1);
                                ClimateDate date2 = new ClimateDate(dateObj2);
                                if (!date1.isMissing() && !date2.isMissing()) {
                                    dates.add(new ClimateDates(date1, date2));
                                }
                            }
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find max 24 hour snow dates for the query: ["
                                + query + "] and map [" + queryParams.toString()
                                + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the max of 24 hour snow for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max of 24 hour snow, or the missing value.
     */
    private float getMax24HSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return ((Number) buildElement(beginDate, endDate, stationID, iType,
                "snow", "snow_max_24h", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_SNOW, true)).floatValue();
    }

    /**
     * Get the occurrences of the given max total snow (rounded to 2 decimals).
     * Rewritten from calc_period_obs.ecpp#return_element_dates.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxTotalSnow
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDates> getMaxTotalSnowOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, float maxTotalSnow,
            PeriodType iType) throws ClimateQueryException {
        List<ClimateDates> dates = new ArrayList<>();
        Map<String, Object> queryParams = new HashMap<>();
        // go by period end date
        StringBuilder query = new StringBuilder("SELECT ");

        if (!PeriodType.OTHER.equals(iType)) {
            query.append("period_end FROM ");
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE period_start >= ");
            query.append(" :beginDate AND period_end <= ");
            query.append(" :endDate AND inform_id = :stationID");
            query.append(" AND ROUND(snow_total::numeric, 2) >= :snowTotal");
            query.append(" AND snow_total != :missing");
            query.append(" AND period_type = :periodType");
            query.append(" ORDER BY snow_total DESC");
            queryParams.put("periodType", 5);
        } else {
            query.append("date FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE date >= ");
            query.append(" :beginDate AND date <= ");
            query.append(" :endDate AND station_id = :stationID");
            query.append(" AND snow >= :snowTotal");
            query.append(" AND snow != :missing");
            query.append(" ORDER BY snow DESC");
        }

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        // round the given total precip
        queryParams.put("snowTotal", ClimateUtilities.nint(maxTotalSnow, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_SNOW);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);

            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        // date can be null
                        Object dateObj = oa[0];
                        if (dateObj != null) {
                            ClimateDate date = new ClimateDate(dateObj);
                            if (!date.isMissing()) {
                                dates.add(new ClimateDates(date,
                                        // make a copy for end date to
                                        // ensure different references
                                        new ClimateDate(date)));
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        // date could be null
                        ClimateDate date = new ClimateDate(result);
                        if (!date.isMissing()) {
                            dates.add(new ClimateDates(date,
                                    // make a copy for end date to
                                    // ensure different references
                                    new ClimateDate(date)));
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Couldn't find a total snow dates for the query: ["
                        + query + "] and map [" + queryParams.toString() + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the count of reports with trace total_snow for the given dates,
     * station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return number of reports with trace total snow, or the missing value.
     */
    private int getNumTotalSnowTrace(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElementEquality(beginDate, endDate, stationID, iType,
                "snow", "snow_total", ParameterFormatClimate.TRACE);
    }

    /**
     * Get the max of total snow for the given dates and station. Do not filter
     * by period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return max of total snow, or the missing value.
     */
    private float getMaxTotalSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationID) {
        StringBuilder query = new StringBuilder("SELECT MAX(snow_total) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(" :beginDate AND period_end <= ");
        query.append(" :endDate AND inform_id = :stationID");
        query.append(" AND snow_total != :missing AND snow_total != ");
        query.append(":trace");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        queryParams.put("missing", ParameterFormatClimate.MISSING_SNOW);
        queryParams.put("trace", ParameterFormatClimate.TRACE);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING_SNOW)).floatValue();
    }

    /**
     * Get the sum of total snow for the given dates, station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of total snow, or the missing value.
     */
    private float getSumTotalSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "snow",
                "snow_total", ClimateDAO.BuildElementType.SUM,
                ParameterFormatClimate.MISSING_SNOW, true).floatValue();
    }

    /**
     * Get the average of mean precipitation for the given dates and station by
     * averaging over the mean precip column for applicable reports, where
     * precip is not missing.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return Missing value if nothing could be found, the average of mean
     *         precip, or trace if the average is less than 0.
     */
    private float getAvgMeanPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        float result = buildElement(beginDate, endDate, stationID, iType,
                "precip", "precip_mean_day", ClimateDAO.BuildElementType.AVG,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue();

        if (result < 0) {
            return ParameterFormatClimate.TRACE;
        } else {
            return result;
        }
    }

    /**
     * Get the occurrences of the given max storm precip, rounded to 2 decimal
     * places. Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxStormPrecip
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDates> getMaxStormPrecipOccurrences(
            ClimateDate beginDate, ClimateDate endDate, int stationID,
            float maxStormPrecip, PeriodType iType)
            throws ClimateQueryException {
        List<ClimateDates> dates = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT pcp_stm_start_day1, pcp_stm_end_day1, pcp_stm_start_day2, ");
        query.append(
                " pcp_stm_end_day2, pcp_stm_start_day3, pcp_stm_end_day3 FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(" :beginDate AND period_end <= ");
        query.append(" :endDate AND inform_id = :stationID");
        query.append(" AND precip_storm_max = ");
        query.append(":precipMaxStorm AND precip_storm_max != :missing");
        Map<String, Object> queryParams = new HashMap<>();

        if (!PeriodType.OTHER.equals(iType)) {
            query.append(" AND period_type = :periodType");
            queryParams.put("periodType", 5);
        }
        query.append(" ORDER BY precip_storm_max DESC");

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        // round the value to 2 decimal places
        queryParams.put("precipMaxStorm",
                ClimateUtilities.nint(maxStormPrecip, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_PRECIP);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int baseIndex = 0; baseIndex < 6; baseIndex += 2) {
                            Object dateObj1 = oa[baseIndex];
                            Object dateObj2 = oa[baseIndex + 1];
                            // dates can be null
                            if ((dateObj1 != null) && (dateObj2 != null)) {
                                ClimateDate date1 = new ClimateDate(dateObj1);
                                ClimateDate date2 = new ClimateDate(dateObj2);
                                if (!date1.isMissing() && !date2.isMissing()) {
                                    dates.add(new ClimateDates(date1, date2));
                                }
                            }
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find a max storm precip dates for the query: ["
                                + query + "] and map [" + queryParams.toString()
                                + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the occurrences of the given max 24 hour precip, rounded to 2 decimal
     * places. Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param max24HPrecip
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDates> getMaxPrecip24HOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, float max24HPrecip,
            PeriodType iType) throws ClimateQueryException {
        List<ClimateDates> dates = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT pcp_24h_start_day1, pcp_24h_end_day1, pcp_24h_start_day2, ");
        query.append(
                " pcp_24h_end_day2, pcp_24h_start_day3, pcp_24h_end_day3 FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(" :beginDate AND period_end <= ");
        query.append(" :endDate AND inform_id = :stationID");
        query.append(" AND ROUND(precip_max_24h::numeric, 2) >= :precipMax24h");
        query.append(" AND precip_max_24h != :missing");
        Map<String, Object> queryParams = new HashMap<>();

        if (!PeriodType.OTHER.equals(iType)) {
            query.append(" AND period_type = :periodType");
            queryParams.put("periodType", 5);
        }
        query.append(" ORDER BY precip_max_24h DESC");

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        // round the value to 2 decimal places
        queryParams.put("precipMax24h", ClimateUtilities.nint(max24HPrecip, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_PRECIP);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int baseIndex = 0; baseIndex < 6; baseIndex += 2) {
                            Object dateObj1 = oa[baseIndex];
                            Object dateObj2 = oa[baseIndex + 1];
                            // dates can be null
                            if ((dateObj1 != null) && (dateObj2 != null)) {
                                ClimateDate date1 = new ClimateDate(dateObj1);
                                ClimateDate date2 = new ClimateDate(dateObj2);
                                if (!date1.isMissing() && !date2.isMissing()) {
                                    dates.add(new ClimateDates(date1, date2));
                                }
                            }
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find a max 24 hour precip dates for the query: ["
                                + query + "] and map [" + queryParams.toString()
                                + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Migrated from common functionality of build_period_obs_climo.ecpp.
     * 
     * Given the start date and hour that were provided from looping through the
     * Daily ASOS table's hourly precip column data, return a
     * {@link ClimateDates} instance over the appropriate 24-hour period.
     * 
     * @param iStartDate
     * @param iStartHour
     * @return
     */
    private static ClimateDates getPrecipDatesFromDateAndHour(
            ClimateDate iStartDate, int iStartHour) {
        ClimateDates newPrecipDates;
        if (iStartHour == 0) {
            /*
             * max found between hours 0 and 23 on first day
             */
            newPrecipDates = new ClimateDates(new ClimateDate(iStartDate),
                    new ClimateDate(iStartDate));
        } else if (iStartHour < TimeUtil.HOURS_PER_DAY) {
            /* date spanned */
            Calendar endCal = iStartDate.getCalendarFromClimateDate();
            endCal.add(Calendar.DATE, 1);
            newPrecipDates = new ClimateDates(new ClimateDate(iStartDate),
                    new ClimateDate(endCal));
        } else {
            /*
             * max found between hours 0 and 23 on second day
             */
            Calendar cal = iStartDate.getCalendarFromClimateDate();
            cal.add(Calendar.DATE, 1);
            newPrecipDates = new ClimateDates(new ClimateDate(cal),
                    new ClimateDate(cal));
        }

        /*
         * determine beginning and ending hours of the 24 hour period
         */
        int startHour = iStartHour;
        int endHour = iStartHour + TimeUtil.HOURS_PER_DAY - 1;
        if (startHour >= TimeUtil.HOURS_PER_DAY) {
            startHour -= TimeUtil.HOURS_PER_DAY;
        }
        if (endHour >= TimeUtil.HOURS_PER_DAY) {
            endHour -= TimeUtil.HOURS_PER_DAY;
        }
        newPrecipDates.setStartTime(new ClimateTime(startHour));
        newPrecipDates.setEndTime(new ClimateTime(endHour));
        return newPrecipDates;
    }

    /**
     * Get the occurrences of the given max total precip (rounded to 2
     * decimals). Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#return_element_dates.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxTotalPrecip
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDates> getMaxTotalPrecipOccurrences(
            ClimateDate beginDate, ClimateDate endDate, int stationID,
            float maxTotalPrecip) throws ClimateQueryException {
        List<ClimateDates> dates = new ArrayList<>();

        // go by period end date
        StringBuilder query = new StringBuilder("SELECT date FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= ");
        query.append(" :beginDate AND date <= ");
        query.append(" :endDate AND station_id = :stationID");
        if (maxTotalPrecip != ParameterFormatClimate.TRACE) {
            // non-trace precip
            query.append(" AND precip >= :totalPrecip");
        } else {
            // trace precip only
            query.append(" AND precip <= :totalPrecip");
        }
        query.append(" AND precip != :missing");
        query.append(" ORDER BY precip DESC");

        Map<String, Object> queryParams = new HashMap<>();

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        // round the given total precip
        queryParams.put("totalPrecip",
                ClimateUtilities.nint(maxTotalPrecip, 2));
        queryParams.put("missing", ParameterFormatClimate.MISSING_PRECIP);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        Object dateObj = oa[0];
                        // date could be null
                        if (dateObj != null) {
                            ClimateDate date = new ClimateDate(dateObj);
                            if (!date.isMissing()) {
                                dates.add(new ClimateDates(date,
                                        // make a copy for end date to
                                        // ensure different references
                                        new ClimateDate(date)));
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        // date could be null
                        ClimateDate date = new ClimateDate(result);
                        if (!date.isMissing()) {
                            dates.add(new ClimateDates(date,
                                    // make a copy for end date to
                                    // ensure different references
                                    new ClimateDate(date)));
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Couldn't find a total precip dates for the query: ["
                                + query + "] and map [" + queryParams.toString()
                                + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the count of reports with trace total_precip for the given dates,
     * station, and period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return number of reports with trace total precip, or the missing value.
     */
    private int getNumTotalPrecipTrace(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType) {
        return buildElementEquality(beginDate, endDate, stationID, iType,
                "precip", "precip_total", ParameterFormatClimate.TRACE);
    }

    /**
     * Get the max storm precipitation for the given dates, station, and period
     * type by the precip_storm_max column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max of total storm precip, or the missing value.
     */
    private float getMaxStormPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "precip",
                "precip_storm_max", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue();
    }

    /**
     * Get the max 24 hour precipitation for the given dates, station, and
     * period type by the precip_max_24h column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max of total 24 hour precip, or the missing value.
     */
    private float getMax24HPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "precip",
                "precip_max_24h", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue();
    }

    /**
     * Get the max of total precipitation for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return max of total precip, or the missing value.
     */
    private float getMaxTotalPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "precip",
                "precip_total", ClimateDAO.BuildElementType.MAX,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue();
    }

    /**
     * Get the average of total precipitation for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return average of total precip, or the missing value.
     */
    private float getAvgTotalPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "precip",
                "precip_total", ClimateDAO.BuildElementType.AVG,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue();
    }

    /**
     * Get the sum of total precipitation for the given dates, station, and
     * period type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return sum of total precip, or the missing value.
     */
    private float getSumTotalPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "precip",
                "precip_total", ClimateDAO.BuildElementType.SUM,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue();
    }

    /**
     * Get the occurrences of the given min temp, rounded to 2 decimals.
     * Rewritten from calc_period_obs.ecpp#return_element_dates and
     * #fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param minTemp
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDate> getMinTempOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, int minTemp, PeriodType iType)
            throws ClimateQueryException {
        List<ClimateDate> dates = new ArrayList<>();

        int expectedResultSize;

        StringBuilder query;
        Map<String, Object> queryParams = new HashMap<>();

        if (PeriodType.OTHER.equals(iType)) {
            // go by daily table dates
            expectedResultSize = 1;
            query = new StringBuilder("SELECT date FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE date >= ");
            query.append(" :beginDate");
            query.append(" AND date <= :endDate");
            query.append(" AND station_id = :stationID");
            query.append(" AND ROUND(min_temp, 2) <= :minTemp");
            query.append(" AND min_temp != :missing ORDER BY min_temp ASC");
        } else {
            // go by actual date DB columns
            expectedResultSize = 3;
            query = new StringBuilder(
                    "SELECT day_min_temp1, day_min_temp2, day_min_temp3 FROM ");
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE period_start >= ");
            query.append(" :beginDate");
            query.append(" AND period_end <= :endDate");
            query.append(" AND inform_id = :stationID");
            query.append(" AND ROUND(min_temp, 2) <= :minTemp");
            query.append(" AND min_temp != :missing AND period_type = ");
            query.append(":periodType ORDER BY min_temp ASC");
            queryParams.put("periodType", 5);
        }
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        queryParams.put("minTemp", minTemp);
        queryParams.put("missing", ParameterFormatClimate.MISSING);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int index = 0; index < expectedResultSize; index++) {
                            Object dateObj = oa[index];
                            // date could be null
                            if (dateObj != null) {
                                ClimateDate date = new ClimateDate(dateObj);
                                if (!date.isMissing()) {
                                    dates.add(date);
                                }
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        // date could be null
                        ClimateDate date = new ClimateDate(result);
                        if (!date.isMissing()) {
                            dates.add(date);
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Couldn't find a min temp dates for the query: ["
                        + query + "] and map [" + queryParams.toString() + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the minimum Min Temperature for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return minimum min temp, or the missing value.
     */
    public int getMinMinTemp(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "min_temp",
                "min_temp", ClimateDAO.BuildElementType.MIN).intValue();
    }

    /**
     * Get the occurrences of the given max temp, rounded to 2 decimals.
     * Rewritten from calc_period_obs.ecpp#return_element_dates and
     * #fill_period_date.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param maxTemp
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return
     * @throws ClimateQueryException
     */
    private List<ClimateDate> getMaxTempOccurrences(ClimateDate beginDate,
            ClimateDate endDate, int stationID, int maxTemp, PeriodType iType)
            throws ClimateQueryException {
        List<ClimateDate> dates = new ArrayList<>();

        int expectedResultSize;

        StringBuilder query;
        Map<String, Object> queryParams = new HashMap<>();

        if (PeriodType.OTHER.equals(iType)) {
            // go by daily table dates
            expectedResultSize = 1;
            query = new StringBuilder("SELECT date FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE date >= ");
            query.append(" :beginDate");
            query.append(" AND date <= :endDate");
            query.append(" AND station_id = :stationID");
            query.append(" AND ROUND(max_temp, 2) >= :maxTemp");
            query.append(" AND max_temp != :missing ORDER BY max_temp DESC");
        } else {
            // go by actual date DB columns in period table
            expectedResultSize = 3;
            query = new StringBuilder(
                    "SELECT day_max_temp1, day_max_temp2, day_max_temp3 FROM ");
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE period_start >= ");
            query.append(":beginDate");
            query.append(" AND period_end <= :endDate");
            query.append(" AND inform_id = :stationID");
            query.append(" AND ROUND(max_temp, 2) >= :maxTemp");
            query.append(" AND max_temp != :missing AND period_type = ");
            query.append(":periodType ORDER BY max_temp DESC");
            queryParams.put("periodType", 5);
        }
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", stationID);
        queryParams.put("maxTemp", maxTemp);
        queryParams.put("missing", ParameterFormatClimate.MISSING);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    queryParams);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        for (int index = 0; index < expectedResultSize; index++) {
                            Object dateObj = oa[index];
                            // date could be null
                            if (dateObj != null) {
                                ClimateDate date = new ClimateDate(dateObj);
                                if (!date.isMissing()) {
                                    dates.add(date);
                                }
                            }
                        }
                    } else if (result instanceof java.sql.Date) {
                        // date could be null
                        ClimateDate date = new ClimateDate(result);
                        if (!date.isMissing()) {
                            dates.add(date);
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Couldn't find a max temp dates for the query: ["
                        + query + "] and map [" + queryParams.toString() + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving  with query: [" + query
                            + "] and map [" + queryParams.toString() + "]",
                    e);
        }

        return dates;
    }

    /**
     * Get the maximum Max Temperature for the given dates, station, and period
     * type.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#build_element.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            if type other than 0, filter by type 5.
     * @return maximum max temp, or the missing value.
     */
    public int getMaxMaxTemp(ClimateDate beginDate, ClimateDate endDate,
            int stationID, PeriodType iType) {
        return buildElement(beginDate, endDate, stationID, iType, "max_temp",
                "max_temp", ClimateDAO.BuildElementType.MAX).intValue();
    }

    /********************************************************************************
     *
     * void get_monthly_ASOS ( int month, char *station_id, period_data *MSM)
     * 
     *
     * Doug Murphy PRC/TDL HP 9000/7xx October 1999
     *
     * FUNCTION DESCRIPTION ==================== This function retrieves the
     * pertinent ASOS MSM data from the cli_asos_monthly database table. It is
     * used in the monthly display_climate for the user to compare to the values
     * calculated from the daily climate database.
     *
     * NOTE: The MSM data does not utilize all entries in the period_data
     * structure. Only the structure values which are used are filled. Also, the
     * MSM has items which are not included in the period_data structure. Only
     * those items which are saved into the structure are selected from the
     * database table.
     *
     ********************************************************************************
     */
    /**
     * Retrieve monthly ASOS data, ignoring "shrtdurpcp" values.
     * 
     * @param iStationCode
     *            station code to look for.
     * @param iMonth
     *            month to look for.
     * @param iYear
     *            year to look for.
     * @return data partially filled by the monthly ASOS table, with the rest of
     *         the data missing.
     * @throws ClimateQueryException
     *             on database or casting error.
     */
    public PeriodData getMonthlyASOS(String iStationCode, int iMonth, int iYear)
            throws ClimateQueryException {
        return getPeriodFromMonthlyASOS(iStationCode,
                new ClimateDates(iMonth, iYear));
    }

    /**
     * Migrated from build_period_sum_climo.ec
     * 
     * <pre>
     * build_period_sum_climo( )
     *
     * December 1999     David T. Miller        PRC/TDL
     *
     * 
     * Purpose:   In build_period_obs_climo(), it was easy to go through the daily_climate
     *      table and find the number of days of an occurrence.  However, for
     *      seasonal and annual, can't go through the daily climate or else the
     *      values from the Monthly Summary Message would be missed.  Therefore,
     *      had to be able to sum up the monthly information.  This routine uses
     *      the dynamic SQL routine build_element to build SELECT SUM statements
     *      for each element.  As a side note, it would be much easier to loop 
     *      through these sums.  Therefore, declared an array of pointer to 
     *      have the same address as the structure variables.  Then was able
     *      to loop through the different sums.
     * 
     * </pre>
     * 
     * Get and sum period data for the given date range and period type, setting
     * values in the given {@link PeriodData} object. This method should only be
     * used for monthly, seasonal, and annual values.
     * 
     * @param beginDate
     *            begin date to look for.
     * @param endDate
     *            end date to look for.
     * @param periodData
     *            object to assign values to. Needs station ID set in order to
     *            query DB.
     * @param iPeriodType
     *            period type to look for.
     */
    public void buildPeriodSumClimo(ClimateDate beginDate, ClimateDate endDate,
            PeriodData periodData, PeriodType iPeriodType, boolean reportSnow) {
        int informID = periodData.getInformId();

        periodData.setNumMaxGreaterThan90F(
                sumNumMaxTempGreater90F(beginDate, endDate, informID));

        periodData.setNumMaxLessThan32F(
                (sumNumMaxTempLess32F(beginDate, endDate, informID)));

        periodData.setNumMaxGreaterThanT1F(
                sumNumMaxTempGreaterT1F(beginDate, endDate, informID));

        periodData.setNumMaxGreaterThanT2F(
                sumNumMaxTempGreaterT2F(beginDate, endDate, informID));

        periodData.setNumMaxLessThanT3F(
                sumNumMaxTempLessT3F(beginDate, endDate, informID));

        periodData.setNumMinLessThan32F(
                sumNumMinTempLess32F(beginDate, endDate, informID));

        periodData.setNumMinLessThan0F(
                sumNumMinTempLess0F(beginDate, endDate, informID));

        periodData.setNumMinGreaterThanT4F(
                sumNumMinTempGreaterT4F(beginDate, endDate, informID));

        periodData.setNumMinLessThanT5F(
                sumNumMinTempLessT5F(beginDate, endDate, informID));

        periodData.setNumMinLessThanT6F(
                sumNumMinTempLessT6F(beginDate, endDate, informID));

        periodData.setNumPrcpGreaterThan01(
                sumNumPrecipGreater01(beginDate, endDate, informID));

        periodData.setNumPrcpGreaterThan10(
                sumNumPrecipGreater10(beginDate, endDate, informID));

        periodData.setNumPrcpGreaterThan50(
                sumNumPrecipGreater50(beginDate, endDate, informID));

        periodData.setNumPrcpGreaterThan100(
                sumNumPrecipGreater100(beginDate, endDate, informID));

        periodData.setNumPrcpGreaterThanP1(
                sumNumPrecipGreaterP1(beginDate, endDate, informID));

        periodData.setNumPrcpGreaterThanP2(
                sumNumPrecipGreaterP2(beginDate, endDate, informID));

        if (reportSnow) {
            periodData.setNumSnowGreaterThanTR(
                    sumNumSnowGreaterTR(beginDate, endDate, informID));

            periodData.setNumSnowGreaterThan1(
                    sumNumSnowGreater1(beginDate, endDate, informID));

            periodData.setNumSnowGreaterThanS1(
                    sumNumSnowGreaterS1(beginDate, endDate, informID));
        }

        periodData.setNumFairDays(sumNumFairDays(beginDate, endDate, informID));

        periodData.setNumPartlyCloudyDays(
                sumNumPartlyCloudyDays(beginDate, endDate, informID));

        periodData.setNumMostlyCloudyDays(
                sumNumMostlyCloudyDays(beginDate, endDate, informID));
    }

    /**
     * Get the average of all min temp mean using DB calls.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return average value, or missing value
     */
    private float avgMinTempMean(ClimateDate beginDate, ClimateDate endDate,
            PeriodType iPeriodType, int informID) {
        return buildElement(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp_mean", ClimateDAO.BuildElementType.AVG)
                        .floatValue();
    }

    /**
     * Get the average of all min temps using DB calls.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return average value, or missing value
     */
    private float avgMinTemp(ClimateDate beginDate, ClimateDate endDate,
            PeriodType iPeriodType, int informID) {
        return buildElement(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp", ClimateDAO.BuildElementType.AVG)
                        .floatValue();
    }

    /**
     * Get the average of all max temp mean using DB calls.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return average value, or missing value
     */
    private float avgMaxTempMean(ClimateDate beginDate, ClimateDate endDate,
            PeriodType iPeriodType, int informID) {
        return buildElement(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp_mean", ClimateDAO.BuildElementType.AVG)
                        .floatValue();
    }

    /**
     * Get the average of all max temps using DB calls.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return average value, or missing value
     */
    private float avgMaxTemp(ClimateDate beginDate, ClimateDate endDate,
            PeriodType iPeriodType, int informID) {
        return buildElement(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp", ClimateDAO.BuildElementType.AVG)
                        .floatValue();
    }

    /**
     * Get the sum of all mostly cloudy days.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMostlyCloudyDays(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder("SELECT SUM(num_mc) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= :startDate");
        query.append(" AND period_end <= :endDate");
        query.append(" AND inform_id = :informID");
        query.append(" AND num_mc != ").append(ParameterFormatClimate.MISSING);

        Map<String, Object> paramMap = new HashMap<>();

        query.append(" AND period_type = :periodType");
        paramMap.put("periodType", 5);

        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("informID", informID);

        return ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all partly cloudy days.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPartlyCloudyDays(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder("SELECT SUM(num_pc) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= :startDate");
        query.append(" AND period_end <= :endDate");
        query.append(" AND inform_id = :informID");
        query.append(" AND num_pc != ").append(ParameterFormatClimate.MISSING);

        Map<String, Object> paramMap = new HashMap<>();

        query.append(" AND period_type = :periodType");
        paramMap.put("periodType", 5);

        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("informID", informID);

        return ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all fair days.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumFairDays(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder("SELECT SUM(num_fair) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= :startDate");
        query.append(" AND period_end <= :endDate");
        query.append(" AND inform_id = :informID");
        query.append(" AND num_fair != ")
                .append(ParameterFormatClimate.MISSING);

        Map<String, Object> paramMap = new HashMap<>();

        query.append(" AND period_type = :periodType");
        paramMap.put("periodType", 5);

        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("informID", informID);

        return ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the count of all days with snow at least the custom snow value,
     * examining the snow total column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values
     * @return sum value, or missing value
     */
    private int sumReportSnowGreaterS1(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        float s1 = global.getS1();

        if (s1 == ParameterFormatClimate.MISSING_SNOW) {
            return ParameterFormatClimate.MISSING;
        }

        if (s1 < -0.05) {
            return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                    "snow", "snow_total", ParameterFormatClimate.MISSING_SNOW,
                    0.05, true, true)
                    + daysPastThresh(beginDate, endDate, informID, iPeriodType,
                            "snow", "snow_total",
                            ParameterFormatClimate.MISSING_SNOW, -0.05, false,
                            true);
        } else {
            return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                    "snow", "snow_total", ParameterFormatClimate.MISSING_SNOW,
                    s1, true, true);
        }
    }

    /**
     * Get the sum of all days with snow at least the custom snow value,
     * directly calling the num_snow_ge_s1 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumSnowGreaterS1(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_snow_ge_s1) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_snow_ge_s1 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the count of all days with snow at least 1 inch, examining the snow
     * total column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportSnowGreater1(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return daysPastThresh(beginDate, endDate, informID, iPeriodType, "snow",
                "snow_total", ParameterFormatClimate.MISSING_SNOW, 1, true,
                true);
    }

    /**
     * Get the sum of all days with snow at least 1 inch, directly calling the
     * num_snow_ge_1 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumSnowGreater1(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_snow_ge_1) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_snow_ge_1 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the count of all days with snow at trace or less, or 0.001 or more,
     * examining the snow total column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportSnowGreaterTR(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return daysPastThresh(beginDate, endDate, informID, iPeriodType, "snow",
                "snow_total", ParameterFormatClimate.MISSING_SNOW,
                ParameterFormatClimate.TRACE, false, true)
                + daysPastThresh(beginDate, endDate, informID, iPeriodType,
                        "snow", "snow_total",
                        ParameterFormatClimate.MISSING_SNOW, 0.001, true, true);
    }

    /**
     * Get the sum of all days with snow at least trace, directly calling the
     * num_snow_ge_tr column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumSnowGreaterTR(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_snow_ge_tr) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_snow_ge_tr != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least the second custom inches,
     * directly calling the num_prcp_ge_p2 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPrecipGreaterP2(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_prcp_ge_p2) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_prcp_ge_p2 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least the second custom inches,
     * examining the total_precip column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values.
     * @return sum value, or missing value
     */
    private int sumReportPrecipGreaterP2(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        float p2 = global.getP2();

        return sumReportPrecipGreater(beginDate, endDate, iPeriodType, informID,
                p2);
    }

    /**
     * Get the sum of all days with precip at least the first custom inches,
     * directly calling the num_prcp_ge_p1 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPrecipGreaterP1(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_prcp_ge_p1) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_prcp_ge_p1 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least the first custom inches,
     * examining the total_precip column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values.
     * @return sum value, or missing value
     */
    private int sumReportPrecipGreaterP1(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        float p1 = global.getP1();

        return sumReportPrecipGreater(beginDate, endDate, iPeriodType, informID,
                p1);
    }

    /**
     * Get the sum of all days with precip at least some inches value, examining
     * the total_precip column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     * @param endDate
     * @param iPeriodType
     * @param informID
     * @param p
     *            custom inches value.
     * @return
     */
    private int sumReportPrecipGreater(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            float p) {
        if (p == ParameterFormatClimate.MISSING_PRECIP) {
            return ParameterFormatClimate.MISSING;
        }

        // Trace values represent a special case, because value is stored in
        // database as -1.
        if (p < -0.005) {
            return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                    "precip", "precip_total",
                    ParameterFormatClimate.MISSING_PRECIP, 0.005, true, true)
                    + daysPastThresh(beginDate, endDate, informID, iPeriodType,
                            "precip", "precip_total",
                            ParameterFormatClimate.MISSING_PRECIP, -0.005,
                            false, true);
        } else {
            return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                    "precip", "precip_total",
                    ParameterFormatClimate.MISSING_PRECIP, p, true, true);
        }
    }

    /**
     * Get the sum of all days with precip at least 1.0 inches, directly calling
     * the num_prcp_ge_100 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPrecipGreater100(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_prcp_ge_100) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_prcp_ge_100 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least 1.0 inches, examining the
     * total_precip column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportPrecipGreater100(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return sumReportPrecipGreater(beginDate, endDate, iPeriodType, informID,
                1);
    }

    /**
     * Get the sum of all days with precip at least 0.5 inches, directly calling
     * the num_prcp_ge_50 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPrecipGreater50(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_prcp_ge_50) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_prcp_ge_50 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least 0.5 inches, examining the
     * total_precip column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportPrecipGreater50(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return sumReportPrecipGreater(beginDate, endDate, iPeriodType, informID,
                0.5f);
    }

    /**
     * Get the sum of all days with precip at least 0.1 inches, directly calling
     * the num_prcp_ge_10 column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPrecipGreater10(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_prcp_ge_10) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_prcp_ge_10 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least 0.1 inches, examining the
     * precip_total column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportPrecipGreater10(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return sumReportPrecipGreater(beginDate, endDate, iPeriodType, informID,
                0.1f);
    }

    /**
     * Get the sum of all days with precip at least 0.01 inches, examining the
     * precip_total column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumPrecipGreater01(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_prcp_ge_01) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_prcp_ge_01 != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with precip at least 0.01 inches, directly
     * calling the num_prcp_ge_01 column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportPrecipGreater01(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return sumReportPrecipGreater(beginDate, endDate, iPeriodType, informID,
                0.01f);
    }

    /**
     * Get the sum of all days with min temp less than sixth custom temp,
     * directly calling the num_min_le_t6f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMinTempLessT6F(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_min_le_t6f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_min_le_t6f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with min temp less than sixth custom temp,
     * examining the min_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values
     * @return sum value, or missing value
     */
    private int sumReportMinTempLessT6F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        int t6 = global.getT6();

        if (t6 == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        }

        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp", t6, false);
    }

    /**
     * Get the sum of all days with min temp less than fifth custom temp,
     * directly calling the num_min_le_t5f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMinTempLessT5F(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_min_le_t5f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_min_le_t5f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with min temp less than fifth custom temp,
     * examining the min_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values
     * @return sum value, or missing value
     */
    private int sumReportMinTempLessT5F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        int t5 = global.getT5();

        if (t5 == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        }

        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp", t5, false);
    }

    /**
     * Get the sum of all days with min temp greater than fourth custom temp,
     * directly calling the num_min_ge_t4f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMinTempGreaterT4F(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_min_ge_t4f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_min_ge_t4f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with min temp greater than fourth custom temp,
     * examining the min_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values
     * @return sum value, or missing value
     */
    private int sumReportMinTempGreaterT4F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        int t4 = global.getT4();

        if (t4 == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        }

        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp", t4, true);
    }

    /**
     * Get the sum of all days with min temp less than 0F, directly calling the
     * num_min_le_0f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMinTempLess0F(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_min_le_0f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_min_le_0f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all reports with min temp less than 0F, examining the
     * min_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportMinTempLess0F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp", 0, false);
    }

    /**
     * Get the sum of all days with min temp less than 32F, directly calling the
     * num_min_le_32f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMinTempLess32F(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_min_le_32f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_min_le_32f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with min temp less than 32F, examining the
     * min_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportMinTempLess32F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "min_temp", "min_temp", 32, false);
    }

    /**
     * Get the sum of all days with max temp less than third custom temp,
     * directly calling the num_max_le_t3f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMaxTempLessT3F(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_max_le_t3f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_max_le_t3f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with max temp less than third custom temp,
     * examining the max_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values.
     * @return sum value, or missing value
     */
    private int sumReportMaxTempLessT3F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        int t3 = global.getT3();

        if (t3 == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        }

        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp", t3, false);
    }

    /**
     * Get the sum of all days with max temp greater than second custom temp,
     * directly calling the num_max_ge_t2f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMaxTempGreaterT2F(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_max_ge_t2f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_max_ge_t2f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with max temp greater than second custom temp,
     * examining the max_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values.
     * @return sum value, or missing value
     */
    private int sumReportMaxTempGreaterT2F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        int t2 = global.getT2();

        if (t2 == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        }

        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp", t2, true);
    }

    /**
     * Get the sum of all days with max temp greater than first custom temp,
     * directly calling the num_max_ge_t1f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMaxTempGreaterT1F(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_max_ge_t1f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_max_ge_t1f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with max temp greater than first custom temp,
     * examining the max_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @param global
     *            global values.
     * @return sum value, or missing value
     */
    private int sumReportMaxTempGreaterT1F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID,
            ClimateGlobal global) {
        if (global == null) {
            return ParameterFormatClimate.MISSING;
        }

        int t1 = global.getT1();

        if (t1 == ParameterFormatClimate.MISSING) {
            return ParameterFormatClimate.MISSING;
        }

        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp", t1, true);
    }

    /**
     * Get the sum of all days with max temp less than 32F, directly calling the
     * num_max_le_32f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMaxTempLess32F(ClimateDate beginDate, ClimateDate endDate,
            int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_max_le_32f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_max_le_32f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with max temp less than 32F, examining the
     * max_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportMaxTempLess32F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp", 32, false);
    }

    /**
     * Get the sum of all days with max temp greater than 90F, directly calling
     * the num_max_ge_90f column.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumNumMaxTempGreater90F(ClimateDate beginDate,
            ClimateDate endDate, int informID) {
        StringBuilder query = new StringBuilder(
                "SELECT SUM(num_max_ge_90f) FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE period_start >= ");
        query.append(":beginDate AND period_end <= ");
        query.append(":endDate AND inform_id = :stationID");
        query.append(" AND num_max_ge_90f != ")
                .append(ParameterFormatClimate.MISSING);
        Map<String, Object> queryParams = new HashMap<>();

        query.append(" AND period_type = :periodType");
        queryParams.put("periodType", 5);

        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("stationID", informID);

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Get the sum of all days with max temp greater than 90F, examining the
     * max_temp column.
     * 
     * Rewritten from build_period_obs_climo.ecpp and
     * calc_period_obs.ecpp#days_past_thresh.
     * 
     * @param beginDate
     *            begin date
     * @param endDate
     *            end date
     * @param iPeriodType
     *            period type. if type other than 0, filter by type 5.
     * @param informID
     *            station ID
     * @return sum value, or missing value
     */
    private int sumReportMaxTempGreater90F(ClimateDate beginDate,
            ClimateDate endDate, PeriodType iPeriodType, int informID) {
        return daysPastThresh(beginDate, endDate, informID, iPeriodType,
                "max_temp", "max_temp", 90, true);
    }

    /**
     * Update the period data for the given station, dates, and period type.
     * 
     * Migrated from update_period_db.ec.
     * 
     * <pre>
     * void update_period_db (    int         itype,
     *              climate_date        *begin,
     *              climate_date        *end,
     *              period_data         *data,
     *              period_data_method      *qc
     *                )
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  November 1999
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function updates/inserts into the monthly climate data base.
     *
     *
     * Modification Log
     * Name                     Date         Change
     * David T. Miller          July 2000    Wind speed datatypes changed from int to
     *                                       to float
     * Doug Murphy              2/12/01      Avg max, avg min, and mean temps changed
     *                                       from int to float  
     * Bob Morris              03/25/03      Fix args to rsetnull, use symbolic names
     *                                       for ESQL-C data types, instead of hard-
     *                                       coded ints for SQL data types. OB2
     * Bob Morris              04/21/03      Change how tests for row existence and
     *                                       log messages for same are done. OB2
     * Gary Battel/Manan Dalal  1/19/05      Conversion from INFORMIX to POSTGRES
     * </pre>
     * 
     * @param iStationID
     * @param iDates
     * @param iPeriodType
     * @param iData
     * @return
     * @throws Exception
     */

    public boolean updatePeriodData(int iStationID, ClimateDates iDates,
            PeriodType iPeriodType, PeriodData iData) throws Exception {

        StringBuilder selectQuery = new StringBuilder("SELECT * FROM ");
        selectQuery.append(
                ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        // conditions
        selectQuery.append(" WHERE inform_id= :iStationID")
                .append(" AND period_type= :type");
        selectQuery.append(" AND period_start= :startDate");
        selectQuery.append(" AND period_end= :endDate");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("iStationID", iStationID);
        paramMap.put("type", iPeriodType.getValue());
        paramMap.put("startDate",
                iDates.getStart().getCalendarFromClimateDate());
        paramMap.put("endDate", iDates.getEnd().getCalendarFromClimateDate());

        Object[] results;
        try {
            results = getDao().executeSQLQuery(selectQuery.toString(),
                    paramMap);
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + selectQuery
                    + "] and map: [" + paramMap + "]", e);
        }

        if ((results != null) && (results.length >= 1)) {

            // data exists for station at given dates and period type, update
            updatePeriodDataSupport(iStationID, iDates, iPeriodType, iData);
        } else {
            // data does not exist for station at given dates and period type,
            // insert
            insertPeriodDataSupport(iStationID, iDates, iPeriodType, iData);
        }
        return true;
    }

    /**
     * The query to insert period data.
     * 
     * Because of the variability of availability in list data, it is much
     * easier to just insert ID, type, and dates, then use an update query.
     * 
     * @param iStationID
     * @param iDates
     * @param iPeriodType
     * @param iData
     * @throws ClimateQueryException
     */
    private void insertPeriodDataSupport(int iStationID, ClimateDates iDates,
            PeriodType iPeriodType, PeriodData iData)
            throws ClimateQueryException {

        Map<String, Object> paramMap = new HashMap<>();

        StringBuilder insert = new StringBuilder("INSERT INTO ");
        insert.append(
                ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        insert.append("(inform_id, ");
        insert.append(" period_type, ");
        insert.append(" period_start, ");
        insert.append(" period_end)");
        insert.append(" VALUES (:iStationID");
        paramMap.put("iStationID", iStationID);
        insert.append(", :type");
        paramMap.put("type", iPeriodType.getValue());
        insert.append(", :start");
        paramMap.put("start", iDates.getStart().getCalendarFromClimateDate());
        insert.append(", :end");
        paramMap.put("end", iDates.getEnd().getCalendarFromClimateDate());
        insert.append(")");

        try {
            getDao().executeSQLUpdate(insert.toString(), paramMap);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: [" + insert
                            + "] and map: [" + paramMap + "]",
                    e);
        }

        updatePeriodDataSupport(iStationID, iDates, iPeriodType, iData);
    }

    /**
     * The query to update period data.
     * 
     * @param iStationID
     * @param iDates
     * @param iPeriodType
     * @param iData
     * @throws ClimateQueryException
     */
    private void updatePeriodDataSupport(int iStationID, ClimateDates iDates,
            PeriodType iPeriodType, PeriodData iData)
            throws ClimateQueryException {
        Map<String, Object> paramMap = new HashMap<>();

        StringBuilder update = new StringBuilder("UPDATE ");
        update.append(
                ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        update.append(" SET ");
        update.append(" max_temp= :max_temp,");
        paramMap.put("max_temp", iData.getMaxTemp());
        update.append(createSetDayClauseForList("day_max_temp",
                iData.getDayMaxTempList().toArray(
                        new ClimateDate[iData.getDayMaxTempList().size()]),
                3, paramMap));
        update.append(" max_temp_mean= :max_temp_mean");
        paramMap.put("max_temp_mean", iData.getMaxTempMean());
        update.append(", mean_temp= :mean_temp");
        paramMap.put("mean_temp", iData.getMeanTemp());
        update.append(", min_temp= :min_temp");
        paramMap.put("min_temp", iData.getMinTemp());
        update.append(", ");
        update.append(createSetDayClauseForList("day_min_temp",
                iData.getDayMinTempList().toArray(
                        new ClimateDate[iData.getDayMinTempList().size()]),
                3, paramMap));
        update.append(" min_temp_mean= :min_temp_mean");
        paramMap.put("min_temp_mean", iData.getMinTempMean());
        update.append(", num_max_ge_90f= :num_max_ge_90f");
        paramMap.put("num_max_ge_90f", iData.getNumMaxGreaterThan90F());
        update.append(", num_max_le_32f= :num_max_le_32f");
        paramMap.put("num_max_le_32f", iData.getNumMaxLessThan32F());

        update.append(", num_max_ge_t1f= :num_max_ge_t1f");
        paramMap.put("num_max_ge_t1f", iData.getNumMaxGreaterThanT1F());
        update.append(", num_max_ge_t2f= :num_max_ge_t2f");
        paramMap.put("num_max_ge_t2f", iData.getNumMaxGreaterThanT2F());
        update.append(", num_max_le_t3f= :num_max_le_t3f");
        paramMap.put("num_max_le_t3f", iData.getNumMaxLessThanT3F());

        update.append(", num_min_le_32f= :num_min_le_32f");
        paramMap.put("num_min_le_32f", iData.getNumMinLessThan32F());
        update.append(", num_min_le_0f= :num_min_le_0f");
        paramMap.put("num_min_le_0f", iData.getNumMinLessThan0F());

        update.append(", num_min_ge_t4f= :num_min_ge_t4f");
        paramMap.put("num_min_ge_t4f", iData.getNumMinGreaterThanT4F());
        update.append(", num_min_le_t5f= :num_min_le_t5f");
        paramMap.put("num_min_le_t5f", iData.getNumMinLessThanT5F());
        update.append(", num_min_le_t6f= :num_min_le_t6f");
        paramMap.put("num_min_le_t6f", iData.getNumMinLessThanT6F());
        update.append(", precip_total= :precip_total");
        paramMap.put("precip_total", iData.getPrecipTotal());
        update.append(", precip_max_24h= :precip_max_24h");
        paramMap.put("precip_max_24h", iData.getPrecipMax24H());
        update.append(", ");

        update.append(createSetDayClauseForList("pcp_24h_start_day",
                "pcp_24h_end_day",
                iData.getPrecip24HDates().toArray(
                        new ClimateDates[iData.getPrecip24HDates().size()]),
                3, true));
        update.append(" precip_storm_max= :precip_storm_max");
        paramMap.put("precip_storm_max", iData.getPrecipStormMax());
        update.append(", ");
        update.append(createSetDayClauseForList("pcp_stm_start_day",
                "pcp_stm_end_day",
                iData.getPrecipStormList().toArray(
                        new ClimateDates[iData.getPrecipStormList().size()]),
                3, true));
        update.append(" precip_mean_day= :precip_mean_day");
        paramMap.put("precip_mean_day", iData.getPrecipMeanDay());
        update.append(", num_prcp_ge_01= :num_prcp_ge_01");
        paramMap.put("num_prcp_ge_01", iData.getNumPrcpGreaterThan01());
        update.append(", num_prcp_ge_10= :num_prcp_ge_10");
        paramMap.put("num_prcp_ge_10", iData.getNumPrcpGreaterThan10());
        update.append(", num_prcp_ge_50= :num_prcp_ge_50");
        paramMap.put("num_prcp_ge_50", iData.getNumPrcpGreaterThan50());
        update.append(", num_prcp_ge_100= :num_prcp_ge_100");
        paramMap.put("num_prcp_ge_100", iData.getNumPrcpGreaterThan100());

        update.append(", num_prcp_ge_p1= :num_prcp_ge_p1");
        paramMap.put("num_prcp_ge_p1", iData.getNumPrcpGreaterThanP1());
        update.append(", num_prcp_ge_p2= :num_prcp_ge_p2");
        paramMap.put("num_prcp_ge_p2", iData.getNumPrcpGreaterThanP2());

        update.append(", snow_total= :snow_total");
        paramMap.put("snow_total", iData.getSnowTotal());
        update.append(", snow_max_24h= :snow_max_24h");
        paramMap.put("snow_max_24h", iData.getSnowMax24H());
        update.append(", ");
        update.append(createSetDayClauseForList("sno_24h_start_day",
                "sno_24h_end_day",
                iData.getSnow24HDates().toArray(
                        new ClimateDates[iData.getSnow24HDates().size()]),
                3, false));
        update.append(" snow_max_storm= :snow_max_storm");
        paramMap.put("snow_max_storm", iData.getSnowMaxStorm());
        update.append(", ");
        update.append(createSetDayClauseForList("sno_stm_start_day",
                "sno_stm_end_day",
                iData.getSnowStormList().toArray(
                        new ClimateDates[iData.getSnowStormList().size()]),
                3, true));
        update.append(" snow_water= :snow_water");
        paramMap.put("snow_water", iData.getSnowWater());
        update.append(", snow_ground_mean= :snow_ground_mean");
        paramMap.put("snow_ground_mean", iData.getSnowGroundMean());
        update.append(", snow_ground_max= :snow_ground_max");
        paramMap.put("snow_ground_max", iData.getSnowGroundMax());
        update.append(", ");
        update.append(createSetDayClauseForList("snow_ground_date",
                iData.getSnowGroundMaxDateList()
                        .toArray(new ClimateDate[iData
                                .getSnowGroundMaxDateList().size()]),
                3, paramMap));
        update.append(" num_snow_ge_tr= :num_snow_ge_tr");
        paramMap.put("num_snow_ge_tr", iData.getNumSnowGreaterThanTR());
        update.append(", num_snow_ge_1= :num_snow_ge_1");
        paramMap.put("num_snow_ge_1", iData.getNumSnowGreaterThan1());
        update.append(", num_snow_ge_s1= :num_snow_ge_s1");
        paramMap.put("num_snow_ge_s1", iData.getNumSnowGreaterThanS1());
        update.append(", num_heat_total= :num_heat_total");
        paramMap.put("num_heat_total", iData.getNumHeatTotal());
        update.append(", num_cool_total= :num_cool_total");
        paramMap.put("num_cool_total", iData.getNumCoolTotal());
        update.append(", avg_wind_spd= :avg_wind_spd");
        paramMap.put("avg_wind_spd", iData.getAvgWindSpd());
        update.append(", result_wind_dir= :result_wind_dir");
        paramMap.put("result_wind_dir", iData.getResultWind().getDir());
        update.append(", result_wind_spd= :result_wind_spd");
        paramMap.put("result_wind_spd", iData.getResultWind().getSpeed());
        update.append(", ");
        update.append(createSetWindClause("max_wind",
                iData.getMaxWindList().toArray(
                        new ClimateWind[iData.getMaxWindList().size()]),
                iData.getMaxWindDayList().toArray(
                        new ClimateDate[iData.getMaxWindDayList().size()]),
                3, paramMap));
        update.append(" ");
        update.append(createSetWindClause("max_gust", iData.getMaxGustList()
                .toArray(new ClimateWind[iData.getMaxGustList().size()]),

                iData.getMaxGustDayList().toArray(
                        new ClimateDate[iData.getMaxGustDayList().size()]),
                3, paramMap));
        update.append(" poss_sun= :poss_sun");
        paramMap.put("poss_sun", iData.getPossSun());
        update.append(", mean_sky_cover= :mean_sky_cover");
        paramMap.put("mean_sky_cover", iData.getMeanSkyCover());
        update.append(", num_fair= :num_fair");
        paramMap.put("num_fair", iData.getNumFairDays());
        update.append(", num_pc= :num_pc");
        paramMap.put("num_pc", iData.getNumPartlyCloudyDays());
        update.append(", num_mc= :num_mc");
        paramMap.put("num_mc", iData.getNumMostlyCloudyDays());
        update.append(", num_t= :num_t");
        paramMap.put("num_t", iData.getNumThunderStorms());
        update.append(", num_p= :num_p");
        paramMap.put("num_p", iData.getNumMixedPrecip());
        update.append(", num_rrr= :num_rrr");
        paramMap.put("num_rrr", iData.getNumHeavyRain());
        update.append(", num_rr= :num_rr");
        paramMap.put("num_rr", iData.getNumRain());
        update.append(", num_r= :num_r");
        paramMap.put("num_r", iData.getNumLightRain());
        update.append(", num_zrr= :num_zrr");
        paramMap.put("num_zrr", iData.getNumFreezingRain());
        update.append(", num_zr= :num_zr");
        paramMap.put("num_zr", iData.getNumLightFreezingRain());
        update.append(", num_a= :num_a");
        paramMap.put("num_a", iData.getNumHail());
        update.append(", num_sss= :num_sss");
        paramMap.put("num_sss", iData.getNumHeavySnow());
        update.append(", num_ss= :num_ss");
        paramMap.put("num_ss", iData.getNumSnow());
        update.append(", num_s= :num_s");
        paramMap.put("num_s", iData.getNumLightSnow());
        update.append(", num_ip= :num_ip");
        paramMap.put("num_ip", iData.getNumIcePellets());
        update.append(", num_f= :num_f");
        paramMap.put("num_f", iData.getNumFog());
        update.append(", num_fquarter= :num_fquarter");
        paramMap.put("num_fquarter", iData.getNumFogQuarterSM());
        update.append(", num_h= :num_h");
        paramMap.put("num_h", iData.getNumHaze());
        update.append(", mean_rh= :mean_rh");
        paramMap.put("mean_rh", iData.getMeanRh());
        update.append(", max_temp_qc= :max_temp_qc");
        paramMap.put("max_temp_qc", iData.getDataMethods().getMaxTempQc());
        update.append(", avg_max_temp_qc= :avg_max_temp_qc");
        paramMap.put("avg_max_temp_qc",
                iData.getDataMethods().getAvgMaxTempQc());
        update.append(", mean_temp_qc= :mean_temp_qc");
        paramMap.put("mean_temp_qc", iData.getDataMethods().getMeanTempQc());
        update.append(", min_temp_qc= :min_temp_qc");
        paramMap.put("min_temp_qc", iData.getDataMethods().getMinTempQc());
        update.append(", avg_min_temp_qc= :avg_min_temp_qc");
        paramMap.put("avg_min_temp_qc",
                iData.getDataMethods().getAvgMinTempQc());
        update.append(", max_temp_ge90_qc= :max_temp_ge90_qc");
        paramMap.put("max_temp_ge90_qc",
                iData.getDataMethods().getMaxTempGE90Qc());
        update.append(", max_temp_le32_qc= :max_temp_le32_qc");
        paramMap.put("max_temp_le32_qc",
                iData.getDataMethods().getMaxTempLE32Qc());
        update.append(", min_temp_le32_qc= :min_temp_le32_qc");
        paramMap.put("min_temp_le32_qc", iData.getDataMethods().getMinLE32Qc());
        update.append(", min_temp_le0_qc= :min_temp_le0_qc");
        paramMap.put("min_temp_le0_qc", iData.getDataMethods().getMinLE0Qc());
        update.append(", precip_max_24h_qc= :precip_max_24h_qc");
        paramMap.put("precip_max_24h_qc",
                iData.getDataMethods().getPrecip24hrMaxQc());
        update.append(", precip_ge01_qc= :precip_ge01_qc");
        paramMap.put("precip_ge01_qc",
                iData.getDataMethods().getPrecipGE01Qc());
        update.append(", precip_ge10_qc= :precip_ge10_qc");
        paramMap.put("precip_ge10_qc",
                iData.getDataMethods().getPrecipGE10Qc());
        update.append(", precip_ge50_qc= :precip_ge50_qc");
        paramMap.put("precip_ge50_qc",
                iData.getDataMethods().getPrecipGE50Qc());
        update.append(", precip_ge100_qc= :precip_ge100_qc");
        paramMap.put("precip_ge100_qc",
                iData.getDataMethods().getPrecipGE100Qc());
        update.append(", snow_max_24h_qc= :snow_max_24h_qc");
        paramMap.put("snow_max_24h_qc",
                iData.getDataMethods().getSnow24hrMaxQc());
        update.append(", max_depth_qc= :max_depth_qc");
        paramMap.put("max_depth_qc", iData.getDataMethods().getMaxDepthQc());
        update.append(", heat_qc= :heat_qc");
        paramMap.put("heat_qc", iData.getDataMethods().getHeatQc());
        update.append(", cool_qc= :cool_qc");
        paramMap.put("cool_qc", iData.getDataMethods().getCoolQc());
        update.append(", poss_sun_qc= :poss_sun_qc");
        paramMap.put("poss_sun_qc", iData.getDataMethods().getPossSunQc());
        update.append(", fair_days_qc= :fair_days_qc");
        paramMap.put("fair_days_qc", iData.getDataMethods().getFairDaysQc());
        update.append(", pc_days_qc= :pc_days_qc");
        paramMap.put("pc_days_qc", iData.getDataMethods().getPcDaysQc());
        update.append(", mc_days_qc= :mc_days_qc");
        paramMap.put("mc_days_qc", iData.getDataMethods().getCloudyDaysQc());
        update.append(" WHERE inform_id= :inform_id");
        paramMap.put("inform_id", iStationID);
        update.append(" AND period_type= :period_type");
        paramMap.put("period_type", iPeriodType.getValue());
        update.append(" AND period_start = :period_start");
        paramMap.put("period_start",
                iDates.getStart().getCalendarFromClimateDate());
        update.append(" AND period_end = :period_end");
        paramMap.put("period_end",
                iDates.getEnd().getCalendarFromClimateDate());

        try {
            getDao().executeSQLUpdate(update.toString(), paramMap);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: [" + update
                            + "] and map: [" + paramMap + "]",
                    e);
        }
    }

    /**
     * Create a set clause for wind/gust using the given column name base, which
     * follows natural numbering naming conventions and wind naming conventions
     * as appropriate, setting values using the winds and dates given, up to the
     * smaller of the size of the list of dates and winds and the limit given.
     * 
     * Return string will either be empty, or will begin with a space (but not
     * it's own starting comma), and end in a comma (but not a space).
     * 
     * @param iColumnNameBase
     * @param iWinds
     * @param iDates
     * @param iLimit
     * @param queryParams
     *            map to put query parameters in.
     * @return
     */
    private static String createSetWindClause(String iColumnNameBase,
            ClimateWind[] iWinds, ClimateDate[] iDates, int iLimit,
            Map<String, Object> queryParams) {
        StringBuilder sb = new StringBuilder();

        if (iWinds.length != 0) {
            String colSpeedName = iColumnNameBase + "_spd";

            sb.append(colSpeedName);
            sb.append("= :");
            sb.append(colSpeedName);
            sb.append(",");

            queryParams.put(colSpeedName, iWinds[0].getSpeed());

            // direction
            for (int i = 0; i < iWinds.length && i < iLimit; i++) {
                String numberedColDirectionName = iColumnNameBase + "_dir"
                        + (i + 1);

                sb.append(" ");
                sb.append(numberedColDirectionName);
                sb.append("= :");
                sb.append(numberedColDirectionName);
                sb.append(",");

                queryParams.put(numberedColDirectionName, iWinds[i].getDir());
            }

            // dates
            sb.append(createSetDayClauseForList(iColumnNameBase + "_date",
                    iDates, iLimit, queryParams));
        }

        return sb.toString();
    }

    /**
     * Create a set clause for the given column name base, which follows natural
     * numbering naming conventions, setting values using the given dates, up to
     * the smaller of the size of the list of dates and the limit given.
     * 
     * Return string will either be empty, or will begin with a space (but not
     * it's own starting comma), and end in a comma (but not a space).
     * 
     * @param iColumnNameBase
     * @param iDates
     * @param iLimit
     * @param queryParams
     *            map to put query parameters in.
     * @return
     */
    private static String createSetDayClauseForList(String iColumnNameBase,
            ClimateDate[] iDates, int iLimit, Map<String, Object> queryParams) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < iDates.length && i < iLimit; i++) {
            if (!iDates[i].isPartialMissing()) {
                String numberedColName = iColumnNameBase + (i + 1);

                sb.append(" ");
                sb.append(numberedColName);
                sb.append("= :");
                sb.append(numberedColName);
                sb.append(",");

                queryParams.put(numberedColName,
                        iDates[i].getCalendarFromClimateDate());
            }
        }

        return sb.toString();
    }

    /**
     * Create a set clause for the given column name base, which follows natural
     * numbering naming conventions, setting values using the given dates, up to
     * the smaller of the size of the list of dates and the limit given.
     * 
     * Return string will either be empty, or will begin with a space (but not
     * it's own starting comma), and end in a comma (but not a space).
     * 
     * @param iColumnNameBaseStart
     * @param iColumnNameBaseEnd
     * @param iDates
     * @param iLimit
     * @param iIncludeHour
     *            true to include hours in the date strings, if hour is not
     *            missing.
     * @return
     */
    private static String createSetDayClauseForList(String iColumnNameBaseStart,
            String iColumnNameBaseEnd, ClimateDates[] iDates, int iLimit,
            boolean iIncludeHour) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < iDates.length && i < iLimit; i++) {
            if (!iDates[i].getStart().isPartialMissing()) {
                sb.append(" ");
                sb.append(iColumnNameBaseStart);
                sb.append(i + 1);
                sb.append("= '");
                sb.append(iDates[i].getStart().toFullDateString());
                if (iIncludeHour && iDates[i].getStartTime()
                        .getHour() != ParameterFormatClimate.MISSING_HOUR) {
                    sb.append(" ");
                    sb.append(iDates[i].getStartTime().getHour());
                }

                sb.append("',");
            }
            if (!iDates[i].getEnd().isPartialMissing()) {
                sb.append(" ");
                sb.append(iColumnNameBaseEnd);
                sb.append(i + 1);
                sb.append("= '");
                sb.append(iDates[i].getEnd().toFullDateString());
                if (iIncludeHour && iDates[i].getEndTime()
                        .getHour() != ParameterFormatClimate.MISSING_HOUR) {
                    sb.append(" ");
                    sb.append(iDates[i].getEndTime().getHour());
                }
                sb.append("',");
            }
        }

        return sb.toString();
    }

    /**
     * Get period data from monthly ASOS based on the given dates for the given
     * station.
     * 
     * @param iStationCode
     * @param iDates
     * @return
     * @throws ClimateQueryException
     */
    public PeriodData getPeriodFromMonthlyASOS(String iStationCode,
            ClimateDates iDates) throws ClimateQueryException {
        PeriodData oPeriodData = PeriodData.getMissingPeriodData();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startYear", iDates.getStart().getYear());
        paramMap.put("startMon", iDates.getStart().getMon());
        paramMap.put("endMon", iDates.getEnd().getMon());
        paramMap.put("stationCode", iStationCode);

        // date/station limited "where" clause
        StringBuilder dateStationWhereClause = new StringBuilder(
                " WHERE station_code= :stationCode AND");
        if (iDates.getStart().getYear() == iDates.getEnd().getYear()) {
            // dates are same year
            dateStationWhereClause.append(" year= :startYear");
            // month in range
            dateStationWhereClause.append(" AND month >= :startMon");
            dateStationWhereClause.append(" AND month <= :endMon");
        } else {
            // dates not the same year

            paramMap.put("endYear", iDates.getEnd().getYear());
            // start year, month >= start month
            dateStationWhereClause.append(" ((year = :startYear");
            dateStationWhereClause.append(" AND month >= :startMon")
                    .append(")");
            // middle year, any month
            dateStationWhereClause.append(" OR (year > :startYear");
            dateStationWhereClause.append(" AND year < :endYear").append(")");
            // end year, month <= end month
            dateStationWhereClause.append(" OR (year = :endYear");
            dateStationWhereClause.append(" AND month <= :endMon").append("))");
        }

        // maximum temperature
        StringBuilder maxTempQuery = new StringBuilder("SELECT");
        // select
        maxTempQuery.append(
                " maxtemp_mon, month, year, maxtemp_day1, maxtemp_day2, maxtemp_day3");
        maxTempQuery.append(" FROM ")
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        maxTempQuery.append(dateStationWhereClause)
                .append(" AND maxtemp_mon != ");
        maxTempQuery.append(ParameterFormatClimate.MISSING);
        // second select
        maxTempQuery
                .append(" AND maxtemp_mon= ( SELECT MAX(maxtemp_mon) FROM ");
        maxTempQuery.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        maxTempQuery.append(dateStationWhereClause)
                .append(" AND maxtemp_mon != ");
        maxTempQuery.append(ParameterFormatClimate.MISSING).append(")");

        try {
            Object[] results = getDao().executeSQLQuery(maxTempQuery.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        /*
                         * any value except station code and month could be null
                         */
                        // max temp
                        oPeriodData.setMaxTemp(
                                oa[0] == null ? ParameterFormatClimate.MISSING
                                        : ((Number) oa[0]).intValue());

                        // dates
                        int month = ((Number) oa[1]).intValue();
                        int year = oa[2] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[2]).intValue();

                        // first day of max temp
                        int firstMaxTempDay = oa[3] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[3]).intValue();
                        ClimateDate firstMaxTempDate;
                        if (firstMaxTempDay == ParameterFormatClimate.MISSING) {
                            firstMaxTempDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            firstMaxTempDate = new ClimateDate(firstMaxTempDay,
                                    month, year);
                        }
                        oPeriodData.getDayMaxTempList().add(firstMaxTempDate);

                        // second day of max temp
                        int secondMaxTempDay = oa[4] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[4]).intValue();
                        ClimateDate secondMaxTempDate;
                        if (secondMaxTempDay == ParameterFormatClimate.MISSING) {
                            secondMaxTempDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            secondMaxTempDate = new ClimateDate(
                                    secondMaxTempDay, month, year);
                        }
                        oPeriodData.getDayMaxTempList().add(secondMaxTempDate);

                        // third day of max temp
                        int thirdMaxTempDay = oa[5] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[5]).intValue();
                        ClimateDate thirdMaxTempDate;
                        if (thirdMaxTempDay == ParameterFormatClimate.MISSING) {
                            thirdMaxTempDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            thirdMaxTempDate = new ClimateDate(thirdMaxTempDay,
                                    month, year);
                        }
                        oPeriodData.getDayMaxTempList().add(thirdMaxTempDate);

                        oPeriodData.getDataMethods()
                                .setMaxTempQc(QCValues.VALUE_FROM_MSM);
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: ["
                            + maxTempQuery + "] and map: [" + paramMap + "]",
                    e);
        }

        // minimum temperature
        StringBuilder minTempQuery = new StringBuilder("SELECT");
        // select
        minTempQuery.append(
                " mintemp_mon, month, year, mintemp_day1, mintemp_day2, mintemp_day3");
        minTempQuery.append(" FROM ")
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        minTempQuery.append(dateStationWhereClause)
                .append(" AND mintemp_mon != ");
        minTempQuery.append(ParameterFormatClimate.MISSING);
        // second select
        minTempQuery
                .append(" AND mintemp_mon= ( SELECT MIN(mintemp_mon) FROM ");
        minTempQuery.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        minTempQuery.append(dateStationWhereClause)
                .append(" AND mintemp_mon != ");
        minTempQuery.append(ParameterFormatClimate.MISSING).append(")");

        try {
            Object[] results = getDao().executeSQLQuery(minTempQuery.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        /*
                         * any value except station code and month could be null
                         */
                        // min temp
                        oPeriodData.setMinTemp(
                                oa[0] == null ? ParameterFormatClimate.MISSING
                                        : ((Number) oa[0]).intValue());

                        // dates
                        int month = ((Number) oa[1]).intValue();
                        int year = oa[2] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[2]).intValue();

                        // first day of min temp
                        int firstMinTempDay = oa[3] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[3]).intValue();
                        ClimateDate firstMinTempDate;
                        if (firstMinTempDay == ParameterFormatClimate.MISSING) {
                            firstMinTempDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            firstMinTempDate = new ClimateDate(firstMinTempDay,
                                    month, year);
                        }
                        oPeriodData.getDayMinTempList().add(firstMinTempDate);

                        // second day of min temp
                        int secondMinTempDay = oa[4] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[4]).intValue();
                        ClimateDate secondMinTempDate;
                        if (secondMinTempDay == ParameterFormatClimate.MISSING) {
                            secondMinTempDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            secondMinTempDate = new ClimateDate(
                                    secondMinTempDay, month, year);
                        }
                        oPeriodData.getDayMinTempList().add(secondMinTempDate);

                        // third day of min temp
                        int thirdMinTempDay = oa[5] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[5]).intValue();
                        ClimateDate thirdMinTempDate;
                        if (thirdMinTempDay == ParameterFormatClimate.MISSING) {
                            thirdMinTempDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            thirdMinTempDate = new ClimateDate(thirdMinTempDay,
                                    month, year);
                        }
                        oPeriodData.getDayMinTempList().add(thirdMinTempDate);

                        oPeriodData.getDataMethods()
                                .setMinTempQc(QCValues.VALUE_FROM_MSM);
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: ["
                            + minTempQuery + "] and map: [" + paramMap + "]",
                    e);
        }

        // average max temp
        // 21320 using average of months calculation per legacy
        StringBuilder avgMaxTempQuery = new StringBuilder("SELECT ");
        // select
        avgMaxTempQuery.append(" AVG(avg_daily_max) FROM ");
        avgMaxTempQuery.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        avgMaxTempQuery.append(dateStationWhereClause)
                .append(" AND avg_daily_max != ");
        avgMaxTempQuery.append(ParameterFormatClimate.MISSING);

        oPeriodData.setMaxTempMean(
                ((Number) queryForOneValue(avgMaxTempQuery.toString(), paramMap,
                        ParameterFormatClimate.MISSING)).floatValue());
        if (oPeriodData
                .getMaxTempMean() != (float) ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setAvgMaxTempQc(QCValues.VALUE_FROM_MSM);
        }

        // average min temp
        // 21320 using average of months calculation per legacy
        StringBuilder avgMinTempQuery = new StringBuilder("SELECT ");
        // select
        avgMinTempQuery.append(" AVG(avg_daily_min) FROM ");
        avgMinTempQuery.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        avgMinTempQuery.append(dateStationWhereClause)
                .append(" AND avg_daily_min != ");
        avgMinTempQuery.append(ParameterFormatClimate.MISSING);

        oPeriodData.setMinTempMean(
                ((Number) queryForOneValue(avgMinTempQuery.toString(), paramMap,
                        ParameterFormatClimate.MISSING)).floatValue());

        if (oPeriodData
                .getMinTempMean() != (float) ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setAvgMinTempQc(QCValues.VALUE_FROM_MSM);
        }

        // average temp
        // 21320 using average of months calculation per legacy
        StringBuilder avgTempQuery = new StringBuilder("SELECT ");
        // select
        avgTempQuery.append(" AVG(avg_mon_temp) FROM ");
        avgTempQuery.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        avgTempQuery.append(dateStationWhereClause)
                .append(" AND avg_mon_temp != ");
        avgTempQuery.append(ParameterFormatClimate.MISSING);

        oPeriodData.setMeanTemp(
                ((Number) queryForOneValue(avgTempQuery.toString(), paramMap,
                        ParameterFormatClimate.MISSING)).floatValue());

        if (oPeriodData
                .getMeanTemp() != (float) ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods().setMeanTempQc(QCValues.VALUE_FROM_MSM);
        }

        // max temp <= 32
        oPeriodData.setNumMaxLessThan32F(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_maxt_blo_fzg", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumMaxLessThan32F() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setMaxTempLE32Qc(QCValues.VALUE_FROM_MSM);
        }

        // max temp >= 90
        oPeriodData.setNumMaxGreaterThan90F(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_maxt_hot", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumMaxGreaterThan90F() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setMaxTempGE90Qc(QCValues.VALUE_FROM_MSM);
        }

        // min temp <= 32
        oPeriodData.setNumMinLessThan32F(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_mint_blo_fzg", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumMinLessThan32F() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods().setMinLE32Qc(QCValues.VALUE_FROM_MSM);
        }

        // min temp <= 0
        oPeriodData.setNumMinLessThan0F(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_mint_blo_0f", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumMinLessThan0F() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods().setMinLE0Qc(QCValues.VALUE_FROM_MSM);
        }

        // heating
        oPeriodData.setNumHeatTotal(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "heating", ParameterFormatClimate.MISSING_DEGREE_DAY)
                                .intValue());

        if (oPeriodData
                .getNumHeatTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
            oPeriodData.getDataMethods().setHeatQc(QCValues.VALUE_FROM_MSM);
        }

        // cooling
        oPeriodData.setNumCoolTotal(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "cooling", ParameterFormatClimate.MISSING_DEGREE_DAY)
                                .intValue());

        if (oPeriodData
                .getNumCoolTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
            oPeriodData.getDataMethods().setCoolQc(QCValues.VALUE_FROM_MSM);
        }

        // total precip
        oPeriodData.setPrecipTotal(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "month_precip", ParameterFormatClimate.MISSING_PRECIP)
                                .floatValue());

        if (oPeriodData
                .getPrecipTotal() != ParameterFormatClimate.MISSING_PRECIP) {
            oPeriodData.getDataMethods().setPrecipQc(QCValues.VALUE_FROM_MSM);
        }

        // precip >= 0.01 inches
        oPeriodData.setNumPrcpGreaterThan01(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_hundreth", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumPrcpGreaterThan01() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setPrecipGE01Qc(QCValues.VALUE_FROM_MSM);
        }

        // precip >= 0.1 inches
        oPeriodData.setNumPrcpGreaterThan10(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_tenth", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumPrcpGreaterThan10() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setPrecipGE10Qc(QCValues.VALUE_FROM_MSM);
        }

        // precip >= 0.5 inches
        oPeriodData.setNumPrcpGreaterThan50(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_half", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumPrcpGreaterThan50() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setPrecipGE50Qc(QCValues.VALUE_FROM_MSM);
        }

        // precip >= 1.0 inches
        oPeriodData.setNumPrcpGreaterThan100(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "days_inch", ParameterFormatClimate.MISSING)
                                .intValue());

        if (oPeriodData
                .getNumPrcpGreaterThan100() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setPrecipGE100Qc(QCValues.VALUE_FROM_MSM);
        }

        // max precip in 24 hours
        StringBuilder maxPrecip24HoursQuery = new StringBuilder("SELECT");
        // select
        maxPrecip24HoursQuery.append(
                " max_24h_pcp, month, year, max_24h_pcp_day1, max_24h_pcp_day2");
        maxPrecip24HoursQuery.append(" FROM ")
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        maxPrecip24HoursQuery.append(dateStationWhereClause)
                .append(" AND max_24h_pcp != ");
        maxPrecip24HoursQuery.append(ParameterFormatClimate.MISSING_PRECIP);
        // second select
        maxPrecip24HoursQuery
                .append(" AND max_24h_pcp= ( SELECT MAX(max_24h_pcp) FROM ");
        maxPrecip24HoursQuery
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        maxPrecip24HoursQuery.append(dateStationWhereClause)
                .append(" AND max_24h_pcp != ");
        maxPrecip24HoursQuery.append(ParameterFormatClimate.MISSING_PRECIP)
                .append(")");

        try {
            Object[] results = getDao().executeSQLQuery(
                    maxPrecip24HoursQuery.toString(), paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        /*
                         * all values exception station code and month could be
                         * null
                         */
                        // max precip 24 hours
                        oPeriodData.setPrecipMax24H(oa[0] == null
                                ? ParameterFormatClimate.MISSING_PRECIP
                                : ((Number) oa[0]).floatValue());

                        // dates
                        int month = ((Number) oa[1]).intValue();
                        int year = oa[2] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[2]).intValue();

                        // start day of max precip
                        int startMaxPrecipDay = oa[3] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : ((Number) oa[3]).intValue();
                        ClimateDate startMaxPrecipDate;
                        if (startMaxPrecipDay == ParameterFormatClimate.MISSING_DATE) {
                            startMaxPrecipDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            startMaxPrecipDate = new ClimateDate(
                                    startMaxPrecipDay, month, year);
                        }

                        // end day of max precip
                        int endMaxPrecipDay = oa[4] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : ((Number) oa[4]).intValue();
                        ClimateDate endMaxPrecipDate;
                        if (endMaxPrecipDay == ParameterFormatClimate.MISSING_DATE) {
                            endMaxPrecipDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            endMaxPrecipDate = new ClimateDate(endMaxPrecipDay,
                                    month, year);
                        }

                        oPeriodData.getPrecip24HDates().add(new ClimateDates(
                                startMaxPrecipDate, endMaxPrecipDate));

                        oPeriodData.getDataMethods()
                                .setPrecip24hrMaxQc(QCValues.VALUE_FROM_MSM);

                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get max precip data using query: ["
                        + maxPrecip24HoursQuery + "] and map: [" + paramMap
                        + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: ["
                            + maxPrecip24HoursQuery + "] and map: [" + paramMap
                            + "]",
                    e);
        }

        // percent sun
        // 21320 using average of months calculation per legacy
        StringBuilder percentSunQuery = new StringBuilder("SELECT ");
        // select
        percentSunQuery.append(" AVG(percent_sun) FROM ");
        percentSunQuery.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        percentSunQuery.append(dateStationWhereClause)
                .append(" AND percent_sun != ");
        percentSunQuery.append(ParameterFormatClimate.MISSING);

        oPeriodData.setPossSun(
                ((Number) queryForOneValue(percentSunQuery.toString(), paramMap,
                        ParameterFormatClimate.MISSING)).intValue());

        if (oPeriodData.getPossSun() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods().setPossSunQc(QCValues.VALUE_FROM_MSM);
        }

        // max snow in 24 hours
        StringBuilder maxSnow24HoursQuery = new StringBuilder("SELECT");
        // select
        maxSnow24HoursQuery.append(
                " max_24h_snow, month, year, max_24h_snow_date1, max_24h_snow_date2");
        maxSnow24HoursQuery.append(" FROM ")
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        maxSnow24HoursQuery.append(dateStationWhereClause)
                .append(" AND max_24h_snow != ");
        maxSnow24HoursQuery.append(ParameterFormatClimate.MISSING_SNOW);
        // second select
        maxSnow24HoursQuery
                .append(" AND max_24h_snow= ( SELECT MAX(max_24h_snow) FROM ");
        maxSnow24HoursQuery
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        maxSnow24HoursQuery.append(dateStationWhereClause)
                .append(" AND max_24h_snow != ");
        maxSnow24HoursQuery.append(ParameterFormatClimate.MISSING_SNOW)
                .append(")");

        try {
            Object[] results = getDao()
                    .executeSQLQuery(maxSnow24HoursQuery.toString(), paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        /*
                         * all values except month and station code could be
                         * null
                         */
                        // max snow 24 hours
                        oPeriodData.setSnowMax24H(oa[0] == null
                                ? ParameterFormatClimate.MISSING_SNOW
                                : ((Number) oa[0]).floatValue());

                        // dates
                        int month = ((Number) oa[1]).intValue();
                        int year = oa[2] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[2]).intValue();

                        // start day of max snow
                        int startMaxSnowDay = oa[3] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : ((Number) oa[3]).intValue();
                        ClimateDate startMaxSnowDate;
                        if (startMaxSnowDay == ParameterFormatClimate.MISSING_DATE) {
                            startMaxSnowDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            startMaxSnowDate = new ClimateDate(startMaxSnowDay,
                                    month, year);
                        }

                        // end day of max snow
                        int endMaxSnowDay = oa[4] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : ((Number) oa[4]).intValue();
                        ClimateDate endMaxSnowDate;
                        if (endMaxSnowDay == ParameterFormatClimate.MISSING_DATE) {
                            endMaxSnowDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            endMaxSnowDate = new ClimateDate(endMaxSnowDay,
                                    month, year);
                        }

                        oPeriodData.getSnow24HDates().add(new ClimateDates(
                                startMaxSnowDate, endMaxSnowDate));

                        oPeriodData.getDataMethods()
                                .setSnow24hrMaxQc(QCValues.VALUE_FROM_MSM);

                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get max snow data using query: ["
                        + maxSnow24HoursQuery + "] and map: [" + paramMap
                        + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: ["
                            + maxSnow24HoursQuery + "] and map: [" + paramMap
                            + "]",
                    e);
        }

        // max snow on ground depth
        StringBuilder maxSnowGroundDepthQuery = new StringBuilder("SELECT");
        // select
        maxSnowGroundDepthQuery
                .append(" snowdepth, month, year, snowdepth_date" + " FROM ");
        maxSnowGroundDepthQuery
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        // where
        maxSnowGroundDepthQuery.append(dateStationWhereClause)
                .append(" AND snowdepth != ");
        maxSnowGroundDepthQuery
                .append(ParameterFormatClimate.MISSING_SNOW_VALUE);
        // second select
        maxSnowGroundDepthQuery
                .append(" AND snowdepth= ( SELECT MAX(snowdepth) FROM ");
        maxSnowGroundDepthQuery
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        maxSnowGroundDepthQuery.append(dateStationWhereClause)
                .append(" AND snowdepth != ");
        maxSnowGroundDepthQuery
                .append(ParameterFormatClimate.MISSING_SNOW_VALUE).append(")");

        try {
            Object[] results = getDao().executeSQLQuery(
                    maxSnowGroundDepthQuery.toString(), paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        /*
                         * All values except month and station code could be
                         * null
                         */
                        // max snow ground depth
                        oPeriodData.setSnowGroundMax(oa[0] == null
                                ? ParameterFormatClimate.MISSING_SNOW_VALUE
                                : ((Number) oa[0]).intValue());

                        // dates
                        int month = ((Number) oa[1]).intValue();
                        int year = oa[2] == null
                                ? ParameterFormatClimate.MISSING
                                : ((Number) oa[2]).intValue();

                        // day of max snow on ground depth
                        int startMaxSnowGroundDepthDay = oa[3] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : ((Number) oa[3]).intValue();
                        ClimateDate startMaxSnowGroundDepthDate;
                        if (startMaxSnowGroundDepthDay == ParameterFormatClimate.MISSING_DATE) {
                            startMaxSnowGroundDepthDate = ClimateDate
                                    .getMissingClimateDate();
                        } else {
                            startMaxSnowGroundDepthDate = new ClimateDate(
                                    startMaxSnowGroundDepthDay, month, year);
                        }
                        oPeriodData.getSnowGroundMaxDateList()
                                .add(startMaxSnowGroundDepthDate);

                        oPeriodData.getDataMethods()
                                .setMaxDepthQc(QCValues.VALUE_FROM_MSM);
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get max snow depth data from query: ["
                        + maxSnowGroundDepthQuery + "] and map: [" + paramMap
                        + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: ["
                            + maxSnowGroundDepthQuery + "] and map: ["
                            + paramMap + "]",
                    e);
        }

        // cloudy days
        oPeriodData.setNumMostlyCloudyDays(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "cloud_days", ParameterFormatClimate.MISSING)
                                .shortValue());

        if (oPeriodData
                .getNumMostlyCloudyDays() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods()
                    .setCloudyDaysQc(QCValues.VALUE_FROM_MSM);
        }

        // partly cloudy days
        oPeriodData.setNumPartlyCloudyDays(
                getPeriodSum(dateStationWhereClause.toString(), paramMap,
                        "pcloud_days", ParameterFormatClimate.MISSING)
                                .shortValue());

        if (oPeriodData
                .getNumPartlyCloudyDays() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods().setPcDaysQc(QCValues.VALUE_FROM_MSM);
        }

        // clear/fair days
        oPeriodData
                .setNumFairDays(getPeriodSum(dateStationWhereClause.toString(),
                        paramMap, "clear_days", ParameterFormatClimate.MISSING)
                                .shortValue());

        if (oPeriodData.getNumFairDays() != ParameterFormatClimate.MISSING) {
            oPeriodData.getDataMethods().setFairDaysQc(QCValues.VALUE_FROM_MSM);
        }

        return oPeriodData;
    }

    /**
     * Get the some of values for the given column applying the given where
     * clause, where the values are not equal to the given missing value.
     * 
     * @param iDateStationWhereClause
     * @param paramMap
     * @param iColumnName
     * @param iMissingValue
     * @return
     */
    private Number getPeriodSum(String iDateStationWhereClause,
            Map<String, Object> paramMap, String iColumnName,
            Object iMissingValue) {
        StringBuilder query = new StringBuilder("SELECT SUM(")
                .append(iColumnName).append(") as sum_values FROM  ");
        query.append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        query.append(iDateStationWhereClause).append(" AND ")
                .append(iColumnName).append(" != ");
        query.append(iMissingValue);

        return (Number) queryForOneValue(query.toString(), paramMap,
                iMissingValue);
    }

    /**
     * Migrated from get_period_data.ec.
     * 
     * <pre>
     * int get_period_data (    int         itype,
     *              climate_date        *begin,
     *              climate_date        *end,
     *              period_data         *data,
     *              period_data_method      *qc
     *                )
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  November 1999
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function retrieves climate data from the monthly, seasonal, 
     *      annual database.
     *
     * </pre>
     * 
     * @param iType
     *            - the type of period data; monthly, seasonal, or annual
     * @param beginDate
     * @param endDate
     * @param data
     * @param dataMethods
     * @return QueryPeriodData
     * @throws ClimateQueryException
     */

    public QueryData getPeriodData(PeriodType iType, ClimateDate beginDate,
            ClimateDate endDate, PeriodData data, PeriodDataMethod dataMethods)
            throws ClimateQueryException {

        QueryData queryData = new QueryData();
        Map<String, Object> paramMap = new HashMap<>();
        String query = getPeriodDataQuery(data.getInformId(), iType,
                new ClimateDates(beginDate, endDate), paramMap);

        try {
            Object[] results = getDao().executeSQLQuery(query, paramMap);
            if ((results != null) && (results.length >= 1)) {
                // get the first record
                Object result = results[0];
                if (result instanceof Object[]) {
                    Object[] oa = (Object[]) result;
                    int index = 0;
                    // any value could be null
                    // max temp
                    if (oa[index] != null) {
                        data.setMaxTemp(((Number) oa[index]).intValue());
                    }
                    index++;

                    // days of max temp
                    List<ClimateDate> maxTempDays = new ArrayList<ClimateDate>();
                    // first day of max temp
                    maxTempDays.add(new ClimateDate(oa[index++]));
                    // second day of max temp
                    maxTempDays.add(new ClimateDate(oa[index++]));
                    // third day of max temp
                    maxTempDays.add(new ClimateDate(oa[index++]));
                    data.setDayMaxTempList(maxTempDays);

                    // average max temp
                    if (oa[index] != null) {
                        data.setMaxTempMean(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // mean temp
                    if (oa[index] != null) {
                        data.setMeanTemp(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // min temp
                    if (oa[index] != null) {
                        data.setMinTemp(((Number) oa[index]).intValue());
                    }
                    index++;

                    // days of min temp
                    List<ClimateDate> minTempDays = new ArrayList<ClimateDate>();
                    // first day of min temp
                    minTempDays.add(new ClimateDate(oa[index++]));
                    // second day of min temp
                    minTempDays.add(new ClimateDate(oa[index++]));
                    // third day of min temp
                    minTempDays.add(new ClimateDate(oa[index++]));
                    data.setDayMinTempList(minTempDays);

                    // average min temp
                    if (oa[index] != null) {
                        data.setMinTempMean(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // days w/ max temp >= 90
                    if (oa[index] != null) {
                        data.setNumMaxGreaterThan90F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ max temp <= 32
                    if (oa[index] != null) {
                        data.setNumMaxLessThan32F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ max temp >= custom number 1
                    if (oa[index] != null) {
                        data.setNumMaxGreaterThanT1F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ max temp >= custom number 2
                    if (oa[index] != null) {
                        data.setNumMaxGreaterThanT2F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ max temp <= custom number
                    if (oa[index] != null) {
                        data.setNumMaxLessThanT3F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/min temp <= 32
                    if (oa[index] != null) {
                        data.setNumMinLessThan32F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // day w/min temp <= 0
                    if (oa[index] != null) {
                        data.setNumMinLessThan0F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ min temp >= custom number
                    if (oa[index] != null) {
                        data.setNumMinGreaterThanT4F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ min temp <= custom number 1
                    if (oa[index] != null) {
                        data.setNumMinLessThanT5F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days w/ min temp <= custom number 2
                    if (oa[index] != null) {
                        data.setNumMinLessThanT6F(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // cooling degree days
                    if (oa[index] != null) {
                        data.setNumCoolTotal(((Number) oa[index]).intValue());
                    }
                    index++;

                    // cooling degree days since jan 1
                    if (oa[index] != null) {
                        data.setNumCool1Jan(((Number) oa[index]).intValue());
                    }
                    index++;

                    // heating degree days
                    if (oa[index] != null) {
                        data.setNumHeatTotal(((Number) oa[index]).intValue());
                    }
                    index++;

                    // heating degree days since jul 1
                    if (oa[index] != null) {
                        data.setNumHeat1July(((Number) oa[index]).intValue());
                    }
                    index++;

                    // total precipitation
                    if (oa[index] != null) {
                        data.setPrecipTotal(((Number) oa[index]).floatValue());

                    }
                    index++;

                    // 24 hour max precipitation
                    if (oa[index] != null) {
                        data.setPrecipMax24H(((Number) oa[index]).floatValue());
                    }
                    index++;

                    List<ClimateDates> precip24HDates = new ArrayList<ClimateDates>();
                    // first start and end dates

                    precip24HDates
                            .add(ClimateDates.parseClimateDatesFromStrings(
                                    oa[index++], oa[index++]));
                    // second start and end dates
                    precip24HDates
                            .add(ClimateDates.parseClimateDatesFromStrings(
                                    oa[index++], oa[index++]));
                    // third start and end dates
                    precip24HDates
                            .add(ClimateDates.parseClimateDatesFromStrings(
                                    oa[index++], oa[index++]));
                    data.setPrecip24HDates(precip24HDates);

                    // greatest storm total
                    if (oa[index] != null) {
                        data.setPrecipStormMax(
                                ((Number) oa[index]).floatValue());
                    }
                    index++;

                    List<ClimateDates> precipStormList = new ArrayList<ClimateDates>();
                    // first start and end dates and hours
                    precipStormList
                            .add(ClimateDates.parseClimateDatesFromStrings(
                                    oa[index++], oa[index++]));
                    // second start and end dates and hours
                    precipStormList
                            .add(ClimateDates.parseClimateDatesFromStrings(
                                    oa[index++], oa[index++]));
                    // third start and end dates and hours
                    precipStormList
                            .add(ClimateDates.parseClimateDatesFromStrings(
                                    oa[index++], oa[index++]));
                    data.setPrecipStormList(precipStormList);

                    // average daily precipitation
                    if (oa[index] != null) {
                        data.setPrecipMeanDay(
                                ((Number) oa[index]).floatValue());
                    }
                    index++;

                    // days with precip >= 0.01 in
                    if (oa[index] != null) {
                        data.setNumPrcpGreaterThan01(
                                ((Number) oa[index]).intValue());
                    }
                    index++;
                    // days with precip >= 0.10 in
                    if (oa[index] != null) {
                        data.setNumPrcpGreaterThan10(
                                ((Number) oa[index]).intValue());
                    }
                    index++;
                    // days with precip >= 0.50 in
                    if (oa[index] != null) {
                        data.setNumPrcpGreaterThan50(
                                ((Number) oa[index]).intValue());
                    }
                    index++;
                    // days with precip >= 1.00 in
                    if (oa[index] != null) {
                        data.setNumPrcpGreaterThan100(
                                ((Number) oa[index]).intValue());
                    }
                    index++;
                    // days with precip >= custom number 1
                    if (oa[index] != null) {
                        data.setNumPrcpGreaterThanP1(
                                ((Number) oa[index]).intValue());
                    }
                    index++;
                    // days with precip >= custom number 2
                    if (oa[index] != null) {
                        data.setNumPrcpGreaterThanP2(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // total snowfall
                    if (oa[index] != null) {
                        data.setSnowTotal(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // 24hr max snowfall
                    if (oa[index] != null) {
                        data.setSnowMax24H(((Number) oa[index]).floatValue());
                    }
                    index++;

                    List<ClimateDates> snow24hDates = new ArrayList<ClimateDates>();
                    // first start and end dates
                    snow24hDates.add(ClimateDates.parseClimateDatesFromStrings(
                            oa[index++], oa[index++]));
                    // second start and end dates
                    snow24hDates.add(ClimateDates.parseClimateDatesFromStrings(
                            oa[index++], oa[index++]));
                    // third start and end dates
                    snow24hDates.add(ClimateDates.parseClimateDatesFromStrings(
                            oa[index++], oa[index++]));
                    data.setSnow24HDates(snow24hDates);

                    // greatest storm total
                    if (oa[index] != null) {
                        data.setSnowMaxStorm(((Number) oa[index]).floatValue());
                    }
                    index++;

                    List<ClimateDates> snowStormList = new ArrayList<ClimateDates>();
                    // first start and end dates and hours
                    snowStormList.add(ClimateDates.parseClimateDatesFromStrings(
                            oa[index++], oa[index++]));
                    // second start and end dates and hours
                    snowStormList.add(ClimateDates.parseClimateDatesFromStrings(
                            oa[index++], oa[index++]));
                    // third start and end dates and hours
                    snowStormList.add(ClimateDates.parseClimateDatesFromStrings(
                            oa[index++], oa[index++]));
                    data.setSnowStormList(snowStormList);

                    // snow water equivalent
                    if (oa[index] != null) {
                        data.setSnowWater(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // average snow depth
                    if (oa[index] != null) {
                        data.setSnowGroundMean(
                                ((Number) oa[index]).floatValue());
                    }
                    index++;

                    // max snow depth
                    if (oa[index] != null) {
                        data.setSnowGroundMax(((Number) oa[index]).intValue());
                    }
                    index++;

                    List<ClimateDate> snowGroundMaxDateList = new ArrayList<ClimateDate>();
                    // first day of max snow depth
                    snowGroundMaxDateList.add(new ClimateDate(oa[index++]));
                    // second day of max snow depth
                    snowGroundMaxDateList.add(new ClimateDate(oa[index++]));
                    // third day of max snow depth
                    snowGroundMaxDateList.add(new ClimateDate(oa[index++]));
                    data.setSnowGroundMaxDateList(snowGroundMaxDateList);

                    // days with any snowfall
                    if (oa[index] != null) {
                        data.setNumSnowGreaterThanTR(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days with 1 in or more
                    if (oa[index] != null) {
                        data.setNumSnowGreaterThan1(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // days with custom number in or more
                    if (oa[index] != null) {
                        data.setNumSnowGreaterThanS1(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // average wind speed
                    if (oa[index] != null) {
                        data.setAvgWindSpd(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // resultant wind direction and speed
                    int resultWindDir = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;
                    index++;
                    int resultWindSpeed = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;
                    index++;
                    data.setResultWind(
                            new ClimateWind(resultWindDir, resultWindSpeed));

                    // max wind speed
                    float maxWindSpeed = (oa[index] != null)
                            ? ((Number) oa[index]).floatValue()
                            : ParameterFormatClimate.MISSING_SPEED;
                    index++;
                    // max wind directions
                    List<ClimateWind> maxWindList = new ArrayList<ClimateWind>();
                    // max wind direction 1
                    int maxWindDir1 = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;

                    maxWindList.add(new ClimateWind(maxWindDir1, maxWindSpeed));
                    index++;
                    // max wind direction 2
                    int maxWindDir2 = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;
                    maxWindList.add(new ClimateWind(maxWindDir2, maxWindSpeed));
                    index++;
                    // max wind direction 3
                    int maxWindDir3 = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;

                    maxWindList.add(new ClimateWind(maxWindDir3, maxWindSpeed));
                    index++;
                    data.setMaxWindList(maxWindList);

                    // max wind dates
                    List<ClimateDate> maxWindDayList = new ArrayList<ClimateDate>();
                    // first max wind day
                    maxWindDayList.add(new ClimateDate(oa[index++]));
                    // second max wind day
                    maxWindDayList.add(new ClimateDate(oa[index++]));
                    // third max wind day
                    maxWindDayList.add(new ClimateDate(oa[index++]));
                    data.setMaxWindDayList(maxWindDayList);

                    // max gust speed
                    float maxGustSpeed = (oa[index] != null)
                            ? ((Number) oa[index]).floatValue()
                            : ParameterFormatClimate.MISSING_SPEED;
                    index++;
                    // max gust directions
                    List<ClimateWind> maxGustList = new ArrayList<ClimateWind>();
                    // max gust direction 1
                    int maxGustDir1 = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;

                    maxGustList.add(new ClimateWind(maxGustDir1, maxGustSpeed));
                    index++;
                    // max gust direction 2
                    int maxGustDir2 = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;
                    maxGustList.add(new ClimateWind(maxGustDir2, maxGustSpeed));
                    index++;
                    // max gust direction 3
                    int maxGustDir3 = (oa[index] != null)
                            ? ((Number) oa[index]).intValue()
                            : ParameterFormatClimate.MISSING;

                    maxGustList.add(new ClimateWind(maxGustDir3, maxGustSpeed));
                    index++;
                    data.setMaxGustList(maxGustList);

                    // max gust dates
                    List<ClimateDate> maxGustDayList = new ArrayList<ClimateDate>();
                    // first max gust day
                    maxGustDayList.add(new ClimateDate(oa[index++]));
                    // second max gust day
                    maxGustDayList.add(new ClimateDate(oa[index++]));
                    // third max gust day
                    maxGustDayList.add(new ClimateDate(oa[index++]));
                    data.setMaxGustDayList(maxGustDayList);

                    // percent possible sun
                    if (oa[index] != null) {
                        data.setPossSun(((Number) oa[index]).intValue());
                    }
                    index++;

                    // average sky cover
                    if (oa[index] != null) {
                        data.setMeanSkyCover(((Number) oa[index]).floatValue());
                    }
                    index++;

                    // mean relative humidity
                    if (oa[index] != null) {
                        data.setMeanRh(((Number) oa[index]).intValue());
                    }
                    index++;

                    // number of fair days
                    if (oa[index] != null) {
                        data.setNumFairDays(((Number) oa[index]).intValue());
                    }
                    index++;

                    // number of partly cloudy days
                    if (oa[index] != null) {
                        data.setNumPartlyCloudyDays(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // number of cloudy days
                    if (oa[index] != null) {
                        data.setNumMostlyCloudyDays(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // thunderstorms
                    if (oa[index] != null) {
                        data.setNumThunderStorms(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // mixed precip
                    if (oa[index] != null) {
                        data.setNumMixedPrecip(((Number) oa[index]).intValue());
                    }
                    index++;

                    // heavy rain
                    if (oa[index] != null) {
                        data.setNumHeavyRain(((Number) oa[index]).intValue());
                    }
                    index++;

                    // rain
                    if (oa[index] != null) {
                        data.setNumRain(((Number) oa[index]).intValue());
                    }
                    index++;

                    // light rain
                    if (oa[index] != null) {
                        data.setNumLightRain(((Number) oa[index]).intValue());
                    }
                    index++;

                    // freezing rain
                    if (oa[index] != null) {
                        data.setNumFreezingRain(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // light freezing rain
                    if (oa[index] != null) {
                        data.setNumLightFreezingRain(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // hail
                    if (oa[index] != null) {
                        data.setNumHail(((Number) oa[index]).intValue());
                    }
                    index++;

                    // heavy snow
                    if (oa[index] != null) {
                        data.setNumHeavySnow(((Number) oa[index]).intValue());
                    }
                    index++;

                    // snow
                    if (oa[index] != null) {
                        data.setNumSnow(((Number) oa[index]).intValue());
                    }
                    index++;

                    // light snow
                    if (oa[index] != null) {
                        data.setNumLightSnow(((Number) oa[index]).intValue());
                    }
                    index++;

                    // ice pellets
                    if (oa[index] != null) {
                        data.setNumIcePellets(((Number) oa[index]).intValue());
                    }
                    index++;

                    // fog
                    if (oa[index] != null) {
                        data.setNumFog(((Number) oa[index]).intValue());
                    }
                    index++;

                    // heavy fog
                    if (oa[index] != null) {
                        data.setNumFogQuarterSM(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // haze
                    if (oa[index] != null) {
                        data.setNumHaze(((Number) oa[index]).intValue());
                    }
                    index++;

                    // Period Data Methods

                    // max temp qc
                    if (oa[index] != null) {
                        dataMethods
                                .setMaxTempQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // average max temp qc
                    if (oa[index] != null) {
                        dataMethods.setAvgMaxTempQc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // mean temp qc
                    if (oa[index] != null) {
                        dataMethods
                                .setMeanTempQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // min temp qc
                    if (oa[index] != null) {
                        dataMethods
                                .setMinTempQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // average min temp qc
                    if (oa[index] != null) {
                        dataMethods.setAvgMinTempQc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // max temp >= 90 qc
                    if (oa[index] != null) {
                        dataMethods.setMaxTempGE90Qc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // max temp <= 32 qc
                    if (oa[index] != null) {
                        dataMethods.setMaxTempLE32Qc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // min temp <= 32 qc
                    if (oa[index] != null) {
                        dataMethods
                                .setMinLE32Qc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // min temp <= 0 qc
                    if (oa[index] != null) {
                        dataMethods
                                .setMinLE0Qc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // max 24hr precip qc
                    if (oa[index] != null) {
                        dataMethods.setPrecip24hrMaxQc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // precip >= .01 qc
                    if (oa[index] != null) {
                        dataMethods.setPrecipGE01Qc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // precip >= .10 qc
                    if (oa[index] != null) {
                        dataMethods.setPrecipGE10Qc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // precip >= .50 qc
                    if (oa[index] != null) {
                        dataMethods.setPrecipGE50Qc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // max precip >= 1.00 qc
                    if (oa[index] != null) {
                        dataMethods.setPrecipGE100Qc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // max 24hr snow qc
                    if (oa[index] != null) {
                        dataMethods.setSnow24hrMaxQc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    // max depth qc
                    if (oa[index] != null) {
                        dataMethods
                                .setMaxDepthQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // heating days qc
                    if (oa[index] != null) {
                        dataMethods.setHeatQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // cooling days qc
                    if (oa[index] != null) {
                        dataMethods.setCoolQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // % possible sun qc
                    if (oa[index] != null) {
                        dataMethods
                                .setPossSunQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // fair days qc
                    if (oa[index] != null) {
                        dataMethods
                                .setFairDaysQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // partly cloudy days qc
                    if (oa[index] != null) {
                        dataMethods
                                .setPcDaysQc(((Number) oa[index]).intValue());
                    }
                    index++;

                    // mostly cloudy days qc
                    if (oa[index] != null) {
                        dataMethods.setCloudyDaysQc(
                                ((Number) oa[index]).intValue());
                    }
                    index++;

                    data.setDataMethods(dataMethods);

                    // The query was successful - set the exists flag to
                    // true.
                    queryData.setData(data);

                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }

            } else {
                logger.warn("Could not get period data using query: [" + query
                        + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: [" + query
                            + "] and map: [" + paramMap + "]",
                    e);
        }
        return queryData;
    }

    /**
     * Overload. Period data contains a data method field.
     * 
     * @param iStationID
     * @param iType
     * @param periodDates
     * @return QueryPeriodData
     * @throws ClimateQueryException
     */
    public QueryData getPeriodData(int iStationID, PeriodType iType,
            ClimateDates periodDates) throws ClimateQueryException {

        PeriodData data = PeriodData.getMissingPeriodData();

        data.setInformId(iStationID);

        return getPeriodData(iType, periodDates.getStart(),
                periodDates.getEnd(), data, data.getDataMethods());
    }

    /**
     * Period data query string
     * 
     * @param iStationID
     * @param iType
     * @param periodDates
     * @param paramMap
     *            sql param map to fill out for query
     * @return query text
     */
    private static String getPeriodDataQuery(int iStationID, PeriodType iType,
            ClimateDates periodDates, Map<String, Object> paramMap) {

        StringBuilder query = new StringBuilder(
                "SELECT max_temp, to_char(day_max_temp1, 'yyyy-MM-dd') as day_max_temp1, ");
        query.append(
                "to_char(day_max_temp2, 'yyyy-MM-dd') as day_max_temp2, to_char(day_max_temp3, 'yyyy-MM-dd') as day_max_temp3, ");
        query.append(
                "max_temp_mean, mean_temp, min_temp, to_char(day_min_temp1, 'yyyy-MM-dd') as day_min_temp1, ");
        query.append(
                "to_char(day_min_temp2, 'yyyy-MM-dd') as day_min_temp2, to_char(day_min_temp3, 'yyyy-MM-dd') as day_min_temp3, ");
        query.append("min_temp_mean, num_max_ge_90f, num_max_le_32f, ");
        query.append("num_max_ge_t1f, num_max_ge_t2f, num_max_le_t3f, ");
        query.append("num_min_le_32f, num_min_le_0f, ");
        query.append("num_min_ge_t4f, num_min_le_t5f, num_min_le_t6f, ");
        query.append(
                "num_cool_total, num_cool_1jan, num_heat_total, num_heat_1july, ");
        query.append(
                "precip_total , precip_max_24h , pcp_24h_start_day1, pcp_24h_end_day1, ");
        query.append(
                "pcp_24h_start_day2, pcp_24h_end_day2, pcp_24h_start_day3, pcp_24h_end_day3, ");
        query.append(
                "precip_storm_max, pcp_stm_start_day1, pcp_stm_end_day1, pcp_stm_start_day2, ");
        query.append(
                "pcp_stm_end_day2, pcp_stm_start_day3, pcp_stm_end_day3, ");
        query.append(
                "precip_mean_day, num_prcp_ge_01, num_prcp_ge_10, num_prcp_ge_50, num_prcp_ge_100, ");
        query.append("num_prcp_ge_p1, num_prcp_ge_p2, ");
        query.append(
                "snow_total, snow_max_24h, sno_24h_start_day1, sno_24h_end_day1, ");
        query.append(
                "sno_24h_start_day2, sno_24h_end_day2, sno_24h_start_day3, sno_24h_end_day3, ");
        query.append("snow_max_storm, ");
        query.append(
                "sno_stm_start_day1, sno_stm_end_day1, sno_stm_start_day2, sno_stm_end_day2, ");
        query.append("sno_stm_start_day3, sno_stm_end_day3, ");
        query.append("snow_water, snow_ground_mean, snow_ground_max, ");
        query.append(
                "to_char(snow_ground_date1, 'yyyy-MM-dd') as snow_ground_date1, to_char(snow_ground_date2, 'yyyy-MM-dd') as snow_ground_date2, ");
        query.append(
                "to_char(snow_ground_date3, 'yyyy-MM-dd') as snow_ground_date3, num_snow_ge_tr, num_snow_ge_1, ");
        query.append("num_snow_ge_s1, ");
        query.append(
                "avg_wind_spd, result_wind_dir, result_wind_spd, max_wind_spd, ");
        query.append("max_wind_dir1, max_wind_dir2, max_wind_dir3, ");
        query.append(
                "to_char(max_wind_date1, 'yyyy-MM-dd') as max_wind_date1, to_char(max_wind_date2, 'yyyy-MM-dd') as max_wind_date2, ");
        query.append(
                "to_char(max_wind_date3, 'yyyy-MM-dd') as max_wind_date3, max_gust_spd, max_gust_dir1, max_gust_dir2, ");
        query.append(
                "max_gust_dir3, to_char(max_gust_date1, 'yyyy-MM-dd') as max_gust_date1, ");
        query.append(
                "to_char(max_gust_date2, 'yyyy-MM-dd') as max_gust_date2, to_char(max_gust_date3, 'yyyy-MM-dd') as max_gust_date3, ");
        query.append(
                "poss_sun, mean_sky_cover, mean_rh, num_fair, num_pc, num_mc, ");
        query.append(
                "num_t, num_p, num_rrr, num_rr, num_r, num_zrr, num_zr, num_a, num_sss, num_ss, ");
        query.append("num_s, num_ip, num_f, num_fquarter, num_h, ");
        // period data methods (QC values)
        query.append(
                "max_temp_qc, avg_max_temp_qc, mean_temp_qc, min_temp_qc, avg_min_temp_qc, ");
        query.append(
                "max_temp_ge90_qc, max_temp_le32_qc, min_temp_le32_qc, min_temp_le0_qc, precip_max_24h_qc, ");
        query.append(
                "precip_ge01_qc, precip_ge10_qc, precip_ge50_qc, precip_ge100_qc, snow_max_24h_qc, ");
        query.append(
                "max_depth_qc, heat_qc, cool_qc, poss_sun_qc, fair_days_qc, pc_days_qc, mc_days_qc ");
        query.append("FROM ");
        query.append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
        query.append(" WHERE inform_id = :iStationID")
                .append(" AND period_type = :type");
        query.append(" AND period_start = :startDate");
        query.append(" AND period_end = :endDate");

        paramMap.put("iStationID", iStationID);
        paramMap.put("type", iType.getValue());
        paramMap.put("startDate",
                periodDates.getStart().getCalendarFromClimateDate());
        paramMap.put("endDate",
                periodDates.getEnd().getCalendarFromClimateDate());

        return query.toString();
    }

    /**
     * insert into climate_period table
     * 
     * @param stationId
     * @param normStartYear
     * @param normEndYear
     * @param recordStartYear
     * @param recordEndYear
     * @return
     * @throws ClimateQueryException
     */
    private boolean insertClimatePeriodSupport(int stationId, int normStartYear,
            int normEndYear, int recordStartYear, int recordEndYear)
            throws ClimateQueryException {

        StringBuilder insertStatement = new StringBuilder("INSERT INTO ");
        insertStatement.append(ClimateDAOValues.CLIMATE_PERIOD_TABLE_NAME);
        insertStatement.append(
                " (station_id,normal_start,normal_end,record_start,record_end)");
        insertStatement.append(
                "VALUES (:station_id,:normal_start,:normal_end,:record_start,:record_end)");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("station_id", stationId);

        if (normStartYear <= 0) {
            normStartYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("normal_start", normStartYear);

        if (normEndYear <= 0) {
            normEndYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("normal_end", normEndYear);

        if (recordStartYear <= 0) {
            recordStartYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("record_start", recordStartYear);

        if (recordEndYear <= 0) {
            recordEndYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("record_end", recordEndYear);

        boolean isInserted = false;
        try {
            int numRow = getDao().executeSQLUpdate(insertStatement.toString(),
                    paramMap);
            isInserted = (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to insert table "
                    + ClimateDAOValues.CLIMATE_PERIOD_TABLE_NAME + ". Query=["
                    + insertStatement + "] and map: [" + paramMap + "]", e);
        }

        return isInserted;
    }

    /**
     * update climate_period table
     * 
     * @param stationId
     * @param normStartYear
     * @param normEndYear
     * @param recordStartYear
     * @param recordEndYear
     * @return
     * @throws ClimateQueryException
     */
    private boolean updateClimatePeriodSupport(int stationId, int normStartYear,
            int normEndYear, int recordStartYear, int recordEndYear)
            throws ClimateQueryException {
        StringBuilder updateStatement = new StringBuilder("UPDATE ");
        updateStatement.append(ClimateDAOValues.CLIMATE_PERIOD_TABLE_NAME);
        updateStatement.append(
                " SET normal_start=:normal_start,normal_end=:normal_end");
        updateStatement
                .append(",record_start=:record_start,record_end=:record_end");
        updateStatement.append(" WHERE station_id=:station_id");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("station_id", stationId);

        if (normStartYear <= 0) {
            normStartYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("normal_start", normStartYear);

        if (normEndYear <= 0) {
            normEndYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("normal_end", normEndYear);

        if (recordStartYear <= 0) {
            recordStartYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("record_start", recordStartYear);

        if (recordEndYear <= 0) {
            recordEndYear = ParameterFormatClimate.MISSING;
        }
        paramMap.put("record_end", recordEndYear);

        boolean isUpdated = false;
        try {
            int numRow = getDao().executeSQLUpdate(updateStatement.toString(),
                    paramMap);
            isUpdated = (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to update table "
                    + ClimateDAOValues.CLIMATE_PERIOD_TABLE_NAME + ". Query=["
                    + updateStatement + "] and map: [" + paramMap + "]", e);
        }

        return isUpdated;
    }

    /**
     * update climate_period table
     * 
     * @param stationId
     * @param normStartYear
     * @param normEndYear
     * @param recordStartYear
     * @param recordEndYear
     * @return
     * @throws ClimateQueryException
     */
    public boolean updateClimatePeriod(int stationId, int normStartYear,
            int normEndYear, int recordStartYear, int recordEndYear)
            throws ClimateQueryException {
        StringBuilder selectQuery = new StringBuilder("SELECT * FROM ");
        selectQuery.append(ClimateDAOValues.CLIMATE_PERIOD_TABLE_NAME);
        // conditions
        selectQuery.append(" WHERE station_id= :stationID");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationID", stationId);

        Object[] results;
        try {
            results = getDao().executeSQLQuery(selectQuery.toString(),
                    paramMap);
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + selectQuery
                    + "] and map: [" + paramMap + "]", e);
        }

        if ((results != null) && (results.length >= 1)) {
            // data exists for station, update
            updateClimatePeriodSupport(stationId, normStartYear, normEndYear,
                    recordStartYear, recordEndYear);
        } else {
            // data does not exist for station, insert
            insertClimatePeriodSupport(stationId, normStartYear, normEndYear,
                    recordStartYear, recordEndYear);
        }
        return true;
    }

    /**
     * Fetch a record from climate_period table
     * 
     * @param stationId
     * @return missing values if no record found.
     * @throws ClimateQueryException
     */
    public int[] fetchClimatePeriod(int stationId)
            throws ClimateQueryException {
        int[] years = new int[] { ParameterFormatClimate.MISSING,
                ParameterFormatClimate.MISSING, ParameterFormatClimate.MISSING,
                ParameterFormatClimate.MISSING };

        StringBuilder sql = new StringBuilder(
                "SELECT normal_start, normal_end, record_start, record_end FROM ");
        sql.append(ClimateDAOValues.CLIMATE_PERIOD_TABLE_NAME);
        sql.append(" WHERE station_id= :stationId");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if (results != null && results.length >= 1) {
                // expect one only
                Object result = results[0];
                if (result instanceof Object[]) {
                    Object[] rowData = (Object[]) result;
                    try {
                        // any values could be null
                        if (rowData[0] != null) {
                            years[0] = ((Number) rowData[0]).shortValue();
                        }
                        if (rowData[1] != null) {
                            years[1] = ((Number) rowData[1]).shortValue();
                        }
                        if (rowData[2] != null) {
                            years[2] = ((Number) rowData[2]).shortValue();
                        }
                        if (rowData[3] != null) {
                            years[3] = ((Number) rowData[3]).shortValue();
                        }
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Unexpected return column type from fetchClimatePeriod query: ["
                                        + sql + "] and map: [" + paramMap + "]",
                                e);
                    }
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from fetchClimatePeriod query, expected Object[], got "
                                    + result.getClass().getName());
                }
            }
        } catch (Exception e) {
            throw new ClimateQueryException("Error querying with: [" + sql
                    + "] and map: [" + paramMap + "]", e);
        }

        return years;
    }

    /**
     * Period execution is running automatically. Do final common calculations
     * before moving on to the next step. Logic from c_display_climate.c.
     * 
     * @param periodType
     *            period type.
     * @param startDate
     *            start date of report.
     * @param endDate
     *            end date of report.
     * @param dataMap
     *            data of report.
     * @throws ClimateInvalidParameterException
     * @throws ClimateSessionException
     */
    public void processDisplayFinalization(PeriodType periodType,
            ClimateDate startDate, ClimateDate endDate,
            HashMap<Integer, ClimatePeriodReportData> dataMap)
            throws ClimateInvalidParameterException, ClimateSessionException {
        /*
         * for compiling, needed to declare empty map and set here rather than
         * in-line
         */
        HashMap<Integer, ClimatePeriodReportData> emptyMap = new HashMap<>();
        Set<Integer> emptySet = new HashSet<>();

        processDisplayFinalization(periodType, startDate, endDate, dataMap,
                emptyMap, emptySet);
    }

    /**
     * Either Period execution is running automatically, or it is manual and
     * user has finished working with the Display module. Do final common
     * calculations before moving on to the next step. Logic from
     * c_display_climate.c.
     * 
     * @param periodType
     *            period type.
     * @param startDate
     *            start date of report.
     * @param endDate
     *            end date of report.
     * @param originalDataMap
     *            original data of report (not changed by user). Used for
     *            potential MSM overwrite of Daily DB data for each station.
     * @param savedData
     *            data saved in GUI.
     * @param msmOverwriteApproved
     *            station IDs approved in GUI by the user to allow overwrite of
     *            conflicting Daily DB data with MSM data.
     * @throws ClimateInvalidParameterException
     * @throws ClimateSessionException
     */
    public void processDisplayFinalization(PeriodType periodType,
            ClimateDate startDate, ClimateDate endDate,
            Map<Integer, ClimatePeriodReportData> originalDataMap,
            Map<Integer, ClimatePeriodReportData> savedData,
            Set<Integer> msmOverwriteApproved)
            throws ClimateInvalidParameterException, ClimateSessionException {
        // MSM data
        HashMap<Integer, PeriodData> msmPeriodDataByStation = new HashMap<>();

        // Daily climate DAO, for Daily DB overwrite with MSM
        DailyClimateDAO dailyClimateDAO = new DailyClimateDAO();

        // Freeze dates DAO, for freeze date updating
        ClimateFreezeDatesDAO freezeDatesDAO = new ClimateFreezeDatesDAO();

        // climate period norm DAO for updating norm records
        ClimatePeriodNormDAO climatePeriodNormDAO = new ClimatePeriodNormDAO();

        /*
         * loop through data map and saved values; any station present in the
         * climate creator data map but not in the saved values should be loaded
         * up programmatically and saved to the saved values map, since the data
         * loaded from ASOS is by default preferred over the Climate Creator
         * data but users should not be forced to go through all stations to
         * load data if they do not want to. Same process is also needed for
         * headless execution.
         */
        for (Entry<Integer, ClimatePeriodReportData> dataMapEntry : originalDataMap
                .entrySet()) {
            int stationID = dataMapEntry.getKey();
            ClimatePeriodReportData reportData = dataMapEntry.getValue();
            PeriodData mapData = reportData.getData();

            /*
             * At this point, the data map entry still contains the original
             * data from Climate Creator (Daily DB). So this is the best place
             * to check if MSM overwrite is desired in Daily DB. Applies only to
             * monthly.
             */
            if ((PeriodType.MONTHLY_NWWS.equals(periodType)
                    || PeriodType.MONTHLY_RAD.equals(periodType))
                    && msmOverwriteApproved.contains(stationID)) {
                /*
                 * Make Daily DB updates as was desired by user for each
                 * station. Legacy asks the mismatch question in general for
                 * everything, not for each station, though it was enhanced to
                 * be per station after code migration.
                 */
                try {
                    // query for MSM data if not in map
                    getMSMByPeriodType(periodType, startDate, endDate,
                            msmPeriodDataByStation, stationID,
                            originalDataMap.get(stationID));

                    PeriodData msmData = msmPeriodDataByStation.get(stationID);

                    /*
                     * override the daily values with monthly values, where
                     * monthly is not missing
                     */
                    try {
                        if ((msmData
                                .getMaxTemp() != ParameterFormatClimate.MISSING)) {
                            // overwrite daily max temp values
                            dailyClimateDAO.updateDailyDataForMaxTemp(stationID,
                                    msmData.getMaxTemp(),
                                    msmData.getDayMaxTempList());
                        }

                        if ((msmData
                                .getMinTemp() != ParameterFormatClimate.MISSING)) {
                            // overwrite daily min temp values
                            dailyClimateDAO.updateDailyDataForMinTemp(stationID,
                                    msmData.getMinTemp(),
                                    msmData.getDayMinTempList());
                        }

                        if ((msmData
                                .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW)) {
                            // overwrite daily max snow on ground values
                            dailyClimateDAO.updateDailyDataForMaxSnow(stationID,
                                    msmData.getSnowGroundMax(),
                                    msmData.getSnowGroundMaxDateList());
                        }
                    } catch (ClimateQueryException e) {
                        throw new ClimateSessionException(
                                "Could not overwrite Daily DB values with MSM values for station ID ["
                                        + stationID + "].",
                                e);
                    }
                } catch (ClimateQueryException e) {
                    throw new ClimateSessionException(
                            "Could not get monthly ASOS data for station ID ["
                                    + stationID + "]",
                            e);
                }
            }

            if (savedData.containsKey(stationID)) {
                /*
                 * save data from saved values map into the climate creator
                 * report data map, as these values are needed in the next
                 * workflow step.
                 */
                /*
                 * Since #saveValues in the GUI starts with the data map entry
                 * as a base and overwrites values from there, there is no
                 * danger of loss of data not present for overwrite in the GUI.
                 */
                originalDataMap.get(stationID)
                        .setData(savedData.get(stationID).getData());
            } else if (PeriodType.MONTHLY_NWWS.equals(periodType)
                    || PeriodType.MONTHLY_RAD.equals(periodType)) {
                /*
                 * No saved data. Need to get ASOS monthly data since this is
                 * preferred over Climate Creator data. Applies only to monthly.
                 */

                try {
                    // monthly ASOS query
                    getMSMByPeriodType(periodType, startDate, endDate,
                            msmPeriodDataByStation, stationID, reportData);

                    PeriodData msmData = msmPeriodDataByStation.get(stationID);

                    if (msmData
                            .getMaxTemp() != ParameterFormatClimate.MISSING) {
                        mapData.setMaxTemp(msmData.getMaxTemp());
                        mapData.setDayMaxTempList(msmData.getDayMaxTempList());
                    }

                    if (msmData
                            .getMinTemp() != ParameterFormatClimate.MISSING) {
                        mapData.setMinTemp(msmData.getMinTemp());
                        mapData.setDayMinTempList(msmData.getDayMinTempList());
                    }

                    if (msmData
                            .getMaxTempMean() != (float) ParameterFormatClimate.MISSING) {
                        mapData.setMaxTempMean(msmData.getMaxTempMean());
                    }

                    if (msmData
                            .getMinTempMean() != (float) ParameterFormatClimate.MISSING) {
                        mapData.setMinTempMean(msmData.getMinTempMean());
                    }

                    if (msmData
                            .getMeanTemp() != (float) ParameterFormatClimate.MISSING) {
                        mapData.setMeanTemp(msmData.getMeanTemp());
                    }

                    if (msmData
                            .getNumMaxLessThan32F() != ParameterFormatClimate.MISSING) {
                        mapData.setNumMaxLessThan32F(
                                msmData.getNumMaxLessThan32F());
                    }

                    if (msmData
                            .getNumMaxGreaterThan90F() != ParameterFormatClimate.MISSING) {
                        mapData.setNumMaxGreaterThan90F(
                                msmData.getNumMaxGreaterThan90F());
                    }

                    if (msmData
                            .getNumMinLessThan32F() != ParameterFormatClimate.MISSING) {
                        mapData.setNumMinLessThan32F(
                                msmData.getNumMinLessThan32F());
                    }

                    if (msmData
                            .getNumMinLessThan0F() != ParameterFormatClimate.MISSING) {
                        mapData.setNumMinLessThan0F(
                                msmData.getNumMinLessThan0F());
                    }

                    if (msmData
                            .getNumHeatTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                        mapData.setNumHeatTotal(msmData.getNumHeatTotal());
                    }

                    if (msmData
                            .getNumCoolTotal() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                        mapData.setNumCoolTotal(msmData.getNumCoolTotal());
                    }

                    if (msmData
                            .getPrecipTotal() != ParameterFormatClimate.MISSING_PRECIP) {
                        mapData.setPrecipTotal(msmData.getPrecipTotal());
                    }

                    if (msmData
                            .getNumPrcpGreaterThan01() != ParameterFormatClimate.MISSING) {
                        mapData.setNumPrcpGreaterThan01(
                                msmData.getNumPrcpGreaterThan01());
                    }

                    if (msmData
                            .getNumPrcpGreaterThan10() != ParameterFormatClimate.MISSING) {
                        mapData.setNumPrcpGreaterThan10(
                                msmData.getNumPrcpGreaterThan10());
                    }

                    if (msmData
                            .getNumPrcpGreaterThan50() != ParameterFormatClimate.MISSING) {
                        mapData.setNumPrcpGreaterThan50(
                                msmData.getNumPrcpGreaterThan50());
                    }

                    if (msmData
                            .getNumPrcpGreaterThan100() != ParameterFormatClimate.MISSING) {
                        mapData.setNumPrcpGreaterThan100(
                                msmData.getNumPrcpGreaterThan100());
                    }

                    if (msmData
                            .getPrecipMax24H() != ParameterFormatClimate.MISSING_PRECIP) {
                        mapData.setPrecipMax24H(msmData.getPrecipMax24H());
                        mapData.setPrecip24HDates(msmData.getPrecip24HDates());
                    }

                    if (msmData
                            .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW) {
                        mapData.setSnowGroundMax(msmData.getSnowGroundMax());
                        mapData.setSnowGroundMaxDateList(
                                msmData.getSnowGroundMaxDateList());
                    }

                    if (msmData
                            .getSnowMax24H() != ParameterFormatClimate.MISSING_SNOW) {
                        mapData.setSnowMax24H(msmData.getSnowMax24H());
                        mapData.setSnow24HDates(msmData.getSnow24HDates());
                    }

                    if (msmData
                            .getPossSun() != ParameterFormatClimate.MISSING) {
                        mapData.setPossSun(msmData.getPossSun());
                    }

                    if (msmData
                            .getNumFairDays() != ParameterFormatClimate.MISSING) {
                        mapData.setNumFairDays(msmData.getNumFairDays());
                    }

                    if (msmData
                            .getNumPartlyCloudyDays() != ParameterFormatClimate.MISSING) {
                        mapData.setNumPartlyCloudyDays(
                                msmData.getNumPartlyCloudyDays());
                    }

                    if (msmData
                            .getNumMostlyCloudyDays() != ParameterFormatClimate.MISSING) {
                        mapData.setNumMostlyCloudyDays(
                                msmData.getNumMostlyCloudyDays());
                    }
                } catch (ClimateQueryException e) {
                    throw new ClimateSessionException(
                            "Could not get monthly ASOS data for station ID ["
                                    + stationID + "]",
                            e);
                }
            }
        }
        /*
         * Save data into database (update_period_db) if this was a whole period
         * (this functionality is only accessible if the period was whole).
         */
        for (Entry<Integer, ClimatePeriodReportData> reportDataEntry : originalDataMap
                .entrySet()) {
            int stationID = reportDataEntry.getKey();
            PeriodData mapData = reportDataEntry.getValue().getData();
            try {
                updatePeriodData(stationID,
                        new ClimateDates(startDate, endDate), periodType,
                        mapData);
            } catch (Exception e) {
                throw new ClimateSessionException(
                        "Error committing Display finalization updates for station ID ["
                                + stationID + "] from date ["
                                + startDate.toFullDateString() + "] to ["
                                + endDate.toFullDateString() + "]",
                        e);
            }
        }
        /*
         * Update freeze dates if necessary (from det_freeze.c).
         */
        determineFreeze(periodType, endDate, originalDataMap, freezeDatesDAO);

        /*
         * Check for new or tied records (check_period_records).
         */
        for (ClimatePeriodReportData reportData : originalDataMap.values()) {
            try {
                climatePeriodNormDAO.compareUpdatePeriodRecords(periodType,
                        endDate, reportData.getData());
            } catch (ClimateQueryException e) {
                throw new ClimateSessionException(
                        "Error checking for new extreme records for station ID ["
                                + reportData.getData().getInformId()
                                + "] from date [" + startDate.toFullDateString()
                                + "] to [" + endDate.toFullDateString() + "]",
                        e);
            }
        }
    }

    /**
     * Migrated from det_freeze.c.
     * 
     * <pre>
     * void det_freeze (    int         num_stations,
    *              int         itype,
    *              climate_date        report_end_date,
    *              period_data     *data)
    *
    *   Doug Murphy        PRC/TDL             January 2000
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *  This function updates freeze dates for a station in the freeze date
    *  database table and data structure if necessary.
     * </pre>
     * 
     * @param periodType
     * @param endDate
     * @param dataMap
     * @param freezeDatesDAO
     * @throws ClimateSessionException
     */
    private void determineFreeze(PeriodType periodType, ClimateDate endDate,
            Map<Integer, ClimatePeriodReportData> dataMap,
            ClimateFreezeDatesDAO freezeDatesDAO)
            throws ClimateSessionException {
        for (Entry<Integer, ClimatePeriodReportData> reportDataEntry : dataMap
                .entrySet()) {
            int stationID = reportDataEntry.getKey();
            PeriodData mapData = reportDataEntry.getValue().getData();

            /*
             * Legacy documentation:
             * 
             * Update db table and retrieve freeze dates if current dates are
             * not the earliest or the latest
             */
            try {
                ClimateDates newFreezeDates = freezeDatesDAO.updateFreezeDB(1,
                        stationID, new ClimateDates(mapData.getEarlyFreeze(),
                                mapData.getLateFreeze()));

                mapData.setEarlyFreeze(newFreezeDates.getStart());
                mapData.setLateFreeze(newFreezeDates.getEnd());
            } catch (ClimateQueryException e) {
                throw new ClimateSessionException(
                        "Error updating freeze dates for station ID ["
                                + stationID + "] and end date ["
                                + endDate.toFullDateString() + "].",
                        e);
            }

            /*
             * Legacy documentation:
             * 
             * Reset the db table for the new winter season if necessary
             */
            if ((endDate.getMon() == 7)
                    && (endDate.getYear() != mapData.getEarlyFreeze().getYear())
                    && (endDate.getYear() == mapData.getLateFreeze()
                            .getYear())) {
                try {
                    freezeDatesDAO.updateFreezeDB(4, stationID,
                            ClimateDates.getMissingClimateDates());
                } catch (ClimateQueryException e) {
                    throw new ClimateSessionException(
                            "Error resetting freeze dates for station ID ["
                                    + stationID + "] and end date ["
                                    + endDate.toFullDateString() + "].",
                            e);
                }
            }

            /*
             * Legacy documentation:
             * 
             * Only want the first freeze to appear in the month it occurred.
             * Want both the first and last freeze to appear in the June monthly
             * report only. Next section sets the date(s) to missing if these
             * requirements are not met.
             */
            if (!PeriodType.MONTHLY_RAD.equals(periodType)
                    || (endDate.getMon() != 6)) {
                mapData.setLateFreeze(ClimateDate.getMissingClimateDate());
            }

            if (!PeriodType.MONTHLY_RAD.equals(periodType)
                    || ((endDate.getMon() != mapData.getEarlyFreeze().getMon())
                            && (endDate.getMon() != 6))) {
                mapData.setEarlyFreeze(ClimateDate.getMissingClimateDate());
            }
        }
    }

    /**
     * Get MSM data and put it in the input map, if it is not already in the
     * map.
     * 
     * @param periodType
     * @param startDate
     * @param endDate
     * @param msmPeriodDataByStation
     * @param stationID
     * @param data
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private void getMSMByPeriodType(PeriodType periodType,
            ClimateDate startDate, ClimateDate endDate,
            HashMap<Integer, PeriodData> msmPeriodDataByStation, int stationID,
            ClimatePeriodReportData data)
            throws ClimateQueryException, ClimateInvalidParameterException {
        if (!msmPeriodDataByStation.containsKey(stationID)) {
            switch (periodType) {
            case MONTHLY_NWWS:
            case MONTHLY_RAD:
                // regular monthly ASOS
                msmPeriodDataByStation.put(stationID,
                        getMonthlyASOS(data.getStation().getIcaoId(),
                                startDate.getMon(), startDate.getYear()));
                break;
            case SEASONAL_NWWS:
            case SEASONAL_RAD:
            case ANNUAL_NWWS:
            case ANNUAL_RAD:
                // monthly ASOS for seasonal or annual period
                msmPeriodDataByStation.put(stationID,
                        getPeriodFromMonthlyASOS(data.getStation().getIcaoId(),
                                new ClimateDates(startDate, endDate)));
                break;
            default:
                throw new ClimateInvalidParameterException(
                        "Invalid period type for calculations: " + periodType);
            }
        }
    }
}

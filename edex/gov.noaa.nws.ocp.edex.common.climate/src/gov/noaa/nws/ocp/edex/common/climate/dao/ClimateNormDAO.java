/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

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
 * Nov 12, 2015            xzhang      Initial creation
 * MAY 23 2016  18384      amoore      Fix Daily Display SQL query
 * 07 JUL 2016  16962      amoore      Fix Daily Display SQL query
 * 13 JUL 2016  20414      amoore      Cleaning up DAO's C-structure implementation.
 * 20 JUL 2016  20591      amoore      Expect and handle some null values in sumHisHeat query.
 * 19 AUG 2016  20753      amoore      EDEX common utils consolidation and cleanup.
 * 01 OCT 2016  20635      wkwock      Add method deleteClimateDayRecord and getAvailableDayOfYear
 * 14 OCT 2016  20536      wkwock      Add updateClimateDayRecordNoMissing
 * 25 NOV 2016  20636      wpaintsil   Add compareUpdateDailyRecords and compareUpdatePeriodRecords
 * 19 DEC 2016  27015      amoore      Backend cleanup.
 * 22 DEC 2016  20772      amoore      Move max days per month array to Climate Norm DAO, since
 *                                     that is the only applicable place for it. Some code cleanup.
 * 27 DEC 2016  22450      amoore      Fix query bug.
 * 12 JAN 2017  26411      wkwock      Added saveClimateDayRecord method
 * 26 JAN 2017  27017      amoore      Fixed null pointer issue.
 * 17 FEB 2017  28609      amoore      Fixed query bug with quotations.
 * 10 MAR 2017  27420      amoore      Fix casting issue.
 * 16 MAR 2017  30162      amoore      Fix logging. Use modern Java standards.
 * 20 MAR 2017  20632      amoore      Handle null DB values.
 * 31 MAR 2017  30166      amoore      More null DB values.
 * 12 APR 2017  30171      amoore      Clean up methods and messages.
 * 19 APR 2017  33104      amoore      Use query maps now that DB issue is fixed.
 * 20 APR 2017  30166      amoore      Clarify comments. Fix parameterization error.
 * 24 APR 2017  33104      amoore      Use query maps for update/insert/delete.
 * 03 MAY 2017  33104      amoore      More query map replacements. Use abstract maps.
 * 04 MAY 2017  33104      amoore      Query bug fixes.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 23 MAY 2017  33104      amoore      Address data discrepancies, fix Legacy logic.
 * 14 JUN 2017  35175      amoore      Remove redundant logic.
 * 21 JUN 2017  35179      amoore      Fix historical precip logic.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateNormDAO extends ClimateDAO {
    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateNormDAO.class);

    /**
     * Maximum days per month. Different from regular calculation for max days
     * in a particular month because this array is for use in calculating
     * normals, and so the maximum days in February is always 29 rather than
     * depending on whether or not it is a leap year.
     */
    private static final int[] MAX_DAYS_PER_MONTH = { 31, 29, 31, 30, 31, 30,
            31, 31, 30, 31, 30, 31 };

    /**
     * Constructor.
     */
    public ClimateNormDAO() {
        super();
    }

    /**
     * Converted from get_his_norms.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     *     void get_his_norms ( const climate_date         &climo_date,  
     *                                 long                 *station_id,
     *                                 climate_record_day   *y_climate   )
     *    
     *    
     * Dan Zipper        PRC/TDL             HP 9000/7xx
     * 
     * FUNCTION DESCRIPTION
     * ====================
     * 
     * This function will retrieve historical normals (i.e. records and averages)
     * from the Informix database and store them in data structures referenced as 
     * y_climate (yesterday's climate) and t_climate (today's_climate).
     * 
     * VARIABLES
     * =========
     * 
     * name                  description
     * -------------------------------------------------------------------------------                  
     *  Input
     *      climo_date        - derived TYPE that contains valid date for
     *                          this climate summary retrieval
     *      station_id        - derived TYPE which contains the station ids
     *                          and plain language stations names for the
     *                          stations in this climate summary
     *  Output
     *      y_climate         - derived TYPE that contains historical climate data
     *                          for this date 
     *  Local
     *      db_status         L  The success/failure code returned by
     *                           dbUtils and Informix functions.
     *  Code Changes
     *  David T. Miller   January 1999   Moved assignment of structure variables
     *                               for the day_climate_norm table before the call
     *                           to the mon_climate_norm table
     *                    December 30 1999  Removed the SQL to retrieve from the monthly
     *                                      table as it's handled by another routine
     *                    July 2000      Added mean temperature and also null database 
     *                                   checks.
     *  Bob Morris        Dec 2002       - Changed date args to reference variables
     *                                   to fix seg. faults under Linux.
     *  Bob Morris        3/25/03        Fixed arguments in calls to risnull, need
     *                                   to use defined constants for C-data 
     *                                   types, not values (hard-coded, no less!)
     *                                   for SQL data types.  OB2
     *  Manan Dalal       Jan, 2005      Ported from Informix to Postgresql - OB6
     * </pre>
     * 
     * @param iDate
     *            date to query for.
     * @param stationId
     *            ID to query for.
     * @throws ClimateQueryException
     *             on error getting data.
     * @return {@link ClimateRecordDay} instance, which may have missing data
     *         (except for station ID).
     */
    public ClimateRecordDay getHistoricalNorms(ClimateDate iDate, int stationId)
            throws ClimateQueryException {
        ClimateRecordDay yClimate = ClimateRecordDay
                .getMissingClimateRecordDay();
        yClimate.setInformId(stationId);

        StringBuilder query = new StringBuilder(
                "SELECT mean_temp, max_temp_mean, min_temp_mean,");
        query.append(" max_temp_record, min_temp_record, max_temp_rec_yr1,");
        query.append(" max_temp_rec_yr2, max_temp_rec_yr3, min_temp_rec_yr1,");
        query.append(" min_temp_rec_yr2, min_temp_rec_yr3, precip_mean,");
        query.append(
                " precip_day_max, precip_day_max_yr1, precip_day_max_yr2,");
        query.append(" precip_day_max_yr3, snow_mean, snow_day_max,");
        query.append(" snow_day_max_yr1, snow_day_max_yr2, snow_day_max_yr3,");
        query.append(" snow_ground_mean, heat_day_mean, cool_day_mean FROM ");
        query.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        query.append(" WHERE station_id = :stationId")
                .append(" AND day_of_year = ");
        query.append(":dayOfYear");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("dayOfYear", iDate.toMonthDayDateString());

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        Object[] oa = (Object[]) result;

                        // any values could be null
                        float ecMeanTemp = oa[0] != null ? (float) oa[0]
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempMean = oa[1] != null ? (short) oa[1]
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempMean = oa[2] != null ? (short) oa[2]
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecord = oa[3] != null ? (short) oa[3]
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecord = oa[4] != null ? (short) oa[4]
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecordYear1 = oa[5] != null
                                ? (short) oa[5]
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecordYear2 = oa[6] != null
                                ? (short) oa[6]
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecordYear3 = oa[7] != null
                                ? (short) oa[7]
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecordYear1 = oa[8] != null
                                ? (short) oa[8]
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecordYear2 = oa[9] != null
                                ? (short) oa[9]
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecordYear3 = oa[10] != null
                                ? (short) oa[10]
                                : ParameterFormatClimate.MISSING;
                        float ecPrecipMean = oa[11] != null ? (float) oa[11]
                                : ParameterFormatClimate.MISSING;
                        float ecPrecipDayRecord = oa[12] != null
                                ? (float) oa[12]
                                : ParameterFormatClimate.MISSING;
                        short ecPrecipDayRecordYear1 = oa[13] != null
                                ? (short) oa[13]
                                : ParameterFormatClimate.MISSING;
                        short ecPrecipDayRecordYear2 = oa[14] != null
                                ? (short) oa[14]
                                : ParameterFormatClimate.MISSING;
                        short ecPrecipDayRecordYear3 = oa[15] != null
                                ? (short) oa[15]
                                : ParameterFormatClimate.MISSING;
                        float ecSnowDayMean = oa[16] != null ? (float) oa[16]
                                : ParameterFormatClimate.MISSING;
                        float ecSnowDayRecord = oa[17] != null ? (float) oa[17]
                                : ParameterFormatClimate.MISSING;
                        short ecSnowDayRecordYear1 = oa[18] != null
                                ? (short) oa[18]
                                : ParameterFormatClimate.MISSING;
                        short ecSnowDayRecordYear2 = oa[19] != null
                                ? (short) oa[19]
                                : ParameterFormatClimate.MISSING;
                        short ecSnowDayRecordYear3 = oa[20] != null
                                ? (short) oa[20]
                                : ParameterFormatClimate.MISSING;
                        /* unused */
                        @SuppressWarnings("unused")
                        float ecSnowGroundMean = oa[21] != null ? (float) oa[21]
                                : ParameterFormatClimate.MISSING;
                        int ecNumHeatMean = oa[22] != null ? (int) oa[22]
                                : ParameterFormatClimate.MISSING;
                        int ecNumCoolMean = oa[23] != null ? (int) oa[23]
                                : ParameterFormatClimate.MISSING;

                        yClimate.setMaxTempYear(new int[] {
                                ecMaxTempRecordYear1, ecMaxTempRecordYear2,
                                ecMaxTempRecordYear3 });
                        yClimate.setMinTempYear(new int[] {
                                ecMinTempRecordYear1, ecMinTempRecordYear2,
                                ecMinTempRecordYear3 });
                        yClimate.setPrecipDayRecordYear(new int[] {
                                ecPrecipDayRecordYear1, ecPrecipDayRecordYear2,
                                ecPrecipDayRecordYear3 });
                        yClimate.setSnowDayRecordYear(new int[] {
                                ecSnowDayRecordYear1, ecSnowDayRecordYear2,
                                ecSnowDayRecordYear3 });

                        yClimate.setMeanTemp(ecMeanTemp);
                        yClimate.setMaxTempMean(ecMaxTempMean);
                        yClimate.setMinTempMean(ecMinTempMean);
                        yClimate.setMaxTempRecord(ecMaxTempRecord);
                        yClimate.setMinTempRecord(ecMinTempRecord);
                        yClimate.setPrecipMean(ecPrecipMean);
                        yClimate.setPrecipDayRecord(ecPrecipDayRecord);
                        yClimate.setSnowDayMean(ecSnowDayMean);
                        yClimate.setSnowDayRecord(ecSnowDayRecord);
                        yClimate.setNumHeatMean(ecNumHeatMean);
                        yClimate.setNumCoolMean(ecNumCoolMean);

                    } catch (Exception e) {
                        // if casting failed
                        throw new ClimateQueryException(
                                "Unexpected return column type from query: ["
                                        + query + "] and map: [" + paramMap
                                        + "]",
                                e);
                    }

                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }

            } else {
                // no results
                logger.warn("No historical normals data for date: ["
                        + iDate.toMonthDayDateString() + "] and station ID: ["
                        + stationId
                        + "]. Empty or null normals query result from: ["
                        + query + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with: [" + query
                            + "] and map: [" + paramMap + "]",
                    e);
        }

        return yClimate;
    }

    /**
     * Fetch a row from day_climate_norm
     * 
     * @param stationId
     * @param dayOfYear
     * @return
     * @throws ClimateQueryException
     */
    public ClimateDayNorm fetchClimateDayRecord(int stationId, String dayOfYear)
            throws ClimateQueryException {
        ClimateDayNorm climateRcd = null;

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(
                "mean_temp, max_temp_record, max_temp_mean, min_temp_record, min_temp_mean,");
        sql.append("max_temp_rec_yr1, max_temp_rec_yr2, max_temp_rec_yr3,");
        sql.append("min_temp_rec_yr1, min_temp_rec_yr2, min_temp_rec_yr3,");
        sql.append("precip_mean, precip_day_max,");
        sql.append(
                "precip_day_max_yr1, precip_day_max_yr2, precip_day_max_yr3,");
        sql.append("snow_mean, snow_day_max,");
        sql.append("snow_day_max_yr1, snow_day_max_yr2, snow_day_max_yr3,");
        sql.append("snow_ground_mean, heat_day_mean, cool_day_mean");
        sql.append(" FROM ");
        sql.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationId");
        sql.append(" AND day_of_year= :dayOfYear");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("dayOfYear", dayOfYear);

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if (results != null && results.length >= 1) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    Object[] rowData = (Object[]) result; // expect one only
                    try {
                        climateRcd = new ClimateDayNorm();
                        climateRcd.setDataToMissing();
                        climateRcd.setStationId(stationId);
                        climateRcd.setDayOfYear(dayOfYear);

                        // any values could be null
                        if (rowData[0] != null) {
                            climateRcd.setMeanTemp((float) rowData[0]);
                        }
                        if (rowData[1] != null) {
                            climateRcd.setMaxTempRecord((short) rowData[1]);
                        }
                        if (rowData[2] != null) {
                            climateRcd.setMaxTempMean((short) rowData[2]);
                        }
                        if (rowData[3] != null) {
                            climateRcd.setMinTempRecord((short) rowData[3]);
                        }
                        if (rowData[4] != null) {
                            climateRcd.setMinTempMean((short) rowData[4]);
                        }

                        short[] maxTempYear = climateRcd.getMaxTempYear();
                        if (rowData[5] != null) {
                            maxTempYear[0] = (short) rowData[5];
                        }
                        if (rowData[6] != null) {
                            maxTempYear[1] = (short) rowData[6];
                        }
                        if (rowData[7] != null) {
                            maxTempYear[2] = (short) rowData[7];
                        }

                        short[] minTempYear = climateRcd.getMinTempYear();
                        if (rowData[8] != null) {
                            minTempYear[0] = (short) rowData[8];
                        }
                        if (rowData[9] != null) {
                            minTempYear[1] = (short) rowData[9];
                        }
                        if (rowData[10] != null) {
                            minTempYear[2] = (short) rowData[10];
                        }

                        if (rowData[11] != null) {
                            climateRcd.setPrecipMean((float) rowData[11]);
                        }
                        if (rowData[12] != null) {
                            climateRcd.setPrecipDayRecord((float) rowData[12]);
                        }

                        short[] precipDayRecordYear = climateRcd
                                .getPrecipDayRecordYear();
                        if (rowData[13] != null) {
                            precipDayRecordYear[0] = (short) rowData[13];
                        }
                        if (rowData[14] != null) {
                            precipDayRecordYear[1] = (short) rowData[14];
                        }
                        if (rowData[15] != null) {
                            precipDayRecordYear[2] = (short) rowData[15];
                        }

                        if (rowData[16] != null) {
                            climateRcd.setSnowDayMean((float) rowData[16]);
                        }
                        if (rowData[17] != null) {
                            climateRcd.setSnowDayRecord((float) rowData[17]);
                        }

                        short[] snowDayRecordYear = climateRcd
                                .getSnowDayRecordYear();
                        if (rowData[18] != null) {
                            snowDayRecordYear[0] = (short) rowData[18];
                        }
                        if (rowData[19] != null) {
                            snowDayRecordYear[1] = (short) rowData[19];
                        }
                        if (rowData[20] != null) {
                            snowDayRecordYear[2] = (short) rowData[20];
                        }

                        if (rowData[21] != null) {
                            climateRcd.setSnowGround((float) rowData[21]);
                        }
                        if (rowData[22] != null) {
                            climateRcd.setNumHeatMean((int) rowData[22]);
                        }
                        if (rowData[23] != null) {
                            climateRcd.setNumCoolMean((int) rowData[23]);
                        }
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Unexpected return column type from query: ["
                                        + sql + "] and map: [" + paramMap + "]",
                                e);
                    }
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }
            } else {
                logger.warn("Empty or null normals result from query: [" + sql
                        + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException("Error querying with: [" + sql
                    + "] and map: [" + paramMap + "]", e);
        }

        return climateRcd;
    }

    /**
     * Converted from get_period_hist_climo.ec
     * 
     * Original comments:
     * 
     * <pre>
     * 
     * get_period_hist_climo( )
     *   ecember 1999     David T. Miller        PRC/TDL
     *         Purpose:  retrieves the historical climatological data from the monthly,
     *           seasonal, and yearly database table
     * 
     * 
     * Variables
     * 
     *    Input  *end_date     pointer to the end date structure of the period
     *       period_type     flag to indicate month, season or year
     * 
     * 
     *    Output  *period_climo  pointer to historical period climatology data structure
     * 
     * 
     *    Local  see variable list
     * 
     * 
     *    Non-system routines used
     * 
     *    Non-system functions used
     * 
     * SUBROUTINE #1: monthly_sums
     *                  This routine sums a variable using the monthly normal
     *                  entries for the passed element.
     * SUBROUTINE #2: average_value
     *                  This routine calculates a weighted normal using a
     *                  combination of monthly and daily normal values. Used
     *                  primarily as a backup for the normal temperatures, it is
     *                  also used as a backup for normal snow depth and avg daily
     *                  precipitation.
     * 
     * MODIFICATION HISTORY
     * --------------------
     *   7/14/00   Doug Murphy      
     *                Normal temperatures changed from int to float
     *   1/17/01   Doug Murphy              
     *                Major additions allow backup calculation of norms when an 
     *                entire period is not asked for or values are missing from the
     *                mon_climate_norm... includes the addition of two generic 
     *                routines- monthly_sums and average_value which can be reused
     *                by the different variables.
     *   4/18/01   Doug Murphy
     *                Normal number of days >/< precip/temp/snow have changed from
     *                ints to floats
     *  12/19/02   Bob Morris
     *                Modified prototypes for called/changed sum_his_* routines, and
     *                removed unnecessary julday() prototype and unused ec_begin_month
     *                variable - OB2
     *  03/05/03   Bob Morris
     *                Eliminate out-of-bounds strcpy's to temp_date.  OB2 DR_12256
     *  03/25/03   Bob Morris
     *                Fix data type arguments to risnull(), should use c-data types
     *                not SQL types, and use their names, not numbers.  OB2 DR_12256
     *   1/19/05   Gary Battel/Manan Dalal
     *                 Conversion of INFORMIX to POSTGRES
     * 
     *   6/24/2007 Darnell Early
     *                 Fixed the code to handle leap year from previous years in seasonal
     *                 and monthly reports
     *                                      
     *   04/13/2011  Xiaochuan
     *         Fix the memory override problem for DR 21228. Add Initialize 
     *         statement that set \0 at the end for temp_year, temp_mon, 
     *         temp_day. The problem appeared after version OB9.2.12. 
     *         Also, change all sprintf() to snprintf().
     * </pre>
     */
    public void getPeriodHistClimo(ClimateDate beginDate, ClimateDate endDate,
            PeriodClimo periodClimo, PeriodType periodType)
                    throws ClimateQueryException {

        ClimateDate begin = ClimateDate.getMissingClimateDate();
        boolean entirePeriod = false;

        /*
         * Initialize structure, set missing data.
         */
        /* stationId */
        int ecInformId = periodClimo.getInformId();
        int ecMonth = endDate.getMon();
        String ecBegin = beginDate.toMonthDayDateString();

        String ecEnd = endDate.toMonthDayDateString();

        /* This section will determine if an entire period was selected */
        if ((beginDate.getMon() == endDate.getMon())
                && (beginDate.getDay() == 1) && (endDate
                        .getDay() == ClimateUtilities.daysInMonth(endDate))) {
            // monthly
            entirePeriod = true;
        } else if (PeriodType.SEASONAL_NWWS.equals(periodType)
                || PeriodType.SEASONAL_RAD.equals(periodType)) {
            // seasonal
            if ((((endDate.getMon() - beginDate.getMon()) == 2)
                    || ((endDate.getMon() == 2) && (beginDate.getMon() == 12)))
                    && (beginDate.getDay() == 1)
                    && (endDate.getDay() == ClimateUtilities
                            .daysInMonth(endDate))) {
                entirePeriod = true;
            }
        } else if (PeriodType.ANNUAL_NWWS.equals(periodType)
                || PeriodType.ANNUAL_RAD.equals(periodType)) {
            // annual
            if (((endDate.getMon() == beginDate.getMon())
                    && (endDate.getDay() == (beginDate.getDay() - 1)))
                    || ((endDate.getMon() == (beginDate.getMon() - 1))
                            && (endDate.getDay() == ClimateUtilities
                                    .daysInMonth(endDate))
                            && (beginDate.getDay() == 1))
                    || ((endDate.getMon() == 12) && (endDate.getDay() == 31)
                            && (beginDate.getMon() == 1)
                            && (beginDate.getDay() == 1))) {
                entirePeriod = true;
            }
        }

        int numMos;
        if (endDate.getMon() < beginDate.getMon()) {
            numMos = ((12 - beginDate.getMon()) + 1) + endDate.getMon();
        } else {
            numMos = (endDate.getMon() - beginDate.getMon()) + 1;
        }

        Map<String, Object> keyParamMap = new HashMap<>();
        /* Only use this SQL select when an entire period is specified */
        if (entirePeriod) {

            StringBuilder fullQuery = new StringBuilder(
                    "SELECT max_temp_mean, max_temp_record, day_max_temp_rec1, ");
            fullQuery.append(" day_max_temp_rec2, day_max_temp_rec3, ");
            fullQuery.append(
                    " min_temp_mean, min_temp_record, day_min_temp_rec1, ");
            fullQuery.append(" day_min_temp_rec2, day_min_temp_rec3, ");
            fullQuery.append(
                    " norm_mean_temp, norm_mean_max_temp, norm_mean_min_temp, ");
            fullQuery.append(" num_max_ge_90f, num_max_le_32f, ");
            fullQuery
                    .append(" num_min_le_32f, num_min_le_0F, precip_pd_mean, ");
            fullQuery.append(
                    " precip_pd_max, precip_pd_max_yr1, precip_pd_max_yr2, ");
            fullQuery.append(" precip_pd_max_yr3, ");
            fullQuery.append(
                    " precip_period_min, precip_pd_min_yr1, precip_pd_min_yr2, ");
            fullQuery.append(" precip_pd_min_yr3, precip_day_norm, ");
            fullQuery.append(
                    " num_prcp_ge_01, num_prcp_ge_10, num_prcp_ge_50, num_prcp_ge_100, ");
            fullQuery.append(" snow_pd_mean, snow_pd_max, snow_pd_max_yr1, ");
            fullQuery.append(" snow_pd_max_yr2, snow_pd_max_yr3, ");
            fullQuery.append(
                    " snow_24h_begin1, snow_24h_begin2, snow_24h_begin3, ");
            fullQuery.append(" snow_max_24h_rec, ");
            fullQuery.append(" snow_24h_end1, snow_24h_end2, snow_24h_end3, ");
            fullQuery.append(" snow_water_pd_norm, ");
            fullQuery.append(
                    " snow_ground_norm, snow_ground_max, day_snow_grnd_max1, ");
            fullQuery.append(" day_snow_grnd_max2, day_snow_grnd_max3, ");
            fullQuery.append(" num_snow_ge_tr, num_snow_ge_1, ");
            fullQuery.append(" heat_pd_mean, cool_pd_mean FROM ");
            fullQuery.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
            fullQuery.append(" WHERE station_id = :ec_inform_id ");
            fullQuery.append(" AND month_of_year = :ec_month ");
            fullQuery.append(" AND period_type = :ec_period_type");

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_inform_id", periodClimo.getInformId());
                keyParamMap.put("ec_month", ecMonth);
                keyParamMap.put("ec_period_type", periodType.getValue());

                Object[] results = getDao()
                        .executeSQLQuery(fullQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    Object result = results[0];
                    if (result instanceof Object[]) {
                        try {
                            Object[] oa = (Object[]) result;
                            // any values could be null
                            /* mean climo max temperature */
                            float maxTempNorm = oa[0] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[0];
                            /* record maxTemp */
                            short maxTempRecord = oa[1] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[1];
                            /* date(s) of record max temp */
                            Date dayMaxTempRecord1 = oa[2] == null ? null
                                    : (Date) oa[2];
                            Date dayMaxTempRecord2 = oa[3] == null ? null
                                    : (Date) oa[3];
                            Date dayMaxTempRecord3 = oa[4] == null ? null
                                    : (Date) oa[4];
                            /* mean climo min temp */
                            float minTempNorm = oa[5] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[5];
                            /*
                             * record min temp, degrees F
                             */
                            short minTempRecord = oa[6] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[6];
                            /* date(s) of observed minTemp */
                            Date dayMinTempRecord1 = oa[7] == null ? null
                                    : (Date) oa[7];
                            Date dayMinTempRecord2 = oa[8] == null ? null
                                    : (Date) oa[8];
                            Date dayMinTempRecord3 = oa[9] == null ? null
                                    : (Date) oa[9];
                            /* norm climo average temp */
                            float normMeanTemp = oa[10] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[10];
                            /* normal mean max temperature */
                            float normMeanMaxTemp = oa[11] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[11];
                            /* normal mean min temperature */
                            float normMeanMinTemp = oa[12] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[12];
                            /* mean # of days max temp GE 90F */
                            float normNumMaxGE90F = oa[13] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[13];
                            /* mean # of days max temp LE 32F */
                            float normNumMaxLE32F = oa[14] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[14];
                            /* mean # of days min temp LE 32F */
                            float normNumMinLE32F = oa[15] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[15];
                            /* mean # of days min temp LE 0F */
                            float normNumMinLE0F = oa[16] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[16];
                            /* mean cumulative precip this month (in.) */
                            float precipPeriodNorm = oa[17] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[17];
                            /* record precip for this month */
                            float precipPeriodMax = oa[18] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[18];
                            /* year of record monthly precip */
                            short precipPeriodMaxYear1 = oa[19] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[19];
                            short precipPeriodMaxYear2 = oa[20] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[20];
                            short precipPeriodMaxYear3 = oa[21] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[21];
                            /* record min precip for this month */
                            float precipPeriodMin = oa[22] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[22];
                            /* year of record monthly precip */
                            short precipPeriodMinYear1 = oa[23] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[23];
                            short precipPeriodMinYear2 = oa[24] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[24];
                            short precipPeriodMinYear3 = oa[25] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[25];
                            /* daily average precip */
                            float precipDayNorm = oa[26] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[26];
                            /* mean # of days precip GE .01 inches */
                            float numPrcpGE01Norm = oa[27] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[27];
                            /* mean # of days precip GE .10 inches */
                            float numPrcpGE10Norm = oa[28] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[28];
                            /* mean # of days precip GE .50 inches */
                            float numPrcpGE50Norm = oa[29] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[29];
                            /* mean # of days precip GE 1.00 inches */
                            float numPrcpGE100Norm = oa[30] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[30];
                            /* total snow for month (in.) */
                            float snowPeriodNorm = oa[31] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[31];
                            /* record snowfall for the month */
                            float snowPeriodRecord = oa[32] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[32];
                            short snowPeriodMaxYear1 = oa[33] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[33];
                            short snowPeriodMaxYear2 = oa[34] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[34];
                            short snowPeriodMaxYear3 = oa[35] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[35];
                            /* begin dates of max 24H snow */
                            Date snow24HBegin1 = oa[36] == null ? null
                                    : (Date) oa[36];
                            Date snow24HBegin2 = oa[37] == null ? null
                                    : (Date) oa[37];
                            Date snow24HBegin3 = oa[38] == null ? null
                                    : (Date) oa[38];
                            /* record max 24 hour snowfall */
                            float snowMax24HRecord = oa[39] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[39];
                            /* end dates of max 24H snow */
                            Date snow24HEnd1 = oa[40] == null ? null
                                    : (Date) oa[40];
                            Date snow24HEnd2 = oa[41] == null ? null
                                    : (Date) oa[41];
                            Date snow24HEnd3 = oa[42] == null ? null
                                    : (Date) oa[42];
                            /* normal water equivalent of snow (in.) */
                            float snowWaterPeriodNorm = oa[43] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[43];
                            /* average climo snow depth */
                            float snowGroundNorm = oa[44] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[44];
                            /* record snow depth for the month */
                            short snowGroundMax = oa[45] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (short) oa[45];
                            /* date(s) of observed snow depth */
                            Date daySnowGroundMax1 = oa[46] == null ? null
                                    : (Date) oa[46];
                            Date daySnowGroundMax2 = oa[47] == null ? null
                                    : (Date) oa[47];
                            Date daySnowGroundMax3 = oa[48] == null ? null
                                    : (Date) oa[48];
                            /* mean # of days with any snowfall */
                            float numSnowGETRNorm = oa[49] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[49];
                            /* mean # of days with snow GEinch */
                            float numSnowGE1Norm = oa[50] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (float) oa[50];
                            /* mean cumulative # of heat days */
                            int numHeatPeriodNorm = oa[51] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (int) oa[51];
                            /* mean cumulative # of cool days */
                            int numCoolPeriodNorm = oa[52] == null
                                    ? ParameterFormatClimate.MISSING
                                    : (int) oa[52];

                            periodClimo.setMonthOfYear(ecMonth);
                            periodClimo.setPeriodType(periodType);
                            periodClimo.setMaxTempNorm(maxTempNorm);
                            periodClimo.setMaxTempRecord(maxTempRecord);
                            periodClimo.setMinTempNorm(minTempNorm);
                            periodClimo.setMinTempRecord(minTempRecord);
                            periodClimo.setNormMeanTemp(normMeanTemp);
                            periodClimo.setNormMeanMaxTemp(normMeanMaxTemp);
                            periodClimo.setNormMeanMinTemp(normMeanMinTemp);
                            periodClimo.setNormNumMaxGE90F(normNumMaxGE90F);
                            periodClimo.setNormNumMaxLE32F(normNumMaxLE32F);
                            periodClimo.setNormNumMinLE32F(normNumMinLE32F);
                            periodClimo.setNormNumMinLE0F(normNumMinLE0F);
                            periodClimo.setPrecipPeriodNorm(precipPeriodNorm);
                            periodClimo.setPrecipPeriodMax(precipPeriodMax);
                            periodClimo.setPrecipPeriodMin(precipPeriodMin);
                            periodClimo.setPrecipDayNorm(precipDayNorm);
                            periodClimo.setNumPrcpGE01Norm(numPrcpGE01Norm);
                            periodClimo.setNumPrcpGE10Norm(numPrcpGE10Norm);
                            periodClimo.setNumPrcpGE50Norm(numPrcpGE50Norm);
                            periodClimo.setNumPrcpGE100Norm(numPrcpGE100Norm);
                            periodClimo.setSnowPeriodNorm(snowPeriodNorm);
                            periodClimo.setSnowPeriodRecord(snowPeriodRecord);
                            periodClimo.setSnowMax24HRecord(snowMax24HRecord);
                            periodClimo.setSnowWaterPeriodNorm(
                                    snowWaterPeriodNorm);
                            periodClimo.setSnowGroundNorm(snowGroundNorm);
                            periodClimo.setSnowGroundMax(snowGroundMax);
                            periodClimo.setNumSnowGETRNorm(numSnowGETRNorm);
                            periodClimo.setNumSnowGE1Norm(numSnowGE1Norm);
                            periodClimo.setNumHeatPeriodNorm(numHeatPeriodNorm);
                            periodClimo.setNumCoolPeriodNorm(numCoolPeriodNorm);

                            List<ClimateDate> dates = new ArrayList<>();
                            dates.add(dayMaxTempRecord1 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(dayMaxTempRecord1));
                            dates.add(dayMaxTempRecord2 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(dayMaxTempRecord2));
                            dates.add(dayMaxTempRecord3 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(dayMaxTempRecord3));
                            periodClimo.setDayMaxTempRecordList(dates);

                            dates = new ArrayList<>();
                            dates.add(dayMinTempRecord1 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(dayMinTempRecord1));
                            dates.add(dayMinTempRecord2 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(dayMinTempRecord2));
                            dates.add(dayMinTempRecord3 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(dayMinTempRecord3));
                            periodClimo.setDayMinTempRecordList(dates);

                            dates = new ArrayList<>();
                            dates.add(new ClimateDate(99, 99,
                                    precipPeriodMaxYear1));
                            dates.add(new ClimateDate(99, 99,
                                    precipPeriodMaxYear2));
                            dates.add(new ClimateDate(99, 99,
                                    precipPeriodMaxYear3));
                            periodClimo.setPrecipPeriodMaxYearList(dates);

                            dates = new ArrayList<>();
                            dates.add(new ClimateDate(99, 99,
                                    precipPeriodMinYear1));
                            dates.add(new ClimateDate(99, 99,
                                    precipPeriodMinYear2));
                            dates.add(new ClimateDate(99, 99,
                                    precipPeriodMinYear3));
                            periodClimo.setPrecipPeriodMinYearList(dates);

                            dates = new ArrayList<>();
                            dates.add(new ClimateDate(99, 99,
                                    snowPeriodMaxYear1));
                            dates.add(new ClimateDate(99, 99,
                                    snowPeriodMaxYear2));
                            dates.add(new ClimateDate(99, 99,
                                    snowPeriodMaxYear3));
                            periodClimo.setSnowPeriodMaxYearList(dates);

                            List<ClimateDates> datess = new ArrayList<>();
                            datess.add(new ClimateDates(
                                    snow24HBegin1 == null
                                            ? ClimateDate
                                                    .getMissingClimateDate()
                                            : new ClimateDate(snow24HBegin1),
                                    snow24HEnd1 == null
                                            ? ClimateDate
                                                    .getMissingClimateDate()
                                            : new ClimateDate(snow24HEnd1)));
                            datess.add(new ClimateDates(
                                    snow24HBegin2 == null
                                            ? ClimateDate
                                                    .getMissingClimateDate()
                                            : new ClimateDate(snow24HBegin2),
                                    snow24HEnd2 == null
                                            ? ClimateDate
                                                    .getMissingClimateDate()
                                            : new ClimateDate(snow24HEnd2)));
                            datess.add(new ClimateDates(
                                    snow24HBegin3 == null
                                            ? ClimateDate
                                                    .getMissingClimateDate()
                                            : new ClimateDate(snow24HBegin3),
                                    snow24HEnd3 == null
                                            ? ClimateDate
                                                    .getMissingClimateDate()
                                            : new ClimateDate(snow24HEnd3)));
                            periodClimo.setSnow24HList(datess);

                            dates = new ArrayList<>();
                            dates.add(daySnowGroundMax1 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(daySnowGroundMax1));
                            dates.add(daySnowGroundMax2 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(daySnowGroundMax2));
                            dates.add(daySnowGroundMax3 == null
                                    ? ClimateDate.getMissingClimateDate()
                                    : new ClimateDate(daySnowGroundMax3));
                            periodClimo.setDaySnowGroundMaxList(dates);

                        } catch (Exception e) { // if casting failed
                            throw new ClimateQueryException(
                                    "Unexpected return column type from getPeriodHistClimo query.",
                                    e);
                        }

                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from getPeriodHistClimo query, expected Object[], got "
                                        + result.getClass().getName());
                    }

                } else {
                    logger.warn("Null or empty historical results for query: ["
                            + fullQuery + "] using map: ["
                            + keyParamMap.toString() + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error querying the climate database period history routine: ["
                                + fullQuery + "] with map: [" + keyParamMap
                                + "]",
                        e);
            }
        }
        /************************************************************/
        /* The remaining portion of this routine will provide */
        /* backup methods for getting normal and record values if */
        /* the dates specified are not entire periods or the values */
        /* are missing in the initial retrieval. */
        /************************************************************/
        /***********************************************/
        /* The max and min record temps and dates */
        /***********************************************/

        StringBuilder maxTempQuery;
        if (periodClimo.getMaxTempRecord() == ParameterFormatClimate.MISSING) {
            if (endDate.getMon() < beginDate.getMon()) {
                maxTempQuery = new StringBuilder(
                        "SELECT MAX(max_temp_record) FROM ");
                maxTempQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                maxTempQuery.append(" WHERE (day_of_year>=:ec_begin ");
                maxTempQuery.append(" OR day_of_year<=:ec_end) ");
                maxTempQuery.append(" AND station_id = :ec_inform_id ");
                maxTempQuery.append(" AND max_temp_record!=:ec_missing_int");
            } else {
                maxTempQuery = new StringBuilder(
                        "SELECT MAX(max_temp_record) FROM ");
                maxTempQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                maxTempQuery.append(" WHERE day_of_year>=:ec_begin ");
                maxTempQuery.append(" AND day_of_year<=:ec_end ");
                maxTempQuery.append(" AND station_id = :ec_inform_id ");
                maxTempQuery.append(" AND max_temp_record!=:ec_missing_int");
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_inform_id", ecInformId);
                keyParamMap.put("ec_begin", ecBegin);
                keyParamMap.put("ec_end", ecEnd);
                keyParamMap.put("ec_missing_int",
                        ParameterFormatClimate.MISSING);

                short maxTempRecord = (short) queryForOneValue(
                        maxTempQuery.toString(), keyParamMap,
                        ParameterFormatClimate.MISSING);
                periodClimo.setMaxTempRecord(maxTempRecord);

            } catch (Exception e) {
                logger.error("Error querying the climate database max temp: ["
                        + maxTempQuery + "] with map: [" + keyParamMap + "]",
                        e);
            }

        }

        StringBuilder minTempQuery;
        if (periodClimo.getMinTempRecord() == ParameterFormatClimate.MISSING) {
            if (endDate.getMon() < beginDate.getMon()) {
                minTempQuery = new StringBuilder(
                        " SELECT MIN(min_temp_record) FROM ");
                minTempQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                minTempQuery.append(" WHERE (day_of_year>=:ec_begin ");
                minTempQuery.append(" OR day_of_year<=:ec_end) ");
                minTempQuery.append(" AND station_id = :ec_inform_id ");
                minTempQuery.append(" AND min_temp_record!=:ec_missing_int");
            } else {
                minTempQuery = new StringBuilder(
                        " SELECT MIN(min_temp_record) FROM ");
                minTempQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                minTempQuery.append(" WHERE day_of_year>=:ec_begin ");
                minTempQuery.append(" AND day_of_year<=:ec_end ");
                minTempQuery.append(" AND station_id = :ec_inform_id ");
                minTempQuery.append(" AND min_temp_record!=:ec_missing_int ");
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_inform_id", ecInformId);
                keyParamMap.put("ec_begin", ecBegin);
                keyParamMap.put("ec_end", ecEnd);
                keyParamMap.put("ec_missing_int",
                        ParameterFormatClimate.MISSING);

                short minTempRecord = (short) queryForOneValue(
                        minTempQuery.toString(), keyParamMap,
                        ParameterFormatClimate.MISSING);
                periodClimo.setMinTempRecord(minTempRecord);

            } catch (Exception e) {
                logger.error("Error querying the climate database min temp: ["
                        + minTempQuery + "] with map: [" + keyParamMap + "]",
                        e);
            }

        }

        /************************************************************/
        /* Next, if the period begins with the first of a month and */
        /* ends with the last of another month, we can sum the */
        /* monthly normals (if available) to get a norm for the */
        /* period for the following variables. Otherwise, they will */
        /* remain missing. */
        /************************************************************/
        if ((beginDate.getDay() == 1) && (endDate
                .getDay() == MAX_DAYS_PER_MONTH[endDate.getMon() - 1])) {

            if (periodClimo
                    .getNormNumMaxGE90F() == ParameterFormatClimate.MISSING) {
                periodClimo.setNormNumMaxGE90F(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_max_ge_90f"));
            }

            if (periodClimo
                    .getNormNumMaxLE32F() == ParameterFormatClimate.MISSING) {
                periodClimo.setNormNumMaxLE32F(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_max_le_32f"));
            }

            if (periodClimo
                    .getNormNumMinLE32F() == ParameterFormatClimate.MISSING) {
                periodClimo.setNormNumMinLE32F(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_min_le_32f"));
            }

            if (periodClimo
                    .getNormNumMinLE0F() == ParameterFormatClimate.MISSING) {
                periodClimo.setNormNumMinLE0F(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_min_le_0f"));
            }

            if (periodClimo
                    .getNumPrcpGE01Norm() == ParameterFormatClimate.MISSING) {
                periodClimo.setNumPrcpGE01Norm(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_prcp_ge_01"));
            }

            if (periodClimo
                    .getNumPrcpGE10Norm() == ParameterFormatClimate.MISSING) {
                periodClimo.setNumPrcpGE10Norm(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_prcp_ge_10"));
            }

            if (periodClimo
                    .getNumPrcpGE50Norm() == ParameterFormatClimate.MISSING) {
                periodClimo.setNumPrcpGE50Norm(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_prcp_ge_50"));
            }

            if (periodClimo
                    .getNumPrcpGE100Norm() == ParameterFormatClimate.MISSING) {
                periodClimo.setNumPrcpGE100Norm(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_prcp_ge_100"));
            }

            if (periodClimo
                    .getNumSnowGETRNorm() == ParameterFormatClimate.MISSING) {
                periodClimo.setNumSnowGETRNorm(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_snow_ge_tr"));
            }

            if (periodClimo
                    .getNumSnowGE1Norm() == ParameterFormatClimate.MISSING) {
                periodClimo.setNumSnowGE1Norm(monthlySums(beginDate.getMon(),
                        endDate.getMon(), numMos, periodClimo.getInformId(),
                        "num_snow_ge_1"));
            }
        }
        /********************************************************/
        /* 24-h snowfall record with dates */
        /********************************************************/
        // if(periodClimo->snowMax24HRecord!=MISSING);
        // {
        // /* HOLD OFF FOR NOW... RATHER COMPLICATED TO RETRIEVE */
        //
        // }
        /********************************************************/
        /* Sum monthly and/or daily precip, snow, HDD, and CDD */
        /* normals to reach an alternate value for the period */
        /********************************************************/

        if (periodClimo
                .getPrecipPeriodNorm() == ParameterFormatClimate.MISSING_PRECIP) {
            /********** Precip Total Normal ***************/
            periodClimo.setPrecipPeriodNorm(
                    sumHisPrecip(beginDate, endDate, ecInformId));
        }
        if (periodClimo
                .getSnowPeriodNorm() == ParameterFormatClimate.MISSING_SNOW) {
            /********** Snow Total Normal ***************/
            periodClimo.setSnowPeriodNorm(
                    sumHisSnow(beginDate, endDate, ecInformId));
        }
        if (periodClimo
                .getNumHeatPeriodNorm() == ParameterFormatClimate.MISSING_DEGREE_DAY) {
            /********** HDD Total Normal ***************/
            periodClimo.setNumHeatPeriodNorm(
                    sumHisHeat(beginDate, endDate, ecInformId));
        }
        if (periodClimo
                .getNumCoolPeriodNorm() == ParameterFormatClimate.MISSING_DEGREE_DAY) {
            /********** CDD Total Normal ***************/
            periodClimo.setNumCoolPeriodNorm(
                    sumHisCool(beginDate, endDate, ecInformId));
        }

        /**********************************************************/
        /* Calculate normal temperatures, normal snow depth, and */
        /* daily avg precip normal using a combination of monthly */
        /* and daily values. */
        /**********************************************************/
        if (periodClimo
                .getNormMeanMaxTemp() == ParameterFormatClimate.MISSING) {
            periodClimo.setNormMeanMaxTemp(averageValue(beginDate, endDate,
                    ecInformId, "max_temp_mean", "norm_mean_max_temp"));
        }

        if (periodClimo
                .getNormMeanMinTemp() == ParameterFormatClimate.MISSING) {
            periodClimo.setNormMeanMinTemp(averageValue(beginDate, endDate,
                    ecInformId, "min_temp_mean", "norm_mean_min_temp"));
        }

        if (periodClimo.getNormMeanTemp() == ParameterFormatClimate.MISSING) {
            periodClimo.setNormMeanTemp(averageValue(beginDate, endDate,
                    ecInformId, "mean_temp", "norm_mean_temp"));
        }

        if (periodClimo.getPrecipDayNorm() == ParameterFormatClimate.MISSING) {
            periodClimo.setPrecipDayNorm(averageValue(beginDate, endDate,
                    ecInformId, "precip_mean", "precip_day_norm"));
        }

        if (periodClimo.getSnowGroundNorm() == ParameterFormatClimate.MISSING) {
            periodClimo.setSnowGroundNorm(averageValue(beginDate, endDate,
                    ecInformId, "snow_ground_mean", "snow_ground_norm"));
        }

        /********************************************************/
        /* This next section finds the normal sums since the */
        /* beginning of the respective seasons */
        /********************************************************/
        begin.setMon(1);
        begin.setDay(1);

        /* Normal CDD Since Jan 1 */
        periodClimo.setNumCool1JanNorm(sumHisCool(begin, endDate, ecInformId));

        begin.setMon(7);

        /* Normal HDD Since July 1 */
        periodClimo.setNumHeat1JulyNorm(sumHisHeat(begin, endDate, ecInformId));

        /* Normal Snow Since July 1 */
        periodClimo.setSnowJuly1Norm(sumHisSnow(begin, endDate, ecInformId));

        /*********************************************************/
        /* Attempt to find the normal water equiv by summing */
        /* monthly norms... if not, try using monthly snow norms */
        /* and dividing by 10 to get a best estimate. */
        /*********************************************************/
        if (periodClimo
                .getSnowWaterPeriodNorm() == ParameterFormatClimate.MISSING) {
            periodClimo.setSnowWaterPeriodNorm(
                    monthlySums(beginDate.getMon(), endDate.getMon(), numMos,
                            periodClimo.getInformId(), "snow_water_pd_norm"));
        }

        /* If monthly water equiv norms missing, try snow norm and */
        /* divide by 10 to get best estimate. */
        if (periodClimo
                .getSnowWaterPeriodNorm() == ParameterFormatClimate.MISSING) {
            periodClimo.setSnowWaterPeriodNorm(
                    monthlySums(beginDate.getMon(), endDate.getMon(), numMos,
                            periodClimo.getInformId(), "snow_pd_mean"));
            if (periodClimo
                    .getSnowWaterPeriodNorm() != ParameterFormatClimate.MISSING) {
                periodClimo.setSnowWaterPeriodNorm(
                        periodClimo.getSnowWaterPeriodNorm() / 10.f);
            }
        }

        /*********************************************************/
        /* Do the same for the "since July 1" normal */
        /*********************************************************/

        if (endDate.getMon() < begin.getMon()) {
            numMos = ((12 - begin.getMon()) + 1) + endDate.getMon();
        } else {
            numMos = (endDate.getMon() - begin.getMon()) + 1;
        }

        periodClimo.setSnowWaterJuly1Norm(
                monthlySums(begin.getMon(), endDate.getMon(), numMos,
                        periodClimo.getInformId(), "snow_water_pd_norm"));

        /* If monthly water equiv norms missing, try snow norm and */
        /* divide by 10 to get best estimate. */
        if (periodClimo
                .getSnowWaterJuly1Norm() == ParameterFormatClimate.MISSING) {
            periodClimo.setSnowWaterJuly1Norm(
                    monthlySums(begin.getMon(), endDate.getMon(), numMos,
                            periodClimo.getInformId(), "snow_pd_mean"));
            if (periodClimo
                    .getSnowWaterJuly1Norm() != ParameterFormatClimate.MISSING) {
                periodClimo.setSnowWaterJuly1Norm(
                        periodClimo.getSnowWaterJuly1Norm() / 10.f);
            }
        }

    }

    /**
     * Converted from get_period_hist_climo.ec
     * 
     * SUBROUTINE #2: average_value This routine calculates a weighted normal
     * using a combination of monthly and daily normal values. Used primarily as
     * a backup for the normal temperatures, it is also used as a backup for
     * normal snow depth and avg daily precipitation.
     * 
     * Original comments:
     * 
     * <pre>
     * *   January 2001     Doug Murphy        PRC/MDL
     * 
     *   Variables
     *    Input  begin_date     begin date of period to be averaged
     *       end_date       end date of period to be averaged
     *           station_id     ID of station whose norms are to be averaged
     *           day_name       column name in the day_climate_norm hmdb table
     *                          of the variable to be averaged
     *           mon_name       column name in the mon_climate_norm hmdb table
     *                          of the variable to be averaged
     * 
     *    Output return_val     returns float value resulting from calculations
     * </pre>
     */
    private float averageValue(ClimateDate beginDate, ClimateDate endDate,
            long stationId, String dayName, String monName) {
        /*
         * smo, DR 17340, a duplicate for OB6.1 - DR 16799, Monthly average
         * temperature departure from normal is missing from F6
         */
        int beginMonth;
        if ((endDate.getMon() < beginDate.getMon())
                || ((endDate.getMon() == beginDate.getMon())
                        && (endDate.getDay() == (beginDate.getDay() - 1)))) {
            beginMonth = beginDate.getMon() - 12;
        } else {
            beginMonth = beginDate.getMon();
        }

        int endMonth = endDate.getMon();

        if (beginDate.getDay() != 1) {
            beginMonth++;
        }

        if (endDate.getDay() < MAX_DAYS_PER_MONTH[endDate.getMon() - 1]) {
            endMonth--;
        }

        // special flag for calculations: 0, 1, or 2
        int dailiesOnly = 0;

        if (beginMonth > endMonth) {
            dailiesOnly = 1;
        }

        /* Build statements */
        StringBuilder ecSum = new StringBuilder(" SELECT SUM(").append(dayName)
                .append(") FROM ");
        ecSum.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        ecSum.append(
                " WHERE station_id = :station_id  AND  day_of_year >= :ecBegin ");
        ecSum.append(" AND  day_of_year <= :ecEnd  AND ").append(dayName);
        ecSum.append(" != 9999::real " + " AND  day_of_year != '02-29' AND ");
        ecSum.append(dayName).append("!= -1::real");

        StringBuilder ecCount = new StringBuilder(" SELECT COUNT(*)  FROM ");
        ecCount.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        ecCount.append(
                " WHERE station_id = :station_id  AND  day_of_year >= :ecBegin ");
        ecCount.append(" AND  day_of_year <= :ecEnd  AND ").append(dayName);
        ecCount.append(" != 9999::real " + " AND  day_of_year != '02-29' AND ");
        ecCount.append(dayName).append("!= -1::real");

        StringBuilder ecSelect = new StringBuilder(" SELECT (").append(monName)
                .append(") FROM ");
        ecSelect.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        ecSelect.append(
                " WHERE period_type = 5 AND station_id = :station_id  AND  month_of_year = :month_of_year ");
        ecSelect.append(" AND ").append(monName).append(" != 9999::real  AND ")
                .append(monName);
        ecSelect.append("!= -1::real");

        int daySum = 0;
        float weightedSum = 0;

        Map<String, Object> keyParamMap = new HashMap<>();

        for (int i = beginMonth; i <= endMonth; i++) {
            int begin;
            if (i < 1) {
                begin = i + 12;
            } else {
                begin = i;
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("station_id", stationId);
                keyParamMap.put("month_of_year", i);
                Object[] results = getDao().executeSQLQuery(ecSelect.toString(),
                        keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    daySum = daySum + MAX_DAYS_PER_MONTH[begin - 1];
                    weightedSum += ((float) results[0]
                            * MAX_DAYS_PER_MONTH[begin - 1]);
                } else {
                    dailiesOnly = 1;
                }
            } catch (Exception e) {
                logger.error("Error querying the climate database: [" + ecSelect
                        + "] with map: [" + keyParamMap + "]", e);
            }

        }

        if ((dailiesOnly == 0) && (beginMonth != beginDate.getMon())) {
            String ecBegin = beginDate.toMonthDayDateString();
            String ecEnd = new ClimateDate(
                    ClimateUtilities.daysInMonth(beginDate), beginDate.getMon(),
                    beginDate.getYear()).toMonthDayDateString();
            int numberOfDays = ClimateUtilities.daysInMonth(beginDate)
                    - beginDate.getDay() + 1;

            try {
                keyParamMap.clear();
                keyParamMap.put("station_id", stationId);
                keyParamMap.put("ecBegin", ecBegin);
                keyParamMap.put("ecEnd", ecEnd);
                Object[] results = getDao().executeSQLQuery(ecCount.toString(),
                        keyParamMap);
                if ((results != null) && (results.length >= 1)) {

                    if (((Number) results[0]).intValue() == numberOfDays) {

                        try {
                            Object[] results2 = getDao().executeSQLQuery(
                                    ecSum.toString(), keyParamMap);
                            // result could be null
                            if ((results2 != null) && (results2.length >= 1)
                                    && (results2[0] != null)) {
                                daySum = daySum + numberOfDays;
                                weightedSum += ((Number) results2[0])
                                        .floatValue();
                            } else {
                                dailiesOnly = 2;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error querying the climate database sum: ["
                                            + ecSum + "] with map: ["
                                            + keyParamMap + "]",
                                    e);
                        }
                    }
                } else {
                    dailiesOnly = 2;
                }
            } catch (Exception e) {
                logger.error("Error querying the climate database count: ["
                        + ecCount + "] with map: [" + keyParamMap + "]", e);
            }

        }

        if ((dailiesOnly == 0) && (endMonth != endDate.getMon())
                && (endDate.getMon() != beginDate.getMon())) {

            String ecBegin = endDate.toFirstDayOfMonthDateString();
            String ecEnd = endDate.toMonthDayDateString();
            int numberOfDays = endDate.getDay();

            try {
                keyParamMap.clear();
                keyParamMap.put("station_id", stationId);
                keyParamMap.put("ecBegin", ecBegin);
                keyParamMap.put("ecEnd", ecEnd);
                Object[] results = getDao().executeSQLQuery(ecCount.toString(),
                        keyParamMap);
                if ((results != null) && (results.length >= 1)) {

                    if ((int) results[0] == numberOfDays) {

                        try {
                            Object[] results2 = getDao().executeSQLQuery(
                                    ecSum.toString(), keyParamMap);
                            // result could be null
                            if ((results2 != null) && (results2.length >= 1)
                                    && (results2[0] != null)) {
                                daySum = daySum + numberOfDays;
                                weightedSum += ((Number) results2[0])
                                        .floatValue();
                            } else {
                                dailiesOnly = 2;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error querying the climate database sum: ["
                                            + ecSum + "] with map: ["
                                            + keyParamMap + "]",
                                    e);
                        }
                    }
                } else {
                    dailiesOnly = 2;
                }
            } catch (Exception e) {
                logger.error("Error querying the climate database count: ["
                        + ecCount + "] with map: [" + keyParamMap + "]", e);
            }
        }

        if (dailiesOnly == 1) {
            int numberOfDays = 0;
            weightedSum = 0;
            daySum = 0;

            if ((endDate.getMon() < beginDate.getMon())
                    || ((endDate.getMon() == beginDate.getMon()) && (endDate
                            .getDay() == (beginDate.getDay() - 1)))) {

                ecSum = new StringBuilder(" SELECT SUM(").append(dayName)
                        .append(") FROM ");
                ecSum.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                ecSum.append(
                        " WHERE station_id = :station_id  AND ( day_of_year >= :ecBegin ");
                ecSum.append(" OR day_of_year <= :ecEnd ) AND ")
                        .append(dayName);
                ecSum.append(" != 9999::real ");
                ecSum.append(" AND day_of_year != '02-29' AND ")
                        .append(dayName);
                ecSum.append("!= -1::real");

                ecCount = new StringBuilder(" SELECT COUNT(*) FROM ");
                ecCount.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                ecCount.append(" WHERE station_id = :station_id ");
                ecCount.append(
                        " AND ( day_of_year >= :ecBegin OR day_of_year <= :ecEnd ) ");
                ecCount.append(" AND ").append(dayName)
                        .append(" != 9999::real ");
                ecCount.append(" AND day_of_year != '02-29' AND ")
                        .append(dayName);
                ecCount.append("!= -1::real");

            }

            String ecBegin = beginDate.toMonthDayDateString();
            String ecEnd = endDate.toMonthDayDateString();

            beginMonth = beginDate.getMon();
            if (endDate.getMon() < beginDate.getMon()) {
                beginMonth = beginMonth - 12;
            }

            if (((endDate.getMon() == beginDate.getMon())
                    && (endDate.getDay() < beginDate.getDay()))
                    || ((endDate.getMon() == 12) && (endDate.getDay() == 31)
                            && (beginDate.getMon() == 1)
                            && (beginDate.getDay() == 1))) {
                numberOfDays = 365;
            } else {
                for (int i = beginMonth; i <= endDate.getMon(); i++) {
                    if ((beginDate.getMon() == endDate.getMon())
                            && (beginDate.getDay() < endDate.getDay())) {
                        numberOfDays = (endDate.getDay() - beginDate.getDay())
                                + 1;
                    } else if (((i == beginDate.getMon())
                            || (i == (beginDate.getMon() - 12)))
                            && (beginDate.getDay() > 1)) {
                        numberOfDays = numberOfDays
                                + ((MAX_DAYS_PER_MONTH[beginDate.getMon() - 1]
                                        - beginDate.getDay()) + 1);
                    } else if ((i == endDate.getMon()) && (endDate
                            .getDay() < MAX_DAYS_PER_MONTH[endDate.getMon()
                                    - 1])) {
                        numberOfDays = numberOfDays + endDate.getDay();
                    } else if (i < 1) {
                        numberOfDays = numberOfDays
                                + MAX_DAYS_PER_MONTH[i + 11];
                    } else {
                        numberOfDays = numberOfDays + MAX_DAYS_PER_MONTH[i - 1];
                    }
                }
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("station_id", stationId);
                keyParamMap.put("ecBegin", ecBegin);
                keyParamMap.put("ecEnd", ecEnd);
                Object[] results = getDao().executeSQLQuery(ecCount.toString(),
                        keyParamMap);
                if ((results != null) && (results.length >= 1)) {

                    if (((Number) results[0]).intValue() == numberOfDays) {

                        try {
                            Object[] results2 = getDao().executeSQLQuery(
                                    ecSum.toString(), keyParamMap);
                            // result could be null
                            if ((results2 != null) && (results2.length >= 1)
                                    && (results2[0] != null)) {
                                daySum = numberOfDays;
                                // depending on sum query, could be int or float
                                weightedSum = ((Number) results2[0])
                                        .floatValue();
                            } else {
                                dailiesOnly = 2;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error querying the climate database sum: ["
                                            + ecSum + "] with map: ["
                                            + keyParamMap + "]",
                                    e);
                        }
                    }
                } else {
                    dailiesOnly = 2;
                }
            } catch (Exception e) {
                logger.error("Error querying the climate database count: ["
                        + ecCount + "] with map: [" + keyParamMap + "]", e);
            }
        }

        if (dailiesOnly < 2) {
            return weightedSum / daySum;
        } else {
            return ParameterFormatClimate.MISSING;
        }
    }

    /**
     * Converted from get_period_hist_climo.ec SUBROUTINE #1: monthly_sums This
     * routine sums a variable using the monthly normal entries for the passed
     * element.
     * 
     * Original comments:
     * 
     * <pre>
     * 
     *   monthly_sums()                                                      
     * 
     *   January 2001     Doug Murphy        PRC/MDL
     * 
     *   Variables
     *    Input  begin_date     begin month of period to be summed
     *       end_date       end month of period to be summed
     *           num_mos        number of complete months within the period
     *                          begin and end
     *           station_id     ID of station whose norms are to be summed
     *           table_name     column name in the mon_climate_norm hmdb table
     *                          of the variable to be summed
     * 
     *    Output return_val     returns float value resulting from calculations
     * </pre>
     */
    public float monthlySums(int beginDate, int endDate, int numMos,
            int stationId, String tableName) {
        StringBuilder ecStmt;
        /* Build statements */
        if (endDate < beginDate) {
            ecStmt = new StringBuilder(
                    " FROM " + ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
            ecStmt.append(
                    " WHERE period_type = 5 AND station_id = :stationId ");
            ecStmt.append(
                    " AND ( month_of_year >= :ecBegin  OR month_of_year <= :ecEnd) ");
            ecStmt.append(" AND ").append(tableName).append(" != ")
                    .append(ParameterFormatClimate.MISSING)
                    .append("::real AND ").append(tableName);
            ecStmt.append(" != ").append(ParameterFormatClimate.TRACE);
        } else {
            ecStmt = new StringBuilder(" FROM ")
                    .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
            ecStmt.append(
                    " WHERE period_type = 5 AND station_id = :stationId ");
            ecStmt.append(
                    " AND month_of_year >= :ecBegin AND month_of_year <= :ecEnd ");
            ecStmt.append(" AND ").append(tableName).append(" != ")
                    .append(ParameterFormatClimate.MISSING)
                    .append("::real AND ").append(tableName);
            ecStmt.append(" != ").append(ParameterFormatClimate.TRACE);
        }

        StringBuilder ecSum = new StringBuilder("SELECT SUM( ")
                .append(tableName).append(")").append(ecStmt);

        StringBuilder ecCount = new StringBuilder("SELECT COUNT(*) ")
                .append(ecStmt);

        // EXEC SQL BEGIN WORK;
        Map<String, Object> keyParamMap = new HashMap<>();

        float returnVal = ParameterFormatClimate.MISSING;
        try {
            keyParamMap.clear();
            keyParamMap.put("stationId", stationId);
            keyParamMap.put("ecBegin", beginDate);
            keyParamMap.put("ecEnd", endDate);
            Object[] results = getDao().executeSQLQuery(ecCount.toString(),
                    keyParamMap);
            if ((results != null) && (results.length >= 1)) {
                int countReturn = ((Number) results[0]).intValue();

                if (countReturn == numMos) {

                    try {
                        Object[] sumResults = getDao()
                                .executeSQLQuery(ecSum.toString(), keyParamMap);
                        // result could be null
                        if ((sumResults != null) && (sumResults.length >= 1)
                                && (sumResults[0] != null)) {
                            returnVal = ((Number) sumResults[0]).intValue();
                        }
                    } catch (Exception e) {
                        logger.error("Error querying sum the climate database ["
                                + ecSum + "] with map: [" + keyParamMap + "]",
                                e);
                    }
                } else {
                    returnVal = ParameterFormatClimate.MISSING;
                }

            } else {
                logger.warn("Expected some result from count query: [" + ecCount
                        + "] using map: [" + keyParamMap + "]");
            }
        } catch (Exception e) {
            logger.error("Error querying the climate database: [" + ecCount
                    + "] with map: [" + keyParamMap + "]", e);
        }

        return returnVal;
    }

    /**
     * Converted from sum_his_cool.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     *    void sum_his_cool ( const climate_date &begin_date
     *                 const climate_date  &end_date,
     *                   long      *station_id,
     *                                int      *sum_cool    )
     *    
     *       Jason Tuell       PRC/TDL             HP 9000/7xx
     *    
     *       FUNCTION DESCRIPTION
     *       ====================
     *          
     *          This function sums the normal cooling degree days in the climate 
     *          database between the given period begin date and end date.
     *          It will use monthly normal values for full months within the two 
     *          dates. Then it will add the normal daily values for the final month
     *          of the period if it is a partial month.
     *    
     *          In the case of an error or the begin date not being the first day of
     *          a month, the function will use only daily normals for the cooling 
     *          degree days' sum. However, if this data is not available, it will 
     *          calculate the cooling degree days from the daily normal maximum and 
     *          minimum temperatures.
     *    
     *          Finally, during leap years, for a February monthly normal total ONLY, 
     *          the Feb 29th normal value is added into the sum. Note that this will
     *          only occur on the 29th of the month.
     *    
     *       VARIABLES
     *       =========
     *    
     *       name                  description
     *    ------------------------------------------------------------------------------
     *        Input
     *          begin_date          - starting date for summation
     *          end_date            - ending date for summation
     *          station_id         - station id of type int for which this function
     *                   is called
     *    
     *        Output
     *          sum_cool            - sum of historical cooling degree days between
     *                                start date and end date
     *    
     *          Local
     *    
     *        MODIFICATIONS
     *        May 1999             David T. Miller                 PRC/TDL
     *                             Added check for leap year.  If it is a leap year, then
     *                             want to include Feb 29th historical data.  If not, 
     *                             then must exclude it.
     *        April 2000           David T. Miller                 PRC/TDL
     *                             Slight modification so YTD normals would add up okay
     *                             during leap years
     *        September 2000       Doug Murphy                     PRC/TDL
     *                             Removed unnecessary include files
     *        January 2001         Doug Murphy                     PRC/MDL
     *                             Major rewrite to take advantage of monthly norms
     *                             that have been introduced - also revised to work
     *                             with monthly, seasonal, and annual reports as well
     *        Dec 2002             Bob Morris                      SAIC/MDL
     *                             - Changed date args to reference variables
     *                             to fix seg. faults under Linux.
     *                             - Added non-const int iyrtemp for call to leap()
     *        Jan 2005             Manan Dalal                     NGIT/MDL
     *                             - Ported code from Informix to Postgresql
     ************************************************************************* 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */
    public int sumHisCool(ClimateDate beginDate, ClimateDate endDate,
            int stationId) {
        int tempSum = 0;

        /* Converting the input date structure to a character string */
        String ecStartDate = beginDate.toMonthDayDateString();
        String ecEndDate = endDate.toMonthDayDateString();

        int ecStartMo = beginDate.getMon();
        int ecEndMo = endDate.getMon();
        String ecMonthBegin = endDate.toFirstDayOfMonthDateString();

        boolean beginLeapFlag = beginDate.isLeapYear();
        boolean endLeapFlag = endDate.isLeapYear();

        /*
         * The begin date of the sum must be the first of the month to take
         * advantage of the monthly norms, otherwise, skip directly to using the
         * daily norms only.
         */
        boolean dailiesOnly = false;
        int numMos = 0;
        if (beginDate.getDay() == 1) {
            if (endDate.getMon() < beginDate.getMon()) {
                numMos = ((12 - beginDate.getMon()) + 1)
                        + (endDate.getMon() - 1);
            } else {
                numMos = endDate.getMon() - beginDate.getMon();
            }
        } else {
            dailiesOnly = true;
        }

        /* Use the monthly norms */
        StringBuilder countQuery;
        Map<String, Object> keyParamMap = new HashMap<>();

        if (!dailiesOnly) {
            /*
             * If numMos is >0, we have a date span which covers more than one
             * month (can use the monthly norms for the full months of the span)
             */
            if (numMos != 0) {
                if (endDate.getMon() < beginDate.getMon()) {
                    countQuery = new StringBuilder(" SELECT COUNT(*) FROM ");
                    countQuery.append(
                            ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                    countQuery.append(" WHERE (month_of_year >= :ec_start_mo ");
                    countQuery.append(" OR month_of_year < :ec_end_mo) ");
                    countQuery.append(" AND station_id = :ec_station_id ");
                    countQuery.append(" AND period_type = 5 ");
                    countQuery.append(" AND cool_pd_mean != ")
                            .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                } else {
                    countQuery = new StringBuilder("SELECT COUNT(*) FROM ");
                    countQuery.append(
                            ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                    countQuery.append(" WHERE month_of_year >= :ec_start_mo ");
                    countQuery.append(" AND month_of_year < :ec_end_mo ");
                    countQuery.append(" AND station_id = :ec_station_id ");
                    countQuery.append(" AND period_type = 5 ");
                    countQuery.append(" AND cool_pd_mean != ")
                            .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                }

                keyParamMap.clear();
                keyParamMap.put("ec_start_mo", ecStartMo);
                keyParamMap.put("ec_end_mo", ecEndMo);
                keyParamMap.put("ec_station_id", stationId);

                int results = ((Number) queryForOneValue(countQuery.toString(),
                        keyParamMap, -1)).intValue();
                if (results != -1) {
                    int ecCountCool = results;
                    if (ecCountCool == numMos) {
                        StringBuilder sumQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            sumQuery = new StringBuilder(
                                    "SELECT SUM (cool_pd_mean) ");
                            sumQuery.append(" FROM ");
                            sumQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            sumQuery.append(
                                    " WHERE (month_of_year >= :ec_start_mo ");
                            sumQuery.append(" OR month_of_year < :ec_end_mo) ");
                            sumQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumQuery.append(" AND period_type = 5 ");
                            sumQuery.append(" AND cool_pd_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                        } else {
                            sumQuery = new StringBuilder(
                                    "SELECT SUM (cool_pd_mean) ");
                            sumQuery.append(" FROM ");
                            sumQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            sumQuery.append(
                                    " WHERE month_of_year >= :ec_start_mo ");
                            sumQuery.append(" AND month_of_year < :ec_end_mo ");
                            sumQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumQuery.append(" AND period_type = 5 ");
                            sumQuery.append(" AND cool_pd_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                        }

                        int sumcoolRes = ((Number) queryForOneValue(
                                sumQuery.toString(), keyParamMap, -1))
                                        .intValue();
                        if (sumcoolRes != -1) {
                            tempSum = sumcoolRes;
                        } else {
                            logger.warn(
                                    "Expected some result summing the cooling degree day"
                                            + " data in climate database: ["
                                            + sumQuery + "] map: ["
                                            + keyParamMap + "]");
                            dailiesOnly = true;
                        }
                    } else {
                        dailiesOnly = true;
                    }
                } else {
                    logger.warn("Expected some result from count query: ["
                            + countQuery + "] using map: [" + keyParamMap
                            + "]");
                    dailiesOnly = true;
                }
            }

            /*
             * If the end day is the end of the month, can use the monthly
             * norm... otherwise, use the daily norms for the last month of the
             * span
             */
            keyParamMap.clear();
            StringBuilder coolingQuery;
            if (endDate.getDay() >= MAX_DAYS_PER_MONTH[endDate.getMon() - 1]) {
                coolingQuery = new StringBuilder("SELECT cool_pd_mean FROM ");
                coolingQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                coolingQuery.append(" WHERE station_id = :ec_station_id ");
                coolingQuery.append(" AND month_of_year = :ec_end_mo ");
                coolingQuery.append(" AND period_type = 5 ");
                coolingQuery.append(" AND cool_pd_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);

                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_end_mo", ecEndMo);
            } else {
                coolingQuery = new StringBuilder(
                        "SELECT SUM(cool_day_mean) FROM ");
                coolingQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                coolingQuery.append(" WHERE day_of_year >= :ec_month_begin ");
                coolingQuery.append(" AND day_of_year <= :ec_end_date ");
                coolingQuery.append(" AND station_id = :ec_station_id ");
                coolingQuery.append(" AND cool_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);

                keyParamMap.put("ec_month_begin", ecMonthBegin);
                keyParamMap.put("ec_end_date", ecEndDate);
                keyParamMap.put("ec_station_id", stationId);
            }
            int results = ((Number) queryForOneValue(coolingQuery.toString(),
                    keyParamMap, -1)).intValue();
            if (results != -1) {
                tempSum += results;
            } else {
                logger.warn(
                        "Expected some result summing the cooling degree day"
                                + " data in climate database: [" + coolingQuery
                                + "] map: [" + keyParamMap + "]");
                dailiesOnly = true;
            }

        }

        /*
         * If we made it through the first section without an error, set cooling
         * degree days to the sum found above
         */
        int sumCool;
        if (!dailiesOnly) {
            sumCool = tempSum;
        } else {
            sumCool = ParameterFormatClimate.MISSING_DEGREE_DAY;
        }

        /*
         * If an error was encountered, or the begin date is not the first, sum
         * the daily norms. This section is original code
         */
        if (dailiesOnly) {
            float sumMax = ParameterFormatClimate.MISSING;
            float sumMin = ParameterFormatClimate.MISSING;
            tempSum = 0;

            /*
             * determine if there is daily cooling degree day data in the data
             * base We will do this by determining if there are any rows in
             * which the cooling degree days aren't set to missing.
             */
            keyParamMap.clear();
            keyParamMap.put("ec_start_date", ecStartDate);
            keyParamMap.put("ec_end_date", ecEndDate);
            keyParamMap.put("ec_station_id", stationId);
            StringBuilder countQuery2;
            if (endDate.getMon() < beginDate.getMon()) {
                countQuery2 = new StringBuilder("SELECT COUNT(*) FROM ");
                countQuery2
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                countQuery2.append(" WHERE (day_of_year >= :ec_start_date ");
                countQuery2.append(" OR day_of_year <= :ec_end_date) ");
                countQuery2.append(" AND station_id = :ec_station_id ");
                countQuery2.append(" AND cool_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                countQuery2.append(" AND day_of_year != '02-29'");
            } else {
                countQuery2 = new StringBuilder("SELECT COUNT(*) FROM ");
                countQuery2
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                countQuery2.append(" WHERE day_of_year >= :ec_start_date ");
                countQuery2.append(" AND day_of_year <= :ec_end_date ");
                countQuery2.append(" AND station_id = :ec_station_id ");
                countQuery2.append(" AND cool_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                countQuery2.append(" AND day_of_year != '02-29'");
            }

            try {
                Object[] results = getDao()
                        .executeSQLQuery(countQuery2.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    int ecCountCool = ((Number) results[0]).intValue();

                    if (ecCountCool != 0) {
                        StringBuilder sumQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            sumQuery = new StringBuilder(
                                    "SELECT SUM(cool_day_mean) FROM ");
                            sumQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            sumQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            sumQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            sumQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumQuery.append(" AND cool_day_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                            sumQuery.append(" AND day_of_year != '02-29'");
                        } else {
                            sumQuery = new StringBuilder(
                                    "SELECT SUM(cool_day_mean) FROM ");
                            sumQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            sumQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            sumQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            sumQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumQuery.append(" AND cool_day_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                            sumQuery.append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] ecsumObjects = getDao().executeSQLQuery(
                                    sumQuery.toString(), keyParamMap);
                            // result could be null
                            if ((ecsumObjects != null)
                                    && (ecsumObjects.length >= 1)
                                    && (ecsumObjects[0] != null)) {
                                sumCool = ((Number) ecsumObjects[0]).intValue();
                            } else {
                                logger.warn("No data for query: [" + sumQuery
                                        + "] and map: [" + keyParamMap + "]");
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error summing the cooling degree day data in climate database: ["
                                            + sumQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                    } else {
                        /*
                         * Now historical mean daily cooling degree data in the
                         * historical database We need to fall back and
                         * calculate the cooling degree days from the daily mean
                         * max and min temperatures
                         */
                        /* sum the max temperatures first */
                        StringBuilder maxTempQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            maxTempQuery = new StringBuilder(
                                    "SELECT SUM(max_temp_mean) FROM ");
                            maxTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            maxTempQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            maxTempQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            maxTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            maxTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(" AND day_of_year != '02-29'");
                        } else {
                            maxTempQuery = new StringBuilder(
                                    "SELECT SUM(max_temp_mean) FROM ");
                            maxTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            maxTempQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            maxTempQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            maxTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            maxTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] ecsumRes = getDao().executeSQLQuery(
                                    maxTempQuery.toString(), keyParamMap);
                            // result could be null
                            if ((ecsumRes != null) && (ecsumRes.length >= 1)
                                    && (ecsumRes[0] != null)) {
                                sumMax = (int) ecsumRes[0];
                            } else {
                                logger.warn("No data for query: ["
                                        + maxTempQuery + "] and map: ["
                                        + keyParamMap + "]");
                                sumCool = ParameterFormatClimate.MISSING_DEGREE_DAY;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error summing the max temp data in climate database: ["
                                            + maxTempQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                        /* sum the min temperatures next */
                        StringBuilder minTempQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            minTempQuery = new StringBuilder(
                                    "SELECT SUM(min_temp_mean) FROM ");
                            minTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            minTempQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            minTempQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            minTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            minTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            minTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            minTempQuery.append(" AND day_of_year != '02-29'");
                        } else {
                            minTempQuery = new StringBuilder(
                                    "SELECT SUM(min_temp_mean) FROM ");
                            minTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            minTempQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            minTempQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            minTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            minTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            minTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            minTempQuery.append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] ecsumRes = getDao().executeSQLQuery(
                                    minTempQuery.toString(), keyParamMap);
                            // result could be null
                            if ((ecsumRes != null) && (ecsumRes.length >= 1)
                                    && (ecsumRes[0] != null)) {
                                sumMin = (int) ecsumRes[0];
                            } else {
                                logger.warn("No data for query: ["
                                        + minTempQuery + "] and map: ["
                                        + keyParamMap + "]");
                                sumCool = ParameterFormatClimate.MISSING_DEGREE_DAY;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error summing the min temp data in climate database: ["
                                            + minTempQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                        /*
                         * Now count the number of rows where there is both a
                         * max and min temperature
                         */
                        StringBuilder tempCountQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            tempCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            tempCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            tempCountQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            tempCountQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            tempCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            tempCountQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            tempCountQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            tempCountQuery
                                    .append(" AND day_of_year != '02-29'");
                        } else {
                            tempCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            tempCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            tempCountQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            tempCountQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            tempCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            tempCountQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            tempCountQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            tempCountQuery
                                    .append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] ecsumObjects = getDao().executeSQLQuery(
                                    tempCountQuery.toString(), keyParamMap);
                            if ((ecsumObjects != null)
                                    && (ecsumObjects.length >= 1)) {
                                int ecCountTemp = ((Number) ecsumObjects[0])
                                        .intValue();
                                /*
                                 * Calculate the cooling degree days if there is
                                 * data
                                 */
                                /* Set to missing if there is no data */
                                if (ecCountTemp != 0) {
                                    float sumTemp65 = ecCountTemp * 65.f;
                                    float avgTemp = (sumMax + sumMin) / 2.f;
                                    int iavg = (int) avgTemp;
                                    if ((avgTemp - iavg) >= 0.5) {
                                        iavg = iavg + 1;
                                    }

                                    if (iavg > sumTemp65) {
                                        sumCool = (int) (iavg - sumTemp65);
                                    } else {
                                        sumCool = 0;
                                    }
                                } else {
                                    return ParameterFormatClimate.MISSING_DEGREE_DAY;
                                }
                            } else {
                                logger.warn("Expected some result from query: ["
                                        + tempCountQuery + "] and map: ["
                                        + keyParamMap + "]");
                                sumCool = ParameterFormatClimate.MISSING_DEGREE_DAY;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error counting the temp data in climate database: ["
                                            + tempCountQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }
                    }
                } else {
                    logger.warn("Expected some result from query: ["
                            + countQuery2 + "] and map: [" + keyParamMap + "]");
                }
            } catch (Exception e) {
                logger.error(
                        "Error counting the temp data in climate database: ["
                                + countQuery2 + "] map: [" + keyParamMap + "]",
                        e);
                return ParameterFormatClimate.MISSING_DEGREE_DAY;
            }

            /*
             * check for leap year; if true, then add Feb 29th in month and
             * seasonal
             * 
             * Migrated Climate: legacy used to do this work and then check if
             * the current sum is missing; check it now instead to save
             * processing
             */
            /* summations only */

            if ((beginLeapFlag || endLeapFlag) && (endDate.getMon() == 2)
                    && (endDate.getDay() == 29)
                    && (sumCool != ParameterFormatClimate.MISSING_DEGREE_DAY)) {

                StringBuilder leapQuery = new StringBuilder(
                        "SELECT cool_day_mean FROM ");
                leapQuery.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                leapQuery.append(" WHERE day_of_year = '02-29' ");
                leapQuery.append(" AND station_id = :ec_station_id ");
                leapQuery.append(" AND cool_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                try {
                    Object[] ecsumRes = getDao()
                            .executeSQLQuery(leapQuery.toString(), keyParamMap);
                    // result could be null
                    if ((ecsumRes != null) && (ecsumRes.length >= 1)
                            && (ecsumRes[0] != null)) {
                        sumCool += (int) ecsumRes[0];
                    } else {
                        logger.warn("No data for query: [" + leapQuery
                                + "] and map: [" + keyParamMap + "]");
                        return sumCool;
                    }
                } catch (Exception e) {
                    logger.error(
                            "Error summing the cooling degree day data in climate database: ["
                                    + leapQuery + "] map: [" + keyParamMap
                                    + "]",
                            e);
                    return sumCool;
                }
            }
        }

        return sumCool;
    }

    /**
     * Converted from sum_his_heat.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     * void sum_his_heat ( const climate_date &begin_date
     *                 const climate_date  &end_date,
     *                   long      *station_id,
     *                                int      *sum_heat    )
     *    
     *       Jason Tuell       PRC/TDL             HP 9000/7xx
     *    
     *       FUNCTION DESCRIPTION
     *       ====================
     *    
     *          This function sums the normal heating degree days in the climate 
     *          database between the given period begin date and end date.
     *          It will use monthly normal values for full months within the two 
     *          dates. Then it will add the normal daily values for the final month
     *          of the period if it is a partial month.
     *    
     *          In the case of an error or the begin date not being the first day of
     *          a month, the function will use only daily normals for the heating 
     *          degree days' sum. However, if this data is not available, it will 
     *          calculate the heating degree days from the daily normal maximum and 
     *          minimum temperatures.
     *    
     *          Finally, during leap years, for a February monthly normal total ONLY, 
     *          the Feb 29th normal value is added into the sum. Note that this will
     *          only occur on the 29th of the month.
     *    
     *       VARIABLES
     *       =========
     *    
     *       name                  description
     *    ------------------------------------------------------------------------------
     *        Input
     *          begin_date          - starting date for summation
     *          end_date            - ending date for summation
     *          station_id         - station id of type int for which this function
     *                   is called
     *    
     *        Output
     *          sum_heat            - sum of historical heating degree days between
     *                                start date and end date
     *    
     *          Local
     *    
     *        MODIFICATIONS
     *    
     *        May 1999              David T. Miller             PRC/TDL
     *                              Had to add a section so if the period included a leap
     *                              year, the historical values for Feb 29th would also
     *                              be added into the summations.
     *        Apr 2000              David T. Miller             PRC/TDL
     *                              Slight modification so Feb 29th YTD values would comply
     *                              with WSOM and NCDC 
     *        September 2000        Doug Murphy                 PRC/TDL
     *                              Removed unnecessary include files
     *        January 2001          Doug Murphy                 PRC/MDL
     *                              Major rewrite to take advantage of monthly norms
     *                              that have been introduced - also revised to work
     *                              with monthly, seasonal, and annual reports as well
     *        Dec 2002              Bob Morris                  SAIC/MDL
     *                              - Changed date args to reference variables
     *                              to fix seg. faults under Linux.
     *                              - Added non-const int iyrtemp for call to leap()
     *        Jan 2005              Manan Dalal                 NGIT/MDL
     *                              - Converted Routine from Informix to Postgresql
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */
    public int sumHisHeat(ClimateDate beginDate, ClimateDate endDate,
            int stationId) {
        int tempSum = 0;

        /* Converting the inputed date structure to a character string */
        String ecStartDate = beginDate.toMonthDayDateString();
        String ecEndDate = endDate.toMonthDayDateString();
        int ecStartMo = beginDate.getMon();
        int ecEndMo = endDate.getMon();
        String ecMonthBegin = endDate.toFirstDayOfMonthDateString();

        boolean beginLeapFlag = beginDate.isLeapYear();
        boolean endLeapFlag = endDate.isLeapYear();

        /* The begin date of the sum must be the first of the month */
        /* to take advantage of the monthly norms, otherwise, skip */
        /* directly to using the daily norms only. */
        boolean dailiesOnly = false;
        int numMos = 0;
        if (beginDate.getDay() == 1) {
            if (endDate.getMon() < beginDate.getMon()) {
                numMos = ((12 - beginDate.getMon()) + 1)
                        + (endDate.getMon() - 1);
            } else {
                numMos = endDate.getMon() - beginDate.getMon();
            }
        } else {
            dailiesOnly = true;
        }

        Map<String, Object> keyParamMap = new HashMap<>();
        /* Use the monthly norms */
        if (!dailiesOnly) {
            /* If numMos is >0, we have a date span which */
            /* covers more than one month (can use the */
            /* monthly norms for the full months of the span) */
            if (numMos != 0) {
                StringBuilder countQuery;
                if (endDate.getMon() < beginDate.getMon()) {
                    countQuery = new StringBuilder("SELECT COUNT (*) FROM ");
                    countQuery.append(
                            ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                    countQuery.append(" WHERE (month_of_year >= :ec_start_mo ");
                    countQuery.append(" OR month_of_year < :ec_end_mo) ");
                    countQuery.append(" AND station_id = :ec_station_id ");
                    countQuery.append(" AND period_type = 5 ");
                    countQuery.append(" AND heat_pd_mean != ")
                            .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                } else {
                    countQuery = new StringBuilder("SELECT COUNT (*) FROM ");
                    countQuery.append(
                            ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                    countQuery.append(" WHERE month_of_year >= :ec_start_mo ");
                    countQuery.append(" AND month_of_year < :ec_end_mo ");
                    countQuery.append(" AND station_id = :ec_station_id ");
                    countQuery.append(" AND period_type = 5 ");
                    countQuery.append(" AND heat_pd_mean != ")
                            .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                }

                keyParamMap.clear();
                keyParamMap.put("ec_start_mo", ecStartMo);
                keyParamMap.put("ec_end_mo", ecEndMo);
                keyParamMap.put("ec_station_id", stationId);

                int results = ((Number) queryForOneValue(countQuery.toString(),
                        keyParamMap, -1)).intValue();
                if (results != -1) {
                    int ecCountHeat = results;

                    if (ecCountHeat == numMos) {
                        StringBuilder sumQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            sumQuery = new StringBuilder(
                                    "SELECT SUM (heat_pd_mean) ");
                            sumQuery.append(" FROM ");
                            sumQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            sumQuery.append(
                                    " WHERE (month_of_year >= :ec_start_mo ");
                            sumQuery.append(" OR month_of_year < :ec_end_mo) ");
                            sumQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumQuery.append(" AND period_type = 5 ");
                            sumQuery.append(" AND heat_pd_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                        } else {
                            sumQuery = new StringBuilder(
                                    "SELECT SUM (heat_pd_mean) ");
                            sumQuery.append(" FROM ");
                            sumQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            sumQuery.append(
                                    " WHERE month_of_year >= :ec_start_mo ");
                            sumQuery.append(" AND month_of_year < :ec_end_mo ");
                            sumQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumQuery.append(" AND period_type = 5 ");
                            sumQuery.append(" AND heat_pd_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                        }

                        int res = ((Number) queryForOneValue(
                                sumQuery.toString(), keyParamMap, -1))
                                        .intValue();
                        // result could be null
                        if (res != -1) {
                            tempSum = res;
                        } else {
                            logger.warn("No data for query: [" + sumQuery
                                    + "] and map: [" + keyParamMap + "]");
                            dailiesOnly = true;
                        }
                    } else {
                        dailiesOnly = true;
                    }
                } else {
                    logger.warn("Expected some result from query: ["
                            + countQuery + "] and map: [" + keyParamMap + "]");
                    dailiesOnly = true;
                }
            }

            /*
             * If the end day is the end of the month, can use the monthly
             * norm... otherwise, use the daily norms for the last month of the
             * span
             */
            StringBuilder heatQuery;
            if (endDate.getDay() >= MAX_DAYS_PER_MONTH[endDate.getMon() - 1]) {
                heatQuery = new StringBuilder("SELECT heat_pd_mean FROM ");
                heatQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                heatQuery.append(" WHERE station_id = :ec_station_id ");
                heatQuery.append(" AND month_of_year = :ec_end_mo ");
                heatQuery.append(" AND period_type = 5 ");
                heatQuery.append(" AND heat_pd_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                keyParamMap.clear();
                keyParamMap.put("ec_end_mo", ecEndMo);
                keyParamMap.put("ec_station_id", stationId);

            } else {
                heatQuery = new StringBuilder(
                        "SELECT SUM(heat_day_mean) FROM ");
                heatQuery.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                heatQuery.append(" WHERE day_of_year >= :ec_month_begin ");
                heatQuery.append(" AND day_of_year <= :ec_end_date ");
                heatQuery.append(" AND station_id = :ec_station_id ");
                heatQuery.append(" AND heat_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                keyParamMap.clear();
                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_end_date", ecEndDate);
                keyParamMap.put("ec_month_begin", ecMonthBegin);
            }

            int results = ((Number) queryForOneValue(heatQuery.toString(),
                    keyParamMap, -1)).intValue();
            if (results != -1) {
                tempSum += results;
            } else {
                logger.warn("No data from query: [" + heatQuery + "] and map: ["
                        + keyParamMap + "]");
                dailiesOnly = true;
            }
        }

        /*
         * If we made it through the first section without an error, set heating
         * degree days to the sum found above
         */
        int sumHeat = ParameterFormatClimate.MISSING_DEGREE_DAY;
        if (!dailiesOnly) {
            sumHeat = tempSum;
        }

        /*
         * If an error was encountered, or the begin date is not the first, sum
         * the daily norms. This section is original code
         */
        if (dailiesOnly) {
            tempSum = 0;
            float sumMax = ParameterFormatClimate.MISSING;
            float sumMin = ParameterFormatClimate.MISSING;

            /*
             * determine if there is daily heating degree day data in the data
             * base. We will do this by determining if there are any rows in
             * which the heating degree days aren't set to missing.
             */
            StringBuilder countQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                countQuery = new StringBuilder("SELECT COUNT(*) FROM ");
                countQuery.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                countQuery.append(" WHERE (day_of_year >= :ec_start_date ");
                countQuery.append(" OR day_of_year <= :ec_end_date) ");
                countQuery.append(" AND station_id = :ec_station_id ");
                countQuery.append(" AND heat_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                countQuery.append(" AND day_of_year != '02-29'");
            } else {
                countQuery = new StringBuilder("SELECT COUNT(*) FROM ");
                countQuery.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                countQuery.append(" WHERE day_of_year >= :ec_start_date ");
                countQuery.append(" AND day_of_year <= :ec_end_date ");
                countQuery.append(" AND station_id = :ec_station_id ");
                countQuery.append(" AND heat_day_mean != ")
                        .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
                countQuery.append(" AND day_of_year != '02-29'");
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_start_date", ecStartDate);
                keyParamMap.put("ec_end_date", ecEndDate);

                Object[] results = getDao()
                        .executeSQLQuery(countQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    int ecCountHeat = ((Number) results[0]).intValue();
                    if (ecCountHeat != 0) {
                        StringBuilder heatQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            heatQuery = new StringBuilder(
                                    "SELECT SUM(heat_day_mean) ");
                            heatQuery.append(" FROM ");
                            heatQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            heatQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            heatQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            heatQuery.append(
                                    " AND station_id = :ec_station_id ");
                            heatQuery.append(" AND heat_day_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                            heatQuery.append(" AND day_of_year != '02-29'");
                        } else {
                            heatQuery = new StringBuilder(
                                    "SELECT SUM(heat_day_mean) ");
                            heatQuery.append(" FROM ");
                            heatQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            heatQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            heatQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            heatQuery.append(
                                    " AND station_id = :ec_station_id ");
                            heatQuery.append(" AND heat_day_mean != ").append(
                                    ParameterFormatClimate.MISSING_DEGREE_DAY);
                            heatQuery.append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] res = getDao().executeSQLQuery(
                                    heatQuery.toString(), keyParamMap);
                            // result could be null
                            if ((res != null) && (res.length >= 1)
                                    && (res[0] != null)) {
                                sumHeat = ((Number) res[0]).intValue();
                            } else {
                                logger.warn("No data for query: [" + heatQuery
                                        + "] and map: [" + keyParamMap + "]");
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error summing the heating degree day data: ["
                                            + heatQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                        /*
                         * Climate Migration Discrepancy #55: In the
                         * sum_his_heat function, due to assumed misplacement of
                         * brackets, an entire branch of logic for calculating
                         * daily heating degree days from mean max/min temps
                         * could never possibly be executed. It is assumed that
                         * the branch of logic should be moved, especially to
                         * mirror equivalent functionality in sum_his_cool.
                         */
                    } else {
                        /*
                         * Now historical mean daily heating degree data in the
                         * historical database We need to fall back and
                         * calculate the heating degree days from the daily mean
                         * max and min temperatures
                         */
                        /* sum the max temperatures first */
                        StringBuilder maxTempQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            maxTempQuery = new StringBuilder(
                                    "SELECT SUM(max_temp_mean) ");
                            maxTempQuery.append(" FROM ");
                            maxTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            maxTempQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            maxTempQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            maxTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            maxTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(" AND day_of_year != '02-29'");
                        } else {
                            maxTempQuery = new StringBuilder(
                                    "SELECT SUM(max_temp_mean) ");
                            maxTempQuery.append(" FROM ");
                            maxTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            maxTempQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            maxTempQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            maxTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            maxTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            maxTempQuery.append(" AND day_of_year != '02-29'");
                        }

                        keyParamMap.clear();
                        keyParamMap.put("ec_station_id", stationId);
                        keyParamMap.put("ec_missing_value",
                                ParameterFormatClimate.MISSING);
                        keyParamMap.put("ec_start_date", ecStartDate);
                        keyParamMap.put("ec_end_date", ecEndDate);
                        try {
                            Object[] res = getDao().executeSQLQuery(
                                    maxTempQuery.toString(), keyParamMap);
                            // result could be null
                            if ((res != null) && (res.length >= 1)
                                    && (res[0] != null)) {
                                sumMax = ((Number) res[0]).intValue();
                            } else {
                                logger.warn("No data for query: ["
                                        + maxTempQuery + "] and map: ["
                                        + keyParamMap + "]");
                                sumHeat = ParameterFormatClimate.MISSING_DEGREE_DAY;
                            }
                        } catch (Exception e) {
                            logger.error("Error summing the max temp data: ["
                                    + maxTempQuery + "] map: [" + keyParamMap
                                    + "]", e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                        /* sum the min temperatures next */
                        StringBuilder minTempQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            minTempQuery = new StringBuilder(
                                    "SELECT SUM(min_temp_mean) FROM ");
                            minTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            minTempQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            minTempQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            minTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            minTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            minTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            minTempQuery.append(" AND day_of_year != '02-29'");
                        } else {
                            minTempQuery = new StringBuilder(
                                    "SELECT SUM(min_temp_mean) FROM ");
                            minTempQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            minTempQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            minTempQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            minTempQuery.append(
                                    " AND station_id = :ec_station_id ");
                            minTempQuery.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            minTempQuery.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            minTempQuery.append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] res = getDao().executeSQLQuery(
                                    minTempQuery.toString(), keyParamMap);
                            // result could be null
                            if ((res != null) && (res.length >= 1)
                                    && (res[0] != null)) {
                                sumMin = (int) res[0];
                            } else {
                                logger.warn("No data for query: ["
                                        + minTempQuery + "] and map: ["
                                        + keyParamMap + "]");
                                sumHeat = ParameterFormatClimate.MISSING_DEGREE_DAY;
                            }
                        } catch (Exception e) {
                            logger.error("Error summing the min temp data: ["
                                    + minTempQuery + "] map: [" + keyParamMap
                                    + "]", e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                        /*
                         * Now count the number of rows where there is both a
                         * max and min temperature
                         */
                        StringBuilder countQuery2;
                        if (endDate.getMon() < beginDate.getMon()) {
                            countQuery2 = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            countQuery2.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            countQuery2.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            countQuery2.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            countQuery2.append(
                                    " AND station_id = :ec_station_id ");
                            countQuery2.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            countQuery2.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            countQuery2.append(" AND day_of_year != '02-29'");
                        } else {
                            countQuery2 = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            countQuery2.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            countQuery2.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            countQuery2.append(
                                    " AND day_of_year <= :ec_end_date ");
                            countQuery2.append(
                                    " AND station_id = :ec_station_id ");
                            countQuery2.append(
                                    " AND max_temp_mean != :ec_missing_value ");
                            countQuery2.append(
                                    " AND min_temp_mean != :ec_missing_value ");
                            countQuery2.append(" AND day_of_year != '02-29'");
                        }

                        try {
                            Object[] res = getDao().executeSQLQuery(
                                    countQuery2.toString(), keyParamMap);
                            if ((res != null) && (res.length >= 1)) {
                                int ecCountTemp = ((Number) res[0]).intValue();
                                if (ecCountTemp != 0) {
                                    float sumTemp65 = ecCountTemp * 65.f;
                                    float avgTemp = (sumMax + sumMin) / 2.f;
                                    int iavg = (int) avgTemp;
                                    if ((avgTemp - iavg) >= 0.5) {
                                        iavg = iavg + 1;
                                    }
                                    if (sumTemp65 > iavg) {
                                        sumHeat = (int) (sumTemp65 - iavg);
                                    } else {
                                        sumHeat = 0;
                                    }
                                } else {
                                    return ParameterFormatClimate.MISSING_DEGREE_DAY;
                                }
                            } else {
                                logger.warn("Expected some result from query: ["
                                        + countQuery2 + "] and map: ["
                                        + keyParamMap + "]");
                                sumHeat = ParameterFormatClimate.MISSING_DEGREE_DAY;
                            }
                        } catch (Exception e) {
                            logger.error("Error counting the temp data: ["
                                    + countQuery2 + "] map: [" + keyParamMap
                                    + "]", e);
                            return ParameterFormatClimate.MISSING_DEGREE_DAY;
                        }

                    }
                } else {
                    logger.warn("Expected some result from query: ["
                            + countQuery + "] and map: [" + keyParamMap + "]");
                }

            } catch (Exception e) {
                logger.error(
                        "Error counting the heating degree day data: ["
                                + countQuery + "] map: [" + keyParamMap + "]",
                        e);
                return ParameterFormatClimate.MISSING_DEGREE_DAY;
            }
        }

        /*
         * check for leap year; if true, then add Feb 29th in month and seasonal
         * summations only
         * 
         * Migrated Climate: legacy used to do this work and then check if the
         * current sum is missing; check it now instead to save processing
         */
        if ((beginLeapFlag || endLeapFlag) && (endDate.getMon() == 2)
                && (endDate.getDay() == 29)
                && (sumHeat != ParameterFormatClimate.MISSING_DEGREE_DAY)) {
            StringBuilder query = new StringBuilder(
                    "SELECT heat_day_mean FROM ");
            query.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
            query.append(" WHERE day_of_year = '02-29' ");
            query.append(" AND station_id = :ec_station_id ");
            query.append(" AND heat_day_mean != ")
                    .append(ParameterFormatClimate.MISSING_DEGREE_DAY);

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_station_id", stationId);
                Object[] results = getDao().executeSQLQuery(query.toString(),
                        keyParamMap);
                // result could be null
                if ((results != null) && (results.length >= 1)
                        && (results[0] != null)) {
                    sumHeat += (int) results[0];
                } else {
                    logger.warn("No data for query: [" + query + "] and map: ["
                            + keyParamMap + "]");
                    return sumHeat;
                }
            } catch (Exception e) {
                logger.error("Error summing the cooling degree day data: ["
                        + query + "] map: [" + keyParamMap + "]", e);
                return sumHeat;
            }

        }
        return sumHeat;
    }

    /**
     * Converted from sum_his_snow.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     * void sum_his_snow ( const climate_date &begin_date
     *                const climate_date  &end_date,
     *                  long      *station_id,
     *                               float        *sum_snow    )
     *         Jason Tuell       PRC/TDL             HP 9000/7xx
     *         FUNCTION DESCRIPTION
     *      ====================
     *            This function sums the normal snowfall in the climate 
     *         database between the given period begin date and end date.
     *         It will use monthly normal values for full months within the two 
     *         dates. Then it will add the normal daily values for the final month
     *         of the period if it is a partial month.
     *            Therefore, there are 3 scenarios:
     *            If the period of interest is less than one full month, daily values 
     *            of normal snowfall are summed for each day of the month.
     *            If the period of interest contains one or more full months, monthly
     *            values of normal snowfall are summed for each full month, and daily
     *            values are used to supplement the incomplete month.
     *            If the period of interest contains one or more full months, and
     *            monthly values or normal snowfall are missing, then daily values
     *            of normal snowfall are added for all the days in the period.
     *            In the monthly data cannot be read or the begin date is not the first
     *         day of a month, the function will use only daily normals for the snow sum.
     *         VARIABLES
     *      =========
     *         name                  description
     *    -----------------------------------------------------------------------------
     *       Input
     *         begin_date          - starting date for summation
     *         end_date            - ending date for summation
     *         station_id         - station id of type int for which this function
     *                  is called
     *          Output
     *         sum_snow            - sum of historical snowfall between
     *                               start date and end date
     *            Local
     *         dailies_only        - 0 when the period of interest spans more than one
     *                               month, and the start date is the 1st of a month
     *                             - 1 when the start date is not the 1st of a month, or
     *                               monthly values are missing or invalid
     *                             - 2 when the period of interest is within one month
     *          MODIFICATIONS
     *          May 1999              David T. Miller             PRC/TDL
     *                             Had to add a section so if the period included a leap
     *                             year, the historical values for Feb 29th would also
     *                             be added into the summations.
     *       Feb 2000              David T. Miller             PRC/TDL
     *                             If trace occurred during the period, this routine
     *                             would set it to zero.  Added a check and another
     *                             ESQL call to account for this occurrence.
     *       Apr 2000              David T. Miller             PRC/TDL
     *                             Slight modification for Feb 29th YTD values so they 
     *                             comply with WSOM and NCDC
     *       September 2000        Doug Murphy                 PRC/TDL
     *                             Removed unnecessary include files
     *       January 2001          Doug Murphy                 PRC/MDL
     *                             Major rewrite to take advantage of monthly norms
     *                             that have been introduced - also revised to work
     *                             with monthly, seasonal, and annual reports as well
     *       Dec 2002              Bob Morris                  SAIC/MDL
     *                             - Changed date args to reference variables
     *                             to fix seg. faults under Linux.
     *       Nov 2004              Gary Battel                 SAIC/MDL
     *                             Corrected code which was incorrect when the normal
     *                             values for each day or month within the period of 
     *                             interest is T
     *       Jan 2005              Manan Dalal                 NGIT/MDL
     *                             - Ported from Informix to Postgresql
     ****************************************************************************** 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */
    public float sumHisSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationId) {
        float tempSum = 0.f;
        float sumSnow = ParameterFormatClimate.MISSING_SNOW;

        /* Converting the inputed date structure to a character string */

        String ecStartDate = beginDate.toMonthDayDateString();
        String ecEndDate = endDate.toMonthDayDateString();

        int ecStartMo = beginDate.getMon();

        int ecEndMo = endDate.getMon();
        String ecMonthBegin = endDate.toFirstDayOfMonthDateString();

        /*
         * The begin date of the sum must be the first of the month to take
         * advantage of the monthly norms, otherwise, skip directly to using the
         * daily norms only.
         */
        int dailiesOnly = 0;
        int numMos = 0;
        if (beginDate.getDay() == 1) {
            if (endDate.getMon() < beginDate.getMon()) {
                numMos = ((12 - beginDate.getMon()) + 1)
                        + (endDate.getMon() - 1);
            } else {
                numMos = endDate.getMon() - beginDate.getMon();
            }
        } else {
            dailiesOnly = 1;
        }

        if (numMos == 0) {
            dailiesOnly = 2;
        }

        Map<String, Object> keyParamMap = new HashMap<>();
        /* Use the monthly norms */
        if (dailiesOnly == 0) {
            /*
             * If numMos is >0, we have a date span which covers more than one
             * month (can use the monthly norms for the full months of the span)
             */
            /*
             * Count the number of missing monthly snow values. If there are
             * any, set dailiesOnly to 1 (count the daily data instead).
             */
            StringBuilder countQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                /* Here if our data period spans two calendar years */
                countQuery = new StringBuilder("SELECT COUNT (*) FROM ");
                countQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                countQuery.append(" WHERE (month_of_year >= :ec_start_mo ");
                countQuery.append(" OR month_of_year < :ec_end_mo) ");
                countQuery.append(" AND station_id = :ec_station_id ");
                countQuery.append(" AND period_type = 5 ");
                countQuery.append(" AND snow_pd_mean = :ec_missing_value");
            } else {
                /* Otherwise, data period falls within the same calendar year */
                countQuery = new StringBuilder("SELECT COUNT (*) FROM ");
                countQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                countQuery.append(" WHERE month_of_year >= :ec_start_mo ");
                countQuery.append(" AND month_of_year < :ec_end_mo ");
                countQuery.append(" AND station_id = :ec_station_id ");
                countQuery.append(" AND period_type = 5 ");
                countQuery.append(" AND snow_pd_mean = :ec_missing_value");
            }
            /*
             * If any of the monthly values are missing or if there is a problem
             * with the monthly normals, set the flag to read the daily normals
             */
            keyParamMap.clear();
            keyParamMap.put("ec_start_mo", ecStartMo);
            keyParamMap.put("ec_end_mo", ecEndMo);
            keyParamMap.put("ec_station_id", stationId);
            keyParamMap.put("ec_missing_value",
                    ParameterFormatClimate.MISSING_SNOW);

            int res = ((Number) queryForOneValue(countQuery.toString(),
                    keyParamMap, -1)).intValue();
            if (res != -1) {
                if (res > 0) {
                    dailiesOnly = 1;
                }
            } else {
                logger.warn("Expected some result from query: [" + countQuery
                        + "] and map: [" + keyParamMap + "]");
                dailiesOnly = 1;
            }

        }
        if (dailiesOnly == 0) {
            /* We are here only if there are no missing monthly values */

            /*
             * Sum up the monthly normal snowfall values for each full month of
             * interest
             */
            /*
             * Don't add trace values, since they are stored in the database as
             * -1
             */
            StringBuilder snowSumQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                snowSumQuery = new StringBuilder(
                        "SELECT SUM (snow_pd_mean) FROM ");
                snowSumQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                snowSumQuery.append(" WHERE (month_of_year >= :ec_start_mo ");
                snowSumQuery.append(" OR month_of_year < :ec_end_mo) ");
                snowSumQuery.append(" AND station_id = :ec_station_id ");
                snowSumQuery.append(" AND period_type = 5 ");
                snowSumQuery.append(" AND snow_pd_mean != :ec_trace_value");
            } else {
                snowSumQuery = new StringBuilder(
                        "SELECT SUM(snow_pd_mean) FROM ");
                snowSumQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                snowSumQuery.append(" WHERE month_of_year >= :ec_start_mo ");
                snowSumQuery.append(" AND month_of_year < :ec_end_mo ");
                snowSumQuery.append(" AND station_id = :ec_station_id ");
                snowSumQuery.append(" AND period_type = 5 ");
                snowSumQuery.append(" AND snow_pd_mean != :ec_trace_value");
            }
            /*
             * We may have any of the following conditions:
             * 
             * some or all missing values
             * 
             * an invalid value or a database retrieval error
             * 
             * a value greater than 0
             * 
             * a value equal to 0 without trace values
             * 
             * a value equal to 0 with trace values
             * 
             * We've already dealt with the missing value case above
             */
            /*
             * Let's handle the invalid case by printing an error message and
             * changing the status, so that we can try again later by using
             * daily values
             */
            try {
                keyParamMap.clear();
                keyParamMap.put("ec_start_mo", ecStartMo);
                keyParamMap.put("ec_end_mo", ecEndMo);
                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_trace_value", ParameterFormatClimate.TRACE);

                Object[] res = getDao().executeSQLQuery(snowSumQuery.toString(),
                        keyParamMap);
                // result could be null
                if ((res != null) && (res.length >= 1) && (res[0] != null)) {
                    float ecSumSnow = ((Number) res[0]).floatValue();
                    if (ClimateUtilities.floatingEquals(ecSumSnow, 0)) {
                        /*
                         * If sum is 0, we need to do an additional check to
                         * determine if any of the months contain TRACE, in
                         * which case, we also set temp_sum to TRACE
                         */
                        StringBuilder countQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            /*
                             * Here if our data period spans two calendar years
                             */
                            countQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            countQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            countQuery.append(
                                    " WHERE (month_of_year >= :ec_start_mo ");
                            countQuery
                                    .append(" OR month_of_year < :ec_end_mo) ");
                            countQuery.append(
                                    " AND station_id = :ec_station_id ");
                            countQuery.append(" AND period_type = 5 ");
                            countQuery.append(
                                    " AND snow_pd_mean = :ec_trace_value");
                        } else {
                            /*
                             * Otherwise, data period falls within the same
                             * calendar year
                             */
                            countQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            countQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            countQuery.append(
                                    " WHERE month_of_year >= :ec_start_mo ");
                            countQuery
                                    .append(" AND month_of_year < :ec_end_mo ");
                            countQuery.append(
                                    " AND station_id = :ec_station_id ");
                            countQuery.append(" AND period_type = 5 ");
                            countQuery.append(
                                    " AND snow_pd_mean = :ec_trace_value");
                        }

                        int res2 = ((Number) queryForOneValue(
                                countQuery.toString(), keyParamMap, -1))
                                        .intValue();
                        if (res2 != -1) {
                            if (res2 > 0) {
                                tempSum = ParameterFormatClimate.TRACE;
                            } else {
                                tempSum = 0.f;
                            }
                        } else {
                            logger.warn("Expected some result for query: ["
                                    + countQuery + "] and map: [" + keyParamMap
                                    + "]");
                        }
                    } else {
                        tempSum = ecSumSnow;
                    }
                } else {
                    /*
                     * If all the months contain a trace, then set the value to
                     * trace
                     */
                    tempSum = ParameterFormatClimate.TRACE;
                }
            } catch (Exception e) {
                logger.error("Error querying the climate database: ["
                        + snowSumQuery + "] map: [" + keyParamMap + "]", e);
                dailiesOnly = 1;
            }

        }

        /*
         * We're here if our period of interest does not span more than one
         * month. We're also here if we just added up the monthly normals and
         * we're completing the calculation with daily normals for a partial
         * month.
         */
        if (dailiesOnly != 1) {
            float ecSumSnow = ParameterFormatClimate.MISSING_SNOW;
            if (endDate.getDay() >= MAX_DAYS_PER_MONTH[endDate.getMon() - 1]) {
                /*
                 * If the end day is the end of the month, can use the monthly
                 * norm... otherwise, use the daily norms for the last month of
                 * the span
                 */
                StringBuilder query = new StringBuilder(
                        "SELECT snow_pd_mean FROM ");
                query.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                query.append(" WHERE station_id = :ec_station_id ");
                query.append(" AND month_of_year = :ec_end_mo ");
                query.append(" AND period_type = 5");

                /*
                 * If this monthly value is missing and we're dealing with one
                 * month only, set the snow sum to missing and exit
                 */
                ecSumSnow = ParameterFormatClimate.MISSING_SNOW;
                try {
                    keyParamMap.clear();
                    keyParamMap.put("ec_end_mo", ecEndMo);
                    keyParamMap.put("ec_station_id", stationId);

                    Object[] res = getDao().executeSQLQuery(query.toString(),
                            keyParamMap);
                    // result could be null
                    if ((res != null) && (res.length >= 1)
                            && (res[0] != null)) {
                        ecSumSnow = (float) res[0];
                    } else {
                        logger.warn("No data for query: [" + query
                                + "] and map: [" + keyParamMap + "]");
                        return ParameterFormatClimate.MISSING_SNOW;
                    }
                } catch (Exception e) {
                    logger.error("Error querying the climate database: ["
                            + query + "] map: [" + keyParamMap + "]", e);
                    return ParameterFormatClimate.MISSING_SNOW;
                }

                if (((dailiesOnly == 2) && ClimateUtilities.floatingEquals(
                        ecSumSnow, ParameterFormatClimate.MISSING_SNOW))) {
                    return ParameterFormatClimate.MISSING_SNOW;
                }
            } else {
                /*
                 * We don't want to sum missing value indicators or trace values
                 * or leap day, but we need to work with trace values later
                 */
                StringBuilder snowSumQuery = new StringBuilder(
                        "SELECT SUM(snow_mean) FROM ");
                snowSumQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                snowSumQuery.append(" WHERE day_of_year >= :ec_month_begin ");
                snowSumQuery.append(" AND day_of_year <= :ec_end_date ");
                snowSumQuery.append(" AND station_id = :ec_station_id ");
                snowSumQuery.append(" AND snow_mean != :ec_missing_value ");
                snowSumQuery.append(" AND snow_mean != :ec_trace_value ");
                snowSumQuery.append(" AND day_of_year !='02-29'");

                /*
                 * If nullSumIndicator = DBNULL, there is some combination of
                 * trace, and/or missing values. If any of the values is a
                 * trace, the sum should be changed to T. If all the values are
                 * missing, and this is the only month we're dealing with, set
                 * the snow sum to missing and exit.
                 */
                /* printf ("Null indicator is %d\n", nullSumIndicator); */
                ecSumSnow = ParameterFormatClimate.MISSING_SNOW;
                try {
                    keyParamMap.clear();

                    keyParamMap.put("ec_station_id", stationId);
                    keyParamMap.put("ec_missing_value",
                            ParameterFormatClimate.MISSING_SNOW);
                    keyParamMap.put("ec_trace_value",
                            ParameterFormatClimate.TRACE);
                    keyParamMap.put("ec_end_date", ecEndDate);
                    keyParamMap.put("ec_month_begin", ecMonthBegin);
                    Object[] sumSnowRes = getDao().executeSQLQuery(
                            snowSumQuery.toString(), keyParamMap);
                    if ((sumSnowRes != null) && (sumSnowRes.length >= 1)) {
                        ecSumSnow = (sumSnowRes[0] == null ? 0
                                : ((Number) sumSnowRes[0]).floatValue());
                        if (ClimateUtilities.floatingEquals(ecSumSnow, 0)) {
                            StringBuilder snowCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            snowCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            snowCountQuery.append(
                                    " WHERE day_of_year >= :ec_month_begin ");
                            snowCountQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            snowCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            snowCountQuery
                                    .append(" AND snow_mean = :ec_trace_value");
                            try {
                                keyParamMap.clear();
                                keyParamMap.put("ec_station_id", stationId);
                                keyParamMap.put("ec_trace_value",
                                        ParameterFormatClimate.TRACE);
                                keyParamMap.put("ec_end_date", ecEndDate);
                                keyParamMap.put("ec_month_begin", ecMonthBegin);
                                int countSnowRes = ((Number) queryForOneValue(
                                        snowCountQuery.toString(), keyParamMap,
                                        -1)).intValue();
                                if (countSnowRes != -1) {
                                    if (countSnowRes > 0) {
                                        ecSumSnow = ParameterFormatClimate.TRACE;
                                    }
                                } else {
                                    logger.warn(
                                            "Expected some result for query: ["
                                                    + snowCountQuery
                                                    + "] and map: ["
                                                    + keyParamMap + "]");
                                }
                            } catch (Exception e) {
                                logger.error(
                                        "Error querying the climate database: ["
                                                + snowCountQuery + "] map: ["
                                                + keyParamMap + "]",
                                        e);
                            }
                        }
                    } else {
                        StringBuilder snowCountQuery = new StringBuilder(
                                "SELECT COUNT(*) FROM ");
                        snowCountQuery.append(
                                ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                        snowCountQuery.append(
                                " WHERE day_of_year >= :ec_month_begin ");
                        snowCountQuery
                                .append(" AND day_of_year <= :ec_end_date ");
                        snowCountQuery
                                .append(" AND station_id = :ec_station_id ");
                        snowCountQuery
                                .append(" AND snow_mean = :ec_trace_value");

                        try {
                            keyParamMap.clear();
                            keyParamMap.put("ec_station_id", stationId);
                            keyParamMap.put("ec_trace_value",
                                    ParameterFormatClimate.TRACE);
                            keyParamMap.put("ec_end_date", ecEndDate);
                            keyParamMap.put("ec_month_begin", ecMonthBegin);
                            Object[] countSnowRes = getDao().executeSQLQuery(
                                    snowCountQuery.toString(), keyParamMap);
                            if ((countSnowRes != null)
                                    && (countSnowRes.length >= 1)) {
                                int ecCountSnow = (countSnowRes[0] == null ? 0
                                        : ((Number) countSnowRes[0]))
                                                .intValue();
                                if (ecCountSnow > 0) {
                                    ecSumSnow = ParameterFormatClimate.TRACE;
                                } else if (dailiesOnly == 2) {
                                    sumSnow = ParameterFormatClimate.MISSING_SNOW;
                                    return sumSnow;
                                } else {
                                    ecSumSnow = ParameterFormatClimate.MISSING_SNOW;
                                }
                            } else {
                                logger.warn("Expected some result for query: ["
                                        + snowCountQuery + "] and map: ["
                                        + keyParamMap + "]");
                                return ParameterFormatClimate.MISSING_SNOW;
                            }
                        } catch (Exception e) {
                            logger.error(
                                    "Error querying the climate database: ["
                                            + snowCountQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_SNOW;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error querying the climate database: ["
                            + snowSumQuery + "] map: [" + keyParamMap + "]", e);
                }
            }
            if (ClimateUtilities.floatingEquals(tempSum,
                    ParameterFormatClimate.TRACE) && (ecSumSnow > 0.0f)) {
                tempSum = ecSumSnow;
            } else if (ClimateUtilities.floatingEquals(ecSumSnow,
                    ParameterFormatClimate.TRACE)
                    && ClimateUtilities.floatingEquals(tempSum, 0.0f)) {
                tempSum = ParameterFormatClimate.TRACE;
            } else if (ecSumSnow > ParameterFormatClimate.TRACE) {
                tempSum += ecSumSnow;
            }
            sumSnow = tempSum;
        } else {
            /*
             * If the begin date is not the first, or the monthly data is not
             * available, sum the daily norms.
             */
            StringBuilder snowSumQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                snowSumQuery = new StringBuilder("SELECT SUM(snow_mean) FROM ");
                snowSumQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                snowSumQuery.append(" WHERE (day_of_year >= :ec_start_date ");
                snowSumQuery.append(" OR day_of_year <= :ec_end_date) ");
                snowSumQuery.append(" AND station_id = :ec_station_id ");
                snowSumQuery.append(" AND snow_mean != :ec_missing_value ");
                snowSumQuery.append(" AND snow_mean != :ec_trace_value ");
                snowSumQuery.append(" AND day_of_year !='02-29'");
            } else {
                snowSumQuery = new StringBuilder("SELECT SUM(snow_mean) FROM ");
                snowSumQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                snowSumQuery.append(" WHERE day_of_year >= :ec_start_date ");
                snowSumQuery.append(" AND day_of_year <= :ec_end_date ");
                snowSumQuery.append(" AND station_id = :ec_station_id ");
                snowSumQuery.append(" AND snow_mean != :ec_missing_value ");
                snowSumQuery.append(" AND snow_mean != :ec_trace_value ");
                snowSumQuery.append(" AND day_of_year !='02-29'");
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_missing_value",
                        ParameterFormatClimate.MISSING_SNOW);
                keyParamMap.put("ec_trace_value", ParameterFormatClimate.TRACE);
                keyParamMap.put("ec_start_date", ecStartDate);
                keyParamMap.put("ec_end_date", ecEndDate);

                Object[] results = getDao()
                        .executeSQLQuery(snowSumQuery.toString(), keyParamMap);
                // result could be null
                if ((results != null) && (results.length >= 1)
                        && (results[0] != null)) {
                    float ecSumSnow = ((Number) results[0]).floatValue();

                    /*
                     * If the value sum is 0, there might be some TRACE values,
                     * in which case, set the sum to TRACE.
                     */
                    if (ClimateUtilities.floatingEquals(ecSumSnow, 0)) {
                        StringBuilder snowCountQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            snowCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            snowCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            snowCountQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            snowCountQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            snowCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            snowCountQuery
                                    .append(" AND snow_mean = :ec_trace_value");

                        } else {
                            snowCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            snowCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            snowCountQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            snowCountQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            snowCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            snowCountQuery
                                    .append(" AND snow_mean = :ec_trace_value");
                        }
                        keyParamMap.clear();
                        keyParamMap.put("ec_station_id", stationId);
                        keyParamMap.put("ec_trace_value",
                                ParameterFormatClimate.TRACE);
                        keyParamMap.put("ec_start_date", ecStartDate);
                        keyParamMap.put("ec_end_date", ecEndDate);

                        int res = ((Number) queryForOneValue(
                                snowCountQuery.toString(), keyParamMap, -1))
                                        .intValue();
                        if (res != -1) {
                            if (res > 0) {
                                sumSnow = ParameterFormatClimate.TRACE;
                            } else {
                                sumSnow = 0.f;
                            }
                        } else {
                            logger.warn("Expected some result from query: ["
                                    + snowCountQuery + "] and map: ["
                                    + keyParamMap + "]");
                        }
                    } else {
                        sumSnow = ecSumSnow;
                    }
                } else {
                    /*
                     * If nullSumIndicator = DBNULL, there might be some days
                     * with a T of snow. In this case, the sum should be changed
                     * to T. Otherwise, they values must all be
                     * ClimateFormatParameter.MISSING.
                     */
                    StringBuilder snowCountQuery;
                    if (endDate.getMon() < beginDate.getMon()) {
                        snowCountQuery = new StringBuilder(
                                "SELECT COUNT(*) FROM ");
                        snowCountQuery.append(
                                ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                        snowCountQuery.append(
                                " WHERE (day_of_year >= :ec_start_date ");
                        snowCountQuery
                                .append(" OR day_of_year <= :ec_end_date) ");
                        snowCountQuery
                                .append(" AND station_id = :ec_station_id ");
                        snowCountQuery
                                .append(" AND snow_mean = :ec_trace_value");

                    } else {
                        snowCountQuery = new StringBuilder(
                                "SELECT COUNT(*) FROM ");
                        snowCountQuery.append(
                                ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                        snowCountQuery.append(
                                " WHERE day_of_year >= :ec_start_date ");
                        snowCountQuery
                                .append(" AND day_of_year <= :ec_end_date ");
                        snowCountQuery
                                .append(" AND station_id = :ec_station_id ");
                        snowCountQuery
                                .append(" AND snow_mean = :ec_trace_value");
                    }
                    keyParamMap.clear();
                    keyParamMap.put("ec_station_id", stationId);
                    keyParamMap.put("ec_trace_value",
                            ParameterFormatClimate.TRACE);
                    keyParamMap.put("ec_start_date", ecStartDate);
                    keyParamMap.put("ec_end_date", ecEndDate);
                    int res = ((Number) queryForOneValue(
                            snowCountQuery.toString(), keyParamMap, -1))
                                    .intValue();
                    if (res != -1) {
                        if (res > 0) {
                            sumSnow = ParameterFormatClimate.TRACE;
                        } else {
                            sumSnow = ParameterFormatClimate.MISSING_SNOW;
                        }
                    } else {
                        logger.warn("Expected some result from query: ["
                                + snowCountQuery + "] and map: [" + keyParamMap
                                + "]");
                    }
                }
            } catch (Exception e) {
                sumSnow = ParameterFormatClimate.MISSING_SNOW;
                logger.error("Error querying the climate database: ["
                        + snowSumQuery + "] map: [" + keyParamMap + "]", e);
            }

        }

        return sumSnow;
    }

    /**
     * Converted from sum_his_precip.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     * void sum_his_precip ( const climate_date   &begin_date
     *                const climate_date    &end_date,
     *                long      *station_id,
     *                             float        *sum_precip  )
     * 
     *    Jason Tuell       PRC/TDL             HP 9000/7xx
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *       The purpose of this function is to calculate the normal value of 
     *       precipitation for the period of interest.
     *       This function sums the normal precipitation in the climate 
     *       database between the given period begin date and end date.
     *       It will use monthly normal values for full months within the two 
     *       dates. Then it will add the normal daily values for the final month
     *       of the period if it is a partial month. 
     * 
     *       Therefore, there are 3 scenarios: 
     *          If the period of interest is less than one full month, daily values of 
     *          normal precipitation are summed for each day of the month.
     *          If the period of interest contains one or more full months, monthly 
     *          values of normal precipitation are summed for each full month, and the
     *          daily values of normal precipitation are added for the remaining days 
     *          of the incomplete month. 
     *          If the period of interest contains one or more full months and monthly 
     *          values of normal precipitation are missing, then daily values of 
     *          normal precip are added for all the days in the period of interest. 
     * 
     *       If the begin date is not the first day of a month, the function will use
     *       only daily normals for the precip sum.
     * 
     *    VARIABLES
     *    =========
     * 
     *    name                  description
     * ------------------------------------------------------------------------------
     *     Input
     *       begin_date          - starting date for summation
     *       end_date            - ending date for summation
     *       station_id         - station id of type int for which this function
     *                is called
     * 
     *     Output
     *       sum_precip          - sum of precipitation between
     *                             start date and end date
     * 
     *       Local
     *       dailies_only        - 0 when the period of interest spans more than one
     *                               month, and the start date is the 1st of a month
     *                             1 when the start date is not the 1st of a month, or 
     *                               monthly values are missing or invalid
     *                             2 when the period of interest is within one month
     * 
     *     MODIFICATIONS
     * 
     *     May 1999              David T. Miller             PRC/TDL
     *                           Had to add a section so if the period included a leap
     *                           year, the historical values for Feb 29th would also
     *                           be added into the summations.
     *     Feb 2000              David T. Miller             PRC/TDL
     *                           If trace occurred during the period, this routine
     *                           would set it to zero.  Added a check and another
     *                           ESQL call to account for this occurrence.
     *     Apr 2000              David T. Miller             PRC/TDL
     *                           Slight modification to Feb 29th YTD totals so code
     *                           complies with WSOM and NCDC
     *     September 2000        Doug Murphy                 PRC/TDL
     *                          Removed unnecessary include files
     *    January 2001          Doug Murphy                 PRC/MDL
     *                          Major rewrite to take advantage of monthly norms
     *                          that have been introduced - also revised to work
     *                          with monthly, seasonal, and annual reports as well
     *    Dec 2002              Bob Morris                  SAIC/MDL
     *                          - Changed date args to reference variables
     *                          to fix seg. faults under Linux.
     *    Nov 2004              Gary Battel                 SAIC/MDL
     *                          Code is incorrect when the normal values for each day 
     *                          or month within the period of record is T.
     *    Jan 2005              Manan Dalal                 NGIT/MDL
     *                          - Ported code from Informix to Postgresql
     * 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */
    public float sumHisPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationId) {
        float tempSum = 0.f;
        float sumPrecip = ParameterFormatClimate.MISSING_PRECIP;

        /* Converting the inputed date structure to a character string */

        String ecStartDate = beginDate.toMonthDayDateString();
        String ecEndDate = endDate.toMonthDayDateString();

        int ecStartMo = beginDate.getMon();

        int ecEndMo = endDate.getMon();
        String ecMonthBegin = endDate.toFirstDayOfMonthDateString();

        /*
         * The begin date of the sum must be the first of the month to take
         * advantage of the monthly norms, otherwise, skip directly to using the
         * daily norms only.
         */
        int dailiesOnly = 0;
        int numMos = 0;
        if (beginDate.getDay() == 1) {
            if (endDate.getMon() < beginDate.getMon()) {
                numMos = ((12 - beginDate.getMon()) + 1)
                        + (endDate.getMon() - 1);
            } else {
                numMos = endDate.getMon() - beginDate.getMon();
            }
        } else {
            dailiesOnly = 1;
        }

        if (numMos == 0) {
            dailiesOnly = 2;
        }

        Map<String, Object> keyParamMap = new HashMap<>();
        /* Use the monthly norms */
        if (dailiesOnly == 0) {
            /*
             * If numMos is >0, we have a date span which covers more than one
             * month (can use the monthly norms for the full months of the span)
             */
            /*
             * Count the number of missing monthly precip values. If there are
             * any, set the indicator to count the daily data instead
             */
            StringBuilder countQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                /* Here if our data period spans two calendar years */
                countQuery = new StringBuilder("SELECT COUNT (*) FROM ");
                countQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                countQuery.append(" WHERE (month_of_year >= :ec_start_mo ");
                countQuery.append(" OR month_of_year < :ec_end_mo) ");
                countQuery.append(" AND station_id = :ec_station_id ");
                countQuery.append(" AND period_type = 5 ");
                countQuery.append(" AND precip_pd_mean = :ec_missing_value");
            } else {
                /* Otherwise, data period falls within the same calendar year */
                countQuery = new StringBuilder("SELECT COUNT (*) FROM ");
                countQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                countQuery.append(" WHERE month_of_year >= :ec_start_mo ");
                countQuery.append(" AND month_of_year < :ec_end_mo ");
                countQuery.append(" AND station_id = :ec_station_id ");
                countQuery.append(" AND period_type = 5 ");
                countQuery.append(" AND precip_pd_mean = :ec_missing_value");
            }
            /*
             * If any of the monthly values are missing, or if there is a
             * problem reading the monthly normals, set the indicator to read
             * the daily normals
             */
            keyParamMap.clear();
            keyParamMap.put("ec_start_mo", ecStartMo);
            keyParamMap.put("ec_end_mo", ecEndMo);
            keyParamMap.put("ec_station_id", stationId);
            keyParamMap.put("ec_missing_value",
                    ParameterFormatClimate.MISSING_PRECIP);

            int res = ((Number) queryForOneValue(countQuery.toString(),
                    keyParamMap, -1)).intValue();

            if (res != -1) {
                if (res > 0) {
                    dailiesOnly = 1;
                }
            } else {
                logger.warn("Expected some result from query: [" + countQuery
                        + "] and map: [" + keyParamMap + "]");
                dailiesOnly = 1;
            }
        }

        if (dailiesOnly == 0) {
            /* If no missing monthly values, we're here. */
            /*
             * Sum up the monthly normal precip values for each full month of
             * interest.
             */
            /*
             * Don't add trace values, since they are stored in the database as
             * -1. Once again, we need to do this database addition differently
             * if the period of record spans more than one calendar year
             */
            StringBuilder precipSumQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                precipSumQuery = new StringBuilder(
                        "SELECT SUM (precip_pd_mean) FROM ");
                precipSumQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                precipSumQuery.append(" WHERE (month_of_year >= :ec_start_mo ");
                precipSumQuery.append(" OR month_of_year < :ec_end_mo) ");
                precipSumQuery.append(" AND station_id = :ec_station_id ");
                precipSumQuery.append(" AND period_type = 5 ");
                precipSumQuery.append(" AND precip_pd_mean != :ec_trace_value");
            } else {
                precipSumQuery = new StringBuilder(
                        "SELECT SUM (precip_pd_mean) FROM ");
                precipSumQuery
                        .append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                precipSumQuery.append(" WHERE month_of_year >= :ec_start_mo ");
                precipSumQuery.append(" AND month_of_year < :ec_end_mo ");
                precipSumQuery.append(" AND station_id = :ec_station_id ");
                precipSumQuery.append(" AND period_type = 5 ");
                precipSumQuery.append(" AND precip_pd_mean != :ec_trace_value");
            }
            /*
             * We may have any of the following conditions:
             * 
             * some or all missing values
             * 
             * an invalid value or a summing error
             * 
             * a value greater than 0
             * 
             * a value equal to 0 without trace values
             * 
             * a value equal to 0, but with trace values we've already handled
             * the cases where any of the months were missing
             */
            try {
                keyParamMap.clear();
                keyParamMap.put("ec_start_mo", ecStartMo);
                keyParamMap.put("ec_end_mo", ecEndMo);
                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_trace_value", ParameterFormatClimate.TRACE);

                Object[] res = getDao().executeSQLQuery(
                        precipSumQuery.toString(), keyParamMap);

                // result could be null
                if ((res != null) && (res.length >= 1) && (res[0] != null)) {
                    float ecSumPrecip = ((Number) res[0]).floatValue();
                    if (ClimateUtilities.floatingEquals(ecSumPrecip, 0)) {
                        /*
                         * If sum is 0, we need to do an additional check to
                         * determine if any of the months contain TRACE, in
                         * which case, we also set tempSum to TRACE
                         */
                        StringBuilder precipCountQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            /*
                             * Here if our data period spans two calendar years
                             */
                            precipCountQuery = new StringBuilder(
                                    "SELECT COUNT (*) FROM ");
                            precipCountQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            precipCountQuery.append(
                                    " WHERE (month_of_year >= :ec_start_mo ");
                            precipCountQuery
                                    .append(" OR month_of_year < :ec_end_mo) ");
                            precipCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            precipCountQuery.append(" AND period_type = 5 ");
                            precipCountQuery.append(
                                    " AND precip_pd_mean = :ec_trace_value");
                        } else {
                            /*
                             * Otherwise, data period falls within the same
                             * calendar year
                             */
                            precipCountQuery = new StringBuilder(
                                    "SELECT COUNT (*) FROM ");
                            precipCountQuery.append(
                                    ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                            precipCountQuery.append(
                                    " WHERE month_of_year >= :ec_start_mo ");
                            precipCountQuery
                                    .append(" AND month_of_year < :ec_end_mo ");
                            precipCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            precipCountQuery.append(" AND period_type = 5 ");
                            precipCountQuery.append(
                                    " AND precip_pd_mean = :ec_trace_value");
                        }

                        keyParamMap.clear();
                        keyParamMap.put("ec_start_mo", ecStartMo);
                        keyParamMap.put("ec_end_mo", ecEndMo);
                        keyParamMap.put("ec_station_id", stationId);
                        keyParamMap.put("ec_trace_value",
                                ParameterFormatClimate.TRACE);

                        int res2 = ((Number) queryForOneValue(
                                precipCountQuery.toString(), keyParamMap, -1))
                                        .intValue();

                        if (res2 != -1) {
                            if (res2 > 0) {
                                tempSum = ParameterFormatClimate.TRACE;
                            } else {
                                tempSum = 0.f;
                            }
                        } else {
                            logger.warn("Expected some result for query: ["
                                    + precipCountQuery + "] and map: ["
                                    + keyParamMap + "]");
                        }
                    } else {
                        tempSum = ecSumPrecip;
                    }
                } else {
                    /*
                     * If all the months contain a trace, then set our sum equal
                     * to trace
                     */
                    tempSum = ParameterFormatClimate.TRACE;
                }
            } catch (Exception e) {
                /*
                 * First, let's handle the invalid value case by printing an
                 * error message and changing the status, so that we try again
                 * later with daily values.
                 */
                logger.error("Error querying the climate database: ["
                        + precipSumQuery + "] map: [" + keyParamMap + "]", e);
                dailiesOnly = 1;
            }
        }

        /*
         * We're here if our period of interest does not span more than one
         * month. We're also here if we just added up the monthly normals, and
         * we're completing the calculation with daily normals for a partial
         * month.
         */
        if (dailiesOnly != 1) {
            float ecSumPrecip = ParameterFormatClimate.MISSING_PRECIP;
            if (endDate.getDay() >= MAX_DAYS_PER_MONTH[endDate.getMon() - 1]) {
                /*
                 * If the end day is the end of the month, can use the monthly
                 * norm... otherwise, use the daily norms for the last month of
                 * the span
                 */
                StringBuilder query = new StringBuilder(
                        "SELECT precip_pd_mean FROM ");
                query.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
                query.append(" WHERE station_id = :ec_station_id ");
                query.append(" AND month_of_year = :ec_end_mo ");
                query.append(" AND period_type = 5");

                /*
                 * If this monthly value is missing and we're dealing with one
                 * month only, set the precip sum to missing and exit.
                 * Otherwise, treat it as 0, because we'll be using values from
                 * other months instead.
                 */
                ecSumPrecip = ParameterFormatClimate.MISSING_PRECIP;
                try {
                    keyParamMap.clear();

                    keyParamMap.put("ec_end_mo", ecEndMo);
                    keyParamMap.put("ec_station_id", stationId);

                    Object[] res = getDao().executeSQLQuery(query.toString(),
                            keyParamMap);

                    // result could be null
                    if ((res != null) && (res.length >= 1)
                            && (res[0] != null)) {
                        ecSumPrecip = (float) res[0];
                    } else {
                        logger.warn("No data for query: [" + query
                                + "] and map: [" + keyParamMap + "]");
                        return ParameterFormatClimate.MISSING_PRECIP;
                    }
                } catch (Exception e) {
                    logger.error("Error querying the climate database: ["
                            + query + "] map: [" + keyParamMap + "]", e);
                    return ParameterFormatClimate.MISSING_PRECIP;
                }

                if (((dailiesOnly == 2) && ClimateUtilities.floatingEquals(
                        ecSumPrecip, ParameterFormatClimate.MISSING_PRECIP))) {
                    return ParameterFormatClimate.MISSING_PRECIP;
                }
            } else {
                /*
                 * We don't want to sum missing value indicators or trace values
                 * or leap day, but we need to work with trace values later
                 */
                StringBuilder sumPrecipQuery = new StringBuilder(
                        "SELECT SUM(precip_mean) FROM ");
                sumPrecipQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                sumPrecipQuery.append(" WHERE day_of_year >= :ec_month_begin ");
                sumPrecipQuery.append(" AND day_of_year <= :ec_end_date ");
                sumPrecipQuery.append(" AND station_id = :ec_station_id ");
                sumPrecipQuery.append(" AND precip_mean != :ec_missing_value ");
                sumPrecipQuery.append(" AND precip_mean != :ec_trace_value ");
                sumPrecipQuery.append(" AND day_of_year !='02-29'");

                /*
                 * If nullSumIndicator = DBNULL, there is some combination of
                 * trace, and/or missing values. If any of the values is a
                 * trace, the sum should be changed to T. If all the values are
                 * missing and this is the only month we're dealing with, set
                 * the precip sum to missing and exit
                 */
                ecSumPrecip = ParameterFormatClimate.MISSING_PRECIP;
                try {
                    keyParamMap.clear();

                    keyParamMap.put("ec_station_id", stationId);
                    keyParamMap.put("ec_missing_value",
                            ParameterFormatClimate.MISSING_PRECIP);
                    keyParamMap.put("ec_trace_value",
                            ParameterFormatClimate.TRACE);
                    keyParamMap.put("ec_end_date", ecEndDate);
                    keyParamMap.put("ec_month_begin", ecMonthBegin);

                    Object[] sumPrecipRes = getDao().executeSQLQuery(
                            sumPrecipQuery.toString(), keyParamMap);

                    // result could be null
                    if ((sumPrecipRes != null) && (sumPrecipRes.length >= 1)) {
                        ecSumPrecip = (sumPrecipRes[0] == null ? 0
                                : ((Number) sumPrecipRes[0]).floatValue());
                        if (ClimateUtilities.floatingEquals(ecSumPrecip, 0)) {
                            StringBuilder sumCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            sumCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            sumCountQuery.append(
                                    " WHERE day_of_year >= :ec_month_begin ");
                            sumCountQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            sumCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            /*
                             * Climate Migration discrepancy #54: In the
                             * sum_his_precip function, if no non-trace precip
                             * values could be found, Legacy checks for trace
                             * snow values. It is assumed this a bug, and
                             * instead the function should check for trace
                             * precip values.
                             */
                            sumCountQuery.append(
                                    " AND precip_mean = :ec_trace_value");
                            try {
                                keyParamMap.clear();
                                keyParamMap.put("ec_station_id", stationId);
                                keyParamMap.put("ec_trace_value",
                                        ParameterFormatClimate.TRACE);
                                keyParamMap.put("ec_end_date", ecEndDate);
                                keyParamMap.put("ec_month_begin", ecMonthBegin);
                                int countPrecipRes = ((Number) queryForOneValue(
                                        sumCountQuery.toString(), keyParamMap,
                                        -1)).intValue();

                                if (countPrecipRes != -1) {
                                    if (countPrecipRes > 0) {
                                        ecSumPrecip = ParameterFormatClimate.TRACE;
                                    }
                                } else {
                                    logger.warn(
                                            "Expected some result for query: ["
                                                    + sumCountQuery
                                                    + "] and map: ["
                                                    + keyParamMap + "]");
                                }
                            } catch (Exception e) {
                                logger.error(
                                        "Error querying the climate database: ["
                                                + sumCountQuery + "] map: ["
                                                + keyParamMap + "]",
                                        e);
                            }

                        }
                    } else {
                        /*
                         * If null/empty results there is some combination of
                         * trace, and/or missing values. If any of the values is
                         * a trace, the sum should be changed to T. If all the
                         * values are missing and this is the only month we're
                         * dealing with, set the precip sum to missing and exit
                         */
                        StringBuilder sumCountQuery = new StringBuilder(
                                "SELECT COUNT(*) FROM ");
                        sumCountQuery.append(
                                ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                        sumCountQuery.append(
                                " WHERE day_of_year >= :ec_month_begin ");
                        sumCountQuery
                                .append(" AND day_of_year <= :ec_end_date ");
                        sumCountQuery
                                .append(" AND station_id = :ec_station_id ");
                        sumCountQuery
                                .append(" AND precip_mean = :ec_trace_value");

                        try {
                            keyParamMap.clear();
                            keyParamMap.put("ec_station_id", stationId);
                            keyParamMap.put("ec_trace_value",
                                    ParameterFormatClimate.TRACE);
                            keyParamMap.put("ec_end_date", ecEndDate);
                            keyParamMap.put("ec_month_begin", ecMonthBegin);
                            Object[] countPrecipRes = getDao().executeSQLQuery(
                                    sumCountQuery.toString(), keyParamMap);

                            if ((countPrecipRes != null)
                                    && (countPrecipRes.length >= 1)) {
                                int ecCountPrecip = (countPrecipRes[0] == null
                                        ? 0
                                        : ((Number) countPrecipRes[0])
                                                .intValue());
                                if (ecCountPrecip > 0) {
                                    ecSumPrecip = ParameterFormatClimate.TRACE;
                                } else if (dailiesOnly == 2) {
                                    sumPrecip = ParameterFormatClimate.MISSING_PRECIP;
                                    return sumPrecip;
                                } else {
                                    ecSumPrecip = ParameterFormatClimate.MISSING_PRECIP;
                                }
                            } else {
                                logger.warn("Expected some result for query: ["
                                        + sumCountQuery + "] and map: ["
                                        + keyParamMap + "]");
                                return ParameterFormatClimate.MISSING_PRECIP;
                            }

                        } catch (Exception e) {
                            logger.error(
                                    "Error querying the climate database: ["
                                            + sumCountQuery + "] map: ["
                                            + keyParamMap + "]",
                                    e);
                            return ParameterFormatClimate.MISSING_PRECIP;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error querying the climate database: ["
                            + sumPrecipQuery + "] map: [" + keyParamMap + "]",
                            e);
                }

            }

            if (ClimateUtilities.floatingEquals(tempSum,
                    ParameterFormatClimate.TRACE) && (ecSumPrecip > 0.0f)) {
                tempSum = ecSumPrecip;
            } else if (ClimateUtilities.floatingEquals(ecSumPrecip,
                    ParameterFormatClimate.TRACE)
                    && ClimateUtilities.floatingEquals(tempSum, 0.0f)) {
                tempSum = ParameterFormatClimate.TRACE;
            } else if (ecSumPrecip > ParameterFormatClimate.TRACE) {
                tempSum += ecSumPrecip;
            }
            sumPrecip = tempSum;
        } else {
            /*
             * If the begin date is not the first, or the the monthly data is
             * not available, sum the daily norms.
             */
            StringBuilder sumPrecipQuery;
            if (endDate.getMon() < beginDate.getMon()) {
                sumPrecipQuery = new StringBuilder(
                        "SELECT SUM(precip_mean) FROM ");
                sumPrecipQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                sumPrecipQuery.append(" WHERE (day_of_year >= :ec_start_date ");
                sumPrecipQuery.append(" OR day_of_year <= :ec_end_date) ");
                sumPrecipQuery.append(" AND station_id = :ec_station_id ");
                sumPrecipQuery.append(" AND precip_mean != :ec_missing_value ");
                sumPrecipQuery.append(" AND precip_mean != :ec_trace_value ");
                sumPrecipQuery.append(" AND day_of_year !='02-29'");
            } else {
                sumPrecipQuery = new StringBuilder(
                        "SELECT SUM(precip_mean) FROM ");
                sumPrecipQuery
                        .append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                sumPrecipQuery.append(" WHERE day_of_year >= :ec_start_date ");
                sumPrecipQuery.append(" AND day_of_year <= :ec_end_date ");
                sumPrecipQuery.append(" AND station_id = :ec_station_id ");
                sumPrecipQuery.append(" AND precip_mean != :ec_missing_value ");
                sumPrecipQuery.append(" AND precip_mean != :ec_trace_value ");
                sumPrecipQuery.append(" AND day_of_year !='02-29'");
            }

            try {
                keyParamMap.clear();
                keyParamMap.put("ec_station_id", stationId);
                keyParamMap.put("ec_missing_value",
                        ParameterFormatClimate.MISSING_PRECIP);
                keyParamMap.put("ec_trace_value", ParameterFormatClimate.TRACE);
                keyParamMap.put("ec_start_date", ecStartDate);
                keyParamMap.put("ec_end_date", ecEndDate);

                Object[] results = getDao().executeSQLQuery(
                        sumPrecipQuery.toString(), keyParamMap);

                // result could be null
                if ((results != null) && (results.length >= 1)
                        && (results[0] != null)) {
                    float ecSumPrecip = ((Number) results[0]).floatValue();
                    /*
                     * If the value sum is 0, there might be some TRACE values,
                     * in which case, set the sum to TRACE.
                     */
                    if (ClimateUtilities.floatingEquals(ecSumPrecip, 0)) {
                        StringBuilder sumCountQuery;
                        if (endDate.getMon() < beginDate.getMon()) {
                            sumCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            sumCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            sumCountQuery.append(
                                    " WHERE (day_of_year >= :ec_start_date ");
                            sumCountQuery.append(
                                    " OR day_of_year <= :ec_end_date) ");
                            sumCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumCountQuery.append(
                                    " AND precip_mean = :ec_trace_value");

                        } else {
                            sumCountQuery = new StringBuilder(
                                    "SELECT COUNT(*) FROM ");
                            sumCountQuery.append(
                                    ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                            sumCountQuery.append(
                                    " WHERE day_of_year >= :ec_start_date ");
                            sumCountQuery.append(
                                    " AND day_of_year <= :ec_end_date ");
                            sumCountQuery.append(
                                    " AND station_id = :ec_station_id ");
                            sumCountQuery.append(
                                    " AND precip_mean = :ec_trace_value");
                        }
                        keyParamMap.clear();
                        keyParamMap.put("ec_station_id", stationId);
                        keyParamMap.put("ec_trace_value",
                                ParameterFormatClimate.TRACE);
                        keyParamMap.put("ec_start_date", ecStartDate);
                        keyParamMap.put("ec_end_date", ecEndDate);

                        int res = ((Number) queryForOneValue(
                                sumCountQuery.toString(), keyParamMap, -1))
                                        .intValue();

                        if (res != -1) {
                            if (res > 0) {
                                sumPrecip = ParameterFormatClimate.TRACE;
                            } else {
                                sumPrecip = 0.f;
                            }
                        } else {
                            logger.warn("Expected some result for query: ["
                                    + sumCountQuery + "] and map: ["
                                    + keyParamMap + "]");
                        }
                    } else {
                        sumPrecip = ecSumPrecip;
                    }
                } else {
                    /*
                     * If nullSumIndicator = DBNULL, there might be some days
                     * with a T of precip. In this case, the sum should be
                     * changed to T. Otherwise, the values must all be
                     * ClimateFormatParameter.MISSING.
                     */
                    StringBuilder precipCountQuery;
                    if (endDate.getMon() < beginDate.getMon()) {
                        precipCountQuery = new StringBuilder(
                                "SELECT COUNT(*) FROM ");
                        precipCountQuery.append(
                                ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                        precipCountQuery.append(
                                " WHERE (day_of_year >= :ec_start_date ");
                        precipCountQuery
                                .append(" OR day_of_year <= :ec_end_date) ");
                        precipCountQuery
                                .append(" AND station_id = :ec_station_id ");
                        precipCountQuery
                                .append(" AND precip_mean = :ec_trace_value");
                    } else {
                        precipCountQuery = new StringBuilder(
                                "SELECT COUNT(*) FROM ");
                        precipCountQuery.append(
                                ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
                        precipCountQuery.append(
                                " WHERE day_of_year >= :ec_start_date ");
                        precipCountQuery
                                .append(" AND day_of_year <= :ec_end_date ");
                        precipCountQuery
                                .append(" AND station_id = :ec_station_id ");
                        precipCountQuery
                                .append(" AND precip_mean = :ec_trace_value");
                    }

                    keyParamMap.clear();
                    keyParamMap.put("ec_station_id", stationId);
                    keyParamMap.put("ec_trace_value",
                            ParameterFormatClimate.TRACE);
                    keyParamMap.put("ec_start_date", ecStartDate);
                    keyParamMap.put("ec_end_date", ecEndDate);
                    int res = ((Number) queryForOneValue(
                            precipCountQuery.toString(), keyParamMap, -1))
                                    .intValue();

                    if (res != -1) {
                        if (res > 0) {
                            sumPrecip = ParameterFormatClimate.TRACE;
                        } else {
                            sumPrecip = ParameterFormatClimate.MISSING_PRECIP;
                        }
                    } else {
                        logger.warn("Expected some result for query: ["
                                + precipCountQuery + "] and map: ["
                                + keyParamMap + "]");
                    }
                }
            } catch (Exception e) {
                sumPrecip = ParameterFormatClimate.MISSING_PRECIP;
                logger.error("Error querying the climate database: ["
                        + sumPrecipQuery + "] map: [" + keyParamMap + "]", e);
            }

        }

        return sumPrecip;
    }

    /**
     * If firstOne, get the earliest day_of_year from table day_climate_norm
     * where station_id=StationId, else get the latest one.
     * 
     * @param firstOne
     * @param stationId
     * @return day of year
     * @throws ClimateQueryException
     */
    public String getAvailableDayOfYear(boolean firstOne, int stationId)
            throws ClimateQueryException {
        String dayOfYear = null;
        StringBuilder sql = new StringBuilder("SELECT day_of_year FROM ");
        sql.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationId");

        if (firstOne) {
            sql.append(" ORDER BY day_of_year ASC LIMIT 1");
        } else {
            sql.append(" ORDER BY day_of_year DESC LIMIT 1");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                if (results[0] instanceof String) {
                    dayOfYear = (String) results[0];
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected String, got "
                                    + results[0].getClass().getName());
                }
            } else {
                logger.warn("No available day of year for query: [" + sql
                        + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + sql
                    + "] and map: [" + paramMap + "]", e);
        }

        return dayOfYear;
    }

    /**
     * Delete record with station_id=statioId and day_of_year=dayOfYear
     * 
     * @param stationId
     * @param dayOfYear
     * @return
     * @throws ClimateQueryException
     */
    public boolean deleteClimateDayRecord(int stationId, String dayOfYear)
            throws ClimateQueryException {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationId");
        sql.append(" AND day_of_year=:dayOfYear");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("dayOfYear", dayOfYear);

        int numRow = 0;
        try {
            numRow = getDao().executeSQLUpdate(sql.toString(), paramMap);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Failed to delete record station ID=" + stationId
                            + " day_of_year=" + dayOfYear + ". Query: [" + sql
                            + "] and map: [" + paramMap + "]",
                    e);
        }

        return (numRow == 1);
    }

    /**
     * insert a row into day_climate_norm table
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean insertClimateDayRecord(ClimateDayNorm record)
            throws ClimateQueryException {
        boolean isInserted = false;

        StringBuilder sql = new StringBuilder();

        Map<String, Object> paramMap = new HashMap<>();

        sql.append("INSERT INTO " + ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME
                + " VALUES(");
        sql.append(":stationID");
        paramMap.put("stationID", record.getStationId());
        sql.append(", :dayOfYear");
        paramMap.put("dayOfYear", record.getDayOfYear());
        sql.append(", :meanTemp");
        paramMap.put("meanTemp", record.getMeanTemp());
        sql.append(", :maxTempRecord");
        paramMap.put("maxTempRecord", record.getMaxTempRecord());
        sql.append(", :maxTempMean");
        paramMap.put("maxTempMean", record.getMaxTempMean());
        sql.append(", :minTempRecord");
        paramMap.put("minTempRecord", record.getMinTempRecord());
        sql.append(", :minTempMean");
        paramMap.put("minTempMean", record.getMinTempMean());

        sql.append(", :maxTempYear1");
        paramMap.put("maxTempYear1", record.getMaxTempYear()[0]);
        sql.append(", :maxTempYear2");
        paramMap.put("maxTempYear2", record.getMaxTempYear()[1]);
        sql.append(", :maxTempYear3");
        paramMap.put("maxTempYear3", record.getMaxTempYear()[2]);

        sql.append(", :minTempYear1");
        paramMap.put("minTempYear1", record.getMinTempYear()[0]);
        sql.append(", :minTempYear2");
        paramMap.put("minTempYear2", record.getMinTempYear()[1]);
        sql.append(", :minTempYear3");
        paramMap.put("minTempYear3", record.getMinTempYear()[2]);

        sql.append(", :precipMean");
        paramMap.put("precipMean", record.getPrecipMean());
        sql.append(", :precipDayRecord");
        paramMap.put("precipDayRecord", record.getPrecipDayRecord());

        sql.append(", :precipDayRecordYear1");
        paramMap.put("precipDayRecordYear1",
                record.getPrecipDayRecordYear()[0]);
        sql.append(", :precipDayRecordYear2");
        paramMap.put("precipDayRecordYear2",
                record.getPrecipDayRecordYear()[1]);
        sql.append(", :precipDayRecordYear3");
        paramMap.put("precipDayRecordYear3",
                record.getPrecipDayRecordYear()[2]);
        sql.append(", :snowDayMean");
        paramMap.put("snowDayMean", record.getSnowDayMean());
        sql.append(", :snowDayRecord");
        paramMap.put("snowDayRecord", record.getSnowDayRecord());

        sql.append(", :snowDayRecordYear1");
        paramMap.put("snowDayRecordYear1", record.getSnowDayRecordYear()[0]);
        sql.append(", :snowDayRecordYear2");
        paramMap.put("snowDayRecordYear2", record.getSnowDayRecordYear()[1]);
        sql.append(", :snowDayRecordYear3");
        paramMap.put("snowDayRecordYear3", record.getSnowDayRecordYear()[2]);

        sql.append(", :snowGround");
        paramMap.put("snowGround", record.getSnowGround());
        sql.append(", :numHeatMean");
        paramMap.put("numHeatMean", record.getNumHeatMean());
        sql.append(", :numCoolMean");
        paramMap.put("numCoolMean", record.getNumCoolMean());
        sql.append(")");

        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), paramMap);
            isInserted = (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Failed to insert record station ID="
                            + record.getStationId() + " day_of_year="
                            + record.getDayOfYear() + ". Query: [" + sql
                            + "] and map: [" + paramMap + "]",
                    e);
        }

        return isInserted;
    }

    /**
     * update table day_climate_norm with exactly record data given.
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean updateClimateDayRecord(ClimateDayNorm record)
            throws ClimateQueryException {
        boolean isUpdated = false;

        Map<String, Object> paramMap = new HashMap<>();

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        sql.append(" SET ");
        sql.append("mean_temp= :mean_temp");
        paramMap.put("mean_temp", record.getMeanTemp());
        sql.append(", max_temp_record= :max_temp_record");
        paramMap.put("max_temp_record", record.getMaxTempRecord());
        sql.append(", max_temp_mean= :max_temp_mean");
        paramMap.put("max_temp_mean", record.getMaxTempMean());
        sql.append(", min_temp_record= :min_temp_record");
        paramMap.put("min_temp_record", record.getMinTempRecord());
        sql.append(", min_temp_mean= :min_temp_mean");
        paramMap.put("min_temp_mean", record.getMinTempMean());

        sql.append(", max_temp_rec_yr1= :max_temp_rec_yr1");
        paramMap.put("max_temp_rec_yr1", record.getMaxTempYear()[0]);
        sql.append(", max_temp_rec_yr2= :max_temp_rec_yr2");
        paramMap.put("max_temp_rec_yr2", record.getMaxTempYear()[1]);
        sql.append(", max_temp_rec_yr3= :max_temp_rec_yr3");
        paramMap.put("max_temp_rec_yr3", record.getMaxTempYear()[2]);

        sql.append(", min_temp_rec_yr1= :min_temp_rec_yr1");
        paramMap.put("min_temp_rec_yr1", record.getMinTempYear()[0]);
        sql.append(", min_temp_rec_yr2= :min_temp_rec_yr2");
        paramMap.put("min_temp_rec_yr2", record.getMinTempYear()[1]);
        sql.append(", min_temp_rec_yr3= :min_temp_rec_yr3");
        paramMap.put("min_temp_rec_yr3", record.getMinTempYear()[2]);

        sql.append(", precip_mean= :precip_mean");
        paramMap.put("precip_mean", record.getPrecipMean());
        sql.append(", precip_day_max= :precip_day_max");
        paramMap.put("precip_day_max", record.getPrecipDayRecord());

        sql.append(", precip_day_max_yr1= :precip_day_max_yr1");
        paramMap.put("precip_day_max_yr1", record.getPrecipDayRecordYear()[0]);
        sql.append(", precip_day_max_yr2= :precip_day_max_yr2");
        paramMap.put("precip_day_max_yr2", record.getPrecipDayRecordYear()[1]);
        sql.append(", precip_day_max_yr3= :precip_day_max_yr3");
        paramMap.put("precip_day_max_yr3", record.getPrecipDayRecordYear()[2]);
        sql.append(", snow_mean= :snow_mean");
        paramMap.put("snow_mean", record.getSnowDayMean());
        sql.append(", snow_day_max= :snow_day_max");
        paramMap.put("snow_day_max", record.getSnowDayRecord());

        sql.append(", snow_day_max_yr1= :snow_day_max_yr1");
        paramMap.put("snow_day_max_yr1", record.getSnowDayRecordYear()[0]);
        sql.append(", snow_day_max_yr2= :snow_day_max_yr2");
        paramMap.put("snow_day_max_yr2", record.getSnowDayRecordYear()[1]);
        sql.append(", snow_day_max_yr3= :snow_day_max_yr3");
        paramMap.put("snow_day_max_yr3", record.getSnowDayRecordYear()[2]);

        sql.append(", snow_ground_mean= :snow_ground_mean");
        paramMap.put("snow_ground_mean", record.getSnowGround());
        sql.append(", heat_day_mean= :heat_day_mean");
        paramMap.put("heat_day_mean", record.getNumHeatMean());
        sql.append(", cool_day_mean= :cool_day_mean");
        paramMap.put("cool_day_mean", record.getNumCoolMean());
        sql.append(" WHERE station_id= :station_id");
        paramMap.put("station_id", record.getStationId());
        sql.append(" AND day_of_year= :day_of_year");
        paramMap.put("day_of_year", record.getDayOfYear());

        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), paramMap);
            isUpdated = (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Failed to update record station ID="
                            + record.getStationId() + " day_of_year="
                            + record.getDayOfYear() + ". Query: [" + sql
                            + "] and map: [" + paramMap + "]",
                    e);
        }

        return isUpdated;
    }

    /**
     * Insert or update record into table day_climate_norm
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean saveClimateDayRecord(ClimateDayNorm record)
            throws ClimateQueryException {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM ");
        sql.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationID");
        sql.append(" AND day_of_year= :dayOfYear");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationID", record.getStationId());
        paramMap.put("dayOfYear", record.getDayOfYear());

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                return updateClimateDayRecord(record);
            } else {
                return insertClimateDayRecord(record);
            }
        } catch (ClimateQueryException e) {
            throw new ClimateQueryException("Error with inner query.", e);
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + sql
                    + "] and map: [" + paramMap + "]", e);
        }
    }

    /**
     * If record does not exist, insert it, else update columns with non-missing
     * data only.
     * 
     * @param record
     * @return true if update succeeded, false if update failed or no update was
     *         possible due to all missing values.
     * @throws ClimateQueryException
     */
    public boolean updateClimateDayNormNoMissing(ClimateDayNorm record)
            throws ClimateQueryException {
        if (fetchClimateDayRecord((int) record.getStationId(),
                record.getDayOfYear()) == null) {
            return insertClimateDayRecord(record);
        }

        Map<String, Object> paramMap = new HashMap<>();

        StringBuilder setClause = new StringBuilder();

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMeanTemp(), ParameterFormatClimate.MISSING,
                "mean_temp", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempRecord(), ParameterFormatClimate.MISSING,
                "max_temp_record", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempMean(), ParameterFormatClimate.MISSING,
                "max_temp_mean", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempRecord(), ParameterFormatClimate.MISSING,
                "min_temp_record", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempMean(), ParameterFormatClimate.MISSING,
                "min_temp_mean", paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempYear()[0], ParameterFormatClimate.MISSING,
                "max_temp_rec_yr1", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempYear()[1], ParameterFormatClimate.MISSING,
                "max_temp_rec_yr2", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempYear()[2], ParameterFormatClimate.MISSING,
                "max_temp_rec_yr3", paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempYear()[0], ParameterFormatClimate.MISSING,
                "min_temp_rec_yr1", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempYear()[1], ParameterFormatClimate.MISSING,
                "min_temp_rec_yr2", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempYear()[2], ParameterFormatClimate.MISSING,
                "min_temp_rec_yr3", paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipMean(), ParameterFormatClimate.MISSING_PRECIP,
                "precip_mean", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipDayRecord(),
                ParameterFormatClimate.MISSING_PRECIP, "precip_day_max",
                paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipDayRecordYear()[0],
                ParameterFormatClimate.MISSING, "precip_day_max_yr1",
                paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipDayRecordYear()[1],
                ParameterFormatClimate.MISSING, "precip_day_max_yr2",
                paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipDayRecordYear()[2],
                ParameterFormatClimate.MISSING, "precip_day_max_yr3",
                paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowDayMean(), ParameterFormatClimate.MISSING_SNOW,
                "snow_mean", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowDayRecord(), ParameterFormatClimate.MISSING_SNOW,
                "snow_day_max", paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowDayRecordYear()[0],
                ParameterFormatClimate.MISSING, "snow_day_max_yr1", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowDayRecordYear()[1],
                ParameterFormatClimate.MISSING, "snow_day_max_yr2", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowDayRecordYear()[2],
                ParameterFormatClimate.MISSING, "snow_day_max_yr3", paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowGround(), ParameterFormatClimate.MISSING_SNOW,
                "snow_ground_mean", paramMap));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumHeatMean(), ParameterFormatClimate.MISSING,
                "heat_day_mean", paramMap));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumCoolMean(), ParameterFormatClimate.MISSING,
                "cool_day_mean", paramMap));

        if (setClause.length() == 0) {
            return false;
        }

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(ClimateDAOValues.DAY_CLIMATE_NORM_TABLE_NAME);
        sql.append(" SET ").append(setClause);
        sql.append(" WHERE station_id= :stationID");
        paramMap.put("stationID", record.getStationId());
        sql.append(" AND day_of_year= :dayOfYear");
        paramMap.put("dayOfYear", record.getDayOfYear());

        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), paramMap);
            return (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Failed to update record station ID="
                            + record.getStationId() + " day_of_year="
                            + record.getDayOfYear() + ". Query: [" + sql
                            + "] and map: [" + paramMap + "]",
                    e);
        }
    }

    /**
     * Migrated from check_daily_records.ec
     * 
     * <pre>
     *   void check_daily_records (        climate_date        a_date,
     *                  long            station_id,
     *                  int         max_temp,
     *                  int         min_temp,
     *                  float           precip,
     *                  float           snow
     *                   )
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  December 1999
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function compares the daily max temp, min temp, precip, and snow
     *  values against the record values stored in the database and updates if
     *  needed.
     *
     *   VARIABLES
     *   =========
     *
     *   name                   description
     *------------------------------------------------------------------------------
     *    Input
     *
     *   MODIFICATION HISTORY
     *   ====================
     *    May 2000      Doug Murphy Added checks to precip and snow for
     *                  special cases where T is record and
     *                  0 is observed and vice versa
     *    November 2000     Doug Murphy     Added section to update end of records
     *                                      period if necessary
     *     3/30/01          Doug Murphy     We do NOT want to update a record 
     *                                      value if it is missing
     *     4/2/02           Gary Battel     We do not want to update a record year
     *                                      if the precip or snow amount is 0.
     *    03/25/03          Bob Morris      Fix args to rsetnull, use symbolic names
     *                                      for ESQL-C data types, instead of hard-
     *                                      coded ints for SQL data types. OB2
     *    01/13/05          Gary Battel/    Conversion from INFORMIX to POSTGRES
     *                      Manan Dalal 
     *   
     *    06/05/06          Darnell Early   Add code to check for dates of min and
     *                                      max temp records for monthly climate
     *                                      table
     * </pre>
     *
     * @param date
     * @param stationId
     * @param maxTemp
     * @param minTemp
     * @param precip
     * @param snow
     * @return true if daily record(s) was updated; false otherwise.
     * @throws ClimateQueryException
     */
    public boolean compareUpdateDailyRecords(ClimateDate date, int stationId,
            short maxTemp, short minTemp, float precip, float snow)
                    throws ClimateQueryException {

        boolean updated = false;

        ClimatePeriodDAO periodDAO = new ClimatePeriodDAO();

        // Update the end date for the records period if necessary
        int[] climatePeriod = periodDAO.fetchClimatePeriod(stationId);
        if (climatePeriod != null) {
            int recordEnd = climatePeriod[3];
            if (recordEnd != ParameterFormatClimate.MISSING
                    && recordEnd != date.getYear()) {

                recordEnd = date.getYear();

                periodDAO.updateClimatePeriod(stationId, climatePeriod[0],
                        climatePeriod[1], climatePeriod[2], recordEnd);
            }

        }

        ClimateDayNorm dayRecord = fetchClimateDayRecord(stationId,
                date.toMonthDayDateString());

        if (dayRecord != null) {
            /* Maximum Temperature Record Check */
            if (maxTemp != ParameterFormatClimate.MISSING && dayRecord
                    .getMaxTempRecord() != ParameterFormatClimate.MISSING) {
                // set the year variables to be record years that were read from
                // db
                short[] year = Arrays.copyOf(dayRecord.getMaxTempYear(),
                        dayRecord.getMaxTempYear().length);

                if (maxTemp > dayRecord.getMaxTempRecord()) {
                    dayRecord.setMaxTempRecord(maxTemp);
                    dayRecord.setMaxTempYear(
                            new short[] { (short) date.getYear(),
                                    ParameterFormatClimate.MISSING,
                                    ParameterFormatClimate.MISSING });
                } else if (maxTemp == dayRecord.getMaxTempRecord()
                        && date.getYear() != year[0]
                        && date.getYear() != year[1]
                        && date.getYear() != year[2]) {
                    year = yearShuffle(year);
                    dayRecord.setMaxTempYear(new short[] { year[2], year[1],
                            (short) date.getYear() });
                }
            }

            /* Minimum Temperature Record Check */
            if (minTemp != ParameterFormatClimate.MISSING && dayRecord
                    .getMinTempRecord() != ParameterFormatClimate.MISSING) {
                // set the year variables to be record years that were read from
                // db
                short[] year = Arrays.copyOf(dayRecord.getMinTempYear(),
                        dayRecord.getMinTempYear().length);

                if (minTemp > dayRecord.getMinTempRecord()) {
                    dayRecord.setMinTempRecord(minTemp);
                    dayRecord.setMinTempYear(
                            new short[] { (short) date.getYear(),
                                    ParameterFormatClimate.MISSING,
                                    ParameterFormatClimate.MISSING });
                } else if (minTemp == dayRecord.getMinTempRecord()
                        && date.getYear() != year[0]
                        && date.getYear() != year[1]
                        && date.getYear() != year[2]) {
                    year = yearShuffle(year);
                    dayRecord.setMinTempYear(new short[] { year[2], year[1],
                            (short) date.getYear() });
                }
            }

            /* Precipitation Record Check */
            if (precip != ParameterFormatClimate.MISSING_PRECIP
                    && dayRecord
                            .getPrecipDayRecord() != ParameterFormatClimate.MISSING_PRECIP
                    && precip != 0.0) {
                // set the year data
                short[] year = Arrays.copyOf(dayRecord.getPrecipDayRecordYear(),
                        dayRecord.getPrecipDayRecordYear().length);

                if ((precip > dayRecord.getPrecipDayRecord() && dayRecord
                        .getPrecipDayRecord() != ParameterFormatClimate.TRACE)
                        || (precip == ParameterFormatClimate.TRACE
                                && dayRecord.getPrecipDayRecord() == 0.0)) {
                    dayRecord.setPrecipDayRecord(precip);

                    dayRecord.setPrecipDayRecordYear(
                            new short[] { (short) date.getYear(),
                                    ParameterFormatClimate.MISSING,
                                    ParameterFormatClimate.MISSING });

                } else if (ClimateUtilities.floatingEquals(precip,
                        dayRecord.getPrecipDayRecord())
                        && date.getYear() != year[0]
                        && date.getYear() != year[1]
                        && date.getYear() != year[2]) {
                    year = yearShuffle(year);
                    dayRecord.setPrecipDayRecordYear(new short[] {
                            (short) date.getYear(), year[1], year[2] });
                }
            }

            /* Snow Record Check */
            if (snow != ParameterFormatClimate.MISSING_PRECIP
                    && dayRecord
                            .getSnowDayRecord() != ParameterFormatClimate.MISSING_PRECIP
                    && snow != 0.0) {
                // set the year data
                short[] year = Arrays.copyOf(dayRecord.getSnowDayRecordYear(),
                        dayRecord.getSnowDayRecordYear().length);

                if ((snow > dayRecord.getSnowDayRecord() && dayRecord
                        .getSnowDayRecord() != ParameterFormatClimate.TRACE)
                        || (snow == ParameterFormatClimate.TRACE
                                && dayRecord.getSnowDayRecord() == 0.0)) {
                    dayRecord.setSnowDayRecord(snow);

                    dayRecord.setSnowDayRecordYear(
                            new short[] { (short) date.getYear(),
                                    ParameterFormatClimate.MISSING,
                                    ParameterFormatClimate.MISSING });

                } else if (ClimateUtilities.floatingEquals(snow,
                        dayRecord.getSnowDayRecord())
                        && date.getYear() != year[0]
                        && date.getYear() != year[1]
                        && date.getYear() != year[2]) {
                    year = yearShuffle(year);
                    dayRecord.setSnowDayRecordYear(new short[] {
                            (short) date.getYear(), year[1], year[2] });
                }
            }

            updated = updateClimateDayRecord(dayRecord);

        }

        // Check against monthly, seasonal, and annual records
        for (int i = 0; i < 3; i++) {
            PeriodType ecPeriodType;
            int ecMonth = ParameterFormatClimate.MISSING_DATE;

            if (i == 0) {
                ecPeriodType = PeriodType.MONTHLY_RAD;
                ecMonth = (short) date.getMon();
            } else if (i == 1) {
                ecPeriodType = PeriodType.SEASONAL_RAD;
                if (date.getMon() == 12 || date.getMon() <= 2)
                    ecMonth = 2;
                else if (date.getMon() > 2 && date.getMon() < 6)
                    ecMonth = 5;
                else if (date.getMon() > 5 && date.getMon() < 9)
                    ecMonth = 8;
                else if (date.getMon() > 8 && date.getMon() < 12)
                    ecMonth = 11;
            } else {
                ecPeriodType = PeriodType.ANNUAL_RAD;
                ecMonth = 12;
            }

            MonthClimateNormDAO monthDAO = new MonthClimateNormDAO();

            PeriodClimo monthRecord = monthDAO
                    .fetchClimateMonthRecord(stationId, ecMonth, ecPeriodType);

            if (monthRecord != null) {
                if (maxTemp != ParameterFormatClimate.MISSING && monthRecord
                        .getMaxTempRecord() != ParameterFormatClimate.MISSING) {

                    if (maxTemp > monthRecord.getMaxTempRecord()) {
                        monthRecord.setMaxTempRecord(maxTemp);
                        List<ClimateDate> tempList = new ArrayList<ClimateDate>();

                        tempList.add(date);
                        tempList.add(ClimateDate.getMissingClimateDate());
                        tempList.add(ClimateDate.getMissingClimateDate());

                        monthRecord.setDayMaxTempRecordList(tempList);
                    }
                    /*
                     * check for temp that matches maximum temp but was not on
                     * the same day
                     */
                    else if (maxTemp == monthRecord.getMaxTempRecord()
                            && date.equals(monthRecord.getDayMaxTempRecordList()
                                    .get(0))) {
                        List<ClimateDate> tempList = new ArrayList<ClimateDate>();
                        tempList.add(date);
                        tempList.add(
                                monthRecord.getDayMaxTempRecordList().get(0));
                        tempList.add(
                                monthRecord.getDayMaxTempRecordList().get(1));

                        monthRecord.setDayMaxTempRecordList(tempList);
                    }
                }

                if (minTemp != ParameterFormatClimate.MISSING) {
                    if (monthRecord
                            .getMinTempRecord() == ParameterFormatClimate.MISSING)
                    // There is no data available for Min temp. Just update with
                    // the year
                    {
                        monthRecord.setMinTempRecord(minTemp);
                        List<ClimateDate> tempList = new ArrayList<ClimateDate>();
                        tempList.add(date);
                        tempList.add(ClimateDate.getMissingClimateDate());
                        tempList.add(ClimateDate.getMissingClimateDate());

                        monthRecord.setDayMinTempRecordList(tempList);
                    }

                    if (minTemp < monthRecord.getMinTempRecord()) {
                        monthRecord.setMinTempRecord(minTemp);

                        List<ClimateDate> tempList = new ArrayList<ClimateDate>();
                        tempList.add(date);
                        tempList.add(ClimateDate.getMissingClimateDate());
                        tempList.add(ClimateDate.getMissingClimateDate());

                        monthRecord.setDayMinTempRecordList(tempList);
                    }
                    /*
                     * check temp the matches the minimum temp but is not on the
                     * same day
                     */
                    else if (minTemp == monthRecord.getMinTempRecord()
                            && date.equals(monthRecord.getDayMinTempRecordList()
                                    .get(0))) {

                        List<ClimateDate> tempList = new ArrayList<ClimateDate>();
                        tempList.add(date);
                        tempList.add(
                                monthRecord.getDayMinTempRecordList().get(0));
                        tempList.add(
                                monthRecord.getDayMinTempRecordList().get(1));

                        monthRecord.setDayMinTempRecordList(tempList);
                    }
                }

                monthDAO.updateClimateMonthRecord(monthRecord);

            }
        }
        return updated;
    }

    /**
     * Migrated from check_period_records.ec.
     * 
     * <pre>
     * void check_period_records (      int     itype,
     *                  climate_date    end,
     *                  period_data obs
     *                   )
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  February 2000
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function compares the current observed values against the record 
     *      values stored in the database and updates if needed.
     *
     *   VARIABLES
     *   =========
     *
     *   name                   description
     *-------------------------------------------------------------------------------   
     *    Input 
     *
     *   MODIFICATION HISTORY
     *   ====================
     *    May 2000      Doug Murphy Added checks to precip and snow for
     *                  special cases where T is record and
     *                  0 is observed and vice versa
     *     3/30/01          Doug Murphy     We do NOT want to update a record
     *                                      if it is missing
     *     4/2/02           Gary Battel     We do not want to update the year of
     *                                      record if the max precip amount or 
     *                                      snow or precip value is 0
     *     3/25/03          Bob Morris      Fixed arguments in calls to risnull and
     *                                      rsetnull, need to use defined constants
     *                                      for C-data types, not values (hard-coded,
     *                                      no less!) for SQL data types.  OB2
     *     1/18/04          Gary Battel/    Conversion from INFORMIX to POSTGRES
     *                      Manan Dalal
     * </pre>
     * 
     * 
     * @param type
     * @param end
     * @param periodData
     * @return true if period record(s) was updated; false otherwise.
     * @throws ClimateQueryException
     */
    public boolean compareUpdatePeriodRecords(PeriodType type, ClimateDate end,
            PeriodData periodData) throws ClimateQueryException {
        boolean updated = false;

        MonthClimateNormDAO monthDAO = new MonthClimateNormDAO();

        PeriodClimo monthRecord = monthDAO.fetchClimateMonthRecord(
                periodData.getInformId(), end.getMon(), type);

        int[] order = new int[6];
        ClimateDate[] beginDate = new ClimateDate[6];
        ClimateDate[] endDate = new ClimateDate[6];

        if (monthRecord != null) {
            Arrays.fill(order, ParameterFormatClimate.MISSING);
            Arrays.fill(endDate, ClimateDate.getMissingClimateDate());

            if (periodData
                    .getPrecipTotal() != ParameterFormatClimate.MISSING_PRECIP) {
                if (monthRecord
                        .getPrecipPeriodMax() == ParameterFormatClimate.MISSING_PRECIP
                        && monthRecord
                                .getPrecipPeriodMin() == ParameterFormatClimate.MISSING_PRECIP) {
                    // new precip record

                    monthRecord.setPrecipPeriodMax(periodData.getPrecipTotal());
                    monthRecord.setPrecipPeriodMin(periodData.getPrecipTotal());

                    List<ClimateDate> precipMaxList = monthRecord
                            .getPrecipPeriodMaxYearList();
                    List<ClimateDate> precipMinList = monthRecord
                            .getPrecipPeriodMinYearList();

                    precipMaxList.set(0, new ClimateDate(1, 1, end.getYear()));
                    precipMinList.set(0, new ClimateDate(1, 1, end.getYear()));
                    precipMaxList.set(1, ClimateDate.getMissingClimateDate());
                    precipMinList.set(1, ClimateDate.getMissingClimateDate());
                    precipMaxList.set(2, ClimateDate.getMissingClimateDate());
                    precipMinList.set(2, ClimateDate.getMissingClimateDate());

                    monthRecord.setPrecipPeriodMaxYearList(precipMaxList);
                    monthRecord.setPrecipPeriodMinYearList(precipMinList);
                }

                else {
                    // Maximum Precipitation Record Check

                    if (monthRecord
                            .getPrecipPeriodMax() != ParameterFormatClimate.MISSING_PRECIP
                            && periodData.getPrecipTotal() != 0.0) {
                        for (int i = 0; i < 3; i++) {
                            if (i < monthRecord.getPrecipPeriodMaxYearList()
                                    .size()) {
                                endDate[i] = monthRecord
                                        .getPrecipPeriodMaxYearList().get(i);
                            } else {
                                endDate[i] = ClimateDate
                                        .getMissingClimateDate();
                            }
                        }
                        endDate[3] = new ClimateDate(1, 1, end.getYear());

                        if ((periodData.getPrecipTotal() > monthRecord
                                .getPrecipPeriodMax()
                                && monthRecord
                                        .getPrecipPeriodMax() != ParameterFormatClimate.TRACE)
                                || (periodData
                                        .getPrecipTotal() == ParameterFormatClimate.TRACE
                                        && monthRecord
                                                .getPrecipPeriodMax() == 0.0)) {
                            // broken precip max record
                            monthRecord.setPrecipPeriodMax(
                                    periodData.getPrecipTotal());

                            List<ClimateDate> maxPrecipList = monthRecord
                                    .getPrecipPeriodMaxYearList();
                            maxPrecipList.set(0,
                                    new ClimateDate(1, 1, end.getYear()));
                            maxPrecipList.set(1,
                                    ClimateDate.getMissingClimateDate());
                            maxPrecipList.set(2,
                                    ClimateDate.getMissingClimateDate());

                            monthRecord
                                    .setPrecipPeriodMaxYearList(maxPrecipList);
                        } else if (ClimateUtilities.floatingEquals(
                                periodData.getPrecipTotal(),
                                monthRecord.getPrecipPeriodMax())) {
                            // tied precip max record
                            yearArrange(endDate, order);

                            List<ClimateDate> maxPrecipList = monthRecord
                                    .getPrecipPeriodMaxYearList();
                            maxPrecipList.set(2, new ClimateDate(1, 1,
                                    endDate[2].getYear()));
                            maxPrecipList.set(1, new ClimateDate(1, 1,
                                    endDate[1].getYear()));
                            maxPrecipList.set(0, new ClimateDate(1, 1,
                                    endDate[0].getYear()));

                            monthRecord
                                    .setPrecipPeriodMaxYearList(maxPrecipList);
                        }
                    }

                    // Minimum Precipitation Record Check
                    if (monthRecord
                            .getPrecipPeriodMin() != ParameterFormatClimate.MISSING_PRECIP) {
                        for (int i = 0; i < 3; i++) {
                            endDate[i] = monthRecord
                                    .getPrecipPeriodMinYearList().get(i);
                        }
                        endDate[3] = new ClimateDate(1, 1, end.getYear());

                        if ((periodData.getPrecipTotal() < monthRecord
                                .getPrecipPeriodMin()
                                && (periodData
                                        .getPrecipTotal() != ParameterFormatClimate.TRACE
                                        || monthRecord
                                                .getPrecipPeriodMin() != 0.0))
                                || (periodData.getPrecipTotal() == 0.0
                                        && monthRecord
                                                .getPrecipPeriodMin() == ParameterFormatClimate.TRACE)) {
                            // broken precip min record
                            monthRecord.setPrecipPeriodMin(
                                    periodData.getPrecipTotal());

                            List<ClimateDate> minPrecipList = monthRecord
                                    .getPrecipPeriodMinYearList();
                            minPrecipList.set(0,
                                    new ClimateDate(1, 1, end.getYear()));
                            minPrecipList.set(1,
                                    ClimateDate.getMissingClimateDate());
                            minPrecipList.set(2,
                                    ClimateDate.getMissingClimateDate());

                            monthRecord
                                    .setPrecipPeriodMinYearList(minPrecipList);
                        } else if (ClimateUtilities.floatingEquals(
                                periodData.getPrecipTotal(),
                                monthRecord.getPrecipPeriodMin())) {
                            // tied precip min record
                            yearArrange(endDate, order);

                            List<ClimateDate> minPrecipList = monthRecord
                                    .getPrecipPeriodMinYearList();
                            minPrecipList.set(2, new ClimateDate(1, 1,
                                    endDate[2].getYear()));
                            minPrecipList.set(1, new ClimateDate(1, 1,
                                    endDate[1].getYear()));
                            minPrecipList.set(0, new ClimateDate(1, 1,
                                    endDate[0].getYear()));

                            monthRecord
                                    .setPrecipPeriodMinYearList(minPrecipList);
                        }
                    }
                }
            }

            // Snow Total Record Check
            if (periodData.getSnowTotal() != ParameterFormatClimate.MISSING_SNOW
                    && periodData.getSnowTotal() != 0.) {
                // Snow value is missing
                if (monthRecord
                        .getSnowPeriodRecord() == ParameterFormatClimate.MISSING_SNOW) {
                    // Assign the new record and date
                    List<ClimateDate> snowList = monthRecord
                            .getSnowPeriodMaxYearList();

                    snowList.set(0, new ClimateDate(1, 1, end.getYear()));
                    snowList.set(1, ClimateDate.getMissingClimateDate());
                    snowList.set(2, ClimateDate.getMissingClimateDate());

                    monthRecord.setSnowPeriodMaxYearList(snowList);
                } else if (monthRecord
                        .getSnowPeriodRecord() != ParameterFormatClimate.MISSING_SNOW) {
                    for (int i = 0; i < 3; i++) {
                        if (i < monthRecord.getSnowPeriodMaxYearList().size()) {
                            endDate[i] = monthRecord.getSnowPeriodMaxYearList()
                                    .get(i);
                        } else {
                            endDate[i] = ClimateDate.getMissingClimateDate();
                        }
                    }
                    endDate[3] = new ClimateDate(1, 1, end.getYear());

                    if ((periodData.getSnowTotal() > monthRecord
                            .getSnowPeriodRecord()
                            && monthRecord
                                    .getSnowPeriodRecord() != ParameterFormatClimate.TRACE)
                            || (periodData
                                    .getSnowTotal() == ParameterFormatClimate.TRACE
                                    && monthRecord
                                            .getSnowPeriodRecord() == 0.0)) {
                        // broken snow record
                        monthRecord
                                .setSnowPeriodRecord(periodData.getSnowTotal());

                        List<ClimateDate> snowList = monthRecord
                                .getSnowPeriodMaxYearList();

                        snowList.set(0, new ClimateDate(1, 1, end.getYear()));
                        snowList.set(1, ClimateDate.getMissingClimateDate());
                        snowList.set(2, ClimateDate.getMissingClimateDate());

                        monthRecord.setSnowPeriodMaxYearList(snowList);
                    } else if (ClimateUtilities.floatingEquals(
                            periodData.getSnowTotal(),
                            monthRecord.getSnowPeriodRecord())) {
                        // tied snow record
                        yearArrange(endDate, order);

                        List<ClimateDate> snowList = monthRecord
                                .getSnowPeriodMaxYearList();

                        snowList.set(2,
                                new ClimateDate(1, 1, endDate[2].getYear()));
                        snowList.set(1,
                                new ClimateDate(1, 1, endDate[1].getYear()));
                        snowList.set(0,
                                new ClimateDate(1, 1, endDate[0].getYear()));

                        monthRecord.setSnowPeriodMaxYearList(snowList);
                    }
                }
            }

            // Show Depth Record Check
            if (periodData.getSnowGroundMax() != ParameterFormatClimate.MISSING
                    && periodData.getSnowGroundMax() != 0) {
                if (monthRecord
                        .getSnowGroundMax() == ParameterFormatClimate.MISSING) {
                    // No value for snow on the ground. Add it
                    monthRecord.setSnowGroundMax(periodData.getSnowGroundMax());
                    monthRecord.setDaySnowGroundMaxList(
                            periodData.getSnowGroundMaxDateList());

                } else if (monthRecord
                        .getSnowGroundMax() != ParameterFormatClimate.MISSING) {
                    for (int i = 0; i < 3; i++) {

                        if (i < monthRecord.getDaySnowGroundMaxList().size()) {
                            endDate[i] = monthRecord.getDaySnowGroundMaxList()
                                    .get(i);
                        } else {
                            endDate[i] = ClimateDate.getMissingClimateDate();
                        }

                        for (int j = 3; j < 6; j++) {
                            if (j - 3 < periodData.getSnowGroundMaxDateList()
                                    .size()) {
                                endDate[j] = periodData
                                        .getSnowGroundMaxDateList().get(j - 3);
                            } else {
                                endDate[i] = ClimateDate
                                        .getMissingClimateDate();
                            }
                        }

                        if ((periodData.getSnowGroundMax() > monthRecord
                                .getSnowGroundMax()
                                && monthRecord
                                        .getSnowGroundMax() != ParameterFormatClimate.TRACE)
                                || (periodData
                                        .getSnowGroundMax() == ParameterFormatClimate.TRACE
                                        && monthRecord
                                                .getSnowGroundMax() == 0.0)) {
                            // broken snow record
                            monthRecord.setSnowGroundMax(
                                    periodData.getSnowGroundMax());

                            monthRecord.setDaySnowGroundMaxList(
                                    periodData.getSnowGroundMaxDateList());

                        } else if (periodData.getSnowGroundMax() == monthRecord
                                .getSnowGroundMax()) {
                            // tied snow record
                            yearArrange(endDate, order);

                            List<ClimateDate> snowList = new ArrayList<ClimateDate>();
                            snowList.set(0, endDate[0]);
                            snowList.set(1, endDate[1]);
                            snowList.set(2, endDate[2]);

                            monthRecord.setDaySnowGroundMaxList(snowList);

                        }
                    }
                }
            }

            // Set all to missing
            Arrays.fill(endDate, ClimateDate.getMissingClimateDate());
            Arrays.fill(beginDate, ClimateDate.getMissingClimateDate());
            Arrays.fill(order, ParameterFormatClimate.MISSING);

            // 24hr Snow Record Check
            if (periodData
                    .getSnowMax24H() != ParameterFormatClimate.MISSING_SNOW
                    && periodData.getSnowMax24H() != 0.) {
                if (monthRecord
                        .getSnowMax24HRecord() == ParameterFormatClimate.MISSING_SNOW) {
                    monthRecord.setSnowMax24HRecord(periodData.getSnowMax24H());
                    monthRecord.setSnow24HList(periodData.getSnow24HDates());

                } else if (monthRecord
                        .getSnowMax24HRecord() != ParameterFormatClimate.MISSING_SNOW) {
                    // Get the retrieved dates

                    for (int i = 0; i < 3; i++) {
                        if (i < monthRecord.getSnow24HList().size()) {
                            beginDate[i] = monthRecord.getSnow24HList().get(i)
                                    .getStart();
                            endDate[i] = monthRecord.getSnow24HList().get(i)
                                    .getEnd();
                        } else {

                            beginDate[i] = ClimateDate.getMissingClimateDate();
                            endDate[i] = ClimateDate.getMissingClimateDate();
                        }
                        order[i] = i;
                    }

                    for (int i = 3; i < 6; i++) {
                        if (i - 3 < periodData.getSnow24HDates().size()) {
                            beginDate[i] = periodData.getSnow24HDates()
                                    .get(i - 3).getStart();
                            endDate[i] = periodData.getSnow24HDates().get(i - 3)
                                    .getEnd();
                        } else {

                            beginDate[i] = ClimateDate.getMissingClimateDate();
                            endDate[i] = ClimateDate.getMissingClimateDate();
                        }
                        order[i] = i;
                    }

                    if ((periodData.getSnowMax24H() > monthRecord
                            .getSnowMax24HRecord()
                            && monthRecord
                                    .getSnowMax24HRecord() != ParameterFormatClimate.TRACE)
                            || (periodData
                                    .getSnowMax24H() == ParameterFormatClimate.TRACE
                                    && monthRecord
                                            .getSnowMax24HRecord() == 0.0)) {
                        // broken snow record
                        monthRecord.setSnowMax24HRecord(
                                periodData.getSnowMax24H());
                        monthRecord
                                .setSnow24HList(periodData.getSnow24HDates());

                    } else if (ClimateUtilities.floatingEquals(
                            periodData.getSnowMax24H(),
                            monthRecord.getSnowMax24HRecord())) {
                        // tied snow record
                        yearArrange(endDate, order);

                        List<ClimateDates> snow24List = new ArrayList<ClimateDates>();

                        for (int i = 0; i < 3; i++) {
                            snow24List.add(new ClimateDates(beginDate[order[i]],
                                    endDate[i]));
                        }

                        monthRecord.setSnow24HList(snow24List);
                    }
                }
            }

            updated = monthDAO.updateClimateMonthRecord(monthRecord);
        }

        return updated;

    }

    /**
     * Migrated from check_daily_records.ec helper function year_shuffle.
     * 
     * <pre>
     * 
     *  void year_shuffle ( int *year)
    *
    *   Doug Murphy        PRC/TDL             HP 9000/7xx
    *                                  December 1999
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *  This function updates the dates of occurence for a given element's
    *  record.
    *
    *   VARIABLES
    *   =========
    *
    *   name                   description
    *-------------------------------------------------------------------------------                   
    *    Input
     * </pre>
     * 
     * @param year
     */
    private static short[] yearShuffle(short[] year) {
        if (year[2] != ParameterFormatClimate.MISSING) {
            if ((year[1] < year[2] && year[1] < year[0]
                    && year[0] != ParameterFormatClimate.MISSING)
                    || year[1] == ParameterFormatClimate.MISSING)
                year[1] = year[0];
            else if (year[2] < year[1] && year[2] < year[0]
                    && year[0] != ParameterFormatClimate.MISSING
                    && year[1] != ParameterFormatClimate.MISSING) {
                year[2] = year[1];
                year[1] = year[0];
            }
        } else {
            if (year[1] != ParameterFormatClimate.MISSING
                    && year[0] != ParameterFormatClimate.MISSING)
                year[2] = year[1];
            if (year[0] != ParameterFormatClimate.MISSING)
                year[1] = year[0];
        }
        return year;
    }

    /**
     * Migrated from check_period_records.ec helper function year_arrange.
     * 
     * <pre>
     *  void year_arrange (  int *year, int *order)
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  December 1999
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function updates the dates of occurence for a given element's
     *  record.
     *
     *   VARIABLES
     *   =========
     *
     *   name                   description
     *-------------------------------------------------------------------------------                   
     *    Input
     * </pre>
     * 
     * @param recDates
     * @param order
     */
    private static void yearArrange(ClimateDate[] recDates, int[] order) {

        for (int i = 1; i < 6; i++) {
            ClimateDate tempDate = recDates[i];
            int tempOrder = order[i];

            // Check for repeat dates (don't want it to appear twice)
            boolean repeat = false;
            for (int j = i - 1; j >= 0; j--) {
                if (tempDate.equals(recDates[j])) {
                    repeat = true;
                }
            }

            for (int j = i - 1; j >= 0 && !repeat; j--) {
                if (tempDate.getYear() != ParameterFormatClimate.MISSING) {
                    if (tempDate.getYear() > recDates[j].getYear()
                            || recDates[j]
                                    .getYear() == ParameterFormatClimate.MISSING) {
                        recDates[j + 1] = recDates[j];
                        order[j + 1] = order[j];
                        recDates[j] = tempDate;
                        order[j] = tempOrder;
                    } else if (tempDate.getYear() == recDates[j].getYear()
                            && tempDate
                                    .getMon() != ParameterFormatClimate.MISSING_DATE) {
                        if (tempDate.getMon() > recDates[j].getMon()
                                || (tempDate.getMon() == recDates[j].getMon()
                                        && tempDate.getDay() > recDates[j]
                                                .getDay())) {
                            recDates[j + 1] = recDates[j];
                            order[j + 1] = order[j];
                            recDates[j] = tempDate;
                            order[j] = tempOrder;
                        }
                    }
                }
            }
        }
    }

}
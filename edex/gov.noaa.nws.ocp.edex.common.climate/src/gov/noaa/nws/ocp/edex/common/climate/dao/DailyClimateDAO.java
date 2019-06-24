/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.QueryData;
import gov.noaa.nws.ocp.common.dataplugin.climate.SLP;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateSessionException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateDAOUtils;

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
 * 20 JUL 2016  20591      amoore      Expect and handle some null values in 
 *                                     getLastYearForMonth query.
 * 22 SEP 2016  21378      amoore      Migrated Resultant Wind calculation.
 * 27 SEP 2016  20632      wpaintsil   Check for null values in daily_climate table.
 * 04 OCT 2016  20636      wpaintsil   Move getPeriodData to ClimatePeriodDAO.
 * 19 OCT 2016  21378      amoore      Created early freeze and late freeze calculations.
 * 27 OCT 2016  21378      amoore      Reference common functionality for calculations.
 * 27 OCT 2016  22135      wpaintsil   Modify get getLastYear() parameters.
 * 01 NOV 2016  22135      wpaintsil   Add data method columns to updateDaily...().
 * 02 NOV 2016  21378      amoore      Extracted common resultant wind functionality.
 * 22 NOV 2016  23222      amoore      Legacy DR 15685. Some code clean up.
 * 09 DEC 2016  27015      amoore      Query clean up and fix.
 * 12 DEC 2016  27015      amoore      Headless Display workflow. Query clean up.
 * 15 DEC 2016  27015      amoore      Query clean up.
 * 25 JAN 2017  22786      amoore      Cleanup action item related to weather elements.
 * 26 JAN 2017  27017      amoore      Correct column types for Time.
 * 17 FEB 2017  28609      amoore      Correct column types for Time.
 * 22 FEB 2017  28609      amoore      Address TODOs. Fix comments. Cleanup code.
 * 10 MAR 2017  27420      amoore      Minor touch-up of string building. Make some errors just logging.
 * 16 MAR 2017  30162      amoore      Fix logging.
 * 21 MAR 2017  20632      amoore      Handle null DB returns.
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 05 APR 2017  30166      amoore      Minor cleanup.
 * 12 APR 2017  30171      amoore      Remove unnecessary method.
 * 13 APR 2017  30166      amoore      SQL type casting fix.
 * 19 APR 2017  33104      amoore      Use query maps now that DB issue is fixed.
 * 02 MAY 2017  33104      amoore      More query map replacements. Use abstract maps.
 * 11 MAY 2017  33104      amoore      Simply query type for #getLastYear.
 * 11 MAY 2017  33104      amoore      Fix up query params. Add TODOs.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 02 JUN 2017  34789      amoore      Fix summing of degree days for a period to not include missing
 *                                     values in query, and to default to the appropriate missing value.
 * 08 JUN 2017  33104      amoore      Touch up of logic. Fix potential Legacy bug in incrementing
 *                                     weather count.
 * 14 JUN 2017  35175      amoore      Fix Precip calculations.
 * 14 JUN 2017  35177      amoore      Fix trace snow sum calculations. Fix latent query bug.
 * 20 JUN 2017  33104      amoore      Address review comments. Remove unused method.
 * 20 JUN 2017  35179      amoore      Fix issue with improper summing dates for season/year data.
 * 30 JUN 2017  35729      amoore      Move #determineWindow from ClimateCreator to Daily DAO.
 * 08 AUG 2017  33104      amoore      Logic branch fix. Minor cleanup of logging and comments.
 * 31 AUG 2017  37561      amoore      Use calendar/date parameters where possible.
 * 06 SEP 2017  37721      amoore      Failure on Display finalization should fail CPG session
 * 08 SEP 2017  37809      amoore      For queries, cast to Number rather than specific number type.
 * 04 OCT 2017  38067      amoore      Fix PM/IM delay in data reports.
 * 23 OCT 2017  39818      amoore      Fix trace counting for 24-hour precip/snow when original count
 *                                     is 0. If no result found on trace, 0 is ok, not set to missing.
 * 24 OCT 2017  39817      amoore      Clean up 24-hour precip calculations while investigating validity of
 *                                     calculations. Handle trace better in hourly precip count.
 * 02 MAY 2018  DR17116    wpaintsil   update yClimate for snow/precip norms.
 * 01 OCT 2018  DR20918    wpaintsil   Add checks for trace values in sumPrecip() & sumSnow().
 * 18 DEC 2018  DR21053    wpaintsil   Correct query and conditional in the fix for the above DR20918.
 * 14 MAR 2018  DR21137    wpaintsil   The snowfall column was being cast to an integer when it should 
 *                                     be cast to a float (retrieveDailySummary()).
 * 30 APR 2019  DR21261    wpaintsil   num_wind_obs column missing from queries.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class DailyClimateDAO extends ClimateDAO {
    /**
     * Constructor.
     */
    public DailyClimateDAO() {
        super();
    }

    /**
     * Rewritten from build_period_obs_climo.ecpp. Get last date between the
     * given dates for the station in which the temperature was no greater than
     * 32.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return
     * @throws ClimateQueryException
     */
    public ClimateDate getLateFreezeDate(ClimateDate beginDate,
            ClimateDate endDate, int stationID) throws ClimateQueryException {
        StringBuilder query = new StringBuilder("SELECT MAX(date) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE station_id = ").append(":stationID");
        query.append(" AND date >= ").append(" :beginDate");
        query.append(" AND date <= ").append(" :endDate");
        query.append(" AND min_temp <= 32");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("stationID", stationID);
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());

        Object date = queryForOneValue(query.toString(), queryParams, null);
        return new ClimateDate(date);
    }

    /**
     * Rewritten from build_period_obs_climo.ecpp. Get first date between the
     * given dates for the station in which the temperature was no greater than
     * 32.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @return
     * @throws ClimateQueryException
     */
    public ClimateDate getEarlyFreezeDate(ClimateDate beginDate,
            ClimateDate endDate, int stationID) throws ClimateQueryException {
        StringBuilder query = new StringBuilder("SELECT MIN(date) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE station_id = ").append(":stationID");
        query.append(" AND date >= ").append(" :beginDate");
        query.append(" AND date <= ").append(" :endDate");
        query.append(" AND min_temp <= 32");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("stationID", stationID);
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());

        Object date = queryForOneValue(query.toString(), queryParams, null);
        return new ClimateDate(date);
    }

    /**
     * Converted from get_last_year.ecpp. get_last_year also has a
     * daily_data_method parameter not shown in the original comments.
     * 
     * <pre>
     * It get the data by day For F6, use
     * getLastYearForMonth to get all data in the month!
     * 
     * Original comments:
     * 
     *   void get_last_year (  const climate_date       &l_date,
     *                                 long           *sta_id,
     *                    daily_climate_data  *last_year
     *                  )
     *   
     *      Jason Tuell       PRC/TDL             HP 9000/7xx
     *      Dan Zipper                 PRC/TDL
     *   
     *      FUNCTION DESCRIPTION
     *      ====================
     *   
     *    This function updates the daily climate data base.  It enters
     *         the contents of yesterday into the daily observed climate data
     *         base.
     * 
     ****************************************************************************** 
     * </pre>
     * 
     * @param date
     *            date to get data for.
     * @param stationId
     *            station ID (inform ID) to get data for.
     * @param data
     *            daily data to fill out. Assumed to already have missing values
     *            for elements related to this query set.
     * @return QueryData
     * @throws ClimateQueryException
     */

    public QueryData getLastYear(ClimateDate date, int stationId,
            DailyClimateData data) throws ClimateQueryException {

        QueryData queryData = new QueryData();
        Map<String, Object> paramMap = new HashMap<>();
        String query = getLastYearQueryString(date, stationId, paramMap);

        try {
            Object[] results = getDao().executeSQLQuery(query, paramMap);
            if ((results != null) && (results.length >= 1)) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    Object[] oa = (Object[]) result;
                    // all data from the query is allowed to be null
                    int index = 0;

                    // max temp
                    Object maxTempObj = oa[index++];
                    if (maxTempObj != null) {
                        data.setMaxTemp(((Number) maxTempObj).intValue());
                    }
                    // max temp time
                    data.setMaxTempTime(new ClimateTime((String) oa[index++]));

                    // max temp data method
                    Object maxTempMethObj = oa[index++];
                    if (maxTempMethObj != null) {
                        data.getDataMethods().setMaxTempQc(
                                ((Number) maxTempMethObj).intValue());
                    }

                    // min temp
                    Object minTempObj = oa[index++];
                    if (minTempObj != null) {
                        data.setMinTemp(((Number) minTempObj).intValue());
                    }

                    // min temp time
                    data.setMinTempTime(new ClimateTime((String) oa[index++]));

                    // min temp data method
                    Object minTempMethObj = oa[index++];
                    if (minTempMethObj != null) {
                        data.getDataMethods().setMinTempQc(
                                ((Number) minTempMethObj).intValue());
                    }

                    // precip
                    Object precipObj = oa[index++];
                    if (precipObj != null) {
                        data.setPrecip(((Number) precipObj).floatValue());
                    }

                    // precip data method
                    Object precipMethObj = oa[index++];
                    if (precipMethObj != null) {
                        data.getDataMethods().setPrecipQc(
                                ((Number) precipMethObj).intValue());
                    }

                    // snow day
                    Object snowDayObj = oa[index++];
                    if (snowDayObj != null) {
                        data.setSnowDay(((Number) snowDayObj).floatValue());
                    }

                    // snow day data method
                    Object snowMethObj = oa[index++];
                    if (snowMethObj != null) {
                        data.getDataMethods()
                                .setSnowQc(((Number) snowMethObj).intValue());
                    }

                    // snow ground
                    Object snowGroundObj = oa[index++];
                    if (snowGroundObj != null) {
                        data.setSnowGround(
                                ((Number) snowGroundObj).floatValue());
                    }

                    // snow ground data method
                    Object snowGroundDepthMethObj = oa[index++];
                    if (snowGroundDepthMethObj != null) {
                        data.getDataMethods().setDepthQc(
                                ((Number) snowGroundDepthMethObj).intValue());
                    }

                    // heating days
                    Object numHeatObj = oa[index++];
                    if (numHeatObj != null) {
                        data.setNumHeat(((Number) numHeatObj).intValue());
                    }

                    // cooling days
                    Object numCoolObj = oa[index++];
                    if (numCoolObj != null) {
                        data.setNumCool(((Number) numCoolObj).intValue());
                    }

                    // max wind direction and speed
                    Object maxWindDirObj = oa[index++];
                    Object maxWindSpdObj = oa[index++];
                    ClimateWind maxWind = ClimateWind.getMissingClimateWind();
                    if (maxWindDirObj != null) {
                        maxWind.setDir(((Number) maxWindDirObj).intValue());
                    }
                    if (maxWindSpdObj != null) {
                        maxWind.setSpeed(((Number) maxWindSpdObj).floatValue());
                    }
                    data.setMaxWind(maxWind);

                    // max wind times
                    data.setMaxWindTime(new ClimateTime((String) oa[index++]));
                    Object maxWindMethObj = oa[index++];
                    if (maxWindMethObj != null) {
                        data.getDataMethods().setMaxWindQc(
                                ((Number) maxWindMethObj).intValue());
                    }

                    // max gust direction and speed
                    Object maxGustDirObj = oa[index++];
                    Object maxGustSpdObj = oa[index++];
                    ClimateWind maxGust = ClimateWind.getMissingClimateWind();
                    if (maxGustDirObj != null) {
                        maxGust.setDir(((Number) maxGustDirObj).intValue());
                    }
                    if (maxGustSpdObj != null) {
                        maxGust.setSpeed(((Number) maxGustSpdObj).floatValue());
                    }
                    data.setMaxGust(maxGust);

                    // max gust times
                    data.setMaxGustTime(new ClimateTime((String) oa[index++]));
                    Object maxGustMethObj = oa[index++];
                    if (maxGustMethObj != null) {
                        data.getDataMethods().setMaxGustQc(
                                ((Number) maxGustMethObj).intValue());
                    }

                    // resultant wind direction and speed
                    Object resultWindDirObj = oa[index++];
                    Object resultWindSpdObj = oa[index++];
                    ClimateWind resultWind = ClimateWind
                            .getMissingClimateWind();
                    if (resultWindDirObj != null) {
                        resultWind
                                .setDir(((Number) resultWindDirObj).intValue());
                    }
                    if (resultWindSpdObj != null) {
                        resultWind.setSpeed(
                                ((Number) resultWindSpdObj).intValue());
                    }
                    data.setResultWind(resultWind);

                    // result x and y
                    Object resultXObj = oa[index++];
                    if (resultXObj != null) {
                        data.setResultX(((Number) resultXObj).doubleValue());
                    }
                    Object resultYObj = oa[index++];
                    if (resultYObj != null) {
                        data.setResultY(((Number) resultYObj).doubleValue());
                    }

                    // number of wind observations
                    Object numWndObsObj = oa[index++];
                    if (numWndObsObj != null) {
                        data.setNumWndObs(((Number) numWndObsObj).intValue());
                    }

                    // average wind speed
                    Object avgWindSpeedObj = oa[index++];
                    if (avgWindSpeedObj != null) {
                        data.setAvgWindSpeed(
                                ((Number) avgWindSpeedObj).floatValue());
                    }

                    // average wind data method
                    Object avgWindMethObj = oa[index++];
                    if (avgWindMethObj != null) {
                        data.getDataMethods().setAvgWindQc(
                                ((Number) avgWindMethObj).intValue());
                    }

                    // minutes of sun
                    Object minSunObj = oa[index++];
                    if (minSunObj != null) {
                        data.setMinutesSun(((Number) minSunObj).intValue());
                    }

                    // minutes of sun data method
                    Object minSunMethObj = oa[index++];
                    if (minSunMethObj != null) {
                        data.getDataMethods().setMinSunQc(
                                ((Number) minSunMethObj).intValue());
                    }

                    // percent possible sun
                    Object possSunObj = oa[index++];
                    if (possSunObj != null) {
                        data.setMinutesSun(((Number) possSunObj).intValue());
                    }

                    // percent possible sun data method
                    Object possSunMethObj = oa[index++];
                    if (possSunMethObj != null) {
                        data.getDataMethods().setPossSunQc(
                                ((Number) possSunMethObj).intValue());
                    }

                    // average sky cover
                    Object skyCoverObj = oa[index++];
                    if (skyCoverObj != null) {
                        data.setSkyCover(((Number) skyCoverObj).floatValue());
                    }

                    // average sky cover data method
                    Object skyCoverMethObj = oa[index++];
                    if (skyCoverMethObj != null) {
                        data.getDataMethods().setSkyCoverQc(
                                ((Number) skyCoverMethObj).intValue());
                    }

                    // mix relative humidity
                    Object maxRhObj = oa[index++];
                    if (maxRhObj != null) {
                        data.setMaxRelHumid(((Number) maxRhObj).intValue());
                    }

                    // time of max relative humidity
                    Object maxRhTimeObj = oa[index++];
                    if (maxRhTimeObj != null) {
                        data.setMaxRelHumidHour(
                                ((Number) maxRhTimeObj).intValue());
                    }

                    // min relative humidity
                    Object minRhObj = oa[index++];
                    if (minRhObj != null) {
                        data.setMinRelHumid(((Number) minRhObj).intValue());
                    }

                    // time of min relative humidity
                    Object minRhTimeObj = oa[index++];
                    if (minRhTimeObj != null) {
                        data.setMinRelHumidHour(
                                ((Number) minRhTimeObj).intValue());
                    }

                    // max pressure
                    Object maxSlpObj = oa[index++];
                    if (maxSlpObj != null) {
                        data.setMaxSlp(((Number) maxSlpObj).floatValue());
                    }

                    // min pressure
                    Object minSlpObj = oa[index++];
                    if (minSlpObj != null) {
                        data.setMinSlp(((Number) minSlpObj).floatValue());
                    }

                    // weather data method
                    Object weatherMethObj = oa[index++];
                    if (weatherMethObj != null) {
                        data.getDataMethods().setWeatherQc(
                                ((Number) weatherMethObj).shortValue());
                    }

                    // number of wx observations
                    Object numberObsWxObj = oa[index++];
                    if (numberObsWxObj != null) {
                        data.setNumWx(((Number) numberObsWxObj).intValue());
                    }

                    for (int i = 0; i < DailyClimateData.TOTAL_WX_TYPES; i++) {
                        Object wxObj = oa[index++];
                        if (wxObj != null) {
                            data.setWxType(i, ((Number) wxObj).intValue());
                        }
                    }

                    data.setDataMethods(data.getDataMethods());

                    // The query was successful - set the exists flag to
                    // true.
                    queryData.setData(data);
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }
            } else {
                logger.warn("No data available for station ID [" + stationId
                        + "] on date [" + date.toFullDateString()
                        + "] using query [" + query + "] and map: [" + paramMap
                        + "]");
            }
        } catch (ClimateQueryException e) {
            throw new ClimateQueryException("Error with query: [" + query
                    + "] and map: [" + paramMap + "]", e);
        }

        return queryData;
    }

    /**
     * Overloaded {@link #getLastYear(ClimateDate, int, DailyClimateData)}, with
     * no original data to overwrite.
     * 
     * @param date
     * @param stationId
     * @return QueryData
     * @throws ClimateQueryException
     */
    public QueryData getLastYear(ClimateDate date, int stationId)
            throws ClimateQueryException {

        DailyClimateData data = DailyClimateData.getMissingDailyClimateData();

        data.setInformId(stationId);
        return getLastYear(date, stationId, data);
    }

    /**
     * Daily data query string
     * 
     * @param date
     * @param stationId
     * @param paramMap
     *            sql param map to fill out
     * @return query string
     */
    private static String getLastYearQueryString(ClimateDate date,
            int stationId, Map<String, Object> paramMap) {
        StringBuilder query = new StringBuilder(
                "SELECT max_temp, to_char(max_temp_time, 'HH24:MI') as max_temp_time, ");
        query.append(" max_temp_meth, min_temp, ");
        query.append(
                " to_char(min_temp_time, 'HH24:MI') as min_temp_time, min_temp_meth, precip, precip_meth, ");
        query.append(" snow, snow_meth, ");
        query.append(
                " snow_ground, ground_meth, heat, cool, max_wind_dir, max_wind_spd, ");
        query.append(
                " to_char(max_wind_time, 'HH24:MI') as max_wind_time, max_wind_meth, max_gust_dir, max_gust_spd, ");
        query.append(" to_char(max_gust_time, 'HH24:MI') as max_gust_time, ");
        query.append(
                " max_gust_meth, result_wind_dir, result_wind_spd, result_x, result_y, ");
        query.append(
                " num_wind_obs, avg_wind_speed, avg_wind_meth, min_sun, min_sun_meth, ");
        query.append(
                " percent_pos_sun, poss_sun_meth, avg_sky_cover, sky_cover_meth, max_rh, ");
        query.append(
                " max_rh_hour, min_rh, min_rh_hour, max_slp, min_slp, wx_meth, number_obs_wx,");
        query.append(
                " wx_1, wx_2, wx_3, wx_4, wx_5, wx_6, wx_7, wx_8, wx_9, wx_10, wx_11, wx_12,");
        query.append(" wx_13, wx_14, wx_15, wx_16, wx_17, wx_18 FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE station_id = :stationId");
        query.append(" AND date = :date");

        paramMap.put("stationId", stationId);
        paramMap.put("date", date.getCalendarFromClimateDate());

        return query.toString();
    }

    /**
     * Converted from sum_heat_degree_days.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     * 
     *   void sum_heat_degree_days (   const climate_date   &begin_date
     *                    const climate_date    &end_date,
     *                      long            *station_id,
     *                                       int        *sum_heat
     *                     )
     * 
     *    Jason Tuell       PRC/TDL             HP 9000/7xx
     *    Dan Zipper                 PRC/TDL                  
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *  This function sums the daily heating degree days in the daily
     *       climate data base between the period begin_date and end_date.
     * 
     * 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */

    public int sumHeatDegreeDays(ClimateDate beginDate, ClimateDate endDate,
            long stationId) {
        int sumHeat = ParameterFormatClimate.MISSING_DEGREE_DAY;
        StringBuilder query = new StringBuilder(
                "SELECT cast(SUM(heat) as int) as sum_heat FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :startDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id =  :stationId");
        query.append(" AND heat != ")
                .append(ParameterFormatClimate.MISSING_DEGREE_DAY);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("stationId", stationId);

        sumHeat = ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING_DEGREE_DAY)).intValue();

        return sumHeat;
    }

    /**
     * Converted from sum_cool_degree_days.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     *   void sum_cool_degree_days (   const climate_date   &begin_date
     *                    const climate_date    &end_date,
     *                      long        *station_id,
     *                                       int        *sum_cool
     *                     )
     * 
     *    Jason Tuell       PRC/TDL             HP 9000/7xx
     *    Dan Zipper                 PRC/TDL                  
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *  This function sums the daily cooling degree days in the daily
     *       climate data base between the period begin_date and end_date.
     * 
     * 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */

    public int sumCoolDegreeDays(ClimateDate beginDate, ClimateDate endDate,
            long stationId) {
        int sumCool = ParameterFormatClimate.MISSING_DEGREE_DAY;
        StringBuilder query = new StringBuilder(
                "SELECT cast(SUM(cool) as int) as sum_cool FROM  ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :startDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id =  :stationId");
        query.append(" AND cool != ")
                .append(ParameterFormatClimate.MISSING_DEGREE_DAY);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("stationId", stationId);

        sumCool = ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING_DEGREE_DAY)).intValue();

        return sumCool;
    }

    /**
     * 
     * converted from sum_precip.eccp
     * 
     * <pre>
     *  *******************************************************************************
     * 
     *   void sum_precip (   const climate_date &begin_date
     *              const climate_date  &end_date,
     *                long      *station_id,
     *                             int      *sum_precip  )
     * 
     *    Jason Tuell       PRC/TDL             HP 9000/7xx
     *    Dan Zipper                 PRC/TDL                  
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *  This function sums the daily heating degree days in the daily
     *       climate data base between the period begin_date and end_date.
     * 
     ******************************************************************************** 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */

    public float sumPrecip(ClimateDate beginDate, ClimateDate endDate,
            int stationId) {
        float sumPrecip = ParameterFormatClimate.MISSING_PRECIP;

        StringBuilder query = new StringBuilder(
                "SELECT SUM(precip) as sum_precip FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :startDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id =  :stationId");
        query.append(" AND precip != ")
                .append(ParameterFormatClimate.MISSING_PRECIP);
        query.append(" AND precip != ").append(ParameterFormatClimate.TRACE);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("stationId", stationId);

        sumPrecip = ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING_PRECIP)).floatValue();

        if (sumPrecip <= 0
                || sumPrecip == ParameterFormatClimate.MISSING_PRECIP) {
            // look for trace values
            query = new StringBuilder("SELECT precip FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE date >= :startDate");
            query.append(" AND date <= :endDate");
            query.append(" AND station_id = :stationId");
            query.append(" AND precip = ").append(ParameterFormatClimate.TRACE);
            query.append(" LIMIT 1 ");

            sumPrecip = ((Number) queryForOneValue(query.toString(), paramMap,
                    ParameterFormatClimate.MISSING_PRECIP)).floatValue();
        }

        return sumPrecip;
    }

    /**
     * Converted from sum_snow.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     * void sum_precip (   const climate_date &begin_date
     *             const climate_date  &end_date,
     *               long      *station_id,
     *                            int      *sum_snow    )
     * 
     *   Jason Tuell       PRC/TDL             HP 9000/7xx
     *   Dan Zipper                 PRC/TDL                  
     * 
     *   FUNCTION DESCRIPTION
     *   ====================
     * 
     * This function sums the daily snowfall in the daily
     *      climate data base between the period begin_date and end_date.
     * 
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationId
     * @return
     */

    public float sumSnow(ClimateDate beginDate, ClimateDate endDate,
            int stationId) {
        float sumSnow = ParameterFormatClimate.MISSING_PRECIP;
        StringBuilder query = new StringBuilder(
                "SELECT SUM(snow) as sum_snow FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >=  :startDate");
        query.append(" AND date <=  :endDate");
        query.append(" AND station_id = :stationId");
        query.append(" AND snow != ")
                .append(ParameterFormatClimate.MISSING_PRECIP);
        query.append(" AND snow != ").append(ParameterFormatClimate.TRACE);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("startDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());
        paramMap.put("stationId", stationId);

        sumSnow = ((Number) queryForOneValue(query.toString(), paramMap,
                ParameterFormatClimate.MISSING_PRECIP)).floatValue();

        if (sumSnow <= 0 || sumSnow == ParameterFormatClimate.MISSING_PRECIP) {
            // look for trace values
            query = new StringBuilder("SELECT snow FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE date >= :startDate");
            query.append(" AND date <= :endDate");
            query.append(" AND station_id = :stationId");
            query.append(" AND snow = ").append(ParameterFormatClimate.TRACE);
            query.append(" LIMIT 1 ");

            sumSnow = ((Number) queryForOneValue(query.toString(), paramMap,
                    ParameterFormatClimate.MISSING_PRECIP)).floatValue();
        }

        return sumSnow;
    }

    /**
     * rewritten from build_element method
     * 
     * get maximum 2-minute wind speed for month
     * 
     * @param aDate
     * @param stationId
     * @return
     * @throws ClimateQueryException
     */
    public List<ClimateWind> getMaxMaxWind(ClimateDate aDate, int stationId)
            throws ClimateQueryException {
        StringBuilder query = new StringBuilder(
                "SELECT max_wind_dir, max_wind_spd FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                .append(" WHERE ");
        query.append(" date >= :startDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id = :stationId");
        query.append(" AND max_wind_spd = (SELECT MAX( max_wind_spd ) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                .append(" WHERE ");
        query.append(" max_wind_spd != ")
                .append(ParameterFormatClimate.MISSING);
        query.append(" AND date >=  :startDate");
        query.append(" AND date <=  :endDate");
        query.append(" AND station_id = :stationId )");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("startDate",
                new ClimateDate(1, aDate.getMon(), aDate.getYear())
                        .getCalendarFromClimateDate());
        paramMap.put("endDate", aDate.getCalendarFromClimateDate());

        List<ClimateWind> list = new ArrayList<>();

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        try {
                            Object[] oa = (Object[]) result;
                            ClimateWind data = new ClimateWind();
                            // values could be null
                            data.setDir(oa[0] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[0]).shortValue());
                            data.setSpeed(oa[1] == null
                                    ? ParameterFormatClimate.MISSING_SPEED
                                    : ((Number) oa[1]).floatValue());
                            list.add(data);
                        } catch (Exception e) {
                            // if casting failed
                            throw new Exception(
                                    "Unexpected return column type from query.",
                                    e);
                        }
                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn(
                        "Could not get maximum max wind speed data using query: ["
                                + query + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + query
                    + "] and map: [" + paramMap + "]", e);
        }

        return list;
    }

    /**
     * rewritten from build_element method
     * 
     * get maximum wind gust or peak wind
     * 
     * @param aDate
     * @param stationId
     * @return
     * @throws ClimateQueryException
     */
    public List<ClimateWind> getMaxGustWind(ClimateDate aDate, int stationId)
            throws ClimateQueryException {
        StringBuilder query = new StringBuilder(
                "SELECT max_gust_dir, max_gust_spd FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                .append(" WHERE ");
        query.append(" date >= :startDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id = :stationId");
        query.append(" AND max_gust_spd = (SELECT MAX( max_gust_spd ) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                .append(" WHERE ");
        query.append(" max_gust_spd != ")
                .append(ParameterFormatClimate.MISSING);
        query.append(" AND date >= :startDate");
        query.append(" AND date <= :endDate");
        query.append(" AND station_id = :stationId )");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("startDate",
                new ClimateDate(1, aDate.getMon(), aDate.getYear())
                        .getCalendarFromClimateDate());
        paramMap.put("endDate", aDate.getCalendarFromClimateDate());

        List<ClimateWind> list = new ArrayList<>();

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        try {
                            Object[] oa = (Object[]) result;
                            ClimateWind data = new ClimateWind();
                            // values could be null
                            data.setDir(oa[0] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[0]).shortValue());
                            data.setSpeed(oa[1] == null
                                    ? ParameterFormatClimate.MISSING_SPEED
                                    : ((Number) oa[1]).floatValue());
                            list.add(data);
                        } catch (Exception e) {
                            // if casting failed
                            throw new Exception(
                                    "Unexpected return column type from query.",
                                    e);
                        }

                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get max gust data using query: [" + query
                        + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + query
                    + "] and map: [" + paramMap + "]", e);
        }

        return list;
    }

    /**
     * Converted from build_month_obs_climo.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     * March 2000     David T. Miller        PRC/TDL
     * 
     * 
     * Purpose:  Retrieves stored climatology values from the 
     *           daily table and builds climatology for the month.
     *      
     * Note:     This is similar to build_period_obs_climo and that 
     *           could have been used here but just needed a subset of 
     *      the monthly variables.  So, decided to pull those
     *      and use here.
     * 
     * </pre>
     * 
     * Expected to be called by F6 builder only, and so the beginDate should be
     * the first day of a month.
     * 
     * @param beginDate
     * @param endDate
     * @param informid
     * @return
     * @throws ClimateQueryException
     */

    public PeriodData buildMonthObsClimo(ClimateDate beginDate,
            ClimateDate endDate, int informid) throws ClimateQueryException {
        PeriodData oPeriodData = PeriodData.getMissingPeriodData();

        Calendar ecStartDate = beginDate.getCalendarFromClimateDate();
        Calendar ecEndDate = endDate.getCalendarFromClimateDate();

        /***************
         * maximum temperature section
         ******************************/
        StringBuilder maxTempQuery = new StringBuilder(
                "SELECT date, max_temp FROM ");
        maxTempQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxTempQuery.append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxTempQuery.append(" AND station_id = :stationId ");
        maxTempQuery.append(" AND max_temp!= :missingValue and max_temp = ( ");
        maxTempQuery.append(" SELECT MAX(max_temp) FROM ");
        maxTempQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxTempQuery.append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxTempQuery.append(" AND station_id = :stationId ");
        maxTempQuery.append(" AND  max_temp!= :missingValue )");

        Map<String, Object> keyParamMap = new HashMap<>();
        keyParamMap.put("stationId", informid);
        keyParamMap.put("beginDate", ecStartDate);
        keyParamMap.put("endDate", ecEndDate);
        keyParamMap.put("missingValue", (int) ParameterFormatClimate.MISSING);

        try {
            Object[] results = getDao().executeSQLQuery(maxTempQuery.toString(),
                    keyParamMap);
            if ((results != null) && (results.length >= 1)) {

                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        oPeriodData.getDayMaxTempList().add(0,
                                new ClimateDate((Date) oa[0]));
                        // max temp could be null
                        oPeriodData.setMaxTemp(
                                oa[1] == null ? ParameterFormatClimate.MISSING
                                        : ((Number) oa[1]).intValue());
                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get max temp data using query: ["
                        + maxTempQuery + "] and map: [" + keyParamMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database for max temp", e);
        }

        /* average max temp */
        oPeriodData.setMaxTempMean(ParameterFormatClimate.MISSING);
        float avgmaxT = ParameterFormatClimate.MISSING;
        StringBuilder avgMaxTempQuery = new StringBuilder(
                "SELECT cast(AVG(max_temp) as real) ");
        avgMaxTempQuery.append(" FROM ")
                .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        avgMaxTempQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        avgMaxTempQuery.append(" AND station_id = :stationId ");
        avgMaxTempQuery.append(" AND max_temp!= :missingValue ");
        avgMaxTempQuery.append(" AND min_temp != :missingValue");

        avgmaxT = ((Number) queryForOneValue(avgMaxTempQuery.toString(),
                keyParamMap, ParameterFormatClimate.MISSING)).floatValue();
        oPeriodData.setMaxTempMean(avgmaxT);

        /* number of days for max temp thresholds */
        StringBuilder maxTempBaseString = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        maxTempBaseString.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxTempBaseString
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxTempBaseString.append(" AND station_id = :stationId ");
        maxTempBaseString.append(" AND max_temp!= :missingValue");

        String avgMaxTempOver90Query = maxTempBaseString
                + " AND max_temp >= 90";
        oPeriodData.setNumMaxGreaterThan90F(
                ((Number) queryForOneValue(avgMaxTempOver90Query, keyParamMap,
                        ParameterFormatClimate.MISSING)).intValue());

        String avgMaxTempOverUnder32Query = maxTempBaseString
                + " AND max_temp <= 32";
        oPeriodData.setNumMaxLessThan32F(
                ((Number) queryForOneValue(avgMaxTempOverUnder32Query,
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .intValue());

        /********************
         * minimum temperature section
         **************************/
        StringBuilder minTempQuery = new StringBuilder(
                "SELECT date, min_temp FROM ");
        minTempQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        minTempQuery.append(" WHERE date >= :beginDate AND date <= :endDate ");
        minTempQuery.append(" AND station_id = :stationId ");
        minTempQuery.append(" AND min_temp!= :missingValue and min_temp = ( ");
        minTempQuery.append(" SELECT MIN(min_temp) FROM ");
        minTempQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        minTempQuery.append(" WHERE date >= :beginDate AND date <= :endDate ");
        minTempQuery.append(" AND station_id = :stationId ");
        minTempQuery.append(" AND min_temp!= :missingValue )");

        oPeriodData.setMinTemp(ParameterFormatClimate.MISSING);
        try {
            Object[] results = getDao().executeSQLQuery(minTempQuery.toString(),
                    keyParamMap);
            if ((results != null) && (results.length >= 1)) {

                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        oPeriodData.getDayMinTempList().add(0,
                                new ClimateDate((Date) oa[0]));
                        // min temp could be null
                        oPeriodData.setMinTemp(
                                oa[1] == null ? ParameterFormatClimate.MISSING
                                        : ((Number) oa[1]).intValue());
                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get min temp data using query: ["
                        + minTempQuery + "] and map: [" + keyParamMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database for min temp", e);
        }

        /* average min temp */
        StringBuilder avgMinTempQuery = new StringBuilder(
                "SELECT cast(AVG(min_temp) as real) ");
        avgMinTempQuery.append(" FROM ")
                .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        avgMinTempQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        avgMinTempQuery.append(" AND station_id = :stationId ");
        avgMinTempQuery.append(" AND max_temp!= :missingValue");
        avgMinTempQuery.append(" AND min_temp != :missingValue");

        float avgminT = ((Number) queryForOneValue(avgMinTempQuery.toString(),
                keyParamMap, ParameterFormatClimate.MISSING)).floatValue();
        oPeriodData.setMinTempMean(avgminT);

        /* number of days for min temp thresholds */
        StringBuilder minTempBaseString = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        minTempBaseString.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        minTempBaseString
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        minTempBaseString.append(" AND station_id = :stationId ");
        minTempBaseString.append(" AND min_temp!= :missingValue");

        String avgMinTempUnder32Query = minTempBaseString
                + " AND min_temp <= 32";
        oPeriodData.setNumMinLessThan32F(
                ((Number) queryForOneValue(avgMinTempUnder32Query, keyParamMap,
                        ParameterFormatClimate.MISSING)).intValue());

        String avgMinTempUnder0Query = minTempBaseString + " AND min_temp <= 0";
        oPeriodData.setNumMinLessThan0F(
                ((Number) queryForOneValue(avgMinTempUnder0Query, keyParamMap,
                        ParameterFormatClimate.MISSING)).intValue());

        if ((avgmaxT != ParameterFormatClimate.MISSING)
                && (avgminT != ParameterFormatClimate.MISSING)) {
            oPeriodData.setMeanTemp((int) ((avgmaxT + avgminT) / 2.0f));
        }

        /*********************
         * cumulative precipitation
         *********************************/
        oPeriodData.setPrecipTotal(buildElement(beginDate, endDate, informid,
                PeriodType.OTHER, "precip", "precip", BuildElementType.SUM,
                ParameterFormatClimate.MISSING_PRECIP, true).floatValue());

        /*******************
         * average precipitation for month
         *******************************/
        int precipDays = daysPastThresh(beginDate, endDate, informid,
                PeriodType.OTHER, "precip", "precip",
                ParameterFormatClimate.MISSING_PRECIP_VALUE,
                ParameterFormatClimate.TRACE, true, true);

        if ((oPeriodData.getPrecipTotal() != ParameterFormatClimate.MISSING)
                && (precipDays != ParameterFormatClimate.MISSING)) {
            oPeriodData.setPrecipMeanDay(
                    oPeriodData.getPrecipTotal() / precipDays);
        }
        if (oPeriodData.getPrecipMeanDay() < 0) {
            oPeriodData.setPrecipMeanDay(ParameterFormatClimate.TRACE);
        }

        /********************************
         * threshold precipitation
         ******************/
        oPeriodData.setNumPrcpGreaterThan01(daysPastThresh(beginDate, endDate,
                informid, PeriodType.OTHER, "precip", "precip",
                ParameterFormatClimate.MISSING_PRECIP_VALUE, 0.01, true, true));

        oPeriodData.setNumPrcpGreaterThan10(daysPastThresh(beginDate, endDate,
                informid, PeriodType.OTHER, "precip", "precip",
                ParameterFormatClimate.MISSING_PRECIP_VALUE, 0.1, true, true));

        oPeriodData.setNumPrcpGreaterThan50(daysPastThresh(beginDate, endDate,
                informid, PeriodType.OTHER, "precip", "precip",
                ParameterFormatClimate.MISSING_PRECIP_VALUE, 0.5, true, true));

        oPeriodData.setNumPrcpGreaterThan100(daysPastThresh(beginDate, endDate,
                informid, PeriodType.OTHER, "precip", "precip",
                ParameterFormatClimate.MISSING_PRECIP_VALUE, 1, true, true));

        /*
         * 10/23/00 - Doug Murphy:
         * 
         * Find max 24-hr precip from stored daily data first In cases where DSM
         * might be missing, this will end up being the max 24-hr value for the
         * F-6 report
         */
        StringBuilder precip24HQuery = new StringBuilder(
                "SELECT date, precip FROM ");
        precip24HQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        precip24HQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        precip24HQuery.append(" AND station_id = :stationId ");
        precip24HQuery.append(" AND precip != :missingValue AND precip = (");
        precip24HQuery.append(" SELECT MAX(precip) FROM ");
        precip24HQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        precip24HQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        precip24HQuery.append(" AND station_id = :stationId ");
        precip24HQuery.append(" AND precip != :missingValue )");

        oPeriodData.setPrecipMax24H(ParameterFormatClimate.MISSING);
        try {
            Object[] results = getDao()
                    .executeSQLQuery(precip24HQuery.toString(), keyParamMap);
            if ((results != null) && (results.length >= 1)) {

                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        /* dates with max 24H precip */
                        oPeriodData.getPrecip24HDates()
                                .add(new ClimateDates(
                                        new ClimateDate((Date) oa[0]),
                                        new ClimateDate((Date) oa[0])));
                        // precip could be null
                        oPeriodData.setPrecipMax24H(oa[1] == null
                                ? ParameterFormatClimate.MISSING_PRECIP
                                : ((Number) oa[1]).floatValue());
                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get 24 hour precip data using query: ["
                        + precip24HQuery + "] and map: [" + keyParamMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database for 24 hour precip",
                    e);
        }

        if (oPeriodData.getPrecipMax24H() == 0.0f) {
            /* check for trace amounts */
            StringBuilder precipTraceQuery = new StringBuilder(
                    "SELECT date FROM ");
            precipTraceQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            precipTraceQuery
                    .append(" WHERE date >= :beginDate  AND date <= :endDate ");
            precipTraceQuery.append(" AND station_id = :stationId ");
            precipTraceQuery.append(" AND precip = -1.0");
            // don't need missing value
            keyParamMap.remove("missingValue");

            try {
                Object[] results = getDao().executeSQLQuery(
                        precipTraceQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    // some trace result exists
                    oPeriodData.setPrecipMax24H(ParameterFormatClimate.TRACE);
                    // clear existing dates
                    oPeriodData.getPrecip24HDates().clear();

                    for (Object result : results) {
                        if (result instanceof Date) {
                            /* dates with max 24H precip */
                            oPeriodData.getPrecip24HDates()
                                    .add(new ClimateDates(
                                            new ClimateDate((Date) result),
                                            new ClimateDate((Date) result)));
                        } else {
                            throw new Exception(
                                    "Unexpected return type from query, expected java.sql.Date, got "
                                            + result.getClass().getName());
                        }
                    }
                } else {
                    // max precip is 0, no trace could be found
                    logger.info(
                            "Could not get trace 24 hour precip data using query: ["
                                    + precipTraceQuery + "] and map: ["
                                    + keyParamMap
                                    + "]. 24-hour precip max is 0.");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error querying the climate database for 24 hour precip",
                        e);
            }

            // replace the missing value
            keyParamMap.put("missingValue",
                    (int) ParameterFormatClimate.MISSING);
        }

        /* max 24 hour precip using DSM from cliAsosDaily table */

        /* must add a day to the interval since 24 hr can span */
        /*
         * since DSM only good for a year, have a problem with the year value of
         * the 24h max precip. Therefore, split this up into month vs season or
         * year. Do a separate SQL on the month 24 hr max to get the season and
         * year values
         */
        /***********************************************************************
         * Logic of the next section:
         * 
         * The maximum 24 hour precipitation can be ANY 24 hour precipitation
         * total during the period. And it could be continued from the previous
         * period because it's reported in the current period. For example, if
         * we found the maximum 24 hour precipitation ran from noon Oct 31 to
         * noon Nov 1, and none of the other Novemenber 24 hour precipitation
         * amounts were greater, then the maximum 24 hour precipitation would be
         * reported as Oct 31 to Nov 1 for the month of November.
         * 
         * Therefore, so date spanning around 24 hour time periods can occur,
         * the routine must grab 48 hours of precipitation data and loop through
         * the information 24 hours at a time.
         * 
         * A few notes though:
         * 
         * 1. If the max 24 hour precip occurs during the first 24 hour period,
         * it is outside the period and must be ignored.
         * 
         * 2. Must account for at least 3 days where the maximum was the same
         * 
         * 3. Must account when the second 24 hour period read in contains the
         * max 24 hr precip. This could cause a duplicate date of occurrence
         * situation
         * 
         * 4. Have to find the hours of occurrence as well
         ************************************************************************/

        /*
         * 11/22/99 Here's the idea for at least the month: Since the 24 hour
         * precip can span dates, need to check two days' worth of precip
         * information in 24 hour blocks. There are 25 blocks over two days. Sum
         * the 24 hours and compare to the existing max. We need to keep track
         * of the dates as well. So if we find a max 24h precip, make that the
         * one and save the day. If it's the first block of 24 hours for the day
         * we've grabbed, then no date span (dates are equal). If it's not the
         * last block, dates span. Last block, second date only and no date span
         * again.
         * 
         * If the precip for the block is equal we need to save the date.
         */
        /*
         * This part has been totally re-written Feb 17, 2016 xzhang
         */

        StringBuilder precip24HStringBuilder = new StringBuilder();
        precip24HStringBuilder
                .append(" SELECT concat(year, '-', day_of_year) date,   "
                        + " pcp_hr_amt_01, pcp_hr_amt_02, pcp_hr_amt_03, pcp_hr_amt_04, pcp_hr_amt_05,  "
                        + " pcp_hr_amt_06, pcp_hr_amt_07, pcp_hr_amt_08, pcp_hr_amt_09, pcp_hr_amt_10,  "
                        + " pcp_hr_amt_11, pcp_hr_amt_12, pcp_hr_amt_13, pcp_hr_amt_14, pcp_hr_amt_15,  "
                        + " pcp_hr_amt_16, pcp_hr_amt_17, pcp_hr_amt_18, pcp_hr_amt_19, pcp_hr_amt_20,  "
                        + " pcp_hr_amt_21, pcp_hr_amt_22, pcp_hr_amt_23, pcp_hr_amt_24 "
                        + " FROM " + ClimateDAOValues.CLI_ASOS_DAILY_TABLE_NAME
                        + " a join "
                        + ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME
                        + " s on a.station_code = s.station_code "
                        + " WHERE   s.station_id = :station_id ");

        // for 24-hour checks, if looking from the start of the month,
        // need to look from the end of the previous month instead
        ClimateDate beginDate24HourCheck = ClimateDate.getMissingClimateDate();
        boolean spanflag = false;
        if (beginDate.getDay() == 1) {
            beginDate24HourCheck.setMon(beginDate.getMon() - 1);
            if (beginDate24HourCheck.getMon() == 0) {
                spanflag = true;
                beginDate24HourCheck.setMon(12);
                beginDate24HourCheck.setYear(beginDate.getYear() - 1);
            } else {
                beginDate24HourCheck.setYear(beginDate.getYear());
            }

            beginDate24HourCheck
                    .setDay(ClimateUtilities.daysInMonth(beginDate24HourCheck));
        } else {
            beginDate24HourCheck = new ClimateDate(beginDate);
        }

        String startDay = beginDate24HourCheck.toMonthDayDateString();
        String endDay = endDate.toMonthDayDateString();

        // reset keyParamMap
        keyParamMap.clear();

        if (spanflag) {
            keyParamMap.put("previousYear", beginDate24HourCheck.getYear());
            precip24HStringBuilder.append(
                    " AND (day_of_year >= :beginDate AND year = :previousYear) or (day_of_year <= :endDate AND year = :year) ");
        } else {
            precip24HStringBuilder.append(
                    " AND day_of_year >= :beginDate AND day_of_year <= :endDate AND year = :year");
        }
        precip24HStringBuilder.append(" order by year, day_of_year ");

        keyParamMap.put("station_id", informid);
        keyParamMap.put("beginDate", startDay);
        keyParamMap.put("endDate", endDay);
        keyParamMap.put("year", beginDate.getYear());

        Map<String, float[]> precipHourMap = new HashMap<>();

        try {
            Object[] results = getDao().executeSQLQuery(
                    precip24HStringBuilder.toString(), keyParamMap);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        String dateKey = (String) oa[0];
                        float[] values = new float[TimeUtil.HOURS_PER_DAY];
                        // precip values could be null
                        for (int l = 0; l < values.length; l++) {
                            values[l] = oa[l + 1] == null
                                    ? ParameterFormatClimate.MISSING_PRECIP
                                    : ((Number) oa[l + 1]).floatValue();
                        }
                        precipHourMap.put(dateKey, values);
                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("Could not get 24 hour precip using query: ["
                        + precip24HStringBuilder + "] and map: [" + keyParamMap
                        + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database for 24 hour precip",
                    e);
        }

        String firstRecord = beginDate24HourCheck.getYear() + "-" + startDay;

        float currentMaxPrecipValue = 0.0f;
        float[] currentPrecipValues = precipHourMap.get(firstRecord);
        int[] maxPrecipValueDays = new int[] { -1, -1 };

        for (int i = 1; i <= endDate.getDay(); i++) {
            String key = String.format("%4d-%02d-%02d", beginDate.getYear(),
                    beginDate.getMon(), i);
            currentMaxPrecipValue = checkMaxPrecipValue(currentPrecipValues,
                    precipHourMap.get(key), currentMaxPrecipValue,
                    maxPrecipValueDays, i);
            currentPrecipValues = precipHourMap.get(key);
        }

        // calculated max has a valid value
        if (currentMaxPrecipValue != 0.0f
                // AND current max is missing
                && ((oPeriodData
                        .getPrecipMax24H() == ParameterFormatClimate.MISSING_PRECIP)
                        // OR calculated is >= current max (since calculated is
                        // not 0, handles possibility of max being trace)
                        || (currentMaxPrecipValue >= oPeriodData
                                .getPrecipMax24H())
                        // OR current max is 0 and calculated is trace
                        || (currentMaxPrecipValue == ParameterFormatClimate.TRACE
                                && oPeriodData.getPrecipMax24H() == 0.0f))) {

            oPeriodData.setPrecipMax24H(currentMaxPrecipValue);
            List<ClimateDates> precip24HMaxDates = new ArrayList<>();
            precip24HMaxDates.add((new ClimateDates(
                    maxPrecipValueDays[0] == 0
                            ? new ClimateDate(beginDate24HourCheck)
                            : new ClimateDate(maxPrecipValueDays[0],
                                    beginDate.getMon(), beginDate.getYear()),
                    new ClimateDate(maxPrecipValueDays[1], beginDate.getMon(),
                            beginDate.getYear()))));
            oPeriodData.setPrecip24HDates(precip24HMaxDates);
        }

        /* cumulative snowfall */
        oPeriodData.setSnowTotal(buildElement(beginDate24HourCheck, endDate,
                informid, PeriodType.OTHER, "snow", "snow",
                BuildElementType.SUM, ParameterFormatClimate.MISSING_SNOW, true)
                        .floatValue());

        /* 10/23/00 - Doug Murphy: */
        /* Find max 24-hr snowfall from stored daily data */
        StringBuilder maxSnow24HQuery = new StringBuilder(
                "SELECT MAX(snow) FROM ");
        maxSnow24HQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxSnow24HQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxSnow24HQuery.append(
                " AND station_id = :stationId AND snow != :missingValue");

        keyParamMap.clear();
        keyParamMap.put("stationId", informid);
        keyParamMap.put("beginDate", ecStartDate);
        keyParamMap.put("endDate", ecEndDate);
        keyParamMap.put("missingValue", (int) ParameterFormatClimate.MISSING);

        oPeriodData.setSnowMax24H(
                ((Number) queryForOneValue(maxSnow24HQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .floatValue());

        // don't need missing value for now
        keyParamMap.remove("missingValue");

        if (ClimateUtilities.floatingEquals(oPeriodData.getSnowMax24H(), 0.0)) {
            /* check for trace amounts */
            StringBuilder maxSnow24HTraceQuery = new StringBuilder(
                    "SELECT MAX(snow) FROM ");
            maxSnow24HTraceQuery
                    .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            maxSnow24HTraceQuery
                    .append(" WHERE date >= :beginDate AND date <= :endDate ");
            maxSnow24HTraceQuery
                    .append(" AND station_id = :stationId AND snow = -1.0 ");

            float traceSnow = ((Number) queryForOneValue(
                    maxSnow24HTraceQuery.toString(), keyParamMap,
                    ParameterFormatClimate.MISSING)).floatValue();

            /*
             * if trace snow value is missing, then 0 was true max. If not,
             * trace is max
             */
            if (ClimateUtilities.floatingEquals(traceSnow,
                    ParameterFormatClimate.MISSING)) {
                oPeriodData.setSnowMax24H(0);
            } else {
                oPeriodData.setSnowMax24H(ParameterFormatClimate.TRACE);
            }
        }

        /* dates with max 24H snowfall */
        if (oPeriodData.getSnowMax24H() != ParameterFormatClimate.MISSING) {
            StringBuilder maxSnow24HDatesQuery = new StringBuilder(
                    "SELECT date, snow FROM ");
            maxSnow24HDatesQuery
                    .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            maxSnow24HDatesQuery
                    .append(" WHERE date >= :beginDate  AND date <= :endDate ");
            maxSnow24HDatesQuery.append(" AND station_id = :stationId ");
            maxSnow24HDatesQuery.append(" AND snow = :maxSnow");

            // new key
            keyParamMap.put("maxSnow", oPeriodData.getSnowMax24H());

            try {
                Object[] results = getDao().executeSQLQuery(
                        maxSnow24HDatesQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {

                    for (Object result : results) {
                        if (result instanceof Object[]) {
                            Object[] oa = (Object[]) result;
                            /* dates with max 24H snow */
                            oPeriodData.getSnow24HDates()
                                    .add(new ClimateDates(
                                            new ClimateDate((Date) oa[0]),
                                            new ClimateDate((Date) oa[0])));

                            // snow value unused
                        } else {
                            throw new Exception(
                                    "Unexpected return type from query, expected Object[], got "
                                            + result.getClass().getName());
                        }
                    }
                } else {
                    logger.warn(
                            "Could not get 24 hour max snow dates using query: ["
                                    + maxSnow24HDatesQuery + "] and map: ["
                                    + keyParamMap + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error querying the climate database for 24 hour max snow dates",
                        e);
            }

            // remove key
            keyParamMap.remove("maxSnow");
        }

        StringBuilder maxSnowPosQuery = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        maxSnowPosQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxSnowPosQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxSnowPosQuery.append(
                " AND station_id = :stationId AND snow::numeric >= 0.001 ");
        /* return greater than zero but not a trace */
        int snowDays = ((Number) queryForOneValue(maxSnowPosQuery.toString(),
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        /* now return the trace amounts */
        StringBuilder maxSnowTraceQuery = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        maxSnowTraceQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxSnowTraceQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxSnowTraceQuery
                .append(" AND station_id = :stationId AND snow <= -1.0 ");
        oPeriodData.setNumSnowGreaterThanTR(
                ((Number) queryForOneValue(maxSnowTraceQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .intValue());

        /*
         * add to get total days GE trace
         */
        oPeriodData.setNumSnowGreaterThanTR(
                oPeriodData.getNumSnowGreaterThanTR() + snowDays);

        StringBuilder maxSnowInchQuery = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        maxSnowInchQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        maxSnowInchQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        maxSnowInchQuery
                .append(" AND station_id = :stationId AND snow >= 1.0 ");
        oPeriodData.setNumSnowGreaterThan1(
                ((Number) queryForOneValue(maxSnowInchQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .intValue());

        /* 24 Hour snow */

        /******************* snow depth information ************************/
        oPeriodData.setSnowGroundMax(buildElement(beginDate24HourCheck, endDate,
                informid, PeriodType.OTHER, "snow_ground", "snow_ground",
                BuildElementType.MAX, ParameterFormatClimate.MISSING_SNOW, true)
                        .intValue());

        if (oPeriodData
                .getSnowGroundMax() != ParameterFormatClimate.MISSING_SNOW_VALUE
                && oPeriodData.getSnowGroundMax() > 0) {
            StringBuilder maxSnowGroundDatesQuery = new StringBuilder(
                    "SELECT date FROM ");
            maxSnowGroundDatesQuery
                    .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            maxSnowGroundDatesQuery
                    .append(" WHERE date >= :beginDate AND date <= :endDate ");
            maxSnowGroundDatesQuery.append(
                    " AND station_id = :stationId AND snow_ground = :snowValue");

            // need snow value key
            keyParamMap.put("snowValue", oPeriodData.getSnowGroundMax());

            try {
                Object[] results = getDao().executeSQLQuery(
                        maxSnowGroundDatesQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {

                    for (Object result : results) {
                        if (result instanceof Date) {
                            oPeriodData.getSnowGroundMaxDateList().add(0,
                                    new ClimateDate((Date) result));
                        } else {
                            throw new Exception(
                                    "Unexpected return type from query, expected java.sql.Date, got "
                                            + result.getClass().getName());
                        }
                    }
                } else {
                    logger.warn(
                            "Could not get max snow depth dates using query: ["
                                    + maxSnowGroundDatesQuery + "] and map: ["
                                    + keyParamMap + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error querying the climate database for max snow depth dates",
                        e);
            }

            // remove snow value key
            keyParamMap.remove("snowValue");
        }

        /**************** heating degree days ******************************/

        StringBuilder heatingDaysQuery = new StringBuilder(
                "SELECT cast(SUM(heat) as int) FROM ");
        heatingDaysQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        heatingDaysQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        heatingDaysQuery.append(" AND station_id = :stationId ");
        heatingDaysQuery.append(" AND heat != ")
                .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
        oPeriodData.setNumHeatTotal(
                ((Number) queryForOneValue(heatingDaysQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING_DEGREE_DAY))
                                .intValue());

        ClimateDate july1Date = new ClimateDate(1, 7, beginDate.getYear());

        if (beginDate.getMon() < 7) {
            july1Date.setYear(july1Date.getYear() - 1);
        }

        keyParamMap.put("beginDate", july1Date.getCalendarFromClimateDate());

        oPeriodData.setNumHeat1July(
                ((Number) queryForOneValue(heatingDaysQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .intValue());

        /* change the beginning date back */
        keyParamMap.put("beginDate", ecStartDate);

        /**********************
         * cooling degree days
         ******************************/

        StringBuilder coolingDaysQuery = new StringBuilder(
                "SELECT cast(SUM(cool) as int) FROM ");
        coolingDaysQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        coolingDaysQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        coolingDaysQuery.append(" AND station_id = :stationId ");
        coolingDaysQuery.append(" AND cool != ")
                .append(ParameterFormatClimate.MISSING_DEGREE_DAY);
        oPeriodData.setNumCoolTotal(
                ((Number) queryForOneValue(coolingDaysQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING_DEGREE_DAY))
                                .intValue());

        keyParamMap.put("beginDate", new ClimateDate(1, 1, beginDate.getYear())
                .getCalendarFromClimateDate());

        oPeriodData.setNumCool1Jan(
                ((Number) queryForOneValue(coolingDaysQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .intValue());

        /* change the beginning date back */
        keyParamMap.put("beginDate", ecStartDate);
        /********************* mean sky cover ******************************/

        StringBuilder meanSkyCoverQuery = new StringBuilder(
                "SELECT cast(AVG(avg_sky_cover) as real) ");
        meanSkyCoverQuery.append(" FROM ")
                .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        meanSkyCoverQuery
                .append(" WHERE date >= :beginDate  AND date<= :endDate ");
        meanSkyCoverQuery.append(" AND station_id = :stationId");

        oPeriodData.setMeanSkyCover(
                ((Number) queryForOneValue(meanSkyCoverQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .floatValue());

        /****************** number cloudy days ***************************/
        /*
         * Legacy documentation:
         * 
         * Task 23218
         * 
         * Note: the display will show values of
         * 
         * 0-.3 for fair
         * 
         * .4-.7 for partly
         * 
         * cloudy .8-1.0 for cloudy
         * 
         * We're assuming that rounding up will be okay here as the average sky
         * cover for the month can be .313 or .35 when Informix does the
         * average. Thresholds below reflect our assumptions.
         */

        /*
         * smo, DR 16759, The F-6 Climate Product should report sky cover in
         * oktas and not tenths. (1) scale the sky_cover by a factor of 0.8,
         * then 0-0.2 for fair; 0.3-0.6 for partly cloudy and 0.7-0.8 for cloudy
         */

        /*
         * smo, DR 16759 thresh= 0.75;
         */
        // thresh= 0.8125; // smo, 0.8125/10*8 = 0.65, 1/10*8=0.8, 0.65~0.8 <->
        // 7~8
        StringBuilder cloudyDaysQuery = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        cloudyDaysQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        cloudyDaysQuery
                .append(" WHERE date >= :beginDate AND date <= :endDate ");
        cloudyDaysQuery.append(
                " AND station_id = :stationId AND avg_sky_cover::numeric >= 0.8125");

        oPeriodData.setNumMostlyCloudyDays(
                ((Number) queryForOneValue(cloudyDaysQuery.toString(),
                        keyParamMap, ParameterFormatClimate.MISSING))
                                .intValue());

        /****************** number partly cloudy days **********************/
        if (oPeriodData
                .getNumMostlyCloudyDays() < ParameterFormatClimate.MISSING) {
            /*
             * smo, DR 16759 thresh = 0.35;
             */
            // thresh = 0.3125; // smo, 0.3125/10*8 = 0.25, 0.25~0.64 <-> 3~6
            StringBuilder partlyCloudyDaysQuery = new StringBuilder(
                    "SELECT cast(COUNT(*) as int) ");
            partlyCloudyDaysQuery.append(" FROM ")
                    .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            partlyCloudyDaysQuery
                    .append(" WHERE date >= :beginDate AND date <= :endDate ");
            partlyCloudyDaysQuery.append(
                    " AND station_id = :stationId AND avg_sky_cover::numeric >= 0.3125");

            oPeriodData.setNumPartlyCloudyDays(
                    ((Number) queryForOneValue(partlyCloudyDaysQuery.toString(),
                            keyParamMap, ParameterFormatClimate.MISSING))
                                    .intValue());

            oPeriodData
                    .setNumPartlyCloudyDays(oPeriodData.getNumPartlyCloudyDays()
                            - oPeriodData.getNumMostlyCloudyDays());

            /*************************
             * number fair days
             ********************************/

            /* smo, DR 16759, 0~0.24 <-> 0~2 */
            StringBuilder fairDaysQuery = new StringBuilder(
                    "SELECT cast(count(*) as int) FROM ");
            fairDaysQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            fairDaysQuery
                    .append(" WHERE date >= :beginDate AND date <= :endDate ");
            fairDaysQuery.append(
                    " AND station_id = :stationId AND avg_sky_cover >= 0.0");

            oPeriodData.setNumFairDays(
                    ((Number) queryForOneValue(fairDaysQuery.toString(),
                            keyParamMap, ParameterFormatClimate.MISSING))
                                    .intValue());
            oPeriodData.setNumFairDays(oPeriodData.getNumFairDays()
                    - oPeriodData.getNumPartlyCloudyDays()
                    - oPeriodData.getNumMostlyCloudyDays());
        } else {
            oPeriodData.setNumFairDays(ParameterFormatClimate.MISSING);
            oPeriodData.setNumPartlyCloudyDays(ParameterFormatClimate.MISSING);
        }

        /*******************
         * weather elements for the month
         *******************************/
        keyParamMap.remove("missingValue");

        // thunder
        String numThunderString = getDailyWeatherCountQuery(
                DailyClimateData.WX_THUNDER_STORM_INDEX + 1);

        int thunderDays = ((Number) queryForOneValue(numThunderString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (thunderDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumThunderStorms(thunderDays);
        }

        // mixed precip
        String numMixedPrecipString = getDailyWeatherCountQuery(
                DailyClimateData.WX_MIXED_PRECIP_INDEX + 1);

        int mixedDays = ((Number) queryForOneValue(numMixedPrecipString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (mixedDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumMixedPrecip(mixedDays);
        }

        // rain
        String numHeavyRainString = getDailyWeatherCountQuery(
                DailyClimateData.WX_HEAVY_RAIN_INDEX + 1);

        int heavyRainDays = ((Number) queryForOneValue(numHeavyRainString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (heavyRainDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumHeavyRain(heavyRainDays);
        }

        String numRainString = getDailyWeatherCountQuery(
                DailyClimateData.WX_RAIN_INDEX + 1);

        int rainDays = ((Number) queryForOneValue(numRainString, keyParamMap,
                ParameterFormatClimate.MISSING)).intValue();

        if (rainDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumRain(rainDays);
        }

        String numLightRainString = getDailyWeatherCountQuery(
                DailyClimateData.WX_LIGHT_RAIN_INDEX + 1);

        int lightRainDays = ((Number) queryForOneValue(numLightRainString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (lightRainDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumLightRain(lightRainDays);
        }

        // freezing rain
        String numFreezingRainString = getDailyWeatherCountQuery(
                DailyClimateData.WX_FREEZING_RAIN_INDEX + 1);

        int freezeRainDays = ((Number) queryForOneValue(numFreezingRainString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (freezeRainDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumFreezingRain(freezeRainDays);
        }

        String numLightFreezingRainString = getDailyWeatherCountQuery(
                DailyClimateData.WX_LIGHT_FREEZING_RAIN_INDEX + 1);

        int lightFreezeRainDays = ((Number) queryForOneValue(
                numLightFreezingRainString, keyParamMap,
                ParameterFormatClimate.MISSING)).intValue();

        if (lightFreezeRainDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumLightFreezingRain(lightFreezeRainDays);
        }

        // hail
        String numHailString = getDailyWeatherCountQuery(
                DailyClimateData.WX_HAIL_INDEX + 1);

        int hailDays = ((Number) queryForOneValue(numHailString, keyParamMap,
                ParameterFormatClimate.MISSING)).intValue();

        if (hailDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumHail(hailDays);
        }

        // snow
        String numHeavySnowString = getDailyWeatherCountQuery(
                DailyClimateData.WX_HEAVY_SNOW_INDEX + 1);

        int heavySnowDays = ((Number) queryForOneValue(numHeavySnowString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (heavySnowDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumHeavySnow(heavySnowDays);
        }

        String numSnowString = getDailyWeatherCountQuery(
                DailyClimateData.WX_SNOW_INDEX + 1);

        int regSnowDays = ((Number) queryForOneValue(numSnowString, keyParamMap,
                ParameterFormatClimate.MISSING)).intValue();

        if (regSnowDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumSnow(regSnowDays);
        }

        String numLightSnowString = getDailyWeatherCountQuery(
                DailyClimateData.WX_LIGHT_SNOW_INDEX + 1);

        int lightSnowDays = ((Number) queryForOneValue(numLightSnowString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (lightSnowDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumLightSnow(lightSnowDays);
        }

        // ice pellets
        String numIcePelletsString = getDailyWeatherCountQuery(
                DailyClimateData.WX_ICE_PELLETS_INDEX + 1);

        int icePelletsDays = ((Number) queryForOneValue(numIcePelletsString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (icePelletsDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumIcePellets(icePelletsDays);
        }

        // fog
        String numFogString = getDailyWeatherCountQuery(
                DailyClimateData.WX_FOG_INDEX + 1);

        int fogDays = ((Number) queryForOneValue(numFogString, keyParamMap,
                ParameterFormatClimate.MISSING)).intValue();

        if (fogDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumFog(fogDays);
        }

        String numFogQuarterSMString = getDailyWeatherCountQuery(
                DailyClimateData.WX_FOG_QUARTER_SM_INDEX + 1);

        int heavyFogDays = ((Number) queryForOneValue(numFogQuarterSMString,
                keyParamMap, ParameterFormatClimate.MISSING)).intValue();

        if (heavyFogDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumFogQuarterSM(heavyFogDays);
        }

        // haze
        String numHazeString = getDailyWeatherCountQuery(
                DailyClimateData.WX_HAZE_INDEX + 1);

        int hazeDays = ((Number) queryForOneValue(numHazeString, keyParamMap,
                ParameterFormatClimate.MISSING)).intValue();

        if (hazeDays != ParameterFormatClimate.MISSING) {
            oPeriodData.setNumHaze(hazeDays);
        }

        return oPeriodData;
    }

    /**
     * @param weather
     *            element index in database.
     * @return weather count query for some index. Query map must be used to
     *         provide stationId, beginDate, and endDate.
     */
    private static String getDailyWeatherCountQuery(int index) {
        StringBuilder query = new StringBuilder(
                "SELECT cast(COUNT(*) as int) FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE date >= :beginDate AND date <= :endDate ");
        query.append(" AND station_id = :stationId AND  wx_");
        query.append(index).append("> 0 ");

        return query.toString();
    }

    /**
     * Converted from retrieve_Msm_Prs.ec
     * 
     * Original comments:
     * 
     * <pre>
     *  *******************************************************************************
     * 
     *  retrieve_msm_prs.ec
     * 
     *  David T. Miller   April 2000
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *  Retrieves the ASOS monthly summary message sea_level pressure values for a station and
     *  given month.
     * 
     ******************************************************************************* 
     * </pre>
     * 
     * @param aDate
     * @param station
     * @return
     * @throws ClimateQueryException
     */

    public SLP retrieveMsmPrs(ClimateDate aDate, Station station)
            throws ClimateQueryException {
        SLP slp = SLP.getMissingSLP();

        StringBuilder keyQuery = new StringBuilder(
                "SELECT max_sea_press, substring( max_slp_date from 1 for 2) max_slp_date, ");
        keyQuery.append(
                " min_sea_press, substring(min_slp_date FROM 1 for 2) min_slp_date ");
        keyQuery.append(" FROM ")
                .append(ClimateDAOValues.CLI_ASOS_MONTHLY_TABLE_NAME);
        keyQuery.append(" a JOIN ")
                .append(ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME);
        keyQuery.append(" s ON a.station_code = s.station_code ");
        keyQuery.append(" WHERE s.station_id = :stationId ");
        keyQuery.append(" AND year = :year AND month = :month ");

        Map<String, Object> keyParamMap = new HashMap<>();
        keyParamMap.put("stationId", station.getInformId());
        keyParamMap.put("year", aDate.getYear());
        keyParamMap.put("month", aDate.getMon());

        try {
            Object[] results = getDao().executeSQLQuery(keyQuery.toString(),
                    keyParamMap);
            if ((results != null) && (results.length >= 1)) {
                // get the first record
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        Object[] oa = (Object[]) result;
                        // values could be null
                        slp.setMaxSLP(oa[0] == null
                                ? ParameterFormatClimate.MISSING_SLP
                                : ((Number) oa[0]).floatValue());
                        slp.setDayMaxSLP(oa[1] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : Integer.parseInt((String) oa[1]));
                        slp.setMinSLP(oa[2] == null
                                ? ParameterFormatClimate.MISSING_SLP
                                : ((Number) oa[2]).floatValue());
                        slp.setDayMinSLP(oa[3] == null
                                ? ParameterFormatClimate.MISSING_DATE
                                : Integer.parseInt((String) oa[3]));
                    } catch (Exception e) {
                        // if casting failed
                        throw new Exception("Unexpected return column type.",
                                e);
                    }
                } else {
                    throw new Exception(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }
            } else {
                logger.warn("Could not get SLP data using query: [" + keyQuery
                        + "] and map: [" + keyParamMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database for SLP", e);
        }

        keyParamMap.clear();
        ClimateDate start = new ClimateDate(aDate);
        start.setDay(1);
        keyParamMap.put("stationId", station.getInformId());
        keyParamMap.put("startDate", start.getCalendarFromClimateDate());
        keyParamMap.put("endDate", aDate.getCalendarFromClimateDate());
        keyParamMap.put("missValue", ParameterFormatClimate.MISSING_SLP);

        /******* This section added 3/10/01 ***************/
        if (ClimateUtilities.floatingEquals(slp.getMaxSLP(),
                ParameterFormatClimate.MISSING_SLP)) {
            StringBuilder maxSLPBackupQuery = new StringBuilder(
                    "SELECT max_slp, cast(EXTRACT(DAY FROM date) as int) ");
            maxSLPBackupQuery.append(" FROM ")
                    .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            maxSLPBackupQuery.append(" WHERE station_id = :stationId ");
            maxSLPBackupQuery.append(
                    " AND date BETWEEN :startDate AND :endDate AND max_slp = ");
            maxSLPBackupQuery.append(" (SELECT MAX(max_slp) FROM ");
            maxSLPBackupQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            maxSLPBackupQuery.append(" WHERE station_id = :stationId ");
            maxSLPBackupQuery.append(
                    " AND date BETWEEN :startDate AND :endDate AND max_slp != :missValue )");

            try {
                Object[] results = getDao().executeSQLQuery(
                        maxSLPBackupQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    // get the first record
                    Object result = results[0];
                    if (result instanceof Object[]) {
                        try {
                            Object[] oa = (Object[]) result;
                            // max SLP could be null
                            slp.setMaxSLP(oa[0] == null
                                    ? ParameterFormatClimate.MISSING_SLP
                                    : ((Number) oa[0]).floatValue());
                            slp.setDayMaxSLP(((Number) oa[1]).intValue());
                        } catch (Exception e) {
                            // if casting failed
                            throw new Exception(
                                    "Unexpected return column type.", e);
                        }
                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                } else {
                    logger.warn("Could not get SLP data from query: ["
                            + maxSLPBackupQuery + "] and map: [" + keyParamMap
                            + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error querying the climate database for SLP", e);
            }

        }

        if (ClimateUtilities.floatingEquals(slp.getMinSLP(),
                ParameterFormatClimate.MISSING_SLP)) {
            StringBuilder minSLPBackupQuery = new StringBuilder(
                    "SELECT min_slp, cast(EXTRACT(DAY FROM  date) as int) ");
            minSLPBackupQuery.append(" FROM ")
                    .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            minSLPBackupQuery.append(" WHERE station_id = :stationId ");
            minSLPBackupQuery.append(
                    " AND date BETWEEN :startDate AND :endDate AND min_slp = ");
            minSLPBackupQuery.append(" (SELECT MIN(min_slp) FROM ");
            minSLPBackupQuery.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            minSLPBackupQuery.append(" WHERE station_id = :stationId ");
            minSLPBackupQuery.append(
                    " AND date BETWEEN :startDate AND :endDate AND min_slp != :missValue )");

            try {
                Object[] results = getDao().executeSQLQuery(
                        minSLPBackupQuery.toString(), keyParamMap);
                if ((results != null) && (results.length >= 1)) {
                    // get the first record
                    Object result = results[0];
                    if (result instanceof Object[]) {
                        try {
                            Object[] oa = (Object[]) result;
                            // min SLP could be null
                            slp.setMinSLP(oa[0] == null
                                    ? ParameterFormatClimate.MISSING_SLP
                                    : ((Number) oa[0]).floatValue());
                            slp.setDayMinSLP(((Number) oa[1]).intValue());
                        } catch (Exception e) {
                            // if casting failed
                            throw new Exception(
                                    "Unexpected return column type.", e);
                        }

                    } else {
                        throw new Exception(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }

                } else {
                    logger.warn("Could not get min SLP data using query: ["
                            + minSLPBackupQuery + "] and map: [" + keyParamMap
                            + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error querying the climate database for SLP", e);
            }
        }

        return slp;
    }

    /**
     * Migrated from retrieve_daily_sum.ec
     * 
     * <pre>
     *    Name:
     *       retrieve_daily_sum.ec
     *       GFS1-NHD:A14950.0000-SRC;12
     *
     * retrieve_daily_sum.ec
     *
     * David T. Miller   April 1999
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     * Retrieves the ASOS daily summary message climate values for a station and
     * given date.
     * 
     * </pre>
     * 
     * @param aDate
     * @param yesterday
     *            daily data to fill out. Needs to already have station ID set.
     * @param itype
     * @param endTime
     * @param numOffUTC
     * @throws ClimateQueryException
     */

    public void retrieveDailySummary(ClimateDate aDate,
            DailyClimateData yesterday, PeriodType itype, ClimateTime endTime,
            short numOffUTC) throws ClimateQueryException {
        int stationID = yesterday.getInformId();

        String dayOfYear = aDate.toMonthDayDateString();

        // verify station code exists for station ID
        String stationCode = getStationCodeByID(stationID);

        if (stationCode.isEmpty()) {
            // no station code exists for the given station ID
            throw new ClimateQueryException("Station code not found in the "
                    + ClimateDAOValues.STATION_LOCATION_TABLE_NAME
                    + " table for station ID [" + stationID + "].");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationCode", stationCode);
        paramMap.put("dayOfYear", dayOfYear);

        // verify some data exists for station code on day of the year; get the
        // year
        StringBuilder yearQuery = new StringBuilder("SELECT year FROM ");
        yearQuery.append(ClimateDAOValues.CLI_ASOS_DAILY_TABLE_NAME);
        yearQuery.append(" WHERE station_code = :stationCode");
        yearQuery.append(" AND day_of_year = :dayOfYear");
        yearQuery.append(" ORDER BY year DESC");

        int year = ((Number) queryForOneValue(yearQuery.toString(), paramMap,
                -1)).intValue();
        if (year == -1) {
            logger.warn("Daily Summary Message not available for station ["
                    + stationCode
                    + "] as the year was not found for time of year ["
                    + dayOfYear
                    + "] in the climate database. Some data will be missing.");
            return;
        }

        // get valid time (only an hour, or missing value) of DSM
        StringBuilder validTimeQuery = new StringBuilder(
                "SELECT to_char(valid_time, 'HH24:MI') FROM ");
        validTimeQuery.append(ClimateDAOValues.CLI_ASOS_DAILY_TABLE_NAME);
        validTimeQuery.append(" WHERE station_code = :stationCode");
        validTimeQuery.append(" AND day_of_year = :dayOfYear");

        String dsmValidTimeString = (String) queryForOneValue(
                validTimeQuery.toString(), paramMap, "");

        /*
         * Legacy documentation:
         * 
         * check to see if valid time is null
         */
        int dsmValidTimeHour;
        if (!dsmValidTimeString.isEmpty()) {
            dsmValidTimeHour = Integer
                    .parseInt(dsmValidTimeString.substring(0, 2));
        } else {
            dsmValidTimeHour = 99;
        }

        /*
         * Legacy documentation:
         * 
         * if dsm valid time is missing, then full dsm available for a morning
         * climate run OR if the end_time hour is within 1 hour of the dsm
         * intermediate hour, use this dsm.
         */
        int localHour = endTime.getHour() + numOffUTC;
        logger.debug("The local hour for the station with informId ["
                + yesterday.getInformId() + "] is [" + localHour
                + "] for DSM date [" + aDate.toFullDateString() + "]");
        if (localHour < 0) {
            localHour += TimeUtil.HOURS_PER_DAY;
        } else if (localHour >= TimeUtil.HOURS_PER_DAY) {
            localHour -= TimeUtil.HOURS_PER_DAY;
        }

        int runTimeDiffFromValidTime = Math.abs(dsmValidTimeHour - localHour);

        /*
         * Legacy documentation:
         * 
         * if the years match and either this is a full dsm with climate summary
         * or this is a intermediate dsm within 1 hour of run time, then use the
         * dsm. Note, for now, only the information absolutely required by
         * create_climate will be retrieved.
         * 
         * New documentation: evening DSMs to be treated the same as
         * intermediate in this case
         */
        if (year == aDate.getYear()) {
            logger.debug(
                    "The DSM valid time hour for the station with informId ["
                            + yesterday.getInformId() + "] is ["
                            + dsmValidTimeHour + "] for DSM date ["
                            + aDate.toFullDateString() + "]");
            if ((dsmValidTimeHour >= TimeUtil.HOURS_PER_DAY
                    && (itype.equals(PeriodType.MORN_NWWS)
                            || itype.equals(PeriodType.MORN_RAD)))
                    || ((!itype.equals(PeriodType.MORN_NWWS)
                            && (!itype.equals(PeriodType.MORN_RAD))
                            && (runTimeDiffFromValidTime <= 1)))) {
                StringBuilder query = new StringBuilder(
                        "SELECT maxtemp_cal, maxtemp_cal_time, ");
                query.append(" mintemp_cal, mintemp_cal_time, ");
                /*
                 * These values are unused and not part of the Data class: +
                 * " maxtemp_day, mintemp_day, "
                 */
                query.append(" min_press, ");
                /*
                 * This value is unused and not part of the Data class: +
                 * " min_press_time, "
                 */
                query.append(" equiv_water, ");
                /*
                 * These values are unused and not part of the Data class: +
                 * " pcp_hr_amt_01, pcp_hr_amt_02, pcp_hr_amt_03, " +
                 * " pcp_hr_amt_04, pcp_hr_amt_05, pcp_hr_amt_06, " +
                 * " pcp_hr_amt_07, pcp_hr_amt_08, pcp_hr_amt_09, " +
                 * " pcp_hr_amt_10, pcp_hr_amt_11, pcp_hr_amt_12, " +
                 * " pcp_hr_amt_13, pcp_hr_amt_14, pcp_hr_amt_15, " +
                 * " pcp_hr_amt_16, pcp_hr_amt_17, pcp_hr_amt_18, " +
                 * " pcp_hr_amt_19, pcp_hr_amt_20, pcp_hr_amt_21, " +
                 * " pcp_hr_amt_22, pcp_hr_amt_23, pcp_hr_amt_24, "
                 */
                query.append(
                        " twomin_wspd, max2min_wdir, max2min_wspd, max2min_wnd_time, ");
                query.append(" pkwnd_dir, pkwnd_spd, pkwnd_time, ");
                query.append(
                        " wx1, wx2, wx3, wx4, wx5, min_sun, percent_sun, ");
                query.append(" solid_precip, snowdepth, ");
                /*
                 * This value is unused and not part of the Data class: +
                 * " sky_cover, "
                 */
                query.append(" avg_sky_cover ");
                /*
                 * This value is unused and not part of the Data class: +
                 * " remarks "
                 */
                query.append(" FROM ")
                        .append(ClimateDAOValues.CLI_ASOS_DAILY_TABLE_NAME);
                query.append(" WHERE station_code = :stationCode");
                query.append(" AND day_of_year = :dayOfYear");

                try {
                    Object[] results = getDao()
                            .executeSQLQuery(query.toString(), paramMap);
                    if ((results != null) && (results.length >= 1)) {
                        Object result = results[0]; // get the first record
                        if (result instanceof Object[]) {
                            Object[] oa = (Object[]) result;
                            /*
                             * Any of the values could be null
                             */
                            // max temp
                            yesterday.setMaxTemp(oa[0] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[0]).intValue());
                            // max temp time
                            yesterday.setMaxTempTime(new ClimateTime(oa[1]));
                            // min temp
                            yesterday.setMinTemp(oa[2] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[2]).intValue());
                            // min temp time
                            yesterday.setMinTempTime(new ClimateTime(oa[3]));
                            // min pressure (SLP)
                            yesterday.setMinSlp(oa[4] == null
                                    ? ParameterFormatClimate.MISSING_SLP
                                    : ((Number) oa[4]).floatValue());
                            // precip/equivalent water
                            yesterday.setPrecip(oa[5] == null
                                    ? ParameterFormatClimate.MISSING_PRECIP
                                    : ((Number) oa[5]).floatValue());
                            // average speed (two minute windspeed)
                            yesterday.setAvgWindSpeed(oa[6] == null
                                    ? ParameterFormatClimate.MISSING_SPEED
                                    : ((Number) oa[6]).floatValue());
                            // max wind dir and speed (two minute)
                            int maxWindDir = oa[7] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[7]).intValue();
                            float maxWindSpd = oa[8] == null
                                    ? ParameterFormatClimate.MISSING_SPEED
                                    : ((Number) oa[8]).intValue();
                            yesterday.setMaxWind(
                                    new ClimateWind(maxWindDir, maxWindSpd));
                            // max wind time
                            yesterday.setMaxWindTime(new ClimateTime(oa[9]));
                            // peak wind/max gust dir and speed
                            int maxGustDir = oa[10] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[10]).intValue();
                            float maxGustSpd = oa[11] == null
                                    ? ParameterFormatClimate.MISSING_SPEED
                                    : ((Number) oa[11]).intValue();
                            yesterday.setMaxGust(
                                    new ClimateWind(maxGustDir, maxGustSpd));
                            // peak wind/max gust time
                            yesterday.setMaxGustTime(new ClimateTime(oa[12]));
                            // wx 1
                            parseWeatherFlagForDailyData(yesterday,
                                    oa[13] == null
                                            ? ParameterFormatClimate.MISSING
                                            : ((Number) oa[13]).intValue());
                            // wx 2
                            parseWeatherFlagForDailyData(yesterday,
                                    oa[14] == null
                                            ? ParameterFormatClimate.MISSING
                                            : ((Number) oa[14]).intValue());
                            // wx 3
                            parseWeatherFlagForDailyData(yesterday,
                                    oa[15] == null
                                            ? ParameterFormatClimate.MISSING
                                            : ((Number) oa[15]).intValue());
                            // wx 4
                            parseWeatherFlagForDailyData(yesterday,
                                    oa[16] == null
                                            ? ParameterFormatClimate.MISSING
                                            : ((Number) oa[16]).intValue());
                            // wx 5
                            parseWeatherFlagForDailyData(yesterday,
                                    oa[17] == null
                                            ? ParameterFormatClimate.MISSING
                                            : ((Number) oa[17]).intValue());
                            // minutes of sun
                            yesterday.setMinutesSun(oa[18] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[18]).intValue());
                            // percent possible sun
                            yesterday.setPercentPossSun(oa[19] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[19]).intValue());
                            // snow in the day/solid precip
                            yesterday.setSnowDay(oa[20] == null
                                    ? ParameterFormatClimate.MISSING_SNOW
                                    : ((Number) oa[20]).floatValue());
                            // snow on ground depth
                            yesterday.setSnowGround(oa[21] == null
                                    ? ParameterFormatClimate.MISSING_SNOW
                                    : ((Number) oa[21]).intValue());
                            // avg sky cover
                            yesterday.setSkyCover(oa[22] == null
                                    ? ParameterFormatClimate.MISSING
                                    : ((Number) oa[22]).floatValue());
                        } else {
                            throw new ClimateQueryException(
                                    "Unexpected return type from query, expected Object[], got "
                                            + result.getClass().getName());
                        }

                    } else {
                        logger.warn(
                                "Could not get daily climate data using query: ["
                                        + query + "] and map: [" + paramMap
                                        + "]");
                    }
                } catch (ClimateQueryException e) {
                    throw new ClimateQueryException(
                            "Error querying the climate database with query: ["
                                    + query + "] and map: [" + paramMap + "]",
                            e);
                }
            } else if (itype.isDaily()) {
                /*
                 * Legacy documentation:
                 * 
                 * If it is morning/evening/intermediate(1,3,2,4,10,11), get
                 * snow_ground from ASOS.
                 */
                StringBuilder query = new StringBuilder(
                        "SELECT snowdepth FROM ");
                query.append(ClimateDAOValues.CLI_ASOS_DAILY_TABLE_NAME);
                query.append(" WHERE station_code = :stationCode");
                query.append(" AND day_of_year = :dayOfYear");
                yesterday.setSnowGround(
                        ((Number) queryForOneValue(query.toString(), paramMap,
                                ParameterFormatClimate.MISSING_SNOW))
                                        .floatValue());
            }
        } else {
            logger.warn("No DSM available for current year with station code: ["
                    + stationCode + "] and day of year: [" + dayOfYear + "]");
        }
    }

    /**
     * Migrated from retrieve_daily_sum.ec.
     * 
     * Parse the given weather flag from the Daily ASOS table, and set the
     * appropriate flag in the given {@link DailyClimateData} object, as well as
     * increment its number of weather events count. Do nothing if the weather
     * flag is set to missing.
     * 
     * @param yesterday
     * @param wx
     */
    private void parseWeatherFlagForDailyData(DailyClimateData yesterday,
            int wx) {
        if (wx != ParameterFormatClimate.MISSING) {
            switch (wx) {
            case 1:
                // Fog
                yesterday.getWxType()[DailyClimateData.WX_FOG_INDEX] = 1;
                break;
            case 2:
                // Heavy fog
                yesterday
                        .getWxType()[DailyClimateData.WX_FOG_QUARTER_SM_INDEX] = 1;
                break;
            case 3:
                // Thunder
                yesterday
                        .getWxType()[DailyClimateData.WX_THUNDER_STORM_INDEX] = 1;
                break;
            case 4:
                // Ice Pellets
                yesterday
                        .getWxType()[DailyClimateData.WX_ICE_PELLETS_INDEX] = 1;
                break;
            case 5:
                // Hail
                yesterday.getWxType()[DailyClimateData.WX_HAIL_INDEX] = 1;
                break;
            case 7:
                // Sandstorm
                yesterday.getWxType()[DailyClimateData.WX_SAND_STORM_INDEX] = 1;
                break;
            case 8:
                // Haze
                yesterday.getWxType()[DailyClimateData.WX_HAZE_INDEX] = 1;
                break;
            case 9:
                // Blowing snow
                yesterday
                        .getWxType()[DailyClimateData.WX_BLOWING_SNOW_INDEX] = 1;
                break;
            case 10:
                // Tornado
                yesterday
                        .getWxType()[DailyClimateData.WX_FUNNEL_CLOUD_INDEX] = 1;
                break;
            default:
                logger.warn("Unhandled weather event type: [" + wx + "]");
            }
            if (yesterday.getNumWx() == ParameterFormatClimate.MISSING) {
                yesterday.setNumWx(1);
            } else {
                yesterday.setNumWx(yesterday.getNumWx() + 1);
            }
        }
    }

    /**
     * Create the Display Daily data select statement for the given station ID
     * and date.
     * 
     * @param iDate
     *            the date to look for.
     * @param iStationID
     *            the station ID to look for.
     * @param paramMap
     *            sql param map to fill out
     * @return the select statement query for the given date and station ID for
     *         Display Daily data.
     */
    private static String getDailyDataForStationAndDateSelectQuery(
            ClimateDate iDate, int iStationID, Map<String, Object> paramMap) {
        StringBuilder query = new StringBuilder(
                "SELECT max_temp, to_char(max_temp_time, 'HH24:MI') as max_temp_time, ");
        query.append(
                " min_temp, to_char(min_temp_time, 'HH24:MI') as min_temp_time, precip, ");
        query.append(
                " snow, snow_ground, heat, cool, max_wind_dir, max_wind_spd, ");
        query.append(
                " to_char(max_wind_time, 'HH24:MI') as max_wind_time, max_gust_dir, max_gust_spd, ");
        query.append(" to_char(max_gust_time, 'HH24:MI') as max_gust_time, ");
        query.append(" result_wind_dir, result_wind_spd, ");
        query.append(
                " avg_wind_speed, min_sun, percent_pos_sun, avg_sky_cover, ");
        query.append(
                " max_rh, max_rh_hour, min_rh, min_rh_hour, max_slp, min_slp, ");
        query.append(
                " wx_1, wx_2, wx_3, wx_4, wx_5, wx_6, wx_7, wx_8, wx_9, wx_10, wx_11, wx_12,");
        query.append(" wx_13, wx_14, wx_15, wx_16, wx_17, wx_18,");
        query.append(
                " max_temp_meth, min_temp_meth, precip_meth, snow_meth, ground_meth,");
        query.append(
                " max_wind_meth, max_gust_meth, avg_wind_meth, min_sun_meth,");
        query.append(" poss_sun_meth, sky_cover_meth, wx_meth FROM ");
        query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
        query.append(" WHERE station_id = :iStationID");
        query.append(" AND date = :date");

        paramMap.put("iStationID", iStationID);
        paramMap.put("date", iDate.getCalendarFromClimateDate());

        return query.toString();
    }

    /**
     * Query the Display Daily data for the given station ID and date.
     * 
     * @param iDate
     *            the date to look for.
     * @param iStationID
     *            the station ID to look for.
     * @return result set.
     * @throws ClimateQueryException
     */
    private Object[] queryDailyDataForStationAndDate(int iStationID,
            ClimateDate date) throws ClimateQueryException {
        Map<String, Object> paramMap = new HashMap<>();
        String query = getDailyDataForStationAndDateSelectQuery(date,
                iStationID, paramMap);

        try {
            return getDao().executeSQLQuery(query, paramMap);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: [" + query
                            + "] and map: [" + paramMap + "]",
                    e);
        }
    }

    /**
     * Migrated from update_daily_db.ecpp
     * 
     * <pre>
     *  void update_daily_db ( const climate_date           &climo_date,
     *                               long           *station_id,
     *                   daily_climate_data *yesterday
     *                               daily_data_method      *qc
     *                )
     *
     *   Jason Tuell        PRC/TDL             HP 9000/7xx
     *   Dan Zipper                 PRC/TDL             
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function updates the daily climate data base.  It enters
     *      the contents of yesterday into the daily observed climate data
     *      base.
     *
     * </pre>
     * 
     * Update daily climate data for a station with the given ID at the given
     * date.
     * 
     * @param iDate
     *            the date to look for.
     * @param iStationID
     *            the station ID to look for.
     * @param iData
     *            the data to update with.
     * @return true if updated, false otherwise.
     * @throws Exception
     */

    public boolean updateDailyDataForStationAndDate(ClimateDate iDate,
            int iStationID, DailyClimateData iData) throws Exception {
        Object[] results = queryDailyDataForStationAndDate(iStationID, iDate);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("max_temp", iData.getMaxTemp());
        queryParams.put("min_temp", iData.getMinTemp());
        queryParams.put("precip", iData.getPrecip());
        queryParams.put("snow", iData.getSnowDay());
        queryParams.put("snow_ground", iData.getSnowGround());
        queryParams.put("heat", iData.getNumHeat());
        queryParams.put("cool", iData.getNumCool());
        queryParams.put("max_wind_dir", iData.getMaxWind().getDir());
        queryParams.put("max_wind_spd", iData.getMaxWind().getSpeed());
        queryParams.put("max_gust_dir", iData.getMaxGust().getDir());
        queryParams.put("max_gust_spd", iData.getMaxGust().getSpeed());
        queryParams.put("result_wind_dir", iData.getResultWind().getDir());
        queryParams.put("result_wind_spd", iData.getResultWind().getSpeed());
        queryParams.put("result_x", iData.getResultX());
        queryParams.put("result_y", iData.getResultY());
        queryParams.put("num_wind_obs", iData.getNumWndObs());
        queryParams.put("avg_wind_speed", iData.getAvgWindSpeed());
        queryParams.put("min_sun", iData.getMinutesSun());
        queryParams.put("percent_pos_sun", iData.getPercentPossSun());
        queryParams.put("avg_sky_cover", iData.getSkyCover());
        queryParams.put("max_rh", iData.getMaxRelHumid());
        queryParams.put("max_rh_hour", iData.getMaxRelHumidHour());
        queryParams.put("min_rh", iData.getMinRelHumid());
        queryParams.put("min_rh_hour", iData.getMinRelHumidHour());
        queryParams.put("max_slp", iData.getMaxSlp());
        queryParams.put("min_slp", iData.getMinSlp());
        queryParams.put("wx_1", iData.getWxType()[0]);
        queryParams.put("wx_2", iData.getWxType()[1]);
        queryParams.put("wx_3", iData.getWxType()[2]);
        queryParams.put("wx_4", iData.getWxType()[3]);
        queryParams.put("wx_5", iData.getWxType()[4]);
        queryParams.put("wx_6", iData.getWxType()[5]);
        queryParams.put("wx_7", iData.getWxType()[6]);
        queryParams.put("wx_8", iData.getWxType()[7]);
        queryParams.put("wx_9", iData.getWxType()[8]);
        queryParams.put("wx_10", iData.getWxType()[9]);
        queryParams.put("wx_11", iData.getWxType()[10]);
        queryParams.put("wx_12", iData.getWxType()[11]);
        queryParams.put("wx_13", iData.getWxType()[12]);
        queryParams.put("wx_14", iData.getWxType()[13]);
        queryParams.put("wx_15", iData.getWxType()[14]);
        queryParams.put("wx_16", iData.getWxType()[15]);
        queryParams.put("wx_17", iData.getWxType()[16]);
        queryParams.put("wx_18", iData.getWxType()[17]);
        queryParams.put("max_temp_meth", iData.getDataMethods().getMaxTempQc());
        queryParams.put("min_temp_meth", iData.getDataMethods().getMinTempQc());
        queryParams.put("precip_meth", iData.getDataMethods().getPrecipQc());
        queryParams.put("snow_meth", iData.getDataMethods().getSnowQc());
        queryParams.put("ground_meth", iData.getDataMethods().getDepthQc());
        queryParams.put("max_wind_meth", iData.getDataMethods().getMaxWindQc());
        queryParams.put("max_gust_meth", iData.getDataMethods().getMaxGustQc());
        queryParams.put("avg_wind_meth", iData.getDataMethods().getAvgWindQc());
        queryParams.put("min_sun_meth", iData.getDataMethods().getMinSunQc());
        queryParams.put("poss_sun_meth", iData.getDataMethods().getPossSunQc());
        queryParams.put("sky_cover_meth",
                iData.getDataMethods().getSkyCoverQc());
        queryParams.put("wx_meth", iData.getDataMethods().getWeatherQc());
        queryParams.put("station_id", iStationID);
        queryParams.put("date", iDate.getCalendarFromClimateDate());

        if ((results != null) && (results.length >= 1)) {
            // data exists for station at given date, update
            StringBuilder update = new StringBuilder("UPDATE ");
            update.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            // TODO handle time without time zone
            // set
            update.append(" SET max_temp=:max_temp");
            update.append(", max_temp_time=");
            update.append(iData.getMaxTempTime().isMissingHourMin() ? "null"
                    : "'" + iData.getMaxTempTime().toHourMinString() + "'");
            update.append(", min_temp=:min_temp");
            update.append(", min_temp_time=");
            update.append(iData.getMinTempTime().isMissingHourMin() ? "null"
                    : "'" + iData.getMinTempTime().toHourMinString() + "'");
            update.append(", precip=:precip");
            update.append(", snow=:snow");
            update.append(", snow_ground=:snow_ground");
            update.append(", heat=:heat");
            update.append(", cool=:cool");
            update.append(", max_wind_dir=:max_wind_dir");
            update.append(", max_wind_spd=:max_wind_spd");
            update.append(", max_wind_time=");
            update.append(iData.getMaxWindTime().isMissingHourMin() ? "null"
                    : "'" + iData.getMaxWindTime().toHourMinString() + "'");
            update.append(", max_gust_dir=:max_gust_dir");
            update.append(", max_gust_spd=:max_gust_spd");
            update.append(", max_gust_time=");
            update.append(iData.getMaxGustTime().isMissingHourMin() ? "null"
                    : "'" + iData.getMaxGustTime().toHourMinString() + "'");
            update.append(", result_wind_dir=:result_wind_dir");
            update.append(", result_wind_spd=:result_wind_spd");
            update.append(", result_x=:result_x");
            update.append(", result_y=:result_y");
            update.append(", num_wind_obs=:num_wind_obs");
            update.append(", avg_wind_speed=:avg_wind_speed");
            update.append(", min_sun=:min_sun");
            update.append(", percent_pos_sun=:percent_pos_sun");
            update.append(", avg_sky_cover=:avg_sky_cover");
            update.append(", max_rh=:max_rh");
            update.append(", max_rh_hour=:max_rh_hour");
            update.append(", min_rh=:min_rh");
            update.append(", min_rh_hour=:min_rh_hour");
            update.append(", max_slp=:max_slp");
            update.append(", min_slp=:min_slp");
            update.append(", wx_1=:wx_1");
            update.append(", wx_2=:wx_2");
            update.append(", wx_3=:wx_3");
            update.append(", wx_4=:wx_4");
            update.append(", wx_5=:wx_5");
            update.append(", wx_6=:wx_6");
            update.append(", wx_7=:wx_7");
            update.append(", wx_8=:wx_8");
            update.append(", wx_9=:wx_9");
            update.append(", wx_10=:wx_10");
            update.append(", wx_11=:wx_11");
            update.append(", wx_12=:wx_12");
            update.append(", wx_13=:wx_13");
            update.append(", wx_14=:wx_14");
            update.append(", wx_15=:wx_15");
            update.append(", wx_16=:wx_16");
            update.append(", wx_17=:wx_17");
            update.append(", wx_18=:wx_18");
            update.append(", max_temp_meth=:max_temp_meth");
            update.append(", min_temp_meth=:min_temp_meth");
            update.append(", precip_meth=:precip_meth");
            update.append(", snow_meth=:snow_meth");
            update.append(", ground_meth=:ground_meth");
            update.append(", max_wind_meth=:max_wind_meth");
            update.append(", max_gust_meth=:max_gust_meth");
            update.append(", avg_wind_meth=:avg_wind_meth");
            update.append(", min_sun_meth=:min_sun_meth");
            update.append(", poss_sun_meth=:poss_sun_meth");
            update.append(", sky_cover_meth=:sky_cover_meth");
            update.append(", wx_meth=:wx_meth");
            // where
            update.append(" WHERE station_id=:station_id");
            update.append(" AND date=:date");

            try {
                getDao().executeSQLUpdate(update.toString(), queryParams);
            } catch (Exception e) {
                throw new Exception(
                        "Error updating the climate database with query: ["
                                + update + "] and map: [" + queryParams + "]",
                        e);
            }
        } else {
            // data does not exist for station at given date, insert
            StringBuilder insert = new StringBuilder("INSERT INTO ");
            insert.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            // columns
            // station ID, date
            insert.append(" (station_id, date,");
            // max temp
            insert.append(" max_temp,");
            // max temp time
            insert.append(" max_temp_time,");
            // min temp
            insert.append("min_temp,");
            // min temp time
            insert.append("min_temp_time,");
            // precip, snow
            insert.append("precip, snow,");
            // snow ground, heat
            insert.append("snow_ground, heat,");
            // cool, max wind dir
            insert.append("cool, max_wind_dir,");
            // max wind spd
            insert.append("max_wind_spd,");
            // max wind time
            insert.append("max_wind_time,");
            // max gust dir
            insert.append("max_gust_dir,");
            // max gust spd
            insert.append("max_gust_spd,");
            // max gust time
            insert.append("max_gust_time,");
            // result wind dir
            insert.append("result_wind_dir,");
            // result wind spd
            insert.append("result_wind_spd,");
            // result x and y
            insert.append("result_x, result_y,");
            // number of wind observations
            insert.append("num_wind_obs,");
            // avg wind, minutes sun
            insert.append("avg_wind_speed, min_sun,");
            // percent of possible sun
            insert.append("percent_pos_sun,");
            // avg sky cover, max humid
            insert.append("avg_sky_cover, max_rh,");
            // max rh hour
            insert.append("max_rh_hour, min_rh,");
            // min rh hour, max slp
            insert.append("min_rh_hour, max_slp,");
            // min slp
            insert.append("min_slp,");
            // wx's
            insert.append(
                    "wx_1, wx_2, wx_3, wx_4, wx_5, wx_6, wx_7, wx_8, wx_9,");
            insert.append("wx_10, wx_11, wx_12, wx_13, wx_14, wx_15, wx_16,");
            insert.append("wx_17, wx_18,");
            insert.append(
                    " max_temp_meth, min_temp_meth, precip_meth, snow_meth, ground_meth,");
            insert.append(
                    " max_wind_meth, max_gust_meth, avg_wind_meth, min_sun_meth,");
            insert.append(" poss_sun_meth, sky_cover_meth, wx_meth");
            // end columns
            insert.append(")");
            // values
            insert.append(" VALUES (");
            // station ID, date
            insert.append(":station_id,:date,");
            // max temp
            insert.append(":max_temp,");
            // max temp time
            insert.append(iData.getMaxTempTime().isMissingHourMin() ? "null,"
                    : "'" + iData.getMaxTempTime().toHourMinString() + "',");
            // min temp
            insert.append(":min_temp,");
            // min temp time
            insert.append(iData.getMinTempTime().isMissingHourMin() ? "null,"
                    : "'" + iData.getMinTempTime().toHourMinString() + "',");
            // precip, snow
            insert.append(":precip,").append(":snow,");
            // snow ground, heat
            insert.append(":snow_ground,").append(":heat,");
            // cool, max wind dir
            insert.append(":cool,").append(":max_wind_dir,");
            // max wind spd
            insert.append(":max_wind_spd,");
            // max wind time
            insert.append(iData.getMaxWindTime().isMissingHourMin() ? "null,"
                    : "'" + iData.getMaxWindTime().toHourMinString() + "',");
            // max gust dir
            insert.append(":max_gust_dir,");
            // max gust spd
            insert.append(":max_gust_spd,");
            // max gust time
            insert.append(iData.getMaxGustTime().isMissingHourMin() ? "null,"
                    : "'" + iData.getMaxGustTime().toHourMinString() + "',");
            // result wind dir
            insert.append(":result_wind_dir,");
            // result wind spd
            insert.append(":result_wind_spd,");
            // result x and y
            insert.append(":result_x, :result_y,");
            // number of wind obs
            insert.append(":num_wind_obs,");
            // avg wind spd, minutes sun
            insert.append(":avg_wind_speed, :min_sun,");
            // percent of possible sun
            insert.append(":percent_pos_sun,");
            // avg sky cover, max humid
            insert.append(":avg_sky_cover,:max_rh,");
            // max hour humid, min humid
            insert.append(":max_rh_hour,:min_rh,");
            // min hour humid, max slp
            insert.append(":min_rh_hour,:max_slp,");
            // min slp
            insert.append(":min_slp,");
            // wx's
            insert.append(
                    ":wx_1, :wx_2, :wx_3, :wx_4, :wx_5, :wx_6, :wx_7, :wx_8, :wx_9,");
            insert.append(
                    ":wx_10, :wx_11, :wx_12, :wx_13, :wx_14, :wx_15, :wx_16,");
            insert.append(":wx_17, :wx_18,");
            // data methods
            insert.append(
                    " :max_temp_meth, :min_temp_meth, :precip_meth, :snow_meth, :ground_meth,");
            insert.append(
                    " :max_wind_meth, :max_gust_meth, :avg_wind_meth, :min_sun_meth,");
            insert.append(" :poss_sun_meth, :sky_cover_meth, :wx_meth");
            // end values
            insert.append(")");

            try {
                getDao().executeSQLUpdate(insert.toString(), queryParams);
            } catch (Exception e) {
                throw new Exception(
                        "Error inserting into the climate database with query: ["
                                + insert + "] and map: [" + queryParams + "]",
                        e);
            }
        }
        return true;
    }

    /**
     * Update daily data for the given station with the given max temp value and
     * dates.
     * 
     * @param iStationID
     * @param iMaxTemp
     * @param iDates
     * @return true if updated, false otherwise.
     * @throws ClimateQueryException
     */
    public boolean updateDailyDataForMaxTemp(int iStationID, int iMaxTemp,
            List<ClimateDate> iDates) throws ClimateQueryException {
        // first see if data exists for station at date
        for (ClimateDate date : iDates) {
            if (!date.isPartialMissing()) {
                Object[] results = queryDailyDataForStationAndDate(iStationID,
                        date);
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("value", iMaxTemp);
                queryParams.put("stationID", iStationID);
                queryParams.put("date", date.getCalendarFromClimateDate());

                if ((results != null) && (results.length >= 1)) {
                    // Task 29500: should max temp time be set?
                    // data exists for station at given date, update
                    StringBuilder update = new StringBuilder("UPDATE ");
                    update.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                    // set
                    update.append(" SET max_temp=:value");
                    // where
                    update.append(" WHERE station_id=:stationID");
                    update.append(" AND date=:date");

                    try {
                        getDao().executeSQLUpdate(update.toString(),
                                queryParams);
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying the climate database with query: ["
                                        + update + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                } else {
                    // Task 29500: should max temp time be set?
                    // data does not exist for station at given date, insert
                    StringBuilder insert = new StringBuilder("INSERT INTO ");
                    insert.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                    // columns
                    // station ID, date
                    insert.append(" (station_id, date,");
                    // max temp
                    insert.append(" max_temp");
                    // end columns
                    insert.append(")");
                    // values
                    insert.append(" VALUES (");
                    // station ID, date
                    insert.append(":stationID,:date,");
                    // max temp
                    insert.append(":value");
                    // end values
                    insert.append(")");

                    try {
                        getDao().executeSQLUpdate(insert.toString(),
                                queryParams);
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying the climate database with query: ["
                                        + insert + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Update daily data for the given station with the given min temp value and
     * dates.
     * 
     * @param iStationID
     * @param iMinTemp
     * @param iDates
     * @return true if updated, false otherwise.
     * @throws ClimateQueryException
     */
    public boolean updateDailyDataForMinTemp(int iStationID, int iMinTemp,
            List<ClimateDate> iDates) throws ClimateQueryException {
        // first see if data exists for station at date
        for (ClimateDate date : iDates) {
            if (!date.isPartialMissing()) {
                Object[] results = queryDailyDataForStationAndDate(iStationID,
                        date);

                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("value", iMinTemp);
                queryParams.put("stationID", iStationID);
                queryParams.put("date", date.getCalendarFromClimateDate());

                if ((results != null) && (results.length >= 1)) {
                    // Task 29500: should min temp time be set?
                    // data exists for station at given date, update
                    StringBuilder update = new StringBuilder("UPDATE ");
                    update.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                    // set
                    update.append(" SET min_temp=:value");
                    // where
                    update.append(" WHERE station_id=:stationID")
                            .append(" AND date=:date");

                    try {
                        getDao().executeSQLUpdate(update.toString(),
                                queryParams);
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying the climate database with query: ["
                                        + update + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                } else {
                    // Task 29500: should min temp time be set?
                    // data does not exist for station at given date, insert
                    StringBuilder insert = new StringBuilder("INSERT INTO ");
                    insert.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                    // columns
                    // station ID, date
                    insert.append(" (station_id, date,");
                    // min temp
                    insert.append(" min_temp");
                    // end columns
                    insert.append(")");
                    // values
                    insert.append(" VALUES (");
                    // station ID, date
                    insert.append(":stationID,:date,");
                    // min temp
                    insert.append(":value");
                    // end values
                    insert.append(")");

                    try {
                        getDao().executeSQLUpdate(insert.toString(),
                                queryParams);
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying the climate database with query: ["
                                        + insert + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Update daily data for the given station with the given max snow value and
     * dates.
     * 
     * @param iStationID
     * @param iMaxSnow
     * @param iDates
     * @return true if updated, false otherwise.
     * @throws ClimateQueryException
     */
    public boolean updateDailyDataForMaxSnow(int iStationID, int iMaxSnow,
            List<ClimateDate> iDates) throws ClimateQueryException {
        // first see if data exists for station at date
        for (ClimateDate date : iDates) {
            if (!date.isPartialMissing()) {
                Object[] results = queryDailyDataForStationAndDate(iStationID,
                        date);
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("value", iMaxSnow);
                queryParams.put("stationID", iStationID);
                queryParams.put("date", date.getCalendarFromClimateDate());

                if ((results != null) && (results.length >= 1)) {
                    // data exists for station at given date, update
                    StringBuilder update = new StringBuilder("UPDATE ");
                    update.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                    // set
                    update.append(" SET snow_ground=:value");
                    // where
                    update.append(" WHERE station_id=:stationID")
                            .append(" AND date=:date");

                    try {
                        getDao().executeSQLUpdate(update.toString(),
                                queryParams);
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying the climate database with query: ["
                                        + update + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                } else {
                    // data does not exist for station at given date, insert
                    StringBuilder insert = new StringBuilder("INSERT INTO ");
                    insert.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                    // columns
                    // station ID, date
                    insert.append(" (station_id, date,");
                    // snow on ground
                    insert.append(" snow_ground");
                    // end columns
                    insert.append(")");
                    // values
                    insert.append(" VALUES (");
                    // station ID, date
                    insert.append(":stationID,:date,");
                    // snow on ground
                    insert.append(":value");
                    // end values
                    insert.append(")");
                    try {
                        getDao().executeSQLUpdate(insert.toString(),
                                queryParams);
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Error querying the climate database with query: ["
                                        + insert + "] and map: [" + queryParams
                                        + "]",
                                e);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Migrated from build_p_resultant_wind.ec
     * 
     * <pre>
     *   December 1999     David T. Miller                      PRC/TDL
     *
     *   Purpose:  This routine retrieves resultant wind x, y, and number of observations
     *   from the daily_climate table and builds the monthly, seasonal, or yearly
     *   resultant wind direction and speed for the period.
     *
     *        SEE TSP-88-07-R1, 1 Feb 93, p.A3-1 for a definition
     *        of the resultant wind direction and speed
     * 
     *         Also see build_resultant_wind.f
     *
     * </pre>
     * 
     * Get resultant wind for the given date range and set in the given
     * {@link PeriodData} object, which should contain the station ID we are
     * interested in.
     * 
     * @param beginDate
     * @param endDate
     * @param periodData
     * @param type
     * @throws ClimateQueryException
     */
    public void buildPResultantWind(ClimateDate beginDate, ClimateDate endDate,
            PeriodData periodData, PeriodType type)
            throws ClimateQueryException {

        int stationID = periodData.getInformId();

        /****************************************************************
         * Legacy documentation:
         * 
         * Now calculate the resultant wind direction and speed from the
         * accumulated sums. Don't forget to divide the wind direction by 10
         * since wind direction is carried along in units of 10's of degrees!
         * Also don't forget to round to the nearest integer since wind
         * direction and speed are both integers! Add a small amount to x and y
         * to avoid division by 0 problems on calm days.
         * 
         ********************************************************************/

        double sumX = buildElement(beginDate, endDate, stationID, type,
                "result_x", "result_x", BuildElementType.SUM).doubleValue();

        double sumY = buildElement(beginDate, endDate, stationID, type,
                "result_y", "result_y", BuildElementType.SUM).doubleValue();

        int numObsHours = buildElement(beginDate, endDate, stationID, type,
                "num_wind_obs", "num_wind_obs", BuildElementType.SUM)
                        .intValue();

        /*
         * Legacy documentation:
         * 
         * Only calculate the resultant and average wind if the number of hours
         * is less than zero. Set to missing if there was no valid wind data
         * (i.e., num_hours= 0)
         * 
         * Migration correction: To simplify and correct what is stated- only do
         * work if number of hours is >0 and not missing. Also should ensure
         * that neither sum is missing.
         */
        periodData.setResultWind(
                ClimateUtilities.buildResultantWind(sumX, sumY, numObsHours));

    }

    /**
     * Either Daily execution is running automatically, or it is manual and user
     * has finished working with the Display module. Do final common
     * calculations before moving on to the next step. Logic from
     * c_display_climate.c.
     * 
     * @param periodType
     *            period type.
     * @param date
     *            date for report.
     * @param dataMap
     *            mapping of data for processing.
     * @throws ClimateSessionException
     *             on exception processing
     */
    public void processDisplayFinalization(PeriodType periodType,
            ClimateDate date, HashMap<Integer, ClimateDailyReportData> dataMap)
            throws ClimateSessionException {
        // climate norm DAO for updating norm records
        ClimateDailyNormDAO climateNormDAO = new ClimateDailyNormDAO();

        for (ClimateDailyReportData reportData : dataMap.values()) {
            DailyClimateData data = reportData.getData();
            ClimateRecordDay yClimate = reportData.getyClimate();
            /*
             * Calculate the derived and cumulative fields now that the user has
             * updated the climatology from yesterday. (build_derived_fields)
             */
            try {
                ClimateDAOUtils.buildDerivedData(date, data.getInformId(), data,
                        yClimate);
            } catch (ClimateQueryException e) {
                throw new ClimateSessionException(
                        "Error building derived data for date ["
                                + date.toFullDateString() + "] and station ID ["
                                + data.getInformId() + "]",
                        e);
            }

            if (PeriodType.MORN_NWWS.equals(periodType)
                    || PeriodType.MORN_RAD.equals(periodType)) {

                /*
                 * Legacy: Now update the data bases and write the various
                 * output files, for morning only (output_daily_climo.f)
                 * 
                 * Migrated: only output to database.
                 */
                try {
                    updateDailyDataForStationAndDate(date, data.getInformId(),
                            data);
                } catch (Exception e) {
                    throw new ClimateSessionException(
                            "Error updating data for date ["
                                    + date.toFullDateString()
                                    + "] and station ID [" + data.getInformId()
                                    + "]",
                            e);
                }

                /*
                 * Check for new or tied records, for morning only
                 * (check_daily_records).
                 */
                try {
                    climateNormDAO.compareUpdateDailyRecords(date,
                            data.getInformId(), (short) data.getMaxTemp(),
                            (short) data.getMinTemp(), data.getPrecip(),
                            data.getSnowDay());
                } catch (ClimateQueryException e) {
                    throw new ClimateSessionException(
                            "Error updating record data for date ["
                                    + date.toFullDateString()
                                    + "] and station ID [" + data.getInformId()
                                    + "]",
                            e);
                }
            }
        }
    }
}

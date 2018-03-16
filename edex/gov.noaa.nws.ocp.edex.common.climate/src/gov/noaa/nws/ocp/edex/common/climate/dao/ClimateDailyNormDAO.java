/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

/**
 * Implementations converted from SUBROUTINES under
 * adapt/climate/lib/src/climate_db_utils
 * 
 * Daily norms.
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
 * 07 JUL 2017  33104      amoore      Split Daily and Period norms into different classes.
 * 08 SEP 2017  37809      amoore      For queries, cast to Number rather than specific number type.
 * 12 OCT 2017  39364      amoore      Wrong inequality for min temp record breaking check.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateDailyNormDAO extends ClimateDAO {
    /**
     * Constructor.
     */
    public ClimateDailyNormDAO() {
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
                        float ecMeanTemp = oa[0] != null
                                ? ((Number) oa[0]).floatValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempMean = oa[1] != null
                                ? ((Number) oa[1]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempMean = oa[2] != null
                                ? ((Number) oa[2]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecord = oa[3] != null
                                ? ((Number) oa[3]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecord = oa[4] != null
                                ? ((Number) oa[4]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecordYear1 = oa[5] != null
                                ? ((Number) oa[5]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecordYear2 = oa[6] != null
                                ? ((Number) oa[6]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMaxTempRecordYear3 = oa[7] != null
                                ? ((Number) oa[7]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecordYear1 = oa[8] != null
                                ? ((Number) oa[8]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecordYear2 = oa[9] != null
                                ? ((Number) oa[9]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecMinTempRecordYear3 = oa[10] != null
                                ? ((Number) oa[10]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        float ecPrecipMean = oa[11] != null
                                ? ((Number) oa[11]).floatValue()
                                : ParameterFormatClimate.MISSING;
                        float ecPrecipDayRecord = oa[12] != null
                                ? ((Number) oa[12]).floatValue()
                                : ParameterFormatClimate.MISSING;
                        short ecPrecipDayRecordYear1 = oa[13] != null
                                ? ((Number) oa[13]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecPrecipDayRecordYear2 = oa[14] != null
                                ? ((Number) oa[14]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecPrecipDayRecordYear3 = oa[15] != null
                                ? ((Number) oa[15]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        float ecSnowDayMean = oa[16] != null
                                ? ((Number) oa[16]).floatValue()
                                : ParameterFormatClimate.MISSING;
                        float ecSnowDayRecord = oa[17] != null
                                ? ((Number) oa[17]).floatValue()
                                : ParameterFormatClimate.MISSING;
                        short ecSnowDayRecordYear1 = oa[18] != null
                                ? ((Number) oa[18]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecSnowDayRecordYear2 = oa[19] != null
                                ? ((Number) oa[19]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        short ecSnowDayRecordYear3 = oa[20] != null
                                ? ((Number) oa[20]).shortValue()
                                : ParameterFormatClimate.MISSING;
                        /* unused */
                        @SuppressWarnings("unused")
                        float ecSnowGroundMean = oa[21] != null
                                ? ((Number) oa[21]).floatValue()
                                : ParameterFormatClimate.MISSING;
                        int ecNumHeatMean = oa[22] != null
                                ? ((Number) oa[22]).intValue()
                                : ParameterFormatClimate.MISSING;
                        int ecNumCoolMean = oa[23] != null
                                ? ((Number) oa[23]).intValue()
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
                            climateRcd.setMeanTemp(
                                    ((Number) rowData[0]).floatValue());
                        }
                        if (rowData[1] != null) {
                            climateRcd.setMaxTempRecord(
                                    ((Number) rowData[1]).shortValue());
                        }
                        if (rowData[2] != null) {
                            climateRcd.setMaxTempMean(
                                    ((Number) rowData[2]).shortValue());
                        }
                        if (rowData[3] != null) {
                            climateRcd.setMinTempRecord(
                                    ((Number) rowData[3]).shortValue());
                        }
                        if (rowData[4] != null) {
                            climateRcd.setMinTempMean(
                                    ((Number) rowData[4]).shortValue());
                        }

                        short[] maxTempYear = climateRcd.getMaxTempYear();
                        if (rowData[5] != null) {
                            maxTempYear[0] = ((Number) rowData[5]).shortValue();
                        }
                        if (rowData[6] != null) {
                            maxTempYear[1] = ((Number) rowData[6]).shortValue();
                        }
                        if (rowData[7] != null) {
                            maxTempYear[2] = ((Number) rowData[7]).shortValue();
                        }

                        short[] minTempYear = climateRcd.getMinTempYear();
                        if (rowData[8] != null) {
                            minTempYear[0] = ((Number) rowData[8]).shortValue();
                        }
                        if (rowData[9] != null) {
                            minTempYear[1] = ((Number) rowData[9]).shortValue();
                        }
                        if (rowData[10] != null) {
                            minTempYear[2] = ((Number) rowData[10])
                                    .shortValue();
                        }

                        if (rowData[11] != null) {
                            climateRcd.setPrecipMean(
                                    ((Number) rowData[11]).floatValue());
                        }
                        if (rowData[12] != null) {
                            climateRcd.setPrecipDayRecord(
                                    ((Number) rowData[12]).floatValue());
                        }

                        short[] precipDayRecordYear = climateRcd
                                .getPrecipDayRecordYear();
                        if (rowData[13] != null) {
                            precipDayRecordYear[0] = ((Number) rowData[13])
                                    .shortValue();
                        }
                        if (rowData[14] != null) {
                            precipDayRecordYear[1] = ((Number) rowData[14])
                                    .shortValue();
                        }
                        if (rowData[15] != null) {
                            precipDayRecordYear[2] = ((Number) rowData[15])
                                    .shortValue();
                        }

                        if (rowData[16] != null) {
                            climateRcd.setSnowDayMean(
                                    ((Number) rowData[16]).floatValue());
                        }
                        if (rowData[17] != null) {
                            climateRcd.setSnowDayRecord(
                                    ((Number) rowData[17]).floatValue());
                        }

                        short[] snowDayRecordYear = climateRcd
                                .getSnowDayRecordYear();
                        if (rowData[18] != null) {
                            snowDayRecordYear[0] = ((Number) rowData[18])
                                    .shortValue();
                        }
                        if (rowData[19] != null) {
                            snowDayRecordYear[1] = ((Number) rowData[19])
                                    .shortValue();
                        }
                        if (rowData[20] != null) {
                            snowDayRecordYear[2] = ((Number) rowData[20])
                                    .shortValue();
                        }

                        if (rowData[21] != null) {
                            climateRcd.setSnowGround(
                                    ((Number) rowData[21]).floatValue());
                        }
                        if (rowData[22] != null) {
                            climateRcd.setNumHeatMean(
                                    ((Number) rowData[22]).intValue());
                        }
                        if (rowData[23] != null) {
                            climateRcd.setNumCoolMean(
                                    ((Number) rowData[23]).intValue());
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

                if (minTemp < dayRecord.getMinTempRecord()) {
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

}
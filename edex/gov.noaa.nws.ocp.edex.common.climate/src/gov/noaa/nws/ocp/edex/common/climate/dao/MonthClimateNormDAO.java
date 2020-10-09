/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * Query the mon_climate_norm table, for basic CRUD rather than complex
 * calculations.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 10/02/2016   20635      wkwock      Initial creation
 * 10/21/2016   20635      wkwock      Add updateClimateMonthNormNoMissing
 * 19 DEC 2016  27015      amoore      Checking against missing date information should
 *                                     require all date data present, not just at least one.
 * 12 JAN 2017  26411      wkwock      Added saveClimateMonthRecord method
 * 16 MAR 2017  30162      amoore      Fix logging.
 * 21 MAR 2017  20632      amoore      Add comment regarding null DB returns.
 * 19 APR 2017  33104      amoore      Use query maps now that DB issue is fixed.
 * 20 APR 2017  30166      amoore      Safer logic to avoid out of bounds.
 * 25 APR 2017  33104      amoore      Logging clean up.
 * 02 MAY 2017  33104      amoore      More query map replacements. Use abstract maps.
 * 08 SEP 2017  37809      amoore      For queries, cast to Number rather than specific number type.
 * 28 APR 2020  21884      smoorthy    Fixed climate snow field update problems
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class MonthClimateNormDAO extends ClimateDAO {
    /**
     * Constructor.
     */
    public MonthClimateNormDAO() {
        super();
    }

    /**
     * Fetch a record from mon_climate_norm table
     * 
     * @param stationId
     * @param monthOfYear
     * @param periodTypeVal
     * @return found {@link PeriodClimo} data from DB, or null if nothing found.
     * @throws ClimateQueryException
     */
    public PeriodClimo fetchClimateMonthRecord(int stationId, int monthOfYear,
            PeriodType periodType) throws ClimateQueryException {
        PeriodClimo climateRcd = null;

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("station_id, month_of_year, period_type, ");
        sql.append("max_temp_mean, max_temp_record, ");
        sql.append("day_max_temp_rec1, day_max_temp_rec2, day_max_temp_rec3, ");
        sql.append("min_temp_mean, min_temp_record, ");
        sql.append("day_min_temp_rec1, day_min_temp_rec2, day_min_temp_rec3, ");
        sql.append("norm_mean_temp, norm_mean_max_temp, norm_mean_min_temp, ");
        sql.append(
                "num_max_ge_90f, num_max_le_32f, num_min_le_32f, num_min_le_0f, ");
        sql.append("precip_pd_mean, precip_pd_max, ");
        sql.append("precip_pd_max_yr1, precip_pd_max_yr2, precip_pd_max_yr3, ");
        sql.append("precip_period_min, ");
        sql.append("precip_pd_min_yr1, precip_pd_min_yr2, precip_pd_min_yr3, ");
        sql.append("precip_day_norm, ");
        sql.append(
                "num_prcp_ge_01, num_prcp_ge_10, num_prcp_ge_50, num_prcp_ge_100, ");
        sql.append("snow_pd_mean, snow_pd_max, ");
        sql.append("snow_pd_max_yr1, snow_pd_max_yr2, snow_pd_max_yr3, ");
        sql.append("snow_24h_begin1, snow_24h_begin2, snow_24h_begin3, ");
        sql.append("snow_max_24h_rec, ");
        sql.append("snow_24h_end1, snow_24h_end2, snow_24h_end3, ");
        sql.append("snow_water_pd_norm, snow_ground_norm, snow_ground_max, ");
        sql.append(
                "day_snow_grnd_max1, day_snow_grnd_max2, day_snow_grnd_max3, ");
        sql.append("num_snow_ge_tr, num_snow_ge_1, ");
        sql.append("heat_pd_mean, cool_pd_mean FROM ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationId");
        sql.append(" AND month_of_year= :monthOfYear");
        sql.append(" AND period_type= :type");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("monthOfYear", monthOfYear);
        paramMap.put("type", periodType.getValue());

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if (results != null && results.length >= 1) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        climateRcd = PeriodClimo.getMissingPeriodClimo();
                        climateRcd.setInformId(stationId);
                        climateRcd.setMonthOfYear(monthOfYear);
                        climateRcd.setPeriodType(periodType);

                        // any of these values could be null
                        Object[] rowData = (Object[]) result;
                        if (rowData[3] != null) {
                            climateRcd.setMaxTempNorm(
                                    ((Number) rowData[3]).floatValue());
                        }
                        if (rowData[4] != null) {
                            climateRcd.setMaxTempRecord(
                                    ((Number) rowData[4]).shortValue());
                        }
                        List<ClimateDate> dayMaxTempRecordList = climateRcd
                                .getDayMaxTempRecordList();
                        if (rowData[5] != null) {
                            dayMaxTempRecordList.set(0,
                                    new ClimateDate((Date) rowData[5]));
                        }
                        if (rowData[6] != null) {
                            dayMaxTempRecordList.set(1,
                                    new ClimateDate((Date) rowData[6]));
                        }
                        if (rowData[7] != null) {
                            dayMaxTempRecordList.set(2,
                                    new ClimateDate((Date) rowData[7]));
                        }

                        if (rowData[8] != null) {
                            climateRcd.setMinTempNorm(
                                    ((Number) rowData[8]).floatValue());
                        }
                        if (rowData[9] != null) {
                            climateRcd.setMinTempRecord(
                                    ((Number) rowData[9]).shortValue());
                        }
                        List<ClimateDate> dayMinTempRecordList = climateRcd
                                .getDayMinTempRecordList();
                        if (rowData[10] != null) {
                            dayMinTempRecordList.set(0,
                                    new ClimateDate((Date) rowData[10]));
                        }
                        if (rowData[11] != null) {
                            dayMinTempRecordList.set(1,
                                    new ClimateDate((Date) rowData[11]));
                        }
                        if (rowData[12] != null) {
                            dayMinTempRecordList.set(2,
                                    new ClimateDate((Date) rowData[12]));
                        }

                        if (rowData[13] != null) {
                            climateRcd.setNormMeanTemp(
                                    ((Number) rowData[13]).floatValue());
                        }
                        if (rowData[14] != null) {
                            climateRcd.setNormMeanMaxTemp(
                                    ((Number) rowData[14]).floatValue());
                        }
                        if (rowData[15] != null) {
                            climateRcd.setNormMeanMinTemp(
                                    ((Number) rowData[15]).floatValue());
                        }
                        if (rowData[16] != null) {
                            climateRcd.setNormNumMaxGE90F(
                                    ((Number) rowData[16]).floatValue());
                        }
                        if (rowData[17] != null) {
                            climateRcd.setNormNumMaxLE32F(
                                    ((Number) rowData[17]).floatValue());
                        }
                        if (rowData[18] != null) {
                            climateRcd.setNormNumMinLE32F(
                                    ((Number) rowData[18]).floatValue());
                        }
                        if (rowData[19] != null) {
                            climateRcd.setNormNumMinLE0F(
                                    ((Number) rowData[19]).floatValue());
                        }
                        if (rowData[20] != null) {
                            climateRcd.setPrecipPeriodNorm(
                                    ((Number) rowData[20]).floatValue());
                        }
                        if (rowData[21] != null) {
                            climateRcd.setPrecipPeriodMax(
                                    ((Number) rowData[21]).floatValue());
                        }
                        List<ClimateDate> precipPeriodMaxYearList = climateRcd
                                .getPrecipPeriodMaxYearList();
                        if (rowData[22] != null) {
                            precipPeriodMaxYearList.set(0, new ClimateDate(1, 1,
                                    ((Number) rowData[22]).intValue()));
                        }
                        if (rowData[23] != null) {
                            precipPeriodMaxYearList.set(1, new ClimateDate(1, 1,
                                    ((Number) rowData[23]).intValue()));
                        }
                        if (rowData[24] != null) {
                            precipPeriodMaxYearList.set(2, new ClimateDate(1, 1,
                                    ((Number) rowData[24]).intValue()));
                        }

                        if (rowData[25] != null) {
                            climateRcd.setPrecipPeriodMin(
                                    ((Number) rowData[25]).floatValue());
                        }

                        List<ClimateDate> precipPeriodMinYearList = climateRcd
                                .getPrecipPeriodMinYearList();
                        if (rowData[26] != null) {
                            precipPeriodMinYearList.set(0, new ClimateDate(1, 1,
                                    ((Number) rowData[26]).intValue()));
                        }
                        if (rowData[27] != null) {
                            precipPeriodMinYearList.set(1, new ClimateDate(1, 1,
                                    ((Number) rowData[27]).intValue()));
                        }
                        if (rowData[28] != null) {
                            precipPeriodMinYearList.set(2, new ClimateDate(1, 1,
                                    ((Number) rowData[28]).intValue()));
                        }

                        if (rowData[29] != null) {
                            climateRcd.setPrecipDayNorm(
                                    ((Number) rowData[29]).floatValue());
                        }
                        if (rowData[30] != null) {
                            climateRcd.setNumPrcpGE01Norm(
                                    ((Number) rowData[30]).floatValue());
                        }
                        if (rowData[31] != null) {
                            climateRcd.setNumPrcpGE10Norm(
                                    ((Number) rowData[31]).floatValue());
                        }
                        if (rowData[32] != null) {
                            climateRcd.setNumPrcpGE50Norm(
                                    ((Number) rowData[32]).floatValue());
                        }
                        if (rowData[33] != null) {
                            climateRcd.setNumPrcpGE100Norm(
                                    ((Number) rowData[33]).floatValue());
                        }

                        if (rowData[34] != null) {
                            climateRcd.setSnowPeriodNorm(
                                    ((Number) rowData[34]).floatValue());
                        }
                        if (rowData[35] != null) {
                            climateRcd.setSnowPeriodRecord(
                                    ((Number) rowData[35]).floatValue());
                        }

                        List<ClimateDate> snowPeriodMaxYearList = climateRcd
                                .getSnowPeriodMaxYearList();
                        if (rowData[36] != null) {
                            snowPeriodMaxYearList.set(0, new ClimateDate(0, 1,
                                    ((Number) rowData[36]).intValue()));
                        }
                        if (rowData[37] != null) {
                            snowPeriodMaxYearList.set(1, new ClimateDate(1, 1,
                                    ((Number) rowData[37]).intValue()));
                        }
                        if (rowData[38] != null) {
                            snowPeriodMaxYearList.set(2, new ClimateDate(1, 1,
                                    ((Number) rowData[38]).intValue()));
                        }

                        ClimateDates snow24hDates = climateRcd.getSnow24HList()
                                .get(0);
                        if (rowData[39] != null) {
                            snow24hDates.setStart(
                                    new ClimateDate((Date) rowData[39]));
                        }
                        if (rowData[43] != null) {
                            snow24hDates.setEnd(
                                    new ClimateDate((Date) rowData[43]));
                        }

                        snow24hDates = climateRcd.getSnow24HList().get(1);
                        if (rowData[40] != null) {
                            snow24hDates.setStart(
                                    new ClimateDate((Date) rowData[40]));
                        }
                        if (rowData[44] != null) {
                            snow24hDates.setEnd(
                                    new ClimateDate((Date) rowData[44]));
                        }

                        snow24hDates = climateRcd.getSnow24HList().get(2);
                        if (rowData[41] != null) {
                            snow24hDates.setStart(
                                    new ClimateDate((Date) rowData[41]));
                        }
                        if (rowData[45] != null) {
                            snow24hDates.setEnd(
                                    new ClimateDate((Date) rowData[45]));
                        }

                        if (rowData[42] != null) {
                            climateRcd.setSnowMax24HRecord(
                                    ((Number) rowData[42]).floatValue());
                        }

                        if (rowData[46] != null) {
                            climateRcd.setSnowWaterPeriodNorm(
                                    ((Number) rowData[46]).floatValue());
                        }
                        if (rowData[47] != null) {
                            climateRcd.setSnowGroundNorm(
                                    ((Number) rowData[47]).floatValue());
                        }
                        if (rowData[48] != null) {
                            climateRcd.setSnowGroundMax(
                                    ((Number) rowData[48]).shortValue());
                        }

                        List<ClimateDate> daySnowGroundMaxList = climateRcd
                                .getDaySnowGroundMaxList();
                        if (rowData[49] != null) {
                            daySnowGroundMaxList.set(0,
                                    new ClimateDate((Date) rowData[49]));
                        }
                        if (rowData[50] != null) {
                            daySnowGroundMaxList.set(1,
                                    new ClimateDate((Date) rowData[50]));
                        }
                        if (rowData[51] != null) {
                            daySnowGroundMaxList.set(2,
                                    new ClimateDate((Date) rowData[51]));
                        }

                        if (rowData[52] != null) {
                            climateRcd.setNumSnowGETRNorm(
                                    ((Number) rowData[52]).floatValue());
                        }
                        if (rowData[53] != null) {
                            climateRcd.setNumSnowGE1Norm(
                                    ((Number) rowData[53]).floatValue());
                        }
                        if (rowData[54] != null) {
                            climateRcd.setNumHeatPeriodNorm(
                                    ((Number) rowData[54]).intValue());
                        }
                        if (rowData[55] != null) {
                            climateRcd.setNumCoolPeriodNorm(
                                    ((Number) rowData[55]).intValue());
                        }
                    } catch (Exception e) {
                        throw new ClimateQueryException(
                                "Unexpected return column type from query.", e);
                    }
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }
            } else {
                logger.warn("No monthly climate data returned from query: ["
                        + sql + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying for monthly climate data with query: ["
                            + sql + "] and map: [" + paramMap + "]",
                    e);
        }

        return climateRcd;
    }

    /**
     * Update a record in table mon_climate_norm using exactly the given data,
     * including missing data. Assumed record already exists.
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean updateClimateMonthRecord(PeriodClimo record)
            throws ClimateQueryException {
        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(" SET max_temp_mean=:max_temp_mean");
        queryParams.put("max_temp_mean", record.getMaxTempNorm());
        sql.append(", max_temp_record=:max_temp_record");
        queryParams.put("max_temp_record", record.getMaxTempRecord());

        List<ClimateDate> dateRcds = record.getDayMaxTempRecordList();
        if (dateRcds.isEmpty() || dateRcds.get(0).isPartialMissing()) {
            sql.append(", day_max_temp_rec1=null");
        } else {
            sql.append(", day_max_temp_rec1=:day_max_temp_rec1");
            queryParams.put("day_max_temp_rec1",
                    dateRcds.get(0).getCalendarFromClimateDate());
        }
        if (dateRcds.size() < 2 || dateRcds.get(1).isPartialMissing()) {
            sql.append(", day_max_temp_rec2=null");
        } else {
            sql.append(", day_max_temp_rec2=:day_max_temp_rec2");
            queryParams.put("day_max_temp_rec2",
                    dateRcds.get(1).getCalendarFromClimateDate());
        }
        if (dateRcds.size() < 3 || dateRcds.get(2).isPartialMissing()) {
            sql.append(", day_max_temp_rec3=null");
        } else {
            sql.append(", day_max_temp_rec3=:day_max_temp_rec3");
            queryParams.put("day_max_temp_rec3",
                    dateRcds.get(2).getCalendarFromClimateDate());
        }

        sql.append(", min_temp_mean=:min_temp_mean");
        queryParams.put("min_temp_mean", record.getMinTempNorm());
        sql.append(", min_temp_record=:min_temp_record");
        queryParams.put("min_temp_record", record.getMinTempRecord());

        dateRcds = record.getDayMinTempRecordList();
        if (dateRcds.isEmpty() || dateRcds.get(0).isPartialMissing()) {
            sql.append(", day_min_temp_rec1=null");
        } else {
            sql.append(", day_min_temp_rec1=:day_min_temp_rec1");
            queryParams.put("day_min_temp_rec1",
                    dateRcds.get(0).getCalendarFromClimateDate());
        }
        if (dateRcds.size() < 2 || dateRcds.get(1).isPartialMissing()) {
            sql.append(", day_min_temp_rec2=null");
        } else {
            sql.append(", day_min_temp_rec2=:day_min_temp_rec2");
            queryParams.put("day_min_temp_rec2",
                    dateRcds.get(1).getCalendarFromClimateDate());
        }
        if (dateRcds.size() < 3 || dateRcds.get(2).isPartialMissing()) {
            sql.append(", day_min_temp_rec3=null");
        } else {
            sql.append(", day_min_temp_rec3=:day_min_temp_rec3");
            queryParams.put("day_min_temp_rec3",
                    dateRcds.get(2).getCalendarFromClimateDate());
        }

        sql.append(", norm_mean_temp=:norm_mean_temp");
        queryParams.put("norm_mean_temp", record.getNormMeanTemp());
        sql.append(", norm_mean_max_temp=:norm_mean_max_temp");
        queryParams.put("norm_mean_max_temp", record.getNormMeanMaxTemp());
        sql.append(", norm_mean_min_temp=:norm_mean_min_temp");
        queryParams.put("norm_mean_min_temp", record.getNormMeanMinTemp());
        sql.append(", num_max_ge_90f=:num_max_ge_90f");
        queryParams.put("num_max_ge_90f", record.getNormNumMaxGE90F());
        sql.append(", num_max_le_32f=:num_max_le_32f");
        queryParams.put("num_max_le_32f", record.getNormNumMaxLE32F());
        sql.append(", num_min_le_32f=:num_min_le_32f");
        queryParams.put("num_min_le_32f", record.getNormNumMinLE32F());
        sql.append(", num_min_le_0f=:num_min_le_0f");
        queryParams.put("num_min_le_0f", record.getNormNumMinLE0F());
        sql.append(", precip_pd_mean=:precip_pd_mean");
        queryParams.put("precip_pd_mean", record.getPrecipPeriodNorm());
        sql.append(", precip_pd_max=:precip_pd_max");
        queryParams.put("precip_pd_max", record.getPrecipPeriodMax());

        dateRcds = record.getPrecipPeriodMaxYearList();
        if (dateRcds.isEmpty() || dateRcds.get(0)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", precip_pd_max_yr1=null");
        } else {
            sql.append(", precip_pd_max_yr1=:precip_pd_max_yr1");
            queryParams.put("precip_pd_max_yr1", dateRcds.get(0).getYear());
        }
        if (dateRcds.size() < 2 || dateRcds.get(1)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", precip_pd_max_yr2=null");
        } else {
            sql.append(", precip_pd_max_yr2=:precip_pd_max_yr2");
            queryParams.put("precip_pd_max_yr2", dateRcds.get(1).getYear());
        }
        if (dateRcds.size() < 3 || dateRcds.get(2)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", precip_pd_max_yr3=null");
        } else {
            sql.append(", precip_pd_max_yr3=:precip_pd_max_yr3");
            queryParams.put("precip_pd_max_yr3", dateRcds.get(2).getYear());
        }

        sql.append(", precip_period_min=:precip_period_min");
        queryParams.put("precip_period_min", record.getPrecipPeriodMin());

        dateRcds = record.getPrecipPeriodMinYearList();
        if (dateRcds.isEmpty() || dateRcds.get(0)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", precip_pd_min_yr1=null");
        } else {
            sql.append(", precip_pd_min_yr1=:precip_pd_min_yr1");
            queryParams.put("precip_pd_min_yr1", dateRcds.get(0).getYear());
        }
        if (dateRcds.size() < 2 || dateRcds.get(1)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", precip_pd_min_yr2=null");
        } else {
            sql.append(", precip_pd_min_yr2=:precip_pd_min_yr2");
            queryParams.put("precip_pd_min_yr2", dateRcds.get(1).getYear());
        }
        if (dateRcds.size() < 3 || dateRcds.get(2)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", precip_pd_min_yr3=null");
        } else {
            sql.append(", precip_pd_min_yr3=:precip_pd_min_yr3");
            queryParams.put("precip_pd_min_yr3", dateRcds.get(2).getYear());
        }

        sql.append(", precip_day_norm=:precip_day_norm");
        queryParams.put("precip_day_norm", record.getPrecipDayNorm());
        sql.append(", num_prcp_ge_01=:num_prcp_ge_01");
        queryParams.put("num_prcp_ge_01", record.getNumPrcpGE01Norm());
        sql.append(", num_prcp_ge_10=:num_prcp_ge_10");
        queryParams.put("num_prcp_ge_10", record.getNumPrcpGE10Norm());
        sql.append(", num_prcp_ge_50=:num_prcp_ge_50");
        queryParams.put("num_prcp_ge_50", record.getNumPrcpGE50Norm());
        sql.append(", num_prcp_ge_100=:num_prcp_ge_100");
        queryParams.put("num_prcp_ge_100", record.getNumPrcpGE100Norm());
        sql.append(", snow_pd_mean=:snow_pd_mean");
        queryParams.put("snow_pd_mean", record.getSnowPeriodNorm());
        sql.append(", snow_pd_max=:snow_pd_max");
        queryParams.put("snow_pd_max", record.getSnowPeriodRecord());

        dateRcds = record.getSnowPeriodMaxYearList();
        
        if (dateRcds.isEmpty() || dateRcds.get(0)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", snow_pd_max_yr1=null");
        } else {
            sql.append(", snow_pd_max_yr1=:snow_pd_max_yr1");
            queryParams.put("snow_pd_max_yr1", dateRcds.get(0).getYear());
        }
        if (dateRcds.size() < 2 || dateRcds.get(1)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", snow_pd_max_yr2=null");
        } else {
            sql.append(", snow_pd_max_yr2=:snow_pd_max_yr2");
            queryParams.put("snow_pd_max_yr2", dateRcds.get(1).getYear());
        }
        if (dateRcds.size() < 3 || dateRcds.get(2)
                .getYear() == ParameterFormatClimate.MISSING) {
            sql.append(", snow_pd_max_yr3=null");
        } else {
            sql.append(", snow_pd_max_yr3=:snow_pd_max_yr3");
            queryParams.put("snow_pd_max_yr3", dateRcds.get(2).getYear());
        }
            
        List<ClimateDates> datesRcd = record.getSnow24HList();
        if (datesRcd.isEmpty() || datesRcd.get(0).getStart() == null
                || datesRcd.get(0).getStart().isPartialMissing()) {
            sql.append(", snow_24h_begin1=null");
        } else {
            sql.append(", snow_24h_begin1=:snow_24h_begin1");
            queryParams.put("snow_24h_begin1",
                    datesRcd.get(0).getStart().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 2 || datesRcd.get(1).getStart() == null
                || datesRcd.get(1).getStart().isPartialMissing()) {
            sql.append(", snow_24h_begin2=null");
        } else {
            sql.append(", snow_24h_begin2=:snow_24h_begin2");
            queryParams.put("snow_24h_begin2",
                    datesRcd.get(1).getStart().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 3 || datesRcd.get(2).getStart() == null
                || datesRcd.get(2).getStart().isPartialMissing()) {
            sql.append(", snow_24h_begin3=null");
        } else {
            sql.append(", snow_24h_begin3=:snow_24h_begin3");
            queryParams.put("snow_24h_begin3",
                    datesRcd.get(2).getStart().getCalendarFromClimateDate());
        }

        if (datesRcd.isEmpty() || datesRcd.get(0).getEnd() == null
                || datesRcd.get(0).getEnd().isPartialMissing()) {
            sql.append(", snow_24h_end1=null");
        } else {
            sql.append(", snow_24h_end1=:snow_24h_end1");
            queryParams.put("snow_24h_end1",
                    datesRcd.get(0).getEnd().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 2 || datesRcd.get(1).getEnd() == null
                || datesRcd.get(1).getEnd().isPartialMissing()) {
            sql.append(", snow_24h_end2=null");
        } else {
            sql.append(", snow_24h_end2=:snow_24h_end2");
            queryParams.put("snow_24h_end2",
                    datesRcd.get(1).getEnd().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 3 || datesRcd.get(2).getEnd() == null
                || datesRcd.get(2).getEnd().isPartialMissing()) {
            sql.append(", snow_24h_end3=null");
        } else {
            sql.append(", snow_24h_end3=:snow_24h_end3");
            queryParams.put("snow_24h_end3",
                    datesRcd.get(2).getEnd().getCalendarFromClimateDate());
        }
        
        
        sql.append(", snow_max_24h_rec=:snow_max_24h_rec");
        queryParams.put("snow_max_24h_rec", record.getSnowMax24HRecord());
        
        sql.append(", snow_water_pd_norm=:snow_water_pd_norm");
        queryParams.put("snow_water_pd_norm", record.getSnowWaterPeriodNorm());
        sql.append(", snow_ground_norm=:snow_ground_norm");
        queryParams.put("snow_ground_norm", record.getSnowGroundNorm());
        sql.append(", snow_ground_max=:snow_ground_max");
        queryParams.put("snow_ground_max", record.getSnowGroundMax());

        
        dateRcds = record.getDaySnowGroundMaxList();
        if (dateRcds.isEmpty() || dateRcds.get(0).isPartialMissing()) {
            sql.append(", day_snow_grnd_max1=null");
        } else {
            sql.append(", day_snow_grnd_max1=:day_snow_grnd_max1");
            queryParams.put("day_snow_grnd_max1",
                    dateRcds.get(0).getCalendarFromClimateDate());
        }

        if (dateRcds.size() < 2 || dateRcds.get(1).isPartialMissing()) {
            sql.append(", day_snow_grnd_max2=null");
        } else {
            sql.append(", day_snow_grnd_max2=:day_snow_grnd_max2");
            queryParams.put("day_snow_grnd_max2",
                    dateRcds.get(1).getCalendarFromClimateDate());
        }

        if (dateRcds.size() < 3 || dateRcds.get(2).isPartialMissing()) {
            sql.append(", day_snow_grnd_max3=null");
        } else {
            sql.append(", day_snow_grnd_max3=:day_snow_grnd_max3");
            queryParams.put("day_snow_grnd_max3",
                    dateRcds.get(2).getCalendarFromClimateDate());
        }

        sql.append(", num_snow_ge_tr=:num_snow_ge_tr");
        queryParams.put("num_snow_ge_tr", record.getNumSnowGETRNorm());
        sql.append(", num_snow_ge_1=:num_snow_ge_1");
        queryParams.put("num_snow_ge_1", record.getNumSnowGE1Norm());
        sql.append(", heat_pd_mean=:heat_pd_mean");
        queryParams.put("heat_pd_mean", record.getNumHeatPeriodNorm());
        sql.append(", cool_pd_mean=:cool_pd_mean");
        queryParams.put("cool_pd_mean", record.getNumCoolPeriodNorm());
        sql.append(" WHERE station_id=:station_id");
        queryParams.put("station_id", record.getInformId());
        sql.append(" AND month_of_year=:month_of_year");
        queryParams.put("month_of_year", record.getMonthOfYear());
        sql.append(" AND period_type=:period_type");
        queryParams.put("period_type", record.getPeriodType().getValue());

        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), queryParams);
            return (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to update table "
                    + ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME
                    + ". Query: [" + sql + "] and map: [" + queryParams + "]",
                    e);
        }
    }

    /**
     * If record doesn't exist in table mon_climate_norm, insert it else update
     * with only the non-missing values from given data.
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean updateClimateMonthNormNoMissing(PeriodClimo record)
            throws ClimateQueryException {
        if (fetchClimateMonthRecord(record.getInformId(),
                record.getMonthOfYear(), record.getPeriodType()) == null) {
            return insertClimateMonthRecord(record);
        }

        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder setClause = new StringBuilder(" SET ");

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempNorm(), ParameterFormatClimate.MISSING,
                "max_temp_mean", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMaxTempRecord(), ParameterFormatClimate.MISSING,
                "max_temp_record", queryParams));

        List<ClimateDate> dateRcds = record.getDayMaxTempRecordList();
        switch (dateRcds.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(2), "day_max_temp_rec3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(1), "day_max_temp_rec2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(0), "day_max_temp_rec1", queryParams));
            break;
        case 0:
            logger.debug("No max temp day records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempNorm(), ParameterFormatClimate.MISSING,
                "min_temp_mean", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getMinTempRecord(), ParameterFormatClimate.MISSING,
                "min_temp_record", queryParams));

        dateRcds = record.getDayMinTempRecordList();
        switch (dateRcds.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(2), "day_min_temp_rec3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(1), "day_min_temp_rec2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(0), "day_min_temp_rec1", queryParams));
            break;
        case 0:
            logger.debug("No min temp day records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormMeanTemp(), ParameterFormatClimate.MISSING,
                "norm_mean_temp", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormMeanMaxTemp(), ParameterFormatClimate.MISSING,
                "norm_mean_max_temp", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormMeanMinTemp(), ParameterFormatClimate.MISSING,
                "norm_mean_min_temp", queryParams));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormNumMaxGE90F(), ParameterFormatClimate.MISSING,
                "num_max_ge_90f", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormNumMaxLE32F(), ParameterFormatClimate.MISSING,
                "num_max_le_32f", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormNumMinLE32F(), ParameterFormatClimate.MISSING,
                "num_min_le_32f", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNormNumMinLE0F(), ParameterFormatClimate.MISSING,
                "num_min_le_0f", queryParams));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipPeriodNorm(),
                ParameterFormatClimate.MISSING_PRECIP, "precip_pd_mean",
                queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipPeriodMax(),
                ParameterFormatClimate.MISSING_PRECIP, "precip_pd_max",
                queryParams));

        dateRcds = record.getPrecipPeriodMaxYearList();
        switch (dateRcds.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(2).getYear(), ParameterFormatClimate.MISSING,
                    "precip_pd_max_yr3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(1).getYear(), ParameterFormatClimate.MISSING,
                    "precip_pd_max_yr2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(0).getYear(), ParameterFormatClimate.MISSING,
                    "precip_pd_max_yr1", queryParams));
            break;
        case 0:
            logger.debug("No precip max year records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipPeriodMin(),
                ParameterFormatClimate.MISSING_PRECIP, "precip_period_min",
                queryParams));

        dateRcds = record.getPrecipPeriodMinYearList();
        switch (dateRcds.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(2).getYear(), ParameterFormatClimate.MISSING,
                    "precip_pd_min_yr3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(1).getYear(), ParameterFormatClimate.MISSING,
                    "precip_pd_min_yr2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(0).getYear(), ParameterFormatClimate.MISSING,
                    "precip_pd_min_yr1", queryParams));
            break;
        case 0:
            logger.debug("No precip min year records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getPrecipDayNorm(),
                ParameterFormatClimate.MISSING_PRECIP, "precip_day_norm",
                queryParams));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumPrcpGE01Norm(), ParameterFormatClimate.MISSING,
                "num_prcp_ge_01", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumPrcpGE10Norm(), ParameterFormatClimate.MISSING,
                "num_prcp_ge_10", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumPrcpGE50Norm(), ParameterFormatClimate.MISSING,
                "num_prcp_ge_50", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumPrcpGE100Norm(), ParameterFormatClimate.MISSING,
                "num_prcp_ge_100", queryParams));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowPeriodNorm(), ParameterFormatClimate.MISSING_SNOW,
                "snow_pd_mean", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowPeriodNorm(), ParameterFormatClimate.MISSING_SNOW,
                "snow_pd_max", queryParams));

        dateRcds = record.getSnowPeriodMaxYearList();
        switch (dateRcds.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(2).getYear(), ParameterFormatClimate.MISSING,
                    "snow_pd_max_yr3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(1).getYear(), ParameterFormatClimate.MISSING,
                    "snow_pd_max_yr2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(0).getYear(), ParameterFormatClimate.MISSING,
                    "snow_pd_max_yr1", queryParams));
            break;
        case 0:
            logger.debug("No snow max year records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowMax24HRecord(),
                ParameterFormatClimate.MISSING_SNOW, "snow_max_24h_rec",
                queryParams));

        List<ClimateDates> dates = record.getSnow24HList();
        switch (dates.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dates.get(2).getStart(), "snow_24h_begin3", queryParams));
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dates.get(2).getEnd(), "snow_24h_end3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dates.get(1).getStart(), "snow_24h_begin2", queryParams));
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dates.get(1).getEnd(), "snow_24h_end2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dates.get(0).getStart(), "snow_24h_begin1", queryParams));
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dates.get(0).getEnd(), "snow_24h_end1", queryParams));
            break;
        case 0:
            logger.debug("No snow max 24-hour records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowWaterPeriodNorm(),
                ParameterFormatClimate.MISSING_SNOW, "snow_water_pd_norm",
                queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowGroundNorm(), ParameterFormatClimate.MISSING_SNOW,
                "snow_ground_norm", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getSnowGroundMax(), ParameterFormatClimate.MISSING_SNOW,
                "snow_ground_max", queryParams));

        dateRcds = record.getDaySnowGroundMaxList();
        switch (dateRcds.size()) {
        default:
        case 3:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(2), "day_snow_grnd_max3", queryParams));
        case 2:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(1), "day_snow_grnd_max2", queryParams));
        case 1:
            setClause.append(SubClause.getSetSubClause(setClause.toString(),
                    dateRcds.get(0), "day_snow_grnd_max1", queryParams));
            break;
        case 0:
            logger.debug("No snow on ground records for station ID: ["
                    + record.getInformId() + "], month: ["
                    + record.getMonthOfYear() + "] and period type: ["
                    + record.getPeriodType() + "]");
        }

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumSnowGETRNorm(), ParameterFormatClimate.MISSING,
                "num_snow_ge_tr", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumSnowGE1Norm(), ParameterFormatClimate.MISSING,
                "num_snow_ge_1", queryParams));

        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumHeatPeriodNorm(), ParameterFormatClimate.MISSING,
                "heat_pd_mean", queryParams));
        setClause.append(SubClause.getSetSubClause(setClause.toString(),
                record.getNumCoolPeriodNorm(), ParameterFormatClimate.MISSING,
                "cool_pd_mean", queryParams));

        if (setClause.length() == 5) {
            return false;
        }

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(setClause);
        sql.append(" WHERE station_id=:station_id");
        queryParams.put("station_id", record.getInformId());
        sql.append(" AND month_of_year=:month_of_year");
        queryParams.put("month_of_year", record.getMonthOfYear());
        sql.append(" AND period_type=:period_type");
        queryParams.put("period_type", record.getPeriodType().getValue());

        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), queryParams);
            return (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to update table "
                    + ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME
                    + ". Query: [" + sql + "] and map: [" + queryParams + "]",
                    e);
        }
    }

    /**
     * delete a record from mon_climate_norm
     * 
     * @param stationId
     * @param monthOfYear
     * @param periodtype
     * @return
     * @throws ClimateQueryException
     */
    public boolean deleteClimateMonthRecord(int stationId, int monthOfYear,
            PeriodType periodType) throws ClimateQueryException {
        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id=:station_id");
        queryParams.put("station_id", stationId);
        sql.append(" AND month_of_year=:month_of_year");
        queryParams.put("month_of_year", monthOfYear);
        sql.append(" AND period_type=:period_type");
        queryParams.put("period_type", periodType.getValue());

        boolean isDeleted = false;
        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), queryParams);
            isDeleted = (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to delete a row. Query: ["
                    + sql + "] and map: [" + queryParams + "]", e);
        }

        return isDeleted;

    }

    /**
     * Get the first/last available record from tables
     * 
     * @param firstOne
     * @param stationId
     * @param periodType
     * @return
     * @throws ClimateQueryException
     */
    public short getAvailableMonthOfYear(boolean firstOne, int stationId,
            PeriodType periodType) throws ClimateQueryException {
        StringBuilder sql = new StringBuilder("SELECT month_of_year FROM ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationId");
        sql.append(" AND period_type= :type");
        if (firstOne) {
            sql.append(" ORDER BY month_of_year ASC LIMIT 1");
        } else {
            sql.append(" ORDER BY month_of_year DESC LIMIT 1");
        }

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("type", periodType.getValue());

        short month = -1;

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                if (results[0] instanceof Number) {
                    month = ((Number) results[0]).shortValue();
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected short, got "
                                    + results[0].getClass().getName());
                }
            } else {
                logger.warn(
                        "Could not get available month of year with query: ["
                                + sql + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying for available month of year with query: ["
                            + sql + "] and map: [" + paramMap + "]",
                    e);
        }

        return month;
    }

    /**
     * Insert a record into table mon_climate_norm
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean insertClimateMonthRecord(PeriodClimo record)
            throws ClimateQueryException {
        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(" VALUES(");
        sql.append(":informID");
        queryParams.put("informID", record.getInformId());
        sql.append(", :monOfYear");
        queryParams.put("monOfYear", record.getMonthOfYear());
        sql.append(", :type");
        queryParams.put("type", record.getPeriodType().getValue());
        sql.append(", :maxTempNorm");
        queryParams.put("maxTempNorm", record.getMaxTempNorm());
        sql.append(", :maxTempRec");
        queryParams.put("maxTempRec", record.getMaxTempRecord());

        List<ClimateDate> tmpRecords = record.getDayMaxTempRecordList();
        if (tmpRecords.isEmpty() || tmpRecords.get(0) == null
                || tmpRecords.get(0).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :dayMaxTemp1");
            queryParams.put("dayMaxTemp1",
                    tmpRecords.get(0).getCalendarFromClimateDate());
        }
        if (tmpRecords.size() < 2 || tmpRecords.get(1) == null
                || tmpRecords.get(1).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :dayMaxTemp2");
            queryParams.put("dayMaxTemp2",
                    tmpRecords.get(1).getCalendarFromClimateDate());
        }
        if (tmpRecords.size() < 3 || tmpRecords.get(2) == null
                || tmpRecords.get(2).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :dayMaxTemp3");
            queryParams.put("dayMaxTemp3",
                    tmpRecords.get(3).getCalendarFromClimateDate());
        }

        sql.append(", :minTempNorm");
        queryParams.put("minTempNorm", record.getMinTempNorm());
        sql.append(", :minTempRec");
        queryParams.put("minTempRec", record.getMinTempRecord());

        tmpRecords = record.getDayMinTempRecordList();
        if (tmpRecords.isEmpty() || tmpRecords.get(0) == null
                || tmpRecords.get(0).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :dayMinTemp1");
            queryParams.put("dayMinTemp1",
                    tmpRecords.get(0).getCalendarFromClimateDate());
        }
        if (tmpRecords.size() < 2 || tmpRecords.get(1) == null
                || tmpRecords.get(1).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :dayMinTemp2");
            queryParams.put("dayMinTemp2",
                    tmpRecords.get(1).getCalendarFromClimateDate());
        }
        if (tmpRecords.size() < 3 || tmpRecords.get(2) == null
                || tmpRecords.get(2).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :dayMinTemp3");
            queryParams.put("dayMinTemp3",
                    tmpRecords.get(2).getCalendarFromClimateDate());
        }

        sql.append(", :normMeanTemp");
        queryParams.put("normMeanTemp", record.getNormMeanTemp());
        sql.append(", :normMeanMaxTemp");
        queryParams.put("normMeanMaxTemp", record.getNormMeanMaxTemp());
        sql.append(", :normMeanMinTemp");
        queryParams.put("normMeanMinTemp", record.getNormMeanMinTemp());

        sql.append(", :normNumMaxGE90");
        queryParams.put("normNumMaxGE90", record.getNormNumMaxGE90F());
        sql.append(", :normNumMaxLE32");
        queryParams.put("normNumMaxLE32", record.getNormNumMaxLE32F());
        sql.append(", :normNumMinLE32");
        queryParams.put("normNumMinLE32", record.getNormNumMinLE32F());
        sql.append(", :normNumMinLE0");
        queryParams.put("normNumMinLE0", record.getNormNumMinLE0F());

        sql.append(", :precipPeriodNorm");
        queryParams.put("precipPeriodNorm", record.getPrecipPeriodNorm());
        sql.append(", :precipPeriodMax");
        queryParams.put("precipPeriodMax", record.getPrecipPeriodMax());

        tmpRecords = record.getPrecipPeriodMaxYearList();
        if (tmpRecords.isEmpty() || tmpRecords.get(0) == null
                || tmpRecords.get(0).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :precipPeriodMaxYear1");
            queryParams.put("precipPeriodMaxYear1",
                    tmpRecords.get(0).getYear());
        }
        if (tmpRecords.size() < 2 || tmpRecords.get(1) == null
                || tmpRecords.get(1).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :precipPeriodMaxYear2");
            queryParams.put("precipPeriodMaxYear2",
                    tmpRecords.get(1).getYear());
        }
        if (tmpRecords.size() < 3 || tmpRecords.get(2) == null
                || tmpRecords.get(2).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :precipPeriodMaxYear3");
            queryParams.put("precipPeriodMaxYear3",
                    tmpRecords.get(2).getYear());
        }

        sql.append(", :precipPeriodMin");
        queryParams.put("precipPeriodMin", record.getPrecipPeriodMin());

        tmpRecords = record.getPrecipPeriodMinYearList();
        if (tmpRecords.isEmpty() || tmpRecords.get(0) == null
                || tmpRecords.get(0).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :precipPeriodMinYear1");
            queryParams.put("precipPeriodMinYear1",
                    tmpRecords.get(0).getYear());
        }
        if (tmpRecords.size() < 2 || tmpRecords.get(1) == null
                || tmpRecords.get(1).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :precipPeriodMinYear2");
            queryParams.put("precipPeriodMinYear2",
                    tmpRecords.get(1).getYear());
        }
        if (tmpRecords.size() < 3 || tmpRecords.get(2) == null
                || tmpRecords.get(2).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :precipPeriodMinYear3");
            queryParams.put("precipPeriodMinYear3",
                    tmpRecords.get(2).getYear());
        }

        sql.append(", :precipDayNorm");
        queryParams.put("precipDayNorm", record.getPrecipDayNorm());

        sql.append(", :numPrecipGE01Norm");
        queryParams.put("numPrecipGE01Norm", record.getNumPrcpGE01Norm());
        sql.append(", :numPrecipGE10Norm");
        queryParams.put("numPrecipGE10Norm", record.getNumPrcpGE10Norm());
        sql.append(", :numPrecipGE50Norm");
        queryParams.put("numPrecipGE50Norm", record.getNumPrcpGE50Norm());
        sql.append(", :numPrecipGE100Norm");
        queryParams.put("numPrecipGE100Norm", record.getNumPrcpGE100Norm());

        sql.append(", :snowPeriodNorm");
        queryParams.put("snowPeriodNorm", record.getSnowPeriodNorm());
        sql.append(", :snowPeriodRec");
        queryParams.put("snowPeriodRec", record.getSnowPeriodRecord());

        tmpRecords = record.getSnowPeriodMaxYearList();
        if (tmpRecords.isEmpty() || tmpRecords.get(0) == null
                || tmpRecords.get(0).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snowPeriodMaxYear1");
            queryParams.put("snowPeriodMaxYear1", tmpRecords.get(0).getYear());
        }
        if (tmpRecords.size() < 2 || tmpRecords.get(1) == null
                || tmpRecords.get(1).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snowPeriodMaxYear2");
            queryParams.put("snowPeriodMaxYear2", tmpRecords.get(1).getYear());
        }
        if (tmpRecords.size() < 3 || tmpRecords.get(2) == null
                || tmpRecords.get(2).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snowPeriodMaxYear3");
            queryParams.put("snowPeriodMaxYear3", tmpRecords.get(2).getYear());
        }

        List<ClimateDates> datesRcd = record.getSnow24HList();
        if (datesRcd.isEmpty() || datesRcd.get(0).getStart() == null
                || datesRcd.get(0).getStart().isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snow24HStart1");
            queryParams.put("snow24HStart1",
                    datesRcd.get(0).getStart().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 2 || datesRcd.get(1).getStart() == null
                || datesRcd.get(1).getStart().isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snow24HStart2");
            queryParams.put("snow24HStart2",
                    datesRcd.get(1).getStart().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 3 || datesRcd.get(2).getStart() == null
                || datesRcd.get(2).getStart().isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snow24HStart3");
            queryParams.put("snow24HStart3",
                    datesRcd.get(2).getStart().getCalendarFromClimateDate());
        }

        sql.append(", :snow24HMaxRec");
        queryParams.put("snow24HMaxRec", record.getSnowMax24HRecord());

        if (datesRcd.isEmpty() || datesRcd.get(0).getEnd() == null
                || datesRcd.get(0).getEnd().isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snow24HEnd1");
            queryParams.put("snow24HEnd1",
                    datesRcd.get(0).getEnd().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 2 || datesRcd.get(1).getEnd() == null
                || datesRcd.get(1).getEnd().isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snow24HEnd2");
            queryParams.put("snow24HEnd2",
                    datesRcd.get(1).getEnd().getCalendarFromClimateDate());
        }
        if (datesRcd.size() < 3 || datesRcd.get(2).getEnd() == null
                || datesRcd.get(2).getEnd().isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snow24HEnd3");
            queryParams.put("snow24HEnd3",
                    datesRcd.get(2).getEnd().getCalendarFromClimateDate());
        }

        sql.append(", :snowWaterPeriodNorm");
        queryParams.put("snowWaterPeriodNorm", record.getSnowWaterPeriodNorm());
        sql.append(", :snowGroundNorm");
        queryParams.put("snowGroundNorm", record.getSnowGroundNorm());
        sql.append(", :snowGroundMax");
        queryParams.put("snowGroundMax", record.getSnowGroundMax());

        tmpRecords = record.getDaySnowGroundMaxList();
        if (tmpRecords.isEmpty() || tmpRecords.get(0) == null
                || tmpRecords.get(0).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snowGroundMaxDay1");
            queryParams.put("snowGroundMaxDay1",
                    tmpRecords.get(0).getCalendarFromClimateDate());
        }
        if (tmpRecords.size() < 2 || tmpRecords.get(1) == null
                || tmpRecords.get(1).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snowGroundMaxDay2");
            queryParams.put("snowGroundMaxDay2",
                    tmpRecords.get(1).getCalendarFromClimateDate());
        }
        if (tmpRecords.size() < 3 || tmpRecords.get(2) == null
                || tmpRecords.get(2).isPartialMissing()) {
            sql.append(", null");
        } else {
            sql.append(", :snowGroundMaxDay3");
            queryParams.put("snowGroundMaxDay3",
                    tmpRecords.get(2).getCalendarFromClimateDate());
        }

        sql.append(", :numSnowGETR");
        queryParams.put("numSnowGETR", record.getNumSnowGETRNorm());
        sql.append(", :numSnowGE1");
        queryParams.put("numSnowGE1", record.getNumSnowGE1Norm());

        sql.append(", :numHeatPeriodNorm");
        queryParams.put("numHeatPeriodNorm", record.getNumHeatPeriodNorm());
        sql.append(", :numCoolPeriodNorm");
        queryParams.put("numCoolPeriodNorm", record.getNumCoolPeriodNorm());
        sql.append(")");

        boolean isInserted = false;
        try {
            int numRow = getDao().executeSQLUpdate(sql.toString(), queryParams);
            isInserted = (numRow == 1);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to insert into table "
                    + ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME
                    + ". Query: [" + sql + "] and map: [" + queryParams + "]",
                    e);
        }

        return isInserted;
    }

    /**
     * Update or insert a record into table mon_climate_norm
     * 
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean saveClimateMonthRecord(PeriodClimo record)
            throws ClimateQueryException {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM ");
        sql.append(ClimateDAOValues.MONTH_CLIMATE_NORM_TABLE_NAME);
        sql.append(" WHERE station_id= :stationID");
        sql.append(" AND month_of_year= :monOfYear");
        sql.append(" AND period_type= :type");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationID", record.getInformId());
        paramMap.put("monOfYear", record.getMonthOfYear());
        paramMap.put("type", record.getPeriodType().getValue());

        try {
            Object[] results = getDao().executeSQLQuery(sql.toString(),
                    paramMap);
            if ((results != null) && (results.length >= 1)) {
                return updateClimateMonthRecord(record);
            } else {
                return insertClimateMonthRecord(record);
            }
        } catch (ClimateQueryException e) {
            throw new ClimateQueryException("Error with inner query.", e);
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + sql
                    + "] and map: [" + paramMap + "]", e);
        }
    }
}

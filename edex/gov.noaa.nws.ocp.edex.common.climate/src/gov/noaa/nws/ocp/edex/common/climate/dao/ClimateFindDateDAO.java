/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.HashMap;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.RecentDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;

/**
 * DAO handling queries to find most recently stored data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 18, 2016 20636      wpaintsil   Initial creation
 * 24 FEB 2017  27420      amoore      Address warnings in code.
 * 16 MAR 2017  30162      amoore      Fix logging.
 * 18 APR 2017  33104      amoore      Use query maps now that DB issue is fixed.
 * 03 MAY 2017  33104      amoore      Use abstract map.
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

public class ClimateFindDateDAO extends ClimateDAO {
    /**
     * Constructor.
     */
    public ClimateFindDateDAO() {
        super();
    }

    /**
     * Migrated from adapt/climate/src/qc_climate/find_date.ec. Instead of
     * setting all the date values in the parameters, return an object
     * containing the dates.
     * 
     * <pre>
    *   find_date (long     station_id,
    *          climate_date     l_date) 
    *  
    *
    *   Doug Murphy         PRC/TDL         HP9000       
    *                           September 1998
    * 
    *   FUNCTION DESCRIPTION
    *   ====================
    *   This function utilizes Informix ESQL 7.1 to find the most 
    *   recently stored climatological data for the first station in the
    *   observed climate databases. This information appears initially in 
    *       the quality control GUI display fields.
    *
     *************************************************************************
     * </pre>
     * 
     * @param iInformId
     *            station ID
     * @return RecentDates - most recent dates for the station
     * @throws ClimateQueryException
     * 
     *
     */
    public RecentDates findDate(int iInformId) throws ClimateQueryException {
        RecentDates recentDates = RecentDates.getMissingRecentDates();

        recentDates.setInformId(iInformId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("iInformId", iInformId);

        StringBuilder dailyQuery = new StringBuilder(
                "SELECT to_char(MAX(date), 'yyyy-MM-dd') as date FROM ")
                        .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                        .append(" WHERE station_id = :iInformId");

        StringBuilder periodQuerySnip = new StringBuilder(
                "SELECT to_char(MAX(period_end), 'yyyy-MM-dd') as date FROM ")
                        .append(ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME)
                        .append(" WHERE inform_id = :iInformId AND period_type = ");

        StringBuilder monthlyQuery = new StringBuilder(periodQuerySnip)
                .append(PeriodType.MONTHLY_RAD.getValue());
        StringBuilder seasonalQuery = new StringBuilder(periodQuerySnip)
                .append(PeriodType.SEASONAL_RAD.getValue());
        StringBuilder annualQuery = new StringBuilder(periodQuerySnip)
                .append(PeriodType.ANNUAL_RAD.getValue());

        recentDates.setDailyDate(
                getDateFromQuery(dailyQuery.toString(), paramMap));
        recentDates.setMonthDate(
                getDateFromQuery(monthlyQuery.toString(), paramMap));
        recentDates.setSeasonDate(
                getDateFromQuery(seasonalQuery.toString(), paramMap));
        recentDates.setAnnualDate(
                getDateFromQuery(annualQuery.toString(), paramMap));

        return recentDates;
    }

    /**
     * Use a query string to get a single max date value from the database.
     * 
     * @param query
     *            query for a period
     * @param paramMap
     * @return resultDate
     * @throws ClimateQueryException
     */
    private ClimateDate getDateFromQuery(String query,
            Map<String, Object> paramMap) throws ClimateQueryException {
        ClimateDate resultDate = ClimateDate.getMissingClimateDate();

        try {
            Object[] results = getDao().executeSQLQuery(query, paramMap);
            if ((results != null) && (results.length >= 1)) {
                Object result = results[0];
                try {
                    resultDate = new ClimateDate(result);
                } catch (NullPointerException e) {
                    throw new ClimateQueryException(
                            "Unexpected null result with query: [" + query
                                    + "] and map: [" + paramMap + "].",
                            e);
                } catch (Exception e) {
                    // if casting failed
                    throw new ClimateQueryException(
                            "Unexpected return column type.", e);
                }
            } else {
                logger.warn("Empty or null results from query: [" + query
                        + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate DB with query: [" + query
                            + "] and map: [" + paramMap + "]",
                    e);
        }
        return resultDate;
    }
}

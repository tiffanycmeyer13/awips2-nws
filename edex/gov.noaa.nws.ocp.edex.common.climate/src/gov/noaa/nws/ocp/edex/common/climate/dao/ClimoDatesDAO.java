/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;

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
 * 28 SEP 2016  20639      wkwock      Add update and insert methods
 * 13 OCT 2016  20639      wkwock      Add getSnowPrecip
 * 22 NOV 2016  23222      amoore      Legacy DR 15685. Some code clean up.
 * 16 MAR 2017  30162      amoore      Fix logging. Use modern Java standards.
 * 21 MAR 2017  20632      amoore      Fix logging, comments on null values.
 * 25 APR 2017  30144      amoore      Clean up queries and variables.
 * 02 MAY 2017  33104      amoore      Refactor queries to constants. Use query maps.
 * 01 MAR 2018  44624      amoore      Remove unused functionality.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public class ClimoDatesDAO extends ClimateDAO {
    /**
     * Query to get all season dates.
     */
    private static final String ALL_SEASONS_QUERY = "SELECT snow_season_beg, snow_year_beg,"
            + " precip_season_beg, precip_year_beg, "
            + " snow_season_end, snow_year_end, precip_season_end, precip_year_end "
            + " FROM " + ClimateDAOValues.CLIMO_DATES_TABLE_NAME;

    /**
     * Query to get snow and precip season begin dates.
     */
    private static final String SNOW_PRECIP_BEG_QUERY = "SELECT snow_season_beg, precip_season_beg FROM "
            + ClimateDAOValues.CLIMO_DATES_TABLE_NAME;

    /**
     * Constructor.
     */
    public ClimoDatesDAO() {
        super();
    }

    /**
     * Converted from get_season.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     ********************************************************************************* 
     * 
     *   void get_season    (       climate_dates   precip_season,
     *                               climate_dates  precip_year,
     *                  climate_dates   snow_season,
     *              climate_dates   snow_year
     *                               int             no_dates
     *                )
     * 
     *    Jason Tuell       PRC/TDL             HP 9000/7xx
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *  This function retrieves the starting and ending dates for the 
     *       precip season and year and snow season and year from the 
     *       climo_dates table.
     * 
     ******************************************************************************* 
     * </pre>
     * 
     * @param precipSeason
     * @param precipYear
     * @param snowSeason
     * @param snowYear
     * @return true if no dates could be found
     * @throws ClimateQueryException
     */
    public boolean getSeason(ClimateDates precipSeason, ClimateDates precipYear,
            ClimateDates snowSeason, ClimateDates snowYear)
            throws ClimateQueryException {
        boolean noDates = false;
        try {
            Object[] results = getDao().executeSQLQuery(ALL_SEASONS_QUERY);
            if ((results != null) && (results.length >= 1)) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        Object[] oa = (Object[]) result;
                        /*
                         * Any of these could be null, but should not be.
                         */
                        setClimoDates(snowSeason.getStart(), (String) oa[0]);
                        setClimoDates(snowYear.getStart(), (String) oa[1]);
                        setClimoDates(precipSeason.getStart(), (String) oa[2]);
                        setClimoDates(precipYear.getStart(), (String) oa[3]);
                        setClimoDates(snowSeason.getEnd(), (String) oa[4]);
                        setClimoDates(snowYear.getEnd(), (String) oa[5]);
                        setClimoDates(precipSeason.getEnd(), (String) oa[6]);
                        setClimoDates(precipYear.getEnd(), (String) oa[7]);
                    } catch (Exception e) {
                        // if casting failed
                        throw new Exception(
                                "Unexpected return column type or null value from query.",
                                e);
                    }

                } else {
                    throw new Exception(
                            "Unexpected return type from getSeason query, expected Object[], got "
                                    + result.getClass().getName());
                }

            } else {
                // no results
                noDates = true;
                logger.warn("Could not get season data using query: ["
                        + ALL_SEASONS_QUERY + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the climate database with query: ["
                            + ALL_SEASONS_QUERY + "]",
                    e);
        }
        return noDates;
    }

    /**
     * get snow_season_beg, precip_season_beg from climo_dates table
     * 
     * @return
     * @throws Exception
     */
    public List<ClimateDate> getSnowPrecip() throws Exception {
        List<ClimateDate> snowPrecipDates = null;
        try {
            Object[] results = getDao().executeSQLQuery(SNOW_PRECIP_BEG_QUERY);
            if ((results != null) && (results.length > 0)) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        Object[] oa = (Object[]) result;
                        /*
                         * Any of these could be null, but should not be
                         */
                        ClimateDate snowSeasonBeg = ClimateDate
                                .parseMonthDayDateFromString((String) oa[0]);
                        ClimateDate precipSeasonBeg = ClimateDate
                                .parseMonthDayDateFromString((String) oa[1]);
                        snowPrecipDates = new ArrayList<>();
                        snowPrecipDates.add(0, snowSeasonBeg);
                        snowPrecipDates.add(1, precipSeasonBeg);
                    } catch (Exception e) { // if casting failed
                        throw new Exception(
                                "Unexpected return column type or null value from query.",
                                e);
                    }

                } else {
                    throw new Exception(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }

            } else {
                // no results
                logger.warn("Could not get season data using query: ["
                        + SNOW_PRECIP_BEG_QUERY + "]");
            }
        } catch (Exception e) {
            throw new Exception(
                    "Error querying the climate database with query: ["
                            + SNOW_PRECIP_BEG_QUERY + "]",
                    e);
        }
        return snowPrecipDates;
    }

    /**
     * update climo_dates table
     * 
     * @param precipSeason
     * @param precipYear
     * @param snowSeason
     * @param snowYear
     * @return number of rows affected
     * @throws ClimateQueryException
     */
    public int updateClimoDates(ClimateDates precipSeason,
            ClimateDates precipYear, ClimateDates snowSeason,
            ClimateDates snowYear) throws ClimateQueryException {

        Map<String, Object> queryParams = new HashMap<>();

        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(ClimateDAOValues.CLIMO_DATES_TABLE_NAME);
        query.append(" SET ");
        query.append("snow_season_beg=:snowSeaStart, ");
        queryParams.put("snowSeaStart",
                snowSeason.getStart().toMonthDayDateString());
        query.append("snow_year_beg=:snowYearStart, ");
        queryParams.put("snowYearStart",
                snowYear.getStart().toMonthDayDateString());
        query.append("precip_season_beg=:precipSeaStart, ");
        queryParams.put("precipSeaStart",
                precipSeason.getStart().toMonthDayDateString());
        query.append("precip_year_beg=:precipYearStart, ");
        queryParams.put("precipYearStart",
                precipYear.getStart().toMonthDayDateString());
        query.append("snow_season_end=:snowSeaEnd, ");
        queryParams.put("snowSeaEnd",
                snowSeason.getEnd().toMonthDayDateString());
        query.append("snow_year_end=:snowYearEnd, ");
        queryParams.put("snowYearEnd",
                snowYear.getEnd().toMonthDayDateString());
        query.append("precip_season_end=:precipSeaEnd, ");
        queryParams.put("precipSeaEnd",
                precipSeason.getEnd().toMonthDayDateString());
        query.append("precip_year_end=:precipYearEnd");
        queryParams.put("precipYearEnd",
                precipYear.getEnd().toMonthDayDateString());
        // No 'where' clause. Table climo_dates should have one row only.

        try {
            return getDao().executeSQLUpdate(query.toString(), queryParams);
        } catch (Exception e) {
            throw new ClimateQueryException("Error with query: [" + query
                    + "] and map: [" + queryParams + "]", e);
        }
    }
}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * DAO handling queries related to freeze dates table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 05 OCT 2016  21378      amoore      Initial creation
 * 28 OCT 2016  20635      wkwock      Fix throw exception type
 * 17 NOV 2016  21378      amoore      Fixed query bugs in parsing dates.
 * 18 NOV 2016  21378      amoore      Protect against null values. Don't use query maps.
 * 16 DEC 2016  27015      amoore      Query fixes.
 * 16 MAR 2017  30162      amoore      Fix logging. Merge two logically identical methods.
 * 20 MAR 2017  20632      amoore      Additional comments.
 * 18 APR 2017  33104      amoore      Use query maps now that DB issue is fixed.
 * 24 APR 2017  33104      amoore      Use query maps for update/insert/delete.
 * 03 MAY 2017  33104      amoore      Use abstract map.
 * 20 JUN 2017  33104      amoore      Address review comments.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateFreezeDatesDAO extends ClimateDAO {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateFreezeDatesDAO.class);

    /**
     * Constant used to set column values to null if value is missing.
     */
    private static final String COLUMN_NULL_STRING = "null";

    /**
     * Constructor.
     */
    public ClimateFreezeDatesDAO() {
        super();
    }

    /**
     * Migrated from get_freeze_dates.ec
     * 
     * <pre>
     * void get_freeze_dates (      int         module,
    *              long            inform_id,
    *              climate_date        *first_freeze1,
    *              climate_date        *last_freeze1,
    *              climate_date        *first_freeze2,
    *              climate_date        *last_freeze2
    *                )
    *
    *   Dave Miller        PRC/TDL             HP 9000/7xx
    *                              October 1999
    *   Doug Murphy        PRC/TDL             December 1999
    *
    *   FUNCTION DESCRIPTION
    *   ====================
    *
    *  This function retrieves freeze dates for a station from the freeze date
    *  database table. Consists of three modules which each retrieve a separate
    *      set of dates. 
    *
    *   VARIABLES
    *   =========
    *
    *   name           description
    *-------------------------------------------------------------------------------                   
    * Input 
    *   module         tells routine which select statement to use
    *                   module 1:   retrieves observed first and
    *                       last freeze dates for season
    *                   module 2:   retrieves normal and record
    *                       first and last freeze dates
    *                   module 3:   used for annual climate reports,
    *                       retrieves last freeze of last 
    *                       season and first freeze for
    *                       current season
    *                      
    *  MODIFICATION HISTORY
    *  --------------------
    *   3/25/03    Bob Morris                Fixed arguments in calls to risnull and
    *                                        rsetnull, need to use defined constants
    *                                        for C-data types, not values (hard-coded,
    *                                        no less!) for SQL data types.  OB2
    *   1/18/05    Gary Battel/              Conversion from INFORMIX to POSTGRES
    *              Manan Dalal
     * </pre>
     * 
     * @param module
     *            module to run.
     * @param informId
     *            station ID.
     * @param firstFreeze1
     *            first freeze 1 to get set. Assumed to be set to missing
     *            already.
     * @param lastFreeze1
     *            last freeze 1 to get set. Assumed to be set to missing
     *            already.
     * @param firstFreeze2
     *            first freeze 2 to get set. Assumed to be set to missing
     *            already.
     * @param lastFreeze2
     *            last freeze 2 to get set. Assumed to be set to missing
     *            already.
     * @throws ClimateQueryException
     */
    public void getFreezeDates(int module, int informId,
            ClimateDate firstFreeze1, ClimateDate lastFreeze1,
            ClimateDate firstFreeze2, ClimateDate lastFreeze2)
                    throws ClimateQueryException {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("informId", informId);

        switch (module) {
        case 1:
            /********************************************************************/
            /* MODULE 1: Retrieve observed freeze date values */
            /********************************************************************/
            StringBuilder query1 = new StringBuilder(
                    "SELECT early_freeze, last_freeze FROM ");
            query1.append(ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME);
            query1.append(" WHERE inform_id = :informId");

            try {
                Object[] results = getDao().executeSQLQuery(query1.toString(),
                        paramMap);
                if ((results != null) && (results.length >= 1)) {
                    Object result = results[0];
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        Object firstFreeze1Obj = oa[0];
                        // date can be null
                        if (firstFreeze1Obj != null) {
                            firstFreeze1.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) firstFreeze1Obj));
                        }

                        Object lastFreeze1Obj = oa[1];
                        // date can be null
                        if (lastFreeze1Obj != null) {
                            lastFreeze1.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) lastFreeze1Obj));
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                } else {
                    logger.warn(
                            "Empty or null freeze date results from query: ["
                                    + query1 + "] and map: [" + paramMap + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "An error was encountered retrieving the freeze date information for "
                                + " station ID [" + informId + "]."
                                + "Error querying the climate database with query: ["
                                + query1 + "] and map: [" + paramMap + "]",
                        e);
            }
        case 2:
            /********************************************************************/
            /* MODULE 2: Retrieve record and normal freeze date values */
            /********************************************************************/
            StringBuilder query2 = new StringBuilder(
                    "SELECT norm_early_freeze, norm_last_freeze, ");
            query2.append(" rec_early_freeze, rec_last_freeze FROM ");
            query2.append(ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME);
            query2.append(" WHERE inform_id = :informId");

            try {
                Object[] results = getDao().executeSQLQuery(query2.toString(),
                        paramMap);
                if ((results != null) && (results.length >= 1)) {
                    Object result = results[0];
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        Object firstFreeze1Obj = oa[0];
                        // date can be null
                        if (firstFreeze1Obj != null) {
                            firstFreeze1.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) firstFreeze1Obj));
                        }

                        Object lastFreeze1Obj = oa[1];
                        // date can be null
                        if (lastFreeze1Obj != null) {
                            lastFreeze1.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) lastFreeze1Obj));
                        }

                        Object firstFreeze2Obj = oa[2];
                        // date can be null
                        if (firstFreeze2Obj != null) {
                            firstFreeze2.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) firstFreeze2Obj));
                        }

                        Object lastFreeze2Obj = oa[3];
                        // date can be null
                        if (lastFreeze2Obj != null) {
                            lastFreeze2.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) lastFreeze2Obj));
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                } else {
                    logger.warn(
                            "Empty or null freeze date results from query: ["
                                    + query2 + "] and map: [" + paramMap + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "An error was encountered retrieving the freeze date information for "
                                + " station ID [" + informId + "]."
                                + "Error querying the climate database with query: ["
                                + query2 + "] and map: [" + paramMap + "]",
                        e);
            }
        case 3:
            /********************************************************************/
            /* MODULE 3: Retrieve first freeze date for current season and */
            /* last winter season's last freeze */
            /* (This module used only for annual reports) */
            /********************************************************************/
            StringBuilder query3 = new StringBuilder(
                    "SELECT early_freeze, last_year_freeze FROM ");
            query3.append(ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME);
            query3.append(" WHERE inform_id = :informId");

            try {
                Object[] results = getDao().executeSQLQuery(query3.toString(),
                        paramMap);
                if ((results != null) && (results.length >= 1)) {
                    Object result = results[0];
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        Object firstFreeze1Obj = oa[0];
                        // date can be null
                        if (firstFreeze1Obj != null) {
                            firstFreeze1.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) firstFreeze1Obj));
                        }

                        Object lastFreeze1Obj = oa[1];
                        // date can be null
                        if (lastFreeze1Obj != null) {
                            lastFreeze1.setDateFromDate(
                                    ClimateDate.parseFullDateFromSQLDate(
                                            (Date) lastFreeze1Obj));
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                } else {
                    logger.warn(
                            "Empty or null results from freeze dates query: ["
                                    + query3 + "] and map: [" + paramMap + "]");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "An error was encountered retrieving the freeze date information for "
                                + " station ID [" + informId + "]."
                                + "Error querying the climate database with query: ["
                                + query3 + "] and map: [" + paramMap + "]",
                        e);
            }
        }
    }

    /**
     * Migrated from det_freeze_dates.ec
     * 
     * <pre>
     *    void det_freeze_dates(long           station_id,
     *                climate_date      start,
     *                climate_date      end, 
     *                climate_date      *early_freeze,
     *                climate_date      *late_freeze)
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  January 2000
     *   Gary Battel/Manan Dalal    SAIC/MDL,NGIT                   January, 2005
     *        Conversion from INFORMIX to POSTGRES
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function determines the first and last freeze date from the 
     *  daily climate database for a given station.
     *
     * </pre>
     *
     * @param stationId
     *            the station id of a given station
     * @param dates
     *            the date range for the freeze dates
     * @return ClimateDates object containing the first and last freeze date
     *         from the daily climate database for a given station.
     * @throws ClimateQueryException
     */
    public ClimateDates detFreezeDates(int stationId, ClimateDates dates)
            throws ClimateQueryException {

        ClimateDates freezeDates = ClimateDates.getMissingClimateDates();

        StringBuilder query = new StringBuilder("SELECT COUNT(date) FROM ")
                .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                .append(" WHERE station_id = :stationId")
                .append(" AND min_temp <= 32 AND date ")
                .append(" BETWEEN :startDate").append(" AND :endDate");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationId", stationId);
        paramMap.put("startDate",
                dates.getStart().getCalendarFromClimateDate());
        paramMap.put("endDate", dates.getEnd().getCalendarFromClimateDate());

        int count = 0;
        try {
            Object[] res = getDao().executeSQLQuery(query.toString(), paramMap);
            if ((res != null) && (res.length >= 1)) {
                if (res[0] != null) {
                    count = ((Number) res[0]).intValue();
                } else {
                    logger.warn("Unexpected null result from query: [" + query
                            + "] and map: [" + paramMap + "]");
                }
            } else {
                logger.warn("Empty or null results from freeze dates query: ["
                        + query + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error querying the Climate DB with query: [" + query
                            + "] and map: [" + paramMap + "]",
                    e);
        }

        if (count != 0) {
            StringBuilder query2 = new StringBuilder(
                    "SELECT MIN(date), MAX(date) FROM ")
                            .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME)
                            .append(" WHERE station_id = :stationId")
                            .append(" AND min_temp <= 32 AND to_char(date, 'yyyy-MM-dd') ")
                            .append("BETWEEN :startDate")
                            .append(" AND :endDate");

            Object[] data;
            try {
                data = getDao().executeSQLQuery(query2.toString(), paramMap);

                if (data != null && data.length >= 1) {
                    // expect one only result
                    Object[] rowData = (Object[]) data[0];
                    // date can be null
                    if (rowData[0] != null) {
                        freezeDates
                                .setStart(new ClimateDate((Date) (rowData[0])));
                    }

                    // date can be null
                    if (rowData[1] != null) {
                        freezeDates
                                .setEnd(new ClimateDate((Date) (rowData[1])));
                    }

                } else {
                    logger.warn(
                            "Empty or null results from freeze dates query: ["
                                    + query2 + "] and map: [" + paramMap + "]");
                }

            } catch (Exception e) {
                throw new ClimateQueryException(
                        "An error was encountered retrieving the freeze date information for "
                                + " station ID [" + stationId + "].",
                        e);
            }

        }
        return freezeDates;
    }

    /**
     * Migrated from update_freeze_db.ec
     * 
     * <pre>
     *  void update_freeze_db ( int             type,
     *              long            station,
     *              climate_date            *first,
     *              climate_date        *last   )
     *
     *   Doug Murphy        PRC/TDL             HP 9000/7xx
     *                                  November 1999
     *
     *   FUNCTION DESCRIPTION
     *   ====================
     *
     *  This function updates/inserts into the freeze date data base.
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
     *      7/18/00  Doug Murphy           Added NULL checks  
     *      3/25/03  Bob Morris            Fixed arguments in calls to risnull and
     *                                     rsetnull, need to use defined constants
     *                                     for C-data types, not values (hard-coded,
     *                                     no less!) for SQL data types.  OB2
     *      1/19/05  Gary Battel/          Conversion from INFORMIX to POSTGRES OB6
     *               Manan Dalal
     * </pre>
     * 
     * @param type
     * @param stationId
     * @param dates
     *            object containing first and last freeze dates
     * @return
     * @throws ClimateQueryException
     */
    public ClimateDates updateFreezeDB(int type, int stationId,
            ClimateDates dates) throws ClimateQueryException {

        StringBuilder selectQuery = new StringBuilder("SELECT ")
                .append("early_freeze, last_freeze, ")
                .append("norm_early_freeze, norm_last_freeze, last_year_freeze, ")
                .append("rec_early_freeze, rec_last_freeze FROM ")
                .append(ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME)
                .append(" WHERE inform_id = :stationId");

        ClimateDate earlyFreeze = ClimateDate.getMissingClimateDate(),
                lastFreeze = ClimateDate.getMissingClimateDate(),
                normEarlyFreeze = ClimateDate.getMissingClimateDate(),
                normLateFreeze = ClimateDate.getMissingClimateDate(),
                lastYearFreeze = ClimateDate.getMissingClimateDate(),
                recEarlyFreeze = ClimateDate.getMissingClimateDate(),
                recLateFreeze = ClimateDate.getMissingClimateDate();

        Map<String, Object> selectParamMap = new HashMap<>();
        selectParamMap.put("stationId", stationId);

        boolean exists = false;
        try {
            Object[] results = getDao().executeSQLQuery(selectQuery.toString(),
                    selectParamMap);
            if ((results != null) && (results.length >= 1)) {
                // expect one only
                Object result = results[0];
                if (result instanceof Object[]) {
                    Object[] oa = (Object[]) result;
                    Object earlyFreezeObj = oa[0];
                    // date could be null
                    if (earlyFreezeObj != null) {
                        earlyFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) earlyFreezeObj));
                    }

                    Object lastFreezeObj = oa[1];
                    // date could be null
                    if (lastFreezeObj != null) {
                        lastFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) lastFreezeObj));
                    }

                    Object firstFreezeObj = oa[2];
                    // date could be null
                    if (firstFreezeObj != null) {
                        normEarlyFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) firstFreezeObj));
                    }

                    Object normLateFreezeObj = oa[3];
                    // date could be null
                    if (normLateFreezeObj != null) {
                        normLateFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) normLateFreezeObj));
                    }

                    Object lastYearFreezeObj = oa[4];
                    // date could be null
                    if (lastYearFreezeObj != null) {
                        lastYearFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) lastYearFreezeObj));
                    }

                    Object recEarlyFreezeObj = oa[5];
                    // date could be null
                    if (recEarlyFreezeObj != null) {
                        recEarlyFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) recEarlyFreezeObj));
                    }

                    Object recLateFreezeObj = oa[6];
                    // date could be null
                    if (recLateFreezeObj != null) {
                        recLateFreeze.setDateFromDate(
                                ClimateDate.parseFullDateFromSQLDate(
                                        (Date) recLateFreezeObj));
                    }

                    exists = true;
                } else {
                    throw new ClimateQueryException(
                            "Unexpected return type from query, expected Object[], got "
                                    + result.getClass().getName());
                }
            }
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "An error was encountered retrieving the freeze date information for "
                            + " station ID [" + stationId + "]."
                            + "Error querying the climate database with query: ["
                            + selectQuery + "] and map: [" + selectParamMap
                            + "]",
                    e);
        }

        StringBuilder updateInsertQuery;
        Map<String, Object> updateInsertParamMap = new HashMap<>();

        if (exists) {
            // Type 1: Update Observed Dates if necessary
            if (type == 1) {
                if ((earlyFreeze.getYear() == ParameterFormatClimate.MISSING
                        || (dates.getStart().getYear() == earlyFreeze.getYear()
                                - 1 && dates.getStart().getMon() > 6)
                        || (dates.getStart().getYear() == earlyFreeze.getYear()
                                && (dates.getStart().getMon() < earlyFreeze
                                        .getMon()
                                        || (dates.getStart()
                                                .getMon() == earlyFreeze
                                                        .getMon()
                                                && dates.getStart()
                                                        .getDay() < earlyFreeze
                                                                .getDay()))))
                        && !dates.getStart().isPartialMissing()) {
                    earlyFreeze.setDateFromDate(dates.getStart());
                } else {
                    dates.setStart(earlyFreeze);
                }

                if ((lastFreeze.getYear() == ParameterFormatClimate.MISSING
                        || ((dates.getEnd().getYear() == lastFreeze.getYear()
                                && (dates.getEnd().getMon() > lastFreeze
                                        .getMon()
                                        || (dates.getEnd()
                                                .getMon() == lastFreeze.getMon()
                                                && dates.getEnd()
                                                        .getDay() > lastFreeze
                                                                .getDay())))
                                || (dates.getEnd().getYear() > lastFreeze
                                        .getYear()
                                        && dates.getEnd().getMon() < lastFreeze
                                                .getMon())))
                        && !dates.getEnd().isPartialMissing()) {
                    lastFreeze.setDateFromDate(dates.getEnd());
                } else {
                    dates.setEnd(lastFreeze);
                }

            }
            // Type 2: Update Normal Dates
            if (type == 2) {
                if (dates.getStart().isPartialMissing()) {
                    normEarlyFreeze = null;
                } else {
                    normEarlyFreeze.setDateFromDate(dates.getStart());
                }

                if (dates.getEnd().isPartialMissing()) {
                    normLateFreeze = null;
                } else {
                    normLateFreeze.setDateFromDate(dates.getEnd());
                }

            }
            // Type 3: Update Record Dates
            if (type == 3) {
                if (dates.getStart().isPartialMissing()) {
                    recEarlyFreeze = null;
                } else {
                    recEarlyFreeze.setDateFromDate(dates.getStart());
                }

                if (dates.getEnd().isPartialMissing()) {
                    recLateFreeze = null;
                } else {
                    recLateFreeze.setDateFromDate(dates.getEnd());
                }

            }
            // Type 4: Reset Dates for New Season
            // Type 5: Reset Dates, but not for New Season
            if (type == 4 || type == 5) {
                if (type == 4) {
                    lastYearFreeze.setDateFromDate(lastFreeze);
                }
                earlyFreeze = null;
                lastFreeze = null;
            }

            updateInsertQuery = new StringBuilder("UPDATE ")
                    .append(ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME)
                    .append(" SET early_freeze = ")
                    .append(dateColumnValueFromClimateDate("earlyFreeze",
                            updateInsertParamMap, earlyFreeze))
                    .append(", last_freeze = ")
                    .append(dateColumnValueFromClimateDate("lateFreeze",
                            updateInsertParamMap, lastFreeze))
                    .append(", norm_early_freeze = ")
                    .append(dateColumnValueFromClimateDate("normEarlyFreeze",
                            updateInsertParamMap, normEarlyFreeze))
                    .append(", norm_last_freeze  = ")
                    .append(dateColumnValueFromClimateDate("normLateFreeze",
                            updateInsertParamMap, normLateFreeze))
                    .append(", last_year_freeze  = ")
                    .append(dateColumnValueFromClimateDate("lastYearFreeze",
                            updateInsertParamMap, lastYearFreeze))
                    .append(", rec_early_freeze  = ")
                    .append(dateColumnValueFromClimateDate("recEarlyFreeze",
                            updateInsertParamMap, recEarlyFreeze))
                    .append(", rec_last_freeze = ")
                    .append(dateColumnValueFromClimateDate("recLateFreeze",
                            updateInsertParamMap, recLateFreeze))
                    .append(" WHERE inform_id = ").append(stationId);
        } else {
            switch (type) {
            // Type 1: Insert Observed Dates
            case 1:
                if (!dates.getStart().isPartialMissing()) {
                    earlyFreeze.setDateFromDate(dates.getStart());
                }
                if (!dates.getEnd().isPartialMissing()) {
                    lastFreeze.setDateFromDate(dates.getEnd());
                }
                break;
            // Type 2: Insert Normal Dates
            case 2:
                if (!dates.getStart().isPartialMissing()) {
                    normEarlyFreeze.setDateFromDate(dates.getStart());
                }
                if (!dates.getEnd().isPartialMissing()) {
                    normLateFreeze.setDateFromDate(dates.getEnd());
                }
                break;
            // Type 3: Insert Record Dates
            case 3:
                if (!dates.getStart().isPartialMissing()) {
                    recEarlyFreeze.setDateFromDate(dates.getStart());
                }
                if (!dates.getEnd().isPartialMissing()) {
                    recLateFreeze.setDateFromDate(dates.getEnd());
                }
                break;
            default:
                // do nothing
                logger.debug("No special logic for freeze dates type: [" + type
                        + "]");
            }

            updateInsertQuery = new StringBuilder("INSERT INTO ")
                    .append(ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME)
                    .append("(inform_id, early_freeze, last_freeze, norm_early_freeze, ")
                    .append("norm_last_freeze, last_year_freeze, rec_early_freeze, rec_last_freeze) VALUES (")
                    .append(stationId).append(", ")
                    .append(dateColumnValueFromClimateDate("earlyFreeze",
                            updateInsertParamMap, earlyFreeze))
                    .append(", ")
                    .append(dateColumnValueFromClimateDate("lastFreeze",
                            updateInsertParamMap, lastFreeze))
                    .append(", ")
                    .append(dateColumnValueFromClimateDate("normEarlyFreeze",
                            updateInsertParamMap, normEarlyFreeze))
                    .append(", ")
                    .append(dateColumnValueFromClimateDate("normLateFreeze",
                            updateInsertParamMap, normLateFreeze))
                    .append(", ")
                    .append(dateColumnValueFromClimateDate("lastYearFreeze",
                            updateInsertParamMap, lastYearFreeze))
                    .append(", ")
                    .append(dateColumnValueFromClimateDate("recEarlyFreeze",
                            updateInsertParamMap, recEarlyFreeze))
                    .append(", ")
                    .append(dateColumnValueFromClimateDate("recLateFreeze",
                            updateInsertParamMap, recLateFreeze))
                    .append(")");

        }

        try {
            getDao().executeSQLUpdate(updateInsertQuery.toString(),
                    updateInsertParamMap);
        } catch (Exception e) {
            throw new ClimateQueryException("Failed to update table "
                    + ClimateDAOValues.CLIMATE_FREEZE_DATES_TABLE_NAME
                    + ". Query: [" + updateInsertQuery + "] and map: ["
                    + updateInsertParamMap + "]", e);
        }

        return dates;
    }

    /**
     * @param column
     *            column ID for parameter
     * @param paramMap
     *            parameter map to fill in
     * @param date
     * @return {@link ClimateFreezeDatesDAO#COLUMN_NULL_STRING} if the given
     *         date is null or partially missing. A parameter declaration using
     *         the column name (:column) otherwise, along with adding the
     *         parameter and value to the map.
     */
    private static String dateColumnValueFromClimateDate(String column,
            Map<String, Object> paramMap, ClimateDate date) {
        if ((date == null) || (date.isPartialMissing())) {
            return COLUMN_NULL_STRING;
        } else {
            paramMap.put(column, date.getCalendarFromClimateDate());
            return ":" + column;
        }
    }
}
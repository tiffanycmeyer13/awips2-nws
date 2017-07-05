/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateDataAccessConfiguration;

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
 * 13 OCT 2016  21378      amoore      Enhance logging of queryForOneValue methods.
 * 20 OCT 2016  21378      amoore      Extracted station code query to here for use by all DAOs.
 * 27 OCT 2016  21378      amoore      Extracted some calc_period_obs functionality here for common use.
 * 17 NOV 2016  20636      wpaintsil   Tweaked use of query maps. Use with column names appears 
 *                                     to cause exceptions. Works fine with values. 
 *                                     Resolved some number casting exceptions as well.
 * 24 JAN 2017  27020      amoore      Refactor of build queries. Additional logging.
 * 08 FEB 2017  28609      amoore      Make single-value queries public.
 * 22 FEB 2017  28609      amoore      Address TODOs. Fix comments.
 * 16 MAR 2017  30162      amoore      Fix comments.
 * 20 MAR 2017  20632      amoore      Fix missing return values for threshold and equality.
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 18 APR 2017  33104      amoore      Code consolidation.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 31 AUG 2017  37561      amoore      Use calendar/date parameters where possible.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public abstract class ClimateDAO {

    /**
     * Enumeration of types of functions that the build element method will
     * take.
     * 
     * <pre>
     *  
     * SOFTWARE HISTORY
     * 
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * 20 OCT 2016  21378      amoore      Initial creation
     * 27 OCT 2016  21378      amoore      Move to super class for common access.
     * 
     * </pre>
     * 
     * @author amoore
     */
    protected enum BuildElementType {
        /**
         * Sum function.
         */
        SUM("SUM"),

        /**
         * Average function.
         */
        AVG("AVG"),

        /**
         * Minimum function.
         */
        MIN("MIN"),

        /**
         * Maximum function.
         */
        MAX("MAX"),

        /**
         * Count function.
         */
        COUNT("COUNT");

        private String value;

        /**
         * @param iValue
         */
        private BuildElementType(final String iValue) {
            this.value = iValue;
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * @param iValue
         *            value.
         */
        public void setValue(String iValue) {
            value = iValue;
        }
    }

    /** The data access object */
    private final CoreDao dao;

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateDAO.class);

    public ClimateDAO() {
        dao = new CoreDao(DaoConfig.forDatabase(
                ClimateDataAccessConfiguration.getClimateDBName()));
    }

    /**
     * Query for a single value using the given parameters. Users of this method
     * must be certain to be careful with typing of the missing value versus
     * what type is expected on valid returns versus how the result of this
     * method is cast/handled on returning.
     * 
     * @param keyQuery
     * @param keyParamMap
     * @param missingValue
     * @return resulting value, or the missing value provided on any error.
     */
    public final Object queryForOneValue(String keyQuery,
            Map<String, Object> keyParamMap, Object missingValue) {
        try {
            Object[] res = dao.executeSQLQuery(keyQuery, keyParamMap);
            if ((res != null) && (res.length >= 1)) {
                if (res[0] != null) {
                    return res[0];
                } else {
                    logger.warn(
                            "Null result querying the climate database with query: ["
                                    + keyQuery + "] and map [" + keyParamMap
                                    + "]");
                }
            } else {
                logger.warn(
                        "Null or empty result set querying the climate database with query: ["
                                + keyQuery + "] and map [" + keyParamMap + "]");
            }
        } catch (Exception e) {
            logger.error("Error querying the climate database with query: ["
                    + keyQuery + "] and map [" + keyParamMap + "]", e);
        }
        return missingValue;
    }

    /**
     * Query for a single value using the given parameters. Users of this method
     * must be certain to be careful with typing of the missing value versus
     * what type is expected on valid returns versus how the result of this
     * method is cast/handled on returning.
     * 
     * @param keyQuery
     * @param missingValue
     * @return resulting value, or the missing value provided on any error.
     */
    public final Object queryForOneValue(String keyQuery, Object missingValue) {
        return queryForOneValue(keyQuery, null, missingValue);
    }

    /**
     * Set the month and day fields for the given object based on the given date
     * string. Date string is assumed to be in the format "mm-dd".
     * 
     * @param date
     * @param datesString
     */
    protected static void setClimoDates(ClimateDate date, String datesString) {
        String[] dates = datesString.split("-");

        date.setDay(Integer.parseInt(dates[1]));
        date.setMon(Integer.parseInt(dates[0]));
    }

    /**
     * @return the dao to use for all querying.
     */
    protected final CoreDao getDao() {
        return dao;
    }

    /**
     * @param stationID
     * @return the station code associated with the given station ID (inform
     *         ID).
     * @throws ClimateQueryException
     */
    public final String getStationCodeByID(int stationID)
            throws ClimateQueryException {
        String query = "SELECT station_code FROM "
                + ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME
                + " WHERE station_id =" + ":stationID";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("stationID", stationID);

        // station code could be null, or no matching entries
        String stationCode = (String) queryForOneValue(query, queryParams,
                null);
        if (stationCode == null) {
            /*
             * It is a critical issue if a station cannot be found.
             */
            throw new ClimateQueryException(
                    "Could not find a station code for ID: [" + stationID
                            + "] using query: [" + query + "] and map: ["
                            + queryParams.toString() + "].");
        }

        return stationCode;
    }

    /**
     * @param stationCode
     * @return the station ID (inform ID) associated with the given station
     *         code.
     * @throws ClimateQueryException
     */
    public final int getStationIDByCode(String stationCode)
            throws ClimateQueryException {
        String query = "SELECT station_id FROM "
                + ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME
                + " WHERE station_code =" + ":stationCode";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("stationCode", stationCode);

        int stationID = (int) queryForOneValue(query, queryParams,
                Integer.MIN_VALUE);
        if (stationID == Integer.MIN_VALUE) {
            /*
             * It is a critical issue if a station cannot be found.
             */
            throw new ClimateQueryException(
                    "Could not find a station ID for code: [" + stationCode
                            + "] using query: [" + query + "] and map: ["
                            + queryParams.toString() + "].");
        }

        return stationID;
    }

    /**
     * Rewritten from calc_period_obs.ecpp#days_past_thresh.
     * 
     * See
     * {@link #daysPastThresh(ClimateDate, ClimateDate, int, PeriodType, String, String, Number, Number, boolean)}
     * 
     * Assume not precip or snow, and standard missing value.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param threshold
     * @param greaterOrLess
     *            true if test for >=, false to test for <=.
     * @return count of instances with the specified value, dates, type, and
     *         station.
     */
    protected final int daysPastThresh(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn, Number threshold,
            boolean greaterOrLess) {
        return daysPastThresh(beginDate, endDate, stationID, iType, dailyColumn,
                periodColumn, ParameterFormatClimate.MISSING, threshold,
                greaterOrLess, false);
    }

    /**
     * Rewritten from calc_period_obs.ecpp#days_past_thresh.
     * 
     * See
     * {@link #daysPastThresh(ClimateDate, ClimateDate, int, PeriodType, String, String, Number, Number, boolean)}
     * 
     * Assume not precip or snow.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param missing
     *            missing value.
     * @param threshold
     * @param greaterOrLess
     *            true if test for >=, false to test for <=.
     * @return count of instances with the specified value, dates, type, and
     *         station.
     */
    protected final int daysPastThresh(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn, Number missing,
            Number threshold, boolean greaterOrLess) {
        return daysPastThresh(beginDate, endDate, stationID, iType, dailyColumn,
                periodColumn, missing, threshold, greaterOrLess, false);
    }

    /**
     * Rewritten from calc_period_obs.ecpp#days_past_thresh.
     * 
     * Similar to
     * {@link #buildElement(ClimateDate, ClimateDate, int, PeriodType, String, String, BuildElementType, Number, boolean)}
     * , but count records where value is past some threshold.
     * 
     * <pre>
     * days_past_thresh( )
    
    Novemeber 1999     David T. Miller        PRC/TDL
    
    Purpose:  Finds the number of days past a threshold.  For example, the number
             of days where the temperature was greater or equal to 90 degrees F
    
    
    Variables
    
      Input table       An array of character strings contain name and columns
                    of the database table SELECT is acting upon.   
        func        The function argument of SELECT, e.g. MAX()
        element         The name of the column in the table which func
                        is acting upon
        start       Beginning date
        end     Ending date
        station     The station identifier
        thresh          The threshold value
        equality_flag   Whether it's less than or equal or greater than or equal
        period_type Flag to used as a key in the cli_mon_season_yr table
    
    
      Output    ec_return_value    The number of days
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param missing
     *            missing value.
     * @param threshold
     * @param greaterOrLess
     *            true if test for >=, false to test for <=.
     * @param precipOrSnow
     *            true if the element being searched for is precip or snow
     *            (needs trace-related computation).
     * @return count of instances with the specified value, dates, type, and
     *         station.
     */
    protected final int daysPastThresh(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn, Number missing,
            Number threshold, boolean greaterOrLess, boolean precipOrSnow) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("stationID", stationID);
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("value", threshold);
        queryParams.put("missing", threshold);
        String element, idCol, endCol, startCol;

        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");

        // select by period from period table, or no period from daily table.
        if (!PeriodType.OTHER.equals(iType)) {
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE ");
            query.append(" period_type = :periodType AND ");

            queryParams.put("periodType", 5);

            element = periodColumn;
            idCol = "inform_id";
            endCol = "period_end";
            startCol = "period_start";
        } else {
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE ");

            element = dailyColumn;
            idCol = "station_id";
            endCol = "date";
            startCol = "date";
        }

        query.append(idCol).append(" = :stationID ");
        query.append(" AND ").append(startCol).append(" >= :beginDate ");
        query.append(" AND ").append(endCol).append(" <= :endDate ");
        query.append(" AND ").append(element).append(" != :missing ");
        query.append(" AND ").append(element);
        if (greaterOrLess) {
            query.append(" >= ");
        } else {
            query.append(" <= ");
        }
        query.append(" :value ");

        if (precipOrSnow) {
            query.append(" AND ").append(element).append(" != ");
            query.append(ParameterFormatClimate.TRACE);
        }

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Rewritten from calc_period_obs.ecpp#build_element.
     * 
     * Similar to
     * {@link #buildElement(ClimateDate, ClimateDate, int, PeriodType, String, String, BuildElementType, Number, boolean)}
     * , but test for equality to some value.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param equalValue
     * @return count of instances with the specified value, dates, type, and
     *         station.
     */
    protected final int buildElementEquality(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn, Number equalValue) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("stationID", stationID);
        queryParams.put("beginDate", beginDate.getCalendarFromClimateDate());
        queryParams.put("endDate", endDate.getCalendarFromClimateDate());
        queryParams.put("value", equalValue);
        String element, idCol, endCol, startCol;

        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ");

        // select by period from period table, or no period from daily table.
        if (!PeriodType.OTHER.equals(iType)) {
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE ");
            query.append(" period_type = :periodType AND ");

            queryParams.put("periodType", 5);
            element = periodColumn;
            idCol = "inform_id";
            endCol = "period_end";
            startCol = "period_start";
        } else {
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE ");

            element = dailyColumn;
            idCol = "station_id";
            endCol = "date";
            startCol = "date";
        }

        query.append(idCol).append(" = :stationID ");
        query.append(" AND ").append(startCol).append(" >= :beginDate ");
        query.append(" AND ").append(endCol).append(" <= :endDate ");
        query.append(" AND ").append(element).append(" = :value ");

        return ((Number) queryForOneValue(query.toString(), queryParams,
                ParameterFormatClimate.MISSING)).intValue();
    }

    /**
     * Rewritten from calc_period_obs.ecpp#build_element.
     * 
     * <pre>
     * build_element( )
    
    October 1999     David T. Miller        PRC/TDL
    
    
    Purpose:  Returns the result from a simple SELECT dynamic SQL call.  Usually,
             the result is from an aggregate function such as MAX, SUM, MIN, or 
         AVG.
    
    
    Variables
    
      Input table       An array of character strings contain name and columns
                    of the database table SELECT is acting upon.   
        func        The function argument of SELECT, e.g. MAX()
        element         The name of the column in the table which func
                        is acting upon
        start       Beginning date
        end     Ending date
        station     The station identifier
        period_type Flag to used as a key in the cli_mon_season_yr table
        add_line        An additional character line to tack onto the built SELECT
                        statement.  Adds a little more flexibility to the routine
                and is used for mean temp and mean rh.
    
      Output    ec_return_value  value returned from the SQL call
     * </pre>
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param buildType
     * @param missingValue
     * @param precipOrSnow
     *            true if the element being searched for is precip or snow
     *            (needs trace-related computation).
     * @return
     */
    protected final Number buildElement(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn,
            ClimateDAO.BuildElementType buildType, Number missingValue,
            boolean precipOrSnow) {
        Number oResult;

        StringBuilder query = new StringBuilder("SELECT ");
        String element, idCol, startCol, endCol;
        // select by period from period table, or no period from daily table.
        if (!PeriodType.OTHER.equals(iType)) {

            element = periodColumn;
            idCol = "inform_id";
            endCol = "period_end";
            startCol = "period_start";

            query.append(buildType.toString() + "("
                    + (buildType == BuildElementType.COUNT ? "*" : element)
                    + ")");
            query.append(" FROM ");
            query.append(
                    ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
            query.append(" WHERE ");
            query.append(" period_type = 5 AND ");
        } else {
            element = dailyColumn;
            idCol = "station_id";
            endCol = "date";
            startCol = "date";

            query.append(buildType.toString()).append("(").append(
                    (buildType == BuildElementType.COUNT ? "*" : element))
                    .append(")");
            query.append(" FROM ");
            query.append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
            query.append(" WHERE ");
        }

        query.append(idCol).append(" = :stationID");
        query.append(" AND ").append(startCol).append(" >= :beginDate");
        query.append(" AND ").append(endCol).append(" <= :endDate");
        query.append(" AND ").append(element).append(" != ")
                .append(missingValue);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationID", stationID);
        paramMap.put("beginDate", beginDate.getCalendarFromClimateDate());
        paramMap.put("endDate", endDate.getCalendarFromClimateDate());

        Object baseResult = queryForOneValue(query.toString(), paramMap,
                missingValue);

        Number baseResultNumber = (Number) baseResult;

        /*
         * if we are calculating sum precip or snow and allowing trace gave back
         * not missing, try excluding trace, which is the preferred data query.
         */
        if ((ClimateDAO.BuildElementType.SUM.equals(buildType)) && precipOrSnow
                && (!ClimateUtilities.floatingEquals(
                        baseResultNumber.doubleValue(),
                        missingValue.doubleValue()))) {

            // if precip or snow, do not allow trace
            query.append(" AND " + element + " != ");
            query.append(ParameterFormatClimate.TRACE);

            Object noTraceResult = queryForOneValue(query.toString(), paramMap,
                    missingValue);

            Number noTraceResultNumber = (Number) noTraceResult;

            if ((noTraceResultNumber.doubleValue() == 0) || ClimateUtilities
                    .floatingEquals(noTraceResultNumber.doubleValue(),
                            missingValue.doubleValue())) {
                /*
                 * allowing trace gave a result while not allowing trace did
                 * not. Return trace.
                 */
                oResult = ParameterFormatClimate.TRACE;
            } else {
                /*
                 * allowing trace gave a result, but so did not allowing trace.
                 * Return without trace.
                 */
                oResult = noTraceResultNumber;
            }
        } else {
            // accept the result
            oResult = baseResultNumber;
        }

        /*
         * Legacy documentation:
         * 
         * smo, DR 17019, F6 product not displaying correct snow depth level. To
         * deal with the scenario: the MAX(snow_ground)=0 but there are TRACE
         * days within the period
         */
        if (oResult.doubleValue() == 0
                && ClimateDAO.BuildElementType.MAX.equals(buildType)
                && ((periodColumn.contains("snow_ground")
                        && (!PeriodType.OTHER.equals(iType)))
                        || ((dailyColumn.contains("snow_ground"))
                                && (PeriodType.OTHER.equals(iType))))) {

            StringBuilder maxSnowGroundQuery = new StringBuilder(
                    "SELECT COUNT(*) FROM ");

            // select by period from period table, or no period from daily
            // table.
            if (!PeriodType.OTHER.equals(iType)) {
                maxSnowGroundQuery.append(
                        ClimateDAOValues.CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME);
                maxSnowGroundQuery.append(" WHERE ");
                maxSnowGroundQuery.append(" period_type = 5 AND ");

                element = periodColumn;
                idCol = "inform_id";
                endCol = "period_end";
                startCol = "period_start";
            } else {
                maxSnowGroundQuery
                        .append(ClimateDAOValues.DAILY_CLIMATE_TABLE_NAME);
                maxSnowGroundQuery.append(" WHERE ");

                element = dailyColumn;
                idCol = "station_id";
                endCol = "date";
                startCol = "date";
            }

            maxSnowGroundQuery.append(idCol).append(" = :stationID");
            maxSnowGroundQuery.append(" AND ").append(startCol)
                    .append(" >= :beginDate");
            maxSnowGroundQuery.append(" AND ").append(endCol)
                    .append(" <= :endDate");
            maxSnowGroundQuery.append(" AND ").append(element).append(" = ");
            maxSnowGroundQuery.append(ParameterFormatClimate.TRACE);

            int maxSnowGroundResult = ((Number) queryForOneValue(
                    maxSnowGroundQuery.toString(), paramMap,
                    ParameterFormatClimate.MISSING)).intValue();

            if (maxSnowGroundResult > 0) {
                oResult = ParameterFormatClimate.TRACE;
            }
        }

        return oResult;
    }

    /**
     * See
     * {@link #buildElement(ClimateDate, ClimateDate, int, PeriodType, String, String, BuildElementType, Number, boolean)}
     * 
     * 
     * Assume not precip or snow.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param buildType
     * @param missingValue
     * @return
     */
    protected final Number buildElement(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn,
            ClimateDAO.BuildElementType buildType, Number missingValue) {
        return buildElement(beginDate, endDate, stationID, iType, dailyColumn,
                periodColumn, buildType, missingValue, false);
    }

    /**
     * See
     * {@link #buildElement(ClimateDate, ClimateDate, int, PeriodType, String, String, BuildElementType, Number, boolean)}
     * 
     * 
     * Assume not precip or snow and standard missing value.
     * 
     * @param beginDate
     * @param endDate
     * @param stationID
     * @param iType
     *            period type. If non-zero, 5 will be used.
     * @param dailyColumn
     *            daily column to use, if period type is 0.
     * @param periodColumn
     *            period column to use, if period type is non-zero.
     * @param buildType
     * @param missingValue
     * @return
     */
    protected final Number buildElement(ClimateDate beginDate,
            ClimateDate endDate, int stationID, PeriodType iType,
            String dailyColumn, String periodColumn,
            ClimateDAO.BuildElementType buildType) {
        return buildElement(beginDate, endDate, stationID, iType, dailyColumn,
                periodColumn, buildType, ParameterFormatClimate.MISSING, false);
    }

    /**
     * Calculate the max precip value given an array of the values.
     * 
     * @param precipValues
     * @param values
     * @param maxValue
     * @param maxValueDays
     * @param day
     * @return The max 24-hour precip value.
     */
    protected static float checkMaxPrecipValue(float[] precipValues,
            float[] values, float maxValue, List<Integer> maxValueDays,
            int day) {
        int noon = 1;
        if (day == 1) {
            noon = 11; // first day, count from noon in previous month.
        }

        if ((precipValues != null) && (values != null)
                && (precipValues.length == TimeUtil.HOURS_PER_DAY)
                && (values.length == TimeUtil.HOURS_PER_DAY)) {

            for (int i = noon; i <= TimeUtil.HOURS_PER_DAY; i++) {
                float tempMax = 0.0f;
                for (int j = 0; j < TimeUtil.HOURS_PER_DAY; j++) {
                    if ((i + j) < TimeUtil.HOURS_PER_DAY) {
                        if (precipValues[i
                                + j] == ParameterFormatClimate.MISSING_PRECIP) {
                            tempMax = ParameterFormatClimate.MISSING_PRECIP;
                            break;
                        } else {
                            if (precipValues[i + j] > 0) {
                                tempMax += precipValues[i + j];
                            }
                        }

                    } else {
                        if (values[(i + j)
                                - TimeUtil.HOURS_PER_DAY] == ParameterFormatClimate.MISSING_PRECIP) {
                            tempMax = ParameterFormatClimate.MISSING_PRECIP;
                            break;
                        } else {
                            if (values[(i + j) - TimeUtil.HOURS_PER_DAY] > 0) {
                                tempMax += values[(i + j)
                                        - TimeUtil.HOURS_PER_DAY];
                            }
                        }
                    }
                }

                if ((tempMax != ParameterFormatClimate.MISSING_PRECIP)
                        && (tempMax > maxValue)) {

                    maxValue = tempMax;
                    if (i == TimeUtil.HOURS_PER_DAY - 1) {
                        maxValueDays.set(0, day);
                        maxValueDays.set(1, day);
                    } else {
                        maxValueDays.set(0, day - 1);
                        maxValueDays.set(1, day);
                    }
                }
            }
        }
        return maxValue;
    }
}
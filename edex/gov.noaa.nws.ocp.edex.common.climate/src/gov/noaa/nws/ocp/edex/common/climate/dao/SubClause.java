/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.Calendar;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;

/**
 * Class for create sub-clause
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#    Engineer    Description
 * ----------- ---------- ----------- --------------------------
 * 10/14/2016  20635      wkwock      Initial creation
 * 11/01/2016  20635      wkwock      Add isSetAlone()
 * 19 DEC 2016 27015      amoore      Checking against missing date information should
 *                                    require all date data present, not just at least one.
 * 03 MAY 2017 33104      amoore      Add query maps and javadoc.
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class SubClause {
    /** set constant */
    private final static String SET = "SET";

    /**
     * Get the sub set clause for this column.
     * 
     * @param setClause
     *            set clause so far.
     * @param value
     *            value to check and set in the clause.
     * @param missing
     *            missing value to check against.
     * @param columnName
     *            name of column to update.
     * @param queryParams
     *            query map to potentially add data to.
     * @return subclause that should be appended to the current set clause, or
     *         empty if value is flagged as missing.
     */
    public static StringBuilder getSetSubClause(String setClause, int value,
            int missing, String columnName, Map<String, Object> queryParams) {
        StringBuilder subClause = new StringBuilder();
        if (value != missing) {
            if (!isSetAlone(setClause)) {
                subClause.append(", ");
            }
            subClause.append(columnName).append("=:").append(columnName);
            queryParams.put(columnName, value);
        }

        return subClause;
    }

    /**
     * Get the sub set clause for this column
     * 
     * @param setClause
     *            set clause so far.
     * @param value
     *            value to check and set in the clause.
     * @param missing
     *            missing value to check against.
     * @param columnName
     *            name of column to update.
     * @param queryParams
     *            query map to potentially add data to.
     * @return subclause that should be appended to the current set clause, or
     *         empty if value is flagged as missing.
     */
    public static StringBuilder getSetSubClause(String setClause, float value,
            float missing, String columnName, Map<String, Object> queryParams) {
        StringBuilder subClause = new StringBuilder();
        if (value != missing) {
            if (!isSetAlone(setClause)) {
                subClause.append(", ");
            }
            subClause.append(columnName).append("=:").append(columnName);
            queryParams.put(columnName, value);
        }

        return subClause;
    }

    /**
     * Get the sub set clause for this column
     * 
     * @param setClause
     *            set clause so far.
     * @param value
     *            value to check and set in the clause.
     * @param columnName
     *            name of column to update.
     * @param queryParams
     *            query map to potentially add data to. {@link Calendar}
     *            equivalent of the given {@link ClimateDate} value will be
     *            used, if the date is not missing information.
     * @return subclause that should be appended to the current set clause, or
     *         empty if value is flagged as missing.
     */
    public static StringBuilder getSetSubClause(String setClause,
            ClimateDate value, String columnName,
            Map<String, Object> queryParams) {
        StringBuilder subClause = new StringBuilder();
        if (!value.isPartialMissing()) {
            if (!isSetAlone(setClause)) {
                subClause.append(", ");
            }
            subClause.append(columnName).append("=:").append(columnName);
            queryParams.put(columnName, value.getCalendarFromClimateDate());
        }

        return subClause;
    }

    /**
     * is set alone
     * 
     * @return
     */
    private static boolean isSetAlone(String setStr) {
        return SET.equalsIgnoreCase(setStr.trim());
    }
}

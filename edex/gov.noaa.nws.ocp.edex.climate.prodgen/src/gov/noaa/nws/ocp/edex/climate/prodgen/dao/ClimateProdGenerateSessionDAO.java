/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen.dao;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.DynamicSerializationManager;
import com.raytheon.uf.common.serialization.DynamicSerializationManager.SerializationType;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.edex.database.dao.CoreDao;
import com.raytheon.uf.edex.database.dao.DaoConfig;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdData;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdGenerateSessionData;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdGenerateSessionDataForView;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.SessionState;
import gov.noaa.nws.ocp.common.dataplugin.climate.StateStatus;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;

/**
 * ClimateProdGenerateSessionDAO
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------------------------
 * Feb 07, 2017  20637    pwang     Initial creation
 * Jun 07, 2017  34790    pwang     Simplified CPG purge call
 * Sep 08, 2017  37809    amoore    For queries, cast to Number rather than specific number type.
 * Nov 03, 2017  36749    amoore    Address review comments.
 * May 03, 2021  7849     mapeters  Switch from UFStatus to slf4j logging
 *
 * </pre>
 *
 * @author pwang
 */
public class ClimateProdGenerateSessionDAO extends CoreDao {

    public static final String CPG_SESSION_TABLE_NAME = "cpg_session";

    public static final String CPG_SESSION_ID_COLUMN = "cpg_session_id";

    public static final String STATE_COLUMN = "state";

    public static final String STATUS_COLUMN = "status";

    public static final String STATUS_DESC_COLUMN = "status_desc";

    public static final String LAST_UPDATED_COLUMN = "last_updated";

    public static final String PENDING_EXP_COLUMN = "pending_expire";

    public static final String REPORT_DATA_COLUMN = "report_data";

    public static final String PROD_DATA_COLUMN = "prod_data";

    private static final Object LOCK = new Object();

    /**
     * Constructor
     */
    public ClimateProdGenerateSessionDAO() {
        super(DaoConfig.forClass("climate",
                ClimateProdGenerateSessionData.class));
    }

    /**
     * Save a new session's data.
     *
     * @param sessionData
     * @return
     */
    public boolean saveNewCPGSessionData(
            ClimateProdGenerateSessionData sessionData)
            throws ClimateQueryException {
        boolean status = true;
        synchronized (LOCK) {

            Map<String, Object> parameters = sessionData.getColumnValues();

            String sql = getInsertStatement(CPG_SESSION_TABLE_NAME,
                    parameters.keySet());
            if (logger.isDebugEnabled()) {
                /* avoid formatting parameters if not in debug */
                logger.debug("SQL = " + sql + " PARAMETERS = " + parameters);
            }
            try {
                status = (executeSQLUpdate(sql, parameters) == 1);
            } catch (Exception e) {
                logger.error("SQL = " + sql + " PARAMETERS = " + parameters);
                logger.error(
                        "Error writing to table: " + CPG_SESSION_TABLE_NAME, e);
                throw new ClimateQueryException(
                        "Error writing to table: " + CPG_SESSION_TABLE_NAME, e);
            }
        }
        return status;
    }

    /**
     * Get all CPG sessions' data.
     *
     * @return
     * @throws Exception
     */
    public List<ClimateProdGenerateSessionData> retrieveAllCPGSessions()
            throws ClimateQueryException {
        List<ClimateProdGenerateSessionData> sessionList = new ArrayList<>();

        ClimateProdGenerateSessionData cpgs = new ClimateProdGenerateSessionData();
        Map<String, Object> parameters = cpgs.getColumnValues();
        String sql = getAllCPGSessionStatement(CPG_SESSION_TABLE_NAME,
                parameters.keySet());

        Object[] results = executeSQLQuery(sql, null);
        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                ClimateProdGenerateSessionData rec = new ClimateProdGenerateSessionData();
                rec.setCpg_session_id((String) oa[0]);
                rec.setRun_type(((Number) oa[1]).intValue());
                rec.setProd_type(((Number) oa[2]).intValue());
                rec.setState(((Number) oa[3]).intValue());
                rec.setStatus(((Number) oa[4]).intValue());
                rec.setStatus_desc((String) oa[5]);
                rec.setGlobal_config((byte[]) oa[6]);
                rec.setProd_setting((byte[]) oa[7]);
                rec.setReport_data((byte[]) oa[8]);
                rec.setProd_data((byte[]) oa[9]);
                rec.setStart_at((Timestamp) oa[10]);
                rec.setLast_updated((Timestamp) oa[11]);
                rec.setPending_expire((Timestamp) oa[12]);

                sessionList.add(rec);
            } else {
                throw new ClimateQueryException(
                        "Unexpected return type from the query, expected Object[], got "
                                + result.getClass().getName());
            }
        }
        return sessionList;
    }

    /**
     * Get all CPG sessions' limited set of data for GUI view.
     *
     * @return
     * @throws ClimateQueryException
     */
    public List<ClimateProdGenerateSessionDataForView> retrieveAllCPGSessionsForView()
            throws ClimateQueryException {
        List<ClimateProdGenerateSessionDataForView> sessionList = new ArrayList<>();

        ClimateProdGenerateSessionDataForView cpgs = new ClimateProdGenerateSessionDataForView();
        Map<String, Object> columns = cpgs.getColumnValues();
        String sql = getAllCPGSessionStatement(CPG_SESSION_TABLE_NAME,
                columns.keySet());

        Object[] results = executeSQLQuery(sql, null);
        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                ClimateProdGenerateSessionDataForView rec = new ClimateProdGenerateSessionDataForView();
                rec.setCpg_session_id((String) oa[0]);
                rec.setRun_type(((Number) oa[1]).intValue());
                rec.setProd_type(this
                        .getPeriodTypeFromValue(((Number) oa[2]).intValue()));
                rec.setState(SessionState.valueOf(((Number) oa[3]).intValue()));
                rec.setStateStatus(new StateStatus(((Number) oa[4]).intValue(),
                        (String) oa[5]));
                rec.setStatus_desc((String) oa[5]);
                rec.setProd_data(bytesToClimateProdData((byte[]) oa[6]));
                rec.setStart_at((Timestamp) oa[7]);
                rec.setLast_updated((Timestamp) oa[8]);

                sessionList.add(rec);
            } else {
                throw new ClimateQueryException(
                        "Unexpected return type from the query, expected Object[], got "
                                + result.getClass().getName());
            }
        }
        return sessionList;
    }

    /**
     * Get a CPG session's data by ID.
     *
     * @param cpgSessionId
     * @return
     * @throws Exception
     */
    public ClimateProdGenerateSessionData getCPGSession(String cpgSessionId)
            throws ClimateQueryException {
        ClimateProdGenerateSessionData cpgData = new ClimateProdGenerateSessionData();
        Map<String, Object> cols = cpgData.getColumnValues();
        String sql = getCPGSessionStatementByID(CPG_SESSION_TABLE_NAME,
                cols.keySet(), cpgSessionId);

        Object[] results = executeSQLQuery(sql, null);
        if (results == null || results.length < 1 || results.length > 1) {
            throw new ClimateQueryException(
                    "Unexpected results from query " + sql);
        }

        if (results[0] instanceof Object[]) {
            Object[] oa = (Object[]) results[0];
            ClimateProdGenerateSessionData rec = new ClimateProdGenerateSessionData();
            rec.setCpg_session_id((String) oa[0]);
            rec.setRun_type(((Number) oa[1]).intValue());
            rec.setProd_type(((Number) oa[2]).intValue());
            rec.setState(((Number) oa[3]).intValue());
            rec.setStatus(((Number) oa[4]).intValue());
            rec.setStatus_desc((String) oa[5]);
            rec.setGlobal_config((byte[]) oa[6]);
            rec.setProd_setting((byte[]) oa[7]);
            rec.setReport_data((byte[]) oa[8]);
            rec.setProd_data((byte[]) oa[9]);
            rec.setStart_at((Timestamp) oa[10]);
            rec.setLast_updated((Timestamp) oa[11]);
            rec.setPending_expire((Timestamp) oa[12]);

            return rec;
        } else {
            throw new ClimateQueryException(
                    "Unexpected return type from bias query, expected Object[], got "
                            + results[0].getClass().getName());
        }
    }

    /**
     * Get a CPG session's limited data by ID for GUI.
     *
     * @param cpgSessionId
     * @return
     * @throws ClimateQueryException
     */
    public ClimateProdGenerateSessionDataForView getCPGSessionForView(
            String cpgSessionId) throws ClimateQueryException {
        ClimateProdGenerateSessionDataForView cpgData = new ClimateProdGenerateSessionDataForView();
        Map<String, Object> cols = cpgData.getColumnValues();
        String sql = getCPGSessionStatementByID(CPG_SESSION_TABLE_NAME,
                cols.keySet(), cpgSessionId);

        Object[] results = executeSQLQuery(sql, null);
        if (results == null || results.length < 1 || results.length > 1) {
            throw new ClimateQueryException(
                    "Unexpected results from query " + sql);
        }

        if (results[0] instanceof Object[]) {
            Object[] oa = (Object[]) results[0];
            ClimateProdGenerateSessionDataForView rec = new ClimateProdGenerateSessionDataForView();
            rec.setCpg_session_id((String) oa[0]);
            rec.setRun_type(((Number) oa[1]).intValue());
            rec.setProd_type(
                    this.getPeriodTypeFromValue(((Number) oa[2]).intValue()));
            rec.setState(SessionState.valueOf(((Number) oa[3]).intValue()));
            rec.setStateStatus(new StateStatus(((Number) oa[4]).intValue(),
                    (String) oa[5]));
            rec.setStatus_desc((String) oa[5]);
            rec.setProd_data(bytesToClimateProdData((byte[]) oa[6]));
            rec.setStart_at((Timestamp) oa[7]);
            rec.setLast_updated((Timestamp) oa[8]);

            return rec;
        } else {
            throw new ClimateQueryException(
                    "Unexpected return type from bias query, expected Object[], got "
                            + results[0].getClass().getName());
        }
    }

    /**
     * update one or more columns of session data.
     *
     * @param cpgSessionId
     * @param updateCols
     * @return
     * @throws Exception
     */
    public int updateCPGSession(String cpgSessionId,
            Map<String, Object> updateCols) throws ClimateQueryException {
        List<String> columns = new ArrayList<>(updateCols.keySet());
        String sql = updateCPGSessionStatement(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>(updateCols);

        int rows = executeSQLUpdate(sql, parameters);
        if (rows <= 0) {
            throw new ClimateQueryException(
                    "Nothing updated on the table: " + CPG_SESSION_TABLE_NAME);
        }

        return rows;
    }

    /**
     * Update a CPG session with a new state/status.
     *
     * @param cpgSessionId
     * @param newState
     * @param status
     * @param statusDesc
     * @param lastUpdateDT
     * @return
     * @throws ClimateQueryException
     */
    public int updateCPGSessionStateWithStatus(String cpgSessionId,
            int newState, int status, String statusDesc,
            LocalDateTime lastUpdateDT) throws ClimateQueryException {
        List<String> columns = new ArrayList<>();
        columns.add(STATE_COLUMN);
        columns.add(STATUS_COLUMN);
        columns.add(STATUS_DESC_COLUMN);
        columns.add(LAST_UPDATED_COLUMN);
        String sql = updateCPGSessionStatement(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(STATE_COLUMN, newState);
        parameters.put(STATUS_COLUMN, status);
        parameters.put(STATUS_DESC_COLUMN, statusDesc);
        parameters.put(LAST_UPDATED_COLUMN, Timestamp.valueOf(lastUpdateDT));

        int rows = executeSQLUpdate(sql, parameters);
        if (rows <= 0) {
            throw new ClimateQueryException(
                    "Nothing updated on the table: " + CPG_SESSION_TABLE_NAME);
        }

        return rows;
    }

    /**
     * Update a CPG session with new state.
     *
     * @param cpgSessionId
     * @param newState
     * @param lastUpdateDT
     * @return
     * @throws ClimateQueryException
     */
    public int updateCPGSessionState(String cpgSessionId, int newState,
            LocalDateTime lastUpdateDT) throws ClimateQueryException {
        List<String> columns = new ArrayList<>();
        columns.add(STATE_COLUMN);
        columns.add(LAST_UPDATED_COLUMN);
        String sql = updateCPGSessionStatement(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(STATE_COLUMN, newState);
        parameters.put(LAST_UPDATED_COLUMN, Timestamp.valueOf(lastUpdateDT));

        int rows = executeSQLUpdate(sql, parameters);
        if (rows <= 0) {
            throw new ClimateQueryException(
                    "Nothing updated on the table: " + CPG_SESSION_TABLE_NAME);
        }

        return rows;
    }

    /**
     * Update a CPG session with new status.
     *
     * @param cpgSessionId
     * @param status
     * @param statusDesc
     * @param lastUpdateDT
     * @return
     * @throws ClimateQueryException
     */
    public int updateCPGSessionStatus(String cpgSessionId, int status,
            String statusDesc, LocalDateTime lastUpdateDT)
            throws ClimateQueryException {
        List<String> columns = new ArrayList<>();
        columns.add(STATUS_COLUMN);
        columns.add(STATUS_DESC_COLUMN);
        columns.add(LAST_UPDATED_COLUMN);
        String sql = updateCPGSessionStatement(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(STATUS_COLUMN, status);
        parameters.put(STATUS_DESC_COLUMN, statusDesc);
        parameters.put(LAST_UPDATED_COLUMN, Timestamp.valueOf(lastUpdateDT));

        int rows = executeSQLUpdate(sql, parameters);
        if (rows <= 0) {
            throw new ClimateQueryException(
                    "Nothing updated on the table: " + CPG_SESSION_TABLE_NAME);
        }

        return rows;
    }

    /**
     * Update the product report data for a session.
     *
     * @param cpgSessionId
     * @param reportData
     * @return
     * @throws ClimateQueryException
     */
    public int updateReportData(String cpgSessionId, byte[] reportData)
            throws ClimateQueryException {
        List<String> columns = new ArrayList<>();
        columns.add(REPORT_DATA_COLUMN);
        String sql = updateCPGSessionStatement(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(REPORT_DATA_COLUMN, reportData);

        int rows = executeSQLUpdate(sql, parameters);
        if (rows <= 0) {
            throw new ClimateQueryException(
                    "Nothing updated on the table: " + CPG_SESSION_TABLE_NAME);
        }

        return rows;
    }

    /**
     * Update product data for a session.
     *
     * @param cpgSessionId
     * @param prodData
     * @return
     * @throws ClimateQueryException
     */
    public int updateProdData(String cpgSessionId, byte[] prodData,
            LocalDateTime pendingExp) throws ClimateQueryException {
        List<String> columns = new ArrayList<>();
        columns.add(PROD_DATA_COLUMN);
        columns.add(PENDING_EXP_COLUMN);
        String sql = updateCPGSessionStatement(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(PROD_DATA_COLUMN, prodData);
        parameters.put(PENDING_EXP_COLUMN, Timestamp.valueOf(pendingExp));

        int rows = executeSQLUpdate(sql, parameters);
        if (rows <= 0) {
            throw new ClimateQueryException(
                    "Nothing updated on the table: " + CPG_SESSION_TABLE_NAME);
        }

        return rows;
    }

    /**
     * retrieve current Session State value from Database by given CPG Session
     * ID
     *
     * @param cpgSessionId
     * @return
     * @throws Exception
     */
    public int getCPGSessionState(String cpgSessionId)
            throws ClimateQueryException {
        List<String> columns = new ArrayList<>();
        columns.add(STATE_COLUMN);
        String sql = getCPGSessionStatementByID(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        Object[] results = executeSQLQuery(sql, parameters);

        if (results == null || results.length < 1 || results[0] == null) {
            throw new ClimateQueryException(
                    "No CPG Session found in the table: "
                            + CPG_SESSION_TABLE_NAME + " the Session ID = "
                            + cpgSessionId);
        }

        Object ob = results[0];
        return ((Number) ob).intValue();

    }

    /**
     * retrieve current Session status object from Database by given CPG Session
     * ID
     *
     * @param cpgSessionId
     * @return
     * @throws ClimateQueryException
     */
    public StateStatus getCurrentStateStatus(String cpgSessionId)
            throws ClimateQueryException {
        ArrayList<String> columns = new ArrayList<>();
        columns.add(STATUS_COLUMN);
        columns.add(STATUS_DESC_COLUMN);
        String sql = getCPGSessionStatementByID(CPG_SESSION_TABLE_NAME, columns,
                cpgSessionId);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        Object[] results = executeSQLQuery(sql, parameters);
        if (results == null || results.length < 1) {
            throw new ClimateQueryException(
                    "No CPG Session found in the table: "
                            + CPG_SESSION_TABLE_NAME + " the Session ID = "
                            + cpgSessionId);
        }

        StateStatus currentStatus = null;
        if (results[0] instanceof Object[]) {
            Object[] oa = (Object[]) results[0];
            currentStatus = new StateStatus(((Number) oa[0]).intValue(),
                    (String) oa[1]);
        }

        return currentStatus;
    }

    /**
     * Remove a terminated session.
     *
     * @param purgeThreshold
     * @return
     * @throws ClimateQueryException
     */
    public int purgeTerminatedCPGSession(LocalDateTime purgeThreshold) {
        String sql = getPurgeTerminatedCPGSessionStatement(
                CPG_SESSION_TABLE_NAME);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(LAST_UPDATED_COLUMN, Timestamp.valueOf(purgeThreshold));

        int rows = executeSQLUpdate(sql, parameters);
        return rows;
    }

    /**
     * Create a parameterized insert statement that can be passed to
     * {@link CoreDao#executeSQLUpdate(String, Map)}
     *
     * @param table
     * @param columns
     * @return
     */
    private static String getInsertStatement(String table,
            Collection<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(table);
        sb.append("(");
        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append(", ").append(iter.next());
            }
        }
        sb.append(") VALUES(");
        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            sb.append(":").append(iter.next());
            while (iter.hasNext()) {
                sb.append(", :").append(iter.next());
            }
        }
        sb.append(");");
        return sb.toString();
    }

    /**
     * Get query for all CPG sessions.
     *
     * @param table
     * @param columns
     * @return
     */
    private static String getAllCPGSessionStatement(String table,
            Collection<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append(", ").append(iter.next());
            }
        }
        sb.append(" FROM ").append(table);
        sb.append(";");
        return sb.toString();
    }

    /**
     * Get query for session by ID.
     *
     * @param table
     * @param columns
     * @return
     */
    private static String getCPGSessionStatementByID(String table,
            Collection<String> columns, String sessionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append(", ").append(iter.next());
            }
        }
        sb.append(" FROM ").append(table);
        sb.append(" WHERE ");
        sb.append(CPG_SESSION_ID_COLUMN).append("='").append(sessionId);
        sb.append("';");
        return sb.toString();
    }

    /**
     * Get query to update CPG session.
     *
     * @param table
     * @param columns
     * @return
     */
    private static String updateCPGSessionStatement(String table,
            Collection<String> columns, String sessionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(table);
        sb.append(" SET ");

        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            String col = iter.next();
            sb.append(col).append("=:").append(col);
            while (iter.hasNext()) {
                col = iter.next();
                sb.append(", ").append(col).append("=:").append(col);
            }
        }
        sb.append(" WHERE ");
        sb.append(CPG_SESSION_ID_COLUMN).append("='").append(sessionId);
        sb.append("';");
        return sb.toString();
    }

    /**
     * Get query for purging a CPG session.
     *
     * @param table
     * @return
     */
    private static String getPurgeTerminatedCPGSessionStatement(String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table);
        sb.append(" WHERE ");
        // Terminated Status
        sb.append(STATUS_COLUMN).append(">").append("1");
        sb.append(" AND ");
        // lastUpdated is older than the purge threshold
        sb.append(LAST_UPDATED_COLUMN).append("<:").append(LAST_UPDATED_COLUMN)
                .append("::timestamp");
        sb.append(";");
        return sb.toString();
    }

    /**
     * getPeriodTypeFromValue
     *
     * @param value
     * @return
     */
    private PeriodType getPeriodTypeFromValue(int value) {
        for (PeriodType pType : PeriodType.values()) {
            if (pType.getValue() == value) {
                return pType;
            }
        }
        // Invalid value
        return PeriodType.OTHER;
    }

    /**
     * Convert bytes to CPG products.
     *
     * @param cpBytes
     * @return
     * @throws ClimateQueryException
     */
    private ClimateProdData bytesToClimateProdData(byte[] cpBytes)
            throws ClimateQueryException {
        ClimateProdData cpd = null;
        try {

            // Formatted ProdData
            if (null != cpBytes) {
                cpd = (ClimateProdData) DynamicSerializationManager
                        .getManager(SerializationType.Thrift)
                        .deserialize(cpBytes);
            }

        } catch (SerializationException se) {
            String errMsg = "Failed to deserialize bytes to ClimateProdData.";
            logger.error(errMsg, se);
            throw new ClimateQueryException(errMsg, se);
        }

        return cpd;
    }
}

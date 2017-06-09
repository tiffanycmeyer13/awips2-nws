/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.database.dao.CoreDao;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProdSendRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;


/**
 * ClimateProdSendRecordDAO
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 26, 2017            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0	
 */
public class ClimateProdSendRecordDAO extends ClimateDAO {
    
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdSendRecordDAO.class);

    public static final String SEND_RECORD_TABLE_NAME = "sent_prod_record";

    public static final String CPG_SESSION_ID_COLUMN = "cpg_session_id";

    public static final String SEND_TIME_COLUMN = "send_time";

    public static final String START_PARAM = "startTime";

    public static final String END_PARAM = "endTime";
    
    public static final String PURGE_PARAM = "purgeTime";

    private static final Object LOCK = new Object();

    /**
     * Constructor
     */
    public ClimateProdSendRecordDAO() {
      super();
    }


    
    /**
     * insertSentClimateProdRecord
     * @param record
     * @return
     * @throws ClimateQueryException
     */
    public boolean insertSentClimateProdRecord(
            ClimateProdSendRecord record)
                    throws ClimateQueryException {
        boolean status = true;
        synchronized (LOCK) {

            Map<String, Object> columns = record.getColumnValues();

            String sql = getInsertStatement(SEND_RECORD_TABLE_NAME,
                    columns.keySet());
            if (logger.isPriorityEnabled(Priority.DEBUG)) {
                /* avoid formatting parameters if not in debug */
                logger.debug("SQL = " + sql + " PARAMETERS = " + columns);
            }
            try {
                status = (this.getDao().executeSQLUpdate(sql, columns) == 1);
            } catch (Exception e) {
                logger.error("SQL = " + sql + " PARAMETERS = " + columns);
                logger.error(
                        "Error writing to table: " + SEND_RECORD_TABLE_NAME, e);
                throw new ClimateQueryException(
                        "Error writing to table: " + SEND_RECORD_TABLE_NAME, e);
            }
        }
        return status;
    }

    /**
     * getSentClimateProductRecords
     * @param startDT
     * @param endDT
     * @return
     * @throws ClimateQueryException
     */
    public List<ClimateProdSendRecord> getSentClimateProductRecords(Date startDT, Date endDT)
            throws ClimateQueryException {
        List<ClimateProdSendRecord> recordList = new ArrayList<>();

        ClimateProdSendRecord cpsr = new ClimateProdSendRecord();
        Map<String, Object> columns = cpsr.getColumnValues();
        String sql = getClimateProdSendRecordStatement(SEND_RECORD_TABLE_NAME,
                columns.keySet());
        
        Map<String, Object> params = new HashMap<>();
        params.put(START_PARAM, new Timestamp(startDT.getTime()));
        params.put(END_PARAM, new Timestamp(endDT.getTime()));

        Object[] results = this.getDao().executeSQLQuery(sql, params);
        for (Object result : results) {
            if (result instanceof Object[]) {
                Object[] oa = (Object[]) result;
                ClimateProdSendRecord rec = new ClimateProdSendRecord();
                rec.setProd_id((String) oa[0]);
                rec.setPeriod_type((String) oa[1]);
                rec.setProd_type((String) oa[2]);
                rec.setFile_name((String) oa[3]);
                rec.setProd_text((String) oa[4]);
                rec.setSend_time((Timestamp) oa[5]);
                rec.setUser_id((String) oa[6]);

                recordList.add(rec);
            } else {
                throw new ClimateQueryException(
                        "Unexpected return type from the query, expected Object[], got "
                                + result.getClass().getName());
            }
        }
        return recordList;
    }
    
    /**
     * purgeSentProductRecords
     * 
     * @param purgeThreshold
     * @return
     * @throws ClimateQueryException
     */
    public int purgeSentProductRecords(LocalDateTime purgeThreshold)
            throws ClimateQueryException {
        String sql = getPurgeSentRecordStatement(
                SEND_RECORD_TABLE_NAME);

        // Parameters used by setXXX
        Map<String, Object> parameters = new HashMap<>();

        parameters.put(PURGE_PARAM, Timestamp.valueOf(purgeThreshold));

        int rows = this.getDao().executeSQLUpdate(sql, parameters);
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
     * getClimateProdSendRecordStatement
     * 
     * @param table
     * @param columns
     * @return
     */
    private static String getClimateProdSendRecordStatement(String table,
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
        sb.append(" WHERE ").append(SEND_TIME_COLUMN).append(" BETWEEN ");
        sb.append(":").append(START_PARAM).append("::timestamp");
        sb.append(" AND ").append(":").append(END_PARAM).append("::timestamp");
        sb.append(";");
        return sb.toString();
    }
    
    /**
     * getPurgeSentRecordStatement
     * @param table
     * @return
     */
    private static String getPurgeSentRecordStatement(String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(table);
        sb.append(" WHERE ");
        // lastUpdated is older than the purge threshold
        sb.append(SEND_TIME_COLUMN).append("<:").append(PURGE_PARAM)
                .append("::timestamp");
        sb.append(";");
        return sb.toString();
    }



    
    /**
     * Test ONLY
     * @param args
     */
    public static void main(String[] args) {
        ClimateProdSendRecord cpgs = new ClimateProdSendRecord();
        Map<String, Object> columns = cpgs.getColumnValues();
        String sql = getInsertStatement(SEND_RECORD_TABLE_NAME,
                columns.keySet());
        
        System.out.println(sql);
        
        sql = getClimateProdSendRecordStatement(SEND_RECORD_TABLE_NAME, columns.keySet());
        
        System.out.println(sql);

        
    }
    
    
}



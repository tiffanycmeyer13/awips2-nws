/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.dao.CoreDao;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAO;

/**
 * Dao for ClimateReport.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 23, 2016            pwang       Initial creation
 * 27 JAN 2017  28609      amoore      FSS table insertions.
 * 21 FEB 2017  28609      amoore      Bug fixes from testing.
 * 22 FEB 2017  28609      amoore      Address TODOs.
 * 09 MAY 2017  33104      amoore      Change extension type to common.
 * 07 SEP 2017  37754      amoore      Exceptions instead of boolean returns.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateReportDao extends ClimateDAO {
    /**
     * Report table.
     */
    public static final String REPORT_TABLE_NAME = "rpt";

    /**
     * Lock object for synchronization.
     */
    private static final Object LOCK = new Object();

    /**
     * Constructor.
     */
    public ClimateReportDao() {
        super();
    }

    /**
     * 
     * @param report
     * @return
     * @throws ClimateException
     */
    public void storeToTable(ClimateReport report) throws ClimateException {
        // insert to report table
        synchronized (LOCK) {
            Map<String, Object> parameters = report.getColumnValues();
            String sql = getInsertStatement(REPORT_TABLE_NAME,
                    parameters.keySet());
            try {
                int changes = getDao().executeSQLUpdate(sql, parameters);

                if (changes != 1) {
                    throw new ClimateQueryException(
                            "Expected query to update 1 row, but updated ["
                                    + changes + "] rows instead.");
                }
            } catch (Exception e) {
                throw new ClimateQueryException(
                        "Error writing to rpt table with query: [" + sql
                                + "] and parameters: [" + parameters + "]",
                        e);
            }
        }
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
        sb.append(" (");
        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            sb.append(iter.next());
            while (iter.hasNext()) {
                sb.append(',').append(iter.next());
            }
        }
        sb.append(") VALUES (");
        if (!columns.isEmpty()) {
            Iterator<String> iter = columns.iterator();
            sb.append(':').append(iter.next());
            while (iter.hasNext()) {
                sb.append(",:").append(iter.next());
            }
        }
        sb.append(");");
        return sb.toString();
    }

    /**
     * 
     * @return
     */
    public boolean purgeTable(int purgeHours) {
        String queryString = "delete from rpt where nominal < :nominal";

        Map<String, Object> params = new HashMap<>();
        Calendar c = TimeUtil.newGmtCalendar();
        c.add(Calendar.HOUR_OF_DAY, -purgeHours);
        params.put("nominal", c);

        try {
            getDao().executeSQLUpdate(queryString, params);
        } catch (Exception e) {
            logger.error(
                    "Error in purging climate database rpt table with query: ["
                            + queryString + "] and map: [" + params + "]",
                    e);
            return false;
        }

        return true;
    }
}

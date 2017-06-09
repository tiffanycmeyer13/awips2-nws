/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Query table climate_day_config.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ --------- ----------- --------------------------
 * 10/05/2016   20639     wkwock      Initial creation
 * 16 MAR 2017  30162     amoore      Fix logging. Use StringBuilder.
 * 18 APR 2017  33104     amoore      Use query maps now that DB issue is fixed.
 * 03 MAY 2017  33104     amoore      Use abstract map.
 * 11 MAY 2017  33104     amoore      Fix query parameter.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class ClimateDayConfigDAO extends ClimateDAO {
    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateDayConfigDAO.class);

    /**
     * Constructor.
     */
    public ClimateDayConfigDAO() {
        super();
    }

    /**
     * fetch from climate_day_config table
     * 
     * @param timeOfDay
     * @return
     * @throws Exception
     */
    public List<String> fetchClimateDayConfig(String timeOfDay)
            throws Exception {
        List<String> stationNames = new ArrayList<>();

        StringBuilder query = new StringBuilder("SELECT prod_id from ");
        query.append(ClimateDAOValues.CLIMATE_DAY_CONFIG_TABLE_NAME);
        query.append(" where time_of_day = :timeOfDay");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("timeOfDay", timeOfDay);

        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    paramMap);
            if ((results != null) && (results.length > 0)) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        Object[] items = (Object[]) result;
                        for (Object item : items) {
                            stationNames.add((String) item);
                        }
                    } catch (Exception e) {
                        throw new Exception(
                                "Unexpected return column type from query: ["
                                        + query + "] and map: [" + paramMap
                                        + "]",
                                e);
                    }
                } else {
                    throw new Exception("Unexpected return type from query: ["
                            + query + "] and map: [" + paramMap + "]");
                }
            } else {
                logger.warn("Empty or null results from query: [" + query
                        + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new Exception("Error from query: [" + query + "] and map: ["
                    + paramMap + "]", e);
        }

        return stationNames;
    }

}
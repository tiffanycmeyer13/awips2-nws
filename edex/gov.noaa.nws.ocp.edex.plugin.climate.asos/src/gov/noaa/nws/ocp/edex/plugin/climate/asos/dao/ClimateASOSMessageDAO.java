/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos.dao;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.asos.ClimateASOSMessageRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAO;

/**
 * Data Access class for Climate ASOS Daily / Monthly Summary Messages
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 12, 2016 16962      pwang       Initial creation
 * 05 MAY 2017  33104      amoore      Clean up SQL and logging.
 * 08 MAY 2017  33104      amoore      Extend common Climate functionality.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class ClimateASOSMessageDAO extends ClimateDAO {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateASOSMessageDAO.class);

    private static final Object LOCK = new Object();

    /**
     * Constructor.
     * 
     * @param pluginName
     */
    public ClimateASOSMessageDAO() {
        super();
    }

    /**
     * Save DSM / MSM record to db table
     * 
     * @param report
     * @return
     */
    public boolean storeToTable(ClimateASOSMessageRecord record) {

        boolean status = true;
        if (record == null) {
            logger.warn("Cannot store null ASOS message record.");
            return false;
        }

        synchronized (LOCK) {

            Map<String, Object> queryExistingParams = new HashMap<>();

            String queryExisting = record
                    .queryExistingRecordSQL(queryExistingParams);

            try {
                long count = ((Number) queryForOneValue(queryExisting,
                        queryExistingParams, -1l)).longValue();

                String updateQuery = null;

                Map<String, Object> queryUpdateParams = new HashMap<>();

                if (count > 0) {
                    logger.info("Record ASOS matching station code: ["
                            + record.getStationCode()
                            + "] for the given date already exists. Updating.");
                    updateQuery = record.toUpdateSQL(queryUpdateParams);
                } else {
                    logger.info("No ASOS record matching station code: ["
                            + record.getStationCode()
                            + "] for the given date exists. Inserting new record.");
                    updateQuery = record.toInsertSQL(queryUpdateParams);
                }

                if (updateQuery != null && !updateQuery.isEmpty()) {
                    try {
                        status = (getDao().executeSQLUpdate(updateQuery,
                                queryUpdateParams) == 1);
                    } catch (Exception e) {
                        throw new ClimateQueryException("Error with query: ["
                                + updateQuery + "] and map: ["
                                + queryUpdateParams + "]", e);
                    }
                } else {
                    logger.warn("No update query applicable for ASOS message.");
                }
            } catch (ClimateQueryException e) {
                logger.error("Error with inner query.", e);
            } catch (Exception e) {
                logger.error(
                        "Error with ASOS query: [" + queryExisting
                                + "] and map: [" + queryExistingParams + "]",
                        e);
            }
        }
        return status;
    }

}

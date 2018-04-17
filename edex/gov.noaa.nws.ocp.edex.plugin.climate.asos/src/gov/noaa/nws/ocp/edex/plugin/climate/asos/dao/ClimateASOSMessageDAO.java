/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos.dao;

import java.util.HashMap;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.asos.ClimateASOSMessageRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
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
 * 07 SEP 2017  37754      amoore      Exceptions instead of boolean returns.
 * 03 NOV 2017  36736      amoore      Get rid of unneeded synchronization.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class ClimateASOSMessageDAO extends ClimateDAO {

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
     * @throws ClimateInvalidParameterException
     * @throws ClimateQueryException
     */
    public void storeToTable(ClimateASOSMessageRecord record)
            throws ClimateInvalidParameterException, ClimateQueryException {
        if (record == null) {
            throw new ClimateInvalidParameterException(
                    "Cannot store null ASOS message record.");
        }

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
                    int changes = getDao().executeSQLUpdate(updateQuery,
                            queryUpdateParams);

                    if (changes != 1) {
                        throw new ClimateQueryException(
                                "Query expected to update 1 row, but updated: ["
                                        + changes + "] rows.");
                    }
                } catch (Exception e) {
                    throw new ClimateQueryException(
                            "Error with query: [" + updateQuery + "] and map: ["
                                    + queryUpdateParams + "]",
                            e);
                }
            } else {
                throw new ClimateInvalidParameterException(
                        "No update query applicable for ASOS message, "
                                + "as either station or date information may be missing.");
            }
        } catch (ClimateQueryException e) {
            throw new ClimateQueryException("Error with inner query.", e);
        } catch (Exception e) {
            throw new ClimateQueryException(
                    "Error with ASOS query: [" + queryExisting + "] and map: ["
                            + queryExistingParams + "]",
                    e);
        }
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.HashMap;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Query station_location table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/06/2016   20639     wkwock     Initial creation
 * 16 MAR 2017  30162     amoore     Fix logging.
 * 21 MAR 2017  20632     amoore     Amend logging/comments for DB null
 *                                   returns. Use StringBuilder.
 * 19 APR 2017  33104     amoore     Use query maps now that DB issue is fixed.
 * 03 MAY 2017  33104     amoore     Use abstract map.
 * 11 MAY 2017  33104     amoore     Minor query param fix.
 * 08 SEP 2017  37809     amoore     For queries, cast to Number rather than specific number type.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class StationLocationDAO extends ClimateDAO {

    /**
     * 
     * @param stationCode
     * @return
     * @throws Exception
     */
    public Station fetchStation(String stationCode) throws Exception {
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(
                "station_id, first_coord_val, second_coord_val, station_code,  station_name, hours_ahead_utc ");
        query.append(" FROM ")
                .append(ClimateDAOValues.STATION_LOCATION_TABLE_NAME);
        query.append(" where station_code= :stationCode");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("stationCode", stationCode);

        Station station = null;
        try {
            Object[] results = getDao().executeSQLQuery(query.toString(),
                    paramMap);
            if ((results != null) && (results.length > 0)) {
                Object result = results[0];
                if (result instanceof Object[]) {
                    try {
                        Object[] oa = (Object[]) result;
                        station = new Station();
                        station.setInformId(((Number) oa[0]).intValue());
                        // any of these values could be null, but are necessary
                        station.setDlat(((Number) oa[1]).doubleValue());
                        station.setDlon(((Number) oa[2]).doubleValue());
                        station.setIcaoId((String) oa[3]);
                        station.setStationName((String) oa[4]);
                        station.setNumOffUTC(((Number) oa[5]).shortValue());
                        station.setStdAllYear((short) 0);
                    } catch (NumberFormatException nfe) {
                        throw new Exception("Failed to parse a number", nfe);
                    } catch (Exception e) {
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
                logger.warn("Could not get station location data using query: ["
                        + query + "] and map: [" + paramMap + "]");
            }
        } catch (Exception e) {
            throw new Exception("Error querying the database using query: ["
                    + query + "] and map: [" + paramMap + "]", e);
        }

        return station;
    }
}

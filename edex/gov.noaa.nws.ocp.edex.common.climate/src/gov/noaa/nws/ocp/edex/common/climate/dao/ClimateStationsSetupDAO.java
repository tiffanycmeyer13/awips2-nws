/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;

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
 * 14 OCT 2016  21378      amoore      Added getting station code by station ID (inform ID).
 * 04 OCT 2016  20639      wkwock      Add update/insert/replace methods.
 * 20 OCT 2016  21378      amoore      Move station code query to parent class.
 * 16 MAR 2017  30162      amoore      Fix logging and comments.
 * 21 MAR 2017  20632      amoore      Comment for null values.
 * 24 APR 2017  30144      amoore      Remove unused method. Use query maps for update/
 *                                     insert/delete. Clean up.
 * 25 APR 2017  30144      amoore      Clean up query.
 * 02 MAY 2017  33104      amoore      Refactor query to constant.
 * 17 MAY 2017  33104      amoore      Use cleaner logic for replacing/updating stations.
 * 20 JUN 2017  33104      amoore      Address review comments.
 * 08 SEP 2017  37809      amoore      For queries, cast to Number rather than specific number type.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateStationsSetupDAO extends ClimateDAO {
    /**
     * Query for all stations.
     */
    private static final String MASTER_STATIONS_QUERY = "SELECT station_id, station_code, cli_sta_name, office_id,"
            + " latitude_n, longitude_e, hours_ahead_utc, std_all_year FROM "
            + ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME;

    /**
     * Query for all stations, IDs only.
     */
    private static final String MASTER_STATIONS_IDS_QUERY = "SELECT station_id FROM "
            + ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME;

    /**
     * Constructor.
     */
    public ClimateStationsSetupDAO() {
        super();
    }

     /**
     * Converted from get_master_stations.ecpp
     * 
     * Original comments:
     * 
     * <pre>
     *  void get_master_stations (  station_list   all_stations,
     *                              int        num_all_stations
     *                                      )
     * 
     *    Jason Tuell       PRC/TDL             HP 9000/7xx
     * 
     *    FUNCTION DESCRIPTION
     *    ====================
     * 
     *  This function updates the daily climate data base.  It enters
     *       the contents of yesterday into the daily observed climate data
     *       base.
     * 
     *     Revised
     *     Teresa Peachey/      1/20/05    Converted INFORMIX to POSTGRES SQL 
     *     Manan Dalal
     * </pre>
     * 
     * @return list of stations in the DB.
     * @throws ClimateQueryException
     */
    
    public List<Station> getMasterStations() throws ClimateQueryException {
        List<Station> stations = new ArrayList<>();
        try {
            Object[] results = getDao().executeSQLQuery(MASTER_STATIONS_QUERY);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Object[]) {
                        Object[] oa = (Object[]) result;
                        try {
                            /*
                             * Any values except station ID could be null, but
                             * they are all necessary for calculations.
                             */
                            Station station = new Station();
                            station.setInformId(((Number) oa[0]).intValue());
                            station.setIcaoId((String) oa[1]);
                            station.setStationName((String) oa[2]);
                            station.setDlat(((Number) oa[4]).doubleValue());
                            station.setDlon(((Number) oa[5]).doubleValue());
                            station.setNumOffUTC(((Number) oa[6]).shortValue());
                            station.setStdAllYear(
                                    ((Number) oa[7]).shortValue());

                            stations.add(station);
                        } catch (NullPointerException e) {
                            throw new ClimateQueryException(
                                    "Unexpected null result with query: ["
                                            + MASTER_STATIONS_QUERY
                                            + "]. All station data must be non-null.",
                                    e);
                        }
                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from getMasterStations query, expected Object[], got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("No stations found in database using query: ["
                        + MASTER_STATIONS_QUERY + "]");
            }
        } catch (ClimateQueryException e) {
            throw new ClimateQueryException(
                    "Error querying the climate database  with query: ["
                            + MASTER_STATIONS_QUERY + "]",
                    e);
        }
        return stations;
    }

    /**
     * @return list of all station IDs
     * @throws ClimateQueryException
     */
    public List<Integer> getMasterStationIDs() throws ClimateQueryException {
        List<Integer> ids = new ArrayList<>();
        try {
            Object[] results = getDao()
                    .executeSQLQuery(MASTER_STATIONS_IDS_QUERY);
            if ((results != null) && (results.length >= 1)) {
                for (Object result : results) {
                    if (result instanceof Number) {
                        try {
                            ids.add(((Number) result).intValue());
                        } catch (NullPointerException e) {
                            throw new ClimateQueryException(
                                    "Unexpected null result with query: ["
                                            + MASTER_STATIONS_IDS_QUERY + "].",
                                    e);
                        } catch (Exception e) {
                            // if casting failed
                            throw new ClimateQueryException(
                                    "Unexpected return column type.", e);
                        }

                    } else {
                        throw new ClimateQueryException(
                                "Unexpected return type from getMasterStationIDs query, expected Integer, got "
                                        + result.getClass().getName());
                    }
                }
            } else {
                logger.warn("No IDS found in database using query: ["
                        + MASTER_STATIONS_IDS_QUERY + "]");
            }
        } catch (ClimateQueryException e) {
            throw new ClimateQueryException(
                    "Error querying the climate database  with query: ["
                            + MASTER_STATIONS_IDS_QUERY + "]",
                    e);
        }
        return ids;
    }

    /**
     * Update/insert rows in cli_sta_setup with stations. Remove existing
     * stations that are not present in the new list.
     * 
     * @param newStations
     * @return query status
     * @throws Exception
     */
    public boolean replaceMasterStations(List<Station> newStations)
            throws Exception {
        // TODO A1 uses env(ADAPT_SITE_ID)
        String officeId = System.getProperty("AW_SITE_IDENTIFIER");

        boolean success = true;

        List<Integer> newStationIDs = new ArrayList<>();

        List<Integer> oldStationIDs = getMasterStationIDs();

        StringBuilder delete = new StringBuilder("DELETE FROM ")
                .append(ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME)
                .append(" WHERE station_id NOT IN (");
        Map<String, Object> deleteParams = new HashMap<>();

        try {
            StringBuilder insertQuery = new StringBuilder();
            Map<String, Object> insertParamMap = new HashMap<>();
            try {
                // insert new rows
                for (Station station : newStations) {
                    // add ID to list
                    newStationIDs.add(station.getInformId());

                    if (!oldStationIDs.contains(station.getInformId())) {
                        // insert
                        insertQuery.append("INSERT INTO ");
                        insertQuery.append(
                                ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME);
                        insertQuery.append(" VALUES (");
                        insertQuery.append(":stationID, ");
                        insertParamMap.put("stationID", station.getInformId());
                        insertQuery.append(":icao, ");
                        insertParamMap.put("icao", station.getIcaoId());
                        insertQuery.append(":name, ");
                        insertParamMap.put("name", station.getStationName());
                        insertQuery.append(":office, ");
                        insertParamMap.put("office", officeId);
                        insertQuery.append(":lat, ");
                        insertParamMap.put("lat", station.getDlat());
                        insertQuery.append(":lon, ");
                        insertParamMap.put("lon", station.getDlon());
                        insertQuery.append(":utc, ");
                        insertParamMap.put("utc", station.getNumOffUTC());
                        insertQuery.append(":std)");
                        insertParamMap.put("std", station.getStdAllYear());
                    } else {
                        // update
                        insertQuery.append("UPDATE ")
                                .append(ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME)
                                .append(" SET ");
                        insertQuery.append("station_code = :icao, ");
                        insertParamMap.put("icao", station.getIcaoId());
                        insertQuery.append("cli_sta_name = :name, ");
                        insertParamMap.put("name", station.getStationName());
                        insertQuery.append("office_id = :office, ");
                        insertParamMap.put("office", officeId);
                        insertQuery.append("latitude_n = :lat, ");
                        insertParamMap.put("lat", station.getDlat());
                        insertQuery.append("longitude_e = :lon, ");
                        insertParamMap.put("lon", station.getDlon());
                        insertQuery.append("hours_ahead_utc = :utc, ");
                        insertParamMap.put("utc", station.getNumOffUTC());
                        insertQuery.append("std_all_year = :std ");
                        insertParamMap.put("std", station.getStdAllYear());

                        insertQuery.append(" WHERE station_id = :stationID");
                        insertParamMap.put("stationID", station.getInformId());
                    }

                    boolean thisQuery = (getDao().executeSQLUpdate(
                            insertQuery.toString(), insertParamMap) == 1);
                    success = success && thisQuery;

                    // clear
                    insertQuery = new StringBuilder();
                    insertParamMap.clear();
                }
            } catch (Exception e) {
                throw new ClimateQueryException("Error with query: ["
                        + insertQuery + "] and map: [" + insertParamMap + "]",
                        e);
            }

            for (int i = 0; i < newStationIDs.size(); i++) {
                int newID = newStationIDs.get(i);
                delete.append(":id").append(i).append(",");
                deleteParams.put("id" + i, newID);
            }

            if (!deleteParams.isEmpty()) {
                // remove last comma
                delete.deleteCharAt(delete.length() - 1);
                // end parenthesis
                delete.append(")");
            } else {
                logger.warn(
                        "No new stations were present in the updated stations list. All stations will be removed.");
                delete = new StringBuilder("DELETE FROM ").append(
                        ClimateDAOValues.CLIMATE_STATION_SETUP_TABLE_NAME);
                deleteParams.clear();
            }
            // remove stations not in the updated list from the table
            getDao().executeSQLUpdate(delete.toString(), deleteParams);
        } catch (ClimateQueryException e) {
            throw new Exception("Error with inner query", e);
        } catch (Exception e) {
            throw new Exception("Error with query: [" + delete + "] and map: ["
                    + deleteParams + "]", e);
        }

        return success;
    }
}
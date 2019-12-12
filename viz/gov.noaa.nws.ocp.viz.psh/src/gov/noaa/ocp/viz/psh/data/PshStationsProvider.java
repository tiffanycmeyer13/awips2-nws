/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.ocp.viz.psh.data;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.localization.psh.PshStation;
import gov.noaa.nws.ocp.common.localization.psh.PshStations;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Provider for PSH Stations.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 04, 2017 #36367     astrakovsky  Initial creation.
 * Aug 29, 2017 #37366     astrakovsky  Fixed error getting lat/lon and added method 
 *                                      for getting station codes only.
 * Nov 14, 2017 #40296     astrakovsky  Improved station queries and result parsing.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshStationsProvider {
    
    /**
     * SQL Queries
     */
    // Get US stations with ICAO and SAO identifiers (Metar)
    private static String METAR_STATION_QUERY = "SELECT icao, name, state, AsBinary(the_geom) FROM awips.common_obs_spatial "
            + "WHERE country = 'US' AND (catalogtype = 1 OR catalogtype = 2)";

    // Get US Metar stations, plus stations with WFO, WMO, and Mesonet locations
    private static String NON_METAR_STATION_QUERY = "SELECT icao, stationid, name, state, AsBinary(the_geom) FROM awips.common_obs_spatial "
            + "WHERE country = 'US' AND (catalogtype = 1 OR catalogtype = 2 OR catalogtype = 10 OR catalogtype = 20 OR catalogtype = 1000 OR catalogtype = 1001)";

    // Get stations at drifting buoy, fixed buoy, and coastal marine locations
    private static String MARINE_STATION_QUERY = "SELECT stationid, name, state, AsBinary(the_geom) FROM awips.common_obs_spatial "
            + "WHERE catalogtype = 31 OR catalogtype = 32 OR catalogtype = 33";

    // Get all US station codes for autocomplete in cities setup
    private static String ALL_STATION_CODES_QUERY = "SELECT icao, stationid FROM awips.common_obs_spatial "
            + "WHERE country = 'US' OR catalogtype = 31 OR catalogtype = 32 OR catalogtype = 33";
    
    /**
     * Load PSH METAR stations from database.
     *
     * @return
     */
    public static PshStations getMetarStations() {

        PshStations metarDBStations = new PshStations();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(METAR_STATION_QUERY, "metadata");
        for (Object[] result : queryResults) {

            PshStation stn = PshStation.getDefaultStation();

            // extract strings from query results
            if (result[0] != null) {
                stn.setCode((String) result[0]);
            }

            // get state from own field if present
            if (result[2] != null) {
                stn.setState((String) result[2]);
            }
            // get name and parse state if possible
            if (result[1] != null) {
                if (stn.getState().isEmpty()
                        && ((String) result[1]).matches(".*(,| ) [A-Z]{2}$")) {
                    // if state not already loaded, copy to state field and remove from name if present
                    stn.setState(((String) result[1])
                            .substring(((String) result[1]).length() - 2));
                    stn.setName(((String) result[1])
                            .substring(0, ((String) result[1]).length() - 4)
                            .trim());
                }
                else if (!stn.getState().isEmpty() && ((String) result[1])
                        .matches(".*(,| ) " + stn.getState() + "$")) {
                    // if state already loaded, remove from name if present
                    stn.setName(((String) result[1])
                            .substring(0,
                                    ((String) result[1]).length()
                                            - stn.getState().length() - 2)
                            .trim());
                }
                else {
                    // if name not loaded yet, get it now
                    stn.setName((String) result[1]);
                }
            }
            
            // extract and parse coordinate from query results
            if (result[3] != null) {
                Coordinate stationCoordinate = PshUtil.getCoordinate(result[3]);
                stn.setLat(stationCoordinate.y);
                stn.setLon(stationCoordinate.x);
            }

            stn.buildFullName();
            metarDBStations.getStations().add(stn);
        }
        return metarDBStations;
    }
    
    /**
     * Load PSH Non-METAR stations from database.
     *
     * @return
     */
    public static PshStations getNonMetarStations() {

        PshStations nonMetarDBStations = new PshStations();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(NON_METAR_STATION_QUERY, "metadata");
        for (Object[] result : queryResults) {

            PshStation stn = PshStation.getDefaultStation();

            // extract strings from query results
            if (result[0] != null && !((String) result[0]).isEmpty()) {
                stn.setCode((String) result[0]);
            }
            else if (result[1] != null) {
                stn.setCode((String) result[1]);
            }

            // get state from own field if present
            if (result[3] != null) {
                stn.setState((String) result[3]);
            }
            // get name and parse state if possible
            if (result[2] != null) {
                if (stn.getState().isEmpty()
                        && ((String) result[2]).matches(".*(,| ) [A-Z]{2}$")) {
                    // if state not already loaded, copy to state field and
                    // remove from name if present
                    stn.setState(((String) result[2])
                            .substring(((String) result[2]).length() - 2));
                    stn.setName(((String) result[2])
                            .substring(0, ((String) result[2]).length() - 4)
                            .trim());
                } else if (!stn.getState().isEmpty() && ((String) result[2])
                        .matches(".*(,| ) " + stn.getState() + "$")) {
                    // if state already loaded, remove from name if present
                    stn.setName(((String) result[2])
                            .substring(0,
                                    ((String) result[2]).length()
                                            - stn.getState().length() - 2)
                            .trim());
                }
                else {
                    // if name not loaded yet, get it now
                    stn.setName((String) result[2]);
                }
            }
            
            // extract and parse coordinate from query results
            if (result[4] != null) {
                Coordinate stationCoordinate = PshUtil.getCoordinate(result[4]);
                stn.setLat(stationCoordinate.y);
                stn.setLon(stationCoordinate.x);
            }

            stn.buildFullName();
            nonMetarDBStations.getStations().add(stn);
        }
        return nonMetarDBStations;
    }
    
    /**
     * Load PSH Marine stations from database.
     *
     * @return
     */
    public static PshStations getMarineStations() {

        PshStations marineDBStations = new PshStations();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(MARINE_STATION_QUERY, "metadata");
        for (Object[] result : queryResults) {

            PshStation stn = PshStation.getDefaultStation();

            // extract strings from query results
            if (result[0] != null) {
                stn.setCode((String) result[0]);
            }
            if (result[1] != null) {
                stn.setName((String) result[1]);
            }
            if (result[2] != null) {
                stn.setState((String) result[2]);
            }

            // extract and parse coordinate from query results
            if (result[3] != null) {
                Coordinate stationCoordinate = PshUtil.getCoordinate(result[3]);
                stn.setLat(stationCoordinate.y);
                stn.setLon(stationCoordinate.x);
            }

            stn.buildFullName();
            marineDBStations.getStations().add(stn);
        }
        return marineDBStations;
    }
    
    /**
     * Load all PSH station codes from database.
     *
     * @return
     */
    public static List<String> getAllStationCodes() {

        List<String> codes = new ArrayList<>();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(ALL_STATION_CODES_QUERY, "metadata");
        for (Object[] result : queryResults) {
            
            // extract string from query results
            if (result[0] != null && !((String) result[0]).isEmpty()) {
                codes.add((String) result[0]);
            }
            else if (result[1] != null) {
                codes.add((String) result[1]);
            }
            
        }
        return codes;
    }
    
}

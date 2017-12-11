/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.ocp.viz.psh.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.localization.psh.PshCities;
import gov.noaa.nws.ocp.common.localization.psh.PshCity;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Provider for PSH Cities.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 08, 2017 #35737     astrakovsky  Initial creation.
 * Aug 29, 2017 #37366     astrakovsky  Fixed error getting lat/lon.
 * Oct 27, 2017 #39988     astrakovsky  Improved city query to include counties and CWAs.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCitiesProvider {

    /**
     * SQL Queries
     */
    private static final String CITIES_QUERY = "SELECT name, sfips, cfips, st, AsBinary(the_geom) FROM mapdata.city "
            + "WHERE name > ''";

    private static final String COUNTIES_QUERY = "SELECT fips, countyname, cwa FROM mapdata.county "
            + "WHERE countyname > ''";

    /**
     * Load PSH cities from database.
     *
     * @return
     */
    public static PshCities getCities() {

        PshCities dbCities = new PshCities();
        Map<String, String> counties = new HashMap<>();
        List<Object[]> queryResults = new ArrayList<>();

        // get cities
        queryResults = PshUtil.executeSQLQuery(CITIES_QUERY, "maps");
        for (Object[] result : queryResults) {

            PshCity city = PshCity.getDefaultCity();

            // extract strings from query results
            // city name
            if (result[0] != null) {
                city.setName((String) result[0]);
            }
            // county FIPS
            if (result[1] != null && result[2] != null) {
                city.setCounty((String) result[1] + (String) result[2]);
            }
            // state
            if (result[3] != null) {
                city.setState((String) result[3]);
            }

            // extract and parse coordinate from query results
            if (result[4] != null) {
                Coordinate stationCoordinate = PshUtil.getCoordinate(result[4]);
                city.setLat((float) stationCoordinate.y);
                city.setLon((float) stationCoordinate.x);
            }

            dbCities.getCities().add(city);
        }

        // get counties
        queryResults = PshUtil.executeSQLQuery(COUNTIES_QUERY, "maps");
        for (Object[] result : queryResults) {

            // extract strings from query results
            if (result[0] != null && result[1] != null && result[2] != null) {
                counties.put((String) result[0],
                        (String) result[1] + "::" + (String) result[2]);
            } else if (result[0] != null && result[1] != null) {
                counties.put((String) result[0], (String) result[1]);
            }
        }

        // find a city's county & CWA by a county's FIPS
        String countyCWA;
        for (PshCity city : dbCities.getCities()) {
            countyCWA = counties.get(city.getCounty());
            if (countyCWA != null) {
                if (countyCWA.contains("::")) {
                    String[] strings = countyCWA.split("::");
                    city.setCounty(strings[0]);
                    city.setCWA(strings[1]);
                } else {
                    city.setCounty(countyCWA);
                    city.setCWA("");
                }
            } else {
                city.setCounty("");
                city.setCWA("");
            }
        }

        return dbCities;
    }

}
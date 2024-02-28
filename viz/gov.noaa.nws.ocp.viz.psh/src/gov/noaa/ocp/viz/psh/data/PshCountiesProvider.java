/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.ocp.viz.psh.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import gov.noaa.nws.ocp.common.localization.psh.PshCounties;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Provider for PSH Counties.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 09, 2017 #35737     astrakovsky  Initial creation.
 * Sep 06, 2017 #36923     astrakovsky  Added methods for loading county geometry data.
 * Dec 21, 2020 #21179     J. Rohwein   update method signatures  AsBinary -> ST_AsBinary 
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCountiesProvider {

    /**
     * Logger.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PshCountiesProvider.class);

    /**
     * SQL Queries
     */
    private static String COUNTIES_QUERY = "SELECT countyname, lat, lon, ST_AsBinary(the_geom) FROM mapdata.county";

    private static String COUNTIES_STRINGS_QUERY = "SELECT countyname FROM mapdata.county";

    /**
     * Load PSH Counties from database.
     *
     * @return
     */
    public static List<PshCounty> getCounties() {

        List<PshCounty> counties = new ArrayList<>();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(COUNTIES_QUERY, "maps");
        for (Object[] result : queryResults) {

            PshCounty county = new PshCounty();

            // extract strings from query results
            if (result[0] != null) {
                county.setName((String) result[0]);
            }

            // extract and parse centroid from query results
            if (result[1] != null && result[2] != null) {
                county.setCentroid(
                        new Coordinate(((Number) result[2]).doubleValue(),
                                ((Number) result[1]).doubleValue()));
            }

            // extract and parse geometry from query results
            if (result[3] != null) {

                byte[] bytes = (byte[]) result[3];
                WKBReader reader = new WKBReader();
                Geometry geo = null;
                try {
                    geo = reader.read(bytes);
                    county.setCentroid(geo.getCentroid().getCoordinate());
                    county.setShape(geo);
                } catch (ParseException e) {
                    statusHandler.handle(Priority.PROBLEM, "", e);
                    return null;
                }
            }

            // add county to map
            counties.add(county);
        }
        return counties;
    }

    /**
     * Load PSH counties from database matching the specified keys.
     *
     * @return
     */
    public static List<PshCounty> getCounties(Set<String> keys) {

        List<PshCounty> counties = new ArrayList<>();
        List<Object[]> queryResults = new ArrayList<>();

        Map<String, Object> paramMap = new HashMap<>();

        StringBuilder finalQuery = new StringBuilder(COUNTIES_QUERY);
        int ii = 0;
        for (String key : keys) {
            if (ii == 0) {
                finalQuery.append(" WHERE ");
            } else {
                finalQuery.append(" OR ");
            }

            String countyName = "countyName" + ii;
            paramMap.put(countyName, "%" + key.toUpperCase() + "%");

            finalQuery.append("UPPER(countyname) LIKE :" + countyName);

            ii++;
        }

        queryResults = PshUtil.executeSQLQuery(finalQuery.toString(), "maps",
                paramMap);
        for (Object[] result : queryResults) {

            PshCounty county = new PshCounty();

            // extract strings from query results
            if (result[0] != null) {
                county.setName((String) result[0]);
            }

            // extract and parse centroid from query results
            if (result[1] != null && result[2] != null) {
                county.setCentroid(
                        new Coordinate(((Number) result[2]).doubleValue(),
                                ((Number) result[1]).doubleValue()));
            }

            // extract and parse geometry from query results
            if (result[3] != null) {

                byte[] bytes = (byte[]) result[3];
                WKBReader reader = new WKBReader();
                Geometry geo = null;
                try {
                    geo = reader.read(bytes);
                    county.setCentroid(geo.getCentroid().getCoordinate());
                    county.setShape(geo);
                } catch (ParseException e) {
                    statusHandler.handle(Priority.PROBLEM, "", e);
                    return null;
                }
            }

            // add county to map
            counties.add(county);
        }
        return counties;
    }

    /**
     * Load PSH county names from database.
     *
     * @return
     */
    public static PshCounties getCountyNames() {

        List<String> countiesList = new ArrayList<>();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(COUNTIES_STRINGS_QUERY, "maps");
        for (Object[] result : queryResults) {

            // extract string from query results
            if (result[0] != null) {
                countiesList.add((String) result[0]);
            }

        }
        return new PshCounties(countiesList);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.ocp.viz.psh.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import gov.noaa.nws.ocp.common.localization.psh.PshCwas;
import gov.noaa.nws.ocp.viz.psh.PshUtil;

/**
 * Provider for PSH Cwas.
 * 
 * TODO: May want to make this static? There are 123 entries in the database as
 * of 08/14/2017.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2017 #35268     astrakovsky  Initial creation.
 * Aug 18, 2017 #36981     astrakovsky  Added alternate methods and queries to 
 *                                      get only specified CWAs or just the strings.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCwaProvider {
    
    /**
     * Logger.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PshCwaProvider.class);
    
    /**
     * SQL Queries
     */
    private static String CWA_QUERY = "SELECT cwa, wfo, lat, lon, AsBinary(the_geom) FROM mapdata.cwa";
    private static String CWA_STRING_QUERY = "SELECT cwa FROM mapdata.cwa";
    
    /**
     * Load PSH CWAs from database.
     *
     * @return
     */
    public static Map<String, PshCwa> getCwas() {

        Map<String, PshCwa> cwas = new HashMap<>();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(CWA_QUERY, "maps");
        for (Object[] result : queryResults) {

            PshCwa cwa = new PshCwa();
            
            // extract strings from query results
            if (result[0] != null) {
                cwa.setCwaName((String) result[0]);
            }
            if (result[1] != null) {
                cwa.setWfoName((String) result[1]);
            }
            
            // extract and parse centroid from query results
            if (result[2] != null && result[3] != null) {
                cwa.setCentroid(
                        new Coordinate(((Number) result[3]).doubleValue(),
                                ((Number) result[2]).doubleValue()));
            } 
            
            // extract and parse geometry from query results
            if (result[4] != null) {
                
                byte[] bytes = (byte[]) result[4];
                WKBReader reader = new WKBReader();
                Geometry geo = null;
                try {
                    geo = reader.read(bytes);
                    cwa.setCentroid(geo.getCentroid().getCoordinate());
                    cwa.setShape(geo);
                } catch (ParseException e) {
                    statusHandler.handle(Priority.PROBLEM, "", e);
                    return null;
                }
            }
            
            // add CWA to map
            cwas.put(cwa.getCwaName(), cwa);
        }
        return cwas;
    }
    
    /**
     * Load PSH CWAs from database matching the specified keys.
     *
     * @return
     */
    public static Map<String, PshCwa> getCwas(List<String> keys) {

        Map<String, PshCwa> cwas = new HashMap<>();
        List<Object[]> queryResults = new ArrayList<>();

        StringBuilder finalQuery = new StringBuilder(CWA_QUERY);
        int ii = 0;
        for (String key : keys) {
            if (ii == 0) {
                finalQuery.append(" WHERE ");
            }
            else {
                finalQuery.append(" OR ");
            }
            finalQuery.append("UPPER(cwa) = '" + key.toUpperCase() + "'");
            ii++;
        }
        
        queryResults = PshUtil.executeSQLQuery(finalQuery.toString(), "maps");
        for (Object[] result : queryResults) {

            PshCwa cwa = new PshCwa();
            
            // extract strings from query results
            if (result[0] != null) {
                cwa.setCwaName((String) result[0]);
            }
            if (result[1] != null) {
                cwa.setWfoName((String) result[1]);
            }
            
            // extract and parse centroid from query results
            if (result[2] != null && result[3] != null) {
                cwa.setCentroid(
                        new Coordinate(((Number) result[3]).doubleValue(),
                                ((Number) result[2]).doubleValue()));
            } 
            
            // extract and parse geometry from query results
            if (result[4] != null) {
                
                byte[] bytes = (byte[]) result[4];
                WKBReader reader = new WKBReader();
                Geometry geo = null;
                try {
                    geo = reader.read(bytes);
                    cwa.setCentroid(geo.getCentroid().getCoordinate());
                    cwa.setShape(geo);
                } catch (ParseException e) {
                    statusHandler.handle(Priority.PROBLEM, "", e);
                    return null;
                }
            }
            
            // add CWA to map
            cwas.put(cwa.getCwaName(), cwa);
        }
        return cwas;
    }
    
    /**
     * Load PSH CWA strings from database.
     *
     * @return
     */
    public static PshCwas getCwaStrings() {

        List<String> cwas = new ArrayList<>();
        List<Object[]> queryResults = new ArrayList<>();

        queryResults = PshUtil.executeSQLQuery(CWA_STRING_QUERY, "maps");
        for (Object[] result : queryResults) {
            
            // extract string from query results
            if (result[0] != null) {
                cwas.add((String) result[0]);
            }
            
        }
        return new PshCwas(cwas);
    }
    
}

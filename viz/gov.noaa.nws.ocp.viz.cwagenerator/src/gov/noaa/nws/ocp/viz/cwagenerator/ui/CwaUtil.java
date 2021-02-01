/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.raytheon.uf.common.json.BasicJsonService;
import com.raytheon.uf.common.json.JsonException;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ncep.ui.pgen.attrdialog.SigmetCommAttrDlg;

/**
 * 
 * Utility class for CWA generator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 16, 2020 75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CwaUtil {
    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CwaUtil.class);

    /** state polygons */
    private static final String POLYGON_FILE = LocalizationUtil.join("cwsu",
            "statePolygons.json");

    private static StatePolygonConfig[] statePolygons = null;

    public static StatePolygonConfig[] getStatePolygons() {
        LocalizationLevel[] lls = new LocalizationLevel[] {
                LocalizationLevel.SITE, LocalizationLevel.BASE };
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile propertiesFile = null;
        for (LocalizationLevel ll : lls) {
            LocalizationContext lc = pm
                    .getContext(LocalizationType.COMMON_STATIC, ll);

            propertiesFile = pm.getLocalizationFile(lc, POLYGON_FILE);
            if (propertiesFile.exists()) {
                break;
            }
        }

        if (propertiesFile.exists()) {
            BasicJsonService json = new BasicJsonService();

            try {

                StatePolygonConfig[] polygons = (StatePolygonConfig[]) json
                        .deserialize(propertiesFile.openInputStream(),
                                StatePolygonConfig[].class);

                return polygons;
            } catch (JsonException | LocalizationException e) {
                logger.error(
                        "Failed to read state polygons file " + POLYGON_FILE,
                        e);
            }
        }

        return null;
    }

    public static boolean pointInPolygon(List<LatLonConfig> polygon, double lat,
            double lon) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[polygon.size() + 1];
        for (int i = 0; i < polygon.size(); i++) {
            coords[i] = new Coordinate(polygon.get(i).getLat(),
                    polygon.get(i).getLon(), 0);
        }
        // close the polygon
        coords[polygon.size()] = new Coordinate(polygon.get(0).getLat(),
                polygon.get(0).getLon(), 0);
        Polygon polygon1 = gf.createPolygon(coords);
        Point point = gf.createPoint(new Coordinate(lat, lon, 0));
        return polygon1.contains(point);
    }

    public static String getStates(Coordinate[] coords, String drawingType) {
        if (statePolygons == null) {
            statePolygons = getStatePolygons();
        }
        if (statePolygons == null) {
            return "";
        }

        List<String> states = new ArrayList<>();

        // get states that drawable in states
        for (StatePolygonConfig statePolygon : statePolygons) {
            for (Coordinate coord : coords) {
                if (pointInPolygon(statePolygon.getPolygon(), coord.x,
                        coord.y)) {
                    if (!states.contains(statePolygon.getStateID())) {
                        states.add(statePolygon.getStateID());
                    }
                }
            }
        }

        if (SigmetCommAttrDlg.AREA.equals(drawingType) && coords.length > 2) {
            // coonvert the drawable coordinates to ArrayList<LatLonConfig>
            List<LatLonConfig> drawableCoords = new ArrayList<>();
            for (Coordinate coord : coords) {
                LatLonConfig latLon = new LatLonConfig((float) coord.x,
                        (float) coord.y);
                drawableCoords.add(latLon);
            }

            // get states that state in drawable
            for (StatePolygonConfig statePolygon : statePolygons) {
                if (!states.contains(statePolygon.getStateID())) {
                    for (LatLonConfig latLon : statePolygon.getPolygon()) {
                        if (pointInPolygon(drawableCoords, latLon.getLat(),
                                latLon.getLon())) {
                            if (!states.contains(statePolygon.getStateID())) {
                                states.add(statePolygon.getStateID());
                            }
                        }
                    }
                }
            }
        }
        // put all states in a string
        String stateStr = "";
        for (String stateId : states) {
            if (!stateStr.isEmpty()) {
                stateStr += " ";
            }
            stateStr += stateId;
        }
        return stateStr;
    }

}

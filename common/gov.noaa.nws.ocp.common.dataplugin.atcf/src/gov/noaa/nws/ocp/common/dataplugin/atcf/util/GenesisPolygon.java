/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.util;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import gov.noaa.nws.ocp.common.dataplugin.atcf.exception.AtcfException;

/**
 * TODO Add Description
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 26, 2018            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class GenesisPolygon {

    // ATCF genesis polygon points (lat, lon)
    private List<Coordinate> points = new ArrayList<>();

    private final GeometryFactory geometryFactory = new GeometryFactory();


    /**
     * Empty constructor
     */
    public GenesisPolygon() {
    }

    /**
     * Constructor by passing WKT string
     * @param wkt
     */
    public GenesisPolygon(String wkt) throws AtcfException {
        WKTReader reader = new WKTReader();
        Polygon polygon = null;
        try {
            polygon = (Polygon)reader.read(wkt);
        }
        catch (Exception e) {
            throw new AtcfException("Failed to read WKT with error : ", e);
        }

        Coordinate[] coordinates = polygon.getCoordinates();
        for(Coordinate c : coordinates) {
            points.add(c);
        }

    }


    /**
     * convert Geometry to WKT string
     * @return
     */
    public String toWKT() {

        Coordinate[] coordinates = points.toArray(new Coordinate[0]);
        LinearRing lr = geometryFactory.createLinearRing(coordinates);
        Polygon polygon = geometryFactory.createPolygon(lr, new LinearRing[]{});
        WKTWriter writer = new WKTWriter();
        return writer.write(polygon);
    }

    /**
     * @return the points
     */
    public List<Coordinate> getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(List<Coordinate> points) {
        this.points = points;
    }

    /**
     *
     * @param lat
     * @param lon
     */
    public void addPoint(double lat, double lon) {
        Coordinate c = new Coordinate(lat, lon);
        points.add(c);
    }

    /**
     *
     * @return
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }

    /**
     *
     * @return
     */
    public int size() {
        return points.size();
    }


}

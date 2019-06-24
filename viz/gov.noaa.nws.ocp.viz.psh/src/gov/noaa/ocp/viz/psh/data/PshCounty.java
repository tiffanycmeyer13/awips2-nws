/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.ocp.viz.psh.data;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Class to hold PSH County information
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 07, 2017 #36923     astrakovsky  Initial creation.
 * 
 * </pre>
 * 
 * @author astrakovsky
 * @version 1.0
 * 
 */

public class PshCounty {

    private String name;

    private Coordinate centroid;

    private Geometry shape;

    /**
     * Constructor
     */
    public PshCounty() {

    }

    /**
     * Constructor
     * 
     * @param name
     * @param centroid
     * @param shape
     */
    public PshCounty(String name, Coordinate centroid, Geometry shape) {
        this.setName(name);
        this.setCentroid(centroid);
        this.setShape(shape);
    }

    /**
     * Set Centroid
     * 
     * @param centriod
     */
    public void setCentroid(Coordinate centroid) {
        this.centroid = centroid;
    }

    /**
     * Get Centroid
     * 
     * @return
     */
    public Coordinate getCentroid() {
        return centroid;
    }

    /**
     * set shape
     * 
     * @param shape
     */
    public void setShape(Geometry shape) {
        this.shape = shape;
    }

    /**
     * get shape
     * 
     * @return
     */
    public Geometry getShape() {
        return shape;
    }

    /**
     * find the
     * 
     * @param geo
     * @return
     */
    public boolean intersectGeometry(Geometry geo) {
        if (shape != null) {
            return shape.intersects(geo);
        } else
            return false;
    }

    /**
     * check if shape contains coordinate
     * 
     * @param coord
     * @return
     */
    public boolean contains(Coordinate coord) {
        return shape.getEnvelopeInternal().contains(coord);
    }

    /**
     * check if shape contains the indicated lat/lon point
     * 
     * @param lat,
     *            lon
     * @return
     */
    public boolean contains(double lat, double lon) {
        return shape.getEnvelopeInternal().contains(lon, lat);
    }

    /**
     * set name
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get name
     * 
     * @return
     */
    public String getName() {
        return name;
    }

}
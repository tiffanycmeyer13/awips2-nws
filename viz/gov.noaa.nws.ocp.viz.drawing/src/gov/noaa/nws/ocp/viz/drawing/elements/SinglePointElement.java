/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.ISinglePoint;

/**
 * Class to represent a single point element.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public abstract class SinglePointElement extends DrawableElement
        implements ISinglePoint {

    protected Color[] colors;

    protected float lineWidth;

    protected double sizeScale;

    protected boolean clear;

    Coordinate location;

    /**
     * Default constructor
     */
    protected SinglePointElement() {
        colors = new Color[] { Color.red };
        lineWidth = (float) 1.0;
        sizeScale = 1.0;
        clear = false;
        location = null;
    }

    /**
     * @param deleted
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param clear
     * @param location
     */
    protected SinglePointElement(Coordinate[] range, Color[] colors,
            float lineWidth, double sizeScale, Boolean clear,
            Coordinate location, String pgenCategory, String pgenType) {
        super(range, pgenCategory, pgenType);
        this.colors = colors;
        this.lineWidth = lineWidth;
        this.sizeScale = sizeScale;
        this.clear = clear;
        this.location = location;
    }

    /**
     * Gets the Lat/lon location of the object
     *
     * @return Lat/lon coordinate
     */
    @Override
    public Coordinate getLocation() {
        return location;
    }

    /**
     * Gets array of colors associated with the object
     *
     * @return Color array
     */
    @Override
    public Color[] getColors() {
        return colors;
    }

    /**
     * Gets the width of the line pattern
     *
     * @return line width
     */
    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Checks whether the background of the object should be cleared.
     *
     * @return true, if background should be cleared
     */
    @Override
    public boolean isClear() {
        return clear;
    }

    /**
     * Gets the size scale factor for the object
     *
     * @return size scale factor
     */
    @Override
    public double getSizeScale() {
        return sizeScale;
    }

    /**
     * Sets the Lat/lon location of the object
     */
    public void setLocation(Coordinate location) {
        setLocationOnly(location);
    }

    public void setLocationOnly(Coordinate location) {
        this.location = location;
    }

    /**
     * Sets the color list associated with the object
     */
    @Override
    public void setColors(Color[] colors) {
        if (colors != null) {
            this.colors = colors;
        }
    }

    /**
     * Sets the width of the line pattern
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Sets whether the background of the object should be cleared.
     */
    public void setClear(Boolean clear) {
        if (clear != null) {
            this.clear = clear;
        }
    }

    /**
     * Sets the size scale factor for the object
     */
    public void setSizeScale(double sizeScale) {
        this.sizeScale = sizeScale;
    }

    /**
     * Update the attributes
     */
    @Override
    public void update(IAttribute iattr) {
        if (iattr instanceof ISinglePoint) {
            ISinglePoint attr = (ISinglePoint) iattr;
            this.setClear(attr.isClear());
            this.setColors(attr.getColors());
            this.setLineWidth(attr.getLineWidth());
            this.setSizeScale(attr.getSizeScale());
        }
    }

    @Override
    public List<Coordinate> getPoints() {

        List<Coordinate> pts = new ArrayList<>();
        pts.add(getLocation());
        return pts;

    }

    @Override
    public void setPointsOnly(List<Coordinate> pts) {

        setLocationOnly(pts.get(0));

    }

    public String getPatternName() {
        return null;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;
import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.IMultiPoint;

/**
 * Class to represent a multiple point element.
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
public abstract class MultiPointElement extends DrawableElement
implements IMultiPoint {

    protected Color[] colors;

    protected float lineWidth;

    protected double sizeScale;

    protected boolean closed;

    protected boolean filled;

    protected int smoothFactor;

    protected FillPattern fillPattern;

    protected List<Coordinate> linePoints;

    protected IAttribute attr;

    /**
     * Default constructor
     */
    protected MultiPointElement() {
        colors = new Color[] { Color.red };
        lineWidth = (float) 1.0;
        sizeScale = 1.0;
        closed = false;
        filled = false;
        linePoints = null;
        smoothFactor = 2;
        fillPattern = FillPattern.SOLID;
    }

    /**
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param closed
     * @param filled
     * @param linePoints
     * @param smoothFactor
     * @param fillPattern
     * @param linePattern
     */
    protected MultiPointElement(Coordinate[] range, Color[] colors,
            float lineWidth, double sizeScale, boolean closed, boolean filled,
            List<Coordinate> linePoints, int smoothFactor,
            FillPattern fillPattern, String pgenCategory, String pgenType) {
        super(range, pgenCategory, pgenType);
        this.colors = colors;
        this.lineWidth = lineWidth;
        this.sizeScale = sizeScale;
        this.closed = closed;
        this.filled = filled;
        this.smoothFactor = smoothFactor;

        this.linePoints = new ArrayList<>();

        if (linePoints != null) {
            this.linePoints.addAll(linePoints);
        }

        FillPattern fp = FillPattern.SOLID;
        if (fillPattern != null) {
            this.fillPattern = fillPattern;
        } else {
            this.fillPattern = fp;
        }

    }

    /**
     * Gets the Lat/lon location of the object
     *
     * @return Lat/lon coordinate
     */
    @Override
    public Coordinate[] getLinePoints() {

        Coordinate[] a = new Coordinate[linePoints.size()];
        linePoints.toArray(a);
        return a;

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
    public void setLinePoints(List<Coordinate> linePoints) {
        this.linePoints = new ArrayList<>(linePoints);
    }

    /**
     * Sets the color list associated with the object
     */
    @Override
    public void setColors(Color[] colors) {
        if (colors != null) {
            if (getElemType() != null && getElemType().contains("FRONT")) {

                if (getElemType().contains("STATIONARY_FRONT")) {
                    // for stationary front
                    if (this.colors.length > colors.length) {
                        // stationary fronts need two color, if there is only
                        // one input, keep the original 2nd color
                        for (int ii = 0; ii < colors.length; ii++) {
                            this.colors[ii] = colors[ii];
                        }
                    } else {
                        this.colors = colors;
                    }
                } else {
                    // for other fronts
                    if (this.colors.length < colors.length) {
                        // for other fronts, keep only the major color
                        for (int ii = 0; ii < this.colors.length; ii++) {
                            this.colors[ii] = colors[ii];
                        }
                    } else {
                        this.colors = colors;

                    }
                }
            } else {
                this.colors = colors;
            }
        }
    }

    /**
     * Sets the width of the line pattern
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * Sets the size scale factor for the object
     */
    public void setSmoothFactor(int smoothFactor) {
        if (smoothFactor >= 0) {
            this.smoothFactor = smoothFactor;
        }
    }

    /**
     * Sets whether the object should be filled.
     */
    public void setFilled(Boolean filled) {
        if (filled != null) {
            this.filled = filled;
        }
    }

    /**
     * Sets whether the object should be closed.
     */
    public void setClosed(Boolean closed) {
        if (closed != null) {
            this.closed = closed;
        }
    }

    /**
     * Sets the size scale factor for the object
     */
    public void setSizeScale(double sizeScale) {
        this.sizeScale = sizeScale;
    }

    /**
     * Sets the fill pattern for the objects
     */
    public void setFillPattern(FillPattern fillPattern) {
        this.fillPattern = fillPattern;
    }

    /**
     * Update the attributes
     */
    @Override
    public void update(IAttribute iattr) {
        if (iattr instanceof IMultiPoint) {

            IMultiPoint attr1 = (IMultiPoint) iattr;

            this.setColors(attr1.getColors());
            this.setLineWidth(attr1.getLineWidth());
            this.setSizeScale(attr1.getSizeScale());

            this.setAttr(attr1);
        }
    }

    /**
     * IAttribute getter/setter for SIGMET gzhang
     */
    public IAttribute getAttr() {
        return this.attr;
    }

    public void setAttr(IAttribute attr) {
        this.attr = attr;
    }

    /**
     * pgenCategory getter for SIGMET gzhang
     */
    @Override
    public String getElemCategory() {
        return this.elemCategory;
    }

    @Override
    public List<Coordinate> getPoints() {
        return linePoints;
    }

    public void setPointsOnly(List<Coordinate> pts) {

        this.linePoints = pts;

    }

    public void addPoint(int index, Coordinate point) {

        linePoints.add(index, point);

    }

    public void removePoint(int index) {

        linePoints.remove(index);

    }

    /**
     * Generate a JTS polygon from a set of points.
     *
     * It is assumed that the first point is not repeated at the end in the
     * input array of point.
     *
     * @param points
     *            array of points
     * @return
     */
    public Polygon toJTSPolygon() {
        GeometryFactory geometryFactory = new GeometryFactory();

        Coordinate[] coords = new Coordinate[linePoints.size() + 1];

        for (int ii = 0; ii < linePoints.size(); ii++) {
            coords[ii] = linePoints.get(ii);
        }
        coords[coords.length - 1] = coords[0];

        CoordinateArraySequence cas = new CoordinateArraySequence(coords);
        LinearRing ring = new LinearRing(cas, geometryFactory);

        return new Polygon(ring, null, geometryFactory);
    }

    /**
     * Gets "filled".
     */
    public boolean getFilled() {
        return filled;
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;
import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.ILine;

/**
 * Class to represent a line element.
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
public class Line extends MultiPointElement implements ILine {

    private boolean flipSide = false;

    /**
     * Default constructor
     */
    public Line() {
    }

    /**
     *
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param closed
     * @param filled
     * @param linePoints
     * @param smoothFactor
     * @param fillPattern
     * @param pgenCategory
     * @param pgenType
     */
    public Line(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, boolean closed, boolean filled,
            List<Coordinate> linePoints, int smoothFactor,
            FillPattern fillPattern, String pgenCategory, String pgenType) {
        super(range, colors, lineWidth, sizeScale, closed, filled, linePoints,
                smoothFactor, fillPattern, pgenCategory, pgenType);
    }

    /**
     *
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param closed
     * @param filled
     * @param linePoints
     * @param smoothFactor
     * @param fillPattern
     * @param pgenCategory
     * @param pgenType
     * @param flipSide
     */
    public Line(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, boolean closed, boolean filled,
            List<Coordinate> linePoints, int smoothFactor,
            FillPattern fillPattern, String pgenCategory, String pgenType,
            boolean flipSide) {

        this(range, colors, lineWidth, sizeScale, closed, filled, linePoints,
                smoothFactor, fillPattern, pgenCategory, pgenType);

        this.setFlipSide(flipSide);

    }

    /**
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());

        result.append("Category:\t" + elemCategory + "\n");
        result.append("Type:\t" + elemType + "\n");
        result.append("Color:\t" + colors[0] + "\n");
        result.append("LineWidth:\t" + lineWidth + "\n");
        result.append("SizeScale:\t" + sizeScale + "\n");
        result.append("Closed:\t" + closed + "\n");
        result.append("Filled:\t" + filled + "\n");
        result.append("SmoothFactor:\t" + smoothFactor + "\n");
        result.append("FillPattern:\t" + fillPattern + "\n");
        result.append("Location:\t\n");
        for (Coordinate point : linePoints) {
            result.append("\t" + point.x + "\t" + point.y + "\n");
        }

        return result.toString();
    }

    /**
     * Update the attributes for the object
     */
    @Override
    public void update(IAttribute iattr) {
        super.update(iattr);
        if (iattr instanceof ILine) {
            ILine attr = (ILine) iattr;
            this.setClosed(attr.isClosedLine());
            this.setFilled(attr.isFilled());
            this.setSmoothFactor(attr.getSmoothFactor());
            if (attr.isFilled()) {
                this.setFillPattern(attr.getFillPattern());
            }
        }
    }

    /**
     * Creates a copy of this object. This is a deep copy and new objects are
     * created so that we are not just copying references of objects
     */
    @Override
    public DrawableElement copy() {

        /*
         * create a new Line object and initially set its attributes to this
         * one's
         */
        Line newLine = new Line();

        /*
         * new Strings are created for Type and LinePattern
         */
        newLine.setElemCategory(this.getElemCategory());
        newLine.setElemType(this.getElemType());
        newLine.setParent(this.getParent());

        newLine.update(this);

        /*
         * new Coordinates points are created and set, so we don't just set
         * references
         */
        ArrayList<Coordinate> ptsCopy = new ArrayList<>();
        for (Coordinate element : this.getPoints()) {
            ptsCopy.add(new Coordinate(element));
        }
        newLine.setPoints(ptsCopy);

        /*
         * new colors are created and set, so we don't just set references
         */
        Color[] colorCopy = new Color[this.getColors().length];
        for (int i = 0; i < this.getColors().length; i++) {
            colorCopy[i] = new Color(this.getColors()[i].getRed(),
                    this.getColors()[i].getGreen(),
                    this.getColors()[i].getBlue());
        }
        newLine.setColors(colorCopy);
        newLine.setFlipSide(this.flipSide);

        return newLine;

    }

    /**
     * @return the flipSide
     */
    public boolean isFlipSide() {
        return flipSide;
    }

    /**
     * @param flipSide
     *            the flipSide to set
     */
    public void setFlipSide(boolean flipSide) {
        this.flipSide = flipSide;
    }

    /**
     * Gets the name of the line pattern associated with this object
     */
    @Override
    public String getPatternName() {
        return getElemType();
    }

    /**
     * Gets the smooth factor used to create line path
     *
     * @return Line smoothing factor
     */
    @Override
    public int getSmoothFactor() {
        return smoothFactor;
    }

    /**
     * Checks whether the line path is closed.
     *
     * @return true, if line path is closed.
     */
    @Override
    public boolean isClosedLine() {
        return closed;
    }

    /**
     * Checks whether the object should be filled
     *
     * @return true, if a fill pattern applies
     */
    @Override
    public boolean isFilled() {
        return filled;
    }

    /**
     * Specifies the Fill Pattern to use, if isFilled returns true.
     *
     * @return The Fill Pattern associated with the object
     */
    @Override
    public FillPattern getFillPattern() {
        FillPattern fp = FillPattern.SOLID;

        if (fillPattern != null) {
            fp = fillPattern;
        }

        return fp;

    }

    /**
     * Get the east most point
     *
     * @return
     */
    public Coordinate getEastMostPoint() {
        Coordinate eastMostPt = getPoints().get(0);

        for (Coordinate pt : getPoints()) {
            if (pt.x > eastMostPt.x) {
                eastMostPt = pt;
            }
        }

        return eastMostPt;
    }

    /**
     * Returns the coordinate of the centroid constructed from the points of
     * this line.
     *
     * @return
     */
    public Coordinate getCentroid() {
        if (getPoints().size() < 2) {
            return null;
        }
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] a = new Coordinate[getPoints().size() + 1];
        getPoints().toArray(a);

        // add the first point to the end;
        a[a.length - 1] = a[0];

        LineString g = factory.createLineString(a);
        Point p = g.getCentroid();
        return p.getCoordinate();
    }

}

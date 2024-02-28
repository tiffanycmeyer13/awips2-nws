/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.drawing.ArrowHead.ArrowHeadType;
import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;
import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.IKink;

/**
 * Class to represent a kink line element.
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
public class KinkLine extends Line implements IKink {

    private double kinkPosition;

    private ArrowHeadType arrowHeadType;

    /**
     * Default constructor
     */
    public KinkLine() {
        kinkPosition = 0.5;
        arrowHeadType = ArrowHeadType.FILLED;
    }

    /**
     * @param deleted
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param closed
     * @param filled
     * @param linePoints
     * @param smoothFactor
     * @param fillPattern
     * @param pgenType
     * @param pgenCategory
     * @param kinkPosition
     * @param arrowHeadType
     */
    public KinkLine(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, boolean closed, boolean filled,
            List<Coordinate> linePoints, int smoothFactor,
            FillPattern fillPattern, String pgenCategory, String pgenType,
            double kinkPosition, ArrowHeadType arrowHeadType) {
        super(range, colors, lineWidth, sizeScale, closed, filled, linePoints,
                smoothFactor, fillPattern, pgenCategory, pgenType);
        this.kinkPosition = kinkPosition;
        this.arrowHeadType = arrowHeadType;
    }

    /**
     * Gets the color for the object
     */
    @Override
    public Color getColor() {
        return colors[0];
    }

    /**
     * Sets the kink position for the object
     *
     * @return type
     */
    public void setKinkPosition(double kinkPosition) {
        this.kinkPosition = kinkPosition;
    }

    /**
     * Gets the kink position for the object
     */
    @Override
    public double getKinkPosition() {
        return kinkPosition;
    }

    /**
     * Sets the arrow head type for the object
     *
     * @return type
     */
    public void setArrowHeadType(ArrowHeadType arrowHeadType) {
        this.arrowHeadType = arrowHeadType;
    }

    /**
     * Gets the arrow head type for the object
     */
    @Override
    public ArrowHeadType getArrowHeadType() {
        return arrowHeadType;
    }

    /**
     * Gets the start point for the object
     */
    @Override
    public Coordinate getStartPoint() {
        if (!linePoints.isEmpty()) {
            return linePoints.get(0);
        } else {
            return null;
        }
    }

    /**
     * Gets the end point for the object
     */
    @Override
    public Coordinate getEndPoint() {
        if (linePoints.size() > 1) {
            return linePoints.get(linePoints.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * Update the attributes for the object
     */
    @Override
    public void update(IAttribute iattr) {

        super.update(iattr);

        if (iattr instanceof IKink) {
            IKink attr = (IKink) iattr;
            this.setKinkPosition(attr.getKinkPosition());
            this.setArrowHeadType(attr.getArrowHeadType());
        }
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
        result.append("FillPattern:\t" + fillPattern + "\n");
        result.append("KinkPosition:\t" + kinkPosition + "\n");
        result.append("ArrowHeadType:\t" + arrowHeadType + "\n");
        result.append("Location:\t\n");
        for (Coordinate point : linePoints) {
            result.append("\t" + point.x + "\t" + point.y + "\n");
        }

        return result.toString();
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
        KinkLine newLine = new KinkLine();

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

        newLine.setFlipSide(super.isFlipSide());

        return newLine;

    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;
import gov.noaa.nws.ocp.viz.drawing.display.IArc;
import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;

/**
 * Class to represent an Arc element.
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
public class Arc extends Line implements IArc {

    private double axisRatio;

    private double startAngle;

    private double endAngle;

    /**
     * Default constructor
     */
    public Arc() {
        axisRatio = 1.0;
        startAngle = 0.0;
        endAngle = 360.0;
    }

    /**
     * @param range
     * @param color
     * @param lineWidth
     * @param sizeScale
     * @param closed
     * @param filled
     * @param linePoints
     * @param smoothFactor
     * @param fillPattern
     * @param pgenType
     * @param centerPoint
     * @param circumfencePoint
     * @param pgenCategory
     * @param axisRatio
     * @param startAngle
     * @param endAngle
     */
    public Arc(Coordinate[] range, Color color, float lineWidth,
            double sizeScale, boolean closed, boolean filled, int smoothFactor,
            FillPattern fillPattern, String pgenType, Coordinate centerPoint,
            Coordinate circumfencePoint, String pgenCategory, double axisRatio,
            double startAngle, double endAngle) {

        super(range, new Color[] { color }, lineWidth, sizeScale, closed,
                filled, new ArrayList<Coordinate>(), smoothFactor, fillPattern,
                pgenCategory, pgenType);

        this.axisRatio = axisRatio;
        this.startAngle = startAngle;
        this.endAngle = endAngle;

        this.setCenterPoint(centerPoint);
        this.setCircumferencePoint(circumfencePoint);

    }

    /**
     * @return the centerPoint
     */
    @Override
    public Coordinate getCenterPoint() {
        return linePoints.get(0);
    }

    /**
     * @return the circumferencePoint
     */
    @Override
    public Coordinate getCircumferencePoint() {
        return linePoints.get(1);
    }

    /**
     * @return the axisRatio
     */
    @Override
    public double getAxisRatio() {
        return axisRatio;
    }

    /**
     * @return the startAngle
     */
    @Override
    public double getStartAngle() {
        return startAngle;
    }

    /**
     * @return the endAngle
     */
    @Override
    public double getEndAngle() {
        return endAngle;
    }

    /**
     * @param centerPoint
     *            the centerPoint to set
     */
    public void setCenterPoint(Coordinate centerPoint) {

        if (linePoints != null) {
            linePoints.clear();
            linePoints.add(0, centerPoint);
        }
    }

    /**
     * @param circumferencePoint
     *            the circumferencePoint to set
     */
    public void setCircumferencePoint(Coordinate circumferencePoint) {

        if (linePoints != null) {
            if (linePoints.size() > 1) {
                linePoints.remove(1);
            }

            linePoints.add(1, circumferencePoint);
        }
    }

    /**
     * @param axisRatio
     *            the axisRatio to set
     */
    public void setAxisRatio(double axisRatio) {
        this.axisRatio = axisRatio;
    }

    /**
     * @param startAngle
     *            the startAngle to set
     */
    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    /**
     * @param endAngle
     *            the endAngle to set
     */
    public void setEndAngle(double endAngle) {
        this.endAngle = endAngle;
    }

    /**
     * Update the attributes for the object
     */
    @Override
    public void update(IAttribute iattr) {

        if (iattr instanceof IArc) {
            IArc attr = (IArc) iattr;
            this.setAxisRatio(attr.getAxisRatio());
            this.setStartAngle(attr.getStartAngle());
            this.setEndAngle(attr.getEndAngle());
            this.setColors(attr.getColors());
            this.setLineWidth(attr.getLineWidth());
            this.setSizeScale(attr.getSizeScale());
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
        Arc newArc = new Arc();
        newArc.update(this);

        /*
         * new Coordinates points are created and set, so we don't just set
         * references
         */
        List<Coordinate> ptsCopy = new ArrayList<>();
        for (int i = 0; i < this.getPoints().size(); i++) {
            ptsCopy.add(new Coordinate(this.getPoints().get(i)));
        }
        newArc.setPoints(ptsCopy);

        /*
         * new colors are created and set, so we don't just set references
         */
        Color[] colorCopy = new Color[this.getColors().length];
        for (int i = 0; i < this.getColors().length; i++) {
            colorCopy[i] = new Color(this.getColors()[i].getRed(),
                    this.getColors()[i].getGreen(),
                    this.getColors()[i].getBlue());
        }

        newArc.setColors(colorCopy);

        /*
         * new Strings are created for Type and LinePattern
         */
        newArc.setElemCategory(this.getElemCategory());
        newArc.setElemType(this.getElemType());

        newArc.setParent(this.getParent());
        return newArc;

    }

    /**
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());

        result.append("Category:\t" + elemCategory + "\n");
        result.append("Type:\t" + elemType + "\n");
        result.append("Color:\t" + this.getColors()[0] + "\n");
        result.append("LineWidth:\t" + lineWidth + "\n");
        result.append("SizeScale:\t" + sizeScale + "\n");
        result.append("Closed:\t" + closed + "\n");
        result.append("Filled:\t" + filled + "\n");
        result.append("FillPattern:\t" + fillPattern + "\n");
        result.append("smoothLevel:\t" + smoothFactor + "\n");
        result.append("AxisRatio:\t" + axisRatio + "\n");
        result.append("StartAngle:\t" + startAngle + "\n");
        result.append("EndAngle:\t" + endAngle + "\n");
        if (this.getCenterPoint() != null) {
            result.append("CenterPoint:\t" + this.getCenterPoint().y + "\t"
                    + this.getCenterPoint().x + "\n");
        } else {
            result.append("CenterPoint:\tnot defined\n");
        }

        if (this.getCircumferencePoint() != null) {
            result.append("CircumfencePoint:\t" + this.getCircumferencePoint().y
                    + "\t" + this.getCircumferencePoint().x + "\n");
        } else {
            result.append("CircumfencePoint:\tnot defined\n");
        }

        return result.toString();
    }

}

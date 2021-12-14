/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;

import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.drawing.ArrowHead;
import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.IVector;

/**
 * Class to represent a vector element, such as wind barbs.
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
public class Vector extends SinglePointElement implements IVector {

    private VectorType vectorType;

    private double speed;

    private double direction;

    private double arrowHeadSize;

    private boolean directionOnly;

    private ArrowHead.ArrowHeadType arrowHeadType = ArrowHead.ArrowHeadType.FILLED;

    /**
     * Default constructor
     */
    public Vector() {
    }

    /**
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param clear
     * @param location
     * @param vc
     * @param speed
     * @param direction
     * @param arrowHeadSize
     * @param directionOnly
     * @param pgenCategory
     * @param pgenType
     */

    public Vector(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, Boolean clear, Coordinate location, VectorType vc,
            double speed, double direction, double arrowHeadSize,
            boolean directionOnly, String pgenCategory, String pgenType) {

        super(range, colors, lineWidth, sizeScale, clear, location,
                pgenCategory, pgenType);

        this.vectorType = vc;
        this.speed = speed;
        this.direction = direction;
        this.arrowHeadSize = arrowHeadSize;
        this.directionOnly = directionOnly;

    }

    /**
     * A constructor that takes in ArrowHead.ArrowHeadType as an argument for
     * choosing between OPEN ( barbed, pointed ) and CLOSED ( filled, solid )
     * arrowheads
     *
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param clear
     * @param location
     * @param vc
     * @param speed
     * @param direction
     * @param arrowHeadSize
     * @param directionOnly
     * @param pgenCategory
     * @param pgenType
     * @param arrowHeadType
     */
    public Vector(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, Boolean clear, Coordinate location, VectorType vc,
            double speed, double direction, double arrowHeadSize,
            boolean directionOnly, String pgenCategory, String pgenType,
            ArrowHead.ArrowHeadType arrowHeadType) {

        this(range, colors, lineWidth, sizeScale, clear, location, vc, speed,
                direction, arrowHeadSize, directionOnly, pgenCategory,
                pgenType);

        if (arrowHeadType != null) {
            this.arrowHeadType = arrowHeadType;
        }

    }

    /**
     * @param vectorType
     *            the vectorType to set
     */
    public void setVectorType(VectorType vectorType) {
        this.vectorType = vectorType;
    }

    /**
     * @return the vectorType
     */
    @Override
    public VectorType getVectorType() {
        return vectorType;
    }

    /**
     * @return the first color
     */
    @Override
    public Color getColor() {
        return colors[0];
    }

    /**
     * @param speed
     *            the speed to set
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return the speed
     */
    @Override
    public double getSpeed() {
        return speed;
    }

    /**
     * @param direction
     *            the direction to set
     */
    public void setDirection(double direction) {
        this.direction = direction;
    }

    /**
     * @return the direction
     */
    @Override
    public double getDirection() {
        return direction;
    }

    /**
     * @param arrowHeadSize
     *            the arrowHeadSize to set
     */
    public void setArrowHeadSize(double arrowHeadSize) {
        this.arrowHeadSize = arrowHeadSize;
    }

    /**
     * @return the arrowHeadSize
     */
    @Override
    public double getArrowHeadSize() {
        return arrowHeadSize;
    }

    /**
     * @param directionOnly
     *            the directionOnly to set
     */
    public void setDirectionOnly(boolean directionOnly) {
        this.directionOnly = directionOnly;
    }

    /**
     * @return the directionOnly
     */
    @Override
    public boolean hasDirectionOnly() {
        return directionOnly;
    }

    /**
     * @return the background mask (clear)
     */
    @Override
    public boolean hasBackgroundMask() {
        return clear;
    }

    /**
     * update attributes for the object
     */
    @Override
    public void update(IAttribute iattr) {

        if (iattr instanceof IVector) {
            super.update(iattr);

            IVector attr = (IVector) iattr;
            // Not using setSpeed because it triggers snap when updating jet
            // barbs.
            this.speed = attr.getSpeed();

            if (elemType == null) {
                this.elemType = ((Vector) attr).getElemType();
            }
            this.setDirection(attr.getDirection());
            this.setArrowHeadSize(attr.getArrowHeadSize());
            this.setClear(attr.hasBackgroundMask());
        }

    }

    /**
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());

        result.append("\nCategory:\t" + elemCategory + "\n");
        result.append("Type:\t" + elemType + "\n");

        result.append("Location:\t" + location.y + "\t" + location.x + "\n");
        result.append("Color:\t" + colors[0] + "\n");
        result.append("LineWidth:\t" + lineWidth + "\n");
        result.append("SizeScale:\t" + sizeScale + "\n");
        result.append("Clear:\t" + clear + "\n");
        result.append("VectorType:\t" + vectorType + "\n");
        result.append("Speed:\t" + speed + "\n");
        result.append("Direction:\t" + direction + "\n");
        result.append("Directional:\t" + directionOnly + "\n");
        result.append("ArrowHeadSize:\t" + arrowHeadSize + "\n");

        return result.toString();
    }

    /**
     * Creates a copy of this object. This is a deep copy and new objects are
     * created so that we are not just copying references of objects
     */
    @Override
    public DrawableElement copy() {

        /*
         * create a new Vector object and initially set its attributes to this
         * one's
         */
        Vector newVector = new Vector();
        newVector.update(this);

        /*
         * Set new Color, Strings and Coordinate so that we don't just set
         * references to this object's attributes.
         */
        newVector
        .setColors(new Color[] { new Color(this.getColors()[0].getRed(),
                this.getColors()[0].getGreen(),
                this.getColors()[0].getBlue()) });
        newVector.setLocation(new Coordinate(this.getLocation()));

        newVector.setElemCategory(this.getElemCategory());
        newVector.setElemType(this.getElemType());

        newVector.setVectorType(this.getVectorType());
        newVector.setParent(this.getParent());

        return newVector;

    }

    /**
     * Calculates the angle of a directional line (p1->p2) relative to the
     * North. Note that the orientation will be clockwise as following: North 0;
     * East 90; South 180; West 270;
     *
     * @param point1
     *            - The starting point in Lat/Lon coordinates
     * @param point2
     *            - The ending point in Lat/Lon coordinates
     * @return The angle of line point1->point2 relative to the North
     */
    public static double northRotationAngle(Coordinate point1,
            Coordinate point2) {

        GeodeticCalculator gc = new GeodeticCalculator(DefaultEllipsoid.WGS84);
        gc.setStartingGeographicPoint(point1.x, point1.y);
        gc.setDestinationGeographicPoint(point2.x, point2.y);

        double azimuth = gc.getAzimuth();
        double angle = azimuth;
        if (angle < 0.0) {
            angle += 360.0;
        }

        return angle;
    }

    /**
     * Calculates the vector direction given an arrow from point 1 to point 2,
     * which also depends on the type of vectors.
     *
     * <pre>
     * Note - Orientation will be as following:
     *     Arrow/Directional: North 180; East 270; South 0; West 90;
     *     Barb/Hash: North 0; East 90; South 180; West 270;
     *     Increment by every 5 degrees.
     * </pre>
     *
     * @param point1
     *            - The starting point in Lat/Lon coordinates
     * @param point2
     *            - The ending point in Lat/Lon coordinates
     * @return The vector direction for the arrow from point1 to point2
     */
    public int vectorDirection(Coordinate point1, Coordinate point2) {

        double dir = Vector.northRotationAngle(point1, point2);

        if ("Arrow".equalsIgnoreCase(elemType)
                || "Directional".equalsIgnoreCase(elemType)) {
            dir += 180.0;
            if (dir > 360.0) {
                dir -= 360.0;
            }
        }

        return ((int) (dir + 3)) / 5 * 5;
    }

    /*
     * Gets an enum indicating whether the arrow head should be OPEN or FILLED
     * for Vector arrows.
     *
     * @return enum indicating the arrow head should be OPEN or FILLED.
     */
    @Override
    public ArrowHead.ArrowHeadType getArrowHeadType() {
        return this.arrowHeadType;

    }

}

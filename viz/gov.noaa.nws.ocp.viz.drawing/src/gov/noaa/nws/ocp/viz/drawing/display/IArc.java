/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import org.locationtech.jts.geom.Coordinate;

/**
 * Interface used to get specific attributes of a drawable object that
 * represents a circle, ellipse, or an arc segment of either
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public interface IArc extends IAttribute {

    /**
     * Gets the lat/lon coordinate of the center of the circle/ellipse
     *
     * @return center lat/lon
     */
    Coordinate getCenterPoint();

    /**
     * Gets the lat/lon of a circumference point. This point, along with the
     * center point, defines the major axis
     *
     * @return lat/lon on the circumference
     */
    Coordinate getCircumferencePoint();

    /**
     * Gets the axis ratio = length of minor axis / length of major axis
     *
     * @return axis ratio
     */
    double getAxisRatio();

    /**
     * Gets the start angle used to find the starting point of the arc. This
     * angle is relative to the major axis
     *
     * @return angle in degrees
     */
    double getStartAngle();

    /**
     * Gets the end angle used to find the ending point of the arc. This angle
     * is relative to the major axis
     *
     * @return angle in degrees
     */
    double getEndAngle();

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.awt.Color;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.drawing.ArrowHead.ArrowHeadType;

/**
 * Interface used to get specific attributes of a PGEN Geographic two-point
 * object that represents a line with a "kink" in it.
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
public interface IKink extends IAttribute {

    /**
     * Gets the starting coordinate of the line segment
     *
     * @return starting Coordinate of the segment
     */
    Coordinate getStartPoint();

    /**
     * Gets the ending coordinate of the line segment
     *
     * @return Ending Coordinate of the segment
     */
    Coordinate getEndPoint();

    /**
     * Gets the color of this line
     *
     * @return the color
     */
    Color getColor();

    /**
     * Gets the location of the kink along the line. Should be in the range 0.25
     * to 0.75
     *
     * @return location as fraction of the way along the line.
     */
    double getKinkPosition();

    /**
     * Gets the arrow head type. open or closed.
     *
     * @return Arrow Head type.
     */
    ArrowHeadType getArrowHeadType();

}

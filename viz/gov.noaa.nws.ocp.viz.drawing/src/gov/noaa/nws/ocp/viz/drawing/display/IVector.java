/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.awt.Color;

import gov.noaa.nws.ocp.common.drawing.ArrowHead;

/**
 * Interface used to get specific attributes of a PGEN Geographic Vector object
 * such as wind barbs, arrows, and hash marks.
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

public interface IVector extends ISinglePoint {

    /**
     * Different Vector Types representing wind direction (and maybe speed)
     */
    public enum VectorType {
        ARROW, WIND_BARB, HASH_MARK
    }

    /**
     * Gets vector object type
     * 
     * @return type of object
     */
    public VectorType getVectorType();

    /**
     * Gets color associated with the object
     * 
     * @return Color
     */
    public Color getColor();

    /**
     * Checks whether the background of the object should be cleared.
     * 
     * @return true, if background should be cleared
     */
    public boolean hasBackgroundMask();

    /**
     * Gets the wind speed
     * 
     * @return wind spped
     */
    public double getSpeed();

    /**
     * Gets the wind direction
     * 
     * @return direction from which the wind is blowing. North is considered 0
     *         degrees and direction increases clockwise.
     */
    public double getDirection();

    /**
     * Gets the size scale for the arrow head.
     * 
     * @return returns arrow head size
     */
    public double getArrowHeadSize();

    /**
     * Gets boolean flag indication whether arrow has an associated speed, or
     * indicates direction only
     * 
     * @return direction only flag
     */
    public boolean hasDirectionOnly();

    /**
     * Gets an enum indicating whether the arrow head should be OPEN or FILLED
     * for Vector arrows.
     * 
     * @return enum indicating the arrow head should be OPEN or FILLED.
     */
    public ArrowHead.ArrowHeadType getArrowHeadType();

}

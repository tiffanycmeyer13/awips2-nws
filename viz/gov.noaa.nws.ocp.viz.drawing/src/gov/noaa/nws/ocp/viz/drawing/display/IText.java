/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.awt.Color;

import org.locationtech.jts.geom.Coordinate;

/**
 * Interface used to get specific attributes of a PGEN text drawable object
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
public interface IText extends ISinglePoint {

    /**
     * Defines whether the text rotation is relative to the screen or geographic
     * north
     */
    public enum TextRotation {
        SCREEN_RELATIVE, NORTH_RELATIVE
    }

    /**
     * Defines the text justification options
     */
    public enum TextJustification {
        LEFT_JUSTIFY, CENTER, RIGHT_JUSTIFY
    }

    /**
     * Defines available font styles
     */
    public enum FontStyle {
        REGULAR, BOLD, ITALIC, BOLD_ITALIC
    }

    public enum DisplayType {
        NORMAL, BOX, UNDERLINE, OVERLINE
    }

    /**
     * Gets the text to draw
     *
     * @return Array of text strings
     */
    String[] getString();

    /**
     * Gets the name of the font to use
     *
     * @return font name
     */
    String getFontName();

    /**
     * Gets the size of the font
     *
     * @return font size
     */
    float getFontSize();

    /**
     * Gets the font style to use
     *
     * @return font style
     */
    FontStyle getStyle();

    /**
     * Gets the lat/lon refernce position for the text display
     *
     * @return lat/lon coordinate
     */
    Coordinate getPosition();

    /**
     * Gets the color to use for the text
     *
     * @return text color
     */
    Color getTextColor();

    /**
     * Gets the specified justification
     *
     * @return the text justification
     */
    TextJustification getJustification();

    /**
     * Gets the rotation angle to use
     *
     * @return rotation angle
     */
    double getRotation();

    /**
     * Gets how the rotation angle is applied
     *
     * @return the rotation relativity
     */
    TextRotation getRotationRelativity();

    /**
     * Determines whether the text should be displayed with an outline box
     *
     * @return true, if outline box should be displayed
     */
    DisplayType getDisplayType();

    /**
     * Determines whether text background should be masked out.
     *
     * @return true, if background is to be masked
     */
    Boolean maskText();

    /**
     * Gets the offset in the x direction for the text location. The offset is
     * specified in half-characters.
     *
     * @return The x direction offset
     */
    int getXOffset();

    /**
     * Gets the offset in the y direction for the text location. The offset is
     * specified in half-characters.
     *
     * @return The y direction offset
     */
    int getYOffset();

    Boolean getHide();

    Boolean getAuto();

    int getIthw();

    int getIwidth();

}

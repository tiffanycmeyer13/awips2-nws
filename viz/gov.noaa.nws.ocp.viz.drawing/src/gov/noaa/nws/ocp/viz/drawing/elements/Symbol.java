/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.ISymbol;

/**
 * Class to represent a symbol element.
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
public class Symbol extends SinglePointElement implements ISymbol {

    /**
     * Default constructor
     */
    public Symbol() {
    }

    /**
     * @param deleted
     * @param range
     * @param colors
     * @param lineWidth
     * @param sizeScale
     * @param clear
     * @param location
     * @param type
     */
    public Symbol(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, Boolean clear, Coordinate location,
            String pgenCategory, String pgenType) {
        super(range, colors, lineWidth, sizeScale, clear, location,
                pgenCategory, pgenType);
    }

    /**
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(getClass().getSimpleName());

        result.append("Category:\t" + elemCategory + "\n");
        result.append("Type:\t" + elemType + "\n");
        result.append("Location:\t" + location.x + "\t" + location.y + "\n");
        result.append("Color:\t" + colors[0] + "\n");
        result.append("LineWidth:\t" + lineWidth + "\n");
        result.append("SizeScale:\t" + sizeScale + "\n");
        result.append("Clear:\t" + clear + "\n");

        return result.toString();
    }

    /**
     * Creates a copy of this object. This is a deep copy and new objects are
     * created so that we are not just copying references of objects
     */
    @Override
    public DrawableElement copy() {

        /*
         * create a new Symbol object and initially set its attributes to this
         * one's
         */
        Symbol newSymbol = new Symbol();
        newSymbol.update(this);

        /*
         * Set new Color, Strings and Coordinate so that we don't just set
         * references to this object's attributes.
         */
        newSymbol
        .setColors(new Color[] { new Color(this.getColors()[0].getRed(),
                this.getColors()[0].getGreen(),
                this.getColors()[0].getBlue()) });
        newSymbol.setLocation(new Coordinate(this.getLocation()));
        newSymbol.setElemCategory(this.getElemCategory());
        newSymbol.setElemType(this.getElemType());
        newSymbol.setParent(this.getParent());

        return newSymbol;

    }

    @Override
    public String getPatternName() {
        return getElemType();
    }

}

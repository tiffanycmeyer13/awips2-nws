/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;
import gov.noaa.nws.ocp.viz.drawing.display.ICombo;

/**
 * Class to represent a combo symbol element.
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
public class ComboSymbol extends SinglePointElement implements ICombo {

    /**
     * Default constructor
     */
    public ComboSymbol() {
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
    public ComboSymbol(Coordinate[] range, Color[] colors, float lineWidth,
            double sizeScale, Boolean clear, Coordinate location,
            String pgenCategory, String pgenType) {
        super(range, colors, lineWidth, sizeScale, clear, location,
                pgenCategory, pgenType);
    }

    /**
     * Update the attributes
     */
    @Override
    public void update(IAttribute attr) {
        super.update(attr);
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
        ComboSymbol newSymbol = new ComboSymbol();
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
        newSymbol.setElemCategory(new String(this.getElemCategory()));
        newSymbol.setElemType(new String(this.getElemType()));

        return newSymbol;

    }

    /**
     * Returns an array of Symbol patterns names. The pattern names are in the
     * pgenType attribute with a vertical bar "|" as a delimiter.
     */
    @Override
    public String[] getPatternNames() {

        String type = getElemType();
        return type.split("\\|");

    }

}

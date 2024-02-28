/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements;

import java.awt.Color;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.ISymbolSet;

/**
 * Class to represent one symbol and a list of one or more lat/lon coordinate
 * locations associated with that symbol.
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
public class SymbolLocationSet implements ISymbolSet {

    /**
     * Drawable element symbol/marker
     */
    private Symbol symbol;

    /**
     * Array of lat/lon coordinate locations
     */
    private Coordinate[] locations;

    /**
     * Constructor used to set symbol and list of locations.
     *
     * @param symbol
     * @param locations
     *            Array of lat/lon coordinate locations
     */
    public SymbolLocationSet(Symbol symbol, Coordinate[] locations) {
        this.symbol = symbol;
        this.locations = locations;
    }

    /**
     * Constructor used to specify all attributes of a symbol along with a list
     * of lat/lon coordinate locations.
     *
     * @param range
     *            TBD
     * @param colors
     *            Symbol color
     * @param lineWidth
     *            Line width used to create symbol
     * @param sizeScale
     *            size scale of the symbol
     * @param hasMask
     *            indicates whether symbol has background mask.
     * @param location
     *            array of lat/lon coordinates
     * @param type
     *            Identifies symbol/marker pattern.
     */
    public SymbolLocationSet(Coordinate[] range, Color[] colors,
            float lineWidth, double sizeScale, Boolean hasMask,
            Coordinate[] locations, String pgenCategory, String pgenType) {

        this.symbol = new Symbol(range, colors, lineWidth, sizeScale, hasMask,
                locations[0], pgenCategory, pgenType);
        this.locations = locations;
    }

    /**
     * Gets list of lat/lon locations
     */
    @Override
    public Coordinate[] getLocations() {
        return locations;
    }

    /**
     * Gets symbol
     */
    @Override
    public Symbol getSymbol() {
        return symbol;
    }

}

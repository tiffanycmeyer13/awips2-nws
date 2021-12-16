/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;

/**
 * Interface used to get specific attributes of a Symbol Set, where a symbol is
 * displayed
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
public interface ISymbolSet {

    /**
     * Gets the symbol/marker to be displayed.
     *
     * @return a symbol object
     */
    Symbol getSymbol();

    /**
     * gets the list of lat/lon Coordinates at which the symbol should be
     * displayed
     *
     * @return coordinate array
     */
    Coordinate[] getLocations();

}

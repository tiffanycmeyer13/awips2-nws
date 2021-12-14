/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.symbolpattern;

import org.locationtech.jts.geom.Coordinate;

/**
 * This the base class defining a single part of a symbol pattern. It basically
 * defines methods to get the line path defining the symbol part and a flag
 * indicating whether the area defined by the path should be filled. The
 * coordinates used for the pattern assume that the center of the symbol is at
 * coordinate (0,0).
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
public interface ISymbolPart {

    /**
     * Gets the coordinates defining the line path
     *
     * @return the line path
     */
    Coordinate[] getPath();

    /**
     * Gets whether area defined by line path should be filled
     *
     * @return the filled flag
     */
    boolean isFilled();

}

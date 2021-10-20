/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import org.locationtech.jts.geom.Coordinate;

/**
 * Interface used to get specific attributes of a geographic single-point object
 * such as Markers and Symbols.
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
public interface ISinglePoint extends IAttribute {

    /**
     * Gets the Lat/lon location of the object
     *
     * @return Lat/lon coordinate
     */
    Coordinate getLocation();

    /**
     * Checks whether the background of the object should be cleared.
     *
     * @return true, if background should be cleared
     */
    boolean isClear();

}

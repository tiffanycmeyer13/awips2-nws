/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import org.locationtech.jts.geom.Coordinate;

/**
 * Interface used to get specific attributes of a geographic multi-point object.
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
public interface IMultiPoint extends IAttribute {

    /**
     * Gets an array of coordinates defining path of the object
     *
     * @return Coordinate array
     */
    Coordinate[] getLinePoints();
}

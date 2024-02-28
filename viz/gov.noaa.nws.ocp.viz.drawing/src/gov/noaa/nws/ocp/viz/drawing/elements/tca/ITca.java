/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

import java.util.Calendar;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.viz.drawing.display.IAttribute;

/**
 * This interface defines the methods used to query the attributes of a PGEN
 * Tropical Cyclone Advisory element ( TCAElement ).
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
public interface ITca extends IAttribute {

    /**
     * Returns the name of the Tropical Cyclone
     *
     * @return
     */
    String getStormName();

    /**
     * Returns the type of storm indicating its strength
     *
     * @return
     */
    String getStormType();

    /**
     * Returns the storm basin
     *
     * @return
     */
    String getBasin();

    /**
     * Returns the number assigned to the cyclone
     *
     * @return
     */
    int getStormNumber();

    /**
     * Gets the advisory number
     *
     * @return
     */
    String getAdvisoryNumber();

    /**
     * Returns the time the advisory is issued
     *
     * @return
     */
    Calendar getAdvisoryTime();

    /**
     * Returns the local time zone near the storm
     *
     * @return
     */
    String getTimeZone();

    /**
     * Returns the list of current watches/warnings.
     *
     * @return
     */
    List<TropicalCycloneAdvisory> getAdvisories();

    /**
     * Returns the issuing status of the advisory
     *
     * @return
     */
    String getIssueStatus();

    /**
     * Returns a lat/lon location to display a text summary when there are no
     * current watches/warnings
     *
     * @return
     */
    Coordinate getTextLocation();

}

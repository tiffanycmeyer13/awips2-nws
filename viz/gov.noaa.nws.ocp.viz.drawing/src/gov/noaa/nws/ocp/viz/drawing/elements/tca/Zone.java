/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

/**
 * This class is used to represent an NWS forecast zone. The constructor accepts
 * a zone in 'SSZNNN' format, where 'SS' 2 char abbreviation of the state, and
 * 'NNN' is the three digit forecast zone number.
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
public class Zone {

    private static final String ZONE_FORMAT = "%2sZ%03d";

    private String state;

    private int number;

    public Zone(String zone) {
        state = zone.substring(0, 2).toUpperCase();
        number = Integer.parseInt(zone.substring(3));
    }

    /**
     * Returns the two character abbreviate of the state
     * 
     * @return
     */
    public String getState() {
        return state;
    }

    /**
     * gets the forecast zone number
     * 
     * @return
     */
    public int getNumber() {
        return number;
    }

    /**
     * formats the zone information in 'SSZNNN' format.
     */
    public String getText() {
        return String.format(ZONE_FORMAT, state, number);
    }

}

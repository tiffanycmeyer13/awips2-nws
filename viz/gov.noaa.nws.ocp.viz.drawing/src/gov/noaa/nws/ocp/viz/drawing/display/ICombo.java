/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

/**
 * Interface used to get specific attributes of a PGEN Geographic combo symbol
 * object.
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
public interface ICombo extends ISinglePoint {

    /**
     * Gets the names of the symbol patterns to use for this element
     * 
     * @return array of symbol pattern names
     */
    public String[] getPatternNames();

}

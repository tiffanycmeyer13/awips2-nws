/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

/**
 * Interface for Symbol and its subclasses.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author B. Yin
 * @version 1.0
 */

public interface ISymbol extends ISinglePoint {
    /**
     * Gets the name of the symbol pattern to use for this element
     * 
     * @return name of symbol pattern
     */
    public String getPatternName();

}

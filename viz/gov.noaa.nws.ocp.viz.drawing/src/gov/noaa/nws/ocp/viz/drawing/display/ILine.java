/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import gov.noaa.nws.ocp.viz.drawing.display.FillPatternList.FillPattern;

/**
 * Interface for all Line and its subclasses
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
public interface ILine extends IMultiPoint {

    /**
     * Gets the pattern that should be applied to the line.
     * 
     * @return The line pattern
     */
    public String getPatternName();

    /**
     * Gets the smooth factor used to create line path
     * 
     * @return Line smoothing factor
     */
    public int getSmoothFactor();

    /**
     * Checks whether the line path is closed.
     * 
     * @return true, if line path is closed.
     */
    public boolean isClosedLine();

    /**
     * Checks whether the object should be filled
     * 
     * @return true, if a fill pattern applies
     */
    public boolean isFilled();

    /**
     * Specifes the Fill Pattern to use, if isFilled returns true.
     * 
     * @return The Fill Pattern associated with the object
     */
    public FillPattern getFillPattern();

}

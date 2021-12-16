/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.linepattern;

import gov.noaa.nws.ocp.common.drawing.DrawingException;

/**
 * This Exception is thrown if requested LinePattern is not found
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
public class LinePatternException extends DrawingException {

    /**
     * For Serializable interface
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor simply calls constructor of superclass
     * 
     * @param message
     *            Reason for Exception
     */
    public LinePatternException(String message) {
        super(message);
    }

    /**
     * Constructor simply calls constructor of superclass
     * 
     * @param message
     *            Reason for Exception
     * @param cause
     *            If caused by another Exception
     */
    public LinePatternException(String message, Throwable cause) {
        super(message, cause);
    }

}

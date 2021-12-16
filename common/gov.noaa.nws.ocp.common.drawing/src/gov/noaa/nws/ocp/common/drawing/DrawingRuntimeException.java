/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing;

/**
 * Superclass for all PGen Exceptions in the scenario classes are not forced to
 * catch the thrown Exception
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
public class DrawingRuntimeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2066263644154503458L;

    /**
     * Constructor simply calls constructor of superclass
     * 
     * @param message
     *            Exception message
     */
    public DrawingRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructor simply calls constructor of superclass
     * 
     * @param message
     *            Exception message
     * @param cause
     *            If another exception is to blame
     */
    public DrawingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

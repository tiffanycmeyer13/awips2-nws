/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing;

/**
 * Superclass for all drawing Exceptions.
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
public class DrawingException extends Exception {

    /**
     * for Serializable interface
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor simply calls constructor of superclass
     * 
     * @param message
     *            Exception message
     */
    public DrawingException(String message) {
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
    public DrawingException(String message, Throwable cause) {
        super(message, cause);
    }

}
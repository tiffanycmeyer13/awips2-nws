/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.exception;

/**
 * AtcfException a general exception for ATCF
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2021  #92591     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class AtcfException extends Exception {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 6744553177256962569L;

    public AtcfException() {
    }

    /**
     * @param message
     */
    public AtcfException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AtcfException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public AtcfException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public AtcfException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.exception;

/**
 * Parent to Climate-specific exceptions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 SEP 2016  21378      amoore      Initial creation
 * 07 SEP 2017  37754      amoore      Get rid of abstract.
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateException extends Exception {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 6744553177256962566L;

    public ClimateException() {
    }

    /**
     * @param message
     */
    public ClimateException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ClimateException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ClimateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public ClimateException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

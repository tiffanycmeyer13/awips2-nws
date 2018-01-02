/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.exception;

/**
 * ClimateSessionException
 * 
 * Some exception with CPG sessions.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 28, 2017 20637      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateSessionException extends ClimateException {

    private static final long serialVersionUID = -1203390368L;

    public ClimateSessionException() {
    }

    public ClimateSessionException(String message) {
        super(message);
    }

    public ClimateSessionException(Throwable cause) {
        super(cause);
    }

    public ClimateSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClimateSessionException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.exception;

/**
 * ClimateDBAccessException
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 28, 2017 20637      pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateDBAccessException extends ClimateException {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = -1L;

    public ClimateDBAccessException() {
    }

    public ClimateDBAccessException(String message) {
        super(message);
    }

    public ClimateDBAccessException(Throwable cause) {
        super(cause);
    }

    public ClimateDBAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClimateDBAccessException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

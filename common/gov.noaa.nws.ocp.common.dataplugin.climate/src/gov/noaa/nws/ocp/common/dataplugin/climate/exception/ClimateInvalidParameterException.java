/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.exception;

/**
 * Invalid parameters exception for Climate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 SEP 2016  21378      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateInvalidParameterException extends ClimateException {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 62052581L;

    public ClimateInvalidParameterException() {
    }

    public ClimateInvalidParameterException(String message) {
        super(message);
    }

    public ClimateInvalidParameterException(Throwable cause) {
        super(cause);
    }

    public ClimateInvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClimateInvalidParameterException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

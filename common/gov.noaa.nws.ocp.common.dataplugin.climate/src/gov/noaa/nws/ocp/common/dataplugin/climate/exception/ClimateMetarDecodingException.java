/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.exception;

/**
 * Climate Metar Decoding exceptions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 06 FEB 2017  28609      amoore      Initial creation
 * 
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public class ClimateMetarDecodingException extends ClimateException {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = -3245785243245037895L;

    public ClimateMetarDecodingException() {
    }

    public ClimateMetarDecodingException(String message) {
        super(message);
    }

    public ClimateMetarDecodingException(Throwable cause) {
        super(cause);
    }

    public ClimateMetarDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClimateMetarDecodingException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.exception;

/**
 * Climate Query exceptions, specifically for expected data/formats of data,
 * inner queries, etc.
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
public class ClimateQueryException extends ClimateException {

    /**
     * Serial ID.
     */
    private static final long serialVersionUID = -7185440231020922087L;

    public ClimateQueryException() {
    }

    public ClimateQueryException(String message) {
        super(message);
    }

    public ClimateQueryException(Throwable cause) {
        super(cause);
    }

    public ClimateQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClimateQueryException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

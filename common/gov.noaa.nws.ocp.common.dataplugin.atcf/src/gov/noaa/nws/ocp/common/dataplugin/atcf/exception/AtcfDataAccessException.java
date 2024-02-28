/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.exception;

/**
 * AtcfDataAccessException
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

public class AtcfDataAccessException extends AtcfException {

    private static final long serialVersionUID = -1L;

    public AtcfDataAccessException() {
    }

    public AtcfDataAccessException(String message) {
        super(message);
    }

    public AtcfDataAccessException(Throwable cause) {
        super(cause);
    }

    public AtcfDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AtcfDataAccessException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

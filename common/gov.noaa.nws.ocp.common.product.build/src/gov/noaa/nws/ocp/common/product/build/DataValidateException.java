/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build;

/**
 * DataValidateException
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 2, 2021 #          pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class DataValidateException extends ProductBuildException {
    private static final long serialVersionUID = -7455324971125247025L;

    /**
     *
     */
    public DataValidateException() {
    }

    /**
     * @param message
     */
    public DataValidateException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public DataValidateException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public DataValidateException(String message, Throwable cause) {
        super(message, cause);
    }

}

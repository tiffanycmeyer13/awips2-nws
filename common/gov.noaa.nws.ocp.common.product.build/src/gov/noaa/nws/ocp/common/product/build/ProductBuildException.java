/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.product.build;

/**
 * ProductBuildException
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

public class ProductBuildException extends Exception {
    private static final long serialVersionUID = -7455324971125247000L;

    /**
     *
     */
    public ProductBuildException() {
    }

    /**
     * @param message
     */
    public ProductBuildException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public ProductBuildException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ProductBuildException(String message, Throwable cause) {
        super(message, cause);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh.response;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Response from ThriftClient request of PSH final product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 14, 2017  #35738    jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public class PshProductServiceResponse {

    @DynamicSerializeElement
    private String message;

    /**
     * Empty constructor.
     */
    public PshProductServiceResponse() {
    }

    /**
     * Constructor.
     * 
     * @param iMessage
     *            task message.
     */
    public PshProductServiceResponse(String iMessage) {
        this.message = iMessage;
    }

    /**
     * @return task message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param iMessage
     *            message to set.
     */
    public void setMessage(String iMessage) {
        this.message = iMessage;
    }

}
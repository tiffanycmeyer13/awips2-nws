package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * CResponse from ThriftClient request for Climate services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 14, 2016            xzhang     Initial creation
 * 20 JUL 2016  20591      amoore      Added constructors.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateServiceResponse {

    @DynamicSerializeElement
    private boolean success;

    @DynamicSerializeElement
    private String message;

    /**
     * Empty constructor.
     */
    public ClimateServiceResponse() {
    }

    /**
     * Constructor.
     * 
     * @param iSuccess
     *            task was successful or not.
     * @param iMessage
     *            task message.
     */
    public ClimateServiceResponse(boolean iSuccess, String iMessage) {
        success = iSuccess;
        message = iMessage;
    }

    /**
     * @return true if task was successful.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param iSuccess
     *            success parameter to set.
     */
    public void setSuccess(boolean iSuccess) {
        this.success = iSuccess;
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

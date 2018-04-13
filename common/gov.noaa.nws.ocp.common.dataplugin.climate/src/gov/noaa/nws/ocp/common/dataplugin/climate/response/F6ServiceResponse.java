/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.response;

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Contain the results for response from ThriftClient request of F6
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 4, 2016            xzhang     Initial creation
 * 20 JUL 2016  20591     amoore     Added constructors.
 * 28 SEP 2016  22166     jwu        Link product with PIL.
 * 14 NOV 2016            jwu        Use product content instead of files.
 * 03 MAY 2017  33104     amoore     Use abstract map.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class F6ServiceResponse extends ClimateServiceResponse {

    @DynamicSerializeElement
    private Map<String, List<String>> contentMap;

    /**
     * Empty constructor.
     */
    public F6ServiceResponse() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param contentMap
     *            F6 content map.
     * @param iSuccess
     *            task was successful or not.
     * @param iMessage
     *            task message.
     */
    public F6ServiceResponse(Map<String, List<String>> contentMap,
            boolean iSuccess, String iMessage) {
        super(iSuccess, iMessage);
        this.contentMap = contentMap;
    }

    /**
     * @return the F6 content map.
     */
    public Map<String, List<String>> getContentMap() {
        return contentMap;
    }

    /**
     * @param contentMap
     *            F6 content map to set.
     */
    public void setContentMap(Map<String, List<String>> contentMap) {
        this.contentMap = contentMap;
    }

}
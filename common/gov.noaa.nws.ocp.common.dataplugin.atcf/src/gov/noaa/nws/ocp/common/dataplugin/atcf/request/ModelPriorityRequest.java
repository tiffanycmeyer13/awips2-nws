/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * ModelPriorityRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * May 30, 2020  78922     mporricelli  Initial creation
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
@DynamicSerialize
public class ModelPriorityRequest implements IServerRequest {
    @DynamicSerializeElement
    private Storm storm;

    /**
     * @return the storm
     */
    public Storm getStorm() {
        return storm;
    }

    /**
     * @param storm
     *            the storm to set
     */
    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    @DynamicSerializeElement
    private String dtg;

    /**
     * @return the dtg
     */
    public String getDtg() {
        return dtg;
    }

    /**
     * @param dtg
     *            the dtg to set
     */
    public void setDtg(String dtg) {
        this.dtg = dtg;
    }

    @DynamicSerializeElement
    private String priority;

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public ModelPriorityRequest() {

    }

    /**
     * construct request
     *
     * @param storm
     * @param dtg
     */
    public ModelPriorityRequest(Storm storm, String dtg, String priority) {
        this.storm = storm;
        this.dtg = dtg;
        this.priority = priority;
    }

}

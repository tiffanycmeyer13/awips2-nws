/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * SendAtcfDataToWcossRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Aug 12, 2019 66071      mporricelli  Initial creation
 * Nov 20, 2019 70253      mporricelli  Moved creation of .com
 *                                      file to PrepareCompute
 *                                      code
 * Dec 13, 2019 71984      mporricelli  Notify user if storm guidance
 *                                      does not come back from WCOSS
 * Apr 16, 2020 71986      mporricelli  Create CSV deck files for
 *                                      sending to external locations
 *                                      (WCOSS, FTP)
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
@DynamicSerialize
public class SendAtcfDataToWcossRequest implements IServerRequest {
    @DynamicSerializeElement
    private Storm storm;

    @DynamicSerializeElement
    private String dtg;

    @DynamicSerializeElement
    private Map<String, Object> queryConditions = new HashMap<>();

    public SendAtcfDataToWcossRequest() {

    }

    /**
     * construct request
     *
     * @param storm
     * @param dtg
     */
    public SendAtcfDataToWcossRequest(Storm storm, String dtg) {
        this.storm = storm;
        this.dtg = dtg;
    }

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

    /**
     * @return the queryDeckConditions
     */
    public Map<String, Object> getQueryConditions() {
        return queryConditions;
    }

    /**
     * @param queryDeckConditions
     *            the queryDeckConditions to set
     */
    public void setQueryConditions(Map<String, Object> queryConditions) {
        this.queryConditions = queryConditions;
    }

    /**
     * @param column
     * @param value
     */
    public void addOneQueryCondition(String column, Object value) {
        this.queryConditions.put(column, value);
    }

    /**
     * Clear all conditions from this map
     */
    public void clearQueryConditions() {
        this.queryConditions.clear();
    }

}

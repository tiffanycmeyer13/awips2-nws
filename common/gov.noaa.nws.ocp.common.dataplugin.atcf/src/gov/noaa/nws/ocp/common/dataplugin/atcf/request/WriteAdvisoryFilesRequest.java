/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.Map;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;

/**
 * Request to write a list of advisory files into its storage directory defined
 * in atcfenv.properties and default is "/awips2/edex/data/atcf/nhc_messages".
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 02, 2020 82721      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 *
 */
@DynamicSerialize
public class WriteAdvisoryFilesRequest implements IServerRequest {

    @DynamicSerializeElement
    private String stormId;

    @DynamicSerializeElement
    private Map<AdvisoryType, String> contents;

    public WriteAdvisoryFilesRequest() {

    }

    public WriteAdvisoryFilesRequest(String stormId,
            Map<AdvisoryType, String> contents) {
        this.stormId = stormId;
        this.contents = contents;
    }

    /**
     * @return the stormId
     */
    public String getStormId() {
        return stormId;
    }

    /**
     * @param stormId
     *            the stormId to set
     */
    public void setStormId(String stormId) {
        this.stormId = stormId;
    }

    /**
     * @return the contents
     */
    public Map<AdvisoryType, String> getContents() {
        return contents;
    }

    /**
     * @param contents
     *            the contents to set
     */
    public void setContents(Map<AdvisoryType, String> contents) {
        this.contents = contents;
    }

}
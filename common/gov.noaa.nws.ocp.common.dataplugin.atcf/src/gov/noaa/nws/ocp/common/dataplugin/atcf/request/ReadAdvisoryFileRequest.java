/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request to read an advisory file into a list of strings from directory
 * defined in atcfenv.properties and default is
 * "/awips2/edex/data/atcf/nhc_messages".
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 05, 2020 82721      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 *
 */
@DynamicSerialize
public class ReadAdvisoryFileRequest implements IServerRequest {

    @DynamicSerializeElement
    private String stormId;

    @DynamicSerializeElement
    private String fileExtention;

    public ReadAdvisoryFileRequest() {

    }

    public ReadAdvisoryFileRequest(String stormId, String fileExtension) {
        this.stormId = stormId;
        this.fileExtention = fileExtension;
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
     * @return the fileExtention
     */
    public String getFileExtention() {
        return fileExtention;
    }

    /**
     * @param fileExtention
     *            the fileExtention to set
     */
    public void setFileExtention(String fileExtention) {
        this.fileExtention = fileExtention;
    }

}
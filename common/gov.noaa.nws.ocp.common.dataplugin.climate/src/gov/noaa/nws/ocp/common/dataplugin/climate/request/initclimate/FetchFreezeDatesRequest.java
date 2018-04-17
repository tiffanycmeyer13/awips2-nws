/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * 
 * Request to fetch a record from cli_freezedates table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/01/2016  20635     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class FetchFreezeDatesRequest implements IServerRequest {

    @DynamicSerializeElement
    private int module;

    @DynamicSerializeElement
    private int informId;

    public FetchFreezeDatesRequest() {
    }

    public FetchFreezeDatesRequest(int module, int informId) {
        this.module = module;
        this.informId = informId;
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public int getInformId() {
        return informId;
    }

    public void setInformId(int informId) {
        this.informId = informId;
    }

}

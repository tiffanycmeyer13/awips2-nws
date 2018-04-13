/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;

/**
 * For Request without args
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 16, 2016            xzhang     Initial creation
 * OCT 04, 2016 20639      wkwock     Add REPLACE_STATIONS type
 * OCT 14, 2016 20639      wkwock     Remove REPLACE_STATIONS
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
@DynamicSerialize
public class ClimateRequest implements IServerRequest {

    @DynamicSerialize
    public enum RequestType {
        @DynamicSerializeElement
        GET_STATIONS, 
        @DynamicSerializeElement
        GET_GLOBAL,
        @DynamicSerializeElement
        SAVE_GLOBAL
    }
    
    @DynamicSerializeElement
    private ClimateGlobal climateGlobal;

    @DynamicSerializeElement
    private RequestType requestType;

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public ClimateGlobal getClimateGlobal() {
        return climateGlobal;
    }
    
    public void setClimateGlobal(ClimateGlobal climateGlobal) {
        this.climateGlobal = climateGlobal;
    }
}

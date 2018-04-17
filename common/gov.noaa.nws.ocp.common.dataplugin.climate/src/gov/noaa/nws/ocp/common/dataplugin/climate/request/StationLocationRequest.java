/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * For request service of station_location table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/06/2016  20639     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class StationLocationRequest implements IServerRequest {
    
    @DynamicSerializeElement
    private String stationID;
    
    public StationLocationRequest() {
    }

    public StationLocationRequest(String stationID) {
        this.stationID = stationID;
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }
}

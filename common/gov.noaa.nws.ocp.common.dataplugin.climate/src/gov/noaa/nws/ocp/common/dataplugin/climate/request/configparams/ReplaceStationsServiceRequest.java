/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.configparams;

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.Station;

/**
 * Request service to replace stations in cli_sta_setup table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/14/2016  20639     wkwock      initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class ReplaceStationsServiceRequest implements IServerRequest {
    @DynamicSerializeElement
    private List<Station> stations;

    public ReplaceStationsServiceRequest() {
    }
    
    public ReplaceStationsServiceRequest(List<Station> stations){
        this.stations=stations;
    }

    public void setStations(List<Station> stations){
        this.stations=stations;
    }

    public List<Station> getStations(){
        return stations;
    }
}

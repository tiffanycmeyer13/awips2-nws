/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.initclimate;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * 
 * Request to fetch a record from climate_period table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/14/2016  20635     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class FetchClimatePeriodRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationId;

    public FetchClimatePeriodRequest() {
    }

    public FetchClimatePeriodRequest(int stationId) {
        this.stationId = stationId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }
}

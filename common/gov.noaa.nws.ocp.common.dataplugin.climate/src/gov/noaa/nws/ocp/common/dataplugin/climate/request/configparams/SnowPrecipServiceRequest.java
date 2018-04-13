/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request.configparams;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request service to get snow and precipitation from climo_dates table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/14/2016  20639     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
@DynamicSerialize
public class SnowPrecipServiceRequest implements IServerRequest {

    public SnowPrecipServiceRequest() {
    }
}

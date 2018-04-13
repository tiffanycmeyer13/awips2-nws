/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler.configparams;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.configparams.ReplaceStationsServiceRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateStationsSetupDAO;

/**
 * Service handler for replace stations in table cli_sta_setup
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * OCT 06, 2016 20369      wkwock     Initial creation
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

public class ReplaceStationsServiceHandler
        implements IRequestHandler<ReplaceStationsServiceRequest> {

    @Override
    public Object handleRequest(ReplaceStationsServiceRequest request)
            throws Exception {
        return new ClimateStationsSetupDAO()
                .replaceMasterStations(request.getStations());
    }

}

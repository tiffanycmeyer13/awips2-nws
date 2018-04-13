/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateStationsSetupDAO;
import gov.noaa.nws.ocp.edex.common.climate.dataaccess.ClimateGlobalConfiguration;

/**
 * General Climate Service Handler handle those requests without args
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 16, 2016            xzhang     Initial creation
 * OCT 06, 2016 20369      wkwock     Add REPLACE_STATIONS
 * OCT 14, 2016 20639      wkwock     Remove REPLACE_STATIONS
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

public class ClimateServiceHandler implements IRequestHandler<ClimateRequest> {

    @Override
    public Object handleRequest(ClimateRequest request) throws Exception {

        RequestType requestType = request.getRequestType();

        switch (requestType) {
        case GET_STATIONS:
            return new ClimateStationsSetupDAO().getMasterStations();

        case GET_GLOBAL:
            return ClimateGlobalConfiguration.getGlobal();

        case SAVE_GLOBAL:
            return ClimateGlobalConfiguration
                    .saveGlobal(request.getClimateGlobal());

        default:
            break;
        }

        return null;

    }

}

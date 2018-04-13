package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.request.StationLocationRequest;
import gov.noaa.nws.ocp.edex.common.climate.dao.StationLocationDAO;

/**
 * Service handler for station_location table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 10/06/2016  20639     wkwock      Initial creation
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class StationLocationServiceHandler implements IRequestHandler<StationLocationRequest> {

    @Override
    public Object handleRequest(StationLocationRequest request) throws Exception {
        String stationID = request.getStationID();
        return new StationLocationDAO().fetchStation(stationID);
    }

}

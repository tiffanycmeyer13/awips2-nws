package gov.noaa.nws.ocp.edex.climate.service.handler;

import com.raytheon.uf.common.serialization.comm.IRequestHandler;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimoDatesRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimoDatesRequest.ClimoDatesRequestType;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimoDatesDAO;

/**
 * Service handler for climo_dates table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 09/27/2016  20639     wkwock      Initial creation
 * 10/06/2016  20639     wkwock      Add GET_SNOWPRECIP
 * 10/14/2016  20639     wkwock      Remove GET_SNOWPRECIP
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class ClimoDatesServiceHandler implements IRequestHandler<ClimoDatesRequest> {

    @Override
    public Object handleRequest(ClimoDatesRequest request) throws Exception {

        ClimoDatesRequestType requestType = request.getRequestType();
        ClimateDates precipSeason=request.getPrecipSeason();
        ClimateDates precipYear=request.getPrecipYear();
        ClimateDates snowSeason=request.getSnowSeason();
        ClimateDates snowYear=request.getSnowYear();


        switch (requestType) {
        case GET_SEASON: 
            return new ClimoDatesDAO().getSeason(precipSeason, precipYear, snowSeason, snowYear);

        case INSERT_CLIMODATES:
            return new ClimoDatesDAO().insertClimoDates(precipSeason, precipYear, snowSeason, snowYear);

        case UPDATE_CLIMODATES:
            return new ClimoDatesDAO().updateClimoDates(precipSeason, precipYear, snowSeason, snowYear);

        default:
            break;
        }

        return null;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.daily;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;

/**
 * Display Station Daily Intermediate dialog action.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 AUG 2016  20414      amoore      Initial creation
 * 21 MAR 2017  30166      amoore      Integration with CPG.
 * 27 AUG 2018  DR20863    wpaintsil   Account for the local timeZone
 *                                     when passing in the date parameter.
 * </pre>
 * 
 * @author amoore
 */
public class DisplayStationDailyIntermediateAction extends DisplayDailyAction {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayDailyAction.class);

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // get global preferences
        ClimateRequest globalsRequest = new ClimateRequest();
        globalsRequest.setRequestType(RequestType.GET_GLOBAL);
        ClimateGlobal climateGlobals;
        try {
            climateGlobals = (ClimateGlobal) ThriftClient
                    .sendRequest(globalsRequest);
            // the service can return nulls
            if (climateGlobals == null) {
                climateGlobals = ClimateGlobal.getMissingClimateGlobal();
            }

        } catch (VizException e) {
            climateGlobals = ClimateGlobal.getMissingClimateGlobal();
            logger.error("Failed to read preferences.", e);
        }
        /*
         * From CONOPS:
         * 
         * For Intermediate and evening climate products, the Latest
         * Intermediate/Evening Climate is the only available option for the
         * GUI. This option will execute the Daily Climatological Formatting
         * application from midnight to either the execution time or a
         * user-defined valid time.
         */
        try {
            openDailyDialog(PeriodType.INTER_RAD,
                    ClimateDate.getLocalDate(climateGlobals.getTimezone()),
                    true);
        } catch (ClimateException e) {
            throw new ExecutionException("Error building dialog.", e);
        } catch (SerializationException e) {
            throw new ExecutionException("Error with product session.", e);
        }

        return null;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.daily;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.common.serialization.SerializationException;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;

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
 * </pre>
 * 
 * @author amoore
 */
public class DisplayStationDailyIntermediateAction extends DisplayDailyAction {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
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
            openDailyDialog(PeriodType.INTER_RAD, ClimateDate.getLocalDate(),
                    true);
        } catch (ClimateException e) {
            throw new ExecutionException("Error building dialog.", e);
        } catch (SerializationException e) {
            throw new ExecutionException("Error with product session.", e);
        }

        return null;
    }

}

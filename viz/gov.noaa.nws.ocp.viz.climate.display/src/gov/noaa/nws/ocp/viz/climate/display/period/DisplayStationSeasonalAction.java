/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.period;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.viz.core.exception.VizException;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;

/**
 * Display Station Seasonal dialog action.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 AUG 2016  20414      amoore      Initial creation
 * 24 AUG 2016  20414      amoore      Pre-display date selection.
 * 15 MAR 2017  30162      amoore      Fix exception throwing.
 * 21 MAR 2017  30166      amoore      Integration with CPG workflow.
 * </pre>
 * 
 * @author amoore
 */
public class DisplayStationSeasonalAction extends DisplayStationPeriodAction {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            preDisplaySelection(PeriodType.SEASONAL_RAD);
        } catch (VizException | ClimateInvalidParameterException e) {
            throw new ExecutionException(
                    "Could not construct date selection pre-display for Period",
                    e);
        }
        return null;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.display.daily;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.prodgen.ManualGenerateClimateProdRequest;
import gov.noaa.nws.ocp.viz.climate.display.daily.dialog.DisplayStationDailyDialog;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Parent action for manual Daily product creation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 21 MAR 2017  30166      amoore      Initial creation.
 * 04 APR 2017  30166      amoore      Change returned data type from EDEX.
 * 10 MAY 2017  33104      amoore      Change returned data type from EDEX.
 * </pre>
 * 
 * @author amoore
 */
public abstract class DisplayDailyAction extends AbstractHandler {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayDailyAction.class);

    /**
     * Open the Daily dialog for manual Climate Product Generation process, and
     * start a Climate Product Generation session.
     * 
     * @param periodType
     *            the period type for product.
     * @param date
     *            date for product data.
     * @param nonRecentRun
     *            false if the data is the most recent full period.
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     * @throws SerializationException
     */
    protected final void openDailyDialog(PeriodType periodType,
            ClimateDate date, boolean nonRecentRun)
            throws ClimateQueryException, ClimateInvalidParameterException,
            SerializationException {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        try {
            ClimateGUIUtils.setCursorWait(shell);

            ManualGenerateClimateProdRequest dailyRequest = new ManualGenerateClimateProdRequest(
                    periodType, date, date, nonRecentRun);

            DisplayStationDailyDialog dialog = new DisplayStationDailyDialog(
                    shell, (String) ThriftClient.sendRequest(dailyRequest));

            dialog.addCloseCallback(new ICloseCallback() {
                @Override
                public void dialogClosed(Object returnValue) {
                    // closing actions
                    logger.debug("Display dialog for type: [" + periodType
                            + "] and date: [" + date.toFullDateString()
                            + "] closed.");
                }
            });

            dialog.open();
        } catch (VizException e) {
            throw new ClimateQueryException("Error getting daily data", e);
        } finally {
            ClimateGUIUtils.resetCursor(shell);
        }
    }
}

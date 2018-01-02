/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.display.common;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.viz.climate.display.daily.dialog.DisplayStationDailyDialog;
import gov.noaa.nws.ocp.viz.climate.display.period.dialog.DisplayStationPeriodDialog;
import gov.noaa.nws.ocp.viz.common.climate.util.ClimateGUIUtils;

/**
 * Display station daily or period dialog to edit data for an existing CPG
 * session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 APR 2017  27199      jwu         Initial creation
 * 12 MAY 2017  33104      amoore      Address FindBugs.
 * 02 JUN 2017  34777      amoore      Minor naming/comment fixes. Move to common Display.
 * </pre>
 * 
 * @author jwu
 */
public class DisplayStationWithDataAction extends AbstractHandler {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayStationWithDataAction.class);

    /**
     * Key to retrieve input parameters.
     */
    private static final String SESSION_ID = "sessionID";

    private static final String IS_DAILY = "isDaily";

    /**
     * Executes with the map of parameter values by name.
     *
     * @param event
     *            An event containing all the information about the current
     *            state of the application; must not be <code>null</code>.
     * @return the result of the execution. Reserved for future use, must be
     *         <code>null</code>.
     * @throws ExecutionException
     *             if an exception occurred during execution.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Get the parameters to feed into the dialog constructor.
        String sid = event.getParameter(SESSION_ID);
        if (sid == null || sid.isEmpty()) {
            logger.error(
                    "DisplayStationWithDataAction: No CPG Session ID found, exiting....");
            return null;
        }

        String param = event.getParameter(IS_DAILY);
        boolean isDaily = false;
        if (param != null) {
            if (Boolean.parseBoolean(param)) {
                isDaily = true;
            }
        }

        // Open the dialog.
        try {
            openDisplayDialog(sid, isDaily);
        } catch (ClimateException e) {
            logger.error("DisplayStationWithDataAction: Error building dialog.",
                    e);
        } catch (SerializationException e) {
            logger.error(
                    "DisplayStationWithDataAction: Error to serialize data.",
                    e);
        }

        return null;
    }

    /**
     * Open the Daily or Period dialog using data from a CPG session.
     * 
     * @param sid
     *            CPG session ID.
     * @param isDaily
     *            Flag to indicate daily or period
     * @throws ClimateInvalidParameterException
     * @throws SerializationException
     * 
     */
    protected final void openDisplayDialog(String sid, boolean isDaily)
            throws ClimateInvalidParameterException, SerializationException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        try {
            ClimateGUIUtils.setCursorWait(shell);

            CaveSWTDialog dialog = null;
            try {
                if (isDaily) {
                    dialog = new DisplayStationDailyDialog(shell, sid);
                } else {
                    dialog = new DisplayStationPeriodDialog(shell, sid);
                }

                dialog.addCloseCallback(new ICloseCallback() {
                    @Override
                    public void dialogClosed(Object returnValue) {
                        // closing actions
                        logger.debug("Display dialog for session ID: [" + sid
                                + "] closed.");
                    }
                });

                dialog.open();
            } catch (VizException e) {
                logger.error("DisplayStationAction: Error building dialog.", e);
            }
        } finally {
            ClimateGUIUtils.resetCursor(shell);
        }
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.review;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.viz.climate.review.dialog.NWWSClimateReviewDialog;

/**
 * Action corresponding to the NWWS Review Climate Dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 21 FEB 2017   22162     astrakovsky  Initial Creation.
 * 11 APR 2017   27199     jwu          Passed in session ID.
 * 
 * </pre>
 * 
 * @author astrakovsky
 */

public class NWWSClimateReviewAction extends AbstractHandler {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(NWWSClimateReviewAction.class);

    /**
     * Key to retrieve input parameters.
     */
    private static final String SESSION_ID = "sessionID";

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

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        // Get session ID to feed into the dialog constructor.
        String sid = event.getParameter(SESSION_ID);
        if (sid == null || sid.isEmpty()) {
            logger.error(
                    "NWWSClimateReviewAction: No CPG Session ID found, exiting....");
            return null;
        }

        // Open the dialog.
        NWWSClimateReviewDialog climateReviewDialogNWWS = new NWWSClimateReviewDialog(
                shell, sid);
        climateReviewDialogNWWS.setCloseCallback(new ICloseCallback() {

            @Override
            public void dialogClosed(Object returnValue) {
                // closing actions
                logger.debug("Review dialog for NWWS session ID: [" + sid
                        + "] closed.");
            }
        });
        climateReviewDialogNWWS.open();

        return null;
    }
}

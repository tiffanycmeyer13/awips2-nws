/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.managestorms.CleanupStormsDialog;

/**
 * Handler for Cleanup Storms Directory dialog
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 08, 2018  51349      wpaintsil  Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class CleanupStormsHandler extends AbstractHandler {

    private static CleanupStormsDialog cleanupStormsDialog;

    /**
     * Open the "Cleanup Storm" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (cleanupStormsDialog == null
                || cleanupStormsDialog.isDisposed()) {
            createDialog();
        }
        cleanupStormsDialog.open();

        return null;
    }

    // Create a "Cleanup Storm" dialog
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        cleanupStormsDialog = new CleanupStormsDialog(shell);
        cleanupStormsDialog.addCloseCallback(ov -> cleanupStormsDialog = null);
    }

}
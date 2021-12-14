/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.FixErrorDialog;

/**
 * Handler to start Fix Errors.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 03, 2019 64494       dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class FixErrorHandler extends AbstractHandler {

    private static FixErrorDialog fixErrorDialog;

    /**
     * Open the "Fix Error" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        if (fixErrorDialog == null || fixErrorDialog.isDisposed()) {
            createDialog();
        }
        fixErrorDialog.open();
        return null;
    }

    /*
     * Create "Fix Error" dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        fixErrorDialog = new FixErrorDialog(shell);
        fixErrorDialog.addCloseCallback(ov -> fixErrorDialog = null);
    }

}

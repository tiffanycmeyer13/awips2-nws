/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.managestorms.StartStormDialog;

/**
 * Handler to Start Storm dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 29, 2018 #45688      dmanzella   Initial creation.
 * Jun 20, 2019 64507       dmanzella   adding support for backend
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class StartStormHandler extends AbstractHandler {

    private static StartStormDialog stormDialog = null;

    /**
     * Open the "Start a Storm" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        if (stormDialog == null || stormDialog.isDisposed()) {
            createDialog();
        }
        stormDialog.open();

        return null;
    }

    /*
     * Create a Start a Storm dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        stormDialog = new StartStormDialog(shell);
        stormDialog.addCloseCallback(ov -> stormDialog = null);
    }

}

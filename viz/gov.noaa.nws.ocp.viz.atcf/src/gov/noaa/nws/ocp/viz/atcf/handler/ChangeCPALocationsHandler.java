/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.ChangeCPALocationsDialog;

/**
 * Handler to start Change CPA Locations.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2019  #59176      dmanzella   initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ChangeCPALocationsHandler extends AbstractHandler {

    private static ChangeCPALocationsDialog changeCPALocationsDialog;

    /**
     * Open the "Change CPA Locations" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        if (changeCPALocationsDialog == null
                || changeCPALocationsDialog.isDisposed()) {
            createDialog();
        }
        changeCPALocationsDialog.open();
        return null;
    }

    /*
     * Create a Change CPA Locations dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        changeCPALocationsDialog = new ChangeCPALocationsDialog(shell);
        changeCPALocationsDialog
                .addCloseCallback(ov -> changeCPALocationsDialog = null);
    }

}

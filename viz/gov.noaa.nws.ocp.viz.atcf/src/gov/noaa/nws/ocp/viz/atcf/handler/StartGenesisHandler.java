/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.managestorms.StartGenesisDialog;

/**
 * Handler for Start Genesis Area dialog
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
public class StartGenesisHandler extends AbstractHandler {

    private static StartGenesisDialog startGenesisDialog;

    /**
     * Open the "Start a Genesis" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        if (startGenesisDialog == null
                || startGenesisDialog.isDisposed()) {
            createDialog();
        }
        startGenesisDialog.open();

        return null;
    }

    /*
     * Create a Start a Genesis dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        startGenesisDialog = new StartGenesisDialog(shell);
        startGenesisDialog.addCloseCallback(ov -> startGenesisDialog = null);
    }

}

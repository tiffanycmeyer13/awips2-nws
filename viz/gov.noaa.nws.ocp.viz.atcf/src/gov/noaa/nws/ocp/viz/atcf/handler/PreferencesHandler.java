/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.PreferencesDialog;

/**
 * Handler to start Preferences.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2019  #59177      dmanzella   initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class PreferencesHandler extends AbstractHandler {

    private static PreferencesDialog preferencesDialog;

    /**
     * Open the "Preferences" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        if (preferencesDialog == null
                || preferencesDialog.isDisposed()) {
            createDialog();
        }
        preferencesDialog.open();

        return null;
    }

    /*
     * Create a Preferences dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        preferencesDialog = new PreferencesDialog(shell);
        preferencesDialog.addCloseCallback(ov -> preferencesDialog = null);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.ChangeObjAidDefaultsDialog;

/**
 * Handler to start Change Obj Aid Defaults.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2019  #58555      dmanzella   initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ChangeObjAidDefaultsHandler extends AbstractHandler {

    private static ChangeObjAidDefaultsDialog changeObjAidDefaultsDialog;

    /**
     * Open the "Change Obj Aid Defaults" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        if (changeObjAidDefaultsDialog == null
                || changeObjAidDefaultsDialog.isDisposed()) {
            createDialog();
        }
        changeObjAidDefaultsDialog.open();
        return null;
    }

    /*
     * Create a Change Obj Aid Defaults dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        changeObjAidDefaultsDialog = new ChangeObjAidDefaultsDialog(shell);
        changeObjAidDefaultsDialog
                .addCloseCallback(ov -> changeObjAidDefaultsDialog = null);
    }

}

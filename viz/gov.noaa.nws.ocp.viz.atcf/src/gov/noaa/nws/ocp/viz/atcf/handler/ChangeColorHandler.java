/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.ChangeColorDialog;

/**
 * Handler to start Change Color.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 7, 2018  #57338      dmanzella   initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ChangeColorHandler extends AbstractHandler {

    private static ChangeColorDialog changeColorDialog;

    /**
     * Open the "Change Color" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        if (changeColorDialog == null
                || changeColorDialog.isDisposed()) {
            createDialog();
        }
        changeColorDialog.open();
        return null;
    }

    /*
     * Create a Change Color dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        changeColorDialog = new ChangeColorDialog(shell);
        changeColorDialog.addCloseCallback(ov -> changeColorDialog = null);
    }

}

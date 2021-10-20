/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.ChooseAreaDialog;

/**
 * Handler to start Choose Area Dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 10, 2018 #45686     dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ChooseAreaHandler extends AbstractHandler {

    private static ChooseAreaDialog chooseAreaDialog = null;

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        /**
         * Pops up the Choose Area Dialog.
         */
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        if (chooseAreaDialog == null || chooseAreaDialog.isDisposed()) {
            chooseAreaDialog = new ChooseAreaDialog(shell);
        }

        chooseAreaDialog.open();

        return null;
    }

}

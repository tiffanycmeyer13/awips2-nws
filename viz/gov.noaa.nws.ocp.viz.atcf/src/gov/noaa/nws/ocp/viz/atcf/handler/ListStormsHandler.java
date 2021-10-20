/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.managestorms.ListStormsDialog;

/**
 * Handler for List Active Storms dialog
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
public class ListStormsHandler extends AbstractHandler {

    private static ListStormsDialog listStormsDialog;

    /**
     * Open the "List Storm" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        if (listStormsDialog == null
                || listStormsDialog.isDisposed()) {
            createDialog();
        }
        listStormsDialog.open();

        return null;
    }

    /*
     * Create a List Storms dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listStormsDialog = new ListStormsDialog(shell);
        listStormsDialog.addCloseCallback(ov -> listStormsDialog = null);
    }

}

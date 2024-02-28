/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.ChooseStormDialog;

/**
 * Handler for Choose A Storm Dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 08, 2018 #51349     wpaintsil   Initial creation.
 * Jul 18, 2017 #52658     jwu         Extended AbstractAtcfTool
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class ChooseStormHandler extends AbstractAtcfTool {

    private static ChooseStormDialog chooseStormsDialog;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (chooseStormsDialog == null
                || chooseStormsDialog.isDisposed()) {
            createDialog();
        }
        chooseStormsDialog.open();
        return null;
    }

    /**
     * Dummy implementation - no second dialog needs to be opened.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {
        // No action.
    }

    // Create a "Choose Storm" dialog
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        chooseStormsDialog = new ChooseStormDialog(shell);
        chooseStormsDialog.addCloseCallback(ov -> {
            chooseStormsDialog = null;
            AtcfSession.getInstance().getSideBar().deselect();
        });
    }

}
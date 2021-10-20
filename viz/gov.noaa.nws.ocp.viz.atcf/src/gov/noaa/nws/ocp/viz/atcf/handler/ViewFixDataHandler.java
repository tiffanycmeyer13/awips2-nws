/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.main.ViewFixDataDialog;

/**
 * Handler to start View Fix Data Dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 4, 2018  #45688      dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ViewFixDataHandler extends AbstractAtcfTool {

    private static ViewFixDataDialog fixDataDialog = null;

    /**
     * Open the "View Fix Data" dialog.
     * 
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (fixDataDialog == null || fixDataDialog.isDisposed()) {
            createDialog();
            fixDataDialog.open();
        } else {
            fixDataDialog.bringToTop();
        }
    }

    /*
     * Create a View Fix Data dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        fixDataDialog = new ViewFixDataDialog(shell);
        fixDataDialog.addCloseCallback(ov -> fixDataDialog = null);
    }

}

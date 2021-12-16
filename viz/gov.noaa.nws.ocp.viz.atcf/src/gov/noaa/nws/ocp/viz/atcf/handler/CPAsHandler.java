/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.advisory.CPAsDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Compute CPAs handler
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 09, 2018  52149      wpaintsil  Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class CPAsHandler extends AbstractAtcfTool {

    private static CPAsDialog cpasDialog;

    /**
     * Open the "Compute CPAs" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (cpasDialog == null || cpasDialog.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        cpasDialog.open();
    }

    /*
     * Create a Compute CPAs dialog
     *
     * @param storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        cpasDialog = new CPAsDialog(shell, storm);
        cpasDialog.addCloseCallback(ov -> cpasDialog = null);
    }

}

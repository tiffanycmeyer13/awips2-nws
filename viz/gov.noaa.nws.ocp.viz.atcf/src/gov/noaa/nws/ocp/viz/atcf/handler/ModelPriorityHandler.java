/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.ModelPriorityDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "NWP Model Priority" under "Aids"=>"Create Objective Forecast"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * May 30, 2020  78922      mporricelli  Initial creation
 *
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
public class ModelPriorityHandler extends AbstractAtcfTool {

    private static ModelPriorityDialog modelPriorityDlg;

    /**
     * Open the "NWP Model Priority" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (modelPriorityDlg == null
                || modelPriorityDlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        modelPriorityDlg.open();
    }

    /*
     * Create a NWP Model Priority dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        modelPriorityDlg = new ModelPriorityDialog(shell, storm);
        modelPriorityDlg.addCloseCallback(ov -> modelPriorityDlg = null);
    }

}

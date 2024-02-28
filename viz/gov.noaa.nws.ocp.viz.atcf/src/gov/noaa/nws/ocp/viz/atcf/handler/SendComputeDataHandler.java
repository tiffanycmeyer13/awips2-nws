/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.SendComputeDataDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Send Compute Data" under "Aids"=>"Create Objective Forecast"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Aug 12, 2019  66071      mporricelli  Initial creation
 *
 * </pre>
 *
 * @author mporricelli
 * @version 1.0
 */
public class SendComputeDataHandler extends AbstractAtcfTool {

    private static SendComputeDataDialog sendComputeDataDlg;

    /**
     * Open the "Send Compute Data" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (sendComputeDataDlg == null
                || sendComputeDataDlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        sendComputeDataDlg.open();
    }

    /*
     * Create a Send Compute Data dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        sendComputeDataDlg = new SendComputeDataDialog(shell, storm);
        sendComputeDataDlg.addCloseCallback(ov -> sendComputeDataDlg = null);
    }

}

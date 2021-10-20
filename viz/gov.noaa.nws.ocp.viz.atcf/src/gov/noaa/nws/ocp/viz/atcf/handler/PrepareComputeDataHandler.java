/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.PrepareComputeDataDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Prepare Compute Data" under "Aids"=>"Create Objective Forecast"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2019 59172      jwu         Initial creation.
 * Feb 22, 2021 88229      mporricelli Clean up code that is
 *                                     repeated in
 *                                     PrepareComputeDialog
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class PrepareComputeDataHandler extends AbstractAtcfTool {

    private static PrepareComputeDataDialog prepareComputeDataDlg;

    /**
     * Open the "Prepare Compute Data" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (prepareComputeDataDlg == null
                || prepareComputeDataDlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        prepareComputeDataDlg.open();
    }

    /*
     * Create a Prepare Compute Data dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        prepareComputeDataDlg = new PrepareComputeDataDialog(shell, storm);
        prepareComputeDataDlg
                .addCloseCallback(ov -> prepareComputeDataDlg = null);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.ListComputeDataDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "List Compute Data" under "Aids"=>"Create Obj Aid Forecasts"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Jun 15, 2020 79543      mporricelli  Initial creation
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */
public class ListComputeDataHandler extends AbstractAtcfTool {

    private static ListComputeDataDialog listComputeDataDlg;

    /**
     * Open the "List Compute Data" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        if (listComputeDataDlg == null
                || listComputeDataDlg.isDisposed()) {
            createDialog(currentStorm);
        } else {
            listComputeDataDlg.updateData(currentStorm);
        }
        listComputeDataDlg.open();
    }

    /*
     * Create a List Compute Data dialog
     *
     * @param storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listComputeDataDlg = new ListComputeDataDialog(shell, storm);
        listComputeDataDlg.addCloseCallback(ov -> listComputeDataDlg = null);
    }

}

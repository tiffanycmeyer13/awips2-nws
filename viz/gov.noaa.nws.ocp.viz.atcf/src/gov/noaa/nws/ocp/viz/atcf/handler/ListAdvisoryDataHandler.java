/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.advisory.ListAdvisoryDataDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Advisory => List Advisory Data".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 13, 2020  84966      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ListAdvisoryDataHandler extends AbstractAtcfTool {

    private static ListAdvisoryDataDialog listAdvisoryDataDlg;

    /**
     * Open the "List Advisory Data" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        if (listAdvisoryDataDlg == null
                || listAdvisoryDataDlg.isDisposed()) {
            createDialog(currentStorm);
        } else {
            listAdvisoryDataDlg.listAdvisoryData(currentStorm);
        }
        listAdvisoryDataDlg.open();
    }

    /*
     * Create a Compute CPAs dialog
     *
     * @param storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listAdvisoryDataDlg = new ListAdvisoryDataDialog(shell, storm);
        listAdvisoryDataDlg.addCloseCallback(ov -> listAdvisoryDataDlg = null);
    }

}

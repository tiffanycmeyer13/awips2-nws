/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.track.ComputeBestTrackCPADialog;

/**
 * Handler for "Track"=>"Compute Best Track CPAs"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2019 59317      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ComputeBestTrackCPAsHandler extends AbstractAtcfTool {

    private static ComputeBestTrackCPADialog computeBestTackCPADlg;

    /**
     * Open the "Compute Best Track CPAs" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (computeBestTackCPADlg == null
                || computeBestTackCPADlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        computeBestTackCPADlg.open();
    }

    /*
     * Create a Compute Best Track CPAs dialog
     *
     * @param bdeckMap
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        computeBestTackCPADlg = new ComputeBestTrackCPADialog(shell, storm);
        computeBestTackCPADlg
                .addCloseCallback(ov -> computeBestTackCPADlg = null);
    }

}

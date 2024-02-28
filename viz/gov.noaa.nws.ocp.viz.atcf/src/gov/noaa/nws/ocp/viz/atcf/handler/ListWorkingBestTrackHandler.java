/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.track.ListWorkingBestTrackDialog;

/**
 * Handler for "Track => List Working Best Track".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 12, 2020  84922      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ListWorkingBestTrackHandler extends AbstractAtcfTool {

    private static ListWorkingBestTrackDialog listWorkingBestTrackDlg;

    /**
     * Open the "List Working Best Track" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        if (listWorkingBestTrackDlg == null
                || listWorkingBestTrackDlg.isDisposed()) {
            createDialog(currentStorm);
        } else {
            listWorkingBestTrackDlg.listBestTrackData(currentStorm);
        }
        listWorkingBestTrackDlg.open();
    }

    /*
     * Create a List Working Best Track dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listWorkingBestTrackDlg = new ListWorkingBestTrackDialog(shell, storm);
        listWorkingBestTrackDlg
                .addCloseCallback(ov -> listWorkingBestTrackDlg = null);
    }

}

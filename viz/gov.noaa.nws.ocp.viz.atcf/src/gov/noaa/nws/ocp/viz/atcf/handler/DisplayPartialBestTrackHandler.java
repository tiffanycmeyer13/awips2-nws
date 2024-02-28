/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.track.DisplayPartialBestTrackDialog;

/**
 * Handler to start Display Partial Best Track Dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 20, 2019 #60741      dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class DisplayPartialBestTrackHandler extends AbstractAtcfTool {

    private static DisplayPartialBestTrackDialog displayPartialBestTrackDialog;

    /**
     * Open the "Display Partial Best Track" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (displayPartialBestTrackDialog == null
                || displayPartialBestTrackDialog.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            Map<String, List<BDeckRecord>> bdeckMap = AtcfDataUtil
                    .getBDeckRecords(currentStorm);
            createDialog(bdeckMap);
        }
        displayPartialBestTrackDialog.open();
    }

    /*
     * Create a Display Partial Best Track dialog
     *
     * @param bdeckMap
     */
    private static synchronized void createDialog(
            Map<String, List<BDeckRecord>> bdeckMap) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        displayPartialBestTrackDialog = new DisplayPartialBestTrackDialog(shell,
                bdeckMap);
        displayPartialBestTrackDialog
                .addCloseCallback(ov -> displayPartialBestTrackDialog = null);
    }

}

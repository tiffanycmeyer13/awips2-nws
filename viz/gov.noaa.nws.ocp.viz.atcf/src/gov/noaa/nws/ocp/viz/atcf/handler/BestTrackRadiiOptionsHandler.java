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
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackRadiiOptionsDialog;

/**
 * Handler to start Best Track Radii Options.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 26, 2019 #61789      dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class BestTrackRadiiOptionsHandler extends AbstractAtcfTool {

    private static BestTrackRadiiOptionsDialog radiiOptionsDialog = null;

    /**
     * Open the "Best Track Radii Options" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (radiiOptionsDialog == null
                || radiiOptionsDialog.isDisposed()) {

            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

            // Get the current BDeck Data
            Map<String, List<BDeckRecord>> bdeckMap = AtcfDataUtil
                    .getBDeckRecords(currentStorm);

            createDialog(bdeckMap);
        }
        radiiOptionsDialog.open();
    }

    /*
     * Create Best Track Radii Options dialog
     *
     * @param bdeckMap
     */
    private static synchronized void createDialog(
            Map<String, List<BDeckRecord>> bdeckMap) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        radiiOptionsDialog = new BestTrackRadiiOptionsDialog(shell, bdeckMap);
        radiiOptionsDialog.addCloseCallback(ov -> radiiOptionsDialog = null);
    }
}

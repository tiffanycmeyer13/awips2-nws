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
import gov.noaa.nws.ocp.viz.atcf.track.EditBestTrackDialog;

/**
 * Handler to start Best Track Dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 13, 2018 52656      dmanzella   Initial creation.
 * May 28, 2019 63377      jwu         Revised data retrieval for B Deck.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class EditBestTrackHandler extends AbstractAtcfTool {

    private static EditBestTrackDialog editBestTrackDialog = null;

    /**
     * Open the "Edit Best Track" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (editBestTrackDialog == null
                || editBestTrackDialog.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            Map<String, List<BDeckRecord>> bdeckMap = AtcfDataUtil
                    .getBDeckRecords(currentStorm, true);
            int sandboxID = AtcfSession.getInstance().getAtcfResource()
                    .getResourceData().getActiveBdeckSandbox();
            createDialog(currentStorm, sandboxID, bdeckMap);
        }
        editBestTrackDialog.open();
    }

    /*
     * Create a Edit Best Track dialog
     *
     * @param bdeckMap
     */
    private static synchronized void createDialog(Storm storm, int sandboxID,
            Map<String, List<BDeckRecord>> bdeckMap) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        editBestTrackDialog = new EditBestTrackDialog(shell, storm, sandboxID,
                bdeckMap);
        editBestTrackDialog.addCloseCallback(ov -> editBestTrackDialog = null);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata.EditFixesDataDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for Choose Storm dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 01, 2018  wpaintsil             Initial creation
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class EditFixesDataHandler extends AbstractAtcfTool {

    private static EditFixesDataDialog editFixesDataDialog = null;

    // Commands issued for entering fix data
    private static final String ENTER_FIXES = "Enter Fix Data";

    /**
     * Open the "Edit Fixes" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (editFixesDataDialog == null
                || editFixesDataDialog.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

            boolean editData = true;
            if (ENTER_FIXES.equalsIgnoreCase(commandName)) {
                editData = false;
            }

            List<FDeckRecord> fDeckRecords = AtcfDataUtil
                    .getFDeckRecords(currentStorm, true);

            int sandboxID = AtcfSession.getInstance().getAtcfResource()
                    .getResourceData().getActiveFdeckSandbox();

            createDialog(currentStorm, fDeckRecords, sandboxID, editData);
        }
        editFixesDataDialog.open();
    }

    /*
     * Create a Enter or Edit Fixes dialog
     *
     * @param bdeckMap
     */
    private static synchronized void createDialog(Storm storm,
            List<FDeckRecord> fDeckRecords, int sandboxID, boolean editData) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        editFixesDataDialog = new EditFixesDataDialog(shell, storm,
                fDeckRecords, sandboxID, editData);
        editFixesDataDialog.addCloseCallback(ov -> editFixesDataDialog = null);
    }

}
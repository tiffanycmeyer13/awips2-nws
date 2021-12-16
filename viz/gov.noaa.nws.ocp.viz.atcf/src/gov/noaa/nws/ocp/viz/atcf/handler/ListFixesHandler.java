/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.fixes.ListFixesDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Fixes => List Fixes".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 14, 2020  85083      jwu          Initial creation
 * Nov 15, 2020  85197      jwu          Allow showing flagged fixes only.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ListFixesHandler extends AbstractAtcfTool {

    private static ListFixesDialog listFixesDlg;

    private static final String LIST_FLAGGED_FIXES = "List Flagged Fixes";

    /**
     * Open the "List Fixes" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        // Check if listing flagged fixes only.
        boolean flagged = false;
        if (commandName.contains(LIST_FLAGGED_FIXES)) {
            flagged = true;
        }

        if (listFixesDlg == null || listFixesDlg.isDisposed()) {
            createDialog(currentStorm, flagged);
        } else {
            listFixesDlg.listFixes(currentStorm, flagged);
        }
        listFixesDlg.open();
    }

    /*
     * Create a List Fixes dialog
     *
     * @param storm =
     *
     * @param flagged
     */
    private static synchronized void createDialog(Storm storm,
            boolean flagged) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listFixesDlg = new ListFixesDialog(shell, storm, flagged);
        listFixesDlg.addCloseCallback(ov -> listFixesDlg = null);
    }

}

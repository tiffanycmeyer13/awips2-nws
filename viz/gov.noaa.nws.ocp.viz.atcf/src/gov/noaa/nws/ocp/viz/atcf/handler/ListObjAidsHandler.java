/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.ListObjAidsDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Aids => List Objective Aids Data".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Jan 05, 2021  85083      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ListObjAidsHandler extends AbstractAtcfTool {

    private static ListObjAidsDialog listObjAidsDlg;

    /**
     * Open the "List Objective Aids Data" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (listObjAidsDlg == null || listObjAidsDlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        listObjAidsDlg.open();
    }

    /*
     * Create a List Objective Aids Data dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listObjAidsDlg = new ListObjAidsDialog(shell, storm);
        listObjAidsDlg.addCloseCallback(ov -> listObjAidsDlg = null);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.EnterObjAidsNoWindRadiiDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Aids"=>"Enter Objective Aids (no wind radii)"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2019 59223      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class EnterObjAidsNoWindRadiiHandler extends AbstractAtcfTool {

    private static EnterObjAidsNoWindRadiiDialog enterObjAidsDlg;

    /**
     * Open the "Enter Objective Aids (no wind radii)" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {
        if (enterObjAidsDlg == null
                || enterObjAidsDlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        enterObjAidsDlg.open();
    }

    /*
     * Create an "Enter Objective Aids (no wind radii)" dialog
     *
     * @param storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        enterObjAidsDlg = new EnterObjAidsNoWindRadiiDialog(shell, storm);
        enterObjAidsDlg.addCloseCallback(ov -> enterObjAidsDlg = null);
    }

}

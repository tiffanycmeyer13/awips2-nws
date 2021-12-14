/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidSpeedAnalysisDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Aids"=>"Objective Aids Speed Analysis"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2019 59175      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ObjAidSpeedAnalysisHandler extends AbstractAtcfTool {

    private static ObjAidSpeedAnalysisDialog objAidSpeedAnalysisDlg;

    /**
     * Open the "Objective Aids Speed Analysis" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        if (objAidSpeedAnalysisDlg == null
                || objAidSpeedAnalysisDlg.isDisposed()) {
            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
            createDialog(currentStorm);
        }
        objAidSpeedAnalysisDlg.open();
    }

    /*
     * Create a Objective Aid Speed Analysis dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        objAidSpeedAnalysisDlg = new ObjAidSpeedAnalysisDialog(shell, storm);
        objAidSpeedAnalysisDlg
                .addCloseCallback(ov -> objAidSpeedAnalysisDlg = null);
    }

}
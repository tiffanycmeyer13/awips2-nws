/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.advisory.AdvisoryCompositionDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Advisory"=>"Advisory Composition"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2018 51856      wpaintsil  Initial creation.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class AdvisoryCompositionHandler extends AbstractAtcfTool {

    private static AdvisoryCompositionDialog advCompDlg;

    /**
     * Open the "Advisory Composition" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        // Retrieve B-Deck data for current storm.
        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        Map<String, List<BDeckRecord>> currentBDeckRecords = AtcfDataUtil
                .getBDeckRecords(currentStorm, false);

        // If no best track yet, warn the user.
        if (currentBDeckRecords == null || currentBDeckRecords.isEmpty()) {
            MessageDialog.openWarning(shell,
                    "Advisory Composition - No Best Track",
                    "No Best Track exists for storm "
                            + currentStorm.getStormId());
            return;
        }

        // If no forecast track yet, warn the user.
        Map<String, List<ForecastTrackRecord>> fcstTrackData = AtcfDataUtil
                .getFcstTrackRecords(currentStorm, true);

        if (fcstTrackData == null || fcstTrackData.isEmpty()) {
            MessageDialog.openWarning(shell,
                    "Advisory Composition - No Forecast Track",
                    "No forecast track has been started for storm "
                            + currentStorm.getStormId()
                            + ". \nPlease start your forecast first.");
            return;
        }

        if (advCompDlg == null || advCompDlg.isDisposed()) {
            createDialog(currentStorm);
        }
        advCompDlg.open();
    }

    /*
     * Create an Advisory Composition dialog
     *
     * @param storm
     */
    private static void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        advCompDlg = new AdvisoryCompositionDialog(shell, storm);
        advCompDlg.addCloseCallback(ov -> advCompDlg = null);
    }

}
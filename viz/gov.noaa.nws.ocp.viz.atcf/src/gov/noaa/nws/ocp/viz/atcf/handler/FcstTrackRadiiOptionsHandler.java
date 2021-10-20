/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackRadiiOptionsDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;

/**
 * Handler for forecast track wind radii options.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 04, 2019 70868      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class FcstTrackRadiiOptionsHandler extends AbstractAtcfTool {

    private static FcstTrackRadiiOptionsDialog radiiOptDlg = null;

    /**
     * Open the "Forecast Track Radii Options" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {
        if (radiiOptDlg == null || radiiOptDlg.isDisposed()) {

            Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

            Map<String, List<ForecastTrackRecord>> fcstTrackDataMap = AtcfDataUtil
                    .getFcstTrackRecords(currentStorm, false);
            AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
            AtcfProduct prd = rsc.getResourceData()
                    .getAtcfProduct(currentStorm);
            prd.setFcstTrackDataMap(fcstTrackDataMap);

            createDialog(fcstTrackDataMap);
        }
        radiiOptDlg.open();
    }

    /*
     * Create a "Forecast Track Radii Options" dialog
     *
     * @param fcstTrackDataMap
     */
    private static synchronized void createDialog(
            Map<String, List<ForecastTrackRecord>> fcstTrackDataMap) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        radiiOptDlg = new FcstTrackRadiiOptionsDialog(shell, fcstTrackDataMap);

        radiiOptDlg.addCloseCallback(ov -> radiiOptDlg = null);
    }

}

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
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.forecast.ForecastSeasDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Forecast"=>"Forecast Seas"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 08, 2020 77478      jwu         Initial creation.
 * May 28, 2020 78027      jwu         Check if forecast exists.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ForecastSeasHandler extends AbstractAtcfTool {

    private static ForecastSeasDialog forecastSeasDlg;

    /**
     * Open the "Forecast Wind Seas" dialog.
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
            warnNoBestTrack(currentStorm);
            return;
        }

        // If no track forecast yet, warn the user & quit.
        Map<String, List<ForecastTrackRecord>> fcstTrackData = AtcfDataUtil
                .getFcstTrackRecords(currentStorm, true);

        if (fcstTrackData == null || fcstTrackData.isEmpty()) {
            warnNoForecast(currentStorm);
            return;
        }

        if (forecastSeasDlg == null
                || forecastSeasDlg.isDisposed()) {
            createDialog(currentStorm, fcstTrackData);
        }
        forecastSeasDlg.open();
    }

    /*
     * Create a "Forecast Seas" dialog
     *
     * @param storm
     *
     * @param fcstTrackData
     */
    private static synchronized void createDialog(Storm storm,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        forecastSeasDlg = new ForecastSeasDialog(shell, storm, fcstTrackData);
        forecastSeasDlg.addCloseCallback(ov -> forecastSeasDlg = null);
    }

}
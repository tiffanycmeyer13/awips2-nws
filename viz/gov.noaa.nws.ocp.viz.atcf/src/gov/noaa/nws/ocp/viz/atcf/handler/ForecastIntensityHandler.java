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
import gov.noaa.nws.ocp.viz.atcf.forecast.ForecastIntensityDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Forecast"=>"Forecast Intensity"
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 21, 2020 71724      jwu         Initial creation.
 * May 28, 2020 78027      jwu         Check if forecast exists.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ForecastIntensityHandler extends AbstractAtcfTool {

    private static ForecastIntensityDialog forecastIntensityDlg;

    /**
     * Open the "Forecast Intensity" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {
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

        if (forecastIntensityDlg == null
                || forecastIntensityDlg.isDisposed()) {
            createDialog(currentStorm, fcstTrackData);
        }
        forecastIntensityDlg.open();
    }

    /*
     * Create a "Forecast Intensity" dialog
     *
     * @param storm
     *
     * @param fcstTrackData
     */
    private static synchronized void createDialog(Storm storm,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        forecastIntensityDlg = new ForecastIntensityDialog(shell, storm,
                fcstTrackData);
        forecastIntensityDlg
                .addCloseCallback(ov -> forecastIntensityDlg = null);
    }

}
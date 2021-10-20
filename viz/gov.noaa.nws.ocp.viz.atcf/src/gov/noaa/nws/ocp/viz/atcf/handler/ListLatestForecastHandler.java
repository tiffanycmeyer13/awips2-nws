/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.forecast.ListLatestForecastDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Forecast => List Latest Forecast".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 14, 2020  84981      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ListLatestForecastHandler extends AbstractAtcfTool {

    private static ListLatestForecastDialog listLatestFcstDlg;

    /**
     * Open the "List Latest Forecast" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
        if (listLatestFcstDlg == null
                || listLatestFcstDlg.isDisposed()) {
            createDialog(currentStorm);
        } else {
            listLatestFcstDlg.listLatestForecast(currentStorm);
        }
        listLatestFcstDlg.open();
    }

    /*
     * Create a List Latest Forecast dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listLatestFcstDlg = new ListLatestForecastDialog(shell, storm);
        listLatestFcstDlg.addCloseCallback(ov -> listLatestFcstDlg = null);
    }

}

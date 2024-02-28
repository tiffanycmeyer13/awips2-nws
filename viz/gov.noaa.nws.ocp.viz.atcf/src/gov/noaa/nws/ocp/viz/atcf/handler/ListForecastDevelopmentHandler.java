/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.forecast.ListForecastDevelopmentDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler for "Aids => ListForecast Development Type/Phase".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ --------------------------
 * Nov 16, 2020  85018      jwu          Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ListForecastDevelopmentHandler extends AbstractAtcfTool {

    private static ListForecastDevelopmentDialog listFcstDvlpDlg;

    /**
     * Open the "List Forecast Development Type/Phase" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    protected void executeEvent(ExecutionEvent event) {

        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();
        if (listFcstDvlpDlg == null
                || listFcstDvlpDlg.isDisposed()) {
            createDialog(currentStorm);
        } else {
            listFcstDvlpDlg.listForecastPhase(currentStorm);
        }
        listFcstDvlpDlg.open();
    }

    /*
     * Create a List Forecast Development Type/Phase dialog
     *
     * @storm
     */
    private static synchronized void createDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        listFcstDvlpDlg = new ListForecastDevelopmentDialog(shell, storm);
        listFcstDvlpDlg.addCloseCallback(ov -> listFcstDvlpDlg = null);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfBasin;
import gov.noaa.nws.ocp.viz.atcf.messages.ForecastGenesisProbDialog;

/**
 * Handler to start TC Genesis Probability dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 19, 2021  #90624     jnengel     initial creation.
 *
 * </pre>
 *
 * @author jnengel
 *
 */

public class ForecastGenesisProbHandler extends AbstractHandler {

    private static ForecastGenesisProbDialog forecastGenesisProbDialog;

    /**
     * Open the "Forecast TC Genesis Probabilities" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent event) {
        if (forecastGenesisProbDialog == null
                || forecastGenesisProbDialog.isDisposed()) {

            String name = event.getParameter("basin");
            AtcfBasin basin = AtcfBasin.AL;

            if (name != null) {
                basin = AtcfBasin.getBasin(name);
                if (basin == null) {
                    basin = AtcfBasin.AL;
                }
            }

            createDialog(basin);
        }
        forecastGenesisProbDialog.open();

        return null;
    }

    /*
     * Create a Forecast TC Genesis Probabilities dialog
     *
     * @param basin AtcfBasin
     */
    private static synchronized void createDialog(AtcfBasin basin) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        forecastGenesisProbDialog = new ForecastGenesisProbDialog(shell, basin);
        forecastGenesisProbDialog
                .addCloseCallback(ov -> forecastGenesisProbDialog = null);
    }

}

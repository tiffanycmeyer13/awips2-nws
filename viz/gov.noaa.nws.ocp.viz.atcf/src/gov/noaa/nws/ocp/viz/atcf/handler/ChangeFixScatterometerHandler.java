/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.ChangeSatelliteTypesDialog;
import gov.noaa.nws.ocp.viz.atcf.configuration.SatelliteTypes;

/**
 * Handler to start Change Fix Scatterometer Satellite Types.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 201  63379       dmanzella   Initial creation.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ChangeFixScatterometerHandler extends AbstractHandler {

    private static ChangeSatelliteTypesDialog changeSatTypesDialog;

    /**
     * Open the "Change Satellite Types" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        if (changeSatTypesDialog == null
                || changeSatTypesDialog.isDisposed()) {
            createDialog();
        }
        changeSatTypesDialog.open();
        return null;
    }

    /*
     * Create a Change Satellite Types dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        changeSatTypesDialog = new ChangeSatelliteTypesDialog(shell,
                SatelliteTypes.SCAT);
        changeSatTypesDialog
                .addCloseCallback(ov -> changeSatTypesDialog = null);
    }

}

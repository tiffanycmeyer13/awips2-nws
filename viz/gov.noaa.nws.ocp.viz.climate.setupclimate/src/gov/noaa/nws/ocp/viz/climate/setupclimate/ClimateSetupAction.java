/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.setupclimate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.viz.climate.setupclimate.dialog.ClimateSetupDialog;

/**
 * 
 * Bring up climate setup dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 18 SEP 2016  20640      jwu      Initial creation.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class ClimateSetupAction extends AbstractHandler {

    private ClimateSetupDialog setupDialog;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        if (setupDialog == null || setupDialog.getShell().isDisposed()) {
            setupDialog = new ClimateSetupDialog(shell);
            setupDialog.addCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    setupDialog = null;
                }
            });
            setupDialog.open();
        } else {
            setupDialog.bringToTop();
        }

        return null;
    }

}

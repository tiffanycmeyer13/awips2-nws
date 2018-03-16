/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.configparams;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * 
 * Config params action, to bring up dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 8, 2016             xzhang      Initial creation
 * 18 AUG 2016  20996      amoore      Make singleton.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public class ConfigParamsAction extends AbstractHandler {

    private ClimatePreferencesDialog preferencesDlg;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        if (preferencesDlg == null || preferencesDlg.getShell().isDisposed()) {
            preferencesDlg = new ClimatePreferencesDialog(shell);
            preferencesDlg.addCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    preferencesDlg = null;
                }
            });
            preferencesDlg.open();
        } else {
            preferencesDlg.bringToTop();
        }

        return null;
    }

}

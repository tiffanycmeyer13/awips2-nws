/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.ConfigSidebarDialog;

/**
 * Handler to open up a dialog to change the selections in sidebar.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2018 54781      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ConfigSidebarHandler extends AbstractHandler {

    private static ConfigSidebarDialog configSidebarDialog;

    /**
     * Open the "Config Sidebar" dialog.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        if (configSidebarDialog == null
                || configSidebarDialog.isDisposed()) {
            createDialog();
        }
        configSidebarDialog.open();

        return null;
    }

    /*
     * Create a Config Sidebar dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        configSidebarDialog = new ConfigSidebarDialog(shell);
        configSidebarDialog.addCloseCallback(ov -> configSidebarDialog = null);
    }

}

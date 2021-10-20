/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.atcf.configuration.ChangeConfigInfoDialog;
import gov.noaa.nws.ocp.viz.atcf.configuration.ChangeConfigInfoDialog.ConfigInfoType;

/**
 * Handler to start Change Fix Site Information.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 15, 2018 #56267      dmanzella   Initial creation.
 * Oct 22, 2018 #56277      dmanzella   updated to differentiate from fix type
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */

public class ChangeFixSiteHandler extends AbstractHandler {

    private static ChangeConfigInfoDialog changeFixSiteDialog;

    /**
     * Open the "Change Config Info" dialog for fix sites.
     *
     * @param event
     *            ExecutionEvent passed in from super class.
     */
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {
        if (changeFixSiteDialog == null
                || changeFixSiteDialog.isDisposed()) {
            createDialog();
        }
        changeFixSiteDialog.open();
        return null;
    }

    /*
     * Create Change Config Info dialog
     */
    private static synchronized void createDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        changeFixSiteDialog = new ChangeConfigInfoDialog(shell,
                ConfigInfoType.SITE);
        changeFixSiteDialog.addCloseCallback(ov -> changeFixSiteDialog = null);
    }

}

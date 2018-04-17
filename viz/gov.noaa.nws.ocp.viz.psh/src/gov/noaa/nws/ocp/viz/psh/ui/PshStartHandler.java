/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.psh.ui.generator.PshGeneratorDialog;

/**
 * A handler class to pop up the PSH setup dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 15, 2017 #34238      B. Yin      Initial creation.
 * Jul 05, 2017 #35463      wpaintsil   Open PSH Generator dialog 
 *                                      instead of Config dialog.
 * 
 * </pre>
 * 
 * @author B. Yin
 * @version 1.0
 * 
 */
public class PshStartHandler extends AbstractHandler {

    private PshGeneratorDialog pshDlg = null;

    /**
     * Pops up the PSH Generator dialog.
     */
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        if (pshDlg == null || !pshDlg.isOpen()) {
            pshDlg = new PshGeneratorDialog(shell);
        }
        pshDlg.open();

        return null;
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.cwagenerator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.viz.cwagenerator.ui.CWAProductDlg;

/**
 * 
 * start handler for CWA
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 1, 2021  75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CwaStartHandler extends AbstractHandler {

    private CWAProductDlg cwaDlg = null;

    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        if (cwaDlg == null || !cwaDlg.isOpen()) {
            cwaDlg = new CWAProductDlg(shell);
        }
        cwaDlg.open();

        return null;
    }
}

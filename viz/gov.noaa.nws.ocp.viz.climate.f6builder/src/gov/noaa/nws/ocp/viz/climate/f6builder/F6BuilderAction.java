/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.f6builder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.dialogs.ICloseCallback;

/**
 * F6 builder action, to bring up F6 builder dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 7, 2015             xzhang      Initial creation
 * 18 AUG 2016  20996      amoore      Make singleton.
 * 
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */

public class F6BuilderAction extends AbstractHandler {

    private F6BuilderDialog f6BuilderDialog;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        if (f6BuilderDialog == null
                || f6BuilderDialog.getShell().isDisposed()) {
            f6BuilderDialog = new F6BuilderDialog(shell);
            f6BuilderDialog.addCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    f6BuilderDialog = null;
                }
            });
            f6BuilderDialog.open();
        } else {
            f6BuilderDialog.bringToTop();
        }

        return null;
    }

}

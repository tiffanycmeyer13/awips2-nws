package gov.noaa.nws.ocp.viz.climate.qualitycontrol;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.viz.climate.qualitycontrol.dialog.QCDialog;

/**
 * Action corresponding to the QC Dialog
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2016  20636      wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 */

public class QCAction extends AbstractHandler {

    private static QCDialog qCDialog;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        if (qCDialog == null || qCDialog.getShell().isDisposed()) {
            qCDialog = new QCDialog(shell);
            qCDialog.addCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    qCDialog = null;
                }
            });
            qCDialog.open();
        } else {
            qCDialog.bringToTop();
        }

        return null;
    }

}

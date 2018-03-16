/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.viz.climate.initClimate.dialog.ClimateInitDialog;

/**
 * Action to open the main climate initialization dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date       Ticket#   Engineer    Description
 * ---------- --------  ----------- --------------------------
 * 3/24/2016  18469     wkwock      Initial creation.
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 * 
 */
public class ClimateInitAction extends AbstractHandler {
    private ClimateInitDialog climateInitDlg;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        if ((climateInitDlg == null)
                || climateInitDlg.getShell().isDisposed()) {
            climateInitDlg = new ClimateInitDialog(Display.getCurrent());
            climateInitDlg.addCloseCallback(new ICloseCallback() {

                @Override
                public void dialogClosed(Object returnValue) {
                    climateInitDlg = null;
                }
            });
            climateInitDlg.open();
        } else {
            climateInitDlg.bringToTop();
        }
        return null;
    }
}

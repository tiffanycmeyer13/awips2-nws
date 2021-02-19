/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * Customized tool tip for CWA
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 3, 2020  75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWAToolTip extends CaveSWTDialog {
    /** The message to display */
    private String message;

    /** tool tip location x */
    int x;

    /** tool tip location y */
    int y;

    protected CWAToolTip(Shell parent, String title, String message, int x,
            int y) {
        super(parent, SWT.DIALOG_TRIM | SWT.NO_FOCUS);
        this.message = message;
        this.x = x;
        this.y = y;
        if (title != null) {
            this.setText(title);
        }
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Label messageLbl = new Label(shell, SWT.NONE);
        messageLbl.setText(message);
        this.preOpened();
    }

    public void preOpened() {
        getShell().setLocation(x, y);
    }
}

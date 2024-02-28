/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.viz.ui.listeners.OcpUnsavedChangesListener;

/**
 * Abstract parent for OCP Cave SWT dialogs that track changes, to hold common
 * functionality.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 26, 2017 44624      amoore      Initial creation
 * Jan 30, 2019 59222      jwu         Adapted from OCP Climate project.
 * </pre>
 * 
 * @author amoore
 *
 */
public abstract class OcpCaveChangeTrackDialog extends OcpCaveSWTDialog {
    /**
     * Change listener. Subclasses should add this listener as modify/change
     * listener on fields/boxes of interest, for tracking if user has made
     * changes to the given dialog and warn them against losing potentially
     * unsaved changes.
     */
    protected final OcpUnsavedChangesListener changeListener = new OcpUnsavedChangesListener();

    /**
     * @see OcpCaveSWTDialog#OcpCaveSWTDialog(Shell) for defaults.
     * @param display
     */
    protected OcpCaveChangeTrackDialog(Display display) {
        super(display);
    }

    /**
     * @see OcpCaveSWTDialog#OcpCaveSWTDialog(Shell) for defaults.
     * @param parent
     */
    protected OcpCaveChangeTrackDialog(Shell parent) {
        super(parent);
    }

    protected OcpCaveChangeTrackDialog(Shell parent, int swtStyle) {
        super(parent, swtStyle);
    }

    protected OcpCaveChangeTrackDialog(Shell parent, int style, int caveStyle) {
        super(parent, style, caveStyle);
    }

    protected OcpCaveChangeTrackDialog(Display display, int dialogSwtStyle,
            int dialogCaveStyle) {
        super(display, dialogSwtStyle, dialogCaveStyle);
    }

    /**
     * If unsaved changes exist, prompt user confirmation on closing. If no
     * unsaved changes, proceed with closing without prompt.
     */
    @Override
    public boolean shouldClose() {
        if (changeListener.isChangesUnsaved()) {
            return MessageDialog.openQuestion(shell, "Unsaved Changes",
                    "Close this window? Unsaved changes will be lost.");
        } else {
            return true;
        }
    }
}
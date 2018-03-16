/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.viz.common.climate.listener.impl.UnsavedChangesListener;

/**
 * Abstract parent for Climate dialogs that track changes, to hold common
 * functionality.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 26, 2017 44624      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 *
 */
public abstract class ClimateCaveChangeTrackDialog extends ClimateCaveDialog {
    /**
     * Change listener. Subclasses should add this listener as modify/change
     * listener on fields/boxes of interest, for tracking if user has made
     * changes to the given dialog and warn them against losing potentially
     * unsaved changes.
     */
    protected final UnsavedChangesListener changeListener = new UnsavedChangesListener();

    /**
     * @see ClimateCaveDialog#ClimateCaveDialog(Shell) for defaults.
     * @param display
     */
    public ClimateCaveChangeTrackDialog(Display display) {
        super(display);
    }

    /**
     * @see ClimateCaveDialog#ClimateCaveDialog(Shell) for defaults.
     * @param parent
     */
    public ClimateCaveChangeTrackDialog(Shell parent) {
        super(parent);
    }

    public ClimateCaveChangeTrackDialog(Shell parent, int swtStyle) {
        super(parent, swtStyle);
    }

    public ClimateCaveChangeTrackDialog(Shell parent, int style,
            int caveStyle) {
        super(parent, style, caveStyle);
    }

    public ClimateCaveChangeTrackDialog(Display display,
            int climateDialogSwtStyle, int climateDialogCaveStyle) {
        super(display, climateDialogSwtStyle, climateDialogCaveStyle);
    }

    /**
     * If unsaved changes exist, prompt user confirmation on closing. If no
     * unsaved changes, proceed with closing without prompt.
     */
    public boolean shouldClose() {
        return changeListener.isChangesUnsaved()
                ? MessageDialog.openQuestion(shell, "Unsaved Changes",
                        "Close this window? Unsaved changes will be lost.")
                : true;
    }
}

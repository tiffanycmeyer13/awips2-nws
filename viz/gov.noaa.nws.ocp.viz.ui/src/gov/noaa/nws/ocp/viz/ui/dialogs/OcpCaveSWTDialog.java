/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.viz.ui.layout.OcpUILayoutUtil;

/**
 * Abstract parent for OCP dialogs extended from CaveSWTDilag, to hold common
 * functionality.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- ---------------------------------
 * Dec 26, 2017 44624      amoore      Initial creation
 * Jan 30, 2019 59222      jwu         Adapted from OCP Climate project.
 * </pre>
 *
 * @author amoore
 */
public abstract class OcpCaveSWTDialog extends CaveSWTDialog {
    /**
     * logger
     */
    protected final IUFStatusHandler logger = UFStatus.getHandler(getClass());

    /**
     * Style for text fields. Single line, bordered, left-aligned.
     */
    public static final int TEXT_FIELD_STYLE = SWT.SINGLE | SWT.BORDER
            | SWT.LEFT;

    /**
     * SWT style for dialogs. Show title, min/max buttons, close button, and
     * allow resizing.
     */
    public static final int OCP_DIALOG_SWT_STYLE = SWT.DIALOG_TRIM | SWT.MAX
            | SWT.MIN | SWT.RESIZE;

    /**
     * SWT style for modal dialogs. Show title, min/max buttons, close button,
     * and allow resizing.
     */
    public static final int OCP_DIALOG_MODAL_STYLE = SWT.DIALOG_TRIM | SWT.MAX
            | SWT.MIN | SWT.RESIZE | SWT.PRIMARY_MODAL;

    /**
     * CAVE style for a primary modal dialog to be used with
     * OCP_DIALOG_MODAL_STYLE. Do Not Block (code continues after "open"
     * command) and perspective independent (persists even if perspective is
     * changed) - cannot use along with CAVE.INDEPENDENT_SHELL.
     */
    public static final int OCP_DIALOG_MODAL_CAVE_STYLE = CAVE.DO_NOT_BLOCK
            | CAVE.PERSPECTIVE_INDEPENDENT;

    /**
     * CAVE style for non-primary-modal dialogs. Do Not Block (code continues
     * after "open" command) and perspective independent (persists even if
     * perspective is changed). Task 20952, DAI 12. Independent Shell (dialog
     * appears in the task bar; dialog is centered). Independent Shell will
     * overwrite SWT.PRIMARY_MODAL flag.
     */
    public static final int OCP_DIALOG_CAVE_STYLE = CAVE.DO_NOT_BLOCK
            | CAVE.PERSPECTIVE_INDEPENDENT | CAVE.INDEPENDENT_SHELL;

    /**
     * Use OCP SWT style {@link OcpUILayoutUtil.OCP_DIALOG_SWT_STYLE} and CAVE
     * style {@link OcpUILayoutUtil.OCP_DIALOG_CAVE_STYLE}.
     * 
     * @param display
     */
    protected OcpCaveSWTDialog(Display display) {
        super(display, OCP_DIALOG_SWT_STYLE, OCP_DIALOG_CAVE_STYLE);
    }

    /**
     * Use OCP SWT style {@link OcpUILayoutUtil.OCP_DIALOG_SWT_STYLE} and CAVE
     * style {@link OcpUILayoutUtil.OCP_DIALOG_CAVE_STYLE}.
     *
     * @param parent
     */
    protected OcpCaveSWTDialog(Shell parent) {
        super(parent, OCP_DIALOG_SWT_STYLE, OCP_DIALOG_CAVE_STYLE);
    }

    protected OcpCaveSWTDialog(Shell parent, int swtStyle) {
        super(parent, swtStyle);
    }

    protected OcpCaveSWTDialog(Shell parent, int style, int caveStyle) {
        super(parent, style, caveStyle);
    }

    protected OcpCaveSWTDialog(Display display, int dialogSwtStyle,
            int dialogCaveStyle) {
        super(display, dialogSwtStyle, dialogCaveStyle);
    }
}
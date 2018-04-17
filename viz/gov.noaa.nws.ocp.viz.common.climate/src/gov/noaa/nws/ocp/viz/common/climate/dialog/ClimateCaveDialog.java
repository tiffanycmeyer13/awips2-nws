/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.dialog;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.viz.common.climate.comp.ClimateLayoutValues;

/**
 * Abstract parent for Climate dialogs, to hold common functionality.
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
public abstract class ClimateCaveDialog extends CaveSWTDialog {
    /**
     * logger
     */
    protected final IUFStatusHandler logger = UFStatus.getHandler(getClass());

    /**
     * Use Climate SWT style
     * {@link ClimateLayoutValues#CLIMATE_DIALOG_SWT_STYLE} and CAVE style
     * {@link ClimateLayoutValues#CLIMATE_DIALOG_CAVE_STYLE}.
     * 
     * @param display
     */
    public ClimateCaveDialog(Display display) {
        super(display, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
                ClimateLayoutValues.CLIMATE_DIALOG_CAVE_STYLE);
    }

    /**
     * Use Climate SWT style
     * {@link ClimateLayoutValues#CLIMATE_DIALOG_SWT_STYLE} and CAVE style
     * {@link ClimateLayoutValues#CLIMATE_DIALOG_CAVE_STYLE}.
     * 
     * @param parent
     */
    public ClimateCaveDialog(Shell parent) {
        super(parent, ClimateLayoutValues.CLIMATE_DIALOG_SWT_STYLE,
                ClimateLayoutValues.CLIMATE_DIALOG_CAVE_STYLE);
    }

    public ClimateCaveDialog(Shell parent, int swtStyle) {
        super(parent, swtStyle);
    }

    public ClimateCaveDialog(Shell parent, int style, int caveStyle) {
        super(parent, style, caveStyle);
    }

    public ClimateCaveDialog(Display display, int climateDialogSwtStyle,
            int climateDialogCaveStyle) {
        super(display, climateDialogSwtStyle, climateDialogCaveStyle);
    }
}

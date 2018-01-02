/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.common.climate.comp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Control;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog.CAVE;

/**
 * This class holds common Display values.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 MAR 2016  16003      amoore      Initial creation
 * 28 JUL 2016  20652      amoore      Common SWT style for Climate dialogs.
 * 05 AUG 2016  20999      amoore      Add plugin ID for looking up icons.
 * 04 OCT 2016  20592      amoore      Dialogs should be perspective independent.
 * 21 NOV 2016  29904      wpaintsil   Dialogs should appear in the task bar.
 * 04 JAN 2017  22134      amoore      Add filler grid layout data (size 0).
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 19 SEP 2017  38124      amoore      Address review comments. Use GC for text
 *                                     control sizes.
 * </pre>
 * 
 * @author amoore
 */
public final class ClimateLayoutValues {

    /**
     * Plug-in ID.
     */
    public static final String PLUGIN_ID = "gov.noaa.nws.ocp.viz.common.climate";

    /**
     * Style for text fields. Single line, bordered, left-aligned.
     */
    public static final int TEXT_FIELD_STYLE = SWT.SINGLE | SWT.BORDER
            | SWT.LEFT;

    /**
     * SWT style for dialogs. Show title, min/max buttons, close button, and
     * allow resizing.
     */
    public static final int CLIMATE_DIALOG_SWT_STYLE = SWT.DIALOG_TRIM | SWT.MAX
            | SWT.MIN | SWT.RESIZE;

    /**
     * CAVE style for all dialogs. Do Not Block (code continues after "open"
     * command) and perspective independent (persists even if perspective is
     * changed). Task 20952, DAI 12. Independent Shell (dialog appears in the
     * task bar; dialog is centered).
     */
    public static final int CLIMATE_DIALOG_CAVE_STYLE = CAVE.DO_NOT_BLOCK
            | CAVE.PERSPECTIVE_INDEPENDENT | CAVE.INDEPENDENT_SHELL;

    /**
     * Private constructor. This is a utility class.
     */
    private ClimateLayoutValues() {
    }

    /**
     * set control's Grid Data for regular fields in a Grid Layout.
     * 
     * @param comp
     */
    public static void assignFieldsGD(Control comp) {
        GC gc = new GC(comp);
        comp.setLayoutData(
                new GridData(8 * gc.getFontMetrics().getAverageCharWidth(),
                        gc.getFontMetrics().getHeight()));
        gc.dispose();
    }

    /**
     * set control's Row Data for regular fields in a Row Layout.
     * 
     * @param comp
     */
    public static void assignFieldsRD(Control comp) {
        GC gc = new GC(comp);
        comp.setLayoutData(
                new RowData(8 * gc.getFontMetrics().getAverageCharWidth(),
                        gc.getFontMetrics().getHeight()));
        gc.dispose();
    }

    /**
     * set control's Grid Data for longer fields in a Grid Layout.
     * 
     * @param comp
     */
    public static void assignLongFieldsGD(Control comp) {
        GC gc = new GC(comp);
        comp.setLayoutData(
                new GridData(16 * gc.getFontMetrics().getAverageCharWidth(),
                        gc.getFontMetrics().getHeight()));
        gc.dispose();
    }

    /**
     * set control's Row Data for longer fields in a Row Layout.
     * 
     * @param comp
     */
    public static void assignLongFieldsRD(Control comp) {
        GC gc = new GC(comp);
        comp.setLayoutData(
                new RowData(16 * gc.getFontMetrics().getAverageCharWidth(),
                        gc.getFontMetrics().getHeight()));
        gc.dispose();
    }

    /**
     * set control's Grid Data for short fields in a Grid Layout.
     * 
     * @param comp
     */
    public static void assignShortFieldsGD(Control comp) {
        GC gc = new GC(comp);
        comp.setLayoutData(
                new GridData(4 * gc.getFontMetrics().getAverageCharWidth(),
                        gc.getFontMetrics().getHeight()));
        gc.dispose();
    }

    /**
     * set control's Row Data for short fields in a Row Layout.
     * 
     * @param comp
     */
    public static void assignShortFieldsRD(Control comp) {
        GC gc = new GC(comp);
        comp.setLayoutData(
                new RowData(4 * gc.getFontMetrics().getAverageCharWidth(),
                        gc.getFontMetrics().getHeight()));
        gc.dispose();
    }

    /**
     * @return Grid Data for a filler composite. Size 0. Take up space in a grid
     *         layout.
     */
    public static GridData getFillerGD() {
        return new GridData(0, 0);
    }
}

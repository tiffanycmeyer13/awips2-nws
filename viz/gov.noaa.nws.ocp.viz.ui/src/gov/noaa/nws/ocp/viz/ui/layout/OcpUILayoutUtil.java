/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.ui.layout;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Control;

/**
 * This class holds common Display values and utilities for GUI development.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- ---------------------------------
 * Mar 28, 2016 16003      amoore      Initial creation
 * Jan 30, 2019 59222      jwu         Adapt from OCP Climate project.
 * May 21, 2020 77847      jwu         Add style for PRIMARY_MODAL dialog.
 * </pre>
 *
 * @author amoore
 */
public final class OcpUILayoutUtil {

    /**
     * Plug-in ID.
     */
    public static final String PLUGIN_ID = "gov.noaa.nws.ocp.viz.ui";

    /**
     * Private constructor. This is a utility class.
     */
    private OcpUILayoutUtil() {
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
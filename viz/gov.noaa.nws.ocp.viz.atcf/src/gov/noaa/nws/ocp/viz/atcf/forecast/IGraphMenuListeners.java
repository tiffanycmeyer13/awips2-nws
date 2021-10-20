/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;

/**
 * Interface for a collection of menu actions in an ATCF 2-D graph window. Menu
 * is built as separate class with these pre-defined methods while the actual
 * implementation of these methods are done by each 2-D graph, including
 * intensity and wind radii graphs.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 25, 2020 75391      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public interface IGraphMenuListeners {

    static final IUFStatusHandler statusHanlder = UFStatus
            .getHandler(IGraphMenuListeners.class);

    /**
     * Print current graph
     */
    default void printGraphic() {
        statusHanlder.warn("File=>Print ... not implemented yet.");
    }

    /**
     * Print current graph in landscape mode
     */
    default void printGraphicAsLandscape() {
        statusHanlder.warn("File=>Print Landscape ... not implemented yet.");
    }

    /**
     * Saves current graph to a file.
     */
    default void saveGraphicToFile() {
        statusHanlder.warn("File=>Save Graphic to File...not implemented yet.");
    }

    /**
     * Email current graph to somebody
     */
    default void emailGraphic() {
        statusHanlder.warn("File=>Email Graphic ... not implemented yet.");
    }

    /**
     * Change Color
     */
    default void changeColor() {
        statusHanlder.warn("File=>Change Color ... not implemented yet.");
    }

    /**
     * Action when a quadrant (Northeast, Southeast, etc) is selected from menu.
     * 
     * @param quad
     *            Quadrant (0 = Northeast; 1 = Southeast; 2 = Southwest; 3 =
     *            Northwest; 4 = 4 Panel
     */
    default void quadrantSelected(int quad) {
        statusHanlder.warn(
                "Selected quadrant=>" + quad + " ... not implemented yet.");
    }

    /**
     * Action when a TAU (0,12,24,...) is selected from menu.
     * 
     * @param tau
     *            TAU hours
     */
    default void tauSelected(int tau) {
        statusHanlder.warn("Selected Tau=>" + tau + " ... not implemented yet.");
    }

    /**
     * Action when a wind radius is selected from menu.
     * 
     * @param radius
     *            Radius selected (34,50,64) knots
     */
    default void radiusSelected(int radius) {
        statusHanlder.warn(
                "Selected radius =>" + radius + " ... not implemented yet.");
    }

    /**
     * Action for "Select Aids".
     */
    default void selectAids() {
        statusHanlder.warn("Select Aids ... not implemented yet.");
    }

    /**
     * Action when a scale is selected.
     * 
     * @param scale
     *            scale selected (50, 100, 200, ...) in nm.
     */
    default void scaleSelected(int scale) {
        statusHanlder.warn(
                "Scale selected" + scale + " ... not implemented yet.");
    }

    /**
     * Action for "Help".
     */
    default void help() {
        statusHanlder.warn("Help ... not implemented yet.");
    }

    /**
     * Exit the graph window and also ask caller to update with the data from
     * the graph window.
     */
    default void exitGraphWindow() {
        statusHanlder.warn("Exit ... not implemented yet.");
    }
}
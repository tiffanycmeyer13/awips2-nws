/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

/**
 * The listener interface for receiving the DrawingCommandManager's stack sizes,
 * when either changes.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public interface DrawingCommandStackListener {

    /**
     * Invoked when the size or either the Undo or redo stack in the
     * DrawingCommandManager changes
     * 
     * @param undoSize
     * @param redoSize
     */
    public void stacksUpdated(int undoSize, int redoSize);

}

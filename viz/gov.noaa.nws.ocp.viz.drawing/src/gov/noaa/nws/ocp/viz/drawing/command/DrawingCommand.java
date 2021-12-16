/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import gov.noaa.nws.ocp.common.drawing.DrawingException;

/**
 * This Interface is used to implement drawing Commands that change are used to
 * change any objects in the resource. These commands could be used to implement
 * an Undo/Redo feature in the drawing tools when needed.
 * 
 * The necessary steps to make the desired change are implemented in the
 * execute() method, and the necessary steps to undo that action should be
 * implemented in the undo() method.
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
public interface DrawingCommand {

    /**
     * Executes the drawing command
     * 
     * @throws drawingException
     */
    public abstract void execute() throws DrawingException;

    /**
     * Un-does the operation implemented in the execute() method
     * 
     * @throws DrawingException
     */
    public abstract void undo() throws DrawingException;

}

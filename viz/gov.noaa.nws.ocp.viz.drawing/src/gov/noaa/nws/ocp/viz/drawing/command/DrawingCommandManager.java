/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Manages a list of DrawingCommand objects to implement and "undo/redo" feature
 * in the drawing tools. The manager executes commands when received and
 * maintains internal stacks that can be used to "undo" or "redo" a prevoius
 * command. The manager will also notify listeners when the size of ither stack
 * has changed.
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
public class DrawingCommandManager {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DrawingCommandManager.class);

    /*
     * Stack of commands that can be undone
     */
    private Deque<DrawingCommand> undo;

    /*
     * stack of commands that can be re-done
     */
    private Deque<DrawingCommand> redo;

    /*
     * List of command stack listeners
     */
    private Set<DrawingCommandStackListener> listeners;

    /**
     * No-arg Constructor
     */
    public DrawingCommandManager() {

        undo = new ArrayDeque<>();
        redo = new ArrayDeque<>();
        listeners = new HashSet<>();
    }

    /**
     * Executes a given DrawingCommand and saves it on the "undo" stack if it
     * executes without exception
     * 
     * @param command
     *            DrawingCommand to execute
     */
    public void addCommand(DrawingCommand command) {

        try {
            /*
             * excute command
             */
            command.execute();

            /*
             * Add command to undo stack and clear out redo stack if it isn't
             * empty
             */
            undo.push(command);
            if (!redo.isEmpty()) {
                redo.clear();
            }
        } catch (Exception e) {
            logger.error("DrawingCommandManager - " + e);
        } finally {
            // notify listeners
            stacksChanged();
        }

    }

    /**
     * Executes the undo() method of the DrawingCommand Object on the undo
     * stack, and then adds the command to the redo stack, if the undo was
     * executed without exception.
     */
    public void undo() {

        if (undo.isEmpty()) {
            return;
        }

        /*
         * Get last command from stack
         */
        DrawingCommand cmd = undo.pop();

        /*
         * Execute command's undo() method and add it to redo stack
         */
        try {
            cmd.undo();
            redo.push(cmd);
        } catch (Exception e) {
            logger.error("DrawingCommandManager - " + e);
        } finally {
            // notify listeners
            stacksChanged();
        }

    }

    /**
     * Runs the execute() method from the last command on the redo stack, and
     * adds it to the undo stack, if it ran without exception.
     */
    public void redo() {

        if (redo.isEmpty()) {
            return;
        }

        /*
         * get command from redo stack
         */
        DrawingCommand cmd = redo.pop();

        /*
         * run command's execute() method and add to undo stack
         */
        try {
            cmd.execute();
            undo.push(cmd);
        } catch (Exception e) {
            logger.error("DrawingCommandManager - " + e);
        } finally {
            // notify listeners
            stacksChanged();
        }

    }

    /**
     * Clear out both the undo and redo stacks. Remove all listeners
     */
    public void flushStacks() {
        undo.clear();
        redo.clear();
        listeners.clear();
    }

    /**
     * Clear out both the undo and redo stacks.
     */
    public void clearStacks() {
        undo.clear();
        redo.clear();
    }

    /**
     * Register a new stack listener
     * 
     * @param clisten
     *            command stack listener
     */
    public void addStackListener(DrawingCommandStackListener clisten) {
        listeners.add(clisten);
        // notify new listener
        clisten.stacksUpdated(undo.size(), redo.size());
    }

    /**
     * Remove the stack listener from the list of listeners
     * 
     * @param clisten
     *            command stack listener
     */
    public void removeStackListener(DrawingCommandStackListener clisten) {
        listeners.remove(clisten);
    }

    /**
     * notify all listeners that the one or more stack sizes have changed
     */
    private void stacksChanged() {

        for (DrawingCommandStackListener clist : listeners) {
            clist.stacksUpdated(undo.size(), redo.size());
        }
    }
}

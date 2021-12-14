/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import java.util.ArrayList;
import java.util.List;

import gov.noaa.nws.ocp.common.drawing.DrawingException;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;

/**
 * Replace a set of existing elements in a layer (product) with a set of new
 * elements. The original elements can be restored as an undo feature.
 * 
 * Note: This command works as "add" if no existing elements are provided. And
 * it works as "delete" if no new elements are provided.
 * 
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ReplaceElementsCommand implements DrawingCommand {

    /*
     * Layer that contains the element to be replaced
     */
    private DECollection parent;

    /*
     * The drawables to be replaced
     */
    private List<AbstractDrawableComponent> oldElements = null;

    /*
     * The new drawable elements
     */
    private List<AbstractDrawableComponent> newElements = null;

    /**
     * Constructor used to specify the product layer as well as the old and new
     * drawable element.
     * 
     * @param layer
     *            The layer contains the elements
     * @param oldElement
     *            Drawable element to replace
     * @param newElement
     *            New drawable element
     */
    public ReplaceElementsCommand(DECollection parent,
            AbstractDrawableComponent oldElement,
            AbstractDrawableComponent newElement) {

        this.parent = parent;

        if (this.oldElements == null) {
            this.oldElements = new ArrayList<>();
        } else {
            this.oldElements.clear();
        }

        this.oldElements.add(oldElement);

        if (this.newElements == null) {
            this.newElements = new ArrayList<>();
        } else {
            this.newElements.clear();
        }

        this.newElements.add(newElement);

    }

    /**
     * Constructor used to specify the product layer as well as the old and new
     * drawable element.
     * 
     * @param The
     *            layer contains the elements
     * @param oldElements
     *            Drawable elements to replace
     * @param newElements
     *            New drawable elements
     */
    public ReplaceElementsCommand(DECollection parent,
            List<AbstractDrawableComponent> oldElements,
            List<AbstractDrawableComponent> newElements) {

        this.parent = parent;
        this.oldElements = oldElements;
        this.newElements = newElements;

    }

    /**
     * Replaces drawable elements with new elements in a layer
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#execute()
     */
    @Override
    public void execute() throws DrawingException {

        if (parent != null) {
            if (oldElements != null) {
                for (AbstractDrawableComponent ade : oldElements) {
                    parent.removeElement(ade);
                }
            }

            if (newElements != null) {
                parent.add(newElements);
            }
        } else if (oldElements.size() == newElements.size()) {
            for (int ii = 0; ii < oldElements.size(); ii++) {
                AbstractDrawableComponent ade = oldElements.get(ii);
                if (ade.getParent() instanceof DECollection) {
                    DECollection dec = (DECollection) ade.getParent();
                    dec.removeElement(ade);
                    dec.add(newElements.get(ii));
                }
            }
        }
    }

    /**
     * Restores the original elements back in the layer
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#undo()
     */
    @Override
    public void undo() throws DrawingException {

        if (parent != null) {
            if (newElements != null) {
                for (AbstractDrawableComponent ade : newElements) {
                    parent.removeElement(ade);
                }
            }

            if (oldElements != null) {
                parent.add(oldElements);
            }
        } else if (oldElements.size() == newElements.size()) {
            for (int ii = 0; ii < newElements.size(); ii++) {
                AbstractDrawableComponent ade = newElements.get(ii);
                if (ade.getParent() instanceof DECollection) {
                    DECollection dec = (DECollection) ade.getParent();
                    dec.removeElement(ade);
                    dec.add(oldElements.get(ii));
                }
            }
        }
    }

}

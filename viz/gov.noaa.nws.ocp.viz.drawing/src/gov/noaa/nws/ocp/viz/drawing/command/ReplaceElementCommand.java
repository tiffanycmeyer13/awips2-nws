/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import java.util.List;

import gov.noaa.nws.ocp.common.drawing.DrawingException;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * This class contains the implementation to replace an existing element in a
 * product list with a new element. The original element can be restored as an
 * undo feature.
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
public class ReplaceElementCommand implements DrawingCommand {

    /*
     * product list
     */
    private List<Product> list;

    /*
     * layer that contains the element to be replaced
     */
    private Layer layer;

    /*
     * the drawable to be replaced
     */
    private AbstractDrawableComponent oldElement;

    /*
     * The new drawable element
     */
    private AbstractDrawableComponent newElement;

    /**
     * Constructor used to specify the product list as well as the old and new
     * drawable element.
     * 
     * @param list
     *            The product list
     * @param oldElement
     *            Drawable element to replace
     * @param newElement
     *            New drawable element
     */
    public ReplaceElementCommand(List<Product> list,
            AbstractDrawableComponent oldElement,
            AbstractDrawableComponent newElement) {
        this.list = list;
        this.oldElement = oldElement;
        this.newElement = newElement;
    }

    /**
     * Replaces one drawable element with another in a product list
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#execute()
     * @throws DrawingException
     *             if the oldElement is not found
     */
    @Override
    public void execute() throws DrawingException {

        for (Product currProd : list) {

            for (Layer currLayer : currProd.getLayers()) {

                if (currLayer.replace(oldElement, newElement)) {
                    layer = currLayer;
                    return;
                }
            }

        }

        throw new DrawingException(
                "Could not find specified element in current product list");

    }

    /**
     * restores the original element back in the product list
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#undo()
     */
    @Override
    public void undo() throws DrawingException {

        if (!layer.replace(newElement, oldElement)) {
            throw new DrawingException(
                    "Could not find original element in current product list for undo");
        }

    }

}

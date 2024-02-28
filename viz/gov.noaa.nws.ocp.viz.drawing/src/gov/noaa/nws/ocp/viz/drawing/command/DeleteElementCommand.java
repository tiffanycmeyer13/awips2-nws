/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import java.util.List;

import gov.noaa.nws.ocp.common.drawing.DrawingException;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * This class contains the implementation to delete a drawable element from a
 * product list. The element can be re-added for an undo feature.
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
public class DeleteElementCommand implements DrawingCommand {

    /*
     * product list from which element should be deleted
     */
    private List<Product> list;

    /*
     * layer from which element should be deleted
     */
    private DECollection collection;

    /*
     * drawable element to delete
     */
    private AbstractDrawableComponent comp;

    /**
     * Constructor used to specify the element and product list.
     * 
     * @param list
     *            Product list from which element should be deleted.
     * @param element
     *            - drawable element to delete.
     */
    public DeleteElementCommand(List<Product> list,
            AbstractDrawableComponent comp) {
        this.list = list;
        this.comp = comp;
    }

    /**
     * Removes the element from the product list. Saves the layer for possible
     * undo
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#execute()
     * @throws DrawingException
     *             if the element could not be found in the list
     */
    @Override
    public void execute() throws DrawingException {

        for (Product currProd : list) {

            for (Layer currLayer : currProd.getLayers()) {

                DECollection dec = currLayer.search(comp);

                if (dec != null) {
                    collection = dec;
                    dec.removeElement(comp);
                    return;
                }
            }

        }

        throw new DrawingException(
                "Could not find specified element in current product list");

    }

    /**
     * Re-adds the drawable element back to the original layer
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#undo()
     */
    @Override
    public void undo() throws DrawingException {

        collection.addElement(comp);

    }

}

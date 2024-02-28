/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

/*
 * 
 * Date created: 22 May 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.noaa.nws.ocp.common.drawing.DrawingException;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.DECollection;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * Implements a DrawingCommand to delete selected elements.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author B. Yin
 * @version 1.0
 */

public class DeleteSelectedElementsCommand implements DrawingCommand {

    /*
     * The Product list.
     */
    private List<Product> prodList;

    /*
     * Elements selected
     */
    private List<AbstractDrawableComponent> elSelected;

    /*
     * Saved version of the elements selected
     */
    private List<AbstractDrawableComponent> saveEl;

    /*
     * Element-layer map for selected elements
     */
    private HashMap<AbstractDrawableComponent, DECollection> elMap;

    /**
     * Constructor
     * 
     * @param list
     * @param elements
     */
    public DeleteSelectedElementsCommand(List<Product> list,
            List<AbstractDrawableComponent> elements) {

        this.prodList = list;
        elSelected = elements;
        saveEl = new ArrayList<>(elements);
        elMap = new HashMap<>();

    }

    /**
     * Remove selected elements and save the layer info for each removed el to a
     * map.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#execute()
     */
    @Override
    public void execute() throws DrawingException {

        for (AbstractDrawableComponent comp : saveEl) {

            for (Product prod : prodList) {

                for (Layer layer : prod.getLayers()) {

                    DECollection dec = layer.search(comp);
                    if (dec != null) {

                        dec.removeElement(comp);
                        elMap.put(comp, dec);
                    }
                }
            }
        }

        elSelected.clear();
    }

    /**
     * Add back all removed elements into the layers they belong to.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command.DrawingCommand#undo()
     */
    @Override
    public void undo() throws DrawingException {

        for (AbstractDrawableComponent comp : saveEl) {

            DECollection dec = elMap.get(comp);

            if (dec != null) {
                dec.addElement(comp);
            } else {
                throw new DrawingException(
                        "Coulnd't find the collection when restoring objects!");
            }

        }

    }

}

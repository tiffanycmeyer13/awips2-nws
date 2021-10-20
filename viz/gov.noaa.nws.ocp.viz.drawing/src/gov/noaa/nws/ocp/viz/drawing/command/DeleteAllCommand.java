/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.command;

import java.util.ArrayList;
import java.util.List;

import gov.noaa.nws.ocp.common.drawing.DrawingException;
import gov.noaa.nws.ocp.viz.drawing.elements.Product;

/**
 * This class contains the implementation needed to remove all products, layers
 * and elements from a Product list. The elements can be re-added for an undo
 * feature.
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

public class DeleteAllCommand implements DrawingCommand {

    /*
     * The Product list.
     */
    private List<Product> list;

    /*
     * saved version of the product list
     */
    private ArrayList<Product> saveList;

    /**
     * Constructor used to specify product list to empty
     * 
     * @param list
     */
    public DeleteAllCommand(List<Product> list) {
        this.list = list;
    }

    /**
     * Saves the product list, and then empties it.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command#execute()
     */
    @Override
    public void execute() throws DrawingException {

        saveList = new ArrayList<>(list);
        list.clear();

    }

    /**
     * Re-adds the saved product list to the original list
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.command#undo()
     */
    @Override
    public void undo() throws DrawingException {

        list.addAll(saveList);
    }

}

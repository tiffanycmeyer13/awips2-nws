/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.cwagenerator.action;

import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.viz.ui.editor.AbstractEditor;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.ui.pgen.tools.InputHandlerDefaultImpl;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.SigmetCommAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Product;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

/**
 * Implements input handler for mouse events for the selecting action.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 19, 2020 75767      wkwock     Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class VorsSelectHandler extends InputHandlerDefaultImpl {

    protected AbstractEditor mapEditor;

    protected PgenResource pgenrsc;

    private IUpdateFormatter formatter;

    /**
     * Flag for point moving cross the screen. inOut=0: outside the map bound,
     * inOut=1: inside the map bound..
     */
    protected int inOut = 1;

    public VorsSelectHandler(AbstractEditor mapEditor, PgenResource resource,
            IUpdateFormatter formatter) {
        this.mapEditor = mapEditor;
        this.pgenrsc = resource;
        this.formatter = formatter;
    }

    @Override
    public boolean handleMouseDown(int anX, int aY, int button) {
        if (button != 1) {
            return false;
        }
        Coordinate loc = mapEditor.translateClick(anX, aY);
        if (loc == null || shiftDown) {
            return false;
        }

        DrawableElement elSelected = null;
        Product selectedProduct = null;
        double minDistance = Double.MAX_VALUE;

        // get nearest element
        for (Product product : pgenrsc.getResourceData().getProductList()) {
            for (Layer layer : product.getLayers()) {
                Iterator<DrawableElement> it = layer.createDEIterator();
                while (it.hasNext()) {
                    DrawableElement element = it.next();

                    double dist = pgenrsc.getDistance(element, loc);
                    if (dist < minDistance && dist < 20) {
                        minDistance = dist;
                        elSelected = element;
                        selectedProduct = product;
                    }
                }
            }
        }

        if (elSelected == null) {
            return false;
        }

        pgenrsc.setSelected(elSelected);
        mapEditor.refresh();

        if (formatter instanceof SigmetCommAttrDlg) {
            SigmetCommAttrDlg sca = (SigmetCommAttrDlg) formatter;
            sca.setAbstractSigmet(elSelected);
        }

        if (selectedProduct != null) {
            formatter.updateFormatter(selectedProduct);
        }

        return true;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        return true;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int button) {
        return false;
    }
}

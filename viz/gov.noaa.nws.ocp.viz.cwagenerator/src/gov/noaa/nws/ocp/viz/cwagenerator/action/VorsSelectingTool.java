/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.cwagenerator.action;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.rsc.PgenResource;
import gov.noaa.nws.ncep.ui.pgen.tools.AbstractPgenDrawingTool;

/**
 * 
 * Class for select VORs
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2020  75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class VorsSelectingTool extends AbstractPgenDrawingTool {
    private IUpdateFormatter formatter;

    /**
     * Input handler for mouse event
     */
    private IInputHandler selectHandler;

    public VorsSelectingTool(AbstractEditor mapEditor,
            PgenResource drawingLayer,
            IUpdateFormatter cwaFormatterDlg) {
        this.mapEditor = mapEditor;
        this.drawingLayer = drawingLayer;
        this.formatter = cwaFormatterDlg;
    }

    @Override
    protected void activateTool() {
    }

    @Override
    public void deactivateTool() {
        super.deactivateTool();
    }

    /**
     * Returns the current mouse handler.
     * 
     * @return
     */
    public IInputHandler getMouseHandler() {
        if (this.selectHandler == null) {
            this.selectHandler = new VorsSelectHandler(mapEditor, drawingLayer,
                    formatter);
        }

        return this.selectHandler;

    }

    @Override
    public void resetMouseHandler() {
        setHandler(selectHandler);
    }

    /**
     * get the selected drawable element.
     * 
     * @return drawable element
     */
    public DrawableElement getSelectedDE() {
        DrawableElement de = null;

        if (drawingLayer != null) {
            de = drawingLayer.getSelectedDE();
        }

        return de;
    }
}

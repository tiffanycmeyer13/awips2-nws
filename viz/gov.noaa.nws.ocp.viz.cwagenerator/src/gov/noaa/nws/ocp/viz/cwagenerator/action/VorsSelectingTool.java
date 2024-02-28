/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.cwagenerator.action;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.tools.AbstractModalTool;

/**
 * 
 * Class for select VORs drawings
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2020  75767      wkwock      Initial creation
 * Sep 10, 2021 22802      wkwock      Remove PGEN dependence
 *
 * </pre>
 *
 * @author wkwock
 */
public class VorsSelectingTool extends AbstractModalTool {
    private IUpdateFormatter formatter;
    private AbstractEditor mapEditor = null;
    /**
     * Input handler for mouse event
     */
    private IInputHandler selectHandler;
    private CWAGeneratorResource drawingLayer;
    public VorsSelectingTool(AbstractEditor mapEditor,
            CWAGeneratorResource drawingLayer,
            IUpdateFormatter cwaFormatterDlg) {
        this.mapEditor = mapEditor;
        this.drawingLayer = drawingLayer;
        this.formatter = cwaFormatterDlg;
    }

    /**
     * Returns the current mouse handler.
     * 
     * @return
     */
    public IInputHandler getMouseHandler() {
        if (this.selectHandler == null) {
            this.selectHandler = new VorsSelectHandler(mapEditor, drawingLayer, formatter);
        }

        return this.selectHandler;
    }

    public void setDrawingLayer(CWAGeneratorResource drawingLayer) {
        this.drawingLayer=drawingLayer;
    }

    public void setHandler(IInputHandler selectHandler) {
        this.selectHandler=selectHandler;
    }

    @Override
    protected void deactivateTool() {
    }

    @Override
    protected void activateTool() {
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.AttrSettings;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.FrontAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.DECollection;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.tools.PgenMultiPointDrawingTool;

/**
 * 
 * Class for draw VORs
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
public class VorsDrawingTool extends PgenMultiPointDrawingTool {
    public VorsDrawingTool() {
        super();
    }

    public void setMapEditor(AbstractEditor editor) {
        this.mapEditor = editor;
    }

    public IInputHandler getMouseHandler() {

        if (this.mouseHandler == null) {

            this.mouseHandler = new VorsDrawingHandler();

        }

        return this.mouseHandler;
    }

    public class VorsDrawingHandler extends PgenMultiPointDrawingHandler {
        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (!drawingLayer.isEditable() || shiftDown)
                return false;

            if (button == 3) {
                if (points.isEmpty()) {
                    PgenUtil.setSelectingMode();

                } else if (points.size() < 2) {

                    drawingLayer.removeGhostLine();
                    points.clear();

                    mapEditor.refresh();

                } else {
                    DrawableType drawableType = DrawableType.CONV_SIGMET;
                    // create a new DrawableElement.
                    elem = def.create(drawableType, (IAttribute) attrDlg,
                            pgenCategory, pgenType, points,
                            drawingLayer.getActiveLayer());
                    attrDlg.setDrawableElement((DrawableElement) elem);
                    AttrSettings.getInstance()
                            .setSettings((DrawableElement) elem);

                    if (elem != null
                            &&  "Front".equalsIgnoreCase(elem.getPgenCategory())
                            && ((FrontAttrDlg) attrDlg).labelEnabled()) {

                        DECollection dec = new DECollection("labeledFront");
                        dec.setPgenCategory(pgenCategory);
                        dec.setPgenType(pgenType);
                        dec.addElement(elem);
                        drawingLayer.addElement(dec);

                        PgenUtil.setDrawingTextMode(true,
                                ((FrontAttrDlg) attrDlg).useFrontColor(), "",
                                dec);
                        elem = null;
                    } else {
                        // add the product to PGEN resource
                        drawingLayer.addElement(elem);
                    }

                    drawingLayer.removeGhostLine();

                    mapEditor.refresh();
                }
                points.clear();
                mapEditor.unregisterMouseHandler(this);
            }

            return true;
        }
    }
}

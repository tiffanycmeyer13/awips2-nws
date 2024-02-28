/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.InputAdapter;
import com.raytheon.viz.ui.tools.AbstractModalTool;

import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;

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
 * Sep 10, 2021 28802      wkwock      Remove PGEN from CWA
 * Mar 03, 2023 23449      wkwock      Fixed the exception when mouse point outside map
 *
 * </pre>
 *
 * @author wkwock
 */
public class VorsDrawingTool extends AbstractModalTool {
    private AbstractEditor mapEditor = null;

    private IInputHandler mouseHandler;

    /** A handler to the current drawing layer */
    private CWAGeneratorResource drawingLayer;

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

    public class VorsDrawingHandler extends InputAdapter {
        @Override
        public boolean handleMouseUp(int x, int y, int button) {
            if (button == 3) {
                drawingLayer.completeDrawing();
                mapEditor.refresh();
                mapEditor.unregisterMouseHandler(this);
            }

            return false;
        }

        @Override
        public boolean handleMouseDown(int x, int y, int button) {
            Coordinate loc = mapEditor.translateClick(x, y);
            if (button == 1 && loc != null) {
                PointLatLon point = new PointLatLon(loc.x, loc.y);
                drawingLayer.addPoint(point, false);
            }

            return false;
        }

        @Override
        public boolean handleMouseMove(int x, int y) {
            Coordinate loc = mapEditor.translateClick(x, y);
            if (loc != null) {
                // loc is null when mouse point outside of map
                PointLatLon point = new PointLatLon(loc.x, loc.y);
                drawingLayer.addPoint(point, true);
            }
            return false;
        }
    }

    protected boolean isResourceEditable() {
        if (drawingLayer == null) {
            return false;
        } else {
            return drawingLayer.isEditable();
        }
    }

    @Override
    protected void deactivateTool() {
        mapEditor.unregisterMouseHandler(getMouseHandler());
    }

    @Override
    protected void activateTool() {
        mapEditor.registerMouseHandler(getMouseHandler());
    }

    public void setDrawingLayer(CWAGeneratorResource drawingLayer) {
        this.drawingLayer = drawingLayer;
    }
}

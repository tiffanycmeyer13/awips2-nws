/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

import gov.noaa.nws.ocp.viz.drawing.display.IText.DisplayType;
import gov.noaa.nws.ocp.viz.drawing.elements.DrawableElement;

/**
 * A Default Element Container that can be used for most Drawable Elements.
 * Recreation of the IDisplayable objects is only done when zooming or if the
 * layer DisplayProperties change.
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
public class DefaultElementContainer extends AbstractElementContainer {

    private DisplayProperties saveProps = null;

    private float zoomLevel = 0;

    /**
     * @param element
     * @param mapDescriptor
     * @param target
     */
    public DefaultElementContainer(DrawableElement element,
            IMapDescriptor mapDescriptor, IGraphicsTarget target) {
        super(element, mapDescriptor, target);
    }

    /*
     * Draws to the given graphics target. Recreates the IDisplayable objects if
     * zooming or if the Layer properties change.
     * 
     * @see gov.noaa.nws.ncep.ui.pgen.display.AbstractElementContainer#draw(com.
     * raytheon.uf.viz .core.IGraphicsTarget,
     * com.raytheon.uf.viz.core.drawables.PaintProperties)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps,
            DisplayProperties dprops) {
        draw(target, paintProps, dprops, false);
    }

    /*
     * Draws to the given graphics target. Recreates the IDisplayable objects if
     * zooming or if the Layer properties change
     * 
     * @see gov.noaa.nws.ncep.ui.pgen.display.AbstractElementContainer#draw(com.
     * raytheon.uf.viz .core.IGraphicsTarget,
     * com.raytheon.uf.viz.core.drawables.PaintProperties, boolean)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps,
            DisplayProperties dprops, boolean needsCreate) {

        /*
         * For ghost drawing - "needsCreate && dprops == null" - It is always on
         * the active layer so DiaplayProperties' "filled" should be true while
         * "monoColor" should be false (using the element's color).
         */
        if (dprops == null) {
            dprops = new DisplayProperties(false, null, true);
        }

        if (needsCreate) {
            dprops.setLayerMonoColor(false);
            dprops.setLayerFilled(true);
        }

        // For normal drawing........
        if ((displayEls == null) || paintProps.isZooming()) {
            needsCreate = true;

            /*
             * Needs to set display properties, otherwise the layer color may
             * not take effect (e.g., after switching projection)
             */
            def.setLayerDisplayAttr(dprops.getLayerMonoColor(),
                    dprops.getLayerColor(), dprops.getLayerFilled());
        }

        if (paintProps.getZoomLevel() != zoomLevel) {
            needsCreate = true;
            zoomLevel = paintProps.getZoomLevel();
        }

        if (!dprops.equals(saveProps)) {
            def.setLayerDisplayAttr(dprops.getLayerMonoColor(),
                    dprops.getLayerColor(), dprops.getLayerFilled());
            needsCreate = true;
        } else if (element instanceof IText
                && ((IText) element).getDisplayType().equals(DisplayType.BOX)) {
            needsCreate = true;
        }

        if (needsCreate) {
            createDisplayables(paintProps);
        }

        saveProps = dprops;

        for (IDisplayable each : displayEls) {
            each.draw(target, paintProps);
        }
    }
}

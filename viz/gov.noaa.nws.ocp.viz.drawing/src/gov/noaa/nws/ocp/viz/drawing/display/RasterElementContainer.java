/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

import gov.noaa.nws.ocp.viz.drawing.elements.DrawableElement;

/**
 * An Element Container that can be used for most Symbol/Marker Elements.
 * Recreation of the IDisplayable objects is only done if the layer
 * DisplayProperties change. The raster images do not need to be recreated when
 * panning or zooming.
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
public class RasterElementContainer extends AbstractElementContainer {

    private DisplayProperties saveProps = null;

    /**
     * @param element
     * @param mapDescriptor
     * @param target
     */
    public RasterElementContainer(DrawableElement element,
            IMapDescriptor mapDescriptor, IGraphicsTarget target) {
        super(element, mapDescriptor, target);
    }

    /**
     * Draws to the given graphics target. Recreates the IDisplayable objects if
     * the Layer properties change.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.display.AbstractElementContainer#draw(com.raytheon.uf.viz
     *      .core.IGraphicsTarget,
     *      com.raytheon.uf.viz.core.drawables.PaintProperties)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps,
            DisplayProperties dprops) {
        draw(target, paintProps, dprops, false);
    }

    /**
     * Draws to the given graphics target. Recreates the IDisplayable objects if
     * the Layer properties change.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.display.AbstractElementContainer#draw(com.raytheon.uf.viz
     *      .core.IGraphicsTarget,
     *      com.raytheon.uf.viz.core.drawables.PaintProperties, boolean)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps,
            DisplayProperties dprops, boolean needsCreate) {

        if (displayEls == null) {
            needsCreate = true;

            /*
             * Needs to set display properties, otherwise the layer color may
             * not take effect (e.g., after switching projection)
             */
            def.setLayerDisplayAttr(dprops.getLayerMonoColor(),
                    dprops.getLayerColor(), dprops.getLayerFilled());
        }

        if ((dprops != null) && !dprops.equals(saveProps)) {
            def.setLayerDisplayAttr(dprops.getLayerMonoColor(),
                    dprops.getLayerColor(), dprops.getLayerFilled());
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

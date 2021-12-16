package gov.noaa.nws.ocp.viz.atcf.rsc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.map.IMapDescriptor;

import gov.noaa.nws.ocp.viz.drawing.display.AbstractElementContainer;
import gov.noaa.nws.ocp.viz.drawing.display.DefaultElementContainer;
import gov.noaa.nws.ocp.viz.drawing.display.DisplayElementFactory;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.drawing.elements.DrawableElement;

/**
 * Ghost drawing for the Atcf resource.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 17, 2019 71722      jwu         Adapted from PgenResourceGhost
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public class AtcfResourceGhost {

    private AbstractDrawableComponent component;

    private Map<Object, AbstractElementContainer> componentMap = new HashMap<>();

    /**
     * Draw the ghost
     * 
     * @param target
     * @param paintProps
     * @param df
     * @param descriptor
     */
    public void draw(IGraphicsTarget target, PaintProperties paintProps,
            DisplayElementFactory df, IMapDescriptor descriptor) {
        df.setLayerDisplayAttr(false, null, false);
        if (component != null) {
            Iterator<DrawableElement> iterator = component.createDEIterator();
            while (iterator.hasNext()) {
                DrawableElement element = iterator.next();
                drawElement(target, paintProps, element, descriptor);
            }
        }
    }

    /**
     * Creates displayables for an element using an ElementContainer and call
     * the displayables' draw() method to draw the element.
     * 
     * @param target
     *            Graphic target
     * @param paintProps
     *            Paint properties
     * @param el
     *            Input drawable element
     * @praram descriptor
     */
    private void drawElement(IGraphicsTarget target, PaintProperties paintProps,
            DrawableElement el, IMapDescriptor descriptor) {
        Object key = createKey(el);
        AbstractElementContainer graphic = componentMap.get(key);

        if (graphic == null) {
            graphic = new DefaultElementContainer(el, descriptor, target);
            componentMap.put(key, graphic);
        } else {
            graphic.setElement(el);
        }
        graphic.draw(target, paintProps, null, true);
    }

    private Object createKey(DrawableElement el) {
        return el.getElemCategory() + ":" + el.getElemType();
    }

    /**
     * Sets the ghost line for the ATCF drawing layer.
     * 
     * @param ghost
     */
    public void setGhost(AbstractDrawableComponent ghost) {
        this.component = ghost;
    }

    /*
     * Release resources held by the ghost elements.
     */
    public void dispose() {
        for (AbstractElementContainer aec : componentMap.values()) {
            aec.dispose();
        }
    }
}

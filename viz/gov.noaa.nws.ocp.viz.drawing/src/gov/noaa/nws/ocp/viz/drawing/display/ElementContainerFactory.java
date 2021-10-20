/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.map.MapDescriptor;

import gov.noaa.nws.ocp.viz.drawing.elements.DrawableElement;
import gov.noaa.nws.ocp.viz.drawing.elements.Symbol;

/**
 * This class contains the implementation needed to add a new drawable element
 * to an existing drawing resource.
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
public class ElementContainerFactory {

    // Private constructor
    private ElementContainerFactory() {
    }

    /**
     * Creates an ElementContainer based on the type of DrawableElement.
     * 
     * @param el
     * @param descriptor
     * @param target
     * @return
     */
    public static AbstractElementContainer createContainer(DrawableElement el,
            MapDescriptor descriptor, IGraphicsTarget target) {

        if ((el instanceof Symbol)) {
            return new RasterElementContainer(el, descriptor, target);
        } else {
            return new DefaultElementContainer(el, descriptor, target);
        }

    }

}

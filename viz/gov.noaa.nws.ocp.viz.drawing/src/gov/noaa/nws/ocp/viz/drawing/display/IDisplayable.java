/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;

/**
 * Interface used for all graphic objects in PGEN.
 * <P>
 * Its intended use is for the PGEN Resource to be able to draw and get rid of
 * graphic objects without needing to know the details of "how".
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
public interface IDisplayable {

    /**
     * Draws graphic objects to the specified graphics target with given paint
     * properties.
     * 
     * @param target
     * @param paintProps
     */
    public void draw(IGraphicsTarget target, PaintProperties paintProps);

    /**
     * Disposes of any resources held by the graphic objects
     */
    public void dispose();

}

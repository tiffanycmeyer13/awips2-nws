/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Contains a set of filled graphic shapes that can readily be displayed to a
 * graphics target.
 * <P>
 * Objects of this class are typically created from PGEN "drawable elements"
 * using the DisplayElementFactory class.
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
public class FillDisplayElement implements IDisplayable {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(FillDisplayElement.class);

    /**
     * The filled shapes to be displayed.
     */
    private IShadedShape shape;

    /**
     * Transparency of filled shapes. Values should range between 0.0 and 1.0
     */
    private float alpha;

    /**
     * Constructor used to set the filled shapes and transparency value
     * 
     * @param shape
     *            Filled shapes to be displayed
     * @param alpha
     *            Transparency of the filled shapes
     */
    public FillDisplayElement(IShadedShape shape, float alpha) {

        this.shape = shape;
        this.alpha = alpha;
    }

    /**
     * Disposes any graphic resources held by this object.
     * 
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#dispose()
     */
    @Override
    public void dispose() {

        shape.dispose();
    }

    /**
     * Draws the filled shapes to the specified graphics target
     * 
     * @param target
     *            Destination graphics target
     * @param paintProps
     *            PaintProperties
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#draw(com.raytheon.viz.core.IGraphicsTarget)
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps) {

        try {
            target.drawShadedShape(shape, alpha);
        } catch (VizException ve) {
            handler.warn("Shaded Shape not displayable." + ve);
        }
    }

}

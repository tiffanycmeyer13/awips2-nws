/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.display;

import java.awt.Color;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;

/**
 * Contains a set of line segments that can readily be displayed to a graphics target.
 * <P>
 * Objects of this class are typically created from PGEN "drawable elements" using the DisplayElementFactory 
 * class.
 * @author sgilbert
 */
/**
 * Contains a set of line segments that can readily be displayed to a graphics
 * target.
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
public class LineDisplayElement implements IDisplayable {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(LineDisplayElement.class);

    /**
     * The line segments to be displayed.
     */
    private IWireframeShape shape;

    /**
     * Color of the line segments.
     */
    private Color color;

    /**
     * Thickness of the line segments.
     */
    private float lineWidth;

    /**
     * Constructor used to set the line segments, and their corresponding color
     * and thickness.
     * 
     * @param shape
     *            - The set of line segments.
     * @param color
     *            - The color in which to display the line segments.
     * @param lineWidth
     *            - The desired line thickness.
     */
    public LineDisplayElement(IWireframeShape shape, Color color,
            float lineWidth) {

        this.shape = shape;
        this.color = color;
        this.lineWidth = lineWidth;
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
     * Draws the line segments to the specified graphics target.
     * 
     * @param target
     *            Destination graphics target.
     * @see gov.noaa.nws.ocp.viz.drawing.display.IDisplayable#draw()
     */
    @Override
    public void draw(IGraphicsTarget target, PaintProperties paintProps) {

        RGB shapeColor = new RGB(color.getRed(), color.getGreen(),
                color.getBlue());
        try {
            target.drawWireframeShape(shape, shapeColor, lineWidth);
        } catch (VizException ve) {
            handler.warn("Wireframe Shape not displayable." + ve);
        }
    }

}

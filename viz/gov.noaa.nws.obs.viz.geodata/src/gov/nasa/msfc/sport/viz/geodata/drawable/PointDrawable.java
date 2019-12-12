package gov.nasa.msfc.sport.viz.geodata.drawable;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.PointStyle;
import com.raytheon.uf.viz.core.drawables.IRenderable;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import org.locationtech.jts.geom.Point;

/**
 * PointDrawable
 *
 * Class which represents an extension of an IRenderable instance. The
 * PointDrawable class handles the instantiation and rendering of A
 * GeoDataRecord where the Geometry is an instance of a Point.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/06/2016     19064      jburks     Initial creation (DCS 19064)
 * 08/20/2016     19064     mcomerford  Adding fill and character rendering for Point.
 * </pre>
 *
 * @author jason.burks
 * @version 1.0
 */

public class PointDrawable implements IRenderable {

    private RGB color;

    private float pointSize;

    private Point point;

    private MapDescriptor desc;

    private PointStyle style = PointStyle.POINT;

    /**
     * Default constructor for a new PointDrawable.
     *
     * @param color
     *            The RGB value to set as the color of the rendered point.
     * @param point
     *            The Geometry object represented by this PointDrawable.
     * @param pointSize
     *            The size to render the point.
     * @param desc
     *            The MapDescriptor associated with this IRenderable.
     * @param style
     *            PointStyle dictating how to render the point.
     */
    public PointDrawable(RGB color, Point point, float pointSize,
            MapDescriptor desc, PointStyle style) {
        super();
        this.color = color;
        this.point = point;
        this.pointSize = pointSize;
        this.desc = desc;
        this.style = style;
    }

    @Override
    public void paint(IGraphicsTarget target, PaintProperties paintProps)
            throws VizException {
        double[] screenPoint = desc.worldToPixel(new double[] {
                point.getCoordinate().x, point.getCoordinate().y });
        target.drawPoint(screenPoint[0], screenPoint[1], 0, color, style,
                pointSize);
    }

    /**
     * Dispose of the IRenderable object when the associated Resource is
     * disposed of.
     */
    public void dispose() {
        /* No Drawables/WireframeShapes to dispose of */
    }

    /**
     * Apply magnification to the point based on StyleRules-defined values.
     * 
     * @param minPointSize
     *            The minimum point size (Attribute value, if configured) for
     *            the GeoDataRecords in the resource
     * @param maxPointSize
     *            The maximum point size (Attribute value, if configured) for
     *            the GeoDataRecords in the resource
     * @param minMag
     *            The minimum magnification ratio defined in StyleRules (1.0 is
     *            default)
     * @param maxMag
     *            The maximum magnification ratio defined in StyleRules (1.0 is
     *            default)
     */
    public void magnifyPoint(float minPointSize, float maxPointSize,
            float minMag, float maxMag) {
        this.pointSize = (((maxMag - minMag) * (this.pointSize - minPointSize))
                / (maxPointSize - minPointSize)) + minMag;
    }

    /**
     * @return the color
     */
    public RGB getColor() {
        return color;
    }

    /**
     * @param color
     *            the color to set
     */
    public void setColor(RGB color) {
        this.color = color;
    }

    /**
     * @return the pointSize
     */
    public float getPointSize() {
        return pointSize;
    }

    /**
     * @param pointSize
     *            the pointSize to set
     */
    public void setPointSize(float pointSize) {
        this.pointSize = pointSize;
    }

}

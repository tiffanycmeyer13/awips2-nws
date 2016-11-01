package gov.nasa.msfc.sport.viz.geodata.drawable;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.drawables.IRenderable;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * GeoDrawable
 *
 * Class which represents an extension of an IRenderable instance. The
 * GeoDrawable class handles the instantiation and rendering of A
 * GeoDataRecord where the Geometry is an instance of any Geometry not
 * represented by Points (LineStrings, Polygons, etc.).
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08/26/2016     19064     mcomerford  Initial creation (DCS 19064).
 * </pre>
 *
 * @author matt.comerford
 * @version 1.0
 */

public class GeoDrawable implements IRenderable {

    private RGB color;

    private IWireframeShape shape;

    private float lineWidth;

    private float alpha;

    private LineStyle style;

    private Geometry geometry;

    private MapDescriptor desc;

    /**
     * Default constructor for a new PolygonDrawable.
     *
     * @param color
     *            The RGB value to set as the color of the rendered Polygon.
     * @param poly
     *            The Geometry object represented by this PolygonDrawable.
     * @param lineWidth
     *            The thickness of the rendered line/edge of the Polygon
     * @param alpha
     *            The alpha (transparency) value of the Polygon.
     * @param style
     *            The style of how to render the line/edge (dotted, filled,
     *            etc.).
     * @param desc
     *            The MapDescriptor associated with this IRenderable.
     */
    public GeoDrawable(RGB color, Geometry geometry, float lineWidth,
            float alpha, LineStyle style, MapDescriptor desc) {
        super();
        this.color = color;
        this.geometry = geometry;
        this.lineWidth = lineWidth;
        this.alpha = alpha;
        this.style = style;
        this.desc = desc;
    }

    @Override
    public void paint(IGraphicsTarget target, PaintProperties paintProps)
            throws VizException {
        if (geometry instanceof Point) {

        } else {
            if (shape == null) {
                IWireframeShape wfs = target.createWireframeShape(false, desc);
                wfs.addLineSegment(geometry.getCoordinates());
                wfs.compile();
                shape = wfs;
            }
            target.drawWireframeShape(shape, color, lineWidth, style, alpha);
        }
    }

    /**
     * Dispose of the IRenderable object when the associated Resource is
     * disposed of.
     */
    public void dispose() {
        if (shape != null) {
            shape.dispose();
        }
    }

}
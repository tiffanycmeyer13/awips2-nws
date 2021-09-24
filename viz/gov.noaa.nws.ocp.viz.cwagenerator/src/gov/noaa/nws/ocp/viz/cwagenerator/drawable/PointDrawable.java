/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.drawable;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.raytheon.uf.viz.core.DrawableCircle;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;

import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;

/**
 * 
 * Class for draw a single point
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 27, 2021 22802      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class PointDrawable extends AbstractDrawableComponent {
    @XmlElement(name = "Point")
    private PointLatLon point;

    private DrawableCircle circle = null;

    private DrawableCircle vorCircle = null;

    public PointDrawable() {
    }

    public PointDrawable(PointLatLon point) {
        this.point = point;
    }

    @Override
    public void draw(IGraphicsTarget target, MapDescriptor descriptor)
            throws VizException {
        // draw regular point
        if (circle == null) {
            double[] startPoint = descriptor.worldToPixel(
                    new double[] { point.getLat(), point.getLon() });

            circle = new DrawableCircle();
            circle.setCoordinates(startPoint[0], startPoint[1], 0);
            circle.lineWidth = 5;
            circle.radius = 50.0;
        }
        circle.basics.color = color;
        if (isSelected) {
            circle.basics.color = selectedColor;
        }
        target.drawCircle(circle);

        // draw the VOR point
        if (vorCircle == null) {
            Point2D point2D = CWAGeneratorUtil.getVorCoor(point.getLat(),
                    point.getLon());
            double[] tmpPoint = descriptor.worldToPixel(
                    new double[] { point2D.getX(), point2D.getY() });

            vorCircle = new DrawableCircle();
            vorCircle.setCoordinates(tmpPoint[0], tmpPoint[1], 0);
            vorCircle.lineWidth = 5;
            vorCircle.radius = 50.0;
        }
        vorCircle.basics.color = vorColor;
        if (isSelected) {
            vorCircle.basics.color = selectedColor;
        }
        target.drawCircle(vorCircle);
    }

    public PointLatLon getPoint() {
        return point;
    }

    @Override
    public List<PointLatLon> getPoints() {
        List<PointLatLon> points = new ArrayList<>();
        points.add(point);
        return points;
    }

    public void setPoint(PointLatLon point) {
        this.point = point;
    }

}

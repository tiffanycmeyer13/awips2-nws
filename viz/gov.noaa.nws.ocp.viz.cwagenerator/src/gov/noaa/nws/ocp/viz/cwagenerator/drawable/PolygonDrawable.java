/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.drawable;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.DrawableLine;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;

import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;

/**
 * 
 * Class for draw a polygon
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 9, 2021  22802      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class PolygonDrawable extends AbstractDrawableComponent {
    private List<PointLatLon> newPoints = new ArrayList<>();

    @XmlElement(name = "coordinates")
    private List<PointLatLon> points = new ArrayList<>();

    private List<PointLatLon> linePoints = new ArrayList<>();

    private List<PointLatLon> vorLinePoints = new ArrayList<>();

    private PointLatLon dynamicPoint = null;

    public PolygonDrawable() {
    }

    public PolygonDrawable(List<PointLatLon> tmpPoints) {
        if (tmpPoints != null) {
            for (PointLatLon point : tmpPoints) {
                points.add(point);
            }
        }
    }

    public void addPoint(PointLatLon coordinate, boolean isDynamic) {
        if (isDynamic) {
            if (!newPoints.isEmpty() || !points.isEmpty()) {
                // dynamic point should not be the first point
                dynamicPoint = coordinate;
            }
        } else {
            newPoints.add(coordinate);
            dynamicPoint = null;
        }
    }

    @Override
    public void draw(IGraphicsTarget target, MapDescriptor descriptor)
            throws VizException {
        if (linePoints.isEmpty() && !points.isEmpty()) {
            createLinePoints(points, descriptor);
        }
        createLinePoints(newPoints, descriptor);

        // move new points
        if (!newPoints.isEmpty()) {
            for (PointLatLon coor : newPoints) {
                points.add(coor);
            }
            newPoints.clear();
        }
        PointLatLon tmpDynamicPoint = null;
        PointLatLon tmpDynamicVorPoint = null;
        if (dynamicPoint != null) {
            double[] tmpPoint = descriptor.worldToPixel(new double[] {
                    dynamicPoint.getLat(), dynamicPoint.getLon() });
            tmpDynamicPoint = new PointLatLon(tmpPoint[0], tmpPoint[1]);

            Point2D point2D = CWAGeneratorUtil.getVorCoor(dynamicPoint.getLat(),
                    dynamicPoint.getLon());
            tmpPoint = descriptor.worldToPixel(
                    new double[] { point2D.getX(), point2D.getY() });
            tmpDynamicVorPoint = new PointLatLon(tmpPoint[0], tmpPoint[1]);
        }

        drawLine(linePoints, color, tmpDynamicPoint, target);

        drawLine(vorLinePoints, vorColor, tmpDynamicVorPoint, target);
    }

    private void createLinePoints(List<PointLatLon> tmpPoints,
            MapDescriptor descriptor) {
        if (!tmpPoints.isEmpty()) {
            for (PointLatLon tmpPoint : tmpPoints) {
                double[] tmpPixel = descriptor.worldToPixel(
                        new double[] { tmpPoint.getLat(), tmpPoint.getLon() });
                PointLatLon point = new PointLatLon(tmpPixel[0], tmpPixel[1]);
                linePoints.add(point);

                Point2D point2D = CWAGeneratorUtil.getVorCoor(tmpPoint.getLat(),
                        tmpPoint.getLon());
                tmpPixel = descriptor.worldToPixel(
                        new double[] { point2D.getX(), point2D.getY() });
                point = new PointLatLon(tmpPixel[0], tmpPixel[1]);
                vorLinePoints.add(point);
            }
        }
    }

    private void drawLine(List<PointLatLon> linePoints, RGB color,
            PointLatLon dynamicCoor, IGraphicsTarget target)
            throws VizException {
        if (linePoints.isEmpty()) {
            return;
        }

        DrawableLine line = new DrawableLine();
        line.lineStyle = LineStyle.SOLID;
        line.width = 5;
        line.basics.color = color;
        if (isSelected) {
            line.basics.color = selectedColor;
        }

        for (PointLatLon coor : linePoints) {
            line.addPoint(coor.getLat(), coor.getLon());
        }
        if (dynamicCoor != null) {
            line.addPoint(dynamicCoor.getLat(), dynamicCoor.getLon());
        }

        // Complete the polygon
        line.addPoint(linePoints.get(0).getLat(), linePoints.get(0).getLon());
        target.drawLine(line);
    }

    public void setComplete() {
        dynamicPoint = null;
    }

    @Override
    public List<PointLatLon> getPoints() {
        return points;
    }
}

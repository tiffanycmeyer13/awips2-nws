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
 * Class for draw a line
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
public class LineDrawable extends AbstractDrawableComponent {
    /** line width in NM */
    @XmlElement(name = "width")
    private float width;

    private List<PointLatLon> newPoints = new ArrayList<>();

    @XmlElement(name = "coordinates")
    private List<PointLatLon> points = new ArrayList<>();

    private List<PointLatLon> linePoints = new ArrayList<>();

    private List<PointLatLon> vorLinePoints = new ArrayList<>();

    private PointLatLon dynamicPoint = null;

    public LineDrawable() {
    }

    public LineDrawable(List<PointLatLon> tmpPoints, float width) {
        this.width = width;

        if (tmpPoints != null) {
            for (PointLatLon point : tmpPoints) {
                this.points.add(point);
            }
        }
    }

    public void addPoint(PointLatLon point, boolean isDynamic) {
        if (isDynamic) {
            if (!newPoints.isEmpty() || !points.isEmpty()) {
                // dynamic point should not be the first point
                dynamicPoint = point;
            }
        } else {
            newPoints.add(point);
            dynamicPoint = null;
        }
    }

    @Override
    public void draw(IGraphicsTarget target, MapDescriptor descriptor)
            throws VizException {
        if (linePoints.isEmpty() && !points.isEmpty()) {
            // handle points from xml file
            createLinePoints(points, descriptor);
        }

        createLinePoints(newPoints, descriptor);
        if (!newPoints.isEmpty()) {
            for (PointLatLon coor : newPoints) {
                points.add(coor);
            }
            newPoints.clear();
        }
        PointLatLon dynamicCoor = null;
        PointLatLon dynamicVorCoor = null;
        if (dynamicPoint != null) {
            double[] tmpPoint = descriptor.worldToPixel(new double[] {
                    dynamicPoint.getLat(), dynamicPoint.getLon() });
            dynamicCoor = new PointLatLon(tmpPoint[0], tmpPoint[1]);

            Point2D point2D = CWAGeneratorUtil.getVorCoor(dynamicPoint.getLat(),
                    dynamicPoint.getLon());
            tmpPoint = descriptor.worldToPixel(
                    new double[] { point2D.getX(), point2D.getY() });
            dynamicVorCoor = new PointLatLon(tmpPoint[0], tmpPoint[1]);
        }

        drawLine(linePoints, color, dynamicCoor, target, descriptor);

        drawLine(vorLinePoints, vorColor, dynamicVorCoor, target, descriptor);
    }

    private void createLinePoints(List<PointLatLon> tmpPoints,
            MapDescriptor descriptor) {
        if (!tmpPoints.isEmpty()) {
            for (PointLatLon coor : tmpPoints) {
                double[] tmpPoint = descriptor.worldToPixel(
                        new double[] { coor.getLat(), coor.getLon() });
                PointLatLon tmpCoor = new PointLatLon(tmpPoint[0], tmpPoint[1]);
                linePoints.add(tmpCoor);

                Point2D point2D = CWAGeneratorUtil.getVorCoor(coor.getLat(),
                        coor.getLon());
                tmpPoint = descriptor.worldToPixel(
                        new double[] { point2D.getX(), point2D.getY() });
                tmpCoor = new PointLatLon(tmpPoint[0], tmpPoint[1]);
                vorLinePoints.add(tmpCoor);
            }
        }
    }

    private void drawLine(List<PointLatLon> linePoints, RGB color,
            PointLatLon dynamicCoor, IGraphicsTarget target,
            MapDescriptor descriptor) throws VizException {
        if (linePoints.isEmpty()) {
            return;
        }

        // convert to NM

        DrawableLine line = new DrawableLine();
        line.lineStyle = LineStyle.SOLID;

        // convert KM to NM
        line.width = (float) (width * 0.2699785
                / descriptor.getRenderableDisplay().getZoom());
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

        target.drawLine(line);
    }

    public void setComplete() {
        dynamicPoint = null;
    }

    @Override
    public List<PointLatLon> getPoints() {
        return points;
    }

    public float getWidth() {
        return width;
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.drawable;

import java.awt.geom.Point2D;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.DrawableCircle;
import com.raytheon.uf.viz.core.DrawableLine;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
/**
 **/
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;

import gov.noaa.nws.ocp.viz.cwagenerator.CWAGeneratorUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;

/**
 * 
 * class for draw a reference point
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
public class RefPointDrawable extends AbstractDrawableComponent {
    private static final int NUM_DIRECTIONS = 16;

    private PointLatLon point;

    private String name;

    private double refPointRadius = 0;

    private DrawableString drawableName = null;

    private DrawableCircle circle = null;

    private DrawableLine[] lines = null;

    public RefPointDrawable(String name, PointLatLon point,
            double refPointRadius) {
        this.name = name;
        this.point = point;
        this.refPointRadius = refPointRadius;
        color = new RGB(255, 156, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        drawableName = null;
    }

    public double getRefPointRadius() {
        return refPointRadius;
    }

    public void setRefPointRadius(double refPointRadius) {
        if (this.refPointRadius != refPointRadius) {
            this.refPointRadius = refPointRadius;
            lines = null;
        }
    }

    public void draw(IGraphicsTarget target, MapDescriptor descriptor)
            throws VizException {
        if (refPointRadius <= 0) {
            // no drawing
            return;
        }

        Point2D startLatLon = new Point2D.Double(point.getLat(),
                point.getLon());
        double[] startPoint = descriptor
                .worldToPixel(new double[] { point.getLat(), point.getLon() });

        // draw name
        if (name != null && !name.isBlank()) {
            if (drawableName == null) {
                drawableName = new DrawableString(name, color);
                drawableName.setCoordinates(startPoint[0], startPoint[1]);
            }
            target.drawStrings(drawableName);
        }

        // draw circle
        if (this.refPointRadius <= 5) {
            if (circle == null) {
                circle = new DrawableCircle();
                circle.setCoordinates(startPoint[0], startPoint[1], 0);
                circle.lineWidth = 1;
                circle.basics.color = color;
                circle.radius = 50.0;
            }
            target.drawCircle(circle);
            return;
        }

        // draw lines
        if (lines == null) {
            lines = new DrawableLine[NUM_DIRECTIONS];
            for (int direction = 0; direction < NUM_DIRECTIONS; direction++) {
                DrawableLine dLine = new DrawableLine();
                dLine.lineStyle = LineStyle.DASHED;
                dLine.width = 1;
                dLine.basics.color = color;
                dLine.setCoordinates(startPoint[0], startPoint[1]);
                Point2D endLatLon = CWAGeneratorUtil.latlonAddDistance(
                        startLatLon, refPointRadius, direction * 22.5);
                double[] endPoint = descriptor.worldToPixel(
                        new double[] { endLatLon.getX(), endLatLon.getY() });
                dLine.addPoint(endPoint[0], endPoint[1]);
                lines[direction] = dLine;
            }
        }
        target.drawLine(lines);
    }

    @Override
    public List<PointLatLon> getPoints() {
        // no need for points
        return null;
    }
}

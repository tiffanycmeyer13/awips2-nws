/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.cwagenerator.action;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.input.InputAdapter;

import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.LineDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PointDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PolygonDrawable;

/**
 * Implements input handler for mouse events for the selecting action.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 19, 2020 75767      wkwock      Initial creation
 * Sep 10, 2021 28802      wkwock      Remove PGEN dependence
 *
 * </pre>
 *
 * @author wkwock
 */
public class VorsSelectHandler extends InputAdapter {

    private AbstractEditor mapEditor;

    private CWAGeneratorResource cwaRsc;

    private AbstractDrawableComponent selectedDawable = null;

    private IUpdateFormatter formatter;

    public VorsSelectHandler(AbstractEditor mapEditor, CWAGeneratorResource resource,
            IUpdateFormatter formatter) {
        this.mapEditor = mapEditor;
        this.cwaRsc = resource;
        this.formatter = formatter;
    }

    @Override
    public boolean handleMouseDown(int anX, int aY, int button) {
        if (button != 1) {
            return false;
        }
        Coordinate loc = mapEditor.translateClick(anX, aY);
        if (loc == null) {
            return false;
        }

        double minDistance = Double.MAX_VALUE;

        // get nearest element
        AbstractDrawableComponent drawable = null;
        for (AbstractDrawableComponent tmpDrawable : cwaRsc.getConfigs()
                .keySet()) {
            if (tmpDrawable instanceof PointDrawable) {
                PointDrawable point = (PointDrawable) tmpDrawable;
                double distance = getDistance(loc, point.getPoint());
                if (distance < minDistance) {
                    minDistance = distance;
                    drawable = tmpDrawable;
                }
            } else if (tmpDrawable instanceof LineDrawable) {
                LineDrawable line = (LineDrawable) tmpDrawable;
                List<PointLatLon> coors = line.getPoints();
                if (coors.size() == 1) {
                    double distance = getDistance(loc, coors.get(0));
                    if (distance < minDistance) {
                        minDistance = distance;
                        drawable = tmpDrawable;
                    }
                } else if (coors.size() > 1) {
                    PointLatLon coor1 = coors.get(0);
                    for (int i = 1; i < coors.size(); i++) {
                        double distance = distanceFromLineSegment(loc, coor1,
                                coors.get(i));
                        if (distance < minDistance) {
                            minDistance = distance;
                            drawable = tmpDrawable;
                        }
                        coor1 = coors.get(i);
                    }
                }
            } else if (tmpDrawable instanceof PolygonDrawable) {
                PolygonDrawable polygon = (PolygonDrawable) tmpDrawable;
                List<PointLatLon> coors = polygon.getPoints();
                if (coors.size() == 1) {
                    double distance = getDistance(loc, coors.get(0));
                    if (distance < minDistance) {
                        minDistance = distance;
                        drawable = tmpDrawable;
                    }
                } else if (coors.size() > 1) {
                    PointLatLon coor1 = coors.get(0);
                    for (int i = 1; i < coors.size(); i++) {
                        double distance = distanceFromLineSegment(loc, coor1,
                                coors.get(i));
                        if (distance < minDistance) {
                            minDistance = distance;
                            drawable = tmpDrawable;
                        }
                        coor1 = coors.get(i);
                    }
                    double distance = distanceFromLineSegment(loc, coor1,
                            coors.get(0));
                    if (distance < minDistance) {
                        minDistance = distance;
                        drawable = tmpDrawable;
                    }

                }
            }
        }

        if (drawable == null || minDistance > 20
                || drawable == selectedDawable) {
            return false;
        }

        drawable.setSelected(true);
        if (selectedDawable != null) {
            selectedDawable.setSelected(false);
        }
        selectedDawable = drawable;
        mapEditor.refresh();

        formatter.updateFormatter(drawable);

        return false;
    }

    public double getDistance(Coordinate loc1, PointLatLon loc2) {
        Point ptScreen = new GeometryFactory()
                .createPoint(new Coordinate(loc1.x, loc1.y));
        return ptScreen.distance(new GeometryFactory()
                .createPoint(new Coordinate(loc2.getLat(), loc2.getLon())));
    }

    public static double distanceFromLineSegment(Coordinate loc,
            PointLatLon startPt, PointLatLon endPt) {
        AbstractEditor mapEditor = null;
        if (EditorUtil.getActiveEditor() instanceof AbstractEditor) {
            mapEditor = (AbstractEditor) EditorUtil.getActiveEditor();
        }

        if (mapEditor == null) {
            return Double.MAX_VALUE;
        }

        double[] locScreen = mapEditor.translateInverseClick(loc);

        Coordinate coor = new Coordinate(startPt.getLat(), startPt.getLon());
        double[] pt1 = mapEditor.translateInverseClick(coor);
        coor = new Coordinate(endPt.getLat(), endPt.getLon());
        double[] pt2 = mapEditor.translateInverseClick(coor);
        LineSegment seg = new LineSegment(new Coordinate(pt1[0], pt1[1]),
                new Coordinate(pt2[0], pt2[1]));

        return seg.distance(new Coordinate(locScreen[0], locScreen[1]));
    }
}

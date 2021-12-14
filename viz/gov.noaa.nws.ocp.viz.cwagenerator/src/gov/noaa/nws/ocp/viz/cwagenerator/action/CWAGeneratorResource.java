/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.action;

import gov.noaa.nws.ncep.edex.common.stationTables.Station;
import gov.noaa.nws.ncep.viz.common.SnapUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.LineDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PointDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PolygonDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.RefPointDrawable;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.EditableCapability;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;
import com.raytheon.viz.ui.input.EditableManager;

import org.geotools.referencing.GeodeticCalculator;

/**
 * 
 * Resource class for CWA generator
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2020 75767      wkwock      Initial creation
 * Aug 27, 2021 22802      wkwock      Remove PGEN/Renamed from CWAResource
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWAGeneratorResource
        extends AbstractVizResource<CWAGeneratorResourceData, MapDescriptor> {
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAGeneratorResource.class);

    private double refPointRadius = 0;

    private Map<String, RefPointDrawable> refPointDrawables = null;

    private List<PointLatLon> points = new ArrayList<>();

    private DrawingType drawType = DrawingType.AREA;

    private float width = 10.0f;

    private PointDrawable point = null;

    private PointDrawable dynamicPoint = null;

    private LineDrawable line = null;

    private PolygonDrawable polygon = null;

    private CaveSWTDialog ownerDlg;

    private AbstractDrawableComponent selectedDrawable = null;

    private Map<AbstractDrawableComponent, AbstractCWANewConfig> configList = null;

    public CWAGeneratorResource(CWAGeneratorResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties);
    }

    @Override
    public void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {

        drawRefPointRadius(target);
        if (drawType == DrawingType.ISOLATED) {
            if (point != null) {
                point.draw(target, descriptor);
            }
            if (dynamicPoint != null) {
                dynamicPoint.draw(target, descriptor);
            }
        } else if (drawType == DrawingType.LINE && line != null) {
            line.draw(target, descriptor);
        } else if (drawType == DrawingType.AREA && polygon != null) {
            polygon.draw(target, descriptor);
        }

        drawProducts(target);
    }

    public void updateRefPointRadius(double radius) {
        if (refPointRadius == radius) {
            return;
        }

        if (radius < 0) {
            this.refPointRadius = 0;
        } else if (radius > 400) {
            this.refPointRadius = 400;
        } else {
            this.refPointRadius = radius;
        }

        for (Entry<String, RefPointDrawable> refPoint : refPointDrawables
                .entrySet()) {
            refPoint.getValue().setRefPointRadius(refPointRadius);
        }

        this.issueRefresh();
    }

    /**
     * 
     * @param point
     *            latitude and longitude point
     * @param distance
     *            in nautical mile
     * @param direction
     *            in degrees
     * @return
     */
    public Point2D latlonAddDistance(Point2D point, double distance,
            double direction) {
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(point);
        distance = distance * 1852;// nautical mile to meter
        if (direction > 180) {
            direction -= 360;
        }
        gc.setDirection(direction, distance);
        return gc.getDestinationGeographicPoint();
    }

    private void setupRefPointDrawables() {
        refPointDrawables = new HashMap<>();
        for (Station station : SnapUtil.VOR_STATION_LIST) {
            PointLatLon latLon = new PointLatLon(station.getLongitude(),
                    station.getLatitude());
            RefPointDrawable tmpPoint = new RefPointDrawable(station.getStid(),
                    latLon, refPointRadius);
            refPointDrawables.put(station.getStid(), tmpPoint);
        }
    }

    private void drawRefPointRadius(IGraphicsTarget target) {
        if (refPointDrawables == null) {
            setupRefPointDrawables();
        }

        for (Entry<String, RefPointDrawable> tmpPoint : refPointDrawables
                .entrySet()) {
            try {
                tmpPoint.getValue().draw(target, descriptor);
            } catch (VizException e) {
                logger.error(
                        "Failed to draw reference point " + tmpPoint.getKey(),
                        e);
            }
        }
    }

    @Override
    public String getName() {
        return "CWA Resource";
    }

    @Override
    public void disposeInternal() {
        ownerDlg.close();
    }

    public void completeDrawing() {
        points.clear();
        dynamicPoint = null;
        if (drawType == DrawingType.LINE && line != null) {
            line.setComplete();
        } else if (drawType == DrawingType.AREA && polygon != null) {
            polygon.setComplete();
        }
    }

    /**
     * Add a point to the drawings
     * 
     * @param coordinate
     * @param isDynamic
     */
    public void addPoint(PointLatLon coordinate, boolean isDynamic) {
        if (drawType == DrawingType.ISOLATED) {// draw point
            points.add(coordinate);
            if (isDynamic) {
                dynamicPoint = new PointDrawable(coordinate);
            } else {

                point = new PointDrawable(coordinate);
                dynamicPoint = null;
            }
        } else if (drawType == DrawingType.LINE) {// draw line
            if (line == null) {
                line = new LineDrawable(null, width);
            }
            line.addPoint(coordinate, isDynamic);

        } else {// draw polygon
            if (polygon == null) {
                polygon = new PolygonDrawable(null);
            }
            polygon.addPoint(coordinate, isDynamic);
        }
        issueRefresh();
    }

    /**
     * Check if the resource is currently editable
     *
     * @return editable
     */
    public boolean isEditable() {
        return getCapability(EditableCapability.class).isEditable();
    }

    public void setEditable(boolean enable) {
        getCapability(EditableCapability.class).setEditable(enable);
        EditableManager.makeEditable(this,
                getCapability(EditableCapability.class).isEditable());
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        EditableManager.makeEditable(this,
                getCapability(EditableCapability.class).isEditable());
    }

    public void setDrawType(DrawingType type) {
        if (drawType != type) {
            points.clear();
            point = null;
            line = null;
            polygon = null;
        }
        drawType = type;
    }

    public void setOwnerDlg(CaveSWTDialog dialog) {
        this.ownerDlg = dialog;
    }

    public void setWidth(float width) {
        if (this.width != width) {
            points.clear();
            point = null;
            line = null;
            polygon = null;
        }
        this.width = width;
    }

    public List<PointLatLon> getCoordinates() {
        if (drawType == DrawingType.ISOLATED && point != null) {
            PointLatLon coor = point.getPoint();
            List<PointLatLon> coordinates = new ArrayList<>();
            coordinates.add(coor);
            return coordinates;
        } else if (drawType == DrawingType.LINE && line != null) {
            return line.getPoints();
        } else if (drawType == DrawingType.AREA && polygon != null) {
            return polygon.getPoints();
        } else if (selectedDrawable != null) {
            return selectedDrawable.getPoints();
        }
        return (new ArrayList<>());
    }

    /**
     * Only clear the drawings
     */
    public void clearDrawings() {
        points.clear();
        point = null;
        line = null;
        polygon = null;
        configList = null;
        this.issueRefresh();
    }

    private void drawProducts(IGraphicsTarget target) {
        if (configList == null) {
            return;
        }

        for (AbstractDrawableComponent drawable : configList.keySet()) {
            try {
                drawable.draw(target, descriptor);
            } catch (VizException e) {
                logger.error("Failed to draw point/line/polygon.", e);
            }
        }
    }

    public void setProductConfigs(CWAProductNewConfig tmpProductconfig) {
        configList = new HashMap<>();
        if (tmpProductconfig != null) {
            for (AbstractCWANewConfig config : tmpProductconfig
                    .getCwaProducts()) {
                configList.put(config.getDrawable(), config);
            }
        }
    }

    public Map<AbstractDrawableComponent, AbstractCWANewConfig> getConfigs() {
        return configList;
    }

    public AbstractCWANewConfig getSelectedConfig(
            AbstractDrawableComponent drawable) {
        selectedDrawable = drawable;
        return configList.get(drawable);
    }

    public AbstractDrawableComponent getDrawable() {
        if (drawType == DrawingType.ISOLATED && point != null) {
            return point;
        } else if (drawType == DrawingType.LINE && line != null) {
            return line;
        } else if (drawType == DrawingType.AREA && polygon != null) {
            return polygon;
        } else if (selectedDrawable != null) {
            return selectedDrawable;
        }

        return null;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/

package gov.noaa.nws.ocp.viz.firewx.rsc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.bufrua.UAObs;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.DrawableCircle;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.EditableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.MagnificationCapability;

/**
 * Provides a resource that will display plot data for a given reference time.
 *
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Feb 07, 2017  18784    wkwock      Initial creation.
 * Dec 01, 2017  5863     mapeters    Change dataTimes to a NavigableSet
 *
 * </pre>
 *
 * @author wkwock
 */

public class FirewxResource
        extends AbstractVizResource<FirewxResourceData, IMapDescriptor> {

    /** input handler */
    private FirewxInputHandler inputManager;

    /** no data message */
    private static final String NO_DATA = "NO DATA";

    /** all records */
    private Collection<UAObs> allRecords = new HashSet<>();

    /** all records grouped by time */
    private Map<DataTime, Collection<UAObs>> groupedRecords = new HashMap<>();

    protected FirewxResource(FirewxResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties, false);
        this.inputManager = new FirewxInputHandler(this);
        getCapability(EditableCapability.class).setEditable(true);
        resourceData.addChangeListener(new IResourceDataChanged() {
            @Override
            public void resourceChanged(ChangeType type, Object object) {
                if (type == ChangeType.DATA_UPDATE) {
                    if (object instanceof PluginDataObject[]) {
                        addRecords((PluginDataObject[]) object);
                    }
                }
            }
        });
    }

    /**
     * get current records
     *
     * @return current records
     */
    protected Collection<UAObs> getCurrentRecords() {
        return groupedRecords.get(descriptor.getTimeForResource(this));
    }

    /**
     * add new records
     *
     * @param records
     */
    public synchronized void addRecords(PluginDataObject... records) {
        for (PluginDataObject record : records) {
            if (record instanceof UAObs) {
                allRecords.add((UAObs) record);
            }
        }

        Map<DataTime, Collection<UAObs>> newGroupedRecords = new HashMap<>();
        for (PluginDataObject record : allRecords) {
            if (record instanceof UAObs) {
                UAObs uaObs = (UAObs) record;
                DataTime normTime = getNormalizedTime(uaObs.getDataTime());
                Collection<UAObs> uaObsList = newGroupedRecords.get(normTime);
                if (uaObsList == null) {
                    uaObsList = new ArrayList<>();
                    uaObsList.add(uaObs);
                    newGroupedRecords.put(normTime, uaObsList);
                } else {
                    uaObsList.add(uaObs);
                }
            }
        }

        this.dataTimes.retainAll(groupedRecords.keySet());
        this.dataTimes.addAll(groupedRecords.keySet());
        this.groupedRecords = newGroupedRecords;
    }

    /**
     * get normalized time
     *
     * @param time
     * @return normalized time
     */
    private DataTime getNormalizedTime(DataTime time) {
        if (this.resourceData.getBinOffset() != null) {
            return this.resourceData.getBinOffset().getNormalizedTime(time);
        } else {
            return time;
        }
    }

    @Override
    public synchronized void remove(DataTime dataTime) {
        Collection<UAObs> records = groupedRecords.remove(dataTime);
        if (records != null) {
            allRecords.removeAll(records);
        }
        super.remove(dataTime);
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {
        String message = NO_DATA;
        UAObs station = inputManager.getClosestRecord();
        if (station != null) {
            message = station.getStationId();
        }
        return message;
    }

    @Override
    protected void disposeInternal() {
        getResourceContainer().unregisterMouseHandler(inputManager);
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        DataTime time = paintProps.getDataTime();
        if (time == null) {
            return;
        }

        Collection<UAObs> records = this.groupedRecords.get(time);
        if (records == null) {
            return;
        }

        RGB color = getCapability(ColorableCapability.class).getColor();
        List<DrawableCircle> circles = new ArrayList<>(records.size());
        for (UAObs record : records) {
            double lat = record.getLatitude();
            double lon = record.getLongitude();
            double[] pixel = descriptor.worldToPixel(new double[] { lon, lat });
            DrawableCircle circle = new DrawableCircle();
            circle.setCoordinates(pixel[0], pixel[1]);
            circle.screenRadius = getRadius();
            circle.numberOfPoints = (int) (circle.screenRadius * 4);
            circle.basics.color = color;
            circle.filled = true;
            circles.add(circle);
        }
        target.drawCircle(circles.toArray(new DrawableCircle[0]));

    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        getResourceContainer().registerMouseHandler(inputManager);
    }

    /**
     * get radius
     *
     * @return radius
     */
    private double getRadius() {
        return 5 * getCapability(MagnificationCapability.class)
                .getMagnification();
    }

    @Override
    public String getName() {
        return resourceData.getPlotSource();
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc.mosaic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.Quantity;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.RGB;
import org.geotools.geometry.DirectPosition2D;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractRequestableResourceData;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IRefreshListener;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.uf.viz.core.rsc.capabilities.AbstractCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.extratext.IExtraTextGeneratingResource;
import com.raytheon.uf.viz.core.rsc.groups.BestResResource;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogateMap;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogator;
import com.raytheon.viz.radar.rsc.MosaicPaintProperties;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.viz.odim.rsc.ODIMRadialResource;
import gov.noaa.nws.ocp.viz.odim.rsc.ODIMRadialResource.AugmentedRecord;
import gov.noaa.nws.ocp.viz.odim.rsc.mosaic.ODIMMosaicRendererFactory.IRadarMosaicRenderer;
import tech.units.indriya.format.SimpleUnitFormat;

/**
 * Copied from com.raytheon.viz.radar.rsc.mosaic.RadarMosaicResource and
 * modified.
 *
 * Implements Radar Mosaic display
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMMosaicResource extends
        AbstractVizResource<ODIMMosaicResourceData, MapDescriptor> implements
        IResourceDataChanged, IExtraTextGeneratingResource, IRefreshListener {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ODIMMosaicResource.class);

    private static final RGB DEFAULT_COLOR = new RGB(255, 255, 255);

    protected IRadarMosaicRenderer mosaicRenderer;

    protected boolean initColorMap = false;

    private String groupName = null;

    protected String unitString = null;

    protected boolean force = false;

    protected DataTime lastTime = null;

    protected Map<AbstractVizResource<?, ?>, DataTime[]> timeMatchingMap = new HashMap<>();

    protected Job timeUpdateJob = new Job("Time Matching Mosaic") {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            updateTimes();
            return Status.OK_STATUS;
        }

    };

    protected ODIMMosaicResource(ODIMMosaicResourceData rrd,
            LoadProperties loadProps) {
        super(rrd, loadProps, false);
        timeUpdateJob.setSystem(true);
        rrd.addChangeListener(this);

        if (this.getCapability(ColorableCapability.class).getColor() == null) {
            this.getCapability(ColorableCapability.class)
                    .setColor(DEFAULT_COLOR);
        }

        // add listener for underlying resources
        for (ResourcePair rp : getResourceList()) {
            if (rp.getResourceData() != null) {
                rp.getResourceData().addChangeListener(this);
            }
        }
    }

    @Override
    protected void disposeInternal() {
        mosaicRenderer.dispose();
        for (ResourcePair rp : getResourceList()) {
            if (rp.getResource() != null) {
                rp.getResource().dispose();
            }
        }
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        mosaicRenderer = ODIMMosaicRendererFactory
                .createNewRenderer(resourceData.getMosaicType());

        // We want to init the most severe resource first so the colormap
        // matches.
        ResourcePair mostSevere = null;
        int severity = -1;
        for (ResourcePair rp : getResourceList()) {
            int recSeverity = getSeverity(rp);
            if (severity < recSeverity) {
                mostSevere = rp;
                severity = recSeverity;
            }

        }
        if (mostSevere != null) {
            mostSevere.getResource().init(target);
            mostSevere.getResource().registerListener(this);
        }
        // add listener for underlying resources
        for (ResourcePair rp : getResourceList()) {
            if (rp.getResource() != null && rp != mostSevere) {
                rp.getResource().init(target);
                rp.getResource().registerListener(this);
            }
        }
    }

    private int getSeverity(ResourcePair rp) {
        int maxSeverity = -1;
        if (rp.getResource() == null) {
            // nothing.
        } else if (rp.getResource() instanceof BestResResource) {
            for (ResourcePair rp1 : ((BestResResource) rp.getResource())
                    .getResourceList()) {
                int severity = getSeverity(rp1);
                if (severity > maxSeverity) {
                    maxSeverity = severity;
                }
            }
        } else if (rp.getResource() instanceof ODIMRadialResource) {
            /*
             * The radar plugin has a concept of "most severe" record, but there
             * is currently no equivalent for the ODIM plugin. Just return "1"
             * for any ODIM resource.
             */
            maxSeverity = 1;
        }
        return maxSeverity;
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        DataTime[] frameTimes = paintProps.getFramesInfo().getTimeMap()
                .get(this);
        if (!Arrays.equals(timeMatchingMap.get(this), frameTimes)) {
            timeUpdateJob.schedule();
            force = true;
        }
        if (force) {
            redoTimeMatching(frameTimes);
        }
        List<ODIMRecord> recordsToMosaic = constructRecordsToMosaic();
        if (!recordsToMosaic.isEmpty()) {
            DataTime curTime = getTimeForResource(this);
            synchronized (this) {
                boolean forceIt = force || !curTime.equals(lastTime);
                force = false;
                lastTime = curTime;
                mosaicRenderer.mosaic(target,
                        new MosaicPaintProperties(paintProps, forceIt), this);
            }

        }
    }

    /**
     * Creates the buffer containing the data for the mosaic'ed radars.
     *
     * @param target
     * @param geo
     * @return
     * @throws VizException
     */
    @SuppressWarnings("unchecked")
    private List<ODIMRecord> constructRecordsToMosaic() throws VizException {
        List<ODIMRecord> recordsToMosaic = new ArrayList<>();

        // Build list of radarRecords to mosaic
        for (ResourcePair pair : getResourceList()) {
            AbstractVizResource<?, ?> rsc = pair.getResource();
            DataTime time = getTimeForResource(rsc);
            if (rsc instanceof BestResResource) {
                rsc = ((BestResResource) rsc).getBestResResource(time);
            }
            if (rsc instanceof ODIMRadialResource) {
                ODIMRadialResource rr = (ODIMRadialResource) rsc;

                AugmentedRecord rec = rr.getAugmentedRecord(time);
                if (rec != null) {
                    if (rec.getStoredDataAsync() != null) {
                        recordsToMosaic.add(rec.getODIMRecord());
                    } else {
                        issueRefresh();
                    }
                }
            }
        }

        if (!recordsToMosaic.isEmpty() && !initColorMap) {

            ColorMapParameters params = null;

            for (ResourcePair rp : getResourceList()) {
                if (rp.getResource() != null && rp.getResource()
                        .hasCapability(ColorMapCapability.class)) {
                    params = rp.getResource()
                            .getCapability(ColorMapCapability.class)
                            .getColorMapParameters();
                    break;
                }
            }
            if (params != null && params.getColorMap() != null) {
                for (ResourcePair rp : getResourceList()) {
                    if (rp.getResource() != null) {
                        rp.getResource().getCapability(ColorMapCapability.class)
                                .setColorMapParameters(params);
                    }
                }
                unitString = SimpleUnitFormat
                        .getInstance(SimpleUnitFormat.Flavor.ASCII)
                        .format(params.getDisplayUnit());
                initColorMap = true;
            }
        }

        return recordsToMosaic;
    }

    public DataTime getTimeForResource(AbstractVizResource<?, ?> rsc) {
        DataTime[] dt = timeMatchingMap.get(rsc);
        int idx = descriptor.getFramesInfo().getFrameIndex();

        if (dt == null || dt.length <= idx || idx < 0) {
            return null;
        }

        return dt[idx];
    }

    /**
     * redoTimeMatching will not trigger an server requests and should be safe
     * to run within paint to guarantee that the latest times for any resources
     * match the frame times for the mosaic resource.
     *
     * @param frameTimes
     * @throws VizException
     */
    private void redoTimeMatching(DataTime[] frameTimes) throws VizException {
        timeMatchingMap.clear();
        if (frameTimes == null) {
            return;
        }
        timeMatchingMap.put(this, frameTimes);
        for (ResourcePair pair : getResourceList()) {
            DataTime[] availableTimes = pair.getResource().getDataTimes();
            DataTime[] displayTimes = timeMatch(frameTimes, availableTimes);
            timeMatchingMap.put(pair.getResource(), displayTimes);
        }
    }

    /**
     * Update times will cause all times to be requested for all child resources
     * and possibly also trigger data requests therefore it should always be run
     * off the UI thread, preferably in the timeUpdateJob.
     */
    private void updateTimes() {
        DataTime[] frameTimes = descriptor.getTimeMatchingMap().get(this);
        if (frameTimes == null) {
            /*
             * This has not been time matched so cannot time match against
             * mosaiced resources.
             */
            issueRefresh();
            return;
        }
        for (ResourcePair pair : getResourceList()) {
            try {
                if (!(pair
                        .getResourceData() instanceof AbstractRequestableResourceData)) {
                    continue;
                }
                AbstractRequestableResourceData arrd = (AbstractRequestableResourceData) pair
                        .getResourceData();
                DataTime[] availableTimes = arrd.getAvailableTimes();

                DataTime[] displayTimes = timeMatch(frameTimes, availableTimes);
                // request any new times.
                PluginDataObject[] pdos = arrd.getLatestPluginDataObjects(
                        displayTimes, availableTimes);
                if (pdos.length > 1) {
                    resourceData.update(pdos);
                    refresh();
                }
                // remove any extra times
                List<DataTime> displayList = Arrays.asList(displayTimes);
                List<DataTime> frameList = Arrays.asList(frameTimes);
                for (DataTime availableTime : pair.getResource()
                        .getDataTimes()) {
                    DataTime adjAvailTime = availableTime;
                    if (resourceData.getBinOffset() != null) {
                        adjAvailTime = resourceData.getBinOffset()
                                .getNormalizedTime(availableTime);
                    }
                    if (!frameList.contains(adjAvailTime)
                            && !displayList.contains(availableTime)) {
                        pair.getResourceData().fireChangeListeners(
                                ChangeType.DATA_REMOVE, availableTime);
                    }
                }
            } catch (VizException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);
            }
        }
    }

    /**
     * Attempt to match the times for the mosaic with the times for an
     * individual radar.
     *
     * @param frameTimes
     *            the frame times for the mosaic resource
     * @param availableTimes
     *            the times for a radar within a mosaic
     * @return
     */
    private DataTime[] timeMatch(DataTime[] frameTimes,
            DataTime[] availableTimes) {
        DataTime[] displayTimes = new DataTime[frameTimes.length];
        for (int i = 0; i < frameTimes.length; i++) {
            DataTime frameTime = frameTimes[i];
            if (frameTime == null) {
                continue;
            }
            if (resourceData.getBinOffset() != null) {
                frameTime = resourceData.getBinOffset()
                        .getNormalizedTime(frameTime);
                long frameValid = frameTime.getMatchValid();
                // Match at twice the range of binOffset this makes
                // things much smoother
                int interval = resourceData.getBinOffset().getInterval() * 2000;
                for (DataTime displayTime : availableTimes) {
                    if (displayTime == null) {
                        continue;
                    }
                    long dispValid = displayTime.getMatchValid();
                    if (Math.abs(dispValid - frameValid) < interval) {
                        if (displayTimes[i] != null) {
                            long d1 = Math.abs(frameValid
                                    - displayTimes[i].getMatchValid());
                            long d2 = Math.abs(frameValid - dispValid);
                            if (d1 < d2) {
                                continue;
                            }
                        }
                        displayTimes[i] = displayTime;
                    }
                }
            } else if (Arrays.asList(availableTimes).contains(frameTime)) {
                displayTimes[i] = frameTime;
            }
        }
        return displayTimes;
    }

    @Override
    public void setDescriptor(MapDescriptor descriptor) {
        super.setDescriptor(descriptor);

        for (ResourcePair rp : this.resourceData.getResourceList()) {
            @SuppressWarnings("unchecked")
            AbstractVizResource<?, MapDescriptor> rsc = (AbstractVizResource<?, MapDescriptor>) rp
                    .getResource();
            if (rsc != null) {
                rsc.setDescriptor(descriptor);
            }
        }
    }

    /**
     * Mosaic inspect method, heavily dependent on ODIMRadialResource's
     * interrogate method is string keys
     */
    @Override
    public String inspect(ReferencedCoordinate latLon) throws VizException {

        Coordinate coord = null;

        try {
            coord = latLon.asLatLon();
        } catch (Exception e) {
            // ignore
        }

        Map<ODIMRadialResource, DataTime> rscTimeMap = new HashMap<>();
        Map<ODIMRadialResource, InterrogateMap> rscInspectMap = new HashMap<>();
        ODIMRadialResource highestRsc = null;
        String inspectString = null;
        if (coord != null) {
            List<ODIMRadialResource> resources = new ArrayList<>();
            double minDist = Double.POSITIVE_INFINITY;
            for (ResourcePair rp : getResourceList()) {
                if (rp.getResource() != null) {
                    AbstractVizResource<?, ?> rsc = rp.getResource();
                    DataTime time = getTimeForResource(rsc);
                    if (rsc instanceof BestResResource) {
                        rsc = ((BestResResource) rsc).getBestResResource(time);
                    }
                    if (rsc instanceof ODIMRadialResource) {
                        @SuppressWarnings("unchecked")
                        ODIMRadialResource rr = (ODIMRadialResource) rsc;
                        rscTimeMap.put(rr, time);
                        try {
                            // Everything in this try block is to only sample
                            // records within range
                            AugmentedRecord arec = rr.getAugmentedRecord(time);
                            ODIMRecord md = arec.getODIMRecord();
                            if (md == null) {
                                continue;
                            } else {
                                int range = md.getGateResolution()
                                        * md.getNumBins();
                                MathTransform ll2crs = MapUtil
                                        .getTransformFromLatLon(arec.getCRS());
                                DirectPosition2D p1 = new DirectPosition2D(
                                        coord.x, coord.y);
                                DirectPosition2D p2 = new DirectPosition2D(
                                        md.getLongitude(), md.getLatitude());
                                ll2crs.transform(p1, p1);
                                ll2crs.transform(p2, p2);
                                if (p1.distance(p2) < minDist) {
                                    highestRsc = rr;
                                    minDist = p1.distance(p2);
                                }
                                if (p1.distance(p2) > range) {
                                    continue;
                                }
                            }
                        } catch (Exception e) {
                            throw new VizException(e);
                        }
                        resources.add(rr);
                    }
                }
            }
            // no on is within range.
            if (resources.isEmpty() && highestRsc != null) {
                resources.add(highestRsc);
            }

            for (ODIMRadialResource rr : resources) {
                InterrogateMap vals = rr.interrogate(rscTimeMap.get(rr), coord);
                if (vals != null) {
                    rscInspectMap.put(rr, vals);
                }
            }
            Double highestVal = Double.NEGATIVE_INFINITY;
            // Now loop through and find highest value
            for (ODIMRadialResource rr : rscInspectMap.keySet()) {
                InterrogateMap valMap = rscInspectMap.get(rr);
                Quantity<?> num = valMap.get(Interrogator.VALUE);
                if (num != null) {
                    Double d = num.getValue().doubleValue();
                    if (d > highestVal) {
                        highestVal = d;
                        highestRsc = rr;
                    }
                }
            }
        }

        if (highestRsc != null) {
            inspectString = highestRsc.inspect(rscTimeMap.get(highestRsc),
                    rscInspectMap.get(highestRsc));
        }
        return inspectString == null ? "NO DATA" : inspectString;
    }

    @Override
    public void project(CoordinateReferenceSystem mapData) throws VizException {
        for (ResourcePair rp : getResourceList()) {
            if (rp.getResource() != null) {
                rp.getResource().project(mapData);
            }
        }
        force = true;
    }

    public ResourceList getResourceList() {
        return this.resourceData.getResourceList();
    }

    @Override
    public void resourceChanged(ChangeType type, Object object) {
        switch (type) {
        case DATA_UPDATE:
            for (PluginDataObject pdo : (PluginDataObject[]) object) {
                DataTime time = pdo.getDataTime();
                if (resourceData.getBinOffset() != null) {
                    time = resourceData.getBinOffset().getNormalizedTime(time);
                }
                synchronized (dataTimes) {
                    dataTimes.add(time);
                }
                if (!Arrays.equals(timeMatchingMap.get(this),
                        descriptor.getFramesInfo().getTimeMap().get(this))) {
                    timeUpdateJob.schedule();
                }
            }
            break;
        case DATA_REMOVE:
            DataTime time = (DataTime) object;
            if (resourceData.getBinOffset() != null) {
                time = resourceData.getBinOffset().getNormalizedTime(time);
            }
            dataTimes.remove(time);
            break;
        case CAPABILITY:
            AbstractCapability cap = (AbstractCapability) object;
            /*
             * Since mosaic shares capabilities, need to make sure resourceData
             * is always set to the mosaic resource data so that all resources
             * are notified.
             */
            if (cap.getResourceData() != resourceData) {
                cap.setResourceData(resourceData);
                resourceData.fireChangeListeners(type, object);
            }
        }
        synchronized (this) {
            force = true;
        }
        issueRefresh();
    }

    @Override
    protected void resourceDataChanged(ChangeType type, Object updateObject) {
        if (ChangeType.CAPABILITY == type) {
            for (ResourcePair rp : getResourceList()) {
                rp.getResourceData().fireChangeListeners(type, updateObject);

            }
        }
    }

    @Override
    public String getName() {
        if (groupName == null) {
            if (unitString != null || (unitString == null && initColorMap)) {
                groupName = resourceData.getProductName();
                if (unitString != null) {
                    if (groupName.contains("{U}")) {
                        groupName = groupName.replace("{U}", unitString);
                    } else {
                        groupName = String.format((groupName + " (%s) "),
                                unitString);
                    }
                }
            } else {
                return resourceData.getProductName();
            }
        }
        return groupName;
    }

    @Override
    public String[] getExtraText(DataTime time) {
        if (!getResourceData().getMergeUpperText()) {
            return null;
        }
        ResourceList list = getResourceList();
        List<Set<String>> texts = new ArrayList<>();
        for (ResourcePair rp : list) {
            if (rp.getResource() != null) {
                AbstractVizResource<?, ?> rsc = rp.getResource();
                time = getTimeForResource(rsc);
                if (rsc instanceof BestResResource) {
                    rsc = ((BestResResource) rsc).getBestResResource(time);
                }
                if (rsc instanceof ODIMRadialResource) {
                    if (rp.getProperties().isVisible()) {
                        String[] text = ((ODIMRadialResource) rsc)
                                .getExtraText(time);
                        if (text == null) {
                            continue;
                        }
                        for (int i = 0; i < text.length; i++) {
                            if (texts.size() < i + 1) {
                                texts.add(new HashSet<String>());
                            }
                            texts.get(i).add(text[i]);
                        }
                    }
                }
            }
        }
        String[] textsArr = new String[texts.size()];
        for (int i = 0; i < texts.size(); i++) {
            textsArr[i] = "";
            for (String s2 : texts.get(i)) {
                String s1 = textsArr[i];
                // If they are equal just go with it
                if (s1.equals(s2)) {
                    continue;
                }
                if (s1.isEmpty()) {
                    textsArr[i] = s2;
                    continue;
                }
                if (s2.isEmpty()) {
                    textsArr[i] = s1;
                    continue;
                }
                // Determine any shared characters making a
                // prefix
                StringBuilder prefixBuilder = new StringBuilder();
                while (s2.startsWith(s1.substring(0, 1))
                        && !Character.isDigit(s1.charAt(0))) {
                    prefixBuilder.append(s1.charAt(0));
                    s1 = s1.substring(1);
                    s2 = s2.substring(1);
                }
                String prefix = prefixBuilder.toString();
                // Determine any shared characters making a
                // suffix
                StringBuilder suffix = new StringBuilder();
                while (s2.endsWith(s1.substring(s1.length() - 1))
                        && !Character.isDigit(s1.charAt(s1.length() - 1))) {
                    suffix.insert(0, s1.charAt(s1.length() - 1));
                    s1 = s1.substring(0, s1.length() - 1);
                    s2 = s2.substring(0, s2.length() - 1);

                }
                // If this is a max or mean tag try to parse the
                // max or min
                if (prefix.startsWith("MX") || prefix.startsWith("MN")) {
                    try {
                        double d1 = Double.parseDouble(s1);
                        double d2 = Double.parseDouble(s2);
                        if (d2 > d1 && prefix.startsWith("MX")) {
                            textsArr[i] = prefix + s2 + suffix;
                        } else if (d2 < d1 && prefix.startsWith("MN")) {
                            textsArr[i] = prefix + s2 + suffix;
                        }
                        continue;
                    } catch (Exception e) {
                        // Its probably just a parse exception,
                        // give up and do the default
                    }
                }
                // Try merging with only one copy of Prefix and
                // suffix
                textsArr[i] = prefix + s1 + ", " + s2 + suffix;
            }
        }
        return textsArr;
    }

    @Override
    public void refresh() {
        synchronized (this) {
            force = true;
        }
        issueRefresh();
    }

}

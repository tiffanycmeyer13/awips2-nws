/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.IncommensurableException;
import javax.measure.Quantity;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;

import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.ColorMapLoader;
import com.raytheon.uf.common.colormap.IColorMap;
import com.raytheon.uf.common.colormap.image.ColorMapData;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters.PersistedParameters;
import com.raytheon.uf.common.dataplugin.HDF5Util;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.radar.RadarRecord;
import com.raytheon.uf.common.dataplugin.radar.RadarStoredData;
import com.raytheon.uf.common.datastorage.DataStoreFactory;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.geospatial.CRSCache;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.style.StyleException;
import com.raytheon.uf.common.style.image.ColorMapParameterFactory;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.DrawableImage;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IMesh;
import com.raytheon.uf.viz.core.PixelCoverage;
import com.raytheon.uf.viz.core.cache.CacheObject;
import com.raytheon.uf.viz.core.cache.CacheObject.IObjectRetriever;
import com.raytheon.uf.viz.core.data.IColorMapDataRetrievalCallback;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IImage;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ext.colormap.IColormappedImageExtension;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.BlendableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.BlendedCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ImagingCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.OutlineCapability;
import com.raytheon.uf.viz.core.rsc.extratext.ExtraTextResourceData;
import com.raytheon.uf.viz.core.rsc.extratext.IExtraTextGeneratingResource;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogatable;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogateMap;
import com.raytheon.uf.viz.core.rsc.interrogation.InterrogationKey;
import com.raytheon.uf.viz.core.rsc.interrogation.Interrogator;
import com.raytheon.uf.viz.core.rsc.interrogation.StringInterrogationKey;
import com.raytheon.uf.viz.d2d.core.sampling.ID2DSamplingResource;
import com.raytheon.viz.awipstools.capabilities.RangeRingsOverlayCapability;
import com.raytheon.viz.awipstools.capabilityInterfaces.IRangeableResource;
import com.raytheon.viz.radar.VizRadarRecord;
import com.raytheon.viz.radar.interrogators.IRadarInterrogator;
import com.raytheon.viz.radar.interrogators.RadarDefaultInterrogator;
import com.raytheon.viz.radar.interrogators.RadarRadialInterrogator;
import com.raytheon.viz.radar.rsc.image.IRadialMeshExtension;
import com.raytheon.viz.radar.rsc.image.IRadialMeshExtension.RadialMeshData;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMProductUtil;
import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMStoredData;
import gov.noaa.nws.ocp.common.dataplugin.odim.util.ODIMDataRetriever;
import gov.noaa.nws.ocp.viz.odim.rsc.ODIMResourceData.Mode;
import si.uom.NonSI;
import systems.uom.common.USCustomary;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.quantity.Quantities;

/**
 * Main resource for the display of ODIM data.
 *
 * Based on radar plugin's AbstractRadarResource, RadarImageResource, and
 * RadarRadialResource.
 *
 * The ODIM plugin currently only supports polar scans.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * Jan 27, 2023 DR 23420   dfriedman   Fix SRM sampling
 * Oct 31, 2023 DR 2036439 dfriedman   Support range rings
 * </pre>
 *
 * @author dfriedman
 */
//
public class ODIMRadialResource
        extends AbstractVizResource<ODIMResourceData, MapDescriptor>
        implements Interrogatable, IExtraTextGeneratingResource,
        IResourceDataChanged, IRangeableResource {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ODIMRadialResource.class);

    private static final int RANGE_CIRCLE_PTS = 360;

    protected Map<DataTime, AugmentedRecord> augmentedRecords;

    protected Map<DataTime, String[]> upperTextMap = new HashMap<>();

    // Last data time that was successfully displayed in paintInternal
    protected DataTime displayedDate;

    protected Map<DataTime, DrawableImage> images = new ConcurrentHashMap<>();

    protected IRadarInterrogator interrogator = new RadarRadialInterrogator();

    protected Map<Float, IWireframeShape> rangeCircleMap = new HashMap<>();

    // Copied from AbstractRadarResource to support sampling/interrogation.
    private static final Set<InterrogationKey<?>> defaultInspectLabels = new HashSet<>(
            Arrays.asList(Interrogator.VALUE,
                    RadarDefaultInterrogator.VALUE_STRING,
                    RadarDefaultInterrogator.SHEAR,
                    RadarDefaultInterrogator.MSL, RadarDefaultInterrogator.AGL,
                    IRadarInterrogator.RANGE, IRadarInterrogator.AZIMUTH,
                    IRadarInterrogator.ICAO));

    private static final Set<InterrogationKey<?>> primaryInspectLabels = new HashSet<>(
            Arrays.asList(RadarDefaultInterrogator.MNEMONIC, Interrogator.VALUE,
                    RadarDefaultInterrogator.VALUE_STRING,
                    RadarDefaultInterrogator.SHEAR,
                    RadarDefaultInterrogator.MSL, RadarDefaultInterrogator.AGL,
                    IRadarInterrogator.RANGE, IRadarInterrogator.AZIMUTH,
                    IRadarInterrogator.ICAO));

    private static final Set<InterrogationKey<?>> secondaryInspectLabels = new HashSet<>(
            Arrays.asList(Interrogator.VALUE,
                    RadarDefaultInterrogator.VALUE_STRING,
                    RadarDefaultInterrogator.SHEAR, IRadarInterrogator.ICAO));

    private static final Set<InterrogationKey<?>> offscreenInspectLabels = new HashSet<>(
            Arrays.asList(RadarDefaultInterrogator.MNEMONIC, Interrogator.VALUE,
                    RadarDefaultInterrogator.VALUE_STRING,
                    RadarDefaultInterrogator.PRIMAY_ELEVATION_ANGLE,
                    RadarDefaultInterrogator.SHEAR));

    protected ODIMRadialResource(ODIMResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties, false);
        augmentedRecords = Collections
                .synchronizedMap(new HashMap<DataTime, AugmentedRecord>());
        resourceData.addChangeListener(this);
    }

    public Map<DataTime, AugmentedRecord> getAugmentedRecords() {
        return augmentedRecords;
    }

    public Map<DataTime, ODIMRecord> getODIMRecords() {
        HashMap<DataTime, ODIMRecord> map = new HashMap<>(
                augmentedRecords.size());
        for (Entry<DataTime, AugmentedRecord> entry : augmentedRecords
                .entrySet()) {
            map.put(entry.getKey(), entry.getValue().rec);
        }
        return map;
    }

    @Override
    protected void disposeInternal() {
        for (IWireframeShape shape : rangeCircleMap.values()) {
            if (shape != null) {
                shape.dispose();
            }
        }
        rangeCircleMap.clear();
        for (DrawableImage image : images.values()) {
            disposeImage(image);
        }
        images.clear();
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        displayedDate = null;
        if ((paintProps == null) || (paintProps.getDataTime() == null)) {
            return;
        }

        displayedDate = paintProps.getDataTime();
        ODIMRecord rec = getRecord(displayedDate);

        if (rec == null) {
            issueRefresh();
            return;
        }

        paintRadar(target, paintProps);

        Double eA = rec.getTrueElevationAngle();
        if (resourceData.rangeRings && eA != null) {
            Float elev = eA.floatValue();
            IWireframeShape rangeCircle = this.rangeCircleMap.get(elev);
            // create range circle
            if (rangeCircle == null) {
                AugmentedRecord arec = getAugmentedRecord(displayedDate);
                // Attempt to create envelope, adapted from AbstractTileSet
                double maxExtent = ODIMVizDataUtil.calculateExtent(arec.rec);
                rangeCircle = computeRangeCircle(target, arec.getCRS(),
                        maxExtent);
                if (rangeCircle != null) {
                    this.rangeCircleMap.put(elev, rangeCircle);
                }
            }

            if ((rangeCircle != null)
                    && (getCapability(OutlineCapability.class).isOutlineOn())) {
                target.drawWireframeShape(rangeCircle,
                        getCapability(ColorableCapability.class).getColor(),
                        getCapability(OutlineCapability.class)
                                .getOutlineWidth(),
                        getCapability(OutlineCapability.class).getLineStyle(),
                        paintProps.getAlpha());
            }
        }

        RangeRingsOverlayCapability cap = getCapability(
                RangeRingsOverlayCapability.class);
        cap.setRangeableResource(this);
        cap.paint(target, paintProps);
    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        for (IWireframeShape ring : rangeCircleMap.values()) {
            ring.dispose();
        }
        rangeCircleMap.clear();

        /*
         * TODO (from RadarRadialResource) dispose just the coverage, not the
         * image.
         */
        for (DrawableImage image : images.values()) {
            if (image != null) {
                image.dispose();
            }
        }
        images.clear();
    }

    public void paintRadar(IGraphicsTarget target, PaintProperties paintProps)
            throws VizException {

        DataTime displayedDate = paintProps.getDataTime();

        synchronized (this.images) {
            ODIMRecord rec = getRecord(displayedDate);
            if (rec == null) {
                return;
            }

            DrawableImage image = getImage(target, displayedDate);
            if (image != null) {
                ImagingCapability cap = getCapability(ImagingCapability.class);
                image.getImage().setBrightness(cap.getBrightness());
                image.getImage().setContrast(cap.getContrast());
                image.getImage().setInterpolated(cap.isInterpolationState());
                target.drawRasters(paintProps, image);
            }

            if (image == null || image.getCoverage() == null
                    || image.getCoverage().getMesh() == null) {
                issueRefresh();
            }
        }
    }

    public DrawableImage getImage(IGraphicsTarget target, DataTime dataTime)
            throws VizException {
        DrawableImage image = images.get(dataTime);
        if (image == null || image.getCoverage() == null) {
            AugmentedRecord arec = getAugmentedRecord(dataTime);
            if (arec != null) {
                ODIMRecord record = arec.rec;
                if (record.getNumRadials() == 0 || record.getNumBins() == 0) {
                    return null;
                } else if (arec.getStoredDataAsync() == null) {
                    issueRefresh();
                } else {
                    try {
                        createTile(target, arec);
                        image = images.get(dataTime);
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        if (msg == null) {
                            msg = "Error rendering radar";
                        }
                        throw new VizException(msg, e);
                    }
                }
            }
        }
        return image;
    }

    protected void createTile(IGraphicsTarget target,
            AugmentedRecord populatedRecord) throws StorageException,
            IOException, ClassNotFoundException, VizException {
        ColorMapParameters params = getColorMapParameters(target,
                populatedRecord);
        RadarRecord radarRecord = createRadarRecord(populatedRecord);

        ODIMRecord rec = populatedRecord.rec;
        rec.setStoredData(populatedRecord.getStoredData());
        PixelCoverage coverage = buildCoverage(target, rec);
        if (coverage.getMesh() == null) {
            coverage.setMesh(buildMesh(target, radarRecord));
        }

        IImage image = createImage(target, params, rec, radarRecord,
                new Rectangle(0, 0, rec.getNumBins(), rec.getNumRadials()));
        DrawableImage dImage = images.put(levelAugmentedDataTime(rec),
                new DrawableImage(image, coverage));
        if (dImage != null) {
            disposeImage(dImage);
        }
    }

    public PixelCoverage buildCoverage(IGraphicsTarget target,
            ODIMRecord timeRecord) throws VizException {
        return new PixelCoverage(new Coordinate(0, 0), 0, 0);
    }

    protected static class ODIMImageDataRetrievalAdapter
            implements IColorMapDataRetrievalCallback {

        protected final ODIMRecord rec;

        protected UnitConverter unitConverter;

        protected ColorMapParameters colorMapParameters;

        protected int[] imageFlagValues;

        protected Rectangle rect;

        private final int hashCode;

        public ODIMImageDataRetrievalAdapter(ODIMRecord rec,
                UnitConverter unitConverter,
                ColorMapParameters colorMapParameters, int[] imageFlagValues,
                Rectangle rect) {
            this.rec = rec;
            this.unitConverter = unitConverter;
            this.colorMapParameters = colorMapParameters;
            this.imageFlagValues = imageFlagValues;
            this.rect = rect;
            this.hashCode = Objects.hash(rec, unitConverter, colorMapParameters,
                    Arrays.hashCode(imageFlagValues), rect);
        }

        @Override
        public ColorMapData getColorMapData() {
            Buffer buffer = ODIMVizDataUtil.convertAndUpdateRecordData(rec,
                    unitConverter, colorMapParameters, imageFlagValues);
            return new ColorMapData(buffer,
                    new int[] { rect.width, rect.height });
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ODIMImageDataRetrievalAdapter other = (ODIMImageDataRetrievalAdapter) obj;
            if (!Objects.equals(rec, other.rec)) {
                return false;
            }
            if (!Objects.equals(unitConverter, other.unitConverter)) {
                return false;
            }
            if (!Arrays.equals(imageFlagValues, other.imageFlagValues)) {
                return false;
            }
            return true;
        }

    }

    public IMesh buildMesh(IGraphicsTarget target, RadarRecord radarRecord)
            throws VizException {
        return target.getExtension(IRadialMeshExtension.class).constructMesh(
                new RadialMeshData(radarRecord), descriptor.getGridGeometry());
    }

    protected static RadarRecord createRadarRecordNoStoredData(
            AugmentedRecord arec) {
        ODIMRecord rec = arec.rec;
        RadarRecord radarRecord = new RadarRecord();
        radarRecord.setIcao(rec.getNode());
        radarRecord.setProductCode(
                ODIMProductUtil.getNexradProductCode(arec.rec.getQuantity()));
        radarRecord.setFormat("Radial");// mnemonic?
        radarRecord.setLongitude(rec.getLongitude());
        radarRecord.setLatitude(rec.getLatitude());
        radarRecord.setElevation(rec.getElevation());
        radarRecord.setAngleData(rec.getAngleData());
        radarRecord.setNumLevels(rec.getNumLevels());
        radarRecord.setNumBins(rec.getNumBins());
        radarRecord.setNumRadials(rec.getNumRadials());
        radarRecord.setGateResolution(rec.getGateResolution());
        radarRecord.setPrimaryElevationAngle(rec.getPrimaryElevationAngle());
        radarRecord.setTrueElevationAngle(
                rec.getTrueElevationAngle().floatValue());
        radarRecord.setUnit(rec.getUnit());
        return radarRecord;
    }

    protected static RadarRecord createRadarRecord(AugmentedRecord arec) {
        RadarRecord radarRecord = createRadarRecordNoStoredData(arec);
        ODIMStoredData data = arec.cacheObject.getObjectAsync();
        if (data != null) {
            radarRecord.setRawData(data.getRawData());
            radarRecord.setRawShortData(data.getRawShortData());
            radarRecord.setAngleData(data.getAngleData());
        }
        return radarRecord;
    }

    protected IImage createImage(IGraphicsTarget target,
            ColorMapParameters params, ODIMRecord rec, RadarRecord radaRecord,
            Rectangle rect) throws VizException {
        UnitConverter unitConverter = ODIMVizDataUtil.getConverter(rec,
                radaRecord, params);
        return target.getExtension(IColormappedImageExtension.class)
                .initializeRaster(new ODIMImageDataRetrievalAdapter(rec,
                        unitConverter, params,
                        ODIMVizDataUtil.getImageFlagValues(rec, params), rect),
                        params);
    }

    public void redoImage(DataTime time) {
        disposeImage(images.remove(time));
    }

    protected void disposeImage(DrawableImage image) {
        if (image != null) {
            image.dispose();
        }
    }

    private ColorMapParameters getColorMapParameters(IGraphicsTarget target,
            AugmentedRecord populatedRecord) throws VizException {
        ColorMapParameters paramsCapability = getCapability(
                ColorMapCapability.class).getColorMapParameters();
        String colorMapName = "";
        IColorMap colorMap = null;
        if (paramsCapability != null
                && paramsCapability.getDataUnit() != null) {
            return paramsCapability;
        } else if (paramsCapability != null) {
            colorMapName = paramsCapability.getColorMapName();
            colorMap = paramsCapability.getColorMap();
        }

        ODIMRecord rec = populatedRecord.rec;
        Unit<?> dataUnit = ODIMVizDataUtil.getRecordDataUnit(rec);

        /*
         * Set default creatingEntity parameter to ODIM to help distinguish,
         * e.g., grid ZDR and ODIM ZDR rules.
         */
        Mode mode = getResourceData().getMode();
        String creatingEntity = mode != null && mode != Mode.DEFAULT
                ? mode.toString()
                : "ODIM";

        ColorMapParameters params;
        try {
            params = ColorMapParameterFactory.build((Object) null,
                    populatedRecord.rec.getQuantity(), dataUnit, null,
                    creatingEntity);
        } catch (StyleException e) {
            throw new VizException(e.getLocalizedMessage(), e);
        }

        if (params.getDisplayUnit() == null) {
            params.setDisplayUnit(ODIMVizDataUtil.getUnitObject(rec));
        }
        getCapability(ColorMapCapability.class).setColorMapParameters(params);

        if (colorMap != null) {
            params.setColorMap(colorMap);
            params.setColorMapName(colorMapName);
        }

        if (params.getColorMap() == null) {
            if (("").equals(colorMapName)) {
                colorMapName = params.getColorMapName();
            }
            if (colorMapName == null) {
                colorMapName = "Radar/OSF/16 Level Reflectivity";
            }

            try {
                params.setColorMap(ColorMapLoader.loadColorMap(colorMapName));
            } catch (ColorMapException e) {
                throw new VizException(e);
            }

        }

        PersistedParameters persisted = null;
        if (paramsCapability != null) {
            persisted = paramsCapability.getPersisted();
        }
        if (persisted != null && persisted.getColorMapMax() != null
                && persisted.getColorMapMin() != null) {
            params.applyPersistedParameters(paramsCapability.getPersisted());
        } else {
            params.setColorMapMax(255);
            params.setColorMapMin(0);
        }
        params.setDataMax(255);
        params.setDataMin(0);
        params.setNoDataValue(0.0);
        return params;
    }

    public IWireframeShape computeRangeCircle(IGraphicsTarget target,
            CoordinateReferenceSystem crs, double range) {
        IWireframeShape rangeCircle = target.createWireframeShape(true,
                descriptor);

        try {
            MathTransform mt = CRS.findMathTransform(crs,
                    MapUtil.getLatLonProjection());

            double[][] pts = new double[RANGE_CIRCLE_PTS + 1][2];
            double azDelta = 2 * Math.PI / RANGE_CIRCLE_PTS;
            double az = 0.0;
            double[] input = new double[2];
            double[] output = new double[2];
            for (int i = 0; i < pts.length; i++) {
                input[0] = range * Math.cos(az);
                input[1] = range * Math.sin(az);
                mt.transform(input, 0, output, 0, 1);
                pts[i] = descriptor.worldToPixel(output);
                az += azDelta;
            }
            pts[RANGE_CIRCLE_PTS] = pts[0];

            rangeCircle.allocate(pts.length);
            rangeCircle.addLineSegment(pts);
        } catch (TransformException | FactoryException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to compute the range circle", e);
            return null;
        }

        return rangeCircle;
    }

    public AugmentedRecord getAugmentedRecord(DataTime time) {
        return augmentedRecords.get(time);
    }

    public ODIMRecord getRecord(DataTime time) {
        AugmentedRecord arec = getAugmentedRecord(time);
        return arec != null ? arec.rec : null;
    }

    protected CompatibilityVizRadarRecord getCompatibilityRadarRecord(
            DataTime time) {
        AugmentedRecord arec = getAugmentedRecord(time);
        if (arec != null) {
            prepareCompatibilityRecord(arec, arec.compatibilityRecord);
            return arec.compatibilityRecord;
        } else {
            return null;
        }
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        /*
         * Need to initialize color map parameters now to support mosaics. The
         * radar plugin has a concept of "most severe" record, but there is
         * currently no equivalent for the ODIM plugin.
         */
        if (!augmentedRecords.isEmpty()) {
            AugmentedRecord arec = augmentedRecords.values().iterator().next();
            getColorMapParameters(target, arec);
        }
        ExtraTextResourceData.addExtraTextResource(descriptor);
    }

    @Override
    public void resourceChanged(ChangeType type, Object object) {
        if (type == ChangeType.DATA_UPDATE) {
            PluginDataObject[] pdoArr = (PluginDataObject[]) object;
            for (PluginDataObject rec : pdoArr) {
                addRecord(rec);
            }
        }
        issueRefresh();
    }

    @Override
    public String getName() {
        ODIMRecord record = getRepresentativeRecord(
                getDescriptor().getTimeForResource(this));
        if (record == null) {
            return "ODIM";
        }
        return String
                .format("%s %s%s", record.getNode(),
                        (record.getTrueElevationAngle() != null ? String.format(
                                "%1.1f ", record.getTrueElevationAngle()) : ""),
                        getQuantityDescriptionWithFallback(
                                record.getQuantity()));
    }

    private static String getQuantityDescriptionWithFallback(String quantity) {
        String description = quantity != null
                ? ODIMProductUtil.getQuantityDescription(quantity)
                : null;
        return description != null ? description : quantity;
    }

    protected ODIMRecord getRepresentativeRecord(DataTime timeForResource) {
        return getRecord(timeForResource);
    }

    @Override
    public void remove(DataTime dataTime) {
        synchronized (dataTimes) {
            super.remove(dataTime);
        }
        augmentedRecords.remove(dataTime);
        upperTextMap.remove(dataTime);
    }

    public void addRecord(PluginDataObject pdo) {
        if (pdo instanceof ODIMRecord) {
            ODIMRecord rec = (ODIMRecord) pdo;
            DataTime dataTime = levelAugmentedDataTime(rec);
            augmentedRecords.put(dataTime, new AugmentedRecord(rec));
            synchronized (dataTimes) {
                dataTimes.add(dataTime);
            }
        }
    }

    private static DataTime levelAugmentedDataTime(ODIMRecord rec) {
        DataTime dataTime = rec.getDataTime();
        Double level = rec.getPrimaryElevationAngle();
        if (level != null) {
            dataTime = dataTime.clone();
            dataTime.setLevelValue(level);
        }
        return dataTime;
    }

    /**
     * Associates an ODIM PDO with cached bulk data and other data required for
     * display. Similar to VizRadarRecord in the radar plugin, but uses
     * composition instead of inheritance.
     */
    public static class AugmentedRecord {
        protected ODIMRecord rec;

        protected CacheObject<ODIMRecord, ODIMStoredData> cacheObject;

        // Supports mosaics and range rings
        protected ProjectedCRS crs;

        protected CompatibilityVizRadarRecord compatibilityRecord;

        public AugmentedRecord(ODIMRecord rec) {
            this.rec = rec;
            cacheObject = CacheObject.newCacheObject(rec,
                    new ODIMStoredDataRetriever());
            compatibilityRecord = new CompatibilityVizRadarRecord(this);
        }

        public ODIMRecord getODIMRecord() {
            return rec;
        }

        public ODIMStoredData getStoredData() {
            return cacheObject.getObjectSync();
        }

        public ODIMStoredData getStoredDataAsync() {
            return cacheObject.getObjectAsync();
        }

        public ProjectedCRS getCRS() {
            if (crs == null) {
                crs = CRSCache.getInstance().constructStereographic(
                        MapUtil.AWIPS_EARTH_RADIUS, MapUtil.AWIPS_EARTH_RADIUS,
                        rec.getLatitude(), rec.getLongitude());
            }
            return crs;
        }

    }

    private static class ODIMStoredDataRetriever
            implements IObjectRetriever<ODIMRecord, ODIMStoredData> {

        private Map<ODIMStoredData, Integer> sizes = new WeakHashMap<>();

        @Override
        public ODIMStoredData retrieveObject(ODIMRecord metadata) {
            ODIMStoredData radarData = new ODIMStoredData();
            File loc = HDF5Util.findHDF5Location(metadata);
            IDataStore dataStore = DataStoreFactory.getDataStore(loc);
            int size;
            try {
                size = ODIMDataRetriever.populateODIMStoredData(dataStore,
                        metadata.getDataURI(), radarData);
            } catch (FileNotFoundException | StorageException e) {
                throw new RuntimeException(e);
            }
            sizes.put(radarData, size);
            return radarData;
        }

        @Override
        public int getSize(ODIMStoredData object) {
            Integer size = sizes.get(object);
            if (size == null) {
                size = 0;
                if (object.getRawData() != null) {
                    size += object.getRawData().length;
                }
                if (object.getRawShortData() != null) {
                    size += object.getRawData().length * 2;
                }
                if (object.getAngleData() != null) {
                    size += object.getAngleData().length * 4;
                }
            }
            return size;
        }

        @Override
        public int hashCode() {
            return ODIMStoredDataRetriever.class.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ODIMStoredDataRetriever;
        }

    }

    protected static int getRawIntDataValue(ODIMRecord rec, int radial,
            int bin) {
        int numRadials = rec.getNumRadials();
        int numBins = rec.getNumBins();

        if ((radial < numRadials) && (bin < numBins)) {
            short[] rawShortData = rec.getRawShortData();
            byte[] rawData = rec.getRawData();
            if (rawShortData != null) {
                return rawShortData[(radial * numBins) + bin] & 0xFFFF;
            } else if (rawData != null) {
                return rawData[(radial * numBins) + bin] & 0xFF;
            }
        }
        return 0;
    }

    /* AbstractVizResource interrogation implementation */

    // Copied from AbstractRadarResource.
    @Override
    public Map<String, Object> interrogate(ReferencedCoordinate coord)
            throws VizException {
        try {
            DataTime displayedDate = descriptor.getTimeForResource(this);

            if (displayedDate == null) {
                displayedDate = this.displayedDate;
            }
            InterrogateMap dataMap = interrogate(displayedDate,
                    coord.asLatLon());
            Map<String, Object> map = new HashMap<>();
            for (InterrogationKey<?> key : dataMap.keySet()) {

                if (key == Interrogator.VALUE) {
                    map.put("numericValue", dataMap.get(Interrogator.VALUE)
                            .getValue().toString());
                } else if (key.equals(RadarDefaultInterrogator.CRS_LOCATION)) {
                    double[] point = dataMap
                            .get(RadarDefaultInterrogator.CRS_LOCATION);
                    map.put(RadarDefaultInterrogator.CRS_LOCATION.getId(),
                            point[0] + "," + point[1]);
                } else if (key.equals(IRadarInterrogator.RANGE)) {
                    map.put(IRadarInterrogator.RANGE.getId(),
                            formatQuantity(
                                    dataMap.get(IRadarInterrogator.RANGE),
                                    USCustomary.NAUTICAL_MILE, "%.0fnm"));
                } else if (key instanceof StringInterrogationKey<?>) {
                    StringInterrogationKey<?> sKey = (StringInterrogationKey<?>) key;

                    String stringVal = null;
                    Object obj = dataMap.get(sKey);
                    if (obj instanceof Quantity<?>) {
                        stringVal = ((Quantity<?>) obj).getValue().toString();
                    } else {
                        stringVal = obj.toString();
                    }
                    map.put(sKey.getId(), stringVal);
                }
            }
            return map;
        } catch (TransformException e) {
            throw new VizException(
                    "Transformation error creating lat/lon from referenced coordinate",
                    e);
        } catch (FactoryException e) {
            throw new VizException(
                    "Error creating lat/lon from referenced coordinate", e);
        }
    }

    // Copied from AbstractRadarResource and modified.
    public InterrogateMap interrogate(DataTime dataTime, Coordinate latLon) {
        if (interrogator == null) {
            return new InterrogateMap();
        }
        ColorMapParameters params = null;
        if (hasCapability(ColorMapCapability.class)) {
            params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();
        }

        VizRadarRecord radarRecord = getCompatibilityRadarRecord(dataTime);
        if (radarRecord != null && radarRecord.getStoredDataAsync() != null) {
            return interrogator.sample(radarRecord, latLon, params,
                    interrogator.getInterrogationKeys());
        }
        return new InterrogateMap();
    }

    // Copied from AbstractRadarResource.
    protected boolean containsNonNullKey(InterrogationKey<?> key,
            Set<InterrogationKey<?>> keys, InterrogateMap dataMap,
            Set<InterrogationKey<?>> keysToSkip) {
        return (keysToSkip == null || !keysToSkip.contains(key))
                && keys.contains(key) && dataMap.containsKey(key)
                && dataMap.get(key) != null;
    }

    // Copied from AbstractRadarResource and modified.
    /**
     * Converts the measure to the desired output unit (if provided) and then
     * returns a formatted string using the converted measure value and unit
     * string, provided in that respective order.
     *
     * If outputUnit is null or AbstractUnit.ONE, the output unit will be the
     * measure's current unit, if non-null.
     *
     * @param measure
     *            The measure. Must be non-null and have a non-null value, or
     *            null will be returned. If the unit is null, no conversion will
     *            happen and "" will be used as the unit String.
     * @param outputUnit
     *            The desired output unit. If null or AbstractUnit.ONE, measure
     *            will not be converted, and the current unit will be used for
     *            the unit string.
     * @param format
     *            The format String. Arguments to this will be the double value
     *            of the measure and the unit String, in that order. This must
     *            be non null.
     * @return The formatted output.
     * @throws VizException
     *             if units are unconvertible.
     */
    protected String formatQuantity(Quantity<?> quantity, Unit<?> outputUnit,
            String format) throws VizException {
        if (quantity == null || quantity.getValue() == null || format == null) {
            return null;
        }

        Unit<?> currentUnit = quantity.getUnit();
        String unitString;
        if (currentUnit == null) {
            unitString = "";
        } else {
            unitString = SimpleUnitFormat
                    .getInstance(SimpleUnitFormat.Flavor.ASCII)
                    .format(quantity.getUnit());
        }

        double value = quantity.getValue().doubleValue();
        if (quantity.getUnit() != outputUnit && quantity.getUnit() != null
                && outputUnit != null && !outputUnit.equals(AbstractUnit.ONE)) {
            UnitConverter toOutputUnit;
            try {
                toOutputUnit = quantity.getUnit().getConverterToAny(outputUnit);
            } catch (IncommensurableException | UnconvertibleException e) {
                throw new VizException(e);
            }
            value = toOutputUnit.convert(value);
            unitString = SimpleUnitFormat
                    .getInstance(SimpleUnitFormat.Flavor.ASCII)
                    .format(outputUnit);

        }
        return String.format(format, value, unitString);
    }

    // Copied from AbstractRadarResource and modified.
    @Override
    public String inspect(ReferencedCoordinate latLon) throws VizException {
        InterrogateMap dataMap;
        // Grab current time
        DataTime displayedDate = descriptor.getTimeForResource(this);

        if (displayedDate == null) {
            FramesInfo fi = descriptor.getFramesInfo();
            DataTime[] times = fi.getTimeMap().get(this);
            int index = fi.getFrameIndex();
            if (times != null && index > 0 && index < times.length) {
                displayedDate = times[index];
            }
        }
        if (displayedDate == null) {
            displayedDate = this.displayedDate;
        }

        try {
            dataMap = interrogate(latLon, displayedDate,
                    getInterrogationKeys().toArray(new InterrogationKey<?>[0]));
        } catch (Exception e) {
            throw new VizException("Error converting coordinate for hover", e);
        }
        // determine if we are blended, if so are we the primary.
        boolean primary = true;
        if (this.hasCapability(BlendedCapability.class)) {
            int myIndex = this.getCapability(BlendedCapability.class)
                    .getResourceIndex();
            int hiddenIndex = this.getCapability(BlendedCapability.class)
                    .getBlendableResource().getResource()
                    .getCapability(BlendableCapability.class)
                    .getResourceIndex();
            primary = myIndex != hiddenIndex;

        }
        // determine if all pane sampling is enabled
        List<ID2DSamplingResource> samplingResources = descriptor
                .getResourceList()
                .getResourcesByTypeAsType(ID2DSamplingResource.class);
        boolean allPaneSample = false;
        if (!samplingResources.isEmpty()) {
            allPaneSample = samplingResources.get(0).isAllPanelSampling();
        }
        if (allPaneSample) {
            // When all pane sampling is on paint lots of info for the primary
            // on the visible pane, other panes return minimal info
            boolean visible = descriptor.getRenderableDisplay().getContainer()
                    .getActiveDisplayPane().getDescriptor() == descriptor;
            if (visible && primary) {
                return "="
                        + inspect(displayedDate, primaryInspectLabels, dataMap);
            } else {
                return " " + inspect(displayedDate, offscreenInspectLabels,
                        dataMap);
            }
        } else if (primary) {
            return inspect(displayedDate, dataMap);
        } else {
            // The secondary returns slightly less data
            return inspect(displayedDate, secondaryInspectLabels, dataMap);
        }
    }

    // Copied from AbstractRadarResource and modified to throw VizException.
    public String inspect(DataTime dataTime, InterrogateMap dataMap)
            throws VizException {
        return inspect(dataTime, defaultInspectLabels, dataMap);
    }

    // Copied from AbstractRadarResource and modified to throw VizException.
    /**
     * Given the map of data values, return the inspection string
     *
     * @param dataMap
     * @return
     */
    public String inspect(DataTime dataTime, Set<InterrogationKey<?>> keys,
            InterrogateMap dataMap) throws VizException {
        if (dataMap == null) {
            return "NO DATA";
        }

        StringBuilder displayedData = new StringBuilder();

        boolean containsValueString = containsNonNullKey(
                IRadarInterrogator.VALUE_STRING, keys, dataMap, null);

        Set<InterrogationKey<?>> keysToSkip;
        if (containsValueString) {
            keysToSkip = this.interrogator.getValueStringKeys();
        } else {
            keysToSkip = Collections.emptySet();
        }

        if (containsNonNullKey(IRadarInterrogator.MNEMONIC, keys, dataMap,
                keysToSkip)) {
            displayedData.append(dataMap.get(IRadarInterrogator.MNEMONIC))
                    .append(" ");
        }

        /*
         * Append either the value string or a formatted Value, but not both, as
         * the value string is either already formatted or more meaningful if
         * non-null.
         */
        if (containsValueString) {
            displayedData.append(dataMap.get(IRadarInterrogator.VALUE_STRING));
        } else if (containsNonNullKey(Interrogator.VALUE, keys, dataMap,
                keysToSkip)) {
            Quantity<?> value = dataMap.get(Interrogator.VALUE);
            String format;
            if (value.getValue() instanceof Double
                    || value.getValue() instanceof Float) {
                format = "%.2f%s";
            } else {
                format = "%.0f%s";
            }
            displayedData.append(formatQuantity(value, null, format));
        }

        if (containsNonNullKey(IRadarInterrogator.PRIMAY_ELEVATION_ANGLE, keys,
                dataMap, keysToSkip)) {
            while (displayedData.length() < 15) {
                displayedData.append(" ");
            }
            displayedData.append(
                    dataMap.get(IRadarInterrogator.PRIMAY_ELEVATION_ANGLE));
        }

        if (containsNonNullKey(IRadarInterrogator.SHEAR, keys, dataMap,
                keysToSkip)) {
            displayedData.append(String.format(" %.4f/s",
                    dataMap.get(IRadarInterrogator.SHEAR)));
        }

        if (containsNonNullKey(IRadarInterrogator.MSL, keys, dataMap,
                keysToSkip)) {
            Quantity<Length> msl = dataMap.get(IRadarInterrogator.MSL);
            displayedData.append(
                    formatQuantity(msl, USCustomary.FOOT, " %.0f%sMSL "));

            Quantity<Length> agl = dataMap.get(IRadarInterrogator.AGL);
            if (agl == null || Double.isNaN(agl.getValue().doubleValue())) {
                displayedData.append("???ft");
            } else {
                displayedData.append(
                        formatQuantity(agl, USCustomary.FOOT, "%.0f%s"));
            }
            displayedData.append("AGL");
        }

        if (containsNonNullKey(IRadarInterrogator.AZIMUTH, keys, dataMap,
                keysToSkip)) {
            Quantity<Length> range = dataMap.get(IRadarInterrogator.RANGE);
            displayedData
                    .append(formatQuantity(range, USCustomary.NAUTICAL_MILE,
                            " %.0fnm"))
                    .append("@").append(dataMap.get(IRadarInterrogator.AZIMUTH)
                            .to(NonSI.DEGREE_ANGLE).getValue().intValue());
        }

        if (containsNonNullKey(IRadarInterrogator.ICAO, keys, dataMap,
                keysToSkip)) {
            displayedData.append(' ')
                    .append(dataMap.get(IRadarInterrogator.ICAO));
        }

        if (displayedData.toString().contains("null")
                || displayedData.toString().isEmpty()) {
            displayedData.replace(0, displayedData.length(), "NO DATA");
        }

        return displayedData.toString();
    }

    /* Interrogatable implementation: */

    // Copied from AbstractRadarResource.
    @Override
    public Set<InterrogationKey<?>> getInterrogationKeys() {
        if (this.interrogator != null) {
            return this.interrogator.getInterrogationKeys();
        }

        return Collections.emptySet();
    }

    // Copied from AbstractRadarResource and modified.
    @Override
    public InterrogateMap interrogate(ReferencedCoordinate coordinate,
            DataTime time, InterrogationKey<?>... keys) {
        if (keys == null || keys.length == 0 || this.interrogator == null) {
            return new InterrogateMap();
        }

        Set<InterrogationKey<?>> keySet = new HashSet<>(Arrays.asList(keys));

        RadarRecord rec = getCompatibilityRadarRecord(time);
        if (rec == null) {
            return new InterrogateMap();
        }

        ColorMapParameters params;
        if (hasCapability(ColorMapCapability.class)) {
            params = getCapability(ColorMapCapability.class)
                    .getColorMapParameters();
        } else {
            params = null;
        }

        try {
            return this.interrogator.sample(rec, coordinate.asLatLon(), params,
                    keySet);
        } catch (TransformException | FactoryException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to convert coordinate to lat/lon.", e);
            return new InterrogateMap();
        }
    }

    /**
     * Given the dataTime, returns the upper text info for that time
     *
     * @param time
     * @return
     */
    @Override
    public String[] getExtraText(DataTime time) {
        AugmentedRecord arec = getAugmentedRecord(time);
        if (arec == null) {
            return null;
        }
        String[] result = upperTextMap.get(time);
        if (result == null && !upperTextMap.containsKey(time)) {
            if (arec.getStoredDataAsync() == null) {
                return null;
            }
            result = getTextContributions(arec);
            upperTextMap.put(time, result);
        }
        return result;
    }

    protected String[] getTextContributions(AugmentedRecord arec) {
        return new String[0];
    }

    /*
     * Bridges this plugin's record type the radar plugin so it can be used by
     * its sampling/interrogation code.
     */
    protected static class CompatibilityVizRadarRecord extends VizRadarRecord {

        protected transient AugmentedRecord rec;

        protected transient RadarStoredData compatibilityStoredData;

        public CompatibilityVizRadarRecord(AugmentedRecord rec) {
            super(createRadarRecordNoStoredData(rec));
            this.rec = rec;
        }

        @Override
        public RadarStoredData getStoredData() {
            if (compatibilityStoredData == null) {
                compatibilityStoredData = convertToRadarStoredData(
                        rec.getStoredData());
            }
            return compatibilityStoredData;
        }

        @Override
        public RadarStoredData getStoredDataAsync() {
            if (compatibilityStoredData == null) {
                ODIMStoredData odimStoredData = rec.getStoredDataAsync();
                if (odimStoredData != null) {
                    compatibilityStoredData = convertToRadarStoredData(
                            odimStoredData);
                }
            }
            return compatibilityStoredData;
        }

        @Override
        public Unit<?> getDataUnit() {
            return ODIMVizDataUtil.getRecordDataUnit(rec.rec);
        }

    }

    protected static RadarStoredData convertToRadarStoredData(
            ODIMStoredData storedData) {
        RadarStoredData result = new RadarStoredData();
        result.setAngleData(storedData.getAngleData());
        result.setRawData(storedData.getRawData());
        result.setRawShortData(storedData.getRawShortData());
        return result;
    }

    protected void prepareCompatibilityRecord(AugmentedRecord arec,
            CompatibilityVizRadarRecord compatibilityRecord) {
        // Base class implementation does nothing.
    }

    @Override
    public Quantity<Length> getElevation() {
        ODIMRecord rec = getRecord(displayedDate);
        if (rec != null) {
            return Quantities.getQuantity(rec.getElevation(),
                    USCustomary.METER);
        }
        return Quantities.getQuantity(0.0, USCustomary.METER);
    }

    @Override
    public Coordinate getCenter() {
        ODIMRecord rec = getRecord(displayedDate);
        if (rec != null) {
            return new Coordinate(rec.getLongitude(), rec.getLatitude());
        }
        return new Coordinate();
    }

    @Override
    public double getTilt() {
        ODIMRecord rec = getRecord(displayedDate);
        if (rec != null) {
            return rec.getTrueElevationAngle();
        }
        return 0;
    }

}

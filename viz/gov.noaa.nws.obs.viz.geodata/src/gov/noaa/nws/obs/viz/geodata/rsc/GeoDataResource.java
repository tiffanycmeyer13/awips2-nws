package gov.noaa.nws.obs.viz.geodata.rsc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.graphics.RGB;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.TransformException;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.colormap.ColorMapException;
import com.raytheon.uf.common.colormap.ColorMapLoader;
import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.geospatial.ReferencedCoordinate;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.style.StyleException;
import com.raytheon.uf.common.style.StyleManager;
import com.raytheon.uf.common.style.StyleRule;
import com.raytheon.uf.common.style.image.ColorMapParameterFactory;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.PointStyle;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.IRenderable;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged.ChangeType;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorMapCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.OutlineCapability;

import gov.nasa.msfc.sport.viz.geodata.drawable.GeoDrawable;
import gov.nasa.msfc.sport.viz.geodata.drawable.PointDrawable;
import gov.noaa.nws.obs.common.dataplugin.geodata.FloatAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.GeoDataRecord;
import gov.noaa.nws.obs.common.dataplugin.geodata.IntegerAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.StringAttribute;
import gov.noaa.nws.obs.viz.geodata.style.GenericGeometryStyleAttribute;
import gov.noaa.nws.obs.viz.geodata.style.GeoDataRecordCriteria;
import gov.noaa.nws.obs.viz.geodata.style.GeometryPreferences;

/**
 * GeoDataResource
 *
 * Class which provides the functionality for painting/rendering a GeoDataRecord
 * to the CAVE display, along with various styling rules, etc.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07/25/2016   19064      jburks      Initial checkin (DCS 19064)
 * 08/04/2016   19064      mcomerford  Adding styleRules handling.
 * Dec 01, 2017 5863       mapeters    Change dataTimes to a NavigableSet
 * Jul 28, 2019 7892       mroos       Replace loops with iterators in remove(DataTime)
 *
 * </pre>
 *
 * @author jason.burks
 */

public class GeoDataResource
        extends AbstractVizResource<GeoDataResourceData, MapDescriptor> {

    /* Logging errors that arise to the console output. */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GeoDataResource.class);

    /*
     * Absolute maximum and minimum point sizes, as defined by the
     * GeoDataRecord's "Attribute of Interest". Point sizes and magnification
     * only apply to PointDrawables.
     */
    private float minPointSize;

    private float maxPointSize;

    /*
     * Maximum and minimum values of Point magnification (to allow for better
     * scaling)
     */
    private float minMagSize;

    private float maxMagSize;

    /*
     * Style to render all PointDrawables handled by this resource (if declared
     * in StyleRules).
     */
    private PointStyle pointStyle;

    /*
     * HashMap that holds the appropriate DataFrame instance (and
     * GeoDataRecords) for a given TimeRange.
     */
    private Map<TimeRange, DataFrame> frames = new HashMap<>();

    /* The GeoDataRecords contained in this Resource. */
    private List<GeoDataRecord> records = new ArrayList<>();

    /*
     * Default color to set for rendered Geometries, if a matching StyleRule
     * cannot be determined.
     */
    private static final Color WHITE = new Color(255, 255, 255);

    /*
     * The default StyleRule as determined during the GeoDataResourceData's
     * constructResource method.
     */
    private StyleRule defStyleRule;

    /*
     * The Default GeometryPreferences relating to this resource. This
     * Preference is populated when the Resource's constructor is called. It
     * contains default "StyleRule" values relating to the rendered Geometries
     * (lineWidth, Color, etc.), and is used in cases where a matching StyleRule
     * cannot be found for a given GeoDataRecord.
     */
    private static final GeometryPreferences defaultPrefs = new GeometryPreferences(
            Float.valueOf(1.f), WHITE, Double.valueOf(5.0));

    /**
     * The default constructor. Generates the GeoDataResource from the
     * associated resourceData and loadProperties. getDefaultPrefs
     *
     * @param resourceData
     *            The GeoDataResourceData instance used to generated the
     *            resource.
     * @param loadProperties
     *            The LoadProperties applied to the generated resource.
     */
    protected GeoDataResource(GeoDataResourceData resourceData,
            LoadProperties loadProperties) {
        super(resourceData, loadProperties, false);
    }

    @Override
    protected void disposeInternal() {
        if (frames != null) {
            for (DataFrame frame : frames.values()) {
                frame.dispose();
            }

            frames.clear();
            frames = null;
        }
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {

        DataTime currentTime = paintProps.getDataTime();

        if (currentTime != null) {
            DataFrame frame = getDataFrameForTime(currentTime);
            if (frame != null) {
                for (IRenderable renderable : frame.getDrawables()) {
                    renderable.paint(target, paintProps);
                }
            }
        }
    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {

        ColorMapParameters colormapParams = null;

        /*
         * Handling StyleRules to determine if the Resource should work with a
         * Colormap.
         */
        if (defStyleRule != null) {
            if (defStyleRule.getPreferences() != null) {
                GeometryPreferences prefs = (GeometryPreferences) defStyleRule
                        .getPreferences();
                maxMagSize = prefs.getMaxMagnification();
                minMagSize = prefs.getMinMagnification();
                pointStyle = prefs.getPointDisplay();
                if (prefs.getDefaultColormap() != null) {
                    try {
                        colormapParams = new ColorMapParameters();
                        colormapParams.setColorMap(ColorMapLoader
                                .loadColorMap(prefs.getDefaultColormap()));
                        colormapParams.setColorMapUnit(prefs.getDisplayUnits());
                        colormapParams.setColorMapMin(prefs.getDataScale()
                                .getMinValue().floatValue());
                        colormapParams.setColorMapMax(prefs.getDataScale()
                                .getMaxValue().floatValue());
                        colormapParams.setColorBarIntervals(
                                prefs.getColorbarLabeling().getValues());
                        getCapability(ColorMapCapability.class)
                                .setColorMapParameters(colormapParams);
                    } catch (ColorMapException e) {
                        throw new VizException(e);
                    } catch (NullPointerException e) {
                        throw new VizException(
                                "Error setting Colormap Parameters.", e);
                    }
                }
            } else {
                maxMagSize = defaultPrefs.getMaxMagnification();
                minMagSize = defaultPrefs.getMinMagnification();
                pointStyle = defaultPrefs.getPointDisplay();
                defStyleRule.setPreferences(defaultPrefs);
            }
        }

        /* Add the GeoDataRecord(s) to their appropriate DataFrame. */
        for (GeoDataRecord record : records) {

            DataFrame frame = getDataFrameForTime(record.getDataTime());

            if (frame == null) {
                DataTime time = record.getDataTime();
                TimeRange tr = resourceData.getBinOffset()
                        .getNormalizedTime(time).getValidPeriod();
                DataFrame newFrame = new DataFrame();
                newFrame.addRecord(record);
                frames.put(tr, newFrame);
            } else {
                frame.addRecord(record);
            }
        }
    }

    /**
     * Add a GeoDataRecord to the Resource.
     *
     * @param rec
     *            The record to be added.
     */
    public void addRecord(GeoDataRecord rec) {
        records.add(rec);
    }

    /**
     * resourceDataChanged Retrieve the DataFrame instance given the
     * corresponding DataTime
     *
     * @param time
     *            The DataTime instance used to fetch the appropriate
     *            DataFrame(s)
     * @return the DataFrame associated with the DataTime
     */
    public DataFrame getDataFrameForTime(DataTime time) {
        if (frames == null) {
            return null;
        }
        return frames.get(resourceData.getBinOffset().getNormalizedTime(time)
                .getValidPeriod());
    }

    @Override
    public String getName() {
        if (defStyleRule != null) {
            GeometryPreferences prefs = (GeometryPreferences) defStyleRule
                    .getPreferences();
            if (prefs.getLegend() != null) {
                return prefs.getLegend() + " (" + prefs.getDisplayUnitLabel()
                        + ")";
            }
        }

        if (records.isEmpty()) {
            return "GeoData";
        }

        return records.get(0).getSource() + " " + records.get(0).getProduct();
    }

    /**
     * Convert the scaled Color instance into an RGB value. Need to multiply the
     * retrieved color (from Color.getRed/Green/Blue) by 255 to map the RGB to
     * the colormap.resourceDataChanged
     *
     * @param color
     *            The Color instance to be converted to RGB.
     * @return The RGB generated from the Color.
     */
    private RGB convertScaledColorToRGB(Color color) {
        return new RGB((int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
    }

    /**
     * Convert the Color instance to an RGB value (straight across).
     *
     * @param color
     *            The Color to convert to an RGB
     * @return The RGB converted from the Color.
     */
    private RGB convertColorToRGB(Color color) {
        return new RGB((int) color.getRed(), (int) color.getGreen(),
                (int) color.getBlue());
    }

    @Override
    public String inspect(ReferencedCoordinate coord) throws VizException {

        FramesInfo info = descriptor.getFramesInfo();
        DataTime currentTime = info.getTimeForResource(this);
        DataFrame frame = getDataFrameForTime(currentTime);

        if (frame == null) {
            return "";
        }

        GeoDataRecord closestRecord = getClosestRecord(coord,
                frame.getRecords());

        String sampleString = "";

        if (closestRecord != null) {

            /*
             * Now that we have the closest GeoDataRecord, determine the
             * matching StyleRule, which contains the unit conversion for any
             * applicable Float/Integer/StringAttribute(s).
             */
            StyleRule closestRecordRule = null;
            GeometryPreferences closestRecordPrefs = defaultPrefs;
            try {
                GeoDataRecordCriteria closestRecordCriteria = new GeoDataRecordCriteria(
                        closestRecord);
                closestRecordRule = StyleManager.getInstance().getStyleRule(
                        StyleManager.StyleType.GEOMETRY, closestRecordCriteria);
            } catch (StyleException e) {
                throw new VizException(e);
            }

            if (closestRecordRule != null) {
                if (closestRecordRule
                        .getPreferences() instanceof GeometryPreferences) {
                    closestRecordPrefs = (GeometryPreferences) closestRecordRule
                            .getPreferences();
                }
            }

            /*
             * Generate the sampleString from the closestRecord and its matching
             * GeometryPreferences.
             */
            sampleString = sample(closestRecord, closestRecordPrefs);
        }

        return sampleString;
    }

    /**
     * Generate the CAVE Sampling String to display/format the Attribute(s)
     * stored in the GeoDataRecord.
     *
     * @param closestRecord
     *            The GeoDataRecord that is closest to the mouse pointer.
     * @param closestRecordPrefs
     *            The GeometryPreferences that correspond to the GeoDataRecord
     * @return The sample string, which has Attribute values/names converted
     *         based on the matching GeometryPreferences.
     */
    public String sample(GeoDataRecord closestRecord,
            GeometryPreferences closestRecordPrefs) {

        StringBuilder sb = new StringBuilder();

        sb.append("Time: " + closestRecord.getDataTime() + "\n");
        for (IntegerAttribute intAtt : closestRecord.getIntegerAtt()) {
            double sampleVal = intAtt.getValue();
            String name = intAtt.getName();
            String units = "";
            genSampleString(sampleVal, name, units, sb, closestRecordPrefs);
        }
        for (FloatAttribute floatAtt : closestRecord.getFloatAtt()) {
            double sampleVal = floatAtt.getValue();
            String name = floatAtt.getName();
            String units = "";
            genSampleString(sampleVal, name, units, sb, closestRecordPrefs);
        }
        for (StringAttribute stringAtt : closestRecord.getStringAtt()) {
            double sampleVal = Double.valueOf(stringAtt.getName());
            String name = stringAtt.getName();
            String units = "";
            genSampleString(sampleVal, name, units, sb, closestRecordPrefs);
        }

        return sb.toString();
    }

    /**
     * Generate the StyleRules-corrected sampling string given the values and
     * GeometryPreferences.
     *
     * @param sampleVal
     *            The unit-corrected value of the Attribute.
     * @param name
     *            The name-corrected display name of the Attribute.
     * @param units
     *            The units of sampleVal.
     * @param sb
     *            The StringBuilder to add sampling information to.
     * @param closestRecordPrefs
     *            GeometryPreferences containing the sampling information.
     */
    public void genSampleString(double sampleVal, String name, String units,
            StringBuilder sb, GeometryPreferences closestRecordPrefs) {

        for (GenericGeometryStyleAttribute genAtt : closestRecordPrefs
                .getAttConversions()) {
            if (genAtt.getDataName().equals(name)) {
                sampleVal = genAtt.convertValue(sampleVal);
                units = genAtt.getDisplayUnits();
                name = genAtt.getDisplayName();
            }
        }

        if (closestRecordPrefs.getSamplePrefs() == null) {
            sb.append(name).append(": ")
                    .append(String.format("%.3f", sampleVal)).append(" ")
                    .append(units).append("\n");
        } else {
            if (closestRecordPrefs.getSamplePrefs().getFormatString() == null) {
                sb.append(name).append(": ")
                        .append(String.format("%.3f", sampleVal)).append(" ")
                        .append(units).append("\n");
            } else {
                sb.append(name).append(": ")
                        .append(String.format(closestRecordPrefs
                                .getSamplePrefs().getFormatString(), sampleVal))
                        .append(" ").append(units).append("\n");
            }
        }
    }

    /**
     * Use the ReferencedCoordinate (of the mouse pointer) to parse through the
     * Set of GeoDataRecords and determine which record provides the Geometry
     * closest to the ReferencedCoordinate.
     *
     * @param coord
     *            The coordinate of the mouse pointer.
     * @param records
     *            The set of GeoDataRecords from which the record closest to the
     *            mouse pointer coordinate will be returned.
     * @return The GeoDataRecord instance that is closest to mouse pointer
     *         coordinate
     * @throws VizException
     *             If there is an error calculating the closest GeoDataRecord.
     */
    public GeoDataRecord getClosestRecord(ReferencedCoordinate coord,
            Set<GeoDataRecord> records) throws VizException {

        try {
            Coordinate pointer = coord.asGridCell(descriptor.getGridGeometry(),
                    PixelInCell.CELL_CENTER);

            double sampleCutoff = 5.0;
            GeoDataRecord closestRecord = null;
            StyleRule sr = null;

            for (GeoDataRecord record : records) {

                Geometry geometry = record.getGeometry();

                /*
                 * If the record Geometry is Polygon, determine if the pointer
                 * is within that Geometry. If so, return that record
                 * immediately.
                 */
                if (geometry instanceof Polygon) {
                    Point point = new GeometryFactory()
                            .createPoint(coord.asLatLon());
                    if (geometry.contains(point)) {
                        return record;
                    }
                }

                /* Get the best matching style rule for this record. */
                GeometryPreferences prefs = defaultPrefs;
                try {
                    GeoDataRecordCriteria recordCriteria = new GeoDataRecordCriteria(
                            record);
                    sr = StyleManager.getInstance().getStyleRule(
                            StyleManager.StyleType.GEOMETRY, recordCriteria);
                } catch (StyleException e) {
                    throw new VizException(e);
                }

                if (sr != null) {
                    if (sr.getPreferences() instanceof GeometryPreferences) {
                        prefs = (GeometryPreferences) sr.getPreferences();
                    }
                }

                /*
                 * If we are working with a GeometryPreferences, then the
                 * sampleCutoff value (or Attribute that holds the value) may be
                 * defined. Need to determine the value if that is the case.
                 */
                if (prefs.getSampleCutoff() instanceof Double) {
                    sampleCutoff = ((Double) prefs.getSampleCutoff())
                            .doubleValue();
                } else if (prefs.getSampleCutoff() instanceof FloatAttribute) {
                    FloatAttribute sampleAtt = (FloatAttribute) prefs
                            .getSampleCutoff();

                    for (FloatAttribute att : record.getFloatAtt()) {
                        if (att.getName().equals(sampleAtt.getName())) {
                            sampleCutoff = att.getValue();
                            break;
                        }
                    }
                } else if (prefs
                        .getSampleCutoff() instanceof IntegerAttribute) {
                    IntegerAttribute sampleAtt = (IntegerAttribute) prefs
                            .getSampleCutoff();

                    for (IntegerAttribute att : record.getIntegerAtt()) {
                        if (att.getName().equals(sampleAtt.getName())) {
                            sampleCutoff = att.getValue();
                            break;
                        }
                    }
                }

                Coordinate geomCenter = geometry.getCentroid().getCoordinate();

                double[] pixel = descriptor.worldToPixel(
                        new double[] { geomCenter.x, geomCenter.y });

                Coordinate recordLoc = new Coordinate(pixel[0], pixel[1]);
                double recordDist = recordLoc.distance(pointer);
                if (recordDist < sampleCutoff) {
                    sampleCutoff = recordDist;
                    closestRecord = record;
                }
            }

            return closestRecord;
        } catch (FactoryException | TransformException e) {
            throw new VizException("Error retrieving the closes GeoDataRecord.",
                    e);
        }

    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        /* Dispose of the shapes used to draw Points/Polygons */
        if (frames != null) {
            for (DataFrame frame : frames.values()) {
                frame.dispose();
            }
        }
    }

    @Override
    protected void resourceDataChanged(ChangeType type, Object updateObject) {
        if (type == ChangeType.DATA_UPDATE) {
            if (updateObject instanceof GeoDataRecord[]) {
                GeoDataRecord[] records = (GeoDataRecord[]) updateObject;
                for (GeoDataRecord record : records) {
                    DataFrame frame = getDataFrameForTime(record.getDataTime());
                    try {
                        if (frame == null) {
                            frame = new DataFrame();
                            TimeRange tr = resourceData.getBinOffset()
                                    .getTimeRange(record.getDataTime());
                            DataTime newDTime = new DataTime(tr.getStart());
                            this.dataTimes.add(newDTime);
                            frames.put(tr, frame);
                        }
                        frame.addRecord(record);
                    } catch (VizException e) {
                        /* Shouldn't fail out on a single record. */
                        statusHandler.error(
                                "An error occurred while handling incoming GeoDataRecords");
                    }
                }
            } else if (updateObject instanceof GeoDataRecord) {
                GeoDataRecord record = (GeoDataRecord) updateObject;
                DataFrame frame = getDataFrameForTime(record.getDataTime());
                try {
                    if (frame == null) {
                        frame = new DataFrame();
                        TimeRange tr = resourceData.getBinOffset()
                                .getTimeRange(record.getDataTime());
                        this.dataTimes.add(new DataTime(tr.getStart()));
                        frames.put(tr, frame);
                    }
                    frame.addRecord(record);
                } catch (VizException e) {
                    /* Shouldn't fail on a single record. */
                    statusHandler.error(
                            "An error occurred while handling an incoming GeoDataRecord");
                }
            }
            issueRefresh();
        } else if (type == ChangeType.DATA_REMOVE) {
            remove((DataTime) updateObject);
        } else if (type == ChangeType.CAPABILITY) {
            if (updateObject instanceof ColorMapCapability) {
            }
        }
        issueRefresh();

    }

    @Override
    public void remove(DataTime dataTime) {
        super.remove(dataTime);
        DataFrame frame = null;
        // Iterators are used in order to properly remove entries w/o error
        Iterator<Map.Entry<TimeRange, DataFrame>> entries = frames.entrySet()
                .iterator();
        while (entries.hasNext()) {
            Map.Entry<TimeRange, DataFrame> entry = entries.next();
            if (entry.getKey().contains(dataTime.getValidTimeAsDate())) {
                frame = entry.getValue();
                entries.remove();
                break;
            }
        }
        if (frame != null) {
            Iterator<GeoDataRecord> rem = frame.getRecords().iterator();
            while (rem.hasNext()) {
                if (rem.next().getDataTime().equals(dataTime)) {
                    rem.remove();
                }
            }
        }
    }

    public StyleRule getDefStyleRule() {
        return defStyleRule;
    }

    public void setDefStyleRule(StyleRule defStyleRule) {
        this.defStyleRule = defStyleRule;
    }

    /**
     * DataFrame
     *
     * Class that provides a mapping between a GeoDataRecord(s) and their
     * respective IRenderables. The IRenderables are instances of
     * Point/Line/Polygon Renderables, which are separate classes that handle
     * the rendering of Java Geometry types (Point, Line, and Polygon types).
     *
     */
    private class DataFrame {

        /*
         * HashMap that maps each GeoDataRecord to its matching Drawable
         * (PointDrawable, GeoDrawable), which is an IRenderable.
         */
        Map<GeoDataRecord, IRenderable> map = new HashMap<>();

        /**
         * Add a GeoDataRecord:IRenderable pair to the DataFrame mapping
         *
         * @param records
         *            Any number of GeoDataRecords
         * @throws VizException
         */
        public void addRecord(GeoDataRecord... records) throws VizException {

            List<PointDrawable> points = new ArrayList<>();

            for (GeoDataRecord record : records) {

                /* Get the matching style rule for the given record. */
                GeometryPreferences prefs = defaultPrefs;
                StyleRule styleRule = null;
                try {
                    GeoDataRecordCriteria recordCriteria = new GeoDataRecordCriteria(
                            record);
                    styleRule = StyleManager.getInstance().getStyleRule(
                            StyleManager.StyleType.GEOMETRY, recordCriteria);
                } catch (StyleException e) {
                    throw new VizException(e);
                }

                /*
                 * Determine if there are any ColorMapParameters for coloring
                 * this record.
                 */
                ColorMapParameters params = getCapability(
                        ColorMapCapability.class).getColorMapParameters();
                if (params == null) {
                    try {
                        params = ColorMapParameterFactory.build(styleRule,
                                styleRule.getPreferences().getDisplayUnits());
                        if (params.getColorMapName() != null) {
                            params.setColorMap(ColorMapLoader
                                    .loadColorMap(params.getColorMapName()));
                            getCapability(ColorMapCapability.class)
                                    .setColorMapParameters(params, false);
                        }
                    } catch (StyleException | ColorMapException e) {
                        /* Do nothing, just default the color. */
                        params = null;
                        statusHandler.warn(
                                "An error occurred generating the ColorMapCapability for this resource. All loaded Geometries will have their colors defaulted.");
                    }
                }

                /*
                 * The default values that will be modified if a matching
                 * StyleRule was found (or to default to if not).
                 */
                float lineWidth = 1.f;
                float alpha = 1.f;
                float floatVal = Float.NaN;
                RGB rgb = null;

                if (styleRule != null) {
                    if (styleRule
                            .getPreferences() instanceof GeometryPreferences) {
                        prefs = (GeometryPreferences) styleRule
                                .getPreferences();

                        /*
                         * Determine the width to draw the Geometry line (or
                         * radius for a point).
                         */
                        if (prefs.getLineWidth() instanceof FloatAttribute) {
                            FloatAttribute lineWidthAtt = (FloatAttribute) prefs
                                    .getLineWidth();

                            /*
                             * Find the first instance of a matching attribute.
                             */
                            for (FloatAttribute att : record.getFloatAtt()) {
                                if (att.getName()
                                        .equals(lineWidthAtt.getName())) {
                                    lineWidth = att.getValue();
                                    break;
                                }
                            }
                        } else if (prefs
                                .getLineWidth() instanceof IntegerAttribute) {
                            IntegerAttribute lineWidthAtt = (IntegerAttribute) prefs
                                    .getLineWidth();

                            for (IntegerAttribute att : record
                                    .getIntegerAtt()) {
                                if (att.getName()
                                        .equals(lineWidthAtt.getName())) {
                                    lineWidth = att.getValue();
                                    break;
                                }
                            }
                        } else if (prefs.getLineWidth() instanceof Float) {
                            /*
                             * Otherwise we are working with a user-defined
                             * value.
                             */
                            lineWidth = ((Float) prefs.getLineWidth())
                                    .floatValue();
                        }
                        /* Finished determining line width. */

                        /*
                         * Now determine the appropriate color for the Drawable
                         * (IRenderable), based on the attribute specified.
                         */
                        if (prefs.getLineColor() instanceof FloatAttribute) {
                            FloatAttribute lineColorAtt = (FloatAttribute) prefs
                                    .getLineColor();

                            for (FloatAttribute recordAtt : record
                                    .getFloatAtt()) {
                                if (recordAtt.getName()
                                        .equals(lineColorAtt.getName())) {
                                    floatVal = recordAtt.getValue();
                                    for (GenericGeometryStyleAttribute attConv : prefs
                                            .getAttConversions()) {
                                        if (attConv.getDataName().equals(
                                                lineColorAtt.getName())) {
                                            floatVal = (float) attConv
                                                    .convertValue(floatVal);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        } else if (prefs
                                .getLineColor() instanceof IntegerAttribute) {
                            IntegerAttribute lineColorAtt = (IntegerAttribute) prefs
                                    .getLineColor();

                            for (IntegerAttribute recordAtt : record
                                    .getIntegerAtt()) {
                                if (recordAtt.getName()
                                        .equals(lineColorAtt.getName())) {
                                    floatVal = recordAtt.getValue();
                                    for (GenericGeometryStyleAttribute attConv : prefs
                                            .getAttConversions()) {
                                        if (attConv.getDataName().equals(
                                                lineColorAtt.getName())) {
                                            floatVal = (float) attConv
                                                    .convertValue(floatVal);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        } else if (prefs.getLineColor() instanceof Color) {
                            rgb = convertColorToRGB(
                                    (Color) prefs.getLineColor());
                        }
                    }
                }

                /*
                 * Determine the RGB value to set for the Drawable
                 * (IRenderable), if it hasn't already been
                 * calculated/defaulted.
                 */
                if (rgb == null) {
                    if (params == null) {
                        rgb = getCapability(ColorableCapability.class)
                                .getColor();
                    } else if (!Float.isNaN(floatVal)) {
                        Color color = params.getColorByValue(floatVal);
                        alpha = color.getAlpha();
                        rgb = convertScaledColorToRGB(color);
                    }
                } else if (!Float.isNaN(floatVal)) {
                    Color color = params.getColorByValue(floatVal);
                    alpha = color.getAlpha();
                    rgb = convertScaledColorToRGB(color);
                }

                if (hasCapability(OutlineCapability.class)) {
                    lineWidth = getCapability(OutlineCapability.class)
                            .getOutlineWidth();
                }

                /*
                 * Add the GeoDataRecord:Drawable pairing to the DataFrame.
                 */
                if (record.getGeometry() instanceof Point) {
                    /* Handle the Magnification */
                    if (lineWidth < minPointSize) {
                        minPointSize = lineWidth;
                    } else if (lineWidth > maxPointSize) {
                        maxPointSize = lineWidth;
                    }

                    PointDrawable point = new PointDrawable(rgb,
                            (Point) record.getGeometry(), lineWidth, descriptor,
                            pointStyle);
                    points.add(point);
                    map.put(record, point);
                } else {
                    map.put(record, new GeoDrawable(rgb, record.getGeometry(),
                            lineWidth, alpha, LineStyle.SOLID, descriptor));
                }
            }
            /* Apply the magnification to PointDrawables. */
            if (minPointSize != maxPointSize) {
                if (minMagSize != maxMagSize) {
                    for (PointDrawable point : points) {
                        point.magnifyPoint(minPointSize, maxPointSize,
                                minMagSize, maxMagSize);
                    }
                }

            }
        }

        /**
         * Dispose of the DataFrame information when the Resource is disposed
         * of.
         */
        public void dispose() {
            if (map.values() == null) {
                return;
            }
            for (IRenderable renderable : map.values()) {
                if (renderable instanceof PointDrawable) {
                    ((PointDrawable) renderable).dispose();
                } else if (renderable instanceof GeoDrawable) {
                    ((GeoDrawable) renderable).dispose();
                }
            }
            map.clear();

        }

        /**
         * Return the Set of GeoDataRecords contained within the DataFrame
         * mapping.
         *
         * @return the Set of GeoDataRecords.
         */
        public Set<GeoDataRecord> getRecords() {
            return map.keySet();
        }

        /**
         * Return the Renderables contained within the DataFrame mapping.
         *
         * @return the Collection of IRenderables.
         */
        public Collection<IRenderable> getDrawables() {
            return map.values();
        }

        /**
         * Remove a record from the DataFrame.
         *
         * @param record
         *            The GeoDataRecord to be removed.
         */
        public void remove(GeoDataRecord record) {
            IRenderable renderable = map.remove(record);
            if (renderable instanceof PointDrawable) {
                ((PointDrawable) renderable).dispose();
            } else if (renderable instanceof GeoDrawable) {
                ((GeoDrawable) renderable).dispose();
            }

        }

    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.edex.plugin.odim;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raytheon.edex.exception.DecoderException;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.exception.BadDataException;
import com.raytheon.uf.common.dataplugin.exception.MalformedDataException;
import com.raytheon.uf.common.dataplugin.exception.UnrecognizedDataException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;

import gov.noaa.nws.ocp.common.dataplugin.odim.CARadarElevations;
import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMStoredData;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * ODIM decoder implementation
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
public class ODIMDecoder {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ODIMDecoder.class);

    // Formats of date/time string attributes
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMdd");

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter
            .ofPattern("HHmmss");

    // root group attributes
    private static final String CONVENTIONS = "Conventions";

    private static final Pattern CONVENTIONS_PATTERN = Pattern
            .compile("^ODIM_H5/V2_[1-3]$");

    private static final String WHAT = "what";

    private static final String WHERE = "where";

    // "what" group attributes
    private static final String WHAT_OBJECT = "object";

    private static final String WHAT_SOURCE = "source";

    private static final String WHAT_VERSION = "version";

    private static final String WHAT_NODATA = "nodata";

    private static final String WHAT_UNDETECT = "undetect";

    private static final String MAJOR = "major";

    private static final String MINOR = "minor";

    private static final Pattern VERSION_PATTERN = Pattern
            .compile("^H5rad (?<" + MAJOR + ">\\d+).(?<" + MINOR + ">\\d+)$");

    // what:source identifiers
    private static final String NOD = "NOD";

    // "where" group attributes
    private static final String WHERE_LAT = "lat";

    private static final String WHERE_LON = "lon";

    private static final String WHERE_HEIGHT = "height";

    // "how" group attributes
    private static final String HOW_AZANGLES = "azangles";

    private static final String HOW_SCAN_INDEX = "scan_index";

    private static final Pattern DATASET_PATTERN = Pattern
            .compile("^dataset(\\d+)$");

    private static final Pattern DATA_PATTERN = Pattern.compile("^data(\\d+)$");

    private static final String OBJECT_PVOL = "PVOL";

    // Polar data set "what" group attributes
    private static final String POLAR_WHAT_PRODUCT = "product";

    // Polar data set "where" group attributes
    private static final String POLAR_WHERE_NBINS = "nbins";

    private static final String POLAR_WHERE_NRAYS = "nrays";

    private static final String POLAR_WHERE_RSCALE = "rscale";

    private static final String POLAR_WHERE_ELANGLE = "elangle";

    private static final String POLAR_WHERE_RANGE_START = "rstart";

    private static CARadarElevations caElevs;

    public PluginDataObject[] decode(File f)
            throws BadDataException, IOException {
        return readODIMRecords(NetcdfFile.open(f.getPath()));
    }

    private ODIMRecord[] readODIMRecords(NetcdfFile f) throws BadDataException {
        // @formatter:off
        return readODIMRecordsMap(f).values().stream()
                .flatMap(Stream::of)
                .collect(Collectors.toList())
                .toArray(ODIMRecord[]::new);
        // @formatter:on
    }

    private Map<Integer, ODIMRecord[]> readODIMRecordsMap(NetcdfFile f)
            throws BadDataException {
        Group root = f.getRootGroup();

        Attribute conventions = root.findAttribute(CONVENTIONS);
        if (conventions != null) {
            String value = conventions.getStringValue();
            if (value == null
                    || !CONVENTIONS_PATTERN.matcher(value).matches()) {
                logger.warn("Potentially unsupported format: " + value);
            }
        }

        checkIsPolarVolume(root);

        ODIMRecord template = getRecordTemplate(root);
        // @formatter:off
        Map<Integer, ODIMRecord[]> groupedRecords = root.getGroups().stream()
                .filter(g -> DATASET_PATTERN.matcher(g.getShortName()).matches())
                .collect(Collectors.toMap(
                        this::getDatasetNumber,
                        g -> processDataset(g, template),
                        (a, b) -> a,
                        TreeMap::new));
        // @formatter:on
        return renumberVolumeScanIndicies(groupedRecords);
    }

    // Currently, just asserts that all scans have an elevation number.
    private Map<Integer, ODIMRecord[]> renumberVolumeScanIndicies(
            Map<Integer, ODIMRecord[]> groupedRecords)
            throws MalformedDataException {
        for (ODIMRecord[] recs : groupedRecords.values()) {
            for (ODIMRecord rec : recs) {
                if (rec.getElevationNumber() == null) {
                    throw new MalformedDataException(
                            "Scan missing scan_index attribute");
                }
            }
        }
        return groupedRecords;
    }

    private void checkIsPolarVolume(Group g) throws BadDataException {
        Group what = getRequiredGroup(g, WHAT);

        String object = findStringAttribute(what, WHAT_OBJECT);
        if (!OBJECT_PVOL.equals(object)) {
            throw new UnrecognizedDataException(
                    "Unsupported object type " + object);
        }
    }

    private int getDatasetNumber(Group g) {
        Matcher matcher = DATASET_PATTERN.matcher(g.getShortName());
        matcher.matches();
        return Integer.parseInt(matcher.group(1));
    }

    private ODIMRecord[] processDataset(Group g, ODIMRecord template) {
        try {
            /*
             * Assumes the data set is a polar volume because of the earlier
             * checkIsPolarVolume call. If other types are to be supported,
             * would need to pass that information to this method or dispatch to
             * other processXxxDataset methods elsewhere.
             */
            return processPolarDataset(g, template);
        } catch (BadDataException e) {
            logger.error(formatDatasetError(g, e), e);
            return new ODIMRecord[0];
        }
    }

    private ODIMRecord createRecordFromTemplate(ODIMRecord template) {
        ODIMRecord rec = template.shallowClone();
        rec.setStoredData(new ODIMStoredData());
        rec.setAngleData(template.getAngleData());
        return rec;
    }

    private ODIMRecord[] processPolarDataset(Group g, ODIMRecord template)
            throws BadDataException {
        ODIMRecord polarTemplate = createRecordFromTemplate(template);
        Group what = getRequiredGroup(g, WHAT);

        polarTemplate.setStartTime(parseDateTime("start", what));
        polarTemplate.setEndTime(parseDateTime("end", what));

        String product = getRequiredStringAttribute(what, POLAR_WHAT_PRODUCT);

        if (!"SCAN".equals(product)) {
            throw new UnrecognizedDataException(
                    "Unsupported product type: " + product);
        }

        Group where = g.findGroup(WHERE);
        int nBins = getRequiredIntAttribute(where, POLAR_WHERE_NBINS);
        int nRays = getRequiredIntAttribute(where, POLAR_WHERE_NRAYS);
        int gateResolution = getRequiredIntAttribute(where, POLAR_WHERE_RSCALE);
        double rawElevAngle = getRequiredDoubleAttribute(where,
                POLAR_WHERE_ELANGLE);
        double usableElevAngle = roundReasonable(rawElevAngle);
        Number rangeStart = where.findAttribute(POLAR_WHERE_RANGE_START)
                .getNumericValue();
        if (rangeStart.doubleValue() != 0.0) {
            throw new UnrecognizedDataException(
                    "Non-zero start range is not supported.");
        }

        polarTemplate.setNumRadials(nRays);
        polarTemplate.setNumBins(nBins);
        polarTemplate.setGateResolution(gateResolution);
        polarTemplate.setTrueElevationAngle(usableElevAngle);

        double primaryElevationAngle = usableElevAngle;
        CARadarElevations elevs = getCARadarElevations();
        if (elevs != null) {
            primaryElevationAngle = elevs
                    .getPrimaryElevationAngle(usableElevAngle);
        }
        polarTemplate.setPrimaryElevationAngle(primaryElevationAngle);

        Group how = g.findGroup("how");
        Attribute anglesAttr = how.findAttribute(HOW_AZANGLES);
        polarTemplate.setAngleData(
                (float[]) anglesAttr.getValues().get1DJavaArray(Float.TYPE));

        Attribute scanIndexAttr = how.findAttribute(HOW_SCAN_INDEX);
        polarTemplate
                .setElevationNumber(scanIndexAttr.getNumericValue().intValue());

        // @formatter:off
        return g.getGroups().stream()
                .filter(d -> DATA_PATTERN.matcher(d.getShortName()).matches())
                .map(d -> processPolarDataWrapped(d, polarTemplate))
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
                .toArray(ODIMRecord[]::new);
        // @formatter:on
    }

    private ODIMRecord processPolarDataWrapped(Group g,
            ODIMRecord polarTemplate) {
        try {
            return processPolarData(g, polarTemplate);
        } catch (BadDataException e) { // NOSONAR
            // Do not need a stack trace for these messages
            logger.error(formatDatasetError(g, e));
            return null;
        } catch (DecoderException e) {
            logger.error(formatDatasetError(g, e), e);
            return null;
        }
    }

    private ODIMRecord processPolarData(Group g, ODIMRecord polarTemplate)
            throws UnrecognizedDataException, DecoderException {
        ODIMRecord rec = createRecordFromTemplate(polarTemplate);
        Group what = g.findGroup("what");

        double offset = what.findAttribute("offset").getNumericValue()
                .doubleValue();
        double scale = what.findAttribute("gain").getNumericValue()
                .doubleValue();

        String quantity = what.findAttribute("quantity").getStringValue();

        String unit;
        switch (quantity) {
        // Reflectivity
        case "DBZH":
        case "TH":
        case "TV":
            unit = "dBZ";
            break;
        // Differential reflectivity
        case "ZDR":
            unit = "dB";
            break;
        // Velocity and spectrum width
        case "VRADH":
        case "WRADH":
            unit = "(m/s)";
            break;
        // Specific differential phase
        case "KDP":
            unit = "deg/km";
            break;
        // Differential phase
        case "PHIDP":
        case "UPHIDP":
            unit = "deg";
            break;
        // Correlation coefficient
        case "RHOHV":
        case "URHOHV":
            unit = null;
            break;
        // Signal quality
        case "SQIH":
        case "SQIV":
        case "USQIH":
        case "USQIV":
            unit = null;
            break;
        default:
            throw new UnrecognizedDataException(
                    "Unsupported quantity " + quantity);
        }

        rec.setQuantity(quantity);
        rec.setUnit(unit);
        rec.setEncodingScale(scale);
        rec.setEncodingOffset(offset);

        /*
         * NEXRAD 8-bit reflectivity has fixed values 0 = "Below Threshold" and
         * 1 = "Missing".
         *
         * NEXRAD 8-bit velocity/spectrum width have fixed values 0 = Below
         * Threshold" and 1 = "Range
         * Folded".  ODIM documentation does not state that the "nodata" value
         * represents a range-folded result specifically.
         */
        Number missing = findNumericAttribute(what, WHAT_UNDETECT);
        if (missing != null) {
            rec.setMissingValue(missing.intValue());
        }
        Number noData = findNumericAttribute(what, WHAT_NODATA);
        if (noData != null) {
            rec.setNoDataValue(noData.intValue());
        }

        Variable v = g.findVariable("data");
        DataType dt = v.getDataType();
        if (dt == DataType.BYTE) {
            byte[] input;
            try {
                input = (byte[]) v.read().get1DJavaArray(Byte.TYPE);
            } catch (IOException e) {
                throw new DecoderException("I/O error", e);
            }
            rec.setRawData(input);
            rec.setNumLevels(256);
        } else if (dt == DataType.SHORT) {
            short[] input;
            try {
                input = (short[]) v.read().get1DJavaArray(Short.TYPE);
            } catch (IOException e) {
                throw new DecoderException("I/O error", e);
            }
            rec.setRawShortData(input);
            rec.setNumLevels(65536);
        } else {
            throw new UnrecognizedDataException(String
                    .format("Unsupported data type for %s: %s", quantity, dt));
        }

        rec.setPersistenceTime(new Date());
        return rec;
    }

    private ODIMRecord getRecordTemplate(Group root) throws BadDataException {
        ODIMRecord rec = new ODIMRecord();
        Group what = getRequiredGroup(root, WHAT);

        String versionString = findStringAttribute(what, WHAT_VERSION);
        if (versionString != null) {
            boolean supported = false;
            try {
                Matcher matcher = VERSION_PATTERN.matcher(versionString);
                if (matcher.matches()) {
                    int major = Integer.parseInt(matcher.group(MAJOR));
                    int minor = Integer.parseInt(matcher.group(MINOR));
                    supported = major == 2 && minor >= 1 && minor <= 3;
                }
            } catch (RuntimeException e) { // NOSONAR
                // Ignore and report as potentially unsupported version below.
            }
            if (!supported) {
                logger.warn(
                        "Potentially unsupported version: " + versionString);
            }
        }

        Map<String, String> map = parseSourceIdentifiers(
                getRequiredStringAttribute(what, WHAT_SOURCE));
        String node = map.get(NOD);
        if (node == null) {
            throw new MalformedDataException("Missing station identifier");
        }

        Group where = getRequiredGroup(root, WHERE);
        float lat = getRequiredFloatAttribute(where, WHERE_LAT);
        float lon = getRequiredFloatAttribute(where, WHERE_LON);
        Attribute heightAttribute = where.findAttribute(WHERE_HEIGHT);
        Number heightNumber = heightAttribute != null
                ? heightAttribute.getNumericValue()
                : null;
        Float height = heightNumber != null ? heightNumber.floatValue() : null;

        rec.setNode(node);
        rec.setDataTime(new DataTime(parseDateTime("", what)));
        rec.setLatitude(lat);
        rec.setLongitude(lon);
        if (height != null) {
            rec.setElevation(height);
        }

        return rec;
    }

    private Date parseDateTime(String attributePrefix, Group g)
            throws BadDataException {
        String dateString = getRequiredStringAttribute(g,
                attributePrefix + "date");
        String timeString = getRequiredStringAttribute(g,
                attributePrefix + "time");
        return Date.from(LocalTime.parse(timeString, TIME_FORMAT)
                .atDate(LocalDate.parse(dateString, DATE_FORMAT))
                .toInstant(ZoneOffset.UTC));
    }

    private Map<String, String> parseSourceIdentifiers(String s) {
        // @formatter:off
        return Stream.of(s.split(","))
                .map(elem -> Stream.of(elem.split(":", 2))
                        .toArray(String[]::new))
                .filter(parts -> parts.length == 2 && !parts[0].isEmpty())
                // Ignore duplicates, favoring the first occurrence.
                .collect(Collectors.toMap(
                        parts -> parts[0],
                        parts -> parts[1],
                        (a, b) -> a));
        // @formatter:on
    }

    private static double roundReasonable(double v) {
        return Math.round(v * 1000) / 1000.0;
    }

    private static Group getRequiredGroup(Group g, String name)
            throws BadDataException {
        Group group = g.findGroup(name);
        if (group == null) {
            throw new MalformedDataException("Missing group " + name);
        }
        return group;
    }

    private static String findStringAttribute(Group group, String name) {
        Attribute attribute = group.findAttribute(name);
        return attribute != null ? attribute.getStringValue() : null;
    }

    private static Number findNumericAttribute(Group group, String name) {
        Attribute attribute = group.findAttribute(name);
        return attribute != null ? attribute.getNumericValue() : null;
    }

    private static Attribute getRequiredAttribute(Group group, String name)
            throws BadDataException {
        Attribute attribute = group.findAttribute(name);
        if (attribute != null) {
            return attribute;
        } else {
            throw new MalformedDataException(
                    String.format("Group %s: missing attribute %s",
                            group.getFullName(), name));
        }
    }

    private static String getRequiredStringAttribute(Group group, String name)
            throws BadDataException {
        Attribute attribute = getRequiredAttribute(group, name);
        String value = attribute.getStringValue();
        if (value == null) {
            throw new MalformedDataException(
                    String.format("Group %s: attribute %s: expected string",
                            group.getFullName(), name));
        }
        return value;
    }

    private static Number getRequiredNumericAttribute(Group group, String name)
            throws BadDataException {
        Attribute attribute = getRequiredAttribute(group, name);
        Number number = attribute.getNumericValue();
        if (number == null) {
            throw new MalformedDataException(
                    String.format("Group %s: attribute %s: expected number",
                            group.getFullName(), name));
        }
        return number;
    }

    private static double getRequiredDoubleAttribute(Group group, String name)
            throws BadDataException {
        return getRequiredNumericAttribute(group, name).doubleValue();
    }

    private static float getRequiredFloatAttribute(Group group, String name)
            throws BadDataException {
        return getRequiredNumericAttribute(group, name).floatValue();
    }

    private static int getRequiredIntAttribute(Group group, String name)
            throws BadDataException {
        return getRequiredNumericAttribute(group, name).intValue();
    }

    private static String formatDatasetError(Group g, Exception e) {
        return String.format("Error decoding Dataset \"%s\": %s",
                g.getFullName(), e.getLocalizedMessage());
    }

    private static CARadarElevations getCARadarElevations() {
        synchronized (ODIMDecoder.class) {
            if (caElevs == null) {
                try {
                    caElevs = CARadarElevations.load();
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return caElevs;
    }

}

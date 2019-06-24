package gov.noaa.nws.obs.edex.plugin.geodata.decoder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.netcdf.description.exception.InvalidDescriptionException;
import com.raytheon.uf.edex.netcdf.description.field.IFieldDescription;
import com.raytheon.uf.edex.netcdf.description.field.date.EpochOffsetDateValue;
import com.raytheon.uf.edex.netcdf.description.field.date.FormattedDateValue;
import com.raytheon.uf.edex.netcdf.description.field.direct.VariableDescription;
import com.raytheon.uf.edex.netcdf.description.field.indirect.DelegateFieldDescription;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import gov.noaa.nws.obs.common.dataplugin.geodata.FloatAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.GeoDataRecord;
import gov.noaa.nws.obs.common.dataplugin.geodata.IntegerAttribute;
import gov.noaa.nws.obs.common.dataplugin.geodata.StringAttribute;
import gov.noaa.nws.obs.edex.plugin.geodata.description.ProductDescription;
import gov.noaa.nws.obs.edex.plugin.geodata.description.ProductDescriptions;
import gov.noaa.nws.obs.edex.plugin.geodata.description.VariableDescriptor;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * GeoDataDecoder
 *
 * Decode items within a NetCDF file into JTS Geometries, and create
 * GeoDataRecords of the Geometry and any associated values.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 08/20/2016    19064      mcomerford  Initial creation (DCS 19064)
 *
 * </pre>
 *
 * @author matt.comerford
 * @version 1.0
 */

public class GeoDataDecoder {

    /* For logging errors that pop up during the decode process. */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(GeoDataDecoder.class);

    /*
     * For parsing out the Epoch Offset Time to set a GeoDataRecord's DataTime.
     */
    private static final String EPOCH_FMT_STRING = "yyyy-MM-dd HH:mm:ss";

    /*
     * For defaulting the epoch to the UNIX epoch (if it is determined the
     * dataFile has the same epoch).
     */
    private static final String UNIX_EPOCH = "1970-01-01 00:00:00";

    /* Factory for generating the Geometries. */
    private GeometryFactory geomFact = new GeometryFactory();

    /* The ProductDescriptions that will be loaded from descriptions files. */
    private ProductDescriptions descriptions;

    /*
     * Generic exception message for an array-retrieval error (mismatch in
     * indices, etc.) for a given GeoDataRecord. Used when debugging a given
     * ProductDescription.
     */
    private static final String ARRAY_RETRIEVAL_ERROR = "Could not retrieve the next value in the \"%s\" array.";

    /**
     * Open a NetCDF file and loop through the list of ProductDescription(s) in
     * order to generate GeoDataRecord(s)
     *
     * @param file
     *            The file that is being decoded
     * @return An array of PluginDataObjects (GeoDataRecords)
     * @throws Exception
     *             If the decoded throws an Exception other than an
     *             InvalidDescriptionException.
     */
    public PluginDataObject[] decode(File file) throws Exception {

        List<GeoDataRecord> records = new ArrayList<>();

        int descMatch = 0;

        NetcdfFile dataFile = NetcdfFile.open(file.getAbsolutePath());

        /*
         * Decode the file open the file parse through to set GeoDataRecords
         * need to collect attribute/variable/value definitions product
         * descriptions?
         */
        for (ProductDescription description : descriptions.getDescriptions()) {
            try {
                processDescription(description, dataFile, records);
                descMatch++;
            } catch (Exception e) {
                if (description.isDebug()) {
                    statusHandler.info(
                            "ProductDescription \"" + description.getName()
                                    + "\" -- " + e.getMessage());
                }
                if (e instanceof InvalidDescriptionException == false) {
                    statusHandler.info("Unhandled exception while decoding:");
                    e.printStackTrace();
                    break;
                }
            }

        }

        dataFile.close();

        if (descMatch == 0) {
            statusHandler.info("No ProductDescription(s) in " + file.getName()
                    + " generated a valid GeoDataRecord.");
        }

        return records.toArray(new PluginDataObject[0]);
    }

    /**
     * Process a ProductDescription to generate valid GeoDataRecords
     *
     * @param description
     *            The ProductDescription the decoder is creating GeoDataRecords
     *            against.
     * @param dataFile
     *            The NetCDF file containing the data to be decoded.
     * @param records
     *            The list that will be populated with each generated
     *            GeoDataRecord.
     * @throws Exception
     * @throws IOException
     */
    private void processDescription(ProductDescription description,
            NetcdfFile dataFile, List<GeoDataRecord> records)
                    throws InvalidDescriptionException, IOException {

        /*
         * Data/file epoch, source, and product should all be the same across
         * all records generated from the passed NetCDF file.
         */
        Calendar epochCal = genEpochCalendar(description, dataFile);
        String source = description.getSource().getString(dataFile);
        String product = description.getProduct().getString(dataFile);

        /* Geometry-related Arrays that all must be the same size. */
        List<Array> geomDimensionArrays = new ArrayList<>();

        /*
         * Generate a list of Geometry-related trivia. For any given
         * GeoDataRecord, there may be zero or more associated "trivia"
         * variables/values. Their value types fall into one of the following
         * categories: String, Float, Integer.
         */
        List<DelegateFieldDescription> lists = description.getGeomNInfos();
        List<VariableDescriptor> geomAtts = new ArrayList<>();
        for (DelegateFieldDescription desc : lists) {
            if (desc.getDelegate() instanceof VariableDescription) {

                /* Read the NetCDF data into an Array. */
                Variable descVar = dataFile.findVariable(desc.getName());
                VariableDescriptor newDesc;
                if (descVar != null) {
                    newDesc = new VariableDescriptor(descVar, desc.getName());
                } else {
                    throw new InvalidDescriptionException(
                            "Could not find trivia variable \"" + desc.getName()
                                    + "\" in the NetCDF File "
                                    + dataFile.getTitle());
                }
                if (!geomAtts.contains(newDesc)) {
                    newDesc.setArray(
                            dataFile.findVariable(desc.getName()).read());
                    geomDimensionArrays.add(newDesc.getArray());
                    geomAtts.add(newDesc);
                }
            } else {
                /* Otherwise the value needs to be defaulted. */
                VariableDescriptor newDesc;
                if (desc.getDelegate().isNumeric(dataFile)) {
                    newDesc = new VariableDescriptor(desc.getName());
                    newDesc.setDefaultValue(desc.getNumber(dataFile));
                } else {
                    newDesc = new VariableDescriptor(desc.getName());
                    newDesc.setDefaultValue(desc.getString(dataFile));
                }

                if (!geomAtts.contains(newDesc)) {
                    geomAtts.add(newDesc);
                }
            }
        }

        /*
         * Determine the epoch offset values (and subsequent observation times)
         * for the Geometries in the file.
         */
        int epochOffsetVal = 0;
        VariableDescriptor epochOffsetVar = processDelegate(
                description.getDataTime().getRefTime().getDelegate(), dataFile);
        if (epochOffsetVar == null) {
            if (description.getDataTime()
                    .getRefTime() instanceof EpochOffsetDateValue) {
                epochOffsetVal = ((EpochOffsetDateValue) description
                        .getDataTime().getRefTime()).getDelegate()
                                .getNumber(dataFile).intValue();
            }
        } else {
            geomDimensionArrays.add(epochOffsetVar.getArray());
        }

        /*
         * Determine the Offset (index) into the lat/lon arrays for the
         * Geometries in the file.
         */
        int geomOffsetVal = 0;
        VariableDescriptor geomOffsetVar = processDelegate(
                description.getGeomOffset().getDelegate(), dataFile);
        if (geomOffsetVar == null) {
            geomOffsetVal = description.getGeomOffset().getDelegate()
                    .getNumber(dataFile).intValue();
        } else {
            geomDimensionArrays.add(geomOffsetVar.getArray());
        }

        /* Determine Geometry Types. */
        int geomTypeVal = -1;
        VariableDescriptor geomTypeVar = processDelegate(
                description.getGeomType().getDelegate(), dataFile);
        if (geomTypeVar == null) {
            geomTypeVal = description.getGeomType().getDelegate()
                    .getNumber(dataFile).intValue();
        } else {
            geomDimensionArrays.add(geomTypeVar.getArray());
        }

        /* Determine number of lat/lon pairs per Geometry */
        int geomNelsVal = 0;
        VariableDescriptor geomNelsVar = processDelegate(
                description.getGeomNels().getDelegate(), dataFile);
        if (geomNelsVar == null) {
            geomNelsVal = description.getGeomNels().getDelegate()
                    .getNumber(dataFile).intValue();
        } else {
            geomDimensionArrays.add(geomNelsVar.getArray());
        }

        /*
         * All possible arrays from above that contain Geometry information must
         * be the same size.
         */
        boolean firstEntry = true;
        long firstVal = 0;
        for (Array entry : geomDimensionArrays) {
            if (firstEntry) {
                firstEntry = false;
                firstVal = entry.getSize();
                continue;
            }
            if (entry.getSize() != firstVal) {
                throw new InvalidDescriptionException(
                        "The Variables of dimension \"ngeom\" are not all the same size.");
            }
        }

        /* Set/calculate Arrays/Vars related to the lat/lon arrays. */
        Variable lat = dataFile.findVariable(description.getLat().getName());
        Variable lon = dataFile.findVariable(description.getLon().getName());

        if (lat == null || lon == null) {
            throw new InvalidDescriptionException(
                    "Could not load the necessary lat/lon NetCDF Variables from the file.");
        }

        VariableDescriptor latVar = new VariableDescriptor(lat,
                description.getLat().getName());
        VariableDescriptor lonVar = new VariableDescriptor(lon,
                description.getLon().getName());

            latVar.setArray(lat.read());
            lonVar.setArray(lon.read());

        if (latVar.getArray().getSize() != lonVar.getArray().getSize()) {
            throw new InvalidDescriptionException(
                    "The lat/lon arrays are not of equal size");
        }

        int numGeoms = (int) firstVal;
        int currentOffset = 0;
        boolean initialGeomOffset = true;
        for (int i = 0; i < numGeoms; i++) {

            /* Grab the next DataTime. */
            if (epochOffsetVar != null && epochOffsetVar.getArray() != null) {
                if (epochOffsetVar.getArray().hasNext()) {
                    epochOffsetVal = ((Number) calcValue(epochOffsetVar))
                            .intValue();
                } else {
                    throw new InvalidDescriptionException(String.format(
                            ARRAY_RETRIEVAL_ERROR, epochOffsetVar.getName()));
                }
            }

            /* Calculate the # of elements in this Geometry. */
            if (geomNelsVar != null && geomNelsVar.getArray() != null) {
                if (geomNelsVar.getArray().hasNext()) {
                    geomNelsVal = ((Number) calcValue(geomNelsVar)).intValue();
                } else {
                    throw new InvalidDescriptionException(String.format(
                            ARRAY_RETRIEVAL_ERROR, geomNelsVar.getName()));
                }
            }

            List<Coordinate> geomCoords = null;

            /*
             * Calculate the offset into the lat/lon arrays for this Geometry.
             */
            if (geomOffsetVar != null && geomOffsetVar.getArray() != null) {
                if (geomOffsetVar.getArray().hasNext()) {
                    geomOffsetVal = ((Number) calcValue(geomOffsetVar))
                            .intValue();
                    geomCoords = buildGeomCoords(geomOffsetVal, geomNelsVal,
                            latVar, lonVar);
                } else {
                    throw new InvalidDescriptionException(String.format(
                            ARRAY_RETRIEVAL_ERROR, geomOffsetVar.getName()));
                }
            } else {
                /*
                 * Otherwise we are working with a constant "stride"
                 * (user-provided geomOffset). So we need iterate the
                 * geomOffsetVal each time (so we have the appropriate lat/lon
                 * indices).
                 */
                if (initialGeomOffset) {
                    geomCoords = buildGeomCoords(currentOffset, geomNelsVal,
                            latVar, lonVar);
                    currentOffset += geomOffsetVal;
                    initialGeomOffset = false;
                } else {
                    geomCoords = buildGeomCoords(currentOffset, geomNelsVal,
                            latVar, lonVar);
                    currentOffset += geomOffsetVal;
                }
            }

            /* Calculate the type of this Geometry. */
            if (geomTypeVar != null && geomTypeVar.getArray() != null) {
                if (geomTypeVar.getArray().hasNext()) {
                    geomTypeVal = ((Number) calcValue(geomTypeVar)).intValue();
                } else {
                    throw new InvalidDescriptionException(String.format(
                            ARRAY_RETRIEVAL_ERROR, geomTypeVar.getName()));
                }
            }

            /*
             * Now, we have all the info necessary to generate the
             * GeoDataRecord's associated Geometry.
             */
            Geometry geometry = buildGeometry(geomTypeVal, geomCoords);

            if (geometry == null) {
                continue;
            }

            Calendar dateTime = (Calendar) epochCal.clone();
            dateTime.add(Calendar.SECOND, epochOffsetVal);

            GeoDataRecord record = new GeoDataRecord();

            record.setSource(source);
            record.setProduct(product);
            record.setGeometry(geometry);
            record.setDataTime(new DataTime(dateTime));

            try {
                genRecordAttributes(geomAtts, record);
            } catch (InvalidDescriptionException e) {
                /*
                 * If an Exception is thrown while setting the attribute(s),
                 * then we don't have a valid GeoDataRecord. Still may have
                 * valid records though, so move on to the next iteration of the
                 * loop.
                 */
                continue;
            }

            records.add(record);
        }
    }

    /**
     * Build the appropriate Geometry given the geomType (From the NetCDF File).
     *
     * @param geomTypeVal
     *            The value representing the Geometry type to generate for this
     *            Geometry.
     * @param geomCoords
     *            The list of Coordinates to use to generate this Geometry.
     * @return The Geometry built from the list of Coordinates.
     * @throws InvalidDescriptionException
     *             If the geomCoords list is empty or null.
     */
    public Geometry buildGeometry(int geomTypeVal, List<Coordinate> geomCoords)
            throws InvalidDescriptionException {

        if (geomCoords.size() == 0 || geomCoords == null) {
            throw new InvalidDescriptionException(
                    "The list of Geometry coordinates is null and this Geometry will not be generated.");
        }

        /* The decoder will skip any records with a 'null' Geometry. */
        Geometry g = null;

        switch (geomTypeVal) {
        case 0:
            /* Point(s) */
            if (geomCoords.size() > 1) {
                g = geomFact.createMultiPoint(
                        geomCoords.toArray(new Coordinate[geomCoords.size()]));
            } else {
                g = geomFact.createPoint(geomCoords.get(0));
            }
            break;
        case 1:
        case 2:
            /* Line (Open Path) */
            g = geomFact.createLineString(
                    geomCoords.toArray(new Coordinate[geomCoords.size()]));
            break;
        case 3:
            /* Linear Ring (Closed Path) */
            if (geomCoords.get(geomCoords.size() - 1) != geomCoords.get(0)) {
                geomCoords.add(new Coordinate(geomCoords.get(0)));
            }
            g = geomFact.createLinearRing(
                    geomCoords.toArray(new Coordinate[geomCoords.size()]));
            break;
        case 4:
            /* Polygon */
            if (geomCoords.get(geomCoords.size() - 1) != geomCoords.get(0)) {
                geomCoords.add(new Coordinate(geomCoords.get(0)));
            }
            LinearRing ring = geomFact.createLinearRing(
                    geomCoords.toArray(new Coordinate[geomCoords.size()]));
            g = geomFact.createPolygon(ring);
            break;
        }

        return g;
    }

    /**
     * Build a list of lon/lat coordinates that make up a particular Geometry
     * item in the data file.
     *
     * @param startIndex
     *            Index into the lat/lon arrays of the starting Coordinate of
     *            the Geometry that will be generated.
     * @param numElements
     *            Number of lat/lon pairs to include in Geometry's Coordinates
     * @param latVar
     *            VariableDescriptor containing Latitude Variable information
     *            the corresponding Array.
     * @param lonVar
     *            VariableDescriptor containing Longitude Variable information
     *            the corresponding Array.
     * @return A list of Coordinates that make up the Geometry.
     */
    private List<Coordinate> buildGeomCoords(int startIndex, int numElements,
            VariableDescriptor latVar, VariableDescriptor lonVar) {

        int endIndex = startIndex + numElements - 1;
        List<Coordinate> geomCoords = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            float lat = calcScaledCoord(latVar, i);
            float lon = calcScaledCoord(lonVar, i);

            geomCoords.add(new Coordinate(lon, lat));
        }

        return geomCoords;
    }

    /**
     * Calculate the next value in the appropriate (Geometry-dimension) array,
     * taking into account the defined scale_factor, add_offset, and _FillValue
     * values contained within the variable.
     *
     * @param var
     *            The instance representing the Variable, and its scale_factor,
     *            add_offset, and _FillValue values.
     * @return The actual value, adjusted for the scale_factor, add_offset, and
     *         _FillValue.
     */
    private Object calcValue(VariableDescriptor var) {

        Array array = var.getArray();
        Object value = null;
        DataType dtype = var.getdType();

        if (dtype.isString()) {
            value = array.next();
        } else {
            Number numValue = (Number) array.next();
            /*
             * Only calculate the value if we aren't dealing with a _FillValue.
             */
            if ((numValue.floatValue() != var.getFillValue().floatValue())) {
                value = numValue.floatValue()
                        * var.getScaleFactor().floatValue()
                        + var.getAddOffset().floatValue();
            } else if (numValue.floatValue() == 0) {
                /*
                 * There's the case that the Fill Value defaults to zero, and we
                 * may want to keep these cases.
                 */
                value = numValue.floatValue()
                        * var.getScaleFactor().floatValue()
                        + var.getAddOffset().floatValue();
            }
        }

        return value;
    }

    /**
     * Calculate the scaled lat or lon value by index.
     *
     * @param var
     *            The instance representing the NetCDF Variable, data Array, and
     *            its fill, scale, and offset information
     * @param index
     *            The index into the lat/lon array of the Geometry's next
     *            coordinate.
     * @return The scaled lat/lon value.
     */
    private float calcScaledCoord(VariableDescriptor var, int index) {
        float value = var.getArray().getFloat(index);

        if (value != var.getFillValue().floatValue()) {
            value = value * var.getScaleFactor().floatValue()
                    + var.getAddOffset().floatValue();
        }

        return value;
    }

    /**
     * Populate a VariableDescriptor from a given IFieldDescription (if it
     * references a NetCDF Array).
     *
     * @param delegate
     *            The IFieldDescription to check against.
     * @param dataFile
     *            The NetCDF file containing the ProductDescriptions
     *            Variable/Attribute/Value.
     * @return The VariableDescriptor instance populated from the
     *         IFieldDescription
     * @throws InvalidDescriptionException
     *             If there is an error retrieving the default value from the
     *             NetCDF File.
     * @throws IOException
     *             If there is an error reading the Variable into an array.
     */
    public VariableDescriptor processDelegate(IFieldDescription delegate,
            NetcdfFile dataFile)
                    throws InvalidDescriptionException, IOException {

        if (!delegate.isPresent(dataFile)) {
            throw new InvalidDescriptionException(
                    "Variable \"" + delegate.getName()
                            + "\" is not present in the NetCDF File "
                            + dataFile.getTitle());
        }

        VariableDescriptor var = null;
        if (delegate instanceof VariableDescription) {
            // try {
            var = new VariableDescriptor(
                    dataFile.findVariable(delegate.getName()),
                    delegate.getName());
            var.setArray(dataFile.findVariable(delegate.getName()).read());
        }

        return var;
    }

    /**
     * Generate the Calendar instance that represents the NetCDF File's epoch
     * date; Some files base their data times from the default UNIX (1970-01-01
     * 00:00:00), yet others (such as some GOES-R) use an alternate time epoch.
     * This method provides the functionality to produce a Calendar instance
     * that matches the file's epoch, so that the dataTime may be accurately
     * calculated during the rest of the decode process.
     *
     * @param description
     *            The ProductDescription the epoch is being pulled from.
     * @param dataFile
     *            The NetCDF file to pull the epoch from.
     * @return A Calendar representing the dataFile's epoch.
     * @throws InvalidDescriptionException
     *             if there isn't enough information to produce the epoch.
     */
    private Calendar genEpochCalendar(ProductDescription description,
            NetcdfFile dataFile) throws InvalidDescriptionException {

        DateFormat sdf = new SimpleDateFormat(EPOCH_FMT_STRING);
        Date timeDate = null;

        /* Default to the file-defined epoch offset (if it is present). */
        if (description.getEpoch() != null
                && description.getEpoch().isPresent(dataFile)) {

            /* The offset between UNIX_EPOCH and the file's epoch (seconds). */
            Number dataEpoch = description.getEpoch().getNumber(dataFile);

            if (dataEpoch.intValue() > 0) {
                int secondsToMillis = 1000;
                timeDate = new Date(dataEpoch.longValue() * secondsToMillis);
            } else {
                try {
                    timeDate = sdf.parse(UNIX_EPOCH);
                } catch (ParseException e) {
                    /* Won't get here (UNIX_EPOCH format matches sdf. */
                    throw new InvalidDescriptionException(e);
                }
            }
        } else {
            if (description.getDataTime()
                    .getRefTime() instanceof EpochOffsetDateValue) {
                try {
                    EpochOffsetDateValue refTime = (EpochOffsetDateValue) description
                            .getDataTime().getRefTime();
                    String epochStr = refTime.getEpoch();
                    timeDate = sdf.parse(epochStr);
                } catch (ParseException e) {
                    throw new InvalidDescriptionException(
                            "The \"epoch\" format must be \"yyyy-MM-dd HH:mm:ss\".");
                }
            } else if (description.getDataTime()
                    .getRefTime() instanceof FormattedDateValue) {
                FormattedDateValue refTime = (FormattedDateValue) description
                        .getDataTime().getRefTime();
                timeDate = refTime.getDate(dataFile);
            }
        }

        return TimeUtil.newGmtCalendar(timeDate);
    }

    /**
     * Generate the Set(s) of Integer/Float/String Attribute(s) that pertain to
     * a single GeoDataRecord.
     *
     * @param geomAtts
     *            List of VariableDescriptors that describe a GeoDataRecords
     *            Attributes.
     * @param record
     *            The GeoDataRecord whose attributes will be set.
     * @throws InvalidDescriptionException
     *             If varValue is not an acceptable GeoDataRecord Attribute
     *             type.
     */
    private void genRecordAttributes(List<VariableDescriptor> geomAtts,
            GeoDataRecord record) throws InvalidDescriptionException {

        Set<FloatAttribute> floatAtts = new HashSet<>();
        Set<IntegerAttribute> integerAtts = new HashSet<>();
        Set<StringAttribute> stringAtts = new HashSet<>();

        Object value = null;

        for (VariableDescriptor att : geomAtts) {
            if (att.getArray() != null) {
                if (att.getArray().hasNext()) {
                    value = calcValue(att);
                } else {
                    throw new InvalidDescriptionException(String
                            .format(ARRAY_RETRIEVAL_ERROR, att.getName()));
                }
            } else {
                value = att.getDefaultValue();
            }

            /* Set the Attributes */
            if (value != null) {
                if (value instanceof Number) {
                    if (value instanceof Integer) {
                        integerAtts.add(new IntegerAttribute(att.getName(),
                                ((Number) value).intValue(), record));
                    } else {
                        floatAtts.add(new FloatAttribute(att.getName(),
                                ((Number) value).floatValue(), record));
                    }
                } else if (value instanceof String) {
                    stringAtts.add(new StringAttribute(att.getName(),
                            value.toString(), record));
                } else {
                    throw new InvalidDescriptionException(
                            "The value associated with the record attribute \""
                                    + att.getName()
                                    + "\" is neither a String or a Number.");
                }
            }
        }
        if (integerAtts.isEmpty() && floatAtts.isEmpty()
                && stringAtts.isEmpty()) {
            throw new InvalidDescriptionException();
        }

        record.setIntegerAtt(integerAtts);
        record.setFloatAtt(floatAtts);
        record.setStringAtt(stringAtts);
    }

    /**
     * Determine the location of DBGeo Description files, and unmarshal those
     * into instances of ProductDescription.
     *
     * @param pathManager
     *            The {@link IPathManager} used to look up description files.
     */
    public void setPathManager(IPathManager pathManager) {
        LocalizationFile[] files = pathManager.listStaticFiles(
                "dbgeo/descriptions", new String[] { ".xml" }, true, true);
        ProductDescriptions descriptions = new ProductDescriptions();
        for (LocalizationFile file : files) {
            statusHandler.info(
                    "Loading DBGeo data description(s) from " + file.getPath());
            try (InputStream inputStream = file.openInputStream()) {
                ProductDescriptions unmarshalled = JAXB.unmarshal(inputStream,
                        ProductDescriptions.class);
                for (ProductDescription description : unmarshalled
                        .getDescriptions()) {
                    try {
                        description.validateDescription();
                        descriptions.addDescription(description);
                    } catch (InvalidDescriptionException e) {
                        statusHandler
                                .error("Unable to load product description \""
                                        + description.getName() + "\" from "
                                        + file.getPath());
                    }
                }
            } catch (LocalizationException | IOException e) {
                statusHandler
                        .error("Unable to load product description(s) from "
                                + file.getPath());
            }
        }

        this.descriptions = descriptions;
    }

}

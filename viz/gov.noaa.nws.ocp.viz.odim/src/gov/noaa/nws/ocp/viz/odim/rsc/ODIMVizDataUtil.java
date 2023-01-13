/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.odim.rsc;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.format.ParserException;

import com.raytheon.uf.common.colormap.prefs.ColorMapParameters;
import com.raytheon.uf.common.colormap.prefs.DataMappingPreferences.DataMappingEntry;
import com.raytheon.uf.common.dataplugin.radar.RadarRecord;
import com.raytheon.uf.common.dataplugin.radar.util.RadarUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.units.PiecewisePixel;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.radar.util.DataUtilities;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;

import tech.units.indriya.AbstractUnit;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.function.AbstractConverter;

/**
 * Provides data calculation and conversion methods for the ODIM plugin.
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
public class ODIMVizDataUtil {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ODIMVizDataUtil.class);

    /**
     * Storm motion values associated with an ODIMRecord
     */
    public static class SRMValues {
        private double speed = 0;

        private double direction = 0;

        private Date movement = null;

        private String sourceName = null;

        public SRMValues(double srmSpeed, double srmDirection, Date srmMovement,
                String srmSourceName) {
            super();
            this.speed = srmSpeed;
            this.direction = srmDirection;
            this.movement = srmMovement;
            this.sourceName = srmSourceName;
        }

        public double getSpeed() {
            return speed;
        }

        public double getDirection() {
            return direction;
        }

        public Date getMovement() {
            return movement;
        }

        public String getSourceName() {
            return sourceName;
        }
    }

    private ODIMVizDataUtil() {
        // static methods only
    }

    /**
     * Convert raw encoded data values to colormapped image values. Also zeroes
     * out the first bin of the raw data.
     *
     * @param rec
     * @param unitConverter
     *            converter from raw encoded values to image values
     * @param colorMapParameters
     * @param imageFlagValues
     *            a two-element array containing the image values to display for
     *            "missing" and "no data"
     * @return colormapped image data
     */
    public static Buffer convertAndUpdateRecordData(ODIMRecord rec,
            UnitConverter unitConverter, ColorMapParameters colorMapParameters,
            int[] imageFlagValues) {
        if (rec.getRawData() != null) {
            return ByteBuffer.wrap(convertAndUpdateByteData(rec, unitConverter,
                    colorMapParameters, imageFlagValues));
        } else {
            return ShortBuffer.wrap(convertAndUpdateShortData(rec,
                    unitConverter, colorMapParameters, imageFlagValues));
        }
    }

    /**
     * Using raw encoded data values and the given storm motion values,
     * calculate SRM and convert to colormapped image values. Also zeroes out
     * the first bin of the raw data.
     *
     * @param rec
     * @param unitConverter
     *            converter from raw encoded values to image values
     * @param colorMapParameters
     * @param imageFlagValues
     *            a two-element array containing the image values to display for
     *            "missing" and "no data"
     * @param srmValues
     * @return colormapped image data
     */
    public static Buffer convertAndUpdateRecordDataSRM(ODIMRecord rec,
            UnitConverter unitConverter, ColorMapParameters colorMapParameters,
            int[] imageFlagValues, SRMValues srmValues) {
        if (rec.getRawData() != null) {
            return ByteBuffer
                    .wrap(convertAndUpdateSRMByteData(rec, unitConverter,
                            colorMapParameters, imageFlagValues, srmValues));
        } else {
            throw new UnsupportedOperationException(
                    "16-bit SRM not supported.");
        }
    }

    protected static byte[] convertAndUpdateByteData(ODIMRecord rec,
            UnitConverter unitConverter, ColorMapParameters colorMapParameters,
            int[] imageFlagValues) {
        byte[] table = createConversionTable(rec, unitConverter,
                colorMapParameters, imageFlagValues);
        byte[] recData = rec.getRawData();
        byte[] imageData = new byte[rec.getNumBins() * rec.getNumRadials()];
        int i = 0;
        for (int h = 0; h < rec.getNumRadials(); ++h) {
            for (int w = 0; w < rec.getNumBins(); ++w) {
                if (!createCircle(recData, h, w, i)) {
                    imageData[i] = table[getRawIntDataValue(rec, h, w)];
                }
                ++i;
            }
        }
        return imageData;
    }

    protected static byte[] convertAndUpdateSRMByteData(ODIMRecord rec,
            UnitConverter unitConverter, ColorMapParameters colorMapParameters,
            int[] imageFlagValues, SRMValues srmValues) {
        byte[] srmData = calculateSRM(rec, srmValues);
        byte[] table = createConversionTable(rec, unitConverter,
                colorMapParameters, imageFlagValues);
        byte[] recData = rec.getRawData();
        byte[] imageData = new byte[rec.getNumBins() * rec.getNumRadials()];
        int numRadials = rec.getNumRadials();
        int numBins = rec.getNumBins();
        int i = 0;
        for (int h = 0; h < numRadials; ++h) {
            for (int w = 0; w < numBins; ++w) {
                if (!createCircle(recData, h, w, i)) {
                    int value = srmData[(h * numBins) + w] & 0xFF;
                    imageData[i] = table[value];
                }
                ++i;
            }
        }
        return imageData;
    }

    protected static short[] convertAndUpdateShortData(ODIMRecord rec,
            UnitConverter unitConverter, ColorMapParameters colorMapParameters,
            int[] imageFlagValues) {
        Converter converter = new Converter(rec, unitConverter,
                colorMapParameters, imageFlagValues);
        short[] recData = rec.getRawShortData();
        short[] imageData = new short[rec.getNumBins() * rec.getNumRadials()];
        int i = 0;
        for (int h = 0; h < rec.getNumRadials(); ++h) {
            for (int w = 0; w < rec.getNumBins(); ++w) {
                if (!createCircle(recData, h, w, i)) {
                    int converted = converter
                            .convertValue(getRawIntDataValue(rec, h, w));
                    imageData[i] = converted != -1 ? (short) converted : 0;
                }
                ++i;
            }
        }
        return imageData;
    }

    /**
     * Determine colormapped image values to use for "missing" and "no data"
     * values of the given ODIM record.
     *
     * "missing" is always mapped to zero. "no data" is mapped to the color map
     * index that is labeled "RF" or "NO DATA" if present. Otherwise, it is
     * mapped to zero.
     *
     * Currently, the given ODIM record does not affect the result.
     *
     * @param rec
     * @param params
     * @return a two-element array containing the image values to display for
     *         "missing" and "no data"
     */
    public static int[] getImageFlagValues(ODIMRecord rec,
            ColorMapParameters params) {
        int[] flags = new int[2];
        flags[0] = 0;
        if (params.getDataMapping() != null) {
            for (DataMappingEntry entry : params.getDataMapping()
                    .getEntries()) {
                if ("RF".equals(entry.getSample())
                        || "NO DATA".equals(entry.getSample())) {
                    flags[1] = entry.getPixelValue().intValue();
                    break;
                }
            }
        }
        return flags;
    }

    /**
     * Creates a UnitCoverter to convert from raw encoded ODIM data values to
     * colormapped image values. Based on
     * RadarImageResource.createConversionTable.
     *
     * @param rec
     * @param radarRecord
     * @param params
     * @return
     * @throws VizException
     *             if units are unconvertible.
     */
    public static UnitConverter getConverter(ODIMRecord rec,
            RadarRecord radarRecord, ColorMapParameters params)
            throws VizException {
        UnitConverter dataToImage = null;
        Unit<?> dataUnit = getRecordDataUnit(rec);

        if (dataUnit != null && !dataUnit.equals(params.getDataUnit())) {
            Unit<?> imageUnit = params.getColorMapUnit();
            try {
                if (imageUnit != null && dataUnit.isCompatible(imageUnit)) {
                    dataToImage = dataUnit.getConverterToAny(imageUnit);
                } else if (imageUnit != null) {
                    dataUnit = DataUtilities.getDataUnit(radarRecord, "");
                    if (dataUnit.isCompatible(imageUnit)) {
                        dataToImage = dataUnit.getConverterToAny(imageUnit);
                    }
                }
            } catch (IncommensurableException | UnconvertibleException e) {
                throw new VizException(e);
            }
        } else {
            dataToImage = params.getDataToImageConverter();
        }
        if (dataToImage == null) {
            dataToImage = AbstractConverter.IDENTITY;
        }
        return dataToImage;
    }

    /**
     * Get the actual unit of the raw encoded values taking the scale, offset,
     * and flag values into account.
     *
     * @param rec
     * @return a unit object that describes the actual byte values.
     */
    public static Unit<?> getRecordDataUnit(ODIMRecord rec) {
        double scale = rec.getEncodingScale();
        double offset = rec.getEncodingOffset();
        Integer missing = rec.getMissingValue();
        Integer noData = rec.getNoDataValue();
        ArrayList<Integer> pix = new ArrayList<>(3);
        Integer flagA = null;
        Integer flagB = null;
        if (missing != null && noData != null
                && missing.intValue() != noData.intValue()) {
            flagA = Math.min(missing, noData);
            flagB = Math.max(missing, noData);
        } else if (missing != null) {
            flagA = missing;
        } else if (noData != null) {
            flagA = noData;
        }
        if (flagA == null) {
            pix.add(0);
            pix.add(rec.getNumLevels());
        } else {
            if (flagA > 0) {
                pix.add(0);
                pix.add(flagA);
            }
            if (flagB != null && flagA.intValue() + 1 != flagB.intValue()) {
                int s = (flagA.intValue() + 1);
                pix.add(s);
                pix.add(flagB);
            }
            int s = flagB != null ? flagB + 1 : flagA + 1;
            if (s < rec.getNumLevels()) {
                pix.add(s);
                pix.add(rec.getNumLevels());
            }
        }

        return new PiecewisePixel(getUnitObject(rec),
                pix.stream().mapToDouble(Double::valueOf).toArray(),
                pix.stream().mapToDouble(v -> offset + v * scale).toArray());
    }

    /**
     * Gets the parameter unit as a javax.measure.Unit<?> object. If the
     * parameter unit string cannot be successfully converted to a
     * javax.measure.Unit<?> object, AbstractUnit.ONE is returned
     *
     * Copied from RadarRecord.getUnitObject.
     *
     * @return The parameter unit as a javax.measure.Unit<?> object
     */
    public static Unit<?> getUnitObject(ODIMRecord rec) {
        Unit<?> retVal = AbstractUnit.ONE;
        String unit = rec.getUnit();

        if (unit != null) {
            try {
                retVal = SimpleUnitFormat
                        .getInstance(SimpleUnitFormat.Flavor.ASCII)
                        .parseProductUnit(unit, new ParsePosition(0));
            } catch (IllegalArgumentException | ParserException e) {
                // Unable to parse unit string
                statusHandler.warn(String.format(
                        "Unable to parse unit string: \"%s\". Treating as unitless.",
                        unit), e);
                retVal = AbstractUnit.ONE;
            }
        }

        return retVal;
    }

    /**
     * Calculates the 8-bit SRM product from 8-bit base velocity. SRM is found
     * by subtracting the overall wind direction from the radial data. The
     * resulting data would show the velocity data as if the wind was
     * stationary.
     *
     * Copied from RadarRecordUtil.calculateSRM8 and modified.
     *
     * @param record
     * @param direction
     * @param speed
     * @param numberOfPixels
     * @param numberOfGates
     * @return
     */
    public static byte[] calculateSRM(ODIMRecord rec, SRMValues srmValues) {
        byte[] srmData = null;
        if (rec.getRawData() != null) {
            int deltaInt = 0;
            byte[] radialData = rec.getRawData();

            // Need to make copy
            srmData = new byte[rec.getRawData().length];

            int currBinPtr = 0;
            int maxBin = 0;

            Integer missingValue = rec.getMissingValue();
            Integer noDataValue = rec.getNoDataValue();

            // Loop through each bin, in each radial
            for (int currRadial = 0; currRadial < rec
                    .getNumRadials(); currRadial++) {
                // If it is moving, find the Integer delta value
                if (srmValues.speed != 0) {
                    // Get the delta value for the current radial in m/s...
                    double delta = srmValues.speed
                            * Math.cos(Math.PI / 180
                                    * (srmValues.direction
                                            - rec.getAngleData()[currRadial]))
                            / 1.944;
                    /*
                     * ...then convert to a delta that can be applied to encoded
                     * values.
                     */
                    delta /= rec.getEncodingScale();

                    // Round delta to next higher whole number
                    if (delta < 0) {
                        deltaInt = (int) (delta - 0.5);
                    } else {
                        deltaInt = (int) (delta + 0.5);
                    }
                }

                maxBin += rec.getNumBins();

                if (deltaInt == 0) {
                    /*
                     * If there is not difference, just assign the original
                     * value
                     */
                    for (int currBin = currBinPtr; currBin < maxBin; currBin++) {
                        srmData[currBin] = radialData[currBin];
                    }
                } else {
                    for (int currBin = currBinPtr; currBin < maxBin; currBin++) {
                        byte v = radialData[currBin];
                        if ((missingValue == null || v != missingValue)
                                && (noDataValue == null || v != missingValue)) {
                            srmData[currBin] = (byte) (v + deltaInt);
                        } else {
                            srmData[currBin] = v;
                        }
                    }
                }

                currBinPtr = maxBin;
            }
        }
        return srmData;
    }

    /**
     * Calculate the extent of the given radar data
     *
     * @param rec
     * @return the extent
     */
    public static double calculateExtent(ODIMRecord rec) {
        return RadarUtil.calculateExtent(rec.getNumBins(), null,
                rec.getGateResolution(), rec.getTrueElevationAngle(),
                getRadarFormat(rec));
    }

    /**
     * Determine the format (as defined by the radar plugin) of the given
     * record.
     *
     * Currently always returns "Radial".
     *
     * @param rec
     * @return
     */
    public static String getRadarFormat(ODIMRecord rec) {
        return "Radial";
    }

    private static class Converter {
        UnitConverter unitConverter;

        private Unit<?> dataUnit;

        private ColorMapParameters colorMapParameters;

        private int[] imageFlagValues;

        private Integer missing;

        private Integer noData;

        private Converter(ODIMRecord rec, UnitConverter unitConverter,
                ColorMapParameters colorMapParameters, int[] imageFlagValues) {
            this.unitConverter = unitConverter;
            this.dataUnit = ODIMVizDataUtil.getRecordDataUnit(rec);
            this.colorMapParameters = colorMapParameters;
            this.missing = rec.getMissingValue();
            this.noData = rec.getNoDataValue();
            this.imageFlagValues = imageFlagValues;
        }

        /*
         * Converts ODIM encoded values to colormap index values. Based on
         * RadarImageResource.createConversionTable(), but uses the special
         * "no data" / "not present" values as defined in the original ODIM
         * data.
         */
        private final int convertValue(int value) {
            if (missing != null && value == missing) {
                return imageFlagValues[0];
            } else if (noData != null && value == noData) {
                return imageFlagValues[1];
            } else {
                double image = unitConverter.convert(value);
                if (!Double.isNaN(image)) {
                    return (int) Math.round(image);
                } else {
                    double d;
                    try {
                        d = dataUnit
                                .getConverterToAny(
                                        colorMapParameters.getDisplayUnit())
                                .convert(value);
                    } catch (IncommensurableException // NOSONAR
                            | UnconvertibleException e) {
                        return imageFlagValues[1];
                    }

                    UnitConverter imageToDisplayConverter = colorMapParameters
                            .getColorMapToDisplayConverter();
                    int ret = imageFlagValues[1];
                    for (int j = 0; j < 256; j++) {
                        double disp = imageToDisplayConverter.convert(j);
                        if (Double.isNaN(disp)) {
                            continue;
                        }
                        if (d < disp) {
                            /*
                             * Map data values smaller than the colormap min to
                             * 0, which should be no data.
                             *
                             * table[i] = (byte) 0;
                             *
                             * If we want small values to appear as the lowest
                             * data value than do this next line instead This
                             * was changed for the DUA product so differences
                             * less than -5 get mapped to a data value.
                             */
                            return j;
                        }
                        if (d > disp) {
                            /*
                             * map data values larger than the colormap max to
                             * the highest value
                             */
                            ret = j;
                        }

                    }
                    return ret;
                }
            }
        }

    }

    private static byte[] createConversionTable(ODIMRecord rec,
            UnitConverter unitConverter, ColorMapParameters colorMapParameters,
            int[] imageFlagValues) {
        Converter converter = new Converter(rec, unitConverter,
                colorMapParameters, imageFlagValues);
        byte[] table = new byte[rec.getNumLevels()];
        for (int i = 0; i < table.length; i++) {
            table[i] = (byte) converter.convertValue(i);
        }
        return table;
    }

    private static boolean createCircle(byte[] recData, int h, int w, int i) {
        if (w == 0) {
            if (recData != null) {
                recData[i] = (byte) 0;
            }
            return true;
        }
        return false;
    }

    private static boolean createCircle(short[] recData, int h, int w, int i) {
        if (w == 0) {
            if (recData != null) {
                recData[i] = (byte) 0;
            }
            return true;
        }
        return false;
    }

    private static int getRawIntDataValue(ODIMRecord rec, int radial, int bin) {
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

}

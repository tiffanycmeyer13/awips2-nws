/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.raytheon.edex.exception.DecoderException;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.EDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * EDeckParser is a csv data parser class for ATCF E deck data
 *
 * <pre>
 *
 * EDeckParser
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Aug 23, 2018 #53502     dfriedman   Modify for Hibernate implementation.
 * Jun 16, 2020 #79546     dfriedman   Fix various parsing issues.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class EDeckParser extends AbstractAtcfParser {

    private static final String TRACK_PROBABILITY_FORMAT = "TR";

    private static final String TRACK_PROBABILITY_FORMAT_OLD = "03";

    private static final String INTENSITY_PROBABILITY_FORMAT = "IN";

    private static final String RAPID_INTENSIFICATION_PROBABILITY_FORMAT = "RI";

    private static final String WIND_RADII_PROBABILITY_FORMAT = "WR";

    private static final String PRESSURE_PROBABILITY_FORMAT = "PR";

    private static final String TC_GENESIS_PROBABILITY_FORMAT = "GN";

    private static final String TC_GENESIS_SHAPE_PROBABILITY_FORMAT = "GS";

    private static final String GENESIS_SHAPE_TYPE_POLYGON = "PLY";

    private static final Set<String> VALID_PROBABILITY_FORMATS = new HashSet<>(
            Arrays.asList("TR", "03", "IN", "RI", "WR", "PR", "GN", "GS"));

    @Override
    public AbstractAtcfRecord[] parseStream(InputStream inputStream,
            Storm thisStorm) throws DecoderException {
        return defaultParseStream(inputStream, thisStorm, EDeckRecord.class);
    }

    /**
     *
     * @param fields
     * @param index
     * @return
     */
    private boolean isValid(String[] fields, int index) {
        return (index < fields.length && !fields[index].isEmpty());
    }

    /**
     * processFields parse one a deck record
     *
     * @param theBulletin
     * @return
     */
    @Override
    protected EDeckRecord processFields(String theBulletin, int year) {
        final Integer MAX_NUMBER_OF_FIELDS = 80;
        EDeckRecord drecord = new EDeckRecord();

        String[] atcfField = SPLIT.split(theBulletin, MAX_NUMBER_OF_FIELDS);

        int fieldIdx = -1;

        // Parse 9 common elements
        /*
         * Fields up to and including the technique name are required. If
         * missing or invalid, will throw a RuntimeException.
         */
        ++fieldIdx;
        drecord.setBasin(validateBasin(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setCycloneNum(Integer.parseInt(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setRefTime(parseDtgAsDate(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setProbFormat(processProbFormat(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setTechnique(atcfField[fieldIdx]);
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setFcstHour(Integer.parseInt(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setClat(processLatLon(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setClon(processLatLon(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbability(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // Parse rest elements based on the probability format (4th column)
        if (drecord.getProbFormat().equalsIgnoreCase(TRACK_PROBABILITY_FORMAT)
                || drecord.getProbFormat()
                        .equalsIgnoreCase(TRACK_PROBABILITY_FORMAT_OLD)) {
            processTrackFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getProbFormat()
                .equalsIgnoreCase(INTENSITY_PROBABILITY_FORMAT)) {
            processIntensityFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getProbFormat()
                .equalsIgnoreCase(RAPID_INTENSIFICATION_PROBABILITY_FORMAT)) {
            processRapidIntensificationFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getProbFormat()
                .equalsIgnoreCase(WIND_RADII_PROBABILITY_FORMAT)) {
            processWindRadiiFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getProbFormat()
                .equalsIgnoreCase(PRESSURE_PROBABILITY_FORMAT)) {
            processPressureFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getProbFormat()
                .equalsIgnoreCase(TC_GENESIS_PROBABILITY_FORMAT)) {
            processGenesisFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getProbFormat()
                .equalsIgnoreCase(TC_GENESIS_SHAPE_PROBABILITY_FORMAT)) {
            processGenesisShapeFields(atcfField, fieldIdx, drecord);
        }

        drecord.setYear(year);

        return drecord;

    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processTrackFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setDevelopmentLevel(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCrossTrackDirection(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadWindQuad(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCrossTrackRadius(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setAlongTrackRadius(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCrossTrackBias(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setAlongTrackBias(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processIntensityFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setDevelopmentLevel(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setHalfRange(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processRapidIntensificationFields(String[] atcfField,
            int fieldIdx, EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setVFinal((Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setForecaster(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRiStartTAU(Integer.parseInt(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRiStopTAU(Integer.parseInt(atcfField[fieldIdx]));
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processWindRadiiFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[9])));
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processPressureFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processGenesisFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setForecaster(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setGenOrDis(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEventDateTimeGroup(atcfField[fieldIdx]);// TODO calendar
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setStormId2(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMinutes(Integer.parseInt(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setGenesisNum(Integer.parseInt(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setUndefined(atcfField[fieldIdx]);
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processGenesisShapeFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProbabilityItem(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setForecaster(atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            // Calendar tcfaManopDtg = processWarnTime();
            drecord.setTcfaManopDtg(/* tcfaManopDtg */ atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            // Calendar tcfaMsgDtg = processWarnTime(atcfField[fieldIdx]);
            drecord.setTcfaMsgDtg(/* tcfaMsgDtg */ atcfField[fieldIdx]);
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTcfaWtNum(Integer.parseInt(atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setShapeType((atcfField[fieldIdx]));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEllipseAngle(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEllipseRCross(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEllipseRAlong(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setBox1LatNS(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setBox1LonEW(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setBox2LatNS(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setBox2LonEW(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTcfaRadius(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }
        // Assume PLY (Polygon points are lat, lon, lat, lon ... format
        if (drecord.getShapeType().equals(GENESIS_SHAPE_TYPE_POLYGON)) {
            processPolygonFields(atcfField, fieldIdx, drecord);
        }
    }

    /**
     *
     * @param atcfField
     * @param fieldIdx
     * @param drecord
     */
    private void processPolygonFields(String[] atcfField, int fieldIdx,
            EDeckRecord drecord) {
        String[] pointFields = Arrays.copyOf(atcfField, fieldIdx);
        drecord.setPolygonPointsText(String.join(", ", pointFields));
        /*
         * TODO: Determine in-memory represenation Coordinate[] coords = new
         * Coordinate[(atcfField.length - fieldIdx) / 2]; int ci = 0; float lat,
         * lon; while (atcfField.length - fieldIdx > 2) { lat =
         * processLatLon(atcfField[++fieldIdx]); lon =
         * processLatLon(atcfField[++fieldIdx]); coords[ci++] = new
         * Coordinate(lon, lat); } if (coords.length > 1) { coords[coords.length
         * - 1] = new Coordinate(coords[0]); } drecord.setGenesisPolygon(new
         * GeometryFactory().createPolygon(coords));
         */
    }

    private static String processProbFormat(String s) {
        if (VALID_PROBABILITY_FORMATS.contains(s)) {
            return s;
        } else {
            throw new IllegalArgumentException(
                    "invalid e-deck format \"" + s + "\"");
        }
    }

}

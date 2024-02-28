/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.parser;

import java.io.InputStream;

import com.raytheon.edex.exception.DecoderException;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * FDeckParser is a csv data parser class for ATCF F deck data
 *
 * <pre>
 *
 * EDeckParser
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 27, 2018            pwang       Initial creation
 * Aug 23, 2018 53502      dfriedman   Modify for Hibernate implementation.
 * Sep 12, 2019 68523      jwu         Include minutes in DTG/reftime.
 * Jun 16, 2020 79546      dfriedman   Fix various parsing issues.
 * May 17, 2021 91567      jwu         Move processWarnTimeMM() AbstractAtcfParser.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class FDeckParser extends AbstractAtcfParser {

    private static final String SUBJECTIVE_DVORAK_FIX_FORMAT = "10";

    private static final String OBJECTIVE_DVORAK_FIX_FORMAT = "20";

    private static final String MICROWAVE_FIX_FORMAT = "30";

    private static final String SCATTEROMETER_FIX_FORMAT = "31";

    private static final String RADAR_FIX_FORMAT = "40";

    private static final String AIRCRAFT_FIX_FORMAT = "50";

    private static final String DROPSONDE_FIX_FORMAT = "60";

    private static final String ANALYSIS_FIX_FORMAT = "70";

    @Override
    public AbstractAtcfRecord[] parseStream(InputStream inputStream,
            Storm thisStorm) throws DecoderException {
        return defaultParseStream(inputStream, thisStorm, FDeckRecord.class);
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
     * @param theBulletin,
     *            year
     * @return
     */
    @Override
    protected FDeckRecord processFields(String theBulletin, int year) {
        final Integer MAX_NUMBER_OF_FIELDS = 80;
        FDeckRecord drecord = new FDeckRecord();
        String[] atcfField = SPLIT.split(theBulletin, MAX_NUMBER_OF_FIELDS);

        int fieldIdx = -1;

        // Parse 32 common elements for all fix types of F-Deck
        /*
         * Fields up to and including the fix type are required. If missing or
         * invalid, will throw a RuntimeException.
         */
        ++fieldIdx;
        drecord.setBasin(validateBasin(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setCycloneNum(Integer.parseInt(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setRefTime(parseDtgMmAsDate(atcfField[fieldIdx]));
        ++fieldIdx;
        drecord.setFixFormat(atcfField[fieldIdx]);
        ++fieldIdx;
        drecord.setFixType(atcfField[fieldIdx]);

        // 6th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCenterOrIntensity(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setFlaggedIndicator(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setClat(processLatLon4(atcfField[fieldIdx]));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setClon(processLatLon4(atcfField[fieldIdx]));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setObHeight((Integer.parseInt(atcfField[fieldIdx])));
        }

        // 11th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setPositionConfidence(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWindMax((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWindMaxConfidence(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMslp((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setPressureConfidence(atcfField[fieldIdx]);
        }

        // 16th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setPressureDerivation(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float radWind = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setRadWind(radWind);
            drecord.setRadiusOfWindIntensity(radWind);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            String windCode = atcfField[fieldIdx];
            drecord.setRadWindQuad(windCode);
            drecord.setWindCode(windCode);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q1RadWind = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad1WindRad(q1RadWind);
            drecord.setWindRad1(q1RadWind);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q2RadWind = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad2WindRad(q2RadWind);
            drecord.setWindRad2(q2RadWind);
        }

        // 21st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q3RadWind = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad3WindRad(q3RadWind);
            drecord.setWindRad3(q3RadWind);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q4RadWind = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad4WindRad(q4RadWind);
            drecord.setWindRad4(q4RadWind);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod1(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod2(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod3(atcfField[fieldIdx]);
        }

        // 26th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod4(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadiiConfidence(atcfField[fieldIdx]);
            drecord.setMicrowaveRadiiConfidence(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadiusOfMaximumWind(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeDiameterNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSubRegion(atcfField[fieldIdx]);
        }

        // 31st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setFixSite(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setInitials(atcfField[fieldIdx]);
        }

        // Parse rest elements based on the fixFormat
        if (drecord.getFixFormat()
                .equalsIgnoreCase(SUBJECTIVE_DVORAK_FIX_FORMAT)) {
            processSubjectiveDvorakFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat()
                .equalsIgnoreCase(OBJECTIVE_DVORAK_FIX_FORMAT)) {
            processObjectiveDvorakFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat()
                .equalsIgnoreCase(MICROWAVE_FIX_FORMAT)) {
            processMicrowaveFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat()
                .equalsIgnoreCase(SCATTEROMETER_FIX_FORMAT)) {
            // Same as MICROWAVE with fixtype as "ASCT"
            processMicrowaveFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat().equalsIgnoreCase(RADAR_FIX_FORMAT)) {
            processRadarFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat()
                .equalsIgnoreCase(AIRCRAFT_FIX_FORMAT)) {
            processAircraftFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat()
                .equalsIgnoreCase(DROPSONDE_FIX_FORMAT)) {
            processDropsondeFields(atcfField, fieldIdx, drecord);
        } else if (drecord.getFixFormat()
                .equalsIgnoreCase(ANALYSIS_FIX_FORMAT)) {
            processAnalysisFields(atcfField, fieldIdx, drecord);
        }

        drecord.setYear(year);

        return drecord;

    }

    /*
     * Subjective Dvorak Fields (33rd to 41st)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processSubjectiveDvorakFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        // 33rd
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSensorType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setPcnCode(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setDvorakCodeLongTermTrend(atcfField[fieldIdx]);
        }

        // 36th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setDvorakCodeShortTermTrend(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCi24hourForecast(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSatelliteType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCenterType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTropicalIndicator(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx]);
        }
    }

    /*
     * Objective Dvorak Fields (33rd to 46th)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processObjectiveDvorakFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        // 33rd
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSensorType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCiNum((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setCiConfidence(atcfField[fieldIdx]);
        }

        // 36th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.settNumAverage(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.settNumAveragingTimePeriod(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.settNumAveragingDerivation(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.settNumRaw(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTempEye((Integer.parseInt(atcfField[fieldIdx])));
        }

        // 41st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTempCloudSurroundingEye(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSceneType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setAlgorithm(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSatelliteType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTropicalIndicator(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx]);
        }
    }

    /*
     * Microwave (including Scatterometer) Fields (33rd to 57th)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processMicrowaveFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        // 33rd
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRainFlag(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRainRate((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setProcess(atcfField[fieldIdx]);
        }

        // 36th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWaveHeight(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTempPassiveMicrowave(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSlpRaw((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSlpRetrieved(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxSeas((Integer.parseInt(atcfField[fieldIdx])));
        }

        // 41st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSatelliteType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float radWind = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setRadWind(radWind);
            drecord.setRadiusOfWindIntensity(radWind);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            String windCode = atcfField[fieldIdx];
            drecord.setRadWindQuad(windCode);
            drecord.setWindCode(windCode);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q1WindRad = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad1WindRad(q1WindRad);
            drecord.setWindRad1(q1WindRad);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q2WindRad = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad2WindRad(q2WindRad);
            drecord.setWindRad2(q2WindRad);
        }

        // 46th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q3WindRad = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad3WindRad(q3WindRad);
            drecord.setWindRad3(q3WindRad);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            float q4WindRad = (Integer.parseInt(atcfField[fieldIdx]));
            drecord.setQuad4WindRad(q4WindRad);
            drecord.setWindRad4(q4WindRad);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWindRad5((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWindRad6((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWindRad7((Integer.parseInt(atcfField[fieldIdx])));
        }

        // 51st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setWindRad8((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod1(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod2(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod3(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod4(atcfField[fieldIdx]);
        }

        // 56th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod5(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod6(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod7(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadMod8(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadiiConfidence(atcfField[fieldIdx]);
            drecord.setMicrowaveRadiiConfidence(atcfField[fieldIdx]);
        }

        // 61st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx]);
        }
    }

    /*
     * Radar Fields (33rd to 54th)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processRadarFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        // 33rd
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadarType(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadarFormat(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadobCode(atcfField[fieldIdx]);
        }

        // 36th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeShape(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setPercentOfEyewallObserved(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSpiralOverlayDegrees(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadarSitePosLat(processLatLon4(atcfField[fieldIdx]));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRadarSitePosLon(processLatLon4(atcfField[fieldIdx]));
        }

        // 41st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setInboundMaxWindSpeed(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setInboundMaxWindAzimuth(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setInboundMaxWindRangeNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setInboundMaxWindElevationFeet(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setOutboundMaxWindSpeed(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // 46th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setOutboundMaxWindAzimuth(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setOutboundMaxWindRangeNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setOutboundMaxWindElevationFeet(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxCloudHeightFeet(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxRainAccumulation(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // 51st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRainAccumulationTimeInterval(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRainAccumulationLat(processLatLon4(atcfField[fieldIdx]));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setRainAccumulationLon(processLatLon4(atcfField[fieldIdx]));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx]);
        }

    }

    /*
     * AircraftFields (33rd to 56th)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processAircraftFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        // 33rd
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setFlightLevel100Feet(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setFlightLevelMillibars(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setFlightLevelMinimumHeightMeters(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // 36th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxSurfaceWindInboundLegIntensity(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxSurfaceWindInboundLegBearing(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxSurfaceWindInboundLegRangeNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxFLWindInboundDirection(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxFLWindInboundIntensity(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // 41st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxFLWindInboundBearing(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMaxFLWindInboundRangeNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMslp((Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTempOutsideEye(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setTempInsideEye(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // 46th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setDewPointTemp(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSeaSurfaceTemp(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeCharacterOrWallCloudThickness(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeShape(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeOrientation(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        // 51st
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeDiameterNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEyeShortAxis(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setAccuracyNavigational(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setAccuracyMeteorological(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setMissionNumber(atcfField[fieldIdx]);
        }

        // 56th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx]);
        }

    }

    /*
     * Dropsonde Fields (33rd to 37th)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processDropsondeFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSondeEnvironment(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setHeightMidpointLowest150m(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSpeedMeanWindLowest150mKt(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSpeedMeanWind0to500mKt(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx]);
        }
    }

    /*
     * Analysis/Synoptic Fields (33rd to 39th)
     *
     * @param atcfField Array of fields
     *
     * @param fieldIdx Starting index
     *
     * @param drecord Record to be set.
     */
    private void processAnalysisFields(String[] atcfField, int fieldIdx,
            FDeckRecord drecord) {

        // 33rd
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setAnalysisInitials(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setStartTime(parseDtgMmAsDate(atcfField[fieldIdx]));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setEndTime(parseDtgMmAsDate(atcfField[fieldIdx]));
        }

        // 36th
        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setDistanceToNearestDataNM(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setSeaSurfaceTemp(
                    (Integer.parseInt(atcfField[fieldIdx])));
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setObservationSources(atcfField[fieldIdx]);
        }

        ++fieldIdx;
        if (isValid(atcfField, fieldIdx)) {
            drecord.setComments(atcfField[fieldIdx].trim());
        }
    }

}

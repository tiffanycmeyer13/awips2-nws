/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.atcf.configuration.GeographyPoint;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ReadAdvisoryFileRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.WriteAdvisoryFilesRequest;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisoryInfo;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.AdvisorySummary;
import gov.noaa.nws.ocp.common.dataplugin.atcf.templateobject.IcaoDataInfo;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;

/**
 * Utility class for composing ATCF advisories.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 19, 2020 82721      jwu         Initial creation
 * Oct 29, 2020 81820      wpaintsil   Add more utility methods.
 * Nov 12, 2020 84446      dfriedman   Refactor error handling
 * Jan 26, 2021 86746      jwu         Add more utility methods.
 * Feb 12, 2021 87783      jwu         Add more utility methods.
 * Mar 22, 2021 88518      dfriedman   Rework product headers.
 * Jun 04, 2021 91765      jwu         Add methods to get sections in TCP.
 * Jul 08, 2021 93998      jwu         Fix issues in getting sections in TCP.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AdvisoryUtil {

    /**
     * File extension, starting/end flag for a previous TCD discussion.
     */
    protected static final String PREVIOUS_TCD_DISCUSS = ".discus.tmp";

    protected static final int PREVIOUS_TCD_DISCUSS_START = 7;

    protected static final String PREVIOUS_TCD_DISCUSS_END = "FORECAST POSITIONS AND MAX WINDS";

    /**
     * Max number of lines allowed to in an advisory.
     */
    protected static final int MAX_ADV_LINES = 500;

    /**
     * Max number of lines allowed to load from stormId.hdline.
     */
    protected static final int MAX_HEAD_LINES = 10;

    /**
     * Max number of lines allowed to load from stormId.hazards.
     */
    protected static final int MAX_HAZARDS_LINES = 100;

    /**
     * Max number of lines allowed to load from stormId.warn.
     */
    protected static final int MAX_WARNING_LINES = 160;

    /**
     * Default warning if no warning exists.
     */
    protected static final String DEFAULT_WARNINGS = "There are no coastal watches or warnings in effect.";

    /**
     * Max number of lines allowed to load from stormId.discus.
     */
    protected static final int MAX_DISCUSS_LINES = 200;

    /**
     * Max number of lines allowed to load from stormId.summary.
     */
    protected static final int MAX_SUMMARY_LINES = 200;

    /**
     * Starting/ending indicators for header line section in TCP.
     */
    protected static final String HEADERLINE_START = "...";

    protected static final String HEADERLINE_END = "SUMMARY OF";

    /**
     * Starting/ending indicators for watch and warning section in TCP.
     */
    protected static final String WATCH_WARNING_START = "WATCHES AND WARNINGS";

    protected static final String WATCH_WARNING_END = "DISCUSSION AND OUTLOOK";

    /**
     * Starting/ending indicators for discuss and outlook section in TCP.
     */
    protected static final String DISCUSSION_START = "DISCUSSION AND OUTLOOK";

    protected static final String DISCUSSION_END = "HAZARDS AFFECTING LAND";

    /**
     * Starting/ending indicators for hazards section in TCP.
     */
    protected static final String HAZARDS_START = "HAZARDS AFFECTING LAND";

    protected static final String HAZARDS_END = "NEXT ADVISORY";

    protected static final String DASH_LINE = "------";

    private static final String READ_FAIL_MSG = "AdvisoryUtil - Failed to read ";

    /**
     * Tropical intensity string
     */
    protected static final String[] TROP_INTENSITY_STRINGS = new String[] {
            "TD", "SD", "TS", "SS", "HU" };

    /**
     * Post-tropical intensity string
     */
    protected static final String[] POST_TROP_INTENSITY_STRINGS = new String[] {
            "PT", "LO", "EX" };

    /**
     * Potential tropical cyclone (PTC) intensity string
     */
    protected static final String[] PTC_INTENSITY_STRINGS = new String[] { "DB",
            "TD", "LO", "EX", "SD" };

    /**
     * Advisory name strings with numbers.
     */
    protected static final String[] ADV_NAME_LETTER_STRINGS = new String[] {
            "ONE", "TWO", "THR", "FOU", "FIV", "SIX", "SEV", "EIG", "NIN",
            "ELE", "TWE", "THI", "FIF" };

    /**
     * Max line width ina dvisory.
     */
    protected static final int MAX_WIDTH = 68;

    /**
     * Compass point direction names
     */
    private static final String[][] DIRECTION_NAMES = { { "north", "N" },
            { "north-northeast", "NNE" }, { "northeast", "NE" },
            { "east-northeast", "ENE" }, { "east", "E" },
            { "east-southeast", "ESE" }, { "southeast", "SE" },
            { "south-southeast", "SSE" }, { "south", "S" },
            { "south-southwest", "SSW" }, { "southwest", "SW" },
            { "west-southwest", "WSW" }, { "west", "W" },
            { "west-northwest", "WNW" }, { "northwest", "NW" },
            { "north-northwest", "NNW" } };

    /*
     * Constructor
     */
    private AdvisoryUtil() {
    }

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AdvisoryUtil.class);

    /**
     * Read an advisory file (i.e. <stormId>.warn/hdline/hazards file)
     *
     * @param storm
     * @param AdvType
     * @return
     */
    public static String readAdvisory(String stormId, AdvisoryType advType) {
        return readAdvisory(stormId, advType, -1);
    }

    /**
     * Read <stormId>.hdline file.
     *
     * @param stormId
     * @return
     */
    public static String readHeadline(String stormId) {
        return readAdvisory(stormId, AdvisoryType.HEADLINE, MAX_HEAD_LINES);
    }

    /**
     * Read <stormId>.hazards file.
     *
     * @param stormId
     * @return
     */
    public static String readHazards(String stormId) {
        return readAdvisory(stormId, AdvisoryType.HAZARDS, MAX_HAZARDS_LINES);
    }

    /**
     * Read <stormId>.discus file - Discuss and Oultook section in TCP/TCP-A.
     *
     * @param stormId
     * @return
     */
    public static String readDiscussion(String stormId) {
        return readAdvisory(stormId, AdvisoryType.TCP_DISCUSS,
                MAX_DISCUSS_LINES);
    }

    /**
     * Read <stormId>.discus file.
     *
     * @param stormId
     * @return
     */
    public static String readSummary(String stormId) {
        return readAdvisory(stormId, AdvisoryType.TCP_SUM, MAX_SUMMARY_LINES);
    }

    /**
     * Read an advisory file for a given storm and advisory type.
     *
     * @param stormId
     * @param AdvType
     *            AdvisoryType
     * @param maxLines
     *            Maximum number of lines to keep (-1 means no limit)
     * @return
     */
    public static String readAdvisory(String stormId, AdvisoryType advType,
            int maxLines) {
        return readAdvisory(stormId, advType.getSuffix(), maxLines);
    }

    /**
     * Read an advisory file for a given storm and specified file extension.
     *
     * @param stormId
     * @param fileExtension
     *            File name extension
     * @param maxLines
     *            Maximum number of lines to keep (-1 means no limit)
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String readAdvisory(String stormId, String fileExtension,
            int maxLines) {
        StringBuilder contents = new StringBuilder();

        boolean ok = false;
        List<String> fileInfo = new ArrayList<>();
        try {
            ReadAdvisoryFileRequest req = new ReadAdvisoryFileRequest(stormId,
                    fileExtension);
            fileInfo = (List<String>) ThriftClient.sendRequest(req);
            ok = true;
        } catch (Exception e) {
            logger.warn(READ_FAIL_MSG + (stormId + fileExtension), e);
        }

        if (ok) {
            if (maxLines <= 0) {
                maxLines = fileInfo.size();
            }

            maxLines = Math.min(maxLines, fileInfo.size());

            for (int ii = 0; ii < maxLines; ii++) {
                contents.append(fileInfo.get(ii)).append("\n");
            }
        }

        return contents.toString();
    }

    /**
     * Read <stormId>.discus.tmp file - a previous TCD and strip of the header
     * part (first 7 lines) and the ending forecast table.
     *
     * @param stormId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String readTCDiscussion(String stormId) {

        StringBuilder contents = new StringBuilder();

        // Fead from stormId.discus.tmp
        boolean ok = false;
        List<String> fileInfo = new ArrayList<>();
        try {
            ReadAdvisoryFileRequest req = new ReadAdvisoryFileRequest(stormId,
                    PREVIOUS_TCD_DISCUSS);
            fileInfo = (List<String>) ThriftClient.sendRequest(req);
            ok = true;
        } catch (Exception e) {
            logger.warn(READ_FAIL_MSG + (stormId + PREVIOUS_TCD_DISCUSS), e);
        }

        // If not found, read from stormId.discus.new
        if (fileInfo.isEmpty()) {
            try {
                ReadAdvisoryFileRequest req = new ReadAdvisoryFileRequest(
                        stormId, AdvisoryType.TCD.getSuffix());
                fileInfo = (List<String>) ThriftClient
                        .sendRequest(req);
                ok = true;
            } catch (Exception e) {
                logger.warn(READ_FAIL_MSG
                        + (stormId + AdvisoryType.TCD.getSuffix()), e);
            }
        }

        if (ok) {
            for (int ii = PREVIOUS_TCD_DISCUSS_START; ii < fileInfo
                    .size(); ii++) {
                String line = fileInfo.get(ii);
                if (!line.isEmpty()
                        && line.trim().length() >= PREVIOUS_TCD_DISCUSS_END
                        .length()
                        && line.trim().startsWith(PREVIOUS_TCD_DISCUSS_END)) {
                    break;
                }

                contents.append(fileInfo.get(ii)).append("\n");
            }
        }

        return contents.toString();

    }

    /**
     * Write an advisory file (i.e. <stormId>.warn/hdline/hazards file)
     *
     * @param stormId
     * @param AdvType
     * @param advisory
     * @return
     */
    public static boolean writeAdvisory(String stormId, AdvisoryType advType,
            String advisory) {
        return writeAdvisories(stormId,
                Collections.singletonMap(advType, advisory));
    }

    /**
     * Write advisories(i.e. <stormId>.warn/hdline/hazards file)
     *
     * @param stormId
     * @param advisories
     *            Map<AdvisoryType, String>
     * @return
     */
    public static boolean writeAdvisories(String stormId,
            Map<AdvisoryType, String> advisories) {
        boolean success = false;

        try {
            WriteAdvisoryFilesRequest advReq = new WriteAdvisoryFilesRequest(
                    stormId, advisories);
            ThriftClient.sendRequest(advReq);
            success = true;
        } catch (Exception e) {
            logger.warn("AdvisoryUtil - Failed to write advisories for .... "
                    + stormId, e);
        }

        return success;
    }

    /**
     * Read <stormId>.warn file.
     *
     * @param stormId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String readWarnings(String stormId) {

        String warnings = DEFAULT_WARNINGS;
        String fileExtension = AdvisoryType.WARNINGS.getSuffix();
        List<String> warningLines = new ArrayList<>();
        try {
            ReadAdvisoryFileRequest req = new ReadAdvisoryFileRequest(stormId,
                    fileExtension);
            warningLines = (List<String>) ThriftClient
                    .sendRequest(req);
        } catch (Exception e) {
            logger.warn("AdvisoryUtil - No warning read for .... "
                    + (stormId + fileExtension), e);
        }

        // Create a no-watches/warnings if no warnings.
        boolean isEmpty = true;
        for (String str : warningLines) {
            if (!str.trim().isEmpty()) {
                isEmpty = false;
                break;
            }
        }

        if (!isEmpty) {
            int maxWarnLines = Math.min(warningLines.size(), MAX_WARNING_LINES);
            StringBuilder wsb = new StringBuilder();
            for (int ii = 0; ii < maxWarnLines; ii++) {
                if (!(warningLines.get(ii).toUpperCase()
                        .startsWith("CHANGES WITH"))) {
                    wsb.append(warningLines.get(ii));
                } else {
                    wsb.append(
                            "Changes in watches and warnings with this Advisory...");
                }

                if (ii < (warningLines.size() - 1)) {
                    wsb.append("\n");
                }
            }

            warnings = wsb.toString();
        }

        return warnings;
    }

    /**
     * Determine the cardinal wind direction String.
     *
     * Reference is nhc_writeadv.f line 8203 ~ 8307, but this implementation
     * uses floating point input.
     *
     * @param direction
     *            Direction in degrees.
     * @param abbr
     *            true - Direction abbreviation; false - full direction
     *            description.
     * @return String
     */
    public static String getDirection(float direction, boolean abbr) {
        final int N_DIRS = DIRECTION_NAMES.length;
        final double DEG_PER_DIR = 360.0 / N_DIRS;
        /*
         * Determine which compass point 'direction' is closest to by
         * calculating how many counts of 360.0/16 degrees around the compass
         * the angle is (adjusted forward by one half), modulo 16.
         */
        int i = (int) Math.floor(direction / DEG_PER_DIR + 0.5) % N_DIRS;
        if (i < 0) {
            i += N_DIRS;
        }
        return DIRECTION_NAMES[i][abbr ? 1 : 0];
    }

    /**
     * Get a full basin for advisory composition - This may be different from
     * the storm's actual basin string in deck record.
     *
     * From nhc_writeadv.f line 7498: Determine basin from initial (tau =
     * 0)/Best Track longitude
     *
     * @param basin
     *            storm's basin in forecast track
     * @param lon
     *            longitude (negative value means West)
     * @return
     */
    public static String getAdvisoryBasin(String basin, float lon) {

        String bsn = "";

        if (lon < -140) {
            // Central Pacific basin
            bsn = "CP";
        } else {
            if ("AL".equalsIgnoreCase(basin)) {
                // Atlantic basin
                bsn = "AT";
            } else {
                // East Pacific basin
                bsn = "EP";
            }
        }

        return bsn;
    }

    /**
     * Build a formatted AdvisorySummary instance for "Summary" section from
     * advisory info and the current forecast track (TAU 3).
     *
     * Note: the data is formatted without padding white spaces to meet the
     * legacy format.
     *
     * @param advInfo
     *            AdvisoryInfo from GUI
     *
     * @param fcstRecord
     *            Forecast track record (TAU 3)
     *
     * @return AdvisorySummary
     */
    public static AdvisorySummary buildAdvisorySummary(AdvisoryInfo advInfo,
            ForecastTrackRecord fcstRecord) {

        AdvisorySummary sum = new AdvisorySummary();

        float lat = fcstRecord.getClat();
        float lon = fcstRecord.getClon();

        // Dateline crossing
        if (lon > 180.0) {
            lon = 360.0f - lon;
        }

        sum.setLat(
                String.format("%.1f%s", Math.abs(lat), (lat < 0) ? "S" : "N"));
        sum.setLon(
                String.format("%.1f%s", Math.abs(lon), (lon > 0) ? "E" : "W"));

        GeographyPoint geo1 = advInfo.getGeoRef1();
        GeographyPoint geo2 = advInfo.getGeoRef2();

        // Distance/direction from geo reference points to storm location.
        if (geo1 != null && !geo1.getName().startsWith("===")) {
            float glon = geo1.getLongitude();
            float glat = geo1.getLatitude();

            // Distance in meters & direction in degrees
            double[] distDir = AtcfVizUtil.getDistNDir(
                    new Coordinate(glon, glat), new Coordinate(lon, lat));
            String dirStr = AdvisoryUtil.getDirection((float) distDir[1], true);
            float distKm = (float) (distDir[0] / 1000.0);
            float distMi = (float) (distDir[0] / AtcfVizUtil.NM2M
                    * AtcfVizUtil.NAUTICAL_MILES_TO_MILES);

            sum.setDistanceMi1(String.format("%d", Math.round(distMi)));
            sum.setDistanceKm1(String.format("%d", Math.round(distKm)));
            sum.setGeoDirection1(dirStr);
            sum.setGeoReference1(geo1.getName());
        }

        if (geo2 != null && !geo2.getName().startsWith("===")) {
            float glat = geo2.getLatitude();
            float glon = geo2.getLongitude();

            // Distance in meters & direction in degrees
            double[] distDir = AtcfVizUtil.getDistNDir(
                    new Coordinate(glon, glat), new Coordinate(lon, lat));
            String dirStr = AdvisoryUtil.getDirection((float) distDir[1], true);
            float distKm = (float) (distDir[0] / 1000.0);
            float distMi = (float) (distDir[0] / AtcfVizUtil.NM2M
                    * AtcfVizUtil.NAUTICAL_MILES_TO_MILES);

            sum.setDistanceMi2(String.format("%d", Math.round(distMi)));
            sum.setDistanceKm2(String.format("%d", Math.round(distKm)));
            sum.setGeoDirection2(dirStr);
            sum.setGeoReference2(geo2.getName());
        }

        float maxWnd = fcstRecord.getWindMax();
        sum.setWindMph(String.format("%d",
                (int) (maxWnd * AtcfVizUtil.NAUTICAL_MILES_TO_MILES)));
        sum.setWindKmh(String.format("%d",
                (int) (maxWnd * AtcfVizUtil.NM2M / 1000.0)));

        /*
         * Determine whether storm is stationary (storm motion = zero). If so,
         * set its direction of motion to North/360 degrees.
         */
        float stormDir = fcstRecord.getStormDrct();
        float stormSpd = fcstRecord.getStormSped();

        if (stormSpd <= 0 || stormSpd > 250) {
            stormSpd = 0;
            stormDir = 360;
            sum.setStationary(true);
        }

        sum.setDirection(AdvisoryUtil.getDirection(stormDir, true));
        sum.setDegrees(String.format("%d", (int) stormDir));

        sum.setMovementKt(String.format("%d", (int) stormSpd));
        sum.setMovementMph(String.format("%d",
                (int) (stormSpd * AtcfVizUtil.NAUTICAL_MILES_TO_MILES)));
        sum.setMovementKmh(String.format("%d",
                (int) (stormSpd * AtcfVizUtil.NM2M / 1000.0f)));

        float pres = advInfo.getPres();
        sum.setPressureMb(String.format("%d", (int) pres));
        sum.setPressureIn(String.format("%.2f", pres * AtcfVizUtil.MB2IN));

        return sum;
    }

    /**
     * Get a section in an advisory based on the given start and end point.
     *
     * @param tcp
     *            advisory
     * @param start
     *            Start line of the section
     * @param end
     *            End line of the section
     * @param nlines
     *            Number of lines expected in the section
     *
     * @return String Lines in the section
     */
    public static String getAdvisorySection(String tcp, String start,
            String end, int nlines) {

        String contents = "";

        if (tcp != null && !tcp.isEmpty()) {
            String[] lines = tcp.split("\n");

            int istart = 0;
            for (String str : lines) {
                if (str.trim().toUpperCase().startsWith(start)) {
                    break;
                }

                istart++;
            }

            // Stop if start indicator not found.
            if (istart == lines.length) {
                return contents;
            }

            // Skip dash lines that immediately after the start line.
            istart++;
            for (int nn = istart; nn < lines.length; nn++) {
                if (lines[nn].trim().startsWith(DASH_LINE)) {
                    istart++;
                } else {
                    break;
                }
            }

            // Find where the section ends.
            int iend = istart;
            boolean endFound = false;
            for (int kk = istart + 1; kk < lines.length; kk++) {
                if (lines[kk].trim().toUpperCase().startsWith(end)) {
                    endFound = true;
                    iend = kk;
                    break;
                }
            }

            // Stop if end indicator not found.
            if (!endFound) {
                return contents;
            }

            // Go backward to exclude empty lines at the end.
            while (iend > istart && lines[iend - 1].trim().isEmpty()) {
                --iend;
            }

            contents = String.join("\n",
                    Arrays.copyOfRange(lines, istart, iend));
        }

        return contents;
    }

    /**
     * Round a floating point number to a nearest increment.
     *
     * @param number
     *            Floating point number to be rounded.
     * @param inc
     *            integer increment to which to round.
     * @return integer Result of 'number' rounded to the nearest increment
     *         'inc'.
     */
    public static int round(float number, int inc) {
        return inc * (int) ((number + .5 * inc) / inc);
    }

    /**
     * Determine a long-form intensity String based on the intensity value from
     * the deck data.
     *
     * See nhc_writeadv.f line 920-946.
     *
     * @param intensity
     *            2-character intensity from TAU 3.
     * @param isPTC
     *            Flag for potential tropical Cyclone
     * @return String
     */
    public static String getAdvisoryClass(String intensity, boolean isPTC) {

        String intStr = "Remnants of";

        if (isPTC) {
            intStr = "Potential Tropical Cyclone";
        } else {
            if (Arrays.asList(TROP_INTENSITY_STRINGS).contains(intensity)) {
                intStr = WordUtils.capitalizeFully(StormDevelopment
                        .getLongIntensityString(intensity, true));
            } else if (Arrays.asList(POST_TROP_INTENSITY_STRINGS)
                    .contains(intensity)) {
                intStr = WordUtils.capitalizeFully(
                        StormDevelopment.PT.getLongIntensityString());
            }
        }

        return intStr;
    }

    /**
     * Derive a IcaoDataInfo instance for aviation advisory (TCA) from advisory
     * info and forecast track.
     *
     * @param specialTau
     *            Special in AdvisoryInfo (usually TAU=3)
     *
     * @param fcstTrackData
     *            Map<String, List<ForecastTrackRecord>>
     *
     * @return IcaoDataInfo
     */
    public static IcaoDataInfo deriveIcaoDataInfo(int specialTau,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {

        IcaoDataInfo icaoInfo = new IcaoDataInfo();

        // Default values
        int tcaFixedTau = IcaoDataInfo.TCA_FIXED_TAU;
        float missing = IcaoDataInfo.MISSING_VALUE;
        int maxItems = IcaoDataInfo.MAX_ITEMS;

        // Initialize forecast TAUs for TCA. First TAU depends on special TAU.
        int[] tcaMarTau = icaoInfo.getTcaMarTau();

        icaoInfo.getStartTime()[0] = specialTau;
        icaoInfo.getFcstTime()[0] = specialTau;
        tcaMarTau[0] = specialTau;

        for (int ii = 1; ii < 5; ii++) {
            icaoInfo.getFcstTime()[ii] = tcaFixedTau + tcaMarTau[ii];
        }

        // Get data for each TAU.
        for (int ii = 0; ii < maxItems; ii++) {
            int tau = icaoInfo.getStartTime()[ii];
            ForecastTrackRecord rec = getSingleTauData(tau, fcstTrackData);
            if (rec != null) {
                icaoInfo.getStartLat()[ii] = rec.getClat();

                /*
                 * Dateline crossing. Reversed to be used for interpolation
                 * calculation. 79.1E is ingested as positive and 79.1W is
                 * ingested as -79.1.
                 */
                float sLon = rec.getClon();
                if (sLon > 0) {
                    sLon = 360 - sLon;
                }

                icaoInfo.getStartLon()[ii] = sLon;
                icaoInfo.getStartMaxWind()[ii] = rec.getWindMax();

                if (ii == 0) {
                    icaoInfo.setMslp((int) rec.getMslp());
                    icaoInfo.setDir((int) rec.getStormDrct());
                    icaoInfo.setSpd((int) rec.getStormSped());
                }
            }
        }

        // Initialize forecast and interpolate.
        icaoInfo.getFcstLat()[0] = icaoInfo.getStartLat()[0];
        icaoInfo.getFcstLon()[0] = icaoInfo.getStartLon()[0];
        icaoInfo.getFcstMaxWind()[0] = icaoInfo.getStartMaxWind()[0];

        for (int ii = 1; ii < maxItems; ii++) {
            int is1 = (int) missing;
            int is2 = (int) missing;
            for (int jj = 0; jj < 4; jj++) {
                if (icaoInfo.getFcstTime()[ii] >= icaoInfo.getStartTime()[jj]
                        && icaoInfo.getFcstTime()[ii] < icaoInfo
                        .getStartTime()[jj + 1]) {
                    is1 = jj;
                    is2 = jj + 1;
                    break;
                }
            }

            if (is1 != (int) missing) {
                icaoInfo.getFcstLat()[ii] = linterp(
                        icaoInfo.getStartTime()[is1],
                        icaoInfo.getStartLat()[is1],
                        icaoInfo.getStartTime()[is2],
                        icaoInfo.getStartLat()[is2],
                        icaoInfo.getFcstTime()[ii]);
                icaoInfo.getFcstLon()[ii] = linterp(
                        icaoInfo.getStartTime()[is1],
                        icaoInfo.getStartLon()[is1],
                        icaoInfo.getStartTime()[is2],
                        icaoInfo.getStartLon()[is2],
                        icaoInfo.getFcstTime()[ii]);
                icaoInfo.getFcstMaxWind()[ii] = linterp(
                        icaoInfo.getStartTime()[is1],
                        icaoInfo.getStartMaxWind()[is1],
                        icaoInfo.getStartTime()[is2],
                        icaoInfo.getStartMaxWind()[is2],
                        icaoInfo.getFcstTime()[ii]);
            }
        }

        return icaoInfo;

    }

    /**
     * Get forecast track data for a given TAU.
     *
     * @param tau
     *            Forecast hour
     * @param fcstTrackData
     *            Map<String, List<ForecastTrackRecord>>
     *
     * @return ForecastTrackRecord (null if not found)
     */
    public static ForecastTrackRecord getSingleTauData(int tau,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {
        ForecastTrackRecord frec = null;
        for (List<ForecastTrackRecord> recs : fcstTrackData.values()) {
            if (recs != null && !recs.isEmpty()) {
                ForecastTrackRecord rec = recs.get(0);
                if (rec.getFcstHour() == tau) {
                    frec = rec;
                    break;
                }
            }
        }

        return frec;
    }

    /**
     * Linear interpolation to find a value between two values.
     *
     * Note - Taken from nhc_writeadv.f line 6457 ~ 6489
     *
     * @param x0
     *
     * @param y0
     *
     * @param x1
     *
     * @param x2
     *
     * @param x
     *
     * @return Interpolated value.
     */
    public static float linterp(float x0, float y0, float x1, float y1,
            float x) {
        float y;

        float missing = -999;
        float minX = -180;
        float minY = -180;
        float small = 1.e-9f;

        // Handle exceptions (return -999 as missing)
        if (x0 < minX || x1 < minX || y0 < minY || y1 < minY) {
            y = missing;
        } else if ((x0 + small) > x1) {
            // x1 == x0
            y = y0;
        } else {
            // Perform linear interpolation
            y = y0 + (x - x0) * (y1 - y0) / (x1 - x0);
        }

        return y;
    }

    /**
     * Determine the system number or name of a storm/advisory.
     *
     * See nhc_writeadv.f line 856-919.
     *
     * @param stormName
     *            Storm name.
     * @param intensity
     *            2-character intensity.
     * @param basinDesignator
     *            1-character "A"/"P".
     * @param isPTC
     *            Flag to indicate "potential tropical storm".
     * @return
     */
    public static String getAdvisoryName(String stormName, String intensity,
            String basinDesignator, boolean isPTC) {

        String advName = "TEST";
        if (stormName != null && !stormName.trim().isEmpty()) {
            advName = stormName;
        }

        // Take care of long number names.
        if (advName.length() >= 10) {
            String ending = advName.substring(6, 10);
            switch (ending) {
            case ("-THR"):
                advName += "EE";
            break;
            case ("-FOU"):
                advName += 'R';
            break;
            case ("-FIV"):
                advName += 'E';
            break;
            case ("-SEV"):
                advName += "EN";
            break;
            case ("-EIG"):
                advName += "HT";
            break;
            case ("-NIN"):
                advName += 'E';
            break;
            default:
                break;
            }
        }

        /*
         * For initial depressions, append "-E" for East Pacific or "-C" for
         * Central Pacific basin.
         */
        boolean isLetter = false;
        if (Arrays.asList(PTC_INTENSITY_STRINGS).contains(intensity) || isPTC) {
            if (advName.length() >= 3) {
                String start = advName.substring(0, 3);
                if (Arrays.asList(ADV_NAME_LETTER_STRINGS).contains(start)) {
                    isLetter = true;
                }
            }
        }

        if (isLetter) {
            if ("P".equals(basinDesignator)) {
                advName = advName.trim() + "-E";
            } else if ("C".equals(basinDesignator)) {
                advName = advName.trim() + "-C";
            }
        }

        return advName;
    }

    /**
     * Determine advisory number phrase. It depends on advisory type, combined
     * length of advisory class and name and if it is a special advisory.
     *
     * See nhc_writeadv.f line 948~967 for example.
     *
     * @param advType
     *            Advisory type.
     * @param isSpecialAdv
     *            Special advisory flag.
     * @param advClass
     *            Adjust advisory class (see getAdvisoryClass()).
     * @param advName
     *            Adjust advisory/storm name (see getAdvisoryName()).
     * @param advNumber
     *            Advisory number
     * @return Adjusted advisory number phase
     */
    public static String getAdvNumPhase(AdvisoryType advType,
            boolean isSpecialAdv, String advClass, String advName,
            int advNumber) {

        String advNumPhrase = advType.getAdvNumPhrase();

        // Handle special advisory.
        if (isSpecialAdv) {
            String spStr1 = "Special " + advNumPhrase;
            String spStr2 = "SP " + advNumPhrase;
            String advNumStr = String.format("%03d", advNumber);
            String testStr = advClass + " " + advName + " " + spStr1 + " "
                    + advNumStr;

            switch (advType) {
            case TCP:
            case TCD:
                advNumPhrase = spStr1;
                break;
            case TCM:
            case TCA:
                advNumPhrase = spStr1;
                if (testStr.length() > MAX_WIDTH) {
                    advNumPhrase = spStr2;
                }

                break;
            case TCP_A:
                break;
            case PWS:
                break;
            default:
                break;
            }
        }

        return advNumPhrase;
    }

    /**
     * Determine the advisory time for a special advisory (See nhc_writeadv.f).
     *
     * @param initialTime
     *            ZonedDateTime at TAU 0
     * @param spTime
     *            Special time to adjust (HHmm)
     * @return ZonedDateTime Adjusted advisory time.
     */
    public static ZonedDateTime adjustSpecialAdvTime(ZonedDateTime initialTime,
            String spTime) {

        /*
         * If there is a special time, change forecast advisory time to special
         * time and change the current marine time to the next marine time.
         */
        int iHour = initialTime.getHour();
        int jHour = getStringAsInt(spTime.substring(0, 2), false, true, iHour);
        int jMin = getStringAsInt(spTime.substring(2, 4), false, true, 0);

        // Commented out in legacy.
        /*-
        if (jMin >= 30) {
           jHour = jHour + 1;
        }
         */

        int lHour = jHour - iHour;
        if (lHour <= 0) {
            lHour = 24 + lHour;
        }

        /*
         * Increment initial time with 'lHour' hours to obtain a new special
         * time.
         */
        ZonedDateTime specialTime = initialTime.plusHours(lHour);
        specialTime = specialTime.plusMinutes(jMin);

        return specialTime;
    }

    /**
     * Get an int from a String
     *
     * @param valString
     *            The string to parse
     * @param reverse
     *            Flips value's sign if true
     * @param useDefault
     *            Check to see if we use a passed in default value if parsing
     *            fails
     *
     * @param defaultValue
     *            Default value if parsing fails
     *
     * @return the parsed value, or the invalid/default.
     */
    public static int getStringAsInt(String valString, boolean reverse,
            boolean useDefault, int defaultValue) {
        int val = 0;
        try {
            val = Integer.parseInt(valString);
            if (reverse) {
                val *= -1;
            }
        } catch (NumberFormatException e) {
            if (useDefault) {
                val = defaultValue;
            }
        }

        return val;
    }

    /**
     * Gets the headline section in a TCP advisory.
     *
     * @param tcp
     * @return Headline as string
     */
    public static String getTcpHeadlineSection(String tcp) {
        return getAdvisorySection(tcp, HEADERLINE_START, HEADERLINE_END,
                MAX_HEAD_LINES);
    }

    /**
     * Gets the watch & warning section in a TCP advisory.
     *
     * @param tcp
     * @return Watch & warning as string
     */
    public static String getTcpWatchWarningSection(String tcp) {
        return getAdvisorySection(tcp, WATCH_WARNING_START, WATCH_WARNING_END,
                MAX_WARNING_LINES);
    }

    /**
     * Gets the discussion & outlook section in a TCP advisory.
     *
     * @param tcp
     * @return Discussion & outlook as string
     */
    public static String getTcpDiscussionSection(String tcp) {
        return getAdvisorySection(tcp, DISCUSSION_START, DISCUSSION_END,
                MAX_HAZARDS_LINES);

    }

    /**
     * Gets the hazards section in a TCP advisory.
     *
     * @param tcp
     * @return Hazards as string
     */
    public static String getTcpHazardsSection(String tcp) {
        return getAdvisorySection(tcp, HAZARDS_START, HAZARDS_END,
                MAX_HAZARDS_LINES);

    }

}

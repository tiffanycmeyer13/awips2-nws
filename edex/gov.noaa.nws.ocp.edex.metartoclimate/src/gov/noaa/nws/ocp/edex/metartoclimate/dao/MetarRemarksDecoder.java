/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateMetarDecodingException;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.DecodedMetar;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.RecentWx;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.SignificantCloud;

/**
 * Decode remarks of METAR report.
 * 
 * Implementations converted from retrieve_OBS.C and related files. Insert Fixed
 * Surface Station/Flight Service Station data based on a METAR report.
 * 
 * METAR decoding resources referenced in addition to consulting Legacy code:
 * 
 * <pre>
 * http://www.moratech.com/aviation/metar-class/metar-pg13-rmk.html
 * http://www.met.tamu.edu/class/metar/quick-metar.html
 * https://math.la.asu.edu/~eric/workshop/METAR.html
 * https://www.aviationweather.gov/static/help/taf-decode.php
 * http://chesapeakesportpilot.com/wp-content/uploads/2015/03/military_wx_codes.pdf
 * http://meteocentre.com/doc/metar.html
 * http://www.nws.noaa.gov/om/forms/resources/SFCTraining.pdf
 * </pre>
 * 
 * In Legacy, Supplemental Climatological Data (SCD) reports were also queried
 * for to be placed into FSS tables. However, according to
 * (https://vlab.ncep.noaa.gov/documents/584952/600396/AWP.RLSN.OB16.2.1_Final.
 * pdf/) (search for "SCD") these products were discontinued 3 years prior to
 * that document (so in 2013).
 * 
 * No work will be done with SCD reports.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 SEP 2017  37754      amoore      Initial creation.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public final class MetarRemarksDecoder {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MetarRemarksDecoder.class);

    /**
     * Decode the remarks section of a METAR report. Based on
     * hmPED_DcdMTRRemark.c.
     * 
     * @param decodedMetar
     *            metar to fill out.
     * @param reportArray
     *            report array to look at.
     * @param startIndex
     *            starting index of report array to look at.
     * @throws ArrayIndexOutOfBoundsException
     *             on invalid array indexing.
     * @throws NumberFormatException
     *             on invalid number.
     * @throws ClimateMetarDecodingException
     *             on invalid format.
     */
    protected static void decodeMetarRemarks(DecodedMetar decodedMetar,
            String[] reportArray, int startIndex)
                    throws ArrayIndexOutOfBoundsException,
                    NumberFormatException, ClimateMetarDecodingException {
        logger.debug("Decoding METAR remarks section.");
        /*
         * Loop through remaining report portions. If a valid section is found,
         * move to next portion. If any invalid portions are found, log return
         * failure.
         */
        for (int reportIndex = startIndex; reportIndex < reportArray.length; reportIndex++) {
            // current report section
            String currReportSection = reportArray[reportIndex];

            // check for simple flags first
            if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.FIRST_INDICATOR)) {
                /*
                 * FIRST section. From hmPED_FIRST.c.
                 */
                decodedMetar.setFirst(true);
                // end FIRST section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.LAST_INDICATOR)) {
                /*
                 * LAST section. From hmPED_LAST.c.
                 */
                decodedMetar.setLast(true);
                // end LAST section
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_PEAK_WIND_INDICATOR)) {
                /*
                 * No peak wind section. From hmPED_PWINO.c.
                 */
                decodedMetar.setNoPeakWind(true);
                // end no peak wind
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_LIGHTNING_INDICATOR)) {
                /*
                 * No lightning data section. From hmPED_TSNO.c.
                 */
                decodedMetar.setNoThunderStorms(true);
                // end no lightning data
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_RUNWAY_VISUAL_RANGE_INDICATOR)) {
                /*
                 * No runway visual range section. From hmPED_RVRNO.c.
                 */
                decodedMetar.setNoRunwayVisualRange(true);
                // end no runway visual range section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.NO_RAIN_INDICATOR)) {
                /*
                 * No rain data section. From hmPED_PNO.c.
                 */
                decodedMetar.setNoRain(true);
                // end no rain data
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_FREEZING_RAIN_INDICATOR)) {
                /*
                 * No freezing rain data section. From hmPED_FZRANO.c.
                 */
                decodedMetar.setNoFreezingRain(true);
                // end no freezing rain data
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_SPECIAL_REPORT_INDICATOR)) {
                /*
                 * No special report section. From hmPED_NOSPECI.c.
                 */
                decodedMetar.setNoSpeci(true);
                // end no special report
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.AURORA_BOREALIS_INDICATOR)) {
                /*
                 * Aurora borealis section. From hmPED_AURBO.c.
                 */
                decodedMetar.setAuroraBorealis(true);
                // end aurora borealis section
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.PRESSURE_FALLING_RAPIDLY_INDICATOR)) {
                /*
                 * Pressure falling rapidly section. From hmPED_PRESFR.c.
                 */
                decodedMetar.setPressureFallingRapidly(true);
                // end pressure falling rapidly
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.PRESSURE_RISING_RAPIDLY_INDICATOR)) {
                /*
                 * Pressure rising rapidly section. From hmPED_PRESRR.c.
                 */
                decodedMetar.setPressureRisingRapidly(true);
                // end pressure rising rapidly
            } else if (currReportSection
                    .equals(MetarDecoderUtil.MAINTENANCE_INDICATOR)) {
                /*
                 * Maintenance section. From hmPED_DollarSign.c.
                 */
                decodedMetar.setMaintenance(true);
                // end maintenance section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.TORNADO_STRING)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.WATERSPOUT_STRING)
                    || currReportSection
                            .equalsIgnoreCase(MetarDecoderUtil.FUNNEL_STRING)) {
                /*
                 * Tornadic activity. From hmPED_TornadicActiv.c.
                 */
                reportIndex = parseTornadicActivity(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end of tornadic section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.A01_INDICATOR_STRING)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.A02_INDICATOR_STRING)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.A01_INDICATOR_STRING_NUM)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.A02_INDICATOR_STRING_NUM)) {
                /*
                 * auto indicator section. From hmPED_A0indicator.c.
                 */
                decodedMetar.setAutoIndicator(currReportSection);
                // end of auto indicator section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.PEAKWIND_PREFIX)) {
                /*
                 * peak wind section
                 */
                reportIndex = parsePeakWind(decodedMetar, reportArray,
                        reportIndex);
                // end of peak wind section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.WIND_SHIFT_STRING)) {
                /*
                 * Wind shift section. From hmPED_WindShift.c.
                 */
                reportIndex = parseWindShift(decodedMetar, reportArray,
                        reportIndex);

                // end of wind shift section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.TOWER_PREFIX)) {
                /*
                 * Tower visibility section
                 */
                reportIndex = parseTowerVisibility(decodedMetar, reportArray,
                        reportIndex);
                // end tower visibility section
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.SURFACE_VISIBILITY_PREFIX)) {
                /*
                 * Surface visibility section
                 */
                reportIndex = parseSurfaceVisibility(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end surface visibility section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.VISIBILITY_PREFIX)) {
                /*
                 * Variable or second site or sector visibility.
                 */
                reportIndex = parseOtherVisibility(decodedMetar, reportArray,
                        reportIndex);
                // end variable or second site or sector visibility
            } else if (currReportSection.startsWith(
                    MetarDecoderUtil.DISPATCH_VISUAL_RANGE_STRING)) {
                /*
                 * Dispatch visual range section
                 */
                parseDispatchVisualRange(decodedMetar, currReportSection);
                // end DVR
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.OCCASIONAL_LIGHTNING_STRING)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.FREQUENT_LIGHTNING_STRING)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.CONSTANT_LIGHTNING_STRING)
                    || currReportSection.equalsIgnoreCase(
                            MetarDecoderUtil.LIGHTNING_TYPE_PREFIX)) {
                /*
                 * Legacy decoder does not allow "LTG" to begin the section,
                 * though this does seem allowed in reports.
                 */
                /*
                 * Lightning section.
                 */
                reportIndex = parseLightningFrequency(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end lightning section
            } else if (currReportSection
                    .matches(MetarDecoderUtil.RECENT_WX_REGEX)) {
                /*
                 * Recent weather string section. One long string with coded
                 * weather + either start or end time. Can be written in any
                 * order as long as each weather event is succeeded by a time.
                 * Weather events of a duplicate type have priority in filling
                 * out missing time data of a previous instance; if the other
                 * instances of the event already have the duplicate's time type
                 * (beginning or end) filled out, create a new event.
                 */
                parseRecentWeather(decodedMetar, currReportSection);
                // end recent weather string
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.THUNDERSTORM_PREFIX)) {
                /*
                 * Thunderstorm location section
                 */
                reportIndex = parseThunderStorm(decodedMetar, reportArray,
                        reportIndex);
                // end thunderstorm location
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.HAIL_ABBR)) {
                /*
                 * Hail section
                 */
                reportIndex = parseHail(decodedMetar, reportArray, reportIndex);
                // end hail
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.VIRGA_STRING)) {
                /*
                 * VIRGA section
                 */
                reportIndex = parseVIRGA(decodedMetar, reportArray,
                        reportIndex);
                // end VIRGA
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.CEILING_HEIGHT_ABBR)) {
                /*
                 * CIG or CIG second site section
                 */
                reportIndex = parseCeilingHeight(decodedMetar, reportArray,
                        reportIndex);
                // end CIG or CIG second site section
            } else if (MetarDecoderUtil.VALID_WX_SYMBOLS
                    .contains(currReportSection)) {
                /*
                 * Obscurations section
                 */
                reportIndex = parseObscuration(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end obscurations section
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.OVERCAST_CLOUDS_STRING)
                    || currReportSection.startsWith(
                            MetarDecoderUtil.SCATTERED_CLOUDS_STRING)
                    || currReportSection
                            .startsWith(MetarDecoderUtil.FEW_CLOUDS_STRING)
                    || currReportSection.startsWith(
                            MetarDecoderUtil.BROKEN_CLOUDS_STRING)) {
                /*
                 * Variable sky section
                 */
                reportIndex = parseVariableSky(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end variable sky
            } else if (MetarDecoderUtil.VALID_SIG_TYPES
                    .contains(currReportSection)) {
                /*
                 * Significant cloud types section
                 */
                reportIndex = parseSignificantClouds(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end significant cloud types
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.SEA_LEVEL_PRESSURE_PREFIX)) {
                /*
                 * Sea level pressure section
                 */
                reportIndex = parseSLP(decodedMetar, reportArray, reportIndex,
                        currReportSection);
                // end sea level pressure
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.SNOW_INCREASING_RAPIDLY_ABBR)) {
                /*
                 * Snow increasing rapidly section
                 */
                reportIndex = parseSnowIncrease(decodedMetar, reportArray,
                        reportIndex);
                // end snow increasing rapidly
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.HOURLY_PRECIP_PREFIX)) {
                /*
                 * Hourly precip section
                 */
                parseHourlyPrecip(decodedMetar, currReportSection);
                // end hourly precip
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.PRECIP_6_OR_3_HOURLY_PREFIX)) {
                /*
                 * 3- or 6-hour precip section
                 */
                parse3or6HourlyPrecip(decodedMetar, currReportSection);
                // end 3- or 6-hour precip
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.PRECIP_24_HOUR_PREFIX)) {
                /*
                 * 24-hour precip section
                 */
                parse24HourPrecip(decodedMetar, currReportSection);
                // end 24-hour precip
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.SNOW_DEPTH_PREFIX)) {
                /*
                 * Snow depth on ground section.
                 */
                parseSnowDepth(decodedMetar, currReportSection);
                // end snow depth on ground
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.WATER_EQUIV_PREFIX)) {
                /*
                 * Water equivalent of snow section
                 */
                parseWaterEquivalent(decodedMetar, currReportSection);
                // end water equivalent of snow
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.SYNOP_CLOUD_TYPES_PREFIX)) {
                /*
                 * Synoptic cloud types section.
                 */
                parseSynopClouds(decodedMetar, currReportSection);
                // end synoptic cloud types
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.SUNSHINE_PREFIX)) {
                /*
                 * Sunshine duration section.
                 */
                parseSunshineDuration(decodedMetar, currReportSection);
                // end sunshine duration
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.TEMP_AND_DEW_PREFIX)
                    && currReportSection
                            .matches(MetarDecoderUtil.ALPHANUMERIC_REGEX)) {
                /*
                 * Temperature and dew point section
                 */
                reportIndex = parseTempAndDew(decodedMetar, reportIndex,
                        currReportSection);
                // end temperature and dew point
            } else if (currReportSection
                    .matches(MetarDecoderUtil.MAX_TEMP_6_HOUR_REGEX)) {
                /*
                 * 6-hourly max temp section. From hmPED_MaxTemp.c.
                 */
                decodedMetar.setMaxTemp(
                        Float.parseFloat(currReportSection.substring(2)) / 10);
                if (currReportSection.substring(1, 2).equals("1")) {
                    decodedMetar.setMaxTemp(decodedMetar.getMaxTemp() * -1);
                }
                // end 6-hourly max temp
            } else if (currReportSection
                    .matches(MetarDecoderUtil.MIN_TEMP_6_HOUR_REGEX)) {
                /*
                 * 6-hourly min temp section. From hmPED_MinTemp.c.
                 */
                decodedMetar.setMinTemp(
                        Float.parseFloat(currReportSection.substring(2)) / 10);
                if (currReportSection.substring(1, 2).equals("1")) {
                    decodedMetar.setMinTemp(decodedMetar.getMinTemp() * -1);
                }
                // end 6-hourly min temp
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.MAX_MIN_TEMPS_24_HOUR_PREFIX)
                    && currReportSection.length() > 5) {
                /*
                 * 24-hour max and min temp section
                 */
                parse24HourMaxMinTemps(decodedMetar, currReportSection);
                // end 24-hour max and min temp
            } else if (currReportSection
                    .startsWith(MetarDecoderUtil.PRESS_TEND_3_HOUR_PREFIX)) {
                /*
                 * 3-hourly pressure tendency section
                 */
                parse3HourPressureTendency(decodedMetar, currReportSection);
                // end 3-hourly pressure tendency
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_SECONDARY_VISUALS_INDICATOR)) {
                /*
                 * No secondary visibility section
                 */
                reportIndex = parseNoSecondaryVisuals(decodedMetar, reportArray,
                        reportIndex);
                // end no secondary visibility
            } else if (currReportSection.equalsIgnoreCase(
                    MetarDecoderUtil.NO_SECONDARY_CEILING_HEIGHT_INDICATOR)) {
                /*
                 * No secondary ceiling height data section
                 */
                reportIndex = parseNoSecondaryCeilingHeight(decodedMetar,
                        reportArray, reportIndex);
                // end no secondary ceiling height data
            } else {
                /*
                 * Unexpected word in remarks section.
                 */
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Unexpected word: [" + currReportSection
                        + "] in METAR remarks. This word will be skipped.");
            }
        }
    }

    /**
     * Parse no secondary ceiling height from METAR remarks. Assume current word
     * was {@link MetarDecoderUtil#NO_SECONDARY_CEILING_HEIGHT_INDICATOR}. From
     * hmPED_CHINO.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_CHINO()
     * 
     * FUNCTION DESCRIPTION
     * This routine check to see is the CHINO string is present in the meassage.
     * If so it returns TRUE; else False. This value is used to indicate that 
     * that staion is equipped with a secondary visibility sensor and that is 
     * it not working. Also if the string is detected it stores the location of
     * the inoperative sensor into the Mptr structure.
     *
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing at.
     */

    private static int parseNoSecondaryCeilingHeight(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            /*
             * Task 29239: legacy wants "RY" but documentation suggests "RWY" is
             * expected
             */
            if (currReportSection.startsWith(MetarDecoderUtil.RUNWAY_RY_STRING)
                    || currReportSection
                            .startsWith(MetarDecoderUtil.RUNWAY_RWY_STRING)) {
                decodedMetar.setNoSecondaryCeilingHeight(true);
                decodedMetar
                        .setNoSecondaryCeilingHeightLocation(currReportSection);
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected location after 'CHINO' in METAR remarks, but got: ["
                                + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected location after 'CHINO' in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse no secondary visuals from METAR remarks. Assume current word was
     * {@link MetarDecoderUtil#NO_SECONDARY_VISUALS_INDICATOR}. From
     * hmPED_VISNO.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_VISNO()
     *
     * FUNCTION DESCRIPTION
     * Determine if the curren token is a VISNO indicator. If so return TRUE;
     * else FALSE. This indicator indicates that a secondary visibility sensor
     * at an automated site in not functioning. Store the location of the
     * sensor in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing at.
     */

    private static int parseNoSecondaryVisuals(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            /*
             * Task 29239: legacy wants "RY" but documentation suggests "RWY" is
             * expected
             */
            if (currReportSection.startsWith(MetarDecoderUtil.RUNWAY_RY_STRING)
                    || currReportSection
                            .startsWith(MetarDecoderUtil.RUNWAY_RWY_STRING)) {
                decodedMetar.setNoSecondaryVisuals(true);
                decodedMetar.setNoSecondaryVisualsLocation(currReportSection);
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected location after 'VISNO' in METAR remarks, but got: ["
                                + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected location after 'VISNO' in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse 3-hour pressure tendency from METAR remarks. Assume current word
     * began with {@link MetarDecoderUtil#PRESS_TEND_3_HOUR_PREFIX}. from
     * hmPED_Ptendency.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_Ptendency()
     * 
     * FUNCTION DESCRIPTION
     * Determine if the current toke is a valid pressure tendency report. if so
     * return TRUE; else FALSE. This reports the 3 hour pressure tendency 
     * for the site and the pressure value is stored in the Decoded_METAR
     * structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */
    private static void parse3HourPressureTendency(DecodedMetar decodedMetar,
            String currReportSection) {
        if (currReportSection
                .matches(MetarDecoderUtil.PRESS_TEND_3_HOUR_REGEX)) {
            decodedMetar.setCharPressureTendency(
                    Integer.parseInt(currReportSection.substring(1, 2)));
            decodedMetar.setPressure3HourTendency(
                    Float.parseFloat(currReportSection.substring(2)) / 10);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected 3-hour pressure tendency: ["
                    + currReportSection + "] does not match pattern: ["
                    + MetarDecoderUtil.PRESS_TEND_3_HOUR_REGEX + "].");
        }
    }

    /**
     * Parse 24-hour max and min temps from METAR remarks. Assume the current
     * word started with {@link MetarDecoderUtil#MAX_MIN_TEMPS_24_HOUR_PREFIX}
     * and is at least 6 characters long. From hmPED_T24MaxMinTemp.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_T24MaxMinTemp()
     * 
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valis 24 hour max min temperature
     * report. If so return TRUE; else FALSE. IF valid, store the max and min 
     * temperature values in the Decoded_METAR structure.
     *
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parse24HourMaxMinTemps(DecodedMetar decodedMetar,
            String currReportSection) {
        if (currReportSection.substring(1, 5)
                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                && (currReportSection.substring(1, 2).equals("0")
                        || currReportSection.substring(1, 2).equals("1"))) {
            decodedMetar.setMax24Temp(
                    Float.parseFloat(currReportSection.substring(2, 5)) / 10);
            if (currReportSection.substring(1, 2).equals("1")) {
                decodedMetar.setMax24Temp(decodedMetar.getMax24Temp() * -1);
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected 24-hour max-min temps: ["
                    + currReportSection + "] has invalid max temp.");
        }

        if (currReportSection.substring(5)
                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                && (currReportSection.substring(5, 6).equals("0")
                        || currReportSection.substring(5, 6).equals("1"))) {
            decodedMetar.setMin24Temp(
                    Float.parseFloat(currReportSection.substring(6)) / 10);
            if (currReportSection.substring(5, 6).equals("1")) {
                decodedMetar.setMin24Temp(decodedMetar.getMin24Temp() * -1);
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected 24-hour max-min temps: ["
                    + currReportSection + "] has invalid min temp.");
        }
    }

    /**
     * Parse temp and dew from METAR remarks. Assume the current word began with
     * {@link MetarDecoderUtil#TEMP_AND_DEW_PREFIX}. From hmPED_TTdTenths.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_TTdTenths()
     *
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid Hourly temperature and
     * dew point report. If so return TRUE; else FALSE. If valid, store the
     * temperature and dew point up to tenths of a degree in the Decoded_
     * METAR structure.
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseTempAndDew(DecodedMetar decodedMetar,
            int reportIndex, String currReportSection) {
        /*
         * legacy suggests that the dew point information is optional, but
         * documentation seems to state it is mandatory
         */
        float tempMultiplier;
        if (currReportSection.length() >= 2
                && currReportSection.substring(1, 2).equals("0")) {
            tempMultiplier = 0.1f;
        } else if (currReportSection.length() >= 2
                && currReportSection.substring(1, 2).equals("1")) {
            tempMultiplier = -0.1f;
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Temperature and dewpoint: [" + currReportSection
                    + "] second character expected to be 0 or 1.");
            return reportIndex;
        }

        if (currReportSection.length() >= 5) {
            decodedMetar.setTempToTenths(tempMultiplier
                    * Float.parseFloat(currReportSection.substring(2, 5)));
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Invalid temperature and dewpoint: [" + currReportSection
                            + "] does not have full temperature data.");
            return reportIndex;
        }

        // see if dew point is present
        if (currReportSection.length() > 5) {
            float dewMultiplier;
            if (currReportSection.substring(5, 6).equals("0")) {
                dewMultiplier = 0.1f;
            } else if (currReportSection.substring(5, 6).equals("1")) {
                dewMultiplier = -0.1f;
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Temperature and dewpoint: [" + currReportSection
                        + "] sixth character expected to be 0 or 1.");
                return reportIndex;
            }

            if (currReportSection.length() > 6) {
                decodedMetar.setDewpointTempToTenths(dewMultiplier
                        * Float.parseFloat(currReportSection.substring(6)));
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Invalid temperature and dewpoint: ["
                        + currReportSection + "] does not have full dew data.");
                return reportIndex;
            }
        } else {
            logger.debug(
                    "No dew point present in: [" + currReportSection + "]");
        }

        return reportIndex;
    }

    /**
     * Parse sunshine duration from METAR remarks. Assume current word started
     * with {@link MetarDecoderUtil#SUNSHINE_PREFIX}. From hmPED_SunshineDur.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_SunshineDur()
     *
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid Sunshine Duration token. If so
     * return TRUE; else FALSE. If it is valid store the amount of sun in the
     * Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parseSunshineDuration(DecodedMetar decodedMetar,
            String currReportSection) {
        if (currReportSection.matches(MetarDecoderUtil.SUNSHINE_REGEX)) {
            if (currReportSection.substring(2)
                    .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                /* Store the sushine duration value. */
                decodedMetar.setSunshineDur(
                        Integer.parseInt(currReportSection.substring(2)));
            } else {
                /*
                 * Must be ///, which indicates the sun sensor is out
                 */
                decodedMetar.setSunSensorOut(true);
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected sunshine duration: [" + currReportSection
                    + "] does not match pattern: ["
                    + MetarDecoderUtil.SUNSHINE_REGEX + "].");
        }
    }

    /**
     * Parse synoptic clouds from METAR remarks. Assume current word started
     * with {@link MetarDecoderUtil#SYNOP_CLOUD_TYPES_PREFIX}. From
     * hmPED_SynopClouds.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_SynopClouds()
     *
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid Synoptic Cloud report. If so
     * return TRUE; else FALSE. If valid, store the clode type in the
     * Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parseSynopClouds(DecodedMetar decodedMetar,
            String currReportSection) {
        if (currReportSection
                .matches(MetarDecoderUtil.SYNOP_CLOUD_TYPES_REGEX)) {
            decodedMetar.setSynopticCloudType(currReportSection);
            decodedMetar.setCloudLow(currReportSection.substring(2, 3));
            decodedMetar.setCloudMedium(currReportSection.substring(3, 4));
            decodedMetar.setCloudHigh(currReportSection.substring(4, 5));
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected synoptic clouds: [" + currReportSection
                    + "] does not match pattern: ["
                    + MetarDecoderUtil.SYNOP_CLOUD_TYPES_REGEX + "].");
        }
    }

    /**
     * Parse water equivalence from METAR remarks. Assume current word started
     * with {@link MetarDecoderUtil#WATER_EQUIV_PREFIX}. From
     * hmPED_H20EquivSnow.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_WaterEquivSnow()
     *
     * FUNCTION DESCRIPTION
     * This routine determines in the water equivalent of snow is of the
     * correct format. If so it returns TRUE; else FALSE. If it is valid,
     * it calculates the water equivalent of the snow fallen, and stores it in
     * the Decode_METAR structure.
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parseWaterEquivalent(DecodedMetar decodedMetar,
            String currReportSection) {
        String waterEquivString = currReportSection
                .substring(MetarDecoderUtil.WATER_EQUIV_PREFIX.length());

        if (waterEquivString.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            decodedMetar.setWaterEquivSnow(
                    Float.parseFloat(waterEquivString) / 10f);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected water equivalent value: ["
                    + waterEquivString + "] is not all numbers.");
        }
    }

    /**
     * Parse snow depth from METAR remarks. Assume current word began with
     * {@link MetarDecoderUtil#SNOW_DEPTH_PREFIX}. From hmPED_SnowDepth.c.
     * 
     * <pre>
     * FUNCTION NAME
     *   hmPED_SnowDepth()
     *
     * FUNCTION DESCRIPTION
     *   Determine if the current group is a snow depth data. If so return TRUE;
     *   else FALSE. If it is snow depth data store the amount of snow in the
     *   Decoded_METAR structure.
     *
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parseSnowDepth(DecodedMetar decodedMetar,
            String currReportSection) {
        String snowDepthString = currReportSection
                .substring(MetarDecoderUtil.SNOW_DEPTH_PREFIX.length());

        if (snowDepthString.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            decodedMetar.setSnowDepth(Integer.parseInt(snowDepthString));
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected snow depth value: [" + currReportSection
                    + "] is not all numbers.");
        }
    }

    /**
     * Parse 24-hour precip from METAR remarks. Assume current word began with
     * {@link MetarDecoderUtil#PRECIP_24_HOUR_PREFIX}. From hmPED_P24Precip.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_P24Precip()
     * 
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid 24 hour precip report. If so
     * return true; else FALSE. If valid store the amount in the Decode_METAR
     * structure.
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parse24HourPrecip(DecodedMetar decodedMetar,
            String currReportSection) {
        String precipValueString = currReportSection
                .substring(MetarDecoderUtil.PRECIP_24_HOUR_PREFIX.length());

        /*
         * check for a report type of 7////. If found store INT_MAX as the
         * precip amount.
         */
        if (precipValueString.equals(MetarDecoderUtil.MISSING_VALUE_SLASHES)) {
            decodedMetar.setPrecip24Amt((float) Integer.MAX_VALUE);
        } else if (precipValueString.matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                && precipValueString.length() == 4) {
            decodedMetar
                    .setPrecip24Amt(Float.parseFloat(precipValueString) / 100);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected 24-hour precip value in METAR remarks: ["
                    + precipValueString + "].");
        }
    }

    /**
     * Parse 3- or 6-hourly precip from METAR remarks. Assume current word began
     * with {@link MetarDecoderUtil#PRECIP_6_OR_3_HOURLY_PREFIX}. From
     * hmPED_P6Precip.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_P6Precip()
     *
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid 6 hour precip report. If so
     * return TRUE; else FALSE. If valid store the amount in the Decoded_METAR
     * structure.
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parse3or6HourlyPrecip(DecodedMetar decodedMetar,
            String currReportSection) {
        String precipValueString = currReportSection.substring(
                MetarDecoderUtil.PRECIP_6_OR_3_HOURLY_PREFIX.length());

        /*
         * check for a report type of 6////. If found store INT_MAX as the
         * precip amount.
         */
        if (precipValueString.equals(MetarDecoderUtil.MISSING_VALUE_SLASHES)) {
            decodedMetar.setPrecipAmt((float) Integer.MAX_VALUE);
        } else if (precipValueString.matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                && precipValueString.length() == 4) {
            decodedMetar
                    .setPrecipAmt(Float.parseFloat(precipValueString) / 100);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected 6-hour precip value in METAR remarks: ["
                    + precipValueString + "].");
        }
    }

    /**
     * Parse hourly precip from METAR remarks. Assume current word began with
     * {@link MetarDecoderUtil#HOURLY_PRECIP_PREFIX}. From hmPED_HourlyPrecip.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_HourlyPrecip()
     *
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid Hourly precip report. If so
     * return TRUE; else FALSE. If valid, store the precip amount in the
     * Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param currReportSection
     */

    private static void parseHourlyPrecip(DecodedMetar decodedMetar,
            String currReportSection) {
        String precipValueString = currReportSection
                .substring(MetarDecoderUtil.HOURLY_PRECIP_PREFIX.length());

        if (precipValueString.matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                && precipValueString.length() == 4) {
            decodedMetar.setHourlyPrecip(
                    Float.parseFloat(precipValueString) * 0.01f);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected hourly precip value in METAR remarks: ["
                    + precipValueString + "].");
        }
    }

    /**
     * Parse snow increase from METAR remarks. Assume current word was
     * {@link MetarDecoderUtil#SNOW_INCREASING_RAPIDLY_ABBR}. From
     * hmPED_SNINCR.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_SNINCR()
     *
     * FUNCTION DESCRIPTION
     * Determines if the SNINCR remark is present. If so return TRUE; else FALSE.
     * This tells the the snow at the current site has increased more that an
     * inche or more in the last hour.
     *
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing at.
     */

    private static int parseSnowIncrease(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        /*
         * Task 29191: legacy expects snow data to be the next word, but
         * documentation states it is appended directly to the end of SNINCR
         */
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            if (currReportSection.matches(MetarDecoderUtil.FRACTION_REGEX)) {
                String[] fractionSplit = currReportSection
                        .split(MetarDecoderUtil.SLASH_DIVIDER);

                decodedMetar
                        .setSnowIncrease(Integer.parseInt(fractionSplit[0]));
                decodedMetar.setSnowIncreaseTotalDepth(
                        Integer.parseInt(fractionSplit[1]));
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected snow values after 'SNINCR' in METAR remarks to match pattern: ["
                                + MetarDecoderUtil.FRACTION_REGEX
                                + "], but got: [" + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected snow values after 'SNINCR' in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse SLP from METAR remarks. Assume current word starts with
     * {@link MetarDecoderUtil#SEA_LEVEL_PRESSURE_PREFIX}. From hmPED_SLP.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_SLP()
     *
     * FUNCTION DESCRIPTION
     * Determines if the SLP remark is present. If so return TRUE; else FALSE.
     * This indicates that a Sea Level Pressure value follows, and the pressure
     * value is stored in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseSLP(DecodedMetar decodedMetar, String[] reportArray,
            int reportIndex, String currReportSection) {
        if (currReportSection.equalsIgnoreCase(
                MetarDecoderUtil.NO_SEA_LEVEL_PRESSURE_INDICATOR)) {
            /* check to see if there is no sea level pressure report. */
            decodedMetar.setSlpNo(true);
        } else {
            /*
             * Task 29192: Legacy allows for pressure value to be separated from
             * SLP, whereas documentation suggest it should always be adjoined
             * directly to the end of SLP
             */
            int pressure;
            if (currReportSection.length() > 3) {
                // pressure appended to SLP
                pressure = Integer.parseInt(currReportSection.substring(3));
            } else {
                // pressure is next word
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected SLP value after 'SLP' in METAR remarks, but nothing was present.");
                    return reportIndex;
                }

                if (currReportSection
                        .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    pressure = Integer.parseInt(currReportSection);
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected SLP value after 'SLP' in METAR remarks, got: ["
                                    + currReportSection + "].");
                    reportIndex--;
                    return reportIndex;
                }
            }

            if (pressure >= 550) {
                decodedMetar.setSlp((((float) pressure) / 10) + 900);
            } else {
                decodedMetar.setSlp((((float) pressure) / 10) + 1000);
            }
        }

        return reportIndex;
    }

    /**
     * Parse significant clouds from METAR remarks. Assumes current word started
     * with a valid SIG type. From hmPED_SigClouds.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_SigClouds()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the input character string signals the
     * beginning of Significant Cloud data. If it is, then interrogate
     * subsquent report groups for direction,location, and movement of clouds and
     * store in the Decoded_METAR structure. Return TRUE, if significant clouds
     * is found. Otherwise, return FALSE.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing at.
     */

    private static int parseSignificantClouds(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        String sigCloudType = "";
        /* check for rotor type */
        if (currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.ROTOR_CLD_PREFIX)) {
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];

                if (!currReportSection
                        .equalsIgnoreCase(MetarDecoderUtil.ROTOR_CLD_SUFFIX)) {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected 'CLD' SIG qualifier after 'ROTOR' in METAR remarks, but got: ["
                                    + currReportSection + "].");
                } else {
                    sigCloudType = MetarDecoderUtil.ROTOR_CLD_STRING;

                    // check next word
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        currReportSection = reportArray[reportIndex];
                    } else {
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                        logger.error(
                                "Expected SIG cloud location or direction after SIG type in METAR remarks, but nothing was present.");
                        return reportIndex;
                    }
                }
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected 'CLD' SIG qualifier after 'ROTOR' in METAR remarks, but nothing was present.");
                return reportIndex;
            }
        } else {
            sigCloudType = currReportSection;

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected SIG cloud location or direction after SIG type in METAR remarks, but nothing was present.");
                return reportIndex;
            }
        }

        /*
         * check to see where it is located in reference to the station;
         * optional
         */
        String location = "";
        if (MetarDecoderUtil.VALID_LOC_SYMBOLS.contains(currReportSection)) {
            if (currReportSection.startsWith("V")) {
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected SIG cloud location 'STN' or direction after SIG location qualifier 'V*' in METAR remarks, but nothing was present.");
                    return reportIndex;
                }

                if (currReportSection.equalsIgnoreCase("STN")) {
                    location = "VC STN";
                } else {
                    location = "VC ";
                }
            } else {
                location = "DSTN ";

                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected SIG cloud direction or dissipation after SIG location in METAR remarks,"
                                    + " but nothing was present.");
                    return reportIndex;
                }
            }
        }

        /* Could have dissipation flag. Optional. */
        if (MetarDecoderUtil.validDissipation(currReportSection)) {
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected SIG cloud direction after SIG dissipation in METAR remarks,"
                                + " but nothing was present.");
                return reportIndex;
            }
        }

        /* get the direction */
        String direction = "";
        if (MetarDecoderUtil.validDir(currReportSection)) {
            direction = currReportSection;

            /*
             * check next word. Could have multiple directions, potentially
             * separated by "AND".
             */
            boolean validDir = true;
            while (validDir) {
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if ("AND".equalsIgnoreCase(currReportSection)
                            || MetarDecoderUtil.validDir(currReportSection)) {
                        direction += (" " + currReportSection);
                    } else {
                        logger.info("Invalid SIG direction: ["
                                + currReportSection + "]. Assumed [" + direction
                                + "] is the complete SIG direction.");
                        validDir = false;
                    }
                } else {
                    logger.info(
                            "Expected SIG cloud movement after SIG direction in METAR remarks, but got: ["
                                    + currReportSection + "].");
                    return reportIndex;
                }
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.warn(
                    "Expected SIG cloud direction after SIG location/type in METAR remarks, but got: ["
                            + currReportSection + "]. Error flag set.");
            reportIndex--;
        }

        /* check for movement indicator; optional */
        String movement = "";
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];

            if (MetarDecoderUtil.VALID_MOV_SYMBOLS
                    .contains(currReportSection)) {
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if (MetarDecoderUtil.validDir(currReportSection)) {
                        movement = currReportSection;
                    } else {
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                        logger.warn(
                                "Expected SIG cloud direction after SIG movement in METAR remarks, but got: ["
                                        + currReportSection
                                        + "]. Error flag set.");
                        reportIndex--;
                        return reportIndex;
                    }
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.warn(
                            "Expected SIG cloud direction after SIG movement in METAR remarks, but nothing was present. Error flag set.");
                    return reportIndex;
                }
            } else {
                logger.info(
                        "No SIG cloud movement after SIG direction in METAR remarks. ["
                                + currReportSection
                                + "] is not a valid movement.");
                reportIndex--;
            }
        } else {
            logger.info(
                    "No SIG cloud movement after SIG direction in METAR remarks; no data.");
            return reportIndex;
        }

        /*
         * See if there is space in the METAR object to hold this SIG info.
         */
        boolean insert = false;
        for (SignificantCloud sigCloud : decodedMetar.getSignificantClouds()) {
            if (sigCloud.getSignificantCloudType().isEmpty()) {
                sigCloud.setSignificantCloudType(sigCloudType);
                sigCloud.setSignificantCloudLocation(location);
                sigCloud.setSignificantCloudDirection(direction);
                sigCloud.setSignificantCloudMovement(movement);
                insert = true;
                break;
            }
        }

        if (!insert) {
            logger.warn("No room left in the SIG array for sig cloud: ["
                    + "type: [" + sigCloudType + "], location: [" + location
                    + "], direction: [" + direction + "], movement: ["
                    + movement + "].");
        }
        return reportIndex;
    }

    /**
     * Parse variable sky from METAR remarks. Assume current word started with
     * {@link MetarDecoderUtil#OVERCAST_CLOUDS_STRING},
     * {@link MetarDecoderUtil#BROKEN_CLOUDS_STRING},
     * {@link MetarDecoderUtil#SCATTERED_CLOUDS_STRING}, or
     * {@link MetarDecoderUtil#FEW_CLOUDS_STRING}. From hmPED_VrbSky.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_VrbSky()
     *
     * FUNCTION DESCRIPTION
     * Determione if the curren token is a valid variable sky condition report.
     * If so return TRUE; else FALSE. Store the sky condition in the Decoded_
     * METAR structure.
     *
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseVariableSky(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        String firstSky = currReportSection;

        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected 'V' variable sky condition after first sky condition in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        if (!currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.VARIABLE_DATA_FLAG)) {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected 'V' after first sky condition in METAR remarks, but got: ["
                            + currReportSection + "].");
            reportIndex--;
        }

        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected variable sky condition after first sky condition and 'V' in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        if (currReportSection
                .startsWith(MetarDecoderUtil.OVERCAST_CLOUDS_STRING)
                || currReportSection
                        .startsWith(MetarDecoderUtil.SCATTERED_CLOUDS_STRING)
                || currReportSection
                        .startsWith(MetarDecoderUtil.FEW_CLOUDS_STRING)
                || currReportSection
                        .startsWith(MetarDecoderUtil.BROKEN_CLOUDS_STRING)) {
            decodedMetar.setVariableSkyBelow(firstSky.substring(0, 3));
            decodedMetar.setVariableSkyAbove(currReportSection);

            // first sky condition may have height after qualifier
            if (firstSky.length() > 3) {
                // hundreds of feet
                decodedMetar.setVariableSkyLayerHeight(
                        Integer.parseInt(firstSky.substring(3)) * 100);
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected second variable sky condition after first sky condition and 'V' in METAR remarks, but got: ["
                            + currReportSection + "].");
            reportIndex--;
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse obscuration from METAR remarks. Assume current word is a valid
     * weather symbol. From hmPED_Obscuration.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_Obscur()
     * 
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid Obscuration report. If 
     * so return TRUE; else FALSE. If valid; store the type of obscuration in 
     * the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseObscuration(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        decodedMetar.setObscuration(currReportSection);

        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected sky cover amount after obscuration in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        if (currReportSection
                .matches(MetarDecoderUtil.OBSCUR_SKY_COVER_REGEX)) {
            decodedMetar.setObscurationSkyCondition(
                    currReportSection.substring(0, 3));
            // in hundreds of feet
            decodedMetar.setObscurationHeight(
                    Integer.parseInt(currReportSection.substring(3)) * 100);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected sky cover amount after obscuration in METAR remarks, but ["
                            + currReportSection + "] does not match pattern: ["
                            + MetarDecoderUtil.OBSCUR_SKY_COVER_REGEX + "].");
            reportIndex--;
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse ceiling height from METAR remarks. Assume previous word is
     * {@link MetarDecoderUtil#CEILING_HEIGHT_ABBR}. From hmPEDDcdMTRRemark.c,
     * hmPED_VariableCIG.c, and hmPED_CIG2ndSite.c.
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing from.
     */
    private static int parseCeilingHeight(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            /* Retrieve the min and max ceiling values. */
            if (currReportSection.matches(MetarDecoderUtil.CIG_VAR_REGEX)) {
                /*
                 * CIG section
                 */
                parseVariableCeilingHeight(decodedMetar, currReportSection);
                return reportIndex;
                // end CIG section
            } else if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.FOG_RGD_INDICATOR)
                    || currReportSection
                            .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                /*
                 * CIG second site section
                 */
                return parseSecondSiteCeilingHeight(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end CIG second site
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected min/max ceiling values, or single ceiling value or 'RGD' after CIG in METAR remarks, but ["
                                + currReportSection
                                + "] does not match expected patterns: ["
                                + MetarDecoderUtil.CIG_VAR_REGEX + "] or ["
                                + MetarDecoderUtil.NUM_ONLY_REGEX + "].");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected min/max ceiling values or single value after CIG in METAR remarks, but nothing was present.");
            return reportIndex;
        }
    }

    /**
     * Parse second site ceiling height from METAR remarks. Assume previous word
     * was {@link MetarDecoderUtil#CEILING_HEIGHT_ABBR} and current one is
     * either {@link MetarDecoderUtil#FOG_RGD_INDICATOR} or is all numbers. From
     * hmPED_CIG2ndSite.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_Ceil2ndSite()
     *
     * FUNCTION DESCRIPTION
     * This routine determines whether or not the current group in combination
     * with the next one or more groups is a report of a ceiling at a 
     * secondary site.If so it returns TRUE; else FALSE. Also if values are
     * there, it stores them in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseSecondSiteCeilingHeight(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        if (currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.FOG_RGD_INDICATOR)) {
            // fog, no other values
            decodedMetar.setCigChar(MetarDecoderUtil.FOG_RGD_INDICATOR);
        } else {
            // height
            /*
             * Task 29193: legacy seems to imply height is in 10s of meters, but
             * documentation suggests it is in hundreds of feet.
             */
            decodedMetar.setCig2ndSiteMeters(
                    Integer.parseInt(currReportSection) * 10);

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];

                /*
                 * Task 29239: legacy looks for "RY", but documentation suggests
                 * it should be "RWY"
                 */
                if (currReportSection
                        .startsWith(MetarDecoderUtil.RUNWAY_RY_STRING)
                        || currReportSection.startsWith(
                                MetarDecoderUtil.RUNWAY_RWY_STRING)) {
                    /*
                     * Task 29243: legacy seems to imply that the full location
                     * is just in this string, but documentation suggests the
                     * runway ID is supposed to be the next token
                     */
                    decodedMetar.setCigSecondSiteLoc(currReportSection);
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected runway qualifier after CIG second site value in METAR remarks, but got: ["
                                    + currReportSection + "].");
                    reportIndex--;
                    return reportIndex;
                }
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected runway qualifier after CIG second site value in METAR remarks, but nothing was present.");
                return reportIndex;
            }
        }
        return reportIndex;
    }

    /**
     * Parse variable ceiling height from METAR remarks. Assume previous word
     * was {@link MetarDecoderUtil#CEILING_HEIGHT_ABBR} and the current one
     * matches {@link MetarDecoderUtil#CIG_VAR_REGEX}. From hmPED_VariableCIG.c.
     * 
     * <pre>
     * FUNCTION NAME
     *   hmPED_VariableCIG()
     *
     * FUNCTION DESCRIPTION
     *   Determine whether or not the current group in combination with the next
     *   one or more groups is a report of variable ceiling. If valid return TRUE;
     *   else FALSE. If valid, store the min and max ceiling in the Decoded_METAR
     *   structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parseVariableCeilingHeight(DecodedMetar decodedMetar,
            String currReportSection) {
        String[] splitCig = currReportSection
                .split(MetarDecoderUtil.VARIABLE_DATA_FLAG);

        decodedMetar.setMinCeiling(Integer.parseInt(splitCig[0]));
        decodedMetar.setMaxCeiling(Integer.parseInt(splitCig[1]));
    }

    /**
     * Parse VIRGA from METAR remarks. Assume previous word was
     * {@link MetarDecoderUtil#VIRGA_STRING}. From hmPED_VIRGA.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_VIRGA()
     * 
     * FUNCTION DESCRIPTION
     * Determine if the current token is a VIRGA indicator. If valid return
     * TRUE; else FALSE. Store the direction in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to parse from.
     */

    private static int parseVIRGA(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        String currReportSection;
        decodedMetar.setVirga(true);

        // direction is optional
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];

            if (MetarDecoderUtil.validDir(currReportSection)) {
                decodedMetar.setVirgaDirection(currReportSection);
            } else {
                logger.debug("No direction after VIRGA in METAR remarks. ["
                        + currReportSection + "] is not a valid direction.");
                reportIndex--;
            }
        } else {
            logger.error(
                    "No direction after VIRGA in METAR remarks; nothing was present.");
            reportIndex--;
        }

        return reportIndex;
    }

    /**
     * Parse hail from METAR remarks. Assumed current word was
     * {@link MetarDecoderUtil#HAIL_ABBR}. From hmPED_GR.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_GR()
     *
     * FUNCTION DESCRIPTION
     * Determine if the current token is a valid hail group. If so return TRUE;
     * else FALSE. if valid store the hail size in the Decoded_METAR
     * structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseHail(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            /* check to see if hail size if less the 1/4 */
            int slashIndex;
            if ((slashIndex = currReportSection
                    .indexOf(MetarDecoderUtil.SLASH_DIVIDER)) != -1) {
                if (currReportSection.equalsIgnoreCase(
                        MetarDecoderUtil.HAIL_MINIMUM_SIZE_STRING)) {
                    decodedMetar.setHailSize(
                            MetarDecoderUtil.HAIL_MINIMUM_SIZE_VALUE);
                    decodedMetar.setHail(true);
                } else if (currReportSection
                        .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                    /* check for a fraction for hail diameter */
                    decodedMetar
                            .setHailSize(Float
                                    .parseFloat(currReportSection.substring(0,
                                            slashIndex))
                            / Float.parseFloat(currReportSection
                                    .substring(slashIndex + 1)));
                    decodedMetar.setHail(true);
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error("Unexpected hail size: [" + currReportSection
                            + "]");
                    reportIndex--;
                    return reportIndex;
                }
            } else {
                /* get the whole number value for hail diameter */
                decodedMetar.setHailSize(Float.parseFloat(currReportSection));
                decodedMetar.setHail(true);

                /*
                 * Legacy demands a fraction after the whole number, but
                 * documentation suggests it is optional
                 */
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if (currReportSection
                            .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                        String[] fractionSplit = currReportSection
                                .split(MetarDecoderUtil.SLASH_DIVIDER);
                        decodedMetar.setHailSize(decodedMetar.getHailSize()
                                + (Float.parseFloat(fractionSplit[0])
                                        / Float.parseFloat(fractionSplit[1])));
                    } else {
                        logger.debug(
                                "Hail diameter after GR in METAR remarks is a whole number. ["
                                        + currReportSection
                                        + "] is not a fraction.");
                        reportIndex--;
                    }
                } else {
                    logger.debug(
                            "Hail diameter after GR in METAR remarks is a whole number; no data after whole number.");
                    return reportIndex;
                }
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected hail diameter after GR in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse thunderstorm from METAR remarks. Assume current word was
     * {@link MetarDecoderUtil#THUNDERSTORM_PREFIX}. From hmPED_TSLocation.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_TSLoc()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the input character string signals the
     * beginning of Thuderstorm Loaction. If so store data and return TRUE;
     * else FALSE.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing at.
     */

    private static int parseThunderStorm(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            /*
             * check to see where it is located in reference to the station;
             * optional
             */
            if (MetarDecoderUtil.VALID_LOC_SYMBOLS
                    .contains(currReportSection)) {
                if (currReportSection.startsWith("V")) {
                    /* check to see if near station */
                    // check next word
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        currReportSection = reportArray[reportIndex];
                    } else {
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                        logger.error(
                                "Expected thunderstorm 'STN' or direction after TS V* in METAR remarks, but nothing was present.");
                        return reportIndex;
                    }

                    if (currReportSection.equalsIgnoreCase("STN")) {
                        decodedMetar.setThunderStormLocation("VC STN ");

                        // check next word
                        reportIndex++;
                        if (reportIndex < reportArray.length) {
                            currReportSection = reportArray[reportIndex];
                        } else {
                            decodedMetar.getCmnData().setDecodeStatus(
                                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                            logger.warn(
                                    "Expected thunderstorm direction after TS V* STN in METAR remarks,"
                                            + " but nothing was present. Error flag set.");
                            return reportIndex;
                        }
                    } else {
                        decodedMetar.setThunderStormLocation("VC ");
                    }
                } else {
                    decodedMetar.setThunderStormLocation("DSTN ");

                    // check next word
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        currReportSection = reportArray[reportIndex];
                    } else {
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                        logger.warn(
                                "Expected thunderstorm direction after TS DSTN in METAR remarks,"
                                        + " but nothing was present. Error flag set.");
                        return reportIndex;
                    }
                }
            }

            /* Check for the compass direction of the thunderstorm */
            if (MetarDecoderUtil.validDir(currReportSection)) {
                decodedMetar.setThunderStormDirection(currReportSection);

                /*
                 * check next word. Could have multiple directions, potentially
                 * separated by "AND".
                 */
                boolean validDir = true;
                while (validDir) {
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        currReportSection = reportArray[reportIndex];

                        if ("AND".equalsIgnoreCase(currReportSection)
                                || MetarDecoderUtil
                                        .validDir(currReportSection)) {
                            decodedMetar.setThunderStormDirection(
                                    decodedMetar.getThunderStormDirection()
                                            + " " + currReportSection);
                        } else {
                            logger.info("Invalid Thunderstorm direction: ["
                                    + currReportSection + "]. Assumed ["
                                    + decodedMetar.getThunderStormDirection()
                                    + "] is the complete lightning direction.");
                            validDir = false;
                        }
                    } else {
                        logger.info(
                                "No thunderstorm movement after thunderstorm direction in METAR remarks.");
                        return reportIndex;
                    }
                }
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.warn("Invalid Thunderstorm direction: ["
                        + currReportSection
                        + "]. Error flag set, but continuing to movement.");
            }

            /* Check for MOV indicator */
            if (MetarDecoderUtil.VALID_MOV_SYMBOLS
                    .contains(currReportSection)) {
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if (MetarDecoderUtil.validDir(currReportSection)) {
                        decodedMetar.setThunderStormMovement(currReportSection);
                    } else {
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                        logger.error(
                                "Invalid Thunderstorm movement direction: ["
                                        + currReportSection + "].");
                        reportIndex--;
                        return reportIndex;
                    }
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected thunderstorm direction after movement: ["
                                    + currReportSection
                                    + "] in METAR remarks, but nothing was present.");
                    reportIndex--;
                    return reportIndex;
                }
            } else {
                logger.info(
                        "No thunderstorm movement after thunderstorm direction in METAR remarks: ["
                                + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected thunderstorm location or direction after TS in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse recent weather from METAR remarks. Assumed that the current word
     * matches regex {@link MetarDecoderUtil#RECENT_WX_REGEX}. From
     * hmPED_RecentWX.c.
     * 
     * <pre>
     * MODULE NUMBER: 1
     * MODULE NAME:   hmPED_RecentWX
     * PURPOSE:       This routine is a modification of the hmPED_RecentWX routine
     *                originally supplied with the hmPED METAR decoder. It enhances
     *                the ability of the original routine to parse the recent
     *                weather string sometimes found in the remarks section
     *                of a METAR. These enhancements were necessitated by the
     *                requirements for Enhanced Aviation Verification (EAV) in
     *                build 5.0 of AWIPS.
     *
     *                As an example of the operation of this decoder:
     *                Given the following recent weather string in the remarks
     *                section of a METAR:
     *                       RAB30PLB35RAE40SNB45PLE50SNE55PLB55
     *
     *                This routine will parse it as follows:
     *
     *       wx_type: RA     wx_type: PL     wx_type: SN     wx_type: PL
     *       beg time: HH30  beg time: HH35  beg time: HH45  beg time: HH55
     *       end time: HH40  end time: HH50  end time: HH55  end time: ---- 
     *
     *                (where HH is the hour of the observation).
     *
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     */

    private static void parseRecentWeather(DecodedMetar decodedMetar,
            String currReportSection) {
        // parse all matches for each individual weather event

        Matcher weatherGroupMatcher = Pattern
                .compile(MetarDecoderUtil.SINGLE_RECENT_WX_REGEX)
                .matcher(currReportSection);
        while (weatherGroupMatcher.find()) {
            // single weather event ww..w(B|E)(hh)mm or
            // ww..w(B|E)(hh)mm(E|B)(hh)mm
            String weatherEventString = weatherGroupMatcher.group();

            logger.debug("Parsing weather event group: [" + weatherEventString
                    + "]");

            /*
             * we know from the regex that either E or B is present before the
             * time, and either E or B could be first. Additionally, multiple
             * time pairs or singlets could follow a single recent weather.
             */
            Matcher weatherTimeMatcher = Pattern
                    .compile(MetarDecoderUtil.SINGLE_RECENT_WX_TIME_REGEX)
                    .matcher(weatherEventString);

            /*
             * we know from previous regex matches that at least one group
             * exists, but put check anyway. Need to know first index of first
             * time so we can parse the weather type.
             */
            int firstTimeCharIndex;
            if (!weatherTimeMatcher.find()) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Could not find a first time using regex: ["
                        + MetarDecoderUtil.SINGLE_RECENT_WX_TIME_REGEX
                        + "] for event: [" + weatherEventString
                        + "] from recent weather group: [" + currReportSection
                        + "].");
                break;
            } else {
                firstTimeCharIndex = weatherTimeMatcher.start();
                // reset time matcher for work with the new weather type
                weatherTimeMatcher.reset();
            }

            String weatherType = weatherEventString.substring(0,
                    firstTimeCharIndex);

            if (!MetarDecoderUtil.VALID_RECENT_WX_SYMBOLS
                    .contains(weatherType)) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Unexpected recent weather type: [" + weatherType
                        + "] from event: [" + weatherEventString
                        + "] from recent weather group: [" + currReportSection
                        + "].");
                break;
            }

            logger.debug("Got weather type: [" + weatherType + "]");

            /*
             * now handle all times for this weather type. Track the index of
             * the last weather event manipulated for this instance of this
             * type.
             */
            int lastWeatherUpdated = 0;
            while (weatherTimeMatcher.find()) {
                String weatherTimeGroup = weatherTimeMatcher.group();
                // flag for begin or end time
                boolean begin;
                if (weatherTimeGroup.startsWith("B")) {
                    begin = true;
                } else {
                    // assume end due to regex work
                    begin = false;
                }
                // cut out first character, the rest is the time
                String weatherTime = weatherTimeGroup.substring(1);

                logger.debug("Parsing weather time: [" + weatherTime + "]");
                /*
                 * decode time
                 * 
                 * Either: hhmm, mm
                 */
                int hour;
                int minute;
                if (weatherTime.length() == 2) {
                    /*
                     * mm only, and we know this is all that is left
                     */
                    hour = decodedMetar.getCmnData().getObHour();
                    minute = Integer.parseInt(weatherTime);
                } else {
                    /*
                     * hhmm, and we know this is all that is left due to regex
                     * work
                     */
                    hour = Integer.parseInt(weatherTime.substring(0, 2));
                    minute = Integer.parseInt(weatherTime.substring(2));
                }

                boolean existingWeatherWithoutThisTime = false;
                boolean foundEmptyWeather = false;
                /*
                 * See if there is an existing weather event of the same type
                 * that does not have this time type (begin or end) filled out.
                 * If so, and we did not parse a full period, fill out its time
                 * with the current event. If not, we will create a new event
                 * (if we have the capacity).
                 */
                while (lastWeatherUpdated < decodedMetar
                        .getRecentWeathers().length
                        && !existingWeatherWithoutThisTime
                        && !foundEmptyWeather) {
                    RecentWx recentWx = decodedMetar
                            .getRecentWeathers()[lastWeatherUpdated];
                    if (recentWx.getRecentWeatherName().isEmpty()) {
                        foundEmptyWeather = true;
                    } else if (recentWx.getRecentWeatherName()
                            .equals(weatherType)) {
                        /*
                         * same weather type
                         */
                        if (begin) {
                            // does it have beginning time?
                            if (recentWx.getBeginHour() == Integer.MAX_VALUE) {
                                // it doesn't, so fill out
                                existingWeatherWithoutThisTime = true;
                                recentWx.setBeginHour(hour);
                                recentWx.setBeginMinute(minute);
                                existingWeatherWithoutThisTime = true;
                            }
                        } else {
                            // does it have ending time?
                            if (recentWx.getEndHour() == Integer.MAX_VALUE) {
                                // it doesn't, so fill out
                                existingWeatherWithoutThisTime = true;
                                recentWx.setEndHour(hour);
                                recentWx.setEndMinute(minute);
                                existingWeatherWithoutThisTime = true;
                            }
                        }
                    }

                    if (!existingWeatherWithoutThisTime && !foundEmptyWeather) {
                        lastWeatherUpdated++;
                    }
                }

                if (!existingWeatherWithoutThisTime && !foundEmptyWeather) {
                    /*
                     * could not find a fitting weather to put time in, but went
                     * through the entire array without finding an empty
                     * element. Do not go over maximum weather events
                     */
                    logger.warn("Could not store recent weather: ["
                            + weatherEventString
                            + "]. Not enough space in array.");
                } else if (!existingWeatherWithoutThisTime) {
                    /*
                     * could not find a fitting weather to put time in, and did
                     * not reach the end of the array, so put this new weather
                     * into the array
                     */
                    RecentWx recentWx = new RecentWx();
                    recentWx.setRecentWeatherName(weatherType);
                    if (begin) {
                        /*
                         * The new weather group had only begin time
                         */
                        recentWx.setBeginHour(hour);
                        recentWx.setBeginMinute(minute);
                    } else {
                        /*
                         * The new weather group had only end time
                         */
                        recentWx.setEndHour(hour);
                        recentWx.setEndMinute(minute);
                    }
                    decodedMetar
                            .getRecentWeathers()[lastWeatherUpdated] = recentWx;
                }
            }
        }
    }

    /**
     * Parse lightning frequency from METAR remarks. Assumed current word was
     * either {@link MetarDecoderUtil#OCCASIONAL_LIGHTNING_STRING},
     * {@link MetarDecoderUtil#FREQUENT_LIGHTNING_STRING}, or
     * {@link MetarDecoderUtil#CONSTANT_LIGHTNING_STRING} . From
     * hmPED_LTGfreq.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_HourlyPrecip()
     *  
     * FUNCTION DESCRIPTION
     * Determine whether or not the current and subsequent groups from the
     * METAR report make up a valid report of lightning.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseLightningFrequency(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        if (currReportSection.equalsIgnoreCase(
                MetarDecoderUtil.OCCASIONAL_LIGHTNING_STRING)) {
            decodedMetar.setOccasionalLightning(true);
            // move to next word
            reportIndex++;
        } else if (currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.FREQUENT_LIGHTNING_STRING)) {
            decodedMetar.setFrequentLightning(true);
            // move to next word
            reportIndex++;
        } else if (currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.CONSTANT_LIGHTNING_STRING)) {
            decodedMetar.setConstantLightning(true);
            // move to next word
            reportIndex++;
        } else if (!currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.LIGHTNING_TYPE_PREFIX)) {
            /*
             * Legacy decoder does not allow "LTG" to begin the section, though
             * this does seem allowed in reports.
             */
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Entered parsing of lightning frequency method, but report section: ["
                            + currReportSection
                            + "] does not match any expected prefixes.");
            return reportIndex;
        }

        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected lightning data after OCNL or FRQ or CONS or LTG in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        if (!currReportSection
                .startsWith(MetarDecoderUtil.LIGHTNING_TYPE_PREFIX)) {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected lightning data to start with a prefix 'LTG', "
                            + "either on its own or after OCNL or FRQ or CONS, in METAR remarks, but got: ["
                            + currReportSection + "].");
            reportIndex--;
            return reportIndex;
        }

        // lightning type
        if (currReportSection.startsWith("LTGCG")) {
            decodedMetar.setCgLtg(true);
        } else if (currReportSection.startsWith("LTGIC")) {
            decodedMetar.setIcLtg(true);
        } else if (currReportSection.startsWith("LTGCC")) {
            decodedMetar.setCcLtg(true);
        } else if (currReportSection.startsWith("LTGCA")) {
            decodedMetar.setCaLtg(true);
        } else if (!currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.LIGHTNING_TYPE_PREFIX)) {
            /*
             * Legacy decoder does not allow "LTG" by itself, however this does
             * seem allowed in reports.
             */
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Unexpected lightning type: [" + currReportSection + "].");
            reportIndex--;
        }

        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected lightning location data after type in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        // lightning location
        if (currReportSection.equalsIgnoreCase("DSNT")
                || currReportSection.equalsIgnoreCase("DSTN")) {
            decodedMetar.setDsntLtg(true);
        } else if (currReportSection.equalsIgnoreCase("AP")) {
            decodedMetar.setApLtg(true);
        } else if (currReportSection.equalsIgnoreCase("VCY")) {
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];

                if (currReportSection.equalsIgnoreCase("STN")) {
                    decodedMetar.setVcyStnLtg(true);
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Expected 'STN' after 'VCY' lightning location data in METAR remarks, but got: ["
                                    + currReportSection + "].");
                    reportIndex--;
                }
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected 'STN' after 'VCY' lightning location data in METAR remarks, but nothing was present.");
                return reportIndex;
            }
        } else {
            logger.info("[" + currReportSection
                    + "] is not a valid lightning location. Assumed location is not in this report.");
            reportIndex--;
        }

        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected lightning direction data after location in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        if (MetarDecoderUtil.validDir(currReportSection)) {
            decodedMetar.setLightningDirection(currReportSection);
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Unexpected lightning direction: [" + currReportSection
                    + "].");
            reportIndex--;
        }

        return reportIndex;
    }

    /**
     * Parse dispatch visual range from METAR remarks. Assume word began with
     * {@link MetarDecoderUtil#DISPATCH_VISUAL_RANGE_STRING}. From hmPED_DVR.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_DVR()
     * 
     * FUNCTION DESCRIPTION
     * The is routine determines is the Dispatch Visual Range remark is pressnt.
     * If so it returns TRUE; else FALSE. Also if the remark is there it stores
     * all relevant data into the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static void parseDispatchVisualRange(DecodedMetar decodedMetar,
            String currReportSection) {
        if (!currReportSection
                .startsWith(MetarDecoderUtil.DISPATCH_VISUAL_RANGE_PREFIX)) {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Invalid Dispatch Visual Range word: ["
                    + currReportSection + "] does not start with DVR/.");
        } else if (!currReportSection.endsWith(MetarDecoderUtil.FEET_STRING)) {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error("Invalid Dispatch Visual Range word: ["
                    + currReportSection + "] does not end with FT.");
        } else {

            int ftIndex = currReportSection.length() - 2;

            if (currReportSection.startsWith(
                    MetarDecoderUtil.DISPATCH_VISUAL_RANGE_PLUS_PREFIX)) {
                /* get the max dvr value */
                decodedMetar.getDvr().setAboveMaxDVR(true);
            } else if (currReportSection.startsWith(
                    MetarDecoderUtil.DISPATCH_VISUAL_RANGE_MINUS_PREFIX)) {
                /* get the min dvr value */
                decodedMetar.getDvr().setBelowMinDVR(true);
            }

            /* get the visual range */
            try {
                int vIndex;
                if ((vIndex = currReportSection.indexOf(
                        MetarDecoderUtil.VARIABLE_DATA_FLAG, 4)) != -1) {
                    // variable range
                    decodedMetar.getDvr().setVariableVisualRange(true);

                    decodedMetar.getDvr().setMinVisualRange(Integer
                            .parseInt(currReportSection.substring(5, vIndex)));

                    decodedMetar.getDvr().setMaxVisualRange(Integer.parseInt(
                            currReportSection.substring(vIndex + 1, ftIndex)));
                } else {
                    if (decodedMetar.getDvr().isAboveMaxDVR()
                            || decodedMetar.getDvr().isBelowMinDVR()) {
                        decodedMetar.getDvr().setVisRange(Integer.parseInt(
                                currReportSection.substring(6, ftIndex)));
                    } else {
                        decodedMetar.getDvr().setVisRange(Integer.parseInt(
                                currReportSection.substring(5, ftIndex)));
                    }
                }
            } catch (NumberFormatException e) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Invalid dispatch visual range value: ["
                        + currReportSection + "].", e);
            }
        }
    }

    /**
     * Parse various visibility data from METAR remarks. Assumed current word
     * was {@link MetarDecoderUtil#VISIBILITY_PREFIX}. From
     * hmPED_DcdMTRRemark.c, hmPED_VariableVsby.c, hmPED_SecondVsby.c, and
     * hmPED_Vsby2ndSite.c.
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing from.
     */

    private static int parseOtherVisibility(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            // check ahead to another word
            int nextIndex = reportIndex + 1;
            String nextWord = "";
            if (nextIndex < reportArray.length) {
                nextWord = reportArray[nextIndex];
            }

            if (MetarDecoderUtil.validDir(currReportSection)) {
                // the value is a valid direction
                /*
                 * Sector visibility
                 */
                return parseSectorVisibility(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end sector visibility
            } else if (currReportSection
                    .matches(MetarDecoderUtil.CORE_VARIABLE_VISIBILITY_REGEX)
                    || (currReportSection
                            .matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                            && nextWord.matches(
                                    MetarDecoderUtil.CORE_VARIABLE_VISIBILITY_REGEX))) {
                return parseVariableVisibility(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end variable visibility
            } else if ((currReportSection
                    .matches(MetarDecoderUtil.FRACTION_REGEX)
                    && (nextWord.startsWith(MetarDecoderUtil.RUNWAY_RY_STRING)
                            || nextWord.startsWith(
                                    MetarDecoderUtil.RUNWAY_RWY_STRING)))
                    || (currReportSection
                            .matches(MetarDecoderUtil.NUM_ONLY_REGEX)
                            && (nextWord
                                    .matches(MetarDecoderUtil.FRACTION_REGEX)
                                    || (nextWord.startsWith(
                                            MetarDecoderUtil.RUNWAY_RY_STRING)
                                            || nextWord.startsWith(
                                                    MetarDecoderUtil.RUNWAY_RWY_STRING))))) {
                /*
                 * Task 29239: legacy expects "RY" prefix, but documentation
                 * expects "RWY"
                 */
                return parseSecondSiteVisibility(decodedMetar, reportArray,
                        reportIndex, currReportSection);
                // end second site visibility
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Following the VIS prefix, the next words ["
                        + currReportSection + "] and [" + nextWord
                        + "] do not match a pattern for sector, second site, or variable visibility.");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected visibility data after 'VIS' in METAR remarks, but nothing was present.");
            return reportIndex;
        }
    }

    /**
     * Parse second site visibility from METAR remarks. Assumed the word
     * {@link MetarDecoderUtil#VISIBILITY_PREFIX} is followed by either a whole
     * number and then a fraction, a whole number and then some runway location,
     * or a fraction and then some runway location. From hmPED_Vsby2ndSite.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_Vsby2ndSite()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the current group and subsequent groups from
     * the METAR report make up a valid report of visibility at a secondary
     * site. If valid return TRUE; else FALSE. If valid; store the values in 
     * the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseSecondSiteVisibility(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        /*
         * The value is a number and the next is the location or a fraction, or
         * the value is a fraction and the next is the location
         */
        /*
         * Second site visibility
         */
        // could start with whole number
        float secondSiteVisibility = 0f;
        if (currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            secondSiteVisibility = Float.parseFloat(currReportSection);

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected second site visibility data after whole number in METAR remarks, but nothing was present.");
                return reportIndex;
            }
        }

        // fractional part
        if (currReportSection.matches(MetarDecoderUtil.FRACTION_REGEX)) {
            String[] fractionSplit = currReportSection
                    .split(MetarDecoderUtil.SLASH_DIVIDER);

            decodedMetar.setVsby2ndSite(
                    secondSiteVisibility + (Float.parseFloat(fractionSplit[0])
                            / Float.parseFloat(fractionSplit[1])));

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected second site visibility location data after fractional in METAR remarks, but nothing was present.");
                return reportIndex;
            }
        }

        // runway location number
        /*
         * Task 29243: some METAR documentation suggests the runway number could
         * be separated by a space from the prefix, but legacy does not handle
         * this
         */
        if (currReportSection.startsWith(MetarDecoderUtil.RUNWAY_RY_STRING)
                || currReportSection
                        .startsWith(MetarDecoderUtil.RUNWAY_RWY_STRING)) {
            if (currReportSection
                    .matches(MetarDecoderUtil.ALPHANUMERIC_REGEX)) {
                decodedMetar.setVsby2ndSiteLoc(currReportSection);
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Runway value for second site visibility: ["
                        + currReportSection + "] was not alphanumeric.");
                reportIndex--;
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected a runway value for second site visibility after visibility data in METAR remarks, but got: ["
                            + currReportSection + "].");
            reportIndex--;
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse variable visibility from METAR remarks. Assumed that the previous
     * word was {@link MetarDecoderUtil#VISIBILITY_PREFIX} and after that is
     * either a variable visibility word, or a whole number and then the
     * variable visibility word. From hmPED_VariableVsby.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_VariableVsby()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the current and subsequent groups from the
     * METAR report make up a valid report of variable prevailing visibility.
     * If valid return TRUE; else FALSE. If valid, store the max and min
     * visibility values in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseVariableVisibility(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        /*
         * Format xVy where x and/or y can be in format "A" or "B/C" or "A B/C"
         */
        /*
         * the value matches the variable visibility pattern, or the value is a
         * number and the next value matches the pattern
         */
        /*
         * Variable visibility
         */
        /*
         * first value may be whole number or fractional minimum visibility, or
         * the core visibility pattern
         */
        if (currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            decodedMetar.setMinVsby(Float.parseFloat(currReportSection));

            /*
             * advance to next section, which we know from the enclosing "if"
             * calling this method should exist and is valid, but check anyway
             */
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                logger.error(
                        "Could not get core variable visibility word. Unexpected end of the report.");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                return reportIndex;
            }
        }

        /*
         * we know from the enclosing "if" that this section is the core
         * visibility pattern
         */
        String[] coreVisibilityArray = currReportSection
                .split(MetarDecoderUtil.VARIABLE_DATA_FLAG);
        // finish min visibility
        String coreMinVisibility = coreVisibilityArray[0];
        if (coreMinVisibility.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            // whole number
            decodedMetar.setMinVsby(Float.parseFloat(coreMinVisibility));
        } else if (coreMinVisibility.matches(MetarDecoderUtil.FRACTION_REGEX)) {
            // fraction
            String[] minVisSplit = coreMinVisibility
                    .split(MetarDecoderUtil.SLASH_DIVIDER);
            decodedMetar.setMinVsby(decodedMetar.getMinVsby()
                    + (Float.parseFloat(minVisSplit[0])
                            / Float.parseFloat(minVisSplit[1])));
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Min visibility: [" + Arrays.toString(coreVisibilityArray)
                            + "] in core portion of variable visibility: ["
                            + currReportSection
                            + "] is neither all numbers nor a fraction.");
            reportIndex--;
            return reportIndex;
        }

        // start max visibility
        String coreMaxVisibility = coreVisibilityArray[1];
        if (coreMaxVisibility.matches(MetarDecoderUtil.FRACTION_REGEX)) {
            // fractional
            String[] maxVisSplit = coreMaxVisibility
                    .split(MetarDecoderUtil.SLASH_DIVIDER);
            decodedMetar.setMaxVsby(Float.parseFloat(maxVisSplit[0])
                    / Float.parseFloat(maxVisSplit[1]));
        } else {
            // whole number
            decodedMetar.setMaxVsby(Float.parseFloat(coreMaxVisibility));

            // get fraction, which is optional
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];

                if (currReportSection
                        .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                    String[] maxVisSplit = currReportSection
                            .split(MetarDecoderUtil.SLASH_DIVIDER);
                    decodedMetar.setMaxVsby(decodedMetar.getMaxVsby()
                            + (Float.parseFloat(maxVisSplit[0])
                                    / Float.parseFloat(maxVisSplit[1])));
                } else {
                    logger.debug("No fractional max visibility data: ["
                            + currReportSection + "] in METAR remarks.");
                    reportIndex--;
                }

            } else {
                logger.debug(
                        "No fractional max visibility data in METAR remarks.");
                reportIndex--;
            }
        }

        return reportIndex;
    }

    /**
     * Parse sector visibility from METAR remarks. Assumed that previous words
     * were {@link MetarDecoderUtil#VISIBILITY_PREFIX} followed by some valid
     * direction. From hmPED_SectorVsby.c.
     * 
     * <pre>
     * FUNCTION NAME
     *   hmPED_SectorVsby()
     *
     * FUNCTION DESCRIPTION
     *   Determine if the current token is a sector visibility report. If so
     *   return TRUE; else FALSE. If this is a sector visibility report store
     *   the direction and visibility in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseSectorVisibility(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        // copy only first two characters of direction
        if (currReportSection.length() == 1) {
            decodedMetar.setSectorVsbyDir(currReportSection);
        } else {
            decodedMetar.setSectorVsbyDir(currReportSection.substring(0, 2));
        }

        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected sector visibility data after direction in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        /*
         * visibility is either a fraction, a whole number and then a fraction,
         * or just a whole number
         */
        if (currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            // whole number, which may be followed by a fraction
            decodedMetar.setSectorVsby(Float.parseFloat(currReportSection));

            // check next word, optional fraction
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];

                if (currReportSection
                        .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                    String[] fractionSplit = currReportSection
                            .split(MetarDecoderUtil.SLASH_DIVIDER);
                    decodedMetar.setSectorVsby(decodedMetar.getSectorVsby()
                            + (Float.parseFloat(fractionSplit[0])
                                    / Float.parseFloat(fractionSplit[1])));
                } else {
                    logger.debug(
                            "Only whole number present for sector visibility.");
                    reportIndex--;
                }
            } else {
                logger.debug(
                        "Only whole number present for sector visibility. End of remarks.");
                return reportIndex;
            }
        } else if (currReportSection.matches(MetarDecoderUtil.FRACTION_REGEX)) {
            // fraction
            String[] fractionSplit = currReportSection
                    .split(MetarDecoderUtil.SLASH_DIVIDER);
            decodedMetar.setSectorVsby(Float.parseFloat(fractionSplit[0])
                    / Float.parseFloat(fractionSplit[1]));
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected sector visibility data after direction in METAR remarks, but got: ["
                            + currReportSection + "].");
            reportIndex--;
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse surface visibility from METAR remarks. Assume current word was
     * {@link MetarDecoderUtil#SURFACE_VISIBILITY_PREFIX}. From
     * hmPED_SurfaceVsby.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_SurfaceVsby()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the current and subsequent groups from the
     * METAR report make up a valid report of surface visibility. If so return
     * TRUE; else FALSE. If a valid report, store the amount of visibility
     * present in the Decoded_METAR structure.
     * 
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseSurfaceVisibility(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        // ensure next word is VIS
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected 'VIS' after 'SFC' in METAR remarks, but nothing was present.");
            return reportIndex;
        }

        if (!currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.VISIBILITY_PREFIX)) {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected 'VIS' after 'SFC' in METAR remarks, but got: ["
                            + currReportSection + "].");
            reportIndex--;
        }

        // visibility data
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "No surface visibility data after SFC VIS prefix in METAR remarks.");
            return reportIndex;
        }

        // expect either a lone digits or a fraction
        if (currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            // lone digits
            decodedMetar.setSfcVsby(Float.parseFloat(currReportSection));

            // optionally followed by fractional section
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];

                if (currReportSection
                        .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                    String[] fractionArray = currReportSection
                            .split(MetarDecoderUtil.SLASH_DIVIDER);
                    decodedMetar.setSfcVsby(decodedMetar.getSfcVsby()
                            + Float.parseFloat(fractionArray[0])
                                    / Float.parseFloat(fractionArray[1]));
                } else {
                    logger.debug("[" + currReportSection
                            + "] is not fractional part after whole number present in METAR remarks after SFC VIS prefix.");
                    reportIndex--;
                }
            } else {
                logger.debug(
                        "No fractional part after whole number present in METAR remarks after SFC VIS prefix. End of remarks.");
                return reportIndex;
            }
        } else if (currReportSection.matches(MetarDecoderUtil.FRACTION_REGEX)) {
            // fraction
            String[] fractionArray = currReportSection
                    .split(MetarDecoderUtil.SLASH_DIVIDER);
            decodedMetar.setSfcVsby(Float.parseFloat(fractionArray[0])
                    / Float.parseFloat(fractionArray[1]));
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Invalid surface visibility data after SFC VIS prefix in METAR remarks: ["
                            + currReportSection + "].");
            reportIndex--;
            return reportIndex;
        }

        return reportIndex;
    }

    /**
     * Parse tower visibility from METAR remarks. Assumed current word was
     * {@link MetarDecoderUtil#TOWER_PREFIX} From hmPED_TowerVsby.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_TowerVsby()
     * 
     * FUNCTION DESCRIPTION
     * Determine whether or not the current and subsquent groups from the
     * METAR report make up a valid report of tower visibility. If valid, return
     * TRUE; else FALSE. If a valid report, store the visibility value in the
     * Decoded_METAR structure.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing from.
     */

    private static int parseTowerVisibility(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // ensure next word is VIS
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            if (!currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.VISIBILITY_PREFIX)) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected 'VIS' after 'TWR' in METAR remarks, but got: ["
                                + currReportSection + "].");
                reportIndex--;
            }

            // visibility data
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "No tower visibility data after TWR VIS prefix in METAR remarks.");
                return reportIndex;
            }

            // expect either a lone digits or a fraction
            if (currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                // lone digits
                decodedMetar.setTwrVsby(Float.parseFloat(currReportSection));

                // optionally followed by fractional section
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if (currReportSection
                            .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                        String[] fractionArray = currReportSection
                                .split(MetarDecoderUtil.SLASH_DIVIDER);
                        decodedMetar.setTwrVsby(decodedMetar.getTwrVsby()
                                + Float.parseFloat(fractionArray[0])
                                        / Float.parseFloat(fractionArray[1]));
                    } else {
                        logger.debug("[" + currReportSection
                                + "] is not fractional part after whole number present in METAR remarks after TWR VIS prefix.");
                        reportIndex--;
                    }
                } else {
                    logger.debug(
                            "No fractional part after whole number present in METAR remarks after TWR VIS prefix.");
                    return reportIndex;
                }

            } else if (currReportSection
                    .matches(MetarDecoderUtil.FRACTION_REGEX)) {
                // fraction
                String[] fractionArray = currReportSection
                        .split(MetarDecoderUtil.SLASH_DIVIDER);
                decodedMetar.setTwrVsby(Float.parseFloat(fractionArray[0])
                        / Float.parseFloat(fractionArray[1]));
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Invalid tower visibility data after TWR VIS prefix in METAR remarks: ["
                                + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            }

            return reportIndex;
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected 'VIS' after 'TWR' in METAR remarks, but nothing was present.");
            return reportIndex;
        }
    }

    /**
     * Parse wind shift from METAR remarks. Assumed current word was
     * {@link MetarDecoderUtil#WIND_SHIFT_STRING}. From hmPED_WindShift.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_WindShift()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the current and subsequent groups from the 
     * METAR report make up a valid report of wind shift and frontal passage,
     * if included. If valid return TRUE; else FALSE. If valid, store all data
     * in the Decoded_METAR structure.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing from.
     */

    private static int parseWindShift(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            if (!currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expecting time information after WSHFT prefix, but got: ["
                                + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            } else {

                // time, may be hour and minute or just minute
                if (currReportSection.length() > 2) {
                    // hour and mintue present
                    decodedMetar.setWshfTimeHour(Integer
                            .parseInt(currReportSection.substring(0, 2)));
                    decodedMetar.setWshfTimeMinute(
                            Integer.parseInt(currReportSection.substring(2)));
                } else {
                    // only minute present
                    decodedMetar.setWshfTimeMinute(
                            Integer.parseInt(currReportSection));
                }

                // optional FROPA (frontal passage) flag
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if (currReportSection.equalsIgnoreCase("FROPA")) {
                        decodedMetar.setwShftFroPa(true);
                    } else {
                        logger.debug("[" + currReportSection
                                + "] is not FROPA flag in METAR remarks after WSHFT prefix.");
                        reportIndex--;
                        return reportIndex;
                    }
                } else {
                    logger.debug(
                            "No FROPA flag present in METAR remarks after WSHFT prefix.");
                    return reportIndex;
                }
            }

            return reportIndex;
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "No wind shift information present in METAR remarks after WSHFT prefix.");
            return reportIndex;
        }
    }

    /**
     * Parse peak wind from METAR remarks. Assumed current word was
     * {@link MetarDecoderUtil#PEAKWIND_PREFIX}. From hmPED_PeakWind.c.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_PeakWind()
     * 
     * FUNCTION DESCRIPTION
     * Determine whether or not the current character string and subsequent
     * groups from the METAR report make up a valid report of peak wind.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing from.
     */

    private static int parsePeakWind(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        // ensure next work is WND
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            String currReportSection = reportArray[reportIndex];

            if (!currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.PEAK_WIND_SUFFIX)) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Expected 'WND' after 'PK' in remarks section of METAR, but instead got: ["
                                + currReportSection + "].");
            } else {
                // get peak wind information
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "No peak wind information present in METAR remarks after PK WND prefix.");
                    reportIndex--;
                    return reportIndex;
                }
            }

            if (!currReportSection.matches(MetarDecoderUtil.PEAK_WIND_REGEX)) {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Peak wind section: [" + currReportSection
                        + "] does not match regex: ["
                        + MetarDecoderUtil.PEAK_WIND_REGEX + "].");
                reportIndex--;
                return reportIndex;
            }

            String[] peakWindArray = currReportSection
                    .split(MetarDecoderUtil.SLASH_DIVIDER);

            // direction and speed
            String dirSpeed = peakWindArray[0];
            decodedMetar
                    .setPkWndDir(Integer.parseInt(dirSpeed.substring(0, 3)));
            decodedMetar.setPkWndSpeed(Integer.parseInt(dirSpeed.substring(3)));

            // time, may be hour and minute or just minute
            String time = peakWindArray[1];
            if (time.length() > 2) {
                // hour and minute present
                decodedMetar
                        .setPkWndHour(Integer.parseInt(time.substring(0, 2)));
                decodedMetar
                        .setPkWndMinute(Integer.parseInt(time.substring(2)));
            } else {
                // only minute present
                decodedMetar
                        .setPkWndHour(decodedMetar.getCmnData().getObHour());
                decodedMetar.setPkWndMinute(Integer.parseInt(time));
            }

            return reportIndex;
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Expected 'WND' after 'PK' in remarks section of METAR, but nothing was present.");
            reportIndex--;
            return reportIndex;
        }
    }

    /**
     * Parse tornadic activity from METAR remarks. From hmPED_TornadicActiv.c.
     * Assume current word is either {@link MetarDecoderUtil#TORNADO_STRING},
     * {@link MetarDecoderUtil#WATERSPOUT_STRING}, or
     * {@link MetarDecoderUtil#FUNNEL_STRING}.
     * 
     * <pre>
     * FUNCTION NAME
     * hmPED_TornadicActiv()
     *
     * FUNCTION DESCRIPTION
     * Determine whether or not the input character string signals the
     * beginning of Tornadic Activity data. If it is, then interrogate
     * subsquent report groups for time, location, and movement of tornado and
     * store in the Decoded_METAR structure. Return TRUE, if tornadic activity
     * is found. Otherwise, return FALSE.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @param currReportSection
     * @return report index to start parsing from.
     */

    private static int parseTornadicActivity(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex, String currReportSection) {
        if (currReportSection
                .equalsIgnoreCase(MetarDecoderUtil.FUNNEL_STRING)) {
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                logger.error(
                        "Invalid tornadic activity. No information after the word 'FUNNEL'.");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                return reportIndex;
            }

            if (currReportSection
                    .equalsIgnoreCase(MetarDecoderUtil.CLOUD_STRING)) {
                // funnel cloud
                decodedMetar
                        .setTornadicType(MetarDecoderUtil.FUNNEL_CLOUD_STRING);
            } else {
                // invalid Funnel
                logger.error(
                        "Expected 'CLOUD' to follow 'FUNNEL' in METAR remarks, but got ["
                                + currReportSection + "]");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                reportIndex--;
            }
        } else {
            // Tornado and waterspout are valid types on their own, but
            // need more data
            decodedMetar.setTornadicType(currReportSection);
        }

        // need timing, location, and movement data
        // check next word
        reportIndex++;
        if (reportIndex < reportArray.length) {
            currReportSection = reportArray[reportIndex];
        } else {
            logger.error(
                    "Invalid tornadic activity. No information after tornadic type.");
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            return reportIndex;
        }

        // timing
        if (currReportSection.startsWith("B")
                || currReportSection.startsWith("E")) {
            // beginning or ending time, or both

            if (currReportSection.startsWith("B")) {
                // beginning time
                // time is either 2 digits (minutes only) or 4 digits
                // (hour and minutes)
                int beginTimeDigits = 2;
                if ((currReportSection.length() >= 5)
                        && (currReportSection.substring(1, 5)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX))) {
                    // four digit time
                    decodedMetar.setbTornadicHour(Integer
                            .parseInt(currReportSection.substring(1, 3)));
                    decodedMetar.setbTornadicMinute(Integer
                            .parseInt(currReportSection.substring(3, 5)));
                    beginTimeDigits = 4;
                } else if (currReportSection.length() >= 3
                        && currReportSection.substring(1, 3)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    // two digit time
                    decodedMetar.setbTornadicMinute(Integer
                            .parseInt(currReportSection.substring(1, 3)));
                    beginTimeDigits = 2;
                } else {
                    logger.error("Invalid Tornadic Begin Time: ["
                            + currReportSection + "].");
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                }

                // cut out beginning portion, to potentially parse
                // ending portion
                currReportSection = currReportSection
                        .substring(beginTimeDigits + 1);
            }

            if (currReportSection.startsWith("E")) {
                // ending time
                // time is either 2 digits (minutes only) or 4 digits
                // (hour and minutes)
                if ((currReportSection.length() >= 5)
                        && (currReportSection.substring(1, 5)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX))) {
                    // four digit time
                    decodedMetar.seteTornadicHour(Integer
                            .parseInt(currReportSection.substring(1, 3)));
                    decodedMetar.seteTornadicMinute(Integer
                            .parseInt(currReportSection.substring(3, 5)));
                } else if (currReportSection.length() >= 3
                        && currReportSection.substring(1, 3)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    // two digit time
                    decodedMetar.seteTornadicMinute(Integer
                            .parseInt(currReportSection.substring(1, 3)));
                } else {
                    logger.error("Invalid Tornadic End Time: ["
                            + currReportSection + "].");
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                }
            }

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                logger.error(
                        "Completed remarks decoding after of decoding tornadic time information, but expected location data.");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                return reportIndex;
            }
        }

        /* Get the location of the current condition. */
        if (MetarDecoderUtil.VALID_LOC_SYMBOLS.contains(currReportSection)) {
            if (currReportSection
                    .startsWith(MetarDecoderUtil.VARIABLE_DATA_FLAG)) {
                // check next word, which is optional
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];

                    if (currReportSection.equalsIgnoreCase("STN")) {
                        decodedMetar.setTornadicLoc("VC STN");

                        // check next word
                        reportIndex++;
                        if (reportIndex < reportArray.length) {
                            currReportSection = reportArray[reportIndex];
                        } else {
                            decodedMetar.getCmnData().setDecodeStatus(
                                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                            logger.error(
                                    "Completed remarks decoding after of decoding tornadic vicinity of station location information, but expected direction.");
                            return reportIndex;
                        }
                    } else {
                        decodedMetar.setTornadicLoc("VC ");
                    }

                } else {
                    decodedMetar.setTornadicLoc("VC ");
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Completed remarks decoding after of decoding tornadic vicinity location information, but expected direction.");
                    return reportIndex;
                }
            } else {
                decodedMetar.setTornadicLoc("DTSN ");
                // check next word
                reportIndex++;
                if (reportIndex < reportArray.length) {
                    currReportSection = reportArray[reportIndex];
                } else {
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.error(
                            "Completed remarks decoding after of decoding tornadic distant location information, but expected direction.");
                    return reportIndex;
                }
            }
        } else if (currReportSection.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
            /* Check for a numerical location as well */
            decodedMetar.setTornadicLocNum(Integer.parseInt(currReportSection));

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Completed remarks decoding after of decoding tornadic numbered location information, but expected direction.");
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Invalid tornadic location: [" + currReportSection + "].");
        }

        /* check for Direction of event */
        if (MetarDecoderUtil.validDir(currReportSection)) {
            decodedMetar.setTornadicDir(currReportSection);

            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Completed remarks decoding after of decoding tornadic direction information, but expected movement.");
                return reportIndex;
            }
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.error(
                    "Invalid tornadic direction: [" + currReportSection + "].");
        }

        /* Check for movement of the event */
        if (MetarDecoderUtil.VALID_MOV_SYMBOLS.contains(currReportSection)) {
            // next word has actual movement string
            // check next word
            reportIndex++;
            if (reportIndex < reportArray.length) {
                currReportSection = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error(
                        "Completed remarks decoding after of decoding tornadic movement prefix, but expected direction.");
                currReportSection = null;
            }

            if (MetarDecoderUtil.validDir(currReportSection)) {
                decodedMetar.setTornadicMov(currReportSection);
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.error("Invalid tornadic movement direction: ["
                        + currReportSection + "].");
                reportIndex--;
                return reportIndex;
            }
        } else {
            logger.info(
                    "Invalid tornadic movement: [" + currReportSection + "].");
            reportIndex--;
            return reportIndex;
        }

        return reportIndex;
    }
}
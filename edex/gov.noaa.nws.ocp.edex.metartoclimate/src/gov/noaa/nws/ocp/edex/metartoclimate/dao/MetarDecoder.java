/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateMetarDecodingException;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.CloudConditions;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.DecodedMetar;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.RunwayVisRange;

/**
 * Decode main body of METAR report.
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
 * 10 FEB 2017  28609      amoore      Initial creation.
 * 21 FEB 2017  28609      amoore      Bug fixes from testing. Better logging.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 25 APR 2017  33104      amoore      Logging cleanup.
 * 02 MAY 2017  33104      amoore      Refactor queries into constants. Use query maps.
 *                                     Address FindBugs.
 * 09 MAY 2017  33104      amoore      If can't find station ID for station code, log
 *                                     warning and return, don't throw an exception.
 * 11 MAY 2017  33104      amoore      Parameterization. Add TODOs.
 * 23 MAY 2017  33104      amoore      Fix casting for report ID counter.
 * 24 MAY 2017  33104      amoore      Fix null pointer for present weather elements array access.
 * 24 MAY 2017  33104      amoore      Fix bad parameter name.
 * 24 MAY 2017  33104      amoore      Fix wrong table name.
 * 24 MAY 2017  33104      amoore      Fix latent Legacy bug with violating DB keys on duplicate reports.
 * 24 MAY 2017  33104      amoore      Safer use of sequence ID for fss_report.
 * 25 MAY 2017  33104      amoore      Lightning section does not need frequency prefix or a specific type.
 *                                     Added "W-NE" as a valid direction. Added more comments.
 * 25 MAY 2017  33104      amoore      Workaround for sequence ID for fss_report. Get ID separately prior
 *                                     to insertion. Fix Recent Weather regex's after testing a report.
 *                                     Weather time groupings may have begin and end time together.
 * 02 JUN 2017  33104      amoore      Re-arrange remarks checking to check for simple flags first. Some
 *                                     flags share prefix characters with more complex sections, so it is
 *                                     better to rule out the flags first than to get an error parsing
 *                                     a false-positive for a complex part.
 * 07 JUN 2017  33104      amoore      Add "SE-SW" valid direction. Lightning location is optional.
 *                                     Add "TS" as valid weather type. Weather type string could start
 *                                     with end time and end with begin time.
 * 13 JUN 2017  33104      amoore      Add "UP" valid weather type, and compare weather types from
 *                                     http://weather.cod.edu/notes/metar.html. Remove duplicates
 *                                     weather types. Adjust RVR regex. Recent weather can have more
 *                                     than two times after each weather marker. Variable visibility
 *                                     can start with a fraction and end with a whole number.
 * 19 JUN 2017  33104      amoore      Downgrade many missing main body items to warning. Make direction 
 *                                     able to be any combination of cardinal directions separated by 
 *                                     hyphens.
 * 07 JUL 2017  33104      amoore      Split class.
 * 20 JUL 2017  33104      amoore      SIG/lightning directions can be several directions with "AND" between
 *                                     them. SIG can have a dissipation flag after location/type.
 * 07 SEP 2017  37754      amoore      Rename and split. Reorganize related methods for METAR decoding
 *                                     and storing.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public final class MetarDecoder {
    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MetarDecoder.class);

    /**
     * Decode a METAR report and assign values to the given metar object from
     * parsing the given report's data. Based on hmPED_decodeMetar.c.
     * 
     * @param decodedMetar
     *            decoded METAR object to fill out
     * @param report
     *            report to parse
     * @throws ArrayIndexOutOfBoundsException
     *             on invalid array indexing.
     * @throws NumberFormatException
     *             on invalid number.
     * @throws ClimateMetarDecodingException
     *             on invalid metar format.
     */
    public static void decodeMetar(DecodedMetar decodedMetar,
            ClimateReport report) throws ArrayIndexOutOfBoundsException,
                    NumberFormatException, ClimateMetarDecodingException {
        String filteredReportText = MetarDecoderUtil
                .stripControlAndPunctuation(report.getReport());

        /*
         * Legacy split just by space, but instead we should split by all
         * consecutive whitespaces.
         */
        String[] reportArray = filteredReportText.split("\\s+");

        int reportIndex = 0;
        /*
         * Type of report. METAR (hourly, scheduled) or SPECI (special,
         * unscheduled).
         */
        reportIndex = parseCodename(decodedMetar, reportArray, reportIndex);

        /*
         * Station ID. Must be 4 characters and alphanumeric.
         */
        reportIndex = parseStationID(decodedMetar, reportArray, reportIndex);

        /*
         * Check for nil or time.
         */
        String nilOrTime = "";
        if (reportIndex < reportArray.length) {
            nilOrTime = reportArray[reportIndex++];
        } else {
            decodedMetar.getCmnData().setDecodeStatus(
                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
            logger.warn("Unexpected end of METAR report after station ID.");
        }
        /*
         * From hmPED_decodeMetar.c and hmPED_nil.c.
         */
        if (nilOrTime.equalsIgnoreCase("NIL")) {
            decodedMetar.getCmnData().setNil(true);
            logger.info("Found NIL METAR report: [" + report.getReport()
                    + "]. Will not decode further.");
        } else if (nilOrTime.matches(MetarDecoderUtil.DATETIME_REGEX)) {
            // not NIL, so must be time
            /*
             * From hmPED_TimeUTC.c.
             */
            // get month and year from report reception
            Calendar originCal = report.getOrigin();
            decodedMetar.getCmnData()
                    .setObMon(originCal.get(Calendar.MONTH) + 1);
            decodedMetar.getCmnData().setObYear(originCal.get(Calendar.YEAR));
            decodedMetar.getCmnData()
                    .setObDay(Integer.parseInt(nilOrTime.substring(0, 2)));
            decodedMetar.getCmnData()
                    .setObHour(Integer.parseInt(nilOrTime.substring(2, 4)));
            decodedMetar.getCmnData()
                    .setObMinute(Integer.parseInt(nilOrTime.substring(4, 6)));

            /*
             * Check for AUTO or COR. Optional field.
             */
            reportIndex = parseAutoOrCor(decodedMetar, reportArray,
                    reportIndex);

            /*
             * Regular wind data. Expected but not mandatory field.
             */
            reportIndex = parseWindData(decodedMetar, report, reportArray,
                    reportIndex);

            /*
             * CAVOK indicator.
             */
            // do not increment index right away, as this is an optional field
            /*
             * From hmPED_decodeMetar.c and hmPED_cavok.c.
             */
            String cavok = "";
            if (reportIndex < reportArray.length) {
                cavok = reportArray[reportIndex];
            } else {
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.warn("Unexpected end of METAR report after Wind Data.");
            }

            if (cavok.equalsIgnoreCase("CAVOK")) {
                decodedMetar.getCmnData().setCavok(true);
                logger.info("Found CAVOK flag in METAR report: ["
                        + report.getReport()
                        + "]. Will not search for wind, visibility, or sky conditions in main METAR body.");
                reportIndex++;
            } else {
                /*
                 * if no CAVOK flag, continue to rest of expected flags;
                 * otherwise skip to temperature
                 */
                /*
                 * variable wind direction. Optional field.
                 */
                reportIndex = parseVariableWindDirection(decodedMetar,
                        reportArray, reportIndex);

                /*
                 * visibility. Expected but not mandatory field.
                 */
                reportIndex = parseVisibility(decodedMetar, report, reportArray,
                        reportIndex);

                /*
                 * Runway visual range. Optional fields.
                 */
                reportIndex = parseRVRs(decodedMetar, reportArray, reportIndex);

                /*
                 * Weather. Optional fields.
                 */
                reportIndex = parsePresentWeather(decodedMetar, reportArray,
                        reportIndex);

                /*
                 * Sky condition/clouds. Optional fields.
                 */
                reportIndex = parseSkyConditions(decodedMetar, reportArray,
                        reportIndex);
            }

            /*
             * Temperature and dew. Expected but not mandatory field.
             */
            reportIndex = parseTempAndDew(decodedMetar, report, reportArray,
                    reportIndex);

            /*
             * Altimeter. Expected but not mandatory field.
             */
            reportIndex = parseAltimeter(decodedMetar, report, reportArray,
                    reportIndex);

            /*
             * Task 29190: METAR guides suggest that Recent Weather and Wind
             * Shear could follow Altimeter as optional groups, but Legacy does
             * not implement this
             */
            /*
             * Remarks. Optional group, so need to make sure we do not go beyond
             * the bounds of the report text. Also log all words from this point
             * to the remarks section as invalid.
             */
            int remarksIndex = Integer.MAX_VALUE;
            List<String> skippedWords = new ArrayList<>();
            for (int i = reportIndex; i < reportArray.length; i++) {
                if (reportArray[i]
                        .equalsIgnoreCase(MetarDecoderUtil.REMARKS_ABBR)) {
                    remarksIndex = i;
                    break;
                } else {
                    skippedWords.add(reportArray[i]);
                }
            }

            if (!skippedWords.isEmpty()) {
                StringBuilder message = new StringBuilder(
                        "End of METAR report: [" + report.getReport()
                                + "] contains non-remarks portions after main fields which will not be decoded: [");
                for (String skippedWord : skippedWords) {
                    message.append(" ").append(skippedWord);
                }
                message.append("]");
                logger.warn(message.toString());
            }

            if (remarksIndex != Integer.MAX_VALUE) {
                MetarRemarksDecoder.decodeMetarRemarks(decodedMetar,
                        reportArray, remarksIndex + 1);
            } else {
                logger.info("METAR report: [" + report.getReport()
                        + "] does not contain a remarks section.");
            }

            logger.info("Finished decoding METAR report: [" + report.getReport()
                    + "] for Climate.");
        } else {
            throw new ClimateMetarDecodingException("The string: [" + nilOrTime
                    + "] is neither NIL nor a valid datetime string.");
        }
    }

    /**
     * Parse altimeter data from METAR report. From hmPED_AltimStng.c.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_AltStng()
    
    FUNCTION DESCRIPTION
    Determine if the current token is a valif altimeter report. if so return
    TRUE; else FALSE. Store altimeter value in the Decoded_METAR structure.
        
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character string that 
                    contains the current token.
    Decoded_METAR   Mptr    Both    A pointer to a structure that contains
                    the decoded METAR data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param report
     * @param reportArray
     * @param reportIndex
     * @return report index to start parsing at.
     */
    private static int parseAltimeter(DecodedMetar decodedMetar,
            ClimateReport report, String[] reportArray, int reportIndex) {
        if (reportIndex < reportArray.length) {
            /*
             * Legacy allowed for international 'Q' prefix but did not actually
             * convert the value from mb to Hg.
             */
            String altimeter = reportArray[reportIndex++];
            if (altimeter.matches(MetarDecoderUtil.ALTIMETER_REGEX)) {

                float multiplier;
                if (altimeter
                        .startsWith(MetarDecoderUtil.ALTIMETER_HG_PREFIX)) {
                    // hundreths of inches of mercury; convert to inches
                    multiplier = 0.01f;
                } else {
                    // must start with Q; millibar; convert to inches of mercury
                    multiplier = MetarDecoderUtil.MB_TO_HG;
                }

                decodedMetar.setAltimeterSet(true);
                // parse into inches of mercury
                decodedMetar.setInchesAltstng(
                        multiplier * Integer.parseInt(altimeter.substring(1)));
            } else if (altimeter.length() >= 5) {
                decodedMetar.setAltimeterSet(false);

                // no altimeter field present
                logger.warn("No altimeter value parsed from report: ["
                        + report.getReport() + "], looking at value: ["
                        + altimeter + "].");
                reportIndex--;
            } else {
                // not a valid altimeter field
                logger.warn("Invalid altimeter value: [" + altimeter
                        + "] in report: [" + report.getReport() + "].");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                reportIndex--;
            }
        } else {
            logger.warn(
                    "Unexpected end of METAR report while parsing altimeter.");
        }
        return reportIndex;
    }

    /**
     * Parse temperature and dew from METAR report. From hmPED_TempGroup.c.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_TempGroup()
    
    FUNCTION DESCRIPTION
    Determine is the current token is a valid tempearture group. If so return
    TRUE; else FALSE. If valid, then store the temperature and dew point
    values in the Decoded_METAR structure.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character
                    string that contains the current token.
    Decoded_METAR   Mptr    Both    A pointer to a structure that contains
                    the decoded METAR data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param report
     * @param reportArray
     * @param reportIndex
     * @return the report index to start parsing from.
     */
    private static int parseTempAndDew(DecodedMetar decodedMetar,
            ClimateReport report, String[] reportArray, int reportIndex) {
        if (reportIndex < reportArray.length) {
            String tempAndDew = reportArray[reportIndex++];
            if (Pattern.compile(MetarDecoderUtil.STARTING_TEMP_AND_DEW_REGEX)
                    .matcher(tempAndDew).find()) {
                /*
                 * Legacy allowed for only the temperature portion to be good
                 * (including the slash)
                 */
                String[] tempAndDewSplitArray = tempAndDew
                        .split(MetarDecoderUtil.SLASH_DIVIDER);

                // temperature
                if (tempAndDewSplitArray[0].startsWith("M")) {
                    decodedMetar.setTemp(-1 * Integer
                            .parseInt(tempAndDewSplitArray[0].substring(1)));
                } else {
                    decodedMetar
                            .setTemp(Integer.parseInt(tempAndDewSplitArray[0]));
                }

                if (tempAndDew.matches(MetarDecoderUtil.TEMP_AND_DEW_REGEX)) {

                    // dewpoint
                    if (tempAndDewSplitArray[1].startsWith("M")) {
                        decodedMetar.setDewPtTemp(-1 * Integer.parseInt(
                                tempAndDewSplitArray[1].substring(1)));
                    } else {
                        decodedMetar.setDewPtTemp(
                                Integer.parseInt(tempAndDewSplitArray[1]));
                    }
                } else {
                    logger.warn("Temp and dew: [" + tempAndDew
                            + "] is missing properly formatted dew data in report: ["
                            + report.getReport() + "].");
                }
            } else {
                // not a valid Temp/Dew field
                logger.warn("Invalid temperature and dewpoint value: ["
                        + tempAndDew + "] from report: [" + report.getReport()
                        + "].");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                reportIndex--;
            }
        } else {
            logger.warn(
                    "Unexpected end of METAR report while parsing temp/dew.");
        }
        return reportIndex;
    }

    /**
     * Parse sky conditions from METAR report. From hmPED_decodeMetar.c,
     * hmPED_SkyCond.c, hmPED_parseCldData.c, and hmPED_CheckforSky.c.
     * 
     * <pre>
     * 
    FUNCTION NAME
    hmPED_SkyCond()
    
    FUNCTION DESCRIPTION
    Determines is a sky condition report is present. If so return TRUE; 
    else FALSE.If present check to see what type of condition is present
    and call the right routine to process that data.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            skycond Input   An address to a pointer to a character
                    string that contains the current token.
    PedCmnStruct    Mptr    Both    A pointer to a structure that contains
                    the decoded data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
                    
    FUNCTION NAME
    hmPED_CheckforSky()
    
    FUNCTION DESCRIPTION
    Determines if the current token is an Sky condition indicator. 
    If so return TRUE; else FALSE.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character string that 
                    contains the current token.
                    
    FUNCTION NAME
    hmPED_parseCldData()
    
    FUNCTION DESCRIPTION
    Parse the cloud data in the current token.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character
                    string that contains the current token.
    PedCmnStruct    Mptr    Both    A pointer to a structure that contains
                    the decoded data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return new report index to start parsing from.
     */
    private static int parseSkyConditions(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        for (int ccIndex = 0; ccIndex < DecodedMetar.NUM_CLOUD_CONDITIONS; ccIndex++) {
            if (reportIndex < reportArray.length) {
                // do not increment index right away, as this is an optional
                // field
                String currCC = reportArray[reportIndex];
                if (currCC.startsWith(MetarDecoderUtil.OVERCAST_CLOUDS_STRING)
                        || currCC.startsWith(
                                MetarDecoderUtil.SCATTERED_CLOUDS_STRING)
                        || currCC.startsWith(MetarDecoderUtil.FEW_CLOUDS_STRING)
                        || currCC.startsWith(
                                MetarDecoderUtil.BROKEN_CLOUDS_STRING)
                        || currCC.startsWith(MetarDecoderUtil.SKY_CLEAR_STRING)
                        || currCC.startsWith(MetarDecoderUtil.CLEAR_SKY_STRING)
                        || currCC.startsWith(
                                MetarDecoderUtil.VERTICAL_VISIBILITY_PREFIX)) {

                    CloudConditions currCloudConditions = decodedMetar
                            .getCmnData().getCloudConditions()[ccIndex];
                    /*
                     * if field is simply CLR (clear) or SKC (sky clear), done
                     * evaluating this report portion
                     */
                    if (currCC
                            .equalsIgnoreCase(MetarDecoderUtil.SKY_CLEAR_STRING)
                            || currCC.equalsIgnoreCase(
                                    MetarDecoderUtil.CLEAR_SKY_STRING)) {
                        currCloudConditions.setCloudType(currCC);
                        // next report portion
                        reportIndex++;
                        continue;
                    } else if (currCC.startsWith(
                            MetarDecoderUtil.VERTICAL_VISIBILITY_PREFIX)) {
                        /*
                         * if field starts with VV (vertical visibility), expect
                         * 3-4 numbers afterwards in hundreds of feet (need to
                         * convert to meters). Does not count towards cloud
                         * conditions array.
                         */
                        try {
                            int feetHeight = Integer
                                    .parseInt(currCC.substring(2));

                            int meterHeight = MetarDecoderUtil
                                    .convertCloudFeetToMeters(feetHeight);
                            decodedMetar.getCmnData()
                                    .setVerticalVisibility(meterHeight);
                        } catch (NumberFormatException e) {
                            decodedMetar.getCmnData().setDecodeStatus(
                                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                            logger.error(
                                    "Error parsing vertical visibility for METAR report.",
                                    e);
                        }
                        // next report portion
                        reportIndex++;
                        /*
                         * cloud conditions array portion not yet filled,
                         * regardless of validity of vertical visibility
                         */
                        ccIndex--;
                    } else if (currCC
                            .startsWith(MetarDecoderUtil.OVERCAST_CLOUDS_STRING)
                            || currCC.startsWith(
                                    MetarDecoderUtil.SCATTERED_CLOUDS_STRING)
                            || currCC.startsWith(
                                    MetarDecoderUtil.FEW_CLOUDS_STRING)
                            || currCC.startsWith(
                                    MetarDecoderUtil.BROKEN_CLOUDS_STRING)) {
                        /*
                         * Field must start with OVC (overcast), SCT (scattered,
                         * 2-4/8 coverage), FEW (1/8 coverage), or BKN (broken,
                         * 5-7/8 coverage).
                         */
                        currCloudConditions
                                .setCloudType(currCC.substring(0, 3));

                        // cut out cloud type
                        String currCCWithoutType = currCC.substring(3);

                        /*
                         * all that is left is 3-4 height digits, and an
                         * optional cloud phenom
                         */
                        int heightDigits;
                        if (currCCWithoutType.length() >= 4
                                && currCCWithoutType.substring(0, 4).matches(
                                        MetarDecoderUtil.NUM_ONLY_REGEX)) {
                            // 4 digits
                            heightDigits = 4;
                        } else if (currCCWithoutType.substring(0, 3)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                            // 3 digits
                            heightDigits = 3;
                        } else {
                            logger.warn("Cloud condition: [" + currCC
                                    + "] has invalid number of digits for height.");
                            decodedMetar.getCmnData().setDecodeStatus(
                                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                            // next report portion
                            reportIndex++;
                            continue;
                        }

                        // store original value string (height in feet)
                        currCloudConditions.setCloudHgtChar(
                                currCCWithoutType.substring(0, heightDigits));
                        int heightFeet = Integer.parseInt(
                                currCloudConditions.getCloudHgtChar());
                        // store meters height
                        currCloudConditions.setCloudHgtMeters(MetarDecoderUtil
                                .convertCloudFeetToMeters(heightFeet));

                        // cut out cloud height
                        String currCCWithoutHeight = currCCWithoutType
                                .substring(heightDigits);

                        // optional other cloud phenom; may just be empty string
                        currCloudConditions
                                .setOtherCldPhenom(currCCWithoutHeight);

                        // next report portion
                        reportIndex++;
                        continue;
                    } else {
                        // not a valid Sky condition, so done with sky section
                        logger.info("Invalid sky condition: [" + currCC
                                + "]. Will stop decoding sky condition for this report.");
                        break;
                    }
                } else {
                    logger.info("Invalid sky condition: [" + currCC
                            + "]. Will stop decoding sky conditions for this report.");
                    break;
                }
            } else {
                logger.warn(
                        "Unexpected end of METAR report while parsing sky condition.");
                break;
            }
        }
        return reportIndex;
    }

    /**
     * Parse present weather from METAR report. From hmPED_decodeMetar.c and
     * hmPED_PresentWX.c.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_PresentWX()
    
    FUNCTION DESCRIPTION
    Determines if the current token is a valid weather group for the current
    weather at the site. If so return TRUE; else FALSE. If valid store the
    weather type in the Decoded_METAR structure.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character string that 
                    contains the current token.
    PedCmnStruct    Mptr    Both    A pointer to a structure that contains
                    the decoded data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return new index to start parsing from.
     */
    private static int parsePresentWeather(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        for (int wxIndex = 0; wxIndex < DecodedMetar.NUM_REWX; wxIndex++) {
            if (reportIndex < reportArray.length) {
                // do not increment index right away, as this is an optional
                // field
                String currWx = reportArray[reportIndex];
                String intensity;
                if (currWx.startsWith("+") || currWx.startsWith("-")) {
                    intensity = currWx.substring(0, 1);
                    currWx = currWx.substring(1);
                } else {
                    intensity = "";
                }

                if (MetarDecoderUtil.VALID_WX_SYMBOLS.contains(currWx)) {
                    // store the weather type
                    decodedMetar.getCmnData()
                            .getWxObstruct()[wxIndex] = intensity + currWx;

                    // move to next report portion
                    reportIndex++;
                } else {
                    // not a valid WX, so done with WX section
                    logger.info("Invalid WX: [" + intensity + currWx
                            + "]. Will stop decoding WX for this report.");
                    break;
                }
            } else {
                logger.warn(
                        "Unexpected end of METAR report while parsing present weather.");
                break;
            }
        }
        return reportIndex;
    }

    /**
     * Parse runway visual range data from METAR report. From
     * hmPED_decodeMetar.c, hmPED_RVR.c, and hmPED_CheckforRVR.c.
     * 
     * <pre>
     * 
     * FUNCTION DESCRIPTION
    Determines if the Runway Visibility Range indicator is present. If so
    return TRUE; else FALSE. If present store the visibility in the Decoded_
    METAR structure.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character string that 
                    contains the current token.
    Decoded_METAR   Mptr    Both    A pointer to a structure that contains
                    the decoded METAR data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
                    
     * FUNCTION NAME
    hmPED_CheckforRVR()
    
    FUNCTION DESCRIPTION
    Determines if the current token is an RVR indicator. If so return TRUE;
    else FALSE.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            token   Input   A pointer to a character string that 
                    contains the current token.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return
     */
    private static int parseRVRs(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        /*
         * Legacy allowed for improperly formatted RVR followed by properly
         * formatted ones; this is not a documented standard but will be done
         * nonetheless.
         * 
         * Multiple RVRs are allowed in succession of each other.
         */
        // check first one
        for (int rvrIndex = 0; rvrIndex < DecodedMetar.NUM_RVR; rvrIndex++) {
            if (reportIndex < reportArray.length) {
                // do not increment index right away, as this is an optional
                // field
                String runwayVisualRange = reportArray[reportIndex];
                // do basic check first
                if (runwayVisualRange
                        .startsWith(MetarDecoderUtil.RUNWAY_VISUAL_RANGE_PREFIX)
                        && runwayVisualRange.length() >= 7) {
                    if (runwayVisualRange.matches(MetarDecoderUtil.RVR_REGEX)) {
                        String[] currSplitRvrArray = runwayVisualRange
                                .split(MetarDecoderUtil.SLASH_DIVIDER);

                        RunwayVisRange currRvr = decodedMetar
                                .getRrvr()[rvrIndex];

                        // runway portion; mandatory section
                        String runwayPortion = currSplitRvrArray[0];
                        // skip "R"
                        currRvr.setRunwayDesignator(runwayPortion.substring(1));

                        // visual range portion; mandatory section
                        String visualRangePortion = currSplitRvrArray[1];

                        if (visualRangePortion.startsWith("M")) {
                            // below report amount
                            currRvr.setBelowMinRVR(true);
                            // cut out this character
                            visualRangePortion = visualRangePortion
                                    .substring(1);
                        } else if (visualRangePortion.startsWith("P")) {
                            // above report amount
                            currRvr.setAboveMaxRVR(true);
                            // cut out this character
                            visualRangePortion = visualRangePortion
                                    .substring(1);
                        }

                        // determine if values will need to be converted to feet
                        // from meters
                        float multiplier = 1;
                        if (!visualRangePortion
                                .endsWith(MetarDecoderUtil.FEET_STRING)) {
                            multiplier = MetarDecoderUtil.M_TO_FT;
                        } else {
                            // cut out "FT" characters
                            visualRangePortion = visualRangePortion.substring(0,
                                    visualRangePortion.length() - 2);
                        }

                        // determine if variable values or not
                        if (visualRangePortion.contains(
                                MetarDecoderUtil.VARIABLE_DATA_FLAG)) {
                            // variable range
                            currRvr.setVrblVisRange(true);

                            // split by "V", all that is left is 2 integer
                            // values
                            String[] variableRangesArray = visualRangePortion
                                    .split(MetarDecoderUtil.VARIABLE_DATA_FLAG);

                            currRvr.setMinVisRange((int) (multiplier * Integer
                                    .parseInt(variableRangesArray[0])));
                            /*
                             * second part could start with M or P, like first
                             * part
                             */
                            String maxVisRangePortion = variableRangesArray[1];
                            if (maxVisRangePortion.startsWith("M")) {
                                // below report amount
                                currRvr.setBelowMinRVR(true);
                                // cut out this character
                                maxVisRangePortion = maxVisRangePortion
                                        .substring(1);
                            } else if (maxVisRangePortion.startsWith("P")) {
                                // above report amount
                                currRvr.setAboveMaxRVR(true);
                                // cut out this character
                                maxVisRangePortion = maxVisRangePortion
                                        .substring(1);
                            }
                            currRvr.setMaxVisRange((int) (multiplier
                                    * Integer.parseInt(maxVisRangePortion)));
                        } else {
                            // set range, all that is left is numbers
                            currRvr.setVisRange((int) (multiplier
                                    * Integer.parseInt(visualRangePortion)));
                        }

                    } else {
                        // not a valid RVR by pattern
                        logger.warn("Invalid RVR: [" + runwayVisualRange
                                + "]. Does not match pattern: ["
                                + MetarDecoderUtil.RVR_REGEX + "].");
                        rvrIndex--;
                    }
                    // move to next report portion, which may or may not be an
                    // RVR
                    reportIndex++;
                    continue;
                } else {
                    // not a valid RVR, as it does not have correct starting
                    // character.
                    logger.info("Invalid RVR: [" + runwayVisualRange
                            + "]. Does not start with R or length is less than 7. Will stop decoding RVRs for this report.");
                    break;
                }
            } else {
                logger.warn(
                        "Unexpected end of METAR report while parsing RVRs.");
                break;
            }
        }
        return reportIndex;
    }

    /**
     * Parse visibility from METAR report. From hmPED_Visibility.c.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_Visibility()
    
    FUNCTION DESCRIPTION
    Determine if the current token is a valid visibility report. If so return
    TRUE; else FALSE. If valid visibility call approprite routine to 
    determine the visibility.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            visblty Input   An address to a pointer to a character
                    string that contains the current token.
    PedCmnStruct    Mptr    Both    A pointer to a structure that contains
                    the decoded data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param report
     * @param reportArray
     * @param reportIndex
     * @return new report index to start parsing from.
     */
    private static int parseVisibility(DecodedMetar decodedMetar,
            ClimateReport report, String[] reportArray, int reportIndex) {
        if (reportIndex < reportArray.length) {
            /*
             * legacy also allowed units to be split away by a space, though
             * this is not a documented standard for METAR
             * 
             * Valid format for all words, where "U" is units and "Z" could be 1
             * or 2 digits is:
             * 
             * "XU" or "X U" or "XY/ZU" or "XY/Z U" or "X Y/ZU" or
             * 
             * "X Y/Z U" or "Y/ZU" or "Y/Z U"
             */
            /*
             * do not increment index right away, as this is an optional field
             */
            String visibility = reportArray[reportIndex];
            if (visibility.equalsIgnoreCase(
                    MetarDecoderUtil.VISIBILITY_LESS_QUARTER_SM)) {
                /* CHECK FOR VISIBILITY MEASURED <1/4SM */
                decodedMetar.getCmnData().setPrevailingVisibilitySM(0);
                logger.debug("Visibility flag is: [" + visibility + "].");
                reportIndex++;
                return reportIndex;
            } else if (visibility.equalsIgnoreCase(
                    MetarDecoderUtil.VISIBILITY_GREATER_6_SM)) {
                /* CHECK FOR VISIBILITY OF P6SM */
                decodedMetar.getCmnData().setPrevailingVisibilitySM(7);
                logger.debug("Visibility flag is: [" + visibility + "].");
                reportIndex++;
                return reportIndex;
            } else if (visibility.matches(
                    MetarDecoderUtil.SINGLE_OR_DOUBLE_DIGIT_VISIB_REGEX)) {
                // simple visibility, single or double digit and units
                // conversion to SM
                float multiplier = 1;
                if (visibility.endsWith(MetarDecoderUtil.KILOMETERS_ABBR)) {
                    multiplier = MetarDecoderUtil.KM_TO_SM;
                }

                float value;
                if (visibility.substring(0, 2)
                        .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    value = Float.parseFloat(visibility.substring(0, 2));
                } else {
                    value = Float.parseFloat(visibility.substring(0, 1));
                }

                decodedMetar.getCmnData()
                        .setPrevailingVisibilitySM(multiplier * value);
                reportIndex++;
                return reportIndex;
            } else if (visibility
                    .matches(MetarDecoderUtil.BAD_FRACTIONAL_VISIB_REGEX)
                    || visibility.matches(
                            MetarDecoderUtil.UNITLESS_BAD_FRACTIONAL_VISIB_REGEX)) {
                logger.warn("Improperly formatted mixed fraction visibility: ["
                        + visibility + "].");

                // conversion to SM
                float multiplier;
                if (visibility
                        .matches(MetarDecoderUtil.BAD_FRACTIONAL_VISIB_REGEX)) {
                    if (visibility.endsWith(MetarDecoderUtil.KILOMETERS_ABBR)) {
                        multiplier = MetarDecoderUtil.KM_TO_SM;
                    } else {
                        multiplier = 1;
                    }
                } else {
                    // check next word for units
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        String units = reportArray[reportIndex];

                        if (units.equalsIgnoreCase(
                                MetarDecoderUtil.KILOMETERS_ABBR)) {
                            multiplier = MetarDecoderUtil.KM_TO_SM;
                        } else if (units.equalsIgnoreCase(
                                MetarDecoderUtil.STATUTE_MILES_ABBR)) {
                            multiplier = 1;
                        } else {
                            logger.error("Could not get visibility units from ["
                                    + visibility + "] or [" + units + "].");
                            decodedMetar.getCmnData().setDecodeStatus(
                                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                            return reportIndex;
                        }
                    } else {
                        logger.error("Could not get visibility units from ["
                                + visibility
                                + "], which is the end of the report.");
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                        return reportIndex;
                    }
                }

                /*
                 * whole number portion. One or two digits prepended to
                 * numerator digit.
                 */
                int wholeNumberPortion;
                if (visibility.substring(0, 3)
                        .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    wholeNumberPortion = 2;
                } else {
                    wholeNumberPortion = 1;
                }

                float visibilityValue = Float.parseFloat(
                        visibility.substring(0, wholeNumberPortion));
                // cut out whole number and the units
                String visibilityWithoutUnitsAndWhole = visibility
                        .substring(0, visibility.length() - 2)
                        .substring(wholeNumberPortion);
                String[] fractionSplit = visibilityWithoutUnitsAndWhole
                        .split(MetarDecoderUtil.SLASH_DIVIDER);
                visibilityValue += Float.parseFloat(fractionSplit[0])
                        / Float.parseFloat(fractionSplit[1]);

                decodedMetar.getCmnData().setPrevailingVisibilitySM(
                        multiplier * visibilityValue);
                reportIndex++;
                return reportIndex;
            } else if (visibility.matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                /*
                 * the whole number has been split from the fraction and units.
                 */
                if (visibility.length() == 1) {
                    float currVisib = Float.parseFloat(visibility);
                    // advance to fractional portion
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        visibility = reportArray[reportIndex];

                        if (visibility.equalsIgnoreCase(
                                MetarDecoderUtil.KILOMETERS_ABBR)
                                || visibility.equalsIgnoreCase(
                                        MetarDecoderUtil.STATUTE_MILES_ABBR)) {
                            // next word could be units, then done
                            // conversion to SM
                            float multiplier = 1;
                            if (visibility.equalsIgnoreCase(
                                    MetarDecoderUtil.KILOMETERS_ABBR)) {
                                multiplier = MetarDecoderUtil.KM_TO_SM;
                            }

                            decodedMetar.getCmnData().setPrevailingVisibilitySM(
                                    multiplier * currVisib);

                            reportIndex++;
                            return reportIndex;
                        } else if (visibility.matches(
                                MetarDecoderUtil.FRACTIONAL_VISIB_REGEX)
                                || visibility.matches(
                                        MetarDecoderUtil.UNITLESS_FRACTIONAL_VISIB_REGEX)) {
                            /*
                             * must be fractional visib remaining, or visibility
                             * is not present
                             */
                            // get numerator
                            float numerator = Float
                                    .parseFloat(visibility.substring(0, 1));
                            float denominator;
                            // denominator may be 1 or 2 digits
                            if (visibility.length() >= 4
                                    && visibility.substring(2, 4).matches(
                                            MetarDecoderUtil.NUM_ONLY_REGEX)) {
                                // 2 digits
                                denominator = Float
                                        .parseFloat(visibility.substring(2, 4));
                            } else {
                                // 1 digit
                                denominator = Float
                                        .parseFloat(visibility.substring(2, 3));
                            }
                            currVisib += (numerator / denominator);

                            // conversion to SM
                            float multiplier;
                            if (visibility.matches(
                                    MetarDecoderUtil.FRACTIONAL_VISIB_REGEX)) {
                                if (visibility.endsWith(
                                        MetarDecoderUtil.KILOMETERS_ABBR)) {
                                    multiplier = MetarDecoderUtil.KM_TO_SM;
                                } else {
                                    multiplier = 1;
                                }
                            } else {
                                // check next word for units
                                reportIndex++;
                                if (reportIndex < reportArray.length) {
                                    String units = reportArray[reportIndex];

                                    if (units.equalsIgnoreCase(
                                            MetarDecoderUtil.KILOMETERS_ABBR)) {
                                        multiplier = MetarDecoderUtil.KM_TO_SM;
                                    } else if (units.equalsIgnoreCase(
                                            MetarDecoderUtil.STATUTE_MILES_ABBR)) {
                                        multiplier = 1;
                                    } else {
                                        logger.error(
                                                "Could not get visibility units from ["
                                                        + visibility + "] or ["
                                                        + units + "].");
                                        decodedMetar.getCmnData()
                                                .setDecodeStatus(
                                                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                                        return reportIndex;
                                    }
                                } else {
                                    logger.error(
                                            "Could not get visibility units from ["
                                                    + visibility
                                                    + "], which is the end of the report.");
                                    decodedMetar.getCmnData().setDecodeStatus(
                                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                                    return reportIndex;
                                }
                            }

                            decodedMetar.getCmnData().setPrevailingVisibilitySM(
                                    multiplier * currVisib);

                            reportIndex++;
                            return reportIndex;
                        } else {
                            if (currVisib != 0) {
                                logger.error(
                                        "Got whole number horizontal visibility ["
                                                + currVisib
                                                + "] but could not find fractional portion in report: ["
                                                + report.getReport() + "].");
                                decodedMetar.getCmnData().setDecodeStatus(
                                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                            } else {
                                logger.error(
                                        "No horizontal visibility present in METAR.");
                            }
                            reportIndex--;
                            return reportIndex;
                        }
                    } else {
                        logger.error(
                                "Unexpected end of report after visibility value ["
                                        + visibility + "].");
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                        return reportIndex;
                    }
                } else {
                    /*
                     * some locations report visibility in meters, numbers only.
                     * 9999 represents 7SM or more.
                     */
                    int visibMeters = Integer.parseInt(visibility);

                    if (visibMeters == 9999) {
                        decodedMetar.getCmnData().setPrevailingVisibilitySM(7);
                    } else {
                        decodedMetar.getCmnData().setPrevailingVisibilitySM(
                                visibMeters * MetarDecoderUtil.KM_TO_SM / 1000);
                    }

                    reportIndex++;
                    return reportIndex;
                }
            } else if (visibility
                    .matches(MetarDecoderUtil.FRACTIONAL_VISIB_REGEX)
                    || visibility.matches(
                            MetarDecoderUtil.UNITLESS_FRACTIONAL_VISIB_REGEX)) {
                // get numerator
                float numerator = Float.parseFloat(visibility.substring(0, 1));
                float denominator;
                // denominator may be 1 or 2 digits
                if (visibility.length() >= 4 && visibility.substring(2, 4)
                        .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    // 2 digits
                    denominator = Float.parseFloat(visibility.substring(2, 4));
                } else {
                    // 1 digit
                    denominator = Float.parseFloat(visibility.substring(2, 3));
                }

                // conversion to SM
                float multiplier;
                if (visibility
                        .matches(MetarDecoderUtil.FRACTIONAL_VISIB_REGEX)) {
                    if (visibility.endsWith(MetarDecoderUtil.KILOMETERS_ABBR)) {
                        multiplier = MetarDecoderUtil.KM_TO_SM;
                    } else {
                        multiplier = 1;
                    }
                } else {
                    // check next word for units
                    reportIndex++;
                    if (reportIndex < reportArray.length) {
                        String units = reportArray[reportIndex];

                        if (units.equalsIgnoreCase(
                                MetarDecoderUtil.KILOMETERS_ABBR)) {
                            multiplier = MetarDecoderUtil.KM_TO_SM;
                        } else if (units.equalsIgnoreCase(
                                MetarDecoderUtil.STATUTE_MILES_ABBR)) {
                            multiplier = 1;
                        } else {
                            logger.error("Could not get visibility units from ["
                                    + visibility + "] or [" + units + "].");
                            decodedMetar.getCmnData().setDecodeStatus(
                                    MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                            return reportIndex;
                        }
                    } else {
                        logger.error("Could not get visibility units from ["
                                + visibility
                                + "], which is the end of the report.");
                        decodedMetar.getCmnData().setDecodeStatus(
                                MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);

                        return reportIndex;
                    }
                }

                decodedMetar.getCmnData().setPrevailingVisibilitySM(
                        multiplier * (numerator / denominator));

                reportIndex++;
                return reportIndex;
            }
        } else {
            logger.warn(
                    "Unexpected end of METAR report while parsing visibility.");
        }

        logger.info("No visibility present in METAR report: ["
                + report.getReport() + "].");
        return reportIndex;
    }

    /**
     * Parse variable wind direction from METAR. From hmPED_MinMaxWinDir.c.
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return new report index to start parsing from.
     */
    private static int parseVariableWindDirection(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        if (reportIndex < reportArray.length) {
            // do not increment index right away, as this is an optional
            // field
            String variableWindDirection = reportArray[reportIndex];
            if (variableWindDirection
                    .matches(MetarDecoderUtil.VAR_WIND_DIR_REGEX)) {
                decodedMetar.setMinWnDir(Integer
                        .parseInt(variableWindDirection.substring(0, 3)));
                decodedMetar.setMaxWnDir(
                        Integer.parseInt(variableWindDirection.substring(4)));
                reportIndex++;
            } else {
                logger.info("No variable wind direction in METAR report. ["
                        + variableWindDirection + "] is not a valid value.");
            }
        } else {
            logger.warn(
                    "Unexpected end of METAR report while parsing variable wind direction.");
        }
        return reportIndex;
    }

    /**
     * Parse wind data from METAR. From hmPED_WindData.c.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_WinData()
    
    FUNCTION DESCRIPTION
    Determine if the current token is valid wind data. if so return TRUE;
    else FALSE. if valid, store all values in the Decoded_METAR structure.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            wind    Input   A pointer to a character
                    string that contains the current token.
    PedCmnStruct    Mptr    Both    A pointer to a structure that contains
                    the decoded data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param report
     * @param reportArray
     * @param reportIndex
     * @return new report index to start parsing from.
     */
    private static int parseWindData(DecodedMetar decodedMetar,
            ClimateReport report, String[] reportArray, int reportIndex) {
        if (reportIndex < reportArray.length) {
            String regularWind = reportArray[reportIndex++];
            // direction
            if (regularWind.startsWith("VRB")) {
                // variable direction
                decodedMetar.getCmnData().getWinData().setWindVrb(true);
            } else if (regularWind.length() >= 3 && regularWind.substring(0, 3)
                    .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                // 3-digit direction
                decodedMetar.getCmnData().getWinData().setWindDir(
                        Integer.parseInt(regularWind.substring(0, 3)));
            } else {
                reportIndex--;
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.warn("Invalid wind: [" + regularWind
                        + "]. No direction given.");
                return reportIndex;
            }

            String windWithoutDirection = regularWind.substring(3);
            // speed, 2 or 3 digits
            String windWithoutSpeed;
            if (windWithoutDirection.length() >= 3 && windWithoutDirection
                    .substring(0, 3).matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                // 3 digit speed
                decodedMetar.getCmnData().getWinData().setWindSpeed(
                        Integer.parseInt(windWithoutDirection.substring(0, 3)));
                windWithoutSpeed = windWithoutDirection.substring(3);
            } else if (windWithoutDirection.length() >= 2
                    && windWithoutDirection.substring(0, 2)
                            .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                // 2 digit speed
                decodedMetar.getCmnData().getWinData().setWindSpeed(
                        Integer.parseInt(windWithoutDirection.substring(0, 2)));
                windWithoutSpeed = windWithoutDirection.substring(2);
            } else {
                reportIndex--;
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                logger.warn("Invalid wind: [" + regularWind
                        + "]. Invalid speed given.");
                return reportIndex;
            }

            // see if gust is present
            if (windWithoutSpeed.startsWith("G")) {
                // gusts
                // remove "G" indicator
                windWithoutSpeed = windWithoutSpeed.substring(1);
                // gust speed, 2 or 3 digits
                if (windWithoutSpeed.length() >= 3
                        && windWithoutSpeed.substring(0, 3)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    // 3 digit speed
                    decodedMetar.getCmnData().getWinData().setWindGust(
                            Integer.parseInt(windWithoutSpeed.substring(0, 3)));
                    windWithoutSpeed = windWithoutSpeed.substring(3);
                } else if (windWithoutSpeed.length() >= 2
                        && windWithoutSpeed.substring(0, 2)
                                .matches(MetarDecoderUtil.NUM_ONLY_REGEX)) {
                    // 2 digit speed
                    decodedMetar.getCmnData().getWinData().setWindGust(
                            Integer.parseInt(windWithoutSpeed.substring(0, 2)));
                    windWithoutSpeed = windWithoutSpeed.substring(2);
                } else {
                    reportIndex--;
                    decodedMetar.getCmnData().setDecodeStatus(
                            MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                    logger.warn("Invalid wind: [" + regularWind
                            + "]. Invalid gust speed given.");
                    return reportIndex;
                }
            }

            // all that is left is units
            if (windWithoutSpeed.equalsIgnoreCase(MetarDecoderUtil.KT_STRING)) {
                decodedMetar.getCmnData().getWinData()
                        .setWindUnits(MetarDecoderUtil.KT_STRING);
            } else if (windWithoutSpeed
                    .equalsIgnoreCase(MetarDecoderUtil.MPS_STRING)) {
                decodedMetar.getCmnData().getWinData()
                        .setWindUnits(MetarDecoderUtil.MPS_STRING);
            } else if (windWithoutSpeed
                    .equalsIgnoreCase(MetarDecoderUtil.KMH_STRING)) {
                decodedMetar.getCmnData().getWinData()
                        .setWindUnits(MetarDecoderUtil.KMH_STRING);
            } else {
                reportIndex--;
                logger.warn("Invalid wind units: [" + windWithoutSpeed
                        + "] in report: [" + report.getReport() + "]");
                decodedMetar.getCmnData().setDecodeStatus(
                        MetarDecoderUtil.DECODING_ERROR_METAR_STATUS);
                return reportIndex;
            }
        } else {
            logger.warn("Unexpected end of METAR report while parsing wind.");
        }

        return reportIndex;
    }

    /**
     * Parse AUTO or COR flag from METAR. From hmPED_decodeMetar.c,
     * hmPED_AUTO.c, and hmPED_COR.c.
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return new report index to start parsing from.
     */
    private static int parseAutoOrCor(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        if (reportIndex < reportArray.length) {
            // do not increment index right away, as this is an optional field
            String autoOrCor = reportArray[reportIndex];
            if (autoOrCor.equalsIgnoreCase("COR")) {
                decodedMetar.setCor(true);
                logger.debug("METAR report is a correction.");
                reportIndex++;
            } else if (autoOrCor.equalsIgnoreCase("AUTO")) {
                decodedMetar.setAuto(true);
                logger.debug("METAR report is an automatic report.");
                reportIndex++;
            } else {
                logger.debug("METAR report is not automatic or a correction.");
            }
        } else {
            logger.warn(
                    "Unexpected end of METAR report while parsing AUTO/COR flags.");
        }
        return reportIndex;
    }

    /**
     * Parse station ID from the report. From hmPED_StationId.C.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_StnID()
    
    FUNCTION DESCRIPTION
    Check to see if the current token is a valid station ID. If so return
    TRUE; else FALSE. If it is valid store the station name in the Decoded_
    METAR structure.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            stnID   Input   A pointer to a character
                    string that contains the current token.
    PedCmnStruct    Mptr    Both    A pointer to a structure that contains
                    the decoded data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return report index to parse next.
     * @throws ClimateMetarDecodingException
     */
    private static int parseStationID(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex)
                    throws ClimateMetarDecodingException {
        String stationID = reportArray[reportIndex++];
        if (stationID.matches(MetarDecoderUtil.STATION_ID_REGEX)) {
            decodedMetar.getCmnData().setStationID(stationID);
            logger.debug("Got station ID for METAR: [" + stationID + "]");
        } else {
            throw new ClimateMetarDecodingException(
                    "Got invalid station ID: [" + stationID + "]");
        }
        return reportIndex;
    }

    /**
     * Decode the codename (report type) from the reportArray. From
     * hmPED_CodeName.c.
     * 
     * <pre>
     * FUNCTION NAME
    hmPED_CodeName()
    
    FUNCTION DESCRIPTION
    This routine determines if the current report is a METAR or SPECI report.
    It checks to see if the string METAR or SPECI is present. If so it
    returns TRUE; else FALSE. Also stores the type of report in the
    Decoded_METAR structure.
    
    PARAMETERS
    Type        Name    I/O     Description
    char         codename   Input   A pointer to a character string that 
                    contains the current token.
    Decoded_METAR   Mptr    Both    A pointer to a structure that contains
                    the decoded METAR data.
    int     NDEX    Both    A pointer to an integer that is the 
                    index into a array that contains the
                    individual groups of the METAR report.
                    being decoded. Upon entry, NDEX is
                    the current group of the METAR report
                    that is to be identified.
     * </pre>
     * 
     * @param decodedMetar
     * @param reportArray
     * @param reportIndex
     * @return index for the rest of parsing to use.
     */
    private static int parseCodename(DecodedMetar decodedMetar,
            String[] reportArray, int reportIndex) {
        String reportType = reportArray[reportIndex++];
        decodedMetar.setReportTypeCodeName(reportType);
        if (!(reportType.equalsIgnoreCase("METAR")
                || reportType.equalsIgnoreCase("SPECI"))) {
            logger.warn("Unexpected METAR report type: [" + reportType + "]");
            reportIndex--;
        } else {
            logger.debug("Got metar report type: [" + reportType + "]");
        }
        return reportIndex;
    }
}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.asos.DailySummaryRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * DSM Message parser (without WMOID and other contents)
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 7, 2016  16962      pwang       Initial creation
 * 25 JAN 2017  23221      amoore      Use common constants.
 * 03 MAY 2017  33104      amoore      Use generic map and set.
 * 05 MAY 2017  33104      amoore      Minor clean up. Logging.
 *                                     Remarks can have multiple parts.
 *                                     Add possible spacing in message.
 *                                     Sky cover can have multiple N's.
 * 08 MAY 2017  33104      amoore      Use regular field assignment.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class DailySummaryMessageParser extends ASOSMessageParser {

    /**
     * Logger.
     */
    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(DailySummaryMessageParser.class);

    // Group-capturing pattern, require JDK 1.7+
    public static final String DSMRAW_REGEX = "(?<stationCode>\\w{3,4}) DS (?:(?:(?<correction>COR)|(?<messageValidTime>\\d{4}))\\s)?(?<day>\\d{2})/(?<month>\\d{2})\\s"
            + "(?:M|N|-(?!\\d)|(?<maxT>-?\\d{2,3}(?=\\d{4}))(?<maxTTime>\\d{4}))/(?:M|N|-(?!\\d)|\\s?(?<minT>-?\\d{2,3}(?=\\d{4}))(?<minTTime>\\d{4}))"
            + "//(?:M|N|-(?!\\d)|\\s?(?<maxTDaytime>-?\\d{1,3}))/(?:M|N|-(?!\\d)|\\s?(?<minTNight>-?\\d{1,3}))"
            + "//(?:M|N|-(?!\\d)|(?<minSeaLevelPressure>\\d{3,5})(?<minSeaLevelPressureTime>\\d{4}))"
            + "/(?:M|N|-|(?<totalPrecip>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip1>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip2>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip3>T|\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<hourlyPrecip4>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip5>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip6>T|\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<hourlyPrecip7>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip8>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip9>T|\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<hourlyPrecip10>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip11>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip12>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip13>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip14>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip15>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip16>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip17>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip18>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip19>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip20>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip21>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip22>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip23>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip24>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<windSpeed2MinAvg>\\d{1,3}))"
            + "/(?:M|N|-|(?<windDirection2MinFastest>\\d{2})(?<windSpeed2MinFastest>\\d{2,3}(?=\\d{4}))(?<windSpeed2MinFastestTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<peakWindDirection>\\d{2})(?<peakWindSpeed>\\d{2,3}(?=\\d{4}))(?<peakWindTime>\\d{4}))"
            + "(?:/\\s?(?:N(?=/)|((?<wxSymbol1>[123456789X]{1}))?((?<wxSymbol2>[123456789X]{1}))?((?<wxSymbol3>[123456789X]{1}))?((?<wxSymbol4>[123456789X]{1}))?((?<wxSymbol5>[123456789X]{1}))?))?"
            + "(?:/\\s?(?:M|N{1,}|-|(?<sunshineMinutes>\\d{1,3}(?=\\d{2}|100|/))\\s?(?<sunshinePercent>100|\\d{2})?))?"
            + "(?:/\\s?(?:M|N{1,}|-|(?<snowAmount>\\d{1,3})))?(?:/\\s?(?:M|N|-|(?<depthOfSnow>\\d{1,3})))?(?:/\\s?(?:M|N{1,}|-|(?<skyCoverDaytime>\\d{2})(?<skyCoverWholeDay>\\d{2})))?"
            + "(?:/(?<remarks>((?:ET|Epr|EP|EW|ES|ESw|ESd|EC)\\s?){1,8}))?";

    private Pattern dsmPattern;

    public DailySummaryMessageParser() {
        if (groupNamePattern == null) {
            groupNamePattern = Pattern
                    .compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
        }

        if (namedGroups == null) {
            namedGroups = new HashSet<>();
        }

        if (namedAsosMap == null) {
            namedAsosMap = new HashMap<>();
        }

        // Build name set for named groups
        Matcher m = groupNamePattern.matcher(DSMRAW_REGEX);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
    }

    @Override
    public DailySummaryRecord parse(String message) {

        dsmPattern = Pattern.compile(DSMRAW_REGEX);
        final Matcher matcher = dsmPattern.matcher(message);
        if (matcher.matches()) {
            /*
             * ASOS message matches the DMS pattern. Assign values.
             */
            for (String groupName : namedGroups) {
                if (matcher.group(groupName) != null)
                    namedAsosMap.put(groupName, matcher.group(groupName));
            }
        }

        // constructor sets default values
        DailySummaryRecord dsm = new DailySummaryRecord();

        List<String> missingGroups = new ArrayList<>();

        // station code
        for (String groupName : namedGroups) {
            // get value from message
            String value = namedAsosMap.get(groupName);

            if (value == null) {
                missingGroups.add(groupName);
                // no need to set missing values since default
                // constructor took care of that
                continue;
            } else {
                logger.debug("Raw value for ASOS group name: [" + groupName
                        + "] is: [" + value + "]");
            }

            try {
                if (groupName.equalsIgnoreCase("stationCode")) {
                    dsm.setStationCode(value);
                } else if (groupName.equalsIgnoreCase("correction")) {
                    // the only boolean field
                    dsm.setCorrection(true);
                } else if (groupName.equalsIgnoreCase("messageValidTime")) {
                    dsm.setMessageValidTime(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("day")) {
                    dsm.setDay(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("month")) {
                    dsm.setMonth(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("maxT")) {
                    dsm.setMaxT(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("maxTTime")) {
                    dsm.setMaxTTime(value);
                } else if (groupName.equalsIgnoreCase("minT")) {
                    dsm.setMinT(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("minTTime")) {
                    dsm.setMinTTime(value);
                } else if (groupName.equalsIgnoreCase("maxTDaytime")) {
                    dsm.setMaxTDaytime(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("minTNight")) {
                    dsm.setMinTNight(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("minSeaLevelPressure")) {
                    /*
                     * maps to SLPmm, reported to the nearest 0.01 inches of Hg,
                     * based on legacy C++, should be in range of 25.00 to 34.00
                     */
                    float val = Float.parseFloat(value);
                    dsm.setMinSeaLevelPressure(
                            val / 100.0f + (val < 500 ? 30.0f : 20.0f));
                } else if (groupName
                        .equalsIgnoreCase("minSeaLevelPressureTime")) {
                    dsm.setMinSeaLevelPressureTime(value);
                } else if (groupName.toLowerCase().contains("precip")) {
                    /*
                     * all precipitation are in hundredths, so divide by 100
                     * here
                     */
                    float val;
                    if (value.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                        val = -1;
                    } else {
                        val = Float.parseFloat(value) / 100;
                    }

                    if (groupName.equalsIgnoreCase("totalPrecip")) {
                        dsm.setTotalPrecip(val);
                    } else if (groupName.startsWith("hourlyPrecip")) {
                        // per review comments, hourly precip is an array
                        int index = Integer.parseInt(
                                groupName.replace("hourlyPrecip", "")) - 1;

                        dsm.getHourlyPrecip()[index] = val;
                    } else {
                        logger.error("Unhandled DSM precip group: [" + groupName
                                + "] with value: [" + val + "]");
                    }
                } else if (groupName.equalsIgnoreCase("windSpeed2MinAvg")) {
                    /*
                     * maps to FaFaFa, reported in tenths of miles per hour
                     * (mph)
                     */
                    dsm.setWindSpeed2MinAvg(Float.parseFloat(value) / 10);
                } else if (groupName
                        .equalsIgnoreCase("windDirection2MinFastest")) {
                    /*
                     * maps to dd, Direction of the 2-minute fastest wind speed,
                     * reported in tens of degrees
                     */
                    dsm.setWindDirection2MinFastest(
                            (short) (Short.parseShort(value) * 10));
                } else if (groupName.equalsIgnoreCase("windSpeed2MinFastest")) {
                    dsm.setWindSpeed2MinFastest(Short.parseShort(value));
                } else if (groupName
                        .equalsIgnoreCase("windSpeed2MinFastestTime")) {
                    dsm.setWindSpeed2MinFastestTime(value);
                } else if (groupName.equalsIgnoreCase("peakWindDirection")) {
                    /*
                     * maps to DD, Direction of the day's peak wind, reported in
                     * tens of degrees
                     */
                    dsm.setPeakWindDirection(
                            (short) (Short.parseShort(value) * 10));
                } else if (groupName.equalsIgnoreCase("peakWindSpeed")) {
                    dsm.setPeakWindSpeed(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("peakWindTime")) {
                    dsm.setPeakWindTime(value);
                } else if (groupName.startsWith("wxSymbol")) {
                    /*
                     * wxSymbol 'X' need to be converted to '10'
                     */
                    short val;
                    if (value.equals("X")) {
                        val = 10;
                    } else {
                        val = Short.parseShort(value);
                    }

                    // per review comments, wx symbols is an array
                    int index = Integer
                            .parseInt(groupName.replace("wxSymbol", "")) - 1;

                    dsm.getWxSymbol()[index] = val;
                } else if (groupName.equalsIgnoreCase("sunshineMinutes")) {
                    dsm.setSunshineMinutes(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("sunshinePercent")) {
                    dsm.setSunshinePercent(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("snowAmount")) {
                    /*
                     * maps to SwSwSw, reported in tenths of an inch (when
                     * available or augmented)
                     */
                    dsm.setSnowAmount(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("depthOfSnow")) {
                    dsm.setDepthOfSnow(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("skyCoverDaytime")) {
                    /*
                     * maps to CsCs, Average daily sky cover from sunrise to
                     * sunset, in tenths of sky cover
                     */
                    dsm.setSkyCoverDaytime(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("skyCoverWholeDay")) {
                    /*
                     * maps to CmCm, Average daily sky cover, midnight to
                     * midnight LST, in tenths of sky cover
                     */
                    dsm.setSkyCoverWholeDay(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("remarks")) {
                    dsm.setRemarks(value);
                } else {
                    logger.error("Unhandled DSM group: [" + groupName
                            + "] with value: [" + value + "]");
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing number for group: [" + groupName
                        + "] with value: [" + value + "]", e);
            } catch (RuntimeException e) {
                logger.error("Unexpected exception for group: [" + groupName
                        + "] with value: [" + value + "]", e);
            }
        }

        if (!missingGroups.isEmpty()) {
            logger.info("No values given for ASOS group names: ["
                    + Arrays.toString(missingGroups.toArray()) + "]");
        }

        return dsm;
    }
}
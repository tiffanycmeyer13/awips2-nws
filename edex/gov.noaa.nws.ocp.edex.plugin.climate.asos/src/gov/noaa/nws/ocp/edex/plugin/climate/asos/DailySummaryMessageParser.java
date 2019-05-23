/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * 07 SEP 2017  37725      amoore      Fix time representations in SQL (needs :).
 *                                     Fix error parsing 24-hour max precip.
 * 08 SEP 2017  37769      amoore      Comments on regex
 * 15 SEP 2017  38015      amoore      Wind/gust times can be 3 digits.
 * 24 OCT 2017  39785      amoore      Groups allowing N{1,} should also allow M{1,}.
 *                                     Cleaner organization and clarity in comments.
 *                                     Organization of optional spacing elements.
 * 31 OCT 2017  40218      amoore      Snow amount and depth can be trace.
 * 31 OCT 2017  40231      amoore      Clean up of MSM/DSM parsing and records. Better
 *                                     logging. Get rid of serialization tags.
 * 03 NOV 2017  36736      amoore      Make several parts and logic static.
 * 11 APR 2019  DR 21229   dfriedman   Change valid time type to string.
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

    /**
     * Group-capturing pattern, require JDK 1.7+
     * 
     * Example products:
     * 
     * KTRI DS 29/10 420025/ 352359// 39/
     * 38//9711340/T/00/00/00/00/00/00/T/00/00/00/00/00/00/00/T/T/T/T/00/00/00/
     * 00/00/00/83/30152050/36200840/1/NN/T
     * 
     * KBIL DS 29/10 580232/ 312358// 47/
     * 45//9730122/16/00/00/00/00/00/00/00/T/04/01/01/02/02/04/01/01/T/T/T/00/00
     * /00/00/00/116/25240057/02290335/1/NN/T/0
     *
     * KCVG DS 28/10 411150/ 350728// 41/
     * 35//9870337/48/14/14/05/05/04/05/01/T/00/00/00/00/00/00/00/00/00/00/00/00
     * /00/00/00/00/66/28161232/27211042/1/NN/T/0
     *
     * KDAY DS 28/10 390941/ 340535// 39/
     * 34//9860321/21/06/09/02/01/02/01/00/00/00/00/00/00/00/00/00/00/00/00/00/
     * 00/00/00/00/00/78/27171022/27211021/1/NN/T/0
     * 
     * KIPT DS 06/06 761707/ 530514// 76/ 53//9840011/00/00/00/00/00/00/00/
     * 00/00/00/00/00/00/00/00/00/00/00/00/00/00/00/00/00/00/68/31151707/
     * 30180953/N/NN/N/N/NN/EW
     * 
     * KIPT DS 1700 06/06 761655/ 530514// 76/ 53//9840011/00/00/00/00/00/
     * 00/00/00/00/00/00/00/00/00/00/00/00/00/-/-/-/-/-/-/-/-/29141041/ 30180953
     * 
     * KSGJ DS 30/08 851138/ 760704//M/
     * 76//M/00/M/M/M/M/M/M/M/M/M/M/M/M/M/M/00/00/00/00/00/00/00/00/00/00/M/
     * 0017704-1765/09141857/N/NN/N/N/NN/ET EP EW
     * 
     * KSGT MS 08 94-22/
     * 63-25/849/692/771/MMMM/M/M/29750/998/011261014+/982302359/13/03000000/
     * 082728/03280922/04280929/06280925/07280929/07280929/07280929/07280929/
     * 07280929/07280929/07280929/07280929/07280929/NN/N/N/NNN/ET EPR EP
     * 
     * KESC MS 08 84-08/
     * 36-25/726/530/628/00000000/86/31/29340/005/041260818+/956172100+/454/
     * 14060302/1440303/34301205/49301207/55301211/60170514/66170514/73170535/
     * 78170535/83170535/87170550/89170601/92170601/92170648/NN/N/N/NNN/EPR EP
     * 
     * KOXC MS 08 86-0122/
     * 47-26/773/583/678/MMMM/22/81/29310/008/040280805+/977230352+/323/11030101
     * /2150505/38050558/67050557/100050557/122050558/150050607/179050610/
     * 198050622/205050635/207050701/207050701/207050701/207050701/NN/N/N/NNN/ET
     * EPR EP
     */
    public static final String DSMRAW_REGEX =
    // M,N, and - are missing indicators
    // 3-4 character station code, optional COR flag, 4 digit time, 2 digit day,
    // 2 digit month
    "(?<stationCode>\\w{3,4}) DS (?:(?:(?<correction>COR)|(?<messageValidTime>\\d{4}))\\s)?(?<day>\\d{2})/(?<month>\\d{2})\\s"
            // missing, or potentially negative 2-3 digit max temp, 4 digit max
            // temp time, and then the same for min temp
            + "(?:M|N|-(?!\\d)|(?<maxT>-?\\d{2,3}(?=\\d{4}))(?<maxTTime>\\d{4}))"
            + "/\\s?(?:M|N|-(?!\\d)|(?<minT>-?\\d{2,3}(?=\\d{4}))(?<minTTime>\\d{4}))"
            // missing, or potentially negative 1-3 digit max temp during day,
            // and then the same for min temp at night
            + "//\\s?(?:M|N|-(?!\\d)|(?<maxTDaytime>-?\\d{1,3}))/\\s?(?:M|N|-(?!\\d)|(?<minTNight>-?\\d{1,3}))"
            // missing, or 3-5 digit min sea pressure, then 4 digit time
            + "//(?:M|N|-(?!\\d)|(?<minSeaLevelPressure>\\d{3,5})(?<minSeaLevelPressureTime>\\d{4}))"
            // missing, or 1-4 digit or trace total precip, then 24 instances of
            // missing or 1-4 digit or trace hourly precips
            + "/(?:M|N|-|(?<totalPrecip>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip1>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip2>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip3>T|\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<hourlyPrecip4>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip5>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip6>T|\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<hourlyPrecip7>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip8>T|\\d{1,4}))/\\s?(?:M|N|-|(?<hourlyPrecip9>T|\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<hourlyPrecip10>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip11>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip12>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip13>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip14>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip15>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip16>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip17>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip18>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip19>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip20>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip21>T|\\d{1,4}))"
            + "/(?:M|N|-|(?<hourlyPrecip22>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip23>T|\\d{1,4}))/(?:M|N|-|(?<hourlyPrecip24>T|\\d{1,4}))"
            // missing, or 1-3 digit average wind speed
            + "/(?:M|N|-|(?<windSpeed2MinAvg>\\d{1,3}))"
            // missing, or 2 digit max wind direction, then 2-3 digit max wind
            // speed, then 3-4 digit time
            + "/(?:M|N|-|(?<windDirection2MinFastest>\\d{2})(?<windSpeed2MinFastest>\\d{2,3}?(?=\\d{3,4}))(?<windSpeed2MinFastestTime>\\d{3,4}))"
            // missing, or 2 digit max gust direction, then 2-3 digit max gust
            // speed, then 3-4 digit time
            + "/\\s?(?:M|N|-|(?<peakWindDirection>\\d{2})(?<peakWindSpeed>\\d{2,3}?(?=\\d{3,4}))(?<peakWindTime>\\d{3,4}))"
            /*
             * Below this point is for the Full DSM only (post-midnight), and
             * thus the entirety is optional as a whole in addition to
             * individual elements being optional.
             */
            // optional: missing, or up to 5 weather symbols (1-9, or X)
            + "(?:/\\s?(?:N(?=/)|((?<wxSymbol1>[123456789X]{1}))?((?<wxSymbol2>[123456789X]{1}))?((?<wxSymbol3>[123456789X]{1}))?((?<wxSymbol4>[123456789X]{1}))?((?<wxSymbol5>[123456789X]{1}))?))?"
            /*
             * optional: missing, or 1-3 digit sunshine minutes, then sunshine
             * percent 2 digits or 100
             */
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<sunshineMinutes>\\d{1,3}(?=\\d{2}|100|/))\\s?(?<sunshinePercent>100|\\d{2})?))?"
            /*
             * Below this point each group is optional, however for one group to
             * exist each previous one must have been present. See comments on
             * each group.
             */
            /*
             * optional: missing, or 1-3 digit/trace snow amount. Sunshine
             * grouping must have been present (or M|N|-).
             */
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<snowAmount>T|\\d{1,3})))?"
            /*
             * optional: missing or 1-3 digit/trace snow depth. Snow amount must
             * have been present (or M|N|-).
             */
            + "(?:/\\s?(?:M|N|-|(?<depthOfSnow>T|\\d{1,3})))?"
            /*
             * optional: missing or 2 digit sky cover during day, then 2 digit
             * sky cover for whole day. Sunshine and snow groupings must have
             * been present (or M|N|-).
             */
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<skyCoverDaytime>\\d{2})(?<skyCoverWholeDay>\\d{2})))?"
            /*
             * optional: nothing, or up to 8 optional remarks from the list.
             * Sunshine, snow, and sky groupings must have been present (or
             * M|N|-).
             */
            + "(?:/(?<remarks>((?:ET|Epr|EP|EW|ES|ESw|ESd|EC)\\s?){1,8}))?";

    /**
     * Set of named groups.
     */
    private static final Set<String> NAMED_GROUPS = new HashSet<>();

    /**
     * Build name set for named groups.
     */
    static {
        Matcher m = GROUP_NAME_PATTERN.matcher(DSMRAW_REGEX);
        while (m.find()) {
            NAMED_GROUPS.add(m.group(1));
        }
    }

    /**
     * Empty constructor.
     */
    public DailySummaryMessageParser() {
    }

    @Override
    public DailySummaryRecord parse(String message) {
        Pattern dsmPattern = Pattern.compile(DSMRAW_REGEX);
        final Matcher matcher = dsmPattern.matcher(message);

        Map<String, String> namedAsosMap = new HashMap<>();

        if (matcher.matches()) {
            /*
             * ASOS message matches the DMS pattern. Assign values.
             */
            for (String groupName : NAMED_GROUPS) {
                if (matcher.group(groupName) != null) {
                    namedAsosMap.put(groupName, matcher.group(groupName));
                }
            }
        } else {
            logger.error("Message: [" + message
                    + "] does not match expected pattern: [" + DSMRAW_REGEX
                    + "]");
        }

        // constructor sets default values
        DailySummaryRecord dsm = new DailySummaryRecord(message);

        List<String> missingGroups = new ArrayList<>();

        // station code
        for (String groupName : NAMED_GROUPS) {
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
                    dsm.setMessageValidTime(adjustTimeString(value));
                } else if (groupName.equalsIgnoreCase("day")) {
                    dsm.setDay(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("month")) {
                    dsm.setMonth(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("maxT")) {
                    dsm.setMaxT(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("maxTTime")) {
                    dsm.setMaxTTime(adjustTimeString(value));
                } else if (groupName.equalsIgnoreCase("minT")) {
                    dsm.setMinT(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("minTTime")) {
                    dsm.setMinTTime(adjustTimeString(value));
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
                            (val / 100.0f) + (val < 500 ? 30.0f : 20.0f));
                } else if (groupName
                        .equalsIgnoreCase("minSeaLevelPressureTime")) {
                    dsm.setMinSeaLevelPressureTime(adjustTimeString(value));
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
                    dsm.setWindSpeed2MinFastestTime(adjustTimeString(value));
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
                    dsm.setPeakWindTime(adjustTimeString(value));
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
                    float val;
                    if (value.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                        val = -1;
                    } else {
                        val = Float.parseFloat(value) / 10;
                    }

                    dsm.setSnowAmount(val);
                } else if (groupName.equalsIgnoreCase("depthOfSnow")) {
                    short val;
                    if (value.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                        val = -1;
                    } else {
                        val = Short.parseShort(value);
                    }

                    dsm.setDepthOfSnow(val);
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
                            + "] with value: [" + value + "] in ASOS message: ["
                            + message + "]");
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing number for group: [" + groupName
                        + "] with value: [" + value + "] in ASOS message: ["
                        + message + "]", e);
            } catch (RuntimeException e) {
                logger.error("Unexpected exception for group: [" + groupName
                        + "] with value: [" + value + "] in ASOS message: ["
                        + message + "]", e);
            }
        }

        if (!missingGroups.isEmpty()) {
            logger.warn("No values given for ASOS group names: ["
                    + Arrays.toString(missingGroups.toArray())
                    + "] in ASOS message: [" + message + "]");
        }

        return dsm;
    }
}
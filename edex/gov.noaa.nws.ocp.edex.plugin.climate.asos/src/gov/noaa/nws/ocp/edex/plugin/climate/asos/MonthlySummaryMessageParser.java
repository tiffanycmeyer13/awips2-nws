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

import gov.noaa.nws.ocp.common.dataplugin.climate.asos.ClimateASOSMessageRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.asos.MonthlySummaryRecord;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;

/**
 * MSM Message Parser
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 12, 2016 16962      pwang       Initial creation
 * 25 JAN 2017  23221      amoore      Use common constants.
 * 03 MAY 2017  33104      amoore      Use generic map and set.
 * 05 MAY 2017  33104      amoore      Minor clean up.
 * 08 MAY 2017  33104      amoore      Fix regex. Allow multiple
 *                                     remarks. Allow whitespaces.
 *                                     Use regular field assignment.
 * 07 SEP 2017  37725      amoore      Fix regex. Days max/min above/below can be
 *                                     missing for each field instead of just as a whole.
 *                                     Epr remark can be all caps. Max precip values
 *                                     can be "00" (missing). Max/min SLP can be missing for
 *                                     the whole rather than each field. Add comments.
 * 08 SEP 2017  37769      amoore      Precip date and time for periods only if precip is
 *                                     not trace. Average temps can be 4 digits.
 * 31 OCT 2017  40231      amoore      Clean up of MSM/DSM parsing and records. Better
 *                                     logging. Get rid of serialization tags.
 * 03 NOV 2017  36736      amoore      Make several parts and logic static.
 * 07 MAR 2019  DR20939    pwang       Fix the unit of max 24 snow
 * 
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class MonthlySummaryMessageParser extends ASOSMessageParser {

    /**
     * Logger.
     */
    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(MonthlySummaryMessageParser.class);

    /**
     * Group-capturing pattern, require JDK 1.7+
     * 
     * Example products:
     * 
     * KCDJ MS 10 87-02/ 24-2829/686/467/577/00000300/265/46/29185/001/
     * 042160755+/946070414+/528/09080302/2230506/25101115/38060213/
     * 52060215/63060216/80060218/94060226/104060236/121060226/131060241/
     * 141060301/147060327/154060354
     * 
     * KCDJ MS 11 80-01/ 22-20/617/374/496/MMMM/452/10/29265/010/057191038+/
     * 910281500+/00/00000000/00/00/00/00/00/00/00/00/00/00/00/00/00/NN/N/N/
     * NNN/ET EP
     * 
     * KDMH MS 10 87-07/ 44-27/737/579/658/00000000/78/109/30085/011/
     * 062031038+/911292033+/264/11040300/1022930/10120026/19120029/
     * 24120029/32120039/42120046/61120046/67120055/75120058/80120105/
     * 81120115/81120115/81120115
     * 
     * TJNR MS 08 91-1113/ 74-17/892/786/839/00140000/0/593/29960/000/
     * 013032255+/987190351+/615/15080202/2721617/23201634/36170637/
     * 48201634/59201637/68170638/88170644/92170648/128170636/163170636/
     * 180170650/182170720/201170809
     * 
     * KNHK MS 10 87-09/ 36-27/728/535/632/00000000/136/85/30055/011/
     * 059031158+/913291913+/349/10060201/1772930/12240258/20240257/
     * 29240300/32240303/34240314/43240322/43240322/44240322/45240322/
     * 49290715/60290715/64290715
     * 
     * KNSE MS 10 91-13/ 37-30/797/566/682/00030000/74/165/29855/003/
     * 025040822+/980281441+/556/05030202/3552223/40221848/68221851/
     * 100221823/126221827/177221837/255221852/294221901/317221921/
     * 334221937/339221946/341222016/341222016
     * 
     * KSAD MS 06 112-202122/ 52-14/1044/684/864/00300000/0/649/26635/971/
     * 999260840+/950221700+/05/01000000/052525/05252042/05252042/05252042/
     * 05252042/05252042/05252042/05252042/05252042/05252042/05252042/
     * 05252042/05252042/NN/N/N/NNN/EP
     */
    private static final String MSMRAW_REGEX =
    // M,N,- are indicators of missing values. In some cases, 00 also indicates
    // missing
    // 3-4 letter station code, MS, optional COR, 2 digit month
    "(?<stationCode>\\w{3,4})\\s+MS\\s+(?:(?<correction>COR)\\s)?(?<month>\\d{2})\\s"
            // missing, or-- 2-3 digit max temp, and up to 3 two-digit dates
            + "(?:M|N|-(?!\\d)|\\s?(?<maxT>-?\\d{2,3})-(?<maxTDate1>\\d{2})(?<maxTDate2>\\d{2})?(?<maxTDate3>\\d{2})?)"
            // missing, or-- 2-3 digit min temp, and up to 3 two-digit dates
            + "/\\s?(?:M|N|-(?!\\d)|\\s?(?<minT>-?\\d{2,3})-(?<minTDate1>\\d{2})(?<minTDate2>\\d{2})?(?<minTDate3>\\d{2})?)"
            // missing or-- 1-4 digit avg daily max temp with possible minus,
            // followed by same for average min and average monthly
            + "/(?:M|N|-(?!\\d)|(?<avgDailyMaxT>-?\\d{1,4}))/(?:M|N|-(?!\\d)|(?<avgDailyMinT>-?\\d{1,4}))/(?:M|N|-(?!\\d)|(?<avgMonthlyT>-?\\d{1,4}))"
            // missing, or-- 2 digit days max/min below/above some temperatures
            + "/(?:M|N|-|((?:M|N|-|(?<daysMaxTBelow32>\\d{2}))(?:M|N|-|(?<daysMaxTAbove90>\\d{2}))(?:M|N|-|(?<daysMinTBelow32>\\d{2}))(?:M|N|-|(?<daysMinTBelow0>\\d{2}))))"
            // missing, or 1-4 digit heating and cooling degree days
            + "/(?:M|N|-|(?<totalHeatingDegreeDays>\\d{1,4}))/(?:M|N|-|(?<totalCoolingDegreeDays>\\d{1,4}))"
            // missing, or-- 1-5 digit mean station pressure; same but up to 3
            // digits for mean sea pressure
            + "/\\s?(?:M|N|-|(?<meanStationPressure>\\d{1,5}))/\\s?(?:M|N|-|(?<meanSeaLevelPressure>\\d{1,3}))"
            // missing, or-- missing or 1-3 digit max SLP, followed by missing
            // or 2 digit date, followed by missing or 4 digit time, followed by
            // optional +
            + "/\\s?(?:M|N|-|((?:M|N|-|(?<maxSeaLevelPressure>\\d{1,3}))(?:M|N|-|(?<maxSeaLevelPressureDate>\\d{2}))(?:M|N|-|(?<maxSeaLevelPressureTime>\\d{4})(?<multiMaxSLP>\\+)?)))"
            // missing, or-- missing or 1-3 digit min SLP, followed by missing
            // or 2 digit date, followed by missing or 4 digit time, followed by
            // optional +
            + "/\\s?(?:M|N|-|((?:M|N|-|(?<minSeaLevelPressure>\\d{1,3}))(?:M|N|-|(?<minSeaLevelPressureDate>\\d{2}))(?:M|N|-|(?<minSeaLevelPressureTime>\\d{4})(?<multiMinSLP>\\+)?)))"
            // missing, or 1-5 digit precip, or T
            + "/\\s?(?:M|N|-|(?<totalPrecip>\\d{1,5}|T))"
            // missing, or 2 digits (each) for days of precip above certain
            // values
            + "/\\s?(?:M|N|-|(?<daysPrecipAbove01>\\d{2})(?<daysPrecipAbove10>\\d{2})(?<daysPrecipAbove50>\\d{2})(?<daysPrecipAbove100>\\d{2}))"
            // missing, or 1-4 digit (or T) max 24 hour precip, 2 digit start
            // date, 2 digit end date, and optional +
            + "/\\s?(?:00|M|N|-|(?<max24HourPrecip>\\d{1,4}|T)(?<max24HourPrecipStartDate>\\d{2})(?<max24HourPrecipEndDate>\\d{2})(?<multiMax24HourPrecip>\\+)?)"
            // missing, or 1-3 digit (or T) max precip for some time period,
            // then 2 digit date and 4 digit time if precip is not trace
            + "/\\s?(?:00|M|N|-|(?<precip5min>\\d{1,3}|T)((?<=T)|(?<precip5minDate>\\d{2})(?<precip5minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip10min>\\d{1,3}|T)((?<=T)|(?<precip10minDate>\\d{2})(?<precip10minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip15min>\\d{1,3}|T)((?<=T)|(?<precip15minDate>\\d{2})(?<precip15minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip20min>\\d{1,3}|T)((?<=T)|(?<precip20minDate>\\d{2})(?<precip20minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip30min>\\d{1,3}|T)((?<=T)|(?<precip30minDate>\\d{2})(?<precip30minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip45min>\\d{1,3}|T)((?<=T)|(?<precip45minDate>\\d{2})(?<precip45minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip60min>\\d{1,3}|T)((?<=T)|(?<precip60minDate>\\d{2})(?<precip60minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip80min>\\d{1,3}|T)((?<=T)|(?<precip80minDate>\\d{2})(?<precip80minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip100min>\\d{1,3}|T)((?<=T)|(?<precip100minDate>\\d{2})(?<precip100minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip120min>\\d{1,3}|T)((?<=T)|(?<precip120minDate>\\d{2})(?<precip120minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip150min>\\d{1,3}|T)((?<=T)|(?<precip150minDate>\\d{2})(?<precip150minTime>\\d{4})))"
            + "/\\s?(?:00|M|N|-|(?<precip180min>\\d{1,3}|T)((?<=T)|(?<precip180minDate>\\d{2})(?<precip180minTime>\\d{4})))"
            // missing (with any number of M or N), or 1-4 digit sunshine hours,
            // followed by either 100 or a 2 digit sunshine percent
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<sunshineHours>\\d{1,4}(?=\\d{2}|100))(?<sunshinePercent>100|\\d{2})))?"
            // missing (with any number of M or N), or 1-3 digit max 24 hour
            // snow, 2 digit start date, 2 digit end date, and optional +
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<max24HourSnow>\\d{1,3})(?:(?<!/0)(?<max24HourSnowStartDate>\\d{2})(?<max24HourSnowEndDate>\\d{2})(?<multiMax24HourSnow>\\+)?)?))?"
            // missing (with any number of M or N), or 1-4 digit max snow depth,
            // 2 digit date, and optional +
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<maxSnowDepth>\\d{1,4}?)(?:(?<!/0)(?<maxSnowDepthDate>\\d{2})(?<multiMaxSnowDepth>\\+)?)?))?"
            // missing (with any number of M or N), or 2 digits each of
            // clear/partly cloudy/cloudy days
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<clearDays>\\d{2})(?<partlyCloudyDays>\\d{2})(?<cloudyDays>\\d{2})))?"
            // 1-8 remarks from the given list
            + "(?:/\\s?(?<remarks>((?:ET|Epr|EPR|EP|ES|ESw|ESd|EC)\\s?){1,8}))?";

    /**
     * Set of named groups.
     */
    private static final Set<String> NAMED_GROUPS = new HashSet<>();

    /**
     * Build name set for named groups.
     */
    static {
        Matcher m = GROUP_NAME_PATTERN.matcher(MSMRAW_REGEX);
        while (m.find()) {
            NAMED_GROUPS.add(m.group(1));
        }
    }

    /**
     * Empty constructor.
     */
    public MonthlySummaryMessageParser() {
    }

    @Override
    public ClimateASOSMessageRecord parse(String message) {
        Pattern msmPattern = Pattern.compile(MSMRAW_REGEX);
        final Matcher matcher = msmPattern.matcher(message);

        Map<String, String> namedAsosMap = new HashMap<>();

        if (matcher.matches()) {
            // ASOS message match the DMS pattern
            for (String groupName : NAMED_GROUPS) {
                if (matcher.group(groupName) != null)
                    namedAsosMap.put(groupName, matcher.group(groupName));
            }
        } else {
            logger.error("Message: [" + message
                    + "] does not match expected pattern: [" + MSMRAW_REGEX
                    + "]");
        }

        // sets values to default
        MonthlySummaryRecord msm = new MonthlySummaryRecord(message);

        List<String> missingGroups = new ArrayList<>();

        for (String groupName : NAMED_GROUPS) {
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
                    msm.setStationCode(value);
                } else if (groupName.equalsIgnoreCase("correction")) {
                    msm.setCorrection(true);
                } else if (groupName.equalsIgnoreCase("month")) {
                    msm.setMonth(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("maxT")) {
                    msm.setMaxT(Short.parseShort(value));
                } else if (groupName.startsWith("maxTDate")) {
                    int index = Integer
                            .parseInt(groupName.replace("maxTDate", "")) - 1;

                    msm.getMaxTDates()[index] = Short.parseShort(value);
                } else if (groupName.equalsIgnoreCase("minT")) {
                    msm.setMinT(Short.parseShort(value));
                } else if (groupName.startsWith("minTDate")) {
                    int index = Integer
                            .parseInt(groupName.replace("minTDate", "")) - 1;

                    msm.getMinTDates()[index] = Short.parseShort(value);
                } else if (groupName.equalsIgnoreCase("avgDailyMaxT")) {
                    /*
                     * Average daily maximum temperature, reported to the
                     * nearest 0.1 degree Fahrenheit
                     */
                    msm.setAvgDailyMaxT(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("avgDailyMinT")) {
                    /*
                     * Average daily minimum temperature, reported to the
                     * nearest 0.1 degree Fahrenheit
                     */
                    msm.setAvgDailyMinT(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("avgMonthlyT")) {
                    /*
                     * Average monthly temperature, reported to the nearest 0.1
                     * degree Fahrenheit
                     */
                    msm.setAvgMonthlyT(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("daysMaxTBelow32")) {
                    msm.setDaysMaxTBelow32(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("daysMaxTAbove90")) {
                    msm.setDaysMaxTAbove90(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("daysMinTBelow32")) {
                    msm.setDaysMinTBelow32(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("daysMinTBelow0")) {
                    msm.setDaysMinTBelow0(Short.parseShort(value));
                } else if (groupName
                        .equalsIgnoreCase("totalHeatingDegreeDays")) {
                    msm.setTotalHeatingDegreeDays(Short.parseShort(value));
                } else if (groupName
                        .equalsIgnoreCase("totalCoolingDegreeDays")) {
                    msm.setTotalCoolingDegreeDays(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("meanStationPressure")) {
                    /*
                     * Monthly mean station pressure, reported to the nearest
                     * 0.005 inch of Hg
                     */
                    msm.setMeanStationPressure(Float.parseFloat(value) / 1000);
                } else if (groupName.equalsIgnoreCase("meanSeaLevelPressure")) {
                    /*
                     * Monthly mean sea-level pressure, reported to the nearest
                     * 0.01 inch of Hg
                     */
                    float val = Float.parseFloat(value);
                    msm.setMeanSeaLevelPressure(
                            (val < 500 ? 30.0f : 20.0f) + val / 100.0f);
                } else if (groupName.equalsIgnoreCase("maxSeaLevelPressure")) {
                    /*
                     * Monthly max sea-level pressure, reported to the nearest
                     * 0.01 inch of Hg
                     */
                    float val = Float.parseFloat(value);
                    msm.setMaxSeaLevelPressure(
                            (val < 500 ? 30.0f : 20.0f) + val / 100.0f);
                } else if (groupName
                        .equalsIgnoreCase("maxSeaLevelPressureDate")) {
                    msm.setMaxSeaLevelPressureDate(value);
                } else if (groupName
                        .equalsIgnoreCase("maxSeaLevelPressureTime")) {
                    msm.setMaxSeaLevelPressureTime(value);
                } else if (groupName.equalsIgnoreCase("multiMaxSLP")) {
                    msm.setMultiMaxSLP(value);
                } else if (groupName.equalsIgnoreCase("minSeaLevelPressure")) {
                    /*
                     * Monthly min sea-level pressure, reported to the nearest
                     * 0.01 inch of Hg
                     */
                    float val = Float.parseFloat(value);
                    msm.setMinSeaLevelPressure(
                            (val < 500 ? 30.0f : 20.0f) + val / 100.0f);
                } else if (groupName
                        .equalsIgnoreCase("minSeaLevelPressureDate")) {
                    msm.setMinSeaLevelPressureDate(value);
                } else if (groupName
                        .equalsIgnoreCase("minSeaLevelPressureTime")) {
                    msm.setMinSeaLevelPressureTime(value);
                } else if (groupName.equalsIgnoreCase("multiMinSLP")) {
                    msm.setMultiMinSLP(value);
                } else if (groupName.toLowerCase().endsWith("precip")) {
                    if (groupName.equalsIgnoreCase("multiMax24HourPrecip")) {
                        msm.setMultiMax24HourPrecip(true);
                    } else {
                        float val;
                        if (value.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                            val = -1;
                        } else {
                            val = Float.parseFloat(value) / 100;
                        }

                        if (groupName.equalsIgnoreCase("totalPrecip")) {
                            msm.setTotalPrecip(val);
                        } else if (groupName
                                .equalsIgnoreCase("max24HourPrecip")) {
                            msm.setMax24HourPrecip(val);
                        } else {
                            logger.error(
                                    "Unhandled MSM precip group: [" + groupName
                                            + "] with value: [" + val + "]");
                        }
                    }
                } else if (groupName.equalsIgnoreCase("daysPrecipAbove01")) {
                    msm.setDaysPrecipAbove01(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("daysPrecipAbove10")) {
                    msm.setDaysPrecipAbove10(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("daysPrecipAbove50")) {
                    msm.setDaysPrecipAbove50(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("daysPrecipAbove100")) {
                    msm.setDaysPrecipAbove100(Short.parseShort(value));
                } else if (groupName
                        .equalsIgnoreCase("max24HourPrecipStartDate")) {
                    msm.setMax24HourPrecipStartDate(Short.parseShort(value));
                } else if (groupName
                        .equalsIgnoreCase("max24HourPrecipEndDate")) {
                    msm.setMax24HourPrecipEndDate(Short.parseShort(value));
                } else if (groupName.startsWith("precip")) {
                    if (groupName.endsWith("min")) {
                        float val;
                        if (value.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                            val = -1;
                        } else {
                            val = Float.parseFloat(value) / 100;
                        }

                        if (groupName.equalsIgnoreCase("precip5min")) {
                            msm.setPrecip5min(val);
                        } else if (groupName.equalsIgnoreCase("precip10min")) {
                            msm.setPrecip10min(val);
                        } else if (groupName.equalsIgnoreCase("precip15min")) {
                            msm.setPrecip15min(val);
                        } else if (groupName.equalsIgnoreCase("precip20min")) {
                            msm.setPrecip20min(val);
                        } else if (groupName.equalsIgnoreCase("precip30min")) {
                            msm.setPrecip30min(val);
                        } else if (groupName.equalsIgnoreCase("precip45min")) {
                            msm.setPrecip45min(val);
                        } else if (groupName.equalsIgnoreCase("precip60min")) {
                            msm.setPrecip60min(val);
                        } else if (groupName.equalsIgnoreCase("precip80min")) {
                            msm.setPrecip80min(val);
                        } else if (groupName.equalsIgnoreCase("precip100min")) {
                            msm.setPrecip100min(val);
                        } else if (groupName.equalsIgnoreCase("precip120min")) {
                            msm.setPrecip120min(val);
                        } else if (groupName.equalsIgnoreCase("precip150min")) {
                            msm.setPrecip150min(val);
                        } else if (groupName.equalsIgnoreCase("precip180min")) {
                            msm.setPrecip180min(val);
                        } else {
                            logger.error("Unhandled MSM precip duration: ["
                                    + groupName + "] with value: [" + val
                                    + "]");
                        }
                    } else {
                        if (groupName.equalsIgnoreCase("precip5minDate")) {
                            msm.setPrecip5minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip5minTime")) {
                            msm.setPrecip5minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip10minDate")) {
                            msm.setPrecip10minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip10minTime")) {
                            msm.setPrecip10minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip15minDate")) {
                            msm.setPrecip15minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip15minTime")) {
                            msm.setPrecip15minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip20minDate")) {
                            msm.setPrecip20minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip20minTime")) {
                            msm.setPrecip20minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip30minDate")) {
                            msm.setPrecip30minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip30minTime")) {
                            msm.setPrecip30minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip45minDate")) {
                            msm.setPrecip45minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip45minTime")) {
                            msm.setPrecip45minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip60minDate")) {
                            msm.setPrecip60minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip60minTime")) {
                            msm.setPrecip60minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip80minDate")) {
                            msm.setPrecip80minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip80minTime")) {
                            msm.setPrecip80minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip100minDate")) {
                            msm.setPrecip100minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip100minTime")) {
                            msm.setPrecip100minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip120minDate")) {
                            msm.setPrecip120minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip120minTime")) {
                            msm.setPrecip120minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip150minDate")) {
                            msm.setPrecip150minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip150minTime")) {
                            msm.setPrecip150minTime(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip180minDate")) {
                            msm.setPrecip180minDate(value);
                        } else if (groupName
                                .equalsIgnoreCase("precip180minTime")) {
                            msm.setPrecip180minTime(value);
                        } else {
                            logger.error("Unhandled precip duration group: ["
                                    + groupName + "] with value: [" + value
                                    + "]");
                        }
                    }
                } else if (groupName.equalsIgnoreCase("sunshineHours")) {
                    /*
                     * Hours of sunshine, reported to the nearest 0.1 hour
                     */
                    msm.setSunshineHours(Float.parseFloat(value) / 10);
                } else if (groupName.equalsIgnoreCase("sunshinePercent")) {
                    msm.setSunshinePercent(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("max24HourSnow")) {
                    /*
                     * Convert from tenth of inch (reported in MSM) to inch
                     */
                    float val = Float.parseFloat(value) / 10;
                    msm.setMax24HourSnow(val);
                } else if (groupName
                        .equalsIgnoreCase("max24HourSnowStartDate")) {
                    msm.setMax24HourSnowStartDate(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("max24HourSnowEndDate")) {
                    msm.setMax24HourSnowEndDate(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("multiMax24HourSnow")) {
                    msm.setMultiMax24HourSnow(true);
                } else if (groupName.equalsIgnoreCase("maxSnowDepth")) {
                    msm.setMaxSnowDepth(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("maxSnowDepthDate")) {
                    msm.setMaxSnowDepthDate(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("multiMaxSnowDepth")) {
                    msm.setMultiMaxSnowDepth(true);
                } else if (groupName.equalsIgnoreCase("clearDays")) {
                    msm.setClearDays(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("partlyCloudyDays")) {
                    msm.setPartlyCloudyDays(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("cloudyDays")) {
                    msm.setCloudyDays(Short.parseShort(value));
                } else if (groupName.equalsIgnoreCase("remarks")) {
                    msm.setRemarks(value);
                } else {
                    logger.error("Unhandled MSM group: [" + groupName
                            + "] with value: [" + value + "] in ASOS message: ["
                            + message + "]");
                }

            } catch (NumberFormatException e) {
                logger.error("Failed to parse number for group: [" + groupName
                        + "] with value: [" + value + "] in ASOS message: ["
                        + message + "]", e);
            } catch (RuntimeException e) {
                logger.error("Unexpected exception for group: [" + groupName
                        + "] with value: [" + value + "] in ASOS message: ["
                        + message + "]", e);
            }
        }

        if (!missingGroups.isEmpty()) {
            logger.warn("No values for ASOS group names: ["
                    + Arrays.toString(missingGroups.toArray())
                    + "] in ASOS message: [" + message + "]");
        }

        return msm;
    }

}

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

    // Group-capturing pattern, require JDK 1.7+
    private static final String MSMRAW_REGEX = "(?<stationCode>\\w{3,4})\\s+MS\\s+(?:(?<correction>COR)\\s)?(?<month>\\d{2})\\s"
            + "(?:M|N|-(?!\\d)|\\s?(?<maxT>-?\\d{2,3})-(?<maxTDate1>\\d{2})(?<maxTDate2>\\d{2})?(?<maxTDate3>\\d{2})?)"
            + "/(?:M|N|-(?!\\d)|\\s?(?<minT>-?\\d{2,3})-(?<minTDate1>\\d{2})(?<minTDate2>\\d{2})?(?<minTDate3>\\d{2})?)"
            + "/(?:M|N|-(?!\\d)|(?<avgDailyMaxT>-?\\d{1,3}))/(?:M|N|-(?!\\d)|(?<avgDailyMinT>-?\\d{1,3}))/(?:M|N|-(?!\\d)|(?<avgMonthlyT>-?\\d{1,3}))"
            + "/(?:M|N|-|(?<daysMaxTBelow32>\\d{2})(?<daysMaxTAbove90>\\d{2})(?<daysMinTBelow32>\\d{2})(?<daysMinTBelow0>\\d{2}))"
            + "/(?:M|N|-|(?<totalHeatingDegreeDays>\\d{1,4}))/(?:M|N|-|(?<totalCoolingDegreeDays>\\d{1,4}))"
            + "/\\s?(?:M|N|-|(?<meanStationPressure>\\d{1,5}))/\\s?(?:M|N|-|(?<meanSeaLevelPressure>\\d{1,3}))"
            + "/\\s?(?:M|N|-|(?<maxSeaLevelPressure>\\d{1,3}))(?:M|N|-|(?<maxSeaLevelPressureDate>\\d{2}))(?:M|N|-|(?<maxSeaLevelPressureTime>\\d{4})(?<multiMaxSLP>\\+)?)"
            + "/\\s?(?:M|N|-|(?<minSeaLevelPressure>\\d{1,3}))(?:M|N|-|(?<minSeaLevelPressureDate>\\d{2}))(?:M|N|-|(?<minSeaLevelPressureTime>\\d{4})(?<multiMinSLP>\\+)?)"
            + "/\\s?(?:M|N|-|(?<totalPrecip>\\d{1,5}))"
            + "/\\s?(?:M|N|-|(?<daysPrecipAbove01>\\d{2})(?<daysPrecipAbove10>\\d{2})(?<daysPrecipAbove50>\\d{2})(?<daysPrecipAbove100>\\d{2}))"
            + "/\\s?(?:M|N|-|(?<max24HourPrecip>\\d{1,4})(?<max24HourPrecipStartDate>\\d{2})(?<max24HourPrecipEndDate>\\d{2})(?<multiMax24HourPrecip>\\+)?)"
            + "/\\s?(?:M|N|-|(?<precip5min>\\d{1,3})(?<precip5minDate>\\d{2})(?<precip5minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip10min>\\d{1,3})(?<precip10minDate>\\d{2})(?<precip10minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip15min>\\d{1,3})(?<precip15minDate>\\d{2})(?<precip15minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip20min>\\d{1,3})(?<precip20minDate>\\d{2})(?<precip20minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip30min>\\d{1,3})(?<precip30minDate>\\d{2})(?<precip30minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip45min>\\d{1,3})(?<precip45minDate>\\d{2})(?<precip45minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip60min>\\d{1,3})(?<precip60minDate>\\d{2})(?<precip60minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip80min>\\d{1,3})(?<precip80minDate>\\d{2})(?<precip80minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip100min>\\d{1,3})(?<precip100minDate>\\d{2})(?<precip100minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip120min>\\d{1,3})(?<precip120minDate>\\d{2})(?<precip120minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip150min>\\d{1,3})(?<precip150minDate>\\d{2})(?<precip150minTime>\\d{4}))"
            + "/\\s?(?:M|N|-|(?<precip180min>\\d{1,3})(?<precip180minDate>\\d{2})(?<precip180minTime>\\d{4}))"
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<sunshineHours>\\d{1,4}(?=\\d{2}|100))(?<sunshinePercent>100|\\d{2})))?"
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<max24HourSnow>\\d{1,3})(?:(?<!/0)(?<max24HourSnowStartDate>\\d{2})(?<max24HourSnowEndDate>\\d{2})(?<multiMax24HourSnow>\\+)?)?))?"
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<maxSnowDepth>\\d{1,4}?)(?:(?<!/0)(?<maxSnowDepthDate>\\d{2})(?<multiMaxSnowDepth>\\+)?)?))?"
            + "(?:/\\s?(?:M{1,}|N{1,}|-|(?<clearDays>\\d{2})(?<partlyCloudyDays>\\d{2})(?<cloudyDays>\\d{2})))?"
            + "(?:/\\s?(?<remarks>((?:ET|Epr|EP|ES|ESw|ESd|EC)\\s?){1,7}))?";

    private Pattern msmPattern;

    public MonthlySummaryMessageParser() {
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
        Matcher m = groupNamePattern.matcher(MSMRAW_REGEX);
        while (m.find()) {
            namedGroups.add(m.group(1));
        }
    }

    @Override
    public ClimateASOSMessageRecord parse(String message) {

        msmPattern = Pattern.compile(MSMRAW_REGEX);
        final Matcher matcher = msmPattern.matcher(message);
        if (matcher.matches()) {
            // ASOS message match the DMS pattern
            for (String groupName : namedGroups) {
                if (matcher.group(groupName) != null)
                    namedAsosMap.put(groupName, matcher.group(groupName));
            }
        }

        // sets values to default
        MonthlySummaryRecord msm = new MonthlySummaryRecord();

        List<String> missingGroups = new ArrayList<>();

        for (String groupName : namedGroups) {
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
                    float val;
                    if (value.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                        val = -1;
                    } else {
                        val = Float.parseFloat(value) / 100;
                    }

                    if (groupName.equalsIgnoreCase("totalPrecip")) {
                        msm.setTotalPrecip(val);
                    } else if (groupName.equalsIgnoreCase("max24HourPrecip")) {
                        msm.setMax24HourPrecip(val);
                    } else {
                        logger.error("Unhandled MSM precip group: [" + groupName
                                + "] with value: [" + val + "]");
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
                } else if (groupName.equalsIgnoreCase("multiMax24HourPrecip")) {
                    msm.setMultiMax24HourPrecip(true);
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
                    msm.setMax24HourSnow(Float.parseFloat(value));
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
                            + "] with value: [" + value + "]");
                }

            } catch (NumberFormatException e) {
                logger.error("Failed to parse number for group: [" + groupName
                        + "] with value: [" + value + "]", e);
            } catch (RuntimeException e) {
                logger.error("Unexpected exception for group: [" + groupName
                        + "] with value: [" + value + "]", e);
            }
        }

        if (!missingGroups.isEmpty()) {
            logger.info("No value for ASOS group names: ["
                    + Arrays.toString(missingGroups.toArray()) + "]");
        }

        return msm;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.WeatherStrings;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.rer.RecordClimateRawData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.RelativeHumidityControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.SkycoverControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.TempRecordControlFlags;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateDAOUtils;

/**
 * Class containing logic for building NWWS daily products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2017 21099      wpaintsil   Initial creation
 * May 19, 2017 30163      wpaintsil   Create a list of new daily records to use for RERs.
 * 13 JUL 2017  33104      amoore      Use common rounding.
 * 24 AUG 2017  37328      amoore      Formatter should not change data.
 * 12 OCT 2017  35865      wpaintsil   Don't generate duplicate RER.
 * 17 NOV 2017  41018      amoore      Get rid of unnecessary assignments, take advantage
 *                                     of aliasing.
 * 20 NOV 2017  41088      amoore      Remove unnecessary double-checking of report windows
 *                                     for snow section.
 * 07 MAY 2018  20714      amoore      RER temperature values should be ints, while precip/
 *                                     snow remain as float.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class ClimateNWWSDailyFormat extends ClimateNWWSFormat {

    /**
     * List used to hold new daily records used to create RERs
     */
    private List<RecordClimateRawData> dailyRecordData = new ArrayList<>();

    private enum PrecipPeriod {
        DAY, MONTH, SEASON, YEAR;
    }

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateNWWSDailyFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        super(currentSettings, globalConfig);
    }

    /**
     * Migrated from build_wire.f
     * 
     * <pre>
     * 
     *   September 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *  Purpose:  This routine is the driver responsible for building the
    *            NWWS evening climate summary.  It controls the construction
    *            of new lines which are associated with a particular type of
    *            meteorological variable.
     *
     * </pre>
     * 
     * @param reportData
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    public Map<String, ClimateProduct> buildText(ClimateRunData reportData)
            throws ClimateQueryException, ClimateInvalidParameterException {
        Map<String, ClimateProduct> prod = new HashMap<>();

        boolean morning;

        if (currentSettings.getReportType() == PeriodType.MORN_NWWS) {
            morning = true;
        } else {
            morning = false;
        }

        Map<Integer, ClimateDailyReportData> reportMap = ((ClimateRunDailyData) reportData)
                .getReportMap();

        StringBuilder productText = new StringBuilder();

        productText.append(buildNWWSHeader(reportData.getBeginDate()));

        // Match station(s) in currentSettings with stations and data in
        // reportMap.
        for (Station station : currentSettings.getStations()) {
            Integer stationId = station.getInformId();

            ClimateDailyReportData report = reportMap.get(stationId);
            if (report == null) {
                logger.warn("The station with informId " + stationId
                        + " defined in the ClimateProductType settings object was not found in the ClimateRunData report map.");
                continue;
            }

            productText.append(buildNWWSComment(stationId, reportData));

            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isMeasured()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isMeasured()
                    || currentSettings.getControl().getTempControl()
                            .getMeanTemp().isMeasured()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipTotal().isMeasured()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowTotal().isMeasured()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthAvg().isMeasured()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getTotalHDD().isMeasured()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getTotalCDD().isMeasured()) {
                productText
                        .append(buildNWWSTables(morning, reportData, report));
            }

            productText.append(buildNWWSWind(report));

            productText.append(buildNWWSSkyAndWeather(report));

            productText.append(
                    buildNWWSWeather(reportMap.get(stationId), morning));

            productText.append(buildNWWSRelHumd(report));

            if ((currentSettings.getControl().getWindControl().getMeanWind()
                    .isMeasured()
                    || currentSettings.getControl().getWindControl()
                            .getMaxWind().isMeasured()
                    || currentSettings.getControl().getWindControl()
                            .getMaxGust().isMeasured()
                    || currentSettings.getControl().getWindControl()
                            .getResultWind().isMeasured()
                    || currentSettings.getControl().getWeatherControl()
                            .isWeather()
                    || currentSettings.getControl().getRelHumidityControl()
                            .getMaxRH().isMeasured()
                    || currentSettings.getControl().getRelHumidityControl()
                            .getMinRH().isMeasured()
                    || currentSettings.getControl().getRelHumidityControl()
                            .getMeanRH().isMeasured())
                    && (currentSettings.getControl().getSunControl().isSunrise()
                            || currentSettings.getControl().getSunControl()
                                    .isSunset()
                            || currentSettings.getControl()
                                    .getTempRecordControl().isMaxTempNorm()
                            || currentSettings.getControl()
                                    .getTempRecordControl().isMinTempNorm()
                            || currentSettings.getControl()
                                    .getTempRecordControl().isMaxTempRecord()
                            || currentSettings.getControl()
                                    .getTempRecordControl().isMinTempRecord()
                            || currentSettings.getControl()
                                    .getTempRecordControl().isMaxTempYear()
                            || currentSettings.getControl()
                                    .getTempRecordControl().isMinTempYear())) {

                productText.append(separator(58).toString()).append("\n\n");

            }

            productText.append(buildNWWSNormTable(stationId,
                    (ClimateRunDailyData) reportData, report, morning));
            productText.append(
                    buildNWWSAstro((ClimateRunDailyData) reportData, report));
            productText.append(NWWS_FOOTNOTE).append("\n\n");
        }

        productText.append("\n\n").append(PRODUCT_TERMINATOR).append("\n");

        prod.put(getName(),
                getProduct(applyGlobalConfig(productText.toString())));

        return prod;

    }

    /**
     * Migrated from build_NWWS_tables.f
     * 
     * <pre>
     *   March 1998     Jason P. Tuell        PRC/TDL
    *   July  1998     Barry N. Baxter       PRC/TDL
    *   July  1998     Dan J. Zipper         PRC/TDL
    *
    *
    *   Purpose:  This subroutine is to call the drivers for building the
    *             temperatures, precipitations, heat and cool, and the 
    *             footnote sentences for the NOAA Weather Wire Service (NWWS).
     * </pre>
     * 
     * @param morning
     * @param report
     * @param reportData
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSTables(boolean morning, ClimateRunData report,
            ClimateDailyReportData reportData)
            throws ClimateQueryException, ClimateInvalidParameterException {
        StringBuilder nwwsTables = new StringBuilder();

        ColumnSpaces tabs = new ColumnSpaces();
        tabs.setDailyTabs(currentSettings, report.getBeginDate());

        // Build the text headers for the columns.
        nwwsTables.append(buildNWWSColumns(tabs));

        // Build temperature section
        nwwsTables.append(buildNWWSTemp(tabs, report, reportData, morning));

        // Build precipitation section
        nwwsTables.append(buildNWWSPrecip(tabs, report, reportData, morning));

        // Build heat and cool section
        nwwsTables.append(
                buildNWWSHeatAndCool(tabs, report, reportData, morning));

        // Append separator
        if (tabs.isTime() || tabs.isRecord() || tabs.isYear() || tabs.isNorm()
                || tabs.isDepart() || tabs.isLast()
                || tabs.getPosActual() > 0) {

            nwwsTables.append(separator(tabs.getPosLast()).toString())
                    .append("\n\n");
        }

        return nwwsTables.toString();
    }

    /**
     * Migrated from build_NWWS_wind.f
     * 
     * <pre>
     *   March 1998     Jason P. Tuell        PRC/TDL
    *   July  1998     Barry N. Baxter       PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *   Purpose:  This routine controls building the wind lines in the NOAA
    *             Weather Wire Service daily climate summary.  The user has
    *             control over which pieces of the wind climatology are reported.
    *             If a particular piece of information is neglected in all the
    *             variables, then that column is omitted in the report.
     * 
     * </pre>
     * 
     * @param climateDailyReportData
     * @return
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSWind(ClimateDailyReportData climateDailyReportData)
            throws ClimateInvalidParameterException {
        StringBuilder nwwsWind = new StringBuilder();

        DailyClimateData yesterday = climateDailyReportData.getData();

        ClimateProductFlags resultWindFlags = currentSettings.getControl()
                .getWindControl().getResultWind();
        ClimateProductFlags maxWindFlags = currentSettings.getControl()
                .getWindControl().getMaxWind();
        ClimateProductFlags maxGustFlags = currentSettings.getControl()
                .getWindControl().getMaxGust();
        ClimateProductFlags avgWindFlags = currentSettings.getControl()
                .getWindControl().getMeanWind();

        /**
         * Create wind section
         * 
         * <pre>
         * Example:
         * 
         * WIND (MPH)
         *   HIGHEST WIND SPEED    17   HIGHEST WIND DIRECTION     N (340)
         *   HIGHEST GUST SPEED    23   HIGHEST GUST DIRECTION     N (340)
         *   AVERAGE WIND SPEED     7.9
         * </pre>
         */
        if (resultWindFlags.isMeasured() || maxWindFlags.isMeasured()
                || maxGustFlags.isMeasured() || avgWindFlags.isMeasured()) {
            StringBuilder nwwsWindLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);
            String wind_mph = WordUtils.capitalize(WIND) + SPACE
                    + MPH.toUpperCase();
            nwwsWindLine1.replace(0, wind_mph.length(), wind_mph);

            nwwsWind.append("\n").append(nwwsWindLine1.toString()).append("\n");
        }

        if (resultWindFlags.isMeasured()) {

            nwwsWind.append(speedDir(RESULTANT + SPACE + WIND,
                    yesterday.getResultWind())).append("\n");
        }
        if (maxWindFlags.isMeasured()) {
            nwwsWind.append(
                    speedDir(HIGHEST + SPACE + WIND, yesterday.getMaxWind()))
                    .append("\n");
        }

        if (maxGustFlags.isMeasured()) {
            nwwsWind.append(
                    speedDir(HIGHEST + SPACE + GUST, yesterday.getMaxGust()))
                    .append("\n");
        }

        if (avgWindFlags.isMeasured()) {
            StringBuilder speedDirLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            String windSpeedStr = WordUtils
                    .capitalize(AVERAGE + SPACE + WIND + SPACE + SPEED);
            speedDirLine.replace(2, 2 + windSpeedStr.length(), windSpeedStr);

            if (yesterday
                    .getAvgWindSpeed() != ParameterFormatClimate.MISSING_SPEED) {
                float avgMph = yesterday.getAvgWindSpeed();
                int numDigits = String.format(FLOAT_ONE_DECIMAL, avgMph)
                        .length();

                int adjust;
                if (numDigits == 2) {
                    adjust = 2;
                } else if (numDigits == 1) {
                    adjust = 3;
                } else if (numDigits == 3) {
                    adjust = 3;
                } else if (numDigits == 4) {
                    adjust = 2;
                } else if (numDigits == 5) {
                    adjust = 1;
                } else {
                    adjust = 0;
                }

                speedDirLine.replace(22 + adjust, 30,
                        String.format(FLOAT_ONE_DECIMAL, avgMph));

            } else {
                speedDirLine.replace(24, 27, ParameterFormatClimate.MM);
            }

            nwwsWind.append(speedDirLine.toString()).append("\n");
        }
        if (resultWindFlags.isMeasured() || maxWindFlags.isMeasured()
                || maxGustFlags.isMeasured() || avgWindFlags.isMeasured()) {
            nwwsWind.append("\n");
        }

        return nwwsWind.toString();
    }

    /**
     * Migrated from build_NWWS_weather.f
     * 
     * <pre>
     *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Dan J. Zipper         PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *   Purpose:  This routine will create the weather portion of the NWWS
    *             daily climate summary. The title "The following weather was
    *             observed (today/yesterday) depending on whether or not its a
    *             morning or evening report. All of the weather conditions will
    *             then be listed.
     * 
     * </pre>
     * 
     * @param climateDailyReportData
     * @param morning
     * @return
     */
    private String buildNWWSWeather(
            ClimateDailyReportData climateDailyReportData, boolean morning) {
        StringBuilder nwwsWeather = new StringBuilder();

        DailyClimateData yesterday = climateDailyReportData.getData();

        /**
         * Create weather section
         * 
         * <pre>
         * Example:
         * 
         * Weather Conditions
         *  The following weather was recorded yesterday.
         *   thunderstorm
         *   rain
         *   light rain
         *   fog
         * </pre>
         */
        if (currentSettings.getControl().getWeatherControl().isWeather()) {
            StringBuilder nwwsWeatherLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            StringBuilder nwwsWeatherLine2 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            nwwsWeatherLine1.replace(0, WEATHER_CONDITIONS.length(),
                    WEATHER_CONDITIONS);

            String weatherPhrase1 = "The following weather was recorded "
                    + (morning ? YESTERDAY : TODAY) + ".";

            nwwsWeatherLine2.replace(0, weatherPhrase1.length(),
                    weatherPhrase1);

            nwwsWeather.append("\n").append(nwwsWeatherLine1.toString())
                    .append("\n").append(nwwsWeatherLine2.toString())
                    .append("\n");

            if (yesterday.getNumWx() == 0
                    || yesterday.getNumWx() == ParameterFormatClimate.MISSING // necessary?
            ) {
                StringBuilder nwwsWeatherLine3 = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS);

                String weatherPhrase2 = "No significant weather was observed.";
                nwwsWeatherLine3.replace(2, weatherPhrase2.length(),
                        weatherPhrase2);

                nwwsWeather.append(nwwsWeatherLine3.toString()).append("\n");
            } else {
                for (int i = 0; i < DailyClimateData.TOTAL_WX_TYPES; i++) {
                    StringBuilder nwwsWeatherLine3 = emptyLine(
                            ParameterFormatClimate.NUM_LINE1_NWWS);

                    if (yesterday.getWxType()[i] == 1) {
                        String wx = WeatherStrings.getString(i);

                        nwwsWeatherLine3.replace(2, 2 + wx.length(), wx);
                        nwwsWeather.append(nwwsWeatherLine3.toString())
                                .append("\n");

                    }
                }
            }
            nwwsWeather.append("\n");
        }

        return nwwsWeather.toString();

    }

    /**
     * Migrated from build_NWWS_sky_and_weather.f
     * 
     * <pre>
     *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Dan J. Zipper         PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL    
    *
    *   Purpose:  This routine controls production of the precipitation 
    *             elements for the NOAA Weather Wire Service reports.  The
    *             data from these elements is reported in the same tabular
    *             format as was the termperature and degree days.
     * </pre>
     * 
     * @param climateDailyReportData
     * @return
     */
    private String buildNWWSSkyAndWeather(
            ClimateDailyReportData climateDailyReportData) {
        StringBuilder nwwsSkyAndWeather = new StringBuilder();

        SkycoverControlFlags skyCoverFlags = currentSettings.getControl()
                .getSkycoverControl();
        DailyClimateData yesterday = climateDailyReportData.getData();

        /**
         * Create sky cover section
         * 
         * <pre>
         * Example:
         * 
         * SKY COVER
         *   POSSIBLE SUNSHINE  MM
         *   AVERAGE SKY COVER 0.5
         * </pre>
         */
        if (skyCoverFlags.isPossSunshine() || skyCoverFlags.isAvgSkycover()) {
            StringBuilder nwwsSkyAndWeatherLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);
            nwwsSkyAndWeatherLine1.replace(0, SKY_COVER.length(), SKY_COVER);
            nwwsSkyAndWeather.append("\n")
                    .append(nwwsSkyAndWeatherLine1.toString()).append("\n");
        }
        if (skyCoverFlags.isPossSunshine()) {
            StringBuilder nwwsSkyAndWeatherLine2 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);
            nwwsSkyAndWeatherLine2.replace(2, POSSIBLE_SUNSHINE.length(),
                    POSSIBLE_SUNSHINE);

            if (yesterday
                    .getPercentPossSun() != ParameterFormatClimate.MISSING) {
                nwwsSkyAndWeatherLine2.replace(
                        23 - String.valueOf(yesterday.getPercentPossSun())
                                .length(),
                        23, String.valueOf(yesterday.getPercentPossSun()));
                nwwsSkyAndWeatherLine2.replace(24, PERCENT.length(), PERCENT);
            } else {
                nwwsSkyAndWeatherLine2.replace(21, 23,
                        ParameterFormatClimate.MM);
            }
            nwwsSkyAndWeather.append(nwwsSkyAndWeatherLine2.toString())
                    .append("\n");
        }

        if (skyCoverFlags.isAvgSkycover()) {
            StringBuilder nwwsSkyAndWeatherLine3 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            nwwsSkyAndWeatherLine3.replace(2, AVG_SKY_COVER.length(),
                    AVG_SKY_COVER);

            if (yesterday.getSkyCover() != ParameterFormatClimate.MISSING) {
                nwwsSkyAndWeatherLine3.replace(20, 23, String.format("%.1f",
                        ClimateUtilities.nint(yesterday.getSkyCover(), 1)));
            } else {
                nwwsSkyAndWeatherLine3.replace(21, 23,
                        ParameterFormatClimate.MM);
            }

            nwwsSkyAndWeather.append(nwwsSkyAndWeatherLine3.toString())
                    .append("\n");
        }

        if (skyCoverFlags.isPossSunshine() || skyCoverFlags.isAvgSkycover()) {
            nwwsSkyAndWeather.append("\n");
        }
        return nwwsSkyAndWeather.toString();
    }

    /**
     * Migrated from build_NWWS_rh.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *
    *   Purpose:
     * 
     * </pre>
     * 
     * @param climateDailyReportData
     * @return
     */
    private String buildNWWSRelHumd(
            ClimateDailyReportData climateDailyReportData) {
        StringBuilder nwwsRelHumd = new StringBuilder();

        RelativeHumidityControlFlags rhFlags = currentSettings.getControl()
                .getRelHumidityControl();
        DailyClimateData yesterday = climateDailyReportData.getData();

        /**
         * Create relative humidity section
         * 
         * <pre>
         * Example:
         * 
         * Relative Humidity (percent)
         *  Highest    92           200 AM
         *  Lowest     35          1200 PM
         *  Average    64
         * </pre>
         * 
         */
        if (rhFlags.getMaxRH().isMeasured() || rhFlags.getMinRH().isMeasured()
                || rhFlags.getMeanRH().isMeasured()) {
            StringBuilder nwwsRelHumdLine1 = new StringBuilder();

            nwwsRelHumdLine1.replace(0, RELATIVE_HUMIDITY.length(),
                    WordUtils.capitalize(RELATIVE_HUMIDITY) + SPACE + "("
                            + PERCENT + ")");

            nwwsRelHumd.append("\n").append(nwwsRelHumdLine1.toString())
                    .append("\n");

            if (rhFlags.getMaxRH().isMeasured()) {
                nwwsRelHumd
                        .append(relHumdLine(HIGHEST, yesterday.getMaxRelHumid(),
                                rhFlags.getMaxRH().isTimeOfMeasured(),
                                yesterday.getMaxRelHumidHour()))
                        .append("\n");
            }

            if (rhFlags.getMinRH().isMeasured()) {
                nwwsRelHumd
                        .append(relHumdLine(LOWEST, yesterday.getMinRelHumid(),
                                rhFlags.getMinRH().isTimeOfMeasured(),
                                yesterday.getMinRelHumidHour()))
                        .append("\n");
            }

            if (rhFlags.getMeanRH().isMeasured()) {
                nwwsRelHumd.append(
                        relHumdLine(AVERAGE, yesterday.getMeanRelHumid(),
                                rhFlags.getMeanRH().isTimeOfMeasured(),
                                ParameterFormatClimate.MISSING))
                        .append("\n");
            }
            nwwsRelHumd.append("\n");
        }

        return nwwsRelHumd.toString();
    }

    /**
     * Migrated from build_NWWS_norm_table.f
     * 
     * <pre>
     *   May 1999    Dan Zipper  PRC/TDL
    *
    *   Purpose:  This routine will create the normal data for a station. This 
    *             routine was created to satisfy field input to have the ability 
    *             to display the climate normals independent of the actual 
    *             temperatures. This table will appear at the bottom of the NWWS
    *             product.
     * 
     * </pre>
     * 
     * @param stationId
     * @param reportData
     * @param climateDailyReportData
     * @param morning
     * @return
     */
    private String buildNWWSNormTable(int stationId,
            ClimateRunDailyData reportData,
            ClimateDailyReportData climateDailyReportData, boolean morning) {
        String stationName = stationMap.get(stationId).getStationName();
        StringBuilder nwwsNormTable = new StringBuilder();

        TempRecordControlFlags normFlags = currentSettings.getControl()
                .getTempRecordControl();

        nwwsNormTable.append("\n");

        /**
         * Create climate normals section
         * 
         * <pre>
         * Example:
         * 
         * The <Station Name> climate normals for today
         *                          Normal    Record    Year
         *  Maximum Temperature (F)   51        70      1943
         *  Minimum Temperature (F)   28         2      1920
         * </pre>
         */

        if (normFlags.isMaxTempNorm() || normFlags.isMinTempNorm()) {
            nwwsNormTable.append("The ").append(stationName)
                    .append(" climate normals for ")
                    .append((morning ? TODAY : TOMORROW));

            StringBuilder nwwsNormLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            nwwsNormLine1.replace(25, 25 + NORMAL.length(), NORMAL);

            if (normFlags.isMaxTempRecord() || normFlags.isMinTempRecord()) {
                nwwsNormLine1.replace(35, 35 + RECORD.length(),
                        WordUtils.capitalize(RECORD));

                if (normFlags.isMaxTempYear() || normFlags.isMinTempYear()) {
                    nwwsNormLine1.replace(45, 45 + YEAR.length(),
                            WordUtils.capitalize(YEAR));
                }
            }

            nwwsNormTable.append("\n").append(nwwsNormLine1.toString())
                    .append("\n");

            if (normFlags.isMaxTempNorm()) {
                nwwsNormTable.append(buildNWWSAnyNorms(reportData,
                        climateDailyReportData, true));
            }
            if (normFlags.isMinTempNorm()) {
                nwwsNormTable.append(buildNWWSAnyNorms(reportData,
                        climateDailyReportData, false));
            }
            nwwsNormTable.append("\n");
        }
        return nwwsNormTable.toString();
    }

    /**
     * Migrated from build_NWWS_astro.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   Jan.  1999     Doug Murphy       PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *   July  1999     Dan Zipper            PRC/TDL
    *
    *
    *   Purpose:  This subroutine will create the structure for sunrise and sunset 
    *             in the output of the National Weather Wire Service (NWWS).  This 
    *             subroutine will also place the sunrise and sunset data into the 
    *             output of the National Weather Wire Service (NNWS).
     * </pre>
     * 
     * @param reportData
     * @param climateDailyReportData
     * @return
     */
    private String buildNWWSAstro(ClimateRunDailyData reportData,
            ClimateDailyReportData climateDailyReportData) {
        StringBuilder nwwsAstro = new StringBuilder();

        /**
         * Create sunrise/sunset section
         * 
         * <pre>
         * Example:
         * 
         * Sunrise and Sunset
         * April  3 2017.........Sunrise   639 AM MDT   Sunset   727 PM MDT
         * April  4 2017.........Sunrise   637 AM MDT   Sunset   728 PM MDT
         * </pre>
         */
        if (currentSettings.getControl().getSunControl().isSunrise()
                || currentSettings.getControl().getSunControl().isSunset()) {
            StringBuilder nwwsAstroLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            int reformat = globalConfig.isNoColon() ? 1 : 0;

            String sunString = WordUtils.capitalize(SUNRISE) + SPACE + AND
                    + SPACE + WordUtils.capitalize(SUNSET);

            nwwsAstroLine1.replace(0, sunString.length(), sunString);
            nwwsAstro.append("\n").append(nwwsAstroLine1).append("\n");

            int julDay1 = 0;
            ClimateDate aDate = new ClimateDate(reportData.getBeginDate());
            ClimateTime[] sunrise = climateDailyReportData.getSunrise();
            ClimateTime[] sunset = climateDailyReportData.getSunset();

            if (currentSettings.getReportType() == PeriodType.MORN_NWWS) {
                julDay1 = aDate.julday() + 1;
                aDate.convertJulday(julDay1);
            }

            for (int i = 0; i < 2; i++) {
                int riseAdj = 0;
                ClimateTime rise = sunrise[i].to12HourTime();
                ClimateTime set = sunset[i].to12HourTime();

                StringBuilder nwwsAstroLine2 = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS);

                nwwsAstroLine2.replace(0, 17, separator(17).toString());
                String dateString = DateFormatSymbols.getInstance()
                        .getMonths()[aDate.getMon() - 1] + SPACE
                        + String.format(INT_TWO, aDate.getDay()) + SPACE
                        + aDate.getYear();
                nwwsAstroLine2.replace(0, dateString.length(), dateString);

                if (currentSettings.getControl().getSunControl().isSunrise()) {

                    riseAdj = (rise.isMissing() || !rise.isValid())
                            && globalConfig.isNoColon() ? 1 : riseAdj;

                    if (!rise.isMissing() && rise.isValid()) {
                        nwwsAstroLine2.replace(17, 22, separator(6).toString());
                        nwwsAstroLine2.replace(22, 22 + SUNRISE.length(),
                                WordUtils.capitalize(SUNRISE));

                        String hourString = rise
                                .getHour() == TimeUtil.HOURS_PER_DAY
                                && rise.getMin() == TimeUtil.HOURS_PER_DAY
                                        ? "--"
                                        : String.format(INT_TWO,
                                                rise.getHour()),
                                minString = rise
                                        .getHour() == TimeUtil.HOURS_PER_DAY
                                        && rise.getMin() == TimeUtil.HOURS_PER_DAY
                                                ? "--"
                                                : String.format(TWO_DIGIT_INT,
                                                        rise.getMin());

                        String dateRiseString = hourString + COLON + minString
                                + SPACE + rise.getAmpm() + SPACE
                                + rise.getZone();

                        nwwsAstroLine2.replace(31, 31 + dateRiseString.length(),
                                dateRiseString);
                    } else {
                        nwwsAstroLine2.replace(37 - reformat, 39 - reformat,
                                ParameterFormatClimate.MM);
                    }
                }

                if (currentSettings.getControl().getSunControl().isSunset()) {
                    nwwsAstroLine2.replace(46 - riseAdj,
                            46 - riseAdj + SUNSET.length(),
                            WordUtils.capitalize(SUNSET));

                    if (set.isValid() && !set.isMissing()) {

                        String hourString = set
                                .getHour() == TimeUtil.HOURS_PER_DAY
                                && rise.getMin() == TimeUtil.HOURS_PER_DAY
                                        ? "--"
                                        : String.format(INT_TWO, set.getHour()),
                                minString = set
                                        .getHour() == TimeUtil.HOURS_PER_DAY
                                        && set.getMin() == TimeUtil.HOURS_PER_DAY
                                                ? "--"
                                                : String.format(TWO_DIGIT_INT,
                                                        set.getMin());

                        String dateSetString = hourString + COLON + minString
                                + SPACE + set.getAmpm() + SPACE + set.getZone();

                        nwwsAstroLine2.replace(54 - riseAdj,
                                54 - riseAdj + dateSetString.length(),
                                dateSetString);
                    } else {
                        nwwsAstroLine2.replace(60 - reformat - riseAdj,
                                62 - reformat - riseAdj,
                                ParameterFormatClimate.MM);
                    }

                    // The instance where sunrise is OFF and sunset is ON
                    if (!currentSettings.getControl().getSunControl()
                            .isSunrise()) {
                        nwwsAstroLine2.replace(17, 22, separator(6).toString());
                    }
                }
                julDay1 = aDate.julday() + 1;
                aDate.convertJulday(julDay1);

                nwwsAstro.append(nwwsAstroLine2.toString()).append("\n");
            }
            nwwsAstro.append("\n");
        }

        return nwwsAstro.toString();
    }

    /**
     * <pre>
     *   March 1998     Jason P. Tuell        PRC/TDL
    *   July  1998     Barry N. Baxter       PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *
    *   Purpose:  This routine builds the text for the column headings
    *             for the weather portion of the NWWS climate summary.
     * </pre>
     * 
     * @param tabs
     * @return
     */
    private String buildNWWSColumns(ColumnSpaces tabs) {
        StringBuilder nwwsColumnsLine1 = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS),
                nwwsColumnsLine2 = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS),
                nwwsColumnsLine3 = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS);

        StringBuilder separator = new StringBuilder();

        /**
         * Create daily column headers for the formatted nwws table
         * 
         * <pre>
         * Example:
         * 
         * WEATHER ITEM   OBSERVED TIME   RECORD YEAR NORMAL DEPARTURE LAST
         *                 VALUE   (LST)  VALUE       VALUE  FROM      YEAR
         *                                                   NORMAL
         * ..............................................................
         * </pre>
         */
        if (tabs.isTime() || tabs.isRecord() || tabs.isYear() || tabs.isNorm()
                || tabs.isDepart() || tabs.isLast()
                || tabs.getPosActual() > 0) {

            int positionStart = 0, positionEnd = WEATHER_ITEM.length();
            nwwsColumnsLine1.replace(positionStart, positionEnd,
                    WordUtils.capitalize(WEATHER_ITEM));

            // add a space of length 3 between "WEATHER ITEM" and "OBSERVED"
            positionStart = positionEnd + 3;
            positionEnd = positionStart + OBSERVED.length();
            nwwsColumnsLine1.replace(positionStart, positionEnd,
                    WordUtils.capitalize(OBSERVED));

            separator = separator(tabs.getPosLast());

        }

        if (tabs.isTime()) {
            int position = tabs.getPosActual();
            nwwsColumnsLine1.replace(position - 1, position + 3,
                    WordUtils.capitalize(TIME));
        }

        if (tabs.isRecord()) {
            int position = tabs.getPosTime() - 2;
            nwwsColumnsLine1.replace(position - 1, position + 5,
                    WordUtils.capitalize(RECORD));
        }

        if (tabs.isYear()) {
            int position = tabs.getPosRecord() - 1;
            nwwsColumnsLine1.replace(position - 1, position + 3,
                    WordUtils.capitalize(YEAR));
        }

        if (tabs.isNorm()) {
            int position = tabs.getPosYear();
            nwwsColumnsLine1.replace(position - 1, position + 5,
                    WordUtils.capitalize(NORMAL));
        }

        if (tabs.isDepart()) {
            int position = tabs.getPosNorm() + 1;
            nwwsColumnsLine1.replace(position - 1, position + 8,
                    WordUtils.capitalize(DEPARTURE));
        }

        if (tabs.isLast()) {
            int position;
            if (tabs.isNorm()) {
                if (tabs.isDepart()) {
                    position = tabs.getPosDepart();
                } else {
                    position = tabs.getPosNorm() + 2;
                }
            } else if (tabs.isDepart()) {
                position = tabs.getPosDepart() + 1;
            } else {
                position = tabs.getPosYear() + 4;
            }
            nwwsColumnsLine1.replace(position - 1, position + 3,
                    WordUtils.capitalize(LAST));
        }

        if (tabs.isTime() || tabs.isRecord() || tabs.isYear() || tabs.isNorm()
                || tabs.isDepart() || tabs.isLast()
                || tabs.getPosActual() > 0) {

            int positionStart = WEATHER_ITEM.length() + 4;
            int positionEnd = positionStart + VALUE.length();
            nwwsColumnsLine2.replace(positionStart, positionEnd,
                    WordUtils.capitalize(VALUE));
        }

        if (tabs.isTime()) {
            int position = tabs.getPosActual();
            nwwsColumnsLine2.replace(position - 1, position + 4, ABBRV_LAST);
        }

        if (tabs.isRecord()) {
            int position = tabs.getPosTime() - 2;
            nwwsColumnsLine2.replace(position - 1, position + 4,
                    WordUtils.capitalize(VALUE));
        }

        if (tabs.isNorm()) {
            int position = tabs.getPosYear();
            nwwsColumnsLine2.replace(position - 1, position + 5,
                    WordUtils.capitalize(VALUE));
            if (tabs.isDepart()) {
                position = tabs.getPosNorm() + 1;
                nwwsColumnsLine2.replace(position - 1, position + 3,
                        WordUtils.capitalize(FROM));
            }
        } else if (tabs.isDepart()) {
            int position = tabs.getPosYear() + 1;
            nwwsColumnsLine2.replace(position - 1, position + 3,
                    WordUtils.capitalize(FROM));
        }

        if (tabs.isLast()) {
            int position;

            if (tabs.isNorm()) {
                if (tabs.isDepart()) {
                    position = tabs.getPosDepart();
                } else {
                    position = tabs.getPosNorm() + 2;
                }
            } else if (tabs.isDepart()) {
                position = tabs.getPosDepart() + 1;
            } else {
                position = tabs.getPosYear() + 4;
            }
            nwwsColumnsLine2.replace(position - 1, position + 3,
                    WordUtils.capitalize(YEAR));
        }

        StringBuilder nwwsColumns = new StringBuilder("\n")
                .append(nwwsColumnsLine1.toString()).append("\n")
                .append(nwwsColumnsLine2.toString());

        if (tabs.isNorm() && tabs.isDepart()) {
            int position = tabs.getPosNorm() + 1;
            nwwsColumnsLine3.replace(position - 1, position + 8,
                    WordUtils.capitalize(NORMAL));
            nwwsColumns.append("\n").append(nwwsColumnsLine3.toString());
        } else if (tabs.isDepart()) {
            int position = tabs.getPosYear() + 1;
            nwwsColumnsLine3.replace(position - 1, position + 8,
                    WordUtils.capitalize(NORMAL));
            nwwsColumns.append("\n").append(nwwsColumnsLine3.toString());
        }

        /**
         * Add a line of underscores to set off the column headings. Adjust the
         * length of the line such that is the same length as the column
         * headings.
         */

        nwwsColumns.append("\n").append(separator).append("\n");

        return nwwsColumns.toString();
    }

    /**
     * Migrated from build_nwws_temp.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   July  1998     Dan J. Zipper         PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL  
    *
    *   Purpose:  This routine will call the temperature segment of the 
    *             NWWS climate summary table.
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param morning
     * @return
     */
    private String buildNWWSTemp(ColumnSpaces tabs, ClimateRunData report,
            ClimateReportData data, boolean morning) {
        StringBuilder nwwsTemp = new StringBuilder();

        StringBuilder nwwsTempLine1 = emptyLine(
                ParameterFormatClimate.NUM_LINE_NWWS);
        StringBuilder nwwsTempLine2 = emptyLine(
                ParameterFormatClimate.NUM_LINE_NWWS);

        if (currentSettings.getControl().getTempControl().getMaxTemp()
                .isMeasured()
                || currentSettings.getControl().getTempControl().getMinTemp()
                        .isMeasured()
                || currentSettings.getControl().getTempControl().getMeanTemp()
                        .isMeasured()) {

            int position = 0;

            // "TEMPERATURE (F)"
            nwwsTempLine1.replace(position, (position++) + TEMPERATURE.length(),
                    WordUtils.capitalize(TEMPERATURE) + SPACE
                            + ABBRV_FAHRENHEIT);
            if (morning) {

                // "YESTERDAY"
                nwwsTempLine2.replace(position, position + YESTERDAY.length(),
                        WordUtils.capitalize(YESTERDAY));
            } else {
                // "TODAY"
                nwwsTempLine2.replace(position, position + TODAY.length(),
                        WordUtils.capitalize(TODAY));
            }

            nwwsTemp.append(nwwsTempLine1.toString()).append("\n");
            nwwsTemp.append(nwwsTempLine2.toString()).append("\n");

            // Create the maximum temp row
            // Example: "MAXIMUM 85 255 PM 99 1946 83 2 87"
            nwwsTemp.append(
                    buildNWWSAnyTemp(true, tabs, (ClimateRunDailyData) report,
                            (ClimateDailyReportData) data));

            // Create the minimum temp row
            // Example: "MINIMUM 58 1151 PM 35 1930 61 -3 75"
            nwwsTemp.append(
                    buildNWWSAnyTemp(false, tabs, (ClimateRunDailyData) report,
                            (ClimateDailyReportData) data));
            // Average temp row
            // Example: "AVERAGE 72 72 0 81"
            nwwsTemp.append(
                    buildNWWSAvgTemp(tabs, (ClimateDailyReportData) data));

        }

        return nwwsTemp.toString();
    }

    /**
     * Migrated from build_NWWS_precip.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose:  This routine is the main driver for building the
    *             precipitation sentences for the NOAA Weather Radio (NWR).
    *             There are three basic types of precipitation sentences:
    *             liquid precip (i.e., rainfall) sentences, snowfall
    *             sentences, and depth of snow on the ground sentences.
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param morning
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSPrecip(ColumnSpaces tabs, ClimateRunData report,
            ClimateReportData data, boolean morning)
            throws ClimateQueryException, ClimateInvalidParameterException {
        StringBuilder nwwsPrecip = new StringBuilder();

        /**
         * Create liquid precipitation rows
         * 
         * <pre>
         * Example:
         * 
         * PRECIPITATION (IN)
         *   YESTERDAY        T             1.19 1988   0.04  -0.04     0.00
         *   MONTH TO DATE    2.22                      1.25   0.97     1.78
         *   SINCE MAR 1      2.22                      1.25   0.97     1.78
         *   SINCE JAN 1      3.53                      3.72  -0.19     3.69
         * </pre>
         */
        nwwsPrecip.append(buildNWWSLiquidPrecip(tabs, report,
                (ClimateDailyReportData) data, morning));

        boolean snowReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getSnowDates(),
                report.getBeginDate());

        /**
         * Create snow precipitation rows
         * 
         * <pre>
         * Example:
         * 
         * SNOWFALL (IN)
         *   YESTERDAY        0.0
         *   MONTH TO DATE    0.0
         *   SINCE MAR 1      0.0
         *   SINCE JUL 1      0.0
         * </pre>
         */
        if (snowReport) {
            nwwsPrecip.append(buildNWWSSnowPrecip(tabs, report,
                    (ClimateDailyReportData) data, morning));
        }

        if (!currentSettings.getControl().getSnowControl().getSnowTotal()
                .isMeasured() || !snowReport) {
            nwwsPrecip.append("\n");
        }
        return nwwsPrecip.toString();
    }

    /**
     * Migrated from build_NWWS_heat_and_cool.f
     * 
     * <pre>
    *   March 1998     Barry N. Baxter       PRC/TDL
    *
    *   Purpose:  
    *        This subroutine is use only to call other subroutines when the flags
    *        to the other subroutines are set to TRUE.
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param morning
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSHeatAndCool(ColumnSpaces tabs,
            ClimateRunData report, ClimateDailyReportData data, boolean morning)
            throws ClimateQueryException, ClimateInvalidParameterException {
        StringBuilder nwwsHeatAndCool = new StringBuilder();
        ClimateProductFlags heatFlags = currentSettings.getControl()
                .getDegreeDaysControl().getTotalHDD();
        ClimateProductFlags coolFlags = currentSettings.getControl()
                .getDegreeDaysControl().getTotalCDD();

        boolean heatReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getHeatDates(),
                report.getBeginDate());
        boolean coolReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getCoolDates(),
                report.getBeginDate());

        if ((heatFlags.isMeasured() && heatReport)
                || (coolFlags.isMeasured() && coolReport)) {
            StringBuilder nwwsHeatAndCoolLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);
            nwwsHeatAndCoolLine1.replace(0, DEGREE_DAYS.length(),
                    WordUtils.capitalize(DEGREE_DAYS));

            nwwsHeatAndCool.append(nwwsHeatAndCoolLine1.toString())
                    .append("\n");

            /**
             * Create Heating rows.
             * 
             * <pre>
             * Example:
             * 
             * DEGREE DAYS
             * HEATING
             *   YESTERDAY        0                         1     -1        0
             *   MONTH TO DATE    4                        48    -44       11
             *   SINCE MAR 1      4                        48    -44       11
             *   SINCE JUL 1    234                       616   -382      387
             * </pre>
             */
            if (heatReport) {
                if (heatFlags.isMeasured()) {
                    StringBuilder nwwsHeatAndCoolLine2 = emptyLine(
                            ParameterFormatClimate.NUM_LINE1_NWWS);

                    nwwsHeatAndCoolLine2.replace(1, 1 + HEATING.length(),
                            WordUtils.capitalize(HEATING));

                    nwwsHeatAndCool.append(nwwsHeatAndCoolLine2.toString())
                            .append("\n");

                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.DAY, true, morning));

                }
                if (heatFlags.isTotalMonth()) {
                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.MONTH, true, morning));
                }
                if (heatFlags.isTotalSeason()) {
                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.SEASON, true, morning));
                }
                if (heatFlags.isTotalYear()) {
                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.YEAR, true, morning));
                }

                if (coolFlags.isMeasured() && coolReport) {
                    nwwsHeatAndCool.append("\n");

                }
            }

            /**
             * Create cooling rows
             * 
             * <pre>
             * Example:
             * 
             * COOLING
             *  YESTERDAY        7                         8     -1       16
             *   MONTH TO DATE  281                       163    118      262
             *   SINCE MAR 1    281                       163    118      262
             *   SINCE JAN 1    648                       288    360      402
             * </pre>
             */
            if (coolReport) {
                if (coolFlags.isMeasured()) {
                    StringBuilder nwwsHeatAndCoolLine3 = emptyLine(
                            ParameterFormatClimate.NUM_LINE1_NWWS);

                    nwwsHeatAndCoolLine3.replace(1, 1 + COOLING.length(),
                            COOLING);

                    nwwsHeatAndCool.append(nwwsHeatAndCoolLine3.toString())
                            .append("\n");

                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.DAY, false, morning));

                }

                if (coolFlags.isTotalMonth()) {
                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.MONTH, false, morning));
                }

                if (coolFlags.isTotalSeason()) {
                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.SEASON, false, morning));
                }

                if (coolFlags.isTotalYear()) {
                    nwwsHeatAndCool.append(buildNWWSDegreeDay(tabs, report,
                            data, PrecipPeriod.YEAR, false, morning));
                }
            }

        }

        return nwwsHeatAndCool.toString();
    }

    /**
     * Build a wind speed/direction line base on whether it's result wind, max
     * wind, max gust, etc.
     * 
     * @param field
     * @param value
     * @return
     * @throws ClimateInvalidParameterException
     */
    private String speedDir(String field, ClimateWind value)
            throws ClimateInvalidParameterException {

        StringBuilder speedDirLine = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS);

        String windSpeedStr = WordUtils.capitalize(field + SPACE + SPEED);
        speedDirLine.replace(2, 2 + windSpeedStr.length(), windSpeedStr);

        if (value.getSpeed() != ParameterFormatClimate.MISSING_SPEED) {
            int mph = ClimateUtilities.nint(value.getSpeed());
            int numDigits = String.valueOf(mph).length();

            int adjust;
            if (numDigits == 2) {
                adjust = 1;
            } else if (numDigits == 1) {
                adjust = 2;
            } else {
                adjust = 0;
            }

            speedDirLine.replace(23 + adjust, 26, String.valueOf(mph));

        } else {
            speedDirLine.replace(24, 27, ParameterFormatClimate.MM);
        }

        String windDirStr = WordUtils.capitalize(field + SPACE + DIRECTION);
        speedDirLine.replace(29, 29 + windDirStr.length(), windDirStr);

        if (value.getDir() != ParameterFormatClimate.MISSING) {
            String dirString = ClimateFormat.whichDirection(value.getDir(),
                    true);

            dirString = SPACE + dirString + " (" + value.getDir() + ")";

            speedDirLine.replace(54, 54 + dirString.length(), dirString);
        } else {
            speedDirLine.replace(55, 57, ParameterFormatClimate.MM);
        }

        return speedDirLine.toString();
    }

    /**
     * Migrated from build_NWWS_any_norms.f
     * 
     * <pre>
     *   May    1999     Dan J. Zipper         PRC/TDL
    *
    *   Purpose:  This routine controls building the normal table at the
    *             bottom of the NWWS output.
     *
     * </pre>
     * 
     * @param reportData
     * @param climateDailyReportData
     * @param max
     * @return
     */
    private String buildNWWSAnyNorms(ClimateRunDailyData reportData,
            ClimateDailyReportData climateDailyReportData, boolean max) {
        StringBuilder nwwsAnyNorms = new StringBuilder();
        ColumnSpaces tabs = new ColumnSpaces();
        tabs.setPosNorm(29);
        tabs.setPosRecord(39);
        tabs.setPosYear(49);

        TempRecordControlFlags normFlags = currentSettings.getControl()
                .getTempRecordControl();
        boolean normalRecord, recordYear;
        int observedValue, recordValue;
        int[] yearOfRecord;

        StringBuilder nwwsAnyNormsLine = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS);

        StringBuilder tempString = new StringBuilder();

        if (max) {
            tempString.append(WordUtils.capitalize(MAXIMUM));
            normalRecord = normFlags.isMaxTempRecord();
            recordYear = normFlags.isMaxTempYear();
            observedValue = climateDailyReportData.gettClimate()
                    .getMaxTempMean();
            recordValue = climateDailyReportData.gettClimate()
                    .getMaxTempRecord();
            yearOfRecord = climateDailyReportData.gettClimate()
                    .getMaxTempYear();

        } else {
            tempString.append(WordUtils.capitalize(MINIMUM));
            normalRecord = normFlags.isMinTempRecord();
            recordYear = normFlags.isMinTempYear();
            observedValue = climateDailyReportData.gettClimate()
                    .getMinTempMean();
            recordValue = climateDailyReportData.gettClimate()
                    .getMinTempRecord();
            yearOfRecord = climateDailyReportData.gettClimate()
                    .getMinTempYear();
        }

        tempString.append(SPACE).append(WordUtils.capitalize(TEMPERATURE))
                .append(SPACE).append(ABBRV_FAHRENHEIT);

        nwwsAnyNormsLine.replace(1, tempString.length(), tempString.toString());

        if (observedValue != ParameterFormatClimate.MISSING) {
            int jPosition = tabs.getPosNorm();
            int iPosition = jPosition - String.valueOf(observedValue).length()
                    + 1;

            nwwsAnyNormsLine.replace(iPosition - 1, jPosition,
                    String.valueOf(observedValue));
        } else {
            nwwsAnyNormsLine.replace(27, 29, ParameterFormatClimate.MM);
        }

        if (normalRecord) {
            if (recordValue != ParameterFormatClimate.MISSING) {
                int jPosition = tabs.getPosRecord();
                int iPosition = jPosition - String.valueOf(recordValue).length()
                        + 1;
                nwwsAnyNormsLine.replace(iPosition - 1, jPosition,
                        String.valueOf(recordValue));
            } else {
                nwwsAnyNormsLine.replace(37, 39, ParameterFormatClimate.MM);
            }

            StringBuilder extraYears = new StringBuilder();
            if (recordYear) {
                if (yearOfRecord[0] != ParameterFormatClimate.MISSING) {
                    int jPosition = tabs.getPosYear();
                    int iPosition = jPosition
                            - String.valueOf(yearOfRecord[0]).length() + 1;

                    nwwsAnyNormsLine.replace(iPosition - 1, jPosition,
                            String.valueOf(yearOfRecord[0]));
                } else {
                    nwwsAnyNormsLine.replace(47, 49, ParameterFormatClimate.MM);
                }
                nwwsAnyNormsLine.append("\n");

                for (int i = 1; i < yearOfRecord.length; i++) {
                    if (yearOfRecord[i] != ParameterFormatClimate.MISSING) {
                        StringBuilder yearLine = emptyLine(
                                ParameterFormatClimate.NUM_LINE1_NWWS);
                        int jPosition = tabs.getPosYear();
                        int iPosition = jPosition
                                - String.valueOf(yearOfRecord[i]).length() + 1;
                        yearLine.replace(iPosition - 1, jPosition,
                                String.valueOf(yearOfRecord[i]));

                        extraYears.append(yearLine.toString()).append("\n");
                    }
                }
            } else {
                nwwsAnyNormsLine.append("\n");
            }

            nwwsAnyNorms.append(
                    nwwsAnyNormsLine.toString() + extraYears.toString());
        } else {
            nwwsAnyNorms.append("\n");
        }

        return nwwsAnyNorms.toString();
    }

    /**
     * Migrated from build_NWWS_any_temp.f
     * 
     * <pre>
    *   April  1999     Barry N. Baxter       PRC/TDL
    *   June   1999     Dan Zipper            PRC/TDL
    *
    *   Purpose:  This routine controls building the  temperature
    *             line in the NOAA Weather Wire Service daily climate 
    *             summary.  The user has control over which pieces of the
    *             temperature climatology are reported.  If a 
    *             particular piece of information is neglected in all the
    *             variables, then that column is omitted in the report.
     * 
     * </pre>
     * 
     * @param isMax
     * @param tabs
     * @param data
     * @return
     */
    private String buildNWWSAnyTemp(boolean isMax, ColumnSpaces tabs,
            ClimateRunDailyData report, ClimateDailyReportData data) {
        StringBuilder nwwsAnyTemp = new StringBuilder();

        ClimateProductFlags tempFlag;
        int observedValue, normalValue, recordValue, lastYearValue;
        int[] yearOfRecord;
        ClimateTime timeObserved;
        String recordElement;

        if (isMax) {
            tempFlag = currentSettings.getControl().getTempControl()
                    .getMaxTemp();
            observedValue = data.getData().getMaxTemp();
            normalValue = data.getyClimate().getMaxTempMean();
            recordValue = data.getyClimate().getMaxTempRecord();
            lastYearValue = data.getLastYearData().getMaxTemp();
            yearOfRecord = data.getyClimate().getMaxTempYear();
            timeObserved = data.getData().getMaxTempTime().to12HourTime();
            recordElement = RecordClimateRawData.TEMP_MAX_RAW_TEXT;

        } else {
            tempFlag = currentSettings.getControl().getTempControl()
                    .getMinTemp();
            observedValue = data.getData().getMinTemp();
            normalValue = data.getyClimate().getMinTempMean();
            recordValue = data.getyClimate().getMinTempRecord();
            lastYearValue = data.getLastYearData().getMinTemp();
            yearOfRecord = data.getyClimate().getMinTempYear();
            timeObserved = data.getData().getMinTempTime().to12HourTime();
            recordElement = RecordClimateRawData.TEMP_MIN_RAW_TEXT;

        }

        int adjust = 0;
        if (tempFlag.isTimeOfMeasured()) {
            if (globalConfig.isNoColon()) {
                if (observedValue != ParameterFormatClimate.MISSING) {
                    if (!timeObserved.isMissing()) {
                        adjust = 1;
                    }

                }
            }

        }

        // Add the time the max/min temperature was observed if appropriate.
        if (tempFlag.isMeasured()) {

            StringBuilder nwwsAnyTempLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            if (isMax) {
                nwwsAnyTempLine.replace(2, 2 + MAXIMUM.length(), MAXIMUM);
            } else {
                nwwsAnyTempLine.replace(2, 2 + MINIMUM.length(), MINIMUM);
            }

            if (observedValue != ParameterFormatClimate.MISSING) {
                int jPosition = tabs.getPosActual() - 5;
                int iPosition = (jPosition
                        - String.valueOf(observedValue).length()) + 1;
                nwwsAnyTempLine.replace(iPosition - 1, jPosition,
                        String.valueOf(observedValue));
            } else {
                int jPosition = tabs.getPosActual() - 6;
                int iPosition = jPosition + 1;
                nwwsAnyTempLine.replace(jPosition - 1, iPosition,
                        ParameterFormatClimate.MM);
            }

            if (tempFlag.isTimeOfMeasured()) {
                if (observedValue != ParameterFormatClimate.MISSING
                        && timeObserved
                                .getHour() != ParameterFormatClimate.MISSING_HOUR
                        && timeObserved
                                .getMin() != ParameterFormatClimate.MISSING_MINUTE
                        && timeObserved.getAmpm() != null) {

                    String timeString = String.format(INT_TWO,
                            timeObserved.getHour())
                            + COLON
                            + String.format(TWO_DIGIT_INT,
                                    timeObserved.getMin())
                            + SPACE + timeObserved.getAmpm();

                    int timePosition = tabs.getPosTime() - 11;
                    nwwsAnyTempLine.replace(timePosition + adjust - 1,
                            timePosition + adjust - 1 + timeString.length(),
                            timeString);

                } else {
                    int jPosition = tabs.getPosTime() - 4;
                    int iPosition = jPosition - 1;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            iPosition + 1 + adjust, ParameterFormatClimate.MM);
                }
            }

            if (tempFlag.isRecord()) {
                if (observedValue != ParameterFormatClimate.MISSING
                        && recordValue != ParameterFormatClimate.MISSING) {

                    // Calculate the difference between the observed maximum
                    // temperature and the record value for this date
                    int tempDelta = observedValue - recordValue;

                    int latestRecordYear = yearOfRecord[0];
                    boolean sameYearAndValue = false;

                    // Prevent RER generation if the record is the same as the
                    // current year and there are no other years.
                    if (tempDelta == 0 && yearOfRecord[0] == report
                            .getBeginDate().getYear()) {
                        if (yearOfRecord[1] != ParameterFormatClimate.MISSING) {
                            // If there's an older record, use that instead of
                            // the current year.
                            latestRecordYear = yearOfRecord[1];
                        } else {
                            sameYearAndValue = true;
                        }
                    }

                    // Check to see if this is record; if it is, then put
                    // the value int he record field and use the current year
                    // in the year field
                    if ((tempDelta >= 0 && isMax)
                            || (tempDelta <= 0 && !isMax)) {

                        int jPosition = tabs.getPosRecord() - 6;
                        int iPosition = jPosition
                                - String.valueOf(recordValue).length() + 1;
                        nwwsAnyTempLine.replace(iPosition + adjust - 1,
                                jPosition + adjust,
                                String.valueOf(recordValue));

                        jPosition = tabs.getPosActual() - 5;
                        iPosition = jPosition
                                - String.valueOf(recordValue).length() + 1;
                        nwwsAnyTempLine.replace(jPosition, jPosition + 1,
                                RECORD_SYMBOL);

                        if (!sameYearAndValue) {
                            // Store the new record data for RERs whenever a new
                            // record is found.
                            logger.debug("Found new record for the station "
                                    + data.getStation().getStationName()
                                    + " (ID: " + data.getStation().getInformId()
                                    + ").");

                            writeBrokenRecs(report.getBeginDate(),
                                    data.getStation().getInformId(),
                                    observedValue, recordValue, recordElement,
                                    latestRecordYear);
                        }

                    } else {
                        // Not a record. Load the historical records and years
                        // as appropriate.
                        int jPosition = tabs.getPosRecord() - 6;
                        int iPosition = jPosition
                                - String.valueOf(recordValue).length() + 1;
                        nwwsAnyTempLine.replace(iPosition + adjust - 1,
                                jPosition + adjust,
                                String.valueOf(recordValue));
                    }
                } else if (observedValue == ParameterFormatClimate.MISSING
                        && recordValue != ParameterFormatClimate.MISSING) {
                    int jPosition = tabs.getPosRecord() - 6;
                    int iPosition = jPosition
                            - String.valueOf(recordValue).length() + 1;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            jPosition + adjust, String.valueOf(recordValue));
                } else if (recordValue == ParameterFormatClimate.MISSING) {
                    int jPosition = tabs.getPosRecord() - 2;
                    int iPosition = jPosition - 5;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            jPosition + adjust, ParameterFormatClimate.MM);
                }

                if (tempFlag.isRecordYear()) {
                    if (yearOfRecord[0] != ParameterFormatClimate.MISSING) {
                        int jPosition = tabs.getPosYear() - 2;
                        int iPosition = jPosition
                                - String.valueOf(yearOfRecord[0]).length() + 1;
                        nwwsAnyTempLine.replace(iPosition + adjust - 1,
                                jPosition + adjust,
                                String.valueOf(yearOfRecord[0]));
                    } else {
                        int jPosition = tabs.getPosYear() - 2;
                        int iPosition = jPosition - 1;
                        nwwsAnyTempLine.replace(iPosition + adjust - 1,
                                jPosition + adjust, ParameterFormatClimate.MM);
                    }
                }

            }

            if (tempFlag.isNorm()) {
                if (normalValue != ParameterFormatClimate.MISSING) {
                    int jPosition = tabs.getPosNorm() - 4;
                    int iPosition = jPosition
                            - String.valueOf(normalValue).length() + 1;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            jPosition + adjust, String.valueOf(normalValue));
                } else {
                    int jPosition = tabs.getPosNorm() - 5;
                    int iPosition = jPosition + 1;
                    nwwsAnyTempLine.replace(jPosition + adjust - 1,
                            iPosition + adjust, ParameterFormatClimate.MM);
                }
            }

            if (tempFlag.isDeparture()) {
                if (observedValue != ParameterFormatClimate.MISSING
                        && normalValue != ParameterFormatClimate.MISSING) {
                    int tempDelta = observedValue - normalValue;
                    int jPosition = tabs.getPosDepart() - 8;
                    int iPosition = jPosition
                            - String.valueOf(tempDelta).length() + 1;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            jPosition + adjust, String.valueOf(tempDelta));
                } else {
                    int jPosition = tabs.getPosDepart() - 9;
                    int iPosition = jPosition + 1;
                    nwwsAnyTempLine.replace(jPosition + adjust - 1,
                            iPosition + adjust, ParameterFormatClimate.MM);
                }
            }

            if (tempFlag.isLastYear()) {
                if (lastYearValue != ParameterFormatClimate.MISSING) {
                    int jPosition = tabs.getPosLast() - 5;
                    int iPosition = jPosition
                            - String.valueOf(lastYearValue).length() + 1;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            jPosition + adjust, String.valueOf(lastYearValue));
                } else {
                    int jPosition = tabs.getPosLast() - 5;
                    int iPosition = jPosition - 1;
                    nwwsAnyTempLine.replace(iPosition + adjust - 1,
                            jPosition + adjust, ParameterFormatClimate.MM);
                }
            }

            nwwsAnyTemp.append(nwwsAnyTempLine.toString()).append("\n");
            nwwsAnyTemp.append(buildNWWSYear(tempFlag, yearOfRecord,
                    recordValue, observedValue, tabs));
        }
        return nwwsAnyTemp.toString();
    }

    /**
     * Migrate build_NWWS_avg_temp.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *
    *   Purpose:  This routine controls building the minimum temperature
    *             line in the NOAA Weather Wire Service daily climate 
    *             summary.  The user has control over which pieces of the
    *             minimum temperature climatology are reported.  If a 
    *             particular piece of information is neglected in all the
    *             variables, then that column is omitted in the report.
     * </pre>
     * 
     * 
     * @param tabs
     * @param data
     * @return
     */
    private String buildNWWSAvgTemp(ColumnSpaces tabs,
            ClimateDailyReportData data) {
        StringBuilder nwwsAvgTemp = new StringBuilder();

        ClimateProductFlags tempFlag = currentSettings.getControl()
                .getTempControl().getMeanTemp();

        DailyClimateData yesterday = data.getData(),
                lastYearValue = data.getLastYearData();
        ClimateRecordDay yClimate = data.getyClimate();

        if (tempFlag.isMeasured()) {
            StringBuilder nwwsAvgLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE_NWWS);

            float averageTempMean = ParameterFormatClimate.MISSING;
            int roundedAvgMean = ParameterFormatClimate.MISSING;

            float averageTemp = ParameterFormatClimate.MISSING;
            int roundedAvg = ParameterFormatClimate.MISSING;

            nwwsAvgLine.replace(2, 2 + AVERAGE.length(), AVERAGE);

            if (yesterday.getMaxTemp() != ParameterFormatClimate.MISSING
                    && yesterday
                            .getMinTemp() != ParameterFormatClimate.MISSING) {
                averageTemp = ((float) yesterday.getMaxTemp()
                        + (float) yesterday.getMinTemp()) / 2;
                roundedAvg = ClimateUtilities.nint(averageTemp);

                int jPosition = tabs.getPosActual() - 3;
                int iPosition = jPosition - String.valueOf(roundedAvg).length()
                        - 1;
                nwwsAvgLine.replace(iPosition - 1, jPosition,
                        String.valueOf(roundedAvg));
            } else {
                int jPosition = tabs.getPosActual() - 5;
                int iPosition = jPosition - 1;
                nwwsAvgLine.replace(iPosition - 1, jPosition,
                        ParameterFormatClimate.MM);
            }

            if (tempFlag.isNorm()) {
                if (yClimate.getMeanTemp() != ParameterFormatClimate.MISSING) {
                    roundedAvgMean = ClimateUtilities
                            .nint(yClimate.getMeanTemp());
                    int jPosition = tabs.getPosNorm() - 4;
                    int iPosition = jPosition
                            - String.valueOf(roundedAvgMean).length() + 1;
                    nwwsAvgLine.replace(iPosition - 1, jPosition,
                            String.valueOf(roundedAvgMean));
                } else if (yClimate
                        .getMaxTempMean() != ParameterFormatClimate.MISSING
                        && yClimate
                                .getMinTempMean() != ParameterFormatClimate.MISSING) {
                    averageTempMean = (yClimate.getMaxTempMean()
                            + yClimate.getMinTempMean()) / 2f;
                    roundedAvgMean = ClimateUtilities.nint(averageTempMean);

                    int jPosition = tabs.getPosNorm() - 4;
                    int iPosition = jPosition
                            - String.valueOf(roundedAvgMean).length() + 1;
                    nwwsAvgLine.replace(iPosition - 1, jPosition,
                            String.valueOf(roundedAvgMean));
                } else {
                    int jPosition = tabs.getPosNorm() - 4;
                    int iPosition = jPosition - 1;
                    nwwsAvgLine.replace(iPosition - 1, jPosition,
                            ParameterFormatClimate.MM);
                }
            }

            else {
                if (yClimate.getMeanTemp() != ParameterFormatClimate.MISSING) {
                    roundedAvgMean = ClimateUtilities
                            .nint(yClimate.getMeanTemp());

                } else if (yClimate
                        .getMaxTempMean() != ParameterFormatClimate.MISSING
                        && yClimate
                                .getMinTempMean() != ParameterFormatClimate.MISSING) {
                    averageTempMean = (yClimate.getMaxTempMean()
                            + yClimate.getMinTempMean()) / 2f;
                    roundedAvgMean = ClimateUtilities.nint(averageTempMean);
                }
            }

            if (tempFlag.isDeparture()) {
                if (yesterday.getMaxTemp() != ParameterFormatClimate.MISSING
                        && yesterday
                                .getMinTemp() != ParameterFormatClimate.MISSING
                        && roundedAvgMean != ParameterFormatClimate.MISSING) {
                    int tempDelta = roundedAvg - roundedAvgMean;
                    int deltaTempRounded = ClimateUtilities.nint(tempDelta);

                    int jPosition = tabs.getPosDepart() - 8;
                    int iPosition = jPosition
                            - String.valueOf(deltaTempRounded).length() + 1;
                    nwwsAvgLine.replace(iPosition - 1, jPosition,
                            String.valueOf(deltaTempRounded));

                } else {
                    int jPosition = tabs.getPosDepart() - 9;
                    int iPosition = jPosition + 1;
                    nwwsAvgLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);
                }
            }

            if (tempFlag.isLastYear()) {
                if (lastYearValue.getMaxTemp() != ParameterFormatClimate.MISSING
                        && lastYearValue
                                .getMinTemp() != ParameterFormatClimate.MISSING) {
                    float avgTempLast = (lastYearValue.getMaxTemp()
                            + lastYearValue.getMinTemp()) / 2f;
                    int roundedAvgLast = ClimateUtilities.nint(avgTempLast);

                    int jPosition = tabs.getPosLast() - 5;
                    int iPosition = jPosition
                            - String.valueOf(roundedAvgLast).length() + 1;
                    nwwsAvgLine.replace(iPosition - 1, jPosition,
                            String.valueOf(roundedAvgLast));
                } else {
                    int jPosition = tabs.getPosLast() - 5;
                    int iPosition = jPosition - 1;
                    nwwsAvgLine.replace(iPosition - 1, jPosition,
                            ParameterFormatClimate.MM);
                }
            }

            nwwsAvgTemp.append(nwwsAvgLine.toString()).append("\n");
        }

        return nwwsAvgTemp.toString();
    }

    /**
     * Migrated from build_nwws_liquid_precip.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Dan J. Zipper         PRC/TDL
    *
    *   Purpose:  This routine controls production of the precipitation 
    *             elements for the NOAA Weather Wire Service reports.  The
    *             data from these elements is reported in the same tabular
    *             format as was the termperature and degree days.
     * 
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param morning
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSLiquidPrecip(ColumnSpaces tabs,
            ClimateRunData report, ClimateDailyReportData data, boolean morning)
            throws ClimateQueryException, ClimateInvalidParameterException {
        StringBuilder nwwsLiquidPrecip = new StringBuilder();

        boolean snow = false;
        ClimateProductFlags precipFlags = currentSettings.getControl()
                .getPrecipControl().getPrecipTotal();

        DailyClimateData yesterday = data.getData();

        if (precipFlags.isMeasured()) {

            StringBuilder nwwsLiquidLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);
            String precipString = PRECIPITATION + SPACE + ABBRV_IN;
            nwwsLiquidLine.replace(0, precipString.length(),
                    StringUtils.capitalize(precipString));

            nwwsLiquidPrecip.append("\n").append(nwwsLiquidLine).append("\n");
            nwwsLiquidPrecip.append(buildNWWSAnyPrecip(tabs, report,
                    (ClimateDailyReportData) data, PrecipPeriod.DAY, snow,
                    morning));

            if (precipFlags.isTotalMonth()) {
                if (yesterday.getPrecip() == ParameterFormatClimate.TRACE
                        && yesterday.getPrecipMonth() == 0) {
                    yesterday.setPrecipMonth(ParameterFormatClimate.TRACE);
                }

                nwwsLiquidPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                        PrecipPeriod.MONTH, snow, morning));

            }

            if (precipFlags.isTotalSeason()) {
                if (yesterday.getPrecip() == ParameterFormatClimate.TRACE
                        && yesterday.getPrecipSeason() == 0) {
                    yesterday.setPrecipSeason(ParameterFormatClimate.TRACE);
                }

                nwwsLiquidPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                        PrecipPeriod.SEASON, snow, morning));
            }
            if (precipFlags.isTotalYear()) {
                if (yesterday.getPrecip() == ParameterFormatClimate.TRACE
                        && yesterday.getPrecipYear() == 0) {
                    yesterday.setPrecipYear(ParameterFormatClimate.TRACE);
                }

                nwwsLiquidPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                        PrecipPeriod.YEAR, snow, morning));
            }

        }

        return nwwsLiquidPrecip.toString();
    }

    /**
     * Migrate from build_NWWS_snow_precip.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Dan J. Zipper         PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *
    *   Purpose:  This routine controls production of the snowitation 
    *             elements for the NOAA Weather Wire Service reports.  The
    *             data from these elements is reported in the same tabular
    *             format as was the termperature and degree days.
     * 
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param morning
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSSnowPrecip(ColumnSpaces tabs, ClimateRunData report,
            ClimateDailyReportData data, boolean morning)
            throws ClimateQueryException, ClimateInvalidParameterException {
        StringBuilder nwwsSnowPrecip = new StringBuilder();

        ClimateProductFlags snowFlags = currentSettings.getControl()
                .getSnowControl().getSnowTotal();
        DailyClimateData yesterday = data.getData();

        boolean snow = true;

        if (snowFlags.isMeasured()) {
            StringBuilder nwwsSnowLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            nwwsSnowLine1.replace(0, SNOWFALL.length(),
                    WordUtils.capitalize(SNOWFALL) + SPACE + ABBRV_IN);

            nwwsSnowPrecip.append("\n").append(nwwsSnowLine1.toString())
                    .append("\n");
            nwwsSnowPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                    PrecipPeriod.DAY, snow, morning));

            if (snowFlags.isTotalMonth()) {
                if (yesterday.getSnowDay() == ParameterFormatClimate.TRACE
                        && yesterday.getSnowMonth() == 0) {
                    yesterday.setSnowMonth(ParameterFormatClimate.TRACE);
                }

                nwwsSnowPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                        PrecipPeriod.MONTH, snow, morning));
            }

            if (snowFlags.isTotalSeason()) {
                if (yesterday.getSnowDay() == ParameterFormatClimate.TRACE
                        && yesterday.getSnowSeason() == 0) {
                    yesterday.setSnowSeason(ParameterFormatClimate.TRACE);
                }

                nwwsSnowPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                        PrecipPeriod.SEASON, snow, morning));
            }

            if (snowFlags.isTotalYear()) {
                if (yesterday.getSnowDay() == ParameterFormatClimate.TRACE
                        && yesterday.getSnowYear() == 0) {
                    yesterday.setSnowYear(ParameterFormatClimate.TRACE);
                }
                nwwsSnowPrecip.append(buildNWWSAnyPrecip(tabs, report, data,
                        PrecipPeriod.YEAR, snow, morning));

            }

            if (currentSettings.getControl().getSnowControl().getSnowDepthAvg()
                    .isMeasured()) {
                nwwsSnowPrecip.append(buildNWWSSnowDepth(tabs, report, data));
            } else {
                nwwsSnowPrecip.append("\n");
            }
        }

        return nwwsSnowPrecip.toString();

    }

    /**
     * Migrated from build_NWWS_degree_day.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Barry N. Baxter       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *   June   1999    Dan Zipper            PRC/TDL (Revised)
    *
    *   Purpose:  This routine controls production of the heating degree days for
    *             the NOAA Weather Wire Service reports.  The data from these
    *             elements is reported in the same tabular format as was the
    *             termperature and precipitation.
     * 
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param precipTime
     * @param heat
     * @param morning
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSDegreeDay(ColumnSpaces tabs, ClimateRunData report,
            ClimateDailyReportData data, PrecipPeriod precipTime, boolean heat,
            boolean morning)
            throws ClimateQueryException, ClimateInvalidParameterException {
        StringBuilder nwwsDegreeDay = new StringBuilder();

        ClimateProductFlags degreeFlags = heat
                ? currentSettings.getControl().getDegreeDaysControl()
                        .getTotalHDD()
                : currentSettings.getControl().getDegreeDaysControl()
                        .getTotalCDD();

        ClimateDates coolSeason = ClimateDates.getMissingClimateDates(),
                coolYear = ClimateDates.getMissingClimateDates(),
                heatSeason = ClimateDates.getMissingClimateDates(),
                heatYear = ClimateDates.getMissingClimateDates(),
                precipSeason = ClimateDates.getMissingClimateDates(),
                precipYear = ClimateDates.getMissingClimateDates(),
                snowSeason = ClimateDates.getMissingClimateDates(),
                snowYear = ClimateDates.getMissingClimateDates();

        int observedValue, normalValue, lastYearValue;

        if (degreeFlags.isMeasured()) {
            StringBuilder nwwsDegreeDayLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            if (precipTime == PrecipPeriod.DAY) {
                if (heat) {
                    observedValue = data.getData().getNumHeat();
                    normalValue = data.getyClimate().getNumHeatMean();
                    lastYearValue = data.getLastYearData().getNumHeat();
                } else {
                    observedValue = data.getData().getNumCool();
                    normalValue = data.getyClimate().getNumCoolMean();
                    lastYearValue = data.getLastYearData().getNumCool();
                }

                if (morning) {
                    nwwsDegreeDayLine.replace(2, 2 + YESTERDAY.length(),
                            WordUtils.capitalize(YESTERDAY));
                } else {
                    nwwsDegreeDayLine.replace(2, 2 + TODAY.length(),
                            WordUtils.capitalize(TODAY));
                }
            }

            else if (precipTime == PrecipPeriod.MONTH) {
                if (heat) {
                    observedValue = data.getData().getNumHeatMonth();
                    normalValue = data.getyClimate().getNumHeatMonth();
                    lastYearValue = data.getLastYearData().getNumHeatMonth();
                } else {
                    observedValue = data.getData().getNumCoolMonth();
                    normalValue = data.getyClimate().getNumCoolMonth();
                    lastYearValue = data.getLastYearData().getNumCoolMonth();
                }

                nwwsDegreeDayLine.replace(2, 2 + MONTH_TO_DATE.length(),
                        MONTH_TO_DATE);
            }

            else if (precipTime == PrecipPeriod.SEASON) {
                ClimateDAOUtils.setSeason(report.getBeginDate(), coolSeason,
                        coolYear, heatSeason, heatYear, precipSeason,
                        precipYear, snowSeason, snowYear);

                if (heat) {
                    observedValue = data.getData().getNumHeatSeason();
                    normalValue = data.getyClimate().getNumHeatSeason();
                    lastYearValue = data.getLastYearData().getNumHeatSeason();

                    nwwsDegreeDayLine = buildNWWSDate(heatSeason.getStart(),
                            nwwsDegreeDayLine);
                } else {
                    observedValue = data.getData().getNumCoolSeason();
                    normalValue = data.getyClimate().getNumCoolSeason();
                    lastYearValue = data.getLastYearData().getNumCoolSeason();

                    nwwsDegreeDayLine = buildNWWSDate(coolSeason.getStart(),
                            nwwsDegreeDayLine);
                }
            } else {
                ClimateDAOUtils.setSeason(report.getBeginDate(), coolSeason,
                        coolYear, heatSeason, heatYear, precipSeason,
                        precipYear, snowSeason, snowYear);
                if (heat) {
                    observedValue = data.getData().getNumHeatYear();
                    normalValue = data.getyClimate().getNumHeatYear();
                    lastYearValue = data.getLastYearData().getNumHeatYear();

                    nwwsDegreeDayLine = buildNWWSDate(heatYear.getStart(),
                            nwwsDegreeDayLine);
                } else {
                    observedValue = data.getData().getNumCoolYear();
                    normalValue = data.getyClimate().getNumCoolYear();
                    lastYearValue = data.getLastYearData().getNumCoolYear();

                    nwwsDegreeDayLine = buildNWWSDate(coolYear.getStart(),
                            nwwsDegreeDayLine);
                }
            }

            if (observedValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                int jPosition = tabs.getPosActual() - 5;
                int iPosition = jPosition
                        - String.valueOf(observedValue).length() + 1;
                nwwsDegreeDayLine.replace(iPosition - 1, jPosition,
                        String.valueOf(observedValue));
            } else {
                int jPosition = tabs.getPosActual() - 6;
                int iPosition = jPosition + 1;
                nwwsDegreeDayLine.replace(jPosition - 1, iPosition,
                        ParameterFormatClimate.MM);
            }

            if (degreeFlags.isNorm()) {
                if (normalValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    int jPosition = tabs.getPosNorm() - 4;
                    int iPosition = jPosition
                            - String.valueOf(normalValue).length() + 1;
                    nwwsDegreeDayLine.replace(iPosition - 1, jPosition,
                            String.valueOf(normalValue));
                } else {
                    int jPosition = tabs.getPosNorm() - 5;
                    int iPosition = jPosition + 1;
                    nwwsDegreeDayLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);
                }
            }

            if (degreeFlags.isDeparture()) {
                if (normalValue != ParameterFormatClimate.MISSING_DEGREE_DAY
                        && observedValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    int meanDegreeDelta = observedValue - normalValue;

                    int jPosition = tabs.getPosDepart() - 8;
                    int iPosition = jPosition
                            - String.valueOf(meanDegreeDelta).length() + 1;
                    nwwsDegreeDayLine.replace(iPosition - 1, jPosition,
                            String.valueOf(meanDegreeDelta));
                } else {
                    int jPosition = tabs.getPosDepart() - 9;
                    int iPosition = jPosition + 1;
                    nwwsDegreeDayLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);
                }
            }

            if (degreeFlags.isLastYear()) {
                if (lastYearValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    int jPosition = tabs.getPosLast() - 5;
                    int iPosition = jPosition
                            - String.valueOf(lastYearValue).length() + 1;

                    nwwsDegreeDayLine.replace(iPosition - 1, jPosition,
                            String.valueOf(lastYearValue));
                } else {
                    int jPosition = tabs.getPosLast() - 6;
                    int iPosition = jPosition + 1;

                    nwwsDegreeDayLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);
                }
            }
            nwwsDegreeDay.append(nwwsDegreeDayLine.toString()).append("\n");
        }

        return nwwsDegreeDay.toString();
    }

    /**
     * Migrated from build_NWWS_year.f
     * 
     * <pre>
    *   July 1999     Dan J. Zipper        PRC/TDL
    *   Jan  2000     Dan J. Zipper        PRC/TDL
    *
    *   Purpose:  This routine will add the extra years in the case where there
    *             was a record tied or set in more than one year.
    *
    *             *  Indicates record was set or tied.
    *             MM Indicates data is missing.
    *             T  Indicates Trace of precipitation.
     * </pre>
     * 
     * @param tempFlag
     * @param yearOfRecord
     * @param recordValue
     * @param observedValue
     * @param tabs
     * @return
     */
    private String buildNWWSYear(ClimateProductFlags tempFlag,
            int[] yearOfRecord, float recordValue, float observedValue,
            ColumnSpaces tabs) {
        StringBuilder nwwsYear = new StringBuilder();

        if (tempFlag.isRecord() && tempFlag.isRecordYear()) {

            for (int i = 1; i < yearOfRecord.length; i++) {
                // "0" entries may also be present for some reason.
                if (yearOfRecord[i] != ParameterFormatClimate.MISSING
                        && yearOfRecord[i] != 0) {
                    StringBuilder nwwsYearLine = emptyLine(
                            ParameterFormatClimate.NUM_LINE_NWWS);
                    int jPosition = tabs.getPosYear() - 2;
                    int iPosition = jPosition
                            - String.valueOf(yearOfRecord[i]).length() + 1;

                    nwwsYearLine.replace(iPosition - 1, jPosition,
                            String.valueOf(yearOfRecord[i]));
                    nwwsYear.append(nwwsYearLine.toString()).append("\n");

                }
            }
        }

        return nwwsYear.toString();
    }

    /**
     * Migrated from build_NWWS_any_precip.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Dan J. Zipper         PRC/TDL
    *   July  1998     Barry N. Baxter       PRC/TDL
    *   Jun   1999     Dan Zipper            PRC/TDL
    *   Nov   2002     Gary Battel           SAIC/MDL
    *   Sep   2003     Mohammed Sikder       RSIS/MDL
    *   Oct   2003     Gary Battel           SAIC/MDL
    *
    *   Purpose:  This routine controls production of the precipitation 
    *             elements for the NOAA Weather Wire Service reports.  The
    *             data from these elements is reported in the same tabular
    *             format as was the termperature and degree days.
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @param precipTime
     * @param snow
     * @param morning
     * @return
     * @throws ClimateQueryException
     * @throws ClimateInvalidParameterException
     */
    private String buildNWWSAnyPrecip(ColumnSpaces tabs, ClimateRunData report,
            ClimateDailyReportData data, PrecipPeriod precipTime, boolean snow,
            boolean morning)
            throws ClimateQueryException, ClimateInvalidParameterException {

        StringBuilder nwwsAnyPrecip = new StringBuilder();

        ClimateProductFlags precipFlags = snow
                ? currentSettings.getControl().getSnowControl().getSnowTotal()
                : currentSettings.getControl().getPrecipControl()
                        .getPrecipTotal();
        String recordElement = snow ? RecordClimateRawData.SNOW_MAX_RAW_TEXT
                : RecordClimateRawData.PRECIP_MAX_RAW_TEXT;

        int decimalPlaces = snow ? 1 : 2;
        int snowOffset = snow ? 1 : 0;

        if (precipFlags.isMeasured()) {
            float observedValue, normalValue, recordValue, lastYearValue;
            int[] yearOfRecord;

            ClimateDates coolSeason = ClimateDates.getMissingClimateDates(),
                    coolYear = ClimateDates.getMissingClimateDates(),
                    heatSeason = ClimateDates.getMissingClimateDates(),
                    heatYear = ClimateDates.getMissingClimateDates(),
                    precipSeason = ClimateDates.getMissingClimateDates(),
                    precipYear = ClimateDates.getMissingClimateDates(),
                    snowSeason = ClimateDates.getMissingClimateDates(),
                    snowYear = ClimateDates.getMissingClimateDates();

            StringBuilder nwwsLiquidLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS);

            ClimateDAOUtils.setSeason(report.getBeginDate(), coolSeason,
                    coolYear, heatSeason, heatYear, precipSeason, precipYear,
                    snowSeason, snowYear);

            if (precipTime == PrecipPeriod.DAY) {
                if (snow) {
                    observedValue = data.getData().getSnowDay();
                    normalValue = data.getyClimate().getSnowDayMean();
                    recordValue = data.getyClimate().getSnowDayRecord();
                    lastYearValue = data.getLastYearData().getSnowDay();
                    yearOfRecord = data.getyClimate().getSnowDayRecordYear();
                } else {
                    observedValue = data.getData().getPrecip();
                    normalValue = data.getyClimate().getPrecipMean();
                    recordValue = data.getyClimate().getPrecipDayRecord();
                    lastYearValue = data.getLastYearData().getPrecip();
                    yearOfRecord = data.getyClimate().getPrecipDayRecordYear();
                }
                if (morning) {
                    nwwsLiquidLine.replace(2, 2 + YESTERDAY.length(),
                            WordUtils.capitalize(YESTERDAY));
                } else {
                    nwwsLiquidLine.replace(2, 2 + TODAY.length(),
                            WordUtils.capitalize(TODAY));
                }
            } else if (precipTime == PrecipPeriod.MONTH) {
                if (snow) {
                    observedValue = data.getData().getSnowMonth();
                    normalValue = data.getyClimate().getSnowMonthMean();
                    recordValue = data.getyClimate().getSnowMonthRecord();
                    lastYearValue = data.getLastYearData().getSnowMonth();
                    yearOfRecord = data.getyClimate().getSnowMonthRecordYear();
                } else {
                    observedValue = data.getData().getPrecipMonth();
                    normalValue = data.getyClimate().getPrecipMonthMean();
                    recordValue = data.getyClimate().getPrecipMonthRecord();
                    lastYearValue = data.getLastYearData().getPrecipMonth();
                    yearOfRecord = data.getyClimate()
                            .getPrecipMonthRecordYear();
                }

                nwwsLiquidLine.replace(2, 2 + MONTH_TO_DATE.length(),
                        MONTH_TO_DATE);

            } else if (precipTime == PrecipPeriod.SEASON) {

                if (snow) {
                    observedValue = data.getData().getSnowSeason();
                    normalValue = data.getyClimate().getSnowSeasonMean();
                    recordValue = data.getyClimate().getSnowSeasonRecord();
                    lastYearValue = data.getLastYearData().getSnowSeason();
                    yearOfRecord = data.getyClimate().getSnowSeasonRecordYear();

                    nwwsLiquidLine = buildNWWSDate(snowSeason.getStart(),
                            nwwsLiquidLine);
                } else {
                    observedValue = data.getData().getPrecipSeason();
                    normalValue = data.getyClimate().getPrecipSeasonMean();
                    recordValue = data.getyClimate().getPrecipSeasonRecord();
                    lastYearValue = data.getLastYearData().getPrecipSeason();
                    yearOfRecord = data.getyClimate()
                            .getPrecipSeasonRecordYear();

                    nwwsLiquidLine = buildNWWSDate(precipSeason.getStart(),
                            nwwsLiquidLine);
                }
            } else {

                if (snow) {
                    observedValue = data.getData().getSnowYear();
                    normalValue = data.getyClimate().getSnowYearMean();
                    recordValue = data.getyClimate().getSnowYearRecord();
                    lastYearValue = data.getLastYearData().getSnowYear();
                    yearOfRecord = data.getyClimate().getSnowYearRecordYear();

                    nwwsLiquidLine = buildNWWSDate(snowYear.getStart(),
                            nwwsLiquidLine);
                } else {
                    observedValue = data.getData().getPrecipYear();
                    normalValue = data.getyClimate().getPrecipYearMean();
                    recordValue = data.getyClimate().getPrecipYearRecord();
                    lastYearValue = data.getLastYearData().getPrecipYear();
                    yearOfRecord = data.getyClimate().getPrecipYearRecordYear();

                    nwwsLiquidLine = buildNWWSDate(precipYear.getStart(),
                            nwwsLiquidLine);
                }
            }

            if (observedValue != ParameterFormatClimate.MISSING_PRECIP
                    && observedValue != ParameterFormatClimate.TRACE) {
                String valueString = String.format("%." + decimalPlaces + "f",
                        observedValue);

                int jPosition = tabs.getPosActual() - 2 - snowOffset;
                int iPosition = jPosition - String.valueOf(valueString).length()
                        + 1;
                nwwsLiquidLine.replace(iPosition - 1, jPosition,
                        String.valueOf(valueString));
            } else if (observedValue == ParameterFormatClimate.TRACE) {
                int jPosition = tabs.getPosActual() - 6;
                int iPosition = jPosition + 1;
                nwwsLiquidLine.replace(iPosition - 1, iPosition,
                        ParameterFormatClimate.TRACE_SYMBOL);
            } else {
                int jPosition = tabs.getPosActual() - 6;
                int iPosition = jPosition + 1;
                nwwsLiquidLine.replace(jPosition - 1, iPosition,
                        ParameterFormatClimate.MM);
            }

            if (precipFlags.isRecord() && precipTime == PrecipPeriod.DAY) {
                if (recordValue == ParameterFormatClimate.MISSING_PRECIP) {
                    int jPosition = tabs.getPosRecord() - 7;
                    int iPositon = jPosition + 1;
                    nwwsLiquidLine.replace(jPosition - 1, iPositon,
                            ParameterFormatClimate.MM);
                } else if (recordValue == ParameterFormatClimate.TRACE) {
                    int jPosition = tabs.getPosRecord() - 7;
                    int iPosition = jPosition + 1;
                    nwwsLiquidLine.replace(iPosition - 1, iPosition,
                            ParameterFormatClimate.TRACE_SYMBOL);
                } else {
                    String valueString = String
                            .format("%." + decimalPlaces + "f", recordValue);
                    int jPosition = tabs.getPosRecord() - 3 - snowOffset;
                    int iPosition = jPosition
                            - String.valueOf(valueString).length() + 1;
                    nwwsLiquidLine.replace(iPosition - 1, jPosition,
                            String.valueOf(valueString));
                }

                if (observedValue == ParameterFormatClimate.TRACE) {
                    if (recordValue == 0) {

                        int jPosition = tabs.getPosActual() - 2;
                        nwwsLiquidLine.replace(jPosition, jPosition + 1,
                                RECORD_SYMBOL);

                        // Store the new record data for RERs whenever a new
                        // record is found.
                        logger.debug("Found new record for the station "
                                + data.getStation().getStationName() + " (ID: "
                                + data.getStation().getInformId() + ").");

                        writeBrokenRecs(report.getBeginDate(),
                                data.getStation().getInformId(), observedValue,
                                recordValue, recordElement, yearOfRecord[0]);
                    }
                }

                if (observedValue >= recordValue && observedValue != 0) {
                    if (observedValue != ParameterFormatClimate.MISSING_PRECIP
                            && recordValue != ParameterFormatClimate.MISSING_PRECIP) {

                        int latestRecordyear = yearOfRecord[0];
                        boolean sameYearAndValue = false;

                        // Prevent RER generation if the record is the same as
                        // the current year and there are no other years.
                        if (observedValue == recordValue
                                && yearOfRecord[0] == report.getBeginDate()
                                        .getYear()) {
                            if (yearOfRecord[1] != ParameterFormatClimate.MISSING) {
                                // If there's an older record, use that instead
                                // of the current year.
                                latestRecordyear = yearOfRecord[1];
                            } else {
                                sameYearAndValue = true;
                            }
                        }

                        int jPosition = tabs.getPosActual() - 2;
                        nwwsLiquidLine.replace(jPosition, jPosition + 1,
                                RECORD_SYMBOL);
                        if (!sameYearAndValue) {
                            // Store the new record data for RERs whenever a new
                            // record is found.
                            logger.debug("Found new record for the station "
                                    + data.getStation().getStationName()
                                    + " (ID: " + data.getStation().getInformId()
                                    + ").");

                            writeBrokenRecs(report.getBeginDate(),
                                    data.getStation().getInformId(),
                                    observedValue, recordValue, recordElement,
                                    latestRecordyear);
                        }
                    }
                }

                if (precipFlags.isRecordYear()
                        && precipTime == PrecipPeriod.DAY) {
                    if (yearOfRecord[0] == ParameterFormatClimate.MISSING) {
                        int jPosition = tabs.getPosYear() - 2;
                        int iPosition = jPosition - 1;
                        nwwsLiquidLine.replace(iPosition - 1, jPosition,
                                ParameterFormatClimate.MM);
                    } else {
                        int jPosition = tabs.getPosYear() - 2;
                        int iPosition = jPosition
                                - String.valueOf(yearOfRecord[0]).length() + 1;

                        nwwsLiquidLine.replace(iPosition - 1, jPosition,
                                String.valueOf(yearOfRecord[0]));
                    }
                }
            }

            if (precipFlags.isNorm()) {
                if (normalValue == ParameterFormatClimate.TRACE) {
                    int jPosition = tabs.getPosNorm() - 5;
                    int iPosition = jPosition + 1;

                    nwwsLiquidLine.replace(iPosition - 1, iPosition,
                            ParameterFormatClimate.TRACE_SYMBOL);

                } else if (normalValue != ParameterFormatClimate.MISSING_PRECIP) {
                    String valueString = String
                            .format("%." + decimalPlaces + "f", normalValue);

                    int jPosition = tabs.getPosNorm() - 1 - snowOffset;
                    int iPosition = jPosition
                            - String.valueOf(valueString).length() + 1;

                    nwwsLiquidLine.replace(iPosition - 1, jPosition,
                            String.valueOf(valueString));
                } else {
                    int jPosition = tabs.getPosNorm() - 5;
                    int iPosition = jPosition + 1;
                    nwwsLiquidLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);
                }
            }

            if (precipFlags.isDeparture()) {
                float pMean = observedValue, cMean = normalValue;
                if (pMean != ParameterFormatClimate.MISSING
                        && cMean != ParameterFormatClimate.MISSING) {
                    cMean = (cMean == ParameterFormatClimate.TRACE) ? 0 : cMean;
                    pMean = (pMean == ParameterFormatClimate.TRACE) ? 0 : pMean;

                    float deltaDayPrecip = pMean - cMean;

                    String valueString = String
                            .format("%." + decimalPlaces + "f", deltaDayPrecip);

                    int jPosition = tabs.getPosDepart() - 5 - snowOffset;
                    int iPosition = jPosition
                            - String.valueOf(valueString).length() + 1;

                    nwwsLiquidLine.replace(iPosition - 1, jPosition,
                            String.valueOf(valueString));

                } else {
                    int jPosition = tabs.getPosDepart() - 9;
                    int iPosition = jPosition + 1;
                    nwwsLiquidLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);

                }
            }

            if (precipFlags.isLastYear()) {
                if (lastYearValue != ParameterFormatClimate.MISSING_PRECIP
                        && lastYearValue != ParameterFormatClimate.TRACE) {
                    String valueString = String
                            .format("%." + decimalPlaces + "f", lastYearValue);

                    int jPosition = tabs.getPosLast() - 2 - snowOffset;
                    int iPosition = jPosition
                            - String.valueOf(valueString).length() + 1;
                    nwwsLiquidLine.replace(iPosition - 1, jPosition,
                            String.valueOf(valueString));
                } else if (lastYearValue == ParameterFormatClimate.TRACE) {
                    int jPosition = tabs.getPosLast() - 5;
                    int iPosition = jPosition + 1;
                    nwwsLiquidLine.replace(iPosition - 1, iPosition,
                            ParameterFormatClimate.TRACE_SYMBOL);
                } else {
                    int jPosition = tabs.getPosLast() - 6;
                    int iPosition = jPosition + 1;
                    nwwsLiquidLine.replace(jPosition - 1, iPosition,
                            ParameterFormatClimate.MM);

                }
            }

            nwwsAnyPrecip.append(nwwsLiquidLine.toString()).append("\n");
            nwwsAnyPrecip.append(buildNWWSYear(precipFlags, yearOfRecord,
                    recordValue, observedValue, tabs));

        }

        return nwwsAnyPrecip.toString();
    }

    /**
     * Migrated from build_NWWS_snow_depth.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   June  1998     Dan J. Zipper         PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL
    *
    *   Purpose:  This routine controls production of the precipitation 
    *             elements for the NOAA Weather Wire Service reports.  The
    *             data from these elements is reported in the same tabular
    *             format as was the termperature and degree days.
     * </pre>
     * 
     * @param tabs
     * @param report
     * @param data
     * @return
     */
    private String buildNWWSSnowDepth(ColumnSpaces tabs, ClimateRunData report,
            ClimateDailyReportData data) {

        DailyClimateData yesterday = data.getData();
        StringBuilder nwwsSnowDepthLine = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS);

        nwwsSnowDepthLine.replace(2, 2 + SNOW_DEPTH.length(),
                WordUtils.capitalize(SNOW_DEPTH));
        if (yesterday.getSnowGround() != ParameterFormatClimate.MISSING
                && yesterday.getSnowGround() != ParameterFormatClimate.TRACE) {

            String snowString = String
                    .valueOf(ClimateUtilities.nint(yesterday.getSnowGround()));
            int jPosition = tabs.getPosActual() - 4;
            int iPosition = jPosition - snowString.length();
            nwwsSnowDepthLine.replace(iPosition - 1, jPosition, snowString);

        } else if (yesterday.getSnowGround() == ParameterFormatClimate.TRACE) {
            int jPositon = tabs.getPosActual() - 6;
            int iPositon = jPositon + 1;
            nwwsSnowDepthLine.replace(jPositon - 1, iPositon,
                    ParameterFormatClimate.TRACE_SYMBOL);
        } else {
            int jPositon = tabs.getPosActual() - 6;
            int iPositon = jPositon + 1;
            nwwsSnowDepthLine.replace(jPositon - 1, iPositon,
                    ParameterFormatClimate.MM);
        }

        return nwwsSnowDepthLine.toString() + "\n\n";
    }

    /**
     * Migrated from build_NWWS_date.f
     * 
     * <pre>
    *   January 1999     Jason P. Tuell       PRC/TDL
    *
    *   Purpose:  
    *        This subroutine inserts the words "Since dd Mon" at position 3
    *        in a line of text.
     *
     * </pre>
     * 
     * @param start
     * @param line
     * @return
     * @throws ClimateInvalidParameterException
     */
    private StringBuilder buildNWWSDate(ClimateDate start, StringBuilder line)
            throws ClimateInvalidParameterException {

        if (start.getMon() >= 1 && start.getMon() <= 12 && start.getDay() >= 1
                && start.getDay() <= 31) {
            String monthString = "Since "
                    + DateFormatSymbols.getInstance()
                            .getShortMonths()[start.getMon() - 1]
                    + SPACE + start.getDay();

            line.replace(2, 2 + monthString.length(), monthString);
        } else {
            throw new ClimateInvalidParameterException(
                    "Invalid start date: " + start.toFullDateString());
        }

        return line;
    }

    /**
     * Build a line for relative humidity based on whether it's "AVERAGE,"
     * "LOWEST," etc.
     * 
     * @param field
     * @param value
     * @param timeIsMeasured
     * @param hour
     * @return
     */
    private String relHumdLine(String field, int value, boolean timeIsMeasured,
            int hour) {

        StringBuilder relHumdLine = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS);

        relHumdLine.replace(1, 1 + field.length(), WordUtils.capitalize(field));

        if (value != ParameterFormatClimate.MISSING) {
            relHumdLine.replace(14 - String.valueOf(value).length(), 14,
                    String.valueOf(value));
        } else {
            relHumdLine.replace(12, 14, ParameterFormatClimate.MM);
        }

        if (!field.equalsIgnoreCase(AVERAGE) && timeIsMeasured
                && hour != ParameterFormatClimate.MISSING_HOUR) {
            ClimateTime time = new ClimateTime(hour).to12HourTime();
            String timeString = String.format(INT_TWO, time.getHour()) + COLON
                    + String.format(TWO_DIGIT_INT, time.getMin()) + SPACE
                    + time.getAmpm();
            relHumdLine.replace(24, 24 + timeString.length(), timeString);

        }

        return relHumdLine.toString();

    }

    /**
     * Migrated from the write_broken_recs function in RecEvntRoutines.C. Checks
     * for broken/tied records and maps the data used to create RERs.
     * 
     * @param date
     * @param id
     * @param observed
     * @param record
     * @param element
     * @param year
     */
    private void writeBrokenRecs(ClimateDate date, int id, Number observed,
            Number record, String element, int year) {

        logger.debug("Storing record for element: [" + element + "], year: ["
                + year + "], record: [" + record + "], observed: [" + observed
                + "], date: [" + date.toFullDateString() + "] and ID: [" + id
                + "]");

        dailyRecordData.add(new RecordClimateRawData(
                getRecordRawDateFormat()
                        .format(date.getCalendarFromClimateDate().getTime()),
                String.valueOf(id), element, String.valueOf(observed),
                String.valueOf(record), String.valueOf(year)));
    }

    /**
     * Date format constant used in recording new records: MMddyy
     */
    private static SimpleDateFormat getRecordRawDateFormat() {
        return new SimpleDateFormat("MMddyy");
    }

    public List<RecordClimateRawData> getDailyRecordData() {
        return dailyRecordData;
    }
}

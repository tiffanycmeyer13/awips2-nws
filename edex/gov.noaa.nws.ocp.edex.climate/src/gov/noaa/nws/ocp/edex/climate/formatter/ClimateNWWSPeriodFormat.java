/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateWind;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.WeatherStrings;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.DegreeDaysControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.PrecipitationControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.SkycoverControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.SnowControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.WeatherControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.WindControlFlags;

/**
 * Class containing logic for building NWWS period products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2017 21099      wpaintsil   Initial creation
 * May 10, 2017 30162      wpaintsil   Address FindBugs issues with date format constants.
 * 16 MAY 2017  33104      amoore      Floating point equality.
 * 20 NOV 2017  41088      amoore      Remove unnecessary double-checking of report windows
 *                                     for snow section.
 * 20 NOV 2017  41125      amoore      Mean RH section should not be dependent on sky section.
 * 13 DEC 2018  DR21053    wpaintsil   Some null checks needed to prevent exceptions.
 * 09 APR 2019  DR21217    wpaintsil   Wrong precision on temp departure from normals.
 * 30 MAY 2017  DR21432    wpaintsil   Correct last year values for threshold line. Correct 
 *                                     error in date formatting.
 * 02 JUL 2019  DR21423    wpaintsil   Snow depth avg. line fails to appear.
 * 25 JUL 2019  DR21490    wpaintsil   Wrong precision on snowfall values. 
 *                                     Should be 10ths not 100ths.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class ClimateNWWSPeriodFormat extends ClimateNWWSFormat {

    /**
     * Length of separator under nwws period table.
     */
    private int periodSeparatorLength = 0;

    /**
     * Holds values for spacing of formatted period summaries.
     */
    private ColumnSpaces periodTabs = new ColumnSpaces();

    /**
     * Used as a flag to determine the number of decimal places in a float
     * value.
     * 
     * @author wpaintsil
     *
     */
    private enum DecimalType {
        SNOW, TEMP, PRECIP;
    }

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateNWWSPeriodFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        super(currentSettings, globalConfig);

    }

    /**
     * Migrated from build_period_wire.c
     * 
     * <pre>
     * 
     *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS IS THE STARTING ROUTINE FOR THE MONTHLY,SEASONAL,YEARLY NWWS PRODUCT.
    *  THIS ROUTINE CONTROLS ALL CALLS TO THE VARIOUS METEOROLOGICAL ROUTINES
    *  THAT WILL BE USED TO CREATED THE WIRE PRODUCT.
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

        Map<Integer, ClimatePeriodReportData> reportMap = ((ClimateRunPeriodData) reportData)
                .getReportMap();
        StringBuilder productText = new StringBuilder();
        productText.append(buildNWWSHeader(reportData.getBeginDate()));

        for (Station station : currentSettings.getStations()) {
            Integer stationId = station.getInformId();

            ClimatePeriodReportData report = reportMap.get(stationId);
            if (report == null) {
                logger.warn("The station with informId " + stationId
                        + " defined in the ClimateProductType settings object was not found in the ClimateRunData report map.");
                continue;
            }

            productText.append(buildNWWSComment(stationId, reportData));

            productText.append(buildNWWSPeriodTable(
                    (ClimateRunPeriodData) reportData, report));

            productText.append(buildNWWSPeriodWind(report));

            productText.append(buildNWWSPeriodSky(report));

            productText.append(NWWS_FOOTNOTE);
        }

        productText.append("\n").append(PRODUCT_TERMINATOR).append("\n");

        prod.put(getName(),
                getProduct(applyGlobalConfig(productText.toString())));

        return prod;
    }

    /**
     * Migrated from build_period_table.c
     * 
     * <pre>
     *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CALL ALL THE ROUTINES THAT WILL BE INVOLVED IN CREATING
    *  THE NWWS TABULAR PORTION OF THE NWWS PRODUCT. THIS WILL INCLUDE CALLS TO
    *  THE TEMPERATURE, LIQUID PRECIP, SNOW PRECIP, AND DEGREE DAYS.
     *
     * </pre>
     * 
     * @param reportData
     * @param climatePeriodReportData
     * @return
     */
    private String buildNWWSPeriodTable(ClimateRunPeriodData reportData,
            ClimatePeriodReportData climatePeriodReportData) {

        StringBuilder nwwsPeriodTable = new StringBuilder();
        ColumnSpaces tabs = new ColumnSpaces();
        tabs.setDailyTabs(currentSettings, reportData.getBeginDate());

        // create table header
        nwwsPeriodTable.append(createPeriodTableHead());
        // create temperature columns
        nwwsPeriodTable.append(buildPeriodTemp(climatePeriodReportData));
        // create precipitation columns
        nwwsPeriodTable
                .append(buildPeriodLiquidPrecip(climatePeriodReportData));

        if (ClimateFormat.reportWindow(
                currentSettings.getControl().getSnowDates(),
                reportData.getBeginDate())) {
            // create snowfall columns
            nwwsPeriodTable.append(buildPeriodSnowPrecip(
                    climatePeriodReportData, reportData.getBeginDate()));
        }

        // create degree days columns
        nwwsPeriodTable.append(buildPeriodDegreeDays(climatePeriodReportData,
                reportData.getBeginDate()));

        // append separator
        nwwsPeriodTable.append(separator(periodSeparatorLength));

        return nwwsPeriodTable.toString() + "\n";
    }

    /**
     * Migrated from build_period_wind.c.
     * 
     * <pre>
    *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CREATE THE TEMPERATURE SEGMENT OF THE NWWS TABLE.
     *
     * </pre>
     * 
     * @param climatePeriodReportData
     * @return
     */
    private String buildNWWSPeriodWind(
            ClimatePeriodReportData climatePeriodReportData) {
        StringBuilder periodWind = new StringBuilder();

        WindControlFlags windFlag = currentSettings.getControl()
                .getWindControl();

        PeriodData actualData = climatePeriodReportData.getData();

        /**
         * Create wind section
         * 
         * <pre>
         * Example:
         * 
         * WIND (MPH)
         * AVERAGE WIND SPEED              11.3
         * HIGHEST WIND SPEED/DIRECTION    39/270    DATE  03/01
         * HIGHEST GUST SPEED/DIRECTION    54/280    DATE  03/01
         * </pre>
         */

        if (windFlag.getResultWind().isMeasured()
                || windFlag.getMaxWind().isMeasured()
                || windFlag.getMaxGust().isMeasured()
                || windFlag.getMeanWind().isMeasured()) {
            periodWind.append("\n").append(WordUtils.capitalize(WIND))
                    .append(SPACE).append(MPH).append("\n");
        }

        int speedPos = 31;
        int speedFloatPos = 32;

        // Average Wind
        if (windFlag.getMeanWind().isMeasured()) {
            StringBuilder windLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);
            String windString = WordUtils
                    .capitalize(AVERAGE + SPACE + WIND + SPACE + SPEED);
            windLine.replace(0, windString.length(), windString);

            switch ((int) actualData.getAvgWindSpd()) {
            case ParameterFormatClimate.MISSING:
                windLine.replace(speedFloatPos, speedFloatPos + 2,
                        ParameterFormatClimate.MM);
                break;
            default:
                String floatString = String.format("%3.1f",
                        actualData.getAvgWindSpd());
                windLine.replace(speedFloatPos,
                        speedFloatPos + floatString.length(), floatString);
                break;
            }

            periodWind.append(windLine.toString()).append("\n");
        }

        if (windFlag.getResultWind().isMeasured()) {
            String speedString = "";
            StringBuilder windLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);
            String windString = WordUtils
                    .capitalize(RESULTANT + SPACE + WIND + SPACE + SPEED) + "/"
                    + WordUtils.capitalize(DIRECTION);
            windLine.replace(0, windString.length(), windString);

            switch ((int) actualData.getResultWind().getSpeed()) {
            case ParameterFormatClimate.MISSING:
                windLine.replace(speedPos + 1, speedPos + 3,
                        ParameterFormatClimate.MM);
                break;
            default:
                speedString = String.format(INT_THREE + "/", ClimateUtilities
                        .nint(actualData.getResultWind().getSpeed()));
                windLine.replace(speedPos, speedPos + speedString.length(),
                        speedString);
                break;
            }
            int dirPos = speedPos + speedString.length();
            if (actualData.getResultWind()
                    .getSpeed() != ParameterFormatClimate.MISSING) {
                switch (actualData.getResultWind().getDir()) {
                case ParameterFormatClimate.MISSING:
                    windLine.replace(dirPos, dirPos + 2,
                            ParameterFormatClimate.MM);
                    break;

                default:
                    String dirString = String.format(THREE_DIGIT_INT,
                            actualData.getResultWind().getDir());
                    windLine.replace(dirPos, dirPos + dirString.length(),
                            dirString);
                    break;
                }
            }

            periodWind.append(windLine.toString()).append("\n");

        }

        // Highest Wind
        if (windFlag.getMaxWind().isMeasured()) {
            periodWind = windDateHelper(periodWind, windFlag.getMaxWind(),
                    actualData.getMaxWindList(), actualData.getMaxWindDayList(),
                    WIND);
        }

        // Highest Gust
        if (windFlag.getMaxGust().isMeasured()) {
            periodWind = windDateHelper(periodWind, windFlag.getMaxGust(),
                    actualData.getMaxGustList(), actualData.getMaxGustDayList(),
                    GUST);
        }

        return periodWind.toString();

    }

    /**
     * Migrated from build_period_sky.c
     * 
     * <pre>
     *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CREATE THE TEMPERATURE SEGMENT OF THE NWWS TABLE.
     *
     * </pre>
     * 
     * @param climatePeriodReportData
     * @return
     */
    private String buildNWWSPeriodSky(
            ClimatePeriodReportData climatePeriodReportData) {
        StringBuilder periodSky = new StringBuilder();

        SkycoverControlFlags skyFlag = currentSettings.getControl()
                .getSkycoverControl();
        PeriodData actualData = climatePeriodReportData.getData();

        int element = 29;
        int elementFloat = 28;
        int table1 = 24;
        int table2 = 57;

        /**
         * Create sky cover section
         * 
         * <pre>
         * Example:
         * 
         * SKY COVER
         * POSSIBLE SUNSHINE (PERCENT)   MM
         * AVERAGE SKY COVER           0.60
         * NUMBER OF DAYS FAIR            6
         * NUMBER OF DAYS PC             12
         * NUMBER OF DAYS CLOUDY         13
         *
         * AVERAGE RH (PERCENT)     55
         * </pre>
         */
        if (skyFlag.isPossSunshine() || skyFlag.isAvgSkycover()
                || skyFlag.isFairDays() || skyFlag.isPartlyCloudyDays()
                || skyFlag.isCloudyDays()) {
            periodSky.append("\n").append(SKY_COVER).append("\n");

            // possible sunshine line
            if (skyFlag.isPossSunshine()) {
                periodSky.append(skyHelper(
                        POSSIBLE_SUNSHINE + SPACE + "("
                                + WordUtils.capitalize(PERCENT) + ")",
                        actualData.getPossSun(), element, element + 1));
            }

            // sky cover line
            if (skyFlag.isAvgSkycover()) {
                StringBuilder sunLine = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS + 1);
                sunLine.replace(0, AVG_SKY_COVER.length(), AVG_SKY_COVER);

                switch ((int) actualData.getMeanSkyCover()) {
                case ParameterFormatClimate.MISSING:
                    sunLine.replace(elementFloat + 2, elementFloat + 4,
                            ParameterFormatClimate.MM);
                    break;
                default:
                    String coverString = String.format(FLOAT_TWO_DECIMALS1,
                            actualData.getMeanSkyCover());
                    sunLine.replace(elementFloat,
                            elementFloat + coverString.length(), coverString);
                    break;
                }
                periodSky.append(sunLine.toString()).append("\n");
            }

            // number of fair days line
            if (skyFlag.isFairDays()) {
                periodSky.append(skyHelper("Number Of Days Fair",
                        actualData.getNumFairDays(), element, element + 1));
            }

            // number of partly cloudy days
            if (skyFlag.isPartlyCloudyDays()) {
                periodSky.append(skyHelper("Number Of Days PC",
                        actualData.getNumPartlyCloudyDays(), element,
                        element + 1));
            }

            // number of cloudy days line
            if (skyFlag.isCloudyDays()) {
                periodSky.append(skyHelper("Number Of Days Cloudy",
                        actualData.getNumMostlyCloudyDays(), element,
                        element + 1));
            }

        }

        periodSky.append("\n");
        // average relative humidity line
        if (currentSettings.getControl().getRelHumidityControl().getAverageRH()
                .isMeasured()) {
            periodSky.append(skyHelper(
                    WordUtils.capitalize(AVERAGE) + " RH ("
                            + WordUtils.capitalize(PERCENT) + ")",
                    actualData.getMeanRh(), table1, table1 + 1));
        }

        periodSky.append("\n");

        /**
         * Create weather section
         * 
         * <pre>
         * Example:
         * 
         * WEATHER CONDITIONS. NUMBER OF DAYS WITH
         * THUNDERSTORM              0     MIXED PRECIP               0
         * HEAVY RAIN                2     RAIN                       5
         * LIGHT RAIN               15     FREEZING RAIN              0
         * LT FREEZING RAIN          2     HAIL                       0
         * HEAVY SNOW                0     SNOW                       4
         * LIGHT SNOW                5     SLEET                      2
         * FOG                      10     FOG W/VIS <= 1/4 MILE      1
         * HAZE                      3
         * </pre>
         */
        WeatherControlFlags weatherFlag = currentSettings.getControl()
                .getWeatherControl();
        // number of days
        boolean wxFlag[] = { weatherFlag.isThunderStorm(),
                weatherFlag.isMixedPrecip(), weatherFlag.isHeavyRain(),
                weatherFlag.isRain(), weatherFlag.isLightRain(),
                weatherFlag.isFreezingRain(), weatherFlag.isLightFreezingRain(),
                weatherFlag.isHail(), weatherFlag.isHeavySnow(),
                weatherFlag.isSnow(), weatherFlag.isLightSnow(),
                weatherFlag.isIcePellet(), weatherFlag.isFog(),
                weatherFlag.isHeavyFog(), weatherFlag.isHaze() };

        boolean hasWeather = false;
        for (int i = 0; i < wxFlag.length; i++) {
            if (wxFlag[i]) {
                hasWeather = true;
                break;
            }
        }

        if (hasWeather) {
            int[] wxValue = { actualData.getNumThunderStorms(),
                    actualData.getNumMixedPrecip(),
                    actualData.getNumHeavyRain(), actualData.getNumRain(),
                    actualData.getNumLightRain(),
                    actualData.getNumFreezingRain(),
                    actualData.getNumLightFreezingRain(),
                    actualData.getNumHail(), actualData.getNumHeavySnow(),
                    actualData.getNumSnow(), actualData.getNumLightSnow(),
                    actualData.getNumIcePellets(), actualData.getNumFog(),
                    actualData.getNumFogQuarterSM(), actualData.getNumHaze() };

            periodSky.append("Weather Conditions. Number Of Days With \n");

            StringBuilder weatherLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);

            int output = 0;
            /*
             * Logic taken from legacy: This loops through the number of weather
             * items. The output flag decides whether is goes in the 1st spot or
             * 2nd spot.
             */
            for (int i = 0; i < wxValue.length; i++) {
                if (wxFlag[i]) {
                    // Shorten the FOG_14 string
                    String weatherString = WeatherStrings.getString(i)
                            .equals(WeatherStrings.FOG_14.getString())
                                    ? "FOG W/VIS <= 1/4 MILE"
                                    : WeatherStrings.getString(i);

                    if (output == 0) {
                        weatherLine.replace(0, weatherString.length(),
                                weatherString);
                        switch (wxValue[i]) {
                        case ParameterFormatClimate.MISSING:
                            weatherLine.replace(table1 + 1, table1 + 3,
                                    ParameterFormatClimate.MM);
                            break;
                        case 0:
                            weatherLine.replace(table1 + 2, table1 + 4, "0");
                            break;
                        default:
                            weatherLine.replace(table1,
                                    table1 + String.format("%3d", wxValue[i])
                                            .length(),
                                    String.format("%3d", wxValue[i]));
                            break;
                        }

                        output = 1;
                    } else if (output == 1) {
                        weatherLine.replace(32, 32 + weatherString.length(),
                                weatherString);
                        switch (wxValue[i]) {
                        case ParameterFormatClimate.MISSING:
                            weatherLine.replace(table2 + 1, table2 + 3,
                                    ParameterFormatClimate.MM);
                            break;
                        case 0:
                            weatherLine.replace(table2 + 2, table2 + 4, "0");
                            break;
                        default:
                            weatherLine.replace(table2,
                                    table2 + String.format("%3d", wxValue[i])
                                            .length(),
                                    String.format("%3d", wxValue[i]));
                            break;
                        }
                        output = 2;
                    }
                }

                if (output == 2 || (i == wxValue.length - 1 && output == 1)) {
                    periodSky.append(weatherLine.toString()).append("\n");
                    weatherLine = emptyLine(
                            ParameterFormatClimate.NUM_LINE1_NWWS + 1);
                    output = 0;
                }
            }
        }

        return periodSky.toString();

    }

    /**
     * Migrated from create_table_head.c
     * 
     * <pre>
     *  November 1999       Dan Zipper       PRC\TDL
    *
    *  This routine creates the header of the table section for monthly, seasonal,
    * and annual climate reports
     *
     * </pre>
     * 
     * @return
     */
    private String createPeriodTableHead() {
        StringBuilder periodTableHead = new StringBuilder();

        periodTabs.setPeriodTabs();

        int separatorLength = 0;

        if (currentSettings.getControl().getTempControl().getMaxTemp()
                .isMeasured()
                || currentSettings.getControl().getTempControl().getMinTemp()
                        .isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMeanMaxTemp().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMeanMinTemp().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempGE90().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempLE32().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempGET1().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempGET2().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempLET3().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempLE32().isMeasured()
                || currentSettings.getControl().getTempControl().getMinTempLE0()
                        .isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMinTempGET4().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMinTempLET5().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMinTempLET6().isMeasured()
                || currentSettings.getControl().getTempControl().getMeanTemp()
                        .isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipTotal().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipAvg().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecip24HR().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipStormMax().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipGE01().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipGE10().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipGE50().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipGE100().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipGEP1().isMeasured()
                || currentSettings.getControl().getPrecipControl()
                        .getPrecipGEP2().isMeasured()
                || currentSettings.getControl().getSnowControl().getSnowTotal()
                        .isMeasured()
                || currentSettings.getControl().getSnowControl()
                        .getSnowWaterTotal().isMeasured()
                || currentSettings.getControl().getSnowControl().getSnowJuly1()
                        .isMeasured()
                || currentSettings.getControl().getSnowControl()
                        .getSnowWaterJuly1().isMeasured()
                || currentSettings.getControl().getSnowControl().getSnowGE100()
                        .isMeasured()
                || currentSettings.getControl().getSnowControl().getSnowAny()
                        .isMeasured()
                || currentSettings.getControl().getSnowControl().getSnowGEP1()
                        .isMeasured()
                || currentSettings.getControl().getSnowControl()
                        .getSnowDepthMax().isMeasured()
                || currentSettings.getControl().getSnowControl().getSnow24hr()
                        .isMeasured()
                || currentSettings.getControl().getSnowControl()
                        .getSnowStormMax().isMeasured()
                || currentSettings.getControl().getDegreeDaysControl()
                        .getTotalHDD().isMeasured()
                || currentSettings.getControl().getDegreeDaysControl()
                        .getTotalCDD().isMeasured()
                || currentSettings.getControl().getDegreeDaysControl()
                        .getEarlyFreeze().isMeasured()
                || currentSettings.getControl().getDegreeDaysControl()
                        .getLateFreeze().isMeasured()
                || currentSettings.getControl().getDegreeDaysControl()
                        .getSeasonHDD().isMeasured()
                || currentSettings.getControl().getDegreeDaysControl()
                        .getSeasonCDD().isMeasured()) {
            StringBuilder tableHeadLine1 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);
            StringBuilder tableHeadLine2 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);
            StringBuilder tableHeadLine3 = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);

            tableHeadLine1.replace(0, WEATHER.length(),
                    WordUtils.capitalize(WEATHER));
            tableHeadLine1.replace(periodTabs.getPosValue() + 2,
                    periodTabs.getPosValue() + 2 + OBSERVED.length(),
                    WordUtils.capitalize(OBSERVED));
            separatorLength = periodTabs.getPosValue() + 2 + OBSERVED.length();

            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isTimeOfMeasured()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isTimeOfMeasured()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTemp().isRecord()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isRecord()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecip24HR().isTimeOfMeasured()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipStormMax().isTimeOfMeasured()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthMax().isTimeOfMeasured()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowStormMax().isTimeOfMeasured()) {
                String valueDates = VALUE + "   " + DATES;
                separatorLength = periodTabs.getPosNorm();
                tableHeadLine2.replace(periodTabs.getPosValue() + 2,
                        periodTabs.getPosValue() + 2 + valueDates.length(),
                        valueDates);
            } else {
                tableHeadLine2.replace(periodTabs.getPosValue() + 2,
                        periodTabs.getPosValue() + 2 + VALUE.length(),
                        WordUtils.capitalize(VALUE));
            }

            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMeanMaxTemp().isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMeanMinTemp().isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTempGE90().isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTempLE32().isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMinTempLE32().isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMeanTemp().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipTotal().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipAvg().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecip24HR().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipStormMax().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE01().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE10().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE50().isNorm()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE100().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowTotal().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowWaterTotal().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowJuly1().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowWaterJuly1().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthAvg().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowGE100().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowAny().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthMax().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnow24hr().isNorm()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowStormMax().isNorm()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getSeasonHDD().isNorm()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getTotalHDD().isNorm()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getSeasonCDD().isNorm()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getEarlyFreeze().isNorm()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getLateFreeze().isNorm()) {
                tableHeadLine1.replace(periodTabs.getPosNorm() + 2,
                        periodTabs.getPosNorm() + 2 + NORMAL.length(),
                        WordUtils.capitalize(NORMAL));
                tableHeadLine2.replace(periodTabs.getPosNorm() + 2,
                        periodTabs.getPosNorm() + 2 + VALUE.length(),
                        WordUtils.capitalize(VALUE));
                separatorLength = periodTabs.getPosNorm() + 2 + NORMAL.length();

            } else {
                periodTabs.setPosDepart(periodTabs.getPosNorm());
            }

            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isDeparture()
                    || currentSettings.getControl().getTempControl()
                            .getMeanMaxTemp().isDeparture()
                    || currentSettings.getControl().getTempControl()
                            .getMeanMinTemp().isDeparture()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTempGE90().isDeparture()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTempLE32().isDeparture()
                    || currentSettings.getControl().getTempControl()
                            .getMinTempLE32().isDeparture()
                    || currentSettings.getControl().getTempControl()
                            .getMeanTemp().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipTotal().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipAvg().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecip24HR().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipStormMax().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE01().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE10().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE50().isDeparture()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE100().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowTotal().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowWaterTotal().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowJuly1().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowWaterJuly1().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthAvg().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowGE100().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowAny().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthMax().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnow24hr().isDeparture()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowStormMax().isDeparture()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getSeasonHDD().isDeparture()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getTotalHDD().isDeparture()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getSeasonCDD().isDeparture()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getEarlyFreeze().isDeparture()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getLateFreeze().isDeparture()) {
                tableHeadLine1.replace(periodTabs.getPosDepart() + 2,
                        periodTabs.getPosDepart() + 2 + DEPART.length(),
                        WordUtils.capitalize(DEPART));
                tableHeadLine2.replace(periodTabs.getPosDepart() + 2,
                        periodTabs.getPosDepart() + 2 + FROM.length(),
                        WordUtils.capitalize(FROM));
                tableHeadLine3.replace(periodTabs.getPosDepart() + 2,
                        periodTabs.getPosDepart() + 2 + NORMAL.length(),
                        WordUtils.capitalize(NORMAL));
                separatorLength = periodTabs.getPosDepart() + 2
                        + DEPART.length();
            } else {
                periodTabs.setPosLastYr(periodTabs.getPosLastYr() - 7);
                periodTabs.setPosLastDate(periodTabs.getPosLastDate() - 7);
            }

            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isNorm()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isLastYear()
                    || currentSettings.getControl().getTempControl()
                            .getMeanMaxTemp().isLastYear()
                    || currentSettings.getControl().getTempControl()
                            .getMeanMinTemp().isLastYear()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTempGE90().isLastYear()
                    || currentSettings.getControl().getTempControl()
                            .getMaxTempLE32().isLastYear()
                    || currentSettings.getControl().getTempControl()
                            .getMinTempLE32().isLastYear()
                    || currentSettings.getControl().getTempControl()
                            .getMeanTemp().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipTotal().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipAvg().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecip24HR().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipStormMax().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE01().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE10().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE50().isLastYear()
                    || currentSettings.getControl().getPrecipControl()
                            .getPrecipGE100().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowTotal().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowWaterTotal().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowJuly1().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowWaterJuly1().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthAvg().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowGE100().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowAny().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowDepthMax().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnow24hr().isLastYear()
                    || currentSettings.getControl().getSnowControl()
                            .getSnowStormMax().isLastYear()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getSeasonHDD().isLastYear()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getTotalHDD().isLastYear()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getSeasonCDD().isLastYear()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getEarlyFreeze().isLastYear()
                    || currentSettings.getControl().getDegreeDaysControl()
                            .getLateFreeze().isLastYear()) {
                String lastYearStr = WordUtils
                        .capitalize(LAST + SPACE + YEAR + "'s");

                tableHeadLine1.replace(periodTabs.getPosLastYr() + 2,
                        periodTabs.getPosLastYr() + 2 + lastYearStr.length(),
                        lastYearStr);
                tableHeadLine2.replace(periodTabs.getPosLastYr() + 2,
                        periodTabs.getPosLastYr() + 2 + VALUE.length(),
                        WordUtils.capitalize(VALUE));

                separatorLength = periodTabs.getPosLastYr() + 2
                        + lastYearStr.length();

                if (currentSettings.getControl().getTempControl().getMaxTemp()
                        .isDateOfLast()
                        || currentSettings.getControl().getTempControl()
                                .getMinTemp().isDateOfLast()
                        || currentSettings.getControl().getPrecipControl()
                                .getPrecip24HR().isDateOfLast()
                        || currentSettings.getControl().getPrecipControl()
                                .getPrecipStormMax().isDateOfLast()
                        || currentSettings.getControl().getSnowControl()
                                .getSnowDepthMax().isDateOfLast()
                        || currentSettings.getControl().getSnowControl()
                                .getSnowStormMax().isDateOfLast()
                        || currentSettings.getControl().getSnowControl()
                                .getSnow24hr().isDateOfLast()) {

                    tableHeadLine2.replace(periodTabs.getPosLastDate() - 1,
                            periodTabs.getPosLastDate() - 1 + DATES.length(),
                            WordUtils.capitalize(DATES));
                    separatorLength = periodTabs.getPosLastDate() - 1
                            + DATES.length();
                }

            }

            periodTableHead.append(tableHeadLine1.toString()).append("\n");
            periodTableHead.append(tableHeadLine2.toString()).append("\n");
            periodTableHead.append(tableHeadLine3.toString()).append("\n");

            periodTableHead.append(separator(separatorLength).toString());
            periodSeparatorLength = separatorLength;

            periodTableHead.append("\n");

        }

        return periodTableHead.toString();
    }

    /**
     * Migrated from build_period_temp.c
     * 
     * <pre>
     *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CREATE THE TEMPERATURE SEGMENT OF THE NWWS TABLE.
     *
     * </pre>
     * 
     * @param climatePeriodReportData
     * @return
     */
    private String buildPeriodTemp(
            ClimatePeriodReportData climatePeriodReportData) {
        StringBuilder periodTemp = new StringBuilder();

        /**
         * Create temperature rows
         * 
         * <pre>
         * Example:
         * 
         * TEMPERATURE (F)
         * RECORD
         *  HIGH              93   03/23/1907
         *  LOW                4   03/04/1873
         * HIGHEST            80   03/01        78       2
         * LOWEST             22   03/15        23      -1
         * AVG. MAXIMUM     56.5              55.9     0.6     63.1
         * AVG. MINIMUM     37.9              37.6     0.3     44.0
         * MEAN             47.2              46.8     0.4
         * DAYS MAX >= 90      0               0.0     0.0
         * DAYS MAX <= 32      0               0.4    -0.4
         * DAYS MIN <= 32     12               7.1     4.9
         * DAYS MIN <= 0       0               0.0     0.0
         * </pre>
         */
        if (currentSettings.getControl().getTempControl().getMaxTemp()
                .isMeasured()
                || currentSettings.getControl().getTempControl().getMinTemp()
                        .isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMeanMaxTemp().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMeanMinTemp().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempGE90().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempLE32().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempGET1().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempGET2().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempLET3().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMaxTempLE32().isMeasured()
                || currentSettings.getControl().getTempControl().getMinTempLE0()
                        .isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMinTempGET4().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMinTempLET5().isMeasured()
                || currentSettings.getControl().getTempControl()
                        .getMinTempLET6().isMeasured()
                || currentSettings.getControl().getTempControl().getMeanTemp()
                        .isMeasured()) {
            // "TEMPERATURE (F)"
            periodTemp.append(WordUtils.capitalize(TEMPERATURE)).append(SPACE)
                    .append(ABBRV_FAHRENHEIT).append("\n");

            // Record High/Low rows
            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isRecord()
                    || currentSettings.getControl().getTempControl()
                            .getMinTemp().isRecord()) {
                if (currentSettings.getControl().getTempControl().getMaxTemp()
                        .isMeasured()
                        || currentSettings.getControl().getTempControl()
                                .getMinTemp().isMeasured()) {
                    periodTemp.append(WordUtils.capitalize(RECORD))
                            .append("\n");
                }
                if (currentSettings.getControl().getTempControl().getMaxTemp()
                        .isRecord()
                        && currentSettings.getControl().getTempControl()
                                .getMaxTemp().isMeasured()) {

                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMaxTemp(),
                                    ParameterFormatClimate.DUMMY, null,
                                    ParameterFormatClimate.DUMMY,
                                    climatePeriodReportData.getClimo()
                                            .getMaxTempRecord(),
                                    climatePeriodReportData.getClimo()
                                            .getDayMaxTempRecordList(),

                                    ParameterFormatClimate.DUMMY,

                                    null, false, false));
                    integerLine.replace(1, 5, "High");
                    periodTemp.append(integerLine.toString());
                }

                if (currentSettings.getControl().getTempControl().getMinTemp()
                        .isRecord()
                        && currentSettings.getControl().getTempControl()
                                .getMinTemp().isMeasured()) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMinTemp(),
                                    ParameterFormatClimate.DUMMY, null,
                                    ParameterFormatClimate.DUMMY,
                                    climatePeriodReportData.getClimo()
                                            .getMinTempRecord(),
                                    climatePeriodReportData.getClimo()
                                            .getDayMinTempRecordList(),

                                    ParameterFormatClimate.DUMMY,

                                    null, false, false));
                    integerLine.replace(1, 4, "Low");
                    periodTemp.append(integerLine.toString());
                }
            }

            if (currentSettings.getControl().getTempControl().getMaxTemp()
                    .isMeasured()) {

                boolean newRecord = false;
                if (climatePeriodReportData.getData()
                        .getMaxTemp() >= climatePeriodReportData.getClimo()
                                .getMaxTempRecord()
                        && climatePeriodReportData.getData()
                                .getMaxTemp() != ParameterFormatClimate.MISSING
                        && climatePeriodReportData.getClimo()
                                .getMaxTempRecord() != ParameterFormatClimate.MISSING) {
                    newRecord = true;
                }

                int nearestInt = ClimateUtilities.nint(
                        climatePeriodReportData.getClimo().getMaxTempNorm());

                // "Highest" row
                StringBuilder integerLine = new StringBuilder(
                        buildNWWSIntegerLine(
                                currentSettings.getControl().getTempControl()
                                        .getMaxTemp(),
                                climatePeriodReportData.getData().getMaxTemp(),
                                climatePeriodReportData.getData()
                                        .getDayMaxTempList(),
                                nearestInt, ParameterFormatClimate.DUMMY, null,
                                climatePeriodReportData.getLastYearData()
                                        .getMaxTemp(),
                                climatePeriodReportData.getLastYearData()
                                        .getDayMaxTempList(),
                                newRecord, false));

                integerLine.replace(0, HIGHEST.length(),
                        WordUtils.capitalize(HIGHEST));
                periodTemp.append(integerLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMinTemp()
                    .isMeasured()) {

                boolean newRecord = false;
                if (climatePeriodReportData.getData()
                        .getMinTemp() <= climatePeriodReportData.getClimo()
                                .getMinTempRecord()
                        && climatePeriodReportData.getData()
                                .getMinTemp() != ParameterFormatClimate.MISSING
                        && climatePeriodReportData.getClimo()
                                .getMinTempRecord() != ParameterFormatClimate.MISSING) {
                    newRecord = true;
                }

                int nearestInt = ClimateUtilities.nint(
                        climatePeriodReportData.getClimo().getMinTempNorm());

                // "Lowest" row
                StringBuilder integerLine = new StringBuilder(
                        buildNWWSIntegerLine(
                                currentSettings.getControl().getTempControl()
                                        .getMaxTemp(),
                                climatePeriodReportData.getData().getMinTemp(),
                                climatePeriodReportData.getData()
                                        .getDayMinTempList(),
                                nearestInt, ParameterFormatClimate.DUMMY, null,
                                climatePeriodReportData.getLastYearData()
                                        .getMinTemp(),
                                climatePeriodReportData.getLastYearData()
                                        .getDayMinTempList(),
                                newRecord, false));

                integerLine.replace(0, LOWEST.length(),
                        WordUtils.capitalize(LOWEST));
                periodTemp.append(integerLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMeanMaxTemp()
                    .isMeasured()) {

                // "Avg. Maximum" row
                StringBuilder floatLine = new StringBuilder(buildNWWSFloatLine(
                        currentSettings.getControl().getTempControl()
                                .getMeanMaxTemp(),
                        climatePeriodReportData.getData().getMaxTempMean(),
                        null, null,
                        climatePeriodReportData.getClimo().getNormMeanMaxTemp(),
                        (float) ParameterFormatClimate.DUMMY_FLOAT, null, null,
                        climatePeriodReportData.getLastYearData()
                                .getMaxTempMean(),
                        null, null, false, DecimalType.TEMP));

                floatLine.replace(0, "Avg. Maximum".length(), "Avg. Maximum");
                periodTemp.append(floatLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMeanMinTemp()
                    .isMeasured()) {
                // "Avg. Minimum" row
                StringBuilder floatLine = new StringBuilder(buildNWWSFloatLine(
                        currentSettings.getControl().getTempControl()
                                .getMeanMinTemp(),
                        climatePeriodReportData.getData().getMinTempMean(),
                        null, null,
                        climatePeriodReportData.getClimo().getNormMeanMinTemp(),
                        (float) ParameterFormatClimate.DUMMY_FLOAT, null, null,
                        climatePeriodReportData.getLastYearData()
                                .getMinTempMean(),
                        null, null, false, DecimalType.TEMP));

                floatLine.replace(0, "Avg. Minimum".length(), "Avg. Minimum");
                periodTemp.append(floatLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMeanTemp()
                    .isMeasured()) {
                // "Mean" row
                StringBuilder floatLine = new StringBuilder(buildNWWSFloatLine(
                        currentSettings.getControl().getTempControl()
                                .getMeanTemp(),
                        climatePeriodReportData.getData().getMeanTemp(), null,
                        null,
                        climatePeriodReportData.getClimo().getNormMeanTemp(),
                        (float) ParameterFormatClimate.DUMMY_FLOAT, null, null,
                        climatePeriodReportData.getLastYearData().getMeanTemp(),
                        null, null, false, DecimalType.TEMP));

                floatLine.replace(0, "Mean".length(), "Mean");
                periodTemp.append(floatLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMaxTempGE90()
                    .isMeasured()) {
                // "Days Max >= 90" row
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(
                                currentSettings.getControl().getTempControl()
                                        .getMaxTempGE90(),
                                climatePeriodReportData.getData()
                                        .getNumMaxGreaterThan90F(),
                                climatePeriodReportData.getClimo()
                                        .getNormNumMaxGE90F(),
                                climatePeriodReportData.getLastYearData()
                                        .getNumMaxGreaterThan90F()));

                threshLine.replace(0, "Days Max >= 90".length(),
                        "Days Max >= 90");
                periodTemp.append(threshLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMaxTempLE32()
                    .isMeasured()) {
                // "Days Max <= 32" row
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(
                                currentSettings.getControl().getTempControl()
                                        .getMaxTempLE32(),
                                climatePeriodReportData.getData()
                                        .getNumMaxLessThan32F(),
                                climatePeriodReportData.getClimo()
                                        .getNormNumMaxLE32F(),
                                climatePeriodReportData.getLastYearData()
                                        .getNumMaxLessThan32F()));

                threshLine.replace(0, "Days Max <= 32".length(),
                        "Days Max <= 32");
                periodTemp.append(threshLine.toString());
            }

            // "Days Max >= <user defined>" rows
            if (currentSettings.getControl().getTempControl().getMaxTempGET1()
                    .isMeasured()) {
                if (globalConfig.getT1() != ParameterFormatClimate.MISSING) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMaxTempGET1(),
                                    climatePeriodReportData.getData()
                                            .getNumMaxGreaterThanT1F(),
                                    null, ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    climatePeriodReportData.getLastYearData()
                                            .getNumMaxGreaterThanT1F(),
                                    null, false, false));

                    String dayString = "Days Max >= " + globalConfig.getT1();
                    integerLine.replace(0, dayString.length(), dayString);
                    periodTemp.append(integerLine.toString());
                }
            }

            if (currentSettings.getControl().getTempControl().getMaxTempGET2()
                    .isMeasured()) {
                if (globalConfig.getT2() != ParameterFormatClimate.MISSING) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMaxTempGET2(),
                                    climatePeriodReportData.getData()
                                            .getNumMaxGreaterThanT2F(),
                                    null, ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    climatePeriodReportData.getLastYearData()
                                            .getNumMaxGreaterThanT2F(),
                                    null, false, false));

                    String dayString = "Days Max >= " + globalConfig.getT2();
                    integerLine.replace(0, dayString.length(), dayString);
                    periodTemp.append(integerLine.toString());
                }
            }

            if (currentSettings.getControl().getTempControl().getMaxTempLET3()
                    .isMeasured()) {
                if (globalConfig.getT3() != ParameterFormatClimate.MISSING) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMaxTempLET3(),
                                    climatePeriodReportData.getData()
                                            .getNumMaxLessThanT3F(),
                                    null, ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    climatePeriodReportData.getLastYearData()
                                            .getNumMaxLessThanT3F(),
                                    null, false, false));

                    String dayString = "Days Max <= " + globalConfig.getT3();
                    integerLine.replace(0, dayString.length(), dayString);
                    periodTemp.append(integerLine.toString());
                }
            }

            if (currentSettings.getControl().getTempControl().getMinTempLE32()
                    .isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(
                                currentSettings.getControl().getTempControl()
                                        .getMinTempLE32(),
                                climatePeriodReportData.getData()
                                        .getNumMinLessThan32F(),
                                climatePeriodReportData.getClimo()
                                        .getNormNumMinLE32F(),
                                climatePeriodReportData.getLastYearData()
                                        .getNumMinLessThan32F()));

                String dayString = "Days Min <= 32";
                threshLine.replace(0, dayString.length(), dayString);
                periodTemp.append(threshLine.toString());

            }

            if (currentSettings.getControl().getTempControl().getMinTempLE0()
                    .isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(
                                currentSettings.getControl().getTempControl()
                                        .getMinTempLE0(),
                                climatePeriodReportData.getData()
                                        .getNumMinLessThan0F(),
                                climatePeriodReportData.getClimo()
                                        .getNormNumMinLE0F(),
                                climatePeriodReportData.getLastYearData()
                                        .getNumMinLessThan0F()));

                String dayString = "Days Min <= 0";
                threshLine.replace(0, dayString.length(), dayString);
                periodTemp.append(threshLine.toString());
            }

            if (currentSettings.getControl().getTempControl().getMinTempGET4()
                    .isMeasured()) {
                if (globalConfig.getT4() != ParameterFormatClimate.MISSING) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMinTempGET4(),
                                    climatePeriodReportData.getData()
                                            .getNumMinGreaterThanT4F(),
                                    null, ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    climatePeriodReportData.getLastYearData()
                                            .getNumMinGreaterThanT4F(),
                                    null, false, false));

                    String dayString = "Days Min >= " + globalConfig.getT4();
                    integerLine.replace(0, dayString.length(), dayString);
                    periodTemp.append(integerLine.toString());
                }
            }

            if (currentSettings.getControl().getTempControl().getMinTempLET5()
                    .isMeasured()) {
                if (globalConfig.getT5() != ParameterFormatClimate.MISSING) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(currentSettings.getControl()
                                    .getTempControl().getMinTempLET5(),

                                    climatePeriodReportData.getData()
                                            .getNumMinLessThanT5F(),
                                    null, ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    climatePeriodReportData.getLastYearData()
                                            .getNumMinLessThanT5F(),
                                    null, false, false));

                    String dayString = "Days Min <= " + globalConfig.getT5();
                    integerLine.replace(0, dayString.length(), dayString);
                    periodTemp.append(integerLine.toString());
                }
            }

            if (currentSettings.getControl().getTempControl().getMinTempLET6()
                    .isMeasured()) {
                if (globalConfig.getT6() != ParameterFormatClimate.MISSING) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(
                                    currentSettings.getControl()
                                            .getTempControl().getMinTempLET6(),
                                    climatePeriodReportData.getData()
                                            .getNumMinLessThanT6F(),
                                    null, ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    climatePeriodReportData.getLastYearData()
                                            .getNumMinLessThanT6F(),
                                    null, false, false));

                    String dayString = "Days Min <= " + globalConfig.getT6();
                    integerLine.replace(0, dayString.length(), dayString);
                    periodTemp.append(integerLine.toString());
                }
            }
        }

        return periodTemp.toString() + "\n";
    }

    /**
     * Migrated from build_period_liquid_precip.c
     * 
     * <pre>
    *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CREATE THE LIQUID PRECIP SEGMENT OF THE NWWS TABLE.
     *
     * </pre>
     * 
     * @param climatePeriodReportData
     * @return
     */
    private String buildPeriodLiquidPrecip(
            ClimatePeriodReportData climatePeriodReportData) {
        StringBuilder liquidPrecip = new StringBuilder();

        PrecipitationControlFlags precipFlag = currentSettings.getControl()
                .getPrecipControl();

        PeriodClimo recordData = climatePeriodReportData.getClimo();
        PeriodData actualData = climatePeriodReportData.getData();
        PeriodData lastYearData = climatePeriodReportData.getLastYearData();

        float measured = (float) ((actualData
                .getPrecipTotal() == ParameterFormatClimate.TRACE) ? 0.005
                        : actualData.getPrecipTotal());
        float recMax = (float) ((recordData
                .getPrecipPeriodMax() == ParameterFormatClimate.TRACE) ? 0.005
                        : recordData.getPrecipPeriodMax());
        float recMin = (float) ((recordData
                .getPrecipPeriodMin() == ParameterFormatClimate.TRACE) ? 0.005
                        : recordData.getPrecipPeriodMin());

        /**
         * Create precipitation rows
         * 
         * <pre>
         * Example:
         * 
         * PRECIPITATION (INCHES)
         * RECORD
         *  MAXIMUM         8.84   1891
         *  MINIMUM         0.05   2006
         * TOTALS           3.19              3.48   -0.29     1.16
         * DAILY AVG.       0.10              0.11   -0.01
         * DAYS >= .01        14              10.5     3.5
         * DAYS >= .10         6               6.5    -0.5
         * DAYS >= .50         2               2.4    -0.4
         * DAYS >= 1.00        0               0.9    -0.9
         * GREATEST
         *  24 HR. TOTAL    0.96   03/31 TO 03/31
         * </pre>
         */
        if (precipFlag.getPrecipTotal().isMeasured()
                || precipFlag.getPrecipAvg().isMeasured()
                || precipFlag.getPrecip24HR().isMeasured()
                || precipFlag.getPrecipStormMax().isMeasured()
                || precipFlag.getPrecipGE01().isMeasured()
                || precipFlag.getPrecipGE10().isMeasured()
                || precipFlag.getPrecipGE50().isMeasured()
                || precipFlag.getPrecipGE100().isMeasured()
                || (globalConfig.getP1() != 0.
                        && precipFlag.getPrecipGEP1().isMeasured())
                || (globalConfig.getP2() != 0.
                        && precipFlag.getPrecipGEP2().isMeasured())) {
            liquidPrecip.append(WordUtils.capitalize(PRECIPITATION))
                    .append(SPACE).append("(").append(INCHES).append(")\n");

            if (precipFlag.getPrecipTotal().isMeasured()) {
                if (precipFlag.getPrecipTotal().isRecord()
                        || precipFlag.getPrecipMin().isRecord()) {
                    liquidPrecip.append(WordUtils.capitalize(RECORD))
                            .append("\n");

                    if (precipFlag.getPrecipTotal().isRecord()) {
                        StringBuilder floatLine = new StringBuilder(
                                buildNWWSFloatLine(precipFlag.getPrecipTotal(),
                                        ParameterFormatClimate.DUMMY, null,
                                        null, ParameterFormatClimate.DUMMY,
                                        recordData.getPrecipPeriodMax(),
                                        recordData.getPrecipPeriodMaxYearList(),
                                        null, ParameterFormatClimate.DUMMY,
                                        null, null, false,
                                        DecimalType.PRECIP));
                        floatLine.replace(1, 1 + MAXIMUM.length(),
                                WordUtils.capitalize(MAXIMUM));
                        liquidPrecip.append(floatLine.toString());
                    }

                    if (precipFlag.getPrecipMin().isRecord()) {
                        StringBuilder floatLine = new StringBuilder(
                                buildNWWSFloatLine(precipFlag.getPrecipMin(),
                                        ParameterFormatClimate.DUMMY, null,
                                        null, ParameterFormatClimate.DUMMY,
                                        recordData.getPrecipPeriodMin(),
                                        recordData.getPrecipPeriodMinYearList(),
                                        null, ParameterFormatClimate.DUMMY,
                                        null, null, false,
                                        DecimalType.PRECIP));
                        floatLine.replace(1, 1 + MINIMUM.length(),
                                WordUtils.capitalize(MINIMUM));
                        liquidPrecip.append(floatLine.toString());
                    }
                }
            }

            if (precipFlag.getPrecipTotal().isMeasured()) {
                boolean newRecord = false;

                if (measured != ParameterFormatClimate.MISSING
                        && recMax != ParameterFormatClimate.MISSING) {
                    newRecord = (measured > recMax) ? true : false;

                    newRecord = (ClimateUtilities.floatingEquals(measured,
                            recMax) && recMax != 0) ? true : false;
                }

                if (measured <= recMin
                        && measured != ParameterFormatClimate.MISSING
                        && recMin != ParameterFormatClimate.MISSING) {
                    newRecord = true;
                }

                StringBuilder floatLine = new StringBuilder(
                        buildNWWSFloatLine(precipFlag.getPrecipTotal(),
                                actualData.getPrecipTotal(), null, null,
                                recordData.getPrecipPeriodNorm(),
                                ParameterFormatClimate.DUMMY, null, null,
                                lastYearData.getPrecipTotal(), null, null,
                                newRecord, DecimalType.PRECIP));

                floatLine.replace(0, TOTALS.length(),
                        WordUtils.capitalize(TOTALS));
                liquidPrecip.append(floatLine.toString());
            }

            if (precipFlag.getPrecipAvg().isMeasured()) {

                StringBuilder floatLine = new StringBuilder(
                        buildNWWSFloatLine(precipFlag.getPrecipAvg(),
                                actualData.getPrecipMeanDay(), null, null,
                                recordData.getPrecipDayNorm(),
                                ParameterFormatClimate.DUMMY, null, null,
                                lastYearData.getPrecipMeanDay(), null, null,
                                false, DecimalType.PRECIP));

                floatLine.replace(0, "Daily Avg.".length(), "Daily Avg.");
                liquidPrecip.append(floatLine.toString());
            }

            if (precipFlag.getPrecipGE01().isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(precipFlag.getPrecipGE01(),
                                actualData.getNumPrcpGreaterThan01(),
                                recordData.getNumPrcpGE01Norm(),
                                lastYearData.getNumPrcpGreaterThan01()));

                threshLine.replace(0, "Days >= .01".length(), "Days >= .01");
                liquidPrecip.append(threshLine.toString());
            }

            if (precipFlag.getPrecipGE10().isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(precipFlag.getPrecipGE10(),
                                actualData.getNumPrcpGreaterThan10(),
                                recordData.getNumPrcpGE10Norm(),
                                lastYearData.getNumPrcpGreaterThan10()));

                threshLine.replace(0, "Days >= .10".length(), "Days >= .10");
                liquidPrecip.append(threshLine.toString());
            }

            if (precipFlag.getPrecipGE50().isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(precipFlag.getPrecipGE50(),
                                actualData.getNumPrcpGreaterThan50(),
                                recordData.getNumPrcpGE50Norm(),
                                lastYearData.getNumPrcpGreaterThan50()));

                threshLine.replace(0, "Days >= .50".length(), "Days >= .50");
                liquidPrecip.append(threshLine.toString());
            }

            if (precipFlag.getPrecipGE100().isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(precipFlag.getPrecipGE100(),
                                actualData.getNumPrcpGreaterThan100(),
                                recordData.getNumPrcpGE100Norm(),
                                lastYearData.getNumPrcpGreaterThan100()));

                threshLine.replace(0, "Days >= 1.00".length(), "Days >= 1.00");
                liquidPrecip.append(threshLine.toString());
            }

            if (precipFlag.getPrecipGEP1().isMeasured()) {
                if (globalConfig
                        .getP1() != ParameterFormatClimate.MISSING_PRECIP) {

                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(precipFlag.getPrecipGEP1(),
                                    actualData.getNumPrcpGreaterThanP1(), null,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    lastYearData.getNumPrcpGreaterThanP1(),
                                    null, false, false));

                    String dayString = globalConfig
                            .getP1() != ParameterFormatClimate.TRACE
                                    ? "Days >= %-5.2f" : "Days >= T";

                    integerLine.replace(0, dayString.length(), dayString);
                    liquidPrecip.append(integerLine.toString());
                }
            }

            if (precipFlag.getPrecipGEP2().isMeasured()) {
                if (globalConfig
                        .getP2() != ParameterFormatClimate.MISSING_PRECIP) {

                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(precipFlag.getPrecipGEP2(),
                                    actualData.getNumPrcpGreaterThanP2(), null,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    lastYearData.getNumPrcpGreaterThanP2(),
                                    null, false, false));

                    String dayString = globalConfig
                            .getP2() != ParameterFormatClimate.TRACE
                                    ? "Days >= %-5.2f" : "Days >= T";

                    integerLine.replace(0, dayString.length(), dayString);
                    liquidPrecip.append(integerLine.toString());
                }
            }

            if (precipFlag.getPrecip24HR().isMeasured()
                    || precipFlag.getPrecipStormMax().isMeasured()) {
                liquidPrecip.append("Greatest\n");

                if (precipFlag.getPrecip24HR().isMeasured()) {

                    List<ClimateDates> dummyActual24HDates = new ArrayList<>();
                    List<ClimateDates> actual24HDates = actualData
                            .getPrecip24HDates();
                    for (int i = 0; i < actual24HDates.size(); i++) {
                        ClimateTime startTime = actual24HDates.get(i)
                                .getStartTime();
                        ClimateTime endTime = actual24HDates.get(i)
                                .getEndTime();
                        startTime.setHour(ParameterFormatClimate.DUMMY_DATA);
                        endTime.setHour(ParameterFormatClimate.DUMMY_DATA);

                        dummyActual24HDates.add(new ClimateDates(
                                actual24HDates.get(i).getStart(),
                                actual24HDates.get(i).getEnd(), startTime,
                                endTime));

                    }

                    List<ClimateDates> dummyLast24HDates = new ArrayList<>();
                    List<ClimateDates> last24HDates = lastYearData
                            .getPrecip24HDates();
                    for (int i = 0; i < last24HDates.size(); i++) {
                        ClimateTime startTime = last24HDates.get(i)
                                .getStartTime();
                        ClimateTime endTime = last24HDates.get(i).getEndTime();
                        startTime.setHour(ParameterFormatClimate.DUMMY_DATA);
                        endTime.setHour(ParameterFormatClimate.DUMMY_DATA);

                        dummyLast24HDates.add(
                                new ClimateDates(last24HDates.get(i).getStart(),
                                        last24HDates.get(i).getEnd(), startTime,
                                        endTime));

                    }

                    StringBuilder floatLine = new StringBuilder(
                            buildNWWSFloatLine(precipFlag.getPrecip24HR(),
                                    actualData.getPrecipMax24H(), null,
                                    dummyActual24HDates,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null, null,
                                    lastYearData.getPrecipMax24H(), null,
                                    dummyLast24HDates, false,
                                    DecimalType.PRECIP));

                    floatLine.replace(1, 1 + "24 Hr. Total".length(),
                            "24 Hr. Total");
                    liquidPrecip.append(floatLine.toString());

                }

                if (precipFlag.getPrecipStormMax().isMeasured()) {
                    StringBuilder floatLine1 = new StringBuilder(
                            buildNWWSFloatLine(precipFlag.getPrecipStormMax(),
                                    actualData.getPrecipStormMax(), null, null,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null, null,
                                    lastYearData.getPrecipStormMax(), null,
                                    null, false, DecimalType.PRECIP));
                    floatLine1.replace(1, 1 + "Storm Total".length(),
                            "Storm Total");
                    liquidPrecip.append(floatLine1.toString());
                }
                if (precipFlag.getPrecipStormMax().isTimeOfMeasured()) {
                    StringBuilder floatLine2 = new StringBuilder(
                            buildNWWSFloatLine(precipFlag.getPrecipStormMax(),
                                    ParameterFormatClimate.DUMMY, null,
                                    actualData.getPrecipStormList(),
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null, null,
                                    ParameterFormatClimate.DUMMY, null,
                                    lastYearData.getPrecipStormList(), false,
                                    DecimalType.PRECIP));
                    floatLine2.replace(1, 1 + "(mm/dd(hh))".length(),
                            "(mm/dd(hh))");
                    liquidPrecip.append(floatLine2.toString());
                }
            }

        }

        return liquidPrecip.toString() + "\n";
    }

    /**
     * Migrated from build_period_snow_precip.c.
     * 
     * <pre>
    *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CREATE THE SNOW SEGMENT OF THE NWWS TABLE.
     *
     * </pre>
     * 
     * @param climatePeriodReportData
     * @return
     */
    private String buildPeriodSnowPrecip(
            ClimatePeriodReportData climatePeriodReportData,
            ClimateDate beginDate) {
        StringBuilder snowPrecip = new StringBuilder();

        SnowControlFlags snowFlag = currentSettings.getControl()
                .getSnowControl();

        PeriodClimo recordData = climatePeriodReportData.getClimo();
        PeriodData actualData = climatePeriodReportData.getData();
        PeriodData lastYearData = climatePeriodReportData.getLastYearData();

        /**
         * Create snowfall rows
         * 
         * <pre>
         * Example:
         * 
         * SNOWFALL (INCHES)
         * RECORDS
         *  TOTAL           19.3   1914
         * TOTALS            2.0               1.3     0.7
         * SINCE 7/1         3.4              15.4   -12.0
         * SNOWDEPTH AVG.      0
         * DAYS >= TRACE       5               0.9     4.1
         * DAYS >= 1.0         1               0.4     0.6
         * GREATEST
         *  SNOW DEPTH         2   03/14
         *  24 HR TOTAL      1.1
         * </pre>
         */
        if (snowFlag.getSnowTotal().isMeasured()
                || snowFlag.getSnowWaterTotal().isMeasured()
                || snowFlag.getSnowJuly1().isMeasured()
                || snowFlag.getSnowWaterJuly1().isMeasured()
                || snowFlag.getSnowDepthAvg().isMeasured()
                || snowFlag.getSnowGE100().isMeasured()
                || snowFlag.getSnowAny().isMeasured()
                || (globalConfig.getS1() != 0
                        && snowFlag.getSnowGEP1().isMeasured())
                || snowFlag.getSnowDepthMax().isMeasured()
                || snowFlag.getSnow24hr().isMeasured()
                || snowFlag.getSnowStormMax().isMeasured()) {

            if (snowFlag.getSnowTotal().isMeasured()) {

                snowPrecip.append(WordUtils.capitalize(SNOWFALL)).append(SPACE)
                        .append("(").append(INCHES).append(")\n");

                if (snowFlag.getSnowTotal().isRecord()
                        || snowFlag.getSnow24hr().isRecord()
                        || snowFlag.getSnowDepthAvg().isRecord()) {
                    snowPrecip.append(WordUtils.capitalize(RECORD))
                            .append("s\n");

                    if (snowFlag.getSnowTotal().isRecord()) {
                        StringBuilder floatLine = new StringBuilder(
                                buildNWWSFloatLine(snowFlag.getSnowTotal(),
                                        ParameterFormatClimate.DUMMY, null,
                                        null, ParameterFormatClimate.DUMMY,
                                        recordData.getSnowPeriodRecord(),
                                        recordData.getSnowPeriodMaxYearList(),
                                        null, ParameterFormatClimate.DUMMY,
                                        null, null, false, DecimalType.SNOW));

                        floatLine.replace(1, 1 + TOTAL.length(), TOTAL);
                        snowPrecip.append(floatLine.toString());
                    }

                    if (snowFlag.getSnow24hr().isRecord()) {
                        StringBuilder floatLine = new StringBuilder(
                                buildNWWSFloatLine(snowFlag.getSnow24hr(),
                                        ParameterFormatClimate.DUMMY, null,
                                        null, ParameterFormatClimate.DUMMY,
                                        recordData.getSnowMax24HRecord(), null,
                                        recordData.getSnow24HList(),
                                        ParameterFormatClimate.DUMMY, null,
                                        null, false, DecimalType.SNOW));

                        floatLine.replace(1, 1 + "24 Hr Total".length(),
                                "24 Hr Total");
                        snowPrecip.append(floatLine.toString());
                    }

                    if (snowFlag.getSnowDepthAvg().isRecord()) {
                        StringBuilder floatLine = new StringBuilder(
                                buildNWWSIntegerLine(snowFlag.getSnowDepthAvg(),
                                        ParameterFormatClimate.DUMMY, null,
                                        ParameterFormatClimate.DUMMY,
                                        recordData.getSnowGroundMax(),
                                        recordData.getDaySnowGroundMaxList(),
                                        ParameterFormatClimate.DUMMY, null,
                                        false, true));

                        floatLine.replace(1, 1 + SNOW_DEPTH.length(),
                                WordUtils.capitalize(SNOW_DEPTH));
                        snowPrecip.append(floatLine.toString());
                    }
                }
            }

            if (snowFlag.getSnowTotal().isMeasured()) {
                float snowMeasured = actualData.getSnowTotal();
                float snowRecord = recordData.getSnowPeriodRecord();

                snowRecord = (float) ((snowRecord == ParameterFormatClimate.TRACE)
                        ? 0.05 : snowRecord);
                snowMeasured = (float) ((snowMeasured == ParameterFormatClimate.TRACE)
                        ? 0.05 : snowMeasured);

                boolean newRecord = false;
                if (snowMeasured != ParameterFormatClimate.MISSING
                        && snowRecord != ParameterFormatClimate.MISSING) {
                    newRecord = (snowMeasured > snowRecord) ? true : false;
                    newRecord = (snowRecord - snowMeasured) < 0.02
                            && snowRecord != 0 ? true : false;
                }
                StringBuilder floatLine = new StringBuilder(buildNWWSFloatLine(
                        snowFlag.getSnowTotal(), actualData.getSnowTotal(),
                        null, null, recordData.getSnowPeriodNorm(),
                        ParameterFormatClimate.DUMMY, null, null,
                        lastYearData.getSnowTotal(), null, null, newRecord,
                        DecimalType.SNOW));

                floatLine.replace(0, TOTALS.length(), TOTALS);
                snowPrecip.append(floatLine.toString());
            }

            if (snowFlag.getSnowWaterTotal().isMeasured()) {

                StringBuilder floatLine = new StringBuilder(buildNWWSFloatLine(
                        snowFlag.getSnowWaterTotal(), actualData.getSnowWater(),
                        null, null, recordData.getSnowWaterPeriodNorm(),
                        ParameterFormatClimate.DUMMY, null, null,
                        lastYearData.getSnowWater(), null, null, false,
                        DecimalType.PRECIP));

                floatLine.replace(1, 1 + "Liquid Equiv".length(),
                        "Liquid Equiv");
                snowPrecip.append(floatLine.toString());
            }

            if (snowFlag.getSnowJuly1().isMeasured()) {

                StringBuilder floatLine = new StringBuilder(buildNWWSFloatLine(
                        snowFlag.getSnowJuly1(), actualData.getSnowJuly1(),
                        null, null, recordData.getSnowJuly1Norm(),
                        ParameterFormatClimate.DUMMY, null, null,
                        lastYearData.getSnowJuly1(), null, null, false,
                        DecimalType.SNOW));

                floatLine.replace(0, "Since 7/1".length(), "Since 7/1");
                snowPrecip.append(floatLine.toString());
            }

            if (snowFlag.getSnowWaterJuly1().isMeasured()) {

                StringBuilder floatLine = new StringBuilder(
                        buildNWWSFloatLine(snowFlag.getSnowWaterJuly1(),
                                actualData.getSnowWaterJuly1(), null, null,
                                recordData.getSnowWaterJuly1Norm(),
                                ParameterFormatClimate.DUMMY, null, null,
                                lastYearData.getSnowWaterJuly1(), null, null,
                                false, DecimalType.PRECIP));

                floatLine.replace(1, 1 + "Liquid 7/1".length(), "Liquid 7/1");
                snowPrecip.append(floatLine.toString());
            }

            if (snowFlag.getSnowDepthAvg().isMeasured()) {
                int snowGround = ClimateUtilities
                        .nint(actualData.getSnowGroundMean());
                int snowNorm = ClimateUtilities
                        .nint(recordData.getSnowGroundNorm());
                int snowLast = ClimateUtilities
                        .nint(lastYearData.getSnowGroundMean());

                StringBuilder integerLine = new StringBuilder(
                        buildNWWSIntegerLine(snowFlag.getSnowDepthAvg(),
                                snowGround, null, ParameterFormatClimate.DUMMY,
                                snowNorm, null, snowLast, null, false, true));

                integerLine.replace(0, "Snowdepth Avg.".length(),
                        "Snowdepth Avg.");
                snowPrecip.append(integerLine.toString());
            }

            if (snowFlag.getSnowAny().isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(snowFlag.getSnowAny(),
                                actualData.getNumSnowGreaterThanTR(),
                                recordData.getNumSnowGETRNorm(),
                                lastYearData.getNumSnowGreaterThanTR()));

                threshLine.replace(0, "Days >= Trace".length(),
                        "Days >= Trace");
                snowPrecip.append(threshLine.toString());
            }

            if (snowFlag.getSnowGE100().isMeasured()) {
                StringBuilder threshLine = new StringBuilder(
                        buildNWWSThreshLine(snowFlag.getSnowGE100(),
                                actualData.getNumSnowGreaterThan1(),
                                recordData.getNumSnowGE1Norm(),
                                lastYearData.getNumSnowGreaterThan1()));

                threshLine.replace(0, "Days >= 1.0".length(), "Days >= 1.0");
                snowPrecip.append(threshLine.toString());
            }

            if (snowFlag.getSnowGEP1().isMeasured()) {
                if (globalConfig
                        .getS1() != ParameterFormatClimate.MISSING_SNOW) {
                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(snowFlag.getSnowGEP1(),
                                    actualData.getNumSnowGreaterThanS1(), null,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    lastYearData.getNumSnowGreaterThanS1(),
                                    null, false, false));

                    String dayString = globalConfig
                            .getS1() != ParameterFormatClimate.TRACE
                                    ? "Days >= " + String.format("%-4.1f",
                                            globalConfig.getS1())
                                    : "Days >= T";

                    integerLine.replace(0, dayString.length(), dayString);
                    snowPrecip.append(integerLine);
                }
            }

            if (snowFlag.getSnowDepthMax().isMeasured()
                    || snowFlag.getSnow24hr().isMeasured()
                    || snowFlag.getSnowStormMax().isMeasured()) {
                snowPrecip.append("Greatest\n");

                if (snowFlag.getSnowDepthMax().isMeasured()) {
                    boolean newRecord = false;
                    float snowMeasured = actualData.getSnowGroundMax();
                    float snowRecord = recordData.getSnowGroundMax();

                    snowRecord = (float) ((snowRecord == ParameterFormatClimate.TRACE)
                            ? 0.05 : snowRecord);
                    snowMeasured = (float) ((snowMeasured == ParameterFormatClimate.TRACE)
                            ? 0.05 : snowMeasured);

                    if (snowMeasured != ParameterFormatClimate.MISSING
                            && snowRecord != ParameterFormatClimate.MISSING) {
                        newRecord = (snowMeasured > snowRecord) ? true : false;
                        newRecord = (snowRecord - snowMeasured < 0.002
                                && snowRecord != 0) ? true : false;
                    }

                    StringBuilder integerLine = new StringBuilder(
                            buildNWWSIntegerLine(snowFlag.getSnowDepthMax(),
                                    actualData.getSnowGroundMax(),
                                    actualData.getSnowGroundMaxDateList(),
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null,
                                    lastYearData.getSnowGroundMax(),
                                    lastYearData.getSnowGroundMaxDateList(),
                                    newRecord, true));

                    integerLine.replace(1, 1 + SNOW_DEPTH.length(), SNOW_DEPTH);
                    snowPrecip.append(integerLine);

                }

                if (snowFlag.getSnow24hr().isMeasured()) {
                    boolean newRecord = false;
                    float snowMeasured = actualData.getSnowMax24H();
                    float snowRecord = recordData.getSnowMax24HRecord();

                    snowRecord = (float) ((snowRecord == ParameterFormatClimate.TRACE)
                            ? 0.05 : snowRecord);
                    snowMeasured = (float) ((snowMeasured == ParameterFormatClimate.TRACE)
                            ? 0.05 : snowMeasured);

                    if (snowMeasured != ParameterFormatClimate.MISSING
                            && snowRecord != ParameterFormatClimate.MISSING) {
                        newRecord = (snowMeasured > snowRecord) ? true : false;
                        newRecord = (snowRecord - snowMeasured < 0.002
                                && snowRecord != 0) ? true : false;
                    }

                    List<ClimateDates> dummyActual24HDates = new ArrayList<>();
                    List<ClimateDates> actual24HDates = actualData
                            .getSnow24HDates();
                    for (int i = 0; i < actual24HDates.size(); i++) {
                        ClimateTime startTime = actual24HDates.get(i)
                                .getStartTime();
                        ClimateTime endTime = actual24HDates.get(i)
                                .getEndTime();
                        startTime.setHour(ParameterFormatClimate.DUMMY_DATA);
                        endTime.setHour(ParameterFormatClimate.DUMMY_DATA);

                        dummyActual24HDates.add(new ClimateDates(
                                actual24HDates.get(i).getStart(),
                                actual24HDates.get(i).getEnd(), startTime,
                                endTime));

                    }

                    List<ClimateDates> dummyLast24HDates = new ArrayList<>();
                    List<ClimateDates> last24HDates = lastYearData
                            .getSnow24HDates();
                    for (int i = 0; i < last24HDates.size(); i++) {
                        ClimateTime startTime = last24HDates.get(i)
                                .getStartTime();
                        ClimateTime endTime = last24HDates.get(i).getEndTime();
                        startTime.setHour(ParameterFormatClimate.DUMMY_DATA);
                        endTime.setHour(ParameterFormatClimate.DUMMY_DATA);

                        dummyLast24HDates.add(
                                new ClimateDates(last24HDates.get(i).getStart(),
                                        last24HDates.get(i).getEnd(), startTime,
                                        endTime));

                    }

                    StringBuilder floatLine = new StringBuilder(
                            buildNWWSFloatLine(snowFlag.getSnow24hr(),
                                    actualData.getSnowMax24H(), null,
                                    dummyActual24HDates,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null, null,
                                    lastYearData.getSnowMax24H(), null,
                                    dummyLast24HDates, newRecord,
                                    DecimalType.SNOW));

                    floatLine.replace(1, 1 + "24 Hr Total".length(),
                            "24 Hr Total");
                    snowPrecip.append(floatLine.toString());

                }

                if (snowFlag.getSnowStormMax().isMeasured()) {
                    StringBuilder floatLine1 = new StringBuilder(
                            buildNWWSFloatLine(snowFlag.getSnowStormMax(),
                                    actualData.getSnowMaxStorm(), null, null,
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null, null,
                                    lastYearData.getSnowMaxStorm(), null, null,
                                    false, DecimalType.SNOW));
                    floatLine1.replace(1, 1 + "Storm Total".length(),
                            "Storm Total");
                    snowPrecip.append(floatLine1.toString());
                }

                if (snowFlag.getSnowStormMax().isTimeOfMeasured()) {
                    StringBuilder floatLine2 = new StringBuilder(
                            buildNWWSFloatLine(snowFlag.getSnowStormMax(),
                                    ParameterFormatClimate.DUMMY, null,
                                    actualData.getSnowStormList(),
                                    ParameterFormatClimate.DUMMY,
                                    ParameterFormatClimate.DUMMY, null, null,
                                    ParameterFormatClimate.DUMMY, null,
                                    lastYearData.getSnowStormList(), false,
                                    DecimalType.SNOW));
                    floatLine2.replace(1, 1 + "(mm/dd(hh))".length(),
                            "(mm/dd(hh))");
                    snowPrecip.append(floatLine2.toString());
                }
            }

        }

        return snowPrecip.toString() + "\n";
    }

    /**
     * Migrated from build_period_degree_days.c
     * 
     * <pre>
    *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS ROUTINE WILL CREATE THE DEGREE DAY SEGMENT OF THE NWWS TABLE.
     * </pre>
     * 
     * @param climatePeriodReportData
     * @return
     */
    private String buildPeriodDegreeDays(
            ClimatePeriodReportData climatePeriodReportData,
            ClimateDate beginDate) {
        StringBuilder degreeDays = new StringBuilder();

        boolean coolReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getCoolDates(), beginDate);
        boolean heatReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getHeatDates(), beginDate);
        DegreeDaysControlFlags degreeFlag = currentSettings.getControl()
                .getDegreeDaysControl();

        PeriodData actualData = climatePeriodReportData.getData();
        PeriodClimo recordData = climatePeriodReportData.getClimo();
        PeriodData lastYearData = climatePeriodReportData.getLastYearData();

        /**
         * Create degree day rows
         * 
         * <pre>
         * Example:
         * 
         * DEGREE_DAYS
         * HEATING TOTAL     548               568     -20      351
         *  SINCE 7/1       2924              3676    -752     2952
         * COOLING TOTAL       5                 2       3        3
         *  SINCE 1/1          6                 2       4        3
         * </pre>
         */
        if ((coolReport && (degreeFlag.getTotalCDD().isMeasured()
                || degreeFlag.getSeasonCDD().isMeasured()))
                || (heatReport && (degreeFlag.getTotalHDD().isMeasured()
                        || degreeFlag.getSeasonHDD().isMeasured()))) {
            degreeDays.append(DEGREE_DAYS).append("\n");
        }

        if (heatReport && (degreeFlag.getTotalHDD().isMeasured()
                || degreeFlag.getSeasonHDD().isMeasured())) {

            StringBuilder integerLine1;
            StringBuilder integerLine2 = new StringBuilder();

            if (degreeFlag.getTotalHDD().isMeasured()) {

                integerLine1 = new StringBuilder(buildNWWSIntegerLine(
                        degreeFlag.getTotalHDD(), actualData.getNumHeatTotal(),
                        null, recordData.getNumHeatPeriodNorm(),
                        ParameterFormatClimate.DUMMY, null,
                        lastYearData.getNumHeatTotal(), null, false, false));
            } else {
                integerLine1 = new StringBuilder(
                        emptyLine(ParameterFormatClimate.NUM_LINE1_NWWS + 1)
                                .toString()).append("\n");
            }

            if (degreeFlag.getSeasonHDD().isMeasured()) {
                integerLine2 = new StringBuilder(buildNWWSIntegerLine(
                        degreeFlag.getSeasonHDD(), actualData.getNumHeat1July(),
                        null, recordData.getNumHeat1JulyNorm(),
                        ParameterFormatClimate.DUMMY, null,
                        lastYearData.getNumHeat1July(), null, false, false));

                integerLine2.replace(1, 1 + "Since 7/1".length(), "Since 7/1");

            }

            if (degreeFlag.getTotalHDD().isMeasured()
                    || degreeFlag.getSeasonHDD().isMeasured()) {
                integerLine1.replace(0, (HEATING + SPACE + TOTAL).length(),
                        WordUtils.capitalize(HEATING + SPACE + TOTAL));
                degreeDays.append(
                        integerLine1.toString() + integerLine2.toString());
            }

        }

        if (coolReport && (degreeFlag.getTotalCDD().isMeasured()
                || degreeFlag.getSeasonCDD().isMeasured())) {

            StringBuilder integerLine1;

            if (degreeFlag.getTotalCDD().isMeasured()) {
                integerLine1 = new StringBuilder(buildNWWSIntegerLine(
                        degreeFlag.getTotalCDD(), actualData.getNumCoolTotal(),
                        null, recordData.getNumCoolPeriodNorm(),
                        ParameterFormatClimate.DUMMY, null,
                        lastYearData.getNumCoolTotal(), null, false, false));
            } else {
                integerLine1 = new StringBuilder(
                        emptyLine(ParameterFormatClimate.NUM_LINE1_NWWS + 1)
                                .toString()).append("\n");
            }

            integerLine1.replace(0, (COOLING + SPACE + TOTAL).length(),
                    WordUtils.capitalize(COOLING + SPACE + TOTAL));
            degreeDays.append(integerLine1);

            if (degreeFlag.getSeasonCDD().isMeasured()) {
                StringBuilder integerLine2 = new StringBuilder(
                        buildNWWSIntegerLine(degreeFlag.getSeasonCDD(),
                                actualData.getNumCool1Jan(), null,
                                recordData.getNumCool1JanNorm(),
                                ParameterFormatClimate.DUMMY, null,
                                lastYearData.getNumCool1Jan(), null, false,
                                false));

                integerLine2.replace(1, 1 + "Since 1/1".length(), "Since 1/1");
                degreeDays.append(integerLine2);

            }
        }

        if (degreeFlag.getEarlyFreeze().isMeasured()
                || degreeFlag.getLateFreeze().isMeasured()) {
            degreeDays.append("\nFreeze Dates\n");

            if (degreeFlag.getEarlyFreeze().isRecord()
                    || degreeFlag.getLateFreeze().isRecord()) {

                if (degreeFlag.getEarlyFreeze().isRecord()) {
                    degreeDays.append(WordUtils.capitalize(RECORD) + "\n");

                    StringBuilder datesLine1 = emptyLine(
                            ParameterFormatClimate.NUM_LINE1_NWWS + 1);

                    datesLine1.replace(1, 1 + EARLIEST.length(), EARLIEST);

                    if (!recordData.getEarlyFreezeRec().isPartialMissing()) {
                        String dateString = getRecordDateFormat(
                                recordData.getEarlyFreezeRec()
                                        .getCalendarFromClimateDate());
                        datesLine1.replace(periodTabs.getPosValue(),
                                periodTabs.getPosValue() + dateString.length(),
                                dateString);
                        degreeDays.append(datesLine1.toString()).append("\n");
                    } else {
                        datesLine1.replace(periodTabs.getPosValue(),
                                periodTabs.getPosValue() + 2,
                                ParameterFormatClimate.MM);
                        degreeDays.append(datesLine1.toString()).append("\n");
                    }
                }

                if (degreeFlag.getLateFreeze().isRecord()) {
                    if (!degreeFlag.getEarlyFreeze().isRecord()) {
                        degreeDays.append(WordUtils.capitalize(RECORD))
                                .append("\n");
                    }

                    StringBuilder datesLine1 = emptyLine(
                            ParameterFormatClimate.NUM_LINE1_NWWS + 1);

                    datesLine1.replace(1, 1 + LATEST.length(), LATEST);

                    if (!recordData.getLateFreezeRec().isPartialMissing()) {
                        String dateString = getRecordDateFormat(
                                recordData.getLateFreezeRec()
                                        .getCalendarFromClimateDate());
                        datesLine1.replace(periodTabs.getPosValue(),
                                periodTabs.getPosValue() + dateString.length(),
                                dateString);
                        degreeDays.append(datesLine1.toString()).append("\n");
                    } else {
                        datesLine1.replace(periodTabs.getPosValue(),
                                periodTabs.getPosValue() + 2,
                                ParameterFormatClimate.MM);
                        degreeDays.append(datesLine1.toString()).append("\n");
                    }
                }
            }

            if (degreeFlag.getEarlyFreeze().isMeasured()) {

                StringBuilder datesLine1 = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS + 1);

                if ((actualData.getEarlyFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE
                        && actualData.getEarlyFreeze()
                                .getDay() != ParameterFormatClimate.MISSING_DATE)
                        || degreeFlag.getEarlyFreeze().isNorm()) {
                    datesLine1.replace(0, EARLIEST.length(), EARLIEST);
                }

                if (actualData.getEarlyFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE
                        && actualData.getEarlyFreeze()
                                .getDay() != ParameterFormatClimate.MISSING_DATE) {
                    String dateString = getShortDateFormat(actualData
                            .getEarlyFreeze().getCalendarFromClimateDate());
                    datesLine1.replace(periodTabs.getPosValue(),
                            periodTabs.getPosValue() + dateString.length(),
                            dateString);
                }

                if (degreeFlag.getEarlyFreeze().isNorm()) {
                    if (recordData.getEarlyFreezeNorm()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                            && recordData.getEarlyFreezeNorm()
                                    .getDay() != ParameterFormatClimate.MISSING_DATE) {
                        String dateString = getShortDateFormat(
                                recordData.getEarlyFreezeNorm()
                                        .getCalendarFromClimateDate());
                        datesLine1.replace(periodTabs.getPosNorm(),
                                periodTabs.getPosNorm() + dateString.length(),
                                dateString);
                    } else {
                        datesLine1.replace(periodTabs.getPosNorm(),
                                periodTabs.getPosNorm() + 2,
                                ParameterFormatClimate.MM);
                    }
                }

                if (!StringUtils.isWhitespace(datesLine1.toString())) {
                    degreeDays.append(datesLine1.toString());
                }
                if ((actualData.getEarlyFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE
                        && actualData.getEarlyFreeze()
                                .getDay() != ParameterFormatClimate.MISSING_DATE)
                        || degreeFlag.getEarlyFreeze().isNorm()) {
                    degreeDays.append("\n");
                }

            }

            if (degreeFlag.getLateFreeze().isMeasured()) {

                StringBuilder datesLine1 = emptyLine(
                        ParameterFormatClimate.NUM_LINE1_NWWS + 1);

                if ((actualData.getLateFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE
                        && actualData.getLateFreeze()
                                .getDay() != ParameterFormatClimate.MISSING_DATE)
                        || degreeFlag.getLateFreeze().isNorm()) {
                    datesLine1.replace(0, LATEST.length(), LATEST);
                }

                if (actualData.getLateFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE
                        && actualData.getLateFreeze()
                                .getDay() != ParameterFormatClimate.MISSING_DATE) {
                    String dateString = getShortDateFormat(actualData
                            .getLateFreeze().getCalendarFromClimateDate());
                    datesLine1.replace(periodTabs.getPosValue(),
                            periodTabs.getPosValue() + dateString.length(),
                            dateString);
                }

                if (degreeFlag.getLateFreeze().isNorm()) {
                    if (recordData.getLateFreezeNorm()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                            && recordData.getLateFreezeNorm()
                                    .getDay() != ParameterFormatClimate.MISSING_DATE) {
                        String dateString = getShortDateFormat(
                                recordData.getLateFreezeNorm()
                                        .getCalendarFromClimateDate());
                        datesLine1.replace(periodTabs.getPosNorm(),
                                periodTabs.getPosNorm() + dateString.length(),
                                dateString);
                    } else {
                        datesLine1.replace(periodTabs.getPosNorm(),
                                periodTabs.getPosNorm() + 2,
                                ParameterFormatClimate.MM);
                    }
                }
                if (!StringUtils.isWhitespace(datesLine1.toString())) {
                    degreeDays.append(datesLine1.toString());
                }
                if ((actualData.getLateFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE
                        && actualData.getLateFreeze()
                                .getDay() != ParameterFormatClimate.MISSING_DATE)
                        || degreeFlag.getLateFreeze().isNorm()) {

                    degreeDays.append("\n");
                }

            }

        }

        return degreeDays.toString();
    }

    /**
     * Repetition of Highest Wind Line and Highest Gust Line reduced to this
     * method.
     * 
     * @param periodWind
     * @param flag
     * @param maxList
     * @param maxDateList
     * @return
     */
    private StringBuilder windDateHelper(StringBuilder periodWind,
            ClimateProductFlags flag, List<ClimateWind> maxList,
            List<ClimateDate> maxDateList, String field) {
        int speedPos = 31;
        int datePos = 48;

        boolean secondTime = false;

        StringBuilder windLine = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS + 1);

        String windString = WordUtils
                .capitalize(HIGHEST + SPACE + field + SPACE + SPEED) + "/"
                + WordUtils.capitalize(DIRECTION);
        windLine.replace(0, windString.length(), windString);

        if (flag.isTimeOfMeasured()) {
            windLine.replace(42, 42 + DATE.length(),
                    WordUtils.capitalize(DATE));
        }

        if (!maxList.isEmpty()) {
            switch ((int) maxList.get(0).getSpeed()) {
            case ParameterFormatClimate.MISSING:
                windLine.replace(speedPos + 1, speedPos + 3,
                        ParameterFormatClimate.MM);
                windLine.replace(datePos + 1, datePos + 3,
                        ParameterFormatClimate.MM);
                periodWind.append(windLine.toString()).append("\n");
                secondTime = true;
                break;

            default:
                String speedString = String.format(INT_THREE + "/",
                        ClimateUtilities.nint(maxList.get(0).getSpeed()));
                windLine.replace(speedPos, speedPos + speedString.length(),
                        speedString);

                int dirPos = speedPos + speedString.length();

                switch (maxList.get(0).getDir()) {
                case ParameterFormatClimate.MISSING:
                    windLine.replace(dirPos, dirPos + 2,
                            ParameterFormatClimate.MM);
                    break;
                default:
                    String dirString = String.format(THREE_DIGIT_INT,
                            maxList.get(0).getDir());
                    windLine.replace(dirPos, dirPos + dirString.length(),
                            dirString);
                    break;
                }

                if (flag.isTimeOfMeasured()) {
                    if (maxDateList.get(0)
                            .getMon() != ParameterFormatClimate.MISSING
                            && maxDateList.get(0)
                                    .getDay() != ParameterFormatClimate.MISSING) {
                        String dateString = getShortDateFormat(maxDateList
                                .get(0).getCalendarFromClimateDate());
                        windLine.replace(datePos, datePos + dateString.length(),
                                dateString);
                    } else {
                        windLine.replace(datePos + 1, datePos + 3,
                                ParameterFormatClimate.MM);
                    }
                }

                periodWind.append(windLine.toString()).append("\n");

                break;
            }
        }

        for (int j = 1; j < maxList.size() && j < maxDateList.size(); j++) {
            StringBuilder windDateLines = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);

            if (!secondTime) {
                String speedString = "";
                switch ((int) maxList.get(j).getSpeed()) {
                case ParameterFormatClimate.MISSING:
                    windDateLines.replace(speedPos + 1, speedPos + 3,
                            ParameterFormatClimate.MM + "/");
                    break;
                default:
                    speedString = String.format(INT_THREE + "/",
                            ClimateUtilities.nint(maxList.get(j).getSpeed()));
                    windDateLines.replace(speedPos,
                            speedPos + speedString.length(), speedString);
                    break;
                }

                int dirPos = speedPos + speedString.length();
                switch (maxList.get(j).getDir()) {
                case ParameterFormatClimate.MISSING:
                    windDateLines.replace(dirPos, dirPos + 2,
                            ParameterFormatClimate.MM);
                    break;

                default:
                    if (maxList.get(j)
                            .getSpeed() == ParameterFormatClimate.MISSING) {
                        windDateLines.replace(dirPos, dirPos + 2,
                                ParameterFormatClimate.MM);
                        break;
                    }
                    String dirString = String.format(THREE_DIGIT_INT,
                            maxList.get(j).getDir());
                    windDateLines.replace(dirPos, dirPos + dirString.length(),
                            dirString);

                    break;
                }

                if (flag.isTimeOfMeasured()) {
                    if (maxDateList.get(j)
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                            && maxDateList.get(j)
                                    .getDay() != ParameterFormatClimate.MISSING_DATE
                            && maxList.get(j)
                                    .getSpeed() != ParameterFormatClimate.MISSING) {
                        String dateString = getShortDateFormat(maxDateList
                                .get(j).getCalendarFromClimateDate());
                        windDateLines.replace(datePos,
                                datePos + dateString.length(), dateString);
                    } else {
                        windDateLines.replace(datePos + 1, datePos + 3,
                                ParameterFormatClimate.MM);
                    }
                }

                if (maxList.get(j)
                        .getSpeed() != ParameterFormatClimate.MISSING) {
                    periodWind.append(windDateLines.toString()).append("\n");
                }
            }
        }

        return periodWind;
    }

    /**
     * Reduce repetition in buildNWWSPeriodSky()
     * 
     * @param field
     * @param value
     * @param position
     * @return
     */
    private String skyHelper(String field, int value, int position,
            int missingPos) {
        StringBuilder sunLine = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS + 1);

        sunLine.replace(0, field.length(), field);

        switch (value) {
        case ParameterFormatClimate.MISSING:
            sunLine.replace(missingPos, missingPos + 2,
                    ParameterFormatClimate.MM);
            break;
        default:
            sunLine.replace(position,
                    position + String.format(INT_THREE, value).length(),
                    String.format(INT_THREE, value));
            break;
        }

        return sunLine.toString() + "\n";
    }

    /**
     * Migrated from build_NWWS_integer_line.c
     * 
     * <pre>
     * SEPTEMBER 1999 Dan Zipper PRC\TDL
     *
     * THIS IS THE GENERIC ROUTINE THAT WILL BE USED FOR ALL LINES WHERE THE
     * ACTUAL VALUE IS INTEGER NUMBER.
     *
     * 
     * @param valueFlag
     * @param actualValue
     * @param dayActualList
     * @param normalValue
     * @param recordValue
     * @param dayRecordList
     * @param lastYearValue
     * @param lastYearList
     * @param newRecord
     * @return
     */
    private String buildNWWSIntegerLine(ClimateProductFlags valueFlag,
            int actualValue, List<ClimateDate> dayActualList, int normalValue,
            int recordValue, List<ClimateDate> dayRecordList, int lastYearValue,
            List<ClimateDate> lastYearList, boolean newRecord, boolean trace) {
        StringBuilder dateLines = new StringBuilder();

        StringBuilder integerLine1 = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS + 1);

        if (valueFlag.isRecord()) {
            switch (recordValue) {
            case ParameterFormatClimate.MISSING:
            case (-1 * ParameterFormatClimate.MISSING):
                integerLine1.replace(periodTabs.getPosValue() + 4,
                        periodTabs.getPosValue() + 6,
                        ParameterFormatClimate.MM);
                break;
            case ParameterFormatClimate.DUMMY:
                // Do nothing; this cell is blank.
                break;
            case (int) ParameterFormatClimate.TRACE:
                if (trace) {
                    integerLine1.replace(periodTabs.getPosValue() + 5,
                            periodTabs.getPosValue() + 6,
                            ParameterFormatClimate.TRACE_SYMBOL);
                }
                break;
            default:
                integerLine1.replace(periodTabs.getPosValue() + 1,
                        periodTabs.getPosValue() + 1
                                + String.format(INT_FIVE, recordValue).length(),
                        String.format(INT_FIVE, recordValue));
                break;
            }

            if (valueFlag.isRecordYear()) {
                if (dayRecordList != null && !dayRecordList.isEmpty()) {
                    if (dayRecordList.get(0).isPartialMissing()) {
                        integerLine1.replace(periodTabs.getPosActDate() - 1,
                                periodTabs.getPosActDate() + 1,
                                ParameterFormatClimate.MM);
                    } else {
                        String dateString = getRecordDateFormat(dayRecordList
                                .get(0).getCalendarFromClimateDate());

                        integerLine1
                                .replace(periodTabs.getPosActDate() - 1,
                                        periodTabs.getPosActDate() - 1
                                                + dateString.length(),
                                        dateString);

                        dateLines.append(multipleYear(
                                new SimpleDateFormat(RECORD_DATE_FORMAT_STRING),
                                dayRecordList, periodTabs.getPosActDate() - 1));
                    }
                }
            }
        }

        if (valueFlag.isMeasured()) {
            switch (actualValue) {
            case ParameterFormatClimate.MISSING:
            case (-1 * ParameterFormatClimate.MISSING):
                integerLine1.replace(periodTabs.getPosValue() + 4,
                        periodTabs.getPosValue() + 6,
                        ParameterFormatClimate.MM);
                break;
            case ParameterFormatClimate.DUMMY:
                // Do nothing; this cell is blank.
                break;
            case (int) ParameterFormatClimate.TRACE:
                if (trace) {
                    integerLine1.replace(periodTabs.getPosValue() + 5,
                            periodTabs.getPosValue() + 6,
                            ParameterFormatClimate.TRACE_SYMBOL);
                    if (newRecord) {
                        integerLine1.replace(periodTabs.getPosValue() + 6,
                                periodTabs.getPosValue() + 7
                                        + RECORD_SYMBOL.length(),
                                RECORD_SYMBOL);
                    }
                }
                break;
            default:
                if (newRecord) {
                    integerLine1
                            .replace(periodTabs.getPosValue() + 6,
                                    periodTabs.getPosValue() + 6
                                            + RECORD_SYMBOL.length(),
                                    RECORD_SYMBOL);
                }
                integerLine1.replace(periodTabs.getPosValue() + 1,
                        periodTabs.getPosValue() + 1
                                + String.format(INT_FIVE, actualValue).length(),
                        String.format(INT_FIVE, actualValue));
                break;
            }

            if (valueFlag.isTimeOfMeasured() && Math
                    .abs(actualValue) != ParameterFormatClimate.MISSING) {

                if (dayActualList != null && !dayActualList.isEmpty()) {
                    if (dayActualList.get(0).isPartialMissing()) {

                        integerLine1.replace(periodTabs.getPosActDate() - 1,
                                periodTabs.getPosActDate() + 1,
                                ParameterFormatClimate.MM);
                    } else {
                        String dateString = getShortDateFormat(dayActualList
                                .get(0).getCalendarFromClimateDate());

                        integerLine1
                                .replace(periodTabs.getPosActDate() - 1,
                                        periodTabs.getPosActDate() - 1
                                                + dateString.length(),
                                        dateString);
                        dateLines.append(multipleYear(
                                new SimpleDateFormat(SHORT_DATE_FORMAT_STRING),
                                dayActualList, periodTabs.getPosActDate() - 1));
                    }
                }

            }

            if (valueFlag.isNorm()) {
                switch (normalValue) {
                case ParameterFormatClimate.MISSING:
                case (-1 * ParameterFormatClimate.MISSING):
                    integerLine1.replace(periodTabs.getPosNorm() + 5,
                            periodTabs.getPosNorm() + 7,
                            ParameterFormatClimate.MM);
                    break;
                case ParameterFormatClimate.DUMMY:
                    // Do nothing; this cell is blank.
                    break;
                case (int) ParameterFormatClimate.TRACE:
                    if (trace) {
                        integerLine1.replace(periodTabs.getPosNorm() + 6,
                                periodTabs.getPosNorm() + 7,
                                ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    break;
                default:

                    integerLine1.replace(periodTabs.getPosNorm() + 2,
                            periodTabs.getPosNorm() + 2
                                    + String.format(INT_FIVE, normalValue)
                                            .length(),
                            String.format(INT_FIVE, normalValue));
                    break;
                }
            }

            if (valueFlag.isDeparture()) {
                int value1 = actualValue;
                int value2 = normalValue;

                if (value1 != ParameterFormatClimate.DUMMY
                        && value2 != ParameterFormatClimate.DUMMY) {
                    if (Math.abs(value1) == ParameterFormatClimate.MISSING
                            || Math.abs(
                                    value2) == ParameterFormatClimate.MISSING) {

                        integerLine1.replace(periodTabs.getPosDepart() + 5,
                                periodTabs.getPosDepart() + 7,
                                ParameterFormatClimate.MM);
                    } else {

                        if (trace) {
                            if (value1 == (int) ParameterFormatClimate.TRACE) {
                                value1 = 0;
                            }
                            if (value2 == (int) ParameterFormatClimate.TRACE) {
                                value2 = 0;
                            }
                        }
                        int departValue = value1 - value2;

                        integerLine1.replace(periodTabs.getPosDepart() + 2,
                                periodTabs.getPosDepart() + 2
                                        + String.format(INT_FIVE, departValue)
                                                .length(),
                                String.format(INT_FIVE, departValue));
                    }
                }
            }

            if (valueFlag.isLastYear()) {
                switch (lastYearValue) {
                case ParameterFormatClimate.MISSING:
                case (-1 * ParameterFormatClimate.MISSING):
                    integerLine1.replace(periodTabs.getPosLastYr() + 5,
                            periodTabs.getPosLastYr() + 7,
                            ParameterFormatClimate.MM);
                    break;
                case ParameterFormatClimate.DUMMY:
                    // Do nothing; this cell is blank.
                    break;

                default:

                    integerLine1.replace(periodTabs.getPosLastYr() + 2,
                            periodTabs.getPosLastYr() + 2
                                    + String.format(INT_FIVE, lastYearValue)
                                            .length(),
                            String.format(INT_FIVE, lastYearValue));
                    break;
                }

                if (valueFlag.isDateOfLast()) {
                    if (lastYearList != null && !lastYearList.isEmpty()) {
                        if (lastYearList.get(0).isPartialMissing()) {
                            integerLine1.replace(periodTabs.getPosLastDate(),
                                    periodTabs.getPosLastDate() + 2,
                                    ParameterFormatClimate.MM);
                        } else {
                            String dateString = getShortDateFormat(lastYearList
                                    .get(0).getCalendarFromClimateDate());

                            integerLine1
                                    .replace(periodTabs.getPosLastDate(),
                                            periodTabs.getPosLastDate()
                                                    + dateString.length(),
                                            dateString);

                            dateLines.append(multipleYear(
                                    new SimpleDateFormat(
                                            SHORT_DATE_FORMAT_STRING),
                                    lastYearList, periodTabs.getPosLastDate()));
                        }
                    }
                }
            }
        }
        return integerLine1.toString() + "\n" + dateLines.toString();
    }

    /**
     * Migrated from build_NWWS_float_line.c.
     * 
     * <pre>
    *  SEPTEMBER 1999       Dan Zipper       PRC\TDL
    *
    *  THIS IS THE GENERIC ROUTINE THAT WILL BE USED FOR ALL LINES WHERE THE 
    *  ACTUAL VALUE IS A REAL NUMBER.
     *
     * </pre>
     * 
     * @param valueFlag
     * @param actualValue
     * @param dayActualList
     * @param normalValue
     * @param recordValue
     * @param dayRecordList
     * @param lastYearValue
     * @param lastYearList
     * @param newRecord
     * @param decimalPlaces
     * @return
     */
    private String buildNWWSFloatLine(ClimateProductFlags valueFlag,
            float actualValue, ClimateDate actualDate1,
            List<ClimateDates> dayActualList, float normalValue,
            float recordValue, List<ClimateDate> dayRecordList,
            List<ClimateDates> recordDates, float lastYearValue,
            ClimateDate lastYearDate1, List<ClimateDates> lastYearDates,
            boolean newRecord, DecimalType decimalPlaces) {

        StringBuilder dateLines = new StringBuilder();

        StringBuilder floatLine1 = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS + 1);

        if (valueFlag.isRecord()) {
            switch ((int) recordValue) {
            case ParameterFormatClimate.MISSING:
                floatLine1.replace(periodTabs.getPosValue() + 4,
                        periodTabs.getPosValue() + 6,
                        ParameterFormatClimate.MM);
                break;
            case ParameterFormatClimate.DUMMY:
                // Do nothing; this cell is blank.
                break;
            case (int) ParameterFormatClimate.TRACE:
                if (decimalPlaces == DecimalType.TEMP) {
                    String value = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                            recordValue);
                    floatLine1.replace(periodTabs.getPosValue() - 1,
                            periodTabs.getPosValue() - 1 + value.length(),
                            value);
                } else {
                    floatLine1.replace(periodTabs.getPosValue() + 5,
                            periodTabs.getPosValue() + 6,
                            ParameterFormatClimate.TRACE_SYMBOL);
                }
                break;

            default:
                String value;
                if (decimalPlaces == DecimalType.SNOW || decimalPlaces == DecimalType.TEMP) {
                    value = String.format(FLOAT_ONE_DECIMAL_SEVEN, recordValue);

                } else {
                    value = String.format(FLOAT_TWO_DECIMALS_SEVEN,
                            recordValue);

                }
                floatLine1.replace(periodTabs.getPosValue() - 1,
                        periodTabs.getPosValue() - 1 + value.length(), value);
                break;

            }

            if (valueFlag.isRecordYear()) {
                if (dayRecordList != null && !dayRecordList.isEmpty()) {
                    if (dayRecordList.get(0)
                            .getYear() == ParameterFormatClimate.DUMMY) {
                        if (dayRecordList.get(0).isPartialMissing()) {
                            floatLine1.replace(periodTabs.getPosActDate() - 1,
                                    periodTabs.getPosActDate() + 1,
                                    ParameterFormatClimate.MM);
                        } else {
                            String dateString = getShortDateFormat(dayRecordList
                                    .get(0).getCalendarFromClimateDate());

                            floatLine1
                                    .replace(periodTabs.getPosActDate() - 1,
                                            periodTabs.getPosActDate() - 1
                                                    + dateString.length(),
                                            dateString);

                            dateLines.append(multipleYear(
                                    new SimpleDateFormat(
                                            SHORT_DATE_FORMAT_STRING),
                                    dayRecordList,
                                    periodTabs.getPosActDate() - 1));
                        }
                    } else {
                        if (dayRecordList.get(0)
                                .getYear() == ParameterFormatClimate.MISSING) {
                            floatLine1.replace(periodTabs.getPosActDate() - 1,
                                    periodTabs.getPosActDate() + 1,
                                    ParameterFormatClimate.MM);
                        } else {
                            String yearString = String
                                    .valueOf(dayRecordList.get(0).getYear());
                            floatLine1
                                    .replace(periodTabs.getPosActDate() - 1,
                                            periodTabs.getPosActDate() - 1
                                                    + yearString.length(),
                                            yearString);
                            dateLines.append(multipleYear(
                                    new SimpleDateFormat(YEAR_FORMAT_STRING),
                                    dayRecordList,
                                    periodTabs.getPosActDate() - 1));
                        }
                    }
                }
            }

            if (valueFlag.isRecordYear()
                    && recordValue != ParameterFormatClimate.MISSING) {
                if (recordDates != null && !recordDates.isEmpty()) {
                    if (recordDates.get(0).getStart().isPartialMissing()
                            || recordDates.get(0).getEnd().isPartialMissing()) {
                        floatLine1.replace(periodTabs.getPosActDate() - 1,
                                periodTabs.getPosActDate() + 1,
                                ParameterFormatClimate.MM);
                    } else {
                        String datesString = getRecordDateFormat(recordDates
                                .get(0).getStart().getCalendarFromClimateDate())
                                + " to "
                                + getRecordDateFormat(recordDates.get(0)
                                        .getEnd().getCalendarFromClimateDate());

                        floatLine1
                                .replace(periodTabs.getPosActDate() - 1,
                                        periodTabs.getPosActDate() - 1
                                                + datesString.length(),
                                        datesString);
                        dateLines.append(multipleYear(
                                new SimpleDateFormat(RECORD_DATE_FORMAT_STRING),
                                recordDates, periodTabs.getPosActDate() - 1,
                                false));
                    }
                }
            }

        }

        if (valueFlag.isMeasured()) {
            switch ((int) actualValue) {
            case ParameterFormatClimate.MISSING:
                floatLine1.replace(periodTabs.getPosValue() + 4,
                        periodTabs.getPosValue() + 6,
                        ParameterFormatClimate.MM);
                break;
            case ParameterFormatClimate.DUMMY:
                // Do nothing; this cell is blank.
                break;
            case (int) ParameterFormatClimate.TRACE:
                if (decimalPlaces == DecimalType.TEMP) {
                    String value = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                            actualValue);
                    floatLine1.replace(periodTabs.getPosValue() - 1,
                            periodTabs.getPosValue() - 1 + value.length(),
                            value);
                } else {
                    floatLine1.replace(periodTabs.getPosValue() + 5,
                            periodTabs.getPosValue() + 6,
                            ParameterFormatClimate.TRACE_SYMBOL);
                }
                break;

            default:
                String value;
                if (decimalPlaces == DecimalType.SNOW || decimalPlaces == DecimalType.TEMP) {
                    value = String.format(FLOAT_ONE_DECIMAL_SEVEN, actualValue);

                } else {
                    value = String.format(FLOAT_TWO_DECIMALS_SEVEN,
                            actualValue);

                }
                floatLine1.replace(periodTabs.getPosValue() - 1,
                        periodTabs.getPosValue() - 1 + value.length(), value);
                break;

            }

            if (newRecord) {
                floatLine1.replace(periodTabs.getPosValue() + 6,
                        periodTabs.getPosValue() + 6 + RECORD_SYMBOL.length(),
                        RECORD_SYMBOL);
            }

            if (valueFlag.isTimeOfMeasured()
                    && actualValue != ParameterFormatClimate.MISSING) {
                if (actualDate1 != null) {
                    if (actualDate1.isPartialMissing()) {
                        floatLine1.replace(periodTabs.getPosActDate() - 1,
                                periodTabs.getPosActDate() + 1,
                                ParameterFormatClimate.MM);
                    } else {
                        String datesString = getShortDateFormat(
                                actualDate1.getCalendarFromClimateDate());
                        floatLine1
                                .replace(periodTabs.getPosActDate() - 1,
                                        periodTabs.getPosActDate() - 1
                                                + datesString.length(),
                                        datesString);
                    }
                }
            }

            if (valueFlag.isTimeOfMeasured()
                    && actualValue != ParameterFormatClimate.MISSING) {
                if (dayActualList != null && !dayActualList.isEmpty()) {
                    if (dayActualList.get(0).getStart()
                            .getDay() == ParameterFormatClimate.MISSING_DATE
                            || dayActualList.get(0).getStart()
                                    .getMon() == ParameterFormatClimate.MISSING_DATE
                            || dayActualList.get(0).getEnd()
                                    .getDay() == ParameterFormatClimate.MISSING_DATE
                            || dayActualList.get(0).getEnd()
                                    .getMon() == ParameterFormatClimate.MISSING_DATE) {
                        floatLine1.replace(periodTabs.getPosActDate() - 1,
                                periodTabs.getPosActDate() + 1,
                                ParameterFormatClimate.MM);
                    } else {
                        if (dayActualList.get(0).getStartTime()
                                .getHour() != ParameterFormatClimate.DUMMY_DATA
                                && dayActualList.get(0).getEndTime()
                                        .getHour() != ParameterFormatClimate.DUMMY_DATA) {
                            if (dayActualList.get(0).getStartTime()
                                    .getHour() != ParameterFormatClimate.MISSING_DATE
                                    && dayActualList.get(0).getEndTime()
                                            .getHour() != ParameterFormatClimate.MISSING_DATE) {

                                String datesString = getShortDateFormat(
                                        dayActualList.get(0).getStart()
                                                .getCalendarFromClimateDate())
                                        + "("
                                        + dayActualList.get(0).getStartTime()
                                                .getHour()
                                        + ")" + " to "
                                        + getShortDateFormat(
                                                dayActualList.get(0).getEnd()
                                                        .getCalendarFromClimateDate())
                                        + "(" + dayActualList.get(0)
                                                .getEndTime().getHour()
                                        + ")";

                                floatLine1.replace(periodTabs.getPosValue() - 1,
                                        periodTabs.getPosValue() - 1
                                                + datesString.length(),
                                        datesString);

                                dateLines.append(multipleYear(
                                        new SimpleDateFormat(
                                                SHORT_DATE_FORMAT_STRING),
                                        dayActualList,
                                        periodTabs.getPosValue() - 1, true));

                            } else {
                                floatLine1.replace(
                                        periodTabs.getPosActDate() - 1,
                                        periodTabs.getPosActDate() + 1,
                                        ParameterFormatClimate.MM);

                            }
                        } else {
                            String datesString = getShortDateFormat(
                                    dayActualList.get(0).getStart()
                                            .getCalendarFromClimateDate())
                                    + " to "
                                    + getShortDateFormat(dayActualList.get(0)
                                            .getEnd()
                                            .getCalendarFromClimateDate());

                            floatLine1.replace(periodTabs.getPosActDate() - 1,
                                    periodTabs.getPosActDate() - 1
                                            + datesString.length(),
                                    datesString);
                            dateLines.append(multipleYear(
                                    new SimpleDateFormat(
                                            SHORT_DATE_FORMAT_STRING),
                                    dayActualList,
                                    periodTabs.getPosActDate() - 1, false));
                        }
                    }
                }
            }

            if (valueFlag.isNorm()) {
                switch ((int) normalValue) {
                case ParameterFormatClimate.MISSING:
                    floatLine1.replace(periodTabs.getPosNorm() + 5,
                            periodTabs.getPosNorm() + 7,
                            ParameterFormatClimate.MM);
                    break;
                case ParameterFormatClimate.DUMMY:
                    // Do nothing; this cell is blank.
                    break;
                case (int) ParameterFormatClimate.TRACE:
                    if (decimalPlaces == DecimalType.TEMP) {
                        String value = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                                normalValue);
                        floatLine1.replace(periodTabs.getPosNorm(),
                                periodTabs.getPosNorm() + value.length(),
                                value);
                    } else {
                        floatLine1.replace(periodTabs.getPosNorm() + 6,
                                periodTabs.getPosNorm() + 7,
                                ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    break;

                default:
                    String value;
                    if (decimalPlaces == DecimalType.SNOW
                            || decimalPlaces == DecimalType.TEMP) {
                        value = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                                normalValue);

                    } else {
                        value = String.format(FLOAT_TWO_DECIMALS_SEVEN,
                                normalValue);

                    }
                    floatLine1.replace(periodTabs.getPosNorm(),
                            periodTabs.getPosNorm() + value.length(), value);
                    break;

                }
            }

            if (valueFlag.isDeparture()) {
                if (actualValue != ParameterFormatClimate.DUMMY
                        && normalValue != ParameterFormatClimate.DUMMY) {
                    if (actualValue == ParameterFormatClimate.MISSING
                            || normalValue == ParameterFormatClimate.MISSING) {
                        floatLine1.replace(periodTabs.getPosDepart() + 5,
                                periodTabs.getPosDepart() + 7,
                                ParameterFormatClimate.MM);
                    } else {
                        float av = (actualValue == ParameterFormatClimate.TRACE)
                                ? 0 : actualValue;
                        float nv = (normalValue == ParameterFormatClimate.TRACE)
                                ? 0 : normalValue;

                        float departValue = av - nv;
                        String valueString;
                        if (decimalPlaces == DecimalType.SNOW
                                || decimalPlaces == DecimalType.TEMP) {
                            valueString = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                                    departValue);

                        } else {
                            valueString = String.format(
                                    FLOAT_TWO_DECIMALS_SEVEN, departValue);
                        }
                        floatLine1
                                .replace(periodTabs.getPosDepart(),
                                        periodTabs.getPosDepart()
                                                + valueString.length(),
                                        valueString);

                    }
                }
            }

            if (valueFlag.isLastYear()) {
                switch ((int) lastYearValue) {
                case ParameterFormatClimate.MISSING:
                    floatLine1.replace(periodTabs.getPosLastYr() + 5,
                            periodTabs.getPosLastYr() + 7,
                            ParameterFormatClimate.MM);
                    break;
                case ParameterFormatClimate.DUMMY:
                    // Do nothing; this cell is blank.
                    break;
                case (int) ParameterFormatClimate.TRACE:
                    if (decimalPlaces == DecimalType.TEMP) {
                        String value = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                                lastYearValue);
                        floatLine1.replace(periodTabs.getPosLastYr(),
                                periodTabs.getPosLastYr() + value.length(),
                                value);
                    } else {
                        floatLine1.replace(periodTabs.getPosLastYr() + 6,
                                periodTabs.getPosLastYr() + 7,
                                ParameterFormatClimate.TRACE_SYMBOL);
                    }
                    break;

                default:
                    String value;
                    if (decimalPlaces == DecimalType.SNOW
                            || decimalPlaces == DecimalType.TEMP) {
                        value = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                                lastYearValue);

                    } else {
                        value = String.format(FLOAT_TWO_DECIMALS_SEVEN,
                                lastYearValue);

                    }
                    floatLine1.replace(periodTabs.getPosLastYr(),
                            periodTabs.getPosLastYr() + value.length(), value);
                    break;

                }

                if (valueFlag.isDateOfLast() && lastYearDate1 != null) {
                    if (lastYearDate1
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                            || lastYearDate1
                                    .getMon() != ParameterFormatClimate.MISSING_DATE) {
                        floatLine1.replace(periodTabs.getPosLastDate(),
                                periodTabs.getPosLastDate() + 2,
                                ParameterFormatClimate.MM);
                    } else {
                        String dateString = lastYearDate1.getDay() + "/"
                                + lastYearDate1.getMon();

                        floatLine1
                                .replace(periodTabs.getPosLastDate(),
                                        periodTabs.getPosLastDate()
                                                + dateString.length(),
                                        dateString);
                    }
                }

                if (valueFlag.isDateOfLast()
                        && lastYearValue != ParameterFormatClimate.MISSING
                        && lastYearDates != null && !lastYearDates.isEmpty()) {
                    if (lastYearDates.get(0).getStart()
                            .getDay() != ParameterFormatClimate.DUMMY_DATA
                            && lastYearDates.get(0).getStart()
                                    .getMon() != ParameterFormatClimate.DUMMY_DATA
                            && lastYearDates.get(0).getEnd()
                                    .getDay() != ParameterFormatClimate.DUMMY_DATA
                            && lastYearDates.get(0).getEnd()
                                    .getMon() != ParameterFormatClimate.DUMMY_DATA) {

                        if (lastYearDates.get(0).getStart()
                                .getDay() == ParameterFormatClimate.MISSING_DATE
                                || lastYearDates.get(0).getStart()
                                        .getMon() == ParameterFormatClimate.MISSING_DATE
                                || lastYearDates.get(0).getEnd()
                                        .getDay() == ParameterFormatClimate.MISSING_DATE
                                || lastYearDates.get(0).getEnd()
                                        .getMon() == ParameterFormatClimate.MISSING_DATE) {
                            floatLine1.replace(periodTabs.getPosActDate(),
                                    periodTabs.getPosActDate() + 2,
                                    ParameterFormatClimate.MM);
                        } else {
                            if (lastYearDates.get(0).getStartTime()
                                    .getHour() != ParameterFormatClimate.DUMMY_DATA
                                    && lastYearDates.get(0).getEndTime()
                                            .getHour() != ParameterFormatClimate.DUMMY_DATA) {
                                if (lastYearDates.get(0).getStartTime()
                                        .getHour() != ParameterFormatClimate.MISSING_DATE
                                        && lastYearDates.get(0).getEndTime()
                                                .getHour() != ParameterFormatClimate.MISSING_DATE) {

                                    String datesString = getShortDateFormat(
                                            lastYearDates.get(0).getStart()
                                                    .getCalendarFromClimateDate())
                                            + "("
                                            + lastYearDates.get(0)
                                                    .getStartTime().getHour()
                                            + ")" + " to "
                                            + getShortDateFormat(lastYearDates
                                                    .get(0).getEnd()
                                                    .getCalendarFromClimateDate())
                                            + "(" + lastYearDates.get(0)
                                                    .getEndTime().getHour()
                                            + ")";

                                    floatLine1.replace(periodTabs.getPosValue(),
                                            periodTabs.getPosValue()
                                                    + datesString.length(),
                                            datesString);

                                    dateLines.append(multipleYear(
                                            new SimpleDateFormat(
                                                    SHORT_DATE_FORMAT_STRING),
                                            lastYearDates,
                                            periodTabs.getPosValue(), true));

                                } else {
                                    floatLine1.replace(
                                            periodTabs.getPosActDate(),
                                            periodTabs.getPosActDate() + 2,
                                            ParameterFormatClimate.MM);

                                }
                            } else {
                                String datesString = getShortDateFormat(
                                        lastYearDates.get(0).getStart()
                                                .getCalendarFromClimateDate())
                                        + " to "
                                        + getShortDateFormat(lastYearDates
                                                .get(0).getEnd()
                                                .getCalendarFromClimateDate());

                                floatLine1.replace(periodTabs.getPosLastDate(),
                                        periodTabs.getPosLastDate()
                                                + datesString.length(),
                                        datesString);

                                dateLines.append(multipleYear(
                                        new SimpleDateFormat(
                                                SHORT_DATE_FORMAT_STRING),
                                        lastYearDates,
                                        periodTabs.getPosLastDate(), false));
                            }
                        }

                    }
                }
            }
        }

        return floatLine1.toString() + "\n" + dateLines.toString();
    }

    /**
     * Migrated from build_NWWS_thresh_line.c
     * 
     * <pre>
    *  April 2001       Doug Murphy       PRC\MDL
    *
    *  Routine used to format threshold lines of monthly, seasonal, and annual
    *  NWWS reports. The normals and departures are float values, and the observed
    *  and previous year values are integers, causing a hybrid line.
     *
     * </pre>
     * 
     * @param valueFlag
     * @param actualValue
     * @param normalValue
     * @param lastYearValue
     * @return
     */
    private String buildNWWSThreshLine(ClimateProductFlags valueFlag,
            int actualValue, float normalValue, int lastYearValue) {

        StringBuilder threshLine1 = emptyLine(
                ParameterFormatClimate.NUM_LINE1_NWWS + 1);

        if (valueFlag.isMeasured()) {
            if (actualValue == ParameterFormatClimate.MISSING) {
                threshLine1.replace(periodTabs.getPosValue() + 4,
                        periodTabs.getPosValue() + 6,
                        ParameterFormatClimate.MM);
            } else {
                threshLine1.replace(periodTabs.getPosValue() + 1,
                        periodTabs.getPosValue() + 1
                                + String.format(INT_FIVE, actualValue).length(),
                        String.format(INT_FIVE, actualValue));
            }

            if (valueFlag.isNorm()) {
                if (normalValue == ParameterFormatClimate.MISSING) {
                    threshLine1.replace(periodTabs.getPosNorm() + 5,
                            periodTabs.getPosNorm() + 7,
                            ParameterFormatClimate.MM);
                } else {
                    String valueString = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                            normalValue);
                    threshLine1.replace(periodTabs.getPosNorm(),
                            periodTabs.getPosNorm() + valueString.length(),
                            valueString);
                }
            }

            if (valueFlag.isDeparture()) {
                if (actualValue == ParameterFormatClimate.MISSING
                        || normalValue == ParameterFormatClimate.MISSING) {
                    threshLine1.replace(periodTabs.getPosDepart() + 5,
                            periodTabs.getPosDepart() + 7,
                            ParameterFormatClimate.MM);
                } else {
                    float departValue = (float) actualValue - normalValue;
                    String valueString = String.format(FLOAT_ONE_DECIMAL_SEVEN,
                            departValue);
                    threshLine1.replace(periodTabs.getPosDepart(),
                            periodTabs.getPosDepart() + valueString.length(),
                            valueString);

                }
            }

            if (valueFlag.isLastYear()) {
                if (lastYearValue == ParameterFormatClimate.MISSING) {
                    threshLine1.replace(periodTabs.getPosLastYr() + 5,
                            periodTabs.getPosLastYr() + 7,
                            ParameterFormatClimate.MM);
                } else {
                    threshLine1.replace(periodTabs.getPosLastYr() + 2,
                            periodTabs.getPosLastYr() + 2
                                    + String.format(INT_FIVE, lastYearValue)
                                            .length(),
                            String.format(INT_FIVE, lastYearValue));
                }
            }
        }

        return threshLine1.toString() + "\n";
    }

    /**
     * Migrated from multiple_year.c A cell that has multiple years will list
     * the extra years underneath in new lines.
     * 
     * @param dateFormat
     * @param dayList
     * @param position
     * @return
     */
    private String multipleYear(SimpleDateFormat dateFormat,
            List<ClimateDate> dayList, int position) {

        StringBuilder multipleYear = new StringBuilder();

        for (int i = 1; i < dayList.size(); i++) {
            StringBuilder dateLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);
            if (!dayList.get(i).isPartialMissing()) {
                String dateString = dateFormat.format(
                        dayList.get(i).getCalendarFromClimateDate().getTime());

                dateLine.replace(position, position + dateString.length(),
                        dateString);

                multipleYear.append(dateLine).append("\n");
            }
        }

        return multipleYear.toString();
    }

    /**
     * Overload. List multiple years with start/end dates and possible time.
     * 
     * @param dateFormat
     * @param dayList
     * @param position
     * @param time
     * @return
     */
    private String multipleYear(SimpleDateFormat dateFormat,
            List<ClimateDates> dayList, int position, boolean time) {

        StringBuilder multipleYear = new StringBuilder();

        for (int i = 1; i < dayList.size(); i++) {
            StringBuilder dateLine = emptyLine(
                    ParameterFormatClimate.NUM_LINE1_NWWS + 1);
            if (!dayList.get(i).getStart().isPartialMissing()
                    && !dayList.get(i).getEnd().isPartialMissing()) {

                String startHour = time
                        ? "(" + dayList.get(i).getStartTime().getHour() + ")"
                        : "";
                String endHour = time
                        ? "(" + dayList.get(i).getEndTime().getHour() + ")"
                        : "";

                String datesString = dateFormat
                        .format(dayList.get(i).getStart()
                                .getCalendarFromClimateDate().getTime())
                        + startHour + " to "
                        + dateFormat.format(dayList.get(i).getEnd()
                                .getCalendarFromClimateDate().getTime())
                        + endHour;

                dateLine.replace(position, position + datesString.length(),
                        datesString);

                multipleYear.append(dateLine).append("\n");
            }
        }

        return multipleYear.toString();
    }
}

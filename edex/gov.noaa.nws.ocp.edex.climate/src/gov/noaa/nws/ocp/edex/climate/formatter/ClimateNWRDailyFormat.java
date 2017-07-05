/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.WordUtils;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateRecordDay;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.DailyClimateData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimateDailyReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorDailyResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.DegreeDaysControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.PrecipitationControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.RelativeHumidityControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.SnowControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.SunriseNsetControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.TempRecordControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.TemperatureControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.WindControlFlags;

/**
 * Class containing logic for building NWR daily products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2017 21099      wpaintsil   Initial creation
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class ClimateNWRDailyFormat extends ClimateNWRFormat {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateNWRDailyFormat.class);

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateNWRDailyFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        super(currentSettings, globalConfig);

    }

    /**
     * migrated from build_radio.f
     * 
     * <pre>
     *   March 1998     Jason P. Tuell        PRC/TDL
    *   July  1998     Barry N. Baxter       PRC/TDL
    *
    *
    *   Purpose:  This routine is the driver responsible for building the
    *             NWR morning climate summary.
    *
    *   Variables
    *
    *      Input
    *
    *    CLIMATE_STATIONS - derived TYPE which contains the station ids and plain
    *                       language stations names for the stations in this climate
    *                      summary.           
    *         DO_CELSIUS - Flag for reporting temperatures in Celsius
    *                      - TRUE  report temps in Celsius
    *                      - FALSE don't report temps in Celsius
    *        DO_MAX_TEMP - Structure containing the flags which control generation
    *                      of various portions.
    *        DO_MIN_TEMP - Structure containing the flags which control generation
    *                      of various portions.
    *        DO_AVG_TEMP - derived TYPE containing flags for reporting the average
    *                      temperature.
    *      DO_PRECIP_DAY - structure containing the flags which control generation
    *                      of various portions of the liquid precip sentences.
    *                      See the structure definition for 
    details.
    *        DO_SNOW_DAY - structure containing the flags which control generation
    *                      of various portions of the snowfall sentences.
    *                      See the structure definition for more details.
    *  DO_12Z_SNOW_DEPTH - structure containing the flags which control generation
    *                      of various portions of the snow depth sentences.
    *                      See the structure definition for more details.
    *          DO_MAX_RH - structure containg the flags which control generation of
    *                      various portions of the maximum rh sentences.
    *                      See the structure defintion for more details.
    *          DO_MIN_RH - structure containg the flags which control generation of
    *                      various portions of the minimum rh sentences.
    *                      See the structure definition for more details
    *         DO_MEAN_RH - structure containg the flags which control generation of
    *                      various portions of the mean rh sentences.
    *                      See the structure definiton for more details.
    *        DO_MAX_WIND - structure containing the flags which control generation
    *                      of various portions of the maximum wind sentences.
    *                      See the structure definition for more details.
    *        DO_MAX_GUST - structure containing the flags which control generation
    *                      of various portions of the maximum gust wind sentences.
    *                      See the structure definition for more details.
    *     DO_RESULT_WIND - structure containing the flags which control generation
    *                      of various portions of the result wind sentences.
    *                      See the structure definition for more details.
    *           DO_COOL  - structure contaning the flags which control generation of
    *                      various portions of the cooling days sentences.
    *                      See the structure defintion for more details.
    *            DO_HEAT - structure containg the flags which control generation of
    *                      various portions of the heating days sentences.
    *                      See the structure definition for more details
    *           DO_ASTRO - structure containing the flags which control generation
    *                      of various portions of the astro sentences.
    *                      See the structure definition for more details.
    *            DO_NORM - structure containing the flags which control generation
    *                      of various portions of the normal sentences.
    *                      See the structure definition for more details.
    *              ITYPE - flag that determines the type of climate summary
    *                      - 1 morning weather radio daily climate summary
    *                      - 2 evening weather radio daily climate summary
    *                      - 3 nwws morning daily climate summary
    *                      - 4 nwws evening daily climate summary
    *                      - 5 monthly radio climate summary
    *                      - 6 monthly nwws climate summary
    *       NUM_STATIONS - number of stations in this group to be summarized.
    *         NWR_HEADER - structure containing the opening data for the CRS.
    *            SUNRISE - structure containing the valid times for the sunrise
    *                      portion of the astro sentence.
    *             SUNSET - structure containing the valid times for the sunset
    *                      portion of the astro sentence.
    *          T_CLIMATE - Structure containing today climatology.
    *             Y_DATE - date structure containing valid date for the climate
    *                      summary.  See the structure definition for more details
    *          Y_CLIMATE - structure containing historical climatology for a given
    *                      date.  See the structure definition for more details
    *          YESTERDAY - structure containing the observed climate data.
    *                      See the structure definition for more details
    *         VALID_TIME - structure containing the valid time which includes hour,
    *                      minutes, AMPM, and zone.
    *
    *     Output
    *
    *        OUTPUT_FILE - file where data is output.
    *
    *     Local
    *       i                  - loop index
    *
    *     Non-system routines used - none
    *
    *     Non-system functions used - none
    *
    * MODIFICATION HISTORY
    * --------------------   
    * 10/01/99    David T. Miller         removed num and num_stations from the 
    *                                     merge phrases routine arguments list 
    *                                     since they aren't neceesary since the 
    *                                     escape b sequence was moved to merge 
    *                                     header.
    *  6/26/01    Doug Murphy             Sunsets occur after midnight in Alaska -
    *                                     added check to use the second day's
    *                                     sunset time for NWR product.
     * </pre>
     * 
     * @param dailyData
     * @return
     * @throws ClimateInvalidParameterException
     */
    public Map<String, ClimateProduct> buildText(
            ClimateCreatorResponse reportData)
                    throws ClimateInvalidParameterException {
        Map<String, ClimateProduct> prod = new HashMap<>();

        Map<Integer, ClimateDailyReportData> reportMap = ((ClimateCreatorDailyResponse) reportData)
                .getReportMap();

        StringBuilder productText = new StringBuilder();
        StringBuilder headerPhrase = new StringBuilder(buildNWRHeader());
        if (headerPhrase.length() > 0) {
            headerPhrase.append("\n\n");
        }
        // Match station(s) in currentSettings with stations and data in
        // reportMap.
        for (Station station : currentSettings.getStations()) {
            Integer stationId = station.getInformId();

            ClimateDailyReportData report = reportMap.get(stationId);
            if (report == null) {
                logger.warn("The station with informId " + stationId
                        + " defined in the ClimateProductType settings object was not found in the ClimateCreatorResponse report map.");

                continue;
            }

            PeriodType type = currentSettings.getReportType();

            // Put no extra spaces between the phrases if the period type is
            // daily.
            String spaces = type == PeriodType.MONTHLY_RAD
                    || type == PeriodType.SEASONAL_RAD
                    || type == PeriodType.ANNUAL_RAD ? "" : "  ";

            /*
             * Legacy comment: Merge the phrases if there more than 0 characters
             * in each phrase buffer. Add two spaces at the end of each merged
             * phrase to accommodate the first sentence of the next phrase.
             */

            // comment phrases
            String commentPhrase = buildNWRComment(stationId, reportData);
            if (commentPhrase.length() > 0) {
                productText.append(commentPhrase + spaces);
            }

            // temperature phrases
            String tempPhrase = buildNWRTemp(report);
            if (tempPhrase.length() > 0) {
                productText.append(tempPhrase + spaces);
            }

            // precipitation phrases
            String precipPhrase = buildNWRPrecip(report, reportData);
            if (precipPhrase.length() > 0) {
                productText.append(precipPhrase + spaces);
            }

            // degree day phrases
            String heatCoolPhrase = buildNWRHeatAndCool(report);
            if (heatCoolPhrase.length() > 0) {
                productText.append(heatCoolPhrase + spaces);
            }

            // wind phrases
            String windPhrase = buildNWRWind(report);
            if (windPhrase.length() > 0) {
                productText.append(windPhrase + spaces);
            }

            // relative humidity phrases
            String relHumdPhrase = buildNWRRelHumd(report);
            if (relHumdPhrase.length() > 0) {
                productText.append(relHumdPhrase + spaces);
            }

            // normal climatology phrases
            String normPhrase = buildNWRNorms(report);
            if (normPhrase.length() > 0) {
                productText.append(normPhrase + spaces);
            }

            // astronomical phrases
            String astroPhrase = buildNWRAstro(report);
            if (astroPhrase.length() > 0) {
                productText.append(astroPhrase + spaces);
            }

            // add a newline about every 80 characters
            productText = insertNewLines(productText);
            productText.append("\n\n");

        }
        /*
         * Legacy comment: Add the escape b at the end of the product. Required
         * for the NWR/CRS product
         */
        productText.append(ESCAPE_B);
        headerPhrase.append(productText);
        productText = headerPhrase;

        prod.put(getName(), getProduct(productText.toString()));

        return prod;
    }

    /**
     * Migrated from build_NWR_astro.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *  
    *   Purpose:  This routine controls building the astronomical sentences in 
    *             the NOAA Weather Radio (NWR) portion of the climate
    *             program.  Sentence formats are documented in the CLIMATE
    *             Design Notebook.
    * 
    *
    *   Variables
    *
    *      Input
    *        astro_phrase - character buffer containing the astronomical
    *                       sentences
    *            do_astro - structure containing the flags which
    *                       control generation of various portions
    *                       of the astro sentences.  See
    *                       the structure definition for more details.
    *             sunrise - structure containing the valid times for the
    *                             sunrise portion of the astro sentence.
    *              sunset - structure containing the valid times for the
    *                       sunset portion of the astro sentence.
    *
    *      Output
    *        astro_phrase - character buffer containing the astronomical
    *                       sentences
    *      Local
    *           char_hour - character form of the hour number.
    *            char_min - character form of the min number.
    *            num_char - number of characters in a string
    *           num_char1 - number of characters in a string
    *         rise_phrase - character buffer containing the sunrise portion
    *                       of the astronomical sentences
    *          set_phrase - character buffer containing the sunset portion
    *                       of the astronomical sentences
    *
    *      Non-system routines used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in 
    *                       a string; string must be terminated with the NULL
    *                       character.
    *      Non-system functions used
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *   6/27/01     Doug Murphy             Had to add phrasing for locations
    *                                       that do not have a sunrise/sunset
    *                                       every day of the year. Tried to make
    *                                       the code more streamlined in the
    *                                       process.
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRAstro(ClimateDailyReportData reportData) {
        StringBuilder nwrAstro;

        int sunriseIndex = currentSettings
                .getReportType() == PeriodType.MORN_RAD ? 0 : 1;
        int sunsetIndex = reportData.getSunset()[0].getHour() < 12 ? 1 : 0;

        ClimateTime sunrise = reportData.getSunrise()[sunriseIndex];
        ClimateTime sunset = reportData.getSunset()[sunsetIndex];

        ClimateTime convRise = new ClimateTime(sunrise);
        ClimateTime convSet = new ClimateTime(sunset);

        boolean morning = currentSettings.getReportType() == PeriodType.MORN_RAD
                ? true : false;

        SunriseNsetControlFlags astroFlag = currentSettings.getControl()
                .getSunControl();

        int noSunrise = 0;
        int noSunset = 0;

        /**
         * Logic taken from legacy:
         * 
         * <pre>
        *        This section sets the no_sunrise and no_sunset flags.
        *        Possible values and their meaning:
        *          0  The sunrise/sunset exists
        *          1  The sunrise/sunset is missing or is not to be included
        *          2  The sun does not rise/set for this station on this date
        *
        *        Also, the 24hr times are converted to 12hr time.
         * </pre>
         **/
        if (convRise.getHour() == 24 && convRise.getMin() == 24) {
            noSunrise = 2;
        } else {
            convRise = convRise.to12HourTime();

            if (convRise.getHour() < 0 || convRise.getHour() > 12
                    || convRise.getMin() < 0 || convRise.getMin() > 59
                    || !astroFlag.isSunrise()) {
                noSunrise = 1;
            }
        }

        if (convSet.getHour() == 24 && convSet.getMin() == 24) {
            noSunset = 2;
        } else {
            convSet = convSet.to12HourTime();
            if (convSet.getHour() < 0 || convSet.getHour() > 12
                    || convSet.getMin() < 0 || convSet.getMin() > 59
                    || !astroFlag.isSunset()) {
                noSunrise = 1;
            }
        }

        StringBuilder risePhrase = new StringBuilder(),
                setPhrase = new StringBuilder();
        /**
         * Logic taken from legacy:
         * 
         * <pre>
        *        The next section builds the sunrise/sunset phrases based on
        *          1. Is the sunrise/sunset completely missing or not required? 
        *          2. Is this a morning or an evening/intermediate report?
        *                 The morning flag indicates this 1=morning, 2=evening
        *          3. Does a sunset/sunrise occur for this location on this date?
        *          4. If this sunrise/sunset is the second portion of the astro
        *             section (for a morning, this means the sunset, in the
        *             evening, it is the sunrise), was the other portion present,
        *             missing, or non-occurring?
         * </pre>
         */
        if (noSunrise != 1) {
            if (morning) {
                if (noSunrise == 0) {
                    risePhrase = new StringBuilder(
                            WordUtils.capitalize(SUNRISE) + SPACE + TODAY
                                    + " is at " + convRise.toHourMinString()
                                    + SPACE + convRise.getAmpm());
                } else {
                    risePhrase = new StringBuilder("The sun does not rise");
                }
            } else {
                if (noSunrise == 0) {
                    risePhrase = new StringBuilder(
                            WordUtils.capitalize(SUNRISE) + SPACE + TOMORROW
                                    + " is at " + convRise.toHourMinString()
                                    + SPACE + convRise.getAmpm());
                } else {
                    if (noSunset == 0) {
                        risePhrase = new StringBuilder("and there is no "
                                + SUNRISE + SPACE + TOMORROW);
                    } else if (noSunset == 1) {
                        risePhrase = new StringBuilder(
                                "The sun does not rise " + TOMORROW);
                    } else {
                        risePhrase = new StringBuilder("or rise " + TOMORROW);
                    }
                }
            }
        }

        if (noSunset != 1) {
            if (morning) {
                if (noSunset == 0) {
                    if (noSunrise == 1) {
                        setPhrase = new StringBuilder(
                                WordUtils.capitalize(SUNSET) + SPACE + TODAY
                                        + " is at " + convSet.toHourMinString()
                                        + SPACE + convSet.getAmpm());
                    } else {
                        setPhrase = new StringBuilder("and " + SUNSET
                                + " is at " + convSet.toHourMinString() + SPACE
                                + convSet.getAmpm());
                    }
                } else {
                    if (noSunrise == 0) {
                        setPhrase = new StringBuilder(
                                "and does not set tonight");
                    } else if (noSunrise == 1) {
                        setPhrase = new StringBuilder(
                                "The sun does not set today");
                    } else {
                        setPhrase = new StringBuilder("or set");
                    }
                }
            } else {
                if (noSunset == 0) {
                    setPhrase = new StringBuilder(WordUtils.capitalize(SUNSET)
                            + " tonight is at " + convSet.toHourMinString()
                            + SPACE + convSet.getAmpm());
                } else {
                    setPhrase = new StringBuilder(
                            "The sun does not set tonight");
                }
            }
        }

        // join sunrise and sunset phrases

        if (morning) {
            if (noSunrise == 2) {
                if (noSunset == 1) {
                    nwrAstro = new StringBuilder(
                            risePhrase.toString() + SPACE + TODAY);
                } else {
                    nwrAstro = new StringBuilder(risePhrase.toString() + SPACE
                            + setPhrase.toString() + SPACE + TODAY);
                }
            } else if (noSunrise == 1 || noSunset == 1) {
                nwrAstro = new StringBuilder(
                        risePhrase.toString() + setPhrase.toString());
            } else {
                nwrAstro = new StringBuilder(
                        risePhrase.toString() + SPACE + setPhrase.toString());
            }
        } else {
            if (noSunset == 1 && noSunrise == 0) {
                nwrAstro = new StringBuilder(setPhrase.toString() + PERIOD
                        + SPACE + risePhrase.toString());
            } else if (noSunrise == 1 || noSunset == 1) {
                nwrAstro = new StringBuilder(
                        setPhrase.toString() + risePhrase.toString());
            } else {
                nwrAstro = new StringBuilder(
                        setPhrase.toString() + SPACE + risePhrase.toString());
            }
        }

        if (nwrAstro.length() > 0) {
            nwrAstro.append(".");
        }

        return nwrAstro.toString();
    }

    /**
     * Migrated from build_NWR_norms.f
     * 
     * <pre>
     *  
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *   January 2001   Gary Battel           GSC/MDL
    *
    *   Purpose:
    *        This subroutine will be used to reprot the normal mean values for the
    *        current date.  The morning report typically will provide information
    *        for climatic means for the current date.  The mean values for yesterday
    *        may be reported for the following variables:  temperature, and record
    *        temperature.
    * 
    *   Variables
    *
    *      Input
    *         DO_AVG_TEMP - derived TYPE containing flags for reporting
    *                       the average temperature.
    *          DO_CELSIUS - Flag for reporting temperatures in Celsius
    *                        - TRUE  report temps in Celsius
    *                        - FALSE don't report temps in Celsius
    *         DO_MAX_TEMP - Structure containing the flags which control generation
    *                       of various protions.
    *         DO_MIN_TEMP - derived TYPE containing flags for reporting
    *                       the minimum temperature.
    *             MORNING - flag which differntiates morning or evening reports;
    *                        -1 morning report
    *                        -2 evening report
    *                 NUM - Number of stations in this group to be summarized.
    *           Y_CLIMATE - Structure containing yesterday"s climatology.
    *           T_CLIMATE - Structure containing today climatology.
    *              Y_DATE - Structure containing the observed date for yesterday.
    *           YESTERDAY - Structure containing the observed climate data.
    *                       See the structure definition for more details.
    *         NORM_PHRASE - Character buffer containing the mean temperature
    *                       sentences.
    *
    *
    *      Output
    *
    *         NORM_PHRASE - Character buffer containing the mean temperature
    *                       sentences.
    *
    *      Local
    *
    *  C_CLIMATE_MAX_TEMP_MEAN - Character string used to hold a 
    *                            y_climate(num)%max_temp_mean temperature value
    *                            once it has been converted to character from
    *                            numeric format.
    *  C_CLIMATE_MIN_TEMP_MEAN - Character string used to hold a 
    *                            y_climate(num)%min_temp_mean temperature value
    *                            once it has been converted to character from
    *                            numeric format.
    *              C_MEAN_TEMP - Character string used to hold a mean_temp
    *                            value once it has been converted to character
    *                            from numeric format.
    *    CLIMATE_MAX_TEMP_MEAN - It's the celsius value of the climate maximum 
    *                            mean temperature.
    *    CLIMATE_MIN_TEMP_MEAN - It's the celsius value of the climate minimum
    *                            mean temperature.
    *                MEAN_TEMP - It's the average value between yesterday
    *                            climate maximum mean temperature and yesterdays
    *                            climate minimum mean temperature.
    *                 NUM_CHAR - Number of characters in a string.
    *               NUM_DIGITS - The number of digits in a given number;
    *                            necessary for the conversion of numeric
    *                            values to  character.
    *
    *      INCLUDE files
    *
    *    DEFINE_general_phrases.I - Contains character strings need to build all
    *                               types of sentences.
    *     DEFINE_precip_phrases.I - Contains character strings needed to build
    *                               precip sentences.
    *  PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                               arrays, etc.  This INCLUDE file must always
    *                               come first.
    *  TYPE_climate_dates.I - Defines the derived TYPE "climate_dates";
    *                                 note that it uses the dervied TYPE "date" - 
    *                                 the "date" file must be INCLUDED before this
    *                                 one.
    *  TYPE_climate_record_day.I - Defines the derived TYPE used to store the
    *                              historical climatological record for a given
    *                              site for a given day.  Uses derived TYPE
    *                             "wind" so that INCLUDE file must come before
    *                              this one.
    *                TYPE_date.I - Defines the derived TYPE "date";
    *                              This INCLUDE file must always come before any
    *                              other INCLUDE file which uses the "date" TYPE.
    *  TYPE_daily_climate_data.I - Defines the derived TYPE used to store the
    *                              observed climatological data.  Note that
    *                              INCLUDE file defining the wind TYPE must be 
    *                              specified before this file.
    *  TYPE_do_weather_element.I - Defines the derived TYPE used to hold the
    *                              controlling logic for producing
    *                              sentences/reports for a given variable.
    *  TYPE_report_climate_norms.I - Defines the derived TYPE used to hold the
    *                              flags which control reporting the
    *                              climatological norms for different
    *                              meteorological variables.
    *                TYPE_time.I - Defines the derived TYPE used to specify the
    *                              time.
    *                TYPE_wind.I - Defines the derived TYPE used to specify wind
    *                              speed,and direction.
    *
    *      Non-system routines used
    *
    *          build_NWR_celsius - Builds part of sentence that 
    *                                 includes converting f to c.             
    *      CONVERT_CHARACTER_INT - Converts INTEGERS numbers to CHARACTER.
    *
    *      Non-system functions used
    *
    *                PICK_DIGITS - Returns the number of digits in a number.
    *
    *                     STRLEN - C function: returns the number of characters in a
    *                              string; string must be terminated with the NULL
    *                              character.
     * </pre>
     * 
     * @param climateDailyReportData
     * @return
     */
    private String buildNWRNorms(ClimateDailyReportData reportData) {
        StringBuilder nwrNorms = new StringBuilder();

        TempRecordControlFlags normFlag = currentSettings.getControl()
                .getTempRecordControl();
        ClimateRecordDay tClimate = reportData.gettClimate();

        boolean morning = currentSettings.getReportType() == PeriodType.MORN_RAD
                ? true : false;

        if (normFlag.isMaxTempNorm() && tClimate
                .getMaxTempMean() != ParameterFormatClimate.MISSING) {
            if (!morning) {
                nwrNorms.append("The " + NORMAL + " high " + TEMPERATURE
                        + " for " + TOMORROW + " is ");
            } else {
                nwrNorms.append("The " + NORMAL + " high " + TEMPERATURE
                        + " for " + TODAY + " is ");
            }

            nwrNorms.append(
                    ClimateNWRFormat.decideDegree(tClimate.getMaxTempMean()));

            if (currentSettings.getControl().isDoCelsius()) {
                nwrNorms.append(ClimateNWRFormat
                        .buildNWRCelsius(tClimate.getMaxTempMean()));
            }
        }

        if (normFlag.isMinTempNorm() && normFlag.isMaxTempNorm()
                && tClimate.getMinTempMean() != ParameterFormatClimate.MISSING
                && tClimate
                        .getMaxTempMean() != ParameterFormatClimate.MISSING) {
            nwrNorms.append(", and the " + NORMAL + " low is "
                    + tClimate.getMinTempMean());

            if (currentSettings.getControl().isDoCelsius()) {
                nwrNorms.append(ClimateNWRFormat
                        .buildNWRCelsius(tClimate.getMinTempMean()));
            }
        }

        if (normFlag.isMinTempNorm()
                && tClimate.getMinTempMean() != ParameterFormatClimate.MISSING
                && (!normFlag.isMaxTempNorm() || tClimate
                        .getMaxTempMean() == ParameterFormatClimate.MISSING)) {
            if (!morning) {
                nwrNorms.append("The " + NORMAL + " low " + TEMPERATURE
                        + " for " + TOMORROW + " is ");
            } else {
                nwrNorms.append("The " + NORMAL + " low " + TEMPERATURE
                        + " for " + TODAY + " is ");
            }

            nwrNorms.append(tClimate.getMinTempMean());

            if (currentSettings.getControl().isDoCelsius()) {
                nwrNorms.append(ClimateNWRFormat
                        .buildNWRCelsius(tClimate.getMinTempMean()));
            }
        }

        if ((normFlag.isMaxTempNorm() || normFlag.isMinTempNorm()) && (tClimate
                .getMaxTempMean() != ParameterFormatClimate.MISSING
                || tClimate
                        .getMinTempMean() != ParameterFormatClimate.MISSING)) {
            nwrNorms.append(PERIOD + SPACE + SPACE);
        }

        boolean more = false;
        if ((normFlag.isMaxTempRecord() && tClimate
                .getMaxTempRecord() != ParameterFormatClimate.MISSING)
                || (normFlag.isMinTempRecord() && tClimate
                        .getMinTempRecord() != ParameterFormatClimate.MISSING)) {

            if (normFlag.isMaxTempRecord() && tClimate
                    .getMaxTempYear()[0] != ParameterFormatClimate.MISSING) {

                nwrNorms.append("The record high for ");

                if (!morning || currentSettings
                        .getReportType() == PeriodType.INTER_RAD) {
                    nwrNorms.append(TOMORROW + " is ");
                } else {
                    nwrNorms.append(TODAY + " is ");
                }

                nwrNorms.append(tClimate.getMaxTempRecord());
            }

            if (!normFlag.isMaxTempYear()) {
                if (!normFlag.isMinTempRecord()) {
                    nwrNorms.append(".");
                }
            } else {
                if (tClimate
                        .getMaxTempYear()[0] != ParameterFormatClimate.MISSING) {
                    more = false;
                    if (tClimate
                            .getMaxTempYear()[1] != ParameterFormatClimate.MISSING) {
                        more = true;
                    }
                    if (!more) {
                        nwrNorms.append(" which occurred in "
                                + tClimate.getMaxTempYear()[0]);
                    } else {
                        nwrNorms.append(" which last occurred in "
                                + tClimate.getMaxTempYear()[0]);
                    }

                }
            }
        }

        if (normFlag.isMaxTempRecord()
                && tClimate
                        .getMaxTempYear()[0] != ParameterFormatClimate.MISSING
                && normFlag.isMaxTempRecord() && tClimate
                        .getMinTempYear()[0] != ParameterFormatClimate.MISSING) {

            nwrNorms.append(", and the " + RECORD + " low is "
                    + tClimate.getMinTempRecord());

            if (!normFlag.isMinTempYear()) {
                nwrNorms.append(".");
            } else {
                nwrNorms.append(", which occurred in ");

                if (tClimate
                        .getMinTempYear()[0] != ParameterFormatClimate.MISSING) {

                    nwrNorms.append(tClimate.getMinTempYear()[0] + ".");

                }
            }
        }

        if ((!normFlag.isMaxTempRecord()
                || tClimate.getMaxTempRecord() == ParameterFormatClimate.MISSING
                || tClimate
                        .getMaxTempYear()[0] == ParameterFormatClimate.MISSING)
                && normFlag.isMinTempRecord() && tClimate
                        .getMinTempYear()[0] != ParameterFormatClimate.MISSING) {

            nwrNorms.append("The " + RECORD + " low for ");

            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                nwrNorms.append(TOMORROW + " is ");
            } else {
                nwrNorms.append(TODAY + " is ");
            }

            nwrNorms.append(tClimate.getMinTempRecord());

            if (!normFlag.isMaxTempYear()) {
                nwrNorms.append(PERIOD);
            }

            if (normFlag.isMaxTempRecord() && normFlag.isMinTempRecord()) {
                nwrNorms.append(PERIOD);
            }
        }

        return nwrNorms.toString();
    }

    /**
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *
    *   Purpose: 
    *        THIS SUBROUTINE WILL REPORT THE HUMIDITY SEGMENT OF THE
    *        CLIMATOLOGY PROGRAM. THE ROUTINE WILL CHECK FLAGS TO SEE
    *        IF IT SHOULD REPORT BOTH MAXIMUM RELATIVE HUMIDITY AND
    *        MINIMUM RELATIVE HUMIDITY, OR JUST ONE. DEPENDING ON WHICH
    *        FLAGS ARE ON (TRUE) THE SENTENCE WILL READ.... "YESTERDAY'S
    *        MAXIMUM RELATIVE HUMIDITY WAS -- PER CENT AND MINIMUM 
    *        RELATIVE HUMIDITY WAS -- PER CENT. " 
    * 
    *      Input
    *              y_date - date structure containing valid date for
    *                       for the climate summary.  See the structure
    *                       definition for more details
    *           do_max_rh - structure containg the flags which control
    *                       generation of various portions of the
    *                       defintion for more details.
    *           do_min_rh - structure containg the flags which control
    *                       generation of various portions of the
    *                       min_rh sentences. See the structure
    *                       definition for more details
    *          do_mean_rh - structure containg the flags which control
    *                       generation of various portions of the
    *                       rh sentences. See the structure
    *                       definition for more details 
    *             morning - flag which differntiates morning and evening
    *                       reports;
    *                       1 - morning report
    *                       2 - evening report
    *                 num - number of stations in this group to be
    *                       summarized
    *           y_climate - structure containing historical climatology
    *                       for a given date.  See the structure 
    *                       definition for more details
    *           yesterday - structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details
    *           rh_phrase - character buffer containing the relative humidity
    *                       sentences
    *
    *      Output
    *           rh_phrase - character buffer containing the relative humidity
    *                       sentences
    *
    *      Local
    *                c_rh - Character string used to hold the relative 
    *                       humidity value once it has been converted to
    *                       character from numeric format
    *            num_char - Number of characters in a string.
    *          num_digits - The number of digits in a given number; necessary for
    *                       the conversion of numeric values to character.
    *
    *      Non-system routines used
    *  CONVERT_CHARACTER_INT - Converts INTEGERS numbers to CHARACTER.
    *
    *      Non-system functions used
    *
    *         PICK_DIGITS - Returns the number of digits in a number.
    *
    *              STRLEN - C function: returns the number of characters in a
    *                         string; string must be terminated with the NULL
    *                        character.
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRRelHumd(ClimateDailyReportData reportData) {
        StringBuilder nwrRelHumd = new StringBuilder();

        RelativeHumidityControlFlags rhFlag = currentSettings.getControl()
                .getRelHumidityControl();
        DailyClimateData yesterday = reportData.getData();

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        if (rhFlag.getMaxRH().isMeasured() && yesterday
                .getMaxRelHumid() != ParameterFormatClimate.MISSING) {

            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {

                    nwrRelHumd.append("The " + MAXIMUM + SPACE
                            + RELATIVE_HUMIDITY + " was "
                            + yesterday.getMaxRelHumid() + SPACE + PERCENT);
                } else {
                    nwrRelHumd.append(WordUtils.capitalize(TODAY) + "'s "
                            + MAXIMUM + SPACE + RELATIVE_HUMIDITY + " was "
                            + yesterday.getMaxRelHumid() + SPACE + PERCENT);
                }
            } else {
                nwrRelHumd.append(ClimateNWRFormat.timeFrame(morning, true)
                        + "'s maximum relative humidity was "
                        + yesterday.getMaxRelHumid() + SPACE + PERCENT);
            }

            if (rhFlag.getMinRH().isMeasured() && yesterday
                    .getMinRelHumid() != ParameterFormatClimate.MISSING) {

                nwrRelHumd.append(", and the " + MINIMUM + SPACE
                        + RELATIVE_HUMIDITY + " was "
                        + yesterday.getMinRelHumid() + SPACE + PERCENT);
            }
        }

        if ((!rhFlag.getMaxRH().isMeasured()
                || yesterday.getMaxRelHumid() == ParameterFormatClimate.MISSING)
                && (rhFlag.getMinRH().isMeasured() && yesterday
                        .getMinRelHumid() != ParameterFormatClimate.MISSING)) {
            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrRelHumd.append("The " + MINIMUM + SPACE
                            + RELATIVE_HUMIDITY + " was "
                            + yesterday.getMinRelHumid() + SPACE + PERCENT);
                } else {
                    nwrRelHumd.append("Today's " + MINIMUM + SPACE
                            + RELATIVE_HUMIDITY + " was "
                            + yesterday.getMinRelHumid() + SPACE + PERCENT);
                }
            } else {
                nwrRelHumd.append(ClimateNWRFormat.timeFrame(morning, true)
                        + "'s " + MINIMUM + SPACE + RELATIVE_HUMIDITY + " was "
                        + yesterday.getMinRelHumid() + SPACE + PERCENT);
            }
        }

        if ((rhFlag.getMinRH().isMeasured()
                && yesterday.getMinRelHumid() != ParameterFormatClimate.MISSING)
                || (rhFlag.getMaxRH().isMeasured() && yesterday
                        .getMaxRelHumid() != ParameterFormatClimate.MISSING)) {
            nwrRelHumd.append(PERIOD + SPACE + SPACE);
        }

        if (rhFlag.getMeanRH().isMeasured()
                && yesterday.getMinRelHumid() != ParameterFormatClimate.MISSING
                && yesterday.getMaxRelHumid() != ParameterFormatClimate.MISSING
                && yesterday
                        .getMeanRelHumid() != ParameterFormatClimate.MISSING) {
            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrRelHumd.append("The mean " + RELATIVE_HUMIDITY + " was "
                            + yesterday.getMeanRelHumid() + SPACE + PERCENT
                            + PERIOD + SPACE + SPACE);
                } else {
                    nwrRelHumd.append(WordUtils.capitalize(TODAY) + "'s mean "
                            + RELATIVE_HUMIDITY + " was "
                            + yesterday.getMeanRelHumid() + SPACE + PERCENT
                            + PERIOD + SPACE + SPACE);
                }
            } else {
                nwrRelHumd.append(ClimateNWRFormat.timeFrame(morning, true)
                        + "'s mean " + RELATIVE_HUMIDITY + " was "
                        + yesterday.getMeanRelHumid() + SPACE + PERCENT + PERIOD
                        + SPACE + SPACE);
            }

        }

        return nwrRelHumd.toString();
    }

    /**
     * Migrated from build_NWR_wind.f.
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   June  1998     Barry N. Baxter       PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *
    *   Purpose: The purpose of this program is to create the wind portion
    *            of the NOAA weather radio. Sentence structure output is 
    *            documented in the climate design notebook.
    * 
    *        Variables
    *
    *        Input
    *         do_max_wind - structure containing the flags which
    *                       control generation of various portions
    *                       of the wind sentences.  See
    *                       the structure definition for more details.                     
    *         do_max_gust - structure containing the flags which
    *                       control generation of various portions
    *                       of the wind sentences.  See
    *                       the structure definition for more details.
    *      do_result_wind - structure containing the flags which
    *                       control generation of various portions
    *                       of the wind sentences.  See
    *                       the structure definition for more details.
    *             morning - flag which differntiates morning and evening
    *                       reports;
    *                       1 - morning report
    *                       2 - evening report
    *                 num - number of stations in this group to be
    *                       summarized
    *           y_climate - structure containing historical climatology
    *                       for a given date.  See the structure 
    *                       definition for more details
    *              y_date - date structure containing valid date for
    *                       for the climate summary.  See the structure
    *                       definition for more details
    *           yesterday - structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details
    *         wind_phrase - character buffer containing the wind
    *                       sentences structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details
    *        Output
    *         wind_phrase - character buffer containing the wind sentences
    *
    *        Local
    *              c_wind - Character string used to hold the relative 
    *                       humidity value once it has been converted to
    *                       character from numeric format.
    *          idirection - This holds the direction of the wind.
    *          jdirection = This holds the direction of the wind gust.
    *            num_char - Number of characters in a string.
    *           num_char1 - Number of characters for the case result,
    *                       i.e., "West".
    *          num_digits - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *         num_digits1 - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *   MODIFICATION HISTORY
    *   --------------------
    *     2/15/01   Doug Murphy               Wind speeds are now in mph
    *                                         throughout climate... no need to
    *                                         convert from knots
     * </pre>
     * 
     * @param reportData
     * @return
     * @throws ClimateInvalidParameterException
     */
    private String buildNWRWind(ClimateDailyReportData reportData)
            throws ClimateInvalidParameterException {
        StringBuilder nwrWind = new StringBuilder();

        WindControlFlags windFlag = currentSettings.getControl()
                .getWindControl();
        DailyClimateData yesterday = reportData.getData();

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        ClimateTime mTime = yesterday.getMaxWindTime().to12HourTime(),
                gTime = yesterday.getMaxGustTime().to12HourTime();

        int windDir = yesterday.getMaxWind().getDir();

        if (windFlag.getMaxGust().isMeasured() && yesterday.getMaxWind()
                .getSpeed() != ParameterFormatClimate.MISSING) {
            // If wind speed is 0, create the sentence:
            // "The wind was calm <so far today>/<yesterday>/<today>"
            if (yesterday.getMaxWind().getSpeed() == 0) {
                windDir = ParameterFormatClimate.MISSING;

                nwrWind.append("The wind was calm ");

                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrWind.append("so far today. ");
                } else {
                    nwrWind.append(
                            ClimateNWRFormat.timeFrame(morning, false) + ". ");
                }
            } else {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrWind.append("so far today. ");
                } else {
                    nwrWind.append(
                            ClimateNWRFormat.timeFrame(morning, true) + SPACE);
                }
            }

            // explicity create the phase "the maximum wind was less than five
            // miles an hour"
            // if the wind speed is less than 5
            if (yesterday.getMaxWind().getSpeed() < 5
                    && yesterday.getMaxWind().getSpeed() > 0) {
                if (windDir == ParameterFormatClimate.MISSING) {
                    nwrWind.append(
                            "the maximum wind was less than five miles an hour.");
                } else {
                    nwrWind.append(
                            "the maximum wind was less than five miles an hour, and the direction was variable.");

                }
            }

            if (yesterday.getMaxWind().getSpeed() >= 5) {
                int windMph = ClimateUtilities
                        .nint(yesterday.getMaxWind().getSpeed());

                nwrWind.append("the maximum wind observed was " + windMph
                        + " miles an hour");

                if (windDir != ParameterFormatClimate.MISSING) {
                    nwrWind.append(" from the " + ClimateFormat.whichDirection(
                            yesterday.getMaxWind().getDir(), false));
                } else {
                    nwrWind.append(SPACE);
                }

                // "which occurred at <time> am/pm"
                if (windFlag.getMaxWind().isTimeOfMeasured()
                        && mTime.getHour() != ParameterFormatClimate.MISSING_HOUR
                        && mTime.getMin() != ParameterFormatClimate.MISSING_MINUTE) {
                    nwrWind.append(
                            " which occurred at " + mTime.toHourMinString()
                                    + SPACE + mTime.getAmpm());
                }

                nwrWind.append(". ");
            }
        }

        // Max gust sentence:
        // "The highest wind gust observed was <gust> miles per hour from the
        // <direction> which occurred at <time>am/pm."
        if (windFlag.getMaxGust().isMeasured() && yesterday.getMaxGust()
                .getSpeed() != ParameterFormatClimate.MISSING) {
            int gustMph = ClimateUtilities
                    .nint(yesterday.getMaxGust().getSpeed());

            nwrWind.append("The highest wind gust observed was " + gustMph
                    + " miles per hour");

            if (yesterday.getMaxGust()
                    .getDir() != ParameterFormatClimate.MISSING
                    && yesterday.getMaxGust().getSpeed() != 0) {
                nwrWind.append(" from the " + ClimateFormat.whichDirection(
                        yesterday.getMaxGust().getDir(), false));
            }

            if (windFlag.getMaxGust().isTimeOfMeasured()
                    && gTime.getHour() != ParameterFormatClimate.MISSING_HOUR
                    && gTime.getMin() != ParameterFormatClimate.MISSING_MINUTE) {
                nwrWind.append(" which occurred at " + gTime.toHourMinString()
                        + SPACE + gTime.getAmpm());
            }

            nwrWind.append(PERIOD + SPACE + SPACE);
        }

        // Average wind sentence:
        // "<So far today>/<Yesterday>/<Today> the average wind speed was
        // <speed> miles per hour.
        if (windFlag.getMeanWind().isMeasured() && yesterday
                .getAvgWindSpeed() != ParameterFormatClimate.MISSING) {
            if (yesterday.getAvgWindSpeed() < 5) {
                nwrWind.append(
                        "The average wind speed was less than five miles per hour ");

                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrWind.append("so far today. ");
                } else {
                    nwrWind.append(
                            ClimateNWRFormat.timeFrame(morning, false) + ". ");
                }
            } else {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrWind.append("So far today ");
                } else {
                    nwrWind.append(
                            ClimateNWRFormat.timeFrame(morning, true) + SPACE);
                }
                int avgMph = ClimateUtilities.nint(yesterday.getAvgWindSpeed());
                nwrWind.append("the average wind speed was " + avgMph
                        + " miles per hour. ");
            }
        }

        return nwrWind.toString();
    }

    /**
     * Migrated from build_NWR_heat_and_cool.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Barry N. Baxter       PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *
    *   Purpose: 
    *
    *        The purpose of this routine is to build the heating degree days
    *        and cooling degree days part of the climatology. The heating and
    *        cooling degree have the exact same sentence structure. Both are 
    *        included in this subroutine with different output phrases so that it
    *        is possible to use just one part of the routine when the flags
    *        are set such...i.e only use the heating degree days sentences when
    *        only heating degree days sentences are turned on (TRUE).
    * 
    *   Variables
    *
    *      Input
    *               adate - date structure containing valid date for
    *                       for the climate summary.  See the structure
    *                       definition for more details
    *             do_cool - structure containg the flags which control
    *                       generation of various portions of the
    *                       defintion for more details.\
    *      do_cool_report - begin and end dates which determine if cooling
    *                       degree days will be included in report
    *             do_heat - structure containg the flags which control
    *                       generation of various portions of the
    *                       heating days sentences. See the structure
    *                       definition for more details
    *      do_heat_report - begin and end dates which determine if heating
    *                       degree days will be included in report
    *             morning - flag which differntiates morning and evening
    *                       reports;
    *                       1 - morning report
    *                       2 - evening report
    *               itype - flag which indicates morning, intermediate, or
    *                       evening report
    *                 num - number of stations in this group to be
    *                       summarized
    *           y_climate - structure containing historical climatology
    *                       for a given date.  See the structure 
    *                       definition for more details
    *           yesterday - structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details
    * heat_and_cool_phrase - character buffer containing the degree
    *                        day sentences
    *          valid_time - structure containing the valid time which 
    *                       includes hour, minutes, AMPM, and zone.
    *
    *      Output
    * heat_and_cool_phrase - character buffer containing the degree
    *                        day sentences
    *
    *      Local
    *
    *           c_deg_day - Character string used to hold a degree day value 
    *                       once it has been converted to character from numeric 
    *                       format
    *            c_digits - Character string in form of format statement...
    *                       derived from num_digits
    *            c_format - Character string in form of format statement used
    *                       to add comma in values greater than 3 in length
    *         char_output - Character string resulting from adding a comma to 
    *                       c_deg_day in cases of length > 3
    *       delta_deg_day - The difference between the observed degree day total
    *                       and the normal degree day total for the same period.
    *              iwhich - Flag used to index an array containing two different
    *                       words.
    *                       above(iwhich) - iwhich1 = above
    *                                       iwhich2 = below
    *                       bring(iwhich) - iwhich1 = bring
    *                                       iwhich2 = leaves
    *                       to(iwhich)    - iwhich1 = to
    *                                       iwhich2 = at  
    *                       See inline documentation for more details.
    *            num_char - Number of characters in a string.
    *          num_digits - The number of digits in a given number; necessary for
    *                       the conversion of numeric values to character.
    *      INCLUDE files
    *
    *      DEFINE_general_phrases.I - Contains character strings need to build all
    *                                 types of sentences.
    *       DEFINE_precip_phrases.I - Contains character strings needed to build
    *                                 precip sentences.
    *    PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                                 arrays, etc.  This INCLUDE file must always
    *                                 come first.
    *          TYPE_climate_dates.I - Defines the derived TYPE "climate_dates";
    *                                 note that it uses the dervied TYPE "date" - 
    *                                 the "date" file must be INCLUDED before this
    *                                 one.
    *     TYPE_climate_record_day.I - Defines the derived TYPE used to store the
    *                                 historical climatological record for a given
    *                                 site for a given day.  Uses derived TYPE
    *                                 "wind" so that INCLUDE file must come before
    *                                 this one.
    *                   TYPE_date.I - Defines the derived TYPE "date";
    *                                 This INCLUDE file must always come before any
    *                                 other INCLUDE file which uses the "date" TYPE.
    *     TYPE_daily_climate_data.I - Defines the derived TYPE used to store the
    *                                 observed climatological data.  Note that
    *                                 INCLUDE file defining the wind TYPE must be 
    *                                 specified before this file.
    *     TYPE_do_weather_element.I - Defines the derived TYPE used to hold the
    *                                 controlling logic for producing
    *                                 sentences/reports for a given variable.
    *   TYPE_report_climate_norms.I - Defines the derived TYPE used to hold the
    *                                 flags which control reporting the
    *                                 climatological norms for different
    *                                 meteorological variables.
    *                   TYPE_time.I - Defines the derived TYPE used to specify the
    *                                 time.
    *                   TYPE_wind.I - Defines the derived TYPE used to specify wind
    *                                 speed,and direction.
    *
    *      Non-system routines used
    *         CONVERT_CHARACTER_INT - Converts INTEGERS numbers to CHARACTER.
    *
    *      Non-system functions used
    *
    *                   PICK_DIGITS - Returns the number of digits in a number.
    *
    *                        STRLEN - C function: returns the number of characters in a
    *                                 string; string must be terminated with the NULL
    *                                 character.
    *  
    * MODIFICATION HISTORY
    * ====================
    *   4/9/01    Doug Murphy               1. Revised code to make more efficient
    *                                       2. Added code which adds a comma to values
    *                                          greater than 3 digits in length
    *                                       3. Cleaned up function description
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRHeatAndCool(ClimateDailyReportData reportData) {
        StringBuilder nwrHeatAndCool = new StringBuilder();

        DegreeDaysControlFlags degreeFlag = currentSettings.getControl()
                .getDegreeDaysControl();

        DailyClimateData yesterday = reportData.getData();
        ClimateRecordDay yClimate = reportData.getyClimate();

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        boolean coolReport = ClimateFormat
                .reportWindow(currentSettings.getControl().getCoolDates());
        boolean heatReport = ClimateFormat
                .reportWindow(currentSettings.getControl().getHeatDates());

        boolean heatAndCoolOnly = false;
        // If heating degree day flag is true. Build the first part of the
        // sentence: "There were __ heating degree days...
        if (heatReport) {

            if (degreeFlag.getTotalHDD().isMeasured() && yesterday
                    .getNumHeat() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                // "There were no "
                if (yesterday.getNumHeat() == 0) {
                    nwrHeatAndCool.append("There were no ");
                }
                // "There was one "
                else if (yesterday.getNumHeat() == 1) {
                    nwrHeatAndCool
                            .append("There was " + yesterday.getNumHeat());
                }
                // "There were __ "
                else {
                    nwrHeatAndCool
                            .append("There were " + yesterday.getNumHeat());
                }

                // "heating degree day "
                if (yesterday.getNumHeat() == 1) {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        nwrHeatAndCool.append(" heating degree day so far");
                    } else {
                        nwrHeatAndCool.append(" heating degree day "
                                + ClimateNWRFormat.timeFrame(morning, false));
                    }
                }
                // "heating degree days "
                else {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        nwrHeatAndCool.append(" heating degree days so far");
                    } else {
                        nwrHeatAndCool.append(" heating degree days "
                                + ClimateNWRFormat.timeFrame(morning, false));
                    }
                }

                if (degreeFlag.getTotalHDD().isDeparture() && yClimate
                        .getNumHeatMean() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append(", which is ");

                    int deltaDegDay = yesterday.getNumHeat()
                            - yClimate.getNumHeatMean();

                    boolean which;
                    if (deltaDegDay < 0) {
                        deltaDegDay = Math.abs(deltaDegDay);
                        which = false;
                    } else {
                        which = true;
                    }

                    if (deltaDegDay != 0) {
                        nwrHeatAndCool.append(deltaDegDay + SPACE
                                + ClimateNWRFormat.aboveBelow(which) + SPACE);
                    }

                    nwrHeatAndCool.append("normal");
                }

                // add a period if this is the last part of the sentence
                if (degreeFlag.getTotalHDD().isTotalMonth()
                        || degreeFlag.getTotalHDD().isTotalSeason()
                        || degreeFlag.getTotalHDD().isTotalYear()
                        || (degreeFlag.getTotalCDD().isMeasured() && coolReport
                                && degreeFlag.getTotalCDD().isTotalMonth())
                        || degreeFlag.getTotalCDD().isTotalSeason()
                        || degreeFlag.getTotalCDD().isLastYear()
                        || (degreeFlag.getTotalHDD().isMeasured()
                                && (!degreeFlag.getTotalCDD().isMeasured()
                                        || !coolReport))) {
                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

                // Add monthly heating phrase if the monthly heating flag is on
                if (degreeFlag.getTotalHDD().isTotalMonth() && yesterday
                        .getNumHeatMonth() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("This ");

                    // "This leaves/brings the monthly total to/at __ "
                    nwrHeatAndCool.append(ClimateNWRFormat.bringLeave(
                            yesterday.getNumHeat() != 0) + " the monthly total "
                            + ClimateNWRFormat.toAt(yesterday.getNumHeat() != 0)
                            + SPACE + yesterday.getNumHeatMonth());

                    // ",which is <__ above/below>/<empty> normal"
                    if (degreeFlag.getTotalHDD().isDeparture()) {
                        int deltaDegDay = yesterday.getNumHeatMonth()
                                - yClimate.getNumHeatMonth();

                        boolean which;
                        if (deltaDegDay < 0) {
                            which = false;
                            deltaDegDay = Math.abs(deltaDegDay);
                        } else {
                            which = true;
                        }

                        nwrHeatAndCool.append(", which is ");

                        if (deltaDegDay != 0) {
                            nwrHeatAndCool.append(
                                    String.format(INT_COMMAS, deltaDegDay)
                                            + SPACE
                                            + ClimateNWRFormat.aboveBelow(which)
                                            + SPACE);
                        }

                        nwrHeatAndCool.append("normal");
                    }

                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

                // Repeat the above for seasonal heating
                if (degreeFlag.getTotalHDD().isTotalSeason() && yesterday
                        .getNumHeatSeason() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("The seasonal total is "
                            + yesterday.getNumHeatSeason());

                    if (degreeFlag.getTotalHDD().isDeparture() && yClimate
                            .getNumHeatSeason() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                        nwrHeatAndCool.append(", which is ");

                        int deltaDegDay = yesterday.getNumHeatSeason()
                                - yClimate.getNumHeatSeason();

                        boolean which;
                        if (deltaDegDay < 0) {
                            which = false;
                        } else {
                            which = true;
                        }

                        if (deltaDegDay != 0) {
                            nwrHeatAndCool.append(
                                    String.format(INT_COMMAS, deltaDegDay)
                                            + SPACE
                                            + ClimateNWRFormat.aboveBelow(which)
                                            + SPACE);
                        }

                        nwrHeatAndCool.append("normal");

                    }

                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

                // repeat the above for yearly heating
                if (degreeFlag.getTotalHDD().isTotalYear() && yesterday
                        .getNumHeatYear() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append(
                            "The yearly total since July 1 is " + String.format(
                                    INT_COMMAS, yesterday.getNumHeatYear()));

                    if (degreeFlag.getTotalHDD().isDeparture() && yClimate
                            .getNumHeatYear() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                        nwrHeatAndCool.append(", which is ");

                        int deltaDegDay = yesterday.getNumHeatYear()
                                - yClimate.getNumHeatYear();

                        boolean which;
                        if (deltaDegDay < 0) {
                            which = false;
                            deltaDegDay = Math.abs(deltaDegDay);
                        } else {
                            which = true;
                        }

                        if (deltaDegDay != 0) {
                            nwrHeatAndCool.append(
                                    String.format(INT_COMMAS, deltaDegDay)
                                            + SPACE
                                            + ClimateNWRFormat.aboveBelow(which)
                                            + SPACE);
                        }

                        nwrHeatAndCool.append("normal");
                    }

                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }
            }

            // append "and" if month/season/year flags are off
            if (yesterday
                    .getNumCool() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                if (degreeFlag.getTotalCDD().isMeasured() && coolReport
                        && !degreeFlag.getTotalHDD().isTotalMonth()
                        && !degreeFlag.getTotalHDD().isTotalSeason()
                        && !degreeFlag.getTotalHDD().isTotalYear()
                        && !degreeFlag.getTotalCDD().isTotalMonth()
                        && !degreeFlag.getTotalCDD().isTotalSeason()
                        && !degreeFlag.getTotalCDD().isTotalYear()) {
                    nwrHeatAndCool.append(" and ");
                    heatAndCoolOnly = true;
                }
            }
        }

        // repeat everything above for cooling
        if (coolReport) {
            if (degreeFlag.getTotalCDD().isMeasured() && yesterday
                    .getNumCool() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                if (heatAndCoolOnly) {
                    if (yesterday.getNumCool() == 1) {
                        nwrHeatAndCool.append("there was ");
                    } else {
                        nwrHeatAndCool.append("there were ");
                    }
                } else {
                    if (yesterday.getNumCool() == 1) {
                        nwrHeatAndCool.append("There was ");
                    } else {
                        nwrHeatAndCool.append("There were ");
                    }
                }

                if (yesterday.getNumCool() == 0) {
                    nwrHeatAndCool.append("no");
                } else {
                    nwrHeatAndCool.append(yesterday.getNumCool());
                }

                if (yesterday.getNumCool() == 1) {
                    nwrHeatAndCool.append(" cooling degree day ");
                } else {
                    nwrHeatAndCool.append(" cooling degree days ");
                }

                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrHeatAndCool.append("so far");
                } else {
                    nwrHeatAndCool
                            .append(ClimateNWRFormat.timeFrame(morning, false));
                }

                if (degreeFlag.getTotalCDD().isDeparture() && yClimate
                        .getNumCoolMean() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append(", which is ");

                    int deltaDegDay = yesterday.getNumCool()
                            - yClimate.getNumCoolMean();

                    boolean which;
                    if (deltaDegDay < 0) {
                        which = false;
                        deltaDegDay = Math.abs(deltaDegDay);
                    } else {
                        which = true;
                    }

                    if (deltaDegDay != 0) {
                        nwrHeatAndCool.append(deltaDegDay + SPACE
                                + ClimateNWRFormat.aboveBelow(which) + SPACE);
                    }

                    nwrHeatAndCool.append("normal");
                }

                if (degreeFlag.getTotalHDD().isTotalMonth()
                        || degreeFlag.getTotalHDD().isTotalSeason()
                        || degreeFlag.getTotalHDD().isTotalYear()
                        || (degreeFlag.getTotalCDD().isMeasured()
                                && degreeFlag.getTotalCDD().isTotalMonth())
                        || degreeFlag.getTotalCDD().isTotalSeason()
                        || degreeFlag.getTotalCDD().isTotalYear()
                        || (degreeFlag.getTotalHDD().isMeasured() && heatReport
                                && !degreeFlag.getTotalCDD().isMeasured())) {
                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

                if (degreeFlag.getTotalCDD().isTotalMonth() && yesterday
                        .getNumCoolMonth() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                    boolean which = yesterday.getNumCool() == 0 ? false : true;

                    nwrHeatAndCool
                            .append("This " + ClimateNWRFormat.bringLeave(which)
                                    + " the monthly total "
                                    + ClimateNWRFormat.toAt(which) + SPACE
                                    + String.format(INT_COMMAS,
                                            yesterday.getNumCoolMonth()));
                    if (degreeFlag.getTotalCDD().isDeparture() && yClimate
                            .getNumCoolMonth() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                        nwrHeatAndCool.append(", which is ");

                        int deltaDegDay = yesterday.getNumCoolMonth()
                                - yClimate.getNumCoolMonth();

                        if (deltaDegDay < 0) {
                            which = false;
                        } else {
                            which = true;
                        }

                        if (deltaDegDay != 0) {
                            nwrHeatAndCool.append(
                                    String.format(INT_COMMAS, deltaDegDay)
                                            + SPACE
                                            + ClimateNWRFormat.aboveBelow(which)
                                            + SPACE);

                        }

                        nwrHeatAndCool.append("normal");
                    }

                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

                if (degreeFlag.getTotalCDD().isTotalSeason() && yesterday
                        .getNumHeatSeason() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("The seasonal total is " + String
                            .format(INT_COMMAS, yesterday.getNumCoolSeason()));

                    if (degreeFlag.getTotalCDD().isDeparture() && yClimate
                            .getNumCoolSeason() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                        nwrHeatAndCool.append(", which is ");

                        int deltaDegDay = yesterday.getNumCoolSeason()
                                - yClimate.getNumCoolSeason();

                        boolean which;
                        if (deltaDegDay < 0) {
                            which = false;
                        } else {
                            which = true;
                        }

                        if (deltaDegDay != 0) {
                            nwrHeatAndCool.append(
                                    String.format(INT_COMMAS, deltaDegDay)
                                            + SPACE
                                            + ClimateNWRFormat.aboveBelow(which)
                                            + SPACE);

                        }

                        nwrHeatAndCool.append("normal");
                    }
                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

                if (degreeFlag.getTotalCDD().isTotalYear() && yesterday
                        .getNumCoolYear() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("The yearly total since January 1 is "
                            + String.format(INT_COMMAS,
                                    yesterday.getNumCoolYear()));

                    if (degreeFlag.getTotalCDD().isDeparture() && yClimate
                            .getNumCoolYear() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                        nwrHeatAndCool.append(", which is ");
                        int deltaDegDay = yesterday.getNumCoolYear()
                                - yClimate.getNumCoolYear();

                        boolean which;
                        if (deltaDegDay < 0) {
                            which = false;
                            deltaDegDay = Math.abs(deltaDegDay);
                        } else {
                            which = true;
                        }

                        if (deltaDegDay != 0) {
                            nwrHeatAndCool.append(
                                    String.format(INT_COMMAS, deltaDegDay)
                                            + SPACE
                                            + ClimateNWRFormat.aboveBelow(which)
                                            + SPACE);
                        }

                        nwrHeatAndCool.append("normal");
                    }
                    nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
                }

            }

            if (heatAndCoolOnly) {
                nwrHeatAndCool.append(PERIOD + SPACE + SPACE);
            }
        }

        return nwrHeatAndCool.toString();
    }

    /**
     * Migrated from build_NWR_precif.f.
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *
    *   Purpose:  This routine is the main driver for building the
    *             precipitation sentences for the NOAA Weather Radio (NWR).
    *             There are three basi* types of precipitation sentences:
    *             liquid precip (i.e., rainfall) sentences, snowfall
    *             sentences, and depth of snow on the ground sentences.
    * 
    *   Variables
    *
    *      Input
    *              a_date - date structure containing valid date for the climate
    *                       summary.  See the structure definition for more details
    *       do_precip_day - structure containing the flags which control generation
    *                       of various portions of the liquid precip sentences.
    *                       See the structure definition for more details.
    *         do_snow_day - structure containing the flags which control generation
    *                       of various portions of the snowfall sentences.
    *                       See the structure definition for more details.
    *   do_snow_12Z_depth - structure containing the flags which control generation
    *                       of various portions of the snow depth sentences.
    *                       See the structure definition for more details.
    *             morning - flag which differntiates morning and evening reports;
    *                       =1 - morning report
    *                       =2 - evening report
    *            num_stat - number of stations in this group to be summarized
    *           y_climate - structure containing historical climatology for a given
    *                       date.  See the structure definition for more details
    *           yesterday - structure containing the observed climate data.
    *                       See the structure definition for more details
    *
    *      Output
    *       precip_phrase - character buffer containing the precipitation sentences.
    *
    *      Local
    *            big_null - string of null characters used to intialize
    *                       character strings to null
    *
    *      INCLUDE files
    *  PARAMETER_format_climate.I - contains all paramters used to
    *                               dimension arrays, etc.  This 
    *                               INCLUDE file must always come
    *                               first.
    *        TYPE_climate_dates.I - defines the derived TYPE 
    *                               "climate_dates"; note that it 
    *                               uses the dervied TYPE "date" - 
    *                               the "date" file must be INCLUDEd
    *                               before this one
    *   TYPE_climate_record_day.I - defines the derived TYPE used to
    *                               store the historical climatological
    *                               record for a given site for a given
    *                               day.  Uses derived TYPE "wind" so
    *                               that INCLUDE file must come before
    *                               this one
    *                 TYPE_date.I - defines the derived TYPE "date";
    *                               This INCLUDE file must always 
    *                               come before any other INCLUDE file
    *                               which uses the "date" TYPE
    *   TYPE_daily_climate_data.I - defines the derived TYPE used to 
    *                               store the observed climatological
    *                               data.  Note that INCLUDE file
    *                               defining the wind TYPE must be 
    *                               specified before this file
    *   TYPE_do_weather_element.I - defines the derived TYPE used to
    *                               hold the controlling logi* for
    *                               producing sentences/reports for
    *                               a given variable
    * TYPE_report_climate_norms.I - defines the derived TYPE used to
    *                               hold the flags which control 
    *                               reporting the climatological norms
    *                               for different meteorological 
    *                               variables
    *                 TYPE_time.I - defines the derived TYPE used to
    *                               specify the time
    *                 TYPE_wind.I - defines the derived TYPE used to
    *                               specify wind speed,and direction
    *
    *      Non-system routines used
    *     build_NWR_liquid_precip - builds the sentences for rainfall
    *       build_NWR_snow_precip - builds the sentences for snowfall
    *        build_NWR_snow_depth - builds the sentences for snowdepth
    *
    *      Non-system functions used
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRPrecip(ClimateDailyReportData reportData,
            ClimateCreatorResponse report) {
        StringBuilder nwrPrecip = new StringBuilder();

        if (currentSettings.getControl().getPrecipControl().getPrecipTotal()
                .isMeasured()) {
            nwrPrecip.append(buildNWRLiquidPrecip(reportData, report));

        }
        if (currentSettings.getControl().getSnowControl().getSnowTotal()
                .isMeasured()) {
            nwrPrecip.append(buildNWRSnowPrecip(reportData, report));
        }
        if (currentSettings.getControl().getSnowControl().getSnowDepthAvg()
                .isMeasured()) {
            nwrPrecip.append(buildNWRSnowDepth(reportData, report));

        }

        return nwrPrecip.toString();
    }

    /**
     * Migrated from build_NWR_snow_depth.f.
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   April 1998     Dan J. Zipper         PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *   May   1999     Barry N. Baxter       PRC/TDL (Revised)
    *
    *   Purpose: 
    *
    *        This routine is going to construct a sentence that will read 
    *        "There were 3.4 inches of snow on the ground as of 7:00 AM 
    *        EST." It will work if the flag "snow depth" is on (TRUE).
    * 
    *   Variables
    *
    *      Input
    * 
    *              a_date - date structure containing valid date for the climate
    *                       summary.  See the structure definition for more details
    *       do_precip_day - structure containing the flags which control generation
    *                       of various portions of the liquid precip sentences.
    *                       See the structure definition for more details.
    *             morning - flag which differntiates morning and evening reports;
    *                       =1 - morning report
    *                       =2 - evening report
    *                 num - number of stations in this group to be summarized.
    *           y_climate - structure containing historical climatology for a given
    *                       date.  See the structure definition for more details
    *           yesterday - structure containing the observed climate data.
    *                       See the structure definition for more details
    *       precip_phrase - character buffer containing the precipitation sentences.       
    *
    *      Output
    *       precip_phrase - character buffer containing the precipitation sentences.  
    *
    *      Local
    *           char_hour - character form of the hour number.
    *            char_min - character form of the min number.
    *              c_snow - character string used to hold a precipitation value once
    *                       it has been converted to character from numeri* format
    *           ihour_12z - The integer value of the timezone adjustment.
    *                zone - character string used to hold the zone (EST).
    *            num_char - number of characters in a string
    *          num_digits - the number of digits in a given number; necessary for
    *                       the conversion of numeri* values to character
    *        num_decimal  - number of digits to carry beyond the decimal point;
    *                       necessary for the conversion of numeri* values to
    *                       character
    *
    *      Non-system routines used
    *
    * convert_character_real - converts REAL numbers to CHARACTER
    *         pick_digits - returns the number of digits in a number
    *           pick_zone - returns the time-zone adjustment of the valid time.
    *                       For example, EST is 5 hours off of UTC.
    *
    *      Non-system functions used
    *
    *      Special system functions used
    *              strlen - C function; returns the number of characters in a
    *                       string; string must be terminated with the NULL 
    *                       character
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRSnowDepth(ClimateDailyReportData reportData,
            ClimateCreatorResponse report) {
        StringBuilder depthPhrase = new StringBuilder();

        boolean snowReport = reportWindow(
                currentSettings.getControl().getSnowDates());

        DailyClimateData yesterday = reportData.getData();

        /*
         * "There <was/were __ inches of>/<There was no>/<There was a trace of> snow on the ground."
         */
        if (snowReport && yesterday
                .getSnowGround() != ParameterFormatClimate.MISSING) {
            if (yesterday.getSnowGround() == 0) {
                depthPhrase.append("There was no snow on the ground");
            } else if (yesterday
                    .getSnowGround() == ParameterFormatClimate.TRACE) {
                depthPhrase.append("There was a trace of snow on the ground");
            } else if (yesterday.getSnowGround() == 1) {
                depthPhrase.append("There was "
                        + ClimateUtilities.nint(yesterday.getSnowGround())
                        + " inch of snow on the ground");
            } else {
                depthPhrase.append("There were "
                        + ClimateUtilities.nint(yesterday.getSnowGround())
                        + " inches of snow on the ground");
            }
            depthPhrase.append(".");
        }

        return depthPhrase.toString();
    }

    /**
     * Migrated from build_NWR_snow_precip.f.
     * 
     * <pre>
    *    October     Dan Zipper    PRC/TDL
    *
    *   Purpose:  This routine controls building the snowfall sentences in 
    *             the NOAA Weather Radio (NWR) portion of the climate
    *             program.  Sentence formats are documented in the CLIMATE
    *             Design Notebook.
    *
    *             Why do we test on half_snow?
    *               Generally, it is bad coding practice to test on whether
    *               two REAL numbers equal each other.  This is because there
    *               may be small differences between the the numbers which
    *               aren't immediately apparent.  The precision to which we
    *               we measure precipitation is 0.1 inches.  We will test 
    *               to see if two precipitation measurements are within 0.005,
    *               or half_snow of one another.  We will consider such values
    *               as being equal if they are within this. 
    *
    *   Variables
    *
    *      Input
    *              Ma_date - date structure containing valid date for the climate
    *                        summary.  See the structure definition for more details
    *          do_snow_day - structure containing the flags which control generation
    *                        of various portions of the liquid snow sentences.
    *                        See the structure definition for more details.
    *              morning - flag which differntiates morning and evening reports;
    *                        =1 - morning report
    *                        =2 - evening report
    *                  num - number of stations in this group to be summarized
    *            y_climate - structure containing historical climatology for a given
    *                        date.  See the structure definition for more details
    *            yesterday - structure containing the observed climate data. 
    *                        See the structure definition for more details
    *        precip_phrase - character buffer containing the precipitation sentences.
    *
    *      Output
    *        precip_phrase - character buffer containing the precipitation sentences.
    *
    *      Local
    *               c_snow - character string used to hold a snow value once it has
    *                        been converted to character from numeri* format
    *               c_year - CHARACTER representation of INTEGER year
    *          char_output - temp character string resulting from adding a comma
    *           delta_snow - difference between normal accumulated snow and
    *                        observed
    *               iwhich - flag used to index an array containing two different
    *                        words, e.g. above (iwhich=1) or below (iwhich =2).
    *                        See inline documentation for more details
    *                 more - LOGICAL flag which determines if a snow record was set
    *                        in more than one year
    *                        = TRUE, snow record set in multiple years
    *                        = FALSE, snow record set in a single year
    *             num_char - number of characters in a string
    *           num_digits - the number of digits in a given number; necessary for
    *                        the conversion of numeri* values to character
    *          num_decimal - number of digits to carry beyond the decimal point;
    *                        necessary for the conversion of numeri* values to
    *                        character
    *          snow_record - LOGICAL flag which indicates whether a record has
    *                        occurred;
    *                        = TRUE - precipitation record broken or tied
    *                        = FALSE - no record event
    *
    *      Non-system routines used
    *      convert_character_real - converts REAL numbers to CHARACTER
    *                 pick_digits - returns the number of digits in a number
    *
    *      Non-system functions used
    *
    *      Special system functions used
    *                     strlen - * function; returns the number of characters in a
    *                              string; string must be terminated with the NULL
    *                              character
    *
    *     MODIFICATION HISTORY
    *        May 2000   Doug Murphy added constraint to record section:
    *                   in the case of observed 0.00 amount and
    *                   T for record value, the value was 
    *                   previously being denoted as a record
    *
    *        May 2000   Dave Miller added some phrases for the case where
    *                   normal is 0 and a trace fell.  Mostly
    *                   for departure from normal.
    *
    *        November 2000  Doug Murphy     fixed problem in record occurrence
    *                                       code... trace was always reported as
    *                                       a record
    *        4/12/01        Doug Murphy     Added code which adds a comma in cases
    *                                       where a lotta snow occurred (>=1000 in)
    *        4/1/03         Gary Battel     Corrected problem with trace of snow
    *                                       setting records
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRSnowPrecip(ClimateDailyReportData reportData,
            ClimateCreatorResponse report) {
        StringBuilder snowPhrase = new StringBuilder();

        boolean snowReport = reportWindow(
                currentSettings.getControl().getSnowDates());

        DailyClimateData yesterday = reportData.getData();
        ClimateRecordDay yClimate = reportData.getyClimate();

        ClimateDate beginDate = new ClimateDate(report.getBeginDate());

        SnowControlFlags snowFlag = currentSettings.getControl()
                .getSnowControl();

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        boolean which = false, snowRecord = false, month = true;
        float dySnowMonth = 0, deltaSnow = 0;

        /*
         * "<No snow>/<__ inches of snow> <fell>/<has fallen> <yesterday/today>"
         */
        if (snowReport) {
            if (yesterday
                    .getSnowDay() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getSnowDay() == ParameterFormatClimate.TRACE) {
                    if (!morning || currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        if (currentSettings
                                .getReportType() == PeriodType.INTER_RAD) {
                            snowPhrase.append("A trace of snow fell");
                        } else {
                            snowPhrase.append("A trace of snow fell today");
                        }
                    } else {
                        snowPhrase.append("A trace of snow fell yesterday");
                    }
                } else if (Math
                        .abs(yesterday
                                .getSnowDay()) < ParameterFormatClimate.HALF_SNOW
                        && yesterday
                                .getSnowDay() != ParameterFormatClimate.TRACE) {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        snowPhrase.append("No snow has fallen");
                    } else {
                        snowPhrase.append("No snow fell "
                                + ClimateNWRFormat.timeFrame(morning, false));
                    }
                } else {
                    String snowString = String.format(FLOAT_ONE_DECIMAL,
                            yesterday.getSnowDay());
                    if (yesterday.getSnowDay() < 1) {
                        snowString = "0";
                    }

                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        snowPhrase.append(
                                snowString + " inches of snow has fallen");
                    } else {
                        snowPhrase.append(snowString + " inches of snow fell "
                                + ClimateNWRFormat.timeFrame(morning, false));
                    }
                }

                if (snowFlag.getSnowTotal().isRecord() && yClimate
                        .getSnowDayRecord() != ParameterFormatClimate.MISSING) {
                    if (yesterday
                            .getSnowDay() == ParameterFormatClimate.TRACE) {
                        if (yClimate.getSnowDayRecord() == 0) {
                            snowRecord = true;
                            which = true;
                        } else if (yClimate
                                .getSnowDayRecord() == ParameterFormatClimate.TRACE) {
                            snowRecord = true;
                            which = false;
                        }
                    }

                    if (yesterday.getSnowDay() != 0) {
                        deltaSnow = yesterday.getSnowDay()
                                - yClimate.getSnowDayRecord();

                        if (yClimate
                                .getSnowDayRecord() != ParameterFormatClimate.TRACE) {
                            if (deltaSnow >= 0) {
                                snowRecord = true;
                                which = true;
                                if (Math.abs(
                                        deltaSnow) < ParameterFormatClimate.HALF_SNOW) {
                                    which = false;
                                }

                            }
                        } else {
                            snowRecord = true;
                            which = Math.abs(deltaSnow) != 0;
                        }
                    }
                }

                /*
                 * ",which breaks/ties the daily record of <a trace>/<__ inches>"
                 */
                if (snowRecord) {
                    String snowString = String.format(FLOAT_ONE_DECIMAL,
                            yClimate.getSnowDayRecord());

                    if (yClimate.getSnowDayRecord() < 1 && yesterday
                            .getSnowDay() != ParameterFormatClimate.TRACE) {
                        snowString = "0";
                    }

                    if (yClimate
                            .getSnowDayRecord() == ParameterFormatClimate.TRACE) {
                        snowPhrase.append(
                                ", which " + ClimateNWRFormat.breaksTies(which)
                                        + " the daily record of a trace");
                    } else {
                        snowPhrase.append(
                                ", which " + ClimateNWRFormat.breaksTies(which)
                                        + " the daily record of " + snowString
                                        + " inches");
                    }

                    int[] tempYear = new int[] {
                            yClimate.getSnowDayRecordYear()[0],
                            yClimate.getSnowDayRecordYear()[1],
                            yClimate.getSnowDayRecordYear()[2] };
                    if (snowFlag.getSnowTotal().isRecordYear()
                            && (tempYear[0] != ParameterFormatClimate.MISSING
                                    || tempYear[1] != ParameterFormatClimate.MISSING
                                    || tempYear[2] != ParameterFormatClimate.MISSING)) {

                        int yearCount = 0;
                        for (int i = 0; i < tempYear.length; i++) {
                            if (tempYear[i] != ParameterFormatClimate.MISSING) {
                                yearCount++;
                            } else {
                                tempYear[i] = 0;
                            }
                        }

                        int recordYear = Arrays.stream(tempYear).max()
                                .getAsInt();

                        if (yearCount <= 1) {
                            snowPhrase.append(" set in " + recordYear);
                        } else {
                            snowPhrase.append(" last set in " + recordYear);
                        }
                    }
                }

                /*
                 * ",which brings/leaves the monthly total to/at <a trace>/<__ inches>"
                 */
                if (snowFlag.getSnowTotal().isTotalMonth() && yesterday
                        .getSnowMonth() != ParameterFormatClimate.MISSING) {
                    if (yesterday.getSnowMonth() == 0 && yesterday
                            .getSnowDay() == ParameterFormatClimate.TRACE) {
                        yesterday.setSnowMonth(ParameterFormatClimate.TRACE);

                    } else if (yesterday
                            .getSnowMonth() == ParameterFormatClimate.TRACE
                            && (yesterday
                                    .getSnowDay() == ParameterFormatClimate.TRACE
                                    || yesterday.getSnowDay() == 0)) {
                        yesterday.setSnowMonth(ParameterFormatClimate.TRACE);
                    }

                    which = yesterday.getSnowDay() == 0;

                    String snowString = String.format(FLOAT_ONE_DECIMAL,
                            yesterday.getSnowMonth());
                    if (yesterday.getSnowMonth() < 1 && yesterday
                            .getSnowMonth() != ParameterFormatClimate.TRACE) {
                        snowString = "0";
                    }

                    snowPhrase.append(", ");

                    if (!snowRecord) {
                        snowPhrase.append("which ");
                    } else {
                        snowPhrase.append("and ");
                    }

                    if (yesterday
                            .getSnowMonth() == ParameterFormatClimate.TRACE) {
                        snowPhrase.append(ClimateNWRFormat.bringLeave(which)
                                + " the monthly total "
                                + ClimateNWRFormat.toAt(which) + " a trace");
                    } else {
                        snowPhrase.append(ClimateNWRFormat.bringLeave(which)
                                + " the monthly total "
                                + ClimateNWRFormat.toAt(which) + SPACE
                                + snowString + " inches");
                    }
                }

                snowPhrase.append(PERIOD + SPACE + SPACE);

                /*
                 * "This is normal for <month>."
                 */
                if (snowFlag.getSnowTotal().isDeparture()
                        && snowFlag.getSnowTotal().isTotalMonth()
                        && yesterday
                                .getSnowMonth() != ParameterFormatClimate.MISSING
                        && yClimate
                                .getSnowMonthMean() != ParameterFormatClimate.MISSING) {
                    dySnowMonth = yesterday.getSnowMonth();
                    float dycSnowMonthMean = yClimate.getSnowMonthMean();

                    if (yesterday.getSnowMonth() < 0) {
                        dySnowMonth = 0;
                    }
                    if (yClimate.getSnowMonthMean() < 0) {
                        dycSnowMonthMean = 0;
                    }

                    deltaSnow = dySnowMonth - dycSnowMonthMean;

                    if (Math.abs(
                            deltaSnow) < ParameterFormatClimate.HALF_SNOW) {
                        snowPhrase.append("This is normal for "
                                + DateFormatSymbols.getInstance()
                                        .getMonths()[beginDate.getMon() - 1]
                                + ". ");
                    } else {
                        if (deltaSnow < 0) {
                            which = false;
                            deltaSnow = Math.abs(deltaSnow);
                        } else {
                            which = true;
                        }

                        String snowString = String.format(FLOAT_ONE_DECIMAL,
                                deltaSnow);
                        if (deltaSnow < 1) {
                            snowString = "0";
                        }

                        snowPhrase.append("This is " + snowString + " inches "
                                + ClimateNWRFormat.aboveBelow(which) + SPACE);

                        snowString = String.format(FLOAT_ONE_DECIMAL,
                                yClimate.getSnowMonthMean());
                        if (snowFlag.getSnowTotal().isNorm()) {
                            if (yClimate.getSnowMonthMean() < 1) {
                                snowString = "0";
                            }

                            snowPhrase
                                    .append("the normal amount of " + snowString
                                            + " inches for "
                                            + DateFormatSymbols.getInstance()
                                                    .getMonths()[beginDate
                                                            .getMon() - 1]
                                            + ". ");
                        } else {
                            snowPhrase.append("normal. ");
                        }
                    }
                }
            }

            /*
             * "The total snowfall for the month stands at <a trace>/<__
             * inches>, which is <normal>/<__ inches above normal>"
             */
            if (snowFlag.getSnowTotal().isTotalMonth()
                    && yesterday.getSnowDay() == ParameterFormatClimate.MISSING
                    && yesterday
                            .getSnowMonth() != ParameterFormatClimate.MISSING
                    && month) {

                String snowString = String.format(FLOAT_ONE_DECIMAL,
                        dySnowMonth);
                if (yesterday
                        .getPrecipMonth() == ParameterFormatClimate.TRACE) {
                    snowPhrase.append(
                            "The total snowfall for the month stands at a trace");
                } else {

                    if (dySnowMonth < 1) {
                        snowString = "0";
                    }
                    snowPhrase
                            .append("The total snowfall for the month stands at "
                                    + snowString + " inches");
                }

                if (snowFlag.getSnowTotal().isDeparture() && yClimate
                        .getSnowMonthMean() != ParameterFormatClimate.MISSING) {

                    if (Math.abs(
                            deltaSnow) < ParameterFormatClimate.HALF_SNOW) {
                        snowPhrase.append(", which is normal ");
                    } else {
                        if (deltaSnow < 0) {
                            which = false;
                            deltaSnow = Math.abs(deltaSnow);
                        } else {
                            which = true;
                        }

                        snowString = String.format(FLOAT_ONE_DECIMAL,
                                deltaSnow);
                        if (deltaSnow < 1) {
                            snowString = "0";
                        }

                        snowPhrase
                                .append(", which is " + snowString + " inches "
                                        + ClimateNWRFormat.aboveBelow(which)
                                        + " normal ");
                    }

                    snowPhrase.append(".");
                } else {
                    snowPhrase.append(".");
                }
            }

            // Repeat above phrase for season
            if (snowFlag.getSnowTotal().isTotalSeason() && yesterday
                    .getSnowSeason() != ParameterFormatClimate.MISSING) {
                if (yesterday.getSnowSeason() == 0 && yesterday
                        .getSnowDay() == ParameterFormatClimate.TRACE) {
                    yesterday.setSnowSeason(ParameterFormatClimate.TRACE);

                } else if (yesterday
                        .getSnowSeason() == ParameterFormatClimate.TRACE
                        && (yesterday
                                .getSnowDay() == ParameterFormatClimate.TRACE
                                || yesterday.getSnowDay() == 0)) {
                    yesterday.setPrecipSeason(ParameterFormatClimate.TRACE);
                }

                snowPhrase.append(" The total snowfall for the season ");
                if (Math.abs(yesterday
                        .getSnowDay()) < ParameterFormatClimate.HALF_SNOW) {
                    snowPhrase.append("still ");

                } else if (yesterday
                        .getSnowDay() != ParameterFormatClimate.MISSING) {
                    snowPhrase.append("now ");
                }

                String snowString = String.format(FLOAT_COMMAS_ONE_DECIMAL,
                        yesterday.getSnowSeason());
                if (yesterday.getSnowSeason() < 1 && yesterday
                        .getSnowDay() != ParameterFormatClimate.TRACE) {
                    snowString = "0";
                }

                if (yesterday.getSnowSeason() == ParameterFormatClimate.TRACE) {
                    snowPhrase.append("stands at a trace");
                } else {
                    snowPhrase.append("stands at " + snowString + " inches");
                }

                if (snowFlag.getSnowTotal().isDeparture() && yClimate
                        .getSnowSeasonMean() != ParameterFormatClimate.MISSING) {
                    float dySnowSeason = yesterday.getSnowSeason();
                    float dycSnowSeasonMean = yClimate.getSnowSeasonMean();

                    if (yesterday.getSnowSeason() < 0) {
                        dySnowSeason = 0;
                    }
                    if (yClimate.getSnowSeasonMean() < 0) {
                        dycSnowSeasonMean = 0;
                    }
                    deltaSnow = dySnowSeason - dycSnowSeasonMean;

                    if (Math.abs(
                            deltaSnow) < ParameterFormatClimate.HALF_SNOW) {
                        snowPhrase.append(", which is normal");
                    } else {
                        if (deltaSnow < 0) {
                            which = false;
                            deltaSnow = Math.abs(deltaSnow);
                        } else {
                            which = true;
                        }

                        snowString = String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                deltaSnow);
                        if (deltaSnow < 1) {
                            snowString = "0";
                        }

                        snowPhrase
                                .append(", which is " + snowString + " inches "
                                        + ClimateNWRFormat.aboveBelow(which)
                                        + " normal ");
                    }
                }

                if (!snowFlag.getSnowTotal().isTotalYear() || yesterday
                        .getSnowYear() == ParameterFormatClimate.MISSING) {
                    snowPhrase.append(PERIOD + SPACE + SPACE);
                }
            }

            // Repeat above phrase for year
            if (snowFlag.getSnowTotal().isTotalYear() && yesterday
                    .getSnowYear() != ParameterFormatClimate.MISSING) {
                if (yesterday.getSnowYear() == 0 && yesterday
                        .getSnowDay() == ParameterFormatClimate.TRACE) {
                    yesterday.setSnowYear(ParameterFormatClimate.TRACE);
                } else if (yesterday
                        .getSnowYear() == ParameterFormatClimate.TRACE
                        && (yesterday
                                .getSnowDay() == ParameterFormatClimate.TRACE
                                || yesterday.getSnowDay() == 0)) {
                    yesterday.setPrecipYear(ParameterFormatClimate.TRACE);
                }

                if (snowFlag.getSnowTotal().isTotalSeason() && yesterday
                        .getSnowSeason() != ParameterFormatClimate.MISSING) {
                    snowPhrase.append(" and the total snowfall for the year ");
                } else {
                    snowPhrase.append(" The total snow fall for the year ");
                }

                if (snowFlag.getSnowTotal().isTotalSeason() && yesterday
                        .getSnowSeason() != ParameterFormatClimate.MISSING) {
                    if (yesterday.getSnowDay() == 0) {
                        snowPhrase.append("still is ");
                    } else if (yesterday
                            .getSnowDay() == ParameterFormatClimate.MISSING) {
                        snowPhrase.append("is ");
                    } else {
                        snowPhrase.append("now is ");
                    }
                } else {
                    if (yesterday.getSnowDay() == 0) {
                        snowPhrase.append("still stands at ");
                    } else if (yesterday
                            .getSnowDay() == ParameterFormatClimate.MISSING) {
                        snowPhrase.append("stands at ");
                    } else {
                        snowPhrase.append("now stands at ");
                    }
                }

                String snowString = String.format(FLOAT_COMMAS_ONE_DECIMAL,
                        yesterday.getSnowYear());
                if (yesterday.getSnowYear() < 1 && yesterday
                        .getSnowDay() != ParameterFormatClimate.TRACE) {
                    snowString = "0";
                }

                if (yesterday.getSnowYear() == ParameterFormatClimate.TRACE) {
                    snowPhrase.append(A_TRACE);
                } else {
                    snowPhrase.append(snowString + " inches");
                }

                if (snowFlag.getSnowTotal().isDeparture() && yClimate
                        .getSnowYearMean() != ParameterFormatClimate.MISSING) {
                    float dySnowYear = yesterday.getSnowYear();
                    float dycSnowYearMean = yClimate.getSnowYearMean();

                    if (yesterday.getSnowYear() < 0) {
                        dySnowYear = 0;
                    }
                    if (yClimate.getSnowYearMean() < 0) {
                        dycSnowYearMean = 0;
                    }

                    deltaSnow = dySnowYear - dycSnowYearMean;

                    if (Math.abs(
                            deltaSnow) < ParameterFormatClimate.HALF_SNOW) {
                        snowPhrase.append(", which is normal.  ");
                    } else {
                        if (deltaSnow < 0) {
                            which = false;
                            deltaSnow = Math.abs(deltaSnow);
                        } else {
                            which = true;
                        }

                        snowString = String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                deltaSnow);
                        if (deltaSnow < 1) {
                            snowString = "0";
                        }

                        snowPhrase
                                .append(", which is " + snowString + " inches "
                                        + ClimateNWRFormat.aboveBelow(which)
                                        + " normal.  ");
                    }
                } else {
                    snowPhrase.append(PERIOD + SPACE + SPACE);
                }
            }

        }

        return snowPhrase.toString();
    }

    /**
     * Migrated from build_NWR_liquid_precip.f.
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1998     David O. Miller       PRC/TDL
    *   Nov.  2002     Gary Battel           SAIC/MDL
    *
    *   Purpose:  This routine controls building the rainfall sentences in 
    *             the NOAA Weather Radio (NWR) portion of the climate
    *             program.  Sentence formats are documented in the CLIMATE
    *             Design Notebook.
    *
    *             Why do we test on half_precip?
    *               Generally, it is bad coding practice to test on whether
    *               two REAL numbers equal each other.  This is because there
    *               may be small differences between the the numbers which
    *               aren't immediately apparent.  The precision to which we
    *               we measure precipitation is 0.01 inches.  We will test 
    *               to see if two precipitation measurements are within 0.005,
    *               or half_precip of one another.  We will consider such values
    *               as being equal if they are within this.
    * 
    *
    *   Variables
    *
    *      Input
    *              a_date - date structure containing valid date for the climate
    *                       summary.  See the structure definition for more details.
    *       do_precip_day - structure containing the flags which control generation
    *                       of various portions of the liquid precip sentences.
    *                       See the structure definition for more details.
    *             morning - flag which differntiates morning and evening reports;
    *                       =1 - morning report
    *                       =2 - evening report
    *            num_stat - number of stations in this group to be summarized.
    *           y_climate - structure containing historical climatology for a given
    *                       date.  See the structure definition for more details.
    *           yesterday - structure containing the observed climate data.
    *                       See the structure definition for more details.
    *       precip_phrase - character buffer containing the precipitation sentences.
    *
    *      Output
    *       precip_phrase - character buffer containing the precipitation sentences.
    *
    *      Local
    *            c_precip - character string used to hold a precipitation value once
    *                       it has been converted to character from numeri* format.
    *              c_year - CHARACTER representation of INTEGER year.
    *         char_output - temp character string resulting from adding a comma
    *        delta_precip - difference between the observed precip and the 
    *                       normal precip.
    *              iwhich - flag used to index an array containing two different
    *                       words, e.g. above (iwhich=1) or below (iwhich =2).
    *                       See inline documentation for more details.
    *                more - LOGICAL flag which determines if a precip record was set
    *                       in more than one year.
    *                       = TRUE, precip record set in multiple years
    *                       = FALSE precip record set in a single year
    *            num_char - number of characters in a string.
    *          num_digits - the number of digits in a given number; necessary for
    *                       the conversion of numeri* values to character.
    *         num_decimal - number of digits to carry beyond the decimal point;
    *                       necessary for the conversion of numeri* values to
    *                       character.
    *       precip_record - LOGICAL flag which indicates whether a record has
    *                       occurred;
    *                       = TRUE - precipitation record broken or tied
    *                       = FALSE - no record event
    *
    *      INCLUDE files
    *          DEFINE_general_phrases.I - contains character strings
    *                                     need to build all types of
    *                                     sentences
    *           DEFINE_precip_phrases.I - contains character strings
    *                                     needed to build precip sentences
    *        PARAMETER_format_climate.I - contains all paramters used to
    *                                     dimension arrays, etc.  This 
    *                                     INCLUDE file must always come
    *                                     first.
    *              TYPE_climate_dates.I - defines the derived TYPE 
    *                                     "climate_dates"; note that it 
    *                                     uses the dervied TYPE "date" - 
    *                                     the "date" file must be INCLUDEd
    *                                     before this one
    *         TYPE_climate_record_day.I - defines the derived TYPE used to
    *                                     store the historical climatological
    *                                     record for a given site for a given
    *                                     day.  Uses derived TYPE "wind" so
    *                                     that INCLUDE file must come before
    *                                     this one
    *                       TYPE_date.I - defines the derived TYPE "date";
    *                                     This INCLUDE file must always 
    *                                     come before any other INCLUDE file
    *                                     which uses the "date" TYPE
    *         TYPE_daily_climate_data.I - defines the derived TYPE used to 
    *                                     store the observed climatological
    *                                     data.  Note that INCLUDE file
    *                                     defining the wind TYPE must be 
    *                                     specified before this file
    *         TYPE_do_weather_element.I - defines the derived TYPE used to
    *                                     hold the controlling logi* for
    *                                     producing sentences/reports for
    *                                     a given variable
    *       TYPE_report_climate_norms.I - defines the derived TYPE used to
    *                                     hold the flags which control 
    *                                     reporting the climatological norms
    *                                     for different meteorological 
    *                                     variables
    *                       TYPE_time.I - defines the derived TYPE used to
    *                                     specify the time
    *               TYPE_station_list.I - defines the derived TYPE used to
    *                                     hold station information for
    *                                     the climate summary
    *                      TYPE_wind.I  - defines the derived TYPE used to
    *                                     specify wind speed,and direction
    *
    *      Non-system routines used
    *            convert_character_real - converts REAL numbers to CHARACTER
    *                       pick_digits - returns the number of digits in a 
    *                                     number
    *
    *      Non-system functions used - none
    *
    *      Special system functions used
    *                            strlen - C function; returns the number of 
    *                                     characters in a string; string must
    *                                     be terminated with the NULL
    *                                     character
    *
    *     MODIFICATION HISTORY
    *        May 2000   Doug Murphy added constraint to record section:
    *                   in the case of observed 0.00 amount and
    *                   T for record value, the value was 
    *                   previously being denoted as a record
    *        May 2000       Dave Miller     Added some phrases for the case where
    *                                       normal is 0 but a trace fell.
    *        Aug. 29, 2000  Doug Murphy     Fixed monthly total section when
    *                                       observed value is missing. Added check
    *                                       for trace which was suspiciously
    *                                       missing. NOTE: would like to look
    *                                       into making this code more
    *                                       streamlined at a later date
    *        Jan. 22, 2001  Doug Murphy     Fixed a punctuation problem between
    *                                       season-to-date and year-to-date
    *                                       sentences. Also, while here fixed some
    *                                       spacing issues.
    *        4/12/01        Doug Murphy     Added code which adds a comma in cases
    *                                       where a lotta rain occurred (>=1000 in)
    *        11/7/02        Gary Battel     Changed wording from inch to inches 
    *                                       whenever a REAL value of 1.00 is 
    *                                       encountered.
     * </pre>
     * 
     * @param reportData
     * 
     * @return
     */
    private String buildNWRLiquidPrecip(ClimateDailyReportData reportData,
            ClimateCreatorResponse report) {
        StringBuilder liquidPhrase = new StringBuilder();

        DailyClimateData yesterday = reportData.getData();
        ClimateRecordDay yClimate = reportData.getyClimate();

        ClimateDate beginDate = new ClimateDate(report.getBeginDate());

        PrecipitationControlFlags precipFlag = currentSettings.getControl()
                .getPrecipControl();

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        boolean monthTotalPhrase = false;
        boolean precipRecord = false, which = false;

        /*
         * "No/<precip>/<A trace of> precipitation <fell>/<has fallen>
         * <yesterday/today>
         */
        if (yesterday.getPrecip() != ParameterFormatClimate.MISSING_PRECIP) {

            if (yesterday.getPrecip() == ParameterFormatClimate.TRACE) {

                if (!morning || currentSettings
                        .getReportType() == PeriodType.INTER_RAD) {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        liquidPhrase
                                .append("A trace of precipitation has fallen");
                    } else {
                        liquidPhrase
                                .append("A trace of precipitation fell today");
                    }
                } else {
                    liquidPhrase
                            .append("A trace of precipitation fell yesterday");
                }
            } else if (Math
                    .abs(yesterday
                            .getPrecip()) < ParameterFormatClimate.HALF_PRECIP
                    && yesterday.getPrecip() != ParameterFormatClimate.TRACE) {

                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    liquidPhrase.append("No precipitation has fallen");
                } else {
                    liquidPhrase.append("No precipitation fell "
                            + ClimateNWRFormat.timeFrame(morning, false));
                }
            } else {
                String precipString = String.format(FLOAT_TWO_DECIMALS1,
                        yesterday.getPrecip());
                if (yesterday.getPrecip() < 1) {
                    precipString = "0";
                }
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    liquidPhrase.append(precipString
                            + " inches of precipitation has fallen");
                } else {
                    liquidPhrase.append(precipString
                            + " inches of  precipitation fell "
                            + ClimateNWRFormat.timeFrame(morning, false));
                }
            }

            if (precipFlag.getPrecipTotal().isRecord() && yClimate
                    .getPrecipDayRecord() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getPrecip() == ParameterFormatClimate.TRACE) {
                    if (yClimate.getPrecipDayRecord() == 0) {
                        precipRecord = true;
                        which = true;
                    } else if (yClimate
                            .getPrecipDayRecord() == ParameterFormatClimate.TRACE) {
                        precipRecord = true;
                        which = false;
                    }
                }
                if (yesterday.getPrecip() == 0) {
                    float deltaPrecip = yesterday.getPrecip()
                            - yClimate.getPrecipDayRecord();

                    if (yClimate
                            .getPrecipDayRecord() != ParameterFormatClimate.TRACE) {
                        if (deltaPrecip >= 0) {
                            precipRecord = true;
                            which = Math
                                    .abs(deltaPrecip) < ParameterFormatClimate.HALF_PRECIP
                                            ? false : true;
                        }
                    } else {
                        precipRecord = true;
                        which = Math
                                .abs(deltaPrecip) < ParameterFormatClimate.HALF_PRECIP
                                        ? false : true;
                    }
                }
            }

            if (precipRecord) {
                String precipString = String.format(FLOAT_TWO_DECIMALS1,
                        yClimate.getPrecipDayRecord());
                if (yClimate.getPrecipDayRecord() < 1 && yesterday
                        .getPrecip() != ParameterFormatClimate.TRACE) {
                    precipString = "0";
                }

                if (yClimate
                        .getPrecipDayRecord() == ParameterFormatClimate.TRACE) {
                    liquidPhrase.append(
                            " which " + ClimateNWRFormat.breaksTies(which)
                                    + " the daily record of a trace");
                } else {
                    liquidPhrase.append(
                            ", which " + ClimateNWRFormat.breaksTies(which)
                                    + " the daily record of " + precipString
                                    + " inches");
                }

                int[] tempYear = new int[] {
                        yClimate.getPrecipDayRecordYear()[0],
                        yClimate.getPrecipDayRecordYear()[1],
                        yClimate.getPrecipDayRecordYear()[2] };
                if (precipFlag.getPrecipTotal().isRecordYear()
                        && (tempYear[0] != ParameterFormatClimate.MISSING
                                || tempYear[1] != ParameterFormatClimate.MISSING
                                || tempYear[2] != ParameterFormatClimate.MISSING)) {

                    int yearCount = 0;
                    for (int i = 0; i < tempYear.length; i++) {
                        if (tempYear[i] != ParameterFormatClimate.MISSING) {
                            yearCount++;
                        } else {
                            tempYear[i] = 0;
                        }
                    }

                    int recordYear = Arrays.stream(tempYear).max().getAsInt();

                    if (yearCount <= 1) {
                        liquidPhrase.append(" set in " + recordYear);
                    } else {
                        liquidPhrase.append(" last set in " + recordYear);
                    }
                }
            }

            if (precipFlag.getPrecipTotal().isTotalMonth() && yesterday
                    .getPrecipMonth() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getPrecipMonth() == 0 && yesterday
                        .getPrecip() == ParameterFormatClimate.TRACE) {
                    yesterday.setPrecipMonth(ParameterFormatClimate.TRACE);
                }

                which = yesterday.getPrecip() != 0;

                String precipString = String.format(FLOAT_TWO_DECIMALS1,
                        yesterday.getPrecipMonth());
                if (yesterday.getPrecipMonth() < 1 && yesterday
                        .getPrecipMonth() != ParameterFormatClimate.TRACE) {
                    precipString = "0";
                }

                liquidPhrase.append(", ");

                if (!precipRecord) {
                    liquidPhrase.append("which ");
                } else {
                    liquidPhrase.append("and ");
                }

                if (yesterday
                        .getPrecipMonth() == ParameterFormatClimate.TRACE) {
                    liquidPhrase.append(ClimateNWRFormat.bringLeave(which)
                            + " the monthly total "
                            + ClimateNWRFormat.toAt(which) + " a trace");
                } else {
                    liquidPhrase.append(ClimateNWRFormat.bringLeave(which)
                            + " the monthly total "
                            + ClimateNWRFormat.toAt(which) + SPACE
                            + precipString + " inches");
                }
            }

            liquidPhrase.append(PERIOD + SPACE + SPACE);

            if (precipFlag.getPrecipTotal().isDeparture()
                    && precipFlag.getPrecipTotal().isTotalMonth()
                    && yesterday
                            .getPrecipMonth() != ParameterFormatClimate.MISSING_PRECIP
                    && yClimate
                            .getPrecipMonthMean() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getPrecipMonth() == ParameterFormatClimate.TRACE
                        && yClimate
                                .getPrecipMonthMean() != ParameterFormatClimate.TRACE
                        && yClimate.getPrecipMonthMean() != 0) {
                    yesterday.setPrecipMonth(0);
                }

                float deltaPrecip = yesterday.getPrecipMonth()
                        - yClimate.getPrecipMonthMean();

                if (Math.abs(
                        deltaPrecip) < ParameterFormatClimate.HALF_PRECIP) {
                    liquidPhrase.append("This is normal for "
                            + DateFormatSymbols.getInstance()
                                    .getMonths()[beginDate.getMon() - 1]
                            + PERIOD + SPACE + SPACE);
                } else if (yesterday
                        .getPrecipMonth() == ParameterFormatClimate.TRACE
                        && yClimate.getPrecipMonthMean() == 0) {
                    liquidPhrase.append("The normal precipitation for "
                            + DateFormatSymbols.getInstance()
                                    .getMonths()[beginDate.getMon() - 1]
                            + " is zero inches.  ");
                } else {
                    if (deltaPrecip < 0) {
                        which = false;
                        deltaPrecip = Math.abs(deltaPrecip);
                    } else {
                        which = true;
                    }

                    String precipString = String.format(FLOAT_TWO_DECIMALS1,
                            deltaPrecip);

                    if (deltaPrecip < 1) {
                        precipString = "0";
                    }

                    liquidPhrase.append("This is " + precipString + " inches "
                            + ClimateNWRFormat.aboveBelow(which));

                    if (precipFlag.getPrecipTotal().isNorm()) {
                        precipString = String.format(FLOAT_TWO_DECIMALS1,
                                yClimate.getPrecipMonthMean());
                        if (yClimate.getPrecipMonthMean() < 1) {
                            precipString = "0";
                        }

                        liquidPhrase.append("the normal amount " + precipString
                                + " inches for "
                                + DateFormatSymbols.getInstance()
                                        .getMonths()[beginDate.getMon() - 1]
                                + PERIOD + SPACE + SPACE);
                    } else {
                        liquidPhrase.append("normal.  ");
                    }
                }
            }

        } else {
            monthTotalPhrase = true;
        }

        /*
         * Departure phrase: "The total precipitation for the month/season/year
         * now/still stands at __
         */
        if (precipFlag.getPrecipTotal().isTotalMonth() && monthTotalPhrase
                && yesterday
                        .getPrecipMonth() != ParameterFormatClimate.MISSING_PRECIP) {
            String precipString = String.format(FLOAT_TWO_DECIMALS1,
                    yesterday.getPrecipMonth());

            if (yesterday.getPrecipMonth() < 1 && yesterday
                    .getPrecipMonth() != ParameterFormatClimate.TRACE) {
                precipString = "0";
            }

            if (yesterday.getPrecipMonth() == ParameterFormatClimate.TRACE) {
                liquidPhrase.append(
                        "The total precipitation for the month stands at a trace");
            } else {
                liquidPhrase
                        .append("The total precipitation for the month stands at "
                                + precipString + " inches");
            }

            if (precipFlag.getPrecipTotal().isDeparture() && yClimate
                    .getPrecipMonthMean() != ParameterFormatClimate.MISSING_PRECIP) {
                float deltaPrecip = yesterday.getPrecipMonth()
                        - yClimate.getPrecipMonthMean();

                if (Math.abs(
                        deltaPrecip) < ParameterFormatClimate.HALF_PRECIP) {
                    liquidPhrase.append(", which is normal");
                } else {
                    if (deltaPrecip < 0) {
                        which = false;
                        deltaPrecip = Math.abs(deltaPrecip);
                    } else {
                        which = true;
                    }

                    precipString = String.format(FLOAT_TWO_DECIMALS1,
                            deltaPrecip);
                    if (deltaPrecip < 1) {
                        precipString = "0";

                    }

                    liquidPhrase.append(", which is " + precipString + " inches"
                            + ClimateNWRFormat.aboveBelow(which) + " normal");
                }

            }

            liquidPhrase.append(PERIOD + SPACE + SPACE);
        }

        if (precipFlag.getPrecipTotal().isTotalSeason() && yesterday
                .getPrecipSeason() != ParameterFormatClimate.MISSING_PRECIP) {
            if (yesterday.getPrecipSeason() == 0
                    && yesterday.getPrecip() == ParameterFormatClimate.TRACE) {
                yesterday.setPrecipSeason(ParameterFormatClimate.TRACE);
            }

            liquidPhrase.append("The total precipitation for the season ");

            if (Math.abs(yesterday
                    .getPrecip()) < ParameterFormatClimate.HALF_PRECIP) {
                liquidPhrase.append("still ");
            } else if (yesterday
                    .getPrecip() != ParameterFormatClimate.MISSING_PRECIP) {
                liquidPhrase.append("now ");
            }

            String precipString = String.format(FLOAT_COMMAS_TWO_DECIMALS,
                    yesterday.getPrecipSeason());
            if (yesterday.getPrecipSeason() < 1
                    && yesterday.getPrecip() != ParameterFormatClimate.TRACE) {
                precipString = "0";
            }

            if (yesterday.getPrecipSeason() == ParameterFormatClimate.TRACE) {
                liquidPhrase.append("stands at a trace");
            } else {
                liquidPhrase.append("stands at " + precipString + " inches");
            }

            if (precipFlag.getPrecipTotal().isDeparture() && yClimate
                    .getPrecipSeasonMean() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getPrecipSeason() == ParameterFormatClimate.TRACE
                        && yClimate
                                .getPrecipSeasonMean() != ParameterFormatClimate.TRACE
                        && yClimate.getPrecipSeasonMean() != 0) {
                    yesterday.setPrecipSeason(0);
                }

                float deltaPrecip = yesterday.getPrecipSeason()
                        - yClimate.getPrecipSeasonMean();

                if (Math.abs(
                        deltaPrecip) < ParameterFormatClimate.HALF_PRECIP) {
                    liquidPhrase.append(", which is normal");
                } else if (yesterday
                        .getPrecipSeason() == ParameterFormatClimate.TRACE
                        && yClimate.getPrecipSeasonMean() == 0) {
                    liquidPhrase.append(
                            ", whereas the normal amount for the season is zero inches");
                } else {
                    if (deltaPrecip < 0) {
                        which = false;
                        deltaPrecip = Math.abs(deltaPrecip);
                    } else {
                        which = true;
                    }

                    precipString = String.format(FLOAT_COMMAS_TWO_DECIMALS,
                            deltaPrecip);
                    if (deltaPrecip < 1) {
                        precipString = "0";
                    }

                    liquidPhrase.append(", which is " + precipString
                            + " inches " + ClimateNWRFormat.aboveBelow(which)
                            + " normal");
                }
            }

            if (!precipFlag.getPrecipTotal().isTotalYear()
                    || precipFlag.getPrecipTotal().isDeparture() || yesterday
                            .getPrecipYear() == ParameterFormatClimate.MISSING_PRECIP) {
                liquidPhrase.append(PERIOD + SPACE + SPACE);
            }
        }

        if (precipFlag.getPrecipTotal().isTotalYear() && yesterday
                .getPrecipYear() != ParameterFormatClimate.MISSING_PRECIP) {
            if (yesterday.getPrecipYear() == 0
                    && yesterday.getPrecip() == ParameterFormatClimate.TRACE) {
                yesterday.setPrecipYear(ParameterFormatClimate.TRACE);
            }

            if (precipFlag.getPrecipTotal().isTotalSeason()
                    && !precipFlag.getPrecipTotal().isDeparture() && yesterday
                            .getPrecipSeason() != ParameterFormatClimate.MISSING_PRECIP) {
                liquidPhrase
                        .append("and the total precipitation for the year ");
            } else {
                liquidPhrase.append("The total precipitation for the year ");
            }

            if (precipFlag.getPrecipTotal().isTotalSeason() && yesterday
                    .getPrecipSeason() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getPrecip() == 0) {
                    liquidPhrase.append("is still ");

                } else if (yesterday
                        .getPrecip() == ParameterFormatClimate.MISSING_PRECIP) {
                    liquidPhrase.append("is ");
                } else {
                    liquidPhrase.append("is now ");
                }
            } else {
                if (yesterday.getPrecip() == 0) {
                    liquidPhrase.append("still stands at ");
                } else if (yesterday
                        .getPrecip() == ParameterFormatClimate.MISSING_PRECIP) {
                    liquidPhrase.append("stands at ");
                } else {
                    liquidPhrase.append("now stands at ");
                }
            }

            String precipString = String.format(FLOAT_COMMAS_TWO_DECIMALS,
                    yesterday.getPrecipYear());
            if (yesterday.getPrecipYear() < 1
                    && yesterday.getPrecip() != ParameterFormatClimate.TRACE) {
                precipString = "0";
            }

            if (yesterday.getPrecipYear() == ParameterFormatClimate.TRACE) {
                liquidPhrase.append("a trace");
            } else {
                liquidPhrase.append(precipString + " inches");
            }

            if (precipFlag.getPrecipTotal().isDeparture() && yClimate
                    .getPrecipYearMean() != ParameterFormatClimate.MISSING_PRECIP) {
                if (yesterday.getPrecipYear() == ParameterFormatClimate.TRACE
                        && yClimate
                                .getPrecipYearMean() != ParameterFormatClimate.TRACE
                        && yClimate.getPrecipYearMean() != 0) {
                    yesterday.setPrecipYear(0);
                }

                float deltaPrecip = yesterday.getPrecipYear()
                        - yClimate.getPrecipYearMean();

                if (Math.abs(
                        deltaPrecip) < ParameterFormatClimate.HALF_PRECIP) {

                    liquidPhrase.append(", which is normal.  ");
                } else if (yesterday
                        .getPrecipYear() == ParameterFormatClimate.TRACE
                        && yClimate.getPrecipYearMean() == 0) {
                    liquidPhrase.append(
                            ", whereas the normal amount for the year is zero inches.  ");
                } else {
                    if (deltaPrecip < 0) {
                        which = false;
                        deltaPrecip = Math.abs(deltaPrecip);
                    } else {
                        which = true;
                    }

                    precipString = String.format(FLOAT_COMMAS_TWO_DECIMALS,
                            deltaPrecip);
                    if (deltaPrecip < 1) {
                        precipString = "0";
                    }

                    liquidPhrase.append(", which is " + precipString
                            + " inches " + ClimateNWRFormat.aboveBelow(which)
                            + " normal.  ");
                }
            } else {
                liquidPhrase.append(PERIOD + SPACE + SPACE);
            }
        }

        return liquidPhrase.toString();
    }

    /**
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   March 1998     Barry N. Baxter       PRC/TDL
    *
    *   Purpose:  
    *        This subroutine is use only to call other subroutines when the flags
    *        to the other subroutines are set to TRUE. 
    *
    *   Variables
    *
    *      Input
    *
    *           DO_CELSIUS - Flag for reporting temperatures in Celsius
    *                        - TRUE  report temps in Celsius
    *                        - FALSE don't report temps in Celsius
    *          DO_MAX_TEMP - Structure containing the flags which control generation
    *                        of various protions.
    *                        the maximum temperature
    *              MORNING - flag which differntiates morning or evening reports;
    *                        -1 morning report
    *                        -2 evening report
    *                  NUM - Number of stations in this group to be summarized.
    *            Y_CLIMATE - Structure containing historical climatology.
    *               Y_DATE - Structure containing the observed date for yesterday.
    *            YESTERDAY - Structure containing the observed climate data.
    *                        See the structure definition for more details.
    *          TEMP_PHRASE - Character buffer containing the temperature sentences.
    *
    *      Output
    *
    *          TEMP_PHRASE - Character buffer containing the temperature sentences.
    *
    *      Local
    *
    *  NONE
    *
    *      INCLUDE files
    *     DEFINE_general_phrases.I - Contains character strings need to build all
    *                               types of sentences.
    *      DEFINE_precip_phrases.I - Contains character strings needed to build
    *                                precip sentences.
    *   PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                                arrays, etc.  This INCLUDE file must always
    *                                come first.
    *         TYPE_climate_dates.I - Defines the derived TYPE "climate_dates";
    *                                note that it uses the dervied TYPE "date" - 
    *                                the "date" file must be INCLUDED before this
    *                                one.
    *    TYPE_climate_record_day.I - Defines the derived TYPE used to store the
    *                                historical climatological record for a given
    *                                site for a given day.  Uses derived TYPE
    *                                "wind" so that INCLUDE file must come before
    *                                this one.
    *                  TYPE_date.I - Defines the derived TYPE "date";
    *                                This INCLUDE file must always come before any
    *                                other INCLUDE file which uses the "date" TYPE.
    *    TYPE_daily_climate_data.I - Defines the derived TYPE used to store the
    *                                observed climatological data.  Note that
    *                                INCLUDE file defining the wind TYPE must be 
    *                                specified before this file.
    *    TYPE_do_weather_element.I - Defines the derived TYPE used to hold the
    *                                controlling logic for producing
    *                                sentences/reports for a given variable.
    *  TYPE_report_climate_norms.I - Defines the derived TYPE used to hold the
    *                                flags which control reporting the
    *                                climatological norms for different
    *                                meteorological variables.
    *                  TYPE_time.I - Defines the derived TYPE used to specify the
    *                                time.
    *                  TYPE_wind.I - Defines the derived TYPE used to specify wind
    *                                speed,and direction.
    *
    *      Non-system routines used
    *
    *            build_NWR_celsius - Builds part of sentence that 
    *                                includes converting f to c. 
    *         build_NWR_difference - routine used to convert celsius 
    *                                number (if negative) to a positive 
    *                        when reporting above or below 
    *                                normal.                 
    *        CONVERT_CHARACTER_INT - Converts INTEGERS numbers to CHARACTER.
    *
    *      Non-system functions used
    *
    *                  PICK_DIGITS - Returns the number of digits in a number.
    *
    *                       STRLEN - C function: returns the number of characters in a
    *                                string; string must be terminated with the NULL
    *                                character.
     *
     * </pre>
     * 
     * @param climateDailyReportData
     * @return
     */
    private String buildNWRTemp(ClimateDailyReportData climateDailyReportData) {
        StringBuilder nwrTemp = new StringBuilder();

        // create an output sentence with the max temp values
        nwrTemp.append(buildMaxMinTemp(climateDailyReportData, true));

        // create an output sentence with the max record temp values
        nwrTemp.append(buildMaxMinTempRecord(climateDailyReportData, true));

        // create an output sentence with the min temp values
        nwrTemp.append(buildMaxMinTemp(climateDailyReportData, false));

        // create an output sentence with the min record temp values
        nwrTemp.append(buildMaxMinTempRecord(climateDailyReportData, false));

        return nwrTemp.toString();
    }

    /**
     * Migrated from build_NWR_max_temp_record.f and
     * build_NWR_min_temp_record.f. Logic in these subroutines is identical, so
     * combine them here.
     * 
     * @param climateDailyReportData
     * @return
     */
    private Object buildMaxMinTempRecord(ClimateDailyReportData reportData,
            boolean max) {
        StringBuilder nwrTempRecordPhrase = new StringBuilder();

        ClimateProductFlags tempFlag = max
                ? currentSettings.getControl().getTempControl().getMaxTemp()
                : currentSettings.getControl().getTempControl().getMinTemp();
        int tempValue = max ? reportData.getData().getMaxTemp()
                : reportData.getData().getMinTemp();
        int tempRecord = max ? reportData.getyClimate().getMaxTempRecord()
                : reportData.getyClimate().getMinTempRecord();

        int[] tempYear = max ? reportData.getyClimate().getMaxTempYear()
                : reportData.getyClimate().getMinTempYear();

        if (tempFlag.isMeasured()) {
            String highOrLow = max ? "high" : "low";
            if (tempValue != ParameterFormatClimate.MISSING
                    && tempRecord != ParameterFormatClimate.MISSING
                    && tempFlag.isRecord()) {
                String breakOrTie;
                if ((max && tempValue >= tempRecord)
                        || (!max && tempValue <= tempRecord)) {
                    breakOrTie = "breaks ";

                    if (max && tempValue == tempRecord) {
                        breakOrTie = "ties ";
                    }

                    nwrTempRecordPhrase.append("The " + highOrLow + SPACE
                            + breakOrTie + "the previous ");

                    nwrTempRecordPhrase
                            .append(ClimateNWRFormat.decideDegree(tempRecord));
                }

                if ((max && tempValue < tempRecord)
                        || (!max && tempValue > tempRecord)) {

                    nwrTempRecordPhrase
                            .append("The record " + highOrLow + " is ");

                    nwrTempRecordPhrase
                            .append(ClimateNWRFormat.decideDegree(tempRecord));
                }

                if (currentSettings.getControl().isDoCelsius()) {
                    nwrTempRecordPhrase.append(
                            ClimateNWRFormat.buildNWRCelsius(tempRecord));
                }

                int yearCount = 0;
                if (tempFlag.isRecordYear()
                        && (tempYear[0] != ParameterFormatClimate.MISSING
                                || tempYear[1] != ParameterFormatClimate.MISSING
                                || tempYear[2] != ParameterFormatClimate.MISSING)) {
                    for (int i = 0; i < tempYear.length; i++) {
                        if (tempYear[i] != ParameterFormatClimate.MISSING) {
                            yearCount++;
                        } else {
                            tempYear[i] = 0;
                        }
                    }

                    int recordYear = Arrays.stream(tempYear).max().getAsInt();

                    if (yearCount <= 1) {
                        nwrTempRecordPhrase
                                .append(" which was set in " + recordYear);
                    } else {
                        nwrTempRecordPhrase
                                .append(" which was last set in " + recordYear);
                    }

                }
                nwrTempRecordPhrase.append(PERIOD + SPACE + SPACE);
            }

        }

        return nwrTempRecordPhrase;
    }

    /**
     * Migrated from build_NWR_max_temp.f and build_NWR_max_temp.f. These
     * subrountines have mostly the same duplicate logic. Combine into one
     * method to avoid lots of repeated code.
     * 
     * @param reportData
     * @param max
     *            true if appending max temp text, false if min temp
     * @return
     */
    private String buildMaxMinTemp(ClimateDailyReportData reportData,
            boolean max) {
        StringBuilder nwrTempPhrase = new StringBuilder();

        // ClimateRecordDay yClimate = reportData.getyClimate();
        ClimateProductFlags tempFlag;
        TemperatureControlFlags tempSetting = currentSettings.getControl()
                .getTempControl();
        int tempValue;
        int tempMean;
        ClimateTime tempTime;
        if (max) {
            tempFlag = tempSetting.getMaxTemp();
            tempValue = reportData.getData().getMaxTemp();
            tempTime = reportData.getData().getMaxTempTime().to12HourTime();
            tempMean = reportData.getyClimate().getMaxTempMean();
        } else {
            tempFlag = tempSetting.getMinTemp();
            tempValue = reportData.getData().getMinTemp();
            tempTime = reportData.getData().getMinTempTime().to12HourTime();
            tempMean = reportData.getyClimate().getMinTempMean();
        }

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        // flag for whether only temperature values are output
        boolean tempOnly = tempSetting.getMaxTemp().isMeasured()
                && !tempSetting.getMaxTemp().isTimeOfMeasured()
                && !tempSetting.getMaxTemp().isNorm()
                && tempSetting.getMinTemp().isMeasured()
                && !tempSetting.getMinTemp().isTimeOfMeasured()
                && !tempSetting.getMinTemp().isNorm();

        if (tempFlag.isMeasured()
                && tempValue != ParameterFormatClimate.MISSING) {
            String highOrLow = max ? "high" : "low";

            if (!max && tempOnly) {

                nwrTempPhrase.append(" and the low ");

            } else {

                if (!morning || currentSettings
                        .getReportType() == PeriodType.INTER_RAD) {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        nwrTempPhrase.append("So far today the " + highOrLow
                                + " temperature has been ");
                    } else {
                        if (!tempFlag.isTimeOfMeasured()
                                || tempTime
                                        .getHour() == ParameterFormatClimate.MISSING_HOUR
                                || tempTime
                                        .getMin() == ParameterFormatClimate.MISSING_MINUTE) {
                            nwrTempPhrase.append("Today's " + highOrLow
                                    + " temperature was ");
                        } else {
                            nwrTempPhrase.append("Today's " + highOrLow
                                    + " temperature of ");
                        }
                    }
                } else {
                    if (!tempFlag.isTimeOfMeasured()
                            || tempTime
                                    .getHour() == ParameterFormatClimate.MISSING_HOUR
                            || tempTime
                                    .getMin() == ParameterFormatClimate.MISSING_MINUTE) {
                        nwrTempPhrase.append("Yesterday's " + highOrLow
                                + " temperature was ");
                    } else {
                        nwrTempPhrase.append("Yesterday's " + highOrLow
                                + " temperature of ");
                    }
                }
            }

            nwrTempPhrase.append(ClimateNWRFormat.decideDegree(tempValue));

            if (currentSettings.getControl().isDoCelsius()) {
                // add the words " degrees farenheit, or <number> celsius"
                nwrTempPhrase.append(ClimateNWRFormat
                        .buildNWRCelsius(ClimateUtilities.nint(ClimateUtilities
                                .fahrenheitToCelsius(tempValue))));
            }

            if (tempFlag.isTimeOfMeasured()
                    && tempTime.getHour() != ParameterFormatClimate.MISSING_HOUR
                    && tempTime
                            .getMin() != ParameterFormatClimate.MISSING_MINUTE) {

                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrTempPhrase.append(", which occurred at ");
                } else {
                    nwrTempPhrase.append(" occurred at ");
                }

                nwrTempPhrase.append(tempTime.toHourMinString() + SPACE
                        + tempTime.getAmpm());
            }

            if (tempFlag.isNorm()
                    && tempMean != ParameterFormatClimate.MISSING) {

                int tempFDelta = tempValue - tempMean;
                int tempCDelta = (int) (ClimateUtilities
                        .fahrenheitToCelsius(tempValue)
                        - ClimateUtilities.fahrenheitToCelsius(tempMean));

                if (!tempFlag.isDeparture()) {
                    nwrTempPhrase.append(".  The normal " + highOrLow + " is ");
                } else {
                    if (tempFDelta != 0) {
                        tempCDelta = Math.abs(tempCDelta);
                        nwrTempPhrase.append(". This " + highOrLow + " was ");

                        nwrTempPhrase.append(ClimateNWRFormat
                                .decideDegree(Math.abs(tempFDelta)));

                        if (currentSettings.getControl().isDoCelsius()) {
                            nwrTempPhrase.append(ClimateNWRFormat
                                    .buildNWRCelsius(tempCDelta));
                        }
                    }

                    tempFDelta = tempValue - tempMean;

                    if (tempFDelta > 0) {
                        nwrTempPhrase.append(
                                " above the normal " + highOrLow + " of ");
                    } else if (tempFDelta < 0) {
                        nwrTempPhrase.append(
                                " below the normal " + highOrLow + " of ");
                    } else {
                        nwrTempPhrase.append(
                                ". This tied the normal " + highOrLow + " of ");
                    }
                }

                nwrTempPhrase.append(tempMean);

                if (currentSettings.getControl().isDoCelsius()) {
                    nwrTempPhrase
                            .append(ClimateNWRFormat.buildNWRCelsius(tempMean));
                }
            }

            nwrTempPhrase.append(PERIOD + SPACE + SPACE);
        }

        return nwrTempPhrase.toString();
    }

}

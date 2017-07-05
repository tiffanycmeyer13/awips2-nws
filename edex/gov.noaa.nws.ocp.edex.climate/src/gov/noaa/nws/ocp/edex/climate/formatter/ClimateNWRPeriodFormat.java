/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.report.ClimatePeriodReportData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.common.localization.climate.producttype.DegreeDaysControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.PrecipitationControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.SnowControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.TemperatureControlFlags;
import gov.noaa.nws.ocp.common.localization.climate.producttype.WindControlFlags;

/**
 * Class containing logic for building NWR monthly/seasonal/annual text.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 07, 2017 21099      wpaintsil   Initial creation
 * May 10, 2017 30162      wpaintsil   Address FindBugs issues with String.format 
 *                                     and exceptions caused by empty lists.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public class ClimateNWRPeriodFormat extends ClimateNWRFormat {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateNWRPeriodFormat.class);

    /**
     * Constant for precipitation threshold string.
     */
    private static final String THRESHOLD_001 = "0.01";

    /**
     * Constant for precipitation threshold string.
     */
    private static final String THRESHOLD_010 = "0.10";

    /**
     * Constant for precipitation threshold string.
     */
    private static final String THRESHOLD_050 = "0.50";

    /**
     * Constant for precipitation threshold string.
     */
    private static final String THRESHOLD_100 = "1.00";

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateNWRPeriodFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        super(currentSettings, globalConfig);
    }

    /**
     * Migrated from build_period_radio.f.
     * 
     * <pre>
    *   October  1999     Dan Zipper   PRC/TDL
    *   December 1999     Bonnie Reed  PRC/TDL
    *
    *   Purpose:  This routine is the driver responsible for building the
    *             NWR monthly, seasonal, and annual climate summary.
    *
    *   Variables
    *
    *      Input
    *
    *    CLIMATE_STATIONS - derived TYPE which contains the station ids and plain
    *                       language stations names for the stations in this climate
    *                       summary.           
    *          DO_CELSIUS - Flag for reporting temperatures in Celsius
    *                       - TRUE  report temps in Celsius
    *                       - FALSE don't report temps in Celsius
    *               ITYPE - flag that determines the type of climate summary
    *                       - 1 morning weather radio daily climate summary
    *                       - 2 evening weather radio daily climate summary
    *                       - 3 nwws morning daily climate summary
    *                       - 4 nwws evening daily climate summary
    *                       - 5 monthly radio climate summary
    *                       - 6 monthly nwws climate summary
    *        NUM_STATIONS - number of stations in this group to be summarized.
    *          NWR_HEADER - structure containing the opening data for the CRS.
    *          VALID_TIME - structure containing the valid time which includes hour,
    *                       minutes, AMPM, and zone.
    *
    *      Output
    *
    *         OUTPUT_FILE - file where data is output.
    *
    *      Local
    *        i                  - loop index
    *
    *      Non-system routines used - none
    *
    *      Non-system functions used - none
    *
    *  CHANGE LOG   NAME              DATE        CHANGES
    *               David T. Miller   Oct 1 1999  removed num
    *                                             and num_stations from the 
    *                                             merge phrases routine arguments
    *                                             list since they aren't neceesary
    *                                             since the escape b sequence was
    *                                             moved to merge header.
    *    5/07/01   Doug Murphy            Made code more efficient - not necessary
    *                                     to pass along entire arrays (only done
    *                                     for precip...others need to be updated
    *                                     if time allows)
     * </pre>
     */
    @Override
    public Map<String, ClimateProduct> buildText(
            ClimateCreatorResponse reportData)
                    throws ClimateInvalidParameterException,
                    ClimateQueryException {
        Map<String, ClimateProduct> prod = new HashMap<>();

        Map<Integer, ClimatePeriodReportData> reportMap = ((ClimateCreatorPeriodResponse) reportData)
                .getReportMap();

        StringBuilder productText = new StringBuilder();
        StringBuilder headerPhrase = new StringBuilder(buildNWRHeader());
        if (headerPhrase.length() > 0) {
            headerPhrase.append("\n\n");
        }
        // Match station(s) in currentSettings with
        // stations and data in reportMap.
        for (Station station : currentSettings.getStations()) {
            Integer stationId = station.getInformId();

            ClimatePeriodReportData report = reportMap.get(stationId);
            if (report == null) {
                logger.warn("The station with informId " + stationId
                        + " defined in the ClimateProductType settings object was not found in the ClimateCreatorResponse report map.");
                continue;
            }

            PeriodType type = currentSettings.getReportType();

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
            String tempPhrase = buildNWRPeriodTemp(report);
            if (tempPhrase.length() > 0) {
                productText.append(tempPhrase + spaces);
            }

            // precipitation phrases
            String precipPhrase = buildNWRPeriodPrecip(report);
            if (precipPhrase.length() > 0) {
                productText.append(precipPhrase + spaces);
            }

            // degree day phrases
            String heatCoolPhrase = buildNWRPeriodHeatAndCool(report);
            if (heatCoolPhrase.length() > 0) {
                productText.append(heatCoolPhrase + spaces);
            }

            // wind phrases
            String windPhrase = buildNWRPeriodWind(report);
            if (windPhrase.length() > 0) {
                productText.append(windPhrase + spaces);
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
     * Migrated from build_NWR_period_wind.f.
     * 
     * <pre>
    *   October  1999     David Zipper       PRC/TDL
    *   November 1999     Bonnie Reed        PRC/TDL
    *
    *   Purpose: The purpose of this program is to create the wind portion
    *            of the NOAA weather radio. Sentence structure output is 
    *            documented in the climate design notebook.
    * 
    *        Variables
    *
    *        Input
    *
    *             do_wind - structure containint the wind flags which control
    *                       generation of various portions of the wind phrase
    *               itype - declares either monthly or seasonal report
    *                 num - number of stations in this group to be
    *                       summarized
    *             h_climo - structure containing historical climatology
    *                       for a given date.  See the structure 
    *                       definition for more details
    *              a_date - date structure containing valid date for
    *                       for the climate summary.  See the structure
    *                       definition for more details
    *              p_data - structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details
    *         wind_phrase - character buffer containing the wind
    *                       sentences structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details
    *
    *
    *        Output
    *         wind_phrase - character buffer containing the wind sentences
    *
    *        Local
    *          gdirection - the direction of the max wind gust
    *                ilen - number of characters in a string
    *          mdirection - the direction of the max wind
    *         num_decimal - number that accounts for the decimal place
    *          rdirection - the direction of the resultant wind
    *           wind_mph1 - value of the wind in mph (converted from knots)
    *             avg_mph - character string for the average windspeed
    *            wind_mph - character string for the max windspeed
    *            gust_mph - character string for the gust wind
    *              direct - holds the number of characters in the wind direction
    *             direct1 - holds the number of characters in the wind direction
    *             direct2 - holds the number of characters in the wind direction
    *                 mon - the character value of the month (January) 
    *         c_wind      - Character string used to hold the relative 
    *                       humidity value once it has been converted to
    *                       character from numeric format.
    *         num_char    - Number of characters in a string.
    *         num_char1   - Number of characters in a string.
    *         num_digits  - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *         num_digits1 - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *      MODIFICATION HISTORY
    *      --------------------
    *        12/28/00   Doug Murphy             Max wind direction was using
    *                                           the max gust direction
    *         2/15/01   Doug Murphy             Wind speeds are now mph
    *                                           throughout climate... no need
    *                                           to convert from knots now
     * </pre>
     * 
     * @param reportData
     * @return
     * @throws ClimateInvalidParameterException
     */
    private String buildNWRPeriodWind(ClimatePeriodReportData reportData)
            throws ClimateInvalidParameterException {
        StringBuilder windPhrase = new StringBuilder();
        WindControlFlags windFlag = currentSettings.getControl()
                .getWindControl();
        PeriodData periodData = reportData.getData();

        // "The wind was calm for the period."
        // OR "The mean wind for the period was <less than 5 miles an hour>/<__
        // miles per hour>."
        if (windFlag.getMeanWind().isMeasured() && periodData
                .getAvgWindSpd() != ParameterFormatClimate.MISSING) {

            if (periodData.getAvgWindSpd() == 0) {
                windPhrase.append("The wind was calm for the period");
            } else {
                windPhrase.append("The mean wind for the period was ");
            }

            if (periodData.getAvgWindSpd() > 0
                    && periodData.getAvgWindSpd() < 5) {
                windPhrase.append("less than 5 miles an hour");
            } else if (periodData.getAvgWindSpd() > 5) {
                int windMph1 = ClimateUtilities
                        .nint(periodData.getAvgWindSpd());

                windPhrase.append(windMph1 + " miles per hour");
            }

            if (periodData.getAvgWindSpd() > 0) {
                windPhrase.append(PERIOD + SPACE + SPACE);
            }
        }

        // "The maximum wind was __ mile(s) per hour from the <direction> and
        // occurred on <date>."
        if (windFlag.getMaxWind().isMeasured()
                && !periodData.getMaxWindList().isEmpty()
                && periodData.getMaxWindList().get(0)
                        .getSpeed() != ParameterFormatClimate.MISSING
                && periodData.getMaxWindList().get(0).getSpeed() != 0) {
            int windMph = ClimateUtilities
                    .nint(periodData.getMaxWindList().get(0).getSpeed());

            windPhrase.append("The " + MAXIMUM + " wind was " + windMph + SPACE
                    + mileMiles(windMph != 1) + " per hour");

            if (periodData.getMaxWindList().get(0)
                    .getDir() != ParameterFormatClimate.MISSING) {
                windPhrase.append(" from the " + whichDirection(
                        periodData.getMaxWindList().get(0).getDir(), false));
            }

            if (windFlag.getMaxWind().isTimeOfMeasured()
                    && periodData.getMaxWindDayList().get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getMaxWindDayList().get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {

                windPhrase.append(" and occurred on "
                        + DateFormatSymbols.getInstance()
                                .getMonths()[periodData.getMaxWindDayList()
                                        .get(0).getMon() - 1]
                        + SPACE
                        + periodData.getMaxWindDayList().get(0).getDay());
            }

            windPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "The maximum wind gust was __ mile(s) per hour from the <direction>
        // and occurred on <date>."
        if (windFlag.getMaxGust().isMeasured()
                && !periodData.getMaxGustList().isEmpty()
                && periodData.getMaxGustList().get(0)
                        .getSpeed() != ParameterFormatClimate.MISSING
                && periodData.getMaxGustList().get(0).getSpeed() != 0) {
            int windMph = ClimateUtilities
                    .nint(periodData.getMaxGustList().get(0).getSpeed());

            windPhrase.append("The " + MAXIMUM + " wind gust was " + windMph
                    + SPACE + mileMiles(windMph != 1) + " per hour");
            if (periodData.getMaxGustList().get(0)
                    .getDir() != ParameterFormatClimate.MISSING) {
                windPhrase.append(" from the " + whichDirection(
                        periodData.getMaxGustList().get(0).getDir(), false));
            }

            if (windFlag.getMaxGust().isTimeOfMeasured()
                    && !periodData.getMaxGustDayList().isEmpty()
                    && periodData.getMaxGustDayList().get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getMaxGustDayList().get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {
                windPhrase.append(" and occurred on "
                        + DateFormatSymbols.getInstance()
                                .getMonths()[periodData.getMaxGustDayList()
                                        .get(0).getMon() - 1]
                        + SPACE
                        + periodData.getMaxGustDayList().get(0).getDay());
            }
            windPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "The resultant wind was __ mile(s) per hour from the <direction>."
        if (windFlag.getResultWind().isMeasured()) {
            if (periodData.getResultWind()
                    .getSpeed() != ParameterFormatClimate.MISSING
                    && periodData.getResultWind().getSpeed() != 0) {
                int windMph = ClimateUtilities
                        .nint(periodData.getResultWind().getSpeed());

                windPhrase.append("The resultant wind was " + windMph + SPACE
                        + mileMiles(windMph != 1) + " per hour");

                if (periodData.getResultWind()
                        .getDir() != ParameterFormatClimate.MISSING) {
                    windPhrase.append(" from the " + whichDirection(
                            periodData.getResultWind().getDir(), false));
                }

                windPhrase.append(PERIOD + SPACE + SPACE);
            }
        }

        return windPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_heat_and_cool.f
     * 
     * <pre>
    *    November 1999  Bonnie Reed            PRC/TDL
    *
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
    *                itype - declares either a monthly report or seasonal report
    *               a_date - date structure containing valid date for
    *                        for the climate summary.  See the structure
    *                        definition for more details
    *       do_cool_report - structure containg the flags which control
    *                        generation of various portions of the
    *                        defintion for more details.
    *       do_heat_report - structure containg the flags which control
    *                        generation of various portions of the
    *                        heating days sentences. See the structure
    *                        definition for more details
    *                  num - number of stations in this group to be
    *                        summarized
    *              h_climo - structure containing historical climatology
    *                        for a given date.  See the structure 
    *                        definition for more details
    *               p_data - structure containing the observed climate
    *                        data.  See the structure definition for
    *                        more details
    * heat_and_cool_phrase - character buffer containing the cooling degree
    *                        day sentences       
    *
    *      Output
    * heat_and_cool_phrase - character buffer containing the cooling degree
    *                       day sentences       
    *
    *      Local
    *
    *                c_cool - character form of the cooling degree day total
    *             c_efreeze - character form of early freezing day
    *                c_heat - character form of the heating degree day total
    *             c_lfreeze - character form of late freezing day
    *                c_norm - character form of the normal value
    *            delta_heat - the difference between two values
    *            delta_cool - the difference between two values
    *        delta_num_cool - the difference between two values
    *        delta_num_heat - the difference between two values      
    *             frez_norm - character form of normal freezing days
    *                  ilen - number of characters in a string
    *                   mon - character string of the word month (January)
    *              num_char - Number of characters in a string.
    *            num_digits - The number of digits in a given number; 
    *                         necessary for the conversion of numeric values 
    *                         to character.
    *                plural - character either set to "day" or "days" 
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *
    *
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
    *                    DECIDE_DAY - FORTRAN function:  returns either the singular or
    *                       plural version of the word "day" as well as the
    *                       number of characters in the word.
    *
    * MODIFICATION HISTORY
    * ====================
    *   3/29/01     Doug Murphy                 Fixed stuttering departure sentences:
    *                                           they were saying "which is which is
    *                                           normal."
    *   4/10/01     Doug Murphy                 Added code which adds commas to
    *                                           values of 1000 or greater... also did
    *                                           some minor revision to make code 
    *                                           cleaner
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRPeriodHeatAndCool(
            ClimatePeriodReportData reportData) {
        StringBuilder heatCoolPhrase = new StringBuilder();

        DegreeDaysControlFlags degreeFlag = currentSettings.getControl()
                .getDegreeDaysControl();

        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        if (reportWindow(currentSettings.getControl().getHeatDates())) {

            // Measured heating degree day
            heatCoolPhrase.append(heatCoolPhraseHelper(degreeFlag.getTotalHDD(),
                    periodData.getNumHeatTotal(), hClimo.getNumHeatPeriodNorm(),
                    "", true));
            // Heating degree day since July 1
            heatCoolPhrase.append(heatCoolPhraseHelper(
                    degreeFlag.getSeasonHDD(), periodData.getNumHeat1July(),
                    hClimo.getNumHeat1JulyNorm(), " since July 1", true));

        }

        if (reportWindow(currentSettings.getControl().getCoolDates())) {

            // Measured cooling degree day
            heatCoolPhrase.append(heatCoolPhraseHelper(degreeFlag.getTotalCDD(),
                    periodData.getNumCoolTotal(), hClimo.getNumCoolPeriodNorm(),
                    "", false));
            // Cooling degree day since January 1
            heatCoolPhrase.append(heatCoolPhraseHelper(
                    degreeFlag.getSeasonCDD(), periodData.getNumCool1Jan(),
                    hClimo.getNumCool1JanNorm(), " since January 1", false));
        }

        // "The first freeze day occurred on <date> and the normal earliest
        // freeze date is <date>."
        if (degreeFlag.getEarlyFreeze().isMeasured()
                && periodData.getEarlyFreeze()
                        .getDay() != ParameterFormatClimate.MISSING_DATE
                && periodData.getEarlyFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE) {
            heatCoolPhrase
                    .append("The first freeze day occurred on "
                            + DateFormatSymbols.getInstance()
                                    .getMonths()[periodData.getEarlyFreeze()
                                            .getMon() - 1]
                            + SPACE + periodData.getEarlyFreeze().getDay());

            if (degreeFlag.getEarlyFreeze().isNorm()
                    && hClimo.getEarlyFreezeNorm()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && hClimo.getEarlyFreezeNorm()
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {

                heatCoolPhrase.append(" and the normal earliest freeze date is "
                        + DateFormatSymbols.getInstance()
                                .getMonths()[hClimo.getEarlyFreezeNorm()
                                        .getMon() - 1]
                        + SPACE + hClimo.getEarlyFreezeNorm().getDay());
            }
            heatCoolPhrase.append(PERIOD + SPACE + SPACE);
        }

        if (degreeFlag.getLateFreeze().isMeasured()
                && periodData.getLateFreeze()
                        .getDay() != ParameterFormatClimate.MISSING_DATE
                && periodData.getLateFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE) {
            heatCoolPhrase
                    .append("The last freeze day occurred on "
                            + DateFormatSymbols.getInstance()
                                    .getMonths()[periodData.getLateFreeze()
                                            .getMon() - 1]
                            + SPACE + periodData.getLateFreeze().getDay());

            if (degreeFlag.getLateFreeze().isNorm()
                    && hClimo.getLateFreezeNorm()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && hClimo.getLateFreezeNorm()
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {

                heatCoolPhrase
                        .append(" and the normal latest freeze date is "
                                + DateFormatSymbols.getInstance()
                                        .getMonths()[hClimo.getLateFreezeNorm()
                                                .getMon() - 1]
                                + SPACE + hClimo.getLateFreezeNorm().getDay());
            }
            heatCoolPhrase.append(PERIOD + SPACE + SPACE);
        }

        return heatCoolPhrase.toString();
    }

    /**
     * Helper method to reduce code repetition in buildNWRPeriodHeatAndCool()
     * 
     * @param degreeFlag
     * @param degreeValue
     * @param normValue
     * @param periodString
     * @param heat
     *            true if outputting a heating phrase, false if outputting a
     *            cooling phrase
     * @return
     */
    private String heatCoolPhraseHelper(ClimateProductFlags degreeFlag,
            int degreeValue, int normValue, String periodString, boolean heat) {
        StringBuilder heatCoolPhrase = new StringBuilder();

        // "There was/were __ degree day(s) <this period>/<since January/July 1>
        // which is <normal>/<the normal amount>"
        if (degreeFlag.isMeasured()
                && degreeValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
            String heatCool = heat ? HEATING : COOLING;

            if (degreeValue == 0) {
                heatCoolPhrase.append("There were no");
            } else {
                heatCoolPhrase.append("There " + wasWere(degreeValue != 1)
                        + SPACE + String.format(INT_COMMAS, degreeValue));

            }

            heatCoolPhrase.append(SPACE + heatCool + " degree "
                    + dayDays(degreeValue != 1)
                    + (periodString.isEmpty() ? " this period" : periodString));

            if (degreeFlag.isDeparture()
                    && normValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                heatCoolPhrase.append(" which is ");

                int deltaDegDay = degreeValue - normValue;

                if (deltaDegDay != 0) {

                    heatCoolPhrase.append(
                            String.format(INT_COMMAS, Math.abs(deltaDegDay))
                                    + SPACE + aboveBelow(deltaDegDay > 0)
                                    + " the normal amount");

                } else {
                    heatCoolPhrase.append("normal");
                }
            }

            // If the degree days amount is normal, append "of days."
            // Otherwise append ". The normal number of heating/cooling degree
            // days is __."
            if (degreeFlag.isNorm()
                    && normValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                int deltaDegDay = degreeValue - normValue;

                if (deltaDegDay != 0) {
                    if (degreeFlag.isDeparture()) {
                        heatCoolPhrase.append(
                                " of " + String.format(INT_COMMAS, normValue)
                                        + SPACE + dayDays(normValue != 1));
                    } else {
                        heatCoolPhrase
                                .append(".  The normal number of " + heatCool
                                        + " degree days" + periodString + " is "
                                        + String.format(INT_COMMAS, normValue));
                    }
                }
            }

            heatCoolPhrase.append(PERIOD + SPACE + SPACE);
        }
        return heatCoolPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_precip.f.
     * 
     * <pre>
    *   October  1999     Dan Zipper TDL/PRC
    *   November 1999     Bonnie Reed TDL/PRC
    *
    *   Purpose:  This routine is the main driver for building the period
    *             precipitation sentences for the NOAA Weather Radio (NWR).
    *             There are three basic types of precipitation sentences:
    *             liquid precip (i.e., rainfall) sentences, snowfall
    *             sentences, and depth of snow on the ground sentences.
    * 
    *   Variables
    *
    *      Input
    *    do_liquid_precip - structure containing the flags which control generation
    *                       of various portions of the liquid precip sentences.
    *                       See the structure definition for more details.
    *      do_snow_precip - structure containing the flags which control generation
    *                       of various portions of the snowfall sentences.
    *                       See the structure definition for more details.
    *             h_climo - structure containing historical climatology for a given
    *                       date.  See the structure definition for more details
    *              p_data - structure containing the observed climate data.
    *                       See the structure definition for more details
    *               itype - declares either a monthly report or seasonal report
    *       global_values - structure containing the definitions of different
    *                       global elements.
    *       precip_phrase - character buffer containing the precip
    *                       sentences structure containing the observed climate
    *                       data.  See the structure definition for
    *                       more details.
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
    *         TYPE_period_climo.I - defines the derived TYPE used to
    *                               store the historical climatological
    *                               record for a given site for a given
    *                               day.  Uses derived TYPE "wind" so
    *                               that INCLUDE file must come before
    *                               this one
    *                 TYPE_date.I - defines the derived TYPE "date";
    *                               This INCLUDE file must always 
    *                               come before any other INCLUDE file
    *                               which uses the "date" TYPE
    *          TYPE_period_data.I - defines the derived TYPE used to 
    *                               store the observed climatological
    *                               data.  Note that INCLUDE file
    *                               defining the wind TYPE must be 
    *                               specified before this file
    * TYPE_do_p_weather_element.I - defines the derived TYPE used to
    *                               hold the controlling logic for
    *                               producing sentences/reports for
    *                               a given variable
    *                 TYPE_time.I - defines the derived TYPE used to
    *                               specify the time
    *                 TYPE_wind.I - defines the derived TYPE used to
    *                               specify wind speed,and direction
    *
    *      Non-system routines used
    *     build_NWR_period_liquid_precip - builds the sentences for rainfall
    *       build_NWR_snow_precip - builds the sentences for snowfall
    *        build_NWR_snow_depth - builds the sentences for snowdepth
    *
    *      Non-system functions used
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *    5/07/01   Doug Murphy            Made code more efficient - not necessary
    *                                     to pass along entire arrays
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRPeriodPrecip(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        // Build liquid precip phrases
        precipPhrase.append(buildNWRLiquidPrecip(reportData));

        precipPhrase.append(buildNWRLiquidFraction(reportData));

        precipPhrase.append(buildNWRLiquidStormAvg(reportData));

        // Build snow precip phrases
        if (reportWindow(currentSettings.getControl().getSnowDates())) {
            precipPhrase.append(buildNWRSnowPrecip(reportData));

            precipPhrase.append(buildNWRSnowTotals(reportData));

            precipPhrase.append(buildNWRSnowWaterGround(reportData));

        }

        return precipPhrase.toString();
    }

    /**
     * Migrated from build_NWR_p_snow_water_ground.f.
     * 
     * <pre>
    *    November 1999    Bonnie Reed    PRC/TDL
    *
    *   Purpose:  This routine controls building the period rainfall sentences in 
    *             the NOAA Weather Radio (NWR) portion of the climate
    *             program.  Sentence formats are documented in the CLIMATE
    *             Design Notebook.
    *
    *             Why do we test on half_snow?
    *               Generally, it is bad coding practice to test on whether
    *               two REAL numbers equal each other.  This is because there
    *               may be small differences between the the numbers which
    *               aren't immediately apparent.  The precision to which we
    *               we measure snowfall is 0.01 inches.  We will test 
    *               to see if two snowfall measurements are within 0.005,
    *               or half_snow of one another.  We will consider such values
    *               as being equal if they are within this.
    * 
    *
    *   Variables
    *
    *      Input
    *        do_snow_precip - structure containing the flags which control the 
    *                         generation of various portions of the precip sentences.
    *                         See the structure definition for more details.
    *                 itype - declares either a monthly report or seasonal report
    *               h_climo - structure containing historical climatology
    *                         for a given date.  See structure definition for more
    *                         details.
    *                p_data - structure containing the observed climate data for the
    *                         month or season.
    *         precip_phrase - character buffer containing the precip
    *                         sentences structure containing the observed climate
    *                         data.  See the structure definition for
    *                         more details.
    *
    *      Output
    *        precip_phrase - character buffer containing the precip sentences
    *
    *      Local
    *            c_norm - character form of the normal value
    *            c_snow - character form of the snow value
    *             c_val - character form of a value
    *       date_length - length of the date_phrase
    *       date_phrase - character string holding dates of occurrence
    *             delta - the difference between two values
    *       delta_value - the difference between two values
    *        difference - character string set to either "above" or "below"
    *            hr_beg - character form of the beginning hour
    *              ilen - number of characters in a string
    *             ileni - number of characters in a string
    *           iplural - character string either set to "inch" or "inches"
    *               mon - character string of the word month (January)
    *          num_char - Number of characters in a string.
    *       num_decimal - number that accounts for the decimal place
    *        num_digits - The number of digits in a given number; 
    *                     necessary for the conversion of numeric values 
    *                     to character.
    *       num_digits1 - The number of digits in a given number; 
    *                     necessary for the conversion of numeric values 
    *                     to character.
    *            plural - character either set to "day" or "days"
    *         val_delta - the difference between two values
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *       date_sentence - FORTRAN function: used to produce sentence portion
    *                       pertaining to dates of occurrence
    *         decide_inch - FORTRAN function: returns either the singular or
    *                       plural version of the word "inch" as well as the
    *                       number of characters in the word.
    *
    *
    *   Include Files
    *
    *   MODIFICATION HISTORY
    *   --------------------
    *     2/12/01  Doug Murphy              Avg. snow depth is reported as an int
    *                                       even though it remains a real in the structure
    *     5/01/01  Doug Murphy              Added enhancement to include all three
    *                                       dates of occurrence for the max snow
    *                                       depth if available
    *     3/27/03  Gary Battel              Converted trace of snow used in
    *                                       calculations from -1, so that it isn't
    *                                       considered less than 0
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRSnowWaterGround(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        SnowControlFlags snowFlag = currentSettings.getControl()
                .getSnowControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        float snowWaterMeasured = periodData.getSnowWater() < 0 ? 0
                : periodData.getSnowWater(),
                snowWaterJuly1 = periodData.getSnowWaterJuly1() < 0 ? 0
                        : periodData.getSnowWaterJuly1(),
                snowWaterNorm = hClimo.getSnowWaterPeriodNorm() < 0 ? 0
                        : hClimo.getSnowWaterPeriodNorm(),
                snowWaterNormJuly1 = hClimo.getSnowWaterJuly1Norm() < 0 ? 0
                        : hClimo.getSnowWaterJuly1Norm();

        // "The snow water equivalent is <__ inch(es)>/<a trace> which is
        // <above/below the normal amount>/<which is normal>
        if (snowFlag.getSnowWaterTotal().isMeasured() && periodData
                .getSnowWater() != ParameterFormatClimate.MISSING) {
            String amount = periodData.getSnowWater() < 0 ? A_TRACE
                    : (String.format(FLOAT_TWO_DECIMALS1,
                            periodData.getSnowWater()) + " inches");
            precipPhrase.append("The snow water equivalent is " + amount);

            if (snowFlag.getSnowWaterTotal().isDeparture() && hClimo
                    .getSnowWaterPeriodNorm() != ParameterFormatClimate.MISSING) {
                float valDelta = snowWaterMeasured - snowWaterNorm;

                if (valDelta != 0) {
                    precipPhrase.append(" which is " + aboveBelow(valDelta > 0)
                            + " the normal amount");
                } else {
                    precipPhrase.append(" which is normal");
                }
            }

            // ". The normal snow water equivalent is __ inch(es)."
            if (snowFlag.getSnowWaterTotal().isNorm() && hClimo
                    .getSnowWaterPeriodNorm() != ParameterFormatClimate.MISSING) {
                float valDelta = snowWaterMeasured - snowWaterNorm;

                if (valDelta != 0) {
                    if (snowFlag.getSnowWaterTotal().isDeparture()) {
                        precipPhrase.append(" of ");
                    } else {
                        precipPhrase.append(
                                ". The normal snow water equivalent is ");
                    }
                    precipPhrase.append(
                            String.format(FLOAT_TWO_DECIMALS1, snowWaterNorm)
                                    + " inches");
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);

        }

        // "The snow water equivalent since July 1 is <__ inch(es)>/<a trace>
        // which is <the normal amount>/<normal>"
        if (snowFlag.getSnowWaterJuly1().isMeasured() && periodData
                .getSnowWaterJuly1() != ParameterFormatClimate.MISSING) {
            String amount = periodData
                    .getSnowWaterJuly1() < 0
                            ? A_TRACE
                            : (String.format(FLOAT_TWO_DECIMALS1,
                                    periodData.getSnowWaterJuly1())
                                    + " inches");
            precipPhrase.append(
                    "The snow water equivalent since July 1 is " + amount);

            if (snowFlag.getSnowWaterJuly1().isDeparture() && hClimo
                    .getSnowWaterJuly1Norm() != ParameterFormatClimate.MISSING) {
                float valDelta = snowWaterJuly1 - snowWaterNormJuly1;

                if (valDelta != 0) {
                    precipPhrase.append(" which is " + aboveBelow(valDelta > 0)
                            + " the normal amount");
                } else {
                    precipPhrase.append(" which is normal");
                }
            }

            if (snowFlag.getSnowWaterJuly1().isNorm() && hClimo
                    .getSnowWaterJuly1Norm() != ParameterFormatClimate.MISSING) {
                float valDelta = snowWaterJuly1 - snowWaterNormJuly1;

                if (valDelta != 0) {
                    if (snowFlag.getSnowWaterJuly1().isDeparture()) {
                        precipPhrase.append(" of ");
                    } else {
                        precipPhrase.append(
                                ". The normal snow water equivalent since July 1 is ");
                    }
                    precipPhrase.append(String.format(FLOAT_TWO_DECIMALS1,
                            snowWaterNormJuly1) + " inches");
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);

        }

        // "The deepest snow depth observed was <__ inch(es)>/<a trace> and
        // occurred on."
        if (snowFlag.getSnowDepthMax().isMeasured() && periodData
                .getSnowGroundMax() != ParameterFormatClimate.MISSING) {

            String amount = (periodData
                    .getSnowGroundMax() == ParameterFormatClimate.TRACE)
                            ? A_TRACE
                            : (periodData.getSnowGroundMax() + SPACE
                                    + inchInches(periodData
                                            .getSnowGroundMax() != 1));
            precipPhrase
                    .append("The deepest snow depth observed was " + amount);

            if (snowFlag.getSnowDepthMax().isTimeOfMeasured()
                    && !periodData.getSnowGroundMaxDateList().isEmpty()
                    && periodData.getSnowGroundMaxDateList().get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnowGroundMaxDateList().get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE) {
                precipPhrase.append(" and occurred on "
                        + dateSentence(periodData.getSnowGroundMaxDateList()));
            }
            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "The average snow depth observed for the period was <__ inch(es)>/<a
        // trace> which is <normal>/<the normal average>
        if (snowFlag.getSnowDepthAvg().isMeasured() && periodData
                .getSnowGroundMean() != ParameterFormatClimate.MISSING) {
            int intNorm = ClimateUtilities.nint(hClimo.getSnowGroundNorm());
            int intObs = ClimateUtilities.nint(periodData.getSnowGroundMean());

            String amount = intObs == ParameterFormatClimate.TRACE ? A_TRACE
                    : SPACE + inchInches(intObs + intObs != 1);
            precipPhrase
                    .append("The average snow depth observed for the period was "
                            + amount);

            if (snowFlag.getSnowDepthAvg().isDeparture()
                    && hClimo
                            .getSnowGroundNorm() != ParameterFormatClimate.MISSING
                    && periodData
                            .getSnowGroundMean() != ParameterFormatClimate.TRACE
                    && hClimo
                            .getSnowGroundNorm() != ParameterFormatClimate.TRACE) {

                int deltaInt = intObs - intNorm;
                int intValue = Math.abs(deltaInt);
                if (deltaInt != 0) {
                    precipPhrase.append(" which is " + aboveBelow(deltaInt > 0)
                            + SPACE + intValue + SPACE
                            + inchInches(intValue != 1)
                            + " the normal average");
                } else {
                    precipPhrase.append(" which is normal");
                }
            }

            // If the norm and mean are a trace append
            // " which is normal." Otherwise append "The normal average snow
            // depth is <a trace>/<__ inches>."
            if (snowFlag.getSnowDepthAvg().isNorm() && hClimo
                    .getSnowGroundNorm() != ParameterFormatClimate.MISSING) {

                if (hClimo.getSnowGroundNorm() == ParameterFormatClimate.TRACE
                        && periodData
                                .getSnowGroundMean() == ParameterFormatClimate.TRACE) {

                    precipPhrase.append(" which is normal");
                } else if (hClimo
                        .getSnowGroundNorm() == ParameterFormatClimate.TRACE
                        && periodData
                                .getSnowGroundMean() != ParameterFormatClimate.TRACE) {
                    precipPhrase.append(
                            ".  The normal average snow depth is a trace");
                } else {
                    int deltaInt = intObs - intNorm;

                    if (deltaInt != 0) {

                        if (snowFlag.getSnowDepthAvg().isDeparture()
                                && periodData
                                        .getSnowGroundMean() != ParameterFormatClimate.TRACE) {
                            precipPhrase.append(" of ");
                        } else {
                            precipPhrase.append(
                                    ".  The normal average snow depth is ");
                        }
                        precipPhrase.append(
                                intNorm + SPACE + inchInches(intNorm != 1));

                    }
                }
            }
            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        return precipPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_snow_totals.f
     * 
     * <pre>
    *    November 1999    Bonnie Reed    PRC/TDL
    *
    *   Purpose:  This routine controls building the period rainfall sentences in 
    *             the NOAA Weather Radio (NWR) portion of the climate
    *             program.  Sentence formats are documented in the CLIMATE
    *             Design Notebook.
    *
    *             Why do we test on half_snow?
    *               Generally, it is bad coding practice to test on whether
    *               two REAL numbers equal each other.  This is because there
    *               may be small differences between the the numbers which
    *               aren't immediately apparent.  The precision to which we
    *               we measure snowfall is 0.01 inches.  We will test 
    *               to see if two snowfall measurements are within 0.005,
    *               or half_snow of one another.  We will consider such values
    *               as being equal if they are within this.
    * 
    *
    *   Variables
    *
    *      Input
    *        do_snow_precip - structure containing the flags which control the 
    *                         generation of various portions of the precip sentences.
    *                         See the structure definition for more details.
    *                 itype - declares either a monthly report or seasonal report
    *               h_climo - structure containing historical climatology
    *                         for a given date.  See structure definition for more
    *                         details.
    *                p_data - structure containing the observed climate data for the
    *                         month or season.
    *         global_values - structure containing the definitions of different
    *                         global elements.
    *         precip_phrase - character buffer containing the precip
    *                         sentences structure containing the observed climate
    *                         data.  See the structure definition for
    *                         more details.
    *
    *     Output
    *         precip_phrase - character buffer containing the precip sentences
    *
    *     Local
    *             c_day - character form of the day value
    *            c_norm - character form of the normal value
    *             c_rec - character form of the record value
    *             c_val - character form of a value
    *            c_year - character form of the year value
    *       date_phrase - portion of sentence dealing with dates of occurrence
    *       date_length - character length of date_phrase
    *        difference - character string set to either "above" or "below"
    *              ilen - number of characters in a string
    *       num_decimal - number that accounts for the decimal place
    *              more - logical value when reporting the record
    *          num_char - Number of characters in a string.
    *        num_digits - The number of digits in a given number; 
    *                        necessary for the conversion of numeric values 
    *                        to character.
    *            plural - character either set to "day" or "days"
    *         val_delta - the difference between two values
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *      dates_sentence - FORTRAN function: builds the dates of occurrence
    *                       section for 24h and storm snowfall
    *
    *          decide_day - FORTRAN function: returns either the singular or
    *                       plural version of the word "day" as well as the
    *                       number of characters in the word.
    *
    *  MODIFICATION HISTORY
    *  ====================
    *    4/18/01   Doug Murphy            The normal Snow GE 1.0 in. has
    *                                     changed to a real value
    *    5/07/01   Doug Murphy            Enhanced code to report all three dates
    *                                     of occurrence for 24h and storm snow...
    *                                     Made code more efficient - not necessary
    *                                     to pass along entire arrays
    *    4/10/03   Gary Battel            24-hour snowfall of 0.0 should not 
    *                                     be considered a record
    *
    *  Include Files
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRSnowTotals(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        SnowControlFlags snowFlag = currentSettings.getControl()
                .getSnowControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        // "There were no days with snowfall greater than or equal to __ inches"
        // OR "__ inches of snow or greater fell on __ day(s)"

        // ..."which is <normal. The normal amount is __ days for the
        // period.>/<above/below the normal amount of __ days.>"
        if (snowFlag.getSnowGE100().isMeasured() && periodData
                .getNumSnowGreaterThan1() != ParameterFormatClimate.MISSING) {

            // Use what 1.0 inch if the value is 0.
            if (periodData.getNumSnowGreaterThan1() == 0) {
                precipPhrase.append(
                        "There were no days with snowfall greater than or equal to 1.0 inches");
            } else {
                precipPhrase.append("1.0 inches of snow or greater fell on "
                        + periodData.getNumSnowGreaterThan1() + SPACE
                        + dayDays(periodData.getNumSnowGreaterThan1() != 1));
            }

            if (snowFlag.getSnowGE100().isDeparture() && hClimo
                    .getNumSnowGE1Norm() != ParameterFormatClimate.MISSING) {
                float valDelta = (float) periodData.getNumSnowGreaterThan1()
                        - hClimo.getNumSnowGE1Norm();

                if (valDelta != 0) {
                    precipPhrase.append(" which is ");

                    precipPhrase.append(
                            String.format(FLOAT_ONE_DECIMAL, Math.abs(valDelta))
                                    + SPACE + aboveBelow(valDelta > 0)
                                    + " the normal amount");

                } else {
                    precipPhrase.append(" which is normal");
                }
            }

            if (snowFlag.getSnowGE100().isNorm() && hClimo
                    .getNumSnowGE1Norm() != ParameterFormatClimate.MISSING) {
                float valDelta = (float) periodData.getNumSnowGreaterThan1()
                        - hClimo.getNumSnowGE1Norm();

                if (valDelta != 0) {

                    if (snowFlag.getSnowGE100().isDeparture()) {
                        precipPhrase
                                .append(" of "
                                        + String.format(FLOAT_ONE_DECIMAL,
                                                hClimo.getNumSnowGE1Norm())
                                        + " days");
                    } else {
                        precipPhrase.append(".  The normal amount is "
                                + String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNumSnowGE1Norm())
                                + " days for the period");
                    }
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "There were no days with snowfall greater than or equal to <__
        // inches>/<a trace>."
        if (snowFlag.getSnowGEP1().isMeasured()
                && periodData
                        .getNumSnowGreaterThanS1() != ParameterFormatClimate.MISSING
                && globalConfig.getS1() != ParameterFormatClimate.MISSING) {

            if (periodData.getNumSnowGreaterThanS1() == 0) {
                String amount = (globalConfig
                        .getS1() == ParameterFormatClimate.TRACE) ? A_TRACE
                                : (String.format(FLOAT_ONE_DECIMAL,
                                        globalConfig.getS1()) + " inches");

                precipPhrase
                        .append("There were no days with snowfall greater than or equal to "
                                + amount);

            } else {
                String amount = (globalConfig
                        .getS1() == ParameterFormatClimate.TRACE)
                                ? StringUtils.capitalize(A_TRACE)
                                : (String.format(FLOAT_ONE_DECIMAL,
                                        globalConfig.getS1()) + " inches");

                precipPhrase.append(amount + " or more of snow fell on "
                        + periodData.getNumSnowGreaterThanS1() + SPACE
                        + dayDays(true) + " this period");
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "The maximum 24 hour snowfall was <__ inches>/<a trace>."
        if (snowFlag.getSnow24hr().isMeasured()
                && periodData.getSnowMax24H() != ParameterFormatClimate.MISSING
                && periodData.getSnowMax24H() != 0) {

            String amount = (periodData
                    .getSnowMax24H() == ParameterFormatClimate.TRACE) ? A_TRACE
                            : (String.format(FLOAT_ONE_DECIMAL,
                                    periodData.getSnowMax24H()) + " inches");

            precipPhrase.append(
                    "The " + MAXIMUM + " 24 hour snowfall was " + amount);

            if (snowFlag.getSnow24hr().isTimeOfMeasured()
                    && !periodData.getSnow24HDates().isEmpty()

                    && periodData.getSnow24HDates().get(0).getStart()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnow24HDates().get(0).getEnd()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnow24HDates().get(0).getStart()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnow24HDates().get(0).getEnd()
                            .getDay() != ParameterFormatClimate.MISSING_DATE) {

                precipPhrase
                        .append(datesSentence(periodData.getSnow24HDates()));
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);

            // "<This ties/breaks the previous record of>/<The record 24 hour
            // snowfall is> <__ inches>/<a trace> which was <set>/<last set> in
            // <year>."
            if (snowFlag.getSnow24hr().isRecord() && hClimo
                    .getSnowMax24HRecord() != ParameterFormatClimate.MISSING) {

                float snowRecord = hClimo.getSnowMax24HRecord();
                if (snowRecord < 0) {
                    snowRecord = 0.05f;
                }

                if (snowRecord < periodData.getSnowMax24H()
                        || (hClimo.getSnowMax24HRecord() == 0 && periodData
                                .getSnowMax24H() == ParameterFormatClimate.TRACE)) {

                    precipPhrase.append("This breaks the previoud record of ");
                } else if (hClimo.getSnowMax24HRecord()
                        - periodData.getSnowMax24H() < 0.02f
                        && periodData.getSnowMax24H() != 0) {
                    precipPhrase.append("This ties the previous record of ");
                } else {
                    precipPhrase.append("The record 24 hour snowfall is ");
                }

                precipPhrase.append(
                        (hClimo.getSnowMax24HRecord() == ParameterFormatClimate.TRACE)
                                ? " a trace"
                                : (String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getSnowMax24HRecord())
                                        + " inches"));

                if (snowFlag.getSnow24hr().isRecordYear()
                        && !hClimo.getSnow24HList().isEmpty()
                        && hClimo.getSnow24HList().get(0).getStart()
                                .getYear() != ParameterFormatClimate.MISSING) {

                    if (hClimo.getSnow24HList().get(1).getStart()
                            .getYear() == ParameterFormatClimate.MISSING) {
                        precipPhrase.append(" which was set in ");
                    } else {
                        precipPhrase.append(" which was last set in ");
                    }

                    precipPhrase.append(hClimo.getSnow24HList().get(0)
                            .getStart().getYear());
                }

                precipPhrase.append(PERIOD + SPACE + SPACE);
            }

        }

        // "The highest total storm snowfall was <__ inches>/<a trace>."
        if (snowFlag.getSnowStormMax().isMeasured()
                && periodData
                        .getSnowMaxStorm() != ParameterFormatClimate.MISSING
                && periodData.getSnowMaxStorm() != 0) {

            String amount = periodData
                    .getSnowMaxStorm() == ParameterFormatClimate.TRACE ? A_TRACE
                            : (String.format(FLOAT_ONE_DECIMAL,
                                    periodData.getSnowMaxStorm()) + " inches");

            precipPhrase
                    .append(" The highest total storm snowfall was " + amount);

            if (snowFlag.getSnowStormMax().isTimeOfMeasured()
                    && !periodData.getSnowStormList().isEmpty()
                    && periodData.getSnowStormList().get(0).getStart()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnowStormList().get(0).getEnd()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnowStormList().get(0).getStart()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnowStormList().get(0).getEnd()
                            .getDay() != ParameterFormatClimate.MISSING_DATE) {
                precipPhrase
                        .append(datesSentence(periodData.getSnowStormList()));

            }
            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        return precipPhrase.toString();
    }

    /**
     * Migrated build_NWR_period_snow_precip.f.
     * 
     * <pre>
    *    October  1999    Dan Zipper     PRC/TDL
    *    November 1999    Bonnie Reed    PRC/TDL
    *
    *   Purpose:  This routine controls building the period rainfall sentences in 
    *             the NOAA Weather Radio (NWR) portion of the climate
    *             program.  Sentence formats are documented in the CLIMATE
    *             Design Notebook.
    *
    *             Why do we test on half_snow?
    *               Generally, it is bad coding practice to test on whether
    *               two REAL numbers equal each other.  This is because there
    *               may be small differences between the the numbers which
    *               aren't immediately apparent.  The precision to which we
    *               we measure snowfall is 0.01 inches.  We will test 
    *               to see if two snowfall measurements are within 0.005,
    *               or half_snow of one another.  We will consider such values
    *               as being equal if they are within this.
    * 
    *
    *   Variables
    *
    *      Input
    *        do_snow_precip - structure containing the flags which control the 
    *                         generation of various portions of the precip sentences.
    *                         See the structure definition for more details.
    *                 itype - declares either a monthly report or seasonal report
    *               h_climo - structure containing historical climatology
    *                         for a given date.  See structure definition for more
    *                         details.
    *                p_data - structure containing the observed climate data for the
    *                         month or season.
    *         precip_phrase - character buffer containing the precip
    *                         sentences structure containing the observed climate
    *                         data.  See the structure definition for
    *                         more details.
    *
    *      Output
    *        precip_phrase - character buffer containing the precip sentences
    *
    *      Local
    *               c_day - character form of the day value
    *              c_norm - character form of the normal value
    *               c_rec - character form of the record value
    *              c_snow - character form of a snow value
    *              c_year - character form of the year value
    *               delta - difference between two values
    *           delta_val - the difference between two values
    *         delta_value - the difference between two values
    *          difference - character string set to either "above" or "below"
    *          idelta_val - the difference between two values
    *        idelta_value - the difference between two values
    *         num_decimal - number that accounts for the decimal place
    *                more - logical value when reporting the record
    *              plural - character either set to "day" or "days"
    *            num_char - Number of characters in a string.
    *          num_digits - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *          decide_day - FORTRAN function: returns either the singular or
    *                       plural version of the word "day" as well as the
    *                       number of characters in the word.
    *
    *
    *   Include Files
    *
    *   MODIFICATION HISTORY
    *   --------------------
    *     01/03/01   Doug Murphy         Fixed check of multiple years'
    *                                    record occurrences
    *      3/22/01   Doug Murphy         Record values were not being denoted
    *                                    for cases where observed is T and record
    *                                    is 0 and observed is > T and record is T
    *      4/12/01   Doug Murphy         Did some reworking of code for efficiency
    *                                    and added calls to add a comma if the
    *                                    snowfall is over 1000 inches for some
    *                                    crazy reason
    *      4/18/01   Doug Murphy         The normal snow GE TR has
    *                                    changed to a real value
    *      4/30/01   Doug Murphy         Changed/added logic to include the
    *                                    departure from normal when trace is
    *                                    involved
    *      5/07/01   Doug Murphy         Made code more efficient - not necessary
    *                                    to pass along entire arrays
    *      4/3/03    Gary Battel         Snowfall of 0.0 should not be considered
    *                                    a record.
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRSnowPrecip(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        SnowControlFlags snowFlag = currentSettings.getControl()
                .getSnowControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        // "<A trace of>/<No> /<A total of __ inches> snow fell during the
        // period
        if (snowFlag.getSnowTotal().isMeasured() && periodData
                .getSnowTotal() != ParameterFormatClimate.MISSING) {

            float deltaValue = 0;
            if (periodData.getSnowTotal() == ParameterFormatClimate.TRACE) {
                precipPhrase.append("A trace of ");
            } else if (Math
                    .abs(periodData
                            .getSnowTotal()) < ParameterFormatClimate.HALF_SNOW
                    && periodData
                            .getSnowTotal() != ParameterFormatClimate.TRACE) {
                precipPhrase.append("No ");
            } else {

                String snowAmount = (periodData.getSnowTotal() < 1) ? "0"
                        : String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                periodData.getSnowTotal());

                precipPhrase.append("A total of " + snowAmount + " inches ");
            }

            precipPhrase.append("snow fell during the period");

            // "... The departure from normal is 0.00"
            // OR
            // ... which is <__ inches above/below the normal amount>/<normal>
            if (snowFlag.getSnowTotal().isDeparture() && hClimo
                    .getSnowPeriodNorm() != ParameterFormatClimate.MISSING) {

                if (hClimo.getSnowPeriodNorm() == 0 && periodData
                        .getSnowTotal() == ParameterFormatClimate.TRACE) {
                    precipPhrase.append(". The departure from normal is 0.00");
                    deltaValue = 0;
                } else {
                    if (hClimo
                            .getSnowPeriodNorm() == ParameterFormatClimate.TRACE
                            && periodData.getSnowTotal() == 0) {
                        deltaValue = 0;
                    } else if (hClimo
                            .getSnowPeriodNorm() != ParameterFormatClimate.TRACE
                            && periodData
                                    .getSnowTotal() == ParameterFormatClimate.TRACE) {
                        deltaValue = -1 * hClimo.getSnowPeriodNorm();
                    } else if (hClimo
                            .getSnowPeriodNorm() == ParameterFormatClimate.TRACE
                            && periodData
                                    .getSnowTotal() != ParameterFormatClimate.TRACE) {
                        deltaValue = periodData.getSnowTotal();
                    } else {
                        deltaValue = periodData.getSnowTotal()
                                - hClimo.getSnowPeriodNorm();
                    }

                    precipPhrase.append(" which is ");

                    if (deltaValue != 0) {
                        precipPhrase
                                .append(String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                        Math.abs(deltaValue)) + " inches "
                                + aboveBelow(deltaValue > 0)
                                + " the normal amount");
                    } else {
                        precipPhrase.append("normal");
                    }
                }
            }

            // If the string appended above is "the normal amount" append
            // "of <a trace>/<__ inches>
            // Otherwise append "The normal amount of snowfall is __."
            if (snowFlag.getSnowTotal().isNorm() && hClimo
                    .getSnowPeriodNorm() != ParameterFormatClimate.MISSING) {

                if (snowFlag.getSnowTotal().isDeparture()) {

                    if (deltaValue != 0) {
                        String amount = (hClimo
                                .getSnowPeriodNorm() != ParameterFormatClimate.TRACE)
                                        ? A_TRACE
                                        : String.format(FLOAT_ONE_DECIMAL,
                                                hClimo.getSnowPeriodNorm());
                        precipPhrase.append(" of " + amount);
                    }
                } else {
                    String amount = (hClimo
                            .getSnowPeriodNorm() != ParameterFormatClimate.TRACE)
                                    ? A_TRACE
                                    : (String.format(FLOAT_ONE_DECIMAL,
                                            hClimo.getSnowPeriodNorm())
                                            + " inches");
                    precipPhrase.append(
                            ".  The normal amount of snowfall is " + amount);

                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);

            // "This breaks/ties the previous record of <__ inches>/<a trace>
            // which was set in <year>."
            // OR "The record amount of snowfall is <__ inches>/<a trace> which
            // was last set in <year>."
            if (snowFlag.getSnowTotal().isRecord() && hClimo
                    .getSnowPeriodRecord() != ParameterFormatClimate.MISSING) {

                float snowRecord = hClimo.getSnowPeriodRecord();

                if (snowRecord < 0) {
                    snowRecord = 0.05f;
                }

                if (snowRecord < periodData.getSnowTotal()
                        || (hClimo.getSnowPeriodRecord() == 0 && periodData
                                .getSnowTotal() == ParameterFormatClimate.TRACE)) {
                    precipPhrase.append("This breaks the previous record of ");
                } else if (hClimo.getSnowPeriodRecord()
                        - periodData.getSnowTotal() < 0.02f
                        && periodData.getSnowTotal() == 0) {
                    precipPhrase.append("This ties the previous record of ");
                } else {
                    precipPhrase.append("The record amount of snowfall is ");
                }

                precipPhrase.append(
                        (hClimo.getSnowPeriodRecord() != ParameterFormatClimate.TRACE)
                                ? A_TRACE
                                : (String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                        hClimo.getSnowPeriodRecord())
                                        + " inches"));

                if (snowFlag.getSnowTotal().isRecordYear()
                        && !hClimo.getSnowPeriodMaxYearList().isEmpty()
                        && hClimo.getSnowPeriodMaxYearList().get(0)
                                .getYear() != ParameterFormatClimate.MISSING) {

                    if ((hClimo.getSnowPeriodMaxYearList().size() > 1
                            && hClimo.getSnowPeriodMaxYearList().get(1)
                                    .getYear() != ParameterFormatClimate.MISSING)
                            || (hClimo.getSnowPeriodMaxYearList().size() > 2
                                    && hClimo.getSnowPeriodMaxYearList().get(2)
                                            .getYear() != ParameterFormatClimate.MISSING)) {
                        precipPhrase.append(" which was last set in ");

                    } else {
                        precipPhrase.append(" which was set in ");
                    }

                    precipPhrase.append(
                            hClimo.getSnowPeriodMaxYearList().get(0).getYear()
                                    + PERIOD + SPACE + SPACE);
                } else {
                    precipPhrase.append(PERIOD + SPACE + SPACE);
                }
            }
        }

        // "<A total of __ inches>/<A trace of> snow has fallen since July 1...
        if (snowFlag.getSnowJuly1().isMeasured() && periodData
                .getSnowJuly1() != ParameterFormatClimate.MISSING) {
            if (periodData.getSnowJuly1() == ParameterFormatClimate.TRACE) {
                precipPhrase.append("A trace of ");
            } else if (Math
                    .abs(periodData
                            .getSnowJuly1()) < ParameterFormatClimate.HALF_SNOW
                    && periodData
                            .getSnowJuly1() != ParameterFormatClimate.TRACE) {
                precipPhrase.append("No ");
            } else {
                String snowAmount = periodData.getSnowJuly1() < 1 ? "0"
                        : String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                periodData.getSnowJuly1());

                precipPhrase.append("A total of " + snowAmount + " inches ");

            }
            precipPhrase.append("snow has fallen since July 1");

            if (snowFlag.getSnowJuly1().isDeparture()
                    && hClimo
                            .getSnowJuly1Norm() != ParameterFormatClimate.MISSING
                    && periodData.getSnowJuly1() != ParameterFormatClimate.TRACE
                    && hClimo
                            .getSnowJuly1Norm() != ParameterFormatClimate.TRACE) {

                float deltaValue = periodData.getSnowJuly1()
                        - hClimo.getSnowJuly1Norm();

                precipPhrase.append(" which is ");

                if (deltaValue != 0) {
                    precipPhrase.append(String.format(FLOAT_COMMAS_ONE_DECIMAL,
                            Math.abs(deltaValue)) + " inches "
                            + aboveBelow(deltaValue > 0)
                            + " the normal amount");
                } else {
                    precipPhrase.append("normal");
                }
            }

            if (snowFlag.getSnowJuly1().isNorm() && hClimo
                    .getSnowJuly1Norm() != ParameterFormatClimate.MISSING) {
                if (hClimo.getSnowJuly1Norm() == ParameterFormatClimate.TRACE
                        && periodData
                                .getSnowJuly1() == ParameterFormatClimate.TRACE) {
                    precipPhrase.append(" which is normal");
                } else if (hClimo
                        .getSnowJuly1Norm() == ParameterFormatClimate.TRACE
                        && periodData
                                .getSnowJuly1() != ParameterFormatClimate.TRACE) {
                    precipPhrase.append(
                            ".  The normal amount of snowfall since July 1 is a trace");
                } else {
                    float deltaValue = periodData.getSnowJuly1()
                            - hClimo.getSnowJuly1Norm();

                    if (deltaValue != 0) {
                        if (snowFlag.getSnowJuly1().isDeparture() && periodData
                                .getSnowJuly1() != ParameterFormatClimate.TRACE) {
                            precipPhrase.append(" of "
                                    + String.format(FLOAT_ONE_DECIMAL,
                                            hClimo.getSnowJuly1Norm())
                                    + " inches");
                        } else {
                            precipPhrase
                                    .append(".  The normal amount of snowfall since July 1 is "
                                            + String.format(FLOAT_ONE_DECIMAL,
                                                    hClimo.getSnowJuly1Norm())
                                            + " inches");
                        }
                    }
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        if (snowFlag.getSnowAny().isMeasured() && periodData
                .getNumSnowGreaterThanTR() != ParameterFormatClimate.MISSING) {

            if (periodData.getNumSnowGreaterThanTR() == 0 || periodData
                    .getNumSnowGreaterThanTR() == ParameterFormatClimate.TRACE) {

                precipPhrase
                        .append("There have been no days of measurable snow");
            } else {
                precipPhrase
                        .append(periodData.getNumSnowGreaterThanTR() + SPACE
                                + dayDays(periodData
                                        .getNumSnowGreaterThanTR() != 1)
                        + " of measurable snow "
                        + wasWere(periodData.getNumSnowGreaterThanTR() != 1)
                        + " observed");

            }

            if (snowFlag.getSnowAny().isDeparture() && hClimo
                    .getNumSnowGETRNorm() != ParameterFormatClimate.MISSING) {
                float deltaValue = (float) periodData.getNumSnowGreaterThanTR()
                        - hClimo.getNumSnowGETRNorm();

                precipPhrase.append(" which is ");

                if (deltaValue != 0) {
                    precipPhrase.append(String.format(FLOAT_ONE_DECIMAL,
                            Math.abs(deltaValue)) + SPACE
                            + aboveBelow(deltaValue > 0)
                            + " the normal amount");
                } else {
                    precipPhrase.append("normal");
                }
            }

            if (snowFlag.getSnowAny().isNorm() && hClimo
                    .getNumSnowGETRNorm() != ParameterFormatClimate.MISSING) {
                float deltaValue = (float) periodData.getNumSnowGreaterThanTR()
                        - hClimo.getNumSnowGETRNorm();

                if (deltaValue != 0) {

                    if (snowFlag.getSnowAny().isDeparture()) {
                        precipPhrase.append(" of "
                                + String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNumSnowGETRNorm())
                                + dayDays(hClimo.getNumSnowGETRNorm() != 1));
                    } else {
                        precipPhrase
                                .append(".  The normal number of days with measurable snowfall is "
                                        + String.format(FLOAT_ONE_DECIMAL,
                                                hClimo.getNumSnowGETRNorm())
                                        + dayDays(hClimo
                                                .getNumSnowGETRNorm() != 1));
                    }
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        return precipPhrase.toString();
    }

    /**
     * Migrated from build_NWR_p_liquid_fraction_val.f. &&
     * build_NWR_p_liquid_half_one.f.
     * 
     * @param reportData
     * @return
     */
    private String buildNWRLiquidFraction(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        PrecipitationControlFlags precipFlag = currentSettings.getControl()
                .getPrecipControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        // Append a phase for each of precip >= 0.01, 0.10, 0.50, and 1.00.
        precipPhrase.append(liquidFractionHelper(precipFlag.getPrecipGE01(),
                periodData.getNumPrcpGreaterThan01(),
                hClimo.getNumPrcpGE01Norm(), THRESHOLD_001));

        precipPhrase.append(liquidFractionHelper(precipFlag.getPrecipGE10(),
                periodData.getNumPrcpGreaterThan10(),
                hClimo.getNumPrcpGE01Norm(), THRESHOLD_010));

        precipPhrase.append(liquidFractionHelper(precipFlag.getPrecipGE50(),
                periodData.getNumPrcpGreaterThan50(),
                hClimo.getNumPrcpGE50Norm(), THRESHOLD_050));

        precipPhrase.append(liquidFractionHelper(precipFlag.getPrecipGE100(),
                periodData.getNumPrcpGreaterThan100(),
                hClimo.getNumPrcpGE100Norm(), THRESHOLD_100));

        // Repeat the above for user defined values.
        if (precipFlag.getPrecipGEP1().isMeasured()
                && periodData
                        .getNumPrcpGreaterThanP1() != ParameterFormatClimate.MISSING
                && globalConfig.getP1() != ParameterFormatClimate.MISSING) {
            if (periodData.getNumPrcpGreaterThanP1() == 0) {
                String amount = (globalConfig
                        .getP1() == ParameterFormatClimate.TRACE) ? A_TRACE
                                : String.format(FLOAT_TWO_DECIMALS2,
                                        globalConfig.getP1()) + " inches";
                precipPhrase.append("There were no days with " + PRECIPITATION
                        + " greater than or equal to " + amount);

            } else {
                String amount = (globalConfig
                        .getP1() == ParameterFormatClimate.TRACE)
                                ? "A trace or more"
                                : String.format(FLOAT_TWO_DECIMALS2,
                                        globalConfig.getP1()) + " inches";
                precipPhrase.append(amount + " of " + PRECIPITATION
                        + " fell on " + periodData.getNumPrcpGreaterThanP1()
                        + SPACE
                        + dayDays(periodData.getNumPrcpGreaterThanP1() != 1));

            }
        }

        if (precipFlag.getPrecipGEP2().isMeasured()
                && periodData
                        .getNumPrcpGreaterThanP2() != ParameterFormatClimate.MISSING
                && globalConfig.getP2() != ParameterFormatClimate.MISSING) {

            if (precipFlag.getPrecipGEP1().isMeasured() && periodData
                    .getNumPrcpGreaterThanP1() != ParameterFormatClimate.MISSING) {
                if (periodData.getNumPrcpGreaterThanP2() == 0) {
                    String amount = (globalConfig
                            .getP2() == ParameterFormatClimate.TRACE) ? A_TRACE
                                    : String.format(FLOAT_TWO_DECIMALS2,
                                            globalConfig.getP1()) + " inches";

                    precipPhrase.append(" and no days with " + PRECIPITATION
                            + " greater than or equal to " + amount
                            + " were observed");

                } else {
                    if (globalConfig.getP2() == ParameterFormatClimate.TRACE) {
                        precipPhrase.append(" and a trace or more of "
                                + PRECIPITATION + " fell on "
                                + periodData.getNumPrcpGreaterThanP2()
                                + dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    } else {
                        precipPhrase.append(" and "
                                + String.format(FLOAT_TWO_DECIMALS2,
                                        globalConfig.getP2())
                                + " inches of " + PRECIPITATION
                                + " or greater fell on "
                                + periodData.getNumPrcpGreaterThanP2()
                                + dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    }
                }
            } else {
                if (periodData.getNumPrcpGreaterThanP2() == 0) {
                    String amount = (globalConfig
                            .getP2() == ParameterFormatClimate.TRACE) ? A_TRACE
                                    : String.format(FLOAT_TWO_DECIMALS2,
                                            globalConfig.getP2()) + " inches";
                    precipPhrase
                            .append("There were no days with " + PRECIPITATION
                                    + " greater than or equal to " + amount);
                } else {
                    if (globalConfig.getP2() == ParameterFormatClimate.TRACE) {
                        precipPhrase.append("A trace or more of "
                                + PRECIPITATION + " fell on "
                                + periodData.getNumPrcpGreaterThanP2()
                                + dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    } else {
                        precipPhrase.append(String.format(FLOAT_TWO_DECIMALS2,
                                globalConfig.getP2()) + " inches of "
                                + PRECIPITATION + " or greater fell on "
                                + periodData.getNumPrcpGreaterThanP2()
                                + dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    }
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        } else if (precipFlag.getPrecipGEP1().isMeasured()
                && !precipFlag.getPrecipGEP2().isMeasured()) {
            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        return precipPhrase.toString();
    }

    /**
     * Helper method to reduce repetitive code in buildNWRLiquidFraction().
     * 
     * @param precipFlag
     * @param precipValue
     * @param normValue
     * @param fractionString
     * @return
     */
    private String liquidFractionHelper(ClimateProductFlags precipFlag,
            int precipValue, float normValue, String fractionString) {
        StringBuilder precipPhrase = new StringBuilder();

        // "There were no days with precipitation greater than or equal to __
        // inches"
        // OR "0.50 inches of precipitation or greater were observed on __
        // day(s)
        // OR "__ day(s) with greater than equal to __ inch(es) of precipitation
        // was/were observed."
        if (precipFlag.isMeasured()
                && precipValue != ParameterFormatClimate.MISSING) {
            if (precipValue == 0) {
                precipPhrase.append("There were no days with " + PRECIPITATION
                        + " greater than or equal to " + fractionString
                        + " inches");
            } else {
                if (fractionString.equals(THRESHOLD_050)) {
                    precipPhrase.append(fractionString + " inches of "
                            + PRECIPITATION + " or greater were observed on "
                            + precipValue + SPACE + dayDays(precipValue != 1));
                } else {
                    precipPhrase.append(precipValue + SPACE
                            + dayDays(precipValue != 1)
                            + " with greater than or equal to " + fractionString
                            + " inches of " + PRECIPITATION + SPACE
                            + wasWere(precipValue != 1) + " observed");
                }
            }

            // "... which is <above/below the normal amount>/<normal>
            if (precipFlag.isDeparture()
                    && normValue != ParameterFormatClimate.MISSING) {
                float valDelta = (float) precipValue - normValue;

                if (valDelta != 0) {
                    precipPhrase.append(" which is");

                    precipPhrase
                            .append(String.format(FLOAT_ONE_DECIMAL, valDelta)
                                    + SPACE + aboveBelow(valDelta > 0)
                                    + " the normal amount");
                } else {
                    precipPhrase.append(" which is normal");
                }
            }

            // "... of __ days."
            // OR "... The normal amount is __ for the period"
            if (precipFlag.isNorm()
                    && normValue != ParameterFormatClimate.MISSING) {
                float valDelta = (float) precipValue - normValue;

                if (valDelta != 0) {
                    if (precipFlag.isDeparture()) {
                        precipPhrase.append(" of "
                                + String.format(FLOAT_ONE_DECIMAL, normValue)
                                + dayDays(normValue != 1));
                    } else {
                        precipPhrase.append(". The normal amount is "
                                + String.format(FLOAT_ONE_DECIMAL, normValue)
                                + " for the period");
                    }
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        return precipPhrase.toString();
    }

    /**
     * Migrated from build_NWR_p_liquid_hr_storm_avg.f.
     * 
     * <pre>
    *   November 1999      Bonnie Reed   PRC/TDL
    *
    *   Purpose:  This routine controls building the period rainfall sentences in 
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
    *        do_liquid_precip - structure containing the flags which control the 
    *                           generation of various portions of the precip sentences.
    *                           See the structure definition for more details.
    *                   itype - declares either a monthly report or seasonal report
    *                 h_climo - structure containing historical climatology
    *                           for a given date.  See structure definition for more
    *                           details.
    *                  p_data - structure containing the observed climate data for the
    *                           month or season.
    *           precip_phrase - character buffer containing the precip
    *                           sentences structure containing the observed climate
    *                           data.  See the structure definition for
    *                           more details.
    * 
    *      Output
    *        precip_phrase - character buffer containing the precip sentences
    *
    * 
    *      Local
    *          avg_precip - character form of the average precip value
    *              c_norm - character form of the normal value
    *         date_phrase - portion of sentence dealing with dates of occurrence
    *         date_length - character length of date_phrase
    *               delta - the difference between two values
    *           delta_val - the difference between two values
    *         delta_value - the difference between two values
    *          difference - character string set to either "above" or "below"
    *              hr_beg - character form of the beginning hour
    *              hr_end - character form of the ending hour
    *              hr_val - character form of the hour
    *                ilen - number of characters in a string
    *                 mon - character string of the word month (January)
    *            num_char - Number of characters in a string.
    *         num_decimal - number that accounts for the decimal place
    *          num_digits - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *        num_digits1  - The number of digits in a given number; 
    *                       necessary for the conversion of numeric values 
    *                       to character.
    *           storm_val - character form of the storm total value
    *
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *      dates_sentence - FORTRAN function: builds the dates of occurrence
    *                       section for 24h and storm precipitation
    *
    *  MODIFICATION HISTORY
    *  ====================
    *    5/07/01   Doug Murphy            Enhanced code to report all three dates
    *                                     of occurrence for 24h and storm precip..
    *                                     Made code more efficient - not necessary
    *                                     to pass along entire arrays
    *    4//1/03   Gary Battel            Corrected problem with monthly max 24 HR
    *                                     precip phrase when monthly max is 0.
    *
    *  Include Files
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRLiquidStormAvg(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        PrecipitationControlFlags precipFlag = currentSettings.getControl()
                .getPrecipControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        // "The maximum 24 hour precipitation was __."
        if (precipFlag.getPrecip24HR().isMeasured()
                && periodData
                        .getPrecipMax24H() != ParameterFormatClimate.MISSING_PRECIP
                && periodData.getPrecipMax24H() != 0) {

            String amount = (periodData
                    .getPrecipMax24H() == ParameterFormatClimate.TRACE)
                            ? A_TRACE
                            : (String.format(FLOAT_TWO_DECIMALS1,
                                    periodData.getPrecipMax24H()) + " inches");
            precipPhrase.append("The " + MAXIMUM + " 24 hour " + PRECIPITATION
                    + " was " + amount);

            if (precipFlag.getPrecip24HR().isTimeOfMeasured()
                    && !periodData.getPrecip24HDates().isEmpty()
                    && periodData.getPrecip24HDates().get(0).getStart()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getPrecip24HDates().get(0).getEnd()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getPrecip24HDates().get(0).getStart()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getPrecip24HDates().get(0).getEnd()
                            .getDay() != ParameterFormatClimate.MISSING_DATE) {
                precipPhrase
                        .append(datesSentence(periodData.getPrecip24HDates()));

            }
            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "The highest total storm precipitation was __."
        if (precipFlag.getPrecipStormMax().isMeasured()
                && periodData
                        .getPrecipStormMax() != ParameterFormatClimate.MISSING
                && periodData.getPrecipStormMax() != 0) {
            String amount = (periodData
                    .getPrecipStormMax() == ParameterFormatClimate.TRACE)
                            ? A_TRACE
                            : (String.format(FLOAT_TWO_DECIMALS1,
                                    periodData.getPrecipStormMax())
                                    + " inches");
            precipPhrase.append("The highest total storm " + PRECIPITATION
                    + " was " + amount);

            if (precipFlag.getPrecipStormMax().isTimeOfMeasured()
                    && !periodData.getPrecipStormList().isEmpty()
                    && periodData.getPrecipStormList().get(0).getStart()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getPrecipStormList().get(0).getEnd()
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getPrecipStormList().get(0).getStart()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getPrecipStormList().get(0).getEnd()
                            .getDay() != ParameterFormatClimate.MISSING_DATE) {
                precipPhrase
                        .append(datesSentence(periodData.getPrecipStormList()));

            }
            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        // "The average precipitation for the period was __ which is
        // <normal>/<__ inches above/below the average daily amount>."
        if (precipFlag.getPrecipAvg().isMeasured() && periodData
                .getPrecipMeanDay() != ParameterFormatClimate.MISSING) {

            String partialAvgPhrase = "The average daily " + PRECIPITATION
                    + " for the period was";
            if (Math.abs(periodData
                    .getPrecipMeanDay()) < ParameterFormatClimate.HALF_PRECIP
                    && periodData
                            .getPrecipMeanDay() != ParameterFormatClimate.TRACE) {
                precipPhrase.append(partialAvgPhrase + " 0.00 inches");
            } else if (periodData
                    .getPrecipMeanDay() == ParameterFormatClimate.TRACE) {
                precipPhrase.append(partialAvgPhrase + " a trace");
            } else {
                String avgPrecip = (periodData.getPrecipMeanDay() < 1) ? "0"
                        : String.format(FLOAT_TWO_DECIMALS1,
                                periodData.getPrecipMeanDay());
                precipPhrase.append(
                        partialAvgPhrase + SPACE + avgPrecip + " inches");
            }

            if (precipFlag.getPrecipAvg().isDeparture()
                    && hClimo
                            .getPrecipDayNorm() != ParameterFormatClimate.MISSING
                    && periodData
                            .getPrecipMeanDay() != ParameterFormatClimate.TRACE
                    && hClimo
                            .getPrecipDayNorm() != ParameterFormatClimate.TRACE) {

                float deltaValue = periodData.getPrecipMeanDay()
                        - hClimo.getPrecipDayNorm();

                if (deltaValue != 0) {
                    precipPhrase.append(" which is "
                            + String.format(FLOAT_TWO_DECIMALS1,
                                    Math.abs(deltaValue))
                            + " inches " + aboveBelow(deltaValue > 0)
                            + " the normal average daily amount");

                } else {
                    precipPhrase.append(" which is normal");
                }
            }

            // If "which is normal" was appended, append "The normal average
            // daily amount of precipitation is __."
            if (precipFlag.getPrecipAvg().isNorm() && hClimo
                    .getPrecipDayNorm() != ParameterFormatClimate.MISSING) {
                if (hClimo.getPrecipDayNorm() == ParameterFormatClimate.TRACE
                        && periodData
                                .getPrecipMeanDay() == ParameterFormatClimate.TRACE) {
                    precipPhrase.append(" which is normal");
                } else if (hClimo
                        .getPrecipDayNorm() == ParameterFormatClimate.TRACE
                        && periodData
                                .getPrecipMeanDay() != ParameterFormatClimate.TRACE) {
                    precipPhrase.append(".  The normal average daily amount of "
                            + PRECIPITATION + " is a trace");
                } else {
                    float deltaValue = periodData.getPrecipMeanDay()
                            - hClimo.getPrecipDayNorm();

                    if (deltaValue != 0) {

                        if (precipFlag.getPrecipAvg().isDeparture()
                                && periodData
                                        .getPrecipMeanDay() != ParameterFormatClimate.TRACE) {
                            precipPhrase.append(" of "
                                    + hClimo.getPrecipDayNorm() + " inches");
                        } else {
                            precipPhrase
                                    .append(".  The normal average daily amount of "
                                            + PRECIPITATION + " is "
                                            + hClimo.getPrecipDayNorm()
                                            + " inches");
                        }
                    }
                }
            }

            precipPhrase.append(PERIOD + SPACE + SPACE);
        }

        return precipPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_liquid_precip.f.
     * 
     * <pre>
     *    October  1999    Dan Zipper     PRC/TDL
    *    November 1999    Bonnie Reed    PRC/TDL
    *
    *   Purpose:  This routine controls building the period rainfall sentences in 
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
    *        do_liquid_precip - structure containing the flags which control the 
    *                           generation of various portions of the precip sentences.
    *                           See the structure definition for more details.
    *                   itype - declares either a monthly report or seasonal report
    *                 h_climo - structure containing historical climatology
    *                           for a given date.  See structure definition for more
    *                           details.
    *                  p_data - structure containing the observed climate data for the
    *                           month or season.
    *           precip_phrase - character buffer containing the precip
    *                           sentences structure containing the observed climate
    *                           data.  See the structure definition for
    *                           more details.
    *
    *      Output
    *        precip_phrase - character buffer containing the precip sentences
    *     
    *      Local
    *           c_precip - character form of a value
    *          delta_val - the difference between two values
    *         difference - character string set to either "above" or "below"
    *        num_decimal - number that accounts for the decimal place
    *               more - logical value when reporting the record
    *           num_char - Number of characters in a string.
    *         num_digits - The number of digits in a given number; 
    *                      necessary for the conversion of numeric values 
    *                      to character.
    *        num_digits1 - The number of digits in a given number; 
    *                      necessary for the conversion of numeric values 
    *                      to character.
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *  MODIFICATION HISTORY
    *  ====================
    *    3/22/01   Doug Murphy            Record values were not being denoted
    *                                     for cases where observed is T and record
    *                                     is 0 and observed is > T and record is T
    *    4/12/01   Doug Murphy            Did some reworking of code for efficiency
    *                                     and added calls to add a comma if the
    *                                     precip is over 1000 inches for some
    *                                     crazy reason
    *    4/30/01   Doug Murphy            Changed/added logic to include the
    *                                     departure from normal when trace is
    *                                     involved
    *    5/07/01   Doug Murphy            Made code more efficient - not necessary
    *                                     to pass along entire arrays
    *    3/18/03   Gary Battel            Corrects problem with trace of precip
    *  Include Files
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRLiquidPrecip(ClimatePeriodReportData reportData) {
        StringBuilder precipPhrase = new StringBuilder();

        PrecipitationControlFlags precipFlag = currentSettings.getControl()
                .getPrecipControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        float recordMax = hClimo
                .getPrecipPeriodMax() == ParameterFormatClimate.TRACE ? 0.005f
                        : hClimo.getPrecipPeriodMax(),
                recordMin = hClimo
                        .getPrecipPeriodMin() == ParameterFormatClimate.TRACE
                                ? 0.005f : hClimo.getPrecipPeriodMin(),
                measured = periodData
                        .getPrecipTotal() == ParameterFormatClimate.TRACE
                                ? 0.005f : periodData.getPrecipTotal();

        // "<A trace of>/<No>/<A total of _ inches of> precipitation fell during
        // the period"
        if (precipFlag.getPrecipTotal().isMeasured() && periodData
                .getPrecipTotal() != ParameterFormatClimate.MISSING) {
            if (periodData.getPrecipTotal() == ParameterFormatClimate.TRACE) {
                precipPhrase.append(StringUtils.capitalize(A_TRACE) + " of ");
            } else if (Math
                    .abs(periodData
                            .getPrecipTotal()) < ParameterFormatClimate.HALF_PRECIP
                    && periodData
                            .getPrecipTotal() != ParameterFormatClimate.TRACE) {
                precipPhrase.append("No ");
            } else {
                precipPhrase.append(
                        "A total of " + String.format(FLOAT_COMMAS_TWO_DECIMALS,
                                periodData.getPrecipTotal()));

                precipPhrase.append(" inches of ");
            }

            precipPhrase.append(PRECIPITATION + " fell during the period");

            // "... The departure from normal is 0.00"
            // OR ".. which is __ inches above/below the normal amount>/<normal>
            float deltaVal = 0;
            if (precipFlag.getPrecipTotal().isDeparture() && hClimo
                    .getPrecipDayNorm() != ParameterFormatClimate.MISSING_PRECIP) {

                if (hClimo.getPrecipPeriodNorm() == 0 && periodData
                        .getPrecipTotal() == ParameterFormatClimate.TRACE) {
                    precipPhrase.append(". The departure from normal is 0.00");
                    deltaVal = 0;
                } else {
                    if (hClimo
                            .getPrecipPeriodNorm() == ParameterFormatClimate.TRACE
                            && periodData.getPrecipTotal() == 0) {
                        deltaVal = 0;
                    } else if (hClimo
                            .getPrecipPeriodNorm() != ParameterFormatClimate.TRACE
                            && periodData
                                    .getPrecipTotal() == ParameterFormatClimate.TRACE) {
                        deltaVal = -1 * hClimo.getPrecipPeriodNorm();
                    } else if (hClimo
                            .getPrecipPeriodNorm() != ParameterFormatClimate.TRACE
                            && periodData
                                    .getPrecipTotal() != ParameterFormatClimate.TRACE) {
                        deltaVal = periodData.getPrecipTotal();
                    } else {
                        deltaVal = periodData.getPrecipTotal()
                                - hClimo.getPrecipPeriodNorm();
                    }

                    precipPhrase.append(" which is ");

                    if (deltaVal != 0) {
                        precipPhrase
                                .append(String.format(FLOAT_COMMAS_TWO_DECIMALS,
                                        Math.abs(deltaVal)) + " inches "
                                + aboveBelow(deltaVal > 0)
                                + " the normal amount");

                    } else {
                        precipPhrase.append("normal");
                    }

                }
            }

            // If the above phrase is "...above/below the normal amount" append
            // "... of <__ [inches]>/<a trace>.
            if (precipFlag.getPrecipTotal().isNorm() && hClimo
                    .getPrecipPeriodNorm() != ParameterFormatClimate.MISSING) {

                if (precipFlag.getPrecipTotal().isDeparture()) {
                    if (deltaVal != 0) {
                        if (hClimo
                                .getPrecipDayNorm() != ParameterFormatClimate.TRACE) {
                            precipPhrase.append(" of "
                                    + String.format(FLOAT_COMMAS_TWO_DECIMALS,
                                            hClimo.getPrecipPeriodNorm()));
                        } else {
                            precipPhrase.append(" of " + A_TRACE);
                        }

                    }
                } else {
                    precipPhrase.append(".  The normal amount of "
                            + PRECIPITATION + " is ");

                    if (hClimo
                            .getPrecipPeriodNorm() == ParameterFormatClimate.TRACE) {
                        precipPhrase.append(A_TRACE);
                    } else {
                        precipPhrase
                                .append(String.format(FLOAT_COMMAS_TWO_DECIMALS,
                                        hClimo.getPrecipPeriodNorm()));
                    }
                }
            }
            precipPhrase.append(PERIOD + SPACE + SPACE);

            if (precipFlag.getPrecipTotal().isRecord() && hClimo
                    .getPrecipPeriodMax() != ParameterFormatClimate.MISSING_PRECIP) {
                if (recordMax == measured && recordMax != 0) {
                    precipPhrase.append("This ties the previous record of ");
                } else if (measured != ParameterFormatClimate.MISSING_PRECIP
                        && measured > recordMax) {
                    precipPhrase.append("This breaks the previous record of ");
                } else {
                    precipPhrase.append(
                            "The record amount of " + PRECIPITATION + " is ");
                }

                if (hClimo
                        .getPrecipPeriodMax() == ParameterFormatClimate.TRACE) {
                    precipPhrase.append(A_TRACE);
                } else {
                    precipPhrase.append(String.format(FLOAT_TWO_DECIMALS1,
                            hClimo.getPrecipPeriodMax()));
                }

                if (precipFlag.getPrecipTotal().isRecordYear()
                        && !hClimo.getPrecipPeriodMaxYearList().isEmpty()
                        && hClimo.getPrecipPeriodMaxYearList().get(0)
                                .getYear() != ParameterFormatClimate.MISSING) {

                    if (hClimo.getPrecipPeriodMaxYearList().size() < 2 || hClimo
                            .getPrecipPeriodMaxYearList().get(1)
                            .getYear() != ParameterFormatClimate.MISSING) {
                        precipPhrase.append(" which was set in ");
                    } else {
                        precipPhrase.append(" which was last set in ");
                    }

                    precipPhrase.append(hClimo.getPrecipPeriodMaxYearList()
                            .get(0).getYear());

                    precipPhrase.append(PERIOD + SPACE + SPACE);
                } else {
                    precipPhrase.append(PERIOD + SPACE + SPACE);
                }
            }

        }

        if (precipFlag.getPrecipTotal().isMeasured()
                && periodData
                        .getPrecipTotal() != ParameterFormatClimate.MISSING_PRECIP
                && precipFlag.getPrecipMin().isRecord() && hClimo
                        .getPrecipPeriodMin() != ParameterFormatClimate.MISSING_PRECIP) {
            if (recordMin == measured) {
                precipPhrase.append("The total ties the previous record "
                        + MINIMUM + " of ");
            } else if (measured < recordMin) {
                precipPhrase.append("The total break the previous " + MINIMUM
                        + SPACE + PRECIPITATION + " record of ");
            } else {
                precipPhrase.append("The record " + MINIMUM + SPACE
                        + PRECIPITATION + " for the period is ");
            }

            if (hClimo.getPrecipPeriodMin() == ParameterFormatClimate.TRACE) {
                precipPhrase.append(A_TRACE);
            } else {
                precipPhrase.append(String.format(FLOAT_TWO_DECIMALS1,
                        hClimo.getPrecipPeriodMin()));
            }

            if (precipFlag.getPrecipMin().isRecordYear()
                    && !hClimo.getPrecipPeriodMinYearList().isEmpty()
                    && hClimo.getPrecipPeriodMinYearList().get(0)
                            .getYear() != ParameterFormatClimate.MISSING) {

                if (hClimo.getPrecipPeriodMinYearList().size() < 2
                        || hClimo.getPrecipPeriodMinYearList().get(1)
                                .getYear() != ParameterFormatClimate.MISSING) {
                    precipPhrase.append(" which was set in ");
                } else {
                    precipPhrase.append(" which was last set in ");
                }

                precipPhrase.append(
                        hClimo.getPrecipPeriodMinYearList().get(0).getYear());

                precipPhrase.append(PERIOD + SPACE + SPACE);
            } else {
                precipPhrase.append(PERIOD + SPACE + SPACE);
            }
        }

        return precipPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_temp.f.
     * 
     * <pre>
    *   October 1999      Dan Zipper    PRC/TDL
    *   October 1999      Bonnie Reed   PRC/TDL
    *
    *   Purpose:  
    *        This subroutine will create the temperature related sentences for the
    *        NWR product.
    *
    *   Variables
    *
    *      Input
    *
    *           do_celsius - Flag for reporting temperatures in Celsius
    *                        - TRUE  report temps in Celsius
    *                        - FALSE don't report temps in Celsius
    *              do_temp - Structure containing the flags which control generation
    *                        of various protions.
    *               a_date - structure containing historical climotology for a 
    *                        given date
    *                itype - delcares whether the report is monthly or seasonal
    *                    i - Number of stations in this group to be summarized.
    *              h_climo - Structure containing historical climatology.
    *               p_data - Structure containing the observed climate data.
    *                        See the structure definition for more details.
    *        global_values - structure containing the definitions of different
    *                        global elements.
    *          temp_phrase - Character buffer containing the temperature sentences.
    *
    *      Output
    *
    *          temp_phrase - Character buffer containing the temperature sentences.
    *
    *      Local
    *        NONE
    *
    *
    *      INCLUDE files
    *     DEFINE_general_phrases.I - Contains character strings need to build all
    *                                types of sentences.
    *      DEFINE_precip_phrases.I - Contains character strings needed to build
    *                                precip sentences.
    *   PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                                arrays, etc.  This INCLUDE file must always
    *                                come first.
    *         TYPE_climate_dates.I - Defines the derived TYPE "climate_dates";
    *                                note that it uses the dervied TYPE "date" - 
    *                                the "date" file must be INCLUDED before this
    *                                one.
    *                  TYPE_date.I - Defines the derived TYPE "date";
    *                                This INCLUDE file must always come before any
    *                                other INCLUDE file which uses the "date" TYPE.
    *   TYPE_do_p_weather_element.I - Defines the derived TYPE used to hold the
    *                                controlling logi* for producing
    *                                sentences/reports for a given variable.
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
     * </pre>
     * 
     * @param climateDailyReportData
     * @return
     */
    private String buildNWRPeriodTemp(ClimatePeriodReportData reportData) {
        StringBuilder tempPhrase = new StringBuilder();

        // Max temp phrase
        tempPhrase.append(buildNWRPeriodMaxMinTemp(reportData, true));
        // Min temp phrase
        tempPhrase.append(buildNWRPeriodMaxMinTemp(reportData, false));
        // Average temp phrase
        tempPhrase.append(buildNWRPeriodMeanTemp(reportData));
        // Max threshold phrase
        tempPhrase.append(buildNWRPeriodTValues(reportData, true));
        // Max user-defined threshold phrase
        tempPhrase.append(buildNWRPeriodUserValues(reportData, true));
        // Min threshold phrase
        tempPhrase.append(buildNWRPeriodTValues(reportData, false));
        // Min user-defined threshold phrase
        tempPhrase.append(buildNWRPeriodUserValues(reportData, false));

        return tempPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_max_usr_val.f and
     * build_NWR_period_min_usr_val.f.
     * 
     * @param reportData
     * @param max
     *            true if user defined maximum temperatures, false if minimum
     *            temperatures
     * @return
     */
    private String buildNWRPeriodUserValues(ClimatePeriodReportData reportData,
            boolean max) {
        StringBuilder tempPhrase = new StringBuilder();
        PeriodData periodData = reportData.getData();

        ClimateProductFlags tempFlag1, tempFlag2, tempFlag3;
        int numComparison1, numComparison2, numComparison3, compareNum1,
                compareNum2, compareNum3;

        String maxMinString;

        boolean compareFlag1, compareFlag2, compareFlag3;

        // determine the parameters to pass to userValuePhraseHelper based on
        // whether it's the max or min
        if (max) {
            maxMinString = MAXIMUM;
            tempFlag1 = currentSettings.getControl().getTempControl()
                    .getMaxTempGET1();
            tempFlag2 = currentSettings.getControl().getTempControl()
                    .getMaxTempGET2();
            tempFlag3 = currentSettings.getControl().getTempControl()
                    .getMaxTempLET3();

            numComparison1 = periodData.getNumMaxGreaterThanT1F();
            numComparison2 = periodData.getNumMaxGreaterThanT2F();
            numComparison3 = periodData.getNumMaxLessThanT3F();

            compareNum1 = globalConfig.getT1();
            compareNum2 = globalConfig.getT2();
            compareNum3 = globalConfig.getT3();

            compareFlag1 = true;
            compareFlag2 = true;
            compareFlag3 = false;

        } else {
            maxMinString = MINIMUM;
            tempFlag1 = currentSettings.getControl().getTempControl()
                    .getMinTempGET4();
            tempFlag2 = currentSettings.getControl().getTempControl()
                    .getMinTempLET5();
            tempFlag3 = currentSettings.getControl().getTempControl()
                    .getMinTempLET6();

            numComparison1 = periodData.getNumMinGreaterThanT4F();
            numComparison2 = periodData.getNumMinLessThanT5F();
            numComparison3 = periodData.getNumMinLessThanT6F();

            compareNum1 = globalConfig.getT4();
            compareNum2 = globalConfig.getT5();
            compareNum3 = globalConfig.getT6();

            compareFlag1 = true;
            compareFlag2 = false;
            compareFlag3 = false;
        }

        // append phrases for each of T1 - T6.
        tempPhrase.append(userValuePhraseHelper(tempFlag1, numComparison1,
                compareNum1, maxMinString, compareFlag1));

        tempPhrase.append(userValuePhraseHelper(tempFlag2, numComparison2,
                compareNum2, maxMinString, compareFlag2));

        tempPhrase.append(userValuePhraseHelper(tempFlag3, numComparison3,
                compareNum3, maxMinString, compareFlag3));

        return tempPhrase.toString();
    }

    /**
     * Helper for buildNWRPeriodUserValues.
     * 
     * @param tempFlag
     * @param numComparison
     * @param compareNum
     * @param maxMinString
     * @param greaterThan
     *            true if the comparison for the global value is
     *            "greater than or equal"; false if it's "less than or equal."
     * @return
     */
    private String userValuePhraseHelper(ClimateProductFlags tempFlag,
            int numComparison, int compareNum, String maxMinString,
            boolean greaterThan) {
        StringBuilder tempPhrase = new StringBuilder();

        if (tempFlag.isMeasured()
                && numComparison != ParameterFormatClimate.MISSING
                && compareNum != ParameterFormatClimate.MISSING) {
            String exceedFall = greaterThan ? "exceed" : "fall below";
            // "The maximum/minimum temperature did not exceed/fall below __."
            // OR "The maximum/minimum temperature was at or above/below __
            // degree(s) fahrenheit<, or __ celsius> on __ day(s)."
            if (numComparison == 0) {
                tempPhrase.append("The " + maxMinString + SPACE + TEMPERATURE
                        + " did not " + exceedFall + SPACE);

                tempPhrase.append(decideDegree(compareNum));

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum));
                }
            } else {
                tempPhrase.append("The " + maxMinString + SPACE + TEMPERATURE
                        + " was at or " + aboveBelow(greaterThan) + SPACE);

                tempPhrase.append(decideDegree(compareNum));

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum));
                }

                tempPhrase.append(" on " + numComparison + SPACE
                        + dayDays(numComparison != 1));
            }

            tempPhrase.append(PERIOD + SPACE + SPACE);
        }

        return tempPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_max_t_values.f and
     * build_NWR_period_min_t_values.f which contain identical logic;
     * consolidated here.
     * 
     * @param reportData
     * @param max
     * @return
     */
    private String buildNWRPeriodTValues(ClimatePeriodReportData reportData,
            boolean max) {
        StringBuilder tempPhrase = new StringBuilder();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        ClimateProductFlags tempFlag1, tempFlag2;
        String maxMinString, exceedFall1, exceedFall2, exceedFall3;
        float normComparison1, normComparison2;
        int numComparison1, numComparison2, compareNum1, compareNum2;
        // determine which variables to add the the phrase based on whether its
        // the maximum or the minimum
        if (max) {
            tempFlag1 = currentSettings.getControl().getTempControl()
                    .getMaxTempGE90();
            tempFlag2 = currentSettings.getControl().getTempControl()
                    .getMaxTempLE32();
            numComparison1 = periodData.getNumMaxGreaterThan90F();
            numComparison2 = periodData.getNumMaxLessThan32F();
            normComparison1 = hClimo.getNormNumMaxGE90F();
            normComparison2 = hClimo.getNormNumMaxLE32F();
            maxMinString = MAXIMUM;
            exceedFall1 = "exceed";
            exceedFall2 = "exceeded";
            exceedFall3 = "exceeding";
            compareNum1 = 90;
            compareNum2 = 32;

        } else {
            tempFlag1 = currentSettings.getControl().getTempControl()
                    .getMinTempLE32();
            tempFlag2 = currentSettings.getControl().getTempControl()
                    .getMinTempLE0();
            numComparison1 = periodData.getNumMinLessThan32F();
            numComparison2 = periodData.getNumMinLessThan0F();
            normComparison1 = hClimo.getNormNumMinLE32F();
            normComparison2 = hClimo.getNormNumMinLE0F();
            maxMinString = MINIMUM;
            exceedFall1 = "fall below";
            exceedFall2 = "dropped below";
            exceedFall3 = "below";
            compareNum1 = 32;
            compareNum2 = 0;
        }
        if (tempFlag1.isMeasured()
                && numComparison1 != ParameterFormatClimate.MISSING) {
            if (numComparison1 == 0) {
                tempPhrase.append("The " + maxMinString + SPACE + TEMPERATURE
                        + " did not " + exceedFall1 + SPACE + compareNum1
                        + " degrees");
                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum1));
                }
            }

            else {
                tempPhrase.append("The " + maxMinString + SPACE + TEMPERATURE
                        + SPACE + exceedFall2 + SPACE + compareNum1
                        + " degrees");

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum1));
                }

                tempPhrase.append(" on " + numComparison1 + SPACE
                        + dayDays(numComparison1 != 1) + " this period");
            }

            float valDelta = (float) numComparison1 - normComparison1;
            if (tempFlag1.isDeparture()
                    && normComparison1 != ParameterFormatClimate.MISSING) {

                if (valDelta != 0) {

                    tempPhrase.append(" which is " + Math.abs(valDelta) + SPACE
                            + aboveBelow(valDelta > 0) + " the normal ");
                } else {
                    tempPhrase.append(" which is normal");
                }
            }

            if (tempFlag1.isNorm()
                    && normComparison1 != ParameterFormatClimate.MISSING) {
                if (valDelta != 0) {
                    if (tempFlag1.isDeparture()) {
                        String dayString = (numComparison1 == 1) ? "day"
                                : "days";

                        tempPhrase.append(normComparison1 + SPACE + dayString);
                    } else {
                        tempPhrase.append(
                                ".  The normal number of days " + exceedFall3
                                        + SPACE + compareNum1 + " degrees");

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(compareNum1));
                        }

                        tempPhrase.append(" is " + normComparison1);
                    }
                }
            }

            tempPhrase.append(PERIOD + SPACE + SPACE);
        }

        if (tempFlag2.isMeasured()
                && numComparison2 != ParameterFormatClimate.MISSING) {

            if (numComparison2 == 0) {
                tempPhrase.append("The " + maxMinString + SPACE + TEMPERATURE
                        + " did not " + "fall below " + compareNum2
                        + " degrees");
                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum2));
                }
            }

            else {
                tempPhrase.append("The " + maxMinString + SPACE + TEMPERATURE
                        + SPACE + "was at or below " + compareNum2
                        + " degrees");

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum2));
                }

                tempPhrase.append(" on " + numComparison2 + SPACE
                        + dayDays(numComparison2 != 1));
            }

            float valDelta = (float) numComparison2 - normComparison2;
            if (tempFlag2.isDeparture()
                    && normComparison2 != ParameterFormatClimate.MISSING) {

                if (valDelta != 0) {

                    tempPhrase.append(" which is " + Math.abs(valDelta) + SPACE
                            + aboveBelow(valDelta > 0) + " the normal ");
                } else {
                    tempPhrase.append(" which is normal");
                }
            }

            if (tempFlag2.isNorm()
                    && normComparison2 != ParameterFormatClimate.MISSING) {
                if (valDelta != 0) {
                    if (tempFlag2.isDeparture()) {

                        tempPhrase.append(SPACE + normComparison2 + SPACE
                                + dayDays(numComparison2 != 1));
                    } else {
                        tempPhrase.append(".  The normal number of days "
                                + "below " + compareNum2 + " degrees");

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(compareNum2));
                        }

                        tempPhrase.append(" is " + normComparison2);
                    }
                }
            }

            tempPhrase.append(PERIOD + SPACE + SPACE);

        }
        return tempPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_mean_temp.f
     * 
     * <pre>
    *   October 1999      Dan Zipper    PRC/TDL
    *   October 1999      Bonnie Reed   PRC/TDL
    *
    *   Purpose:  
    *        This subroutine will create the temperature related sentences for the
    *        NWR product.
    *
    *   Variables
    *
    *      Input
    *
    *           do_celsius - Flag for reporting temperatures in Celsius
    *                        - TRUE  report temps in Celsius
    *                        - FALSE don't report temps in Celsius
    *              do_temp - Structure containing the flags which control generation
    *                        of various protions.
    *               a_date - Structure containing the observed date for yesterday.
    *                itype - declares whether it is month or seasonal.
    *                  num - Number of stations in this group to be summarized.
    *              h_climo - Structure containing historical climatology.
    *               p_data - Structure containing the observed climate data.
    *                        See the structure definition for more details.
    *          temp_phrase - Character buffer containing the temperature sentences.
    *
    *      Output
    *
    *          temp_phrase - Character buffer containing the temperature sentences.
    *
    *      Local
    *                    c_avg - character form of a value
    *               difference - character string set to either "above" or "below"
    *        delta_temp_mean_f - difference between two values (Fahrenheit)
    *        delta_temp_mean_c - difference between two values (Celsius)
    *                 num_char - Number of characters in a string.
    *               num_digits - The number of digits in a given number; 
    *                            necessary for the conversion of numeric values 
    *                            to character.
    *               val_deltaf - difference between two values
    *
    *        Non-system routines used
    *
    *        Non-system functions used
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *
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
    *                  TYPE_date.I - Defines the derived TYPE "date";
    *                                This INCLUDE file must always come before any
    *                                other INCLUDE file which uses the "date" TYPE.
    *   TYPE_do_p_weather_element.I - Defines the derived TYPE used to hold the
    *                                controlling logic for producing
    *                                sentences/reports for a given variable.
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
    *  MODIFICATION HISTORY
    *  --------------------
    *    7/14/00   Doug Murphy            Normal temp values changed from ints to
    *                                     reals. Therefore need to round to int
    *                                     before calculating departures.
    *    2/06/01   Doug Murphy            Major revision to accomodate mean temp
    *                                     values being changed to reals
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRPeriodMeanTemp(ClimatePeriodReportData reportData) {
        StringBuilder tempPhrase = new StringBuilder();

        TemperatureControlFlags tempFlag = currentSettings.getControl()
                .getTempControl();
        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();
        float deltaTempMeanF = 0;
        int numDecimals = 1;

        // "The average temperature was __ degree(s) which is __ degree(s) which
        // is <normal>/<__ degree(s) above/below the normal average
        // temperature>
        if (tempFlag.getMeanTemp().isMeasured()
                && periodData.getMeanTemp() != ParameterFormatClimate.MISSING) {
            tempPhrase.append("The average " + TEMPERATURE + " was "
                    + String.format(FLOAT_ONE_DECIMAL, periodData.getMeanTemp())
                    + " degree");

            if (periodData.getMeanTemp() != 1
                    && periodData.getMeanTemp() != -1) {
                tempPhrase.append("s");
            }

            if (currentSettings.getControl().isDoCelsius()) {

                tempPhrase.append(
                        buildNWRCelsius(periodData.getMeanTemp(), numDecimals));
            }

            if (tempFlag.getMeanTemp().isDeparture() && hClimo
                    .getNormMeanTemp() != ParameterFormatClimate.MISSING) {
                deltaTempMeanF = periodData.getMeanTemp()
                        - hClimo.getNormMeanTemp();
                float valDeltaF = Math.abs(deltaTempMeanF);

                double deltaTempMeanC = Math.abs(deltaTempMeanF * (5. / 9.));

                if (deltaTempMeanF != 0) {
                    tempPhrase.append(" which is "
                            + String.format(FLOAT_ONE_DECIMAL, valDeltaF)
                            + " degree");

                    if (Math.abs(valDeltaF) != 1) {
                        tempPhrase.append("s");
                    }

                    if (currentSettings.getControl().isDoCelsius()) {
                        tempPhrase.append(
                                buildNWRCelsius(deltaTempMeanC, numDecimals));
                    }
                }

                if (deltaTempMeanF != 0) {
                    tempPhrase.append(SPACE + aboveBelow(deltaTempMeanF > 0)
                            + " the normal average " + TEMPERATURE);
                } else {
                    tempPhrase.append(" which is normal");
                }
            }

            // If "which is normal" was appended above, append "The normal
            // average
            // temperature for the period is __ degree(s)."
            if (tempFlag.getMeanTemp().isNorm() && hClimo
                    .getNormMeanTemp() != ParameterFormatClimate.MISSING) {
                if (tempFlag.getMeanTemp().isDeparture()) {
                    if (deltaTempMeanF != 0) {
                        tempPhrase
                                .append(" of "
                                        + String.format(FLOAT_ONE_DECIMAL,
                                                hClimo.getNormMeanTemp())
                                        + " degree");

                        if (Math.abs(hClimo.getNormMeanTemp()) != 1) {
                            tempPhrase.append("s");
                        }

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(
                                    hClimo.getNormMeanTemp(), numDecimals));
                        }
                    }

                } else {
                    tempPhrase
                            .append(".  The normal average " + TEMPERATURE
                                    + " for the period is "
                                    + String.format(FLOAT_ONE_DECIMAL,
                                            hClimo.getNormMeanTemp())
                            + " degree");
                    if (Math.abs(hClimo.getNormMeanTemp()) != 1) {
                        tempPhrase.append("s");
                    }

                    if (currentSettings.getControl().isDoCelsius()) {
                        tempPhrase.append(buildNWRCelsius(
                                hClimo.getNormMeanTemp(), numDecimals));
                    }
                }
            }

            tempPhrase.append(PERIOD + SPACE + SPACE);
        }
        return tempPhrase.toString();
    }

    /**
     * Migrated from build_NWR_period_max_temp.f and
     * build_NWR_period_min_temp.f. The logic is nearly identical in both, so
     * consolidate them.
     * 
     * @param reportData
     * @param max
     *            true if this is a phrase for max temp, false if this is a
     *            phrase for min temp
     * @return
     */
    private String buildNWRPeriodMaxMinTemp(ClimatePeriodReportData reportData,
            boolean max) {
        StringBuilder tempPhrase = new StringBuilder();

        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        ClimateProductFlags tempFlag;
        int observedTemp;
        float normTemp;
        int recordTemp;
        List<ClimateDate> dates;
        List<ClimateDate> recordDates;

        // determine which variables to add the the phrase based on whether its
        // the maximum or the minimum
        if (max) {
            tempFlag = currentSettings.getControl().getTempControl()
                    .getMaxTemp();
            observedTemp = periodData.getMaxTemp();
            normTemp = hClimo.getMaxTempNorm();
            recordTemp = hClimo.getMaxTempRecord();
            dates = periodData.getDayMaxTempList();
            recordDates = hClimo.getDayMaxTempRecordList();
        } else {
            tempFlag = currentSettings.getControl().getTempControl()
                    .getMinTemp();
            observedTemp = periodData.getMinTemp();
            normTemp = hClimo.getMinTempNorm();
            recordTemp = hClimo.getMinTempRecord();
            dates = periodData.getDayMinTempList();
            recordDates = hClimo.getDayMinTempRecordList();
        }

        String maxMin = max ? MAXIMUM : MINIMUM;

        // "The maximum/minimum temperature for the period is __ degrees
        // Fahrenheit<, or __
        // Celsius> <above/below the normal maximum/minimum>/<which is normal>"
        if (tempFlag.isMeasured()
                && observedTemp != ParameterFormatClimate.MISSING) {
            tempPhrase.append("The " + maxMin + SPACE + TEMPERATURE
                    + " for the period was " + observedTemp);

            if (currentSettings.getControl().isDoCelsius()) {
                tempPhrase.append(buildNWRCelsius(observedTemp));
            }

            int nearestInt = ClimateUtilities.nint(normTemp);

            int tempFDelta = 0;
            if (tempFlag.isDeparture()
                    && normTemp != ParameterFormatClimate.MISSING) {
                tempFDelta = observedTemp - nearestInt;

                int valDeltaF = Math.abs(tempFDelta);

                double tempCDelta = ClimateUtilities
                        .fahrenheitToCelsius(observedTemp)
                        - ClimateUtilities.fahrenheitToCelsius(nearestInt);
                double valDeltaC = Math.abs(tempCDelta);

                if (tempFDelta != 0) {
                    tempPhrase.append(" which is ");

                    tempPhrase.append(decideDegree(valDeltaF));

                    if (currentSettings.getControl().isDoCelsius()) {
                        tempPhrase.append(
                                " Fahrenheit, or " + valDeltaC + " Celsius");
                    }
                }

                if (tempFDelta != 0) {
                    tempPhrase.append(SPACE + aboveBelow(tempFDelta > 0)
                            + " the normal " + maxMin);
                } else {
                    tempPhrase.append(" which is normal");
                }
            }

            // Append "The normal maximum/minimum temperature is __" if "which
            // is normal" was appended above.
            // Append "... of __ fahrenheit, or __ celsius" otherwise.
            if ((max && tempFlag.isNorm()
                    && normTemp != ParameterFormatClimate.MISSING)
                    || (!max && tempFlag.isNorm() && tempFDelta != 0
                            && normTemp != ParameterFormatClimate.MISSING)) {
                tempFDelta = observedTemp - nearestInt;

                if (tempFDelta != 0) {
                    if (tempFlag.isDeparture()) {
                        tempPhrase.append(" of " + nearestInt);

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(nearestInt));
                        }
                    } else {
                        tempPhrase.append(".  The normal " + maxMin + SPACE
                                + TEMPERATURE + " is " + nearestInt);

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(nearestInt));
                        }
                    }
                }
            }

            tempPhrase.append(PERIOD + SPACE + SPACE);

            // "The maximum/minimum occurred on <date(s)>."
            if (tempFlag.isTimeOfMeasured() && !dates.isEmpty()
                    && dates.get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && dates.get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {
                tempPhrase.append("The " + maxMin + " occurred on "
                        + dateSentence(dates) + PERIOD + SPACE + SPACE);
            }

            // "The record maximum/minimum temperature is __ which last was set
            // in <year>."
            // OR "This breaks/ties the previous record of __ which was set in
            // <year>."
            if (tempFlag.isRecord()
                    && recordTemp != ParameterFormatClimate.MISSING) {
                if ((max && recordTemp > observedTemp)
                        || (!max && recordTemp < observedTemp)) {
                    tempPhrase.append("The record " + maxMin + SPACE
                            + TEMPERATURE + " is ");
                } else if ((max && recordTemp < observedTemp)
                        || (!max && recordTemp > observedTemp)) {
                    tempPhrase.append("This breaks the previous record of ");
                } else if (recordTemp == observedTemp) {
                    tempPhrase.append("This ties the previous record of ");
                }

                tempPhrase.append(decideDegree(recordTemp));

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(recordTemp));
                }

                if (tempFlag.isRecordYear() && !recordDates.isEmpty()
                        && recordDates.get(0)
                                .getYear() != ParameterFormatClimate.MISSING) {
                    if (recordDates.size() < 2 || recordDates.get(1)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        tempPhrase.append(" which was set in ");
                    } else {
                        tempPhrase.append(" which was last set in ");
                    }

                    tempPhrase.append(recordDates.get(0).getYear());
                }
                tempPhrase.append(PERIOD + SPACE + SPACE);
            }
        }

        return tempPhrase.toString();
    }

    /**
     * Migrated from date_sentence.f.
     * 
     * <pre>
    *    April 2001       Doug Murphy    PRC/MDL
    *
    *   Purpose:  This routine creates the dates of occurrence phrases for a
    *             period climate report. 
    *
    *   Variables:
    *    Input:
    *                 itype - integer signifying the type of climate report
    *                         being run
    *                 occur - dates of occurrence
    *
    *    Output:
    *           date_phrase - phrase containing the date(s) of occurrence
    *           date_length - character length of date_phrase 
    *
    *    Local:
    *                 c_day - character version of the day of occurrence
    *                     i - loop control variable
    *                  ilen - length of the month character string
    *                   mon - chracter string containing the month name
    *            num_digits - number of digits in day of occurrence
    *                  suff - integer determining which array position to use
    *                         in suffix array
    *                suffix - array containing ordinal abbreviations
    *
    *    Non-system functions used:
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *     <Include date, name, and description if changes are made to routine>
     * </pre>
     * 
     * @param dates
     * @return
     */
    private String dateSentence(List<ClimateDate> dates) {
        StringBuilder datePhrase = new StringBuilder();

        if (!dates.isEmpty()) {
            datePhrase.append(DateFormatSymbols.getInstance()
                    .getMonths()[dates.get(0).getMon() - 1] + SPACE
                    + dates.get(0).getDay());
        }
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).getDay() == ParameterFormatClimate.MISSING_DATE) {
                break;
            } else {
                if ((i == 1 && dates.size() > 2
                        && dates.get(2)
                                .getDay() == ParameterFormatClimate.MISSING_DATE)
                        || i == 2) {
                    datePhrase.append(" and ");
                } else {
                    datePhrase.append(", ");
                }

                if (currentSettings.getReportType() == PeriodType.MONTHLY_RAD) {
                    datePhrase.append(getOrdinal(dates.get(i).getDay()));
                } else {
                    datePhrase.append(DateFormatSymbols.getInstance()
                            .getMonths()[dates.get(i).getMon()] + SPACE
                            + dates.get(i).getDay());
                }
            }
        }

        return datePhrase.toString();
    }

    /**
     * Migrated from another subroutine, dates_sentence, in date_sentence.f.
     * 
     * <pre>
    *    April 2001       Doug Murphy    PRC/MDL
    *
    *   Purpose:  This routine creates the dates of occurrence phrases for
    *             variables with a begin and end date for a period climate 
    *             report. 
    *
    *   Variables:
    *    Input:
    *                 itype - integer signifying the type of climate report
    *                         being run
    *                 cdate - structure containing begin and end dates of 
    *                         occurrence
    *
    *    Output:
    *           date_phrase - phrase containing the date(s) of occurrence
    *           date_length - character length of date_phrase 
    *
    *
    *    Local:
    *                 c_beg - character version of the begin day of occurrence
    *                 c_end - character version of the end day of occurrence
    *                     i - loop control variable
    *                  blen - length of the begin month character string
    *                  elen - length of the end month character string
    *               beg_mon - chracter string containing the begin month name
    *               end_mon - chracter string containing the end month name
    *            beg_digits - number of digits in begin day of occurrence
    *            end_digits - number of digits in end day of occurrence
    *                  suff - integer determining which array position to use
    *                         in suffix array
    *                suffix - array containing ordinal abbreviations
    *
    *    Non-system functions used:
    *         pick_digits - Returns the number of digits in a number.
    *
    *              strlen - C function: returns the number of characters in a
    *                       string; string must be terminated with the NULL
    *                       character.
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *     <Include date, name, and description if changes are made to routine>
     * </pre>
     * 
     * @param dates
     * @return
     */
    private String datesSentence(List<ClimateDates> dates) {
        StringBuilder datePhrase = new StringBuilder();

        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).getStart()
                    .getDay() == ParameterFormatClimate.MISSING_DATE) {
                break;
            } else {
                if ((i == 1 && dates.size() > 2
                        && dates.get(2).getStart()
                                .getDay() == ParameterFormatClimate.MISSING_DATE)
                        || i == 2) {
                    datePhrase.append(" and");
                } else if (i == 1) {
                    datePhrase.append(",");
                }

                if (i == 0) {
                    if (dates.get(i).getStart().getDay() == dates.get(i)
                            .getEnd().getDay()) {
                        datePhrase = new StringBuilder(" on ");
                    } else {
                        datePhrase = new StringBuilder(" between ");
                    }
                } else {
                    if (dates.get(i).getStart().getDay() == dates.get(i)
                            .getEnd().getDay()) {
                        datePhrase.append(" on ");
                    } else {
                        datePhrase.append(" between ");
                    }
                }

                if (dates.get(i).getStart().getDay() != dates.get(i).getEnd()
                        .getDay()
                        && dates.get(i).getStart().getMon() != dates.get(i)
                                .getEnd().getMon()) {
                    String endMon = DateFormatSymbols.getInstance()
                            .getMonths()[dates.get(i).getEnd().getMon()];

                    datePhrase.append(" and " + endMon + SPACE
                            + dates.get(i).getEnd().getDay());
                } else {
                    datePhrase.append(" and "
                            + getOrdinal(dates.get(i).getEnd().getDay()));
                }
            }

        }

        return datePhrase.toString();
    }

    /**
     * Convert an integer to its ordinal abbreviation.
     * 
     * @param n
     * @return
     */
    public static String getOrdinal(int n) {
        String[] suffix = new String[] { "th", "st", "nd", "rd", "th", "th",
                "th", "th", "th", "th" };
        switch (n % 100) {
        case 11:
        case 12:
        case 13:
            return n + "th";
        default:
            return n + suffix[n % 10];

        }
    }

}

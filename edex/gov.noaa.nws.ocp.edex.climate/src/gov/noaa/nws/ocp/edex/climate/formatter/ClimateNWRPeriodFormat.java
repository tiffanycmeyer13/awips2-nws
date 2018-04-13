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
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
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
     * </pre>
     */
    @Override
    public Map<String, ClimateProduct> buildText(
            ClimateRunData reportData)
                    throws ClimateInvalidParameterException,
                    ClimateQueryException {
        Map<String, ClimateProduct> prod = new HashMap<>();

        Map<Integer, ClimatePeriodReportData> reportMap = ((ClimateRunPeriodData) reportData)
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
                        + " defined in the ClimateProductType settings object was not found in the ClimateRunData report map.");
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
            String precipPhrase = buildNWRPeriodPrecip(report,
                    reportData.getBeginDate());
            if (precipPhrase.length() > 0) {
                productText.append(precipPhrase + spaces);
            }

            // degree day phrases
            String heatCoolPhrase = buildNWRPeriodHeatAndCool(report,
                    reportData.getBeginDate());
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

                windPhrase.append(windMph1).append(" miles per hour");
            }

            if (periodData.getAvgWindSpd() > 0) {
                windPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

            windPhrase.append("The ").append(MAXIMUM).append(" wind was ")
                    .append(windMph).append(SPACE)
                    .append(mileMiles(windMph != 1)).append(" per hour");

            if (periodData.getMaxWindList().get(0)
                    .getDir() != ParameterFormatClimate.MISSING) {
                windPhrase.append(" from the ").append(whichDirection(
                        periodData.getMaxWindList().get(0).getDir(), false));
            }

            if (windFlag.getMaxWind().isTimeOfMeasured()
                    && periodData.getMaxWindDayList().get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getMaxWindDayList().get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {

                windPhrase.append(" and occurred on ")
                        .append(DateFormatSymbols.getInstance()
                                .getMonths()[periodData.getMaxWindDayList()
                                        .get(0).getMon() - 1])
                        .append(SPACE)
                        .append(periodData.getMaxWindDayList().get(0).getDay());
            }

            windPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

            windPhrase.append("The ").append(MAXIMUM).append(" wind gust was ")
                    .append(windMph).append(SPACE)
                    .append(mileMiles(windMph != 1)).append(" per hour");
            if (periodData.getMaxGustList().get(0)
                    .getDir() != ParameterFormatClimate.MISSING) {
                windPhrase.append(" from the ").append(whichDirection(
                        periodData.getMaxGustList().get(0).getDir(), false));
            }

            if (windFlag.getMaxGust().isTimeOfMeasured()
                    && !periodData.getMaxGustDayList().isEmpty()
                    && periodData.getMaxGustDayList().get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getMaxGustDayList().get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {
                windPhrase.append(" and occurred on ")
                        .append(DateFormatSymbols.getInstance()
                                .getMonths()[periodData.getMaxGustDayList()
                                        .get(0).getMon() - 1])
                        .append(SPACE)
                        .append(periodData.getMaxGustDayList().get(0).getDay());
            }
            windPhrase.append(PERIOD).append(SPACE).append(SPACE);
        }

        // "The resultant wind was __ mile(s) per hour from the <direction>."
        if (windFlag.getResultWind().isMeasured()) {
            if (periodData.getResultWind()
                    .getSpeed() != ParameterFormatClimate.MISSING
                    && periodData.getResultWind().getSpeed() != 0) {
                int windMph = ClimateUtilities
                        .nint(periodData.getResultWind().getSpeed());

                windPhrase.append("The resultant wind was ").append(windMph)
                        .append(SPACE).append(mileMiles(windMph != 1))
                        .append(" per hour");

                if (periodData.getResultWind()
                        .getDir() != ParameterFormatClimate.MISSING) {
                    windPhrase.append(" from the ").append(whichDirection(
                            periodData.getResultWind().getDir(), false));
                }

                windPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
    *
    * </pre>
    * 
    * @param reportData
    * @return
    */
  
    private String buildNWRPeriodHeatAndCool(ClimatePeriodReportData reportData,
            ClimateDate beginDate) {
        StringBuilder heatCoolPhrase = new StringBuilder();

        DegreeDaysControlFlags degreeFlag = currentSettings.getControl()
                .getDegreeDaysControl();

        PeriodData periodData = reportData.getData();
        PeriodClimo hClimo = reportData.getClimo();

        if (reportWindow(currentSettings.getControl().getHeatDates(),
                beginDate)) {

            // Measured heating degree day
            heatCoolPhrase.append(heatCoolPhraseHelper(degreeFlag.getTotalHDD(),
                    periodData.getNumHeatTotal(), hClimo.getNumHeatPeriodNorm(),
                    "", true));
            // Heating degree day since July 1
            heatCoolPhrase.append(heatCoolPhraseHelper(
                    degreeFlag.getSeasonHDD(), periodData.getNumHeat1July(),
                    hClimo.getNumHeat1JulyNorm(), " since July 1", true));

        }

        if (reportWindow(currentSettings.getControl().getCoolDates(),
                beginDate)) {

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
            heatCoolPhrase.append("The first freeze day occurred on ")
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[periodData.getEarlyFreeze().getMon()
                                    - 1])
                    .append(SPACE).append(periodData.getEarlyFreeze().getDay());

            if (degreeFlag.getEarlyFreeze().isNorm()
                    && hClimo.getEarlyFreezeNorm()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && hClimo.getEarlyFreezeNorm()
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {

                heatCoolPhrase
                        .append(" and the normal earliest freeze date is ")
                        .append(DateFormatSymbols.getInstance()
                                .getMonths()[hClimo.getEarlyFreezeNorm()
                                        .getMon() - 1])
                        .append(SPACE)
                        .append(hClimo.getEarlyFreezeNorm().getDay());
            }
            heatCoolPhrase.append(PERIOD).append(SPACE).append(SPACE);
        }

        if (degreeFlag.getLateFreeze().isMeasured()
                && periodData.getLateFreeze()
                        .getDay() != ParameterFormatClimate.MISSING_DATE
                && periodData.getLateFreeze()
                        .getMon() != ParameterFormatClimate.MISSING_DATE) {
            heatCoolPhrase.append("The last freeze day occurred on ")
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[periodData.getLateFreeze().getMon()
                                    - 1])
                    .append(SPACE).append(periodData.getLateFreeze().getDay());

            if (degreeFlag.getLateFreeze().isNorm()
                    && hClimo.getLateFreezeNorm()
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && hClimo.getLateFreezeNorm()
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {

                heatCoolPhrase.append(" and the normal latest freeze date is ")
                        .append(DateFormatSymbols.getInstance()
                                .getMonths()[hClimo.getLateFreezeNorm().getMon()
                                        - 1])
                        .append(SPACE)
                        .append(hClimo.getLateFreezeNorm().getDay());
            }
            heatCoolPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                heatCoolPhrase.append("There ")
                        .append(wasWere(degreeValue != 1)).append(SPACE)
                        .append(String.format(INT_COMMAS, degreeValue));

            }

            heatCoolPhrase.append(SPACE).append(heatCool).append(" degree ")
                    .append(dayDays(degreeValue != 1))
                    .append((periodString.isEmpty() ? " this period"
                            : periodString));

            if (degreeFlag.isDeparture()
                    && normValue != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                heatCoolPhrase.append(" which is ");

                int deltaDegDay = degreeValue - normValue;

                if (deltaDegDay != 0) {

                    heatCoolPhrase
                            .append(String.format(INT_COMMAS,
                                    Math.abs(deltaDegDay)))
                            .append(SPACE).append(aboveBelow(deltaDegDay > 0))
                            .append(" the normal amount");

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
                        heatCoolPhrase.append(" of ")
                                .append(String.format(INT_COMMAS, normValue))
                                .append(SPACE).append(dayDays(normValue != 1));
                    } else {
                        heatCoolPhrase.append(".  The normal number of ")
                                .append(heatCool).append(" degree days")
                                .append(periodString).append(" is ")
                                .append(String.format(INT_COMMAS, normValue));
                    }
                }
            }

            heatCoolPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
    * </pre>
    * 
    * @param reportData
    * @param report
    * @return
    */

    private String buildNWRPeriodPrecip(ClimatePeriodReportData reportData,
            ClimateDate beginDate) {
        StringBuilder precipPhrase = new StringBuilder();

        // Build liquid precip phrases
        precipPhrase.append(buildNWRLiquidPrecip(reportData));

        precipPhrase.append(buildNWRLiquidFraction(reportData));

        precipPhrase.append(buildNWRLiquidStormAvg(reportData));

        // Build snow precip phrases
        if (reportWindow(currentSettings.getControl().getSnowDates(),
                beginDate)) {
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
            precipPhrase.append("The snow water equivalent is ").append(amount);

            if (snowFlag.getSnowWaterTotal().isDeparture() && hClimo
                    .getSnowWaterPeriodNorm() != ParameterFormatClimate.MISSING) {
                float valDelta = snowWaterMeasured - snowWaterNorm;

                if (valDelta != 0) {
                    precipPhrase.append(" which is ")
                            .append(aboveBelow(valDelta > 0))
                            .append(" the normal amount");
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
                            String.format(FLOAT_TWO_DECIMALS1, snowWaterNorm))
                            .append(" inches");
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);

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
            precipPhrase.append("The snow water equivalent since July 1 is ")
                    .append(amount);

            if (snowFlag.getSnowWaterJuly1().isDeparture() && hClimo
                    .getSnowWaterJuly1Norm() != ParameterFormatClimate.MISSING) {
                float valDelta = snowWaterJuly1 - snowWaterNormJuly1;

                if (valDelta != 0) {
                    precipPhrase.append(" which is ")
                            .append(aboveBelow(valDelta > 0))
                            .append(" the normal amount");
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
                            snowWaterNormJuly1)).append(" inches");
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);

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
            precipPhrase.append("The deepest snow depth observed was ")
                    .append(amount);

            if (snowFlag.getSnowDepthMax().isTimeOfMeasured()
                    && !periodData.getSnowGroundMaxDateList().isEmpty()
                    && periodData.getSnowGroundMaxDateList().get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE
                    && periodData.getSnowGroundMaxDateList().get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE) {
                precipPhrase.append(" and occurred on ").append(
                        dateSentence(periodData.getSnowGroundMaxDateList()));
            }
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                    .append("The average snow depth observed for the period was ")
                    .append(amount);

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
                    precipPhrase.append(" which is ")
                            .append(aboveBelow(deltaInt > 0)).append(SPACE)
                            .append(intValue).append(SPACE)
                            .append(inchInches(intValue != 1))
                            .append(" the normal average");
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
                        precipPhrase.append(intNorm).append(SPACE)
                                .append(inchInches(intNorm != 1));

                    }
                }
            }
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                precipPhrase.append("1.0 inches of snow or greater fell on ")
                        .append(periodData.getNumSnowGreaterThan1())
                        .append(SPACE).append(dayDays(
                                periodData.getNumSnowGreaterThan1() != 1));
            }

            if (snowFlag.getSnowGE100().isDeparture() && hClimo
                    .getNumSnowGE1Norm() != ParameterFormatClimate.MISSING) {
                float valDelta = (float) periodData.getNumSnowGreaterThan1()
                        - hClimo.getNumSnowGE1Norm();

                if (valDelta != 0) {
                    precipPhrase.append(" which is ");

                    precipPhrase
                            .append(String.format(FLOAT_ONE_DECIMAL,
                                    Math.abs(valDelta)))
                            .append(SPACE).append(aboveBelow(valDelta > 0))
                            .append(" the normal amount");

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
                        precipPhrase.append(" of ")
                                .append(String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNumSnowGE1Norm()))
                                .append(" days");
                    } else {
                        precipPhrase.append(".  The normal amount is ")
                                .append(String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNumSnowGE1Norm()))
                                .append(" days for the period");
                    }
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                        .append("There were no days with snowfall greater than or equal to ")
                        .append(amount);

            } else {
                String amount = (globalConfig
                        .getS1() == ParameterFormatClimate.TRACE)
                                ? StringUtils.capitalize(A_TRACE)
                                : (String.format(FLOAT_ONE_DECIMAL,
                                        globalConfig.getS1()) + " inches");

                precipPhrase.append(amount).append(" or more of snow fell on ")
                        .append(periodData.getNumSnowGreaterThanS1())
                        .append(SPACE).append(dayDays(true))
                        .append(" this period");
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
        }

        // "The maximum 24 hour snowfall was <__ inches>/<a trace>."
        if (snowFlag.getSnow24hr().isMeasured()
                && periodData.getSnowMax24H() != ParameterFormatClimate.MISSING
                && periodData.getSnowMax24H() != 0) {

            String amount = (periodData
                    .getSnowMax24H() == ParameterFormatClimate.TRACE) ? A_TRACE
                            : (String.format(FLOAT_ONE_DECIMAL,
                                    periodData.getSnowMax24H()) + " inches");

            precipPhrase.append("The ").append(MAXIMUM)
                    .append(" 24 hour snowfall was ").append(amount);

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

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);

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

                precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

            precipPhrase.append(" The highest total storm snowfall was ")
                    .append(amount);

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
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

                String snowAmount = String.format(FLOAT_COMMAS_ONE_DECIMAL,
                        periodData.getSnowTotal());

                precipPhrase.append("A total of ").append(snowAmount)
                        .append(" inches ");
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
                                        Math.abs(deltaValue)))
                                .append(" inches ")
                                .append(aboveBelow(deltaValue > 0))
                                .append(" the normal amount");
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
                        precipPhrase.append(" of ").append(amount);
                    }
                } else {
                    String amount = (hClimo
                            .getSnowPeriodNorm() != ParameterFormatClimate.TRACE)
                                    ? A_TRACE
                                    : (String.format(FLOAT_ONE_DECIMAL,
                                            hClimo.getSnowPeriodNorm())
                                            + " inches");
                    precipPhrase.append(".  The normal amount of snowfall is ")
                            .append(amount);

                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);

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

                    precipPhrase
                            .append(hClimo.getSnowPeriodMaxYearList().get(0)
                                    .getYear())
                            .append(PERIOD).append(SPACE).append(SPACE);
                } else {
                    precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                String snowAmount = String.format(FLOAT_COMMAS_ONE_DECIMAL,
                        periodData.getSnowJuly1());

                precipPhrase.append("A total of ").append(snowAmount)
                        .append(" inches ");

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
                    precipPhrase
                            .append(String.format(FLOAT_COMMAS_ONE_DECIMAL,
                                    Math.abs(deltaValue)))
                            .append(" inches ")
                            .append(aboveBelow(deltaValue > 0))
                            .append(" the normal amount");
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
                            precipPhrase.append(" of ")
                                    .append(String.format(FLOAT_ONE_DECIMAL,
                                            hClimo.getSnowJuly1Norm()))
                                    .append(" inches");
                        } else {
                            precipPhrase
                                    .append(".  The normal amount of snowfall since July 1 is ")
                                    .append(String.format(FLOAT_ONE_DECIMAL,
                                            hClimo.getSnowJuly1Norm()))
                                    .append(" inches");
                        }
                    }
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
        }

        if (snowFlag.getSnowAny().isMeasured() && periodData
                .getNumSnowGreaterThanTR() != ParameterFormatClimate.MISSING) {

            if (periodData.getNumSnowGreaterThanTR() == 0 || periodData
                    .getNumSnowGreaterThanTR() == ParameterFormatClimate.TRACE) {

                precipPhrase
                        .append("There have been no days of measurable snow");
            } else {
                precipPhrase.append(periodData.getNumSnowGreaterThanTR())
                        .append(SPACE)
                        .append(dayDays(
                                periodData.getNumSnowGreaterThanTR() != 1))
                        .append(" of measurable snow ")
                        .append(wasWere(
                                periodData.getNumSnowGreaterThanTR() != 1))
                        .append(" observed");

            }

            if (snowFlag.getSnowAny().isDeparture() && hClimo
                    .getNumSnowGETRNorm() != ParameterFormatClimate.MISSING) {
                float deltaValue = (float) periodData.getNumSnowGreaterThanTR()
                        - hClimo.getNumSnowGETRNorm();

                precipPhrase.append(" which is ");

                if (deltaValue != 0) {
                    precipPhrase
                            .append(String.format(FLOAT_ONE_DECIMAL,
                                    Math.abs(deltaValue)))
                            .append(SPACE).append(aboveBelow(deltaValue > 0))
                            .append(" the normal amount");
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
                        precipPhrase.append(" of ")
                                .append(String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNumSnowGETRNorm()))
                                .append(dayDays(
                                        hClimo.getNumSnowGETRNorm() != 1));
                    } else {
                        precipPhrase
                                .append(".  The normal number of days with measurable snowfall is ")
                                .append(String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNumSnowGETRNorm()))
                                .append(dayDays(
                                        hClimo.getNumSnowGETRNorm() != 1));
                    }
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                precipPhrase.append("There were no days with ")
                        .append(PRECIPITATION)
                        .append(" greater than or equal to ").append(amount);

            } else {
                String amount = (globalConfig
                        .getP1() == ParameterFormatClimate.TRACE)
                                ? "A trace or more"
                                : String.format(FLOAT_TWO_DECIMALS2,
                                        globalConfig.getP1()) + " inches";
                precipPhrase.append(amount).append(" of ").append(PRECIPITATION)
                        .append(" fell on ")
                        .append(periodData.getNumPrcpGreaterThanP1())
                        .append(SPACE).append(dayDays(
                                periodData.getNumPrcpGreaterThanP1() != 1));

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

                    precipPhrase.append(" and no days with ")
                            .append(PRECIPITATION)
                            .append(" greater than or equal to ").append(amount)
                            .append(" were observed");

                } else {
                    if (globalConfig.getP2() == ParameterFormatClimate.TRACE) {
                        precipPhrase.append(" and a trace or more of ")
                                .append(PRECIPITATION).append(" fell on ")
                                .append(periodData.getNumPrcpGreaterThanP2())
                                .append(dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    } else {
                        precipPhrase.append(" and ")
                                .append(String.format(FLOAT_TWO_DECIMALS2,
                                        globalConfig.getP2()))
                                .append(" inches of ").append(PRECIPITATION)
                                .append(" or greater fell on ")
                                .append(periodData.getNumPrcpGreaterThanP2())
                                .append(dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    }
                }
            } else {
                if (periodData.getNumPrcpGreaterThanP2() == 0) {
                    String amount = (globalConfig
                            .getP2() == ParameterFormatClimate.TRACE) ? A_TRACE
                                    : String.format(FLOAT_TWO_DECIMALS2,
                                            globalConfig.getP2()) + " inches";
                    precipPhrase.append("There were no days with ")
                            .append(PRECIPITATION)
                            .append(" greater than or equal to ")
                            .append(amount);
                } else {
                    if (globalConfig.getP2() == ParameterFormatClimate.TRACE) {
                        precipPhrase.append("A trace or more of ")
                                .append(PRECIPITATION).append(" fell on ")
                                .append(periodData.getNumPrcpGreaterThanP2())
                                .append(dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    } else {
                        precipPhrase
                                .append(String.format(FLOAT_TWO_DECIMALS2,
                                        globalConfig.getP2()))
                                .append(" inches of ").append(PRECIPITATION)
                                .append(" or greater fell on ")
                                .append(periodData.getNumPrcpGreaterThanP2())
                                .append(dayDays(periodData
                                        .getNumPrcpGreaterThanP2() != 1));
                    }
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
        } else if (precipFlag.getPrecipGEP1().isMeasured()
                && !precipFlag.getPrecipGEP2().isMeasured()) {
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                precipPhrase.append("There were no days with ")
                        .append(PRECIPITATION)
                        .append(" greater than or equal to ")
                        .append(fractionString).append(" inches");
            } else {
                if (fractionString.equals(THRESHOLD_050)) {
                    precipPhrase.append(fractionString).append(" inches of ")
                            .append(PRECIPITATION)
                            .append(" or greater were observed on ")
                            .append(precipValue).append(SPACE)
                            .append(dayDays(precipValue != 1));
                } else {
                    precipPhrase.append(precipValue).append(SPACE)
                            .append(dayDays(precipValue != 1))
                            .append(" with greater than or equal to ")
                            .append(fractionString).append(" inches of ")
                            .append(PRECIPITATION).append(SPACE)
                            .append(wasWere(precipValue != 1))
                            .append(" observed");
                }
            }

            // "... which is <above/below the normal amount>/<normal>
            if (precipFlag.isDeparture()
                    && normValue != ParameterFormatClimate.MISSING) {
                float valDelta = (float) precipValue - normValue;

                if (valDelta != 0) {
                    precipPhrase.append(" which is");

                    precipPhrase
                            .append(String.format(FLOAT_ONE_DECIMAL, valDelta))
                            .append(SPACE).append(aboveBelow(valDelta > 0))
                            .append(" the normal amount");
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
                        precipPhrase.append(" of ")
                                .append(String.format(FLOAT_ONE_DECIMAL,
                                        normValue))
                                .append(dayDays(normValue != 1));
                    } else {
                        precipPhrase
                                .append(". The normal amount is ").append(String
                                        .format(FLOAT_ONE_DECIMAL, normValue))
                                .append(" for the period");
                    }
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
            precipPhrase.append("The ").append(MAXIMUM).append(" 24 hour ")
                    .append(PRECIPITATION).append(" was ").append(amount);

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
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
            precipPhrase.append("The highest total storm ")
                    .append(PRECIPITATION).append(" was ").append(amount);

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
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                precipPhrase.append(partialAvgPhrase).append(" 0.00 inches");
            } else if (periodData
                    .getPrecipMeanDay() == ParameterFormatClimate.TRACE) {
                precipPhrase.append(partialAvgPhrase).append(" a trace");
            } else {
                String avgPrecip = String.format(FLOAT_TWO_DECIMALS1,
                        periodData.getPrecipMeanDay());
                precipPhrase.append(partialAvgPhrase).append(SPACE)
                        .append(avgPrecip).append(" inches");
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
                    precipPhrase.append(" which is ")
                            .append(String.format(FLOAT_TWO_DECIMALS1,
                                    Math.abs(deltaValue)))
                            .append(" inches ")
                            .append(aboveBelow(deltaValue > 0))
                            .append(" the normal average daily amount");

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
                    precipPhrase
                            .append(".  The normal average daily amount of ")
                            .append(PRECIPITATION).append(" is a trace");
                } else {
                    float deltaValue = periodData.getPrecipMeanDay()
                            - hClimo.getPrecipDayNorm();

                    if (deltaValue != 0) {

                        if (precipFlag.getPrecipAvg().isDeparture()
                                && periodData
                                        .getPrecipMeanDay() != ParameterFormatClimate.TRACE) {
                            precipPhrase.append(" of ")
                                    .append(hClimo.getPrecipDayNorm())
                                    .append(" inches");
                        } else {
                            precipPhrase
                                    .append(".  The normal average daily amount of ")
                                    .append(PRECIPITATION).append(" is ")
                                    .append(hClimo.getPrecipDayNorm())
                                    .append(" inches");
                        }
                    }
                }
            }

            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                precipPhrase.append(StringUtils.capitalize(A_TRACE))
                        .append(" of ");
            } else if (Math
                    .abs(periodData
                            .getPrecipTotal()) < ParameterFormatClimate.HALF_PRECIP
                    && periodData
                            .getPrecipTotal() != ParameterFormatClimate.TRACE) {
                precipPhrase.append("No ");
            } else {
                precipPhrase.append("A total of ")
                        .append(String.format(FLOAT_COMMAS_TWO_DECIMALS,
                                periodData.getPrecipTotal()));

                precipPhrase.append(" inches of ");
            }

            precipPhrase.append(PRECIPITATION)
                    .append(" fell during the period");

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
                                        Math.abs(deltaVal)))
                                .append(" inches ")
                                .append(aboveBelow(deltaVal > 0))
                                .append(" the normal amount");

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
                            precipPhrase.append(" of ")
                                    .append(String.format(
                                            FLOAT_COMMAS_TWO_DECIMALS,
                                            hClimo.getPrecipPeriodNorm()));
                        } else {
                            precipPhrase.append(" of ").append(A_TRACE);
                        }

                    }
                } else {
                    precipPhrase.append(".  The normal amount of ")
                            .append(PRECIPITATION).append(" is ");

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
            precipPhrase.append(PERIOD).append(SPACE).append(SPACE);

            if (precipFlag.getPrecipTotal().isRecord() && hClimo
                    .getPrecipPeriodMax() != ParameterFormatClimate.MISSING_PRECIP) {
                if (recordMax == measured && recordMax != 0) {
                    precipPhrase.append("This ties the previous record of ");
                } else if (measured != ParameterFormatClimate.MISSING_PRECIP
                        && measured > recordMax) {
                    precipPhrase.append("This breaks the previous record of ");
                } else {
                    precipPhrase.append("The record amount of ")
                            .append(PRECIPITATION).append(" is ");
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

                    precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
                } else {
                    precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
                }
            }

        }

        if (precipFlag.getPrecipTotal().isMeasured()
                && periodData
                        .getPrecipTotal() != ParameterFormatClimate.MISSING_PRECIP
                && precipFlag.getPrecipMin().isRecord() && hClimo
                        .getPrecipPeriodMin() != ParameterFormatClimate.MISSING_PRECIP) {
            if (recordMin == measured) {
                precipPhrase.append("The total ties the previous record ")
                        .append(MINIMUM).append(" of ");
            } else if (measured < recordMin) {
                precipPhrase.append("The total break the previous ")
                        .append(MINIMUM).append(SPACE).append(PRECIPITATION)
                        .append(" record of ");
            } else {
                precipPhrase.append("The record ").append(MINIMUM).append(SPACE)
                        .append(PRECIPITATION).append(" for the period is ");
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

                precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
            } else {
                precipPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                tempPhrase.append("The ").append(maxMinString).append(SPACE)
                        .append(TEMPERATURE).append(" did not ")
                        .append(exceedFall).append(SPACE);

                tempPhrase.append(decideDegree(compareNum));

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum));
                }
            } else {
                tempPhrase.append("The ").append(maxMinString).append(SPACE)
                        .append(TEMPERATURE).append(" was at or ")
                        .append(aboveBelow(greaterThan)).append(SPACE);

                tempPhrase.append(decideDegree(compareNum));

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum));
                }

                tempPhrase.append(" on ").append(numComparison).append(SPACE)
                        .append(dayDays(numComparison != 1));
            }

            tempPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
                tempPhrase.append("The ").append(maxMinString).append(SPACE)
                        .append(TEMPERATURE).append(" did not ")
                        .append(exceedFall1).append(SPACE).append(compareNum1)
                        .append(" degrees");
                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum1));
                }
            }

            else {
                tempPhrase.append("The ").append(maxMinString).append(SPACE)
                        .append(TEMPERATURE).append(SPACE).append(exceedFall2)
                        .append(SPACE).append(compareNum1).append(" degrees");

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum1));
                }

                tempPhrase.append(" on ").append(numComparison1).append(SPACE)
                        .append(dayDays(numComparison1 != 1))
                        .append(" this period");
            }

            float valDelta = (float) numComparison1 - normComparison1;
            if (tempFlag1.isDeparture()
                    && normComparison1 != ParameterFormatClimate.MISSING) {

                if (valDelta != 0) {

                    tempPhrase.append(" which is ").append(Math.abs(valDelta))
                            .append(SPACE).append(aboveBelow(valDelta > 0))
                            .append(" the normal ");
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

                        tempPhrase.append(normComparison1).append(SPACE)
                                .append(dayString);
                    } else {
                        tempPhrase.append(".  The normal number of days ")
                                .append(exceedFall3).append(SPACE)
                                .append(compareNum1).append(" degrees");

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(compareNum1));
                        }

                        tempPhrase.append(" is ").append(normComparison1);
                    }
                }
            }

            tempPhrase.append(PERIOD).append(SPACE).append(SPACE);
        }

        if (tempFlag2.isMeasured()
                && numComparison2 != ParameterFormatClimate.MISSING) {

            if (numComparison2 == 0) {
                tempPhrase.append("The ").append(maxMinString).append(SPACE)
                        .append(TEMPERATURE).append(" did not ")
                        .append("fall below ").append(compareNum2)
                        .append(" degrees");
                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum2));
                }
            }

            else {
                tempPhrase.append("The ").append(maxMinString).append(SPACE)
                        .append(TEMPERATURE).append(SPACE)
                        .append("was at or below ").append(compareNum2)
                        .append(" degrees");

                if (currentSettings.getControl().isDoCelsius()) {
                    tempPhrase.append(buildNWRCelsius(compareNum2));
                }

                tempPhrase.append(" on ").append(numComparison2).append(SPACE)
                        .append(dayDays(numComparison2 != 1));
            }

            float valDelta = (float) numComparison2 - normComparison2;
            if (tempFlag2.isDeparture()
                    && normComparison2 != ParameterFormatClimate.MISSING) {

                if (valDelta != 0) {

                    tempPhrase.append(" which is ").append(Math.abs(valDelta))
                            .append(SPACE).append(aboveBelow(valDelta > 0))
                            .append(" the normal ");
                } else {
                    tempPhrase.append(" which is normal");
                }
            }

            if (tempFlag2.isNorm()
                    && normComparison2 != ParameterFormatClimate.MISSING) {
                if (valDelta != 0) {
                    if (tempFlag2.isDeparture()) {

                        tempPhrase.append(SPACE).append(normComparison2)
                                .append(SPACE)
                                .append(dayDays(numComparison2 != 1));
                    } else {
                        tempPhrase.append(".  The normal number of days ")
                                .append("below ").append(compareNum2)
                                .append(" degrees");

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(compareNum2));
                        }

                        tempPhrase.append(" is ").append(normComparison2);
                    }
                }
            }

            tempPhrase.append(PERIOD).append(SPACE).append(SPACE);

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
            tempPhrase.append("The average ").append(TEMPERATURE)
                    .append(" was ").append(String.format(FLOAT_ONE_DECIMAL,
                            periodData.getMeanTemp()))
                    .append(" degree");

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
                    tempPhrase.append(" which is ")
                            .append(String.format(FLOAT_ONE_DECIMAL, valDeltaF))
                            .append(" degree");

                    if (Math.abs(valDeltaF) != 1) {
                        tempPhrase.append("s");
                    }

                    if (currentSettings.getControl().isDoCelsius()) {
                        tempPhrase.append(
                                buildNWRCelsius(deltaTempMeanC, numDecimals));
                    }
                }

                if (deltaTempMeanF != 0) {
                    tempPhrase.append(SPACE)
                            .append(aboveBelow(deltaTempMeanF > 0))
                            .append(" the normal average ").append(TEMPERATURE);
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
                        tempPhrase.append(" of ")
                                .append(String.format(FLOAT_ONE_DECIMAL,
                                        hClimo.getNormMeanTemp()))
                                .append(" degree");

                        if (Math.abs(hClimo.getNormMeanTemp()) != 1) {
                            tempPhrase.append("s");
                        }

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(
                                    hClimo.getNormMeanTemp(), numDecimals));
                        }
                    }

                } else {
                    tempPhrase.append(".  The normal average ")
                            .append(TEMPERATURE).append(" for the period is ")
                            .append(String.format(FLOAT_ONE_DECIMAL,
                                    hClimo.getNormMeanTemp()))
                            .append(" degree");
                    if (Math.abs(hClimo.getNormMeanTemp()) != 1) {
                        tempPhrase.append("s");
                    }

                    if (currentSettings.getControl().isDoCelsius()) {
                        tempPhrase.append(buildNWRCelsius(
                                hClimo.getNormMeanTemp(), numDecimals));
                    }
                }
            }

            tempPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
            tempPhrase.append("The ").append(maxMin).append(SPACE)
                    .append(TEMPERATURE).append(" for the period was ")
                    .append(observedTemp);

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
                        tempPhrase.append(" Fahrenheit, or ").append(valDeltaC)
                                .append(" Celsius");
                    }
                }

                if (tempFDelta != 0) {
                    tempPhrase.append(SPACE).append(aboveBelow(tempFDelta > 0))
                            .append(" the normal ").append(maxMin);
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
                        tempPhrase.append(" of ").append(nearestInt);

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(nearestInt));
                        }
                    } else {
                        tempPhrase.append(".  The normal ").append(maxMin)
                                .append(SPACE).append(TEMPERATURE)
                                .append(" is ").append(nearestInt);

                        if (currentSettings.getControl().isDoCelsius()) {
                            tempPhrase.append(buildNWRCelsius(nearestInt));
                        }
                    }
                }
            }

            tempPhrase.append(PERIOD).append(SPACE).append(SPACE);

            // "The maximum/minimum occurred on <date(s)>."
            if (tempFlag.isTimeOfMeasured() && !dates.isEmpty()
                    && dates.get(0)
                            .getDay() != ParameterFormatClimate.MISSING_DATE
                    && dates.get(0)
                            .getMon() != ParameterFormatClimate.MISSING_DATE) {
                tempPhrase.append("The ").append(maxMin).append(" occurred on ")
                        .append(dateSentence(dates)).append(PERIOD)
                        .append(SPACE).append(SPACE);
            }

            // "The record maximum/minimum temperature is __ which last was set
            // in <year>."
            // OR "This breaks/ties the previous record of __ which was set in
            // <year>."
            if (tempFlag.isRecord()
                    && recordTemp != ParameterFormatClimate.MISSING) {
                if ((max && recordTemp > observedTemp)
                        || (!max && recordTemp < observedTemp)) {
                    tempPhrase.append("The record ").append(maxMin)
                            .append(SPACE).append(TEMPERATURE).append(" is ");
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
                tempPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
    *
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
                    datePhrase
                            .append(DateFormatSymbols.getInstance()
                                    .getMonths()[dates.get(i).getMon()])
                            .append(SPACE).append(dates.get(i).getDay());
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

                    datePhrase.append(" and ").append(endMon).append(SPACE)
                            .append(dates.get(i).getEnd().getDay());
                } else {
                    datePhrase.append(" and ")
                            .append(getOrdinal(dates.get(i).getEnd().getDay()));
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

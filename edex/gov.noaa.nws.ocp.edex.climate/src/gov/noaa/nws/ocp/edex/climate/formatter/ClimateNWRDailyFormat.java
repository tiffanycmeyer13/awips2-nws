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
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunDailyData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
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
 * Oct 31, 2017 40112      wpaintsil   Correct period placement in temp sentence.
 * Aug 08, 2018 DR20836    wpaintsil   Minor correction to precip grammar.
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
     * </pre>
     * 
     * @param dailyData
     * @return
     * @throws ClimateInvalidParameterException
     */
    public Map<String, ClimateProduct> buildText(
            ClimateRunData reportData)
                    throws ClimateInvalidParameterException {
        Map<String, ClimateProduct> prod = new HashMap<>();

        Map<Integer, ClimateDailyReportData> reportMap = ((ClimateRunDailyData) reportData)
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
                        + " defined in the ClimateProductType settings object was not found in the ClimateRunData report map.");

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
                productText.append(commentPhrase).append(spaces);
            }

            // temperature phrases
            String tempPhrase = buildNWRTemp(report);
            if (tempPhrase.length() > 0) {
                productText.append(tempPhrase).append(spaces);
            }

            // precipitation phrases
            String precipPhrase = buildNWRPrecip(report, reportData);
            if (precipPhrase.length() > 0) {
                productText.append(precipPhrase).append(spaces);
            }

            // degree day phrases
            String heatCoolPhrase = buildNWRHeatAndCool(report,
                    reportData.getBeginDate());
            if (heatCoolPhrase.length() > 0) {
                productText.append(heatCoolPhrase).append(spaces);
            }

            // wind phrases
            String windPhrase = buildNWRWind(report);
            if (windPhrase.length() > 0) {
                productText.append(windPhrase).append(spaces);
            }

            // relative humidity phrases
            String relHumdPhrase = buildNWRRelHumd(report);
            if (relHumdPhrase.length() > 0) {
                productText.append(relHumdPhrase).append(spaces);
            }

            // normal climatology phrases
            String normPhrase = buildNWRNorms(report);
            if (normPhrase.length() > 0) {
                productText.append(normPhrase).append(spaces);
            }

            // astronomical phrases
            String astroPhrase = buildNWRAstro(report);
            if (astroPhrase.length() > 0) {
                productText.append(astroPhrase).append(spaces);
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
                            WordUtils.capitalize(SUNRISE)).append(SPACE)
                                    .append(TODAY).append(" is at ")
                                    .append(convRise.toHourMinString())
                                    .append(SPACE).append(convRise.getAmpm());
                } else {
                    risePhrase = new StringBuilder("The sun does not rise");
                }
            } else {
                if (noSunrise == 0) {
                    risePhrase = new StringBuilder(
                            WordUtils.capitalize(SUNRISE)).append(SPACE)
                                    .append(TOMORROW).append(" is at ")
                                    .append(convRise.toHourMinString())
                                    .append(SPACE).append(convRise.getAmpm());
                } else {
                    if (noSunset == 0) {
                        risePhrase = new StringBuilder("and there is no ")
                                .append(SUNRISE).append(SPACE).append(TOMORROW);
                    } else if (noSunset == 1) {
                        risePhrase = new StringBuilder("The sun does not rise ")
                                .append(TOMORROW);
                    } else {
                        risePhrase = new StringBuilder("or rise ")
                                .append(TOMORROW);
                    }
                }
            }
        }

        if (noSunset != 1) {
            if (morning) {
                if (noSunset == 0) {
                    if (noSunrise == 1) {
                        setPhrase = new StringBuilder(
                                WordUtils.capitalize(SUNSET)).append(SPACE)
                                        .append(TODAY).append(" is at ")
                                        .append(convSet.toHourMinString())
                                        .append(SPACE)
                                        .append(convSet.getAmpm());
                    } else {
                        setPhrase = new StringBuilder("and ").append(SUNSET)
                                .append(" is at ")
                                .append(convSet.toHourMinString()).append(SPACE)
                                .append(convSet.getAmpm());
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
                    setPhrase = new StringBuilder(WordUtils.capitalize(SUNSET))
                            .append(" tonight is at ")
                            .append(convSet.toHourMinString()).append(SPACE)
                            .append(convSet.getAmpm());
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
                    nwrAstro = new StringBuilder(risePhrase.toString())
                            .append(SPACE).append(TODAY);
                } else {
                    nwrAstro = new StringBuilder(risePhrase.toString())
                            .append(SPACE).append(setPhrase.toString())
                            .append(SPACE).append(TODAY);
                }
            } else if (noSunrise == 1 || noSunset == 1) {
                nwrAstro = new StringBuilder(risePhrase.toString())
                        .append(setPhrase.toString());
            } else {
                nwrAstro = new StringBuilder(risePhrase.toString())
                        .append(SPACE).append(setPhrase.toString());
            }
        } else {
            if (noSunset == 1 && noSunrise == 0) {
                nwrAstro = new StringBuilder(setPhrase.toString())
                        .append(PERIOD).append(SPACE)
                        .append(risePhrase.toString());
            } else if (noSunrise == 1 || noSunset == 1) {
                nwrAstro = new StringBuilder(setPhrase.toString())
                        .append(risePhrase.toString());
            } else {
                nwrAstro = new StringBuilder(setPhrase.toString()).append(SPACE)
                        .append(risePhrase.toString());
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
                nwrNorms.append("The ").append(NORMAL).append(" high ")
                        .append(TEMPERATURE).append(" for ").append(TOMORROW)
                        .append(" is ");
            } else {
                nwrNorms.append("The ").append(NORMAL).append(" high ")
                        .append(TEMPERATURE).append(" for ").append(TODAY)
                        .append(" is ");
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
            nwrNorms.append(", and the ").append(NORMAL).append(" low is ")
                    .append(tClimate.getMinTempMean());

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
                nwrNorms.append("The ").append(NORMAL).append(" low ")
                        .append(TEMPERATURE).append(" for ").append(TOMORROW)
                        .append(" is ");
            } else {
                nwrNorms.append("The ").append(NORMAL).append(" low ")
                        .append(TEMPERATURE).append(" for ").append(TODAY)
                        .append(" is ");
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
            nwrNorms.append(PERIOD).append(SPACE).append(SPACE);
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
                    nwrNorms.append(TOMORROW).append(" is ");
                } else {
                    nwrNorms.append(TODAY).append(" is ");
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
                        nwrNorms.append(" which occurred in ")
                                .append(tClimate.getMaxTempYear()[0]);
                    } else {
                        nwrNorms.append(" which last occurred in ")
                                .append(tClimate.getMaxTempYear()[0]);
                    }

                }
            }
        }

        if (normFlag.isMaxTempRecord()
                && tClimate
                        .getMaxTempYear()[0] != ParameterFormatClimate.MISSING
                && normFlag.isMaxTempRecord() && tClimate
                        .getMinTempYear()[0] != ParameterFormatClimate.MISSING) {

            nwrNorms.append(", and the ").append(RECORD).append(" low is ")
                    .append(tClimate.getMinTempRecord());

            if (!normFlag.isMinTempYear()) {
                nwrNorms.append(".");
            } else {
                nwrNorms.append(", which occurred in ");

                if (tClimate
                        .getMinTempYear()[0] != ParameterFormatClimate.MISSING) {

                    nwrNorms.append(tClimate.getMinTempYear()[0]).append(".");

                }
            }
        }

        if ((!normFlag.isMaxTempRecord()
                || tClimate.getMaxTempRecord() == ParameterFormatClimate.MISSING
                || tClimate
                        .getMaxTempYear()[0] == ParameterFormatClimate.MISSING)
                && normFlag.isMinTempRecord() && tClimate
                        .getMinTempYear()[0] != ParameterFormatClimate.MISSING) {

            nwrNorms.append("The ").append(RECORD).append(" low for ");

            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                nwrNorms.append(TOMORROW).append(" is ");
            } else {
                nwrNorms.append(TODAY).append(" is ");
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

                    nwrRelHumd.append("The ").append(MAXIMUM).append(SPACE)
                            .append(RELATIVE_HUMIDITY).append(" was ")
                            .append(yesterday.getMaxRelHumid()).append(SPACE)
                            .append(PERCENT);
                } else {
                    nwrRelHumd.append(WordUtils.capitalize(TODAY)).append("'s ")
                            .append(MAXIMUM).append(SPACE)
                            .append(RELATIVE_HUMIDITY).append(" was ")
                            .append(yesterday.getMaxRelHumid()).append(SPACE)
                            .append(PERCENT);
                }
            } else {
                nwrRelHumd.append(ClimateNWRFormat.timeFrame(morning, true))
                        .append("'s maximum relative humidity was ")
                        .append(yesterday.getMaxRelHumid()).append(SPACE)
                        .append(PERCENT);
            }

            if (rhFlag.getMinRH().isMeasured() && yesterday
                    .getMinRelHumid() != ParameterFormatClimate.MISSING) {

                nwrRelHumd.append(", and the ").append(MINIMUM).append(SPACE)
                        .append(RELATIVE_HUMIDITY).append(" was ")
                        .append(yesterday.getMinRelHumid()).append(SPACE)
                        .append(PERCENT);
            }
        }

        if ((!rhFlag.getMaxRH().isMeasured()
                || yesterday.getMaxRelHumid() == ParameterFormatClimate.MISSING)
                && (rhFlag.getMinRH().isMeasured() && yesterday
                        .getMinRelHumid() != ParameterFormatClimate.MISSING)) {
            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrRelHumd.append("The ").append(MINIMUM).append(SPACE)
                            .append(RELATIVE_HUMIDITY).append(" was ")
                            .append(yesterday.getMinRelHumid()).append(SPACE)
                            .append(PERCENT);
                } else {
                    nwrRelHumd.append("Today's ").append(MINIMUM).append(SPACE)
                            .append(RELATIVE_HUMIDITY).append(" was ")
                            .append(yesterday.getMinRelHumid()).append(SPACE)
                            .append(PERCENT);
                }
            } else {
                nwrRelHumd.append(ClimateNWRFormat.timeFrame(morning, true))
                        .append("'s ").append(MINIMUM).append(SPACE)
                        .append(RELATIVE_HUMIDITY).append(" was ")
                        .append(yesterday.getMinRelHumid()).append(SPACE)
                        .append(PERCENT);
            }
        }

        if ((rhFlag.getMinRH().isMeasured()
                && yesterday.getMinRelHumid() != ParameterFormatClimate.MISSING)
                || (rhFlag.getMaxRH().isMeasured() && yesterday
                        .getMaxRelHumid() != ParameterFormatClimate.MISSING)) {
            nwrRelHumd.append(PERIOD).append(SPACE).append(SPACE);
        }

        if (rhFlag.getMeanRH().isMeasured()
                && yesterday.getMinRelHumid() != ParameterFormatClimate.MISSING
                && yesterday.getMaxRelHumid() != ParameterFormatClimate.MISSING
                && yesterday
                        .getMeanRelHumid() != ParameterFormatClimate.MISSING) {
            if (!morning || currentSettings
                    .getReportType() == PeriodType.INTER_RAD) {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrRelHumd.append("The mean ").append(RELATIVE_HUMIDITY)
                            .append(" was ").append(yesterday.getMeanRelHumid())
                            .append(SPACE).append(PERCENT).append(PERIOD)
                            .append(SPACE).append(SPACE);
                } else {
                    nwrRelHumd.append(WordUtils.capitalize(TODAY))
                            .append("'s mean ").append(RELATIVE_HUMIDITY)
                            .append(" was ").append(yesterday.getMeanRelHumid())
                            .append(SPACE).append(PERCENT).append(PERIOD)
                            .append(SPACE).append(SPACE);
                }
            } else {
                nwrRelHumd.append(ClimateNWRFormat.timeFrame(morning, true))
                        .append("'s mean ").append(RELATIVE_HUMIDITY)
                        .append(" was ").append(yesterday.getMeanRelHumid())
                        .append(SPACE).append(PERCENT).append(PERIOD)
                        .append(SPACE).append(SPACE);
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
                    nwrWind.append(ClimateNWRFormat.timeFrame(morning, false))
                            .append(". ");
                }
            } else {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrWind.append("so far today. ");
                } else {
                    nwrWind.append(ClimateNWRFormat.timeFrame(morning, true))
                            .append(SPACE);
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

                nwrWind.append("the maximum wind observed was ").append(windMph)
                        .append(" miles an hour");

                if (windDir != ParameterFormatClimate.MISSING) {
                    nwrWind.append(" from the ")
                            .append(ClimateFormat.whichDirection(
                                    yesterday.getMaxWind().getDir(), false));
                } else {
                    nwrWind.append(SPACE);
                }

                // "which occurred at <time> am/pm"
                if (windFlag.getMaxWind().isTimeOfMeasured()
                        && mTime.getHour() != ParameterFormatClimate.MISSING_HOUR
                        && mTime.getMin() != ParameterFormatClimate.MISSING_MINUTE) {
                    nwrWind.append(" which occurred at ")
                            .append(mTime.toHourMinString()).append(SPACE)
                            .append(mTime.getAmpm());
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

            nwrWind.append("The highest wind gust observed was ")
                    .append(gustMph).append(" miles per hour");

            if (yesterday.getMaxGust()
                    .getDir() != ParameterFormatClimate.MISSING
                    && yesterday.getMaxGust().getSpeed() != 0) {
                nwrWind.append(" from the ")
                        .append(ClimateFormat.whichDirection(
                                yesterday.getMaxGust().getDir(), false));
            }

            if (windFlag.getMaxGust().isTimeOfMeasured()
                    && gTime.getHour() != ParameterFormatClimate.MISSING_HOUR
                    && gTime.getMin() != ParameterFormatClimate.MISSING_MINUTE) {
                nwrWind.append(" which occurred at ")
                        .append(gTime.toHourMinString()).append(SPACE)
                        .append(gTime.getAmpm());
            }

            nwrWind.append(PERIOD).append(SPACE).append(SPACE);
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
                    nwrWind.append(ClimateNWRFormat.timeFrame(morning, false))
                            .append(". ");
                }
            } else {
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    nwrWind.append("So far today ");
                } else {
                    nwrWind.append(ClimateNWRFormat.timeFrame(morning, true))
                            .append(SPACE);
                }
                int avgMph = ClimateUtilities.nint(yesterday.getAvgWindSpeed());
                nwrWind.append("the average wind speed was ").append(avgMph)
                        .append(" miles per hour. ");
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
     * </pre>
     * 
     * @param reportData
     * @return
     */
    private String buildNWRHeatAndCool(ClimateDailyReportData reportData,
            ClimateDate beginDate) {
        StringBuilder nwrHeatAndCool = new StringBuilder();

        DegreeDaysControlFlags degreeFlag = currentSettings.getControl()
                .getDegreeDaysControl();

        DailyClimateData yesterday = reportData.getData();
        ClimateRecordDay yClimate = reportData.getyClimate();

        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        boolean coolReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getCoolDates(), beginDate);
        boolean heatReport = ClimateFormat.reportWindow(
                currentSettings.getControl().getHeatDates(), beginDate);

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
                    nwrHeatAndCool.append("There was ")
                            .append(yesterday.getNumHeat());
                }
                // "There were __ "
                else {
                    nwrHeatAndCool.append("There were ")
                            .append(yesterday.getNumHeat());
                }

                // "heating degree day "
                if (yesterday.getNumHeat() == 1) {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        nwrHeatAndCool.append(" heating degree day so far");
                    } else {
                        nwrHeatAndCool.append(" heating degree day ").append(
                                ClimateNWRFormat.timeFrame(morning, false));
                    }
                }
                // "heating degree days "
                else {
                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        nwrHeatAndCool.append(" heating degree days so far");
                    } else {
                        nwrHeatAndCool.append(" heating degree days ").append(
                                ClimateNWRFormat.timeFrame(morning, false));
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
                        nwrHeatAndCool.append(deltaDegDay).append(SPACE)
                                .append(ClimateNWRFormat.aboveBelow(which))
                                .append(SPACE);
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
                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

                // Add monthly heating phrase if the monthly heating flag is on
                if (degreeFlag.getTotalHDD().isTotalMonth() && yesterday
                        .getNumHeatMonth() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("This ");

                    // "This leaves/brings the monthly total to/at __ "
                    nwrHeatAndCool
                            .append(ClimateNWRFormat
                                    .bringLeave(yesterday.getNumHeat() != 0))
                            .append(" the monthly total ")
                            .append(ClimateNWRFormat
                                    .toAt(yesterday.getNumHeat() != 0))
                            .append(SPACE).append(yesterday.getNumHeatMonth());

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
                            nwrHeatAndCool
                                    .append(String.format(INT_COMMAS,
                                            deltaDegDay))
                                    .append(SPACE)
                                    .append(ClimateNWRFormat.aboveBelow(which))
                                    .append(SPACE);
                        }

                        nwrHeatAndCool.append("normal");
                    }

                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

                // Repeat the above for seasonal heating
                if (degreeFlag.getTotalHDD().isTotalSeason() && yesterday
                        .getNumHeatSeason() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("The seasonal total is ")
                            .append(yesterday.getNumHeatSeason());

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
                            nwrHeatAndCool
                                    .append(String.format(INT_COMMAS,
                                            deltaDegDay))
                                    .append(SPACE)
                                    .append(ClimateNWRFormat.aboveBelow(which))
                                    .append(SPACE);
                        }

                        nwrHeatAndCool.append("normal");

                    }

                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

                // repeat the above for yearly heating
                if (degreeFlag.getTotalHDD().isTotalYear() && yesterday
                        .getNumHeatYear() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("The yearly total since July 1 is ")
                            .append(String.format(INT_COMMAS,
                                    yesterday.getNumHeatYear()));

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
                            nwrHeatAndCool
                                    .append(String.format(INT_COMMAS,
                                            deltaDegDay))
                                    .append(SPACE)
                                    .append(ClimateNWRFormat.aboveBelow(which))
                                    .append(SPACE);
                        }

                        nwrHeatAndCool.append("normal");
                    }

                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
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
                        nwrHeatAndCool.append(deltaDegDay).append(SPACE)
                                .append(ClimateNWRFormat.aboveBelow(which))
                                .append(SPACE);
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
                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

                if (degreeFlag.getTotalCDD().isTotalMonth() && yesterday
                        .getNumCoolMonth() != ParameterFormatClimate.MISSING_DEGREE_DAY) {

                    boolean which = yesterday.getNumCool() == 0 ? false : true;

                    nwrHeatAndCool.append("This ")
                            .append(ClimateNWRFormat.bringLeave(which))
                            .append(" the monthly total ")
                            .append(ClimateNWRFormat.toAt(which)).append(SPACE)
                            .append(String.format(INT_COMMAS,
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
                            nwrHeatAndCool
                                    .append(String.format(INT_COMMAS,
                                            deltaDegDay))
                                    .append(SPACE)
                                    .append(ClimateNWRFormat.aboveBelow(which))
                                    .append(SPACE);

                        }

                        nwrHeatAndCool.append("normal");
                    }

                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

                if (degreeFlag.getTotalCDD().isTotalSeason() && yesterday
                        .getNumHeatSeason() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool.append("The seasonal total is ")
                            .append(String.format(INT_COMMAS,
                                    yesterday.getNumCoolSeason()));

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
                            nwrHeatAndCool
                                    .append(String.format(INT_COMMAS,
                                            deltaDegDay))
                                    .append(SPACE)
                                    .append(ClimateNWRFormat.aboveBelow(which))
                                    .append(SPACE);

                        }

                        nwrHeatAndCool.append("normal");
                    }
                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

                if (degreeFlag.getTotalCDD().isTotalYear() && yesterday
                        .getNumCoolYear() != ParameterFormatClimate.MISSING_DEGREE_DAY) {
                    nwrHeatAndCool
                            .append("The yearly total since January 1 is ")
                            .append(String.format(INT_COMMAS,
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
                            nwrHeatAndCool
                                    .append(String.format(INT_COMMAS,
                                            deltaDegDay))
                                    .append(SPACE)
                                    .append(ClimateNWRFormat.aboveBelow(which))
                                    .append(SPACE);
                        }

                        nwrHeatAndCool.append("normal");
                    }
                    nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
                }

            }

            if (heatAndCoolOnly) {
                nwrHeatAndCool.append(PERIOD).append(SPACE).append(SPACE);
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
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRPrecip(ClimateDailyReportData reportData,
            ClimateRunData report) {
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
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRSnowDepth(ClimateDailyReportData reportData,
            ClimateRunData report) {
        StringBuilder depthPhrase = new StringBuilder();

        boolean snowReport = reportWindow(
                currentSettings.getControl().getSnowDates(),
                report.getBeginDate());

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
                depthPhrase.append("There was ")
                        .append(ClimateUtilities
                                .nint(yesterday.getSnowGround()))
                        .append(" inch of snow on the ground");
            } else {
                depthPhrase.append("There were ")
                        .append(ClimateUtilities
                                .nint(yesterday.getSnowGround()))
                        .append(" inches of snow on the ground");
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
     * </pre>
     * 
     * @param reportData
     * @param report
     * @return
     */
    private String buildNWRSnowPrecip(ClimateDailyReportData reportData,
            ClimateRunData report) {
        StringBuilder snowPhrase = new StringBuilder();

        boolean snowReport = reportWindow(
                currentSettings.getControl().getSnowDates(),
                report.getBeginDate());

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
                        snowPhrase.append("No snow fell ").append(
                                ClimateNWRFormat.timeFrame(morning, false));
                    }
                } else {
                    String snowString = String.format(FLOAT_ONE_DECIMAL,
                            yesterday.getSnowDay());

                    if (currentSettings
                            .getReportType() == PeriodType.INTER_RAD) {
                        snowPhrase.append(snowString)
                                .append(" inches of snow has fallen");
                    } else {
                        snowPhrase.append(snowString)
                                .append(" inches of snow fell ")
                                .append(ClimateNWRFormat.timeFrame(morning,
                                        false));
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

                    if (yClimate
                            .getSnowDayRecord() == ParameterFormatClimate.TRACE) {
                        snowPhrase.append(", which ")
                                .append(ClimateNWRFormat.breaksTies(which))
                                .append(" the daily record of a trace");
                    } else {
                        snowPhrase.append(", which ")
                                .append(ClimateNWRFormat.breaksTies(which))
                                .append(" the daily record of ")
                                .append(snowString).append(" inches");
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
                            snowPhrase.append(" set in ").append(recordYear);
                        } else {
                            snowPhrase.append(" last set in ")
                                    .append(recordYear);
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

                    snowPhrase.append(", ");

                    if (!snowRecord) {
                        snowPhrase.append("which ");
                    } else {
                        snowPhrase.append("and ");
                    }

                    if (yesterday
                            .getSnowMonth() == ParameterFormatClimate.TRACE) {
                        snowPhrase.append(ClimateNWRFormat.bringLeave(which))
                                .append(" the monthly total ")
                                .append(ClimateNWRFormat.toAt(which))
                                .append(" a trace");
                    } else {
                        snowPhrase.append(ClimateNWRFormat.bringLeave(which))
                                .append(" the monthly total ")
                                .append(ClimateNWRFormat.toAt(which))
                                .append(SPACE).append(snowString)
                                .append(" inches");
                    }
                }

                snowPhrase.append(PERIOD).append(SPACE).append(SPACE);

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
                        snowPhrase.append("This is normal for ")
                                .append(DateFormatSymbols.getInstance()
                                        .getMonths()[beginDate.getMon() - 1])
                                .append(". ");
                    } else {
                        if (deltaSnow < 0) {
                            which = false;
                            deltaSnow = Math.abs(deltaSnow);
                        } else {
                            which = true;
                        }

                        String snowString = String.format(FLOAT_ONE_DECIMAL,
                                deltaSnow);

                        snowPhrase.append("This is ").append(snowString)
                                .append(" inches ")
                                .append(ClimateNWRFormat.aboveBelow(which))
                                .append(SPACE);

                        snowString = String.format(FLOAT_ONE_DECIMAL,
                                yClimate.getSnowMonthMean());
                        if (snowFlag.getSnowTotal().isNorm()) {

                            snowPhrase.append("the normal amount of ")
                                    .append(snowString).append(" inches for ")
                                    .append(DateFormatSymbols.getInstance()
                                            .getMonths()[beginDate.getMon()
                                                    - 1])
                                    .append(". ");
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
                    snowPhrase
                            .append("The total snowfall for the month stands at ")
                            .append(snowString).append(" inches");
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

                        snowPhrase.append(", which is ").append(snowString)
                                .append(" inches ")
                                .append(ClimateNWRFormat.aboveBelow(which))
                                .append(" normal ");
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

                if (yesterday.getSnowSeason() == ParameterFormatClimate.TRACE) {
                    snowPhrase.append("stands at a trace");
                } else {
                    snowPhrase.append("stands at ").append(snowString)
                            .append(" inches");
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

                        snowPhrase.append(", which is ").append(snowString)
                                .append(" inches ")
                                .append(ClimateNWRFormat.aboveBelow(which))
                                .append(" normal ");
                    }
                }

                if (!snowFlag.getSnowTotal().isTotalYear() || yesterday
                        .getSnowYear() == ParameterFormatClimate.MISSING) {
                    snowPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

                        snowPhrase.append(", which is ").append(snowString)
                                .append(" inches ")
                                .append(ClimateNWRFormat.aboveBelow(which))
                                .append(" normal.  ");
                    }
                } else {
                    snowPhrase.append(PERIOD).append(SPACE).append(SPACE);
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
     * </pre>
     * 
     * @param reportData
     * 
     * @return
     */
    private String buildNWRLiquidPrecip(ClimateDailyReportData reportData,
            ClimateRunData report) {
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
                    liquidPhrase.append("No precipitation fell ")
                            .append(ClimateNWRFormat.timeFrame(morning, false));
                }
            } else {
                String precipString = String.format(FLOAT_TWO_DECIMALS1,
                        yesterday.getPrecip());
                if (currentSettings.getReportType() == PeriodType.INTER_RAD) {
                    liquidPhrase.append(precipString)
                            .append(" inches of precipitation has fallen");
                } else {
                    liquidPhrase.append(precipString)
                            .append(" inches of  precipitation fell ")
                            .append(ClimateNWRFormat.timeFrame(morning, false));
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

                if (yClimate
                        .getPrecipDayRecord() == ParameterFormatClimate.TRACE) {
                    liquidPhrase.append(" which ")
                            .append(ClimateNWRFormat.breaksTies(which))
                            .append(" the daily record of a trace");
                } else {
                    liquidPhrase.append(", which ")
                            .append(ClimateNWRFormat.breaksTies(which))
                            .append(" the daily record of " + precipString)
                            .append(" inches");
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
                        liquidPhrase.append(" set in ").append(recordYear);
                    } else {
                        liquidPhrase.append(" last set in ").append(recordYear);
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

                liquidPhrase.append(", ");

                if (!precipRecord) {
                    liquidPhrase.append("which ");
                } else {
                    liquidPhrase.append("and ");
                }

                if (yesterday
                        .getPrecipMonth() == ParameterFormatClimate.TRACE) {
                    liquidPhrase.append(ClimateNWRFormat.bringLeave(which))
                            .append(" the monthly total ")
                            .append(ClimateNWRFormat.toAt(which))
                            .append(" a trace");
                } else {
                    liquidPhrase.append(ClimateNWRFormat.bringLeave(which))
                            .append(" the monthly total ")
                            .append(ClimateNWRFormat.toAt(which)).append(SPACE)
                            .append(precipString).append(" inches");
                }
            }

            liquidPhrase.append(PERIOD).append(SPACE).append(SPACE);

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
                    liquidPhrase.append("This is normal for ")
                            .append(DateFormatSymbols.getInstance()
                                    .getMonths()[beginDate.getMon() - 1])
                            .append(PERIOD).append(SPACE).append(SPACE);
                } else if (yesterday
                        .getPrecipMonth() == ParameterFormatClimate.TRACE
                        && yClimate.getPrecipMonthMean() == 0) {
                    liquidPhrase.append("The normal precipitation for ")
                            .append(DateFormatSymbols.getInstance()
                                    .getMonths()[beginDate.getMon() - 1])
                            .append(" is zero inches.  ");
                } else {
                    if (deltaPrecip < 0) {
                        which = false;
                        deltaPrecip = Math.abs(deltaPrecip);
                    } else {
                        which = true;
                    }

                    String precipString = String.format(FLOAT_TWO_DECIMALS1,
                            deltaPrecip);

                    liquidPhrase.append("This is ").append(precipString)
                            .append(" inches ")
                            .append(ClimateNWRFormat.aboveBelow(which)).append(SPACE);

                    if (precipFlag.getPrecipTotal().isNorm()) {
                        precipString = String.format(FLOAT_TWO_DECIMALS1,
                                yClimate.getPrecipMonthMean());

                        liquidPhrase.append("the normal amount of ")
                                .append(precipString).append(" inches for ")
                                .append(DateFormatSymbols.getInstance()
                                        .getMonths()[beginDate.getMon() - 1])
                                .append(PERIOD + SPACE + SPACE);
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

            if (yesterday.getPrecipMonth() == ParameterFormatClimate.TRACE) {
                liquidPhrase.append(
                        "The total precipitation for the month stands at a trace");
            } else {
                liquidPhrase
                        .append("The total precipitation for the month stands at ")
                        .append(precipString).append(" inches");
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

                    liquidPhrase.append(", which is ").append(precipString)
                            .append(" inches")
                            .append(ClimateNWRFormat.aboveBelow(which))
                            .append(" normal");
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

            if (yesterday.getPrecipSeason() == ParameterFormatClimate.TRACE) {
                liquidPhrase.append("stands at a trace");
            } else {
                liquidPhrase.append("stands at ").append(precipString)
                        .append(" inches");
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

                    liquidPhrase.append(", which is ").append(precipString)
                            .append(" inches ")
                            .append(ClimateNWRFormat.aboveBelow(which))
                            .append(" normal");
                }
            }

            if (!precipFlag.getPrecipTotal().isTotalYear()
                    || precipFlag.getPrecipTotal().isDeparture() || yesterday
                            .getPrecipYear() == ParameterFormatClimate.MISSING_PRECIP) {
                liquidPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

            if (yesterday.getPrecipYear() == ParameterFormatClimate.TRACE) {
                liquidPhrase.append("a trace");
            } else {
                liquidPhrase.append(precipString).append(" inches");
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

                    liquidPhrase.append(", which is ").append(precipString)
                            .append(" inches ")
                            .append(ClimateNWRFormat.aboveBelow(which))
                            .append(" normal.  ");
                }
            } else {
                liquidPhrase.append(PERIOD).append(SPACE).append(SPACE);
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

                    nwrTempRecordPhrase.append("The ").append(highOrLow)
                            .append(SPACE).append(breakOrTie)
                            .append("the previous ");

                    nwrTempRecordPhrase
                            .append(ClimateNWRFormat.decideDegree(tempRecord));
                }

                if ((max && tempValue < tempRecord)
                        || (!max && tempValue > tempRecord)) {

                    nwrTempRecordPhrase.append("The record ").append(highOrLow)
                            .append(" is ");

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
                        nwrTempRecordPhrase.append(" which was set in ")
                                .append(recordYear);
                    } else {
                        nwrTempRecordPhrase.append(" which was last set in ")
                                .append(recordYear);
                    }

                }
                nwrTempRecordPhrase.append(PERIOD).append(SPACE).append(SPACE);
            }

        }

        return nwrTempRecordPhrase;
    }

    /**
     * Migrated from build_NWR_max_temp.f and build_NWR_min_temp.f. These
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
                        nwrTempPhrase.append("So far today the ")
                                .append(highOrLow)
                                .append(" temperature has been ");
                    } else {
                        if (!tempFlag.isTimeOfMeasured()
                                || tempTime
                                        .getHour() == ParameterFormatClimate.MISSING_HOUR
                                || tempTime
                                        .getMin() == ParameterFormatClimate.MISSING_MINUTE) {
                            nwrTempPhrase.append("Today's ").append(highOrLow)
                                    .append(" temperature was ");
                        } else {
                            nwrTempPhrase.append("Today's ").append(highOrLow)
                                    .append(" temperature of ");
                        }
                    }
                } else {
                    if (!tempFlag.isTimeOfMeasured()
                            || tempTime
                                    .getHour() == ParameterFormatClimate.MISSING_HOUR
                            || tempTime
                                    .getMin() == ParameterFormatClimate.MISSING_MINUTE) {
                        nwrTempPhrase.append("Yesterday's ").append(highOrLow)
                                .append(" temperature was ");
                    } else {
                        nwrTempPhrase.append("Yesterday's ").append(highOrLow)
                                .append(" temperature of ");
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
                    nwrTempPhrase.append(".  The normal ").append(highOrLow)
                            .append(" is ");
                } else {
                    if (tempFDelta != 0) {
                        tempCDelta = Math.abs(tempCDelta);
                        nwrTempPhrase.append(". This ").append(highOrLow)
                                .append(" was ");

                        nwrTempPhrase.append(ClimateNWRFormat
                                .decideDegree(Math.abs(tempFDelta)));

                        if (currentSettings.getControl().isDoCelsius()) {
                            nwrTempPhrase.append(ClimateNWRFormat
                                    .buildNWRCelsius(tempCDelta));
                        }
                    }

                    tempFDelta = tempValue - tempMean;

                    if (tempFDelta > 0) {
                        nwrTempPhrase.append(" above the normal ")
                                .append(highOrLow).append(" of ");
                    } else if (tempFDelta < 0) {
                        nwrTempPhrase.append(" below the normal ")
                                .append(highOrLow).append(" of ");
                    } else {
                        nwrTempPhrase.append(". This tied the normal ")
                                .append(highOrLow).append(" of ");
                    }
                }

                nwrTempPhrase.append(tempMean);

                if (currentSettings.getControl().isDoCelsius()) {
                    nwrTempPhrase
                            .append(ClimateNWRFormat.buildNWRCelsius(tempMean));
                }
            }

            if (!(max && tempOnly)) {
                nwrTempPhrase.append(PERIOD).append(SPACE).append(SPACE);
            }
        }

        return nwrTempPhrase.toString();
    }

}

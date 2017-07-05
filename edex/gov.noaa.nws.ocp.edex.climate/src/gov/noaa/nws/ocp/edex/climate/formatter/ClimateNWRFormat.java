/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.TimeZone;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductHeader;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;

/**
 * Class containing common logic for building NWR text.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2017 21099      wpaintsil   Initial creation
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 12 APR 2017  28536      wpaintsil   Update Headers; expiration and effective times are GMT, 
 *                                     creation time is current.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public abstract class ClimateNWRFormat extends ClimateFormat {

    /**
     * local timezone environment variable
     */
    private static final String FXA_LOCAL_TZ_STRING = "FXA_LOCAL_TZ";

    /**
     * "Escape a" is appended to the beginning of NWR products.
     */
    protected static final String ESCAPE_A = (char) 27 + "" + (char) 97;

    /**
     * "Escape b" is appended to the end of NWR products.
     */
    protected static final String ESCAPE_B = (char) 27 + "" + (char) 98;

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateNWRFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        super(currentSettings, globalConfig);
    }

    protected static String aboveBelow(boolean which) {
        return which ? "above" : "below";
    }

    protected static String bringLeave(boolean which) {
        return which ? "brings" : "leaves";
    }

    protected static String toAt(boolean which) {
        return which ? "to" : "at";
    }

    protected static String timeFrame(boolean which, boolean capitalized) {
        return which ? (capitalized ? "Yesterday" : "yesterday")
                : (capitalized ? "Today" : "today");
    }

    protected static String breaksTies(boolean which) {
        return which ? "breaks" : "ties";
    }

    protected static String mileMiles(boolean plural) {
        return plural ? "miles" : "mile";
    }

    protected static String wasWere(boolean plural) {
        return plural ? "were" : "was";
    }

    protected static String dayDays(boolean plural) {
        return plural ? "days" : "day";
    }

    protected static String inchInches(boolean plural) {
        return plural ? "inches" : "inch";
    }

    /**
     * Migrated from decide_degree.f.
     * 
     * @param temp
     * @return
     */
    protected static String decideDegree(int temp) {
        return temp + " " + (Math.abs(temp) == 1 ? "degree" : "degrees");
    }

    /**
     * Migrated from build_NWR_header.f
     * 
     * <pre>
    *         MAY    1998    BARRY BAXTER                PRC/TDL
    *         AUGUST 1998    BARRY BAXTER                PRC/TDL  UPDATE
    *         Sept.  1998    David O. Miller             PRC/TDL
    *
    *         PURPOSE :  To build the header file for the National Weather 
    *                    Radio (NWR).
    *             
    *       DATA SET USED
    *            NONE
    *    
    *         VARIABLES
    *
    *               INPUT: 
    *       HEADER_PHRASE - CHARACTER BUFFER CONTAINING THE TEMPERATURE SENTENCES.
    *          NWR_HEADER - THIS HOLDS THE VALUES OF "TYPE_nwr_setup.I".
    *
    *               OUTPUT:               
    *       HEADER_PHRASE - CHARACTER BUFFER CONTAINING THE TEMPERATURE SENTENCES.
    *      NWR_HEADER%AIS - ACTIVE/INACTIVE STORAGE UPON OCCURRENCE OF EXPIRATION
    *                       (ACTIVE=A, INACTIVE=I).
    *      NWR_HEADER%ATM - ALERT TONE TO SEND FRONT OF THE MESSAGE 
    *                       (" "=NO ALERT TONE BUT NWSAME TONE,"N" NO ALERT TONE AND
    *                       NO NWRSAME TONE, "A"=ALERT TONE AND NWRSAME TONE).
    *      NWR_HEADER%BMI - BEGIN MESSAGE INDICATOR.
    *      NWR_HEADER%CC* - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE CC* IS THE NODE
    *                       ORIGINATION SITE.
    *      NWR_HEADER%DSM - DELETE/SAVE MESSAGE (DELETE-D, SAVE-S).
    *      NWR_HEADER%FTN - FIRST TWO SPECIFI* PRODUCT DESIGNATORS OF XXX IN AFOS PRODUCT
    *                       IDENTIFIER (CCCNNNXXX) ARE THE CLIMATE IDENTIFIERS.
    *                       1 - CL = THIS STANDS FOR THE CLIMATE.
    *      NWR_HEADER%IFM - INTERRUPT FLAG FOR THE MESSAGE (I=INTERRUPT, (SPACE)=NO INTERRUPT).
    *      NWR_HEADER%LA* - THE LISTENING AREA CODE FIELD.
    * NWR_HEADER%IPRODNTM - THE # OF THE CURRENT LISTENING TOWERS (1 - 10).
    *      NWR_HEADER%MCO - MESSAGE CONFIRMATION ON OR OFF (ON="C", OFF=(SPACE)).
    *      NWR_HEADER%XXX - IN AFOS PRODUCT IDENTIFIER (CCCNNNXXX), THE NNN IS THE STATION
    *                       THAT YOU ARE DOING FOR THE CLIMATE.
    * NWR_HEADER%STATIONID - THE THREE LETTER CODE FOR THE STATION THAT IS BEING BROADCASTED.
    *      NWR_HEADER%THN - THIRD SPECIFI* PRODUCT DESIGNATOR OF NNN IN AFOS PRODUCT IDENTIFIER
    *                       (CCCNNNXXX) IS THE IDENTIFER FOR WHICH HEADER PRODUCT YOUR GOING TO
    *                       USE.
    *                       1 - M = CLIMATE MONTHLY
    *                       2 - I = CLIMATE DAILY
    *                       3 - S = CLIMATE SEASON
    *      NWR_HEADER%VTT - MESSAGE FORMAT (V_ENG=ENGLISH VOICE, V_SPA=SPANISH VOICE,
    *                       T_ENG=ENGLISH TEXT, T_SPA=SPANISH TEXT).
    *
    *               LOCAL
    *            NUM_CHAR - Number of characters in the string.
    *          NUM_DIGITS - The number of digits in a given number; necessary for
    *                       the conversion of numeric values to character.                            
    *
    *      INCLUDE files
    *     DEFINE_general_phrases.I - Contains character strings need to build all
    *                                 types of sentences.
    *      DEFINE_precip_phrases.I - Contains character strings needed to build
    *                                 precip sentences.
    *   PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                                 arrays, etc.  This INCLUDE file must always
    *                                 come first.
    *         TYPE_climate_dates.I - Defines the derived TYPE "climate_dates";
    *                                 note that it uses the dervied TYPE "date" - 
    *                                 the "date" file must be INCLUDED before this
    *                                 one.
    *    TYPE_climate_record_day.I - Defines the derived TYPE used to store the
    *                                 historical climatological record for a given
    *                                 site for a given day.  Uses derived TYPE
    *                                 "wind" so that INCLUDE file must come before
    *                                 this one.
    *                  TYPE_date.I - Defines the derived TYPE "date";
    *                                 This INCLUDE file must always come before any
    *                                 other INCLUDE file which uses the "date" TYPE.
    *    TYPE_daily_climate_data.I - Defines the derived TYPE used to store the
    *                                 observed climatological data.  Note that
    *                                 INCLUDE file defining the wind TYPE must be 
    *                                 specified before this file.
    *    TYPE_do_weather_element.I - Defines the derived TYPE used to hold the
    *                                 controlling logic for producing
    *                                 sentences/reports for a given variable.
    *  TYPE_report_climate_norms.I - Defines the derived TYPE used to hold the
    *                                 flags which control reporting the
    *                                 climatological norms for different
    *                                 meteorological variables.
    *                  TYPE_time.I - Defines the derived TYPE used to specify the
    *                                 time.
    *                  TYPE_wind.I - Defines the derived TYPE used to specify wind
    *                                 speed,and direction.
    *
    *         NON-SYSTEM SUBROUTINE USED
    *                  NONE
    *
    *         NON-SYSTEM FUNCTIONS USED
    *
    *                   PICK_DIGITS - Returns the number of digits in a number.
    *                        STRLEN - C function: returns the number of characters
    *                                 in a string; string must be terminated wit
    *                                 the NULL character.
     * </pre>
     * 
     * @return
     */
    protected String buildNWRHeader() {
        StringBuilder nwrHeader = new StringBuilder();

        // Create first part of the header phrase
        nwrHeader.append(
                ESCAPE_A + currentSettings.getHeader().getMessageFormat()
                        + currentSettings.getHeader().getNodeOrigSite()
                        + currentSettings.getHeader().getProductCategory()
                        + currentSettings.getHeader().getStationId());

        // Update header expiration, effective, and creation times.
        updateNWRHeader();

        // insert creation date
        ClimateDate creationDate = currentSettings.getHeader()
                .getCreationDate();
        ClimateTime creationTime = currentSettings.getHeader()
                .getCreationTime();

        nwrHeader.append(timeHeader(creationDate, creationTime));

        ClimateDate effectiveDate = currentSettings.getHeader()
                .getEffectiveDate();
        ClimateTime effectiveTime = currentSettings.getHeader()
                .getEffectiveTime();

        nwrHeader.append(timeHeader(effectiveDate, effectiveTime));

        // Periodicity is not a date & time in the header object like in
        // legacy, just minutes; convert to the appropriate format.
        nwrHeader.append(periodicityHeader(
                currentSettings.getHeader().getPeriodicity()));

        // Create second part of the header phrase
        nwrHeader.append(currentSettings.getHeader().getActiveStorage()
                + currentSettings.getHeader().getDelSaveMsg()
                + currentSettings.getHeader().getMessageConfirm() + "   "
                + currentSettings.getHeader().getInterruptMsg()
                + currentSettings.getHeader().getAlertTone()
                + currentSettings.getHeader().getListenAreaCode());

        ClimateDate expirationDate = currentSettings.getHeader()
                .getExpirationDate();
        ClimateTime expirationTime = currentSettings.getHeader()
                .getExpirationTime();

        nwrHeader.append(timeHeader(expirationDate, expirationTime));

        return nwrHeader.toString();
    }

    /**
     * Convert periodicity minutes to days, hours, minutes, seconds in the
     * format DDHHMMSS. Example: 00002000 = 20 minutes.
     * 
     * @param minutes
     * @return
     */
    private static String periodicityHeader(int minutes) {
        return String.format(TWO_DIGIT_INT, minutes / TimeUtil.MINUTES_PER_DAY)
                + String.format(TWO_DIGIT_INT,
                        minutes / TimeUtil.MINUTES_PER_HOUR
                                % TimeUtil.HOURS_PER_DAY)
                + String.format(TWO_DIGIT_INT,
                        minutes % TimeUtil.MINUTES_PER_HOUR)
                + "00";

    }

    /**
     * Migrated from build_NWR_comment.f
     * 
     * <pre>
    *   March 1998     Jason P. Tuell        PRC/TDL
    *   Sept. 1998     David O. Miller       PRC/TDL
    *   Oct.  1999     Bonnie E. Reed    PRC/TDL
    *
    *   Purpose:  This subroutine builds the comment (i.e, first sentence) 
    *             for a given station in the climate summary.
    * 
    *
    *   Variables
    *
    *      Input
    *               adate - derived TYPE which contains the valid date for this
    *                       climate summary.
    *    climate_stations - derived TYPE which contains the station ids and plain
    *                       language stations names for the stations in this
    *                       climate summary.
    *      comment-phrase - buffer containing the comment phrase.
    *             morning - flag which differentiates morning and evening reports.
    *                       =1, morning report
    *                       =2, evening report
    *                 num - index to the station for this climate summary.
    *
    *      Output
    *      comment-phrase - buffer containing the comment phrase.
    *
    *
    *      Local
    *                cday - character version of the INTEGER day.
    *               cyear - character version of the INTEGER year.
    *                ilen - # of characters, i.e., length, of a character buffer.
    *            num_char - # of characters, i.e., length, of a character buffer.
    *           num_char1 = # of characters, i.e., length, of a character buffer.
    *        
    *      INCLUDE files
    *    DEFINE_general_phrases.I - Contains character strings need to build all
    *                               types of sentences.
    *  PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                               arrays, etc.  This INCLUDE file must always
    *                               come first.
    *     TYPE_climate_stations.I - Defines the derived TYPE which specifies the
    *                               climate stations
    *                 TYPE_date.I - Defines the derived TYPE "date";
    *                               This INCLUDE file must always come before any
    *                               other INCLUDE file which uses the "date" TYPE.
    *                 TYPE_time.I - Defines the derived TYPE used to specify the
    *                               time.
    *
    *      Non-system routines used
    *       convert_character_int - Converts INTEGER numbers to CHARACTER
    *
    *      Non-system functions used
    *
    *      Special system functions used
    *                      strlen - C function; returns the number of characters in
    *                               a string; string must be terminated with the
    *                               NULL character.
    *
    *     MODIFICATION HISTORY
    *   April 2000  Doug Murphy     Edited seasonal date section
    *                       (changed from mm/dd/yyyy format to
    *                        month year format)
     * </pre>
     * 
     * @param stationId
     * @param reportData
     * @return
     */
    protected String buildNWRComment(Integer stationId,
            ClimateCreatorResponse reportData) {
        StringBuilder nwrComment = new StringBuilder();

        String stationName = stationMap.get(stationId).getStationName();
        PeriodType type = currentSettings.getReportType();
        ClimateTime validTime = ClimateTime
                .getDailyValidTime(type, globalConfig).to12HourTime();
        String monSea = "";
        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        // "The <station name> climate summary "
        nwrComment.append("The " + stationName + " climate summary ");

        if (type == PeriodType.MONTHLY_RAD) {
            monSea = "month";
        } else if (type == PeriodType.SEASONAL_RAD) {
            monSea = "season";
        } else if (type == PeriodType.ANNUAL_RAD) {
            monSea = "year";
        }

        // for the <month/season/year> <as of>/<for this evening, as of>/<for
        // today/yesterday>
        if (type == PeriodType.MONTHLY_RAD || type == PeriodType.SEASONAL_RAD
                || type == PeriodType.ANNUAL_RAD) {
            nwrComment.append("for the " + monSea + SPACE);
        } else if (type == PeriodType.EVEN_RAD
                || type == PeriodType.INTER_RAD) {
            ClimateTime tempTime = validTime;
            if (type == PeriodType.INTER_RAD) {
                nwrComment.append("as of ");
            } else {
                nwrComment.append("for this evening, as of ");
            }

            nwrComment.append(tempTime.toHourMinString() + SPACE
                    + tempTime.getAmpm() + ",");
        } else {
            nwrComment.append(
                    "for " + ClimateNWRFormat.timeFrame(morning, false) + ",");
        }

        // Add the last part, the begin/end dates
        if (type == PeriodType.MONTHLY_RAD) {
            ClimateDate endDate = ((ClimateCreatorPeriodResponse) reportData)
                    .getEndDate();

            nwrComment.append(" of "
                    + DateFormatSymbols.getInstance()
                            .getMonths()[endDate.getMon() - 1]
                    + ", " + endDate.getYear() + PERIOD + SPACE + SPACE);
        } else if (type == PeriodType.SEASONAL_RAD) {
            ClimateDate beginDate = new ClimateDate(reportData.getBeginDate());
            ClimateDate endDate = ((ClimateCreatorPeriodResponse) reportData)
                    .getEndDate();

            nwrComment.append(", from " + DateFormatSymbols.getInstance()
                    .getMonths()[beginDate.getMon() - 1]);
            if (beginDate.getMon() > endDate.getMon()) {
                nwrComment.append(SPACE + beginDate.getYear());
            }

            nwrComment.append(" to "
                    + DateFormatSymbols.getInstance()
                            .getMonths()[endDate.getMon() - 1]
                    + " " + endDate.getYear() + PERIOD + SPACE + SPACE);
        } else if (type == PeriodType.ANNUAL_RAD) {
            ClimateDate endDate = ((ClimateCreatorPeriodResponse) reportData)
                    .getEndDate();
            nwrComment.append(
                    " of " + endDate.getYear() + PERIOD + SPACE + SPACE);
        } else {
            ClimateDate beginDate = new ClimateDate(reportData.getBeginDate());
            nwrComment.append(SPACE
                    + DateFormatSymbols.getInstance()
                            .getMonths()[beginDate.getMon() - 1]
                    + SPACE + beginDate.getDay() + ", " + beginDate.getYear()
                    + PERIOD + SPACE + SPACE);
        }

        return nwrComment.toString();
    }

    /**
     * Migrated from build_NWR_celsius.f. Formats decimal values for celsius.
     * 
     * @param value
     * @param numDecimals
     *            the number of decimal places for decimal values
     * @return
     */
    protected static String buildNWRCelsius(double value, int numDecimals) {
        return " fahrenheit, or "
                + String.format("%." + numDecimals + "f",
                        ClimateUtilities.fahrenheitToCelsius(value))
                + " celsius";
    }

    /**
     * Migrated from build_NWR_celsius.f. Formats integer values for celsius.
     * 
     * @param value
     * @return
     */
    protected static String buildNWRCelsius(double value) {
        return buildNWRCelsius(value, 0);
    }

    /**
     * Migrated from insert_lf.f.
     * 
     * <pre>
    *   Mar 98          Jason P. Tuell                                 PRC
    *
    *
    *   Purpose:  This routine inserts a LF character approximately every
    *             80 spaces so that the output is easier to display and edit.
    *             
    *
    *   Variables
    *
    *      Input
    *        in_buffer           - character buffer
    *        max_char1           - maximum size of in_buffer
    *        num_char            - # of characters in in_buffer
    *        num_line            - # of characters at which to insert 
    *                              line feed
    *      
    *      Output
    *        num_char_out        - # of characters in the output buffer 
    *
    *      Local
    *        big_blank           - blank phrase equal in size to out_buffer
    *        blank               - parameter set to a value of " "
    *        i                   - loop index
    *        irest               - remaining # of characters to be added to 
    *                              out_buffer
    *        lf                  - line feed character
    *        max_big_char        - maximum # of characters in in_buffer
    *        num_big             - max size of out_buffer
    *        num_feed            - # of characters in the line feed character
    *        num_old             - character position pointer in in_buffer
    *        num_new             - character position pointer in out_buffer
    *        out_buffer          - output character buffer
     * </pre>
     * 
     * @param inputText
     * @return
     */
    protected static StringBuilder insertNewLines(StringBuilder inputText) {
        StringBuilder outputText = new StringBuilder();

        /*
         * Legacy comment: Loop until you are on the last line of characters in
         * the buffer. The inner loop start at the end of a line and decrement
         * until a space is found. It will load the line of characters from the
         * the input buffer into the output buffer up to the space and then will
         * insert a line feed character into the output buffer after the space.
         */
        int k = 0;
        int i = ParameterFormatClimate.NUM_LINE - 1;
        while (i < inputText.length()) {
            for (int j = i; j > 0; j--) {
                if (inputText.substring(j, j + 1)
                        .equals(ParameterFormatClimate.BLANK)) {
                    outputText.append(inputText.substring(k, j) + "\n");

                    k = j + 1;
                    i = j + ParameterFormatClimate.NUM_LINE;
                    break;
                }

            }
        }
        // append any remaining text
        if (k < inputText.length()) {
            outputText.append(inputText.substring(k));
        }

        return outputText;
    }

    /**
     * Migrated from time_header.f
     * 
     * <pre>
    *         NOVEMBER 1997   BARRY BAXTER   PR* UNIX
    *
    *         PURPOSE
    *
    *        The purpose of the subroutine is to take the broadcast start date and
    *        time and convert them over to character form, so they can be put into
    *        the "header_phrase output sentence".
    *             
    *         DATA SET USED
    *            NONE
    *    
    *         VARIABLES
    *  CHAR EFFECTIVE_DAY    - This will hold the character value of the start
    *                          broadcasting listing area code day.
    *  CHAR EFFECTIVE_MONTH  - This will hold the character value of the start
    *                          broadcasting listing area code month.
    *  CHAR EFFECTIVE_YEAR   - This will hold the character value of the start
    *                          broadcasting listing area code year.
    *  CHAR EFFECTIVE_HOUR   - This will hold the character value of the start
    *                          broadcasting listing area code hour.
    *  CHAR EFFECTIVE_MIN    - This will hold the character value of the start
    *                          broadcasting listing area code mintue.
    *  CHAR EXPIRATION_DAY   - This will hold the character value of the end
    *                          broadcasting listing area code day.
    *  CHAR EXPIRATION_IHOUR - This will hold the character value of the end
    *                          broadcasting listing area code hour.
    *  CHAR EXPIRATION_IMIN  - This will hold the character value of the end
    *                          broadcasting listing area code minute.
    *  CHAR EXPIRATION_MONTH - This will hold the character value of the end
    *                          broadcasting listing area code month.
    *  CHAR EXPIRATION_YEAR  - This will hold the character value of the end
    *                          broadcasting listing area code year.
    *               NUM_CHAR - Number of characters in a string. 
    *
    *         INPUT: 
    *
    *          HEADER_PHRASE - Character buffer containing the header string.
    *
    *         OUTPUT:
    *
    *          HEADER_PHRASE - Character buffer containing the header string.
    *
    *      INCLUDE files
    *     DEFINE_general_phrases.I - Contains character strings need to build all
    *                                 types of sentences.
    *        DEFINE_temp_phrases.I - Contains character strings needed to build
    *                                 temperature sentences.
    *   PARAMETER_format_climate.I - Contains all paramters used to dimension
    *                                 arrays, etc.  This INCLUDE file must always
    *                                 come first.
    *         TYPE_climate_dates.I - Defines the derived TYPE "climate_dates";
    *                                 note that it uses the dervied TYPE "date" - 
    *                                 the "date" file must be INCLUDED before this
    *                                 one.
    *    TYPE_climate_record_day.I - Defines the derived TYPE used to store the
    *                                 historical climatological record for a given
    *                                 site for a given day.  Uses derived TYPE
    *                                 "wind" so that INCLUDE file must come before
    *                                 this one.
    *                 TYPE_date.I  - Defines the derived TYPE "date";
    *                                 This INCLUDE file must always come before any
    *                                 other INCLUDE file which uses the "date" TYPE.
    *    TYPE_daily_climate_data.I - Defines the derived TYPE used to store the
    *                                 observed climatological data.  Note that
    *                                 INCLUDE file defining the wind TYPE must be 
    *                                 specified before this file.
    *    TYPE_do_weather_element.I - Defines the derived TYPE used to hold the
    *                                 controlling logic for producing
    *                                 sentences/reports for a given variable.
    *  TYPE_report_climate_norms.I - Defines the derived TYPE used to hold the
    *                                 flags which control reporting the
    *                                 climatological norms for different
    *                                 meteorological variables.
    *                  TYPE_time.I - Defines the derived TYPE used to specify the
    *                                 time.
    *                  TYPE_wind.I - Defines the derived TYPE used to specify wind
    *
    *    NON-SYSTEM SUBROUTINE USED
    *                        STRLEN - C function: returns the number of characters
    *                                 in a string; string must be terminated with
    *                                 the NULL character.
     * </pre>
     * 
     * @param nwrDate
     * @param validTime
     * @param nwrHeader
     * @return
     */
    private static String timeHeader(ClimateDate nwrDate,
            ClimateTime validTime) {

        StringBuilder nwrHeader = new StringBuilder();

        nwrHeader.append(String.valueOf(nwrDate.getYear()).substring(2, 4));

        nwrHeader.append(String.format(TWO_DIGIT_INT, nwrDate.getMon()));

        nwrHeader.append(String.format(TWO_DIGIT_INT, nwrDate.getDay()));

        nwrHeader.append(String.format(TWO_DIGIT_INT,
                validTime.to24HourTime().getHour()));

        nwrHeader.append(String.format(TWO_DIGIT_INT, validTime.getMin()));

        // return YYMMDDhhmm
        return nwrHeader.toString();
    }

    /**
     * Migrated from update_NWR_header.f.
     * 
     * <pre>
     *   August 1998     Jason P. Tuell        PRC/TDL
    *   August 1998     Barry N. Baxter       PRC/TDL
    *   February 1999   Jason P. Tuell        PRC/TDL
    *
    *
    *   Purpose: This program updates the NWR header information.  This
    *            converts the effective and expiration times from local
    *            to UTC and inserts the proper date for this execution.
    *            It also sets the creation date and time.
    * 
    *   Variables
    *
    *      Input
    *        nwr_header  - structure that contains information necessary
    *                      for the CRS.  See TYPE definition for more 
    *                      details
    *
    *      Output
    *
    *        nwr_header  - structure that contains information necessary
    *                      for the CRS.  See TYPE definition for more 
    *                      details
    *
    *      Local
    *        iday        - Julian day for current date
    *        iday_eff    - Julian day for effective date
    *        iday_exp    - Julian day for expiration date
    *        ihour_eff   - hours for effective date
    *        ihour_exp   - hours for expiration date
    *        imin_eff    - minutes for effective date
    *        imin_exp    - minutes for expiration date
    *        ioff        - hours for the offset from UTC
    *        isec        - number of seconds from reference start of system
    *        x_hour_local - hours of execution time in local time
    *        
    *
    *      Non-system routines used
    *        get_offset     - returns the offset from UTC for the WFO
    *        convert_julday - returns a date for an input Julian day
    *           
    *
    *      Non-system functions used
    *         julday        - returns a Julian day for an input date
    *
    *     MODIFICATION HISTORY   NAME         DATE   CHANGE
    *                          Dave Miller   7/7/99  Removed getenv call
    *                                                and restricted
    *                                                it to the called 
    *                                                routine get_offset.
     * </pre>
     *
     * @param header
     * @return
     */
    private void updateNWRHeader() {

        // Get current UTC time.
        Calendar cal = TimeUtil.newGmtCalendar();
        ClimateDate utcDate = new ClimateDate(cal);
        ClimateTime utcTime = new ClimateTime(cal);

        ClimateProductHeader header = currentSettings.getHeader();

        // Set the nwr creation time to the current UTC date-time.
        header.setCreationDate(utcDate);
        header.setCreationTime(utcTime);

        String timeZoneString = System.getenv(FXA_LOCAL_TZ_STRING);

        if (timeZoneString == null) {
            timeZoneString = GMT_STRING;
        }
        TimeZone localTimeZone = TimeZone.getTimeZone(timeZoneString);

        // Convert effective date and time to UTC
        Calendar effectiveLocal = TimeUtil.newCalendar(localTimeZone);
        effectiveLocal.set(header.getEffectiveDate().getYear(),
                header.getEffectiveDate().getMon() - 1,
                header.getEffectiveDate().getDay(),
                header.getEffectiveTime().getHour(),
                header.getEffectiveTime().getMin());

        Calendar effectiveGMT = TimeUtil.newGmtCalendar();
        effectiveGMT.setTimeInMillis(effectiveLocal.getTimeInMillis());
        header.setEffectiveDate(utcDate);
        header.setEffectiveTime(new ClimateTime(effectiveGMT));

        // Convert expiration date and time to UTC
        Calendar expirationLocal = TimeUtil.newCalendar(localTimeZone);
        expirationLocal.set(header.getExpirationDate().getYear(),
                header.getExpirationDate().getMon() - 1,
                header.getExpirationDate().getDay(),
                header.getExpirationTime().getHour(),
                header.getExpirationTime().getMin());

        Calendar expirationGMT = TimeUtil.newGmtCalendar();
        expirationGMT.setTimeInMillis(expirationLocal.getTimeInMillis());
        header.setExpirationDate(utcDate);
        header.setExpirationTime(new ClimateTime(expirationGMT));

        currentSettings.setHeader(header);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorPeriodResponse;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateCreatorResponse;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductHeader;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimatePeriodDAO;

/**
 * Class containing common logic for building NWWS products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2017 21099      wpaintsil   Initial creation
 * Apr 12, 2017 28536      wpaintsil   Update CDE4 in header to current UTC time.
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 10 MAY 2017  30162      wpaintsil   Address FindBugs issues with date format constants.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public abstract class ClimateNWWSFormat extends ClimateFormat {

    /**
     * TODO: The following constants are also used in RecordClimate. Move them
     * to ParameterFormatClimate.
     **/

    /**
     * The default timezone to use.
     */
    public static final String DEFAULT_IFPS_SITE_TIMEZONE = "GMT";

    /**
     * Format for date and time in NWWS header; e.g. 237 PM EST THU JUL 16 2004
     */
    public static final String HEADER_DATE_FORMAT_STRING = "hmm a z E MMM dd yyyy";

    public static final String RECORD_DATE_FORMAT_STRING = "MM/dd/yyyy";

    public static final String SHORT_DATE_FORMAT_STRING = "MM/dd";

    public static final String YEAR_FORMAT_STRING = "yyyy";

    protected static final String RECORD_SYMBOL = "*";

    /**
     * Migrated from build_NWWS_footnote.f. The subroutine just returns the same
     * same string, so it can be reduced to a constant.
     */
    public static final String NWWS_FOOTNOTE = "\n-  Indicates negative numbers.\n"
            + RECORD_SYMBOL + "  Indicates record was set or tied.\n"
            + ParameterFormatClimate.MM + " Indicates data is missing.\n"
            + ParameterFormatClimate.TRACE_SYMBOL
            + "  Indicates Trace Amount.\n";

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateNWWSFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        super(currentSettings, globalConfig);
    }

    /**
     * Get a formatted header date/time string, e.g. 237 PM EST THU JUL 16 2004.
     * 
     * @param date
     * @param timeZone
     * @return
     */
    protected static String getHeaderDateFormat(Calendar date,
            TimeZone timeZone) {
        SimpleDateFormat headerDateFormat = new SimpleDateFormat(
                HEADER_DATE_FORMAT_STRING);
        headerDateFormat.setTimeZone(timeZone);
        return headerDateFormat.format(date.getTime());
    }

    /**
     * Get a formatted MM/dd/yyyy date string.
     * 
     * @param date
     * @return
     */
    protected static String getRecordDateFormat(Calendar date) {
        return new SimpleDateFormat(RECORD_DATE_FORMAT_STRING)
                .format(date.getTime());
    }

    /**
     * Get a formatted MM/dd date string.
     * 
     * @param date
     * @return
     */
    protected static String getShortDateFormat(Calendar date) {
        return new SimpleDateFormat(SHORT_DATE_FORMAT_STRING)
                .format(date.getTime());
    }

    /**
     * Get a formatted yyyy year string.
     * 
     * @param date
     * @return
     */
    protected static String getYearFormat(Calendar date) {
        return new SimpleDateFormat(YEAR_FORMAT_STRING).format(date.getTime());
    }

    /**
     * Based on user-defined global configuration, the record symbol is either
     * "R" or "*", the negative number symbol is either "M" or "-", colons are
     * included or removed, and the full text is either mixed-case or
     * upper-case.
     * 
     * @param text
     * @return
     */
    protected String applyGlobalConfig(String text) {
        text = globalConfig.isNoAsterisk()
                ? StringUtils.replace(text, RECORD_SYMBOL, "R") : text;

        /*
         * There is one instance in buildNWWSAstro in which "--" is used for
         * time that we don't want replaced with "MM"
         */
        text = StringUtils.replace(text, "--", "~~");

        // replace negative signs if necessary
        text = globalConfig.isNoMinus() ? StringUtils.replace(text, "-", "M")
                : text;

        text = StringUtils.replace(text, "~~", "--");

        // remove colons if necessary
        text = globalConfig.isNoColon() ? StringUtils.replace(text, COLON, "")
                : text;

        // change to uppercase if necessary
        text = globalConfig.isNoSmallLetters() ? text.toUpperCase() : text;

        return text;
    }

    /**
     * Migrated from build_NWWS_header.f
     * 
     * <pre>
     *   March  1998     Jason P. Tuell        PRC/TDL
    *   July   1998     Barry N. Baxter       PRC/TDL
    *   August 1998     Barry N. Baxter       PRC/TDL  UPDATE
    *   September 1998  Jason P. Tuell        PRC/TDL
    *                   - modified routine to accomodate increased
    *                     size of character strings required by C
    *
    *
    *   Purpose: To build the header file for the National Weather 
    *                    Wire Service (NWWS).
    * 
    *
    *   Variables
    *
    *      Input
    *
    *         ALL_PHRASES - buffer containing the climate report.
    *
    *
    *      Output
    *
    *         ALL_PHRASES - buffer containing the climate report.
    *
    *
    *      Local
    *       error_problem - text string containing warning to be put in the log
    *     NWWS_HEADER%AAA - The address for the transmition to the station
    *                       that they are writing for. (3 numbers)
    *     NWWS_HEADER%CCC - The three letter code for the station is 
    *                       sending out the National Weather Wire Service 
    *                       (NWWS) AFOS data.
    *    NWWS_HEADER%CDE1 - This will hold the fix value of "TTAA00".
    *    NWWS_HEADER%CDE2 - This will hold the fix value of "K".
    *    NWWS_HEADER%CDE3 - this will hold the three letter code for the station is 
    *                       sending out the National Weather Wire Service 
    *                       (NWWS) AFOS data.
    *    NWWS_HEADER%CDE4 - This holds the time when the nwws_header was form.
    *     NWWS_HEADER%CHR - This will hold all of the axci numbers behind the
    *                       the address data. (The NWWS_HEADER%CHR will hold
    *                       10 character spaces because each axci number only
    *                       takes up 1 space and there are 10 axci numbers.)
    *      NWR_HEADER%FTN - First two specific product designators of XXX in AFOS product
    *                       identifier (CCCNNNXXX) are the climate identifiers.
    *                       1 - CL = This stands for the climate.
    *     NWWS_HEADER%XXX - The three letter code for the station that is 
    *                       beinging written for the National Weather Wire 
    *                       Service (NWWS) AFOS data.
    *     NWWS_HEADER%THN - Third specific product designatior of NNN in afos product identifer
    *                       (CCCNNNXXX) is the identifer for which header product your going to
    *                       use.
    *                       1 - M = climate monthly
    *                       2 - I = climate daily
    *                       3 - S = climate season
    *                      
    *
    *      Non-system routines used
    *
    *   build_NWWS_header - adds the header line to the report
    *          error_warn - puts a warning message in the log file
    *
    *      Non-system functions used
    *
    *         NONE
    *
    *      Special system functions used
    *              getenv - HP intrinisic that returns an environmental variable
    *              strlen - C function; returns the number of characters in a string;
    *                       string must be terminated with the NULL character.
    *
    *  MODIFICATION LOG
    *  Name                    Date      Reason
    *  David T. Miller         May 2000  Some sites need the ability to modify
    *                                    the CCC portion of the header.
    *                                    Therefore, we're trusting the site
    *                                    to put the correct CCC in the setup.
    *                                    Will try to use node if CCC is originally
    *                                    blank.  Will warn if this occurs and 
    *                                    if node is blank.
     * </pre>
     * 
     * @param beginDate
     * @return
     */
    public String buildNWWSHeader(ClimateDate beginDate) {
        StringBuilder nwwsHeader = new StringBuilder();
        // First part of the header phrase
        // Example: "ALYCLIDCA000TTAA00 KALY 310802"
        nwwsHeader.append(currentSettings.getHeader().getNodeOrigSite()
                + currentSettings.getHeader().getProductCategory()
                + currentSettings.getHeader().getStationId()
                + currentSettings.getHeader().getAddress()
                + currentSettings.getHeader().getCDE1() + SPACE
                + currentSettings.getHeader().getCDE2()
                + currentSettings.getHeader().getCDE3() + SPACE);

        // Creation date and time (UTC) for the header
        updateNWWSHeader();
        nwwsHeader.append(String.format(TWO_DIGIT_INT,
                currentSettings.getHeader().getCDE4_Date().getDay())
                + String.format(TWO_DIGIT_INT,
                        currentSettings.getHeader().getCDE4_Time()
                                .to24HourTime().getHour())
                + String.format(TWO_DIGIT_INT,
                        currentSettings.getHeader().getCDE4_Time().getMin())
                + "\n");

        nwwsHeader.append("\n\nClimate Report" + "\nNATIONAL WEATHER SERVICE");

        String officeName = globalConfig.getOfficeName();
        if (officeName != null
                && !officeName.equalsIgnoreCase("NATIONAL WEATHER SERVICE")) {
            nwwsHeader.append(SPACE + officeName);
        }
        nwwsHeader.append("\n");

        /*
         * Legacy comment: Get the local time zone previously set in the
         * environment (by climate.sh or display.sh), and reset TZ to this value
         */
        String timeEnv = globalConfig.getTimezone();
        String timeZone = DEFAULT_IFPS_SITE_TIMEZONE;
        if (timeEnv != null && !timeEnv.isEmpty()) {
            timeZone = timeEnv;
        }

        // Get current datetime (local timezone).
        Calendar cal = TimeUtil.newCalendar();
        // header date format
        // Example:802 PM GMT FRI MAR 14 2017
        nwwsHeader
                .append(getHeaderDateFormat(cal, TimeZone.getTimeZone(timeZone))
                        + "\n\n");

        return nwwsHeader.toString();
    }

    /**
     * Migrated from build_NWWS_Comment.f
     * 
     * <pre>
    *   March  1998     Jason P. Tuell        PRC/TDL
    *   July   1998     Barry N. Baxter       PRC/TDL
    *   August 1998     Barry N. Baxter       PRC/TDL  UPDATE
    *   Sept.  1998     David O. Miller       PRC/TDL
    *   Oct.   1999     Dan Zipper            PRC/TDL  5.0 UPDATE
    *
    *   Purpose:  This subroutine is to create the header line for the station that
    *             the program is working on.  The header line will consists of
    *             the stations' name and the date.
    * 
    *
    *   Variables
    *
    *      Input
    *
    *              a_date - date structure containing valid date for the climate
    *                       summary.  See the structure definition for more details.
    *         all_phrases - buffer which holds the entire report output.
    *    climate_stations - derived TYPE containing the station id's and plain
    *                       language station names for this climate report. 
    *          valid_time - derived TYPE that contains the valid time of the
    *                       climate summary; only used for evening reports
    *
    *      Output
    *
    *         all_phrases - buffer which holds the entire report output
    *
    *      Local
    *
    *               c_day - This is to hold the character value of the day number .
    *             c_month - This is to hold the character value of the month number.
    *              c_year - This is to hold the character value of the year number.
    *            num_char - number of characters in buffer all_phrases.
    *            num_dayc - number of digits ("width") in day.
    *            num_monc - number of digits ("width") in month.
    *           num_years - number of digits ("width") in year.
    *
    *      Non-system routines used
    *
    *  convert_character_int - converts an input INTEGER to its CHARACTER equivalent.
    *                          User must supply the INTEGER and the # of digits in
    *                          the INTEGER.
    *     get_climate_period - returns the starting and ending years of the
    *                           normal period and record period
    *
    *      Non-system functions used
    *
    *         pick_digits - Returns the number of digits in a number.
    *              strlen - C function; returns the number of characters in a string;
    *                      string must be terminated with the NULL character.
    *
    *  MODIFICATION HISTORY
    *  --------------------
    *   10/27/00  Doug Murphy            Cleaned up routine greatly...
    *                                    Removed two unnecessary passed args
    *                                    and changed to pass only the pertinent
    *                                    station's info
    *    3/22/01  Doug Murphy            Removed call to conv_time
    *    5/16/01  Doug Murphy            Reversed month/day combo for daily
    *                                    reports
    *    8/23/01  Doug Murphy            Added extra comment which will appear
    *                                    on normal period line if dates have
    *                                    been updated to the 1971-2000 norms
    *                                    AND it is currently Jan-Jun 2002
     * </pre>
     * 
     * @param stationId
     * @param report
     * @return
     * @throws ClimateQueryException
     */
    public String buildNWWSComment(int stationId, ClimateCreatorResponse report)
            throws ClimateQueryException {
        StringBuilder nwwsComment = new StringBuilder();
        String stationName = stationMap.get(stationId).getStationName();
        PeriodType type = currentSettings.getReportType();

        ClimateTime validTime = ClimateTime.getDailyValidTime(type,
                globalConfig);

        /**
         * Example of the header comment:
         * 
         * <pre>
         * ...................................
         *
         * ...THE WASHINGTON NATIONAL DC CLIMATE SUMMARY FOR MARCH 5 2016...
         *
         * CLIMATE NORMAL PERIOD: 1981 TO 2010
         * CLIMATE RECORD PERIOD: 1871 TO 2016
         * </pre>
         */

        nwwsComment.append("\n..................................." + "\n\n"
                + "...The " + stationName + " climate summary for ");

        if (type == PeriodType.MONTHLY_NWWS) {
            nwwsComment.append("the month of "
                    + DateFormatSymbols.getInstance()
                            .getMonths()[report.getBeginDate().getMon() - 1]
                                    .toUpperCase()
                    + SPACE + report.getBeginDate().getYear());

        } else if (type == PeriodType.SEASONAL_NWWS) {
            nwwsComment.append(
                    "the season, from\n" + report.getBeginDate().getMon() + "/"
                            + report.getBeginDate().getDay() + "/"
                            + report.getBeginDate().getYear() + " to "
                            + ((ClimateCreatorPeriodResponse) report)
                                    .getEndDate().getMon()
                            + "/"
                            + ((ClimateCreatorPeriodResponse) report)
                                    .getEndDate().getDay()
                            + "/" + ((ClimateCreatorPeriodResponse) report)
                                    .getEndDate().getYear());
        } else if (type == PeriodType.ANNUAL_NWWS) {
            nwwsComment
                    .append("the year of " + report.getBeginDate().getYear());
        } else {
            nwwsComment.append(DateFormatSymbols.getInstance()
                    .getMonths()[report.getBeginDate().getMon() - 1]
                            .toUpperCase()
                    + SPACE + report.getBeginDate().getDay() + SPACE
                    + report.getBeginDate().getYear());
        }

        nwwsComment.append("...\n\n");

        if (type == PeriodType.EVEN_NWWS || type == PeriodType.INTER_NWWS) {
            if (type == PeriodType.INTER_NWWS) {
                nwwsComment.append("Valid as of ");
            } else {
                nwwsComment.append("Valid today as of ");
            }
            nwwsComment.append(String.format(TWO_DIGIT_INT, validTime.getHour())
                    + String.format(TWO_DIGIT_INT, validTime.getMin()) + SPACE
                    + validTime.getAmpm() + " local time.");
        }

        ClimatePeriodDAO climatePeriodDAO = new ClimatePeriodDAO();
        int[] climatePeriodYears = climatePeriodDAO
                .fetchClimatePeriod(stationId);
        nwwsComment.append(
                "Climate normal period" + COLON + SPACE + climatePeriodYears[0]
                        + " to " + climatePeriodYears[1] + "\n");

        if (report.getBeginDate().getYear() == 2002
                && report.getBeginDate().getMon() >= 1
                && report.getBeginDate().getMon() <= 6
                && climatePeriodYears[1] == 2000) {
            if (type == PeriodType.ANNUAL_NWWS) {
                nwwsComment.append(" Snow/Heating Degree Days 1961-1990");
            }
        }
        nwwsComment.append(
                "Climate record period" + COLON + SPACE + climatePeriodYears[2]
                        + " to " + climatePeriodYears[3] + "\n");

        return nwwsComment + "\n";
    }

    /**
     * Create a blank line
     * 
     * @param length
     *            the number of spaces
     * @return
     */
    protected static StringBuilder emptyLine(int length) {
        StringBuilder emptyLine = new StringBuilder();
        for (int i = 0; i < length; i++) {
            emptyLine.append(SPACE);
        }
        return emptyLine;
    }

    /**
     * Create a line of period characters.
     * 
     * @param length
     *            the number of period characters
     * @return
     */
    protected static StringBuilder separator(int length) {
        StringBuilder separator = new StringBuilder();
        for (int i = 0; i < length; i++) {
            separator.append(PERIOD);
        }
        return separator;
    }

    /**
     * Migrated from build_NWWS_header.f
     * 
     * <pre>
     *   August 1998     Jason P. Tuell        PRC/TDL
     *   August 1998     Barry N. Baxter       PRC/TDL
     *
     *   Purpose: This program is to setup the nwws_header file for the Climate
     *            NOAA Weather Radio. 
     * 
     *
     *   Variables
     *
     *      Input
     *
     *             A_DATE - This holds the current Zulu date.
     *             X_TIME - This holds the current Zulu time.
     *
     *      Output
     *
     *        NWWS_HEADER - This is were the creation_date, and creation_time
     *                      gets store for later use in the subroutine
     *                      NWWS_HEADER.F in format_climate directory.
     *
     *      Local
     *
     * NWWS_HEADER%CREATION_DATE - This will hold the current Zulu date and time.
     * NWWS_HEADER%CREATION_TIME - This will hold the current Zulu time.
     *
     *      Non-system routines used
     *
     *                NONE
     *
     *      Non-system functions used
     *
     *                NONE
     * </pre>
     * 
     * @param header
     * @return
     */
    private void updateNWWSHeader() {
        ClimateProductHeader header = currentSettings.getHeader();

        // Set header creation date-time to the current time (UTC)
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(GMT_STRING));
        ClimateDate date = header.getCDE4_Date();
        date.setDay(new ClimateDate(cal).getDay());
        header.setCDE4_Date(date);

        header.setCDE4_Time(new ClimateTime(cal));

        currentSettings.setHeader(header);

    }
}

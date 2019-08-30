/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateTime;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
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
 * 11 OCT 2017  39212      amoore      Better logging of TimeZone defaulting.
 * 11 OCT 2017  39238      amoore      Shortened and correct DST-dependent timezones in
 *                                     Formatted and RER headers.
 * 16 AUG 2019  DR21231    wpaintsil   Correct the format of the header.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public abstract class ClimateNWWSFormat extends ClimateFormat {
    /**
     * Formats for date and time in NWWS header; e.g. 237 PM EST THU JUL 16
     * 2004. Split in two so that logic for inserting appropriate short timezone
     * name (including DST logic) in between.
     */
    public static final String[] HEADER_DATE_FORMAT_STRING = new String[] {
            "hmm a", "E MMM dd yyyy" };

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
        SimpleDateFormat headerDateFormat1 = new SimpleDateFormat(
                HEADER_DATE_FORMAT_STRING[0]);
        headerDateFormat1.setTimeZone(timeZone);
        SimpleDateFormat headerDateFormat2 = new SimpleDateFormat(
                HEADER_DATE_FORMAT_STRING[1]);
        headerDateFormat2.setTimeZone(timeZone);

        Date time = date.getTime();

        StringBuilder headerDate = new StringBuilder();
        headerDate.append(headerDateFormat1.format(time));
        headerDate.append(" ");
        headerDate.append(timeZone.getDisplayName(timeZone.inDaylightTime(time),
                TimeZone.SHORT));
        headerDate.append(" ");
        headerDate.append(headerDateFormat2.format(time));

        return headerDate.toString();
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
     * </pre>
     * 
     * @param beginDate
     * @return
     */
    public String buildNWWSHeader(ClimateDate beginDate) {
        StringBuilder nwwsHeader = new StringBuilder();

        // Creation date and time (UTC) for the header
        updateNWWSHeader();

        // First part of the header phrase
        // Example:
        // 000
        // TTAA00 KHUN 110701
        // CLIHSV
        nwwsHeader.append(currentSettings.getHeader().getAddress()).append("\n")
                .append(currentSettings.getHeader().getCDE1()).append(SPACE)
                .append(currentSettings.getHeader().getCDE2())
                .append(currentSettings.getHeader().getNodeOrigSite())
                .append(SPACE)
                .append(String.format(TWO_DIGIT_INT,
                        currentSettings.getHeader().getCDE4_Date().getDay()))
                .append(String.format(TWO_DIGIT_INT,
                        currentSettings.getHeader().getCDE4_Time()
                                .to24HourTime().getHour()))
                .append(String.format(TWO_DIGIT_INT,
                        currentSettings.getHeader().getCDE4_Time().getMin()))
                .append("\n")
                .append(currentSettings.getHeader().getProductCategory())
                .append(currentSettings.getHeader().getStationId());

        nwwsHeader.append("\n\nClimate Report").append(SPACE)
                .append("\nNational Weather Service");

        String officeName = globalConfig.getOfficeName();
        if (officeName != null
                && !officeName.equalsIgnoreCase("National Weather Service")) {
            nwwsHeader.append(SPACE).append(officeName);
        }
        nwwsHeader.append("\n");

        // Get current datetime (local timezone).
        Calendar cal = TimeUtil.newCalendar();

        TimeZone tz = parseTimeZone();
        // header date format
        // Example:802 PM GMT FRI MAR 14 2017
        nwwsHeader.append(getHeaderDateFormat(cal, tz)).append("\n");

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
     * </pre>
     * 
     * @param stationId
     * @param report
     * @return
     * @throws ClimateQueryException
     */
    public String buildNWWSComment(int stationId, ClimateRunData report)
            throws ClimateQueryException {
        StringBuilder nwwsComment = new StringBuilder();
        String stationName = stationMap.get(stationId).getStationName();
        PeriodType type = currentSettings.getReportType();

        ClimateTime validTime = ClimateTime
                .getDailyValidTime(type, globalConfig).to12HourTime();

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

        nwwsComment.append("\n...................................")
                .append("\n\n").append("...The ").append(stationName)
                .append(" climate summary for ");

        if (type == PeriodType.MONTHLY_NWWS) {
            nwwsComment.append("the month of ")
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[report.getBeginDate().getMon() - 1]
                                    .toUpperCase())
                    .append(SPACE).append(report.getBeginDate().getYear());

        } else if (type == PeriodType.SEASONAL_NWWS) {
            nwwsComment.append("the season, from\n")
                    .append(report.getBeginDate().getMon()).append("/")
                    .append(report.getBeginDate().getDay()).append("/")
                    .append(report.getBeginDate().getYear()).append(" to ")
                    .append(((ClimateRunPeriodData) report).getEndDate()
                            .getMon())
                    .append("/")
                    .append(((ClimateRunPeriodData) report).getEndDate()
                            .getDay())
                    .append("/").append(((ClimateRunPeriodData) report)
                            .getEndDate().getYear());
        } else if (type == PeriodType.ANNUAL_NWWS) {
            nwwsComment.append("the year of ")
                    .append(report.getBeginDate().getYear());
        } else {
            nwwsComment
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[report.getBeginDate().getMon() - 1]
                                    .toUpperCase())
                    .append(SPACE).append(report.getBeginDate().getDay())
                    .append(SPACE).append(report.getBeginDate().getYear());
        }

        nwwsComment.append("...\n");

        if (type == PeriodType.EVEN_NWWS || type == PeriodType.INTER_NWWS) {
            if (type == PeriodType.INTER_NWWS) {
                nwwsComment.append("Valid as of ");
            } else {
                nwwsComment.append("Valid today as of ");
            }
            nwwsComment
                    .append(String.format(TWO_DIGIT_INT,
                            validTime.getHour() == 0 ? 12
                                    : validTime.getHour()))
                    .append(String.format(TWO_DIGIT_INT, validTime.getMin()))
                    .append(SPACE).append(validTime.getAmpm())
                    .append(" local time.\n\n");
        } else {
            nwwsComment.append("\n");
        }

        ClimatePeriodDAO climatePeriodDAO = new ClimatePeriodDAO();
        int[] climatePeriodYears = climatePeriodDAO
                .fetchClimatePeriod(stationId);
        nwwsComment.append("Climate normal period").append(COLON).append(SPACE)
                .append(climatePeriodYears[0])
                .append(" to " + climatePeriodYears[1]).append("\n");

        if (report.getBeginDate().getYear() == 2002
                && report.getBeginDate().getMon() >= 1
                && report.getBeginDate().getMon() <= 6
                && climatePeriodYears[1] == 2000) {
            if (type == PeriodType.ANNUAL_NWWS) {
                nwwsComment.append(" Snow/Heating Degree Days 1961-1990");
            }
        }
        nwwsComment.append("Climate record period").append(COLON).append(SPACE)
                .append(climatePeriodYears[2]).append(" to ")
                .append(climatePeriodYears[3]).append("\n");

        return nwwsComment.toString() + "\n";
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

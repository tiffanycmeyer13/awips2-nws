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
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunPeriodData;
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
 * 11 OCT 2017  39212      amoore      Better logging of TimeZone defaulting. Use globalDay
 *                                     timezone, not system FXA timezone.
 * 16 AUG 2018  DR20837    wpaintsil   Corrected updateNWRHeader() to shift the local 
 *                                     date/time (including date) to the UTC date/time. 
 *                                     It was only shifting the hour.
 * Sep 19, 2018 DR20888    wpaintsil   The time in the opening sentence should be 12 hour time.
 * 08 NOV 2018  DR20943    wpaintsil   Apply the current local date to the expiration/effective 
 *                                     times rather than the dates stored in the product settings.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public abstract class ClimateNWRFormat extends ClimateFormat {

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
     * </pre>
     * 
     * @return
     */
    protected String buildNWRHeader() {
        StringBuilder nwrHeader = new StringBuilder();

        // Create first part of the header phrase
        nwrHeader.append(ESCAPE_A)
                .append(currentSettings.getHeader().getMessageFormat())
                .append(currentSettings.getHeader().getNodeOrigSite())
                .append(currentSettings.getHeader().getProductCategory())
                .append(currentSettings.getHeader().getStationId());

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
        nwrHeader.append(currentSettings.getHeader().getActiveStorage())
                .append(currentSettings.getHeader().getDelSaveMsg())
                .append(currentSettings.getHeader().getMessageConfirm())
                .append("   ")
                .append(currentSettings.getHeader().getInterruptMsg())
                .append(currentSettings.getHeader().getAlertTone())
                .append(currentSettings.getHeader().getListenAreaCode());

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
     * </pre>
     * 
     * @param stationId
     * @param reportData
     * @return
     */
    protected String buildNWRComment(Integer stationId,
            ClimateRunData reportData) {
        StringBuilder nwrComment = new StringBuilder();

        String stationName = stationMap.get(stationId).getStationName();
        PeriodType type = currentSettings.getReportType();
        ClimateTime validTime = ClimateTime.getDailyValidTime(type,
                globalConfig);
        String monSea = "";
        boolean morning = currentSettings
                .getReportType() == PeriodType.MORN_RAD;

        // "The <station name> climate summary "
        nwrComment.append("The ").append(stationName)
                .append(" climate summary ");

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
            nwrComment.append("for the ").append(monSea).append(SPACE);
        } else if (type == PeriodType.EVEN_RAD
                || type == PeriodType.INTER_RAD) {
            ClimateTime tempTime = validTime;
            if (type == PeriodType.INTER_RAD) {
                nwrComment.append("as of ");
            } else {
                nwrComment.append("for this evening, as of ");
            }

            ClimateTime tempTime12Hr = tempTime.to12HourTime();
            nwrComment.append(tempTime12Hr.toHourMinString()).append(SPACE)
                    .append(tempTime12Hr.getAmpm()).append(",");
        } else {
            nwrComment.append("for ")
                    .append(ClimateNWRFormat.timeFrame(morning, false))
                    .append(",");
        }

        // Add the last part, the begin/end dates
        if (type == PeriodType.MONTHLY_RAD) {
            ClimateDate endDate = ((ClimateRunPeriodData) reportData)
                    .getEndDate();

            nwrComment.append(" of ")
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[endDate.getMon() - 1])
                    .append(", ").append(endDate.getYear()).append(PERIOD)
                    .append(SPACE).append(SPACE);
        } else if (type == PeriodType.SEASONAL_RAD) {
            ClimateDate beginDate = new ClimateDate(reportData.getBeginDate());
            ClimateDate endDate = ((ClimateRunPeriodData) reportData)
                    .getEndDate();

            nwrComment.append(", from ").append(DateFormatSymbols.getInstance()
                    .getMonths()[beginDate.getMon() - 1]);
            if (beginDate.getMon() > endDate.getMon()) {
                nwrComment.append(SPACE).append(beginDate.getYear());
            }

            nwrComment.append(" to ")
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[endDate.getMon() - 1])
                    .append(" ").append(endDate.getYear()).append(PERIOD)
                    .append(SPACE).append(SPACE);
        } else if (type == PeriodType.ANNUAL_RAD) {
            ClimateDate endDate = ((ClimateRunPeriodData) reportData)
                    .getEndDate();
            nwrComment.append(" of ").append(endDate.getYear()).append(PERIOD)
                    .append(SPACE).append(SPACE);
        } else {
            ClimateDate beginDate = new ClimateDate(reportData.getBeginDate());
            nwrComment.append(SPACE)
                    .append(DateFormatSymbols.getInstance()
                            .getMonths()[beginDate.getMon() - 1])
                    .append(SPACE).append(beginDate.getDay()).append(", ")
                    .append(beginDate.getYear()).append(PERIOD).append(SPACE)
                    .append(SPACE);
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
                    outputText.append(inputText.substring(k, j)).append("\n");

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

        TimeZone localTimeZone = parseTimeZone();

        // Convert effective date and time to UTC
        Calendar effectiveLocal = TimeUtil.newCalendar(localTimeZone);
        effectiveLocal.set(Calendar.HOUR, header.getEffectiveTime().getHour());
        effectiveLocal.set(Calendar.MINUTE, header.getEffectiveTime().getMin());

        Calendar effectiveGMT = TimeUtil.newGmtCalendar();
        effectiveGMT.setTimeInMillis(effectiveLocal.getTimeInMillis());
        header.setEffectiveDate(new ClimateDate(effectiveGMT));
        header.setEffectiveTime(new ClimateTime(effectiveGMT));

        // Convert expiration date and time to UTC
        Calendar expirationLocal = TimeUtil.newCalendar(localTimeZone);
        expirationLocal.set(Calendar.HOUR,
                header.getExpirationTime().getHour());
        expirationLocal.set(Calendar.MINUTE,
                header.getExpirationTime().getMin());

        Calendar expirationGMT = TimeUtil.newGmtCalendar();
        expirationGMT.setTimeInMillis(expirationLocal.getTimeInMillis());
        header.setExpirationDate(new ClimateDate(expirationGMT));
        header.setExpirationTime(new ClimateTime(expirationGMT));

        currentSettings.setHeader(header);
    }
}
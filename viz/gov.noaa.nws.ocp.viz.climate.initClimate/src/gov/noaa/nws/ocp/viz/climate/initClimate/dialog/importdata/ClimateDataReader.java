/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.importdata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDayNorm;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodClimo;
import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterBounds;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest;
import gov.noaa.nws.ocp.common.dataplugin.climate.request.ClimateRequest.RequestType;
import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateUtilities;

/**
 * Read daily or monthly climate data
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#  Engineer    Description
 * ------------ -------- ----------- --------------------------
 * 10/10/2016   20639    wkwock      Initial creation
 * 10/27/2016   20635    wkwock      Clean up
 * 20 DEC 2016  26404    amoore      Correcting yes-no, ok-cancel ordering in message boxes.
 * 22 DEC 2016  22395    amoore      Correct multi-selection of stations/months; java standards.
 * 25 JAN 2017  26511    wkwock      Implements multiple selection of 'Other Data Not Listed'.
 * 27 FEB 2017  27420    amoore      Clean up formatting. Log errors. Minor bug for max mean temp.
 *                                   Modernize logging/messages.
 * 13 APR 2017  33104    amoore      Address comments from review.
 * 03 MAY 2017  33104    amoore      Address FindBugs.
 * 15 MAY 2017  33104    amoore      Fix null pointer logic issues.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */

public class ClimateDataReader {

    /** logger */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateDataReader.class);

    /** continue to process? */
    private boolean continueProcess = true;

    /** shell */
    private Shell shell;

    public ClimateDataReader(Shell shell) {
        this.shell = shell;
    }

    /**
     * Read a climate daily file
     * 
     * @param fileData
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ClimateDayNorm> readClimateDay(FileData fileData) {
        int lineCount = 0;
        int stationId = -1 * ParameterFormatClimate.MISSING;

        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);
        List<Station> climateStations = null;
        try {
            climateStations = (List<Station>) ThriftClient.sendRequest(request);
            String stationName = fileData.getStationName();
            if (!stationName.equalsIgnoreCase(FileData.LOCATED_IN_DATA)) {
                for (Station station : climateStations) {
                    if (stationName
                            .equalsIgnoreCase(station.getStationName())) {
                        stationId = station.getInformId();
                        break;
                    }
                }
            }
        } catch (VizException e) {
            MessageDialog.openError(shell, "Unable to get stations",
                    "Failed to retrieve station names.");
            logger.error("Failed to retrieve stations.", e);
            return null;
        }

        List<ClimateDayNorm> dayRecords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(
                fileData.getPathName() + "/" + fileData.getFileName()));) {
            String strLine;
            continueProcess = true;

            while ((strLine = br.readLine()) != null && continueProcess) {
                lineCount++;
                ClimateDayNorm dayRecord = null;
                try {
                    dayRecord = parseDayLine(strLine, fileData, lineCount,
                            stationId, climateStations);
                } catch (ClimateInvalidParameterException e) {
                    logger.error("Error parsing invalid parameter.", e);
                }
                if (dayRecord != null) {
                    if (dayRecord.getStationId() == -1
                            * ParameterFormatClimate.MISSING) {
                        MessageDialog.openError(shell, "No Station",
                                "No station found for line " + lineCount);
                    } else {
                        dayRecords.add(dayRecord);
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            MessageDialog.openError(shell, "Can't Open File",
                    "Failed to open file " + fileData.getFileName());
            logger.error("Failed to open file " + fileData.getFileName() + ".",
                    fnfe);
            return null;
        } catch (IOException e) {
            MessageDialog.openError(shell, "Can't Read File",
                    "Failed to read file " + fileData.getFileName());
            logger.error("Failed to open file " + fileData.getFileName() + ".",
                    e);
            return null;
        }

        return dayRecords;
    }

    /**
     * Parse a daily climate data string
     * 
     * @param lineStr
     * @param fileData
     * @param lineCount
     * @param stationId
     * @param climateStations
     * @return
     * @throws ClimateInvalidParameterException
     */
    private ClimateDayNorm parseDayLine(String lineStr, FileData fileData,
            int lineCount, int stationId, List<Station> climateStations)
            throws ClimateInvalidParameterException {

        ClimateDayNorm dayRecord = new ClimateDayNorm();
        dayRecord.setDataToMissing();
        if (stationId != -1 * ParameterFormatClimate.MISSING) {
            dayRecord.setStationId(stationId);
        }

        String delimiter = fileData.getDelimiter();
        String[] items = lineStr.split(delimiter);

        if (items.length < fileData.getChoices().length) {
            continueProcess = MessageDialog.openQuestion(shell, "Too Few Items",
                    "Too few items at line " + lineCount + ". Skipped line "
                            + lineCount + " and continue to next line?");
            return null;
        }

        int status = 0;

        for (int i = 0; i < fileData.getChoices().length && status == 0; i++) {
            String item = items[i].trim();
            // parse it as integer number
            Short shortNum = null;
            try {
                shortNum = Short.parseShort(item);
            } catch (NumberFormatException nfe) {
                shortNum = null;
                logger.error("Error parsing short.", nfe);
            }

            // parse it as float number
            Float floatNum = null;
            try {
                floatNum = Float.parseFloat(item);
            } catch (NumberFormatException nfe) {
                floatNum = null;
                logger.error("Error parsing float.", nfe);
            }

            // get field name
            int choice = fileData.getChoices()[i];
            String fieldName;
            if (choice >= 1 && choice <= DataArrangeDialog.getDaily().length) {
                fieldName = DataArrangeDialog.getDaily()[choice - 1];
            } else if (choice == DataArrangeDialog.STATION_NAME_CHOICE_NUM) {
                fieldName = "Station Name";
            } else if (choice == DataArrangeDialog.DATE_CHOICE_NUM) {
                fieldName = "Date";
            } else {
                throw new ClimateInvalidParameterException(
                        "Invalid data type choice: [" + choice
                                + "] for Import Daily Data.");
            }

            switch (choice) {
            case 0: // Other Data Not Listed
                continue;
            case 1: // Mean Maximum Temperature
                if (shortNum != null
                        && (shortNum > ParameterBounds.TEMP_LOWER_BOUND
                                && shortNum < ParameterBounds.TEMP_UPPER_BOUND
                                && (dayRecord
                                        .getMaxTempRecord() == ParameterFormatClimate.MISSING
                                        || shortNum < dayRecord
                                                .getMaxTempRecord())
                                && (dayRecord
                                        .getMinTempMean() == ParameterFormatClimate.MISSING
                                        || shortNum > dayRecord
                                                .getMinTempMean())
                                && (dayRecord
                                        .getMinTempRecord() == ParameterFormatClimate.MISSING
                                        || shortNum > dayRecord
                                                .getMinTempRecord())
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    dayRecord.setMaxTempMean(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 2:// Record Maximum Temperature
                if (shortNum != null
                        && ((shortNum > ParameterBounds.TEMP_LOWER_BOUND
                                && shortNum < ParameterBounds.TEMP_UPPER_BOUND
                                && (dayRecord
                                        .getMaxTempMean() == ParameterFormatClimate.MISSING
                                        || shortNum > dayRecord
                                                .getMaxTempMean())
                                && (dayRecord
                                        .getMinTempMean() == ParameterFormatClimate.MISSING
                                        || shortNum > dayRecord
                                                .getMinTempMean())
                                && (dayRecord
                                        .getMinTempRecord() == ParameterFormatClimate.MISSING
                                        || shortNum > dayRecord
                                                .getMinTempRecord())
                                || shortNum == ParameterFormatClimate.MISSING))) {
                    dayRecord.setMaxTempRecord(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 3: // Year(s) Rec Max Temp
                if (shortNum != null
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (dayRecord
                            .getMaxTempYear()[0] == ParameterFormatClimate.MISSING) {
                        dayRecord.getMaxTempYear()[0] = shortNum;
                    } else if (dayRecord
                            .getMaxTempYear()[1] == ParameterFormatClimate.MISSING)
                        dayRecord.getMaxTempYear()[1] = shortNum;
                    else
                        dayRecord.getMaxTempYear()[2] = shortNum;
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 4: // Mean Minimum Temp
                if (shortNum != null
                        && ((shortNum > ParameterBounds.TEMP_LOWER_BOUND
                                && shortNum < ParameterBounds.TEMP_UPPER_BOUND
                                && (dayRecord
                                        .getMaxTempMean() == ParameterFormatClimate.MISSING
                                        || shortNum < dayRecord
                                                .getMaxTempMean())
                                && (dayRecord
                                        .getMaxTempRecord() == ParameterFormatClimate.MISSING
                                        || shortNum < dayRecord
                                                .getMaxTempRecord())
                                && (dayRecord
                                        .getMinTempRecord() == ParameterFormatClimate.MISSING
                                        || shortNum > dayRecord
                                                .getMinTempRecord()))
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    dayRecord.setMinTempMean(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 5:// Record Minimum Temperature
                if (shortNum != null
                        && ((shortNum > ParameterBounds.TEMP_LOWER_BOUND
                                && shortNum < ParameterBounds.TEMP_UPPER_BOUND
                                && (dayRecord
                                        .getMaxTempMean() == ParameterFormatClimate.MISSING
                                        || shortNum < dayRecord
                                                .getMaxTempMean())
                                && (dayRecord
                                        .getMaxTempRecord() == ParameterFormatClimate.MISSING
                                        || shortNum < dayRecord
                                                .getMaxTempRecord())
                                && (dayRecord
                                        .getMinTempMean() == ParameterFormatClimate.MISSING
                                        || shortNum < dayRecord
                                                .getMinTempMean()))
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    dayRecord.setMinTempRecord(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 6:// Year(s) Rec Min Temp
                if (shortNum != null
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (dayRecord
                            .getMinTempYear()[0] == ParameterFormatClimate.MISSING)
                        dayRecord.getMinTempYear()[0] = shortNum;
                    else if (dayRecord
                            .getMinTempYear()[1] == ParameterFormatClimate.MISSING)
                        dayRecord.getMinTempYear()[1] = shortNum;
                    else
                        dayRecord.getMinTempYear()[2] = shortNum;
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 7: // Heating Deg Days
                if (shortNum != null
                        && ((shortNum >= ParameterBounds.DEGREE_DAY_LOWER
                                && shortNum < 100)
                                || shortNum == ParameterFormatClimate.MISSING_DEGREE_DAY))
                    dayRecord.setNumHeatMean(shortNum);
                else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 8:// Cooling Deg Days
                if (shortNum != null
                        && ((shortNum >= ParameterBounds.DEGREE_DAY_LOWER
                                && shortNum < 50)
                                || shortNum == ParameterFormatClimate.MISSING_DEGREE_DAY))
                    dayRecord.setNumCoolMean(shortNum);
                else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 9:// Average Precip
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    dayRecord.setPrecipMean(ParameterFormatClimate.TRACE);
                } else if (floatNum != null
                        && ((floatNum >= ParameterBounds.PRECIP_LOWER_BOUND
                                && floatNum < ParameterBounds.PRECIP_UPPER_BOUND
                                && (dayRecord
                                        .getPrecipDayRecord() == ParameterFormatClimate.MISSING
                                        || floatNum <= dayRecord
                                                .getPrecipDayRecord()))
                                || floatNum == ParameterFormatClimate.MISSING_PRECIP)) {
                    dayRecord.setPrecipMean(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 10:// Record Max Precip
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    dayRecord.setPrecipDayRecord(ParameterFormatClimate.TRACE);
                } else if (floatNum != null
                        && ((floatNum >= ParameterBounds.PRECIP_LOWER_BOUND
                                && floatNum < ParameterBounds.PRECIP_UPPER_BOUND
                                && (dayRecord
                                        .getPrecipMean() == ParameterFormatClimate.MISSING
                                        || floatNum >= dayRecord
                                                .getPrecipMean()))
                                || floatNum == ParameterFormatClimate.MISSING_PRECIP)) {
                    dayRecord.setPrecipDayRecord(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 11: // Year(s) Rec Max Precip
                if (shortNum != null
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (dayRecord
                            .getPrecipDayRecordYear()[0] == ParameterFormatClimate.MISSING)
                        dayRecord.getPrecipDayRecordYear()[0] = shortNum;
                    else if (dayRecord
                            .getPrecipDayRecordYear()[1] == ParameterFormatClimate.MISSING)
                        dayRecord.getPrecipDayRecordYear()[1] = shortNum;
                    else
                        dayRecord.getPrecipDayRecordYear()[2] = shortNum;
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 12:// Average Daily Snow
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    dayRecord.setSnowDayMean(ParameterFormatClimate.TRACE);
                } else if (floatNum != null
                        && ((floatNum >= ParameterBounds.SNOW_LOWER_BOUND
                                && floatNum < ParameterBounds.SNOW_UPPER_BOUND
                                && (dayRecord
                                        .getSnowDayRecord() == ParameterFormatClimate.MISSING
                                        || floatNum <= dayRecord
                                                .getSnowDayRecord()))
                                || floatNum == ParameterFormatClimate.MISSING_SNOW)) {
                    dayRecord.setSnowDayMean(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 13:// Record Max Snowfall
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    dayRecord.setSnowDayRecord(ParameterFormatClimate.TRACE);
                } else if (floatNum != null
                        && ((floatNum >= ParameterBounds.SNOW_LOWER_BOUND
                                && floatNum < ParameterBounds.SNOW_UPPER_BOUND
                                && (dayRecord
                                        .getSnowDayMean() == ParameterFormatClimate.MISSING_SNOW
                                        || floatNum >= dayRecord
                                                .getSnowDayMean()))
                                || floatNum == ParameterFormatClimate.MISSING_SNOW)) {
                    dayRecord.setSnowDayRecord(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 14:// Year(s) Rec Max Snow
                if (shortNum != null
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (dayRecord
                            .getSnowDayRecordYear()[0] == ParameterFormatClimate.MISSING)
                        dayRecord.getSnowDayRecordYear()[0] = shortNum;
                    else if (dayRecord
                            .getSnowDayRecordYear()[1] == ParameterFormatClimate.MISSING)
                        dayRecord.getSnowDayRecordYear()[1] = shortNum;
                    else
                        dayRecord.getSnowDayRecordYear()[2] = shortNum;
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 15:// Daily Snow on Ground
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL))
                    dayRecord.setSnowGround(ParameterFormatClimate.TRACE);
                else if (floatNum != null
                        && ((floatNum >= ParameterBounds.SNOW_LOWER_BOUND
                                && floatNum < ParameterBounds.SNOW_UPPER_BOUND)
                                || floatNum == ParameterFormatClimate.MISSING_SNOW)) {
                    dayRecord.setSnowGround(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 16:// Normal Mean Temperature
                if (floatNum != null
                        && ((floatNum > ParameterBounds.TEMP_LOWER_BOUND
                                && floatNum < ParameterBounds.TEMP_UPPER_BOUND)
                                || floatNum == ParameterFormatClimate.MISSING)) {
                    dayRecord.setMeanTemp(Math.abs(floatNum));
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case DataArrangeDialog.STATION_NAME_CHOICE_NUM: // Station Name
                for (int k = 0; k < climateStations.size(); k++) {
                    if (climateStations.get(k).getIcaoId().equals(item)
                            || climateStations.get(k).getStationName()
                                    .equals(item)) {
                        dayRecord.setStationId(
                                climateStations.get(k).getInformId());
                        break;
                    }
                }
                if (dayRecord.getStationId() == (-1
                        * ParameterFormatClimate.MISSING)) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case DataArrangeDialog.DATE_CHOICE_NUM:// Date
                String dayOfYear = parseDayOfYear(item, fileData);
                if (dayOfYear != null) {
                    dayRecord.setDayOfYear(dayOfYear);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            default:
                logger.error("Unhandled choice number: [" + choice + "]");
                break;
            }

        }

        if (status != 0) {
            String message = "An error was encountered on "
                    + fileData.getFileName() + "#" + lineCount + ": [" + lineStr
                    + "]";

            // set record to null, as some issue occurred on this line
            dayRecord = null;
            if (status == 1) {
                logger.info(message
                        + ", and user proceeded with parsing the rest of the file.");
                this.continueProcess = true;
            } else {
                logger.warn(message
                        + ", and user cancelled parsing the rest of the file.");
                this.continueProcess = false;
            }
        }

        return dayRecord;
    }

    /**
     * parse string for day of year
     * 
     * @param dateStr
     * @param fileData
     * @return
     */
    private String parseDayOfYear(String dateStr, FileData fileData) {
        String dayOfYear = null;
        if (fileData.getDateFormat().equals(FileData.MONTH_FIRST_STRING)) {
            // expect mm-dd, 12-30
            String[] items = dateStr.split("-");
            if (items.length != 2) {
                logger.warn(dateStr + " is not in valid mm-dd format.");
                return null;
            }

            try {
                int month = Integer.parseInt(items[0]);
                int day = Integer.parseInt(items[1]);
                if (month < 1 || month > 12) {
                    logger.warn(
                            dateStr + " has invalid month: [" + month + "].");
                    return null;
                }
                // 2016, to allow leap year
                int maxDay = ClimateUtilities.daysInMonth(2016, month);
                if (day < 1 || day > maxDay) {
                    logger.warn(dateStr + " has invalid day: [" + day + "].");
                    return null;
                }
            } catch (NumberFormatException nfe) {
                logger.error(
                        "Error parsing date information: [" + dateStr + "]",
                        nfe);
                return null;
            }
            dayOfYear = dateStr;
        } else if (fileData.getDateFormat().equals(FileData.DAY_FIRST_STRING)) {
            // expect dd-mm, 30-12
            String[] items = dateStr.split("-");
            if (items.length != 2) {
                logger.warn(dateStr + " is not in valid dd-mm format.");
                return null;
            }

            try {
                int day = Integer.parseInt(items[0]);
                int month = Integer.parseInt(items[1]);
                if (month < 1 || month > 12) {
                    logger.warn(
                            dateStr + " has invalid month: [" + month + "].");
                    return null;
                }
                int maxDay = ClimateUtilities.daysInMonth(2016, month);
                if (day < 1 || day > maxDay) {
                    logger.warn(dateStr + " has invalid day: [" + day + "].");
                    return null;
                }
                dayOfYear = String.format("%02d-%02d", month, day);
            } catch (NumberFormatException nfe) {
                logger.error(
                        "Error parsing date information: [" + dateStr + "]",
                        nfe);
                return null;
            }
        } else if (fileData.getDateFormat().equals(FileData.JULIAN_DAYS_STRING)
                || fileData.getDateFormat()
                        .equals(FileData.JULIAN_DAYS_FEB29_60_STRING)) {
            // expect [1-365]
            int maxDayInYear = 366;
            if (fileData.getDateFormat().equals(FileData.JULIAN_DAYS_STRING)) {
                maxDayInYear = 365;
            }
            try {
                int jDay = Integer.parseInt(dateStr);
                if (jDay < 1 || jDay > maxDayInYear) {
                    logger.warn(
                            "Julian day: [" + dateStr + "] is out of range.");
                    return null;
                }
                GregorianCalendar gc = new GregorianCalendar();
                gc.set(GregorianCalendar.DAY_OF_YEAR, jDay);
                if (maxDayInYear == 365) {
                    gc.set(GregorianCalendar.YEAR, 2015);
                } else {
                    gc.set(GregorianCalendar.YEAR, 2016);
                }
                int month = gc.get(GregorianCalendar.MONTH);
                int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
                dayOfYear = String.format("%02d-%02d", month, day);
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse Julian day: [" + dateStr + "]",
                        nfe);
                return null;
            }
        } else {// this must one of 12 months
            // expect [1-31]
            Date date;
            try {
                date = new SimpleDateFormat("MMMM")
                        .parse(fileData.getDateFormat());
            } catch (ParseException e) {
                logger.error("Failed to parse month: ["
                        + fileData.getDateFormat() + "]", e);
                return null;
            }

            int day;
            try {
                day = Integer.parseInt(dateStr);
            } catch (NumberFormatException nfe) {
                logger.error("Failed to parse day from: [" + dateStr + "]",
                        nfe);
                return null;
            }

            Calendar cal = TimeUtil.newCalendar();
            cal.setTime(date);
            cal.set(Calendar.YEAR, 2016);// leap year
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            if (day < 1 || day > daysInMonth) {
                logger.error("Invalid day: [" + day + "]");
                return null;
            }

            dayOfYear = String.format("%02d-%02d", cal.get(Calendar.MONTH) + 1,
                    day);
        }
        return dayOfYear;
    }

    /**
     * Read a monthly climate file
     * 
     * @param fileData
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<PeriodClimo> readClimateMonth(FileData fileData) {
        int lineCount = 0;
        int stationId = -1 * ParameterFormatClimate.MISSING;

        ClimateRequest request = new ClimateRequest();
        request.setRequestType(RequestType.GET_STATIONS);
        List<Station> climateStations = null;
        try {
            climateStations = (List<Station>) ThriftClient.sendRequest(request);
            String stationName = fileData.getStationName();
            if (!stationName.equalsIgnoreCase(FileData.LOCATED_IN_DATA)) {
                for (Station station : climateStations) {
                    if (stationName
                            .equalsIgnoreCase(station.getStationName())) {
                        stationId = station.getInformId();
                        break;
                    }
                }
            }
        } catch (VizException e) {
            logger.error("Failed to retrieve stations from DB.", e);
            return null;
        }

        List<PeriodClimo> monthRecords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(
                fileData.getPathName() + "/" + fileData.getFileName()));) {
            String strLine;
            continueProcess = true;

            while ((strLine = br.readLine()) != null && continueProcess) {
                lineCount++;
                PeriodClimo monthRecord = null;
                try {
                    monthRecord = parseMonthLine(strLine, fileData, lineCount,
                            stationId, climateStations);
                } catch (ClimateInvalidParameterException e) {
                    logger.error("Error parsing invalid parameter.", e);
                }
                if (monthRecord != null) {
                    if (monthRecord.getInformId() == -1
                            * ParameterFormatClimate.MISSING) {
                        MessageDialog.openError(shell, "No Station",
                                "No station found for line " + lineCount);
                    } else {
                        monthRecords.add(monthRecord);
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            MessageDialog.openError(shell, "Error",
                    "Failed to open file " + fileData.getFileName());
            logger.error("Failed to open file " + fileData.getFileName() + ".",
                    fnfe);
            return null;
        } catch (IOException e) {
            MessageDialog.openError(shell, "Error",
                    "Failed to read file " + fileData.getFileName());
            logger.error("Failed to read file " + fileData.getFileName() + ".",
                    e);
            return null;
        }

        return monthRecords;
    }

    /**
     * parse a monthly climate data string
     * 
     * @param strLine
     * @param fileData
     * @param lineCount
     * @param stationId
     * @param climateStations
     * @return
     * @throws ClimateInvalidParameterException
     */
    private PeriodClimo parseMonthLine(String strLine, FileData fileData,
            int lineCount, int stationId, List<Station> climateStations)
            throws ClimateInvalidParameterException {
        String[] items = strLine.split(fileData.getDelimiter());

        if (items.length < fileData.getChoices().length) {

            continueProcess = MessageDialog.openQuestion(shell, "Error",
                    "Too few items at line " + lineCount
                            + ". Continue to next line?");

            return null;
        }

        PeriodClimo monthRecord = PeriodClimo.getMissingPeriodClimo();
        monthRecord.setPeriodType(PeriodType.MONTHLY_RAD);

        int status = 0;

        for (int i = 0; i < fileData.getChoices().length && status == 0; i++) {
            String item = items[i].trim();
            // parse it as whole number
            Short shortNum = null;
            try {
                shortNum = Short.parseShort(item);
            } catch (NumberFormatException nfe) {
                shortNum = null;
                logger.error("Error parsing short.", nfe);
            }

            // parse it as float number
            Float floatNum = null;
            try {
                floatNum = Float.parseFloat(item);
            } catch (NumberFormatException nfe) {
                floatNum = null;
                logger.error("Error parsing float.", nfe);
            }

            // get field name
            int choice = fileData.getChoices()[i];
            String fieldName;
            if (choice >= 1
                    && choice <= DataArrangeDialog.getMonthly().length) {
                fieldName = DataArrangeDialog.getMonthly()[choice - 1];
            } else if (choice == DataArrangeDialog.STATION_NAME_CHOICE_NUM) {
                fieldName = "Station Name";
            } else if (choice == DataArrangeDialog.DATE_CHOICE_NUM) {
                fieldName = "Date";
            } else {
                throw new ClimateInvalidParameterException(
                        "Invalid data type choice: [" + choice
                                + "] for Import Monthly Data.");
            }

            if (stationId != -1) {
                monthRecord.setInformId(stationId);
            }

            switch (choice) {
            case 1:// Normal Maximum Temperature
                if (floatNum != null) {
                    monthRecord.setMaxTempNorm(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 2: // No. Days Max T GE 90
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32
                        && monthRecord
                                .getNormNumMaxGE90F() == ParameterFormatClimate.MISSING)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNormNumMaxGE90F(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 3:// No. Days Max T LE 32
                if (floatNum != null && ((floatNum >= 0 && floatNum < 32
                        && monthRecord
                                .getNormNumMaxLE32F() == ParameterFormatClimate.MISSING)
                        || floatNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNormNumMaxLE32F(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 4:// Normal Minimum Temperature
                if (floatNum != null) {
                    monthRecord.setMinTempNorm(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 5:// No. Days Min T LE 32
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNormNumMinLE32F(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 6: // No. Days Min T LE 0
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNormNumMinLE0F(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 7: // Record Maximum Precip
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    monthRecord.setPrecipPeriodMax(-1);
                } else if (floatNum != null
                        && ((floatNum >= ParameterBounds.PRECIP_LOWER_BOUND
                                && floatNum < ParameterBounds.PRECIP_UPPER_BOUND
                                && (floatNum > monthRecord.getPrecipPeriodMin()
                                        || monthRecord
                                                .getPrecipPeriodMin() == ParameterFormatClimate.MISSING_PRECIP))
                                || floatNum == ParameterFormatClimate.MISSING_PRECIP)) {
                    monthRecord.setPrecipPeriodMax(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 8: // Year(s) Rec Max Precip
                if (shortNum != null
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (monthRecord.getPrecipPeriodMaxYearList().get(0)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        monthRecord.getPrecipPeriodMaxYearList().get(0)
                                .setYear(shortNum);
                    } else if (monthRecord.getPrecipPeriodMaxYearList().get(1)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        monthRecord.getPrecipPeriodMaxYearList().get(1)
                                .setYear(shortNum);
                    } else {
                        monthRecord.getPrecipPeriodMaxYearList().get(2)
                                .setYear(shortNum);
                    }
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 9: // Record Minimum Precip
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    monthRecord
                            .setPrecipPeriodMin(ParameterFormatClimate.TRACE);
                } else if ((floatNum >= ParameterBounds.PRECIP_LOWER_BOUND
                        && floatNum < ParameterBounds.PRECIP_UPPER_BOUND
                        && floatNum < monthRecord.getPrecipPeriodMax())
                        || floatNum == ParameterFormatClimate.MISSING_PRECIP) {
                    monthRecord.setPrecipPeriodMin(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 10: // Year(s) Rec Min Precip
                if (shortNum != null
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (monthRecord.getPrecipPeriodMinYearList().get(0)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        monthRecord.getPrecipPeriodMinYearList().get(0)
                                .setYear(shortNum);
                    } else if (monthRecord.getPrecipPeriodMinYearList().get(1)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        monthRecord.getPrecipPeriodMinYearList().get(1)
                                .setYear(shortNum);
                    } else {
                        monthRecord.getPrecipPeriodMinYearList().get(2)
                                .setYear(shortNum);
                    }
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 11: // No. Days Precip > 0.01"
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNumPrcpGE01Norm(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 12: // No. Days Precip > 0.10"
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNumPrcpGE10Norm(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 13: // No. Days Precip > 0.50"
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNumPrcpGE50Norm(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 14: // No. Days Precip > 1.00"
                if (shortNum != null && ((shortNum >= 0 && shortNum < 32)
                        || shortNum == ParameterFormatClimate.MISSING)) {
                    monthRecord.setNumPrcpGE100Norm(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
            case 15: // Record Maximum Snowfall
                if (item.equals(ParameterFormatClimate.TRACE_SYMBOL)) {
                    monthRecord
                            .setSnowPeriodRecord(ParameterFormatClimate.TRACE);
                } else if (floatNum != null
                        && ((floatNum >= ParameterBounds.SNOW_LOWER_BOUND
                                && floatNum < ParameterBounds.SNOW_UPPER_BOUND)
                                || floatNum == ParameterFormatClimate.MISSING_PRECIP)) {
                    monthRecord.setSnowPeriodRecord(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 16: // Year(s) Rec Max Snowfall
                if ((shortNum != null)
                        && ((shortNum > ParameterBounds.YEAR_LOWER_BOUND
                                && shortNum < ParameterBounds.YEAR_UPPER_BOUND)
                                || shortNum == ParameterFormatClimate.MISSING)) {
                    if (monthRecord.getDaySnowGroundMaxList().get(0)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        monthRecord.getDaySnowGroundMaxList().get(0)
                                .setYear(shortNum);
                    } else if (monthRecord.getDaySnowGroundMaxList().get(1)
                            .getYear() == ParameterFormatClimate.MISSING) {
                        monthRecord.getDaySnowGroundMaxList().get(1)
                                .setYear(shortNum);
                    } else {
                        monthRecord.getDaySnowGroundMaxList().get(2)
                                .setYear(shortNum);
                    }
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 17: // Record Maximum 24Hr Snowfall Total
                if (floatNum != null
                        && ((floatNum >= ParameterBounds.SNOW_LOWER_BOUND
                                && floatNum < ParameterBounds.SNOW_UPPER_BOUND)
                                || floatNum == ParameterFormatClimate.MISSING_SNOW)) {
                    monthRecord.setSnowMax24HRecord(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 18:// Start Date(s) Record Maximum 24Hr
                    // Snowfall
                try {
                    ClimateDate startDate = ClimateDate
                            .parseFullDateFromString(item);
                    monthRecord.getSnow24HList().get(0).setStart(startDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 19:// End Date(s) Record Maximum 24Hr
                    // Snowfall
                try {
                    ClimateDate endDate = ClimateDate
                            .parseFullDateFromString(item);
                    monthRecord.getSnow24HList().get(0).setEnd(endDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 20:// Normal Water Equivalent of Snow
                if (floatNum != null) {
                    monthRecord.setSnowWaterPeriodNorm(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 21:// Record Snow Depth
                if (floatNum != null) {
                    monthRecord.setSnowPeriodNorm(floatNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 22:// Date(s) of Record Snow Depth
                try {
                    ClimateDate snowDate = ClimateDate
                            .parseFullDateFromString(item);
                    monthRecord.getSnowPeriodMaxYearList().set(0, snowDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 23:// Norm No. Days with Any Snowfall
                if (shortNum != null) {
                    monthRecord.setNumSnowGETRNorm(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 24:// Norm No. Days with Snowfall GE
                    // 1.0 in
                if (shortNum != null) {
                    monthRecord.setNumSnowGE1Norm(shortNum);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 25:// Normal First Freeze Date
                try {
                    ClimateDate freezeDate = ClimateDate
                            .parseMonthDayDateFromString(item);
                    monthRecord.setEarlyFreezeNorm(freezeDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 26:// Normal Last Freeze Date
                try {
                    ClimateDate freezeDate = ClimateDate
                            .parseMonthDayDateFromString(item);
                    monthRecord.setLateFreezeNorm(freezeDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 27:// Record First Freeze Date
                try {
                    ClimateDate freezeDate = ClimateDate
                            .parseFullDateFromString(item);
                    monthRecord.setEarlyFreezeRec(freezeDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case 28:// Record Last Freeze Date
                try {
                    ClimateDate freezeDate = ClimateDate
                            .parseFullDateFromString(item);
                    monthRecord.setLateFreezeRec(freezeDate);
                } catch (ParseException pe) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case DataArrangeDialog.STATION_NAME_CHOICE_NUM:// Station Name
                boolean notFound = true;
                for (Station station : climateStations) {
                    if (station.getIcaoId().equals(item)) {
                        monthRecord.setInformId(station.getInformId());
                        notFound = false;
                        break;
                    }
                }
                if (notFound) {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            case DataArrangeDialog.DATE_CHOICE_NUM: // Date
                int month = parseMonthOfYear(item, fileData);
                if (month != ParameterFormatClimate.MISSING) {
                    monthRecord.setMonthOfYear(month);
                } else {
                    status = warning(fieldName, item, lineCount,
                            fileData.getFileName());
                }
                break;
            default:
                logger.error("Unhandled choice number: [" + choice + "]");
                break;
            }
        }

        if (status == -1) {
            this.continueProcess = false;
        }

        if (status != 0) {
            return null;
        }

        return monthRecord;
    }

    /**
     * Parse month of year string
     * 
     * @param dateStr
     * @param fileData
     * @return
     */
    private int parseMonthOfYear(String dateStr, FileData fileData) {
        int monthOfYear = ParameterFormatClimate.MISSING;

        if (fileData.getDateFormat().equals(FileData.MONTH_FIRST_STRING)
                || fileData.getDateFormat().equals(FileData.DAY_FIRST_STRING)) {
            // expect mm-dd, 12-30 or dd-mm, 30-12
            String[] items = dateStr.split("-");
            if (items.length == 2) {
                try {
                    int month;
                    if (fileData.getDateFormat()
                            .equals(FileData.MONTH_FIRST_STRING)) {
                        month = Integer.parseInt(items[0]);
                    } else {
                        month = Integer.parseInt(items[1]);
                    }

                    if (month > 0 && month < 13) {
                        monthOfYear = month;
                    }
                } catch (NumberFormatException nfe) {
                    logger.error("Invalid date: [" + dateStr + "]", nfe);
                }
            }
        } else if (fileData.getDateFormat().equals(FileData.JULIAN_DAYS_STRING)
                || fileData.getDateFormat()
                        .equals(FileData.JULIAN_DAYS_FEB29_60_STRING)) {
            // expect [1-365]
            int maxDayInYear = 366;
            if (fileData.getDateFormat().equals(FileData.JULIAN_DAYS_STRING)) {
                maxDayInYear = 365;
            }
            try {
                int jDay = Integer.parseInt(dateStr);
                if (jDay > 0 && jDay <= maxDayInYear) {

                    GregorianCalendar gc = new GregorianCalendar();
                    gc.set(GregorianCalendar.DAY_OF_YEAR, jDay);
                    if (maxDayInYear == 365) {
                        gc.set(GregorianCalendar.YEAR, 2015);
                    } else {
                        gc.set(GregorianCalendar.YEAR, 2016);
                    }
                    monthOfYear = gc.get(GregorianCalendar.MONTH);
                }
            } catch (NumberFormatException nfe) {
                logger.error("Invalid Julian date: [" + dateStr + "]", nfe);
            }
        } else {
            // expect [1-12]
            try {
                int month = Integer.parseInt(dateStr);
                if (month > 0 && month < 13) {
                    monthOfYear = month;
                }
            } catch (NumberFormatException nfe) {
                logger.error("Invalid month: [" + dateStr + "]", nfe);
            }
        }
        return monthOfYear;
    }

    /**
     * Display a warning message
     * 
     * @param field
     * @param value
     * @param lineNum
     * @param fileName
     * @return
     */
    private int warning(String field, String value, int lineNum,
            String fileName) {
        String message = "Invalid value detected in " + fileName + ":\nLine "
                + lineNum + ":  " + field + " = " + value
                + ". Value may be null, out of bounds, or less extreme than current normal value.";
        logger.warn(message);

        boolean returnVal = MessageDialog.openQuestion(shell, "Invalid Value",
                message + "\nChoosing Yes will skip over this line and continue with the read");

        if (returnVal) {
            return 1;
        } else {
            return -1;
        }
    }
}

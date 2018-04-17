/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.climate.initClimate.dialog.importdata;

/**
 * Store file data. Mimic struct file_data in appWindImportClimate.C
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ---------- --------------------------
 * 07/29/2016   18469       wkwock     Initial creation.
 * 10/27/2016   20635       wkwock     Clean up
 * 22 DEC 2016  22395       amoore     Correct multi-selection of stations/months; java standards.
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class FileData {

    /**
     * Item is located in data.
     */
    public final static String LOCATED_IN_DATA = "Located in Data";

    /** file path */
    private String pathName;

    /** file name */
    private String fileName;

    /** station name */
    private String stationName;

    /** is daily (true) or (false)monthly */
    private boolean isDaily;

    /** date format */
    private String dateFormat;

    /** delimiter */
    private String delimiter;

    /** choices of data order */
    private int choices[];

    /**
     * Month first date format string.
     */
    public static final String MONTH_FIRST_STRING = "Month First";

    /**
     * Day first date format string.
     */
    public static final String DAY_FIRST_STRING = "Day First";

    /**
     * Julian days format string.
     */
    public static final String JULIAN_DAYS_STRING = "Julian Days";

    /**
     * Julian days where 2/29 is day 60 format string (julian days on a leap
     * year).
     */
    public static final String JULIAN_DAYS_FEB29_60_STRING = "Julian Days(Feb29=60)";

    /**
     * Empty constructor.
     */
    public FileData() {
        pathName = null;
        fileName = null;
        stationName = null;
        isDaily = true;
        dateFormat = null;
        delimiter = "";
        choices = null;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public boolean isDaily() {
        return isDaily;
    }

    public void setDaily(boolean isDaily) {
        this.isDaily = isDaily;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public int[] getChoices() {
        return choices;
    }

    public void setChoices(int[] choices) {
        this.choices = choices;
    }
}

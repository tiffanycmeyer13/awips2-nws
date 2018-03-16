/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.formatter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDate;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;
import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateProduct;
import gov.noaa.nws.ocp.common.dataplugin.climate.Station;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateInvalidParameterException;
import gov.noaa.nws.ocp.common.dataplugin.climate.exception.ClimateQueryException;
import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.common.dataplugin.climate.response.ClimateRunData;
import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductType;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateStationsSetupDAO;

/**
 * Abstract class used to consolidate common functionality for child format
 * classes.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 07, 2017 21099      wpaintsil   Initial creation
 * 13 APR 2017  33104      amoore      Address comments from review.
 * 11 OCT 2017  39212      amoore      Better logging of TimeZone defaulting.
 * 20 NOV 2017  41088      amoore      Snow section was missing in CLM due to
 *                                     faulty checks of reportWindow
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */
public abstract class ClimateFormat {

    /**
     * The logger.
     */
    protected final IUFStatusHandler logger = UFStatus.getHandler(getClass());

    /**
     * Holds the current settings being used from the given list of settings.
     */
    protected ClimateProductType currentSettings = new ClimateProductType();

    /**
     * Holds global configuration.
     */
    protected final ClimateGlobal globalConfig;

    /**
     * Holds list of stations.
     */
    protected Map<Integer, Station> stationMap = new HashMap<>();

    /**
     * The extension for an nwws text product file.
     */
    private static final String NWWS_EXT = ".nwws";

    /**
     * The extension for an nwr text product file.
     */
    private static final String NWR_EXT = ".nwr";

    /**
     * Used to get list of stations.
     */
    protected final ClimateStationsSetupDAO climateStationsSetupDao = new ClimateStationsSetupDAO();

    /**
     * Parameter for String.format to specify a two-digit zero-padded integer.
     */
    protected static final String TWO_DIGIT_INT = "%02d";

    /**
     * Parameter for String.format to specify a three-digit zero-padded integer.
     */
    protected static final String THREE_DIGIT_INT = "%03d";

    /**
     * Parameter for String.format to specify an integer with commas.
     */
    protected static final String INT_COMMAS = "%,d";

    /**
     * Parameter for String.format to specify a float with 1 decimal place.
     */
    protected static final String FLOAT_ONE_DECIMAL = "%.1f";

    /**
     * Parameter for String.format to specify a float with 1 decimal place and
     * String length of seven.
     */
    protected static final String FLOAT_ONE_DECIMAL_SEVEN = "%7.1f";

    /**
     * Parameter for String.format to specify an integer with a space-padded
     * string length of five.
     */
    protected static final String INT_FIVE = "%5d";

    /**
     * Parameter for String.format to specify an integer with a space-padded
     * string length of two.
     */
    protected static final String INT_TWO = "%2d";

    /**
     * Parameter for String.format to specify an integer with a space-padded
     * string length of three.
     */
    protected static final String INT_THREE = "%3d";

    /**
     * Parameter for String.format to specify a float with 2 decimal places.
     */
    protected static final String FLOAT_TWO_DECIMALS1 = "%.2f";

    /**
     * Parameter for String.format to specify a float with 2 decimal places.
     */
    protected static final String FLOAT_TWO_DECIMALS_SEVEN = "%7.2f";

    /**
     * Parameter for String.format to specify a float with 2 decimal places and
     * 0 padded in the ones place.
     */
    protected static final String FLOAT_TWO_DECIMALS2 = "%04.2f";

    /**
     * Parameter for String.format to specify a float with 1 decimal and commas.
     */
    protected static final String FLOAT_COMMAS_ONE_DECIMAL = "%,.1f";

    /**
     * GMT timezone string
     */
    public static final String GMT_STRING = "GMT";

    /**
     * Parameter for String.format to specify a float with 2 decimals and
     * commas.
     */
    protected static final String FLOAT_COMMAS_TWO_DECIMALS = "%,.2f";

    protected static final String AMOUNT = "amount";

    protected static final String AND = "and";

    protected static final String BRINGING = "bringing";

    protected static final String CELSIUS = "celsius";

    protected static final String COLON = ":";

    protected static final String COMMA = ", ";

    protected static final String DEGREE = "degree";

    protected static final String DEGREES = "degrees";

    protected static final String EXACTLY = "exactly";

    protected static final String EVENING = "evening";

    protected static final String FAHRENHEIT = "fahrenheit";

    protected static final String FROM = "from";

    protected static final String FOR = "for";

    protected static final String HAD_FALLEN = "had fallen";

    protected static final String INCH = "inch";

    protected static final String INCHES = "inches";

    protected static final String LEAVING = "leaving";

    protected static final String NO = "no";

    protected static final String AS = "as";

    protected static final String AT = "at";

    protected static final String IN = "in";

    protected static final String IS = "is";

    protected static final String LAST = "last";

    protected static final String MONTHLY = "the monthly total";

    protected static final String NORMAL = "normal";

    protected static final String PERIOD = ".";

    protected static final String OF = "of";

    protected static final String OR = "or";

    protected static final String SET_IN = "set in";

    protected static final String SPACE = " ";

    protected static final String CLIMATE_SUMMARY = "the climate summary";

    protected static final String THE = "the";

    protected static final String THIS = "this";

    protected static final String TOMORROW = "tomorrow";

    protected static final String WHICH = "which";

    protected static final String WAS = "was";

    protected static final String THERE = "there";

    protected static final String THERE_WERE = "there were";

    protected static final String ZERO = "0";

    protected static final String WEATHER_ITEM = "weather item";

    protected static final String OBSERVED = "observed";

    protected static final String TIME = "time";

    protected static final String RECORD = "record";

    protected static final String YEAR = "year";

    protected static final String DEPARTURE = "departure";

    protected static final String VALUE = "value";

    protected static final String ABBRV_LAST = "(LST)";

    protected static final String ABBRV_FAHRENHEIT = "(F)";

    protected static final String TEMPERATURE = "temperature";

    protected static final String YESTERDAY = "yesterday";

    protected static final String TODAY = "today";

    protected static final String MAXIMUM = "maximum";

    protected static final String MINIMUM = "minimum";

    protected static final String AVERAGE = "average";

    protected static final String PRECIPITATION = "precipitation";

    protected static final String MONTH_TO_DATE = "Month to Date";

    protected static final String ABBRV_IN = "(in)";

    protected static final String SNOWFALL = "snowfall";

    protected static final String SNOW_DEPTH = "snow depth";

    protected static final String HEATING = "heating";

    protected static final String COOLING = "cooling";

    protected static final String DEGREE_DAYS = "degree days";

    protected static final String MPH = "(mph)";

    protected static final String WIND = "wind";

    protected static final String GUST = "gust";

    protected static final String SPEED = "speed";

    protected static final String DIRECTION = "direction";

    protected static final String RESULTANT = "resultant";

    protected static final String HIGHEST = "highest";

    protected static final String SKY_COVER = "Sky Cover";

    protected static final String POSSIBLE_SUNSHINE = "Possible Sunshine";

    protected static final String PERCENT = "percent";

    protected static final String WEATHER_CONDITIONS = "weather conditions";

    protected static final String RELATIVE_HUMIDITY = "relative humidity";

    protected static final String LOWEST = "lowest";

    protected static final String AVG_SKY_COVER = "Average Sky Cover";

    protected static final String SUNRISE = "sunrise";

    protected static final String SUNSET = "sunset";

    protected static final String WEATHER = "weather";

    protected static final String DEPART = "depart";

    protected static final String DATES = "date(s)";

    protected static final String DATE = "date";

    protected static final String A_TRACE = "a trace";

    protected static final String TOTAL = "total";

    protected static final String TOTALS = "Totals";

    protected static final String LATEST = "Latest";

    protected static final String EARLIEST = "Earliest";

    protected static final String PRODUCT_TERMINATOR = "$$";

    /**
     * Constructor. Set the current settings and global configuration.
     * 
     * @param currentSettings
     * @param globalConfig
     * @throws ClimateQueryException
     */
    public ClimateFormat(ClimateProductType currentSettings,
            ClimateGlobal globalConfig) throws ClimateQueryException {
        this.globalConfig = globalConfig;
        this.currentSettings = currentSettings;

        List<Station> stations = climateStationsSetupDao.getMasterStations();

        if (stations.isEmpty()) {
            logger.error("No stations returned from getMasterStations query.");
        }

        // Map station to stationIds for easy station lookup.
        for (Station station : stations) {
            stationMap.put(station.getInformId(), station);
        }
    }

    /**
     * Build different text product types.
     * 
     * @param reportData
     * @return
     * @throws ClimateInvalidParameterException
     * @throws ClimateQueryException
     */
    public abstract Map<String, ClimateProduct> buildText(
            ClimateRunData reportData) throws ClimateInvalidParameterException,
                    ClimateQueryException;

    /**
     * Migrated from a subroutine, direction, in build_NWWS_wind.f and a portion
     * of build_NWR_wind.f. Determines the direction based on the numeric value.
     * 
     * @param value
     * @param abbreviated
     *            true if abbreviated direction string, false if full string
     * @return
     * @throws ClimateInvalidParameterException
     */
    public static String whichDirection(int value, boolean abbreviated)
            throws ClimateInvalidParameterException {

        if (value >= 0 && value <= 22) {
            return abbreviated ? " N" : "north";
        } else if (value >= 23 && value <= 68) {
            return abbreviated ? "NE" : "northeast";
        } else if (value >= 69 && value <= 112) {
            return abbreviated ? " E" : "east";
        } else if (value >= 113 && value <= 158) {
            return abbreviated ? "SE" : "southeast";
        } else if (value >= 159 && value <= 203) {
            return abbreviated ? " S" : "south";
        } else if (value >= 204 && value <= 248) {
            return abbreviated ? "SW" : "southwest";
        } else if (value >= 249 && value <= 293) {
            return abbreviated ? " W" : "west";
        } else if (value >= 294 && value <= 337) {
            return abbreviated ? "NW" : "northwest";
        } else if (value >= 338 && value <= 360) {
            return abbreviated ? " N" : "north";
        } else if (value == ParameterFormatClimate.MISSING) {
            // in legacy nothing is returned for unabbreviated direction string
            // where the value is missing
            return abbreviated ? ParameterFormatClimate.MM : "";
        } else {
            throw new ClimateInvalidParameterException(
                    "Invalid direction value: " + value);
        }
    }

    /**
     * Migrated from report_window.f
     * 
     * <pre>
     *    Sept. 1998     Barry Baxter        PRC/TDL
     *  Sept. 1998     David O. Miller     PRC/TDL
     *
     *
     *   Purpose:  To determine if the cooling, heating, and/or snow reports
     *            should be created and included in the format_climate output.
     * 
     * </pre>
     * 
     * @param reportDates
     * @return
     */
    public static boolean reportWindow(ClimateDates reportDates,
            ClimateDate beginDate) {
        /*
         * Rewritten to use more robust logic than the DAY OF YEAR logic of
         * Legacy
         */
        ClimateDate startDate = new ClimateDate(reportDates.getStart());
        ClimateDate endDate = new ClimateDate(reportDates.getEnd());

        startDate.setYear(beginDate.getYear());
        endDate.setYear(beginDate.getYear());

        boolean startCheck = (startDate.equals(beginDate)
                || startDate.before(beginDate));
        boolean endCheck = (endDate.equals(beginDate)
                || endDate.after(beginDate));

        if (startDate.after(endDate)) {
            /*
             * special case of multi-year range; can just use OR for the major
             * checks since we assign the same year to both ends
             */
            if (startCheck || endCheck) {
                // within regular range
                return true;
            } else {
                // outside the range
                return false;
            }
        } else {
            // regular range check
            if (startCheck && endCheck) {
                // within regular range
                return true;
            } else {
                // outside the range
                return false;
            }
        }
    }

    /**
     * Get the file name for the formatted text. File name is of the format
     * "output_<period type>_<product id>.<wire or radio extension>" Example:
     * "output_am_DCA.nwws"
     * 
     * @return
     * @throws ClimateInvalidParameterException
     */
    protected String getName() throws ClimateInvalidParameterException {
        String extension;
        switch (currentSettings.getReportType()) {
        case MORN_RAD:
        case EVEN_RAD:
        case INTER_RAD:
        case MONTHLY_RAD:
        case SEASONAL_RAD:
        case ANNUAL_RAD:
            extension = NWR_EXT;
            break;
        case MORN_NWWS:
        case EVEN_NWWS:
        case INTER_NWWS:
        case MONTHLY_NWWS:
        case SEASONAL_NWWS:
        case ANNUAL_NWWS:
            extension = NWWS_EXT;
            break;
        default:
            throw new ClimateInvalidParameterException("Invalid period type: "
                    + currentSettings.getReportType().toString());
        }

        return "output_" + currentSettings.getReportType().getPeriodName() + "_"
                + currentSettings.getProdId() + extension;

    }

    /**
     * Get the PIL for the file to be used for querying the TextDB.
     * 
     * @return
     */
    private String getPil() {
        return currentSettings.getHeader().getNodeOrigSite()
                + currentSettings.getHeader().getProductCategory()
                + currentSettings.getProdId();
    }

    /**
     * Convert the expiration ClimateDate and ClimateTime in the header to a
     * Calendar object.
     * 
     * @return
     */
    private Calendar getExpirationTime() {
        Calendar cal = TimeUtil.newCalendar();
        cal.set(currentSettings.getHeader().getExpirationDate().getYear(),
                currentSettings.getHeader().getExpirationDate().getMon() - 1,
                currentSettings.getHeader().getExpirationDate().getDay(),
                currentSettings.getHeader().getExpirationTime().getHour(),
                currentSettings.getHeader().getExpirationTime().getMin());
        return cal;
    }

    /**
     * Return a ClimateProduct object containing the the appropriate initialized
     * fields.
     * 
     * @param text
     *            the proper formatted text
     * @return
     * @throws ClimateInvalidParameterException
     */
    protected ClimateProduct getProduct(String text)
            throws ClimateInvalidParameterException {
        return new ClimateProduct(getName(), getPil(), text,
                currentSettings.getReportType(), getExpirationTime());
    }

    /**
     * @return {@link TimeZone} object. If global config timezone is null,
     *         empty, or not parseable, object is based on GMT.
     */
    protected TimeZone parseTimeZone() {
        String timeZoneString = globalConfig.getTimezone();
        if (timeZoneString == null || timeZoneString.isEmpty()) {
            timeZoneString = GMT_STRING;
        }

        TimeZone localTimeZone = TimeZone.getTimeZone(timeZoneString);

        if (!localTimeZone.getID().equalsIgnoreCase(timeZoneString)) {
            logger.warn("Formatter tried to use globalDay timezone of: ["
                    + timeZoneString + "], but Java parsed this as: ["
                    + localTimeZone.getID() + "].");
        }
        return localTimeZone;
    }
}

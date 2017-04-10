/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.parameter;

/**
 * Converted from rehost-adapt/adapt/climate/include/PARAMETER_format_climate.h
 * This include file defines the parameters used in the format_climate program.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 13, 2015 18024      xzhang      Initial creation
 * AUG 23 2016  20414      amoore      Change MISSING SLP to double. Future task
 *                                     should consider avoiding use of floats.
 * OCT 21 2016  20639      wkwock      Add trace symbol
 * 22 FEB 2017  28609      amoore      Comment fixes.
 * 17 APR 2017  33104      amoore      Remove unneeded comments and constants.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
/*
 * Task 29493: reorg all Parameter classes by use rather than module, given
 * values are used across modules
 */
public final class ParameterFormatClimate {

    public static final int DUMMY = -999;

    public static final double DUMMY_FLOAT = -999.9;

    public static final int DUMMY_DATA = -99;

    public static final double DUMMY_SNOW = -9999.9;

    public static final double DUMMY_PRECIP = -9999.99;;

    public static final String BLANK = " ";

    public static final String LF = "10";

    public static final String ZERO_LINE = "                                                                      ";

    public static final String GLOBAL_BLANK_LINE = "                    ";

    public static final String NULL = "\0";

    public static final String MM = "MM";

    public static final String AM = "AM";

    public static final String ENDPROD = "$$";

    public static final int PRODUCT_ID_LEN = 4;

    public static final int MAX_PRODUCT_IDS = 100;

    public static final int MAX_FILE_NAME = 100;

    /** flag for missing data */
    public static final short MISSING = 9999;

    public static final float MISSING_SPEED = MISSING;

    public static final float MISSING_SNOW = 9999.0f;

    public static final float MISSING_PRECIP = 9999.00f;

    public static final int MISSING_DATE = 99;

    public static final int MISSING_DEGREE_DAY = (-1 * MISSING);

    public static final int MISSING_HOUR = 99;

    public static final int MISSING_MINUTE = 99;

    public static final int MISSING_PRECIP_VALUE = MISSING;

    public static final int MISSING_SNOW_VALUE = MISSING;

    public static final double MISSING_SLP = 99.99;

    /**
     * max # of precip char
     */
    public static final int MAX_PRECIP = 7;

    /**
     * max # of wx. types
     */
    public static final int MAX_WX = 18;

    /**
     * # of spaces for CHAR_HOUR and CHAR_MIN
     */
    public static final int NUM_CLOCK = 2;

    /**
     * max # of characters in an ICAO id
     */
    public static final int NUM_ICAO = 5;

    /**
     * # of char. in line feed
     */
    public static final int NUM_FEED = 1;

    /**
     * # of char. in line feed
     */
    public static final int NUM_LINE_NWWS = 69;

    /**
     * # at which to wrap
     */
    public static final int NUM_LINE = 80;

    /**
     * # at which to wrap
     */
    public static final int NUM_LINE1_NWWS = 70;

    public static final int NUM_LINE1 = (NUM_LINE + 1);

    public static final int NUM_TEMP = 100;

    /**
     * # of characters in a year
     */
    public static final int NUM_YEAR = 4;

    /**
     * # of spaces for the time zone letters
     */
    public static final int NUM_ZONE = 3;

    /**
     * flag for trace precip.
     */
    public static final float TRACE = -1.0f;

    /**
     * trace symbol
     */
    public static final String TRACE_SYMBOL = "T";

    /**
     * no error was found (error logging)
     */
    public static final int SUCCESS = 0;

    /**
     * error found but still can run (error logging)
     */
    public static final int WARNING = 1;

    /**
     * error found, cannot run. (error logging)
     */
    public static final int crash = 2;

    /**
     * precision for precip meaasure
     */
    public static final double SIG_PRECIP = 0.01;

    public static final double HALF_PRECIP = (SIG_PRECIP / 2);

    /**
     * precision for snow measure
     */
    public static final double SIG_SNOW = 0.1;

    public static final double HALF_SNOW = (SIG_SNOW / 2);

    private ParameterFormatClimate() {

    }
}

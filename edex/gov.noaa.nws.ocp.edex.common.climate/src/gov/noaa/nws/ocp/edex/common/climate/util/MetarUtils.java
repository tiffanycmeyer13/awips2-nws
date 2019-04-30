/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.util;

/**
 * Values from metar_utils.h. METAR values in database are stored by special key
 * values, listed here..  Also includes definitions of special precipitation
 * values from Common_Defs.h used in the fss_contin_real table.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 Oct 2016  21378      amoore      Initial creation.
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * 26 Apr 2019  DR 21195   dfriedman   Add special case precipitation values.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public final class MetarUtils {

    public static final int METAR_TEMP = 25760;

    public static final int METAR_1HR_PRECIP = 25763;

    public static final int METAR_3HR_PRECIP = 25764;

    public static final int METAR_6HR_PRECIP = 25765;

    public static final int METAR_24HR_PRECIP = 25766;

    public static final int METAR_WX = 25767;

    public static final int METAR_WIND_SPEED = 25769;

    public static final int METAR_WIND_DIRECTION = 25770;

    public static final int METAR_CLOUD_COVER = 25771;

    public static final int METAR_CLOUD_HEIGHT = 25774;

    public static final int METAR_VISIB = 25788;

    public static final int METAR_SNOW_DEPTH = 25776;

    public static final int METAR_VERT_VISIB = 25795;

    public static final int METAR_MSL_PRESS = 25796;

    public static final int METAR_ALT_SETTING = 25797;

    public static final int METAR_3HR_PRESS_CHNG = 25798;

    public static final int METAR_3HR_PRESS_TREND = 25799;

    public static final int METAR_DEWPOINT = 25800;

    public static final int METAR_6HR_MAXTEMP = 25801;

    public static final int METAR_6HR_MINTEMP = 25802;

    public static final int METAR_MAX_WIND_GUST = 25805;

    public static final int METAR_CLOUD_TYPE = 25806;

    public static final int METAR_TEMP_2_TENTHS = 25823;

    public static final int METAR_DEWPOINT_2_TENTHS = 25824;

    public static final int SCD_SNOW = 25822;

    public static final int METAR_24HR_MAXTEMP = 25803;

    public static final int METAR_24HR_MINTEMP = 25804;

    public static final int METAR_PEAK_WIND_SPEED = 25826;

    public static final int METAR_PEAK_WIND_DIR = 25827;

    public static final int METAR_PEAK_WIND_TIME = 25828;

    public static final int METAR_SUNSHINE_DURATION = 25829;

    /**
     * Legacy documentation: Used to indicate that the precipitation sensor was
     * not operational.
     */
    public static final int PNO_PRESENT = -1;

    /**
     * Indicates a trace amount of precipitation in fss_contin_real. Note this
     * is different from the value of TRACE = -1 used almost everywhere else in
     * Climate.
     */
    public static final int FSS_CONTIN_TRACE = -2;

    /**
     * Private constructor. This is a utility class.
     */
    private MetarUtils() {
    }
}

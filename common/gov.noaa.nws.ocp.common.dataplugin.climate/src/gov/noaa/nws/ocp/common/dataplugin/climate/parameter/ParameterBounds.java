package gov.noaa.nws.ocp.common.dataplugin.climate.parameter;

/**
 * Converted from rehost-adapt/adapt/climate/include/PARAMETER_climate_errors.h
 * and PARAMETER_create_climate.h.
 * 
 * This include file defines the error limits for various climate variables. It
 * also defines the limits for the time variables.
 *
 * Constants HOUR_LOWER_BOUND - lower limit on the hour HOUR_UPPER_BOUND - upper
 * limit on the hour MIN_LOWER_BOUND - lower limit on the minute MIN_UPPER_BOUND
 * - upper limit on the minute PRECIP_LOWER_BOUND - lower limit on the
 * precipitation PRECIP_UPPER_BOUND - upper limit on the precipitation
 * SNOW_LOWER_BOUND - lower limit on the snowfall SNOW_UPPER_BOUND - upper limit
 * on the snowfall TEMP_LOWER_BOUND - lower limit on the temperature
 * TEMP_UPPER_BOUND - upper limit on the temperature WIND_DIR_LOWER_BOUND -
 * lower limit on the wind direction WIND_DIR_UPPER_BOUND - upper limit on the
 * wind direction WIND_SPD_LOWER_BOUND - lower limit on the wind speed
 * WIND_SPD_UPPER_BOUND - upper limit on the wind speed MIN_SUN_UPPER_BOUND -
 * upper limit on the minutes of sun MIN_SUN_LOWER_BOUND - lower limit on the
 * minutes of sun PRESS_UPPER_BOUND - upper limit on the pressure values
 * PRESS_LOWER_BOUND - lower limit on the pressure values
 *
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date        Ticket#   Engineer    Description
 * ----------- --------- ----------- --------------------------
 * 08/16/2016  21198     wkwock      Initial creation
 * OCT 28 2016 21378     amoore      Added values present in other files.
 * 11/07/2016  20635     wkwock      Added year bounds.
 * 
 * </pre>
 * 
 * @author wkwock
 * @version 1.0
 */
public class ParameterBounds {

    public static final int TEMP_LOWER_BOUND = -120;

    public static final int TEMP_UPPER_BOUND = 135;

    public static final int TEMP_UPPER_BOUND_QC = 140;

    public static final float PRECIP_UPPER_BOUND = 999.0f;

    public static final float PRECIP_LOWER_BOUND = 0.0f;

    public static final float SNOW_UPPER_BOUND = 1500.0f;

    public static final float SNOW_LOWER_BOUND = 0.0f;

    public static final int WIND_DIR_UPPER_BOUND = 360;

    public static final int WIND_DIR_LOWER_BOUND = 0;

    public static final int WIND_SPD_UPPER_BOUND = 250;

    public static final int WIND_SPD_LOWER_BOUND = 0;

    public static final int SPD_UPPER_BOUND_QC = 200;

    public static final int RH_UPPER_BOUND = 105;

    public static final int RH_LOWER_BOUND = 0;

    public static final int HOUR_UPPER_BOUND = 23;

    public static final int HOUR_LOWER_BOUND = 0;

    public static final int MIN_UPPER_BOUND = 59;

    public static final int MIN_LOWER_BOUND = 0;

    public static final int PERCENT_UPPER_BOUND = 100;

    public static final int PERCENT_LOWER_BOUND = 0;

    public static final float SKY_UPPER_BOUND = 1.0f;

    public static final float SKY_LOWER_BOUND = 0.0f;

    public static final int DEGREE_DAY_LOWER = 0;

    public static final int DEGREE_DAY_UPPER = 25000;

    public static final int MIN_SUN_LOWER_BOUND = 0;

    public static final int MIN_SUN_UPPER_BOUND = 1440;

    public static final float PRESS_LOWER_BOUND = 10.0f;

    public static final float PRESS_UPPER_BOUND = 50.0f;

    public static final int MIN_DEW_QC = -100;

    public static final int MAX_DEW_QC = 100;

    public static final int SPECI_UPPER_BOUND_QC = 200;

    public static final int SPECI_LOWER_BOUND_QC = 0;

    public static final int PEAK_TIME_UPPER_BOUND_QC = 2359;

    public static final int PEAK_TIME_LOWER_BOUND_QC = 0;

    public static final int MAX_SPECI = 240;

    public static final int YEAR_LOWER_BOUND = 1800;

    public static final int YEAR_UPPER_BOUND = 3000;
}

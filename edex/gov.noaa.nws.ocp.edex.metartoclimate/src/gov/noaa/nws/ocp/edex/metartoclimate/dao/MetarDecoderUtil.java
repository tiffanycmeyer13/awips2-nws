/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.metartoclimate.dao;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.parameter.ParameterFormatClimate;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAO;
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateDAOValues;
import gov.noaa.nws.ocp.edex.common.climate.util.MetarUtils;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.CloudConditions;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.DecodedMetar;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.QCMetar;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.RecentWx;
import gov.noaa.nws.ocp.edex.metartoclimate.dao.data.SurfaceObs;

/**
 * Utility class for METAR decoding.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 07 SEP 2017  37754      amoore      Initial creation.
 * 27 OCT 2017  40123      amoore      Simply time nominalization logic.
 * 26 APR 2019  DR 21195   dfriedman   Fix "precip not available" processing.
 * </pre>
 * 
 * @author amoore
 */
public final class MetarDecoderUtil {

    /**
     * The logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MetarDecoderUtil.class);

    /**
     * Missing data value. From hmPED_WMO.h.
     */
    protected static final int MISSING_DATA = 9999;
    /**
     * METAR cloud cover value scale.
     */
    protected static final int METAR_CLOUD_COVER_SCALE = 10;
    /**
     * Meters to 100s of feet. From check_METAR_quality.c.
     */
    protected static final float M_TO_100S_OF_FT = 0.0328f;
    /**
     * Tornado int value. From hmPED_WMO.h.
     */
    protected static final int TORNADO_VALUE = 1;
    /**
     * Funnel cloud int value. From hmPED_WMO.h.
     */
    protected static final int FUNNEL_CLOUD_VALUE = 2;
    /**
     * Water spout int value. From hmPED_WMO.h.
     */
    protected static final int WATER_SPOUT_VALUE = 3;
    /**
     * Automated indicators. According to documentation it seems these should be
     * A, then 0 (zero), and then 1 or 2 per examples. However in practice and
     * even in official in-text METAR documentation it seems that O (the letter)
     * is also used instead of 0 (zero).
     */
    /**
     * Automated indicator 2 String numerical.
     */
    protected static final String A02_INDICATOR_STRING_NUM = "A02";
    /**
     * Automated indicator 1 String, numerical.
     */
    protected static final String A01_INDICATOR_STRING_NUM = "A01";
    /**
     * Automated indicator 2 String.
     */
    protected static final String A02_INDICATOR_STRING = "AO2";
    /**
     * Automated indicator 1 String.
     */
    protected static final String A01_INDICATOR_STRING = "AO1";
    /**
     * METAR horizontal visibility value scale. For data validation. From
     * check_METAR_quality.c.
     * 
     * Task 29248: This is a concerning scale to use and does not match the
     * other scale.
     */
    protected static final int METAR_HORIZ_VISIB_VALIDITY_SCALE = 16;
    /**
     * Kilometers abbreviation.
     */
    protected static final String KILOMETERS_ABBR = "KM";
    /**
     * Statute miles abbreviation.
     */
    protected static final String STATUTE_MILES_ABBR = "SM";
    /**
     * Visibility is greater than 6 statute miles indicator.
     */
    protected static final String VISIBILITY_GREATER_6_SM = "P6SM";
    /**
     * Visibility is less than a quarter of a statute mile indicator.
     */
    protected static final String VISIBILITY_LESS_QUARTER_SM = "M1/4SM";
    /**
     * Runway visual range prefix.
     */
    protected static final String RUNWAY_VISUAL_RANGE_PREFIX = "R";
    /**
     * Altimeter reading in mercury units prefix.
     */
    protected static final String ALTIMETER_HG_PREFIX = "A";
    /**
     * Remarks abbreviation.
     */
    protected static final String REMARKS_ABBR = "RMK";
    /**
     * Maintenance indicator string.
     */
    protected static final String MAINTENANCE_INDICATOR = "$";
    /**
     * No secondary ceiling height indicator string.
     */
    protected static final String NO_SECONDARY_CEILING_HEIGHT_INDICATOR = "CHINO";
    /**
     * No secondary visuals indicator string.
     */
    protected static final String NO_SECONDARY_VISUALS_INDICATOR = "VISNO";
    /**
     * No lightning indicator string.
     */
    protected static final String NO_LIGHTNING_INDICATOR = "TSNO";
    /**
     * No freezing rain indicator string.
     */
    protected static final String NO_FREEZING_RAIN_INDICATOR = "FZRANO";
    /**
     * No rain indicator string.
     */
    protected static final String NO_RAIN_INDICATOR = "PNO";
    /**
     * No peak wind indicator string.
     */
    protected static final String NO_PEAK_WIND_INDICATOR = "PWINO";
    /**
     * No runway visual range indicator string.
     */
    protected static final String NO_RUNWAY_VISUAL_RANGE_INDICATOR = "RVRNO";
    /**
     * Temperature and dewpoint prefix.
     */
    protected static final String TEMP_AND_DEW_PREFIX = "T";
    /**
     * Water equivalent prefix.
     */
    protected static final String WATER_EQUIV_PREFIX = "933";
    /**
     * Snow depth prefix.
     */
    protected static final String SNOW_DEPTH_PREFIX = "4/";
    /**
     * 24-hour precip prefix.
     */
    protected static final String PRECIP_24_HOUR_PREFIX = "7";
    /**
     * For some METAR data, a series of slashes signifies missing data.
     */
    protected static final String MISSING_VALUE_SLASHES = "////";
    /**
     * 3- or 6-hourly precip prefix.
     */
    protected static final String PRECIP_6_OR_3_HOURLY_PREFIX = "6";
    /**
     * Hourly precip prefix.
     */
    protected static final String HOURLY_PRECIP_PREFIX = "P";
    /**
     * Last indicator string.
     */
    protected static final String LAST_INDICATOR = "LAST";
    /**
     * First indicator string.
     */
    protected static final String FIRST_INDICATOR = "FIRST";
    /**
     * Snow increasing rapidly abbreviation.
     */
    protected static final String SNOW_INCREASING_RAPIDLY_ABBR = "SNINCR";
    /**
     * No special report indicator string.
     */
    protected static final String NO_SPECIAL_REPORT_INDICATOR = "NOSPECI";
    /**
     * Sea level pressure prefix.
     */
    protected static final String SEA_LEVEL_PRESSURE_PREFIX = "SLP";
    /**
     * No sea level pressure indicator string.
     */
    protected static final String NO_SEA_LEVEL_PRESSURE_INDICATOR = SEA_LEVEL_PRESSURE_PREFIX
            + "NO";
    /**
     * Pressure rising rapidly indicator string.
     */
    protected static final String PRESSURE_RISING_RAPIDLY_INDICATOR = "PRESRR";
    /**
     * Pressure falling rapidly indicator string.
     */
    protected static final String PRESSURE_FALLING_RAPIDLY_INDICATOR = "PRESFR";
    /**
     * Aurora borealis indicator string.
     */
    protected static final String AURORA_BOREALIS_INDICATOR = "AURBO";
    /**
     * ROTOR CLD suffix.
     */
    protected static final String ROTOR_CLD_SUFFIX = "CLD";
    /**
     * ROTOR CLD prefix.
     */
    protected static final String ROTOR_CLD_PREFIX = "ROTOR";
    /**
     * ROTOR CLD string.
     */
    protected static final String ROTOR_CLD_STRING = ROTOR_CLD_PREFIX + " "
            + ROTOR_CLD_SUFFIX;
    /**
     * Fog RGD indicator string.
     */
    protected static final String FOG_RGD_INDICATOR = "RGD";
    /**
     * Ceiling height abbreviation.
     */
    protected static final String CEILING_HEIGHT_ABBR = "CIG";
    /**
     * VIRGA string.
     */
    protected static final String VIRGA_STRING = "VIRGA";
    /**
     * Hail minimum size value. 1/8 inch.
     */
    protected static final float HAIL_MINIMUM_SIZE_VALUE = 0.125f;
    /**
     * Hail minimum size string. Less than 0.25 inches.
     */
    protected static final String HAIL_MINIMUM_SIZE_STRING = "M1/4";
    /**
     * Hail abbreviation.
     */
    protected static final String HAIL_ABBR = "GR";
    /**
     * Thunderstorm prefix.
     */
    protected static final String THUNDERSTORM_PREFIX = "TS";
    /**
     * Lightning type prefix.
     */
    protected static final String LIGHTNING_TYPE_PREFIX = "LTG";
    /**
     * Constant lightning string.
     */
    protected static final String CONSTANT_LIGHTNING_STRING = "CONS";
    /**
     * Frequent lightning string.
     */
    protected static final String FREQUENT_LIGHTNING_STRING = "FRQ";
    /**
     * Occasional lightning string.
     */
    protected static final String OCCASIONAL_LIGHTNING_STRING = "OCNL";
    /**
     * Feet string.
     */
    protected static final String FEET_STRING = "FT";
    /**
     * Dispatch visual range string.
     */
    protected static final String DISPATCH_VISUAL_RANGE_STRING = "DVR";
    /**
     * Dispatch visual range prefix.
     */
    protected static final String DISPATCH_VISUAL_RANGE_PREFIX = DISPATCH_VISUAL_RANGE_STRING
            + "/";
    /**
     * Dispatch visual range minus prefix.
     */
    protected static final String DISPATCH_VISUAL_RANGE_MINUS_PREFIX = DISPATCH_VISUAL_RANGE_PREFIX
            + "M";
    /**
     * Dispatch visual range plus prefix.
     */
    protected static final String DISPATCH_VISUAL_RANGE_PLUS_PREFIX = DISPATCH_VISUAL_RANGE_PREFIX
            + "P";
    /**
     * Runway "RWY" string used in METAR documentation for locations.
     */
    protected static final String RUNWAY_RWY_STRING = "RWY";
    /**
     * Runway 'RY' string used in Legacy for locations.
     */
    protected static final String RUNWAY_RY_STRING = "RY";
    /**
     * Slash divider in METAR report. Divides fractions and some multi-part data
     * groups.
     */
    protected static final String SLASH_DIVIDER = "/";
    /**
     * Variable data flag in METAR report. 'V'.
     */
    protected static final String VARIABLE_DATA_FLAG = "V";
    /**
     * Surface visibility prefix.
     */
    protected static final String SURFACE_VISIBILITY_PREFIX = "SFC";
    /**
     * Visibility prefix.
     */
    protected static final String VISIBILITY_PREFIX = "VIS";
    /**
     * Tower visibility prefix.
     */
    protected static final String TOWER_PREFIX = "TWR";
    /**
     * Wind shift string.
     */
    protected static final String WIND_SHIFT_STRING = "WSHFT";
    /**
     * Peak wind remarks suffix.
     */
    protected static final String PEAK_WIND_SUFFIX = "WND";
    /**
     * Peak wind remarks prefix.
     */
    protected static final String PEAKWIND_PREFIX = "PK";
    /**
     * QC/validity error source status flag.
     */
    protected static final int QC_ERROR_SOURCE_STATUS = 2;
    /**
     * All valid WX Symbols (without +- intensity symbol). From
     * hmPED_Obscuration.c.
     */
    protected static final List<String> VALID_WX_SYMBOLS = Arrays.asList("BCFG",
            "BLDU", "BLSA", "BLSN", "BLPY", "FZBR", "VCBR", "BR", "DRDU",
            "DRSA", "DRSN", "DS", "FZFG", "FZDZ", "FZRA", "DZ", "TSRA", "TSSN",
            "TSPE", "TSPL", "TSGS", "TSGR", "PRFG", "FU", "GS", "GR", "HZ",
            "IC", "MIFG", "VCBLSA", "VCBLSN", "SHRA", "SHSN", "SHPE", "SHPL",
            "SHGS", "SHGR", "RA", "SN", "SG", "SQ", "SA", "SS", "VCTS", "TS",
            "VA", "VCFG", "VCFC", "VCSH", "VCPO", "VCBLDU", "PE", "PL", "PO",
            "DU", "FG", "FC", "NSW", "UP");
    /**
     * All valid Recent WX Symbols. From hmPED_RecentWX.c and supplemented both
     * from testing and comparing with listing in
     * http://weather.cod.edu/notes/metar.html.
     */
    protected static final List<String> VALID_RECENT_WX_SYMBOLS = Arrays.asList(
            "-DZ", "DZ", "+DZ", "FZDZ", "-RA", "RA", "+RA", "SHRA", "TSRA",
            "FZRA", "-SN", "SN", "+SN", "DRSN", "BLSN", "SHSN", "TSSN", "-SG",
            "SG", "+SG", "IC", "-PL", "PL", "+PL", "SHPL", "TSPL", "GR", "SHGR",
            "TSGR", "GS", "SHGS", "TSGS", "-GS", "+GS", "TS", "VCTS", "-TSRA",
            "TSRA", "+TSRA", "-TSSN", "TSSN", "+TSSN", "-TSPL", "TSPL", "+TSPL",
            "-TSGS", "TSGS", "+TSGS", "VCSH", "-SHRA", "+SHRA", "-SHSN",
            "+SHSN", "-SHPL", "+SHPL", "-SHGS", "+SHGS", "-FZDZ", "+FZDZ",
            "-FZRA", "+FZRA", "FZFG", "+FZFG", "BR", "FG", "VCFG", "MIFG",
            "PRFG", "BCFG", "FU", "VA", "DU", "DRDU", "BLDU", "SA", "DRSA",
            "BLSA", "HZ", "BLPY", "BLSN", "+BLSN", "VCBLSN", "BLSA", "+BLSA",
            "VCBLSA", "+BLDU", "VCBLDU", "PO", "VCPO", "SQ", "FC", "+FC",
            "VCFC", "SS", "+SS", "VCSS", "DS", "+DS", "VCDS", "UP");
    /**
     * Recent weather events regex. Created from analysis of hmPED_RecentWX.c
     * documentation:
     * 
     * Given the following recent weather string in the remarks section of a
     * METAR:
     *
     * RAB30PLB35RAE40SNB45PLE50SNE55PLB55
     *
     * This routine will parse it as follows:
     *
     * <pre>
     * wx_type: RA     wx_type: PL     wx_type: SN     wx_type: PL
     * beg time: HH30  beg time: HH35  beg time: HH45  beg time: HH55
     * end time: HH40  end time: HH50  end time: HH55  end time: ----
     * </pre>
     * 
     * A single weather marker need not have only two time slots, as with snow
     * below:
     * 
     * UPE1250SNB05E35B43
     */
    protected static final String RECENT_WX_REGEX = "^((\\+|-)?[A-Z]{2,6}?((B|E)([0-9]{2,4}))+)+$";
    /**
     * Recent weather individual event regex.
     */
    protected static final String SINGLE_RECENT_WX_REGEX = "((\\+|-)?[A-Z]{2,6}?((B|E)([0-9]{2,4}))+)";
    /**
     * Recent weather individual time regex.
     */
    protected static final String SINGLE_RECENT_WX_TIME_REGEX = "(B|E)([0-9]{2,4})";
    /**
     * All valid relative location symbols, from hmPED_validloc.c.
     */
    protected static final List<String> VALID_LOC_SYMBOLS = Arrays
            .asList("DSNT", "VC", "VCY", "DSTN", "VCNTY");
    /**
     * All valid relative direction symbols, originally from hmPED_validdir.c.
     * Enhanced due to obvious use of an expanded set of directions in present
     * day reports.
     */
    /**
     * Valid cardinal directions.
     */
    protected static final List<String> VALID_CARDINAL_DIR = Arrays.asList("N",
            "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW",
            "WSW", "W", "WNW", "NW", "NNW");
    /**
     * Cardinal direction separator.
     */
    protected static final String DIR_SEPARATOR = "-";
    /**
     * Other direction symbols, valid on their own.
     */
    protected static final List<String> VALID_DIR_SYMBOLS = Arrays
            .asList("ALQDS", "OHD", "BLDG", "OVHD");
    /**
     * Valid dissipation flags for SIG.
     */
    protected static final List<String> VALID_DISS_SYMBOLS = Arrays
            .asList("DSIPT", "DSIPTD", "DSIPTG", "DSIPTN", "DSIPTS");
    /**
     * All valid relative movement symbols, from hmPED_validmov.c.
     */
    protected static final List<String> VALID_MOV_SYMBOLS = Arrays.asList("MOV",
            "MOVG");
    /**
     * All valid SIG (significant cloud) types, from hmPED_SigClouds.c.
     */
    protected static final List<String> VALID_SIG_TYPES = Arrays.asList("CB",
            "CBMAM", "CU", "TCU", "ACC", "ACCAS", "SCSL", "ACSL", "CCSL",
            ROTOR_CLD_PREFIX);
    /**
     * Kilometers to statute miles.
     */
    protected static final float KM_TO_SM = 0.621371f;
    /**
     * Meter to foot.
     */
    protected static final float M_TO_FT = 3.28084f;
    /**
     * Millibar to inches of mercury.
     */
    protected static final float MB_TO_HG = 0.02953f;
    /**
     * Station ID regex. 4 alphanumeric characters.
     */
    protected static final String STATION_ID_REGEX = "^[a-zA-Z0-9]{4}$";
    /**
     * Any alphanumeric pattern regex.
     */
    protected static final String ALPHANUMERIC_REGEX = "^[0-9a-zA-Z]+$";
    /**
     * Datetime regex. 6 numbers followed by Z.
     */
    protected static final String DATETIME_REGEX = "^[0-9]{6}Z$";
    /**
     * Numeric-only regex.
     */
    protected static final String NUM_ONLY_REGEX = "^[0-9]+$";
    /**
     * Variable wind direction regex.
     */
    protected static final String VAR_WIND_DIR_REGEX = "^[0-9]{3}V[0-9]{3}$";
    /**
     * Ceiling height (CIG) variability regex.
     */
    protected static final String CIG_VAR_REGEX = "^[0-9]{3}V[0-9]{3}$";
    /**
     * Obscuration sky cover regex.
     */
    protected static final String OBSCUR_SKY_COVER_REGEX = "^(FEW|SCT|BKN|OVC)[0-9]{3}$";
    /**
     * Basic single or double digit visibility regex.
     */
    protected static final String SINGLE_OR_DOUBLE_DIGIT_VISIB_REGEX = "^[0-9]{1,2}(SM|KM)$";
    /**
     * Improperly formatted fractional visibility regex, where the whole number
     * was not separated from the fraction.
     */
    protected static final String BAD_FRACTIONAL_VISIB_REGEX = "^[0-9]{2,3}\\/[0-9]{1,2}(SM|KM)$";
    /**
     * Unitless improperly formatted fractional visibility regex, where the
     * whole number was not separated from the fraction.
     */
    protected static final String UNITLESS_BAD_FRACTIONAL_VISIB_REGEX = "^[0-9]{2,3}\\/[0-9]{1,2}$";
    /**
     * Fractional visibility regex.
     */
    protected static final String FRACTIONAL_VISIB_REGEX = "^[0-9]\\/[0-9]{1,2}(SM|KM)$";
    /**
     * Unitless fractional visibility regex.
     */
    protected static final String UNITLESS_FRACTIONAL_VISIB_REGEX = "^[0-9]\\/[0-9]{1,2}$";
    /**
     * Improperly formatted temp and dew regex, where some invalid parts may
     * have taken the place of the dew values. The first part of the temp/dew
     * regex only.
     */
    protected static final String STARTING_TEMP_AND_DEW_REGEX = "^M?[0-9]{2,3}\\/";
    /**
     * Temperature and dewpoint regex.
     */
    protected static final String TEMP_AND_DEW_REGEX = "^M?[0-9]{2,3}\\/M?[0-9]{2,3}$";
    /**
     * Altimeter regex.
     */
    protected static final String ALTIMETER_REGEX = "^(A|Q)[0-9]{4}$";
    /**
     * Peak wind regex.
     */
    protected static final String PEAK_WIND_REGEX = "^[0-9]{3}[0-9]{2,3}\\/[0-9]{2}([0-9]{2})?$";
    /**
     * Fraction regex.
     */
    protected static final String FRACTION_REGEX = "^[0-9]+\\/[0-9]+$";
    /**
     * Synoptic cloud types prefix.
     */
    protected static final String SYNOP_CLOUD_TYPES_PREFIX = "8/";
    /**
     * Synoptic cloud types regex.
     */
    protected static final String SYNOP_CLOUD_TYPES_REGEX = "^8\\/([0-9]|\\/){3}$";
    /**
     * Sunshine duration prefix.
     */
    protected static final String SUNSHINE_PREFIX = "98";
    /**
     * Sunshine duration regex.
     */
    protected static final String SUNSHINE_REGEX = "^98([0-9]{3}|\\/{3})$";
    /**
     * 6-hourly max temp regex.
     */
    protected static final String MAX_TEMP_6_HOUR_REGEX = "^1(0|1)[0-9]{3}$";
    /**
     * 6-hourly min temp regex.
     */
    protected static final String MIN_TEMP_6_HOUR_REGEX = "^2(0|1)[0-9]{3}$";
    /**
     * 24-hour max and min temp prefix.
     */
    protected static final String MAX_MIN_TEMPS_24_HOUR_PREFIX = "4";
    /**
     * 3-hourly pressure tendency prefix.
     */
    protected static final String PRESS_TEND_3_HOUR_PREFIX = "5";
    /**
     * 3-hourly pressure tendency regex.
     */
    protected static final String PRESS_TEND_3_HOUR_REGEX = "^5[0-8][0-9]{3}$";
    /**
     * Runway visual range regex.
     * 
     * R12L/M4523
     * 
     * R20/P344FT/U
     * 
     * R23C/453V4622/N
     * 
     * R07/4500VP6000FT
     */
    protected static final String RVR_REGEX = "^R[0-9]{2}(R|L|C)?\\/(((M|P)?[0-9]{3,4})(V(M|P)?[0-9]{3,4})?)(FT)?(\\/(U|D|N))?$";
    /**
     * Core variable visibility regex.
     */
    protected static final String CORE_VARIABLE_VISIBILITY_REGEX = "^[0-9]+(\\/[0-9]+)?V[0-9]+(\\/[0-9]+)?$";
    /**
     * Overcast clouds string.
     */
    protected static final String OVERCAST_CLOUDS_STRING = "OVC";
    /**
     * Clear skies string.
     */
    protected static final String CLEAR_SKY_STRING = "CLR";
    /**
     * Skies clear string.
     */
    protected static final String SKY_CLEAR_STRING = "SKC";
    /**
     * Broken clouds string.
     */
    protected static final String BROKEN_CLOUDS_STRING = "BKN";
    /**
     * Scattered clouds string.
     */
    protected static final String SCATTERED_CLOUDS_STRING = "SCT";
    /**
     * Few clouds string.
     */
    protected static final String FEW_CLOUDS_STRING = "FEW";
    /**
     * Funnel cloud string.
     */
    protected static final String FUNNEL_CLOUD_STRING = "FUNNEL CLOUD";
    /**
     * Cloud string.
     */
    protected static final String CLOUD_STRING = "CLOUD";
    /**
     * Funnel string.
     */
    protected static final String FUNNEL_STRING = "FUNNEL";
    /**
     * Waterspout string.
     */
    protected static final String WATERSPOUT_STRING = "WATERSPOUT";
    /**
     * Tornado string.
     */
    protected static final String TORNADO_STRING = "TORNADO";
    /**
     * METAR vertical visibility prefix for cloud parsing.
     */
    protected static final String VERTICAL_VISIBILITY_PREFIX = "VV";
    /**
     * Knots string.
     */
    protected static final String KT_STRING = "KT";
    /**
     * Kilometers per hour string.
     */
    protected static final String KMH_STRING = "KMH";
    /**
     * Meters per second string.
     */
    protected static final String MPS_STRING = "MPS";
    /**
     * Decoding error METAR status flag.
     */
    protected static final int DECODING_ERROR_METAR_STATUS = 2;
    /**
     * Decoding error source status flag.
     */
    protected static final int DECODER_ERROR_SOURCE_STATUS = 1;
    /**
     * Meters per second to knots. From metardecoderP.C.
     */
    protected static final float MPS_PER_KTS = 3600f / 1852;
    /**
     * Kilometers per hour to knots. From metardecoderP.C.
     */
    protected static final float KMH_PER_KT = 1000f / 1852;

    /**
     * From check_source_status.c#check_source_status.
     * 
     * <pre>
     * MODULE NUMBER: 1
    * MODULE NAME:   check_source_status
    * PURPOSE:       This routine checks through the dqd flags for all of the 
    *                decoded METAR elements for the METAR report currently being
    *                processed. The source status flag is set according to the 
    *                values of the individual dqd flags.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I/O    int*        source_status        A pointer to the value of the
    *                                           source status flag.
    *   I      QC_METAR*   qc_Ptr               A pointer to the QC_METAR
    *                                           structure that corresponds to
    *                                           the METAR report currently being 
    *                                           processed.
     * </pre>
     * 
     * @param qcMetar
     * @return
     */
    protected static int checkSourceStatus(QCMetar qcMetar) {
        int sourceStatus = 0;

        // clouds
        sourceStatus = setSourceStatus(qcMetar.getLowCloudHgtDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getLowCloudCoverDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getLowCloudTypeDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getMidCloudHgtDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getMidCloudCoverDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getMidCloudTypeDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getHighCloudHgtDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getHighCloudCoverDqd(),
                sourceStatus);

        sourceStatus = setSourceStatus(qcMetar.getHighCloudTypeDqd(),
                sourceStatus);

        /*
         * Task #29189: Legacy does not check the numbered cloud layers
         */

        /*
         * Task #29189: legacy has a comment to check WX DQD, but does not do it
         */

        /* Check the validity of the horizontal visibility. */
        sourceStatus = setSourceStatus(qcMetar.getVsbyDqd(), sourceStatus);

        /* Check the validity of the veritical visibility. */
        sourceStatus = setSourceStatus(qcMetar.getVertVsbyDqd(), sourceStatus);

        /* Check the validity of the altimeter setting. */
        sourceStatus = setSourceStatus(qcMetar.getAltSettingDqd(),
                sourceStatus);

        /* Check the validity of the sea level pressure. */
        sourceStatus = setSourceStatus(qcMetar.getSLPDqd(), sourceStatus);

        /* Check the validity of the 3 hour pressure change. */
        sourceStatus = setSourceStatus(qcMetar.getPresChg3hrDqd(),
                sourceStatus);

        /* Check the validity of the 3 hour pressure tendency. */
        sourceStatus = setSourceStatus(qcMetar.getPresTendDqd(), sourceStatus);

        /* Check the validity of the temperature. */
        sourceStatus = setSourceStatus(qcMetar.getTempDqd(), sourceStatus);

        /* Check the validity of the dewpoint. */
        sourceStatus = setSourceStatus(qcMetar.getDewPtDqd(), sourceStatus);

        /* Check the validity of the 6 hour maximum temperature. */
        sourceStatus = setSourceStatus(qcMetar.getMaxTemp6hrDqd(),
                sourceStatus);

        /* Check the validity of the 6 hour minimum temperature. */
        sourceStatus = setSourceStatus(qcMetar.getMinTemp6hrDqd(),
                sourceStatus);

        /* Check the validity of the 1 hour precipitation amount. */
        sourceStatus = setSourceStatus(qcMetar.getPrecip1hrDqd(), sourceStatus);

        /* Check the validity of the 6 hour precipitation amount. */
        sourceStatus = setSourceStatus(qcMetar.getPrecip6hrDqd(), sourceStatus);

        /*
         * Task #29189: legacy does not check precip 3-hour DQD
         */

        /* Check the validity of the 24 hour precipitation amount. */
        sourceStatus = setSourceStatus(qcMetar.getPrecip24hrDqd(),
                sourceStatus);

        /* Check the validity of the snow depth. */
        sourceStatus = setSourceStatus(qcMetar.getSnowDepthDqd(), sourceStatus);

        /* Check the validity of the wind direction. */
        sourceStatus = setSourceStatus(qcMetar.getWindDirDqd(), sourceStatus);

        /* Check the validity of the wind speed. */
        sourceStatus = setSourceStatus(qcMetar.getWindSpdDqd(), sourceStatus);

        /* Check the validity of the wind gust speed. */
        sourceStatus = setSourceStatus(qcMetar.getGustSpdDqd(), sourceStatus);

        /*
         * Task #29189: legacy does not check peak wind DQDs
         */

        return sourceStatus;
    }

    /**
     * From check_source_status.c#set_source_status.
     * 
     * <pre>
     * MODULE NUMBER:  2
    * MODULE NAME:    set_source_status
    * PURPOSE:        This routine checks to see if a decoded METAR element
    *                 has passed or failed its QC check. If it has failed,
    *                 the source status flag is set to reflect this.
    *
    *                 The values representing the error codes are
    *                 as follows:
    *
    *                 binary       Error name           Decimal
    *                 ------       ----------           -------
    *                 0000         NO_errors                  0
    *                 0001         DECODE_error               1 
    *                 0010         QC_error                   2
    *                 0011         DECODE_QC_error            3
    *                 0100         TBD_error                  4
    *                 0101         DECODE_TBD_error           5
    *                 0110         QC_TBD_error               6
    *                 0111         DECODE_QC_TBD_error        7
    *
    *                 Therefore, an inclusive bitwise OR of a status
    *                 of NO_errors and a status of QC_error yields a
    *                 status of QC_error while an inclusive bitwise OR
    *                 of a status of QC_error and a status of DECODE_error
    *                 yields a status of DECODE_QC_error, etc.
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      char        dqd                  The data quality descriptor 
    *                                           flag associated with the 
    *                                           particular decoded METAR weather
    *                                           element being processed.
    *   I/O    int*        source_status        The source status flag whose
    *                                           value is set according to 
    *                                           the state of the dqd flag.
     * </pre>
     * 
     * @param dqdValue
     * @param sourceStatus
     * @return
     */
    protected static int setSourceStatus(String dqdValue, int sourceStatus) {
        switch (dqdValue) {
        case QCMetar.FAILED_VALIDITY_CHECK:
            /*
             * Failed validity.
             */
            return sourceStatus | QC_ERROR_SOURCE_STATUS;
        case QCMetar.DECODER_ERROR:
            /*
             * Decoding error.
             */
            return sourceStatus | DECODER_ERROR_SOURCE_STATUS;
        default:
            /*
             * The quality control was either a 'Z' or a 'C'. In either case we
             * do nothing to the source_status flag.
             */
            return sourceStatus;
        }
    }

    /**
     * 
     * @param units
     *            units to be converted to knots.
     * @return multiplier to use on a value in the given units to convert to
     *         knots.
     */
    protected static float getToKnotsMultiplier(String units) {
        switch (units) {
        case MPS_STRING:
            return 1 / MPS_PER_KTS;
        case KMH_STRING:
            return 1 / KMH_PER_KT;
        default:
            // assumed to already be knots
            return 1;
        }
    }

    /**
     * 
     * @param cloudType
     * @return cloud cover value based on given cloud type.
     */
    protected static float getCloudCoverFromCloudType(String cloudType) {
        switch (cloudType) {
        case FEW_CLOUDS_STRING:
            return CloudConditions.FEW_COVER;
        case SCATTERED_CLOUDS_STRING:
            return CloudConditions.SCT_COVER;
        case BROKEN_CLOUDS_STRING:
            return CloudConditions.BKN_COVER;
        default:
            return CloudConditions.OVC_COVER;
        }
    }

    /**
     * From hmPED_Hgt2Meters.c.
     * 
     * <pre>
     * FUNCTION DESCRIPTION
    Converts a coded cloud height value into meters.
    
    PARAMETERS
    Type        Name    I/O     Description
    char            string  Input   A pointer to a character string that 
                    contains the current token.
    RETURNS
    The converted coded cloud height value in meters.   
    ERRORS REPORTED
    None.
    
    HISTORY
    March 1996  Carl McCalla    Intial Coding
    04/05/96    Doug Rankin     Added Trace information
    04/15/96    Doug Rankin Added PROLOGUE and comments
    04/25/96    Doug Rankin Added inline documentation
        03/21/98        Bryon Lawrence  Renamed this routine from 
                                        Hgt2Meters to hmPED_Hgt2Meters.
     * </pre>
     * 
     * @param feetHeight
     *            in hundreds of feet
     * @return height in meters
     */
    protected static int convertCloudFeetToMeters(int feetHeight) {
        if (feetHeight == 999) {
            // max height
            return 30000;
        } else {
            // multiply by 30 to get meters (from
            // hmPED_Hgt2Meters.c.)
            return feetHeight * 30;
        }
    }

    /**
     * From hmHMU
     * 
     * <pre>
     * FUNCTION NAME
    hmHMU_stripcntl()
    
    FUNCTION DESCRIPTION
    This routine will take the input METAR report and search it for control
    characters. If there is a control character in the report it is removed.
    The function will then return the string with all control characters
    removed.
    
    PARAMETERS
    Type    Name    I/O Description
    char    String  Both    A pointer to a character string the contains
                the current METAR message.
    RETURNS
    The input string with all control characters removed.
    
    ERRORS REPORTED
    Returns and error if it can not allocate memory for the copy string.
    
    HISTORY
    04/18/96    Douglas Rankin  Inital Coding
    04/30/96    Douglas Rankin  Added error logging
        07/23/96    Douglas Rankin  Added the ability to remove all
                    punctuation marks except the slash
        03/16/98        Bryon Lawrence  Changed the name of this routine from
                                        stripcntl to hmHMU_stripcntl.
        06/1999         Bill Mattison   Changed hardwired severity level to
                                        pre-defined constant in call to
                                        hmHMU_logError.
        09/30/04        Jerry Wiedenfeld Added check for NULL values.
        10/04/04        J Moeller       Fixed NULL value check
     * </pre>
     * 
     * @param report
     * @return
     */
    protected static String stripControlAndPunctuation(String report) {
        StringBuilder filteredReportText = new StringBuilder();

        for (int i = 0; i < report.length(); i++) {
            char c = report.charAt(i);

            if ((!isControlCharacter(c)) && ((!isPunctuationCharacter(c))
                    || (c == '/') || (c == '$') || (c == '+') || (c == '-'))) {
                filteredReportText.append(c);
            }
        }

        return filteredReportText.toString();
    }

    /**
     * Based off C++ #iscntrl method as used by hmHMU_stripcntl.c. Based off
     * table in http://en.cppreference.com/w/cpp/string/byte/iscntrl.
     * 
     * @param c
     * @return
     */
    protected static boolean isControlCharacter(char c) {
        if ((c <= 31) || (c == 127)) {
            return true;
        }

        return false;
    }

    /**
     * Based off C++ #ispunct method as used by hmHMU_stripcntrl.c. Based off
     * table in http://en.cppreference.com/w/cpp/string/byte/iscntrl.
     * 
     * @param c
     * @return
     */
    protected static boolean isPunctuationCharacter(char c) {
        if ((c >= 33) && (c <= 47)) {
            return true;
        } else if ((c >= 58) && (c <= 64)) {
            return true;
        } else if ((c >= 91) && (c <= 96)) {
            return true;
        } else if ((c >= 123) && (c <= 126)) {
            return true;
        }

        return false;
    }

    /**
     * @param direction
     * @return true if the given word is a valid direction, or valid combination
     *         of cardinal directions.
     */
    protected static boolean validDir(String direction) {
        if (VALID_DIR_SYMBOLS.contains(direction)) {
            // a valid direction code on its own
            return true;
        }

        String[] directions = direction.split(DIR_SEPARATOR);
        for (String curr : directions) {
            if (!VALID_CARDINAL_DIR.contains(curr)) {
                // some non-cardinal direction
                return false;
            }
        }
        // some combination of cardinal directions
        return true;
    }

    /**
     * 
     * @param dissipation
     * @return true if the given word is a valid dissipation flag.
     */
    protected static boolean validDissipation(String dissipation) {
        /*
         * Wrapping a single field's #contains since other related flags may fit
         * into this check later.
         */
        if (VALID_DISS_SYMBOLS.contains(dissipation)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * From hmPED_insert_data.c#hmPED_insert_surfaceData.
     * 
     * <pre>
     * FUNCTION DESCRIPTION
     This routine will insert all the necessary surface data into the
     Surface_Obs structure.
    
    PARAMETERS
    Type        Name    I/O     Description
        Decoded_METAR   *Mptr   Input   A pointer to the decoded data
    Report          rpt_ptr Input   A pointer to the current report.
    Surface_Obs sfc_ptr Both    A pointer to the surface obs structure
     * </pre>
     * 
     * @param decodedMetar
     * @param report
     * @return
     */
    public static SurfaceObs insertSurfaceData(DecodedMetar decodedMetar,
            ClimateReport report) {
        SurfaceObs surfaceObs = new SurfaceObs();

        /* location id */
        surfaceObs.setObsLocID(report.getIcao_loc_id());

        /* location name */
        if (report.getName() != null) {
            surfaceObs.setLocName(report.getName());
        }

        /* station priority */
        if (report.getPrior() != null) {
            surfaceObs.setPriority(report.getPrior());
        }

        /* WMO data designator */
        surfaceObs.setWmoDD(report.getWmo_dd());

        /* AFOS data designator */
        surfaceObs.setAfosDD(report.getAfos_dd());

        /* observation time */
        Calendar obCal = TimeUtil.newCalendar();
        obCal.setTime(report.getDate().getTime());
        obCal.set(Calendar.DATE, decodedMetar.getCmnData().getObDay());
        obCal.set(Calendar.HOUR_OF_DAY, decodedMetar.getCmnData().getObHour());
        obCal.set(Calendar.MINUTE, decodedMetar.getCmnData().getObMinute());
        surfaceObs.setObsTime(obCal.getTimeInMillis());

        /*
         * nominalize date, zero out minutes and seconds. If minutes > 44, go to
         * next hour.
         */
        if (decodedMetar.getCmnData().getObMinute() > 44) {
            obCal.add(Calendar.HOUR_OF_DAY, 1);
        }
        obCal.set(Calendar.MINUTE, 0);
        obCal.set(Calendar.SECOND, 0);
        surfaceObs.setNominalTime(obCal.getTimeInMillis());

        /* origin time */
        surfaceObs.setOriginTime(report.getOrigin().getTimeInMillis());

        /* station latitude */
        if (report.getLat() != null) {
            surfaceObs.setLat(report.getLat().floatValue());
        }

        /* station longitude */
        if (report.getLon() != null) {
            surfaceObs.setLon(report.getLon().floatValue());
        }

        /* station elevation */
        if (report.getElev() != null) {
            surfaceObs.setElevation(report.getElev().floatValue());
        }

        /* station state location */
        if (report.getState() != null) {
            surfaceObs.setState(report.getState());
        }

        /* station region */
        if (report.getCountry() != null) {
            surfaceObs.setRegion(report.getCountry());
        }

        /* Copy the decode status */
        surfaceObs.setDecodeStatus(decodedMetar.getCmnData().getDecodeStatus());

        /* Is this report a special report? */
        if (report.getReport_type().equals("SPECI")) {
            surfaceObs.setSpeciFlag(1);
        } else if (report.getReport_type().equals("METAR")) {
            surfaceObs.setSpeciFlag(0);
        } else {
            logger.warn("Unexpected report type: [" + report.getReport_type()
                    + "].");
            surfaceObs.setSpeciFlag(Integer.MAX_VALUE);
        }

        /* type of station */
        if (decodedMetar.getAutoIndicator().equals(A01_INDICATOR_STRING)
                || decodedMetar.getAutoIndicator()
                        .equals(A01_INDICATOR_STRING_NUM)) {
            surfaceObs.setAutoFlag(1);
        } else if (decodedMetar.getAutoIndicator().equals(A02_INDICATOR_STRING)
                || decodedMetar.getAutoIndicator()
                        .equals(A02_INDICATOR_STRING_NUM)) {
            surfaceObs.setAutoFlag(2);
        } else {
            surfaceObs.setAutoFlag(0);
        }

        /* Is this report automated, corrected or neither? */
        if (decodedMetar.isAuto()) {
            surfaceObs.setCorFlag(2);
        } else if (decodedMetar.isCor()) {
            surfaceObs.setCorFlag(1);
        } else {
            surfaceObs.setCorFlag(0);
        }

        /* count the number of cloud layers reported */
        int numCloudLayers = 0;
        for (CloudConditions cloudConditions : decodedMetar.getCmnData()
                .getCloudConditions()) {
            if (!cloudConditions.getCloudType().isEmpty()) {
                numCloudLayers++;
            } else {
                break;
            }
        }

        /* cloud data */
        /* Initialize the cloud data */
        surfaceObs.setLowCloudHeight(MISSING_DATA);
        surfaceObs.setLowCloudCover(MISSING_DATA);
        surfaceObs.setLowCloudType(MISSING_DATA);
        surfaceObs.setMidCloudHeight(MISSING_DATA);
        surfaceObs.setMidCloudCover(MISSING_DATA);
        surfaceObs.setMidCloudType(MISSING_DATA);
        surfaceObs.setHighCloudHeight(MISSING_DATA);
        surfaceObs.setHighCloudCover(MISSING_DATA);
        surfaceObs.setHighCloudType(MISSING_DATA);
        surfaceObs.setLayer4CloudHeight(MISSING_DATA);
        surfaceObs.setLayer4CloudCover(MISSING_DATA);
        surfaceObs.setLayer4CloudType(MISSING_DATA);
        surfaceObs.setLayer5CloudHeight(MISSING_DATA);
        surfaceObs.setLayer5CloudCover(MISSING_DATA);
        surfaceObs.setLayer5CloudType(MISSING_DATA);
        surfaceObs.setLayer6CloudHeight(MISSING_DATA);
        surfaceObs.setLayer6CloudCover(MISSING_DATA);
        surfaceObs.setLayer6CloudType(MISSING_DATA);

        /*
         * only do this if there is data to process. Check to make sure there is
         * cloud data
         */
        if (numCloudLayers > 0) {
            if (decodedMetar.getCmnData().getCloudConditions()[0].getCloudType()
                    .equals(CLEAR_SKY_STRING)
                    || decodedMetar.getCmnData().getCloudConditions()[0]
                            .getCloudType().equals(SKY_CLEAR_STRING)) {
                /*
                 * check for CLR or SKC. If present then set cloud_cover to
                 * zero.
                 */
                surfaceObs.setLowCloudCover(CloudConditions.CLR_COVER);
                surfaceObs.setMidCloudCover(CloudConditions.CLR_COVER);
                surfaceObs.setHighCloudCover(CloudConditions.CLR_COVER);
                surfaceObs.setLayer4CloudCover(CloudConditions.CLR_COVER);
                surfaceObs.setLayer5CloudCover(CloudConditions.CLR_COVER);
                surfaceObs.setLayer6CloudCover(CloudConditions.CLR_COVER);
            } else if (decodedMetar.getCmnData().getCloudConditions()[0]
                    .getCloudType().equals(OVERCAST_CLOUDS_STRING)) {
                /* check for OVC in first two layers */
                surfaceObs.setLowCloudHeight(Float.parseFloat(
                        decodedMetar.getCmnData().getCloudConditions()[0]
                                .getCloudHgtChar()));
                surfaceObs.setLowCloudCover(CloudConditions.OVC_COVER);

                /*
                 * check for other cloud types. check for TCU or CB
                 */
                if (decodedMetar.getCmnData().getCloudConditions()[0]
                        .getOtherCldPhenom().equals("CB")) {
                    surfaceObs.setLowCloudType(CloudConditions.CB_CLOUD_TYPE);
                } else if (decodedMetar.getCmnData().getCloudConditions()[0]
                        .getOtherCldPhenom().equals("TCU")) {
                    surfaceObs.setLowCloudType(CloudConditions.TCU_CLOUD_TYPE);
                }
            } else if (decodedMetar.getCmnData().getCloudConditions()[1]
                    .getCloudType().equals(OVERCAST_CLOUDS_STRING)) {
                surfaceObs.setLowCloudHeight(Float.parseFloat(
                        decodedMetar.getCmnData().getCloudConditions()[0]
                                .getCloudHgtChar()));

                /* determine the cloud cover */
                if (decodedMetar.getCmnData().getCloudConditions()[0]
                        .matchedType(FEW_CLOUDS_STRING)) {
                    surfaceObs.setLowCloudCover(CloudConditions.FEW_COVER);
                } else if (decodedMetar.getCmnData().getCloudConditions()[0]
                        .matchedType(SCATTERED_CLOUDS_STRING)) {
                    surfaceObs.setLowCloudCover(CloudConditions.SCT_COVER);
                } else {
                    surfaceObs.setLowCloudCover(CloudConditions.BKN_COVER);
                }

                /*
                 * check for other cloud types. check for TCU or CB
                 */
                if (decodedMetar.getCmnData().getCloudConditions()[0]
                        .getOtherCldPhenom().equals("CB")) {
                    surfaceObs.setLowCloudType(CloudConditions.CB_CLOUD_TYPE);
                } else if (decodedMetar.getCmnData().getCloudConditions()[0]
                        .getOtherCldPhenom().equals("TCU")) {
                    surfaceObs.setLowCloudType(CloudConditions.TCU_CLOUD_TYPE);
                }

                surfaceObs.setMidCloudHeight(Float.parseFloat(
                        decodedMetar.getCmnData().getCloudConditions()[1]
                                .getCloudHgtChar()));
                surfaceObs.setMidCloudCover(CloudConditions.OVC_COVER);

                /*
                 * Legacy duplicated code, checking the 0th index again. Assumed
                 * to actually want to check the 1st index.
                 * 
                 * check for other cloud types. check for TCU or CB
                 */
                if (decodedMetar.getCmnData().getCloudConditions()[1]
                        .getOtherCldPhenom().equals("CB")) {
                    surfaceObs.setMidCloudType(CloudConditions.CB_CLOUD_TYPE);
                } else if (decodedMetar.getCmnData().getCloudConditions()[1]
                        .getOtherCldPhenom().equals("TCU")) {
                    surfaceObs.setMidCloudType(CloudConditions.TCU_CLOUD_TYPE);
                }
            } else {
                /* go through layers one-by-one */
                switch (numCloudLayers) {
                case 6:
                    surfaceObs.setLayer6CloudHeight(Float.parseFloat(
                            decodedMetar.getCmnData().getCloudConditions()[5]
                                    .getCloudHgtChar()));
                    /* determine the cloud cover */
                    surfaceObs.setLayer6CloudCover(getCloudCoverFromCloudType(
                            decodedMetar.getCmnData().getCloudConditions()[5]
                                    .getCloudType()));

                    /* check for other cloud types */
                    /* check for TCU or CB */
                    if (decodedMetar.getCmnData().getCloudConditions()[5]
                            .getOtherCldPhenom().equals("CB")) {
                        surfaceObs.setLayer6CloudType(
                                CloudConditions.CB_CLOUD_TYPE);
                    } else if (decodedMetar.getCmnData().getCloudConditions()[5]
                            .getOtherCldPhenom().equals("TCU")) {
                        surfaceObs.setLayer6CloudType(
                                CloudConditions.TCU_CLOUD_TYPE);
                    }
                case 5:
                    surfaceObs.setLayer5CloudHeight(Float.parseFloat(
                            decodedMetar.getCmnData().getCloudConditions()[4]
                                    .getCloudHgtChar()));
                    /* determine the cloud cover */
                    surfaceObs.setLayer5CloudCover(getCloudCoverFromCloudType(
                            decodedMetar.getCmnData().getCloudConditions()[4]
                                    .getCloudType()));

                    /* check for other cloud types */
                    /* check for TCU or CB */
                    if (decodedMetar.getCmnData().getCloudConditions()[4]
                            .getOtherCldPhenom().equals("CB")) {
                        surfaceObs.setLayer5CloudType(
                                CloudConditions.CB_CLOUD_TYPE);
                    } else if (decodedMetar.getCmnData().getCloudConditions()[4]
                            .getOtherCldPhenom().equals("TCU")) {
                        surfaceObs.setLayer5CloudType(
                                CloudConditions.TCU_CLOUD_TYPE);
                    }
                case 4:
                    surfaceObs.setLayer4CloudHeight(Float.parseFloat(
                            decodedMetar.getCmnData().getCloudConditions()[3]
                                    .getCloudHgtChar()));
                    /* determine the cloud cover */
                    surfaceObs.setLayer4CloudCover(getCloudCoverFromCloudType(
                            decodedMetar.getCmnData().getCloudConditions()[3]
                                    .getCloudType()));

                    /* check for other cloud types */
                    /* check for TCU or CB */
                    if (decodedMetar.getCmnData().getCloudConditions()[3]
                            .getOtherCldPhenom().equals("CB")) {
                        surfaceObs.setLayer4CloudType(
                                CloudConditions.CB_CLOUD_TYPE);
                    } else if (decodedMetar.getCmnData().getCloudConditions()[3]
                            .getOtherCldPhenom().equals("TCU")) {
                        surfaceObs.setLayer4CloudType(
                                CloudConditions.TCU_CLOUD_TYPE);
                    }
                case 3:
                    surfaceObs.setHighCloudHeight(Float.parseFloat(
                            decodedMetar.getCmnData().getCloudConditions()[2]
                                    .getCloudHgtChar()));
                    /* determine the cloud cover */
                    surfaceObs.setHighCloudCover(getCloudCoverFromCloudType(
                            decodedMetar.getCmnData().getCloudConditions()[2]
                                    .getCloudType()));

                    /* check for other cloud types */
                    /* check for TCU or CB */
                    if (decodedMetar.getCmnData().getCloudConditions()[2]
                            .getOtherCldPhenom().equals("CB")) {
                        surfaceObs.setHighCloudType(
                                CloudConditions.CB_CLOUD_TYPE);
                    } else if (decodedMetar.getCmnData().getCloudConditions()[2]
                            .getOtherCldPhenom().equals("TCU")) {
                        surfaceObs.setHighCloudType(
                                CloudConditions.TCU_CLOUD_TYPE);
                    }
                case 2:
                    surfaceObs.setMidCloudHeight(Float.parseFloat(
                            decodedMetar.getCmnData().getCloudConditions()[1]
                                    .getCloudHgtChar()));
                    /* determine the cloud cover */
                    surfaceObs.setMidCloudCover(getCloudCoverFromCloudType(
                            decodedMetar.getCmnData().getCloudConditions()[1]
                                    .getCloudType()));

                    /* check for other cloud types */
                    /* check for TCU or CB */
                    if (decodedMetar.getCmnData().getCloudConditions()[1]
                            .getOtherCldPhenom().equals("CB")) {
                        surfaceObs
                                .setMidCloudType(CloudConditions.CB_CLOUD_TYPE);
                    } else if (decodedMetar.getCmnData().getCloudConditions()[1]
                            .getOtherCldPhenom().equals("TCU")) {
                        surfaceObs.setMidCloudType(
                                CloudConditions.TCU_CLOUD_TYPE);
                    }
                case 1:
                    surfaceObs.setLowCloudHeight(Float.parseFloat(
                            decodedMetar.getCmnData().getCloudConditions()[0]
                                    .getCloudHgtChar()));
                    /* determine the cloud cover */
                    surfaceObs.setLowCloudCover(getCloudCoverFromCloudType(
                            decodedMetar.getCmnData().getCloudConditions()[0]
                                    .getCloudType()));

                    /* check for other cloud types */
                    /* check for TCU or CB */
                    if (decodedMetar.getCmnData().getCloudConditions()[0]
                            .getOtherCldPhenom().equals("CB")) {
                        surfaceObs
                                .setLowCloudType(CloudConditions.CB_CLOUD_TYPE);
                    } else if (decodedMetar.getCmnData().getCloudConditions()[0]
                            .getOtherCldPhenom().equals("TCU")) {
                        surfaceObs.setLowCloudType(
                                CloudConditions.TCU_CLOUD_TYPE);
                    }
                case 0:
                    logger.debug(numCloudLayers + " cloud layers reported.");
                    break;
                default:
                    logger.warn("Unhandled number of cloud layers: ["
                            + numCloudLayers + "] were reported.");
                }
            }
        }

        /* weather flag. Not used in METAR. Set to Missing */
        surfaceObs.setWxflag(MISSING_DATA);

        /*
         * past weather group. This is not reported in METAR so set to an empty
         * string
         */
        for (int i = 0; i < surfaceObs.getPastWx().length; i++) {
            surfaceObs.getPastWx()[i] = "";
        }

        /* present weather */
        for (int i = 0; (i < surfaceObs.getPresentWx().length)
                && (i < decodedMetar.getCmnData().getWxObstruct().length)
                && (!decodedMetar.getCmnData().getWxObstruct()[i]
                        .isEmpty()); i++) {
            surfaceObs.getPresentWx()[i] = decodedMetar.getCmnData()
                    .getWxObstruct()[i];
        }

        /* horizontal visibility */
        if (decodedMetar.getCmnData()
                .getPrevailingVisibilitySM() != (float) Integer.MAX_VALUE) {
            surfaceObs.setVisibility(
                    decodedMetar.getCmnData().getPrevailingVisibilitySM());
        } else {
            surfaceObs.setVisibility(MISSING_DATA);
        }

        /* vertical visibility. if reported */
        if (decodedMetar.getCmnData()
                .getVerticalVisibility() != Integer.MAX_VALUE) {
            surfaceObs.setVerticalVisibility(
                    (float) decodedMetar.getCmnData().getVerticalVisibility());
        } else {
            surfaceObs.setVerticalVisibility(MISSING_DATA);
        }

        /* station pressure */
        if (decodedMetar.isAltimeterSet()) {
            surfaceObs.setAltSetting((float) decodedMetar.getInchesAltstng());
        } else {
            surfaceObs.setAltSetting(MISSING_DATA);
        }

        /* aurora borealis */
        surfaceObs
                .setAuroraBorealisFlag(decodedMetar.isAuroraBorealis() ? 1 : 0);

        /* sea level pressure */
        if (decodedMetar.getSlp() != (float) Integer.MAX_VALUE) {
            surfaceObs.setSlp(decodedMetar.getSlp());
        } else {
            surfaceObs.setSlp(MISSING_DATA);
        }

        /* 3 hour pressure change */
        if (decodedMetar
                .getPressure3HourTendency() != (float) Integer.MAX_VALUE) {
            surfaceObs.setPressureChange3hr(
                    decodedMetar.getPressure3HourTendency());
        } else {
            surfaceObs.setPressureChange3hr(MISSING_DATA);
        }

        /* pressure tendency */
        if (decodedMetar.getCharPressureTendency() != Integer.MAX_VALUE) {
            surfaceObs.setPressureTendency(
                    decodedMetar.getCharPressureTendency());
        } else {
            surfaceObs.setPressureTendency(MISSING_DATA);
        }

        /* temperature */
        if (decodedMetar.getTemp() != Integer.MAX_VALUE) {
            surfaceObs.setTemp(decodedMetar.getTemp());
        } else {
            surfaceObs.setTemp(MISSING_DATA);
        }

        /* dew point temperature */
        if (decodedMetar.getDewPtTemp() != Integer.MAX_VALUE) {
            surfaceObs.setDewPt(decodedMetar.getDewPtTemp());
        } else {
            surfaceObs.setDewPt(MISSING_DATA);
        }

        /* temperature to the nearest tenth of a degree C */
        if (decodedMetar.getTempToTenths() != (float) Integer.MAX_VALUE) {
            surfaceObs.setTemp2Tenths(decodedMetar.getTempToTenths());
        } else {
            surfaceObs.setTemp2Tenths(MISSING_DATA);
        }

        /* dew point temperature to the nearest tenth of a degree C */
        if (decodedMetar
                .getDewpointTempToTenths() != (float) Integer.MAX_VALUE) {
            surfaceObs.setDewPt2Tenths(decodedMetar.getDewpointTempToTenths());
        } else {
            surfaceObs.setDewPt2Tenths(MISSING_DATA);
        }

        /* max 6 hour temp */
        if (decodedMetar.getMaxTemp() != (float) Integer.MAX_VALUE) {
            surfaceObs.setMaxTemp6hr(decodedMetar.getMaxTemp());
        } else {
            surfaceObs.setMaxTemp6hr(MISSING_DATA);
        }

        /* min 6 hour temp */
        if (decodedMetar.getMinTemp() != (float) Integer.MAX_VALUE) {
            surfaceObs.setMinTemp6hr(decodedMetar.getMinTemp());
        } else {
            surfaceObs.setMinTemp6hr(MISSING_DATA);
        }

        /* max 24 hour temp */
        if (decodedMetar.getMax24Temp() != (float) Integer.MAX_VALUE) {
            surfaceObs.setMax24temp(decodedMetar.getMax24Temp());
        } else {
            surfaceObs.setMax24temp(MISSING_DATA);
        }

        /* min 24 hour temp */
        if (decodedMetar.getMin24Temp() != (float) Integer.MAX_VALUE) {
            surfaceObs.setMin24temp(decodedMetar.getMin24Temp());
        } else {
            surfaceObs.setMin24temp(MISSING_DATA);
        }

        /* precip flag */
        surfaceObs.setPrecipPresent(decodedMetar.isNoRain() ? 0 : 1);

        /* hourly precip */
        if (decodedMetar.getHourlyPrecip() != (float) Integer.MAX_VALUE) {
            surfaceObs.setPrecip1hr(decodedMetar.getHourlyPrecip());
        } else {
            surfaceObs.setPrecip1hr(MISSING_DATA);
        }

        /* 6 hour precip */
        if (decodedMetar.getPrecipAmt() != (float) Integer.MAX_VALUE) {
            surfaceObs.setPrecip6hr(decodedMetar.getPrecipAmt());
        } else {
            surfaceObs.setPrecip6hr(MISSING_DATA);
        }

        /* 24 hour precip */
        if (decodedMetar.getPrecip24Amt() != (float) Integer.MAX_VALUE) {
            surfaceObs.setPrecip24hr(decodedMetar.getPrecip24Amt());
        } else {
            surfaceObs.setPrecip24hr(MISSING_DATA);
        }

        /* snow depth */
        if (decodedMetar.getSnowDepth() != Integer.MAX_VALUE) {
            surfaceObs.setSnowDepth(decodedMetar.getSnowDepth());
        } else {
            surfaceObs.setSnowDepth(MISSING_DATA);
        }

        /* wind direction */
        if (decodedMetar.getCmnData().getWinData()
                .getWindDir() != Integer.MAX_VALUE) {
            surfaceObs.setWindDir(
                    decodedMetar.getCmnData().getWinData().getWindDir());
        } else {
            surfaceObs.setWindDir(MISSING_DATA);
        }

        /* Was the wind light (<= 6kts) and variable? */
        surfaceObs.setVariableWindFlag(
                decodedMetar.getCmnData().getWinData().isWindVrb() ? 1 : 0);

        /* wind speed */
        if (decodedMetar.getCmnData().getWinData()
                .getWindSpeed() != Integer.MAX_VALUE) {
            /* check the units of wind speed. IF MPS or KMH convert to knots */
            surfaceObs.setWindSpd(
                    decodedMetar.getCmnData().getWinData().getWindSpeed()
                            * getToKnotsMultiplier(decodedMetar.getCmnData()
                                    .getWinData().getWindUnits()));
        } else {
            surfaceObs.setWindSpd(MISSING_DATA);
        }

        /* gust speed */
        if (decodedMetar.getCmnData().getWinData()
                .getWindGust() != Integer.MAX_VALUE) {
            /*
             * a legacy bug would convert wind speed, not gust speed, when units
             * were KMH.
             */
            surfaceObs.setGustSpd(
                    decodedMetar.getCmnData().getWinData().getWindGust()
                            * getToKnotsMultiplier(decodedMetar.getCmnData()
                                    .getWinData().getWindUnits()));
        } else {
            surfaceObs.setGustSpd(MISSING_DATA);
        }

        /* Process the peak wind speed. */
        if (decodedMetar.getPkWndSpeed() != Integer.MAX_VALUE) {
            surfaceObs.setPeakWindSpeed(decodedMetar.getPkWndSpeed()
                    * getToKnotsMultiplier(decodedMetar.getCmnData()
                            .getWinData().getWindUnits()));
        } else {
            surfaceObs.setPeakWindSpeed(MISSING_DATA);
        }

        /* Process the peak wind direction. */
        if (decodedMetar.getPkWndDir() != Integer.MAX_VALUE) {
            surfaceObs.setPeakWindDir(decodedMetar.getPkWndDir());
        } else {
            surfaceObs.setPeakWindDir(MISSING_DATA);
        }

        /* Process the peak wind hour and minute. */
        if ((decodedMetar.getPkWndHour() != Integer.MAX_VALUE)
                && (decodedMetar.getPkWndMinute() != Integer.MAX_VALUE)) {
            surfaceObs.setPeakWindHHMM((decodedMetar.getPkWndHour() * 100)
                    + decodedMetar.getPkWndMinute());
        } else {
            surfaceObs.setPeakWindHHMM(MISSING_DATA);
        }

        /* Process the sunshine duration. */
        if (decodedMetar.getSunshineDur() != Integer.MAX_VALUE) {
            surfaceObs.setSunshineDur(decodedMetar.getSunshineDur());
        } else {
            surfaceObs.setSunshineDur(MISSING_DATA);
        }

        /* Process the funnel cloud, tornado, waterspout information. */
        if (decodedMetar.getTornadicType().startsWith("T")) {
            surfaceObs.setTornadic(TORNADO_VALUE);
        } else if (decodedMetar.getTornadicType().startsWith("W")) {
            surfaceObs.setTornadic(WATER_SPOUT_VALUE);
        } else if (decodedMetar.getTornadicType().startsWith("F")) {
            surfaceObs.setTornadic(FUNNEL_CLOUD_VALUE);
        } else {
            surfaceObs.setTornadic(0);
        }

        /*
         * Process the recent weather information which will include weather
         * types and the begin and end times of the precipitation. Make certain
         * that the weather strings in each of the Recent_Wx structures contains
         * an empty string to begin with.
         */
        for (int i = 0; i < surfaceObs.getWeatherBeginEnd().length; i++) {
            RecentWx recentWx = new RecentWx();
            recentWx.setRecentWeatherName("");
            recentWx.setBeginHour(MISSING_DATA);
            recentWx.setBeginMinute(MISSING_DATA);
            recentWx.setEndHour(MISSING_DATA);
            recentWx.setEndMinute(MISSING_DATA);

            surfaceObs.getWeatherBeginEnd()[i] = recentWx;
        }

        /* Now copy the recent weather data from the Decoded_METAR structure. */
        for (int i = 0; (i < surfaceObs.getWeatherBeginEnd().length)
                && (i < decodedMetar.getRecentWeathers().length)
                && (!decodedMetar.getRecentWeathers()[i].getRecentWeatherName()
                        .isEmpty()); i++) {
            RecentWx sfcReWx = surfaceObs.getWeatherBeginEnd()[i];
            RecentWx metarReWx = decodedMetar.getRecentWeathers()[i];

            sfcReWx.setRecentWeatherName(metarReWx.getRecentWeatherName());
            if (metarReWx.getBeginHour() != Integer.MAX_VALUE) {
                sfcReWx.setBeginHour(metarReWx.getBeginHour());
            }
            if (metarReWx.getBeginMinute() != Integer.MAX_VALUE) {
                sfcReWx.setBeginMinute(metarReWx.getBeginMinute());
            }
            if (metarReWx.getEndHour() != Integer.MAX_VALUE) {
                sfcReWx.setEndHour(metarReWx.getEndHour());
            }
            if (metarReWx.getEndMinute() != Integer.MAX_VALUE) {
                sfcReWx.setEndMinute(metarReWx.getEndMinute());
            }
        }

        surfaceObs.setQcMetar(new QCMetar());

        return surfaceObs;
    }

    /**
     * From check_METAR_quality.c#check_METAR_quality.
     * 
     * <pre>
     * MODULE NUMBER: 1
    * MODULE NAME:   check_METAR_quality
    * PURPOSE:       Given an HM_DATA structure containing decoded METAR data,
    *                this routine will set the corresponding
    *                quality control flags in a QC_METAR
    *                structure to the appropriate values based upon whether the
    *                METAR elements have valid or invalid values. If a linked list
    *                HM_DATA structures is passed to this routine, then this 
    *                routine will build a linked list of QC_METAR structures that
    *                has a one to one mapping with the HM_DATA linked list.
    *                
    *                It is the responsibility of the user to open and close
    *                the connection with the verification database.
    *
    *                It is the responsibility of the user to free up the memory
    *                used by the dynamically created link list of  QC_METAR 
    *                structures.
    *
    *                It is the responsibility of the user to ensure that 
    *                *qc_list_Ptr is set to NULL before being passed to this 
    *                routine.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      HM_DATA*    metar_list_Ptr       A pointer to a linked list of
    *                                           HM_DATA structures.
    *   O      QC_METAR**  qc_list_Ptr          A pointer to a pointer to a 
    *                                           linked list of QC_METAR 
    *                                           structures.
    *   I/O    int*        source_status        A pointer to the value of the 
    *                                           source status flag.
    *
    * RETURNS:
    *   DATA TYPE   NAME                        DESCRIPTION
    *   STATUS      status                      Contains the exit status of this
    *                                           routine. Contains any exit
    *                                           codes that might point to sources
    *                                           of error.
    *
    * APIs UTILIZED:
    *   NAME                      HEADER FILE          DESCRIPTION
    *   check_contin_element      QC_METAR.h           Checks validity of 
    *                                                  a continuous METAR
    *                                                  element.
    *   check_discrete_element    QC_METAR.h           Checks validity of 
    *                                                  a discrete METAR element.
    *   build_key_tree            dbMETAR.h            Builds an array of binary
    *                                                  search trees containing
    *                                                  search values for the 
    *                                                  discrete METAR elements.
    *   build_maxmin_tree         dbMETAR.h            Builds the search tree
    *                                                  used to determine if the
    *                                                  continuous METAR elements
    *                                                  are valid.
    *   free_key_tree             dbMETAR.h            Frees the memory used by
    *                                                  the array of binary search
    *                                                  trees created by 
    *                                                  build_key_tree.
    *   free_tree                 treeUtil.h           Frees the memory used by 
    *                                                  the maxmin binary search
    *                                                  created by build_maxmin_tree.
     * </pre>
     * 
     * @param surfaceObs
     *            metar values to check the validity of
     */
    public static void checkMetarQuality(SurfaceObs surfaceObs) {
        QCMetar qcMetar = surfaceObs.getQcMetar();

        /*
         * Clouds
         */
        qcMetar.setLowCloudHgtDqd(checkDiscreteElement(
                surfaceObs.getLowCloudHeight(), MetarUtils.METAR_CLOUD_HEIGHT));

        qcMetar.setLowCloudCoverDqd(
                checkDiscreteElement(surfaceObs.getLowCloudCover(),
                        MetarDecoderUtil.METAR_CLOUD_COVER_SCALE,
                        MetarUtils.METAR_CLOUD_COVER));

        qcMetar.setLowCloudTypeDqd(checkDiscreteElement(
                surfaceObs.getLowCloudType(), MetarUtils.METAR_CLOUD_TYPE));

        qcMetar.setMidCloudHgtDqd(checkDiscreteElement(
                surfaceObs.getMidCloudHeight(), MetarUtils.METAR_CLOUD_HEIGHT));

        qcMetar.setMidCloudCoverDqd(
                checkDiscreteElement(surfaceObs.getMidCloudCover(),
                        MetarDecoderUtil.METAR_CLOUD_COVER_SCALE,
                        MetarUtils.METAR_CLOUD_COVER));

        qcMetar.setMidCloudTypeDqd(checkDiscreteElement(
                surfaceObs.getMidCloudType(), MetarUtils.METAR_CLOUD_TYPE));

        qcMetar.setHighCloudHgtDqd(
                checkDiscreteElement(surfaceObs.getHighCloudHeight(),
                        MetarUtils.METAR_CLOUD_HEIGHT));

        qcMetar.setHighCloudCoverDqd(
                checkDiscreteElement(surfaceObs.getHighCloudCover(),
                        MetarDecoderUtil.METAR_CLOUD_COVER_SCALE,
                        MetarUtils.METAR_CLOUD_COVER));

        qcMetar.setHighCloudTypeDqd(checkDiscreteElement(
                surfaceObs.getHighCloudType(), MetarUtils.METAR_CLOUD_TYPE));

        qcMetar.setLayer4CloudHgtDqd(
                checkDiscreteElement(surfaceObs.getLayer4CloudHeight(),
                        MetarUtils.METAR_CLOUD_HEIGHT));

        qcMetar.setLayer4CloudCoverDqd(
                checkDiscreteElement(surfaceObs.getLayer4CloudCover(),
                        MetarDecoderUtil.METAR_CLOUD_COVER_SCALE,
                        MetarUtils.METAR_CLOUD_COVER));

        qcMetar.setLayer4CloudTypeDqd(checkDiscreteElement(
                surfaceObs.getLayer4CloudType(), MetarUtils.METAR_CLOUD_TYPE));

        qcMetar.setLayer5CloudHgtDqd(
                checkDiscreteElement(surfaceObs.getLayer5CloudHeight(),
                        MetarUtils.METAR_CLOUD_HEIGHT));

        qcMetar.setLayer5CloudCoverDqd(
                checkDiscreteElement(surfaceObs.getLayer5CloudCover(),
                        MetarDecoderUtil.METAR_CLOUD_COVER_SCALE,
                        MetarUtils.METAR_CLOUD_COVER));

        qcMetar.setLayer5CloudTypeDqd(checkDiscreteElement(
                surfaceObs.getLayer5CloudType(), MetarUtils.METAR_CLOUD_TYPE));

        qcMetar.setLayer6CloudHgtDqd(
                checkDiscreteElement(surfaceObs.getLayer6CloudHeight(),
                        MetarUtils.METAR_CLOUD_HEIGHT));

        qcMetar.setLayer6CloudCoverDqd(
                checkDiscreteElement(surfaceObs.getLayer6CloudCover(),
                        MetarDecoderUtil.METAR_CLOUD_COVER_SCALE,
                        MetarUtils.METAR_CLOUD_COVER));

        qcMetar.setLayer6CloudTypeDqd(checkDiscreteElement(
                surfaceObs.getLayer6CloudType(), MetarUtils.METAR_CLOUD_TYPE));
                /*
                 * end clouds
                 */

        /* Check the validity of the horizontal visibility. */
        qcMetar.setVsbyDqd(checkDiscreteElement(surfaceObs.getVisibility(),
                MetarDecoderUtil.METAR_HORIZ_VISIB_VALIDITY_SCALE,
                MetarUtils.METAR_VISIB));

        /* Check the validity of the vertical visibility. */
        qcMetar.setVertVsbyDqd(checkDiscreteElement(
                surfaceObs.getVerticalVisibility(),
                MetarDecoderUtil.M_TO_100S_OF_FT, MetarUtils.METAR_VERT_VISIB));

        /* Check the validity of the altimeter setting. */
        qcMetar.setAltSettingDqd(checkContinuousElement(
                MetarUtils.METAR_ALT_SETTING, surfaceObs.getAltSetting()));

        /* Check the validity of the sea level pressure. */
        qcMetar.setSLPDqd(checkContinuousElement(MetarUtils.METAR_MSL_PRESS,
                surfaceObs.getSlp()));

        /* Check the validity of the 3 hour pressure change. */
        qcMetar.setPresChg3hrDqd(
                checkContinuousElement(MetarUtils.METAR_3HR_PRESS_CHNG,
                        surfaceObs.getPressureChange3hr()));

        /* Check the validity of the 3 hour pressure tendency. */
        qcMetar.setPresTendDqd(
                checkDiscreteElement(surfaceObs.getPressureTendency(),
                        MetarUtils.METAR_3HR_PRESS_TREND));

        /* Check the validity of the temperature. */
        /*
         * validity checking already checks for missing and returns a status;
         * but Legacy does not allow a status flag if this data is missing.
         * Given all other if-else branches provide for setting the missing flag
         * (unnecessarily) on other data, it is assumed this was a copy-paste
         * bug.
         */
        qcMetar.setTempDqd(checkContinuousElement(MetarUtils.METAR_TEMP,
                surfaceObs.getTemp()));

        /* Check the validity of the dewpoint. */
        qcMetar.setDewPtDqd(checkContinuousElement(MetarUtils.METAR_DEWPOINT,
                surfaceObs.getDewPt()));

        /*
         * Check the validity of the temperature to the nearest tenth of a
         * degree C.
         */
        qcMetar.setTemp2TenthsDqd(checkContinuousElement(
                MetarUtils.METAR_TEMP_2_TENTHS, surfaceObs.getTemp2Tenths()));

        /*
         * Check the validity of the dewpoint to the nearest tenth of a degree
         * C.
         */
        qcMetar.setDewPt2TenthsDqd(
                checkContinuousElement(MetarUtils.METAR_DEWPOINT_2_TENTHS,
                        surfaceObs.getDewPt2Tenths()));

        /* Check the validity of the 6 hour maximum temperature. */
        qcMetar.setMaxTemp6hrDqd(checkContinuousElement(
                MetarUtils.METAR_6HR_MAXTEMP, surfaceObs.getMaxTemp6hr()));

        /* Check the validity of the 6 hour minimum temperature. */
        qcMetar.setMinTemp6hrDqd(checkContinuousElement(
                MetarUtils.METAR_6HR_MINTEMP, surfaceObs.getMinTemp6hr()));

        /* Check the validity of the 24 hour maximum temperature. */
        qcMetar.setMaxTemp24hrDqd(checkContinuousElement(
                MetarUtils.METAR_24HR_MAXTEMP, surfaceObs.getMax24temp()));

        /* Check the validity of the 24 hour minimum temperature. */
        qcMetar.setMinTemp24hrDqd(checkContinuousElement(
                MetarUtils.METAR_24HR_MINTEMP, surfaceObs.getMin24temp()));

        /* Check the validity of the peak wind speed. */
        qcMetar.setPeakWindSpdDqd(
                checkContinuousElement(MetarUtils.METAR_PEAK_WIND_SPEED,
                        surfaceObs.getPeakWindSpeed()));

        /* Check the validity of the peak wind direction. */
        qcMetar.setPeakWindDirDqd(checkDiscreteElement(
                surfaceObs.getPeakWindDir(), MetarUtils.METAR_PEAK_WIND_DIR));

        /* Check the validity of the peak wind time. */
        if (surfaceObs.getPeakWindHHMM() != MetarDecoderUtil.MISSING_DATA) {
            int minute = surfaceObs.getPeakWindHHMM() % 100;
            int hour = surfaceObs.getPeakWindHHMM() / 100;

            if (minute >= 0 && minute <= 59 && hour >= 0 && hour <= 23) {
                qcMetar.setPeakWindTimeDqd(QCMetar.COARSE_CHECKS_PASSED);
            } else {
                qcMetar.setPeakWindTimeDqd(QCMetar.FAILED_VALIDITY_CHECK);
            }
        } else {
            qcMetar.setPeakWindTimeDqd(QCMetar.NO_QC_PERFORMED);
        }

        /* Check the validity of the sunshine duration. */
        qcMetar.setSunshineDurDqd(
                checkContinuousElement(MetarUtils.METAR_SUNSHINE_DURATION,
                        surfaceObs.getSunshineDur()));

        /* Check the validity of the 1 hour precipitation amount. */
        qcMetar.setPrecip1hrDqd(checkContinuousElement(
                MetarUtils.METAR_1HR_PRECIP, surfaceObs.getPrecip1hr()));

        /*
         * Check the validity of the 3 hour or the 6 hour precipitation amount
         * based upon the synoptic hour.
         */
        Calendar synCal = TimeUtil.newCalendar();
        synCal.setTimeInMillis(surfaceObs.getNominalTime());
        int synopticHour = synCal.get(Calendar.HOUR_OF_DAY);

        if (synopticHour % 6 == 0) {
            qcMetar.setPrecip6hrDqd(checkContinuousElement(
                    MetarUtils.METAR_6HR_PRECIP, surfaceObs.getPrecip6hr()));
            qcMetar.setPrecip3hrDqd(QCMetar.NO_QC_PERFORMED);
        } else if (synopticHour % 3 == 0) {
            qcMetar.setPrecip3hrDqd(checkContinuousElement(
                    MetarUtils.METAR_3HR_PRECIP, surfaceObs.getPrecip6hr()));
            qcMetar.setPrecip6hrDqd(QCMetar.NO_QC_PERFORMED);
        } else {
            qcMetar.setPrecip6hrDqd(QCMetar.NO_QC_PERFORMED);
            qcMetar.setPrecip3hrDqd(QCMetar.NO_QC_PERFORMED);
        }

        /* Check the validity of the 24 hour precipitation amount. */
        qcMetar.setPrecip24hrDqd(checkContinuousElement(
                MetarUtils.METAR_24HR_PRECIP, surfaceObs.getPrecip24hr()));

        /* Check the validity of the snow depth. */
        qcMetar.setSnowDepthDqd(checkContinuousElement(
                MetarUtils.METAR_SNOW_DEPTH, surfaceObs.getSnowDepth()));

        /* Check the validity of the wind direction. */
        qcMetar.setWindDirDqd(checkDiscreteElement(surfaceObs.getWindDir(),
                MetarUtils.METAR_WIND_DIRECTION));

        /* Check the validity of the wind speed. */
        qcMetar.setWindSpdDqd(checkContinuousElement(
                MetarUtils.METAR_WIND_SPEED, surfaceObs.getWindSpd()));

        /* Check the validity of the wind gust speed. */
        qcMetar.setGustSpdDqd(checkContinuousElement(
                MetarUtils.METAR_MAX_WIND_GUST, surfaceObs.getGustSpd()));
    }

    /**
     * From check_METAR_quality.c#check_contin_element.
     * 
     * <pre>
     * MODULE NUMBER:  4
    * MODULE NAME:    check_contin_element 
    * PURPOSE:        Checks the validity of a continous METAR element. This is 
    *                 basically done by doing a range check on the value.
    *                 Examples of continuous METAR elements include temperature,
    *                 dewpoint, wind speed, precipitation amount, etc.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      TREENODE*  search_tree_Ptr       A pointer to the search tree
    *                                           that will be used to check the 
    *                                           validity of a given element.
    *   I      long       element_id            Contains the identifier of the 
    *                                           element to be validated.
    *   I      float      element_value         The value to be validated.
    *   O      char*      qc_element            The dqd flag to be set in
    *                                           accordance with the success or
    *                                           lack there of of the binary tree
    *                                           search.
     * </pre>
     * 
     * @param elementID
     * @param value
     * @return
     */
    private static String checkContinuousElement(int elementID, Number value) {
        if (value.intValue() == MetarDecoderUtil.MISSING_DATA) {
            return QCMetar.NO_QC_PERFORMED;
        } else {

            StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ")
                    .append(ClimateDAOValues.CONTIN_REAL_ELE_TABLE_NAME)
                    .append(" WHERE ");
            query.append(" element_id=").append(elementID);
            // max value may not exist
            query.append(
                    "AND ((maximum_value IS NULL) OR (maximum_value IS NOT NULL AND ")
                    .append(value.floatValue()).append(" <= maximum_value)) ");
            // min value may not exist
            query.append(
                    "AND ((minimum_value IS NULL) OR (minimum_value IS NOT NULL AND ")
                    .append(value.floatValue()).append(" >= minimum_value)) ");

            int count = ((Number) new ClimateDAO().queryForOneValue(
                    query.toString(), ParameterFormatClimate.MISSING))
                            .intValue();

            if (count == ParameterFormatClimate.MISSING) {
                logger.error(
                        "Error querying for continuous value validity with: ["
                                + query.toString() + "]");
                return QCMetar.NO_QC_PERFORMED;
            } else if (count == 0) {
                logger.warn("Invalid continuous value: [" + value.floatValue()
                        + "] for element ID: [" + elementID + "]. QC query: ["
                        + query.toString() + "].");
                return QCMetar.FAILED_VALIDITY_CHECK;
            }

            return QCMetar.COARSE_CHECKS_PASSED;
        }
    }

    /**
     * From check_METAR_quality.c#check_discrete_element.
     * 
     * <pre>
     * MODULE NUMBER:  3
    * MODULE NAME:    check_discrete_element
    * PURPOSE:        This routine checks the validity of a discrete METAR 
    *                 element. Examples of discrete METAR elements include
    *                 wind direction, cloud height, sky cover, and the 
    *                 3 hour pressure tendency.
    *
    * ARGUMENTS:
    *   TYPE   DATA TYPE   NAME                 DESCRIPTION/UNITS
    *   I      TREENODE*   search_tree_Ptr      A pointer to the tree to search 
    *                                           for the element value in.
    *   I      float       element_value        The value that needs to be 
    *                                           validated.
    *   I      float       scale_factor         The value by which the 
    *                                           element needs to be scaled so that
    *                                           it may be compared with the 
    *                                           values in the database.
    *   O      char*       qc_element           A pointer to the dqd flag that 
    *                                           corresponds to the METAR element
    *                                           currently being checked for
    *                                           quality.
     * </pre>
     * 
     * @param value
     * @return
     */
    private static String checkDiscreteElement(Number value, Number scale,
            int elementID) {
        if (value.intValue() == MetarDecoderUtil.MISSING_DATA) {
            return QCMetar.NO_QC_PERFORMED;
        } else {

            /*
             * Verify that this discrete value exists for the given ID in the
             * defined_values table.
             */
            StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ")
                    .append(ClimateDAOValues.DEFINED_VALUES_TABLE_NAME)
                    .append(" WHERE ");
            query.append(" element_id=").append(elementID);
            query.append(" AND defined_value=")
                    .append((int) (value.floatValue() * scale.floatValue()));

            int count = ((Number) new ClimateDAO().queryForOneValue(
                    query.toString(), ParameterFormatClimate.MISSING))
                            .intValue();

            if (count == ParameterFormatClimate.MISSING) {
                logger.error(
                        "Error querying for discrete value validity with: ["
                                + query.toString() + "]");
                return QCMetar.FAILED_VALIDITY_CHECK;
            } else if (count == 0) {
                logger.warn(
                        "Invalid discrete value: ["
                                + (int) (value.floatValue()
                                        * scale.floatValue())
                                + "] for element ID: [" + elementID
                                + "]. QC Query: [" + query.toString() + "].");
                return QCMetar.FAILED_VALIDITY_CHECK;
            }

            return QCMetar.COARSE_CHECKS_PASSED;
        }
    }

    /**
     * See {@link #checkDiscreteElement(Number, int, int)}. Assume scale is 1.
     * 
     * @param value
     * @param elementID
     * @return
     */
    private static String checkDiscreteElement(Number value, int elementID) {
        return checkDiscreteElement(value, 1, elementID);
    }
}

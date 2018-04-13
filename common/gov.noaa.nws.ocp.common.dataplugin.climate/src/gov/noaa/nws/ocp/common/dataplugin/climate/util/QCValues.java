/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.util;

/**
 * This class holds the QC constants from PARAMETER_qc_methods.h. Not converted
 * to enums since a QC value could be undefined in this list.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 14 SEP 2016  22133      amoore      Initial creation
 * 27 OCT 2016  22135      wpaintsil   Add QCValueType enum
 * 24 JAN 2017  28499      amoore      Make final, and have private constructor.
 * </pre>
 * 
 * @author amoore
 */
public final class QCValues {
    /**
     * The lower bound for qc flags
     */
    public static final int QC_LOWER_BOUND = -1;

    /**
     * The upper bound for qc flags
     */
    public static final int QC_UPPER_BOUND = 18;

    /**
     * The value has been edited and saved by user
     */
    public static final int MANUAL_ENTRY = 0;

    /**
     * The value was retrieved from the ASOS MSM
     */
    public static final int VALUE_FROM_MSM = 1;

    /**
     * The value was taken from the daily DB
     */
    public static final int VALUE_FROM_DAILY = 2;

    /**** Temperature QC Flags ****/
    /**
     * Max-Min temp retrieved from Daily Summ. Msg
     */
    public static final int TEMP_FROM_DSM = 1;

    /**
     * Max-Min temp retrieved from 24-hour group
     */
    public static final int TEMP_FROM_24 = 2;

    /**
     * Max-Min temp retrieved from 6-hour group
     */
    public static final int TEMP_FROM_6 = 3;

    /**
     * Max-Min temp retrieved from hourly report
     */
    public static final int TEMP_FROM_HOURLY = 4;

    /**
     * Max-Min temp retrieved from rounded hourly
     */
    public static final int TEMP_FROM_WHOLE_HOURLY = 5;

    /**** Precipitation QC Flags ****/
    /**
     * Precip retrieved from Daily Summ Msg
     */
    public static final int PRECIP_FROM_DSM = 1;

    /**
     * Precip retrieved from 6, 3 and 1 hr amts
     */
    public static final int PRECIP_FROM_631HR = 2;

    /**
     * Precip retrieved from 6 and 3 hr amts
     */
    public static final int PRECIP_FROM_63HR = 3;

    /**
     * Precip retrieved from 6 and 1 hr amts
     */
    public static final int PRECIP_FROM_61HR = 4;

    /**
     * Precip retrieved from 3 and 1 hr amts
     */
    public static final int PRECIP_FROM_31HR = 5;

    /**
     * Precip retrieved from 6 hr amts only
     */
    public static final int PRECIP_FROM_6HR = 6;

    /**
     * Precip retrieved from 3 hr amts only
     */
    public static final int PRECIP_FROM_3HR = 7;

    /**
     * Precip retrieved from 1 hr amts only
     */
    public static final int PRECIP_FROM_1HR = 9;

    /**** Snow QC Flags ****/
    /**
     * Snow retrieved from Daily Summ Msg
     */
    public static final int SNOW_FROM_DSM = 1;

    /**
     * Snow retrieved from Suppl Clim Data
     */
    public static final int SNOW_FROM_SCD = 2;

    /**
     * Snow assumed based on temp/precip
     */
    public static final int SNOW_ASSUMED = 3;

    /**
     * Snow depth from 0Z METAR
     */
    public static final int DEPTH_00Z = -1;

    /**
     * Snow depth from 6Z METAR
     */
    public static final int DEPTH_06Z = 6;

    /**
     * Snow depth from 12Z METAR
     */
    public static final int DEPTH_12Z = 12;

    /**
     * Snow depth from 18Z METAR
     */
    public static final int DEPTH_18Z = 18;

    /**** Wind QC Flags ****/
    /**
     * Max Wind retrieved from Daily Summ Msg
     */
    public static final int MAX_WIND_FROM_DSM = 1;

    /**
     * Max Wind retrieved from special
     */
    public static final int MAX_WIND_FROM_SPECI = 2;

    /**
     * Max Wind retrieved from hourly
     */
    public static final int MAX_WIND_FROM_HOURLY = 3;

    /**
     * Max Gust retrieved from Daily Summ Msg
     */
    public static final int MAX_GUST_FROM_DSM = 1;

    /**
     * Max Gust retrieved from peak
     */
    public static final int MAX_GUST_FROM_PEAK = 4;

    /**
     * Max Wind retrieved from gust report
     */
    public static final int MAX_GUST_FROM_GUST = 5;

    /**
     * Average wind speed from Daily Summ Msg
     */
    public static final int AVG_WIND_FROM_DSM = 1;

    /**
     * Avg. wind calculated from METAR reports
     */
    public static final int AVG_WIND_CALCULATED = 2;

    /**** Sky Cover QC Flags ****/
    /**
     * Sky Cover retrieved from Daily Summ Msg
     */
    public static final int SKY_COVER_FROM_DSM = 1;

    /**
     * Sky Cover calculated from cloud reports
     */
    public static final int SKY_COVER_CALCULATED = 2;

    /**** Minutes Sunshine QC Flags ****/
    /**
     * Minutes of Sunshine from Daily Summ Msg
     */
    public static final int MIN_SUN_FROM_DSM = 1;

    /**
     * Minutes of Sunshine from 8Z METAR
     */
    public static final int MIN_SUN_METAR = 2;

    /**** Possible Sun QC Flags ****/
    /**
     * Poss Sun retrieved from Daily Summ Msg
     */
    public static final int POSS_SUN_FROM_DSM = 1;

    /**
     * Poss Sun calculated from astro and minutes sun
     */
    public static final int POSS_SUN_CALCULATED = 2;

    /**** Weather QC Flags ****/
    /**
     * Weather retrieved from Daily Summ Msg
     */
    public static final int WX_FROM_DSM = 1;

    /**
     * Weather retrieved from hourly obs
     */
    public static final int WX_FROM_OBS = 2;

    /**
     * Nothing exciting happened
     */
    public static final int WX_BORING = 3;

    /**
     * Weather from DSM and hourlies combined
     */
    public static final int WX_COMBO = 4;

    /**
     * Missing string commonly displayed in the message strings.
     */
    public static final String MISSING_STRING = "Missing";

    /**
     * Enum describing the types of fields a message string may be associated
     * with.
     * 
     * <pre>
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * 27 OCT 2016  22135      wpaintsil      Initial creation
     * 
     * </pre>
     * 
     * @author wpaintsil
     */
    public enum QCValueType {
        /**
         * Represents daily temperature fields.
         */
        TEMPERATURE,
        /**
         * Represents daily wind speed fields.
         */
        WIND,
        /**
         * Represents daily wind gust fields.
         */
        GUST,
        /**
         * Represents daily precip fields.
         */
        PRECIP,
        /**
         * Represents daily snow field.
         */
        SNOW,
        /**
         * Represents daily % poss sunshine field.
         */
        SUNSHINE,
        /**
         * Represents daily sky cover field.
         */
        SKYCOVER,
        /**
         * Represents weather checkboxes.
         */
        WEATHER,
        /**
         * Represents daily avg wind field.
         */
        AVGWIND,
        /**
         * Represents daily snow depth field.
         */
        SNOWDEPTH,
        /**
         * Represents daily minutes of sun field.
         */
        MINSUN,
        /**
         * Represents period fields.
         */
        PERIOD,
        /**
         * Represents missing value type.
         */
        MISSING;

        /**
         * Get the message string that would be associated with the given QC
         * value type(daily temperture, period, etc) and value.
         * 
         * @param qcValue
         * @return the message string that would be associated with the given
         *         temperature QC value and type.
         */
        public String getQCString(int qcValue) {
            switch (this) {
            case TEMPERATURE:
                return getTemperatureQCString(qcValue);
            case WIND:
                return getWindQCString(qcValue);
            case GUST:
                return getGustQCString(qcValue);
            case PRECIP:
                return getPrecipQCString(qcValue);
            case SNOW:
                return getSnowQCString(qcValue);
            case SUNSHINE:
                return getSunshineQCString(qcValue);
            case SKYCOVER:
                return getSkyCoverQCString(qcValue);
            case WEATHER:
                return getWeatherQCString(qcValue);
            case AVGWIND:
                return getAvgWindSpeedQCString(qcValue);
            case SNOWDEPTH:
                return getSnowDepthQCString(qcValue);
            case MINSUN:
                return getMinutesSunQCString(qcValue);
            case PERIOD:
                return getPeriodQCString(qcValue);
            default:
                return "Method: " + MISSING_STRING;
            }
        }
    }

    /**
     * Private constructor. This is a utility class.
     */
    private QCValues() {
    }

    /**
     * Get the message string that would be associated with the given
     * temperature QC value. First, the data value (not QC value) should be
     * checked if it is missing; if so, display "Method: Missing" instead of
     * calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given
     *         temperature QC value.
     */
    public static String getTemperatureQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == TEMP_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == TEMP_FROM_24) {
            result.append("24-Hour Temperature Group");
        } else if (iQC == TEMP_FROM_6) {
            result.append("6-Hour Temperature Group");
        } else if (iQC == TEMP_FROM_HOURLY) {
            result.append("Hourly Temperature");
        } else if (iQC == TEMP_FROM_WHOLE_HOURLY) {
            result.append("Rounded Hourly Temperature");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given wind QC
     * value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given wind
     *         QC value.
     */
    public static String getWindQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == MAX_WIND_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == MAX_WIND_FROM_SPECI) {
            result.append("Special Wind");
        } else if (iQC == MAX_WIND_FROM_HOURLY) {
            result.append("Hourly Wind");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given gust QC
     * value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given gust
     *         QC value.
     */
    public static String getGustQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == MAX_GUST_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == MAX_GUST_FROM_PEAK) {
            result.append("Peak Wind");
        } else if (iQC == MAX_GUST_FROM_GUST) {
            result.append("Hourly Gust");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given precip QC
     * value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given precip
     *         QC value.
     */
    public static String getPrecipQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == PRECIP_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == PRECIP_FROM_631HR) {
            result.append("6-Hour, 3-Hour, and Hourly Amounts");
        } else if (iQC == PRECIP_FROM_63HR) {
            result.append("6-Hour and 3-Hour Amounts");
        } else if (iQC == PRECIP_FROM_61HR) {
            result.append("6-Hour and Hourly Amounts");
        } else if (iQC == PRECIP_FROM_31HR) {
            result.append("3-Hour and Hourly Amounts");
        } else if (iQC == PRECIP_FROM_6HR) {
            result.append("6-Hour Amounts");
        } else if (iQC == PRECIP_FROM_3HR) {
            result.append("3-Hour Amounts");
        } else if (iQC == PRECIP_FROM_1HR) {
            result.append("Hourly Amounts");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given snow QC
     * value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given snow
     *         QC value.
     */
    public static String getSnowQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == SNOW_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == SNOW_FROM_SCD) {
            result.append("Supplementary Climate Data");
        } else if (iQC == SNOW_ASSUMED) {
            result.append("Assumed");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given sunshine
     * QC value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given
     *         sunshine QC value.
     */
    public static String getSunshineQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == POSS_SUN_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == POSS_SUN_CALCULATED) {
            result.append("Calculated");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given sky cover
     * QC value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given sky
     *         cover QC value.
     */
    public static String getSkyCoverQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == SKY_COVER_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == SKY_COVER_CALCULATED) {
            result.append("Calculated");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given weather QC
     * value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given
     *         weather QC value.
     */
    public static String getWeatherQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == WX_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == WX_FROM_OBS) {
            result.append("Hourly and Special Observations");
        } else if (iQC == WX_COMBO) {
            result.append("DSM and Hourly Observations");
        } else if (iQC == WX_BORING) {
            result.append("No Weather Reported");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given average
     * wind speed QC value. First, the data value (not QC value) should be
     * checked if it is missing; if so, display "Method: Missing" instead of
     * calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given
     *         average wind speed QC value.
     */
    public static String getAvgWindSpeedQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == AVG_WIND_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == AVG_WIND_CALCULATED) {
            result.append("Calculated from METAR Reports");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given snow depth
     * QC value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given snow
     *         depth QC value.
     */
    public static String getSnowDepthQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == SNOW_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == DEPTH_12Z) {
            result.append("12Z METAR Report");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given minutes of
     * sun QC value. First, the data value (not QC value) should be checked if
     * it is missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given
     *         minutes of sun QC value.
     */
    public static String getMinutesSunQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == MIN_SUN_FROM_DSM) {
            result.append("Daily Summary Message");
        } else if (iQC == MIN_SUN_METAR) {
            result.append("METAR Report");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }

    /**
     * Get the message string that would be associated with the given period QC
     * value. First, the data value (not QC value) should be checked if it is
     * missing; if so, display "Method: Missing" instead of calling this.
     * 
     * @param iQC
     * @return the message string that would be associated with the given period
     *         QC value.
     */
    public static String getPeriodQCString(int iQC) {
        /*
         * using if-else instead of switch for code clarity (cases would be
         * numbers instead of the names of the constants)
         */
        StringBuilder result = new StringBuilder("Method: ");
        if (iQC == MANUAL_ENTRY) {
            result.append("Entered Manually");
        } else if (iQC == VALUE_FROM_MSM) {
            result.append("Monthly Summary Message");
        } else if (iQC == VALUE_FROM_DAILY) {
            result.append("Daily Database");
        } else {
            result.append("Missing");
        }

        return result.toString();
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dao;

/**
 * Common values for the Climate DAOs.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 13 JUL 2016  20414      amoore      Initial creation.
 * 06 OCT 2016  20639      wkwock      Add table climate_day_config
 * 14 OCT 2016  20635      wkwock      Add climate_period
 * 09 FEB 2017  28609      amoore      Add fss wx period table.
 * 24 MAY 2017  33104      amoore      New constant for FSS work.
 * </pre>
 * 
 * @author amoore
 * @version 1.0
 */
public final class ClimateDAOValues {

    /**
     * Name of climate dates table.
     */
    public static final String CLIMO_DATES_TABLE_NAME = "climo_dates";

    /**
     * Name of daily climate table.
     */
    public static final String DAILY_CLIMATE_TABLE_NAME = "daily_climate";

    /**
     * Name of climate station setup table.
     */
    public static final String CLIMATE_STATION_SETUP_TABLE_NAME = "cli_sta_setup";

    /**
     * Name of day climate norm table.
     */
    public static final String DAY_CLIMATE_NORM_TABLE_NAME = "day_climate_norm";

    /**
     * Name of month climate norm table.
     */
    public static final String MONTH_CLIMATE_NORM_TABLE_NAME = "mon_climate_norm";

    /**
     * Name of monthly ASOS climate table.
     */
    public static final String CLI_ASOS_MONTHLY_TABLE_NAME = "cli_asos_monthly";

    /**
     * Name of daily ASOS climate table.
     */
    public static final String CLI_ASOS_DAILY_TABLE_NAME = "cli_asos_daily";

    /**
     * Name of monthly/seasonal/annual period data climate table.
     */
    public static final String CLIMATE_MONTHLY_SEASON_ANNUAL_TABLE_NAME = "cli_mon_season_yr";

    /**
     * Name of the station location climate table.
     */
    public static final String STATION_LOCATION_TABLE_NAME = "station_location";

    /**
     * Name of the boolean values climate table.
     */
    public static final String BOOLEAN_VALUES_TABLE_NAME = "boolean_values";

    /**
     * Name of the FSS Report climate table.
     */
    public static final String FSS_REPORT_TABLE_NAME = "fss_report";

    /**
     * Name of the FSS Category Multi climate table.
     */
    public static final String FSS_CATEGORY_MULTI_TABLE_NAME = "fss_categ_multi";

    /**
     * Name of the FSS Category Single climate table.
     */
    public static final String FSS_CATEGORY_SINGLE_TABLE_NAME = "fss_categ_single";

    /**
     * Name of the Climate Freeze Dates climate table.
     */
    public static final String CLIMATE_FREEZE_DATES_TABLE_NAME = "cli_freezedates";

    /**
     * Name of the FSS Contin Real climate table.
     */
    public static final String FSS_CONTIN_REAL_TABLE_NAME = "fss_contin_real";

    /**
     * Name of the FSS Cloud Layer climate table.
     */
    public static final String FSS_CLOUD_LAYER_TABLE_NAME = "fss_cloud_layer";

    /**
     * Name of the FSS Weather Period climate table.
     */
    public static final String FSS_WX_PERIOD_TABLE_NAME = "fss_wx_period";

    /**
     * Name of the climate_day_config table
     */
    public static final String CLIMATE_DAY_CONFIG_TABLE_NAME = "climate_day_config";

    /**
     * Name of the climate_period table.
     */
    public static final String CLIMATE_PERIOD_TABLE_NAME = "climate_period";

    /**
     * Name of defined_values table, holding all valid discrete values for all
     * METAR/TAF element IDs.
     */
    public static final String DEFINED_VALUES_TABLE_NAME = "defined_values";

    /**
     * Name of continuous real elements table, holding all valid max and min
     * values for all METAR/TAF element IDs.
     */
    public static final String CONTIN_REAL_ELE_TABLE_NAME = "contin_real_ele";

    /**
     * FSS Report instance sequence relation for fss_report table.
     */
    public static final String FSS_REPORT_FSS_RPT_INSTANCE_SEQ = "fss_report_fss_rpt_instance_seq";
}

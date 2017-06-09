package gov.noaa.nws.ocp.edex.common.climate.dataaccess;

/**
 * Configuration for Climate Tool data access
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 2016                    Wufeng Zhou Initial creation.
 * 18 AUG 2016  20750      amoore      Move to Climate EDEX common. Clean up.
 * 29 SEP 2016  22139      jwu         add velocity template files.
 * 30 SEP 2016  20752      pwang       Default to climate DB
 * 10 MAR 2017  30128      amoore      Globalday properties file should be a static property file.
 * 10 MAR 2017  30130      amoore      F6 should not write data to awips2 directories.
 * </pre>
 * 
 * @author Wufeng Zhou
 * @version 1.0
 */
public class ClimateDataAccessConfiguration {
    /**
     * Default Climate DB name.
     */
    private static final String DEFAULT_CLIMATE_DB_NAME = "climate";

    /**
     * Environment variable stating name of AWIPS 2 Climate database.
     */
    private static final String ENV_CLIMATE_DB_NAME = "climate.db.name";

    /**
     * Location of temporary F6 output, for printing.
     */
    public static final String F6_OUTPUT_LOCATION = "/tmp/climate/output/";

    /**
     * Location of climate data and properties.
     */
    public static final String DATA_LOCATION = "/awips2/climate/data/";

    /**
     * Location of Global Day properties.
     */
    public static final String GLOBAL_DAY_FILE = "/awips2/edex/conf/resources/globalDay.properties";

    /**
     * Velocity template file - all upper case.
     */
    public static final String VELOCITY_TEMP_ALL_UPPER_CASE = "/reportTemplates/f6_report.vm";

    /**
     * Velocity template file - mixed case.
     */
    public static final String VELOCITY_TEMP_MIXED_CASE = "/reportTemplates/f6_report_mixed_case.vm";

    /**
     * Get the name of the Climate database, based on environment variable first
     * {@link ClimateDataAccessConfiguration#ENV_CLIMATE_DB_NAME}, property file
     * second, and default to
     * {@link ClimateDataAccessConfiguration#DEFAULT_CLIMATE_DB_NAME}.
     * 
     * TODO this is only meant for parallel operations. Once AWIPS 1 Climate is
     * dropped in favor of only using AWIPS 2 Climate, the Climate database
     * should not be variable.
     * 
     * @return name of Climate database to use.
     */
    public static String getClimateDBName() {
        // if appears in system property, the use that
        if (System.getProperty(ENV_CLIMATE_DB_NAME, "").equals("") == false) {
            return System.getProperty(ENV_CLIMATE_DB_NAME, "");
        } else {
            // use property file setting
            // TODO: once the property file location is determined
        }
        // return DEFAULT_CLIMATE_DB_NAME;
        // for now, use the A2 DB name
        return DEFAULT_CLIMATE_DB_NAME;
    }
}

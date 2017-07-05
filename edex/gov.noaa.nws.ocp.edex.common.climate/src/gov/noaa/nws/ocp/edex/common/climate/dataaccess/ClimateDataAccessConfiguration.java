/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
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
 * 07 AUG 2017  36783      amoore      Move constants.
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

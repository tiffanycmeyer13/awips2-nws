/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dataaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateGlobal;

/**
 * 
 * Read and write preferences to file globalDay.properties.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 21, 2016 20639      xzhang      Initial creation
 * Sep 30, 2016 20639      wkwock      Fix directory does not exist issue.
 * 06 DEC 2016  20414      amoore      Fix logging and doc.
 * 19 DEC 2016  20955      amoore      Save and return default globals if file not found.
 * 24 JAN 2017  28496      amoore      Rename from ClimateIOSrv to ClimateGlobalConfiguration.
 * 23 FEB 2017  29416      wkwock      Added displayWait and reviewWait.
 * 10 MAR 2017  30128      amoore      Globalday properties file should be a static property file.
 * 15 MAR 2017  30162      amoore      Move out of DAO package, since this does not use database.
 * 02 Jun 2017  34788      pwang       add allowAutoSend, copyNWRTo, allowDisseminate
 * 19 JUN 2017  33104      amoore      Address review comments.
 * 07 AUG 2017  36783      amoore      Globalday properties file should be a localization file,
 *                                     so that EDEX clusters are consistent. Move to localization
 *                                     plugin. Globalday config was not reading and saving from
 *                                     the same place.
 * 18 AUG 2017  37104      amoore      Add IFPS office name and timezone.
 * 22 AUG 2017  37240      amoore      Add new settings to saving.
 * 04 OCT 2017  38067      amoore      Fix PM/IM delay in data reports.
 * 20 OCT 2017  39784      amoore      Prefer SITE, then REGION, then BASE, not just BASE.
 * 06 NOV 2017  35731      pwang       added properties for controlling if an product can be auto generated
 * 19 SEP 2018  DR 20890   dfriedman   Save preferences with a LocalizationFile.
 * 12 OCT 2018  DR 20897   dfriedman   Support AFOS designator overrides.
 * 23 OCT 2018  DR 20919   dfriedman   Make properties available to Spring.
 * </pre>
 * 
 * @author xzhang
 * @version 1.0
 */
public class ClimateGlobalConfiguration {

    /** The logger */
    private final static IUFStatusHandler logger = UFStatus
            .getHandler(ClimateGlobalConfiguration.class);

    /**
     * Directory of Global Day properties.
     */
    private static final String GLOBAL_DAY_DIR = "climate" + File.separator;

    /**
     * Name of Global Day properties.
     */
    private static final String GLOBAL_DAY_FILE = "globalDay.properties";

    /**
     * Location of Global Day properties.
     */
    private static final String GLOBAL_DAY_PATH = GLOBAL_DAY_DIR
            + GLOBAL_DAY_FILE;

    /**
     * Localization levels to try to load, in order
     */
    private static final LocalizationLevel[] LOCALIZATIONS_TO_TRY = new LocalizationLevel[] {
            LocalizationLevel.SITE, LocalizationLevel.REGION,
            LocalizationLevel.BASE };

    /**
     * Name of placeholder property for site time zone in Spring XML files.
     */
    private static final String CPG_CRON_TIMEZONE_SPRING_PROPERTY = "climate.cpg.cron.timezone";

    /**
     * @return global configuration values from SITE-REGION-BASE in that
     *         preference order; can be null on error.
     */
    public static ClimateGlobal getGlobal() {
        ClimateGlobal resGlobal = new ClimateGlobal();
        Properties prop = new Properties();

        IPathManager pm = PathManagerFactory.getPathManager();

        boolean success = false;

        for (int i = 0; (i < LOCALIZATIONS_TO_TRY.length) && !success; i++) {
            LocalizationLevel level = LOCALIZATIONS_TO_TRY[i];

            LocalizationContext lc = pm
                    .getContext(LocalizationType.COMMON_STATIC, level);

            File globalFile = pm.getFile(lc, GLOBAL_DAY_PATH);

            try (InputStream input = new FileInputStream(globalFile)) {
                prop.load(input);

                resGlobal.setUseValidIm(
                        "T".equals(prop.getProperty("climate.useValidIm"))
                                ? true : false);
                resGlobal.setUseValidPm(
                        "T".equals(prop.getProperty("climate.useValidPm"))
                                ? true : false);
                resGlobal.setNoAsterisk(
                        "T".equals(prop.getProperty("climate.noAsterisk"))
                                ? true : false);
                resGlobal.setNoColon(
                        "T".equals(prop.getProperty("climate.noColon")) ? true
                                : false);
                resGlobal.setNoMinus(
                        "T".equals(prop.getProperty("climate.noMinus")) ? true
                                : false);
                resGlobal.setNoSmallLetters(
                        "T".equals(prop.getProperty("climate.noSmallLetters"))
                                ? true : false);
                resGlobal.setValidIm(prop.getProperty("climate.intermediate"));
                resGlobal.setValidPm(prop.getProperty("climate.evening"));
                resGlobal.setT1(
                        Integer.parseInt(prop.getProperty("climate.T1")));
                resGlobal.setT2(
                        Integer.parseInt(prop.getProperty("climate.T2")));
                resGlobal.setT3(
                        Integer.parseInt(prop.getProperty("climate.T3")));
                resGlobal.setT4(
                        Integer.parseInt(prop.getProperty("climate.T4")));
                resGlobal.setT5(
                        Integer.parseInt(prop.getProperty("climate.T5")));
                resGlobal.setT6(
                        Integer.parseInt(prop.getProperty("climate.T6")));
                resGlobal.setP1(
                        Float.parseFloat(prop.getProperty("climate.P1")));
                resGlobal.setP2(
                        Float.parseFloat(prop.getProperty("climate.P2")));
                resGlobal.setS1(
                        Float.parseFloat(prop.getProperty("climate.S1")));

                resGlobal.setDisplayWait(Integer
                        .parseInt(prop.getProperty("climate.displayWait")));
                resGlobal.setReviewWait(Integer
                        .parseInt(prop.getProperty("climate.reviewWait")));
                resGlobal.setAllowAutoSend(
                        prop.getProperty("climate.allowAutoSend")
                                .equalsIgnoreCase("true") ? true : false);
                resGlobal.setCopyNWRTo(prop.getProperty("climate.copyNWRTo"));
                resGlobal.setAllowDisseminate(
                        prop.getProperty("climate.allowDisseminate")
                                .equalsIgnoreCase("true") ? true : false);
                resGlobal.setOfficeName(
                        prop.getProperty("climate.siteofficename"));
                resGlobal.setTimezone(prop.getProperty("climate.sitetimezone"));

                resGlobal.setAutoF6(prop.getProperty("climate.autoF6")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setAutoAM(prop.getProperty("climate.autoAM")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setAutoIM(prop.getProperty("climate.autoIM")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setAutoPM(prop.getProperty("climate.autoPM")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setAutoCLM(prop.getProperty("climate.autoCLM")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setAutoCLS(prop.getProperty("climate.autoCLS")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setAutoCLA(prop.getProperty("climate.autoCLA")
                        .equalsIgnoreCase("true") ? true : false);
                resGlobal.setStationDesignatorOverrides(parseStationDesignatorOverrides(
                        prop.getProperty("climate.stationDesignatorOverrides")));

                // got to end without error; use this globalDay file
                success = true;
            } catch (FileNotFoundException e) {
                logger.info("Could not find globalDay file ["
                        + globalFile.getAbsolutePath()
                        + "] at localization level: [" + level.toString()
                        + "]. " + e.getMessage());
            } catch (IOException e) {
                logger.error("Error reading globals file. Returning null.", e);
                return null;
            } catch (NumberFormatException e) {
                logger.error("Failed to parse globals file. Returning null.",
                        e);
                return null;
            } catch (NullPointerException e) {
                logger.error(
                        "Failed to parse globals file due to some missing property. Returning null.");
                return null;
            }
        }

        if (!success) {
            logger.warn(
                    "Could not find climate globals file. Saving and returning default values.");
            resGlobal = ClimateGlobal.getDefaultGlobalValues();
            saveGlobal(resGlobal);
        }

        return resGlobal;
    }

    /**
     * Save the given global day settings to SITE.
     * 
     * @param global
     *            settings to save
     * @return status indicator
     */
    public static int saveGlobal(ClimateGlobal global) {
        int status = 0;
        Properties prop = new Properties();

        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        ILocalizationFile lf = pm.getLocalizationFile(lc, GLOBAL_DAY_PATH);

        try (SaveableOutputStream output = lf.openOutputStream()) {
            // set the properties value
            prop.setProperty("climate.T1", String.valueOf(global.getT1()));
            prop.setProperty("climate.T2", String.valueOf(global.getT2()));
            prop.setProperty("climate.T3", String.valueOf(global.getT3()));
            prop.setProperty("climate.T4", String.valueOf(global.getT4()));
            prop.setProperty("climate.T5", String.valueOf(global.getT5()));
            prop.setProperty("climate.T6", String.valueOf(global.getT6()));
            prop.setProperty("climate.P1", String.valueOf(global.getP1()));
            prop.setProperty("climate.P2", String.valueOf(global.getP2()));
            prop.setProperty("climate.S1", String.valueOf(global.getS1()));
            prop.setProperty("climate.noAsterisk",
                    global.isNoAsterisk() ? "T" : "F");
            prop.setProperty("climate.useValidPm",
                    global.isUseValidPm() ? "T" : "F");
            prop.setProperty("climate.evening",
                    global.getValidPm().toFullString());
            prop.setProperty("climate.useValidIm",
                    global.isUseValidIm() ? "T" : "F");
            prop.setProperty("climate.intermediate",
                    global.getValidIm().toFullString());
            prop.setProperty("climate.noMinus",
                    global.isNoMinus() ? "T" : "F");
            prop.setProperty("climate.noSmallLetters",
                    global.isNoSmallLetters() ? "T" : "F");
            prop.setProperty("climate.noColon",
                    global.isNoColon() ? "T" : "F");
            prop.setProperty("climate.displayWait",
                    Integer.toString(global.getDisplayWait()));
            prop.setProperty("climate.reviewWait",
                    Integer.toString(global.getReviewWait()));
            prop.setProperty("climate.allowAutoSend",
                    global.isAllowAutoSend() ? "true" : "false");
            prop.setProperty("climate.copyNWRTo", global.getCopyNWRTo());
            prop.setProperty("climate.allowDisseminate",
                    global.isAllowDisseminate() ? "true" : "false");
            prop.setProperty("climate.siteofficename",
                    global.getOfficeName());
            prop.setProperty("climate.sitetimezone", global.getTimezone());

            prop.setProperty("climate.autoF6",
                    global.isAutoF6() ? "true" : "false");
            prop.setProperty("climate.autoAM",
                    global.isAutoAM() ? "true" : "false");
            prop.setProperty("climate.autoIM",
                    global.isAutoIM() ? "true" : "false");
            prop.setProperty("climate.autoPM",
                    global.isAutoPM() ? "true" : "false");
            prop.setProperty("climate.autoCLM",
                    global.isAutoCLM() ? "true" : "false");
            prop.setProperty("climate.autoCLS",
                    global.isAutoCLS() ? "true" : "false");
            prop.setProperty("climate.autoCLA",
                    global.isAutoCLA() ? "true" : "false");
            prop.setProperty("climate.stationDesignatorOverrides",
                    formatStationDesignatorOverrides(
                            global.getStationDesignatorOverrides()));

            // save properties
            prop.store(output, null);
            output.save();
        } catch (Exception e) {
            logger.error("Error saving global day properties.", e);
            status = -1;
        }

        return status;
    }

    /**
     * Parse the "climate.stationDesignatorOverrides" setting into a Map.
     * <p>
     * The format is a sequence of ',' separated elements. Each element is of
     * the form {station ID}:{product designator}. Invalid elements are
     * ignored (and lost if saved again.)
     *
     * @param text
     * @return
     */
    private static Map<String, String> parseStationDesignatorOverrides(String text) {
        return Stream.of((text != null ? text : "").split(","))
            .map(elem -> Stream.of(elem.split(":"))
                    .map(p -> p.trim())
                    .toArray(String[]::new))
            .filter(parts -> parts.length == 2 && parts[0].length() > 0
                    && parts[1].length() > 0)
            .collect(Collectors.toMap(
                    parts -> parts[0], parts -> parts[1],
                    (a, b) -> a)); // ignore duplicates, favoring the first value
    }

    /**
     * Format a map of station IDs to product IDs into text for the
     * "climate.stationDesignatorOverrides" setting.
     *
     * @param map
     * @return
     */
    private static String formatStationDesignatorOverrides(Map<String, String> map) {
        return (map != null ? map : new HashMap<String, String>()).entrySet()
                .stream()
                .map(e -> e.getKey() + ':' + e.getValue())
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /**
     * Retrieve properties to be used in Spring XML files.  Currently only
     * the site time zone is provided.
     */
    public static Properties getSpringProperties() {
        Properties props = new Properties();
        ClimateGlobal globalConfig = getGlobal();
        props.setProperty(CPG_CRON_TIMEZONE_SPRING_PROPERTY, globalConfig.getTimezone());
        return props;
    }
}

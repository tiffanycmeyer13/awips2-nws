/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.common.climate.dataaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
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
     * Location of Global Day properties.
     */
    private static final String GLOBAL_DAY_FILE = "climate/globalDay.properties";

    /**
     * @return global configuration values; can be null.
     */
    public static ClimateGlobal getGlobal() {
        ClimateGlobal global = new ClimateGlobal();
        Properties prop = new Properties();

        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE);

        File globalFile = pm.getFile(lc, GLOBAL_DAY_FILE);

        try (InputStream input = new FileInputStream(globalFile)) {
            prop.load(input);

            global.setUseValidIm(
                    "T".equals(prop.getProperty("climate.useValidIm")) ? true
                            : false);
            global.setUseValidPm(
                    "T".equals(prop.getProperty("climate.useValidPm")) ? true
                            : false);
            global.setNoAsterisk(
                    "T".equals(prop.getProperty("climate.noAsterisk")) ? true
                            : false);
            global.setNoColon("T".equals(prop.getProperty("climate.noColon"))
                    ? true : false);
            global.setNoMinus("T".equals(prop.getProperty("climate.noMinus"))
                    ? true : false);
            global.setNoSmallLetters(
                    "T".equals(prop.getProperty("climate.noSmallLetters"))
                            ? true : false);
            global.setValidIm(prop.getProperty("climate.morning"));
            global.setValidPm(prop.getProperty("climate.evening"));
            global.setT1(Integer.parseInt(prop.getProperty("climate.T1")));
            global.setT2(Integer.parseInt(prop.getProperty("climate.T2")));
            global.setT3(Integer.parseInt(prop.getProperty("climate.T3")));
            global.setT4(Integer.parseInt(prop.getProperty("climate.T4")));
            global.setT5(Integer.parseInt(prop.getProperty("climate.T5")));
            global.setT6(Integer.parseInt(prop.getProperty("climate.T6")));
            global.setP1(Float.parseFloat(prop.getProperty("climate.P1")));
            global.setP2(Float.parseFloat(prop.getProperty("climate.P2")));
            global.setS1(Float.parseFloat(prop.getProperty("climate.S1")));

            global.setDisplayWait(
                    Integer.parseInt(prop.getProperty("climate.displayWait")));
            global.setReviewWait(
                    Integer.parseInt(prop.getProperty("climate.reviewWait")));
            global.setAllowAutoSend(prop.getProperty("climate.allowAutoSend")
                    .equalsIgnoreCase("true") ? true : false);
            global.setCopyNWRTo(prop.getProperty("climate.copyNWRTo"));
            global.setAllowDisseminate(
                    prop.getProperty("climate.allowDisseminate")
                            .equalsIgnoreCase("true") ? true : false);
            global.setOfficeName(prop.getProperty("climate.siteofficename"));
            global.setTimezone(prop.getProperty("climate.sitetimezone"));

        } catch (FileNotFoundException e) {
            logger.error(
                    "Error finding globals file. Saving and returning default values.",
                    e);
            global = ClimateGlobal.getDefaultGlobalValues();
            saveGlobal(global);
        } catch (IOException e) {
            logger.error("Error reading globals file. Returning null.", e);
            global = null;
        } catch (NumberFormatException e) {
            logger.error("Failed to parse globals file. Returning null.", e);
            global = null;
        }

        return global;
    }

    /**
     * Save the given global day settings.
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
                LocalizationLevel.BASE);

        File globalDayPath = pm.getFile(lc, GLOBAL_DAY_FILE);

        try {
            if (!globalDayPath.exists()) {
                if (!globalDayPath.mkdirs()) {
                    logger.error("The file: [" + globalDayPath.getAbsolutePath()
                            + "] does not exist and a path could not be created.");
                } else {
                    logger.debug("The file: [" + globalDayPath.getAbsolutePath()
                            + "] does not exist but the path was created.");
                }
            }

            try (OutputStream output = new FileOutputStream(globalDayPath)) {
                // set the properties value
                prop.setProperty("climate.useValidIm",
                        global.isUseValidIm() ? "T" : "F");
                prop.setProperty("climate.useValidPm",
                        global.isUseValidPm() ? "T" : "F");
                prop.setProperty("climate.noAsterisk",
                        global.isNoAsterisk() ? "T" : "F");
                prop.setProperty("climate.noColon",
                        global.isNoColon() ? "T" : "F");
                prop.setProperty("climate.noMinus",
                        global.isNoMinus() ? "T" : "F");
                prop.setProperty("climate.noSmallLetters",
                        global.isNoSmallLetters() ? "T" : "F");
                prop.setProperty("climate.morning",
                        global.getValidIm().toFullString());
                prop.setProperty("climate.evening",
                        global.getValidPm().toFullString());
                prop.setProperty("climate.T1", global.getT1() + "");
                prop.setProperty("climate.T2", global.getT2() + "");
                prop.setProperty("climate.T3", global.getT3() + "");
                prop.setProperty("climate.T4", global.getT4() + "");
                prop.setProperty("climate.T5", global.getT5() + "");
                prop.setProperty("climate.T6", global.getT6() + "");
                prop.setProperty("climate.P1", global.getP1() + "");
                prop.setProperty("climate.P2", global.getP2() + "");
                prop.setProperty("climate.S1", global.getS1() + "");
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

                // save properties
                prop.store(output, null);
            } catch (Exception e) {
                logger.error("Error setting global day properties.", e);
                status = -1;
            }
        } catch (Exception e) {
            logger.error("Error with global day properties directory", e);
            status = -2;
        }

        return status;
    }
}

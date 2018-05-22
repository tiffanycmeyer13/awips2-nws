/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.climodates;

import java.io.InputStream;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.localization.climate.producttype.ClimateProductTypeManager;

/**
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 09 APR 2018  DR17116     wpaintsil   Initial creation
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */
public class ClimoDatesManager {

    /**
     * Names of directories for the localization files.
     */
    public static final String CLIMATE_ROOT = "climate"
            + IPathManager.SEPARATOR;

    public static final String CLIMO_DATES_ROOT = CLIMATE_ROOT + "climoDates"
            + IPathManager.SEPARATOR;

    /**
     * File names for forecasters.
     */
    private static final String CLIMO_DATES_XML_FILE = "climoDates.xml";

    /**
     * Singleton instance of this class
     */
    private static ClimoDatesManager instance;

    /**
     * JAXB manager for marshal/unmarshal.
     */
    private static final SingleTypeJAXBManager<ClimoDates> jaxb = SingleTypeJAXBManager
            .createWithoutException(ClimoDates.class);

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProductTypeManager.class);

    private ClimoDatesManager() {

    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     */
    public synchronized static ClimoDatesManager getInstance() {
        if (instance == null) {
            instance = new ClimoDatesManager();
        }

        return instance;
    }

    /**
     * Saves ClimoDates to localization (SITE level).
     *
     * @param climoDates
     * @return true if properly saved; false otherwise.
     */
    public boolean saveClimoDates(ClimoDates climoDates) {
        IPathManager pm = PathManagerFactory.getPathManager();

        // Create a SITE level file if one is not found.
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        ILocalizationFile locFile = pm.getLocalizationFile(lc,
                CLIMO_DATES_ROOT + CLIMO_DATES_XML_FILE);

        // Save to an XML file.
        try (SaveableOutputStream sos = locFile.openOutputStream()) {
            jaxb.marshalToStream(climoDates, sos);
            sos.save();
        } catch (Exception e) {
            logger.error("ClimoDatesManager couldn't save configuration file: ",
                    locFile.getPath(), e);
            return false;
        }
        return true;
    }

    /**
     * Get ClimoDates information.
     *
     * @return climoDates.
     */
    public ClimoDates getClimoDates() {

        ClimoDates climoDates = ClimoDates.getDefaultClimoDates();

        Object climoDatesObj = null;

        IPathManager pm = PathManagerFactory.getPathManager();

        String fullName = CLIMO_DATES_ROOT + CLIMO_DATES_XML_FILE;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fullName);

        if (lf != null) {

            try (InputStream is = lf.openInputStream()) {
                climoDatesObj = jaxb.unmarshalFromInputStream(is);
            } catch (Exception ee) {
                logger.error("ClimoDatesManager: Error unmarshalling XML file: "
                        + lf.getPath(), ee);
            }
        }

        if (climoDatesObj != null) {
            climoDates = (ClimoDates) climoDatesObj;
        }

        return climoDates;
    }

}

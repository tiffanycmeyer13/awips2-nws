/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.plugin.climate.asos.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.ILocalizationPathObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * ClimateIngestConfigurationManager enable site to control which data sources
 * can be ingested via Localization
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 2, 2016  20905      pwang       Initial creation
 * 09 MAY 2017  33104      amoore      Keep default to SITE level,
 *                                     but use BASE if SITE is undefined.
 *                                     Fix logging. Clean up.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class ClimateIngestConfigurationManager
        implements ILocalizationPathObserver {

    /** Path to Climate ingest Source filter config. */
    private static final String CONFIG_FILE_NAME = "climate"
            + IPathManager.SEPARATOR + "climate_ingest_config.xml";

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateIngestConfigurationManager.class);

    private static final SingleTypeJAXBManager<ClimateIngestConfigXML> jaxb = SingleTypeJAXBManager
            .createWithoutException(ClimateIngestConfigXML.class);

    /**
     * FFMP Run Configuration XML object.
     */
    protected ClimateIngestConfigXML configXml;

    /** Singleton instance of this class */
    private static ClimateIngestConfigurationManager instance = new ClimateIngestConfigurationManager();

    /** Private Constructor */
    private ClimateIngestConfigurationManager() {
        try {
            readConfigXml();
        } catch (Exception e) {
            logger.error("Error reading ASOS ingest config file.", e);
        }
    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     * @throws FileNotFoundException
     */
    public static ClimateIngestConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Read the XML configuration data for the current XML file name.
     */
    public void readConfigXml() throws SerializationException {

        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        ILocalizationFile lf = pm.getLocalizationFile(lc, CONFIG_FILE_NAME);
        pm.addLocalizationPathObserver(CONFIG_FILE_NAME, this);

        if (!lf.exists()) {
            logger.warn(lf.getPath()
                    + " does not exist for this site. Base file will be used.");

            lc = pm.getContext(LocalizationType.COMMON_STATIC,
                    LocalizationLevel.BASE);
            lf = pm.getLocalizationFile(lc, CONFIG_FILE_NAME);

            if (!lf.exists()) {
                logger.error(lf.getPath()
                        + " base file path does not exist. Empty configuration will be used.");
                return;
            }
        }

        ClimateIngestConfigXML configXmltmp = null;

        try (InputStream is = lf.openInputStream()) {
            configXmltmp = jaxb.unmarshalFromInputStream(is);
        } catch (IOException | LocalizationException e) {
            throw new SerializationException(
                    "Error unmarshalling " + lf.getPath(), e);
        }

        configXml = configXmltmp;
    }

    /**
     * Save the XML configuration data to the current SITE XML file name.
     */
    public void saveConfigXml() {
        // Save the xml object to disk
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        ILocalizationFile newXmlFile = pm.getLocalizationFile(lc,
                CONFIG_FILE_NAME);

        try (SaveableOutputStream sos = newXmlFile.openOutputStream()) {
            jaxb.marshalToStream(configXml, sos);
            sos.save();
        } catch (Exception e) {
            logger.handle(Priority.ERROR, "Couldn't save config file.", e);
        }
    }

    /**
     * Get the FFMP runners
     *
     * @return
     */
    public ArrayList<ClimateIngestFilterXML> getIngestFilters() {
        if (configXml == null) {
            return null;
        }

        return configXml.getFilters();
    }

    @Override
    public void fileChanged(ILocalizationFile file) {
        if (file.getPath().equals(CONFIG_FILE_NAME)) {
            try {
                readConfigXml();
            } catch (Exception e) {
                logger.error(
                        "Error trying to update after Climate ASOS ingestion filter: ["
                                + file.getPath() + "] was changed.",
                        e);
            }
        }
    }

}

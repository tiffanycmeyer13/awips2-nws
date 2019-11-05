/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.stationorder;

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
 * Get the order of stations as listed in a localization XML file.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2019 DR21671    wpaintsil   Initial creation
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */

public class ClimateStationOrderManager implements ILocalizationPathObserver {

    /** Path to Climate station order XML file. */
    private static final String CONFIG_FILE_NAME = "climate"
            + IPathManager.SEPARATOR + "climate_station_order.xml";

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateStationOrderManager.class);

    private static final SingleTypeJAXBManager<ClimateStationOrder> jaxb = SingleTypeJAXBManager
            .createWithoutException(ClimateStationOrder.class);

    /**
     * Station Order Configuration XML object.
     */
    protected ClimateStationOrder stationOrderXml;

    /** Singleton instance of this class */
    private static ClimateStationOrderManager instance = new ClimateStationOrderManager();

    /** Private Constructor */
    private ClimateStationOrderManager() {
        try {
            readStationOrder();
        } catch (Exception e) {
            logger.error("Error reading station order config file.", e);
        }
    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     * @throws FileNotFoundException
     */
    public static ClimateStationOrderManager getInstance() {
        return instance;
    }

    /**
     * Read the XML configuration data for the current XML file name.
     */
    public void readStationOrder() throws SerializationException {
        // initialize site file object and add self as observer
        IPathManager pm = PathManagerFactory.getPathManager();

        pm.addLocalizationPathObserver(CONFIG_FILE_NAME, this);

        // localization file to use
        ILocalizationFile localizationFile = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, CONFIG_FILE_NAME);

        ClimateStationOrder configXmltmp = null;

        try (InputStream is = localizationFile.openInputStream()) {
            configXmltmp = jaxb.unmarshalFromInputStream(is);
        } catch (IOException | LocalizationException e) {
            throw new SerializationException(
                    "Error unmarshalling " + localizationFile.getPath(), e);
        }

        stationOrderXml = configXmltmp;
    }

    /**
     * Save the XML configuration data to the current SITE XML file name.
     */
    public void saveStationOrder() {
        // Save the xml object to disk
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        ILocalizationFile newXmlFile = pm.getLocalizationFile(lc,
                CONFIG_FILE_NAME);

        try (SaveableOutputStream sos = newXmlFile.openOutputStream()) {
            jaxb.marshalToStream(stationOrderXml, sos);
            sos.save();
        } catch (Exception e) {
            logger.handle(Priority.ERROR, "Couldn't save config file.", e);
        }
    }

    /**
     *
     * @return the ordered list of stations
     */
    public ArrayList<String> getStationOrder() {
        if (stationOrderXml == null) {
            return new ArrayList<>();
        }

        return stationOrderXml.getStations();
    }

    @Override
    public void fileChanged(ILocalizationFile file) {
        if (file.getPath().equals(CONFIG_FILE_NAME)) {
            try {
                readStationOrder();
            } catch (Exception e) {
                logger.error(
                        "Error trying to update after station order file: ["
                                + file.getPath() + "] was changed.",
                        e);
            }
        }
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.time.util.TimeUtil;

import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductsConfig;


/**
 * 
 * Class for CWA generator utilities
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 15, 2020 75767      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWAGeneratorUtil {
    public static final String CWA_DIR = "cwsu";

    public static String CWA_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWAProductConfig.xml");

    public static String CWA_PRACTICE_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWAPracticeProductConfig.xml");

    public static String CWS_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWSProductConfig.xml");

    public static String CWS_PRACTICE_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWSPracticeProductConfig.xml");

    /** operational mode CWA products XML file */
    public static final String PRODUCTS_FILE = LocalizationUtil.join(CWA_DIR,
            "products.xml");

    /** practice mode CWA products XML file */
    public static final String PRACTICE_PRODUCTS_FILE = LocalizationUtil
            .join(CWA_DIR, "practiceProducts.xml");

    public String[] types = { "Thunderstorm", "IFR/LIFR", "Turb/LLWS",
            "Icing/FRZA", "BLDU/BLSA", "Volcano", "Can/Man", "MIS", "All" };

    /**
     * save CWA product configuration to a XML file
     * 
     * @param configs
     * @param fileName
     * @throws JAXBException
     * @throws IOException
     * @throws LocalizationException
     * @throws SerializationException
     */
    public static void saveProductConfigurations(CWAProductConfig configs,
            String fileName) throws JAXBException, IOException,
            LocalizationException, SerializationException {
        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.USER);

        LocalizationFile configFile = pm.getLocalizationFile(lc, fileName);

        SingleTypeJAXBManager<CWAProductConfig> jaxb = new SingleTypeJAXBManager<>(
                CWAProductConfig.class);
        try (SaveableOutputStream sos = configFile.openOutputStream()) {
            jaxb.marshalToStream(configs, sos);
            sos.save();
        }
    }

    public static CWAProductConfig readProductConfigurations(String fileName)
            throws SerializationException, LocalizationException, IOException,
            JAXBException {
        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationFile configFile = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fileName);
        if (configFile == null || !configFile.exists()) {
            return null;
        }

        CWAProductConfig productConfigs = null;
        SingleTypeJAXBManager<CWAProductConfig> jaxb = new SingleTypeJAXBManager<>(
                CWAProductConfig.class);
        try (InputStream is = configFile.openInputStream()) {
            productConfigs =  jaxb
                    .unmarshalFromInputStream(is);
        }

        return productConfigs;
    }

    /**
     * product text list from XML file
     * 
     * @param isOperational
     * @param retainInDays
     * @return
     * @throws LocalizationException
     * @throws SerializationException
     * @throws IOException
     * @throws JAXBException
     */
    public static List<ProductConfig> readProductsXML(boolean isOperational,
            int retainInDays) throws LocalizationException,
            SerializationException, IOException, JAXBException {
        String fileName = PRODUCTS_FILE;
        if (!isOperational) {
            fileName = PRACTICE_PRODUCTS_FILE;
        }
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile xmlFile = null;
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        List<ProductConfig> productTextList = new ArrayList<>();
        xmlFile = pm.getLocalizationFile(lc, fileName);
        if (!xmlFile.exists()) {
            return productTextList;
        }

        SingleTypeJAXBManager<ProductsConfig> jaxb = new SingleTypeJAXBManager<>(
                ProductsConfig.class);
        ProductsConfig productsConfig = null;
        try (InputStream is = xmlFile.openInputStream()) {
            productsConfig = jaxb.unmarshalFromInputStream(is);
        }

        if (productsConfig == null
                || productsConfig.getProductsConfig() != null) {
            long startTime = TimeUtil.newGmtCalendar().getTimeInMillis()
                    - (retainInDays * TimeUtil.MILLIS_PER_DAY);
            for (ProductConfig productConfig : productsConfig
                    .getProductsConfig()) {
                if (productConfig.getTime() > startTime) {
                    productTextList.add(productConfig);
                }
            }
        }
        return productTextList;

    }

    /**
     * Save product text list to XML file
     * 
     * @param isOperational
     * @param productXMLList
     * @throws IOException
     * @throws LocalizationException
     * @throws SerializationException
     * @throws JAXBException
     */
    public static void writeProductsXML(boolean isOperational,
            List<ProductConfig> productXMLList) throws IOException,
            LocalizationException, SerializationException, JAXBException {
        String fileName = PRODUCTS_FILE;
        if (!isOperational) {
            fileName = PRACTICE_PRODUCTS_FILE;
        }
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);// site only
        LocalizationFile xmlFile = pm.getLocalizationFile(lc, fileName);

        ProductsConfig productsConfig = new ProductsConfig();
        productsConfig.setProductsConfig(productXMLList);
        SingleTypeJAXBManager<ProductsConfig> jaxb = new SingleTypeJAXBManager<>(
                ProductsConfig.class);
        try (SaveableOutputStream sos = xmlFile.openOutputStream();) {
            jaxb.marshalToStream(productsConfig, sos);
            sos.save();
        }
    }

}

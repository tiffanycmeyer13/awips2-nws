/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.localization.climate.producttype;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.ILocalizationPathObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.monitor.events.MonitorConfigEvent;
import com.raytheon.uf.common.monitor.events.MonitorConfigListener;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.climate.PeriodType;

/**
 * ClimateProductTypeManager enables user to control product types via
 * Localization.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 22, 2016  20640      jwu         Initial creation
 * Feb 28, 2017  29599      jwu         Config product only at SITE level.
 * May 11, 2017  33104      amoore      Address minor FindBugs.
 * May 11, 2017  33534      jwu         Always reload types by default.
 * May 31, 2017  33104      amoore      Address review comments.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ClimateProductTypeManager implements ILocalizationPathObserver {

    /**
     * Constants to access climate product type configuration files.
     */
    public static final String CLIMATE_ROOT = "climate"
            + IPathManager.SEPARATOR;

    public static final String SETUP_ROOT = CLIMATE_ROOT + "productTypes"
            + IPathManager.SEPARATOR;

    public static final String FILE_NAME_PREFIX = "product";

    public static final String FILE_NAME_SEPARATOR = "_";

    public static final String FILE_NAME_EXT = ".xml";

    public static final String FILE_NAME_START = SETUP_ROOT + FILE_NAME_PREFIX
            + FILE_NAME_SEPARATOR;

    public static final Pattern FILE_NAME_PATTERN = Pattern.compile(
            "product_(am|im|pm|mon|sea|ann)_([A-Z,0-9]{3})_(NWR|NWWS).xml");

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProductTypeManager.class);

    /**
     * JAXB manager for marshal/unmarshal.
     */
    private static final SingleTypeJAXBManager<ClimateProductType> jaxb = SingleTypeJAXBManager
            .createWithoutException(ClimateProductType.class);

    /**
     * Localization Level for "Climate" - always read/write on SITE level.
     */
    private static final LocalizationLevel CLIMATE_LOCALIZATION_LEVEL = LocalizationLevel.SITE;

    /**
     * Map of available climate product types at CLIMATE_LOCALIZATION_LEVEL.
     */
    private Map<String, ClimateProductType> productTypeMap;

    /**
     * Map of climate product types at BASE level (for default) .
     */
    private Map<String, ClimateProductType> baseProductTypeMap;

    /**
     * MonitorConfigListener.
     */
    private Set<MonitorConfigListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Singleton instance of this class
     */
    private static ClimateProductTypeManager instance;

    /**
     * Flags if the product types have been loaded.
     */
    protected boolean isPopulated;

    /**
     * Private Constructor
     */
    private ClimateProductTypeManager() {
        isPopulated = false;
        loadProductTypes();
    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     */
    public synchronized static ClimateProductTypeManager getInstance() {
        if (instance == null) {
            instance = new ClimateProductTypeManager();
        }

        return instance;
    }

    /**
     * Add MonitorConfigListener.
     * 
     * @param fl
     *            MonitorConfigListener
     */
    public void addListener(MonitorConfigListener fl) {
        listeners.add(fl);
    }

    /**
     * Remove MonitorConfigListener.
     * 
     * @param fl
     *            MonitorConfigListener
     */
    public void removeListener(MonitorConfigListener fl) {
        listeners.remove(fl);
    }

    /**
     * @return the isPopulated
     */
    public boolean isPopulated() {
        return isPopulated;
    }

    /**
     * Loads all product types from localization.
     */
    private void loadProductTypes() {

        // Initialize or clean up.
        if (baseProductTypeMap == null) {
            baseProductTypeMap = new HashMap<>();
        } else {
            baseProductTypeMap.clear();
        }

        if (productTypeMap == null) {
            productTypeMap = new HashMap<>();
        } else {
            productTypeMap.clear();
        }

        // Load BASE level product types for default.
        loadProductTypes(LocalizationLevel.BASE, baseProductTypeMap);

        // Load CLIMATE_LOCALIZATION_LEVEL (SITE) product types.
        loadProductTypes(CLIMATE_LOCALIZATION_LEVEL, productTypeMap);

        // Comment this out to force re-load by default.
        // isPopulated = true;
    }

    /**
     * Loads product types from a given localization level into a Map.
     *
     * @param level
     *            LocalizationLevel to look at
     * @param cptMap
     *            Map to hold product types
     */
    private void loadProductTypes(LocalizationLevel level,
            Map<String, ClimateProductType> cptMap) {

        // Find a list of xml files on "level".
        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                level);

        ILocalizationFile[] lfs = pm.listStaticFiles(
                new LocalizationContext[] { lc }, SETUP_ROOT,
                new String[] { FILE_NAME_EXT }, false, true);

        // Read all product types on "level".
        for (ILocalizationFile lf : lfs) {
            ClimateProductType ptyp = readClimateProductType(lf);
            if (ptyp != null) {
                cptMap.put(ptyp.getName(), ptyp);
            }
        }

    }

    /**
     * Read directly from a product type file in localization store.
     * 
     * Note: the input file name should be a pure file name without path.
     * 
     * @param fname
     *            File name without path
     * 
     * @return ClimateProductType
     * 
     */
    public ClimateProductType readClimateProductType(String fname)
            throws SerializationException {
        ClimateProductType ptyp = null;
        if (FILE_NAME_PATTERN.matcher(fname).matches()) {
            IPathManager pm = PathManagerFactory.getPathManager();

            String fullName = SETUP_ROOT + fname;
            ILocalizationFile lf = pm.getStaticLocalizationFile(
                    LocalizationType.COMMON_STATIC, fullName);

            pm.addLocalizationPathObserver(fullName, this);

            ptyp = readClimateProductType(lf);
        }

        return ptyp;
    }

    /**
     * Read a product type file.
     * 
     * @param lf
     *            ILocalizationFile
     * @return ClimateProductType
     * 
     */
    private ClimateProductType readClimateProductType(ILocalizationFile lf) {

        ClimateProductType ptyp = null;

        String fpath = lf.getPath();
        String fname = fpath
                .substring(fpath.lastIndexOf(IPathManager.SEPARATOR) + 1);

        if (FILE_NAME_PATTERN.matcher(fname).matches()) {

            try (InputStream is = lf.openInputStream()) {
                ptyp = jaxb.unmarshalFromInputStream(is);
            } catch (Exception ee) {
                logger.error("Error unmarshalling product type " + lf.getPath(),
                        ee);
            }
        }

        return ptyp;

    }

    /**
     * Check if a product type file exists on CLIMATE_LOCALIZATION_LEVEL.
     * 
     * @param ptyp
     *            ClimateProductType
     * @return isTypeOnClimateLocalizationLevel() boolean
     */
    public boolean isTypeOnClimateLocalizationLevel(ClimateProductType ptyp) {

        String fname = ptyp.getFileName();
        if (fname == null) {
            fname = ptyp.getPreferedFileName();
        }

        String fileName = SETUP_ROOT + fname;

        // Check if the file exists on CLIMATE_LOCALIZATION_LEVEL.
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile prdTypesFile = pm.getStaticLocalizationFile(fileName);

        return isClimateLocalizationLevelFile(prdTypesFile);
    }

    /**
     * Check if a localization file is a CLIMATE_LOCALIZATION_LEVEL file.
     * 
     * @param lf
     *            ILocalizationFile
     * @return isClimateLocalizationLevelFile() boolean
     */
    private boolean isClimateLocalizationLevelFile(ILocalizationFile lf) {
        return (lf != null && lf.getContext()
                .getLocalizationLevel() == CLIMATE_LOCALIZATION_LEVEL);
    }

    /**
     * Save a product type into a localization file at
     * CLIMATE_LOCALIZATION_LEVEL.
     * 
     * @param ptyp
     *            ClimateProductType
     */
    public void saveProductType(ClimateProductType ptyp) {

        String fileName = SETUP_ROOT + ptyp.getFileName();

        // Check if the file exists in any localization level.
        IPathManager pm = PathManagerFactory.getPathManager();
        ILocalizationFile prdTypesFile = pm.getStaticLocalizationFile(fileName);

        /*
         * Create the file if it does not exists or it is not a
         * CLIMATE_LOCALIZATION_LEVEL file.
         */
        if (!isClimateLocalizationLevelFile(prdTypesFile)) {
            LocalizationContext lc = pm.getContext(
                    LocalizationType.COMMON_STATIC, CLIMATE_LOCALIZATION_LEVEL);
            prdTypesFile = pm.getLocalizationFile(lc, fileName);
        }

        // Save to the product type to XML file.
        try (SaveableOutputStream sos = prdTypesFile.openOutputStream()) {
            jaxb.marshalToStream(ptyp, sos);
            sos.save();
            productTypeMap.put(ptyp.getName(), ptyp);
        } catch (Exception e) {
            logger.error("Couldn't save product type file: ", fileName, e);
        }

    }

    /**
     * Delete a product type file from Localization store.
     * 
     * @param ptyp
     *            ClimateProductType
     */
    public void deleteProductType(ClimateProductType ptyp) {

        IPathManager pm = PathManagerFactory.getPathManager();
        String fname = ptyp.getFileName();
        ILocalizationFile lf = pm.getStaticLocalizationFile(SETUP_ROOT + fname);

        try {
            lf.delete();
            productTypeMap.remove(ptyp.getName());
        } catch (LocalizationException e) {
            logger.error("Couldn't delete product type file: ", fname, e);
        }

    }

    /**
     * File change monitor.
     * 
     * @param lfile
     *            ILocalizationFile
     */
    @Override
    public void fileChanged(ILocalizationFile lfile) {
        if (lfile.getPath().contains(SETUP_ROOT)) {
            ClimateProductType ptyp = readClimateProductType(lfile);
            if (ptyp != null) {
                productTypeMap.put(ptyp.getName(), ptyp);
            }

            // inform listeners
            for (MonitorConfigListener fl : listeners) {
                fl.configChanged(new MonitorConfigEvent(this));
            }
        }
    }

    /**
     * @return the productTypeMap
     */
    public Map<String, ClimateProductType> getProductTypeMap() {
        if (!this.isPopulated) {
            loadProductTypes();
        }

        return productTypeMap;
    }

    /**
     * @param productTypeMap
     *            the productTypeMap to set
     */
    public void setProductTypeMap(
            Map<String, ClimateProductType> productTypeMap) {
        this.productTypeMap = productTypeMap;
    }

    /**
     * @return the baseProductTypeMap
     */
    public Map<String, ClimateProductType> getBaseProductTypeMap() {
        if (!this.isPopulated) {
            loadProductTypes();
        }

        return baseProductTypeMap;
    }

    /**
     * Get a list of report types from all existing product types.
     * 
     * @return List<String>
     */
    public List<String> getReportTypes() {
        List<String> rtypes = new ArrayList<>();
        for (ClimateProductType ptyp : productTypeMap.values()) {
            String type = ptyp.getReportType().getPeriodDescriptor();
            if (!rtypes.contains(type)) {
                rtypes.add(type);
            }
        }

        rtypes.sort(null);

        return rtypes;
    }

    /**
     * Get a list of product IDs for a given report type.
     * 
     * @param rtype
     *            report type
     * 
     * @return List<String>
     */
    public List<String> getProdIDs(String rtype) {
        List<String> pid = new ArrayList<>();
        for (ClimateProductType ptyp : productTypeMap.values()) {

            if (rtype.equals(ptyp.getReportType().getPeriodDescriptor())) {
                String prodId = ptyp.getProdId();
                String source = ptyp.getReportType().getSource();
                String nid = prodId + FILE_NAME_SEPARATOR + source;
                if (!pid.contains(prodId) && !pid.contains(nid)) {
                    pid.add(prodId);
                } else {
                    if (source.equals(PeriodType.CLIMATE_SOURCES[0])) {
                        source = PeriodType.CLIMATE_SOURCES[1];
                    } else {
                        source = PeriodType.CLIMATE_SOURCES[0];
                    }

                    String newId = prodId + FILE_NAME_SEPARATOR + source;

                    pid.remove(prodId);
                    pid.add(newId);
                    pid.add(nid);
                }
            }
        }

        pid.sort(null);

        return pid;
    }

    /**
     * Get a product type by report period, source and id
     *
     * @param rtype
     *            String am|im|pm|mon|sea|ann
     * @param prodID
     *            String
     * @param source
     *            String, NWR or NWWS
     * 
     * @return ClimateProductType
     */
    public ClimateProductType getProductType(String rtype, String source,
            String prodID) {
        return getProductTypeMap()
                .get(ClimateProductType.buildName(rtype, source, prodID));
    }

    /**
     * Get a product type by name ([periodType]_[prodId]_[source])
     *
     * @param name
     *            String
     * @return ClimateProductType
     */
    public ClimateProductType getTypeByName(String name) {
        return getProductTypeMap().get(name);
    }

    /**
     * Get a BASE product type by name ([periodType]_[prodId]_[source])
     *
     * @param name
     *            String
     * @return ClimateProductType
     */
    public ClimateProductType getBaseTypeByName(String name) {
        return getBaseProductTypeMap().get(name);
    }

    /**
     * Get a list of product type by report period descriptor.
     *
     * @param peridDsp
     *            String
     * @return getTypesByPeriodDescripter() List of ClimateProductType
     */
    public List<ClimateProductType> getTypesByPeriodDescripter(
            String peridDsp) {

        List<ClimateProductType> ptyps = new ArrayList<>();
        for (ClimateProductType ptyp : getProductTypeMap().values()) {
            if (ptyp.getReportType().getPeriodDescriptor().equals(peridDsp)) {
                ptyps.add(ptyp);
            }
        }

        return ptyps;

    }

    /**
     * Get a list of product type by report period name.
     *
     * @param periodName
     *            String
     * @return getTypesByPeriodName() List of ClimateProductType
     */
    public List<ClimateProductType> getTypesByPeriodName(String periodName) {

        List<ClimateProductType> ptyps = new ArrayList<>();
        for (ClimateProductType ptyp : getProductTypeMap().values()) {
            if (ptyp.getReportType().getPeriodName().equals(periodName)) {
                ptyps.add(ptyp);
            }
        }

        return ptyps;
    }

    /**
     * Get a list of product type by report PeriodType.
     *
     * @param periodType
     *            String
     * @return getTypesByPeriodName() List of ClimateProductType
     */
    public List<ClimateProductType> getTypesByPeriodType(
            PeriodType periodType) {

        List<ClimateProductType> ptyps = new ArrayList<>();
        for (ClimateProductType ptyp : getProductTypeMap().values()) {
            if (ptyp.getReportType() == periodType) {
                ptyps.add(ptyp);
            }
        }

        return ptyps;
    }

}
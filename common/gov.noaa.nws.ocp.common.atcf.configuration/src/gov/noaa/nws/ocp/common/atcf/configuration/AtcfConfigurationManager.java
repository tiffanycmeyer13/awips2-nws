/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.serialization.jaxb.JaxbDummyObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfSitePreferences.PreferenceOptions;

/**
 * Class to manage ATCF configuration files.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2018 52692      jwu         Created.
 * Sep 14, 2018 54781      jwu         Added sidebar configuration.
 * Oct 18, 2018 55963      jwu         Save config info in the same format 
 *                                     as in legacy and applied for obj aids
 *                                     profiles.
 * Nov 22, 2018 57338      dmanzella   Added support for saving color files
 * Feb 15, 2019 59913      dmanzella   Added support for saving preference configuration files
 * Feb 26, 2019 59910      dmanzella   Added support for saving CPA location files
 * Apr 04, 2019 62029      jwu         Added defaults for atcf site preference.
 * May 27, 2019 63379      dmanzella   Added support for fix microwave & scatterometer satellite types
 * Jun 04, 2019 64494      dmanzella   Added support for fix error
 * Feb 21, 2020 71724      jwu         Added gust.dat.
 * Mar 01, 2020 71720      wpaintsil   Add methods for text product formatting.
 * Mar 24, 2020 76600      dfriedman   Improve product formatting localization support.
 * Apr 23, 2020 72252      jwu         Convert plain text configuration into XML format
 * Aug 12, 2020 76541      mporricelli Add getEnvConfig()
 * Sep 08, 2020 82576      jwu         Convert geography points & forecaster initials.
 * Oct 27, 2020 82623      jwu         Add master storm.table.
 * Jan 22, 2021 86476      jwu         Add ATCF offices & full initials.
 * Feb 22, 2021 87890      dfriedman   Add color change notification.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AtcfConfigurationManager {

    private static final String INVALID_COLOR_MSG = "AtcfConfigurationManager - invalid color selection entry: ";

    private static final String SAVE_ERROR_MSG = "AtcfConfigurationManager couldn't save configuration file: ";

    /**
     * Root for ATCF configuration in Localization.
     */
    private static final String ATCF_CONFIG_ROOT = "atcf/config"
            + IPathManager.SEPARATOR;

    /**
     * Root for ATCF configuration for objective aids profiles.
     */
    private static final String ATCF_OBJ_AIDS_PROFILE_ROOT = ATCF_CONFIG_ROOT
            + "profile" + IPathManager.SEPARATOR;

    private static final String ATCF_TEMPLATE_ROOT = ATCF_CONFIG_ROOT
            + "templates" + IPathManager.SEPARATOR;

    public static final String ATCF_JSON_ROOT = ATCF_CONFIG_ROOT + "json"
            + IPathManager.SEPARATOR;

    private static AtcfEnvironmentConfig envConfig = null;

    /**
     * Path for geography points files
     */
    private static final String GEOGRAPHY_POINTS_PATH = LocalizationUtil
            .join("atcf", "config", "geography") + IPathManager.SEPARATOR;

    /**
     * Path for ATCF environment/properties file
     */
    private static final String ATCF_PROPS_PATH = LocalizationUtil.join("atcf",
            "config", "atcf.properties");

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfConfigurationManager.class);

    /**
     * JAXB manager for marshal/unmarshal.
     */
    private static final JAXBManager jaxb = buildJaxbManager();

    /**
     * Path manager.
     */
    private IPathManager pm = PathManagerFactory.getPathManager();

    /**
     * Legacy file names for ATCF configuration files. When converting these
     * files into XML versions, for files ending with ".dat", ".dat" will be
     * replaced with ".xml"; for other file names, "xml" is added as the suffix.
     */
    private static final String COLOR_TABLE_FILE = "colortable.dat";

    private static final String MICRO_SAT_FILE = "microsattypes.dat";

    private static final String SCAT_SAT_FILE = "scatsattypes.dat";

    private static final String COLOR_SELECTION_FILE = "colsel.dat";

    private static final String FIX_SITES_FILE = "fixsites.dat";

    private static final String FIX_TYPES_FILE = "fixtypes.dat";

    private static final String CPA_LOCATIONS_FILE = "cpa.loc";

    private static final String PREFERENCES_FILE = "atcfsite.prefs";

    private static final String DEFAULT_AIDS_FILE = "default_aids.dat";

    private static final String TECH_LIST_FILE = "techlist.dat";

    private static final String FIX_ERROR_FILE = "fixerror.prefs";

    private static final String SIDEBAR_SELECTION_FILE = "sidebar_selections.xml";

    private static final String GUST_FILE = "gust.dat";

    private static final String FORECASTER_INITIALS_FILE = "initials.dat";

    // Files contains all forecasters' initial & full name.
    private static final String FULL_INITIALS_FILE = "initials_full.dat";

    // Files for geography points - one per basin, "bb" is basin id.
    private static final String GEOGRAPHY_AL_FILE = "geography_bb.dat";

    // Known basins with geography points
    private static final String[] basins = new String[] { "AL", "WP", "CP",
            "EP" };

    // Storm states for advisory composition's forecast type
    private static final String STORM_STATES_FILE = "stormstates.xml";

    // All storms from 1851 to present.
    private static final String STORM_TABLE_FILE = "storm.table";

    // ATCF sites for advisory composition.
    private static final String ATCF_SITES_FILE = "atcfsites.xml";

    /**
     * Data separators in ATCF configuration files.
     */
    private static final String START_OF_DATA = "START_OF_DATA";

    private static final String TECH_LIST_START_OF_DATA = "NUM";

    private static final String CPA_LOCATIONS_START_OF_DATA = "";

    private static final String WHITE_SPACE_SEPARATOR = "\\s+";

    private static final String COLON_SEPARATOR = ":";

    private static final String COMMENT_INDICATOR = "#";

    private static final String OBJ_AIDS_PROFILE_EXT = ".aids";

    private static final String DAT_EXTENSION = ".dat";

    private static final String XML_EXTENSION = ".xml";

    /**
     * A common indicator in legacy ATCF configuration files for where actual
     * data is about to start. The lines above it are considered the header part
     * of the file. When saving a configuration object, this part should be
     * preserved. The lines starting from this indicator should be generated
     * from the object's toString() method.
     */
    protected static final String DELETION_WARNING = "DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.\n"
            + START_OF_DATA + "\n\n";

    private static final String END_OF_HEADER = "DO NOT DELETE";

    private static final String CPA_END_OF_HEADER = "#";

    /**
     * Singleton instance of this class
     */
    private static AtcfConfigurationManager instance;

    // ATCF custom colors.
    private AtcfCustomColors atcfCustomColors;

    // ATCF custom colors.
    private AtcfColorSelections atcfColorSelections;

    private CopyOnWriteArraySet<IColorConfigurationChangedListener> colorConfigurationChangeListeners = new CopyOnWriteArraySet<>();

    /**
     * Private Constructor
     */
    private AtcfConfigurationManager() {
    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     */
    public static synchronized AtcfConfigurationManager getInstance() {
        if (instance == null) {
            instance = new AtcfConfigurationManager();
        }

        return instance;
    }

    /**
     * Build a JaxbManager to marshal/unmarshal of classes that holds ATCF
     * configurations.
     *
     * @return a new JAXBManager for ATCF configuration
     */
    private static JAXBManager buildJaxbManager() {

        /**
         * JAXB manager for marshal/unmarshal.
         */
        Class<?>[] clzz = { JaxbDummyObject.class, FixSites.class,
                FixTypes.class, ObjectiveAidTechniques.class,
                DefaultObjAidTechniques.class, AtcfSitePreferences.class,
                AtcfColorSelections.class, AtcfCustomColors.class,
                CpaLocations.class, SidebarMenuSelection.class, FixError.class,
                FixMicrowaveSatelliteTypes.class, FixScatSatTypes.class,
                MaxWindGustPairs.class, GeographyPoints.class,
                ForecasterInitials.class, StormStates.class, AtcfSites.class,
                FullInitials.class };

        JAXBManager jaxb = null;

        try {
            jaxb = new JAXBManager(clzz);
        } catch (JAXBException e) {
            logger.error(
                    "AtcfConfigurationManager: Error initializing JaxbManager due to ",
                    e);
        }

        return jaxb;
    }

    /**
     * @return the jaxb
     */
    public static JAXBManager getJaxb() {
        return jaxb;
    }

    /**
     * Load all ATCF configurations.
     */
    public void loadConfiguration() {

        getPreferences();
        getObjectiveAidTechniques();
        getCpaLocations();

        getFixError();
        getFixSites();
        getFixTypes();
        getMicroSat();
        getScatSat();
        getDefaultObjAidTechniques();
        getAtcfCustomColors();
        getAtcfColorSelections();
        getMaxWindGustPairs();
        getStormStates();
        getForecasterInitials();
        getAtcfSites();
        getFullInitials();

        // Load all geography points.
        for (String bsn : basins) {
            getBasinGeoPoints(bsn);
        }

        // Load all obj aids profiles.
        String[] profiles = getObjAidsProfileNames();
        for (String profile : profiles) {
            getSiteObjAidsProfile(profile);
        }
    }

    /**
     * Get list of Preferences from the plain-text legacy file.
     * 
     * @return list of preferences
     */
    public AtcfSitePreferences getPreferencesFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                PREFERENCES_FILE, START_OF_DATA, COLON_SEPARATOR,
                COMMENT_INDICATOR);

        AtcfSitePreferences preferences = new AtcfSitePreferences();

        // Load preferences from "atcfsite.prefs" file.
        for (String[] item : dataItems) {
            if (item.length > 1) {
                preferences.getPreferences().add(
                        new AtcfSitePreferenceEntry(item[0], item[1].trim()));
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid preference entry: "
                                + item[0]);
            }
        }

        // Fill with default values for preferences not found in file.
        for (PreferenceOptions ps : PreferenceOptions.values()) {
            AtcfSitePreferenceEntry pEntry = preferences
                    .getPreference(ps.toString());

            if (pEntry == null) {
                pEntry = new AtcfSitePreferenceEntry(ps);
                preferences.getPreferences().add(pEntry);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, PREFERENCES_FILE,
                END_OF_HEADER);
        preferences.setHeader(header);

        return preferences;
    }

    /**
     * Get the list of CpaLocations
     * 
     * @return list of CpaLocations
     */
    public CpaLocations getCpaLocationsFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                CPA_LOCATIONS_FILE, CPA_LOCATIONS_START_OF_DATA,
                WHITE_SPACE_SEPARATOR, COMMENT_INDICATOR);

        CpaLocations cpaLocations = new CpaLocations();

        for (String[] item : dataItems) {
            if (item.length > 3) {
                int tempPriority = 0;
                float tempLat = 0.0f;
                float tempLon = 0.0f;
                try {
                    tempPriority = Integer.parseInt(item[3]);
                    tempLat = Float.parseFloat(
                            item[0].substring(0, item[0].length() - 1));
                    if (item[0].endsWith("S")) {
                        tempLat *= -1;
                    }
                    tempLon = Float.parseFloat(
                            item[1].substring(0, item[1].length() - 1));
                    if (item[1].endsWith("W")) {
                        tempLon *= -1;
                    }

                    cpaLocations.getCpaLocations().put(item[2],
                            new CpaLocationEntry(tempLat, tempLon, item[2],
                                    tempPriority));

                } catch (NumberFormatException e) {
                    logger.warn(
                            "AtcfConfigurationManager - invalid CPA Location entry: "
                                    + item[2]);
                }
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid CPA Location entry: "
                                + item[2]);
            }
        }

        // Load header information
        String header = getCpaFileHeader(CPA_END_OF_HEADER);
        cpaLocations.setHeader(header);

        return cpaLocations;
    }

    /**
     * Get the list of Fix Error from fixerror.prefs.
     * 
     * @return list of Fix Error
     */
    public FixError getFixErrorFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                FIX_ERROR_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        FixError error = new FixError();
        for (String[] item : dataItems) {

            if (item.length > 11) {
                List<Integer> posit = new ArrayList<>();
                posit.add(Integer.parseInt(item[2]));
                posit.add(Integer.parseInt(item[3]));
                posit.add(Integer.parseInt(item[4]));

                ArrayList<Integer> intens = new ArrayList<>();
                intens.add(Integer.parseInt(item[5]));
                intens.add(Integer.parseInt(item[6]));
                intens.add(Integer.parseInt(item[7]));

                ArrayList<Integer> radii = new ArrayList<>();
                radii.add(Integer.parseInt(item[8]));
                radii.add(Integer.parseInt(item[9]));
                radii.add(Integer.parseInt(item[10]));

                boolean retired = !"0".equals(item[11]);

                error.getFixError().add(new FixErrorEntry(item[0], item[1],
                        posit, intens, radii, retired));

            } else if ("track_fitting_algorithm:".equals(item[0])
                    || "int___fitting_algorithm:".equals(item[0])
                    || "rad___fitting_algorithm:".equals(item[0])
                    || "type_site".equals(item[0])) {
                // These lines represent deprecated data,
                // legacy says they should be set in a different GUI

            } else {

                logger.warn(
                        "AtcfConfigurationManager - invalid fix error entry: "
                                + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, FIX_ERROR_FILE,
                END_OF_HEADER);
        error.setHeader(header);

        return error;
    }

    /**
     * Get the list of fix sites from fixsites.dat.
     *
     * @return list of fix sites.
     */
    public FixSites getFixSitesFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                FIX_SITES_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        FixSites sites = new FixSites();
        for (String[] item : dataItems) {
            if (item.length > 1) {
                boolean retired = !"0".equals(item[1]);
                sites.getFixSites().add(new FixSiteEntry(item[0], retired));
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid fix site entry: "
                                + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, FIX_SITES_FILE,
                END_OF_HEADER);
        sites.setHeader(header);

        return sites;
    }

    /**
     * Get the list of fix types from fixtypes.dat.
     *
     * @return list of fix types.
     */
    public FixTypes getFixTypesFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                FIX_TYPES_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        FixTypes types = new FixTypes();
        for (String[] item : dataItems) {
            if (item.length > 1) {
                boolean retired = !"0".equals(item[1]);
                types.getFixTypes().add(new FixTypeEntry(item[0], retired));
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid fix type entry: "
                                + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, FIX_TYPES_FILE,
                END_OF_HEADER);
        types.setHeader(header);

        return types;
    }

    /**
     * Get the list of microwave satellites from microsattypes.dat.
     *
     * @return list of microwave satellites.
     */
    public FixMicrowaveSatelliteTypes getMicroSatFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                MICRO_SAT_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        FixMicrowaveSatelliteTypes types = new FixMicrowaveSatelliteTypes();
        for (String[] item : dataItems) {
            if (item.length >= 1) {
                types.getMicroSats().add(item[0]);
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid microwave satellite entry: "
                                + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, MICRO_SAT_FILE,
                END_OF_HEADER);
        types.setHeader(header);

        return types;
    }

    /**
     * Get the list of scatterometer satellites from scatsattypes.dat.
     *
     * @return list of scatterometer satellites.
     */
    public FixScatSatTypes getScatSatFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT, SCAT_SAT_FILE,
                START_OF_DATA, WHITE_SPACE_SEPARATOR, COMMENT_INDICATOR);

        FixScatSatTypes types = new FixScatSatTypes();
        for (String[] item : dataItems) {
            if (item.length >= 1) {
                types.getScatSats().add(item[0]);
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid scatterometer satellite entry: "
                                + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, SCAT_SAT_FILE,
                END_OF_HEADER);
        types.setHeader(header);

        return types;
    }

    /**
     * Get the list of default objective aid techniques from default_aids.dat.
     *
     * @return list of default objective aid techniques.
     */
    public DefaultObjAidTechniques getDefaultObjAidTechniquesFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                DEFAULT_AIDS_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        DefaultObjAidTechniques aids = new DefaultObjAidTechniques();
        for (String[] item : dataItems) {
            if (item.length > 1) {
                aids.getDefaultObjAidTechniques().add(
                        new DefaultObjAidTechEntry(item[0], combine(item, 1)));
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid default obj aid entry: "
                                + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, DEFAULT_AIDS_FILE,
                END_OF_HEADER);
        aids.setHeader(header);

        return aids;
    }

    /**
     * Get the list of ATCF custom colors from colortable.dat.
     *
     * @return list of ATCF custom color definition.
     */
    public AtcfCustomColors getAtcfCustomColorsFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                COLOR_TABLE_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        AtcfCustomColors colors = new AtcfCustomColors();
        for (String[] item : dataItems) {
            if (item.length > 0 && item[0].length() > 5) {
                colors.getColors().add(item[0]);
            } else {
                logger.warn("AtcfConfigurationManager - invalid color entry: "
                        + item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, COLOR_TABLE_FILE,
                END_OF_HEADER);
        colors.setHeader(header);

        return colors;
    }

    /**
     * Get the list of ATCF color selections from colsel.dat.
     *
     * @return list of ATCF color selections.
     */
    public AtcfColorSelections getAtcfColorSelectionsFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                COLOR_SELECTION_FILE, START_OF_DATA, COLON_SEPARATOR,
                COMMENT_INDICATOR);

        AtcfColorSelections colors = new AtcfColorSelections();

        // Parse color selections for MAPDSPLY
        List<String[]> mapColors = getDataByColorSelectionGroup(dataItems,
                ColorSelectionGroup.MAPDSPLY);

        for (String[] item : mapColors) {
            if (item.length > 1) {
                String[] clrs = item[1].trim().split(WHITE_SPACE_SEPARATOR);

                int[] indexes = new int[clrs.length];
                try {
                    int ii = 0;
                    for (String ss : clrs) {
                        indexes[ii] = Integer.parseInt(ss);
                        ii++;
                    }
                } catch (NumberFormatException ee) {
                    logger.warn(INVALID_COLOR_MSG + combine(item, 0));
                }

                colors.getMapDisplayColors()
                        .add(new ColorSelectionEntry(item[0].trim(), indexes));

            } else {
                logger.warn(INVALID_COLOR_MSG + combine(item, 0));
            }
        }

        // Parse color selections for PRINTER
        List<String[]> printerColors = getDataByColorSelectionGroup(dataItems,
                ColorSelectionGroup.PRINTER);

        for (String[] item : printerColors) {
            if (item.length > 1) {
                String[] clrs = item[1].trim().split(WHITE_SPACE_SEPARATOR);
                int[] indexes = new int[clrs.length];
                try {
                    int ii = 0;
                    for (String ss : clrs) {
                        indexes[ii] = Integer.parseInt(ss);
                        ii++;
                    }
                } catch (NumberFormatException ee) {
                    logger.warn(INVALID_COLOR_MSG + combine(item, 0));
                }

                colors.getPrinterColors()
                        .add(new ColorSelectionEntry(item[0].trim(), indexes));

            } else {
                logger.warn(INVALID_COLOR_MSG + combine(item, 0));
            }
        }

        // Parse color selections for PLOTTER
        List<String[]> plotterColors = getDataByColorSelectionGroup(dataItems,
                ColorSelectionGroup.PLOTTER);

        for (String[] item : plotterColors) {
            if (item.length > 1) {
                String[] clrs = item[1].trim().split(WHITE_SPACE_SEPARATOR);
                int[] indexes = new int[clrs.length];
                try {
                    int ii = 0;
                    for (String ss : clrs) {
                        indexes[ii] = Integer.parseInt(ss);
                        ii++;
                    }
                } catch (NumberFormatException ee) {
                    logger.warn(INVALID_COLOR_MSG + combine(item, 0));
                }

                colors.getPlotterColors()
                        .add(new ColorSelectionEntry(item[0].trim(), indexes));

            } else {
                logger.warn(INVALID_COLOR_MSG + combine(item, 0));
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT, COLOR_SELECTION_FILE,
                END_OF_HEADER);
        colors.setHeader(header);

        return colors;
    }

    /**
     * Get the list of objective aid techniques.
     *
     * @return list of objective aid techniques.
     */
    public ObjectiveAidTechniques getObjAidTechFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                TECH_LIST_FILE, TECH_LIST_START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        ObjectiveAidTechniques aids = new ObjectiveAidTechniques();
        for (String[] item : dataItems) {
            if (item.length > 6) {
                try {
                    int num = Integer.parseInt(item[0]);
                    int color = Integer.parseInt(item[4]);

                    boolean errs = !"0".equals(item[2]);
                    boolean retired = !"0".equals(item[3]);
                    boolean aidDflt = !"0".equals(item[5]);
                    boolean intDflt = !"0".equals(item[6]);
                    boolean radiiDflt = !"0".equals(item[7]);

                    String desc = "";
                    if (item.length > 8) {
                        desc = combine(item, 8);
                    }

                    aids.getObjectiveAidTechniques()
                            .add(new ObjectiveAidTechEntry(num, item[1], errs,
                                    retired, color, aidDflt, intDflt, radiiDflt,
                                    desc));
                } catch (NumberFormatException ee) {
                    logger.warn(
                            "AtcfConfigurationManager - invalid obj aid entry: "
                                    + combine(item, 0));
                }
            } else {
                logger.warn("AtcfConfigurationManager - invalid obj aid entry: "
                        + combine(item, 0));
            }
        }

        // Load header information
        String header = getTechFileHeader();
        aids.setHeader(header);

        return aids;
    }

    /**
     * Get the list of maximum wind/gust pairs from gust.dat.
     *
     * @return list of maximum wind/gust pairs..
     */
    public MaxWindGustPairs getMaxWindGustPairsFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT, GUST_FILE,
                START_OF_DATA, WHITE_SPACE_SEPARATOR, COMMENT_INDICATOR);

        MaxWindGustPairs gustPairs = new MaxWindGustPairs();
        for (String[] item : dataItems) {
            if (item.length > 1) {
                try {
                    int maxWnd = Integer.parseInt(item[0]);
                    int gust = Integer.parseInt(item[1]);

                    gustPairs.getGustPairs()
                            .add(new GustPairEntry(maxWnd, gust));
                } catch (NumberFormatException ne) {
                    logger.warn(
                            "AtcfConfigurationManager - invalid maximum wind/gust pair: "
                                    + item[0] + "   " + item[1]);
                }
            } else {
                logger.warn(
                        "AtcfConfigurationManager - maximum wind/gust pair: "
                                + item[0]);
            }
        }

        String header = readFileHeader(ATCF_CONFIG_ROOT, GUST_FILE,
                END_OF_HEADER);
        gustPairs.setHeader(header);

        saveConfig(GUST_FILE, gustPairs);

        return gustPairs;
    }

    /**
     * Get the list of geography points from geography_bb.dat, where "bb" is the
     * basin id.
     * 
     * @param File
     *            file name
     *
     * @return list of geography points.
     */
    public GeographyPoints getGeographyPointsFromText(String file) {
        List<String[]> dataItems = getDataItems(GEOGRAPHY_POINTS_PATH, file, "",
                WHITE_SPACE_SEPARATOR, COMMENT_INDICATOR);

        // Item format is like "22.5N 79.5W CAIBARIEN CUBA"
        GeographyPoints pts = new GeographyPoints();
        for (String[] item : dataItems) {
            if (item.length > 2) {
                try {
                    int llg = item[0].length();
                    float lat = Float.parseFloat(item[0].substring(0, llg - 1));
                    if (item[0].endsWith("S")) {
                        lat = -lat;
                    }

                    int lln = item[1].length();
                    float lon = Float.parseFloat(item[1].substring(0, lln - 1));
                    if (item[1].endsWith("W")) {
                        lon = -lon;
                    }

                    // Capitalize from all upper case to support mixed case.
                    StringBuilder name = new StringBuilder();
                    for (int ii = 2; ii < item.length; ii++) {
                        name.append(
                                StringUtils.capitalize(item[ii].toLowerCase()));
                        if (ii < (item.length - 1)) {
                            name.append(" ");
                        }
                    }

                    GeographyPoint pt = new GeographyPoint(lat, lon,
                            name.toString());
                    pts.getGeoPoints().add(pt);
                } catch (NumberFormatException e) {
                    logger.warn(
                            "AtcfConfigurationManager - invalid geography point: "
                                    + item[0] + "," + item[1]);
                }
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid geography point: "
                                + item[0] + "," + item[1]);
            }
        }

        return pts;
    }

    /**
     * Get the list of ForecastInitials from initials.dat.
     *
     * @return list of ForecastInitials.
     */
    public ForecasterInitials getForecasterInitialsFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                FORECASTER_INITIALS_FILE, START_OF_DATA, WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        ForecasterInitials inis = new ForecasterInitials();
        for (String[] item : dataItems) {
            if (item.length > 0) {
                inis.getInitials().add(item[0]);
            }
        }

        // Load header information
        String header = getFileHeader(ATCF_CONFIG_ROOT,
                FORECASTER_INITIALS_FILE, END_OF_HEADER);
        inis.setHeader(header);

        return inis;
    }

    /*
     * Get an object instantiated from a given file from ATCF_CONFIG_ROOT.
     *
     * @param file The file name - xml file name or the original file name.
     *
     * @return Object An object instantiated from the given XML file.
     */
    private Object getConfigXmlObject(String file) {
        String xmlFile = file;
        if (!file.endsWith(XML_EXTENSION)) {
            xmlFile = getXmlFileName(file);
        }

        return getXmlObject(xmlFile, ATCF_CONFIG_ROOT);
    }

    /*
     * Get an object instantiated from a given XML file from
     * ATCF_OBJ_AIDS_PROFILE_ROOT.
     *
     * @return Object An object instantiated from the given XML file.
     */
    private Object getProfileXmlObject(String xmlFile) {
        return getXmlObject(xmlFile, ATCF_OBJ_AIDS_PROFILE_ROOT);
    }

    /*
     * Get an object instantiated from a given XML file from
     * GEOGRAPHY_POINTS_PATH.
     *
     * @return Object An object instantiated from the given XML file.
     */
    private Object getGeographyPointsXmlObject(String fl) {
        String file = fl;
        if (!file.endsWith(XML_EXTENSION)) {
            file = getXmlFileName(file);
        }

        return getXmlObject(file, GEOGRAPHY_POINTS_PATH);
    }

    /*
     * Get an object instantiated from a given XML file from (...atcf/config).
     *
     * @return Object An object instantiated from the given XML file.
     */
    private Object getXmlObject(String xmlFile, String path) {

        Object obj = null;

        String fullName = path + xmlFile;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fullName);

        if (lf != null) {
            try (InputStream is = lf.openInputStream()) {
                obj = jaxb.unmarshalFromInputStream(is);
            } catch (Exception ee) {
                logger.error(
                        "AtcfConfigurationManager: Error unmarshalling XML file: "
                                + lf.getPath(),
                        ee);
            }
        }

        return obj;
    }

    /*
     * Read the contents of a file into a list of strings (one line per string).
     * This is intended to read in the legacy text setup files for parsing.
     *
     * Note: blank lines are not included.
     *
     * @param path whole path of the file ending with IPathManager.Separator.
     * 
     * @param fileIn A file to be read.
     * 
     * @param remove Flag to remove empty string.
     * 
     * @return List<String> Content of the file.
     */
    private List<String> readFileAsList(String path, String fileIn,
            boolean remove) {

        List<String> list = new ArrayList<>();

        String fileName = path + fileIn;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fileName);

        if (lf != null) {

            try (InputStream is = lf.openInputStream()) {
                Stream<String> stream = new BufferedReader(
                        new InputStreamReader(is)).lines();
                list = stream.collect(Collectors.toList());
            } catch (Exception ee) {
                logger.error("AtcfConfigurationManager: Error reading file: "
                        + lf.getPath(), ee);
            }
        }

        // Remove potential empty strings, if requested.
        List<String> validList = new ArrayList<>();
        for (String str : list) {
            if (remove) {
                if (str.trim().length() > 0) {
                    validList.add(str.trim());
                }
            } else {
                validList.add(str);
            }
        }

        return validList;
    }

    /*
     * Read the file by line and split the data lines into a list of array of
     * strings.
     * 
     * Note: blank lines are not included.
     * 
     * @param path whole path of the file ending with IPathManager.Separator.
     *
     * @param fileIn A file to be read.
     * 
     * @param dataStart A string to indicate the start of data.
     * 
     * @param sep String used to split a line.
     * 
     * @param comment String to indicate a comment line.
     * 
     * @return List<String[]> List of data items.
     */
    private List<String[]> getDataItems(String path, String fileIn,
            String dataStart, String sep, String comment) {

        List<String> fileContent = readFileAsList(path, fileIn, true);

        List<String[]> dataItems = new ArrayList<>();

        // Find where the actual data starts.
        int startLine = -1;
        if (dataStart != null && !dataStart.isEmpty()) {
            startLine = 0;
            for (String str : fileContent) {
                if (str.startsWith(dataStart)) {
                    break;
                }

                startLine++;
            }
        }

        // Parse the actual data lines.
        int jj = 0;
        for (String str : fileContent) {

            // Ignore comment lines.
            if (jj > startLine && !str.startsWith(comment)) {
                String[] items = str.split(sep);
                if (items.length > 0) {
                    dataItems.add(items);
                }
            }

            jj++;
        }

        return dataItems;
    }

    /*
     * Read the file by line and return the header lines as a string.
     * 
     * Note: blank lines are included.
     * 
     * @param path whole path of the file ending with IPathManager.Separator.
     * 
     * @param fileIn A file to be read.
     * 
     * @param headerEnd A string to indicate the end of the header.
     * 
     * @return String header of file.
     */
    private String getFileHeader(String path, String fileIn, String headerEnd) {

        List<String> fileContent = readFileAsList(path, fileIn, false);

        StringBuilder sb = new StringBuilder();
        for (String str : fileContent) {
            if (!str.startsWith(headerEnd)) {
                sb.append(str);
                sb.append("\n");
            } else {
                break;
            }
        }

        return sb.toString();
    }

    /*
     * Read the file by line and return the header lines as a string.
     * 
     * Note: starting and ending blank lines are excluded.
     * 
     * @param path whole path of the file ending with IPathManager.Separator.
     * 
     * @param fileIn A file to be read.
     * 
     * @param headerEnd A string to indicate the end of the header.
     * 
     * @return String header of file.
     */
    private String readFileHeader(String path, String fileIn,
            String headerEnd) {

        List<String> fileContent = readFileAsList(path, fileIn, false);

        // Remove empty strings at beginning and at the end, if requested.
        List<String> validList = new ArrayList<>();
        boolean start = false;
        for (int ii = 0; ii < fileContent.size(); ii++) {
            String istr = fileContent.get(ii);
            if (!start && !istr.trim().isEmpty()) {
                start = true;
            }

            if (start) {
                if (istr.startsWith(headerEnd)) {
                    break;
                }

                validList.add(istr);
            }
        }

        List<String> validList1 = new ArrayList<>();
        boolean start1 = false;
        for (int ii = (validList.size() - 1); ii >= 0; ii--) {
            String istr = validList.get(ii);
            if (!start1 && !istr.trim().isEmpty()) {
                start1 = true;
            }

            if (start1) {
                validList1.add(istr);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int ii = (validList1.size() - 1); ii >= 0; ii--) {
            String str = validList1.get(ii);
            sb.append(str);
            sb.append("\n");
        }

        return sb.toString();
    }

    /*
     * Read the cpa.loc file by line and return the header lines as a string
     * 
     * @param header A string to indicate the the header.
     * 
     * @return String header of file.
     */
    private String getCpaFileHeader(String header) {

        List<String> fileContent = readFileAsList(ATCF_CONFIG_ROOT,
                CPA_LOCATIONS_FILE, false);

        StringBuilder sb = new StringBuilder();
        for (String str : fileContent) {
            if (str.startsWith(header)) {
                sb.append(str);
                sb.append("\n");
            } else {
                break;
            }
        }

        return sb.toString();

    }

    /*
     * Read the techlist.dat file by line and return the header lines as a
     * string. Data lines starts with a digit.
     *
     * @return String header of file.
     */
    private String getTechFileHeader() {

        List<String> fileContent = readFileAsList(ATCF_CONFIG_ROOT,
                TECH_LIST_FILE, false);

        StringBuilder sb = new StringBuilder();
        for (String str : fileContent) {
            String nstr = str.trim();
            if (nstr.length() == 0 || !Character.isDigit(nstr.charAt(0))) {
                sb.append(str);
                sb.append("\n");
            } else {
                break;
            }
        }

        return sb.toString();

    }

    /*
     * Combined items in a string array into a single string, starting from a
     * given index.
     *
     * @param inStrings Input string array.
     * 
     * @param startIndex index of the first item to be combined.
     * 
     * @return String Combined string.
     */
    private String combine(String[] inStrings, int startIndex) {

        StringBuilder sbd = new StringBuilder();

        int ii = 0;
        for (String str : inStrings) {
            if (ii >= startIndex) {
                sbd.append(str);
                sbd.append(" ");
            }

            ii++;
        }

        return sbd.toString().trim();
    }

    /**
     * Get the list of ATCF color selections for a given group.
     *
     * @param items
     *            Data items for all color selections
     * @param clrgrp
     *            The color group
     *
     * @return list of ATCF color selections.
     */
    public List<String[]> getDataByColorSelectionGroup(List<String[]> items,
            ColorSelectionGroup clrgrp) {

        List<String[]> data = new ArrayList<>();

        boolean grpStart = false;
        boolean grpEnd = false;
        for (String[] item : items) {
            if (item[0].trim().equals(clrgrp.getName())) {
                grpStart = true;
                continue;
            }

            if (grpStart) {
                for (ColorSelectionGroup grp : ColorSelectionGroup.values()) {
                    if (item[0].trim().equals(grp.getName())) {
                        grpEnd = true;
                        break;
                    }

                }

                if (!grpEnd) {
                    data.add(item);
                } else {
                    break;
                }
            }
        }

        return data;
    }

    /**
     * Saves ATCF sidebar menu selection to localization (SITE level).
     *
     * @param sidebarMenuSel
     *            SidebarMenuSelection
     * @return
     */
    public boolean saveSidebarMenuSelection(
            SidebarMenuSelection sidebarMenuSel) {

        ILocalizationFile locFile = getSiteLocalizationFile(
                SIDEBAR_SELECTION_FILE);

        return save(locFile, sidebarMenuSel);
    }

    /**
     * Get ATCF sidebar menu selection information.
     *
     * @return A SidebarMenuSelection to hold all sidebar menu selection info.
     */
    public SidebarMenuSelection getSidebarMenuSelection() {

        SidebarMenuSelection sel = new SidebarMenuSelection();

        /*
         * First try to load from sidebar_selections.xml file. If not find, use
         * the default set.
         *
         */
        Object selObj = getConfigXmlObject(SIDEBAR_SELECTION_FILE);
        if (selObj != null) {
            sel = (SidebarMenuSelection) selObj;
        } else {
            sel = sel.getDefaultSidebarMenuSelection();
        }

        return sel;
    }

    /**
     * Get a SITE level localization file at ATCF_CONFIG_ROOT (create one if not
     * exists).
     *
     * @param fileName
     *            name of the localization file.
     */
    private ILocalizationFile getSiteLocalizationFile(String fileName) {

        // Create a SITE level file if not found.
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        return pm.getLocalizationFile(lc, ATCF_CONFIG_ROOT + fileName);
    }

    /**
     * Get a SITE level localization file at TCF_OBJ_AIDS_PROFILE_ROOT (create
     * one if not exists).
     *
     * @param fileName
     *            name of the localization file.
     */
    private ILocalizationFile getSiteAidsProfileFile(String fileName) {

        // Create a SITE level file if not found.
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        return pm.getLocalizationFile(lc,
                ATCF_OBJ_AIDS_PROFILE_ROOT + fileName);
    }

    /**
     * Get a SITE level localization file at GEOGRAPHY_POINTS_PATH (create one
     * if not exists).
     *
     * @param fileName
     *            name of the localization file.
     */
    private ILocalizationFile getSiteGeographyPointsFile(String fileName) {

        // Create a SITE level file if not found.
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        return pm.getLocalizationFile(lc, GEOGRAPHY_POINTS_PATH + fileName);
    }

    /**
     * Saves ATCFconfiguration object to localization (SITE level).
     *
     * @param locFile
     *            localization file to be saved into.
     * @param obj
     *            ATCF configuration object to be saved.
     */
    private boolean save(ILocalizationFile locFile, Object obj) {

        // Save to XML file.
        try (SaveableOutputStream sos = locFile.openOutputStream()) {
            jaxb.marshalToStream(obj, sos);
            sos.save();
        } catch (Exception e) {
            logger.error(SAVE_ERROR_MSG, locFile.getPath(), e);
            return false;
        }
        return true;

    }

    /**
     * Saves fix sites information to localization in plain text format (SITE
     * level).
     *
     * @param fixSites
     *            - fix sites to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveFixSitesAsText(FixSites fixSites) {
        return saveAsText(getSiteLocalizationFile(FIX_SITES_FILE), fixSites,
                getFileHeader(ATCF_CONFIG_ROOT, FIX_SITES_FILE, END_OF_HEADER));
    }

    /**
     * Saves fix sites information to localization in XML format (SITE level).
     *
     * @param fixSites
     *            - fix sites to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveFixSites(FixSites fixSites) {
        return saveConfig(FIX_SITES_FILE, fixSites);
    }

    /**
     * Saves microwave satellites sites information to localization in plain
     * text format (SITE level).
     *
     * @param microSat
     *            - Microwave Satellites to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveMicroSatsAsText(FixMicrowaveSatelliteTypes microSat) {
        return saveAsText(getSiteLocalizationFile(MICRO_SAT_FILE), microSat,
                getFileHeader(ATCF_CONFIG_ROOT, MICRO_SAT_FILE, END_OF_HEADER));
    }

    /**
     * Saves microwave satellites information to localization in plain text
     * format (SITE level).
     *
     * @param microSat
     *            - Microwave Satellites to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveMicroSats(FixMicrowaveSatelliteTypes microSat) {
        return saveConfig(MICRO_SAT_FILE, microSat);
    }

    /**
     * Saves scatterometer satellites information to localization in plain text
     * format (SITE level).
     *
     * @param scatSat
     *            - Scatterometer Satellites to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */

    public boolean saveScatSatsAsText(FixScatSatTypes scatSat) {
        return saveAsText(getSiteLocalizationFile(SCAT_SAT_FILE), scatSat,
                getFileHeader(ATCF_CONFIG_ROOT, SCAT_SAT_FILE, END_OF_HEADER));
    }

    /**
     * Saves scatterometer satellites information to localization in plain text
     * format (SITE level).
     *
     * @param scatSat
     *            - Scatterometer Satellites to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveScatSats(FixScatSatTypes scatSat) {
        return saveConfig(SCAT_SAT_FILE, scatSat);
    }

    /**
     * Saves fix types information to localization in plain text format (SITE
     * level).
     *
     * @param fixTypes
     *            - fix types to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveFixTypesAsText(FixTypes fixTypes) {
        return saveAsText(getSiteLocalizationFile(FIX_TYPES_FILE), fixTypes,
                getFileHeader(ATCF_CONFIG_ROOT, FIX_TYPES_FILE, END_OF_HEADER));
    }

    /**
     * Saves fix types information to localization in XML format (SITE level).
     *
     * @param fixTypes
     *            - fix types to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveFixTypes(FixTypes fixTypes) {
        return saveConfig(FIX_TYPES_FILE, fixTypes);
    }

    /**
     * Saves ATCF Custom Color information to localization in plain text format
     * (SITE level).
     *
     * @param atcfCustomColors
     *            - Colors to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveCustomColorsAsText(AtcfCustomColors atcfCustomColors) {
        return saveAsText(getSiteLocalizationFile(COLOR_TABLE_FILE),
                atcfCustomColors, getFileHeader(ATCF_CONFIG_ROOT,
                        COLOR_TABLE_FILE, END_OF_HEADER));
    }

    /**
     * Saves ATCF Custom Color information to localization in XML format (SITE
     * level).
     *
     * @param atcfCustomColors
     *            - Colors to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveCustomColors(AtcfCustomColors atcfCustomColors) {
        return saveConfig(COLOR_TABLE_FILE, atcfCustomColors);
    }

    /**
     * Saves CPA Location information to localization in plain text format (SITE
     * level)
     * 
     * @param cpaLocations
     *            - Locations to save
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveCpaLocationsAsText(CpaLocations cpaLocations) {
        return saveAsText(getSiteLocalizationFile(CPA_LOCATIONS_FILE),
                cpaLocations, getCpaFileHeader(CPA_END_OF_HEADER));
    }

    /**
     * Saves CPA Location information to localization in XML format (SITE level)
     * 
     * @param cpaLocations
     *            - Locations to save
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveCpaLocations(CpaLocations cpaLocations) {
        return saveConfig(CPA_LOCATIONS_FILE, cpaLocations);
    }

    /**
     * Saves ATCF Color Selection information to localization in plain text
     * format (SITE level).
     *
     * @param atcfColorSelections
     *            - Colors Selections to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveColorSelectionsAsText(
            AtcfColorSelections atcfColorSelections) {
        return saveAsText(getSiteLocalizationFile(COLOR_SELECTION_FILE),
                atcfColorSelections, getFileHeader(ATCF_CONFIG_ROOT,
                        COLOR_SELECTION_FILE, END_OF_HEADER));
    }

    /**
     * Saves ATCF Color Selection information to localization in plain text
     * format (SITE level).
     *
     * @param atcfColorSelections
     *            - Colors Selections to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveColorSelections(
            AtcfColorSelections atcfColorSelections) {
        try {
            fireColorConfigurationChangeListeners();
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return saveConfig(COLOR_SELECTION_FILE, atcfColorSelections);
    }

    /**
     * Saves Objective Aid Techniques information to localization in plain text
     * format (SITE level).
     *
     * @param objectiveAidTechniques
     *            - Colors Selections to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveObjectiveAidTechniquesAsText(
            ObjectiveAidTechniques objectiveAidTechniques) {
        return saveAsText(getSiteLocalizationFile(TECH_LIST_FILE),
                objectiveAidTechniques,
                getFileHeader(ATCF_CONFIG_ROOT, TECH_LIST_FILE, END_OF_HEADER));
    }

    /**
     * Saves Objective Aid Techniques information to localization in XML format
     * (SITE level).
     *
     * @param objectiveAidTechniques
     *            - Colors Selections to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveObjectiveAidTechniques(
            ObjectiveAidTechniques objectiveAidTechniques) {
        return saveConfig(TECH_LIST_FILE, objectiveAidTechniques);
    }

    /**
     * Saves Configuration Preference menu options to localization in plain text
     * format (SITE level).
     * 
     * @param atcfSitePreferences
     *            - configuration preferences to save
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveAtcfSitePreferencesAsText(
            AtcfSitePreferences atcfSitePreferences) {
        return saveAsText(getSiteLocalizationFile(PREFERENCES_FILE),
                atcfSitePreferences, getFileHeader(ATCF_CONFIG_ROOT,
                        PREFERENCES_FILE, END_OF_HEADER));
    }

    /**
     * Saves Configuration Preference menu options to localization in XML format
     * (SITE level).
     * 
     * @param atcfSitePreferences
     *            - configuration preferences to save
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveAtcfSitePreferences(
            AtcfSitePreferences atcfSitePreferences) {
        return saveConfig(PREFERENCES_FILE, atcfSitePreferences);
    }

    /**
     * Saves FixError options to localization in plain text format (SITE level).
     * 
     * @param FixError
     *            - configuration preferences to save
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveFixErrorAsText(FixError fixError) {
        return saveAsText(getSiteLocalizationFile(FIX_ERROR_FILE), fixError,
                getFileHeader(ATCF_CONFIG_ROOT, FIX_ERROR_FILE, END_OF_HEADER));
    }

    /**
     * Saves FixError options to localization in XML format (SITE level).
     * 
     * @param FixError
     *            - configuration preferences to save
     * @return boolean - true, saved; false - fail to save.
     */
    public boolean saveFixError(FixError fixError) {
        return saveConfig(FIX_ERROR_FILE, fixError);
    }

    /*
     * Save information into a localization file (SITE level).
     *
     * @param lfile - Localization file to save into
     * 
     * @param obj - Information to save
     * 
     * @param header - Header information to save
     * 
     * @return boolean - true, saved; false - fail to save.
     */
    private boolean saveAsText(ILocalizationFile lfile, Object obj,
            String header) {

        boolean saved = false;

        // Save text file.
        if (lfile != null) {
            try (SaveableOutputStream sos = lfile.openOutputStream()) {
                sos.write((header + obj.toString()).getBytes());
                sos.save();
                saved = true;
            } catch (Exception e) {
                logger.error(SAVE_ERROR_MSG, lfile.getPath(), e);
            }
        }

        return saved;

    }

    /**
     * Get an array of SITE level profile file names in localization store.
     *
     * @return String[] array of obj aids profile names.
     */
    public String[] getObjAidsProfileNames() {

        // Add profiles in text format.
        ILocalizationFile[] lfiles = pm.listStaticFiles(
                LocalizationType.COMMON_STATIC, ATCF_OBJ_AIDS_PROFILE_ROOT,
                new String[] { OBJ_AIDS_PROFILE_EXT }, false, true);

        List<String> aids = new ArrayList<>();
        for (ILocalizationFile lf : lfiles) {
            String fileName = lf.getPath().replace(ATCF_OBJ_AIDS_PROFILE_ROOT,
                    "");
            aids.add(fileName.replace(OBJ_AIDS_PROFILE_EXT, ""));
        }

        // Add profiles in xml format.
        ILocalizationFile[] xmlFiles = pm.listStaticFiles(
                LocalizationType.COMMON_STATIC, ATCF_OBJ_AIDS_PROFILE_ROOT,
                new String[] { OBJ_AIDS_PROFILE_EXT + XML_EXTENSION }, false,
                true);
        if (xmlFiles.length > 0) {
            for (ILocalizationFile lf : xmlFiles) {
                String fileName = lf.getPath()
                        .replace(ATCF_OBJ_AIDS_PROFILE_ROOT, "");
                String fname = fileName
                        .replace(OBJ_AIDS_PROFILE_EXT + XML_EXTENSION, "");
                if (!aids.contains(fname)) {
                    aids.add(fname);
                }
            }
        }

        Collections.sort(aids);

        return aids.toArray(new String[lfiles.length]);

    }

    /**
     * Get an array of non-BASE level profile file names in localization store.
     *
     * @return String[] array of all non-BASE obj aids profile names.
     */
    public String[] getNonBaseObjAidsProfileNames() {

        ILocalizationFile[] lfiles = pm.listStaticFiles(
                LocalizationType.COMMON_STATIC, ATCF_OBJ_AIDS_PROFILE_ROOT,
                new String[] { OBJ_AIDS_PROFILE_EXT }, false, true);

        List<String> aids = new ArrayList<>();
        for (ILocalizationFile lf : lfiles) {
            LocalizationLevel lvl = lf.getContext().getLocalizationLevel();
            if (lvl != LocalizationLevel.BASE) {
                String fileName = lf.getPath()
                        .replace(ATCF_OBJ_AIDS_PROFILE_ROOT, "");
                aids.add(fileName.replace(OBJ_AIDS_PROFILE_EXT, ""));
            }
        }

        // Add profiles in xml format.
        ILocalizationFile[] xmlFiles = pm.listStaticFiles(
                LocalizationType.COMMON_STATIC, ATCF_OBJ_AIDS_PROFILE_ROOT,
                new String[] { OBJ_AIDS_PROFILE_EXT + XML_EXTENSION }, false,
                true);
        if (xmlFiles.length > 0) {
            for (ILocalizationFile lf : xmlFiles) {
                LocalizationLevel lvl = lf.getContext().getLocalizationLevel();
                if (lvl != LocalizationLevel.BASE) {
                    String fileName = lf.getPath()
                            .replace(ATCF_OBJ_AIDS_PROFILE_ROOT, "");
                    String fname = fileName
                            .replace(OBJ_AIDS_PROFILE_EXT + XML_EXTENSION, "");
                    if (!aids.contains(fname)) {
                        aids.add(fname);
                    }
                }
            }
        }

        Collections.sort(aids);

        return aids.toArray(new String[aids.size()]);

    }

    /**
     * Delete a SITE level profile file from localization store.
     *
     * @param profile
     *            name of the profile (without .aids extension)
     * @return boolean true - deleted; false - fail to delete
     */
    public boolean deleteObjAidsProfile(String profile) {

        boolean deleted = false;
        String file = ATCF_OBJ_AIDS_PROFILE_ROOT + profile
                + OBJ_AIDS_PROFILE_EXT + ".xml";

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);
        ILocalizationFile locFile = pm.getLocalizationFile(lc, file);
        try {
            locFile.delete();
            deleted = true;
        } catch (LocalizationException e) {
            logger.error("AtcfConfigurationManager fails to delete: ", file, e);
        }

        return deleted;

    }

    /**
     * Save a SITE level profile file into localization store.
     * 
     * Note: a legacy ATCF obj. aids profile has the SAME format as the
     * default_aids.dat.
     *
     * @param profile
     *            name of the profile (without .aids extension)
     * @param techs
     *            Obj Aids tech entries (same format as DefaultObjAidTechniques)
     * @return boolean true - saved; false - fail to save
     */
    public boolean saveObjAidsProfileAsText(String profile,
            DefaultObjAidTechniques techs) {

        // Create a SITE level file if not found.
        String file = ATCF_OBJ_AIDS_PROFILE_ROOT + profile
                + OBJ_AIDS_PROFILE_EXT;

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        ILocalizationFile locFile = pm.getLocalizationFile(lc, file);

        String header = getFileHeader(ATCF_OBJ_AIDS_PROFILE_ROOT,
                profile + OBJ_AIDS_PROFILE_EXT, END_OF_HEADER);

        // Try to get a header for a new profile from an existing one.
        if (header.isEmpty()) {
            String[] profiles = getObjAidsProfileNames();
            if (profiles.length > 0) {
                header = getFileHeader(ATCF_OBJ_AIDS_PROFILE_ROOT,
                        profiles[0] + OBJ_AIDS_PROFILE_EXT, END_OF_HEADER);
            }
        }

        return saveAsText(locFile, techs, header);

    }

    /**
     * Get the content of a SITE level obj aids profile file in localization
     * store.
     * 
     * @param profile
     *            name of the profile (without .aids extension)
     * @return DefaultObjAidTechniques obj aids tech entries in profile.
     */
    public DefaultObjAidTechniques getSiteObjAidsProfileFromText(
            String profile) {

        String file = profile + OBJ_AIDS_PROFILE_EXT;

        List<String[]> dataItems = getDataItems(ATCF_OBJ_AIDS_PROFILE_ROOT,
                file, START_OF_DATA, WHITE_SPACE_SEPARATOR, COMMENT_INDICATOR);

        DefaultObjAidTechniques aids = new DefaultObjAidTechniques();
        for (String[] item : dataItems) {
            if (item.length > 1) {
                aids.getDefaultObjAidTechniques().add(
                        new DefaultObjAidTechEntry(item[0], combine(item, 1)));
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid obj aid entry in profile: "
                                + item[0]);
            }
        }

        String header = getFileHeader(ATCF_OBJ_AIDS_PROFILE_ROOT,
                profile + OBJ_AIDS_PROFILE_EXT, END_OF_HEADER);
        aids.setHeader(header);

        return aids;
    }

    /**
     *
     * @return the template directory within localization.
     */
    public String getTemplateDirectory() {
        return ATCF_TEMPLATE_ROOT;
    }

    /**
     * Save a file to SITE level under JSON directory.
     *
     * @param fileName
     * 
     * @param schema
     *
     * @return LocalizationFile
     */
    public LocalizationFile saveAtcfJson(String fileName, String schema) {

        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, ATCF_JSON_ROOT + fileName);
        if (lf == null) {

            // Create a SITE level file if not found.
            LocalizationContext lc = pm.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);

            LocalizationFile locFile = pm.getLocalizationFile(lc,
                    ATCF_JSON_ROOT + fileName);

            // Save json file.
            if (locFile != null) {
                try (SaveableOutputStream sos = locFile.openOutputStream()) {
                    sos.write(schema.getBytes());
                    sos.save();
                } catch (Exception e) {
                    logger.error(SAVE_ERROR_MSG, locFile.getPath(), e);
                    return null;
                }
            }
            return locFile;
        } else {
            return (LocalizationFile) lf;
        }
    }

    /*
     * Gets a AML file name from a given file name.
     * 
     * For files ending with ".dat", ".dat" will be replaced with ".xml"; for
     * other file names, "xml" is added as the suffix.
     *
     * @param fileName - Original file name
     *
     * @return String - A new file name ending with ".xml".
     */
    private String getXmlFileName(String fileName) {

        String fname = fileName;

        // Strip of ending ".dat", if any.
        int index = fileName.lastIndexOf(DAT_EXTENSION);
        if (index >= 0) {
            fname = fileName.substring(0, index);
        }

        return fname + XML_EXTENSION;

    }

    /*
     * Saves configuration information to localization in XML format (SITE
     * level).
     *
     * @param file Configuration file to be saved into
     * 
     * @param config Configuration to be saved
     *
     * @return boolean - true, saved; false - fail to save.
     */
    private boolean saveConfig(String file, Object config) {

        String xmlFile = file;
        if (!file.endsWith(XML_EXTENSION)) {
            xmlFile = getXmlFileName(file);
        }

        ILocalizationFile locFile = getSiteLocalizationFile(xmlFile);
        return save(locFile, config);
    }

    /**
     * Get the list of objective aid techniques. First try to load from
     * techlist.xml. If not find, try to load from the legacy file techlist.dat
     * and save in XML format for later use.
     *
     * @return list of objective aid techniques.
     */
    public ObjectiveAidTechniques getObjectiveAidTechniques() {

        ObjectiveAidTechniques aids;

        Object aidsObj = getConfigXmlObject(TECH_LIST_FILE);
        if (aidsObj != null) {
            aids = (ObjectiveAidTechniques) aidsObj;
        } else {
            aids = getObjAidTechFromText();

            // Save in XML format.
            saveConfig(TECH_LIST_FILE, aids);
        }

        return aids;

    }

    /**
     * Get list of Preferences. First try to load from atcfsite.prefs.xml. If
     * not find, try to load from the legacy file atcfsite.prefs and save in XML
     * format for later use.
     *
     * @return list of preferences
     */
    public AtcfSitePreferences getPreferences() {

        AtcfSitePreferences preferences;

        Object prefObj = getConfigXmlObject(PREFERENCES_FILE);
        if (prefObj != null) {
            preferences = (AtcfSitePreferences) prefObj;
        } else {
            preferences = getPreferencesFromText();

            // Save in XML format.
            saveConfig(PREFERENCES_FILE, preferences);
        }

        return preferences;
    }

    /**
     * Get list of Preferences. First try to load from cpa.loc.xml. If not find,
     * try to load from the legacy file cpa.loc and save in XML format for later
     * use.
     *
     * @return list of CPA locations
     */
    public CpaLocations getCpaLocations() {

        CpaLocations cpas;

        Object cpaObj = getConfigXmlObject(CPA_LOCATIONS_FILE);
        if (cpaObj != null) {
            cpas = (CpaLocations) cpaObj;
        } else {
            cpas = getCpaLocationsFromText();

            // Save in XML format.
            saveConfig(CPA_LOCATIONS_FILE, cpas);
        }

        return cpas;
    }

    /**
     * Get list of fix errors. First try to load from fixerror.prefs.xml. If not
     * find, try to load from the legacy file fixerror.prefs and save in XML
     * format for later use.
     *
     * @return list of FixError
     */
    public FixError getFixError() {

        FixError ferr;

        Object ferrObj = getConfigXmlObject(FIX_ERROR_FILE);
        if (ferrObj != null) {
            ferr = (FixError) ferrObj;
        } else {
            ferr = getFixErrorFromText();

            // Save in XML format.
            saveConfig(FIX_ERROR_FILE, ferr);
        }

        return ferr;
    }

    /**
     * Get list of fix sites. First try to load from fixsites.xml. If not find,
     * try to load from the legacy file fixsites.dat and save in XML format for
     * later use.
     *
     * @return list of FixSite
     */
    public FixSites getFixSites() {

        FixSites fsites;

        Object fsiteObj = getConfigXmlObject(FIX_SITES_FILE);
        if (fsiteObj != null) {
            fsites = (FixSites) fsiteObj;
        } else {
            fsites = getFixSitesFromText();

            // Save in XML format.
            saveConfig(FIX_SITES_FILE, fsites);
        }

        return fsites;
    }

    /**
     * Get list of fix types. First try to load from fixtypes.xml. If not find,
     * try to load from the legacy file fixtypes.dat and save in XML format for
     * later use.
     *
     * @return list of FixType
     */
    public FixTypes getFixTypes() {

        FixTypes config;

        Object xmlObj = getConfigXmlObject(FIX_TYPES_FILE);
        if (xmlObj != null) {
            config = (FixTypes) xmlObj;
        } else {
            config = getFixTypesFromText();

            // Save in XML format.
            saveConfig(FIX_TYPES_FILE, config);
        }

        return config;
    }

    /**
     * Get list of microwave satellite types. First try to load from
     * microsattypes.xml. If not find, try to load from the legacy file
     * microsattypes.dat and save in XML format for later use.
     *
     * @return list of FixMicrowaveSatelliteTypes
     */
    public FixMicrowaveSatelliteTypes getMicroSat() {

        FixMicrowaveSatelliteTypes config;

        Object xmlObj = getConfigXmlObject(MICRO_SAT_FILE);
        if (xmlObj != null) {
            config = (FixMicrowaveSatelliteTypes) xmlObj;
        } else {
            config = getMicroSatFromText();

            // Save in XML format.
            saveConfig(MICRO_SAT_FILE, config);
        }

        return config;
    }

    /**
     * Get the list of scatterometer satellites. First try to load from
     * scatsattypes.xml. If not find, try to load from the legacy file
     * scatsattypes.dat and save in XML format for later use.
     *
     * @return list of FiScatSatType
     */
    public FixScatSatTypes getScatSat() {

        FixScatSatTypes config;

        Object xmlObj = getConfigXmlObject(SCAT_SAT_FILE);
        if (xmlObj != null) {
            config = (FixScatSatTypes) xmlObj;
        } else {
            config = getScatSatFromText();

            // Save in XML format.
            saveConfig(SCAT_SAT_FILE, config);
        }

        return config;
    }

    /**
     * Get the list of default objective aid techniques. First try to load from
     * default_aids.xml. If not find, try to load from the legacy file
     * default_aids.dat and save in XML format for later use.
     *
     * @return list of default objective aid techniques.
     */
    public DefaultObjAidTechniques getDefaultObjAidTechniques() {

        DefaultObjAidTechniques config;

        Object xmlObj = getConfigXmlObject(DEFAULT_AIDS_FILE);
        if (xmlObj != null) {
            config = (DefaultObjAidTechniques) xmlObj;
        } else {
            config = getDefaultObjAidTechniquesFromText();

            // Save in XML format.
            saveConfig(DEFAULT_AIDS_FILE, config);
        }

        return config;
    }

    /**
     * Get the list of ATCF custom colors. First try to load from
     * colortable.xml. If not find, try to load from the legacy file
     * colortable.dat and save in XML format for later use.
     *
     * @return list of ATCF custom color definition.
     */
    public synchronized AtcfCustomColors getAtcfCustomColors() {

        // Only load once.
        if (atcfCustomColors == null) {
            Object xmlObj = getConfigXmlObject(COLOR_TABLE_FILE);
            if (xmlObj != null) {
                atcfCustomColors = (AtcfCustomColors) xmlObj;
            } else {
                atcfCustomColors = getAtcfCustomColorsFromText();

                // Save in XML format.
                saveConfig(COLOR_TABLE_FILE, atcfCustomColors);
            }
        }

        return atcfCustomColors;

    }

    /**
     * Get the list of ATCF color selections. First try to load from
     * colortable.xml. If not find, try to load from the legacy file
     * colortable.dat and save in XML format for later use.
     * 
     * @return list of ATCF color selections.
     */
    public synchronized AtcfColorSelections getAtcfColorSelections() {

        if (atcfColorSelections == null) {

            Object xmlObj = getConfigXmlObject(COLOR_SELECTION_FILE);
            if (xmlObj != null) {
                atcfColorSelections = (AtcfColorSelections) xmlObj;
            } else {
                atcfColorSelections = getAtcfColorSelectionsFromText();

                // Save in XML format.
                saveConfig(COLOR_SELECTION_FILE, atcfColorSelections);
            }
        }

        return atcfColorSelections;

    }

    /**
     * Get the list of maximum wind/gust pairs. First try to load from gust.xml.
     * If not find, try to load from the legacy file gust.dat and save in XML
     * format for later use.
     *
     * @return list of maximum wind/gust pairs..
     */
    public MaxWindGustPairs getMaxWindGustPairs() {

        MaxWindGustPairs config;

        Object xmlObj = getConfigXmlObject(GUST_FILE);
        if (xmlObj != null) {
            config = (MaxWindGustPairs) xmlObj;
        } else {
            config = getMaxWindGustPairsFromText();

            // Save in XML format.
            saveConfig(GUST_FILE, config);
        }

        return config;
    }

    /**
     * Get the content of a SITE level obj aids profile file in localization
     * store.
     * 
     * @param profile
     *            name of the profile (without .aids extension)
     * @return DefaultObjAidTechniques obj aids tech entries in profile.
     */
    public DefaultObjAidTechniques getSiteObjAidsProfile(String profile) {

        String xmlFile = getXmlFileName(profile + OBJ_AIDS_PROFILE_EXT);

        DefaultObjAidTechniques config;

        Object xmlObj = getProfileXmlObject(xmlFile);
        if (xmlObj != null) {
            config = (DefaultObjAidTechniques) xmlObj;
        } else {
            config = getSiteObjAidsProfileFromText(profile);

            // Save in XML format.
            saveObjAidsProfile(profile, config);
        }

        return config;
    }

    /**
     * Save a SITE level profile file into localization store in XML foramt.
     * 
     * Note: a legacy ATCF obj. aids profile has the SAME format as the
     * default_aids.dat.
     *
     * @param profile
     *            name of the profile (without .aids extension)
     * @param techs
     *            Obj Aids tech entries (same format as DefaultObjAidTechniques)
     * @return boolean true - saved; false - fail to save
     */
    public boolean saveObjAidsProfile(String profile,
            DefaultObjAidTechniques techs) {

        // Create a SITE level file if not found.
        String xmlFile = getXmlFileName(profile + OBJ_AIDS_PROFILE_EXT);

        ILocalizationFile locFile = getSiteAidsProfileFile(xmlFile);
        return save(locFile, techs);

    }

    /**
     * Save a SITE level profile file into localization store in XML foramt.
     * 
     * Note: a legacy ATCF obj. aids profile has the SAME format as the
     * default_aids.dat.
     *
     * @param profile
     *            name of the profile (without .aids extension)
     * @param techs
     *            Obj Aids tech entries (same format as DefaultObjAidTechniques)
     * @return boolean true - saved; false - fail to save
     */
    public boolean saveGeographyPoints(String geoFile,
            GeographyPoints geoPoints) {

        // Create a SITE level file if not found.
        String xmlFile = getXmlFileName(geoFile);

        ILocalizationFile locFile = getSiteGeographyPointsFile(xmlFile);
        return save(locFile, geoPoints);

    }

    /**
     * get script for communicating with LDM
     * 
     * @return atcfScriptFile
     */
    public static File getPushScript() {

        IPathManager pm = PathManagerFactory.getPathManager();

        String atcfScriptPath = LocalizationUtil.join("atcf", "wcoss",
                "scripts", "pushToLdm.sh");

        File atcfScriptFile = null;

        LocalizationContext[] lc = getBaseSiteContexts();

        for (LocalizationContext ctx : lc) {
            atcfScriptFile = pm.getStaticFile(ctx.getLocalizationType(),
                    atcfScriptPath);
            if (atcfScriptFile != null
                    && ctx.getLocalizationLevel() == LocalizationLevel.SITE) {
                return atcfScriptFile;
            }
        }
        return atcfScriptFile;
    }

    /**
     * Get contents of atcf.properties
     *
     * @return the atcf properties
     */
    public static AtcfEnvironmentConfig getEnvConfig() {
        synchronized (AtcfConfigurationManager.class) {
            if (envConfig == null) {
                envConfig = loadEnvConfig();
            }
        }
        return envConfig;
    }

    private static AtcfEnvironmentConfig loadEnvConfig() {

        AtcfEnvironmentConfig result = new AtcfEnvironmentConfig();

        Properties atcfProperties = new Properties();

        IPathManager pm = PathManagerFactory.getPathManager();

        ILocalizationFile propsFile = null;

        LocalizationContext[] lc = getBaseSiteContexts();

        propsFile = pm.getStaticLocalizationFile(lc, ATCF_PROPS_PATH);

        try (InputStream input = propsFile.openInputStream()) {
            atcfProperties.load(input);

        } catch (IOException | LocalizationException e) {
            logger.warn("Failed to load " + propsFile.toString()
                    + "Using default properties. ", e);
        }
        /*
         * Fill in env config with retrieved atcf properties
         */
        String fieldName = "";
        Field[] fields = result.getClass().getDeclaredFields();
        String propName = null;

        for (Field field : fields) {
            fieldName = field.getName().toUpperCase();
            propName = atcfProperties.getProperty(fieldName);

            // If valid property value retrieved, override env field
            if (propName != null) {
                PropertyDescriptor pd = null;
                Method setter;
                try {
                    pd = new PropertyDescriptor(field.getName(),
                            result.getClass());
                } catch (IntrospectionException e) {
                    logger.warn(
                            "AtcfConfigurationManager: Error getting property descriptor for "
                                    + propName + e.getLocalizedMessage(),
                            e);
                }
                // Call the AtcfEnvironmentConfig setter for this field
                try {
                    setter = pd.getWriteMethod();
                    if (setter != null) {
                        setter.invoke(result, propName);
                    } else {
                        logger.warn(
                                "AtcfConfigurationManager: Invlid pd to invoke setter for "
                                        + propName);
                    }
                } catch (IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    logger.warn(
                            "AtcfConfigurationManager: Failed to invoke setter for "
                                    + propName,
                            e);
                }

            } else {
                usingDefalutCfgMsg(fieldName);
            }
        }

        return result;

    }

    /**
     * Get a list of defined geography points for a given basin. First try to
     * load from xml file. If not find, try to load from the legacy text file
     * and save in XML format for later use.
     *
     * @return list of GeographyPoints
     */
    public GeographyPoints getBasinGeoPoints(String basinId) {

        GeographyPoints config;

        String gFile = GEOGRAPHY_AL_FILE.replace("bb", basinId.toLowerCase());
        Object xmlObj = getGeographyPointsXmlObject(gFile);
        if (xmlObj != null) {
            config = (GeographyPoints) xmlObj;
        } else {
            config = getGeographyPointsFromText(gFile);

            // Save in XML format.
            saveGeographyPoints(gFile, config);
        }

        return config;
    }

    /**
     * Get list of forecaster initials. First try to load from initials.xml. If
     * not find, try to load from the legacy file initials.dat and save in XML
     * format for later use.
     *
     * @return list of ForecasterInitials
     */
    public ForecasterInitials getForecasterInitials() {

        ForecasterInitials config;

        Object xmlObj = getConfigXmlObject(FORECASTER_INITIALS_FILE);
        if (xmlObj != null) {
            config = (ForecasterInitials) xmlObj;
        } else {
            config = getForecasterInitialsFromText();

            // Save in XML format.
            saveConfig(FORECASTER_INITIALS_FILE, config);
        }

        return config;
    }

    /**
     * Get list of storm state in stormstates.xml
     * 
     * @return list of storm state
     */
    public StormStates getStormStates() {

        StormStates states = new StormStates();

        Object statesObj = getConfigXmlObject(STORM_STATES_FILE);
        if (statesObj != null) {
            states = (StormStates) statesObj;
        }

        return states;
    }

    /**
     * Get list of ATCF sites in atcfsites.xml
     * 
     * @return list of ATCF sites
     */
    public AtcfSites getAtcfSites() {

        AtcfSites sites = new AtcfSites();

        Object sitesObj = getConfigXmlObject(ATCF_SITES_FILE);
        if (sitesObj != null) {
            sites = (AtcfSites) sitesObj;
        }

        return sites;
    }

    /**
     * Get localization contexts
     * 
     * @return
     */
    private static LocalizationContext[] getBaseSiteContexts() {

        IPathManager pm = PathManagerFactory.getPathManager();

        return new LocalizationContext[] {
                pm.getContext(LocalizationType.COMMON_STATIC,
                        LocalizationLevel.SITE),
                pm.getContext(LocalizationType.COMMON_STATIC,
                        LocalizationLevel.BASE) };
    }

    /**
     * Warn user that property is missing from properties file so default will
     * be used
     * 
     * @param propName
     */
    private static void usingDefalutCfgMsg(String propName) {
        logger.warn("AtcfConfigurationManager: no valid values defined for "
                + propName + " in atcf.properties. Using default value.");
    }

    /**
     * Get a list of all ATCF storms from 1851 to present (storm.table) with
     * storm ID (i.e., AL112017) as the key.
     *
     * <pre>
     *  storm.table entry format example: 
     *
     *  KATIE, AL, L, , , , , 86, 2020, HU, O, 2020091400, 9999999999, , , , , 8, WARNING, 1, AL862020
     *
     *  See Storm.java for detailed description for each item in an entry.
     *
     * </pre>
     *
     * @return List<String>
     */
    public List<String> loadStormTable() {
        return readFileAsList(ATCF_CONFIG_ROOT, STORM_TABLE_FILE, true);
    }

    /**
     * Retrieve the initial & first name from full_initials.xml or
     * full_initials.dat.
     *
     * @return FullInitials
     */
    public FullInitials getFullInitials() {

        FullInitials config;

        Object xmlObj = getConfigXmlObject(FULL_INITIALS_FILE);
        if (xmlObj != null) {
            config = (FullInitials) xmlObj;
        } else {
            config = getFullInitialsFromText();

            // Save in XML format.
            saveConfig(FULL_INITIALS_FILE, config);
        }

        return config;
    }

    /**
     * Get the list of full initials from full_initials.dat.
     *
     * @return list of full initial.
     */
    public FullInitials getFullInitialsFromText() {
        List<String[]> dataItems = getDataItems(ATCF_CONFIG_ROOT,
                FULL_INITIALS_FILE, "", WHITE_SPACE_SEPARATOR,
                COMMENT_INDICATOR);

        FullInitials inis = new FullInitials();
        for (String[] item : dataItems) {
            if (item.length > 1) {
                inis.getFullInitials()
                        .add(new FullInitialEntry(item[0], item[1]));
            } else {
                logger.warn(
                        "AtcfConfigurationManager - invalid full initial entry: "
                                + item[0]);
            }
        }

        return inis;
    }

    public void addColorConfigurationChangeListener(
            IColorConfigurationChangedListener listener) {
        colorConfigurationChangeListeners.add(listener);
    }

    public void removeColorConfigurationChangeListener(
            IColorConfigurationChangedListener listener) {
        colorConfigurationChangeListeners.remove(listener);
    }

    protected void fireColorConfigurationChangeListeners() {
        for (IColorConfigurationChangedListener listener : colorConfigurationChangeListeners) {
            listener.colorsChanged();
        }
    }
}

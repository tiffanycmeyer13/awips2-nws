/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.localization.psh;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.monitor.events.MonitorConfigListener;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.serialization.jaxb.JaxbDummyObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * PshConfigurationManager enables user to set up PSH via Localization.
 *
 * Note: For each configuration file, first try to load from XML file, from the
 * lowest Localization level (USER first, if not found, then SITE level). If not
 * found, try to load from the legacy DAT or TXT file - this helps ease the
 * transition since the user could use the legacy file directly when they switch
 * to the new PSH application.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 JUN 2017  #35269     jwu         Initial creation
 * 26 JUN 2017  #35269     jwu         Add config_headers.xml
 * 29 JUN 2017  #35269     jwu         Add stations (metar/non-metar/marine)
 * 30 JUN 2017  #35269     jwu         Add cwas and cities
 * 06 JUL 2017  #35269     jwu         Add storm name list
 * 24 AUG 2017  #35269     jwu         Add station id/gauge to cities.
 * 03 NOV 2017  #40407     jwu         Load NOS tide gauge stations to city list.
 * 14 NOV 2017  #40296     astrakovsky Fixed error loading legacy stations and added
 *                                     boolean indicating last load source.
 * 11 DEC 2017  #41998     jwu         Use access control file in base/roles.
 * 11 JAN,2018  DCS19326   jwu         Baseline version.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class PshConfigurationManager {

    /**
     * Root for PSH configuration in Localization.
     */
    private static final String PSH_ROOT = "psh" + IPathManager.SEPARATOR;

    private static final String SETUP_ROOT = PSH_ROOT + "setup"
            + IPathManager.SEPARATOR;

    /**
     * MonitorConfigListener.
     */
    private Set<MonitorConfigListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(PshConfigurationManager.class);

    /**
     * JAXB manager for marshal/unmarshal.
     */
    private static final JAXBManager jaxb = buildJaxbManager();

    /**
     * Path manager.
     */
    private IPathManager pm = PathManagerFactory.getPathManager();

    /**
     * File names for PSH program configuration.
     */
    private static final String CONFIG_XML_FILE = "config_headers.xml";

    private static final String CONFIG_TXT_FILE = "config_headers.txt";

    /**
     * File names for forecasters.
     */
    private static final String FORECASTER_XML_FILE = "fcstr.xml";

    private static final String FORECASTER_DAT_FILE = "fcstr.dat";

    /**
     * File names for counties.
     */
    private static final String COUNTY_XML_FILE = "counties.xml";

    private static final String COUNTY_DAT_FILE = "county2.dat";

    /**
     * File names for PSH METAR stations.
     */
    private static final String METAR_XML_FILE = "metar_stationinfo.xml";

    private static final String METAR_TXT_FILE = "official_stationinfo.txt";

    /**
     * File names for PSH non-METAR stations.
     */
    private static final String NON_METAR_XML_FILE = "non_metar_stationinfo.xml";

    private static final String NON_METAR_TXT_FILE = "unofficial_stationinfo.txt";

    /**
     * File names for PSH marine stations.
     */
    private static final String MARINE_XML_FILE = "marine_stationinfo.xml";

    private static final String MARINE_TXT_FILE = "marine_stationinfo.txt";

    /**
     * File names for PSH CWA list.
     */
    private static final String CWA_XML_FILE = "cwas.xml";

    private static final String CWA_TXT_FILE = "shapefiles_cwas.txt";

    /**
     * File names for PSH cities.
     */
    private static final String CITY_XML_FILE = "cities.xml";

    private static final String CITY_TXT_FILE = "cities_pipe.txt";

    /**
     * File name for PSH water level stations - will be appended into PSH
     * cities.
     */
    private static final String NOS_TIDE_STATION_FILE = "Water_Level_Stations.txt";

    /**
     * Pattern in legacy PSH station files.
     */
    private static Pattern STATION_PATTERN = Pattern.compile("\\{([^\\}]*)\\}");

    /**
     * Singleton instance of this class
     */
    private static PshConfigurationManager instance;

    /**
     * Indicates if the settings were last loaded from XML version or a legacy
     * text file
     */
    private boolean loadedFromXml = false;

    /**
     * Private Constructor
     */
    private PshConfigurationManager() {
    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     */
    public synchronized static PshConfigurationManager getInstance() {
        if (instance == null) {
            instance = new PshConfigurationManager();
        }

        return instance;
    }

    /**
     * Build a JaxbManager to marshal/unmarshal of classes that hold PSH setup
     * information (forecasters, counties, etc.).
     *
     * @return a new JAXBManager for Psh configuration
     */
    private static JAXBManager buildJaxbManager() {

        /**
         * JAXB manager for marshal/unmarshal.
         */
        Class<?>[] clzz = { JaxbDummyObject.class, PshConfigHeader.class,
                PshForecasters.class, PshCounties.class, PshStations.class,
                PshCwas.class, PshCities.class, PshStormNames.class };

        JAXBManager jaxb = null;

        try {
            jaxb = new JAXBManager(clzz);
        } catch (JAXBException e) {
            logger.error(
                    "PshConfigurationManager: Error initializing JaxbManager due to ",
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
     * Saves PSH program configuration header to localization (SITE level).
     *
     * @param header
     *            PshConfigHeader.
     * @return
     */
    public boolean saveConfigHeader(PshConfigHeader header) {

        ILocalizationFile locFile = getSiteLocalizationFile(CONFIG_XML_FILE);

        return save(locFile, header);
    }

    /**
     * Get PSH program configuration header information.
     *
     * @return A PshConfigHeader to hold all config header info.
     */
    public PshConfigHeader getConfigHeader() {

        PshConfigHeader header = PshConfigHeader.getDefaultHeader();

        /*
         * First try to load from config_headers.xml file. If not find, try to
         * load from the legacy config_headers.dat.
         */
        Object headerObj = getXmlObject(CONFIG_XML_FILE);
        if (headerObj != null) {
            header = (PshConfigHeader) headerObj;
            loadedFromXml = true;
        } else {
            List<String> headerTxt = readFileAsList(CONFIG_TXT_FILE);
            header.fill(headerTxt);
            loadedFromXml = false;
        }

        return header;
    }

    /**
     * Saves PSH forecaster to localization (SITE level).
     *
     * @param fcstrs
     *            PshForecasters.
     * @return
     */
    public boolean saveForecasters(PshForecasters fcstrs) {

        ILocalizationFile locFile = getSiteLocalizationFile(
                FORECASTER_XML_FILE);

        return save(locFile, fcstrs);
    }

    /**
     * Get a list of PSH forecasters.
     *
     * @return PshForecasters A PshForecasters to hold all forecasters.
     */
    public PshForecasters getForecasters() {

        PshForecasters fcstrs = new PshForecasters();

        /*
         * First try to load from fcstr.xml.xml. If not find, try to load from
         * the legacy file fcstr.dat.
         */
        Object fcstObj = getXmlObject(FORECASTER_XML_FILE);
        if (fcstObj != null) {
            fcstrs = (PshForecasters) fcstObj;
            loadedFromXml = true;
        } else {
            List<String> forecasters = readFileAsList(FORECASTER_DAT_FILE);
            fcstrs.setForecasters(forecasters);
            loadedFromXml = false;
        }

        return fcstrs;
    }

    /**
     * Saves PSH counties to localization (SITE level).
     *
     * @param counties
     *            PshCounties.
     * @return
     */
    public boolean saveCounties(PshCounties counties) {

        ILocalizationFile locFile = getSiteLocalizationFile(COUNTY_XML_FILE);

        return save(locFile, counties);
    }

    /**
     * Get a list of PSH counties.
     *
     * @return PshCounties A PshCounties to hold all county names.
     */
    public PshCounties getCounties() {

        PshCounties counties = new PshCounties();

        /*
         * First try to load from county2.xml. If not find, try to load from the
         * legacy file county2.dat.
         */
        Object countyObj = getXmlObject(COUNTY_XML_FILE);
        if (countyObj != null) {
            counties = (PshCounties) countyObj;
            loadedFromXml = true;
        } else {
            List<String> countyList = readFileAsList(COUNTY_DAT_FILE);
            counties.setCounties(countyList);
            loadedFromXml = false;
        }

        return counties;
    }

    /**
     * Saves PSH METAR stations to localization (SITE level).
     *
     * @param stations
     *            PshStations.
     * @return
     */
    public boolean saveMetarStations(PshStations stations) {

        ILocalizationFile locFile = getSiteLocalizationFile(METAR_XML_FILE);

        return save(locFile, stations);
    }

    /**
     * Saves PSH non-METAR stations to localization (SITE level).
     *
     * @param stations
     *            PshStations.
     * @return
     */
    public boolean saveNonMetarStations(PshStations stations) {

        ILocalizationFile locFile = getSiteLocalizationFile(NON_METAR_XML_FILE);

        return save(locFile, stations);
    }

    /**
     * Saves PSH marine stations to localization (SITE level).
     *
     * @param stations
     *            PshStations.
     * @return
     */
    public boolean saveMarineStations(PshStations stations) {

        ILocalizationFile locFile = getSiteLocalizationFile(MARINE_XML_FILE);

        return save(locFile, stations);
    }

    /**
     * Get a list of PSH METAR stations.
     *
     * @return PshStations All PSH MTAR stations.
     */
    public PshStations getMetarStations() {

        PshStations metars = new PshStations();

        /*
         * First try XMl file. If not found, try the legacy file.
         */
        Object metarObj = getXmlObject(METAR_XML_FILE);
        if (metarObj != null) {
            metars = (PshStations) metarObj;
            loadedFromXml = true;
        } else {
            List<String> metarList = readFileAsList(METAR_TXT_FILE);

            for (String str : metarList) {
                PshStation metarStn = getMetarStation(str);
                if (metarStn != null) {
                    metars.getStations().add(metarStn);
                }
            }
            loadedFromXml = false;
        }

        return metars;
    }

    /**
     * Get a list of PSH non-METAR stations.
     *
     * @return PshStations All PSH non-METAR stations.
     */
    public PshStations getNonMetarStations() {

        PshStations nonMetars = new PshStations();

        /*
         * First try XMl file. If not found, try the legacy file.
         */
        Object nonmetarObj = getXmlObject(NON_METAR_XML_FILE);
        if (nonmetarObj != null) {
            nonMetars = (PshStations) nonmetarObj;
            loadedFromXml = true;
        } else {
            List<String> nonmetarList = readFileAsList(NON_METAR_TXT_FILE);

            for (String str : nonmetarList) {
                PshStation stn = getNonMetarStation(str);
                if (stn != null) {
                    nonMetars.getStations().add(stn);
                }
            }
            loadedFromXml = false;
        }

        return nonMetars;
    }

    /**
     * Get a list of PSH marine stations.
     *
     * @return PshStations All PSH marine stations.
     */
    public PshStations getMarineStations() {

        PshStations marines = new PshStations();

        /*
         * First try XMl file. If not found, try the legacy txt or dat file.
         */
        Object marineObj = getXmlObject(MARINE_XML_FILE);
        if (marineObj != null) {
            marines = (PshStations) marineObj;
            loadedFromXml = true;
        } else {
            List<String> marineList = readFileAsList(MARINE_TXT_FILE);

            for (String str : marineList) {
                PshStation marineStn = getMarineStation(str);
                if (marineStn != null) {
                    marines.getStations().add(marineStn);
                }
            }
            loadedFromXml = false;
        }

        return marines;
    }

    /**
     * Get an object instantiated from a given PSH setup XML file.
     *
     * @return Object An object instantiated from the given XML file.
     */
    private Object getXmlObject(String xmlFile) {

        Object obj = null;

        String fullName = SETUP_ROOT + xmlFile;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fullName);

        if (lf != null) {

            try (InputStream is = lf.openInputStream()) {
                obj = jaxb.unmarshalFromInputStream(is);
            } catch (Exception ee) {
                logger.error(
                        "PshConfigurationManager: Error unmarshalling XML file: "
                                + lf.getPath(),
                        ee);
            }
        }

        return obj;
    }

    /**
     * Read the contents of a file into a list of strings (one line per string).
     * This is intended to read in the legacy text setup files for parsing.
     *
     * Note: blank lines are not included.
     *
     * @param fileIn
     *            A file to be read.
     * @return List<String> Content of the file.
     */
    private List<String> readFileAsList(String fileIn) {

        List<String> list = new ArrayList<>();

        String fileName = SETUP_ROOT + fileIn;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fileName);

        if (lf != null) {

            try (InputStream is = lf.openInputStream()) {
                Stream<String> stream = new BufferedReader(
                        new InputStreamReader(is)).lines();
                list = stream.collect(Collectors.toList());
            } catch (Exception ee) {
                logger.error("PshConfigurationManager: Error reading file: "
                        + lf.getPath(), ee);
            }
        }

        // Remove potential empty strings.
        List<String> validList = new ArrayList<>();
        for (String str : list) {
            if (str.trim().length() > 0) {
                validList.add(str);
            }
        }

        return validList;
    }

    /**
     * Get a SITE level localization file (create one if not exists).
     *
     * @param fileName
     *            name of the localization file.
     */
    private ILocalizationFile getSiteLocalizationFile(String fileName) {

        // Create a SITE level file if not found.
        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.SITE);

        return pm.getLocalizationFile(lc, SETUP_ROOT + fileName);
    }

    /**
     * Saves PSH information object to localization (SITE level).
     *
     * @param locFile
     *            localization file to be saved into.
     * @param obj
     *            PSH object to be saved.
     */
    private boolean save(ILocalizationFile locFile, Object obj) {

        // Save to XML file.
        try (SaveableOutputStream sos = locFile.openOutputStream()) {
            jaxb.marshalToStream(obj, sos);
            sos.save();
        } catch (Exception e) {
            logger.error(
                    "PshConfigurationManager couldn't save configuration file: ",
                    locFile.getPath(), e);
            return false;
        }
        return true;

    }

    /**
     * Parse a legacy METAR station definition into a PshStation.
     *
     * <pre>
     *      Example:
     *         {MIA}  {KTPA-TAMPA INTERNATIONAL AIRPORT FL}  {27.97}  {-82.53}
     *         {MIA}  {KPIE-SAINT PETERSBURG FL}  {27.91}  {-82.69}
     * </pre>
     *
     * @param metarStn
     *            A legacy text definition of a METAR station.
     *
     * @return PshStation
     */
    private PshStation getMetarStation(String metarStn) {

        PshStation stn = null;

        List<String> stnInfo = parseStationInfo(metarStn);

        int len = stnInfo.size();
        if (len > 0) {
            stn = PshStation.getDefaultStation();
            stn.setNode(stnInfo.get(0));

            if (len > 1) {
                // A station's full name has format "[code]-[name] state"
                String[] tokens = stnInfo.get(1).split("-");
                if (tokens.length > 0) {
                    stn.setCode(tokens[0].trim());
                }

                // Parse/set station's code, name, and state
                if (tokens.length > 1) {
                    String[] tks = tokens[1].split("\\s+");
                    if (tks.length > 1) {
                        String st = tks[tks.length - 1].trim();
                        if (st.length() == 2) {
                            stn.setState(st);

                            String nm = tokens[1].trim();
                            stn.setName(
                                    nm.substring(0, nm.length() - 2).trim());
                        } else {
                            stn.setName(tokens[1].trim());
                        }
                    } else {
                        stn.setName(tokens[1].trim());
                    }
                }
            }

            stn.buildFullName();

            // Station's lat/lon have two decimals.
            if (len > 2) {
                double value = parseDouble(stnInfo.get(2));
                value = Math.ceil(value * 100) / 100.0;
                stn.setLat(value);
            }

            if (len > 3) {
                double value = parseDouble(stnInfo.get(3));
                value = Math.ceil(value * 100) / 100.0;
                stn.setLon(value);
            }
        }

        return stn;
    }

    /**
     * Parse a legacy station definition into a list of Strings.
     *
     * @param mstarStn
     *            A string like: "{MIA} {KTPA-TAMPA INTERNATIONAL AIRPORT FL}
     *            {27.97} {-82.53}"
     * @return List<String>
     */
    private List<String> parseStationInfo(String stnStr) {

        List<String> stn = new ArrayList<>();

        Matcher m = STATION_PATTERN.matcher(stnStr);
        while (m.find()) {
            stn.add(m.group(1));
        }

        return stn;
    }

    /**
     * Parse a string to a double number, default to 0.0.
     *
     * @param inStr
     *            Input string.
     * @return parseDouble() A double value.
     */
    private double parseDouble(String inStr) {

        double value = 0.0F;
        if (inStr.length() > 0) {
            try {
                value = Double.parseDouble(inStr);
            } catch (NumberFormatException ne) {
                // Use default value
                logger.warn(
                        "PshConfigurationManager: error converting to double -  "
                                + ne);
            }
        }

        return value;
    }

    /**
     * Parse a string to a float number, default to 0.0.
     *
     * @param inStr
     *            Input string.
     * @return parseDouble() A double value.
     */
    private float parseFloat(String inStr) {

        float value = 0.0f;
        if (inStr.length() > 0) {
            try {
                value = Float.parseFloat(inStr);
            } catch (NumberFormatException ne) {
                // Use default value
                logger.warn(
                        "PshConfigurationManager: error converting to float - "
                                + ne);
            }
        }

        return value;
    }

    /**
     * Parse a legacy non-METAR station definition into a PshStation.
     *
     * <pre>
     *      Example:
     *           {KVVG-THE VILLAGES FL}  {28.90}  {-82.00}
     *           {SEBRING FAWN}  {27.42}  {-81.40}
     * </pre>
     *
     * @param nonMetarStn
     *            A legacy text definition of a non-metar station.
     * @return PshStation
     */
    private PshStation getNonMetarStation(String nonMetarStn) {

        PshStation stn = null;

        List<String> stnInfo = parseStationInfo(nonMetarStn);

        int len = stnInfo.size();
        if (len > 0) {
            stn = PshStation.getDefaultStation();

            /*
             * A non-metar station's full name has format "[node]-[name] state"
             * where node and state could be missing.
             */
            String[] tokens = stnInfo.get(0).split("-");
            if (tokens.length < 2) {
                stn.setName(tokens[0].trim());
            } else {

                // Parse/set station's code, name, and state
                stn.setCode(tokens[0].trim());

                if (tokens.length > 1) {
                    String[] tks = tokens[1].split("\\s+");
                    if (tks.length > 1) {
                        String st = tks[tks.length - 1].trim();
                        if (st.length() == 2) {
                            stn.setState(st);

                            String nm = tokens[1].trim();
                            stn.setName(
                                    nm.substring(0, nm.length() - 2).trim());
                        } else {
                            stn.setName(tokens[1].trim());
                        }
                    } else {
                        stn.setName(tokens[1].trim());
                    }
                }
            }

            stn.buildFullName();

            // Station's lat/lon have two decimals.
            if (len > 1) {
                double value = parseDouble(stnInfo.get(1));
                value = Math.ceil(value * 100) / 100.0;
                stn.setLat(value);
            }

            if (len > 2) {
                double value = parseDouble(stnInfo.get(2));
                value = Math.ceil(value * 100) / 100.0;
                stn.setLon(value);
            }
        }

        return stn;
    }

    /**
     * Parse a legacy marine station definition into a PshStation.
     *
     * <pre>
     *      Example:
     *           {42036-WEST OF TAMPA}  {28.50}  {-84.52}
     *           {PAG-PASS-A-GRILLE-COMPS}  {27.68}  {-82.77}
     *           {BIG CARLOS PASS-COMPS}  {26.40}  {-81.88}
     *           {C-CUT-PORTS}  {27.65}  {-82.62}
     * </pre>
     *
     * @param marineStn
     *            A legacy text definition of a marine station.
     *
     * @return PshStation
     */
    private PshStation getMarineStation(String marineStn) {

        PshStation stn = null;

        List<String> stnInfo = parseStationInfo(marineStn);

        int len = stnInfo.size();

        /*
         * Looks like marine stations in the legacy files do not have fixed
         * format, we only set it into name and do not try to parse
         * code/name/state.
         */
        if (len > 0) {
            stn = PshStation.getDefaultStation();
            stn.setName(stnInfo.get(0));

            // Station's lat/lon have two decimals.
            if (len > 1) {
                double value = parseDouble(stnInfo.get(1));
                value = Math.ceil(value * 100) / 100.0;
                stn.setLat(value);
            }

            if (len > 2) {
                double value = parseDouble(stnInfo.get(2));
                value = Math.ceil(value * 100) / 100.0;
                stn.setLon(value);
            }

            stn.buildFullName();
        }

        return stn;
    }

    /**
     * Saves PSH CWA list to localization (SITE level).
     *
     * @param cwas
     *            PshCwas.
     * @return
     */
    public boolean saveCwas(PshCwas cwas) {

        ILocalizationFile locFile = getSiteLocalizationFile(CWA_XML_FILE);

        return save(locFile, cwas);
    }

    /**
     * Get the list of PSH CWAs.
     *
     * @return PshCwas A PshCwas to hold all CWAs currently used in PSH.
     */
    public PshCwas getCwas() {

        PshCwas cwas = new PshCwas(new ArrayList<>());

        /*
         * First try to load from cwas.xml. If not find, try to load from the
         * legacy file shapefiles_cwas.txt.
         */
        Object cwaObj = getXmlObject(CWA_XML_FILE);
        if (cwaObj != null) {
            cwas = (PshCwas) cwaObj;
            loadedFromXml = true;
        } else {
            List<String> cwaList = readFileAsList(CWA_TXT_FILE);

            for (String str : cwaList) {
                cwas.getCwas().add(str);
            }
            loadedFromXml = false;
        }

        return cwas;
    }

    /**
     * Saves PSH city list to localization (SITE level).
     *
     * @param cities
     *            PshCities.
     */
    public boolean saveCities(PshCities cities) {

        ILocalizationFile locFile = getSiteLocalizationFile(CITY_XML_FILE);

        return save(locFile, cities);
    }

    /**
     * Get the list of PSH cities and NOS tide gauge stations.
     *
     * Note: NOS tide gauge stations are newly added for water level section.
     *
     * @return PshCities A PshCities to hold all cities currently used in PSH.
     */
    public PshCities getCities() {

        PshCities cities = new PshCities();

        /*
         * First try to load from cities.xml. If not find, try to load from the
         * legacy file cities_pipe.txt.
         */
        Object cityObj = getXmlObject(CITY_XML_FILE);
        if (cityObj != null) {
            cities = (PshCities) cityObj;
            loadedFromXml = true;
        } else {
            // Read LSR Cities.
            List<String> cityList = readFileAsList(CITY_TXT_FILE);

            for (String str : cityList) {
                PshCity city = getCity(str);
                if (city != null) {
                    cities.getCities().add(city);
                }
            }
            loadedFromXml = false;

            // Read NOS tide stations.
            List<String> tideStationList = readFileAsList(
                    NOS_TIDE_STATION_FILE);

            for (String str : tideStationList) {
                PshCity city = getCity(str);
                if (city != null) {
                    cities.getCities().add(city);
                }
            }
        }

        return cities;
    }

    /**
     * Parse a legacy city definition into a PshCity.
     *
     * <pre>
     *        CITY NAME|COUNTY|STATE|LAT|LON|STATION ID|GAUGE
     *
     *        ALVA|LEE|FL|26.7151|-81.6111
     *        ANNA MARIA ISLAND|MANATEE|FL|27.5289|-82.733|KTPA|G
     * 
     * </pre>
     *
     * @param cityStr
     *            A legacy text definition of a city.
     * @return PshCity
     */
    private PshCity getCity(String cityStr) {

        PshCity city = null;

        /*
         * A city should have format "CITY NAME|COUNTY|STATE|LAT|LON|ID|G""
         */
        String[] tokens = cityStr.split("\\|");

        int len = tokens.length;

        if (len > 0) {
            city = PshCity.getDefaultCity();
            city.setName(tokens[0]);

            if (len > 1) {
                city.setCounty((tokens[1]));
            }

            if (len > 2) {
                city.setState((tokens[2]));
            }

            // City's lat/lon have 5 decimals.
            if (len > 3) {
                float value = parseFloat(tokens[3]);
                city.setLat(value);
            }

            if (len > 4) {
                float value = parseFloat(tokens[4]);
                city.setLon(value);
            }

            if (len > 5) {
                String value = tokens[5];
                city.setStationID("");
                if (value.length() >= 4 && !value.startsWith("-")) {
                    city.setStationID(value.trim());
                }
            }

            if (len > 6) {
                city.setGauge("");
                if (tokens[6].equalsIgnoreCase("G")) {
                    city.setGauge("G");
                }
            }
        }

        return city;
    }

    /**
     * Get a list of storm names for a given basin/year.
     *
     * @param basin
     *            PSH basin.
     * @param year
     *            a four digit year.
     *
     * @return PshStormNames.
     */
    public PshStormNames getStormNames(PshBasin basin, String year) {

        PshStormNames storms = PshStormNames.getDefault();
        storms.setBasin(basin);
        storms.setYear(year);

        /*
         * First try to load from XML. If not find, try to load from the legacy
         * text file stored as like "atlantic/2012/storm12.txt".
         */
        String stormFile = getStormFileName(basin, year);

        Object stormNameObj = getXmlObject(stormFile + ".xml");
        if (stormNameObj != null) {
            storms = (PshStormNames) stormNameObj;
            loadedFromXml = true;
        } else {
            if (year.length() > 3) {
                String stormNameFile = stormFile + ".txt";

                List<String> names = readFileAsList(stormNameFile);

                // Add names to the list - the first one is year, skip it.
                int len = names.size();
                if (len > 1) {
                    List<String> sname = names.subList(1, len);
                    for (String str : sname) {
                        storms.getStorms().add(str);
                    }
                }

                loadedFromXml = false;
            }
        }

        return storms;
    }

    /**
     * Saves PSH storm names to localization (SITE level).
     *
     * @param stormName
     *            PshStormNames
     * @return
     */
    public boolean saveStormNames(PshStormNames stormName) {

        String stormFile = getStormFileName(stormName.getBasin(),
                stormName.getYear()) + ".xml";

        ILocalizationFile locFile = getSiteLocalizationFile(stormFile);

        return save(locFile, stormName);
    }

    /**
     * Gets storm file name for a basin and year, no file name extension.
     *
     * @return String
     *
     */
    private static String getStormFileName(PshBasin basin, String year) {
        return basin.getDirName() + IPathManager.SEPARATOR + "storm"
                + year.substring(2, 4);
    }

    /**
     * @return the loadedFromXml
     */
    public boolean isLoadedFromXml() {
        return loadedFromXml;
    }

}
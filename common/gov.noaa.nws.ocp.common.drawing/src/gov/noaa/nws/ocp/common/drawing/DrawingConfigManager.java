/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.JAXBManager;
import com.raytheon.uf.common.serialization.jaxb.JaxbDummyObject;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.drawing.breakpoint.CoastBreakpointList;
import gov.noaa.nws.ocp.common.drawing.breakpoint.IslandBreakpointList;
import gov.noaa.nws.ocp.common.drawing.breakpoint.WaterBreakpointList;
import gov.noaa.nws.ocp.common.drawing.linepattern.LinePatternList;
import gov.noaa.nws.ocp.common.drawing.symbolpattern.SymbolPatternList;

/**
 * Class to manage configuration files for drawing library.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         created.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class DrawingConfigManager {

    /**
     * Root for drawing configuration in Localization.
     */
    private static final String DRAWING_ROOT = "drawing"
            + IPathManager.SEPARATOR;

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DrawingConfigManager.class);

    /**
     * JAXB manager for marshal/unmarshal.
     */
    private static final JAXBManager jaxb = buildJaxbManager();

    /**
     * Path manager.
     */
    private IPathManager pm = PathManagerFactory.getPathManager();

    /**
     * File names for OCP Drawing library configuration.
     */
    private static final String LINE_PATTERN_FILE = "LinePatterns.xml";

    private static final String SYMBOL_PATTERN_FILE = "SymbolPatterns.xml";

    private static final String ISLAND_PRKPTS_FILE = "IslandBreakpoints.xml";

    private static final String WATER_BRKPTS_FILE = "WaterBreakpoints.xml";

    private static final String COAST_BRKPTS_FILE = "CoastBreakpoints.xml";

    /**
     * Singleton instance of this class
     */
    private static DrawingConfigManager instance;

    /**
     * Private Constructor
     */
    private DrawingConfigManager() {
    }

    /**
     * Get an instance of this singleton.
     *
     * @return Instance of this class
     */
    public static synchronized DrawingConfigManager getInstance() {
        if (instance == null) {
            instance = new DrawingConfigManager();
        }

        return instance;
    }

    /**
     * Build a JaxbManager to marshal/unmarshal of classes that hold drawing
     * configuration (line patterns, symbol patterns, break points, etc).
     *
     * @return a new JAXBManager for drawing configuration
     */
    private static JAXBManager buildJaxbManager() {

        /**
         * JAXB manager for marshal/unmarshal.
         */
        Class<?>[] clzz = { JaxbDummyObject.class, CoastBreakpointList.class,
                IslandBreakpointList.class, WaterBreakpointList.class,
                LinePatternList.class, SymbolPatternList.class };

        JAXBManager jaxb = null;

        try {
            jaxb = new JAXBManager(clzz);
        } catch (JAXBException e) {
            logger.error(
                    "DrawingConfigManager: Error initializing JaxbManager due to ",
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
     * Get the list of coast break points.
     *
     * @return list of coast break points.
     */
    public CoastBreakpointList getCoastBrkpts() {
        Object coastObj = getXmlObject(COAST_BRKPTS_FILE);

        CoastBreakpointList coasts = null;
        if (coastObj != null) {
            coasts = (CoastBreakpointList) coastObj;
        } else {
            coasts = new CoastBreakpointList();
            coasts.setCoasts(new ArrayList<>());
        }

        return coasts;

    }

    /**
     * Get the list of island break points.
     *
     * @return list of island break points.
     */
    public IslandBreakpointList getIslandBrkpts() {
        Object islandObj = getXmlObject(ISLAND_PRKPTS_FILE);

        IslandBreakpointList islands = null;
        if (islandObj != null) {
            islands = (IslandBreakpointList) islandObj;
        } else {
            islands = new IslandBreakpointList();
            islands.setIslands(new ArrayList<>());
        }

        return islands;
    }

    /**
     * Get the list of water break points.
     *
     * @return list of water break points.
     */
    public WaterBreakpointList getWaterBrkpts() {

        Object waterwayObj = getXmlObject(WATER_BRKPTS_FILE);

        WaterBreakpointList waterways = null;
        if (waterwayObj != null) {
            waterways = (WaterBreakpointList) waterwayObj;
        } else {
            waterways = new WaterBreakpointList();
            waterways.setWaterways(new ArrayList<>());
        }

        return waterways;
    }

    /**
     * Get an object instantiated from a given XML file.
     *
     * @return Object An object instantiated from the given XML file.
     */
    private Object getXmlObject(String xmlFile) {

        Object obj = null;

        String fullName = DRAWING_ROOT + xmlFile;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fullName);

        if (lf != null) {

            try (InputStream is = lf.openInputStream()) {
                obj = jaxb.unmarshalFromInputStream(is);
            } catch (Exception ee) {
                logger.error(
                        "DrawingConfigManager: Error unmarshalling XML file: "
                                + lf.getPath(),
                        ee);
            }
        }

        return obj;
    }

    /**
     * Get the list of line patterns.
     *
     * @return list of line patterns.
     */
    public LinePatternList getLinePatterns() {

        Object linePatternObj = getXmlObject(LINE_PATTERN_FILE);

        LinePatternList linePatterns = null;
        if (linePatternObj != null) {
            linePatterns = (LinePatternList) linePatternObj;
        } else {
            linePatterns = new LinePatternList();
            linePatterns.setPatternList(new ArrayList<>());
        }

        return linePatterns;
    }

    /**
     * Get the list of symbol patterns.
     *
     * @return list of symbol patterns.
     */
    public SymbolPatternList getSymbolPatterns() {

        Object symbolPatternObj = getXmlObject(SYMBOL_PATTERN_FILE);

        SymbolPatternList symPatterns = null;
        if (symbolPatternObj != null) {
            symPatterns = (SymbolPatternList) symbolPatternObj;
        } else {
            symPatterns = new SymbolPatternList();
            symPatterns.setPatternList(new ArrayList<>());
        }

        return symPatterns;
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

        String fileName = DRAWING_ROOT + fileIn;
        ILocalizationFile lf = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fileName);

        if (lf != null) {

            try (InputStream is = lf.openInputStream()) {
                Stream<String> stream = new BufferedReader(
                        new InputStreamReader(is)).lines();
                list = stream.collect(Collectors.toList());
            } catch (Exception ee) {
                logger.error("DrawingConfigManager: Error reading file: "
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

}
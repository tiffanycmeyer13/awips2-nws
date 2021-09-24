/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import com.raytheon.uf.common.json.BasicJsonService;
import com.raytheon.uf.common.json.JsonException;
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
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ncep.edex.common.stationTables.Station;
import gov.noaa.nws.ncep.ui.pgen.file.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.file.Point;
import gov.noaa.nws.ncep.ui.pgen.file.Products;
import gov.noaa.nws.ncep.viz.common.SnapUtil;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWAConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.AbstractCWANewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.CWAProductNewConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.DrawingType;
import gov.noaa.nws.ocp.viz.cwagenerator.config.PointLatLon;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.config.ProductsConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.AbstractDrawableComponent;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.LineDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PointDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.drawable.PolygonDrawable;
import gov.noaa.nws.ocp.viz.cwagenerator.ui.LatLonConfig;
import gov.noaa.nws.ocp.viz.cwagenerator.ui.StatePolygonConfig;

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
 * Sep 10, 2021 28802      wkwock      Added conversion method
 *
 * </pre>
 *
 * @author wkwock
 */
public class CWAGeneratorUtil {
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(CWAGeneratorUtil.class);

    public static final String CWA_DIR = "cwsu";

    public static final String CWA_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWAProductConfig.xml");

    public static final String CWA_PRACTICE_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWAPracticeProductConfig.xml");

    public static final String CWS_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWSProductConfig.xml");

    public static final String CWS_PRACTICE_PRODUCT_CONFIG_FILE = LocalizationUtil
            .join(CWA_DIR, "CWSPracticeProductConfig.xml");

    /** operational mode CWA products XML file */
    public static final String PRODUCTS_FILE = LocalizationUtil.join(CWA_DIR,
            "products.xml");

    /** practice mode CWA products XML file */
    public static final String PRACTICE_PRODUCTS_FILE = LocalizationUtil
            .join(CWA_DIR, "practiceProducts.xml");

    /** state polygons */
    private static final String POLYGON_FILE = LocalizationUtil.join("cwsu",
            "statePolygons.json");

    private static StatePolygonConfig[] statePolygons = null;

    private CWAGeneratorUtil() {
        // hide the constructor
    }

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
    public static void saveProductConfigurations(CWAProductNewConfig configs,
            String fileName) throws JAXBException, IOException,
            LocalizationException, SerializationException {
        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationContext lc = pm.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.USER);

        LocalizationFile configFile = pm.getLocalizationFile(lc, fileName);

        SingleTypeJAXBManager<CWAProductNewConfig> jaxb = new SingleTypeJAXBManager<>(
                CWAProductNewConfig.class);
        try (SaveableOutputStream sos = configFile.openOutputStream()) {
            jaxb.marshalToStream(configs, sos);
            sos.save();
        }
    }

    public static CWAProductNewConfig readProductConfigurations(String fileName)
            throws SerializationException, LocalizationException, IOException,
            JAXBException {
        IPathManager pm = PathManagerFactory.getPathManager();

        LocalizationFile configFile = pm.getStaticLocalizationFile(
                LocalizationType.COMMON_STATIC, fileName);
        if (configFile == null || !configFile.exists()) {
            return null;
        }

        CWAProductNewConfig productnewConfigs = null;
        try (InputStream is = configFile.openInputStream()) {
            SingleTypeJAXBManager<CWAProductNewConfig> jaxb = new SingleTypeJAXBManager<>(
                    CWAProductNewConfig.class);
            productnewConfigs = jaxb.unmarshalFromInputStream(is);
            return productnewConfigs;
        } catch (Exception e) {
            // It must be in the older format.
        }

        CWAProductConfig productConfigs = null;
        SingleTypeJAXBManager<CWAProductConfig> jaxb2 = new SingleTypeJAXBManager<>(
                CWAProductConfig.class);
        try (InputStream is = configFile.openInputStream()) {
            productConfigs = jaxb2.unmarshalFromInputStream(is);
            return conversion(productConfigs);
        }
    }

    /**
     * convert the old CWA product configuration format to the new one
     * 
     * @param tmpProductconfig
     * @return new CWA product configuration
     */
    private static CWAProductNewConfig conversion(
            CWAProductConfig tmpProductconfig) {
        if (tmpProductconfig == null) {
            return null;
        }

        CWAProductNewConfig newProductConfig = new CWAProductNewConfig();
        for (AbstractCWAConfig config : tmpProductconfig.getCwaProducts()) {
            Products products = config.getVorProduct();
            AbstractCWANewConfig newConfig = AbstractCWANewConfig
                    .getNewConfig(config);
            newProductConfig.addConfig(newConfig);
            for (gov.noaa.nws.ncep.ui.pgen.file.Product product : products
                    .getProduct()) {
                for (gov.noaa.nws.ncep.ui.pgen.file.Layer layer : product
                        .getLayer()) {
                    DrawableElement drawable = layer.getDrawableElement();
                    for (gov.noaa.nws.ncep.ui.pgen.file.Sigmet sigmet : drawable
                            .getSigmet()) {
                        AbstractDrawableComponent tmpDrawable = null;
                        if (sigmet.getPoint() == null
                                || sigmet.getPoint().isEmpty()) {
                            continue;
                        }
                        List<PointLatLon> coors = new ArrayList<>();
                        for (Point p : sigmet.getPoint()) {
                            coors.add(new PointLatLon(p.getLon(), p.getLat()));
                        }
                        if (sigmet.getType().equalsIgnoreCase("Isolated")) {
                            tmpDrawable = new PointDrawable(coors.get(0));
                        } else if (sigmet.getType().startsWith("Line")) {
                            tmpDrawable = new LineDrawable(coors,
                                    sigmet.getWidth().floatValue());
                        } else if (sigmet.getType().equalsIgnoreCase("Area")) {
                            tmpDrawable = new PolygonDrawable(coors);
                        }

                        if (tmpDrawable != null) {
                            newConfig.setDrawable(tmpDrawable);
                        }
                    }
                }
            }

        }
        return newProductConfig;
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

    public static StatePolygonConfig[] getStatePolygons() {
        LocalizationLevel[] lls = new LocalizationLevel[] {
                LocalizationLevel.SITE, LocalizationLevel.BASE };
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile propertiesFile = null;
        for (LocalizationLevel ll : lls) {
            LocalizationContext lc = pm
                    .getContext(LocalizationType.COMMON_STATIC, ll);

            propertiesFile = pm.getLocalizationFile(lc, POLYGON_FILE);
            if (propertiesFile.exists()) {
                break;
            }
        }

        if (propertiesFile != null && propertiesFile.exists()) {
            BasicJsonService json = new BasicJsonService();

            try {
                return (StatePolygonConfig[]) json.deserialize(
                        propertiesFile.openInputStream(),
                        StatePolygonConfig[].class);
            } catch (JsonException | LocalizationException e) {
                logger.error(
                        "Failed to read state polygons file " + POLYGON_FILE,
                        e);
            }
        }

        return null;
    }

    public static boolean pointInPolygon(List<LatLonConfig> polygon, double lat,
            double lon) {
        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coords = new Coordinate[polygon.size() + 1];
        for (int i = 0; i < polygon.size(); i++) {
            coords[i] = new Coordinate(polygon.get(i).getLat(),
                    polygon.get(i).getLon(), 0);
        }
        // close the polygon
        coords[polygon.size()] = new Coordinate(polygon.get(0).getLat(),
                polygon.get(0).getLon(), 0);
        Polygon polygon1 = gf.createPolygon(coords);
        org.locationtech.jts.geom.Point point = gf
                .createPoint(new Coordinate(lat, lon, 0));
        return polygon1.contains(point);
    }

    public static String getStates(Coordinate[] coords,
            DrawingType drawingType) {
        if (statePolygons == null) {
            statePolygons = getStatePolygons();
        }
        if (statePolygons == null) {
            return "";
        }

        List<String> states = new ArrayList<>();

        // get states that drawable in states
        for (StatePolygonConfig statePolygon : statePolygons) {
            for (Coordinate coord : coords) {
                if (pointInPolygon(statePolygon.getPolygon(), coord.x, coord.y)
                        && !states.contains(statePolygon.getStateID())) {
                    states.add(statePolygon.getStateID());
                }
            }
        }

        if (DrawingType.AREA == drawingType && coords.length > 2) {
            // coonvert the drawable coordinates to ArrayList<LatLonConfig>
            List<LatLonConfig> drawableCoords = new ArrayList<>();
            for (Coordinate coord : coords) {
                LatLonConfig latLon = new LatLonConfig((float) coord.x,
                        (float) coord.y);
                drawableCoords.add(latLon);
            }

            // get states that state in drawable
            for (StatePolygonConfig statePolygon : statePolygons) {
                if (!states.contains(statePolygon.getStateID())) {
                    for (LatLonConfig latLon : statePolygon.getPolygon()) {
                        if (pointInPolygon(drawableCoords, latLon.getLat(),
                                latLon.getLon())
                                && !states
                                        .contains(statePolygon.getStateID())) {
                            states.add(statePolygon.getStateID());
                        }
                    }
                }
            }
        }
        // put all states in a string
        StringBuilder stateStr = new StringBuilder();
        for (String stateId : states) {
            if (stateStr.length() != 0) {
                stateStr.append(" ");
            }
            stateStr.append(stateId);
        }
        return stateStr.toString();
    }

    public static final AbstractEditor getActiveEditor() {
        if (EditorUtil.getActiveEditor() instanceof AbstractEditor) {
            return (AbstractEditor) EditorUtil.getActiveEditor();
        } else {
            return null;
        }
    }

    public static Point2D latlonAddDistance(Point2D point, double distance,
            double direction) {
        GeodeticCalculator gc = new GeodeticCalculator();
        gc.setStartingGeographicPoint(point);
        distance = distance * 1852;// nautical mile to meter
        if (direction > 180) {
            direction -= 360;
        }
        gc.setDirection(direction, distance);
        return gc.getDestinationGeographicPoint();
    }

    public static Point2D getVorCoor(double lat, double lon) {
        List<Station> list = SnapUtil.VOR_STATION_LIST;
        GeodeticCalculator gc = new GeodeticCalculator(DefaultEllipsoid.WGS84);

        Coordinate coor = new Coordinate(lat, lon);
        TreeMap<Double, Station> treeMap = new TreeMap<>();

        SnapUtil.populateStationsTreeMap(treeMap, list, gc, coor, true);

        double distance = treeMap.firstKey();
        Station vorStn = treeMap.get(distance);
        gc.setStartingGeographicPoint(vorStn.getLongitude(),
                vorStn.getLatitude());
        gc.setDestinationGeographicPoint(coor.x, coor.y);

        // Round distance to the nearest 10 nautical miles;
        double distance2 = SnapUtil.getSnapDistance(distance, 10) / 1852.0f;

        double azimuth = Math.round((gc.getAzimuth() % 360) / 22.5) * 22.5;

        Point2D latLonPoint = new Point2D.Double(vorStn.getLongitude(),
                vorStn.getLatitude());

        return latlonAddDistance(latLonPoint, distance2, azimuth);
    }
}

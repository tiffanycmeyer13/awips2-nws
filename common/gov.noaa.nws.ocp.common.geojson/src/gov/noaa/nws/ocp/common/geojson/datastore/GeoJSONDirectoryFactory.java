package gov.noaa.nws.ocp.common.geojson.datastore;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.geotools.data.DataUtilities;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * GeoJSONDirectoryFactory
 * 
 * Implemented a Directory DataStore factory
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date           Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2016   17912      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class GeoJSONDirectoryFactory extends GeoJSONDataStoreFactory {

    private IUFStatusHandler logger = UFStatus
            .getHandler(GeoJSONDirectoryFactory.class);

    /** The directory to be scanned for file data stores */
    public static final Param URLP = new Param("url", URL.class,
            "Directory containing GeoJSON files", true);

    /**
     * getDisplayName
     * 
     * @return
     */
    public String getDisplayName() {
        return "Directory of GIS (GeoJSON) files";
    }

    /**
     * getDescription
     * 
     * @return
     */
    public String getDescription() {
        return "Takes a directory of GeoJSON and exposes it as a data store";
    }

    /**
     * canProcess
     * 
     * @param Map
     */
    public boolean canProcess(Map params) {
        // we don't try to steal single shapefiles away from the main factory
        if (super.canProcess(params)) {
            try {
                URL url = (URL) URLP.lookUp(params);
                File f = DataUtilities.urlToFile(url);
                return f != null && f.exists() && f.isDirectory();
            } catch (Exception e) {
                logger.error("Failed to get URL via lookup params.", e);
                return false;
            }
        } else {
            logger.warn("Calling super.canProcess(params) retirn false.");
            return false;
        }
    }
}

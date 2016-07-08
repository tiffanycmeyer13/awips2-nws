package gov.noaa.nws.ocp.common.geojson.datastore;

import gov.noaa.nws.ocp.common.geojson.datastore.util.GeoJSONFile;

import java.awt.RenderingHints.Key;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.directory.DirectoryDataStore;
import org.geotools.data.directory.FileStoreFactory;
import org.geotools.util.KVP;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * GeoJSONDataStoreFactory
 * 
 * Concrete class implemented AbstractDataStoreFactory
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2016  17912     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GeoJSONDataStoreFactory extends AbstractDataStoreFactory
        implements FileDataStoreFactorySpi {

    private IUFStatusHandler logger = UFStatus
            .getHandler(GeoJSONDataStoreFactory.class);

    /**
     * url to the .geojson file.
     */
    public static final Param URLP = new Param("url", URL.class,
            "url to a .geojson file", true, null,
            new KVP(Param.EXT, "geojson"));

    /**
     * Optional - uri of the FeatureType's namespace
     */
    public static final Param NAMESPACEP = new Param("namespace", URI.class,
            "uri to a the namespace", false, null, // not required
            new KVP(Param.LEVEL, "advanced"));

    /**
     * Optional - enable/disable the use of memory-mapped io
     */
    public static final Param MEMORY_MAPPED = new Param("memory mapped buffer",
            Boolean.class, "enable/disable the use of memory-mapped io", false,
            false, new KVP(Param.LEVEL, "advanced"));

    /**
     * Optional - enable/disable the use of memory-mapped io
     */
    public static final Param CACHE_MEMORY_MAPS = new Param(
            "cache and reuse memory maps", Boolean.class,
            "only memory map a file one, then cache and reuse the map", false,
            true, new KVP(Param.LEVEL, "advanced"));

    /**
     * Optional - Enable/disable the automatic creation of spatial index
     */
    public static final Param CREATE_SPATIAL_INDEX = new Param(
            "create spatial index", Boolean.class,
            "enable/disable the automatic creation of spatial index", false,
            true, new KVP(Param.LEVEL, "advanced"));

    /**
     * Optional parameter used to indicate 'gjson-ng' (as a marker to select the
     * implementation of DataStore to use).
     */
    public static final Param FSTYPE = new Param("fstype", String.class,
            "Enable using a setting of 'gjson'.", false, "gjson",
            new KVP(Param.LEVEL, "advanced", Param.OPTIONS, Arrays
                    .asList(new String[] { "geojson", "gjson", "index" })));

    /**
     * Optional - enable spatial index for local files
     */
    public static final Param ENABLE_SPATIAL_INDEX = new Param(
            "enable spatial index", Boolean.class,
            "enable/disable the use of spatial index for local geojson file",
            false, true, new KVP(Param.LEVEL, "advanced"));

    public String getDisplayName() {
        return "GeoJSON";
    }

    public String getDescription() {
        return "GeoJSON (*.geojson)";
    }

    /**
     * getParametersInfo
     * 
     * @return Parms[]
     */
    public Param[] getParametersInfo() {
        return new Param[] { URLP, NAMESPACEP, ENABLE_SPATIAL_INDEX,
                CREATE_SPATIAL_INDEX, MEMORY_MAPPED, CACHE_MEMORY_MAPS,
                FSTYPE };
    }

    public boolean isAvailable() {
        return true;
    }

    /**
     * getImplementationHints
     * 
     * @return Map
     */
    public Map<Key, ?> getImplementationHints() {
        return Collections.EMPTY_MAP;
    }

    /**
     * createDataStore
     * 
     * @param Map
     *            (connection parameters)
     * @return DataStore (GeoJSONDataStore)
     */
    public DataStore createDataStore(Map<String, Serializable> params)
            throws IOException {
        URL url = lookup(URLP, params, URL.class);
        Boolean isMemoryMapped = lookup(MEMORY_MAPPED, params, Boolean.class);
        URI namespace = lookup(NAMESPACEP, params, URI.class);

        // Check if creating a directory of GeoJSON store, or a single file
        File dir = DataUtilities.urlToFile(url);
        if (dir != null && dir.isDirectory()) {
            return new DirectoryDataStore(DataUtilities.urlToFile(url),
                    new GeoJSONFileStoreFactory(this, params));
        } else {
            GeoJSONFile gjsonFile = new GeoJSONFile(url);

            boolean isLocal = gjsonFile.isLocal();
            boolean useMemoryMappedBuffer = isLocal
                    && isMemoryMapped.booleanValue();

            // build the store
            GeoJSONDataStore store = new GeoJSONDataStore(url);
            if (namespace != null) {
                store.setNamespaceURI(namespace.toString());
            }
            store.setMemoryMapped(useMemoryMappedBuffer);
            return store;
        }
    }

    /**
     * createNewDataStore
     * 
     * @param Map
     *            (connection parameters)
     * @return DataStore (GeoJSONDataStore)
     */
    public DataStore createNewDataStore(Map<String, Serializable> params)
            throws IOException {
        return createDataStore(params);
    }

    /**
     * Looks up a parameter, if not found it returns the default value, assuming
     * there is one, or null otherwise
     * 
     * @param <T>
     * @param param
     * @param params
     * @param target
     * @return
     * @throws IOException
     */
    public <T> T lookup(Param param, Map<String, Serializable> params,
            Class<T> target) throws IOException {
        T result = (T) param.lookUp(params);
        if (result == null) {
            return (T) param.getDefaultValue();
        } else {
            return result;
        }

    }

    /**
     * canProcess
     * 
     * @param Map
     * @return
     */
    @Override
    public boolean canProcess(Map params) {
        if (!super.canProcess(params)) {
            return false; // fail basic param check
        }
        try {
            URL url = (URL) URLP.lookUp(params);
            if (canProcess(url)) {
                return true;
            } else {
                File dir = DataUtilities.urlToFile(url);
                // check for null fileType for backwards compatibility
                return dir.isDirectory();
            }
        } catch (IOException e) {
            logger.error("Failed to get URL or convert the URL to file.", e);
            return false;
        }
    }

    /**
     * canProcess
     * 
     * @param URL
     * @return
     */
    public boolean canProcess(URL f) {
        return f != null && f.getFile().toUpperCase().endsWith("GEOJSON");
    }

    /**
     * A delegates class that allow to build a directory of GeoJSON DataStore
     */
    public static class GeoJSONFileStoreFactory implements FileStoreFactory {

        GeoJSONDataStoreFactory gjsonFactory;

        Map originalParams;

        public GeoJSONFileStoreFactory(GeoJSONDataStoreFactory factory,
                Map originalParams) {
            this.gjsonFactory = factory;
            this.originalParams = originalParams;
        }

        public DataStore getDataStore(File file) throws IOException {
            final URL url = DataUtilities.fileToURL(file);
            if (gjsonFactory.canProcess(url)) {
                Map<String, Serializable> params = new HashMap<String, Serializable>(
                        originalParams);
                params.put(URLP.key, url);
                return gjsonFactory.createDataStore(params);
            } else {
                return null;
            }
        }

    }

    /**
     * getFileExtensions
     * 
     * @return
     */
    @Override
    public String[] getFileExtensions() {
        return new String[] { ".geojson" };
    }

    /**
     * createDataStore
     * 
     * @param URL
     * @return
     */
    @Override
    public FileDataStore createDataStore(URL url) throws IOException {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(URLP.key, url);

        boolean isLocal = url.getProtocol().equalsIgnoreCase("file");
        File file = DataUtilities.urlToFile(url);
        if (file != null && file.isDirectory()) {
            return null;
        } else {
            if (isLocal && !file.exists()) {
                return (FileDataStore) createNewDataStore(params);
            } else {
                return (FileDataStore) createDataStore(params);
            }
        }
    }

    /**
     * getTypeName
     * 
     * @param URL
     * @return
     */
    @Override
    public String getTypeName(URL url) throws IOException {
        DataStore ds = createDataStore(url);
        String[] names = ds.getTypeNames(); // should be exactly one
        ds.dispose();
        return ((names == null || names.length == 0) ? null : names[0]);
    }

}

package gov.noaa.nws.ocp.common.geojson.datastore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.FileDataStore;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.geojson.datastore.util.GeoJSONFeatureCollection;
import gov.noaa.nws.ocp.common.geojson.datastore.util.GeoJSONFile;

/**
 * Implementation a ContentStore for GeoJSON Files
 * 
 * GeoJSONDataStore is a core class for implementing the DataStore for GeoJSON
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 31, 2016  17912       pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class GeoJSONDataStore extends ContentDataStore
        implements FileDataStore {

    private IUFStatusHandler logger = UFStatus
            .getHandler(GeoJSONDataStore.class);

    private GeoJSONFile gjson;

    // Reserved for memory mapped
    private boolean memoryMapped = false;

    /**
     * GeoJSONDataStore
     * 
     * @param file
     */
    public GeoJSONDataStore(File file) {
        this.gjson = new GeoJSONFile(file);
    }

    /**
     * GeoJSONDataStore
     * 
     * @param url
     */
    public GeoJSONDataStore(URL url) {
        this.gjson = new GeoJSONFile(url);
    }

    /**
     * GeoJSONFeatureCollection
     * 
     * @return
     * @throws IOException
     */
    public GeoJSONFeatureCollection read() throws IOException {
        if (gjson == null) {
            return null;
        }
        return gjson.getFeatures();
    }

    /**
     * getOriginalFile
     * 
     * @return
     */
    public File getOriginalFile() {
        return gjson.getFile();
    }

    /**
     * createSchema
     * 
     * @param SimpleFeatureType
     * @throws IOException
     */
    @Override
    public void createSchema(SimpleFeatureType featureType) throws IOException {
        if (!gjson.isLocal()) {
            throw new IOException(
                    "Cannot create FeatureType on remote or in-classpath GeoJSON File");
        }

        /*
         * TODO: to support writing SimpleFeature to a GeoJSON file, implement
         * the schema.
         */
    }

    /**
     * createFeatureSource
     * 
     * @param ContentEntry
     * @return ContentFeatureSource
     * @throws IOException
     */
    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry)
            throws IOException {
        if (gjson.canWrite()) {
            return new GeoJSONFeatureStore(entry, Query.ALL);
        } else {
            return new GeoJSONFeatureSource(entry, Query.ALL);
        }
    }

    /**
     * GeoJSONFile
     * 
     * @return the gjson
     */
    public GeoJSONFile getGjson() {
        return gjson;
    }

    /**
     * setGjson
     * 
     * @param gjson
     *            the gjson to set
     */
    public void setGjson(GeoJSONFile gjson) {
        this.gjson = gjson;
    }

    /**
     * getSchema
     * 
     * @return SimpleFeatureType
     * @throws IOException
     */
    @Override
    public SimpleFeatureType getSchema() throws IOException {
        return gjson.getFeatures().getSchema();
    }

    /**
     * updateSchema
     * 
     * @param SimpleFeatureType
     * @throws IOException
     */
    @Override
    public void updateSchema(SimpleFeatureType featureType) throws IOException {
        /**
         * Implement this when support GeoJSON Writing
         */
    }

    /**
     * getTypeName
     * 
     * @return Name
     */
    public Name getTypeName() {
        return new NameImpl(namespaceURI, gjson.getTypeName());
    }

    /**
     * getFeatureSource
     * 
     * @return ContentFeatureSource
     * @throws IOException
     */
    @Override
    public ContentFeatureSource getFeatureSource() throws IOException {
        ContentEntry entry = ensureEntry(getTypeName());
        if (gjson.canWrite()) {
            return new GeoJSONFeatureStore(entry, Query.ALL);
        } else {
            return new GeoJSONFeatureSource(entry, Query.ALL);
        }
    }

    /**
     * getFeatureReader
     * 
     * @return FeatureReader
     * @throws IOException
     */
    @Override
    public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader()
            throws IOException {
        return super.getFeatureReader(new Query(getTypeName().getLocalPart()),
                Transaction.AUTO_COMMIT);
    }

    /**
     * getFeatureWriter
     * 
     * @param Filter
     * @param Transaction
     * @return FeatureWriter
     * @throws IOException
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(
            Filter filter, Transaction transaction) throws IOException {
        return getFeatureWriter(getTypeName().getLocalPart(), filter,
                transaction);
    }

    /**
     * getFeatureWriter
     * 
     * @param Transaction
     * @return FeatureWriter
     * @throws IOException
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(
            Transaction transaction) throws IOException {
        return getFeatureWriter(getTypeName().getLocalPart(), transaction);
    }

    /**
     * getFeatureWriterAppend
     * 
     * @param Transaction
     * @return FeatureWriter
     * @throws IOException
     */
    @Override
    public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(
            Transaction transaction) throws IOException {
        return getFeatureWriterAppend(getTypeName().getLocalPart(),
                transaction);
    }

    /**
     * createTypeNames
     * 
     * @return List
     * @throws IOException
     */
    @Override
    protected List<Name> createTypeNames() throws IOException {
        return Collections.singletonList(getTypeName());
    }

    /**
     * isMemoryMapped
     * 
     * @return
     */
    public boolean isMemoryMapped() {
        return memoryMapped;
    }

    /**
     * setMemoryMapped
     * 
     * @param memoryMapped
     */
    public void setMemoryMapped(boolean memoryMapped) {
        this.memoryMapped = memoryMapped;
    }

}
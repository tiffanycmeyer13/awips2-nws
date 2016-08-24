package gov.noaa.nws.ocp.common.geojson.datastore;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.QueryCapabilities;
import org.geotools.data.ResourceInfo;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.data.store.ContentState;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

/**
 * GeoJSONFeatureStore implementation
 * 
 * FeatureStore Implementation, compare to FeatureSource is for Feature Reading
 * and Writing
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date           Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 30, 2016   17912      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class GeoJSONFeatureStore extends ContentFeatureStore {

    /**
     * Delegate used for FeatureSource methods (We do this because Java cannot
     * inherit from both ContentFeatureStore and CSVFeatureSource at the same
     * time
     */
    private GeoJSONFeatureSource delegate = new GeoJSONFeatureSource(entry,
            query) {
        @Override
        public void setTransaction(Transaction transaction) {
            super.setTransaction(transaction);
            // Keep these two implementations on the same transaction
            GeoJSONFeatureStore.this.setTransaction(transaction);
        }
    };

    /**
     * GeoJSONFeatureStore constructor
     * 
     * @param entry
     * @param query
     */
    public GeoJSONFeatureStore(ContentEntry entry, Query query) {
        super(entry, query);
    }

    /**
     * getWriterInternal
     * 
     * @param Query
     * @param
     */
    @Override
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> getWriterInternal(
            Query query, int flags) throws IOException {
        return new GeoJSONFeatureWriter(getState(), query);
    }

    /**
     * setTransaction
     * 
     * @param Transaction
     */
    @Override
    public void setTransaction(Transaction transaction) {
        super.setTransaction(transaction);
        if (delegate.getTransaction() != transaction) {
            delegate.setTransaction(transaction);
        }
    }

    // Internal Delegate Methods
    // Implement FeatureSource methods using CSVFeatureSource implementation

    /**
     * buildFeatureType
     * 
     * @return SimpleFeatureType
     * @throws IOException
     */
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        return delegate.buildFeatureType();
    }

    /**
     * getBoundsInternal
     * 
     * @param Query
     * @return ReferencedEnvelope
     * @throws IOException
     */
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query)
            throws IOException {
        return delegate.getBoundsInternal(query);
    }

    /**
     * getCountInternal
     * 
     * @param Query
     * @return
     * @throws IOException
     */
    @Override
    protected int getCountInternal(Query query) throws IOException {
        return delegate.getCountInternal(query);
    }

    /**
     * getReaderInternal
     * 
     * @param Query
     * @return FeatureReader
     * @throws IOException
     */
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
            Query query) throws IOException {
        return delegate.getReaderInternal(query);
    }

    /**
     * handleVisitor
     * 
     * @param Query
     * @param FeatureVisitor
     * @return
     * @throws IOException
     */
    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor)
            throws IOException {
        return delegate.handleVisitor(query, visitor);
    }

    // Public Delegate Methods
    // Implement FeatureSource methods using CSVFeatureSource implementation

    /**
     * getDataStore
     * 
     * @return GeoJSONDataStore
     */
    @Override
    public GeoJSONDataStore getDataStore() {
        return delegate.getDataStore();
    }

    /**
     * getEntry
     * 
     * @return ContentEntry
     */
    @Override
    public ContentEntry getEntry() {
        return delegate.getEntry();
    }

    /**
     * getTransaction
     * 
     * @return Transaction
     */
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    /**
     * getState
     * 
     * @return ContentState
     */
    public ContentState getState() {
        return delegate.getState();
    }

    /**
     * getInfo
     * 
     * @return ResourceInfo
     */
    public ResourceInfo getInfo() {
        return delegate.getInfo();
    }

    /**
     * getName
     * 
     * @return Name
     */
    public Name getName() {
        return delegate.getName();
    }

    /**
     * getQueryCapabilities
     * 
     * @return QueryCapabilities
     */
    public QueryCapabilities getQueryCapabilities() {
        return delegate.getQueryCapabilities();
    }

}
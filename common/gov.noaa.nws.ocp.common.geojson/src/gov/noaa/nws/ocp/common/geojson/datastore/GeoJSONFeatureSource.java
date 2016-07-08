package gov.noaa.nws.ocp.common.geojson.datastore;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import gov.noaa.nws.ocp.common.geojson.datastore.util.GeoJSONFile;

/**
 * GeoJSONFeatureSource implementation
 * 
 * FeatureSource Implementation is mainly for Feature Reading
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 2, 2016            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class GeoJSONFeatureSource extends ContentFeatureSource {
    
    private GeoJSONFile gjson;
    

    /**
     * GeoJSONFeatureSource constructor
     * @param entry
     * @param query
     */
    public GeoJSONFeatureSource(ContentEntry entry, Query query) {
        super(entry, query);
        gjson = ((GeoJSONDataStore) super.getDataStore()).getGjson();
    }

    /**
     * getBoundsInternal
     * @param entry
     * @return ReferencedEnvelope
     */
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query)
            throws IOException {
        return gjson.getBounds();
    }

    /**
     * getCountInternal
     * @param Query
     * @return
     */
    @Override
    protected int getCountInternal(Query query) throws IOException {
        return gjson.getCount();
    }

    /**
     * getReaderInternal
     * @param Query
     * @return FeatureReader
     * @throws IOException
     */
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
            Query query) throws IOException {
        return new GeoJSONFeatureReader(getState(), query);
    }

    /**
     * buildFeatureType
     * @return SimpleFeatureType
     * @throws IOException
     */
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        return gjson.buildFeatureType();
    }
    
    /**
     * getDataStore
     * @return GeoJSONDataStore
     */
    public GeoJSONDataStore getDataStore() {
        return (GeoJSONDataStore) super.getDataStore();
    }
    
    /**
     * handleVisitor
     * Make handleVisitor package visible allowing CSVFeatureStore to delegate to
     * this implementation.
     */
    @Override
    protected boolean handleVisitor(Query query, FeatureVisitor visitor) throws IOException {
        return super.handleVisitor(query, visitor);
    }

}
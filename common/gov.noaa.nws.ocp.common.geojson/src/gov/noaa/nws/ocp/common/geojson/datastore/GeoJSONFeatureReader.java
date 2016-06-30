package gov.noaa.nws.ocp.common.geojson.datastore;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import gov.noaa.nws.ocp.common.geojson.datastore.util.GeoJSONFeatureCollection;

/**
 * GeoJSONFeatureReader
 * 
 * Implemented FeatureReader for reading SimpleFeature in the GeoJSON file
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date           Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 31, 2016   17912      pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class GeoJSONFeatureReader
        implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    /** State used when reading file */
    protected ContentState state;

    private GeoJSONFeatureCollection gjsonFeatures;

    private FeatureIterator<SimpleFeature> featureIter;

    /** The next feature */
    private SimpleFeature next;

    /**
     * GeoJSONFeatureReader constructor
     * 
     * @param contentState
     * @param query
     * @throws IOException
     */
    public GeoJSONFeatureReader(ContentState contentState, Query query)
            throws IOException {
        this.state = contentState;
        GeoJSONDataStore gjsonStore = (GeoJSONDataStore) contentState.getEntry()
                .getDataStore();
        gjsonFeatures = gjsonStore.read();

        if (hasValidFeatureCollection()) {
            featureIter = gjsonFeatures.features();
        }

    }

    /**
     * hasValidFeatureCollection Check if geojson file has been parsed and
     * populated
     * 
     * @return
     */
    private boolean hasValidFeatureCollection() {
        if (null == gjsonFeatures || gjsonFeatures.isEmpty()) {
            return false;
        } else {
            return true;
        }

    }

    /**
     * getFeatureType
     * 
     * @param SimpleFeatureType
     */
    public SimpleFeatureType getFeatureType() {
        if (!hasValidFeatureCollection()) {
            return null;
        }

        if (next == null) {
            next = featureIter.next();
        }
        return (SimpleFeatureType) next.getFeatureType();
    }

    /**
     * next Access the next feature (if available).
     * 
     * @return SimpleFeature read from property file
     * @throws IOException
     *             If problem encountered reading file
     * @throws IllegalAttributeException
     *             for invalid data
     * @throws NoSuchElementException
     *             If hasNext() indicates no more features are available
     */
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        SimpleFeature feature;
        if (next != null) {
            feature = next;
            next = null;
        } else {
            feature = featureIter.next();
        }
        return feature;
    }

    /**
     * hasNext Check if additional content is available.
     * 
     * @return <code>true</code> if additional content is available
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        } else if (featureIter == null) {
            return false;
        } else {
            return featureIter.hasNext();
        }
    }

    /**
     * Close the FeatureReader when not in use.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        next = null;
    }

}
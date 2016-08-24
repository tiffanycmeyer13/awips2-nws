package gov.noaa.nws.ocp.common.geojson.datastore;

import gov.noaa.nws.ocp.common.geojson.datastore.util.GeoJSONWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * GeoJSONFeatureWriter
 * 
 * Note: Associated GeoJSONWriter is not implemented. Write SimpleFeatures to
 * GeoJSON file is not supported in this moment
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

public class GeoJSONFeatureWriter
        implements FeatureWriter<SimpleFeatureType, SimpleFeature> {
    // State of current transaction
    private ContentState state;

    // Delegate handing reading of original file
    private GeoJSONFeatureReader delegate;

    // Temporary file used to stage output
    private File temp;

    // GeoJSONWriter used for temp file output
    private GeoJSONWriter gjsonWriter;

    // Current feature available for modification, may be null if feature
    // removed
    private SimpleFeature currentFeature;

    // Flag indicating we have reached the end of the file
    private boolean appending = false;

    /**
     * GeoJSONFeatureWriter
     * 
     * @param state
     * @param query
     * @throws IOException
     */
    public GeoJSONFeatureWriter(ContentState state, Query query)
            throws IOException {
        this.state = state;
        String typeName = query.getTypeName();
        File file = ((GeoJSONDataStore) state.getEntry().getDataStore())
                .getOriginalFile();
        File directory = file.getParentFile();
        this.temp = File.createTempFile(typeName + System.currentTimeMillis(),
                "gjson", directory);
        this.gjsonWriter = new GeoJSONWriter(new FileWriter(this.temp));
        this.delegate = new GeoJSONFeatureReader(state, query);
    }

    /**
     * getFeatureType
     * 
     * @return SimpleFeatureType
     */
    @Override
    public SimpleFeatureType getFeatureType() {
        return state.getFeatureType();
    }

    /**
     * nasNext
     */
    @Override
    public boolean hasNext() throws IOException {
        if (gjsonWriter == null) {
            return false;
        }
        if (this.appending) {
            return false; // reader has no more contents
        }
        return delegate.hasNext();
    }

    /**
     * next
     * 
     * @return SimpleFeature
     * @throws IOException,
     *             IllegalArgumentException, NoSuchElementException
     */
    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        if (gjsonWriter == null) {
            throw new IOException("FeatureWriter has been closed");
        }
        if (this.currentFeature != null) {
            this.write(); // the previous one was not written, so do it now.
        }
        try {
            if (!appending) {
                if (delegate.hasNext()) {
                    this.currentFeature = delegate.next();
                    return this.currentFeature;
                } else {
                    this.appending = true;
                }
            }
            SimpleFeatureType featureType = state.getFeatureType();
            String fid = featureType.getTypeName();
            Object values[] = DataUtilities.defaultValues(featureType);

            this.currentFeature = SimpleFeatureBuilder.build(featureType,
                    values, fid);
            return this.currentFeature;
        } catch (IllegalArgumentException invalid) {
            throw new IOException(
                    "Unable to create feature:" + invalid.getMessage(),
                    invalid);
        }
    }

    /**
     * Remove the feature
     * 
     * @throws IOException
     */
    public void remove() throws IOException {
        this.currentFeature = null;
    }

    /**
     * write the feature
     */
    public void write() throws IOException {
        if (this.currentFeature == null) {
            return;
        }
        for (Property property : currentFeature.getProperties()) {
            Object value = property.getValue();
            if (value == null) {
                this.gjsonWriter.write("");
            } else {
                this.gjsonWriter.write(currentFeature);
            }
        }
        this.currentFeature = null;
    }

    /**
     * close close writer and delegate and set them to null
     * 
     * @throws IOException
     *             if the Writer is closed but remaining contents need to be
     *             written
     */
    @Override
    public void close() throws IOException {
        if (gjsonWriter == null && (this.currentFeature != null || hasNext())) {
            throw new IOException(
                    "Writer already closed with remaining content need to be written.");
        }

        try {
            if (this.currentFeature != null) {
                // the previous one was not written, so do it now.
                this.write();
            }
            // Write out remaining contents, if have any
            while (hasNext()) {
                next();
                write();
            }
        } catch (IOException e) {
            throw new IOException(
                    "Contents are not completely written to a GeoJSON File.",
                    e);
        } finally {
            if (gjsonWriter != null) {
                gjsonWriter.close();
                gjsonWriter = null;
            }
            if (delegate != null) {
                this.delegate.close();
                this.delegate = null;
            }
        }

    }

}
package gov.noaa.nws.ocp.common.geojson.datastore.util;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.opengis.feature.simple.SimpleFeature;

/**
 * GeoJSONWriter, a Writer for GeoJSON Features
 * 
 * TODO: implement this class when GeoJSONDataStore is required to support
 * writing SimpleFeatures into GeoJSON file(s)
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 1, 2016            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class GeoJSONWriter {

    /**
     * Constructor
     * 
     * @param fw
     */
    public GeoJSONWriter(FileWriter fw) {

    }

    /**
     * write
     * 
     * @param feature
     */
    public void write(SimpleFeature feature) {

    }

    /**
     * write
     * 
     * @param f
     */
    public void write(File f) {

    }

    /**
     * write
     * 
     * @param string
     */
    public void write(String string) {

    }

    /**
     * write
     * 
     * @param bytes
     */
    public void write(Byte[] bytes) {

    }

    /**
     * close
     */
    public void close() {

    }

}
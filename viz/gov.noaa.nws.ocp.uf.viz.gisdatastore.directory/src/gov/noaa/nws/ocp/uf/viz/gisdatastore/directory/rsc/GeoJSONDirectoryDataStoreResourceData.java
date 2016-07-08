package gov.noaa.nws.ocp.uf.viz.gisdatastore.directory.rsc;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.geotools.data.DataStore;

import com.raytheon.uf.viz.gisdatastore.rsc.DataStoreResourceData;
import gov.noaa.nws.ocp.common.geojson.datastore.GeoJSONDirectoryFactory;

/**
 * DataStore resource data class for GIS feature files
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 15, 2016  17912     pwang       Initial creation
 * 
 * </pre>
 * 
 * @author pwang
 * @version 1.0
 */

@XmlAccessorType(XmlAccessType.NONE)
public class GeoJSONDirectoryDataStoreResourceData extends DataStoreResourceData {
    
    /**
     * GeoJSONDirectoryDataStoreResourceData constructor
     */
    public GeoJSONDirectoryDataStoreResourceData() {
    }

    /**
     * GeoJSONDirectoryDataStoreResourceData constructor
     * @param typeName
     * @param connectionParameters
     */
    public GeoJSONDirectoryDataStoreResourceData(String typeName,
            Map<String, Object> connectionParameters) {
        super(typeName, connectionParameters);
    }

   /**
    * constructDataStore
    * @return DataStore
    */
    @Override
    protected DataStore constructDataStore() throws IOException {
        GeoJSONDirectoryFactory factory = new GeoJSONDirectoryFactory();
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        for (Entry<String, Object> entry : getConnectionParameters().entrySet()) {
            if (entry.getValue() instanceof Serializable) {
                params.put(entry.getKey(), (Serializable) entry.getValue());
            } else {
                throw new IllegalArgumentException(entry.getKey() + "("
                        + entry.getValue().getClass().getName()
                        + ") not an instance of java.io.Serializable");
            }
        }
        DataStore dataStore = factory.createDataStore(params);
        return dataStore;
    }
}


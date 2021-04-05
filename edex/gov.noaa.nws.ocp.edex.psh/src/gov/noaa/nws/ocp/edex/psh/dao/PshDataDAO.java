/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.psh.dao;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.StorageProperties;
import com.raytheon.uf.common.datastorage.records.AbstractStorageRecord;
import com.raytheon.uf.common.datastorage.records.StringDataRecord;
import com.raytheon.uf.edex.database.plugin.PluginDao;

import gov.noaa.nws.ocp.common.dataplugin.psh.StormDataRecord;

/**
 * PshDataDAO
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date          Ticket#  Engineer   Description
 * ------------- -------- ---------- -------------------------------------------
 * Aug 12, 2017           pwang      Initial creation
 * Jan 25, 2018  45125    wpaintsil  Use path keys xml file instead of
 *                                   overriding persistToHDF5().
 * Apr 05, 2021  8374     randerso   Renamed IDataRecord.get/setProperties to
 *                                   get/setProps
 *
 * </pre>
 *
 * @author pwang
 */
public class PshDataDAO extends PluginDao {

    public PshDataDAO(String pluginName) throws PluginException {
        super(pluginName);
    }

    @Override
    protected IDataStore populateDataStore(IDataStore dataStore,
            IPersistable obj) throws Exception {

        AbstractStorageRecord storageRecord = null;
        StormDataRecord record = (StormDataRecord) obj;

        storageRecord = new StringDataRecord(StormDataRecord.STORMDATA_XML,
                record.getDataURI(), new String[] { record.getStormDataXML() });

        StorageProperties props = new StorageProperties();

        storageRecord.setProps(props);
        storageRecord.setCorrelationObject(record);
        dataStore.addDataRecord(storageRecord);

        return dataStore;
    }

}

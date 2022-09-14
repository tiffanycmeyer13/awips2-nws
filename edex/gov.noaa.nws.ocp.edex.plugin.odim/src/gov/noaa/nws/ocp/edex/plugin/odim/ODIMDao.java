/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.edex.plugin.odim;

import com.raytheon.uf.common.dataplugin.PluginException;
import com.raytheon.uf.common.dataplugin.persist.IPersistable;
import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.StorageProperties;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.DataUriMetadataIdentifier;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.IMetadataIdentifier;
import com.raytheon.uf.common.datastorage.records.ShortDataRecord;
import com.raytheon.uf.edex.core.dataplugin.PluginRegistry;
import com.raytheon.uf.edex.database.plugin.PluginDao;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMStoredData;

/**
 * DAO for ODIM data
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMDao extends PluginDao {

    public ODIMDao() throws PluginException {
        super("odim");
    }

    public ODIMDao(String pluginName) throws PluginException {
        super(pluginName);
    }

    @Override
    protected IDataStore populateDataStore(IDataStore dataStore,
            IPersistable obj) throws Exception {
        ODIMRecord pdo = (ODIMRecord) obj;
        StorageProperties sp = null;
        String compression = PluginRegistry.getInstance()
                .getRegisteredObject(pluginName).getCompression();
        if (compression != null) {
            sp = new StorageProperties();
            sp.setCompression(
                    StorageProperties.Compression.valueOf(compression));
        }

        IMetadataIdentifier metaId = new DataUriMetadataIdentifier(pdo);
        if (pdo.getRawData() != null) {
            IDataRecord rec = new ByteDataRecord(ODIMStoredData.RAW_DATA_ID,
                    pdo.getDataURI(), pdo.getRawData(), 2,
                    new long[] { pdo.getNumBins(), pdo.getNumRadials() });
            rec.setCorrelationObject(pdo);
            dataStore.addDataRecord(rec, metaId, sp);
        }
        if (pdo.getRawShortData() != null) {
            IDataRecord rec = new ShortDataRecord(ODIMStoredData.SHORT_DATA_ID,
                    pdo.getDataURI(), pdo.getRawShortData(), 2,
                    new long[] { pdo.getNumBins(), pdo.getNumRadials() });
            rec.setCorrelationObject(pdo);
            dataStore.addDataRecord(rec, metaId, sp);
        }
        if (pdo.getAngleData() != null) {
            IDataRecord rec = new FloatDataRecord(ODIMStoredData.ANGLE_DATA_ID,
                    pdo.getDataURI(), pdo.getAngleData(), 1,
                    new long[] { pdo.getNumRadials() });
            rec.setCorrelationObject(pdo);
            dataStore.addDataRecord(rec, metaId, sp);
        }

        return dataStore;
    }

}

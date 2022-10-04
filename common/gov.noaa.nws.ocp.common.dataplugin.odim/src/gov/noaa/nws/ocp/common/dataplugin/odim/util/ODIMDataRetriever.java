/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.odim.util;

import java.io.FileNotFoundException;

import com.raytheon.uf.common.datastorage.IDataStore;
import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.datastorage.records.ByteDataRecord;
import com.raytheon.uf.common.datastorage.records.FloatDataRecord;
import com.raytheon.uf.common.datastorage.records.IDataRecord;
import com.raytheon.uf.common.datastorage.records.ShortDataRecord;

import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMRecord;
import gov.noaa.nws.ocp.common.dataplugin.odim.ODIMStoredData;

/**
 * Retrieves bulk data from ODIM data stores.
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
public class ODIMDataRetriever {

    private ODIMDataRetriever() {
        // static methods only
    }

    /**
     * Populate the bulk data fields of the given ODIM record from the given
     * data store with ODIM plugin bulk data.
     *
     * @param dataStore
     * @param rec
     * @throws StorageException
     * @throws FileNotFoundException
     */
    public static void populateODIMRecord(IDataStore dataStore, ODIMRecord rec)
            throws StorageException, FileNotFoundException {
        populateODIMStoredData(dataStore, rec.getDataURI(),
                rec.getStoredData());
    }

    /**
     * Populate the given ODIMStoredData object from the given data store and
     * data URI.
     *
     * @param dataStore
     * @param rec
     * @throws StorageException
     * @throws FileNotFoundException
     * @return size of the retrieved data in bytes
     */
    public static int populateODIMStoredData(IDataStore dataStore,
            String dataURI, ODIMStoredData radarData)
            throws FileNotFoundException, StorageException {
        int size = 0;
        for (IDataRecord rec : dataStore.retrieve(dataURI)) {
            if (rec == null || rec.getName() == null) {
                continue;
            }
            size += rec.getSizeInBytes();
            if (ODIMStoredData.RAW_DATA_ID.equals(rec.getName())) {
                ByteDataRecord byteData = (ByteDataRecord) rec;
                radarData.setRawData(byteData.getByteData());
            } else if (ODIMStoredData.SHORT_DATA_ID.equals(rec.getName())) {
                ShortDataRecord byteData = (ShortDataRecord) rec;
                radarData.setRawShortData(byteData.getShortData());
            } else if (ODIMStoredData.ANGLE_DATA_ID.equals(rec.getName())) {
                FloatDataRecord floatData = (FloatDataRecord) rec;
                radarData.setAngleData(floatData.getFloatData());
            } else {
                size -= rec.getSizeInBytes();
            }
        }
        return size;
    }
}

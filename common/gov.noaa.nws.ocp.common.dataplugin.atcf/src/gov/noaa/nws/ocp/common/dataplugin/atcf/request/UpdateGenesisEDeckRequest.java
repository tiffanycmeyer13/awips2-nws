/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;

/**
 * Send a request to modify the genesisedeck table
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 11, 2021 #91551      jnengel     initial creation.
 * 
 * </pre>
 *
 * @author jnengel
 *
 */
@DynamicSerialize
public class UpdateGenesisEDeckRequest implements IServerRequest {
    @DynamicSerializeElement
    private List<ModifiedDeckRecord> modifiedGenesisEDeckRecords;

    public UpdateGenesisEDeckRequest() {
        modifiedGenesisEDeckRecords = new ArrayList<>();
    }

    public List<ModifiedDeckRecord> getModifiedGenesisEDeckRecords() {
        return modifiedGenesisEDeckRecords;
    }

    public void setModifiedGenesisEDeckRecords(
            List<ModifiedDeckRecord> modifiedGenesisEDeckRecords) {
        this.modifiedGenesisEDeckRecords = modifiedGenesisEDeckRecords;
    }

    public void addRecord(ModifiedDeckRecord modifiedRecord) {
        modifiedGenesisEDeckRecords.add(modifiedRecord);
    }

    public boolean isEmpty() {
        return modifiedGenesisEDeckRecords.isEmpty();
    }

}

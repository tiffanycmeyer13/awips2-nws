/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisBDeckRecord;


/**
 * NewGenesisRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 6, 2020 # 77134     pwang       Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class NewGenesisRequest implements IServerRequest {

    @DynamicSerializeElement
    private Genesis newGenesis;

    @DynamicSerializeElement
    private List<GenesisBDeckRecord> genesisBDeckRecord = new ArrayList<>();


    public Genesis getNewGenesis() {
        return newGenesis;
    }

    public void setNewGenesis(Genesis newGenesis) {
        this.newGenesis = newGenesis;
    }

    public List<GenesisBDeckRecord> getGenesisBDeckRecord() {
        return genesisBDeckRecord;
    }

    public void setGenesisBDeckRecord(List<GenesisBDeckRecord> genesisBDeckRecord) {
        this.genesisBDeckRecord = genesisBDeckRecord;
    }

    public void addGenesisBdeckRecord(GenesisBDeckRecord gbdeckRecord) {
        this.genesisBDeckRecord.add(gbdeckRecord);
    }

    public boolean hasGenesisBDeckRecord() {
        return !this.genesisBDeckRecord.isEmpty();
    }

}

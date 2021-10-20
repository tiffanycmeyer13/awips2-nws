/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * NewStormRequest
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 14, 2018            pwang     Initial creation
 * Jun 28, 2019            pwang     add BDeckRecord for a new storm
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class NewStormRequest implements IServerRequest {

    @DynamicSerializeElement
    private Storm newStorm;
    
    @DynamicSerializeElement
    private List<BDeckRecord> bdeckRecord = new ArrayList<>();


    /**
     * @return the newStorm
     */
    public Storm getNewStorm() {
        return newStorm;
    }

    /**
     * @param newStorm
     *            the newStorm to set
     */
    public void setNewStorm(Storm newStorm) {
        this.newStorm = newStorm;
    }

    /**
     * @return the bdeckRecord
     */
    public List<BDeckRecord> getBdeckRecord() {
        return bdeckRecord;
    }

    /**
     * @param bdeckRecord the bdeckRecord to set
     */
    public void setBdeckRecord(List<BDeckRecord> bdeckRecord) {
        this.bdeckRecord = bdeckRecord;
    }
    
    public void addBdeckRecord(BDeckRecord bdeckRecord) {
        this.bdeckRecord.add(bdeckRecord);
    }
    
    public boolean hasBDeckRecord() {
        return !this.bdeckRecord.isEmpty();
    }

}

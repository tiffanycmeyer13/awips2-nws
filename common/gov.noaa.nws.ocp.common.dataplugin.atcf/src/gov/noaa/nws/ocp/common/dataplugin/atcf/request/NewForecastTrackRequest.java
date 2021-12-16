/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.request;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * NewStormRequest
 * 1) to start a set of new Forecast, keep sandboxId <0
 * 2) to add more ForecastTrackRecord in the sandbox, provid a right sandboxId
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 11, 2019 #69593     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@DynamicSerialize
public class NewForecastTrackRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxId = -1;
    
    @DynamicSerializeElement
    private Storm currentStorm = null;
    
    @DynamicSerializeElement
    private List<ForecastTrackRecord> fstRecords = new ArrayList<>();
    
    @DynamicSerializeElement
    private String userId;


    /**
     * @return the sandboxId
     */
    public int getSandboxId() {
        return sandboxId;
    }

    /**
     * @param sandboxId the sandboxId to set
     */
    public void setSandboxId(int sandboxId) {
        this.sandboxId = sandboxId;
    }
    
    

    /**
     * @return the currentStorm
     */
    public Storm getCurrentStorm() {
        return currentStorm;
    }

    /**
     * @param currentStorm the currentStorm to set
     */
    public void setCurrentStorm(Storm currentStorm) {
        this.currentStorm = currentStorm;
    }

    /**
     * @return the fstRecords
     */
    public List<ForecastTrackRecord> getFstRecords() {
        return fstRecords;
    }

    /**
     * @param fstRecords the fstRecords to set
     */
    public void setFstRecords(List<ForecastTrackRecord> fstRecords) {
        this.fstRecords = fstRecords;
    }

    public void addFstRecord(ForecastTrackRecord fstRecord) {
        this.fstRecords.add(fstRecord);
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
}


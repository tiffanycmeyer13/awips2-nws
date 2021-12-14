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


/**
 * DeleteForecastTrackRecordRequest
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 6, 2020 # 72989     pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public class DeleteForecastTrackRecordRequest implements IServerRequest {

    @DynamicSerializeElement
    private int sandboxId;

    @DynamicSerializeElement
    private List<ForecastTrackRecord> fstRecords = new ArrayList<>();


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

}



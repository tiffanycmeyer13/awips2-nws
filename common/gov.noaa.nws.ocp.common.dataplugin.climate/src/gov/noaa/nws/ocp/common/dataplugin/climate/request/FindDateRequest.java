/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Request for most recent date from monthly/seasonal/annual and daily tables
 * for a stationID.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 18 OCT 2016  20636      wpaintsil   Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class FindDateRequest implements IServerRequest {

    @DynamicSerializeElement
    private int stationID;

    /**
     * Empty constructor.
     */
    public FindDateRequest() {
    }

    /**
     * Constructor.
     * 
     * @param iStationID
     *            station ID to search for.
     */
    public FindDateRequest(int iStationID) {
        stationID = iStationID;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int iStationID) {
        this.stationID = iStationID;
    }
}

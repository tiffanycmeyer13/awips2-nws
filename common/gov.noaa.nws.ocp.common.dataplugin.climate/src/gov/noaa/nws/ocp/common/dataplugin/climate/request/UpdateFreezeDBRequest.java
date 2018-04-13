
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.request;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.climate.ClimateDates;

/**
 * Request to update freeze dates for a station.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 28 JUL 2016  20636      wpaintsil      Initial creation
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 */

@DynamicSerialize
public class UpdateFreezeDBRequest implements IServerRequest {
    @DynamicSerializeElement
    private int type;

    @DynamicSerializeElement
    private int stationID;

    @DynamicSerializeElement
    private ClimateDates dates;

    /**
     * Empty constructor.
     */
    public UpdateFreezeDBRequest() {
    }

    /**
     * Constructor
     * 
     * @param type
     * @param stationID
     * @param dates
     */
    public UpdateFreezeDBRequest(int type, int stationID, ClimateDates dates) {
        this.type = type;
        this.stationID = stationID;
        this.dates = dates;
    }

    /**
     * @return the station
     */
    public int getStationID() {
        return stationID;
    }

    /**
     * @param stationID
     */
    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ClimateDates getDates() {
        return dates;
    }

    public void setDates(ClimateDates dates) {
        this.dates = dates;
    }

}

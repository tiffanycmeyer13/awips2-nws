/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.climate.rer;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * RecordClimate-related class for wrapping record station data.
 * 
 * <pre>
* SOFTWARE HISTORY
* 
* Date         Ticket#    Engineer    Description
* ------------ ---------- ----------- --------------------------
* NOV 29 2016  21100      amoore      Initial creation
 * </pre>
 * 
 * @author amoore
 *
 */
@DynamicSerialize
public class StationInfo {

    @DynamicSerializeElement
    private String stationID;

    @DynamicSerializeElement
    private String stationNameTextual;

    /**
     * Empty constructor.
     */
    public StationInfo() {
    }

    /**
     * @param id
     * @param name
     */
    public StationInfo(String id, String name) {
        stationID = id;
        stationNameTextual = name;
    }

    /**
     * @return the stationID
     */
    public String getStationID() {
        return stationID;
    }

    /**
     * @param stationID
     *            the stationID to set
     */
    public void setStationIDs(String stationID) {
        this.stationID = stationID;
    }

    /**
     * @return the stationNameTextual
     */
    public String getStationNameTextual() {
        return stationNameTextual;
    }

    /**
     * @param stationNameTextual
     *            the stationNameTextual to set
     */
    public void setStationNameTextuals(String stationNameTextual) {
        this.stationNameTextual = stationNameTextual;
    }
}

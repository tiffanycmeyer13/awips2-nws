/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.stationorder;

import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Mapping class for ordered list of stations.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2019 DR21671    wpaintsil   Initial creation
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 */

@XmlRootElement(name = "ClimateStationOrder")
@XmlAccessorType(XmlAccessType.NONE)
public class ClimateStationOrder {

    @XmlElements({ @XmlElement(name = "station") })
    private ArrayList<String> stations = new ArrayList<>();

    /**
     * Empty constructor.
     */
    public ClimateStationOrder() {
    }

    public ArrayList<String> getStations() {
        return stations;
    }

    public void setStations(ArrayList<String> stations) {
        this.stations = stations;
    }
}
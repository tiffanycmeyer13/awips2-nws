/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.climate.stationorder;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

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
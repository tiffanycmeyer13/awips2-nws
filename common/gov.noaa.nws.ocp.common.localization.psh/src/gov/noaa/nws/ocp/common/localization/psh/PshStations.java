/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.localization.psh;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * List of PSH stations.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 26 JUN 2017  #35269     jwu         Initial creation
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHStations")
@XmlAccessorType(XmlAccessType.NONE)
public class PshStations {

    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Station", type = PshStation.class) })
    private List<PshStation> stations;

    /**
     * Constructor
     */
    public PshStations() {
        stations = new ArrayList<>();
    }

    /**
     * @return the stations
     */
    public List<PshStation> getStations() {
        return stations;
    }

    /**
     * @param stations
     *            the stations to set
     */
    public void setStations(List<PshStation> stations) {
        this.stations = stations;
    }

    /**
     * Constructor
     */
    public PshStations(List<PshStation> stationList) {
        stations = new ArrayList<PshStation>(stationList);
    }

}

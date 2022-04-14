/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.localization.psh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * List of PSH cities.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 JUN 2017  #35269     jwu         Initial creation
 * 03 NOV 2017  #40407     jwu         Get list of tide gauge stations
 * 16 NOV 2017  #40987     jwu         Sort water level stations by name.
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHCities")
@XmlAccessorType(XmlAccessType.NONE)
public class PshCities {

    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "City", type = PshCity.class) })
    private List<PshCity> cities;

    /**
     * Constructor
     */
    public PshCities() {
    }

    /**
     * @return the cities
     */
    public List<PshCity> getCities() {
        if (cities == null) {
            cities = new ArrayList<>();
        }
        return cities;
    }

    /**
     * @param cities
     *            the cities to set
     */
    public void setCities(List<PshCity> cities) {
        this.cities = cities;
    }

    /**
     * Constructor
     */
    public PshCities(List<PshCity> cityList) {
        cities = new ArrayList<PshCity>(cityList);
    }

    /**
     * @return the cities that is designated as official tide gauges.
     */
    public List<PshCity> getTideGaugeStations() {
        List<PshCity> tgStations = new ArrayList<PshCity>();
        for (PshCity city : cities) {
            if ("G".equalsIgnoreCase(city.getGauge())) {
                tgStations.add(city);
            }
        }

        Collections.sort(tgStations);

        return tgStations;
    }

}
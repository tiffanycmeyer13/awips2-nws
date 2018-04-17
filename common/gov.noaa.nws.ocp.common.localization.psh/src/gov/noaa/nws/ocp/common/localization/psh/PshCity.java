/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.psh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * A PSH City.
 * 
 * This class holds the info of a single PSH City as in the legacy file
 * "cities_pipe.txt".
 * 
 * Note: STATATION ID is optional, and if it is a number (Urban Indicator), it
 * is ignored. "-" means no ID and no Urban Indicator. GAUGE FLAG is optional
 * too. If it presents, the "Offical Tide Gauge" in the Storm Surge/Tide Tab
 * will be activated automatically for the city.
 *
 * <pre>
 *  
 *      CITY NAME|COUNTY|STATE|LAT|LON|STATION ID|GAUGE FLAG
 * 
 *      ALVA|LEE|FL|26.7151|-81.6111
 *      ANNA MARIA ISLAND|MANATEE|FL|27.5289|-82.733|KTPA|G
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 29 JUN 2017  #35269     jwu         Initial creation
 * 31 OCT 2017  #39988     astrakovsky Added CWA field to city.
 * 27 NOV 2017  #41281     jwu         Use float for lat/lon.
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 * 
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHCity")
@XmlAccessorType(XmlAccessType.NONE)
public class PshCity implements Comparable<PshCity> {

    /**
     * Name
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Name")
    private String name;

    /**
     * County
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "County")
    private String county;

    /**
     * State
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "State")
    private String state;

    /**
     * Latitude
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Lat")
    private float lat;

    /**
     * Longitude
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Lon")
    private float lon;

    /**
     * Station ID
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "StationID")
    private String stationID;

    /**
     * CWA
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "CWA")
    private String cwa;

    /**
     * Official gauge
     */
    @DynamicSerializeElement
    @XmlAttribute(name = "Gauge")
    private String gauge;

    /**
     * Constructor.
     */
    public PshCity() {
    }

    /**
     * Constructor.
     * 
     * @param name
     *            City's name
     * @param county
     *            City's county
     * @param state
     *            City's state
     * @param lat
     *            Latitude
     * @param lon
     *            Longitude
     * @param stationID
     *            stationID
     * @param cwa
     *            CWA
     * @param gauge
     *            gauge
     * 
     */
    public PshCity(String name, String county, String state, float lat,
            float lon, String stationID, String cwa, String gauge) {
        this.name = name;
        this.county = county;
        this.state = state;
        this.lat = lat;
        this.lon = lon;
        this.stationID = stationID;
        this.cwa = cwa;
        this.gauge = gauge;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the county
     */
    public String getCounty() {
        return county;
    }

    /**
     * @param county
     *            the county to set
     */
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the lat
     */
    public float getLat() {
        return lat;
    }

    /**
     * @param lat
     *            the lat to set
     */
    public void setLat(float lat) {
        this.lat = lat;
    }

    /**
     * @return the lon
     */
    public float getLon() {
        return lon;
    }

    /**
     * @param lon
     *            the lon to set
     */
    public void setLon(float lon) {
        this.lon = lon;
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
    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    /**
     * @return the CWA
     */
    public String getCWA() {
        return cwa;
    }

    /**
     * @param cwa
     *            the CWA to set
     */
    public void setCWA(String cwa) {
        this.cwa = cwa;
    }

    /**
     * @return the gauge
     */
    public String getGauge() {
        return gauge;
    }

    /**
     * @param gauge
     *            the gauge to set
     */
    public void setGauge(String gauge) {
        this.gauge = gauge;
    }

    /**
     * @return a new object filled with default values.
     */
    public static PshCity getDefaultCity() {
        PshCity city = new PshCity();
        city.setDataToDefault();
        return city;
    }

    /**
     * Set data to default values.
     */
    public void setDataToDefault() {

        this.name = "";
        this.county = "";
        this.state = "";
        this.lat = -9999.0f;
        this.lon = -9999.0f;
        this.stationID = "";
        this.cwa = "";
        this.gauge = "";
    }

    @Override
    public int compareTo(PshCity aCity) {
        return this.name.compareTo(aCity.getName());
    }

}
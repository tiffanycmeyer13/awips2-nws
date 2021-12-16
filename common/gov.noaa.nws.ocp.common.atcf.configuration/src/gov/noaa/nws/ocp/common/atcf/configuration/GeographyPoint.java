/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.text.DecimalFormat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represent a geography location entry in
 * geography-(AL|CP|EP|WP).dat, which is used advisory composition.
 *
 * <pre>
 *
 * The file format is as follows:
 *
 * 00.0N  00.0W === EASTERN ATLANTIC ===
 * 16.0N  24.0W THE CABO VERDE ISLANDS
 * 14.8N  24.4W THE SOUTHERNMOST CABO VERDE ISLANDS
 * ...
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 03, 2020 82576      jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class GeographyPoint {

    /**
     * Latitude
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float lat;

    /**
     * Longitude
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float lon;

    /**
     * Name
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Constructor.
     */
    public GeographyPoint() {
        lat = 0;
        lon = 0;
        name = "";
    }

    /**
     * For when lat and lon are passed in as floats
     * 
     * @param lat
     * @param lon
     * @param name
     */
    public GeographyPoint(float lat, float lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    /**
     * @return the latitude
     */
    public float getLatitude() {
        return lat;
    }

    /**
     * @param lat
     *            the latitude to set
     */
    public void setLatitude(float lat) {
        this.lat = lat;
    }

    /**
     * @return the longitude
     */
    public float getLongitude() {
        return lon;
    }

    /**
     * @param lon
     *            the longitude to set
     */
    public void setLongitude(float lon) {
        this.lon = lon;
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
     * @return String representation of the latitude
     */
    public String getLatitudeText() {
        DecimalFormat df1 = new DecimalFormat("0.0");
        DecimalFormat df2 = new DecimalFormat("00.0");
        float tlat = Math.abs(this.lat);
        String dir = lat >= 0 ? "N" : "S";
        if (tlat > 0.0 && tlat < 10.0) {
            return String.format("%5s", (df1.format(tlat) + dir));
        } else {
            return String.format("%5s", (df2.format(tlat) + dir));
        }
    }

    /**
     * @return String representation of the longitude
     */
    public String getLongitudeText() {
        DecimalFormat df1 = new DecimalFormat("0.0");
        DecimalFormat df2 = new DecimalFormat("00.0");
        DecimalFormat df3 = new DecimalFormat("000.0");
        float tlon = Math.abs(this.lon);
        String dir = lon >= 0 ? "E" : "W";
        if (tlon > 0.0 && tlon < 10.0) {
            return String.format("%6s", (df1.format(tlon)) + dir);
        } else if (tlon >= 100.0) {
            return String.format("%6s", (df3.format(tlon)) + dir);
        } else {
            return String.format("%6s", (df2.format(tlon)) + dir);
        }
    }

    /**
     * Constructs a string representation of the point
     * 
     * @return String In format of "16.0N 24.0W THE CABO VERDE ISLANDS"
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getLatitudeText());
        sb.append(" ");
        sb.append(getLongitudeText());
        sb.append(" ");
        sb.append(String.format("%s", this.getName()));

        return sb.toString();
    }

}
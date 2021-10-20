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
 * This class represent a CPA location entry in cpa.loc, which is used for
 * displaying CPA locations and priorities.
 *
 * <pre>
 *
 * The file format is as follows:
 *
 * #
 * # Closest Point of Approach (CPA) definitions
 * #
 * # Destination names should not contain white spaces, max. 20 chars.
 * #
 * # Lat   Lon   Destination         Priority
 * #
 * 16.8N 099.9W Acapulco               0
 * 13.3N 144.8E Hagatna                0
 * ...
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2019 #59910     dmanzella   Created
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class CpaLocationEntry {

    /**
     * Destination
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String destination;

    /**
     * Priority number
     */
    @DynamicSerializeElement
    @XmlAttribute
    private int priority;

    /**
     * Latitude Value
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float lat;

    /**
     * Longitude Value
     */
    @DynamicSerializeElement
    @XmlAttribute
    private float lon;

    /**
     * Constructor.
     */
    public CpaLocationEntry() {
        lat = 0;
        lon = 0;
        destination = "";
        priority = 0;
    }

    /**
     * For when lat and lon are passed in as floats
     * 
     * @param lat
     * @param lon
     * @param destination
     * @param priority
     */
    public CpaLocationEntry(float lat, float lon, String destination,
            int priority) {
        this.destination = destination;
        this.priority = priority;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination
     *            the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
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
     * Constructs a string representation of a CPA Location Entry
     */
    public String toString() {
        DecimalFormat df = new DecimalFormat("###.#");
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%6s",
                (df.format(Math.abs(this.lat)) + (lat >= 0 ? "N" : "S"))));
        sb.append(" ");
        sb.append(String.format("%6s", df.format(Math.abs(this.lon)))
                + (lon >= 0 ? "E" : "W"));
        sb.append(" ");
        sb.append(String.format("%-20s", this.getDestination()));
        sb.append(" ");
        sb.append(String.format("%3d", (this.getPriority())));

        return sb.toString();
    }

}
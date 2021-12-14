/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represent geography locations in geography-(AL|CP|EP|WP).dat,
 * which are used advisory composition.
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
@XmlRootElement(name = "GeographyPoints")
@XmlAccessorType(XmlAccessType.NONE)
public class GeographyPoints {

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Point", type = GeographyPoint.class) })

    private List<GeographyPoint> geoPoints;

    /**
     * Constructor
     */
    public GeographyPoints() {
        geoPoints = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param geoPoints
     *            List of geography points
     */
    public GeographyPoints(List<GeographyPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }

    /**
     * @return the geoPoints
     */
    public List<GeographyPoint> getGeoPoints() {
        return geoPoints;
    }

    /**
     * @param geoPoints
     *            the geoPoints to set
     */
    public void setGeoPoints(List<GeographyPoint> geoPoints) {
        this.geoPoints = geoPoints;
    }

    /**
     * Add to the list
     * 
     * @param point
     */
    public void add(GeographyPoint point) {
        geoPoints.add(point);
    }

    /**
     * Remove a point
     * 
     * @param point
     * @return if point successfully removed
     */
    public boolean remove(GeographyPoint point) {
        return geoPoints.remove(point);
    }

    /**
     * Find the geography point with its full string representation.
     *
     * @return the entry
     */
    public GeographyPoint getPointByDesp(String desp) {
        GeographyPoint point = null;
        String dsp = desp.replaceAll("\\s", "");
        for (GeographyPoint pt : geoPoints) {
            if (dsp.equalsIgnoreCase(pt.toString().replaceAll("\\s", ""))) {
                point = pt;
                break;
            }
        }

        return point;
    }

    /**
     * Find the geography point with the given name.
     *
     * @return the entry
     */
    public GeographyPoint getPointByName(String name) {
        GeographyPoint point = null;
        for (GeographyPoint pt : geoPoints) {
            if (name.equalsIgnoreCase(pt.getName())) {
                point = pt;
                break;
            }
        }

        return point;
    }

    /**
     * Constructs a string representation of the data in the legacy format.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        for (GeographyPoint pt : geoPoints) {
            sb.append(pt.toString());
            sb.append(newline);
        }

        return sb.toString();
    }

}
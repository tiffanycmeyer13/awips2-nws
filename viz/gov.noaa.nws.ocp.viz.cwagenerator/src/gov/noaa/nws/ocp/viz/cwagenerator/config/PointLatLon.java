/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.cwagenerator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * Class for a point
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 10, 2021 28802      wkwock      Initial creation
 *
 * </pre>
 *
 * @author wkwock
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Point")
public class PointLatLon {

    @XmlAttribute(name = "Lon")
    private Double lon;

    @XmlAttribute(name = "Lat")
    private Double lat;

    public PointLatLon() {
    }

    public PointLatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }
}

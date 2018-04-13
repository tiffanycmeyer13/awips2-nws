/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.localization.psh.PshCity;

/**
 * TornadoDataEntry
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2017            pwang       Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@XmlRootElement(name = "TornadoDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class TornadoDataEntry extends StormDataEntry {

    @DynamicSerializeElement
    @XmlElement
    private PshCity location;

    @DynamicSerializeElement
    @XmlElement
    private String datetime;

    @DynamicSerializeElement
    @XmlElement
    private String magnitude;

    @DynamicSerializeElement
    @XmlElement
    private String direction;

    @DynamicSerializeElement
    @XmlElement
    private float distance;

    @DynamicSerializeElement
    @XmlElement
    private String incomplete;

    @DynamicSerializeElement
    @XmlElement
    private String remarks;

    public TornadoDataEntry() {

    }

    /**
     * @return the location
     */
    public PshCity getLocation() {
        return location;
    }

    /**
     * @param location
     *            the location to set
     */
    public void setLocation(PshCity location) {
        this.location = location;
    }

    /**
     * @return the datetime
     */
    public String getDatetime() {
        return datetime;
    }

    /**
     * @param datetime
     *            the datetime to set
     */
    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    /**
     * @return the magnitude
     */
    public String getMagnitude() {
        return magnitude;
    }

    /**
     * @param magnitude
     *            the magnitude to set
     */
    public void setMagnitude(String magnitude) {
        this.magnitude = magnitude;
    }

    /**
     * @return the direction
     */
    public String getDirection() {
        return direction;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks
     *            the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @param direction
     *            the direction to set
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * @return the distance
     */
    public float getDistance() {
        return distance;
    }

    /**
     * @param distance
     *            the distance to set
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }

    /**
     * @return the incomplete
     */
    public String getIncomplete() {
        return incomplete;
    }

    /**
     * @param incomplete
     *            the incomplete to set
     */
    public void setIncomplete(String incomplete) {
        this.incomplete = incomplete;
    }

}
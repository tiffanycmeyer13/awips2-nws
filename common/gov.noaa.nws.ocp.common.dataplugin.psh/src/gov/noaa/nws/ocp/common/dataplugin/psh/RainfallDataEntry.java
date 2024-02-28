/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.time.ZonedDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.nws.ocp.common.localization.psh.PshCity;

/**
 * RainfallDataEntry
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2017            pwang       Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * Jul 27, 2021 DCS22098   mporricelli Add reportDateTime
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@XmlRootElement(name = "RainfallDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class RainfallDataEntry extends StormDataEntry {

    @DynamicSerializeElement
    @XmlElement
    private PshCity city;

    @DynamicSerializeElement
    @XmlElement
    private float rainfall;

    @DynamicSerializeElement
    @XmlElement
    private String direction;

    @DynamicSerializeElement
    @XmlElement
    private float distance;

    @DynamicSerializeElement
    @XmlElement
    private String incomplete;

    private ZonedDateTime reportDateTime = null;

    public RainfallDataEntry() {
    }

    /**
     * @return the city
     */
    public PshCity getCity() {
        return city;
    }

    /**
     * @param city
     *            the city to set
     */
    public void setCity(PshCity city) {
        this.city = city;
    }

    /**
     * @return the rainfall
     */
    public float getRainfall() {
        return rainfall;
    }

    /**
     * @param rainfall
     *            the rainfall to set
     */
    public void setRainfall(float rainfall) {
        this.rainfall = rainfall;
    }

    /**
     * @return the direction
     */
    public String getDirection() {
        return direction;
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

    /**
     * @return reportDateTime - ZonedDateTime for LSR report
     */
    public ZonedDateTime getReportDateTime() {
        return reportDateTime;
    }

    /**
     * Set date/time
     *
     * @param reportDateTime
     *            the LSR report date/time
     */
    public void setReportDateTime(ZonedDateTime reportDateTime) {
        this.reportDateTime = reportDateTime;
    }

}
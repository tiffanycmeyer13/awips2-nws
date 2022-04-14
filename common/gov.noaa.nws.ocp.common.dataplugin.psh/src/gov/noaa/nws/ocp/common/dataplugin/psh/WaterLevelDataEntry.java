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
 * WaterLevelDataEntry - replace the legacy tide/surge.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2017            pwang       Initial creation
 * Nov,08  2017 #40423     jwu         Replace tide/surge with water level.
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@XmlRootElement(name = "WaterLevelDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class WaterLevelDataEntry extends StormDataEntry {

    @DynamicSerializeElement
    @XmlElement
    private PshCity location;

    @DynamicSerializeElement
    @XmlElement
    private float waterLevel;

    @DynamicSerializeElement
    @XmlElement
    private String datum;

    @DynamicSerializeElement
    @XmlElement
    private String datetime;

    @DynamicSerializeElement
    @XmlElement
    private String source;

    @DynamicSerializeElement
    @XmlElement
    private String incomplete;

    public WaterLevelDataEntry() {

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
     * @return the waterLevel
     */
    public float getWaterLevel() {
        return waterLevel;
    }

    /**
     * @param waterLevel
     *            the waterLevel to set
     */
    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    /**
     * @return the datum
     */
    public String getDatum() {
        return datum;
    }

    /**
     * @param datum
     *            the datum to set
     */
    public void setDatum(String datum) {
        this.datum = datum;
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
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
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


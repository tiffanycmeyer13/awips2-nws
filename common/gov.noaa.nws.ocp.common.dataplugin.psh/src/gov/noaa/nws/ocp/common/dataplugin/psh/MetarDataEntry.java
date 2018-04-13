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

/**
 * MetarDataEntry
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

@XmlRootElement(name = "MetarDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class MetarDataEntry extends StormDataEntry {

    @DynamicSerializeElement
    @XmlElement
    protected String site;

    @DynamicSerializeElement
    @XmlElement
    protected float lat;

    @DynamicSerializeElement
    @XmlElement
    protected float lon;

    @DynamicSerializeElement
    @XmlElement
    protected String minSeaLevelPres;

    @DynamicSerializeElement
    @XmlElement
    protected String minSeaLevelPresTime;

    @DynamicSerializeElement
    @XmlElement
    protected String minSeaLevelComplete;

    @DynamicSerializeElement
    @XmlElement
    protected String sustWind;

    @DynamicSerializeElement
    @XmlElement
    protected String sustWindTime;

    @DynamicSerializeElement
    @XmlElement
    protected String sustWindComplete;

    @DynamicSerializeElement
    @XmlElement
    protected String peakWind;

    @DynamicSerializeElement
    @XmlElement
    protected String peakWindTime;

    @DynamicSerializeElement
    @XmlElement
    protected String peakWindComplete;

    public MetarDataEntry() {

    }

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }

    /**
     * @param site
     *            the site to set
     */
    public void setSite(String site) {
        this.site = site;
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
     * @return the minSeaLevelPres
     */
    public String getMinSeaLevelPres() {
        return minSeaLevelPres;
    }

    /**
     * @param minSeaLevelPres
     *            the minSeaLevelPres to set
     */
    public void setMinSeaLevelPres(String minSeaLevelPres) {
        this.minSeaLevelPres = minSeaLevelPres;
    }

    /**
     * @return the minSeaLevelPresTime
     */
    public String getMinSeaLevelPresTime() {
        return minSeaLevelPresTime;
    }

    /**
     * @param minSeaLevelPresTime
     *            the minSeaLevelPresTime to set
     */
    public void setMinSeaLevelPresTime(String minSeaLevelPresTime) {
        this.minSeaLevelPresTime = minSeaLevelPresTime;
    }

    /**
     * @return the minSeaLevelComplete
     */
    public String getMinSeaLevelComplete() {
        return minSeaLevelComplete;
    }

    /**
     * @param minSeaLevelComplete
     *            the minSeaLevelComplete to set
     */
    public void setMinSeaLevelComplete(String minSeaLevelComplete) {
        this.minSeaLevelComplete = minSeaLevelComplete;
    }

    /**
     * @return the sustWind
     */
    public String getSustWind() {
        return sustWind;
    }

    /**
     * @param sustWind
     *            the sustWind to set
     */
    public void setSustWind(String sustWind) {
        this.sustWind = sustWind;
    }

    /**
     * @return the sustWindTime
     */
    public String getSustWindTime() {
        return sustWindTime;
    }

    /**
     * @param sustWindTime
     *            the sustWindTime to set
     */
    public void setSustWindTime(String sustWindTime) {
        this.sustWindTime = sustWindTime;
    }

    /**
     * @return the sustWindComplete
     */
    public String getSustWindComplete() {
        return sustWindComplete;
    }

    /**
     * @param sustWindComplete
     *            the sustWindComplete to set
     */
    public void setSustWindComplete(String sustWindComplete) {
        this.sustWindComplete = sustWindComplete;
    }

    /**
     * @return the peakWind
     */
    public String getPeakWind() {
        return peakWind;
    }

    /**
     * @param peakWind
     *            the peakWind to set
     */
    public void setPeakWind(String peakWind) {
        this.peakWind = peakWind;
    }

    /**
     * @return the peakWindTime
     */
    public String getPeakWindTime() {
        return peakWindTime;
    }

    /**
     * @param peakWindTime
     *            the peakWindTime to set
     */
    public void setPeakWindTime(String peakWindTime) {
        this.peakWindTime = peakWindTime;
    }

    /**
     * @return the peakWindComplete
     */
    public String getPeakWindComplete() {
        return peakWindComplete;
    }

    /**
     * @param peakWindComplete
     *            the peakWindComplete to set
     */
    public void setPeakWindComplete(String peakWindComplete) {
        this.peakWindComplete = peakWindComplete;
    }

}

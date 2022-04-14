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
 * EffectDataEntry
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 06/20/2017              pwang       Initial creation
 * 01/11/2018   DCS19326   jwu         Baseline version.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

@XmlRootElement(name = "EffectDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class EffectDataEntry extends StormDataEntry {

    @DynamicSerializeElement
    @XmlElement
    private String county;

    @DynamicSerializeElement
    @XmlElement
    private int deaths = 0;

    @DynamicSerializeElement
    @XmlElement
    private int injuries = 0;

    @DynamicSerializeElement
    @XmlElement
    private int evacuations = 0;

    @DynamicSerializeElement
    @XmlElement
    private String remarks;

    public EffectDataEntry() {

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
     * @return the deaths
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * @param deaths
     *            the deaths to set
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     * @return the injuries
     */
    public int getInjuries() {
        return injuries;
    }

    /**
     * @param injuries
     *            the injuries to set
     */
    public void setInjuries(int injuries) {
        this.injuries = injuries;
    }

    /**
     * @return the evacuations
     */
    public int getEvacuations() {
        return evacuations;
    }

    /**
     * @param evacuations
     *            the evacuations to set
     */
    public void setEvacuations(int evacuations) {
        this.evacuations = evacuations;
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

}

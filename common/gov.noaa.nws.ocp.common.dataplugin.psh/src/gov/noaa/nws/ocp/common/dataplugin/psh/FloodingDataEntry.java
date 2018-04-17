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
 * FloodingDataEntry
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

@XmlRootElement(name = "FloodingDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class FloodingDataEntry extends StormDataEntry {

    @DynamicSerializeElement
    @XmlElement
    private String county;

    @DynamicSerializeElement
    @XmlElement
    private String remarks;

    public FloodingDataEntry() {

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

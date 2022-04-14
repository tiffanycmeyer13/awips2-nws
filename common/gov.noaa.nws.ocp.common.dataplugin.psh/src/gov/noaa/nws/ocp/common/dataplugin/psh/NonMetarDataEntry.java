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
 * NonMetarDataEntry
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

@XmlRootElement(name = "NonMetarDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class NonMetarDataEntry extends MarineDataEntry {

    @DynamicSerializeElement
    @XmlElement
    protected String estWind;

    public NonMetarDataEntry() {

    }

    /**
     * @return the estWind
     */
    public String getEstWind() {
        return estWind;
    }

    /**
     * @param estWind
     *            the estWind to set
     */
    public void setEstWind(String estWind) {
        this.estWind = estWind;
    }

}

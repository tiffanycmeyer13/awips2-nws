/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * MarineDataEntry
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
@XmlRootElement(name = "MarineDataEntry")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class MarineDataEntry extends MetarDataEntry {

    @DynamicSerializeElement
    @XmlElement
    protected String anemHgmt;

    public MarineDataEntry() {

    }

    /**
     * @return the anemHgmt
     */
    public String getAnemHgmt() {
        return anemHgmt;
    }

    /**
     * @param anemHgmt
     *            the anemHgmt to set
     */
    public void setAnemHgmt(String anemHgmt) {
        this.anemHgmt = anemHgmt;
    }

}

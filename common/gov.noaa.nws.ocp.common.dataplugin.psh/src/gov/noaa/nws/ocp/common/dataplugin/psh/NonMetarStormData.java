/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Contains a list of non-METAR data entries.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 12, 2017 #39468     jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@XmlRootElement(name = "NonMetarStormData")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class NonMetarStormData extends StormData {

    @DynamicSerializeElement
    @XmlElements(@XmlElement(name = "NonMetarData", type = NonMetarDataEntry.class))
    private List<NonMetarDataEntry> data;

    public NonMetarStormData() {
        super();
    }

    /**
     * @return the data
     */
    public List<NonMetarDataEntry> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(List<NonMetarDataEntry> data) {
        this.data = data;
    }

}

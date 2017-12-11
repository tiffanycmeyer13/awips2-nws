/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Contains a list of PSH storm tornado data entries.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 12, 2017            jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@XmlRootElement(name = "TornadoStormData")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class TornadoStormData extends StormData {

    @DynamicSerializeElement
    @XmlElements(@XmlElement(name = "TornadoData", type = TornadoDataEntry.class))
    private List<TornadoDataEntry> data;

    public TornadoStormData() {
        super();
    }

    /**
     * @return the data
     */
    public List<TornadoDataEntry> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(List<TornadoDataEntry> data) {
        this.data = data;
    }

}

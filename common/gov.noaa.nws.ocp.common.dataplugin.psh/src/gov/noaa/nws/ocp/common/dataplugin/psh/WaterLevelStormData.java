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
 * Contains a list of PSH maximum water level data entries.
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
@XmlRootElement(name = "WaterLevelStormData")
@XmlAccessorType(XmlAccessType.FIELD)
@DynamicSerialize
public class WaterLevelStormData extends StormData {

    @DynamicSerializeElement
    @XmlElements(@XmlElement(name = "WaterLevelData", type = WaterLevelDataEntry.class))
    private List<WaterLevelDataEntry> data;

    public WaterLevelStormData() {
        super();
    }

    /**
     * @return the data
     */
    public List<WaterLevelDataEntry> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    /**
     * @param data
     *            the data to set
     */
    public void setData(List<WaterLevelDataEntry> data) {
        this.data = data;
    }

}

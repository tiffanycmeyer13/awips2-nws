/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Wrapper for XML adapter of {@code Configuration.modelConfiguration}
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul  8, 2020 78599      dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ModelConfigList {
    @XmlElement(name = "model")
    private ArrayList<ModelConfig> list = new ArrayList<>();

    public ModelConfigList() {
    }

    public ModelConfigList(Collection<ModelConfig> list) {
        this.list = new ArrayList<>(list);
    }

    public List<ModelConfig> getList() {
        return list;
    }

}

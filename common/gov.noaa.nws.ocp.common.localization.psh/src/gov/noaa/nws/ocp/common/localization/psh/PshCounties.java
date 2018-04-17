/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.localization.psh;

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
 * List of PSH county names.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 16 JUN 2017  #35269     jwu         Initial creation
 * 02 OCT 2017  #38429     astrakovsky Fixed error caused by constructor.
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PSHCounties")
@XmlAccessorType(XmlAccessType.NONE)
public class PshCounties {

    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Name", type = String.class) })
    private List<String> counties;

    /**
     * Constructor
     */
    public PshCounties() {
        counties = new ArrayList<>();
    }

    /**
     * Constructor
     * 
     * @param counties
     *            List of Psh county names
     */
    public PshCounties(List<String> counties) {
        this.counties = counties;
    }

    /**
     * @return the counties
     */
    public List<String> getCounties() {
        return counties;
    }

    /**
     * @param counties
     *            the counties to set
     */
    public void setCounties(List<String> counties) {
        this.counties = counties;
    }
}

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
 * List of PSH Forecasters.
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
@XmlRootElement(name = "PSHForecasters")
@XmlAccessorType(XmlAccessType.NONE)
public class PshForecasters {

    @DynamicSerializeElement
    @XmlElements({
 @XmlElement(name = "Name", type = String.class) })
    private List<String> forecasters;

    /**
     * Constructor
     */
    public PshForecasters() {
        forecasters = new ArrayList<>();
    }

    /**
     * Constructor
     * 
     * @param list
     *            List of Psh forecaster
     */
    public PshForecasters(List<String> list) {
        this.forecasters = list;
    }

    /**
     * @return the forecasters
     */
    public List<String> getForecasters() {
        return forecasters;
    }

    /**
     * @param forecasters
     *            the forecasters to set
     */
    public void setForecasters(List<String> forecasters) {
        this.forecasters = forecasters;
    }
}

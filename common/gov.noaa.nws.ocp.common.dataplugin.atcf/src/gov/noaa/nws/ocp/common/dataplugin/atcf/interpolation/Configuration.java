/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Interpolator configuration
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
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class Configuration {
    @XmlElement(name="models")
    @XmlJavaTypeAdapter(value = ModelConfigMapAdapter.class)
    private Map<String, ModelConfig> modelConfiguration = new HashMap<>();

    @XmlElement
    private int nSmooth = 10;

    @XmlElement
    private int fcstHourOutputFrequency = 6;

    public Map<String, ModelConfig> getModelConfiguration() {
        return modelConfiguration;
    }

    public void setModelConfiguration(
            Map<String, ModelConfig> modelConfiguration) {
        this.modelConfiguration = modelConfiguration;
    }

    public int getnSmooth() {
        return nSmooth;
    }

    public void setnSmooth(int nSmooth) {
        this.nSmooth = nSmooth;
    }

    public int getFcstHourOutputFrequency() {
        return fcstHourOutputFrequency;
    }

    public void setFcstHourOutputFrequency(int fcstHourOutputFrequency) {
        this.fcstHourOutputFrequency = fcstHourOutputFrequency;
    }

}

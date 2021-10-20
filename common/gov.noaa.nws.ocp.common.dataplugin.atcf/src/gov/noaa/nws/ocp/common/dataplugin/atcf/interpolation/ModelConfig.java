/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Interpolator configuration for a given model type
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
public class ModelConfig {
    /** Deck file technique name for the given model */
    @XmlAttribute(name = "input")
    private String inputName;

    /** Deck file technique name for the given model's interpolated output */
    @XmlAttribute(name = "output")
    private String outputName;

    /**
     * Minimum forecast hour to start linearly relaxing to model from initial
     * value adjustments. Up to and including this hour, apply the full
     * adjustment.
     */
    @XmlAttribute(name = "minRelaxFH")
    private int minRelaxFcstHour;

    /**
     * Maximum forecast hour to stop linearly relaxing to model from initial
     * value adjustments. At and after this hour, do not apply the adjustment at
     * all.
     */
    @XmlAttribute(name = "maxRelaxFH")
    private int maxRelaxFcstHour;

    private ModelConfig() {

    }

    public ModelConfig(String inputName, String outputName, int minFcstHour, int maxFcstHour) {
        super();
        this.inputName = inputName;
        this.outputName = outputName;
        this.minRelaxFcstHour = minFcstHour;
        this.maxRelaxFcstHour = maxFcstHour;
    }

    public String getInputName() {
        return inputName;
    }

    public String getOutputName() {
        return outputName;
    }

    public int getMinRelaxFcstHour() {
        return minRelaxFcstHour;
    }

    public int getMaxRelaxFcstHour() {
        return maxRelaxFcstHour;
    }
}

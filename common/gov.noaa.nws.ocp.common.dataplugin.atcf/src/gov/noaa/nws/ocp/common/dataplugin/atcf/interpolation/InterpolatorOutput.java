/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.Calendar;
import java.util.Map;

/**
 * Output of the interpolator
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
public class InterpolatorOutput {
    /**
     * Input time adjusted by input forecast hour offset
     */
    private Calendar dtg;

    /**
     * Map of model name -> bucket key -> ModelData. DeckInterpolator uses the
     * bucket key to group on wind intensity (34kt, 50kt, 64kt). The key must be
     * immutable and sharable.
     */
    private Map<String, Map<Object, ModelData>> modelData;

    public InterpolatorOutput(Calendar dtg, Map<String, Map<Object, ModelData>> modelData) {
        if (dtg == null || modelData == null) {
            throw new IllegalArgumentException(
                    "InterpolationOutput args must not be null.");
        }
        this.dtg = dtg;
        this.modelData = modelData;
    }

    public Calendar getDtg() {
        return dtg;
    }

    public void setDtg(Calendar dtg) {
        this.dtg = dtg;
    }

    public Map<String, Map<Object, ModelData>> getModelData() {
        return modelData;
    }

    public void setModelData(Map<String, Map<Object, ModelData>> modelData) {
        this.modelData = modelData;
    }
}

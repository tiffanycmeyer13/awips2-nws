/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.Map;

/**
 * Encapsulates input to the interpolator excluding "configuration".
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
public class InterpolationInput {
    private InterpolationArgs args;

    /**
     * Map of model name -> bucket key -> ModelData. DeckInterpolator uses the
     * bucket key to group on wind intensity (34kt, 50kt, 64kt).The key must be
     * immutable and sharable.
     *
     * TODO: May need additional requirements about all having the same number
     * of fields and there must be an (offset) zero hour in first ModelData as
     * sorted by the bucket key.
     */
    private Map<String, Map<Object, ModelData>> modelData;

    public InterpolationInput(InterpolationArgs args,
            Map<String, Map<Object, ModelData>> modelData) {
        if (args == null || modelData == null) {
            throw new IllegalArgumentException(
                    "InterpolationInput args must not be null.");
        }
        this.args = args;
        this.modelData = modelData;
    }

    public InterpolationArgs getArgs() {
        return args;
    }

    public void setArgs(InterpolationArgs args) {
        this.args = args;
    }

    public Map<String, Map<Object, ModelData>> getModelData() {
        return modelData;
    }

    public void setModelData(Map<String, Map<Object, ModelData>> modelData) {
        this.modelData = modelData;
    }
}

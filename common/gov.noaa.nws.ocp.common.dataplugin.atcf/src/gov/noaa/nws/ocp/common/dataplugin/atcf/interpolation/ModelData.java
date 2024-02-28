/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

/**
 * Interpolator input data for a given model (and wind radii bucket)
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
public class ModelData {
    /** Forecast hour values */
    public int[] fcstHour;

    /**
     * Interpolated field values. There is one element for each given forecast
     * hour.
     */
    public float[][] fields;

    public ModelData(int[] fcstHour, float[][] fields) {
        this.fcstHour = fcstHour;
        this.fields = fields;
    }
}
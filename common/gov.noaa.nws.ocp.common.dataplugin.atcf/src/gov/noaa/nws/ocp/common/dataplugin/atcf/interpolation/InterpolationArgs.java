/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import java.util.Date;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Encapsulates arguments to the interpolator that are neither "configuration"
 * nor the input deck data.
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
@DynamicSerialize
public class InterpolationArgs implements Cloneable {
    @DynamicSerializeElement
    private Date dtg;

    @DynamicSerializeElement
    private int fcstHourOffset;

    @DynamicSerializeElement
    private float[] initialFields;

    @DynamicSerializeElement
    private String[] models;

    public InterpolationArgs() {
    }

    public InterpolationArgs(Date dtg, int fcstHourOffset,
            float[] initialFields, String[] models) {
        this.dtg = dtg;
        this.fcstHourOffset = fcstHourOffset;
        this.initialFields = initialFields;
        this.models = models;
    }

    public InterpolationArgs clone() {
        try {
            return (InterpolationArgs) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Date getDtg() {
        return dtg;
    }

    public void setDtg(Date dtg) {
        this.dtg = dtg;
    }

    public int getFcstHourOffset() {
        return fcstHourOffset;
    }

    public void setFcstHourOffset(int fcstHourOffset) {
        this.fcstHourOffset = fcstHourOffset;
    }

    public float[] getInitialFields() {
        return initialFields;
    }

    public void setInitialFields(float[] initialFields) {
        this.initialFields = initialFields;
    }

    public String[] getModels() {
        return models;
    }

    public void setModels(String[] models) {
        this.models = models;
    }
}

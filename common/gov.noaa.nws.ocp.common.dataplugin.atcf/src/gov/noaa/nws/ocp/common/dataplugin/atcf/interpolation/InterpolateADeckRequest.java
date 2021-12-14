/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf.interpolation;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;

/**
 * Request to performs interpolation on a set of records in the A deck.
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
public class InterpolateADeckRequest implements IServerRequest {
    @DynamicSerializeElement
    private Storm storm;

    @DynamicSerializeElement
    private InterpolationArgs args;

    @DynamicSerializeElement
    private BaseADeckRecord initialValues;

    public Storm getStorm() {
        return storm;
    }

    public void setStorm(Storm storm) {
        this.storm = storm;
    }

    public InterpolationArgs getArgs() {
        return args;
    }

    public void setArgs(InterpolationArgs args) {
        this.args = args;
    }

    public BaseADeckRecord getInitialValues() {
        return initialValues;
    }

    public void setInitialValues(BaseADeckRecord initialValues) {
        this.initialValues = initialValues;
    }

}

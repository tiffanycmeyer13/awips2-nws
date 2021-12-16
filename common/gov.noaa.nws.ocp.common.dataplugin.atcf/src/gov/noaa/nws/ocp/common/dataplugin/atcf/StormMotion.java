/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Enumeration of cyclone mover S, R, O - straight mover, recurver, other.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 27, 2020 82623      jwu  Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
@DynamicSerialize
public enum StormMotion {
    S("Straight"), R("Recurver"), O("Other");

    @DynamicSerializeElement
    private String value;

    /**
     * @param iValue
     */
    private StormMotion(final String iValue) {
        this.value = iValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

}

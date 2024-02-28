/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This enum lists the radar type in an ATCF fix record (FDeckRecord).
 *
 * <pre>
 *     Radar Type: (1 char)
 *     L - Land; S - Ship; A - Aircraft; T - Satellite
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2020 85083      jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public enum RadarType {
    L("Land"), S("Ship"), A("Aircraft"), T("Satellite");

    @DynamicSerializeElement
    private String value;

    /**
     * @param iValue
     */
    private RadarType(final String iValue) {
        this.value = iValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get a RadarType by its name.
     *
     * @ name
     *
     * @return RadarType
     */
    public static RadarType getRadarType(String name) {
        for (RadarType rt : RadarType.values()) {
            if (name.equals(rt.name())) {
                return rt;
            }
        }
        return null;
    }

}

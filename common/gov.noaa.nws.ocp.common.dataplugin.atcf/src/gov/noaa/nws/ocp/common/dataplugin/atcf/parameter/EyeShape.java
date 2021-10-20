/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf.parameter;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * This enum holds the ATCF Eye Shapes.
 *
 * <pre>
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 3, 2019  68738      dmanzella   Created
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
/**
 * Enum representing the Eye Shape in the Aircraft tab
 */
public enum EyeShape {
    CO("CO - Concentric"), CI("CI - circ"), EL("EL - Elliptic");

    private String value;

    private EyeShape(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param val
     * @return
     */
    public static String getNameFromString(String val) {
        String ret = "";
        for (EyeShape name : EyeShape.values()) {
            if (val.equals(name.getValue())) {
                ret = name.name();
            }
        }
        return ret;
    }

    /**
     * @param val
     * @return
     */
    public static String getValueFromString(String value) {
        String ret = "";
        for (EyeShape name : EyeShape.values()) {
            if (value.equals(name.name())) {
                ret = name.getValue();
            }
        }
        return ret;
    }

}

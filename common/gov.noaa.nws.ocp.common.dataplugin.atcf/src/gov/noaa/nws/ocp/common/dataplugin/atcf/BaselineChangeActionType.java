/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * BaselineChangeActionType
 * Used for distinguish why the baseline data changed
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 15, 2020 #          pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public enum BaselineChangeActionType {
    EDIT("Merge edited records"), WCOSS("Merge from WCOSS output"), EXTREP(
            "Replaced by external deck"), EXTMERGE("Merge external deck");

    @DynamicSerializeElement
    private String value;

    /**
     * @param iValue
     */
    private BaselineChangeActionType(final String iValue) {
        this.value = iValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
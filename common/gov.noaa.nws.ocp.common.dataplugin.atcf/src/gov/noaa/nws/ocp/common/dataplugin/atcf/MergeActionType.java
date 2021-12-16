/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

@DynamicSerialize
public enum MergeActionType {
    USEBASELINE("Use baseline record"), USESANDBOX(
            "Keep sandbox record"), MERGED("Mix records as new");

    @DynamicSerializeElement
    private String value;

    /**
     * @param iValue
     */
    private MergeActionType(final String iValue) {
        this.value = iValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
}

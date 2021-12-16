/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

@DynamicSerialize
public enum AtcfModeType {
    OP("OPERATION MODE"), VO("VIEW ONLY MODE"), SIM("SIMULATION MODE");

    @DynamicSerializeElement
    private String value;

    /**
     * @param iValue
     */
    private AtcfModeType(final String iValue) {
        this.value = iValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
}


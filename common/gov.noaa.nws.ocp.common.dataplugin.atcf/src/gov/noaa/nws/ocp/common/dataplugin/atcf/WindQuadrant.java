/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Radius Code for wind intensity: AAA = full circle; QQQ = quadrant (NNQ, NEQ,
 * EEQ, SEQ, SSQ, SWQ, WWQ, NWQ)
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2019 61882     jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public enum WindQuadrant {
    AAA("AAA"),
    NNQ("NNQ"),
    NEQ("NEQ"),
    EEQ("EEQ"),
    SEQ("SEQ"),
    SSQ("SSQ"),
    SWQ("SWQ"),
    WWQ("WWQ"),
    NWQ("NWQ");

    @DynamicSerializeElement
    private String value;

    /**
     * @param iValue
     */
    private WindQuadrant(final String iValue) {
        this.value = iValue;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
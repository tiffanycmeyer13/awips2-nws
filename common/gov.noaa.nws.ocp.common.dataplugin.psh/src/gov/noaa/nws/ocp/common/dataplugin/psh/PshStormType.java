/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Enumerates PSH storm types.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 05, 2017 #37365     jwu         Initial creation
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "StormType")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum PshStormType {

    /**
     * Sub-tropical storm
     */
    SUBTROPICAL_STORM(1, "Subtropical Storm"),

    /**
     * Tropical depression
     */
    TROPICAL_DEPRESSION(2, "Tropical Depression"),

    /**
     * Tropical storm
     */
    TROPICAL_STORM(3, "Tropical Storm"),

    /**
     * Hurricane
     */
    HURRICANE(4, "Hurricane");

    @DynamicSerializeElement
    private int code;

    @DynamicSerializeElement
    private String desc;

    /**
     * Constructor
     * 
     * @param code
     * @param desc
     */
    private PshStormType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code
     *            the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc
     *            the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

}
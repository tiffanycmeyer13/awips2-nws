/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.localization.psh;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This enum holds the storm basin names in PSH.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 26 JUN 2017  #35269     jwu         Initial creation
 * 06 JUL 2017  #35269     jwu         Add dirName
 * 11 JAN 2018  DCS19326   jwu         Baseline version.
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "PshBasin")
@XmlAccessorType(XmlAccessType.NONE)
@XmlEnum
public enum PshBasin {

    /**
     * Atlantic.
     */
    AT(0, "Atlantic", "atlantic"),

    /**
     * Eastern.
     */
    EP(1, "East Pacific", "eastpac"),

    /**
     * Central.
     */
    CP(2, "Central Pacific", "centralpac"),

    /**
     * West.
     */
    WP(3, "West Pacific", "westpac");

    @DynamicSerializeElement
    private int value;

    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private String dirName;

    /**
     * @param iValue
     */
    private PshBasin(final int iValue, final String name,
            final String dirName) {
        this.value = iValue;
        this.name = name;
        this.dirName = dirName;
    }

    /**
     * @return enum value.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param iValue
     *            value.
     */
    public void setValue(int iValue) {
        value = iValue;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the dirName
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * @param dirName
     *            the dirName to set
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * Find a PSH basin based on name.
     * 
     * @param name
     *            name such as "AT" or "Atlantic"
     */
    public static PshBasin getPshBasin(String basin) {
        PshBasin ptz = PshBasin.AT;
        if (basin != null) {
            for (PshBasin bn : PshBasin.values()) {
                if (bn.toString().equals(basin.toUpperCase())
                        || bn.getName().equals(basin)) {
                    ptz = bn;
                    break;
                }
            }
        }

        return ptz;
    }

}
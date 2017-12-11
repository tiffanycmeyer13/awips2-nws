/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.psh;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * PshDataCategory
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 16, 2017            pwang       Initial creation
 * Sep 28, 2017 #38374     wpaintsil   Consolidate with PshTabType.java
 * Nov,08  2017 #40423     jwu         Replace tide/surge with water level.
 * Jan 11, 2018 DCS19326   jwu         Baseline version.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
@DynamicSerialize
public enum PshDataCategory {

    /**
     * Metar observations
     */
    METAR(0, "Metar", "Metar Observations"),

    /**
     * Non-Metar observation
     */
    NON_METAR(1, "Non-Metar", "Non-Metar Observations"),

    /**
     * Marine
     */
    MARINE(2, "Marine", "Marine Observations"),

    /**
     * Storm rainfall
     */
    RAINFALL(3, "Storm Rainfall", "Storm Total Rainfall"),

    /**
     * Inland flooding
     */
    FLOODING(4, "Inland Flooding", "Inland Flooding by County"),

    /**
     * Maximum water level
     */
    WATER_LEVEL(5, "Water Level",
            "Maximum Observed Water Level (WL) by Gauge Station"),
    /**
     * Tornadoes
     */
    TORNADO(6, "Tornadoes", "Tornadoes by County"),

    /**
     * Storm effects
     */
    EFFECT(7, "Storm Effects", "Storm Effects by County"),

    /**
     * None of the above
     */
    UNKNOWN(-1, "Unknown Category", "Unknown Category");

    private int code;

    private String name;

    private String desc;

    private PshDataCategory(int code, String name, String desc) {
        this.code = code;
        this.name = name;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return a description tab data in the GUI
     */
    public String getDesc() {
        return desc;
    }

}

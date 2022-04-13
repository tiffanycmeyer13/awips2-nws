/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.psh.ui.generator;

/**
 * Enum containing the types of storm hazards to be used in the LSR Dialog.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2017 #36919     wpaintsil   Initial creation.
 * 
 * </pre>
 * 
 * @author wpaintsil
 * @version 1.0
 *
 */
public enum PshLSRHazard {

    /**
     * High Surf
     */
    HIGH_SURF("High Surf"),
    /**
     * Flood
     */
    FLOOD("Flood"),
    /**
     * Downburst
     */
    DOWNBURST("Downburst"),
    /**
     * Flash Flood
     */
    FLASH_FLOOD("Flash Flood"),
    /**
     * Non-Thunderstorm Wind Damage
     */
    NON_TSTM_WND_DMG("Non-Tstm Wnd Dmg"),
    /**
     * Marine Thunderstorm Wind
     */
    MARINE_TSTM_WIND("Marine Tstm Wind"),
    /**
     * Lightning
     */
    LIGHTNING("Lightning"),
    /**
     * High Sustained Wind
     */
    HIGH_SUST_WIND("High Sust Wind"),
    /**
     * Tornado
     */
    TORNADO("Tornado"),
    /**
     * Storm Surge
     */
    STORM_SURGE("Storm Surge"),
    /**
     * Rip Currents
     */
    RIP_CURRENTS("Rip Currents"),
    /**
     * Non-Thunderstorm Wind Gust
     */
    NON_TSTM_WND_GST("Non-Tstm Wnd Gst"),
    /**
     * Waterspout
     */
    WATERSPOUT("Waterspout"),
    /**
     * Thunderstorm Wind Gust
     */
    TSTM_WND_GST("Tstm Wnd Gst"),
    /**
     * Thunderstorm Wind Damage
     */
    TSTM_WND_DMG("Tstm Wnd Dmg"),
    /**
     * Tropical Storm
     */
    TROPICAL_STORM("Tropical Storm"),
    /**
     * Hurricane
     */
    HURRICANE("Hurricane");

    /**
     * Label for the type of hazard
     */
    private String label;

    /**
     * Constructor
     * 
     * @param label
     */
    private PshLSRHazard(String label) {
        this.label = label;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Return the PshLSRHazard with the label that matches the given string.
     * Return null if none match.
     * 
     * @param hazardString
     * @return
     */
    public static PshLSRHazard getHazard(String hazardString) {
        for (PshLSRHazard hazard : PshLSRHazard.values()) {
            if (hazard.getLabel().equals(hazardString)) {
                return hazard;
            }
        }
        return null;
    }

}

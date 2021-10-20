/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.dataplugin.atcf;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This enum holds the ATCF fix formats.
 *
 * <pre>
 *      Fix Format (3 characters or 2 digits)
 *
 *      10 - subjective dvorak
 *      20 - objective dvorak
 *      30 - microwave
 *      31 - scatterometer
 *      40 - radar
 *      50 - aircraft
 *      60 - dropsonde
 *      70 - analysis
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 #52658     jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
public enum FixFormat {

    SUBJECTIVE_DVORAK("10", "Sat.-Subj. Dvorak"),

    OBJECTIVE_DVORAK("20", "Sat.-Obj. Dvorak"),

    MICROWAVE("30", "Microwave-SSMI, TRMM"),

    SCATTEROMETER("31", "Scatterometer"),

    RADAR("40", "Radar"),

    AIRCRAFT("50", "AirCraft"),

    DROPSONDE("60", "Dropsonde"),

    ANALYSIS("70", "Analysis/Synoptic");

    @DynamicSerializeElement
    private String value;

    @DynamicSerializeElement
    private String description;

    /**
     * @param iValue
     */
    private FixFormat(final String iValue, String desc) {
        this.value = iValue;
        this.description = desc;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get a FixFormat by value.
     *
     * @ value
     *
     * @return FixFormat
     */
    public static FixFormat getFixFormat(String value) {
        for (FixFormat fmt : FixFormat.values()) {
            if (value.equals(fmt.getValue())) {
                return fmt;
            }
        }
        return null;
    }

}

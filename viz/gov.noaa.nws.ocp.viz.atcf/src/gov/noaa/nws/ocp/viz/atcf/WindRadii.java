/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.viz.atcf;

/**
 * Wind radii definitions in ATCF.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 02, 2020 71742      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public enum WindRadii {
    RADII_0_KNOT(0, "0 knot"),
    RADII_34_KNOT(34, "34 kt"),
    RADII_50_KNOT(50, "50 kt"),
    RADII_64_KNOT(64, "64 kt");

    private final int value;

    private final String name;

    /**
     * @param iValue
     */
    private WindRadii(final int iValue, final String name) {
        this.value = iValue;
        this.name = name;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get a WindRadii by its value
     * 
     * @param value
     *            WindRadii's value
     * @return WindRadii
     */
    public static WindRadii getWindRadii(int value) {
        WindRadii radii = null;
        for (WindRadii rad : WindRadii.values()) {
            if (rad.getValue() == value) {
                radii = rad;
                break;
            }
        }

        return radii;
    }

}
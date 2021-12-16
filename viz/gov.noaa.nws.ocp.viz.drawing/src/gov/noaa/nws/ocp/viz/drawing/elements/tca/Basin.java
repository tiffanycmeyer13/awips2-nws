/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

/**
 * Class contains static methods that convert different representations of
 * Tropical Cyclone basins.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
public class Basin {

    private static final String ATLANTIC = "Atlantic";

    private static final String EAST_PACIFIC = "E. Pacific";

    private static final String CENTRAL_PACIFIC = "C. Pacific";

    private static final String WEST_PACIFIC = "W. Pacific";

    private Basin() {

    }

    /**
     * Returns a two character abbreviation for the given Basin.
     * 
     * @param basin
     *            tropical cyclone basin name
     * @return basin abbreviation
     */
    public static String getBasinAbbrev(String basin) {

        String abbrev = null;

        if (basin.equals(ATLANTIC)) {
            abbrev = "al";
        } else if (basin.equals(EAST_PACIFIC)) {
            abbrev = "ep";
        } else if (basin.equals(CENTRAL_PACIFIC)) {
            abbrev = "cp";
        } else if (basin.equals(WEST_PACIFIC)) {
            abbrev = "wp";
        } else {
            abbrev = "xx";
        }

        return abbrev;
    }

    /**
     * Returns a integer for a given tropical cyclone basin. This number is used
     * in the Tropical Cylcone VTEC (TCV) message
     * 
     * @param basin
     *            tropical cyclone basin name
     * @return basin number
     */
    public static int getBasinNumber(String basin) {

        int num = 0;

        if (basin.equals(ATLANTIC) || "al".equalsIgnoreCase(basin)) {
            num = 1;
        } else if (basin.equals(EAST_PACIFIC) || "ep".equalsIgnoreCase(basin)) {
            num = 2;
        } else if (basin.equals(CENTRAL_PACIFIC)
                || basin.equalsIgnoreCase("cp")) {
            num = 3;
        } else if (basin.equals(WEST_PACIFIC) || "wp".equalsIgnoreCase(basin)) {
            num = 4;
        }

        return num;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

/**
 * This Filter is used when searching for tropical cyclone breakpoints. Users
 * can set the filter to accept "official" breakpoints only or breakpoints only
 * part of a specified coast line.
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
public class BreakpointFilter {

    private boolean official;

    private String coastName;

    private BreakpointManager bmgr;

    /**
     * Default constructor - no breakpoints are filtered
     */
    public BreakpointFilter() {
        bmgr = BreakpointManager.getInstance();
        official = false;
        coastName = null;
    }

    /**
     * Set the filter to accept only "official" breakpoints.
     */
    public void setOfficialOnly() {
        official = true;
    }

    /**
     * Set the filter to accept breakpoints belonging to the given coast.
     * 
     * @param name
     *            Coast name
     */
    public void filterCoastName(String name) {
        coastName = name;
    }

    /**
     * Determine if the given breakpoint should be filtered out.
     * 
     * @param bkpt
     *            Breakpoint to test
     * @return true, if breakpoint is not filtered out.
     */
    public boolean isAccepted(Breakpoint bkpt) {

        if (official && !bkpt.isOfficial()) {
            return false;
        }

        return !(coastName != null
                && !bmgr.findCoastName(bkpt).equals(coastName));
    }

}

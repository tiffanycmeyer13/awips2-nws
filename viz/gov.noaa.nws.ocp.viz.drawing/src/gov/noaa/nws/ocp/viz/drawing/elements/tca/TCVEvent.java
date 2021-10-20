/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import gov.noaa.nws.ocp.common.drawing.breakpoint.Breakpoint;

/**
 * This class represents an Event group in a Tropical Cyclone VTEC (TCV)
 * message. The event group consists of a UGC line, one or more VTEC lines, and
 * one or more tropical cyclone breakpoints.
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
public class TCVEvent implements Comparable<TCVEvent> {

    public enum TCVEventType {
        LIST, SEGMENT
    }

    private TCVEventType evenType;

    private UGCGroup ugc;

    private List<TVtecObject> vtecLines;

    private List<Breakpoint> breakpoints;

    /**
     * @param evenType
     */
    public TCVEvent(TCVEventType evenType) {
        super();
        this.evenType = evenType;
        ugc = new UGCGroup();
        vtecLines = new ArrayList<>();
        breakpoints = new ArrayList<>();
    }

    /**
     * @return the evenType
     */
    public TCVEventType getEvenType() {
        return evenType;
    }

    /**
     * @return the vtecLines
     */
    public List<TVtecObject> getVtecLines() {
        return vtecLines;
    }

    /**
     * @return the breakpoints
     */
    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    public void addVtecLine(TVtecObject vtec) {
        vtecLines.add(vtec);
        Collections.sort(vtecLines);
    }

    /**
     * @return the ugc
     */
    public UGCGroup getUgc() {
        return ugc;
    }

    /**
     * @param breakpoints
     *            the breakpoints to set
     */
    public void setBreakpoints(List<Breakpoint> breakpoints) {
        this.breakpoints = breakpoints;
    }

    public void addZones(List<String> zones) {
        ugc.addZones(zones);
    }

    public void setPurgeTime(Calendar time) {
        ugc.setPurgeTime(time);
    }

    /**
     * The ordering of two event groups is defined the same as the ordering of
     * the highest priority VTEC line of each.
     */
    @Override
    public int compareTo(TCVEvent o) {

        TVtecObject thisone = this.getVtecLines().get(0);
        TVtecObject thatone = o.getVtecLines().get(0);

        return thisone.compareTo(thatone);
    }

    /*
     * two Events are considered equal if their leading VTEC lines have the same
     * priority
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this.getClass() == obj.getClass()) {
            TVtecObject thisone = this.getVtecLines().get(0);
            TCVEvent o = (TCVEvent) obj;
            TVtecObject thatone = o.getVtecLines().get(0);
            return thisone.equals(thatone);
        } else {
            return false;
        }
    }

    public void addBreakpoint(Breakpoint bkpt) {
        breakpoints.add(bkpt);
    }

    /**
     * @return int
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result;
        result = prime * result
                + ((evenType == null) ? 0 : evenType.hashCode());
        result = prime * result + ((ugc == null) ? 0 : ugc.hashCode());
        result = prime * result
                + ((vtecLines == null) ? 0 : vtecLines.hashCode());
        result = prime * result
                + ((breakpoints == null) ? 0 : breakpoints.hashCode());
        return result;
    }

}

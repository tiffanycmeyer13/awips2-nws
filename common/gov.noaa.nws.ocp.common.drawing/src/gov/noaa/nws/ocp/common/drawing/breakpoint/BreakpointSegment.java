/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This class is used to identify a Breakpoint Geography segment where only one
 * breakpoint is specified, and the geography is assumed to be valid up until
 * the next breakpoint in a master coastal breakpoint list with in inherent
 * geographical order proceeding along a specific coast line.
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
@XmlAccessorType(XmlAccessType.NONE)
public class BreakpointSegment extends BPGeography {

    /*
     * The breakpoint defining the beginning of the segment. The end of the
     * segment is assumed to be the next breakpoint in the list.
     */
    @XmlElement
    private Breakpoint breakpoint;

    /**
     * Constructor
     */
    public BreakpointSegment() {
        super();
    }

    /**
     * @return the breakpoint
     */
    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    /**
     * @param breakpoint
     *            the breakpoint to set
     */
    public void setBreakpoint(Breakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    @Override
    public List<Breakpoint> getBreakpoints() {
        List<Breakpoint> list = new ArrayList<>();
        list.add(breakpoint);
        return list;
    }

}

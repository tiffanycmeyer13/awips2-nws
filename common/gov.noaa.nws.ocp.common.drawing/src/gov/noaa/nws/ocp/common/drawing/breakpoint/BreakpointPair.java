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
 * This class is used to identify a Breakpoint Geography that is associated with
 * a pair of breakpoints that define the two endpoints of the segment along a
 * coast line..
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
public class BreakpointPair extends BPGeography {

    /*
     * The two breakpoints associated with this geography
     */
    @XmlElement
    private List<Breakpoint> breakpoints;

    /**
     * Constructor
     */
    public BreakpointPair() {
        super();
        breakpoints = new ArrayList<>();
    }

    /**
     * @param breakpoints
     */
    public BreakpointPair(List<Breakpoint> breakpoints) {
        super();
        this.breakpoints = breakpoints;
    }

    /**
     * Get break points
     * 
     */
    @Override
    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    public void addBreakpoint(Breakpoint bkpt) {
        breakpoints.add(bkpt);
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * This class is used to hold a list of Breakpoint Geography segments, where
 * each breakpoint contains the geography which is assumed to be valid up until
 * the next breakpoint in the list. The list of breakpoints has an inherent
 * geographical order proceeding along a specific coast line thus defining the
 * coast line. Coasts can be identified as an island, when applicable,
 * indicating that the last breakpoint is assumed to be just before the first
 * breakpoint geographically, and therefore can be treated as a circular list of
 * breakpoints.
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
public class CoastBreakpoint {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private boolean island;

    @XmlElements({ @XmlElement(name = "segment") })
    private List<BreakpointSegment> segments;

    /**
     * 
     */
    public CoastBreakpoint() {
        super();
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
     * @return the breakpoint segments
     */
    public List<BreakpointSegment> getSegments() {
        return segments;
    }

    /**
     * @param breakpoints
     *            the breakpoints to set
     */
    public void setSegments(List<BreakpointSegment> segments) {
        this.segments = segments;
    }

    /**
     * @return the island
     */
    public boolean isIsland() {
        return island;
    }

    /**
     * @param island
     *            the island to set
     */
    public void setIsland(boolean island) {
        this.island = island;
    }

}

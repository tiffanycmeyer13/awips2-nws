package gov.noaa.nws.ocp.common.drawing.breakpoint;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains a list of many island breakpoints
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
@XmlRootElement(name = "islandBreakpoints")
@XmlAccessorType(XmlAccessType.NONE)
public class IslandBreakpointList {

    @XmlElement(name = "island")
    private List<IslandBreakpoint> islands;

    /**
     * @return the islands
     */
    public List<IslandBreakpoint> getIslands() {
        return islands;
    }

    /**
     * @param islands
     *            the islands to set
     */
    public void setIslands(List<IslandBreakpoint> islands) {
        this.islands = islands;
    }

}

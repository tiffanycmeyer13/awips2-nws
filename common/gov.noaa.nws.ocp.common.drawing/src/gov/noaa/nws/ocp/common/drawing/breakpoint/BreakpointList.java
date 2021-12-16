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
 * a list of (typically more than two) breakpoints.
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
public class BreakpointList extends BPGeography {

    /*
     * breakpoints associated with this geography
     */
    @XmlElement
    private List<Breakpoint> breakpoints;

    /**
     * 
     */
    public BreakpointList() {
        super();
        breakpoints = new ArrayList<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.noaa.nws.ncep.ui.pgen.tca.BPGeography#getBreakpoints()
     */
    @Override
    public List<Breakpoint> getBreakpoints() {
        return breakpoints;
    }

    public void addBreakpoint(Breakpoint bkpt) {
        breakpoints.add(bkpt);
    }

}

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
 * This class identifies an entire waterway as a breakpoint. The geography
 * (drawing path and land zones) are assumed to be valid for the entire waterway
 * defined by the breakpoint
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
public class WaterBreakpoint extends BPGeography {

    @XmlElement
    private Breakpoint breakpoint;

    /**
     * 
     */
    public WaterBreakpoint() {
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

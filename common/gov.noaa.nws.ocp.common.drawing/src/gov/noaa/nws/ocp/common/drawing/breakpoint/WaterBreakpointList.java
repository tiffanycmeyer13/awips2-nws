/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains a list of many waterway breakpoints
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
@XmlRootElement(name = "waterBreakpoints")
@XmlAccessorType(XmlAccessType.NONE)
public class WaterBreakpointList {

    @XmlElement(name = "waterway")
    private List<WaterBreakpoint> breakpoints;

    /**
     * @return the waterway breakpoints
     */
    public List<WaterBreakpoint> getWaterways() {
        return breakpoints;
    }

    /**
     * @param breakpoints
     *            the list of waterway breakpoints
     */
    public void setWaterways(List<WaterBreakpoint> breakpoints) {
        this.breakpoints = breakpoints;
    }

}

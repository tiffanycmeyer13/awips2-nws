/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.breakpoint;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains a list of CoastBreakpoint objects defining multiple coast lines.
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
@XmlRootElement(name = "coastBreakpoints")
@XmlAccessorType(XmlAccessType.NONE)
public class CoastBreakpointList {

    @XmlElements({ @XmlElement(name = "coast") })
    private List<CoastBreakpoint> coasts;

    /**
     * @return the coasts
     */
    public List<CoastBreakpoint> getCoasts() {
        return coasts;
    }

    /**
     * @param coasts
     *            the coasts to set
     */
    public void setCoasts(List<CoastBreakpoint> coasts) {
        this.coasts = coasts;
    }

    public List<BreakpointSegment> getCoast(String name) {

        for (CoastBreakpoint coast : coasts) {
            if (coast.getName().equals(name)) {
                return coast.getSegments();
            }
        }

        return new ArrayList<>();
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.drawing.elements.tca;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

import gov.noaa.nws.ocp.common.drawing.breakpoint.BPGeography;
import gov.noaa.nws.ocp.common.drawing.breakpoint.Breakpoint;
import gov.noaa.nws.ocp.common.drawing.breakpoint.BreakpointManager;
import gov.noaa.nws.ocp.common.drawing.breakpoint.BreakpointPair;
import gov.noaa.nws.ocp.common.drawing.breakpoint.IslandBreakpoint;
import gov.noaa.nws.ocp.common.drawing.breakpoint.WaterBreakpoint;

/**
 * Defines the intensity and geographical area for a specific Tropical cyclone
 * advisory.
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
@XmlType(name = "", propOrder = { "severity", "advisoryType", "geographyType",
        "segment" })
@XmlAccessorType(XmlAccessType.NONE)
public class TropicalCycloneAdvisory {

    // Typically, this indicates Tropical Storm or Hurricane
    @XmlElement
    private String severity;

    // advisory type can be a watch or warning
    @XmlElement
    private String advisoryType;

    @XmlElement
    private String geographyType;

    @XmlElements({ @XmlElement(name = "coast", type = BreakpointPair.class),
            @XmlElement(name = "island", type = IslandBreakpoint.class),
            @XmlElement(name = "waterway", type = WaterBreakpoint.class) })

    private BPGeography segment;

    /**
     * Constructor
     */
    public TropicalCycloneAdvisory() {
    }

    /**
     * @param severity
     * @param advisoryType
     * @param geographyType
     * @param breakpoints
     *            public TropicalCycloneAdvisory(String severity, String
     *            advisoryType, String geographyType, ArrayList<BPGeography>
     *            breakpoints) { super(); this.severity = severity;
     *            this.advisoryType = advisoryType; this.geographyType =
     *            geographyType; this.breakpoints = breakpoints; }
     */

    /**
     * @return the severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severity
     * @param advisoryType
     * @param geographyType
     * @param segment
     */
    public TropicalCycloneAdvisory(String severity, String advisoryType,
            String geographyType, BPGeography segment) {
        super();
        this.severity = severity;
        this.advisoryType = advisoryType;
        this.geographyType = geographyType;
        this.segment = segment;
    }

    /**
     * @param severity
     *            the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * @return the advisoryType
     */
    public String getAdvisoryType() {
        return advisoryType;
    }

    /**
     * @param advisoryType
     *            the advisoryType to set
     */
    public void setAdvisoryType(String advisoryType) {
        this.advisoryType = advisoryType;
    }

    /**
     * @return the geographyType
     */
    public String getGeographyType() {
        return geographyType;
    }

    /**
     * @param geographyType
     *            the geographyType to set
     */
    public void setGeographyType(String geographyType) {
        this.geographyType = geographyType;
    }

    /**
     * @return the segment
     */
    public BPGeography getSegment() {
        return this.segment;
    }

    /**
     * @param segment
     *            the segment to set
     */
    public void setSegment(BPGeography segment) {
        this.segment = segment;
    }

    /**
     * Create a copy of this object
     */
    public TropicalCycloneAdvisory copy() {
        String sevType = this.getSeverity();
        String advType = this.getAdvisoryType();
        String geogType = this.getGeographyType();
        return new TropicalCycloneAdvisory(sevType, advType, geogType,
                this.getSegment());
    }

    /*
     * (non-Javadoc) Determines whether this advisory has same attributes and
     * breakpoints as another.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (this.getClass() == obj.getClass()) {
            TropicalCycloneAdvisory tca = (TropicalCycloneAdvisory) obj;

            if (!this.geographyType.equals(tca.getGeographyType())
                    || !this.advisoryType.equals(tca.getAdvisoryType())
                    || !this.severity.equals(tca.getSeverity())) {
                return false;
            }

            List<Breakpoint> thislist = this.segment.getBreakpoints();
            List<Breakpoint> thatlist = tca.getSegment().getBreakpoints();
            if (thislist.size() != thatlist.size()) {
                return false;
            }

            for (int j = 0; j < thislist.size(); j++) {
                if (!thislist.get(j).equals(thatlist.get(j)))
                    return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Determines whether the watch/warning segment of this advisory defined by
     * the pair of breakpoints overlaps the watch/warning segment of the given
     * advisory
     * 
     * @param tca
     * @return
     */
    public boolean overlaps(TropicalCycloneAdvisory tca) {

        BreakpointManager bm = BreakpointManager.getInstance();

        if (!this.advisoryType.equals(tca.getAdvisoryType())) {
            return false;
        }

        if (!this.severity.equals(tca.getSeverity())) {
            return false;
        }

        if (!(this.segment instanceof BreakpointPair)
                || !(tca.getSegment() instanceof BreakpointPair)) {
            return false;
        }

        return bm.pairsOverlap((BreakpointPair) this.segment,
                (BreakpointPair) tca.getSegment());
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
                + ((severity == null) ? 0 : severity.hashCode());
        result = prime * result
                + ((advisoryType == null) ? 0 : advisoryType.hashCode());
        result = prime * result
                + ((geographyType == null) ? 0 : geographyType.hashCode());
        result = prime * result + ((segment == null) ? 0 : segment.hashCode());
        return result;
    }

}

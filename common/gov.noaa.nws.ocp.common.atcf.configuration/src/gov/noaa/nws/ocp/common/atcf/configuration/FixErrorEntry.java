/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represent a fix error entry in fixerror.pref, which is used for
 * displaying fixes and computing error statistics.
 *
 * <pre>
 *
 * The file format is as follows:
 * fix site - The specific site from which the fix comes (maximum of 50)
 * fix error(3) - avg position error (nm), intensity error (kt), wind radii error (nm)
 *            posit          intensity      wind radii
 *       good fair poor   good fair poor   good fair poor 
 *      
 *        
 * DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 * START_OF_DATA:
 *
 * track_fitting_algorithm: least_squares  (deprecated for now, set in GUI)
 * int___fitting_algorithm: least_squares  (deprecated for now, set in GUI)
 * rad___fitting_algorithm: least_squares  (deprecated for now, set in GUI)
 * type_site posit_weights  intens_weights  radii_weights  research_fix
 * DVTS PGTW  15  30  45      3   7  10     999 999 999     0
 * DVTS KGWC  20  35  50     20  30  40     999 999 999     0
 * DVTS KNES  20  35  50     20  30  40     999 999 999     0
 * DVTS PHFO  15  30  45    999 999 999     999 999 999     0
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 04, 2019 64494      dmanzella   Created
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class FixErrorEntry {

    /**
     * Site
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String site;

    /**
     * Type
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String type;

    /**
     * Position weights
     */
    @DynamicSerializeElement
    @XmlAttribute
    private List<Integer> positWeights;

    /**
     * Intensity weights
     */
    @DynamicSerializeElement
    @XmlAttribute
    private List<Integer> intensWeights;

    /**
     * Radii weights
     */
    @DynamicSerializeElement
    @XmlAttribute
    private List<Integer> radiiWeights;

    /**
     * research fix flag
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean research;

    /**
     * Constructor.
     */
    public FixErrorEntry() {
        positWeights = new ArrayList<>();
        intensWeights = new ArrayList<>();
        radiiWeights = new ArrayList<>();
    }

    /**
     * Constructor.
     * 
     * @param name
     *            Type name
     * @param retired
     *            If the Type is retired or not
     */
    public FixErrorEntry(String site, String type, List<Integer> posit,
            List<Integer> intens, List<Integer> radii,
            boolean research) {
        this.site = site;
        this.type = type;
        this.positWeights = posit;
        this.intensWeights = intens;
        this.radiiWeights = radii;
        this.research = research;
    }

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }

    /**
     * @param site
     *            the site to set
     */
    public void setSite(String site) {
        this.site = site;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the research
     */
    public boolean isResearch() {
        return research;
    }

    /**
     * @param retired
     *            the retired to set
     */
    public void setResearch(boolean retired) {
        this.research = retired;
    }

    /**
     * @return the positions
     */
    public List<Integer> getPositions() {
        return this.positWeights;
    }

    /**
     * @return the intensities
     */
    public List<Integer> getIntensities() {
        return this.intensWeights;
    }

    /**
     * @return the radii
     */
    public List<Integer> getRadii() {
        return this.radiiWeights;
    }

    /**
     * @param pos
     *            the positions to set
     */
    public void setPositions(List<Integer> pos) {
        this.positWeights = pos;
    }

    /**
     * @param intens
     *            the intensities to set
     */
    public void setIntensities(List<Integer> intens) {
        this.intensWeights = intens;
    }

    /**
     * @param radii
     *            the radii to set
     */
    public void setRadii(List<Integer> radii) {
        this.radiiWeights = radii;
    }

}
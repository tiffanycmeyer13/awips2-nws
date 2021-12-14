/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class holds the fix error defined in fixerror.pref.xml.
 *
 * The format in original fixerror.pref.dat is as following:
 *
 * <pre>
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
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 04, 2019 64494      dmanzella   Created
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "FixErrors")
@XmlAccessorType(XmlAccessType.NONE)
public class FixError {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Error", type = FixErrorEntry.class) })
    private List<FixErrorEntry> fixError;

    /**
     * Constructor
     */
    public FixError() {
        fixError = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param siteList
     *            List of fix site entry
     */
    public FixError(List<FixErrorEntry> errorList) {
        fixError = new ArrayList<>(errorList);
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the fixSites
     */
    public List<FixErrorEntry> getFixError() {
        return fixError;
    }

    /**
     * Get all site entries.
     *
     * @return all sites' names
     */
    public String[] getSites() {
        List<String> sites = new ArrayList<>();
        for (FixErrorEntry st : fixError) {
            sites.add(st.getSite());
        }

        return sites.toArray(new String[sites.size()]);
    }

    /**
     * @param fixSites
     *            the fixSites to set
     */
    public void setFixError(List<FixErrorEntry> fixErrors) {
        this.fixError = fixErrors;
    }

    /**
     * Get the all non-retired site entries.
     *
     * @return the FixSites
     */
    public FixError getAvailableFixSites() {
        FixError errors = new FixError();
        for (FixErrorEntry st : fixError) {
            if (!st.isResearch()) {
                errors.getFixError().add(st);
            }
        }

        return errors;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * fixerror.pref
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        sb.append(
                "track_fitting_algorithm: least_squares  (deprecated for now, set in GUI)");
        sb.append(newline);

        sb.append(
                "int___fitting_algorithm: least_squares  (deprecated for now, set in GUI)");
        sb.append(newline);

        sb.append(
                "rad___fitting_algorithm: least_squares  (deprecated for now, set in GUI)");
        sb.append(newline);

        sb.append(
                "type_site posit_weights  intens_weights  radii_weights  research_fix");
        sb.append(newline);

        for (FixErrorEntry fs : fixError) {
            // All the pos/intens/radii
            sb.append(String.format("%4s", fs.getType()));
            sb.append(String.format("%5s", fs.getSite()));

            sb.append(String.format("%4d", (fs.getPositions().get(0))));
            sb.append(String.format("%4d", (fs.getPositions().get(1))));
            sb.append(String.format("%4d", (fs.getPositions().get(2))));

            sb.append(String.format("%7d", (fs.getIntensities().get(0))));
            sb.append(String.format("%4d", (fs.getIntensities().get(1))));
            sb.append(String.format("%4d", (fs.getIntensities().get(2))));

            sb.append(String.format("%8d", (fs.getRadii().get(0))));
            sb.append(String.format("%4d", (fs.getRadii().get(1))));
            sb.append(String.format("%4d", (fs.getRadii().get(2))));

            sb.append(String.format("%6d", (fs.isResearch()) ? 1 : 0));
            sb.append(newline);
        }

        return sb.toString();
    }

}
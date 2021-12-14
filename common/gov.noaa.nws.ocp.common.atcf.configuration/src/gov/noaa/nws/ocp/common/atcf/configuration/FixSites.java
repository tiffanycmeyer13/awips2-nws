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
 * This class holds the fix sites defined in fixsites.xml that are used for
 * entering and displaying satellite fixes and for computing error statistics.
 *
 * The format in original fixsites.dat is as following:
 *
 * <pre>
 * The file format is as follows:
 *    fix site - The specific site from which the fix comes (4 or 5 digits).
 *    retired/developmental flag - 1 means don't use the fix site in normal operations.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  #SITE RETIRED
 *  TAFB 0
 *  SAB 0
 *  KNHC 0
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 #52692     jwu         Created
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "FixSites")
@XmlAccessorType(XmlAccessType.NONE)
public class FixSites {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries

    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Site", type = FixSiteEntry.class) })
    private List<FixSiteEntry> fixSites;

    /**
     * Constructor
     */
    public FixSites() {
        fixSites = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param siteList
     *            List of fix site entry
     */
    public FixSites(List<FixSiteEntry> siteList) {
        fixSites = new ArrayList<>(siteList);
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
    public List<FixSiteEntry> getFixSites() {
        return fixSites;
    }

    /**
     * Get all site entries.
     *
     * @return all sites' names
     */
    public String[] getSites() {
        List<String> sites = new ArrayList<>();
        for (FixSiteEntry st : fixSites) {
            sites.add(st.getName());
        }

        return sites.toArray(new String[sites.size()]);
    }

    /**
     * @param fixSites
     *            the fixSites to set
     */
    public void setFixSites(List<FixSiteEntry> fixSites) {
        this.fixSites = fixSites;
    }

    /**
     * Find the site entry with the given name.
     *
     * @return the fixSite
     */
    public FixSiteEntry getFixSite(String siteName) {
        FixSiteEntry site = null;
        for (FixSiteEntry st : fixSites) {
            if (st.getName().equals(siteName)) {
                site = st;
                break;
            }
        }

        return site;
    }

    /**
     * Get the all non-retired site entries.
     *
     * @return the FixSites
     */
    public FixSites getAvailableFixSites() {
        FixSites sites = new FixSites();
        for (FixSiteEntry st : fixSites) {
            if (!st.isRetired()) {
                sites.getFixSites().add(st);
            }
        }

        return sites;
    }

    /**
     * Get the all non-retired site entries.
     *
     * @return all non-retired sites' names
     */
    public String[] getAvailableSites() {
        List<String> sites = new ArrayList<>();
        for (FixSiteEntry st : fixSites) {
            if (!st.isRetired()) {
                sites.add(st.getName());
            }
        }

        return sites.toArray(new String[sites.size()]);
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * fixsites.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        sb.append("#SITE RETIRED");
        sb.append(newline);
        sb.append(newline);

        for (FixSiteEntry fs : fixSites) {
            sb.append(String.format("%5s", fs.getName()));
            sb.append(String.format("%2d", (fs.isRetired()) ? 1 : 0));
            sb.append(newline);
        }

        return sb.toString();
    }

}
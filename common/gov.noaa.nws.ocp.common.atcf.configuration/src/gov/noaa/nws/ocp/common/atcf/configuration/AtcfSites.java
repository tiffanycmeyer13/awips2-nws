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
 * This class represent a list of ATCF sites, which are used to compose advisory
 * products.
 *
 * <pre>
 * Entry format:
 *
 * <Site id="NHC"  wfoId="KNHC" node="MIA" bureau="NWS" 
 *       center="National Hurricane Center" city="Miami" state="FL"/>
 *
 * <Site id="CPHC" wfoId="PHFO" node="HFO" bureau="NWS" 
 *       center="Central Pacific Hurricane Center" city="Honolulu" state="HI"/>
 *
 * <Site id="WPC"  wfoId="KWNH" node="NFD" bureau="NWS" 
 *       center="Weather Prediction Center" city="College Park" state="MD"/>
 *
 *  where:
 *        id            - site dentifier
 *        wfoId         - WFO station code
 *        node          - Node for AWIPS products issued by this site
 *        bureau    - Department where the forecast center belongs to.
 *        center        - Name of forecast center
 *        city          - City of forecast center
 *        state         - State  of forecast center
 *        issueByOffice - Description of the issuing office - combo of "super center city state"
 *
 * Note: The entries in atcfsites.xml are compiled from nhc_writeadv.f.
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2021 86476      jwu         Created
 * Mar 25, 2021 90014      dfriedman   Remove node field.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "AtcfSites")
@XmlAccessorType(XmlAccessType.NONE)
public class AtcfSites {

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Site", type = AtcfSiteEntry.class) })
    private List<AtcfSiteEntry> atcfSites;

    /**
     * Constructor
     */
    public AtcfSites() {
        atcfSites = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param siteList
     *            List of ATCF site entry
     */
    public AtcfSites(List<AtcfSiteEntry> siteList) {
        atcfSites = new ArrayList<>(siteList);
    }

    /**
     * @return the atcfSites
     */
    public List<AtcfSiteEntry> getAtcfSites() {
        return atcfSites;
    }

    /**
     * @param atcfSites
     *            the atcfSites to set
     */
    public void setAtcfSites(List<AtcfSiteEntry> atcfSites) {
        this.atcfSites = atcfSites;
    }

    /**
     * Find the storm state entry with a given id.
     *
     * @return the StormStateEntry
     */
    public AtcfSiteEntry getAtcfSitebyID(String id) {
        AtcfSiteEntry site = null;
        for (AtcfSiteEntry st : atcfSites) {
            if (st.getId().equals(id)) {
                site = st;
                break;
            }
        }

        return site;
    }

    /**
     * Constructs a string representation of all ATCF sites.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        for (AtcfSiteEntry fs : atcfSites) {
            sb.append(String.format("Site: id=\"%s", fs.getId()) + "\"");
            sb.append(String.format("\twfoId=\"%s", fs.getWfoId()) + "\"");
            sb.append(
                    String.format("\tissueByOffice=\"%s", fs.getIssueByOffice())
                            + "\"");
            sb.append(newline);
        }

        return sb.toString();
    }

}
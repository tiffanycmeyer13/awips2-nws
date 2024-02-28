/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represent a collection of CPA locations in cpa.loc.xml, which is
 * used for displaying CPA locations and priorities.
 *
 * The format in original cpa.loc is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 *
 * #
 * # Closest Point of Approach (CPA) definitions
 * #
 * # Destination names should not contain white spaces, max. 20 chars.
 * #
 * # Lat   Lon   Destination         Priority
 * #
 * 16.8N 099.9W Acapulco               0
 * 13.3N 144.8E Hagatna                0
 * ...
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 26, 2019 #59910     dmanzella   Created
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "CpaLocations")
@XmlAccessorType(XmlAccessType.NONE)
public class CpaLocations {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "Location", type = CpaLocationEntry.class) })

    private Map<String, CpaLocationEntry> cpaLocations;

    /**
     * Constructor
     */
    public CpaLocations() {
        cpaLocations = new TreeMap<>();
    }

    /**
     * Constructor
     *
     * @param cpaLocations
     *            List of CPA Locations
     */
    public CpaLocations(List<CpaLocationEntry> cpaLocations) {
        for (CpaLocationEntry entry : cpaLocations) {
            this.cpaLocations.put(entry.getDestination(), entry);
        }
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
     * @return the cpaLocations
     */
    public Map<String, CpaLocationEntry> getCpaLocations() {
        return cpaLocations;
    }

    /**
     * Find the ordered index of the given entry
     *
     * @param entry
     * @return index
     */
    public int indexOf(CpaLocationEntry entry) {

        if (cpaLocations.containsValue(entry)) {
            int ii = 0;
            for (CpaLocationEntry thisEntry : cpaLocations.values()) {
                if (thisEntry.equals(entry)) {
                    return ii;
                }
            }
        }
        return -1;
    }

    /**
     * Add to the list
     *
     * @param entry
     */
    public void add(CpaLocationEntry entry) {
        cpaLocations.put(entry.getDestination(), entry);
    }

    /**
     * If contains the entry, remove it
     *
     * @param entry
     * @return if entry successfully removed
     */
    public boolean remove(CpaLocationEntry entry) {
        if (cpaLocations.keySet().contains(entry.getDestination())) {
            cpaLocations.remove(entry.getDestination());
            return true;
        }
        return false;
    }

    /**
     * @param cpaLocations
     *            the cpaLocations to set
     */
    public void setCpaLocations(List<CpaLocationEntry> cpaLocations) {
        cpaLocations.clear();
        for (CpaLocationEntry entry : cpaLocations) {
            this.cpaLocations.put(entry.getDestination(), entry);
        }
    }

    /**
     * Find the CPA Locations entry with the given destination.
     *
     * @return the entry
     */
    public CpaLocationEntry getEntryFromName(String cpaDestination) {
        return cpaLocations.get(cpaDestination);
    }

    /**
     * Ffind the CPA Location entry with the given index
     *
     * @param index
     * @return the entry
     */
    public CpaLocationEntry getEntryFromIndex(int index) {
        int ii = 0;
        Iterator<String> iter = cpaLocations.keySet().iterator();
        while (iter.hasNext()) {
            if (ii == index) {
                return cpaLocations.get(iter.next());
            }
            ii++;
        }
        return null;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * cpa.loc.
     */
    @Override
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = cpaLocations.keySet().iterator();
        while (iter.hasNext()) {
            sb.append(cpaLocations.get(iter.next()).toString());
            sb.append(newline);
        }
        return sb.toString();
    }

}
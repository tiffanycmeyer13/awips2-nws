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
 * This class represent a full list of forecaster' initial & first name in
 * initials_full.dat, which holds all forecasters' initial and first name from
 * all ATCF sites. (NHC/CPHC/WPC).
 *
 * <pre>
 *
 * The file format is as follows:
 *
 *   MJB BRENNAN
 *   ENR RAPPAPORT
 *   RJP PASCH
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 22, 2021 86476      jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "FullInitials")
@XmlAccessorType(XmlAccessType.NONE)
public class FullInitials {

    // Entries
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "Initial", type = FullInitialEntry.class) })
    private List<FullInitialEntry> fullInitials;

    /**
     * Constructor
     */
    public FullInitials() {
        fullInitials = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param initialList
     *            List of full initial entry
     */
    public FullInitials(List<FullInitialEntry> initialList) {
        fullInitials = new ArrayList<>(initialList);
    }

    /**
     * @return the fullInitials
     */
    public List<FullInitialEntry> getFullInitials() {
        return fullInitials;
    }

    /**
     * @param fullInitials
     *            the fullInitials to set
     */
    public void setFullInitials(List<FullInitialEntry> fullInitials) {
        this.fullInitials = fullInitials;
    }

    /**
     * Find the full initial entry with a given initial.
     *
     * @return the FullInitialEntry
     */
    public FullInitialEntry getFullInitial(String initial) {
        FullInitialEntry init = null;
        for (FullInitialEntry st : fullInitials) {
            if (st.getInitial().equals(initial)) {
                init = st;
                break;
            }
        }

        return init;
    }

    /**
     * Find the first name with a given initial.
     *
     * @return the first name
     */
    public String getFirstName(String initial) {
        String name = initial;
        FullInitialEntry entry = getFullInitial(initial);
        if (entry != null) {
            name = entry.getName();
        }

        return name;
    }

    /**
     * Constructs a string representation of all full initials.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        for (FullInitialEntry fs : fullInitials) {
            sb.append(String.format("\tFull Initial: initial=\"%s",
                    fs.getInitial()) + "\"");
            sb.append(String.format("\tname=\"%s", fs.getName()) + "\"");
            sb.append(newline);
        }

        return sb.toString();
    }

}
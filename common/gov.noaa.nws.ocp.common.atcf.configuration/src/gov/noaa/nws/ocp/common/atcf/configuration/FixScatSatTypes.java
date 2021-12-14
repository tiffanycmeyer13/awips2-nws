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
 * This class holds satellite types used in scatsattypes.xml for entering
 * scatterometer fixes.
 *
 * The format in original scatsattypes.dat is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 * This file contains satellite types used for entering scatterometer fixes.
 * Note, these satellite types must also be listed in $ATCFINC/fixtypes.dat.
 *
 * DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 * START_OF_DATA:
 *
 * ASCT
 * RSCT
 * OSCT
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 27, 2019 63379      dmanzella   Created
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "FixScatSatTypes")
@XmlAccessorType(XmlAccessType.NONE)
public class FixScatSatTypes {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Type", type = String.class) })
    private List<String> scatSats;

    /**
     * Constructor
     */
    public FixScatSatTypes() {
        scatSats = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param siteList
     *            List of scat entry
     */
    public FixScatSatTypes(List<String> siteList) {
        scatSats = new ArrayList<>(siteList);
    }

    /**
     * @return the scatSats
     */
    public List<String> getScatSats() {
        return scatSats;
    }

    /**
     * @param scatSats
     *            the scatSats to set
     */
    public void setScatSats(List<String> scatSats) {
        this.scatSats = scatSats;
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
     * Find the site entry with the given name.
     *
     * @return the scatSats
     */
    public String getScatName(String siteName) {
        String site = null;
        for (String st : scatSats) {
            if (st.equals(siteName)) {
                site = st;
                break;
            }
        }

        return site;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * scatsattypes.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        for (String fs : scatSats) {
            sb.append(String.format("%5s", fs));
            sb.append(newline);
        }

        return sb.toString();
    }

}
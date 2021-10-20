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
 * This class holds satellite types in microsattypes.xml used for entering
 * microwave fixes.
 *
 * The format in original microsattypes.dat is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 * This file contains satellite types used for entering microwave fixes.
 * 
 * DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 * START_OF_DATA: 
 * 
 * SSMS
 * SSMI
 * GPM
 * AMSU
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
@XmlRootElement(name = "FixMicrowaveSatelliteTypes")
@XmlAccessorType(XmlAccessType.NONE)
public class FixMicrowaveSatelliteTypes {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Types", type = String.class) })
    private List<String> microSats;

    /**
     * Constructor
     */
    public FixMicrowaveSatelliteTypes() {
        microSats = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param siteList
     *            List of Microwave sats
     */
    public FixMicrowaveSatelliteTypes(List<String> siteList) {
        microSats = new ArrayList<>(siteList);
    }

    /**
     * @return the microSats
     */
    public List<String> getMicroSats() {
        return microSats;
    }

    /**
     * @param microSats
     *            the microSats to set
     */
    public void setMicroSats(List<String> microSats) {
        this.microSats = microSats;
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
     * @return the microSat name
     */
    public String getMicroName(String siteName) {
        String site = null;
        for (String st : microSats) {
            if (st.equals(siteName)) {
                site = st;
                break;
            }
        }

        return site;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * microsatypes.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        for (String fs : microSats) {
            sb.append(String.format("%5s", fs));
            sb.append(newline);
        }

        return sb.toString();
    }

}
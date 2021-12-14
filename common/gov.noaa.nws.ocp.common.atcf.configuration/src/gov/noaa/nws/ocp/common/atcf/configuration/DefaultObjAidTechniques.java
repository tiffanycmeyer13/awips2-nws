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
 * This class holds the default objective aid techniques defined in
 * default_aids.xml, which are used for displaying satellite the objective aid.
 *
 * The format in original default_aids.dat is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 *     The first field is the technique name.
 *     The second field is the technique description.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  FSSE  FSU Superensemble
 *  HWRF  HWRF model
 *  ....
 *
 * <pre>
 *
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
@XmlRootElement(name = "DefaultObjAidTechniques")
@XmlAccessorType(XmlAccessType.NONE)
public class DefaultObjAidTechniques {

    @DynamicSerializeElement
    @XmlElement(name = "Header", type = String.class)
    private String header;

    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "Tech", type = DefaultObjAidTechEntry.class) })
    private List<DefaultObjAidTechEntry> defaultObjAidTechniques;

    /**
     * Constructor
     */
    public DefaultObjAidTechniques() {
        defaultObjAidTechniques = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param aidList
     *            List of default obj aid tech entry
     */
    public DefaultObjAidTechniques(List<DefaultObjAidTechEntry> aidList) {
        defaultObjAidTechniques = new ArrayList<>(aidList);
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
     * @return the DefaultObjAidTechniques
     */
    public List<DefaultObjAidTechEntry> getDefaultObjAidTechniques() {
        return defaultObjAidTechniques;
    }

    /**
     * @param DefaultObjAidTechniques
     *            the DefaultObjAidTechniques to set
     */
    public void setDefaultObjAidTechniques(
            List<DefaultObjAidTechEntry> defaultObjAidTechniques) {
        this.defaultObjAidTechniques = defaultObjAidTechniques;
    }

    /**
     * Find the default obj aid tech entry with the given name.
     *
     * @return the DefaultObjAidTechEntry
     */
    public DefaultObjAidTechEntry getDefaultObjAidTechEntry(String aidName) {
        DefaultObjAidTechEntry aid = null;
        for (DefaultObjAidTechEntry st : defaultObjAidTechniques) {
            if (st.getName().equals(aidName)) {
                aid = st;
                break;
            }
        }

        return aid;
    }

    /**
     * Get all default obj aid tech names.
     *
     * @return List<String>
     */
    public List<String> getDefaultObjAidNames() {
        List<String> names = new ArrayList<>();
        for (DefaultObjAidTechEntry st : defaultObjAidTechniques) {
            names.add(st.getName());
        }

        return names;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * default_aids.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        for (DefaultObjAidTechEntry fs : defaultObjAidTechniques) {
            sb.append(String.format("%-6s", fs.getName()));
            sb.append(String.format("%s", fs.getDescription()));
            sb.append(newline);
        }

        return sb.toString();
    }

}
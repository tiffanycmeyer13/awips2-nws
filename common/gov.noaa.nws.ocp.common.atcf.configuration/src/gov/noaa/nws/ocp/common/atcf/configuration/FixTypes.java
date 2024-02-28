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
 * This class holds the fix types defined in fixtypes.xml that are used for
 * displaying fixes and computing error statistics.
 *
 * The format in original fixtypes.dat is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 *     fix type - 4 digit fix type
 *     retired/developmental flag - 1 means don't use the fix type in normal operations.
 *
 *  DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.
 *  START_OF_DATA:
 *
 *  #Type RETIRED
 *  DVTS    0
 *  DVTO    0
 *  SSMI    0
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 52692      jwu         Created
 * Apr 22, 2020 72252      jwu         Add header & save into XML
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "FixTypes")
@XmlAccessorType(XmlAccessType.NONE)
public class FixTypes {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Type", type = FixTypeEntry.class) })
    private List<FixTypeEntry> fixTypes;

    /**
     * Constructor
     */
    public FixTypes() {
        fixTypes = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param typeList
     *            List of fix type entry
     */
    public FixTypes(List<FixTypeEntry> typeList) {
        fixTypes = new ArrayList<>(typeList);
    }

    /**
     * @return the FixTypes
     */
    public List<FixTypeEntry> getFixTypes() {
        return fixTypes;
    }

    /**
     * @param FixTypes
     *            the FixTypes to set
     */
    public void setFixTypes(List<FixTypeEntry> fixTypes) {
        this.fixTypes = fixTypes;
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
     * Find the type entry with the given name.
     *
     * @return the fixType
     */
    public FixTypeEntry getFixType(String typeName) {
        FixTypeEntry type = null;
        for (FixTypeEntry st : fixTypes) {
            if (st.getName().equals(typeName)) {
                type = st;
                break;
            }
        }

        return type;
    }

    /**
     * Get the all non-retired type entries.
     *
     * @return the FixTypes
     */
    public FixTypes getAvailableFixTypes() {
        FixTypes types = new FixTypes();
        for (FixTypeEntry st : fixTypes) {
            if (!st.isRetired()) {
                types.getFixTypes().add(st);
            }
        }

        return types;
    }

    /**
     * Get the all non-retired type entries.
     *
     * @return all non-retired types' names
     */
    public String[] getAvailableTypes() {
        List<String> types = new ArrayList<>();
        for (FixTypeEntry st : fixTypes) {
            if (!st.isRetired()) {
                types.add(st.getName());
            }
        }

        return types.toArray(new String[types.size()]);
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * fixtypes.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(AtcfConfigurationManager.DELETION_WARNING);

        sb.append("#TYPE RETIRED");
        sb.append(newline);
        for (FixTypeEntry fs : fixTypes) {
            sb.append(String.format("%-8s", fs.getName()));
            sb.append(String.format("%d", (fs.isRetired()) ? 1 : 0));
            sb.append(newline);
        }

        return sb.toString();
    }

}
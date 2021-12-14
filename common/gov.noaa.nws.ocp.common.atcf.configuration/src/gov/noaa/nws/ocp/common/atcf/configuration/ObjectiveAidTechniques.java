/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class holds the objective aid techniques defined in techlist.xml, which
 * are used for displaying the objective aid.
 *
 * The format in original techlist.dat is as following:
 *
 * <pre>
 *
 * The file format is as follows:
 *
 * NUM TECH ERRS RETIRED COLOR DEFAULTS INT-DEFS RADII-DEFS LONG-NAME
 *  00 CARQ   0      0     0      0        0         1                Combined ARQ Position
 *  00 WRNG   0      0     0      0        0         1                Warning
 *  ....
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 52692      jwu         Created
 * Mar 25, 2020 75391      jwu         Added a few convenient methods.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "ObjAidTechniques")
@XmlAccessorType(XmlAccessType.NONE)
public class ObjectiveAidTechniques {

    // Header
    @DynamicSerializeElement
    @XmlElement
    private String header;

    // Entries
    @DynamicSerializeElement
    @XmlElements({
            @XmlElement(name = "Tech", type = ObjectiveAidTechEntry.class) })
    private List<ObjectiveAidTechEntry> objectiveAidTechniques;

    /**
     * Constructor
     */
    public ObjectiveAidTechniques() {
        objectiveAidTechniques = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param techList
     *            List of obj aid tech entry
     */
    public ObjectiveAidTechniques(List<ObjectiveAidTechEntry> techList) {
        objectiveAidTechniques = new ArrayList<>(techList);
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
     * @return the objectiveAidTechniques
     */
    public List<ObjectiveAidTechEntry> getObjectiveAidTechniques() {
        return objectiveAidTechniques;
    }

    /**
     * @param objectiveAidTechniques
     *            the objectiveAidTechniques to set
     */
    public void setObjectiveAidTechniques(
            List<ObjectiveAidTechEntry> objectiveAidTechniques) {
        this.objectiveAidTechniques = objectiveAidTechniques;
    }

    /**
     * Find the objective aid tech entry with the given name.
     *
     * @return the ObjectiveAidTechEntry
     */
    public ObjectiveAidTechEntry getObjectiveAidTechnique(String techName) {
        ObjectiveAidTechEntry tech = null;
        for (ObjectiveAidTechEntry st : objectiveAidTechniques) {
            if (st.getName().equals(techName)) {
                tech = st;
                break;
            }
        }

        return tech;
    }

    /**
     * Get the all non-retired technique entries into a map.
     *
     * @return the non-retired ObjectiveAidTechniques
     */
    public Map<String, ObjectiveAidTechEntry> getAvailableTechniques() {
        Map<String, ObjectiveAidTechEntry> teches = new LinkedHashMap<>();
        for (ObjectiveAidTechEntry st : objectiveAidTechniques) {
            if (!st.isRetired()) {
                teches.put(st.getName(), st);
            }
        }

        return teches;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * tech_list.dat.
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append(
                "NUM TECH ERRS RETIRED COLOR DEFAULTS INT-DEFS RADII-DEFS LONG-NAME");
        sb.append(newline);
        for (ObjectiveAidTechEntry fs : objectiveAidTechniques) {
            sb.append(" ");
            sb.append(String.format("%02d", fs.getNum()));
            sb.append(" ");
            sb.append(String.format("%-6s", fs.getName()));
            sb.append(String.format("%-7d", (fs.isErrs()) ? 1 : 0));
            sb.append(String.format("%-5d", (fs.isRetired()) ? 1 : 0));
            sb.append(String.format("%2d", fs.getColor()));
            sb.append("      ");
            sb.append(String.format("%-9d", (fs.isAidDflt()) ? 1 : 0));
            sb.append(String.format("%-10d", (fs.isIntDflt()) ? 1 : 0));
            sb.append(String.format("%-7d", (fs.isRadiiDflt()) ? 1 : 0));
            sb.append(String.format("%s", fs.getDescription()));

            sb.append(newline);
        }

        return sb.toString();
    }

    /**
     * Get a map of objective aids defined as the defaults for track forecast.
     *
     * @return Map<String, ObjectiveAidTechEntry>
     */
    public Map<String, ObjectiveAidTechEntry> getDefObjAidForTrackFcst() {
        Map<String, ObjectiveAidTechEntry> techs = new HashMap<>();
        for (ObjectiveAidTechEntry st : objectiveAidTechniques) {
            if (!st.isRetired() && st.isAidDflt()) {
                techs.put(st.getName(), st);
            }
        }

        return techs;
    }

    /**
     * Get a map of objective aids defined as the defaults for intensity
     * forecast.
     *
     * @return Map<String, ObjectiveAidTechEntry>
     */
    public Map<String, ObjectiveAidTechEntry> getDefObjAidForIntensityFcst() {
        Map<String, ObjectiveAidTechEntry> techs = new HashMap<>();
        for (ObjectiveAidTechEntry st : objectiveAidTechniques) {
            if (!st.isRetired() && st.isIntDflt()) {
                techs.put(st.getName(), st);
            }
        }

        return techs;
    }

    /**
     * Get a map of objective aids defined as the defaults for wind radii
     * forecast.
     *
     * @return Map<String, ObjectiveAidTechEntry>
     */
    public Map<String, ObjectiveAidTechEntry> getDefObjAidForWindRadiiFcst() {
        Map<String, ObjectiveAidTechEntry> techs = new HashMap<>();
        for (ObjectiveAidTechEntry st : objectiveAidTechniques) {
            if (!st.isRetired() && st.isRadiiDflt()) {
                techs.put(st.getName(), st);
            }
        }

        return techs;
    }

    /**
     * Get a map of objective aids defined as the defaults for errs.
     *
     * @return Map<String, ObjectiveAidTechEntry>
     */
    public Map<String, ObjectiveAidTechEntry> getDefObjAidForErrs() {
        Map<String, ObjectiveAidTechEntry> techs = new HashMap<>();
        for (ObjectiveAidTechEntry st : objectiveAidTechniques) {
            if (!st.isRetired() && st.isErrs()) {
                techs.put(st.getName(), st);
            }
        }

        return techs;
    }

}
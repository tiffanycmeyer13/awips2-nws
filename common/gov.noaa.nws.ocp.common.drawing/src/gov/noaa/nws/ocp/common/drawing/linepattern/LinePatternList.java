/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.linepattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This object contains a list of defined Line Patterns that can be applied to a
 * multi-point line path. The line patterns are stored as a list of
 * LinePatternMapEntry objects that each contain both the "key" and "value" of a
 * HashMap holding LinePatterns.
 * <P>
 * An object of this class is used by JAXB when marshaling/unmarshaling a list
 * of LinePatterns to/from an XML file.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 05, 2018 #48178     jwu         Extracted from NCEP PGEN.
 *
 * </pre>
 *
 * @author sgilbert
 * @version 1.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class LinePatternList {
    /**
     * A list of the available Line Patterns.
     */
    @XmlElement(name = "patternEntry")
    private List<LinePatternMapEntry> patternList;

    /**
     * Default constructor.
     */
    public LinePatternList() {

    }

    /**
     * Constructor used with existing pattern map
     * 
     * @param patternMap
     */
    public LinePatternList(Map<String, LinePattern> patternMap) {
        patternList = new ArrayList<>();
        for (Map.Entry<String, LinePattern> entry : patternMap.entrySet()) {
            patternList.add(
                    new LinePatternMapEntry(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * @param patternList
     *            the patternList to set
     */
    public void setPatternList(List<LinePatternMapEntry> patternList) {
        this.patternList = patternList;
    }

    /**
     * @return the patternList
     */
    public List<LinePatternMapEntry> getPatternList() {
        return patternList;
    }

}

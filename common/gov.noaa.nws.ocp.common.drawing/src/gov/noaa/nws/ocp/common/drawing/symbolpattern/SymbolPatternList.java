/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.common.drawing.symbolpattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This object contains a list of defined Symbol Patterns that can be applied to
 * a single-point drawable. The symbol patterns are stored as a list of
 * SymbolPatternMapEntry objects that each contain both the "key" and "value" of
 * a HashMap holding SymbolPatterns.
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
public class SymbolPatternList {

    /**
     * A list of the available Line Patterns.
     */
    @XmlElement(name = "patternEntry")
    private List<SymbolPatternMapEntry> patternList;

    /**
     * Default constructor.
     */
    public SymbolPatternList() {

    }

    /**
     * Constructor used with existing pattern map
     * 
     * @param patternMap
     */
    public SymbolPatternList(Map<String, SymbolPattern> patternMap) {
        patternList = new ArrayList<>();
        for (Map.Entry<String, SymbolPattern> entry : patternMap.entrySet()) {
            patternList.add(new SymbolPatternMapEntry(entry.getKey(),
                    entry.getValue()));
        }
    }

    /**
     * @param patternList
     *            the patternList to set
     */
    public void setPatternList(List<SymbolPatternMapEntry> patternList) {
        this.patternList = patternList;
    }

    /**
     * Gets the list of symbol patterns
     * 
     * @return the patternList
     */
    public List<SymbolPatternMapEntry> getPatternList() {
        return patternList;
    }

}

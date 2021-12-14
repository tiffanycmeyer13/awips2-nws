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
 * This class represents a list of storm states, which are used to compose
 * advisory product.
 *
 * <pre>
 *  Format:
 *    <StormStates>
 *      <State id="NO" retired="false" name="Normal" description=" "/>
 *      <State id="IN" retired="false" name="Inland" description="inland"/>
 *      ....
 *   </StormStates>
 *
 *  where:
 *      id         -  Unique identifier
 *      retired     - Flag to indicate if the entry should be shown in GUI.
 *                   false: show in GUI; true - do not show in GUI
 *      name        - Name shown in GUI
 *      description - Description shown in an advisory
 *
 *  Note:
 *      1. The entries in stormstates.xml are taking from nhc_writeadv.f.
 *      2. The "retired" flag is set to "true" for entries that does NOT
 *         appear in Advisory Composition => Forecast Type dialog. Otherwise,
 *         it is set to "false".
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2020 82721      jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "StormStates")
@XmlAccessorType(XmlAccessType.NONE)
public class StormStates {

    // Entries
    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "State", type = StormStateEntry.class) })
    private List<StormStateEntry> stormStates;

    /**
     * Constructor
     */
    public StormStates() {
        stormStates = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param stateList
     *            List of storm state entry
     */
    public StormStates(List<StormStateEntry> stateList) {
        stormStates = new ArrayList<>(stateList);
    }

    /**
     * @return the stormStates
     */
    public List<StormStateEntry> getStormStates() {
        return stormStates;
    }

    /**
     * @param stormStates
     *            the stormStates to set
     */
    public void setStormStates(List<StormStateEntry> stormStates) {
        this.stormStates = stormStates;
    }

    /**
     * Find the storm state entry with a given id.
     *
     * @return the StormStateEntry Null if no entry is found.
     */
    public StormStateEntry getStormStatebyID(String id) {
        StormStateEntry stormState = null;
        for (StormStateEntry st : stormStates) {
            if (st.getId().equals(id)) {
                stormState = st;
                break;
            }
        }

        return stormState;
    }

    /**
     * Find the storm state entry by a given name.
     *
     * @return the StormStateEntry Null if no entry is found.
     */
    public StormStateEntry getStormStatebyName(String stateName) {
        StormStateEntry stormState = null;
        for (StormStateEntry st : stormStates) {
            if (st.getName().equals(stateName)) {
                stormState = st;
                break;
            }
        }

        return stormState;
    }

    /**
     * Get the all non-retired storm state entries.
     *
     * @return the StormStates
     */
    public StormStates getAvailableStormStates() {
        StormStates states = new StormStates();
        for (StormStateEntry st : stormStates) {
            if (!st.isRetired()) {
                states.getStormStates().add(st);
            }
        }

        return states;
    }

    /**
     * Constructs a string representation of storm states
     */
    public String toString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        for (StormStateEntry fs : stormStates) {
            sb.append(String.format("State: id=\"%s", fs.getId()) + "\"");
            sb.append(String.format("\tretired=\"%s", fs.isRetired()) + "\"");
            sb.append(String.format("\tname=\"%s", fs.getName()) + "\"");
            sb.append(String.format("\tdescription=\"%s", fs.getDescription())
                    + "\"");
            sb.append(newline);
        }

        return sb.toString();
    }

}
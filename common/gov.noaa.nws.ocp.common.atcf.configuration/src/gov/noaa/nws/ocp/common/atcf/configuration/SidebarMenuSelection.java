/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class holds the entries defined in sidebar_selections.xml that are
 * listed in sidebar menu.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#   Engineer    Description
 * ------------ --------  ----------- --------------------------
 * Sep 14, 2018 54781     jwu         Initial creation
 * Oct 16, 2019 69594     jwu         Updated default entries.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlRootElement(name = "SidebarMenuSelection")
@XmlAccessorType(XmlAccessType.NONE)
public class SidebarMenuSelection {

    private static final String SEPARATOR_DASH = "-------------";

    @DynamicSerializeElement
    @XmlElements({ @XmlElement(name = "Entry", type = SidebarMenuEntry.class) })
    private List<SidebarMenuEntry> sidebarMenuSelection;

    /**
     * Default sidebar entries
     */
    protected static final String[] SIDEBAR_DEFAULT_ENTRIES = new String[] {
            "select storm", "clear ATCF display", SEPARATOR_DASH,
            "best tracks",
            "b-track 34-kt radii", "b-track 50-kt radii", "b-track 64-kt radii",
            "b-track RMW", "b-track ROCI", "b-track intensities",
            "b-track labels", SEPARATOR_DASH, "fixes", "fix wind radii",
            "fix autolabel", "fix confidences", SEPARATOR_DASH, "obj aids",
            "aid intensities", "aid 34-kt radii", "aid 50-kt radii",
            "aid 64-kt radii", "GPCE", "GPCE climatology", "GPCE-AX",
            SEPARATOR_DASH, "forecast track", "forecast wind radii",
            "forecast seas radii", "cumul wind probs", "forecast labels",
            SEPARATOR_DASH, "prepare compute data", "list compute data",
            "send compute data", "NWP model priority",
            "retrieve PRIMARY guidance", "run your own consensus",
            "list latest consensus run", SEPARATOR_DASH, "chart/storm titles",
            "logo-thumbnail", "34kt wind probs at sites", };

    // Default if an ATCF commands is not implemented yet.
    public static final String SIDEBAR_DEFAULT_COMMAND = "com.raytheon.viz.ui.actions.notImplemented";

    /**
     * Constructor
     */
    public SidebarMenuSelection() {
        sidebarMenuSelection = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param menuList
     *            List of sidebar menu entry
     */
    public SidebarMenuSelection(List<SidebarMenuEntry> menuList) {
        sidebarMenuSelection = new ArrayList<>(menuList);
    }

    /**
     * @return the sidebarMenuSelection
     */
    public List<SidebarMenuEntry> getSidebarMenuSelection() {
        return sidebarMenuSelection;
    }

    /**
     * @param sidebarMenuSelection
     *            the sidebarMenuSelection to set
     */
    public void setSidebarMenuSelection(
            List<SidebarMenuEntry> sidebarMenuSelection) {
        this.sidebarMenuSelection = sidebarMenuSelection;
    }

    /**
     * Find the entry with the given name.
     *
     * @return the SidebarMenuEntry
     */
    public SidebarMenuEntry getEntryByName(String name) {
        SidebarMenuEntry type = null;
        for (SidebarMenuEntry st : sidebarMenuSelection) {
            if (st.getName().equals(name)) {
                type = st;
                break;
            }
        }

        return type;
    }

    /**
     * Find the entry with the given alias.
     *
     * @return the SidebarMenuEntry
     */
    public SidebarMenuEntry getEntryByAlias(String alias) {
        SidebarMenuEntry type = null;
        for (SidebarMenuEntry st : sidebarMenuSelection) {
            if (st.getAlias().equals(alias)) {
                type = st;
                break;
            }
        }

        return type;
    }

    /**
     * Get all shown sidebar menu entries.
     *
     * @return the sidebarMenuSelection
     */
    public SidebarMenuSelection getAvailableSidebarMenuSelection() {
        SidebarMenuSelection selections = new SidebarMenuSelection();
        for (SidebarMenuEntry st : sidebarMenuSelection) {
            if (st.isShow()) {
                selections.getSidebarMenuSelection().add(st);
            }
        }

        return selections;
    }

    /**
     * Get a default set of sidebar menu entries.
     *
     * @return the sidebarMenuSelection
     */
    public SidebarMenuSelection getDefaultSidebarMenuSelection() {
        SidebarMenuSelection selections = new SidebarMenuSelection();
        for (String entryName : SIDEBAR_DEFAULT_ENTRIES) {

            SidebarMenuEntry entry = new SidebarMenuEntry(true, entryName,
                    entryName, SIDEBAR_DEFAULT_COMMAND);
            selections.getSidebarMenuSelection().add(entry);
        }

        return selections;
    }

    /**
     * Constructs a string representation of the data in the format of legacy
     * sidebarMenuSelection.dat.
     */
    public String toFileString() {
        final String newline = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("DO NOT DELETE THE NEXT TWO LINES -- ATCF depends on them.");
        sb.append(newline);
        sb.append("START_OF_DATA:");
        sb.append(newline);
        for (SidebarMenuEntry fs : sidebarMenuSelection) {
            sb.append(String.format("%d", (fs.isShow()) ? 1 : 0));
            sb.append("       # ");
            sb.append(String.format("%-s", fs.getName()));
            sb.append(newline);
        }

        return sb.toString();
    }

    public static List<String> getDefaultEntries() {
        return Collections.unmodifiableList(Arrays.asList(SIDEBAR_DEFAULT_ENTRIES));
    }

}
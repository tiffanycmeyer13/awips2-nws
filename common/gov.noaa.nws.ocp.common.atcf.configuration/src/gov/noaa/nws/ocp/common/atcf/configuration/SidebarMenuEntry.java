/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.common.atcf.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * This class represents a sidebar menu entry in sidebar_selections.xml, which
 * is used to configure the entries in ATCF sidebar.
 *
 * <pre>
 *
 * Legacy ATCF uses sidebar_sellctions_???.dat file to configure the entries in sidebar 
 * with the following format:
 *
 *    0       # zoom(click & drag)
 *    1       # select storm
 *    1       # select map
 *
 * The first column contains a 1 or 0 for on or off.
 * The remainder of the line is comments, not used by ATCF.
 * A 0 in the first column means that sidebar item will not appear in sidebar menu.
 * The lines are order dependent, meaning if you delete or add lines your results 
 * will be undefined.
 *
 * XML format will be used for A2 ATCF, each entry will have four attributes:
 *
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2017 #52692     jwu         Created
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
@DynamicSerialize
@XmlAccessorType(XmlAccessType.NONE)
public class SidebarMenuEntry {

    /**
     * Show/hide flag
     */
    @DynamicSerializeElement
    @XmlAttribute
    private boolean show;

    /**
     * Command name defined in ATCF plugin.xml.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String name;

    /**
     * Alias to appear in sidebar menu.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String alias;

    /**
     * Actual ATCF command for this menu entry.
     */
    @DynamicSerializeElement
    @XmlAttribute
    private String command;

    /**
     * Constructor.
     */
    public SidebarMenuEntry() {
    }

    /**
     * Constructor.
     * 
     * @param show
     *            If the entry be shown in sidebar
     * @param name
     *            name of the entry
     * @param alias
     *            alias or the entry to show in sidebar
     * @param command
     *            actual ATCF command
     */
    public SidebarMenuEntry(boolean show, String name, String alias,
            String command) {
        this.show = show;
        this.name = name;
        this.alias = alias;
        this.command = command;
    }

    /**
     * @return the show
     */
    public boolean isShow() {
        return show;
    }

    /**
     * @param show
     *            the show to set
     */
    public void setShow(boolean show) {
        this.show = show;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias
     *            the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command
     *            the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

}
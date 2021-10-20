/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

/**
 * Class to represent a menu item in ATCF graph menu.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 25, 2020 75391      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class GraphMenuItem {

    private String menuName;

    private String menuItem;

    private int value;

    /**
     * Constructor
     *
     * @param menuName
     *            Name for the main menu
     * @param menuItem
     *            Name for an item in the main menu
     * @param value
     *            Data value an item may carry.
     */
    public GraphMenuItem(String menuName, String menuItem, int value) {
        this.menuName = menuName;
        this.menuItem = menuItem;
        this.value = value;
    }

    /**
     * @return the menuName
     */
    public String getMenuName() {
        return menuName;
    }

    /**
     * @param menuName
     *            the menuName to set
     */
    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    /**
     * @return the menuItem
     */
    public String getMenuItem() {
        return menuItem;
    }

    /**
     * @param menuItem
     *            the menuItem to set
     */
    public void setMenuItem(String menuItem) {
        this.menuItem = menuItem;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }
}
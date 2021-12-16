/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;

/**
 * Class to contain all menu items' description in ATCF graph menu.
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
 *
 */
public class GraphMenuCollection {

    private Map<String, List<GraphMenuItem>> menuItems;

    private static final String[] menuBarItems = new String[] { "File",
            "Quadrant", "TAU", "Radius", "Select Aids", "Scale", "Help" };

    private static final String[] fileMenuItems = new String[] { "Print",
            "Print Landscape", "Save Graphic to File ...",
            "Send Graphic to ...", "Change Color ...", "Exit" };

    private static final String[] quadrantMenuItems = new String[] {
            "Northeast", "Southeast", "Southwest", "Northwest",
            "4 Panel Display" };

    private static final String[] radiusMenuItems = new String[] { "34", "50",
            "64" };

    private static final String RADIUS_MENU_ITEM_UNIT = "knots";

    private static final String[] scaleMenuItems = new String[] { "50", "100",
            "200", "300", "400", "500", "750", "1000", "1250", "1500" };

    private static final String SCALE_MENU_ITEM_UNIT = "nm";

    /**
     * Constructor
     */
    public GraphMenuCollection() {
        initialize();
    }

    /*
     * Initialize with all menu items in ATCF graph.
     */
    private void initialize() {

        if (menuItems == null) {
            menuItems = new LinkedHashMap<>();
        }

        // 0 - File
        List<GraphMenuItem> fileMenuList = new ArrayList<>();
        for (int ii = 0; ii < fileMenuItems.length; ii++) {
            fileMenuList.add(
                    new GraphMenuItem(menuBarItems[0], fileMenuItems[ii], ii));
        }
        menuItems.put(menuBarItems[0], fileMenuList);

        // 1 - Quadrant
        List<GraphMenuItem> quadMenuList = new ArrayList<>();
        for (int ii = 0; ii < quadrantMenuItems.length; ii++) {
            quadMenuList.add(new GraphMenuItem(menuBarItems[1],
                    quadrantMenuItems[ii], ii));
        }
        menuItems.put(menuBarItems[1], quadMenuList);

        // 2 - Tau
        List<GraphMenuItem> tauMenuList = new ArrayList<>();
        List<AtcfTaus> fcstTaus = AtcfTaus.getForecastTaus();
        for (AtcfTaus tau : fcstTaus) {
            tauMenuList.add(new GraphMenuItem(menuBarItems[2],
                    "" + tau.getValue(), tau.getValue()));
        }
        menuItems.put(menuBarItems[2], tauMenuList);

        // 3 - Radius
        List<GraphMenuItem> radiusMenuList = new ArrayList<>();
        for (int ii = 0; ii < radiusMenuItems.length; ii++) {
            int radius = Integer.parseInt(radiusMenuItems[ii]);
            radiusMenuList.add(new GraphMenuItem(menuBarItems[3],
                    radiusMenuItems[ii] + " " + RADIUS_MENU_ITEM_UNIT, radius));
        }
        menuItems.put(menuBarItems[3], radiusMenuList);

        // 4 - Select Aids
        List<GraphMenuItem> selectAidsMenuList = new ArrayList<>();
        selectAidsMenuList.add(new GraphMenuItem(menuBarItems[4], "", -1));
        menuItems.put(menuBarItems[4], selectAidsMenuList);

        // 5 - Scale
        List<GraphMenuItem> scaleMenuList = new ArrayList<>();
        for (int ii = 0; ii < scaleMenuItems.length; ii++) {
            int scale = Integer.parseInt(scaleMenuItems[ii]);
            scaleMenuList.add(new GraphMenuItem(menuBarItems[5],
                    scaleMenuItems[ii] + " " + SCALE_MENU_ITEM_UNIT, scale));
        }
        menuItems.put(menuBarItems[5], scaleMenuList);

        // 6 - Help
        List<GraphMenuItem> helpMenuList = new ArrayList<>();
        helpMenuList.add(new GraphMenuItem(menuBarItems[6], "", -1));
        menuItems.put(menuBarItems[6], helpMenuList);
    }

    /**
     * @return the menuItems
     */
    public Map<String, List<GraphMenuItem>> getMenuItems() {
        return menuItems;
    }

    /**
     * @param menuItems
     *            the menuItems to set
     */
    public void setMenuItems(Map<String, List<GraphMenuItem>> menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * Get a list of menu items for "File" menu
     */
    public List<GraphMenuItem> getFileMenuItems() {
        return menuItems.get(menuBarItems[0]);
    }

    /**
     * Get a list of menu items for "Quadrant" menu
     */
    public List<GraphMenuItem> getQuadrantMenuItems() {
        return menuItems.get(menuBarItems[1]);
    }

    /**
     * Get a list of menu items for "TAU" menu
     */
    public List<GraphMenuItem> getTauMenuItems() {
        return menuItems.get(menuBarItems[2]);
    }

    /**
     * Get a list of menu items for "Radius" menu
     */
    public List<GraphMenuItem> getRadiusMenuItems() {
        return menuItems.get(menuBarItems[3]);
    }

    /**
     * Get a list of menu items for "Radius" menu
     */
    public List<GraphMenuItem> getSelectAidsMenuItems() {
        return menuItems.get(menuBarItems[4]);
    }

    /**
     * Get a list of menu items for "Select Aids" menu
     */
    public List<GraphMenuItem> getScaleMenuItems() {
        return menuItems.get(menuBarItems[5]);
    }

    /**
     * Get a list of menu items for "Help" menu
     */
    public List<GraphMenuItem> getHelpMenuItems() {
        return menuItems.get(menuBarItems[6]);
    }

    /**
     * Find menu items in a given main menu.
     * 
     * @param String
     *            Name of the main menu
     * @return List<GraphMenuItem>
     */
    public List<GraphMenuItem> getMenuList(String menuName) {
        List<GraphMenuItem> menuList = new ArrayList<>();
        for (Map.Entry<String, List<GraphMenuItem>> entry : menuItems
                .entrySet()) {
            if (entry.getKey().equals(menuName)) {
                menuList.addAll(entry.getValue());
            }
        }

        return menuList;
    }
}
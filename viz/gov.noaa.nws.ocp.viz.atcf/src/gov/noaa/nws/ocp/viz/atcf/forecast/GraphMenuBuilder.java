/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * Build a general menu system to be used by ATCF graphs, including intensity
 * and wind radii graph.
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
public class GraphMenuBuilder {

    private Shell shell;

    // Listener for menu selections.
    private IGraphMenuListeners menuListener;

    // Menubar that holds all Menu/MenuItems.
    private Menu graphMenu;

    // Instance to hold description of all MenuItems.
    private GraphMenuCollection menuCollection;

    /**
     * Constructor
     *
     * @param parent
     *            Shell
     */
    public GraphMenuBuilder(Shell parent, IGraphMenuListeners menuListener) {
        this.shell = parent;
        this.menuListener = menuListener;

        // Retrieve all menu entries.
        this.menuCollection = new GraphMenuCollection();

        // Create the menu
        createMenuBar();
    }

    /**
     * @return the graphMenu
     */
    public Menu getGraphMenu() {
        return graphMenu;
    }

    /**
     * Get "File" menu.
     * 
     * @return MenuItem File menu
     */
    public MenuItem getFileMenu() {
        return graphMenu.getItem(0);
    }

    /**
     * Get "Quadrant" menu.
     * 
     * @return MenuItem Quadrant menu
     */
    public MenuItem getQuadrantMenu() {
        return graphMenu.getItem(1);
    }

    /**
     * Get "TAU" menu.
     * 
     * @return MenuItem Tau menu
     */
    public MenuItem getTauMenu() {
        return graphMenu.getItem(2);
    }

    /**
     * Get "Radius" menu.
     * 
     * @return MenuItem Radius menu
     */
    public MenuItem getRadiusMenu() {
        return graphMenu.getItem(3);
    }

    /**
     * Get "Select Aids" menu.
     * 
     * @return MenuItem Select Aids menu
     */
    public MenuItem getSelectAidsMenu() {
        return graphMenu.getItem(4);
    }

    /**
     * Get "Scale" menu.
     * 
     * @return MenuItem Scale menu
     */
    public MenuItem getScaleMenu() {
        return graphMenu.getItem(5);
    }

    /**
     * Get "Help" menu.
     * 
     * @return MenuItem Help menu
     */
    public MenuItem getHelpMenu() {
        return graphMenu.getItem(6);
    }

    /**
     * Enable/Disable a main menu or menu items in it.
     *
     * @param mainMenuName
     *            Name of a main menu
     * @param name
     *            Name of a menu item within the main menu
     * @param enabled
     *            true - enable; false - disable
     * @param all
     *            true - enable/disable all items under main menu; false -
     *            enable/disable only the given subitem.
     */
    public void enable(String mainMenuName, String itemName, boolean enabled,
            boolean all) {
        MenuItem[] mainMenu = graphMenu.getItems();
        for (MenuItem mmi : mainMenu) {
            if (mmi.getText().contains(mainMenuName)) {
                MenuItem[] subMenu = mmi.getMenu().getItems();
                if (all) {
                    for (MenuItem smi : subMenu) {
                        smi.setEnabled(enabled);
                    }
                } else {
                    for (MenuItem smi : subMenu) {
                        if (smi.getText().contains(itemName)) {
                            smi.setEnabled(enabled);
                        }
                    }
                }
            }
        }
    }

    /*
     * Create the menu bar for the dialog.
     */
    private void createMenuBar() {

        graphMenu = new Menu(shell, SWT.BAR);

        Map<String, List<GraphMenuItem>> menuEntries = menuCollection
                .getMenuItems();

        int ii = 0;
        for (Map.Entry<String, List<GraphMenuItem>> entry : menuEntries
                .entrySet()) {
            MenuItem mainMenuItem = new MenuItem(graphMenu, SWT.CASCADE);
            String mainMenuName = entry.getKey();
            mainMenuItem.setText("&" + mainMenuName);
            mainMenuItem.setData(ii);
            List<GraphMenuItem> subItems = entry.getValue();
            if (subItems.size() > 1) {
                // Menu with sub-items
                Menu subMenu = new Menu(graphMenu);
                mainMenuItem.setMenu(subMenu);

                for (GraphMenuItem mi : subItems) {
                    MenuItem subMenuItem = new MenuItem(subMenu, SWT.PUSH);
                    subMenuItem.setText("&" + mi.getMenuItem());
                    subMenuItem.setData(mi.getValue());
                    subMenuItem.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent event) {
                            menuSelected(mainMenuItem, subMenuItem);
                        }
                    });
                }
            } else {
                // Menu without sub items (i.e., "Select Aids")
                mainMenuItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent event) {
                        menuSelected(mainMenuItem, null);
                    }

                });
            }

            ii++;
        }
    }

    /*
     * Handle requests from a menu selection.
     * 
     * @param main MenuItem from main menubar
     * 
     * @param item MenuItem from each menu
     */
    private void menuSelected(MenuItem mainItem, MenuItem subItem) {
        int menubarIndex = (int) mainItem.getData();

        switch (menubarIndex) {
        case 0:
            fileMenuItemSelected(subItem);
            break;
        case 1:
            quadrantMenuItemSelected(subItem);
            break;
        case 2:
            tauMenuItemSelected(subItem);
            break;
        case 3:
            radiusMenuItemSelected(subItem);
            break;
        case 4:
            selectAids();
            break;
        case 5:
            scaleMenuItemSelected(subItem);
            break;
        case 6:
            help();
            break;
        default:
            break;
        }
    }

    /*
     * Handle a request from "File" menu selection.
     * 
     * @param MenuItem Selected menu item from File menu
     */
    private void fileMenuItemSelected(MenuItem mi) {

        int itemIndex = (int) mi.getData();

        switch (itemIndex) {
        case 0:
            menuListener.printGraphic();
            break;
        case 1:
            menuListener.printGraphicAsLandscape();
            break;
        case 2:
            menuListener.saveGraphicToFile();
            break;
        case 3:
            menuListener.emailGraphic();
            break;
        case 4:
            menuListener.changeColor();
            break;
        case 5:
            menuListener.exitGraphWindow();
            break;
        default:
            break;
        }
    }

    /*
     * Handle a request from "Quadrant" menu selection.
     * 
     * @param MenuItem Selected menu item from Quadrant menu
     */
    private void quadrantMenuItemSelected(MenuItem mi) {
        int itemIndex = (int) mi.getData();
        menuListener.quadrantSelected(itemIndex);
    }

    /*
     * Handle a request from "Tau" menu selection.
     * 
     * @param MenuItem Selected menu item from TAU menu
     */
    private void tauMenuItemSelected(MenuItem mi) {
        int tau = (int) mi.getData();
        menuListener.tauSelected(tau);
    }

    /*
     * Handle a request from "Radius" menu selection.
     * 
     * @param MenuItem Selected menu item from Radius menu
     */
    private void radiusMenuItemSelected(MenuItem mi) {
        int radius = (int) mi.getData();
        menuListener.radiusSelected(radius);
    }

    /*
     * Handle a request for "Select Aids".
     */
    private void selectAids() {
        menuListener.selectAids();
    }

    /*
     * Handle a request from "Radius" menu selection.
     * 
     * @param MenuItem Selected menu item from Radius menu
     */
    private void scaleMenuItemSelected(MenuItem mi) {
        int scale = (int) mi.getData();
        menuListener.scaleSelected(scale);
    }

    /*
     * Handle a request for "Help".
     */
    private void help() {
        menuListener.help();
    }
}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.edex.plugin.odim.menus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.menus.xml.CommonAbstractMenuContribution;
import com.raytheon.uf.common.menus.xml.CommonIncludeMenuContribution;
import com.raytheon.uf.common.menus.xml.CommonIncludeMenuItem;
import com.raytheon.uf.common.menus.xml.CommonMenuContributionFile;
import com.raytheon.uf.common.menus.xml.CommonSubmenuContribution;
import com.raytheon.uf.common.menus.xml.MenuTemplateFile;
import com.raytheon.uf.common.menus.xml.VariableSubstitution;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.edex.menus.AbstractMenuUtil;

/**
 * Generates menus for the ODIM plugin.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * Nov  4, 2022 DR  23341  dfriedman   Handle empty custom radar lists.
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMMenuUtilImpl extends AbstractMenuUtil {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ODIMMenuUtilImpl.class);

    @Override
    public void createMenus() {
        ODIMMenus menus;
        try {
            menus = ODIMMenus.load(getSite());
        } catch (LocalizationException e) {
            handler.handle(Priority.PROBLEM,
                    "Failed to create menus:" + e.getLocalizedMessage(), e);
            return;
        }
        createToplevelMenus(menus);
        createLocalMenus(menus);

    }

    private void createToplevelMenus(ODIMMenus menus) {
        String menuPath = getMenuPath("index.xml").getPath();
        CommonMenuContributionFile menuContributionFile = new CommonMenuContributionFile();
        List<CommonIncludeMenuItem> items = new ArrayList<>();
        CommonIncludeMenuItem includeMenuItem = null;
        List<String> radars = menus.getToplevelRadars();
        if (!radars.isEmpty()) {
            for (String radar : radars) {
                /*
                 * Currently only supports Canadian S-Band radars. Other kinds
                 * of radars will need specific support or a more general system
                 * of menu generation is required.
                 */
                includeMenuItem = createCanadianRadarMenuInclude(radar);
                items.add(includeMenuItem);
            }
            for (CommonIncludeMenuItem item : items) {
                item.visibleOnActionSet = new String[] {
                        "com.raytheon.uf.viz.d2d.ui.D2DActionSet" };
                item.installationLocation = "menu:org.eclipse.ui.main.menu?after=satellite";
            }
            /*
             * Reverse the order of the items so that inserting to
             * menu:...?after=satellite results in the items being in the same
             * order as in the menu config file.
             */
            Collections.reverse(items);
            menuContributionFile.contribution = items
                    .toArray(CommonIncludeMenuItem[]::new);
            toXml(menuContributionFile, menuPath);
        } else {
            removeFile(menuPath);
        }
    }

    private void createLocalMenus(ODIMMenus menus) {
        String menuPath = getMenuPath("caLocalRadars.xml").getPath();
        MenuTemplateFile menuTemplateFile = new MenuTemplateFile();
        List<CommonIncludeMenuContribution> subMenus = new ArrayList<>();
        CommonIncludeMenuContribution subMenu = null;
        List<String> radars = menus.getLocalRadars();
        Collections.sort(radars);
        if (!radars.isEmpty()) {
            for (String radar : radars) {
                /*
                 * Currently only supports Canadian S-Band radars. Other kinds
                 * of radars will need specific support or a more general system
                 * of menu generation is required.
                 */
                subMenu = createCanadianRadarMenuContribution(radar);
                subMenus.add(subMenu);
            }

            /*
             * If there are more than 12 menu items, split into submenus of up
             * to 12 items each.
             */
            CommonAbstractMenuContribution[] contributions;
            if (subMenus.size() > 12) {
                List<CommonAbstractMenuContribution> groups = new ArrayList<>();

                int i = 0;
                while (i < subMenus.size()) {
                    int j = Math.min(i + 12, subMenus.size());
                    if (j - i > 1) {
                        List<CommonIncludeMenuContribution> subItems = subMenus
                                .subList(i, j);
                        CommonSubmenuContribution group = new CommonSubmenuContribution();
                        group.menuText = subItems.get(0).substitutions[0].value
                                + "-" + subItems.get(subItems.size()
                                        - 1).substitutions[0].value;
                        group.contributions = subItems
                                .toArray(CommonAbstractMenuContribution[]::new);
                        groups.add(group);
                    } else {
                        // Do not create a submenu for a single leftover item.
                        groups.add(subMenus.get(i));
                    }
                    i = j;
                }

                contributions = groups
                        .toArray(CommonAbstractMenuContribution[]::new);
            } else {
                contributions = subMenus
                        .toArray(CommonAbstractMenuContribution[]::new);
            }

            menuTemplateFile.contributions = contributions;
            toXml(menuTemplateFile, menuPath);
        } else {
            removeFile(menuPath);
        }

    }

    private Pair<File, VariableSubstitution[]> createCanadianRadarMenu(
            String radar) {
        VariableSubstitution[] vars = new VariableSubstitution[1];
        vars[0] = subst("node", radar);
        return new Pair<>(getMenuPath("baseODIMMenu.xml"), vars);
    }

    private CommonIncludeMenuItem createCanadianRadarMenuInclude(String radar) {
        Pair<File, VariableSubstitution[]> pair = createCanadianRadarMenu(
                radar);
        CommonIncludeMenuItem item = new CommonIncludeMenuItem();
        item.fileName = pair.getFirst();
        item.substitutions = pair.getSecond();
        return item;
    }

    private CommonIncludeMenuContribution createCanadianRadarMenuContribution(
            String radar) {
        Pair<File, VariableSubstitution[]> pair = createCanadianRadarMenu(
                radar);
        CommonIncludeMenuContribution item = new CommonIncludeMenuContribution();
        item.fileName = pair.getFirst();
        item.substitutions = pair.getSecond();
        return item;
    }

    private VariableSubstitution subst(String key, String value) {
        return new VariableSubstitution(key, value);
    }

    private File getMenuPath(String path) {
        return new File(LocalizationUtil.join("menus", "odim", path));
    }

    @Override
    protected boolean checkCreated() {
        return super.checkCreated("odimMenus.txt", "odim")
                && super.checkCreated("caRadarElevs.txt", "odim");
    }

    private void removeFile(String menuPath) {
        LocalizationContext context = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.CONFIGURED);
        context.setContextName(getSite());
        ILocalizationFile file = pm.getLocalizationFile(context, menuPath);
        try {
            file.delete();
        } catch (LocalizationException e) {
            handler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

}

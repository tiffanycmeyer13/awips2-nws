/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.edex.plugin.odim.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * ODIM menu configuration
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2022 DCS 21569  dfriedman   Initial creation
 * </pre>
 *
 * @author dfriedman
 */
public class ODIMMenus {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(ODIMMenus.class);

    private static final String PATH = LocalizationUtil.join("odim",
            "odimMenus.txt");

    private static final String TOPLEVEL_KEY = "toplevel";

    private static final String LOCAL_KEY = "local";

    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z0-9]+");

    private static final Pattern SEP_PATTERN = Pattern.compile(":");

    private ArrayList<String> toplevelRadars = new ArrayList<>();

    private ArrayList<String> localRadars = new ArrayList<>();

    public List<String> getToplevelRadars() {
        return toplevelRadars;
    }

    public List<String> getLocalRadars() {
        return localRadars;
    }

    public static ODIMMenus load(String site) throws LocalizationException {
        ODIMMenus result = new ODIMMenus();
        LocalizationFile lf = getLocalizationFile(site);

        if (lf != null && lf.exists()) {
            try (Scanner s = new Scanner(lf.openInputStream())) {
                while (s.hasNextLine()) {
                    String line = s.nextLine();

                    // Strip comments and ignore empty lines.
                    line = line.replaceFirst("#.*", "");
                    line = line.strip();
                    if (line.isEmpty()) {
                        continue;
                    }

                    parseLine(lf, line, result);
                }
            }
        }
        return result;
    }

    private static void parseLine(LocalizationFile lf, String line,
            ODIMMenus result) {
        try (Scanner ls = new Scanner(line)) {
            String key = null;
            String sep = null;
            ArrayList<String> values = new ArrayList<>();

            try {
                key = ls.next(WORD_PATTERN);
                sep = ls.next(SEP_PATTERN);
                values = new ArrayList<>();
            } catch (NoSuchElementException // NOSONAR
                    | IllegalStateException e) {
                // Will report as error below.
            }

            while (ls.hasNext(WORD_PATTERN)) {
                values.add(ls.next(WORD_PATTERN));
            }
            /*
             * If the key and ':' were present and there are no more tokens on
             * the line, it is valid.
             */
            if (key != null && sep != null && !ls.hasNext()) {
                switch (key) {
                case TOPLEVEL_KEY:
                    result.toplevelRadars = values;
                    break;
                case LOCAL_KEY:
                    result.localRadars = values;
                    break;
                default:
                    handler.error(
                            String.format("%s: invalid key \"%s\"", lf, key));
                }
            } else {
                handler.error(
                        String.format("%s: invalid line \"%s\"", lf, line));
            }
        }
    }

    private static LocalizationFile getLocalizationFile(String site) {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = null;

        // If the site is not given, default to the regular site.
        if (site == null) {
            context = pm.getContext(LocalizationType.COMMON_STATIC,
                    LocalizationLevel.SITE);
            site = context.getContextName();
        } else {
            context = pm.getContextForSite(LocalizationType.COMMON_STATIC,
                    site);
        }

        LocalizationFile lf = pm.getLocalizationFile(context, PATH);
        if (!lf.exists()) {
            LocalizationContext baseContext = pm.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
            lf = pm.getLocalizationFile(baseContext, PATH);
            handler.info("Site " + PATH + " file not configured for " + site
                    + ".  Using the base file.");

        }
        return lf;
    }

}

package gov.noaa.nws.mdl.viz.awipsref;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXB;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Loads the configuration for AWIPS II Reference System.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer    Description
 * ------------- -------- ----------- --------------------------
 * Jul 01, 2016           jburks       Initial creation
 * Jul 09, 2016           jburks       Added Exception Handling,changed location of configuration, 
 *                                     and refactor to awips reference
 * 
 * 
 * </pre>
 * 
 * @author jburks
 * @version 1.0
 */

public class AWIPSRefConfigurationManager {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AWIPSRefConfigurationManager.class);

    private final static String AWIPSREF_CONFIG_FILE = "awipsRefConfig.xml";

    private static AWIPSRefConfigurationManager instance;

    private AWIPSRefConfigurationManager() {

    }

    public static AWIPSRefConfigurationManager getInstance() {
        if (instance == null) {
            instance = new AWIPSRefConfigurationManager();
        }
        return instance;
    }

    public AWIPSRefConfiguration loadConfiguration() {
        String path = "awipsref" + IPathManager.SEPARATOR
                + AWIPSREF_CONFIG_FILE;
        IPathManager pm = PathManagerFactory.getPathManager();

        List<LocalizationContext> contexts = Arrays.asList(pm
                .getLocalSearchHierarchy(LocalizationType.CAVE_STATIC));
        Collections.reverse(contexts);
        AWIPSRefConfiguration jittConfig = null;
        for (LocalizationContext ctx : contexts) {
            File f = pm.getFile(ctx, path);
            if (f != null && f.isFile()) {
                File file = pm.getStaticFile(path);

                try {
                    jittConfig = JAXB.unmarshal(file,
                            AWIPSRefConfiguration.class);

                    return jittConfig;
                } catch (RuntimeException e) {
                    statusHandler
                            .error("Problem loading the AWIPS Reference Configuration file: "
                                    + AWIPSREF_CONFIG_FILE);

                }
            }
        }
        return jittConfig;
    }

}

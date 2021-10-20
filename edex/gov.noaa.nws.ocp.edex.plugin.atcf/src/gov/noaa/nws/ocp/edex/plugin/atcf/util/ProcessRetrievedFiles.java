/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 *
 **/
package gov.noaa.nws.ocp.edex.plugin.atcf.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.PythonScript;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils.LockState;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfEnvironmentConfig;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import jep.JepConfig;
import jep.JepException;

/**
 * Process the files retrieved from WCOSS and OPAH
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- -----------  --------------------------
 * Sep 09, 2020 80672      mporricelli  Initial creation
 *
 * </pre>
 *
 * @author porricel
 * @version 1.0
 */

@DynamicSerialize
public class ProcessRetrievedFiles {

    private static final long CLUSTER_LOCK_TIMEOUT = 5
            * TimeUtil.MILLIS_PER_MINUTE;

    private static final String PYTHON_SCRIPT_PATH = LocalizationUtil
            .join("atcf", "wcoss", "scripts", "ProcessAtcfFiles.py");

    public void execProcessScript(String file, String rootDir, Storm storm,
            String dtg, AtcfEnvironmentConfig envConfig) throws JepException {

        Map<String, Object> args = new HashMap<>();

        args.put("input_tar", file);
        args.put("tempDir", rootDir);
        args.put("storm", storm);
        args.put("dtg", dtg);
        args.put("envCfg", envConfig);

        ClusterTask ct = null;
        try {
            do {
                ct = ClusterLockUtils.lock("procAtcfFileLock", "procAtcfFiles",
                        CLUSTER_LOCK_TIMEOUT, true);
            } while (!LockState.SUCCESSFUL.equals(ct.getLockState()));

            PythonScript pyScript = null;
            try {
                pyScript = getPython();
            } catch (JepException e) {
                throw new JepException("Failed to get python script: "
                        + PYTHON_SCRIPT_PATH + " " + e.getMessage(), e);
            }
            try {
                pyScript.execute("processATCF", args);
            } catch (JepException e) {
                throw new JepException("Exception while running python script: "
                        + PYTHON_SCRIPT_PATH + " " + e.getMessage(), e);
            } finally {
                pyScript.close();
            }
        } finally {
            ClusterLockUtils.unlock(ct, false);
        }

    }

    /**
     * Get the ProcessAtcfFiles script
     *
     * @return
     * @throws JepException
     */
    private PythonScript getPython() throws JepException {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext[] lc = getBaseSiteContexts();
        File runner = null;
        String filePath = null;
        String includePath = null;
        for (LocalizationContext ctx : lc) {
            runner = pm.getStaticFile(ctx.getLocalizationType(),
                    PYTHON_SCRIPT_PATH);
            filePath = runner.getPath();
            includePath = PyUtil.buildJepIncludePath(
                    runner.getParentFile().getPath(),
                    pm.getFile(ctx, "python").getPath());
            if (ctx.getLocalizationLevel() == LocalizationLevel.SITE) {
                break;
            }

        }
        JepConfig config = new JepConfig().setIncludePath(includePath)
                .setClassLoader(getClass().getClassLoader());
        return new PythonScript(config, filePath);
    }

    /**
     * Get localization contexts
     *
     */
    private static LocalizationContext[] getBaseSiteContexts() {

        IPathManager pm = PathManagerFactory.getPathManager();

        return new LocalizationContext[] {
                pm.getContext(LocalizationType.COMMON_STATIC,
                        LocalizationLevel.SITE),
                pm.getContext(LocalizationType.COMMON_STATIC,
                        LocalizationLevel.BASE) };
    }
}

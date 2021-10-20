package gov.noaa.nws.ocp.edex.plugin.atcf.purge;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.database.DataAccessLayerException;

import gov.noaa.nws.ocp.edex.plugin.atcf.dao.AtcfProcessDao;

/**
 * SandboxPurger 1) Purge submitted sandboxes when retention time reached 2)
 * Purge idle sandboxes which are not updated longer than configured retention
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2019            pwang     Initial creation
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class SandboxPurger {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SandboxPurger.class);

    private static final String SANDBOX_PURGE_FILE = "sandbox_purge.properties";

    private static final String ATCF_FOLDER = "atcf" + File.separator;

    private static final String SANDBOX_PURGE_PATH = ATCF_FOLDER
            + SANDBOX_PURGE_FILE;

    private static final String PROP_NAME_SUBMIT = "submitted.retention.hr";

    private static final String PROP_NAME_IDLE = "idle.retention.hr";

    private static final int DEFAULT_SUBMIT_RETENTION = 24;

    private static final int DEFAULT_IDLE_RETENTION = 96;

    /**
     * Localization levels to try to load, in order
     */
    private static final LocalizationLevel[] LOCALIZATIONS_TO_TRY = new LocalizationLevel[] {
            LocalizationLevel.SITE, LocalizationLevel.BASE };


    /**
     * purgeSandbox purge expired sandboxes, called by camel
     */
    public void purgeSandbox() {
        this.purgeIdleSandbox();
        this.purgeSubmittedSandbox();
    }

    private AtcfProcessDao getDao() throws Exception {
        AtcfProcessDao dao = null;
        try {
            dao = new AtcfProcessDao();
        } catch (Exception e) {
            throw new DataAccessLayerException(
                    "Failed to instantiate ClimateProdSendRecordDAO", e);
        }

        return dao;
    }

    private void purgeSubmittedSandbox() {

        Properties props = loadSandboxPurgeProperties();

        int submitRetention = Integer
                .parseInt(props.getProperty(PROP_NAME_SUBMIT));
        Calendar threshold = Calendar.getInstance();
        threshold.add(Calendar.HOUR, -submitRetention);
        List<Integer> purgedSandboxes = null;
        try {
            AtcfProcessDao dao = getDao();
            purgedSandboxes = dao.purgeSubmittedSandboxes(threshold);
        } catch (Exception e) {
            logger.error("Failed to purge submitted sandboxes", e);
        }

        StringBuilder sboxList = new StringBuilder(
                " Following submitted sandbox have been purged: ");
        if (purgedSandboxes != null && !purgedSandboxes.isEmpty()) {
            for (Integer sbid : purgedSandboxes) {
                sboxList.append(sbid).append(",");
            }
            logger.info(sboxList.toString());
        }

    }

    private void purgeIdleSandbox() {

        Properties props = loadSandboxPurgeProperties();
        int idleRetention = Integer.parseInt(props.getProperty(PROP_NAME_IDLE));
        Calendar threshold = Calendar.getInstance();
        threshold.add(Calendar.HOUR, -idleRetention);

        List<Integer> purgedSandboxes = null;
        try {
            AtcfProcessDao dao = getDao();
            purgedSandboxes = dao.purgeIdleSandboxes(threshold);
        } catch (Exception e) {
            logger.error("Failed to purge idle sandboxes", e);
        }

        StringBuilder sboxList = new StringBuilder(
                " Following idle sandbox have been purged: ");
        if (purgedSandboxes != null && !purgedSandboxes.isEmpty()) {
            for (Integer sbid : purgedSandboxes) {
                sboxList.append(sbid).append(",");
            }
            logger.info(sboxList.toString());
        }

    }

    private Properties loadSandboxPurgeProperties() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile sboxPurgeRetentionFile = pm
                .getStaticLocalizationFile(SANDBOX_PURGE_PATH);

        if (sboxPurgeRetentionFile != null) {
            try (InputStream is = sboxPurgeRetentionFile.openInputStream()) {
                Properties sbpurgeParams = new Properties();
                sbpurgeParams.load(is);
                return sbpurgeParams;
            } catch (Exception e) {
                logger.error(
                        "Failed to load sandbox retention times; using defaults.",
                        e);
            }
        } else {
            logger.warn("Sandbox retention times file not found; using defaults.");
        }
        return defaultProperties();

    }

    private static Properties defaultProperties() {
        Properties props = new Properties();
        props.setProperty(PROP_NAME_SUBMIT,
                String.valueOf(DEFAULT_SUBMIT_RETENTION));
        props.setProperty(PROP_NAME_IDLE,
                String.valueOf(DEFAULT_IDLE_RETENTION));
        return props;
    }

}

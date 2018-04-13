/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 **/
package gov.noaa.nws.ocp.edex.climate.prodgen;

import java.time.LocalDateTime;
import java.util.Date;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.core.EDEXUtil;

import gov.noaa.nws.ocp.common.dataplugin.climate.util.ClimateMessageUtils;
import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateAlertUtils;

/**
 * CPG session purger, for expired/completed sessions.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 6, 2017  20637      pwang       Initial creation
 * 23 MAR 2017  30515      amoore      Replace constants that are already defined in AWIPS.
 * 19 MAY 2017  33104      amoore      Increase retention time for purge of sessions.
 *                                     As discussed in past demos, sometimes work could occur
 *                                     while users are gone for a weekend.
 * 26 JUL 2017  33104      amoore      Address review comments.
 * 03 NOV 2017  36749      amoore      Fix class name capitalization. Privatize default hours.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateProdGenerateSessionPurger {

    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdGenerateSessionPurger.class);

    private static final long DEFAULT_RETENTION_HOURS = 3
            * TimeUtil.HOURS_PER_DAY;

    private ClimateProdGenerateSessionDAO dao = null;

    private long retentionHours = DEFAULT_RETENTION_HOURS;

    /**
     * Construct an instance of this transformer.
     */
    public ClimateProdGenerateSessionPurger() {
        this.retentionHours = DEFAULT_RETENTION_HOURS;
        try {
            dao = new ClimateProdGenerateSessionDAO();
        } catch (Exception e) {
            logger.error("Failed to instantiate ClimateProdGenerateSessionDAO",
                    e);
        }
    }

    /**
     * Constructor with passed in retention hours
     * 
     * @param retentionHours
     */
    public ClimateProdGenerateSessionPurger(long retentionHours) {

        this.retentionHours = retentionHours;
        try {
            dao = new ClimateProdGenerateSessionDAO();
        } catch (Exception e) {
            logger.error("Failed to instantiate ClimateProdGenerateSessionDAO",
                    e);
        }
    }

    /**
     * purge saved Climate Product Generate sessions
     */
    public void purgeTerminatedCPGSession() {
        if (dao != null) {
            LocalDateTime purgeThreshold = LocalDateTime.now()
                    .minusHours(retentionHours);

            logger.info("Purge CPG Session started [" + retentionHours + "]");

            int purgedCount = 0;
            try {
                purgedCount = dao.purgeTerminatedCPGSession(purgeThreshold);
            } catch (Exception e) {
                logger.error("Failed to execute purgeTerminatedCPGSession", e);
            }

            if (purgedCount > 0) {
                this.sendPurgeNotifyMessage(purgedCount);
            }
            logger.info(purgedCount + " CPG Session were purged");
        } else {
            logger.error(
                    "CPG Purging DAO is null. Cannot perform purge action.");
        }
    }

    /**
     * @return the dao
     */
    public ClimateProdGenerateSessionDAO getDao() {
        return dao;
    }

    /**
     * @param dao
     *            the dao to set
     */
    public void setDao(ClimateProdGenerateSessionDAO dao) {
        this.dao = dao;
    }

    /**
     * @return the retentionHours
     */
    public long getRetentionHours() {
        return retentionHours;
    }

    /**
     * @param retentionHours
     *            the retentionHours to set
     */
    public void setRetentionHours(long retentionHours) {
        this.retentionHours = retentionHours;
    }

    /**
     * Send alert viz notification of purge.
     * 
     * @param count
     */
    public void sendPurgeNotifyMessage(int count) {
        String detailMsg = "PURGER=CPG_SESSION, PURGE_COUNT=" + count;

        StatusMessage sm = new StatusMessage();
        sm.setPriority(Priority.INFO);
        sm.setPlugin(ClimateMessageUtils.CPG_PLUGIN_ID);
        sm.setCategory(ClimateAlertUtils.CATEGORY_CLIMATE);
        sm.setMessage("CPG Session table was purged");
        sm.setMachineToCurrent();
        sm.setSourceKey("EDEX");
        sm.setDetails(detailMsg);
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer()
                    .sendAsync(ClimateAlertUtils.CPG_ENDPOINT, sm);
        } catch (Exception e) {
            logger.error("Could not send message to ClimateView", e);
        }

    }

}

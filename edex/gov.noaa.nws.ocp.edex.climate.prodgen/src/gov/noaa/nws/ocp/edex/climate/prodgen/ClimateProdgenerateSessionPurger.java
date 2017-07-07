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

import gov.noaa.nws.ocp.edex.climate.prodgen.dao.ClimateProdGenerateSessionDAO;

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
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateProdgenerateSessionPurger {

    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(ClimateProdgenerateSessionPurger.class);

    public static final long DEFAULT_RETENTION_HOURS = 3
            * TimeUtil.HOURS_PER_DAY;

    private ClimateProdGenerateSessionDAO dao = null;

    private long retentionHours = DEFAULT_RETENTION_HOURS;

    /**
     * Construct an instance of this transformer.
     */
    public ClimateProdgenerateSessionPurger() {
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
    public ClimateProdgenerateSessionPurger(long retentionHours) {

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

        LocalDateTime purgeThreshold = LocalDateTime.now()
                .minusHours(retentionHours);

        logger.info("Purge CPG Session started [" + retentionHours + "]");

        int purgedCount = 0;

        if (dao != null) {
            try {
                purgedCount = dao.purgeTerminatedCPGSession(purgeThreshold);
            } catch (Exception e) {
                logger.error("Failed to execute purgeTerminatedCPGSession", e);
            }

            if (purgedCount > 0) {
                this.sendPurgeNotifyMessage(purgedCount);
            }
            logger.info(purgedCount + " CPG Session were purged");
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
        sm.setPriority(Priority.EVENTA);
        sm.setPlugin(ClimateProdGenerateSession.PLUGIN_ID);
        sm.setCategory(ClimateProdGenerateSession.CATEGORY);
        sm.setMessage("CPG Session table was purged");
        sm.setMachineToCurrent();
        sm.setSourceKey("EDEX");
        sm.setDetails(detailMsg);
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer()
                    .sendAsync(ClimateProdGenerateSession.cpgEndpoint, sm);
        } catch (Exception e) {
            logger.error("Could not send message to ClimateView", e);
        }

    }

}

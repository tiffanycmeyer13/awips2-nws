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
import gov.noaa.nws.ocp.edex.common.climate.dao.ClimateProdSendRecordDAO;
import gov.noaa.nws.ocp.edex.common.climate.util.ClimateAlertUtils;

/**
 * SentClimateProductRecordPurger
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 7, 2017  34790      pwang       Initial creation
 * 26 JUL 2017  33104      amoore      Address review comments.
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class SentClimateProductRecordPurger {

    private static final transient IUFStatusHandler logger = UFStatus
            .getHandler(SentClimateProductRecordPurger.class);

    public static final long DEFAULT_RETENTION_HOURS = 7
            * TimeUtil.HOURS_PER_DAY;

    private ClimateProdSendRecordDAO dao = null;

    private long retentionHours = DEFAULT_RETENTION_HOURS;

    /**
     * Construct an instance of this transformer.
     */
    public SentClimateProductRecordPurger() {

        this.retentionHours = DEFAULT_RETENTION_HOURS;
        try {
            dao = new ClimateProdSendRecordDAO();
        } catch (Exception e) {
            logger.error("Failed to instantiate ClimateProdSendRecordDAO", e);
        }
    }

    /**
     * Constructor with passed in retention hours
     * 
     * @param retentionHours
     */
    public SentClimateProductRecordPurger(long retentionHours) {

        this.retentionHours = retentionHours;
        try {
            dao = new ClimateProdSendRecordDAO();
        } catch (Exception e) {
            logger.error("Failed to instantiate ClimateProdSendRecordDAO", e);
        }
    }

    /**
     * purge saved sent product records
     */
    public void purgeSentProductRecords() {

        LocalDateTime purgeThreshold = LocalDateTime.now()
                .minusHours(retentionHours);

        logger.info(
                "Purge Sent Climate Product Record [" + retentionHours + "]");

        int purgedCount = 0;

        if (dao != null) {
            try {
                purgedCount = dao.purgeSentProductRecords(purgeThreshold);
            } catch (Exception e) {
                logger.error("Failed to execute purgeSentProductRecords", e);
            }
            if (purgedCount > 0) {
                this.sendPurgeNotifyMessage(purgedCount);
            }
            logger.info(
                    purgedCount + " sent climate product records were purged");
        }
    }

    /**
     * @return the dao
     */
    public ClimateProdSendRecordDAO getDao() {
        return dao;
    }

    /**
     * @param dao
     *            the dao to set
     */
    public void setDao(ClimateProdSendRecordDAO dao) {
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
     * Send alert viz message of purge.
     * 
     * @param count
     */
    public void sendPurgeNotifyMessage(int count) {
        String detailMsg = "PURGER=SENT_RECORD, PURGE_COUNT=" + count;

        StatusMessage sm = new StatusMessage();
        sm.setPriority(Priority.INFO);
        sm.setPlugin(ClimateMessageUtils.CPG_PLUGIN_ID);
        sm.setCategory(ClimateAlertUtils.CATEGORY_CLIMATE);
        sm.setMessage("Sent Product Record table was purged");
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

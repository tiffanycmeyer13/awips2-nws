package gov.noaa.nws.ocp.edex.plugin.atcf.util;

import java.util.Date;

import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.edex.core.EDEXUtil;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;

/**
 * Utility class to send notification to ATCF end point.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2019            pwang       Initial creation
 * Aug 28, 2019 67881      jwu         Send an AtcfDataChangeNotification
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */

public class NotifyUtil {

    public static final String A2ATCF_ENDPOINT = "a2atcfNotify";

    public static final String ATCF_PLUGIN_ID = "a2atcf";

    public static final String ATCF_CATEGORY = "ATCF";

    public static final String NOTIFY_SOURCE = "edex";

    /**
     * Send notification message to a2atcfNotify
     * 
     * @param desc
     * @param detailMsg
     * @throws Exception
     */
    public static void sendNotifyMessage(String desc, String detailMsg)
            throws Exception {

        StatusMessage sm = new StatusMessage();
        sm.setPriority(Priority.INFO);
        sm.setPlugin(ATCF_PLUGIN_ID);
        sm.setCategory(ATCF_CATEGORY);
        sm.setMessage(desc);
        sm.setMachineToCurrent();
        sm.setSourceKey(NOTIFY_SOURCE);
        sm.setDetails(detailMsg);
        sm.setEventTime(new Date());

        try {
            EDEXUtil.getMessageProducer().sendAsync(A2ATCF_ENDPOINT, sm);
        } catch (Exception e) {
            throw new Exception(
                    "Failed to send notification to the endpoint: a2atcfNotify",
                    e);
        }

    }

    /**
     * Send an AtcfDataChangeNotification to a2atcfNotify
     * 
     * @param desc
     * @param notification
     * @throws Exception
     */
    public static void sendNotifyMessage(
            AtcfDataChangeNotification notification) throws Exception {

        try {
            EDEXUtil.getMessageProducer().sendAsync(A2ATCF_ENDPOINT,
                    notification);
        } catch (Exception e) {
            throw new Exception(
                    "Error Sending AtcfDataChangeNotification to the endpoint: a2atcfNotify",
                    e);
        }
    }

}

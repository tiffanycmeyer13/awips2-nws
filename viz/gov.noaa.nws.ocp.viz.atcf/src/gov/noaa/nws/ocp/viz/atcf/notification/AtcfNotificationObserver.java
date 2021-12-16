/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.notification;

import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;

/**
 * AtcfNotificationObserver - Receives ATCF notification from EDEX and notify
 * registered listeners.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 19, 2019 65564      jwu         Initial creation
 * Aug 28, 2019 67881      jwu         Use AtcfDataChangeNotification
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class AtcfNotificationObserver implements INotificationObserver {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AtcfNotificationObserver.class);

    /**
     * Topic for ATCF notification message.
     */
    public static final String ATCF_NOTIFY_TOPIC = "edex.a2atcf.msg";

    /**
     * Instance.
     */
    private static AtcfNotificationObserver instance = null;

    /**
     * Constructor
     */
    private AtcfNotificationObserver() {
    }

    /**
     * Register the observer for receiving ATCF notification from edex
     */
    public static synchronized void register() {
        if (instance == null) {
            instance = new AtcfNotificationObserver();
        }

        NotificationManagerJob.addObserver(ATCF_NOTIFY_TOPIC, instance);
    }

    /**
     * Unregister the observer for receiving ATCF notification from edex
     */
    public static synchronized void unregister() {
        if (instance != null) {
            NotificationManagerJob.removeObserver(ATCF_NOTIFY_TOPIC, instance);
        }
    }

    /**
     * 
     * (non-Javadoc)
     *
     * @see com.raytheon.uf.common.jms.notification.INotificationObserver#
     *      notificationArrived
     *      com.raytheon.uf.common.jms.notification.NotificationMessage[])
     */
    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        for (NotificationMessage msg : messages) {
            try {
                Object ob = msg.getMessagePayload();
                if (ob instanceof AtcfDataChangeNotification) {
                    AtcfDataChangeNotification ntf = (AtcfDataChangeNotification) ob;
                    AtcfNotificationListeners.getInstance().notify(ntf);
                }
            } catch (NotificationException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "AtcfNotificationObserver: Could not send out notification to listeners.",
                        e);
            }
        }
    }

}
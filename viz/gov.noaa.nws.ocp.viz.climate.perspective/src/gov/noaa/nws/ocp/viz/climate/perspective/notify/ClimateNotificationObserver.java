/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective.notify;

import com.raytheon.uf.common.jms.notification.INotificationObserver;
import com.raytheon.uf.common.jms.notification.NotificationException;
import com.raytheon.uf.common.jms.notification.NotificationMessage;
import com.raytheon.uf.common.message.StatusMessage;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.notification.jobs.NotificationManagerJob;

/**
 * ClimateNotificationObserver
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2017            pwang     Initial creation
 * May 22, 2017            jwu       Some cleanup.
 *
 * </pre>
 *
 * @author pwang
 * @version 1.0
 */
public class ClimateNotificationObserver implements INotificationObserver {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ClimateNotificationObserver.class, "GDN_ADMIN",
                    "GDN_ADMIN");

    private static final String CLIMATE_NOTIFY_TOPIC = "edex.climate.msg";

    private static ClimateNotificationObserver instance = null;

    private ClimateNotificationObserver() {
    }

    /**
     * Register the climate notification observer for receiving climate messages
     * from edex
     */
    public static synchronized void registerClimateNotificationObserver() {
        if (instance == null) {
            instance = new ClimateNotificationObserver();
            NotificationManagerJob.addObserver(CLIMATE_NOTIFY_TOPIC, instance);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.raytheon.uf.common.jms.notification.INotificationObserver#
     * notificationArrived
     * (com.raytheon.uf.common.jms.notification.NotificationMessage[])
     */
    @Override
    public void notificationArrived(NotificationMessage[] messages) {
        StatusMessage ob = null;
        for (NotificationMessage message : messages) {
            try {
                ob = (StatusMessage) message.getMessagePayload();
                VizApp.runAsync(new ClimateAlertRun(ob));
            } catch (NotificationException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "ClimateNotificationObserver: Could not pass message from server.",
                        e);
            }
        }
    }

    private static class ClimateAlertRun implements Runnable {
        private StatusMessage sm;

        public ClimateAlertRun(StatusMessage sm) {
            this.sm = sm;
        }

        @Override
        public void run() {
            ClimateNotificationJob.getInstance().receive(sm);
        }
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.notification;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;

/**
 * AtcfNotificationListeners - a collection of IAtcfNotificationListener that
 * will be fired when an ATCF notification message from EDEX arrives.
 *
 * Note: These listeners are fired when AtcfNotificationObserver receives
 * notification. AtcfNotificationObserver is registered with
 * NotificationManagerJob when AtcfSidebar is created.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 28, 2019 67881      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 */
public class AtcfNotificationListeners {

    public static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(AtcfNotificationListeners.class);

    /**
     * Set of listeners
     */
    private Set<IAtcfNotificationListener> listeners;

    /**
     * Instance
     */
    private static AtcfNotificationListeners instance = null;

    /**
     * Constructor.
     */
    private AtcfNotificationListeners() {
        listeners = new CopyOnWriteArraySet<>();
    }

    /**
     * @return AtcfNotificationListeners
     */
    public static synchronized AtcfNotificationListeners getInstance() {
        if (instance == null) {
            instance = new AtcfNotificationListeners();
        }

        return instance;
    }

    /**
     * Adds a listener for getting notified when a notification is received from
     * the receiver.
     *
     * @param listener
     */
    public void addListener(IAtcfNotificationListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a listener for getting notified when a notification is received
     * from the receiver.
     *
     * @param listener
     */
    public void removeListener(IAtcfNotificationListener listener) {
        if (listeners != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Handle received notification - simply pass onto
     * IAtcfNotificationListener.
     *
     * @param sm
     */
    public void notify(AtcfDataChangeNotification notification) {
        if (notification != null) {
            for (IAtcfNotificationListener listener : listeners) {
                listener.notificationArrived(notification);
            }
        }
    }

}
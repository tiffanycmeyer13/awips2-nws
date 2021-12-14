/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT.
 */
package gov.noaa.nws.ocp.viz.atcf.notification;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDataChangeNotification;

/**
 * Interface to be implemented by an Atcf Session or dialogs to handle
 * notification from data changes in ATCF baseline tables resulting from user
 * editing.
 *
 * <pre>
 *     An entity implementing this interface can be added as:
 *        AtcfNotificationListeners.addListner(this);
 * </pre>
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 28, 2019 67881      jwu         Initial creation
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public interface IAtcfNotificationListener {

    /**
     * Listener to handle received ATCF notification about baseline data
     * changes.
     *
     * @param AtcfDataChangeNotification
     */
    public void notificationArrived(AtcfDataChangeNotification notification);
}
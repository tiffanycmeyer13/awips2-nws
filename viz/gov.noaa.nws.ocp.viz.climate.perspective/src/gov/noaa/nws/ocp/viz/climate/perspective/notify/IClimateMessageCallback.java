/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT.
 */
package gov.noaa.nws.ocp.viz.climate.perspective.notify;

import com.raytheon.uf.common.message.StatusMessage;

/**
 * Callback to tie into Climate Product Generation View to of handle newly
 * arrived session messages.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2017 #27199     jwu     Initial creation
 * 
 * </pre>
 * 
 * @author jwu
 * @version 1.0
 */
public interface IClimateMessageCallback {

    /**
     * Call back with CPG messages as StatusMessage.
     * 
     * @param statusMessage
     */
    public void messageArrived(StatusMessage statusMessage);
}

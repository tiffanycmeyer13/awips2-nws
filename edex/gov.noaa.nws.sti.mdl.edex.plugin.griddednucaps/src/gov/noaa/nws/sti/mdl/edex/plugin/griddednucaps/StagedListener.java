package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

/**
 * Interface to be notified when event has been fired.
 *
 * <pre>
*
* SOFTWARE HISTORY
*
* Date          Ticket#  Engineer  Description
* ------------- -------- --------- -----------------
 * Oct 20, 2018   DCS-18691 jburks  Initial creation
 *
 * </pre>
 *
 * @author jburks
 */
public interface StagedListener {

    /**
     * Notify event when the condition has been satisfied.
     */
    public void notifyEvent();
}

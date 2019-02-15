package gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Timer that will send message at specific interval, and during downtime can be
 * turned off.
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
public class TimedChecker {

    /** The timer thread. */
    Timer timer = new Timer();

    /** The task that fires back a notification to the listener. */
    TimerTask task;

    /** The listener to notify time has passed. */
    StagedListener listener;

    /** The running state variable */
    boolean running = false;

    /** The delay for periodic firing in milliseconds. */
    long delay;

    /**
     * Instantiates a new timed checker.
     *
     * @param listener
     *            the listener to be notified
     * @param delay
     *            the delay to periodically notify
     */
    public TimedChecker(StagedListener listener, long delay) {
        this.listener = listener;
        this.delay = delay;

    }

    /**
     * Stop the thread and task.
     */
    public void stop() {
        if (running) {
            if (timer != null)
                timer.cancel();
            timer = null;
            if (task != null)
                task.cancel();
            task = null;
            running = false;
        }
    }

    /**
     * Start the timer and task.
     */
    public void start() {
        if (!running) {
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    fire();
                }
            };
            timer.scheduleAtFixedRate(task, delay, delay);
            running = true;
        }
    }

    /**
     * Fire and notify the listener.
     */
    public void fire() {
        listener.notifyEvent();
    }

    /**
     * Checks if is running.
     *
     * @return true, if is running
     */
    public boolean isRunning() {
        return running;
    }

}

/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.review;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Action corresponding to the NWWS Review Climate Dialog.
 * 
 * Note: To bring up the legacy browser, two environment variables need to be
 * defined: FXA_HOME & FXA_LOCAL_SITE. Normally, FXA_HOME points to the
 * directory (e.g., /awips/fxa), which has a "bin" sub-directory containing a
 * soft link named "NWRBrowser" should point to where "AWIPS2-browser.tcl" is
 * installed. FXA_LOCAL_SITE is the local site ID (e.g., OAX).
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 27 FEB 2017   28039     astrakovsky  Initial Creation.
 * 11 APR 2017   27199     jwu          Passed in session ID.
 * 27 APR 2017   27199     jwu          Invoke legacy browser for NWR products.
 * 26 SEP 2017   33520     amoore       Remove NWR Review dialog (just Legacy will be used).
 * </pre>
 * 
 * @author astrakovsky
 */

public class NWRClimateReviewAction extends AbstractHandler {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(NWRClimateReviewAction.class);

    /**
     * Path to legacy NWRBrowser.
     */
    private static final String FXA_HOME = "FXA_HOME";

    /**
     * NWRBrowser.
     */
    private static final String NWR_BROWSER = "/bin/NWRBrowser";

    /**
     * FXA_LOCAL_SITE to run NWRBrowser.
     */
    private static final String FXA_LOCAL_SITE = "FXA_LOCAL_SITE";

    /**
     * Key to retrieve input parameters.
     */
    private static final String SESSION_ID = "sessionID";

    /**
     * Executes with the map of parameter values by name.
     *
     * @param event
     *            An event containing all the information about the current
     *            state of the application; must not be <code>null</code>.
     * @return the result of the execution. Reserved for future use, must be
     *         <code>null</code>.
     * @throws ExecutionException
     *             if an exception occurred during execution.
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Get session ID to feed into the dialog constructor.
        String sid = event.getParameter(SESSION_ID);
        if (sid == null || sid.isEmpty()) {
            logger.error(
                    "NWRClimateReviewAction: No CPG Session ID found, exiting....");
            return null;
        }

        // Open the dialog.
        // Make sure both $FXA_HOME and $FXA_LOCAL_SITE have been set.
        String path = System.getenv(FXA_HOME);
        if (path == null) {
            logger.error(
                    "NWRClimateReviewAction: FXA_HOME is not defined, exiting....");
            return null;
        }

        String localSite = System.getenv(FXA_LOCAL_SITE);
        if (localSite == null) {
            logger.error(
                    "NWRClimateReviewAction: FXA_LOCAL_SITE is not defined, exiting....");
            return null;
        }

        String command = path + NWR_BROWSER;
        try {
            Runtime.getRuntime().exec("sh " + command);
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                logger.error("NWRClimateReviewAction: " + command
                        + " does not exist, exiting....");
            } else {
                logger.error("NWRClimateReviewAction: Failed to invoke "
                        + command + ", exiting....");
            }
        }

        return null;
    }
}
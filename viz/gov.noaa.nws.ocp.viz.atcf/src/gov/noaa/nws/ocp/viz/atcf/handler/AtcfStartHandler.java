/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Handler to start ATCF.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 04, 2018 #34955      jwu         Initial creation.
 * Jun 28, 2018 #51961      jwu         Test drawing B-Deck
 * Jul 30, 2019 66618       dfriedman   Remove embedded menu bar.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */

public class AtcfStartHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        // Activate ATCF context
        AtcfVizUtil.activateAtcfContext();

        // Start ATCF session
        AtcfSession.getInstance().start();

        return null;
    }

}

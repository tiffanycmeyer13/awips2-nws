/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.ImportFixesRequest;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Implementation for Fixes=>Import Fixes.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2020 81449      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ImportFixesHandler extends AbstractAtcfTool {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ImportFixesHandler.class);

    @Override
    protected void executeEvent(ExecutionEvent event) {
        // Sanity check - the storm should have been selected at this point.
        Storm storm = AtcfSession.getInstance().getActiveStorm();
        if (storm == null) {
            logger.error("ImportFixesHandler: No storm is selected yet.");
            return;
        }

        String fixType = event.getParameter("fixType");

        ImportFixesRequest req = new ImportFixesRequest(storm, fixType);

        try {
            ThriftClient.sendRequest(req);
        } catch (Exception e) {
            logger.warn("ImportFixesHandler- No fix data have been imported.",
                    e);
        }

    }

}
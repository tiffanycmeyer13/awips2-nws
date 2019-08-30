/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.perspectives.AbstractVizPerspectiveManager;

import gov.noaa.nws.ocp.viz.climate.perspective.notify.ClimateNotificationJob;
import gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateView;

/**
 * Climate perspective implementation.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 04, 2016 20744      wpaintsil   Initial creation
 * Aug 17, 2016 20744      wpaintsil   Keyboard shortcuts fix
 * May 04, 2017 33534      jwu         Implement ClimateView to show sent products.
 * May 03, 2018 20711      amoore      Close listener should only apply to Climate
 *                                     perspective.
 * Aug 05, 2019 DR 21509   dfriedman   Hide the editor area.
 * </pre>
 * 
 * @author wpaintsil
 */

public class ClimatePerspectiveManager extends AbstractVizPerspectiveManager {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(ClimatePerspectiveManager.class);

    @Override
    protected void open() {
        /*
         * This is specified on the layout in
         * ClimatePerspective.createInitialLayout(), but it is apparently
         * necessary to set it here.
         */
        page.setEditorAreaVisible(false);

        /*
         * Keyboard shortcuts are inactive until focus is on a control within a
         * view or editor within the perspective. When switching back and forth
         * between Climate and other perspectives, focus is lost.
         *
         * Add a perspective listener to automatically set focus when returning
         * to the Climate perspective, and to cancel notification jobs when the
         * perspective is closed.
         */
        perspectiveWindow.addPerspectiveListener(new PerspectiveAdapter() {
            @Override
            public void perspectiveActivated(IWorkbenchPage page,
                    IPerspectiveDescriptor perspective) {
                if (perspective.getId().compareTo(
                        "gov.noaa.nws.ocp.viz.climate.perspective.ClimatePerspective") == 0) {
                    try {
                        page.showView(ClimateView.ID);
                    } catch (PartInitException e) {
                        logger.error("Could not show ClimateView.", e);
                    }
                }
            }

            @Override
            public void perspectiveClosed(IWorkbenchPage page,
                    IPerspectiveDescriptor perspective) {
                if (perspective.getId().compareTo(
                        "gov.noaa.nws.ocp.viz.climate.perspective.ClimatePerspective") == 0) {
                    logger.debug("Closing Climate Perspective");
                    ClimateNotificationJob.getInstance().cancel();
                    perspectiveWindow.removePerspectiveListener(this);
                }
            }
        });
    }
}
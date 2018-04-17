/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.climate.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateProdGenerationView;
import gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateProductSentViewer;
import gov.noaa.nws.ocp.viz.climate.perspective.views.ClimateView;

/**
 * CAVE Perspective for Climate.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer    Description
 * ------------  ---------- ----------- --------------------------
 * Aug 03, 2016  20744      wpaintsil   Initial creation
 * Mar 01, 2017  27199      jwu         Add product generation view.
 * May 04, 2017  33534      jwu         Added ClimateProductSentViewer.
 * 
 * </pre>
 * 
 * @author wpaintsil
 */

public class ClimatePerspective implements IPerspectiveFactory {

    @Override
    public void createInitialLayout(IPageLayout layout) {

        String editorArea = layout.getEditorArea();
        layout.setFixed(false);

        layout.setEditorAreaVisible(false);

        /*
         * Climate view at left to show products sent already.
         */
        layout.addView(ClimateView.ID, IPageLayout.LEFT, (float) 0.25,
                editorArea);
        layout.getViewLayout(ClimateView.ID).setCloseable(false);

        /*
         * One folder to hold Climate product generation view and
         * ClimateProductSentViewer at right side.
         */
        IFolderLayout rightFolder = layout.createFolder("Right",
                IPageLayout.RIGHT, .73F, editorArea);
        rightFolder.addView(ClimateProdGenerationView.ID);
        layout.getViewLayout(ClimateProdGenerationView.ID).setCloseable(false);

        rightFolder.addView(ClimateProductSentViewer.ID);
        layout.getViewLayout(ClimateProductSentViewer.ID).setCloseable(false);

    }

}
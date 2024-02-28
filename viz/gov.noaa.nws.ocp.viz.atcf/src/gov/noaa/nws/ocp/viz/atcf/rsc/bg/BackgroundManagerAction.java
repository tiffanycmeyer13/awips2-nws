/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.rsc.bg;

import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;

import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;

/**
 * Right-click legend menu action for ATCF resource to display background
 * manager dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Feb 22, 2021 87890       dfriedman   Initial creation
 *
 * </pre>
 *
 * @author dfriedman
 */
public class BackgroundManagerAction extends AbstractRightClickAction {

    @Override
    public String getText() {
        return "ATCF Background...";
    }

    @Override
    public void run() {
        if (selectedRsc != null) {
            AbstractVizResource<?, ?> resource = selectedRsc.getResource();
            if (resource instanceof AtcfResource) {
                ((AtcfResource) resource).getBackgroundManager().showDialog();
            }
        }
    }
}

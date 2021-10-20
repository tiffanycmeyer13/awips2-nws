package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AdvisoryType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.advisory.AdvisoryUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.textedit.AtcfTextEditing;

/**
 * Base command handler for editing ATCF text products.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 22, 2021 96311      dfriedman   Initial version, refactored
 *                                     from various handler classes.
 * </pre>
 *
 * @version 1.0
 *
 */

public abstract class BaseEditProductHandler extends AbstractAtcfTool {

    @Override
    public void executeEvent(ExecutionEvent event) {
        AdvisoryType advisoryType = getAdvisoryType();
        Storm storm = AtcfSession.getInstance().getActiveStorm();

        // Use lower-case id to match legacy file name.
        String stormId = storm.getStormId().toLowerCase();

        String tcp = AdvisoryUtil.readAdvisory(stormId, advisoryType);

        if (tcp == null || tcp.isEmpty()) {
            Shell sh = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageDialog.openInformation(sh, "Edit " + advisoryType.getName(),
                    String.format("No %s has been generated yet for storm %s.\n"
                            + "\nPlease generate via Advisory=>Advisory Composition.",
                            getProductDescription(), stormId));
        } else {
            AtcfTextEditing.editProduct(stormId, tcp, advisoryType);
        }
    }

    protected abstract AdvisoryType getAdvisoryType();

    /**
     * @return description to be used in messages; defaults to
     * {@code getAdvisoryType().getName()}.
     */
    protected String getProductDescription() {
        return getAdvisoryType().getName();
    }
}

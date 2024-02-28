/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.tools.AbstractTool;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.ChooseStormDialog;

/**
 * Abstract tool for Atcf Data displaying and processing
 *
 * Most of cases, the "Choose Storm dialog" needs to be opened first if no storm
 * has been selected/set as the current storm yet. So this action will be always
 * checked here and be executed first if needed. Other Atcf tools can extend
 * this class and implement executeEvent() method for any subsequent actions.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 24, 2018 52658      jwu         Initial creation.
 * Dec 07, 2018 57484      jwu         Retrieve command's name from event.
 * Mar 22, 2019 61613      jwu         Add "selectStorm" flag to indicate
 *                                     a storm is selected via "Choose A
 *                                     Storm Dialog".
 * Mar 28, 2019 61882      jwu         Store command name from sidebar.
 * Apr 10, 2018 62427      jwu         Distinguish menu/sidebar commands.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public abstract class AbstractAtcfTool extends AbstractTool {

    private static ChooseStormDialog chooseStormDialog;

    // Command name issued from ATCF menu
    protected String commandName = "";

    // Command name issued from ATCF sidebar
    protected String sidebarCmdName = "";

    // Flag to show a storm needs to be selected.
    protected boolean stormSelected = true;

    /**
     * Open the Choose Storm Dialog.
     *
     * @param event
     *            ExecutionEvent
     *
     * @throws NotDefinedException
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        super.execute(event);

        /*
         * Get the command name. This could be used later to identify different
         * commands activated from menu but handled by the same class for
         * sidebar commands.
         */
        try {
            commandName = event.getCommand().getName();
        } catch (NotDefinedException e) {
            throw new ExecutionException("Unexpected unnamed command", e);
        }

        /*
         * Get the actual sidebar command name (may not be the command alias
         * shown in sidebar). This could be used later to identify different
         * commands activated from sidebar but handled by the same class and
         * handler.
         */
        sidebarCmdName = event.getParameter(AtcfVizUtil.SIDEBAR_COMMAND_NAME);

        stormSelected = false;

        if (AtcfSession.getInstance().getActiveStorm() == null) {
            openChooseStormDlg(event);
        } else {
            executeEvent(event);
        }

        return null;
    }

    /**
     * Open the Choose Storm dialog.
     *
     * @param event
     *            ExecutionEvent
     *
     */
    private void openChooseStormDlg(ExecutionEvent event) {

        if (chooseStormDialog == null
                || chooseStormDialog.isDisposed()) {
            createChooseStormsDialog();
            chooseStormDialog.setOkSelectedHandler(() -> {
                stormSelected = true;
                executeEvent(event);
            });
        }
        chooseStormDialog.open();
    }

    /**
     * Implement this method to handle subsequent event once an active storm has
     * been chosen.
     *
     * @param event
     *            ExecutionEvent
     */
    protected abstract void executeEvent(ExecutionEvent event);

    /*
     * Create a ChooseStormDialog
     */
    private static void createChooseStormsDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        chooseStormDialog = new ChooseStormDialog(shell);
        chooseStormDialog.addCloseCallback(ov -> chooseStormDialog = null);
    }

    protected static void warnNoBestTrack(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        MessageDialog.openWarning(shell, "Forecast Track - No Best Track",
                "No Best Track being started yet for storm "
                        + storm.getStormId());
    }

    protected static void warnNoForecast(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        MessageDialog.openWarning(shell, "Forecast Intensity - No Forecast",
                "No forecast has been started yet for storm "
                        + storm.getStormId()
                        + ".\n You must first make a track forecast for this storm.");
    }

}
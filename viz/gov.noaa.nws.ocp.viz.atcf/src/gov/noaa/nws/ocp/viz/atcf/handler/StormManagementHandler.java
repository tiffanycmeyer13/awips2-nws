/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.Genesis;
import gov.noaa.nws.ocp.common.dataplugin.atcf.GenesisState;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.UpdateGenesisRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.CopyStormDialog;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.DeleteStormDialog;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.EndStormDialog;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.RestartStormDialog;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.SpawnInvestDialog;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.StormManagementDialog;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.StormManagementDialog.StormManagementDialogStatusType;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.StormManagementType;
import gov.noaa.nws.ocp.viz.atcf.main.managestorms.UpdateStormDialog;

/**
 * Provide the ability to implement various versions of the same Storm
 * Management dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 06, 2018 50987      wpaintsil   Initial creation.
 * Aug 10, 2020 79571      wpaintsil   Implement Genesis backend.
 * Oct 28, 2020 82623      jwu         Implement storm GUI & action.
 * Dec 17, 2020 86027      jwu         Update for End/Restart genesis.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class StormManagementHandler extends AbstractHandler {

    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AtcfDataUtil.class);

    private static StormManagementDialog stormManagementDialog;

    // Dialogs for genesis management
    private static SpawnInvestDialog spawnInvestDialog;

    // Dialogs for storm management
    private static CopyStormDialog copyStormDialog;

    private static RestartStormDialog restartStormDialog;

    private static UpdateStormDialog updateStormDialog;

    private static EndStormDialog endStormDialog;

    private static DeleteStormDialog deleteStormDialog;

    // Type for storm management
    private StormManagementType selection;

    private Genesis currentGenesis = new Genesis();

    private Storm currentStorm;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        selection = StormManagementType
                .getStormDialogType(event.getParameter("menuSelection"));

        openStormManagementDlg(selection.getDialogTitle());

        return null;
    }

    /*
     * Open one of several Storm Management dialogs.
     *
     * @param title
     */
    private void openStormManagementDlg(String title) {

        List<?> dataList = getDataList(selection);

        if (stormManagementDialog == null
                || stormManagementDialog.isDisposed()) {

            createStormManagementDialog(title, dataList);
            addStormManagementCloseCallback();
        } else {
            stormManagementDialog.refreshDataList(dataList);
        }
        stormManagementDialog.open();
    }

    /*
     * Create a StormManagement dialog
     *
     * @param title
     *
     * @param dataList
     */
    private static synchronized void createStormManagementDialog(String title,
            List<?> dataList) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        stormManagementDialog = new StormManagementDialog(shell, title,
                dataList);
    }

    /*
     *
     */
    private void addStormManagementCloseCallback() {
        stormManagementDialog.addCloseCallback(ov -> {
            StormManagementDialogStatusType status = stormManagementDialog
                    .getWindowStatus();

            currentGenesis = stormManagementDialog.getCurrentGenesis();
            currentStorm = stormManagementDialog.getCurrentStorm();

            removeStormManagementDialog();

            if (status == StormManagementDialogStatusType.OK) {
                if (selection == StormManagementType.SPAWN_INVEST) {
                    openSpawnInvestDlg();
                } else if (selection == StormManagementType.END_GENESIS) {
                    openEndGenesisDlg();
                } else if (selection == StormManagementType.RESTART_GENESIS) {
                    openRestartGenesisDlg();
                } else if (selection == StormManagementType.RESTART_STORM) {
                    openRestartStormDlg();
                } else if (selection == StormManagementType.END_STORM) {
                    openEndStormDlg();
                } else if (selection == StormManagementType.DELETE_STORM) {
                    openDeleteStormDlg();
                } else if (selection == StormManagementType.COPY_STORM) {
                    openCopyStormDlg();
                } else if (selection == StormManagementType.RENUMBER_STORM
                        || selection == StormManagementType.CORRECT_STORM
                        || selection == StormManagementType.NAME_EXISTING) {
                    openUpdateStormDlg(selection.getDialogTitle());
                }
            }
        });
    }

    /*
     * Remove Storm Management dialog
     */
    private static synchronized void removeStormManagementDialog() {
        stormManagementDialog = null;
    }

    /*
     * Load genesis or storm list.
     *
     * Geneses that have ended or become invests will not be included in genesis
     * lists for Spawn an Invest or Restart a enesis.
     */
    private List<?> getDataList(StormManagementType selection) {

        List<?> dataList;
        if (selection == StormManagementType.SPAWN_INVEST
                || selection == StormManagementType.END_GENESIS) {
            dataList = AtcfDataUtil.getActiveGenesis();
        } else if (selection == StormManagementType.RESTART_GENESIS) {
            dataList = AtcfDataUtil.getRestartableGenesis();
        } else if (selection == StormManagementType.RESTART_STORM) {
            dataList = AtcfDataUtil.getRestartableStorms();
        } else if (selection == StormManagementType.COPY_STORM) {
            dataList = AtcfDataUtil.getStormList();
        } else if (selection == StormManagementType.END_STORM) {
            dataList = AtcfDataUtil.getActiveStorms();
        } else if (selection == StormManagementType.DELETE_STORM) {
            dataList = AtcfDataUtil.getDeletableStorms();
        } else {
            dataList = AtcfDataUtil.getUpdatableStorms();
        }

        return dataList;
    }

    /*
     * Open Spawn Invest dialog
     */
    private void openSpawnInvestDlg() {
        if (spawnInvestDialog == null
                || spawnInvestDialog.isDisposed()) {
            createSpawnInvestDialog(currentGenesis);
        }
        spawnInvestDialog.open();
    }

    /*
     * Create a Spawn Invest dialog
     *
     * @param gen Genesis
     */
    private static synchronized void createSpawnInvestDialog(Genesis gen) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        spawnInvestDialog = new SpawnInvestDialog(shell, gen);
        spawnInvestDialog.addCloseCallback(ov -> spawnInvestDialog = null);
    }

    /*
     * Open End Genesis dialog
     */
    private void openEndGenesisDlg() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        if (currentGenesis != null) {
            MessageDialog dialog = new MessageDialog(shell, "End Genesis?",
                    null, getGensisInfoString(currentGenesis),
                    MessageDialog.QUESTION, new String[] { "Ok", "Cancel" }, 0);

            if (dialog.open() == Window.OK) {
                endRestartGenesis(currentGenesis, true);
            }
        }
    }

    /*
     * Open Restart Genesis dialog
     */
    private void openRestartGenesisDlg() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();

        if (currentGenesis != null) {
            MessageDialog dialog = new MessageDialog(shell, "Restart Genesis?",
                    null, getGensisInfoString(currentGenesis),
                    MessageDialog.QUESTION, new String[] { "Ok", "Cancel" }, 0);
            if (dialog.open() == Window.OK) {
                endRestartGenesis(currentGenesis, false);
            }
        }
    }

    /*
     * Construct an info string for a genesis
     *
     * @param gen Genesis
     */
    private String getGensisInfoString(Genesis gen) {
        StringBuilder sbd = new StringBuilder();
        sbd.append(String.format("%-8s%-12s%-12s%-20s", "#", "Year", "Basin",
                "Genesis Name"));
        sbd.append("\n");
        sbd.append(String.format("%-8d%-12d%-12s%-20s", gen.getGenesisNum(),
                gen.getYear(), gen.getRegion(), gen.getGenesisName()));

        return sbd.toString();
    }

    /*
     * End or Restart a Genesis
     *
     * @param genesis
     *
     * @param end true if ending a genesis, false if restarting a genesis
     */
    private void endRestartGenesis(Genesis genesis, boolean end) {
        if (end) {
            // Set an end time for the genesis.
            currentGenesis.setEndDTG(TimeUtil.newGmtCalendar());
            currentGenesis.setGenesisState(GenesisState.END.toString());
        } else {
            // remove the end time for a genesis
            genesis.setEndDTG(null);
            currentGenesis.setGenesisState(GenesisState.GENESIS.toString());
        }

        UpdateGenesisRequest updateRequest = new UpdateGenesisRequest();
        updateRequest.setGenesis(genesis);

        // update that genesis.
        try {
            ThriftClient.sendRequest(updateRequest);

        } catch (VizException e) {
            logger.warn("StormManagementHandler - Failed to "
                    + (end ? "end" : "restart")
                    + " the storm genesis with name and Id "
                    + updateRequest.getGenesis().getGenesisName() + ", "
                    + updateRequest.getGenesis().getGenesisId(), e);
        }
    }

    /*
     * Open "End a Storm" dialog
     */
    private void openEndStormDlg() {
        if (currentStorm != null) {
            if (endStormDialog == null
                    || endStormDialog.isDisposed()) {
                createEndStormDialog(currentStorm);
            }
            endStormDialog.open();
        }
    }

    /*
     * Create an End Storm dialog
     *
     * @param storm Storm
     */
    private static synchronized void createEndStormDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        endStormDialog = new EndStormDialog(shell, storm);
        endStormDialog.addCloseCallback(ov -> endStormDialog = null);
    }

    /*
     * Open "Restart a Storm" dialog
     */
    private void openRestartStormDlg() {
        if (currentStorm != null) {
            if (restartStormDialog == null
                    || restartStormDialog.isDisposed()) {
                createRestartStormDialog(currentStorm);
            }
            restartStormDialog.open();
        }
    }

    /*
     * Create a Restart Storm dialog
     *
     * @param storm Storm
     */
    private static synchronized void createRestartStormDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        restartStormDialog = new RestartStormDialog(shell, storm);
        restartStormDialog.addCloseCallback(ov -> restartStormDialog = null);
    }

    /*
     * Open "Delete a Storm" dialog
     */
    private void openDeleteStormDlg() {
        if (currentStorm != null) {
            if (deleteStormDialog == null
                    || deleteStormDialog.isDisposed()) {
                createDeleteStormDialog(currentStorm);
            }
            deleteStormDialog.open();
        }
    }

    /*
     * Create a Delete Storm dialog
     *
     * @param storm Storm
     */
    private static synchronized void createDeleteStormDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        deleteStormDialog = new DeleteStormDialog(shell, storm);
        deleteStormDialog.addCloseCallback(ov -> deleteStormDialog = null);
    }

    /*
     * Open "Renumber/Rename/Correct a Storm" dialog
     */
    private void openUpdateStormDlg(String title) {
        if (currentStorm != null) {
            if (updateStormDialog == null
                    || deleteStormDialog.isDisposed()) {
                createUpdateStormDialog(currentStorm, title);
            }
            updateStormDialog.open();
        }
    }

    /*
     * Create an Update Storm dialog
     *
     * @param storm Storm
     *
     * @param title
     */
    private static synchronized void createUpdateStormDialog(Storm storm,
            String title) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        updateStormDialog = new UpdateStormDialog(shell, storm, title);
        updateStormDialog.addCloseCallback(ov -> updateStormDialog = null);
    }

    /*
     * Open "Copy a Storm" dialog
     */
    private void openCopyStormDlg() {
        if (currentStorm != null) {
            if (copyStormDialog == null
                    || copyStormDialog.isDisposed()) {

                createCopyStormDialog(currentStorm);
            }
            copyStormDialog.open();
        }
    }

    /*
     * Create a Copy Storm dialog
     *
     * @param storm Storm
     */
    private static synchronized void createCopyStormDialog(Storm storm) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        copyStormDialog = new CopyStormDialog(shell, storm);
        copyStormDialog.addCloseCallback(ov -> copyStormDialog = null);
    }

}

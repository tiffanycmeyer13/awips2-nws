/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.tools.AbstractModalTool;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.ChooseStormDialog;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.track.ReBestTrackDialog;

/**
 * Tool for ATCF interactive best track editing (Track=>ReBest)
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 22, 2020 79573      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ReBestTrackTool extends AbstractModalTool {

    // Dialogs
    private static ChooseStormDialog chooseStormDialog;

    private static ReBestTrackDialog reBestTrackDlg;

    // Flag to show a storm needs to be selected.
    protected boolean selectStorm = true;

    // Editor.
    protected AbstractEditor mapEditor = null;

    // AtcfResource.
    protected AtcfResource drawingLayer;

    // Mouse handler.
    protected IInputHandler inputHandler = null;

    /*
     * (non-Javadoc)
     *
     * @see com.raytheon.viz.ui.tools.AbstractMadalTool#activateTool()
     */
    @Override
    protected void activateTool() {

        if (editor instanceof AbstractEditor) {
            this.mapEditor = (AbstractEditor) super.editor;
        }

        // Get Atcf Resource
        drawingLayer = AtcfSession.getInstance().getAtcfResource();

        /*
         * Unload appropriate input handler
         */
        if (this.inputHandler != null) {
            mapEditor.unregisterMouseHandler(this.inputHandler);
        }

        // Turn off, so tool doesn't exhibit toggle behavior
        setEnabled(false);

        // Open dialog.
        selectStorm = false;

        if (AtcfSession.getInstance().getActiveStorm() == null) {
            openChooseStormDlg();
        } else {
            openReBestTrackDlg();
        }

    }

    /**
     * Clean up: remove ghost line and handle bars.
     */
    @Override
    public void deactivateTool() {

        if (drawingLayer != null) {
            drawingLayer.removeGhost();
            mapEditor.refresh();
        }

        if (mapEditor != null && this.inputHandler != null) {
            mapEditor.unregisterMouseHandler(this.inputHandler);
        }

        this.inputHandler = null;

        if (chooseStormDialog != null) {
            chooseStormDialog.close();
        }

        if (reBestTrackDlg != null) {
            reBestTrackDlg.close();
        }

    }

    /**
     * Re-activate the tool with a new handler.
     */
    public void reactivateTool() {

        // Register mouse handler.
        this.inputHandler = getMouseHandler();
        if (this.inputHandler != null) {
            mapEditor.registerMouseHandler(this.inputHandler);
            AtcfSession.getInstance().setAtcfTool(this);
        }
    }

    /**
     * Remove handler.
     */
    public void removeTool() {

        if (mapEditor != null && this.inputHandler != null) {
            mapEditor.unregisterMouseHandler(this.inputHandler);
        }

        this.inputHandler = null;
    }

    /**
     * Refresh ghost.
     */
    public void refreshGhost() {
        if (mapEditor != null && this.inputHandler != null) {
            ((ReBestTrackHandler) inputHandler).redrawGhost();
        }
    }

    /*
     * Open a Choose Storm dialog.
     */
    private void openChooseStormDlg() {
        if (chooseStormDialog == null
                || chooseStormDialog.isDisposed()) {
            createChooseStormDialog();
            chooseStormDialog.setOkSelectedHandler(() -> {
                selectStorm = true;
                openReBestTrackDlg();
            });
        }
        chooseStormDialog.open();
    }

    /*
     * Create a "Choose Storm" dialog
     */
    private static synchronized void createChooseStormDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        chooseStormDialog = new ChooseStormDialog(shell);
        chooseStormDialog.addCloseCallback(ov -> chooseStormDialog = null);
    }

    /**
     * Open the "ReBest Track" dialog.
     */
    protected void openReBestTrackDlg() {

        // Retrieve B-Deck data for current storm.
        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        // Get B-Deck records.
        Map<String, List<BDeckRecord>> bdeckDataMap = AtcfDataUtil
                .getBDeckRecords(currentStorm, true);

        // If no best track yet, warn the user.
        if (bdeckDataMap == null || bdeckDataMap.isEmpty()) {
            AbstractAtcfTool.warnNoBestTrack(currentStorm);
            return;
        }

        // Bring up dialog.
        if (reBestTrackDlg == null || reBestTrackDlg.isDisposed()) {
            createRebestTrackDialog(currentStorm, bdeckDataMap);
            reBestTrackDlg.setReBestTrackTool(this);

        }
        reBestTrackDlg.open();

        // Create mouse handler - register/activate when "ReBest" is clicked.
        this.inputHandler = getMouseHandler();
        if (this.inputHandler != null) {
            AtcfSession.getInstance().setAtcfTool(this);
        }
    }

    /*
     * Create a "ReBest Track" dialog
     *
     * @param storm
     *
     * @param bdeckDataMap
     */
    private static synchronized void createRebestTrackDialog(Storm storm,
            Map<String, List<BDeckRecord>> bdeckDataMap) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        reBestTrackDlg = new ReBestTrackDialog(shell, storm, bdeckDataMap);
        reBestTrackDlg.addCloseCallback(ov -> {
            reBestTrackDlg.deactivateTool();
            reBestTrackDlg = null;
        });
    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    public IInputHandler getMouseHandler() {

        if (this.inputHandler == null) {
            this.inputHandler = new ReBestTrackHandler(this);
        } else {
            ReBestTrackHandler handler = (ReBestTrackHandler) inputHandler;
            if (handler.getDrawingLayer() != drawingLayer
                    || handler.getCurStorm() != AtcfSession.getInstance()
                            .getActiveStorm()
                    || !handler.getSelectedDTG()
                            .equals(reBestTrackDlg.getSelectedDTG())) {
                mapEditor.unregisterMouseHandler(handler);

                this.inputHandler = new ReBestTrackHandler(this);
            }
        }

        return this.inputHandler;
    }

    /**
     * @return the reBestTrackDlg
     */
    public static ReBestTrackDialog getReBestTrackDlg() {
        return reBestTrackDlg;
    }

    /**
     * @return the mapEditor
     */
    public AbstractEditor getMapEditor() {
        return mapEditor;
    }

    /**
     * @return the drawingLayer
     */
    public AtcfResource getDrawingLayer() {
        return drawingLayer;
    }

}
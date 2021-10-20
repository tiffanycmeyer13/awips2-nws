/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.raytheon.viz.ui.tools.AbstractModalTool;

import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.forecast.ForecastTrackDialog;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.ChooseStormDialog;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;

/**
 * Tool for ATCF interactive track forecast (Forecast=>Forecast Track)
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 12, 2019 71722      jwu         Initial creation.
 * Jun 10, 2020 78027      jwu         Load forecast track data.
 * May 27, 2021 91757      jwu         Revise mouse actions on NHC feedback.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ForecastTrackTool extends AbstractModalTool {

    // Dialogs
    private static ChooseStormDialog chooseStormDialog;

    private static ForecastTrackDialog forecastTrackDlg;

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
            openForecastTrackDlg();
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

        if (forecastTrackDlg != null) {
            forecastTrackDlg.close();
        }

    }

    /*
     * Open the Choose Storm dialog.
     */
    private void openChooseStormDlg() {
        if (chooseStormDialog == null
                || chooseStormDialog.isDisposed()) {
            createChooseStormDialog();
            chooseStormDialog.setOkSelectedHandler(() -> {
                selectStorm = true;
                openForecastTrackDlg();
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
     * Open the "Forecast Track" dialog.
     */
    protected void openForecastTrackDlg() {

        // Retrieve B-Deck data for current storm.
        Storm currentStorm = AtcfSession.getInstance().getActiveStorm();

        Map<String, List<BDeckRecord>> currentBDeckRecords = AtcfDataUtil
                .getBDeckRecords(currentStorm, false);

        // If no best track yet, warn the user.
        if (currentBDeckRecords == null || currentBDeckRecords.isEmpty()) {
            AbstractAtcfTool.warnNoBestTrack(currentStorm);
            return;
        }

        // Check track forecast.
        Map<String, List<ForecastTrackRecord>> fcstTrackData = AtcfDataUtil
                .getFcstTrackRecords(currentStorm, true);

        if (fcstTrackData == null) {
            fcstTrackData = new LinkedHashMap<>();
        }

        // Bring up dialog.
        if (forecastTrackDlg == null
                || forecastTrackDlg.isDisposed()) {

            createForecastTrackDialog(currentStorm, fcstTrackData);
            forecastTrackDlg.setFcstTrackTool(this);

        }
        forecastTrackDlg.open();

        // Register mouse handler.
        this.inputHandler = getMouseHandler();
        if (this.inputHandler != null) {
            mapEditor.registerMouseHandler(this.inputHandler);
            AtcfSession.getInstance().setAtcfTool(this);
        }
    }

    /*
     * Create a "Forecast Track" dialog
     *
     * @param storm
     *
     * @param fcstTrackData
     */
    private static synchronized void createForecastTrackDialog(Storm storm,
            Map<String, List<ForecastTrackRecord>> fcstTrackData) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        forecastTrackDlg = new ForecastTrackDialog(shell, storm, fcstTrackData);
        forecastTrackDlg.addCloseCallback(ov -> {
            forecastTrackDlg.deactivateTool();
            forecastTrackDlg = null;
        });
    }

    /**
     * Returns the current mouse handler.
     *
     * @return
     */
    public IInputHandler getMouseHandler() {
        if (this.inputHandler == null) {
            this.inputHandler = new ForecastTrackHandler(this);
        } else {
            ForecastTrackHandler handler = (ForecastTrackHandler) inputHandler;
            if (handler.getDrawingLayer() != drawingLayer
                    || handler.getCurStorm() != AtcfSession.getInstance()
                            .getActiveStorm()) {
                this.inputHandler = new ForecastTrackHandler(this);
            }
        }

        return this.inputHandler;
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
     * @return the forecastTrackDlg
     */
    public static ForecastTrackDialog getForecastTrackDlg() {
        return forecastTrackDlg;
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
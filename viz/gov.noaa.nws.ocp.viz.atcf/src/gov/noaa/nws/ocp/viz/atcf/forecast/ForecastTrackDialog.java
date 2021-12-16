/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.DeleteForecastTrackRecordRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.aids.DisplayObjectiveAidsDialog;
import gov.noaa.nws.ocp.viz.atcf.aids.ObjAidsGenerator;
import gov.noaa.nws.ocp.viz.atcf.handler.ForecastTrackHandler;
import gov.noaa.nws.ocp.viz.atcf.handler.ForecastTrackTool;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.drawing.elements.AbstractDrawableComponent;

/**
 * Dialog for "Forecast"=>"Forecast Track...".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 02, 2019 71722      jwu         Initial creation.
 * Jun 10, 2020 78027      jwu         Add more functionality.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Apr 19, 2021 88712      jwu         Revise on NHC feedback.
 * May 27, 2021 91757      jwu         Revise mouse actions on NHC feedback.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ForecastTrackDialog extends ForecastDialog {

    /*
     * Default consensus aids to be drawn when "Consensus Aids" is clicked. May
     * be updated based on NHC's request.
     */
    private static final java.util.List<String> OBJ_TRACK_CONSENSUS = Arrays
            .asList("TVCN", "HCCA");

    // Names/units for track information parameters
    private static final String[] trackParameters = new String[] {
            "  TAU\n  (hrs)", "     LAT\n ", "      LON\n ", "        DIR\n ",
    "SPEED\n(kts)" };

    // Names/units for aid information parameters
    private static final String[] aidDisplayOptions = new String[] {
            "Tau Labels", "Complete Tracks", "Concensus Aids", "GPCE Prob.",
            "GPCE Climatology", "GPCE-AX" };

    private static final String DIR_SPD_FORMAT = " %8d %10d";

    // private org.eclipse.swt.graphics.Color tauBtnClr;

    // List to show current data.
    private List trkInfoList;

    // "Forecast" radio button
    private Button fcstBtn;

    // Save button.
    private Button saveBtn;

    // Display option buttons.
    private java.util.List<Button> aidsDisplayBtns = new ArrayList<>();

    // Forecast track records from DB for current storm.
    private Map<String, java.util.List<ForecastTrackRecord>> fcstTrkData;

    // Instance used to generator obj aids display.
    private ObjAidsGenerator objAidsGenerator = null;

    // Forecast track tool.
    private ForecastTrackTool fcstTrackTool;

    // Mode - True: Forecast; False - Delete.
    private boolean fcstMode = true;

    /**
     * Constructor
     *
     * @param parent
     */
    public ForecastTrackDialog(Shell parent, Storm curStorm,
            Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData) {

        super(parent, curStorm, ForecastType.TRACK, fcstTrackData);

        // Initialize
        initialize();

        // Retrieve A-Deck records for current DTG and display
        displayObjAids();
    }

    /**
     * Initialize the dialog components.
     *
     * @param parent
     */
    @Override
    protected void populateScrollComponent(Composite parent) {
        createDataComp(parent);
        createControlButtons(parent);
    }

    /**
     * Create the top section.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, true);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginHeight = 3;
        dtgInfoLayout.marginWidth = 20;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // Display current DTG
        createDtgSelectionComp(mainComp);

        // Create composite for display and forecast options.
        createForecastComp(mainComp);

        // Create the list to show forecast track information.
        createTrackInfoComp(mainComp);
    }

    /**
     * Create obj aids display options and TAU selection composite at top.
     *
     * @param parent
     */
    private void createForecastComp(Composite parent) {

        // Major composite for aids display options and forecast TAUs.
        Composite forecastComp = new Composite(parent, SWT.BORDER);
        GridLayout forecastCompLayout = new GridLayout(2, true);
        forecastCompLayout.verticalSpacing = 10;
        forecastCompLayout.marginHeight = 3;
        forecastCompLayout.marginWidth = 0;
        forecastComp.setLayout(forecastCompLayout);
        forecastComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        createOptionsComp(forecastComp);

        createForecastTauComp(forecastComp);
    }

    /**
     * Create obj aids display options composite.
     *
     * @param parent
     */
    private void createOptionsComp(Composite parent) {
        Composite aidsDisplayComp = new Composite(parent, SWT.NONE);
        GridLayout aidsDisplayGL = new GridLayout(1, false);
        aidsDisplayGL.marginWidth = 5;
        aidsDisplayGL.marginHeight = 5;
        aidsDisplayGL.verticalSpacing = 26;
        aidsDisplayComp.setLayout(aidsDisplayGL);
        aidsDisplayComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Composite aidOptionsComp = new Composite(aidsDisplayComp, SWT.BORDER);
        GridLayout aidOptGL = new GridLayout(1, true);
        aidOptGL.marginWidth = 5;
        aidOptGL.marginHeight = 10;
        aidOptionsComp.setLayout(aidOptGL);
        aidOptionsComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        int optNum = 0;
        for (String opt : aidDisplayOptions) {
            Button dispOptionBtn = new Button(aidOptionsComp, SWT.CHECK);
            aidsDisplayBtns.add(dispOptionBtn);
            dispOptionBtn.setText(opt);
            dispOptionBtn.setData(optNum);
            GridData dispOptionBtnGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                    false);
            dispOptionBtn.setLayoutData(dispOptionBtnGD);
            dispOptionBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int optSeq = (int) e.widget.getData();
                    boolean optVal = dispOptionBtn.getSelection();
                    switch (optSeq) {
                    case 0:
                        // TODO draw TAU labels
                        drawForecastTrack();
                        break;
                    case 1:
                        // Complete Tracks
                        if (optVal) {
                            objAidsProperties
                            .setSelectedPartialTime(objAidsProperties
                                    .getAvailablePartialTimes()[0]);
                        } else {
                            objAidsProperties.setSelectedPartialTime(
                                    "" + currentTau.getValue());
                        }
                        break;
                    case 2:
                        // consensus aids
                        if (optVal) {
                            selectedObjAids = objAidsProperties
                                    .getSelectedObjectiveAids();
                            objAidsProperties.setSelectedObjectiveAids(
                                    OBJ_TRACK_CONSENSUS);
                        } else {
                            objAidsProperties
                            .setSelectedObjectiveAids(selectedObjAids);
                        }
                        break;
                    case 3:
                        // GPCE
                        objAidsProperties.setGpce(optVal);
                        break;
                    case 4:
                        // GPCE Climatology
                        objAidsProperties.setGpceClimatology(optVal);
                        break;
                    case 5:
                        // GPCE-AX
                        objAidsProperties.setGpceAX(optVal);
                        break;
                    default:
                        break;
                    }

                    displayObjAids();
                }
            });

            optNum++;
        }

        Button aidsBtn = new Button(aidsDisplayComp, SWT.PUSH);
        GridData aidsBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        aidsBtn.setLayoutData(aidsBtnGD);
        aidsBtn.setText("Other Aids...");
        aidsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DisplayObjectiveAidsDialog aidsDlg = DisplayObjectiveAidsDialog
                        .getInstance(shell);
                if (aidsDlg == null) {
                    logger.error(
                            "ForecastTrackDialog: Cannot create DisplayObjectiveAidsDialog.");
                } else {
                    aidsDlg.setBlockOnOpen(false);

                    if (objAidsProperties
                            .getAvailableDateTimeGroups().length == 0) {
                        java.util.List<String> allDTG = AtcfDataUtil
                                .getDateTimeGroups(storm);
                        objAidsProperties.setAvailableDateTimeGroups(
                                allDTG.toArray(new String[allDTG.size()]));
                    }

                    aidsDlg.setObjAidsGenerator(objAidsGenerator);
                    aidsDlg.setObjAidsProperties(objAidsProperties);

                    aidsDlg.open();
                }
            }
        });

        Button cpaBtn = new Button(aidsDisplayComp, SWT.PUSH);
        cpaBtn.setText("Priority CPA's...");

        GridData cpaBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        cpaBtn.setLayoutData(cpaBtnGD);
        cpaBtn.setEnabled(false);

        Button consensusBtn = new Button(aidsDisplayComp, SWT.PUSH);
        consensusBtn.setText("Use Consensus");

        GridData consensusBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true,
                true);
        consensusBtn.setLayoutData(consensusBtnGD);
        consensusBtn.setEnabled(true);
        consensusBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                useConsensus();

                // Show ghost for current TAU
                ForecastTrackHandler handler = (ForecastTrackHandler) (getFcstTrackTool()
                        .getMouseHandler());

                handler.setTrackPos(getTrackPositions());
                handler.drawGhost(currentTau, getTrackPositions(), null);
            }
        });

    }

    /**
     * Create forecast TAU selection composite.
     *
     * @param parent
     */
    private void createForecastTauComp(Composite parent) {

        tauBtns = new EnumMap<>(AtcfTaus.class);

        Composite forecastTauComp = new Composite(parent, SWT.NONE);
        GridLayout fcstTauGL = new GridLayout(1, false);
        fcstTauGL.marginWidth = 5;
        fcstTauGL.marginHeight = 5;
        fcstTauGL.verticalSpacing = 5;
        forecastTauComp.setLayout(fcstTauGL);
        forecastTauComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Composite fcstDelComp = new Composite(forecastTauComp, SWT.BORDER);
        GridLayout fcstDelGL = new GridLayout(2, false);
        fcstDelGL.marginWidth = 5;
        fcstDelGL.horizontalSpacing = 10;
        fcstDelComp.setLayout(fcstDelGL);
        fcstDelComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        fcstBtn = new Button(fcstDelComp, SWT.RADIO);
        fcstBtn.setText("Forecast");
        GridData fcstBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        fcstBtn.setLayoutData(fcstBtnGD);
        fcstBtn.setSelection(fcstMode);
        fcstBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                fcstMode = fcstBtn.getSelection();
                // Show ghost for current TAU
                ForecastTrackHandler handler = (ForecastTrackHandler) (getFcstTrackTool()
                        .getMouseHandler());

                handler.setTrackPos(getTrackPositions());
                handler.drawGhost(currentTau, getTrackPositions(), null);
            }
        });

        Button delBtn = new Button(fcstDelComp, SWT.RADIO);
        delBtn.setText("Delete");
        GridData delBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        delBtn.setLayoutData(delBtnGD);
        delBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // remove ghost, if any....
                drawingLayer.removeGhost();
                AbstractEditor editor = AtcfVizUtil.getActiveEditor();
                if (editor instanceof AbstractEditor) {
                    editor.refresh();
                }
            }
        });

        Composite tauBtnComp = new Composite(forecastTauComp, SWT.NONE);
        GridLayout tauBtnLayout = new GridLayout(1, true);
        tauBtnLayout.marginWidth = 5;
        tauBtnLayout.marginHeight = 0;
        tauBtnLayout.verticalSpacing = 0;
        tauBtnComp.setLayout(tauBtnLayout);
        tauBtnComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        currentTau = AtcfTaus.TAU12;

        workingTaus = AtcfTaus.getForecastTaus();
        for (AtcfTaus tau : workingTaus) {
            if (tau.getValue() > 0) {
                Button tauBtn = new Button(tauBtnComp, SWT.PUSH);
                tauBtn.setText("" + tau.getValue());
                GridData tauBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true,
                        true);
                tauBtn.setLayoutData(tauBtnGD);
                tauBtn.setData(tau);

                tauBtns.put(tau, tauBtn);

                if (tau == EXPERIMENTAL_TAU) {
                    tauBtn.setEnabled(useTau60);
                }

                tauBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        AtcfTaus preTau = currentTau;
                        currentTau = (AtcfTaus) e.widget.getData();

                        // Update the current TAU for mouse handler.
                        ForecastTrackHandler handler = (ForecastTrackHandler) (getFcstTrackTool()
                                .getMouseHandler());
                        handler.setCurrentTau(currentTau);

                        if (preTau != currentTau) {
                            for (Button btn : tauBtns.values()) {
                                if (btn == tauBtn) {
                                    AtcfVizUtil.setActiveButtonBackground(btn);
                                } else {
                                    AtcfVizUtil.setDefaultBackground(btn);
                                }
                            }

                            objAidsProperties.setSelectedPartialTime(
                                    "" + currentTau.getValue());

                            displayObjAids();
                        }

                        if (isForecast()) {

                            fcstTrackTool.reactivateTool();

                            // Show ghost for current TAU.
                            Map<Integer, Coordinate> trackPos = getTrackPositions();
                            handler.setTrackPos(trackPos);
                            handler.drawGhost(currentTau, trackPos, null);
                        } else {
                            // Delete forecast for current TAU, if any.
                            deleteForecast(currentTau);
                        }

                    }
                });

                if (tau == currentTau) {
                    tauBtn.setSelection(true);
                    AtcfVizUtil.setActiveButtonBackground(tauBtn);

                }
            }
        }
    }

    /**
     * Create the composite list obj aid info.
     *
     * @param parent
     */
    private void createTrackInfoComp(Composite parent) {
        Composite trkInfoComp = new Composite(parent, SWT.BORDER);
        GridLayout trkInfoGL = new GridLayout(trackParameters.length, false);
        trkInfoGL.marginWidth = 5;
        trkInfoGL.marginHeight = 15;
        trkInfoGL.horizontalSpacing = 30;
        trkInfoGL.verticalSpacing = 5;
        trkInfoComp.setLayout(trkInfoGL);
        trkInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        for (String param : trackParameters) {
            Label infoLbl = new Label(trkInfoComp, SWT.NONE);
            infoLbl.setText(param);
            GridData infoLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false);
            infoLbl.setLayoutData(infoLblData);
        }

        trkInfoList = new List(trkInfoComp,
                SWT.NONE | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData trkInfoGD = new GridData(400, 180);
        trkInfoGD.horizontalAlignment = SWT.FILL;
        trkInfoGD.verticalAlignment = SWT.FILL;
        trkInfoGD.horizontalSpan = trackParameters.length;
        trkInfoList.setLayoutData(trkInfoGD);

        trkInfoList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        updateTrackInfoList();
    }

    /**
     * Create the composite to select DTG.
     *
     * @param parent
     */
    private void createDtgSelectionComp(Composite parent) {
        Composite dtgComp = new Composite(parent, SWT.NONE);
        GridLayout dtgCompGD = new GridLayout(2, false);
        dtgCompGD.marginWidth = 0;
        dtgCompGD.marginHeight = 0;
        dtgComp.setLayout(dtgCompGD);
        dtgComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label dtgLabel = new Label(dtgComp, SWT.NONE);
        dtgLabel.setText("Current DTG (YYYYMMDDHH): ");
        GridData dtgLabelData = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgLabel.setLayoutData(dtgLabelData);

        Text dtgTxt = new Text(dtgComp, SWT.NONE);
        dtgTxt.setEditable(false);
        GridData dtgTxtGD = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        dtgTxt.setLayoutData(dtgTxtGD);
        dtgTxt.setText(currentDTG);
    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnCompGL = new GridLayout(5, true);
        ctrlBtnCompGL.marginWidth = 20;
        ctrlBtnCompGL.horizontalSpacing = 15;
        ctrlBtnComp.setLayout(ctrlBtnCompGL);
        ctrlBtnComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(ctrlBtnComp, AtcfVizUtil.buttonGridData());

        saveBtn = new Button(ctrlBtnComp, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setEnabled(true);
        saveBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                fcstTrackTool.removeTool();

                saveForecast();
                displayObjAids();
                saveBtn.setEnabled(false);
            }
        });

        Button submitBtn = new Button(ctrlBtnComp, SWT.PUSH);
        submitBtn.setText("Submit");
        submitBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        submitBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                submitForecast(saveBtn.isEnabled());
                deactivateTool();

                close();
            }
        });

        Button cancelBtn = new Button(ctrlBtnComp, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        Button nextBtn = new Button(ctrlBtnComp, SWT.PUSH);
        nextBtn.setText(">>");
        nextBtn.setToolTipText("Continue to Forecast Intensity");
        nextBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        nextBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                saveForecast();

                deactivateTool();
                close();

                // Start Forecast Intensity
                AtcfVizUtil.executeCommand(
                        "gov.noaa.nws.ocp.viz.atcf.ui.intensityForecast", null,
                        null);
            }
        });
    }

    /*
     * Initialize - make a copy of data for editing.
     */
    private void initialize() {

        fcstTrkData = new LinkedHashMap<>();

        for (Map.Entry<String, java.util.List<ForecastTrackRecord>> entry : fcstTrackData
                .entrySet()) {
            java.util.List<ForecastTrackRecord> recs = new ArrayList<>();
            for (ForecastTrackRecord frec : entry.getValue()) {
                recs.add(new ForecastTrackRecord(frec));
            }

            fcstTrkData.put(entry.getKey(), recs);
        }
    }

    /**
     * Update the information in the information list
     */
    public void updateTrackInfoList() {

        if (trkInfoList == null || trkInfoList.isDisposed()) {
            return;
        }

        trkInfoList.removeAll();

        java.util.List<ForecastTrackRecord> recs = new ArrayList<>();
        for (AtcfTaus tau : workingTaus) {

            java.util.List<ForecastTrackRecord> fcstRecs = getForecastForTau(
                    tau);
            if (!fcstRecs.isEmpty()) {
                recs.add(fcstRecs.get(0));
            }
        }

        ForecastTrackRecord rec0 = recs.get(0);
        if (rec0.getStormDrct() > MAX_WIND_DRCT
                || rec0.getStormSped() > MAX_WIND_SPEED) {
            trkInfoList.add(buildInfoForTau0());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(buildTrackInfoStr(rec0));
            sb.append(String.format(DIR_SPD_FORMAT, (int) rec0.getStormDrct(),
                    (int) rec0.getStormSped()));
            trkInfoList.add(sb.toString());
        }

        for (int ii = 1; ii < recs.size(); ii++) {

            ForecastTrackRecord rec1 = recs.get(ii);

            if (rec1 != null) {
                StringBuilder sb = new StringBuilder();
                sb.append(buildTrackInfoStr(rec1));

                if (rec1.getStormDrct() > MAX_WIND_DRCT
                        || rec1.getStormSped() > MAX_WIND_SPEED) {
                    ForecastTrackRecord rec = recs.get(ii - 1);
                    int fcstHr = rec.getFcstHour();
                    Coordinate loc = new Coordinate(rec.getClon(),
                            rec.getClat());
                    int fcstHr1 = rec1.getFcstHour();
                    Coordinate loc1 = new Coordinate(rec1.getClon(),
                            rec1.getClat());
                    double[] distDir = AtcfVizUtil.getDistNDir(loc, loc1);
                    double spd = distDir[0] / AtcfVizUtil.NM2M
                            / (fcstHr1 - fcstHr);

                    sb.append(String.format(" %8s %10s", (int) distDir[1],
                            (int) Math.round(spd)));
                } else {
                    sb.append(String.format(DIR_SPD_FORMAT,
                            (int) rec1.getStormDrct(),
                            (int) rec1.getStormSped()));
                }

                trkInfoList.add(sb.toString());
            }
        }
    }

    /*
     * Build an information string for an ForecastTrackRecord
     *
     * @param rec ForecastTrackRecord
     *
     * @return String
     */
    private String buildTrackInfoStr(ForecastTrackRecord rec) {

        String str = "";
        if (rec != null) {

            int fcstHr = rec.getFcstHour();

            float lat = rec.getClat();
            String latDir = "N";
            if (lat < 0) {
                latDir = "S";
            }

            float lon = rec.getClon();
            String lonDir = "E";
            if (lon <= 0.0F) {
                lonDir = "W";
            }

            if (lon < 0) {
                lon = -lon;
            }

            str += String.format("%3d %10.1f%s %8.1f%s", fcstHr, Math.abs(lat),
                    latDir, Math.abs(lon), lonDir);

        }

        return str;
    }

    /*
     * Redraw forecast track with current changes (may not have been saved yet).
     */
    public void redrawForecastTrack() {
        super.drawForecastTrack(fcstTrkData);
    }

    /**
     * Move to next TAU.
     */
    public void moveToNextTau() {

        int index = workingTaus.indexOf(currentTau);
        int nextInd = index + 1;

        if (nextInd < workingTaus.size()) {
            currentTau = workingTaus.get(index + 1);

            for (Map.Entry<AtcfTaus, Button> entry : tauBtns.entrySet()) {
                Button btn = entry.getValue();
                if (entry.getKey() == currentTau) {
                    AtcfVizUtil.setActiveButtonBackground(btn);
                } else {
                    AtcfVizUtil.setDefaultBackground(btn);
                }
            }

            objAidsProperties
            .setSelectedPartialTime("" + currentTau.getValue());

            displayObjAids();
        }

    }

    /**
     * Deactivate ForecastTrackTool.
     */
    public void deactivateTool() {
        if (fcstTrackTool != null) {
            fcstTrackTool.deactivate();
        }
    }

    /*
     * Delete track forecast for a give forecast hour
     *
     * @param tau delete forecast for the TAU.
     */
    private void deleteForecast(AtcfTaus tau) {
        java.util.List<ForecastTrackRecord> fcst = getForecastForTau(tau);
        if (fcst.isEmpty()) {
            return;
        }

        MessageDialog confirmDlg = new MessageDialog(this.getShell(),
                "Delete forecast for TAU " + tau.getValue(), null,
                "Are you sure you want to delete the forecast for TAU "
                        + tau.getValue() + "?",
                        MessageDialog.QUESTION, new String[] { "OK", "Cancel" }, 0);
        confirmDlg.open();

        if (confirmDlg.getReturnCode() == Window.OK) {

            // Check if there are records for this DTG and TAU in DB.
            boolean recInDb = false;
            for (ForecastTrackRecord rec : fcst) {
                ForecastTrackRecord orec = getForecastByTauNRadii(tau,
                        WindRadii.getWindRadii((int) rec.getRadWind()));
                if (orec != null) {
                    recInDb = true;
                    break;
                }
            }

            // Remove from DB.
            if (recInDb) {
                DeleteForecastTrackRecordRequest req = new DeleteForecastTrackRecordRequest();
                req.setFstRecords(fcst);
                req.setSandboxId(fcstTrackSandBoxID);

                try {
                    ThriftClient.sendRequest(req);
                } catch (Exception e) {
                    logger.warn(
                            "ForecastTrackDialog: Failed to delete forecast for TAU: "
                                    + tau.getValue(),
                            e);
                }
            }

            // Remove from working copy and update tool.
            removeForecastForTau(tau);
            ForecastTrackHandler handler = (ForecastTrackHandler) (getFcstTrackTool()
                    .getMouseHandler());
            handler.setTrackPos(getTrackPositions());

            redrawForecastTrack();

            updateTrackInfoList();
        }

    }

    /*
     * Find the forecast track records stored in the map for a TAU.
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @return List<ForecastTrackRecord>
     */
    private java.util.List<ForecastTrackRecord> getForecastForTau(
            AtcfTaus tau) {
        java.util.List<ForecastTrackRecord> recs = new ArrayList<>();
        for (Map.Entry<String, java.util.List<ForecastTrackRecord>> entry : fcstTrkData
                .entrySet()) {
            java.util.List<ForecastTrackRecord> rcs = entry.getValue();
            if (rcs != null && tau.getValue() == rcs.get(0).getFcstHour()) {
                recs.addAll(rcs);
                break;
            }
        }

        return recs;
    }

    /*
     * Remove records from the forecast track records stored in the map for a
     * TAU.
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @return List<ForecastTrackRecord>
     */
    private void removeForecastForTau(AtcfTaus tau) {
        for (Map.Entry<String, java.util.List<ForecastTrackRecord>> entry : fcstTrkData
                .entrySet()) {
            java.util.List<ForecastTrackRecord> rcs = entry.getValue();
            if (rcs != null && tau.getValue() == rcs.get(0).getFcstHour()) {
                fcstTrkData.remove(entry.getKey());
                break;
            }
        }
    }

    /*
     * Build info string for TAU 0.
     *
     * @return String
     */
    private String buildInfoForTau0() {

        StringBuilder sb = new StringBuilder();

        java.util.List<BDeckRecord> brecs = currentBDeckRecords.get(currentDTG);
        if (brecs == null) {
            String latestDtg = Collections.max(currentBDeckRecords.keySet());
            brecs = currentBDeckRecords.get(latestDtg);
        }

        if (brecs != null) {
            BDeckRecord rec = brecs.get(0);
            int fcstHr = rec.getFcstHour();

            float lat = rec.getClat();
            String latDir = "N";
            if (lat < 0) {
                latDir = "S";
            }

            float lon = rec.getClon();
            String lonDir = "E";
            if (lon <= 0.0F) {
                lonDir = "W";
            }

            if (lon < 0) {
                lon = -lon;
            }

            sb.append(String.format("%3d %10.1f%s %8.1f%s", fcstHr,
                    Math.abs(lat), latDir, Math.abs(lon), lonDir));

            if (currentBDeckRecords.size() > 1) {

                TreeSet<String> dtgs = new TreeSet<>(
                        currentBDeckRecords.keySet());
                dtgs.remove(currentDTG);
                java.util.List<BDeckRecord> brecs1 = currentBDeckRecords
                        .get(dtgs.last());

                BDeckRecord rec1 = brecs1.get(0);

                int timeInHr = (int) ((rec.getRefTime().getTime()
                        - rec1.getRefTime().getTime()) / 1000 / 3600);

                Coordinate loc = new Coordinate(rec.getClon(), rec.getClat());
                Coordinate loc1 = new Coordinate(rec1.getClon(),
                        rec1.getClat());

                double[] distDir = AtcfVizUtil.getDistNDir(loc, loc1);
                double spd = distDir[0] / AtcfVizUtil.NM2M / timeInHr;

                sb.append(String.format(DIR_SPD_FORMAT, (int) distDir[1],
                        (int) Math.round(spd)));
            }
        }

        return sb.toString();

    }

    /*
     * Draw objective aids (A-Deck)
     */
    private void displayObjAids() {

        if (objAidsGenerator == null) {
            objAidsGenerator = new ObjAidsGenerator();
            objAidsGenerator.setObjAidsProperties(objAidsProperties);
        }

        boolean displayAids = false;

        AtcfProduct prd = drawingLayer.getResourceData().getActiveAtcfProduct();
        java.util.List<AbstractDrawableComponent> elems = prd.getADeckLayer()
                .getDrawables();

        for (Button btn : aidsDisplayBtns) {
            if (btn.getSelection()) {
                displayAids = true;
                break;
            }
        }

        /*
         * Display obj. aids if they are currently displaying or asked to be
         * displayed in this dialog.
         */
        if (displayAids || !elems.isEmpty()) {
            objAidsGenerator.draw();
        } else {
            objAidsGenerator.remove();
        }
    }

    /*
     * Put current forecast locations into a map.
     *
     * @return Map<Integer, Coordinate>
     */
    private Map<Integer, Coordinate> getTrackPositions() {

        Map<Integer, Coordinate> trackPos = new LinkedHashMap<>();

        // Put all track locations into a map.
        for (AtcfTaus tau : workingTaus) {
            Coordinate pos = null;
            for (java.util.List<ForecastTrackRecord> recs : fcstTrkData
                    .values()) {
                ForecastTrackRecord rc = recs.get(0);
                if (tau.getValue() == rc.getFcstHour()) {
                    pos = new Coordinate(AtcfVizUtil.snapToTenth(rc.getClon()),
                            AtcfVizUtil.snapToTenth(rc.getClat()));
                    break;
                }
            }

            trackPos.put(tau.getValue(), pos);
        }

        return trackPos;

    }

    /**
     * @return boolean forecast or delete
     */
    public boolean isForecast() {
        return fcstMode;
    }

    /**
     * @return the fcstTrackTool
     */
    public ForecastTrackTool getFcstTrackTool() {
        return fcstTrackTool;
    }

    /**
     * @return the fcstTrkData
     */
    public Map<String, java.util.List<ForecastTrackRecord>> getFcstTrkData() {
        return fcstTrkData;
    }

    /**
     * @param fcstTrackTool
     *            the fcstTrackTool to set
     */
    public void setFcstTrackTool(ForecastTrackTool fcstTrackTool) {
        this.fcstTrackTool = fcstTrackTool;
    }

    /**
     * Enable/disable "Save" button.
     *
     * @param enable
     *            True/false - enable/disable save button
     */
    public void setSaveStatus(boolean enable) {
        saveBtn.setEnabled(enable);
        if (enable) {
            changeListener.setChangesUnsaved(true);
        }
    }

    /**
     * Apply local changes before saving.
     */
    @Override
    protected void applyChanges() {

        fcstTrackData.clear();

        for (Map.Entry<String, java.util.List<ForecastTrackRecord>> entry : fcstTrkData
                .entrySet()) {
            java.util.List<ForecastTrackRecord> recs = new ArrayList<>();
            for (ForecastTrackRecord frec : entry.getValue()) {
                recs.add(new ForecastTrackRecord(frec));
            }

            fcstTrackData.put(entry.getKey(), recs);
        }

        drawForecastTrack();
    }

    /*
     * Use consensus forecast (TVCN)
     */
    private void useConsensus() {

        // Find intensity from consensus records
        Map<Integer, ADeckRecord> consensusRecords = new HashMap<>();
        java.util.List<ForecastTrackRecord> consensusFcst = new ArrayList<>();
        for (ADeckRecord rec : currentADeckRecords) {
            String aidCode = rec.getTechnique();
            if (TRACK_CONSENSUS.equalsIgnoreCase(aidCode)) {
                consensusRecords.put(rec.getFcstHour(), rec);
                consensusFcst.add(AtcfDataUtil.makeFcstTrackRecord(rec));
            }
        }

        // Use consensus technique's forecast
        if (consensusRecords.isEmpty()) {
            logger.warn("ForecastTrackDialog: No consensus forecast found for "
                    + TRACK_CONSENSUS);
        } else {
            Map<String, java.util.List<ForecastTrackRecord>> consensusData = AtcfDataUtil
                    .sortForecastTrackDataInDTGs(consensusFcst, true);
            for (String dtg : fcstTrkData.keySet()) {
                java.util.List<ForecastTrackRecord> recs = consensusData
                        .get(dtg);
                if (recs != null) {
                    fcstTrkData.put(dtg, recs);
                }
            }

            // Draw forecast track.
            drawForecastTrack(fcstTrkData);

            // Update forecast info list.
            updateTrackInfoList();

            saveBtn.setEnabled(true);
            changeListener.setChangesUnsaved(true);
        }
    }

    /**
     * If unsaved changes exist, prompt user confirmation on closing. If no
     * unsaved changes, proceed with closing without prompt.
     */
    @Override
    public boolean shouldClose() {
        boolean close = true;
        if (changeListener.isChangesUnsaved()) {
            MessageDialog confirmDlg = new MessageDialog(getShell(),
                    "ForecastTrackDialog - Unsaved Changes", null,
                    "Close this window? Unsaved changes will be lost.",
                    MessageDialog.CONFIRM, new String[] { "Yes", "No" }, 1);
            confirmDlg.open();

            if (confirmDlg.getReturnCode() == Window.OK) {
                changeListener.setChangesUnsaved(false);

                drawForecastTrack();
                deactivateTool();
            } else {
                close = false;
            }
        }

        return close;
    }

}

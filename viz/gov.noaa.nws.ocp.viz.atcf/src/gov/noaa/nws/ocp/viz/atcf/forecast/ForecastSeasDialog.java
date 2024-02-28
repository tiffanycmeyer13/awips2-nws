/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;

/**
 * Dialog for Forecast=>Forecast Seas.
 *
 * Note: The 12 ft seas forecast is only done for TAU 3 and it is manually
 * entered via "Advisory Composition => Advisory Data". It could be edited via
 * this tool but new forecast records will be not created here.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 08, 2020 77478      jwu         Initial creation.
 * May 28, 2020 78027      jwu         Update to track changes etc.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Apr 29, 2021 88730      jwu         Update Tau 0 and ADeck .
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */
public class ForecastSeasDialog extends ForecastDialog {

    // Label at the top to show the current TAU
    private Label currentTauLbl;

    // Label for 12 ft seas wave radii guidance, including the current TAU
    private Label seasGuidanceLbl;

    private Label maxWindLbl;

    private Label spdLbl;

    private Label dirLbl;

    // Forecast guidance list
    private List seasGuideList;

    // List for current 12 ft seas forecast
    private List seasForecastList;

    // Set while updating CCombo controls
    private boolean ignoreUIChanges;

    // Flag for wave radii in "Circle" or "Quad"
    private boolean isCircle = false;

    // Buttons and CCombos for 12 ft seas radii.
    private java.util.List<Button> radiiQuadBtns;

    private java.util.List<CCombo> radiiValueCmbs;

    // Save, Submit buttons
    private Button saveBtn;

    private Button submitBtn;

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     * @param storm
     *            Storm
     */
    public ForecastSeasDialog(Shell parent, Storm storm,
            Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData) {
        super(parent, storm, ForecastType.SEAS, fcstTrackData);
    }

    /**
     * Initializes the components.
     *
     * @param parent
     */
    @Override
    protected void populateScrollComponent(Composite parent) {

        radiiQuadBtns = new ArrayList<>();
        radiiValueCmbs = new ArrayList<>();

        // Create dialog.
        createContents(parent);
    }

    /**
     * Create contents.
     *
     * @param parent
     */
    protected void createContents(Composite parent) {

        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainCompGL = new GridLayout(1, false);
        mainCompGL.verticalSpacing = 5;
        mainComp.setLayout(mainCompGL);
        GridData mainGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComp.setLayoutData(mainGD);

        createSeasRadiiInfoComp(mainComp);
        createSeasGuidanceComp(mainComp);
        createCurrentForecastComp(mainComp);
        createCtrlBtns(mainComp);

        setTauBtnStatus();

        populateForecastInfo(currentTau);

        updateSeasGuideList(currentTau);

        updateSeasForecastList();
    }

    /**
     * Creates the top section of the GUI
     *
     * @param parent
     */
    protected void createSeasRadiiInfoComp(Composite parent) {
        Composite topComp = new Composite(parent, SWT.NONE);
        GridLayout topCompGL = new GridLayout(2, false);
        topCompGL.horizontalSpacing = 10;
        topCompGL.marginTop = 0;
        topComp.setLayout(topCompGL);

        Group leftComp = new Group(topComp, SWT.NONE);
        GridLayout leftCompGL = new GridLayout(1, true);
        leftCompGL.marginHeight = 20;
        leftCompGL.verticalSpacing = 37;
        leftComp.setLayout(leftCompGL);

        GridData leftCompGD = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        leftComp.setLayoutData(leftCompGD);

        // Label for current TAU
        currentTauLbl = new Label(leftComp, SWT.CENTER);
        currentTauLbl.setText(String.format(" TAU %5d     12 Ft Seas ",
                currentTau.getValue()));
        GridData mainLblGD = new GridData(SWT.CENTER, SWT.TOP, false, false);
        mainLblGD.horizontalSpan = 5;
        currentTauLbl.setLayoutData(mainLblGD);

        // Composite for 12 ft seas wave radii selections
        Composite radiiComp = new Composite(leftComp, SWT.NONE);
        GridLayout radiiCompGL = new GridLayout(5, false);
        radiiCompGL.verticalSpacing = 10;
        radiiComp.setLayout(radiiCompGL);

        // Row for labeling quadrants for radii
        Label spaceLbl = new Label(radiiComp, SWT.NONE);
        spaceLbl.setText("");
        GridData lblGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        lblGD.horizontalSpan = 1;
        spaceLbl.setLayoutData(lblGD);

        for (int i = 0; i < 4; i++) {
            Label radiiLbl = new Label(radiiComp, SWT.NONE);
            radiiLbl.setText(quadrantLabels[i]);
        }

        // Row for 12 ft seas wave radii selection
        Group ktQuadGrp = new Group(radiiComp, SWT.NONE);
        GridLayout ktQuadGrpGL = new GridLayout(2, true);
        ktQuadGrpGL.horizontalSpacing = 2;
        ktQuadGrp.setLayout(ktQuadGrpGL);

        Button ktCircleBtn = new Button(ktQuadGrp, SWT.RADIO);
        ktCircleBtn.setText("Circle");
        radiiQuadBtns.add(ktCircleBtn);
        ktCircleBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button btn = (Button) e.widget;
                isCircle = ((Button) e.widget).getSelection();
                updateWaveRadiiQuad(currentTau, btn);
                saveBtn.setEnabled(true);
                submitBtn.setEnabled(true);
                changeListener.setChangesUnsaved(true);
            }
        });

        Button ktQuadBtn = new Button(ktQuadGrp, SWT.RADIO);
        ktQuadBtn.setText("Quad");
        ktQuadBtn.setData(true);
        ktQuadBtn.setSelection(true);
        radiiQuadBtns.add(ktQuadBtn);
        ktQuadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button btn = (Button) e.widget;
                isCircle = !((Button) e.widget).getSelection();
                updateWaveRadiiQuad(currentTau, btn);
                saveBtn.setEnabled(true);
                submitBtn.setEnabled(true);
                changeListener.setChangesUnsaved(true);
            }
        });

        GridData radiiDropGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        radiiDropGD.widthHint = 63;

        for (int kk = 0; kk < 4; kk++) {
            CCombo ktCombo = new CCombo(radiiComp, SWT.NONE);
            ktCombo.setItems(radiiEntries);
            ktCombo.select(0);
            ktCombo.setData(kk);
            ktCombo.setLayoutData(radiiDropGD);
            radiiValueCmbs.add(ktCombo);

            ktCombo.addVerifyListener(
                    verifyListener.getRadiiCmbVerifyListener());

            ktCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateWaveRadiiValue(currentTau, e.widget);
                    saveBtn.setEnabled(true);
                    submitBtn.setEnabled(true);
                }
            });

            ktCombo.addModifyListener(e -> {

                if (ignoreUIChanges) {
                    return;
                }

                updateWaveRadiiValue(currentTau, e.widget);
                saveBtn.setEnabled(true);
                submitBtn.setEnabled(true);

                // Update flag to indicate new changes.
                if (!changeListener.isIgnoreChanges()) {
                    changeListener.setChangesUnsaved(true);
                }
            });
        }

        // Delete 12 Ft Seas Radii
        Composite actionComp = new Composite(leftComp, SWT.NONE);
        GridLayout actionCompGL = new GridLayout(1, true);
        actionCompGL.horizontalSpacing = 10;
        actionCompGL.verticalSpacing = 15;
        actionComp.setLayout(actionCompGL);

        int wd = 145;
        GridData actionCompGD = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        actionComp.setLayoutData(actionCompGD);

        Button deleteBtn = new Button(actionComp, SWT.NONE);
        deleteBtn.setText("Delete Radii");
        GridData delBtnGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        delBtnGD.widthHint = wd;
        deleteBtn.setLayoutData(delBtnGD);

        deleteBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteRadii(currentTau);
                saveBtn.setEnabled(true);
            }
        });

        // Group to select TAUs and show forecast info.
        Group rightComp = new Group(topComp, SWT.NONE);

        GridLayout rightCompGL = new GridLayout(1, true);
        rightCompGL.marginTop = 0;
        rightCompGL.verticalSpacing = 5;
        rightComp.setLayout(rightCompGL);

        GridData tauGD = new GridData(SWT.CENTER, SWT.TOP, false, false);

        Label tauLbl = new Label(rightComp, SWT.NONE);
        tauLbl.setText("TAU");
        tauLbl.setLayoutData(tauGD);

        // Group for all TAUs
        int columns = 3;

        Composite tauComp = new Composite(rightComp, SWT.NONE);

        GridLayout tauCompGL = new GridLayout(columns, true);
        tauCompGL.horizontalSpacing = 0;
        tauCompGL.verticalSpacing = 0;
        tauCompGL.marginTop = 0;
        tauComp.setLayout(tauCompGL);

        GridData tauCompGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        tauComp.setLayoutData(tauCompGD);

        tauBtns = new EnumMap<>(AtcfTaus.class);
        for (AtcfTaus tau : workingTaus) {
            Button tauBtn = createTauBtn(tauComp, tau);
            tauBtns.put(tau, tauBtn);
        }

        // Show max. wind, speed, direction for current TAU.
        Composite infoComp = new Composite(rightComp, SWT.NONE);

        GridLayout infoCompGL = new GridLayout(1, true);
        infoCompGL.marginTop = 0;
        infoCompGL.verticalSpacing = 3;
        infoComp.setLayout(infoCompGL);

        GridData infoGD = new GridData(SWT.LEFT, SWT.TOP, false, false);
        maxWindLbl = new Label(infoComp, SWT.NONE);
        maxWindLbl.setText("Max Wind: 300 kts");
        maxWindLbl.setLayoutData(infoGD);

        GridData spdGD = new GridData(SWT.LEFT, SWT.TOP, false, false);
        spdLbl = new Label(infoComp, SWT.NONE);
        spdLbl.setText("Speed:  55 nm");
        spdLbl.setLayoutData(spdGD);

        GridData dirGD = new GridData(SWT.LEFT, SWT.TOP, false, false);
        dirLbl = new Label(infoComp, SWT.NONE);
        dirLbl.setText("Direction: 335");
        dirLbl.setLayoutData(dirGD);
    }

    /**
     * Creates the Seas Guidance section
     *
     * @param parent
     */
    protected void createSeasGuidanceComp(Composite parent) {

        Composite seasGuideComp = new Composite(parent, SWT.NONE);
        GridLayout seasGuideGL = new GridLayout(1, false);
        seasGuideGL.marginTop = 15;
        seasGuideComp.setLayout(seasGuideGL);
        GridData radiiGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        seasGuideComp.setLayoutData(radiiGD);

        seasGuidanceLbl = new Label(seasGuideComp, SWT.NONE);
        seasGuidanceLbl.setText(String.format("Seas Guidance for TAU %6d ",
                currentTau.getValue()));
        GridData waveRadiiGuidanceGD = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        seasGuidanceLbl.setLayoutData(waveRadiiGuidanceGD);

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        Composite seasListComp = new Composite(seasGuideComp, SWT.NONE);
        GridLayout seasListGL = new GridLayout(10, false);
        seasListGL.horizontalSpacing = 40;
        seasListComp.setLayout(seasListGL);
        GridData seasListGD = new GridData(SWT.CENTER, SWT.NONE, true, false);
        seasListComp.setLayoutData(seasListGD);

        Label techLbl = new Label(seasListComp, SWT.NONE);
        techLbl.setText("Tech");
        techLbl.setFont(txtFont);
        GridData techLblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        techLblGD.horizontalIndent = 5;
        techLbl.setLayoutData(techLblGD);

        Label maxWndLbl = new Label(seasListComp, SWT.NONE);
        maxWndLbl.setText("V-Max(kts)");
        maxWndLbl.setFont(txtFont);
        maxWndLbl.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label kt34Lbl = new Label(seasListComp, SWT.NONE);
        kt34Lbl.setText("34 knot radii(nm)");
        kt34Lbl.setFont(txtFont);
        GridData kt34LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt34LblGD.horizontalSpan = 4;
        kt34Lbl.setLayoutData(kt34LblGD);

        Label seasLbl = new Label(seasListComp, SWT.NONE);
        seasLbl.setText("12 ft seas radii(nm)");
        seasLbl.setFont(txtFont);
        GridData seasLblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        seasLblGD.horizontalSpan = 4;
        seasLbl.setLayoutData(seasLblGD);

        // Create the list for guidance
        seasGuideList = new List(seasListComp, SWT.NONE | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData radiiListGD = new GridData(600, 100);
        radiiListGD.horizontalAlignment = SWT.FILL;
        radiiListGD.verticalAlignment = SWT.FILL;
        radiiListGD.horizontalSpan = 10;
        seasGuideList.setLayoutData(radiiListGD);

        seasGuideList.setFont(txtFont);
    }

    /**
     * Creates the Current Seas Forecast section
     *
     * @param parent
     */
    protected void createCurrentForecastComp(Composite parent) {
        Composite seasFcstComp = new Composite(parent, SWT.NONE);
        GridLayout seasFcstGL = new GridLayout(1, false);
        seasFcstGL.marginBottom = 0;
        seasFcstComp.setLayout(seasFcstGL);
        GridData radiiGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        seasFcstComp.setLayoutData(radiiGD);

        Label seasForecastLbl = new Label(seasFcstComp, SWT.NONE);
        seasForecastLbl.setText("Current Seas Forecast");
        GridData seasForecastGD = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        seasForecastLbl.setLayoutData(seasForecastGD);

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        Composite seasListComp = new Composite(seasFcstComp, SWT.NONE);
        GridLayout seasListGL = new GridLayout(10, false);
        seasListGL.horizontalSpacing = 40;
        seasListComp.setLayout(seasListGL);

        Label techLbl = new Label(seasListComp, SWT.NONE);
        techLbl.setText("TAU");
        techLbl.setFont(txtFont);
        GridData techLblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        techLblGD.horizontalIndent = 8;
        techLbl.setLayoutData(techLblGD);

        Label maxWndLbl = new Label(seasListComp, SWT.NONE);
        maxWndLbl.setText("V-Max(kts)");
        maxWndLbl.setFont(txtFont);
        maxWndLbl.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label kt34Lbl = new Label(seasListComp, SWT.NONE);
        kt34Lbl.setText("34 knot radii(nm)");
        kt34Lbl.setFont(txtFont);
        GridData kt34LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt34LblGD.horizontalSpan = 4;
        kt34Lbl.setLayoutData(kt34LblGD);

        Label seasLbl = new Label(seasListComp, SWT.NONE);
        seasLbl.setText("12 ft seas radii(nm)");
        seasLbl.setFont(txtFont);
        GridData seasLblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        seasLblGD.horizontalSpan = 4;
        seasLbl.setLayoutData(seasLblGD);

        // Create the list for guidance
        seasForecastList = new List(seasListComp, SWT.NONE | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData seasForecastListGD = new GridData(600, 100);
        seasForecastListGD.horizontalAlignment = SWT.FILL;
        seasForecastListGD.verticalAlignment = SWT.FILL;
        seasForecastListGD.horizontalSpan = 10;
        seasForecastList.setLayoutData(seasForecastListGD);

        seasForecastList.setFont(txtFont);
    }

    /**
     * Creates the <<, save, submit, and close buttons.
     *
     * @param parent
     */
    protected void createCtrlBtns(Composite parent) {
        GridLayout ctrlBtnCompGL = new GridLayout(3, true);
        ctrlBtnCompGL.horizontalSpacing = 40;
        ctrlBtnCompGL.marginWidth = 40;
        ctrlBtnCompGL.marginTop = 0;

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        ctrlBtnComp.setLayout(ctrlBtnCompGL);
        ctrlBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // Button to save changes
        saveBtn = new Button(ctrlBtnComp, SWT.NONE);
        saveBtn.setText("Save");
        saveBtn.setEnabled(false);
        saveBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveForecast();
                saveBtn.setEnabled(false);
                submitBtn.setEnabled(true);
            }
        });

        // Button to submit changes and exits
        submitBtn = new Button(ctrlBtnComp, SWT.NONE);
        submitBtn.setText("Submit");
        submitBtn.setEnabled(false);
        submitBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        submitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * TODO this should activate “nhc.ftpfst.sh” and
                 * “nhc_ftpfiles.sh”?
                 */
                submitForecast(saveBtn.isEnabled());
                close();
            }
        });

        // Button to exit without saving.
        Button cancelBtn = new Button(ctrlBtnComp, SWT.NONE);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /*
     * Update GUI based on the selected TAU.
     *
     * @param btn Control to be set on
     */
    private void updateForTau(AtcfTaus tau) {

        currentTauLbl.setText(
                String.format(" TAU %5d     12 Ft Seas ", tau.getValue()));
        seasGuidanceLbl.setText(
                String.format("Seas Guidance for TAU %5d ", tau.getValue()));

        populateForecastInfo(tau);

        // Update 12 ft seas radii guidance for TAU
        updateSeasGuideList(tau);

        // Update current 12 ft seas forecast list.
        updateSeasForecastList();

    }

    /*
     * Create a TAU button.
     *
     * @param tauComp Composite holding all TAU buttons.
     *
     * @param tau AtcfTaus.
     *
     * @return Button
     */
    private Button createTauBtn(Composite tauComp, AtcfTaus tau) {

        org.eclipse.swt.graphics.Color clr = tauComp.getBackground();
        GridData tauBtnGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        tauBtnGD.widthHint = 50;

        Button tauBtn = new Button(tauComp, SWT.NONE);
        tauBtn.setText("" + tau.getValue());

        tauBtn.setLayoutData(tauBtnGD);
        tauBtn.setData(tau);

        tauBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                AtcfTaus preTau = currentTau;
                currentTau = (AtcfTaus) e.widget.getData();

                if (preTau != currentTau) {
                    for (Button btn : tauBtns.values()) {
                        if (btn == tauBtn) {
                            AtcfVizUtil.setActiveButtonBackground(btn);
                        } else {
                            AtcfVizUtil.setDefaultBackground(btn);
                        }
                    }
                }

                updateForTau(currentTau);
            }
        });

        if (tau == currentTau) {
            tauBtn.setSelection(true);
            AtcfVizUtil.setActiveButtonBackground(tauBtn);
        } else {
            AtcfVizUtil.setDefaultBackground(tauBtn);
        }

        return tauBtn;

    }

    /*
     * Set all TAU status based on max. wind intensity.
     */
    private void setTauBtnStatus() {

        // 12 ft seas forecast is only for TAU 3.
        for (Map.Entry<AtcfTaus, Button> entry : tauBtns.entrySet()) {
            AtcfTaus tau = entry.getKey();
            ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                    WindRadii.RADII_34_KNOT);

            boolean enable = true;
            if (tau.getValue() == 0 || rec34 == null) {
                enable = false;
            } else {
                int maxWnd = (int) rec34.getWindMax();
                int radWave = (int) rec34.getRadWave();
                if (radWave != RAD_WAVE
                        || maxWnd < WindRadii.RADII_34_KNOT.getValue()) {
                    enable = false;
                }
            }

            entry.getValue().setEnabled(enable);
        }
    }

    /*
     * Populate forecast panel with existing forecast data.
     *
     * @param tau AtcfTaus
     */
    private void populateForecastInfo(AtcfTaus tau) {

        // Set flag to ignore these changes.
        changeListener.setIgnoreChanges(true);

        // Reset to defaults.
        resetWndSpdDir();

        // Set to values in Forecast Records.
        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);

        int maxWnd = 0;
        if (rec34 == null) {
            resetWaveRadii();
        } else {
            maxWnd = (int) rec34.getWindMax();
        }

        if (rec34 != null) {
            int spd = (int) rec34.getStormSped();
            int dir = (int) rec34.getStormDrct();

            spd = (spd > 999) ? 0 : spd;
            dir = (dir >= 360) ? 0 : dir;

            maxWindLbl.setText("Max Wind: " + maxWnd + " kts");
            spdLbl.setText("Speed:   " + spd + " kts");
            dirLbl.setText("Direction:  " + dir);

            maxWindLbl.getParent().getParent().pack();

            String radQuad = rec34.getRadWaveQuad();
            boolean isCirc = radQuad.equals(FULL_CIRCLE);

            int[] waveRadii = rec34.getWaveRadii();
            radiiQuadBtns.get(0).setSelection(isCirc);
            radiiQuadBtns.get(1).setSelection(!isCirc);
            updateRadiiCombo(radiiValueCmbs.get(0), waveRadii[0]);
            if (!isCirc) {
                for (int qq = 1; qq < 4; qq++) {
                    updateRadiiCombo(radiiValueCmbs.get(qq), waveRadii[qq]);
                }
            }
        }

        // Reset flag to check future changes on GUI.
        changeListener.setIgnoreChanges(false);
        changeListener.setChangesUnsaved(false);
    }

    /*
     * Resets max wind/spd/dir display to 0.
     */
    private void resetWndSpdDir() {
        maxWindLbl.setText("Max Wind:  0 kts");
        spdLbl.setText("Speed:     0 kts");
        dirLbl.setText("Direction: 0");
    }

    /*
     * Resets all 12 ft seas radii selection to default.
     */
    private void resetWaveRadii() {
        radiiQuadBtns.get(0).setSelection(false);
        radiiQuadBtns.get(1).setSelection(true);
        for (CCombo cmb : radiiValueCmbs) {
            cmb.select(0);
        }
    }

    /*
     * Update the information in the 12 ft seas wave guidance list
     *
     * Note: so far, only OFCL has 12 ft seas forecast at TAU 3!
     *
     * @param tau AtcfTaus
     */
    private void updateSeasGuideList(AtcfTaus tau) {

        seasGuideList.removeAll();

        /*
         * Find records for each selected aids and current TAU (forecast hour).
         * Then format/add to the guidance list.
         */
        ADeckRecord rec34kt = null;
        for (String tech : selectedObjAids) {

            rec34kt = null;

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%4s", tech));

            for (ADeckRecord rec : currentADeckRecords) {
                String aidCode = rec.getTechnique();
                int fcstHr = rec.getFcstHour();
                int rad = (int) rec.getRadWind();

                if (fcstHr == tau.getValue() && aidCode.equalsIgnoreCase(tech)
                        && rad == WindRadii.RADII_34_KNOT.getValue()) {
                    rec34kt = rec;
                }
            }

            // Add techniques that have max wind >= 34 kt for guidance.
            int maxWnd = 0;
            if (rec34kt != null) {
                maxWnd = (int) rec34kt.getWindMax();

                if (maxWnd >= WindRadii.RADII_34_KNOT.getValue()) {
                    sb.append(String.format("%12s", maxWnd));

                    int[] windRadii = rec34kt.getWindRadii();

                    sb.append(String.format("%16s", windRadii[0]));
                    sb.append(String.format("%5s", windRadii[1]));
                    sb.append(String.format("%5s", windRadii[2]));
                    sb.append(String.format("%5s", windRadii[3]));

                    int radWave = (int) rec34kt.getRadWave();
                    if (radWave == RAD_WAVE) {
                        int[] waveRadii = rec34kt.getWaveRadii();
                        sb.append(String.format("%10s", waveRadii[0]));
                        sb.append(String.format("%5s", waveRadii[1]));
                        sb.append(String.format("%5s", waveRadii[2]));
                        sb.append(String.format("%5s", waveRadii[3]));
                    }

                    seasGuideList.add(sb.toString());
                }
            }
        }
    }

    /*
     * Update info in forecast panel with existing forecast data.
     */
    private void updateSeasForecastList() {

        seasForecastList.removeAll();

        int ii = 0;
        for (AtcfTaus tau : workingTaus) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%3s", tau.getValue()));

            ForecastTrackRecord rec34kt = getForecastByTauNRadii(tau,
                    WindRadii.RADII_34_KNOT);

            if (rec34kt != null) {
                int maxWnd = (int) rec34kt.getWindMax();
                sb.append(String.format("%13s", maxWnd));

                int[] windRadii = rec34kt.getWindRadii();
                sb.append(String.format("%16s", windRadii[0]));
                sb.append(String.format("%5s", windRadii[1]));
                sb.append(String.format("%5s", windRadii[2]));
                sb.append(String.format("%5s", windRadii[3]));

                int radWave = (int) rec34kt.getRadWave();
                if (radWave == RAD_WAVE) {
                    int[] waveRadii = rec34kt.getWaveRadii();
                    sb.append(String.format("%10s", waveRadii[0]));
                    sb.append(String.format("%5s", waveRadii[1]));
                    sb.append(String.format("%5s", waveRadii[2]));
                    sb.append(String.format("%5s", waveRadii[3]));
                }
            }

            seasForecastList.add(sb.toString());

            if (tau == currentTau) {
                seasForecastList.select(ii);
            }

            ii++;
        }
    }

    /*
     * Delete 12 ft seas wave radii values for the given TAU.
     *
     * @param tau AtcfTaus
     */
    private void deleteRadii(AtcfTaus tau) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                WindRadii.RADII_64_KNOT);

        updateRecordWaveRadii(rec34, 0);
        updateRecordWaveRadii(rec50, 0);
        updateRecordWaveRadii(rec64, 0);

        // Update GUI.
        resetWaveRadii();
        updateSeasForecastList();

    }

    /*
     * Update changes from 12 ft seas wave radii CComboes.
     *
     * Note: all three WindRadii (34/50/64) have the same wave radii.
     *
     * @param tau AtcfTaus
     *
     * @param wid CCombo that changed.
     */
    private void updateWaveRadiiValue(AtcfTaus tau, Widget wid) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                WindRadii.RADII_64_KNOT);

        CCombo cmb = (CCombo) wid;
        if (cmb.isEnabled() && rec34 != null) {
            float radVal = 0.0F;
            try {
                radVal = Float.parseFloat(cmb.getText());
            } catch (Exception ee) {
                // Use default 0.
                logger.warn("ForecastSeasDialog: Wind radii defaults to 0.",
                        ee);
            }

            // Make sure rounding it to nearest 5.
            int radii = AtcfDataUtil.roundToNearest(radVal, 5);

            int which = (int) (wid.getData());

            // Update Tau 0 records.
            updateTau0WaveRadii(radii, which);

            // Update 12 ft seas wave radii in records.
            if (!isCircle) {
                // By quadrant
                setRecordWaveRadi(rec34, radii, which);
                setRecordWaveRadi(rec50, radii, which);
                setRecordWaveRadi(rec64, radii, which);
            } else {
                // Circle - all the same.
                updateRecordWaveRadii(rec34, radii);
                updateRecordWaveRadii(rec50, radii);
                updateRecordWaveRadii(rec64, radii);

                // Update on all wave radii CCombos.
                for (CCombo comb : radiiValueCmbs) {
                    updateRadiiCombo(comb, radii);
                }
            }

            updateSeasForecastList();
        }
    }

    /*
     * Update changes from 12 ft seas wave radii circle/quad radio buttons.
     *
     * Note: all three WindRadii (34/50/64) have the same wave radii.
     *
     * @param tau AtcfTaus
     *
     * @param btn Button.
     */
    private void updateWaveRadiiQuad(AtcfTaus tau, Button btn) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                WindRadii.RADII_64_KNOT);

        if (btn.isEnabled() && rec34 != null) {

            int radVal = (int) rec34.getQuad1WaveRad();
            String waveQuad = rec34.getRadWaveQuad();

            if (isCircle) {

                // Use quad1 12 ft seas wave radii for all other quads.
                updateRecordWaveRadii(rec34, radVal);
                updateRecordWaveRadii(rec50, radVal);
                updateRecordWaveRadii(rec64, radVal);

                // Update on wave radii CCombos.
                for (CCombo cmb : radiiValueCmbs) {
                    updateRadiiCombo(cmb, radVal);
                }

            } else {
                if (waveQuad.equals(FULL_CIRCLE)) {
                    rec34.setRadWaveQuad(DEFAULT_QUAD);
                }

                if (rec50 != null && waveQuad.equals(FULL_CIRCLE)) {
                    rec50.setRadWaveQuad(DEFAULT_QUAD);
                }

                if (rec64 != null && waveQuad.equals(FULL_CIRCLE)) {
                    rec64.setRadWaveQuad(DEFAULT_QUAD);
                }
            }

            updateTau0WaveRadii(radVal, -1);

            updateSeasForecastList();
        }
    }

    /**
     * Apply edited 12 ft seas wave radii into forecast records for saving.
     */
    @Override
    protected void applyChanges() {
        for (Map.Entry<ForecastTrackRecord, ForecastTrackRecord> entry : fcstTrackEditMap
                .entrySet()) {
            ForecastTrackRecord newRec = entry.getKey();
            ForecastTrackRecord origRec = entry.getValue();
            origRec.setRadWave(newRec.getRadWave());
            origRec.setRadWaveQuad(newRec.getRadWaveQuad());
            origRec.setQuad1WaveRad(newRec.getQuad1WaveRad());
            origRec.setQuad2WaveRad(newRec.getQuad2WaveRad());
            origRec.setQuad3WaveRad(newRec.getQuad3WaveRad());
            origRec.setQuad4WaveRad(newRec.getQuad4WaveRad());
        }
    }

    /*
     * Set a record's wave radii for all quadrant to a same value.
     *
     * @param rec ForecastTrackRecord
     *
     * @param radVal wave radius
     */
    private void updateRecordWaveRadii(ForecastTrackRecord rec, int radVal) {
        if (rec != null) {
            if (isCircle) {
                rec.setRadWaveQuad(FULL_CIRCLE);
            }

            rec.setQuad1WaveRad(radVal);
            rec.setQuad2WaveRad(radVal);
            rec.setQuad3WaveRad(radVal);
            rec.setQuad4WaveRad(radVal);
        }
    }

    /**
     * Calls super.updateRadiiCombo(), ignoring the resulting events.
     *
     * @param combo
     * @param index
     */
    @Override
    protected void updateRadiiCombo(CCombo cmb, int radiiValue) {
        boolean prevIngore = ignoreUIChanges;
        ignoreUIChanges = true;
        try {
            super.updateRadiiCombo(cmb, radiiValue);
        } finally {
            ignoreUIChanges = prevIngore;
        }
    }

    /*
     * Update changes from 12 ft seas wave radii into TAU 0 records.
     *
     * @param radii New wave radii
     *
     * @param Which Which wave radii combo trigger the changes.
     *
     */
    private void updateTau0WaveRadii(int radii, int which) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(AtcfTaus.TAU0,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(AtcfTaus.TAU0,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(AtcfTaus.TAU0,
                WindRadii.RADII_64_KNOT);

        if (isCircle) {
            updateRecordWaveRadii(rec34, radii);
            updateRecordWaveRadii(rec50, radii);
            updateRecordWaveRadii(rec64, radii);
        } else {
            // By quadrant
            setRecordWaveRadi(rec34, radii, which);
            setRecordWaveRadi(rec50, radii, which);
            setRecordWaveRadi(rec64, radii, which);
        }
    }

    /*
     * Set wave radii in a forecast record.
     *
     * @param rec ForecastTrackRecord
     *
     * @param radii Radii value
     *
     * @param which Quadrant (1,2,3,4)
     *
     */
    private void setRecordWaveRadi(ForecastTrackRecord rec, int radii,
            int which) {
        if (rec != null) {

            String waveRad = rec.getRadWaveQuad();
            if (waveRad.equals(FULL_CIRCLE)) {
                rec.setRadWaveQuad(DEFAULT_QUAD);
            }

            if (which == 0) {
                rec.setQuad1WaveRad(radii);
            } else if (which == 1) {
                rec.setQuad2WaveRad(radii);
            } else if (which == 2) {
                rec.setQuad3WaveRad(radii);
            } else if (which == 3) {
                rec.setQuad4WaveRad(radii);
            }
        }
    }

}
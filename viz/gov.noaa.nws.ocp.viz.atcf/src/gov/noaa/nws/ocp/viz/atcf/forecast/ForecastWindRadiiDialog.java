/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.dialogs.MessageDialog;
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
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackRadiiOptionsDialog;

/**
 * Dialog for Forecast=>Forecast Wind Radii.
 * 
 * Note: Only wind radii could be edited via this tool. It will not change max
 * wind, storm speed and direction, and other data and no new forecast records
 * will be created as well.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 25, 2020 75391      jwu        initial creation
 * May 28, 2020 78027      jwu        Update to track changes etc.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Mar 24, 2021 88727      mporricelli Disable wind radii for some taus;
 *                                     add checkWindRadii qc
 * May 27, 2021 91757      jwu         Fix "Use RVCN for all TAUs".
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ForecastWindRadiiDialog extends ForecastDialog
        implements IMapChangeListener<RecordKey, ForecastTrackRecord> {

    private static final String QUAD_RADII_FORMAT = "%8s%4s%4s%4s";

    // Label at the top to show the current TAU
    private Label currentTauLbl;

    // Buttons for each TAU
    private Map<AtcfTaus, Button> radiiTauBtns;

    // Buttons to activate wind radii graph.
    private Map<WindRadii, Button> radiiGraphBtns;

    // Label for wind radii guidance, including the current TAU
    private Label windRadiiGuidanceLbl;

    private Label maxWindLbl;

    private Label spdLbl;

    private Label dirLbl;

    // Forecast guidance list
    private List radiiGuideList;

    // List for current wind radii forecast
    private List radiiForecastList;

    // Buttons and CCombos for wind radii.
    private Map<WindRadii, java.util.List<Button>> radiiQuadBtns;

    private Map<WindRadii, java.util.List<CCombo>> radiiValueCmbs;

    private WindRadiiValidation windRadiiValidation;

    // Observable "current tau" value shared with graph dialog
    private WritableValue<AtcfTaus> observableCurrentTau = new WritableValue<>();

    // Associated graph dialog
    private WindRadiiGraphDialog radiiGraphDlg;

    // Set while updating CCombo controls
    private boolean ignoreUIChanges;

    /**
     * Constructor
     * 
     * @param parent
     *            parent shell
     */
    public ForecastWindRadiiDialog(Shell parent, Storm curStorm,
            Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData) {

        super(parent, curStorm, ForecastType.WIND_RADII, fcstTrackData);
        observableCurrentTau.setValue(currentTau);
        observableCurrentTau.addValueChangeListener(
                event -> currentTauChanged(event.diff.getNewValue()));
        windRadiiValidation = new WindRadiiValidation(fcstTrackRecordMap);
    }

    /**
     * Initializes the components.
     * 
     * @param parent
     */
    @Override
    protected void populateScrollComponent(Composite parent) {

        // Maps of buttons and CCombos for wind radii.
        radiiQuadBtns = new EnumMap<>(WindRadii.class);
        radiiQuadBtns.put(WindRadii.RADII_34_KNOT, new ArrayList<>());
        radiiQuadBtns.put(WindRadii.RADII_50_KNOT, new ArrayList<>());
        radiiQuadBtns.put(WindRadii.RADII_64_KNOT, new ArrayList<>());

        radiiValueCmbs = new EnumMap<>(WindRadii.class);
        radiiValueCmbs.put(WindRadii.RADII_34_KNOT, new ArrayList<>());
        radiiValueCmbs.put(WindRadii.RADII_50_KNOT, new ArrayList<>());
        radiiValueCmbs.put(WindRadii.RADII_64_KNOT, new ArrayList<>());

        // Create dialog.
        createContents(parent);
    }

    /**
     * Create contents.
     * 
     * @param parent
     */
    protected void createContents(Composite parent) {

        fcstTrackRecordMap.addMapChangeListener(this);

        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout mainCompGL = new GridLayout(1, false);
        mainCompGL.verticalSpacing = 5;
        mainComp.setLayout(mainCompGL);
        GridData mainGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComp.setLayoutData(mainGD);

        currentTauLbl = new Label(mainComp, SWT.CENTER);
        currentTauLbl.setText("TAU 12");
        GridData mainLblGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        currentTauLbl.setLayoutData(mainLblGD);

        createWindRadiiInfoComp(mainComp);
        createWindRadiiGuidanceComp(mainComp);
        createCurrentForecastComp(mainComp);
        createCtrlBtns(mainComp);

        setTauBtnStatus();
        currentTauChanged(getCurrentTau());
    }

    /**
     * Creates the top section of the GUI
     * 
     * @param parent
     */
    protected void createWindRadiiInfoComp(Composite parent) {
        Group topComp = new Group(parent, SWT.NONE);
        GridLayout topCompGL = new GridLayout(2, false);
        topCompGL.horizontalSpacing = 30;
        topCompGL.marginTop = 0;
        topComp.setLayout(topCompGL);

        Composite leftComp = new Composite(topComp, SWT.NONE);
        GridLayout leftCompGL = new GridLayout(1, true);
        leftCompGL.marginTop = 0;
        leftCompGL.verticalSpacing = 15;
        leftComp.setLayout(leftCompGL);

        // Composite for 34kt/50kt/64kt wind radii selections
        Composite radiiComp = new Composite(leftComp, SWT.NONE);
        GridLayout radiiCompGL = new GridLayout(6, false);
        radiiCompGL.verticalSpacing = 10;
        radiiComp.setLayout(radiiCompGL);

        // Row for labeling 34kt/50kt/64kt wind radii
        Label spaceLbl = new Label(radiiComp, SWT.NONE);
        spaceLbl.setText("");
        GridData lblGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        lblGD.horizontalSpan = 2;
        spaceLbl.setLayoutData(lblGD);

        for (int i = 0; i < 4; i++) {
            Label quadLbl = new Label(radiiComp, SWT.NONE);
            quadLbl.setText(quadrantLabels[i]);
        }

        // Row for 34kt/50kt/64kt wind radii
        for (WindRadii wndRad : WindRadii.values()) {
            if (wndRad.getValue() > 0) {
                Label ktLbl = new Label(radiiComp, SWT.NONE);
                ktLbl.setText(wndRad.getName() + ":");

                Group ktQuadGrp = new Group(radiiComp, SWT.NONE);
                GridLayout ktQuadGrpGL = new GridLayout(2, true);
                ktQuadGrpGL.horizontalSpacing = 2;
                ktQuadGrp.setLayout(ktQuadGrpGL);

                Button ktCircleBtn = new Button(ktQuadGrp, SWT.RADIO);
                ktCircleBtn.setText("Circle");
                ktCircleBtn.setData(true);
                radiiQuadBtns.get(wndRad).add(ktCircleBtn);
                ktCircleBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (((Button) e.widget).getSelection()) {
                            updateWindRadiiQuad(getCurrentTau(), wndRad, true);
                            if (!changeListener.isIgnoreChanges()) {
                                changeListener.setChangesUnsaved(true);
                            }
                        }
                    }
                });

                Button ktQuadBtn = new Button(ktQuadGrp, SWT.RADIO);
                ktQuadBtn.setText("Quad");
                ktQuadBtn.setData(false);
                ktQuadBtn.setSelection(false);
                radiiQuadBtns.get(wndRad).add(ktQuadBtn);
                ktQuadBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (((Button) e.widget).getSelection()) {
                            updateWindRadiiQuad(getCurrentTau(), wndRad, false);
                            if (!changeListener.isIgnoreChanges()) {
                                changeListener.setChangesUnsaved(true);
                            }
                        }
                    }
                });

                GridData radiiDropGD = new GridData(SWT.CENTER, SWT.NONE, false,
                        false);
                radiiDropGD.widthHint = 63;

                for (int kk = 0; kk < 4; kk++) {
                    CCombo ktCombo = new CCombo(radiiComp, SWT.NONE);
                    ktCombo.setItems(radiiEntries);
                    ktCombo.select(0);
                    ktCombo.setData(kk);
                    ktCombo.setLayoutData(radiiDropGD);
                    radiiValueCmbs.get(wndRad).add(ktCombo);

                    ktCombo.addVerifyListener(
                            verifyListener.getRadiiCmbVerifyListener());

                    ktCombo.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            if (ignoreUIChanges) {
                                return;
                            }
                            updateWindRadiiValue(getCurrentTau(), wndRad,
                                    e.widget);

                            // Update flag to indicate new changes.
                            if (!changeListener.isIgnoreChanges()) {
                                changeListener.setChangesUnsaved(true);
                            }
                        }
                    });

                    ktCombo.addModifyListener(e -> {
                        if (ignoreUIChanges) {
                            return;
                        }
                        // Update flag to indicate new changes.
                        if (!changeListener.isIgnoreChanges()) {
                            changeListener.setChangesUnsaved(true);
                        }

                        updateWindRadiiValue(getCurrentTau(), wndRad, e.widget);
                    });
                }
            }
        }

        // Group for actions
        Composite actionComp = new Composite(leftComp, SWT.NONE);
        GridLayout actionCompGL = new GridLayout(3, true);
        actionCompGL.horizontalSpacing = 10;
        actionCompGL.verticalSpacing = 15;
        actionComp.setLayout(actionCompGL);

        int wd = 145;
        GridData actionCompGD = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        actionComp.setLayoutData(actionCompGD);

        for (WindRadiiAction action : WindRadiiAction.values()) {
            Button actionBtn = new Button(actionComp, SWT.NONE);
            actionBtn.setText(action.getName());

            GridData actionBtnGD = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false);
            actionBtnGD.widthHint = wd;
            actionBtn.setLayoutData(actionBtnGD);

            actionBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    performRadiiAction(action);
                }
            });

        }

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
        int rows = 2;

        Composite tauComp = new Composite(rightComp, SWT.NONE);

        GridLayout tauCompGL = new GridLayout(rows, true);
        tauCompGL.horizontalSpacing = 10;
        tauCompGL.verticalSpacing = 0;
        tauCompGL.marginTop = 0;
        tauComp.setLayout(tauCompGL);

        GridData tauCompGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        tauComp.setLayoutData(tauCompGD);

        radiiTauBtns = new EnumMap<>(AtcfTaus.class);

        int middle = workingTaus.size() / rows + workingTaus.size() % rows;

        for (int ii = 0; ii < middle; ii++) {

            AtcfTaus tau = workingTaus.get(ii);
            Button tauBtn = createTauBtn(tauComp, tau);
            radiiTauBtns.put(tau, tauBtn);

            if ((ii + middle) < workingTaus.size()) {
                AtcfTaus tau1 = workingTaus.get(ii + middle);
                Button tauBtn1 = createTauBtn(tauComp, tau1);
                radiiTauBtns.put(tau1, tauBtn1);
            }
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

        // Composite for "Graph"
        Composite graphComp = new Composite(parent, SWT.NONE);

        GridLayout graphGL = new GridLayout(4, false);

        GridData graphGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        graphGL.horizontalSpacing = 30;
        graphGL.marginTop = 10;
        graphComp.setLayout(graphGL);
        graphComp.setLayoutData(graphGD);

        Label graphLbl = new Label(graphComp, SWT.NONE);
        graphLbl.setText("Graph/Select radii (radial graph):     ");

        GridData graphBtnGD = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        graphBtnGD.widthHint = 100;

        radiiGraphBtns = new EnumMap<>(WindRadii.class);

        for (WindRadii wndRad : WindRadii.values()) {
            if (wndRad.getValue() > 0) {
                Button ktGraphBtn = new Button(graphComp, SWT.NONE);
                ktGraphBtn.setText(wndRad.getName() + " ... ");
                ktGraphBtn.setData(wndRad);
                ktGraphBtn.setLayoutData(graphBtnGD);
                ktGraphBtn.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        showGraphDialog(wndRad);
                    }
                });

                radiiGraphBtns.put(wndRad, ktGraphBtn);
            }
        }
    }

    /**
     * Creates the Wind Radii Guidance section
     * 
     * @param parent
     */
    protected void createWindRadiiGuidanceComp(Composite parent) {

        Composite windRadiiGuideComp = new Composite(parent, SWT.NONE);
        GridLayout windRadiiGuideGL = new GridLayout(1, false);
        windRadiiGuideGL.marginTop = 15;
        windRadiiGuideComp.setLayout(windRadiiGuideGL);
        GridData radiiGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        windRadiiGuideComp.setLayoutData(radiiGD);

        windRadiiGuidanceLbl = new Label(windRadiiGuideComp, SWT.NONE);
        windRadiiGuidanceLbl.setText(
                "Wind Radii Guidance for TAU " + getCurrentTau().getValue());
        GridData windRadiiGuidanceGD = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        windRadiiGuidanceLbl.setLayoutData(windRadiiGuidanceGD);

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        Composite windRadiiListComp = new Composite(windRadiiGuideComp,
                SWT.NONE);
        GridLayout windRadiiListGL = new GridLayout(14, false);
        windRadiiListGL.horizontalSpacing = 20;
        windRadiiListComp.setLayout(windRadiiListGL);
        GridData windRadiiListGD = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        windRadiiListComp.setLayoutData(windRadiiListGD);

        Label techLbl = new Label(windRadiiListComp, SWT.NONE);
        techLbl.setText("Tech");
        techLbl.setFont(txtFont);
        techLbl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label maxWndLbl = new Label(windRadiiListComp, SWT.NONE);
        maxWndLbl.setText("V-Max(kts)");
        maxWndLbl.setFont(txtFont);
        maxWndLbl.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label kt34Lbl = new Label(windRadiiListComp, SWT.NONE);
        kt34Lbl.setText("34 knot radii(nm)");
        kt34Lbl.setFont(txtFont);
        GridData kt34LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt34LblGD.horizontalSpan = 4;
        kt34Lbl.setLayoutData(kt34LblGD);

        Label kt50Lbl = new Label(windRadiiListComp, SWT.NONE);
        kt50Lbl.setText("50 knot radii(nm)");
        kt50Lbl.setFont(txtFont);
        GridData kt50LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt50LblGD.horizontalSpan = 4;
        kt50Lbl.setLayoutData(kt50LblGD);

        Label kt64Lbl = new Label(windRadiiListComp, SWT.NONE);
        kt64Lbl.setText("64 knot radii(nm)");
        kt64Lbl.setFont(txtFont);
        GridData kt64LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt64LblGD.horizontalSpan = 4;
        kt64Lbl.setLayoutData(kt64LblGD);

        // Create the list for guidance
        radiiGuideList = new List(windRadiiListComp, SWT.NONE | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData radiiListGD = new GridData(615, 120);
        radiiListGD.horizontalAlignment = SWT.FILL;
        radiiListGD.verticalAlignment = SWT.FILL;
        radiiListGD.horizontalSpan = 14;
        radiiGuideList.setLayoutData(radiiListGD);

        radiiGuideList.setFont(txtFont);
    }

    /**
     * Creates the Current Forecast section
     * 
     * @param parent
     */
    protected void createCurrentForecastComp(Composite parent) {
        Composite windRadiiGuideComp = new Composite(parent, SWT.NONE);
        GridLayout windRadiiGuideGL = new GridLayout(1, false);
        windRadiiGuideGL.marginBottom = 0;
        windRadiiGuideComp.setLayout(windRadiiGuideGL);
        GridData radiiGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        windRadiiGuideComp.setLayoutData(radiiGD);

        Label wRadiiGuidanceLbl = new Label(windRadiiGuideComp, SWT.NONE);
        wRadiiGuidanceLbl.setText("Current Forecast");
        GridData windRadiiGuidanceGD = new GridData(SWT.CENTER, SWT.NONE, true,
                false);
        wRadiiGuidanceLbl.setLayoutData(windRadiiGuidanceGD);

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        Composite windRadiiListComp = new Composite(windRadiiGuideComp,
                SWT.NONE);
        GridLayout windRadiiListGL = new GridLayout(14, false);
        windRadiiListGL.horizontalSpacing = 20;
        windRadiiListComp.setLayout(windRadiiListGL);

        Label techLbl = new Label(windRadiiListComp, SWT.NONE);
        techLbl.setText("TAU");
        techLbl.setFont(txtFont);
        techLbl.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label maxWndLbl = new Label(windRadiiListComp, SWT.NONE);
        maxWndLbl.setText("V-Max(kts)");
        maxWndLbl.setFont(txtFont);
        maxWndLbl.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label kt34Lbl = new Label(windRadiiListComp, SWT.NONE);
        kt34Lbl.setText("34 knot radii(nm)");
        kt34Lbl.setFont(txtFont);
        GridData kt34LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt34LblGD.horizontalSpan = 4;
        kt34Lbl.setLayoutData(kt34LblGD);

        Label kt50Lbl = new Label(windRadiiListComp, SWT.NONE);
        kt50Lbl.setText("50 knot radii(nm)");
        kt50Lbl.setFont(txtFont);
        GridData kt50LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt50LblGD.horizontalSpan = 4;
        kt50Lbl.setLayoutData(kt50LblGD);

        Label kt64Lbl = new Label(windRadiiListComp, SWT.NONE);
        kt64Lbl.setText("64 knot radii(nm)");
        kt64Lbl.setFont(txtFont);
        GridData kt64LblGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        kt64LblGD.horizontalSpan = 4;
        kt64Lbl.setLayoutData(kt64LblGD);

        // Create the list for forecast
        radiiForecastList = new List(windRadiiListComp, SWT.NONE | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData radiiForecastListGD = new GridData(615, 120);
        radiiForecastListGD.horizontalAlignment = SWT.FILL;
        radiiForecastListGD.verticalAlignment = SWT.FILL;
        radiiForecastListGD.horizontalSpan = 14;
        radiiForecastList.setLayoutData(radiiForecastListGD);

        radiiForecastList.setFont(txtFont);
    }

    /**
     * Creates the <<, save, submit, and close buttons.
     * 
     * @param parent
     */
    protected void createCtrlBtns(Composite parent) {
        GridLayout ctrlBtnCompGL = new GridLayout(4, true);
        ctrlBtnCompGL.horizontalSpacing = 40;
        ctrlBtnCompGL.marginWidth = 40;
        ctrlBtnCompGL.marginTop = 0;

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        ctrlBtnComp.setLayout(ctrlBtnCompGL);
        ctrlBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        /*
         * Button to activate "Forecast Intensity". It also saves changes in
         * wind radii and exits "Forecast Wind Radii".
         */
        Button prevBtn = new Button(ctrlBtnComp, SWT.NONE);
        prevBtn.setText("<<");
        prevBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        prevBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                saveForecast();
                close();

                // Start Forecast Intensity
                AtcfVizUtil.executeCommand(
                        "gov.noaa.nws.ocp.viz.atcf.ui.intensityForecast", null,
                        null);

            }
        });

        // Button to save changes
        Button saveBtn = new Button(ctrlBtnComp, SWT.NONE);
        saveBtn.setText("Save");
        saveBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveForecast();
            }
        });

        // Button to submit changes and exits
        Button submitBtn = new Button(ctrlBtnComp, SWT.NONE);
        submitBtn.setText("Submit");
        submitBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        submitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
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

    /**
     * Find if a WindRadii is circle or quadrant.
     * 
     * @param wndRad
     *            WindRadii
     * @return boolean true - quadrant; false - circle.
     * 
     */
    public boolean isKtQuad(WindRadii wndRad) {
        return radiiQuadBtns.get(wndRad).get(1).getSelection();
    }

    /**
     * Called in response to a forecast record change. Does not handle
     * adds/removes which should not happen for this dialog.
     */
    @Override
    public void handleMapChange(
            MapChangeEvent<? extends RecordKey, ? extends ForecastTrackRecord> event) {
        // Update information.
        for (RecordKey key : event.diff.getChangedKeys()) {
            if (key.getTau() == getCurrentTau().getValue()) {
                populateForecastInfo(getCurrentTau());
                break;
            }
        }

        // Update flag to indicate new changes.
        changeListener.setChangesUnsaved(true);
        updateForecastList();
    }

    /*
     * Update GUI based on the selected TAU.
     *
     * @param btn Control to be set on
     * 
     * @param clr Color
     */
    private void updateForTau(AtcfTaus tau) {

        int currentTau = tau.getValue();
        currentTauLbl.setText("TAU " + currentTau);
        windRadiiGuidanceLbl
                .setText("Wind Radii Guidance for TAU " + currentTau);
        windRadiiGuidanceLbl.pack();

        populateForecastInfo(tau);

        // Update wind radii guidance for TAU
        updateRadiiGuideList(tau);

        // Update current forecast.
        updateForecastList();

        // Update "34 kt"/"50 kt"/"64 kt" buttons
        updateForRadiiGraphBtns(tau);
    }

    /*
     * Enable/disable radii graph buttons for the selected TAU.
     *
     * @param btn Control to be set on
     * 
     * @param clr Color
     */
    private void updateForRadiiGraphBtns(AtcfTaus tau) {

        // Update "34 kt"/"50 kt"/"64 kt" buttons
        for (Map.Entry<WindRadii, Button> entry : radiiGraphBtns.entrySet()) {
            entry.getValue().setEnabled(windRadiiValidation
                    .isWindRadiiAllowed(entry.getKey(), tau));
        }
    }

    /*
     * Create an array for the radius of specified wind intensity: 0 through
     * 1200 nm with interval of 5 nm.
     *
     * @param tauComp Composite holding all TAU buttons.
     * 
     * @param tau AtcfTaus.
     *
     * return Button
     */
    private Button createTauBtn(Composite tauComp, AtcfTaus tau) {

        GridData tauBtnGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        tauBtnGD.widthHint = 50;

        Button tauBtn = new Button(tauComp, SWT.NONE);
        tauBtn.setText("" + tau.getValue());

        tauBtn.setLayoutData(tauBtnGD);
        tauBtn.setData(tau);

        tauBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                boolean cont = false;

                // Check if there are any missing wind radii.
                cont = checkWindRadii();
                // Continue if no missing wind radii or user opted to continue
                if (cont) {
                    observableCurrentTau
                            .setValue((AtcfTaus) e.widget.getData());
                }
            }
        });

        return tauBtn;

    }

    /*
     * Enable or disable TAU buttons based on rules and forecast state.
     */
    private void setTauBtnStatus() {

        for (Map.Entry<AtcfTaus, Button> entry : radiiTauBtns.entrySet()) {
            entry.getValue().setEnabled(
                    windRadiiValidation.isTauAllowed(entry.getKey()));
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

        if (rec34 != null) {
            int spd = (int) rec34.getStormSped();
            int dir = (int) rec34.getStormDrct();

            /*
             * Dir/spd are updated in location & intensity forecast.
             */
            spd = (spd > 999) ? 0 : spd;
            dir = (dir >= 360) ? 0 : dir;

            maxWnd = (int) rec34.getWindMax();
            maxWindLbl.setText("Max Wind: " + maxWnd + " kts");
            spdLbl.setText("Speed:   " + spd + " kts");
            dirLbl.setText("Direction:  " + dir);

            maxWindLbl.getParent().getParent().pack();
        }

        setWindRadiiStatus(tau);

        boolean restIsMissing = false;

        for (WindRadii windRadii : WindRadii.values()) {
            if (windRadii.getValue() <= 0) {
                continue;
            }

            ForecastTrackRecord rec = getForecastByTauNRadii(tau, windRadii);

            if (!restIsMissing && rec != null) {
                String radQuad = rec.getRadWindQuad();
                boolean isCircle = radQuad.equals(FULL_CIRCLE);
                int[] quadRadii = { (int) rec.getQuad1WindRad(),
                        (int) rec.getQuad2WindRad(),
                        (int) rec.getQuad3WindRad(),
                        (int) rec.getQuad4WindRad() };

                radiiQuadBtns.get(windRadii).get(0).setSelection(isCircle);
                radiiQuadBtns.get(windRadii).get(1).setSelection(!isCircle);

                for (int q = 0; q < 4; ++q) {
                    updateRadiiCombo(radiiValueCmbs.get(windRadii).get(q),
                            quadRadii[q]);
                }
            } else {
                /*
                 * Do not update later rows. Assumes 64kt exists only if 50kt
                 * exists and 50kt exists only if 34kt exists.
                 */
                restIsMissing = true;
                resetWindRadii(windRadii);
            }
        }

        // Set flag to track future changes.
        changeListener.setIgnoreChanges(false);
    }

    /*
     * Resets all wind radii selections to defaults.
     */
    private void resetWndSpdDir() {

        maxWindLbl.setText("Max Wind:  0 kts");
        spdLbl.setText("Speed:     0 kts");
        dirLbl.setText("Direction: 0");
    }

    /*
     * Resets all wind radii selections to default.
     */
    private void resetWindRadii() {

        for (WindRadii radii : WindRadii.values()) {
            if (radii.getValue() > 0) {
                radiiQuadBtns.get(radii).get(0).setSelection(false);
                radiiQuadBtns.get(radii).get(1).setSelection(true);
                for (CCombo cmb : radiiValueCmbs.get(radii)) {
                    selectCombo(cmb, 0);
                }
            }
        }
    }

    /*
     * Resets a given wind radii selection to default.
     * 
     * @param radii WindRadii to be reset.
     */
    private void resetWindRadii(WindRadii radii) {
        if (radii.getValue() > 0) {
            radiiQuadBtns.get(radii).get(0).setSelection(false);
            radiiQuadBtns.get(radii).get(1).setSelection(true);
            for (CCombo cmb : radiiValueCmbs.get(radii)) {
                selectCombo(cmb, 0);
            }
        }
    }

    /*
     * Enable/disable wind radii widgets based on max wind speed and tau
     *
     * @param tau AtcfTaus
     */
    private void setWindRadiiStatus(AtcfTaus tau) {

        boolean tauAllowed = windRadiiValidation.isTauAllowed(tau);

        // Assumes radiiQuadBtns.keySet().equals(radiiValueCmbs.keySet())
        for (Map.Entry<WindRadii, java.util.List<Button>> entry : radiiQuadBtns
                .entrySet()) {
            WindRadii radii = entry.getKey();
            boolean radiiAllowed = tauAllowed
                    && windRadiiValidation.isWindRadiiAllowed(radii, tau);
            ForecastTrackRecord rec = getForecastByTauNRadii(tau, radii);
            boolean isCircle = rec != null
                    && FULL_CIRCLE.equals(rec.getRadWindQuad());
            boolean firstQuad = true;

            entry.getValue().get(0).setEnabled(radiiAllowed);
            entry.getValue().get(1).setEnabled(radiiAllowed);
            for (CCombo cmb : radiiValueCmbs.get(radii)) {
                cmb.setEnabled(radiiAllowed && (firstQuad || !isCircle));
                firstQuad = false;
            }
        }

    }

    /*
     * Update the information in the intensity guide list
     *
     * @param tau AtcfTaus
     */
    private void updateRadiiGuideList(AtcfTaus tau) {

        radiiGuideList.removeAll();

        /*
         * Find records for each selected aids and current TAU (forecast hour).
         * Then format/add to the guidance list.
         */
        ADeckRecord rec34kt = null;
        ADeckRecord rec50kt = null;
        ADeckRecord rec64kt = null;
        for (String tech : selectedObjAids) {

            // Exclude "OFCL" - which is the forecast itself.
            if ("OFCL".equals(tech)) {
                continue;
            }

            rec34kt = null;
            rec50kt = null;
            rec64kt = null;

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%4s", tech));

            for (ADeckRecord rec : currentADeckRecords) {
                String aidCode = rec.getTechnique();
                int fcstHr = rec.getFcstHour();
                int rad = (int) rec.getRadWind();

                if (fcstHr == tau.getValue()
                        && aidCode.equalsIgnoreCase(tech)) {

                    if (rad == WindRadii.RADII_34_KNOT.getValue()) {
                        rec34kt = rec;
                    } else if (rad == WindRadii.RADII_50_KNOT.getValue()) {
                        rec50kt = rec;
                    } else if (rad == WindRadii.RADII_64_KNOT.getValue()) {
                        rec64kt = rec;
                    }
                }
            }

            // Add techniques that have max wind >= 34 kt for guidance.
            int maxWnd = 0;
            if (rec34kt != null) {
                maxWnd = (int) rec34kt.getWindMax();

                if (maxWnd >= WindRadii.RADII_34_KNOT.getValue()) {
                    sb.append(String.format("%8s", maxWnd));

                    /*
                     * Wind radii defaults to RMISSD for a new record, so
                     * display it as 0 here for new forecast.
                     */
                    int[] quadRadii = rec34kt.getWindRadii();
                    sb.append(String.format("%11s%4s%4s%4s", quadRadii[0],
                            quadRadii[1], quadRadii[2], quadRadii[3]));

                    if (rec50kt != null) {
                        quadRadii = rec50kt.getWindRadii();
                        sb.append(String.format(QUAD_RADII_FORMAT, quadRadii[0],
                                quadRadii[1], quadRadii[2], quadRadii[3]));

                        if (rec64kt != null) {
                            quadRadii = rec64kt.getWindRadii();
                            sb.append(String.format(QUAD_RADII_FORMAT,
                                    quadRadii[0], quadRadii[1], quadRadii[2],
                                    quadRadii[3]));
                        }
                    }

                    // Only add techniques that have max wind >= 34 kt.
                    radiiGuideList.add(sb.toString());
                }
            }
        }
    }

    /*
     * Update info in forecast panel with existing forecast data.
     */
    private void updateForecastList() {

        radiiForecastList.removeAll();

        int ii = 0;
        for (AtcfTaus tau : workingTaus) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%3s", tau.getValue()));

            ForecastTrackRecord rec34kt = getForecastByTauNRadii(tau,
                    WindRadii.RADII_34_KNOT);

            ForecastTrackRecord rec50kt = getForecastByTauNRadii(tau,
                    WindRadii.RADII_50_KNOT);

            ForecastTrackRecord rec64kt = getForecastByTauNRadii(tau,
                    WindRadii.RADII_64_KNOT);

            if (rec34kt != null) {
                int maxWnd = (int) rec34kt.getWindMax();
                sb.append(String.format("%9s", maxWnd));

                /*
                 * Wind radii defaults to RMISSD for a new record, so display it
                 * as 0 here for new forecast.
                 */
                int[] quadRadii = rec34kt.getWindRadii();
                sb.append(String.format("%11s%4s%4s%4s", quadRadii[0],
                        quadRadii[1], quadRadii[2], quadRadii[3]));

                if (rec50kt != null) {
                    quadRadii = rec50kt.getWindRadii();
                    sb.append(String.format(QUAD_RADII_FORMAT, quadRadii[0],
                            quadRadii[1], quadRadii[2], quadRadii[3]));

                    if (rec64kt != null) {
                        quadRadii = rec64kt.getWindRadii();
                        sb.append(String.format(QUAD_RADII_FORMAT, quadRadii[0],
                                quadRadii[1], quadRadii[2], quadRadii[3]));
                    }
                }
            }

            radiiForecastList.add(sb.toString());

            if (tau == getCurrentTau()) {
                radiiForecastList.select(ii);
            }

            ii++;
        }
    }

    /*
     * Perform actions for wind radii data selection and display.
     * 
     * @param action One of the six wind radii action
     */
    private void performRadiiAction(WindRadiiAction action) {

        // Set flag to allow update data on GUI.
        changeListener.setIgnoreChanges(true);

        switch (action) {
        case USE_PREVIOUS_TAU:
            int index = workingTaus.indexOf(getCurrentTau());
            if (index > 0) {
                AtcfTaus prevTau = workingTaus.get(index - 1);

                // Update ForecastTrackRecord in working copy
                java.util.List<ForecastTrackRecord> records = new ArrayList<>();
                for (WindRadii rad : WindRadii.values()) {
                    if (rad.getValue() > 0) {
                        records.add(getForecastByTauNRadii(prevTau, rad));
                    }
                }

                updateRadiiChanges(getCurrentTau(), records);
                changeListener.setChangesUnsaved(true);
            }

            break;

        case USE_RVCN:
            useTechnique(RADII_CONSENSUS, getCurrentTau(), false);
            changeListener.setChangesUnsaved(true);
            break;

        case DELETE_RADII:
            deleteRadii(getCurrentTau());
            changeListener.setChangesUnsaved(true);
            break;

        case USE_DRCL:
            useTechnique("DRCL", getCurrentTau(), false);
            changeListener.setChangesUnsaved(true);
            break;

        case USE_RVCN_ALL_TAUS:
            useTechnique(RADII_CONSENSUS, getCurrentTau(), true);
            changeListener.setChangesUnsaved(true);
            break;

        case DISPLAY_OPTIONS:
            changeDisplayOptions();
            break;

        default:
            break;
        }

        // Reset flag to track future changes on GUI.
        changeListener.setIgnoreChanges(false);
    }

    /*
     * Apply wind radii from a given technique as forecast.
     *
     * @param tech obj aid technique
     *
     * @param tau AtcfTaus
     *
     * @param all Flag to apply the technique to all TAUs.
     */
    private void useTechnique(String tech, AtcfTaus tau, boolean all) {

        java.util.List<ADeckRecord> records = new ArrayList<>();

        RecordKey k34 = new RecordKey(tech, currentDTG, tau.getValue(),
                WindRadii.RADII_34_KNOT.getValue());
        ADeckRecord rec34 = aDeckRecordMap.get(k34);
        records.add(rec34);

        RecordKey k50 = new RecordKey(tech, currentDTG, tau.getValue(),
                WindRadii.RADII_50_KNOT.getValue());
        ADeckRecord rec50 = aDeckRecordMap.get(k50);
        records.add(rec50);

        RecordKey k64 = new RecordKey(tech, currentDTG, tau.getValue(),
                WindRadii.RADII_64_KNOT.getValue());
        ADeckRecord rec64 = aDeckRecordMap.get(k64);
        records.add(rec64);

        // Update working copy of wind radii for current Tau.
        updateRadiiChangesFromADeck(getCurrentTau(), records, true);

        // Update working copy of wind radii for other Taus.
        if (all) {
            for (AtcfTaus otau : workingTaus) {
                if (otau != getCurrentTau()) {

                    records.clear();

                    k34 = new RecordKey(tech, currentDTG, otau.getValue(),
                            WindRadii.RADII_34_KNOT.getValue());
                    rec34 = aDeckRecordMap.get(k34);

                    records.add(rec34);

                    k50 = new RecordKey(tech, currentDTG, otau.getValue(),
                            WindRadii.RADII_50_KNOT.getValue());
                    rec50 = aDeckRecordMap.get(k50);
                    records.add(rec50);

                    k64 = new RecordKey(tech, currentDTG, otau.getValue(),
                            WindRadii.RADII_64_KNOT.getValue());
                    rec64 = aDeckRecordMap.get(k64);
                    records.add(rec64);

                    updateRadiiChangesFromADeck(otau, records, false);
                }
            }
        }

    }

    /**
     * Apply edited wind radii changes into forecast records for saving.
     */
    @Override
    protected void applyChanges() {
        for (Map.Entry<ForecastTrackRecord, ForecastTrackRecord> entry : fcstTrackEditMap
                .entrySet()) {
            ForecastTrackRecord newRec = entry.getKey();
            ForecastTrackRecord origRec = entry.getValue();
            origRec.setRadWind(newRec.getRadWind());
            origRec.setRadWindQuad(newRec.getRadWindQuad());
            origRec.setQuad1WindRad(newRec.getQuad1WindRad());
            origRec.setQuad2WindRad(newRec.getQuad2WindRad());
            origRec.setQuad3WindRad(newRec.getQuad3WindRad());
            origRec.setQuad4WindRad(newRec.getQuad4WindRad());
        }
    }

    /*
     * Update wind radii with the values in the given forecast track records.
     *
     * @param tau AtcfTaus
     *
     * @param records List of ForecastTrackRecord
     */
    private void updateRadiiChanges(AtcfTaus tau,
            java.util.List<ForecastTrackRecord> records) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                WindRadii.RADII_64_KNOT);

        if (rec34 != null) {
            rec34.setRadWindQuad(records.get(0).getRadWindQuad());
            rec34.setQuad1WindRad(records.get(0).getQuad1WindRad());
            rec34.setQuad2WindRad(records.get(0).getQuad2WindRad());
            rec34.setQuad3WindRad(records.get(0).getQuad3WindRad());
            rec34.setQuad4WindRad(records.get(0).getQuad4WindRad());
            fcstTrackRecordMap.fireRecordChanged(rec34);

            if (rec50 != null) {
                rec50.setRadWindQuad(records.get(1).getRadWindQuad());
                rec50.setQuad1WindRad(records.get(1).getQuad1WindRad());
                rec50.setQuad2WindRad(records.get(1).getQuad2WindRad());
                rec50.setQuad3WindRad(records.get(1).getQuad3WindRad());
                rec50.setQuad4WindRad(records.get(1).getQuad4WindRad());
                fcstTrackRecordMap.fireRecordChanged(rec50);

                if (rec64 != null) {
                    rec64.setRadWindQuad(records.get(2).getRadWindQuad());
                    rec64.setQuad1WindRad(records.get(2).getQuad1WindRad());
                    rec64.setQuad2WindRad(records.get(2).getQuad2WindRad());
                    rec64.setQuad3WindRad(records.get(2).getQuad3WindRad());
                    rec64.setQuad4WindRad(records.get(2).getQuad4WindRad());
                    fcstTrackRecordMap.fireRecordChanged(rec64);
                }
            }
        }

    }

    /*
     * Update wind radii with the values in the given A-Deck records.
     *
     * @param tau AtcfTaus
     *
     * @param records List of ADeckRecords
     * 
     * @param Flag an event so other affected parties could update.
     */
    private void updateRadiiChangesFromADeck(AtcfTaus tau,
            java.util.List<ADeckRecord> records, boolean fireChanges) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                WindRadii.RADII_64_KNOT);

        if (rec34 != null) {
            rec34.setRadWindQuad(records.get(0).getRadWindQuad());
            rec34.setQuad1WindRad(records.get(0).getQuad1WindRad());
            rec34.setQuad2WindRad(records.get(0).getQuad2WindRad());
            rec34.setQuad3WindRad(records.get(0).getQuad3WindRad());
            rec34.setQuad4WindRad(records.get(0).getQuad4WindRad());
            if (fireChanges) {
                fcstTrackRecordMap.fireRecordChanged(rec34);
            }

            if (rec50 != null) {
                rec50.setRadWindQuad(records.get(1).getRadWindQuad());
                rec50.setQuad1WindRad(records.get(1).getQuad1WindRad());
                rec50.setQuad2WindRad(records.get(1).getQuad2WindRad());
                rec50.setQuad3WindRad(records.get(1).getQuad3WindRad());
                rec50.setQuad4WindRad(records.get(1).getQuad4WindRad());
                if (fireChanges) {
                    fcstTrackRecordMap.fireRecordChanged(rec50);
                }

                if (rec64 != null) {
                    rec64.setRadWindQuad(records.get(2).getRadWindQuad());
                    rec64.setQuad1WindRad(records.get(2).getQuad1WindRad());
                    rec64.setQuad2WindRad(records.get(2).getQuad2WindRad());
                    rec64.setQuad3WindRad(records.get(2).getQuad3WindRad());
                    rec64.setQuad4WindRad(records.get(2).getQuad4WindRad());
                    if (fireChanges) {
                        fcstTrackRecordMap.fireRecordChanged(rec64);
                    }
                }
            }
        }

    }

    /*
     * Delete wind radii values for the given TAU.
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

        if (rec34 != null) {
            rec34.setQuad1WindRad(0);
            rec34.setQuad2WindRad(0);
            rec34.setQuad3WindRad(0);
            rec34.setQuad4WindRad(0);
            fcstTrackRecordMap.fireRecordChanged(rec34);
        }

        if (rec50 != null) {
            rec50.setQuad1WindRad(0);
            rec50.setQuad2WindRad(0);
            rec50.setQuad3WindRad(0);
            rec50.setQuad4WindRad(0);
            fcstTrackRecordMap.fireRecordChanged(rec50);
        }

        if (rec64 != null) {
            rec64.setQuad1WindRad(0);
            rec64.setQuad2WindRad(0);
            rec64.setQuad3WindRad(0);
            rec64.setQuad4WindRad(0);
            fcstTrackRecordMap.fireRecordChanged(rec64);
        }

    }

    /*
     * Change display options for wind radii.
     */
    private void changeDisplayOptions() {
        FcstTrackRadiiOptionsDialog optionsDlg = new FcstTrackRadiiOptionsDialog(
                shell, fcstTrackData);
        optionsDlg.open();
    }

    /*
     * Update changes from wind radii CComboes.
     *
     * @param tau AtcfTaus
     *
     * @param wndRad WindRadii
     *
     * @param wid CCombo that changed.
     */
    private void updateWindRadiiValue(AtcfTaus tau, WindRadii wndRad,
            Widget wid) {
        ForecastTrackRecord rec = getForecastByTauNRadii(tau, wndRad);
        CCombo cmb = (CCombo) wid;
        if (cmb.isEnabled() && rec != null) {
            float radVal = 0.0F;
            try {
                radVal = Float.parseFloat(cmb.getText());
            } catch (Exception ee) {
                // Use default 0.
                logger.warn(
                        "ForecastWindRadiiDialog: Wind radii defaults to 0.",
                        ee);
            }

            // Make sure rounding it to nearest 5.
            int radii = AtcfDataUtil.roundToNearest(radVal, 5);
            // Update wind radii in record.
            int which = (int) (wid.getData());
            boolean isCircle = FULL_CIRCLE.equals(rec.getRadWindQuad());

            if (!isCircle) {
                if (which == 0) {
                    rec.setQuad1WindRad(radii);
                } else if (which == 1) {
                    rec.setQuad2WindRad(radii);
                } else if (which == 2) {
                    rec.setQuad3WindRad(radii);
                } else if (which == 3) {
                    rec.setQuad4WindRad(radii);
                }
            } else if (which == 0) {
                // which != 0 should not happen
                rec.setQuad1WindRad(radii);
                rec.setQuad2WindRad(radii);
                rec.setQuad3WindRad(radii);
                rec.setQuad4WindRad(radii);
            }

            fcstTrackRecordMap.fireRecordChanged(rec);
        }
    }

    /**
     * Update changes from wind radii circle/quad radio buttons.
     *
     * @param tau
     * @param wndRad
     * @param isCircle
     */
    private void updateWindRadiiQuad(AtcfTaus tau, WindRadii wndRad,
            boolean isCircle) {
        ForecastTrackRecord rec = getForecastByTauNRadii(tau, wndRad);
        if (rec != null) {
            // Legacy behavior.
            if (isCircle) {
                rec.setRadWindQuad(FULL_CIRCLE);

                // Use quad1 wind radii for all other quads.
                float rad1 = rec.getQuad1WindRad();
                int radVal = AtcfDataUtil.roundToNearest((int) rad1, 5);
                rec.setQuad1WindRad(radVal);
                rec.setQuad2WindRad(radVal);
                rec.setQuad3WindRad(radVal);
                rec.setQuad4WindRad(radVal);
            } else {
                rec.setRadWindQuad(DEFAULT_QUAD);
            }

            fcstTrackRecordMap.fireRecordChanged(rec);
        }
    }

    /*
     * Create a wind radii 2-D dialog.
     *
     * @param wndRad WIndRadii
     *
     * @return WindRadiiGraphDialog
     */
    private WindRadiiGraphDialog createWindRadiiGraph(WindRadii wndRad) {

        // Build titles and axis labels.
        String dialogTitle = "ATCF Wind Radii Graph - " + storm.getStormName()
                + " " + storm.getStormId() + " at " + currentDTG.substring(4);

        String chartTitle = " Forecast Wind Radii for " + storm.getCycloneNum()
                + storm.getSubRegion() + " at Tau ";

        String xAxisLbl = "nm";
        String yAxisLbl = "nm";

        return new WindRadiiGraphDialog(ForecastWindRadiiDialog.this.getShell(),
                dialogTitle, chartTitle, xAxisLbl, yAxisLbl, currentDTG,
                observableCurrentTau, wndRad, aDeckRecordMap,
                fcstTrackRecordMap, windRadiiValidation, selectedObjAids);
    }

    /*
     * Enumeration of the actions defined for wind radii data selection display
     * in this dialog.
     */
    private enum WindRadiiAction {
        USE_PREVIOUS_TAU("Use Previous TAU"),
        USE_RVCN("Use RVCN"),
        DELETE_RADII("Delete Radii"),
        USE_DRCL("Use DRCL"),
        USE_RVCN_ALL_TAUS("Use RVCN - All Taus"),
        DISPLAY_OPTIONS("Display Options");

        private final String name;

        /*
         * @param name
         */
        private WindRadiiAction(final String name) {
            this.name = name;
        }

        /*
         * @return the name
         */
        private String getName() {
            return name;
        }
    }

    /**
     * Check if wind radii are missing for a TAU.
     *
     * @return boolean True - none missing, continue; False - warn user to
     *         correct missing data
     */
    private boolean checkWindRadii() {

        String message = windRadiiValidation.checkRadiiValid(fcstTrackRecordMap,
                getCurrentTau());

        // Open up warning dialog so user can make changes
        if (message != null) {
            MessageDialog.openWarning(shell, "Check Wind Radii", message);
            return false;
        }
        // No missing radii
        return true;
    }

    @Override
    protected void disposed() {
        fcstTrackRecordMap.removeMapChangeListener(this);
        super.disposed();
    }

    private void showGraphDialog(WindRadii wndRad) {
        if (radiiGraphDlg == null) {
            // Create & open wind radii 2-D dialog.
            radiiGraphDlg = createWindRadiiGraph(wndRad);
        } else {
            radiiGraphDlg.radiusSelected(wndRad.getValue());
        }
        radiiGraphDlg.open();
    }

    /**
     * Select the given index of the given CCombo, ignoring the resulting
     * events.
     *
     * @param combo
     * @param index
     */
    private void selectCombo(CCombo combo, int index) {
        boolean prevIngore = ignoreUIChanges;
        ignoreUIChanges = true;
        try {
            combo.select(index);
        } finally {
            ignoreUIChanges = prevIngore;
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

    @Override
    public AtcfTaus getCurrentTau() {
        return observableCurrentTau.getValue();
    }

    private void currentTauChanged(AtcfTaus newTau) {
        for (Map.Entry<AtcfTaus, Button> entry : radiiTauBtns.entrySet()) {
            Button btn = entry.getValue();
            if (newTau == entry.getKey()) {
                AtcfVizUtil.setActiveButtonBackground(btn);
            } else {
                AtcfVizUtil.setDefaultBackground(btn);
            }
        }

        updateForTau(getCurrentTau());
    }

}
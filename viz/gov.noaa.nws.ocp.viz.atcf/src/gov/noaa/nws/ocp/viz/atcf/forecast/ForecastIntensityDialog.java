/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.MaxWindGustPairs;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;

/**
 * Dialog for "Forecast=>Forecast Intensity".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 06, 2018 50993      dmanzella   initial creation
 * Jan 21, 2020 71724      jwu         Overhaul GUI, populate data &
 *                                     implement functionalities.
 * Mar 25, 2020 75391      jwu         Use intensity defaults from techlist.
 * May 28, 2020 78027      jwu         Update to all add/delete record,
 *                                     track changes, & use IVCN as consensus.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Apr 22, 2021 88729      jwu         Revise storm type threshold.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ForecastIntensityDialog extends ForecastDialog {

    // Name for all forecast items
    private static final String[] forecastItems = new String[] { "Intensity",
            "Gusts", "Development", "RI Prob" };

    // List for intensity guidance
    private List intGuideList;

    // Map to hold all forecast Ccombos
    private Map<String, Map<AtcfTaus, CCombo>> fcstComboes;

    // Items for storm developement
    private java.util.List<String> developItems;

    // Control buttons
    private Button saveBtn;

    // Forecast info for selected obj aids.
    private ForecastInfo fcstInfo;

    // Associated graph dialog
    private IntensityGraphDialog intensityGraphDialog;

    // Set while updating CCombo controls
    private boolean ignoreUIChanges;

    // Maximum wind and gust conversion pairs.
    private static MaxWindGustPairs gustConversion = AtcfConfigurationManager
            .getInstance().getMaxWindGustPairs();

    /**
     * Constructor
     *
     * @param shell
     */
    public ForecastIntensityDialog(Shell parent, Storm curStorm,
            Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData) {
        super(parent, curStorm, ForecastType.INTENSITY, fcstTrackData);
    }

    /**
     * Initializes the components.
     *
     * @param parent
     */
    @Override
    protected void populateScrollComponent(Composite parent) {

        // Items for storm development
        if (developItems == null) {
            developItems = new ArrayList<>();
            Collections.addAll(developItems,
                    StormDevelopment.getStormShortIntensityStrings());

        }

        // Combos for forecast items
        if (fcstComboes == null) {
            fcstComboes = new HashMap<>();
            for (String item : forecastItems) {
                fcstComboes.put(item, new EnumMap<>(AtcfTaus.class));
            }
        }

        // Create GUI.
        createContents(parent);

        // Populate existing forecast data.
        populateForecastData();

        /*
         * Add a change handler for the forecast track data. Only changes to
         * existing records are handled; adds and removes are ignored. This
         * dialog only adds/removes 50kt and 64kt records (in applyChanges())
         * and those records are never displayed.
         */
        fcstTrackRecordMap.addMapChangeListener(
                event -> intensityChanged(event.diff.getChangedKeys().stream()
                        .map(k -> k.getTau()).collect(Collectors.toSet())));

        // Prepare intensity info for 2-D plot.
        prepareIntensityInfo();

        // List the intensity from selected techniques.
        updateIntGuideList();
    }

    /**
     * Create contents.
     *
     * @param parent
     */
    protected void createContents(Composite parent) {
        Composite mainComposite = new Composite(parent, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.verticalSpacing = 10;
        mainLayout.marginWidth = 10;
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComposite.setLayoutData(mainLayoutData);

        createForecastPanel(mainComposite);
        createActionButtons(mainComposite);
        createIntensityGuidance(mainComposite);
        createRapidIntensityGuidance(mainComposite);
        createControlButtons(mainComposite);
    }

    /**
     * Creates the top panel for selecting intensity/gust/development/RI prob.
     *
     * @param parent
     */
    protected void createForecastPanel(Composite parent) {

        Composite intFcstComp = new Composite(parent, SWT.NONE);
        GridLayout intCompGL = new GridLayout(1, true);
        intCompGL.verticalSpacing = 10;
        intCompGL.marginWidth = 10;
        intFcstComp.setLayout(intCompGL);

        Label mainLabel = new Label(intFcstComp, SWT.CENTER);
        mainLabel.setText("Intensity Forecast");
        GridData intensityForecastGridData = new GridData(SWT.CENTER, SWT.NONE,
                false, false);
        mainLabel.setLayoutData(intensityForecastGridData);

        Composite intInfoComp = new Composite(intFcstComp, SWT.NONE);
        GridLayout intInfoGL = new GridLayout(workingTaus.size() + 1, false);
        intInfoGL.horizontalSpacing = 8;
        intInfoComp.setLayout(intInfoGL);
        intInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // Create labels for each forecast TAU.
        Label spaceLbl = new Label(intInfoComp, SWT.NONE);
        spaceLbl.setText("  ");
        GridData spaceGD = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        spaceLbl.setLayoutData(spaceGD);

        for (AtcfTaus tau : workingTaus) {
            Label tauLbl = new Label(intInfoComp, SWT.NONE);
            if (tau.getValue() == 0) {
                tauLbl.setText("0" + tau.getValue() + "h");
            } else {
                tauLbl.setText("" + tau.getValue() + "h");
            }

            GridData tauGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            tauLbl.setLayoutData(tauGD);
        }

        // Creates CCombos for each forecast items.
        for (String item : forecastItems) {

            Label titleLabel = new Label(intInfoComp, SWT.RIGHT);
            titleLabel.setText(item);
            GridData labelGD = new GridData(SWT.RIGHT, SWT.CENTER, false,
                    false);
            titleLabel.setLayoutData(labelGD);

            String[] items;
            if (item.equals(forecastItems[0])
                    || item.equals(forecastItems[1])) {
                items = windEntries;
            } else if (item.equals(forecastItems[2])) {
                items = StormDevelopment.getStormShortIntensityStrings();
            } else {
                items = getRIProbabiltyEntries();
            }

            int charWidth = -1;
            for (AtcfTaus tau : workingTaus) {
                CCombo fcstCmb = new CCombo(intInfoComp, SWT.BORDER);
                fcstComboes.get(item).put(tau, fcstCmb);

                if (charWidth < 0) {
                    charWidth = AtcfVizUtil.getCharWidth(fcstCmb);
                }

                GridData cmbGD = new GridData(SWT.CENTER, SWT.CENTER, false,
                        false);
                cmbGD.widthHint = charWidth * 9;
                fcstCmb.setLayoutData(cmbGD);

                fcstCmb.setItems(items);
                selectCombo(fcstCmb, 0);

                // For "RI Prob", only 24 hr is availbale now.
                if (item.equals(forecastItems[3]) && tau.getValue() != 24) {
                    fcstCmb.setEnabled(false);
                }
            }
        }

        // Add verify and modify listeners.
        for (AtcfTaus tau : workingTaus) {
            CCombo intCmb = fcstComboes.get(forecastItems[0]).get(tau);
            CCombo gustCmb = fcstComboes.get(forecastItems[1]).get(tau);
            CCombo devCmb = fcstComboes.get(forecastItems[2]).get(tau);
            intCmb.addModifyListener(ev -> {
                float maxWnd;

                if (ignoreUIChanges) {
                    return;
                }

                try {
                    maxWnd = Float.parseFloat(intCmb.getText());
                } catch (NumberFormatException e) {
                    // No changes.
                    return;
                }

                setWindMax(fcstTrackRecordMap, tau.getValue(), (int) maxWnd);
            });

            intCmb.addVerifyListener(verifyListener.getWindCmbVerifyListener());

            gustCmb.addModifyListener(ev -> {
                if (ignoreUIChanges) {
                    return;
                }

                saveBtn.setEnabled(true);

                // Update flag to indicate new changes.
                if (!changeListener.isIgnoreChanges()) {
                    changeListener.setChangesUnsaved(true);
                }
            });

            devCmb.addModifyListener(ev -> {
                if (ignoreUIChanges) {
                    return;
                }

                saveBtn.setEnabled(true);

                // Update flag to indicate new changes.
                if (!changeListener.isIgnoreChanges()) {
                    changeListener.setChangesUnsaved(true);
                }
            });
        }

    }

    /**
     * Creates the "View Intensity Graph" and "Use Consensus" buttons
     *
     * @param parent
     *            parent composite
     */
    protected void createActionButtons(Composite parent) {
        Composite actionComp = new Composite(parent, SWT.NONE);
        GridLayout actionCompGL = new GridLayout(2, false);
        actionCompGL.horizontalSpacing = 220;
        actionCompGL.marginLeft = 130;
        actionComp.setLayout(actionCompGL);

        Button intGraphBtn = new Button(actionComp, SWT.CENTER);
        intGraphBtn.setText("   View Intensity Graph / Make Forecast ...   ");
        GridData intGraphGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        intGraphBtn.setLayoutData(intGraphGD);

        intGraphBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ObjAidsForIntensityDialog selectAidsDlg = new ObjAidsForIntensityDialog(
                        shell, storm, ForecastIntensityDialog.this);
                selectAidsDlg.setSelectedDateTimeGroup(
                        objAidsProperties.getSelectedDateTimeGroups()[0]);
                selectAidsDlg.open();
            }
        });

        Button consensusBtn = new Button(actionComp, SWT.CENTER);
        consensusBtn.setText("   Use Consensus   ");
        GridData consensusBtnGD = new GridData(SWT.CENTER, SWT.NONE, false,
                false);
        consensusBtn.setLayoutData(consensusBtnGD);
        consensusBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useConsensus();
            }
        });
    }

    /**
     * Creates the Intensity Guidance section
     *
     * @param parent
     */
    protected void createIntensityGuidance(Composite parent) {

        Composite intensityGuideComp = new Composite(parent, SWT.NONE);
        GridLayout intensityGuideGL = new GridLayout(1, true);
        intensityGuideGL.verticalSpacing = 10;
        intensityGuideGL.marginWidth = 60;
        intensityGuideComp.setLayout(intensityGuideGL);

        Label intensityGuideLbl = new Label(intensityGuideComp, SWT.CENTER);
        intensityGuideLbl.setText("Intensity Guidance");
        GridData intensityGuideGD = new GridData(SWT.CENTER, SWT.NONE, false,
                false);
        intensityGuideLbl.setLayoutData(intensityGuideGD);

        Composite intGuideComp = new Composite(intensityGuideComp, SWT.NONE);
        GridLayout intGuideGL = new GridLayout(1, false);
        intGuideGL.marginWidth = 35;
        intGuideComp.setLayout(intGuideGL);
        intGuideComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        // Create the label
        Label aid = new Label(intGuideComp, SWT.NONE);
        aid.setFont(txtFont);

        GridData aidGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        aid.setLayoutData(aidGD);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%6s", "AID "));
        for (AtcfTaus tau : workingTaus) {
            if (tau.getValue() > 0) {
                sb.append(String.format("%8s", "" + tau.getValue() + "h"));
            }
        }

        aid.setText(sb.toString());

        // Create the list for guidance
        intGuideList = new List(intGuideComp, SWT.NONE | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData intListGD = new GridData(720, 165);
        intListGD.horizontalAlignment = SWT.FILL;
        intListGD.verticalAlignment = SWT.FILL;
        intGuideList.setLayoutData(intListGD);

        intGuideList.setFont(txtFont);

    }

    /**
     * Creates the Rapid Intensity Guidance section
     *
     * @param parent
     */
    protected void createRapidIntensityGuidance(Composite parent) {

        Composite rapidIntGuideComp = new Composite(parent, SWT.NONE);
        GridLayout rapidIntGuideGL = new GridLayout(1, true);
        rapidIntGuideGL.verticalSpacing = 10;
        rapidIntGuideGL.marginWidth = 60;
        rapidIntGuideComp.setLayout(rapidIntGuideGL);

        Label rapidIntGuideLbl = new Label(rapidIntGuideComp, SWT.CENTER);
        rapidIntGuideLbl.setText("Rapid Intensity Guidance");
        GridData rapidIntGuideGD = new GridData(SWT.CENTER, SWT.NONE, false,
                false);
        rapidIntGuideLbl.setLayoutData(rapidIntGuideGD);

        Composite intGuideComp = new Composite(rapidIntGuideComp, SWT.NONE);
        GridLayout intGuideGL = new GridLayout(1, false);
        intGuideGL.marginWidth = 35;
        intGuideComp.setLayout(intGuideGL);
        intGuideComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        // Create the label
        Label aid = new Label(intGuideComp, SWT.NONE);
        aid.setFont(txtFont);

        GridData aidGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        aid.setLayoutData(aidGD);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%6s", "AID "));
        for (AtcfTaus tau : workingTaus) {
            if (tau.getValue() > 0) {
                sb.append(String.format("%8s", "" + tau.getValue() + "h"));
            }
        }

        aid.setText(sb.toString());

        // Create the list for guidance
        List rapidIntGuideList = new List(intGuideComp, SWT.NONE | SWT.SINGLE
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData intListGD = new GridData(720, 162);
        intListGD.horizontalAlignment = SWT.FILL;
        intListGD.verticalAlignment = SWT.FILL;
        rapidIntGuideList.setLayoutData(intListGD);

        rapidIntGuideList.setFont(txtFont);
    }

    /**
     * Creates the help, ok, and cancel buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlCompGL = new GridLayout(5, true);
        ctrlCompGL.horizontalSpacing = 80;
        ctrlCompGL.marginWidth = 70;
        ctrlBtnComp.setLayout(ctrlCompGL);

        ctrlBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        Button prevBtn = new Button(ctrlBtnComp, SWT.PUSH);
        prevBtn.setText("<<");
        prevBtn.setToolTipText("Return to Forecast Track Position");
        prevBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        prevBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveForecast();
                close();

                // Start Forecast Track
                AtcfVizUtil.executeCommand(
                        "gov.noaa.nws.ocp.viz.atcf.ui.forecastTrack", null,
                        null);
            }
        });

        saveBtn = new Button(ctrlBtnComp, SWT.CENTER);
        saveBtn.setText("Save");
        saveBtn.setEnabled(false);
        saveBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateIntGuideList();
                saveForecast();
                saveBtn.setEnabled(false);
            }
        });

        Button submitButton = new Button(ctrlBtnComp, SWT.CENTER);
        submitButton.setText("Submit");
        submitButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        submitButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                submitForecast(saveBtn.isEnabled());
                close();
            }
        });

        Button cancelButton = new Button(ctrlBtnComp, SWT.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        Button nextBtn = new Button(ctrlBtnComp, SWT.PUSH);
        nextBtn.setText(">>");
        nextBtn.setToolTipText("Continue to Forecast Wind Radii");
        nextBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        nextBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveForecast();
                close();

                // Start Forecast Wind Radii
                AtcfVizUtil.executeCommand(
                        "gov.noaa.nws.ocp.viz.atcf.ui.windRadiiForecast", null,
                        null);
            }
        });

    }

    /*
     * Create an array for RI probabilities: 0 through 100 with interval of 1.
     *
     * return
     */
    private String[] getRIProbabiltyEntries() {

        String[] probEntries = new String[101];

        for (int k = 0; k <= 100; k++) {
            probEntries[k] = String.valueOf(k);
        }

        return probEntries;
    }

    /**
     * Update the information in the intensity guide list
     */
    public void updateIntGuideList() {

        intGuideList.removeAll();

        java.util.List<String> selectedAids = selectedObjAids;

        /*
         * Find records for selected aids, current DTG, & wind radii <= 34kt
         * (some aids has wind radii = 0).
         */
        java.util.List<ADeckRecord> records = new ArrayList<>();
        for (ADeckRecord rec : currentADeckRecords) {
            String aidCode = rec.getTechnique();
            int windRad = (int) rec.getRadWind();
            // TODO Confirm the logic here ... consider wind radii 0?
            if (windRad <= 34 && selectedAids.contains(aidCode)) {
                records.add(rec);
            }
        }

        java.util.List<ADeckRecord> recForAid = new ArrayList<>();
        int[] intensityForAid;
        for (String aid : selectedAids) {
            // Find records for a given technique (aid)
            for (ADeckRecord rec : records) {
                String aidCode = rec.getTechnique();
                if (aid.equals(aidCode)) {
                    recForAid.add(rec);
                }
            }

            // Find maximum wind (intensity) for each forecast hour (TAU).
            intensityForAid = new int[workingTaus.size() - 1];
            int ii = 0;
            for (AtcfTaus tau : workingTaus) {
                if (tau.getValue() > 0) {
                    intensityForAid[ii] = 0;
                    for (ADeckRecord rec : recForAid) {
                        if (rec.getFcstHour() == tau.getValue()) {
                            int intensity = (int) rec.getWindMax();
                            if (intensity >= 0 && intensity <= MAX_WIND_SPEED) {
                                intensityForAid[ii] = intensity;
                            }
                            break;
                        }
                    }

                    ii++;
                }
            }

            recForAid.clear();

            // Build information and add to the intensity guidance list.
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%5s", aid));

            boolean hasNonZero = false;
            for (int intensity : intensityForAid) {
                if (intensity > 0) {
                    hasNonZero = true;
                }

                sb.append(String.format("%8s", intensity));
            }

            if (hasNonZero) {
                intGuideList.add(sb.toString());
            }
        }
    }

    /*
     * Populate forecast panel with existing forecast data.
     */
    private void populateForecastData() {

        // Update flag to ignore changes here.
        changeListener.setIgnoreChanges(true);
        changeListener.setChangesUnsaved(false);

        try {
            for (AtcfTaus tau : workingTaus) {
                // same logic as hasLocationForecast
                java.util.List<ForecastTrackRecord> frecs = getForecastForTau(
                        tau);
                if (!frecs.isEmpty()) {
                    ForecastTrackRecord rec = frecs.get(0);
                    int intensity = (int) rec.getWindMax();
                    int gust = (int) rec.getGust();
                    String develop = rec.getIntensity();
                    int rip = 0;

                    if (intensity >= 0 && intensity <= MAX_WIND_SPEED) {
                        CCombo cmb = fcstComboes.get(forecastItems[0]).get(tau);
                        setComboText(cmb, "" + intensity);
                    }

                    if (gust >= 0 && gust <= MAX_WIND_SPEED) {
                        CCombo cmb = fcstComboes.get(forecastItems[1]).get(tau);
                        setComboText(cmb, "" + gust);
                    }

                    if (develop != null && develop.length() > 0
                            && developItems.indexOf(develop) >= 0) {
                        int index = developItems.indexOf(develop);
                        if (index >= 0) {
                            CCombo cmb = fcstComboes.get(forecastItems[2])
                                    .get(tau);
                            selectCombo(cmb, index);
                        }
                    } else {
                        // TODO Find from maximum wind?
                    }

                    CCombo cmb = fcstComboes.get(forecastItems[3]).get(tau);
                    setComboText(cmb, "" + rip);
                } else {
                    for (String item : forecastItems) {
                        CCombo cmb = fcstComboes.get(item).get(tau);
                        selectCombo(cmb, 0);
                        cmb.setEnabled(false);
                    }
                }
            }
        } catch (Exception ee) {
            logger.warn(
                    "ForecastIntensityDialog: Failed to populate forecast data: ",
                    ee);
        } finally {
            // Update flag to track changes.
            changeListener.setIgnoreChanges(false);
            saveBtn.setEnabled(false);
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
        for (java.util.List<ForecastTrackRecord> rcs : fcstTrackData.values()) {
            if (rcs != null && tau.getValue() == rcs.get(0).getFcstHour()) {
                recs.addAll(rcs);
                break;
            }
        }

        return recs;
    }

    /**
     * @return the fcstInfo
     */
    public ForecastInfo getFcstInfo() {
        return fcstInfo;
    }

    /**
     *
     * @param intensityMap
     *            the intensityForecast to set
     */
    public void intensityChanged(Collection<? extends Integer> changedSet) {

        saveBtn.setEnabled(true);

        // Update intensity and storm development (and gust?)
        for (Integer i : changedSet) {
            AtcfTaus tau = AtcfTaus.getTau(i);
            CCombo cmb = fcstComboes.get(forecastItems[0]).get(tau);
            if (hasLocationForecast(tau)) {
                int intensity = ForecastIntensityDialog
                        .getWindMax(fcstTrackRecordMap, tau.getValue());
                if (intensity >= 0 && intensity <= MAX_WIND_SPEED) {
                    setComboText(cmb, "" + intensity);

                    int gust = gustConversion.getGustValue(intensity);
                    CCombo gustCmb = fcstComboes.get(forecastItems[1]).get(tau);
                    setComboText(gustCmb, "" + gust);

                    CCombo devCmb = fcstComboes.get(forecastItems[2]).get(tau);
                    String iniType = devCmb.getItem(devCmb.getSelectionIndex());
                    String develop = StormDevelopment.getIntensity(iniType,
                            intensity);
                    int indx = developItems.indexOf(develop);
                    if (indx >= 0) {
                        selectCombo(devCmb, indx);
                    }
                }
            }
        }

        // Update flag to indicate new changes.
        changeListener.setChangesUnsaved(true);
    }

    /**
     * Prepare a ForecastInfo instance to be used for 2-D plot.
     *
     * @param intensityForecast
     *            the intensityForecast to set
     */
    public void prepareIntensityInfo() {

        if (fcstInfo == null) {
            fcstInfo = new ForecastInfo(storm.getStormId(),
                    storm.getStormName(), currentDTG);
        }

        /*
         * Find records for selected aids, & wind radii <= 34kt (some aids has
         * wind radii = 0).
         */
        java.util.List<ADeckRecord> records = new ArrayList<>();
        for (ADeckRecord rec : currentADeckRecords) {
            String aidCode = rec.getTechnique();
            int windRad = (int) rec.getRadWind();
            int fcstHr = rec.getFcstHour();
            // TODO should we include wind radii = 0?
            if (fcstHr >= 0 && windRad <= 34
                    && selectedObjAids.contains(aidCode)) {
                records.add(rec);
            }
        }

        java.util.List<ADeckRecord> recForAid = new ArrayList<>();
        for (String aid : selectedObjAids) {

            // Find records for a given technique (aid)
            for (ADeckRecord rec : records) {
                String aidCode = rec.getTechnique();
                if (aid.equals(aidCode)) {
                    recForAid.add(rec);
                }
            }

            if (!recForAid.isEmpty()) {

                for (AtcfTaus tau : workingTaus) {
                    for (ADeckRecord rec : recForAid) {
                        if (rec.getFcstHour() == tau.getValue()) {
                            int maxWnd = (int) rec.getWindMax();
                            if (maxWnd > 0 && maxWnd <= MAX_WIND_SPEED) {
                                fcstInfo.addIntensity(aid, tau.getValue(),
                                        maxWnd);
                                break;
                            }
                        }
                    }
                }
            }

            recForAid.clear();
        }

    }

    /**
     * Apply changes into forecast records for saving. New records may be
     * created and existing records may be deleted.
     */
    @Override
    protected void applyChanges() {

        // Update/add/delete records based on max wind (intensity) on GUI
        java.util.List<ForecastTrackRecord> recToRemove = new ArrayList<>();
        java.util.List<ForecastTrackRecord> recToAdd = new ArrayList<>();
        for (AtcfTaus tau : workingTaus) {
            if (hasLocationForecast(tau)) {
                float wnd = getWindMax(fcstTrackRecordMap, tau.getValue());

                if (wnd >= 0) {
                    String gust = fcstComboes.get(forecastItems[1]).get(tau)
                            .getText();
                    float gst = gustConversion.getGustValue((int) wnd);
                    try {
                        gst = Float.parseFloat(gust);
                    } catch (Exception ee) {
                        // Use value converted from max wind
                        logger.warn("ForecastIntensityDialog:  use max wind. ",
                                ee);
                    }

                    String develop = fcstComboes.get(forecastItems[2]).get(tau)
                            .getText();

                    // TODO where to set the RI Probability?
                    String rip = "";
                    if (tau.getValue() == 24) {
                        rip = fcstComboes.get(forecastItems[3]).get(tau)
                                .getText();
                    }

                    /*
                     * Update record for 34 kt. This one should exist and won't
                     * be deleted.
                     */
                    ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                            WindRadii.RADII_34_KNOT);
                    if (rec34 != null) {
                        rec34.setWindMax(wnd);
                        rec34.setGust(gst);
                        if (developItems.contains(develop)) {
                            rec34.setIntensity(develop);
                        }

                        /*
                         * Check record for 50 kt - update/delete/create based
                         * on new max wind.
                         */
                        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                                WindRadii.RADII_50_KNOT);
                        int radWnd50 = WindRadii.RADII_50_KNOT.getValue();

                        if (rec50 != null) {
                            if (wnd < radWnd50) {
                                // Delete record
                                recToRemove.add(rec50);
                            } else {
                                // Update record
                                rec50.setWindMax(wnd);
                                rec50.setGust(gst);
                                if (developItems.contains(develop)) {
                                    rec50.setIntensity(develop);
                                }
                            }
                        } else {
                            if (wnd >= radWnd50) {
                                // Add a new record
                                rec50 = new ForecastTrackRecord(rec34);
                                rec50.setRadWind(radWnd50);
                                rec50.setWindMax(wnd);
                                rec50.setGust(gst);
                                if (developItems.contains(develop)) {
                                    rec50.setIntensity(develop);
                                }
                                recToAdd.add(rec50);
                            }
                        }

                        /*
                         * Check record for 64 kt - update/delete/create based
                         * on new max wind.
                         */
                        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                                WindRadii.RADII_64_KNOT);
                        int radWnd64 = WindRadii.RADII_64_KNOT.getValue();

                        if (rec64 != null) {
                            if (wnd < radWnd64) {
                                // Delete record
                                recToRemove.add(rec64);
                            } else {
                                // Update record
                                rec64.setWindMax(wnd);
                                rec64.setGust(gst);
                                if (developItems.contains(develop)) {
                                    rec64.setIntensity(develop);
                                }
                            }
                        } else {
                            if (wnd >= radWnd64) {
                                // Add a new record
                                rec64 = new ForecastTrackRecord(rec50);
                                rec64.setRadWind(radWnd64);
                                rec64.setWindMax(wnd);
                                rec64.setGust(gst);
                                if (developItems.contains(develop)) {
                                    rec64.setIntensity(develop);
                                }

                                recToAdd.add(rec64);
                            }
                        }
                    }
                }
            }
        }

        // Update the original records which will be used for saveForecast().
        for (Map.Entry<ForecastTrackRecord, ForecastTrackRecord> entry : fcstTrackEditMap
                .entrySet()) {
            ForecastTrackRecord newRec = entry.getKey();
            ForecastTrackRecord origRec = entry.getValue();
            origRec.setWindMax(newRec.getWindMax());
            origRec.setGust(newRec.getGust());
            origRec.setIntensity(newRec.getIntensity());
        }

        // Add new records
        for (ForecastTrackRecord rec : recToAdd) {
            RecordKey key = new RecordKey("FCST", currentDTG, rec.getFcstHour(),
                    (int) rec.getRadWind());
            ForecastTrackRecord nrec = new ForecastTrackRecord(rec);
            fcstTrackEditMap.put(nrec, rec);
            fcstTrackRecordMap.put(key, nrec);

            Calendar dataTime = rec.getRefTimeAsCalendar();
            dataTime.add(Calendar.HOUR_OF_DAY, rec.getFcstHour());
            String dtg = AtcfDataUtil.calendarToDateTimeString(dataTime);
            java.util.List<ForecastTrackRecord> dtgRecs = fcstTrackData
                    .get(dtg);
            dtgRecs.add(rec);
        }

        // Remove records in map and list.
        java.util.List<ForecastTrackRecord> origRecToRemove = new ArrayList<>();
        for (ForecastTrackRecord rec : recToRemove) {
            RecordKey key = new RecordKey("FCST", currentDTG, rec.getFcstHour(),
                    (int) rec.getRadWind());
            fcstTrackRecordMap.remove(key);

            ForecastTrackRecord origRec = fcstTrackEditMap.get(rec);
            origRecToRemove.add(origRec);

            Calendar dataTime = origRec.getRefTimeAsCalendar();
            dataTime.add(Calendar.HOUR_OF_DAY, origRec.getFcstHour());
            String dtg = AtcfDataUtil.calendarToDateTimeString(dataTime);
            java.util.List<ForecastTrackRecord> dtgRecs = fcstTrackData
                    .get(dtg);

            dtgRecs.remove(origRec);
            fcstTrackEditMap.remove(rec);
        }

        // Delete from DB, if any needed.
        deleteForecast(origRecToRemove);

        // Redisplay forecast track
        if (!recToAdd.isEmpty() || !recToRemove.isEmpty()) {
            drawForecastTrack();
        }
    }

    /*
     * Use consensus intensity
     */
    private void useConsensus() {

        changeListener.setIgnoreChanges(true);

        // Find intensity from consensus records
        try {
            Map<Integer, Integer> intensityMap = new HashMap<>();
            for (ADeckRecord rec : currentADeckRecords) {
                String aidCode = rec.getTechnique();
                if (INTENSITY_CONSENSUS.equalsIgnoreCase(aidCode)) {
                    intensityMap.put(rec.getFcstHour(), (int) rec.getWindMax());
                }
            }

            // Only Update intensity (legacy behavior?)
            for (AtcfTaus tau : workingTaus) {
                if (hasLocationForecast(tau)) {
                    int intensity = intensityMap.get(tau.getValue());
                    if (intensity >= 0 && intensity <= MAX_WIND_SPEED) {
                        // Make sure rounding it to nearest 5.
                        int intVal = AtcfDataUtil.roundToNearest(intensity, 5);
                        // This also updates gusts and development
                        setWindMax(fcstTrackRecordMap, tau.getValue(), intVal);
                    }
                }
            }
        } catch (Exception ee) {
            logger.warn(
                    "ForecastIntensityDialog: Failed to update from consensus: ",
                    ee);
        } finally {
            // Update flag to indicate new changes.
            changeListener.setIgnoreChanges(false);
            changeListener.setChangesUnsaved(true);
        }
    }

    /**
     * Select the given index of the given CCombo, ignoring the resulting
     * events.
     *
     * @param combo
     * @param index
     */
    private void selectCombo(CCombo combo, int index) {
        boolean saved = ignoreUIChanges;
        ignoreUIChanges = true;
        try {
            combo.select(index);
        } finally {
            ignoreUIChanges = saved;
        }
    }

    /**
     * Set the text of the given CCombo, ignoring the resulting events.
     *
     * @param combo
     * @param index
     */
    private void setComboText(CCombo combo, String text) {
        boolean saved = ignoreUIChanges;
        ignoreUIChanges = true;
        try {
            combo.setText(text);
        } finally {
            ignoreUIChanges = saved;
        }
    }

    /**
     * This method is used to determine if it is valid to edit the intensity at
     * a given forecast time.
     *
     * @param tau
     * @return
     */
    private boolean hasLocationForecast(AtcfTaus tau) {
        return !getForecastForTau(tau).isEmpty();
    }

    /**
     * Set and display the given graph dialog.
     *
     * @param intensityGraphDialog
     */
    public void setGraphDialog(IntensityGraphDialog intensityGraphDialog) {
        if (this.intensityGraphDialog != null) {
            this.intensityGraphDialog.close();
        }
        this.intensityGraphDialog = intensityGraphDialog;
        intensityGraphDialog.open();
    }

    /* package */ ForecastTrackRecordMap getForecastTrackRecordMap() {
        return fcstTrackRecordMap;
    }

    /**
     * Get the "max wind" for the given forecast hour. Throws an exception if
     * the forecast hour does not exist.
     * <p>
     * This is intended as a utility method for the "Forecast Intensity" set of
     * UIs and not a general purpose method because the semantics of such a
     * method are not clear. For example, should 50kt and 64kt records also be
     * modified? In this case, 50kt and 64kt records are synchronized only in
     * {@code applyChanges()}.
     *
     * @param forecastTrackRecordMap
     * @param tau
     * @return
     */
    /* package */ static int getWindMax(
            ForecastTrackRecordMap forecastTrackRecordMap, int tau) {
        return (int) forecastTrackRecordMap
                .getByTauRadii(AtcfTaus.getTau(tau), WindRadii.RADII_34_KNOT)
                .getWindMax();
    }

    /**
     * Set the "max wind" for the given forecast hour.
     * <p>
     * This is intended as a utility method for the "Forecast Intensity" set of
     * UIs and not a general purpose method because the semantics of such a
     * method are not clear. For example, should 50kt and 64kt records also be
     * modified? In this case, 50kt and 64kt records are synchronized only in
     * {@code applyChanges()}.
     *
     * @param forecastTrackRecordMap
     *            Owning record map
     * @param tau
     * @param windMax
     * @return
     */
    /* package */ static boolean setWindMax(
            ForecastTrackRecordMap forecastTrackRecordMap, int tau,
            int windMax) {
        ForecastTrackRecord rec = forecastTrackRecordMap
                .getByTauRadii(AtcfTaus.getTau(tau), WindRadii.RADII_34_KNOT);
        if (rec != null) {
            rec.setWindMax(windMax);
            forecastTrackRecordMap.fireRecordChanged(rec);
            return true;
        } else {
            return false;
        }
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Dialog for "Aids"=>"Enter Objective Aids (no wind radii)".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 12, 2019 59223      jwu         Initial creation.
 * Aug 14, 2019 65562      jwu         Implement functionalities.
 * Aug 16, 2019 67323      jwu         Add verify listeners.
 * Sep 04, 2019 68112      jwu         Save with batch processing.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Mar 05, 2021 88229      mporricelli Redraw aids display upon Save
 * Mar 18, 2021 89201      mporricelli Fix for loop & disable Submit
 *                                     on GUI startup
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class EnterObjAidsNoWindRadiiDialog extends OcpCaveChangeTrackDialog {

    // Names/units for aid information parameters
    private static final String[] inputParameters = new String[] { "Tau",
            "Latitude", "Longitude", "Max Wind (Knots)" };

    // Current storm and sandbox id for A-Deck.
    private Storm storm;

    private int sandboxID;

    // Parameter to include all wind radiis when building a RecordKey.
    private static final int ALL_WIND_RADII = -1;

    // Action buttons.
    private Button saveButton;

    private Button submitButton;

    // Available and selected date time groups (DTG).
    private String selectedDateTimeGroup;

    private String[] availableDateTimeGroups;

    // All obj aids tech from current master tech file (normally techlist.dat)
    private Map<String, ObjectiveAidTechEntry> allObjectiveAids;

    // Map of A-Deck records retrieved.
    private Map<String, Map<RecordKey, List<ADeckRecord>>> retrievedADeckRecords;

    // Map of A-Deck records holds lat/lon/windmax initially retrieved.
    private Map<RecordKey, ADeckRecord> initialADeckRecords;

    // Map of A-Deck records changed.
    private Map<RecordKey, ADeckRecord> changedADeckRecords;

    // Technique and DTG combos
    private CCombo techCombo;

    private CCombo dtgCombo;

    // Widgets to input lat/lon/max wind for each TAU
    private Label[] tauLbls;

    private Text[] latTxts;

    private Button[] latNorthBtns;

    private Button[] latSouthBtns;

    private Text[] lonTxts;

    private Button[] lonEastBtns;

    private Button[] lonWestBtns;

    private Text[] windTxts;

    // Listener for lat/lon/max wind changes.
    private ModifyListener txtModifyListener;

    /**
     * Collection of verify listeners.
     */
    private final AtcfTextListeners verifyListeners = new AtcfTextListeners();

    /**
     * Constructor
     *
     * @param parent
     */
    public EnterObjAidsNoWindRadiiDialog(Shell parent, Storm storm) {
        super(parent);

        this.storm = storm;
        this.sandboxID = AtcfDataUtil.getADeckSandbox(storm);

        setText("Enter Objective Aid (no wind radii) - " + storm.getStormName()
                + " " + storm.getStormId());

        // Initialize widgets.
        int num = AtcfTaus.values().length;
        tauLbls = new Label[num];
        latTxts = new Text[num];
        latNorthBtns = new Button[num];
        latSouthBtns = new Button[num];
        lonTxts = new Text[num];
        lonEastBtns = new Button[num];
        lonWestBtns = new Button[num];
        windTxts = new Text[num];

        changedADeckRecords = new HashMap<>();

        initialADeckRecords = new HashMap<>();

        // Get all defined objective techniques
        allObjectiveAids = AtcfConfigurationManager.getInstance()
                .getObjectiveAidTechniques().getAvailableTechniques();

        // Get all A-Deck date/time groups (DTGs)
        List<String> baselineDtgs = AtcfDataUtil
                .getDateTimeGroups(storm);

        TreeSet<String> dtgs = new TreeSet<>();
        dtgs.addAll(baselineDtgs);

        availableDateTimeGroups = dtgs.toArray(new String[0]);

        if (!dtgs.isEmpty()) {
            selectedDateTimeGroup = dtgs.last();
        }

    }

    /**
     * Initialize the dialog components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        final Composite mainComp = new Composite(scrollComposite, SWT.NONE);

        createDataComp(mainComp);
        createControlButtons(mainComp);

        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 0;

        mainComp.setLayout(mainLayout);
        mainComp.setData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Load data and populate.
        updateData();

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    /**
     * Create the data input section.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, false);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginHeight = 8;
        dtgInfoLayout.marginWidth = 20;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        txtModifyListener = txtModifyListener();

        // Selection of objective aid technique name.
        createTechSelectionComp(mainComp);

        // Selection of storm DTG
        createDtgSelectionComp(mainComp);

        // Major composite for data input.
        createDataInputComp(mainComp);
    }

    /**
     * Create the composite to select objective aid technique name.
     *
     * @param parent
     */
    private void createTechSelectionComp(Composite parent) {
        Composite techSelectComp = new Composite(parent, SWT.NONE);
        GridLayout techSelectLayout = new GridLayout(2, false);
        techSelectLayout.marginLeft = 35;
        techSelectLayout.marginHeight = 0;
        techSelectComp.setLayout(techSelectLayout);
        techSelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label techLabel = new Label(techSelectComp, SWT.NONE);
        techLabel.setText("Technique name ");
        GridData techLabelData = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false);
        techLabel.setLayoutData(techLabelData);

        techCombo = new CCombo(techSelectComp, SWT.BORDER);
        for (String tech : allObjectiveAids.keySet()) {
            techCombo.add(tech);
        }

        techCombo.select(0);
        techCombo.setEditable(false);
        GridData techComboData = new GridData(SWT.RIGHT, SWT.CENTER, true,
                false);
        techCombo.setLayoutData(techComboData);

        techCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchTechDtg("Technique");
            }
        });
    }

    /**
     * Create the composite to select DTG.
     *
     * @param parent
     */
    private void createDtgSelectionComp(Composite parent) {
        Composite dtgSelectComp = new Composite(parent, SWT.NONE);
        GridLayout dtgSelectLayout = new GridLayout(2, false);
        dtgSelectLayout.marginWidth = 0;
        dtgSelectLayout.marginHeight = 0;
        dtgSelectComp.setLayout(dtgSelectLayout);
        dtgSelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label dtgLabel = new Label(dtgSelectComp, SWT.NONE);
        dtgLabel.setText("Select DTG (YYYYMMDDHH) ");
        GridData dtgLabelData = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgLabel.setLayoutData(dtgLabelData);

        dtgCombo = new CCombo(dtgSelectComp, SWT.BORDER);
        dtgCombo.setItems(availableDateTimeGroups);
        dtgCombo.setEditable(false);
        dtgCombo.select(0);
        GridData dtgComboData = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgCombo.setLayoutData(dtgComboData);

        dtgCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                switchTechDtg("DTG");
            }
        });

        if (selectedDateTimeGroup == null) {
            dtgCombo.select(dtgCombo.getItemCount() - 1);
        } else {
            dtgCombo.select(dtgCombo.indexOf(selectedDateTimeGroup));
        }

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createDataInputComp(Composite parent) {

        Composite dataInputComp = new Composite(parent, SWT.BORDER);
        GridLayout tableLayout = new GridLayout(4, false);
        tableLayout.horizontalSpacing = 35;
        tableLayout.verticalSpacing = 12;
        tableLayout.marginHeight = 8;
        tableLayout.marginWidth = 10;
        dataInputComp.setLayout(tableLayout);
        dataInputComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // Add parameter names at top.
        for (String param : inputParameters) {
            Label infoLbl = new Label(dataInputComp, SWT.NONE);
            infoLbl.setText(param);
            GridData infoLblData = new GridData(SWT.LEFT, SWT.CENTER, false,
                    false);
            infoLbl.setLayoutData(infoLblData);
        }

        SelectionAdapter dirSelAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateChangedRecords();
            }
        };

        // Add rows to input lat, lon, and max wind for each tau.
        for (AtcfTaus tau : AtcfTaus.values()) {
            int ii = tau.ordinal();
            tauLbls[ii] = new Label(dataInputComp, SWT.NONE);
            String tauLbl = String.format("%-4d", tau.getValue());
            tauLbls[ii].setText(tauLbl);
            GridData tauLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false);
            tauLbls[ii].setLayoutData(tauLblData);

            // Composite for latitude
            Composite currentLatDirComp = new Composite(dataInputComp,
                    SWT.NONE);
            GridLayout currentLatDirLayout = new GridLayout(3, false);
            currentLatDirLayout.marginHeight = 0;
            currentLatDirLayout.marginWidth = 2;
            currentLatDirLayout.horizontalSpacing = 8;
            currentLatDirComp.setLayout(currentLatDirLayout);
            GridData currentLatDirCompGD = new GridData(SWT.CENTER, SWT.CENTER,
                    true, false);
            currentLatDirComp.setLayoutData(currentLatDirCompGD);

            latTxts[ii] = new Text(currentLatDirComp, SWT.SINGLE | SWT.BORDER);
            GridData latTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
            latTxtGD.widthHint = 35;
            latTxts[ii].setLayoutData(latTxtGD);

            updateTextOnly(latTxts[ii], "");

            latTxts[ii].addListener(SWT.Verify,
                    verifyListeners.getLatVerifyListener());

            latNorthBtns[ii] = new Button(currentLatDirComp, SWT.RADIO);
            latNorthBtns[ii].setText("N");
            GridData latNorthBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                    false);
            latNorthBtns[ii].setLayoutData(latNorthBtnGD);
            latNorthBtns[ii].setSelection(true);

            latNorthBtns[ii].addSelectionListener(dirSelAdapter);

            latSouthBtns[ii] = new Button(currentLatDirComp, SWT.RADIO);
            latSouthBtns[ii].setText("S");
            GridData latSouthBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                    false);
            latSouthBtns[ii].setLayoutData(latSouthBtnGD);
            latSouthBtns[ii].addSelectionListener(dirSelAdapter);

            // Composite for longitude
            Composite currentLonDirComp = new Composite(dataInputComp,
                    SWT.NONE);
            GridLayout currentLonDirLayout = new GridLayout(3, false);
            currentLonDirLayout.marginHeight = 0;
            currentLonDirLayout.marginWidth = 2;
            currentLonDirLayout.horizontalSpacing = 8;
            currentLonDirComp.setLayout(currentLonDirLayout);
            GridData currentLonDirCompGD = new GridData(SWT.CENTER, SWT.DEFAULT,
                    true, false);
            currentLonDirComp.setLayoutData(currentLonDirCompGD);

            lonTxts[ii] = new Text(currentLonDirComp, SWT.SINGLE | SWT.BORDER);
            GridData lonTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
            lonTxtGD.widthHint = 40;
            lonTxts[ii].setLayoutData(lonTxtGD);

            updateTextOnly(lonTxts[ii], "");
            lonTxts[ii].addListener(SWT.Verify,
                    verifyListeners.getLonVerifyListener());

            lonEastBtns[ii] = new Button(currentLonDirComp, SWT.RADIO);
            lonEastBtns[ii].setText("E");
            GridData lonEastBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                    false);
            lonEastBtns[ii].setLayoutData(lonEastBtnGD);
            lonEastBtns[ii].addSelectionListener(dirSelAdapter);

            lonWestBtns[ii] = new Button(currentLonDirComp, SWT.RADIO);
            lonWestBtns[ii].setText("W");
            GridData lonWestBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                    false);
            lonWestBtns[ii].setLayoutData(lonWestBtnGD);
            lonWestBtns[ii].setSelection(true);
            lonWestBtns[ii].addSelectionListener(dirSelAdapter);

            // Text boxes to enter max wind
            windTxts[ii] = new Text(dataInputComp, SWT.SINGLE | SWT.BORDER);
            GridData windTxtGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                    false);
            windTxtGD.widthHint = 30;
            windTxts[ii].setLayoutData(windTxtGD);
            windTxts[ii].addListener(SWT.Verify,
                    verifyListeners.getWindTextVerifyListener());

            updateTextOnly(windTxts[ii], "");
        }
    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonCompGL = new GridLayout(4, true);
        buttonCompGL.marginBottom = 15;
        buttonCompGL.marginWidth = 40;
        buttonCompGL.horizontalSpacing = 30;
        buttonComp.setLayout(buttonCompGL);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        saveButton = new Button(buttonComp, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.setLayoutData(AtcfVizUtil.buttonGridData());
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveChanges();
            }
        });

        submitButton = new Button(buttonComp, SWT.PUSH);
        submitButton.setText("Submit");
        submitButton.setEnabled(false);
        submitButton.setLayoutData(AtcfVizUtil.buttonGridData());
        submitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfDataUtil.checkinADeckRecords(sandboxID);

                // Reset sandbox ID for this storm product.
                AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                        .getResourceData().getAtcfProduct(storm);
                prd.setAdeckSandboxID(-1);

                close();
            }
        });

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Get the data based on the user input in the dialog.
     *
     * @return A Map of ADeckRecords for current DTG with updated values on GUI
     */
    private void updateChangedRecords() {

        String dtg = dtgCombo.getText();
        String tech = techCombo.getText();

        /*
         * Update records for each TAU. If no records exists for a TAU, create
         * new records if lat/lon/Max wind are entered for that TAU.
         */
        if (isValidTechNDtg(tech, dtg)) {

            for (AtcfTaus tau : AtcfTaus.values()) {
                int ii = tau.ordinal();
                StringBuilder sbd = new StringBuilder();
                sbd.append(String.format("%-4d", tau.getValue()));

                RecordKey rkey = new RecordKey(tech, dtg, tau.getValue(),
                        ALL_WIND_RADII);
                List<ADeckRecord> recList = retrievedADeckRecords
                        .get(dtg).get(rkey);

                boolean validValues = isValuesValid(latTxts[ii], lonTxts[ii],
                        windTxts[ii]);

                if (!validValues) {
                    continue; // skip
                }

                float lat = Float.parseFloat(latTxts[ii].getText());
                float lon = Float.parseFloat(lonTxts[ii].getText());
                float wnd = Float.parseFloat(windTxts[ii].getText());

                if (!latNorthBtns[ii].getSelection()) {
                    lat = -lat;
                }

                if (!lonEastBtns[ii].getSelection()) {
                    lon = -lon;
                }

                // Update existing records if values changed.
                if (recList != null && !recList.isEmpty()) {

                    boolean changed = isRecordChanged(recList.get(0), lat, lon,
                            wnd);

                    ADeckRecord irec = initialADeckRecords.get(rkey);
                    boolean changedFromInitial = isRecordChanged(irec, lat, lon,
                            wnd);
                    if (changedFromInitial) {
                        sbd.append(" *" + recList.size());
                    }

                    /*
                     * TODO If max wind changes, ideally we should try to either
                     * create/delete records based on the new value. But it
                     * could make the logic intuitive to the user. We will not
                     * create/delete and only update for now.
                     */
                    if (changed) {
                        for (ADeckRecord orec : recList) {
                            RecordKey rk = new RecordKey(tech, dtg,
                                    tau.getValue(), (int) orec.getRadWind());
                            orec.setClat(lat);
                            orec.setClon(lon);
                            orec.setWindMax(wnd);

                            if (!changedFromInitial) {
                                changedADeckRecords.remove(rk);
                            } else {
                                changedADeckRecords.put(rk, orec);
                            }
                        }
                    }
                } else {

                    int numRecords = 1;
                    Date refTime = AtcfDataUtil.parseDtg(dtg);
                    ADeckRecord rec34 = new ADeckRecord(storm, tech, refTime,
                            tau.getValue(), 34, lat, lon, wnd);
                    RecordKey rk34 = new RecordKey(tech, dtg, tau.getValue(),
                            34);
                    changedADeckRecords.put(rk34, rec34);

                    if (wnd >= 50) {
                        ADeckRecord rec50 = new ADeckRecord(storm, tech,
                                refTime, tau.getValue(), 50, lat, lon, wnd);
                        RecordKey rk = new RecordKey(tech, dtg, tau.getValue(),
                                50);
                        changedADeckRecords.put(rk, rec50);
                        numRecords++;
                    }

                    if (wnd >= 64) {
                        ADeckRecord rec64 = new ADeckRecord(storm, tech,
                                refTime, tau.getValue(), 64, lat, lon, wnd);
                        RecordKey rk = new RecordKey(tech, dtg, tau.getValue(),
                                64);
                        changedADeckRecords.put(rk, rec64);
                        numRecords++;
                    }

                    sbd.append(" +" + numRecords);
                }

                tauLbls[ii].setText(sbd.toString());
                tauLbls[ii].pack();
            }
        }

        // Update "Save" button status.
        saveButton.setEnabled(!changedADeckRecords.isEmpty());
    }

    /**
     * Retrieve all A-Deck Data for a given DTG.
     *
     * @param dtg
     *            data time group string
     * @return Map<RecordKey, List<ADeckRecord>>
     *
     */
    private Map<RecordKey, List<ADeckRecord>> getADeckData(
            String dtg) {

        Map<RecordKey, List<ADeckRecord>> records = new HashMap<>();
        if (retrievedADeckRecords != null) {
            records = retrievedADeckRecords.get(dtg);
        } else {
            retrievedADeckRecords = new HashMap<>();
        }

        if (records == null || records.isEmpty()) {
            Map<String, List<ADeckRecord>> arecs = AtcfDataUtil
                    .retrieveADeckData(storm, new String[] { dtg }, true);

            List<ADeckRecord> recs = arecs.get(dtg);
            if (recs != null && !recs.isEmpty()) {
                records = groupADeckData(dtg, recs);
                retrievedADeckRecords.put(dtg, records);

                // Save initial lat/lon/max wind.
                for (Map.Entry<RecordKey, List<ADeckRecord>> entry : records
                        .entrySet()) {
                    ADeckRecord rec = new ADeckRecord();
                    rec.setClat(entry.getValue().get(0).getClat());
                    rec.setClon(entry.getValue().get(0).getClon());
                    rec.setWindMax(entry.getValue().get(0).getWindMax());
                    initialADeckRecords.put(entry.getKey(), rec);
                }
            }
        }

        /*
         * Update sandbox ID - sandboxID is set after the A-Deck is checked the
         * first time.
         */
        if (sandboxID <= 0) {
            sandboxID = AtcfDataUtil.getADeckSandbox(storm);
        }

        return records;
    }

    /**
     * Group a list of ADeckRecord by Tech/DTG/TAU, including all wind radii -
     * 34kt, 50kt, and 64kt).
     *
     * @param dtg
     *            data time group string
     * @param recs
     *            list of ADeckRecords for the dtg
     *
     * @return Map<RecordKey, ADeckRecord>
     */
    private Map<RecordKey, List<ADeckRecord>> groupADeckData(
            String dtg, List<ADeckRecord> recs) {

        // Group records by tech/DTG/Tau
        Map<RecordKey, List<ADeckRecord>> recordMap = new HashMap<>();

        for (ADeckRecord rec : recs) {
            String aid = rec.getTechnique();
            int tau = rec.getFcstHour();

            RecordKey rkey = new RecordKey(aid, dtg, tau, ALL_WIND_RADII);
            List<ADeckRecord> recList = recordMap
                    .computeIfAbsent(rkey, k -> new ArrayList<>());
            recList.add(rec);
        }

        return recordMap;
    }

    /**
     * Load the data based on the TECH or DTG change.
     */
    private void updateData() {

        String dtg = dtgCombo.getText();
        String tech = techCombo.getText();

        /*
         * Retrieve ADeckRecord for current DTG - each ADeck DTG will have
         * multiple records, depending on technique, forecast time (TAU in
         * seconds), and radii wind.
         */
        Map<RecordKey, List<ADeckRecord>> adeckData = getADeckData(
                dtg);
        if (adeckData != null && !adeckData.isEmpty()) {
            int ii = 0;
            for (AtcfTaus tau : AtcfTaus.values()) {
                RecordKey rkey = new RecordKey(tech, dtg, tau.getValue(),
                        ALL_WIND_RADII);
                List<ADeckRecord> tauData = adeckData.get(rkey);

                if (tauData != null && !tauData.isEmpty()) {
                    ADeckRecord rec = tauData.get(0);

                    float latValue = rec.getClat();
                    String latString = String.format("%.1f",
                            latValue < 0 ? (-1 * latValue) : latValue);
                    updateTextOnly(latTxts[ii], latString);

                    if (latValue < 0) {
                        latNorthBtns[ii].setSelection(false);
                        latSouthBtns[ii].setSelection(true);
                    } else {
                        latNorthBtns[ii].setSelection(true);
                        latSouthBtns[ii].setSelection(false);
                    }

                    float lonValue = rec.getClon();
                    String lonString = String.format("%.1f",
                            lonValue < 0 ? (-1 * lonValue) : lonValue);
                    updateTextOnly(lonTxts[ii], lonString);
                    if (lonValue < 0) {
                        lonEastBtns[ii].setSelection(false);
                        lonWestBtns[ii].setSelection(true);
                    } else {
                        lonEastBtns[ii].setSelection(true);
                        lonWestBtns[ii].setSelection(false);
                    }

                    int maxWnd = (int) rec.getWindMax();
                    String maxWndString = String.format("%d", maxWnd);
                    updateTextOnly(windTxts[ii], maxWndString);

                } else {
                    updateTextOnly(latTxts[ii], "");
                    updateTextOnly(lonTxts[ii], "");
                    updateTextOnly(windTxts[ii], "");
                }

                ii++;
            }
        }
    }

    /*
     * Listener to handle text changes in lat, lon, and max wind.
     */
    private ModifyListener txtModifyListener() {
        return e -> updateChangedRecords();
    }

    /*
     * Method to ONLY update text in lat, lon, or max wind Text .
     *
     * @param txt Text widget
     *
     * @param value String
     */
    private void updateTextOnly(Text txt, String value) {
        txt.removeModifyListener(txtModifyListener);
        txt.setText(value);
        txt.addModifyListener(txtModifyListener);
    }

    /*
     * Check if technique and DTG are valid
     */
    private boolean isValidTechNDtg(String tech, String dtg) {

        boolean valid = false;

        if (allObjectiveAids.keySet().contains(tech)
                && AtcfDataUtil.parseDtg(dtg) != null) {
            valid = true;
        }

        return valid;
    }

    /**
     * Check if an ADeckRecord has different lat, lon, max wind.
     *
     * @param rec
     *            ADeckRecord
     * @param lat
     *            new latitude
     * @param lon
     *            new longitude
     * @param wnd
     *            new max wind
     * @return boolean true - different; false - same.
     *
     */
    private boolean isRecordChanged(ADeckRecord rec, float clat, float clon,
            float wnd) {
        return ((int) (rec.getClat() * 100) != (int) (clat * 100)
                || (int) (rec.getClon() * 100) != (int) (clon * 100)
                || ((int) rec.getWindMax() != (int) wnd));
    }

    /**
     * Check if lat, lon, max wind widgets have valid values.
     *
     * @param latTxt
     *            Widget for latitude
     * @param lonTxt
     *            Widget for longitude
     * @param wndTxt
     *            Widget for max wind
     * @return boolean true - valid; false - at least one widget has invalid
     *         value.
     *
     */
    private boolean isValuesValid(Text latTxt, Text lonTxt, Text wndTxt) {
        boolean validChanges = true;
        try {
            Float.parseFloat(latTxt.getText());
            Float.parseFloat(lonTxt.getText());
            Float.parseFloat(wndTxt.getText());
        } catch (NumberFormatException ee) {
            validChanges = false;
            logger.warn(
                    "EnterObjAidsNoWindRadiiDialog - Error parsing text value for latitude, longitude, or max wind.",
                    ee);
        }

        return validChanges;
    }

    /**
     * Save changes for current Tech/DTG, if any.
     */
    private void saveChanges() {

        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
        Set<String> updatedDtgs = new TreeSet<>();

        // Build a list of ModifiedDeckRecord.
        for (Map.Entry<RecordKey, ADeckRecord> entry : changedADeckRecords
                .entrySet()) {
            RecordKey rk = entry.getKey();
            ADeckRecord srec = entry.getValue();

            RecordEditType editType = RecordEditType.MODIFY;
            if (srec.getId() <= 0) {
                editType = RecordEditType.NEW;
            }

            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            mdr.setRecord(srec);
            modifiedRecords.add(mdr);

            updatedDtgs.add(rk.getDtg());
        }

        // batch update into the sandbox
        if (!modifiedRecords.isEmpty()) {
            AtcfDataUtil.updateDeckRecords(sandboxID, AtcfDeckType.A,
                    modifiedRecords);
        }

        /*
         * Refresh the records in retrievedADeckRecords - this helps to get
         * "id"s back for new records, which is generated by DB.
         */
        if (!updatedDtgs.isEmpty()) {
            for (String dtg : updatedDtgs) {
                retrievedADeckRecords.remove(dtg);
                getADeckData(dtg);
            }
        }

        // Clear up.
        changedADeckRecords.clear();

        // Redraw aids if currently displayed
        AtcfVizUtil.redrawAids(storm);

        saveButton.setEnabled(false);
        submitButton.setEnabled(true);
    }

    /**
     * Switch technique or DTG - ask fro saving before switching.
     *
     * @param techdtg
     *            tech or dtg
     */
    private void switchTechDtg(String techdtg) {

        if (!changedADeckRecords.isEmpty()) {

            boolean save = MessageDialog.openConfirm(shell, "Save changes?",
                    "There are unsaved changes. Switching " + techdtg
                            + " will lose the changes. Save now?");

            if (save) {
                saveChanges();
            }

            changedADeckRecords.clear();
            saveButton.setEnabled(false);
        }

        updateData();
    }
}

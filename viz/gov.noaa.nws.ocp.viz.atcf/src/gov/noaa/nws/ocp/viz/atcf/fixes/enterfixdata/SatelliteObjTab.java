/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.FixSiteEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.FixSites;
import gov.noaa.nws.ocp.common.atcf.configuration.FixTypeEntry;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FixFormat;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * The Composite for the Satellite Obj. Dvorak tab
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2019 54779      wpaintsil  Initial creation.
 * Aug 15, 2019 65561      dmanzella  implemented backend functionality
 * Oct 20, 2019 68738      dmanzella  implemented edit functionality
 * Apr 01, 2021 87786      wpaintsil  Revise UI.
 * Jun 24, 2021 91759      wpaintsil  Remove unnecessary parameters.
 * Nov 09, 2022 109847     jwu        Fix "Sat Type" validation issue in DR23333.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class SatelliteObjTab extends EditFixesTab {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SatelliteObjTab.class);

    private Text latText;

    private Text lonText;

    private Button visualButton;

    private Button infraButton;

    private Button microButton;

    private Combo fixSiteCombo;

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Button eastButton;

    private Button northButton;

    private Button westButton;

    private Button southButton;

    private Button goodButton;

    private Combo altSceneCombo;

    private Button fairButton;

    private Button poorButton;

    private Button ciGoodButton;

    private Button ciFairButton;

    private Button ciPoorButton;

    private Combo tNumDerCombo;

    private Combo tNumRawCombo;

    private Combo algorithmCombo;

    private Text commentText;

    private Text initialsText;

    private Combo cNumCombo;

    private Combo tNumCombo;

    private Combo eyeTempCombo;

    private Combo tNumAvCombo;

    private Combo tempCloudCombo;

    private Combo satTypeCombo;

    private Button tropicalButton;

    private Button subTropButton;

    private Button extraTropButton;

    private String centerInts;

    private Date currDate;

    private FixTypeEntry typeEntry;

    private String typeName;

    private static final String NONE = "none";

    /**
     * Collection of verify listeners.
     */
    private final AtcfTextListeners verifyListeners = new AtcfTextListeners();

    /**
     * Constructor
     *
     * @param parent
     * @param dtgSelectionList
     */
    public SatelliteObjTab(TabFolder parent, String tabName, Storm storm,
            boolean editData, String[] actionBtnNames, int sandBoxId,
            Map<FDeckRecordKey, List<FDeckRecord>> fixData,
            org.eclipse.swt.widgets.List dtgSelectionList) {
        super(parent, SWT.NONE, dtgSelectionList);
        this.editData = editData;
        this.actionBtnNames = actionBtnNames;
        this.sandBoxID = sandBoxId;
        this.storm = storm;
        this.fixDataMap = fixData;
        shell = getShell();
        selectedRecords = new ArrayList<>();
        currDate = new Date();
        typeEntry = AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("DVTO");
        if (typeEntry != null) {
            typeName = typeEntry.getName();
        } else {
            typeName = " ";
        }

        TabItem tab = new TabItem(parent, SWT.NONE);
        tab.setText(tabName);
        this.tabName = tabName;

        GridLayout tabLayout = new GridLayout(1, false);

        tabLayout.marginHeight = 20;
        tabLayout.marginWidth = 0;
        tabLayout.verticalSpacing = 0;

        this.setLayout(tabLayout);
        this.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        tab.setControl(this);

        createComponents();

        addActionButtons();
    }

    /**
     * Assemble the composites for this tab.
     */
    private void createComponents() {
        Group mainComposite = new Group(this, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginWidth = 30;
        mainLayout.verticalSpacing = 22;
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.CENTER, SWT.FILL, true,
                true);
        mainComposite.setLayoutData(mainLayoutData);

        createIDComp(mainComposite);
        createCIComp(mainComposite);
        createCINumberComp(mainComposite);
        createCommentComp(mainComposite);

        new Label(mainComposite, SWT.NONE)
                .setText(EditFixesDataDialog.ASTERISK_NOTE);

        // Find the first valid record and fill in current data if in edit mode
        if (editData) {
            // if the first values don't have an associated record, find some
            // that do
            if (!findCurrentRec() && !fixDataMap.isEmpty()) {
                // Get an arbitrary fdeckrecord from the map
                Map.Entry<FDeckRecordKey, List<FDeckRecord>> entry = fixDataMap
                        .entrySet().iterator().next();
                FDeckRecord rec = entry.getValue().get(0);

                // Use it to set the initial record to fill
                try {
                    dtgText.setText(TabUtility.formatDtg(rec.getRefTime()));
                } catch (RuntimeException e) {
                    logger.warn("SatelliteObjTab" + TabUtility.DTG_ERROR_STRING,
                            e);
                }
                satTypeCombo.setText(rec.getSatelliteType());
                fixSiteCombo.setText(rec.getFixSite());
            }
            fillData();
        }

        centerInts = TabUtility.setCenterIntensity(centerFixChk.getSelection(),
                maxWindFixChk.getSelection(), false, false);
    }

    /**
     * Composite to pick DTG, site, satellite type.
     */
    private void createIDComp(Composite parent) {
        Composite dtgSiteComp = new Composite(parent, SWT.NONE);
        GridLayout dtgSiteLayout = new GridLayout(3, false);
        dtgSiteLayout.marginHeight = 0;
        dtgSiteLayout.horizontalSpacing = 20;
        dtgSiteComp.setLayout(dtgSiteLayout);
        dtgSiteComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite dtgComp = new Composite(dtgSiteComp, SWT.NONE);
        GridLayout dtgLayout = new GridLayout(2, false);
        dtgLayout.marginWidth = 0;
        dtgComp.setLayout(dtgLayout);
        dtgComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label dtgLbl = new Label(dtgComp, SWT.NONE);
        dtgLbl.setText("* DTG (YYYYMMDDHHMN)");

        if (editData) {
            dtgText = new Text(dtgComp, SWT.BORDER | SWT.READ_ONLY);
        } else {
            dtgText = new Text(dtgComp, SWT.BORDER);
        }

        List<String> dtgList = new ArrayList<>();

        for (FDeckRecordKey key : fixDataMap.keySet()) {
            if (!dtgList.contains(key.getDtg())) {
                dtgList.add(key.getDtg());
            }
        }

        if (!dtgList.isEmpty()) {
            dtgText.setText(dtgList.get(0));
        } else {
            dtgText.setText(TabUtility.defaultDtgText(storm));
        }

        // We only want this in Edit mode
        if (editData) {
            dtgText.addModifyListener(e -> fillData());
        }

        Composite siteComp = new Composite(dtgSiteComp, SWT.NONE);
        GridLayout siteLayout = new GridLayout(2, false);
        siteComp.setLayout(siteLayout);
        siteComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label siteLbl = new Label(siteComp, SWT.NONE);
        siteLbl.setText("* Fix Site");

        if (editData) {
            fixSiteCombo = new Combo(siteComp, SWT.BORDER | SWT.READ_ONLY);
        } else {
            fixSiteCombo = new Combo(siteComp, SWT.BORDER);
        }

        FixSites fixSites = AtcfConfigurationManager.getInstance()
                .getFixSites();
        for (FixSiteEntry site : fixSites.getFixSites()) {
            fixSiteCombo.add(site.getName());
        }

        fixSiteCombo.select(0);
        if (editData) {
            fixSiteCombo.addModifyListener(e -> fillData());
        }

        Composite satTypeComp = new Composite(dtgSiteComp, SWT.NONE);

        GridLayout satTypeLayout = new GridLayout(2, false);
        satTypeComp.setLayout(satTypeLayout);
        satTypeComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label satTypeLbl = new Label(satTypeComp, SWT.NONE);
        satTypeLbl.setText("* Satellite Type");

        if (editData) {
            satTypeCombo = new Combo(satTypeComp, SWT.BORDER | SWT.READ_ONLY);
        } else {
            satTypeCombo = new Combo(satTypeComp, SWT.BORDER);
        }
        // TODO pull data from sattypes.dat
        satTypeCombo.add("GOES16");
        satTypeCombo.add("GOES15");
        satTypeCombo.add("GOES14");
        satTypeCombo.add("GOES13");
        satTypeCombo.add("MET10");
        satTypeCombo.add("HIM18");
        satTypeCombo.add("NOAA15");
        satTypeCombo.add("NOAA18");
        satTypeCombo.add("METOPA");
        satTypeCombo.add("METOPB");
        satTypeCombo.select(0);

        if (editData) {
            satTypeCombo.addModifyListener(e -> fillData());
        }
    }

    /**
     * Assemble the first few rows of widgets from the "Center/Intensity"
     * options up to "Satellite Type."
     */
    private void createCIComp(Composite parent) {
        Group topComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        topComp.setLayout(topLayout);
        topComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label centerIntensityLbl = new Label(topComp, SWT.NONE);
        centerIntensityLbl.setText("*  Center/Intensity");

        Composite centerWindComp = new Composite(topComp, SWT.NONE);
        GridLayout centerWindLayout = new GridLayout(2, false);
        centerWindLayout.marginWidth = 0;
        centerWindComp.setLayout(centerWindLayout);
        centerWindComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        SelectionAdapter centerIntsAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                centerInts = TabUtility.setCenterIntensity(
                        centerFixChk.getSelection(),
                        maxWindFixChk.getSelection(), false, false);
            }
        };

        centerFixChk = new Button(centerWindComp, SWT.CHECK);
        centerFixChk.setText("Center Fix");
        centerFixChk.addSelectionListener(centerIntsAdapter);

        maxWindFixChk = new Button(centerWindComp, SWT.CHECK);
        maxWindFixChk.setText("Max Wind Speed Fix");
        maxWindFixChk.addSelectionListener(centerIntsAdapter);

        Composite latitudeComp = new Composite(topComp, SWT.NONE);
        GridLayout latitudeLayout = new GridLayout(4, false);
        latitudeLayout.marginWidth = 0;
        latitudeComp.setLayout(latitudeLayout);
        latitudeComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        Label latLabel = new Label(latitudeComp, SWT.NONE);
        latLabel.setText("Latitude");

        latText = new Text(latitudeComp, SWT.BORDER);
        latText.addListener(SWT.Verify, verifyListeners.getLatVerifyListener());

        northButton = new Button(latitudeComp, SWT.RADIO);
        northButton.setText("N");
        northButton.setSelection(true);

        southButton = new Button(latitudeComp, SWT.RADIO);
        southButton.setText("S");

        Composite longitudeComp = new Composite(topComp, SWT.NONE);
        GridLayout longitudeLayout = new GridLayout(4, false);
        longitudeLayout.marginWidth = 0;
        longitudeComp.setLayout(longitudeLayout);
        longitudeComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false));

        Label lonLabel = new Label(longitudeComp, SWT.NONE);
        lonLabel.setText("Longitude");

        lonText = new Text(longitudeComp, SWT.BORDER);
        lonText.addListener(SWT.Verify, verifyListeners.getLonVerifyListener());

        GridData latGridData = new GridData();
        latGridData.widthHint = 60;
        latText.setLayoutData(latGridData);
        GridData lonGridData = new GridData();
        lonGridData.widthHint = 60;
        lonText.setLayoutData(lonGridData);

        eastButton = new Button(longitudeComp, SWT.RADIO);
        eastButton.setText("E");
        westButton = new Button(longitudeComp, SWT.RADIO);
        westButton.setText("W");
        westButton.setSelection(true);

        Label posConfLbl = new Label(topComp, SWT.NONE);
        posConfLbl.setText("Position Confidence");
        Composite pastChComp = new Composite(topComp, SWT.NONE);
        GridLayout pastChLayout = new GridLayout(4, false);
        pastChLayout.marginWidth = 0;
        pastChComp.setLayout(pastChLayout);
        pastChComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        goodButton = new Button(pastChComp, SWT.RADIO);
        goodButton.setText("Good");
        fairButton = new Button(pastChComp, SWT.RADIO);
        fairButton.setText("Fair");
        fairButton.setSelection(true);
        poorButton = new Button(pastChComp, SWT.RADIO);
        poorButton.setText("Poor");

    }

    /**
     * Create the set of fields from "CI Number" up to "Algorithm."
     *
     * @param parent
     */
    private void createCINumberComp(Composite parent) {
        Group longTermComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        longTermComp.setLayout(topLayout);
        longTermComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label cNumLbl = new Label(longTermComp, SWT.NONE);
        cNumLbl.setText("* CI Number");
        cNumCombo = new Combo(longTermComp, SWT.BORDER | SWT.READ_ONLY);

        cNumCombo.add(NONE);
        TabUtility.populateNumericCombo(cNumCombo, 1.0, 8.0, .1);
        cNumCombo.select(0);

        Label ciConfLbl = new Label(longTermComp, SWT.NONE);
        ciConfLbl.setText("CI Confidence");
        Composite ciConfComp = new Composite(longTermComp, SWT.NONE);
        GridLayout ciConfLayout = new GridLayout(4, false);
        ciConfComp.setLayout(ciConfLayout);
        ciConfComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        ciGoodButton = new Button(ciConfComp, SWT.RADIO);
        ciGoodButton.setText("Good");
        ciFairButton = new Button(ciConfComp, SWT.RADIO);
        ciFairButton.setText("Fair");
        ciFairButton.setSelection(true);
        ciPoorButton = new Button(ciConfComp, SWT.RADIO);
        ciPoorButton.setText("Poor");

        Label avgTNumLbl = new Label(longTermComp, SWT.NONE);
        avgTNumLbl.setText("Average T-Number");
        tNumCombo = new Combo(longTermComp, SWT.BORDER | SWT.READ_ONLY);
        tNumCombo.add(NONE);
        TabUtility.populateNumericCombo(tNumCombo, 1.0, 8.0, .1);
        tNumCombo.select(0);

        Label tNumAvgLbl = new Label(longTermComp, SWT.NONE);
        tNumAvgLbl.setText("T-Num averaging time period");
        Composite tNumAvgComp = new Composite(longTermComp, SWT.NONE);
        GridLayout tNumAvgLayout = new GridLayout(2, false);
        tNumAvgLayout.marginWidth = 0;
        tNumAvgComp.setLayout(tNumAvgLayout);
        tNumAvgComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        tNumAvCombo = new Combo(tNumAvgComp, SWT.BORDER | SWT.READ_ONLY);
        tNumAvCombo.add(NONE);
        TabUtility.populateIntNumericCombo(tNumAvCombo, 1, 24, 1);
        tNumAvCombo.select(0);
        new Label(tNumAvgComp, SWT.NONE).setText("hours");

        Label tNumDerLbl = new Label(longTermComp, SWT.NONE);
        tNumDerLbl.setText("T-Num Averaging Derivation");
        tNumDerCombo = new Combo(longTermComp, SWT.BORDER);
        tNumDerCombo.add("L - straight linear");
        tNumDerCombo.add("T - time weighted");

        Label tNumRawLbl = new Label(longTermComp, SWT.NONE);
        tNumRawLbl.setText("T-Number (Raw)");
        tNumRawCombo = new Combo(longTermComp, SWT.BORDER | SWT.READ_ONLY);
        tNumRawCombo.add(NONE);
        TabUtility.populateNumericCombo(tNumRawCombo, 1.0, 8.0, .1);
        tNumRawCombo.select(0);

        Label eyeTempLbl = new Label(longTermComp, SWT.NONE);
        eyeTempLbl.setText("Eye Temperature");
        Composite eyeTempComp = new Composite(longTermComp, SWT.NONE);
        GridLayout eyeTempLayout = new GridLayout(2, false);
        eyeTempLayout.marginWidth = 0;
        eyeTempComp.setLayout(eyeTempLayout);
        eyeTempComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        eyeTempCombo = new Combo(eyeTempComp, SWT.BORDER | SWT.READ_ONLY);
        eyeTempCombo.add(NONE);
        TabUtility.populateIntNumericCombo(eyeTempCombo, -99, 50, 1);
        eyeTempCombo.select(0);
        new Label(eyeTempComp, SWT.NONE).setText("celsius");

        Label tempCloudLbl = new Label(longTermComp, SWT.NONE);
        tempCloudLbl.setText("Temp. of cloud surrounding eye");
        Composite tempCloudComp = new Composite(longTermComp, SWT.NONE);
        GridLayout tempCloudLayout = new GridLayout(2, false);
        tempCloudLayout.marginWidth = 0;
        tempCloudComp.setLayout(tempCloudLayout);
        tempCloudComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        tempCloudCombo = new Combo(tempCloudComp, SWT.BORDER | SWT.READ_ONLY);
        tempCloudCombo.add(NONE);
        TabUtility.populateIntNumericCombo(tempCloudCombo, -99, 50, 1);
        tempCloudCombo.select(0);
        new Label(tempCloudComp, SWT.NONE).setText("celsius");

        Label altSceneLbl = new Label(longTermComp, SWT.NONE);
        altSceneLbl.setText("* Scene Type");
        altSceneCombo = new Combo(longTermComp, SWT.BORDER | SWT.READ_ONLY);
        altSceneCombo.add("CDO - central dense overcase");
        altSceneCombo.add("EYE - defineable eye");
        altSceneCombo.add("EMBC - embedded center");
        altSceneCombo.add("CBND - curved band");
        altSceneCombo.add("SHER - wind shear causing partially exposed eye");

        Label algorithmLbl = new Label(longTermComp, SWT.NONE);
        algorithmLbl.setText("Algorithm");
        algorithmCombo = new Combo(longTermComp, SWT.BORDER);
        algorithmCombo.add("AD - Advanced ODT");
        algorithmCombo.add("R9 - Rule 9");
        algorithmCombo.add("RP - Rapid");
    }

    /**
     * Create the remaining fields at the bottom of the tab up to the "Initials"
     * field.
     *
     * @param parent
     */
    private void createCommentComp(Composite parent) {
        Group bottomComp = new Group(parent, SWT.NONE);
        GridLayout bottomLayout = new GridLayout(2, false);
        bottomLayout.marginBottom = 5;
        bottomLayout.marginWidth = 15;
        bottomLayout.horizontalSpacing = 20;
        bottomComp.setLayout(bottomLayout);
        bottomComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label sensorTypeLbl = new Label(bottomComp, SWT.NONE);
        sensorTypeLbl.setText("*  Sensor Type");

        Composite sensorTypeComp = new Composite(bottomComp, SWT.NONE);
        GridLayout sensorTypeLayout = new GridLayout(3, false);
        sensorTypeComp.setLayout(sensorTypeLayout);
        sensorTypeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        visualButton = new Button(sensorTypeComp, SWT.CHECK);
        visualButton.setText("Visual");
        infraButton = new Button(sensorTypeComp, SWT.CHECK);
        infraButton.setText("Infrared");
        microButton = new Button(sensorTypeComp, SWT.CHECK);
        microButton.setText("Microwave");

        Composite tropicTypeComp = new Composite(bottomComp, SWT.NONE);
        GridLayout tropicTypeLayout = new GridLayout(3, false);
        tropicTypeLayout.marginWidth = 0;
        tropicTypeComp.setLayout(tropicTypeLayout);
        GridData tropicTypeData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        tropicTypeData.horizontalSpan = 2;
        tropicTypeComp.setLayoutData(tropicTypeData);

        tropicalButton = new Button(tropicTypeComp, SWT.RADIO);
        tropicalButton.setText("Tropical");
        tropicalButton.setSelection(true);
        subTropButton = new Button(tropicTypeComp, SWT.RADIO);
        subTropButton.setText("SubTropical");
        extraTropButton = new Button(tropicTypeComp, SWT.RADIO);
        extraTropButton.setText("ExtraTropical");

        Label commentLabel = new Label(bottomComp, SWT.NONE);
        commentLabel.setText("Comments");

        GridData commentGridData = new GridData();
        commentGridData.widthHint = 500;
        commentText = new Text(bottomComp, SWT.BORDER);
        commentText.setLayoutData(commentGridData);

        Label initialsLabel = new Label(bottomComp, SWT.NONE);
        initialsLabel.setText("Initials");
        initialsText = new Text(bottomComp, SWT.BORDER);
        GridData initialsGridData = new GridData();
        initialsGridData.widthHint = 60;
        initialsText.setLayoutData(initialsGridData);
    }

    /**
     * Compiles the modified records, prepares them for saving
     */
    @Override
    protected boolean saveNewRecords() {

        boolean ret = false;
        String sensorText = TabUtility.saveSensorType(visualButton, infraButton,
                microButton);
        currDate = TabUtility.checkDtg(dtgText.getText());
        if (TabUtility.isRecordValid(latText.getText(), lonText.getText(),
                northButton, westButton, !(centerInts.isEmpty()),
                !(findCurrentRec()), currDate, shell,
                !(satTypeCombo.getText().isEmpty()), !(sensorText.isEmpty()),
                !(fixSiteCombo.getText().isEmpty()), true,
                !(NONE.equals(cNumCombo.getText())),
                !(altSceneCombo.getText().isEmpty()), true)) {

            FDeckRecord fDeckRecord = new FDeckRecord();

            fDeckRecord.setRefTime(new Date(currDate.getTime()));
            fDeckRecord.setCycloneNum(storm.getCycloneNum());
            fDeckRecord.setBasin(storm.getRegion());
            fDeckRecord.setSubRegion(storm.getSubRegion());
            fDeckRecord.setFixFormat(FixFormat.OBJECTIVE_DVORAK.getValue());
            fDeckRecord.setYear(storm.getYear());
            fDeckRecord.setFixType(typeName);

            saveFieldsToRec(fDeckRecord);

            List<FDeckRecord> recs = new ArrayList<>();
            recs.add(fDeckRecord);

            ret = validRecordUpdate(recs);
        }

        return ret;
    }

    /**
     * Finds and returns the FDeckRecords based off of the current DTG, FixSite,
     * Tab, and Sat Type. Returns false if no record based off of that
     * combination exists.
     *
     * @return Current FDeckRecord
     */
    @Override
    protected boolean findCurrentRec() {
        boolean ret = false;
        FDeckRecordKey tempKey = new FDeckRecordKey(dtgText.getText(),
                fixSiteCombo.getText(), typeName, satTypeCombo.getText());

        if (fixDataMap.containsKey(tempKey)) {
            selectedRecords = fixDataMap.get(tempKey);
            ret = true;
        }

        return ret;
    }

    /**
     * When in edit mode, and a new record is selected, fill in fields from the
     * record's data
     *
     * @param fDeckRecord
     *            The record to get the data from
     */
    @Override
    protected void setFields(ArrayList<FDeckRecord> fDeckRecords) {
        FDeckRecord fDeckRecord = fDeckRecords.get(0);

        latText.setText(TabUtility.saveLat(fDeckRecord.getClat(), northButton,
                southButton));
        lonText.setText(TabUtility.saveLon(fDeckRecord.getClon(), eastButton,
                westButton));
        TabUtility.saveCenterIntensity(fDeckRecord.getCenterOrIntensity(),
                centerFixChk, maxWindFixChk, null, null);

        if (fDeckRecord.getCiNum() == TabUtility.DEFAULT) {
            cNumCombo.setText(NONE);
        } else {
            cNumCombo.setText(String.valueOf(fDeckRecord.getCiNum()));
        }

        tNumCombo.setText(String.valueOf(fDeckRecord.gettNumAverage()));
        tNumAvCombo.setText(
                String.valueOf((int) fDeckRecord.gettNumAveragingTimePeriod()));
        tNumDerCombo.setText(fDeckRecord.gettNumAveragingDerivation());
        tNumRawCombo.setText(fDeckRecord.gettNumRaw());
        eyeTempCombo.setText(String.valueOf((int) fDeckRecord.getTempEye()));
        tempCloudCombo.setText(
                String.valueOf((int) fDeckRecord.getTempCloudSurroundingEye()));

        algorithmCombo.setText(fDeckRecord.getAlgorithm());
        altSceneCombo.setText(
                SceneType.getValueFromString(fDeckRecord.getSceneType()));

        TabUtility.setConfidence(fDeckRecord.getCiConfidence(), ciGoodButton,
                ciFairButton, ciPoorButton);
        TabUtility.setConfidence(fDeckRecord.getPositionConfidence(),
                goodButton, fairButton, poorButton);

        TabUtility.setSensorType(fDeckRecord.getSensorType(), visualButton,
                infraButton, microButton);
        TabUtility.setTropics(fDeckRecord.getTropicalIndicator(),
                tropicalButton, subTropButton, extraTropButton);
        commentText.setText(fDeckRecord.getComments());
        initialsText.setText(fDeckRecord.getInitials());
    }

    /**
     * Reset all fields for a new entry
     */
    @Override
    protected void clearFields() {
        fixSiteCombo.setText("");
        satTypeCombo.setText("");
        centerFixChk.setSelection(false);
        maxWindFixChk.setSelection(false);
        latText.setText("");
        lonText.setText("");
        goodButton.setSelection(false);
        fairButton.setSelection(true);
        poorButton.setSelection(false);
        ciFairButton.setSelection(true);
        ciGoodButton.setSelection(false);
        ciPoorButton.setSelection(false);

        cNumCombo.setText(NONE);
        tNumAvCombo.setText(NONE);
        tNumDerCombo.setText("");
        tNumCombo.setText(NONE);
        tNumRawCombo.setText(NONE);
        eyeTempCombo.setText(NONE);
        tempCloudCombo.setText(NONE);

        altSceneCombo.setText("");
        algorithmCombo.setText("");
        visualButton.setSelection(false);
        infraButton.setSelection(false);
        microButton.setSelection(false);
        tropicalButton.setSelection(true);
        subTropButton.setSelection(false);
        extraTropButton.setSelection(false);
        commentText.setText("");
        initialsText.setText("");
    }

    /**
     * Saves the current values of the editable fields to the provided
     * FDeckRecord
     *
     * @param fDeckRecord
     */
    @Override
    protected void saveFieldsToRec(FDeckRecord fDeckRecord) {
        fDeckRecord.setCenterOrIntensity(centerInts);
        fDeckRecord.setClon(AtcfVizUtil.getStringAsFloat(lonText.getText(),
                westButton.getSelection(), false, 0));
        fDeckRecord.setClat(AtcfVizUtil.getStringAsFloat(latText.getText(),
                southButton.getSelection(), false, 0));

        fDeckRecord.setSatelliteType(satTypeCombo.getText());
        fDeckRecord.setCiNum(AtcfVizUtil.getStringAsFloat(cNumCombo.getText(),
                false, true, TabUtility.DEFAULT));

        if (!NONE.equals(tNumDerCombo.getText())) {
            fDeckRecord.settNumAveragingDerivation(tNumDerCombo.getText());
        }

        if (!NONE.equals(tNumRawCombo.getText())) {
            fDeckRecord.settNumRaw(tNumRawCombo.getText());
        }

        if (!NONE.equals(algorithmCombo.getText())) {
            fDeckRecord.setAlgorithm(algorithmCombo.getText());
        }

        fDeckRecord.setSensorType(TabUtility.saveSensorType(visualButton,
                infraButton, microButton));
        fDeckRecord.setFixSite(fixSiteCombo.getText());
        fDeckRecord.setComments(commentText.getText());
        fDeckRecord.setInitials(initialsText.getText());

        fDeckRecord.setSceneType(
                SceneType.getNameFromString(altSceneCombo.getText()).name());

        fDeckRecord.setPositionConfidence(
                TabUtility.saveConfidence(goodButton, fairButton, poorButton));
        fDeckRecord.setCiConfidence(TabUtility.saveConfidence(ciGoodButton,
                ciFairButton, ciPoorButton));

        fDeckRecord.setTropicalIndicator(TabUtility.saveTropics(tropicalButton,
                subTropButton, extraTropButton));

        fDeckRecord.settNumAverage(AtcfVizUtil.getStringAsFloat(
                tNumCombo.getText(), false, true, TabUtility.DEFAULT));

        fDeckRecord.setTempEye(AtcfVizUtil.getStringAsFloat(
                eyeTempCombo.getText(), false, true, TabUtility.DEFAULT));

        fDeckRecord.settNumAveragingTimePeriod(AtcfVizUtil.getStringAsFloat(
                tNumAvCombo.getText(), false, true, TabUtility.DEFAULT));

        fDeckRecord.setTempCloudSurroundingEye(AtcfVizUtil.getStringAsFloat(
                tempCloudCombo.getText(), false, true, TabUtility.DEFAULT));
    }

    /**
     * Enum representing the SceneType in the Satellite Obj Dvorak tab
     *
     * TODO Replace this with data from scenetypes.dat
     */
    enum SceneType {
        CDO("CDO - central dense overcase"),
        EYE("EYE - defineable eye"),
        EMBC("EMBC - embedded center"),
        CBND("CBND - curved band"),
        SHER("SHER - wind shear causing partially exposed eye");

        private String value;

        SceneType(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        public static SceneType getNameFromString(String value) {
            SceneType ret = SceneType.CDO;
            for (SceneType name : SceneType.values()) {
                if (value.equals(name.getValue())) {
                    ret = name;
                }
            }

            return ret;
        }

        /**
         * @param val
         * @return
         */
        public static String getValueFromString(String value) {
            String ret = "";
            for (SceneType name : SceneType.values()) {
                if (value.equals(name.name())) {
                    ret = name.getValue();
                }
            }

            return ret;
        }

    }

}

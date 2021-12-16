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
 * The Composite for the Dropsonde tab
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2019 54779      wpaintsil  Initial creation.
 * Aug 15, 2019 65561      dmanzella  implemented backend functionality
 * Oct 20, 2019 68738      dmanzella  implemented edit functionality
 * Apr 01, 2021 87786      wpaintsil  Revise UI.
 * Jun 24, 2021 91759      wpaintsil  Replace default 999999 with whitespace.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class DropsondeTab extends EditFixesTab {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DropsondeTab.class);

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Combo fixSiteCombo;

    private Text latText;

    private Text lonText;

    private Button southButton;

    private Button eastButton;

    private Button westButton;

    private Button northButton;

    private Text commentText;

    private Combo sondeCombo;

    private Text initialsText;

    private Text layer500Text;

    private Text lowest150Text;

    private Text midHeightText;

    private Button goodButton1;

    private Button goodButton2;

    private Button goodButton3;

    private Button fairButton1;

    private Button fairButton2;

    private Button fairButton3;

    private Button poorButton1;

    private Button poorButton2;

    private Button poorButton3;

    private Text minSeaText;

    private Text sfcWindText;

    private Date currDate;

    private String centerInts;

    private FixTypeEntry typeEntry;

    private String typeName;

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
    public DropsondeTab(TabFolder parent, String tabName, Storm storm,
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
        currDate = new Date();
        selectedRecords = new ArrayList<>();
        typeEntry = AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("DRPS");
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
        createLowest150Comp(mainComposite);
        createCommentsComp(mainComposite);

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
                    logger.warn("DropsondeTab" + TabUtility.DTG_ERROR_STRING,
                            e);
                }
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
    }

    /**
     * Assemble the bulk of the fields from the "Center/Intensity" options up to
     * "Midpoint 150 m height."
     */
    private void createCIComp(Composite parent) {
        Group topComp1 = new Group(parent, SWT.NONE);
        GridLayout topLayout1 = new GridLayout(2, false);
        topLayout1.marginBottom = 5;
        topLayout1.marginWidth = 15;
        topLayout1.horizontalSpacing = 20;
        topComp1.setLayout(topLayout1);
        topComp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label centerIntensityLbl = new Label(topComp1, SWT.NONE);
        centerIntensityLbl.setText("* Center/Intensity ");

        Composite centerWindComp = new Composite(topComp1, SWT.NONE);
        GridLayout centerWindLayout = new GridLayout(2, false);
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

        Composite latitudeComp = new Composite(topComp1, SWT.NONE);
        GridLayout latitudeLayout = new GridLayout(4, false);
        latitudeLayout.marginWidth = 0;
        latitudeComp.setLayout(latitudeLayout);
        latitudeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label latLabel = new Label(latitudeComp, SWT.NONE);
        latLabel.setText("Latitude");

        latText = new Text(latitudeComp, SWT.BORDER);
        latText.addListener(SWT.Verify, verifyListeners.getLatVerifyListener());

        northButton = new Button(latitudeComp, SWT.RADIO);
        northButton.setText("N");
        northButton.setSelection(true);
        southButton = new Button(latitudeComp, SWT.RADIO);
        southButton.setText("S");

        Composite longitudeComp = new Composite(topComp1, SWT.NONE);
        GridLayout longitudeLayout = new GridLayout(4, false);
        longitudeComp.setLayout(longitudeLayout);
        longitudeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

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

        Group topComp2 = new Group(parent, SWT.NONE);
        GridLayout topLayout2 = new GridLayout(2, false);
        topLayout2.marginBottom = 5;
        topLayout2.marginWidth = 15;
        topLayout2.horizontalSpacing = 20;
        topComp2.setLayout(topLayout2);
        topComp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label posConfLbl = new Label(topComp2, SWT.NONE);
        posConfLbl.setText("Position Confidence");
        Composite pastChComp = new Composite(topComp2, SWT.NONE);
        GridLayout pastChLayout = new GridLayout(4, false);
        pastChLayout.marginWidth = 0;
        pastChComp.setLayout(pastChLayout);
        pastChComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
        goodButton1 = new Button(pastChComp, SWT.RADIO);
        goodButton1.setText("Good");
        fairButton1 = new Button(pastChComp, SWT.RADIO);
        fairButton1.setText("Fair");
        fairButton1.setSelection(true);
        poorButton1 = new Button(pastChComp, SWT.RADIO);
        poorButton1.setText("Poor");

        Composite sfcWindComp = new Composite(topComp2, SWT.NONE);
        GridLayout sfcWindLayout = new GridLayout(2, false);
        sfcWindLayout.marginWidth = 0;
        sfcWindComp.setLayout(sfcWindLayout);
        sfcWindComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        new Label(sfcWindComp, SWT.NONE).setText("Sfc. Wind Speed");
        sfcWindText = new Text(sfcWindComp, SWT.BORDER);
        GridData sfcGridData = new GridData();
        sfcGridData.widthHint = 50;
        sfcWindText.setLayoutData(sfcGridData);

        Composite sfcConfComp = new Composite(topComp2, SWT.NONE);
        GridLayout sfcConfLayout = new GridLayout(4, false);
        sfcConfLayout.marginWidth = 0;
        sfcConfComp.setLayout(sfcConfLayout);
        sfcConfComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        new Label(sfcConfComp, SWT.NONE).setText("\tConfidence ");

        goodButton2 = new Button(sfcConfComp, SWT.RADIO);
        goodButton2.setText("Good");
        fairButton2 = new Button(sfcConfComp, SWT.RADIO);
        fairButton2.setText("Fair");
        fairButton2.setSelection(true);
        poorButton2 = new Button(sfcConfComp, SWT.RADIO);
        poorButton2.setText("Poor");

        new Label(topComp2, SWT.NONE).setText("Min. Sea Level Pressure");
        Composite minSeaComp = new Composite(topComp2, SWT.NONE);
        GridLayout minSeaLayout = new GridLayout(2, false);
        minSeaLayout.marginWidth = 0;
        minSeaComp.setLayout(minSeaLayout);
        minSeaComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        minSeaText = new Text(minSeaComp, SWT.BORDER);
        GridData minSeaGridData = new GridData();
        minSeaGridData.widthHint = 50;
        minSeaText.setLayoutData(minSeaGridData);

        new Label(minSeaComp, SWT.NONE).setText("mb.");

        Label pressConfLbl = new Label(topComp2, SWT.NONE);
        pressConfLbl.setText("Pressure Confidence");
        Composite pressConfComp = new Composite(topComp2, SWT.NONE);
        GridLayout pressConfLayout = new GridLayout(4, false);
        pressConfLayout.marginWidth = 0;
        pressConfComp.setLayout(pressConfLayout);
        pressConfComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
        goodButton3 = new Button(pressConfComp, SWT.RADIO);
        goodButton3.setText("Good");
        fairButton3 = new Button(pressConfComp, SWT.RADIO);
        fairButton3.setText("Fair");
        fairButton3.setSelection(true);
        poorButton3 = new Button(pressConfComp, SWT.RADIO);
        poorButton3.setText("Poor");

        Label sondeEnvLabel = new Label(topComp2, SWT.NONE);
        sondeEnvLabel.setText("*  Sonde Environment ");

        sondeCombo = new Combo(topComp2, SWT.BORDER | SWT.READ_ONLY);
        sondeCombo.add("EYEWALL");
        sondeCombo.add("EYE");
        sondeCombo.add("RAINBAND");
        sondeCombo.add("MXWNDBND");
        sondeCombo.add("SYNOPTIC");

        new Label(topComp2, SWT.NONE).setText("Midpoint 150 m height");
        Composite midHeightComp = new Composite(topComp2, SWT.NONE);
        GridLayout midHeightLayout = new GridLayout(2, false);
        midHeightLayout.marginWidth = 0;
        midHeightComp.setLayout(midHeightLayout);
        midHeightComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        midHeightText = new Text(midHeightComp, SWT.BORDER);
        GridData midHeightGridData = new GridData();
        midHeightGridData.widthHint = 50;
        midHeightText.setLayoutData(midHeightGridData);

        new Label(midHeightComp, SWT.NONE).setText("meters");

    }

    /**
     * Create fields starting from the "Lowest 150 m..." field up to the "0 -
     * 500 m layer" field.
     *
     * @param parent
     */
    private void createLowest150Comp(Composite parent) {
        Group middleComp = new Group(parent, SWT.NONE);
        GridLayout middleLayout = new GridLayout(2, false);
        middleLayout.marginBottom = 5;
        middleLayout.marginWidth = 15;
        middleLayout.horizontalSpacing = 20;
        middleComp.setLayout(middleLayout);
        middleComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        new Label(middleComp, SWT.NONE).setText("Lowest 150 m of drop: ");
        Composite lowest150Comp = new Composite(middleComp, SWT.NONE);
        GridLayout lowest150Layout = new GridLayout(3, false);
        lowest150Comp.setLayout(lowest150Layout);
        lowest150Comp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        new Label(lowest150Comp, SWT.NONE).setText("Speed of mean wind ");
        lowest150Text = new Text(lowest150Comp, SWT.BORDER);
        GridData lowestGridData = new GridData();
        lowestGridData.widthHint = 50;
        lowest150Text.setLayoutData(lowestGridData);

        new Label(lowest150Comp, SWT.NONE).setText("kts");

        new Label(middleComp, SWT.NONE).setText("0 - 500 m layer: ");
        Composite layer500Comp = new Composite(middleComp, SWT.NONE);
        GridLayout layer500Layout = new GridLayout(3, false);
        layer500Comp.setLayout(layer500Layout);
        layer500Comp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        new Label(layer500Comp, SWT.NONE).setText("Speed of mean wind ");
        layer500Text = new Text(layer500Comp, SWT.BORDER);
        GridData layerGridData = new GridData();
        layerGridData.widthHint = 50;
        layer500Text.setLayoutData(layerGridData);

        new Label(layer500Comp, SWT.NONE).setText("kts");
    }

    /**
     * Create the remaining fields starting with the "Comments" field.
     *
     * @param parent
     */
    private void createCommentsComp(Composite parent) {
        Group bottomComp = new Group(parent, SWT.NONE);
        GridLayout bottomLayout = new GridLayout(2, false);
        bottomLayout.marginBottom = 5;
        bottomLayout.marginWidth = 15;
        bottomLayout.horizontalSpacing = 20;
        bottomComp.setLayout(bottomLayout);
        bottomComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

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

        currDate = TabUtility.checkDtg(dtgText.getText());
        if (TabUtility.isRecordValid(latText.getText(), lonText.getText(),
                northButton, westButton, !(centerInts.isEmpty()),
                !(findCurrentRec()), currDate, shell, true, true, true,
                !(sondeCombo.getText().isEmpty()), true, true, true)) {
            FDeckRecord fDeckRecord = new FDeckRecord();
            saveFieldsToRec(fDeckRecord);

            fDeckRecord.setRefTime(new Date(currDate.getTime()));
            fDeckRecord.setCycloneNum(storm.getCycloneNum());
            fDeckRecord.setBasin(storm.getRegion());
            fDeckRecord.setSubRegion(storm.getSubRegion());
            fDeckRecord.setFixFormat(FixFormat.DROPSONDE.getValue());
            fDeckRecord.setYear(storm.getYear());
            fDeckRecord.setFixType(typeName);

            ArrayList<FDeckRecord> recs = new ArrayList<>();
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
                fixSiteCombo.getText(), typeName);

        if (fixDataMap.containsKey(tempKey)) {
            selectedRecords = fixDataMap.get(tempKey);
            ret = true;
        }

        return ret;
    }

    /**
     * Reset all fields for a new entry
     */
    @Override
    protected void clearFields() {
        fixSiteCombo.setText("");
        centerFixChk.setSelection(false);
        maxWindFixChk.setSelection(false);
        latText.setText("");
        lonText.setText("");
        northButton.setSelection(true);
        southButton.setSelection(false);
        eastButton.setSelection(false);
        westButton.setSelection(true);

        goodButton1.setSelection(false);
        fairButton1.setSelection(true);
        poorButton1.setSelection(false);
        goodButton2.setSelection(false);
        fairButton2.setSelection(true);
        poorButton2.setSelection(false);
        goodButton3.setSelection(false);
        fairButton3.setSelection(true);
        poorButton3.setSelection(false);
        sfcWindText.setText("");
        minSeaText.setText("");
        sondeCombo.setText("");
        midHeightText.setText("");
        lowest150Text.setText("");
        layer500Text.setText("");

        commentText.setText("");
        initialsText.setText("");
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

        TabUtility.saveCenterIntensity(fDeckRecord.getCenterOrIntensity(),
                centerFixChk, maxWindFixChk, null, null);
        latText.setText(TabUtility.saveLat(fDeckRecord.getClat(), northButton,
                southButton));
        lonText.setText(TabUtility.saveLon(fDeckRecord.getClon(), eastButton,
                westButton));

        TabUtility.setConfidence(fDeckRecord.getPositionConfidence(),
                goodButton1, fairButton1, poorButton1);

        TabUtility.setConfidence(fDeckRecord.getWindMaxConfidence(),
                goodButton2, fairButton2, poorButton2);

        TabUtility.setConfidence(fDeckRecord.getPressureConfidence(),
                goodButton3, fairButton3, poorButton3);

        setNumberField(minSeaText, fDeckRecord.getMslp());
        sondeCombo.setText(fDeckRecord.getSondeEnvironment());
        setNumberField(sfcWindText, fDeckRecord.getWindMax());
        setNumberField(midHeightText,
                fDeckRecord.getHeightMidpointLowest150m());

        setNumberField(lowest150Text,
                fDeckRecord.getSpeedMeanWindLowest150mKt());
        setNumberField(layer500Text, fDeckRecord.getSpeedMeanWind0to500mKt());

        commentText.setText(fDeckRecord.getComments());
        initialsText.setText(fDeckRecord.getInitials());
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

        fDeckRecord.setSondeEnvironment(sondeCombo.getText());
        fDeckRecord.setFixSite(fixSiteCombo.getText());
        fDeckRecord.setComments(commentText.getText());
        fDeckRecord.setInitials(initialsText.getText());

        fDeckRecord.setPositionConfidence(TabUtility.saveConfidence(goodButton1,
                fairButton1, poorButton1));
        fDeckRecord.setWindMaxConfidence(TabUtility.saveConfidence(goodButton2,
                fairButton2, poorButton2));
        fDeckRecord.setPressureConfidence(TabUtility.saveConfidence(goodButton3,
                fairButton3, poorButton3));

        fDeckRecord.setHeightMidpointLowest150m(AtcfVizUtil.getStringAsFloat(
                midHeightText.getText(), false, true, TabUtility.DEFAULT));

        fDeckRecord.setMslp(AtcfVizUtil.getStringAsFloat(minSeaText.getText(),
                false, true, TabUtility.DEFAULT));
        fDeckRecord.setSpeedMeanWindLowest150mKt(AtcfVizUtil.getStringAsFloat(
                lowest150Text.getText(), false, true, TabUtility.DEFAULT));
        fDeckRecord.setWindMax(AtcfVizUtil.getStringAsFloat(
                sfcWindText.getText(), false, true, TabUtility.DEFAULT));
        fDeckRecord.setSpeedMeanWind0to500mKt(AtcfVizUtil.getStringAsFloat(
                layer500Text.getText(), false, true, TabUtility.DEFAULT));
    }

}

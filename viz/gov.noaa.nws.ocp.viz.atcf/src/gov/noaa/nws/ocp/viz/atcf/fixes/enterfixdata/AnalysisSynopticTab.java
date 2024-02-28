/**
* This software was developed and / or modified by NOAA/NWS/OCP/ASDT
*/

package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.widgets.List;
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
 * The Composite for the Analysis/Synoptic tab
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
 * Oct 28, 2022 109503     jwu        Fix exception reported in DR23333.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class AnalysisSynopticTab extends EditFixesTab {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AnalysisSynopticTab.class);

    private static final String PARSE_WARNING = "AnalysisSynopticTab"
            + TabUtility.DTG_ERROR_STRING;

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Button minSfcChk;

    private Text latText;

    private Button southButton;

    private Text lonText;

    private Button eastButton;

    private Button northButton;

    private Button westButton;

    private Button goodButton1;

    private Button fairButton1;

    private Button poorButton1;

    private Button goodButton2;

    private Button fairButton2;

    private Button poorButton2;

    private Button goodButton3;

    private Button fairButton3;

    private Button poorButton3;

    private Text initialsText;

    private Combo fixSiteCombo;

    private Text commentText;

    private Text analystInitText;

    private Text nearDataText;

    private Text startText;

    private Text endText;

    private Text seaPressureText;

    private Text windRadText;

    private Text eyeDiaText;

    private Text windEstText;

    private List observationList;

    private String centerInts;

    private Date currDate;

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
    public AnalysisSynopticTab(TabFolder parent, String tabName, Storm storm,
            boolean editData, String[] actionBtnNames, int sandBoxId,
            Map<FDeckRecordKey, java.util.List<FDeckRecord>> fixData,
            List dtgSelectionList) {
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
                .getFixType("ANAL");
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
    protected void createComponents() {
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
        createWindComp(mainComposite);
        createCommentComp(mainComposite);

        new Label(mainComposite, SWT.NONE)
                .setText(EditFixesDataDialog.ASTERISK_NOTE);

        // Find the first valid record and fill in current data if in edit mode
        if (editData) {
            // if the first values don't have an associated record, find some
            // that do
            if (!findCurrentRec() && !fixDataMap.isEmpty()) {
                // Get an arbitrary fdeckrecord from the map
                Map.Entry<FDeckRecordKey, java.util.List<FDeckRecord>> entry = fixDataMap
                        .entrySet().iterator().next();
                FDeckRecord rec = entry.getValue().get(0);

                // Use it to set the initial record to fill
                try {
                    dtgText.setText(TabUtility.formatDtg(rec.getRefTime()));
                } catch (RuntimeException e) {
                    logger.warn(PARSE_WARNING, e);
                }

                fixSiteCombo.setText(rec.getFixSite());
            }
            fillData();
        }

        centerInts = TabUtility.setCenterIntensity(centerFixChk.getSelection(),
                maxWindFixChk.getSelection(), false, minSfcChk.getSelection());
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

        java.util.List<String> dtgList = new ArrayList<>();

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
     * Assemble the first few rows of widgets from the "Center/Intensity"
     * options up to the Confidence options.
     */
    private void createCIComp(Composite parent) {
        Group topComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        topComp.setLayout(topLayout);
        topComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, true, false));

        Label centerIntensityLbl = new Label(topComp, SWT.NONE);
        centerIntensityLbl.setText("* Center/Intensity ");

        Composite centerWindComp = new Composite(topComp, SWT.NONE);
        GridLayout centerWindLayout = new GridLayout(3, false);
        centerWindComp.setLayout(centerWindLayout);
        centerWindComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        SelectionAdapter centerIntsAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                centerInts = TabUtility.setCenterIntensity(
                        centerFixChk.getSelection(),
                        maxWindFixChk.getSelection(), false,
                        minSfcChk.getSelection());
            }
        };

        centerFixChk = new Button(centerWindComp, SWT.CHECK);
        centerFixChk.setText("Center Fix");
        centerFixChk.addSelectionListener(centerIntsAdapter);

        maxWindFixChk = new Button(centerWindComp, SWT.CHECK);
        maxWindFixChk.setText("Max Wind Speed Fix");
        maxWindFixChk.addSelectionListener(centerIntsAdapter);

        minSfcChk = new Button(centerWindComp, SWT.CHECK);
        minSfcChk.setText("Min Sfc Pressure Fix");
        minSfcChk.addSelectionListener(centerIntsAdapter);

        Composite latitudeComp = new Composite(topComp, SWT.NONE);
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

        Composite longitudeComp = new Composite(topComp, SWT.NONE);
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

        Label confLbl = new Label(topComp, SWT.NONE);
        confLbl.setText("Conf.");
        Composite confComp = new Composite(topComp, SWT.NONE);
        GridLayout confLayout = new GridLayout(4, false);
        confComp.setLayout(confLayout);
        confComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        goodButton1 = new Button(confComp, SWT.RADIO);
        goodButton1.setText("Good");
        fairButton1 = new Button(confComp, SWT.RADIO);
        fairButton1.setText("Fair");
        fairButton1.setSelection(true);
        poorButton1 = new Button(confComp, SWT.RADIO);
        poorButton1.setText("Poor");
    }

    /**
     * Create a Composite containing various wind data fields.
     *
     * @param parent
     */
    private void createWindComp(Composite parent) {
        Group middleComp = new Group(parent, SWT.NONE);
        GridLayout middleLayout = new GridLayout(2, false);
        middleLayout.marginBottom = 5;
        middleLayout.marginWidth = 15;
        middleLayout.horizontalSpacing = 20;
        middleComp.setLayout(middleLayout);
        middleComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        Composite windComp = new Composite(middleComp, SWT.NONE);
        GridLayout windSpeedLayout = new GridLayout(3, false);
        windSpeedLayout.marginWidth = 0;
        windComp.setLayout(windSpeedLayout);
        windComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label windEstLabel = new Label(windComp, SWT.NONE);
        windEstLabel.setText("Wind Speed Estimate");

        windEstText = new Text(windComp, SWT.BORDER);
        GridData windEstGridData = new GridData();
        windEstGridData.widthHint = 50;
        windEstText.setLayoutData(windEstGridData);

        Label ktsLabel = new Label(windComp, SWT.NONE);
        ktsLabel.setText("kts");

        Label windRadLabel = new Label(windComp, SWT.NONE);
        windRadLabel.setText("Radius of max winds");

        windRadText = new Text(windComp, SWT.BORDER);
        GridData windRadGridData = new GridData();
        windRadGridData.widthHint = 50;
        windRadText.setLayoutData(windRadGridData);

        new Label(windComp, SWT.NONE).setText("nm");

        Label seaPressureLabel = new Label(windComp, SWT.NONE);
        seaPressureLabel.setText("Min. Sea Level Pressure");

        seaPressureText = new Text(windComp, SWT.BORDER);
        GridData seaGridData = new GridData();
        seaGridData.widthHint = 50;
        seaPressureText.setLayoutData(seaGridData);

        new Label(windComp, SWT.NONE).setText("mb");

        Label eyeDiaLabel = new Label(windComp, SWT.NONE);
        eyeDiaLabel.setText("Eye Diameter");

        eyeDiaText = new Text(windComp, SWT.BORDER);
        GridData eyeGridData = new GridData();
        eyeGridData.widthHint = 50;
        eyeDiaText.setLayoutData(eyeGridData);

        new Label(windComp, SWT.NONE).setText("nm");

        Composite windConfComp = new Composite(middleComp, SWT.NONE);
        GridLayout windConfLayout = new GridLayout(4, false);
        windConfLayout.marginWidth = 0;
        windConfComp.setLayout(windConfLayout);
        windConfComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite speedComp = new Composite(windConfComp, SWT.NONE);
        GridLayout speedLayout = new GridLayout(4, false);
        speedLayout.marginWidth = 0;
        speedComp.setLayout(speedLayout);
        speedComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label confLabel = new Label(speedComp, SWT.NONE);
        confLabel.setText("Wind Speed Conf. ");
        confLabel.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.CENTER, false, true));
        goodButton2 = new Button(speedComp, SWT.RADIO);
        goodButton2.setText("Good");
        fairButton2 = new Button(speedComp, SWT.RADIO);
        fairButton2.setText("Fair");
        fairButton2.setSelection(true);
        poorButton2 = new Button(speedComp, SWT.RADIO);
        poorButton2.setText("Poor");

        Label spaceLabel1 = new Label(windConfComp, SWT.NONE);
        GridData spacerData1 = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                true);
        spacerData1.horizontalSpan = 4;
        spaceLabel1.setLayoutData(spacerData1);

        Composite pressureComp = new Composite(windConfComp, SWT.NONE);
        GridLayout pressureLayout = new GridLayout(4, false);
        pressureLayout.marginWidth = 0;
        pressureComp.setLayout(pressureLayout);
        pressureComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        new Label(pressureComp, SWT.NONE).setText("Pressure Conf. ");
        goodButton3 = new Button(pressureComp, SWT.RADIO);
        goodButton3.setText("Good");
        fairButton3 = new Button(pressureComp, SWT.RADIO);
        fairButton3.setText("Fair");
        fairButton3.setSelection(true);
        poorButton3 = new Button(pressureComp, SWT.RADIO);
        poorButton3.setText("Poor");

        Label spaceLabel2 = new Label(windConfComp, SWT.NONE);
        GridData spacerData2 = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                true);
        spacerData2.horizontalSpan = 4;
        spaceLabel2.setLayoutData(spacerData2);

        Group middleComp2 = new Group(parent, SWT.NONE);
        GridLayout middleLayout2 = new GridLayout(2, false);
        middleLayout2.marginBottom = 5;
        middleLayout2.marginWidth = 15;
        middleLayout2.horizontalSpacing = 20;
        middleComp2.setLayout(middleLayout2);
        middleComp2.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        GridData startGridData = new GridData();
        startGridData.widthHint = 100;
        new Label(middleComp2, SWT.NONE).setText("Start Time (YYYYMMDDHHMN)");
        startText = new Text(middleComp2, SWT.BORDER);
        startText.setLayoutData(startGridData);

        GridData endGridData = new GridData();
        endGridData.widthHint = 100;
        new Label(middleComp2, SWT.NONE).setText("End Time (YYYYMMDDHHMN)");
        endText = new Text(middleComp2, SWT.BORDER);
        endText.setLayoutData(endGridData);

        Label nearDataLabel = new Label(middleComp2, SWT.NONE);
        nearDataLabel.setText("Distance to Nearest Data");

        Composite nearDataComp = new Composite(middleComp2, SWT.NONE);
        GridLayout nearDataLayout = new GridLayout(2, false);
        nearDataLayout.marginWidth = 0;
        nearDataComp.setLayout(nearDataLayout);
        nearDataComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        nearDataText = new Text(nearDataComp, SWT.BORDER);
        GridData nearDataGridData = new GridData();
        nearDataGridData.widthHint = 50;
        nearDataText.setLayoutData(nearDataGridData);

        new Label(nearDataComp, SWT.NONE).setText("nm");

        new Label(middleComp2, SWT.NONE).setText("Observation Sources");
        observationList = new List(middleComp2,
                SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData listData = new GridData(SWT.DEFAULT, SWT.CENTER, false, false);
        observationList.setLayoutData(listData);
        listData.heightHint = 75;
        listData.widthHint = 100;
        for (ObservationSources val : ObservationSources.values()) {
            observationList.add(val.getValue());
        }

        observationList.select(0);
        new Label(middleComp2, SWT.NONE).setText("Analyst Initials");
        analystInitText = new Text(middleComp2, SWT.BORDER);
        GridData analystInitGridData = new GridData();
        analystInitGridData.widthHint = 60;
        analystInitText.setLayoutData(analystInitGridData);
    }

    /**
     * Create the remaining fields including "Comments" and "Initials".
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
                !(findCurrentRec()), currDate, shell, true, true, true, true,
                true, true, true)) {
            FDeckRecord fDeckRecord = new FDeckRecord();

            fDeckRecord.setCycloneNum(storm.getCycloneNum());
            fDeckRecord.setBasin(storm.getRegion());
            fDeckRecord.setYear(storm.getYear());
            fDeckRecord.setSubRegion(storm.getSubRegion());
            fDeckRecord.setFixFormat(FixFormat.ANALYSIS.getValue());
            fDeckRecord.setFixType(typeName);
            fDeckRecord.setRefTime(new Date(currDate.getTime()));

            saveFieldsToRec(fDeckRecord);

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
        minSfcChk.setSelection(false);
        latText.setText("");
        northButton.setSelection(true);
        southButton.setSelection(false);
        eastButton.setSelection(false);
        westButton.setSelection(true);
        lonText.setText("");
        goodButton1.setSelection(false);
        fairButton1.setSelection(true);
        poorButton1.setSelection(false);
        goodButton2.setSelection(false);
        fairButton2.setSelection(true);
        poorButton2.setSelection(false);
        goodButton3.setSelection(false);
        fairButton3.setSelection(true);
        poorButton3.setSelection(false);
        windEstText.setText("");
        windRadText.setText("");
        seaPressureText.setText("");
        eyeDiaText.setText("");
        startText.setText("");
        endText.setText("");
        nearDataText.setText("");
        observationList.select(0);
        analystInitText.setText("");
        initialsText.setText("");
        commentText.setText("");
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
                centerFixChk, maxWindFixChk, null, minSfcChk);

        TabUtility.setConfidence(fDeckRecord.getPositionConfidence(),
                goodButton1, fairButton1, poorButton1);

        TabUtility.setConfidence(fDeckRecord.getWindMaxConfidence(),
                goodButton2, fairButton2, poorButton2);

        TabUtility.setConfidence(fDeckRecord.getPressureConfidence(),
                goodButton3, fairButton3, poorButton3);

        setNumberField(windEstText, fDeckRecord.getWindMax());

        setNumberField(windRadText, fDeckRecord.getRadiusOfMaximumWind());

        setNumberField(seaPressureText, fDeckRecord.getMslp());

        setNumberField(eyeDiaText, (fDeckRecord.getEyeDiameterNM()));

        setNumberField(nearDataText, fDeckRecord.getDistanceToNearestDataNM());

        // TODO -> Format is incorrect
        for (String s : observationList.getItems()) {
            if (fDeckRecord.getObservationSources().equals(s)) {
                observationList.select(observationList.indexOf(s));
            }
        }

        setTimeField(startText, fDeckRecord.getStartTime());
        setTimeField(endText, fDeckRecord.getEndTime());

        commentText.setText(fDeckRecord.getComments());
        initialsText.setText(fDeckRecord.getInitials());
        analystInitText.setText(fDeckRecord.getAnalysisInitials());
    }

    /**
     * Set time string in a Text field. If date is not valid, the text will be
     * blank.
     *
     * @param text
     *            Text widget to set time.
     * @param date
     *            Date
     */
    private void setTimeField(Text text, Date date) {
        text.setText("");
        if (date != null) {
            try {
                String tm = TabUtility.formatDtg(date);
                text.setText(tm);
            } catch (DateTimeException e) {
                logger.warn(PARSE_WARNING, e);
            }
        }
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

        fDeckRecord.setAnalysisInitials(analystInitText.getText());
        fDeckRecord.setInitials(initialsText.getText());
        fDeckRecord.setObservationSources(
                ObservationSources.getName(observationList.getSelection()[0]));

        fDeckRecord.setPositionConfidence(TabUtility.saveConfidence(goodButton1,
                fairButton1, poorButton1));
        fDeckRecord.setWindMaxConfidence(TabUtility.saveConfidence(goodButton2,
                fairButton2, poorButton2));
        fDeckRecord.setPressureConfidence(TabUtility.saveConfidence(goodButton3,
                fairButton3, poorButton3));
        fDeckRecord.setComments(commentText.getText());
        fDeckRecord.setFixSite(fixSiteCombo.getText());

        // Set start and end time if the inputs are valid.
        Date startTime = TabUtility.checkDtg(startText.getText());
        if (!TabUtility.DEFAULTDATE.equals(startTime)) {
            fDeckRecord.setStartTime(startTime);
        }

        Date endTime = TabUtility.checkDtg(endText.getText());
        if (!TabUtility.DEFAULTDATE.equals(endTime)) {
            fDeckRecord.setEndTime(endTime);
        }

        fDeckRecord.setRadiusOfMaximumWind(AtcfVizUtil
                .getStringAsFloat(windRadText.getText(), false, false, 0));
        fDeckRecord.setEyeDiameterNM(AtcfVizUtil
                .getStringAsFloat(eyeDiaText.getText(), false, false, 0));
        fDeckRecord.setDistanceToNearestDataNM(AtcfVizUtil
                .getStringAsFloat(nearDataText.getText(), false, false, 0));
        fDeckRecord.setWindMax(AtcfVizUtil
                .getStringAsFloat(windEstText.getText(), false, false, 0));
        fDeckRecord.setMslp(AtcfVizUtil
                .getStringAsFloat(seaPressureText.getText(), false, false, 0));

    }

    /**
     * Enum representing the Observation Sources in the Analysis tab
     *
     * TODO Replace this with data from analobsources.dat
     */
    enum ObservationSources {
        b("buoy"),
        l("land station"),
        m("SSMI"),
        c("scat"),
        t("trmm"),
        i("ir"),
        v("vis"),
        p("ship"),
        d("dropsonde"),
        a("aircraft"),
        r("radar"),
        x("other");

        private String value;

        ObservationSources(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * @param val
         * @return
         */
        public static String getName(String val) {
            for (ObservationSources name : ObservationSources.values()) {
                if (val.equals(name.name())) {
                    return name.name();
                }
            }
            return "";
        }
    }

}
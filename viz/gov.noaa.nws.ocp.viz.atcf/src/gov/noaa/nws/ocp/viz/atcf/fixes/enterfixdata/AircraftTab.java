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
import gov.noaa.nws.ocp.common.dataplugin.atcf.parameter.EyeShape;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 * The Composite for the Aircraft tab
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
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class AircraftTab extends EditFixesTab {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(AircraftTab.class);

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Combo fixSiteCombo;

    private Text latText;

    private Text lonText;

    private Button southButton;

    private Button westButton;

    private Button northButton;

    private Button eastButton;

    private Text commentText;

    private Text initialsText;

    private Text minSeaLvlText;

    private Text outEyeTempText;

    private Text insEyeTempText;

    private Text dewPointText;

    private Text seaSurfTempText;

    private Combo lEyeCombo;

    private Combo mEyeCombo;

    private Text orientText;

    private Text shortAxisText;

    private Text meteorText;

    private Text diameterText;

    private Text oAccuracyText;

    private Text missionText;

    private Text flightLvlText;

    private Button feet100Rdo;

    private Button millibarsRdo;

    private Text flightLvlMinText;

    private Text maxSfcWindText;

    private Text bearingText;

    private Text rangeText;

    private Text maxFltLevText;

    private Text gBearingText;

    private Text gRangeText;

    private Text intensityMinText;

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
     * @param tabName
     * @param dtgSelectionList
     */
    public AircraftTab(TabFolder parent, String tabName, Storm storm,
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
        typeEntry = AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("AIRC");
        if (typeEntry != null) {
            typeName = typeEntry.getName();
        } else {
            typeName = " ";
        }

        selectedRecords = new ArrayList<>();

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
        createFlightComp(mainComposite);
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
                    logger.warn("AircraftTab" + TabUtility.DTG_ERROR_STRING, e);
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
     * Assemble the first few rows of widgets from the "Center/Intensity"
     * options up to longitude and latitude.
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
        centerWindComp.setLayout(centerWindLayout);
        centerWindComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

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

        Composite dtgComp = new Composite(topComp, SWT.NONE);
        GridLayout dtgLayout = new GridLayout(2, false);
        dtgLayout.marginWidth = 0;
        dtgComp.setLayout(dtgLayout);
        dtgComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Composite siteComp = new Composite(topComp, SWT.NONE);
        GridLayout siteLayout = new GridLayout(2, false);
        siteComp.setLayout(siteLayout);
        siteComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

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
        longitudeComp.setLayout(longitudeLayout);
        longitudeComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

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
    }

    /**
     * Create two columns of text fields relating to a flight, from the "C.
     * Flight Level" field up to "Mission Number."
     *
     * @param parent
     */
    private void createFlightComp(Composite parent) {
        Group middleComp = new Group(parent, SWT.NONE);
        GridLayout middleLayout = new GridLayout(2, false);
        middleLayout.marginBottom = 5;
        middleLayout.marginWidth = 15;
        middleLayout.horizontalSpacing = 20;
        middleComp.setLayout(middleLayout);
        middleComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // The first column of text fields
        Composite leftComp = new Composite(middleComp, SWT.NONE);
        GridLayout leftLayout = new GridLayout(3, false);
        leftLayout.marginWidth = 0;
        leftComp.setLayout(leftLayout);
        leftComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        new Label(leftComp, SWT.NONE).setText("C. Flight Level");
        flightLvlText = new Text(leftComp, SWT.BORDER);
        flightLvlText.setLayoutData(new GridData(50, SWT.DEFAULT));
        Group flightLvlComp = new Group(leftComp, SWT.BORDER);
        GridLayout flightLvlLayout = new GridLayout(1, false);
        flightLvlComp.setLayout(flightLvlLayout);
        feet100Rdo = new Button(flightLvlComp, SWT.RADIO);
        feet100Rdo.setText("100s of feet");
        feet100Rdo.setSelection(true);
        millibarsRdo = new Button(flightLvlComp, SWT.RADIO);
        millibarsRdo.setText("millibars");

        new Label(leftComp, SWT.NONE).setText("C. Flight Level Min Height");
        flightLvlMinText = new Text(leftComp, SWT.BORDER);
        flightLvlMinText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("meters");

        new Label(leftComp, SWT.NONE).setText("Max Sfc Wind: D. Intensity");
        maxSfcWindText = new Text(leftComp, SWT.BORDER);
        maxSfcWindText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("kts");

        new Label(leftComp, SWT.NONE).setText("E. Bearing");
        bearingText = new Text(leftComp, SWT.BORDER);
        bearingText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("deg");

        new Label(leftComp, SWT.NONE).setText("E. Range");
        rangeText = new Text(leftComp, SWT.BORDER);
        rangeText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("nm");

        new Label(leftComp, SWT.NONE).setText("Max FltLev Wind:    F.Dir");
        maxFltLevText = new Text(leftComp, SWT.BORDER);
        maxFltLevText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("deg");

        new Label(leftComp, SWT.NONE).setText("F. Intensity");
        intensityMinText = new Text(leftComp, SWT.BORDER);
        intensityMinText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("kts");

        new Label(leftComp, SWT.NONE).setText("G. Bearing");
        gBearingText = new Text(leftComp, SWT.BORDER);
        gBearingText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("deg");

        new Label(leftComp, SWT.NONE).setText("G. Range");
        gRangeText = new Text(leftComp, SWT.BORDER);
        gRangeText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("nm");

        new Label(leftComp, SWT.NONE).setText("H. Min Sea Level Pressure");
        minSeaLvlText = new Text(leftComp, SWT.BORDER);
        minSeaLvlText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(leftComp, SWT.NONE).setText("millibars");

        // The second column of text fields
        Composite rightComp = new Composite(middleComp, SWT.NONE);
        GridLayout rightLayout = new GridLayout(3, false);
        rightComp.setLayout(rightLayout);
        rightComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        new Label(rightComp, SWT.NONE).setText("I. Outside Eye Temp");
        outEyeTempText = new Text(rightComp, SWT.BORDER);
        outEyeTempText.setLayoutData(new GridData(50, SWT.DEFAULT));

        new Label(rightComp, SWT.NONE).setText("deg C");

        new Label(rightComp, SWT.NONE).setText("J. Inside Eye Temp");
        insEyeTempText = new Text(rightComp, SWT.BORDER);
        insEyeTempText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("deg C");

        new Label(rightComp, SWT.NONE).setText("K. Dew Point");
        dewPointText = new Text(rightComp, SWT.BORDER);
        dewPointText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("deg C");

        new Label(rightComp, SWT.NONE).setText("K. Sea Surface Temp");
        seaSurfTempText = new Text(rightComp, SWT.BORDER);
        seaSurfTempText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("deg C");

        new Label(rightComp, SWT.NONE).setText("L. Eye Character");
        lEyeCombo = new Combo(rightComp, SWT.BORDER | SWT.READ_ONLY);
        GridData lEyeData = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        lEyeData.horizontalSpan = 2;
        lEyeCombo.setLayoutData(lEyeData);
        for (EyeCharacter val : EyeCharacter.values()) {
            lEyeCombo.add(val.getValue());
        }
        lEyeCombo.select(0);

        new Label(rightComp, SWT.NONE).setText("M. Eye:    Eye Shape");
        mEyeCombo = new Combo(rightComp, SWT.BORDER | SWT.READ_ONLY);
        GridData mEyeData = new GridData(SWT.LEFT, SWT.DEFAULT, false, false);
        mEyeData.horizontalSpan = 2;
        mEyeCombo.setLayoutData(mEyeData);
        mEyeCombo.add("None");
        for (EyeShape val : EyeShape.values()) {
            mEyeCombo.add(val.getValue());
        }
        mEyeCombo.select(0);

        new Label(rightComp, SWT.NONE).setText("Orientation");
        orientText = new Text(rightComp, SWT.BORDER);
        orientText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("deg");

        new Label(rightComp, SWT.NONE).setText("Diameter");
        diameterText = new Text(rightComp, SWT.BORDER);
        diameterText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE)
                .setText("nm    (Long axis if Elliptical)");

        new Label(rightComp, SWT.NONE).setText("Short Axis");
        shortAxisText = new Text(rightComp, SWT.BORDER);
        shortAxisText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE)
                .setText("nm    (Blank if not Elliptical)");

        new Label(rightComp, SWT.NONE).setText("O. Accuracy: Navigational");
        oAccuracyText = new Text(rightComp, SWT.BORDER);
        oAccuracyText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("nm");

        new Label(rightComp, SWT.NONE).setText("Meteorological");
        meteorText = new Text(rightComp, SWT.BORDER);
        meteorText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("nm");

        new Label(rightComp, SWT.NONE).setText("Mission Number");
        missionText = new Text(rightComp, SWT.BORDER);
        missionText.setLayoutData(new GridData(50, SWT.DEFAULT));
        new Label(rightComp, SWT.NONE).setText("");
    }

    /**
     * Create the bottom row of text widgets containing the "Comments" and
     * "Initials" entries.
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
                !(findCurrentRec()), currDate, shell, true, true,
                !(fixSiteCombo.getText().isEmpty()), true, true, true, true)) {
            FDeckRecord fDeckRecord = new FDeckRecord();

            saveFieldsToRec(fDeckRecord);

            fDeckRecord.setRefTime(new Date(currDate.getTime()));
            fDeckRecord.setYear(storm.getYear());
            fDeckRecord.setCycloneNum(storm.getCycloneNum());
            fDeckRecord.setBasin(storm.getRegion());
            fDeckRecord.setSubRegion(storm.getSubRegion());
            fDeckRecord.setFixFormat(FixFormat.AIRCRAFT.getValue());
            fDeckRecord.setFixType(typeName);

            ArrayList<FDeckRecord> recs = new ArrayList<>();
            recs.add(fDeckRecord);

            ret = validRecordUpdate(recs);
        }

        return ret;
    }

    /**
     * Finds and returns the FDeckRecords based off of the current DTG and
     * FixSite. Returns false if no record based off of that combination exists.
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
        westButton.setSelection(true);
        eastButton.setSelection(false);
        northButton.setSelection(true);
        southButton.setSelection(false);

        flightLvlText.setText("");
        flightLvlMinText.setText("");
        feet100Rdo.setSelection(true);
        millibarsRdo.setSelection(false);
        maxSfcWindText.setText("");
        bearingText.setText("");
        rangeText.setText("");
        maxFltLevText.setText("");
        intensityMinText.setText("");
        gBearingText.setText("");
        gRangeText.setText("");
        minSeaLvlText.setText("");
        outEyeTempText.setText("");
        insEyeTempText.setText("");
        dewPointText.setText("");
        seaSurfTempText.setText("");
        lEyeCombo.setText("");
        mEyeCombo.setText("none");
        orientText.setText("");
        diameterText.setText("");
        shortAxisText.setText("");
        oAccuracyText.setText("");
        meteorText.setText("");
        missionText.setText("");

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

        setFlightLvText(fDeckRecord);
        setNumberField(flightLvlMinText,
                fDeckRecord.getFlightLevelMinimumHeightMeters());
        setNumberField(maxSfcWindText,
                fDeckRecord.getMaxSurfaceWindInboundLegIntensity());
        setNumberField(bearingText,
                fDeckRecord.getMaxSurfaceWindInboundLegBearing());
        setNumberField(rangeText,
                fDeckRecord.getMaxSurfaceWindInboundLegRangeNM());
        setNumberField(maxFltLevText,
                fDeckRecord.getMaxFLWindInboundDirection());
        setNumberField(intensityMinText,
                fDeckRecord.getMaxFLWindInboundIntensity());
        setNumberField(gBearingText, fDeckRecord.getMaxFLWindInboundBearing());
        setNumberField(gRangeText, fDeckRecord.getMaxFLWindInboundRangeNM());
        setNumberField(minSeaLvlText, fDeckRecord.getMslp());
        setNumberField(outEyeTempText, fDeckRecord.getTempOutsideEye());
        setNumberField(insEyeTempText, fDeckRecord.getTempInsideEye());
        setNumberField(dewPointText, fDeckRecord.getDewPointTemp());
        setNumberField(seaSurfTempText, fDeckRecord.getSeaSurfaceTemp());

        lEyeCombo.setText(EyeCharacter.getValueFromString(
                fDeckRecord.getEyeCharacterOrWallCloudThickness()));
        mEyeCombo.setText(
                EyeShape.getValueFromString(fDeckRecord.getEyeShape()));

        setNumberField(orientText, fDeckRecord.getEyeOrientation());
        setNumberField(diameterText, fDeckRecord.getEyeDiameterNM());
        setNumberField(shortAxisText, fDeckRecord.getEyeShortAxis());
        setNumberField(oAccuracyText, fDeckRecord.getAccuracyNavigational());
        setNumberField(meteorText, fDeckRecord.getAccuracyMeteorological());
        missionText.setText(String.valueOf(fDeckRecord.getMissionNumber()));

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
        fDeckRecord.setClon(AtcfVizUtil.getStringAsFloat(lonText.getText(),
                westButton.getSelection(), false, 0));
        fDeckRecord.setClat(AtcfVizUtil.getStringAsFloat(latText.getText(),
                southButton.getSelection(), false, 0));

        fDeckRecord.setCenterOrIntensity(centerInts);

        fDeckRecord.setMissionNumber(missionText.getText());
        fDeckRecord.setEyeCharacterOrWallCloudThickness(
                EyeCharacter.getNameFromString(lEyeCombo.getText()));

        if (!"None".equals(mEyeCombo.getText())) {
            fDeckRecord.setEyeShape(
                    EyeShape.getNameFromString(mEyeCombo.getText()));
        }

        fDeckRecord.setFixSite(fixSiteCombo.getText());
        fDeckRecord.setComments(commentText.getText());
        fDeckRecord.setInitials(initialsText.getText());

        fDeckRecord.setMslp(AtcfVizUtil
                .getStringAsFloat(minSeaLvlText.getText(), false, false, 0));
        fDeckRecord.setTempOutsideEye(AtcfVizUtil
                .getStringAsFloat(outEyeTempText.getText(), false, false, 0));
        fDeckRecord.setTempInsideEye(AtcfVizUtil
                .getStringAsFloat(insEyeTempText.getText(), false, false, 0));
        fDeckRecord.setDewPointTemp(AtcfVizUtil
                .getStringAsFloat(dewPointText.getText(), false, false, 0));
        fDeckRecord.setSeaSurfaceTemp(AtcfVizUtil
                .getStringAsFloat(seaSurfTempText.getText(), false, false, 0));
        fDeckRecord.setEyeOrientation(AtcfVizUtil
                .getStringAsFloat(orientText.getText(), false, false, 0));
        fDeckRecord.setEyeDiameterNM(AtcfVizUtil
                .getStringAsFloat(diameterText.getText(), false, false, 0));
        fDeckRecord.setEyeShortAxis(AtcfVizUtil
                .getStringAsFloat(shortAxisText.getText(), false, false, 0));
        fDeckRecord.setAccuracyNavigational(AtcfVizUtil
                .getStringAsFloat(oAccuracyText.getText(), false, false, 0));
        fDeckRecord.setAccuracyMeteorological(AtcfVizUtil
                .getStringAsFloat(meteorText.getText(), false, false, 0));

        if (feet100Rdo.getSelection()) {
            fDeckRecord.setFlightLevel100Feet(AtcfVizUtil.getStringAsFloat(
                    flightLvlText.getText(), false, false, 0));
        } else if (millibarsRdo.getSelection()) {
            fDeckRecord.setFlightLevelMillibars(AtcfVizUtil.getStringAsFloat(
                    flightLvlText.getText(), false, false, 0));
        }

        fDeckRecord.setFlightLevelMinimumHeightMeters(AtcfVizUtil
                .getStringAsFloat(flightLvlMinText.getText(), false, false, 0));
        fDeckRecord.setMaxSurfaceWindInboundLegIntensity(AtcfVizUtil
                .getStringAsFloat(maxSfcWindText.getText(), false, false, 0));
        fDeckRecord.setMaxSurfaceWindInboundLegBearing(AtcfVizUtil
                .getStringAsFloat(bearingText.getText(), false, false, 0));
        fDeckRecord.setMaxSurfaceWindInboundLegRangeNM(AtcfVizUtil
                .getStringAsFloat(rangeText.getText(), false, false, 0));
        fDeckRecord.setMaxFLWindInboundDirection(AtcfVizUtil
                .getStringAsFloat(maxFltLevText.getText(), false, false, 0));
        fDeckRecord.setMaxFLWindInboundIntensity(AtcfVizUtil
                .getStringAsFloat(intensityMinText.getText(), false, false, 0));
        fDeckRecord.setMaxFLWindInboundBearing(AtcfVizUtil
                .getStringAsFloat(gBearingText.getText(), false, false, 0));
        fDeckRecord.setMaxFLWindInboundRangeNM(AtcfVizUtil
                .getStringAsFloat(gRangeText.getText(), false, false, 0));

    }

    /**
     * Sets the flight level text
     *
     * @return
     */
    private void setFlightLvText(FDeckRecord fDeckRecord) {
        if (fDeckRecord.getFlightLevelMillibars() != TabUtility.DEFAULT) {
            flightLvlText.setText(
                    String.valueOf(fDeckRecord.getFlightLevelMillibars()));
            millibarsRdo.setSelection(true);
            feet100Rdo.setSelection(false);
        } else if (fDeckRecord.getFlightLevel100Feet() != TabUtility.DEFAULT) {
            flightLvlText.setText(
                    String.valueOf(fDeckRecord.getFlightLevel100Feet()));
            millibarsRdo.setSelection(false);
            feet100Rdo.setSelection(true);
        } else {
            flightLvlText.setText("");
            millibarsRdo.setSelection(false);
            feet100Rdo.setSelection(true);
        }
    }

    /**
     * Enum representing the Eye Character in the Aircraft tab
     *
     * TODO Replace this with data from air_eyechar.dat
     */
    enum EyeCharacter {
        NA("NA - < 50% eyewall"),
        CL("CL - Closed Wall"),
        PD("PD - Poorly Defined"),
        N("N - Open North"),
        NE("NE - Open Northeast"),
        E("E - Open East"),
        SE("SE - Open Southeast"),
        S("S - Open South"),
        SW("SW - Open Southwest"),
        W("W - Open West"),
        NW("NW - Open Northwest"),
        SB("SB - Spiral Band");

        private String value;

        /**
         * @param iValue
         */
        private EyeCharacter(String iValue) {
            this.value = iValue;
        }

        /**
         * @param val
         * @return
         */
        public static String getNameFromString(String val) {
            String ret = "";
            for (EyeCharacter name : EyeCharacter.values()) {
                if (val.equals(name.getValue())) {
                    ret = name.name();
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
            for (EyeCharacter name : EyeCharacter.values()) {
                if (value.equals(name.name())) {
                    ret = name.getValue();
                }
            }

            return ret;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

}
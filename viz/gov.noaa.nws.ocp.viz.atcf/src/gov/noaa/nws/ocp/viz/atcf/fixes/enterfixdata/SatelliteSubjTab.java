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
 * The Composite for the Satellite - Subj. Dvorak tab
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2019 54779      wpaintsil  Initial creation.
 * Aug 15, 2019 65561      dmanzella  implemented backend functionality
 * Oct 20, 2019 68738      dmanzella  implemented edit functionality
 * Apr 01, 2021 87786      wpaintsil  Revise UI.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class SatelliteSubjTab extends EditFixesTab {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(SatelliteSubjTab.class);

    private Text latText;

    private Text lonText;

    private Combo satTypeCombo;

    private Combo centerTypeCombo;

    private Button visualButton;

    private Button infraButton;

    private Button microButton;

    private Text commentCombo;

    private Combo fixSiteCombo;

    private Text initialsText;

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Button eastButton;

    private Button westButton;

    private Button southButton;

    private Button northButton;

    private Text hoursText;

    private Text hoursText2;

    private Combo tNumCombo;

    private Combo amountCombo1;

    private Combo amountCombo2;

    private Combo intensityCombo;

    private Button tropicalButton;

    private Button subTropButton;

    private Button extraTropButton;

    private Combo pcnConfCombo;

    private Combo cNumCombo;

    private Button plusButton;

    private Button minusButton;

    private Button blankButton1;

    private Button develButton;

    private Button steadyButton;

    private Button weakButton;

    private Button blankButton2;

    private Button develButton2;

    private Button steadyButton2;

    private Button weakButton2;

    private Button blankButton3;

    private Date currDate;

    private String centerInts;

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
     * @param tabName
     * @param storm
     * @param editData
     * @param actionBtnNames
     * @param dtgSelectionList
     */
    public SatelliteSubjTab(TabFolder parent, String tabName, Storm storm,
            boolean editData, String[] actionBtnNames, int sandBoxId,
            Map<FDeckRecordKey, List<FDeckRecord>> fixData,
            org.eclipse.swt.widgets.List dtgSelectionList) {
        super(parent, SWT.NONE, dtgSelectionList);
        TabItem tab = new TabItem(parent, SWT.NONE);
        tab.setText(tabName);
        this.tabName = tabName;
        this.editData = editData;
        this.actionBtnNames = actionBtnNames;
        this.sandBoxID = sandBoxId;
        this.storm = storm;
        this.fixDataMap = fixData;
        shell = getShell();
        selectedRecords = new ArrayList<FDeckRecord>();
        currDate = new Date();
        typeEntry = AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("DVTS");
        if (typeEntry != null) {
            typeName = typeEntry.getName();
        } else {
            typeName = " ";
        }

        GridLayout tabLayout = new GridLayout(1, false);

        tabLayout.marginHeight = 10;
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
        GridData mainLayoutData = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                true);
        mainComposite.setLayoutData(mainLayoutData);

        createIDComp(mainComposite);
        createCIComp(mainComposite);
        createLongTermComp(mainComposite);
        createShortTermComp(mainComposite);
        createCommentComp(mainComposite);

        new Label(mainComposite, SWT.NONE)
                .setText(EditFixesDataDialog.ASTERISK_NOTE);

        // Fill in current data if in edit mode
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
                    logger.warn(
                            "SatelliteSubjTab" + TabUtility.DTG_ERROR_STRING,
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
     * Composite to pick "Center/Intensity", lat/lon, pcn/cnf. options up to
     * "Satellite Type."
     */
    private void createCIComp(Composite parent) {

        Group topComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 40;
        topComp.setLayout(topLayout);
        topComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Label centerIntensityLbl = new Label(topComp, SWT.NONE);
        centerIntensityLbl.setText("*  Center/Intensity");

        Composite centerWindComp = new Composite(topComp, SWT.NONE);
        GridLayout centerWindLayout = new GridLayout(2, false);
        centerWindLayout.marginWidth = 0;
        centerWindLayout.horizontalSpacing = 15;
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
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

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

        Composite pcnConfComp = new Composite(topComp, SWT.NONE);
        GridLayout pcnConfLayout = new GridLayout(2, false);
        pcnConfLayout.marginWidth = 0;
        pcnConfLayout.horizontalSpacing = 20;
        pcnConfComp.setLayout(pcnConfLayout);
        pcnConfComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));

        Button pcnButton = new Button(pcnConfComp, SWT.RADIO);
        pcnButton.setText("PCN");
        pcnButton.setSelection(true);

        Button confButton = new Button(pcnConfComp, SWT.RADIO);
        confButton.setText("CONF");

        Composite pcnConfComp2 = new Composite(topComp, SWT.NONE);
        GridLayout pcnConfLayout2 = new GridLayout(2, false);
        pcnConfLayout2.marginWidth = 0;
        pcnConfComp2.setLayout(pcnConfLayout2);
        Label pncConfLbl = new Label(pcnConfComp2, SWT.NONE);
        pncConfLbl.setText("PCN or CONF: ");
        pcnConfCombo = new Combo(pcnConfComp2, SWT.BORDER | SWT.READ_ONLY);
        for (PCNCode val : PCNCode.values()) {
            pcnConfCombo.add(val.getValue());
        }
        pcnConfCombo.select(0);

    }

    /**
     * Create the group widget with a border containing the fields for "Dvorak
     * Code - Long Term Trend."
     * 
     * @param parent
     */
    private void createLongTermComp(Composite parent) {
        Group longTermComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        longTermComp.setLayout(topLayout);
        longTermComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        longTermComp.setText("Dvorak Code - Long Term Trend");

        Label tNumLbl = new Label(longTermComp, SWT.NONE);
        tNumLbl.setText("Final T-Number");
        tNumCombo = new Combo(longTermComp, SWT.BORDER | SWT.READ_ONLY);
        tNumCombo.add(NONE);

        TabUtility.populateNumericCombo(tNumCombo, 0.0, 8.0, .5);
        tNumCombo.select(0);

        Label cNumLbl = new Label(longTermComp, SWT.NONE);
        cNumLbl.setText("CI Number");
        cNumCombo = new Combo(longTermComp, SWT.BORDER | SWT.READ_ONLY);
        cNumCombo.add(NONE);

        TabUtility.populateNumericCombo(cNumCombo, 0.0, 8.0, .5);
        cNumCombo.select(0);

        Label intensityLbl = new Label(longTermComp, SWT.NONE);
        intensityLbl.setText("Anticipated Intensity Change");

        Composite intensityComp = new Composite(longTermComp, SWT.NONE);
        GridLayout intensityLayout = new GridLayout(3, false);
        intensityComp.setLayout(intensityLayout);
        intensityComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        plusButton = new Button(intensityComp, SWT.RADIO);
        plusButton.setText("+");
        minusButton = new Button(intensityComp, SWT.RADIO);
        minusButton.setText("-");
        blankButton1 = new Button(intensityComp, SWT.RADIO);
        blankButton1.setText("Blank");
        blankButton1.setSelection(true);

        Label pastChLbl = new Label(longTermComp, SWT.NONE);
        pastChLbl.setText("Past Change");
        Composite pastChComp = new Composite(longTermComp, SWT.NONE);
        GridLayout pastChLayout = new GridLayout(4, false);
        pastChComp.setLayout(pastChLayout);
        pastChComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        develButton = new Button(pastChComp, SWT.RADIO);
        develButton.setText("Developed");
        steadyButton = new Button(pastChComp, SWT.RADIO);
        steadyButton.setText("Steady");
        weakButton = new Button(pastChComp, SWT.RADIO);
        weakButton.setText("Weakened");
        blankButton2 = new Button(pastChComp, SWT.RADIO);
        blankButton2.setText("Blank");
        blankButton2.setSelection(true);

        Composite amountComp = new Composite(longTermComp, SWT.NONE);
        GridLayout amountLayout = new GridLayout(2, false);
        amountComp.setLayout(amountLayout);
        amountComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label amountLabel = new Label(amountComp, SWT.NONE);
        amountLabel.setText("Amount of T-Num change");

        amountCombo1 = new Combo(amountComp, SWT.BORDER | SWT.READ_ONLY);
        amountCombo1.add(NONE);

        TabUtility.populateNumericCombo(amountCombo1, 0.0, 8.0, .5);
        amountCombo1.select(0);

        Composite hoursComp = new Composite(longTermComp, SWT.NONE);
        GridLayout latitudeLayout = new GridLayout(2, false);
        hoursComp.setLayout(latitudeLayout);
        hoursComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label hoursLabel = new Label(hoursComp, SWT.NONE);
        hoursLabel.setText("Hours since previous eval");

        hoursText = new Text(hoursComp, SWT.BORDER);
        GridData hoursGridData = new GridData();
        hoursGridData.widthHint = 40;
        hoursText.setLayoutData(hoursGridData);
    }

    /**
     * Create the group widget with a border containing the fields for "Dvorak
     * Code - Short Term Trend."
     * 
     * @param parent
     */
    private void createShortTermComp(Composite parent) {
        Group shortTermComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        shortTermComp.setLayout(topLayout);
        shortTermComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        shortTermComp.setText("Dvorak Code - Short Term Trend");

        Label pastChLbl = new Label(shortTermComp, SWT.NONE);
        pastChLbl.setText("Past Change");
        Composite pastChComp = new Composite(shortTermComp, SWT.NONE);
        GridLayout pastChLayout = new GridLayout(4, false);
        pastChComp.setLayout(pastChLayout);
        pastChComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        develButton2 = new Button(pastChComp, SWT.RADIO);
        develButton2.setText("Developed");
        steadyButton2 = new Button(pastChComp, SWT.RADIO);
        steadyButton2.setText("Steady");
        weakButton2 = new Button(pastChComp, SWT.RADIO);
        weakButton2.setText("Weakened");
        blankButton3 = new Button(pastChComp, SWT.RADIO);
        blankButton3.setText("Blank");
        blankButton3.setSelection(true);

        Composite amountComp = new Composite(shortTermComp, SWT.NONE);
        GridLayout amountLayout = new GridLayout(2, false);
        amountLayout.marginWidth = 0;
        amountComp.setLayout(amountLayout);
        amountComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label amountLabel = new Label(amountComp, SWT.NONE);
        amountLabel.setText("Amount of T-Num change");

        amountCombo2 = new Combo(amountComp, SWT.BORDER | SWT.READ_ONLY);
        amountCombo2.add(NONE);

        TabUtility.populateNumericCombo(amountCombo2, 0.0, 8.0, .5);
        amountCombo2.select(0);

        Composite hoursComp = new Composite(shortTermComp, SWT.NONE);
        GridLayout latitudeLayout = new GridLayout(2, false);
        hoursComp.setLayout(latitudeLayout);
        hoursComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label hoursLabel = new Label(hoursComp, SWT.NONE);
        hoursLabel.setText("Hours since previous eval");

        hoursText2 = new Text(hoursComp, SWT.BORDER);
        GridData hoursGridData = new GridData();
        hoursGridData.widthHint = 40;
        hoursText2.setLayoutData(hoursGridData);
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
        bottomLayout.marginWidth = 15;
        bottomLayout.horizontalSpacing = 30;
        bottomComp.setLayout(bottomLayout);
        bottomComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite intensityComp = new Composite(bottomComp, SWT.NONE);
        GridLayout intensityLayout = new GridLayout(2, false);
        intensityLayout.marginWidth = 0;
        intensityComp.setLayout(intensityLayout);
        intensityComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label intensityLabel = new Label(intensityComp, SWT.NONE);
        intensityLabel.setText("Forecast Intensity");

        intensityCombo = new Combo(intensityComp, SWT.BORDER | SWT.READ_ONLY);
        intensityCombo.add(NONE);

        TabUtility.populateNumericCombo(intensityCombo, 0.0, 8.0, .5);

        intensityCombo.select(0);

        Composite centerTypeComp = new Composite(bottomComp, SWT.NONE);
        GridLayout fixTypeLayout = new GridLayout(2, false);
        centerTypeComp.setLayout(fixTypeLayout);
        centerTypeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // Controlled by satfixtypes.dat
        Label fixTypeLabel = new Label(centerTypeComp, SWT.NONE);
        fixTypeLabel.setText("* Center Type");

        centerTypeCombo = new Combo(centerTypeComp, SWT.BORDER | SWT.READ_ONLY);
        for (CenterType val : CenterType.values()) {
            centerTypeCombo.add(val.getValue());
        }
        centerTypeCombo.select(0);

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
        tropicTypeLayout.horizontalSpacing = 30;
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

        Composite commentComp = new Composite(bottomComp, SWT.NONE);
        GridLayout commentLayout = new GridLayout(2, false);
        commentLayout.marginWidth = 0;
        commentComp.setLayout(commentLayout);

        GridData commentData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        commentData.horizontalSpan = 2;
        commentComp.setLayoutData(commentData);

        Label commentLabel = new Label(commentComp, SWT.NONE);
        commentLabel.setText("Comments");

        commentCombo = new Text(commentComp, SWT.BORDER);
        GridData commentsGridData = new GridData();
        commentsGridData.widthHint = 500;
        commentCombo.setLayoutData(commentsGridData);

        GridData initGridData = new GridData();
        initGridData.widthHint = 40;
        Label initialsLabel = new Label(commentComp, SWT.NONE);
        initialsLabel.setText("Initials");

        initialsText = new Text(commentComp, SWT.BORDER);
        initialsText.setLayoutData(initGridData);
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
        String sensorText = TabUtility.saveSensorType(visualButton, infraButton,
                microButton);
        if (TabUtility.isRecordValid(latText.getText(), lonText.getText(),
                northButton, westButton, !(centerInts.isEmpty()),
                !(findCurrentRec()), currDate, shell,
                !(satTypeCombo.getText().isEmpty()), !(sensorText.isEmpty()),
                !(fixSiteCombo.getText().isEmpty()), true, true, true, true)) {
            FDeckRecord fDeckRecord = new FDeckRecord();
            fDeckRecord.setRefTime(new Date(currDate.getTime()));
            fDeckRecord.setYear(storm.getYear());
            fDeckRecord.setCycloneNum(storm.getCycloneNum());
            fDeckRecord.setFixType(typeName);

            fDeckRecord.setBasin(storm.getRegion());

            fDeckRecord.setSubRegion(storm.getSubRegion());
            fDeckRecord.setFixFormat(FixFormat.SUBJECTIVE_DVORAK.getValue());

            saveFieldsToRec(fDeckRecord);

            List<FDeckRecord> recs = new ArrayList<>();
            recs.add(fDeckRecord);

            // Only call this if we know the record is valid
            ret = validRecordUpdate(recs);
        }

        return ret;
    }

    /**
     * Saves the Short Dvorak Code field in the database
     * 
     * Made up of 5 characters
     * 
     * First character is either 'D', 'S', 'W', or '/', depending on which radio
     * button is selected
     * 
     * Second and third characters are pulled from the double stored in
     * amountCombo2, without the '.'
     * 
     * Fourth and fifth characters are pulled from hoursText2
     * 
     * @return Short Dvorak Code
     */
    private String saveDvorakShort() {
        String dvorakShort = "";
        if (develButton2.getSelection()) {
            dvorakShort += "D";
        } else if (steadyButton2.getSelection()) {
            dvorakShort += "S";
        } else if (weakButton2.getSelection()) {
            dvorakShort += "W";
        } else if (blankButton3.getSelection()) {
            dvorakShort += "/";
        }

        if (NONE.equals(amountCombo2.getText())) {
            dvorakShort += "//";
        } else {
            dvorakShort += amountCombo2.getText().substring(0, 1);
            dvorakShort += amountCombo2.getText().substring(2, 3);
        }

        // TODO verify hours
        dvorakShort += hoursText2.getText();

        return dvorakShort;
    }

    /**
     * Sets the fields from the database to populate the Dvorak Code Short Term
     * Trend box
     * 
     * @param dvShort
     */
    private void setDvorakShort(String dvShort) {
        if (!dvShort.isEmpty()) {
            if (dvShort.length() > 3) {
                if ("/".equals(dvShort.substring(1, 2))) {
                    amountCombo2.select(0);
                } else {
                    String num = dvShort.substring(1, 2) + "."
                            + dvShort.substring(2, 3);
                    amountCombo2.setText(num);
                }
            }

            if (dvShort.length() > 4) {
                if ("//".equals(dvShort.substring(3))) {
                    hoursText2.setText("");
                } else {
                    hoursText2.setText(dvShort.substring(3));
                }
            }

            if (dvShort.length() > 2) {
                if ("D".equals(dvShort.substring(0, 1))) {
                    develButton2.setSelection(true);
                    steadyButton2.setSelection(false);
                    weakButton2.setSelection(false);
                    blankButton3.setSelection(false);
                } else if ("S".equals(dvShort.substring(0, 1))) {
                    develButton2.setSelection(false);
                    steadyButton2.setSelection(true);
                    weakButton2.setSelection(false);
                    blankButton3.setSelection(false);
                } else if ("W".equals(dvShort.substring(0, 1))) {
                    develButton2.setSelection(false);
                    steadyButton2.setSelection(false);
                    weakButton2.setSelection(true);
                    blankButton3.setSelection(false);
                } else {
                    develButton2.setSelection(false);
                    steadyButton2.setSelection(false);
                    weakButton2.setSelection(false);
                    blankButton3.setSelection(true);
                }
            }
        } else {
            amountCombo2.select(0);
            hoursText2.setText("");
            develButton2.setSelection(false);
            steadyButton2.setSelection(false);
            weakButton2.setSelection(false);
            blankButton3.setSelection(true);
        }
    }

    /**
     * Saves the Long Dvorak Code field in the database
     * 
     * Made from 10 characters
     * 
     * Characters one and two are pulled from the double stored in tNumCombo,
     * without the '.'
     * 
     * Characters three and four are pulled from the double stored in cNumCombo,
     * without the '.'
     * 
     * Character five is either '+', '-', or ' ', depending on the selected
     * radio button
     * 
     * Character six is either 'D', 'S', 'W', or '/', depending on the selected
     * radio button
     * 
     * Characters seven and eight are pulled from the double stored in
     * amountCombo1, without the '.'
     * 
     * The ninth and tenth characters are pulled from hoursText
     * 
     * @return Dvorak Code Long
     */
    private String saveDvorakLong() {
        String dvorakLong = "";
        if (NONE.equals(tNumCombo.getText())) {
            dvorakLong += "//";
        } else {
            dvorakLong += tNumCombo.getText().substring(0, 1);
            dvorakLong += tNumCombo.getText().substring(2, 3);
        }
        if (NONE.equals(cNumCombo.getText())) {
            dvorakLong += "//";
        } else {
            dvorakLong += cNumCombo.getText().substring(0, 1);
            dvorakLong += cNumCombo.getText().substring(2, 3);
        }

        if (plusButton.getSelection()) {
            dvorakLong += "+";
        } else if (minusButton.getSelection()) {
            dvorakLong += "-";
        } else if (blankButton1.getSelection()) {
            dvorakLong += " ";
        }

        if (develButton.getSelection()) {
            dvorakLong += "D";
        } else if (steadyButton.getSelection()) {
            dvorakLong += "S";
        } else if (weakButton.getSelection()) {
            dvorakLong += "W";
        } else if (blankButton2.getSelection()) {
            dvorakLong += "/";
        }

        if (NONE.equals(amountCombo1.getText())) {
            dvorakLong += "//";
        } else {
            dvorakLong += amountCombo1.getText().substring(0, 1);
            dvorakLong += amountCombo1.getText().substring(2, 3);
        }

        if (hoursText.getText().isEmpty()) {
            dvorakLong += "//";
        } else {
            dvorakLong += hoursText.getText();
        }

        return dvorakLong;
    }

    /**
     * Sets the fields from the database to populate the Dvorak Code Long Term
     * Trend box
     * 
     * @param dvlong
     */
    private void setDvorakLong(String dvLong) {
        if (!dvLong.isEmpty()) {

            if (dvLong.length() > 2) {
                if ("/".equals(dvLong.substring(0, 1))) {
                    tNumCombo.select(0);
                } else {
                    String num = dvLong.substring(0, 1) + "."
                            + dvLong.substring(1, 2);
                    tNumCombo.setText(num);
                }
            }

            if (dvLong.length() > 4) {
                if ("/".equals(dvLong.substring(2, 3))) {
                    cNumCombo.select(0);
                } else {
                    String num = dvLong.substring(2, 3) + "."
                            + dvLong.substring(3, 4);
                    cNumCombo.setText(num);
                }
            }

            if (dvLong.length() > 8) {
                if ("/".equals(dvLong.substring(6, 7))) {
                    amountCombo1.select(0);
                } else {
                    String num = dvLong.substring(6, 7) + "."
                            + dvLong.substring(7, 8);
                    amountCombo1.setText(num);
                }
            }

            if (dvLong.length() > 10) {
                if ("//".equals(dvLong.substring(8))) {
                    hoursText.setText("");
                } else {
                    hoursText.setText(dvLong.substring(8));
                }
            }

            if (dvLong.length() > 6) {
                if ("+".equals(dvLong.substring(4, 5))) {
                    plusButton.setSelection(true);
                    minusButton.setSelection(false);
                    blankButton1.setSelection(false);
                } else if ("-".equals(dvLong.substring(4, 5))) {
                    plusButton.setSelection(false);
                    minusButton.setSelection(true);
                    blankButton1.setSelection(false);
                } else {
                    plusButton.setSelection(false);
                    minusButton.setSelection(false);
                    blankButton1.setSelection(true);
                }
            }

            if (dvLong.length() > 7) {
                if ("D".equals(dvLong.substring(5, 6))) {
                    develButton.setSelection(true);
                    steadyButton.setSelection(false);
                    weakButton.setSelection(false);
                    blankButton2.setSelection(false);
                } else if ("S".equals(dvLong.substring(5, 6))) {
                    develButton.setSelection(false);
                    steadyButton.setSelection(true);
                    weakButton.setSelection(false);
                    blankButton2.setSelection(false);
                } else if ("W".equals(dvLong.substring(5, 6))) {
                    develButton.setSelection(false);
                    steadyButton.setSelection(false);
                    weakButton.setSelection(true);
                    blankButton2.setSelection(false);
                } else {
                    develButton.setSelection(false);
                    steadyButton.setSelection(false);
                    weakButton.setSelection(false);
                    blankButton2.setSelection(true);
                }
            }

        } else {
            tNumCombo.select(0);
            cNumCombo.select(0);
            amountCombo1.select(0);
            hoursText.setText("");

            plusButton.setSelection(false);
            minusButton.setSelection(false);
            blankButton1.setSelection(true);
            develButton.setSelection(false);
            steadyButton.setSelection(false);
            weakButton.setSelection(false);
            blankButton2.setSelection(true);
        }
    }

    /**
     * When in edit mode, and a new record is selected, fill in fields from the
     * record's data
     * 
     * @param fDeckRecord
     *            The record to get the data from
     */
    protected void setFields(ArrayList<FDeckRecord> fDeckRecords) {
        FDeckRecord fDeckRecord = fDeckRecords.get(0);

        TabUtility.saveCenterIntensity(fDeckRecord.getCenterOrIntensity(),
                centerFixChk, maxWindFixChk, null, null);
        latText.setText(TabUtility.saveLat(fDeckRecord.getClat(), northButton,
                southButton));
        lonText.setText(TabUtility.saveLon(fDeckRecord.getClon(), eastButton,
                westButton));
        pcnConfCombo
                .setText(PCNCode.getValFromNumber(fDeckRecord.getPcnCode()));
        setDvorakLong(fDeckRecord.getDvorakCodeLongTermTrend());
        setDvorakShort(fDeckRecord.getDvorakCodeShortTermTrend());

        String intensity = "";
        if (fDeckRecord.getCi24hourForecast() == TabUtility.DEFAULT) {
            intensity = NONE;
        } else {
            intensity = String.valueOf(fDeckRecord.getCi24hourForecast());
        }

        intensityCombo.setText(intensity);
        centerTypeCombo.setText(CenterType
                .getValueFromString(fDeckRecord.getCenterType()).getValue());
        TabUtility.setSensorType(fDeckRecord.getSensorType(), visualButton,
                infraButton, microButton);
        TabUtility.setTropics(fDeckRecord.getTropicalIndicator(),
                tropicalButton, subTropButton, extraTropButton);
        commentCombo.setText(fDeckRecord.getComments());
        initialsText.setText(fDeckRecord.getInitials());
    }

    /**
     * Reset all fields for a new entry
     */
    protected void clearFields() {
        fixSiteCombo.setText("");
        satTypeCombo.setText("");
        centerFixChk.setSelection(false);
        maxWindFixChk.setSelection(false);
        latText.setText("");
        lonText.setText("");
        northButton.setSelection(true);
        southButton.setSelection(false);
        eastButton.setSelection(false);
        westButton.setSelection(true);
        pcnConfCombo.select(0);
        tNumCombo.select(0);
        cNumCombo.select(0);
        plusButton.setSelection(false);
        minusButton.setSelection(false);
        blankButton1.setSelection(true);
        develButton.setSelection(false);
        steadyButton.setSelection(false);
        weakButton.setSelection(false);
        blankButton2.setSelection(true);
        amountCombo1.select(0);
        hoursText.setText("");
        develButton2.setSelection(false);
        steadyButton2.setSelection(false);
        weakButton2.setSelection(false);
        blankButton3.setSelection(true);
        amountCombo2.select(0);
        hoursText2.setText("");
        intensityCombo.select(0);
        centerTypeCombo.select(0);
        visualButton.setSelection(false);
        infraButton.setSelection(false);
        microButton.setSelection(false);
        tropicalButton.setSelection(true);
        subTropButton.setSelection(false);
        extraTropButton.setSelection(false);
        commentCombo.setText("");
        initialsText.setText("");
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
     * Saves the current values of the editable fields to the provided
     * FDeckRecord
     * 
     * @param fDeckRecord
     */
    protected void saveFieldsToRec(FDeckRecord fDeckRecord) {
        fDeckRecord.setCenterType(
                CenterType.getNameFromText(centerTypeCombo.getText()));
        fDeckRecord.setCenterOrIntensity(centerInts);
        fDeckRecord.setClon(AtcfVizUtil.getStringAsFloat(lonText.getText(),
                westButton.getSelection(), false, 0));
        fDeckRecord.setClat(AtcfVizUtil.getStringAsFloat(latText.getText(),
                southButton.getSelection(), false, 0));
        fDeckRecord.setPcnCode(pcnConfCombo.getText().substring(0, 1));

        fDeckRecord.setDvorakCodeLongTermTrend(saveDvorakLong());
        fDeckRecord.setDvorakCodeShortTermTrend(saveDvorakShort());

        fDeckRecord.setSensorType(TabUtility.saveSensorType(visualButton,
                infraButton, microButton));
        fDeckRecord.setFixSite(fixSiteCombo.getText());
        fDeckRecord.setSatelliteType(satTypeCombo.getText());
        fDeckRecord.setComments(commentCombo.getText());
        fDeckRecord.setInitials(initialsText.getText());

        fDeckRecord.setCi24hourForecast(AtcfVizUtil.getStringAsFloat(
                intensityCombo.getText(), false, true, TabUtility.DEFAULT));

        fDeckRecord.setTropicalIndicator(TabUtility.saveTropics(tropicalButton,
                subTropButton, extraTropButton));
    }

    /**
     * Enum representing the PCN code
     */
    enum PCNCode {
        POOR2("6 = Poorly Defined Circ. Center/Ephemeris"),
        POOR1("5 = Poorly Defined Circ. Center/Geography"),
        WELL2("4 = Well Defined Circ. Center/Ephemeris"),
        WELL1("3 = Well Defined Circ. Center/Geography"),
        EYE2("2 = Eye/Ephemeris"),
        EYE1("1 = Eye/Geography"),
        UNKNOWN("Unknown");

        private String value;

        private PCNCode(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        public static String getValFromNumber(String num) {
            String ret = PCNCode.UNKNOWN.getValue();
            for (PCNCode val : PCNCode.values()) {
                if (num.equals(val.getValue().substring(0, 1))) {
                    ret = val.getValue();
                }
            }

            return ret;
        }

        /**
         * @param val
         * @return
         */
        public static String getName(String val) {
            for (PCNCode name : PCNCode.values()) {
                if (val.equals(name.name())) {
                    return name.name();
                }
            }

            return "";
        }

    }

    /**
     * Enum representing center type in Sat Subj tab.
     * 
     * TODO Replace this with data from satfixtypes.dat
     */
    enum CenterType {
        CSC("CSC - cloud system center"),
        LLCC("LLCC - lower level cloud center"),
        ULCC("ULCC - upper level cloud center");

        private String value;

        private CenterType(String value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        public static CenterType getValueFromString(String name) {
            CenterType ret = CenterType.CSC;
            for (CenterType val : CenterType.values()) {
                if (name.equals(val.name())) {
                    ret = val;
                }
            }

            return ret;
        }

        /**
         * @param val
         * @return
         */
        public static String getNameFromText(String value) {
            String ret = "";
            for (CenterType name : CenterType.values()) {
                if (value.equals(name.getValue())) {
                    ret = name.name();
                }
            }

            return ret;
        }
    }

}

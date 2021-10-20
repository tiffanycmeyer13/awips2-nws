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
 * The Composite for the Radar tab
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
public class RadarTab extends EditFixesTab {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(RadarTab.class);

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Button windRadChk;

    private Button southButton;

    private Button northButton;

    private Button westButton;

    private Button eastButton;

    private Text lonText;

    private Text latText;

    private Text lonRainText;

    private Text latRainText;

    private Button southRainButton;

    private Button northRainButton;

    private Button westRainButton;

    private Button eastRainButton;

    private Text latitudeText;

    private Text longitudeText;

    private Button eastButtonRadar;

    private Button westButtonRadar;

    private Button northButtonRadar;

    private Button southButtonRadar;

    private Button goodButton;

    private Button fairButton;

    private Button poorButton;

    private Button goodWindButton;

    private Button fairWindButton;

    private Button poorWindButton;

    private Button goodRadiiButton;

    private Button fairRadiiButton;

    private Button poorRadiiButton;

    private Button landButton;

    private Button shipButton;

    private Button aircraftButton;

    private Button satelliteButton;

    private Button radobButton;

    private Button plainLngButton;

    private Button dopplerButton;

    private Text eyeDiaText;

    private Text spiralText;

    private Text radMaxWindText;

    private Combo wmoIdCombo;

    private Combo eyeWallCombo;

    private Combo eyeCombo;

    private Text maxWindText;

    private Date currDate;

    private String centerInts;

    private Text inchesText;

    private Text hoursText;

    private Text commentText;

    private Text initialsText;

    private ArrayList<FixTypeEntry> typeEntries;

    private ArrayList<Text> inOutTexts;

    private ArrayList<Button> circleQuadButtons;

    private ArrayList<Text> ktTexts;

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
    public RadarTab(TabFolder parent, String tabName, Storm storm,
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
        selectedRecords = new ArrayList<FDeckRecord>();
        typeEntries = new ArrayList<>();
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("RDRC"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("RDRD"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("RDRT"));

        TabItem tab = new TabItem(parent, SWT.NONE);
        tab.setText(tabName);
        this.tabName = tabName;

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
        mainLayout.verticalSpacing = 6;
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.CENTER, SWT.FILL, true,
                true);
        mainComposite.setLayoutData(mainLayoutData);

        createIDComp(mainComposite);
        createCIComp(mainComposite);
        createAccuracyComp(mainComposite);
        createMaxWindComp(mainComposite);
        createWindComp(mainComposite);

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
                    logger.warn("RadarTab" + TabUtility.DTG_ERROR_STRING, e);
                }
                wmoIdCombo.setText(rec.getFixSite());
            }
            fillData();
        }

        centerInts = TabUtility.setCenterIntensity(centerFixChk.getSelection(),
                maxWindFixChk.getSelection(), windRadChk.getSelection(), false);
    }

    /**
     * Composite to pick DTG, site, satellite type.
     */
    private void createIDComp(Composite parent) {

        Composite dtgSiteComp = new Composite(parent, SWT.NONE);
        GridLayout dtgSiteLayout = new GridLayout(3, false);
        dtgSiteLayout.marginWidth = 8;
        dtgSiteLayout.horizontalSpacing = 10;
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
        siteLbl.setText("*  WMO Identifier");
        if (editData) {
            wmoIdCombo = new Combo(siteComp, SWT.BORDER | SWT.READ_ONLY);
        } else {
            wmoIdCombo = new Combo(siteComp, SWT.BORDER);
        }
        FixSites fixSites = AtcfConfigurationManager.getInstance()
                .getFixSites();
        for (FixSiteEntry site : fixSites.getFixSites()) {
            wmoIdCombo.add(site.getName());
        }
        wmoIdCombo.select(0);
        if (editData) {
            wmoIdCombo.addModifyListener(e -> fillData());
        }

    }

    /**
     * Assemble the first few rows of widgets from the "Center/Intensity"
     * options up to latitude and longitude.
     */
    private void createCIComp(Composite parent) {
        Group topComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(2, false);
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        topComp.setLayout(topLayout);
        topComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

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
                        maxWindFixChk.getSelection(), windRadChk.getSelection(),
                        false);
            }
        };

        centerFixChk = new Button(centerWindComp, SWT.CHECK);
        centerFixChk.setText("Center Fix");
        centerFixChk.addSelectionListener(centerIntsAdapter);

        maxWindFixChk = new Button(centerWindComp, SWT.CHECK);
        maxWindFixChk.setText("Max Wind Speed Fix");
        maxWindFixChk.addSelectionListener(centerIntsAdapter);

        windRadChk = new Button(centerWindComp, SWT.CHECK);
        windRadChk.setText("Wind Radii Fix");
        windRadChk.addSelectionListener(centerIntsAdapter);

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

    }

    /**
     * Create the remaining fields starting with the "Accuracy" field.
     * 
     * @param parent
     */
    private void createAccuracyComp(Composite parent) {
        Group middleComp = new Group(parent, SWT.NONE);
        GridLayout middleLayout = new GridLayout(2, false);
        middleLayout.marginBottom = -5;
        middleLayout.verticalSpacing = -8;
        middleLayout.marginWidth = 5;
        middleComp.setLayout(middleLayout);
        middleComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite accuracyComp = new Composite(middleComp, SWT.NONE);
        GridLayout accuracyLayout = new GridLayout(4, false);
        accuracyLayout.horizontalSpacing = 35;
        accuracyComp.setLayout(accuracyLayout);
        GridData accCompGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        accCompGridData.horizontalSpan = 2;
        accuracyComp.setLayoutData(accCompGridData);

        Label accuracyLbl = new Label(accuracyComp, SWT.NONE);
        accuracyLbl.setText(" Accuracy:");

        Composite goodComp = new Composite(accuracyComp, SWT.NONE);
        goodComp.setLayout(new GridLayout(2, false));

        goodButton = new Button(goodComp, SWT.RADIO);
        goodButton.setText("Good");
        goodButton.setSelection(true);
        new Label(goodComp, SWT.NONE).setText("within 5 nm");

        Composite fairComp = new Composite(accuracyComp, SWT.NONE);
        fairComp.setLayout(new GridLayout(2, false));
        fairButton = new Button(fairComp, SWT.RADIO);
        fairButton.setText("Fair");
        new Label(fairComp, SWT.NONE).setText("5-16 nm");

        Composite poorComp = new Composite(accuracyComp, SWT.NONE);
        poorComp.setLayout(new GridLayout(2, false));
        poorButton = new Button(poorComp, SWT.RADIO);
        poorButton.setText("Poor");
        new Label(poorComp, SWT.NONE).setText("16-27 nm");

        Composite radarFixComp = new Composite(middleComp, SWT.NONE);
        GridLayout radarGridLayout = new GridLayout(5, false);
        radarGridLayout.horizontalSpacing = 40;
        radarFixComp.setLayout(radarGridLayout);
        GridData radarGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        radarGridData.horizontalSpan = 2;
        radarFixComp.setLayoutData(radarGridData);

        Label radarFxTypeLbl = new Label(radarFixComp, SWT.NONE);
        radarFxTypeLbl.setText(" Radar Fix Type:");

        landButton = new Button(radarFixComp, SWT.RADIO);
        landButton.setText("Land");
        landButton.setSelection(true);
        shipButton = new Button(radarFixComp, SWT.RADIO);
        shipButton.setText("Ship");
        aircraftButton = new Button(radarFixComp, SWT.RADIO);
        aircraftButton.setText("Aircraft");
        satelliteButton = new Button(radarFixComp, SWT.RADIO);
        satelliteButton.setText("Satellite");

        Composite enterFxComp = new Composite(middleComp, SWT.NONE);
        GridLayout enterGridLayout = new GridLayout(4, false);
        enterGridLayout.horizontalSpacing = 40;
        enterFxComp.setLayout(enterGridLayout);
        GridData enterGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        enterGridData.horizontalSpan = 2;
        enterFxComp.setLayoutData(enterGridData);

        Label enterFixLbl = new Label(enterFxComp, SWT.NONE);
        enterFixLbl.setText(" Enter Fix as:");

        radobButton = new Button(enterFxComp, SWT.RADIO);
        radobButton.setText("RADOB Code");
        radobButton.setSelection(true);
        plainLngButton = new Button(enterFxComp, SWT.RADIO);
        plainLngButton.setText("Plain Language");
        dopplerButton = new Button(enterFxComp, SWT.RADIO);
        dopplerButton.setText("Doppler");

        Composite radarSitePosComp = new Composite(middleComp, SWT.NONE);
        GridLayout radarSitePosLayout = new GridLayout(7, false);
        radarSitePosComp.setLayout(radarSitePosLayout);
        GridData radarSiteGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        radarSiteGridData.horizontalSpan = 2;
        radarSitePosComp.setLayoutData(radarSiteGridData);

        Label radarSitePosLbl = new Label(radarSitePosComp, SWT.NONE);
        radarSitePosLbl.setText(" Radar Site Position:");

        new Label(radarSitePosComp, SWT.NONE).setText("Latitude");
        latitudeText = new Text(radarSitePosComp, SWT.BORDER);

        Composite northSouthComposite = new Composite(radarSitePosComp,
                SWT.NONE);
        GridLayout northSouthGridLayout = new GridLayout(2, false);
        northSouthComposite.setLayout(northSouthGridLayout);

        northButtonRadar = new Button(northSouthComposite, SWT.RADIO);
        northButtonRadar.setText("N");
        northButtonRadar.setSelection(true);
        southButtonRadar = new Button(northSouthComposite, SWT.RADIO);
        southButtonRadar.setText("S");

        new Label(radarSitePosComp, SWT.NONE).setText("Longitude");
        longitudeText = new Text(radarSitePosComp, SWT.BORDER);

        GridData latGridData = new GridData();
        latGridData.widthHint = 60;
        latitudeText.setLayoutData(latGridData);
        GridData lonGridData = new GridData();
        lonGridData.widthHint = 60;
        longitudeText.setLayoutData(lonGridData);

        Composite eastWestComposite = new Composite(radarSitePosComp, SWT.NONE);
        GridLayout eastWestGridLayout = new GridLayout(2, false);
        eastWestComposite.setLayout(eastWestGridLayout);

        eastButtonRadar = new Button(eastWestComposite, SWT.RADIO);
        eastButtonRadar.setText("E");
        westButtonRadar = new Button(eastWestComposite, SWT.RADIO);
        westButtonRadar.setText("W");
        westButtonRadar.setSelection(true);

        Composite eyeComp = new Composite(middleComp, SWT.NONE);
        GridLayout eyeLayout = new GridLayout(3, false);
        eyeComp.setLayout(eyeLayout);
        GridData eyeCompGridData = new GridData(SWT.NONE, SWT.DEFAULT, false,
                false);
        eyeCompGridData.horizontalSpan = 2;
        eyeComp.setLayoutData(eyeCompGridData);

        Composite innerComp = new Composite(eyeComp, SWT.NONE);
        GridLayout innerGridLayout = new GridLayout(3, false);
        innerComp.setLayout(innerGridLayout);
        innerComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label eyeLabel = new Label(innerComp, SWT.NONE);
        eyeLabel.setText("Radius of max winds");

        radMaxWindText = new Text(innerComp, SWT.BORDER);
        GridData mWindGridData = new GridData();
        mWindGridData.widthHint = 50;
        radMaxWindText.setLayoutData(mWindGridData);

        Label endLabel = new Label(innerComp, SWT.NONE);
        endLabel.setText("nm");

        Composite innerComp2 = new Composite(eyeComp, SWT.NONE);
        GridLayout innerGridLayout2 = new GridLayout(3, false);
        innerComp2.setLayout(innerGridLayout2);

        Label eyeLabel2 = new Label(innerComp2, SWT.NONE);
        eyeLabel2.setText("Eye Diameter");

        eyeDiaText = new Text(innerComp2, SWT.BORDER);
        GridData mWindGridData2 = new GridData();
        mWindGridData2.widthHint = 50;
        eyeDiaText.setLayoutData(mWindGridData2);

        Label endLabel2 = new Label(innerComp2, SWT.NONE);
        endLabel2.setText("nm");

        Composite innerComp3 = new Composite(eyeComp, SWT.NONE);
        GridLayout innerGridLayout3 = new GridLayout(3, false);
        innerComp3.setLayout(innerGridLayout3);

        Label eyeLabel3 = new Label(innerComp3, SWT.NONE);
        eyeLabel3.setText("Spiral Overlay");

        spiralText = new Text(innerComp3, SWT.BORDER);
        GridData mWindGridData3 = new GridData();
        mWindGridData3.widthHint = 50;
        spiralText.setLayoutData(mWindGridData3);

        Label endLabel3 = new Label(innerComp3, SWT.NONE);
        endLabel3.setText("deg");

        Composite innerEyeComp1 = new Composite(eyeComp, SWT.NONE);
        GridLayout innerEyeGridLayout1 = new GridLayout(2, false);
        innerEyeComp1.setLayout(innerEyeGridLayout1);

        new Label(innerEyeComp1, SWT.NONE).setText("Eye Shape");
        eyeCombo = new Combo(innerEyeComp1, SWT.BORDER | SWT.READ_ONLY);
        eyeCombo.add("None");
        for (EyeShape val : EyeShape.values()) {
            eyeCombo.add(val.getValue());
        }
        eyeCombo.select(0);

        Composite innerEyeComp2 = new Composite(eyeComp, SWT.NONE);
        GridLayout innerEyeGridLayout2 = new GridLayout(2, false);
        innerEyeComp2.setLayout(innerEyeGridLayout2);

        new Label(innerEyeComp2, SWT.NONE).setText("% of Eye Wall Observed");
        eyeWallCombo = new Combo(innerEyeComp2, SWT.BORDER | SWT.READ_ONLY);
        TabUtility.populateIntNumericCombo(eyeWallCombo, 0, 100, 1);
        eyeWallCombo.select(0);

        Composite innerEyeComp3 = new Composite(eyeComp, SWT.NONE);
        GridLayout innerEyeGridLayout3 = new GridLayout(3, false);
        innerEyeComp3.setLayout(innerEyeGridLayout3);

        Label maxWindLbl = new Label(innerEyeComp3, SWT.NONE);
        maxWindLbl.setText("Max. Wind Speed");

        maxWindText = new Text(innerEyeComp3, SWT.BORDER);
        GridData maxWindGridData = new GridData();
        maxWindGridData.widthHint = 50;
        maxWindText.setLayoutData(maxWindGridData);

        Label ktsLabel = new Label(innerEyeComp3, SWT.NONE);
        ktsLabel.setText("kts");

        Composite innerEyeComp4 = new Composite(middleComp, SWT.NONE);
        GridLayout innerEyeGridLayout4 = new GridLayout(4, false);
        innerEyeComp4.setLayout(innerEyeGridLayout4);

        Label confLabel2 = new Label(innerEyeComp4, SWT.NONE);
        confLabel2.setText(" Wind Speed Confidence ");
        goodWindButton = new Button(innerEyeComp4, SWT.RADIO);
        goodWindButton.setText("Good");
        fairWindButton = new Button(innerEyeComp4, SWT.RADIO);
        fairWindButton.setText("Fair");
        fairWindButton.setSelection(true);
        poorWindButton = new Button(innerEyeComp4, SWT.RADIO);
        poorWindButton.setText("Poor");

        Composite innerEyeComp5 = new Composite(middleComp, SWT.NONE);
        GridLayout innerEyeGridLayout5 = new GridLayout(4, false);
        innerEyeComp5.setLayout(innerEyeGridLayout5);

        Label confLabel3 = new Label(innerEyeComp5, SWT.NONE);
        confLabel3.setText("\tRadii Confidence ");
        goodRadiiButton = new Button(innerEyeComp5, SWT.RADIO);
        goodRadiiButton.setText("Good");
        fairRadiiButton = new Button(innerEyeComp5, SWT.RADIO);
        fairRadiiButton.setText("Fair");
        fairRadiiButton.setSelection(true);
        poorRadiiButton = new Button(innerEyeComp5, SWT.RADIO);
        poorRadiiButton.setText("Poor");

    }

    /**
     * Create the remaining fields starting with the "Accuracy" field.
     * 
     * @param parent
     */
    private void createMaxWindComp(Composite parent) {
        Group stormCentComp = new Group(parent, SWT.NONE);
        stormCentComp.setText("Storm Centered");
        GridLayout stormCentLayout = new GridLayout(5, false);
        stormCentLayout.marginWidth = 15;
        stormCentLayout.horizontalSpacing = 20;
        stormCentComp.setLayout(stormCentLayout);
        stormCentComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        inOutTexts = new ArrayList<>();

        for (int ii = 0; ii < 2; ii++) {
            if (ii == 0) {
                Label inboundLabel = new Label(stormCentComp, SWT.NONE);
                inboundLabel.setText("Inbound");
            } else {
                Label outboundLabel = new Label(stormCentComp, SWT.NONE);
                outboundLabel.setText("Outbound");
            }

            for (int jj = 0; jj < 4; jj++) {
                Composite mWindComp = new Composite(stormCentComp, SWT.NONE);
                GridLayout mWindGridLayout = new GridLayout(3, false);
                mWindGridLayout.horizontalSpacing = 2;
                mWindComp.setLayout(mWindGridLayout);
                Label maxWindLabel = new Label(mWindComp, SWT.NONE);

                if (jj == 0) {
                    maxWindLabel.setText("Max wind");
                } else if (jj == 1) {
                    maxWindLabel.setText("Azm");
                } else if (jj == 2) {
                    maxWindLabel.setText("Range");
                } else {
                    maxWindLabel.setText("Elev");
                }

                Text mWindText = new Text(mWindComp, SWT.BORDER);
                GridData mWindGridData = new GridData();
                mWindGridData.widthHint = 50;
                mWindText.setLayoutData(mWindGridData);
                inOutTexts.add(mWindText);

                Label ktsLabel1 = new Label(mWindComp, SWT.NONE);
                if (jj == 0) {
                    ktsLabel1.setText("kts");
                } else if (jj == 1) {
                    ktsLabel1.setText("deg.s");
                } else if (jj == 2) {
                    ktsLabel1.setText("nm");
                } else {
                    ktsLabel1.setText("ft");
                }
            }
        }

    }

    /**
     * Create the rows of wind data fields.
     * 
     * @param parent
     */
    private void createWindComp(Composite parent) {
        ktTexts = new ArrayList<>();
        circleQuadButtons = new ArrayList<>();

        Composite doubleComp = new Composite(parent, SWT.NONE);
        GridLayout doubleCompGridLayout = new GridLayout(2, false);
        doubleComp.setLayout(doubleCompGridLayout);

        Group windComp = new Group(doubleComp, SWT.NONE);
        GridLayout windLayout = new GridLayout(4, false);
        windLayout.marginBottom = 5;
        windComp.setLayout(windLayout);
        GridData windCompGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                true);
        windComp.setLayoutData(windCompGridData);

        String[] ktStrings = new String[] { "34", "50", "64", " " };
        // Create the 3 columns of wind data.
        for (int jj = 0; jj < 4; jj++) {
            Composite ktComp = new Composite(windComp, SWT.NONE);
            // For the first column, add a column of appropriate labels and
            // blank spaces appropriate for each row.
            GridLayout ktLayout = new GridLayout(2, false);
            ktComp.setLayout(ktLayout);
            ktComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            Label kt = new Label(ktComp, SWT.NONE);
            kt.setText(ktStrings[jj] + (jj == 3 ? "" : " kt winds"));
            GridData ktlabGridData = new GridData();
            ktlabGridData.horizontalSpan = 2;
            kt.setLayoutData(ktlabGridData);

            if (jj != 3) {
                Composite buttonComp = new Composite(ktComp, SWT.NONE);
                buttonComp.setLayout(new GridLayout(2, false));
                GridData buttonGridData = new GridData();
                buttonGridData.horizontalSpan = 2;
                buttonComp.setLayoutData(buttonGridData);

                Button circleChk = new Button(buttonComp, SWT.RADIO);
                circleChk.setText("Circle");
                circleChk.setSelection(true);
                Button quadsChk = new Button(buttonComp, SWT.RADIO);
                quadsChk.setText("Quads");
                circleQuadButtons.add(circleChk);
                circleQuadButtons.add(quadsChk);
            } else {
                new Label(ktComp, SWT.NONE).setText(" ");
                new Label(ktComp, SWT.NONE).setText(" ");
            }

            // TODO if Circle is selected, only NE Row is visible
            String[] directionStrings = new String[] { "NE", "SE", "SW", "NW" };
            for (int ii = 0; ii < 4; ii++) {
                if (jj == 0) {
                    Label dirLabel = new Label(ktComp, SWT.NONE);
                    dirLabel.setText(directionStrings[ii]);
                    GridData dirLabGridData = new GridData();
                    dirLabGridData.verticalIndent = 8;
                    dirLabel.setLayoutData(dirLabGridData);
                }
                GridData windGridData = new GridData();
                windGridData.widthHint = 40;

                if (jj != 0) {
                    windGridData.horizontalSpan = 2;
                }

                if (jj != 3) {
                    Text windText = new Text(ktComp, SWT.BORDER);
                    windText.setLayoutData(windGridData);
                    ktTexts.add(windText);
                } else {
                    new Label(ktComp, SWT.NONE).setText(" ");
                }

            }

        }

        Group rainAccGroup = new Group(doubleComp, SWT.NONE);
        GridLayout rainCompGridLayout = new GridLayout(1, false);
        rainAccGroup.setLayout(rainCompGridLayout);
        rainAccGroup.setText("Rain Accumulation");
        GridData rainAccGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        rainAccGridData.verticalSpan = 2;
        rainAccGroup.setLayoutData(rainAccGridData);

        Composite accComp = new Composite(rainAccGroup, SWT.NONE);
        accComp.setLayout(new GridLayout(3, false));
        GridData accGridData = new GridData();
        accComp.setLayoutData(accGridData);
        inchesText = new Text(accComp, SWT.BORDER);

        GridData inchesGridData = new GridData();
        inchesGridData.horizontalSpan = 2;
        Label inchesLabel = new Label(accComp, SWT.NONE);
        inchesLabel.setText("inches");
        inchesLabel.setLayoutData(inchesGridData);

        Label inLabel = new Label(accComp, SWT.NONE);
        inLabel.setText("in   :");

        hoursText = new Text(accComp, SWT.BORDER);

        Label hoursLabel = new Label(accComp, SWT.NONE);
        hoursLabel.setText("hours");

        Composite latlonComp = new Composite(rainAccGroup, SWT.NONE);
        GridLayout latitudeLayout = new GridLayout(3, false);
        latitudeLayout.marginWidth = 0;
        latlonComp.setLayout(latitudeLayout);
        latlonComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label latLabel = new Label(latlonComp, SWT.NONE);
        latLabel.setText("Latitude");

        latRainText = new Text(latlonComp, SWT.BORDER);
        latRainText.addListener(SWT.Verify,
                verifyListeners.getLatVerifyListener());

        Composite northSouthComp = new Composite(latlonComp, SWT.NONE);
        northSouthComp.setLayout(new GridLayout(2, false));

        northRainButton = new Button(northSouthComp, SWT.RADIO);
        northRainButton.setText("N");
        northRainButton.setSelection(true);
        southRainButton = new Button(northSouthComp, SWT.RADIO);
        southRainButton.setText("S");

        Label lonLabel = new Label(latlonComp, SWT.NONE);
        lonLabel.setText("Longitude");

        lonRainText = new Text(latlonComp, SWT.BORDER);
        lonRainText.addListener(SWT.Verify,
                verifyListeners.getLonVerifyListener());

        GridData latGridData = new GridData();
        latGridData.widthHint = 60;
        latText.setLayoutData(latGridData);
        GridData lonGridData = new GridData();
        lonGridData.widthHint = 60;
        lonText.setLayoutData(lonGridData);

        Composite eastWestComp = new Composite(latlonComp, SWT.NONE);
        eastWestComp.setLayout(new GridLayout(2, false));

        eastRainButton = new Button(eastWestComp, SWT.RADIO);
        eastRainButton.setText("E");
        westRainButton = new Button(eastWestComp, SWT.RADIO);
        westRainButton.setText("W");
        westRainButton.setSelection(true);

        Group commentsGroup = new Group(doubleComp, SWT.NONE);
        GridLayout commentsGridLayout = new GridLayout(2, false);
        commentsGroup.setLayout(commentsGridLayout);
        GridData commentsGridData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                true);
        rainAccGroup.setLayoutData(commentsGridData);

        Label commentLabel = new Label(commentsGroup, SWT.NONE);
        commentLabel.setText("Comments");

        GridData commentGridData = new GridData();
        commentGridData.widthHint = 400;
        commentText = new Text(commentsGroup, SWT.BORDER);
        commentText.setLayoutData(commentGridData);

        Label initialsLabel = new Label(commentsGroup, SWT.NONE);
        initialsLabel.setText("Initials");
        initialsText = new Text(commentsGroup, SWT.BORDER);
        GridData initialsGridData = new GridData();
        initialsGridData.widthHint = 60;
        initialsText.setLayoutData(initialsGridData);

    }

    /**
     * Saves the radar type
     * 
     * @return radar type
     */
    private String saveRadarType() {
        String radarType = "";
        if (landButton.getSelection()) {
            radarType = "L";
        }

        if (shipButton.getSelection()) {
            radarType = "S";
        }

        if (aircraftButton.getSelection()) {
            radarType = "A";
        }

        if (satelliteButton.getSelection()) {
            radarType = "T";
        }

        return radarType;
    }

    /**
     * Compiles the modified records, prepares them for saving
     */
    @Override
    protected boolean saveNewRecords() {
        boolean ret = false;

        float maxWind = AtcfVizUtil.getStringAsFloat(maxWindText.getText(),
                false, false, 0);

        currDate = TabUtility.checkDtg(dtgText.getText());
        if (TabUtility.isRecordValid(latText.getText(), lonText.getText(),
                northButton, westButton, !(centerInts.isEmpty()),
                !(findCurrentRec()), currDate, shell, true, true, true, true,
                true, true, !(wmoIdCombo.getText().isEmpty()))) {
            List<FDeckRecord> recs = new ArrayList<>();
            recs.add(new FDeckRecord());
            if (maxWind >= 50) {
                recs.add(new FDeckRecord());
            }

            if (maxWind >= 64) {
                recs.add(new FDeckRecord());
            }

            for (FDeckRecord fDeckRecord : createRecordsByWindSpeeds(recs)) {

                fDeckRecord.setRefTime(new Date(currDate.getTime()));
                fDeckRecord.setCycloneNum(storm.getCycloneNum());
                fDeckRecord.setBasin(storm.getRegion());
                fDeckRecord.setSubRegion(storm.getSubRegion());
                fDeckRecord.setFixFormat(FixFormat.RADAR.getValue());
                fDeckRecord.setYear(storm.getYear());
                fDeckRecord.setFixType("RDRD");

                saveFieldsToRec(fDeckRecord);

            }
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

        for (FixTypeEntry type : typeEntries) {
            if (type != null) {
                FDeckRecordKey tempKey = new FDeckRecordKey(dtgText.getText(),
                        wmoIdCombo.getText(), type.getName());

                if (fixDataMap.containsKey(tempKey)) {
                    selectedRecords = fixDataMap.get(tempKey);
                    ret = true;
                }
            }
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
    protected void setFields(ArrayList<FDeckRecord> fDeckRecords) {

        FDeckRecord fDeckRecord = fDeckRecords.get(0);

        TabUtility.saveCenterIntensity(fDeckRecord.getCenterOrIntensity(),
                centerFixChk, maxWindFixChk, windRadChk, null);

        latText.setText(TabUtility.saveLat(fDeckRecord.getClat(), northButton,
                southButton));
        lonText.setText(TabUtility.saveLon(fDeckRecord.getClon(), eastButton,
                westButton));

        latRainText.setText(
                TabUtility.saveLat(fDeckRecord.getRainAccumulationLat(),
                        northRainButton, southRainButton));
        lonRainText.setText(
                TabUtility.saveLon(fDeckRecord.getRainAccumulationLon(),
                        eastRainButton, westRainButton));
        setNumberField(inchesText,
                fDeckRecord.getRainAccumulationTimeInterval());

        latitudeText
                .setText(TabUtility.saveLat(fDeckRecord.getRadarSitePosLat(),
                        northButtonRadar, southButtonRadar));
        longitudeText
                .setText(TabUtility.saveLon(fDeckRecord.getRadarSitePosLon(),
                        eastButtonRadar, westButtonRadar));

        setNumberField(maxWindText, fDeckRecord.getWindMax());

        eyeWallCombo.setText(String
                .valueOf((int) fDeckRecord.getPercentOfEyewallObserved()));

        setNumberField(radMaxWindText, fDeckRecord.getRadiusOfMaximumWind());
        setNumberField(eyeDiaText, (fDeckRecord.getEyeDiameterNM()));
        setNumberField(spiralText, fDeckRecord.getSpiralOverlayDegrees());

        commentText.setText(fDeckRecord.getComments());
        initialsText.setText(fDeckRecord.getInitials());

        setNumberField(inOutTexts.get(0), fDeckRecord.getInboundMaxWindSpeed());
        setNumberField(inOutTexts.get(1),
                fDeckRecord.getInboundMaxWindAzimuth());
        setNumberField(inOutTexts.get(2),
                fDeckRecord.getInboundMaxWindRangeNM());
        setNumberField(inOutTexts.get(3),
                fDeckRecord.getInboundMaxWindElevationFeet());
        setNumberField(inOutTexts.get(4),
                fDeckRecord.getOutboundMaxWindSpeed());
        setNumberField(inOutTexts.get(5),
                fDeckRecord.getOutboundMaxWindAzimuth());
        setNumberField(inOutTexts.get(6),
                fDeckRecord.getOutboundMaxWindRangeNM());
        setNumberField(inOutTexts.get(7),
                fDeckRecord.getOutboundMaxWindElevationFeet());

        eyeCombo.setText(
                EyeShape.getValueFromString(fDeckRecord.getEyeShape()));

        TabUtility.setConfidence(fDeckRecord.getRadiiConfidence(),
                goodRadiiButton, fairRadiiButton, poorRadiiButton);
        TabUtility.setConfidence(fDeckRecord.getWindMaxConfidence(),
                goodWindButton, fairWindButton, poorWindButton);

        setRadarForm(fDeckRecord);
        setRadarType(fDeckRecord);

        // Have to set the wind speed portion
        for (int ii = 0; ii < fDeckRecords.size(); ii++) {
            // Gets the values for the NE/SE/SW/NW text fields
            setNumberField(ktTexts.get(ii * 4),
                    (int) fDeckRecords.get(ii).getWindRad1());
            setNumberField(ktTexts.get((ii * 4) + 1),
                    (int) fDeckRecords.get(ii).getWindRad2());
            setNumberField(ktTexts.get((ii * 4) + 2),
                    (int) fDeckRecords.get(ii).getWindRad3());
            setNumberField(ktTexts.get((ii * 4) + 3),
                    (int) fDeckRecords.get(ii).getWindRad4());

            // Gets the values for Circle/Quad radio buttons
            if ("NEQ".equals(fDeckRecords.get(ii).getWindCode())) {
                circleQuadButtons.get((ii * 2) + 1).setSelection(true);
                circleQuadButtons.get(ii * 2).setSelection(false);
            } else if ("AAA".equals(fDeckRecords.get(ii).getWindCode())) {
                circleQuadButtons.get((ii * 2) + 1).setSelection(false);
                circleQuadButtons.get(ii * 2).setSelection(true);
            }
        }

    }

    /**
     * Sets the radar form radio buttons
     */
    private void setRadarForm(FDeckRecord fDeckRecord) {
        if ("R".equals(fDeckRecord.getRadarFormat())) {
            radobButton.setSelection(true);
            plainLngButton.setSelection(false);
            dopplerButton.setSelection(false);

            if (!fDeckRecord.getRadobCode().isEmpty()) {
                if ("1".equals(fDeckRecord.getRadobCode().substring(0, 1))) {
                    goodButton.setSelection(true);
                    fairButton.setSelection(false);
                    poorButton.setSelection(false);
                } else if ("2"
                        .equals(fDeckRecord.getRadobCode().substring(0, 1))) {
                    goodButton.setSelection(false);
                    fairButton.setSelection(true);
                    poorButton.setSelection(false);
                } else if ("3"
                        .equals(fDeckRecord.getRadobCode().substring(0, 1))) {
                    goodButton.setSelection(false);
                    fairButton.setSelection(false);
                    poorButton.setSelection(true);
                }
            }
        }

        if ("P".equals(fDeckRecord.getRadarFormat())) {
            radobButton.setSelection(false);
            plainLngButton.setSelection(true);
            dopplerButton.setSelection(false);
            goodButton.setSelection(true);
            fairButton.setSelection(false);
            poorButton.setSelection(false);
        }

        if ("D".equals(fDeckRecord.getRadarFormat())) {
            radobButton.setSelection(false);
            plainLngButton.setSelection(false);
            dopplerButton.setSelection(true);
            goodButton.setSelection(true);
            fairButton.setSelection(false);
            poorButton.setSelection(false);
        }

    }

    /**
     * Sets the radar type radio buttons
     * 
     * @param fDeckRecord
     */
    private void setRadarType(FDeckRecord fDeckRecord) {

        if ("L".equals(fDeckRecord.getRadarType())) {
            landButton.setSelection(true);
            shipButton.setSelection(false);
            aircraftButton.setSelection(false);
            satelliteButton.setSelection(false);
        }

        if ("S".equals(fDeckRecord.getRadarType())) {
            landButton.setSelection(false);
            shipButton.setSelection(true);
            aircraftButton.setSelection(false);
            satelliteButton.setSelection(false);
        }

        if ("A".equals(fDeckRecord.getRadarType())) {
            landButton.setSelection(false);
            shipButton.setSelection(false);
            aircraftButton.setSelection(true);
            satelliteButton.setSelection(false);
        }

        if ("T".equals(fDeckRecord.getRadarType())) {
            landButton.setSelection(false);
            shipButton.setSelection(false);
            aircraftButton.setSelection(false);
            satelliteButton.setSelection(true);
        }
    }

    /**
     * Reset all fields for a new entry
     */
    protected void clearFields() {
        wmoIdCombo.setText("");

        centerFixChk.setSelection(false);
        maxWindFixChk.setSelection(false);
        windRadChk.setSelection(false);

        latText.setText("");
        lonText.setText("");
        northButton.setSelection(true);
        southButton.setSelection(false);
        westButton.setSelection(true);
        eastButton.setSelection(false);

        latitudeText.setText("");
        longitudeText.setText("");
        northButtonRadar.setSelection(true);
        southButtonRadar.setSelection(false);
        westButtonRadar.setSelection(true);
        eastButtonRadar.setSelection(false);

        radobButton.setSelection(true);
        plainLngButton.setSelection(false);
        dopplerButton.setSelection(false);
        goodButton.setSelection(true);
        fairButton.setSelection(false);
        poorButton.setSelection(false);

        landButton.setSelection(true);
        shipButton.setSelection(false);
        aircraftButton.setSelection(false);
        satelliteButton.setSelection(false);

        maxWindText.setText("");
        goodRadiiButton.setSelection(false);
        fairRadiiButton.setSelection(true);
        poorRadiiButton.setSelection(false);
        goodWindButton.setSelection(false);
        fairWindButton.setSelection(true);
        poorWindButton.setSelection(false);

        for (Text text : inOutTexts) {
            text.setText("");
        }

        for (Text text : ktTexts) {
            text.setText("");
        }

        // Set every other button true
        for (int i = 0; i < circleQuadButtons.size(); i++) {
            circleQuadButtons.get(i).setSelection(i % 2 == 0);
        }

        inchesText.setText("");
        hoursText.setText("");
        northRainButton.setSelection(true);
        southRainButton.setSelection(false);
        eastRainButton.setSelection(false);
        westRainButton.setSelection(true);
        latRainText.setText("");
        lonRainText.setText("");

        radMaxWindText.setText("");
        eyeCombo.setText("None");
        eyeDiaText.setText("");
        eyeWallCombo.setText("0");
        spiralText.setText("");

        commentText.setText("");
        initialsText.setText("");

    }

    /**
     * Saves the radar form
     * 
     * @return the radar form
     */
    private String saveRadarForm(FDeckRecord fDeckRecord) {
        String radarForm = "";
        if (radobButton.getSelection()) {
            radarForm = "R";
            if (goodButton.getSelection()) {
                fDeckRecord.setRadobCode("1/////////");
            } else if (fairButton.getSelection()) {
                fDeckRecord.setRadobCode("2/////////");
            } else if (poorButton.getSelection()) {
                fDeckRecord.setRadobCode("3/////////");
            }
        }

        if (plainLngButton.getSelection()) {
            radarForm = "P";
        }

        if (dopplerButton.getSelection()) {
            radarForm = "D";
        }

        return radarForm;
    }

    /**
     * Saves the current values of the editable fields to the provided
     * FDeckRecord
     * 
     * @param fDeckRecord
     */
    protected void saveFieldsToRec(FDeckRecord fDeckRecord) {
        fDeckRecord.setFixSite(wmoIdCombo.getText());
        fDeckRecord.setCenterOrIntensity(centerInts);
        fDeckRecord.setRadarType(saveRadarType());
        fDeckRecord.setRadarFormat(saveRadarForm(fDeckRecord));
        fDeckRecord.setClon(AtcfVizUtil.getStringAsFloat(lonText.getText(),
                westButton.getSelection(), false, 0));
        fDeckRecord.setClat(AtcfVizUtil.getStringAsFloat(latText.getText(),
                southButton.getSelection(), false, 0));

        fDeckRecord.setRadarSitePosLon(
                AtcfVizUtil.getStringAsFloat(longitudeText.getText(),
                        eastButtonRadar.getSelection(), false, 0));
        fDeckRecord.setRadarSitePosLat(
                AtcfVizUtil.getStringAsFloat(latitudeText.getText(),
                        southButtonRadar.getSelection(), false, 0));

        fDeckRecord.setWindMax(AtcfVizUtil
                .getStringAsFloat(maxWindText.getText(), false, false, 0));
        fDeckRecord.setWindMaxConfidence(TabUtility.saveConfidence(
                goodWindButton, fairWindButton, poorWindButton));
        fDeckRecord.setRadiiConfidence(TabUtility.saveConfidence(
                goodRadiiButton, fairRadiiButton, poorRadiiButton));

        fDeckRecord.setSpiralOverlayDegrees(AtcfVizUtil
                .getStringAsFloat(spiralText.getText(), false, false, 0));

        fDeckRecord.setEyeDiameterNM(AtcfVizUtil
                .getStringAsFloat(eyeDiaText.getText(), false, false, 0));

        fDeckRecord.setRadiusOfMaximumWind(AtcfVizUtil
                .getStringAsFloat(radMaxWindText.getText(), false, false, 0));

        fDeckRecord.setPercentOfEyewallObserved(AtcfVizUtil
                .getStringAsFloat(eyeWallCombo.getText(), false, false, 0));

        if (!"None".equals(eyeCombo.getText())) {
            fDeckRecord.setEyeShape(
                    EyeShape.getNameFromString(eyeCombo.getText()));
        }

        fDeckRecord.setInboundMaxWindSpeed(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(0).getText(), false, false, 0));
        fDeckRecord.setInboundMaxWindAzimuth(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(1).getText(), false, false, 0));
        fDeckRecord.setInboundMaxWindRangeNM(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(2).getText(), false, false, 0));
        fDeckRecord.setInboundMaxWindElevationFeet(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(3).getText(), false, false, 0));

        fDeckRecord.setOutboundMaxWindSpeed(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(4).getText(), false, false, 0));
        fDeckRecord.setOutboundMaxWindAzimuth(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(5).getText(), false, false, 0));
        fDeckRecord.setOutboundMaxWindRangeNM(AtcfVizUtil.getStringAsFloat(
                inOutTexts.get(6).getText(), false, false, 0));
        fDeckRecord.setOutboundMaxWindElevationFeet(
                AtcfVizUtil.getStringAsFloat(inOutTexts.get(7).getText(), false,
                        false, 0));

        fDeckRecord.setComments(commentText.getText());
        fDeckRecord.setInitials(initialsText.getText());

        fDeckRecord.setRainAccumulationLat(
                AtcfVizUtil.getStringAsFloat(latRainText.getText(),
                        southRainButton.getSelection(), false, 0));
        fDeckRecord.setRainAccumulationLon(
                AtcfVizUtil.getStringAsFloat(lonRainText.getText(),
                        eastRainButton.getSelection(), false, 0));

        fDeckRecord.setMaxRainAccumulation(AtcfVizUtil
                .getStringAsFloat(inchesText.getText(), false, false, 0));
        fDeckRecord.setRainAccumulationTimeInterval(AtcfVizUtil
                .getStringAsFloat(hoursText.getText(), false, false, 0));

    }

    /**
     * Handles the cases for multiple FDeckRecords
     * 
     * @param fDeckRecord
     * @return
     */
    @Override
    protected List<FDeckRecord> createRecordsByWindSpeeds(
            List<FDeckRecord> tempMod) {

        // Loop through the records for 34, 50, and 64kt winds to set the data
        // from the middle portion of the dialog
        for (int ii = 0; ii < tempMod.size(); ii++) {

            if (tempMod.size() == 1) {
                tempMod.get(ii).setRadiusOfWindIntensity(34);
            } else if (tempMod.size() == 2) {
                tempMod.get(ii).setRadiusOfWindIntensity(50);
            } else {
                tempMod.get(ii).setRadiusOfWindIntensity(64);
            }

            // need every other one
            // This gets the values from the 34, 50, and 64tk Circle and Quad
            // radio buttons
            if (circleQuadButtons.get(ii * 2).getSelection()) {
                tempMod.get(ii).setWindCode("AAA");
                tempMod.get(ii).setRadWindQuad("AAA");
            } else {
                tempMod.get(ii).setWindCode("NEQ");
                tempMod.get(ii).setRadWindQuad("NEQ");
            }

            // need to get numnbers 0 - 3, but incrementing per rec
            // This gets the values from the NE, SE, SW, and NW text boxes
            tempMod.get(ii)
                    .setWindRad1(AtcfVizUtil.getStringAsInt(
                            ktTexts.get(ii * 4).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setWindRad2(AtcfVizUtil.getStringAsInt(
                            ktTexts.get((ii * 4) + 1).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setWindRad3(AtcfVizUtil.getStringAsInt(
                            ktTexts.get((ii * 4) + 2).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setWindRad4(AtcfVizUtil.getStringAsInt(
                            ktTexts.get((ii * 4) + 3).getText(), false, true,
                            TabUtility.DEFAULT));

            // These are duplicate values
            tempMod.get(0)
                    .setQuad1WindRad(AtcfVizUtil.getStringAsFloat(
                            ktTexts.get(ii * 4).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(0)
                    .setQuad2WindRad(AtcfVizUtil.getStringAsFloat(
                            ktTexts.get((ii * 4) + 1).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(0)
                    .setQuad3WindRad(AtcfVizUtil.getStringAsFloat(
                            ktTexts.get((ii * 4) + 2).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(0)
                    .setQuad4WindRad(AtcfVizUtil.getStringAsFloat(
                            ktTexts.get((ii * 4) + 3).getText(), false, true,
                            TabUtility.DEFAULT));

        }

        return tempMod;
    }

}

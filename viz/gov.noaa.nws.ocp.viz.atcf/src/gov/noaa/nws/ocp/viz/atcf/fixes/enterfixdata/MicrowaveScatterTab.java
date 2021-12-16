/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

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
 * The Composite for the Microwave and Scatterometer tabs
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
public class MicrowaveScatterTab extends EditFixesTab {
    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(MicrowaveScatterTab.class);

    private Button centerFixChk;

    private Button maxWindFixChk;

    private Button windRadChk;

    private Button minSfcChk;

    private Text latText;

    private Button rssButton;

    private Text slpText;

    private Text maxWindText;

    private Button nesdisButton;

    private Text lonText;

    private Button southButton;

    private Button westButton;

    private Button northButton;

    private Button rainButton;

    private Text rainText;

    private Button fnmocButton;

    private Button eastButton;

    private Combo satTypeCombo;

    private Text eyeText;

    private Text seasText;

    private Text waveText;

    private Text commentCombo;

    private Combo fixSiteCombo;

    private Text initialsText;

    private Text tempText;

    private Button goodSlpButton;

    private Button fairSlpButton;

    private Button poorSlpButton;

    private Button goodWindButton;

    private Button fairWindButton;

    private Button poorWindButton;

    private Button goodPosButton;

    private Button fairPosButton;

    private Button poorPosButton;

    private ArrayList<Text> windTexts;

    private ArrayList<Button> circleQuadButtons;

    private ArrayList<Button> passCutoffButtons;

    private ArrayList<Button> radiiConfButtons;

    private String centerInts;

    private Date currDate;

    private ArrayList<FixTypeEntry> typeEntries;

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
    public MicrowaveScatterTab(TabFolder parent, String tabName, Storm storm,
            boolean editData, String[] actionBtnNames, int sandBoxId,
            Map<FDeckRecordKey, java.util.List<FDeckRecord>> fixData,
            List dtgSelectionList) {
        super(parent, SWT.NONE, dtgSelectionList);
        this.editData = editData;
        this.actionBtnNames = actionBtnNames;
        this.sandBoxID = sandBoxId;
        this.storm = storm;
        this.tabName = tabName;
        this.fixDataMap = fixData;
        shell = getShell();
        currDate = new Date();
        selectedRecords = new ArrayList<>();
        typeEntries = new ArrayList<>();
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("AMSU"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("SSMI"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("TRMM"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("ALTI"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("ERS2"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("QSCT"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("SEAW"));
        typeEntries.add(AtcfConfigurationManager.getInstance().getFixTypes()
                .getFixType("ASCT"));

        TabItem tab = new TabItem(parent, SWT.NONE);
        tab.setText(tabName);

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
                    logger.warn(
                            "MicrowaveScatterTab" + TabUtility.DTG_ERROR_STRING,
                            e);
                }
                satTypeCombo.setText(rec.getSatelliteType());
                fixSiteCombo.setText(rec.getFixSite());
            }
            fillData();
        }

        centerInts = TabUtility.setCenterIntensity(centerFixChk.getSelection(),
                maxWindFixChk.getSelection(), windRadChk.getSelection(),
                minSfcChk.getSelection());
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
        satTypeCombo.add("NOAA19");
        satTypeCombo.add("NOAA90");
        satTypeCombo.add("METOPA");
        satTypeCombo.add("METOPB");
        satTypeCombo.add("ASCT");
        satTypeCombo.add("SSMS");
        satTypeCombo.add("AMSR");
        satTypeCombo.add("WSAT");
        satTypeCombo.add("GPM");
        satTypeCombo.add("AMSU");
        satTypeCombo.add("SSMI");
        satTypeCombo.add("AMSR");
        satTypeCombo.add("GPMI");
        satTypeCombo.select(0);

        if (editData) {
            satTypeCombo.addModifyListener(e -> fillData());
        }
    }

    /**
     * Assemble the first few rows of widgets from the "Center/Intensity"
     * options up to "Max. Wind Speed."
     */
    private void createCIComp(Composite parent) {
        Group topComp = new Group(parent, SWT.NONE);
        GridLayout topLayout = new GridLayout(1, false);
        topLayout.marginBottom = 5;
        topLayout.marginWidth = 15;
        topLayout.horizontalSpacing = 20;
        topComp.setLayout(topLayout);
        topComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite centerWindComp = new Composite(topComp, SWT.NONE);
        GridLayout centerWindLayout = new GridLayout(5, false);
        centerWindLayout.horizontalSpacing = 35;
        centerWindComp.setLayout(centerWindLayout);
        centerWindComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label cLbl = new Label(centerWindComp, SWT.NONE);
        cLbl.setText("* Center/Intensity ");
        SelectionAdapter centerIntsAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                centerInts = TabUtility.setCenterIntensity(
                        centerFixChk.getSelection(),
                        maxWindFixChk.getSelection(), windRadChk.getSelection(),
                        minSfcChk.getSelection());
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

        minSfcChk = new Button(centerWindComp, SWT.CHECK);
        minSfcChk.setText("Min Sfc Pressure Fix");
        minSfcChk.addSelectionListener(centerIntsAdapter);

        Composite latlonComp = new Composite(topComp, SWT.NONE);
        GridLayout latlonLayout = new GridLayout(6, false);
        latlonLayout.marginWidth = 0;
        latlonComp.setLayout(latlonLayout);
        latlonComp.setLayoutData(
                new GridData(SWT.NONE, SWT.DEFAULT, false, false));

        Composite latitudeComp = new Composite(latlonComp, SWT.NONE);
        GridLayout latitudeLayout = new GridLayout(4, false);
        latitudeComp.setLayout(latitudeLayout);
        latitudeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label latLabel = new Label(latitudeComp, SWT.NONE);
        latLabel.setText("Lat");

        latText = new Text(latitudeComp, SWT.BORDER);
        latText.addListener(SWT.Verify, verifyListeners.getLatVerifyListener());

        northButton = new Button(latitudeComp, SWT.RADIO);
        northButton.setText("N");
        northButton.setSelection(true);

        southButton = new Button(latitudeComp, SWT.RADIO);
        southButton.setText("S");

        Composite longitudeComp = new Composite(latlonComp, SWT.NONE);
        GridLayout longitudeLayout = new GridLayout(4, false);
        longitudeComp.setLayout(longitudeLayout);
        longitudeComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label lonLabel = new Label(longitudeComp, SWT.NONE);
        lonLabel.setText("Lon");

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

        Label confLabel1 = new Label(latlonComp, SWT.NONE);
        confLabel1.setText("\tConfidence ");
        goodPosButton = new Button(latlonComp, SWT.RADIO);
        goodPosButton.setText("Good");
        fairPosButton = new Button(latlonComp, SWT.RADIO);
        fairPosButton.setText("Fair");
        fairPosButton.setSelection(true);
        poorPosButton = new Button(latlonComp, SWT.RADIO);
        poorPosButton.setText("Poor");

        Composite maxWindComp = new Composite(topComp, SWT.NONE);
        GridLayout maxWindLayout = new GridLayout(7, false);
        maxWindComp.setLayout(maxWindLayout);
        maxWindComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label maxWindLabel = new Label(maxWindComp, SWT.NONE);
        maxWindLabel.setText("Max. Wind Speed");

        maxWindText = new Text(maxWindComp, SWT.BORDER);
        GridData maxWindGridData = new GridData();
        maxWindGridData.widthHint = 50;
        maxWindText.setLayoutData(maxWindGridData);

        Label ktsLabel = new Label(maxWindComp, SWT.NONE);
        ktsLabel.setText("kts");

        Label confLabel2 = new Label(maxWindComp, SWT.NONE);
        confLabel2.setText("\tWind Speed Confidence ");
        goodWindButton = new Button(maxWindComp, SWT.RADIO);
        goodWindButton.setText("Good");
        fairWindButton = new Button(maxWindComp, SWT.RADIO);
        fairWindButton.setText("Fair");
        fairWindButton.setSelection(true);
        poorWindButton = new Button(maxWindComp, SWT.RADIO);
        poorWindButton.setText("Poor");

    }

    /**
     * Create the rows of wind data fields.
     * 
     * @param parent
     */
    private void createWindComp(Composite parent) {
        windTexts = new ArrayList<>();
        circleQuadButtons = new ArrayList<>();
        passCutoffButtons = new ArrayList<>();
        radiiConfButtons = new ArrayList<>();

        Group windComp = new Group(parent, SWT.NONE);
        GridLayout windLayout = new GridLayout(4, false);
        windLayout.marginBottom = 5;
        windLayout.marginWidth = 15;
        windLayout.horizontalSpacing = 20;
        windComp.setLayout(windLayout);
        windComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        String[] ktStrings = new String[] { "34", "50", "64" };
        // Create the 3 columns of wind data.
        for (int jj = 0; jj < 3; jj++) {
            Composite ktComp = new Composite(windComp, SWT.NONE);
            // For the first column, add a column of appropriate labels and
            // blank spaces appropriate for each row.
            GridLayout ktLayout = new GridLayout(jj == 0 ? 4 : 3, false);
            ktComp.setLayout(ktLayout);
            ktComp.setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));

            if (jj == 0) {
                new Label(ktComp, SWT.NONE).setText(" ");
            }

            new Label(ktComp, SWT.NONE).setText(ktStrings[jj] + " kt winds");

            Button circleChk = new Button(ktComp, SWT.RADIO);
            circleChk.setText("Circle");
            circleChk.setSelection(true);
            Button quadsChk = new Button(ktComp, SWT.RADIO);
            quadsChk.setText("Quads");

            // TODO if Circle is selected, only NE Row is visible
            circleQuadButtons.add(circleChk);
            circleQuadButtons.add(quadsChk);

            if (jj == 0) {
                new Label(ktComp, SWT.NONE).setText(" ");
            }

            new Label(ktComp, SWT.NONE).setText(" ");
            new Label(ktComp, SWT.NONE).setText("Pass Edge");
            new Label(ktComp, SWT.NONE).setText("Cut off by land");
            String[] directionStrings = new String[] { "NE", "SE", "SW", "NW" };
            for (int ii = 0; ii < 4; ii++) {
                if (jj == 0) {
                    new Label(ktComp, SWT.NONE).setText(directionStrings[ii]);
                }
                GridData windGridData = new GridData();
                windGridData.widthHint = 40;
                Text windText = new Text(ktComp, SWT.BORDER);
                windText.setLayoutData(windGridData);
                windTexts.add(windText);
                Button passButton = new Button(ktComp, SWT.CHECK);
                Button cutButton = new Button(ktComp, SWT.CHECK);
                passCutoffButtons.add(passButton);
                passCutoffButtons.add(cutButton);
            }

            Composite ktRadComp = new Composite(ktComp, SWT.NONE);
            GridLayout ktRadCompLayout = new GridLayout(4, false);
            ktRadComp.setLayout(ktRadCompLayout);
            GridData ktRadCompGridData = new GridData();
            ktRadCompGridData.horizontalSpan = 4;
            ktRadComp.setLayoutData(ktRadCompGridData);
            if (jj == 0) {
                new Label(ktRadComp, SWT.NONE).setText("Radii Conf.");
            }
            Button goodChk = new Button(ktRadComp, SWT.RADIO);
            goodChk.setText("Good");

            Button fairChk = new Button(ktRadComp, SWT.RADIO);
            fairChk.setText("Fair");
            fairChk.setSelection(true);

            Button poorChk = new Button(ktRadComp, SWT.RADIO);
            poorChk.setText("Poor");
            radiiConfButtons.add(goodChk);
            radiiConfButtons.add(fairChk);
            radiiConfButtons.add(poorChk);
        }
    }

    /**
     * Create the remaining fields at the bottom of the tab up to the "Initials"
     * field.
     * 
     * @param parent
     */
    private void createCommentComp(Composite parent) {
        Group bottomComp = new Group(parent, SWT.NONE);
        GridLayout bottomLayout = new GridLayout(1, false);
        bottomLayout.marginBottom = 5;
        bottomLayout.marginWidth = 15;
        bottomLayout.horizontalSpacing = 20;
        bottomComp.setLayout(bottomLayout);
        bottomComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite rainComp = new Composite(bottomComp, SWT.NONE);
        GridLayout rainLayout = new GridLayout(8, false);
        rainComp.setLayout(rainLayout);
        rainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        rainButton = new Button(rainComp, SWT.CHECK);
        rainButton.setText("Rain");

        new Label(rainComp, SWT.NONE).setText("\tRain rate");
        rainText = new Text(rainComp, SWT.BORDER);
        GridData rainGridData = new GridData();
        rainGridData.widthHint = 50;
        rainText.setLayoutData(rainGridData);

        new Label(rainComp, SWT.NONE).setText("mm/h");

        new Label(rainComp, SWT.NONE).setText("\tRain Algorithm ");

        fnmocButton = new Button(rainComp, SWT.RADIO);
        fnmocButton.setText("FNMOC");
        fnmocButton.setSelection(true);
        nesdisButton = new Button(rainComp, SWT.RADIO);
        nesdisButton.setText("NESDIS");
        rssButton = new Button(rainComp, SWT.RADIO);
        rssButton.setText("RSS");

        Composite slpComp = new Composite(bottomComp, SWT.NONE);
        GridLayout slpLayout = new GridLayout(7, false);
        slpComp.setLayout(slpLayout);
        slpComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        new Label(slpComp, SWT.NONE).setText("SLP");
        slpText = new Text(slpComp, SWT.BORDER);
        GridData slpGridData = new GridData();
        slpGridData.widthHint = 50;
        slpText.setLayoutData(slpGridData);

        new Label(slpComp, SWT.NONE).setText("mb");

        new Label(slpComp, SWT.NONE).setText("\tConfidence ");

        goodSlpButton = new Button(slpComp, SWT.RADIO);
        goodSlpButton.setText("Good");
        fairSlpButton = new Button(slpComp, SWT.RADIO);
        fairSlpButton.setText("Fair");
        fairSlpButton.setSelection(true);
        poorSlpButton = new Button(slpComp, SWT.RADIO);
        poorSlpButton.setText("Poor");

        Composite tempComp = new Composite(bottomComp, SWT.NONE);
        GridLayout tempLayout = new GridLayout(12, false);
        tempComp.setLayout(tempLayout);
        tempComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        new Label(tempComp, SWT.NONE).setText("Temp");
        tempText = new Text(tempComp, SWT.BORDER);
        GridData tempGridData = new GridData();
        tempGridData.widthHint = 50;
        tempText.setLayoutData(tempGridData);

        new Label(tempComp, SWT.NONE).setText("celsius");

        new Label(tempComp, SWT.NONE).setText("\tEye Diameter");
        eyeText = new Text(tempComp, SWT.BORDER);
        GridData eyeGridData = new GridData();
        eyeGridData.widthHint = 50;
        eyeText.setLayoutData(eyeGridData);

        new Label(tempComp, SWT.NONE).setText("nm");

        new Label(tempComp, SWT.NONE).setText("\tWave Height");
        waveText = new Text(tempComp, SWT.BORDER);
        GridData waveGridData = new GridData();
        waveGridData.widthHint = 50;
        waveText.setLayoutData(waveGridData);

        new Label(tempComp, SWT.NONE).setText("ft");

        new Label(tempComp, SWT.NONE).setText("\tMax Seas");
        seasText = new Text(tempComp, SWT.BORDER);
        GridData seasGridData = new GridData();
        seasGridData.widthHint = 50;
        seasText.setLayoutData(seasGridData);

        new Label(tempComp, SWT.NONE).setText("ft");

        Composite commentComp = new Composite(bottomComp, SWT.NONE);
        GridLayout commentLayout = new GridLayout(2, false);
        commentComp.setLayout(commentLayout);
        commentComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        new Label(commentComp, SWT.NONE).setText("Comments");
        commentCombo = new Text(commentComp, SWT.BORDER);
        GridData commentGridData = new GridData();
        commentGridData.widthHint = 500;
        commentCombo.setLayoutData(commentGridData);

        new Label(commentComp, SWT.NONE).setText("Initials");
        GridData initialsGridData = new GridData();
        initialsGridData.widthHint = 60;
        initialsText = new Text(commentComp, SWT.BORDER);
        initialsText.setLayoutData(initialsGridData);
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
                !(findCurrentRec()), currDate, shell,
                !(satTypeCombo.getText().isEmpty()), true, true, true, true,
                true, true)) {
            java.util.List<FDeckRecord> recs = new ArrayList<>();
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
                fDeckRecord.setYear(storm.getYear());
                fDeckRecord.setSatelliteType(satTypeCombo.getText());

                if (tabName.equals(FixFormat.MICROWAVE.getDescription())) {
                    fDeckRecord.setFixFormat(FixFormat.MICROWAVE.getValue());
                    fDeckRecord.setFixType("AMSU");
                } else if (tabName
                        .equals(FixFormat.SCATTEROMETER.getDescription())) {
                    fDeckRecord
                            .setFixFormat(FixFormat.SCATTEROMETER.getValue());
                    fDeckRecord.setFixType("ASCT");
                }

                saveFieldsToRec(fDeckRecord);
            }

            ret = validRecordUpdate(recs);
        }

        return ret;
    }

    /**
     * Saves the values of Pass/Cut to the database
     * 
     * @param pass
     * @param cut
     * @return
     */
    private String savePassCut(boolean pass, boolean cut) {
        String passCut = "";
        if (pass) {
            passCut = "E";
        }

        if (cut) {
            passCut = "C";
        }

        if (pass && cut) {
            passCut = "B";
        }

        return passCut;
    }

    /**
     * Sets the selections of the Pass/Cut checkboxes from the database
     * 
     * @param val
     */
    private void setPassCut(String val, Button edge, Button cut) {
        if ("E".equals(val)) {
            edge.setSelection(true);
            cut.setSelection(false);
        } else if ("C".equals(val)) {
            edge.setSelection(false);
            cut.setSelection(true);
        } else if ("B".equals(val)) {
            edge.setSelection(true);
            cut.setSelection(true);
        }
    }

    /**
     * Saves the process to the database
     * 
     * @return process
     */
    private String saveProcess() {
        String ret = "";
        if (rssButton.getSelection()) {
            ret = "RSS";
        } else if (fnmocButton.getSelection()) {
            ret = "FNMOC";
        } else if (nesdisButton.getSelection()) {
            ret = "NESDIS";
        }

        return ret;
    }

    /**
     * Sets the process
     * 
     * @param fDeckRecord
     */
    private void setProcess(FDeckRecord fDeckRecord) {
        if ("RSS".equals(fDeckRecord.getProcess())) {
            rssButton.setSelection(true);
            fnmocButton.setSelection(false);
            nesdisButton.setSelection(false);
        } else if ("NESDIS".equals(fDeckRecord.getProcess())) {
            rssButton.setSelection(false);
            fnmocButton.setSelection(false);
            nesdisButton.setSelection(true);
        } else {
            rssButton.setSelection(false);
            fnmocButton.setSelection(true);
            nesdisButton.setSelection(false);
        }
    }

    /**
     * Handles the cases for multiple FDeckRecords
     * 
     * @param fDeckRecord
     * @return
     */
    @Override
    protected java.util.List<FDeckRecord> createRecordsByWindSpeeds(
            java.util.List<FDeckRecord> tempMod) {

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

            // have to get numbers 0-7, but incrementing per rec
            // This gets the values from the 'Pass Edge' and 'Cut off by land'
            // check buttons
            tempMod.get(ii).setRadMod1(savePassCut(
                    passCutoffButtons.get((ii * 8)).getSelection(),
                    passCutoffButtons.get((ii * 8) + 1).getSelection()));
            tempMod.get(ii).setRadMod2(savePassCut(
                    passCutoffButtons.get((ii * 8) + 2).getSelection(),
                    passCutoffButtons.get((ii * 8) + 3).getSelection()));
            tempMod.get(ii).setRadMod3(savePassCut(
                    passCutoffButtons.get((ii * 8) + 4).getSelection(),
                    passCutoffButtons.get((ii * 8) + 5).getSelection()));
            tempMod.get(ii).setRadMod4(savePassCut(
                    passCutoffButtons.get((ii * 8) + 6).getSelection(),
                    passCutoffButtons.get((ii * 8) + 7).getSelection()));

            // need to get number 0 - 2, but incrementing per rec
            // This gets the values from the radii Conf radio buttons
            tempMod.get(ii)
                    .setRadiiConfidence(TabUtility.saveConfidence(
                            radiiConfButtons.get(ii * 3),
                            radiiConfButtons.get((ii * 3) + 1),
                            radiiConfButtons.get((ii * 3) + 2)));
            // These are duplicate values
            tempMod.get(ii)
                    .setMicrowaveRadiiConfidence(TabUtility.saveConfidence(
                            radiiConfButtons.get(ii * 3),
                            radiiConfButtons.get((ii * 3) + 1),
                            radiiConfButtons.get((ii * 3) + 2)));

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
                            windTexts.get(ii * 4).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setWindRad2(AtcfVizUtil.getStringAsInt(
                            windTexts.get((ii * 4) + 1).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setWindRad3(AtcfVizUtil.getStringAsInt(
                            windTexts.get((ii * 4) + 2).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setWindRad4(AtcfVizUtil.getStringAsInt(
                            windTexts.get((ii * 4) + 3).getText(), false, true,
                            TabUtility.DEFAULT));

            // These are duplicate values
            tempMod.get(ii)
                    .setQuad1WindRad(AtcfVizUtil.getStringAsFloat(
                            windTexts.get(ii * 4).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setQuad2WindRad(AtcfVizUtil.getStringAsFloat(
                            windTexts.get((ii * 4) + 1).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setQuad3WindRad(AtcfVizUtil.getStringAsFloat(
                            windTexts.get((ii * 4) + 2).getText(), false, true,
                            TabUtility.DEFAULT));
            tempMod.get(ii)
                    .setQuad4WindRad(AtcfVizUtil.getStringAsFloat(
                            windTexts.get((ii * 4) + 3).getText(), false, true,
                            TabUtility.DEFAULT));

        }

        return tempMod;
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

        for (FixTypeEntry type : typeEntries) {
            if (type != null) {
                FDeckRecordKey tempKey = new FDeckRecordKey(dtgText.getText(),
                        fixSiteCombo.getText(), type.getName(),
                        satTypeCombo.getText());

                if (fixDataMap.containsKey(tempKey)) {
                    selectedRecords = fixDataMap.get(tempKey);
                    ret = true;
                }
            }
        }

        return ret;
    }

    /**
     * Reset all fields for a new entry
     */
    protected void clearFields() {
        fixSiteCombo.setText("");
        centerFixChk.setSelection(false);
        maxWindFixChk.setSelection(false);
        windRadChk.setSelection(false);
        minSfcChk.setSelection(false);
        latText.setText("");
        lonText.setText("");
        northButton.setSelection(true);
        southButton.setSelection(false);
        eastButton.setSelection(false);
        westButton.setSelection(true);
        goodPosButton.setSelection(false);
        goodSlpButton.setSelection(false);
        goodWindButton.setSelection(false);
        fairPosButton.setSelection(true);
        fairSlpButton.setSelection(true);
        fairWindButton.setSelection(true);
        poorPosButton.setSelection(false);
        poorSlpButton.setSelection(false);
        poorWindButton.setSelection(false);
        maxWindText.setText("");

        for (Text text : windTexts) {
            text.setText("");
        }

        // Set every other button true
        for (int i = 0; i < circleQuadButtons.size(); i++) {
            circleQuadButtons.get(i).setSelection(i % 2 == 0);
        }

        for (Button button : passCutoffButtons) {
            button.setSelection(false);
        }

        // Set every 'fair' button true
        for (int i = 0; i < radiiConfButtons.size(); i++) {
            radiiConfButtons.get(i).setSelection((i - 1) % 3 == 0);
        }

        rainButton.setSelection(false);
        rainText.setText("");
        fnmocButton.setSelection(true);
        nesdisButton.setSelection(false);
        rssButton.setSelection(false);
        slpText.setText("");
        tempText.setText("");
        eyeText.setText("");
        waveText.setText("");
        seasText.setText("");
        commentCombo.setText("");
        initialsText.setText("");

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

        latText.setText(TabUtility.saveLat(fDeckRecord.getClat(), northButton,
                southButton));
        lonText.setText(TabUtility.saveLon(fDeckRecord.getClon(), eastButton,
                westButton));
        TabUtility.saveCenterIntensity(fDeckRecord.getCenterOrIntensity(),
                centerFixChk, maxWindFixChk, windRadChk, minSfcChk);
        TabUtility.setConfidence(fDeckRecord.getPressureConfidence(),
                goodSlpButton, fairSlpButton, poorSlpButton);
        TabUtility.setConfidence(fDeckRecord.getWindMaxConfidence(),
                goodWindButton, fairWindButton, poorWindButton);
        TabUtility.setConfidence(fDeckRecord.getPositionConfidence(),
                goodPosButton, fairPosButton, poorPosButton);

        setProcess(fDeckRecord);
        rainButton.setSelection(!fDeckRecord.getRainFlag().isEmpty());
        setNumberField(rainText, fDeckRecord.getRainRate());
        setNumberField(slpText, fDeckRecord.getMslp());
        setNumberField(tempText, fDeckRecord.getTempPassiveMicrowave());
        setNumberField(eyeText, fDeckRecord.getEyeDiameterNM());
        setNumberField(waveText, fDeckRecord.getWaveHeight());
        setNumberField(seasText, fDeckRecord.getMaxSeas());

        commentCombo.setText(fDeckRecord.getComments());
        initialsText.setText(fDeckRecord.getInitials());

        setNumberField(maxWindText, fDeckRecord.getWindMax());

        // Have to set the middle portion...
        for (int ii = 0; ii < fDeckRecords.size(); ii++) {
            // Gets the values for the NE/SE/SW/NW text fields
            setNumberField(windTexts.get(ii * 4),
                    (int) fDeckRecords.get(ii).getWindRad1());
            setNumberField(windTexts.get((ii * 4) + 1),
                    (int) fDeckRecords.get(ii).getWindRad2());
            setNumberField(windTexts.get((ii * 4) + 2),
                    (int) fDeckRecords.get(ii).getWindRad3());
            setNumberField(windTexts.get((ii * 4) + 3),
                    (int) fDeckRecords.get(ii).getWindRad4());

            // Gets the values for the PassEdge/Cutoff check buttons
            setPassCut(fDeckRecords.get(ii).getRadMod1(),
                    passCutoffButtons.get(ii * 8),
                    passCutoffButtons.get((ii * 8) + 1));
            setPassCut(fDeckRecords.get(ii).getRadMod2(),
                    passCutoffButtons.get((ii * 8) + 2),
                    passCutoffButtons.get((ii * 8) + 3));
            setPassCut(fDeckRecords.get(ii).getRadMod3(),
                    passCutoffButtons.get((ii * 8) + 4),
                    passCutoffButtons.get((ii * 8) + 5));
            setPassCut(fDeckRecords.get(ii).getRadMod4(),
                    passCutoffButtons.get((ii * 8) + 6),
                    passCutoffButtons.get((ii * 8) + 7));

            // Gets the values for Circle/Quad radio buttons
            if ("NEQ".equals(fDeckRecords.get(ii).getWindCode())) {
                circleQuadButtons.get((ii * 2) + 1).setSelection(true);
                circleQuadButtons.get(ii * 2).setSelection(false);
            } else if ("AAA".equals(fDeckRecords.get(ii).getWindCode())) {
                circleQuadButtons.get((ii * 2) + 1).setSelection(false);
                circleQuadButtons.get(ii * 2).setSelection(true);
            }

            // Gets the values for the Radii Conf radio buttons
            TabUtility.setConfidence(fDeckRecords.get(ii).getRadiiConfidence(),
                    radiiConfButtons.get(ii * 3),
                    radiiConfButtons.get((ii * 3) + 1),
                    radiiConfButtons.get((ii * 3) + 2));

        }

    }

    /**
     * Saves the current values of the editable fields to the provided
     * FDeckRecord
     * 
     * @param fDeckRecord
     */
    protected void saveFieldsToRec(FDeckRecord fDeckRecord) {
        fDeckRecord.setCenterOrIntensity(centerInts);
        fDeckRecord.setClon(AtcfVizUtil.getStringAsFloat(lonText.getText(),
                westButton.getSelection(), false, 0));
        fDeckRecord.setClat(AtcfVizUtil.getStringAsFloat(latText.getText(),
                southButton.getSelection(), false, 0));

        fDeckRecord.setProcess(saveProcess());
        fDeckRecord.setWindMax(AtcfVizUtil
                .getStringAsFloat(maxWindText.getText(), false, false, 0));
        fDeckRecord.setComments(commentCombo.getText());
        fDeckRecord.setInitials(initialsText.getText());
        fDeckRecord.setFixSite(fixSiteCombo.getText());

        fDeckRecord.setPressureConfidence(TabUtility
                .saveConfidence(goodSlpButton, fairSlpButton, poorSlpButton));
        fDeckRecord.setWindMaxConfidence(TabUtility.saveConfidence(
                goodWindButton, fairWindButton, poorWindButton));
        fDeckRecord.setPositionConfidence(TabUtility
                .saveConfidence(goodPosButton, fairPosButton, poorPosButton));

        if (rainButton.getSelection()) {
            fDeckRecord.setRainFlag("R");
        } else {
            fDeckRecord.setRainFlag("");
        }

        fDeckRecord.setTempPassiveMicrowave(AtcfVizUtil
                .getStringAsFloat(tempText.getText(), false, false, 0));

        fDeckRecord.setRainRate(AtcfVizUtil.getStringAsFloat(rainText.getText(),
                false, false, 0));

        fDeckRecord.setEyeDiameterNM(AtcfVizUtil
                .getStringAsFloat(eyeText.getText(), false, false, 0));
        fDeckRecord.setWaveHeight(AtcfVizUtil
                .getStringAsFloat(waveText.getText(), false, false, 0));
        fDeckRecord.setMaxSeas(AtcfVizUtil.getStringAsFloat(seasText.getText(),
                false, false, 0));
        fDeckRecord.setMslp(AtcfVizUtil.getStringAsFloat(slpText.getText(),
                false, false, 0));

    }

}

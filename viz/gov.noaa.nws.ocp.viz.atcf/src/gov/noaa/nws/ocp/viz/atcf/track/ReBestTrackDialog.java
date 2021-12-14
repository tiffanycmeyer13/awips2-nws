/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.locationtech.jts.geom.Coordinate;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.MaxWindGustPairs;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AbstractAtcfRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FixFormat;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.BDeckRecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesDialog;
import gov.noaa.nws.ocp.viz.atcf.fixes.DisplayFixesProperties;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.forecasttrack.FcstTrackProperties;
import gov.noaa.nws.ocp.viz.atcf.handler.ReBestTrackTool;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;
import gov.noaa.nws.ocp.viz.ui.listeners.DtgVerifyListener;

/**
 * Dialog for Re-Best Track GUI.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 09, 2018 45691      wpaintsil   Initial creation.
 * Aug 07, 2018 52657      wpaintsil   Load b-deck data
 * Jul 13, 2020 79573      jwu         Implement most functionalities
 * Nov 10, 2020 84442      wpaintsil   Add scrollbars.
 * Apr 22, 2021 88729      jwu         Revise storm type threshold.
 * May 17, 2021 91567      jwu         Support new DTG with minutes, & various fixes.
 * Jul 16, 2021 93152      jwu         Refresh forecast track, verify new DTG etc.
 *
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class ReBestTrackDialog extends OcpCaveChangeTrackDialog {

    // Number of DTGs to be added before the first DTG and after the latest DTG.
    private static final int DTG_INTERVAL = 6;

    private static final int DTGS_BEFORE = 8;

    private static final int DTGS_AFTER = 12;

    private static final String NEW_DTG_MARKER = "new";

    private static final int DTG_LENGTH = 12;

    private static final String WORKING_DTG_MARKER = "working";

    private static final String WHITE_SPACE_SEPARATOR = "\\s+";

    // Verify listeners for radius of wave quadrant intensity.
    private final AtcfTextListeners verifyListener = new AtcfTextListeners();

    /*
     * Maximum number hours of to create new DTGs before the first DTG and after
     * the latest DTG
     */
    private static final int NEW_DTG_MAX_HOUR = 144;

    // Action buttons.
    private Button saveBtn;

    private Button submitBtn;

    // Current storm, and seleted DTG in the list.
    private Storm storm;

    private String selectedDTG;

    // Full DTG strings shown in the list.
    private String[] fullDTGStrings;

    // Actual DTGs.
    private java.util.List<String> actualDTGs;

    /*
     * First & latest DTG in available best track, DTGs added before first DTG,
     * DTGs added after the latest DTG.
     */
    private String firstDTG;

    private String currentDTG;

    private java.util.List<String> dtgsBefore;

    private java.util.List<String> dtgsAfter;

    private java.util.List<String> bDeckDtgs;

    // Map of B-Deck records.
    private Map<String, java.util.List<BDeckRecord>> currentBDeckRecords;

    private Map<BDeckRecordKey, BDeckRecord> currentBDeckRecordMap;

    private Map<BDeckRecordKey, BDeckRecord> modifiedBDeckRecordMap;

    private Map<BDeckRecordKey, BDeckRecord> deletedBDeckRecordMap;

    private java.util.List<FDeckRecord> fixesRecords;

    // Get the current AtcfResource.
    private AtcfResource drawingLayer;

    private Button trackWindRadBtn;

    private Button trackIntBtn;

    private List dtgList;

    private CCombo windCombo;

    private CCombo developmentCombo;

    private CCombo pressureCombo;

    private CCombo windRadCombo;

    private CCombo outermostCombo;

    private CCombo radOutermostCombo;

    private CCombo[] ktCombos1;

    private CCombo[] ktCombos2;

    private CCombo[] ktCombos3;

    private Label dvorakInfoLbl;

    private List trackInfoList;

    private List imageList;

    // Format to list best track record information.
    private static final String REC_FORMAT = "%-13s%10s%10s%9s%12s%11s%10s%14s";

    // Items for storm development
    private java.util.List<String> developItems;

    // Maximum wind and gust conversion pairs.
    private MaxWindGustPairs gustConversion;

    // Current sandbox ID
    private int sandboxID;

    // Rebest track tool.
    private ReBestTrackTool reBestTrackTool;

    /**
     * Constructor
     *
     * @param parent
     *            Parent shell
     * @param storm
     *            Storm
     * @param bdeckData
     *            Map of BDeckData.
     */
    public ReBestTrackDialog(Shell parent, Storm storm,
            Map<String, java.util.List<BDeckRecord>> bdeckData) {

        super(parent);
        this.storm = storm;
        this.sandboxID = AtcfDataUtil.getBDeckSandbox(storm);
        this.currentBDeckRecords = bdeckData;
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

        initialize();

        createTopComp(mainComp);
        createOptions(mainComp);
        createDataTable(mainComp);
        createControlButtons(mainComp);

        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 0;

        mainComp.setLayout(mainLayout);
        mainComp.setData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Load data and populate.
        populateData();

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
        .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Create the top section.
     *
     * @param parent
     */
    private void createTopComp(Composite parent) {

        Composite dtgImageComp = new Composite(parent, SWT.NONE);
        GridLayout dtgImageLayout = new GridLayout(3, false);
        dtgImageLayout.verticalSpacing = 0;
        dtgImageLayout.marginHeight = 0;
        dtgImageLayout.marginWidth = 20;
        dtgImageComp.setLayout(dtgImageLayout);
        dtgImageComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Label dtgLabel = new Label(dtgImageComp, SWT.NONE);
        dtgLabel.setText("DTG of position");
        GridData dtgLabelData = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgLabelData.horizontalSpan = 2;
        dtgLabel.setLayoutData(dtgLabelData);

        Composite imageSelectComp = new Composite(dtgImageComp, SWT.NONE);
        GridLayout imageSelectLayout = new GridLayout(3, false);
        imageSelectLayout.marginWidth = 0;
        imageSelectLayout.marginHeight = 0;
        imageSelectComp.setLayout(imageSelectLayout);
        imageSelectComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Combo imageCombo = new Combo(imageSelectComp, SWT.NONE);
        imageCombo
        .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button refreshButton = new Button(imageSelectComp, SWT.NONE);
        refreshButton.setText("Refresh");
        refreshButton.setEnabled(false);
        refreshButton.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.CENTER, false, false));

        Composite arrowComp = new Composite(imageSelectComp, SWT.NONE);
        arrowComp.setLayout(new GridLayout(2, true));
        Button leftArrowBtn = new Button(arrowComp, SWT.ARROW | SWT.LEFT);
        leftArrowBtn.setEnabled(false);
        leftArrowBtn.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.CENTER, false, false));
        Button rightArrowBtn = new Button(arrowComp, SWT.ARROW | SWT.RIGHT);
        rightArrowBtn.setEnabled(false);
        rightArrowBtn.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.CENTER, false, false));

        imageSelectComp.setEnabled(false);

        dtgList = new List(dtgImageComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData dtgListData = new GridData(200, 100);
        dtgListData.verticalAlignment = SWT.FILL;
        dtgList.setLayoutData(dtgListData);
        dtgList.setItems(fullDTGStrings);

        int sel = actualDTGs.indexOf(selectedDTG);
        dtgList.setSelection(sel);

        dtgList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                reBestTrackTool.refreshGhost();
                reBestTrackTool.removeTool();

                selectedDTG = actualDTGs.get(dtgList.getSelectionIndex());

                dvorakInfoLbl.setText("");
                String dovarkInfo = getClosestDvorakFix(selectedDTG);
                dvorakInfoLbl.setText(dovarkInfo);
                dvorakInfoLbl.pack();

                populateData();
            }
        });

        Composite dtgButtonComp = new Composite(dtgImageComp, SWT.NONE);
        GridLayout dtgButtonLayout = new GridLayout(1, true);
        dtgButtonLayout.marginRight = 20;
        dtgButtonComp.setLayout(dtgButtonLayout);
        dtgButtonComp.setLayoutData(
                new GridData(SWT.DEFAULT, SWT.TOP, false, false));

        Button deleteBtn = new Button(dtgButtonComp, SWT.PUSH);
        deleteBtn.setText("Delete");
        deleteBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteDTG();
                updateChangeStatus(true);
            }
        });

        Button newDtgBtn = new Button(dtgButtonComp, SWT.PUSH);
        newDtgBtn.setText("New DTG...");
        newDtgBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));
        newDtgBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                NewDtgDialog newDtgDlg = new NewDtgDialog(shell);
                newDtgDlg.open();
            }
        });

        Button reBestBtn = new Button(dtgButtonComp, SWT.PUSH);
        reBestBtn.setText("Re-Best");
        reBestBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));
        reBestBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                reBestTrackTool.reactivateTool();
            }
        });

        Button useObjTrkBtn = new Button(dtgButtonComp, SWT.PUSH);
        useObjTrkBtn.setText("Use Obj-BT");
        useObjTrkBtn.setEnabled(false);
        useObjTrkBtn.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));
        useObjTrkBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO
            }
        });

        imageList = new List(dtgImageComp,
                SWT.NONE | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData imageListData = new GridData(SWT.DEFAULT, 100);
        imageListData.horizontalAlignment = SWT.FILL;
        imageListData.verticalAlignment = SWT.FILL;
        imageList.setLayoutData(imageListData);
    }

    /**
     * Create the middle section.
     *
     * @param parent
     */
    private void createOptions(Composite parent) {

        Composite optionsComp = new Composite(parent, SWT.NONE);
        GridLayout optionsLayout = new GridLayout(2, false);
        optionsLayout.verticalSpacing = 0;
        optionsLayout.marginHeight = 0;
        optionsLayout.marginWidth = 10;
        optionsComp.setLayout(optionsLayout);
        optionsComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite leftOptionsComp = new Composite(optionsComp, SWT.NONE);
        GridLayout leftOptionsLayout = new GridLayout(1, false);
        leftOptionsLayout.verticalSpacing = 0;
        leftOptionsComp.setLayout(leftOptionsLayout);

        Composite windIntensityComp = new Composite(leftOptionsComp, SWT.NONE);
        GridLayout windIntensityLayout = new GridLayout(2, false);
        windIntensityLayout.marginWidth = 0;
        windIntensityLayout.horizontalSpacing = 10;
        windIntensityComp.setLayout(windIntensityLayout);

        Composite windSpeedComp = new Composite(windIntensityComp, SWT.NONE);
        GridLayout windSpeedLayout = new GridLayout(2, false);
        windSpeedLayout.marginHeight = 0;
        windSpeedComp.setLayout(windSpeedLayout);

        Label windLabel = new Label(windSpeedComp, SWT.NONE);
        windLabel.setText("Wind Speed");
        GridData windLabelData = new GridData(SWT.LEFT, SWT.BOTTOM, false,
                false);
        windLabelData.horizontalSpan = 2;
        windLabel.setLayoutData(windLabelData);

        SelectionAdapter selAdaptor = getSelectionAdaptor();

        GridData cmbGD = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
        cmbGD.widthHint = 75;

        windCombo = new CCombo(windSpeedComp, SWT.NONE);
        windCombo.setLayoutData(cmbGD);
        windCombo.setItems(AtcfVizUtil.getMaxWindEntries());
        windCombo.select(0);
        windCombo.addVerifyListener(verifyListener.getWindCmbVerifyListener());
        windCombo.setData(RebestField.MAX_WIND);
        windCombo.addSelectionListener(selAdaptor);
        Label ktLabel = new Label(windSpeedComp, SWT.NONE);
        ktLabel.setText("kt");

        Button intensityTimeBtn = new Button(windIntensityComp, SWT.PUSH);
        intensityTimeBtn.setEnabled(false);
        intensityTimeBtn.setText("Intensity vs Time...");
        intensityTimeBtn.setLayoutData(
                new GridData(SWT.RIGHT, SWT.BOTTOM, false, false));
        Composite developmentComp = new Composite(leftOptionsComp, SWT.NONE);
        developmentComp.setLayout(new GridLayout(1, false));
        Label developmentLabel = new Label(developmentComp, SWT.NONE);
        developmentLabel.setText("Development");
        developmentCombo = new CCombo(developmentComp, SWT.NONE);
        developmentCombo.setItems(StormDevelopment.getStormTypeStrings());
        developmentCombo.select(0);
        developmentCombo.setEditable(false);
        developmentCombo.setData(RebestField.DEVELOPMENT);
        developmentCombo.addSelectionListener(selAdaptor);

        Composite pressureComp = new Composite(leftOptionsComp, SWT.NONE);
        pressureComp.setLayout(new GridLayout(2, false));
        Label pressureLabel = new Label(pressureComp, SWT.NONE);
        pressureLabel.setText("Pressure");
        GridData pressureData = new GridData();
        pressureData.horizontalSpan = 2;
        pressureLabel.setLayoutData(pressureData);
        pressureCombo = new CCombo(pressureComp, SWT.NONE);
        pressureCombo.setLayoutData(cmbGD);
        String[] presEntries = AtcfVizUtil.getEntries(850, 1050, 1, null);
        pressureCombo.setItems(presEntries);
        pressureCombo.select(0);
        pressureCombo.setData(RebestField.MSLP);
        pressureCombo.addSelectionListener(selAdaptor);
        Label pressureMbLabel = new Label(pressureComp, SWT.NONE);
        pressureMbLabel.setText("mb");

        String[] radiiEntries = AtcfVizUtil.getRadiiEntries();

        Composite windRadComp = new Composite(leftOptionsComp, SWT.NONE);
        windRadComp.setLayout(new GridLayout(2, false));
        Label windRadLabel = new Label(windRadComp, SWT.NONE);
        windRadLabel.setText("Max Wind Radius");
        GridData windRadData = new GridData();
        windRadData.horizontalSpan = 2;
        windRadLabel.setLayoutData(windRadData);
        windRadCombo = new CCombo(windRadComp, SWT.NONE);
        windRadCombo.setLayoutData(cmbGD);
        windRadCombo.setItems(radiiEntries);
        windRadCombo.select(0);
        windRadCombo
        .addVerifyListener(verifyListener.getRadiiCmbVerifyListener());
        windRadCombo.setData(RebestField.RMW);
        windRadCombo.addSelectionListener(selAdaptor);
        Label windRadNmLabel = new Label(windRadComp, SWT.NONE);
        windRadNmLabel.setText("nm");

        Composite outermostComp = new Composite(leftOptionsComp, SWT.NONE);
        outermostComp.setLayout(new GridLayout(2, false));
        Label outermostLabel = new Label(outermostComp, SWT.NONE);
        outermostLabel.setText("Outermost Closed Isobar");
        GridData outermostData = new GridData();
        outermostData.horizontalSpan = 2;
        outermostLabel.setLayoutData(outermostData);
        outermostCombo = new CCombo(outermostComp, SWT.NONE);
        outermostCombo.setLayoutData(cmbGD);

        String[] ociEntries = AtcfVizUtil.getEntries(900, 1050, 1, null);
        outermostCombo.setItems(ociEntries);
        outermostCombo.select(0);
        outermostCombo.setData(RebestField.OCI);
        outermostCombo.addSelectionListener(selAdaptor);
        Label outermostMbLabel = new Label(outermostComp, SWT.NONE);
        outermostMbLabel.setText("mb");

        Composite radOutermostComp = new Composite(leftOptionsComp, SWT.NONE);
        radOutermostComp.setLayout(new GridLayout(2, false));
        Label radOutermostLabel = new Label(radOutermostComp, SWT.NONE);
        radOutermostLabel.setText("Radius Outermost Closed Isobar");
        GridData radOutermostData = new GridData();
        radOutermostData.horizontalSpan = 2;
        radOutermostLabel.setLayoutData(radOutermostData);
        radOutermostCombo = new CCombo(radOutermostComp, SWT.NONE);
        radOutermostCombo.setLayoutData(cmbGD);

        radOutermostCombo.setItems(radiiEntries);
        radOutermostCombo.select(0);
        radOutermostCombo
        .addVerifyListener(verifyListener.getRadiiCmbVerifyListener());
        radOutermostCombo.setData(RebestField.ROCI);
        radOutermostCombo.addSelectionListener(selAdaptor);

        Label radOutermostNmLabel = new Label(radOutermostComp, SWT.NONE);
        radOutermostNmLabel.setText("nm");

        Composite rightOptionsComp = new Composite(optionsComp, SWT.NONE);
        GridLayout rightOptionsLayout = new GridLayout(1, false);
        rightOptionsLayout.verticalSpacing = 0;
        rightOptionsComp.setLayout(rightOptionsLayout);
        GridData rightOptionsData = new GridData(SWT.RIGHT, SWT.FILL, true,
                true);
        rightOptionsComp.setLayoutData(rightOptionsData);

        Composite directionComp = new Composite(rightOptionsComp, SWT.NONE);
        GridLayout directionCompLayout = new GridLayout(5, true);
        directionComp.setLayout(directionCompLayout);
        GridData directionData = new GridData(SWT.FILL, SWT.TOP, true, false);
        directionComp.setLayoutData(directionData);

        // empty label for aesthetic spacing
        @SuppressWarnings("unused")
        Label blankLabel1 = new Label(directionComp, SWT.NONE);

        Button objRadButton = new Button(directionComp, SWT.PUSH);
        objRadButton.setText("Use Obj Radii");
        objRadButton.setEnabled(false);
        GridData objRadBtnData = new GridData(SWT.CENTER, SWT.DEFAULT, false,
                false);
        objRadBtnData.horizontalSpan = 4;
        objRadButton.setLayoutData(objRadBtnData);

        // empty label for aesthetic spacing
        @SuppressWarnings("unused")
        Label blankLabel2 = new Label(directionComp, SWT.NONE);

        Label neLabel = new Label(directionComp, SWT.NONE);
        neLabel.setText("NE");
        neLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label seLabel = new Label(directionComp, SWT.NONE);
        seLabel.setText("SE");
        seLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label swLabel = new Label(directionComp, SWT.NONE);
        swLabel.setText("SW");
        swLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        Label nwLabel = new Label(directionComp, SWT.NONE);
        nwLabel.setText("NW");
        nwLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, false, false));

        String[] ktItems = AtcfVizUtil.getRadiiEntries();

        Label ktLabel1 = new Label(directionComp, SWT.NONE);
        ktLabel1.setText("34 kt: ");
        ktLabel1.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        ktCombos1 = new CCombo[4];
        for (int jj = 0; jj < ktCombos1.length; jj++) {
            ktCombos1[jj] = new CCombo(directionComp, SWT.NONE);
            ktCombos1[jj].setItems(ktItems);
            ktCombos1[jj].select(0);

            ktCombos1[jj].setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
            ktCombos1[jj].addVerifyListener(
                    verifyListener.getRadiiCmbVerifyListener());
            ktCombos1[jj].setData(RebestField.RAD_34);
            ktCombos1[jj].addSelectionListener(selAdaptor);
        }

        Label ktLabel2 = new Label(directionComp, SWT.NONE);
        ktLabel2.setText("50 kt: ");
        ktLabel2.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        ktCombos2 = new CCombo[4];
        for (int jj = 0; jj < ktCombos2.length; jj++) {
            ktCombos2[jj] = new CCombo(directionComp, SWT.NONE);
            ktCombos2[jj].setItems(ktItems);
            ktCombos2[jj].select(0);

            ktCombos2[jj].setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
            ktCombos2[jj].addVerifyListener(
                    verifyListener.getRadiiCmbVerifyListener());
            ktCombos2[jj].setData(RebestField.RAD_50);
            ktCombos2[jj].addSelectionListener(selAdaptor);
        }

        Label ktLabel3 = new Label(directionComp, SWT.NONE);
        ktLabel3.setText("64 kt: ");
        ktLabel3.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        ktCombos3 = new CCombo[4];
        for (int jj = 0; jj < ktCombos3.length; jj++) {
            ktCombos3[jj] = new CCombo(directionComp, SWT.NONE);
            ktCombos3[jj].setItems(ktItems);
            ktCombos3[jj].select(0);

            ktCombos3[jj].setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
            ktCombos3[jj].addVerifyListener(
                    verifyListener.getRadiiCmbVerifyListener());
            ktCombos3[jj].setData(RebestField.RAD_64);
            ktCombos3[jj].addSelectionListener(selAdaptor);
        }

        Composite graphRadComp1 = new Composite(rightOptionsComp, SWT.NONE);
        GridLayout graphRadLayout1 = new GridLayout(2, false);
        graphRadLayout1.marginWidth = 0;
        graphRadComp1.setLayout(graphRadLayout1);
        graphRadComp1
        .setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));

        Label graphRadLabel1 = new Label(graphRadComp1, SWT.NONE);
        graphRadLabel1.setText("Graph 34 kt radii: ");

        Composite graphDirBtnComp = new Composite(graphRadComp1, SWT.NONE);
        GridLayout graphDirBtnLayout = new GridLayout(4, true);
        graphDirBtnComp.setLayout(graphDirBtnLayout);
        graphDirBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graphNeButton = new Button(graphDirBtnComp, SWT.PUSH);
        graphNeButton.setEnabled(false);
        graphNeButton.setText("NE...");
        graphNeButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graphSeButton = new Button(graphDirBtnComp, SWT.PUSH);
        graphSeButton.setText("SE...");
        graphSeButton.setEnabled(false);
        graphSeButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graphSwButton = new Button(graphDirBtnComp, SWT.PUSH);
        graphSwButton.setText("SW...");
        graphSwButton.setEnabled(false);
        graphSwButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graphNwButton = new Button(graphDirBtnComp, SWT.PUSH);
        graphNwButton.setText("NW...");
        graphNwButton.setEnabled(false);
        graphNwButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite graphRadComp2 = new Composite(rightOptionsComp, SWT.NONE);
        GridLayout graphRadLayout2 = new GridLayout(2, false);
        graphRadLayout2.marginWidth = 0;
        graphRadComp2.setLayout(graphRadLayout2);
        graphRadComp2
        .setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

        Label graphRadLabel2 = new Label(graphRadComp2, SWT.NONE);
        graphRadLabel2.setText("Graph radii (radial graph): ");

        Composite graphKtBtnComp = new Composite(graphRadComp2, SWT.NONE);
        GridLayout graphKtBtnLayout = new GridLayout(3, true);
        graphKtBtnComp.setLayout(graphKtBtnLayout);
        graphKtBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graph34KtButton = new Button(graphKtBtnComp, SWT.PUSH);
        graph34KtButton.setText("34 kt ...");
        graph34KtButton.setEnabled(false);
        graph34KtButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graph50KtButton = new Button(graphKtBtnComp, SWT.PUSH);
        graph50KtButton.setText("50 kt ...");
        graph50KtButton.setEnabled(false);
        graph50KtButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button graph64KtButton = new Button(graphKtBtnComp, SWT.PUSH);
        graph64KtButton.setText("64 kt ...");
        graph64KtButton.setEnabled(false);
        graph64KtButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Composite trackOptionsComp = new Composite(rightOptionsComp, SWT.NONE);
        GridLayout trackOptionLayout = new GridLayout(2, true);
        trackOptionsComp.setLayout(trackOptionLayout);
        GridData trackOptData = new GridData(SWT.FILL, SWT.BOTTOM, true, true);
        trackOptionsComp.setLayoutData(trackOptData);

        Composite trackChkBxComp = new Composite(trackOptionsComp, SWT.BORDER);
        GridLayout trackChkBxLayout = new GridLayout(2, true);
        trackChkBxComp.setLayout(trackChkBxLayout);

        Label trackOptLabel = new Label(trackChkBxComp, SWT.NONE);
        trackOptLabel.setText("Track Options");
        GridData trackOptLblData = new GridData(SWT.CENTER, SWT.DEFAULT, false,
                false);
        trackOptLblData.horizontalSpan = 2;
        trackOptLabel.setLayoutData(trackOptLblData);

        trackWindRadBtn = new Button(trackChkBxComp, SWT.CHECK);
        trackWindRadBtn.setText("Wind Radii");
        trackWindRadBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                redrawBestTrackIntNRadii();
            }
        });

        trackIntBtn = new Button(trackChkBxComp, SWT.CHECK);
        trackIntBtn.setText("Intensity");
        trackIntBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                redrawBestTrackIntNRadii();
            }
        });

        Button fixOptButton = new Button(trackOptionsComp, SWT.PUSH);
        fixOptButton.setText("Fix Options...");
        GridData fixOptData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
        fixOptData.heightHint = SWT.DEFAULT;
        fixOptButton.setLayoutData(fixOptData);
        fixOptButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayFixes();
            }
        });

    }

    /**
     * Create the table section.
     *
     * @param parent
     */
    private void createDataTable(Composite parent) {
        Composite dvorakComp = new Composite(parent, SWT.NONE);
        GridLayout dvorakLayout = new GridLayout(1, false);
        dvorakLayout.marginWidth = 20;
        dvorakComp.setLayout(dvorakLayout);
        dvorakComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        dvorakInfoLbl = new Label(dvorakComp, SWT.NONE);

        String dovarkInfo = getClosestDvorakFix(selectedDTG);
        dvorakInfoLbl.setText(dovarkInfo);

        Composite tableComp = new Composite(dvorakComp, SWT.BORDER);
        GridLayout tableLayout = new GridLayout(8, true);
        tableLayout.verticalSpacing = 0;
        tableComp.setLayout(tableLayout);
        tableComp
        .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Label dateTimeLabel = new Label(tableComp, SWT.NONE);
        dateTimeLabel.setText("  Date - Time\n\n");
        dateTimeLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label latLabel = new Label(tableComp, SWT.NONE);
        latLabel.setText("     Lat\n\n");
        latLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label lonLabel = new Label(tableComp, SWT.NONE);
        lonLabel.setText("Lon\n\n");
        lonLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label oldSpeedLabel = new Label(tableComp, SWT.NONE);
        oldSpeedLabel.setText("Old\nSpeed\n");
        oldSpeedLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label newSpeedLabel = new Label(tableComp, SWT.NONE);
        newSpeedLabel.setText("New\nSpeed\n");
        newSpeedLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label bestTrkIntLabel = new Label(tableComp, SWT.NONE);
        bestTrkIntLabel.setText("Best Track\nIntensity\n");
        bestTrkIntLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label objTrkIntLabel = new Label(tableComp, SWT.NONE);
        objTrkIntLabel.setText("Obj Track\nIntensity\n");
        objTrkIntLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Label centrPressLabel = new Label(tableComp, SWT.NONE);
        centrPressLabel.setText("Central\nPressure\n");
        centrPressLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        trackInfoList = new List(tableComp, SWT.BORDER | SWT.SINGLE);
        GridData trkInfoGD = new GridData(380, 125);
        trkInfoGD.horizontalAlignment = SWT.FILL;
        trkInfoGD.verticalAlignment = SWT.FILL;
        trkInfoGD.horizontalSpan = 8;
        trackInfoList.setLayoutData(trkInfoGD);

        trackInfoList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
    }

    /**
     * Create bottom buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite bottomComp = new Composite(parent, SWT.NONE);
        GridLayout bottomCompLayout = new GridLayout(2, false);
        bottomCompLayout.marginWidth = 20;
        bottomComp.setLayout(bottomCompLayout);
        bottomComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        Composite buttonComp1 = new Composite(bottomComp, SWT.NONE);
        GridLayout buttonCompLayout1 = new GridLayout(1, false);
        buttonComp1.setLayout(buttonCompLayout1);
        buttonComp1.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        Button qcBtn = new Button(buttonComp1, SWT.PUSH);
        qcBtn.setText("Wind Radii QC Check");
        qcBtn.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, true, false));
        qcBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String radiiQc = windRadiiQCCheck();
                if (!radiiQc.isEmpty()) {
                    MessageDialog.openInformation(shell, "Wind Radii QC Check",
                            radiiQc);
                }
            }
        });

        Composite buttonComp2 = new Composite(bottomComp, SWT.NONE);
        GridLayout buttonCompLayout2 = new GridLayout(4, true);
        buttonComp2.setLayout(buttonCompLayout2);
        buttonComp2.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp2, AtcfVizUtil.buttonGridData());

        saveBtn = new Button(buttonComp2, SWT.PUSH);
        saveBtn.setText("Save");
        saveBtn.setEnabled(true);
        saveBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        saveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                reBestTrackTool.removeTool();

                boolean saved = saveChanges();

                if (saved) {
                    drawingLayer.removeGhost();
                    redrawBestTrack();

                    updateChangeStatus(false);
                    refreshDTGStatus();

                    submitBtn.setEnabled(true);

                    drawForecastTrack();
                }
            }
        });

        submitBtn = new Button(buttonComp2, SWT.PUSH);
        submitBtn.setText("Submit");
        submitBtn.setEnabled(false);
        submitBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        submitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                if (changeListener.isChangesUnsaved()) {
                    saveChanges();
                }

                AtcfDataUtil.checkinBDeckRecords(sandboxID);

                // Reset the sandbox ID in AtcfProduct.
                AtcfProduct prd = drawingLayer.getResourceData()
                        .getAtcfProduct(storm);
                prd.setBdeckSandboxID(-1);

                reBestTrackTool.deactivateTool();

                redrawBestTrack();

                drawForecastTrack();

                close();
            }
        });

        Button cancelBtn = new Button(buttonComp2, SWT.PUSH);
        cancelBtn.setText("Cancel");
        cancelBtn.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /*
     * Initialize data.
     */
    private void initialize() {

        // Sort records by DTG and wind radii.
        currentBDeckRecordMap = new HashMap<>();
        modifiedBDeckRecordMap = new HashMap<>();
        deletedBDeckRecordMap = new HashMap<>();

        prepareBDeckForEditing();

        // Items for storm development
        if (developItems == null) {
            developItems = new ArrayList<>();
            Collections.addAll(developItems,
                    StormDevelopment.getStormShortIntensityStrings());

        }

        // Load gust conversion table
        gustConversion = AtcfConfigurationManager.getInstance()
                .getMaxWindGustPairs();

        // Initialize other info from data.
        firstDTG = bDeckDtgs.get(0);
        currentDTG = bDeckDtgs.get(bDeckDtgs.size() - 1);

        fullDTGStrings = makeDtgList(bDeckDtgs).toArray(new String[0]);

        actualDTGs = new ArrayList<>();
        actualDTGs.addAll(dtgsBefore);
        actualDTGs.addAll(bDeckDtgs);
        actualDTGs.addAll(dtgsAfter);

        selectedDTG = currentDTG;

        setText("Re-Best Track - " + storm.getStormName() + " "
                + storm.getStormId() + " - " + currentDTG.substring(4));

        drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Fixes
        fixesRecords = drawingLayer.getResourceData().getActiveAtcfProduct()
                .getFDeckData();

        if (fixesRecords.isEmpty()) {
            fixesRecords = AtcfDataUtil.getFDeckRecords(storm);
        }
    }

    /*
     * Prepare B-Deck records for editing.
     */
    private void prepareBDeckForEditing() {

        // Get the unique list of DTGs in B-Deck records.
        bDeckDtgs = new ArrayList<>();
        if (!currentBDeckRecords.isEmpty()) {
            bDeckDtgs.addAll(currentBDeckRecords.keySet());
            Collections.sort(bDeckDtgs);
        }

        // Put B-Deck records into a map for easier reference.
        currentBDeckRecordMap.clear();
        for (Map.Entry<String, java.util.List<BDeckRecord>> entry : currentBDeckRecords
                .entrySet()) {
            for (BDeckRecord rec : entry.getValue()) {
                BDeckRecordKey rkey = new BDeckRecordKey(entry.getKey(),
                        (int) rec.getRadWind());
                currentBDeckRecordMap.put(rkey, rec);
            }
        }
    }

    /*
     * Populate the data based on the DTG selection.
     */
    private void populateData() {

        String useDtg = selectedDTG;

        /*
         * Check if this is a new DTG. If so, use the record preceding
         * immediately of the "selectedDTG".
         */
        if (isNewDtg(selectedDTG)) {
            useDtg = getDtgWithOffset(selectedDTG, -1);
        }

        /*
         * Look at modified records first. If not found, use the last-saved
         * ones.
         */
        BDeckRecord rec34 = getWorkingRecord(useDtg,
                WindRadii.RADII_34_KNOT.getValue());

        BDeckRecord rec50 = getWorkingRecord(useDtg,
                WindRadii.RADII_50_KNOT.getValue());

        BDeckRecord rec64 = getWorkingRecord(useDtg,
                WindRadii.RADII_64_KNOT.getValue());

        // Clear off data in the dialog.
        clearData();

        // Update fields.
        if (rec34 != null) {
            windCombo.setText(String.format("%d",
                    (int) getDfltValue(rec34.getWindMax())));

            pressureCombo.setText(
                    String.format("%d", (int) getDfltValue(rec34.getMslp())));

            windRadCombo.setText(String.format("%d",
                    (int) getDfltValue(rec34.getMaxWindRad())));

            outermostCombo.setText(String.format("%d",
                    (int) getDfltValue(rec34.getClosedP())));

            radOutermostCombo.setText(String.format("%d",
                    (int) getDfltValue(rec34.getRadClosedP())));

            developmentCombo.setText(StormDevelopment
                    .getFullIntensityString(rec34.getIntensity()));

            int[] r34 = rec34.getWindRadii();
            for (int ii = 0; ii < 4; ii++) {
                ktCombos1[ii].setEnabled(true);
                ktCombos1[ii].setText(String.format("%d", r34[ii]));
            }

            if (rec50 != null) {
                int[] r50 = rec50.getWindRadii();
                for (int ii = 0; ii < 4; ii++) {
                    ktCombos2[ii].setEnabled(true);
                    ktCombos2[ii].setText(String.format("%d", r50[ii]));
                }

                if (rec64 != null) {
                    int[] r64 = rec64.getWindRadii();
                    for (int ii = 0; ii < 4; ii++) {
                        ktCombos3[ii].setEnabled(true);
                        ktCombos3[ii].setText(String.format("%d", r64[ii]));
                    }
                }
            }
        }

        populateInfoTable();

    }

    /*
     * Clear off when no record exists on the DTG selection.
     */
    private void clearData() {

        if (windCombo != null) {
            windCombo.select(0);
        }

        if (developmentCombo != null) {
            developmentCombo.select(0);
        }

        if (pressureCombo != null) {
            pressureCombo.setText("0");
        }

        if (windRadCombo != null) {
            windRadCombo.select(0);
        }

        if (outermostCombo != null) {
            outermostCombo.setText("0");
        }

        if (radOutermostCombo != null) {
            radOutermostCombo.select(0);
        }

        for (int ii = 0; ii < 4; ii++) {
            ktCombos1[ii].select(0);
            ktCombos2[ii].select(0);
            ktCombos3[ii].select(0);

            ktCombos1[ii].setEnabled(false);
            ktCombos2[ii].setEnabled(false);
            ktCombos3[ii].setEnabled(false);
        }
    }

    /*
     * Make DTGs by adding new DTGs to the beginning & ending DTG of the
     * available best track.
     *
     * @param availableDtgs DTGS available in storm's best track
     *
     * @return java.util.List<String> List of DTGs
     */
    private java.util.List<String> makeDtgList(
            java.util.List<String> bDeckDtgs) {
        java.util.List<String> dtgs = new ArrayList<>();
        dtgsBefore = new ArrayList<>();
        dtgsAfter = new ArrayList<>();

        if (bDeckDtgs != null && !bDeckDtgs.isEmpty()) {
            dtgs.addAll(bDeckDtgs);

            // Add DTGs before the first DTG
            for (int ii = 0; ii < DTGS_BEFORE; ii++) {
                String ndtg = AtcfDataUtil.getNewDTG(firstDTG,
                        -(ii + 1) * DTG_INTERVAL);
                dtgsBefore.add(0, ndtg);
                dtgs.add(0, ndtg + " " + NEW_DTG_MARKER);
            }

            // Add DTGs 72 hours after the last DTG.
            for (int ii = 0; ii < DTGS_AFTER; ii++) {
                String ndtg = AtcfDataUtil.getNewDTG(currentDTG,
                        (ii + 1) * DTG_INTERVAL);
                dtgsAfter.add(ndtg);
                dtgs.add(ndtg + " " + NEW_DTG_MARKER);
            }

        }

        return dtgs;
    }

    /**
     * List info for DTGs 12 hours before and 12 hours after the selected DTG.
     */
    public void populateInfoTable() {

        trackInfoList.removeAll();

        // Find two DTGs before and two DTGs after the selected DTG.
        ArrayList<String> infoDtgList = new ArrayList<>();
        for (int ii = 0; ii < 5; ii++) {
            String idtg = getDtgWithOffset(selectedDTG, ii - 2);
            if (idtg != null) {
                infoDtgList.add(idtg);
            }
        }

        for (String dtg : infoDtgList) {
            trackInfoList.add(getTrackInfoStr(dtg));
        }

        trackInfoList.select(infoDtgList.indexOf(selectedDTG));
    }

    /*
     * Build an information string for a DTG to display.
     *
     * @param dtg DTG string
     *
     * @return string
     */
    private String getTrackInfoStr(String dtg) {

        int radii = WindRadii.RADII_34_KNOT.getValue();
        BDeckRecord rec = getWorkingRecord(dtg, radii);

        String dtgStr = dtg + "Z";
        String latStr = "";
        String lonStr = "";
        String oldSpdStr = "N/A";
        String newSpdStr = "N/A";
        String btIntStr = "N/A";
        String objBtIntStr = "N/A";
        String mslpStr = "N/A";

        if (rec != null) {
            float latValue = rec.getClat();
            latStr = String.format("%.01f",
                    latValue < 0 ? (-1 * latValue) : latValue)
                    + (latValue < 0 ? "S" : "N");

            float lonValue = rec.getClon();
            lonStr = String.format("%.01f",
                    lonValue < 0 ? (-1 * lonValue) : lonValue)
                    + (lonValue < 0 ? "W" : "E");

            // old speed
            int indx = bDeckDtgs.indexOf(dtg);
            BDeckRecord recBf = null;
            String dtgBf = "";
            if (indx > 0) {
                for (int ii = indx - 1; ii >= 0; ii--) {
                    dtgBf = bDeckDtgs.get(ii);
                    recBf = getCurrentRecord(dtgBf, radii);

                    if (recBf != null) {
                        break;
                    }
                }
            }

            BDeckRecord orec = getCurrentRecord(dtg, radii);
            if (recBf != null && orec != null) {
                int speed = (int) AtcfVizUtil.getSpeed(recBf, orec);
                oldSpdStr = String.valueOf(speed);
            }

            // new speed
            BDeckRecord nrec = getModifiedRecord(dtgBf, radii);
            if (nrec == null) {
                nrec = getCurrentRecord(dtgBf, radii);
            }

            if (nrec != null) {
                int speed = (int) AtcfVizUtil.getSpeed(nrec, rec);
                newSpdStr = String.valueOf(speed);
            }

            // Best track intensity
            btIntStr = String.valueOf((int) rec.getWindMax()) + " kts";

            // TODO Objective best track intensity (NHC does not use it for now)

            // central pressure
            float mslp = rec.getMslp();
            if (mslp != AbstractAtcfRecord.RMISSD) {
                mslpStr = String.valueOf(mslp);
            }
        }

        latStr = StringUtils.center(latStr, 10);
        lonStr = StringUtils.center(lonStr, 10);
        oldSpdStr = StringUtils.center(oldSpdStr, 9);
        newSpdStr = StringUtils.center(newSpdStr, 12);
        btIntStr = StringUtils.center(btIntStr, 11);
        objBtIntStr = StringUtils.center(objBtIntStr, 10);
        mslpStr = StringUtils.center(mslpStr, 14);

        return String.format(REC_FORMAT, dtgStr, latStr, lonStr, oldSpdStr,
                newSpdStr, btIntStr, objBtIntStr, mslpStr);

    }

    /*
     * Redraw best track for wind radii and intensity.
     */
    private void redrawBestTrackIntNRadii() {
        BestTrackProperties prop = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getBestTrackProperties();

        // Wind radii
        boolean radii = trackWindRadBtn.getSelection();
        prop.setRadiiFor34Knot(radii);
        prop.setRadiiFor50Knot(radii);
        prop.setRadiiFor64Knot(radii);

        // Intensity
        prop.setTrackIntensities(trackIntBtn.getSelection());

        // Redraw.
        redrawBestTrack();
    }

    /*
     * Redraw best track.
     */
    private void redrawBestTrack() {
        BestTrackProperties prop = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getBestTrackProperties();

        // Create storm track
        BestTrackGenerator btkGen = new BestTrackGenerator(drawingLayer, prop,
                storm);

        btkGen.create();
    }

    /*
     * Opens up DisplayFixesDialog to draw fixes.
     */
    private void displayFixes() {

        DisplayFixesDialog dispFixDlg = DisplayFixesDialog.getInstance(shell);

        // Use the last-used display attributes; if not, use the default one.
        DisplayFixesProperties lastAttr = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getDisplayFixesProperties();

        if (lastAttr != null) {
            dispFixDlg.setDisplayProperties(lastAttr);
        }

        DisplayFixesProperties dispAttr = dispFixDlg.getDisplayProperties();

        // Retrieve all the F-deck records for the current storm
        java.util.List<FDeckRecord> fixesRecs = drawingLayer.getResourceData()
                .getActiveAtcfProduct().getFDeckData();

        if (fixesRecs.isEmpty()) {
            if (fixesRecords.isEmpty()) {
                fixesRecords = AtcfDataUtil.getFDeckRecords(storm);
            }

            if (fixesRecords == null || fixesRecords.isEmpty()) {
                logger.warn("ReBestDialog - No fixes data found for storm "
                        + storm.getStormName() + ".");
                return;
            }

            // Store data to ATCF Resource
            drawingLayer.getResourceData().getActiveAtcfProduct()
            .setFDeckData(fixesRecords);

            // Determine all DTGs appearing in retrieved records
            SortedSet<String> timeSet = AtcfDataUtil
                    .getFDeckDateTimeGroups(fixesRecords);

            dispAttr.setAvailableDateTimeGroups(timeSet.toArray(new String[0]));

            if (!timeSet.isEmpty()) {
                dispAttr.setDateTimeGroup(timeSet.last());
            }
        }

        dispFixDlg.setBlockOnOpen(false);
        dispFixDlg.open();
    }

    /*
     * Finds the closest Dvorak fix and return info with fix time, Dvorak long
     * term trend and max wind, i.e "at 201709101745Z is 5560 ///// 115kts."
     *
     * @return Information for the closest Dvorak fix.
     */
    private String getClosestDvorakFix(String dtg) {

        StringBuilder closestDvorak = new StringBuilder();
        closestDvorak.append("\t\tClosest Dvorak ");

        FDeckRecord closestDvorakFix = null;
        long closestDvorakTime = 0;
        long curTm = AtcfDataUtil.parseDtg(dtg).getTime();

        String subDvorak = FixFormat.SUBJECTIVE_DVORAK.getValue();
        String objDvorak = FixFormat.OBJECTIVE_DVORAK.getValue();
        for (FDeckRecord rec : fixesRecords) {
            String fixFmt = rec.getFixFormat();
            if (subDvorak.equals(fixFmt) || objDvorak.equals(fixFmt)) {
                long tm = rec.getRefTime().getTime();
                if (closestDvorakFix == null) {
                    closestDvorakFix = rec;
                    closestDvorakTime = Math.abs(curTm - tm);
                } else {
                    long tmDiff = Math.abs(curTm - tm);
                    if (tmDiff < closestDvorakTime) {
                        closestDvorakFix = rec;
                        closestDvorakTime = tmDiff;
                    }
                }
            }
        }

        // Add data time, Dvorak code long term trend and max wind.
        if (closestDvorakFix == null) {
            closestDvorak.append("unavailable.");
        } else {
            closestDvorak.append("at ");

            String dt = AtcfDataUtil.calendarToLongDateTimeString(
                    closestDvorakFix.getRefTimeAsCalendar());
            closestDvorak.append(dt.substring(0, 4) + "-" + dt.substring(4, 6)
            + "-" + dt.substring(6, 8) + " " + dt.substring(8, 10) + ":"
            + dt.substring(10) + "Z is ");

            closestDvorak.append(closestDvorakFix.getDvorakCodeLongTermTrend());

            int wnd = (int) closestDvorakFix.getWindMax();
            if (wnd < AbstractAtcfRecord.RMISSD) {
                closestDvorak.append("  " + wnd + " kts");
            }
        }

        return closestDvorak.toString();
    }

    /*
     * Check if wind radii are missing and report.
     *
     * @return Information any missing wind radii.
     */
    private String windRadiiQCCheck() {

        String missingRd = "Missing Wind Radii for: \n\n";

        StringBuilder radiiQc = new StringBuilder();
        for (Map.Entry<String, java.util.List<BDeckRecord>> entry : currentBDeckRecords
                .entrySet()) {
            String dtg = entry.getKey();
            java.util.List<BDeckRecord> brecs = entry.getValue();
            if (brecs != null && !brecs.isEmpty()) {
                for (BDeckRecord rec : brecs) {
                    float maxWnd = rec.getWindMax();
                    int[] radii = rec.getWindRadii();
                    boolean missing = true;
                    if (maxWnd < 34 || radii[0] > 0 || radii[1] > 0
                            || radii[2] > 0 || radii[3] > 0) {
                        missing = false;
                    }

                    if (missing) {
                        radiiQc.append("\n\n\t").append(String.format(
                                "%s-%s-%s %sZ %d kts", dtg.substring(0, 4),
                                dtg.substring(4, 6), dtg.substring(6, 8),
                                dtg.substring(8, 10), (int) rec.getRadWind()));
                    }
                }
            }
        }

        if (radiiQc.length() > 0) {
            missingRd += radiiQc.toString();
        } else {
            missingRd = "No missing wind radii:\n\nAll records have either maximum sustained wind < 34KT or\nat least have radii in one quadrant.";
        }

        return missingRd;
    }

    /*
     * Apply changes in a modified BDeck record to the original BDeck record, if
     * any, in lat, lon, max wind, mslp, max wind radius, storm development,
     * outermost closed isobar, radius of outermost closed isobar, or quadrant
     * winds.
     *
     * Note: for B-Deck, lat/lon should have one 1 decimal precision.
     *
     * @param rec1 original BDeck record
     *
     * @param rec2 modified BDeck record
     *
     * @return boolean changes applied or not.
     *
     */
    private boolean applyChanges(BDeckRecord origRec, BDeckRecord modRec) {
        boolean applied = false;
        if (origRec != null && modRec != null) {
            if ((int) (origRec.getClat() * 10) != (int) (modRec.getClat()
                    * 10)) {
                origRec.setClat(modRec.getClat());
                applied = true;
            }

            if ((int) (origRec.getClon() * 10) != (int) (modRec.getClon()
                    * 10)) {
                origRec.setClon(modRec.getClon());
                applied = true;
            }

            if ((int) origRec.getWindMax() != (int) modRec.getWindMax()) {
                origRec.setWindMax(modRec.getWindMax());
                applied = true;
            }

            if (!(origRec.getIntensity().equals(modRec.getIntensity()))) {
                origRec.setIntensity(modRec.getIntensity());
                applied = true;
            }

            if ((int) origRec.getMaxWindRad() != (int) modRec.getMaxWindRad()) {
                origRec.setMaxWindRad(modRec.getMaxWindRad());
                applied = true;
            }

            if ((int) origRec.getMslp() != (int) modRec.getMslp()) {
                origRec.setMslp(modRec.getMslp());
                applied = true;
            }

            if ((int) origRec.getClosedP() != (int) modRec.getClosedP()) {
                origRec.setClosedP(modRec.getClosedP());
                applied = true;
            }

            if ((int) origRec.getRadClosedP() != (int) modRec.getRadClosedP()) {
                origRec.setRadClosedP(modRec.getRadClosedP());
                applied = true;
            }

            if ((int) origRec.getQuad1WindRad() != (int) modRec
                    .getQuad1WindRad()) {
                origRec.setQuad1WindRad(modRec.getQuad1WindRad());
                applied = true;
            }

            if ((int) origRec.getQuad2WindRad() != (int) modRec
                    .getQuad2WindRad()) {
                origRec.setQuad2WindRad(modRec.getQuad2WindRad());
                applied = true;
            }

            if ((int) origRec.getQuad3WindRad() != (int) modRec
                    .getQuad3WindRad()) {
                origRec.setQuad3WindRad(modRec.getQuad3WindRad());
                applied = true;
            }

            if ((int) origRec.getQuad4WindRad() != (int) modRec
                    .getQuad4WindRad()) {
                origRec.setQuad4WindRad(modRec.getQuad4WindRad());
                applied = true;
            }
        }

        return applied;
    }

    /*
     * Find the value in a CCombo widget - default as 0.
     *
     * @param cmb CCombo
     *
     * @return float
     */
    private float getComboValue(CCombo cmb) {

        String txt = cmb.getText();
        float val = 0.0F;
        try {
            val = Float.valueOf(txt);
        } catch (NumberFormatException ne) {
            // use default 0.
        }

        return val;
    }

    /*
     * Selection listener to update GUI changes into records.
     */
    private SelectionAdapter getSelectionAdaptor() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // Update data.
                boolean updated = updateRecord(
                        (RebestField) e.widget.getData());

                updateChangeStatus(updated);

                populateInfoTable();

                // Perform QC check on wind radii.
                qcWindRadii(e.widget);
            }
        };
    }

    /*
     * Update a BDeckRecord's quadrant wind radii from GUI, if the value is
     * changed.
     *
     * @param mrec BDeckRecord
     *
     * @param ktCombos CCombs to get quadrant values
     *
     * @return Flag to indicate if data is updated.
     */
    private boolean updateQuadrant(BDeckRecord mrec, CCombo[] ktCombos) {
        boolean updated = false;
        int[] radVal = getQuadrant(ktCombos);
        int[] rd = mrec.getWindRadii();
        if (radVal[0] != rd[0] || radVal[1] != rd[1] || radVal[2] != rd[2]
                || radVal[3] != rd[3]) {
            updated = true;
            mrec.setQuad1WindRad(radVal[0]);
            mrec.setQuad2WindRad(radVal[1]);
            mrec.setQuad3WindRad(radVal[2]);
            mrec.setQuad4WindRad(radVal[3]);
        }

        return updated;
    }

    /*
     * Set a BDeckRecord's quadrant wind radii from GUI.
     *
     * @param mrec BDeckRecord
     *
     * @param ktCombos CCombs to get quadrant values
     */
    private void setQuadrant(BDeckRecord rec, CCombo[] ktCombos) {
        int[] radVal = getQuadrant(ktCombos);
        rec.setQuad1WindRad(radVal[0]);
        rec.setQuad2WindRad(radVal[1]);
        rec.setQuad3WindRad(radVal[2]);
        rec.setQuad4WindRad(radVal[3]);
    }

    /*
     * Find a working record by DTG and wind Radii.
     *
     * @param dtg DTG
     *
     * @param radWnd Wind radii (34, 50, 64)
     *
     * @return BDeckRecord
     */
    private BDeckRecord getWorkingRecord(String dtg, int radWnd) {
        BDeckRecord rec = getModifiedRecord(dtg, radWnd);
        if (rec == null) {
            rec = getCurrentRecord(dtg, radWnd);
        }

        return rec;
    }

    /*
     * Find a current record (saved) by DTG and wind Radii.
     *
     * @param dtg DTG
     *
     * @param radWnd Wind radii (34, 50, 64)
     *
     * @return BDeckRecord
     */
    private BDeckRecord getCurrentRecord(String dtg, int radWnd) {
        BDeckRecordKey rkey = new BDeckRecordKey(dtg, radWnd);
        return currentBDeckRecordMap.get(rkey);
    }

    /*
     * Find a modified record (not saved yet) by DTG and wind Radii.
     *
     * @param dtg DTG
     *
     * @param radWnd Wind radii (34, 50, 64)
     *
     * @return BDeckRecord
     */
    private BDeckRecord getModifiedRecord(String dtg, int radWnd) {
        BDeckRecordKey rkey = new BDeckRecordKey(dtg, radWnd);
        return modifiedBDeckRecordMap.get(rkey);
    }

    /*
     * Find a deleted record (not saved yet) by DTG and wind Radii.
     *
     * @param dtg DTG
     *
     * @param radWnd Wind radii (34, 50, 64)
     *
     * @return BDeckRecord
     */
    private BDeckRecord getDeletedRecord(String dtg, int radWnd) {
        BDeckRecordKey rkey = new BDeckRecordKey(dtg, radWnd);
        return deletedBDeckRecordMap.get(rkey);
    }

    /*
     * Update B-Deck records' max wind and intensity for currently-selected DTG.
     */
    private boolean updateMaxWind() {

        boolean updated = false;

        // Max wind & intensity based on wind speed
        float wnd = getComboValue(windCombo);

        String dvp = developmentCombo
                .getItem(developmentCombo.getSelectionIndex());
        String iniType = StormDevelopment.getShortIntensityString(dvp);

        String develop = StormDevelopment.getIntensity(iniType, wnd);
        int indx = developItems.indexOf(develop);
        if (indx >= 0) {
            developmentCombo.select(indx);
        }

        int gust = gustConversion.getGustValue((int) wnd);

        int r34 = WindRadii.RADII_34_KNOT.getValue();
        int r50 = WindRadii.RADII_50_KNOT.getValue();
        int r64 = WindRadii.RADII_64_KNOT.getValue();

        // Only new and modified records are stored in modifiedBDeckRecordMap.
        BDeckRecord mrec34 = getModifiedRecord(selectedDTG, r34);
        BDeckRecord brec34 = getCurrentRecord(selectedDTG, r34);

        // Copy the original record for editing - note "id" is not copied.
        boolean addForModify = false;
        if (mrec34 == null && brec34 != null) {
            mrec34 = new BDeckRecord(brec34);
            addForModify = true;
        }

        // If no record found for 34 kts, no need to update.
        if (mrec34 == null) {
            return updated;
        }

        // If no wind changes, no need to update.
        int origWnd = (int) mrec34.getWindMax();
        if (wnd == origWnd) {
            return updated;
        }

        // Update now.
        updated = true;

        if (addForModify) {
            BDeckRecordKey rkey = new BDeckRecordKey(selectedDTG, r34);
            modifiedBDeckRecordMap.put(rkey, mrec34);
        }

        /*
         * Records exist, update ....
         */
        mrec34.setWindMax(wnd);
        mrec34.setIntensity(develop);
        mrec34.setGust(gust);

        if (origWnd >= r50 && wnd >= r50) {
            updateRecord(r50, (int) wnd, develop, gust);
        }

        if (origWnd >= r64 && wnd >= r64) {
            updateRecord(r64, (int) wnd, develop, gust);
        }

        /*
         * Wind increased over a threshold, create new records.
         */
        if (origWnd < r50 && wnd >= r50) {
            addRecord(r50, (int) wnd, develop, gust, mrec34);
        }

        if (origWnd < r64 && wnd >= r64) {
            addRecord(r64, (int) wnd, develop, gust, mrec34);
        }

        /*
         * Wind decreased over a threshold, delete records.
         */
        if (origWnd >= r50 && wnd < r50) {
            removeRecord(r50);
        }

        if (origWnd >= r64 && wnd < 64) {
            removeRecord(r64);
        }

        return updated;
    }

    /*
     * Update B-Deck records' max wind and intensity for given wind radii &
     * currently selected DTG.
     *
     * @param radii Radii (34/50/64 knots).
     *
     * @param wnd Max wnd
     *
     * @param develop Intensity string
     *
     * @param gust Gust
     *
     * @return boolean Flag to indicate if any update happened.
     */
    private boolean updateRecord(int radii, int wnd, String develop, int gust) {

        boolean update = false;

        BDeckRecord mrec = getModifiedRecord(selectedDTG, radii);
        BDeckRecord brec = getCurrentRecord(selectedDTG, radii);

        // Copy the original record for editing - note "id" is not copied.
        if (mrec == null && brec != null) {
            mrec = new BDeckRecord(brec);
            BDeckRecordKey rkey = new BDeckRecordKey(selectedDTG, radii);
            modifiedBDeckRecordMap.put(rkey, mrec);
        }

        if (mrec != null) {
            mrec.setWindMax(wnd);
            mrec.setIntensity(develop);
            mrec.setGust(gust);

            update = true;
        }

        return update;
    }

    /*
     * Add a B-Deck records with given max wind, intensity, wind radii for
     * currently selected DTG.
     *
     * @param radii Radii (34/50/64 knots).
     *
     * @param wnd Max wnd
     *
     * @param develop Intensity string
     *
     * @param gust Gust
     *
     * @param base BDeckRecord to copy from
     *
     * @return boolean Flag to indicate if records added.
     */
    private boolean addRecord(int radii, int wnd, String develop, int gust,
            BDeckRecord base) {

        boolean update = true;

        BDeckRecord mrec = getDeletedRecord(selectedDTG, radii);
        BDeckRecordKey rkey = new BDeckRecordKey(selectedDTG, radii);
        if (mrec != null) {
            deletedBDeckRecordMap.remove(rkey);
        } else {
            mrec = new BDeckRecord(base);
            mrec.setRadWind(radii);
            mrec.setQuad1WindRad(0);
            mrec.setQuad2WindRad(0);
            mrec.setQuad3WindRad(0);
            mrec.setQuad4WindRad(0);
        }

        mrec.setWindMax(wnd);
        mrec.setIntensity(develop);
        mrec.setGust(gust);

        modifiedBDeckRecordMap.put(rkey, mrec);

        setRadiiStatus(radii, mrec);

        return update;

    }

    /*
     * Remove a B-Deck record for given wind radii & currently selected DTG.
     *
     * @param radii Radii (34/50/64 knots).
     *
     * @return boolean Flag to indicate if record is removed.
     */
    private boolean removeRecord(int radii) {

        boolean update = false;

        BDeckRecordKey rkey = new BDeckRecordKey(selectedDTG, radii);
        BDeckRecord mrec = modifiedBDeckRecordMap.get(rkey);
        if (mrec != null) {
            modifiedBDeckRecordMap.remove(rkey);
            update = true;
        }

        BDeckRecord rec = currentBDeckRecordMap.get(rkey);
        if (rec != null) {
            deletedBDeckRecordMap.put(rkey, rec);
            update = true;
        }

        setRadiiStatus(radii, null);

        return update;
    }

    /*
     * Update the wind radii status for given wind radii & currently-selected
     * DTG.
     *
     * @param radii Radii (34/50/64 knots).
     *
     * @param rec BDeckRecord.
     *
     */
    private void setRadiiStatus(int radii, BDeckRecord rec) {
        CCombo[] ktCombos = null;
        if (radii == WindRadii.RADII_34_KNOT.getValue()) {
            ktCombos = ktCombos1;
        } else if (radii == WindRadii.RADII_50_KNOT.getValue()) {
            ktCombos = ktCombos2;
        } else if (radii == WindRadii.RADII_64_KNOT.getValue()) {
            ktCombos = ktCombos3;
        }

        if (ktCombos != null) {
            for (CCombo cmb : ktCombos) {
                cmb.setEnabled(rec != null);
            }

            int[] rd = new int[] { 0, 0, 0, 0 };
            if (rec != null) {
                rd = rec.getWindRadii();
            }

            ktCombos[0].setText(String.format("%d", rd[0]));
            ktCombos[1].setText(String.format("%d", rd[1]));
            ktCombos[2].setText(String.format("%d", rd[1]));
            ktCombos[3].setText(String.format("%d", rd[3]));
        }
    }

    /*
     * Update B-Deck records for currently-selected DTG.
     *
     * @param field BDeck field to be updated.
     *
     * @return boolean Flag to indicate if any update happened.
     */
    private boolean updateRecord(RebestField field) {

        boolean updated = false;

        // Update records in modifiedBDeckRecordMap.
        if (field == RebestField.MAX_WIND) {
            updated = updateMaxWind();
        } else if (field == RebestField.RAD_34) {
            updated = updateRecord(WindRadii.RADII_34_KNOT, field);
        } else if (field == RebestField.RAD_50) {
            updated = updateRecord(WindRadii.RADII_50_KNOT, field);
        } else if (field == RebestField.RAD_64) {
            updated = updateRecord(WindRadii.RADII_64_KNOT, field);
        } else {
            updated = updateRecord(WindRadii.RADII_34_KNOT, field);
            if (updated) {
                boolean updated50 = updateRecord(WindRadii.RADII_50_KNOT,
                        field);

                if (updated50) {
                    updateRecord(WindRadii.RADII_64_KNOT, field);
                }
            }
        }

        return updated;
    }

    /*
     * Update B-Deck records' maximum wind and intensity for currently-selected
     * DTG and a given wind radii.
     *
     * @param radii WindRadii
     *
     * @param maxWnd New maximum wind
     *
     * @param develop Intensity string
     *
     * @param gust Wind gust.
     *
     * @return true - record(s) updated, false - no records updated.
     */
    private boolean updateRecord(WindRadii radii, RebestField field) {

        boolean updated = false;
        boolean addForModify = false;

        // Only new and modified records are stored in modifiedBDeckRecordMap.
        BDeckRecord mrec = getModifiedRecord(selectedDTG, radii.getValue());
        BDeckRecord brec = getCurrentRecord(selectedDTG, radii.getValue());

        // Copy the original record for editing - note "id" is not copied.
        if (mrec == null && brec != null) {
            mrec = new BDeckRecord(brec);
            addForModify = true;
        }

        if (mrec == null) {
            return updated;
        }

        switch (field) {
        case MAX_WIND:
            // Handled separately.
            break;
        case DEVELOPMENT:
            String dvlp = developmentCombo
            .getItem(developmentCombo.getSelectionIndex());
            String intensity = StormDevelopment.getShortIntensityString(dvlp);
            if (!intensity.equals(mrec.getIntensity())) {
                updated = true;
                mrec.setIntensity(intensity);
            }
            break;

        case MSLP:
            int pres = (int) getComboValue(pressureCombo);
            if (pres != (int) mrec.getMslp()) {
                updated = true;
                mrec.setMslp(pres);
            }
            break;

        case RMW:
            int rmw = (int) getComboValue(windRadCombo);
            if (rmw != (int) mrec.getMaxWindRad()) {
                updated = true;
                mrec.setMaxWindRad(rmw);
            }
            break;

        case OCI:
            int oci = (int) getComboValue(outermostCombo);
            if (oci != (int) mrec.getClosedP()) {
                updated = true;
                mrec.setClosedP(oci);
            }
            break;

        case ROCI:
            int roci = (int) getComboValue(radOutermostCombo);
            if (roci != (int) mrec.getRadClosedP()) {
                updated = true;
                mrec.setRadClosedP(roci);
            }
            break;

        case RAD_34:
            if (radii.getValue() == WindRadii.RADII_34_KNOT.getValue()) {
                updated = updateQuadrant(mrec, ktCombos1);
            }
            break;

        case RAD_50:
            if (radii.getValue() == WindRadii.RADII_50_KNOT.getValue()) {
                updated = updateQuadrant(mrec, ktCombos2);
            }
            break;

        case RAD_64:
            if (radii.getValue() == WindRadii.RADII_64_KNOT.getValue()) {
                updated = updateQuadrant(mrec, ktCombos3);
            }
            break;
        }

        if (addForModify && updated) {
            BDeckRecordKey rkey = new BDeckRecordKey(selectedDTG,
                    radii.getValue());
            modifiedBDeckRecordMap.put(rkey, mrec);
        }

        return updated;
    }

    /*
     * Delete currently selected DTG and its records.
     */
    private void deleteDTG() {

        // Ask for user confirmation.
        MessageDialog confirmDlg = new MessageDialog(shell,
                "Delete best track for DTG: " + selectedDTG, null,
                "Are you sure you want to delete the best track record for "
                        + selectedDTG + "?\n Existing records will be lost.",
                        MessageDialog.CONFIRM, new String[] { "Yes", "No" }, 1);
        confirmDlg.open();

        if (confirmDlg.getReturnCode() == Window.OK) {
            /*
             * Delete from modified record map & store into deleted record map.
             * At "Save", records in deleted record map will be sent in as
             * records to be deleted.
             */
            if (bDeckDtgs.contains(selectedDTG)) {
                for (WindRadii radii : WindRadii.values()) {
                    if (radii.getValue() > 0) {
                        BDeckRecordKey rkey = new BDeckRecordKey(selectedDTG,
                                radii.getValue());
                        BDeckRecord mrec = modifiedBDeckRecordMap.get(rkey);
                        if (mrec != null) {
                            modifiedBDeckRecordMap.remove(rkey);
                        }

                        BDeckRecord rec = currentBDeckRecordMap.get(rkey);
                        if (rec != null) {
                            deletedBDeckRecordMap.put(rkey, rec);
                        }
                    }
                }
            }

            // Update DTG list.
            int ind = dtgList.getSelectionIndex();

            dtgList.remove(ind);
            fullDTGStrings = (String[]) ArrayUtils.remove(fullDTGStrings, ind);
            bDeckDtgs.remove(selectedDTG);
            actualDTGs.remove(selectedDTG);

            int sel = Math.min(ind, actualDTGs.size() - 1);
            selectedDTG = actualDTGs.get(sel);

            dtgList.setSelection(new String[] { dtgList.getItems()[sel] });

            populateData();
        }
    }

    /*
     * Save new and changed records into sandbox.
     */
    private boolean saveChanges() {

        java.util.List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
        boolean successfulSave = true;

        for (Map.Entry<BDeckRecordKey, BDeckRecord> entry : modifiedBDeckRecordMap
                .entrySet()) {

            BDeckRecord srec = entry.getValue();
            BDeckRecord erec = currentBDeckRecordMap.get(entry.getKey());

            /*
             * editType is for each modified record and should be set when
             * making edit action on the record. A record in
             * modifiedBDeckRecordMap but not in currentBDeckRecordMap is a NEW
             * record. We can try to check ID for record as well?
             */
            RecordEditType editType = RecordEditType.MODIFY;
            if (srec != null && erec == null) {
                editType = RecordEditType.NEW;
            }

            /*
             * Save changed records - isRecordChanged() is an extra check to
             * guard against the user change the value back and forth and
             * returns to the original value.
             */
            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            if (editType == RecordEditType.NEW) {
                mdr.setRecord(srec);
                modifiedRecords.add(mdr);
            } else {
                boolean changed = applyChanges(erec, srec);
                if (changed) {
                    mdr.setRecord(erec);
                    modifiedRecords.add(mdr);
                }
            }
        }

        // Delete records
        deletedBDeckRecordMap.values().forEach(brec -> {
            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(RecordEditType.DELETE);
            mdr.setRecord(brec);
            modifiedRecords.add(mdr);
        });

        // batch update into the sandbox
        if (!modifiedRecords.isEmpty()) {
            successfulSave = AtcfDataUtil.updateDeckRecords(sandboxID,
                    AtcfDeckType.B, modifiedRecords);
        }

        if (successfulSave) {
            /*
             * Refresh the records - this helps to get "id"s back for new
             * records, which is generated by DB.
             */
            AtcfProduct atcfPrd = drawingLayer.getResourceData()
                    .getAtcfProduct(storm);

            atcfPrd.getBDeckDataMap().clear();

            currentBDeckRecords.clear();
            currentBDeckRecords
            .putAll(AtcfDataUtil.getBDeckRecords(storm, true));

            atcfPrd.setBDeckData(currentBDeckRecords);

            prepareBDeckForEditing();

            // Clear maps for any new editing.
            modifiedBDeckRecordMap.clear();
            deletedBDeckRecordMap.clear();
        } else {
            logger.error(
                    "ReBestTrackDialog: Best track data was not saved successfully.");
        }

        return successfulSave;
    }

    /**
     * Update flags and save button to indicate changes.
     *
     * @param changed
     *            True/False
     */
    public void updateChangeStatus(boolean changed) {
        changeListener.setChangesUnsaved(changed);
        saveBtn.setEnabled(changed);
    }

    /**
     * Update the DTG status.
     *
     * @param dtg
     *            DTG
     */
    public void updateDTGStatus(String dtg) {
        int index = actualDTGs.indexOf(dtg);
        String dtgItem = fullDTGStrings[index];

        // DTG could be yyyyMMddHH or yyyyMMddHHmm
        String dtgStr = dtgItem.split(WHITE_SPACE_SEPARATOR)[0];

        if (index >= dtgsBefore.size()
                && index < (dtgsBefore.size() + bDeckDtgs.size())) {
            dtgStr += (" " + WORKING_DTG_MARKER);
        }

        dtgList.setItem(index, dtgStr);
    }

    /**
     * Refresh the string shown in DTG list.
     */
    private void refreshDTGStatus() {
        String[] dtgItems = dtgList.getItems();
        for (int ii = 0; ii < dtgItems.length; ii++) {
            String item = dtgItems[ii];
            if (item.contains(WORKING_DTG_MARKER)) {
                String itemStr = item.split(WHITE_SPACE_SEPARATOR)[0];
                dtgList.setItem(ii, itemStr);
                fullDTGStrings[ii] = itemStr;
            }
        }
    }

    /**
     * If unsaved changes exist, prompt user confirmation on closing. If no
     * unsaved changes, proceed with closing without prompt.
     */
    @Override
    public boolean shouldClose() {
        boolean close = true;
        if (changeListener.isChangesUnsaved()) {
            MessageDialog confirmDlg = new MessageDialog(getShell(),
                    "ReBestTrackDialog - Unsaved Changes", null,
                    "Close this window? Unsaved changes will be lost.",
                    MessageDialog.CONFIRM, new String[] { "Yes", "No" }, 1);
            confirmDlg.open();

            if (confirmDlg.getReturnCode() == Window.OK) {
                changeListener.setChangesUnsaved(false);
                drawingLayer.removeGhost();
                reBestTrackTool.deactivateTool();
                redrawBestTrack();
            } else {
                close = false;
            }
        }

        return close;
    }

    /**
     * Show lat/lon on top-right
     */
    public void showLatLon(Coordinate loc) {
        imageList.removeAll();
        if (loc != null) {
            imageList.add(
                    StringUtils.center("Right mouse click to cancel.", 50));
            imageList.add(StringUtils
                    .center(String.format("DTG: %12s", selectedDTG), 50));
            String locStr = StringUtils
                    .center(String.format("Location: %5.1f%2s, %6.1f%2s",
                            Math.abs(loc.y), (loc.y < 0) ? "S" : "N",
                                    Math.abs(loc.x), (loc.x > 0) ? "E" : "W"), 50);
            imageList.add(locStr);
        }
    }

    /**
     * Creates new BDeckRecords for a given DTG, filled with all values from
     * this dialog. The number of records created depends on the max wind
     * selected on GUI.
     *
     * @param dtg
     *            DTG
     *
     * @return java.util.List<BDeckRecord>
     */
    public java.util.List<BDeckRecord> createNewRecords(String dtg) {

        java.util.List<BDeckRecord> recs = new ArrayList<>();

        BDeckRecord orec = currentBDeckRecordMap.values().iterator().next();
        BDeckRecord mrec = new BDeckRecord(orec);

        mrec.setRadWind(WindRadii.RADII_34_KNOT.getValue());
        mrec.setRefTime(AtcfDataUtil.parseDtg(dtg));

        recs.add(mrec);

        float wnd = getComboValue(windCombo);
        mrec.setWindMax(wnd);

        int gust = gustConversion.getGustValue((int) wnd);
        mrec.setGust(gust);

        String dvlp = developmentCombo
                .getItem(developmentCombo.getSelectionIndex());
        String intensity = StormDevelopment.getShortIntensityString(dvlp);
        mrec.setIntensity(intensity);

        int pres = (int) getComboValue(pressureCombo);
        mrec.setMslp(pres);

        int rmw = (int) getComboValue(windRadCombo);
        mrec.setMaxWindRad(rmw);

        int oci = (int) getComboValue(outermostCombo);
        mrec.setClosedP(oci);

        int roci = (int) getComboValue(radOutermostCombo);
        mrec.setRadClosedP(roci);

        setQuadrant(mrec, ktCombos1);

        // Add record for 50 Kt
        if (wnd >= WindRadii.RADII_50_KNOT.getValue()) {
            BDeckRecord rec50 = new BDeckRecord(mrec);
            rec50.setRadWind(WindRadii.RADII_50_KNOT.getValue());
            setQuadrant(rec50, ktCombos2);

            recs.add(rec50);

            // Add record for 64 Kt
            if (wnd >= WindRadii.RADII_64_KNOT.getValue()) {
                BDeckRecord rec64 = new BDeckRecord(mrec);
                rec64.setRadWind(WindRadii.RADII_64_KNOT.getValue());
                setQuadrant(rec64, ktCombos3);

                recs.add(rec64);
            }
        }

        return recs;
    }

    /**
     * Deactivate ForecastTrackTool.
     */
    public void deactivateTool() {
        if (reBestTrackTool != null) {
            reBestTrackTool.deactivateTool();
        }
    }

    /**
     * @return the selectedDTG
     */
    public String getSelectedDTG() {
        return selectedDTG;
    }

    /**
     * @return the fullDTGStrings
     */
    public String[] getFullDTGStrings() {
        return fullDTGStrings;
    }

    /**
     * @return the actualDTGs
     */
    public java.util.List<String> getActualDTGs() {
        return actualDTGs;
    }

    /**
     * @return the currentBDeckRecordMap
     */
    public Map<BDeckRecordKey, BDeckRecord> getCurrentBDeckRecordMap() {
        return currentBDeckRecordMap;
    }

    /**
     * @return the modifiedBDeckRecordMap
     */
    public Map<BDeckRecordKey, BDeckRecord> getModifiedBDeckRecordMap() {
        return modifiedBDeckRecordMap;
    }

    public Map<BDeckRecordKey, BDeckRecord> getDeletedBDeckRecordMap() {
        return deletedBDeckRecordMap;
    }

    /**
     * @return the reBestTrackTool
     */
    public ReBestTrackTool getReBestTrackTool() {
        return reBestTrackTool;
    }

    /**
     * @param reBestTrackTool
     *            the reBestTrackTool to set
     */
    public void setReBestTrackTool(ReBestTrackTool reBestTrackTool) {
        this.reBestTrackTool = reBestTrackTool;
    }

    /**
     * Dialog for entering a new DTG.
     */
    private class NewDtgDialog extends OcpCaveChangeTrackDialog {

        private long minDtg;

        private long maxDtg;

        /**
         * Constructor
         *
         * @param parent
         *            parent shell
         * @param advData
         *            Advisory data
         */
        public NewDtgDialog(Shell parent) {
            // Use an SWT.PRIMARY_MODAL dialog.
            super(parent, OCP_DIALOG_MODAL_STYLE, OCP_DIALOG_MODAL_CAVE_STYLE);

        }

        /**
         * Initializes the components.
         *
         * @param shell
         */
        @Override
        protected void initializeComponents(Shell shell) {
            Composite mainComp = new Composite(shell, SWT.NONE);
            GridLayout mainCompGL = new GridLayout(1, false);
            mainCompGL.verticalSpacing = 15;
            mainComp.setLayout(mainCompGL);
            GridData mainGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
            mainComp.setLayoutData(mainGD);

            Label lbl = new Label(mainComp, SWT.CENTER);
            lbl.setText("Enter a new DTG");
            GridData mainLblGD = new GridData(SWT.CENTER, SWT.NONE, false,
                    false);
            lbl.setLayoutData(mainLblGD);

            Text dtgTxt = new Text(mainComp, SWT.BORDER);
            GridData dtgTxtGD = new GridData(SWT.FILL, SWT.CENTER, false,
                    false);
            dtgTxtGD.minimumWidth = 60;
            dtgTxt.setLayoutData(dtgTxtGD);
            dtgTxt.setTextLimit(DTG_LENGTH);
            dtgTxt.setToolTipText("YYYYMMDDHHmm and mm is optional");

            /*
             * Limit new DTG to NEW_DTG_MAX_HOUR before first DTG & after
             * current DTG.
             */
            String minDtgStr = AtcfDataUtil.getNewDTG(firstDTG,
                    -NEW_DTG_MAX_HOUR) + "00";
            minDtg = Long.valueOf(minDtgStr);
            String maxDtgStr = AtcfDataUtil.getNewDTG(currentDTG,
                    NEW_DTG_MAX_HOUR) + "00";
            maxDtg = Long.valueOf(maxDtgStr);

            Date sDate = AtcfDataUtil.parseDtg(minDtgStr);
            Date eDate = AtcfDataUtil.parseDtg(maxDtgStr);

            DtgVerifyListener dtgVerifyListener = new DtgVerifyListener(
                    actualDTGs, sDate, eDate);
            dtgTxt.addListener(SWT.Verify, dtgVerifyListener);

            GridLayout ctrlBtnCompGL = new GridLayout(2, true);
            ctrlBtnCompGL.marginTop = 0;

            Composite ctrlBtnComp = new Composite(mainComp, SWT.NONE);
            ctrlBtnComp.setLayout(ctrlBtnCompGL);
            ctrlBtnComp.setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, false, false));

            Button okBtn = new Button(ctrlBtnComp, SWT.NONE);
            okBtn.setText("Ok");
            okBtn.setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
            okBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    String dtgStr = dtgTxt.getText();
                    String msg = verifyDtg(dtgTxt.getText());

                    if (msg != null && msg.length() > 0) {
                        MessageDialog.openWarning(shell, "New DTG", msg);
                    } else {
                        addNewDtg(getCleanDtgStr(dtgStr));
                        close();
                    }
                }
            });

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

        /*
         * Verify if the input new DTG is valid and in range.
         *
         * @param dtgStr new DTG to be verified
         *
         * @param String Validation message (null means valid).
         */
        private String verifyDtg(String dtgStr) {
            String msg = null;

            if (dtgStr.length() < 10 || dtgStr.length() == (DTG_LENGTH - 1)) {
                msg = "Incomplete DTG - should be in format of yyyyMMddHH or yyyyMMddHHmm";
            } else {
                dtgStr = getCleanDtgStr(dtgStr);

                if (actualDTGs.contains(dtgStr)) {
                    msg = "This is not a new DTG.";
                } else {
                    int year = Integer.parseInt(dtgStr.substring(0, 4));
                    int month = Integer.parseInt(dtgStr.substring(4, 6));
                    int day = Integer.parseInt(dtgStr.substring(6, 8));
                    int hour = Integer.parseInt(dtgStr.substring(8, 10));

                    int stormYr = storm.getYear();
                    String subMsg = "";
                    if (year < (stormYr - 1) || year > (stormYr + 1)) {
                        subMsg = "year";
                    }

                    if (month < 1 && month > 12) {
                        subMsg += (subMsg.length() > 0) ? "/month" : "month";
                    }

                    if (day < 0 && month > 31) {
                        subMsg += (subMsg.length() > 0) ? "/day" : "day";
                    }

                    // Allow intermediate hours with minutes.
                    if ((hour < 0 || hour > 24)) {
                        subMsg += (subMsg.length() > 0) ? "/hour" : "hour";
                    }

                    int minute = 0;
                    if (dtgStr.length() == DTG_LENGTH) {
                        minute = Integer.valueOf(dtgStr.substring(10));
                        if (minute < 0 || minute >= 60) {
                            subMsg += (subMsg.length() > 0) ? "/minute"
                                    : "minute";
                        }
                    }

                    if (subMsg.length() > 0) {
                        msg = "Invalid " + subMsg + ".";
                    } else {
                        msg = subMsg;
                        Long dtgVal = Long.parseLong(dtgStr);
                        if (dtgStr.length() == 10) {
                            dtgVal *= 100;
                        } else if (dtgStr.length() == 11) {
                            dtgVal *= 10;
                        }

                        if (dtgVal < minDtg) {
                            msg += "New DTG is " + NEW_DTG_MAX_HOUR
                                    + " hours before " + firstDTG;
                        } else if (dtgVal > maxDtg) {
                            msg += "New DTG is " + NEW_DTG_MAX_HOUR
                                    + " hours after " + currentDTG;
                        }
                    }
                }
            }

            return msg;
        }

        /*
         * Removing ending "00" if the DTG length is 12 (having minutes).
         *
         * @param String dtgStr
         *
         * @return String dtg
         */
        private String getCleanDtgStr(String dtgStr) {

            if (dtgStr != null && dtgStr.length() == DTG_LENGTH
                    && dtgStr.endsWith("00")) {
                return dtgStr.substring(0, 10);
            }

            return dtgStr;
        }

        /*
         * Add a new DTG to DTG list and update selection.
         *
         * @param dtgStr new DTG to be verified
         *
         */
        private void addNewDtg(String dtgStr) {
            dtgList.removeAll();
            java.util.List<String> newDtgs = new ArrayList<>();
            newDtgs.addAll(Arrays.asList(fullDTGStrings));
            String ndtg = dtgStr + " " + NEW_DTG_MARKER;
            newDtgs.add(ndtg);
            Collections.sort(newDtgs);

            actualDTGs.add(dtgStr);
            Collections.sort(actualDTGs);

            bDeckDtgs.add(dtgStr);
            Collections.sort(bDeckDtgs);

            dtgList.setItems(newDtgs.toArray(new String[] {}));
            dtgList.setSelection(new String[] { ndtg });
            fullDTGStrings = dtgList.getItems();

            selectedDTG = dtgStr;

            populateData();
        }

    }

    /**
     * Class to represent 9 B-Deck record fields that could be modified on this
     * dialog.
     */
    private enum RebestField {
        MAX_WIND, DEVELOPMENT, MSLP, RMW, OCI, ROCI, RAD_34, RAD_50, RAD_64;
    }

    /*
     * Quality check for wind radii.
     *
     * a) At least one quadrant should have radii when intensity threshold
     * reached (34/50/64KT)
     *
     * b) Radii in each quadrant should be 34kt >50kt>64kt.
     *
     * c) Open up a message box to ask the user to correct.
     *
     * @param widget The wind radii CCombo selected
     *
     * @return boolean Flag to indicate if the new wind radii is valid.
     */
    private boolean qcWindRadii(Widget widget) {

        boolean validWndRadii = true;

        RebestField field = (RebestField) widget.getData();
        StringBuilder sbd = new StringBuilder();

        if (field == RebestField.MAX_WIND || field == RebestField.RAD_34
                || field == RebestField.RAD_50 || field == RebestField.RAD_64) {

            // Enable radii comboes based on max. wind.
            float wnd = getComboValue(windCombo);

            if (field == RebestField.MAX_WIND) {
                for (int ii = 0; ii < 4; ii++) {
                    ktCombos1[ii].setEnabled(wnd > 0);
                    ktCombos2[ii].setEnabled(wnd >= 50);
                    ktCombos3[ii].setEnabled(wnd >= 64);
                }
            }

            // First check if at least one quadrant have radii.
            if (wnd >= 34) {
                int[] rd = getQuadrant(ktCombos1);
                boolean valid34 = rd[0] > 0 || rd[1] > 0 || rd[2] > 0
                        || rd[3] > 0;
                        if (!valid34) {
                            sbd.append(
                                    "34KT: at least one quadrant should have radii > 0.");
                        }

                        if (wnd >= 50) {
                            rd = getQuadrant(ktCombos2);
                            boolean valid50 = rd[0] > 0 || rd[1] > 0 || rd[2] > 0
                                    || rd[3] > 0;
                                    if (!valid50) {
                                        sbd.append(
                                                "\n50KT: at least one quadrant should have radii > 0.");
                                    }

                                    if (wnd >= 64) {
                                        rd = getQuadrant(ktCombos3);
                                        boolean valid64 = rd[0] > 0 || rd[1] > 0 || rd[2] > 0
                                                || rd[3] > 0;
                                                if (!valid64) {
                                                    sbd.append(
                                                            "\n64KT: at least one quadrant should have radii > 0.");
                                                }
                                    }
                        }
            }
        }

        // Check if wind radii 34kt>50kt>64kt
        String vmsg = "";
        if (field == RebestField.RAD_34 || field == RebestField.RAD_50
                || field == RebestField.RAD_64) {

            CCombo cmb = (CCombo) widget;
            int rInd = getComboIndex(cmb);
            int r34 = getQuadrant(ktCombos1, rInd);
            int r50 = getQuadrant(ktCombos2, rInd);
            int r64 = getQuadrant(ktCombos3, rInd);

            switch (field) {
            case RAD_34:
                if (r50 >= r34) {
                    vmsg = "34KT wind radii must be greater than 50KT wind radii.";
                }
                break;

            case RAD_50:
                if (r50 >= r34) {
                    vmsg = "50KT wind radii must be less than 34KT wind radii.";
                } else if (r50 <= r64) {
                    vmsg = "50KT wind radii must be greater than 64KT wind radii.";
                }
                break;

            case RAD_64:
                if (r64 >= r50) {
                    vmsg = "64KT wind radii must be less than 50KT wind radii.";
                }
                break;
            default:
                break;

            }

            if (!vmsg.isEmpty()) {
                AtcfVizUtil.setWarningBackground(cmb);

            }
        }

        // Warning and update widget background.
        if (!vmsg.isEmpty()) {
            sbd.append("\n" + vmsg);
        }

        vmsg = sbd.toString();

        if (!vmsg.isEmpty()) {
            validWndRadii = false;
            MessageDialog.openWarning(getShell(),
                    "Warning - ReBest Track Dialog",
                    "Please correct the following wind radii errors:\n\n"
                            + vmsg);
        }

        // Reset radii comboes' color.
        if (validWndRadii) {
            for (int ii = 0; ii < 4; ii++) {
                AtcfVizUtil.setDefaultBackground(ktCombos1[ii]);
                AtcfVizUtil.setDefaultBackground(ktCombos2[ii]);
                AtcfVizUtil.setDefaultBackground(ktCombos3[ii]);
            }
        }

        return validWndRadii;
    }

    /*
     * Get the selected wind radii values for 34KT, 50Kt, or 64KT..
     *
     * @param CCombo[] CCombo group the wind radii belongs to
     *
     * @return int[] Selected wind radii values.
     */
    private int[] getQuadrant(CCombo[] ktCombos) {
        int[] rad = new int[4];
        for (int ii = 0; ii < 4; ii++) {
            rad[ii] = (int) getComboValue(ktCombos[ii]);
        }

        return rad;
    }

    /*
     * Get the selected wind radii value.
     *
     * @param CCombo[] CCombo group the wind radii belongs to
     *
     * @param which CComb that is selected
     *
     * @return int Selected value of the wind radii.
     */
    private int getQuadrant(CCombo[] ktCombos, int which) {
        return (int) getComboValue(ktCombos[which]);
    }

    /*
     * Find the index of the given wind radii CCombo in its group (34/50/64KT).
     *
     * @param cmb Selected wind radii CCombo
     *
     * @return int Index
     */
    private int getComboIndex(CCombo cmb) {
        int index = -1;
        // Check for 34 KT.
        for (int ii = 0; ii < ktCombos1.length; ii++) {
            if (ktCombos1[ii] == cmb) {
                index = ii;
                break;
            }
        }

        // Check for 50 KT.
        if (index < 0) {
            for (int ii = 0; ii < ktCombos2.length; ii++) {
                if (ktCombos2[ii] == cmb) {
                    index = ii;
                    break;
                }
            }
        }

        // Check for 64 KT.
        if (index < 0) {
            for (int ii = 0; ii < ktCombos3.length; ii++) {
                if (ktCombos3[ii] == cmb) {
                    index = ii;
                    break;
                }
            }
        }

        return index;
    }

    /*
     * Get a DTG with an index offset to a a given DTG in the DTG list.
     *
     * @param dtg DTG to search
     *
     * @param offset Index offset ( < 0, before; > 0, after; 0, original)
     *
     * @return String DTG with an "offset" to the given DTG.
     */
    private String getDtgWithOffset(String dtg, int offset) {
        String prevDtg = null;

        if (offset == 0) {
            prevDtg = dtg;
        } else {
            if (dtg != null) {
                int ind = actualDTGs.indexOf(dtg);
                int indBf = ind + offset;
                if (indBf >= 0 && indBf < actualDTGs.size()) {
                    prevDtg = actualDTGs.get(indBf);
                }
            }
        }

        return prevDtg;

    }

    /*
     * Get a proper default value - default to 0 if there is no data for the
     * selected DTG.
     *
     * @param recVal Value in the BDeckRecord (34kt) for a DTG
     *
     * @return float Default value found.
     */
    private float getDfltValue(float recVal) {
        return (recVal == AbstractAtcfRecord.RMISSD) ? 0 : recVal;
    }

    /*
     * Check if the given DTG is a "new" DTG.
     *
     * @param dtg Value indicating an invalid or missed value.
     *
     * @return boolean
     */
    private boolean isNewDtg(String dtg) {
        boolean isNew = false;
        int indx = actualDTGs.indexOf(dtg);
        if (indx >= 0 && fullDTGStrings[indx].endsWith(NEW_DTG_MARKER)) {
            isNew = true;
        }

        return isNew;
    }

    /*
     * Draw forecast track
     */
    private void drawForecastTrack() {

        AtcfProduct prd = drawingLayer.getResourceData().getAtcfProduct(storm);
        FcstTrackProperties fcstProp = prd.getFcstTrackProperties();

        /*
         * if forecast track is drawn before, retrieve forecast track for
         * current storm at latest best track DTG, refresh data in AtcfProduct,
         * and redraw.
         */
        if (fcstProp != null && prd.getForecastTrackLayer().isOnOff()) {
            Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData = AtcfDataUtil
                    .getFcstTrackRecords(storm, false);
            prd.setFcstTrackDataMap(fcstTrackData);
        }

        FcstTrackGenerator fstTrackGen = new FcstTrackGenerator(drawingLayer,
                fcstProp, storm);
        fstTrackGen.create(true);
    }

}

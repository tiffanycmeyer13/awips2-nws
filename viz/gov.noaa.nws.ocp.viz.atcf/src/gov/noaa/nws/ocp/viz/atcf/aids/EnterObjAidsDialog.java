/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
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
import gov.noaa.nws.ocp.viz.atcf.main.NotificationToolbarDialog;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;

/**
 * Dialog for "Aids"=>"Enter Objective Aids".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 18, 2019 59222      jwu         Initial creation.
 * Jul 15, 2019 65307      jwu         Implement functionalities.
 * Aug 05, 2019 66888      jwu         Update with changes in backend.
 * Aug 07, 2019 66999      jwu         Refresh after saving to get record ID.
 * Aug 16, 2019 67323      jwu         Add verify listeners.
 * Sep 04, 2019 68112      jwu         Save with batch processing.
 * Jun 11, 2019 68118      wpaintsil   Concurrency/multi-user support.
 * Mar 05, 2021 88229      mporricelli Redraw aids display upon Save
 * Mar 18, 2021 89201      mporricelli Add automatic save when submitting
 * Apr 22, 2021 88729      jwu         Update storm type threshold.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class EnterObjAidsDialog extends NotificationToolbarDialog {

    // Names/units for aid information parameters
    private static final String[] aidParameters = new String[] { " TAU\n  hrs",
            "Verify\ndy/hr", "Lat\ndeg", "Lon\ndeg", "Dir\n  ", "Spd\n  ",
            "Wind\n kts", "Value\n  ",
            "             Radii (nm)    \n   NE      SE    SW    NW   " };

    // Parameter to include all wind radiis when building a RecordKey.
    private static final int ALL_WIND_RADII = -1;

    // Available and selected date time groups (DTG).
    private String selectedDateTimeGroup;

    private String[] availableDateTimeGroups;

    // All obj aids tech from current master tech file (normally techlist.dat)
    private Map<String, ObjectiveAidTechEntry> allObjectiveAids = null;

    // Map of A-Deck records retrieved.
    private Map<String, Map<RecordKey, java.util.List<ADeckRecord>>> retrievedADeckRecords;

    // Map of A-Deck records subject to be changed.
    private Map<RecordKey, ADeckRecord> changedADeckRecords;

    // List to show current data.
    private List aidRecList;

    // Technique and DTG combos
    private CCombo techCombo;

    private CCombo dtgCombo;

    // Label to show current TAU
    private Label currentTauLbl;

    private AtcfTaus currentTau;

    private AtcfTaus prevTau;

    // Controls for current DTG information row.
    private Text currentLatTxt;

    private Button latNorthBtn;

    private Button latSouthBtn;

    private Button lonEastBtn;

    private Button lonWestBtn;

    private Text currentLonTxt;

    private Text currentMaxWindTxt;

    // Controls for current DTG's speed/quadrant.
    private CCombo[] ktCombos1;

    private CCombo[] ktCombos2;

    private CCombo[] ktCombos3;

    // Control buttons.
    private Button saveButton;

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
    public EnterObjAidsDialog(Shell parent, Storm storm) {
        super(parent, storm);

        this.sandboxID = AtcfDataUtil.getADeckSandbox(storm);

        setText("Enter Objective Aid - " + storm.getStormName() + " "
                + storm.getStormId());

        if (changedADeckRecords == null) {
            changedADeckRecords = new HashMap<>();
        }

        // Get all defined objective techniques
        if (allObjectiveAids == null) {
            allObjectiveAids = AtcfConfigurationManager.getInstance()
                    .getObjectiveAidTechniques().getAvailableTechniques();
        }

        /*
         * Get all A-Deck date/time groups (DTGs), including baseline and
         * sandbox.
         */
        java.util.List<String> baselineDtgs = AtcfDataUtil
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
        super.initializeComponents(shell);

        // Load data and populate.
        updateData();
    }

    @Override
    protected void createMainContent(Composite parent) {
        createDataComp(parent);
        createControlButtons(parent);

    }

    /**
     * Create the top section.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite dtgInfoComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, false);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginHeight = 3;
        dtgInfoLayout.marginWidth = 20;
        dtgInfoComp.setLayout(dtgInfoLayout);
        dtgInfoComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // List to show obj. aid information.
        createAidInfoComp(dtgInfoComp);

        // Selection of objective aid technique name.
        createTechSelectionComp(dtgInfoComp);

        // Selection of storm DTG
        createDtgSelectionComp(dtgInfoComp);

        // Major composite for data input.
        Composite dataInputComp = new Composite(dtgInfoComp, SWT.BORDER);
        GridLayout tableLayout = new GridLayout(1, true);
        tableLayout.verticalSpacing = 10;
        tableLayout.marginHeight = 3;
        tableLayout.marginWidth = 0;
        dataInputComp.setLayout(tableLayout);
        dataInputComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // Create buttons for all TAUs.
        createTAUButtons(dataInputComp);

        // Create label to show current TAU.
        currentTauLbl = new Label(dataInputComp, SWT.NONE);
        String currentTauStr = "TAU:   " + currentTau.getValue();
        currentTauLbl.setText(currentTauStr);
        GridData currentTauLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        currentTauLbl.setLayoutData(currentTauLblData);

        // Create the row to input current lat/lon/max wind.
        createLatLonWindComp(dataInputComp);

        // Composite for Speed/Quadrant
        createSpeedQuardantComp(dataInputComp);

    }

    /**
     * Create the composite list obj aid info.
     *
     * @param parent
     */
    private void createAidInfoComp(Composite parent) {
        Composite aidInfoComp = new Composite(parent, SWT.BORDER);
        GridLayout aidInfoLayout = new GridLayout(aidParameters.length, false);
        aidInfoLayout.marginWidth = 5;
        aidInfoLayout.marginHeight = 15;
        aidInfoLayout.horizontalSpacing = 30;
        aidInfoLayout.verticalSpacing = 5;
        aidInfoComp.setLayout(aidInfoLayout);
        aidInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        for (String param : aidParameters) {
            Label infoLbl = new Label(aidInfoComp, SWT.NONE);
            infoLbl.setText(param);
            GridData infoLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false);
            infoLbl.setLayoutData(infoLblData);
        }

        Font aidsInfoFont = JFaceResources.getTextFont();
        aidRecList = new List(aidInfoComp,
                SWT.NONE | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        aidRecList.setFont(aidsInfoFont);
        GridData aidRecListData = new GridData(SWT.DEFAULT, 200);
        aidRecListData.horizontalAlignment = SWT.FILL;
        aidRecListData.verticalAlignment = SWT.FILL;
        aidRecListData.horizontalSpan = aidParameters.length;
        aidRecList.setLayoutData(aidRecListData);

        Label recordTypeLbl = new Label(aidInfoComp, SWT.NONE);
        recordTypeLbl.setText("*   Modified Record       +   New Record");
        GridData recordTypeLblData = new GridData(SWT.NONE);
        recordTypeLblData.horizontalAlignment = SWT.FILL;
        recordTypeLblData.verticalAlignment = SWT.FILL;
        recordTypeLblData.horizontalSpan = aidParameters.length;
        recordTypeLbl.setLayoutData(recordTypeLblData);
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
        GridData techComboData = new GridData(SWT.RIGHT, SWT.CENTER, true,
                false);
        techCombo.setLayoutData(techComboData);

        techCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean valid = isValidTechNDtg(techCombo.getText(),
                        dtgCombo.getText());

                if (valid) {
                    updateData();
                }
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
        GridLayout dtgSelectLayout = new GridLayout(2, true);
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
        GridData dtgComboData = new GridData(SWT.FILL, SWT.CENTER, false,
                false);
        dtgCombo.setLayoutData(dtgComboData);

        dtgCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateData();
            }
        });

        if (selectedDateTimeGroup == null) {
            dtgCombo.select(dtgCombo.getItemCount() - 1);
        } else {
            dtgCombo.select(dtgCombo.indexOf(selectedDateTimeGroup));
        }
    }

    /**
     * Create the TAU buttons .
     *
     * @param parent
     */
    private void createTAUButtons(Composite parent) {

        // Composite for parameter names
        Composite tauComp = new Composite(parent, SWT.NONE);
        GridLayout tauLayout = new GridLayout(2, false);
        tauLayout.marginHeight = 10;
        tauLayout.marginWidth = 10;
        tauComp.setLayout(tauLayout);
        GridData tauCompGD = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        tauComp.setLayoutData(tauCompGD);

        Label tauLbl = new Label(tauComp, SWT.NONE);
        tauLbl.setText("TAU:");
        GridData tauLblGD = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        tauLbl.setLayoutData(tauLblGD);

        // Composite for CCombos to select/type parameters
        Composite tauBtnComp = new Composite(tauComp, SWT.NONE);
        GridLayout tauBtnCompLayout = new GridLayout(8, true);
        tauBtnCompLayout.verticalSpacing = 5;
        tauBtnCompLayout.horizontalSpacing = 10;
        tauBtnCompLayout.marginHeight = 0;
        tauBtnCompLayout.marginWidth = 10;
        tauBtnComp.setLayout(tauBtnCompLayout);
        GridData tauBtnCompGD = new GridData(SWT.LEFT, SWT.DEFAULT, true, true);
        tauBtnComp.setLayoutData(tauBtnCompGD);

        prevTau = AtcfTaus.TAU0;
        currentTau = AtcfTaus.TAU0;

        for (AtcfTaus tau : AtcfTaus.values()) {
            Button tauBtn = new Button(tauBtnComp, SWT.TOGGLE);
            tauBtn.setText("" + tau.getValue());
            GridData tauBtnGD = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
            tauBtn.setLayoutData(tauBtnGD);

            tauBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    tauBtn.setSelection(true);

                    prevTau = currentTau;
                    currentTau = tau;

                    if (currentTau != prevTau) {
                        if (currentTauLbl != null) {
                            currentTauLbl
                                    .setText("TAU:   " + currentTau.getValue());
                            currentTauLbl.pack();
                        }

                        for (Control btn : tauBtnComp.getChildren()) {
                            AtcfVizUtil.setDefaultBackground(btn);
                        }

                        AtcfVizUtil.setActiveButtonBackground(tauBtn);

                        // load data for new Tech/DTG/TAU
                        updateDataForTAU();
                    }
                }
            });

            if (tau == currentTau) {
                tauBtn.setSelection(true);
                AtcfVizUtil.setActiveButtonBackground(tauBtn);
            }
        }
    }

    /**
     * Create the row to input current lat/lon/max wind.
     *
     * @param parent
     */
    private void createLatLonWindComp(Composite parent) {

        Composite currentInfoComp = new Composite(parent, SWT.NONE);
        GridLayout currentInfoLayout = new GridLayout(3, true);
        currentInfoLayout.marginHeight = 3;
        currentInfoLayout.marginWidth = 3;
        currentInfoLayout.horizontalSpacing = 10;
        currentInfoComp.setLayout(currentInfoLayout);
        GridData currentInfoCompGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        currentInfoComp.setLayoutData(currentInfoCompGD);

        Composite currentLatComp = new Composite(currentInfoComp, SWT.NONE);
        GridLayout currentLatLayout = new GridLayout(3, false);
        currentLatLayout.marginHeight = 3;
        currentLatLayout.marginWidth = 3;
        currentLatLayout.horizontalSpacing = 5;
        currentLatComp.setLayout(currentLatLayout);
        GridData currentLatCompGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        currentLatComp.setLayoutData(currentLatCompGD);

        Label latLbl = new Label(currentLatComp, SWT.NONE);
        latLbl.setText("Latitude");
        GridData latLblGD = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        latLbl.setLayoutData(latLblGD);

        currentLatTxt = new Text(currentLatComp, SWT.BORDER);
        GridData currentLatComboGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        currentLatTxt.setLayoutData(currentLatComboGD);

        txtModifyListener = txtModifyListener();

        updateTextOnly(currentLatTxt, "25.0");

        currentLatTxt.addListener(SWT.Verify,
                verifyListeners.getLatVerifyListener());

        Composite currentLatDirComp = new Composite(currentLatComp, SWT.NONE);
        GridLayout currentLatDirLayout = new GridLayout(1, true);
        currentLatDirLayout.marginHeight = 0;
        currentLatDirLayout.marginWidth = 0;
        currentLatDirLayout.verticalSpacing = 0;
        currentLatDirComp.setLayout(currentLatDirLayout);
        GridData currentLatDirCompGD = new GridData(SWT.CENTER, SWT.DEFAULT,
                true, false);
        currentLatDirComp.setLayoutData(currentLatDirCompGD);

        latNorthBtn = new Button(currentLatDirComp, SWT.RADIO);
        latNorthBtn.setText("N");
        latNorthBtn.setSelection(true);
        GridData latNorthBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        latNorthBtn.setLayoutData(latNorthBtnGD);

        SelectionAdapter dirSelAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDataForLocWnd();
            }
        };

        latNorthBtn.addSelectionListener(dirSelAdapter);

        latSouthBtn = new Button(currentLatDirComp, SWT.RADIO);
        latSouthBtn.setText("S");
        GridData latSouthBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        latSouthBtn.setLayoutData(latSouthBtnGD);
        latSouthBtn.addSelectionListener(dirSelAdapter);

        Composite currentLonComp = new Composite(currentInfoComp, SWT.NONE);
        GridLayout currentLonLayout = new GridLayout(3, false);
        currentLonLayout.marginHeight = 3;
        currentLonLayout.marginWidth = 3;
        currentLonLayout.horizontalSpacing = 3;
        currentLonComp.setLayout(currentLonLayout);
        GridData currentLonCompGD = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        currentLonComp.setLayoutData(currentLonCompGD);

        Label lonLbl = new Label(currentLonComp, SWT.NONE);
        lonLbl.setText("Longitude");
        GridData lonLblGD = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        lonLbl.setLayoutData(lonLblGD);

        currentLonTxt = new Text(currentLonComp, SWT.BORDER);
        GridData currentLonComboGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        currentLonTxt.setLayoutData(currentLonComboGD);
        updateTextOnly(currentLonTxt, "100.0");
        currentLonTxt.addListener(SWT.Verify,
                verifyListeners.getLonVerifyListener());

        Composite currentLonDirComp = new Composite(currentLonComp, SWT.NONE);
        GridLayout currentLonDirLayout = new GridLayout(1, true);
        currentLonDirLayout.marginHeight = 0;
        currentLonDirLayout.marginWidth = 0;
        currentLonDirLayout.verticalSpacing = 0;
        currentLonDirComp.setLayout(currentLonDirLayout);
        GridData currentLonDirGD = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        currentLatDirComp.setLayoutData(currentLonDirGD);

        lonEastBtn = new Button(currentLonDirComp, SWT.RADIO);
        lonEastBtn.setText("E");
        GridData lonEastBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        lonEastBtn.setLayoutData(lonEastBtnGD);
        lonEastBtn.addSelectionListener(dirSelAdapter);

        lonWestBtn = new Button(currentLonDirComp, SWT.RADIO);
        lonWestBtn.setText("W");
        lonWestBtn.setSelection(true);
        GridData lonWestBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        lonWestBtn.setLayoutData(lonWestBtnGD);
        lonWestBtn.addSelectionListener(dirSelAdapter);

        Composite maxWindComp = new Composite(currentInfoComp, SWT.NONE);
        GridLayout maxWindLayout = new GridLayout(3, false);
        maxWindLayout.marginWidth = 3;
        maxWindLayout.horizontalSpacing = 3;
        maxWindComp.setLayout(currentLonLayout);
        GridData maxWindCompGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        maxWindComp.setLayoutData(maxWindCompGD);

        Label maxWindLbl = new Label(maxWindComp, SWT.NONE);
        maxWindLbl.setText("Max Wind");
        GridData maxWindLblGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        maxWindLbl.setLayoutData(maxWindLblGD);

        currentMaxWindTxt = new Text(maxWindComp, SWT.BORDER);
        GridData currentMaxWindComboGD = new GridData(SWT.CENTER, SWT.CENTER,
                true, false);
        currentMaxWindComboGD.minimumWidth = 35;
        currentMaxWindTxt.setLayoutData(currentMaxWindComboGD);
        updateTextOnly(currentMaxWindTxt, "100.0");
        currentMaxWindTxt.addListener(SWT.Verify,
                verifyListeners.getWindTextVerifyListener());

        Label maxWindUnitLbl = new Label(maxWindComp, SWT.NONE);
        maxWindUnitLbl.setText("knots");
        GridData maxWindUnitLblGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        maxWindUnitLbl.setLayoutData(maxWindUnitLblGD);

    }

    /**
     * Create composite to input speed and quadrant.
     *
     * @param parent
     */
    private void createSpeedQuardantComp(Composite parent) {
        Composite spdQuadrantComp = new Composite(parent, SWT.NONE);
        GridLayout spdQuadrantLayout = new GridLayout(5, false);
        spdQuadrantLayout.marginWidth = 0;
        spdQuadrantLayout.marginHeight = 5;
        spdQuadrantLayout.horizontalSpacing = 0;
        spdQuadrantLayout.verticalSpacing = 8;
        spdQuadrantComp.setLayout(spdQuadrantLayout);
        GridData spdQuadrantCompGD = new GridData(SWT.FILL, SWT.TOP, true,
                false);
        spdQuadrantCompGD.horizontalSpan = 6;
        spdQuadrantComp.setLayoutData(spdQuadrantCompGD);

        Label spdQuadrantLbl = new Label(spdQuadrantComp, SWT.NONE);
        spdQuadrantLbl.setText("");
        spdQuadrantLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label neLabel = new Label(spdQuadrantComp, SWT.NONE);
        neLabel.setText("NE (nm)");
        neLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label seLabel = new Label(spdQuadrantComp, SWT.NONE);
        seLabel.setText("SE (nm)");
        seLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label swLabel = new Label(spdQuadrantComp, SWT.NONE);
        swLabel.setText("SW (nm)");
        swLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label nwLabel = new Label(spdQuadrantComp, SWT.NONE);
        nwLabel.setText("NW (nm)");
        nwLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        String[] ktItems = new String[200];
        for (int ii = 0; ii < ktItems.length; ii++) {
            ktItems[ii] = String.valueOf(ii * 5);
        }

        Label ktLabel1 = new Label(spdQuadrantComp, SWT.NONE);
        ktLabel1.setText("34 kt winds");
        ktLabel1.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        ktCombos1 = new CCombo[4];
        for (int jj = 0; jj < ktCombos1.length; jj++) {
            ktCombos1[jj] = new CCombo(spdQuadrantComp, SWT.BORDER);
            ktCombos1[jj].setItems(ktItems);
            ktCombos1[jj].select(0);
            ktCombos1[jj].setEditable(false);
            ktCombos1[jj].setData(jj);
            ktCombos1[jj].setEnabled(false);

            ktCombos1[jj].setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, true, false));

            ktCombos1[jj].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateDataForLocWnd();
                }
            });
        }

        Label ktLabel2 = new Label(spdQuadrantComp, SWT.NONE);
        ktLabel2.setText("50 kt winds");
        ktLabel2.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        ktCombos2 = new CCombo[4];
        for (int jj = 0; jj < ktCombos2.length; jj++) {
            ktCombos2[jj] = new CCombo(spdQuadrantComp, SWT.BORDER);
            ktCombos2[jj].setItems(ktItems);
            ktCombos2[jj].select(0);
            ktCombos2[jj].setEditable(false);
            ktCombos2[jj].setData(jj);
            ktCombos2[jj].setEnabled(false);

            ktCombos2[jj].setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, true, false));

            ktCombos2[jj].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int ind = (int) e.widget.getData();
                    int kt1Ind = ktCombos1[ind].getSelectionIndex();
                    int wnd1 = Integer.parseInt(ktItems[kt1Ind]);
                    int wnd2 = Integer.parseInt(ktCombos2[ind].getText());
                    if (wnd2 >= wnd1) {
                        if (kt1Ind > 0) {
                            ktCombos2[ind].select(kt1Ind - 1);
                        } else {
                            ktCombos2[ind].select(0);
                        }
                    }

                    updateDataForLocWnd();
                }
            });
        }

        Label ktLabel3 = new Label(spdQuadrantComp, SWT.NONE);
        ktLabel3.setText("64 kt winds");
        ktLabel3.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        ktCombos3 = new CCombo[4];
        for (int jj = 0; jj < ktCombos3.length; jj++) {
            ktCombos3[jj] = new CCombo(spdQuadrantComp, SWT.BORDER);
            ktCombos3[jj].setItems(ktItems);
            ktCombos3[jj].select(0);
            ktCombos3[jj].setEditable(false);
            ktCombos3[jj].setData(jj);
            ktCombos3[jj].setEnabled(false);

            ktCombos3[jj].setLayoutData(
                    new GridData(SWT.CENTER, SWT.CENTER, true, false));
            ktCombos3[jj].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int ind = (int) e.widget.getData();
                    int kt1Ind = ktCombos2[ind].getSelectionIndex();
                    int wnd1 = Integer.parseInt(ktItems[kt1Ind]);
                    int wnd2 = Integer.parseInt(ktCombos3[ind].getText());
                    if (wnd2 >= wnd1) {
                        if (kt1Ind > 0) {
                            ktCombos3[ind].select(kt1Ind - 1);
                        } else {
                            ktCombos3[ind].select(kt1Ind);
                        }
                    }

                    updateDataForLocWnd();
                }
            });
        }

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite controlComp = new Composite(parent, SWT.NONE);
        GridLayout controlCompLayout = new GridLayout(3, true);
        controlComp.setLayout(controlCompLayout);
        controlComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(controlComp, AtcfVizUtil.buttonGridData());

        saveButton = new Button(controlComp, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.setLayoutData(AtcfVizUtil.buttonGridData());
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saveChanges()) {
                    saveButton.setEnabled(false);
                }
            }
        });

        Button cancelButton = new Button(controlComp, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    /**
     * Enum representing radwind values
     */
    private enum RadWind {
        KT34, KT50, KT64;
    }

    /**
     * Update a ADeckRecord's lat, lon and max wind from GUI.
     *
     * @param rec
     *            ADeckRecord
     */
    private void updateLocWnd(ADeckRecord rec) {

        String lat = currentLatTxt.getText();
        float latVal = 0.0F;
        try {
            latVal = Float.valueOf(lat);
            if (latSouthBtn.getSelection()) {
                latVal = -latVal;
            }
        } catch (NumberFormatException ne) {
            // use default 0.
        }

        rec.setClat(latVal);

        String lon = currentLonTxt.getText();
        float lonVal = 0.0F;
        try {
            lonVal = Float.valueOf(lon);
            if (lonWestBtn.getSelection()) {
                lonVal = -lonVal;
            }
        } catch (NumberFormatException ne) {
            // use default 0.
        }
        rec.setClon(lonVal);

        String wnd = currentMaxWindTxt.getText();
        float wndVal = 0F;
        try {
            wndVal = Float.valueOf(wnd);
        } catch (NumberFormatException ne) {
            // use default 0.
        }
        rec.setWindMax(wndVal);

        // Set intensity based on wind speed.
        String iniType = rec.getIntensity();
        rec.setIntensity(StormDevelopment.getIntensity(iniType, wndVal));

    }

    /**
     * Update a ADeckRecord's quadrant wind from GUI.
     *
     * @param rec
     *            ADeckRecord
     * @param ktCombos
     *            CCombs to get quadrant values
     *
     */
    private void updateQuadrant(ADeckRecord rec, CCombo[] ktCombos) {
        int quad1WindRad = Integer.parseInt(ktCombos[0].getText());
        int quad2WindRad = Integer.parseInt(ktCombos[1].getText());
        int quad3WindRad = Integer.parseInt(ktCombos[2].getText());
        int quad4WindRad = Integer.parseInt(ktCombos[3].getText());

        rec.setQuad1WindRad(quad1WindRad);
        rec.setQuad2WindRad(quad2WindRad);
        rec.setQuad3WindRad(quad3WindRad);
        rec.setQuad4WindRad(quad4WindRad);
    }

    /**
     * Create a new ADeckRecord for a Tau/windRadii if at least one of quadrant
     * winds > 0.
     *
     * @param ktCombos
     *            CCombs to get quadrant values
     * @param tau
     *            AtcfTaus
     * @return ADeckRecord
     */
    private ADeckRecord createRecordFromQuadrant(CCombo[] ktCombos,
            AtcfTaus tau) {

        ADeckRecord rec = null;

        String lat = currentLatTxt.getText();
        String lon = currentLonTxt.getText();
        String wndMax = currentMaxWindTxt.getText();

        // If lat/lon/max wind are all empty, do not create new records.
        if (lat.trim().isEmpty() && lon.trim().isEmpty()
                && wndMax.trim().isEmpty()) {
            return rec;
        }

        if ((ktCombos == ktCombos2 && !ktCombos2[0].isEnabled())
                || (ktCombos == ktCombos3 && !ktCombos3[0].isEnabled())) {
            return rec;
        }

        // Now create new records.
        int qw1 = Integer.parseInt(ktCombos[0].getText());
        int qw2 = Integer.parseInt(ktCombos[1].getText());
        int qw3 = Integer.parseInt(ktCombos[2].getText());
        int qw4 = Integer.parseInt(ktCombos[3].getText());

        rec = new ADeckRecord();

        rec.setBasin(storm.getRegion());
        rec.setCycloneNum(storm.getCycloneNum());
        rec.setYear(storm.getYear());
        rec.setStormName(storm.getStormName());

        rec.setTechnique(techCombo.getText());
        rec.setFcstHour(tau.getValue());

        rec.setRefTime(AtcfDataUtil.parseDtg(dtgCombo.getText()));
        rec.setFcstHour(tau.getValue());

        rec.setMaxSeas(0);
        rec.setEyeSize(0);

        updateLocWnd(rec);

        rec.setQuad1WindRad(qw1);
        rec.setQuad2WindRad(qw2);
        rec.setQuad3WindRad(qw3);
        rec.setQuad4WindRad(qw4);

        float radWnd = 34;
        if (ktCombos == ktCombos2) {
            radWnd = 50;
        } else if (ktCombos == ktCombos3) {
            radWnd = 64;
        }

        rec.setRadWind(radWnd);

        return rec;
    }

    /**
     * @param selectedDateTimeGroups
     *            the selectedDateTimeGroups to set
     */
    public void setSelectedDateTimeGroup(String selectedDateTimeGroup) {
        this.selectedDateTimeGroup = selectedDateTimeGroup;
    }

    /**
     * @param availableDateTimeGroups
     *            the availableDateTimeGroups to set
     */
    public void setAvailableDateTimeGroups(String[] availableDateTimeGroups) {
        this.availableDateTimeGroups = availableDateTimeGroups;
    }

    /**
     * Retrieve all A-Deck Data for a given DTG.
     *
     * @param dtg
     *            data time group string
     * @return Map<RecordKey, java.util.List<ADeckRecord>>
     *
     */
    private Map<RecordKey, java.util.List<ADeckRecord>> getADeckData(
            String dtg) {

        Map<RecordKey, java.util.List<ADeckRecord>> records = new HashMap<>();
        if (retrievedADeckRecords != null) {
            records = retrievedADeckRecords.get(dtg);
        } else {
            retrievedADeckRecords = new HashMap<>();
        }

        if (records == null || records.isEmpty()) {
            Map<String, java.util.List<ADeckRecord>> arecs = AtcfDataUtil
                    .retrieveADeckData(storm, new String[] { dtg }, true);

            java.util.List<ADeckRecord> recs = arecs.get(dtg);
            if (recs != null && !recs.isEmpty()) {
                records = groupADeckData(dtg, recs);
            }
        }

        retrievedADeckRecords.put(dtg, records);

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
     * Group a list of ADeckRecord by Tech/DTG/TAU.
     *
     * @param dtg
     *            data time group string
     * @param recs
     *            list of ADeckRecords for the dtg
     *
     * @return Map<RecordKey, ADeckRecord>
     */
    private Map<RecordKey, java.util.List<ADeckRecord>> groupADeckData(
            String dtg, java.util.List<ADeckRecord> recs) {

        // Group records by tech/DTG/Tau
        Map<RecordKey, java.util.List<ADeckRecord>> recordMap = new HashMap<>();

        for (ADeckRecord rec : recs) {
            String aid = rec.getTechnique();
            int tau = rec.getFcstHour();

            RecordKey rkey = new RecordKey(aid, dtg, tau, ALL_WIND_RADII);
            java.util.List<ADeckRecord> recList = recordMap.get(rkey);
            if (recList == null) {
                recList = new ArrayList<>();
            }

            recList.add(rec);

            recordMap.put(rkey, recList);
        }

        return recordMap;
    }

    /*
     * Clear/reset all data field except aidInfoList.
     */
    private void clearData() {

        updateTextOnly(currentLatTxt, "");

        latNorthBtn.setSelection(true);
        latSouthBtn.setSelection(false);

        updateTextOnly(currentLonTxt, "");

        lonEastBtn.setSelection(false);
        lonWestBtn.setSelection(true);

        updateTextOnly(currentMaxWindTxt, "");

        for (CCombo cmb : ktCombos1) {
            cmb.setText("0");
            cmb.setEnabled(false);
        }

        for (CCombo cmb : ktCombos2) {
            cmb.setText("0");
            cmb.setEnabled(false);
        }

        for (CCombo cmb : ktCombos3) {
            cmb.setText("0");
            cmb.setEnabled(false);
        }
    }

    /*
     * Update all data shown in the dialog
     *
     * @param recs List of ADeckRecord for a given DTG/Tech/TAU
     */
    private void updateDataDisplay(java.util.List<ADeckRecord> recs) {

        // Update info list
        updateAidInfoList();

        // Clear off other displayed data
        clearData();

        if (recs == null || recs.isEmpty()) {
            return;
        }

        /*
         * Update display values for lat/lon/max wind/quadrant wind.
         */
        float latValue = recs.get(0).getClat();
        String latString = String.format("%.2f",
                latValue < 0 ? (-1 * latValue) : latValue);
        updateTextOnly(currentLatTxt, latString);

        if (latValue < 0) {
            latNorthBtn.setSelection(false);
            latSouthBtn.setSelection(true);
        } else {
            latNorthBtn.setSelection(true);
            latSouthBtn.setSelection(false);
        }

        float lonValue = recs.get(0).getClon();
        String lonString = String.format("%.2f",
                lonValue < 0 ? (-1 * lonValue) : lonValue);
        updateTextOnly(currentLonTxt, lonString);
        if (lonValue < 0) {
            lonEastBtn.setSelection(false);
            lonWestBtn.setSelection(true);
        } else {
            lonEastBtn.setSelection(true);
            lonWestBtn.setSelection(false);
        }

        int maxWnd = (int) recs.get(0).getWindMax();
        String maxWndString = String.format("%d", maxWnd);
        updateTextOnly(currentMaxWindTxt, maxWndString);

        // Enable/disable quadrant combos based on max wind.
        enableQuadantCombos(maxWnd);

        // Group the record based on its rad wind.
        Map<RadWind, ADeckRecord> aDeckRowKt = new EnumMap<>(RadWind.class);

        for (ADeckRecord rec : recs) {
            if ((int) rec.getRadWind() == 34) {
                aDeckRowKt.put(RadWind.KT34, rec);
            } else if ((int) rec.getRadWind() == 50) {
                aDeckRowKt.put(RadWind.KT50, rec);
            } else if ((int) rec.getRadWind() == 64) {
                aDeckRowKt.put(RadWind.KT64, rec);
            }
        }

        // Speed/Quadrant
        for (int ii = 0; ii < 4; ii++) {
            if (aDeckRowKt.containsKey(RadWind.KT34)) {
                ktCombos1[ii].setText(String.format("%d",
                        aDeckRowKt.get(RadWind.KT34).getWindRadii()[ii]));
            }
            if (aDeckRowKt.containsKey(RadWind.KT50)) {
                ktCombos2[ii].setText(String.format("%d",
                        aDeckRowKt.get(RadWind.KT50).getWindRadii()[ii]));
            }
            if (aDeckRowKt.containsKey(RadWind.KT64)) {
                ktCombos3[ii].setText(String.format("%d",
                        aDeckRowKt.get(RadWind.KT64).getWindRadii()[ii]));
            }
        }

    }

    /*
     * Build an information string for an ADeckRecord
     *
     * @param rec ADeckRecord
     *
     * @return String
     */
    private String buildADeckRecordInfoStr(ADeckRecord rec) {
        String str = null;
        if (rec != null) {

            RecordKey rk = new RecordKey(rec.getTechnique(), dtgCombo.getText(),
                    rec.getFcstHour(), (int) rec.getRadWind());

            ADeckRecord baseRec = AtcfDataUtil.getADeckRecord(storm, rk, -1);

            Calendar rtime = rec.getRefTimeAsCalendar();
            int fcstHr = rec.getFcstHour();
            rtime.add(Calendar.HOUR_OF_DAY, fcstHr);

            String timeStr = String.format("%02d/%02d",
                    rtime.get(Calendar.DAY_OF_MONTH),
                    rtime.get(Calendar.HOUR_OF_DAY));

            float lat = rec.getClat();
            String latDir = "N";
            if (lat < 0) {
                latDir = "S";
            }

            float lon = rec.getClon();
            String lonDir = "E";
            if (lon <= 0.0F) {
                lonDir = "W";
            }

            if (lon < 0) {
                lon = -lon;
            }

            float dir = rec.getStormDrct();
            String dirStr = "0";
            if (dir >= 0 && dir <= 360) {
                dirStr = "" + (int) dir;
            }

            float spd = rec.getStormSped();
            String spdStr = "0";
            if (spd >= 0 && dir < 1000) {
                spdStr = "" + (int) spd;
            }

            float maxWnd = rec.getWindMax();
            String maxWndStr = "";
            if (maxWnd >= 0 && maxWnd < 1000) {
                maxWndStr = "" + (int) maxWnd;
            }

            float wndRad = rec.getRadWind();
            String wndRadStr = "" + (int) wndRad + "kt";

            /*
             * For new records (record not exists in baseline), prefix "+"; for
             * modified records, prefix "*".
             */
            str = " ";
            if (baseRec == null) {
                str = "+";
            } else {
                if (isRecordChanged(baseRec, rec)) {
                    str = "*";
                }
            }

            str += String.format(
                    "%3d %7s %6.1f%s %5.1f%s %4s %5s %6s %8s %8d %4d %4d %4d",
                    fcstHr, timeStr, Math.abs(lat), latDir, Math.abs(lon),
                    lonDir, dirStr, spdStr, maxWndStr, wndRadStr,
                    (int) rec.getQuad1WindRad(), (int) rec.getQuad2WindRad(),
                    (int) rec.getQuad3WindRad(), (int) rec.getQuad4WindRad());

        }

        return str;

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
     * Load the data based on the TECH or DTG change.
     */
    private void updateData() {

        String dtg = dtgCombo.getText();
        String tech = techCombo.getText();

        // First check if data exists in "changedRecords"
        ADeckRecord rec34 = null;
        ADeckRecord rec50 = null;
        ADeckRecord rec64 = null;
        RecordKey key34 = new RecordKey(tech, dtg, currentTau.getValue(), 34);
        RecordKey key50 = new RecordKey(tech, dtg, currentTau.getValue(), 50);
        RecordKey key64 = new RecordKey(tech, dtg, currentTau.getValue(), 64);

        rec34 = changedADeckRecords.get(key34);
        rec50 = changedADeckRecords.get(key50);
        rec64 = changedADeckRecords.get(key64);

        /*
         * Retrieve ADeckRecord for current DTG - each ADeck DTG will have
         * multiple records, depending on technique, forecast time (TAU in
         * seconds), and radii wind.
         */
        Map<RecordKey, java.util.List<ADeckRecord>> adeckData = getADeckData(
                dtg);

        // Now find records for current technique, DTG, and tau.
        RecordKey rkey = new RecordKey(tech, dtg, currentTau.getValue(),
                ALL_WIND_RADII);
        java.util.List<ADeckRecord> recs = new ArrayList<>();

        if (adeckData != null) {
            java.util.List<ADeckRecord> recList = adeckData.get(rkey);

            /*
             * Use the changed record if found; otherwise use the retrieved
             * ones.
             */
            if (rec34 != null) {
                recs.add(rec34);
            } else {
                if (recList != null) {
                    for (ADeckRecord arec : recList) {
                        if ((int) arec.getRadWind() == 34) {
                            recs.add(arec);
                            break;
                        }
                    }
                }
            }

            if (rec50 != null) {
                recs.add(rec50);
            } else {
                if (recList != null) {
                    for (ADeckRecord arec : recList) {
                        if ((int) arec.getRadWind() == 50) {
                            recs.add(arec);
                            break;
                        }
                    }
                }
            }

            if (rec64 != null) {
                recs.add(rec64);
            } else {
                if (recList != null) {
                    for (ADeckRecord arec : recList) {
                        if ((int) arec.getRadWind() == 64) {
                            recs.add(arec);
                            break;
                        }
                    }
                }
            }
        }

        updateDataDisplay(recs);

    }

    /**
     * Load the data based on the TAU change.
     */
    private void updateDataForTAU() {

        /*
         * Check if technique and DTG are valid
         */
        String tech = techCombo.getText();
        String dtg = dtgCombo.getText();

        boolean valid = isValidTechNDtg(tech, dtg);
        if (valid && currentTau != prevTau) {

            // Update changes for previous TAU before display data for new TAU.
            ADeckRecord rec34 = null;
            ADeckRecord rec50 = null;
            ADeckRecord rec64 = null;
            RecordKey key34 = new RecordKey(tech, dtg, prevTau.getValue(), 34);
            RecordKey key50 = new RecordKey(tech, dtg, prevTau.getValue(), 50);
            RecordKey key64 = new RecordKey(tech, dtg, prevTau.getValue(), 64);

            // Get data
            Map<RecordKey, java.util.List<ADeckRecord>> adeckData = getADeckData(
                    dtg);

            // Now find records for previous technique, DTG, and tau.
            RecordKey rkey = new RecordKey(tech, dtg, prevTau.getValue(),
                    ALL_WIND_RADII);

            if (adeckData != null) {
                java.util.List<ADeckRecord> recList = adeckData.get(rkey);
                if (recList != null) {
                    rec34 = changedADeckRecords.get(key34);
                    if (rec34 == null) {
                        for (ADeckRecord arec : recList) {
                            if ((int) arec.getRadWind() == 34) {
                                rec34 = arec;
                                break;
                            }
                        }
                    }

                    rec50 = changedADeckRecords.get(key50);
                    if (rec50 == null) {
                        for (ADeckRecord arec : recList) {
                            if ((int) arec.getRadWind() == 50) {
                                rec50 = arec;
                                break;
                            }
                        }
                    }

                    rec64 = changedADeckRecords.get(key64);
                    if (rec64 == null) {
                        for (ADeckRecord arec : recList) {
                            if ((int) arec.getRadWind() == 64) {
                                rec64 = arec;
                                break;
                            }
                        }
                    }
                }
            }

            // Check for wind radii 34
            if (rec34 != null) {
                updateLocWnd(rec34);
                updateQuadrant(rec34, ktCombos1);
            } else {
                rec34 = createRecordFromQuadrant(ktCombos1, prevTau);
            }

            if (rec34 != null) {
                changedADeckRecords.put(key34, rec34);
            }

            // Check for each wind radii 50
            if (rec50 != null) {
                updateLocWnd(rec50);
                updateQuadrant(rec50, ktCombos2);
            } else {
                rec50 = createRecordFromQuadrant(ktCombos2, prevTau);
            }

            if (rec50 != null) {
                changedADeckRecords.put(key50, rec50);
            }

            // Check for each wind radii 64
            if (rec64 != null) {
                updateLocWnd(rec64);
                updateQuadrant(rec64, ktCombos3);
            } else {
                rec64 = createRecordFromQuadrant(ktCombos3, prevTau);
            }

            if (rec64 != null) {
                changedADeckRecords.put(key64, rec64);
            }

            // Now load data.
            updateData();
        }
    }

    /**
     * Enable/disable quadrant combos based on max wind speed.
     *
     * @param maxWnd
     *            max wind
     */
    private void enableQuadantCombos(int maxWnd) {
        boolean enable34 = false;
        boolean enable50 = false;
        boolean enable64 = false;

        if (maxWnd >= 64) {
            enable64 = true;
            enable50 = true;
            enable34 = true;
        } else if (maxWnd >= 50) {
            enable50 = true;
            enable34 = true;
        } else if (maxWnd >= 34) {
            enable34 = true;
        }

        for (CCombo cmb : ktCombos1) {
            cmb.setEnabled(enable34);
        }

        for (CCombo cmb : ktCombos2) {
            cmb.setEnabled(enable50);
        }

        for (CCombo cmb : ktCombos3) {
            cmb.setEnabled(enable64);
        }
    }

    /**
     * Finds all most-recent ADeckRecords for given technique and DTG. For the a
     * record with the same tech/dtg/tau/radwind, use the most recent changed
     * one (changedADeckRecord). If not in changedADeckRecord, use the one in
     * retrievedADeckRecords, which stores records from current sandbox.
     *
     * @param tech
     *            aids technique
     * @param dtg
     *            date time group (YYYYMMDDHH)
     * @return list<ADeckRecord>
     */
    private java.util.List<ADeckRecord> findRecordsForAllTAUs(String tech,
            String dtg) {

        Map<RecordKey, java.util.List<ADeckRecord>> recMap4DTG = retrievedADeckRecords
                .get(dtg);

        java.util.List<ADeckRecord> recList = new ArrayList<>();

        for (AtcfTaus tau : AtcfTaus.values()) {

            RecordKey key34 = new RecordKey(tech, dtg, tau.getValue(), 34);
            RecordKey key50 = new RecordKey(tech, dtg, tau.getValue(), 50);
            RecordKey key64 = new RecordKey(tech, dtg, tau.getValue(), 64);

            RecordKey allKey = new RecordKey(tech, dtg, tau.getValue(),
                    ALL_WIND_RADII);

            // First check if records are in changedADeckRecords.
            ADeckRecord rec34 = changedADeckRecords.get(key34);
            ADeckRecord rec50 = changedADeckRecords.get(key50);
            ADeckRecord rec64 = changedADeckRecords.get(key64);

            /*
             * If not found in changedADeckRecords, check if records are in
             * retrievedADeckRecords.
             */
            java.util.List<ADeckRecord> allRecs = recMap4DTG.get(allKey);

            if (allRecs != null) {
                for (ADeckRecord arec : allRecs) {
                    int radWnd = (int) arec.getRadWind();
                    if (rec34 == null && radWnd == 34) {
                        rec34 = arec;
                    } else if (rec50 == null && radWnd == 50) {
                        rec50 = arec;
                    } else if (rec64 == null && radWnd == 64) {
                        rec64 = arec;
                    }
                }
            }

            // Add records into list.
            if (rec34 != null) {
                recList.add(rec34);
            }

            if (rec50 != null) {
                recList.add(rec50);
            }

            if (rec64 != null) {
                recList.add(rec64);
            }

        }

        return recList;
    }

    /**
     * Check if two ADeckRecords have different lat, lon, max wind, or quadrant
     * winds.
     *
     * @param rec1
     *            ADeckRecord
     * @param rec2
     *            ADeckRecord
     * @return boolean true - different; false - same.
     *
     */
    private boolean isRecordChanged(ADeckRecord rec1, ADeckRecord rec2) {
        boolean changed = false;

        if ((int) (rec1.getClat() * 100) != (int) (rec2.getClat() * 100)
                || (int) (rec1.getClon() * 100) != (int) (rec2.getClon() * 100)
                || (int) rec1.getWindMax() != (int) rec2.getWindMax()
                || (int) rec1.getQuad1WindRad() != (int) rec2.getQuad1WindRad()
                || (int) rec1.getQuad2WindRad() != (int) rec2.getQuad2WindRad()
                || (int) rec1.getQuad3WindRad() != (int) rec2.getQuad3WindRad()
                || (int) rec1.getQuad4WindRad() != (int) rec2
                        .getQuad4WindRad()) {
            changed = true;
        }

        return changed;

    }

    /**
     * Load the data based on the TAU change.
     */
    private void updateDataForLocWnd() {

        /*
         * Check if technique and DTG are valid
         */
        String tech = techCombo.getText();
        String dtg = dtgCombo.getText();

        boolean valid = isValidTechNDtg(tech, dtg);
        if (valid) {

            // Update changes for CURRENT TAU before display data for new TAU.
            ADeckRecord rec34 = null;
            ADeckRecord rec50 = null;
            ADeckRecord rec64 = null;

            RecordKey key34 = new RecordKey(tech, dtg, currentTau.getValue(),
                    34);
            RecordKey key50 = new RecordKey(tech, dtg, currentTau.getValue(),
                    50);
            RecordKey key64 = new RecordKey(tech, dtg, currentTau.getValue(),
                    64);

            rec34 = changedADeckRecords.get(key34);
            rec50 = changedADeckRecords.get(key50);
            rec64 = changedADeckRecords.get(key64);

            // Get data
            Map<RecordKey, java.util.List<ADeckRecord>> adeckData = getADeckData(
                    dtg);

            // Now find records for previous technique, DTG, and tau.
            RecordKey rkey = new RecordKey(tech, dtg, currentTau.getValue(),
                    ALL_WIND_RADII);

            if (adeckData != null) {
                java.util.List<ADeckRecord> recList = adeckData.get(rkey);
                if (recList != null) {
                    if (rec34 == null) {
                        for (ADeckRecord arec : recList) {
                            if ((int) arec.getRadWind() == 34) {
                                rec34 = arec;
                                break;
                            }
                        }
                    }

                    if (rec50 == null) {
                        for (ADeckRecord arec : recList) {
                            if ((int) arec.getRadWind() == 50) {
                                rec50 = arec;
                                break;
                            }
                        }
                    }

                    if (rec64 == null) {
                        for (ADeckRecord arec : recList) {
                            if ((int) arec.getRadWind() == 64) {
                                rec64 = arec;
                                break;
                            }
                        }
                    }
                }
            }

            // Check for wind radii 34
            if (rec34 != null) {
                updateLocWnd(rec34);
                updateQuadrant(rec34, ktCombos1);
            } else {
                rec34 = createRecordFromQuadrant(ktCombos1, currentTau);
            }

            if (rec34 != null) {
                changedADeckRecords.put(key34, rec34);
                saveButton.setEnabled(true);
                setToolbarStatus(ToolbarType.READY_SUBMIT);
            }

            // Check for each wind radii 50
            if (rec50 != null) {
                updateLocWnd(rec50);
                updateQuadrant(rec50, ktCombos2);
            } else {
                rec50 = createRecordFromQuadrant(ktCombos2, currentTau);
            }

            if (rec50 != null) {
                changedADeckRecords.put(key50, rec50);
                saveButton.setEnabled(true);
                setToolbarStatus(ToolbarType.READY_SUBMIT);
            }

            // Check for each wind radii 64
            if (rec64 != null) {
                updateLocWnd(rec64);
                updateQuadrant(rec64, ktCombos3);
            } else {
                rec64 = createRecordFromQuadrant(ktCombos3, currentTau);
            }

            if (rec64 != null) {
                changedADeckRecords.put(key64, rec64);
                saveButton.setEnabled(true);
                setToolbarStatus(ToolbarType.READY_SUBMIT);
            }

            // Now load data.
            updateAidInfoList();
        }
    }

    /*
     * Update data shown in the info list at the dialog
     */
    private void updateAidInfoList() {

        java.util.List<String> recInfos = new ArrayList<>();

        // Find all records for the current technique/DTG.
        java.util.List<ADeckRecord> recList = findRecordsForAllTAUs(
                techCombo.getText(), dtgCombo.getText());

        // Build information for all records found.
        for (ADeckRecord rec : recList) {
            String str = buildADeckRecordInfoStr(rec);
            if (str != null) {
                recInfos.add(str);
            }
        }

        // Display information for all records found.
        aidRecList.removeAll();
        for (String str : recInfos) {
            aidRecList.add(str);
        }

    }

    /*
     * Listener to handle text changes in lat, lon, and max wind.
     */
    private ModifyListener txtModifyListener() {
        return (e -> {
            if (e.widget == currentMaxWindTxt) {
                String wndStr = ((Text) e.widget).getText();
                try {
                    int maxWnd = Integer.parseInt(wndStr);
                    // Enable/disable quadrant combos based on max wind.
                    enableQuadantCombos(maxWnd);
                } catch (NumberFormatException ne) {
                    // Ignore
                }
            }

            updateDataForLocWnd();
        });

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
     * Save new and changed records into sandbox.
     */
    private boolean saveChanges() {

        java.util.List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();
        java.util.Set<String> updatedDtgs = new TreeSet<>();
        boolean successfulSave = false;

        for (Map.Entry<RecordKey, ADeckRecord> entry : changedADeckRecords
                .entrySet()) {
            RecordKey rk = entry.getKey();
            ADeckRecord srec = entry.getValue();

            /*
             * editType is for each modified record and should be set when
             * making edit action on the record
             */
            RecordEditType editType = RecordEditType.MODIFY;
            if (srec.getId() <= 0) {
                editType = RecordEditType.NEW;
            }

            ADeckRecord erec = AtcfDataUtil.getADeckRecord(storm, rk,
                    sandboxID);

            /*
             * Only save changed records.
             */
            if (editType == RecordEditType.NEW || isRecordChanged(erec, srec)) {

                ModifiedDeckRecord mdr = new ModifiedDeckRecord();
                mdr.setEditType(editType);
                mdr.setRecord(srec);
                modifiedRecords.add(mdr);

                updatedDtgs.add(rk.getDtg());
            }
        }

        // batch update into the sandbox
        if (!modifiedRecords.isEmpty()) {
            successfulSave = AtcfDataUtil.updateDeckRecords(sandboxID,
                    AtcfDeckType.A, modifiedRecords);
        }

        if (successfulSave) {
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

            // Update info at the top and clear up.
            updateAidInfoList();
            changedADeckRecords.clear();

            // Redraw aids if currently displayed
            AtcfVizUtil.redrawAids(storm);

        } else {
            logger.error(
                    "EnterObjAidDialog: Deck data was not saved successfully.");
        }

        return successfulSave;
    }

    /**
     * Implement the "start over" functionality. Saved sandbox deck data should
     * be discarded and refreshed with baseline data.
     */
    @Override
    protected void restartData() {
        sandboxID = AtcfDataUtil.checkoutADeck(storm);
        updateData();

    }

    /**
     * If Save button is enabled (indicating that user has made a change), save
     * changes to sandbox before submitting
     */
    @Override
    protected void submitAction() {
        if (saveButton.isEnabled()) {
            saveChanges();
        }
        super.submitAction();
    }
}
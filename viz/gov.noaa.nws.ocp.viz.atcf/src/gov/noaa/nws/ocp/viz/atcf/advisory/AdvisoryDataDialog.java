/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.advisory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.raytheon.viz.ui.dialogs.ICloseCallback;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.MaxWindGustPairs;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ForecastTrackRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.RecordKey;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.aids.AtcfTaus;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Dialog for Advisory=>Advisory Composition=>Advisory Data.
 *
 * This function displays the advisory's content in a table to allow the
 * forecasters to check the data for consistency.
 *
 * Should the forecast for a particular TAU need to be added or changed, the
 * forecaster simply clicks on the button labeled with the forecast period (TAU)
 * forecast period. The forecaster may then enter/change the forecast position,
 * max wind, gust, dir, speed, and wind radii information. TAU"s that have had
 * something entered or changed are flagged with a "*" in the table next to the
 * TAU column.
 *
 * Clicking on Ok will close the dialog. The data is not actually saved until Ok
 * is clicked on in the "Advisory Composition" dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 24, 2020 77847      jwu         Initial creation.
 * May 28, 2020 78027      jwu         Added more detailed checks.
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Mar 17, 2021 88583      jwu         Implement special advisory.
 * Apr 22, 2021 88729      jwu         Revise storm type threshold.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class AdvisoryDataDialog extends OcpCaveChangeTrackDialog {

    // Names/units for forecast information parameters
    private static final String[] aidParameters = new String[] { " TAU\n  hrs",
            "Verify\ndy/hr", "Lat\ndeg", "Lon\ndeg", "Dir\n  ", "Spd\n  ",
            "Wind\n kts", "Gust\n kts", "Value\n  ",
            "             Radii (nm)    \n   NE    SE    SW    NW     " };

    // Directions/units for forecast radii
    private static final String[] QUADRANT_LABELS = new String[] { "NE (nm)", "SE (nm)", "SW (nm)",
            "NW (nm)" };

    // Number of radii quadrants.
    private static final int RADII_QUADRANTS = QUADRANT_LABELS.length;

    // Format to list forecast track record information.
    private static String seasFormat = "%3d%56dft%9d%4d%4d%4d";

    private static String recFormat = "%3d%9s%6.1f%s%6.1f%s%6d%6d%7d%8d%6dkt%9d%4d%4d%4d";

    private static String calFormat = "%02d/%02d";

    // Number to indicate 12 ft seas forecast.
    private static final int RAD_WAVE = 12;

    // Maximum radii.
    private static final int MAXIMUM_WIND_RADII = 995;

    // Number of visible items in forecast record list at bottom.
    private static final int FCST_LIST_ITEMS = 6;

    // Current storm && DTG.
    private Storm storm;

    private String currentDTG;

    // Current TAU
    private int currentTauHr = AtcfTaus.TAU3.getValue();

    // Label at the top to show the current TAU
    private Label currentTauLbl;

    // TAUs
    private java.util.List<AtcfTaus> workingTaus;

    private java.util.List<Integer> workingTauHrs;

    // Special TAU if "Special Advisory" is On.
    private int specialTau = 3;

    // Buttons for each TAU
    private Map<Integer, Button> tauHrBtns;

    // Widgets to show forecast record info.
    private Text latTxt;

    private Button latNorthBtn;

    private Button lonWestBtn;

    private Text lonTxt;

    private CCombo maxWindCmb;

    private Text gustTxt;

    private Text dirTxt;

    private Text speedTxt;

    private CCombo developCmb;

    // List for current forecasts.
    private List fcstRecordList;

    // Advisory data.
    private IAdvisoryDataListener advData;

    // Buttons and CCombos for wind radii.
    private Map<WindRadii, java.util.List<CCombo>> radiiValueCmbs;

    // Items for wind radii CComboes
    private static String[] radiiEntries = AtcfVizUtil.getRadiiEntries();

    // Items for max wind CCombo
    private static String[] maxWindEntries = AtcfVizUtil.getMaxWindEntries();

    // Verify listeners.
    private final AtcfTextListeners verifyListener = new AtcfTextListeners();

    // Working copy of forecast track records sorted by RecordKey.
    private Map<RecordKey, ForecastTrackRecord> fcstTrackRecordMap;

    // Callback when the dialog is closed.
    private ICloseCallback dlgCbk;

    // Flag to indicate changes have been updated to Advisory Composition.
    private boolean applied;

    // Items for storm development
    private java.util.List<String> developItems;

    // Maximum wind and gust conversion pairs.
    private MaxWindGustPairs gustConversion;

    // Listener for lat/lon changes etc.
    private ModifyListener txtModifyListener;

    // Listener for max wind changes.
    private ModifyListener windModifyListener;

    /*
     * Flag to add records when max wind reaches a threshold, regardless of if
     * wind radii exists.
     */
    private boolean addRecordWithMaxWind = true;

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     */
    public AdvisoryDataDialog(Shell parent, Storm curStorm) {
        super(parent);
        this.storm = curStorm;
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent shell
     * @param advData
     *            Advisory data
     */
    public AdvisoryDataDialog(Shell parent, IAdvisoryDataListener advData) {
        // Use an SWT.PRIMARY_MODAL dialog.
        super(parent, OCP_DIALOG_MODAL_STYLE, OCP_DIALOG_MODAL_CAVE_STYLE);

        if (advData == null) {
            throw new IllegalArgumentException("advData must be non-null");
        }

        this.advData = advData;
        this.dlgCbk = dlgCloseCallBack();
        this.applied = false;
        this.addCloseCallback(dlgCbk);

    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        initialize();
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite mainComp = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainCompGL = new GridLayout(1, false);
        mainCompGL.verticalSpacing = 15;
        mainComp.setLayout(mainCompGL);
        GridData mainGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        mainComp.setLayoutData(mainGD);

        // Create TAU label and buttons
        createTauComp(mainComp);

        // Create widgets for lat/lon/max wind/spd/dir/development.
        createBasicInfoComp(mainComp);

        // Create the section showing 12 ft seas and 34/50/64 kt radii.
        createRadiiInfoComp(mainComp);

        // Create the list to show all forecast records.
        createForecastListComp(mainComp);

        // Create control buttons.
        createCtrlBtns(mainComp);

        // Listener for input changes.
        txtModifyListener = txtModifyListener();
        windModifyListener = windModifyListener();

        // Populate lat/lon/max wind/spd/dir/development & all radii.
        populateForecastInfo(currentTauHr);

        // List all forecast records.
        updateForecastList();

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /*
     * Create widgets for TAU label and buttons.
     *
     * @param parent
     */
    private void createTauComp(Composite parent) {

        currentTauLbl = new Label(parent, SWT.CENTER);
        currentTauLbl.setText(String.format("TAU %-3d  ", currentTauHr));
        GridData mainLblGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        currentTauLbl.setLayoutData(mainLblGD);

        // Group to select TAUs and show forecast info.
        Group tauBtnComp = new Group(parent, SWT.NONE);

        GridLayout rightCompGL = new GridLayout(2, false);
        tauBtnComp.setLayout(rightCompGL);
        tauBtnComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.NONE, false, false));

        Label tauLbl = new Label(tauBtnComp, SWT.NONE);
        tauLbl.setText("TAU:");
        tauLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));

        // Group for all TAUs
        Composite tauComp = new Composite(tauBtnComp, SWT.NONE);

        GridLayout tauCompGL = new GridLayout(workingTaus.size(), true);
        tauComp.setLayout(tauCompGL);

        tauComp.setLayoutData(new GridData(SWT.CENTER, SWT.NONE, false, false));

        tauHrBtns = new HashMap<>();
        for (int tau : workingTauHrs) {
            Button tauBtn = createTauBtn(tauComp, tau);
            tauHrBtns.put(tau, tauBtn);
        }

        // Set TAU buttons' status.
        enableTauBtns();
    }

    /*
     * Create widgets for current lat/lon/max wind/spd/dir/development.
     *
     * @param parent
     */
    private void createBasicInfoComp(Composite parent) {
        final String knotsLabel = "knots";
        Composite currentInfoComp = new Composite(parent, SWT.BORDER);
        GridLayout currentInfoLayout = new GridLayout(2, false);
        currentInfoLayout.horizontalSpacing = 20;
        currentInfoLayout.marginHeight = 10;
        currentInfoComp.setLayout(currentInfoLayout);
        GridData currentInfoCompGD = new GridData(SWT.CENTER, SWT.NONE, false,
                false);
        currentInfoComp.setLayoutData(currentInfoCompGD);

        Composite leftComp = new Composite(currentInfoComp, SWT.NONE);
        GridLayout leftGL = new GridLayout(2, false);
        leftGL.marginHeight = 0;
        leftGL.verticalSpacing = 0;
        leftComp.setLayout(leftGL);
        GridData leftGD = new GridData(SWT.LEFT, SWT.NONE, false, false);
        leftComp.setLayoutData(leftGD);

        Label latLbl = new Label(leftComp, SWT.NONE);
        latLbl.setText("Latitude");
        GridData latLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        latLbl.setLayoutData(latLblGD);

        SelectionAdapter updateAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateForTau(currentTauHr, -1);
            }
        };

        Composite currentLatComp = new Composite(leftComp, SWT.NONE);
        GridLayout currentLatGL = new GridLayout(2, false);
        currentLatGL.marginHeight = 0;
        currentLatComp.setLayout(currentLatGL);
        GridData currentLatCompGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        currentLatComp.setLayoutData(currentLatCompGD);

        latTxt = new Text(currentLatComp, SWT.BORDER);
        GridData currentLatTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                false);
        latTxt.setLayoutData(currentLatTxtGD);
        latTxt.addListener(SWT.Verify, verifyListener.getLatVerifyListener1());

        Composite latDirComp = new Composite(currentLatComp, SWT.NONE);
        GridLayout latDirCompGL = new GridLayout(2, true);
        latDirCompGL.marginHeight = 0;
        latDirComp.setLayout(latDirCompGL);
        GridData latDirCompGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        latDirComp.setLayoutData(latDirCompGD);

        latNorthBtn = new Button(latDirComp, SWT.RADIO);
        latNorthBtn.setText("N");
        latNorthBtn.setSelection(true);
        GridData latNorthBtnGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                false);
        latNorthBtn.setLayoutData(latNorthBtnGD);

        latNorthBtn.addSelectionListener(updateAdapter);

        Button latSouthBtn = new Button(latDirComp, SWT.RADIO);
        latSouthBtn.setText("S");
        GridData latSouthBtnGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                false);
        latSouthBtn.setLayoutData(latSouthBtnGD);
        latSouthBtn.addSelectionListener(updateAdapter);

        // Max wind
        Label maxWindLbl = new Label(leftComp, SWT.NONE);
        maxWindLbl.setText("Max Wind");
        GridData maxWindLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false);
        maxWindLbl.setLayoutData(maxWindLblGD);

        Composite maxWindComp = new Composite(leftComp, SWT.NONE);
        GridLayout maxWindGL = new GridLayout(2, false);
        maxWindGL.marginHeight = 0;
        maxWindComp.setLayout(maxWindGL);
        GridData maxWindCompGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        maxWindComp.setLayoutData(maxWindCompGD);

        maxWindCmb = new CCombo(maxWindComp, SWT.BORDER);
        GridData currentMaxWindCmbGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                false);
        maxWindCmb.setLayoutData(currentMaxWindCmbGD);
        maxWindCmb.setItems(maxWindEntries);

        maxWindCmb.addListener(SWT.Verify,
                verifyListener.getWindTextVerifyListener());
        Label maxWindUnitLbl = new Label(maxWindComp, SWT.NONE);
        maxWindUnitLbl.setText(knotsLabel);
        GridData maxWindUnitLblGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        maxWindUnitLbl.setLayoutData(maxWindUnitLblGD);

        // Direction
        Label dirLbl = new Label(leftComp, SWT.NONE);
        dirLbl.setText("Direction");
        GridData dirLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        dirLbl.setLayoutData(dirLblGD);

        Composite dirComp = new Composite(leftComp, SWT.NONE);
        GridLayout dirCompGL = new GridLayout(2, false);
        dirCompGL.marginHeight = 0;
        dirComp.setLayout(dirCompGL);
        GridData dirCompGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        dirComp.setLayoutData(dirCompGD);

        dirTxt = new Text(dirComp, SWT.BORDER);
        GridData dirTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        dirTxt.setLayoutData(dirTxtGD);

        Label dirUnitLbl = new Label(dirComp, SWT.NONE);
        dirUnitLbl.setText("degrees");
        GridData dirUnitLblGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        dirUnitLbl.setLayoutData(dirUnitLblGD);

        // Direction
        Label developLbl = new Label(leftComp, SWT.NONE);
        developLbl.setText("Development");
        GridData developLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false);
        developLbl.setLayoutData(developLblGD);

        Composite developComp = new Composite(leftComp, SWT.NONE);
        GridLayout developCompGL = new GridLayout(1, false);
        developCompGL.marginHeight = 0;
        developComp.setLayout(dirCompGL);
        GridData developCompGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        developComp.setLayoutData(developCompGD);

        developCmb = new CCombo(developComp, SWT.BORDER);
        developCmb.setItems(StormDevelopment.getStormTypeStrings());
        GridData devCmbGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        developCmb.setLayoutData(devCmbGD);
        developCmb.setEditable(false);
        developCmb.addSelectionListener(updateAdapter);

        // Longitude.....
        Composite rightComp = new Composite(currentInfoComp, SWT.NONE);
        GridLayout rightGL = new GridLayout(2, false);
        rightGL.marginHeight = 0;
        rightGL.verticalSpacing = 0;
        rightComp.setLayout(rightGL);
        GridData rightGD = new GridData(SWT.LEFT, SWT.NONE, false, false);
        rightComp.setLayoutData(rightGD);

        Label lonLbl = new Label(rightComp, SWT.NONE);
        lonLbl.setText("Longitude");
        GridData lonLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        lonLbl.setLayoutData(lonLblGD);

        Composite currentLonComp = new Composite(rightComp, SWT.NONE);
        GridLayout currentLonGL = new GridLayout(2, false);
        currentLonGL.marginHeight = 0;
        currentLonComp.setLayout(currentLonGL);
        GridData currentLonCompGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        currentLonComp.setLayoutData(currentLonCompGD);

        lonTxt = new Text(currentLonComp, SWT.BORDER);
        GridData currentLonTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                false);
        lonTxt.setLayoutData(currentLonTxtGD);

        lonTxt.addListener(SWT.Verify, verifyListener.getLonVerifyListener1());

        Composite lonDirComp = new Composite(currentLonComp, SWT.NONE);
        GridLayout lonDirCompGL = new GridLayout(2, true);
        lonDirCompGL.marginHeight = 0;
        lonDirComp.setLayout(lonDirCompGL);
        GridData lonDirCompGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        lonDirComp.setLayoutData(lonDirCompGD);

        Button lonEastBtn = new Button(lonDirComp, SWT.RADIO);
        lonEastBtn.setText("E");
        GridData lonEastBtnGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        lonEastBtn.setLayoutData(lonEastBtnGD);
        lonEastBtn.addSelectionListener(updateAdapter);

        lonWestBtn = new Button(lonDirComp, SWT.RADIO);
        lonWestBtn.setText("W");
        lonWestBtn.setSelection(true);
        GridData lonWestBtnGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        lonWestBtn.setLayoutData(lonWestBtnGD);
        lonWestBtn.addSelectionListener(updateAdapter);

        // Gust
        Label gustLbl = new Label(rightComp, SWT.NONE);
        gustLbl.setText("Gust");
        GridData gustLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gustLbl.setLayoutData(gustLblGD);

        Composite gustComp = new Composite(rightComp, SWT.NONE);
        GridLayout gustGL = new GridLayout(2, false);
        gustGL.marginHeight = 0;
        gustComp.setLayout(gustGL);
        GridData gustCompGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gustComp.setLayoutData(gustCompGD);

        gustTxt = new Text(gustComp, SWT.BORDER);
        GridData gustTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        gustTxt.setLayoutData(gustTxtGD);

        Label gustUnitLbl = new Label(gustComp, SWT.NONE);
        gustUnitLbl.setText(knotsLabel);
        GridData gustUnitLblGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        gustUnitLbl.setLayoutData(gustUnitLblGD);

        // Speed
        Label speedLbl = new Label(rightComp, SWT.NONE);
        speedLbl.setText("Speed");
        GridData speedLblGD = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        speedLbl.setLayoutData(speedLblGD);

        Composite speedComp = new Composite(rightComp, SWT.NONE);
        GridLayout speedCompGL = new GridLayout(2, false);
        speedCompGL.marginHeight = 0;
        speedComp.setLayout(gustGL);
        GridData speedCompGD = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        speedComp.setLayoutData(speedCompGD);

        speedTxt = new Text(speedComp, SWT.BORDER);
        GridData speedTxtGD = new GridData(SWT.LEFT, SWT.CENTER, true, false);
        speedTxt.setLayoutData(speedTxtGD);

        Label speedUnitLbl = new Label(speedComp, SWT.NONE);
        speedUnitLbl.setText(knotsLabel);
        GridData speedUnitLblGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        speedUnitLbl.setLayoutData(speedUnitLblGD);

    }

    /*
     * Creates the section showing 12 ft seas and 34/50/64 kt radii.
     *
     * @param parent
     */
    protected void createRadiiInfoComp(Composite parent) {

        Composite radiiComp = new Composite(parent, SWT.BORDER);
        GridLayout radiiCompGL = new GridLayout(5, false);
        radiiCompGL.marginWidth = 15;
        radiiCompGL.marginHeight = 10;
        radiiCompGL.horizontalSpacing = 45;
        radiiComp.setLayout(radiiCompGL);
        GridData radiiCompGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        radiiComp.setLayoutData(radiiCompGD);

        // Row for labeling 12 ft seas & 34kt/50kt/64kt radii
        Label spaceLbl = new Label(radiiComp, SWT.NONE);
        spaceLbl.setText("");
        GridData lblGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        spaceLbl.setLayoutData(lblGD);

        for (String label : QUADRANT_LABELS) {
            Label dirLbl = new Label(radiiComp, SWT.NONE);
            dirLbl.setText(label);
        }

        // Row for 12 ft seas & 34kt/50kt/64kt wind radii
        for (WindRadii wndRad : WindRadii.values()) {
            Label ktLbl = new Label(radiiComp, SWT.NONE);
            if (wndRad == WindRadii.RADII_0_KNOT) {
                ktLbl.setText("12 ft seas");
            } else {
                ktLbl.setText(wndRad.getName() + " winds");
            }

            for (int kk = 0; kk < RADII_QUADRANTS; kk++) {
                CCombo ktCombo = new CCombo(radiiComp, SWT.NONE);
                ktCombo.setItems(radiiEntries);
                ktCombo.select(0);
                ktCombo.setData(kk);
                GridData radiiDropGD = new GridData(SWT.CENTER, SWT.NONE, false,
                        false);
                ktCombo.setLayoutData(radiiDropGD);
                radiiValueCmbs.get(wndRad).add(ktCombo);

                ktCombo.addVerifyListener(
                        verifyListener.getRadiiCmbVerifyListener());

                ktCombo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        updateForTau(currentTauHr, -1);
                    }
                });

            }
        }
    }

    /*
     * Create the composite list all forecast info.
     *
     * @param parent
     */
    private void createForecastListComp(Composite parent) {

        Font txtFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);

        Composite fcstInfoComp = new Composite(parent, SWT.BORDER);
        GridLayout fcstInfoGL = new GridLayout(aidParameters.length, false);
        fcstInfoGL.marginHeight = 15;
        fcstInfoGL.horizontalSpacing = 30;
        fcstInfoComp.setLayout(fcstInfoGL);
        fcstInfoComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        for (String param : aidParameters) {
            Label infoLbl = new Label(fcstInfoComp, SWT.NONE);
            infoLbl.setText(param);
            GridData infoLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                    false);
            infoLbl.setLayoutData(infoLblData);
        }

        fcstRecordList = new List(fcstInfoComp,
                SWT.NONE | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        fcstRecordList.setFont(txtFont);

        int listHeight = FCST_LIST_ITEMS * fcstRecordList.getItemHeight();
        GridData aidRecListData = new GridData(SWT.DEFAULT, listHeight);
        aidRecListData.horizontalAlignment = SWT.FILL;
        aidRecListData.verticalAlignment = SWT.FILL;
        aidRecListData.horizontalSpan = aidParameters.length;
        fcstRecordList.setLayoutData(aidRecListData);
    }

    /*
     * Creates the save, submit, and close buttons.
     *
     * @param parent
     */
    private void createCtrlBtns(Composite parent) {
        GridLayout ctrlBtnCompGL = new GridLayout(2, true);
        ctrlBtnCompGL.horizontalSpacing = 60;
        ctrlBtnCompGL.marginWidth = 60;
        ctrlBtnCompGL.marginTop = 0;

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        ctrlBtnComp.setLayout(ctrlBtnCompGL);
        ctrlBtnComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        // Button to send changes back and exits
        Button submitBtn = new Button(ctrlBtnComp, SWT.NONE);
        submitBtn.setText("Ok");
        submitBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        submitBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // Update forecast info before switching to the new TAU.
                updateRecordsForTau(currentTauHr);

                // Send data back to Advisory Composition.
                advData.advisoryDataChanged(fcstTrackRecordMap, true);
                applied = true;

                close();
            }
        });

        // Button to exit without passing info back.
        Button closeBtn = new Button(ctrlBtnComp, SWT.NONE);
        closeBtn.setText("Cancel");
        closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Update forecast info before switching to the new TAU.
                updateRecordsForTau(currentTauHr);

                // Send data back to Advisory Composition.
                advData.advisoryDataChanged(fcstTrackRecordMap, false);

                close();
            }
        });

    }

    /*
     * Initialize maps and data.
     */
    private boolean initialize() {

        boolean hasForecast = true;

        // Items for storm development
        if (developItems == null) {
            developItems = new ArrayList<>();
            Collections.addAll(developItems,
                    StormDevelopment.getStormShortIntensityStrings());

        }

        // Load gust conversion table
        gustConversion = AtcfConfigurationManager.getInstance()
                .getMaxWindGustPairs();

        // Maps of buttons and CCombos for 12 ft seas & all wind radii.
        radiiValueCmbs = new EnumMap<>(WindRadii.class);
        radiiValueCmbs.put(WindRadii.RADII_0_KNOT, new ArrayList<>());
        radiiValueCmbs.put(WindRadii.RADII_34_KNOT, new ArrayList<>());
        radiiValueCmbs.put(WindRadii.RADII_50_KNOT, new ArrayList<>());
        radiiValueCmbs.put(WindRadii.RADII_64_KNOT, new ArrayList<>());

        fcstTrackRecordMap = new HashMap<>();

        storm = advData.getStorm();
        currentDTG = advData.getCurrentDTG();
        workingTaus = advData.getWorkingTaus();

        specialTau = advData.getSpecialTau();
        currentTauHr = specialTau;

        Map<String, java.util.List<ForecastTrackRecord>> fcstTrackData = advData
                .getFcstTrackData();

        /*
         * Make a local copy of forecast track records and sort into a
         * RecordKey map.
         */
        for (java.util.List<ForecastTrackRecord> frecs : fcstTrackData
                .values()) {
            for (ForecastTrackRecord rec : frecs) {
                ForecastTrackRecord nrec = new ForecastTrackRecord(rec);
                int windRad = (int) rec.getRadWind();
                RecordKey key = new RecordKey("FCST", currentDTG,
                        rec.getFcstHour(), windRad);
                fcstTrackRecordMap.put(key, nrec);
            }
        }

        // Forecast hours - check for special TAU!
        workingTauHrs = new ArrayList<>();
        for (AtcfTaus tau : workingTaus) {
            int tauHr = tau.getValue();

            if (tauHr > 0 && tauHr < 9) {
                tauHr = specialTau;
            }

            workingTauHrs.add(tauHr);
        }

        // Update dialog title.
        setText("NHC Advisory Data - " + storm.getStormName() + " "
                + storm.getStormId() + " - " + currentDTG.substring(4));

        return hasForecast;
    }

    /*
     * Update records for a given TAU, switch to a new TAU if given, & update
     * GUI.
     *
     * @param prevTau Tau before switching
     *
     * @param curTau Tau to be switched on (if >= 0)
     */
    private void updateForTau(int prevTau, int nextTau) {

        // Update forecast info before switching to the new TAU.
        updateRecordsForTau(prevTau);

        if (nextTau >= 0) {
            // Update forecast info from the new TAU.
            currentTauLbl.setText(String.format("TAU %-3d  ", nextTau));

            // Update basic forecast info from the new TAU.
            populateForecastInfo(nextTau);
        }

        // Update forecast info in the list from the new TAU.
        updateForecastList();
    }

    /*
     * Update records for the selected TAU from GUI.
     *
     * @param tau TAU to be updated
     */
    private void updateRecordsForTau(int tau) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);

        /*
         * Check if we need to create a record for special TAU. This allows the
         * user to manually create forecast record for 12 ft seas wave forecast.
         */
        ForecastTrackRecord recTau3 = null;
        if (rec34 == null && tau == specialTau) {
            recTau3 = checkSpecialTauRecord(tau, true);
            // New record for TAU 3 at 34 kt is created.
            if (recTau3 != null) {
                RecordKey rkey = new RecordKey("FCST", currentDTG,
                        recTau3.getFcstHour(),
                        WindRadii.RADII_34_KNOT.getValue());
                fcstTrackRecordMap.put(rkey, recTau3);
                rec34 = recTau3;
            }
        }

        // Update records for previous TAU using input from GUI.
        if (rec34 != null) {
            float lat = AtcfVizUtil.getStringAsFloat(latTxt.getText(),
                    !latNorthBtn.getSelection(), true, rec34.getClat());
            float lon = AtcfVizUtil.getStringAsFloat(lonTxt.getText(),
                    lonWestBtn.getSelection(), true, rec34.getClon());
            float maxWnd = AtcfVizUtil.getStringAsFloat(maxWindCmb.getText(),
                    false, true, rec34.getWindMax());
            float gust = AtcfVizUtil.getStringAsFloat(gustTxt.getText(), false,
                    true, rec34.getGust());
            float dir = AtcfVizUtil.getStringAsFloat(dirTxt.getText(), false,
                    true, rec34.getStormDrct());
            float spd = AtcfVizUtil.getStringAsFloat(speedTxt.getText(), false,
                    true, rec34.getStormSped());

            String develop = developCmb.getItem(developCmb.getSelectionIndex());
            String intensity = StormDevelopment
                    .getShortIntensityString(develop);

            // Update records with new data.
            updateRecords(tau, lat, lon, maxWnd, gust, dir, spd, intensity);

            // Copy 12-ft sea wave radii in special TAU into TAU 0.
            if (tau == specialTau) {
                ForecastTrackRecord tau0R34 = getForecastByTauNRadii(0,
                        WindRadii.RADII_34_KNOT);
                if (tau0R34 != null) {
                    tau0R34.setQuad1WaveRad(rec34.getQuad1WaveRad());
                    tau0R34.setQuad2WaveRad(rec34.getQuad2WaveRad());
                    tau0R34.setQuad3WaveRad(rec34.getQuad3WaveRad());
                    tau0R34.setQuad4WaveRad(rec34.getQuad4WaveRad());

                    ForecastTrackRecord tau0R50 = getForecastByTauNRadii(0,
                            WindRadii.RADII_50_KNOT);
                    if (tau0R50 != null) {
                        tau0R50.setQuad1WaveRad(rec34.getQuad1WaveRad());
                        tau0R50.setQuad2WaveRad(rec34.getQuad2WaveRad());
                        tau0R50.setQuad3WaveRad(rec34.getQuad3WaveRad());
                        tau0R50.setQuad4WaveRad(rec34.getQuad4WaveRad());

                        ForecastTrackRecord tau0R64 = getForecastByTauNRadii(0,
                                WindRadii.RADII_64_KNOT);
                        if (tau0R64 != null) {
                            tau0R64.setQuad1WaveRad(rec34.getQuad1WaveRad());
                            tau0R64.setQuad2WaveRad(rec34.getQuad2WaveRad());
                            tau0R64.setQuad3WaveRad(rec34.getQuad3WaveRad());
                            tau0R64.setQuad4WaveRad(rec34.getQuad4WaveRad());
                        }
                    }
                }
            }
        }
    }

    /*
     * Update records for the selected TAU from GUI.
     *
     * Note: records for 34/60 kt may be added or deleted based on max wind.
     *
     * @param tau tau for the records
     *
     * @param lat Lat for the records
     *
     * @param lon Lon for the records
     *
     * @param maxWnd Max wind for the records
     *
     * @param gust Gust for the records
     *
     * @param dir Wind Direction for the records
     *
     * @param spd Wind Speed for the records
     *
     * @param intensity Storm development status for the records
     *
     */
    private void updateRecords(int tau, float lat, float lon, float maxWnd,
            float gust, float dir, float spd, String intensity) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);
        ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                WindRadii.RADII_50_KNOT);
        ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                WindRadii.RADII_64_KNOT);

        // Add or delete records based on max wind.
        if (rec34 != null) {
            int radii50 = WindRadii.RADII_50_KNOT.getValue();
            int radii64 = WindRadii.RADII_64_KNOT.getValue();
            RecordKey key50 = new RecordKey("FCST", currentDTG,
                    rec34.getFcstHour(), radii50);
            RecordKey key64 = new RecordKey("FCST", currentDTG,
                    rec34.getFcstHour(), radii64);

            /*
             * Delete for 64 kt if max wind < 64; add if max wind >= 64 at least
             * one wind radii for 64 kt > 0.
             */
            if (maxWnd >= radii64) {
                if (rec64 == null) {
                    // TODO --- check if this logic is needed.
                    float[] wndRad = getRadiiValue(WindRadii.RADII_64_KNOT);
                    boolean add = false;
                    for (float rad : wndRad) {
                        if (rad > 0.0F) {
                            add = true;
                            break;
                        }
                    }

                    if (add || addRecordWithMaxWind) {
                        ForecastTrackRecord rec64New = new ForecastTrackRecord(
                                rec34);
                        rec64New.setRadWind(radii64);
                        fcstTrackRecordMap.put(key64, rec64New);
                    }
                }
            } else {
                if (rec64 != null) {
                    fcstTrackRecordMap.remove(key64);
                }

                /*
                 * Delete for 50 kt if max wind < 50; add if max wind >= 50 at
                 * least one wind radii for 50 kt > 0.
                 */
                if (maxWnd >= radii50) {
                    if (rec50 == null) {
                        // TODO --- check if this logic is needed?
                        float[] wndRad = getRadiiValue(WindRadii.RADII_50_KNOT);
                        boolean add = false;
                        for (float rad : wndRad) {
                            if (rad > 0.0F) {
                                add = true;
                                break;
                            }
                        }

                        if (add || addRecordWithMaxWind) {
                            ForecastTrackRecord rec50New = new ForecastTrackRecord(
                                    rec34);
                            rec50New.setRadWind(radii50);
                            fcstTrackRecordMap.put(key50, rec50New);
                        }
                    }
                } else {
                    if (rec50 != null) {
                        fcstTrackRecordMap.remove(key50);
                    }
                }
            }
        }

        // Now update values from GUI.
        for (WindRadii radii : WindRadii.values()) {
            int radiiValue = radii.getValue();
            if (radiiValue > 0) {
                ForecastTrackRecord rec = getForecastByTauNRadii(tau, radii);
                if (rec != null) {
                    rec.setClat(lat);
                    rec.setClon(lon);
                    rec.setWindMax(maxWnd);
                    rec.setGust(gust);
                    rec.setStormDrct(dir);
                    rec.setStormSped(spd);
                    rec.setIntensity(intensity);

                    for (CCombo cmb : radiiValueCmbs.get(radii)) {
                        updateWindRadiiValue(rec, cmb, false);
                    }

                    // Update 12 ft seas radii for TAU 3.
                    if (tau == specialTau) {
                        for (CCombo cmb : radiiValueCmbs
                                .get(WindRadii.RADII_0_KNOT)) {
                            updateWindRadiiValue(rec, cmb, true);
                        }

                        int radWave = (int) rec.getRadWave();
                        if (radWave != RAD_WAVE) {
                            rec.setRadWave(RAD_WAVE);
                        }
                    }
                }
            }
        }
    }

    /*
     * Check if we need to create a record for special TAU. This allows the user
     * to manually create forecast records for 12 ft seas wave forecast.
     *
     * Note: A Special TAU record is created based on TAU 0 record. If another
     * TAU after the special TAU has forecast, the direction/speed/location are
     * interpolated between that TAU and TAU 0.
     *
     * @param prevTau Tau
     *
     * @param checkRadii Flag to check radii.
     */
    private ForecastTrackRecord checkSpecialTauRecord(int tau,
            boolean checkRadii) {

        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);

        if (rec34 == null && tau == specialTau) {

            // Check if at least one wave radii for 12 ft seas > 0.
            boolean add = true;
            float[] wndRad = getRadiiValue(WindRadii.RADII_0_KNOT);
            if (checkRadii) {
                add = false;
                for (float rad : wndRad) {
                    if (rad > 0.0F) {
                        add = true;
                        break;
                    }
                }
            }

            if (add) {
                ForecastTrackRecord tau0Rec34 = getForecastByTauNRadii(0,
                        WindRadii.RADII_34_KNOT);

                if (tau0Rec34 != null) {

                    // Find record for next TAU after special TAU.
                    ForecastTrackRecord rec34NextTau = null;
                    for (int tauHr : tauHrBtns.keySet()) {
                        if (tauHr > specialTau) {
                            rec34NextTau = getForecastByTauNRadii(tauHr,
                                    WindRadii.RADII_34_KNOT);
                            if (rec34NextTau != null) {
                                break;
                            }
                        }
                    }

                    // Create a record by interpolation.
                    int[] fcstHrs = new int[] { tau };
                    java.util.List<ForecastTrackRecord> recs = AtcfVizUtil
                            .interpolateRecord(tau0Rec34, rec34NextTau, fcstHrs,
                                    true);

                    rec34 = recs.get(0);
                    rec34.setFcstHour(specialTau);
                    rec34.setRadWave(RAD_WAVE);

                    // Linear interpolation max wind.
                    if (rec34NextTau != null) {
                        float wnd0 = tau0Rec34.getWindMax();
                        float wnd1 = rec34NextTau.getWindMax();
                        float wnd = wnd0 + (wnd1 - wnd0)
                                / rec34NextTau.getFcstHour() * specialTau;
                        wnd = AtcfDataUtil.roundToNearest(wnd, 5);
                        rec34.setWindMax(wnd);
                        rec34.setGust(gustConversion.getGustValue((int) wnd));
                    }
                }

                // Create/add new records for special TAU.
                createSpecialTauRecords(rec34);
            }
        }

        return rec34;
    }

    /*
     * Create a button for the specified forecast hour (TAU).
     *
     * @param tauComp Composite holding all TAU buttons.
     *
     * @param tau AtcfTaus.
     *
     * return Button
     */
    private Button createTauBtn(Composite tauComp, int tau) {

        Button tauBtn = new Button(tauComp, SWT.PUSH);
        tauBtn.setText(Integer.toString(tau));
        GridData tauBtnGD = new GridData(SWT.CENTER, SWT.NONE, false, false);
        tauBtn.setLayoutData(tauBtnGD);
        tauBtn.setData(tau);

        tauBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // Check if there are any missing wind radii.
                boolean edit = checkWindRadii(currentTauHr);

                /*
                 * Switch if no missing wind radii or the user decides to switch
                 * without editing missing wind radii.
                 */
                if (!edit) {
                    int preTau = currentTauHr;
                    currentTauHr = (int) e.widget.getData();

                    Button currentBtn = (Button) e.widget;
                    if (preTau != currentTauHr) {
                        for (Button btn : tauHrBtns.values()) {
                            if (btn == currentBtn) {
                                AtcfVizUtil.setActiveButtonBackground(btn);
                            } else {
                                AtcfVizUtil.setDefaultBackground(btn);
                            }
                        }
                    }

                    updateForTau(preTau, currentTauHr);
                }
            }
        });

        if (tau == currentTauHr) {
            tauBtn.setSelection(true);
            AtcfVizUtil.setActiveButtonBackground(tauBtn);
        } else {
            AtcfVizUtil.setDefaultBackground(tauBtn);
        }

        return tauBtn;
    }

    /*
     * Enable TAU buttons for TAU 3 or TAUs having records.
     */
    private void enableTauBtns() {
        for (Map.Entry<Integer, Button> entry : tauHrBtns.entrySet()) {
            ForecastTrackRecord rec34 = getForecastByTauNRadii(entry.getKey(),
                    WindRadii.RADII_34_KNOT);
            entry.getValue().setEnabled(
                    (rec34 != null) || entry.getKey() == specialTau);
        }
    }

    /*
     * Populate forecast panel with existing forecast data.
     *
     * @param tau AtcfTaus
     */
    private void populateForecastInfo(int tau) {

        removeModifyListeners();

        // Get Forecast Records.
        ForecastTrackRecord rec34 = getForecastByTauNRadii(tau,
                WindRadii.RADII_34_KNOT);

        // Reset.
        resetInfo();

        // For TAU 3, if no forecast yet, create records to populate GUI.
        if (tau == specialTau && rec34 == null) {
            rec34 = checkSpecialTauRecord(tau, false);
        }

        // Update radii dropdown status.
        setRadiiStatus(tau, rec34);

        // Set to values in Forecast Records.
        if (rec34 != null) {

            setBasicInfo(rec34);

            // Update 12 ft seas radii for TAU 3.
            int radWave = (int) rec34.getRadWave();
            if (tau == specialTau && radWave == RAD_WAVE) {
                int[] seasRadii = rec34.getWaveRadii();
                for (int ii = 0; ii < RADII_QUADRANTS; ii++) {
                    updateRadiiCombo(
                            radiiValueCmbs.get(WindRadii.RADII_0_KNOT).get(ii),
                            seasRadii[ii]);
                }
            }

            int[] quadRadii34 = rec34.getWindRadii();
            for (int ii = 0; ii < RADII_QUADRANTS; ii++) {
                updateRadiiCombo(
                        radiiValueCmbs.get(WindRadii.RADII_34_KNOT).get(ii),
                        quadRadii34[ii]);
            }

            ForecastTrackRecord rec50 = getForecastByTauNRadii(tau,
                    WindRadii.RADII_50_KNOT);
            if (rec50 != null) {
                int[] quadRadii50 = rec50.getWindRadii();
                for (int ii = 0; ii < RADII_QUADRANTS; ii++) {
                    updateRadiiCombo(
                            radiiValueCmbs.get(WindRadii.RADII_50_KNOT).get(ii),
                            quadRadii50[ii]);
                }

                ForecastTrackRecord rec64 = getForecastByTauNRadii(tau,
                        WindRadii.RADII_64_KNOT);
                if (rec64 != null) {
                    int[] quadRadii64 = rec64.getWindRadii();
                    for (int ii = 0; ii < RADII_QUADRANTS; ii++) {
                        updateRadiiCombo(radiiValueCmbs
                                .get(WindRadii.RADII_64_KNOT).get(ii),
                                quadRadii64[ii]);
                    }
                }
            }
        }

        // Put listeners back.
        addModifyListeners();

    }

    /*
     * Enable/disable wind radii widgets based on max wind speed.
     *
     * @param maxWnd Maximum wind intensity
     */
    private void setWindRadiiStatus(int maxWnd) {

        // TODO As long as we have a record, Radii 34 should be enabled?
        for (CCombo cmb : radiiValueCmbs.get(WindRadii.RADII_34_KNOT)) {
            cmb.setEnabled(true);
        }

        if (maxWnd >= WindRadii.RADII_50_KNOT.getValue()) {
            for (CCombo cmb : radiiValueCmbs.get(WindRadii.RADII_50_KNOT)) {
                cmb.setEnabled(true);
            }

            if (maxWnd >= WindRadii.RADII_64_KNOT.getValue()) {
                for (CCombo cmb : radiiValueCmbs.get(WindRadii.RADII_64_KNOT)) {
                    cmb.setEnabled(true);
                }
            }
        }
    }

    /*
     * Enable/disable all radii widgets based on max wind speed in the record.
     * 12 ft seas radii is only enabled for TAU 3, regardless of wind speed.
     *
     * @param tau AtcfTaus
     *
     * @param rec ForecastTrackRecord
     */
    private void setRadiiStatus(int tau, ForecastTrackRecord rec) {

        for (WindRadii radii : WindRadii.values()) {
            for (CCombo cmb : radiiValueCmbs.get(radii)) {
                cmb.select(0);
                cmb.setEnabled(false);
            }
        }

        // 12 ft seas radii.
        if (tau == specialTau) {
            for (CCombo cmb : radiiValueCmbs.get(WindRadii.RADII_0_KNOT)) {
                cmb.setEnabled(true);
            }
        }

        // 34/50/64 knots wind radii
        if (rec != null) {
            int maxWnd = (int) rec.getWindMax();
            setWindRadiiStatus(maxWnd);
        }
    }

    /*
     * Find the forecast track record stored in the map for a TAU/Wind Radii.
     *
     * @param tau AtcfTaus (forecast hour)
     *
     * @param radii WindRadii
     *
     * @return ForecastTrackRecord
     */
    private ForecastTrackRecord getForecastByTauNRadii(int tau,
            WindRadii radii) {
        RecordKey key = new RecordKey("FCST", currentDTG, tau,
                radii.getValue());
        return fcstTrackRecordMap.get(key);
    }

    /*
     * Update info in forecast panel with existing forecast data.
     */
    private void updateForecastList() {

        fcstRecordList.removeAll();

        int ii = 0;
        int selected = 0;
        for (int tau : workingTauHrs) {

            ForecastTrackRecord rec34kt = getForecastByTauNRadii(tau,
                    WindRadii.RADII_34_KNOT);

            // For TAU 3, if no forecast yet, create one to populate GUI.
            if (tau == specialTau && rec34kt == null) {
                rec34kt = checkSpecialTauRecord(tau, true);
            }

            if (rec34kt != null) {
                String seasInfo = getRecInfoString(rec34kt, true);
                if (!seasInfo.isEmpty()) {
                    fcstRecordList.add(seasInfo);
                    if (currentTauHr == specialTau) {
                        selected = ii;
                    }
                    ii++;
                }

                String recInfo = getRecInfoString(rec34kt, false);
                if (!recInfo.isEmpty()) {
                    fcstRecordList.add(recInfo);
                    if (tau == currentTauHr && currentTauHr != specialTau) {
                        selected = ii;
                    }
                    ii++;
                }

                ForecastTrackRecord rec50kt = getForecastByTauNRadii(tau,
                        WindRadii.RADII_50_KNOT);
                if (rec50kt != null) {
                    String recInfo50 = getRecInfoString(rec50kt, false);
                    if (!recInfo50.isEmpty()) {
                        fcstRecordList.add(recInfo50);
                        ii++;
                    }

                    ForecastTrackRecord rec64kt = getForecastByTauNRadii(tau,
                            WindRadii.RADII_64_KNOT);
                    if (rec64kt != null) {
                        String recInfo64 = getRecInfoString(rec64kt, false);
                        if (!recInfo64.isEmpty()) {
                            fcstRecordList.add(recInfo64);
                            ii++;
                        }
                    }
                }
            }

            fcstRecordList.select(selected);
        }
    }

    /*
     * Gets an info string for forecast list from a ForecastTrackRecord.
     *
     * @param rec ForecastTrackRecord
     *
     * @param isSeas Flag to build string from 12 ft seas wave.
     *
     * @return String
     */
    private String getRecInfoString(ForecastTrackRecord rec, boolean isSeas) {

        StringBuilder sb = new StringBuilder();
        if (rec != null) {
            int radWnd = (int) rec.getRadWind();
            int[] waveRad = rec.getWaveRadii();
            int[] wndRad = rec.getWindRadii();

            if (radWnd == 34) {

                // full info for 34 kt record.
                if (!isSeas) {
                    int fcstHr = rec.getFcstHour();
                    Calendar cal = rec.getRefTimeAsCalendar();
                    cal.add(Calendar.HOUR_OF_DAY, fcstHr);
                    String calStr = String.format(calFormat,
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.HOUR_OF_DAY));

                    float lat = rec.getClat();
                    float lon = rec.getClon();

                    sb.append(String.format(recFormat, fcstHr, calStr,
                            Math.abs(lat), (lat >= 0) ? "N" : "S",
                            Math.abs(lon), (lon <= 0) ? "W" : "E",
                            (int) rec.getStormDrct(), (int) rec.getStormSped(),
                            (int) rec.getWindMax(), (int) rec.getGust(), radWnd,
                            wndRad[0], wndRad[1], wndRad[2], wndRad[3]));
                } else {
                    // 12 ft seas wave
                    int radWave = (int) rec.getRadWave();
                    if (radWave == RAD_WAVE) {
                        sb.append(String.format(seasFormat, rec.getFcstHour(),
                                radWave, waveRad[0], waveRad[1], waveRad[2],
                                waveRad[3]));
                    }

                }
            } else {
                // only wind radii info for 50/64 kt records.
                sb.append(String.format(seasFormat, rec.getFcstHour(), radWnd,
                        wndRad[0], wndRad[1], wndRad[2], wndRad[3]));
            }
        }

        return sb.toString();

    }

    /*
     * Update a wind radii combo's selection or text
     *
     * @param cmb radii CCombo
     *
     * @param radiiValue radii value
     */
    private void updateRadiiCombo(CCombo cmb, int radiiValue) {

        // Round to nearest 5 first.
        int radii = AtcfDataUtil.roundToNearest(radiiValue, 5);
        int ind = Arrays.asList(radiiEntries).indexOf(Integer.toString(radii));

        if (ind >= 0) {
            cmb.select(ind);
        } else {
            // For invalid radii (likely the default in a new record), set to 0.
            if (radii > MAXIMUM_WIND_RADII) {
                cmb.select(0);
            } else {
                cmb.setText(Integer.toString(radiiValue));
            }
        }
    }

    /*
     * Update ForecastTrackRecord from a wind radii CCombo .
     *
     * @param rec ForecastTrackRecord
     *
     * @param wid CCombo that changed.
     */
    private void updateWindRadiiValue(ForecastTrackRecord rec, Widget wid,
            boolean isWave) {
        CCombo cmb = (CCombo) wid;
        if (cmb.isEnabled()) {
            float radVal = AtcfVizUtil.getStringAsFloat(cmb.getText(), false,
                    true, 0.0F);

            // Make sure rounding it to nearest 5.
            int radii = AtcfDataUtil.roundToNearest(radVal, 5);

            // Update wind radii in record.
            int which = (int) (wid.getData());
            if (which == 0) {
                if (isWave) {
                    rec.setQuad1WaveRad(radii);
                } else {
                    rec.setQuad1WindRad(radii);
                }
            } else if (which == 1) {
                if (isWave) {
                    rec.setQuad2WaveRad(radii);
                } else {
                    rec.setQuad2WindRad(radii);
                }

            } else if (which == 2) {
                if (isWave) {
                    rec.setQuad3WaveRad(radii);
                } else {
                    rec.setQuad3WindRad(radii);
                }
            } else if (which == 3) {
                if (isWave) {
                    rec.setQuad4WaveRad(radii);
                } else {
                    rec.setQuad4WindRad(radii);
                }
            }
        }
    }

    /*
     * Resets all information to default.
     */
    private void resetInfo() {

        latTxt.setText("");
        lonTxt.setText("");
        maxWindCmb.select(0);
        gustTxt.setText("");
        dirTxt.setText("");
        speedTxt.setText("");
        developCmb.select(0);

        for (WindRadii radii : WindRadii.values()) {
            for (CCombo cmb : radiiValueCmbs.get(radii)) {
                cmb.select(0);
            }
        }
    }

    /*
     * Set lat/lon/max wind/gust/dir/speed/storm development to values in given
     * record.
     *
     * @param rec ForecastTrackRecord
     */
    private void setBasicInfo(ForecastTrackRecord rec) {
        if (rec != null) {
            float lat = rec.getClat();
            latTxt.setText(String.format("%.1f", Math.abs(lat)));
            latNorthBtn.setSelection(lat >= 0);

            float lon = rec.getClon();
            lonTxt.setText(String.format("%.1f", Math.abs(lon)));
            lonWestBtn.setSelection(lon <= 0);

            int maxWnd = (int) rec.getWindMax();
            updateMaxWindCombo(maxWindCmb, maxWnd);

            gustTxt.setText(String.format("%-4d", (int) rec.getGust()));

            int dir = (int) rec.getStormDrct();
            dir = (dir >= 360) ? 0 : dir;
            dirTxt.setText(String.format("%-3d", dir));

            int spd = (int) rec.getStormSped();
            spd = (spd > 999) ? 0 : spd;
            speedTxt.setText(String.format("%-4d", spd));

            String develop = rec.getIntensity();
            int ind = developCmb
                    .indexOf(StormDevelopment.getFullIntensityString(develop));
            if (ind < 0) {
                ind = 0;
            }

            developCmb.select(ind);
        }
    }

    /*
     * Update a max wind combo's selection or text
     *
     * @param cmb max wind CCombo
     *
     * @param radiiValue radii value
     */
    private void updateMaxWindCombo(CCombo cmb, int windValue) {

        // Round to nearest 5 first.
        int wind = AtcfDataUtil.roundToNearest(windValue, 5);
        int ind = cmb.indexOf(Integer.toString(wind));

        if (ind >= 0) {
            cmb.select(ind);
        } else {
            cmb.select(0);
        }
    }

    /*
     * Callback to remind the user to update data when dialog is closed.
     *
     * @return ICloseCallback
     */
    private ICloseCallback dlgCloseCallBack() {
        return (rv -> {
            if (!applied) {
                Shell shell = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell();
                boolean update = MessageDialog.openQuestion(shell,
                        "Update Advisory Data",
                        "Update data to Advisory Composition Dialog before closing?");
                advData.advisoryDataChanged(fcstTrackRecordMap, update);
            }
        });
    }

    /*
     * Update gust, development, and radii combos when max wind is changed.
     */
    private void modifyMaxWnd() {
        float maxWnd = AtcfVizUtil.getStringAsFloat(maxWindCmb.getText(), false,
                false, 0);

        if (maxWnd != AtcfVizUtil.INVALID_FLOAT) {
            int gust = gustConversion.getGustValue((int) maxWnd);
            gustTxt.setText(Integer.toString(gust));

            String dvp = developCmb.getItem(developCmb.getSelectionIndex());
            String iniType = StormDevelopment.getShortIntensityString(dvp);

            String develop = StormDevelopment.getIntensity(iniType, maxWnd);
            int indx = developItems.indexOf(develop);
            if (indx >= 0) {
                developCmb.select(indx);
            }

            boolean enable50 = false;
            boolean enable64 = false;
            if (maxWnd >= WindRadii.RADII_50_KNOT.getValue()) {
                enable50 = true;
                if (maxWnd >= 64) {
                    enable64 = true;
                }
            }

            /*
             * Only enable/disable, not reset to 0 yet since the user may change
             * it again.
             */
            for (CCombo cmb : radiiValueCmbs.get(WindRadii.RADII_50_KNOT)) {
                cmb.setEnabled(enable50);
            }

            for (CCombo cmb : radiiValueCmbs.get(WindRadii.RADII_64_KNOT)) {
                cmb.setEnabled(enable64);
            }
        }
    }

    /*
     * Get values from wind radii CCombos for a WindRadii.
     *
     * @param radii WindRadii
     *
     * @return float[] Wind Radii for 4 quadrants.
     */
    private float[] getRadiiValue(WindRadii radii) {
        float[] wndRad = new float[] { -1.0F, -1.0F, -1.0F, -1.0F };
        int ii = 0;
        for (CCombo cmb : radiiValueCmbs.get(radii)) {
            if (cmb.isEnabled()) {
                float radVal = AtcfVizUtil.getStringAsFloat(cmb.getText(),
                        false, true, 0.0F);
                // Make sure rounding it to nearest 5.
                wndRad[ii] = AtcfDataUtil.roundToNearest(radVal, 5);
            }

            ii++;
        }

        return wndRad;
    }

    /*
     * Check if wind radii are missing for a TAU.
     *
     * @param tau AtcfTaus
     *
     * @return boolean True - continue; False - Stay
     */
    private boolean checkWindRadii(int tau) {

        // Check wind radii for TAU 3 12 ft seas.
        StringBuilder sb = new StringBuilder();
        if (tau == specialTau) {
            float[] wndRad = getRadiiValue(WindRadii.RADII_0_KNOT);
            for (float rad : wndRad) {
                if (rad < 0.0F) {
                    sb.append("12 ft seas");
                    break;
                }
            }
        }

        // Check wind radii for 34/50/64 kt.
        for (WindRadii radii : WindRadii.values()) {
            if (radii.getValue() > 0) {
                float[] wndRad = getRadiiValue(radii);

                boolean valid = false;
                for (float rad : wndRad) {
                    // Valid when it is not enabled or have a value > 0
                    if (rad < 0.0F || rad > 0.0F) {
                        valid = true;
                        break;
                    }
                }

                if (!valid) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }

                    sb.append(radii.getName());
                    break;
                }
            }
        }

        // Open up confirmation.
        boolean edit = false;
        if (sb.length() > 0) {
            edit = MessageDialog.openQuestion(shell, "Check Wind Radii",
                    "\tMissing " + sb.toString() + " wind radii for TAU " + tau
                            + ". Edit Wind Radii? ");
        }

        return edit;
    }

    /**
     * Changes are sent back to Advisory Composition Dialog so this returns
     * true.
     *
     * @return true if dialog should close
     */
    @Override
    public boolean shouldClose() {
        return true;
    }

    /*
     * Add modify listeners for all text.
     */
    private void addModifyListeners() {
        latTxt.addModifyListener(txtModifyListener);
        lonTxt.addModifyListener(txtModifyListener);
        dirTxt.addModifyListener(txtModifyListener);
        gustTxt.addModifyListener(txtModifyListener);
        speedTxt.addModifyListener(txtModifyListener);

        maxWindCmb.addModifyListener(windModifyListener);

        for (java.util.List<CCombo> cmbs : radiiValueCmbs.values()) {
            for (CCombo cmb : cmbs) {
                cmb.addModifyListener(txtModifyListener);
            }
        }
    }

    /*
     * Remove modify listeners for all text.
     */
    private void removeModifyListeners() {
        latTxt.removeModifyListener(txtModifyListener);
        lonTxt.removeModifyListener(txtModifyListener);
        dirTxt.removeModifyListener(txtModifyListener);
        gustTxt.removeModifyListener(txtModifyListener);
        speedTxt.removeModifyListener(txtModifyListener);

        maxWindCmb.removeModifyListener(windModifyListener);

        for (java.util.List<CCombo> cmbs : radiiValueCmbs.values()) {
            for (CCombo cmb : cmbs) {
                cmb.removeModifyListener(txtModifyListener);
            }
        }
    }

    /*
     * Listener to handle text changes in lat, lon etc.
     */
    private ModifyListener txtModifyListener() {
        return (e -> updateForTau(currentTauHr, -1));
    }

    /*
     * Listener to handle text changes for max wind.
     */
    private ModifyListener windModifyListener() {
        return (e -> {
            modifyMaxWnd();
            updateForTau(currentTauHr, -1);
        });
    }

    /*
     * Create records for special TAU based on maximum wind.
     *
     * @param rec34 ForecastTrackRecord at 34 KT.
     */
    private void createSpecialTauRecords(ForecastTrackRecord rec34) {
        if (rec34 != null) {
            RecordKey rkey = new RecordKey("FCST", currentDTG,
                    rec34.getFcstHour(), WindRadii.RADII_34_KNOT.getValue());
            fcstTrackRecordMap.put(rkey, rec34);

            int rad50 = WindRadii.RADII_50_KNOT.getValue();
            if (rec34.getWindMax() >= rad50) {
                ForecastTrackRecord tau0Rec50 = getForecastByTauNRadii(0,
                        WindRadii.RADII_50_KNOT);
                ForecastTrackRecord rec50 = new ForecastTrackRecord(rec34);
                rec50.setRadWind(rad50);

                if (tau0Rec50 != null) {
                    rec50.setQuad1WindRad(tau0Rec50.getQuad1WindRad());
                    rec50.setQuad2WindRad(tau0Rec50.getQuad2WindRad());
                    rec50.setQuad3WindRad(tau0Rec50.getQuad3WindRad());
                    rec50.setQuad4WindRad(tau0Rec50.getQuad4WindRad());
                }

                RecordKey rk = new RecordKey("FCST", currentDTG,
                        rec50.getFcstHour(), rad50);
                fcstTrackRecordMap.put(rk, rec50);

                int rad64 = WindRadii.RADII_64_KNOT.getValue();
                if (rec34.getWindMax() >= rad64) {
                    ForecastTrackRecord tau0Rec64 = getForecastByTauNRadii(0,
                            WindRadii.RADII_64_KNOT);
                    ForecastTrackRecord rec64 = new ForecastTrackRecord(rec34);
                    rec64.setRadWind(rad64);

                    if (tau0Rec64 != null) {
                        rec64.setQuad1WindRad(tau0Rec64.getQuad1WindRad());
                        rec64.setQuad2WindRad(tau0Rec64.getQuad2WindRad());
                        rec64.setQuad3WindRad(tau0Rec64.getQuad3WindRad());
                        rec64.setQuad4WindRad(tau0Rec64.getQuad4WindRad());
                    }

                    RecordKey rr = new RecordKey("FCST", currentDTG,
                            rec64.getFcstHour(), rad64);
                    fcstTrackRecordMap.put(rr, rec64);
                }
            }
        }
    }

}
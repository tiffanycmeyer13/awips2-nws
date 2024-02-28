/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.locationtech.jts.geom.Coordinate;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;

import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.common.dataplugin.atcf.request.PrepareComputeRequest;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.WindRadii;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackGenerator;
import gov.noaa.nws.ocp.viz.atcf.track.BestTrackProperties;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveChangeTrackDialog;

/**
 * Dialog for "Prepare Compute Data" under "Aids"=>"Create Objective Forecast".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 16, 2019 59172      jwu         Initial creation.
 * Nov 08, 2019 70253      mporricelli Create .com file and update
 *                                     A & B decks from GUI input
 * Nov 10, 2020 84442      wpaintsil   Add Scrollbars.
 * Mar 05, 2021 88229      mporricelli Use bdeck to populate GUI;
 *                                     redraw best track display
 *                                     upon Save
 * May 25, 2021 91762      wpaintsil   Compute first guess direction/speed
 *                                     instead of assuming 0.
 * Jul 28, 2021 94527      jwu         Fix path & speed/dir calculation.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class PrepareComputeDataDialog extends OcpCaveChangeTrackDialog {

    // Names for storm parameters
    private static final String[] parameterNames = new String[] {
            "Eye Diameter", "Max Wind Radius",
            "Vertical Extent of Circlulation", "Central Pressure",
            "Outmost Closed Isobar", "Radius Outmost Closed Isobar" };

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(PrepareComputeDataDialog.class);

    private Button submitButton;

    private Storm storm;

    private int adeckSandboxID;

    private int bdeckSandboxID;

    private String dtg;

    private String ofclDtg;

    private Map<String, List<BDeckRecord>> retrievedBDeckRecords;

    private Map<String, List<ADeckRecord>> retrievedADeckRecords;

    private List<ADeckRecord> updatedADeckRecords;

    private List<BDeckRecord> updatedBDeckRecords;

    // Controls for past 24 and 12 hours DTG information rows.
    private Label past24hrLatLbl;

    private Label past24hrLonLbl;

    private Label past24hrMaxWndLbl;

    private Label past24hrDirLbl;

    private Label past24hrSpdLbl;

    private Label past12hrLatLbl;

    private Label past12hrLonLbl;

    private Label past12hrMaxWndLbl;

    private Label past12hrDirLbl;

    private Label past12hrSpdLbl;

    // Controls for current DTG information row.
    private CCombo currentLatCombo;

    private Button latNorthBtn;

    private Button latSouthBtn;

    private Button lonEastBtn;

    private Button lonWestBtn;

    private CCombo currentLonCombo;

    private CCombo currentMaxWindCombo;

    private CCombo currentDirCombo;

    private CCombo currentSpdCombo;

    // Controls for current DTG's key parameters.
    private CCombo[] parameterCombos = new CCombo[6];

    // Controls for current DTG's speed/quadrant.
    private CCombo[] ktCombos1;

    private CCombo[] ktCombos2;

    private CCombo[] ktCombos3;

    /**
     * Constructor
     *
     * @param parent
     */
    public PrepareComputeDataDialog(Shell parent, Storm storm) {

        super(parent);

        this.storm = storm;

        initialize();

        setText("Prepare Compute Data - " + storm.getStormName() + " "
                + storm.getStormId());
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

        createDataComp(mainComp);
        createControlButtons(mainComp);
        populateData();

        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginHeight = 0;

        mainComp.setLayout(mainLayout);
        mainComp.setData(new GridData(SWT.FILL, SWT.FILL, true, false));

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
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, true);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginHeight = 5;
        dtgInfoLayout.marginWidth = 20;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // Create label with storm information
        Label nameLbl = new Label(mainComp, SWT.NONE);
        String nameStr = "" + storm.getCycloneNum() + " " + storm.getYear()
                + " " + storm.getRegion() + " - " + storm.getStormName() + " ";
        nameLbl.setText(nameStr);
        GridData nameLblData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        nameLbl.setLayoutData(nameLblData);

        // Selection of storm DTG
        createDtgSelectionComp(mainComp);

        // Major composite for data input.
        Composite dataInputComp = new Composite(mainComp, SWT.BORDER);
        GridLayout tableLayout = new GridLayout(6, true);
        tableLayout.verticalSpacing = 15;
        tableLayout.marginHeight = 10;
        tableLayout.marginWidth = 5;
        dataInputComp.setLayout(tableLayout);
        dataInputComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // Labels for lat/lon/max wind/dir/spd
        createInfoTitleRow(dataInputComp);

        // Past 24 hour & 12 hour lat/lon/max wind/dir/spd
        createPastInfoRows(dataInputComp);

        // Add a separator
        Label sepLbl = new Label(dataInputComp, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 6;
        sepLbl.setLayoutData(gd);

        // Current lat/lon/max wind/dir/spd
        createCurrentInfoRow(dataInputComp);

        // Composite for parameter names - ”Eye Diameter" etc.
        createParamInputSection(dataInputComp);

        // Composite for Speed/Quadrant
        createSpeedQuadrantComp(dataInputComp);

    }

    /*
     * Initialize data.
     */
    private void initialize() {

        retrievedBDeckRecords = AtcfDataUtil.getBDeckRecords(storm, true);
        bdeckSandboxID = AtcfDataUtil.getBDeckSandbox(storm);

        TreeSet<String> dtgs = new TreeSet<>(retrievedBDeckRecords.keySet());

        // Get the most recent DTG and the one directly before it
        if (!dtgs.isEmpty()) {
            dtg = dtgs.last();
            ofclDtg = dtgs.lower(dtg);
        }

        if (dtg != null) {
            retrievedADeckRecords = AtcfDataUtil.retrieveADeckData(storm,
                    new String[] { dtg }, true);
        } else {
            retrievedADeckRecords = new LinkedHashMap<>();
        }
        adeckSandboxID = AtcfDataUtil.getADeckSandbox(storm);
    }

    /**
     * Create the composite to select DTG.
     *
     * @param parent
     */
    private void createDtgSelectionComp(Composite parent) {
        Composite dtgSelectComp = new Composite(parent, SWT.NONE);
        GridLayout dtgSelectLayout = new GridLayout(2, true);
        dtgSelectLayout.marginWidth = 5;
        dtgSelectLayout.marginHeight = 0;
        dtgSelectComp.setLayout(dtgSelectLayout);
        dtgSelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label dtgLabel = new Label(dtgSelectComp, SWT.NONE);
        dtgLabel.setText("      Date-Time-Group: ");
        GridData dtgLabelData = new GridData(SWT.RIGHT, SWT.CENTER, false,
                false);
        dtgLabel.setLayoutData(dtgLabelData);

        CCombo dtgCombo = new CCombo(dtgSelectComp, SWT.NONE | SWT.READ_ONLY);
        if (dtg != null) {
            dtgCombo.add(dtg);
            dtgCombo.select(0);
        }

        GridData dtgComboData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        dtgCombo.setLayoutData(dtgComboData);

    }

    /**
     * Create the title row to list lat/lon/max wind/dir/spd.
     *
     * @param parent
     */
    private void createInfoTitleRow(Composite parent) {

        Label emptyLabel = new Label(parent, SWT.NONE);
        emptyLabel.setText("");
        emptyLabel.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label latLbl = new Label(parent, SWT.NONE);
        latLbl.setText("Lat");
        latLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label lonLbl = new Label(parent, SWT.NONE);
        lonLbl.setText("Lon");
        lonLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label maxWndLbl = new Label(parent, SWT.NONE);
        maxWndLbl.setText("Max Wind (kt)");
        maxWndLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label dirLbl = new Label(parent, SWT.NONE);
        dirLbl.setText("Dir (deg)");
        dirLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label spdLbl = new Label(parent, SWT.NONE);
        spdLbl.setText("Spd (kt)");
        spdLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

    }

    /**
     * Create the row to input current lat/lon/max wind/dir/spd.
     *
     * @param parent
     */
    private void createPastInfoRows(Composite parent) {
        // Past 24 hour & 12 hour info
        Label past24hrLbl = new Label(parent, SWT.NONE);
        past24hrLbl.setText("Past 24 hr:");
        past24hrLbl.setLayoutData(
                new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

        past24hrLatLbl = new Label(parent, SWT.NONE);
        past24hrLatLbl.setText("23.1   N");
        past24hrLatLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past24hrLonLbl = new Label(parent, SWT.NONE);
        past24hrLonLbl.setText("80.2   W");
        past24hrLonLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past24hrMaxWndLbl = new Label(parent, SWT.NONE);
        past24hrMaxWndLbl.setText("110");
        past24hrMaxWndLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past24hrDirLbl = new Label(parent, SWT.NONE);
        past24hrDirLbl.setText("");
        past24hrDirLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past24hrSpdLbl = new Label(parent, SWT.NONE);
        past24hrSpdLbl.setText("");
        past24hrSpdLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // Past 12 hour info
        Label past12hrLbl = new Label(parent, SWT.NONE);
        past12hrLbl.setText("Past 12 hr:");
        past12hrLbl.setLayoutData(
                new GridData(SWT.RIGHT, SWT.DEFAULT, true, false));

        past12hrLatLbl = new Label(parent, SWT.NONE);
        past12hrLatLbl.setText("23.7   N");
        past12hrLatLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past12hrLonLbl = new Label(parent, SWT.NONE);
        past12hrLonLbl.setText("81.5   W");
        past12hrLonLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past12hrMaxWndLbl = new Label(parent, SWT.NONE);
        past12hrMaxWndLbl.setText("115");
        past12hrMaxWndLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past12hrDirLbl = new Label(parent, SWT.NONE);
        past12hrDirLbl.setText("334");
        past12hrDirLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        past12hrSpdLbl = new Label(parent, SWT.NONE);
        past12hrSpdLbl.setText("6");
        past12hrSpdLbl.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));
    }

    /**
     * Create the row to input current lat/lon/max wind/dir/spd.
     *
     * @param parent
     */
    private void createCurrentInfoRow(Composite parent) {

        Label currentLbl = new Label(parent, SWT.NONE);
        currentLbl.setText("Current:");
        currentLbl.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, true, false));

        Composite currentLatComp = new Composite(parent, SWT.NONE);
        GridLayout currentLatLayout = new GridLayout(2, false);
        currentLatLayout.marginHeight = 3;
        currentLatLayout.marginWidth = 3;
        currentLatLayout.horizontalSpacing = 0;
        currentLatComp.setLayout(currentLatLayout);
        GridData currentLatCompGD = new GridData(SWT.CENTER, SWT.CENTER, true,
                false);
        currentLatComp.setLayoutData(currentLatCompGD);

        String[] latItems = new String[901];
        for (int ii = 0; ii < latItems.length; ii++) {
            latItems[ii] = String.valueOf((ii * 100 * 0.10) / 100);
        }
        currentLatCombo = new CCombo(currentLatComp, SWT.NONE);
        currentLatCombo.setItems(latItems);
        currentLatCombo.select(0);
        GridData currentLatComboGD = new GridData(SWT.FILL, SWT.CENTER, true,
                false);
        currentLatCombo.setLayoutData(currentLatComboGD);

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
        GridData latNorthBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        latNorthBtn.setLayoutData(latNorthBtnGD);

        latSouthBtn = new Button(currentLatDirComp, SWT.RADIO);
        latSouthBtn.setText("S");
        GridData latSouthBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        latSouthBtn.setLayoutData(latSouthBtnGD);

        Composite currentLonComp = new Composite(parent, SWT.NONE);
        GridLayout currentLonLayout = new GridLayout(2, false);
        currentLonLayout.marginHeight = 3;
        currentLonLayout.marginWidth = 3;
        currentLonLayout.horizontalSpacing = 0;
        currentLonComp.setLayout(currentLonLayout);
        GridData currentLonCompGD = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        currentLonComp.setLayoutData(currentLonCompGD);

        String[] lonItems = new String[1800];
        for (int ii = 0; ii < lonItems.length; ii++) {
            lonItems[ii] = String.valueOf((ii * 100 * 0.10) / 100);
        }

        currentLonCombo = new CCombo(currentLonComp, SWT.NONE);
        currentLonCombo.setItems(lonItems);
        currentLonCombo.select(0);
        GridData currentLonComboGD = new GridData(SWT.FILL, SWT.CENTER, true,
                false);
        currentLonCombo.setLayoutData(currentLonComboGD);

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

        lonWestBtn = new Button(currentLonDirComp, SWT.RADIO);
        lonWestBtn.setText("W");
        GridData lonWestBtnGD = new GridData(SWT.DEFAULT, SWT.CENTER, true,
                false);
        lonWestBtn.setLayoutData(lonWestBtnGD);

        String[] maxwItems = new String[51];
        for (int ii = 0; ii < maxwItems.length; ii++) {
            maxwItems[ii] = String.valueOf(ii * 5);
        }
        currentMaxWindCombo = new CCombo(parent, SWT.NONE);
        currentMaxWindCombo.setItems(maxwItems);
        currentMaxWindCombo.select(0);
        GridData currentMaxWindComboGD = new GridData(SWT.FILL, SWT.CENTER,
                false, false);
        currentMaxWindCombo.setLayoutData(currentMaxWindComboGD);

        String[] dirItems = new String[72];
        for (int ii = 0; ii < dirItems.length; ii++) {
            dirItems[ii] = String.valueOf(ii * 5);
        }
        currentDirCombo = new CCombo(parent, SWT.NONE);
        currentDirCombo.setItems(dirItems);
        currentDirCombo.select(0);
        GridData currentDirComboGD = new GridData(SWT.FILL, SWT.CENTER, false,
                false);
        currentDirCombo.setLayoutData(currentDirComboGD);

        String[] spdItems = new String[100];
        for (int ii = 0; ii < spdItems.length; ii++) {
            spdItems[ii] = String.valueOf(ii);
        }
        currentSpdCombo = new CCombo(parent, SWT.NONE);
        currentSpdCombo.setItems(spdItems);
        currentSpdCombo.select(0);
        GridData currentSpdComboGD = new GridData(SWT.FILL, SWT.CENTER, false,
                false);
        currentSpdCombo.setLayoutData(currentSpdComboGD);
    }

    /**
     * Create the middle section to input parameters like "Eye Diameter" etc.
     * and "Guidance"/"Bogus History" buttons.
     *
     * @param parent
     */
    private void createParamInputSection(Composite parent) {

        // Composite for CCombos to select/type parameters
        Composite paramNameComp = new Composite(parent, SWT.NONE);
        GridLayout paramNameGridLayout = new GridLayout(3, false);
        paramNameGridLayout.marginWidth = 0;
        paramNameGridLayout.marginHeight = 15;
        paramNameComp.setLayout(paramNameGridLayout);
        GridData paramNameCompGD = new GridData(SWT.RIGHT, SWT.DEFAULT, false,
                false);
        paramNameCompGD.horizontalSpan = 3;
        paramNameComp.setLayoutData(paramNameCompGD);

        String[] measurementLbls = new String[] { "nm", "nm", "", "mb", "mb",
                "nm" };

        for (int i = 0; i < 6; i++) {

            GridData rightAlignGridData = new GridData(SWT.RIGHT, SWT.CENTER,
                    true, false);
            Label boxLabel = new Label(paramNameComp, SWT.NONE);
            boxLabel.setText(parameterNames[i] + ": ");
            boxLabel.setLayoutData(rightAlignGridData);

            GridData textAlignGridData = new GridData(SWT.LEFT, SWT.CENTER,
                    true, false);

            if (i == 2) {
                textAlignGridData.horizontalSpan = 2;
            }

            parameterCombos[i] = new CCombo(paramNameComp, SWT.BORDER);
            parameterCombos[i].setText("");
            parameterCombos[i].setLayoutData(textAlignGridData);

            if (i != 2) {

                Label label = new Label(paramNameComp, SWT.NONE);
                label.setText(measurementLbls[i]);
                label.setLayoutData(
                        new GridData(SWT.LEFT, SWT.CENTER, true, false));

            }
        }

        String[] eyeDiamItems = new String[21];
        for (int ii = 0; ii < eyeDiamItems.length; ii++) {
            eyeDiamItems[ii] = String.valueOf(ii * 5);
        }
        parameterCombos[0].setItems(eyeDiamItems);
        parameterCombos[0].select(0);

        String[] maxwradItems = new String[200];
        for (int ii = 0; ii < maxwradItems.length; ii++) {
            maxwradItems[ii] = String.valueOf(ii * 5);
        }
        parameterCombos[1].setItems(maxwradItems);
        parameterCombos[1].select(0);

        parameterCombos[2].setItems(new String[] { "Shallow   <700 mb    ",
                "Medium   700 mb - 400 mb   ", "Deep      >400 mb    " });
        parameterCombos[2].select(0);

        String[] cpresItems = new String[201];
        for (int ii = 0; ii < cpresItems.length; ii++) {
            cpresItems[ii] = String.valueOf(850 + ii);
        }
        parameterCombos[3].setItems(cpresItems);
        parameterCombos[3].select(0);

        String[] omclosedItems = new String[152];
        omclosedItems[0] = "0";
        for (int ii = 1; ii < omclosedItems.length; ii++) {
            omclosedItems[ii] = String.valueOf(899 + ii);
        }
        parameterCombos[4].add("0");
        parameterCombos[4].setItems(omclosedItems);

        String[] omclosedradItems = new String[200];
        for (int ii = 0; ii < omclosedradItems.length; ii++) {
            omclosedradItems[ii] = String.valueOf(ii * 5);
        }
        parameterCombos[5].setItems(omclosedradItems);
        parameterCombos[5].select(0);

        // Composite to hold "Guidance" and "Bogus History" buttons
        Composite otherCmdComp = new Composite(parent, SWT.NONE);
        GridLayout otherCmdLayout = new GridLayout(1, true);
        otherCmdLayout.verticalSpacing = 40;
        otherCmdLayout.marginHeight = 30;
        otherCmdLayout.marginWidth = 3;
        otherCmdComp.setLayout(otherCmdLayout);
        GridData otherCmdCompGD = new GridData(SWT.CENTER, SWT.DEFAULT, true,
                false);
        otherCmdCompGD.horizontalSpan = 3;
        otherCmdComp.setLayoutData(otherCmdCompGD);

        Button guidanceBtn = new Button(otherCmdComp, SWT.NONE);
        guidanceBtn.setText("Guidance...");
        GridData guidanceBtnGD = new GridData(SWT.LEFT, SWT.CENTER, true, true);
        guidanceBtn.setLayoutData(guidanceBtnGD);

        Button bogusHistBtn = new Button(otherCmdComp, SWT.NONE);
        bogusHistBtn.setText("Bogus History...");
        GridData bogusHistBtnGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                true);
        bogusHistBtn.setLayoutData(bogusHistBtnGD);

    }

    /**
     * Create composite to input speed and quadrant.
     *
     * @param parent
     */
    private void createSpeedQuadrantComp(Composite parent) {
        Composite spdQuadrantComp = new Composite(parent, SWT.NONE);
        GridLayout spdQuadrantLayout = new GridLayout(5, false);
        spdQuadrantLayout.marginWidth = 5;
        spdQuadrantLayout.marginHeight = 5;
        spdQuadrantLayout.horizontalSpacing = 5;
        spdQuadrantComp.setLayout(spdQuadrantLayout);
        GridData spdQuadrantCompGD = new GridData(SWT.FILL, SWT.TOP, true,
                false);
        spdQuadrantCompGD.horizontalSpan = 6;
        spdQuadrantComp.setLayoutData(spdQuadrantCompGD);

        Label spdQuadrantLbl = new Label(spdQuadrantComp, SWT.NONE);
        spdQuadrantLbl.setText("Speed/Quadrant");
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
        ktLabel1.setText("34 kt: ");
        ktLabel1.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        ktCombos1 = new CCombo[4];
        for (int jj = 0; jj < ktCombos1.length; jj++) {
            ktCombos1[jj] = new CCombo(spdQuadrantComp, SWT.NONE);
            ktCombos1[jj].setItems(ktItems);
            ktCombos1[jj].select(0);

            ktCombos1[jj].setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        }

        Label ktLabel2 = new Label(spdQuadrantComp, SWT.NONE);
        ktLabel2.setText("50 kt: ");
        ktLabel2.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        ktCombos2 = new CCombo[4];
        for (int jj = 0; jj < ktCombos2.length; jj++) {
            ktCombos2[jj] = new CCombo(spdQuadrantComp, SWT.NONE);
            ktCombos2[jj].setItems(ktItems);
            ktCombos2[jj].select(0);

            ktCombos2[jj].setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        }

        Label ktLabel3 = new Label(spdQuadrantComp, SWT.NONE);
        ktLabel3.setText("64 kt: ");
        ktLabel3.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, false));
        ktCombos3 = new CCombo[4];
        for (int jj = 0; jj < ktCombos3.length; jj++) {
            ktCombos3[jj] = new CCombo(spdQuadrantComp, SWT.NONE);
            ktCombos3[jj].setItems(ktItems);
            ktCombos3[jj].select(0);

            ktCombos3[jj].setLayoutData(
                    new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        }

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite buttonComp2 = new Composite(parent, SWT.NONE);
        GridLayout buttonCompLayout2 = new GridLayout(4, true);
        buttonCompLayout2.marginWidth = 15;
        buttonCompLayout2.horizontalSpacing = 15;
        buttonComp2.setLayout(buttonCompLayout2);
        buttonComp2.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp2, AtcfVizUtil.buttonGridData());

        Button saveButton = new Button(buttonComp2, SWT.PUSH);
        saveButton.setText("Save Changes");
        saveButton.setLayoutData(AtcfVizUtil.buttonGridData());
        saveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                submitButton.setEnabled(true);

                updatedDeckRecords();

                saveADeckDataToSbx(updatedADeckRecords);
                saveBDeckDataToSbx(updatedBDeckRecords);

                refreshBDeck();
                redraw(true);

            }
        });

        MessageBox submitDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        submitDialog.setText("Alert");
        submitDialog
                .setMessage("Are you sure you wish to submit your changes?");

        submitButton = new Button(buttonComp2, SWT.PUSH);
        submitButton.setText("Submit");
        submitButton.setLayoutData(AtcfVizUtil.buttonGridData());
        submitButton.setEnabled(false);
        submitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int submitID = submitDialog.open();
                if (submitID == SWT.OK) {
                    createComFile(updatedADeckRecords);

                    AtcfDataUtil.checkinADeckRecords(adeckSandboxID);
                    AtcfDataUtil.checkinBDeckRecords(bdeckSandboxID);

                    AtcfProduct prd = AtcfSession.getInstance()
                            .getAtcfResource().getResourceData()
                            .getAtcfProduct(storm);
                    prd.setAdeckSandboxID(-1);
                    prd.setBdeckSandboxID(-1);
                    redraw(false);

                    close();
                }

            }
        });

        Button cancelButton = new Button(buttonComp2, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();

            }
        });
    }

    /*
     * Populate the dialog with the data based on the DTG selection.
     */
    private void populateData() {

        /*
         * Normally, each B-Deck DTG will have three records, 34/50/64KT.
         */
        float past12hrLat = 0;
        float past24hrLat = 0;
        float curLat = 0;
        float past12hrLon = 0;
        float past24hrLon = 0;
        float curLon = 0;
        float past12hrMaxWind = 0;
        float past24hrMaxWind = 0;
        float curMaxWind = 0;
        float past12hrDir = 0;
        float past24hrDir = 0;
        float curDir = 0;
        float past12hrSpd = 0;
        float past24hrSpd = 0;
        float curSpd = 0;

        float curEyeSize = 0;
        float curMaxWindRad = 0;
        float curMslp = 0;
        float curClosedP = 0;
        float curRadClosedP = 0;
        String curStormDepth = "";
        String curDepthFullString = "";
        int depthSelectIdx = 0;

        Map<WindRadii, BDeckRecord> bDeckRowKt = new EnumMap<>(WindRadii.class);

        if (bdeckSandboxID <= 0) {
            bdeckSandboxID = AtcfDataUtil.getBDeckSandbox(storm);
        }

        String dtgBack36 = AtcfDataUtil.getNewDTG(dtg, -36);
        String dtgBack24 = AtcfDataUtil.getNewDTG(dtg, -24);
        String dtgBack12 = AtcfDataUtil.getNewDTG(dtg, -12);

        List<BDeckRecord> recsBack36 = retrievedBDeckRecords
                .get(dtgBack36);
        List<BDeckRecord> recsBack24 = retrievedBDeckRecords
                .get(dtgBack24);
        List<BDeckRecord> recsBack12 = retrievedBDeckRecords
                .get(dtgBack12);
        List<BDeckRecord> recs = retrievedBDeckRecords.get(dtg);

        BDeckRecord past36Rec = recsBack36 != null
                ? recsBack36.get(recsBack36.size() - 1)
                : null;

        BDeckRecord past24Rec = recsBack36 != null
                ? recsBack24.get(recsBack24.size() - 1)
                : null;
        past24hrLat = past24Rec.getClat();
        past24hrLon = past24Rec.getClon();
        past24hrMaxWind = past24Rec.getWindMax();

        int[] dirSpd24 = directionSpeed(past24Rec, past36Rec);
        past24hrDir = dirSpd24[0];
        past24hrSpd = dirSpd24[1];

        BDeckRecord past12Rec = recsBack12 != null
                ? recsBack12.get(recsBack12.size() - 1)
                : null;
        past12hrLat = past12Rec.getClat();
        past12hrLon = past12Rec.getClon();
        past12hrMaxWind = past12Rec.getWindMax();

        int[] dirSpd12 = directionSpeed(past12Rec, past24Rec);
        past12hrDir = dirSpd12[0];
        past12hrSpd = dirSpd12[1];

        BDeckRecord curRec = recs != null ? recs.get(recs.size() - 1) : null;
        curLat = curRec.getClat();
        curLon = curRec.getClon();
        curMaxWind = curRec.getWindMax();

        int[] curDirSpd = directionSpeed(curRec, past12Rec);
        curDir = curDirSpd[0];
        curSpd = curDirSpd[1];
        curEyeSize = curRec.getEyeSize();
        curMaxWindRad = curRec.getMaxWindRad();
        curStormDepth = curRec.getStormDepth();

        if ("S".equals(curStormDepth)) {
            depthSelectIdx = 0;
        } else if ("M".equals(curStormDepth)) {
            depthSelectIdx = 1;
        } else if ("D".equals(curStormDepth)) {
            depthSelectIdx = 2;
        }

        curMslp = curRec.getMslp();
        curClosedP = curRec.getClosedP();
        curRadClosedP = curRec.getRadClosedP();

        if (curRec.getRadWind() == 34) {
            bDeckRowKt.put(WindRadii.RADII_34_KNOT, curRec);
        } else if (curRec.getRadWind() == 50) {
            bDeckRowKt.put(WindRadii.RADII_50_KNOT, curRec);
        } else if (curRec.getRadWind() == 64) {
            bDeckRowKt.put(WindRadii.RADII_64_KNOT, curRec);
        }

        // Info for past 24 hour DTG
        String latString = String.format("%.1f", Math.abs(past24hrLat))
                + (past24hrLat < 0 ? "S" : "N");
        past24hrLatLbl.setText(latString);

        String lonString = String.format("%.1f", Math.abs(past24hrLon))
                + (past24hrLon > 0 ? "E" : "W");
        past24hrLonLbl.setText(lonString);

        String maxWndString = String.format("%d", (int) past24hrMaxWind);
        past24hrMaxWndLbl.setText(maxWndString);

        String dirString = String.format("%.2f", past24hrDir);
        past24hrDirLbl.setText(dirString);

        String spdString = String.format("%.2f", past24hrSpd);
        past24hrSpdLbl.setText(spdString);

        // Info for past 12 hour DTG
        latString = String.format("%.1f", Math.abs(past12hrLat))
                + (past12hrLat < 0 ? "S" : "N");
        past12hrLatLbl.setText(latString);

        lonString = String.format("%.1f", Math.abs(past12hrLon))
                + (past12hrLon > 0 ? "E" : "W");
        past12hrLonLbl.setText(lonString);

        maxWndString = String.format("%d", (int) past12hrMaxWind);
        past12hrMaxWndLbl.setText(maxWndString);

        dirString = String.format("%.2f", past12hrDir);
        past12hrDirLbl.setText(dirString);

        spdString = String.format("%.2f", past12hrSpd);
        past12hrSpdLbl.setText(spdString);

        // Information for current DTG
        latString = String.format("%.1f", Math.abs(curLat));
        currentLatCombo.setText(latString);
        latNorthBtn.setSelection(curLat >= 0);
        latSouthBtn.setSelection(curLat < 0);

        lonString = String.format("%.1f", Math.abs(curLon));
        currentLonCombo.setText(lonString);
        lonEastBtn.setSelection(curLon > 0);
        lonWestBtn.setSelection(curLon <= 0);

        maxWndString = String.format("%d", (int) curMaxWind);
        currentMaxWindCombo.setText(maxWndString);

        dirString = String.format("%d", (int) curDir);
        currentDirCombo.setText(dirString);

        spdString = String.format("%d", (int) curSpd);
        currentSpdCombo.setText(spdString);

        parameterCombos[0].setText(String.format("%d", (int) curEyeSize));

        parameterCombos[1].setText(String.format("%d", (int) curMaxWindRad));

        parameterCombos[2].setText(curDepthFullString);

        parameterCombos[2].select(depthSelectIdx);

        parameterCombos[3].setText(String.format("%d", (int) curMslp));

        parameterCombos[4].setText(String.format("%d", (int) curClosedP));

        parameterCombos[5].setText(String.format("%d", (int) curRadClosedP));

        // Speed/Quadrant
        for (int ii = 0; ii < 4; ii++) {
            if (bDeckRowKt.containsKey(WindRadii.RADII_34_KNOT)) {
                ktCombos1[ii].setText(String.format("%d", bDeckRowKt
                        .get(WindRadii.RADII_34_KNOT).getWindRadii()[ii]));
            }
            if (bDeckRowKt.containsKey(WindRadii.RADII_50_KNOT)) {
                ktCombos2[ii].setText(String.format("%d", bDeckRowKt
                        .get(WindRadii.RADII_50_KNOT).getWindRadii()[ii]));
            }
            if (bDeckRowKt.containsKey(WindRadii.RADII_64_KNOT)) {
                ktCombos3[ii].setText(String.format("%d", bDeckRowKt
                        .get(WindRadii.RADII_64_KNOT).getWindRadii()[ii]));
            }
        }

    }

    /**
     * Calculate the direction and speed from one track location to another
     * track location (BDeckRecords). Default to 0.
     *
     * @param startingRecord
     * @param endingRecord
     * @return int[]: int[0] Direction; int[1] Speed in knots.
     */
    private static int[] directionSpeed(BDeckRecord startingRecord,
            BDeckRecord endingRecord) {
        if (startingRecord != null && endingRecord != null) {
            double[] distDir = AtcfVizUtil.getDistNDir(
                    new Coordinate(endingRecord.getClon(),
                            endingRecord.getClat()),
                    new Coordinate(startingRecord.getClon(),
                            startingRecord.getClat()));
            float timeDiff = Math
                    .abs(endingRecord.getRefTime().getTime()
                            - startingRecord.getRefTime().getTime())
                    / (float) TimeUtil.MILLIS_PER_HOUR;

            return new int[] { (int) distDir[1], (int) Math
                    .round(distDir[0] / AtcfVizUtil.NM2M / timeDiff) };
        }

        return new int[] { 0, 0 };
    }

    /**
     * Update A- and B-deck data based on the user input in the dialog.
     *
     */
    private void updatedDeckRecords() {

        updatedADeckRecords = new ArrayList<>();
        updatedBDeckRecords = new ArrayList<>();

        float latValue = Float.parseFloat(currentLatCombo.getText());
        if (latSouthBtn.getSelection()) {
            latValue = -latValue;
        }

        float lonValue = Float.parseFloat(currentLonCombo.getText());
        if (lonWestBtn.getSelection()) {
            lonValue = -lonValue;
        }

        float maxWnd = Float.parseFloat(currentMaxWindCombo.getText());
        float dir = Float.parseFloat(currentDirCombo.getText());
        float spd = Float.parseFloat(currentSpdCombo.getText());
        float eyeSize = Float.parseFloat(parameterCombos[0].getText());
        float maxWindRad = Float.parseFloat(parameterCombos[1].getText());
        String depth = "";
        depth = parameterCombos[2]
                .getItem(parameterCombos[2].getSelectionIndex());
        if (!depth.isEmpty()) {
            depth = depth.substring(0, 1);
        }
        float pres = Float.parseFloat(parameterCombos[3].getText());
        float closedPres = Float.parseFloat(parameterCombos[4].getText());
        float radClosedP = Float.parseFloat(parameterCombos[5].getText());

        // Update A-Deck records
        if (retrievedADeckRecords != null
                && retrievedADeckRecords.containsKey(dtg)) {
            for (ADeckRecord rec : retrievedADeckRecords.get(dtg)) {
                if ("CARQ".equals(rec.getTechnique())
                        && rec.getFcstHour() == 0) {
                    // Information for current DTG
                    rec.setClat(latValue);
                    rec.setClon(lonValue);
                    rec.setWindMax(maxWnd);
                    rec.setStormDrct(dir);
                    rec.setStormSped(spd);
                    rec.setEyeSize(eyeSize);
                    rec.setMaxWindRad(maxWindRad);
                    rec.setStormDepth(depth);
                    rec.setMslp(pres);
                    rec.setClosedP(closedPres);
                    rec.setRadClosedP(radClosedP);
                    // Update speed for each quadrant.
                    if (rec.getRadWind() == 34) {
                        updateQuadrant(rec, ktCombos1);
                    } else if (rec.getRadWind() == 50) {
                        updateQuadrant(rec, ktCombos2);
                    } else if (rec.getRadWind() == 64) {
                        updateQuadrant(rec, ktCombos3);
                    }
                    updatedADeckRecords.add(rec);
                } else if ("CARQ".equals(rec.getTechnique())
                        && rec.getFcstHour() < 0) {
                    updatedADeckRecords.add(rec);
                }
            }
        } else {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "No A-Deck records available for DTG  " + dtg);
        }

        // Update B-Deck records
        if (bdeckSandboxID <= 0) {
            bdeckSandboxID = AtcfDataUtil.getBDeckSandbox(storm);
        }

        if (retrievedBDeckRecords != null
                && retrievedBDeckRecords.containsKey(dtg)) {
            for (BDeckRecord rec : retrievedBDeckRecords.get(dtg)) {
                if ("BEST".equals(rec.getTechnique())
                        && rec.getFcstHour() == 0) {
                    // Information for current DTG
                    rec.setClat(latValue);
                    rec.setClon(lonValue);
                    rec.setWindMax(maxWnd);
                    rec.setStormDrct(dir);
                    rec.setStormSped(spd);
                    rec.setEyeSize(eyeSize);
                    rec.setMaxWindRad(maxWindRad);
                    rec.setStormDepth(depth);
                    rec.setMslp(pres);
                    rec.setClosedP(closedPres);
                    rec.setRadClosedP(radClosedP);
                    // Update speed for each quadrant.
                    if (rec.getRadWind() == 34) {
                        updateQuadrant(rec, ktCombos1);
                    } else if (rec.getRadWind() == 50) {
                        updateQuadrant(rec, ktCombos2);
                    } else if (rec.getRadWind() == 64) {
                        updateQuadrant(rec, ktCombos3);
                    }
                    updatedBDeckRecords.add(rec);
                }
            }
        } else {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "No B-Deck records available for DTG  " + dtg
                            + ". B-Deck will not be updated.");
        }
    }

    /**
     * Update an ADeckRecord's quadrants
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
     * Update a BDeckRecord's quadrants
     *
     * @param rec
     *            BDeckRecord
     * @param ktCombos
     *            CCombs to get quadrant values
     *
     */
    private void updateQuadrant(BDeckRecord rec, CCombo[] ktCombos) {
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
     * Create the compute file from the updated A-deck records
     *
     * @param updatedADeckRecords
     */
    private void createComFile(
            List<ADeckRecord> updatedADeckRecords) {

        List<ADeckRecord> fullRecords = new ArrayList<>();

        Map<String, List<ADeckRecord>> prevDtgADeckRecords = new HashMap<>();
        List<ADeckRecord> prevDtgRecs = new ArrayList<>();

        /*
         * Get the A-Deck records for the previous DTG in order to pull the OFCL
         * or OFCP entries into the .com file
         */

        prevDtgADeckRecords = AtcfDataUtil.retrieveADeckData(storm,
                new String[] { ofclDtg }, false);

        if (prevDtgADeckRecords != null && !prevDtgADeckRecords.isEmpty()) {
            prevDtgRecs = prevDtgADeckRecords.get(ofclDtg);
        } else {
            statusHandler.warn(
                    "No previous OFCL records for storm " + storm.getStormName()
                            + ". Compute file will contain CARQ only");
        }

        /*
         * In .com file, OFCL lines are first followed by CARQ lines, ordered by
         * forecast time, then by radWind (34,50,64)
         */
        class SortByTauAndRadWnd implements Comparator<ADeckRecord> {
            @Override
            public int compare(ADeckRecord rec2, ADeckRecord rec1) {
                int fcstCmp = Integer.compare(rec2.getFcstHour(),
                        rec1.getFcstHour());
                float radWndCmp = Float.compare(rec2.getRadWind(),
                        rec1.getRadWind());
                return (int) ((fcstCmp != 0) ? fcstCmp : radWndCmp);
            }
        }
        Collections.sort(prevDtgRecs, new SortByTauAndRadWnd());
        Collections.sort(updatedADeckRecords, new SortByTauAndRadWnd());

        for (ADeckRecord rec : prevDtgRecs) {
            if ("OFCL".equals(rec.getTechnique())
                    || "OFCP".equals(rec.getTechnique())) {
                fullRecords.add(rec);
            }
        }

        // updatedADeckRecords contains the CARQ lines for this DTG
        for (ADeckRecord rec : updatedADeckRecords) {
            fullRecords.add(rec);
        }

        PrepareComputeRequest sendReq = new PrepareComputeRequest(storm,
                fullRecords);

        Object retval = null;
        try {
            retval = ThriftClient.sendRequest(sendReq);
            if (retval instanceof String
                    && !((String) retval).contains("Success")) {
                statusHandler.handle(Priority.SIGNIFICANT, (String) retval);
            }
        } catch (VizException e) {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "Exception while creating compute file.", e);
        }
    }

    /**
     * Put updated A-deck records into sandbox
     *
     * @param updatedADeckRecords
     */
    private void saveADeckDataToSbx(
            List<ADeckRecord> updatedADeckRecords) {

        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();

        for (ADeckRecord rec : updatedADeckRecords) {

            // Add to list of ModifiedDeckRecord
            RecordEditType editType = RecordEditType.MODIFY;
            if (rec.getId() <= 0) {
                editType = RecordEditType.NEW;
            }

            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            mdr.setRecord(rec);
            modifiedRecords.add(mdr);

        }

        // batch update into the sandbox
        if (!modifiedRecords.isEmpty()) {
            AtcfDataUtil.updateDeckRecords(adeckSandboxID, AtcfDeckType.A,
                    modifiedRecords);
        }
    }

    /**
     * Put updated B-deck records into sandbox
     *
     * @param updatedBDeckRecords
     */
    private void saveBDeckDataToSbx(
            List<BDeckRecord> updatedBDeckRecords) {

        List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();

        for (BDeckRecord rec : updatedBDeckRecords) {

            // Add to list of ModifiedDeckRecord
            RecordEditType editType = RecordEditType.MODIFY;
            if (rec.getId() <= 0) {
                editType = RecordEditType.NEW;
            }

            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            mdr.setRecord(rec);
            modifiedRecords.add(mdr);

        }

        // batch update into the sandbox
        if (!modifiedRecords.isEmpty()) {
            AtcfDataUtil.updateDeckRecords(bdeckSandboxID, AtcfDeckType.B,
                    modifiedRecords);
        }

    }

    /*
     * Refresh BDeck data in AtcfProduct.
     */
    private void refreshBDeck() {
        // Refresh BDeck data in Atcf
        AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                .getResourceData().getAtcfProduct(storm);

        prd.getBDeckDataMap().clear();

        retrievedBDeckRecords.clear();
        retrievedBDeckRecords.putAll(AtcfDataUtil.getBDeckRecords(storm, true));
        prd.setBDeckData(retrievedBDeckRecords);

    }

    /**
     * Redraw best track with option to overlap baseline.
     *
     * @param baseline
     *            indicate whether baseline to be included in display
     */
    private void redraw(boolean baseline) {
        AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
        AtcfProduct prd = rsc.getResourceData().getAtcfProduct(storm);
        BestTrackProperties btp = prd.getBestTrackProperties();
        if (btp != null) {
            BestTrackGenerator btkGen = new BestTrackGenerator(rsc, btp, storm,
                    baseline);
            btkGen.create();
        }
    }
}

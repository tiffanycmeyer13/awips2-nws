/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.track;

import java.util.ArrayList;
import java.util.Map;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.dataplugin.atcf.AtcfDeckType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ModifiedDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.RecordEditType;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfTextListeners;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.StormDevelopment;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Edit Best Track.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 13, 2018 52656      dmanzella   initial creation
 * Oct 12, 2018 52656      dmanzella   implemented backend capabilities
 * Nov 02, 2018 57099      dmanzella   updated GUI and functionality
 * Feb 26, 2019 60613      jwu         Pass in BdeckRecords as a map.
 * Mar 29, 2019 61590      dfriedman   Use new request types.
 * Aug 16, 2019 67323      jwu         Add verify listeners and adjust layout.
 * Sep 04, 2019 68112      jwu         Save with batch processing.
 * Nov 06, 2019 70959      jwu         Fix exception at "Submit".
 * Nov 10, 2020 84442      wpaintsil   Add scrollbars.
 * Dec 10, 2020 85849      jwu         Redraw track after save.
 * May 17, 2021 91567      jwu         Adjust layout.
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class EditBestTrackDialog extends OcpCaveSWTDialog {

    private static String[] topLabels = { "Latitude", "Longitude", "Wind Speed",
            "Highest Development", "Min Sea Level Pressure", "Max Wind Radius",
            "Outermost Closed Isobar", "Radius Outermost Closed Isobar" };

    private static String[] measurements = { "kts", "", "mb", "nm", "mb",
            "nm" };

    private static String[][] winds = { { "34 kt winds", "NE", "SE", "SW" },
            { "50 kt winds", "NE", "SE", "SW" },
            { "64 kt winds", "NE", "SE", "SW" } };

    private Button submitButton;

    private Button saveButton;

    private Control[] verticalList;

    private CCombo[][] ktCombos;

    private Text userTitleText;

    private Text userDataText;

    private ArrayList<Button> directionalButtons;

    private Storm storm;

    private Map<String, java.util.List<BDeckRecord>> bdeckDataMap;

    private ArrayList<BDeckRecord> selectedRecords;

    // List of unique date time group strings
    private List dtgList;

    private int currsandboxID;

    /**
     * Collection of verify listeners.
     */
    private final AtcfTextListeners verifyListeners = new AtcfTextListeners();

    /**
     * Constructor
     *
     * @param parent
     * @param storm
     * @param sandboxID
     * @param pdos
     */
    public EditBestTrackDialog(Shell parent, Storm storm, int sandboxID,
            Map<String, java.util.List<BDeckRecord>> bdeckDataMap) {
        super(parent);
        this.storm = storm;
        setText("Edit Best Track - " + this.storm.getStormName() + " "
                + this.storm.getStormId());

        this.bdeckDataMap = bdeckDataMap;

        currsandboxID = sandboxID;
    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        Composite mainComposite = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        mainComposite.setLayoutData(mainLayoutData);

        createTopGrid(mainComposite);
        createMiddleArea(mainComposite);
        createBottomArea(mainComposite);
        createControlButtons(mainComposite);

        selectedRecords = new ArrayList<>();
        selectedRecords.addAll(bdeckDataMap.get(dtgList.getSelection()[0]));
        populateData(selectedRecords);

        scrollComposite.setContent(mainComposite);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setMinSize(
                mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Creates the top section of the GUI
     *
     * @param parent
     */
    protected void createTopGrid(Composite parent) {
        Composite topComposite = new Composite(parent, SWT.NONE);
        GridLayout topGridLayout = new GridLayout(3, false);
        topGridLayout.marginWidth = 15;
        topGridLayout.marginHeight = 15;
        topGridLayout.horizontalSpacing = 15;
        topComposite.setLayout(topGridLayout);

        // First Composite
        Composite leftTopComposite = new Composite(topComposite, SWT.NONE);
        GridLayout leftTopGridLayout = new GridLayout(1, false);
        leftTopGridLayout.marginWidth = 15;
        leftTopGridLayout.marginHeight = 15;
        leftTopGridLayout.verticalSpacing = 8;
        leftTopComposite.setLayout(leftTopGridLayout);

        Label mainLabel = new Label(leftTopComposite, SWT.CENTER);
        mainLabel.setText("DTG (YYYYMMDDHH)");
        GridData dtgLblGridData = new GridData(SWT.CENTER, SWT.NONE, false,
                false);
        mainLabel.setLayoutData(dtgLblGridData);

        // Set up sorted DTG list
        String[] dtgss = bdeckDataMap.keySet().toArray(new String[0]);
        dtgList = new List(leftTopComposite,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        dtgList.setItems(dtgss);
        dtgList.setLayoutData(new GridData(150, 190));

        dtgList.select(dtgss.length - 1);
        dtgList.setTopIndex(dtgss.length - 1);

        dtgList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedRecords = new ArrayList<>();
                selectedRecords
                        .addAll(bdeckDataMap.get(dtgList.getSelection()[0]));
                populateData(selectedRecords);
            }
        });

        MessageBox deleteDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        deleteDialog.setText("Alert");
        deleteDialog.setMessage("Are you sure you wish to delete this record?");

        GridData deleteBtnGD = new GridData(SWT.LEFT, SWT.NONE, false, false);
        deleteBtnGD.horizontalIndent = 20;
        Button deleteRecord = new Button(leftTopComposite, SWT.LEFT);
        deleteRecord.setText("Delete Record");
        deleteRecord.setLayoutData(deleteBtnGD);
        deleteRecord.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int deleteID = deleteDialog.open();
                if (deleteID == SWT.OK) {
                    delete();
                }
            }
        });

        // Right Composite
        createRightPanel(topComposite);
    }

    /*
     * Creates the widgets at right side.
     */
    private void createRightPanel(Composite parent) {

        Composite topRightComposite = new Composite(parent, SWT.NONE);
        GridLayout topRightGridLayout = new GridLayout(3, false);
        topRightGridLayout.marginWidth = 0;
        topRightGridLayout.horizontalSpacing = 10;
        topRightGridLayout.marginHeight = 15;
        topRightComposite.setLayout(topRightGridLayout);
        verticalList = new Control[8];
        directionalButtons = new ArrayList<>();

        for (int i = 0; i < 8; i++) {

            GridData rightAlignGridData = new GridData(SWT.RIGHT, SWT.CENTER,
                    true, false);
            Label boxLabel = new Label(topRightComposite, SWT.NONE);
            boxLabel.setText(topLabels[i]);
            boxLabel.setLayoutData(rightAlignGridData);

            GridData textAlignGridData = new GridData(SWT.LEFT, SWT.CENTER,
                    true, false);

            if (i == 3) {
                textAlignGridData.horizontalSpan = 2;
            }

            if (i < 2) {
                verticalList[i] = new Text(topRightComposite, SWT.BORDER);
                textAlignGridData.minimumWidth = 50;
                verticalList[i].setLayoutData(textAlignGridData);
                ((Text) verticalList[i]).setText("");
            } else {
                verticalList[i] = new CCombo(topRightComposite, SWT.BORDER);
                ((CCombo) verticalList[i]).setText("");
                verticalList[i].setLayoutData(textAlignGridData);
            }

            if (i > 1) {
                if (i != 3) {
                    Label label = new Label(topRightComposite, SWT.NONE);
                    label.setText(measurements[i - 2]);
                }
            } else {
                GridData dirGrpGD = new GridData(SWT.LEFT, SWT.CENTER, true,
                        false);
                Group radioGroup = new Group(topRightComposite, SWT.NONE);
                GridLayout dirGrpLayout = new GridLayout(2, false);
                dirGrpLayout.marginWidth = 0;
                dirGrpLayout.marginHeight = 0;
                radioGroup.setLayout(dirGrpLayout);
                radioGroup.setLayoutData(dirGrpGD);

                Button radioButtonOne = new Button(radioGroup, SWT.RADIO);
                radioButtonOne.setText(i == 0 ? "N" : "E");
                directionalButtons.add(radioButtonOne);

                Button radioButtonTwo = new Button(radioGroup, SWT.RADIO);
                radioButtonTwo.setText(i == 0 ? "S" : "W");
                directionalButtons.add(radioButtonTwo);
            }
        }

        // Populate dropdowns
        populateRightPanel();
    }

    /*
     * Populate widgets at right side
     */
    private void populateRightPanel() {

        String[] windMax = new String[51];
        for (int i = 0; i <= 50; i++) {
            windMax[i] = String.valueOf(i * 5);
        }
        ((CCombo) verticalList[2]).setItems(windMax);
        ((CCombo) verticalList[3])
                .setItems(StormDevelopment.getStormTypeStrings());

        String[] mslp = new String[41];
        for (int i = 0; i <= 40; i++) {
            mslp[i] = String.valueOf((i * 5) + 850);
        }
        ((CCombo) verticalList[4]).setItems(mslp);

        String[] maxWindRadius = new String[201];
        String[] radOutermostClosedIsobar = new String[201];

        for (int i = 0; i <= 200; i++) {
            maxWindRadius[i] = String.valueOf(i * 5);
            radOutermostClosedIsobar[i] = String.valueOf(i * 5);
        }
        ((CCombo) verticalList[5]).setItems(maxWindRadius);
        ((CCombo) verticalList[7]).setItems(radOutermostClosedIsobar);

        String[] outermostClosedIsobar = new String[151];
        for (int i = 0; i <= 150; i++) {
            outermostClosedIsobar[i] = String.valueOf(i + 900);
        }
        ((CCombo) verticalList[6]).setItems(outermostClosedIsobar);

        // Add verification listeners for lat/lon/max wind
        verticalList[0].addListener(SWT.Verify,
                verifyListeners.getLatVerifyListener());

        verticalList[1].addListener(SWT.Verify,
                verifyListeners.getLonVerifyListener());

        ((CCombo) verticalList[2])
                .addVerifyListener(verifyListeners.getWindCmbVerifyListener());

    }

    /**
     * Creates the middle section of the dialog
     *
     * @param parent
     */
    protected void createMiddleArea(Composite parent) {
        GridLayout middleAreaGridLayout = new GridLayout(9, false);
        middleAreaGridLayout.marginLeft = 30;
        middleAreaGridLayout.marginRight = 15;
        middleAreaGridLayout.marginHeight = 15;
        middleAreaGridLayout.horizontalSpacing = 10;
        GridData middleAreaGridData = new GridData(SWT.LEFT, SWT.NONE, false,
                false);

        Composite middleAreaComposite = new Composite(parent, SWT.NONE);
        middleAreaComposite.setLayout(middleAreaGridLayout);
        middleAreaComposite.setLayoutData(middleAreaGridData);

        ktCombos = new CCombo[3][4];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                String[] data = new String[201];

                for (int k = 0; k <= 200; k++) {
                    data[k] = String.valueOf(k * 5);
                }

                GridData hIndentGridData = new GridData(SWT.NONE, SWT.NONE,
                        false, false);
                hIndentGridData.horizontalIndent = 25;

                Label label = new Label(middleAreaComposite, SWT.NONE);
                label.setText(winds[i][j]);

                ktCombos[i][j] = new CCombo(middleAreaComposite, SWT.BORDER);
                ktCombos[i][j].setLayoutData(hIndentGridData);
                ktCombos[i][j].setItems(data);
                ktCombos[i][j].select(0);

            }

            Label northW = new Label(middleAreaComposite, SWT.NONE);
            northW.setText("NW");
        }
    }

    /**
     * Creates the Current Forecast section
     *
     * @param parent
     */
    protected void createBottomArea(Composite parent) {
        GridLayout bottomAreaGridLayout = new GridLayout(1, false);
        bottomAreaGridLayout.marginWidth = 15;
        bottomAreaGridLayout.marginHeight = 15;
        Composite bottomAreaComposite = new Composite(parent, SWT.NONE);
        bottomAreaComposite.setLayout(bottomAreaGridLayout);

        Label userTitleLabel = new Label(bottomAreaComposite, SWT.NONE);
        userTitleLabel.setText("User Defined Title (max 20 chars, no spaces)");

        GridData userTitleGridData = new GridData(SWT.NONE, SWT.NONE, false,
                false);
        userTitleGridData.widthHint = 200;

        userTitleText = new Text(bottomAreaComposite, SWT.BORDER);
        userTitleText.setLayoutData(userTitleGridData);

        Label userDataLabel = new Label(bottomAreaComposite, SWT.NONE);
        userDataLabel.setText("User Defined Data (max 200 chars, spaces OK)");

        GridData userDataGridData = new GridData(SWT.NONE, SWT.NONE, false,
                false);
        userDataGridData.widthHint = 640;

        userDataText = new Text(bottomAreaComposite, SWT.BORDER);
        userDataText.setLayoutData(userDataGridData);
    }

    /**
     * Creates the help, apply, ok, and cancel buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        GridLayout okCancelGridLayout = new GridLayout(4, true);
        okCancelGridLayout.marginWidth = 15;
        okCancelGridLayout.marginHeight = 15;
        Composite okCancelComposite = new Composite(parent, SWT.NONE);
        okCancelComposite.setLayout(okCancelGridLayout);
        okCancelComposite.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(okCancelComposite,
                AtcfVizUtil.buttonGridData());

        saveButton = new Button(okCancelComposite, SWT.CENTER);
        saveButton.setToolTipText("Save changes to sandbox B-deck table. ");
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.setLayoutData(AtcfVizUtil.buttonGridData());
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                save();
                submitButton.setEnabled(true);
            }
        });

        MessageBox submitDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
        submitDialog.setText("Alert");
        submitDialog
                .setMessage("Are you sure you wish to submit your changes?");

        submitButton = new Button(okCancelComposite, SWT.CENTER);
        submitButton.setText("Submit");
        submitButton.setEnabled(false);
        submitButton.setToolTipText(
                "Submit all changes to operational B-Deck table");
        submitButton.setLayoutData(AtcfVizUtil.buttonGridData());
        submitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int submitID = submitDialog.open();

                if (submitID == SWT.OK) {
                    submitButton.setEnabled(false);
                    submit();
                }
            }
        });

        Button cancelButton = new Button(okCancelComposite, SWT.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                redraw(false);
                close();
            }
        });
    }

    /*
     * Check-in all sandbox changes to the operational B-Deck table
     */
    private void submit() {
        AtcfDataUtil.checkinBDeckRecords(currsandboxID);
        redraw(false);
        close();
    }

    /*
     * Save changes to the sandbox B-Deck table
     */
    private void save() {

        java.util.List<ModifiedDeckRecord> modifiedRecords = new ArrayList<>();

        for (BDeckRecord rec : selectedRecords) {

            BDeckRecord toWrite = rec;

            String lat = ((Text) verticalList[0]).getText();
            int mult = (directionalButtons.get(0).getSelection() ? 1 : -1);
            toWrite.setClat(Float.valueOf(lat) * mult);

            String lon = ((Text) verticalList[1]).getText();
            mult = (directionalButtons.get(2).getSelection() ? 1 : -1);
            toWrite.setClon(Float.valueOf(lon) * mult);

            toWrite.setWindMax(
                    Float.valueOf(((CCombo) verticalList[2]).getText()));

            toWrite.setIntensity(StormDevelopment.getShortIntensityString(
                    ((CCombo) verticalList[3]).getText()));

            toWrite.setMslp(
                    Float.valueOf(((CCombo) verticalList[4]).getText()));

            toWrite.setMaxWindRad(
                    Float.valueOf(((CCombo) verticalList[5]).getText()));

            toWrite.setClosedP(
                    Float.valueOf(((CCombo) verticalList[6]).getText()));

            toWrite.setRadClosedP(
                    Float.valueOf(((CCombo) verticalList[7]).getText()));

            toWrite.setUserDefined(userTitleText.getText());

            toWrite.setUserData(userDataText.getText());

            int update;
            if (toWrite.getRadWind() == 50.0) {
                update = 1;
            } else if (toWrite.getRadWind() == 64.0) {
                update = 2;
            } else {
                update = 0;
            }

            toWrite.setQuad1WindRad(
                    Float.valueOf(ktCombos[update][0].getText()));
            toWrite.setQuad2WindRad(
                    Float.valueOf(ktCombos[update][1].getText()));
            toWrite.setQuad3WindRad(
                    Float.valueOf(ktCombos[update][2].getText()));
            toWrite.setQuad4WindRad(
                    Float.valueOf(ktCombos[update][3].getText()));

            // Add to list of ModifiedDeckRecord
            RecordEditType editType = RecordEditType.MODIFY;
            if (toWrite.getId() <= 0) {
                editType = RecordEditType.NEW;
            }

            ModifiedDeckRecord mdr = new ModifiedDeckRecord();
            mdr.setEditType(editType);
            mdr.setRecord(toWrite);
            modifiedRecords.add(mdr);

        }

        // batch update into the sandbox
        if (!modifiedRecords.isEmpty()) {
            AtcfDataUtil.updateDeckRecords(currsandboxID, AtcfDeckType.B,
                    modifiedRecords);

            // Refresh BDeck data in Atcf
            AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                    .getResourceData().getAtcfProduct(storm);

            prd.getBDeckDataMap().clear();

            bdeckDataMap.clear();
            bdeckDataMap.putAll(AtcfDataUtil.getBDeckRecords(storm, true));
            prd.setBDeckData(bdeckDataMap);

            redraw(true);
        }

    }

    /*
     * Mark records as DELETE in the sandbox
     */
    private void delete() {
        dtgList.remove(dtgList.getSelectionIndex());
        for (BDeckRecord rec : selectedRecords) {
            AtcfDataUtil.updateBDeckRecord(currsandboxID, rec,
                    RecordEditType.DELETE);
        }

        redraw(true);
    }

    /*
     * Populate data based on the DTG selection.
     *
     * @param records
     */
    private void populateData(java.util.List<BDeckRecord> records) {

        setWindRadiiComboes(records);

        // Pull Latitude
        if (verticalList[0] != null) {
            float latValue = records.get(0).getClat();
            String latString = String.format("%.02f", Math.abs(latValue));
            boolean posLat = latValue >= 0;
            directionalButtons.get(0).setSelection(posLat);
            directionalButtons.get(1).setSelection(!posLat);
            ((Text) verticalList[0]).setText(latString);
        }

        // Pull Longitude
        if (verticalList[1] != null) {
            float lonValue = records.get(0).getClon();
            String lonString = String.format("%.02f", Math.abs(lonValue));
            boolean posLon = lonValue >= 0;
            directionalButtons.get(2).setSelection(posLon);
            directionalButtons.get(3).setSelection(!posLon);
            ((Text) verticalList[1]).setText(lonString);
        }

        // Pull Wind Speed
        if (verticalList[2] != null) {
            ((CCombo) verticalList[2]).setText(
                    String.format("%d", (int) records.get(0).getWindMax()));
        }

        // Pull Highest Development
        if (verticalList[3] != null) {
            ((CCombo) verticalList[3]).setText(StormDevelopment
                    .getFullIntensityString(records.get(0).getIntensity()));
        }

        // Pull Min Sea Level Pressure
        if (verticalList[4] != null) {
            ((CCombo) verticalList[4]).setText(
                    String.format("%d", (int) records.get(0).getMslp()));
        }

        // Pull Max Wind Radius
        if (verticalList[5] != null) {
            ((CCombo) verticalList[5]).setText(
                    String.format("%d", (int) records.get(0).getMaxWindRad()));
        }

        // Pull Outermost Closed Isobar
        if (verticalList[6] != null) {
            ((CCombo) verticalList[6]).setText(
                    String.format("%d", (int) records.get(0).getClosedP()));
        }

        // Pull Radius Outermost Closed Isobar
        if (verticalList[7] != null) {
            ((CCombo) verticalList[7]).setText(
                    String.format("%d", (int) records.get(0).getRadClosedP()));
        }

        userTitleText.setText(records.get(0).getUserDefined());
        userDataText.setText(records.get(0).getUserData());

        saveButton.setEnabled(true);
    }

    /*
     * Reset all wind radii comboes.
     */
    private void setWindRadiiComboes(java.util.List<BDeckRecord> records) {

        for (CCombo[] combo : ktCombos) {
            for (CCombo element2 : combo) {
                element2.setText("0");
            }
        }

        for (BDeckRecord rec : records) {
            int update;
            if (rec.getRadWind() == 50.0) {
                update = 1;
            } else if (rec.getRadWind() == 64.0) {
                update = 2;
            } else {
                update = 0;
            }
            ktCombos[update][0]
                    .setText(String.format("%d", (int) rec.getQuad1WindRad()));
            ktCombos[update][1]
                    .setText(String.format("%d", (int) rec.getQuad2WindRad()));
            ktCombos[update][2]
                    .setText(String.format("%d", (int) rec.getQuad3WindRad()));
            ktCombos[update][3]
                    .setText(String.format("%d", (int) rec.getQuad4WindRad()));
        }
    }

    /*
     * Redraw best track with option to overlap baseline.
     */
    private void redraw(boolean baseline) {
        AtcfResource rsc = AtcfSession.getInstance().getAtcfResource();
        AtcfProduct prd = rsc.getResourceData().getAtcfProduct(storm);
        BestTrackProperties btp = prd.getBestTrackProperties();

        BestTrackGenerator btkGen = new BestTrackGenerator(rsc, btp, storm,
                baseline);
        btkGen.create();
    }

}
/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes.enterfixdata;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.TabFolder;

import gov.noaa.nws.ocp.common.dataplugin.atcf.FDeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.FixFormat;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfProduct;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Enter/Edit Fixes Data menu.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 21, 2019 54779       wpaintsil   initial creation
 * Aug 15, 2019 65561       dmanzella   implemented backend functionality
 * Oct 01, 2019 68738       dmanzella   added support for Edit Fixes
 * Nov 10, 2020 84442       wpaintsil   Add Scrollbars.
 * Apr 01, 2021 87786       wpaintsil   Revise UI.
 * Jun 24, 2021 91759       wpaintsil   Move Redraw Fixes to AtcfVizUtil.
 * </pre>
 *
 * @author wpaintsil
 * @version 1.0
 *
 */
public class EditFixesDataDialog extends OcpCaveSWTDialog {

    public static final String ASTERISK_NOTE = "* Fields marked with an asterisk (*) are required.";

    private static final String[] ACTION_BTN_NAMES = new String[] {
            "Save Changes", "Delete Record", "Flag Fix" };

    private String[] actionBtnNames;

    private Storm storm;

    private int sandBoxId;

    private Map<FixFormat, EditFixesTab> tabComposites = new EnumMap<>(
            FixFormat.class);

    private boolean editData;

    // Sort fix data by fix format and then by dtg/fixsite/fixtype.
    private Map<String, Map<FDeckRecordKey, List<FDeckRecord>>> fixDataMap;

    private TabFolder tabFolder;

    private org.eclipse.swt.widgets.List dtgSelectionList;

    /**
     * Constructor
     * 
     * @param parent
     * @param storm
     * @param fixData
     * @param sandBox
     * @param edit
     */
    public EditFixesDataDialog(Shell parent, Storm storm,
            List<FDeckRecord> fixData, int sandBox, boolean edit) {
        super(parent);
        this.storm = storm;
        sandBoxId = sandBox;
        this.editData = edit;

        if (editData) {
            setText("Edit Fixes - " + storm.getStormName() + " "
                    + storm.getStormId());
            actionBtnNames = ACTION_BTN_NAMES;
        } else {
            setText("Enter Fixes - " + storm.getStormName() + " "
                    + storm.getStormId());
            actionBtnNames = new String[] { "Add Record" };
        }

        fixDataMap = groupFDeckDataByFormat(fixData);
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
        GridLayout mainLayout = new GridLayout(editData ? 2 : 1, false);
        mainLayout.marginWidth = 10;
        mainComposite.setLayout(mainLayout);
        GridData mainLayoutData = new GridData(SWT.CENTER, SWT.FILL, true,
                false);
        mainComposite.setLayoutData(mainLayoutData);

        if (editData) {
            createDtgList(mainComposite);
        }

        Composite rightComposite = new Composite(mainComposite, SWT.NONE);
        GridLayout rtLayout = new GridLayout(1, false);
        rightComposite.setLayout(rtLayout);
        GridData rtLayoutData = new GridData(SWT.CENTER, SWT.FILL, true, false);
        rightComposite.setLayoutData(rtLayoutData);

        createTabs(rightComposite);
        createControlButtons(rightComposite);

        scrollComposite.setContent(mainComposite);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite.setMinSize(
                mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Create a side panel with a list of DTGs for the edit version of the
     * dialog.
     * 
     * @param parent
     */
    private void createDtgList(Composite parent) {
        Composite leftComposite = new Composite(parent, SWT.NONE);
        GridLayout leftLayout = new GridLayout(1, false);
        leftLayout.verticalSpacing = 10;
        leftComposite.setLayout(leftLayout);
        GridData leftLayoutData = new GridData(SWT.CENTER, SWT.FILL, true,
                false);
        leftComposite.setLayoutData(leftLayoutData);

        Label selectDtgLbl = new Label(leftComposite, SWT.NONE);
        selectDtgLbl.setText("Select DTG of record to edit");

        dtgSelectionList = new org.eclipse.swt.widgets.List(leftComposite,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);

        updateDtgList();

        dtgSelectionList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                String dtgString = dtgSelectionList
                        .getItem(dtgSelectionList.getSelectionIndex());

                tabComposites
                        .get(FixFormat.values()[tabFolder.getSelectionIndex()])
                        .setDtgText(
                                dtgString.substring(0, dtgString.indexOf(' ')));

            }
        });

        dtgSelectionList.setLayoutData(new GridData(200, 200));
    }

    /**
     * Helper method for displaying the appropriate dtg list for each tab.
     */
    private void updateDtgList() {
        List<String> dtgList = new ArrayList<>();
        int selectionIndex = 0;
        if (tabFolder != null && !tabFolder.isDisposed()
                && tabFolder.getSelectionIndex() > 0) {
            selectionIndex = tabFolder.getSelectionIndex();
        }

        Map<FDeckRecordKey, List<FDeckRecord>> tabFixDataMap = fixDataMap
                .get(FixFormat.values()[selectionIndex].getValue());

        for (FDeckRecordKey key : tabFixDataMap.keySet()) {
            if (!dtgList.contains(key.getDtg())) {
                dtgList.add(key.getDtg() + " "
                        + StringUtils.rightPad(key.getFixSite(), 4) + " "
                        + key.getFixType());
            }
        }

        if (dtgSelectionList != null) {
            if (dtgSelectionList.getItemCount() > 0) {
                dtgSelectionList.removeAll();
            }
            for (int ii = 0; ii < dtgList.size(); ii++) {
                dtgSelectionList.add(dtgList.get(ii));
            }
            if (dtgSelectionList.getItemCount() > 0) {
                dtgSelectionList.select(0);
            }
        }
    }

    /**
     * Assemble the tabs for this dialog.
     * 
     * @param parent
     */
    private void createTabs(Composite parent) {
        tabFolder = new TabFolder(parent, SWT.TOP | SWT.BORDER);

        tabFolder
                .setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        SatelliteSubjTab satSubjTab = new SatelliteSubjTab(tabFolder,
                FixFormat.SUBJECTIVE_DVORAK.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.SUBJECTIVE_DVORAK.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.SUBJECTIVE_DVORAK, satSubjTab);

        SatelliteObjTab satObjTab = new SatelliteObjTab(tabFolder,
                FixFormat.OBJECTIVE_DVORAK.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.OBJECTIVE_DVORAK.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.OBJECTIVE_DVORAK, satObjTab);

        MicrowaveScatterTab microwvTab = new MicrowaveScatterTab(tabFolder,
                FixFormat.MICROWAVE.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.MICROWAVE.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.MICROWAVE, microwvTab);

        MicrowaveScatterTab scatterTab = new MicrowaveScatterTab(tabFolder,
                FixFormat.SCATTEROMETER.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.SCATTEROMETER.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.SCATTEROMETER, scatterTab);

        RadarTab radarTab = new RadarTab(tabFolder,
                FixFormat.RADAR.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.RADAR.getValue()), dtgSelectionList);
        tabComposites.put(FixFormat.RADAR, radarTab);

        AircraftTab aircraftTab = new AircraftTab(tabFolder,
                FixFormat.AIRCRAFT.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.AIRCRAFT.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.AIRCRAFT, aircraftTab);

        DropsondeTab dropsTab = new DropsondeTab(tabFolder,
                FixFormat.DROPSONDE.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.DROPSONDE.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.DROPSONDE, dropsTab);

        AnalysisSynopticTab analysisTab = new AnalysisSynopticTab(tabFolder,
                FixFormat.ANALYSIS.getDescription(), storm, editData,
                actionBtnNames, sandBoxId,
                fixDataMap.get(FixFormat.ANALYSIS.getValue()),
                dtgSelectionList);
        tabComposites.put(FixFormat.ANALYSIS, analysisTab);

        tabFolder.setSelection(0);

        tabFolder.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDtgList();
            }
        });
    }

    /**
     * Create control buttons.
     * 
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(2, true);
        buttonLayout.horizontalSpacing = 40;
        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Button submitButton = new Button(buttonComp, SWT.PUSH);
        submitButton.setText("Submit");
        GridData sumbitButtonData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        sumbitButtonData.widthHint = 120;
        submitButton.setLayoutData(sumbitButtonData);
        submitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                submitButton();
            }
        });

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
        cancelButton.setText("Close");
        GridData cancelButtonData = new GridData(SWT.FILL, SWT.DEFAULT, true,
                false);
        cancelButtonData.widthHint = 120;
        cancelButton.setLayoutData(cancelButtonData);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();

            }
        });
    }

    /**
     * Group a list of FDeckRecord by DTG/Site/Type, including all wind radii -
     * 34kt, 50kt, and 64kt) and all other possibilities.
     * 
     * @param recs
     *            list of FDeckRecords
     * 
     * @return Map<RecordKey, ADeckRecord>
     */
    private Map<String, Map<FDeckRecordKey, List<FDeckRecord>>> groupFDeckDataByFormat(
            List<FDeckRecord> recs) {

        Map<String, Map<FDeckRecordKey, List<FDeckRecord>>> recordMap = new HashMap<>();
        Map<String, List<FDeckRecord>> iniRecMap = new HashMap<>();

        for (FixFormat fmt : FixFormat.values()) {
            iniRecMap.put(fmt.getValue(), new ArrayList<>());
            recordMap.put(fmt.getValue(), new TreeMap<>());
        }

        for (FDeckRecord rec : recs) {
            List<FDeckRecord> dataList = iniRecMap.get(rec.getFixFormat());
            dataList.add(rec);
        }

        for (Map.Entry<String, List<FDeckRecord>> entry : iniRecMap
                .entrySet()) {
            List<FDeckRecord> dataList = entry.getValue();
            if (!dataList.isEmpty()) {
                recordMap.put(entry.getKey(), groupFDeckData(dataList));
            }
        }

        return recordMap;
    }

    /**
     * Group a list of FDeckRecord by DTG/Site/Type, including all wind radii -
     * 34kt, 50kt, and 64kt) and all other possibilities.
     * 
     * @param recs
     *            list of FDeckRecords
     * 
     * @return Map<RecordKey, ADeckRecord>
     */
    private Map<FDeckRecordKey, List<FDeckRecord>> groupFDeckData(
            List<FDeckRecord> recs) {

        // Group records by DTG/Site/Type
        Map<FDeckRecordKey, List<FDeckRecord>> recordMap = new TreeMap<>();

        for (FDeckRecord rec : recs) {
            String dtg = AtcfDataUtil
                    .calendarToLongDateTimeString(rec.getRefTimeAsCalendar());
            String site = rec.getFixSite();
            String type = rec.getFixType();
            String satType = rec.getSatelliteType();

            FDeckRecordKey rkey = new FDeckRecordKey(dtg, site, type, satType);
            List<FDeckRecord> recList = recordMap.computeIfAbsent(rkey,
                    k -> new ArrayList<>());
            recList.add(rec);
        }

        return recordMap;
    }

    /**
     * Controls for the submit button
     */
    private void submitButton() {
        MessageBox confirmationDialog = new MessageBox(shell,
                SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmationDialog.setText("Alert");

        String confirmationString = "Do you want to submit your changes to baseline? Once submitted, you cannot roll back.";

        confirmationDialog.setMessage(confirmationString);
        int result = confirmationDialog.open();
        if (result == SWT.YES) {

            AtcfDataUtil.checkinFDeckRecords(sandBoxId);

            // Reset sandbox ID for this storm product.
            AtcfProduct prd = AtcfSession.getInstance().getAtcfResource()
                    .getResourceData().getAtcfProduct(storm);

            prd.setFdeckSandboxID(-1);

            AtcfVizUtil.redrawFixes();
            close();
        }

    }

}

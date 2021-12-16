/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.forecast;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Forecast"=>"Forecast Intensity" => "View Intensity Graph / Make
 * Forecast" to allow the user to select obj aids to be used for intensity
 * forecast.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 03, 2020 71724      jwu         Initial creation.
 * May 28, 2020 78027      jwu         Update to use selection in Forecast 
 *                                     Intensity Dialog.
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ObjAidsForIntensityDialog extends OcpCaveSWTDialog {

    private Storm storm;

    private ForecastIntensityDialog fcstIntensityDlg;

    private String selectedDateTimeGroup;

    private String[] availableDateTimeGroups;

    private java.util.List<String> selectedObjAids;

    // All obj aids tech from current master tech file (normally techlist.dat)
    private Map<String, ObjectiveAidTechEntry> allObjectiveAids = null;

    private java.util.List<String> displayedObjAids;

    // List for DTG and techniques.
    private List dtgList;

    private List techList;

    /**
     * Constructor
     * 
     * @param parent
     */
    public ObjAidsForIntensityDialog(Shell parent, Storm curStorm,
            ForecastIntensityDialog dlg) {

        super(parent);

        this.storm = curStorm;

        this.fcstIntensityDlg = dlg;

        setText("Obj Aids Time Intensity - " + storm.getStormName() + " "
                + storm.getStormId());

        // Get info from Forecast Intensity Dialog..
        allObjectiveAids = dlg.getObjAidsProperties().getAllObjectiveAids();
        displayedObjAids = new ArrayList<>();
        displayedObjAids.addAll(allObjectiveAids.keySet());

        selectedObjAids = dlg.getSelectedObjAids();

        // Get all A-Deck date/time groups (DTGs)
        java.util.List<String> dtgs = AtcfDataUtil.getDateTimeGroups(storm);

        availableDateTimeGroups = dtgs.toArray(new String[0]);

        if (availableDateTimeGroups.length > 0) {
            selectedDateTimeGroup = availableDateTimeGroups[availableDateTimeGroups.length
                    - 1];
        }

    }

    /**
     * Initialize the dialog components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createDataComp(shell);
        createControlButtons(shell);
    }

    /*
     * Create the data input section.
     * 
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, false);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginWidth = 10;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // Selection of storm DTG
        createDtgTechSelectionComp(mainComp);

    }

    /*
     * Create the composite to select DTG and technique.
     * 
     * @param parent
     */
    private void createDtgTechSelectionComp(Composite parent) {
        Composite dtgSelectComp = new Composite(parent, SWT.NONE);
        GridLayout dtgSelectLayout = new GridLayout(2, false);
        dtgSelectLayout.horizontalSpacing = 40;
        dtgSelectLayout.verticalSpacing = 10;
        dtgSelectLayout.marginWidth = 15;
        dtgSelectLayout.marginHeight = 10;
        dtgSelectComp.setLayout(dtgSelectLayout);
        dtgSelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // List to select a DTG
        Label dtgLabel = new Label(dtgSelectComp, SWT.NONE);
        dtgLabel.setText("Select a DTG ");
        GridData dtgLabelData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        dtgLabel.setLayoutData(dtgLabelData);

        Label techLabel = new Label(dtgSelectComp, SWT.NONE);
        techLabel.setText("Select Objective Aids");
        GridData techLabelData = new GridData(SWT.CENTER, SWT.CENTER, false,
                false);
        techLabel.setLayoutData(techLabelData);

        dtgList = new List(dtgSelectComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        dtgList.setToolTipText("Select DTGs");
        dtgList.setItems(availableDateTimeGroups);
        if (selectedDateTimeGroup != null) {
            dtgList.select(dtgList.indexOf(selectedDateTimeGroup));
        } else {
            dtgList.select(dtgList.getItemCount() - 1);
        }

        GridData dtgListGD = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgListGD.widthHint = 125;
        dtgListGD.heightHint = 750;
        dtgList.setLayoutData(dtgListGD);
        dtgList.setEnabled(false);

        dtgList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedDateTimeGroup = dtgList.getSelection()[0];
            }
        });

        dtgList.showSelection();

        // List to select techniques
        techList = new List(dtgSelectComp,
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        for (String aid : displayedObjAids) {
            String fullAid = aid;
            ObjectiveAidTechEntry tech = allObjectiveAids.get(aid);
            if (tech != null) {
                if (fullAid.length() < 4) {
                    fullAid += ("    " + tech.getDescription());
                } else {
                    fullAid += ("   " + tech.getDescription());
                }
            }

            techList.add(fullAid);
        }

        if (selectedObjAids != null) {
            for (String aid : selectedObjAids) {
                techList.select(displayedObjAids.indexOf(aid));
            }
        }

        GridData techListGD = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        techListGD.widthHint = 600;
        techListGD.heightHint = 750;
        techList.setLayoutData(techListGD);
        techList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        techList.showSelection();

        techList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedObjAids.clear();
                for (String aid : techList.getSelection()) {
                    selectedObjAids.add(AtcfVizUtil.aidCode(aid));
                }
            }
        });

    }

    /*
     * Create control buttons.
     * 
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnCompLayout = new GridLayout(2, true);
        ctrlBtnCompLayout.marginWidth = 40;
        ctrlBtnCompLayout.marginBottom = 15;
        ctrlBtnCompLayout.horizontalSpacing = 30;
        ctrlBtnComp.setLayout(ctrlBtnCompLayout);
        ctrlBtnComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button okButton = new Button(ctrlBtnComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        okButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                close();

                if (fcstIntensityDlg == null || fcstIntensityDlg.isDisposed()) {
                    return;
                }

                // Update info in Intensity Guide list
                fcstIntensityDlg.updateIntGuideList();

                // Update info ForecastInfo
                fcstIntensityDlg.prepareIntensityInfo();

                // Create an intensity 2-D dialog.
                IntensityGraphDialog intGraphDlg = createIntensityGraph();
                fcstIntensityDlg.setGraphDialog(intGraphDlg);
            }
        });

        Button cancelButton = new Button(ctrlBtnComp, SWT.PUSH);
        cancelButton.setText("Close");
        cancelButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /*
     * Create an intensity 2-D dialog.
     * 
     * @return IntensityGraphDialog
     */
    private IntensityGraphDialog createIntensityGraph() {

        // Build titles and axis labels.
        String dialogTitle = "ATCF Intensity Graph - " + storm.getStormName()
                + " " + storm.getStormId() + " at "
                + fcstIntensityDlg.getCurrentDTG().substring(4);

        String chartTitle = "Obj. Aids Intensity For " + storm.getStormId()
                + " at " + fcstIntensityDlg.getCurrentDTG().substring(4);

        String xAxisLabel = "Forecast Period";
        String yAxisLabel = "Intensity (kts)";

        return new IntensityGraphDialog(fcstIntensityDlg.getShell(),
                dialogTitle, chartTitle, xAxisLabel, yAxisLabel,
                fcstIntensityDlg, fcstIntensityDlg.getFcstInfo());
    }

    /**
     * @param selectedDateTimeGroup
     *            the selectedDateTimeGroup to set
     */
    public void setSelectedDateTimeGroup(String selectedDateTimeGroup) {
        this.selectedDateTimeGroup = selectedDateTimeGroup;
    }

}
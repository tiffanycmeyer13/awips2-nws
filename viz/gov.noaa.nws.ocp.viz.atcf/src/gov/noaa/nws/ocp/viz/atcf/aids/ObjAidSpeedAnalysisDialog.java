/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.Map;

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

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Aids"=>"Objective Aids Speed Analysis".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 23, 2019 59175      jwu         Initial creation.
 *
 * </pre>
 *
 * @author jwu
 * @version 1.0
 *
 */
public class ObjAidSpeedAnalysisDialog extends OcpCaveSWTDialog {

    private String[] availableDateTimeGroups;

    // All obj aids tech from current master tech file (normally techlist.dat)
    private Map<String, ObjectiveAidTechEntry> allObjectiveAids = null;

    // Default obj aids tech from default_aids.dat
    private String[] defaultObjectiveAids = null;

    // Current A-Deck records.
    private java.util.List<ADeckRecord> currentADeckRecords;

    /**
     * Selection Counter
     */
    private int profileSelectionCounter = 0;

    /**
     * Constructor
     *
     * @param parent
     */
    public ObjAidSpeedAnalysisDialog(Shell parent, Storm storm) {
        super(parent);

        setText("Compute Objective Aid Speeds - " + storm.getStormName() + " "
                + storm.getStormId());

        // TODO Retrieve A-Deck data for current storm.
        if (currentADeckRecords == null) {
            currentADeckRecords = new ArrayList<>();
        }

        currentADeckRecords.clear();

        // Get configurations.
        if (allObjectiveAids == null) {
            allObjectiveAids = AtcfConfigurationManager.getInstance()
                    .getObjectiveAidTechniques().getAvailableTechniques();
        }

        if (defaultObjectiveAids == null) {
            java.util.List<String> dflt = AtcfConfigurationManager.getInstance()
                    .getDefaultObjAidTechniques().getDefaultObjAidNames();
            defaultObjectiveAids = dflt.toArray(new String[dflt.size()]);
        }

        // Get all A-Deck date/time groups (DTGs)
        java.util.List<String> dtgs = AtcfDataUtil.getDateTimeGroups(storm);

        setAvailableDateTimeGroups(dtgs.toArray(new String[0]));
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

    /**
     * Create the data input section.
     *
     * @param parent
     */
    private void createDataComp(Composite parent) {

        // Main composite
        Composite mainComp = new Composite(parent, SWT.NONE);
        GridLayout dtgInfoLayout = new GridLayout(1, false);
        dtgInfoLayout.verticalSpacing = 10;
        dtgInfoLayout.marginHeight = 8;
        dtgInfoLayout.marginWidth = 20;
        mainComp.setLayout(dtgInfoLayout);
        mainComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // Selection of objective aid profile.
        createProfileSelectionComp(mainComp);

        // Selection of storm DTG
        createDtgTechSelectionComp(mainComp);

    }

    /**
     * Create the composite to select objective aid technique name.
     *
     * @param parent
     */
    private void createProfileSelectionComp(Composite parent) {

        Composite profileSelectComp = new Composite(parent, SWT.NONE);
        GridLayout profileSelectLayout = new GridLayout(1, false);
        profileSelectLayout.marginHeight = 5;
        profileSelectLayout.marginWidth = 15;
        profileSelectComp.setLayout(profileSelectLayout);
        profileSelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        // "Select profile:" Label, List.
        Label selectProfileLabel = new Label(profileSelectComp, SWT.None);
        GridData selectProfileGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        selectProfileLabel.setLayoutData(selectProfileGD);
        selectProfileLabel.setText("Select profile:");

        List selectProfileList = new List(profileSelectComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        selectProfileList.setToolTipText("Select profile");
        GridData selectProfileListGD = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        selectProfileListGD.widthHint = 200;
        selectProfileListGD.heightHint = 110;
        selectProfileList.setLayoutData(selectProfileListGD);
        String[] availableProfiles = AtcfConfigurationManager.getInstance()
                .getObjAidsProfileNames();
        selectProfileList.setItems(availableProfiles);

        selectProfileList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                profileSelectionCounter++;
            }
        });

    }

    /**
     * Create the composite to select DTG and technique.
     *
     * @param parent
     */
    private void createDtgTechSelectionComp(Composite parent) {
        Composite dtgSelectComp = new Composite(parent, SWT.BORDER);
        GridLayout dtgSelectLayout = new GridLayout(2, false);
        dtgSelectLayout.horizontalSpacing = 40;
        dtgSelectLayout.verticalSpacing = 10;
        dtgSelectLayout.marginWidth = 15;
        dtgSelectLayout.marginHeight = 10;
        dtgSelectComp.setLayout(dtgSelectLayout);
        dtgSelectComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Label dtgLabel = new Label(dtgSelectComp, SWT.NONE);
        dtgLabel.setText("Select DTG (s): ");
        GridData dtgLabelData = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgLabel.setLayoutData(dtgLabelData);

        Label techLabel = new Label(dtgSelectComp, SWT.NONE);
        techLabel.setText("Select Objective Aids:");
        GridData techLabelData = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        techLabel.setLayoutData(techLabelData);

        List dtgList = new List(dtgSelectComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        dtgList.setToolTipText("Select DTGs");
        dtgList.setItems(availableDateTimeGroups);
        dtgList.select(0);
        GridData dtgListGD = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        dtgListGD.widthHint = 125;
        dtgListGD.heightHint = 300;
        dtgList.setLayoutData(dtgListGD);

        dtgList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Not implemented.
            }
        });

        List techList = new List(dtgSelectComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        for (Map.Entry<String, ObjectiveAidTechEntry> entry : allObjectiveAids
                .entrySet()) {
            String fullAid = entry.getKey();
            ObjectiveAidTechEntry tech = entry.getValue();
            if (tech != null) {
                fullAid += ("   " + tech.getDescription());
            }

            techList.add(fullAid);
        }

        techList.select(0);
        GridData techListGD = new GridData(SWT.DEFAULT, SWT.CENTER, false,
                false);
        techListGD.widthHint = 350;
        techListGD.heightHint = 300;
        techList.setLayoutData(techListGD);

        techList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Not implemented.
            }
        });

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {

        Composite ctrlBtnComp = new Composite(parent, SWT.NONE);
        GridLayout ctrlBtnCompLayout = new GridLayout(4, true);
        ctrlBtnCompLayout.marginWidth = 40;
        ctrlBtnCompLayout.marginBottom = 15;
        ctrlBtnCompLayout.horizontalSpacing = 30;
        ctrlBtnComp.setLayout(ctrlBtnCompLayout);
        ctrlBtnComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(ctrlBtnComp, AtcfVizUtil.buttonGridData());

        Button computeButton = new Button(ctrlBtnComp, SWT.PUSH);
        computeButton.setText("Compute");
        computeButton.setLayoutData(AtcfVizUtil.buttonGridData());
        computeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Not implemented
            }
        });

        Button okButton = new Button(ctrlBtnComp, SWT.PUSH);
        okButton.setText("OK");
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO Perform actions with updated data.
                close();
            }
        });

        Button cancelButton = new Button(ctrlBtnComp, SWT.PUSH);
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
     * @param availableDateTimeGroups
     *            the availableDateTimeGroups to set
     */
    public void setAvailableDateTimeGroups(String[] availableDateTimeGroups) {
        this.availableDateTimeGroups = availableDateTimeGroups;
    }

}
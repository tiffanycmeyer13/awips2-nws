/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.DefaultObjAidTechniques;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.dataplugin.atcf.ADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.BaseADeckRecord;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for "Aids => List Objective Aids Data".
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date          Ticket#    Engineer     Description
 * ------------- ---------- ------------ -------------------------------------
 * Jan 05, 2021  85083      jwu          Initial creation
 * Mar 18, 2021  89201      mporricelli  Make aids list based on selected dtg
 * </pre>
 *
 * @author jwu
 * @version 1.0
 */

public class ListObjAidsDialog extends OcpCaveSWTDialog {

    // Current storm.
    private Storm storm;

    /*
     * Instance of obj aids display properties.
     */
    private ObjAidsProperties objAidsProperties = null;

    /*
     * Widgets.
     */
    private List profileList;

    private List techList;

    private StyledText dataInfoTxt;

    // Profile selection Counter
    private int profileSelectionCounter = 0;

    /**
     * Constructor
     * 
     * @param parent
     * @param storm
     *            Storm
     */
    public ListObjAidsDialog(Shell parent, Storm storm) {
        super(parent);
        this.storm = storm;

        setText("List Objective Aids for " + storm.getStormId() + " "
                + storm.getStormName());

        loadObjAidProperties();
    }

    /**
     * Initialize the dialog components.
     * 
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {

        // Add a scroll-able composite.
        shell.setLayout(new FillLayout());

        final ScrolledComposite scrollComposite = new ScrolledComposite(shell,
                SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);

        final Composite mainComp = new Composite(scrollComposite, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 0;
        mainComp.setLayout(mainLayout);

        // Add sub-composites within the scroll-able composite.
        createDataInfoComp(mainComp);
        createControlButtons(mainComp);

        scrollComposite.setContent(mainComp);
        scrollComposite.setExpandVertical(true);
        scrollComposite.setExpandHorizontal(true);
        scrollComposite
                .setMinSize(mainComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        // List default data
        listData();
    }

    /*
     * Loads configured properties for objective aids.
     */
    private void loadObjAidProperties() {

        objAidsProperties = new ObjAidsProperties();

        // Get configurations.
        if (objAidsProperties.getAllObjectiveAids() == null) {
            Map<String, ObjectiveAidTechEntry> allObjectiveAids = AtcfConfigurationManager
                    .getInstance().getObjectiveAidTechniques()
                    .getAvailableTechniques();
            objAidsProperties.setAllObjectiveAids(allObjectiveAids);
        }

        if (objAidsProperties.getDefaultObjectiveAids() == null) {
            java.util.List<String> dflt = AtcfConfigurationManager.getInstance()
                    .getDefaultObjAidTechniques().getDefaultObjAidNames();
            objAidsProperties.setDefaultObjectiveAids(
                    dflt.toArray(new String[dflt.size()]));
        }

        if (objAidsProperties.getAvailableProfiles() == null) {
            String[] availableProfiles = AtcfConfigurationManager.getInstance()
                    .getObjAidsProfileNames();

            objAidsProperties.setAvailableProfiles(availableProfiles);
        }

        /*
         * Find the date/time groups (DTGs) and objective aid techniques
         * appearing in retrieved records
         */
        java.util.List<String> allDTG = AtcfDataUtil.getDateTimeGroups(storm);

        objAidsProperties.setAvailableDateTimeGroups(
                allDTG.toArray(new String[allDTG.size()]));
        objAidsProperties.setSelectedDateTimeGroups(
                new String[] { allDTG.get(allDTG.size() - 1) });

    }

    /*
     * Create the component to select/list data.
     *
     * @param parent
     */
    private void createDataInfoComp(Composite parent) {
        Composite dataInfoComp = new Composite(parent, SWT.NONE);
        GridLayout dataInfoGL = new GridLayout(4, false);
        dataInfoGL.verticalSpacing = 10;
        dataInfoGL.horizontalSpacing = 15;
        dataInfoComp.setLayout(dataInfoGL);

        // Select obj. aids profile
        Composite profileComp = new Composite(dataInfoComp, SWT.NONE);
        GridLayout profileGL = new GridLayout(1, false);
        profileGL.verticalSpacing = 10;
        profileComp.setLayout(profileGL);
        profileComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));

        Label profileLbl = new Label(profileComp, SWT.NONE);
        profileLbl.setText("Select Profile:");
        profileLbl.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));

        profileList = new List(profileComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        profileList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        profileList.setLayoutData(new GridData(100, 260));
        profileList.setItems(objAidsProperties.getAvailableProfiles());

        profileList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                profileSelectionCounter++;

                if (profileSelectionCounter > 1) {
                    String selectedProfile = profileList.getSelection()[0];
                    objAidsProperties.setSelectedProfile(selectedProfile);

                    // Load the profile and update the selections.
                    DefaultObjAidTechniques aids = AtcfConfigurationManager
                            .getInstance()
                            .getSiteObjAidsProfile(selectedProfile);

                    techList.deselectAll();
                    for (String aid : aids.getDefaultObjAidNames()) {
                        techList.select(objAidsProperties
                                .getDisplayedObjectiveAids().indexOf(aid));
                    }

                    objAidsProperties.getSelectedObjectiveAids().clear();
                    for (String aid : techList.getSelection()) {
                        objAidsProperties.getSelectedObjectiveAids()
                                .add(AtcfVizUtil.aidCode(aid));
                    }

                } else {
                    profileList.deselectAll();
                    objAidsProperties.setSelectedProfile("");
                }
            }
        });

        String selectedProfile = objAidsProperties.getSelectedProfile();
        if (selectedProfile != null && !selectedProfile.isEmpty()) {
            profileList.select(profileList.indexOf(selectedProfile));
        }

        // Select obj. aids DTGs
        Composite dtgComp = new Composite(dataInfoComp, SWT.NONE);
        GridLayout dtgGL = new GridLayout(1, false);
        dtgGL.verticalSpacing = 10;
        dtgComp.setLayout(dtgGL);
        dtgComp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

        Label dtgLbl = new Label(dtgComp, SWT.NONE);
        dtgLbl.setText("Select DTGs:");
        dtgLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

        List dtgList = new List(dtgComp,
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        dtgList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        dtgList.setLayoutData(new GridData(100, 260));
        dtgList.setItems(objAidsProperties.getAvailableDateTimeGroups());
        String[] selectedDateTimeGroups = objAidsProperties
                .getSelectedDateTimeGroups();
        if (selectedDateTimeGroups == null
                || selectedDateTimeGroups.length == 0) {
            dtgList.select(dtgList.getItemCount() - 1);
            objAidsProperties.setSelectedDateTimeGroups(dtgList.getSelection());
        } else {
            for (String dtg : selectedDateTimeGroups) {
                dtgList.select(dtgList.indexOf(dtg));
            }
        }

        dtgList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties
                        .setSelectedDateTimeGroups(dtgList.getSelection());
                populateTechList();
            }
        });

        dtgList.showSelection();

        // Select obj. aids technique
        Composite techComp = new Composite(dataInfoComp, SWT.NONE);
        GridLayout techGL = new GridLayout(1, false);
        techComp.setLayout(techGL);

        Label techLbl = new Label(techComp, SWT.NONE);
        techLbl.setText("Select Obj Aids:");
        techLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        techList = new List(techComp,
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        techList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        techList.setLayoutData(new GridData(600, 230));
        populateTechList();

        techList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                profileList.deselectAll();
                objAidsProperties.getSelectedObjectiveAids().clear();
                for (String aid : techList.getSelection()) {
                    objAidsProperties.getSelectedObjectiveAids()
                            .add(AtcfVizUtil.aidCode(aid));
                }
            }
        });

        // Quick access to clear/select default/select all obj. aids technique
        Composite techOptComp = new Composite(techComp, SWT.NONE);
        GridLayout techOptGL = new GridLayout(3, true);
        techOptGL.marginWidth = 40;
        techOptGL.horizontalSpacing = 25;
        techOptComp.setLayout(techOptGL);
        techOptComp
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Button clearTechBtn = new Button(techOptComp, SWT.PUSH);
        clearTechBtn.setText("Clear");
        clearTechBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        clearTechBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                profileList.deselectAll();
                techList.deselectAll();
                objAidsProperties.getSelectedObjectiveAids().clear();

                objAidsProperties.setSelectedObjectiveAids(
                        objAidsProperties.getSelectedObjectiveAids());
            }
        });

        Button defTechBtn = new Button(techOptComp, SWT.PUSH);
        defTechBtn.setText("Select Defaults");
        defTechBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        defTechBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                profileList.deselectAll();
                techList.deselectAll();
                for (String aid : objAidsProperties.getDefaultObjectiveAids()) {
                    techList.select(objAidsProperties
                            .getDisplayedObjectiveAids().indexOf(aid));
                }

                objAidsProperties.getSelectedObjectiveAids().clear();
                for (String aid : techList.getSelection()) {
                    objAidsProperties.getSelectedObjectiveAids()
                            .add(AtcfVizUtil.aidCode(aid));
                }
            }
        });

        Button allTechBtn = new Button(techOptComp, SWT.PUSH);
        allTechBtn.setText("Select All");
        allTechBtn
                .setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        allTechBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                profileList.deselectAll();
                techList.selectAll();

                objAidsProperties.getSelectedObjectiveAids().clear();
                for (String aid : techList.getSelection()) {
                    objAidsProperties.getSelectedObjectiveAids()
                            .add(AtcfVizUtil.aidCode(aid));
                }
            }
        });

        // Select a forecast hour to list data up to it.
        Composite tauComp = new Composite(dataInfoComp, SWT.NONE);
        GridLayout tauGL = new GridLayout(1, false);
        tauComp.setLayout(tauGL);
        tauComp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

        Label tauLbl = new Label(tauComp, SWT.NONE);
        tauLbl.setText("TAU Limit:");
        tauLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

        List tauList = new List(tauComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        tauList.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
        tauList.setLayoutData(new GridData(60, 260));
        tauList.setItems(objAidsProperties.getAvailablePartialTimes());

        String selectedPartialTime = objAidsProperties.getSelectedPartialTime();
        if (selectedPartialTime == null || selectedPartialTime.isEmpty()) {
            tauList.select(0);
            objAidsProperties.setSelectedPartialTime(tauList.getSelection()[0]);
        } else {
            tauList.select(tauList.indexOf(selectedPartialTime));
        }

        tauList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties
                        .setSelectedPartialTime(tauList.getSelection()[0]);
            }
        });

        // StyledText to show current storm's obj. aids data.
        dataInfoTxt = new StyledText(dataInfoComp,
                SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData dataInfoGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        dataInfoGD.horizontalSpan = 4;
        dataInfoGD.verticalIndent = 2;
        dataInfoGD.horizontalIndent = 5;
        dataInfoGD.widthHint = 1100;
        dataInfoGD.heightHint = 600;

        dataInfoTxt.setLayoutData(dataInfoGD);
        dataInfoTxt.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

    }

    /*
     * Create control buttons.
     *
     * @param parent
     */
    private void createControlButtons(Composite parent) {
        Composite btnComp = new Composite(parent, SWT.NONE);
        GridLayout btnGL = new GridLayout(2, true);
        btnGL.marginWidth = 150;
        btnGL.horizontalSpacing = 100;
        btnComp.setLayout(btnGL);
        btnComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));

        Button applyBtn = new Button(btnComp, SWT.PUSH);
        applyBtn.setText("Apply");
        applyBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listData();
            }
        });

        Button closeBtn = new Button(btnComp, SWT.PUSH);
        closeBtn.setText("Close");
        closeBtn.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, true));
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        closeBtn.setFocus();
    }

    /*
     * Retrieve A-Deck Data for current storm.
     * 
     * @return java.util.List<BaseADeckRecord> Sorted data for selected DTGs
     */
    private java.util.List<BaseADeckRecord> retrieveData() {

        // Retrieve data for each DTG.
        String[] dtgs = objAidsProperties.getSelectedDateTimeGroups();
        Map<String, java.util.List<ADeckRecord>> recMap = AtcfDataUtil
                .retrieveADeckData(storm, dtgs, false);
        java.util.List<BaseADeckRecord> adeckData = new ArrayList<>();
        for (java.util.List<ADeckRecord> recs : recMap.values()) {
            adeckData.addAll(recs);
        }

        BaseADeckRecord.sortADeck(adeckData);

        return adeckData;
    }

    /*
     * List selected data.
     */
    private void listData() {

        // Get TAU limit.
        int tauLimit = 168;
        try {
            tauLimit = Integer
                    .parseInt(objAidsProperties.getSelectedPartialTime());
        } catch (NumberFormatException e) {
            tauLimit = 168;
        }

        // Retrieve data & list.
        java.util.List<BaseADeckRecord> adeckData = retrieveData();
        StringBuilder sbr = new StringBuilder();
        for (BaseADeckRecord rec : adeckData) {
            for (String aid : objAidsProperties.getSelectedObjectiveAids()) {
                if (aid.equalsIgnoreCase(rec.getTechnique())
                        && rec.getFcstHour() <= tauLimit) {
                    sbr.append(rec.toADeckString());
                    sbr.append("\n");
                }
            }
        }

        dataInfoTxt.setText(sbr.toString());
    }

    /**
     * Create or update aids list for this storm based on dtg selection
     */
    private void populateTechList() {

        objAidsProperties.getDisplayedObjectiveAids().clear();

        // Pluck active aids for selected dtg from list of all available aids
        for (String dtg : objAidsProperties.getSelectedDateTimeGroups()) {
            for (String aid : AtcfDataUtil.getObjAidTechniques(storm, dtg)) {
                objAidsProperties.getDisplayedObjectiveAids().add(aid);
            }
        }

        // Load the aids for Profile if selected
        DefaultObjAidTechniques aids;

        String selectedProfile = objAidsProperties.getSelectedProfile();

        if (selectedProfile != null && !selectedProfile.isEmpty()) {
            aids = AtcfConfigurationManager.getInstance()
                    .getSiteObjAidsProfile(selectedProfile);

            for (String aid : aids.getDefaultObjAidNames()) {
                objAidsProperties.getSelectedObjectiveAids().add(aid);
            }
        }
        // If no previously selected aids, get the defaults
        if (objAidsProperties.getSelectedObjectiveAids().isEmpty()) {
            for (String aid : objAidsProperties.getDefaultObjectiveAids()) {
                objAidsProperties.getSelectedObjectiveAids().add(aid);
            }
        }

        techList.removeAll();
        // Add all displayed aids with full descriptions to displayed list
        for (String aid : objAidsProperties.getDisplayedObjectiveAids()) {
            String fullAid = aid;
            ObjectiveAidTechEntry tech = objAidsProperties.getAllObjectiveAids()
                    .get(aid);
            if (tech != null) {
                fullAid += ("   " + tech.getDescription());
            }
            techList.add(fullAid);
        }

        techList.deselectAll();
        // Select and highlight the default or previously selected aids
        for (String aid : objAidsProperties.getSelectedObjectiveAids()) {
            techList.select(
                    objAidsProperties.getDisplayedObjectiveAids().indexOf(aid));
        }
    }
}

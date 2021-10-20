/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;
import com.raytheon.viz.ui.editor.AbstractEditor;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.DefaultObjAidTechniques;
import gov.noaa.nws.ocp.common.atcf.configuration.ObjectiveAidTechEntry;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.AtcfDataUtil;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;
import gov.noaa.nws.ocp.viz.atcf.main.ChooseStormDialog;
import gov.noaa.nws.ocp.viz.atcf.rsc.AtcfResource;
import gov.noaa.nws.ocp.viz.drawing.elements.Layer;

/**
 * Dialog to "Display Objective Aids..."
 *
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Sep 27, 2017             B. Hebbard  Initial Creation.
 * Jun 05, 2018 48178       jwu         Updated.
 * Jul 05, 2018 52119       jwu         Added listeners.
 * Aug 21, 2018 53949       jwu         Implement listeners.
 * Oct 19, 2018 55963       jwu         Implemented save/delete profile.
 * Mar 28, 2019 61882       jwu         Move adjustWindQuadrant() to AtfcVizUtil.
 * Apr 11, 2019 62487       jwu         Decouple GUI with draw options.
 * May 07, 2019 63005       jwu         Retrieve A-Deck from baseline only.
 * Apr 23, 2020 72252       jwu         Save full description of obj. aids.
 * Nov 10, 2020 84442       wpaintsil   Add Scrollbar.
 * Mar 04, 2021 88229       mporricelli Make aids list based on selected dtg.
 * Jun 24, 2021 91761       wpaintsil   Toggle bold selected aids correction
 *                                      OFCL, OFCP, OFCI, OFPI should always
 *                                      be listed at the top of the list of
 *                                      guidance available.
 * Jul 08, 2021 93998       jwu         Fix save/delete profile.
 * </pre>
 *
 * @author bhebbard
 * @version 1
 */
public class DisplayObjectiveAidsDialog extends CaveJFACEDialog {

    /**
     * Logger.
     */
    private static final IUFStatusHandler logger = UFStatus
            .getHandler(DisplayObjectiveAidsDialog.class);

    // Instance of "Choose Storm" dialog
    private static ChooseStormDialog chooseStormDialog;

    // Controls
    private List selectProfileList;

    private List dateTimeGroupList;

    private List objectiveAidsList;

    private List partialTimeDisplayList;

    private Button toggleBoldSelectedAidsCheckbox;

    private Button displayAidIntensitiesCheckbox;

    private Button gpceCheckbox;

    private Button gpceClimatologyCheckbox;

    private Button gpceAXCheckbox;

    private Button show34ktWindRadiiCheckbox;

    private Button show50ktWindRadiiCheckbox;

    private Button show64ktWindRadiiCheckbox;

    private Button boldLinesAllCheckbox;

    private Button colorsByIntensityCheckbox;

    private Button colorsBySaffirSimpsonScaleCheckbox;

    // Control buttons
    private static final int APPLY_ID = IDialogConstants.CLIENT_ID + 0;

    private static final String APPLY_LABEL = "Apply";

    private static final int OK_ID = IDialogConstants.CLIENT_ID + 1;

    private static final String OK_LABEL = "Ok";

    private static final int CLOSE_ID = IDialogConstants.CLIENT_ID + 2;

    private static final String CLOSE_LABEL = "Close";

    private static final java.util.List<String> FIXED_OBJ_AIDS = Arrays
            .asList("OFCL", "OFCP", "OFCI", "OFPI");

    /**
     * Selection Counter
     */
    private int profileSelectionCounter = 0;

    // Private instance of this dialog
    private static DisplayObjectiveAidsDialog instance = null;

    /*
     * Instance of obj aids display properties.
     */
    private ObjAidsProperties objAidsProperties = null;

    /*
     * Instance used to generator obj aids display.
     */
    private ObjAidsGenerator objAidsGenerator = null;

    /**
     * Get instance of this dialog
     *
     * @param parentShell
     * @return instance
     */
    public static synchronized DisplayObjectiveAidsDialog getInstance(
            Shell parentShell) {
        if (instance == null) {
            instance = new DisplayObjectiveAidsDialog(parentShell);
        }
        return instance;
    }

    /*
     * Private constructor
     *
     * @param parentShell
     */
    private DisplayObjectiveAidsDialog(Shell parentShell) {
        super(parentShell);

        setShellStyle(SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER
                | SWT.TITLE | SWT.RESIZE);

        objAidsProperties = new ObjAidsProperties();
        objAidsGenerator = new ObjAidsGenerator();

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
    }

    /*
     * Configure shell
     *
     * @param shell
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);

        String title = "Objective Aids ";

        Storm curStorm = AtcfSession.getInstance().getActiveStorm();
        if (curStorm != null) {
            title += " - ";
            if (curStorm.getStormName().length() > 0) {
                title += curStorm.getStormName();
                title += (" " + curStorm.getStormId().toLowerCase());
            } else {
                title += curStorm.getStormId().toLowerCase();
            }
        }

        shell.setText(title);

    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite dlgAreaForm = (Composite) super.createDialogArea(parent);

        FormToolkit toolkit = new FormToolkit(dlgAreaForm.getDisplay());
        ScrolledForm form = toolkit.createScrolledForm(dlgAreaForm);
        form.getBody().setLayout(new GridLayout());
        form.setBackground(dlgAreaForm.getBackground());
        form.setLayoutData(new GridData(GridData.FILL_BOTH));
        form.setExpandVertical(true);
        form.setExpandHorizontal(true);

        Composite topComp = form.getBody();

        GridLayout layout0 = new GridLayout(1, false);
        topComp.setLayout(layout0);

        Composite profileComp = new Composite(topComp, SWT.NONE);
        // "Select profile:" Label, List, and Buttons ("Save" and "Delete")
        GridLayout profileLayout = new GridLayout(2, false);
        profileComp.setLayout(profileLayout);
        Composite profileListComp = new Composite(profileComp, SWT.NONE);
        GridLayout profileListLayout = new GridLayout(1, false);
        profileListComp.setLayout(profileListLayout);

        Label selectProfileLabel = new Label(profileListComp, SWT.None);
        selectProfileLabel.setText("Select profile:");

        selectProfileList = new List(profileListComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        selectProfileList.setToolTipText("Select profile");
        selectProfileList.setItems(objAidsProperties.getAvailableProfiles());

        selectProfileList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                profileSelectionCounter++;

                if (profileSelectionCounter > 1) {
                    String selectedProfile = selectProfileList
                            .getSelection()[0];
                    objAidsProperties.setSelectedProfile(selectedProfile);

                    // Load the profile and update the selections.
                    DefaultObjAidTechniques aids = AtcfConfigurationManager
                            .getInstance()
                            .getSiteObjAidsProfile(selectedProfile);

                    objectiveAidsList.deselectAll();
                    for (String aid : aids.getDefaultObjAidNames()) {
                        objectiveAidsList.select(objAidsProperties
                                .getDisplayedObjectiveAids().indexOf(aid));
                    }

                    objAidsProperties.getSelectedObjectiveAids().clear();
                    for (String aid : objectiveAidsList.getSelection()) {
                        objAidsProperties.getSelectedObjectiveAids()
                                .add(AtcfVizUtil.aidCode(aid));
                    }

                } else {
                    selectProfileList.deselectAll();
                    objAidsProperties.setSelectedProfile("");
                }
            }
        });

        GridData profileListGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        Rectangle trim = selectProfileList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(selectProfileList) * 20,
                selectProfileList.getItemHeight() * 2);
        profileListGD.widthHint = trim.width;
        profileListGD.heightHint = trim.height;
        selectProfileList.setLayoutData(profileListGD);

        String selectedProfile = objAidsProperties.getSelectedProfile();
        if (selectedProfile != null && !selectedProfile.isEmpty()) {
            selectProfileList
                    .select(selectProfileList.indexOf(selectedProfile));
        }

        Composite profileButtonComp = new Composite(profileComp, SWT.NONE);
        GridLayout profileButtonLayout = new GridLayout(1, false);
        profileButtonComp.setLayout(profileButtonLayout);
        profileButtonComp
                .setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true));

        Button saveNewProfileButton = new Button(profileButtonComp, SWT.PUSH);
        saveNewProfileButton.setText("Save New Profile...");
        saveNewProfileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ManageProfileDialog profileDlg = new ManageProfileDialog(
                        getShell(), true, selectProfileList, objectiveAidsList);
                profileDlg.open();
            }
        });

        Button deleteProfileButton = new Button(profileButtonComp, SWT.PUSH);
        deleteProfileButton.setText("Delete Profile...   ");
        deleteProfileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ManageProfileDialog profileDlg = new ManageProfileDialog(
                        getShell(), false, selectProfileList,
                        objectiveAidsList);
                profileDlg.open();
            }
        });

        Composite centerListComp = new Composite(topComp, SWT.NONE);
        GridLayout centerListLayout = new GridLayout(3, false);
        centerListComp.setLayout(centerListLayout);
        centerListComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        // Date Time Group
        Composite dtgListComp = new Composite(centerListComp, SWT.NONE);
        GridLayout dtgListLayout = new GridLayout(1, false);
        dtgListComp.setLayout(dtgListLayout);
        dtgListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.FILL, false, false));

        Label selectDTGsLabel = new Label(dtgListComp, SWT.None);
        selectDTGsLabel.setText("\nSelect DTG(s):");

        dateTimeGroupList = new List(dtgListComp,
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        dateTimeGroupList
                .setItems(objAidsProperties.getAvailableDateTimeGroups());
        String[] selectedDateTimeGroups = objAidsProperties
                .getSelectedDateTimeGroups();
        if (selectedDateTimeGroups == null
                || selectedDateTimeGroups.length == 0) {
            dateTimeGroupList.select(dateTimeGroupList.getItemCount() - 1);
            objAidsProperties.setSelectedDateTimeGroups(
                    dateTimeGroupList.getSelection());
        } else {
            for (String dtg : selectedDateTimeGroups) {
                dateTimeGroupList.select(dateTimeGroupList.indexOf(dtg));
            }
        }

        dateTimeGroupList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setSelectedDateTimeGroups(
                        dateTimeGroupList.getSelection());
                populateAidsList();
                retrieveData();
            }
        });

        dateTimeGroupList.showSelection();

        GridData dtgListGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        trim = dateTimeGroupList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(dateTimeGroupList) * 15,
                dateTimeGroupList.getItemHeight() * 4);
        dtgListGD.widthHint = trim.width;
        dtgListGD.heightHint = trim.height;
        dateTimeGroupList.setLayoutData(dtgListGD);

        Group dtgOffsetsGroup = new Group(dtgListComp, SWT.ALL);
        dtgOffsetsGroup.setText("DTG vs. Latest");

        GridLayout dtgOffsetsLayout = new GridLayout();
        dtgOffsetsLayout.numColumns = 2;
        dtgOffsetsLayout.makeColumnsEqualWidth = true;
        dtgOffsetsLayout.marginWidth = 2;
        dtgOffsetsLayout.marginHeight = 2;
        dtgOffsetsLayout.horizontalSpacing = 2;
        dtgOffsetsLayout.verticalSpacing = 4;
        dtgOffsetsGroup.setLayout(dtgOffsetsLayout);

        // "Minus 48 Hours" Button
        Button minus48DTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        minus48DTGButton.setText("-48");
        minus48DTGButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        minus48DTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(48);
            }
        });

        // "Minus 36 Hours" Button
        Button minus36DTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        minus36DTGButton.setText("-36");
        minus36DTGButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        minus36DTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(36);
            }
        });

        // "Minus 24 Hours" Button
        Button minus24DTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        minus24DTGButton.setText("-24");
        minus24DTGButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        minus24DTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(24);
            }
        });

        // "Minus 18 Hours" Button
        Button minus18DTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        minus18DTGButton.setText("-18");
        minus18DTGButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        minus18DTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(18);
            }
        });

        // "Minus 12 Hours" Button
        Button minus12DTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        minus12DTGButton.setText("-12");
        minus12DTGButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        minus12DTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(12);
            }
        });

        // "Minus 06 Hours" Button
        Button minus06DTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        minus06DTGButton.setText("-06");
        minus06DTGButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        minus06DTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(6);
            }
        });

        // "Latest DTG" Button
        Button latestDTGButton = new Button(dtgOffsetsGroup, SWT.PUSH);
        latestDTGButton.setText("Latest DTG");
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.CENTER;
        gd.horizontalSpan = 2;
        latestDTGButton.setLayoutData(gd);
        latestDTGButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                drawRelativeDTG(0);
            }
        });

        /*
         * "Select Objective Aids" Label, List, and Buttons ("Clear", "Select",
         * Defaults", and "Select All").
         */

        Composite aidsListComp = new Composite(centerListComp, SWT.NONE);
        GridLayout aidsListLayout = new GridLayout(1, false);
        aidsListComp.setLayout(aidsListLayout);
        aidsListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.FILL, false, false));

        Label selectObjectiveAidsLabel = new Label(aidsListComp, SWT.None);
        selectObjectiveAidsLabel.setText("\nSelect Objective Aids:");

        objectiveAidsList = new List(aidsListComp,
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.NONE);
        objectiveAidsList.setToolTipText(
                "Select objective aids (techniques) to display");
        objectiveAidsList
                .setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

        populateAidsList();

        objectiveAidsList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.getSelectedObjectiveAids().clear();
                for (String aid : objectiveAidsList.getSelection()) {
                    objAidsProperties.getSelectedObjectiveAids()
                            .add(AtcfVizUtil.aidCode(aid));
                }
            }
        });

        GridData aidsListGD = new GridData(SWT.FILL, SWT.FILL, true, true);
        trim = objectiveAidsList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(objectiveAidsList) * 40,
                objectiveAidsList.getItemHeight() * 10);
        aidsListGD.widthHint = trim.width;
        aidsListGD.heightHint = trim.height;
        objectiveAidsList.setLayoutData(aidsListGD);

        Composite clearSelectComposite = new Composite(aidsListComp, SWT.ALL);

        GridLayout clearSelectLayout = new GridLayout();
        clearSelectLayout.numColumns = 3;
        clearSelectLayout.makeColumnsEqualWidth = false;
        clearSelectLayout.marginWidth = 2;
        clearSelectLayout.marginHeight = 2;
        clearSelectLayout.horizontalSpacing = 2;
        clearSelectLayout.verticalSpacing = 4;
        clearSelectComposite.setLayout(clearSelectLayout);
        clearSelectComposite.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        Button objectiveAidsClearButton = new Button(clearSelectComposite,
                SWT.PUSH);
        objectiveAidsClearButton.setText("Clear");
        objectiveAidsClearButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        objectiveAidsClearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objectiveAidsList.deselectAll();
                objAidsProperties.getSelectedObjectiveAids().clear();

                objAidsProperties.setSelectedObjectiveAids(
                        objAidsProperties.getSelectedObjectiveAids());
            }
        });

        Button objectiveAidsSelectDefaultsButton = new Button(
                clearSelectComposite, SWT.PUSH);
        objectiveAidsSelectDefaultsButton.setText("Select Defaults");
        objectiveAidsSelectDefaultsButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        objectiveAidsSelectDefaultsButton
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {

                        objectiveAidsList.deselectAll();
                        for (String aid : objAidsProperties
                                .getDefaultObjectiveAids()) {
                            objectiveAidsList.select(objAidsProperties
                                    .getDisplayedObjectiveAids().indexOf(aid));
                        }

                        objAidsProperties.getSelectedObjectiveAids().clear();
                        for (String aid : objectiveAidsList.getSelection()) {
                            objAidsProperties.getSelectedObjectiveAids()
                                    .add(AtcfVizUtil.aidCode(aid));
                        }
                    }
                });

        Button objectiveAidsSelectAllButton = new Button(clearSelectComposite,
                SWT.PUSH);
        objectiveAidsSelectAllButton.setText("Select All");
        objectiveAidsSelectAllButton.setLayoutData(
                new GridData(GridData.CENTER, GridData.CENTER, true, false));
        objectiveAidsSelectAllButton
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        objectiveAidsList.selectAll();

                        objAidsProperties.getSelectedObjectiveAids().clear();
                        for (String aid : objectiveAidsList.getSelection()) {
                            objAidsProperties.getSelectedObjectiveAids()
                                    .add(AtcfVizUtil.aidCode(aid));
                        }
                    }
                });

        Composite partialListComp = new Composite(centerListComp, SWT.NONE);
        GridLayout partialListLayout = new GridLayout(1, false);
        partialListComp.setLayout(partialListLayout);
        partialListComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.FILL, false, false));

        // Partial Time Aid Display
        Label displayToTauLabel = new Label(partialListComp, SWT.None);
        displayToTauLabel.setText("Partial Aid Display\nDisplay to TAU:");

        partialTimeDisplayList = new List(partialListComp,
                SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        partialTimeDisplayList
                .setItems(objAidsProperties.getAvailablePartialTimes());
        String selectedPartialTime = objAidsProperties.getSelectedPartialTime();
        if (selectedPartialTime == null || selectedPartialTime.isEmpty()) {
            partialTimeDisplayList.select(0);
            objAidsProperties.setSelectedPartialTime(
                    partialTimeDisplayList.getSelection()[0]);
        } else {
            partialTimeDisplayList.select(
                    partialTimeDisplayList.indexOf(selectedPartialTime));
        }

        partialTimeDisplayList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setSelectedPartialTime(
                        partialTimeDisplayList.getSelection()[0]);
            }
        });

        GridData partialListGD = new GridData(SWT.CENTER, SWT.FILL, true, true);
        trim = partialTimeDisplayList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(partialTimeDisplayList) * 3,
                partialTimeDisplayList.getItemHeight() * 10);
        partialListGD.widthHint = trim.width;
        partialListGD.heightHint = trim.height;
        partialTimeDisplayList.setLayoutData(partialListGD);

        Composite displayOptionsComp = new Composite(topComp, SWT.NONE);
        GridLayout displayOptionsLayout = new GridLayout(2, false);
        displayOptionsComp.setLayout(displayOptionsLayout);
        displayOptionsComp.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, false, false));

        // Display Options Group and individual Checkboxes
        Group displayOptionsGroup = new Group(displayOptionsComp, SWT.ALL);
        displayOptionsGroup.setText("Display Options");

        RowLayout displayOptionsGrpLayout = new RowLayout();
        displayOptionsGrpLayout.type = SWT.VERTICAL;
        displayOptionsGrpLayout.marginWidth = 10;
        displayOptionsGrpLayout.marginHeight = 10;
        displayOptionsGrpLayout.spacing = 1;
        displayOptionsGroup.setLayout(displayOptionsGrpLayout);

        // "Toggle Bold Selected Aids" Option
        toggleBoldSelectedAidsCheckbox = new Button(displayOptionsGroup,
                SWT.CHECK);
        toggleBoldSelectedAidsCheckbox.setText("Toggle Bold Selected Aids");
        toggleBoldSelectedAidsCheckbox
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        objAidsProperties.setToggleBoldSelectedAids(
                                toggleBoldSelectedAidsCheckbox.getSelection());
                        objAidsGenerator.draw();
                    }
                });
        toggleBoldSelectedAidsCheckbox
                .setSelection(objAidsProperties.isToggleBoldSelectedAids());

        // "Display Aid Intensities" Option
        displayAidIntensitiesCheckbox = new Button(displayOptionsGroup,
                SWT.CHECK);
        displayAidIntensitiesCheckbox.setText("Display Aid Intensities");
        displayAidIntensitiesCheckbox
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        objAidsProperties.setDisplayAidIntensities(
                                displayAidIntensitiesCheckbox.getSelection());
                        objAidsGenerator.draw();
                    }
                });

        displayAidIntensitiesCheckbox
                .setSelection(objAidsProperties.isDisplayAidIntensities());

        // "GPCE" Option
        gpceCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        gpceCheckbox.setText("GPCE");
        gpceCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setGpce(gpceCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });

        gpceCheckbox.setSelection(objAidsProperties.isGpce());

        // "GPCE Climatology" Option
        gpceClimatologyCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        gpceClimatologyCheckbox.setText("GPCE Climatology");
        gpceClimatologyCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setGpceClimatology(
                        gpceClimatologyCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });

        gpceClimatologyCheckbox
                .setSelection(objAidsProperties.isGpceClimatology());

        // "GPCE-AX" Option
        gpceAXCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        gpceAXCheckbox.setText("GPCE-AX");
        gpceAXCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setGpceAX(gpceAXCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });
        gpceAXCheckbox.setSelection(objAidsProperties.isGpceAX());

        // "34 kt aid wind radii" Option
        show34ktWindRadiiCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        show34ktWindRadiiCheckbox.setText("34 kt aid wind radii");
        show34ktWindRadiiCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setShow34ktWindRadii(
                        show34ktWindRadiiCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });

        objAidsProperties
                .setShow34ktWindRadii(objAidsProperties.isShow34ktWindRadii());

        // "50 kt aid wind radii" Option
        show50ktWindRadiiCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        show50ktWindRadiiCheckbox.setText("50 kt aid wind radii");
        show50ktWindRadiiCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setShow50ktWindRadii(
                        show50ktWindRadiiCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });

        show50ktWindRadiiCheckbox
                .setSelection(objAidsProperties.isShow50ktWindRadii());

        // "64 kt aid wind radii" Option
        show64ktWindRadiiCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        show64ktWindRadiiCheckbox.setText("64 kt aid wind radii");
        show64ktWindRadiiCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setShow64ktWindRadii(
                        show64ktWindRadiiCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });
        show64ktWindRadiiCheckbox
                .setSelection(objAidsProperties.isShow64ktWindRadii());

        // "Bold Lines (all aids)" Option
        boldLinesAllCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        boldLinesAllCheckbox.setText("Bold Lines (all aids)");
        boldLinesAllCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties
                        .setBoldLinesAll(boldLinesAllCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });

        boldLinesAllCheckbox.setSelection(objAidsProperties.isBoldLinesAll());

        // "Colors by intensity (TD, TS, and TY/HU)" Option
        colorsByIntensityCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        colorsByIntensityCheckbox
                .setText("Colors by intensity (TD, TS, and TY/HU)");
        colorsByIntensityCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                objAidsProperties.setColorsByIntensity(
                        colorsByIntensityCheckbox.getSelection());
                objAidsGenerator.draw();
            }
        });
        colorsByIntensityCheckbox
                .setSelection(objAidsProperties.isColorsByIntensity());

        // "Colors by Saffir-Simpson scale" Option
        colorsBySaffirSimpsonScaleCheckbox = new Button(displayOptionsGroup,
                SWT.CHECK);
        colorsBySaffirSimpsonScaleCheckbox
                .setText("Colors by Saffir-Simpson scale");
        colorsBySaffirSimpsonScaleCheckbox
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        objAidsProperties.setColorsBySaffirSimpsonScale(
                                colorsBySaffirSimpsonScaleCheckbox
                                        .getSelection());
                        objAidsGenerator.draw();
                    }
                });
        colorsBySaffirSimpsonScaleCheckbox
                .setSelection(objAidsProperties.isColorsBySaffirSimpsonScale()); // new

        Composite displayButtonsComp = new Composite(displayOptionsComp,
                SWT.NONE);
        GridLayout displayButtonLayout = new GridLayout(1, false);
        displayButtonsComp.setLayout(displayButtonLayout);
        displayButtonsComp
                .setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true));

        // "Clear Aids Display" Button
        Button clearAidsDisplayButton = new Button(displayButtonsComp,
                SWT.PUSH);

        clearAidsDisplayButton.setText("Clear Aids Display");
        clearAidsDisplayButton.setToolTipText(
                "Select a different storm for which to display objective aids");

        clearAidsDisplayButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // go to clear display...
                clearAidsDisplay();
            }
        });

        // "Different Storm..." Button
        Button differentStormButton = new Button(displayButtonsComp, SWT.PUSH);
        differentStormButton.setText("Different Storm...");
        differentStormButton.setToolTipText(
                "Select a different storm for which to display objective aids");
        differentStormButton.setLayoutData(
                new GridData(SWT.FILL, SWT.DEFAULT, true, false));

        differentStormButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (chooseStormDialog == null
                        || chooseStormDialog.getShell().isDisposed()) {
                    createChooseStormDialog();
                }
                chooseStormDialog.open();

            }
        });

        // "PrintNSave" Button
        Button printNSaveButton = new Button(displayButtonsComp, SWT.PUSH);
        printNSaveButton.setText("PrintNSave");
        printNSaveButton.setToolTipText("Not yet available");

        printNSaveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // go to storm selection...
            }

        });
        printNSaveButton.setEnabled(false);
        printNSaveButton.setLayoutData(
                new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 3));

        form.reflow(true);

        return topComp;
    }

    /*
     * Create a Choose Storm dialog
     */
    private static synchronized void createChooseStormDialog() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        chooseStormDialog = new ChooseStormDialog(shell);
        chooseStormDialog.addCloseCallback(ov -> chooseStormDialog = null);
    }

    /**
     * Create Apply/Ok/Cancel buttons
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, APPLY_ID, APPLY_LABEL, false);
        createButton(parent, OK_ID, OK_LABEL, false);
        createButton(parent, CLOSE_ID, CLOSE_LABEL, true);

    }

    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case APPLY_ID:
            objAidsGenerator.draw();
            break;
        case OK_ID:
            objAidsGenerator.draw();
            close();
            break;
        case CLOSE_ID:
            // Find the current ATCF resource.
            AtcfResource drawingLayer = AtcfSession.getInstance()
                    .getAtcfResource();
            AbstractEditor editor = AtcfVizUtil.getActiveEditor();

            drawingLayer.getResourceData().getActiveAtcfProduct()
                    .getGhostLayer().removeAllElements();

            if (editor instanceof AbstractEditor) {
                editor.refresh();
            }

            profileSelectionCounter = 0;

            close();
            break;

        default:
            // do nothing
            break;
        }
    }

    /*
     * Clear all A-Deck elements
     */
    private void clearAidsDisplay() {

        // Get the current AtcfResource.
        AtcfResource drawingLayer = AtcfSession.getInstance().getAtcfResource();

        // Clear elements on A-Deck layer & ghost layer.
        Layer aLayer = drawingLayer.getResourceData().getActiveAtcfProduct()
                .getADeckLayer();
        Layer gLayer = drawingLayer.getResourceData().getActiveAtcfProduct()
                .getGhostLayer();

        aLayer.removeAllElements();
        gLayer.removeAllElements();

        // Refresh to display elements.
        AbstractEditor editor = AtcfVizUtil.getActiveEditor();
        if (editor instanceof AbstractEditor) {
            editor.refresh();
        }

    }

    /*
     * Draws the DTG "hours" before the latest.
     *
     * @param hours Number of hours before the latest hours.
     */
    private void drawRelativeDTG(int hours) {

        dateTimeGroupList.deselectAll();

        String latestDTG = dateTimeGroupList
                .getItem(dateTimeGroupList.getItemCount() - 1);

        String newDtg = AtcfDataUtil.getNewDTG(latestDTG, -hours);

        int index = dateTimeGroupList.indexOf(newDtg);

        if (index >= 0) {
            dateTimeGroupList.select(index);
            String[] selectedDateTimeGroups = dateTimeGroupList.getSelection();
            objAidsProperties.setSelectedDateTimeGroups(selectedDateTimeGroups);
            populateAidsList();
            retrieveData();
            objAidsGenerator.draw();
        } else {
            logger.warn("No DTG found for " + newDtg);
        }
    }

    /**
     * Create or update aids list for this storm based on dtg selection
     */
    private void populateAidsList() {
        objAidsProperties.getDisplayedObjectiveAids().clear();
        Storm curStorm = AtcfSession.getInstance().getActiveStorm();

        /*
         * First, OFCL, OFCP, OFCI, OFPI should always be listed at the top of
         * the list of guidance available.
         */
        for (String aid : FIXED_OBJ_AIDS) {
            ObjectiveAidTechEntry tech = objAidsProperties.getAllObjectiveAids()
                    .get(aid);
            if (tech != null) {
                objAidsProperties.getDisplayedObjectiveAids().add(aid);
            }
        }

        /*
         * Pluck active aids for selected dtg from list of all available aids.
         */
        for (String dtg : objAidsProperties.getSelectedDateTimeGroups()) {
            for (String aid : AtcfDataUtil.getObjAidTechniques(curStorm, dtg)) {
                if (!FIXED_OBJ_AIDS.contains(aid) && objAidsProperties
                        .getActiveObjectiveAids().contains(aid)) {
                    objAidsProperties.getDisplayedObjectiveAids().add(aid);
                }
            }
        }

        // Second, if no previously selected aids, select the defaults
        if (objAidsProperties.getSelectedObjectiveAids().isEmpty()) {
            for (String aid : objAidsProperties.getDefaultObjectiveAids()) {
                objAidsProperties.getSelectedObjectiveAids().add(aid);
            }
        }

        // Add full description for displayed aids
        objectiveAidsList.removeAll();

        for (String aid : objAidsProperties.getDisplayedObjectiveAids()) {
            ObjectiveAidTechEntry tech = objAidsProperties.getAllObjectiveAids()
                    .get(aid);
            if (tech != null) {
                objectiveAidsList.add(aid + "   " + tech.getDescription());
            }
        }

        // Load the profile and related aids if selected
        DefaultObjAidTechniques aids;
        objectiveAidsList.deselectAll();
        String selectedProfile = objAidsProperties.getSelectedProfile();

        if (selectedProfile != null && !selectedProfile.isEmpty()) {
            aids = AtcfConfigurationManager.getInstance()
                    .getSiteObjAidsProfile(selectedProfile);

            for (String aid : aids.getDefaultObjAidNames()) {
                objectiveAidsList.select(objAidsProperties
                        .getDisplayedObjectiveAids().indexOf(aid));
            }
        }

        // Select and highlight the default or previously selected aids
        for (String aid : objAidsProperties.getSelectedObjectiveAids()) {
            objectiveAidsList.select(
                    objAidsProperties.getDisplayedObjectiveAids().indexOf(aid));
        }

    }

    /**
     * Retrieve A-Deck Data for current storm.
     */
    public void retrieveData() {

        // Get the current storm.
        Storm curStorm = AtcfSession.getInstance().getActiveStorm();

        /*
         * Retrieve data for each DTG and store into AtcfProduct.
         */
        String[] dtgs = objAidsProperties.getSelectedDateTimeGroups();
        AtcfDataUtil.retrieveADeckData(curStorm, dtgs, false);
    }

    /**
     * @return the objAidsProperties
     */
    public ObjAidsProperties getObjAidsProperties() {
        return objAidsProperties;
    }

    /**
     * @param objAidsProperties
     *            the objAidsProperties to set
     */
    public void setObjAidsProperties(ObjAidsProperties objAidsProperties) {
        this.objAidsProperties = objAidsProperties;
    }

    /**
     * @return the objAidsGenerator
     */
    public ObjAidsGenerator getObjAidsGenerator() {
        return objAidsGenerator;
    }

    /**
     * @param objAidsGenerator
     *            the objAidsGenerator to set
     */
    public void setObjAidsGenerator(ObjAidsGenerator objAidsGenerator) {
        this.objAidsGenerator = objAidsGenerator;
    }

}
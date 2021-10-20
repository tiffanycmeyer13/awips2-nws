/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.aids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.DefaultObjAidTechEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.DefaultObjAidTechniques;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;

/**
 *
 * Private dialog to save/delete obj. aids profiles within "Display Obj. Aids"
 * dialog.
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Oct 19, 2018 55963       jwu         Initial creation.
 * </pre>
 *
 * @author jwu
 *
 * @version 1.0
 *
 */
public class ManageProfileDialog extends CaveSWTDialog {
    /**
     * Widget for the list of profiles
     */
    private List profileList;

    /**
     * Widget to enter profile name.
     */
    org.eclipse.swt.widgets.Text nameTxt;

    /**
     * OK button
     */
    private Button saveButton;

    private int profileSelectionCounter = 0;

    private boolean saveProfile;

    private List selectProfileList;

    private List objectiveAidsList;

    /**
     * Constructor
     *
     * @param parentShell
     * @param save
     *            true - save profile; false - delete profile
     */
    public ManageProfileDialog(Shell parentShell, boolean save,
            List selectProfileList, List objectiveAidsList) {
        super(parentShell, SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);

        this.selectProfileList = selectProfileList;
        this.objectiveAidsList = objectiveAidsList;

        saveProfile = save;
        if (save) {
            setText("Save Profile");
        } else {
            setText("Delete Profile");
        }
    }

    /**
     * Initialize the dialog components.
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createProfileList(shell);
        createControlButtons(shell);
    }

    /**
     * Create list of profiles.
     *
     * @param parent
     */
    private void createProfileList(Composite parent) {

        Composite profileListComp = new Composite(parent, SWT.NONE);

        GridLayout listCompLayout = new GridLayout(1, false);
        listCompLayout.marginWidth = 15;
        profileListComp.setLayout(listCompLayout);
        GridData profileListGD = new GridData(SWT.CENTER, SWT.DEFAULT, false,
                false);
        profileListComp.setLayoutData(profileListGD);

        Label chooseLabel = new Label(profileListComp, SWT.NONE);

        profileList = new List(profileListComp,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

        /*
         * For save, all existing profiles can be re-used so the user can modify
         * existing ones. For delete, only non-base level profiles should be
         * listed - BASE level profiles will alwasy stay.
         */
        if (saveProfile) {
            chooseLabel.setText("Select a profile or enter a new name below:");
            profileList.setItems(selectProfileList.getItems());
        } else {
            chooseLabel.setText("Select a profile below to delete:");
            String[] deleteableProfiles = AtcfConfigurationManager.getInstance()
                    .getNonBaseObjAidsProfileNames();
            profileList.setItems(deleteableProfiles);
        }

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Rectangle trim = profileList.computeTrim(0, 0,
                AtcfVizUtil.getCharWidth(profileList) * 3,
                profileList.getItemHeight() * 6);
        gd.widthHint = trim.width;
        gd.heightHint = trim.height;
        profileList.setLayoutData(gd);

        profileList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                /*
                 * Enable the save button if an item is selected and add it to
                 * the textbox for possible editing.
                 */
                if (profileList.getSelectionCount() > 0 && saveButton != null) {
                    profileSelectionCounter++;

                    if (profileSelectionCounter > 1) {
                        saveButton.setEnabled(true);
                        if (saveProfile) {
                            nameTxt.setText(profileList.getSelection()[0]);
                        }
                    } else {
                        // Clear the textbox if nothing is selected
                        profileList.deselectAll();
                        saveButton.setEnabled(false);
                        if (saveProfile) {
                            nameTxt.setText("");
                        }
                    }
                }
            }
        });

        if (saveProfile) {
            Composite profileNameComp = new Composite(profileListComp,
                    SWT.NONE);
            GridLayout nameCompLayout = new GridLayout(1, false);
            nameCompLayout.marginTop = 20;
            nameCompLayout.marginWidth = 0;

            profileNameComp.setLayout(nameCompLayout);
            Label nameLabel = new Label(profileNameComp, SWT.NONE);
            nameLabel.setText("Profile Name:");
            nameTxt = new org.eclipse.swt.widgets.Text(profileNameComp,
                    SWT.BORDER);
            GridData nameTxtGD = new GridData(SWT.LEFT, SWT.DEFAULT, false,
                    false);
            nameTxtGD.widthHint = 260;
            nameTxt.setLayoutData(nameTxtGD);
            nameTxt.setText("");
            nameTxt.addModifyListener(e -> saveButton
                    .setEnabled(nameTxt.getText().trim().length() > 0));

        }

    }

    /**
     * Create control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {

        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout buttonLayout = new GridLayout(3, true);
        buttonLayout.marginWidth = 15;
        buttonLayout.marginHeight = 10;

        buttonComp.setLayout(buttonLayout);
        buttonComp.setLayoutData(AtcfVizUtil.horizontalFillGridData());

        AtcfVizUtil.createHelpButton(buttonComp, AtcfVizUtil.buttonGridData());

        saveButton = new Button(buttonComp, SWT.PUSH);
        if (saveProfile) {
            saveButton.setText("Save");
        } else {
            saveButton.setText("Delete");
        }

        saveButton.setLayoutData(AtcfVizUtil.buttonGridData());
        saveButton.setEnabled(
                profileList != null && profileList.getSelectionCount() > 0);
        saveButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (saveProfile) {
                    saveNewProfile(nameTxt.getText());
                } else {
                    String sel = profileList.getSelection()[0];
                    boolean deleted = deleteProfile(sel);
                    if (deleted) {
                        profileList.remove(sel);
                    }
                }

                close();
            }
        });
        saveButton.setEnabled(false);

        Button cancelButton = new Button(buttonComp, SWT.PUSH);
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
     * Save the selected obj aids techs into a profile.
     *
     * @param profile
     *            name of the profile
     *
     */
    private void saveNewProfile(String profile) {
        if (profile != null && !profile.isEmpty()) {
            DefaultObjAidTechniques aids = new DefaultObjAidTechniques();
            for (String entry : objectiveAidsList.getSelection()) {
                String[] items = entry.split("\\s+", 2);
                aids.getDefaultObjAidTechniques()
                        .add(new DefaultObjAidTechEntry(items[0], items[1]));
            }

            boolean saved = AtcfConfigurationManager.getInstance()
                    .saveObjAidsProfile(profile, aids);

            // Add this to the profile list and set as the selected profile.
            if (saved) {
                String[] profiles = selectProfileList.getItems();
                java.util.List<String> profList = new ArrayList<>(
                        Arrays.asList(profiles));
                if (!profList.contains(profile)) {
                    profList.add(profile);
                    Collections.sort(profList);
                    selectProfileList.removeAll();
                    selectProfileList.setItems(
                            profList.toArray(new String[profList.size()]));
                }
            }

            selectProfileList.select(selectProfileList.indexOf(profile));
        }
    }

    /**
     * Delete the selected obj aids profile.
     *
     * @param profile
     *            name of the profile
     */
    private boolean deleteProfile(String profile) {
        boolean deleted = false;
        if (profile != null) {
            deleted = AtcfConfigurationManager.getInstance()
                    .deleteObjAidsProfile(profile);

            if (deleted) {
                selectProfileList.remove(profile);
                selectProfileList.deselectAll();
            }
        }

        return deleted;
    }

}
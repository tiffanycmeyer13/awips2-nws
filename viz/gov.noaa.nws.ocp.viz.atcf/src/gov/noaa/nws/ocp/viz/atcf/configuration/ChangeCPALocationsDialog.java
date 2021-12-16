/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.configuration;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.atcf.configuration.CpaLocationEntry;
import gov.noaa.nws.ocp.common.atcf.configuration.CpaLocations;
import gov.noaa.nws.ocp.viz.atcf.AtcfVizUtil;
import gov.noaa.nws.ocp.viz.ui.dialogs.OcpCaveSWTDialog;

/**
 * Dialog for Change CPA Locations
 *
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 15, 2019 #59176      dmanzella   initial creation
 *
 * </pre>
 *
 * @author dmanzella
 * @version 1.0
 *
 */
public class ChangeCPALocationsDialog extends OcpCaveSWTDialog {

    /**
     * Pulls the CPA Locations from localization
     */
    private CpaLocations cpaLocations;

    private List cpaLocationEntryList;

    /**
     * flag to see if changes have been saved before submitting
     */
    private boolean unsavedChanges;

    private boolean nameUnsavedChanged;

    private Button northButton;

    private Button southButton;

    private Button eastButton;

    private Button westButton;

    private Button priorityButton;

    private Button addButton;

    private Button applyButton;

    private Text nameText;

    private Text latitudeText;

    private Text longitudeText;

    private CpaLocationEntry selectedEntry;

    /**
     * Constructor
     *
     * @param shell
     */
    public ChangeCPALocationsDialog(Shell shell) {

        super(shell, SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
                | SWT.RESIZE);
        setText("Change CPA Locations");
    }

    /**
     * Initializes the components.
     *
     * @param shell
     */
    @Override
    protected void initializeComponents(Shell shell) {
        createContents();
    }

    /**
     * Create contents.
     *
     * @param parent
     */
    protected void createContents() {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout mainLayout = new GridLayout(2, false);
        mainComposite.setLayout(mainLayout);

        cpaLocations = AtcfConfigurationManager.getInstance().getCpaLocations();

        createMainArea(mainComposite);
        createControlButtons(mainComposite);
        unsavedChanges = false;
        nameUnsavedChanged = false;
    }

    /**
     * Creates the main area of the GUI
     *
     * @param parent
     */
    protected void createMainArea(Composite parent) {
        // List of entries
        Font fixedWidthFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
        cpaLocationEntryList = new List(parent,
                SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        cpaLocationEntryList.setFont(fixedWidthFont);
        for (String entry : cpaLocations.getCpaLocations().keySet()) {
            cpaLocationEntryList
                    .add(cpaLocations.getCpaLocations().get(entry).toString());
        }
        GridData cpaEntryListGridData = new GridData();
        cpaEntryListGridData.widthHint = 325;
        cpaEntryListGridData.heightHint = 275;
        cpaEntryListGridData.horizontalIndent = 10;
        cpaEntryListGridData.verticalIndent = 5;
        cpaLocationEntryList.setLayoutData(cpaEntryListGridData);
        if (cpaLocationEntryList.getItemCount() > 0) {
            cpaLocationEntryList.select(0);
        }
        cpaLocationEntryList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                populateData(cpaLocations.getEntryFromIndex(
                        cpaLocationEntryList.getSelectionIndex()));
                selectedEntry = cpaLocations.getEntryFromIndex(
                        cpaLocationEntryList.getSelectionIndex());

                unsavedChanges = false;
                nameUnsavedChanged = false;

                addButton.setEnabled(false);
            }
        });

        selectedEntry = cpaLocations.getEntryFromIndex(0);

        // Buttons and text boxes
        Composite right = new Composite(parent, SWT.NONE);
        right.setLayout(new GridLayout(4, false));

        // Name Label
        GridData nameLabelGridData = new GridData();
        nameLabelGridData.horizontalSpan = 4;
        nameLabelGridData.verticalIndent = 12;
        nameLabelGridData.horizontalIndent = 10;
        nameLabelGridData.widthHint = 150;

        Label titleRight = new Label(right, SWT.NONE);
        titleRight.setText("Name (no spaces)");
        titleRight.setLayoutData(nameLabelGridData);

        ModifyListener modifyListener = e -> unsavedChanges = true;

        ModifyListener nameModifyListener = e -> {
            if (selectedEntry != null && !selectedEntry.getDestination()
                    .equals(nameText.getText())) {
                nameUnsavedChanged = true;
                unsavedChanges = true;
            }
        };

        // Name text box
        GridData nameTextGridData = new GridData();
        nameTextGridData.horizontalSpan = 3;
        nameTextGridData.widthHint = 150;
        nameTextGridData.horizontalIndent = 10;

        nameText = new Text(right, SWT.BORDER);
        nameText.setLayoutData(nameTextGridData);
        nameText.addListener(SWT.FocusIn, e -> addButton.setEnabled(true));

        nameText.addModifyListener(nameModifyListener);

        GridData actionButtonGridData = new GridData();
        actionButtonGridData.widthHint = 85;
        actionButtonGridData.horizontalIndent = 10;

        // Add new button
        addButton = new Button(right, SWT.NONE);
        addButton.setText("Add new");
        addButton.setEnabled(false);
        addButton.setLayoutData(actionButtonGridData);
        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewCpaEntry();
            }
        });

        Label latitude = new Label(right, SWT.NONE);
        latitude.setText("Latitude");
        GridData latGridData = new GridData();
        latGridData.horizontalSpan = 3;
        latGridData.verticalIndent = 12;
        latGridData.horizontalIndent = 10;
        latitude.setLayoutData(latGridData);

        applyButton = new Button(right, SWT.NONE);
        applyButton.setText("Apply");
        applyButton.setLayoutData(actionButtonGridData);

        applyButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                applyCpaChanges();
            }
        });

        GridData latLonTextGridData = new GridData();
        latLonTextGridData.widthHint = 50;
        latLonTextGridData.horizontalIndent = 10;

        latitudeText = new Text(right, SWT.BORDER);
        latitudeText.setLayoutData(latLonTextGridData);
        latitudeText.addListener(SWT.FocusIn, e -> addButton.setEnabled(true));
        latitudeText.addModifyListener(modifyListener);

        // RADIOBUTTONS
        GridData latGroupGridData = new GridData();
        latGroupGridData.horizontalIndent = -30;
        latGroupGridData.horizontalSpan = 2;
        latGroupGridData.widthHint = 120;

        Group latGroup = new Group(right, SWT.NONE);
        latGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        latGroup.setLayoutData(latGroupGridData);

        northButton = new Button(latGroup, SWT.RADIO);
        northButton.setText("N");
        northButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                unsavedChanges = true;
            }
        });

        southButton = new Button(latGroup, SWT.RADIO);
        southButton.setText("S");
        southButton.setVisible(true);
        southButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                unsavedChanges = true;
            }
        });

        Button deleteButton = new Button(right, SWT.NONE);
        deleteButton.setText("Delete");
        deleteButton.setLayoutData(actionButtonGridData);
        deleteButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteCpaEntry();
            }
        });

        GridData lonLabelGridData = new GridData();
        lonLabelGridData.horizontalIndent = 10;
        lonLabelGridData.verticalIndent = 12;
        lonLabelGridData.horizontalSpan = 3;

        Label lonLabel = new Label(right, SWT.NONE);
        lonLabel.setText("Longitude");
        lonLabel.setLayoutData(lonLabelGridData);

        Button clearButton = new Button(right, SWT.NONE);
        clearButton.setLayoutData(actionButtonGridData);
        clearButton.setText("Clear");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                nameText.setText("");
                latitudeText.setText("");
                longitudeText.setText("");
                addButton.setEnabled(false);
            }
        });

        longitudeText = new Text(right, SWT.BORDER);
        longitudeText.setLayoutData(latLonTextGridData);
        longitudeText.addListener(SWT.FocusIn, e -> addButton.setEnabled(true));
        longitudeText.addModifyListener(modifyListener);

        GridData lonGroupGridData = new GridData();
        lonGroupGridData.horizontalSpan = 3;
        lonGroupGridData.horizontalIndent = -30;
        lonGroupGridData.widthHint = 90;

        Group lonGroup = new Group(right, SWT.NONE);
        lonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        lonGroup.setLayoutData(lonGroupGridData);

        eastButton = new Button(lonGroup, SWT.RADIO);
        eastButton.setText("E");
        eastButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                unsavedChanges = true;
            }
        });

        westButton = new Button(lonGroup, SWT.RADIO);
        westButton.setText("W");
        westButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                unsavedChanges = true;
            }
        });

        GridData priorityButtonGridData = new GridData();
        priorityButtonGridData.horizontalIndent = 10;

        priorityButton = new Button(right, SWT.CHECK);
        priorityButton.setText("High Priority");
        priorityButton.setLayoutData(priorityButtonGridData);
        priorityButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                unsavedChanges = true;
            }
        });

        populateData(selectedEntry);

    }

    /**
     * Verifies the validity of the destination text field for saving
     *
     * @param added
     *            - whether this method is being called to add a new entry
     * @return validity
     */
    public boolean verifyDestinationField(boolean added) {
        String currText = nameText.getText();
        if (currText.contains(" ") || currText.isEmpty()) {
            return false;
        }

        if (cpaLocations.getCpaLocations().containsKey(currText)
                && nameUnsavedChanged) {
            return false;
        }

        return !(selectedEntry != null
                && nameText.getText().equals(selectedEntry.getDestination())
                && added);
    }

    /**
     * Verifies the validity of the latitude text field for saving
     *
     * @param text
     *            - the text to be verified
     * @return validity
     */
    public boolean verifyLatField(String text) {
        boolean valid = true;
        float temp = 0;
        try {
            temp = Float.parseFloat(text);
        } catch (Exception ex) {
            logger.warn(
                    "ChangeCPALocationsDialog - Error parsing latitude text:"
                            + text,
                    ex);
            return false;
        }

        if (Math.abs(temp) > 90) {
            valid = false;
        }

        return valid;
    }

    /**
     * Verifies the validity of the longitude text field for saving
     *
     * @param text
     *            - the text to be verified
     * @return validity
     */
    public boolean verifyLonField(String text) {
        boolean valid = true;
        float temp = 0;
        try {
            temp = Float.parseFloat(text);
        } catch (Exception ex) {
            logger.warn(
                    "ChangeCPALocationsDialog - Error parsing longitude text:"
                            + text,
                    ex);
            return false;
        }

        if (Math.abs(temp) > 180) {
            valid = false;
        }

        return valid;
    }

    /**
     * Populate the fields from the selected CPA Location Entry
     *
     * @param entry
     */
    public void populateData(CpaLocationEntry entry) {
        if (entry.getDestination() != null) {
            nameText.setText(entry.getDestination());
        } else {
            nameText.setText("");
        }

        if (entry.getLatitude() != 0.0f) {
            latitudeText.setText(String.valueOf(Math.abs(entry.getLatitude())));
        } else {
            latitudeText.setText("");
        }

        if (entry.getLongitude() != 0.0f) {
            longitudeText
                    .setText(String.valueOf(Math.abs(entry.getLongitude())));
        } else {
            longitudeText.setText("");
        }

        priorityButton.setSelection(entry.getPriority() != 0);

        if (entry.getLatitude() != 0.0f && entry.getLatitude() >= 0) {
            northButton.setSelection(true);
            southButton.setSelection(false);
        } else {
            southButton.setSelection(true);
            northButton.setSelection(false);
        }

        if (entry.getLongitude() != 0.0f && entry.getLongitude() >= 0) {
            eastButton.setSelection(true);
            westButton.setSelection(false);
        } else {
            westButton.setSelection(true);
            eastButton.setSelection(false);
        }
    }

    /**
     * Save the currently entered data to the selected CPA Location Entry
     */
    public void updateSelectedEntry() {
        if (selectedEntry != null) {
            selectedEntry.setDestination(nameText.getText());

            float lat = Float.parseFloat(latitudeText.getText());
            lat *= (northButton.getSelection() ? 1 : -1);
            selectedEntry.setLatitude(lat);

            float lon = Float.parseFloat(longitudeText.getText());
            lon *= (eastButton.getSelection() ? 1 : -1);
            selectedEntry.setLongitude(lon);

            selectedEntry.setPriority(priorityButton.getSelection() ? 1 : 0);
        }
    }

    /**
     * Creates the control buttons.
     *
     * @param parent
     */
    protected void createControlButtons(Composite parent) {
        Composite controlButtons = new Composite(parent, SWT.NONE);
        controlButtons.setLayout(new GridLayout(3, true));
        GridData controlButtonsCompositeGridData = AtcfVizUtil
                .horizontalFillGridData();
        controlButtonsCompositeGridData.horizontalSpan = 2;
        controlButtonsCompositeGridData.verticalIndent = 25;
        controlButtons.setLayoutData(controlButtonsCompositeGridData);

        AtcfVizUtil.createHelpButton(controlButtons,
                AtcfVizUtil.buttonGridData());

        Button okButton = new Button(controlButtons, SWT.PUSH);
        okButton.setLayoutData(AtcfVizUtil.buttonGridData());
        okButton.setText("OK");
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AtcfConfigurationManager.getInstance()
                        .saveCpaLocations(cpaLocations);
                close();
            }
        });

        Button cancelButton = new Button(controlButtons, SWT.PUSH);
        cancelButton.setLayoutData(AtcfVizUtil.buttonGridData());
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int save = SWT.NO;
                if (unsavedChanges) {
                    MessageBox saveWarning = new MessageBox(shell,
                            SWT.YES | SWT.NO);
                    saveWarning.setText("Warning");
                    saveWarning.setMessage(
                            "There are unsaved changes. Do you want to save before exiting?");
                    save = saveWarning.open();
                }
                if (save == SWT.NO) {
                    close();
                }
            }
        });

    }

    /**
     * Adds a new entry to the CPA entry list
     */
    private void addNewCpaEntry() {
        boolean destinationTextValid = verifyDestinationField(true);

        boolean latTextValid = verifyLatField(latitudeText.getText());
        boolean lonTextValid = verifyLonField(longitudeText.getText());

        if (destinationTextValid && latTextValid && lonTextValid) {

            float tempLat = Float.parseFloat(latitudeText.getText());
            if (latitudeText.getText().endsWith("S")) {
                tempLat *= -1;
            }
            float tempLon = Float.parseFloat(longitudeText.getText());
            if (longitudeText.getText().endsWith("W")) {
                tempLon *= -1;
            }

            CpaLocationEntry temp = new CpaLocationEntry(tempLat, tempLon,
                    nameText.getText(),
                    (priorityButton.getSelection() ? 1 : 0));
            cpaLocations.add(temp);
            int orderedLocation = cpaLocations.indexOf(temp);
            cpaLocationEntryList.add(temp.toString(), orderedLocation);
            cpaLocationEntryList.select(orderedLocation);
            selectedEntry = temp;
            applyButton.setEnabled(true);
        } else {
            MessageBox invalidWarning = new MessageBox(shell, SWT.OK);
            invalidWarning.setText("Error");
            String errorString = "Your entry contains the following error(s) -";
            if (!destinationTextValid) {
                errorString += "\n The name must not contain spaces and must be unique";
            }
            if (!latTextValid) {
                errorString += "\n The Latitude must be a numerical value between -90 and 90";
            }
            if (!lonTextValid) {
                errorString += "\n The Longitude must be a numerical value between -180 and 180";
            }

            invalidWarning.setMessage(errorString);
            invalidWarning.open();
        }
    }

    /**
     * Applies and visibly updates changes made in the text fields to the CPA
     * List
     */
    private void applyCpaChanges() {
        boolean destinationTextValid = verifyDestinationField(false);

        boolean latTextValid = verifyLatField(latitudeText.getText());
        boolean lonTextValid = verifyLonField(longitudeText.getText());

        if (latTextValid && lonTextValid && destinationTextValid) {
            updateSelectedEntry();
            cpaLocationEntryList.setItem(
                    cpaLocationEntryList.getSelectionIndex(),
                    selectedEntry.toString());
            unsavedChanges = false;
            nameUnsavedChanged = false;
        } else {
            MessageBox invalidWarning = new MessageBox(shell, SWT.OK);
            invalidWarning.setText("Error");
            String errorString = "Your entry contains the following error(s) -";
            if (!destinationTextValid) {
                errorString += "\n The name must not contain spaces and must be unique";
            }
            if (!latTextValid) {
                errorString += "\n The Latitude must be a numerical value between -90 and 90";
            }
            if (!lonTextValid) {
                errorString += "\n The Longitude must be a numerical value between -180 and 180";
            }

            invalidWarning.setMessage(errorString);
            invalidWarning.open();
        }
    }

    /**
     * Deletes the selected entry from the CPA List
     */
    private void deleteCpaEntry() {
        MessageBox saveWarning = new MessageBox(shell, SWT.YES | SWT.NO);
        saveWarning.setText("Warning");
        saveWarning.setMessage("Are you sure you wish to delete this entry?");

        int save = saveWarning.open();
        if (save == SWT.YES && selectedEntry != null) {
            int selected = cpaLocationEntryList.getSelectionIndex();
            cpaLocations.remove(selectedEntry);
            cpaLocationEntryList.remove(cpaLocationEntryList.getSelection()[0]);
            if (cpaLocationEntryList.getItemCount() == 0) {
                selectedEntry = null;
                nameText.setText("");
                latitudeText.setText("");
                longitudeText.setText("");
                applyButton.setEnabled(false);
                return;
            }

            if (cpaLocationEntryList.getItemCount() == selected) {
                selected--;
            }

            cpaLocationEntryList.setSelection(selected);
            selectedEntry = cpaLocations.getEntryFromIndex(
                    cpaLocationEntryList.getSelectionIndex());
            populateData(selectedEntry);
            unsavedChanges = false;
            nameUnsavedChanged = false;
        }
    }
}
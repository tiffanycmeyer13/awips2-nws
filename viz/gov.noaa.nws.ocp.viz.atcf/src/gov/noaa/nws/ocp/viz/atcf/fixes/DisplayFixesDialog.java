/**
 * This software was developed and / or modified by NOAA/NWS/OCP/ASDT
 */
package gov.noaa.nws.ocp.viz.atcf.fixes;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

import gov.noaa.nws.ocp.common.atcf.configuration.AtcfConfigurationManager;
import gov.noaa.nws.ocp.common.dataplugin.atcf.Storm;
import gov.noaa.nws.ocp.viz.atcf.main.AtcfSession;

/**
 * Dialog to "Display Fixes by Site..."
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Jun 07, 2017             B. Hebbard  Initial Creation.
 * Jun 05, 2018 #48178      jwu         Updated.
 * Jul 18, 2017 #52658      jwu         Retrieve data from DB & localization.
 * Oct 26, 2018 #54780      jwu         Implement display options.
 * Nov 10, 2020 #84442      wpaintsil   Add Scrollbars.
 * </pre>
 * 
 * @author bhebbard
 * @version 1
 */

public class DisplayFixesDialog extends CaveJFACEDialog {

    public enum NonCenterFixMode {
        HIDE, USE_NON_CENTER_FIX_COLOR, USE_FIX_TYPE_COLOR
    }

    private static final String PLUS_MINUS_LABEL = "+/-";

    private static final String HOURS_LABEL = "hours";

    private static final int APPLY_ID = IDialogConstants.CLIENT_ID + 7587;

    private static final String APPLY_LABEL = "Apply";

    private static final int OK_ID = IDialogConstants.CLIENT_ID + 7588;

    private static final String OK_LABEL = "Ok";

    private static final int CLOSE_ID = IDialogConstants.CLIENT_ID + 7589;

    private static final String CLOSE_LABEL = "Close";

    private static List fixSitesList = null;

    private List fixTypesList = null;

    private Combo dateTimeGroupCombo = null;

    private Button highlightFixesCheckbox = null;

    private Button limitFixDisplayCheckbox = null;

    private Button confidenceCheckbox = null;

    private Button windRadiiCheckbox = null;

    private Button autoLabelCheckbox = null;

    private Button fixSiteLabelsCheckbox = null;

    private Button tAndCILabelsCheckbox = null;

    private Button showCommentsCheckbox = null;

    private Spinner plusMinusHoursForHighlightFixesSpinner = null;

    private Spinner plusMinusHoursForLimitFixDisplaySpinner = null;

    private Spinner plusMinusHoursForConfidenceSpinner = null;

    private Spinner plusMinusHoursForWindRadiiSpinner = null;

    private Spinner plusMinusHoursForAutoLabelSpinner = null;

    /*
     * Instance of display properties.
     */
    private DisplayFixesProperties displayProperties = null;

    /*
     * Instance used to draw fixes.
     */
    private DrawFixesElements drawFixesElements = null;

    /*
     * Instance of this dialog
     */
    private static DisplayFixesDialog instance = null;

    /**
     * Get instance of this dialog
     * 
     * @param parentShell
     *
     * @return instance of this dialog
     */
    public static synchronized DisplayFixesDialog getInstance(
            Shell parentShell) {
        if (instance == null) {
            instance = new DisplayFixesDialog(parentShell);
        }

        return instance;
    }

    /*
     * Constructor
     */
    private DisplayFixesDialog(Shell parentShell) {
        super(parentShell);

        setShellStyle(SWT.MIN | SWT.CLOSE | SWT.MODELESS | SWT.BORDER
                | SWT.TITLE | SWT.RESIZE);

        displayProperties = new DisplayFixesProperties();
        drawFixesElements = new DrawFixesElements();

        // Get configurations
        if (displayProperties.getAvailableFixSites() == null) {
            displayProperties.setAvailableFixSites(AtcfConfigurationManager
                    .getInstance().getFixSites().getAvailableSites());
        }

        if (displayProperties.getAvailableFixTypes() == null) {
            displayProperties.setAvailableFixTypes(AtcfConfigurationManager
                    .getInstance().getFixTypes().getAvailableTypes());
        }

    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);

        String title = "Display Fixes by Site";

        Storm curStorm = AtcfSession.getInstance().getActiveStorm();
        if (curStorm != null) {
            title += (" - " + curStorm.getStormId());
        }

        shell.setText(title);

    }

    /**
     * (non-Javadoc) Create all of the widgets on the Dialog
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
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

        // Composite topComp = new Composite(form, SWT.NONE);

        FormLayout layout0 = new FormLayout();
        layout0.marginLeft = 20;
        layout0.marginRight = 20;
        topComp.setLayout(layout0);

        // "Different Storm..." Button
        Button differentStormButton = new Button(topComp, SWT.PUSH);
        FormData fd = new FormData(160, 40);
        fd.left = new FormAttachment(50, -80);
        differentStormButton.setLayoutData(fd);
        differentStormButton.setText("Different Storm...");
        differentStormButton.setToolTipText(
                "Select a different storm for which to display fixes");

        differentStormButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // TODO in legacy, even if "Choose a Storm" opens and you can
                // pick a storm here, but it displays a message saying "Display
                // Fixes" dialog is already open and "ok" on that message just
                // dismiss the dialog. So the behavior needs to be determined
                // with NHC.
            }
        });

        differentStormButton.setEnabled(false);

        // "Fix Sites" Label, List, and Buttons ("Clear" and "Select All")
        Label fixSitesLabel = new Label(topComp, SWT.None);
        fd = new FormData();
        fd.left = new FormAttachment(16, 0);
        fd.top = new FormAttachment(differentStormButton, 25, SWT.BOTTOM);
        fixSitesLabel.setLayoutData(fd);
        fixSitesLabel.setText("Fix Sites");

        fixSitesList = new List(topComp, SWT.MULTI | SWT.V_SCROLL);
        fixSitesList.setToolTipText("Select sites for which to display fixes");
        fd = new FormData(100, 200);
        fd.left = new FormAttachment(fixSitesLabel, -10, SWT.LEFT);
        fd.top = new FormAttachment(fixSitesLabel, 8, SWT.BOTTOM);
        fixSitesList.setLayoutData(fd);
        fixSitesList.setItems(displayProperties.getAvailableFixSites());
        fixSitesList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties
                        .setSelectedFixSites(fixSitesList.getSelection());
            }
        });

        if (displayProperties.getSelectedFixSites() != null) {
            fixSitesList.setSelection(displayProperties.getSelectedFixSites());
        } else {
            fixSitesList.selectAll();
        }

        displayProperties.setSelectedFixSites(fixSitesList.getSelection());

        Button fixSitesClearButton = new Button(topComp, SWT.PUSH);
        fd = new FormData();
        fd.top = new FormAttachment(fixSitesList, 16, SWT.BOTTOM);
        fd.left = new FormAttachment(fixSitesList, 0, SWT.LEFT);
        fd.right = new FormAttachment(fixSitesList, 0, SWT.RIGHT);
        fixSitesClearButton.setLayoutData(fd);
        fixSitesClearButton.setText("Clear");
        fixSitesClearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fixSitesList.deselectAll();
                displayProperties
                        .setSelectedFixSites(fixSitesList.getSelection());
            }
        });

        Button fixSitesSelectAllButton = new Button(topComp, SWT.PUSH);
        fd = new FormData();
        fd.top = new FormAttachment(fixSitesClearButton, 4, SWT.BOTTOM);
        fd.left = new FormAttachment(fixSitesList, 0, SWT.LEFT);
        fd.right = new FormAttachment(fixSitesList, 0, SWT.RIGHT);
        fixSitesSelectAllButton.setLayoutData(fd);
        fixSitesSelectAllButton.setText("Select All");
        fixSitesSelectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fixSitesList.selectAll();
                displayProperties
                        .setSelectedFixSites(fixSitesList.getSelection());
            }
        });

        // "Fix Types" Label, List, and Buttons ("Clear" and "Select All")
        Label fixTypesLabel = new Label(topComp, SWT.None);
        fd = new FormData();
        fd.left = new FormAttachment(fixSitesLabel, 110, SWT.RIGHT);
        fd.top = new FormAttachment(fixSitesLabel, 0, SWT.TOP);
        fixTypesLabel.setLayoutData(fd);
        fixTypesLabel.setText("Fix Types");

        fixTypesList = new List(topComp, SWT.MULTI | SWT.V_SCROLL);
        fixTypesList.setToolTipText("Select fix types to display");
        fd = new FormData(100, 200);
        fd.left = new FormAttachment(fixTypesLabel, -20, SWT.LEFT);
        fd.top = new FormAttachment(fixTypesLabel, 8, SWT.BOTTOM);
        fixTypesList.setLayoutData(fd);
        fixTypesList.setItems(displayProperties.getAvailableFixTypes());
        fixTypesList.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties
                        .setSelectedFixTypes(fixTypesList.getSelection());
            }
        });

        if (displayProperties.getSelectedFixTypes() != null) {
            fixTypesList.setSelection(displayProperties.getSelectedFixTypes());
        } else {
            fixTypesList.selectAll();
        }

        displayProperties.setSelectedFixTypes(fixTypesList.getSelection());

        Button fixTypesClearButton = new Button(topComp, SWT.PUSH);
        fd = new FormData();
        fd.top = new FormAttachment(fixTypesList, 16, SWT.BOTTOM);
        fd.left = new FormAttachment(fixTypesList, 0, SWT.LEFT);
        fd.right = new FormAttachment(fixTypesList, 0, SWT.RIGHT);
        fixTypesClearButton.setLayoutData(fd);
        fixTypesClearButton.setText("Clear");
        fixTypesClearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fixTypesList.deselectAll();
                displayProperties
                        .setSelectedFixTypes(fixTypesList.getSelection());
            }
        });

        Button fixTypesSelectAllButton = new Button(topComp, SWT.PUSH);
        fd = new FormData();
        fd.top = new FormAttachment(fixTypesClearButton, 4, SWT.BOTTOM);
        fd.left = new FormAttachment(fixTypesList, 0, SWT.LEFT);
        fd.right = new FormAttachment(fixTypesList, 0, SWT.RIGHT);
        fixTypesSelectAllButton.setLayoutData(fd);
        fixTypesSelectAllButton.setText("Select All");
        fixTypesSelectAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fixTypesList.selectAll();
                displayProperties
                        .setSelectedFixTypes(fixTypesList.getSelection());
            }
        });

        // Date Time Group
        dateTimeGroupCombo = new Combo(topComp, SWT.None);
        fd = new FormData(160, 30);
        fd.left = new FormAttachment(fixSitesSelectAllButton, -50, SWT.RIGHT);
        fd.top = new FormAttachment(fixSitesSelectAllButton, 30, SWT.BOTTOM);
        dateTimeGroupCombo.setLayoutData(fd);
        dateTimeGroupCombo
                .setItems(displayProperties.getAvailableDateTimeGroups());
        if (displayProperties.getDateTimeGroup() == null) {
            dateTimeGroupCombo.select(dateTimeGroupCombo.getItemCount() - 1);
        } else {
            dateTimeGroupCombo.select(dateTimeGroupCombo
                    .indexOf(displayProperties.getDateTimeGroup()));
        }

        dateTimeGroupCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties
                        .setDateTimeGroup(dateTimeGroupCombo.getText());
                draw();
            }
        });

        if (displayProperties.getDateTimeGroup() != null) {
            dateTimeGroupCombo.setText(displayProperties.getDateTimeGroup());
        } else {
            String[] availableDateTimeGroups = displayProperties
                    .getAvailableDateTimeGroups();
            if (availableDateTimeGroups.length > 0) {
                dateTimeGroupCombo.setText(
                        availableDateTimeGroups[availableDateTimeGroups.length
                                - 1]);
            }
        }

        displayProperties.setDateTimeGroup(dateTimeGroupCombo.getText());

        // DTG combo is enabled when "Highlight" checkbox is selected.
        dateTimeGroupCombo.setEnabled(displayProperties.getHighlightFixes());

        // Display Options Group and individual Checkboxes/Spinners
        Group displayOptionsGroup = new Group(topComp, SWT.ALL);
        displayOptionsGroup.setText("Display Options");
        fd = new FormData(400, 430);
        fd.left = new FormAttachment(50, -200);
        fd.top = new FormAttachment(dateTimeGroupCombo, 20, SWT.BOTTOM);
        displayOptionsGroup.setLayoutData(fd);

        FormLayout displayOptionsLayout = new FormLayout();
        displayOptionsLayout.marginWidth = 10;
        displayOptionsLayout.marginHeight = 10;
        displayOptionsLayout.spacing = 5;
        displayOptionsGroup.setLayout(displayOptionsLayout);

        // "Highlight Fixes" Option
        highlightFixesCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(0, 8);
        fd.left = new FormAttachment(8, 0);
        highlightFixesCheckbox.setLayoutData(fd);
        highlightFixesCheckbox.setText("Highlight Fixes");

        highlightFixesCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean highlightFixes = highlightFixesCheckbox.getSelection();
                displayProperties.setHighlightFixes(highlightFixes);
                plusMinusHoursForHighlightFixesSpinner
                        .setEnabled(highlightFixes);

                // DTG combo is enabled this" checkbox is selected.
                dateTimeGroupCombo.setEnabled(highlightFixes);
                draw();
            }
        });

        highlightFixesCheckbox
                .setSelection(displayProperties.getHighlightFixes());

        plusMinusHoursForHighlightFixesSpinner = new Spinner(
                displayOptionsGroup, SWT.BORDER);
        fd = new FormData();
        fd.top = new FormAttachment(highlightFixesCheckbox, 0, SWT.CENTER);
        fd.left = new FormAttachment(68, 0);
        fd.width = 20;
        plusMinusHoursForHighlightFixesSpinner.setLayoutData(fd);
        plusMinusHoursForHighlightFixesSpinner.setDigits(0);
        plusMinusHoursForHighlightFixesSpinner.setMinimum(1);
        plusMinusHoursForHighlightFixesSpinner.setMaximum(99);
        plusMinusHoursForHighlightFixesSpinner.setSelection(
                displayProperties.getPlusMinusHoursForHighlightFixes());
        plusMinusHoursForHighlightFixesSpinner
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        displayProperties.setPlusMinusHoursForHighlightFixes(
                                plusMinusHoursForHighlightFixesSpinner
                                        .getSelection());
                        draw();
                    }
                });
        plusMinusHoursForHighlightFixesSpinner
                .setEnabled(displayProperties.getHighlightFixes());

        Label plusMinusLabel1 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.right = new FormAttachment(plusMinusHoursForHighlightFixesSpinner,
                -8, SWT.LEFT);
        fd.top = new FormAttachment(plusMinusHoursForHighlightFixesSpinner, 5,
                SWT.TOP);
        plusMinusLabel1.setLayoutData(fd);
        plusMinusLabel1.setText(PLUS_MINUS_LABEL);

        Label hoursLabel1 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.left = new FormAttachment(plusMinusHoursForHighlightFixesSpinner, +8,
                SWT.RIGHT);
        fd.top = new FormAttachment(plusMinusHoursForHighlightFixesSpinner, 5,
                SWT.TOP);
        hoursLabel1.setLayoutData(fd);
        hoursLabel1.setText(HOURS_LABEL);

        // "Limit Fix Display" Option
        limitFixDisplayCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(highlightFixesCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(highlightFixesCheckbox, 0, SWT.LEFT);
        limitFixDisplayCheckbox.setLayoutData(fd);
        limitFixDisplayCheckbox.setText("Limit Fix Display");
        limitFixDisplayCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean limitFixDisplay = limitFixDisplayCheckbox
                        .getSelection();
                displayProperties.setLimitFixDisplay(limitFixDisplay);
                plusMinusHoursForLimitFixDisplaySpinner
                        .setEnabled(limitFixDisplay);
                draw();
            }
        });
        limitFixDisplayCheckbox
                .setSelection(displayProperties.getLimitFixDisplay());

        plusMinusHoursForLimitFixDisplaySpinner = new Spinner(
                displayOptionsGroup, SWT.BORDER);
        fd = new FormData();
        fd.left = new FormAttachment(68, 0);
        fd.top = new FormAttachment(limitFixDisplayCheckbox, 0, SWT.CENTER);
        fd.width = 20;
        plusMinusHoursForLimitFixDisplaySpinner.setLayoutData(fd);
        plusMinusHoursForLimitFixDisplaySpinner.setDigits(0);
        plusMinusHoursForLimitFixDisplaySpinner.setMinimum(1);
        plusMinusHoursForLimitFixDisplaySpinner.setMaximum(99);
        plusMinusHoursForLimitFixDisplaySpinner.setSelection(
                displayProperties.getPlusMinusHoursForLimitFixDisplay());
        plusMinusHoursForLimitFixDisplaySpinner
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        displayProperties.setPlusMinusHoursForLimitFixDisplay(
                                plusMinusHoursForLimitFixDisplaySpinner
                                        .getSelection());
                        draw();
                    }
                });
        plusMinusHoursForLimitFixDisplaySpinner
                .setEnabled(displayProperties.getLimitFixDisplay());

        Label plusMinusLabel2 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.right = new FormAttachment(plusMinusHoursForLimitFixDisplaySpinner,
                -8, SWT.LEFT);
        fd.top = new FormAttachment(plusMinusHoursForLimitFixDisplaySpinner, 5,
                SWT.TOP);
        plusMinusLabel2.setLayoutData(fd);
        plusMinusLabel2.setText(PLUS_MINUS_LABEL);

        Label hoursLabel2 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.left = new FormAttachment(plusMinusHoursForLimitFixDisplaySpinner,
                +10, SWT.RIGHT);
        fd.top = new FormAttachment(plusMinusHoursForLimitFixDisplaySpinner, 5,
                SWT.TOP);
        hoursLabel2.setLayoutData(fd);
        hoursLabel2.setText(HOURS_LABEL);

        // "Confidence" Option
        confidenceCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(limitFixDisplayCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(limitFixDisplayCheckbox, 0, SWT.LEFT);
        confidenceCheckbox.setLayoutData(fd);
        confidenceCheckbox.setText("Confidence");
        confidenceCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean confidence = confidenceCheckbox.getSelection();
                displayProperties.setConfidence(confidence);
                plusMinusHoursForConfidenceSpinner.setEnabled(confidence);
                draw();
            }
        });

        confidenceCheckbox.setSelection(displayProperties.getConfidence());

        plusMinusHoursForConfidenceSpinner = new Spinner(displayOptionsGroup,
                SWT.BORDER);
        fd = new FormData();
        fd.left = new FormAttachment(68, 0);
        fd.top = new FormAttachment(confidenceCheckbox, 0, SWT.CENTER);
        fd.width = 20;
        plusMinusHoursForConfidenceSpinner.setLayoutData(fd);
        plusMinusHoursForConfidenceSpinner.setDigits(0);
        plusMinusHoursForConfidenceSpinner.setMinimum(1);
        plusMinusHoursForConfidenceSpinner.setMaximum(99);
        plusMinusHoursForConfidenceSpinner.setSelection(
                displayProperties.getPlusMinusHoursForConfidence());
        plusMinusHoursForConfidenceSpinner
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        displayProperties.setPlusMinusHoursForConfidence(
                                plusMinusHoursForConfidenceSpinner
                                        .getSelection());
                        draw();
                    }
                });
        plusMinusHoursForConfidenceSpinner
                .setEnabled(displayProperties.getConfidence());

        Label plusMinusLabel3 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.right = new FormAttachment(plusMinusHoursForConfidenceSpinner, -8,
                SWT.LEFT);
        fd.top = new FormAttachment(plusMinusHoursForConfidenceSpinner, 5,
                SWT.TOP);
        plusMinusLabel3.setLayoutData(fd);
        plusMinusLabel3.setText(PLUS_MINUS_LABEL);

        Label hoursLabel3 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.left = new FormAttachment(plusMinusHoursForConfidenceSpinner, +10,
                SWT.RIGHT);
        fd.top = new FormAttachment(plusMinusHoursForConfidenceSpinner, 5,
                SWT.TOP);
        hoursLabel3.setLayoutData(fd);
        hoursLabel3.setText(HOURS_LABEL);

        // "Wind Radii" Option
        windRadiiCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(confidenceCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(confidenceCheckbox, 0, SWT.LEFT);
        windRadiiCheckbox.setLayoutData(fd);
        windRadiiCheckbox.setText("Wind Radii");
        windRadiiCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean windRadii = windRadiiCheckbox.getSelection();
                displayProperties.setWindRadii(windRadii);
                plusMinusHoursForWindRadiiSpinner.setEnabled(windRadii);
                draw();
            }
        });
        windRadiiCheckbox.setSelection(displayProperties.getWindRadii());

        plusMinusHoursForWindRadiiSpinner = new Spinner(displayOptionsGroup,
                SWT.BORDER);
        fd = new FormData();
        fd.left = new FormAttachment(68, 0);
        fd.top = new FormAttachment(windRadiiCheckbox, 0, SWT.CENTER);
        fd.width = 20;
        plusMinusHoursForWindRadiiSpinner.setLayoutData(fd);
        plusMinusHoursForWindRadiiSpinner.setDigits(0);
        plusMinusHoursForWindRadiiSpinner.setMinimum(1);
        plusMinusHoursForWindRadiiSpinner.setMaximum(99);
        plusMinusHoursForWindRadiiSpinner.setSelection(
                displayProperties.getPlusMinusHoursForWindRadii());
        plusMinusHoursForWindRadiiSpinner
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        displayProperties.setPlusMinusHoursForWindRadii(
                                plusMinusHoursForWindRadiiSpinner
                                        .getSelection());
                        draw();
                    }
                });
        plusMinusHoursForWindRadiiSpinner
                .setEnabled(displayProperties.getWindRadii());

        Label plusMinusLabel4 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.right = new FormAttachment(plusMinusHoursForWindRadiiSpinner, -8,
                SWT.LEFT);
        fd.top = new FormAttachment(plusMinusHoursForWindRadiiSpinner, 5,
                SWT.TOP);
        plusMinusLabel4.setLayoutData(fd);
        plusMinusLabel4.setText(PLUS_MINUS_LABEL);

        Label hoursLabel4 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.left = new FormAttachment(plusMinusHoursForWindRadiiSpinner, +10,
                SWT.RIGHT);
        fd.top = new FormAttachment(plusMinusHoursForWindRadiiSpinner, 5,
                SWT.TOP);
        hoursLabel4.setLayoutData(fd);
        hoursLabel4.setText(HOURS_LABEL);

        // "Autolabel" Option
        autoLabelCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(windRadiiCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(windRadiiCheckbox, 0, SWT.LEFT);
        autoLabelCheckbox.setLayoutData(fd);
        autoLabelCheckbox.setText("Autolabel");
        autoLabelCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean autoLabel = autoLabelCheckbox.getSelection();
                displayProperties.setAutoLabel(autoLabel);
                plusMinusHoursForAutoLabelSpinner.setEnabled(autoLabel);
                fixSiteLabelsCheckbox.setEnabled(autoLabel);
                tAndCILabelsCheckbox.setEnabled(autoLabel);
                showCommentsCheckbox.setEnabled(autoLabel);

                draw();
            }
        });
        autoLabelCheckbox.setSelection(displayProperties.getAutoLabel());
        displayProperties.setAutoLabel(autoLabelCheckbox.getSelection());

        plusMinusHoursForAutoLabelSpinner = new Spinner(displayOptionsGroup,
                SWT.BORDER);
        fd = new FormData();
        fd.left = new FormAttachment(68, 0);
        fd.top = new FormAttachment(autoLabelCheckbox, 0, SWT.CENTER);
        fd.width = 20;
        plusMinusHoursForAutoLabelSpinner.setLayoutData(fd);
        plusMinusHoursForAutoLabelSpinner.setDigits(0);
        plusMinusHoursForAutoLabelSpinner.setMinimum(1);
        plusMinusHoursForAutoLabelSpinner.setMaximum(99);
        plusMinusHoursForAutoLabelSpinner.setSelection(
                displayProperties.getPlusMinusHoursForAutoLabel());
        plusMinusHoursForAutoLabelSpinner
                .addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        displayProperties.setPlusMinusHoursForAutoLabel(
                                plusMinusHoursForAutoLabelSpinner
                                        .getSelection());
                        draw();
                    }
                });
        plusMinusHoursForAutoLabelSpinner
                .setEnabled(displayProperties.getAutoLabel());

        Label plusMinusLabel5 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.right = new FormAttachment(plusMinusHoursForAutoLabelSpinner, -8,
                SWT.LEFT);
        fd.top = new FormAttachment(plusMinusHoursForAutoLabelSpinner, 5,
                SWT.TOP);
        plusMinusLabel5.setLayoutData(fd);
        plusMinusLabel5.setText(PLUS_MINUS_LABEL);

        Label hoursLabel5 = new Label(displayOptionsGroup, SWT.ALL);
        fd = new FormData();
        fd.left = new FormAttachment(plusMinusHoursForAutoLabelSpinner, +10,
                SWT.RIGHT);
        fd.top = new FormAttachment(plusMinusHoursForAutoLabelSpinner, 5,
                SWT.TOP);
        hoursLabel5.setLayoutData(fd);
        hoursLabel5.setText(HOURS_LABEL);

        // "Fix Site Labels" Option
        fixSiteLabelsCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(autoLabelCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(autoLabelCheckbox, 20, SWT.LEFT);
        fixSiteLabelsCheckbox.setLayoutData(fd);
        fixSiteLabelsCheckbox.setText("Fix Site Labels");
        fixSiteLabelsCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties
                        .setFixSiteLabels(fixSiteLabelsCheckbox.getSelection());
                draw();
            }
        });
        fixSiteLabelsCheckbox
                .setSelection(displayProperties.getFixSiteLabels());
        fixSiteLabelsCheckbox.setEnabled(displayProperties.getAutoLabel());

        // "T And CI Labels" Option

        tAndCILabelsCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(fixSiteLabelsCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(fixSiteLabelsCheckbox, 0, SWT.LEFT);
        tAndCILabelsCheckbox.setLayoutData(fd);
        tAndCILabelsCheckbox.setText("T And CI Labels");
        tAndCILabelsCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties
                        .settAndCILabels(tAndCILabelsCheckbox.getSelection());

                draw();
            }
        });
        tAndCILabelsCheckbox.setSelection(displayProperties.gettAndCILabels());
        tAndCILabelsCheckbox.setEnabled(displayProperties.getAutoLabel());

        // "Add Comments to Label" Option // DEMO - could be removed
        showCommentsCheckbox = new Button(displayOptionsGroup, SWT.CHECK);
        fd = new FormData();
        fd.top = new FormAttachment(tAndCILabelsCheckbox, 6, SWT.BOTTOM);
        fd.left = new FormAttachment(tAndCILabelsCheckbox, 0, SWT.LEFT);
        showCommentsCheckbox.setLayoutData(fd);
        showCommentsCheckbox.setText("Add Comments to Labels");
        showCommentsCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties
                        .setShowComments(showCommentsCheckbox.getSelection());
                draw();
            }
        });
        showCommentsCheckbox.setSelection(displayProperties.getShowComments());
        showCommentsCheckbox.setEnabled(displayProperties.getAutoLabel());

        // "Non-Center Fixes" Option
        Group radioGroup = new Group(displayOptionsGroup, SWT.NONE);
        fd = new FormData();
        fd.top = new FormAttachment(showCommentsCheckbox, 8, SWT.BOTTOM);
        fd.left = new FormAttachment(autoLabelCheckbox, 0, SWT.LEFT);
        radioGroup.setLayoutData(fd);
        radioGroup.setLayout(new RowLayout(SWT.VERTICAL));
        radioGroup.setText("Non-Center Fixes:");

        Button hideButton = new Button(radioGroup, SWT.RADIO);
        hideButton.setText("Hide");
        hideButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties.setNonCenterFixMode(NonCenterFixMode.HIDE);
                draw();
            }
        });
        hideButton.setSelection(displayProperties
                .getNonCenterFixMode() == NonCenterFixMode.HIDE);

        Button useNonCenterFixColorButton = new Button(radioGroup, SWT.RADIO);
        useNonCenterFixColorButton.setText("Use non-center fix color");
        useNonCenterFixColorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties.setNonCenterFixMode(
                        NonCenterFixMode.USE_NON_CENTER_FIX_COLOR);
                draw();
            }
        });
        useNonCenterFixColorButton.setSelection(displayProperties
                .getNonCenterFixMode() == NonCenterFixMode.USE_NON_CENTER_FIX_COLOR);

        Button useFixTypeColorButton = new Button(radioGroup, SWT.RADIO);
        useFixTypeColorButton.setText("Use fix-type color");
        useFixTypeColorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayProperties.setNonCenterFixMode(
                        NonCenterFixMode.USE_FIX_TYPE_COLOR);
                draw();
            }
        });
        useFixTypeColorButton.setSelection(displayProperties
                .getNonCenterFixMode() == NonCenterFixMode.USE_FIX_TYPE_COLOR);

        form.reflow(true);

        return topComp;
    }

    /**
     * Create Apply/Ok/Cancel buttons
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        Button applyBtn = createButton(parent, APPLY_ID, APPLY_LABEL, false);
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                draw();
            }
        });

        Button okBtn = createButton(parent, OK_ID, OK_LABEL, false);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                draw();

                close();
            }
        });

        Button cancelBtn = createButton(parent, CLOSE_ID, CLOSE_LABEL, true);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

    }

    /**
     * @return the displayProperties
     */
    public DisplayFixesProperties getDisplayProperties() {
        return displayProperties;
    }

    /**
     * @param displayProperties
     *            the displayProperties to set
     */
    public void setDisplayProperties(DisplayFixesProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

    /**
     * @return the drawFixesElements
     */
    public DrawFixesElements getDrawFixesElements() {
        return drawFixesElements;
    }

    /**
     * @param drawFixesElements
     *            the drawFixesElements to set
     */
    public void setDrawFixesElements(DrawFixesElements drawFixesElements) {
        this.drawFixesElements = drawFixesElements;
    }

    /**
     * Draw all F-Deck records
     */
    private void draw() {
        drawFixesElements.setDisplayProperties(getDisplayProperties());
        drawFixesElements.draw();
    }

}